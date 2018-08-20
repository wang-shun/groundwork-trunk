/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.theme;

import java.io.PrintWriter;

import org.jboss.portal.theme.render.AbstractObjectRenderer;
import org.jboss.portal.theme.render.RenderException;
import org.jboss.portal.theme.render.RendererContext;
import org.jboss.portal.theme.render.renderer.DecorationRenderer;
import org.jboss.portal.theme.render.renderer.DecorationRendererContext;

/**
 * Abstract base class for portlet decoration renderers.
 * 
 * @author Paul Burry
 * @version $Revision$
 * @since GWMON 6.0
 */
public abstract class PortletDecorationRenderer extends AbstractObjectRenderer
		implements DecorationRenderer {

	public void render(RendererContext rendererContext,
			DecorationRendererContext drc) throws RenderException {
        PrintWriter markup = rendererContext.getWriter();

        markup.print("<div class=\"portlet-titlebar-decoration\"></div>");
        markup.print("<span class=\"portlet-titlebar-title\">");
        markup.print(drc.getTitle());
        markup.print("</span>");    	

        markup.print("<div class=\"portlet-mode-container\">");
        renderActions(rendererContext, drc);        
        markup.print("</div>");
	}

    protected abstract void renderActions(RendererContext ctx,
            DecorationRendererContext drc);
}
