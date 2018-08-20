package com.groundworkopensource.portal.common.ws.impl;

import java.util.Collection;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;


import org.apache.log4j.Logger;


import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupList;
import com.groundworkopensource.portal.common.ws.IPortalCustomGroupWSFacade;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.model.EntityTypeList;
import org.groundwork.rs.client.PortalCustomGroupClient;

/**
 * This class provides methods to interact with "CustomGroup" from JBOSS REST
 * service.
 *
 * @author Arul
 *
 */

public class PortalCustomGroupWSFacade extends BaseFacade implements IPortalCustomGroupWSFacade {

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();



    /**
     * returns all available custom groups
     *
     * @return Collection<CustomGroup>
     * @throws WSDataUnavailableException
     */

    public Collection<CustomGroup> findCustomGroups()
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        CustomGroupList groupList = client.findCustomGroups();
        Collection<CustomGroup> groups = groupList.getList();
        if (groups == null)
            groups = new ArrayList<CustomGroup>();
        return groups;
    }


    /**
     * Create customgroup.
     *
     * @throws WSDataUnavailableException

     */
    public void createCustomGroup(String groupName, int entityTypeId,
                                  String parents, String groupState, String createdBy, String children)
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.createCustomGroup(groupName,entityTypeId,parents,groupState,createdBy,children);
    }



    /**
     * Update customgroup.
     *
     * @throws WSDataUnavailableException

     */
    public void updateCustomGroup(String groupName, int entityTypeId,
                                  String parents, String groupState, String createdBy, String children)
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.updateCustomGroup(groupName, entityTypeId, parents, groupState, createdBy, children);
    }

    /**
     * Remove customgroup.
     *
     * @throws WSDataUnavailableException

     */
    public void removeCustomGroup(Long groupid)
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.removeCustomGroup(String.valueOf(groupid));
    }

    /**
     * Removes orphaned children just in case monarch delete hostgroups or
     * servie groups.
     *
     * @throws WSDataUnavailableException

     */
    public void removeOrphanedChildren(Long elementId, int entityTypeId)
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.removeOrphanedChildren(elementId,entityTypeId);
    }

    /**
     * Gets all entitytypes.
     *
     * @throws WSDataUnavailableException

     */
    public Collection<EntityType> findEntityTypes()
            throws WSDataUnavailableException {
        PortalCustomGroupClient client = new PortalCustomGroupClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        EntityTypeList groupList = client.findEntityTypes();
        Collection<EntityType> entityTypes = groupList.getList();
        if (entityTypes == null)
            entityTypes = new ArrayList<EntityType>();
        return entityTypes;
    }

}
