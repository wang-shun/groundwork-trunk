#!/usr/local/groundwork/perl/bin/perl -w --

use strict;
use warnings;
use Getopt::Long;
use GW::RAPID;
use Data::Dumper; $Data::Dumper::Indent = 1; $Data::Dumper::Sortkeys = 1;
use Log::Log4perl qw(get_logger);
my ( $help, $access, $rest_api, $hostgroups, $logger );

initialize_logging();
initialize_options();
remove_hostgroups();

exit;

# ---------------------------------------------------------------------------------
END { $rest_api = undef; }

# ---------------------------------------------------------------------------------
sub remove_hostgroups
{
    my ( $hostgroup, @hostgroups, %outcome, %results, @results) ;

    $logger->info("Using $access for foundation REST API url and credentials");
    $rest_api = GW::RAPID->new( undef, undef, undef, undef, $0 , { access => $access });
	if ( not $rest_api ) { 
        $@ =~ s/\n//g if defined $@;
        die("ERROR - Failed to initialize Groundwork REST API. $@\n");
    }

    # use this for testing to create a domtest hostgroup everytime and then -hostgroups domtest to delete it
    # @hostgroups = ( { "name" => 'domtest', "description" => "domtesting", "alias" => "Alias" } );
    # $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results );

    @hostgroups = split(',', $hostgroups );

    HOSTGROUP: foreach $hostgroup ( @hostgroups ) {

        # First get the hostgroup
        if ( not $rest_api->get_hostgroups( [ $hostgroup ], { }, \%outcome, \%results ) ) {
            if ( $outcome{response_code} eq '404' ) { 
                $logger->warn( "HOSTGROUP $hostgroup does not exist. Nothing will be done for this hostgroup.");
            }
            else { 
                $logger->error( "Failed to check hostgroup '$hostgroup' existence - skipping deletion. Error details : " . Dumper \%outcome );
            }
            next HOSTGROUP;
        }
            
        print "Deleting hostgroup '$hostgroup' ... ";
        if ( not $rest_api->delete_hostgroups( [ $hostgroup ] , {}, \%outcome, \@results ) ) {
             $logger->error( "Could not delete hostgroup '$hostgroup : " . Dumper \%outcome );
        }
        else {     
             print "Ok\n";
        }
    }

}

# ---------------------------------------------------------------------------------
sub initialize_options
{
    # Command line options processing and help.

    my $helpstring = "
$0 removes hostgroups from GroundWork. 

Options
    -access <properties file>     : specify alternative to /usr/local/groundwork/ws_client.properties file (default).
    -help                         : show this help
    -hostgroups (hostgroup list)  : A comma separated list of hostgroups to remove

Example

    $0 -hostgroups hg1,hg2,hg3 -access /usr/local/groundwork/config/ws_client_parent.properties
    
    This will use /usr/local/groundwork/config/ws_client_parent.properties to figure out which GroundWork server 
    to operate against, and then attempt to remove hostgroups hg1, hg2 and hg3.

";

    GetOptions(
        'access=s'  => \$access,
        'help'    => \$help,
        'hostgroups=s' => \$hostgroups,
    ) or die "$helpstring\n";

    if ( defined $help ) { print $helpstring; exit; }
    if ( not $hostgroups ) { 
        print $helpstring; 
        exit;
    }


    # check/define access for RAPID
    if ( defined $access ) { 
        if (  ! -r $access or ! -e $access ) { 
            $logger->error( "Access file $access doesn't exist or isn't readable - quitting");
            exit;
        }
    }
    else {
        $access = '/usr/local/groundwork/config/ws_client.properties';
    }


    return 1;

}


# ---------------------------------------------------------------------------------
sub initialize_logging
{
    my $log4perl_config;
    $log4perl_config = <<EOF;
log4perl.category.remover  = DEBUG, Screen
log4perl.appender.Screen =  Log::Log4perl::Appender::Screen
log4perl.appender.Screen.stderr =  0
log4perl.appender.Screen.layout =  Log::Log4perl::Layout::SimpleLayout
EOF
    Log::Log4perl::init( \$log4perl_config);
    $logger = Log::Log4perl::get_logger('remover');
}
