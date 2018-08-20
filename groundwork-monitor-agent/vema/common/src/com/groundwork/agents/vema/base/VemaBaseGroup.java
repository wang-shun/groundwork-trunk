package com.groundwork.agents.vema.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.GWOSEntity;
import com.groundwork.agents.vema.api.VemaConstants;

public abstract class VemaBaseGroup extends GWOSEntity
{
	private static Logger log = Logger.getLogger(VemaBaseHost.class);

	private String groupName = null;
	private ConcurrentHashMap<String, VemaBaseObject> objectHash = 
		new ConcurrentHashMap<String, VemaBaseObject >();
	
	public List<String> getObjectKeys()
	{
		return new ArrayList<String>( objectHash.keySet() );
	}
	
	public VemaBaseGroup( String groupName )
	{
		this.groupName = groupName;
	}
	
	public String getGroupName()
	{
		return groupName;
	}
	
	public void addObject( String key, VemaBaseObject object )
	{
		if( key == null )           log.error( "key is (null)" );
		else if( object == null )   log.error( "object is (null)" );
		else                        objectHash.put( key, object );
	}
	
	public VemaBaseObject getObject( String key )
	{
		if( key == null )
		{
			log.error( "key = (null)" );
			return null;
		}
		return objectHash.get( key );
	}
}
