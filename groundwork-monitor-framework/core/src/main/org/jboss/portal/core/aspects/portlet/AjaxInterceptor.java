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

import org.jboss.portal.core.portlet.info.AjaxInfo;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.FragmentResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;

/**
 * Look at the portlet ajax meta data.
 * <p/>
 * Improve later when we will have the structure to build real portlet runtime meta information.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class AjaxInterceptor extends CorePortletInterceptor
{

   /** . */
   public static final String PARTIAL_REFRESH = "partialRefresh";

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      PortletInvocationResponse response = super.invoke(invocation);

      //
      if (response instanceof FragmentResponse)
      {
         FragmentResponse fragment = (FragmentResponse)response;
         PortletInfo corePortletInfo = getPortletInfo(invocation);
         if (corePortletInfo != null)
         {
            AjaxInfo ajax = corePortletInfo.getAttachment(AjaxInfo.class);
            if (ajax != null)
            {
               fragment = updateFragmentWithPropertiesIfNeeded(fragment);
               ResponseProperties props = fragment.getProperties();
               props.getTransportHeaders().setValue(PARTIAL_REFRESH, "" + ajax.getPartialRefresh());

               return fragment;
            }
         }
      }

      //
      return response;
   }
}
