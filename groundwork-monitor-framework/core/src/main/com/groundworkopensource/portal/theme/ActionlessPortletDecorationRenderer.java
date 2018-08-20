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

import org.jboss.portal.theme.render.RendererContext;
import org.jboss.portal.theme.render.renderer.DecorationRendererContext;

/**
 * Portlet decoration renderer that does not include triggerable actions 
 * (such as "maximize", "minimize" or "preferences").
 * 
 * @author Paul Burry
 * @version $Revision: 2141 $
 * @since GWMON 6.0
 */
public class ActionlessPortletDecorationRenderer 
extends PortletDecorationRenderer
{
	protected void renderActions(RendererContext ctx,
			DecorationRendererContext drc) {
		// Do nothing
	}
}
