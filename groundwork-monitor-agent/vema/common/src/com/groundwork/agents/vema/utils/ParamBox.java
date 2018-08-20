package com.groundwork.agents.vema.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

// one of the things I'm trying to do ( 130228.rlynch ) is to
// standardize on the usage of various parameter "identifiers".
//
// how to do this remains ... somewhat mysterious
//
// for instance:
//
//      host                  = eng-rhev-m-1.groundwork.groundworkopensource.com
// ==?  uri                     eng-rhev-m-1.groundwork.groundworkopensource.com
// 
// but what about 
//      url                     https://eng-rhev-m-1.groundwork.groundworkopensource.com:443
//
// --------------------------------------------------------------------------
// allowing unqualified parameters... and throwing exceptions
// --------------------------------------------------------------------------
// ... I decided that no parameters can be added unless "registered"
// ... and that there would be a number of preregistered ones, below. 
// ... but that it would be easy to register more.
// ... keeping a check on the programmer, for misusing this, with mysterious
// ... consequences.
// --------------------------------------------------------------------------

public class ParamBox
{
	ConcurrentHashMap<String, 
	ConcurrentHashMap<String, 
	ConcurrentHashMap<String, 
	ConcurrentHashMap<String, String>>>> param = new
	ConcurrentHashMap<String, 
	ConcurrentHashMap<String,
	ConcurrentHashMap<String,
	ConcurrentHashMap<String, String>>>>();

    public final String[] predefined =
    {
        "monitor.check.interval",
        "monitor.coma.interval",
        "monitor.sync.interval",

        "foundation.gwos.port",
        "foundation.gwos.sslenabled",
        "foundation.gwos.host",
        "foundation.ws.baseuri",
        "foundation.ws.hostgroupname",
        "foundation.ws.hostname",
        "foundation.ws.password",
        "foundation.ws.user",

        "vemamulti.vema.{name}.sslenabled",     // "on" or "true" or "1"
        "vemamulti.vema.{name}.host",           // eng-rhev-m-1
        "vemamulti.vema.{name}.fqhost",         // eng-rhev-m-1.groundwork.groundworkopensource.com
        "vemamulti.vema.{name}.type",           // rhev
        "vemamulti.vema.{name}.baseuri",        // api
        "vemamulti.vema.{name}.realm",          // internal
        "vemamulti.vema.{name}.user",           // admin
        "vemamulti.vema.{name}.password",       // m######rite
        "vemamulti.vema.{name}.certsfile",      // /usr/local/groundwork/keys/cacerts
        "vemamulti.vema.{name}.certspass",      // changeit
        "vemamulti.vema.{name}.protocol",       // https
        "vemamulti.vema.{name}.port",           // 443

        "vema.api.sslenabled",                  // "on" or "true" or "1"
        "vema.api.host",                        // eng-rhev-m-1
        "vema.api.fqhost",                      // eng-rhev-m-1.groundwork.groundworkopensource.com
        "vema.api.type",                        // rhev
        "vema.api.baseuri",                     // api
        "vema.api.realm",                       // internal
        "vema.api.user",                        // admin
        "vema.api.password",                    // m######rite
        "vema.api.certsfile",                   // /usr/local/groundwork/keys/cacerts
        "vema.api.certspass",                   // changeit
        "vema.api.protocol",                    // https
        "vema.api.port",                        // 443
    };

	ConcurrentHashMap<String, Boolean> qualified = new ConcurrentHashMap<String, Boolean>();

	private static org.apache.log4j.Logger	log  = Logger.getLogger( ParamBox.class );
	
	//constructor
	public ParamBox()
	{
        for( String composite : predefined )
        {
            String parts[] = composite.split( "[.]" );
            // this artificially enforces that 
            // with long dotted parameter strings, that only the first two
            // parts, plus the last, constitute the validation set.
            //
            // The assumption is that any that are between the 2nd and last
            // are variable ( describing many items of a similar class )
            //

            try 
            {
                registerParameter( parts[0], parts[1], parts[ parts.length - 1 ] );
            }
            catch ( Exception e )
            {
                log.info( "\n"
                    + "#parts = " + ( parts != null ? parts.length : "(null)" ) + "\n"
                    + "part[0]= " + ( parts != null && parts.length > 0 ? parts[0] : "" ) + "\n"
                    + "part[1]= " + ( parts != null && parts.length > 1 ? parts[1] : "" ) + "\n"
                    + "part[2]= " + ( parts != null && parts.length > 2 ? parts[2] : "" ) + "\n"
                    + "part[3]= " + ( parts != null && parts.length > 3 ? parts[3] : "" ) + "\n"
                    + "part[4]= " + ( parts != null && parts.length > 4 ? parts[4] : "" ) + "\n"
                    + "part[5]= " + ( parts != null && parts.length > 5 ? parts[5] : "" ) + "\n"
                    + "------------------" + "\n"
                    + "stack:" + "\n" 
                    + e.getStackTrace()[0].toString() + "\n"
					+ e.getStackTrace()[1].toString() + "\n"
					+ e.getStackTrace()[2].toString() + "\n"
					+ e.getStackTrace()[3].toString() + "\n"
					+ e.getStackTrace()[4].toString() + "\n"
					+ e.getStackTrace()[5].toString() + "\n"
					+ e.getStackTrace()[6].toString() + "\n"
					+ e.getStackTrace()[7].toString() + "\n"
                    );
            }
        }
    }
	
    public void registerParameter( String block, String partition, String parameter )
    {
        String composite = block + "." + partition + "." + parameter;

        if( ! qualified.containsKey( composite ) )
            qualified.put( composite, true );
    }

    public boolean allowsParameter( String block, String partition, String subpart, String parameter )
    {
        return allowsParameter( block, partition, parameter );  // special case, don't combine partition.subpart
    }

    public boolean allowsParameter( String block, String partition, String parameter )
    {
        String composite = block + "." + partition + "." + parameter;

        if( qualified.containsKey( composite ) )
             return qualified.get( composite );

        return false;
    }

    public String get( String block, String partition, String parameter )
    {
        return get( block, partition, "", parameter );
    }

	public String get( String block, String partition, String subpart, String parameter )
	{
        if( ( param.get( block )                                                  != null )
        &&  ( param.get( block ).get( partition )                                 != null )
        &&  ( param.get( block ).get( partition ).get( subpart )                  != null )
        &&  ( param.get( block ).get( partition ).get( subpart ).get( parameter ) != null )
        )
            return param.get( block ).get( partition ).get( subpart ).get( parameter ) ;

        else
            throw new IllegalArgumentException( 
                String.format( "no parambox element( %s, %s, %s, %s )", block, partition, subpart, parameter ));
	}

	public void put( String block, String partition, String parameter, String value ) throws IllegalArgumentException
    {
        put( block, partition, "", parameter, value );
    }

	public void put( String block, String partition, String subpart, String parameter, String value ) throws IllegalArgumentException
    {
        String composite = block + "." + partition + "." + parameter;

        if( !qualified.containsKey( composite ) )
            throw new IllegalArgumentException( 
                String.format( "Parameter '%s' not qualified parameter (register() it!)", composite ) );

		if( param.get( block ) == null )
			param.put( block,   new ConcurrentHashMap<String, 
                                    ConcurrentHashMap<String, 
                                    ConcurrentHashMap<String, String>>>() );
		
		if( param.get( block ).get( partition ) == null )
			param.get( block ).put( partition, 
                                new ConcurrentHashMap<String, 
                                    ConcurrentHashMap<String, String>>() );
		
		if( param.get( block ).get( partition ).get( subpart ) == null )
			param.get( block ).get( partition ).put( subpart,
                                new ConcurrentHashMap<String, String>() );
		
		param.get( block ).get( partition ).get( subpart ).put( parameter, value );
	}

	public List<String> getBlocks()
	{
		List<String> list = new ArrayList<String>();
		list.addAll( param.keySet() );
		return list;		
	}
	
	public List<String> getPartitions( String block )
	{
		List<String> list = new ArrayList<String>();

		if( param.containsKey( block ) )
			list.addAll( param.get( block ).keySet() );

		return list;		
	}

	public void delete( String block, String partition, String subpart, String parameter )
	{
		if( param.containsKey(  block  ) )  // must protect with { } blocks!
        {
			if( param.get( block ).containsKey( partition ) )
            {
				if( param.get( block ).get( partition ).containsKey( parameter ) )
                {
                    if( param.get( block ).get( partition ).get( subpart ).containsKey( parameter ) )
                        param.get( block ).get( partition ).get( subpart ).remove( parameter );

                    if( param.get( block ).get( partition ).get( subpart ).isEmpty() )
                        param.get( block ).get( subpart ).remove( partition );
                }
                if( param.get( block ).get( partition ).isEmpty() )
                    param.get( block ).remove( partition );
            }
            if( param.get( block ).isEmpty() )
                param.remove( block );
        }
	}

    public String formatSelf()
    {
        StringBuilder s = new StringBuilder( 1000 );

        s.append( "Parambox with sprinkles:\n" );
        for( String block : param.keySet() )
            for( String partition : param.get( block ).keySet() )
                for( String subpart : param.get( block ).get( partition ).keySet() )
                    for( String parameter : param.get( block ).get( partition ).get( subpart ).keySet() )
                        s.append( String.format( "%-40s: '%s'\n", 
                            block + "." + partition + "." + subpart + "." + parameter, 
                            param.get( block ).get( partition ).get( subpart ).get( parameter ) ) );

        return s.toString();
    }

}
