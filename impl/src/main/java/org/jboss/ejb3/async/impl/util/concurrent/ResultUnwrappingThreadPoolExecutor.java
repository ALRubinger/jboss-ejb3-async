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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ResultUnwrappingThreadPoolExecutor
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ResultUnwrappingThreadPoolExecutor extends ThreadPoolExecutor
{

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Delegates to super implementation only
    */
   public ResultUnwrappingThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime,
         final TimeUnit unit, final BlockingQueue<Runnable> workQueue)
   {
      super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
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

   @Override
   public <T> Future<T> submit(final Callable<T> task)
   {
      if (task == null)
         throw new NullPointerException();
      FutureTask<T> ftask = new ResultUnwrappingFuture<T>(task);
      execute(ftask);
      return ftask;
   }

   @Override
   public <T> Future<T> submit(final Runnable task, final T result)
   {
      if (task == null)
         throw new NullPointerException();
      FutureTask<T> ftask = new ResultUnwrappingFuture<T>(task, result);
      execute(ftask);
      return ftask;
   }

   @Override
   public Future<?> submit(final Runnable task)
   {
      if (task == null)
         throw new NullPointerException();
      FutureTask<Object> ftask = new ResultUnwrappingFuture<Object>(task, null);
      execute(ftask);
      return ftask;
   }

}
