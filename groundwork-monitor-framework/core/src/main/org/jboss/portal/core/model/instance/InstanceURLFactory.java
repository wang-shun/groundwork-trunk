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
package org.jboss.portal.core.model.instance;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.URLFactoryDelegate;
import org.jboss.portal.core.model.instance.command.PortletInstanceCommand;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceActionCommand;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceRenderCommand;
import org.jboss.portal.core.model.instance.command.render.RenderPortletInstanceCommand;
import org.jboss.portal.core.portlet.PortletRequestEncoder;
import org.jboss.portal.server.AbstractServerURL;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerURL;
import org.jboss.util.NotImplementedException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10545 $
 */
public class InstanceURLFactory extends URLFactoryDelegate
{

   /** . */
   private String path;

   /** . */
   private String instanceId;

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   public String getInstanceId()
   {
      return instanceId;
   }

   public void setInstanceId(String instanceId)
   {
      this.instanceId = instanceId;
   }

   public ServerURL doMapping(ControllerContext controllerContext, ServerInvocation invocation, ControllerCommand cmd)
   {
      if (cmd == null)
      {
         throw new IllegalArgumentException("No null command accepted");
      }

      //
      if (cmd instanceof PortletInstanceCommand)
      {
         PortletInstanceCommand iic = (PortletInstanceCommand)cmd;
         if (iic.getInstanceId().equals(instanceId))
         {
            AbstractServerURL url = new AbstractServerURL();
            url.setPortalRequestPath(path);
            if (cmd instanceof InvokePortletInstanceRenderCommand)
            {
               InvokePortletInstanceRenderCommand iprc = (InvokePortletInstanceRenderCommand)cmd;
               PortletRequestEncoder encoder = new PortletRequestEncoder(url.getParameterMap());
               encoder.encodeRender(iprc.getNavigationalState(), null, null);
            }
            else if (cmd instanceof RenderPortletInstanceCommand)
            {
               RenderPortletInstanceCommand rpic = (RenderPortletInstanceCommand)cmd;
               PortletRequestEncoder encoder = new PortletRequestEncoder(url.getParameterMap());
               encoder.encodeRender(rpic.getNavigationalState(), null, null);
            }
            else if (cmd instanceof InvokePortletInstanceActionCommand)
            {
               InvokePortletInstanceActionCommand iprc = (InvokePortletInstanceActionCommand)cmd;
               PortletRequestEncoder encoder = new PortletRequestEncoder(url.getParameterMap());
               encoder.encodeAction(iprc.getNavigationalState(), iprc.getInteractionState(), null, null);
            }
            else
            {
               throw new NotImplementedException(cmd + "is an unknown sub-class of PortletInstanceCommand");
            }
            return url;

         }
      }

      //
      return null;
   }
}
