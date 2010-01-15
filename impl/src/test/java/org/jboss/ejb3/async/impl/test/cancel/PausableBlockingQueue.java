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

   private final BlockingQueue<E> backlogQueue;

   /**
    * A reference to the current queue to be consulted in polling
    */
   /*
    * volatile because we don't want to synchronize access 
    * here for queue operations "offer" and "take", but we
    * need the Thread visibility to be correct
    */
   private volatile BlockingQueue<E> currentQueue;

   private static final String MSG_UNSUPPORTED = "Should not be used in testing";

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public PausableBlockingQueue(boolean active)
   {
      this.delegate = new ArrayBlockingQueue<E>(10);
      this.backlogQueue = new ArrayBlockingQueue<E>(10);
      this.currentQueue = active ? this.delegate : this.backlogQueue;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public boolean isActive()
   {
      return this.currentQueue == this.delegate ? true : false;
   }

   public void pause()
   {
      if (!this.isActive())
      {
         //NOOP
         return;
      }

      // Atomic
      synchronized (this.currentQueue)
      {
         this.currentQueue = this.backlogQueue;
         log.info(this + ": Paused");
      }
   }

   public void resume()
   {
      if (this.isActive())
      {
         //NOOP
         return;
      }

      // Atomic
      synchronized (this.currentQueue)
      {
         // Drain to the delegate from the backlog
         this.backlogQueue.drainTo(this.delegate);

         // Set the correct current queue
         this.currentQueue = this.delegate;

         // Log
         log.info(this + ": Resumed");
      }
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Offers to the current queue in play
    */
   public boolean offer(final E o)
   {
      final BlockingQueue<E> current = this.currentQueue;
      log.info("Offering: " + o + " to " + current);
      return current.offer(o);
   }

   public E take() throws InterruptedException
   {
      final BlockingQueue<E> current = this.currentQueue;
      final E obj = current.take();
      log.info("Taking : " + obj);
      return obj;
   }

   public boolean isEmpty()
   {
      return this.currentQueue.isEmpty();
   }

   /*
    * UNSUPPORTED below this marker
    */

   public boolean add(E o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public int drainTo(Collection<? super E> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public int drainTo(Collection<? super E> c, int maxElements)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public E poll(long timeout, TimeUnit unit) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public void put(E o) throws InterruptedException
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public int remainingCapacity()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public E element()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public E peek()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public E poll()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public E remove()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean addAll(Collection<? extends E> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public void clear()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean contains(Object o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean containsAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public Iterator<E> iterator()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean remove(Object o)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public int size()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public Object[] toArray()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   public <T> T[] toArray(T[] a)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

}
