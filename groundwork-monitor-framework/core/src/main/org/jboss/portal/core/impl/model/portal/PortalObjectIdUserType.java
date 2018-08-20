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
package org.jboss.portal.core.impl.model.portal;

import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.jems.hibernate.StringWrapperUserType;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11554 $
 */
public class PortalObjectIdUserType extends StringWrapperUserType
{

   /** . */
   private static final PortalObjectId EMPTY = new PortalObjectId("", PortalObjectPath.ROOT_PATH);

   public Object fromNull()
   {
      return EMPTY;
   }

   public Object fromString(String name)
   {
      return PortalObjectId.parse(name.trim(), PortalObjectPath.LEGACY_FORMAT);
   }

   public String toString(Object value)
   {
      return ((PortalObjectId)value).toString(PortalObjectPath.LEGACY_FORMAT).trim();
   }

   public Class returnedClass()
   {
      return PortalObjectId.class;
   }
}
