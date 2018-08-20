/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.statusviewer.common;

import org.apache.xerces.parsers.SAXParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains JMS push related methods used by various portlets. This
 * class mainly consists of methods for parsing various JMS Push XMLs like: <br>
 * 
 * 1) Normal Entity: <ENTITY TYPE="SERVICESTATUS" TEXT="UPDATE:11;UPDATE:9;" />
 * 
 * 2) Aggregate: <AGGREGATE><ENTITY TYPE="HOST" TEXT="UPDATE:2;" /><ENTITY
 * TYPE="HOSTGROUP" TEXT="UPDATE:1;" /></AGGREGATE>
 * 
 * 3) Tree View: <TREEVIEWUPDATES><ENTITY TYPE=\"HOSTGROUP\"
 * TEXT=\"UPDATE:2;UPDATE:3;UPDATE:4;UPDATE:10;\"/><ENTITY TYPE=\"SERVICEGROUP\"
 * TEXT=\"UPDATE:5;UPDATE:7;UPDATE:1;UPDATE:9;\"/></TREEVIEWUPDATES>
 * 
 * @author shivangi_walvekar
 * 
 */
public class JMSUtils {

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected JMSUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * For testing - Do Not Delete
     * 
     * @param args
     */
    public static void main(String[] args) {
        String xml = "<TREEVIEWUPDATES><ENTITY TYPE=\"HOSTGROUP\" TEXT=\"UPDATE:2;UPDATE:3;UPDATE:4;UPDATE:10;\"/><ENTITY TYPE=\"SERVICEGROUP\" TEXT=\"UPDATE:5;UPDATE:7;UPDATE:1;UPDATE:9;\"/></TREEVIEWUPDATES>";
        // "<ENTITY TYPE=\"SERVICESTATUS\" TEXT=\"UPDATE:11;UPDATE:9;\" />";
        // "<AGGREGATE><ENTITY TYPE=\"HOST\" TEXT=\"UPDATE:2;UPDATE:3;UPDATE:4;UPDATE:10;\"/><ENTITY TYPE=\"HOSTGROUP\" TEXT=\"UPDATE:1;\" /></AGGREGATE>";

        // Map<NodeType, List<JMSUpdate>> treeViewJMSUpdates =
        // getTreeViewJMSUpdates(xml);
        List<JMSUpdate> updatesListFromXML = getJMSUpdatesListFromXML(xml,
                NodeType.HOST_GROUP);
        for (JMSUpdate update : updatesListFromXML) {
            System.out.println(update);
        }
    }

    /**
     * This method retrieves the JMS updates from the input xml. It parses the
     * xml and returns the list of JMSUpdate objects. It captures update for
     * HOST,HOSTGROUP,SERVICE,SERVICEGROUP.
     * 
     * @param xml
     * @return List of JMSUpdate
     */
    public static List<JMSUpdate> getJMSUpdatesListFromXML(String xml) {
        return JMSUtils.getJMSUpdatesListFromXML(xml, null);
    }

    /**
     * This method retrieves the JMS updates from the input xml. It parses the
     * xml and returns the list of JMSUpdate objects. It captures updates for
     * the nodeType passed as an input parameter and does not return all the jms
     * updates.
     * 
     * @param xml
     * @param nodeType
     * @return List of JMSUpdate
     */
    public static List<JMSUpdate> getJMSUpdatesListFromXML(String xml,
            NodeType nodeType) {
        List<JMSUpdate> jmsUpdates = new ArrayList<JMSUpdate>();
        if (xml != null && !Constant.EMPTY_STRING.equalsIgnoreCase(xml)) {
            SAXMessageHandler handler = new SAXMessageHandler();
            SAXParser parser = new SAXParser();
            try {
                parser.setContentHandler(handler);
                parser.parse(new org.xml.sax.InputSource(
                        new java.io.StringReader(xml)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get all the JMS updates from SAX parser
            List<JMSUpdate> updatesAfterParsing = handler.getJmsUpdates();
            // if nodeType is null, return all updates
            if (nodeType == null) {
                return updatesAfterParsing;
            }

            // if nodeType is specified, retrieve specific node type updates
            for (JMSUpdate update : updatesAfterParsing) {
                if (update.getNodeType().equals(nodeType)) {
                    jmsUpdates.add(update);
                }
            }
        }
        return jmsUpdates;
    }

    /**
     * This method retrieves the JMS updates from the input xml. It parses the
     * xml and returns the list of JMSUpdate objects in Map as per the NodeType.
     * This will allow users to selectively process JMS updates as per the
     * sequence logic demands.
     * 
     * @param xml
     * @return List of JMSUpdate objects in Map as per the NodeType.
     */
    public static Map<NodeType, List<JMSUpdate>> getJMSUpdatesMapFromXML(
            String xml) {
        Map<NodeType, List<JMSUpdate>> jmsUpdatesMap = new HashMap<NodeType, List<JMSUpdate>>();

        if (xml != null && !Constant.EMPTY_STRING.equalsIgnoreCase(xml)) {
            SAXMessageHandler handler = new SAXMessageHandler();
            SAXParser parser = new SAXParser();
            try {
                parser.setContentHandler(handler);
                parser.parse(new org.xml.sax.InputSource(
                        new java.io.StringReader(xml)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get all the JMS updates from SAX parser
            List<JMSUpdate> updatesAfterParsing = handler.getJmsUpdates();

            List<JMSUpdate> hostUpdates = new ArrayList<JMSUpdate>();
            List<JMSUpdate> hostGroupUpdates = new ArrayList<JMSUpdate>();
            List<JMSUpdate> serviceUpdates = new ArrayList<JMSUpdate>();
            List<JMSUpdate> serviceGroupUpdates = new ArrayList<JMSUpdate>();
            List<JMSUpdate> customGroupUpdates = new ArrayList<JMSUpdate>();

            // if nodeType is specified, retrieve specific node type updates
            for (JMSUpdate update : updatesAfterParsing) {
                switch (update.getNodeType()) {
                    case HOST:
                        hostUpdates.add(update);
                        break;
                    case HOST_GROUP:
                        hostGroupUpdates.add(update);
                        break;
                    case SERVICE:
                        serviceUpdates.add(update);
                        break;
                    case SERVICE_GROUP:
                        serviceGroupUpdates.add(update);
                        break;
                    case CUSTOM_GROUP:
                        customGroupUpdates.add(update);
                        break;
                    default:
                        // Do Nothing
                        break;
                }
            }

            // add all lists to map
            jmsUpdatesMap.put(NodeType.HOST, hostUpdates);
            jmsUpdatesMap.put(NodeType.HOST_GROUP, hostGroupUpdates);
            jmsUpdatesMap.put(NodeType.SERVICE, serviceUpdates);
            jmsUpdatesMap.put(NodeType.SERVICE_GROUP, serviceGroupUpdates);
            jmsUpdatesMap.put(NodeType.CUSTOM_GROUP, customGroupUpdates);
        }
        return jmsUpdatesMap;
    }

}
