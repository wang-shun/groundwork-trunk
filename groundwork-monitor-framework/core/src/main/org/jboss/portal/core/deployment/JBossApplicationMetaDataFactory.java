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
package org.jboss.portal.core.deployment;

import org.jboss.portal.core.metadata.ServiceMetaData;
import org.jboss.portal.core.metadata.portlet.AjaxMetaData;
import org.jboss.portal.core.metadata.portlet.ElementMetaData;
import org.jboss.portal.core.metadata.portlet.HeaderContentMetaData;
import org.jboss.portal.core.metadata.portlet.JBossApplicationMetaData;
import org.jboss.portal.core.metadata.portlet.JBossPortletMetaData;
import org.jboss.portal.core.metadata.portlet.PortletIconMetaData;
import org.jboss.portal.core.metadata.portlet.PortletInfoMetaData;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
public class JBossApplicationMetaDataFactory extends org.jboss.portal.portlet.deployment.jboss.JBossApplicationMetaDataFactory
{

   public Object newRoot(Object root, UnmarshallingContext nav, String nsURI, String localName, Attributes attrs)
   {
      if (root == null)
      {
         root = new JBossApplicationMetaData();
      }
      root = super.newRoot(root, nav, nsURI, localName, attrs);
      if (root instanceof JBossApplicationMetaData == false)
      {
         throw new IllegalArgumentException();
      }
      return root;
   }

   public Object newChild(Object object, UnmarshallingContext nav, String nsURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (object instanceof JBossApplicationMetaData)
      {
         if ("service".equals(localName))
         {
            child = new ServiceMetaData();
         }
      }
      else if (object instanceof JBossPortletMetaData)
      {
         if ("header-content".equals(localName))
         {
            child = new HeaderContentMetaData();
         }
         else if ("ajax".equals(localName))
         {
            child = new AjaxMetaData();
         }
         else if ("portlet-info".equals(localName))
         {
            child = new PortletInfoMetaData();
         }
      }
      else if (object instanceof PortletInfoMetaData)
      {
         if ("icon".equals(localName))
         {
            child = new PortletIconMetaData();
         }
      }
      else if (object instanceof HeaderContentMetaData)
      {
         if ("link".equalsIgnoreCase(localName))
         {
            String href = attrs.getValue("href");
            String type = attrs.getValue("type");
            String media = attrs.getValue("media");
            String rel = attrs.getValue("rel");
            String title = attrs.getValue("title");
            ElementMetaData elt = ElementMetaData.createLinkElement(type, rel, href, media, title);
            elt.init();
            child = elt;
         }
         else if ("script".equalsIgnoreCase(localName))
         {
            String src = attrs.getValue("src");
            String type = attrs.getValue("type");
            ElementMetaData elt = ElementMetaData.createScriptElement(type, src);
            elt.init();
            child = elt;
         }
         else if ("meta".equalsIgnoreCase(localName))
         {
            String name = attrs.getValue("name");
            String content = attrs.getValue("content");
            ElementMetaData elt = ElementMetaData.createNamedMetaElement(name, content);
            elt.init();
            child = elt;
         }
      }
      if (child == null)
      {
         child = super.newChild(object, nav, nsURI, localName, attrs);
      }
      return child;
   }

   public void addChild(Object parent, Object child, UnmarshallingContext nav, String nsURI, String localName)
   {
      if (child instanceof ServiceMetaData)
      {
         ServiceMetaData service = (ServiceMetaData)child;
         JBossApplicationMetaData app = (JBossApplicationMetaData)parent;
         app.getServices().put(service.getName(), service);
      }
      else if (child instanceof HeaderContentMetaData)
      {
         HeaderContentMetaData headerContent = (HeaderContentMetaData)child;
         JBossPortletMetaData portlet = (JBossPortletMetaData)parent;
         portlet.setHeaderContent(headerContent);
      }
      else if (child instanceof AjaxMetaData)
      {
         AjaxMetaData ajax = (AjaxMetaData)child;
         JBossPortletMetaData portlet = (JBossPortletMetaData)parent;
         portlet.setAjax(ajax);
      }
      else if (child instanceof PortletInfoMetaData)
      {
         PortletInfoMetaData portletInfo = (PortletInfoMetaData)child;
         JBossPortletMetaData portlet = (JBossPortletMetaData)parent;
         portlet.setPortletInfo(portletInfo);
      }
      else if (child instanceof PortletIconMetaData)
      {
         PortletIconMetaData portletIcon = (PortletIconMetaData)child;
         PortletInfoMetaData portletInfo = (PortletInfoMetaData)parent;
         portletInfo.setPortletIcon(portletIcon);
      }
      else if (child instanceof ElementMetaData)
      {
         ElementMetaData element = (ElementMetaData)child;
         HeaderContentMetaData headerContent = (HeaderContentMetaData)parent;
         headerContent.getElements().add(element);
      }
      else
      {
         super.addChild(parent, child, nav, nsURI, localName);
      }
   }

   public void setValue(Object object, UnmarshallingContext nav, String nsURI, String localName, String value)
   {
      if (object instanceof ServiceMetaData)
      {
         ServiceMetaData service = (ServiceMetaData)object;
         if ("service-name".equals(localName))
         {
            service.setName(value);
         }
         else if ("service-class".equals(localName))
         {
            service.setClassName(value);
         }
         else if ("service-ref".equals(localName))
         {
            service.setRef(value);
         }
      }
      else if (object instanceof ElementMetaData)
      {
         if ("script".equals(localName))
         {
            ElementMetaData elt = (ElementMetaData)object;
            elt.setBodyContent(value);
            elt.init();
         }
      }
      else if (object instanceof AjaxMetaData)
      {
         if ("partial-refresh".equals(localName))
         {
            AjaxMetaData ajax = (AjaxMetaData)object;
            ajax.setPartialRefresh(Boolean.valueOf(value));
         }
      }
      else if (object instanceof PortletIconMetaData)
      {
         if ("small-icon".equals(localName))
         {
            PortletIconMetaData portletIcon = (PortletIconMetaData)object;
            portletIcon.setIconLocation(value, PortletIconMetaData.SMALL);
         }
         else if ("large-icon".equals(localName))
         {
            PortletIconMetaData portletIcon = (PortletIconMetaData)object;
            portletIcon.setIconLocation(value, PortletIconMetaData.LARGE);
         }
      }
      else
      {
         super.setValue(object, nav, nsURI, localName, value);
      }
   }

   protected org.jboss.portal.portlet.deployment.jboss.metadata.JBossPortletMetaData createJBossPortlet()
   {
      return new JBossPortletMetaData();
   }
}
