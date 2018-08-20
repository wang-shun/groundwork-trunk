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

package com.groundwork.feeder.service;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.HibernateProgrammaticTxnSupport;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.rs.common.ConfiguredObjectMapper;
import org.groundwork.rs.opentsdb.OpenTSDBClient;
import org.groundwork.rs.opentsdb.OpenTSDBConfiguration;
import org.hibernate.FlushMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OpenTSDBPerfDataWriter
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class OpenTSDBPerfDataWriter implements PerfDataWriter {

    private static final Log log = LogFactory.getLog(OpenTSDBPerfDataWriter.class);

    private static final ThreadLocal<Map<String,HostHostGroupNamesCacheEntry>> hostHostGroupNamesCache =
            new ThreadLocal<Map<String,HostHostGroupNamesCacheEntry>>();
    private static final ObjectMapper mapper = new ConfiguredObjectMapper();

    private static final String MESSAGE_DELIMITER = "\t";
    private static final String THRESHOLD_DELIMITER = ";";
    private static final String VALUES_DELIMITERS = "[=" + THRESHOLD_DELIMITER + "]";
    private static final String TAG_DELIMITER = ";";
    private static final String TAGS_DELIMITERS = "[=" + TAG_DELIMITER + "]";
    private static final String VALUE = "value";
    private static final String WARNING_THRESHOLD = "thold-w";
    private static final String CRITICAL_THRESHOLD = "thold-c";
    private static final String [] VALUE_TYPES = new String[]{VALUE, WARNING_THRESHOLD, CRITICAL_THRESHOLD};

    private boolean openTSDBHostGroupsEnabled;
    private int openTSDBCacheTTL;

    public OpenTSDBPerfDataWriter() {
        this.openTSDBHostGroupsEnabled = OpenTSDBConfiguration.isOpenTSDBHostGroupsEnabled();
        this.openTSDBCacheTTL = OpenTSDBConfiguration.getOpenTSDBCacheTTL();
    }

    @Override
    public void writeMessages(List<String> messageList, String appType) {
        if ((messageList == null) || messageList.isEmpty()) {
            return;
        }

        // parse messages
        List<Message> parsedMessages = new ArrayList<Message>();
        for (String message : messageList) {
            try {
                // parse message, expecting:
                //
                // serverTime TAB serverName TAB serviceName TAB TAB label = value ; warning ; critical [ TAB [ tagName = tagValue ; ]* ]?
                //
                // serverTime is seconds since the epoch, (parsed as long to hold an unsigned
                // integer), serviceName and value are required fields, label may have been
                // defaulted from serviceName, (last 19 chars), and negative warning and
                // critical values should be ignored
                String[] messageFields = message.split(MESSAGE_DELIMITER);
                if ((messageFields.length < 5) || (messageFields.length > 6)) {
                    throw new RuntimeException("expected 5 or 6 message fields");
                }
                Message parsedMessage = new Message();
                parsedMessage.serverTime = (!messageFields[0].isEmpty() ?
                        Long.parseLong(messageFields[0].trim()) : System.currentTimeMillis());
                parsedMessage.serverName = messageFields[1].trim();
                if (openTSDBHostGroupsEnabled && !parsedMessage.serverName.isEmpty()) {
                    parsedMessage.hostGroupNames = lookupHostHostGroupNames(parsedMessage.serverName);
                }
                parsedMessage.serviceName = messageFields[2].trim();
                String [] valuesFields = messageFields[4].split(VALUES_DELIMITERS);
                if (!((valuesFields.length >= 2) && (valuesFields.length <= 4))) {
                    throw new RuntimeException("expected 2, 3, or 4 message values fields");
                }
                parsedMessage.label = valuesFields[0].trim();
                if (parsedMessage.label.equals(parsedMessage.serviceName) ||
                        ((parsedMessage.label.length() == MAX_LABEL_LENGTH) &&
                                parsedMessage.serviceName.endsWith(parsedMessage.label))) {
                    // remove defaulted serviceName label
                    parsedMessage.label = "";
                }
                parsedMessage.value = toNumber(valuesFields[1].trim(), true);
                if (parsedMessage.value == null) {
                    throw new RuntimeException("missing message value");
                }
                if (valuesFields.length >= 3) {
                    parsedMessage.warning = toNumber(valuesFields[2].trim(), false);
                }
                if (valuesFields.length == 4) {
                    parsedMessage.critical = toNumber(valuesFields[3].trim(), false);
                }
                if (messageFields.length == 6) {
                    parsedMessage.tags = new HashMap<String,String>();
                    String[] tagsFields = messageFields[5].split(TAGS_DELIMITERS);
                    for (int i = 0; (i+1 < tagsFields.length); i = i+2) {
                        parsedMessage.tags.put(tagsFields[i], tagsFields[i+1]);
                    }
                }
                parsedMessage.appType = appType;
                // validate message required fields
                if (parsedMessage.serviceName.isEmpty()) {
                    throw new RuntimeException("missing message service name");
                }
                // add to parsed messages
                parsedMessages.add(parsedMessage);
            } catch (Exception e) {
                log.error("Cannot parse message, ("+e+"): "+message, e);
                continue;
            }
        }
        if (parsedMessages.isEmpty()) {
            return;
        }

        // convert parsed messages into JSON mappable OpenTSDB data point nodes
        ArrayNode dataPointNodes = mapper.createArrayNode();
        for (Message message : parsedMessages) {
            for (String valueType : VALUE_TYPES) {
                // add required message fields to data point or skip
                ObjectNode dataPointNode = mapper.createObjectNode();
                if (message.serverTime == 0L) {
                    continue;
                }
                dataPointNode.put("timestamp", message.serverTime);
                if (message.serviceName == null) {
                    continue;
                }
                dataPointNode.put("metric", OpenTSDBClient.cleanNameKey(message.serviceName));
                if (valueType.equals(VALUE)) {
                    if (message.value instanceof Long) {
                        dataPointNode.put("value", (Long) message.value);
                    } else if (message.value instanceof BigDecimal) {
                        dataPointNode.put("value", (BigDecimal) message.value);
                    } else {
                        continue;
                    }
                } else if (valueType.equals(WARNING_THRESHOLD)) {
                    if (message.warning instanceof Long) {
                        dataPointNode.put("value", (Long) message.warning);
                    } else if (message.warning instanceof BigDecimal) {
                        dataPointNode.put("value", (BigDecimal) message.warning);
                    } else {
                        continue;
                    }
                } else if (valueType.equals(CRITICAL_THRESHOLD)) {
                    if (message.critical instanceof Long) {
                        dataPointNode.put("value", (Long) message.critical);
                    } else if (message.critical instanceof BigDecimal) {
                        dataPointNode.put("value", (BigDecimal) message.critical);
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
                ObjectNode dataPointTagsNode = mapper.createObjectNode();
                dataPointTagsNode.put("valuetype", valueType);
                dataPointNode.put("tags", dataPointTagsNode);
                // add additional message fields to data point tags
                if ((message.appType != null) && !message.appType.isEmpty()) {
                    dataPointTagsNode.put("source", message.appType);
                }
                if ((message.serverName != null) && !message.serverName.isEmpty()) {
                    dataPointTagsNode.put("hostname", OpenTSDBClient.cleanNameKey(message.serverName));
                }
                if ((message.hostGroupNames != null) && (message.hostGroupNames.length > 0)) {
                    // hostgroups is formatted as a list of slash, ('/'), separated
                    // host group names, (the ',' character is illegal in a tag value)
                    if (message.hostGroupNames.length == 1) {
                        dataPointTagsNode.put("hostgroups", OpenTSDBClient.cleanNameKeyListElement(message.hostGroupNames[0]));
                    } else {
                        StringBuilder hostGroupNames = new StringBuilder();
                        for (String hostGroupName : message.hostGroupNames) {
                            if (hostGroupNames.length() > 0) {
                                hostGroupNames.append('/');
                            }
                            hostGroupNames.append(OpenTSDBClient.cleanNameKeyListElement(hostGroupName));
                        }
                        dataPointTagsNode.put("hostgroups", hostGroupNames.toString());
                    }
                }
                if ((message.label != null) && !message.label.isEmpty()) {
                    dataPointTagsNode.put("label", OpenTSDBClient.cleanNameKey(message.label));
                }
                if (message.tags != null) {
                    for (Map.Entry<String, String> tag : message.tags.entrySet()) {
                        dataPointTagsNode.put(OpenTSDBClient.cleanNameKey(tag.getKey()), OpenTSDBClient.cleanNameKey(tag.getValue()));
                    }
                }
                // add data point to data points
                dataPointNodes.add(dataPointNode);
            }
        }

        // write parsed messages to OpenTSDB
        OpenTSDBClient.putOpenTSDBPerfData(dataPointNodes);
    }

    /**
     * Lookup host host group names.
     *
     * @param hostName host name
     * @return host group names or null
     */
    @SuppressWarnings("unchecked")
    protected String [] lookupHostHostGroupNames(final String hostName) {
        // check host host group names cache
        Map<String,HostHostGroupNamesCacheEntry> cache = hostHostGroupNamesCache.get();
        if (cache == null) {
            cache = new HashMap<String,HostHostGroupNamesCacheEntry>();
            hostHostGroupNamesCache.set(cache);
        }
        HostHostGroupNamesCacheEntry cachedHostHostGroupNamesEntry = cache.get(hostName);
        if ((cachedHostHostGroupNamesEntry != null) &&
                (System.currentTimeMillis() < cachedHostHostGroupNamesEntry.expires)) {
            return cachedHostHostGroupNamesEntry.hostHostGroupNames;
        }
        // lookup host host group names
        Set<String> hostHostGroupNames = null;
        try {
            hostHostGroupNames = (Set<String>) HibernateProgrammaticTxnSupport.executeInTxn(
                    new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                        @Override
                        public Object run() throws Exception {
                            try {
                                HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
                                Host host = hostIdentityService.getHostByIdOrHostName(hostName);
                                if ((host != null) && (host.getHostGroups() != null) && !host.getHostGroups().isEmpty()) {
                                    Set<String> hostHostGroupNames = new HashSet<String>();
                                    for (HostGroup hostGroup : (Set<HostGroup>) host.getHostGroups()) {
                                        hostHostGroupNames.add(hostGroup.getName());
                                        if ((hostGroup.getAlias() != null) && !hostGroup.getAlias().isEmpty()) {
                                            hostHostGroupNames.add(hostGroup.getAlias());
                                        }
                                    }
                                    return hostHostGroupNames;
                                }
                            } catch (Exception e) {
                                log.error("Cannot lookup host host groups for " + hostName + ": " + e, e);
                            }
                            return null;
                        }

                        @Override
                        public HibernateProgrammaticTxnSupport.RunInTxnRetry retryNotification(Object result, Exception exception) {
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETURN;
                        }
                    }, FlushMode.MANUAL, true);
        } catch (Exception e) {
            log.error("Cannot lookup host host groups for " + hostName + ": " + e, e);
        }
        // cache and return sorted host host group names
        cachedHostHostGroupNamesEntry = new HostHostGroupNamesCacheEntry(openTSDBCacheTTL);
        cachedHostHostGroupNamesEntry.hostHostGroupNames = ((hostHostGroupNames != null) ?
                hostHostGroupNames.toArray(new String[hostHostGroupNames.size()]) : null);
        Arrays.sort(cachedHostHostGroupNamesEntry.hostHostGroupNames);
        cache.put(hostName, cachedHostHostGroupNamesEntry);
        return cachedHostHostGroupNamesEntry.hostHostGroupNames;
    }

    /**
     * Class to hold parsed message. The serverTime field is a long
     * value to hold an unsigned integer, (seconds since the epoch).
     */
    private static class Message {
        public long serverTime;
        public String serverName;
        public String [] hostGroupNames;
        public String serviceName;
        public String label;
        public Number value;
        public Number warning;
        public Number critical;
        public String appType;
        public Map<String,String> tags;
    }

    /**
     * Convert message field to a number value. Strips percent sign from value.
     *
     * @param numericValue numeric String value
     * @param allowNegativeValues allow negative values or return null
     * @return number value or null
     */
    private static Number toNumber(String numericValue, boolean allowNegativeValues) {
        if (numericValue.isEmpty() || (!allowNegativeValues && numericValue.startsWith("-"))) {
            return null;
        }
        if (numericValue.endsWith("%")) {
            numericValue = numericValue.substring(0, numericValue.length()-1).trim();
        }
        return (numericValue.contains(".") ? new BigDecimal(numericValue) : new Long(numericValue));
    }

    /**
     * Class to hold expiring host host group names cache entries.
     */
    private static class HostHostGroupNamesCacheEntry {

        public long expires;
        public String [] hostHostGroupNames;

        /**
         * Create new cache entry with TTL.
         *
         * @param ttlSeconds ttl in seconds
         */
        public HostHostGroupNamesCacheEntry(int ttlSeconds) {
            this.expires = System.currentTimeMillis()+(((long)ttlSeconds)*1000L);
        }
    }
}
