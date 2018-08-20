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

/**
 * Contains nodes of type portal.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public interface PortalContainer extends PortalObject
{
   /**
    * Return an existing portal or null if such a child does not exist or does not have the right type.
    *
    * @param name the portal name
    * @return the specified portal
    * @throws IllegalArgumentException if the specified name is null
    */
   Portal getPortal(String name) throws IllegalArgumentException;

   /**
    * Create a new portal.
    *
    * @param name the portal name
    * @return the newly created portal
    * @throws DuplicatePortalObjectException if a child with the specified name already exists
    * @throws IllegalArgumentException       if the name argument is null
    */
   Portal createPortal(String name) throws DuplicatePortalObjectException, IllegalArgumentException;

   /**
    * Returns the default portal.
    *
    * @return the default portal of that container
    */
   Portal getDefaultPortal();

//   /**
//    * Returns an existing portal container or null if such a child does not exist or does not have the appropriate type.
//    *
//    * @param name the portal container name
//    * @return the specified portal container
//    * @throws IllegalArgumentException if the specified name is null
//    */
//   PortalContainer getPortalContainer(String name) throws IllegalArgumentException;
//
//   /**
//    * Creates a new portal container.
//    *
//    * @param name the portal container name
//    * @return the newly created portal container
//    * @throws DuplicatePortalObjectException if a child with the specified name already exists
//    * @throws IllegalArgumentException if the name argument is null
//    */
//   PortalContainer createPortalContainer(String name) throws DuplicatePortalObjectException, IllegalArgumentException;
}
