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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JMXAgent - abstract JMX agent implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class JMXAgent {

    private static Logger log = Logger.getLogger(JMXAgent.class);

    private final static String DEFAULT_OBJECT_NAME_FILTER = "*:*";

    protected final static String NAME_DELIMITER = ".";

    private final static Set<String> INTEGER_ATTRIBUTE_TYPES = new HashSet<String>(Arrays.asList("int",
            "java.lang.Integer", "long", "java.lang.Long"));
    private final static Set<String> FLOAT_ATTRIBUTE_TYPES = new HashSet<String>(Arrays.asList("float",
            "java.lang.Float", "double", "java.lang.Double"));
    private final static Set<String> BOOLEAN_ATTRIBUTE_TYPES = new HashSet<String>(Arrays.asList("boolean",
            "java.lang.Boolean"));
    private final static Set<String> STATS_ATTRIBUTE_TYPES = new HashSet<String>(Arrays.asList(
            "javax.management.j2ee.statistics.Stats"));
    private final static Set<String> COMPOSITE_ATTRIBUTE_TYPES = new HashSet<String>(Arrays.asList(
            "javax.management.openmbean.CompositeData"));

    private final static Set<String> IGNORE_INTEGER_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("debug"));
    private final static Set<String> IGNORE_FLOAT_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("debug"));
    private final static Set<String> IGNORE_BOOLEAN_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("debug"));

    private final static String [] GC_PLATFORM_ATTRIBUTE_NAMES = new String[]{"CollectionCount", "CollectionTime"};
    private final static String [] MEMORY_POOL_PLATFORM_ATTRIBUTE_NAMES = new String[]{"CollectionUsageThreshold",
            "CollectionUsageThresholdCount", "UsageThreshold", "UsageThresholdCount"};
    private final static String [] MEMORY_PLATFORM_ATTRIBUTE_NAMES = new String[]{"HeapMemoryUsage",
            "NonHeapMemoryUsage", "ObjectPendingFinalizationCount"};
    private final static String [] THREAD_POOL_PLATFORM_ATTRIBUTE_NAMES = new String[]{"CurrentThreadCpuTime",
            "CurrentThreadUserTime", "DaemonThreadCount", "PeakThreadCount", "ThreadCount", "ThreadCpuTime",
            "ThreadUserTime", "TotalStartedThreadCount"};

    protected final static String MEMORY_USAGE_PLATFORM_ATTRIBUTE_TYPE = "java.lang.management.MemoryUsage";
    protected final static String [] MEMORY_USAGE_PLATFORM_ATTRIBUTE_KEYS = new String[]{"Init", "Max", "Used",
            "Committed"};

    /**
     * Get all JMX data from agent.
     *
     * @param configuration agent configuration
     * @return JMX data
     */
    public Map<String,Object> getAllJMXData(JMXAgentConfiguration configuration) throws Exception {
        // read filtered management beans from JMX connection
        MBeanServerConnection jmxConnection = createMBeanServerConnection(configuration);
        String objectNameFilter = ((configuration.getObjectNameFilter() != null) ? configuration.getObjectNameFilter() :
                DEFAULT_OBJECT_NAME_FILTER);
        Set<ObjectInstance> managementBeans = jmxConnection.queryMBeans(new ObjectName(objectNameFilter), null);
        // iterate over management beans extracting JMX data from their attributes
        Map<String,Object> allJMXData = new HashMap<String,Object>();
        for (ObjectInstance managementBean : managementBeans) {
            ObjectName managementBeanName = managementBean.getObjectName();
            String jmxDataNamePrefix = jmxDataNamePrefix(managementBeanName);
            MBeanAttributeInfo [] managementBeanAttrInfoArray =
                    jmxConnection.getMBeanInfo(managementBeanName).getAttributes();
            // iterate over management bean attributes
            for (MBeanAttributeInfo managementBeanAttrInfo : managementBeanAttrInfoArray) {
                // extract JMX data from management bean attributes
                if (addIntegerAttribute(jmxConnection, managementBeanName, managementBeanAttrInfo, jmxDataNamePrefix,
                        allJMXData)) {
                    continue;
                }
                if (addFloatAttribute(jmxConnection, managementBeanName, managementBeanAttrInfo, jmxDataNamePrefix,
                        allJMXData)) {
                    continue;
                }
                if (addBooleanAttribute(jmxConnection, managementBeanName, managementBeanAttrInfo, jmxDataNamePrefix,
                        allJMXData)) {
                    continue;
                }
                if (addStatsAttribute(jmxConnection, managementBeanName, managementBeanAttrInfo, jmxDataNamePrefix,
                        allJMXData)) {
                    continue;
                }
                if (addCompositeAttribute(jmxConnection, managementBeanName, managementBeanAttrInfo, jmxDataNamePrefix,
                        allJMXData)) {
                    continue;
                }
            }
        }
        // merge platform management bean JMX data
        if (enableMergePlatformMBeans()) {
            // platform GC
            addPlatformMBeans(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, GC_PLATFORM_ATTRIBUTE_NAMES,
                    "jvm.gc", allJMXData);
            // platform Thread Pool
            addPlatformMBeans(ManagementFactory.THREAD_MXBEAN_NAME, THREAD_POOL_PLATFORM_ATTRIBUTE_NAMES,
                    "jvm.threadPool", allJMXData);
            // platform Memory Pool
            addPlatformMBeans(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE, MEMORY_POOL_PLATFORM_ATTRIBUTE_NAMES,
                    "jvm.memoryPool", allJMXData);
        }
        return allJMXData;
    }

    /**
     * Create JMX agent management bean server connection.
     *
     * @param configuration agent configuration
     * @return JMX management bean server connection
     * @throws Exception
     */
    protected abstract MBeanServerConnection createMBeanServerConnection(JMXAgentConfiguration configuration)
            throws Exception;

    /**
     * Derive JMX data name prefix used for data keys.
     *
     * @param managementBeanName management bean name
     * @return JMX data name prefix
     */
    protected String jmxDataNamePrefix(ObjectName managementBeanName) {
        // compute prefix from management bean name properties
        StringBuilder prefix = new StringBuilder(managementBeanName.getDomain());
        String serviceProperty = managementBeanName.getKeyProperty("service");
        if (serviceProperty != null) {
            prefix.append(NAME_DELIMITER);
            prefix.append(stripNameSpecialChars(serviceProperty));
        }
        String j2eeTypeProperty = managementBeanName.getKeyProperty("j2eeType");
        if (j2eeTypeProperty != null) {
            prefix.append(NAME_DELIMITER);
            prefix.append(stripNameSpecialChars(j2eeTypeProperty));
        }
        String typeProperty = managementBeanName.getKeyProperty("type");
        if (typeProperty != null) {
            prefix.append(NAME_DELIMITER);
            prefix.append(stripNameSpecialChars(typeProperty));
        }
        String pathProperty = managementBeanName.getKeyProperty("path");
        if (pathProperty != null) {
            prefix.append(NAME_DELIMITER);
            prefix.append(stripNameSpecialChars(pathProperty));
        }
        String nameProperty = managementBeanName.getKeyProperty("name");
        if (nameProperty != null) {
            prefix.append(NAME_DELIMITER);
            prefix.append(stripNameSpecialChars(nameProperty));
        }
        return prefix.toString();
    }

    /**
     * Add integer management bean attribute to JMX data. Returns true if attribute
     * is an integer or long type, whether or not it has been added.
     *
     * @param jmxConnection JMX connection
     * @param managementBeanName management bean name
     * @param managementBeanAttrInfo management bean attribute info
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     * @return integer attribute
     */
    private boolean addIntegerAttribute(MBeanServerConnection jmxConnection, ObjectName managementBeanName,
                                        MBeanAttributeInfo managementBeanAttrInfo, String jmxDataNamePrefix,
                                        Map<String,Object> jmxData) {
        // validate management bean attribute type
        if (!INTEGER_ATTRIBUTE_TYPES.contains(managementBeanAttrInfo.getType())) {
            return false;
        }
        // validate management bean attribute name
        if ((managementBeanAttrInfo.getName() != null) && managementBeanAttrInfo.isReadable() &&
                !ignoreIntegerAttributeNames().contains(managementBeanAttrInfo.getName())) {
            try {
                // get JMX data from management bean attribute
                Object managementBeanAttrValue = jmxConnection.getAttribute(managementBeanName,
                        managementBeanAttrInfo.getName());
                long jmxDataValue = -1L;
                if (managementBeanAttrValue instanceof Number) {
                    jmxDataValue = ((Number)managementBeanAttrValue).longValue();
                }
                String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                        stripNameSpecialChars(managementBeanAttrInfo.getName());
                jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
            } catch (Exception e) {
                if (!(getThrowableCause(e) instanceof UnsupportedOperationException)) {
                    log.error("Error reading integer attribute from " + managementBeanAttrInfo.getName() + ": " + e, e);
                }
            }
        }
        return true;
    }

    /**
     * Return set of integer management bean attribute names to ignore.
     *
     * @return set of attribute names to ignore
     */
    protected Set<String> ignoreIntegerAttributeNames() {
        return IGNORE_INTEGER_ATTRIBUTE_NAMES;
    }

    /**
     * Add float management bean attribute to JMX data. Returns true if attribute
     * is a float or double type, whether or not it has been added.
     *
     * @param jmxConnection JMX connection
     * @param managementBeanName management bean name
     * @param managementBeanAttrInfo management bean attribute info
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     * @return float attribute
     */
    private boolean addFloatAttribute(MBeanServerConnection jmxConnection, ObjectName managementBeanName,
                                      MBeanAttributeInfo managementBeanAttrInfo, String jmxDataNamePrefix,
                                      Map<String,Object> jmxData) {
        // validate management bean attribute type
        if (!FLOAT_ATTRIBUTE_TYPES.contains(managementBeanAttrInfo.getType())) {
            return false;
        }
        // validate management bean attribute name
        if ((managementBeanAttrInfo.getName() != null) && managementBeanAttrInfo.isReadable() &&
                !ignoreIntegerAttributeNames().contains(managementBeanAttrInfo.getName())) {
            try {
                // get JMX data from management bean attribute
                Object managementBeanAttrValue = jmxConnection.getAttribute(managementBeanName,
                        managementBeanAttrInfo.getName());
                double jmxDataValue = -1.0;
                if (managementBeanAttrValue instanceof Number) {
                    jmxDataValue = ((Number)managementBeanAttrValue).doubleValue();
                }
                String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                        stripNameSpecialChars(managementBeanAttrInfo.getName());
                jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
            } catch (Exception e) {
                if (!(getThrowableCause(e) instanceof UnsupportedOperationException)) {
                    log.error("Error reading float attribute from " + managementBeanAttrInfo.getName() + ": " + e, e);
                }
            }
        }
        return true;
    }

    /**
     * Return set of float management bean attribute names to ignore.
     *
     * @return set of attribute names to ignore
     */
    protected Set<String> ignoreFloatAttributeNames() {
        return IGNORE_FLOAT_ATTRIBUTE_NAMES;
    }

    /**
     * Add boolean management bean attribute to JMX data. Returns true if attribute
     * is a boolean type, whether or not it has been added.
     *
     * @param jmxConnection JMX connection
     * @param managementBeanName management bean name
     * @param managementBeanAttrInfo management bean attribute info
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     * @return boolean attribute
     */
    private boolean addBooleanAttribute(MBeanServerConnection jmxConnection, ObjectName managementBeanName,
                                        MBeanAttributeInfo managementBeanAttrInfo, String jmxDataNamePrefix,
                                        Map<String,Object> jmxData) {
        // validate management bean attribute type
        if (!BOOLEAN_ATTRIBUTE_TYPES.contains(managementBeanAttrInfo.getType())) {
            return false;
        }
        // validate management bean attribute name
        if ((managementBeanAttrInfo.getName() != null) && managementBeanAttrInfo.isReadable() &&
                !ignoreBooleanAttributeNames().contains(managementBeanAttrInfo.getName())) {
            try {
                // get JMX data from management bean attribute
                Object managementBeanAttrValue = jmxConnection.getAttribute(managementBeanName,
                        managementBeanAttrInfo.getName());
                boolean jmxDataValue = false;
                if (managementBeanAttrValue instanceof Boolean) {
                    jmxDataValue = ((Boolean)managementBeanAttrValue).booleanValue();
                }
                String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                        stripNameSpecialChars(managementBeanAttrInfo.getName());
                jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
            } catch (Exception e) {
                if (!(getThrowableCause(e) instanceof UnsupportedOperationException)) {
                    log.error("Error reading boolean attribute from " + managementBeanAttrInfo.getName() + ": " + e, e);
                }
            }
        }
        return true;
    }

    /**
     * Return set of boolean management bean attribute names to ignore.
     *
     * @return set of attribute names to ignore
     */
    protected Set<String> ignoreBooleanAttributeNames() {
        return IGNORE_BOOLEAN_ATTRIBUTE_NAMES;
    }

    /**
     * Add stats management bean attribute to JMX data. Returns true if attribute
     * is a stats type, whether or not it has been added.
     *
     * @param jmxConnection JMX connection
     * @param managementBeanName management bean name
     * @param managementBeanAttrInfo management bean attribute info
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     * @return stats attribute
     */
    private boolean addStatsAttribute(MBeanServerConnection jmxConnection, ObjectName managementBeanName,
                                      MBeanAttributeInfo managementBeanAttrInfo, String jmxDataNamePrefix,
                                      Map<String,Object> jmxData) {
        // validate management bean attribute type
        if (!STATS_ATTRIBUTE_TYPES.contains(managementBeanAttrInfo.getType())) {
            return false;
        }
        // validate management bean attribute name
        if ((managementBeanAttrInfo.getName() != null) && managementBeanAttrInfo.isReadable()) {
            try {
                // get JMX data from management bean attribute
                Object managementBeanAttrValue = jmxConnection.getAttribute(managementBeanName,
                        managementBeanAttrInfo.getName());
                if (managementBeanAttrValue instanceof Stats) {
                    jmxDataNamePrefix = jmxDataNamePrefix + NAME_DELIMITER +
                            stripNameSpecialChars(managementBeanAttrInfo.getName());
                    // get JMX data for individual statistics in statistics
                    for (Statistic statistic : ((Stats)managementBeanAttrValue).getStatistics()) {
                        long jmxDataValue;
                        if (statistic instanceof CountStatistic) {
                            jmxDataValue = ((CountStatistic) statistic).getCount();
                        } else if (statistic instanceof TimeStatistic) {
                            jmxDataValue = ((TimeStatistic) statistic).getCount();
                        } else if (statistic instanceof RangeStatistic) {
                            jmxDataValue = ((RangeStatistic) statistic).getCurrent();
                        } else if (statistic instanceof BoundedRangeStatistic) {
                            jmxDataValue = ((BoundedRangeStatistic) statistic).getCurrent();
                        } else {
                            continue;
                        }
                        String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                                stripNameSpecialChars(statistic.getName());
                        jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
                    }
                }
            } catch (Exception e) {
                if (!(getThrowableCause(e) instanceof UnsupportedOperationException)) {
                    log.error("Error reading stats attribute from " + managementBeanAttrInfo.getName() + ": " + e, e);
                }
            }
        }
        return true;
    }

    /**
     * Add composite management bean attribute to JMX data. Returns true if attribute
     * is a composite type, whether or not it has been added.
     *
     * @param jmxConnection JMX connection
     * @param managementBeanName management bean name
     * @param managementBeanAttrInfo management bean attribute info
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     * @return composite attribute
     */
    private boolean addCompositeAttribute(MBeanServerConnection jmxConnection, ObjectName managementBeanName,
                                          MBeanAttributeInfo managementBeanAttrInfo, String jmxDataNamePrefix,
                                          Map<String,Object> jmxData) {
        // validate management bean attribute type
        if (!COMPOSITE_ATTRIBUTE_TYPES.contains(managementBeanAttrInfo.getType())) {
            return false;
        }
        // validate management bean attribute name
        if ((managementBeanAttrInfo.getName() != null) && managementBeanAttrInfo.isReadable()) {
            try {
                // get JMX data from management bean attribute
                Object managementBeanAttrValue = jmxConnection.getAttribute(managementBeanName,
                        managementBeanAttrInfo.getName());
                if (managementBeanAttrValue instanceof CompositeData) {
                    CompositeData composite = (CompositeData)managementBeanAttrValue;
                    // filter composites by type name
                    Set<String> includeCompositeTypeNames = includeCompositeAttributeTypeNames();
                    String compositeTypeName = composite.getCompositeType().getTypeName();
                    if ((includeCompositeTypeNames == null) || includeCompositeTypeNames.contains(compositeTypeName)) {
                        // get JMX data for composite keys in composite
                        Map<String,Set<String>> includeCompositeTypeKeys = includeCompositeAttributeTypeKeys();
                        jmxDataNamePrefix = jmxDataNamePrefix + NAME_DELIMITER +
                                stripNameSpecialChars(managementBeanAttrInfo.getName());
                        for (String compositeKey : composite.getCompositeType().keySet()) {
                            // filter composite keys by name for composite type name
                            if ((includeCompositeTypeKeys == null) ||
                                    !includeCompositeTypeKeys.containsKey(compositeTypeName) ||
                                    includeCompositeTypeKeys.get(compositeTypeName).contains(compositeKey)) {
                                // get JMX data for numeric composite key values
                                Object compositeKeyValue = composite.get(compositeKey);
                                if (compositeKeyValue instanceof Number) {
                                    long jmxDataValue = ((Number) compositeKeyValue).longValue();
                                    String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                                            stripNameSpecialChars(compositeKey);
                                    jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (!(getThrowableCause(e) instanceof UnsupportedOperationException)) {
                    log.error("Error reading composite attribute from " + managementBeanAttrInfo.getName() + ": " + e, e);
                }
            }
        }
        return true;
    }


    /**
     * Return set of composite management bean attribute type names to include.
     *
     * @return set of attribute type names to include or null for all
     */
    protected Set<String> includeCompositeAttributeTypeNames() {
        return null;
    }

    /**
     * Return map of composite management bean attribute type names keys to include.
     *
     * @return set of attribute type names keys to include or null for all
     */
    protected Map<String,Set<String>> includeCompositeAttributeTypeKeys() {
        return null;
    }

    /**
     * Enable merge platform in addition to JMX agent management beans
     * when the local management bean server does not provide them.
     *
     * @return merge flag
     */
    protected boolean enableMergePlatformMBeans() {
        return false;
    }

    /**
     * Add platform management bean attributes to JMX data.
     *
     * @param managementBeanNameFilter platform bean name filter
     * @param managementBeanAttrNames platform bean attribute names
     * @param jmxDataNamePrefix JMX data name prefix
     * @param jmxData returned JMX data
     */
    private void addPlatformMBeans(String managementBeanNameFilter, String [] managementBeanAttrNames,
                                   String jmxDataNamePrefix, Map<String,Object> jmxData) {
        // get platform management beans
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectNameFilter = new ObjectName(managementBeanNameFilter + ",*");
            Set<ObjectName> managementBeanNames = server.queryNames(objectNameFilter, null);
            for (ObjectName managementBeanName : managementBeanNames) {
                // add platform management bean attributes as JMX data
                String nameKeyProperty = managementBeanName.getKeyProperty("name");
                if (nameKeyProperty != null) {
                    jmxDataNamePrefix = jmxDataNamePrefix + NAME_DELIMITER + stripNameSpecialChars(nameKeyProperty);
                }
                AttributeList managementBeanAttrs = server.getAttributes(managementBeanName, managementBeanAttrNames);
                for (Object managementBeanAttrElement : managementBeanAttrs) {
                    Attribute managementBeanAttr = (Attribute)managementBeanAttrElement;
                    String jmxDataName = jmxDataNamePrefix + NAME_DELIMITER +
                            stripNameSpecialChars(managementBeanAttr.getName());
                    Object jmxDataValue = managementBeanAttr.getValue();
                    if (jmxDataValue instanceof Long) {
                        // add integer platform management bean attribute as JMX data
                        jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
                    } else if (jmxDataValue instanceof Integer) {
                        // add integer platform management bean attribute as JMX data
                        jmxDataValue = ((Integer)jmxDataValue).longValue();
                        jmxData.put(jmxDataName.toLowerCase(), jmxDataValue);
                    } else if (jmxDataValue instanceof CompositeData) {
                        // add composite platform management bean attribute integer keys as JMX data
                        CompositeData composite = (CompositeData)jmxDataValue;
                        if (composite.getCompositeType().getTypeName().equals(MEMORY_USAGE_PLATFORM_ATTRIBUTE_TYPE)) {
                            for (String compositeKey : MEMORY_USAGE_PLATFORM_ATTRIBUTE_KEYS) {
                                Object jmxDataKeyValue = composite.get(compositeKey);
                                if (jmxDataKeyValue instanceof Long) {
                                    // add integer composite platform management bean attribute key as JMX data
                                    String jmxDataKeyName = jmxDataName + NAME_DELIMITER +
                                            stripNameSpecialChars(compositeKey);
                                    jmxData.put(jmxDataKeyName.toLowerCase(), jmxDataKeyValue);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading platform attributes from "+managementBeanNameFilter+": "+e, e);
        }
    }

    /**
     * Strip special characters from name components. Also converts
     * whitespace to underscores.
     *
     * @param name name component string
     * @return stripped name component string
     */
    protected static String stripNameSpecialChars(String name) {
        return name.replaceAll("[^0-9A-Za-z._]", "").replaceAll("\\s+", "_");
    }

    /**
     * Return underlying cause of throwable/exception.
     *
     * @param t throwable/exception
     * @return throwable/exception cause
     */
    protected static Throwable getThrowableCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
