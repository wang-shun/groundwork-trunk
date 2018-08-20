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

package org.groundwork.rs.common;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * ConfiguredObjectMapper
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class ConfiguredObjectMapper extends ObjectMapper {

    /**
     * Construct configured ObjectMapper.
     */
    public ConfiguredObjectMapper() {
        super();
        // basic settings
        configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getDefault());
        setSerializationConfig(getSerializationConfig().withDateFormat(dateFormat));
        // ensure JSON mapping conforms to JAXB annotations except where overridden using Jackson annotations
        AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector();
        AnnotationIntrospector introspectors = new AnnotationIntrospector.Pair(jacksonIntrospector, jaxbIntrospector);
        setDeserializationConfig(getDeserializationConfig().withAnnotationIntrospector(introspectors));
        setSerializationConfig(getSerializationConfig().withAnnotationIntrospector(introspectors));
        // define module to map abstract collection types
        SimpleModule module = new SimpleModule(getClass().getPackage().getName(), Version.unknownVersion());
        module.addAbstractTypeMapping(Collection.class, ArrayList.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        module.addAbstractTypeMapping(Set.class, HashSet.class);
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        registerModule(module);
    }
}
