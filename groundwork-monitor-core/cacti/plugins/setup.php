<?php

// Revs
// 0.1.8  9/25/15  : first working version
// 0.1.9  10/23/15 : adding perf timing stats ; setting post perfdata value to 0 if value was null/empty/not set
// 0.2.0  10/27/15 : decreased CURLOPT_TIMEOUT to 30 from 300 after performance testing it, and wanting to avoid overruns 
//                   ie condition where > 1 poller.phps are running; increased detail for curl_exec case returning 0 - this
//                   might be useful for ssl-cert related issues.  Also added CURLOPT_FOLLOWLOCATION, and CURLOPT_MAXREDIRS.
//
// TO DO
// - add suppression at info and debug logging level of errors relating to services/hosts not being present ?
// - CURLOPT_SSL_VERIFYPEER and other ssl settings currently apply to all endpoints. 
//   This might need to change to allow different settings per endpoint. Moving this setting to 
//   each cacti feeder's endpoint config might be a solution at that point. Wait and see.

# ------------------------------------------------------------------------------------------------
// required plugin function
function plugin_gwperf_install () {
    global $config;
    api_plugin_register_hook('gwperf', 'config_settings', 'gwperf_config_settings', 'setup.php');
    api_plugin_register_hook('gwperf', 'poller_top',      'start_up',        'setup.php');
    api_plugin_register_hook('gwperf', 'poller_bottom',   'finish_up',       'setup.php');
    # No longer need to hook in with poller_output, since using thold data directly
    #api_plugin_register_hook('gwperf', 'poller_output',   'gwperf_write',    'setup.php'); 
}

# ------------------------------------------------------------------------------------------------
// required plugin function
function plugin_gwperf_uninstall () {
    // Do any extra Uninstall stuff here.
    // At this time, nothing to do here.
}

# ------------------------------------------------------------------------------------------------
// required plugin function
function gwperf_version () {
    return plugin_gwperf_version();
}

# ------------------------------------------------------------------------------------------------
// required plugin function
function plugin_gwperf_version () {
    return array(    
            'name'      => 'gwperf',
            'version'   => '0.2.0',
            'longname'  => 'GroundWork Performance Data Feeder',
            'author'    => 'GroundWork',
            'homepage'  => 'http://gwos.com',
            'email'     => 'info@gwos.com',
            'url'       => 'http://gwos.com/'
    );
}

# ------------------------------------------------------------------------------------------------
// required plugin function
function plugin_gwperf_check_config () {
    // Here we will check to ensure everything is configured
    // Nothing to do currently.
    return true; 

}

# ------------------------------------------------------------------------------------------------
function plugin_gwperf_upgrade () {
    // Here we will upgrade to the newest version
    // Nothing to do currently.
    return false;
}


# ------------------------------------------------------------------------------------------------
#function gwperf_write( $rrd_update_array ) { # use this is associating with poller_output hook
function gwperf_write( $error_ref, $took_ref ) {
    # Writes thold measurement data out to the GroundWork perfdata api.
    # This plugin constrains to just thresholded things.
    # The thold plugin has already done the work of getting the measurement
    # so that there's no need to go get it out of the rrd data after all.
    # The thold plugin also has the service name that the cacti feeder uses, and the hostname.
    # Just need to use the same query as the cacti feeder.
    # Also : if this routine is associated with poller_output, it will be called once per host in the rrd_update_array dataset.
    # Args : 
    #  - ref to an error which will be populated with errors along the way, or just be null. 
    #  - ref to var that will contain how long the function took to send all data 
    #  - also globally accesses data and settings via $config.
    # Returns: true on success, false on any errors (other than an endpoint not having a token set)

    global $config, $settings;
    $error_ref = ""; # will contain error detail, accumulated across all endpoints processing

    # For future reference leaving this stuff in for now about rrd_update_array, if this routine is associated
    # with the poller_output hook.
    #
    # The rrd_update_array looks like this :
    # (
    #     [/usr/local/groundwork/cacti/htdocs/rra/localhost_proc_7.rrd] => Array
    #         (
    #             [local_data_id] => 7
    #             [times] => Array
    #                 (
    #                     [1441378801] => Array
    #                         (
    #                             [proc] => 205
    #                         )
    #                 )
    #         )
    #    [/usr/local/groundwork/cacti/htdocs/rra/localhost_load_1min_5.rrd] => Array
    #         (
    #             [local_data_id] => 5
    #             [times] => Array
    #                 (
    #                     [1441378801] => Array
    #                         (
    #                             [load_1min] => 0.00
    #                             [load_5min] => 0.01
    #                             [load_15min] => 0.00
    #                         )
    #                 )
    #         )
    # )
    # 
    # Add a hostname key to every entry first - maybe want to review this
    # $rrd_data = $rrd_update_array; # preserve the original just in case it's used again later
    # get_rrd_hostnames( &$rrd_data );
    #
    # The rrd_data data structure now looks like this :
    # (
    #     [/usr/local/groundwork/cacti/htdocs/rra/localhost_proc_7.rrd] => Array
    #         (
    #             [hostname] => 'some host name' <<< *** ADDED ***
    #             [local_data_id] => 7
    #             [times] => Array
    #                 (
    #                     [1441378801] => Array
    #                         (
    #                             [proc] => 205
    #                         )
    #                 )
    #         ) ...
    # )
    #
    # gw_log( "RRDFILE => HOST => CACTI_LOCAL_DATA_ID => TIME => DS/METRIC => VALUE", 2 );
    # foreach ( $rrd_data as $rrd => $data ) {  # loop over top level .rrd file name entry
    #     $hostname = $rrd_data[ $rrd ] ['hostname'];
    #     $rrdfile = $rrd;
    #     $local_data_id = $rrd_data[ $rrd ] ['local_data_id'];
    #     foreach ( $data['times'] as $timekey => $timevalue ) { 
    #         foreach ( $rrd_data[ $rrd ]['times'][$timekey] as $dsname => $dsvalue ) { # loop over datasources in that time block
    #             gw_log( "$rrd => $hostname => $local_data_id => $timekey => $dsname => $dsvalue", 2 );
    #         }
    #         
    #     }
    #  }

    # Don't try to process if running against an unsupported version of cacti
    # Notes: 
    # - this linkage is left in from when poller_output hook was connected to this function. 
    # - it's left in for future use if that linkage is re-established. For now, start_up() calls this function.
    if ( ! $config['gw_api_requirements_met'] ) { 
        gw_log("Cacti system requirements not met - no sending of data to GroundWork REST API will happen.", 1 );
        return false;
    }

    # if there's no thold data to process, just get out of here
    $amount_of_thold_data = count( $config['gwperf_thold_data'] );
    if ( $amount_of_thold_data == 0 ) { 
        gw_log("No thold data to process - nothing to do", 1);
        return true;
    }

    # Loop over each endpoint and send to it
    $bundle_size = read_config_option('bundle_size');
    $took_ref = 0;
    foreach ( $config['gw_api_endpoints'] as $endpoint_name => $endpoint_config ) {
        
        # Only proceed if there as an auth token set
        if ( ! isset( $endpoint_config['token'] ) ) { 
            gw_log("ERROR Cannot send to endpoint '$endpoint_name' because no authentication token was defined - skipping sending data to this endpoint", 1);
            $error_ref .= "ERROR Cannot send to endpoint '$endpoint_name' because no authentication token was defined - skipping sending data to this endpoint. ";
            continue;
        }

        gw_log("Sending $amount_of_thold_data threshold measurements to GroundWork REST Performance API for endpoint '$endpoint_name', in bundles of $bundle_size ...", 1 );

        # POST url for logging in
        $url = $endpoint_config['foundation_rest_url'] . '/perfdata' ; 

        # app type comes via the cacti feeder config
        $apptype = $endpoint_config['apptype'] ;

        # Do n-ary splitting of the entire data set to achieve api bundling for efficiency.
        # Copy the original data, don't splice it - will need it for subsequent endpoint processing
        $the_data = $config['gwperf_thold_data'];
        $endpoint_took = 0; # start timing post across all bundles
        while ( $bundle = array_splice( $the_data, 0, $bundle_size ) ) {

            gw_log("Processing bundle of " . count( $bundle ) . " measurements ...", 2);

            # start construction of the json bundle that will be sent to the /perfdata api
            $api_bundle = Array( "perfDataList" => Array() );
            $now = time();

            foreach ( $bundle as $thold_data ) {  
    
                $hostname    = $thold_data['description'];
                $servicename = $thold_data['name'];
                //$value       = $thold_data['lastread'];
                # If the value is 0, '', null - set it to 0
                $value = ( is_null( $thold_data['lastread'] ) || (! isset( $thold_data['lastread'] )) || empty( $thold_data['lastread'] ) ) ? 0 : $thold_data['lastread']; # 0.1.9
                
                # For label, the API docs say 'A text string label describing this service name that is attached to the plotted line graph'.
                # For now will set it to the service name with the '[dsname]' bit removed.
                #$label       = preg_replace('/^.*\[(.*)\]\s*$/', '${1}', $hostname ); # extracts the [DSNAME] bit from the end as a label 
                $label       = preg_replace('/\[.*\]$\s*/', '', $hostname); # removes the [dsname] bit from the cacti thold service name.
    
                array_push( $api_bundle['perfDataList'], Array(
                                                                "appType"     => $apptype,
                                                                "label"       => $label,
                                                                "serverName"  => $hostname,
                                                                "serverTime"  => $now,
                                                                "serviceName" => $servicename,
                                                                "value"       => $value
                                                        )
                );

            }
    
            gw_log("Bundle : size :" . count($api_bundle['perfDataList']) . ", content: " . print_r( $api_bundle, 1), 3);

            # encode the data into JSON
            $json = json_encode($api_bundle);
            # Be nice to use json_last_error() here but PHP version 5.2 doesn't support it. Instead will rely on   
            # the api POST to throw an error and catch it there.

            gw_log("JSON encoded : $json", 3 );
    
            # Send the encoded data to the GW api
            $result = null;
            $took = 0;
            $post_status = gw_api_post( Array( 
                                            'url' => $url,  
                                            'encoded_instructions' => $json,
                                            'headers' => array( 
                                                                "gwos-api-token: " . $endpoint_config['token'],
                                                                "gwos-app-name: " .  $config['gw_api_requestor'],
                                                                'Content-Type: application/json',
                                                                'Accept: application/json' # for results in json, not xml
                                                        )
                                        ),
                                        &$result,
                                        &$took
            ) ;
            $endpoint_took = $endpoint_took + $took;
            
            if ( $post_status == 200 ) {
                gw_log("POST RESULT OK - status code = 200, result = " . print_r( $result, 1 ), 3); 
                # Still need to see what the result was tho - it could contain valid failures
                $json_decoded = json_decode($result, true); # the true is to decode into an ass. array
                if ( $json_decoded['failed'] > 0 ) {
                    # this might turn out to be too noisey - try it and see
                    gw_log("ERROR Failures were detected during posting of data to the perfdata api. Detail : " . print_r($result,1), 1);
                }
            }
            else {
                $error_ref .= "Status code '$post_status', result : " . print_r( $result, 1 );
                #gw_log("ERROR posting data to GroundWork API. Detail : $error_ref", 1);
            }

        } // bundled send loop

        # timinig info for all bundles
        gw_log("TIMING : it took $endpoint_took seconds to POST all bundles to endpoint $endpoint_name", 2);  
        $took_ref += $endpoint_took;
    
    } // endpoint loop

    if ( $error_ref == "" ) { 
        return true;
    }
    else { 
        return false;
    }

}

# ------------------------------------------------------------------------------------------------
function get_rrd_hostnames( $rrd_data ) {
    # Not currently in use.
    # Takes rrd data array, by ref, and inserts hostnames for each entry 
    foreach ($rrd_data as $rrd => $val) {
        foreach ($val as $detailk => $detailv ) {
            $rrd_data[ $rrd ] ['hostname']  = get_rrd_hostname( $rrd_data[$rrd]['local_data_id'] );
        }
    }
}

# ------------------------------------------------------------------------------------------------
function get_rrd_hostname( $local_data_id ) {
    # Not in use.
    # Gets a hostname for a given cacti local data id 
    $hostname = db_fetch_cell( "SELECT host.description
                                FROM   data_local, data_template_data, host
                                WHERE  data_local.id = data_template_data.local_data_id
                                AND    data_local.host_id = host.id
                                AND    data_local.id = $local_data_id" );
    return $hostname;
}

# ------------------------------------------------------------------------------------------------
function get_thold_data ( $sql_ref, $res_ref, $took_ref ) {

    # Takes some sql, by ref, and runs it and gets data back and puts results into res_ref.
    # Args
    #   - sql_ref - the sql, by ref
    #   - res_ref - the results, populated here, by ref.
    #   - took - how long it took to get the data in this function, calculated here, by ref.
    # Returns
    #   always true cos little to do re. error handling - this routine doesn't return status, and reports its own error, possibly even exits.

    $took_ref = -microtime(true); # 0.1.9
    $res_ref = db_fetch_assoc( $sql_ref ) ;
    $took_ref += microtime(true); # 0.1.9
    return true;
}

# ------------------------------------------------------------------------------------------------
function get_api_auth_token( $endpoint_config, $token_result_ref  ) {

    # Gets an auth token for an endpoint
    # Args
    # - $endoint_config = Array( 'foundation_rest_url' => "url", 'webservices_user' => "user", 'webservices_password' => "password" );
    # - ref to token result
    # Returns
    # - the http status of the POST
    # - populated token on success, on null token on failure

    global $config;

    # POST url for logging in
    $url = $endpoint_config['foundation_rest_url'] . '/auth/login' ; 
    $encoded_instructions =
               "gwos-app-name=" . $config['gw_api_requestor']  . '&'
             . "user="          . base64_encode( $endpoint_config['webservices_user'] ) . '&' 
             . "password="      .                $endpoint_config['webservices_password'];
    $result = null;
    $took = 0;
    $post_status = gw_api_post( 
                                Array( 
                                        'url' => $url, 
                                        'encoded_instructions' => $encoded_instructions,
                                        'headers' => array( 'Content-Type: application/x-www-form-urlencoded')
                                ), &$result, &$took ) ;

    # Only case of success is a 200. All others are considered failure.
    if ( $post_status == 200 ) {
        $token_result_ref = $result;
    }
    else {
        $config['gw_api_auth_fail'] = true; # make a note
        gw_log("ERROR Problem getting GroundWork API authentication token",1);
        if ( ! empty( $result ) ) { 
            gw_log("ERROR result was : '$result'",1);
        }
            
        $token_result = null;
    }

    # Return the curl http status
    return $post_status;
    
}

# ------------------------------------------------------------------------------------------------
function gw_api_post( $post_data, $result_ref, $took_ref ) {

    # Post's some data to a URL
    # Args
    # - Takes an array with :
    #   url => url
    #   encoded_instructions => string of instructions
    #   optional : headers => array( key=val, ... ) TBD
    # - result ref for populating
    # - took ref - this will be populated with just how long the curl execution takes    
    # 
    # Returns 
    #   - Always the http status from the post as a return value
    #   - For status 200  : The result, via $result_ref ;
    #   - For all other status : Null in $result_ref
    # TO DO
    # - check required keys are set to something

    gw_log('POSTing data to GW REST API', 2);
    gw_log('POST data is: ' . print_r($post_data,1) , 3 );

    # first check curl is installed - case : standalone mysql cacti systems
    if  ( ! in_array  ('curl', get_loaded_extensions())) {
        gw_log("ERROR The cURL extension does not appear to be installed or enabled on this server!", 1);
        return false;
    }

    # initialize a curl handle
    $ch = curl_init(); 
    if ( ! $ch ) {
        gw_log("ERROR Unable to create a cURL handle!", 1);
        return false;
    }

    # set the url for the curl ops
    curl_setopt($ch, CURLOPT_URL, $post_data['url']); 

    # set POST'ing mode
    #curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST"); # http://evertpot.com/curl-redirect-requestbody/
    curl_setopt($ch, CURLOPT_POST, 1); 

    # Return output as string, rather than to stdout
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); 

    # Allow some redirection possibly in some cases of ssl enabled GW server 
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true); 
    curl_setopt($ch, CURLOPT_MAXREDIRS, 5);  # don't let it redirect forever tho - default is 5 but spelling it out is good

    # Removed for time being until can figure out better way of capturing output from curl to a log file
    #$curlopt_stderr =  read_config_option('curlopt_stderr');
    #if ( isset( $curlopt_stderr ) and ! empty( $curlopt_stderr ) ) {
    #    curl_setopt($ch, CURLOPT_STDERR, fopen($curlopt_stderr, 'a+') ); # would need to close this again later but worried would interfere with logging or increasing open file handles count
    #    gw_log("cURL standard error location set to $curlopt_stderr ", 2 ); 
    #}
    # Verbosity for debuggering
    #$curlopt_verbose = read_config_option('curlopt_verbose');
    #if ( $curlopt_verbose ) {
    #    curl_setopt($ch, CURLOPT_VERBOSE, true);
    #    gw_log("cURL verbosity enabled", 2 ); 
    #}

    # Set connection timeout
    $curlopt_connecttimeout =  read_config_option('curlopt_connecttimeout');
    if ( isset( $curlopt_connecttimeout ) and ! empty( $curlopt_connecttimeout ) ) {
        curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $curlopt_connecttimeout );
        gw_log("cURL connection timeout set to $curlopt_connecttimeout seconds", 2 ); 
    }

    # Set execution timeout
    $curlopt_timeout = read_config_option('curlopt_timeout');
    if ( isset( $curlopt_timeout ) and ! empty( $curlopt_timeout ) ) { 
        curl_setopt($ch, CURLOPT_TIMEOUT, $curlopt_timeout );  
        gw_log("cURL max execution timeout set to $curlopt_timeout seconds", 2 ); 
    }

    # Set ssl verify peer
    $curlopt_ssl_verifypeer = read_config_option('curlopt_ssl_verifypeer');
#gw_log("---CURLOPT_SSL_VERIFYPEER = '$curlopt_ssl_verifypeer'", 1);
    if ( isset( $curlopt_ssl_verifypeer )  and ! empty($curlopt_ssl_verifypeer) ) { 
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true ); 
        gw_log('cURL option CURLOPT_SSL_VERIFYPEER set to true', 2 ); 
    }
    else {
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false ); 
        gw_log('cURL option CURLOPT_SSL_VERIFYPEER set to false', 2 ); 
    }

    # Set Cert Auth info
    $curlopt_cainfo = read_config_option('curlopt_cainfo');
    if ( isset($curlopt_cainfo) and ! empty($curlopt_cainfo) ) {
        curl_setopt($ch, CURLOPT_CAINFO, $curlopt_cainfo );
        gw_log("cURL option CURLOPT_CAINFO set to '$curlopt_cainfo'", 2 ); 
    }

    # Set Cert Auth path
    $curlopt_capath = read_config_option('curlopt_capath');
    if ( isset($curlopt_capath) and ! empty($curlopt_capath) ) {
        curl_setopt($ch, CURLOPT_CAPATH, $curlopt_capath );
        gw_log("cURL option CURLOPT_CAPATH set to '$curlopt_capath'", 2 ); 
    }

    # Add the POST data
    # TBD check for failure 
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_data['encoded_instructions']); 

    # if headers are provided, use them
    if ( isset( $post_data['headers'] ) ) { 
        gw_log('cURl headers set to :' . print_r( $post_data['headers'] , 1) , 3 ); # trace on this else too noisey for debug
        curl_setopt($ch, CURLOPT_HTTPHEADER, $post_data['headers']); 
    }

    # Do the cURL
    gw_log('Starting curl_exec ...', 2);

    $took_ref = -microtime(true); # start timing
    $result = curl_exec($ch); 
    $took_ref += microtime(true); # end timing

    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    gw_log("curl_exec finished, HTTP status code = $httpcode", 2);
    gw_log("POST result was : '$result'" , 3);

    # Check for failure 
    $curl_error = null;
    if ( curl_errno( $ch )  ) {
        $curl_error = "ERROR A cURL error occurred (HTTP status $httpcode): " . curl_error($ch) ;
        # If the curl_exec failed completely, try to return as much info as to why...
        if ( $httpcode == 0 ) { 
            $curl_fail_info = curl_getinfo($ch);
            gw_log( "ERROR cURL error detail : " . print_r( $curl_fail_info,1), 1  ) ;
        }
    }
    curl_close($ch);

    # If an error occurred doing the curl, relay that back
    if ( isset( $curl_error ) ) { 
        gw_log($curl_error, 1);
        $result_ref = null;
    }
    else {
        $result_ref = $result;
    }
    return $httpcode;

}

# ------------------------------------------------------------------------------------------------
function start_up( ) {

    # The poller_top hook associated function. 
    # This only gets call once via the poller_top hook.
    # It does this  :
    # - checks requirements 
    # - checks cacti and thold plugin versions meet requirements
    # - reads the configs to determine GroundWork REST endpoint(s0 details and creds
    # - gets and stores GW API auth tokens for each endpoint
    # - retrieves a thold query for the version of cacti (same as the cacti feeder)
    # - runs that query and stores the data
    # - writes the queried data to GW perfdata api for each endpoint
    # Args : none
    # Returns : true on success, failure if problem

    $total_elapsed_time = -microtime(true); # time the entire thing

    global $config;

    gw_log("Starting up", 3);

    # Check version of cacti ok and that thold is installed and at sufficient version
    # Notes:
    # - $config['gw_api_requirements_met'] is set here for possible future use. In an earlier verison
    #   of this code, poller_output hook was connected with gwperf_write, so this was the linkage to 
    #   prevent that function from doing anything.
    $cacti_version = ''; $thold_version = '';
    if ( ! cacti_requirements_met( &$cacti_version, &$thold_version ) ) {
        gw_log("ERROR Cacti system requirements not met - no data will be sent to the GroundWork REST API .", 1 );
        $config['gw_api_requirements_met'] = false;
        return false;
    }
    else { 
        $config['gw_api_requirements_met'] = true;
    }

    # set a global gw api requestor name for use later 
    $config['gw_api_requestor'] = 'cacti-plugin-gwperf';

    # Read in the cacti feeder configurations to get endpoint urls and credentials, and store them in $config['gw_endpoints'][<endpoint_name>][...]
    if ( ! get_endpoints_config() ) { 
        gw_log("ERROR an error occurred getting the GroundWork endpoints configuration data", 1);
        # For now, at least try to process any endpoints that were configured ok so don't return false
        # But only if there some! 
        if ( count( $config['gw_api_endpoints'] ) == 0 ) { 
            gw_log("ERROR No endpoints to process - nothing to do", 1); 
            return false ;
        }
    }

    # Get the gw api auth tokens for each endpoint
    # Notes: 
    #  - If using poller_output hook later, it gets call once per host with rrd data.
    #     - In that case, to avoid multiple auth token requests, get gw api auth tokens for each endppoint now 
    #     - This script runs every 5 minutes - GW auth tokens expire after 8 hours.
    foreach ( $config['gw_api_endpoints'] as $endpoint_name => $endpoint_config ) {
        $token = null;
        if ( get_api_auth_token( $endpoint_config, &$token ) == 200 ) {
            $config['gw_api_endpoints'][$endpoint_name]['token'] = $token;
            gw_log( "GW API Authentication token = $token", 2);
        }
        else { 
            gw_log("ERROR A problem occurred getting a GroundWork API authentication token", 1);
            # don't set the token for this endpoint. If it's not set, the gwperf_write will skip the endpoint.
        }
    }

    # Get the query appropriate for this version of cacti
    $sql = "";  $took = null;
    $config['gwperf_thold_data'] = Array();  # see comment below.
    if ( ! generate_query( &$sql, $cacti_version ) ) {
        gw_log("ERROR no suitable thold query was found - no thold data will be retrieved and nothing will be processed",1);
        return false;
    }
    else {
        # Run the generated query to get the thold data which contains all data needed to do the perfdata api calls.
        # The thold data is then stored in the $config global to retain decoupled linkage if in future we need to go back 
        # to calling gwperf_write from some other function other than here ie some other hook.
        if ( ! get_thold_data( &$sql, &$config['gwperf_thold_data'] , &$took ) ) {
            gw_log("ERROR Could not get thold data", 1);
            return false;
        }
        else {
            # Have successfully gotten a suitable query and executed the query without issue, so send the data.
            gw_log("TIMING : it took took $took seconds to execute the SQL query that gathers the thold data", 2);
            $took = null;
            $write_errors = null;
            if ( ! gwperf_write( &$write_errors, &$took ) ) { 
                gw_log("ERROR An error occurred writing the data to the GroundWork perfdata API", 1);
                gw_log("ERROR The error detail : \n$write_errors", 1);
                $total_elapsed_time += microtime(true); # end the timing
                gw_log("TIMING : It took a total of $total_elapsed_time to do everything, including retrieval and posting", 2);
            }
            else {
                gw_log("TIMING : Wrote the data to the GroundWork perfdata API (to all endpoints) in $took seconds", 2);
                $total_elapsed_time += microtime(true); # end the timing
                gw_log("TIMING : It took a total of $total_elapsed_time to do everything, including retrieval and posting", 2);

                return true;
            }
        }
    }

}

# ------------------------------------------------------------------------------------------------
function generate_query( $sql_ref, $cacti_version ) {

    # Generates a query upon to use in get_data() - this data drives the entire feeder.
    # Takes a cacti version string which is used to determine which query to return.
    # Args
    #  - sql ref that will be updated here
    #  - a version of cacti 
    # Returns true on success, false otherwise

    if ( $cacti_version == '0.8.7g' ) {
         $sql_ref = "SELECT
                              thold_data.bl_alert,
                              thold_data.bl_enabled,
                              thold_data.bl_fail_count,
                              thold_data.bl_fail_trigger,
                              thold_data.bl_pct_down,
                              thold_data.bl_pct_up,
                              thold_data.host_id,
                              thold_data.lastread,
                              thold_data.name,
                              thold_data.thold_alert,
                              thold_data.thold_enabled,
                              thold_data.thold_fail_count,
                              thold_data.thold_fail_trigger,
                              thold_data.thold_hi,
                              thold_data.thold_low,
                              host.description,
                              host.status
                  FROM        thold_data
                  LEFT JOIN   host
                  ON          thold_data.host_id=host.id
                  WHERE       thold_enabled='on'  OR  bl_enabled='on'
                  ORDER BY    thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
    }
    elseif ( $cacti_version == '0.8.8c' or $cacti_version == '0.8.8f' ) {
         $sql_ref = "SELECT
                              thold_data.bl_alert,
                              thold_data.bl_fail_count,
                              thold_data.bl_fail_trigger,
                              thold_data.bl_pct_down,
                              thold_data.bl_pct_up,
                              thold_data.host_id,
                              thold_data.lastread,
                              thold_data.name,
                              thold_data.thold_alert,
                              thold_data.thold_enabled,
                              thold_data.thold_fail_count,
                              thold_data.thold_fail_trigger,
                              thold_data.thold_hi,
                              thold_data.thold_low,
                              host.description,
                              host.status
                      FROM    thold_data
                 LEFT JOIN    host
                        ON    thold_data.host_id=host.id
                     WHERE    thold_enabled='on'
                   ORDER BY    thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
    }
    else {
        gw_log( "ERROR No suitable cacti query has been defined yet for Cacti version $cacti_version", 1);
        return false;
    }

    return true;
}

# ------------------------------------------------------------------------------------------------
function cacti_requirements_met( $cacti_version_ref, $thold_version_ref ) {

    # Gets and checks versions of cacti and thold plugin
    # Args :
    #  - cacti version by ref
    #  - thold version by ref
    # Returns : 
    #  - cacti version by ref, if found and requirments met
    #  - thold version by ref, if found and requirments met
    #  - true if all requirements met
    #  - false otherwise    
    
    # Assume all ok and disprove
    $requirements_met = true;  

    # Check for valid version of cacti
    $supported_cacti_versions = Array( '0.8.7g' => 1, '0.8.8c' => 1, '0.8.8f' => 1 );
    $cacti_version_ref = get_cacti_version(); 
    if ( array_key_exists( $cacti_version_ref, $supported_cacti_versions) ) { 
        gw_log( "Cacti version $cacti_version_ref meets requirements", 2 );
    }
    else {
        $keys = ""; foreach ( $supported_cacti_versions as $key => $val ) { $keys .= "$key "; }
        gw_log( "ERROR Cacti version $cacti_version_ref does not meet requirements - supported versions are : $keys", 1 );
        $requirements_met = false;  
    }

    # Check for valid version of thold plugin
    $supported_thold_versions = Array( '0.4.2' => 1, '0.5' => 1 );
    $thold_version_ref = get_thold_version(); 
    if ( array_key_exists( $thold_version_ref , $supported_thold_versions) ) { 
        gw_log( "Thold plugin version $thold_version_ref meets requirements", 2 );
    }
    else {
        if ( $thold_version_ref == '' or ! isset( $thold_version_ref ) ) { 
            gw_log( "ERROR Thold plugin version not found - is it installed ?", 1 );
        }
        else {
            $keys = ""; foreach ( $supported_thold_versions as $key => $val ) { $keys .= "$key "; }
            gw_log( "ERROR Thold plugin version $thold_version_ref does not meet requirements -  supported versions are : $keys", 1 );
        }
        $requirements_met = false;  
    }

    return $requirements_met;
}

# ------------------------------------------------------------------------------------------------
function get_cacti_version( ) {
    # gets and returns cacti version from the cacti db.
    $cacti_version = db_fetch_cell( "SELECT * FROM version" );
    return $cacti_version;
}

# ------------------------------------------------------------------------------------------------
function get_thold_version( ) {
    # Gets the thold plugin version - this returns nothing if that plugin is not installed
    $thold_version = db_fetch_cell( "SELECT version FROM plugin_config WHERE name = 'Thresholds'");
    return $thold_version;
}

# ------------------------------------------------------------------------------------------------
function finish_up( ) {
    
    # finish_up() is the poller_bottom() hook associated function.
    # It's used to release GW api connections.

    global $config;

    gw_log( "Finishing up", 3);

    # POST url for logging out
    foreach ( $config['gw_api_endpoints'] as $endpoint_name => $endpoint_config ) {

        # just quietly move on to the next endpoint if there's no auth token set for this one
        if ( ! isset( $endpoint_config['token'] ) ) { continue ; }

        $url = $endpoint_config['foundation_rest_url'] . '/auth/logout' ; 

        # do a logout : eg POST /api/auth/logout?gwos-api-token=392393939239&gwos-app-name=cloudhub
        $encoded_instructions =
               "gwos-api-token=" . $endpoint_config['token'] . '&' . 
               "gwos-app-name=" . $config['gw_api_requestor'] ;
        
        $result = null; $took = 0;
        $post_status = gw_api_post( Array( 'url' => $url,  'encoded_instructions' => $encoded_instructions  ) , &$result, &$took) ;
        if ( $post_status != 200 ) { 
            gw_log("ERROR failed to log out - status code '$post_status'", 1);
        }
    }

    return true;

}

# ------------------------------------------------------------------------------------------------
function gwperf_config_settings( ) {

    # config_settings hook. Creates a GWPerf tab under Settings and sets settings global for use elsewhere.
    # Takes and returns nothing.

    global $tabs, $settings, $config;

    # Not sure if need this, but thold does it... and it causes this to return immediately so skipping it for now!
    # if (isset($_SERVER['PHP_SELF']) && basename($_SERVER['PHP_SELF']) != 'settings.php') {
    #     return;
    # }

    $tabs['gwperf'] = 'GroundWork Performance Data Feeder';

    $settings['gwperf'] = array(

        'general_header' => array( 'friendly_name' => 'General', 'method' => 'spacer' ),
             'main_config' => array(
                 'friendly_name' => 'Cacti Feeder main configuration',
                 'description' => 'The fully qualified name of the GroundWork Cacti Feeder top-level configuration file.',
                 'method' => 'filepath',
                 'default' => "/usr/local/groundwork/config/cacti_feeder.conf",
                 'max_length' => 1000
             ),
             'bundle_size' => array(
                 'friendly_name' => 'API bundling size',
                 'description' => 'The max number of data elements sent to the GroundWork /api/perfdata API in one go.',
                 'method' => 'textbox',
                 'default' => 50,
                 'max_length' => 1000
             ),
            'logging_level' => array(
                'friendly_name' => 'Debug logging level',
                'description' => 'The level of logging from this plugin. Logging is sent to the cacti.log, with GWPERF tags.',
                'method' => 'drop_array',
                'default' => 1,
                'array' => array(1 => 'Minimal', 2 => 'Debug', 3 => 'Trace')
            ),
        'SSL_header' => array( 'friendly_name' => 'cURL related options - see <a target="_blank" href="http://php.net/manual/en/function.curl-setopt.php">http://php.net/manual/en/function.curl-setopt.php</a> for more info.', 'method' => 'spacer' ),
             'curlopt_connecttimeout' => array(
                 'friendly_name' => 'CURLOPT_CONNECTTIMEOUT',
                 'description' => 'The number of seconds to wait while trying to connect.',
                 'method' => 'textbox',
                 'default' => '30',
                 'max_length' => 4
             ),
             'curlopt_timeout' => array(
                 'friendly_name' => 'CURLOPT_TIMEOUT',
                 'description' => 'The maximum number of seconds to allow cURL functions to execute.',
                 'method' => 'textbox',
                 'default' => '30',
                 'max_length' => 3
             ),
            #'curlopt_stderr' => array(
            #    'friendly_name' => 'CURLOPT_STDERR',
            #    'description' => 'Where to put STDERR from cURL operations. This can be helpful when trying to track down SSL issues with this plugin, when used with CURLOPT_VERBOSE.',
            #    'method' => 'textbox',
            #    'default' => '/usr/local/groundwork/cacti/htdocs/log/cacti.log',
            #    'max_length' => 1000
            #),
            #'curlopt_verbose' => array(
            #    'friendly_name' => 'CURLOPT_VERBOSE',
            #    'description' => 'Make cURL operations verbose. This can be helpful when trying to track down SSL issues with this plugin. Output is sent to CURLOPT_STDERR.',
            #    'method' => 'checkbox',
            #    'default' => 'oin'
            #),
             // Setting the key name to 'CURLOPT_SSL_VERIFYPEER' prevents the default on from working - wtf ?
		    'curlopt_ssl_verifypeer' => array(
			    'friendly_name' => 'CURLOPT_SSL_VERIFYPEER',
			    'description' => 'Verify the peers certificate. Disabling this can be useful in the case of preventing self-signed certificate errors for example.',
			    'method' => 'checkbox',
			    'default' => 'on'
			  ),
             'curlopt_cainfo' => array(
                 'friendly_name' => 'CURLOPT_CAINFO',
                 'description' => 'The name of a file holding one or more certificates to verify the peer with. This only makes sense when used in combination with CURLOPT_SSL_VERIFYPEER.',
                 'method' => 'filepath',
                 'default' => '',
                 'max_length' => 1000
             ),
             'curlopt_capath' => array(
                 'friendly_name' => 'CURLOPT_CAPATH',
                 'description' => 'A directory that holds multiple CA certificates. Use this option alongside CURLOPT_SSL_VERIFYPEER.',
                 'method' => 'filepath',
                 'default' => '',
                 'max_length' => 1000
             ),
                
       );

}

# ------------------------------------------------------------------------------------------------
function gw_log ( $msg, $level ) {

    # A log wrapper. Uses logging_level setting.
    $levels = Array(1 => 'MINIMAL', 2 => 'DEBUG', 3 => 'TRACE');
    $logging_level  = read_config_option('logging_level');
    if ( $level <= $logging_level ) { 
        if ( $level > 1 ) { $msg = "(" . $levels[$level] . ") : $msg"; }
        cacti_log( $msg, false, "GWPERF" );
    }
    return true;
}

# ------------------------------------------------------------------------------------------------
function get_endpoints_config ( ) {

    # Reads properties from various GroundWork configuration files that will determine how/where to 
    # send the data to via the GroundWork API. 
    # In more detail :
    # - reads the cacti feeder main config file, as pointed to by the main_config setting
    # - for each of the endpoint entries in it
    #    - reads the endpoint's name and associated config file
    #    - grabs ws_client_config_file property value from the config file 
    #    - from the ws_client_config_file, grabs username, password and url
    #    - stores all of this under $config['gw_api_endpoints'][$endpoint_name][...]
    # Args : none
    # Returns : true if all ok, false if a problem occurred.
    # 
    # TO DO
    # - dry this all up together with improvements to the get_conf_property_values() api.

	global $config;

    # Get the main config file from the settings, and do a quick sanity spot check on it
    $main_config = read_config_option('main_config');
    if ( ( ! file_exists( $main_config) ) or ( ! is_readable( $main_config) ) ) { 
        gw_log("ERROR Main cacti feeder configuration file '$main_config' does not exist or is not readable");
        return false;
    }
    
	# Get the endpoints from the master config
	$endpoints = Array();
	if ( ! get_conf_property_values ($main_config, "endpoint", &$endpoints ) ) {
        gw_log("ERROR getting endpoint property value(s) from main cacti feeder configuration '$main_config'");
        return false;
    }

    # check if there are no endpoints
    if ( empty( $endpoints ) ) { 
        gw_log("ERROR No endpoints were defined in '$main_config'", 1);
        return false;
    }
	
    # endpoint data will be stored back in $config['gw_api_endpoints'] 
	$config['gw_api_endpoints'] = Array ();

    # loop over each endpoint and built the data up for it
    $props_ok = 1; # a flag to indicate an error occurred anywhere during this loop getting prop values
	foreach ( $endpoints as $endpoint ) {

        # create a new endpoint config array for this endpoint being processed
        # this will get added to $config['gw_api_endpoints'] later.
		$endpoint_config = Array();

		# get the endpoint name and config file 
		$endpoint_e = explode( ':', $endpoint );
		$endpoint_name = $endpoint_e[0];
		$endpoint_conf = $endpoint_e[1];

        # check endpoint conf readable/exists
        if ( ( ! file_exists( $endpoint_conf) ) or ( ! is_readable( $endpoint_conf) ) ) { 
            gw_log("ERROR Endpoint configuration file '$endpoint_conf' does not exist or is not readable");
            $props_ok = 0;
            continue; # skip trying to get info for this endpoint and go on to next one
        }
        
		# get the ws props config file for this endpoint
		$res = Array();
        gw_log("Getting value for property 'ws_client_config_file' from '$endpoint_conf'", 3);
		if ( get_conf_property_values( $endpoint_conf, 'ws_client_config_file', &$res ) ) { 
            $ws_props = $res[0];
        }
        else {
            gw_log("ERROR A problem occurred getting value for property 'ws_client_config_file' from '$endpoint_conf'", 1);
            $props_ok = 0;
        }

        # Get the app type - usually its just CACTI, but it could change.
		$res = Array();
        gw_log("Getting value for property 'app_type' from '$endpoint_conf'", 3);
		if ( get_conf_property_values( $endpoint_conf, 'app_type', &$res ) ) {
		    $endpoint_config['apptype'] = $res[0];
        }
        else {
            gw_log("ERROR A problem occurred getting value for property 'app_type' from '$endpoint_conf'", 1);
            $props_ok = 0;
            continue;
        }

        if ( ( ! file_exists( $ws_props) ) or ( ! is_readable( $ws_props) ) ) { 
            gw_log("ERROR Web services (ws_client_config_file) configuration file '$ws_props' does not exist or is not readable");
            $props_ok = 0;
            continue; # skip trying to get info for this endpoint and go on to next one
        }

		# get the creds and endpoint url props from the ws props file
		$res = Array();
        gw_log("Getting value for property 'webservices_user' from '$ws_props'", 3);
		if ( get_conf_property_values( $ws_props, 'webservices_user', &$res ) ) {
		    $endpoint_config['webservices_user'] = $res[0];
        }
        else { 
            gw_log("ERROR A problem occurred getting value for property 'webservices_user' from '$ws_props'", 1);
            $props_ok = 0;
        }

		$res = Array();
        gw_log("Getting value for property 'webservices_password' from '$ws_props'", 3);
		if ( get_conf_property_values( $ws_props, 'webservices_password', &$res ) ) {
		    $endpoint_config['webservices_password'] = $res[0];
        }
        else { 
            gw_log("ERROR A problem occurred getting value for property 'webservices_password' from '$ws_props'", 1);
            $props_ok = 0;
        }
		
		$res = Array();
        gw_log("Getting value for property 'foundation_rest_url' from '$ws_props'", 3);
		if ( get_conf_property_values( $ws_props, 'foundation_rest_url', &$res ) ) {
		    $endpoint_config['foundation_rest_url'] = $res[0];
        }
        else { 
            gw_log("ERROR A problem occurred getting value for property 'foundation_rest_url' from '$ws_props'", 1);
            $props_ok = 0;
        }

        # put together the endpoint config into the global data structure , regardless of any errors for now
		$config['gw_api_endpoints'][$endpoint_name] = $endpoint_config;
		
	}

    # return failure if any part of the reading of properties was in error
	if ( $props_ok == 1 ) { 
        return true;
    }
    else {
        return false;
    }
	
}

# ------------------------------------------------------------------------------------------------
function get_conf_property_values ( $file, $prop, $results_ref ) {

    # Retrieves a GroundWork config file property value.
    # Args
    #  - config file
    #  - property name
    #  - results ref 
    # Returns
    #  - results ref populated on success with an array of one or more values , and true
    #  - empty results, and false, on error - including prop not found
    # TO DO
    # - Currently this returns an array, even for props that occur just one. It returns an array to support
    #   props that occur > once, like endpoint in main feeder configs.
    # - This should be refactored to support a better api where you can pass in a list of props and 
    #   get them all in one go, rather than prop by prop, each time opening the file, scanning it etc.
    #   For now it'll do.

    # Open the file, or error
    $fh = fopen( $file, "r" );
    if ( ! $fh ) {
	    $e = error_get_last();
        gw_log("ERROR A problem occurred opening configuration file '$file' : " . $e['message'] , 1 );
	    return false;
    }

    # Line at a time, search for the prop word, ignoring comment lines
    while ( ( $line = fgets($fh) ) !== false ) {
	    $line = trim($line); # remove leading and trialing whitespace
        if ( ! preg_match( "/^#/", $line ) ) {  # if the line isn't a comment ...
 		    if ( preg_match( "/\b$prop\b/", $line) ) { # if the line contains the property based on a word boundary search ...
		 	    $val = explode( '=', $line );  # ... get the value
			    $val = $val[1] ; 
			    $val= trim( $val ); # and remove leading/trailing whitespace from it
		        $val = str_replace('"', "" , $val); # remove double quotes if any - might need to back this out later perhaps
		        $val = str_replace("'", "" , $val); # remove single quotes if any - might need to back this out later perhaps
			    array_push( $results_ref, $val); # and finally put the property value in the results array
		    }
	    }
    }

    fclose( $fh ) ;

    # If prop not found, that's an error
    if ( count($results_ref) == 0 ) { 
        return false;
    }
    else {
        return true;
    }

}


?>
