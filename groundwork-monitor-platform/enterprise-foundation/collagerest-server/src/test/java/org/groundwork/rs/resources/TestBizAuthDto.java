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

package org.groundwork.rs.resources;

import org.groundwork.rs.common.ConfiguredObjectMapper;
import org.groundwork.rs.dto.DtoBizAuthorization;
import org.groundwork.rs.dto.DtoBizAuthorizedServices;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TestBizAuthDto
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestBizAuthDto {

    @Test
    public void test1() throws Exception {
        List<String> hgs = Arrays.asList(new String[]{"hg1", "hg2"});
        List<String> sgs = Arrays.asList(new String[]{"sg1"});
        DtoBizAuthorization auth = new DtoBizAuthorization(hgs, sgs);
        ConfiguredObjectMapper mapper = new ConfiguredObjectMapper();
        String authAsString = mapper.writeValueAsString(auth);
        auth = mapper.readValue(authAsString.getBytes(), DtoBizAuthorization.class);
        JAXBContext context = JAXBContext.newInstance(DtoBizAuthorization.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(auth, out);
        authAsString = new String(out.toByteArray());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ByteArrayInputStream in = new ByteArrayInputStream(authAsString.getBytes());
        auth = (DtoBizAuthorization)unmarshaller.unmarshal(in);
        assert auth.getHostGroupNames() != null;
        assert auth.getHostGroupNames().size() == 2;
        assert auth.getHostGroupNames().contains("hg1");
        assert auth.getHostGroupNames().contains("hg2");
        assert auth.getServiceGroupNames() != null;
        assert auth.getServiceGroupNames().size() == 1;
        assert auth.getServiceGroupNames().contains("sg1");
    }

    @Test
    public void test2() throws Exception {
        DtoBizAuthorization auth = new DtoBizAuthorization();
        ConfiguredObjectMapper mapper = new ConfiguredObjectMapper();
        String authAsString = mapper.writeValueAsString(auth);
        auth = mapper.readValue(authAsString.getBytes(), DtoBizAuthorization.class);
        JAXBContext context = JAXBContext.newInstance(DtoBizAuthorization.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(auth, out);
        authAsString = new String(out.toByteArray());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ByteArrayInputStream in = new ByteArrayInputStream(authAsString.getBytes());
        auth = (DtoBizAuthorization)unmarshaller.unmarshal(in);
        assert auth.getHostGroupNames() == null;
        assert auth.getServiceGroupNames() == null;
    }

    @Test
    public void test3() throws Exception {
        List<String> hs = Arrays.asList(new String[]{"h1", "h2"});
        Map<String,List<String>> shs = new TreeMap<String,List<String>>();
        shs.put("s1", hs);
        DtoBizAuthorizedServices auth = new DtoBizAuthorizedServices(hs, shs);
        ConfiguredObjectMapper mapper = new ConfiguredObjectMapper();
        String authAsString = mapper.writeValueAsString(auth);
        auth = mapper.readValue(authAsString.getBytes(), DtoBizAuthorizedServices.class);
        JAXBContext context = JAXBContext.newInstance(DtoBizAuthorizedServices.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(auth, out);
        authAsString = new String(out.toByteArray());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ByteArrayInputStream in = new ByteArrayInputStream(authAsString.getBytes());
        auth = (DtoBizAuthorizedServices)unmarshaller.unmarshal(in);
        assert auth.getHostNames() != null;
        assert auth.getHostNames().size() == 2;
        assert auth.getHostNames().contains("h1");
        assert auth.getHostNames().contains("h2");
        assert auth.getServiceHostNames() != null;
        assert auth.getServiceHostNames().size() == 1;
        assert auth.getServiceHostNames().containsKey("s1");
        assert auth.getServiceHostNames().get("s1") != null;
        assert auth.getServiceHostNames().get("s1").size() == 2;
        assert auth.getServiceHostNames().get("s1").contains("h1");
        assert auth.getServiceHostNames().get("s1").contains("h2");
    }

    @Test
    public void test4() throws Exception {
        DtoBizAuthorizedServices auth = new DtoBizAuthorizedServices();
        ConfiguredObjectMapper mapper = new ConfiguredObjectMapper();
        String authAsString = mapper.writeValueAsString(auth);
        auth = mapper.readValue(authAsString.getBytes(), DtoBizAuthorizedServices.class);
        JAXBContext context = JAXBContext.newInstance(DtoBizAuthorizedServices.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(auth, out);
        authAsString = new String(out.toByteArray());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ByteArrayInputStream in = new ByteArrayInputStream(authAsString.getBytes());
        auth = (DtoBizAuthorizedServices)unmarshaller.unmarshal(in);
        assert auth.getHostNames() == null;
        assert auth.getServiceHostNames() == null;
    }
}
