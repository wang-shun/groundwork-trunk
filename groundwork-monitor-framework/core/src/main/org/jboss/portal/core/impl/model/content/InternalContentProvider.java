/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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
package org.jboss.portal.core.impl.model.content;

import org.jboss.logging.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.net.media.MediaType;
import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.core.aspects.portlet.AjaxInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstancePermission;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.response.MarkupResponse;
import org.jboss.portal.core.model.portal.content.ContentRenderer;
import org.jboss.portal.core.model.portal.content.ContentRendererContext;
import org.jboss.portal.core.model.portal.content.WindowRendition;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;
import org.jboss.portal.portlet.info.CapabilitiesInfo;
import org.jboss.portal.portlet.info.MetaInfo;
import org.jboss.portal.portlet.info.ModeInfo;
import org.jboss.portal.portlet.info.WindowStateInfo;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.response.ErrorResponse;
import org.jboss.portal.portlet.invocation.response.FragmentResponse;
import org.jboss.portal.portlet.invocation.response.InsufficientPrivilegesResponse;
import org.jboss.portal.portlet.invocation.response.InsufficientTransportGuaranteeResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.invocation.response.ResponseProperties;
import org.jboss.portal.portlet.invocation.response.UnavailableResponse;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12809 $
 */
public abstract class InternalContentProvider implements ContentRenderer
{

   /** . */
   private final Logger log = Logger.getLogger(getClass());

   /** . */
   private InternalContentProviderRegistry registry;

   /** . */
   private String contentType;

   /** . */
   private PortalAuthorizationManagerFactory pamf;

   /** . */
   ContentType registeredContentType;

   /** . */
   ContentProvider contentProvider;

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return pamf;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.pamf = portalAuthorizationManagerFactory;
   }

   public String getContentType()
   {
      return contentType;
   }

   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }

   public ContentType getRegisteredContentType()
   {
      return registeredContentType;
   }

   public InternalContentProviderRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(InternalContentProviderRegistry registry)
   {
      this.registry = registry;
   }

   public void start() throws Exception
   {
      registeredContentType = ContentType.create(contentType);

      //
      contentProvider = createProvider();

      //
      registry.registerContentProvider(this);
   }

   protected abstract ContentProvider createProvider();

   public void stop()
   {
      if (registeredContentType != null)
      {
         registry.unregisterContentProvider(registeredContentType);
      }
   }

   /**
    * Returns the portlet instance to render view mode.
    *
    * @param rendererContext
    * @return the portlet instance for the view mode
    */
   protected abstract Instance getPortletInstance(ContentRendererContext rendererContext);

   public WindowRendition renderWindow(ContentRendererContext rendererContext)
   {
      Window window = rendererContext.getWindow();

      // Get the parent portal
      Portal portal = null;
      for (PortalObject current = window; current != null; current = current.getParent())
      {
         if (current.getType() == PortalObject.TYPE_PORTAL)
         {
            portal = (Portal)current;
            break;
         }
      }

      // Get Window properties
      Map<String, String> windowProps = new HashMap<String, String>(window.getProperties());

      try
      {
         // Check that the associated portlet is deployed
         Instance portletInstance = getPortletInstance(rendererContext);
         if (portletInstance == null)
         {
            log.debug("Portlet associated with " + rendererContext.getWindow() + " window could not be found!");
            throw new NoSuchPortletException("Portlet associated with " + rendererContext.getWindow() + " window could not be found!");
         }
         portletInstance.getPortlet();
      }
      catch (PortletInvokerException e)
      {
         ControllerResponse cr;

         if (e instanceof NoSuchPortletException)
         {
            cr = new UnavailableResourceResponse(((NoSuchPortletException)e).getPortletId(), false);
         }
         else
         {
            log.error("Portlet invoker exception during portlet window rendering", e);
            cr = new org.jboss.portal.core.controller.command.response.ErrorResponse(e, false);
         }

         return new WindowRendition(windowProps, WindowState.NORMAL, Mode.VIEW, null, null, cr);
      }

      PortletWindowNavigationalState windowNS = rendererContext.getPortletNavigationalState();

      Mode mode = windowNS.getMode();
      WindowState windowState = windowNS.getWindowState();

      // Obtain instance
      Instance instance = getPortletInstance(rendererContext);

      // No instance means we can't continue
      if (instance == null)
      {
         String ref = null;
         Content content = window.getContent();
         if (content != null)
         {
            ref = content.getURI();
         }
         if (ref == null)
         {
            ref = window.getId().toString();
         }
         UnavailableResourceResponse cr = new UnavailableResourceResponse(ref, false);
         return new WindowRendition(windowProps, windowState, mode, Collections.singletonList(windowState), Collections.singletonList(mode), cr);
      }

      // Create invocation
      RenderInvocation invocation = rendererContext.createRenderInvocation(new PortletWindowNavigationalState(windowNS.getPortletNavigationalState(), mode, windowState));

      //
      List<WindowState> supportedWindowStates = Collections.emptyList();
      List<Mode> supportedModes = Collections.emptyList();
      PortletInvocationResponse response;

      //
      try
      {
         Portlet portlet = instance.getPortlet();
         CapabilitiesInfo capabilitiesInfo = portlet.getInfo().getCapabilities();

         // Get current Media Type
         MediaType mediaType = invocation.getContext().getMarkupInfo().getMediaType();

         // Add window states for the current media type 
         Set<WindowStateInfo> windowStatesInfo = capabilitiesInfo.getWindowStates(mediaType);
         supportedWindowStates = new ArrayList<WindowState>(windowStatesInfo.size());
         for (WindowStateInfo windowStateInfo : windowStatesInfo)
         {
            WindowState tmp = windowStateInfo.getWindowState();
            if (portal.getSupportedWindowStates().contains(tmp))
            {
               supportedWindowStates.add(tmp);
            }
         }

         // Add modes specific to the current media type
         Set<ModeInfo> modesInfo = capabilitiesInfo.getModes(mediaType);
         supportedModes = new ArrayList<Mode>(modesInfo.size());
         for (ModeInfo modeInfo : modesInfo)
         {
            Mode tmp = modeInfo.getMode();
            if (portal.getSupportedModes().contains(tmp))
            {
               supportedModes.add(tmp);
            }
         }

         // Remove edit mode if the user is not logged it
         if (rendererContext.getUser() == null)
         {
            supportedModes.remove(Mode.EDIT);
         }

         //
         InstancePermission perm = new InstancePermission(instance.getId(), InstancePermission.ADMIN_ACTION);
         PortalAuthorizationManager pam = pamf.getManager();
         boolean authorized = pam.checkPermission(perm);
         if (!authorized)
         {
            // Remove the modes that we know only admin are authorized to use
            supportedModes.remove(Mode.ADMIN);
         }

         //
         response = instance.invoke(invocation);
      }
      catch (Exception e)
      {
         ControllerResponse cr;

         //
         if (e instanceof NoSuchPortletException)
         {
            cr = new UnavailableResourceResponse(((NoSuchPortletException)e).getPortletId(), false);
         }
         else
         {
            log.error("Portlet invoker exception during portlet window rendering", e);
            cr = new org.jboss.portal.core.controller.command.response.ErrorResponse(e, false);
         }

         //
         return new WindowRendition(windowProps, windowState, mode, supportedWindowStates, supportedModes, cr);
      }

      //
      ControllerResponse cr;
      if (response instanceof FragmentResponse)
      {
         FragmentResponse fragment = (FragmentResponse)response;

         //
         String windowTitle = fragment.getTitle();
         if (windowTitle == null)
         {
            windowTitle = window.getName();
         }

         List<Element> headElements = null;
         ResponseProperties properties = fragment.getProperties();
         if (properties != null)
         {
            // header handling
            MultiValuedPropertyMap<Element> headers = properties.getMarkupHeaders();
            headElements = headers.getValues(MimeResponse.MARKUP_HEAD_ELEMENT);

            // deal with partial refresh
            MultiValuedPropertyMap<String> transport = properties.getTransportHeaders();
            String partialRefreshValue = transport.getValue(AjaxInterceptor.PARTIAL_REFRESH);
            Boolean partialRefresh = Boolean.parseBoolean(partialRefreshValue);
            if (partialRefresh != null && Boolean.FALSE.equals(partialRefresh))
            {
               DynaRenderOptions options = DynaRenderOptions.getOptions(null, partialRefresh);
               options.setOptions(windowProps);
            }
         }

         // Handle minimized here
         String contentChars;
         if (WindowState.MINIMIZED == windowNS.getWindowState())
         {
            contentChars = "";
         }
         else
         {
            contentChars = fragment.getChars();
         }

         //
         cr = new MarkupResponse(windowTitle, contentChars, headElements);

      }
      else if (response instanceof ErrorResponse)
      {
         cr = new org.jboss.portal.core.controller.command.response.ErrorResponse(((ErrorResponse)response).getCause(), false);
      }
      else if (response instanceof UnavailableResponse)
      {
         cr = new UnavailableResourceResponse(instance.getId(), false);
      }
      else if (response instanceof InsufficientPrivilegesResponse)
      {
         cr = new SecurityErrorResponse(SecurityErrorResponse.NOT_AUTHORIZED, false);
      }
      else if (response instanceof InsufficientTransportGuaranteeResponse)
      {
         cr = new SecurityErrorResponse(SecurityErrorResponse.NOT_SECURE, false);
      }
      else
      {
         return null;
      }

      //
      return new WindowRendition(windowProps, windowState, mode, supportedWindowStates, supportedModes, cr);
   }

   private String getPortletName(Portlet portlet) throws PortletInvokerException
   {
      LocalizedString displayName = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME);
      // if we can't get a display name, default to portlet id...
      String name;
      if (displayName == null)
      {
         name = portlet.getContext().getId();
      }
      else
      {
         name = displayName.getDefaultString();
      }
      return "'" + name + "'";
   }
}
