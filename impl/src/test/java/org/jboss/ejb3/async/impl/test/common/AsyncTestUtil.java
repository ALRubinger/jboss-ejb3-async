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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.ejb3.async.impl.util.concurrent.ResultUnwrappingThreadPoolExecutor;

/**
 * AsyncTestUtil
 * 
 * A utility class used solely in tests.  Provides
 * tight coupling between components which will typically be
 * together as configurable beans via MC
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public final class AsyncTestUtil
{
   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Non-instanciable
    */
   private AsyncTestUtil()
   {

   }

   // --------------------------------------------------------------------------------||
   // Utilities ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the default ExecutorService to be used for asynchronous
    * invocations.  In reality, this is injected via MC, as a per-container
    * configurable property
    * 
    * @return 
    */
   public static ExecutorService getDefaultAsyncExecutorService()
   {
      return new ResultUnwrappingThreadPoolExecutor(3, 6, 3L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
   }

}
