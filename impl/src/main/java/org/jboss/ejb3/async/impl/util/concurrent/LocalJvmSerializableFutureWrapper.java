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
package org.jboss.ejb3.async.impl.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;

/**
 * Simple wrapper which makes {@link Future} implementations {@link Serializable}
 * with the following restrictions:
 * 
 * 1) Only one serialization request using this class may be made per-Thread at any time
 * 2) This mechanism will work only while staying in the same JVM, not across the wire
 * to other processes.  While this wrapper is itself serializable, it maintains references
 * which must be available upon deserialization. 
 * 
 * This class is in place to assist in pass-by-value semantics.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
class LocalJvmSerializableFutureWrapper<V> implements Serializable, Future<V>
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   /**
    * Holds a reference to the delegate during serialization of the instance.  This mechanism
    * makes this wrapper class not safe for more than one serialization request per thread (though
    * in ejb3-async this is OK as each async request has thread confinement). 
    */
   private static final ThreadLocal<Future<?>> DELEGATE_REFERENCE_DURING_SERIALIZATION = new ThreadLocal<Future<?>>();

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(LocalJvmSerializableFutureWrapper.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The delegate for all operations; must never be null
    */
   private transient Future<V> delegate;

   /**
    * Creates a new instance to wrap the specified delegate
    * @param delegate
    */
   LocalJvmSerializableFutureWrapper(final Future<V> delegate)
   {

      if (delegate == null)
      {
         throw new IllegalArgumentException("delegate must be specified");
      }

      // Set
      this.delegate = delegate;
   }

   // --------------------------------------------------------------------------------||
   // Serialization ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Serializes the invocation with a custom form
    * 
    * @serialData After all non-transient fields are written, we
    * manually store the non-serializable delegate reference in 
    * {@link LocalJvmSerializableFutureWrapper#DELEGATE_REFERENCE_DURING_SERIALIZATION}.
    */
   private void writeObject(final ObjectOutputStream out) throws IOException
   {
      // Default write of non-transient fields
      out.defaultWriteObject();

      // Store the reference to our delegate
      DELEGATE_REFERENCE_DURING_SERIALIZATION.set(delegate);
   }

   /**
    * Deserializes according to the custom form 
    * defined by {@link LocalJvmSerializableFutureWrapper#writeObject(ObjectOutputStream)}
    */
   @SuppressWarnings("unchecked")
   // We can't have static fields use non-static generics "V", so unchecked
   // cast is needed
   private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      // Get default form
      in.defaultReadObject();

      // Get out the delegate reference
      delegate = (Future<V>) DELEGATE_REFERENCE_DURING_SERIALIZATION.get();
      // Enforce postconditions
      if (delegate == null)
      {
         throw new IllegalStateException("Delegate was null during deserialization");
      }
      // Clear the ThreadLocal
      DELEGATE_REFERENCE_DURING_SERIALIZATION.set(null);
   }

   // --------------------------------------------------------------------------------||
   // Delegate Methods ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public boolean cancel(boolean mayInterruptIfRunning)
   {
      return delegate.cancel(mayInterruptIfRunning);
   }

   public boolean isCancelled()
   {
      return delegate.isCancelled();
   }

   public boolean isDone()
   {
      return delegate.isDone();
   }

   public V get() throws InterruptedException, ExecutionException
   {
      return delegate.get();
   }

   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
   {
      return delegate.get(timeout, unit);
   }

}
