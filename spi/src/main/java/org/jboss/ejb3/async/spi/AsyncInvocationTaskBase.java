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
package org.jboss.ejb3.async.spi;

import java.util.concurrent.Callable;

import org.jboss.security.SecurityContext;

/**
 * Base {@link Callable} implementation to set the {@link SecurityContext}
 * in the new Thread when this is invoked, and replace when done.  Contains
 * lifecycle hooks for implementations to proceed, and receive events 
 * for before and after invocation
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public abstract class AsyncInvocationTaskBase<V> implements Callable<V>
{
   /**
    * SecurityContext to use for the invocation
    */
   protected final SecurityContext sc;

   /**
    * ID of the invocation
    */
   protected final AsyncInvocationId id;

   public AsyncInvocationTaskBase(final SecurityContext sc, final AsyncInvocationId id)
   {
      assert id != null : "Async Invocation ID must be supplied";
      this.sc = sc;
      this.id = id;
   }

   /**
    * Implementation-specific hook before the invocation is invoked
    * @throws Exception
    */
   protected abstract void before() throws Exception;

   /**
    * Implementation-specific hook to proceed with the invocation
    */
   protected abstract V proceed() throws Throwable;

   /**
    * Implementation-specific hook after the invocation has been invoked
    * @throws Exception
    */
   protected abstract void after() throws Exception;

   /**
    * {@inheritDoc}
    * @see java.util.concurrent.Callable#call()
    */
   public final V call() throws Exception
   {
      // Get existing security context
      final SecurityContext oldSc = SecurityActions.getSecurityContext();

      try
      {
         // Set new sc
         SecurityActions.setSecurityContext(this.sc);

         // Before Callback
         this.before();

         // Invoke
         return this.proceed();
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new Error(t);
      }
      finally
      {
         // Replace the old security context
         SecurityActions.setSecurityContext(oldSc);

         // After callback
         this.after();
      }
   }
}
