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
package org.jboss.ejb3.async.impl.test.cancel;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.ejb3.async.impl.test.common.ThreadPoolAsyncContainer;
import org.jboss.ejb3.async.impl.util.concurrent.ResultUnwrappingExecutorService;

/**
 * PausableProcessingAsyncContainer
 * 
 * A Container which permits blocking upon the work queue to pause processing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class PausableProcessingAsyncContainer<T> extends ThreadPoolAsyncContainer<T>

{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying pausable queue
    */
   private PausableBlockingQueue<Runnable> queue;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Internal ctor
    */
   private PausableProcessingAsyncContainer(final String name, final String domainName,
         final Class<? extends T> beanClass, PausableBlockingQueue<Runnable> queue)
   {
      super(name, domainName, beanClass, new ResultUnwrappingExecutorService(new ThreadPoolExecutor(3, 6, 3,
            TimeUnit.SECONDS, queue)));
      this.queue = queue;
   }

   /**
    * Factory create method
    * @param <T>
    * @param name
    * @param domainName
    * @param beanClass
    * @return
    */
   public static <T> PausableProcessingAsyncContainer<T> create(final String name, final String domainName,
         final Class<T> beanClass)
   {
      final PausableBlockingQueue<Runnable> queue = new PausableBlockingQueue<Runnable>(false);
      return new PausableProcessingAsyncContainer<T>(name, domainName, beanClass, queue);
   }

   /**
    * Obtains the queue (blockable) behind the container
    * @return
    */
   public PausableBlockingQueue<Runnable> getQueue()
   {
      return queue;
   }

}
