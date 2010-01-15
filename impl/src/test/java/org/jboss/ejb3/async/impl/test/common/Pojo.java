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

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;

/**
 * Pojo
 * 
 * A simple POJO to act as BeanContext for a test Container,
 * candidate for asynchronous interception
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Pojo
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static final String VALUE = "Test Value";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public final AtomicInteger counter = new AtomicInteger();

   // --------------------------------------------------------------------------------||
   // Business Methods ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains a contracted value, asynchronously
    * 
    * @return
    */
   public Future<String> getValueAsynchronous()
   {
      return new AsyncResult<String>(this.getValueSynchronous());
   }

   /**
    * Obtains a contracted value
    * 
    * @return
    */
   public String getValueSynchronous()
   {
      return VALUE;
   }

   /**
    * Increments the internal counter; intentionally
    * void return to test @Asynchronous on void
    */
   @Asynchronous
   public void incrementCounterAsynchronous()
   {
      // The @Asynchronous annotation here will spawn this off,
      // so just delegate to the synchronous handling 
      this.incrementCounterSynchronous();
   }

   /**
    * Obtains the internal counter
    * 
    * @return
    */
   public Future<Integer> getCounter()
   {
      return new AsyncResult<Integer>(counter.intValue());
   }

   /**
    * Increments the internal counter, though
    * does so synchronously (not a candidate for
    * interception)
    */
   public void incrementCounterSynchronous()
   {
      counter.incrementAndGet();
   }

}
