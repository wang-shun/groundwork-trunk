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
package org.jboss.portal.core.model.content;

import org.jboss.portal.common.i18n.LocalizedString;

import java.util.Iterator;
import java.util.Map;

/**
 * Defines the base interface for content.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11168 $
 */
public interface Content
{
   /**
    * Return a localized display name of the content.
    *
    * @return the content display name
    */
   LocalizedString getDisplayName();

   /**
    * Returns true if the content state can be changed.
    *
    * @return true if the content state can be changed
    */
   boolean isMutable();

   /**
    * Returns the content URI.
    *
    * @return the content URI
    */
   String getURI();

   /**
    * Updates the content URI.
    *
    * @param uri the new content URI value
    * @throws IllegalStateException if the content cannot be changed for some reason
    */
   void setURI(String uri) throws IllegalStateException;

   Iterator<String> getParameterNames();

   void setParameter(String name, String value) throws IllegalArgumentException;

   void setParameters(Map<String, String> parameters) throws IllegalArgumentException;

   String getParameter(String name) throws IllegalArgumentException;

   void clearParameters();
}
