package GWDOWN;

# GWDOWN - GroundWork REST API Perl Interface for NOMA
#
# Copyright 2013-2016 RealStuff Informatik AG

# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# Author: Andreas Wenger

use warnings;
use strict;

use Data::Dumper;
use TypedConfig;
use REST::Client;                       # REST API operations
use MIME::Base64;                       # REST API operations
use URI::Escape;                        # For percent-encoding query URIs to the API

use Time::HiRes;
use HTTP::Status qw(status_message);    # For translation of API HTTP status codes
use List::MoreUtils qw(any);            # For subroutine argument checking
use YAML::Syck;                            # For importing of noma configuration
use Log::Log4perl qw(:easy);            # For Logging

use Log::Log4perl qw(get_logger :levels);

my $myLogger = get_logger("GWDOWN");
$myLogger->level($INFO);

# Appenders
my $appender = Log::Log4perl::Appender->new(
    "Log::Log4perl::Appender::File",
    filename => "/usr/local/groundwork/noma/var/noma_downtime.log",
    utf8     => 0,
    layout   => Log::Log4perl::Layout::PatternLayout->new(
        ConversionPattern => "[%d{EEE MMM dd HH:mm:ss yyyy}] %m%n"
    )
);

$myLogger->add_appender($appender);

my $notifierConfig    = '/usr/local/groundwork/noma/etc/NoMa.yaml';

our $conf = LoadFile($notifierConfig);

# my $debug = $conf->{debug}->{logging};
# my $debug_queries = $conf->{debug}->{queries};
# my $debug_file = $conf->{debug}->{file};

my $hostname;
my $username;
my $password;
my $protocol;
my $restport = undef;
my $basepath = undef;
my $foundation_rest_url;
my $requestor;
my $rest_client;
my $rest_api_token = undef;

# The multithreaded => 1 option is REQUIRED from the caller here when we are operating in a
# multi-threaded Perl program (e.g., noma_daemon.pl).  In that context, the JSON package will
# default to using JSON::XS for converting between data structures and JSON.  But JSON::XS
# completely breaks down and fails to operate correctly once the first extra thread is created
# in a Perl program.  So we must switch to using the pure-Perl JSON::PP package instead.

sub new {
    my $class           = shift;
    $requestor          = $_[0];
    my $options         = $_[1];
    my $timeout         = 10;                             # default value
    my $interruptible   = 0;
    my $force_crl_check = 1;                              # default value
    my $multithreaded   = 0;                              # default value
    my $JSON_package    = 'JSON';                         # default value; threaded code must use 'JSON::PP' instead
    my $scrambled       = 0;

    my $self = { };
    eval {
        my %valid_options = (
            access          => 'file',
            multithreaded   => 'boolean'
        );
        
        if ( defined $options ) {
        
            $multithreaded = $options->{multithreaded} if defined $options->{multithreaded};

            if ($options->{access}) {
                eval {
                    my $access_config = TypedConfig->new($options->{access});
        
                    if ( not defined $username ) {
                    $username = $access_config->get_scalar('webservices_user');
                        if ( $username =~ /^\s*$/ ) {
                            $myLogger->error("cannot find a valid \"webservices_user\" field");
                            die "ERROR:  cannot find a valid \"webservices_user\" field\n";
                        }
                    }
        
                    if ( not defined $password ) {
                        $password = $access_config->get_scalar('webservices_password');
                        if ( $password =~ /^\s*$/ ) {
                            $myLogger->error("cannot find a valid \"webservices_password\" field");
                            die "ERROR:  cannot find a valid \"webservices_password\" field\n";
                        }
                    }
        
                    $foundation_rest_url = $access_config->get_scalar('foundation_rest_url');
                    if ( $foundation_rest_url =~ m{^(http(?:s)?)://([-a-zA-Z0-9.]+)(?::(\d+))?((/[^/?&%][^?&%]*)?/api)$}i ) {
                        $protocol = $1 if not defined $protocol;
                        $hostname = $2 if not defined $hostname;
                        $restport = $3 if not defined $restport;
                        $basepath = $4 if not defined $basepath;
                    }
                    else {
                        $myLogger->error("cannot find a valid \"foundation_rest_url\" field", 1);
                        die "ERROR:  cannot find a valid \"foundation_rest_url\" field\n";
                    }
                };
                if ($@) {
                    chomp $@;
                    $@ =~ s/^ERROR:\s+//i;
                    $myLogger->fatal("Cannot read config file $options->{access}:  $@");
                }
            }
        }

        $JSON_package = 'JSON::PP' if $multithreaded;
        if ( not package_is_loaded($JSON_package) ) {
            $myLogger->error("Cannot load the \"$JSON_package\" package.");
            die "ERROR:  Cannot load the \"$JSON_package\" package.\n";
        }

        $basepath = '/api' if not defined $basepath;
        
        $restport = 443 if not defined($restport) and defined($protocol) and $protocol =~ /https/i;
        
        $rest_client = REST::Client->new();

        $self->{JSON_package} = $JSON_package;

        bless $self, $class;

        # We cannot "return" here to the function's caller, because a return within an eval
        # block just exits the eval block, not the function containing the eval statement.
    };
    if ($@) {
        return undef;
    }
    return $self;
}

sub package_is_loaded {
    my $package = shift;
    ## We're careful to use a form of the require that should provide some protection
    ## against Perl-injection attacks through external configuration, though of course
    ## there is no possible protection against what is in the loaded package itself.
    ## Note that we only "require" the package; we don't also "import" its symbols.
    return 0 if ! defined $package || ! $package;
    eval "require $package;";
    if ($@) {
        ## 'require' died; $package is not available.
        return 0;
    } else {
        ## 'require' succeeded; $package was loaded.
        return 1;
    }
}

sub login {
    my $encoded_instructions =
        "gwos-app-name=" . $requestor                     . '&'
          . "user="      . encode_base64( $username, '' ) . '&'
          . "password="  . uri_escape( $password );
    
    # Login to Foundation
    $rest_client->POST( $foundation_rest_url."/auth/login", $encoded_instructions, { "Content-Type" => "application/x-www-form-urlencoded" } );
    
    my $http_response_code = $rest_client->responseCode();
    my $response_content   = $rest_client->responseContent();
    
    if ($http_response_code == 200) {
        $rest_api_token = $response_content;
        $myLogger->info("Login successful.");
        return 1;
    }
    
    $myLogger->info("Login not successful.");
    $myLogger->error("Login ERROR -> Status Code: ".Dumper $http_response_code);
    $myLogger->debug("Login Response: ".Dumper $response_content);
    
    return 0;
}

sub getInDowntime {
    my ( $self, $hostname, $servicedescription ) = @_;

    # first check if logged in
    if ( not $rest_api_token) {
        $myLogger->debug("Try to login...");
        $self->login();
    }
    
    my %outcome = ();
    my @results = ();
    
    my %options = ();
    my @objects = ( { hostName => $hostname, serviceDescription => $servicedescription } );
    
    my $status = undef;
    eval {
        $status = $self->_API_POST( "biz/getindowntime", \@objects, \%options, \%outcome, \@results );
    };
    if ($@) {
        chomp $@;
        $myLogger->error("biz/getindowntime call got exception:  $@");
        $status = undef;
    }
    if ( not $status ) {
        $myLogger->info( 'Could not find host ' . ( defined($servicedescription) ? '/ service ' : '' ) . 'in GroundWork Foundation.' );
        return 0;
    }
    else {
        if (defined $results[0]{'scheduledDowntimeDepth'}) {
            if ( $results[0]{'scheduledDowntimeDepth'} > 0 ) {
                $myLogger->info( 'Host ' . ( defined($servicedescription) ? '/ Service ' : '' ) . 'is in Downtime.' );
                return 1;
            }
        }
        $myLogger->info( 'Host ' . ( defined($servicedescription) ? '/ Service ' : '' ) . 'is not in Downtime.' );
        return 0;
    }
}

sub _API_POST {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;

    my $usage__API_POST = "\nUSAGE API POST\n";

    my $start_time;
    if ( 1 ) {
        $start_time = Time::HiRes::time();
        $myLogger->info( "Starting REST API CALL");
    }
    
    $myLogger->error("Invalid number of args. $usage__API_POST") if @_ != 6;
    $myLogger->error("ERROR:  Undefined arg(s). $usage__API_POST")
        if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;

    # a hash of valid api POST objects -- not surprisingly, mostly the same as api GET objects
    #
    # The use of mixed case in the REST API object naming, plus the special cases for
    # event ack and unack, forces us to create a map here of actual object names to use in
    # the generated JSON, instead of just using $api_method directly.
    my %api_post_objects = (
        "biz/getindowntime"    => "bizHostServiceInDowntimes"
    );

    $myLogger->error("Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    $myLogger->error("Expecting HASH reference")  if ref $options ne 'HASH';
    $myLogger->error("Expecting HASH reference")  if ref $outcome ne 'HASH';
    $myLogger->error("Expecting ARRAY reference") if ref $results ne 'ARRAY';

    # Validate arguments.
    $myLogger->error("unrecognized API command root '$api_method'")
        if not defined $api_post_objects{$api_method};

    my (
        $response_content,             # the POST response content/body
        $http_response_code,           # the POST HTTP response code
        $ref_decoded_response,         # a HASH reference of the decoded JSON version of the response content
        $error_status,                 # stores some info about an error if the response was not 200, or 404
        $json_encoded_instructions,    # JSON encoded instructions hash
        $full_post_url,                # the full post url, starting with /$api_method
    ) = undef;

    # Construct POST url
    $full_post_url = $foundation_rest_url."/$api_method";
    $full_post_url .= '?' . join( '&', map { uri_escape($_) . '=' . uri_escape( $options->{$_} ) } keys %$options ) if %$options;

    $myLogger->trace("Full POST URL:  '$full_post_url'");

    $json_encoded_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode(
        $api_post_objects{$api_method} ? { $api_post_objects{$api_method} => $objects } : $objects
    );
            
    $myLogger->trace("JSON STRING $json_encoded_instructions");

    my %headers = ();
    $headers{ 'GWOS-App-Name' } = $requestor if $rest_api_token;
    
    # Set up Headers for our REST Client
    $headers{ 'Content-Type' } = "application/json";
    $headers{ 'Accept' } = "application/json";

    foreach my $retry ( ( 1, 0 ) ) {
        $myLogger->debug("attempting a POST to the REST API");

        %$outcome = ();
        @$results = ();
    
        # We need to set this on each loop iteration because it might have changed due to re-authorization.
        $headers{'GWOS-API-Token'} = $rest_api_token if $rest_api_token;
        
        $myLogger->trace("current token:  '$rest_api_token'") if $rest_api_token;
        
        $myLogger->trace("headers: ".Dumper(%headers));
    
        # Try to POST a result to the API
        $rest_client->POST( $full_post_url, $json_encoded_instructions, \%headers );
    
        # &$callback(@$callbackargs) if $callback;
    
        $http_response_code = $rest_client->responseCode();
        $response_content   = $rest_client->responseContent();
    
        # With the case of a POST, HTTP status 200 on success of doing the POST
        # does NOT necessarily mean successful object upsert(s).
        if ( $http_response_code == 200 ) {
            $myLogger->trace("POST JSON response:\n$response_content");
            $ref_decoded_response = $self->{JSON_package}->new->utf8(1)->decode($response_content);    # decode JSON back into Perl data structure
            $myLogger->trace("POST JSON response decoded back into Perl structure (Dumper() output):\n" . Dumper($ref_decoded_response));
            %$outcome = %{$ref_decoded_response};                      # copy the decoded response into the hash i.e., not a copy of the ref
            @$results = @{ delete $outcome->{$api_post_objects{$api_method}} };
    
            # The POST might be successful, but the create/update might have fully or partially failed.
            # That is, we can't just return success here without first analyzing the response.
            if ( 1 ) {
                $myLogger->info("exiting "
                    . " (call took "
                    . sprintf( "%.3f", Time::HiRes::time() - $start_time )
                    . " seconds)");
            }
            return $ref_decoded_response;    # this routine will log the details
        }
        elsif ( $http_response_code != 401 or not $retry ) {
            ## Either not an authorization failure, or we won't try again to re-authorize.
            ##
            ## The REST API possibly might not return JSON-formatted things for POST errors.
            ## So we manufacture a fixed structure to return to the caller.
            $outcome->{response_error}  = $response_content;
            $outcome->{response_code}   = $http_response_code;
            $outcome->{response_status} = status_message($http_response_code);
    
            $myLogger->error("POST error for REST API $api_method:  " . $http_response_code ." ". Dumper $response_content);
            if ( 1 ) {
                $myLogger->info("exiting "
                    . " (call took "
                    . sprintf( "%.3f", Time::HiRes::time() - $start_time )
                    . " seconds)", 1);
            }
            return 0;    # failed to upsert
        }
        else {
            ## Got an authorization failure.  Try to re-authorize before retrying.
            $myLogger->error("REST API authorization failed; will retry");
            if ( not $self->login() ) {
                if ( 1 ) {
                    $myLogger->info("exiting "
                        . " (call took "
                        . sprintf( "%.3f", Time::HiRes::time() - $start_time )
                        . " seconds)");
                }
                return 0;    # No luck.
            }
        }
    }

    if ( 1 ) {
        $myLogger->info("exiting "
            . " (call took "
            . sprintf( "%.3f", Time::HiRes::time() - $start_time )
            . " seconds)");
    }
    return 0;    # should not get here; just in case, return failure
}
1;
