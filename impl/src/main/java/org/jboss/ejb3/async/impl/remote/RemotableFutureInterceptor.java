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
package org.jboss.ejb3.async.impl.remote;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.Future;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.async.impl.remote.r2.RemotableAsyncFutureInvocationHandler;
import org.jboss.ejb3.async.spi.container.remote.RemotableAsyncInvocationProcessor;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.logging.Logger;

/**
 * RemotableFutureInterceptor
 *
 * Examines invocation metadata to determine if this
 * is a remote async invocation; if so, stores the returned Future
 * into the RemoteAsyncTaskRegistry and returns a remotable
 * hook to the client
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RemotableFutureInterceptor implements Interceptor
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(RemotableFutureInterceptor.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * No-arg constructor required
    */
   public RemotableFutureInterceptor()
   {
      log.debug("Created: " + this);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.aop.advice.Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getSimpleName();
   }

   /* (non-Javadoc)
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   public Object invoke(final Invocation invocation) throws Throwable
   {
      // Continue along the chain
      final Object returnValue = invocation.invokeNext();

      // If a remote invocation
      if (this.isRemoteInvocation(invocation))
      {
         // If the return value is a Future
         if (returnValue instanceof Future)
         {
            // Cast
            final Future<?> futureReturnValue = (Future<?>) returnValue;

            // Make a UUID so we can reference the return value later
            final UUID uuid = UUID.randomUUID();

            // Put into the Containers Remote Async Task Registry
            this.getInvocationProcessor(invocation).getRemoteAsyncTaskRegistry().put(uuid, futureReturnValue);

            // Return a remoteable hook to the actual Future
            final Class<?>[] interfaces = new Class<?>[]
            {Future.class};
            final RemotableAsyncFutureInvocationHandler handler = new RemotableAsyncFutureInvocationHandler();
            final Future<?> remotableFuture = (Future<?>) Proxy.newProxyInstance(Thread.currentThread()
                  .getContextClassLoader(), interfaces, handler);
            return remotableFuture;
         }
      }

      // NO-OP
      return returnValue;
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Determines whether the specified invocation is asynchronous
    * by inspecting its metadata
    */
   //FIXME This implementation is specific to R2
   private boolean isRemoteInvocation(final Invocation invocation)
   {
      // Precondition check
      assert invocation instanceof MethodInvocation : this.getClass().getName() + " supports only "
            + MethodInvocation.class.getSimpleName() + ", but has been passed: " + invocation;

      // Get out remoting metadata (if it exists)
      Object remotingMetadata = invocation.getMetaData(InvokeRemoteInterceptor.REMOTING,
            InvokeRemoteInterceptor.INVOKER_LOCATOR);

      // Return if the invocation was remote
      return remotingMetadata != null;
   }

   /**
    * Returns the Container associated w/ this Invocation
    * 
    * @return
    */
   //FIXME We can't inject the Container?
   @SuppressWarnings("unchecked")
   private RemotableAsyncInvocationProcessor getInvocationProcessor(final Invocation invocation)
   {
      //TODO This won't work when we integrate w/ ejb3-core, as Advisor will need:
      // ((ManagedObjectAdvisor) invocation.getAdvisor()).getContainer().getEJBContainer();
      return (RemotableAsyncInvocationProcessor) ((ManagedObjectAdvisor) invocation.getAdvisor()).getContainer();
   }

}
