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

package com.groundwork.collage.impl.admin;

import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author <a href="mailto:pparavicini@itgroundwork.com">Philippe Paravicini</a>
 */
public class CollageAdminMetadataImpl implements CollageAdminMetadata
{
	Log log = LogFactory.getLog(this.getClass());

	private MetadataService metadataService;

	public CollageAdminMetadataImpl(MetadataService metadataService) 
	{
		this.metadataService = metadataService;
	}

	public String[] getApplicationTypeNames()
	{
		FoundationQueryList l = null;
		try 
		{
			l = metadataService.getApplicationTypes(null, null, -1, -1);
		}
		catch (Exception e)
		{
			String msg = "Error occurred getApplicationTypeNames()";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		
		String[] names = new String[l.size()];

		int j = 0;
		for (Iterator i = l.iterator(); i.hasNext();) {
			ApplicationType a = (ApplicationType)i.next();
			names[j] = a.getName();
			j++;
		}

		return names;
	}

	public String[] getExtensibleEntityNames()
	{
		FoundationQueryList l = null;
		try {
			l = metadataService.getEntityTypes(null, null, -1, -1);
		}
		catch (Exception e)
		{
			String msg = "Error occurred getExtensibleEntityNames()";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		
		String[] names = new String[l.size()];

		int j = 0;
		for (Iterator i = l.iterator(); i.hasNext();) {
			names[j] = ((EntityType)i.next()).getName();
			j++;
		}

		return names;
	}

	public String[] getPropertyTypeNames()
	{
		FoundationQueryList l = null;
		try 
		{
			l = metadataService.getPropertyTypes(null, null, -1, -1);
		}
		catch (Exception e)
		{
			String msg = "Error occurred getPropertyTypeNames()";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		
		String[] names = new String[l.size()];

		int j = 0;
		for (Iterator i = l.iterator(); i.hasNext();) {
			names[j] = ((PropertyType)i.next()).getName();
			j++;
		}

		return names;
	}

	public String[] getSupportedPrimitives()
	{
		return PropertyType.SUPPORTED_PRIMITIVES;
	}

	public Map getAllMetadata()
	{
		String[] appTypeNames = this.getApplicationTypeNames();

		Map<String, ApplicationType> allMeta = new HashMap<String, ApplicationType>();

		try {
			for (int i=0; i < appTypeNames.length ; i++)
				allMeta.put(appTypeNames[i], 
						metadataService.getApplicationTypeByName(appTypeNames[i]));
		}
		catch (Exception e)
		{
			String msg = "Error occurred getAllMetadata()";
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
		return allMeta;
	}

	public ApplicationType getApplicationType(String applicationTypeName)
	{
		try
		{
			return metadataService.getApplicationTypeByName(applicationTypeName);
		}
		catch (Exception e)
		{
			String msg = "Error occurred getApplicationType() - AppType: " + applicationTypeName;			
			log.error(msg, e);
			log.error(msg, e);
			throw new CollageException(msg, e);
		}
	}

	public void createApplicationType(String name, String description)
	{
		try {
			metadataService.saveApplicationType(name, description);
		}
		catch (Exception e)
		{
			String msg = "Error occurred createApplicationType() - Name: " + name
			+ ", Description: " + description;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}		
	}

	public void createPropertyType(String name, String description, String primitiveType)
	{
		try {
			PropertyType propType = metadataService.getPropertyTypeByName(name);
	
			// if the property does not exist, simply add it
			if (propType == null) 
			{
				metadataService.savePropertyType(name, description, primitiveType);
			} 
			// this is a strict 'add': if the propertyType already exists and has a
			// different primitive type, we complain without modifying
			else if (!propType.getPrimitiveType().equals(primitiveType))
			{
					String msg = "Unable to create PropertyType '" + name + "'"
							+ " - it already exists and has a different type: " + propType.toString();
					log.error(msg);
					throw new CollageException(msg);
			}
			else if (log.isInfoEnabled()) { 
				log.info("ignore createPropertyType with name '" + name + "' and primitiveType '" 
						+ primitiveType + "': a property with that name/primitive already exists");
			}			
		}
		catch (Exception e)
		{
			String msg = "Error occurred createPropertyType() - Name: " + name
							+ ", Description: " + description
							+ ", PrimitiveType: " + primitiveType;

			log.error(msg, e);
			throw new CollageException(msg, e);
		}				
	}

	public void createOrUpdatePropertyType(String name, String description, String primitiveType)
	{
		try {
			metadataService.savePropertyType(name, description, primitiveType);
		}
		catch (Exception e)
		{
			String msg = "Error occurred createOrUpdatePropertyType() - Name: " + name
						+ ", Description: " + description
						+ ", PrimitiveType: " + primitiveType;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}	
	}

	public void assignPropertyType(
			String applicationTypeName, String entityTypeName, String propertyTypeName)
	{
		if (log.isDebugEnabled()) log.debug("attempting to assign PropertyType '" + propertyTypeName + "'"
				+ " to '" + applicationTypeName + "-" + entityTypeName + "'");

		List<String> errs = null;
		boolean hasErr = false;

		// check that the ApplicationType has been defined
		ApplicationType appType = metadataService.getApplicationTypeByName(applicationTypeName);

		if (appType == null) 
		{
			if (errs == null) errs = new ArrayList<String>();
			errs.add("ApplicationType '" + applicationTypeName + "' was not found");
			hasErr = true;
		}

		// check whether the EntityType exists
		EntityType entityType = metadataService.getEntityTypeByName(entityTypeName);

		if (entityType == null)
		{
			if (errs == null) errs = new ArrayList<String>();
			errs.add("EntityType '" + entityTypeName + "' was not found");
			hasErr = true;
		}

		// check whether the PropertyType exists
		PropertyType propertyType = metadataService.getPropertyTypeByName(propertyTypeName);

		if (propertyType == null) 
		{
			if (errs == null) errs = new ArrayList<String>();
			errs.add("PropertyType '" + propertyTypeName + "' was not found");
			hasErr = true;
		}

		// report on all the errors that we may have encountered
		if (hasErr) {
			String msg = "Unable to assign PropertyType '" + propertyTypeName + "'"
					+ " to '" + applicationTypeName + "-" + entityTypeName + "' errs: " + this.concatenateErrors(errs);
			log.error(msg);
			throw new CollageException(msg);
		}

		// if we got this far we have a live trifecta
		appType.assignPropertyType(entityType, propertyType, 999);
		
		try
		{ 
			metadataService.saveApplicationType(appType);
			
			if (log.isInfoEnabled()) log.info("successfully assigned PropertyType '" + propertyTypeName + "'"
					+ " to '" + applicationTypeName + "-" + entityTypeName + "'");
		}
		catch (Exception e)
		{
			String msg = "Unable to assign PropertyType '" + propertyTypeName + "'"
					+ " to '" + applicationTypeName + "-" + entityTypeName + "' error while saving";
			log.error(msg, e);
			throw new CollageException(msg,e);
		}
	}

	public void unassignPropertyType(
			String applicationTypeName, String entityTypeName, String propertyTypeName)
	{
		if (log.isDebugEnabled()) log.debug("attempting to unassignPropertyType '" + propertyTypeName + "'"
				+ " from '" + applicationTypeName + "-" + entityTypeName + "'");

		if (this.isPropertyTypeAssigned(applicationTypeName, entityTypeName, propertyTypeName)) 
		{
			ApplicationType appType = metadataService.getApplicationTypeByName(applicationTypeName);
			if (appType.unassignPropertyType(entityTypeName, propertyTypeName))
			{
				metadataService.saveApplicationType(appType);
				if (log.isInfoEnabled()) log.info("successfully unassigned '" + propertyTypeName + "' from '" + applicationTypeName + "-" + entityTypeName + "'");
			}
		}
		else if (log.isInfoEnabled()) {
			log.info("ignored unassignPropertyType '" + propertyTypeName + "'"
				+ " from '" + applicationTypeName + "-" + entityTypeName + "': assignment does not exist");
		}
	}

	public boolean isPropertyTypeAssigned(
			String applicationTypeName, String entityTypeName, String propertyTypeName)
	{
		try {
			ApplicationType appType = metadataService.getApplicationTypeByName(applicationTypeName);
	
			if (appType != null)
				return appType.getPropertyType(entityTypeName, propertyTypeName) != null;
			else
				return false;
		}
		catch (Exception e)
		{
			String msg ="Error occurred isPropertyTypeAssigned() - AppType: " 
				+ applicationTypeName + "EntityType: " + entityTypeName
				+ "PropertyType: " + propertyTypeName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}	
	}

	/** 
	 * The implementation of this method iterates over the ApplicationType names
	 * and the EntityNames in the metadata to retrieve all assignments for a
	 * given PropertyType, in the form of string array tuples where the first
	 * element is the ApplicationType name and the second element is the
	 * EntityType name; the assignments are retrieved from the cache without
	 * refreshing it, call getAllMetadata(boolean) getAllMetadata(true)
	 * beforehand if needed to refresh the cache.
	 * <p>
	 * This implementation is not particularly smart may not be optimal for
	 * large amounts of metadata because it causes all the metadata to be loaded
	 * into the cache. This should not be an issue in typical installations with
	 * very small amounts of metadata; the benefit is that it keeps the model and
	 * hibernate implementation simple
	 * </p>
	 */
	public String[][] getPropertyTypeAssignments(String propertyTypeName)
	{
		try {
			String[] apps     = this.getApplicationTypeNames();
			String[] entities = this.getExtensibleEntityNames();
	
			List<String[]> l = new ArrayList<String[]>();
	
			for (int i=0; i < apps.length ; i++)
			{
				for (int j=0; j < entities.length; j++)
				{
					if (this.isPropertyTypeAssigned(apps[i], entities[j], propertyTypeName))
						l.add(new String[] {apps[i], entities[j]});
				}
			}
			
			if (log.isDebugEnabled()) log.debug("returning " + l.size() + " assignments for '" + propertyTypeName + "'");

			return (String[][])l.toArray(new String[l.size()][]);			
		}
		catch (CollageException ce)
		{
			log.error("Error occurred getPropertyTypeAssignments() - PropertyTypeName: " + propertyTypeName, ce);
			throw ce;
		}
		catch (Exception e)
		{
			String msg = "Error occurred getPropertyTypeAssignments() - PropertyTypeName: " + propertyTypeName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}	
	}

	/*
	 * Removes a PropertyType definition from the system altogether; if
	 * <code>safeDelete</code> is <code>true</code> and the PropertyType is currently
	 * in use by an EntityType, the operation fails; if <code>safeDelete</code>
	 * is <code>false</code> the PropertyType is unassigned from all the
	 * EntityTypes that may be using it, and expunged from the system
	 * unconditionally.  
	 *
	 * Note that this method causes both the PropertyType and the ApplicationType
	 * metadata caches to refresh in their totality (although the refresh occurs
	 * from the hibernate read-write secondary cache, so it should be relatively
	 * fast).  This is a quick and dirty implementation that assumes that
	 * PropertyTypes are not created/deleted intensively; if such were the case,
	 * a more intelligent implementation may be warranted
	 */
	public void deletePropertyType(String propertyTypeName, boolean safeDelete)
	{
		if (log.isDebugEnabled()) log.debug("attempting to delete PropertyType: '" + propertyTypeName + " - safeDelete " + safeDelete);
		String[][] assignments = this.getPropertyTypeAssignments(propertyTypeName);

		if (safeDelete)
		{
			if (assignments.length > 0)
			{
				String msg = "unable to safe-delete PropertyType '" + propertyTypeName + "': it is assigned to: " + concatenateTuples(assignments);
				log.error(msg);
				throw new CollageException(msg);
			}
		}

		// we are doing this to save development time by re-using existing methods;
		// this could be optimized by mapping the assignments to the PropertyType in
		// the hibernate mappings and doing a cascade delete instead of repeatedly
		// deleting PropertyTypes and refreshing the cache
		for (int i=0; i < assignments.length ; i++)
			this.unassignPropertyType(assignments[i][0], assignments[i][1], propertyTypeName);

		try {
			metadataService.deletePropertyTypeByName(propertyTypeName);
		}
		catch (Exception e)
		{
			String msg = "Error occurred deletePropertyType() - PropertyTypeName: " + propertyTypeName;
			log.error(msg, e);
			throw new CollageException(msg, e);
		}

		if (log.isInfoEnabled()) log.info("successfully deleted PropertyType: " + propertyTypeName);
	}

    /** 
     * used to create/assign new PropertyType objects, when the environment
     * variable AutoCreateUnknownProperties is set to 'true'
     */
    public void createOrAssignUnknownProperties(String applicationType, String entityType, Map properties)
    {
        if (log.isDebugEnabled()) log.debug("checking unknown PropertyTypes for: " + applicationType + "-" + entityType);
        for (Iterator i = properties.keySet().iterator(); i.hasNext();)
        {
            String propertyType = (String)i.next();

            if (!metadataService.isBuiltInProperty(entityType, propertyType))
            {
                if (metadataService.getPropertyTypeByName(propertyType) == null)
                    this.createPropertyType(propertyType, propertyType, getPrimitiveType(properties.get(propertyType)));

                if (!this.isPropertyTypeAssigned(applicationType, entityType, propertyType))
                    this.assignPropertyType(applicationType, entityType, propertyType);
            }
        }        
    }
    
    /*************************************************************************/
    /* Private Methods 
    /*************************************************************************/
    
	private String concatenateErrors(List list)
	{
		StringBuffer errs = new StringBuffer();

		for (Iterator i = list.iterator(); i.hasNext();)
		{
			errs.append((String)i.next());
			if (i.hasNext()) errs.append(" - ");
		}
		return errs.toString();
	}


	/* creates a string of the form [tuple00-tuple01][tuple10-tuple11]... */
	private String concatenateTuples(String[][] tuples)
	{
		StringBuffer out = new StringBuffer();

		for (int i=0; i < tuples.length ; i++)
			out.append("[").append(tuples[i][0]).append("-").append(tuples[i][1]).append("]");

		return out.toString();
	}

    /** 
     * Returns the primitive type code corresponding to the class of the
     * object passed, or PropertyType.STRING if another type cannot be
     * discerned
     */
    private static String getPrimitiveType(Object o)
    {
        if (o instanceof Date)
            return PropertyType.DATE;

        else if (o instanceof Boolean)
            return PropertyType.BOOLEAN;
  
        else if (o instanceof Integer)
            return PropertyType.INTEGER;

        else if (o instanceof Long)
            return PropertyType.LONG;

        else if (o instanceof Double)
            return PropertyType.DOUBLE;

        else 
            return PropertyType.STRING;
    }

}
