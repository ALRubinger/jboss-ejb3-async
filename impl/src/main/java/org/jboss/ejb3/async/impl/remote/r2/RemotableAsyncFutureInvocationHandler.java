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
package org.jboss.ejb3.async.impl.remote.r2;

import java.net.MalformedURLException;

import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.async.spi.container.remote.EndpointConstants;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.transport.Connector;

/**
 * RemotableAsyncFutureInvocationHandler
 * 
 * Remotable proxy handler for a Future result of 
 * an Asynchronous invocation
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RemotableAsyncFutureInvocationHandler extends PojiProxy
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(RemotableAsyncFutureInvocationHandler.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param uri The InvokerLocator to the target endpoint 
    */
   public RemotableAsyncFutureInvocationHandler()
   {
      super(EndpointConstants.ASYNCHRONOUS_REMOTING_ENDPOINT_NAME,
            getClientBinding(EndpointConstants.ASYNCHRONOUS_REMOTING_CONNECTOR_MC_BIND_NAME), null);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the InvokerLocator for the specified 
    * invokerName (supplied as the Object Store bind name in
    * MC)
    * 
    * @param invokerName
    * @return
    * @throws NotBoundException If the specified invokerName is not bound in MC
    */
   private static InvokerLocator getClientBinding(String invokerName) throws NotBoundException
   {
      // Initialize
      String url = null;
      Connector connector = null;
      InvokerLocator locator = null;

      // Lookup the Connector in MC
      try
      {
         connector = Ejb3RegistrarLocator.locateRegistrar().lookup(invokerName, Connector.class);
      }
      catch (NotBoundException nbe)
      {
         // Log and rethrow
         log.warn("Could not find the remoting connector for the specified invoker name, " + invokerName + " in MC");
         throw nbe;
      }

      // Use the binding specified by the Connector
      try
      {
         url = connector.getInvokerLocator();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not obtain " + InvokerLocator.class.getSimpleName()
               + " from EJB3 Remoting Connector", e);
      }

      // Construct Locator
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }

      // Return 
      return locator;
   }

}
