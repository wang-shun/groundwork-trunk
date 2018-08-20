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
package org.jboss.portlet.filter;

import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossActionResponse;
import org.jboss.portlet.JBossRenderRequest;
import org.jboss.portlet.JBossRenderResponse;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;
import java.io.IOException;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision$
 */
public class JBossPortletFilter implements ActionFilter, RenderFilter, ResourceFilter, EventFilter
{

   public void doFilter(ActionRequest request, ActionResponse response, FilterChain filterChain) throws IOException, PortletException
   {
      JBossActionRequest actionRequest = new JBossActionRequest(request);
      JBossActionResponse actionResponse = new JBossActionResponse(response);

      filterChain.doFilter(actionRequest, actionResponse);
   }

   public void destroy()
   {
      // FIXME destroy

   }

   public void init(FilterConfig arg0) throws PortletException
   {
      // FIXME init

   }

   public void doFilter(RenderRequest request, RenderResponse response, FilterChain filterChain) throws IOException, PortletException
   {
      JBossRenderRequest renderRequest = new JBossRenderRequest(request);
      JBossRenderResponse renderResponse = new JBossRenderResponse(response);

      filterChain.doFilter(renderRequest, renderResponse);
   }

   public void doFilter(ResourceRequest resourceRequest, ResourceResponse resourceResponse, FilterChain filterChain) throws IOException, PortletException
   {
      filterChain.doFilter(resourceRequest, resourceResponse);
   }

   public void doFilter(EventRequest eventRequest, EventResponse eventResponse, FilterChain filterChain) throws IOException, PortletException
   {
      filterChain.doFilter(eventRequest, eventResponse);
   }
}

