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
package org.jboss.portal.core.impl.model.content.generic;

import org.apache.log4j.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.api.content.SelectedContent;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.CoreConstants;
import org.jboss.portal.core.impl.model.content.InternalContentProvider;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.content.spi.handler.ContentState;
import org.jboss.portal.core.model.content.spi.portlet.ContentPortlet;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.response.MarkupResponse;
import org.jboss.portal.core.model.portal.content.ContentRendererContext;
import org.jboss.portal.core.model.portal.content.WindowRendition;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;
import org.jboss.portal.portlet.info.NavigationInfo;
import org.jboss.portal.portlet.info.ParameterInfo;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.RenderInvocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11705 $
 */
public class InternalGenericContentProvider extends InternalContentProvider implements ContentHandler
{

   /** . */
   private static final Logger log = Logger.getLogger(InternalGenericContentProvider.class);

   /** . */
   protected InstanceContainer instanceContainer;

   /** . */
   protected boolean decorateContent;

   /** . */
   protected ContentPortlet contentPortlet;

   public InstanceContainer getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainer instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   public ContentPortlet getContentPortletInfo()
   {
      return contentPortlet;
   }

   public void setContentPortletInfo(ContentPortlet contentPortlet)
   {
      this.contentPortlet = contentPortlet;
   }

   public boolean getDecorateContent()
   {
      return decorateContent;
   }

   public void setDecorateContent(boolean decorateContent)
   {
      this.decorateContent = decorateContent;
   }

   protected ContentProvider createProvider()
   {
      return new ContentProvider()
      {
         public ContentType getContentType()
         {
            return getRegisteredContentType();
         }

         public LocalizedString getDisplayName()
         {
            return null;
         }

         public LocalizedString getDescription()
         {
            return null;
         }

         public ContentHandler getHandler()
         {
            return InternalGenericContentProvider.this;
         }

         public ContentPortlet getPortletInfo()
         {
            return contentPortlet;
         }
      };
   }

   public Content newContent(String contextId, ContentState state)
   {
      return new GenericContent(state);
   }

   protected Instance getPortletInstance(ContentRendererContext rendererContext)
   {
      String id = contentPortlet.getPortletName(Mode.VIEW);

      //
      if (id != null)
      {
         return instanceContainer.getDefinition(id);
      }
      else
      {
         return null;
      }
   }

   public void contentCreated(String contextId, ContentState state)
   {
   }

   public void contentDestroyed(String contextId, ContentState state)
   {
   }

   public WindowRendition renderWindow(final ContentRendererContext rendererContext)
   {
      Window window = rendererContext.getWindow();

      // No content
      final Content content = window.getContent();
      if (content == null)
      {
         return null;
      }

      //
      ContentRendererContext rendererContext2 = new ContentRendererContext()
      {
         public Window getWindow()
         {
            return rendererContext.getWindow();
         }

         public PortletWindowNavigationalState getPortletNavigationalState()
         {
            return rendererContext.getPortletNavigationalState();
         }

         public User getUser()
         {
            return rendererContext.getUser();
         }

         public RenderInvocation createRenderInvocation(PortletWindowNavigationalState navigationalState)
         {
            RenderInvocation invocation = rendererContext.createRenderInvocation(navigationalState);

            //
            if (invocation.getPublicNavigationalState() == null || invocation.getPublicNavigationalState().size() == 0)
            {

               String id_uri = null;
               String id_parameters = null;

               try
               {
                  Instance instance = getPortletInstance(rendererContext);
                  Portlet portlet = instance.getPortlet();
                  PortletInfo portletInfo = portlet.getInfo();
                  NavigationInfo navigationInfo = portletInfo.getNavigation();

                  ParameterInfo parameterInfo = navigationInfo.getPublicParameter(CoreConstants.JBOSS_PORTAL_CONTENT_URI);
                  if (parameterInfo != null)
                  {
                     id_uri = parameterInfo.getId();
                  }
                  
                  parameterInfo = navigationInfo.getPublicParameter(CoreConstants.JBOSS_PORTAL_CONTENT_PARAMETERS);
                  if (parameterInfo != null)
                  {
                     id_parameters = parameterInfo.getId();
                  }
               }
               catch (PortletInvokerException e)
               {
                  log.error("Cannot read portlet instance public navigational info", e);
               }

               
               ParameterMap parameterMap = new ParameterMap();

               //
               if (id_uri != null)
               {
                  parameterMap.put(id_uri, new String[]{content.getURI()});
               }
               if (id_parameters != null)
               {
                  List<String> paramNames = new ArrayList<String>();
                  Iterator<String> params = content.getParameterNames();
                  ParameterMap parameterMap2 = new ParameterMap();
                  while (params.hasNext())
                  {
                     String name = params.next();
                     String value = content.getParameter(name);
                     parameterMap2.put(id_parameters + "." + name, new String[]{value});
                     paramNames.add(name);
                  }
                  invocation.setNavigationalState(ParametersStateString.create(parameterMap2));
                  
                  if (paramNames.size() != 0)
                  {
                     parameterMap.put(id_parameters, paramNames.toArray(new String[paramNames.size()]));
                  }
               }
               invocation.setPublicNavigationalState(parameterMap);
            }

            //
            return invocation;
         }
      };

      //
      WindowRendition rendition = super.renderWindow(rendererContext2);

      //
      if (rendition != null && rendition.getControllerResponse() instanceof MarkupResponse && !getDecorateContent())
      {
         Map<String, String> props = rendition.getProperties();
         if (props.get("theme.windowRendererId") == null)
         {
            props.put("theme.windowRendererId", "emptyRenderer");
         }
         if (props.get("theme.decorationRendererId") == null)
         {
            props.put("theme.decorationRendererId", "emptyRenderer");
         }
         if (props.get("theme.portletRendererId") == null)
         {
            props.put("theme.portletRendererId", "emptyRenderer");
         }
      }

      //
      return rendition;
   }
}
