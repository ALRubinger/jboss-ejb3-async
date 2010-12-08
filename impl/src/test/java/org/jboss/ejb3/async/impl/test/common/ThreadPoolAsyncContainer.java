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
package org.jboss.ejb3.async.impl.test.common;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.async.impl.interceptor.AsynchronousClientInterceptor;
import org.jboss.ejb3.async.spi.AsyncCancellableContext;
import org.jboss.ejb3.async.spi.AsyncInvocation;
import org.jboss.ejb3.async.spi.AsyncInvocationContext;
import org.jboss.ejb3.context.base.BaseSessionInvocationContext;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.interceptors.direct.DirectContainer;

/**
 * {@link DirectContainer} extension which dispatches new invocations
 * as {@link AsyncContainerMethodInvocation}; this provides a 
 * {@link AsyncInvocation} as required by the {@link AsynchronousClientInterceptor}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ThreadPoolAsyncContainer<T> extends DirectContainer<T>
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * To be used for asynchronous invocations
    */
   private ExecutorService asynchronousExecutor;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ThreadPoolAsyncContainer(final String name, final String domainName, final Class<? extends T> beanClass)
   {
      this(name, domainName, beanClass, AsyncTestUtil.getDefaultAsyncExecutorService());
   }

   public ThreadPoolAsyncContainer(final String name, final String domainName, final Class<? extends T> beanClass,
         final ExecutorService asynchronousExecutor)
   {
      super(name, domainName, beanClass);
      this.setAsynchronousExecutor(asynchronousExecutor);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ExecutorService getAsynchronousExecutor()
   {
      return asynchronousExecutor;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private void setAsynchronousExecutor(final ExecutorService asynchronousExecutor)
   {
      this.asynchronousExecutor = asynchronousExecutor;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Construct our own {@link Invocation} type to be {@link AsyncContainerMethodInvocation}
    * @see org.jboss.ejb3.interceptors.direct.AbstractDirectContainer#invoke(org.jboss.ejb3.interceptors.container.BeanContext, java.lang.reflect.Method, java.lang.Object[])
    */
   @Override
   public Object invoke(final BeanContext<T> target, final Method method, final Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if (info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      final ContainerMethodInvocation invocation = new AsyncContainerMethodInvocation(info, target, arguments);
      return invocation.invokeNext();
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Members --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * {@link AsyncInvocation} contract fulfilling implementation extension of a container-based
    * method Invocation
    */
   private class AsyncContainerMethodInvocation extends ContainerMethodInvocation implements AsyncInvocation
   {

      AsyncContainerMethodInvocation(MethodInfo info, BeanContext<?> beanContext, Object arguments[])
      {
         super(info, info.getInterceptors());

         assert beanContext != null : "beanContext is null";

         setArguments(arguments);
         setBeanContext(beanContext);
      }

      @Override
      public AsyncInvocationContext getAsyncInvocationContext()
      {
         return new AsyncInvocationContextImpl(this);
      }

      @Override
      public AsyncCancellableContext getCancellableContext()
      {
         // Not needed for this test
         return null;
      }

   }

   /**
    * {@link BaseSessionInvocationContext} extended to plug in 
    * {@link AsyncInvocationContext} support
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    */
   private class AsyncInvocationContextImpl extends BaseSessionInvocationContext implements AsyncInvocationContext
   {

      private final MethodInvocation delegate;

      AsyncInvocationContextImpl(final MethodInvocation delegate)
      {
         super(null, delegate.getMethod(), delegate.getArguments());
         this.delegate = delegate;

      }

      @Override
      public Object proceed() throws Exception
      {
         try
         {
            return delegate.invokeNext();
         }
         catch (Exception e)
         {
            throw e;
         }
         catch (Error e)
         {
            throw (Error) e;
         }
         catch (Throwable t)
         {
            throw new RuntimeException(t);
         }
      }

      @Override
      public ExecutorService getAsynchronousExecutor()
      {
         return asynchronousExecutor;
      }

   }

}
