#!/usr/local/groundwork/perl/bin/perl
# Migrates a 702 cacti configuration to a 710 set of configurations.
#
# Copyright 2015 GroundWork OpenSource
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# Revision History
# 2015-06-30 DN - v1.0.0 - initial version
# 2015-07-08 DN - v1.0.1 - added -delete_health_host option to remove old 702 feeder health host so 
# 			   feeder services will have correct Since times.
# 2016-01-10 DN - v1.0.2 - artificially add ws_client_config_file to the 702 config hash in read_cacti_config() 
# 			   if its missing. This allows clean migration from stock unpatched 702 install now.
# 2016-01-21 DN - v1.0.3 - adding retry_cache_limits block to the general config template
# 2016-02-03 DN - v1.0.4 - mods for latest cacti feeder changes for gwmon-12363 : 
#                           1. health_hostname prop gets deleted from endpoint config after -delete_health_host step
#                           2. changed $config710 from ...cacti_feeder_localhost.conf to ...cacti_feeder_cacti_feeder_host.conf
#                              (thats cos its not of the format cacti_feeder_<feeder host name>.conf
#                           3. the way delete_health_host() works changed to delete hosts created by this feeder that are also 
#                              in the feeder health host group.
# 2016-02-03 DN - v1.0.5 - Default endpoint name changed back to 'localhost'
# 2016-08-11 DN - v1.0.6 - Don't fail with error if migration alreday done - for 7.1.0->7.1.1
#
# NOTE - Update $VERSION below when changing the version # here.
# VIM : set tabstop=4  set expandtab - please use just these settings and no others
#
use 5.0;
use warnings;
use strict;

use version;
my  $VERSION = qv('1.0.6'); # keep this up to date
use TypedConfig qw(); # leave qw() on to address minor bug in TypedConfig.pm
use Data::Dumper; $Data::Dumper::Indent = 2; $Data::Dumper::Sortkeys = 1;
use File::Basename;
use Getopt::Long;
use Sys::Hostname; 
use File::Copy;
use GW::RAPID;
my ( $config702hash , # global TypedConfig hash ref
     $endpoint_config_template, $general_config_template , # global templates embedded in this script
     $clean, $help, $show_version, $delete_health_host # CLI option vars
);
my $config702 = '/usr/local/groundwork/config/cacti_feeder.conf';
my $config710 = '/usr/local/groundwork/config/cacti_feeder_localhost.conf';
sub fail;
sub error;

# =================================================================================
main();
# =================================================================================

# ---------------------------------------------------------------------------------
sub main
{
    initialize_templates(); # builds some globals to represent the new config templates

    initialize_options(); # read and process cli opts

    if ( not defined $delete_health_host ) {
        print '-' x 80 . "\n";
        # some description for the installer/patcher log
        print "Migrating cacti feeder configuration $config702\n";
        print "This process will update $config702, and create $config710\n";
        print "It is designed to update from a 7.0.2 system to an SP03 or 7.1.0 system\n";
        print '-' x 80 . "\n";
    }

    # check existence etc of existing and future config files
    if ( not check_configs() ) { 
        fail "A problem occurred checking config files - quitting"; 
    }

    # read in the existing cacti config
    if ( not read_cacti_config() ) {
        fail "A problem occurred reading the configuration file";
    }

    # remove the health host if -delete_health_host option was supplied
    if ( defined $delete_health_host ) {
        if ( not delete_health_host() ) {
	    fail "A problem occurred deleting the feeder health host";
        }
    } 

    # do the migration of the feeder config file
    elsif ( not migrate_cacti_config() ) { 
        fail "A problem occurred migrating the configuration file";
    }

    exit 0;

}

# ---------------------------------------------------------------------------------
sub migrate_cacti_config
{
    # The migration process takes a 702 config file (cacti_feeder.conf) and
    # a) creates the endpoint config file (typically cacti_feeder_localhost.conf)
    # b) updates the general feeder config (typically cacti_feeder.conf)
    # Args 
    #   none
    # Returns
    #   1 on success and updated config files
    #   0 otherwise 

    my ( $endpoint_name, $endpoint_cfg_name );

    # make a backup of the source config
    if ( not backup_existing_config() ) { 
        error "Unable to back up the existing configuration file $config702 - nothing will be updated";
        return 0;
    }

    # figure out the endpoint config file name
    if ( not calculate_endpoint_info( \$endpoint_name, \$endpoint_cfg_name ) ) { 
        error "Unable to figure out what the new endpoint configuration file name should be - nothing will be updated";
        return 0;
    }

    # create the new endpoint config file
    if ( not create_new_config_files( $endpoint_name, $endpoint_cfg_name ) ) {
        error "Unable to create the new endpoint configuration file '$endpoint_cfg_name'";
        return 0;
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub create_new_config_files
{
    # Tries to create a new endpoint config file, and a new general feeder config file, based on the 
    # values read in to the read in existing 702 config hash.
    # This means updating the template vars with new values, then writing the template vars out to a files.
    #
    # Args :
    #   endpoint name
    #   endpoint cfg name
    #   (global 702 config hash)
    # Returns :
    #   1 if created ok
    #   0 if not

    my ( $endpoint_name, $endpoint_cfg_name ) = @_;
    my ( %endpoint_mappings, %general_mappings, %mappings, $prop, $destcfg, $template_string, $value, $target_prop );
    my %yesno = ( '1' => 'yes', '0' => 'no' );

    # Calculate values to update the template with
    %mappings = (
        # 702 config property name  => { 'destcfgs' => general and/or endpoint,   'type' => prop type,   ['newname' => some new prop name to map this to in new configs] } 
        'enable_processing'                         => { 'destcfgs' => 'general,endpoint',  'type' => 'boolean' },
        'app_type'                                  => { 'destcfgs' => 'endpoint',          'type' => 'scalar'  },
        'cycle_timings'                             => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'cacti_system_indicator_check_frequency'    => { 'destcfgs' => 'general',           'type' => 'number',   'newname' => 'system_indicator_check_frequency' },
        'health_hostname'                           => { 'destcfgs' => 'endpoint',          'type' => 'scalar'  },  
        'health_hostgroup'                          => { 'destcfgs' => 'endpoint',          'type' => 'scalar'  },  
        'cacti_system_indicator_file'               => { 'destcfgs' => 'general',           'type' => 'scalar',   'newname' => 'system_indicator_file' },
        'ws_client_config_file'                     => { 'destcfgs' => 'endpoint',          'type' => 'scalar' },
        'api_timeout'                               => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'RAPID_debug'                               => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'host_bundle_size'                          => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'hostgroup_bundle_size'                     => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'service_bundle_size'                       => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'events_bundle_size'                        => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'notifications_bundle_size'                 => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'cactidbtype'                               => { 'destcfgs' => 'general',           'type' => 'scalar' },
        'cactidbhost'                               => { 'destcfgs' => 'general',           'type' => 'scalar' },
        'cactidbport'                               => { 'destcfgs' => 'general',           'type' => 'number' },
        'cactidbname'                               => { 'destcfgs' => 'general',           'type' => 'scalar' },
        'cactidbuser'                               => { 'destcfgs' => 'general',           'type' => 'scalar' },
        'cactidbpass'                               => { 'destcfgs' => 'general',           'type' => 'scalar' },
        'check_thold_fail_count'                    => { 'destcfgs' => 'general',           'type' => 'boolean' },
        'check_bl_fail_count'                       => { 'destcfgs' => 'general',           'type' => 'boolean' },
        'always_send_full_updates'                  => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'full_update_frequency'                     => { 'destcfgs' => 'endpoint',          'type' => 'number' },
        'post_notifications'                        => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'post_events'                               => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'constrain_to_hostgroups'                   => { 'destcfgs' => 'endpoint',          'type' => 'hash' },
        'default_hostgroups'                        => { 'destcfgs' => 'endpoint',          'type' => 'hash' },
        'guid'                                      => { 'destcfgs' => 'endpoint',          'type' => 'scalar' },
        'auditing'                                  => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
        'cacti_system_test_tweaks_file'             => { 'destcfgs' => 'endpoint',          'type' => 'scalar' },
        'update_hosts_statuses'                     => { 'destcfgs' => 'endpoint',          'type' => 'boolean' },
    );

    # Cycle through each prop from the mappings 
    foreach $prop ( sort keys %mappings ) {
        foreach $destcfg ( split /,/, $mappings{$prop}{'destcfgs'} ) {

            $value = ""; # some things might have no value  eg tweaks file 

            # calc the target property name 
            $target_prop = ( exists $mappings{$prop}{'newname'} ? $mappings{$prop}{'newname'} : $prop );

            # the template string that'll be used for subbing is the upper case of the target prop name
            # and the template vars need to have these set for the subs to then work
            $template_string = uc $target_prop;

            # TypedConfig will convert yes/no to 1/0 so need to convert back again
            if ( $mappings{$prop}{'type'} eq 'boolean' ) {
                $value = $yesno{ $config702hash->{$prop} } ;
            } 

            # For hashes in cacti config, only interested in the keys, not the values ie constrain_to_hostgroups and default_hostgroups
            elsif ( $mappings{$prop}{'type'} eq 'hash' ) {
                $value = "";
                foreach my $hashkey ( sort keys %{$config702hash->{$prop}} ) {
                    $value .= "    $hashkey\n";
                }
                $value =~ s/\n$//g if $value;
            }
            
            # All else are direct pass through - scalar and number
            else {  
                $value = $config702hash->{$prop} ;
            }

            #print "$prop : $destcfg : $template_string => $value\n";

            # update the appropriate template var
            if ( $destcfg eq 'general' ) { 
                 $general_config_template =~ s/$template_string/$value/g;
            }
            else {
                $endpoint_config_template =~ s/$template_string/$value/g;
            }
        }
    }

    # udpate the endpoint name in the comments of the endpoint config file
    $endpoint_config_template =~ s/ENDPOINTNAME/$endpoint_name/g;

    # udpate the endpoint name and config file name in the general config 
    $general_config_template =~ s/ENDPOINTNAME/$endpoint_name/g;
    $general_config_template =~ s/ENDPOINTCONFIGNAME/$endpoint_cfg_name/g;

    # write the updated endpoint config template out to the filesystem   
    if ( not write_new_config( $endpoint_cfg_name, \$endpoint_config_template ) ) { 
        fail "Could not write '$endpoint_cfg_name'";
        return 0;
    }

    # update the cacti_feeder.conf file with the updated general config template 
    if ( not write_new_config( $config702, \$general_config_template ) ) { 
        fail "Could not write $config702";
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub write_new_config
{
    # Tries to write the updated internal template variable to a file
    # Args :
    #   config file name
    #   (global: the updated template var)
    # Returns :
    #   1 if wrote ok
    #   0 if not

    my ( $cfg_name, $template_var_ref ) = @_;
    my ( $fh );

    print "Writing new configuration file '$cfg_name'\n";

    # open the file for write
    if ( not open $fh, '>', $cfg_name ) {
        error "Failed to open the new configuration file '$cfg_name' for writing : $!";
        return 0;
    }

    # write the content
    if ( not print $fh ${$template_var_ref} ) { 
        error "Failed to write content to the new configuration file '$cfg_name' : $!";
        return 0;
    }

    # close the file
    if ( not close $fh ) { 
        error "Failed to close the new configuration file '$cfg_name' : $!";
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub calculate_endpoint_info
{
    # Tries to figure out what the new endpoint config file name should be
    # Args :
    #   ref to the new endpoint name
    #   ref to the new calculated endpoint cfg name = /usr/local/groundwork/config/cacti_feeder_<endpointname>.conf
    # Returns :
    #   1 if calc'd ok
    #   0 if not

    my ( $endpoint_name_ref, $endpoint_cfg_name_ref ) = @_;

    # v 1.0.4 the endpoint name is going to be just 'cacti_feeder_host';
    # v 1.0.5 the endpoint name is going to be just 'localhost';
    ${$endpoint_name_ref}     = 'localhost';
    ${$endpoint_cfg_name_ref} = $config710; 
    return 1;



    my ( $ws_file, $fh, $line, $key, $val ) ;

    # Get the endpoint name from the foundation_rest_url prop from the 
    # file that is pointed to by the cacti conf's ws_client_config_file property

    # Get the ws client config file
    $ws_file = $config702hash->{ws_client_config_file};
    
    # check this file exists and is readable
    if ( ( not -e $ws_file ) or ( not -r $ws_file ) ) { 
        error "The web services configuration file '$ws_file' doesn't exist or isn't readable";
        return 0;
    }

    # open the ws props file
    if ( not open $fh, '<', $ws_file ) { 
        error "Could not open the web services configuration file '$ws_file' : $!";
        return 0;
    }

    # get the foundation_rest_url from it
    while ( $line = <$fh> ) {
        next if $line !~ /^foundation_rest_url\s*=/;
        ($key, $val) = split /=/, $line;
        last;
    }

    # close the ws props file
    if ( not close $fh ) { 
        error "Could not close the web services configuration file '$ws_file' : $!";
        return 0;
    }
    
    # At this point, $val should be of this format :
    #    http://localhost:8080/foundation-webapp/api, or,
    #    https://remotehost/foundation-webapp/api

    if ( $val ) { # test if val set just in case foundation_rest_url prop was not defined for some reason
        $val = ( split /\/\//, $val )[1]; # removes the http[s]:// piece so now have localhost:8080/...  or remotehost/...
        $val = ( split /\//, $val )[0]; # removes the /...
        $val =~ s/:.*$//g; # removes any :8080 stuff
    }

    # one last sanity check - if isolated endpoint host name is empty, catch that and set it to localhost
    if ( not $val or $val eq "" or $val =~ /^\s+$/ )  { 
        error "Couldn't calculate endpoint hostname properly - setting it to just endpoint_host";
        ${$endpoint_name_ref} = 'endpoint_host';
    }
    else {
        ${$endpoint_name_ref} = $val;
    }

    ${$endpoint_cfg_name_ref} = "/usr/local/groundwork/config/cacti_feeder_${$endpoint_name_ref}.conf";

    return 1;
    
}

# ---------------------------------------------------------------------------------
sub backup_existing_config
{
    # Tries to back up the existing config file
    # Args :
    #   none
    # Returns :
    #   1 if backed up ok
    #   0 if not

    my $backup_name = $config702 . ".backup." . time();

    print "Backing up $config702 to $backup_name\n";

    if ( not copy( $config702, $backup_name ) ) {  
        error "Unable to copy $config702 to $backup_name : $!";
        return 0;
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub check_configs
{
    # Does some basic access checks on config files
    # Args :
    #   none
    # Returns :
    #   1 if checks all ok
    #   0 if a check fails

    # check that the 702 config exists
    if ( ! -e $config702 ) {
        print "Configuration file $config702 doesn't exist\n";
        return 0;
    }

    # check that the 702 config is readable
    if ( ! -r $config702 ) {
        print "Configuration file $config702 is not readable\n";
        return 0;
    }

    # check that the 702 config is writable
    if ( ! -w $config702 ) {
        print "Configuration file $config702 is not writable and cannot be updated\n";
        return 0;
    }

    # if there's a 710 config AND it's not writable, bail - this shouldn't happen
    if ( -e $config710 and ! -w $config710 ) { 
        print "Configuration file $config710 already exists but is not writable\n";
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub read_cacti_config
{
    # Reads the 702 config file into a global hash
    # Args :
    #   none
    # Returns :
    #   1 if read and validated ok
    #   exits on failure
    # Notes
    # This code assumes that the 702 config is legit and validates and that any problems
    # found by the cacti_feeder.pl script have already been surfaced and fixed.

    print "Reading feeder configuration file $config702\n";
    eval {
        $config702hash = TypedConfig->new( $config702  );
    };
	if ($@) {
	    chomp $@;
	    $@ =~ s/^ERROR:\s+//i;
	    fail "Cannot read config file $config702: $@";
	}

    # Try to handle case where this migration script is running again manually outside of 
    # SP03 or 710 installation process. In this case, the 710 config will contain 'system_indicator_file'
    # and no 'ws_client_config_file' props
    if ( not $delete_health_host ) {
        if ( exists $config702hash->{system_indicator_file} and not exists $config702hash->{ws_client_config_file} ) {
            # v 1.0.6 update ... when migrating 7.1.0 to 7.1.1 this is ok and there's nothing to do.
            # No changes for cacti feeder have occurred between 7.1.0 and 7.1.1.
            #fail "Looks like this system has already been migrated . Nothing will be done.";  
            print "Looks like this system has already been migrated. Nothing will be done.\n";  
	    exit 0;
        }
    }

    # v1.0.2 mod:
    # If there's no ws_client_config_file prop, then most likely that the upgdade is going from an unpatched 702.
    # In 702 sp2, ws_client_config_file prop gets added due to changes in RAPID.
    # The easiest thing to do is insert this into the config here ie add what the sp02 patch would add to the config anyway.
    # This then prevents calculate_endpoint_info() failing later and lets the migration work.
    # Even if this value doesn't actually exist, at least it will allow migration to work. 
    # Extremely high chances are it will exist anyway since it's a standard sys config file.
    if ( not exists $config702hash->{ws_client_config_file}  ) {
         $config702hash->{ws_client_config_file} = "/usr/local/groundwork/config/ws_client.properties";
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub fail
{
    my ( $msg ) = @_;
    error $msg;
    exit 1; # seems like migration scripts exit 1 when they fail
}

# ---------------------------------------------------------------------------------
sub error
{
    my ( $msg ) = @_;
    print "ERROR : $msg\n" if defined $msg;
}


# ---------------------------------------------------------------------------------
sub initialize_options
{
    # Command line options processing and help.

    my $helpstring = "
Groundwork Cacti feeder configuration migration tool - version $VERSION

Description

    This tool migrates the configuration for cacti feeder in GWME 7.0.2
    to be used in an GWME SP03 or GWME 7.1.0. 
    The migration process will update $config702, 
    and create $config710.

    GroundWork 7.0.2 service pack 3 and Groundwork 7.1.0 onwards introduce
    multiple endpoints, retry caches and other new features and improvements
    into the Cacti Feeder application.

    The migration script generates the new configure files required by the updated
    version of the cacti feeder. At this time, it adds just one endpoint in the main
    cacti feeder configuration file.
    
How To Use

    This tool runs in one of two modes :

    Mode 1. Without any arguments, it will migrate the cacti feeder configuration files
            In this mode, GroundWork does not need to be running at all.

    Mode 2. With the -delete_health_host option, it will remove any hosts created by this
            feeder ( ie with agentId = the guid from the endpoint config ), that are in the
            hostgroup defined in the health_hostgroup property in the endpoint config.
            It then deletes the health_hostname name property from the endpoiont config as it is no longer used.
            In this mode, the GroundWork REST API is used so GroundWork needs to be up and running.
            This second mode and invocation is required to address a bug in earlier versions of the
            cacti feeder where feeder host and service 'Since' times were not updated and remain so until the
            feeder health services are removed and rebuilt. The cleanest way is to just remove the
            health host. A restart of GroundWork services is required for make removal of the 
            health_hostname property take effect. This is done as part of the upgrade process.

    First run in mode 1 (possibly with groundwork services down). 
    Then when groundwork is back up, run in mode 2.
    Both steps are required for a complete migration.

Options
    -conf <conf>  - optional config to parse - default is $config702
    -delete_health_host - deletes the feeder health host.
    -help         - Show this help
    -version      - Show version and exit

Exit values
    1 - some error occurred
    0 - migration completed successfully

Author
    GroundWork 2015

";

    GetOptions(
        'conf=s'  => \$config702,
        'delete_health_host'    => \$delete_health_host,
        'help'    => \$help,
        'version' => \$show_version,
    ) or die "$helpstring\n";

    if ( defined $help ) { print $helpstring; exit; }
    if ( defined $show_version ) { print "$0 version $VERSION\n"; exit; }

}


# ---------------------------------------------------------------------------------
sub initialize_templates
{
    # This routine simply sets up some globals which are used as template SP03/710 
    # config files which this script builds.
    # args - none, returns 1.

    $endpoint_config_template = <<TEMPLATE1;;
# cacti_feeder_ENDPOINTNAME.conf - the ENDPOINTNAME endpoint configuration file
#
# Copyright 2014-15 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved.  This program is free software; you can
# redistribute it and/or modify it under the terms of the GNU
# General Public License version 2 as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
#
# The values specified here are used to control the behavior of the Cacti Feeder
# for this ENDPOINTNAME endpoint.

# Disable or enable the feeder for the ENDPOINTNAME endpoint.
# The feeder will only process cacti hosts and thresholds when this option is set to yes.
enable_processing = ENABLE_PROCESSING

# Foundation feeder application type. Leave this set to CACTI.
app_type = APP_TYPE

# Log how long each cycle is taking  
cycle_timings = CYCLE_TIMINGS

# Cacti feeder health and metrics virtual hosts GroundWork hostgroup.
health_hostname  = HEALTH_HOSTNAME
health_hostgroup = HEALTH_HOSTGROUP

# REST end point host and credentials are in ws_client_config_file
ws_client_config_file = "WS_CLIENT_CONFIG_FILE"
api_timeout = API_TIMEOUT

# Forcing a CRL check means the Perl GW::RAPID package will insist on having a
# Certificate Revocation List file available if the server is configured to use SSL
# (as specified in the foundation_rest_url setting of the file listed above as the
# ws_client_config_file option value).  This should be enabled for a properly secure
# SSL setup; you will then need to provide a valid CRL file, even if it doesn't
# list any revoked certificates, in the /usr/local/groundwork/common/openssl/certs/
# directory.  Disabling this is a less-secure setup, as then the HTTPS connection
# does not protect against a man-in-the-middle attack.  If the server does not use
# SSL, this option has no effect.
# [yes/no]
force_crl_check = no

# Bundling options that determine how many to bundle up in one REST API call for CRUD ops
host_bundle_size          = HOST_BUNDLE_SIZE
hostgroup_bundle_size     = HOSTGROUP_BUNDLE_SIZE
service_bundle_size       = SERVICE_BUNDLE_SIZE
events_bundle_size        = EVENTS_BUNDLE_SIZE
notifications_bundle_size = NOTIFICATIONS_BUNDLE_SIZE

# Update frequency options
# Switch this to yes to send all updates on every cycle. 
# Setting to yes turns off full_update_frequency functionality ie always sends all updates on every cycle
# Setting to no turns on full_update_frequency functionality ie only sends all updates on every full_update_frequency'th cycle
always_send_full_updates = ALWAYS_SEND_FULL_UPDATES
# Period on which to post all cacti host and service states, regardless of whether 
# or not they changed eg setting this to 3 means on every 3rd cycle, post everything,
# and on cycles in between, just post hosts and/or services with state changes.
# All updates are sent out on the very first cycle each time the feeder is (re)started.
# This option is overridden by always_send_full_updates = yes
full_update_frequency = FULL_UPDATE_FREQUENCY

# Notification and event options
# Enable/disable notifications for cacti host and service state changes.
post_notifications = POST_NOTIFICATIONS
# Enable/disable creation of events in Foundation for cacti host and service state changes.
# Generally leave this to yes unless instructed otherwise by GroundWork.
post_events = POST_EVENTS

# Hostgroup related options
# What subset of hosts to check Cacti thresholds for.  
# Typically, this hash would have one hostgroup in it, eg a child server, or have none.
# Another way of saying this is : Only process cacti thresholds which are for hosts that are already in these Foundation hostgroups.
# Note : this acts like an early filter - any hosts that were created by this feeder that are not in these hostgroups will be deleted.
<constrain_to_hostgroups> 
CONSTRAIN_TO_HOSTGROUPS
   #Cacti Hostgroup 1
   #Cacti Hostgroup 2
</constrain_to_hostgroups> 

# What hostgroup(s) to add hosts to by default 
<default_hostgroups>
DEFAULT_HOSTGROUPS
    # cactigroup 2
    # cactigroup 3
</default_hostgroups>

# Service state options
# Note : This feature is not included in this release.
# Only *add* services (ie cacti interface services) if they are in a state listed.
# Valid states are : any, ok, unscheduled critical
# Make note of the cacti->GW translation table here
# These are GW states only, not cacti. maybe a future rev.
# This filtering only applies to things being created for the first time in Foundation.
# Note that this does not affect services that already exist - state changes for those 
# are processed normally.
#process_service_states = "unscheduled critical"
# leave this empty for no constraints
# currently only supported values are OK and UNSCHEDULED CRITICAL
# but this is designed for expansion later on eg with WARNING or other GW states
#<constrain_to_service_states>
   #UNSCHEDULED CRITICAL
   #OK
#</constrain_to_service_states>

# Feeder Unique Identifier options
# Globally unique identifier for this feeder instance.
# This is used for setting the value of AgentId which is field that is attached to host, hostgroup and service objects
# in Foundation that are/were created by this feeder.  
# When the feeder runs, if the value of guid is set to 'undefined', then the feeder will 
# write a new value in this config file for this option.
# Generally don't touch this unless otherwise instructed by GroundWork.
guid = GUID

# Auditing options
# The feeder can produce an audit trail of the following things :
#    - creation of new hosts, new hostgroups, new services
#    - deletion of hosts, hostgroups, services
# On each feeder cycle, if any of the above  things happens, Foundation audit events will be created. 
# There is an overhead in performance for auditing. 
auditing = AUDITING

# Testing options
# For testing purposes, emulate cacti host and interface states and removal etc.
# To disable testing, set this value to a non existent file, or an empty file, or blank.
cacti_system_test_tweaks_file = CACTI_SYSTEM_TEST_TWEAKS_FILE

# Set this to true if the feeder should send in host status updates, false if not.
# Note that the feeder will always create a host with a status from the feeder regardless of this setting.
update_hosts_statuses = UPDATE_HOSTS_STATUSES

# The maximum age in seconds of entries in the retry cache. Entries with timestamps older than this 
# maximum age will be discarded during the retry cache import stage (1 day =  86400 seconds).
retry_cache_max_age = 172800

# Send feeder metrics data to performance graphs. This is experimental.
send_feeder_perf_data = no

# Feeder.pm module error emulation (fmee) is used for QA and testing purposes.
# Adding a Feeder.pm sub name into this block will cause that sub to return 0.
# 'timestamp' can also be used to emulate a failure at a specific retry cache entry.
#<fmee>
    #timestamp = some epoch time that matches a retry cache entry timestamp
    #feeder_upsert_hosts
    #feeder_upsert_hostgroups
    #feeder_upsert_services
    #feeder_delete_hosts
    #feeder_delete_services
    #check_foundation_objects_existence
    #flush_audit
    #initialize_health_objects
    #license_installed
    #check_license
#</fmee>

# End of configuration
TEMPLATE1

    $general_config_template = <<TEMPLATE2;;
# cacti_feeder.conf - the master feeder configuration file
#
# Copyright 2014,2015 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved.  This program is free software; you can
# redistribute it and/or modify it under the terms of the GNU
# General Public License version 2 as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
#
# The values specified here are used to control the behavior of the Cacti Feeder.
#
# Disable or enable the feeder completely. 
# When disabled, no processing of any endpoints will be done.
feeder_enabled = ENABLE_PROCESSING

# How often (seconds) to check to see if cacti system indicated that its ready for the feeder to run
system_indicator_check_frequency = SYSTEM_INDICATOR_CHECK_FREQUENCY

# The cacti system readiness indicator file - presence of this file will indicate to the feeder to go ahead process/sync endpoints
system_indicator_file = SYSTEM_INDICATOR_FILE

# Which servers, aka endpoints, this feeder is going to feed.
# Endpoints will be processed in the order the are specified here.
# At least one needs to be defined. More than one can be defined by repeating the endpoint directive.
# The format is : 
#   endpoint = <simple endpoint name>:<path to it's configuration file>
# The <simple endpoint name> is not a hostname but just an identifier referenced in logging by the feeder. 
# The endpoint hostname itself is defined in the associated configuration file.
# For clarity, it's recommended to have this name be the hostname of the endpoint.
# The <path to it's configuration file> needs to be a fully qualified filename.
endpoint = ENDPOINTNAME:ENDPOINTCONFIGNAME
#endpoint = standby1:/usr/local/groundwork/config/cacti_feeder_standby1.conf
#endpoint = standby2:/usr/local/groundwork/config/cacti_feeder_standby2.conf

# In the event of failing to create a new Feeder object for the endpoint, wait this many seconds, and retry this many times
endpoint_max_retries = 3
endpoint_retry_wait  = 5

# Directory in which endpoint retry caches are stored
retry_cache_directory = /usr/local/groundwork/foundation/feeder/retry_caches

# Retry cache size warning and critical thresholds.
# These settings are used to alert via the feeder's cache_errors service when a retry cache
# file has exceeded some size thresholds. The units are Mb. These settings are optional.
# You may define warning and/or critical thresholds for zero or more endpoints.
# If warning or critical thresholds are exceeded, the cache_errors service will
# convey this information. The critical threshold is also used to determine whether to truncate
# a cache file from the front of the file (oldest data removed first).
<retry_cache_limits>

    <ENDPOINTNAME>
        warning  = 100
        critical = 150
    </ENDPOINTNAME>

</cache_limits>

# Cacti database options ...
cactidbtype = CACTIDBTYPE 
cactidbhost = CACTIDBHOST
cactidbport = CACTIDBPORT
cactidbname = CACTIDBNAME
cactidbuser = CACTIDBUSER
cactidbpass = CACTIDBPASS

# To use the feeder against a standalone mysql based Cacti installation ...
# (ensure grant privs on to cacti user at this host on the Cacti side)
# cactidbtype = mysql
# cactidbhost = cactisystemhostname
# cactidbport = 3306
# cactidbname = cacti
# cactidbuser = cactiuser
# cactidbpass = cactiuser

# Cacti threshold qualification options ...
# Whether to qualify threshold alerts by checking the threshold fail count against the threshold fail trigger.
check_thold_fail_count = CHECK_THOLD_FAIL_COUNT
# Whether to qualify baseline alerts by checking the baseline fail count against the baseline fail trigger.
check_bl_fail_count = CHECK_BL_FAIL_COUNT

# ----------------------------------------------------------------
# Logger settings
# ----------------------------------------------------------------

# Where the log file is to be written.
logfile = /usr/local/groundwork/foundation/container/logs/cacti_feeder.log

# There are six predefined log levels within the Log4perl package:  FATAL,  
# ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).  We define
# two custom levels at the application level to form the full useful set:
# FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE.  To see an
# individual message appear, your configured logging level here has to at
# least match the priority of that logging message in the code.
GW_RAPID_log_level = "ERROR"

# The application-level logging level is set separately from the logging 
# level used by the GW::RAPID package, to avoid drowning in low-level
# detail from the GW::RAPID module. 
feeder_log_level = "INFO"

# Application-level logging configuration, for that portion of the logging
# which is currently handled by the Log4perl package.
log4perl_config = <<EOF

# Use this to send everything from FATAL through \$GW_RAPID_log_level
# (for messages from the GW::RAPID package) or \$feeder_log_level
# (for messages from the application level) to the logfile.
log4perl.category.GW.RAPID.module = \$GW_RAPID_log_level, cacti_feeder_logfile
log4perl.category.cacti_feeder    = \$feeder_log_level, cacti_feeder_logfile

# Add the Screen appender if you want to see output to stdout ...
#log4perl.category.cacti_feeder = \$feeder_log_level, cacti_feeder_logfile, Screen

log4perl.appender.cacti_feeder_logfile          = Log::Log4perl::Appender::File
log4perl.appender.cacti_feeder_logfile.filename = \$logfile
log4perl.appender.cacti_feeder_logfile.utf8     = 0
log4perl.appender.cacti_feeder_logfile.layout   = Log::Log4perl::Layout::PatternLayout
log4perl.appender.cacti_feeder_logfile.layout.ConversionPattern = [%d{EEE MMM dd HH:mm:ss yyyy}] %m%n
log4perl.appender.Screen            =  Log::Log4perl::Appender::Screen
log4perl.appender.Screen.stderr     =  0
log4perl.appender.Screen.layout     =  Log::Log4perl::Layout::SimpleLayout

EOF

# End of configuration

TEMPLATE2

    return 1;
    
}

# ---------------------------------------------------------------------------------
sub delete_health_host
{
    my ( $endpoint_cfg_name, $rest_api, %rest_api_options, $rest_api_requestor, $ws_client_config_file,     
         $endpoint_cfg_hash, %outcome, @results, $health_hostname, $try, %results );
    my $maxtries = 3;
    
    print '-' x 80 . "\n";
    print "Deleting feeder health host(s)\n";

    # Need to get the ws file name for the RAPID object creation...
    # There is expected to be only one endpoint entry in the newly created endpoint config file
    # after a migration of the confs from 702. Check just in case.
    if ( ref $config702hash->{endpoint} eq 'ARRAY') {
        fail( "Expecting just one endpoint - found more than one :\n\t" . join "\n\t", @{ $config702hash->{endpoint} } );
    }
    $endpoint_cfg_name = $config702hash->{endpoint};
    $endpoint_cfg_name = ( split /:/, $endpoint_cfg_name )[1]; # get the endpoint config file name
    $endpoint_cfg_name =~ s/(^\s+|\s+$)//g; # strip any leading/trailing whitespace - shouldn't be any

    # read in the endpoint cfg name and get the ws_client_config prop
    # (Reading errors will be trapped by TypedConfig and none are expected)
    eval { $endpoint_cfg_hash = TypedConfig->new( $endpoint_cfg_name  ); };
	if ($@) {
	    chomp $@;
	    $@ =~ s/^ERROR:\s+//i;
	    fail "Cannot read endpoint config file $endpoint_cfg_name: $@";
	}

    $health_hostname = $endpoint_cfg_hash->{health_hostname};
    if ( not defined $health_hostname or not $health_hostname ) { 
        fail "The feeder health_hostname property was not set to anything in the $endpoint_cfg_name config file!";
    }

    # use the extracted ws client config file value - talk about long winded! ...
    %rest_api_options = (
	    access          => $endpoint_cfg_hash->{ws_client_config_file}, # the location of the ws props
        force_crl_check => $endpoint_cfg_hash->{force_crl_check},       # need to pass this along  too
    );

    # create a RAPID object
    $rest_api_requestor = "Cacti feeder migration tool";
    $try = 1 ; 
    while ( not ($rest_api = GW::RAPID->new( undef, undef, undef, undef, $rest_api_requestor, \%rest_api_options ))   and $try <= $maxtries ) {
        error "Failed to create a GW REST connection - try $try/$maxtries - will sleep 5 and retry";
        $try++;
        sleep 5;
    }
    if ( not defined $rest_api ) {
        fail "Could not initialize a GW REST object : $@";
    }

    # v 1.0.4 deletes any hosts in the health host group that were created by the cacti feeder
    my $query = "hostgroup = '$endpoint_cfg_hash->{health_hostgroup}' and agentId = '$endpoint_cfg_hash->{guid}' ";
    $try = 1;
    if ( not $rest_api->get_hosts( [ ] , { query => $query }, \%outcome, \%results ) and $try <= $maxtries ) { 
        if ( defined $outcome{response_code} and $outcome{response_code} == 404 ) { 
            print "No feeder health hosts were found in hostgroup '$endpoint_cfg_hash->{health_hostgroup}' with agentId '$endpoint_cfg_hash->{guid}' - nothing to do\n";
            exit 0;
        }
        error "Failed to test if health host '$health_hostname' exists - try $try/$maxtries - will sleep 5 and retry";
        $try++;
        sleep 5;
    }

    my @feeder_health_hosts = keys %results;
    my $feeder_health_hosts = join ",", @feeder_health_hosts;

    # One last check - if there were no keys in the results, then no hosts to delete 
    if ( not @feeder_health_hosts  ) {
        print "No feeder health hosts were found to delete - nothing to do\n";
        exit 0;
    }
        
    # try and delete the feeder health host
    print "Deleting feeder health host(s) $feeder_health_hosts\n";
    $try = 1 ; 
    while ( not $rest_api->delete_hosts( \@feeder_health_hosts, {} , \%outcome, \@results ) and $try <= $maxtries ) {
        error "Failed to delete the feeder health host(s) $feeder_health_hosts - try $try/$maxtries - will sleep 5 and retry";
        $try++;
        sleep 5;
    }
    if ( $try > $maxtries ) {
        $rest_api = undef; # terminate REST API cleanly
        fail "Couldn't delete the feeder health host '$health_hostname'!";
    }

    #$rest_api->{logger} = undef; # makes no difference - still get a benign 'Log4perl: Seems like no initialization happened. Forgot to call init()?' at the end 
    $rest_api = undef; # terminate REST API cleanly

    print "Feeder health host(s) $feeder_health_hosts deleted\n";

    # V 1.0.4 now also need to remove the health_hostname property from the endpoint config 
    print "Removing health_hostname property from endpoint config file '$endpoint_cfg_name'.\n";
    system "sed -i -r -e '/^[\t\ ]*health_hostname/d' $endpoint_cfg_name";

    print '-' x 80 . "\n";

    return 1;

}

