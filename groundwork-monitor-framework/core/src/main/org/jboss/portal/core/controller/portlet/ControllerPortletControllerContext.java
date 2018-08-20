/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.jboss.portal.core.controller.portlet;

import org.apache.log4j.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.identity.User;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.impl.model.content.InternalContentProviderRegistry;
import org.jboss.portal.core.impl.model.content.portlet.PortletContent;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.navstate.NavigationalStateContext;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.controller.PortletControllerContext;
import org.jboss.portal.portlet.controller.event.EventControllerContext;
import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.EventInvocation;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.ResourceInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.spi.PortletInvocationContext;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 630 $
 */
public class ControllerPortletControllerContext implements PortletControllerContext
{
   private final static Logger log = Logger.getLogger(ControllerPortletControllerContext.class);

   /** . */
   private final ControllerContext controllerContext;

   /** A map of window name -> portlet info. */
   private final Map<String, PortletInfo> infos;

   /** A map of window name -> window. */
   private final Map<String, Window> windows;

   /** A map of window name -> portlet info. */
   private final Map<String, Instance> instances;

   /** . */
   private final ControllerStateControllerContext stateControllerContext;

   /** . */
   private final CoreEventControllerContext eventControllerContext;

   /** . */
   private final String pageId;

   /** . */
   private final Page page;

   public ControllerPortletControllerContext(ControllerContext controllerContext, Page page)
   {
      InstanceContainer instanceContainer = controllerContext.getController().getInstanceContainer();

      //
      Map<String, PortletInfo> infos = new HashMap<String, PortletInfo>();
      Map<String, Instance> instances = new HashMap<String, Instance>();
      Map<String, Window> windows = new HashMap<String, Window>();

      //
      for (PortalObject child : page.getChildren(PortalObject.WINDOW_MASK))
      {
         Window window = (Window)child;

         //
         Content content = window.getContent();

         //
         String instanceId;
         if (content instanceof PortletContent)
         {
            PortletContent portletContent = (PortletContent)content;
            instanceId = portletContent.getInstanceRef();
         }
         else
         {
            InternalContentProviderRegistry registry = controllerContext.getController().getContentProviderRegistry();
            ContentType contentType = window.getContentType();
            ContentProvider provider = registry.getContentProvider(contentType);
            if (provider != null)
            {
               instanceId = provider.getPortletInfo().getPortletName(Mode.VIEW);
            }
            else
            {
               log.debug("Couldn't find a ContentProvider for content type '" + contentType + "'");
               instanceId = null;
            }
         }

         if (instanceId != null)
         {
            Instance instance = instanceContainer.getDefinition(instanceId);
            if (instance != null)
            {
               try
               {
                  Portlet portlet = instance.getPortlet();
                  infos.put(window.getName(), portlet.getInfo());
                  instances.put(window.getName(), instance);
                  windows.put(window.getName(), window);
               }
               catch (PortletInvokerException ignore)
               {
                  log.debug("Couldn't get portlet from instance '" + instance + "'", ignore);
               }
            }
         }
         else
         {
            log.debug("Couldn't resolve instance id for window '" + window.getName() + "'");
         }
      }

      // State controller context
      NavigationalStateContext nsContext = (NavigationalStateContext)controllerContext.getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE);

      //
      this.stateControllerContext = new ControllerStateControllerContext(nsContext, this);
      this.eventControllerContext = new CoreEventControllerContext(this);
      this.controllerContext = controllerContext;
      this.windows = windows;
      this.infos = infos;
      this.instances = instances;
      this.pageId = page.getId().toString();
      this.page = page;
   }

   public Page getPage()
   {
      return page;
   }

   public String getPageId()
   {
      return pageId;
   }

   public Window getWindow(String windowName)
   {
      return windows.get(windowName);
   }

   public Set<String> getWindowNames()
   {
      return windows.keySet();
   }

   public PortletInfo getPortletInfo(String windowName)
   {
      return infos.get(windowName);
   }

   public PortletInvocationContext createPortletInvocationContext(String s, PortletPageNavigationalState pageNavigationalState)
   {
      Window window = windows.get(s);

      //
      return PortletInvocationFactory.createInvocationContext(controllerContext, window, pageNavigationalState);
   }

   public PortletInvocationResponse invoke(ActionInvocation actionInvocation) throws PortletInvokerException
   {
      return internalInvoke(actionInvocation);
   }

   /** todo : handle cookies redistribution */
   public PortletInvocationResponse invoke(List<Cookie> cookies, EventInvocation eventInvocation) throws PortletInvokerException
   {
      return internalInvoke(eventInvocation);
   }

   /** todo : handle cookies redistribution */
   public PortletInvocationResponse invoke(List<Cookie> cookies, RenderInvocation renderInvocation) throws PortletInvokerException
   {
      return internalInvoke(renderInvocation);
   }

   public PortletInvocationResponse invoke(ResourceInvocation resourceInvocation) throws PortletInvokerException
   {
      return internalInvoke(resourceInvocation);
   }

   public EventControllerContext getEventControllerContext()
   {
      return eventControllerContext;
   }

   public ControllerStateControllerContext getStateControllerContext()
   {
      return stateControllerContext;
   }

   private PortletInvocationResponse internalInvoke(PortletInvocation actionInvocation) throws PortletInvokerException
   {
      PortletInvocationFactory.contextualize(actionInvocation);

      //
      Window window = PortletInvocationFactory.getTargetWindow(actionInvocation);

      //
      User user = controllerContext.getUser();

      //
      CustomizationManager customizationManager = controllerContext.getController().getCustomizationManager();

      //
      Instance instance = customizationManager.getInstance(window, user);

      //
      return instance.invoke(actionInvocation);
   }

   public ControllerContext getControllerContext()
   {
      return controllerContext;
   }
}
