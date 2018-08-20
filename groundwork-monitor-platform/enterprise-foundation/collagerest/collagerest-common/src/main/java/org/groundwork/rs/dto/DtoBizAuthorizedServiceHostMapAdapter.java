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

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DtoBizAuthorizedServiceHostMapAdapter
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DtoBizAuthorizedServiceHostMapAdapter extends XmlAdapter<DtoBizAuthorizedServiceHostMap, Map<String,List<String>>> {
    @Override
    public Map<String,List<String>> unmarshal(DtoBizAuthorizedServiceHostMap dtoMap) throws Exception {
        Map<String,List<String>> map = null;
        if (dtoMap != null) {
            map = new TreeMap<String,List<String>>();
            if (dtoMap.getServiceHosts() != null) {
                for (DtoBizAuthorizedServiceHostMapEntry entry : dtoMap.getServiceHosts()) {
                    map.put(entry.getServiceName(), entry.getHostNames());
                }
            }
        }
        return map;
    }

    @Override
    public DtoBizAuthorizedServiceHostMap marshal(Map<String,List<String>> map) throws Exception {
        DtoBizAuthorizedServiceHostMap dtoMap = null;
        if (map != null) {
            dtoMap = new DtoBizAuthorizedServiceHostMap();
            for (Map.Entry<String,List<String>> entry : map.entrySet()) {
                if (dtoMap.getServiceHosts() == null) {
                    dtoMap.setServiceHosts(new ArrayList<DtoBizAuthorizedServiceHostMapEntry>());
                }
                dtoMap.getServiceHosts().add(new DtoBizAuthorizedServiceHostMapEntry(entry.getKey(), entry.getValue()));
            }
        }
        return dtoMap;
    }
}
