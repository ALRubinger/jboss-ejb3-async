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

import java.lang.reflect.Method;

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AsyncMethodMetaData;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;

/**
 * Utility class to centralize @Asynchronous business logic
 * for use by adaptors into various interception models
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class AsyncUtil
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(AsyncUtil.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Internal ctor, do not use
    */
   private AsyncUtil()
   {
      throw new UnsupportedOperationException("No instances permitted");
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Determines whether the invoked method is @Asynchronous
    * @param invokedMethod The invoked method
    * @param asyncMethodsForBean Methods declared as asynchronous for the EJB in question
    * @return
    */
   public static boolean methodIsAsynchronous(final Method invokedMethod, final AsyncMethodsMetaData asyncMethodsForBean)
   {
      // Loop through the declared async methods for this EJB
      for (final AsyncMethodMetaData asyncMethod : asyncMethodsForBean)
      {
         // Name matches?
         final String invokedMethodName = invokedMethod.getName();
         if (invokedMethodName.equals(asyncMethod.getMethodName()))
         {
            if (log.isTraceEnabled())
            {
               log.trace("Async method names match: " + invokedMethodName);
            }

            // Params match?
            MethodParametersMetaData asyncParams = asyncMethod.getMethodParams();
            if (asyncParams == null)
            {
               asyncParams = new MethodParametersMetaData();
            }
            final Class<?>[] invokedParams = invokedMethod.getParameterTypes();
            final int invokedParamsSize = invokedParams.length;
            if (asyncParams.size() != invokedParams.length)
            {
               if (log.isTraceEnabled())
               {
                  log.trace("Different async params size, no match");
               }
               return false;
            }
            for (int i = 0; i < invokedParamsSize; i++)
            {
               final String invokedParamTypeName = invokedParams[i].getName();
               final String declaredName = asyncParams.get(i);
               if (!invokedParamTypeName.equals(declaredName))
               {
                  return false;
               }
            }

            // Name and params all match
            if (log.isTraceEnabled())
            {
               log.trace("Marked as @Asynchronous: " + invokedMethod);
            }
            return true;
         }
      }

      // Not async
      if (log.isTraceEnabled())
      {
         log.trace("Not @Asynchronous: " + invokedMethod);
      }
      return false;

   }
}
