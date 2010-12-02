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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

/**
 * PausableBlockingQueue
 * 
 * Does not comply with contracts provided by j.u.c.BlockingQueue.  For
 * testing only.
 * 
 * This implementation supports "pausing" a work queue such that no new tasks 
 * will be submitted while inactive.  Upon reactivation the backlog of tasks 
 * will be added to the queue.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class PausableBlockingQueue<E> implements BlockingQueue<E>
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(PausableBlockingQueue.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private final BlockingQueue<E> delegate;

   private final BlockingQueue<E> backlog;

   private volatile boolean active;

   private static final String MSG_UNSUPPORTED = "Should not be used in testing";

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public PausableBlockingQueue(boolean active)
   {
      this.delegate = new ArrayBlockingQueue<E>(10);
      this.backlog = new ArrayBlockingQueue<E>(10);
      this.active = active;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   boolean isActive()
   {
      return this.active;
   }

   synchronized void pause()
   {
      if (!this.isActive())
      {
         //NOOP
         return;
      }

      // Move everything to the backlog
      this.delegate.drainTo(this.backlog);

      // Set flag
      this.active = false;

      log.info("Paused");
   }

   synchronized void resume()
   {
      if (this.isActive())
      {
         //NOOP
         return;
      }

      // Move everything back into the delegate
      this.backlog.drainTo(this.delegate);

      // Set flag
      this.active = true;

      log.info("Resumed");
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Offers to the current queue in play
    */
   @Override
   public boolean offer(final E o)
   {
      if (this.isActive())
      {
         log.info("Offering: " + o + " to " + delegate);
         return delegate.offer(o);
      }
      else
      {
         backlog.add(o);
         return true;
      }
   }

   @Override
   public E take() throws InterruptedException
   {
      log.info("Taking from delegate: " + delegate);
      log.info("Backlog is: " + this.backlog);
      final E value = this.delegate.take();
      log.info("Got: " + value);
      return value;
   }

   @Override
   public boolean isEmpty()
   {
      return this.delegate.isEmpty();
   }

   /*
    * UNSUPPORTED below this marker
    */
   @Override
   public boolean add(E o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public int drainTo(Collection<? super E> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public int drainTo(Collection<? super E> c, int maxElements)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public E poll(long timeout, TimeUnit unit) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public void put(E o) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public int remainingCapacity()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public E element()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public E peek()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public E poll()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public E remove()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean addAll(Collection<? extends E> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public void clear()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean contains(Object o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public Iterator<E> iterator()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean remove(Object o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public int size()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public Object[] toArray()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public <T> T[] toArray(T[] a)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

}
