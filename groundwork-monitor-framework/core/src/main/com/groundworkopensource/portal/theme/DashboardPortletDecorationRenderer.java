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
import java.util.Collection;

import org.jboss.portal.Mode;
import org.jboss.portal.theme.render.RendererContext;
import org.jboss.portal.theme.render.renderer.ActionRendererContext;
import org.jboss.portal.theme.render.renderer.DecorationRendererContext;

/**
 * Decoration renderer for dashboard portlets.
 * 
 * @author Paul Burry
 * @version $Revision$
 * @since GWMON 6.0
 */
public class DashboardPortletDecorationRenderer 
extends PortletDecorationRenderer
{
    protected void renderActions(RendererContext ctx, 
    		DecorationRendererContext drc) {
    	String editModeName = Mode.EDIT.toString();
        Collection<ActionRendererContext> modeContexts = 
        	drc.getTriggerableActions(ActionRendererContext.MODES_KEY);
        for (ActionRendererContext modeContext : modeContexts) {
        	// Only render the "edit preferences" control for now
        	if (modeContext.isEnabled() && 
        			editModeName.equals(modeContext.getName())) {
                PrintWriter out = ctx.getWriter();
                out.print("<span class=\"mode-button\" title=\"");
                out.print(editModeName);
                out.print("\"><a class=\"portlet-mode-");
                out.print(editModeName);
                out.print("\" href=\"");
                out.print(modeContext.getURL());
                out.print("\">&nbsp;</a></span>");        		
        	}
        }
    }
}
