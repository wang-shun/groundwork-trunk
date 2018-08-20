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
package org.jboss.portal.core.theme;

import org.jboss.portal.common.util.MarkupInfo;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.theme.PageService;
import org.jboss.portal.theme.PortalLayout;
import org.jboss.portal.theme.PortalTheme;
import org.jboss.portal.theme.page.PageResult;
import org.jboss.portal.theme.render.RenderException;
import org.jboss.portal.theme.render.RendererContext;
import org.jboss.portal.theme.render.ThemeContext;
import org.jboss.portal.web.ServletContextDispatcher;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Should not be a controller response, but it comes from legacy design.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10542 $
 */
public class PageRendition extends ControllerResponse
{

   /** . */
   private PortalLayout layout;

   /** . */
   private PageResult pageResult;

   /** . */
   private PortalTheme theme;

   /** . */
   private PageService pageService;

   public PageRendition(
      PortalLayout layout,
      PortalTheme theme,
      PageResult markupResult,
      PageService pageService)
   {
      this.layout = layout;
      this.theme = theme;
      this.pageResult = markupResult;
      this.pageService = pageService;
   }

   /** Performs the page rendition. */
   public void render(MarkupInfo markupInfo, ServletContextDispatcher dispatcher) throws IOException, ServletException
   {
      // Compute correct content type response header
      String contentType = markupInfo.getMediaType().getValue() + "; charset=" + markupInfo.getCharset();

      // Set charset and content type on the response
      dispatcher.getResponse().setContentType(contentType);
      dispatcher.getResponse().setCharacterEncoding(markupInfo.getCharset());

      //
      ThemeContext themeContext = new ThemeContext(theme, pageService.getThemeService());

      //
      RendererContext rendererContext = layout.getRenderContext(themeContext, markupInfo, dispatcher);

      //
      try
      {
         rendererContext.render(pageResult);
      }
      catch (RenderException e)
      {
         e.printStackTrace();
      }
   }

   public PageResult getPageResult()
   {
      return pageResult;
   }
}
