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
package org.jboss.ejb3.async.spi.container.remote;

/**
 * EndpointConstants
 * 
 * Defines contracted constants for Asynchronous Remoting Endpoints
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface EndpointConstants
{

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Name under which the server-side remoting endpoint for asynchronous 
    * invocations is registered
    */
   String ASYNCHRONOUS_REMOTING_ENDPOINT_NAME = "org.jboss.ejb3.async.remoting.AsynchronousRemotingEndpoint";

   /**
    * The name under which the Remoting Connector is bound in MC
    */
   String ASYNCHRONOUS_REMOTING_CONNECTOR_MC_BIND_NAME = "org.jboss.ejb3.async.remoting.ConnectorMcBindName";
}
