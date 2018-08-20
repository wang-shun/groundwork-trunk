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
package org.jboss.portal.core.aspects.portlet;

import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.core.metadata.portlet.MarkupElement;
import org.jboss.portal.core.portlet.info.MarkupHeaderInfo;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.FragmentResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;

/**
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11068 $
 */
public class HeaderInterceptor extends CorePortletInterceptor
{

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      PortletInvocationResponse response = super.invoke(invocation);

      // Only affect fragments
      if (response instanceof FragmentResponse)
      {
         FragmentResponse fragment = (FragmentResponse)response;

         PortletInfo portletInfo = getPortletInfo(invocation);
         if (portletInfo != null)
         {
            MarkupHeaderInfo headerContentInfo = portletInfo.getAttachment(MarkupHeaderInfo.class);
            if (headerContentInfo != null)
            {
               fragment = updateFragmentWithPropertiesIfNeeded(fragment);
               ResponseProperties props = fragment.getProperties();

               // Get the context path
               String contextPath = (String)invocation.getDispatchedRequest().getAttribute("javax.servlet.include.context_path");

               MultiValuedPropertyMap<Element> headers = props.getMarkupHeaders();
               for (MarkupElement markupElement : headerContentInfo.getMarkupElements())
               {
                  headers.addValue(MimeResponse.MARKUP_HEAD_ELEMENT, markupElement.toElement(contextPath));
               }

               return fragment;
            }
         }
      }

      //
      return response;
   }
}
