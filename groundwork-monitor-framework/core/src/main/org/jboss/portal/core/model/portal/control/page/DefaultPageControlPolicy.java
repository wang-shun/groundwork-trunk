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
package org.jboss.portal.core.model.portal.control.page;

import org.jboss.logging.Logger;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerRequestDispatcher;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.command.response.MarkupResponse;
import org.jboss.portal.core.model.portal.content.WindowRendition;
import org.jboss.portal.core.model.portal.control.ControlConstants;
import org.jboss.portal.server.config.ServerConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class DefaultPageControlPolicy implements PageControlPolicy
{

   /** . */
   private static final Logger log = Logger.getLogger(DefaultPageControlPolicy.class);

   /** . */
   private static final Map errorTypeMapping = new HashMap();

   static
   {
      errorTypeMapping.put(ControlConstants.PAGE_ACCESS_DENIED_CONTROL_KEY, ControlConstants.ACCESS_DENIED_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PAGE_ERROR_CONTROL_KEY, ControlConstants.ERROR_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PAGE_INTERNAL_ERROR_CONTROL_KEY, ControlConstants.INTERNAL_ERROR_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PAGE_UNAVAILABLE_CONTROL_KEY, ControlConstants.UNAVAILABLE_ERROR_TYPE);
      errorTypeMapping.put(ControlConstants.PAGE_NOT_FOUND_CONTROL_KEY, ControlConstants.NOT_FOUND_ERROR_TYPE);
   }

   /** . */
   private ServerConfig serverConfig;

   /** . */
   private PortalObjectContainer portalObjectContainer;

   public ServerConfig getServerConfig()
   {
      return serverConfig;
   }

   public void setServerConfig(ServerConfig serverConfig)
   {
      this.serverConfig = serverConfig;
   }

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public void doControl(PageControlContext controlContext)
   {
      WindowRendition rendition = controlContext.getRendition();

      //
      ControllerResponse response = rendition.getControllerResponse();

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
               policyKey = ControlConstants.PAGE_ACCESS_DENIED_CONTROL_KEY;
            }
         }
         else
         {
            policyKey = error.isInternal() ? ControlConstants.PAGE_INTERNAL_ERROR_CONTROL_KEY : ControlConstants.PAGE_ERROR_CONTROL_KEY;
         }
      }
      else if (response instanceof UnavailableResourceResponse)
      {
         UnavailableResourceResponse unavailable = (UnavailableResourceResponse)response;

         //
         if (log.isTraceEnabled())
         {
            log.trace("Window not found " + unavailable.getRef());
         }

         policyKey = unavailable.isLocated() ? ControlConstants.PAGE_UNAVAILABLE_CONTROL_KEY : ControlConstants.PAGE_NOT_FOUND_CONTROL_KEY;
      }

      //
      if (cause != null)
      {
         log.error("Rendering portlet window " + "" + " produced an error", cause);
      }

      //
      if (policyKey != null)
      {
         Map properties = controlContext.getRendition().getProperties();

         //
         String policyValue = (String)properties.get(policyKey);

         //
         if (policyValue != null)
         {
            if (ControlConstants.HIDE_CONTROL_VALUE.equals(policyValue))
            {
               rendition.setControllerResponse(null);
            }
            else if (ControlConstants.JSP_CONTROL_VALUE.equals(policyValue))
            {
               String resourceURI = (String)properties.get(ControlConstants.PAGE_RESOURCE_URI_CONTROL_KEY);
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
                     String markup = rd.getMarkup();
                     rendition.setSupportedWindowStates(Collections.EMPTY_LIST);
                     rendition.setSupportedModes(Collections.EMPTY_LIST);
                     rendition.setControllerResponse(new MarkupResponse("An error occured", markup, null));
                  }
               }
            }
         }
      }
   }

}
