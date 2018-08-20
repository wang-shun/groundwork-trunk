#!/usr/local/groundwork/perl/bin/perl -w

# 2017-05 DN - v1.0.0 - initial version
# TODO
# - maybe add read/write tests to groundwork perf database to ensure it's working

use 5.24.0;
use warnings;
use strict;
use version;
my $VERSION = version->declare("v1.0.0"); # keep this up to date
use Data::Dumper; $Data::Dumper::Indent = 1; $Data::Dumper::Sortkeys = 1; $Data::Dumper::Terse = 1;
use Getopt::Long;
use File::Copy qw(copy);
use REST::Client;
use JSON;
use Sys::Hostname;
use URI::Escape;
use POSIX qw(strftime);

sub fail;
sub debug;

my %ERRORS = ('UNKNOWN' , '-1', 'OK' , '0', 'WARNING', '1', 'CRITICAL', '2');
my $OKSTATUS = 0;
my ( $debug, $help, $influxdbUrl, $wait, $maxRetries ); # cli vars
my $installDir = "/usr/local/groundwork";
my $versionHeader = 'X-Influxdb-Version'; # expected headed from HEAD /ping call

# ---------------------------------------------------------------------------------
# Best practice here lifted from our RAPID.pm
BEGIN {
    # This is supposed to be the default, but we force it anyway, because we want to ensure that
    # we have the default SSL_cipher_list from IO::Socket::SSL in play (and not whatever Net::SSL
    # provides, if anything), even if somebody has set the PERL_NET_HTTPS_SSL_SOCKET_CLASS
    # environment variable to something else.  See the Net::HTTPS documentation for information
    # on this variable.
    # LWP::UserAgent "use"s LWP::Protocol and calls LWP::Protocol::create() to dynamically reference
    # LWP::Protocol::https if we have an HTTPS connection configured, and that in turn "require"s
    # Net::HTTPS at run time.  While this chain works just fine, the Perl compilation phase can't
    # tell whether Net::HTTPS will be loaded, so it complains about "used only once: possible typo"
    # for the following assignment, which will be the only reference to this variable at compilation
    # time.  We disable that noisy warning about a known singleton use of the variable.
    no warnings 'once';
    $Net::HTTPS::SSL_SOCKET_CLASS = 'IO::Socket::SSL';
}
use IO::Socket::SSL 2.037;  # Make sure IO::Socket::SSL is used in preference to Net::SSL, and use a recent cipher list.

# =================================================================================
main();
# =================================================================================

# ---------------------------------------------------------------------------------
sub main {

    my ( $error ) ;

    # parse cli
    initializeOptions(); 
    
    # check health of influxdb 
    if ( not checkInfluxDBHealth( \$error ) ) { 
        fail $error;
    }

    print "InfluxDB at $influxdbUrl health check ok\n";
    exit $ERRORS{"OK"};

}

# ---------------------------------------------------------------------------------
sub debug {
    # A simple routine to emit a debug message prefixed with a time stamp
    my ( $info ) = @_;
    print "[" . localtime() . "] DEBUG $info\n" if defined $debug;
}

# ---------------------------------------------------------------------------------
sub fail {
    # A simple routine to emit a failure message and quit the program with the error status.
    my ( $error ) = @_;
    print "$error\n";
    exit $ERRORS{"CRITICAL"};
}

# ---------------------------------------------------------------------------------
sub initializeOptions {

    # Process command line arguments and provide help.
    # No args. Returns 1 if ok, fails otherwise.

    my $helpString = "
$0 - version $VERSION

Description

    This plugin script checks to see if InfluxDB is up and healthy. 
    Today this script just does a call to the InfluxDB /ping endpoint
    and regards InfluxDB to be ok if that works.
    In the future, this script can be improved to do a transactional 
    test against one of the GroundWork InfluxDB databases.

    For the /ping call, a 204 HTTP status is expected, and 
    a $versionHeader header is expected to be present in the HEAD response.
    If both are satisfied, then this script resturns OK, else CRITICAL.
  
    By default, the script is designed to work with GroundWork 7.2.0+, 
    which includes InfluxDB. The script uses the url property from 
    $installDir/config/influxdb.properties for connecting to the 
    Influx /ping endpoint. No arguments are required to $0.

    The script can also be used against any InfluxDB 1.2+ instance 
    using the -url option.

Options

    None of these options are required.

    -url <InfluxDB URL> 
       Defines a InfluxDB URL to use instead of the default url 
       taken from $installDir/config/influxdb.properties.
       Eg -url https://myInfluxDBServer:8086

    -retries <#>
         How many times to retry when /api/health is returning 
         non 200 HTTP status. Default is 3.
   
    -wait <#>
         How long to wait in seconds between retries when /api/health 
         is returning non 200 HTTP status. Default is 10 seconds.
 
    -debug 
         Turn on debug output

    -help           
         Show this help

    -version
         Show version of this script

Author
    GroundWork 2017

";

    GetOptions(
        'retries=i'        => \$maxRetries,
        'wait=i'           => \$wait,
        'url=s'            => \$influxdbUrl,
        'debug'            => \$debug,
        'help'             => \$help,
    ) or die "$helpString\n";

    if ( defined $help ) { print $helpString; exit $OKSTATUS; }
    $wait = 10 if not defined $wait;
    $maxRetries = 3 if not defined $maxRetries;

    return 1;
}


# ---------------------------------------------------------------------------------
sub getPropertyValues {

    # Gets property values from config/props files (format: prop = value)
    # Args
    #   A property or config file
    #   A ref to a hash containing a list of properties to get values for of this format { p1 => undef, p2 => undef, .. } )
    #   Some comments to use when constructing messages
    #   An error by ref
    # Success
    #   returns 1
    #   fills in values retrieved for properties, or leaves them as undef if not found
    # Failure
    #   returns 0 and error by ref
    
    my ( $propsFile, $settingsHashRef, $comments, $errorRef ) = @_ ;
    my ( @config, $line, $prop, $value, %seen) ;
    
    # read in the props/config file into mem
    open (PROPS, $propsFile) or do {
	    $$errorRef .= "Failed getting property values from $propsFile ($comments). Reason: Could not open the file '$propsFile' for reading: $!. ";
	    return 0;
    };
    @config = <PROPS>;
    close PROPS;

    # try to retrieve values for each prop
    LINE: for ( my $i = 0; $i <= $#config ; $i++ ) { # loop over each line of the prop file
        PROP: foreach $prop ( keys %$settingsHashRef ) {  # loop over each property to adjust 
            if ( $config[$i] =~ m{ ^\s*$prop\s*=.*$  }xms ) {  # if the line of the prop file matches ...
                $value = $config[$i];
                chomp $value;
                $value =~ s/^\s*$prop\s*=(.*)$/$1/g; # get the value
                $value =~ s/(^\s*|\s*$)//g; # white space strip
                $settingsHashRef->{$prop} = $value; # shove value back into settings hash
            }
        }
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub checkInfluxDBHealth  {

    # Tries to check health of InfluxDB 
    # - do a ping
    # - get some db from groundwork db
    # Returns 1 if ok, 0 and error by ref if not.

    my ( $errorRef ) = @_;

    my ( $userAgent, $restClient, $retry, $HTTPResponseCode, $responseContent, $responseHeader, $JSONResponse) ;
    my ( %influxdbProps, %sslOptions );

    debug "Checking InfluxDB health using ping endpoint";

    %sslOptions = (
        verify_hostname => 1,
        SSL_ca_path     => "/usr/local/groundwork/common/openssl/certs",
        SSL_version     => 'TLSv1_2',
       #SSL_check_crl   => 0, # crl checking not present in this version of the script - will add later if required
    );

    # create an agent object 
    eval {
        $userAgent = LWP::UserAgent->new( agent => "check_influxdb", timeout=> 30, ssl_opts =>\%sslOptions );
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $$errorRef = "\tCannot create a user agent : $@"; 
        return 0;
    }

    # If no -url <url> option was given, try to calculate a default based on local gw influxdb config
    if ( not defined $influxdbUrl ) {
        # Get the influxdb url from the gw props
        %influxdbProps = ( "url" => undef );
        if ( not getPropertyValues( "$installDir/config/influxdb.properties", \%influxdbProps, "Getting GroundWork InfluxDB url property", $errorRef ) ) { 
            return 0;
        } 

        # check properties are all found and set
        if ( not defined $influxdbProps{url} ) {
                $$errorRef = "A required property (url) from $installDir/config/influxdb.properties was missing and the check cannot be performed";
		return 0;
        }
        $influxdbUrl = $influxdbProps{url};
    }

    debug "\tInfluxDB URL set to $influxdbUrl";

    $restClient = REST::Client->new( { host => $influxdbUrl,  
	                               timeout => 10,
	                               useragent => $userAgent } );
	
    if ( not defined $restClient ) { 
       $$errorRef .= "\tCould not create REST::Client object. "; 
       return 0;
    }

    # Try to call the /ping endpoint at the given url, repeating a few times before giving up
    # https://docs.influxdata.com/influxdb/v1.2//tools/api/#ping
    debug "\tChecking InfluxDB pings via $influxdbUrl/ping";
    $retry = 1 ;
    $HTTPResponseCode = -1; # some invalid code
    $responseHeader = undef; 
    while ( $HTTPResponseCode != 204 and $retry <= $maxRetries ) { 
    
	$restClient->HEAD( "/ping" );
        $HTTPResponseCode = $restClient->responseCode();
        $responseHeader = $restClient->responseHeader($versionHeader); 
        if ( $HTTPResponseCode != 204 ) {
            debug "\tHealth check failed - status received was $HTTPResponseCode, expected 204 - try $retry/$maxRetries, trying again in $wait seconds";
            sleep $wait;
        }
        elsif ( not defined $responseHeader ) {	
            debug "\tHealth check failed - didn't receive a $versionHeader header - try $retry/$maxRetries, trying again in $wait seconds";
            sleep $wait;
        }
        $retry++;
    }
    if ( $retry > $maxRetries ) { 
        # During an error, the output may contain html markup chars which will prevent response in status viewer service message from displaying so removing it for now.
        #$$errorRef .= "Health check from $influxdbUrl/ping failed. Last status code: $HTTPResponseCode, last response: $responseContent. ";
        $$errorRef .= "CRITICAL: Health check of InfluxDB at $influxdbUrl failed. Last status code: $HTTPResponseCode. ";
        $$errorRef =~ s/\n/ /g;
        return 0;
    }

    # Try to test that database is at least readable 
    # TBD in future
    
    # No need to decode the response. This simple check is just looking for a status 200 and will error on anything else.
    #$JSONResponse = decode_json( $responseContent ) ;
    
    return 1;

}


