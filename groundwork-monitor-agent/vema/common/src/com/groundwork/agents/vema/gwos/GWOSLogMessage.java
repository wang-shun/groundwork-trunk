/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.gwos;

import com.groundwork.agents.vema.api.GWOSEntity;

/**
 * @author rlynch
 * Created: May 22, 2013
 */

/**
 * <Adapter Session='28164' AdapterType='SystemAdmin'>
 *     <Command Action='ADD' ApplicationType='CHRHEV'>
 *         <LogMessage                                 // this is the key part
 *             MonitorServerName = 'localhost'
 *             Device            = '{macaddress}'
 *             LastInsertDate    = '2008-04-16 11:17:20'
 *             TextMessage       = 'Hello'
 *             ReportDate        = '2008-04-16 11:17:20'
 *             Severity          = 'OK'
 *             MonitorStatus     = 'UP'
 *             ErrorType         = 'HOST ALERT'
 *             Host              = 'sfrx005' 
 *             ServiceDescription = 'alskjdflaksjdf' />
 *     </Command>
 * </Adapter>
 *
 *
 *
 *
 **/
public class GWOSLogMessage extends GWOSEntity
{
	static final private String	XML_HEAD	           = "<LogMessage ";

    static final private String DEF_HOSTNAME           = "";
    static final private String DEF_MONITORSTATUS      = "";
    static final private String DEF_SEVERITYLEVEL      = "";
    static final private String DEF_MONITORSERVERNAME  = "localhost";
    static final private String DEF_DEVICENAME         = null;
    static final private String DEF_LASTINSERTDATE     = null;
    static final private String DEF_TEXTMESSAGE        = null;
    static final private String DEF_REPORTDATE         = null;
    static final private String DEF_SERVICEDESCRIPTION = null;

    private              String hostName	           = DEF_HOSTNAME;
    private              String monitorStatus          = DEF_MONITORSTATUS;
    private              String severityLevel          = DEF_SEVERITYLEVEL;
    private              String monitorServerName      = DEF_MONITORSERVERNAME;
    private              String deviceName             = DEF_DEVICENAME;
    private              String lastInsertDate         = DEF_LASTINSERTDATE;
    private              String textMessage            = DEF_TEXTMESSAGE;
    private              String reportDate             = DEF_REPORTDATE;
    private              String serviceDescription     = DEF_SERVICEDESCRIPTION;

    public GWOSLogMessage()
    {
		// -------------------------------------------------
		// Add attributes for Service
		// -------------------------------------------------
		addAttribute( "Host",               this.hostName          ); // sfrx005
		addAttribute( "MonitorStatus",      this.monitorStatus     ); // UP / PENDING / DOWN
		addAttribute( "Severity",           this.severityLevel     ); // OK
		addAttribute( "MonitorServerName",  this.monitorServerName ); // localhost
		addAttribute( "Device",             this.deviceName        ); // {MAC address}
		addAttribute( "LastInsertDate",     this.lastInsertDate    ); // 2008-04-16 11:17:20
		addAttribute( "TextMessage",        this.textMessage       ); // Hello
		addAttribute( "ReportDate",         this.reportDate        ); // 2008-04-16 11:17:20
		addAttribute( "ServiceDescription", this.serviceDescription); // sfrx005
    }

	public GWOSLogMessage(
        String HostName,             // 0 position
        String MonitorStatus,        // 1
        String SeverityLevel,        // 2
        String MonitorServerName,    // 3
        String DeviceName,           // 4
        String LastInsertDate,       // 5
        String TextMessage,          // 6
        String ReportDate,           // 7
        String ServiceDescription  ) // 8
	{
        setMonitorServerName(  MonitorServerName );
        setDevice(             DeviceName );
        setLastInsertDate(     LastInsertDate );
        setTextMessage(        TextMessage );
        setReportDate(         ReportDate );
        setSeverity(           SeverityLevel );
        setMonitorStatus(      MonitorStatus );
        setHost(               HostName );
        setServiceDescription( ServiceDescription );

		setXmlHead( XML_HEAD );
	}

	void setMonitorServerName( String s )
	{
        if( s == null )
            if( DEF_MONITORSERVERNAME == null )
                delAttribute( "MonitorServerName" );
            else addAttribute( "MonitorServerName", DEF_MONITORSERVERNAME );
        else addAttribute( "MonitorServerName", s );
	}

    void setDevice( String s )
    {
        if( s == null )
            if( DEF_DEVICENAME == null )
                delAttribute( "Device" );
            else addAttribute( "Device", DEF_DEVICENAME );
        else addAttribute( "Device", s );
    }

    void setLastInsertDate( String s )
    {
        if( s == null )
            if( DEF_LASTINSERTDATE == null )
                delAttribute( "LastInsertDate" );
            else addAttribute( "LastInsertDate", DEF_LASTINSERTDATE );
        else addAttribute( "LastInsertDate", s );
    }

    void setTextMessage( String s )
    {
        if( s == null )
            if( DEF_TEXTMESSAGE == null )
                delAttribute( "TextMessage" );
            else addAttribute( "TextMessage", DEF_TEXTMESSAGE );
        else addAttribute( "TextMessage", s );
    }

    void setReportDate( String s )
    {
        if( s == null )
            if( DEF_REPORTDATE == null )
                delAttribute( "ReportDate" );
            else addAttribute( "ReportDate", DEF_REPORTDATE );
        else addAttribute( "ReportDate", s );
    }

    void setSeverity( String s )
    {
        if( s == null )
            if( DEF_SEVERITYLEVEL == null )
                delAttribute( "Severity" );
            else addAttribute( "Severity", DEF_SEVERITYLEVEL );
        else addAttribute( "Severity", s );
    }

    void setMonitorStatus( String s )
    {
        if( s == null )
            if( DEF_MONITORSTATUS == null )
                delAttribute( "MonitorStatus" );
            else addAttribute( "MonitorStatus", DEF_MONITORSTATUS );
        else addAttribute( "MonitorStatus", s );
    }

    void setHost( String s )
    {
        if( s == null )
            if( DEF_HOSTNAME == null )
                delAttribute( "Host" );
            else addAttribute( "Host", DEF_HOSTNAME );
        else addAttribute( "Host", s );
    }

    void setServiceDescription( String s )
    {
        if( s == null )
            if( DEF_SERVICEDESCRIPTION == null )
                delAttribute( "ServiceDescription" );
            else addAttribute( "ServiceDescription", DEF_SERVICEDESCRIPTION );
        else addAttribute( "ServiceDescription", s );
    }
}
