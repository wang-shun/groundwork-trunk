/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.rrd;

import java.util.concurrent.Callable;
import org.groundwork.foundation.ws.model.impl.RRDGraph;

public interface RRDToolCall extends Callable<RRDGraph> {
	
	public final String RRD_TOOL_COMMAND = "rrdtool ";
	public final String RRD_TOOL_PATH = "/usr/local/groundwork/common/bin";
	
	public final String RRD_START_TOKEN_SHORT = " -s ";
	public final String RRD_START_TOKEN_LONG = " --start ";
	public final String RRD_END_TOKEN_SHORT =" -e ";
	public final String RRD_END_TOKEN_LONG =" --end ";
	public final String RRD_IMAGE_FORMAT_SHORT = " -a ";
	public final String RRD_IMAGE_FORMAT_LONG = " --imgformat";
	public final String RRD_IMAGE_FORMAT = "PNG";
	public final String RRD_IMAGE_WIDTH = " --width ";
	
	public void initialize(long startDate, long endDate);
	
}
