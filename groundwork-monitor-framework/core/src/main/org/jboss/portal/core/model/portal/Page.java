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

import org.jboss.portal.core.model.content.ContentType;

/**
 * A page contains window and is also a page container which can contain nested pages.
 *
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @version $Revision: 8786 $
 */
public interface Page extends PageContainer
{
   /**
    * Return the portal containing this page.
    *
    * @return the portal for this page
    */
   Portal getPortal();

   /**
    * Return a page window or null if the portal object does not exist or does not have the appropriate type.
    *
    * @param name the window name
    * @return the specified page window
    * @throws IllegalArgumentException if the name is null
    */
   Window getWindow(String name) throws IllegalArgumentException;

   /**
    * Create a new window.
    *
    * @param name        window name
    * @param contentType the window content type
    * @param contentURI  the window content URI
    * @return the created window
    * @throws DuplicatePortalObjectException if a portal object with the specified name already exist
    * @throws IllegalArgumentException       if the name is null
    */
   Window createWindow(String name, ContentType contentType, String contentURI) throws DuplicatePortalObjectException, IllegalArgumentException;
}
