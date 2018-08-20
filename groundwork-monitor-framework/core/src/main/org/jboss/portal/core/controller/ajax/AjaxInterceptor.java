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
package org.jboss.portal.core.controller.ajax;

import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.navstate.NavigationalStateContext;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.server.AbstractServerURL;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.server.ServerURL;
import org.jboss.portal.server.request.URLFormat;
import org.jboss.portal.theme.impl.render.dynamic.DynaConstants;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author Luca Stancapiano
 * @version $Revision: 10530 $
 */
public class AjaxInterceptor extends ControllerInterceptor
{
   /**
    * Luca Stancapiano - 29 - 09 -2007 targetContextPath is used to assign a context path when default portal-ajax path
    * cannot to be used or if it is changed through context-root property into jboss-web.xml
    */
   private String targetContextPath = "/portal-ajax";

   public ControllerResponse invoke(ControllerCommand cmd) throws Exception, InvocationException
   {
      ControllerResponse response = (ControllerResponse)cmd.invokeNext();

      // Configure ajax if needed
      if (response instanceof PageRendition)
      {
         ServerInvocationContext serverContext = cmd.getControllerContext().getServerInvocation().getServerContext();

         //
         ControllerContext controllerContext = cmd.getControllerContext();

         //
         PageRendition rendition = (PageRendition)response;
         Map pageProps = rendition.getPageResult().getProperties();

         //
         NavigationalStateContext ctx = (NavigationalStateContext)controllerContext.getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE);
         String viewId = ctx.getViewId();

         //
         ServerURL baseServerURL = new AbstractServerURL();
         baseServerURL.setPortalRequestPath("/");
         String url = serverContext.renderURL(baseServerURL, serverContext.getURLContext(), URLFormat.newInstance(true, true));

         //
         pageProps.put(DynaConstants.RESOURCE_BASE_URL, targetContextPath + "/dyna");
         pageProps.put(DynaConstants.SERVER_BASE_URL, url);
         pageProps.put(DynaConstants.VIEW_STATE, viewId);

         // If user is logged in and is on dashboard we enable ajax
//         if (cmd instanceof RenderPageCommand)
//         {
//            RenderPageCommand rpc = (RenderPageCommand)cmd;
//            if (serverContext.getClientRequest().getRemoteUser() != null && rpc.isDashboard())
//            {
//               DynaRenderOptions.AJAX.setOptions(pageProps);
//            }
//         }
      }

      //
      return response;
   }

   public String getTargetContextPath()
   {
      return targetContextPath;
   }

   public void setTargetContextPath(String context)
   {
      targetContextPath = context;
   }
}
