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
package org.jboss.ejb3.async.impl.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * {@link ExecutorService} implementation which submits all
 * incoming {@link Callable}s or {@link Runnable}s as
 * {@link ResultUnwrappingFuture} such that the bean provider's
 * true value may be obtained as a result.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ResultUnwrappingExecutorService implements ExecutorService
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Internal delegate
    */
   private final ExecutorService delegate;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructs a new instance using the specified {@link ExecutorService}
    * delegate
    * 
    * @param delegate {@link ExecutorService} implementation used to carry out all tasks
    * @throws IllegalArgumentException If the delegate is not specified
    */
   public ResultUnwrappingExecutorService(final ExecutorService delegate) throws IllegalArgumentException
   {
      if (delegate == null)
      {
         throw new IllegalArgumentException("Delegate " + ExecutorService.class.getSimpleName() + " must be specified");
      }

      // Set
      this.delegate = delegate;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * The "submit" methods below effectively perform the same
    * function as those specified by AbstractExecutorService, 
    * though we'll use our own j.u.c.Future implementation 
    * in order to unwrap an AsyncResult return type given by the
    * bean provider
    */

   /**
    * {@inheritDoc}}
    * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
    */
   @Override
   public <T> Future<T> submit(final Callable<T> task)
   {
      if (task == null)
         throw new NullPointerException();
      final FutureTask<T> ftask = new ResultUnwrappingFuture<T>(task);
      delegate.execute(ftask);
      return ftask;
   }

   /**
    * {@inheritDoc}
    * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
    */
   @Override
   public <T> Future<T> submit(final Runnable task, final T result)
   {
      if (task == null)
         throw new NullPointerException();
      final FutureTask<T> ftask = new ResultUnwrappingFuture<T>(task, result);
      delegate.execute(ftask);
      return ftask;
   }

   /**
    * {@inheritDoc}
    * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
    */
   @Override
   public Future<?> submit(final Runnable task)
   {
      if (task == null)
         throw new NullPointerException();
      final FutureTask<Object> ftask = new ResultUnwrappingFuture<Object>(task, null);
      delegate.execute(ftask);
      return ftask;
   }

   /*
    * Everything below this line delegates 
    * to the delegate ES
    */

   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
   {
      return delegate.awaitTermination(timeout, unit);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
   {
      return delegate.invokeAll(tasks);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
         throws InterruptedException
   {
      return delegate.invokeAll(tasks, timeout, unit);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
   {
      return delegate.invokeAny(tasks);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
         throws InterruptedException, ExecutionException, TimeoutException
   {
      return delegate.invokeAny(tasks, timeout, unit);
   }

   @Override
   public boolean isShutdown()
   {
      return delegate.isShutdown();
   }

   @Override
   public boolean isTerminated()
   {
      return delegate.isTerminated();
   }

   @Override
   public void shutdown()
   {
      delegate.shutdown();
   }

   @Override
   public List<Runnable> shutdownNow()
   {
      return delegate.shutdownNow();
   }

   @Override
   public void execute(Runnable command)
   {
      // TODO Auto-generated method stub

   }

}
