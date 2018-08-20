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

package org.jboss.portal.core.aspects.portlet;

import org.jboss.portal.portlet.PortletInvokerInterceptor;
import org.jboss.portal.portlet.container.PortletContainer;
import org.jboss.portal.portlet.container.ContainerPortletInvoker;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.FragmentResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class CorePortletInterceptor extends PortletInvokerInterceptor
{
   /**
    * Retrieve the CorePortetInfo, if any, associated with the Portlet being invoked.
    *
    * @param invocation the current PortletInvocation
    * @return the CorePortetInfo associated with the Portlet being invoked or <code>null</code> if no such information
    *         is present
    */
   protected PortletInfo getPortletInfo(PortletInvocation invocation)
   {
      PortletContainer container = (PortletContainer)invocation.getAttribute(ContainerPortletInvoker.PORTLET_CONTAINER);
      return container.getInfo();
   }

   /**
    * Create a new FragmentResponse if the current one doesn't have any properties.
    *
    * @param fragment the current FragmentResponse
    * @return the passed FragmentResponse or a new one with properties
    */
   protected FragmentResponse updateFragmentWithPropertiesIfNeeded(FragmentResponse fragment)
   {
      ResponseProperties props = fragment.getProperties();

      // if we don't currently have properties, copy the current response and add properties
      if (props == null)
      {
         props = new ResponseProperties();
         fragment = new FragmentResponse(props, fragment.getAttributes(),
            fragment.getContentType(), fragment.getBytes(), fragment.getChars(), fragment.getTitle(),
            fragment.getCacheControl(), fragment.getNextModes());
      }
      return fragment;
   }
}
