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
package org.jboss.portal.core.model.portal.metadata;

import org.jboss.logging.Logger;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.impl.model.portal.WindowImpl;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.theme.ThemeConstants;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class WindowMetaData extends PortalObjectMetaData
{

   private static final Logger logger = Logger.getLogger(WindowMetaData.class);

   /** The window region. */
   protected String region;

   /** The window order. */
   protected int order;

   /** The initial window state. */
   protected String initialWindowState;

   /** The initial window state. */
   protected String initialMode;

   /** The window content. */
   protected ContentMetaData content;

   /** . */
   protected ContentType contentType;

   public String getRegion()
   {
      return region;
   }

   public void setRegion(String region)
   {
      this.region = region;
   }

   public int getOrder()
   {
      return order;
   }

   public void setOrder(int order)
   {
      this.order = order;
   }

   public ContentMetaData getContent()
   {
      return content;
   }

   public void setContent(ContentMetaData content)
   {
      this.content = content;
   }

   public ContentType getContentType()
   {
      return contentType;
   }

   public void setContentType(ContentType contentType)
   {
      this.contentType = contentType;
   }

   public String getInitialWindowState()
   {
      return initialWindowState;
   }

   public void setInitialWindowState(String initialWindowState)
   {
      this.initialWindowState = initialWindowState;
   }

   public String getInitialMode()
   {
      return initialMode;
   }

   public void setInitialMode(String initialMode)
   {
      this.initialMode = initialMode;
   }

   protected PortalObject newInstance(BuildContext buildContext, PortalObject parent) throws Exception
   {
      if (!(parent instanceof Page))
      {
         throw new IllegalArgumentException("Not a page");
      }

      //
      Window window = ((Page)parent).createWindow(getName(), content.getContentType(), content.getURI());

      //
      window.setDeclaredProperty(ThemeConstants.PORTAL_PROP_REGION, region);
      window.setDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER, "" + order);
      if (initialWindowState != null)
      {
         window.setDeclaredProperty(WindowImpl.PORTAL_INITIAL_WINDOW_STATE, "" + initialWindowState);
      }
      if (initialMode != null)
      {
         window.setDeclaredProperty(WindowImpl.PORTAL_INITIAL_MODE, "" + initialMode);
      }

      //
      return window;
   }

   public static WindowMetaData buildPortletWindowMetaData(ContentProviderRegistry contentProviderRegistry, Element windowElt) throws Exception
   {
      WindowMetaData windowMD = new WindowMetaData();

      //
      String windowName = XMLTools.asString(XMLTools.getUniqueChild(windowElt, "window-name", true));
      windowMD.setName(windowName);

      // Get coordinates
      String region = XMLTools.asString(XMLTools.getUniqueChild(windowElt, "region", true));
      windowMD.setRegion(region);

      // Get initial window state
      Element element = XMLTools.getUniqueChild(windowElt, "initial-window-state", false);
      if (element != null)
      {
         String initialWindowState = XMLTools.asString(element);
         if (initialWindowState.toLowerCase().equals(WindowState.MAXIMIZED.toString())
            || (initialWindowState.toLowerCase().equals(WindowState.MINIMIZED.toString()))
            || (initialWindowState.toLowerCase().equals(WindowState.NORMAL.toString())))
         {
            windowMD.setInitialWindowState(initialWindowState);
         }
         else
         {
            logger.error("initial-window-state for '" + windowName + "' must be one of 'MAXIMIZED', 'MINIMIZED' or 'NORMAL'");
         }
      }

      // Get initial mode
      element = XMLTools.getUniqueChild(windowElt, "initial-mode", false);
      if (element != null)
      {
         String initialMode = XMLTools.asString(element);
         windowMD.setInitialMode(initialMode);
      }

      //
      int height = Integer.parseInt(XMLTools.asString(XMLTools.getUniqueChild(windowElt, "height", true)));
      windowMD.setOrder(height);

      //
      ContentType contentType;
      String contentURI;
      Element instanceRefElt = XMLTools.getUniqueChild(windowElt, "instance-ref", false);
      if (instanceRefElt != null)
      {
         contentType = ContentType.PORTLET;
         contentURI = XMLTools.asString(instanceRefElt);
      }
      else
      {
         Element contentElt = XMLTools.getUniqueChild(windowElt, "content", true);
         Element contentTypeElt = XMLTools.getUniqueChild(contentElt, "content-type", true);
         Element contentURIElt = XMLTools.getUniqueChild(contentElt, "content-uri", true);

         //
         contentType = ContentType.create(XMLTools.asString(contentTypeElt));
         contentURI = XMLTools.asString(contentURIElt);
      }

      // Build content meta data
      ContentMetaData contentMD = new ContentMetaData();
      contentMD.setContentType(contentType);
      contentMD.setURI(contentURI);

      //
      windowMD.setContent(contentMD);

      //
      return windowMD;
   }

   public String toString()
   {
      return "Window[" + getName() + "]";
   }
}
