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
package org.jboss.portal.core.metadata.portlet;

import java.util.ArrayList;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10269 $
 */
public class NamedMetaElementMetaData extends ElementMetaData
{

   /** NAME. */
   private String nameAttribute;

   /** CDATA. */
   private String contentAttribute;

   public NamedMetaElementMetaData(String nameAttribute, String contentAttribute)
   {
      if (nameAttribute == null)
      {
         throw new IllegalArgumentException("'name' attribute of meta element must NOT be null. http-equiv meta " +
            "elements are not currently supported.");
      }
      if (contentAttribute == null)
      {
         throw new IllegalArgumentException("Content attribute required for meta element: " + nameAttribute);
      }

      //
      this.nameAttribute = nameAttribute;
      this.contentAttribute = contentAttribute;
   }

   public String getNameAttribute()
   {
      return nameAttribute;
   }

   public String getContentAttribute()
   {
      return contentAttribute;
   }

   public MarkupElement buildElement()
   {
      ArrayList attributes = new ArrayList(3);
      if (nameAttribute != null && nameAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("name", nameAttribute, MarkupAttribute.Type.NAME));
      }
      if (contentAttribute != null && contentAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("content", contentAttribute, MarkupAttribute.Type.CDATA));
      }
      return new MarkupElement("meta", null, false, (MarkupAttribute[])attributes.toArray(new MarkupAttribute[attributes.size()]));
   }
}
