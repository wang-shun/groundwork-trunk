package com.groundworkopensource.portal.common.ws;


import java.util.Collection;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.EntityTypeList;
import com.groundworkopensource.portal.model.EntityType;

/**
 * Interface defining methods for "Customgroup" web service
 * 
 * @author Arul Shanmugam
 * 
 */
public interface ICustomGroupWSFacade {

    /**
     * Returns the list of customgroups.
     * 
     * @return the list of all custom-groups
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	Collection<CustomGroup> findCustomGroups() throws WSDataUnavailableException;
	
	 /**
     * Create customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	void createCustomGroup(String groupName,
			int entityTypeId, String parents, String groupState,
			String createdBy, String children) throws WSDataUnavailableException;
	
	/**
     * Update customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	void updateCustomGroup(String groupName,
			int entityTypeId, String parents, String groupState,
			String createdBy, String children) throws WSDataUnavailableException;

	/**
     * Remove customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	void removeCustomGroup(Long groupid) throws WSDataUnavailableException;
	
	/**
     * Removes orphaned children just in case monarch delete hostgroups or servie groups.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	void removeOrphanedChildren(Long elementId, int entityTypeId) throws WSDataUnavailableException;
	
	/**
     * Gets all entitytypes.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
	Collection<EntityType> findEntityTypes() throws WSDataUnavailableException;

}
