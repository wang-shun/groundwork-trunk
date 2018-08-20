/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.cloudhub.connectors.icinga2.client;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Icinga2ObjectMapper
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2ObjectMapper extends ObjectMapper {

    /**
     * Construct configured ObjectMapper.
     */
    public Icinga2ObjectMapper() {
        super();
        configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        configure(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        configure(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS, true);
        setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
