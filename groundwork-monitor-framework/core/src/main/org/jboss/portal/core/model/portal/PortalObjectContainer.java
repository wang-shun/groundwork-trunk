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
package org.jboss.portal.core.model.portal;

import org.jboss.portal.security.spi.provider.AuthorizationDomain;

/**
 * A container for portal object.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11795 $
 */
public interface PortalObjectContainer
{
   /**
    * Returns a portal object or null if it cannot be found.
    *
    * @param id the portal object id
    * @return the specified portal object
    * @throws IllegalArgumentException if the id is null
    */
   PortalObject getObject(PortalObjectId id) throws IllegalArgumentException;

   /**
    * Returns the portal object of the specified type identified with the specified identified or <code>null</code> if
    * it cannot be found.
    *
    * @param id           the portal object identifier
    * @param expectedType the expected type of the object to be retrieved
    * @param <T>          a class extending PortalObject
    * @return the PortalObject identified by the specified id or <code>null</code> if it cannot be found
    * @throws IllegalArgumentException if the specified id or the specified class is <code>null</code>
    * @since 2.7
    */
   <T extends PortalObject> T getObject(PortalObjectId id, Class<T> expectedType) throws IllegalArgumentException;

   /**
    * Returns the default root object.
    *
    * @return a root object
    */
   Context getContext();

   Context getContext(String namespace);

   Context createContext(String namespace) throws DuplicatePortalObjectException;

   /**
    * Get the authorization domain.
    *
    * @return the authorization domain
    */
   AuthorizationDomain getAuthorizationDomain();
}
