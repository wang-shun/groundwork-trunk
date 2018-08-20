/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.gwos;

import java.util.List;

import com.groundwork.agents.vema.api.GWOSEntity;
import org.apache.log4j.Logger;

public class GWOSHostGroup extends GWOSEntity
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GWOSHostGroup.class);
	
	static final String	XML_HEAD	=	"<HostGroup ";

	private String hostGroup	    =	null;
	private String alias		    =	null;
	private String description	    =	null;
	private String applicationType  =   null;

	/**
	 * Default constructor requires Host information
	 * @param serverName
	 * @param serverIP
	 * @param serverMAC
	 */
	public GWOSHostGroup( String hostGroup, String description, String alias, String applicationType )
	{
		this.hostGroup       = hostGroup;
		this.description     = description;
		this.alias           = alias;
		this.applicationType = applicationType;

		setXmlHead( XML_HEAD );

		// Add attributes for Host
		addAttribute( "HostGroup", this.hostGroup );

		if (this.alias != null)
			addAttribute( "Alias", this.alias );
		
		if (this.description != null)
			addAttribute( "Description", this.description );

	}
	
	public String getXMLAddHostgroup()
	{
		StringBuffer xmlOutput = new StringBuffer();
		
		xmlOutput.append( XML_ADAPTER_OPEN );
		{
			xmlOutput.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_ADD, applicationType ) );
			xmlOutput.append( getXML() );
			xmlOutput.append( GWOSEntity.xmlCmdClose() );			
		}
		xmlOutput.append( XML_ADAPTER_CLOSE );

		String output = xmlOutput.toString();
		log.debug( " HostGroup ADD XML:\n" + output + "\n" );
		return output;
	}
	
	public String getXMLModifyHostgroup( List<String> hostList )
	{
		StringBuffer xmlOutput = new StringBuffer();

		xmlOutput.append( XML_ADAPTER_OPEN );
		{
			xmlOutput.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_CLEAR, applicationType ) );
			{
				xmlOutput.append( "<HostGroup HostGroup='" + this.hostGroup + "'>" );
				xmlOutput.append( XML_HOSTGROUP_CLOSE );
			}
			xmlOutput.append( GWOSEntity.xmlCmdClose() );			
			xmlOutput.append( "\n" );
	
			xmlOutput.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_MODIFY, applicationType ) );
			{
				xmlOutput.append( "<HostGroup HostGroup='" + this.hostGroup + "'>\n" );
		
				for( String host : hostList )
					xmlOutput.append( "<Host Host='" + host + "'/>\n" );
		
				xmlOutput.append( XML_HOSTGROUP_CLOSE );
			}
			xmlOutput.append( GWOSEntity.xmlCmdClose() );			
		}
		xmlOutput.append( XML_ADAPTER_CLOSE );
		xmlOutput.append( "\n" );

		String output = xmlOutput.toString();
		log.debug( " HostGroup MODIFY XML:\n" + output + "\n" );
		return output;
	}
	
	public String getXMLDeleteHostgroup()
	{
		StringBuffer xmlOutput = new StringBuffer( );
		
		xmlOutput.append( XML_ADAPTER_OPEN );
		{
			xmlOutput.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_DELETE, applicationType ) );
			xmlOutput.append( getXML() );
			xmlOutput.append( GWOSEntity.xmlCmdClose() );			
		}
		xmlOutput.append( XML_ADAPTER_CLOSE );
		
		String output = xmlOutput.toString();
		log.debug( " HostGroup DELETE XML:\n" + output + "\n" );
		return output;
	}

	public String getHostGroupName()
	{
		return hostGroup;
	}
}
