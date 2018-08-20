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
public class LinkElementMetaData extends ElementMetaData
{

   /** %ContentType required : CDATA -- media type, as per [RFC2045]. */
   private String typeAttribute;

   /** %LinkTypes : CDATA -- space-separated list of link types. */
   private String relAttribute;

   /** %URI : CDATA -- a Uniform Resource Identifier, see [URI]. */
   private String hrefAttribute;

   /** %MediaDesc : CDATA -- single or comma-separated list of media descriptors. */
   private String mediaAttribute;

   /** %Text : CDATA -- advisory title/amplification. */
   private String titleAttribute;

   public LinkElementMetaData(
      String relAttribute,
      String typeAttribute,
      String hrefAttribute,
      String mediaAttribute,
      String titleAttribute)
   {
      this.typeAttribute = typeAttribute;
      this.relAttribute = relAttribute;
      this.hrefAttribute = hrefAttribute;
      this.mediaAttribute = mediaAttribute;
      this.titleAttribute = titleAttribute;
   }

   public MarkupElement buildElement()
   {
      ArrayList attributes = new ArrayList(5);
      if (typeAttribute != null && typeAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("type", typeAttribute, MarkupAttribute.Type.CONTENT_TYPE));
      }
      if (relAttribute != null && relAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("rel", relAttribute, MarkupAttribute.Type.LINK_TYPES));
      }
      if (hrefAttribute != null && hrefAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("href", hrefAttribute, MarkupAttribute.Type.URI));
      }
      if (mediaAttribute != null && mediaAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("media", mediaAttribute, MarkupAttribute.Type.MEDIA_DESC));
      }
      if (titleAttribute != null && titleAttribute.length() > 0)
      {
         attributes.add(new MarkupAttribute("title", titleAttribute, MarkupAttribute.Type.TEXT));
      }
      return new MarkupElement("link", null, false, (MarkupAttribute[])attributes.toArray(new MarkupAttribute[attributes.size()]));
   }

   public String getTypeAttribute()
   {
      return typeAttribute;
   }

   public String getRelAttribute()
   {
      return relAttribute;
   }

   public String getHrefAttribute()
   {
      return hrefAttribute;
   }

   public String getMediaAttribute()
   {
      return mediaAttribute;
   }

   public String getTitleAttribute()
   {
      return titleAttribute;
   }
}
