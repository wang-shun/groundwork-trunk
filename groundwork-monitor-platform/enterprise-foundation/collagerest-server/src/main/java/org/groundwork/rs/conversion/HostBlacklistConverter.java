/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.conversion;

import com.groundwork.collage.model.HostBlacklist;
import org.groundwork.rs.dto.DtoHostBlacklist;

/**
 * HostBlacklistConverter
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostBlacklistConverter {

    /**
     * Convert HostBlacklist model to DtoHostBlacklist instance.
     *
     * @param hostBlacklist HostBlacklist model instance
     * @return new DtoHostBlacklist instance
     */
    public final static DtoHostBlacklist convert(HostBlacklist hostBlacklist) {
        return new DtoHostBlacklist(
                hostBlacklist.getHostBlacklistId(),
                hostBlacklist.getHostName());
    }
}
