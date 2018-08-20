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
package org.jboss.portal.core.controller.command.mapper;

import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.SignOutCommand;
import org.jboss.portal.server.ServerInvocation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision: 10531 $
 */
public class SignOutCommandFactoryService extends AbstractCommandFactory implements SignOutCommandFactory
{

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host,
                                      String contextPath, String requestPath)
   {
      String location = null;
      ParameterMap parameterMap = controllerContext.getServerInvocation().getServerContext().getQueryParameterMap();
      if (parameterMap != null)
      {
         try
         {
            if (parameterMap.get("location") != null)
            {
               location = URLDecoder.decode(parameterMap.get("location")[0], "UTF-8");
            }
         }
         catch (UnsupportedEncodingException e)
         {
            // ignore
         }
      }

      if (location == null)
      {
         return new SignOutCommand();
      }
      else
      {
         return new SignOutCommand(location);
      }
   }

}

