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
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.async.impl.AsyncInvocationIdUUIDImpl;
import org.jboss.ejb3.async.impl.ClientExecutorService;
import org.jboss.ejb3.async.impl.util.concurrent.ResultUnwrappingExecutorService;
import org.jboss.ejb3.async.spi.AsyncEndpoint;
import org.jboss.ejb3.async.spi.AsyncInvocation;
import org.jboss.ejb3.async.spi.AsyncInvocationContext;
import org.jboss.ejb3.async.spi.AsyncInvocationId;
import org.jboss.ejb3.async.spi.AsyncInvocationTaskBase;
import org.jboss.ejb3.async.spi.AsyncUtil;
import org.jboss.ejb3.async.spi.CurrentAsyncInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;
import org.jboss.remoting.InvokerLocator;
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
public class AsynchronousClientInterceptor implements Interceptor, Serializable
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
   private static final Logger log = Logger.getLogger(AsynchronousClientInterceptor.class);

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
   public AsynchronousClientInterceptor(final AsyncMethodsMetaData asyncMethods)
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

      // Make a new ID for the invocation
      final AsyncInvocationId id = new AsyncInvocationIdUUIDImpl();

      // Make the asynchronous task from the invocation
      final Callable<Object> asyncTask = new AsyncAOPInvocationTask<Object>(nextInvocation, sc, id);

      // Short-circuit the invocation into new Thread
      final Future<Object> task;
      try
      {
         // Mark the thread w/ the UUID so it can be picked up by the ES during submit, and stuffed into the Future
         CurrentAsyncAOPInvocation.markCurrentInvocation(id, nextInvocation);
         task = executorService.submit(asyncTask);
         if (log.isTraceEnabled())
         {
            log.trace("Submitting async invocation " + invocation + " via " + executorService);
         }
      }
      finally
      {
         // Clear the Thread
         CurrentAsyncInvocation.unmarkCurrentInvocationFromThread();
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

      // See if we've already been here, if so, don't handle as async
      final String beenHere = (String) invocation.getMetaData().getMetaData(INVOCATION_METADATA_TAG,
            INVOCATION_METADATA_ATTR);
      if (beenHere != null && beenHere.equals(INVOCATION_METADATA_VALUE))
      {
         // Do not handle
         if (log.isTraceEnabled())
         {
            log.trace("Been here, not dispatching as async again");
         }
         return false;
      }

      // Get the actual method
      final Method actualMethod = si.getActualMethod();

      // Return if the method is async
      return AsyncUtil.methodIsAsynchronous(actualMethod, asyncMethods);
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
         final InvokerLocator locator = (InvokerLocator) invocation.getMetaData(InvokeRemoteInterceptor.REMOTING,
               InvokeRemoteInterceptor.INVOKER_LOCATOR);
         Object oid = invocation.getMetaData().getMetaData(Dispatcher.DISPATCHER, Dispatcher.OID);
         log.info("OID: " + oid);
         final PojiProxy proxy = new PojiProxyHack(oid, locator, new Interceptor[]
         {});
         final AsyncEndpoint container = (AsyncEndpoint) Proxy.newProxyInstance(Thread.currentThread()
               .getContextClassLoader(), new Class<?>[]
         {AsyncEndpoint.class}, proxy);
         return new ResultUnwrappingExecutorService(ClientExecutorService.INSTANCE, container);
      }
   }

   private static final class PojiProxyHack extends PojiProxy
   {
      /**
       * serialVersionUID
       */
      private static final long serialVersionUID = 1L;

      public PojiProxyHack(Object oid, InvokerLocator uri, Interceptor[] interceptors)
      {
         super(oid, uri, interceptors);
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         final Object obj = super.invoke(proxy, method, args);
         log.info("Invoked view from proxy: " + method);
         return obj;
      }

   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Task to invoke the held invocation in a new Thread, either 
    * returning the result or throwing the generated Exception
    */
   private class AsyncAOPInvocationTask<V> extends AsyncInvocationTaskBase<V>
   {
      private final Invocation invocation;

      public AsyncAOPInvocationTask(final Invocation invocation, final SecurityContext sc, final AsyncInvocationId id)
      {
         super(sc, id);
         assert invocation != null : "Invocation must be supplied";
         this.invocation = invocation;
      }

      @Override
      protected void before() throws Exception
      {
         // Mark the current invocation both on the executing Thread and the Invocation
         CurrentAsyncAOPInvocation.markCurrentInvocation(this.id, this.invocation);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected V proceed() throws Throwable
      {
         return (V) invocation.invokeNext();
      }

      @Override
      protected void after() throws Exception
      {
         // Unmark the current invocation both on the executing Thread and the Invocation
         CurrentAsyncAOPInvocation.unmarkCurrentInvocation(this.invocation);
      }

   }

}
