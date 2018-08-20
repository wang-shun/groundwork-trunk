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

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10269 $
 */
public abstract class ElementMetaData
{

   /** . */
   protected String bodyContent;

   /** . */
   protected MarkupElement element;

   protected ElementMetaData()
   {
   }

   public void init()
   {
      element = buildElement();
   }

   protected abstract MarkupElement buildElement();

   public MarkupElement getElement()
   {
      return element;
   }

   public String getBodyContent()
   {
      return bodyContent;
   }

   public void setBodyContent(String bodyContent)
   {
      this.bodyContent = bodyContent;
   }

   /**
    * Create a meta header element. <p>This element will create a meta tag.</p>
    *
    * @param name    name attribute of the meta element
    * @param content content attribute of the meta element
    * @return a new meta header element
    */
   public static ElementMetaData createNamedMetaElement(String name, String content)
   {
      return new NamedMetaElementMetaData(name, content);
   }

   /**
    * Create a link header element. <p>This element will create a link tag.</p>
    *
    * @param type  the type attribute of the link
    * @param rel   the rel attribute of the link
    * @param href  the href attribute of the link
    * @param media the media attribute of the link
    * @return a new link header element
    */
   public static ElementMetaData createLinkElement(String type, String rel, String href, String media, String title)
   {
      return new LinkElementMetaData(rel, type, href, media, title);
   }

   /**
    * Create a script header element. <p>This element will create a script tag.</p>
    *
    * @param type the type attribute of this script
    * @param src  the src attribute of this script
    * @return a new script header element
    */
   public static ElementMetaData createScriptElement(String type, String src)
   {
      return new ScriptElementMetaData(type, src);
   }
}
