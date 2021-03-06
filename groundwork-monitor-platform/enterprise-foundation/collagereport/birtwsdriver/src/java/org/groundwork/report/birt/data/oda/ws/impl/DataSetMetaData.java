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

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * Implementation class of IDataSetMetaData for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that assume this custom ODA data set
 * is capable of handling a query that returns a single result set and 
 * accepts scalar input parameters by index.
 * A custom ODA driver is expected to implement own data set specific
 * behavior in its place. 
 */
public class DataSetMetaData implements IDataSetMetaData
{
    private IConnection connection;
    
    public DataSetMetaData (IConnection connection)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("Invalid null connection parameter.");
        }
        
        this.connection = connection;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getConnection()
     */
    public IConnection getConnection() throws OdaException {
        return this.connection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceMajorVersion()
     */
    public int getDataSourceMajorVersion() throws OdaException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceMinorVersion()
     */
    public int getDataSourceMinorVersion() throws OdaException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceObjects(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public IResultSet getDataSourceObjects(String arg0, String arg1, String arg2, String arg3) throws OdaException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceProductName()
     */
    public String getDataSourceProductName() throws OdaException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceProductVersion()
     */
    public String getDataSourceProductVersion() throws OdaException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getSortMode()
     */
    public int getSortMode() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getSQLStateType()
     */
    public int getSQLStateType() throws OdaException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsInParameters()
     */
    public boolean supportsInParameters() throws OdaException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsMultipleOpenResults()
     */
    public boolean supportsMultipleOpenResults() throws OdaException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsMultipleResultSets()
     */
    public boolean supportsMultipleResultSets() throws OdaException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsNamedParameters()
     */
    public boolean supportsNamedParameters() throws OdaException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsNamedResultSets()
     */
    public boolean supportsNamedResultSets() throws OdaException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsOutParameters()
     */
    public boolean supportsOutParameters() throws OdaException {
        return false;
    }    
}
