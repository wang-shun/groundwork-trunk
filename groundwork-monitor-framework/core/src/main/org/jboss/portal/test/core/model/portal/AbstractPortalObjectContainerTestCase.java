/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portal.test.core.model.portal;

import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.test.core.PortalBaseTestCase;

/**
 * Portal Object Container Test Cases based on the microcontainer architecture
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:anil.saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 8786 $
 */
public class AbstractPortalObjectContainerTestCase extends PortalBaseTestCase
{

   /** . */
   protected PortalObjectContainer container;

   /** . */
   protected PortalAuthorizationManagerFactory authorizationManagerFactory;

   protected String getConfigLocationPrefix()
   {
      return "org/jboss/portal/test/core/model/portal/";
   }

   public PortalObjectContainer getPortalObjectContainer()
   {
      return container;
   }

   public void setPortalObjectContainer(PortalObjectContainer container)
   {
      this.container = container;
   }

   public PortalAuthorizationManagerFactory getAuthorizationManagerFactory()
   {
      return authorizationManagerFactory;
   }

   public void setAuthorizationManagerFactory(PortalAuthorizationManagerFactory authorizationManagerFactory)
   {
      this.authorizationManagerFactory = authorizationManagerFactory;
   }

   public PortalAuthorizationManager getAuthorizationManager()
   {
      return authorizationManagerFactory.getManager();
   }

   public void setUp() throws Exception
   {
      super.setUp();

      // Create root context
      TransactionAssert.beginTransaction();
      container.createContext("");
      TransactionAssert.commitTransaction();
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
   }
}
