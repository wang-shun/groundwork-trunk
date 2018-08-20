/*
 * StatusViewer - The ultimate gwportal framework. Copyright (C) 2004-2009
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.groundworkopensource.portal.statusviewer.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author swapnil_gujrathi
 * 
 */
public class SAXMessageHandler extends DefaultHandler {

    /**
     * Logger.
     */
    private static Logger logger = Logger.getLogger(SAXMessageHandler.class
            .getName());

    /**
     * List of JMSUpdates
     */
    private List<JMSUpdate> jmsUpdates = new ArrayList<JMSUpdate>();

    /**
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) {
        if (localName.equals("ENTITY")) {
            String type = atts.getValue("", "TYPE");
            NodeType nodeType = null;
            if (Constant.JMS_HOSTGROUP.equals(type)) {
                nodeType = NodeType.HOST_GROUP;
            } else if (Constant.JMS_HOST.equals(type)) {
                nodeType = NodeType.HOST;
            } else if (Constant.JMS_SERVICEGROUP.equals(type)) {
                nodeType = NodeType.SERVICE_GROUP;
            } else if (Constant.JMS_SERVICESTATUS.equals(type)) {
                nodeType = NodeType.SERVICE;
            } else {
                logger.debug(Constant.METHOD
                        + "getJMSUpdatesListFromXML(String xml) : "
                        + "Unknown node type.");
            }

            String text = atts.getValue("", "TEXT");
            StringTokenizer tokenizer = new StringTokenizer(text,
                    Constant.SEMICOLON);

            while (tokenizer.hasMoreTokens()) {
                String nextElement = tokenizer.nextToken();
                // Tokenize the nextElement based on colon (:)
                StringTokenizer tokenizerForColon = new StringTokenizer(
                        nextElement, Constant.COLON);
                int countTokens = tokenizerForColon.countTokens();
                if (countTokens == 2) {
                    // Get the action. UPDATE/ADD etc.
                    String action = tokenizerForColon.nextToken();
                    // Get the nodeId for the action.
                    String idString = tokenizerForColon.nextToken();
                    jmsUpdates.add(new JMSUpdate(action, Integer
                            .parseInt(idString), nodeType));
                } // end if
            } // end while
        } // end if
    }

    /**
     * @return jmsUpdates
     */
    public List<JMSUpdate> getJmsUpdates() {
        return jmsUpdates;
    }
}
