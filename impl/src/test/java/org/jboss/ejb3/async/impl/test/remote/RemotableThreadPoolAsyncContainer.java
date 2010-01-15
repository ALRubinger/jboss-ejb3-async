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
package org.jboss.ejb3.async.impl.test.remote;

import java.util.concurrent.ExecutorService;

import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.async.impl.test.common.AsyncTestUtil;
import org.jboss.ejb3.async.impl.test.common.ThreadPoolAsyncContainer;
import org.jboss.ejb3.async.spi.container.remote.EndpointConstants;
import org.jboss.ejb3.async.spi.container.remote.RemotableAsyncInvocationProcessor;
import org.jboss.ejb3.async.spi.container.remote.RemoteAsyncTaskRegistry;

/**
 * ThreadPoolAsyncContainer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RemotableThreadPoolAsyncContainer<T> extends ThreadPoolAsyncContainer<T>
      implements
         RemotableAsyncInvocationProcessor
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private RemoteAsyncTaskRegistry remoteAsyncTaskRegistry;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public RemotableThreadPoolAsyncContainer(final String name, final String domainName,
         final Class<? extends T> beanClass, final RemoteAsyncTaskRegistry remoteAsyncTaskRegistry)
   {
      this(name, domainName, beanClass, remoteAsyncTaskRegistry, AsyncTestUtil.getDefaultAsyncExecutorService());
   }

   public RemotableThreadPoolAsyncContainer(final String name, final String domainName,
         final Class<? extends T> beanClass, final RemoteAsyncTaskRegistry remoteAsyncTaskRegistry,
         final ExecutorService asynchronousExecutor)
   {
      super(name, domainName, beanClass, asynchronousExecutor);
      this.setRemoteAsyncTaskRegistry(remoteAsyncTaskRegistry);

      // Register w/ Remoting (R2)
      // In an actual implementation we won't expose "this", but rather a 
      // simplified view which delegates to the Container
      Dispatcher.singleton.registerTarget(EndpointConstants.ASYNCHRONOUS_REMOTING_ENDPOINT_NAME, this);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public RemoteAsyncTaskRegistry getRemoteAsyncTaskRegistry()
   {
      return remoteAsyncTaskRegistry;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private void setRemoteAsyncTaskRegistry(RemoteAsyncTaskRegistry remoteAsyncTaskRegistry)
   {
      this.remoteAsyncTaskRegistry = remoteAsyncTaskRegistry;
   }

}
