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
package org.groundwork.report.birt.data.oda.ws.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;

public class WSCommand implements IParameterMetaData
{
	public static final String PARAMETER_INDICATOR = "?";
	
	private static final String NULL_STRING_VALUE = "null";
	
	private String applicationType = null;
    private String entityType = null;   
    private EntityFilterList filterList = null;
    
    private List<Object> parameterValues = new ArrayList<Object>(5);
    
    
    public WSCommand (String entityType, String applicationType, EntityFilterList filterList)
    {
        if (entityType == null || entityType.length() == 0)
        {
            throw new IllegalArgumentException("Invalid null/empty web method parameter.");
        }
        
        this.entityType = entityType;
        this.applicationType = applicationType;
        this.filterList = filterList;
    }

    /**
     * @return Returns the params.
     */
    public EntityFilterList filterList() {
        return this.filterList;
    }

    /**
     * @param filterList The filterList
     */
    public void setParams(EntityFilterList filterList)
    {
        this.filterList = filterList;
    }

    /**
     * @return Returns the entityType.
     */
    public String getEntityType() {
        return this.entityType;
    }

    /**
     * @param entityType The entityType to set.
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    /**
     * @return Returns the applicationType.
     */
    public String getApplicationType() {
        return this.applicationType;
    }

    /**
     * @param applicationType The applicationType to set.
     */
    public void setApplicationType(String appType) {
        this.applicationType = appType;
    }     
    
    public Filter getFilter ()
    {
    	if (filterList == null)
    		return null;
    	    	
    	List<EntityFilter> entityFilters = filterList.getFilters();
    	if (entityFilters == null || entityFilters.size() == 0)
    		return null;
    	
    	Filter filter = null;
    	Filter filter2 = null;
    	EntityFilter entityFilter = null;
    	EntityTypeProperty property = null;
    	Iterator<EntityFilter> it = entityFilters.iterator();
    	Object value = null;
    	int parameterIndex = 0;
    	
    	while (it.hasNext())
    	{
    		entityFilter = it.next();
    		
    		property = entityFilter.getProperty();
    		
    		// Replace parameter indicator with actual parameter value
    		value = entityFilter.getValue();
    		
    		// Note:  We ignore the filter if its value is not set 
    		if (property == null || value == null)
    			continue;
    		
    		if (value instanceof String) 
    		{
    			if (WSCommand.PARAMETER_INDICATOR.equals(value))
    			{
    				value = parameterValues.get(parameterIndex++);
    				
        			// If the value is null then we ignore the filter    			
        			if (value == null) continue;
    			}  	
    		}

			// If the value is the string value "null" we also ignore the filter
			if ((value instanceof String) && ((String)value).equalsIgnoreCase(NULL_STRING_VALUE))
			{
				continue;
			}   
			
    		if (filter == null)
    		{	
    			filter = new Filter(property.getDescription(), 
    								entityFilter.getOperator(), 
    								value);
    		}
    		else 
    		{
    			filter2 = new Filter(property.getDescription(), 
    								 entityFilter.getOperator(), 
    								 value);
    			
    			if (FilterOperator.OR.equals(entityFilter.getLogicalOperator()))
    			{    				
    				filter = Filter.OR(filter, filter2);
    			} // Default to AND
    			else 
    			{
    				filter = Filter.AND(filter, filter2);
    			}
    		}
    	}

    	return filter;
    }     
    
	public int getParameterCount() throws OdaException 
	{
		if (filterList == null)
			return 0;
		    	    	
    	List<EntityFilter> entityFilters = filterList.getFilters();
    	if (entityFilters == null || entityFilters.size() == 0)
    		return 0;
    	
    	EntityFilter entityFilter = null;
    	Iterator<EntityFilter> it = entityFilters.iterator();

    	int parameterCount = 0;
    	
    	while (it.hasNext())
    	{
    		entityFilter = it.next();
    		    	
    		// QUESTION MARK INDICATES PARAMETER
    		if (PARAMETER_INDICATOR.equals(entityFilter.getValue()))
    			parameterCount++;    		
    	}

    	return parameterCount;		
	}

	public int getParameterMode(int param) throws OdaException 
	{
		return IParameterMetaData.parameterModeIn;
	}

	public String getParameterName(int param) throws OdaException 
	{
		if (filterList == null)
			throw new OdaException("Invalid parameter index - No Filters defined.");
		
		EntityFilter filter = filterList.getFilters().get(param - 1);
		
		EntityTypeProperty entityProperty = filter.getProperty();
		
		// Use the entity property name as the parameter name (uppercase) 
		return entityProperty.getName().toUpperCase();
	}

	public int getParameterType(int param) throws OdaException 
	{
		if (filterList == null)
			throw new OdaException("Invalid parameter index - No Filters defined.");
		
		EntityFilter filter = filterList.getFilters().get(param - 1);
		
		EntityTypeProperty entityProperty = filter.getProperty();
		
		return DataTypes.getType(entityProperty.getDataType());
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterTypeName(int)
	 */
	public String getParameterTypeName( int param ) throws OdaException 
	{
        int nativeTypeCode = getParameterType( param );
        return Driver.getNativeDataTypeName( nativeTypeCode );
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getPrecision(int)
	 */
	public int getPrecision( int param ) throws OdaException 
	{
        // TODO Auto-generated method stub
		return -1;
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getScale(int)
	 */
	public int getScale( int param ) throws OdaException 
	{
        // TODO Auto-generated method stub
		return -1;
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#isNullable(int)
	 */
	public int isNullable( int param ) throws OdaException 
	{
		// We don't allow nulls
		return IParameterMetaData.parameterNoNulls;
	}
	
	public void addParameterValue(int paramIndex, Object value)
	{
		if (paramIndex < 1 || (paramIndex > (parameterValues.size() + 1)))
		{
			throw new IllegalArgumentException("Invalid parameter value index - " + paramIndex);
		}
		
		// If a string value is equal to the string, "null", or is an empty string we add null as the
		// parameter value.  In effect, this will cause the filter to be ignored.
		if (value != null && (value instanceof String))
		{
			String strValue = (String)value;
			if (strValue.length() == 0 || strValue.equalsIgnoreCase(NULL_STRING_VALUE))
			{
				value = null;
			}
		}		
		
		// Replace current parameter value
		if (paramIndex < parameterValues.size())
		{
			// Incoming parameter index is 1 based
			paramIndex = paramIndex - 1;
			
			parameterValues.remove(paramIndex);
			parameterValues.add(paramIndex, value);
		}
		else {  // Add to end
			parameterValues.add(value);
		}
	}	
}
