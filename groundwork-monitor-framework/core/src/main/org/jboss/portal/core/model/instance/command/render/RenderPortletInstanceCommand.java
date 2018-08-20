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
package org.jboss.portal.core.model.instance.command.render;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.NotYetImplemented;
import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.portlet.InvokePortletCommandFactory;
import org.jboss.portal.core.controller.portlet.PortletContextFactory;
import org.jboss.portal.core.controller.portlet.PortletInvocationFactory;
import org.jboss.portal.core.model.instance.InvokePortletInstanceCommandFactory;
import org.jboss.portal.core.model.instance.command.PortletInstanceCommand;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.response.ErrorResponse;
import org.jboss.portal.portlet.invocation.response.FragmentResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;
import org.jboss.portal.theme.LayoutService;
import org.jboss.portal.theme.PageService;
import org.jboss.portal.theme.PortalLayout;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.page.PageResult;
import org.jboss.portal.theme.page.WindowContext;
import org.jboss.portal.theme.page.WindowResult;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.MimeResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11754 $
 */
public class RenderPortletInstanceCommand extends PortletInstanceCommand
{

   public RenderPortletInstanceCommand(String instanceId, StateString navigationalState)
   {
      super(instanceId, navigationalState);
   }

   public CommandInfo getInfo()
   {
      return null;
   }

   public ControllerResponse execute() throws ControllerException
   {

      try
      {
         PortletContextFactory pcf1 = new PortletContextFactory(context);
         InvokePortletCommandFactory pcf2 = new InvokePortletInstanceCommandFactory(instanceId);
         RenderInvocation render = PortletInvocationFactory.createRender(context, Mode.VIEW, WindowState.MAXIMIZED, navigationalState, pcf1, pcf2);
         PortletInvocationResponse response = instance.invoke(render);

         // For now let the controller handle non fragment response
         String content;
         List<Element> headElements = null;
         if (response instanceof FragmentResponse)
         {
            FragmentResponse fragment = (FragmentResponse)response;
            content = fragment.getContent();
            ResponseProperties properties = fragment.getProperties();
            if (properties != null)
            {
               // header handling
               MultiValuedPropertyMap<Element> headers = properties.getMarkupHeaders();
               headElements = headers.getValues(MimeResponse.MARKUP_HEAD_ELEMENT);
            }
         }
         else if (response instanceof ErrorResponse)
         {
            content = ((ErrorResponse)response).toHTML();
         }
         else
         {
            throw new NotYetImplemented();
         }

         //
         PageService ps = context.getController().getPageService();
         LayoutService ls = ps.getLayoutService();
         PortalLayout layout = ls.getLayout("generic", true);
         Map pageProperties = new HashMap();
         pageProperties.put("theme.renderSetId", "divRenderer");
         pageProperties.put("theme.id", "renewal");
         PageResult result = new PageResult("BILTO", pageProperties);

         //
         Map windowProps = new HashMap();
         windowProps.put(ThemeConstants.PORTAL_PROP_WINDOW_RENDERER, "emptyRenderer");
         windowProps.put(ThemeConstants.PORTAL_PROP_DECORATION_RENDERER, "emptyRenderer");
         windowProps.put(ThemeConstants.PORTAL_PROP_PORTLET_RENDERER, "emptyRenderer");

         //
         WindowResult res = new WindowResult("", content, Collections.EMPTY_MAP, windowProps, headElements, WindowState.MAXIMIZED, Mode.VIEW);
         WindowContext blah = new WindowContext("BILTO", "maximized", "0", res);
         result.addWindowContext(blah);

         //
         return new PageRendition(layout, null, result, ps);
      }
      catch (PortletInvokerException e)
      {
         throw new ControllerException(e);
      }
   }
}
