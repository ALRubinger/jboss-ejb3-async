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
package org.jboss.ejb3.async.impl.test.security.unit;

import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.async.impl.test.common.SecurityActions;
import org.jboss.ejb3.async.impl.test.common.TestConstants;
import org.jboss.ejb3.async.impl.test.common.ThreadPoolAsyncContainer;
import org.jboss.ejb3.async.impl.test.security.SecurityAwarePojo;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SecurityContextPropagationTestCase
 * 
 * Tests for SecurityContext propagation in
 * EJB 3.1 @Asynchronous invocations 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SecurityContextPropagationTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SecurityContextPropagationTestCase.class);

   private static final AOPDeployer aopDeployer = new AOPDeployer(TestConstants.AOP_DEPLOYABLE_FILENAME_SIMPLE);

   private static ThreadPoolAsyncContainer<SecurityAwarePojo> container;

   /*
    * Method names in Test POJO
    */

   private static final String METHOD_NAME_GET_SECURITY_CONTEXT = "getSecurityContext";

   // --------------------------------------------------------------------------------||
   // Test Lifecycle -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      aopDeployer.deploy();
      container = new ThreadPoolAsyncContainer<SecurityAwarePojo>("Test Security Aware POJO Container",
            TestConstants.DOMAIN_ASYNC, SecurityAwarePojo.class);
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      aopDeployer.undeploy();
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that security context propagates from 
    * the calling Thread to the invocation Thread
    * 
    * @throws Throwable 
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSecurityPropagation() throws Throwable
   {
      // Initialize
      final String username = "ALR";
      final String password = "Get 'er done";

      // Make a new bean instance upon which we'll invoke
      final BeanContext<SecurityAwarePojo> bean = container.construct();

      // Set a Security Context via the client
      final SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple(username, password);
      client.login();

      // Get the SecurityContext for this Thread
      final SecurityContext scBefore = SecurityActions.getSecurityContext();
      TestCase.assertNotNull("Test is invalid as security context before invocation is null", scBefore);

      // Use the container to get a contracted value from the bean
      final Future<SecurityContext> futureResult = (Future<SecurityContext>) container.invoke(bean,
            METHOD_NAME_GET_SECURITY_CONTEXT);

      // Get the Future value
      final SecurityContext scFromInvocation = futureResult.get();
      TestCase.assertNotNull("SecurtyContext from invocation did not propagate", scFromInvocation);
      TestCase.assertEquals("Security Context from invocation did not match that of the calling Thread", scBefore,
            scFromInvocation);
   }
}
