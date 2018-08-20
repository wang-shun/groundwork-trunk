/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.groundwork.foundation.profiling;

public class MessageDistribution implements java.io.Serializable
{
	// Static table of MessageDistribution - NOTE:  This has to be initialized before the
	// MessageDistribution instances otherwise a null pointer exception will occur
    private static java.util.HashMap<String, MessageDistribution> _table_ = 
    	new java.util.HashMap<String, MessageDistribution>(3);
    
    public static final java.lang.String _EVEN = "even";
    public static final java.lang.String _RANDOM = "random";
    public static final java.lang.String _BURST = "burst";
    
    public static final MessageDistribution EVEN = new MessageDistribution(_EVEN);
    public static final MessageDistribution RANDOM = new MessageDistribution(_RANDOM);
    public static final MessageDistribution BURST = new MessageDistribution(_BURST);
    
    private java.lang.String _value_;
   
    // Constructor
    protected MessageDistribution(java.lang.String value) 
    {
        _value_ = value;
        _table_.put(_value_, this);
    }
    
    public java.lang.String getValue() { return _value_;}

    public static MessageDistribution fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
    	MessageDistribution enumeration = (MessageDistribution)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    
    public static MessageDistribution fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    
    public int hashCode() { return toString().hashCode();}
    
    public java.lang.String toString() { return _value_;}
}