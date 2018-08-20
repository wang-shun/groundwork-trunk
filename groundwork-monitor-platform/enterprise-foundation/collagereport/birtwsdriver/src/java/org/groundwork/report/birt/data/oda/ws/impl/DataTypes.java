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

import java.util.HashMap;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.PropertyDataType;

/**
 * Singleton responsible for mapping data type;
 * @author glee
 *
 */
public final class DataTypes
{
    public static final int STRING = 1;
    public static final int INT = 2;
    public static final int DOUBLE = 3;       
    public static final int BOOLEAN = 5;
    public static final int DATETIME = 6;
    public static final int LONG = 7;

    private static HashMap<PropertyDataType, Integer> typeMappings = new HashMap<PropertyDataType, Integer>();

    static
    {
    	typeMappings.put( PropertyDataType.STRING, new Integer(STRING ) );
        typeMappings.put( PropertyDataType.INTEGER, new Integer(INT));
        typeMappings.put( PropertyDataType.DOUBLE, new Integer(DOUBLE ) );
        typeMappings.put( PropertyDataType.BOOLEAN, new Integer ( BOOLEAN ) );
        typeMappings.put( PropertyDataType.DATE, new Integer(DATETIME ) );
        typeMappings.put( PropertyDataType.LONG, new Integer ( LONG ) );
    }

    private DataTypes( )
    {
    }
    
    /**
     * Return the int which stands for the type specified by input argument
     * 
     * @param typeName
     *            the String value of a Type
     * @return the int which stands for the type specified by input typeName
     * @throws OdaException
     *             Once the input arguement is not a valid type name
     */
    public static int getType( PropertyDataType dataType ) throws OdaException
    {
       
        if (typeMappings.containsKey(dataType))
        {
            return  ((Integer)(typeMappings.get(dataType))).intValue();
        }

        throw new OdaException( "Invalid type: " + dataType);
    }

    /**
     * Evalute whether an input String is a valid type that is supported by flat
     * file driver
     * 
     * @param typeName
     * @return
     */
    public static boolean isValidType( String typeName )
    {
        return typeMappings.containsKey(typeName.trim().toUpperCase());
    }
}