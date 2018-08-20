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

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class SimpleCommandFactory extends AbstractCommandFactory
{

   /** . */
   private String commandClassName;

   /** . */
   private Class commandClass;


   public String getCommandClassName()
   {
      return commandClassName;
   }

   public void setCommandClassName(String commandClassName)
   {
      this.commandClassName = commandClassName;
   }

   protected void startService() throws Exception
   {
      commandClass = Thread.currentThread().getContextClassLoader().loadClass(commandClassName);

      //
      super.startService();
   }


   protected void stopService() throws Exception
   {
      super.stopService();

      //
      commandClass = null;
   }

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host, String contextPath, String requestPath)
   {
      try
      {
         return (ControllerCommand)commandClass.newInstance();
      }
      catch (Exception e)
      {
         log.error("Cannot create command", e);
         return null;
      }
   }
}
