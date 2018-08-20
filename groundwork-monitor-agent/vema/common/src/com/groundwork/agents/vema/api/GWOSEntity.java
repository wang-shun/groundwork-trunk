/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.api;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import com.groundwork.agents.vema.gwos.EntityAttribute;

/**
 * @author rruttimann@gwos.com
 * Created: Jun 22, 2012
 */
public class GWOSEntity 
{
    public               String xmlHead              = "undefined";
    public               String XML_TAIL             = " />";

//  public  static final String XML_CMD_ADD          = "<Command Action='ADD'    ApplicationType='VEMA'>";
//  public  static final String XML_CMD_MODIFY       = "<Command Action='MODIFY' ApplicationType='VEMA'>";
//  public  static final String XML_CMD_DELETE       = "<Command Action='REMOVE' ApplicationType='VEMA'>";
//  public  static final String XML_CMD_CLEAR        = "<Command Action='CLEAR'  ApplicationType='VEMA'>";

    public  static final String XML_ADAPTER_OPEN     = "<Adapter Session='4159924500' AdapterType='SystemAdmin'>";
    public  static final String XML_ADAPTER_CLOSE    = "</Adapter>";
    public  static final String XML_HOST_HEAD        = "<Host ";
    public  static final String XML_HOSTGROUP_CLOSE  = "</HostGroup>";
 
    public  static final String ACTION_ADD           = "ADD";
    public  static final String ACTION_MODIFY        = "MODIFY";
    public  static final String ACTION_DELETE        = "DELETE";    
    public  static final String ACTION_CLEAR         = "CLEAR";    
    
    /* Keep a list of Host attributes */
    private   ConcurrentHashMap<String,EntityAttribute> mapEntityAttributes 
        = new ConcurrentHashMap<String,EntityAttribute>(10);
    
    public boolean addAttribute(String name, String value)
    {
        if (name == null || value == null) 
            return false;

        this.mapEntityAttributes.put(name, new EntityAttribute(name, value));

        return true;
    }

    public void delAttribute( String name )
    {
        if( this.mapEntityAttributes.containsKey( name ) )
            this.mapEntityAttributes.remove( name );
    }

    
    public String getXML()
    {
        StringBuffer result = new StringBuffer( 250 );

        result.append(getXmlHead());

        for( EntityAttribute ea : this.mapEntityAttributes.values() )
            result.append( ea.getName() + "='" + ea.getValue() + "' " );

        result.append(XML_TAIL);

        return result.toString();
    }

    public static String xmlCmdOpen( String action, String applicationType )
    {
        StringBuffer sb = new StringBuffer( 250 );

        sb.append( "<Command" );
        sb.append( " Action='"          + action          + "'" );
        sb.append( " ApplicationType='" + applicationType + "'" );
        sb.append( " >" );

        return sb.toString();
    }

    public static String xmlCmdClose()
    {
        return "</Command>";
    }

    
    /**
     * Setters and getters
     */
    
    public  String getXmlHead()               { return xmlHead;         } 
    public void    setXmlHead(String xmlHead) { this.xmlHead = xmlHead; }
}
