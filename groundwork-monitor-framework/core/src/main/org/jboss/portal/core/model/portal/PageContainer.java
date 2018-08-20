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
 * An interface which defines a page container.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12053 $
 */
public interface PageContainer extends PortalObject
{
   /**
    * Return an existing page or null if the child does not exist or does not have the right type.
    *
    * @param name the name of the child page to be retrieved
    * @return the specified page
    * @throws IllegalArgumentException if the name argument is null
    */
   Page getPage(String name) throws IllegalArgumentException;

   /**
    * Create a new page in the scope of this container.
    *
    * @param name the name of the child page to create
    * @return the create page
    * @throws DuplicatePortalObjectException if an object with the specified name already exist
    * @throws IllegalArgumentException       if the name argument is null
    */
   Page createPage(String name) throws DuplicatePortalObjectException, IllegalArgumentException;
}
