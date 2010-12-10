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

import org.jboss.ejb3.async.spi.lang.ThreadLocalStack;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class CurrentAsyncInvocation
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CurrentAsyncInvocation.class);

   /**
    * Current invocations in play
    */
   private static final ThreadLocalStack<AsyncInvocationId> CURRENT_EXECUTING_INVOCATIONS = new ThreadLocalStack<AsyncInvocationId>();

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Internal ctor, prohibited use
    */
   private CurrentAsyncInvocation()
   {
      throw new UnsupportedOperationException("No instances");
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods--------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the current {@link AsyncInvocationId} from the thread (local)
    * @return
    */
   public static AsyncInvocationId getCurrentAsyncInvocationId()
   {
      // Attempt to get from the Thread (local)
      return CURRENT_EXECUTING_INVOCATIONS.get();
   }

   public static void markCurrentInvocationOnThread(final AsyncInvocationId uuid)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Putting current invocation on Thread " + Thread.currentThread() + ":  " + uuid);
      }
      CURRENT_EXECUTING_INVOCATIONS.push(uuid);
   }

   public static AsyncInvocationId unmarkCurrentInvocationFromThread()
   {
      final AsyncInvocationId id = CURRENT_EXECUTING_INVOCATIONS.pop();
      if (log.isTraceEnabled())
      {
         log.trace("Removing current invocation from Thread " + Thread.currentThread() + ":  " + id);
      }
      return id;
   }
}
