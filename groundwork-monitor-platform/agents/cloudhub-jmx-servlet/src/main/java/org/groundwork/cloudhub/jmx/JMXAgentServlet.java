/*
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

package org.groundwork.cloudhub.jmx;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

/**
 * JMXAgentServlet - JMX agent servlet test implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class JMXAgentServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(JMXAgentServlet.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        OBJECT_MAPPER.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    private JMXAgentConfiguration jmxAgentConfiguration;
    private JMXAgent jmxAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get JSON JMX data or error response
        ObjectNode responseJsonNode = OBJECT_MAPPER.createObjectNode();
        try {
            log.info("Get all JMX data...");
            Map<String,Object> data = jmxAgent.getAllJMXData(jmxAgentConfiguration);
            TreeMap<String,Object> sortedData = new TreeMap<String,Object>(data);
            for (Map.Entry<String,Object> entry : sortedData.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    responseJsonNode.put(entry.getKey(), (Long)entry.getValue());
                } else if (entry.getValue() instanceof Double) {
                    responseJsonNode.put(entry.getKey(), (Double)entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    responseJsonNode.put(entry.getKey(), (Boolean)entry.getValue());
                } else {
                    throw new RuntimeException("Unexpected JMX data type: "+
                            ((entry.getValue() != null) ? entry.getValue().getClass().getName() : null));
                }
            }
            log.info("Retrieved "+data.size()+" JMX data");
        } catch (Exception e) {
            log.error("Unable to get all JMX data: "+e, e);
            responseJsonNode.put("error", "Unable to get all JMX data: "+e);
        }
        // send JSON response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Writer writer = response.getWriter();
        writer.write(OBJECT_MAPPER.writeValueAsString(responseJsonNode));
        writer.flush();
        writer.close();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        // initialize JMX agent configuration
        jmxAgentConfiguration = new JMXAgentConfiguration() {
            @Override
            public Integer getPort() {
                String port = config.getInitParameter("port");
                return ((port != null) ? Integer.parseInt(port) : null);
            }

            @Override
            public String getUsername() {
                return config.getInitParameter("username");
            }

            @Override
            public String getPassword() {
                return config.getInitParameter("password");
            }

            @Override
            public String getObjectNameFilter() {
                return config.getInitParameter("filter");
            }
        };
        // initialize JMX agent
        String jmxAgentClass = config.getInitParameter("jmxAgentClass");
        try {
            jmxAgent = (JMXAgent)Class.forName(jmxAgentClass).newInstance();
        } catch (Exception e) {
            log.error("Unable to create JMX agent "+jmxAgentClass+" instance: "+e, e);
            throw new ServletException("Unable to create JMX agent "+jmxAgentClass+" instance: "+e, e);
        }
        log.info("JMX agent initialized");
    }
}
