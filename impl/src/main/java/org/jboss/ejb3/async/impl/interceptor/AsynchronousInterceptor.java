/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.async.impl.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.async.impl.ClientExecutorService;
import org.jboss.ejb3.async.spi.AsyncInvocation;
import org.jboss.ejb3.async.spi.AsyncInvocationContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AsyncMethodMetaData;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;
import org.jboss.security.SecurityContext;

/**
 * Examines invocation metadata to determine if this
 * should be handled asynchronously; if so, short-circuits and
 * spawns off into a new Thread.
 * 
 * If the invocation has been equipped with an {@link AsyncInvocationContext} 
 * (ie. is of type {@link AsyncInvocation}), the associated {@link ExecutorService}
 * will be used.  Else we'll provide an {@link ExecutorService}
 * implementation on behalf of the client.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsynchronousInterceptor implements Interceptor, Serializable
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(AsynchronousInterceptor.class);

   /*
    * Metadata attachments flagging this invocation's already been dispatched
    */
   private static final String INVOCATION_METADATA_TAG = "ASYNC";

   private static final String INVOCATION_METADATA_ATTR = "BEEN_HERE";

   private static final String INVOCATION_METADATA_VALUE = Boolean.TRUE.toString();

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Asynchronous Methods to be handled by this interceptor
    */
   private final AsyncMethodsMetaData asyncMethods;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    */
   public AsynchronousInterceptor(final AsyncMethodsMetaData asyncMethods)
   {
      assert asyncMethods != null : "Async Methods must be supplied";
      this.asyncMethods = asyncMethods;
      log.debug("Created: " + this + " to handle " + asyncMethods);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getSimpleName();
   }

   /**
    * {@inheritDocs}
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   public Object invoke(final Invocation invocation) throws Throwable
   {
      // If asynchronous
      if (this.isAsyncInvocation(invocation))
      {
         // Spawn
         return this.invokeAsync(invocation);
      }
      // Regular synchronous call
      else
      {
         // Continue along the chain
         return invocation.invokeNext();
      }
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Breaks off the specified invocation into 
    * a queue for asynchronous processing, returning 
    * a handle to the task
    */
   private Future<?> invokeAsync(final Invocation invocation)
   {
      // Get the appropriate ExecutorService
      final ExecutorService executorService = this.getAsyncExecutor(invocation);

      // Get the existing SecurityContext
      final SecurityContext sc = SecurityActions.getSecurityContext();

      // Copy the invocation (must be done for Thread safety, as we spawn this off and 
      // subsequent calls can mess with the internal interceptor index)
      final Invocation nextInvocation = invocation.copy();

      // Mark that we've already been async'd, so when the invocation comes around again we don't infinite loop
      nextInvocation.getMetaData().addMetaData(INVOCATION_METADATA_TAG, INVOCATION_METADATA_ATTR,
            INVOCATION_METADATA_VALUE);

      // Make the asynchronous task from the invocation
      final Callable<Object> asyncTask = new AsyncInvocationTask<Object>(nextInvocation, sc);

      // Short-circuit the invocation into new Thread 
      final Future<Object> task = executorService.submit(asyncTask);
      if (log.isTraceEnabled())
      {
         log.trace("Submitting async invocation " + invocation + " via " + executorService);
      }

      // Return
      return task;
   }

   /**
    * Determines whether the specified invocation is asynchronous
    * by inspecting its metadata
    * 
    * EJB 3.1 4.5.2.2
    */
   private boolean isAsyncInvocation(final Invocation invocation)
   {
      // Precondition check
      if (log.isTraceEnabled())
      {
         log.trace("Checking to see if async: " + invocation);
      }
      assert invocation instanceof MethodInvocation : this.getClass().getName() + " supports only "
            + MethodInvocation.class.getSimpleName() + ", but has been passed: " + invocation;
      final MethodInvocation si = (MethodInvocation) invocation;

      //       See if we've already been here, if so, don't handle as async
      final String beenHere = (String) invocation.getMetaData().getMetaData(INVOCATION_METADATA_TAG,
            INVOCATION_METADATA_ATTR);
      if (beenHere != null && beenHere.equals(INVOCATION_METADATA_VALUE))
      {
         // Do not handle
         if(log.isTraceEnabled())
         {
            log.trace("Been here, not dispatching as async again");
         }
         return false;
      }

      // Get the actual method
      final Method actualMethod = si.getActualMethod();

      // Loop through the declared async methods for this EJB
      for (final AsyncMethodMetaData asyncMethod : asyncMethods)
      {
         // Name matches?
         final String invokedMethodName = actualMethod.getName();
         if (invokedMethodName.equals(asyncMethod.getMethodName()))
         {
            log.info("Async method names match: " + invokedMethodName);

            // Params match?
            final MethodParametersMetaData asyncParams = asyncMethod.getMethodParams();
            final Class<?>[] invokedParams = actualMethod.getParameterTypes();
            final int invokedParamsSize = invokedParams.length;
            if (asyncParams.size() != invokedParams.length)
            {
               log.info("Different async params size, no match");
               return false;
            }
            for (int i = 0; i < invokedParamsSize; i++)
            {
               final String invokedParamTypeName = invokedParams[i].getName();
               final String declaredName = asyncParams.get(i);
               if (!invokedParamTypeName.equals(declaredName))
               {
                  return false;
               }
            }

            // Name and params all match
            if (log.isTraceEnabled())
            {
               log.trace("Dispatching as @Asynchronous: " + actualMethod);
            }
            return true;
         }
      }

      // Not async
      log.info("NOT ASYNC");
      return false;
   }

   /**
    * Obtains an appropriate {@link ExecutorService} to handle the invocation
    * based upon the type of {@link Invocation} provided.  If we're got a 
    * {@link AsyncInvocation}, the associated {@link ExecutorService} will be used,
    * else we'll supply a default one.
    * 
    * @param invocation
    * @return
    */
   private ExecutorService getAsyncExecutor(final Invocation invocation)
   {
      // Precondition checks
      assert invocation != null : "Invocation must be specified";

      // If this invocation has been equipped with an associated ES
      if (invocation instanceof AsyncInvocation)
      {

         // Cast
         final AsyncInvocation asyncInvocation = (AsyncInvocation) invocation;

         // Get out the ES
         final AsyncInvocationContext context = asyncInvocation.getAsyncInvocationContext();
         assert context != null : "async invocation context of " + invocation + " was null";
         final ExecutorService es = context.getAsynchronousExecutor();
         assert es != null : ExecutorService.class.getSimpleName() + " associated with " + context + " was null";
         return es;

      }
      // Supply our own ES for the client
      else
      {
         return ClientExecutorService.INSTANCE;
      }
   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Task to invoke the held invocation in a new Thread, either 
    * returning the result or throwing the generated Exception
    */
   private class AsyncInvocationTask<V> implements Callable<V>
   {
      private final Invocation invocation;

      /**
       * SecurityContext to use for the invocation
       */
      private final SecurityContext sc;

      public AsyncInvocationTask(final Invocation invocation, final SecurityContext sc)
      {
         this.invocation = invocation;
         this.sc = sc;
      }

      @SuppressWarnings("unchecked")
      public V call() throws Exception
      {
         // Get existing security context
         final SecurityContext oldSc = SecurityActions.getSecurityContext();

         try
         {
            // Set new sc
            SecurityActions.setSecurityContext(this.sc);

            // Invoke
            return (V) invocation.invokeNext();
         }
         catch (Throwable t)
         {
            throw new Exception(t);
         }
         finally
         {
            // Replace the old security context
            SecurityActions.setSecurityContext(oldSc);
         }
      }

   }

}
