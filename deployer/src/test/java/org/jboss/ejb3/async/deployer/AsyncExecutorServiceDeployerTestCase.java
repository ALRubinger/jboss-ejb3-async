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
package org.jboss.ejb3.async.deployer;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.async.spi.AttachmentNames;
import org.jboss.reloaded.shrinkwrap.api.ShrinkWrapDeployer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests to ensure the {@link AsyncExecutorServiceDeployer}
 * is working as expected
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@RunWith(Arquillian.class)
public class AsyncExecutorServiceDeployerTestCase
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(AsyncExecutorServiceDeployerTestCase.class.getName());

   /**
    * The deployment containing the deployer chain under test
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "async-deployer.jar").addPackage(
            AsyncExecutorServiceDeployer.class.getPackage())
            .addResource("META-INF/ejb3-async-deployer-jboss-beans.xml").addManifestResource(
                  "ejb3-async-mock-deployer-jboss-beans.xml").addClasses(MockEjb3MetaData.class,
                  MockEjb3MetaDataDeployer.class);
      log.info(archive.toString(true));
      return archive;
   }

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Deployer
    */
   @Inject
   private ShrinkWrapDeployer deployer;

   // ------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Ensures that the {@link AsyncExecutorServiceDeployer} attaches an {@link ExecutorService}
    * implementation to incoming EJB3 deployments under name {@link AttachmentNames#ASYNC_INVOCATION_PROCESSOR}
    */
   @Test
   public void attachesExecutorService() throws Exception
   {
      // Make a new fake deployment
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClass(AsyncExecutorServiceDeployer.class);

      // Deploy
      deployer.deploy(archive);
      try
      {

         // Grab the last DU as cached from the chain
         final DeploymentUnit du = CachingDeployer.lastDeployed;
         Assert.assertNotNull("Last deployment was not cached/processed", du);
         log.info("Got last deployment: " + du);

         // See that the ES was attached
         final ExecutorService es = (ExecutorService) du.getAttachment(AttachmentNames.ASYNC_INVOCATION_PROCESSOR);
         Assert.assertNotNull(ExecutorService.class.getSimpleName() + " was not attached to the incoming DU", es);
         log.info("Got: " + es);
      }
      finally
      {
         // Undeploy
         deployer.undeploy(archive);
      }
   }

}
