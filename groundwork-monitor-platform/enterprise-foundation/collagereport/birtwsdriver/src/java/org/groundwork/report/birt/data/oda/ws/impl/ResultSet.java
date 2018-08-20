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

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.PropertyDataType;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;

/**
 * Implementation class of IResultSet for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 */
public class ResultSet implements IResultSet
{
    private PropertyTypeBinding[]  sourceData = null;
    
    private IResultSetMetaData resultSetMetaData = null;
    
    private int cursor = -1;
    
    private int maxRows = 0;
    
    //Boolean which marks whether it is successful of last call to getXXX();
    private boolean wasNull = false;
    
    public ResultSet (PropertyTypeBinding[] sourceData,  IResultSetMetaData metaData)
    {
    	if (sourceData == null)
        {
            this.sourceData = new PropertyTypeBinding[0];
        }
        else
        {
            this.sourceData = sourceData;
        }
        
        this.resultSetMetaData = metaData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#close()
     */
    public void close() throws OdaException {
        this.cursor = 0;
        this.sourceData = null;
        this.resultSetMetaData = null;
        this.maxRows = 0;
    }

    /* Note:  Index returned is one based.
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#findColumn(java.lang.String)
     */
    public int findColumn(String columnName) throws OdaException 
    {
        if (columnName == null || columnName.length() == 0)
        {
            throw new IllegalArgumentException("Invalid null/empty column name.");
        }
        
        IResultSetMetaData metaData = this.getMetaData();
        
        for ( int i = 1; i <= metaData.getColumnCount(); i++)
        {
            if (columnName.trim().equalsIgnoreCase(metaData.getColumnName(i)))
            {
                return i;
            }
        }
        
        throw new OdaException( "Column Not Found." );
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(int)
     */
    public BigDecimal getBigDecimal(int index) throws OdaException 
    {
        isInitialized();
        
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);

    	return getBigDecimal(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(java.lang.String)
     */
    public BigDecimal getBigDecimal(String columnName) throws OdaException
    {
        isInitialized();
        
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
	    Long value = (Long)propertyBinding.getPropertyValue(columnName, PropertyDataType.LONG);
	    
	    if (value == null)
	    	return null;
	    
	    return new BigDecimal(value);
    }

    public boolean getBoolean(int index) throws OdaException {
    	isInitialized();
    	
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);

    	return getBoolean(columnName);
	}

	public boolean getBoolean(String columnName) throws OdaException {
		isInitialized();
		
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	Boolean value = (Boolean)propertyBinding.getPropertyValue(columnName, PropertyDataType.BOOLEAN);
	    
	    if (value == null)
	    	return false;
	    
	    return value.booleanValue();	
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(int)
     */
    public IBlob getBlob(int index) throws OdaException {
        throw new UnsupportedOperationException("Blob types are not supported.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(java.lang.String)
     */
    public IBlob getBlob(String columnName) throws OdaException 
    {
        throw new UnsupportedOperationException("Blob types are not supported.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(int)
     */
    public IClob getClob(int index) throws OdaException {
        throw new UnsupportedOperationException("Clob types are not supported.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(java.lang.String)
     */
    public IClob getClob(String arg0) throws OdaException 
    {
        throw new UnsupportedOperationException("Blob types are not supported.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(int)
     */
    public Date getDate(int index) throws OdaException 
    {
    	isInitialized();
        
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);

    	return getDate(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(java.lang.String)
     */
    public Date getDate(String columnName) throws OdaException 
    {
    	isInitialized();
    	
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	java.util.Date value = (java.util.Date)propertyBinding.getPropertyValue(columnName, PropertyDataType.DATE);
	    
	    if (value == null)
	    	return null;

	    return new Date(value.getTime());
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(int)
     */
    public double getDouble(int index) throws OdaException 
    {
        isInitialized();
        
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);

        return getDouble(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(java.lang.String)
     */
    public double getDouble(String columnName) throws OdaException 
    {
        isInitialized();
        
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	Double value = (Double)propertyBinding.getPropertyValue(columnName, PropertyDataType.DOUBLE);
	    
	    if (value == null)
	    	return Double.MIN_VALUE;  // TODO:  Throw exception?
	    
	    return value.doubleValue();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(int)
     */
    public int getInt(int columnIndex) throws OdaException 
    {
        isInitialized();
        
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(columnIndex);
        
        return getInt(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(java.lang.String)
     */
    public int getInt(String columnName) throws OdaException 
    {
        isInitialized();
        
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	Integer value = (Integer)propertyBinding.getPropertyValue(columnName, PropertyDataType.INTEGER);
	    
	    if (value == null)
	    	return Integer.MIN_VALUE;  // TODO:  Throw exception?
	    
	    return value.intValue();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(int)
     */
    public Time getTime(int index) throws OdaException
    {
    	isInitialized();
    	
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);
    	
    	return getTime(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(java.lang.String)
     */
    public Time getTime(String columnName) throws OdaException 
    {
    	isInitialized();
    	
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	java.util.Date value = (java.util.Date)propertyBinding.getPropertyValue(columnName, PropertyDataType.DATE);
	    
	    if (value == null)
	    	return null;

	    return new Time(value.getTime());    	
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(int)
     */
    public Timestamp getTimestamp(int index) throws OdaException 
    {
    	isInitialized();
    	
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(index);
        
    	return getTimestamp(columnName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(java.lang.String)
     */
    public Timestamp getTimestamp(String columnName) throws OdaException 
    {
    	isInitialized();
    	
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	java.util.Date value = (java.util.Date)propertyBinding.getPropertyValue(columnName, PropertyDataType.DATE);
	    
	    if (value == null)
	    	return null;	    
	    
	    return new Timestamp(value.getTime());    	
    }    

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(int)
     */
    public String getString(int columnIndex) throws OdaException {
       
        isInitialized();
        
        // Look up property
        String columnName = this.resultSetMetaData.getColumnName(columnIndex);
        
        return getString(columnName);               
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(java.lang.String)
     */
    public String getString(String columnName) throws OdaException 
    {
        isInitialized();
        
    	PropertyTypeBinding propertyBinding = this.sourceData[this.cursor];    	       
    	String value = (String)propertyBinding.getPropertyValue(columnName, PropertyDataType.STRING);
	    
	    return value;	    
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
     */
    public IResultSetMetaData getMetaData() throws OdaException {
        return this.resultSetMetaData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getRow()
     */
    public int getRow() throws OdaException 
    {
        isInitialized( );
        return this.cursor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
     */
    public boolean next() throws OdaException 
    {
        if ( (this.maxRows <= 0 ? false : cursor >= this.maxRows - 1) || cursor >= this.sourceData.length - 1)
        {
            this.cursor = 0;
            return false;
        }
        
        this.cursor++;
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
     */
    public void setMaxRows(int maxRows) throws OdaException {
        this.maxRows = maxRows;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#wasNull()
     */
    public boolean wasNull() throws OdaException 
    {
        return this.wasNull;
    }

    /**
     * Test if the cursor has been initialized
     * 
     * @throws OdaException
     *             Once the cursor is stll not initialized
     */
    private void isInitialized( ) throws OdaException
    {
        if ( this.cursor < 0 )
            throw new OdaException("Cursor has not been initialized." );
    }
         
}