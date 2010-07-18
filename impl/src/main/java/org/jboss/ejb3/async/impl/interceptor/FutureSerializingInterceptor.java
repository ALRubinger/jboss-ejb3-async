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
package org.jboss.ejb3.async.impl.interceptor;

import java.io.Serializable;
import java.util.concurrent.Future;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.async.impl.SerializableFuture;
import org.jboss.logging.Logger;

/**
 * Ensures the {@link Future} implementation provided by a bean provider
 * is capable of being serialized; if not, provides this mechanism
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class FutureSerializingInterceptor implements Interceptor, Serializable
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FutureSerializingInterceptor.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * No-arg constructor required
    */
   public FutureSerializingInterceptor()
   {
      log.debug("Created: " + this);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getSimpleName();
   }

   /**
    * {@inheritDocs}
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   public Object invoke(final Invocation invocation) throws Throwable
   {
      // Come around after the invocation chain
      Object returnValue = invocation.invokeNext();

      // If this is a Future
      if (returnValue instanceof Future)
      {
         // Cast
         final Future<?> future = (Future<?>) returnValue;

         // If not Serializable
         if (!(returnValue instanceof Serializable))
         {
            // Make it so
            returnValue = new SerializableFuture<Object>(future.get());
         }
      }

      return returnValue;
   }
}
