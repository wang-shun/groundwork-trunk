/**
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2009
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
package org.groundwork.foundation.bs.rrd;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.ws.model.impl.RRDGraph;

import java.util.Collection;

/**
 * Foundation Business Service for retrieving graphs from existing RRD files.
 * 
 * @author rruttimann@gwos.com
 */
public interface RRDService extends BusinessService {

    /** The Constant RRD_THREAD_TIMEOUT. */
    static final String RRD_THREAD_TIMEOUT = "rrdtool.thread.timeout";
    
    /** The Constant RRD_THREAD_POOL_SIZE. */
    static final String RRD_THREAD_POOL_SIZE = "rrdtool.thread.pool.size";

    /** The Constant DEFAULT_MAX_THERADPOOL_SIZE. */
    static final String DEFAULT_MAX_THERADPOOL_SIZE = "10";

    /* Default size of the RRD Graph if the size is not defined */
    /** The Constant DEFAULT_RRD_WIDTH. */
    static final int DEFAULT_RRD_WIDTH = 620;

    /** Default set to /usr/local/groundwork/common/bin. */
    static final String RRD_TOOL_PATH = "rrdtool.path";

    /** The Constant RRD_PROPERTY_RRDPATH. */
    static final String RRD_PROPERTY_RRDPATH = "RRDPath";
    
    /** The Constant RRD_PROPERTY_LABEL. */
    static final String RRD_PROPERTY_LABEL = "RRDLabel";

    /** The Constant RRD_PROPERTY_COMMAND. */
    static final String RRD_PROPERTY_COMMAND = "RRDCommand";
    
    /** The Constant REMOTE_RRD_PROPERTY_COMMAND. */
    static final String REMOTE_RRD_PROPERTY_COMMAND = "RemoteRRDCommand";
    
    /** The Constant REMOTE_RRD_ENDPOINT. */
    static final String REMOTE_RRD_ENDPOINT = "http://$REMOTE_RRD_HOST$/foundation-webapp/services/wsrrd";
    
    /** The Constant REMOTE_RRD_HOST. */
    static final String REMOTE_RRD_HOST = "$REMOTE_RRD_HOST$";

    /** "wsrrd" Web service. */
    static final String FOUNDATION_END_POINT_RRD = "wsrrd";

    /** The Constant CACTI_PROPERTY_COMMAND. */
    static final String CACTI_PROPERTY_COMMAND = "CactiRRDCommand";
    
    /** The Constant CACTI_INTERFACE_DELIMITER. */
    static final String CACTI_INTERFACE_DELIMITER = "cacti.interface.delimiter";
    
    /** The Constant CACTI_INTERFACE_LABELURL_DELIMITER. */
    static final String CACTI_INTERFACE_LABELURL_DELIMITER = "cacti.interface.labelurl.delimiter";

    /**
     * Get one or more graph objects for a given Host or Host/Service object. If
     * the serviceName parameter is not set all graphs for a given host will be
     * returned.
     * 
     * Arguments:
     * 
     * @param applicationType
     *            -- Default Nagios but can be other DAta providers such as
     *            Cacti
     * @param hostName
     *            -- Required. Host Name for which the graph is to be retrieved
     * @param serviceName
     *            -- Service Name for which the graph si to be retrieved. If
     *            null all graphs for a host are retrieved.
     * @param startDate   rtDate
     *            -- Start date is total number of seconds since epoch (time in
     *            seconds since 01-01-1970)
     * @param endDate
     *            -- optional. Total number of seconds since epoch (time in
     *            seconds since 01-01-1970)Default to current time
     * @param graphWidth
     *            the graph width
     * 
     * @return the collection< rrd graph>
     * 
     * @throws BusinessServiceException
     *             the business service exception
     */
    public Collection<RRDGraph> generateGraph(String applicationType,
            String hostName, String serviceName, long startDate, long endDate,
            int graphWidth) throws BusinessServiceException;

    /**
     * Get one or more graph objects for a given Host or Host/Service object. If
     * the serviceName parameter is not set all graphs for a given host will be
     * returned.
     * 
     * @param applicationType
     *            -- Default Nagios but can be other DAta providers such as
     *            Cacti
     * @param hostName
     *            -- Required. Host Name for which the graph is to be retrieved
     * @param serviceName
     *            -- Service Name for which the graph si to be retrieved. If
     *            null all graphs for a host are retrieved.
     * @param timeInterval
     *            in seconds. It defines the time range back from current time
     * 
     * @return the collection< rrd graph>
     * 
     * @throws BusinessServiceException
     *             the business service exception
     */
    public Collection<RRDGraph> generateGraph(String applicationType,
            String hostName, String serviceName, long timeInterval)
            throws BusinessServiceException;

}
