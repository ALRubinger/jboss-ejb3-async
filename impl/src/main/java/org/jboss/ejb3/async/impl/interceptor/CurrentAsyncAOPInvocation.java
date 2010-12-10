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
package org.jboss.ejb3.async.impl.interceptor;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.async.spi.AsyncInvocation;
import org.jboss.ejb3.async.spi.AsyncInvocationId;
import org.jboss.ejb3.async.spi.CurrentAsyncInvocation;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class CurrentAsyncAOPInvocation
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CurrentAsyncAOPInvocation.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Internal ctor, prohibited use
    */
   private CurrentAsyncAOPInvocation()
   {
      throw new UnsupportedOperationException("No instances");
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods--------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the current {@link AsyncInvocationId}, first looking in the
    * current Thread, then examining the specified invocation metadata.
    * If the current invocation is not asynchronous, null is returned.
    */
   public static AsyncInvocationId getCurrentAsyncInvocationId(final Invocation invocation)
   {
      // Precondition checks
      assert invocation != null : "Invocation must be specified";

      // Attempt to get from the Thread (local)
      AsyncInvocationId current = CurrentAsyncInvocation.getCurrentAsyncInvocationId();

      // Attempt to get from the invocation
      if (current == null)
      {
         current = (AsyncInvocationId) invocation.getMetaData(AsyncInvocation.METADATA_GROUP_ASYNC,
               AsyncInvocation.METADATA_KEY_ID);
      }

      // Return
      return current;
   }

   public static void markCurrentInvocation(final AsyncInvocationId uuid, final Invocation invocation)
   {
      // Precondition checks
      assert invocation != null : "Invocation must be specified";

      CurrentAsyncInvocation.markCurrentInvocationOnThread(uuid);
      invocation.getMetaData().addMetaData(AsyncInvocation.METADATA_GROUP_ASYNC, AsyncInvocation.METADATA_KEY_ID, uuid);
   }

   public static AsyncInvocationId unmarkCurrentInvocation(final Invocation invocation)
   {
      // Pop off
      final AsyncInvocationId current = CurrentAsyncInvocation.unmarkCurrentInvocationFromThread();

      // Remove metadata from the invocation
      invocation.getMetaData().removeMetaData(AsyncInvocation.METADATA_GROUP_ASYNC, AsyncInvocation.METADATA_KEY_ID);

      return current;
   }
}
