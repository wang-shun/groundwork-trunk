/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.performancedata;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.LogPerformanceData;
import com.groundwork.collage.model.PerformanceDataLabel;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.DateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author rogerrut
 *         <p>
 *         Created: Jan 23, 2007
 */
public class PerformanceDataServiceImpl extends EntityBusinessServiceImpl implements PerformanceDataService {
    /**
     * Enable Logging *
     */
    protected static Log log = LogFactory.getLog(PerformanceDataServiceImpl.class);

    /* Log mesages */
    private static final String ERROR_CREATE_OBJECT = "Unable to create a LogPerformanceData object";
    private static final String ERROR_SERVICE_LOOKUP_FAILED1 = "1. Can't find Service Status for ServiceDescription [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED2 = "2. Can't find Service Status for ServiceDescription [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED3 = "3. Can't find Service Status for ServiceDescription [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED4 = "4. Can't find Service Status for ServiceDescription [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED5 = "5. Can't find Service Status for ServiceDescription [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED_2 = "] on Host [";
    private static final String ERROR_SERVICE_LOOKUP_FAILED_3 = " because ServiceStatus Entry doesn't exist";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED1 = "1. Can't find PerformanceLabelId for PerformanceLabel x [";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED2 = "2. Can't find PerformanceLabelId for PerformanceLabel x0 [";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED3 = "3. Can't find PerformanceLabelId for PerformanceLabel x1 [";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED4 = "4. Can't find PerformanceLabelId for PerformanceLabel x2 [";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED5 = "5. Can't find PerformanceLabelId for PerformanceLabel x3 [";
    private static final String ERROR_PERFORMANCELABEL_CREATE = "Insertion failed for PerformanceLabel x4 [";
    private static final String ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2 = "]";
    private static final String ERROR_THROWS_EXCEPTION = "] Systems throws exception: ";
    private static final String ERROR_LABEL_COMMENT = "] and Label [";
    private static final String ERROR_BRACKET_CLOSE = "]";
    private static final String ERROR_REMOVE_OBJECTS = "deletePerformanceData() method error:";
    private static final String ERROR_INVALID_START_DATE = "Provided Dates are invalid start Date [";
    private static final String ERROR_INVALID_END_DATE = "] End Date [";
    private static final String ERROR_INVALID_DATE_FORMAT = "]. Strings should be in Format: yyyy-MM-dd HH:mm:ss";
    private static final String ERROR_LOG_PERFORMANCE_DELETE = "Delete entries from LogPerformance table failed with error ";

    private static final String WARNING_NOTHING_TO_DELETE = "No records in specified date range.";

    protected Cache<String, PerformanceDataLabel> labelCache = CacheBuilder.newBuilder().build();
    protected Cache<String, LogPerformanceData> perfCache = CacheBuilder.newBuilder().build();
    private boolean isCacheEnabled = false;
    protected static final String KEY_DELIMITER = "::";
    protected Properties foundationProperties;

    private static final MetricRegistry registry = new MetricRegistry();
    private static final Timer perfTimer = registry.timer("perfTimer");

    static {
        JmxReporter.forRegistry(registry).build().start();
    }

    public Properties getFoundationProperties() {
        return foundationProperties;
    }

    public void setFoundationProperties(Properties foundationProperties) {
        this.foundationProperties = foundationProperties;
    }

    public PerformanceDataServiceImpl(FoundationDAO foundationDAO) {
        super(foundationDAO, LogPerformanceData.INTERFACE_NAME, LogPerformanceData.COMPONENT_NAME);
    }

    public void createOrUpdatePerformanceData(String hostName,
                                              String serviceDescription, String performanceDataLabel,
                                              double performanceValue, String checkDate)
            throws BusinessServiceException {
        if (log.isDebugEnabled()) log.debug("createOrUpdatePerformanceData1");

        createOrUpdatePerformanceData(hostName, serviceDescription, performanceDataLabel, performanceValue, checkDate, "day");
    }


    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#createPerformanceData(java.lang.String,
     *      java.lang.String, java.lang.String, double, java.lang.String)
     */
    public void createOrUpdatePerformanceData(String hostName,
                                              String serviceDescription, String performanceName,    // was
                                              // performanceDataLabel
                                              double performanceValue, String checkDate, String rollup)
            throws BusinessServiceException {
        Timer.Context timer = perfTimer.time();
        if (log.isDebugEnabled()) log.debug("createOrUpdatePerformanceData-2");
        Integer performanceDataLabelId = null;
        PerformanceDataLabel label = null;
        try {
            label = getPerformanceDataLabel(performanceName);
            if (label != null) {
                performanceDataLabelId = label.getPerformanceDataLabelId();
            }
            else {
                performanceDataLabelId = new Integer(-1);
            }
        } catch (BusinessServiceException bse) {
            log.error("Lookup/creation of PerformanceDataLabel entry failed. Error: " + bse);
            performanceDataLabelId = new Integer(-1);
        }

        if (performanceDataLabelId.intValue() > -1) {
            try {
                LogPerformanceData obj = null;
                Date incomingCheckDate = DateTime.parse(checkDate);
                boolean bNewEntry = false;

                if (isCacheEnabled) {
                    String key = makeKey(hostName, serviceDescription, performanceName);
                    LogPerformanceData cached = perfCache.getIfPresent(key);
                    if (cached != null) {
                        Session session = this.getSession();
                        obj = cached;
                        if (!session.contains(obj)) {
                            session.update(obj);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Missed perf cache lookup on key: " + key);
                    }
                }
                if (obj == null) {
                    obj = this.createPerformanceData();
                    if (obj == null)
                        throw new BusinessServiceException(ERROR_CREATE_OBJECT);
                    /*
					 * Query if an entry exists
					 */
                    SortCriteria sort = SortCriteria.desc("lastCheckTime");
                    FilterCriteria filter = FilterCriteria.eq("serviceStatus.serviceDescription", serviceDescription);
                    filter.and(FilterCriteria.eq("serviceStatus.host.hostName", hostName));
                    filter.and(FilterCriteria.eq("performanceDataLabel.performanceName", performanceName));
                    filter.and(FilterCriteria.eq("performanceDataLabel.performanceDataLabelId", performanceDataLabelId));
                    FoundationQueryList performanceData = this.getPerformanceData(filter, sort, 0, 1);
                    // If list is entry data doesn't exist -- create new entry
                    if ((performanceData == null) || (performanceData != null && performanceData.size() == 0)) {
                        obj = this.createPerformanceData();
                        bNewEntry = true;
                    } else {
                        obj = (LogPerformanceData) performanceData.get(0);
                    }
                }
                // If entry exist and is not for the same day create new entry
                if (!bNewEntry) {
                    if (obj == null) {
                        obj = this.createPerformanceData();
                        bNewEntry = true;
                    } else {
                        if (log.isDebugEnabled()) log.debug("debug: Rollup is " + rollup);
                        if (rollup.equals("day")) {
                            // daily
                            if (obj.getLastCheckTime().getDay() != incomingCheckDate.getDay()) {
                                // Create new Object but re-use the Label and the
                                // Service Status from the existing object
                                LogPerformanceData newPerformanceEntry = this.createPerformanceData();
                                newPerformanceEntry.setPerformanceDataLabelId(performanceDataLabelId);
                                newPerformanceEntry.setServiceStatus(obj.getServiceStatus());
                                newPerformanceEntry.setPerformanceDataLabel(getPerformanceDataLabel(performanceName));
                                // re-assign new object to current for further
                                // processing
                                obj = newPerformanceEntry;
                            }
                        } else if (rollup.equals("hour")) {
                            // hourly
                            if (obj.getLastCheckTime().getHours() != incomingCheckDate.getHours()) {
                                // Create new Object but re-use the Label and the
                                // Service Status from the existing object
                                LogPerformanceData newPerformanceEntry = this.createPerformanceData();
                                newPerformanceEntry.setPerformanceDataLabelId(performanceDataLabelId);
                                newPerformanceEntry.setServiceStatus(obj.getServiceStatus());
                                newPerformanceEntry.setPerformanceDataLabel(getPerformanceDataLabel(performanceName));
                                // re-assign new object to current for further
                                // processing
                                obj = newPerformanceEntry;
                            }
                        } else if (rollup.equals("minute")) {
                            // by minute
                            if (obj.getLastCheckTime().getMinutes() != incomingCheckDate.getMinutes()) {
                                // Create new Object but re-use the Label and the
                                // Service Status from the existing object
                                LogPerformanceData newPerformanceEntry = this.createPerformanceData();
                                newPerformanceEntry.setPerformanceDataLabelId(performanceDataLabelId);
                                newPerformanceEntry.setServiceStatus(obj.getServiceStatus());
                                newPerformanceEntry.setPerformanceDataLabel(getPerformanceDataLabel(performanceName));
                                // re-assign new object to current for further
                                // processing
                                obj = newPerformanceEntry;
                            }
                        }
                    }
                } else {
                    obj.setPerformanceDataLabelId(performanceDataLabelId);
                    obj.setPerformanceDataLabel(label);
                }
                // If entry exists and is for the same day update average, min, max,
                // date and measurement points
                obj.setAverage(this.calculateAverage(obj.getAverage().doubleValue(), performanceValue, obj.getMeasurementPoints().intValue()));
                obj.setMeasurementPoints(obj.getMeasurementPoints() + 1);
                obj.setMinimum(this.minimum(obj.getMinimum().doubleValue(), performanceValue));
                obj.setMaximum(this.maximum(obj.getMaximum().doubleValue(), performanceValue));
                obj.setLastCheckTime(incomingCheckDate);

                // For new entries a lookup for PerformanceDataLabelId is necessary
                if (bNewEntry == true) {
                    // For new entries a lookup for Service Status is necessary
                    try {
                        // obj.setPerformanceName(performanceDataLabel);
					
						/* Get Service Status Object */
                        FilterCriteria ssFilter = FilterCriteria.eq("serviceDescription", serviceDescription);
                        ssFilter.and(FilterCriteria.eq("host.hostName", hostName));
                        List ssl = _foundationDAO.query("com.groundwork.collage.model.impl.ServiceStatus", ssFilter, null);

                        if (ssl == null || ssl.size() == 0) {
                            StringBuilder sb = new StringBuilder(ERROR_SERVICE_LOOKUP_FAILED1);
                            sb.append(serviceDescription).append(ERROR_SERVICE_LOOKUP_FAILED_2).append(hostName).append(ERROR_SERVICE_LOOKUP_FAILED_3);

                            throw new BusinessServiceException(sb.toString());
                        }
                        obj.setServiceStatus((ServiceStatus) ssl.get(0));
                        obj.setPerformanceDataLabel(getPerformanceDataLabel(performanceName));
                        obj.setPerformanceDataLabelId(obj.getPerformanceDataLabel().getPerformanceDataLabelId());
                    } catch (DataAccessException e) {
                        StringBuilder sb = new StringBuilder(ERROR_SERVICE_LOOKUP_FAILED2);
                        sb.append(serviceDescription).append(ERROR_SERVICE_LOOKUP_FAILED_2).append(hostName).append(ERROR_THROWS_EXCEPTION);

                        throw new BusinessServiceException(sb.toString(), e);
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder(ERROR_SERVICE_LOOKUP_FAILED3);
                        sb.append(serviceDescription).append(ERROR_SERVICE_LOOKUP_FAILED_2).append(hostName).append(ERROR_THROWS_EXCEPTION);

                        throw new BusinessServiceException(sb.toString(), e);
                    }
                }
                // Persist new or updated object
                this.save(obj);
                if (isCacheEnabled) {
                    perfCache.put(makeKey(hostName, serviceDescription, performanceName), obj);
                }
            } catch (Exception e) {
                String labelMessage = (label == null) ? "none" : (label.getPerformanceName() + "," + label.getPerformanceDataLabelId());
                String message = "perf-key: " + makeKey(hostName, serviceDescription, performanceName) +
                        ", label: " + labelMessage + ", err: " + e.getMessage();
                log.error("Failed to store perfdata: " + message, e);
                StringBuilder sb = new StringBuilder(ERROR_SERVICE_LOOKUP_FAILED4);
                sb.append(serviceDescription).append(ERROR_SERVICE_LOOKUP_FAILED_2).append(hostName).append(ERROR_LABEL_COMMENT).append(performanceName).append(ERROR_BRACKET_CLOSE);

                throw new BusinessServiceException(sb.toString(), e);
            }
        } else {
            StringBuilder sb = new StringBuilder(ERROR_SERVICE_LOOKUP_FAILED5);
            sb.append(serviceDescription).append(ERROR_SERVICE_LOOKUP_FAILED_2).
                    append(hostName).append(ERROR_LABEL_COMMENT).append(performanceName).append(ERROR_BRACKET_CLOSE);
			/* Log the failure of processing the command */
            log.error(sb.toString());
        }
        timer.stop();
    }

    public PerformanceDataLabel createPerformanceDataLabelEntry(String performanceName) {
        PerformanceDataLabel performanceDataLabel = null;
        try {
            performanceDataLabel = new com.groundwork.collage.model.impl.PerformanceDataLabel(performanceName, performanceName, "", "");
            _foundationDAO.save(performanceDataLabel);
            if (isCacheEnabled) {
                labelCache.put(performanceName, performanceDataLabel);
            }
        } catch (Exception e) {
            log.error("PerformanceLabel [" + performanceName + "] was not saved!");
        }
        return performanceDataLabel;
    }


    public void updatePerformanceDataLabelEntry(Integer performanceDataLabelId, String serviceDisplayName, String metricLabel, String unit) {
        log.debug("updatePerformanceDataLabelEntry performanceDataLabelId [" + performanceDataLabelId + "] serviceDisplayName[" + serviceDisplayName + "] metricLabel[" + metricLabel + "] unit[" + unit + "]");
        PerformanceDataLabel performanceDataLabel = null;
        try {
            FilterCriteria pdlFilter = FilterCriteria.eq("performanceDataLabelId", performanceDataLabelId);
            List pdl = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", pdlFilter, null);

            if (pdl == null || pdl.size() == 0) {

                StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_CREATE);
                sb.append(performanceDataLabelId).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

                throw new BusinessServiceException(sb.toString());

            } else
                performanceDataLabel = (PerformanceDataLabel) pdl.get(0);
            performanceDataLabel.setServiceDisplayName(serviceDisplayName);
            performanceDataLabel.setMetricLabel(metricLabel);
            performanceDataLabel.setUnit(unit);
            _foundationDAO.save(performanceDataLabel);
            if (isCacheEnabled) {
                labelCache.put(performanceDataLabel.getPerformanceName(), performanceDataLabel);
            }
        } catch (Exception e) {
            log.error("PerformanceDataLabelId [" + performanceDataLabelId + "] was not saved!");
        }
    }

    public String tableHeading() {
        String style = "<style type=\"text/css\"> table.sf{ text-align: center;font-family: Verdana;font-weight: normal;font-size: 11px;color: #404040;width: 620px;border: 1px #6699CC solid;border-collapse: collapse;border-spacing: 0px; white-space: nowrap;}";
        style = style + "td.pe{border-bottom: 2px solid #6699CC;}";
        style = style + "td.pf{ border-bottom: 2px solid #6699CC;border-left: 1px solid #6699CC;background-color: #BEC8D1;text-align: left;text-indent: 5px;font-family: Verdana;font-weight: bold;font-size: 11px;color: #404040; white-space: nowrap;}";
        style = style + "td.ph{ border-bottom: 2px solid #6699CC;border-left: 1px solid #6699CC;background-color: white;text-align: left;text-indent: 5px;font-family: Verdana;font-size: 9px;color: #404040; white-space: nowrap;}";
        style = style + "input.pi{border:0px none;width:90%;height:90%;background-color:	#FFFFE0;font-family: Verdana;font-size: 10px;color: #404040;}";
        style = style + "td.pj{border-left: 1px solid #6699CC;}</style>";
        return style + "<table class=\"sf\"><tr><td class=\"pe\"><b>PerformanceName</b></td><td class=\"pe\"><b>ServiceDisplayName</b></td><td align=\"middle\" class=\"pe\"><b>MetricLabel</b></td><td align=\"middle\" class=\"pe\"><b>Unit</b></td><td><b>Update Data</b></td></tr>";
    }

    public String tableRow(String r, String performanceDataLabelID, String performanceName, String serviceDisplayName, String metricLabel, String unit) {
        return "<tr><td class=\"pf\" width=\"25%\">" + performanceName + "</td><td class=\"ph\" width=\"35%\"><input name=\"serviceDisplayName" + "." + r + "\" value=\"" + serviceDisplayName + "\"  class=\"pi\"></td><td  class=\"ph\" width=\"25%\"><input name=\"metricLabel" + "." + r + "\" value=\"" + metricLabel + "\" class=\"pi\"></td><td  class=\"ph\" width=\"20%\"><input name=\"unit" + "." + r + "\" value=\"" + unit + "\" size=\"5\" class=\"pi\"></td><td align=\"middle\" width=\"5%\" style=\"border-left: 1px solid #6699CC;\"><input name=\"performanceDataLabelID\" value=\"" + performanceDataLabelID + "\" type=\"checkbox\"  ></td></tr>";
    }

    public String tableFooting() {
        return "<tr><td colspan=\"5\"><center><input TYPE=\"submit\" NAME=\"cmd\" VALUE=\"Update Data\"></center></td></tr></table>";
    }

    public String getPerformanceDataLabel() {
        String rows = "";
        PerformanceDataLabel performanceDataLabel;
        try {
            // obj.setPerformanceName(performanceDataLabel);

			/* Get Service Status Object */
            List pdl = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", null, null);

            if (pdl != null && pdl.size() > 0) {
                Iterator it = pdl.iterator();
                while (it.hasNext()) {
                    performanceDataLabel = (PerformanceDataLabel) it.next();
                    Integer performanceDataLabelID = performanceDataLabel.getPerformanceDataLabelId();
                    String r = performanceDataLabelID.toString();
                    String performanceName = performanceDataLabel.getPerformanceName();
                    String serviceDisplayName = performanceDataLabel.getServiceDisplayName();
                    String metricLabel = performanceDataLabel.getMetricLabel();
                    String unit = performanceDataLabel.getUnit();
                    rows = rows + tableRow(r, performanceDataLabelID.toString(), performanceName, serviceDisplayName, metricLabel, unit);
                }
            } else {
                performanceDataLabel = (PerformanceDataLabel) pdl.get(0);
                if (isCacheEnabled) {
                    labelCache.put(performanceDataLabel.getPerformanceName(), performanceDataLabel);
                }
            }

        } catch (DataAccessException e) {
            rows = "<tr><td>DataAccessException when trying to retrieve all PerformanceDataLabel.</td></tr>";
        } catch (Exception e) {
            rows = "<tr><td>DataAccessException when trying to retrieve all PerformanceDataLabel.</td></tr>";
        }
        return tableHeading() + rows + tableFooting();

    }


    public Integer getPerformanceDataLabelId(String performanceName) {
        Integer performanceDataLabelId = null;
        try {
            if (isCacheEnabled) {
                com.groundwork.collage.model.PerformanceDataLabel label = labelCache.getIfPresent(performanceName);
                if (label != null) {
                    return label.getPerformanceDataLabelId();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Missed label cache lookup on key: " + performanceName);
                }
            }
            // obj.setPerformanceName(performanceDataLabel);
			/* Get Service Status Object */
            FilterCriteria pdlFilter = FilterCriteria.eq("performanceName", performanceName);

            List pdl = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", pdlFilter, null);
            PerformanceDataLabel performanceDataLabel = null;
            if (pdl == null || pdl.size() == 0) {
                // because the performanceName doesn't exist we create a new entry in the PerformanceDataLabel table:
                performanceDataLabel = createPerformanceDataLabelEntry(performanceName);
                if (performanceDataLabel == null) {
                    StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_CREATE);
                    sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);
                    throw new BusinessServiceException(sb.toString());

                }
            } else {
                performanceDataLabel = (PerformanceDataLabel) pdl.get(0);
                if (isCacheEnabled) {
                    labelCache.put(performanceDataLabel.getPerformanceName(), performanceDataLabel);
                }
            }
            // obj.setPerformanceDataLabelId(performanceDataLabel.getPerformanceDataLabelId());
            performanceDataLabelId = performanceDataLabel.getPerformanceDataLabelId();
        } catch (DataAccessException e) {
            StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_LOOKUP_FAILED2);
            sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

            throw new BusinessServiceException(sb.toString(), e);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_LOOKUP_FAILED3);
            sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

            throw new BusinessServiceException(sb.toString(), e);
        }
        if (performanceDataLabelId == null)
            return new Integer(-1);
        else
            return performanceDataLabelId;

    }


    public PerformanceDataLabel getPerformanceDataLabel(String performanceName) {
        PerformanceDataLabel performanceDataLabel;
        try {
            // obj.setPerformanceName(performanceDataLabel);
            if (isCacheEnabled) {
                com.groundwork.collage.model.PerformanceDataLabel label = labelCache.getIfPresent(performanceName);
                if (label != null) {
                    Session session = this.getSession();
                    if (!session.contains(label)) {
                        session.update(label);
                    }
                    return label;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Missed label cache lookup on key: " + performanceName);
                }
            }
			/* Get Service Status Object */
            FilterCriteria pdlFilter = FilterCriteria.eq("performanceName", performanceName);
            List pdl = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", pdlFilter, null);

            if (pdl == null || pdl.size() == 0) {
                // because the performanceName doesn't exist we create a new entry in the PerformanceDataLabel table:
                performanceDataLabel = createPerformanceDataLabelEntry(performanceName);
                if (performanceDataLabel == null) {
                    StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_CREATE);
                    sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

                    throw new BusinessServiceException(sb.toString());
                }
            } else {
                performanceDataLabel = (PerformanceDataLabel) pdl.get(0);
                if (isCacheEnabled) {
                    labelCache.put(performanceDataLabel.getPerformanceName(), performanceDataLabel);
                }
            }
        } catch (DataAccessException e) {
            StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_LOOKUP_FAILED4);
            sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

            throw new BusinessServiceException(sb.toString() + ", " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Exception looking up label " + e.getMessage(), e);
            StringBuilder sb = new StringBuilder(ERROR_PERFORMANCELABEL_LOOKUP_FAILED5);
            sb.append(performanceName).append(ERROR_PERFORMANCELABEL_LOOKUP_FAILED_2).append(ERROR_THROWS_EXCEPTION);

            throw new BusinessServiceException(sb.toString() + ", " + e.getMessage(), e);
        }
        return performanceDataLabel;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#createPerformanceData()
     */
    public LogPerformanceData createPerformanceData() throws BusinessServiceException {
        return (LogPerformanceData) this.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#deletePerformanceData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void deletePerformanceData(String hostName,
                                      String serviceDescription, String performanceDataLabel,
                                      String startDate, String endDate) throws BusinessServiceException {
		
		/* Check date Format */
        final Date startDATE = DateTime.parse(startDate);
        final Date endDATE = DateTime.parse(endDate);

        if (startDATE == null || endDATE == null) {
            StringBuilder sb = new StringBuilder(ERROR_REMOVE_OBJECTS + ERROR_INVALID_START_DATE);
            sb.append(startDate).append(ERROR_INVALID_END_DATE).append(endDate).append(ERROR_INVALID_DATE_FORMAT);
            throw new CollageException(sb.toString());
        }
		
		/* Lookup the list */
        FilterCriteria filter = FilterCriteria.ge("lastCheckTime", startDate);
        filter.and(FilterCriteria.eq("lastCheckTime", endDate));

        List l = this.query(filter, null);

        if (l == null && l.size() == 0) {
            log.warn(WARNING_NOTHING_TO_DELETE);
        } else {
            try {
					/* Remove result set */
                this.delete(l);
            } catch (Exception e) {
                throw new BusinessServiceException(ERROR_LOG_PERFORMANCE_DELETE, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#deletePerformanceData(com.groundwork.collage.model.LogPerformanceData)
     */
    public void deletePerformanceData(LogPerformanceData logPerformanceData)
            throws BusinessServiceException {
        this.delete(logPerformanceData);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#getPerformanceData(org.groundwork.foundation.dao.FilterCriteria,
     *      org.groundwork.foundation.dao.SortCriteria, int, int)
     */
    public FoundationQueryList getPerformanceData(FilterCriteria filter,
                                                  SortCriteria sort, int firstResult, int maxResults)
            throws BusinessServiceException {
        return this.query(filter, sort, firstResult, maxResults);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#getPerformanceData(java.lang.String,
     *      java.lang.String, java.lang.String, int, int)
     */
    public FoundationQueryList getPerformanceData(String hostName,
                                                  String serviceDescription, String performanceDataLabel,
                                                  int firstResult, int maxResults) throws BusinessServiceException {
        return getPerformanceData(hostName, serviceDescription, performanceDataLabel, null, null, -1, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#getPerformanceData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, int, int)
     */
    public FoundationQueryList getPerformanceData(String hostName,
                                                  String serviceDescription, String performanceDataLabel,
                                                  String startDate, String endDate, int firstResult, int maxResults)
            throws BusinessServiceException {
		
		/* Filter */
        FilterCriteria filter = FilterCriteria.eq("serviceStatus.serviceDescription", serviceDescription);
        filter.and(FilterCriteria.eq("host.hostName", hostName));
        filter.and(FilterCriteria.eq("performanceName", performanceDataLabel));

		/* Check date Format */
        if (startDate != null) {
            final Date startDATE = DateTime.parse(startDate);
            final Date endDATE = DateTime.parse(endDate);

            if (startDATE == null || endDATE == null) {
                StringBuilder sb = new StringBuilder(ERROR_REMOVE_OBJECTS + ERROR_INVALID_START_DATE);
                sb.append(startDate).append(ERROR_INVALID_END_DATE).append(endDate).append(ERROR_INVALID_DATE_FORMAT);
                throw new BusinessServiceException(sb.toString());
            }
		
			/* Lookup the list */
            filter.and(FilterCriteria.ge("lastCheckTime", startDate));
            filter.and(FilterCriteria.eq("lastCheckTime", endDate));
        }

        return this.getPerformanceData(filter, null, firstResult, maxResults);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#savePerformanceData(com.groundwork.collage.model.LogPerformanceData)
     */
    public void savePerformanceData(LogPerformanceData performanceData)
            throws BusinessServiceException {
        this.save(performanceData);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.groundwork.foundation.bs.performancedata.PerformanceDataService#savePerformanceData(java.util.Collection)
     */
    public void savePerformanceData(
            Collection<LogPerformanceData> collectionPerformanceData)
            throws BusinessServiceException {
        this.save(collectionPerformanceData);
    }
	
	/*
	 * --- Utility methods ---
	 */

    /* Calcualte average */
    private double calculateAverage(double average, double newEntry, int points) {
        // calculate the new average
        return ((average * points) + newEntry) / (points + 1);
    }

    /* minimum calculation */
    private double minimum(double currentMinimum, double newEntry) {
        // First Entry
        if (currentMinimum == -1)
            return newEntry;

        if (newEntry < currentMinimum)
            return newEntry;
        else
            return currentMinimum;
    }

    /* maximum calculation */
    private double maximum(double currentMaximum, double newEntry) {
        if (newEntry > currentMaximum)
            return newEntry;
        else
            return currentMaximum;
    }

    public void init() {
        String prop = foundationProperties.getProperty("performancedata.cache.enabled", "true");
        this.isCacheEnabled = Boolean.parseBoolean(prop);
        if (log.isInfoEnabled()) {
            log.info("Performance Data Service caching is " + isCacheEnabled);
        }
    }

    protected String makeKey(String hostName, String serviceDescription, String performanceName) {
        return hostName + KEY_DELIMITER + serviceDescription + KEY_DELIMITER + performanceName;
    }

    public void flushCaches() {
        perfCache.invalidateAll();
        labelCache.invalidateAll();
    }

    public void deleteLabel(String performanceName) {
        FilterCriteria pdlFilter = FilterCriteria.eq("performanceName", performanceName);
        List result = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", pdlFilter, null);

        if (result == null || result.size() == 0) {
            return;
        }
        _foundationDAO.delete(result.get(0));
    }

    public void deletePerformanceData(String hostName, String serviceName, String performanceName) {
        FilterCriteria filter = FilterCriteria.eq("serviceStatus.serviceDescription", serviceName);
        filter.and(FilterCriteria.eq("serviceStatus.host.hostName", hostName));
        filter.and(FilterCriteria.eq("performanceDataLabel.performanceName", performanceName));
        FoundationQueryList performanceData = this.getPerformanceData(filter, null, 0, 1);
        // If list is entry data doesn't exist -- create new entry
        if ((performanceData == null) || (performanceData != null && performanceData.size() == 0))
            _foundationDAO.delete(performanceData.get(0));
    }

    public LogPerformanceData lookupPerformanceData(String hostName, String serviceName, String performanceName) {
        FilterCriteria filter = FilterCriteria.eq("serviceStatus.serviceDescription", serviceName);
        filter.and(FilterCriteria.eq("serviceStatus.host.hostName", hostName));
        filter.and(FilterCriteria.eq("performanceDataLabel.performanceName", performanceName));
        FoundationQueryList performanceData = this.getPerformanceData(filter, null, 0, 1);
        // If list is entry data doesn't exist -- create new entry
        if ((performanceData == null || performanceData.size() == 0)) {
            return null;
        }
        return (LogPerformanceData) performanceData.get(0);
    }

    public com.groundwork.collage.model.PerformanceDataLabel lookupPerformanceDataLabel(String performanceName) {
        PerformanceDataLabel performanceDataLabel;
        // obj.setPerformanceName(performanceDataLabel);
        // TODO: cache by object (PerformanceDataLabel)
		/* Get Service Status Object */
        FilterCriteria pdlFilter = FilterCriteria.eq("performanceName", performanceName);
        List pdl = _foundationDAO.query("com.groundwork.collage.model.impl.PerformanceDataLabel", pdlFilter, null);
        if (pdl == null || pdl.size() == 0) {
            return null;
        }
        return (com.groundwork.collage.model.PerformanceDataLabel) pdl.get(0);
    }
}
