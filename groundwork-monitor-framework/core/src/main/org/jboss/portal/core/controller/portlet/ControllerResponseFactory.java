/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.FixMe;
import org.jboss.portal.common.NotYetImplemented;
import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.command.response.RedirectionResponse;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.core.controller.command.response.SignOutResponse;
import org.jboss.portal.core.controller.command.response.StreamContentResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.model.instance.command.response.PortletInstanceActionResponse;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.response.PortletWindowActionResponse;
import org.jboss.portal.core.model.portal.navstate.PageNavigationalState;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.info.ParameterInfo;
import org.jboss.portal.portlet.invocation.response.ContentResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;
import org.jboss.portal.portlet.invocation.response.UpdateNavigationalStateResponse;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12953 $
 */
public class ControllerResponseFactory
{

   public static ControllerResponse createResponse(PortletInvokerException e)
   {
      if (e instanceof NoSuchPortletException)
      {
         return new UnavailableResourceResponse(((NoSuchPortletException)e).getPortletId(), false);
      }
      else
      {
         return new org.jboss.portal.core.controller.command.response.ErrorResponse(e, false);
      }
   }

   public static ControllerResponse createActionResponse(PortalObjectId targetId, PortletInvocationResponse response, org.jboss.portal.portlet.info.PortletInfo portletInfo, PageNavigationalState pns)
   {
      if (response instanceof UpdateNavigationalStateResponse)
      {
         UpdateNavigationalStateResponse renderResult = (UpdateNavigationalStateResponse)response;

         //
         Mode mode = renderResult.getMode();

         //
         WindowState windowState = renderResult.getWindowState();

         StateString state = renderResult.getNavigationalState();

         // if we are in the local case, decode the parameters and mix in public navigational state if needed
         // in the WSRP case, we get an OpaqueStateString that we just pass along as is
         if (state instanceof ParametersStateString)
         {
            Map<String, String[]> stringMap = ((ParametersStateString)state).getParameters();

            Map<String, String[]> parameters = new HashMap<String, String[]>(stringMap);
          
            parameters.putAll(renderResult.getPublicNavigationalStateUpdates());

            if (pns != null)
            {
               //
               for (ParameterInfo parameterInfo : portletInfo.getNavigation().getPublicParameters())
               {
                  String key = parameterInfo.getId();

                  //
                  String[] values = pns.getParameter(parameterInfo.getName());

                  //
                  if (values != null)
                  {
                     parameters.put(key, values);
                  }
               }
            }
            state = ParametersStateString.create(parameters);
         }

         return new PortletWindowActionResponse(targetId, windowState, mode, state);
      }
      else
      {
         return createResponse(response);
      }
   }

   public static ControllerResponse createActionResponse(String instanceId, PortletInvocationResponse response)
   {
      if (response instanceof UpdateNavigationalStateResponse)
      {
         UpdateNavigationalStateResponse renderResult = (UpdateNavigationalStateResponse)response;

         //
         return new PortletInstanceActionResponse(instanceId, null, null, renderResult.getNavigationalState());
      }
      else
      {
         return createResponse(response);
      }
   }

   private static ControllerResponse createResponse(PortletInvocationResponse response)
   {
      if (response instanceof org.jboss.portal.portlet.invocation.response.HTTPRedirectionResponse)
      {
         org.jboss.portal.portlet.invocation.response.HTTPRedirectionResponse redirection = (org.jboss.portal.portlet.invocation.response.HTTPRedirectionResponse)response;
         String location = redirection.getLocation();
         return new RedirectionResponse(location);
      }
      else if (response instanceof org.jboss.portal.portlet.invocation.response.InsufficientTransportGuaranteeResponse)
      {
         return new SecurityErrorResponse(SecurityErrorResponse.NOT_SECURE, false);
      }
      else if (response instanceof org.jboss.portal.portlet.invocation.response.InsufficientPrivilegesResponse)
      {
         return new SecurityErrorResponse(SecurityErrorResponse.NOT_AUTHORIZED, false);
      }
      else if (response instanceof org.jboss.portal.core.controller.portlet.SignOutResponse)
      {
         return new SignOutResponse(((org.jboss.portal.core.controller.portlet.SignOutResponse)response).getLocation());
      }
      else if (response instanceof org.jboss.portal.portlet.invocation.response.ErrorResponse)
      {
         return new ErrorResponse(((org.jboss.portal.portlet.invocation.response.ErrorResponse)response).getCause(), false);
      }
      else if (response instanceof org.jboss.portal.portlet.invocation.response.SecurityErrorResponse)
      {
         org.jboss.portal.portlet.invocation.response.SecurityErrorResponse ser = (org.jboss.portal.portlet.invocation.response.SecurityErrorResponse)response;
         return new SecurityErrorResponse(ser.getThrowable(), SecurityErrorResponse.NOT_AUTHORIZED, false);
      }
      else if (response instanceof ContentResponse)
      {
         ContentResponse contentResponse = (ContentResponse)response;

         //
         int type = contentResponse.getType();
         if (type == ContentResponse.TYPE_EMPTY)
         {
            throw new NotYetImplemented("handling of empty ContentResponse");
         }
         else
         {
            String contentType = contentResponse.getContentType();

            //
            ResponseProperties properties = contentResponse.getProperties();
            MultiValuedPropertyMap<String> headers = null;
            if (properties != null)
            {
               headers = properties.getTransportHeaders();
            }
            if (type == ContentResponse.TYPE_BYTES)
            {
               return new StreamContentResponse(contentType, headers, new ByteArrayInputStream(contentResponse.getBytes()));
            }
            else
            {
               return new StreamContentResponse(contentType, headers, new StringReader(contentResponse.getChars()));
            }
         }
      }
      else
      {
         throw new FixMe("Undefined response mapping for " + response);
      }
   }
}
