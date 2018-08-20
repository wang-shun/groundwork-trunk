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
package org.jboss.portal.core.model.portal.control.portal;

import org.jboss.logging.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerRequestDispatcher;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.AccessDeniedException;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.control.ControlConstants;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.theme.LayoutService;
import org.jboss.portal.theme.PageService;
import org.jboss.portal.theme.PortalLayout;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.page.PageResult;
import org.jboss.portal.theme.page.WindowContext;
import org.jboss.portal.theme.page.WindowResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12092 $
 */
public class DefaultPortalControlPolicy implements PortalControlPolicy
{

   /** . */
   private static final Logger log = Logger.getLogger(DefaultPortalControlPolicy.class);

   /** . */
   private static final Map errorTypeMapping = new HashMap();

   static
   {
      errorTypeMapping.put(ControlConstants.PORTAL_ACCESS_DENIED_CONTROL_KEY, ControlConstants.ACCESS_DENIED_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PORTAL_ERROR_CONTROL_KEY, ControlConstants.ERROR_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PORTAL_INTERNAL_ERROR_CONTROL_KEY, ControlConstants.INTERNAL_ERROR_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PORTAL_UNAVAILABLE_CONTROL_KEY, ControlConstants.UNAVAILABLE_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PORTAL_NOT_FOUND_CONTROL_KEY, ControlConstants.NOT_FOUND_ERROR_TYPE);
   }

   /** . */
   private PortalObjectContainer portalObjectContainer;

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public void doControl(PortalControlContext controlContext)
   {

      ControllerResponse response = controlContext.getResponse();

      //
      String policyKey = null;
      Throwable cause = null;
      String message = null;

      //
      if (response instanceof ErrorResponse)
      {
         ErrorResponse error = (ErrorResponse)response;
         cause = error.getCause();
         message = error.getMessage();

         //
         if (response instanceof SecurityErrorResponse)
         {
            SecurityErrorResponse ser = (SecurityErrorResponse)response;

            //
            if (ser.getStatus() == SecurityErrorResponse.NOT_AUTHORIZED)
            {
               policyKey = ControlConstants.PORTAL_ACCESS_DENIED_CONTROL_KEY;
            }
         }
         else
         {
            policyKey = error.isInternal() ? ControlConstants.PORTAL_INTERNAL_ERROR_CONTROL_KEY : ControlConstants.PORTAL_ERROR_CONTROL_KEY;
         }
      }
      else if (response instanceof UnavailableResourceResponse)
      {
         UnavailableResourceResponse unavailable = (UnavailableResourceResponse)response;

         //
         policyKey = unavailable.isLocated() ? ControlConstants.PORTAL_UNAVAILABLE_CONTROL_KEY : ControlConstants.PORTAL_NOT_FOUND_CONTROL_KEY;
      }

      //
      if (cause != null)
      {
         if (cause instanceof AccessDeniedException)
         {
            log.debug("Rendering portlet window " + "" + " produced an error", cause);
         }
         else
         {
            log.error("Rendering portlet window " + "" + " produced an error", cause);
         }
      }

      //
      String policyValue;
      if (policyKey != null)
      {
         PortalObject object = portalObjectContainer.getObject(controlContext.getPortalId());
         if (object != null)
         {
            policyValue = object.getProperty(policyKey);

            if (policyValue != null)
            {
               if (ControlConstants.JSP_CONTROL_VALUE.equals(policyValue))
               {
                  String resourceURI = object.getProperty(ControlConstants.PORTAL_RESOURCE_URI_CONTROL_KEY);
                  if (resourceURI != null)
                  {
                     ControllerContext controllerCtx = controlContext.getControllerContext();
                     ControllerRequestDispatcher rd = controllerCtx.getRequestDispatcher("/portal-core", resourceURI);
                     if (rd != null)
                     {
                        String errorType = (String)errorTypeMapping.get(policyKey);
                        rd.setAttribute(ControlConstants.ERROR_TYPE_ATTRIBUTE, errorType);
                        rd.setAttribute(ControlConstants.CAUSE_ATTRIBUTE, cause);
                        rd.setAttribute(ControlConstants.MESSAGE_ATTRIBUTE, message);

                        //
                        rd.include();

                        //
                        PageService ps = controlContext.getControllerContext().getController().getPageService();
                        LayoutService ls = ps.getLayoutService();
                        PortalLayout layout = ls.getLayout("generic", true);
                        Map pageProperties = new HashMap();
                        pageProperties.put("theme.renderSetId", "divRenderer");
                        pageProperties.put("theme.id", "renewal");
                        PageResult result = new PageResult("BILTO", pageProperties);

                        //
                        Map windowProps = new HashMap();
                        windowProps.put(ThemeConstants.PORTAL_PROP_WINDOW_RENDERER, "emptyRenderer");
                        windowProps.put(ThemeConstants.PORTAL_PROP_DECORATION_RENDERER, "emptyRenderer");
                        windowProps.put(ThemeConstants.PORTAL_PROP_PORTLET_RENDERER, "emptyRenderer");

                        //
                        WindowResult res = new WindowResult("", rd.getMarkup(), Collections.EMPTY_MAP, windowProps, null, WindowState.MAXIMIZED, Mode.VIEW);
                        WindowContext blah = new WindowContext("BILTO", "maximized", "0", res);
                        result.addWindowContext(blah);

                        //
                        controlContext.setResponse(new PageRendition(layout, null, result, ps));
                     }
                  }
               }
            }
         }
      }
   }
}
