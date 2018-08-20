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
package org.jboss.portal.core.model.portal.command.action;

import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;
import org.jboss.portal.portlet.controller.request.ContainerRequest;
import org.jboss.portal.portlet.controller.PortletController;
import org.jboss.portal.portlet.controller.response.PageUpdateResponse;
import org.jboss.portal.portlet.controller.response.PortletResponse;
import org.jboss.portal.portlet.controller.response.ResourceResponse;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.WindowCommand;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.controller.portlet.ControllerResponseFactory;
import org.jboss.portal.core.controller.portlet.ControllerPageNavigationalState;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12314 $
 */
public abstract class InvokeWindowCommand extends WindowCommand
{

   public InvokeWindowCommand(PortalObjectId windowId) throws IllegalArgumentException
   {
      super(windowId);
   }

   protected abstract ContainerRequest createPortletRequest(
      PortletInfo portletInfo,
      PortletPageNavigationalState pageNS,
      PortletWindowNavigationalState windowNS);

}
