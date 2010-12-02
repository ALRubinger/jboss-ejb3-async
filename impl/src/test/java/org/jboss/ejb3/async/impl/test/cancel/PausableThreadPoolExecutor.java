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
package org.jboss.ejb3.async.impl.test.cancel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExecutorService} implementation which may be paused, suspending requests
 * to submit new jobs until resumed
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Whether or not the service is active
    */
   private volatile boolean active;

   /**
    * All tasks to be executed while paused
    */
   private final Set<Runnable> backlog = new HashSet<Runnable>();

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
         TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
         TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
         TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
         TimeUnit unit, BlockingQueue<Runnable> workQueue)
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * 
    * @see org.jboss.ejb3.async.impl.util.concurrent.ResultUnwrappingExecutorService#execute(java.lang.Runnable)
    */
   @Override
   public void execute(final Runnable command)
   {
      if (active)
      {
         super.execute(command);
      }
      else
      {
         backlog.add(command);
      }
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   synchronized void pause()
   {
      if (!active)
      {
         // NOOP
         return;
      }

      // Set flag
      active = false;
   }

   synchronized void resume()
   {
      if (active)
      {
         // NOOP
         return;
      }

      // Execute all pending tasks
      for (final Runnable task : backlog)
      {
         super.execute(task);
      }

      // Clear the backlog
      backlog.clear();

      // Set flag
      active = true;
   }

}
