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
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.PageContainer;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.metadata.coordination.CoordinationMetaData;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:theute@jboss.org">Thoams Heute</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 11348 $
 */
public class PageMetaData extends PortalObjectMetaData
{
   private static final Logger logger = Logger.getLogger(PageMetaData.class);


   private CoordinationMetaData coordinationMetaData;

   public PageMetaData()
   {
   }

   protected PortalObject newInstance(BuildContext buildContext, PortalObject parent) throws Exception
   {
      if (!(parent instanceof PageContainer))
      {
         throw new IllegalArgumentException("Cannot build page " + getName() + " because the parent it references is not a page container " + parent);
      }

      //
      return ((PageContainer)parent).createPage(getName());
   }

   public static PageMetaData buildPageMetaData(ContentProviderRegistry contentProviderRegistry, Element pageElt)
   {
      PageMetaData pageMD = new PageMetaData();

      //
      String pageName = XMLTools.asString(XMLTools.getUniqueChild(pageElt, "page-name", true));
      pageMD.setName(pageName);

      // Add the contained getWindows
      List windowElts = XMLTools.getChildren(pageElt, "window");
      for (int j = 0; j < windowElts.size(); j++)
      {
         Element windowElt = (Element)windowElts.get(j);
         try
         {
            WindowMetaData windowMD = (WindowMetaData)PortalObjectMetaData.buildMetaData(contentProviderRegistry, windowElt);
            pageMD.getChildren().put(windowMD.getName(), windowMD);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      // Log errors if any
      checkConstraints(pageMD);

      // Add the contained pages
      List pageElts = XMLTools.getChildren(pageElt, "page");
      for (int j = 0; j < pageElts.size(); j++)
      {
         Element childPageElt = (Element)pageElts.get(j);
         try
         {
            PageMetaData childPageMD = (PageMetaData)PortalObjectMetaData.buildMetaData(contentProviderRegistry, childPageElt);
            pageMD.getChildren().put(childPageMD.getName(), childPageMD);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      Element coordinationElt = XMLTools.getUniqueChild(pageElt, "coordination", false);
      if (coordinationElt != null)
      {
         pageMD.setCoordinationMetaData(CoordinationMetaData.buildMetaData(coordinationElt));
      }

      //
      return pageMD;
   }

   private void setCoordinationMetaData(CoordinationMetaData coordinationMetaData)
   {
      this.coordinationMetaData = coordinationMetaData;
   }

   public CoordinationMetaData getCoordinationMetaData()
   {
      return coordinationMetaData;
   }

   private static boolean checkConstraints(PageMetaData pageMD)
   {
      // Check that no more than 1 window has been defined as maximized
      Collection values = pageMD.getChildren().values();
      List maximizedWindows = new ArrayList();
      if (values != null)
      {
         Iterator it = values.iterator();
         while (it.hasNext())
         {
            PortalObjectMetaData poMetaData = (PortalObjectMetaData)it.next();
            if (poMetaData instanceof WindowMetaData)
            {
               WindowMetaData windowMetaData = (WindowMetaData)poMetaData;
               if (WindowState.MAXIMIZED.toString().equals(windowMetaData.getInitialWindowState()))
               {
                  maximizedWindows.add(windowMetaData);
                  if (maximizedWindows.size() > 1)
                  {
                     logger.debug("Set initial window state to NORMAL for window '" + windowMetaData + "'");
                     windowMetaData.setInitialWindowState(WindowState.NORMAL.toString());
                  }
               }
            }
         }
      }
      if (maximizedWindows.size() > 1)
      {
         StringBuffer windowsList = new StringBuffer();
         Iterator it = maximizedWindows.iterator();
         while (it.hasNext())
         {
            WindowMetaData windowMD = (WindowMetaData)it.next();
            windowsList.append(windowMD.toString() + "\n");
         }
         logger.error("More than one window is defined as maximized for page '" + pageMD.getName() + "'. The following windows" +
            " have been defined as maximized:\n" + windowsList);
         return false;
      }
      return true;
   }

   public String toString()
   {
      return "Page[" + getName() + "]";
   }
}
