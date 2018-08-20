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
package org.jboss.portal.core.ui.content.portlet;

import org.apache.log4j.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.ModeInfo;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.spi.ClientContext;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import javax.xml.namespace.QName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12562 $
 */
public class PortletContentEditorPortlet extends GenericPortlet
{

	private Logger log = Logger.getLogger(getClass());
	
   public static final String DEFAULT_PORTLET_ICON = "/portal-core/images/portletIcon_Default1.gif";

   /** . */
   private final PortletMode EDIT_CONTENT = new PortletMode("edit_content");

   /** . */
   private final Mode INTERNAL_EDIT_CONTENT = Mode.create("edit_content");

   /** . */
   private InstanceContainer instanceContainer;
   private static final QName CONTENT_SELECT = new QName("urn:jboss:portal:content", "select");
   private static final String CONTENT_URI = "content.uri";

   public void init() throws PortletException
   {
      instanceContainer = (InstanceContainer)getPortletContext().getAttribute("InstanceContainer");
   }

   public void processAction(ActionRequest req, ActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      if ((req.getPortletMode().equals(EDIT_CONTENT)))
      {
         String uri = req.getParameter(CONTENT_URI);
         if (uri != null)
         {
            resp.setRenderParameter(CONTENT_URI, uri);
            resp.setEvent(CONTENT_SELECT, uri);
         }
      }
   }

   protected void doDispatch(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      if (EDIT_CONTENT.equals(req.getPortletMode()))
      {
         doEditContent(req, resp);
      }
      else
      {
         super.doDispatch(req, resp);
      }
   }

   
   protected void doView(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      writer.write("This portlet is not made to be viewed");
      writer.close();
   }

   protected void doEditContent(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      getContent(req, resp, false);
   }

   protected void getContent(RenderRequest req, RenderResponse resp, boolean newContent) throws PortletException, PortletSecurityException, IOException
   {	   
      String selectedURI = req.getParameter(CONTENT_URI);
      if (selectedURI == null)
      {
         // Get the uri value optionally provided by the portal
         selectedURI = req.getParameter("uri");
      }
      
      //
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();

      // load instances this user has access to.

      // HACK: If this is being used in the Dashboard Configurator, filter the 
      // available portlet list to portlets that are appropriate for the 
      // "My Groundwork" pages (defined in a properties file).
      // TODO: Move this code outside of JBoss Portal code.
	  RenderInvocation invocation = (RenderInvocation) 
	  	req.getAttribute("org.jboss.portal.attribute.component_invocation");
	  HttpServletRequest request = invocation.getDispatchedRequest();
      HashSet<String> allowedPortlets = null;
      
      if (request != null && request.getRequestURI().
    		  contains("dashboard")) {
    	  allowedPortlets = new HashSet<String>();
    	  File myGroundworkPropsFile = 
    		  new File("/usr/local/groundwork/config/mygroundwork.xml");
    	  if (!myGroundworkPropsFile.canRead()) {
    		  log.error("File does not exist or is not readable: " + 
    				  myGroundworkPropsFile + 
    				  " -- cannot filter My Groundwork portlet names");
    	  } else {
        	  Properties myGroundworkProperties = new Properties();
        	  try {
        		  myGroundworkProperties.loadFromXML(
        				  new FileInputStream(myGroundworkPropsFile));
        	  } catch (Exception exception) {
        		  log.error("Error reading mygroundwork.xml: cannot filter " +
        				  "My Groundwork portlet names",
        				  exception);
        	  }
        	  
        	  if (myGroundworkProperties.containsKey("allowedPortlets")) {
        		  StringTokenizer portletNames = new StringTokenizer(
        				  myGroundworkProperties.getProperty("allowedPortlets"));
        		  while (portletNames.hasMoreTokens()) {
        			  allowedPortlets.add(portletNames.nextToken());
        		  }
        	  }
    	  }

      }
      
      // Remove content editors
      List<Instance> available_instances = new ArrayList<Instance>();
      nextInstance:
      for (Instance instance : instanceContainer.getDefinitions())
      {
    	  if (allowedPortlets != null && 
    			  !allowedPortlets.contains(instance.getId())) {
    		  log.debug("Skipping instance: " + instance.getId());
    		  continue nextInstance;
    	  }
    	  
         //
         try
         {
            // Filter portlets that are editors 
            Portlet portlet = instance.getPortlet();
            PortletInfo info = portlet.getInfo();
            for (ModeInfo modeInfo : info.getCapabilities().getAllModes())
            {
               if (modeInfo.getMode().equals(INTERNAL_EDIT_CONTENT))
               {
                  continue nextInstance;
               }
            }

            //
            available_instances.add(instance);
         }
         catch (PortletInvokerException ignore)
         {
         }
      }

      // Sort alphabetically
      Comparator<Instance> simpleComparator = new Comparator<Instance>()
      {
         public int compare(Instance i1, Instance i2)
         {
            return i1.getId().compareToIgnoreCase(i2.getId());
         }
      };
      Collections.sort(available_instances, simpleComparator);

      //
      Instance selectedInstance = null;
      for (Instance instance : available_instances)
      {
         if (instance.getId().equals(selectedURI))
         {
            selectedInstance = instance;
         }
      }

      //
      req.setAttribute("INSTANCES", available_instances);
      req.setAttribute("SELECTED_INSTANCE", selectedInstance);
      req.setAttribute("NEW_CONTENT", newContent);

      //
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/content/portlet_editor.jsp");
      dispatcher.include(req, resp);

      //
      writer.close();
   }


}
