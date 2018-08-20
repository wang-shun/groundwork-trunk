package com.groundworkopensource.portal.statusviewer.common;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.log4j.Logger;

/**
 * Comparator class, used in sorting of NetworkMetaEntity objects. Used in
 * search functionality.
 * 
 * @author nitin_jadhav
 * 
 */
public class NetworkMetaEntityComparator implements
        Comparator<NetworkMetaEntity>, Serializable {

    /**
     * default UId
     */
    private static final long serialVersionUID = 1L;

    /**
     * MINUS_ONE
     */
    private static final int MINUS_ONE = -1;

    /**
     * FOUR
     */
    private static final int FOUR = 4;

    /**
     * THREE
     */
    private static final int THREE = 3;

    /**
     * TWO
     */
    private static final int TWO = 2;

    /**
     * ONE
     */
    private static final int ONE = 1;

    /**
     * Currently set sorting type
     */
    private SortTypeEnum currentSortType;

    /**
     * weight for host type
     */
    private int hostWeight = 0;

    /**
     * weight for host group type
     */
    private int hostGroupWeight = 0;

    /**
     * weight for service type
     */
    private int serviceWeight = 0;

    /**
     * 
     */
    public NetworkMetaEntityComparator() {
    }

    /**
     * weight for service group type
     */
    private int serviceGroupWeight = 0;

    /**
     * logger
     * 
     * As this class is Serializable, Logger is transient
     * 
     */
    private static final Logger LOGGER = Logger
            .getLogger(NetworkMetaEntityComparator.class.getName());

    /**
     * Set current sort option. Also, set weights according to option
     * 
     * @param sortType
     */
    public void setSortType(SortTypeEnum sortType) {
        currentSortType = sortType;

        switch (currentSortType) {
            case ALPHABETIC:
                break;
            case HOST_HOSTGROUP_SERVICE_SERVICEGROUP:
                hostWeight = ONE;
                hostGroupWeight = TWO;
                serviceWeight = THREE;
                serviceGroupWeight = FOUR;
                break;

            case HOSTGROUP_HOST_SERVICEGROUP_SERVICE:
                hostWeight = TWO;
                hostGroupWeight = ONE;
                serviceWeight = FOUR;
                serviceGroupWeight = THREE;
                break;

            case SERVICE_SERVICEGROUP_HOST_HOSTGROUP:
                hostWeight = THREE;
                hostGroupWeight = FOUR;
                serviceWeight = ONE;
                serviceGroupWeight = TWO;
                break;

            case SERVICEGROUP_SERVICE_HOSTGROUP_HOST:
                hostWeight = FOUR;
                hostGroupWeight = THREE;
                serviceWeight = TWO;
                serviceGroupWeight = ONE;
                break;

            default:
                break;
        }

    }

    /**
     * Comparison method of Comparator.
     * 
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * 
     * @param entity1
     * @param entity2
     * 
     * @return int
     */
    public int compare(NetworkMetaEntity entity1, NetworkMetaEntity entity2) {

        int entity1Weight = getWeight(entity1.getType());
        int entity2Weight = getWeight(entity2.getType());

        if (entity1Weight > entity2Weight) {
            return ONE;
        } else if (entity1Weight < entity2Weight) {
            return MINUS_ONE;
        } else {
            // both are equal, now sort in alphabetic order
            return entity1.getName().compareTo(entity2.getName());
        }

    }

    /**
     * Returns weights of Node types
     * 
     * @param nodeType
     * @return
     */
    private int getWeight(NodeType nodeType) {
        switch (nodeType) {
            case HOST:
                return hostWeight;
            case HOST_GROUP:
                return hostGroupWeight;
            case SERVICE:
                return serviceWeight;
            case SERVICE_GROUP:
                return serviceGroupWeight;
            default:
                LOGGER.debug("Unknown node type encountered: " + nodeType);
                return 0;
        }

    }

}
