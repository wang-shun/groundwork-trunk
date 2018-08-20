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

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;

/**
 * Represents a window, i.e the view port to integrated content.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public interface Window extends PortalObject
{

   /**
    * Return the page containing this window.
    *
    * @return the page
    */
   Page getPage();

   /**
    * Return the window content type.
    *
    * @return the window content type
    */
   ContentType getContentType();

   /**
    * Returns the window content or null if no content can be provided. Content could not be provided if no content
    * handler has been found in the content handler registry.
    *
    * @return the window content
    */
   Content getContent();

   /**
    * Returns the inital window state (the window state to use when no navigational state exists, for example on a new
    * page request) for this particular window
    *
    * @return a windowState
    */
   WindowState getInitialWindowState();

   /**
    * Returns the inital mode to use (the mode to use when no navigational state exists, for example on a new page
    * request) for this particular window
    *
    * @return a portlet mode
    */
   Mode getInitialMode();
}
