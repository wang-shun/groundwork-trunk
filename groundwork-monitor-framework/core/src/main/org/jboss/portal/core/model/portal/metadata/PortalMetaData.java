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

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalContainer;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.metadata.coordination.CoordinationMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.PortletModeMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.SupportsMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.WindowStateMetaData;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 11348 $
 */
public class PortalMetaData extends PortalObjectMetaData
{

   private SupportsMetaData supportsMetaData;
   private CoordinationMetaData coordinationMetaData;

   public PortalMetaData()
   {
      supportsMetaData = new SupportsMetaData();
      PortletModeMetaData mode = new PortletModeMetaData();
      mode.setPortletMode(Mode.EDIT);
      supportsMetaData.addPortletMode(mode);
      mode = new PortletModeMetaData();
      mode.setPortletMode(Mode.VIEW);
      supportsMetaData.addPortletMode(mode);
      mode = new PortletModeMetaData();
      mode.setPortletMode(Mode.HELP);
      supportsMetaData.addPortletMode(mode);
      mode = new PortletModeMetaData();
      mode.setPortletMode(Mode.ADMIN);
      supportsMetaData.addPortletMode(mode);
      mode = new PortletModeMetaData();
      mode.setPortletMode(Mode.EDIT_DEFAULTS);
      supportsMetaData.addPortletMode(mode);
      
      
      WindowStateMetaData windowState = new WindowStateMetaData();
      windowState.setWindowState(WindowState.MAXIMIZED);
      supportsMetaData.addWindowState(windowState);
      windowState = new WindowStateMetaData();
      windowState.setWindowState(WindowState.MINIMIZED);
      supportsMetaData.addWindowState(windowState);
      windowState = new WindowStateMetaData();
      windowState.setWindowState(WindowState.NORMAL);
      supportsMetaData.addWindowState(windowState);
   }

   public SupportsMetaData getSupportsMetaData()
   {
      return supportsMetaData;
   }
   
   /*
   public ModesMetaData getModes()
   {
      return modes;
   }

   public void setModes(ModesMetaData modes)
   {
      this.modes = modes;
   }

   public WindowStatesMetaData getWindowStates()
   {
      return windowStates;
   }

   public void setWindowStates(WindowStatesMetaData windowStates)
   {
      this.windowStates = windowStates;
   }
*/

   protected PortalObject newInstance(BuildContext buildContext, PortalObject parent) throws Exception
   {
      if (!(parent instanceof PortalContainer))
      {
         throw new IllegalArgumentException("Not a context");
      }

      //
      Portal portal = ((PortalContainer)parent).createPortal(getName());

      //
      for (Iterator i = supportsMetaData.getPortletModes().iterator(); i.hasNext();)
      {
         Mode mode = ((PortletModeMetaData)i.next()).getPortletMode();
         portal.getSupportedModes().add(mode);
      }

      //
      for (Iterator i = supportsMetaData.getWindowStates().iterator(); i.hasNext();)
      {
         WindowState windowState = ((WindowStateMetaData)i.next()).getWindowState();
         portal.getSupportedWindowStates().add(windowState);
      }

      //
      return portal;
   }

   /** Parse the following XML elements : portal-name, supported-modes, supported-window-states. */
   public static PortalMetaData buildPortalMetaData(ContentProviderRegistry contentProviderRegistry, Element portalElt) throws Exception
   {
      PortalMetaData portalMD = new PortalMetaData();

      //
      String portalName = XMLTools.asString(XMLTools.getUniqueChild(portalElt, "portal-name", true));
      if (portalName != null && portalName.length() > 0 && portalName.indexOf(".") < 0)
      {
         portalMD.setName(portalName);
      }
      else
      {
         throw new IllegalArgumentException("Invalid portal-name: '" + portalName
            + "'. Must not be null, empty or contain a '.'");
      }

      //
      Element supportedModesElt = XMLTools.getUniqueChild(portalElt, "supported-modes", false);
      if (supportedModesElt != null)
      {
         buildSupportedModes(portalMD, supportedModesElt);
      }

      //
      Element supportedWindowStatesElt = XMLTools.getUniqueChild(portalElt, "supported-window-states", false);
      if (supportedWindowStatesElt != null)
      {
         buildSupportedWindowStates(portalMD, supportedWindowStatesElt);
      }

      //
      List pageElts = XMLTools.getChildren(portalElt, "page");
      for (int i = 0; i < pageElts.size(); i++)
      {
         Element pageElt = (Element)pageElts.get(i);
         PageMetaData pageMD = (PageMetaData)PortalObjectMetaData.buildMetaData(contentProviderRegistry, pageElt);
         portalMD.getChildren().put(pageMD.getName(), pageMD);
      }

      Element coordinationElt = XMLTools.getUniqueChild(portalElt, "coordination", false);
      if (coordinationElt != null)
      {
         portalMD.setCoordinationMetaData(CoordinationMetaData.buildMetaData(coordinationElt));
      }

      //
      return portalMD;
   }

   private void setCoordinationMetaData(CoordinationMetaData coordinationMetaData)
   {
      this.coordinationMetaData = coordinationMetaData;
   }

   public CoordinationMetaData getCoordinationMetaData()
   {
      return coordinationMetaData;
   }

   public static void buildSupportedModes(PortalMetaData portalMD, Element supportedModesElt)
   {
      List modeElts = XMLTools.getChildren(supportedModesElt, "mode");
      for (int i = 0; i < modeElts.size(); i++)
      {
         Element modeElt = (Element)modeElts.get(i);
         String modeAsString = XMLTools.asString(modeElt);
         PortletModeMetaData mode = new PortletModeMetaData();
         mode.setPortletMode(new Mode(modeAsString));
         portalMD.getSupportsMetaData().addPortletMode(mode);
      }
   }

   public static void buildSupportedWindowStates(PortalMetaData portalMD, Element supportedWindowStatesElt)
   {
      List windowStates = XMLTools.getChildren(supportedWindowStatesElt, "window-state");
      for (int i = 0; i < windowStates.size(); i++)
      {
         Element windowStateElt = (Element)windowStates.get(i);
         String windowStateAsString = XMLTools.asString(windowStateElt);
         WindowStateMetaData windowState = new WindowStateMetaData();
         windowState.setWindowState(new WindowState(windowStateAsString));
         portalMD.getSupportsMetaData().addWindowState(windowState);
      }
   }

   public String toString()
   {
      return "Portal[" + getName() + "]";
   }
}
