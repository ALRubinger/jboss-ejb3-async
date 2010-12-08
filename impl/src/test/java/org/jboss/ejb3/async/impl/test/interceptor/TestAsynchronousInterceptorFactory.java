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
package org.jboss.ejb3.async.impl.test.interceptor;

import gnu.trove.TLongObjectHashMap;

import java.lang.reflect.Method;

import javax.ejb.Asynchronous;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.async.impl.interceptor.AsynchronousClientInterceptor;
import org.jboss.ejb3.async.impl.interceptor.CreatePerClassAspectFactory;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AsyncMethodMetaData;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;

/**
 * Factory to create instances of the {@link AsynchronousClientInterceptor}
 * based on a testing environment
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class TestAsynchronousInterceptorFactory extends CreatePerClassAspectFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TestAsynchronousInterceptorFactory.class);

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#createPerClass(org.jboss.aop.Advisor)
    */
   @Override
   public Object createPerClass(final Advisor advisor)
   {
      // Create the interceptor instance
      final AsyncMethodsMetaData asyncMethods = new AsyncMethodsMetaData();
      final ManagedObjectAdvisor<?, ?> managedAdvisor = (ManagedObjectAdvisor<?, ?>) advisor;
      final TLongObjectHashMap methods = managedAdvisor.getAdvisedMethods();
      final long[] keys = methods.keys();
      for (final long key : keys)
      {
         final Method method = (Method) methods.get(key);
         if (method.isAnnotationPresent(Asynchronous.class))
         {
            final AsyncMethodMetaData asyncMethod = new AsyncMethodMetaData();
            asyncMethod.setMethodName(method.getName());
            final MethodParametersMetaData paramsMd = new MethodParametersMetaData();
            for (final Class<?> paramType : method.getParameterTypes())
            {
               paramsMd.add(paramType.getName());
            }
            asyncMethod.setMethodParams(paramsMd);
            asyncMethods.add(asyncMethod);
         }
      }
      final Object interceptor = new AsynchronousClientInterceptor(asyncMethods);
      log.info("Created: " + interceptor);
      return interceptor;
   }
}
