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
package org.jboss.portal.core.aspects.controller;

import org.apache.log4j.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.api.PortalURL;
import org.jboss.portal.core.aspects.controller.node.Navigation;
import org.jboss.portal.core.controller.Controller;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerRequestDispatcher;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.SignOutCommand;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceRenderCommand;
import org.jboss.portal.core.model.instance.command.render.RenderPortletInstanceCommand;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.core.model.portal.command.PageCommand;
import org.jboss.portal.core.model.portal.command.action.ImportPageToDashboardCommand;
import org.jboss.portal.core.model.portal.command.render.RenderPageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewContextCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPortalCommand;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.server.config.ServerConfig;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.jboss.portal.theme.page.Region;
import org.jboss.portal.theme.page.WindowContext;
import org.jboss.portal.theme.page.WindowResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.security.Principal;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision: 11064 $
 */
public class PageCustomizerInterceptor extends ControllerInterceptor
{

   /** . */
   private static Logger log = Logger.getLogger(PageCustomizerInterceptor.class);

   /** . */
   private static final PortalObjectId defaultPortalId = PortalObjectId.parse("/", PortalObjectPath.CANONICAL_FORMAT);

   /** . */
   private static PortalObjectId adminPortalId = PortalObjectId.parse("/admin", PortalObjectPath.CANONICAL_FORMAT);

   /** . */
   private String targetContextPath;

   /** . */
   private String headerPath;

   /** . */
   private String tabsPath;

   /** . */
   private String loginNamespace;

   /** . */
   private ServerConfig config;

   /** . */
   private PortalAuthorizationManagerFactory portalAuthorizationManagerFactory;

   /** . */
   private PortalObjectContainer portalObjectContainer;

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return portalAuthorizationManagerFactory;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.portalAuthorizationManagerFactory = portalAuthorizationManagerFactory;
   }

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public ControllerResponse invoke(ControllerCommand cmd) throws Exception
   {
      ControllerResponse resp = (ControllerResponse)cmd.invokeNext();

      // Insert navigation portlet in the page
      if (resp instanceof PageRendition)
      {
         PageRendition rendition = (PageRendition)resp;

         //
         if (cmd instanceof PageCommand)
         {
            PageCommand rpc = (PageCommand)cmd;

            //
            String tabbedNav = injectTabbedNav(rpc);
            if (tabbedNav != null)
            {
               Map windowProps = new HashMap();
               windowProps.put(ThemeConstants.PORTAL_PROP_WINDOW_RENDERER, "emptyRenderer");
               windowProps.put(ThemeConstants.PORTAL_PROP_DECORATION_RENDERER, "emptyRenderer");
               windowProps.put(ThemeConstants.PORTAL_PROP_PORTLET_RENDERER, "emptyRenderer");
               WindowResult res = new WindowResult("", tabbedNav, Collections.EMPTY_MAP, windowProps, null, WindowState.NORMAL, Mode.VIEW);
               WindowContext blah = new WindowContext("BLAH", "navigation", "0", res);
               rendition.getPageResult().addWindowContext(blah);

               //
               Region region = rendition.getPageResult().getRegion2("navigation");
               DynaRenderOptions.NO_AJAX.setOptions(region.getProperties());
            }
         }

         //
         String dashboardNav = injectDashboardNav(cmd);
         if (dashboardNav != null)
         {
            Map windowProps = new HashMap();
            windowProps.put(ThemeConstants.PORTAL_PROP_WINDOW_RENDERER, "emptyRenderer");
            windowProps.put(ThemeConstants.PORTAL_PROP_DECORATION_RENDERER, "emptyRenderer");
            windowProps.put(ThemeConstants.PORTAL_PROP_PORTLET_RENDERER, "emptyRenderer");
            WindowResult res = new WindowResult("", dashboardNav, Collections.EMPTY_MAP, windowProps, null, WindowState.NORMAL, Mode.VIEW);
            WindowContext bluh = new WindowContext("BLUH", "dashboardnav", "0", res);
            rendition.getPageResult().addWindowContext(bluh);

            //
            Region region = rendition.getPageResult().getRegion2("dashboardnav");
            DynaRenderOptions.NO_AJAX.setOptions(region.getProperties());
         }
      }

      //
      return resp;
   }

   public String injectDashboardNav(ControllerCommand cc)
   {
      ControllerContext controllerCtx = cc.getControllerContext();
      ControllerRequestDispatcher rd = controllerCtx.getRequestDispatcher(targetContextPath, headerPath);


      //
      if (rd != null)
      {
         // Get user
         Controller controller = controllerCtx.getController();
         User user = controllerCtx.getUser();
         rd.setAttribute("org.jboss.portal.header.USER", user);

         Principal principal = controllerCtx.getServerInvocation().getServerContext().getClientRequest().getUserPrincipal();
         rd.setAttribute("org.jboss.portal.header.PRINCIPAL", principal);

         if (principal == null)
         {
            PortalURL portalURL;

            String configNamespace = config.getProperty("core.login.namespace");
            if (loginNamespace == null)
            {
               loginNamespace = configNamespace;
            }

            if (loginNamespace != null && !loginNamespace.toLowerCase().trim().equals("default"))
            {
               ViewContextCommand vcc = new ViewContextCommand(new PortalObjectId(loginNamespace, new PortalObjectPath()));
               portalURL = new PortalURLImpl(vcc, controllerCtx, Boolean.TRUE, null);
            }
            else
            {
               portalURL = new PortalURLImpl(cc, controllerCtx, Boolean.TRUE, null);
            }
            String securedLogin = config.getProperty("core.login.secured");
            if (securedLogin != null && "true".equals(securedLogin.toLowerCase()))
            {
               portalURL.setSecure(Boolean.TRUE);
            }
            rd.setAttribute("org.jboss.portal.header.LOGIN_URL", portalURL);
         }

         // Edit dashboard page || Copy to dashboard link
         boolean isDashboard = false;
         if (cc instanceof RenderPageCommand)
         {
            RenderPageCommand rpc = (RenderPageCommand)cc;
            Page page = rpc.getPage();
            String pageName = page.getName();
            isDashboard = rpc.isDashboard();

            //
            if (isDashboard)
            {
               // Edit page
               ParametersStateString navState = ParametersStateString.create();
               navState.setValue("editPageSelect", pageName);
               InvokePortletInstanceRenderCommand command = new InvokePortletInstanceRenderCommand("DashboardConfigPortletInstance", navState);
               rd.setAttribute("org.jboss.portal.header.EDIT_DASHBOARD_URL", new PortalURLImpl(command, controllerCtx, null, null));
            }
            else
            {
               //
               if (user != null)
               {
                  CustomizationManager cm = controller.getCustomizationManager();
                  Portal dashboard = cm.getDashboard(user);
                  if (dashboard.getChild(pageName) == null)
                  {
                     ImportPageToDashboardCommand iptdc = new ImportPageToDashboardCommand(page.getId());
                     rd.setAttribute("org.jboss.portal.header.COPY_TO_DASHBOARD_URL", new PortalURLImpl(iptdc, controllerCtx, null, null));
                  }
               }

            }
         }

         //
         if (!isDashboard && user != null)
         {
            Portal dashboard = controller.getCustomizationManager().getDashboard(user);
            if (dashboard != null)
            {
               ViewPortalCommand vdc = new ViewPortalCommand(dashboard.getId());
               rd.setAttribute("org.jboss.portal.header.DASHBOARD_URL", new PortalURLImpl(vdc, controllerCtx, null, null));
            }

         }

         //
         boolean admin = false;
         if (cc instanceof RenderPageCommand)
         {
            RenderPageCommand rpc = (RenderPageCommand)cc;
            PortalObject portalObject = rpc.getPage().getPortal();
            admin = "admin".equalsIgnoreCase(portalObject.getName());
         }

         //
         if (!admin || isDashboard)
         {
            PortalObjectPermission perm = new PortalObjectPermission(adminPortalId, PortalObjectPermission.VIEW_MASK);
            try
            {
               if (controller.getPortalAuthorizationManagerFactory().getManager().checkPermission(perm))
               {
                  ViewPageCommand showadmin = new ViewPageCommand(adminPortalId);
                  rd.setAttribute("org.jboss.portal.header.ADMIN_PORTAL_URL", new PortalURLImpl(showadmin, controllerCtx, null, null));
               }
            }
            catch (PortalSecurityException e)
            {
               log.error("", e);
            }
         }

         //
         if (admin || isDashboard || cc instanceof RenderPortletInstanceCommand)
         {
            // Link to default page of default portal
            // Cannot use defaultPortalId in 2.6.x because the default context doesn't have the view right.
            // Upgrading from 2.6.1 to 2.6.2 would break.
            ViewPageCommand vpc = new ViewPageCommand(portalObjectContainer.getContext().getDefaultPortal().getId());
            rd.setAttribute("org.jboss.portal.header.DEFAULT_PORTAL_URL", new PortalURLImpl(vpc, controllerCtx, null, null));
         }

         //
         SignOutCommand cmd = new SignOutCommand();
         rd.setAttribute("org.jboss.portal.header.SIGN_OUT_URL", new PortalURLImpl(cmd, controllerCtx, Boolean.FALSE, null));

         //
         rd.include();
         return rd.getMarkup();
      }

      //
      return null;
   }

   public String injectTabbedNav(PageCommand rpc)
   {
      ControllerContext controllerCtx = rpc.getControllerContext();
      ControllerRequestDispatcher rd = controllerCtx.getRequestDispatcher(targetContextPath, tabsPath);

      //
      if (rd != null)
      {
         Page page = rpc.getPage();
         PortalAuthorizationManager pam = portalAuthorizationManagerFactory.getManager();
         PortalNodeImpl node = new PortalNodeImpl(pam, page);

         //
         rd.setAttribute("org.jboss.portal.api.PORTAL_NODE", node);
         rd.setAttribute("org.jboss.portal.api.PORTAL_RUNTIME_CONTEXT", Navigation.getPortalRuntimeContext());

         //
         rd.include();
         return rd.getMarkup();
      }

      //
      return null;
   }

   public String getHeaderPath()
   {
      return headerPath;
   }

   public void setHeaderPath(String headerPath)
   {
      this.headerPath = headerPath;
   }

   public String getTargetContextPath()
   {
      return targetContextPath;
   }

   public void setTargetContextPath(String context)
   {
      targetContextPath = context;
   }

   public String getTabsPath()
   {
      return tabsPath;
   }

   public void setTabsPath(String tabsPath)
   {
      this.tabsPath = tabsPath;
   }

   private static class PortalURLImpl implements PortalURL
   {

      /** . */
      private ControllerCommand command;

      /** . */
      private ControllerContext context;

      /** . */
      private Boolean wantAuthenticated;

      /** . */
      private Boolean wantSecure;

      /** . */
      private boolean relative;

      /** . */
      private String value;


      public PortalURLImpl(ControllerCommand command, ControllerContext context, Boolean wantAuthenticated, Boolean wantSecure)
      {
         this.command = command;
         this.context = context;
         this.wantAuthenticated = wantAuthenticated;
         this.wantSecure = wantSecure;
         this.relative = false;
         this.value = null;
      }

      public void setAuthenticated(Boolean wantAuthenticated)
      {
         this.wantAuthenticated = wantAuthenticated;
         this.value = null;
      }

      public void setSecure(Boolean wantSecure)
      {
         this.wantSecure = wantSecure;
         this.value = null;
      }

      public void setRelative(boolean relative)
      {
         this.relative = relative;
         this.value = null;
      }

      public String toString()
      {
         if (value == null)
         {
            URLContext urlContext = context.getServerInvocation().getServerContext().getURLContext();
            urlContext = urlContext.withAuthenticated(wantAuthenticated).withSecured(wantSecure);
            value = context.renderURL(command, urlContext, URLFormat.newInstance(relative, true));
         }
         return value;
      }
   }

   public String getLoginNamespace()
   {
      return loginNamespace;
   }

   public void setLoginNamespace(String loginNamespace)
   {
      this.loginNamespace = loginNamespace;
   }

   public ServerConfig getConfig()
   {
      return config;
   }

   public void setConfig(ServerConfig config)
   {
      this.config = config;
   }
}
