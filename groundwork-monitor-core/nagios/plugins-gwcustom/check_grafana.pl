#!/usr/local/groundwork/perl/bin/perl -w

# 2017-05 DN - v1.0.0 - initial version

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
my ( $debug, $help, $grafanaUrl, $wait, $maxRetries ); # cli vars
my $installDir = "/usr/local/groundwork";

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
    
    # check health of grafana using /api/health endpoint
    if ( not checkGrafanaHealth( \$error ) ) { 
        fail $error;
    }

    print "Grafana at $grafanaUrl health check out ok\n";
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

    This plugin script checks to see if Grafana is up and healthy. 
    To do this, it calls the Grafana /api/health endpoint.
    If anything other than an HTTP status of 200 is returned, 
    this plugin returns status $ERRORS{'CRITICAL'}.
    If an HTTP status of 200 is returned, this plugin returns status $ERRORS{'OK'}.
  
    By default, the script is designed to work with GroundWork 7.2.0+, 
    which includes Grafana. The script uses the root_url property from 
    $installDir/grafana/conf/defaults.ini for connecting to the 
    Grafana /api/health endpoint. No arguments are required to $0.

    The script can also be used against any Grafana 4.3.0+ instance 
    using the -url option.

Options

    None of these options are required.

    -url <Grafana URL> 
       Defines a Grafana URL to use instead of the default root_url 
       taken from $installDir/grafana/conf/defaults.ini.

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
        'url=s'            => \$grafanaUrl,
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
sub checkGrafanaHealth  {

    # Tries to check health of Grafana using /api/health
    # Returns 1 if ok, 0 and error by ref if not.

    my ( $errorRef ) = @_;

    my ( $userAgent, $restClient, $retry, $HTTPResponseCode, $responseContent, $JSONResponse ) ;
    my ( %grafanaProps, %sslOptions );

    debug "Checking Grafana health";

    %sslOptions = (
        verify_hostname => 1,
        SSL_ca_path     => "/usr/local/groundwork/common/openssl/certs",
        SSL_version     => 'TLSv1_2',
       #SSL_check_crl   => 0, # crl checking not present in this version of the script - will add later if required
    );

    # create an agent object 
    eval {
        $userAgent = LWP::UserAgent->new( agent => "check_grafana", timeout=> 30, ssl_opts =>\%sslOptions );
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $$errorRef = "\tCannot create a user agent : $@"; 
        return 0;
    }

    # If no -url <url> option was given, try to calculate a default based on local grafana config
    if ( not defined $grafanaUrl ) {
        # Get the grafana protocol and port from the Grafana config file. 
        # TODO Also try to use default admin creds. This might not work if they have been changed but will work 
        # most of the time except possibly for customers connecting to existing grafana instances - different procedure for that which this script doesn't yet support.
        %grafanaProps = ( "root_url" => undef );
        if ( not getPropertyValues( "$installDir/grafana/conf/defaults.ini", \%grafanaProps, "Getting Grafana properties", $errorRef ) ) { 
            return 0;
        } 

        # check properties are all found and set
        if ( not defined $grafanaProps{root_url} ) {
                $$errorRef = "A required property (root_url) from $installDir/grafana/conf/defaults.ini was missing and the check cannot be performed";
		return 0;
        }
 
        # This is being grabbed from the locally installed Grafana that was configured by GroundWork and
        # is expected to be of the format http[s]://hostname/grafana. No port is required since it's proxied via Groundwork.
        # Also, for /api/health, no auth is required.
        $grafanaUrl = "$grafanaProps{root_url}";
    }
    debug "\tGrafana URL set to $grafanaUrl";

    $restClient = REST::Client->new( { host => $grafanaUrl,  # TODO
	                               timeout => 10,
	                               useragent => $userAgent } );
	
    if ( not defined $restClient ) { 
       $$errorRef .= "\tCould not create REST::Client object. "; 
       return 0;
    }

    # try to call the /api/health endpoint at the given url, repeating a few times before giving up
    $retry = 1 ;
    $HTTPResponseCode = -1; # some invalid code
    while ( $HTTPResponseCode != 200 and $retry <= $maxRetries ) { 
    
	$restClient->GET( "/api/health" );
        $HTTPResponseCode = $restClient->responseCode();
        $responseContent = $restClient->responseContent();
        if ( $HTTPResponseCode != 200 )  {
            debug "\tHealth check failed - try $retry/$maxRetries, trying again in $wait seconds";
            sleep $wait;
        }
        $retry++;
    }
    if ( $retry > $maxRetries ) { 
        # For a 503 for example, the output contains html markup chars which will prevent response in status viewer service message from displaying so removing it for now.
        #$$errorRef .= "Health check from $grafanaUrl/api/health failed - expected status only 200. Last status code: $HTTPResponseCode, last response: '$responseContent'. ";
        $$errorRef .= "CRITICAL: Health check of Grafana at $grafanaUrl failed - expected status only 200. Last status code: $HTTPResponseCode. ";
        $$errorRef =~ s/\n/ /g;
        return 0;
    }
    
    # No need to decode the response. This simple check is just looking for a status 200 and will error on anything else.
    #$JSONResponse = decode_json( $responseContent ) ;
    
    return 1;

}


