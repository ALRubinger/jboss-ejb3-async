/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.async.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.ejb3.async.spi.AsyncInvocation;

/**
 * Static singleton access to a default {@link ExecutorService} implementation
 * used by clients who do not supply a {@link AsyncInvocation} to handle
 * asynchronous EJB 3.1 invocations.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public enum ClientExecutorService implements ExecutorService {
   INSTANCE;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Singleton instance, uses a backing thread pool AS, fronted by an ES impl to unwrap 
    * the return value
    */
   private final ExecutorService delegate = Executors.newCachedThreadPool();

   // --------------------------------------------------------------------------------||
   // Delegate Methods ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public void execute(Runnable command)
   {
      delegate.execute(command);
   }

   public void shutdown()
   {
      delegate.shutdown();
   }

   public List<Runnable> shutdownNow()
   {
      return delegate.shutdownNow();
   }

   public boolean isShutdown()
   {
      return delegate.isShutdown();
   }

   public boolean isTerminated()
   {
      return delegate.isTerminated();
   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
   {
      return delegate.awaitTermination(timeout, unit);
   }

   public <T> Future<T> submit(Callable<T> task)
   {
      return delegate.submit(task);
   }

   public <T> Future<T> submit(Runnable task, T result)
   {
      return delegate.submit(task, result);
   }

   public Future<?> submit(Runnable task)
   {
      return delegate.submit(task);
   }

   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
   {
      return delegate.invokeAll(tasks);
   }

   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
         throws InterruptedException
   {
      return delegate.invokeAll(tasks, timeout, unit);
   }

   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
   {
      return delegate.invokeAny(tasks);
   }

   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
         throws InterruptedException, ExecutionException, TimeoutException
   {
      return delegate.invokeAny(tasks, timeout, unit);
   }
}
