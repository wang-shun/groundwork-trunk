/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.bs.actions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.impl.ActionReturn;

public abstract class FoundationActionImpl implements FoundationAction
{
	protected Action action = null;
	protected Map<String, String> parameters = null;
	protected ActionReturn actionReturn = null;	
	
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(FoundationActionImpl.class);
	
	public boolean initialize(Action action, Map<String, String> parameters)
	{
		if (action == null)
		{
			actionReturn = new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, "Invalid null action parameter.");
			return false;
		}
		
		this.action = action;
		this.parameters = parameters;
		
		return true;
	}
}
