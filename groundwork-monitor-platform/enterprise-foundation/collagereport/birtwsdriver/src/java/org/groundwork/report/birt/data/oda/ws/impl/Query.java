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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * Implementation class of IQuery for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 */
public class Query implements IQuery
{
	private static final String COMMA = ",";
	private static final String SEMI_COLON = ";";
	
    // Data source connection
    private Connection connection = null;   
    
    // Web Service command
    private WSCommand wsCommand = null;
    
    // Maximum number of rows in result set
    private int maxRows = 0;
    
    //The meta data of the result set to be produced.
    //It only available after a statement being prepared
    private IResultSetMetaData resultSetMetaData = null;
    
    public Query (Connection connection)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("Invalid null connection parameter.");
        }
        
        this.connection = connection;
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
     */
    public void clearInParameters() throws OdaException {
        throw new UnsupportedOperationException ();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
     */
    public void close() throws OdaException {
        this.connection = null;
        this.resultSetMetaData = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
     */
    public IResultSet executeQuery() throws OdaException 
    {               
        // fetch data from web service
        WSCommon wsCommon = this.connection.getService();
        
        if (wsCommon == null)
        {
            return new ResultSet(new PropertyTypeBinding[0], this.resultSetMetaData);
        }

        try {
        	// TODO:  Support statistic query, paging parameters and filters
        	WSFoundationCollection wsfndCollection =
        		wsCommon.performEntityQuery(this.wsCommand.getEntityType(), 
        									this.wsCommand.getFilter(),
        									null, 
        									-1, 
        									-1);
        	
			// Data returned in property type binding (name, value pairs);            
            return new ResultSet(wsfndCollection.getPropertyTypeBinding(), this.resultSetMetaData);        
        }
        catch (Exception e)
        {
            Driver.LogError("Error executing entity query.  Make sure web services are available.", e);            
            throw new OdaException("Error executing entity query.  Make sure web services are available - " + e.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
     */
    public int findInParameter(String arg0) throws OdaException {
        throw new UnsupportedOperationException (); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
     */
    public int getMaxRows() throws OdaException 
    {
        return this.maxRows;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
     */
    public void setMaxRows(int maxRows) throws OdaException {
        this.maxRows = maxRows;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
     */
    public IResultSetMetaData getMetaData() throws OdaException {
        return this.resultSetMetaData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
     */
    public IParameterMetaData getParameterMetaData() throws OdaException 
    {
        return this.wsCommand;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
     */
    public SortSpec getSortSpec() throws OdaException {
        throw new UnsupportedOperationException (); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
     */
    public void prepare(String query) throws OdaException {
        
        // Test Connection To Web Service
        testConnection();
        
        // Parse, validate and prepare meta data for the command
        WSCommand command = parseCommand(query);
        
        try {
            this.resultSetMetaData = retrieveMetaData(command);
            this.wsCommand = command;
        }
        catch (Exception e)
        {            
            Driver.LogError("Error preparing metadata.  Make sure web services are available.", e);                         
            throw new OdaException("Error preparing metadata.  Make sure web services are available.");
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setAppContext(java.lang.Object)
     */
    public void setAppContext(Object arg0) throws OdaException
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(int, java.math.BigDecimal)
     */
    public void setBigDecimal(int arg0, BigDecimal arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
     */
    public void setBigDecimal(String arg0, BigDecimal arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;    	
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(int, java.sql.Date)
     */
    public void setDate(int arg0, Date arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(java.lang.String, java.sql.Date)
     */
    public void setDate(String arg0, Date arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(int, double)
     */
    public void setDouble(int arg0, double arg1) throws OdaException
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(java.lang.String, double)
     */
    public void setDouble(String arg0, double arg1) throws OdaException {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(int, int)
     */
    public void setInt(int arg0, int arg1) throws OdaException {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(java.lang.String, int)
     */
    public void setInt(String arg0, int arg1) throws OdaException {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String arg0, String arg1) throws OdaException 
    {
        throw new UnsupportedOperationException (); 
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
     */
    public void setSortSpec(SortSpec arg0) throws OdaException {
        throw new UnsupportedOperationException ();         
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(int, java.lang.String)
     */
    public void setString(int arg0, String arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);    
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(java.lang.String, java.lang.String)
     */
    public void setString(String arg0, String arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(int, java.sql.Time)
     */
    public void setTime(int arg0, Time arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(java.lang.String, java.sql.Time)
     */
    public void setTime(String arg0, Time arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(int, java.sql.Timestamp)
     */
    public void setTimestamp(int arg0, Timestamp arg1) throws OdaException 
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);       
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(java.lang.String, java.sql.Timestamp)
     */
    public void setTimestamp(String arg0, Timestamp arg1) throws OdaException 
    {
    	if (arg1 == null)
    		throw new IllegalArgumentException("Invalid null Timestamp parameter value.");

    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);
        
    }
    
    public void setBoolean(int arg0, boolean arg1) throws OdaException
    {
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(arg0, arg1);
	}

	public void setBoolean(String arg0, boolean arg1) throws OdaException 
	{
    	if (this.wsCommand == null)
    		return;
    	
    	this.wsCommand.addParameterValue(Integer.parseInt(arg0), arg1);	
	}

	public void setNull(int arg0) throws OdaException 
	{
		 throw new UnsupportedOperationException (); 
	}

	public void setNull(String arg0) throws OdaException 
	{
		 throw new UnsupportedOperationException (); 		
	}    

    /*************************************************************************/
    /* PRIVATE METHODS */
    /*************************************************************************/
    
    /**
     * Test whether the Connection is established
     * 
     * @throws OdaException
     *             Once Connection is not established yet
     */
    private void testConnection( ) throws OdaException
    {
        if (this.connection.isOpen( ) == false )
            throw new OdaException("Connection not opened.");
    }
    
    /**
     * Parses the command string and returns an array.
     * Command String Format:
     *  <AppType>,<Entity Name>;<filter 1>;<filter 2>;...
     *  
     * @param command
     * @return Returns string array where the first element is the web method name
     */
    private WSCommand parseCommand(String command) throws OdaException
    {
        if (command == null || command.trim().length() == 0)
        {
            throw new IllegalArgumentException("Invalid null/empty command string parameter.  Please make sure the query string is set up properly.");
        }      
        
        int index = command.indexOf(SEMI_COLON);
        String entityType = null;
        String appType = null;
        String appEntityString = null;
        String filterListString = null;
        EntityFilterList filterList = null;
        
        // No filter specified
        if (index < 0)
        {
        	appEntityString = command;
        }                
        else {                             
        	// Set the currently selected app type and entity type
        	appEntityString = command.substring(0, index);
        }
        
    	int commaIndex = appEntityString.indexOf(COMMA);
    	
    	// No app type defined, defaults to system if null
    	if (commaIndex < 0)
    	{
    		entityType = appEntityString;
    	}
    	else 
    	{
    		appType = appEntityString.substring(0, commaIndex);
    		entityType = appEntityString.substring(commaIndex + 1);
    	}
    		        	
    	if (index > 0 && ((index + 1) < command.length()))
    	{        		
    		filterListString = command.substring(index + 1);
    	}        
                
        // Parse Filter List
        if (filterListString != null && filterListString.length() > 0)
        {
        	// Get properties for entity
        	EntityTypeProperty[] entityProps = this.connection.getEntityProperties(appType, entityType);
        	
        	try {        
        		filterList = new EntityFilterList(entityProps, filterListString);
        	}
        	catch (Exception e)
        	{
        		throw new OdaException("Error parsing query filter list - " + e.toString());
        	}
        }

        return new WSCommand(entityType, appType, filterList); 
    }
        
    private IResultSetMetaData retrieveMetaData (WSCommand command) throws OdaException
    {
    	try {
	        if (command == null) {
	            throw new IllegalArgumentException("Invalid null command argument.");
	        }
	                
	        WSCommon wsCommon = this.connection.getService();
	                
	        WSFoundationCollection wsfCol = 
	        	wsCommon.getEntityTypeProperties(command.getEntityType(), command.getApplicationType(), false);
	        
	        EntityTypeProperty[] properties = wsfCol.getEntityTypeProperty();
	        if (properties == null)
	        	return null;
	                
	        return new ResultSetMetaData(properties);
    	}
    	catch (Exception e)
    	{
    		throw new OdaException("Error retrieving meta data - " + e.toString());
    	}
    }
}
