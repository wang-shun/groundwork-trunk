/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
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

/* Created on: Mar 20, 2006 */

package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.MatchType;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.AttributeData;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.impl.CollageConvert;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ApplicationEntityProperty;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.impl.ActionReturn;

/**
 * WebServiec Implementation for WSCommon interface.
 * 
 * @author rogerrut
 */
public class WSCommonImpl extends WebServiceImpl implements WSCommon {

    /** EMPTY_STRING. */
    private static final String EMPTY_STRING = "";

    /** EXCLUDED_ROLE_KEY. */
    private static final String EXCLUDE_HG_OR_SG_KEY = "R#STR!CT#D";

    /** UNSERSCORE String. */
    public static final String UNSERSCORE = "_";

    /**
     * Instantiates a new WS common impl.
     */
    public WSCommonImpl() {
    }

    /** Enable logging */
    protected static Log log = LogFactory.getLog(WSCommonImpl.class);

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#login(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String login(String username, String password, String realUserName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#logout()
     */
    public void logout() {
        // TODO Auto-generated method stub
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getAttributeData(org.groundwork
     *      .foundation.ws.model.impl.AttributeQueryType)
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection getAttributeData(
            org.groundwork.foundation.ws.model.impl.AttributeQueryType type)
            throws WSFoundationException {
        if (type == null) {
            log.error("AttributeQueryType cannot be null");
            throw new WSFoundationException(
                    "AttributeQueryType cannot be null",
                    ExceptionType.WEBSERVICE);
        }

        if (log.isInfoEnabled()) {
            log.info("Retrieving attribute data for " + type.toString());
        }

        try {
            Collection col = null;

            if (type == AttributeQueryType.APPLICATION_TYPES) {
                FoundationQueryList list = getMetadataService()
                        .getApplicationTypes(null, null, -1, -1);

                col = list.getResults();
            } else if (type == AttributeQueryType.CATEGORIES) {
                FoundationQueryList list = getCategoryService().getCategories(
                        null, null, -1, -1);
                col = list.getResults();
            } else if (type == AttributeQueryType.CHECK_TYPES) {
                col = getMetadataService().getCheckTypeValues();
            } else if (type == AttributeQueryType.COMPONENTS) {
                col = getMetadataService().getComponentValues();
            } else if (type == AttributeQueryType.MONITOR_STATUSES) {
                col = getMetadataService().getMonitorStatusValues();
            } else if (type == AttributeQueryType.OPERATION_STATUSES) {
                col = getMetadataService().getOperationStatusValues();
            } else if (type == AttributeQueryType.PRIORITIES) {
                col = getMetadataService().getPriorityValues();
            } else if (type == AttributeQueryType.SEVERITIES) {
                col = getMetadataService().getSeverityValues();
            } else if (type == AttributeQueryType.STATE_TYPES) {
                col = getMetadataService().getStateTypeValues();
            } else if (type == AttributeQueryType.TYPE_RULES) {
                col = getMetadataService().getTypeRuleValues();
            } else {
                throw new WSFoundationException(
                        "AttributeQueryType not handled - " + type.toString(),
                        ExceptionType.WEBSERVICE);
            }

            if (log.isInfoEnabled()) {
                log.info("Retrieved " + (col == null ? 0 : col.size())
                        + " values");
            }

            // Convert to WSObjects
            AttributeData[] objs = getConverter()
                    .convertAttributeData(
                            (Collection<com.groundwork.collage.model.AttributeData>) col);

            return new WSFoundationCollection(objs);
        } catch (Exception e) {
            log.error("Error occurred in getAttributeData()", e);
            throw new WSFoundationException(
                    "Error occurred in getAttributeData()" + e,
                    ExceptionType.WEBSERVICE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getAttributeDataByString(java
     *      .lang.String)
     */
    public WSFoundationCollection getAttributeDataByString(String type)
            throws WSFoundationException {
        if (type == null || type.length() == 0) {
            throw new WSFoundationException(
                    "Invalid null/empty attribute query type string",
                    ExceptionType.WEBSERVICE);
        }

        org.groundwork.foundation.ws.model.impl.AttributeQueryType queryType = org.groundwork.foundation.ws.model.impl.AttributeQueryType
                .fromValue(type);

        return getAttributeData(queryType);
    }

    /**
     * Utility methods to execute prepared queries or to cancel queries
     */
    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#executeQuery(int)
     */
    public WSFoundationCollection executeQuery(int sessionID)
            throws WSFoundationException {
        return null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#cancelQuery(int)
     */
    public String cancelQuery(int sessionID) {
        return EMPTY_STRING;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getEntityTypeProperties(java
     *      .lang.String, java.lang.String, boolean)
     */
    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean bComponentProperties)
            throws WSFoundationException, RemoteException {
        if (entityType == null || entityType.length() == 0) {
            throw new WSFoundationException(
                    "Invalid null/empty entity type parameter",
                    ExceptionType.WEBSERVICE);
        }

        try {
            List<ApplicationEntityProperty> list = getMetadataService()
                    .getApplicationEntityProperties(entityType, appType,
                            bComponentProperties);

            return new WSFoundationCollection(list.size(), getConverter()
                    .convertEntityProperty(list));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getEntityTypes()
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection getEntityTypes()
            throws WSFoundationException, RemoteException {
        try {
            FoundationQueryList list = getMetadataService().getEntityTypes(
                    null, SortCriteria.asc(MetadataService.PROP_NAME), -1, -1);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertEntityType(
                            (Collection<EntityType>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#performEntityQuery(java.lang
     *      .String, org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection performEntityQuery(String entityType,
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws WSFoundationException, RemoteException {
        if (entityType == null || entityType.length() == 0) {
            throw new WSFoundationException(
                    "Invalid null/empty entity type parameter",
                    ExceptionType.WEBSERVICE);
        }

        try {
            FoundationQueryList results = getMetadataService()
                    .performEntityQuery(entityType,
                            getConverter().convert(filter),
                            getConverter().convert(sort), firstResult,
                            maxResults);

            // Convert to map of property values
            return new WSFoundationCollection(results.size(), getConverter()
                    .convertPropertyExtensible((List<PropertyExtensible>) results.getResults(),
                            false));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#performEntityCountQuery(java
     *      .lang.String, org.groundwork.foundation.ws.model.impl.Filter)
     */
    public int performEntityCountQuery(String entityType, Filter filter)
            throws WSFoundationException, RemoteException {
        if (entityType == null || entityType.length() == 0) {
            throw new WSFoundationException(
                    "Invalid null/empty entity type parameter",
                    ExceptionType.WEBSERVICE);
        }

        try {
            return getMetadataService().performEntityCountQuery(entityType,
                    getConverter().convert(filter));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getActionsByApplicationType
     *      (java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean includeSystem) throws WSFoundationException,
            RemoteException {
        if (appType == null || appType.length() == 0) {
            throw new WSFoundationException(
                    "Invalid null/empty app type parameter",
                    ExceptionType.WEBSERVICE);
        }

        try {
            FoundationQueryList results = getActionService()
                    .getActionByApplicationType(appType, includeSystem);

            // Convert to map of property values
            return new WSFoundationCollection(results.size(), getConverter()
                    .convertAction((List<Action>) results.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#getActionsByCriteria(org.groundwork
     *      .foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection getActionsByCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults)
            throws WSFoundationException, RemoteException {
        try {

            CollageConvert converter = getConverter();

            FilterCriteria filterCriteria = converter.convert(filter);
            org.groundwork.foundation.dao.SortCriteria sortCriteria = converter
                    .convert(sort);

            FoundationQueryList results = getActionService()
                    .getActionsByCriteria(filterCriteria, sortCriteria,
                            firstResult, maxResults);

            // Convert to map of property values
            return new WSFoundationCollection(results.getTotalCount(),
                    getConverter().convertAction((List<Action>) results.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSCommon#performActions(org.groundwork
     *      .foundation.ws.model.impl.ActionPerform[])
     */
    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws WSFoundationException, RemoteException {
        // if (actionPerforms == null || actionPerforms.getActionPerform() ==
        // null)
        // throw new WSFoundationException("Invalid null / empty
        // WSFoundationCollection parameter.",
        // ExceptionType.WEBSERVICE);

        try {
            List<com.groundwork.collage.model.impl.ActionPerform> listActionPerforms = getConverter()
                    .convert(actionPerforms);

            List<ActionReturn> returns = getActionService().performActions(
                    listActionPerforms);

            // Convert to map of property values
            return new WSFoundationCollection(returns.size(), getConverter()
                    .convert(returns));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        }
    }

    /**
     * Searchs HostGroups, Hosts, ServiceGroups and Services filter by comma
     * separated service group and host group list String and returns the
     * results. If comma separated service group and host group list String is
     * empty then return All HostGroups, Hosts, ServiceGroups and Services as
     * per search text. if comma separated service group contains exclude
     * keyword then search only depends on host group list and vice versa.
     * 
     * Null extended role service group or host group list string is invalid.
     * 
     * @param text
     *            search text
     * @param maxresults
     *            the maxresults
     * @param extRoleServiceGroupList
     *            the ext role service group list
     * @param extRoleHostGroupList
     *            the ext role host group list
     * 
     * @return WSFoundationCollection
     * 
     * @throws RemoteException
     *             the remote exception
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    @SuppressWarnings("unchecked")
    public WSFoundationCollection searchEntity(String text, int maxresults,
            String extRoleServiceGroupList, String extRoleHostGroupList)
            throws RemoteException, WSFoundationException {

        // First check the maxresult value
        if (maxresults <= 0) {
            throw new WSFoundationException(
                    "Max results must be greater than zero",
                    ExceptionType.WEBSERVICE);
        } // end if

        if (text == null || text.equalsIgnoreCase(EMPTY_STRING)) {
            throw new WSFoundationException("Invalid search text",
                    ExceptionType.WEBSERVICE);
        }
        /*
         * if search text contains wildcard character underscore, then escape
         * it.
         */
        if (text.contains(UNSERSCORE)) {
            text = text.replaceAll(UNSERSCORE, "\\\\_");
        }
        if (extRoleServiceGroupList == null || extRoleHostGroupList == null) {
            throw new WSFoundationException(
                    "Invalid Extended host group list or service group list",
                    ExceptionType.WEBSERVICE);
        }
        WSFoundationCollection col = null;
        org.groundwork.foundation.ws.model.impl.HostGroup[] hgArray = null;
        org.groundwork.foundation.ws.model.impl.Host[] hostArray = null;
        org.groundwork.foundation.ws.model.impl.Category[] catArray = null;
        org.groundwork.foundation.ws.model.impl.ServiceStatus[] serArray = null;
        int sum = 0;

        if (!extRoleHostGroupList.contains(EXCLUDE_HG_OR_SG_KEY)) {
            FilterCriteria filterCriteria = FilterCriteria.ilike(
                    HostGroup.HP_NAME, text, MatchType.ANYWHERE);
            
            FilterCriteria aliasCriteria = FilterCriteria.ilike(
                    HostGroup.HP_ALIAS, text, MatchType.ANYWHERE);            
            filterCriteria.or(aliasCriteria);

            if (!extRoleHostGroupList.equals(EMPTY_STRING)) {

                FilterCriteria hgFilterCriteria = createInFilterCriteria(
                        HostGroup.HP_NAME, extRoleHostGroupList);

                filterCriteria.and(hgFilterCriteria);
            }
            FoundationQueryList hostGroups = this.getHostGroupService()
                    .getHostGroups(filterCriteria, null, -1, -1);
            hgArray = getConverter().convertHostGroup(
                    (Collection<HostGroup>) hostGroups.getResults(), false);
            sum = sum + hostGroups.size();
        }
        if (sum < maxresults) {
            log
                    .debug("Summation is  less than the max results. Checking hosts..");
            if (!extRoleHostGroupList.contains(EXCLUDE_HG_OR_SG_KEY)) {
                FilterCriteria finalHostFilterCriteria = FilterCriteria.ilike(
                        Host.HP_NAME, text, MatchType.ANYWHERE);
                
                FilterCriteria aliasNameCriteria = FilterCriteria.eq(
                        "hostStatus.propertyValues.name", "Alias");       
                
                FilterCriteria aliasValueCriteria = FilterCriteria.ilike(
                        "hostStatus.propertyValues.valueString",text,MatchType.ANYWHERE);        
                
                aliasNameCriteria.and(aliasValueCriteria);                
                finalHostFilterCriteria.or(aliasNameCriteria);
                
                FilterCriteria deviceIdCriteria = FilterCriteria.ilike(
                        "device.identification",text,MatchType.ANYWHERE);
                
                finalHostFilterCriteria.or(deviceIdCriteria);
                
                if (!extRoleHostGroupList.equals(EMPTY_STRING)) {

                    FilterCriteria HostFilterCriteria = createInFilterCriteria(
                            "hostGroups.name", extRoleHostGroupList);

                    finalHostFilterCriteria.and(HostFilterCriteria);
                }
                FoundationQueryList hosts = this.getHostService().getHosts(
                        finalHostFilterCriteria, null, -1, -1);
                sum = sum + hosts.size();
                hostArray = getConverter().convertHost(
                        (Collection<Host>) hosts.getResults(), false);

            } // end if
        }
        if (sum < maxresults) {
            log
                    .debug("Summation is  less than the max results. Service Groups..");
            if (!extRoleServiceGroupList.contains(EXCLUDE_HG_OR_SG_KEY)) {
                FilterCriteria sgFilterCriteria = FilterCriteria.ilike(
                        Category.HP_NAME, text, MatchType.ANYWHERE);
                if (!extRoleServiceGroupList.equals(EMPTY_STRING)) {

                    FilterCriteria extSgFilterCriteria = createInFilterCriteria(
                            Category.HP_NAME, extRoleServiceGroupList);

                    sgFilterCriteria.and(extSgFilterCriteria);
                }

                FoundationQueryList serviceGroups = this.getCategoryService()
                        .getCategories(sgFilterCriteria, null, -1, -1);
                sum = sum + serviceGroups.size();
                catArray = getConverter().convertCategory(
                        (Collection<Category>) serviceGroups.getResults());
            }
        } // end if
        if (sum < maxresults) {
            log
                    .debug("Summation is  less than the max results. Checking Service status..");
            FilterCriteria servcieFilterCriteria = FilterCriteria.ilike(
                    ServiceStatus.HP_SERVICE_DESCRIPTION, text,
                    MatchType.ANYWHERE);
            // check if both host group list and service group list are empty
            // then search services in all host groups and service groups.
            if (!extRoleServiceGroupList.equals(EMPTY_STRING)
                    && !extRoleHostGroupList.equals(EMPTY_STRING)) {
                List<Integer> servicesIdList = new ArrayList<Integer>();
                if (!extRoleServiceGroupList.contains(EXCLUDE_HG_OR_SG_KEY)) {
                    if (!extRoleServiceGroupList.equals(EMPTY_STRING)) {

                        FilterCriteria sgFilterCriteria = createInFilterCriteria(
                                Category.HP_NAME, extRoleServiceGroupList);

                        FoundationQueryList serviceGroups = this
                                .getCategoryService().getCategories(
                                        sgFilterCriteria, null, -1, -1);
                        org.groundwork.foundation.ws.model.impl.Category[] categoryArray = getConverter()
                                .convertCategory(
                                        (Collection<Category>) serviceGroups
                                                .getResults());

                        if (null != categoryArray) {
                            for (org.groundwork.foundation.ws.model.impl.Category category : categoryArray) {
                                CategoryEntity[] categoryEntities = category
                                        .getCategoryEntities();
                                for (CategoryEntity categoryEntity : categoryEntities) {
                                    servicesIdList.add(categoryEntity
                                            .getObjectID());
                                }

                            }
                        }

                    } else {
                        Collection<Category> rootCategories = this
                                .getCategoryService().getRootCategories(
                                        "SERVICE_GROUP");
                        Iterator<Category> iterator = rootCategories.iterator();

                        while (iterator.hasNext()) {
                            Category category = iterator.next();
                            if (category != null) {
                                Collection<com.groundwork.collage.model.CategoryEntity> categoryEntities = category
                                        .getCategoryEntities();
                                if (categoryEntities != null) {
                                    Iterator<com.groundwork.collage.model.CategoryEntity> categoryEntity = categoryEntities
                                            .iterator();
                                    while (categoryEntity.hasNext()) {
                                        com.groundwork.collage.model.CategoryEntity CategoryEntity = categoryEntity
                                                .next();
                                        servicesIdList.add(CategoryEntity
                                                .getObjectID());
                                    }// end while
                                }// end if
                            }// end if

                        }// end while
                    }
                }
                // check for inclusion of host group in serach criteria
                if (!extRoleHostGroupList.contains(EXCLUDE_HG_OR_SG_KEY)) {
                    if (!extRoleHostGroupList.equals(EMPTY_STRING)) {

                        FilterCriteria hgFilterCriteria = createInFilterCriteria(
                                "host.hostGroups.name", extRoleHostGroupList);

                        if (servicesIdList.isEmpty()) {
                            servcieFilterCriteria.and(hgFilterCriteria);
                        } else {
                            FilterCriteria servideIdsFilter = FilterCriteria
                                    .in("serviceStatusId", servicesIdList);
                            servideIdsFilter.or(hgFilterCriteria);
                            servcieFilterCriteria.and(servideIdsFilter);
                        }
                    }
                } else {
                    // service id list is empty, if no service group defined in
                    // system or supplied service group does not contains any
                    // service.
                    if (servicesIdList.isEmpty()) {
                        servcieFilterCriteria.and(FilterCriteria.ilike(
                                ServiceStatus.HP_SERVICE_DESCRIPTION, "-1",
                                MatchType.ANYWHERE));
                    } else {
                        FilterCriteria servideIdsFilter = FilterCriteria.in(
                                "serviceStatusId", servicesIdList);
                        servcieFilterCriteria.and(servideIdsFilter);
                    }
                }
            }
            FoundationQueryList serviceStatus = this.getStatusService()
                    .getServices(servcieFilterCriteria, null, -1, -1);
            sum = sum + serviceStatus.size();
            serArray = getConverter().convertServiceStatus(
                    (Collection<ServiceStatus>) serviceStatus.getResults(),
                    false);
        } // end if

        col = new WSFoundationCollection();
        col.setHostGroup(hgArray);
        col.setHost(hostArray);
        col.setCategory(catArray);
        col.setServiceStatus(serArray);
        col.setTotalCount(sum);

        log.debug("HostGroup Array Size = " + hgArray);
        log.debug("Host Array Size = " + hostArray);
        log.debug("Category Array Size = " + catArray);
        log.debug("ServiceStatus Array Size = " + serArray);
        return col;
    }

    /**
     * Create In filterCriteria as per property name and value.
     * 
     * @param propertyName
     *            the property name
     * @param value
     *            the value
     * 
     * @return FilterCriteria
     */
    private FilterCriteria createInFilterCriteria(String propertyName,
            String value) {
        FilterCriteria filterCriteria = null;
        if (propertyName != null && value != null) {
            StringTokenizer stkn = new StringTokenizer(value, ",");
            Object[] objArray = new Object[stkn.countTokens()];
            int i = 0;
            while (stkn.hasMoreTokens()) {
                String tokenValue = stkn.nextToken();
                objArray[i] = tokenValue;
                i++;
            }
            filterCriteria = FilterCriteria.in(propertyName, objArray);
        }
        return filterCriteria;
    }

}
