################################################################################
#
# GDMA::Configuration
#
# This library contains routines that support actions which are specific to the
# server side of GDMA Auto-Setup.
#
# Copyright (c) 2017-2018 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#
################################################################################

package GDMA::Configuration;

use strict;
use warnings;

use Exporter;
use Text::Wrap;

use MonarchStorProc;
use MonarchAPI;
use MonarchExternals;

# ================================
# Package Variables
# ================================

our @ISA = ('Exporter');
our $VERSION = '0.8.1';

# ================================
# Package Routines
# ================================

# Package constructor.
sub new {
    my $invocant = $_[0];
    my $options  = $_[1];                          # hashref; options like base directory
    my $class    = ref($invocant) || $invocant;    # object or class name

    # FIX MAJOR:  how do we expect get_logger() to find an appropriate logger?
    my $logger = ( defined($options) ? $options->{logger} : undef ) || Log::Log4perl::get_logger("GDMA");

    # FIX MAJOR:  verify that all of the required options are in fact supplied
    # user_acct is the name of the user which is generating externals
    # session_id is the Session ID for the user session which is generating externals
    # via_web_ui is a boolean flag that tells whether we are being executed in an HTML-display context
    my %config = (
	hostname   => $options->{hostname},
	enabled    => $options->{enabled} ? 1 : 0,
	logger     => $logger,
	user_acct  => 'script',
	session_id => undef,
	via_web_ui => 0,
    );

    my $self = bless \%config, $class;

    return $self;
}

# store results as received from the client
sub save_discovery_results {
    my $self    = shift;
    my $outcome = 0;

    ## FIX MAJOR

    return $outcome;
}

# check for client, syntax, and internal errors, inconsistencies
sub analyze_discovery_results {
    my $self    = shift;
    my $outcome = 0;

    ## FIX MAJOR

    return $outcome;
}

# make a connection to the Monarch database
sub database_connect {
    my $self    = shift;
    my $dbh     = undef;
    my $outcome = 0;

    # We make our own connection to the Monarch databaes, with AutoCommit disabled, RaiseError
    # enabled, and perhaps PrintError disabled.  We then pass our handle to MonarchStorProc to
    # override its connection handle.  That will allow us to roll back failed application of our
    # changes, and also leave us free to make database queries that are not currently supported
    # in various Monarch routines, using our own copy of the handle, without fear of deadlock
    # due to having multiple handles open in the same process to the same database and having
    # at least one of those handles open withh AutoCommit disabled.

    my ( $dbtype, $dbhost, $dbname, $dbuser, $dbpass ) = undef;
    my $config_file = '/usr/local/groundwork/config/db.properties';
    if ( !open( FILE, '<', $config_file ) ) {
	$self->{logger}->error("ERROR:  Cannot read $config_file ($!).");
	return $outcome;
    }
    while ( my $line = <FILE> ) {
	if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/ )  { $dbtype = $1 }
	if ( $line =~ /^\s*monarch\.dbhost\s*=\s*(\S+)/ )   { $dbhost = $1 }
	if ( $line =~ /^\s*monarch\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	if ( $line =~ /^\s*monarch\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	if ( $line =~ /^\s*monarch\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
    }
    close(FILE);
    if ( !defined($dbhost) or !defined($dbname) or !defined($dbuser) or !defined($dbpass) ) {
	my $database_name = defined($dbname) ? $dbname : 'monarch';
	$self->{logger}->error("ERROR:  Cannot read the \"$database_name\" database configuration.");
	return $outcome;
    }

    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    eval { $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 0, 'RaiseError' => 1, 'PrintError' => 0 } ); };
    if ($@) {
	chomp $@;
	$self->{logger}->error("ERROR:  Database connect failed ($@).");
	return $outcome;
    }

    $self->{dbh} = $dbh;
    my $auth = StorProc->dbconnect( $self->{dbh} );

    # We opened the connection with AutoCommit disabled.  That means that any activity done on this connection
    # by StorProc, even just reading data, has already begun the first transaction, and that will block us from
    # starting a new transaction.  So we roll back any open transaction before returning control to the caller.
    my $problem;
    ( $outcome, $problem ) = $self->roll_back_config_changes();
    if ( not $outcome ) {
	return $outcome;
    }

    $outcome = 1;
    return $outcome;
}

# release all database-connection resources
sub database_disconnect {
    my $self    = shift;
    my $outcome = 0;

    if ($self->{dbh}) {
	## We don't bother disconnecting at the MonarchStorProc level, because we assume that
	## the fact that we are disconnecting at the application level means the entire process
	## is going to exit soon without further access to the MonarchStorProc level.  (Also,
	## we currently have no means to ensure that the Monarch $dbh handle itself is destroyed,
	## which would currently be necessary to re-establish a connection at the MonarchStorProc
	## level.)
	$self->{dbh}->disconnect();
	$self->{dbh} = undef;
    }

    return $outcome;
}

# compare current Monarch config against last discovery results
sub audit {
    my $self    = shift;
    my $outcome = 0;

    ## FIX MAJOR

    return $outcome;
}

# begin database transaction
#
# Using a single transaction for all changes to the database allows us to roll back all
# of them if any logical problem is found late in the processing.  (If we need a finer
# granularity of rollback control within the overall transaction, we can use savepoints.)
# It is also faster, since statements in a transaction block are executed more quickly,
# avoiding the overhead of making each individual statement be its own transaction block.
#
sub begin_config_changes {
    my $self    = shift;
    my $dbh     = $self->{dbh};
    my $outcome = 0;

    # We set up so all database transactions generated on our database handled are
    # automatically completely safe for concurrent operation against other actors
    # who might be changing the database at the same time, including other copies
    # of this same scripting.
    #
    # We don't run an explicit "start transaction" command after setting up the transaction
    # isolation level, because we don't think we'll need one.  If it's needed at all, it
    # should be managed by the DBI package, since we disabled AutoCommit.
    #
    eval { $self->{dbh}->do("set session transaction isolation level serializable"); };
    if ($@) {
	chomp $@;
	$self->{logger}->error("ERROR:  Database start transaction failed ($@).");
	return $outcome;
    }

    $outcome = 1;
    return $outcome;
}

# reflect all discovery results into Monarch
sub make_config_changes {
    my $self    = shift;
    my $outcome = 0;

    ## FIX MAJOR

    return $outcome;
}

# execute database commit
sub commit_config_changes {
    my $self    = shift;
    my $outcome = 0;

    eval {
	if ( defined $self->{dbh} ) {
	    $self->{dbh}->commit();
	}
	else {
	    die "there is no active handle for the database connection\n";
	}
    };
    if ($@) {
	chomp $@;
	$self->{logger}->error("ERROR:  Database commit failed ($@).");
	return $outcome, $@;
    }

    $outcome = 1;
    return $outcome, '';
}

# execute database rollback
sub roll_back_config_changes {
    my $self    = shift;
    my $outcome = 0;

    eval {
	if ( defined $self->{dbh} ) {
	    $self->{dbh}->rollback();
	}
	else {
	    die "there is no active handle for the database connection\n";
	}
    };
    if ($@) {
	chomp $@;
	$self->{logger}->error("ERROR:  Database rollback failed ($@).");
	return $outcome, $@;
    }

    $outcome = 1;
    return $outcome, '';
}

# run the complete set of changes into the database
sub apply_consolidated_results {
    my $self                                          = shift;
    my $default_change_policy                         = shift;
    my $hostgroups_to_assign                          = shift;    # arrayref
    my $assign_hostgroups_to_existing_hostgroup_hosts = shift;
    my $monarch_groups_to_assign                      = shift;    # arrayref
    my $assign_monarch_groups_to_existing_group_hosts = shift;
    my $elemental_results                             = shift;
    my $persistence                                   = shift;
    my $failed                                        = 0;
    my $failure_reason                                = '';
    my $is_data_problem                               = undef;
    my @database_changes                              = ();
    my $outcome                                       = undef;
    my $dbh                                           = undef;
    local $_;

    # FIX MINOR:  Extend the upstream code to allow a trigger file to contain a "change_policy"
    # option, as a per-host-discovery override to the global configuration of the change policy.
    my $change_policy = $elemental_results->{change_policy} // $default_change_policy;

    # FIX MAJOR:  The present construction of this routine only covers a $change_policy of 'non_destructive'.
    # There are some early tendrils of support for 'from_scratch' and 'ignore_extras' policies, but they have
    # not yet been tested.
    #
    # With respect to implementing a 'from_scratch' change policy, that could be simulated by simply deleting
    # the entire host and having it be re-added based on the discovery results.  The problem with that
    # approach is that the host_id, service_id, and perhaps some similar values in the "monarch" database
    # would end up with completely new values every time a new discovery is run for a host under that change
    # policy.  That shouldn't affect monitoring as a whole, since most monitoring will be based on object
    # names instead of such ID values, and Foundation has its own independent sets of such ID values that
    # ought to remain unchanged.  But it would cause an awful lot of churn in the "monarch" database ID
    # values, making it significantly more difficult to track changes over time and harder for a human to read
    # the database as the ID values grow large.  It would be better to strip back the database objects to
    # only those which are known to be supported by the new discovery results, and then run in the discovery
    # results with some sort of "replace" option to get new detailed settings whenever that makes sense.

    # FIX MAJOR:  Compare the $elemental_results against the Monarch database, and further
    # validate the upstream discovery results in that aspect.  Record even more detail
    # about all the changes to be made, if that makes sense.  If that validation succeeds,
    # apply all of the elemental configuration changes listed in the $elemental_results or
    # as otherwise determined by comparison with the database to the database.  Aggregate
    # the success or failure along the way, and log individual failures.

    # Take into account the current content of host and service profiles, service definitions, and so forth.
    # Log problems, and report out failure.

    unless ($failed) {
	if ( not $self->database_connect() ) {
	    $failed          = 1;
	    $failure_reason  = 'cannot connect to the database';
	    $is_data_problem = 0;
	}
    }

    unless ($failed) {
	if ( not $self->begin_config_changes() ) {
	    $failed          = 1;
	    $failure_reason  = 'cannot start a database transaction';
	    $is_data_problem = 0;
	}
    }

    # Take these steps:
    #
    # FIX MAJOR:
    # The following list currently only specifies adding stuff to Monarch (more or less
    # corresponding to a $change_policy value of "non_destructive").  We also need to
    # decide exactly what to do with existing data beyond service profiles and services
    # that are mentioned in the elemental results, based on the $change_policy.  Do we
    # leave those alone, or strip them before applying configuration from the elemental
    # results?  In general, we wish to leave alone as much as possible any objects which
    # already exist and will be part of the configuration after the discovery results are
    # applied, so as to avoid pointless churning of database object-ID values that would
    # disrupt the ability to identify a continuously-monitored object in both the run-time
    # and archive databases.
    #
    # We also need to make decisions on how to treat application of service-level and
    # service-instance-level attributes when the service or instance already exists.
    # Do we override existing values, or leave them be?
    #
    # (*) validate that the mentioned host profile actually exists;
    #     make a list of all associated service profiles
    # (*) validate that all service profiles mentioned in the discovery results actually
    #     exist in Monarch; make lists of all services associated with both the mentioned
    #     and derivative service profiles
    # (*) validate that all services mentioned in the discovery results actually exist in Monarch
    # (*) check if there are any generic services referenced directly or indirectly by the discovery
    #     results, shared with generic services referenced elsewhere directly or indirectly by the
    #     discovery results, with different values of the service-level or instance-level attributes
    #     given in the elemental results
    # (*) check whether the host already exists
    # (*) If the host already exists, find the host profile already applied to this host, if any
    # (*) If the host already exists, make a list of all the service profiles applied to this host,
    #     either indirectly through a possible host profile applied to the host or directly to the host
    # (*) If the host already exists, make a list of all the services applied to this host,
    #     so we know unambiguously later on which services were added and which not
    # (*) If the host already exists, make a list of all the service instances applied to this host,
    #     so we know unambiguously later on which service instances were added and which not
    # (*) create the host if necessary, presumably using the specified host profile
    # (*) check whether the host already has this host profile assigned
    # (*) assign and apply the host profile if necessary
    # (*) check whether the host already has all of these service profiles applied
    # (*) assign and apply each service profile if necessary
    # (*) check whether the host already has all the services referenced by the directly or indirectly
    #     discovered service profiles and all the individual services listed in the discovery results
    # (*) add any missing services referenced by the host's service profiles and individual services
    #     listed in the discovery results
    # (*) add any missing service instances implied by the discovery results; at the same time,
    #     make sure all of the instance_suffix, instance_cmd_args, and instance_ext_args values,
    #     if available in the elemental results, are applied to each service instance that is
    #     newly added to the host
    # (*) make sure all of the check_command, command_arguments, and externals_arguments
    #     values, if available in the elemental results, are applied to each service that
    #     was newly added to the host
    # (*) make sure that host externals are added to the host if they do not exist there, if they
    #     are assigned to the host profile that the discovery results mention and that host profile
    #     was not earlier applied to the host because it was already applied
    # (*) make sure that service externals are added to the host services if they do not exist
    #     there, for all services indirectly or directly referenced by the discovery results,
    #     whether or not those host services were already present before the discovery results
    #     were processed
    # (*) make sure the host is a member of some particular well-known hostgroup, so it shows up
    #     categorized both in Monarch and in Foundation under at least one hostgroup name
    # (*) make sure the host is a member of some particuler well-known Monarch Group, so the
    #     process of building externals will know where to put them

    # It is also possible to assign a hostgroup to a service profile, and then to apply the service
    # profile to all of the hosts in the hostgroup.  Doing so will directly apply all the services
    # in the service profile to those hosts, but it will not assign the service profile to the
    # host, so once the applying of the service profile to the hostgroup is done, there is no more
    # indirection evident at the host level.  Hence we ignore here any associations between service
    # profiles and hostgroups that happen to contain the current host.

    # (*) validate that the mentioned host profile actually exists;
    #     make a list of all associated service profiles

    # The $elemental_results should have undergone earlier validation, so by this time
    # we know there is one and only one host profile mentioned in those results, and it
    # makes sense to use a fixed [0] array element to extract that name.
    my $discovered_host_profile_name     = ( keys %{ $elemental_results->{host_profiles} } )[0];
    my @discovered_service_profile_names = keys %{ $elemental_results->{service_profiles} };
    my @discovered_service_names         = keys %{ $elemental_results->{services} };

    # All the usual Monarch fields for a host profile.
    my %discovered_host_profile = ();

    # service_profile_name => serviceprofile_id
    my %host_profile_service_profiles = ();

    unless ($failed) {
	eval {
	    %discovered_host_profile = StorProc->fetch_one( 'profiles_host', 'name', $discovered_host_profile_name );
	    if (0) {
		foreach my $key ( sort keys %discovered_host_profile ) {
		    print "host profile $key => $discovered_host_profile{$key}\n";
		}
	    }
	    if ( not %discovered_host_profile ) {
		$is_data_problem = 1;
		die "host profile $discovered_host_profile_name does not exist\n";
	    }

	    # service_profile_name => serviceprofile_id
	    %host_profile_service_profiles = StorProc->get_host_profile_service_profiles( $discovered_host_profile{hostprofile_id} );
	    if (0) {
		foreach my $key ( sort keys %host_profile_service_profiles) {
		    print "service profile $key => $host_profile_service_profiles{$key}\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) validate that all service profiles mentioned in the discovery results actually
    #     exist in Monarch; make lists of all services associated with both the mentioned
    #     and derivative service profiles

    my %distinct_discovered_service_profiles = ();

    # serviceprofile_name => [associated service names]
    my %service_profile_service_list = ();

    unless ($failed) {
	eval {
	    my @all_discovered_service_profiles = ( keys %host_profile_service_profiles, @discovered_service_profile_names );
	    @distinct_discovered_service_profiles{@all_discovered_service_profiles} = (undef) x @all_discovered_service_profiles;
	    foreach my $service_profile ( keys %distinct_discovered_service_profiles ) {
		my %one_service_profile = StorProc->fetch_one( 'profiles_service', 'name', $service_profile );
		if ( not %one_service_profile ) {
		    $is_data_problem = 1;
		    die "service profile $service_profile does not exist\n";
		}

		# We get back for each entry:
		#     service name (as hash key)
		#     {template}    service template name
		#     {command}     command line or commmand name
		#     {dependency}  ????????????                         (not reliable, due to an apparent bug in StorProc)
		#     {extinfo}     extended service info template name  (not reliable, due to an apparent bug in StorProc)
		# Because this data is so limited, we will only use the service names.
		my %service_profile_services = StorProc->get_service_profile_services( $one_service_profile{serviceprofile_id} );

		@{ $service_profile_service_list{$service_profile} } = keys %service_profile_services;
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) validate that all services mentioned in the discovery results actually exist in Monarch

    unless ($failed) {
	eval {
	    foreach my $service_name (@discovered_service_names) {
		my %one_service = StorProc->fetch_one( 'service_names', 'name', $service_name );
		if ( not %one_service ) {
		    $is_data_problem = 1;
		    die "service $service_name does not exist\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) check if there are any generic services referenced directly or indirectly by the discovery
    #     results, shared with generic services referenced elsewhere directly or indirectly by the
    #     discovery results, with different values of the service-level or instance-level attributes
    #     given in the elemental results

    my %all_service_sensors   = ();
    my %all_service_instances = ();

    unless ($failed) {
	foreach my $host_profile ( keys %{ $elemental_results->{host_profiles} } ) {
	    ##
	    ## This overall block of code is partly about checking conflicts with respect to applying instances
	    ## to services.  That said, earlier code that validated the discovery instructions as a whole without
	    ## reference to the Monarch database may have marked the discovery results as being invalid if a <host>
	    ## sensor included an instance_suffix field.  So the work we do right here in this inner block is
	    ## perhaps speculative, supporting some future capability in that regard, if it makes any sense at all.
	    ##
	    ## Record instances for services assigned to service profiles assigned to the discovered host profile.
	    ##
	    my %host_profile_services = ();
	    foreach my $service_profile ( keys %host_profile_service_profiles ) {
		@host_profile_services{ @{ $service_profile_service_list{$service_profile} } } =
		  (undef) x scalar @{ $service_profile_service_list{$service_profile} };
	    }
	    my $instances = $elemental_results->{host_profiles}{$host_profile}{instances};
	    foreach my $service ( keys %host_profile_services ) {
		push @{ $all_service_sensors{$service} }, { sensor_class => 'host_profiles', sensor_item => $host_profile };
		push @{ $all_service_instances{$service} }, $instances;
	    }
	}
	foreach my $service_profile ( keys %{ $elemental_results->{service_profiles} } ) {
	    ## Record instances for services assigned to discovered service profiles.
	    my $instances = $elemental_results->{service_profiles}{$service_profile}{instances};
	    foreach my $service ( @{ $service_profile_service_list{$service_profile} } ) {
		push @{ $all_service_sensors{$service} }, { sensor_class => 'service_profiles', sensor_item => $service_profile };
		push @{ $all_service_instances{$service} }, $instances;
	    }
	}
	foreach my $service ( keys %{ $elemental_results->{services} } ) {
	    ## Record instances for discovered services.
	    my $instances = $elemental_results->{services}{$service}{instances};
	    push @{ $all_service_sensors{$service} }, { sensor_class => 'services', sensor_item => $service };
	    push @{ $all_service_instances{$service} }, $instances;
	}
    }

    unless ($failed) {
	foreach my $service ( keys %all_service_sensors ) {
	    if ( @{ $all_service_sensors{$service} } > 1 ) {
		my $check_command_class       = undef;
		my $check_command_item        = undef;
		my $check_command             = undef;
		my $command_arguments_class   = undef;
		my $command_arguments_item    = undef;
		my $command_arguments         = undef;
		my $externals_arguments_class = undef;
		my $externals_arguments_item  = undef;
		my $externals_arguments       = undef;
		my %instance_suffix_sensor    = ();

		# The processing here currently flags non-overlapping sets of service-level attributes as being a broken
		# discovery result.  That allows us to simplify downstream code, which can depend on any one sensor that
		# creates a new service to have all the service-level attributes available in just one single designated
		# sensor result.  If we relax the model here to allow non-overlapping sets of attributes, the downstream
		# code will need to be generalized as well, to pull in service-level attributes from every sensor result
		# that relates to this service.

		foreach my $service_sensor ( @{ $all_service_sensors{$service} } ) {
		    my $sensor_class   = $service_sensor->{sensor_class};
		    my $sensor_item    = $service_sensor->{sensor_item};
		    my $sensor_results = $elemental_results->{$sensor_class}{$sensor_item};
		    unless ($failed) {
			if ( not defined $check_command_item ) {
			    $check_command_class = $sensor_class;
			    $check_command_item  = $sensor_item;
			    $check_command       = $sensor_results->{check_command};
			}
			else {
			    if ( defined $sensor_results->{check_command} ) {
				if ( not defined $check_command ) {
				    $failed = 1;
				}
				elsif ( $check_command ne $sensor_results->{check_command} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $check_command ) {
				$failed = 1;
			    }
			    if ($failed) {
				( my $singular_sensor_class        = $sensor_class )        =~ s/s$//;
				( my $singular_check_command_class = $check_command_class ) =~ s/s$//;
				$failure_reason = "when checking the intended setup for service '$service', found conflicting values"
				  . " of check_command in sensor results yielding $singular_sensor_class '$sensor_item'";
				$failure_reason .= " and $singular_check_command_class '$check_command_item'"
				  if $check_command_class ne $sensor_class
				      or $check_command_item ne $sensor_item;
			    }
			}
		    }
		    unless ($failed) {
			if ( not defined $command_arguments_item ) {
			    $command_arguments_class = $sensor_class;
			    $command_arguments_item  = $sensor_item;
			    $command_arguments       = $sensor_results->{command_arguments};
			}
			else {
			    if ( defined $sensor_results->{command_arguments} ) {
				if ( not defined $command_arguments ) {
				    $failed = 1;
				}
				elsif ( $command_arguments ne $sensor_results->{command_arguments} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $command_arguments ) {
				$failed = 1;
			    }
			    if ($failed) {
				( my $singular_sensor_class            = $sensor_class )            =~ s/s$//;
				( my $singular_command_arguments_class = $command_arguments_class ) =~ s/s$//;
				$failure_reason = "when checking the intended setup for service '$service', found conflicting values"
				  . " of command_arguments in sensor results yielding $singular_sensor_class '$sensor_item'";
				$failure_reason .= " and $singular_command_arguments_class '$command_arguments_item'"
				  if $command_arguments_class ne $sensor_class
				      or $command_arguments_item ne $sensor_item;
			    }
			}
		    }
		    unless ($failed) {
			if ( not defined $externals_arguments_item ) {
			    $externals_arguments_class = $sensor_class;
			    $externals_arguments_item  = $sensor_item;
			    $externals_arguments       = $sensor_results->{externals_arguments};
			}
			else {
			    if ( defined $sensor_results->{externals_arguments} ) {
				if ( not defined $externals_arguments ) {
				    $failed = 1;
				}
				elsif ( $externals_arguments ne $sensor_results->{externals_arguments} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $externals_arguments ) {
				$failed = 1;
			    }
			    if ($failed) {
				( my $singular_sensor_class = $sensor_class ) =~ s/s$//;
				( my $singular_externals_arguments_class = $externals_arguments_class ) =~ s/s$//;
				$failure_reason = "when checking the intended setup for service '$service', found conflicting values"
				  . " of externals_arguments in sensor results yielding $singular_sensor_class '$sensor_item'";
				$failure_reason .= " and $singular_externals_arguments_class '$externals_arguments_item'"
				  if $externals_arguments_class ne $sensor_class
				      or $externals_arguments_item ne $sensor_item;
			    }
			}
		    }
		    unless ($failed) {
			## Presently, this code disallows having more than one sensor match with an identical instance_suffix.
			## That is the most aggressive possible type of validation.
			##
			## FIX MAJOR:  Verify that we are doing the right thing here with respect to a cardinality-'first'
			## sensor result with multiple instances in the discovery results.  (We should be ignoring all the
			## remaining instances for that sensor, in that case.)
			##
			foreach my $instance ( @{ $sensor_results->{instances} } ) {
			    if ( defined $instance->{instance_suffix} ) {
				if ( exists $instance_suffix_sensor{ $instance->{instance_suffix} } ) {
				    unless ( $instance_suffix_sensor{ $instance->{instance_suffix} }{class} eq $sensor_class
					&& $instance_suffix_sensor{ $instance->{instance_suffix} }{item} eq $sensor_item
					&& $sensor_results->{cardinality} eq 'first' )
				    {
					( my $singular_sensor_class = $sensor_class ) =~ s/s$//;
					( my $singular_instance_suffix_class = $instance_suffix_sensor{ $instance->{instance_suffix} }{class} ) =~ s/s$//;
					$failed         = 1;
					$failure_reason = "when checking the intended setup for service '$service', found duplicate values of instance_suffix"
					  . " ('$instance->{instance_suffix}') in sensor results yielding $singular_sensor_class '$sensor_item'";
					$failure_reason .= " and $singular_instance_suffix_class '$instance_suffix_sensor{$instance->{instance_suffix}}{item}'"
					  if $instance_suffix_sensor{ $instance->{instance_suffix} }{class} ne $sensor_class
					      or $instance_suffix_sensor{ $instance->{instance_suffix} }{item} ne $sensor_item;
				    }
				}
				else {
				    $instance_suffix_sensor{ $instance->{instance_suffix} } = { class => $sensor_class, item => $sensor_item };
				}
			    }
			    else {
				my $empty_string = '';
				$instance_suffix_sensor{$empty_string} = { class => $sensor_class, item => $sensor_item };
			    }

			    ## FIX LATER:  If we relax the previous test and allow duplicate instance_suffix values
			    ## across multiple matched sensors that all ultimately refer to the same service, check
			    ## that all the important instance-level fields for those instances are identical:
			    ##
			    ##     instance_cmd_args
			    ##     instance_ext_args
			    ##
			    ## (That presumes we don't allow one instance to supply just one of those values and some
			    ## other instance to provide just the other, without directly conflicting values.)
			    ##
			    ## Note that since multiple matching sensors might possibly contribute different parts of these
			    ## items without otherwise conflicting, we use the "consolidate_discovery_results" routine to
			    ## collapse down the discovery results to a final form before trying to apply them to Monarch.
			    ## The processing here takes place after that consolidation.
			    ##
			    ## Also note that if we do relax that test, we will need to elaborate code in the
			    ## consolidate_discovery_results() routine that merges instances of sensors that
			    ## name the same $sensor_class object, so as to eliminate duplicates.
			    ##
			    last if $failed;
			}
		    }
		    last if $failed;
		}
		unless ($failed) {
		    ## If there is more than one sensor instance over all matching sensors that ultimately configure the
		    ## same $service, then all instance_suffix values for those sensor instances must be non-empty strings.
		    ## Which is to day, you can't have the discovery results configuring just a base instance of a service
		    ## while simultaneously configuring some non-base instances.
		    my $empty_string = '';
		    if ( scalar keys %instance_suffix_sensor > 1 and exists $instance_suffix_sensor{$empty_string} ) {
			( my $singular_instance_suffix_class = $instance_suffix_sensor{$empty_string}{class} ) =~ s/s$//;
			$failed = 1;
			$failure_reason =
			    "when checking the intended setup for service '$service', found an empty or missing value of instance_suffix"
			  . " in sensor results yielding $singular_instance_suffix_class '$instance_suffix_sensor{$empty_string}{item}' when the"
			  . " results for some other matching sensor that configures this same service contain a non-empty instance_suffix value";
			## Let's not make it too difficult for the user to understand exactly which "other matching sensor" is involved.
			delete $instance_suffix_sensor{$empty_string};
			my $other_matching_sensor_instance_suffix = ( keys %instance_suffix_sensor )[0];
			( $singular_instance_suffix_class = $instance_suffix_sensor{$other_matching_sensor_instance_suffix}{class} ) =~ s/s$//;
			$failure_reason .= "; compare to the instance_suffix in the sensor results yielding"
			  . " $singular_instance_suffix_class '$instance_suffix_sensor{$other_matching_sensor_instance_suffix}{item}'";
		    }
		}
	    }
	    last if $failed;
	}
    }

    # (*) check whether the host already exists

    my %existing_host = ();
    unless ($failed) {
	eval {
	    %existing_host = StorProc->fetch_one( 'hosts', 'name', $elemental_results->{chosen_hostname} );
	    if ( not %existing_host ) {
		$self->{logger}->notice("NOTICE:  Host \"$elemental_results->{chosen_hostname}\" does not exist and will be added.");
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) If the host already exists, find the host profile already applied to this host, if any

    # FIX MAJOR:  where do we use $existing_host_profile_name once we have it?

    my $existing_host_profile_name = undef;
    unless ($failed) {
	eval {
	    if ( %existing_host and defined $existing_host{hostprofile_id} ) {
		my %existing_host_profile = StorProc->fetch_one( 'profiles_host', 'hostprofile_id', $existing_host{hostprofile_id} );
		if ( not %existing_host_profile ) {
		    $is_data_problem = 0;
		    die "cannot fetch host profile for host profile ID $existing_host{hostprofile_id}\n";
		}
		$existing_host_profile_name = $existing_host_profile{name};
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) If the host already exists, make a list of all the service profiles applied to this host,
    #     either indirectly through a possible host profile applied to the host or directly to the host

    # FIX MAJOR:  Where do we subsequently use the %existing_host_service_profiles hash?
    # Answer:  it may come in especially handy when we need to figure out what has been incrementally
    # added vs. already existed, when it comes to properly implementing the $change_policy.

    my %serviceprofile_id_by_name = ();
    my %serviceprofile_name_by_id = ();

    # We make this a name=>id hash, partly in case we need the id values later on and partly because it is
    # convenient to use the hash as a lookup device to see if a given service profile is applied to the host.
    my %existing_host_service_profiles = ();
    unless ($failed) {
	eval {
	    %serviceprofile_id_by_name = StorProc->get_table_objects( 'profiles_service', '0' );
	    %serviceprofile_name_by_id = StorProc->get_table_objects( 'profiles_service', '1' );
	    if (%existing_host) {
		if ( defined $existing_host{hostprofile_id} ) {
		    ## service_profile_name => serviceprofile_id
		    %existing_host_service_profiles = StorProc->get_host_profile_service_profiles( $existing_host{hostprofile_id} );
		}
		my @host_serviceprofile_ids =
		  StorProc->fetch_list_where( 'serviceprofile_host', 'serviceprofile_id', { 'host_id' => $existing_host{host_id} } );
		@existing_host_service_profiles{ map { $serviceprofile_name_by_id{$_} } @host_serviceprofile_ids } = @host_serviceprofile_ids;
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) If the host already exists, make a list of all the services applied to this host,
    #     so we know unambiguously later on which services were added and which not

    # The %existing_host_services hash is used later on when we need to figure out what services have been
    # incrementally added vs. already existed, when it comes to applying service-level field changes in the
    # discovery results under change policies that prefer to preserve existing setup.
    #
    # This hash is indexed by service_id, not by the service name.  To get the name when we need it,
    # we will need to use the $existing_host_service_instances{$service_id}{servicename_id} field.
    my %existing_host_services = ();
    unless ($failed) {
	eval {
	    if (%existing_host) {
		%existing_host_services = StorProc->get_host_services_detail( $existing_host{host_id} );
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) If the host already exists, make a list of all the service instances applied to this host,
    #     so we know unambiguously later on which service instances were added and which not

    # FIX MAJOR:  Where do we subsequently use the %existing_host_service_instances hash?
    # Answer:  it may come in especially handy when we need to figure out what has been incrementally
    # added vs. already existed, when it comes to properly implementing the $change_policy.

    # This three-level hash is indexed by the service_id, the instance name (suffix),
    # and finally the several instance fields (id, status, args, ext_args, inh_ext_args).
    my %existing_host_service_instances = ();
    unless ($failed) {
	eval {
	    foreach my $service_id ( keys %existing_host_services ) {
		$existing_host_service_instances{$service_id} = { StorProc->get_service_instances($service_id) };
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) create the host if necessary, presumably using the specified host profile

    unless ($failed) {
	eval {
	    if ( not %existing_host ) {
		$self->{logger}->notice( "NOTICE:  Host \"$elemental_results->{chosen_hostname}\" is being "
		      . ( $persistence ne 'commit' ? 'provisionally ' : '' )
		      . 'added.' );

		# FIX LATER:  We use the "os_type" field from the discovery results and plug that into
		#             the $new_host{os} field; but testing against GWMEE 7.2.0 shows that the
		#             API->import_host() routine is not paying any attention to the "os" field.

		( my $sanitized_os_type = $elemental_results->{os_type} )            =~ tr/-._ a-zA-Z0-9//cd;
		( my $sanitized_agent   = $elemental_results->{registration_agent} ) =~ tr/-._ a-zA-Z0-9//cd;

		my %new_host = ();
		$new_host{new}            = 1;
		$new_host{name}           = $elemental_results->{chosen_hostname};
		$new_host{alias}          = $elemental_results->{chosen_alias};
		$new_host{address}        = $elemental_results->{chosen_address};
		$new_host{os}             = $sanitized_os_type;
		$new_host{hostprofile_id} = $discovered_host_profile{hostprofile_id};
		$new_host{comment}        = "# host added by $sanitized_agent";

		push @database_changes, "* adding host $elemental_results->{chosen_hostname} with host profile $discovered_host_profile_name\n";
		my @results = API->import_host( \%new_host );
		my $results = join( "\n", @results );

		if ( $results =~ /error/i ) {
		    $self->{logger}->error("ERROR:  import_host() call returns error message:\n$results");
		    ## Whether this condition is a data problem or a physical problem is a bit ambiguous.
		    ## We're calling it a physical problem here because most of the error conditions inside
		    ## API->import_host() would arise only if we had some problem with modifying the database,
		    ## not a logical conflict with the data itself.  Similar logic may apply elsewhere with
		    ## respect to how we set the value of $is_data_problem.
		    $is_data_problem = 0;
		    die "cannot add host \"$elemental_results->{chosen_hostname}\" to the database\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) check whether the host already has this host profile assigned

    # FIX MINOR:  Logically, we should go further and not only check that the host profile is assigned, but
    # also that it has been applied.  For the time being, we're going to skip the more in-depth verification,
    # and just assume that if it was previously assigned, then it was also previously applied.  But in that
    # case, we must be sure that there are no additional service profiles currently associated with the host
    # profile that were not assigned and applied at the earlier time, that must now be assigned and applied
    # (probably in later code below).

    my $discovered_host_profile_is_assigned = 0;
    my %host_details = ();
    unless ($failed) {
	eval {
	    %host_details = StorProc->fetch_one( 'hosts', 'name', $elemental_results->{chosen_hostname} );
	    if ( not %host_details ) {
		$self->{logger}->error("ERROR:  Host $elemental_results->{chosen_hostname} does not exist when it should.");
		$is_data_problem = 0;
		die "host $elemental_results->{chosen_hostname} does not exist when it should\n";
	    }
	    if ( defined $host_details{hostprofile_id} ) {
		my %applied_host_profile = StorProc->fetch_one( 'profiles_host', 'hostprofile_id', $host_details{hostprofile_id} );
		if ( not %applied_host_profile ) {
		    $self->{logger}->error("ERROR:  Cannot fetch the host profile for host profile ID $host_details{hostprofile_id}.");
		    $is_data_problem = 0;
		    die "cannot fetch the host profile for host profile ID $host_details{hostprofile_id}\n";
		}
		$discovered_host_profile_is_assigned = ($applied_host_profile{name} eq $discovered_host_profile_name);
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) assign and apply the host profile if necessary

    ## FIX MAJOR:  Possibly, the host profile has changed in unexpected ways since the last time it was applied to this host.
    ## So we need to figure out what objects are associated with the host profile that we might need to apply to this host
    ## if we are not re-applying the host profile, and apply those objects separately.  (Or does it make sense to re-apply
    ## the host profile, and not worry about the details because they will then be handled automatically?)  In particular,
    ## if there are now additional or different host externals assigned to the host profile, we need to deal with that.

    unless ($failed) {
	eval {
	    if ( not $discovered_host_profile_is_assigned ) {
		## Because assigning the host profile and then applying it are all bundled into one large database transaction,
		## we don't worry here about trying to control the order in which the assignment and application are carried out.
		## If we didn't bundle them together, we might try to do the application first and the assignment second, to
		## provide some level of robustness and future self-healing in case the application fails.
		push @database_changes, "* assigning host profile $discovered_host_profile_name to host $elemental_results->{chosen_hostname}\n";
		my $result =
		  StorProc->update_obj( 'hosts', 'host_id', $host_details{host_id},
		    { 'hostprofile_id' => $discovered_host_profile{hostprofile_id} } );
		if ( $result =~ /Error/ ) {
		    $self->{logger}->error( "ERROR:  Assigning host profile $discovered_host_profile_name"
			  . " to host $elemental_results->{chosen_hostname} failed:  $result" );
		    $is_data_problem = 0;
		    die "could not assign host profile $discovered_host_profile_name to host $elemental_results->{chosen_hostname}\n";
		}

		## FIX MAJOR:  Check that this StorProc routine actually does what I want it to (deep application of the
		## host profile, in all its glory).  Compare to how monarch.cgi operates.  Look for associated JIRAs
		## regarding possibly incomplete behavior in applying a host profile.  In particular, start with these:
		##
		## GDMA-384     GDMA Auto Registration can upset with existing host specification
		## GWMON-2153   Assigning host to host profile via hostgroup doesn't appear to work
		## GWMON-6113   Applying host profile does not update the host detail with the contact group associated with the host profile
		## GWMON-7417   Applying host profile to hostgroups does not update host detail with host template
		## GWMON-9779   Escalations should be assigned and applied to host and service profiles.
		## GWMON-9846   Perserve servicegroups of a host, when applying host profile to this host
		## GWMON-9950   host-profile-sync processes some records then tries to delete records it just imported
		## GWMON-10271  Service group should not be removed after host profile is re-applied
		## GWMON-10511  dassmonarch Import_host call does not work
		## GWMON-10513  importing of externals with multiple host profiles generates errors and makes bad assignments
		## GWMON-10885  auto-registering an existing host does not assign/apply host or service externals
		## GWMON-12882  Changes to host template, host profile, and host external broken when applied via host profile by apply hostgroups
		## GWMON-13210  could use better support for service externals arguments
		##
		## FIX MAJOR:  Inasmuch as this routine is said not to modify the host template ID, nor the host escalation ID,
		## nor the service escalation ID, also deal with those aspects if we care about them.  (This is a fine point of
		## how the $change_policy is to be interpreted.)
		##
		## FIX MAJOR:  Look at the dassmonarch routine apply_host_template()
		## FIX MAJOR:  Look at the dassmonarch routine apply_hostescalation_tree()
		## FIX MAJOR:  Look at the dassmonarch routine apply_service_escalation_tree_to_host()
		## FIX MAJOR:  Look at the dassmonarch routine apply_serviceescalation_tree_to_hostservice()
		## FIX MAJOR:  Look at the dassmonarch routine apply_serviceescalation_tree_to_all_hostservices()
		##
		push @database_changes, "* applying host profile $discovered_host_profile_name to host $elemental_results->{chosen_hostname}\n";
		$result = StorProc->host_profile_apply( $discovered_host_profile{hostprofile_id}, [ $host_details{host_id} ] );
		if ( $result =~ /Error/ ) {
		    $self->{logger}->error( "ERROR:  Applying host profile $discovered_host_profile_name"
			  . " to host $elemental_results->{chosen_hostname} failed:  $result" );
		    $is_data_problem = 0;
		    die "could not apply host profile $discovered_host_profile_name to host $elemental_results->{chosen_hostname}\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) check whether the host already has all of these service profiles applied

    # FIX MAJOR:  There is the possibility that a service profile might be associated with a host not by direct
    # assignment to the host and not by assignment to the host's host profile, but via assignment to a hostgroup of
    # which the host is a member.  Check that path, too, to figure out what service profiles ought to be dealt with
    # in some manner in the subsequent step, even if such service profiles should be applied and not assigned.

    # When doing this checking, we must bear in mind that applying a host profile in the previous step
    # may have cascaded into assigning and applying any service profiles that are associated with the
    # host profile.  So we cannot depend solely on some earlier determination of what service profiles
    # are applied to the host; we are forced to check anew.

    # We make this a name=>id hash, because we need the id values later on.
    my %missing_service_profiles = ();
    unless ($failed) {
	eval {
	    my @host_serviceprofile_ids =
	      StorProc->fetch_list_where( 'serviceprofile_host', 'serviceprofile_id', { 'host_id' => $host_details{host_id} } );
	    my %current_host_service_profiles = ();
	    @current_host_service_profiles{ map { $serviceprofile_name_by_id{$_} } @host_serviceprofile_ids } = @host_serviceprofile_ids;
	    foreach my $service_profile ( keys %{ $elemental_results->{service_profiles} } ) {
		## FIX MAJOR:  test this both in the case that the host already existed, and in the case where we needed to create the host
		$missing_service_profiles{$service_profile} = $serviceprofile_id_by_name{$service_profile}
		  if not exists $current_host_service_profiles{$service_profile};
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) assign and apply each service profile if necessary

    unless ($failed) {
	## FIX MAJOR:  compare to the dassmonarch routine assign_and_apply_hostprofile_serviceprofiles()
	eval {
	    my $host_id = $host_details{host_id};
	    if ( not $host_id ) {
		$is_data_problem = 0;
		die "there is no valid host_id for host $elemental_results->{chosen_hostname}\n";
	    }

	    foreach my $service_profile ( keys %missing_service_profiles ) {
		my $serviceprofile_id = $missing_service_profiles{$service_profile};
		if ( not $serviceprofile_id ) {
		    $is_data_problem = 1;
		    die "there is no valid serviceprofile_id for service profile $service_profile\n";
		}

		# By construction of %missing_service_profiles above, the serviceprofile_host table must not already
		# include the (serviceprofile_id, host_id) pair of interest to us now.  So we insert it, to assign
		# this service profile to this host.  Afterward, we will still need to apply the profile to the host.
		#
		# We might get a "duplicate key value violates unique constraint" error here.  That particular type of
		# exception is caught within StorProc->insert_obj() and reported back here; it doesn't cause our own
		# eval{}; block to trap an exception.  That kind of exception might arise from either of two sources.
		# Possibly, we might be seeing concurrent inserts by another actor (perhaps an interactive user of the
		# Monarch UI).  I would have thought that our transaction isolation level would have blocked us from
		# seeing that, but then I don't fully understand the transaction serialization protocol and what would
		# happen if two processes tried to make the same changes at the same time.  Maybe we wouldn't see that
		# until we try to commit the entire transaction, and the commit might fail.  Or we might simply have had
		# the service profile already be applied as a result of it being referenced by the host profile that we
		# applied earlier.  That case should have been dealt with already by our having checked for the set of
		# service profiles only after we applied that host profile, so I don't expect that condition to be a
		# problem.  The upshot is that I don't see any way that we could get a valid duplicate key where we
		# would feel safe in continuing on, so we're going to fail the configuration changes if we see one.
		#
		push @database_changes, "* assigning service profile $service_profile to host $elemental_results->{chosen_hostname}\n";
		my $result = StorProc->insert_obj( 'serviceprofile_host', [ $serviceprofile_id, $host_id ] );
		if ( $result =~ /Error/ ) {
		    if ( $result =~ /duplicate/i ) {
			## This might possibly happen because of concurrent inserts by another actor (perhaps
			## an interactive user of the Monarch UI).  I'm not sure how such a thing would interact
			## with our having wrapped the changes here inside a serialized transaction, though.
			$is_data_problem = 0;
			die "service profile $service_profile was being concurrently assigned to host $elemental_results->{chosen_hostname}\n";
		    }
		    else {
			$is_data_problem = 0;
			die "could not assign service profile $service_profile (serviceprofile_id $serviceprofile_id); error: $result\n";
		    }
		}

		# $replace should be True, if existing services should be replaced by services in the service profile,
		# and false, if services should be merged.
		#
		# FIX MAJOR:  Verify correct operation for (i.e., correct interpretation of) every possible value of the
		# $change_policy.  It may be the case that neither 'replace' nor 'merge' for $replacestring adequately
		# expresses our intent for a $change_policy of 'ignore_extras', meaning that we might need to extend the
		# underlying StorProc routine to support more choices.
		#
		my $replacestring =
		    $change_policy eq 'from_scratch'    ? 'replace'
		  : $change_policy eq 'ignore_extras'   ? 'replace'
		  : $change_policy eq 'non_destructive' ? 'merge'
		  :                                       'merge';
		push @database_changes, "* applying service profile $service_profile to host $elemental_results->{chosen_hostname}\n";
		my ( $profcnt, $errormsg ) = StorProc->service_profile_apply( [$serviceprofile_id], $replacestring, [$host_id] );
		if ( @{$errormsg} ) {
		    $is_data_problem = 0;
		    die "Error applying service profile $service_profile: " . join( ', ', @{$errormsg} ) . "\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) check whether the host already has all the services referenced by the directly or indirectly
    #     discovered service profiles and all the individual services listed in the discovery results

    # You might think that all the services assigned to profiles should be there, but that might not be the case.  If
    # the host already had a particular service profile assigned (and presumably applied), and then the set of services
    # associated with that service profile is extended, and then new discovery results are processed, the service
    # profile will not be re-applied.  So the new services won't be associated with the host.  We must therefore check
    # explicitly for all the intended services, and add in any stragglers.  This must be done before we go trying to
    # make adjustments to the service-level data driven by the details of discovery results.

    my %missing_host_services = ();

    unless ($failed) {
	eval {
	    my @host_service_names = StorProc->get_host_services( $host_details{host_id} );

	    my %is_a_host_service = ();
	    @is_a_host_service{@host_service_names} = (1) x @host_service_names;

	    my %all_service_profile_services = ();
	    foreach my $service_profile ( keys %distinct_discovered_service_profiles ) {
		@all_service_profile_services{ @{ $service_profile_service_list{$service_profile} } } =
		  (1) x @{ $service_profile_service_list{$service_profile} };
	    }

	    ## Capture the names of all services directly or indirectly derived from the discovery results,
	    ## that are not already present on the host.
	    foreach my $service_name ( keys %all_service_profile_services, keys %{ $elemental_results->{services} } ) {
		$missing_host_services{$service_name} = 1 if not $is_a_host_service{$service_name};
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) add any missing services referenced by the host's service profiles and individual services
    #     listed in the discovery results

    # FIX MAJOR:  Don't we need to potentially customize each service we add here, according to details of the sensor results?
    # Or is that already handled, later on in this code?  How will we make sure that we apply the correct sensor customization
    # of such newly added services, or do we not worry about that because we earlier looked for conflicts and would not now be
    # doing this werk if there were any conflicts?

    unless ($failed) {
	eval {
	    my $hostname = $elemental_results->{chosen_hostname};
	    my %contactgroup_name_by_id = StorProc->get_table_objects( 'contactgroups', '1' );
	    foreach my $service_name ( keys %missing_host_services ) {
		my %s = StorProc->fetch_one( 'service_names', 'name', $service_name );
		if ( not %s ) {
		    $is_data_problem = 0;
		    die "cannot find service $service_name\n";
		}

		## FIX MINOR:  don't we already have these properties as %host_details?
		my %properties = StorProc->fetch_host($hostname);
		if ( defined $properties{'errors'} ) {
		    $is_data_problem = 0;
		    die join( "\n", @{ $properties{'errors'} } ) . "\n";
		}

		my @values = (
		    \undef,             $properties{'host_id'}, $s{'servicename_id'}, $s{'template'},
		    $s{'extinfo'},      $s{'escalation'},       '1',                  $s{'check_command'},
		    $s{'command_line'}, '',                     ''
		);
		push @database_changes, "* adding service $service_name to host $hostname\n";
		my $id = StorProc->insert_obj_id( 'services', \@values, 'service_id' );
		if ( $id =~ /^Error/ ) {
		    $is_data_problem = 0;
		    die "adding $service_name to host $hostname failed: $id\n";
		}

		my %so = StorProc->fetch_one( 'servicename_overrides', 'servicename_id', $s{'servicename_id'} );
		if (%so) {
		    my $data = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\n<data>\n";
		    foreach my $prop ( keys %so ) {
			unless ( $prop =~ /check_period|notification_period|event_handler|servicename_id/ ) {
			    $data .= "  <prop name=\"$prop\"><![CDATA[$so{$prop}]]>\n  </prop>\n";
			}
		    }
		    $data .= "</data>";
		    @values = ( $id, $so{'check_period'}, $so{'notification_period'}, $so{'event_handler'}, $data );
		    push @database_changes, "* adding inheritance overrides to service $service_name on host $hostname\n";
		    my $result = StorProc->insert_obj( 'service_overrides', \@values );
		    if ( $result =~ /^Error/ ) {
			$is_data_problem = 0;
			die "$result\n";
		    }
		}

# ========================================
=pod

FIX MAJOR:  Handling of dependency data is still a work in progress.

service_dependency fields are:

id                   unique identifier for this host-service dependency association
service_id           the host-service identifier for the service which has a dependency on some other service
host_id              the host identifier for the host whose service has a dependency on some other service
depend_on_host_id    host ID of the specific host owning the service that the {host_id, service_id} host service depends on
template             service_dependency_templates.id field that specifies the particular generic-service dependency
comment              text field, seemingly unused by the Monarch UI

service_dependency_templates.servicename_id used as service_names.servicename_id gives service_names.name as the name of the service being depended on

servicename_dependency fields are:

id                unique identifier for this generic-service dependency association
servicename_id    ID of the generic service having a dependency on one or more host services specified by {depend_on_host_id, template}
depend_on_host_id host ID of the specific host owning the service that the generic servicename_id service depends on; null if "same host"
template          service_dependency_templates.id field that specifies the particular generic-service dependency

service_dependency_templates fields are:

id             unique identifier for this service dependency template
name           unique name of this service dependency template
servicename_id | integer                | not null default 0
data           | text                   |
comment        text field, of no particular concern though it should probably be copied to the service_dependency.comment field

=cut
# ========================================

		if ( $s{'dependency'} ) {
		    ## FIX TODAY:  What is the point of %t ?  Did something not get implemented correctly here?
		    ## FIX TODAY:  Why is $properties{'host_id'} just duplicated here?  Shouldn't it have some
		    ## dependency on the specific service dependency being copied, not reflecting the host itself?
		    ## Fix this in dassmonarch add_service(), too.
		    ## FIX TODAY:  We should name the specific service dependency (dependent host and service) being established.
		    ## my %t = StorProc->fetch_one( 'service_dependency_templates', 'name', $s{'dependency'} );
		    @values = ( \undef, $id, $properties{'host_id'}, $properties{'host_id'}, $s{'dependency'}, '' );
		    push @database_changes, "* adding service dependency to service $service_name on host $hostname\n";
		    my $result = StorProc->insert_obj( 'service_dependency', \@values );
		    if ( $result =~ /^Error/ ) {
			$is_data_problem = 0;
			die "$result\n";
		    }
		}

		# transfer contactgroups
		my %where = ( 'servicename_id' => $s{'servicename_id'} );
		my @contactgroup_ids = StorProc->fetch_list_where( 'contactgroup_service_name', 'contactgroup_id', \%where );
		foreach my $cgid (@contactgroup_ids) {
		    my @values = ( $cgid, $id );
		    push @database_changes, "* adding contactgroup $contactgroup_name_by_id{$cgid} to service $service_name on host $hostname\n";
		    my $result = StorProc->insert_obj( 'contactgroup_service', \@values );
		    if ( $result =~ /^Error/ ) {
			$is_data_problem = 0;
			die "$result\n";
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) add any missing service instances implied by the discovery results; at the same time,
    #     make sure all of the instance_suffix, instance_cmd_args, and instance_ext_args values,
    #     if available in the elemental results, are applied to each service instance that is
    #     newly added to the host

    # For each service derived from the elemental results, whether indirectly via a host_profiles entry and its
    # associated service profiles and their associated services in Monarch, or indirectly via a service_profiles
    # entry and its associated services in Monarch, or directly via a services entry, find its respective existing
    # service instances, if any.  Check each instances entry in the elemental results that refers to this service,
    # indirectly or directly as described.  If it has some instance_suffix values that are not the empty string,
    # then we must create corresponding service instances if they do not already exist.

    my %servicename_id_by_service = ();
    my %service_id_by_service     = ();

    unless ($failed) {
	eval {
	    foreach my $service ( keys %all_service_instances ) {
		my %s = StorProc->fetch_one( 'service_names', 'name', $service );
		if ( not %s ) {
		    $is_data_problem = 0;
		    die "cannot find generic service $service\n";
		}
		my %hs =
		  StorProc->fetch_one_where( 'services', { 'host_id' => $host_details{host_id}, 'servicename_id' => $s{servicename_id} } );
		if ( not %hs ) {
		    $is_data_problem = 0;
		    die "cannot find host service $service on host $host_details{name}\n";
		}
		$servicename_id_by_service{$service} = $s{servicename_id};
		$service_id_by_service{$service}     = $hs{service_id};
		my %service_instances = StorProc->get_service_instances( $hs{service_id} );
		foreach my $instances ( @{ $all_service_instances{$service} } ) {
		    foreach my $instance (@$instances) {
			if ( exists( $instance->{instance_suffix} ) && $instance->{instance_suffix} ne '' ) {
			    if ( not exists $service_instances{ $instance->{instance_suffix} } ) {
				## For a newly created service instance, there's no point in not using data from the
				## discovery results to populate the arguments, externals_arguments, and inherit_ext_args
				## columns as opposed to always using some standard values for these columns, even if we
				## instrument the code elsewhere to set these values in a secondary pass.
				## FIX TODAY:  There may have been some trouble reported in testing with using a
				## single '!' character for the instance_cmd_args field.  Look into this.
				my @values = (
				    \undef, $hs{service_id}, $instance->{instance_suffix},
				    1,
				    $instance->{instance_cmd_args} // '!',
				    $instance->{instance_ext_args} // undef,
				    $instance->{instance_ext_args} ? '00' : 1
				);
				push @database_changes, "* adding instance $service$instance->{instance_suffix}"
				  . " to service $service on host $host_details{name}\n";
				my $result = StorProc->insert_obj( 'service_instance', \@values );
				if ( $result =~ /^Error/ ) {
				    $is_data_problem = 0;
				    die "$result\n";
				}
			    }
			    elsif ( $change_policy eq 'from_scratch' || $change_policy eq 'ignore_extras' ) {
				## Under certain change policies, we must update the data in existing service instances.
				my %values = ( status => 1 );
				if ( $change_policy eq 'from_scratch' ) {
				    ## FIX TODAY:  There may have been some trouble reported in testing with using a
				    ## single '!' character for the instance_cmd_args field.  Look into this.
				    $values{arguments}           = $instance->{instance_cmd_args} // '!';
				    $values{externals_arguments} = $instance->{instance_ext_args} // undef;
				    $values{inherit_ext_args} = $instance->{instance_ext_args} ? '00' : 1;
				}
				elsif ( $change_policy eq 'ignore_extras' ) {
				    $values{arguments}           = $instance->{instance_cmd_args} if defined $instance->{instance_cmd_args};
				    $values{externals_arguments} = $instance->{instance_ext_args} if defined $instance->{instance_ext_args};
				    $values{inherit_ext_args}    = '00'                           if defined $instance->{instance_ext_args};
				}
				else {
				    $is_data_problem = 0;
				    die "internal logic error";
				}
				push @database_changes, "* updating instance $service$instance->{instance_suffix}"
				  . " of service $service on host $host_details{name}\n";
				my $result =
				  StorProc->update_obj( 'service_instance', 'instance_id',
				    $service_instances{ $instance->{instance_suffix} }{id}, \%values );
				if ( $result =~ /^Error/ ) {
				    $self->{logger}->error( "ERROR:  Cannot update host $elemental_results->{chosen_hostname}"
					  . " service $service instance $instance->{instance_suffix}:  $result" );
				    $is_data_problem = 0;
				    die "cannot update host $elemental_results->{chosen_hostname}"
				      . " service $service instance $instance->{instance_suffix}\n";
				}
			    }
			}
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) make sure all of the check_command, command_arguments, and externals_arguments
    #     values, if available in the elemental results, are applied to each service that
    #     was newly added to the host

    # FIX MAJOR:  This construction currently only covers a $change_policy of 'non_destructive'.

    unless ($failed) {
	eval {
	    my %new_services          = ();
	    my %current_host_services = StorProc->get_host_services_detail( $host_details{host_id} );
	    foreach my $service_id ( keys %current_host_services ) {
		## $existing_host_services{$service_id}{servicename_id}
		if ( not exists $existing_host_services{$service_id} ) {
		    ##
		    ## This service was added somehow by our earlier processing.
		    ##
		    ## We have to figure out exactly what service mentioned in the elemental results, or what service
		    ## profile either mentioned directly in the elemental results or mentioned indirectly by the host
		    ## profile specified in the elemental results, created this service.  That tells us which sensor
		    ## results have the attributes we must apply to the new service.
		    ##
		    ## In general, it is possible that a given service might be added through multiple paths.  Sensor
		    ## attribute conflicts have been checked for earlier, so it won't matter in this code block which
		    ## of perhaps multiple sensors we choose here as the canonical sensor to apply the service-level
		    ## attributes from.
		    ##
		    ## FIX MAJOR:  Figure out if we need to handle indirectly-assigned services, added only via a host
		    ## profile.  It would make sense to do that only if we have one or more of the check_command,
		    ## command_arguments, or externals_arguments fields defined for such a host profile.  If that is the
		    ## case, we should probably apply the data from the host profile before we apply any data from the
		    ## service profiles, in case there is any overlap.
		    ##
		    my $service = StorProc->get_service_name($service_id);
		    if ( defined $elemental_results->{services}{$service} ) {
			$new_services{$service} = { service_id => $service_id, sensor_class => 'services', sensor_item => $service };
		    }
		    else {
			foreach my $service_profile ( keys %{ $elemental_results->{service_profiles} } ) {
			    my %service_profile_services =
			      StorProc->get_service_profile_services( $serviceprofile_id_by_name{$service_profile} );
			    ## If this service profile has this service as a member, make the assignment.
			    if ( exists $service_profile_services{$service} ) {
				## FIX MAJOR:  Verify that we don't have more than one sensor that directly or indirectly references
				## this service, with possibly conflicting attributes; that should have been checked above.
				$new_services{$service} =
				  { service_id => $service_id, sensor_class => 'service_profiles', sensor_item => $service_profile };
				last;
			    }
			}
		    }
		    if ( not exists $new_services{$service} ) {
			foreach my $service_profile ( keys %host_profile_service_profiles ) {
			    ## Don't bother checking this $service_profile if we already checked it just above.
			    if ( not exists $elemental_results->{service_profiles}{$service_profile} ) {
				my %service_profile_services =
				  StorProc->get_service_profile_services( $serviceprofile_id_by_name{$service_profile} );
				## If this service profile has this service as a member, make the assignment.
				if ( exists $service_profile_services{$service} ) {
				    ## FIX MAJOR:  Verify that we don't have more than one sensor that directly or indirectly references
				    ## this service, with possibly conflicting attributes; that should have been checked above.
				    $new_services{$service} = {
					service_id   => $service_id,
					sensor_class => 'host_profiles',
					sensor_item  => $discovered_host_profile_name
				    };
				    last;
				}
			    }
			}
		    }
		    if ( not exists $new_services{$service} ) {
			## Somehow the host service appeared, but now we can't figure out how that happened.
			## This seems more like a database-integrity or coding problem than a data problem.
			$is_data_problem = 0;
			die "cannot find any service, service_profile, or host_profile sensor in the discovery results that"
			  . " directly or indirectly added service \"$service\" to host $elemental_results->{chosen_hostname}\n";
		    }
		}
	    }

	    ## Here for each service, we only apply the service-level attributes from a single designated sensor
	    ## result, for the full set of such attributes that we manipulate.  (Service-instance-level attributes
	    ## are handled elsewhere, not here.)  But what if different sensors provide different non-overlapping
	    ## parts of the set of sensor-level atttributes?  If we didn't see any effective conflicts in their
	    ## values above, we will now be missing some attributes.
	    ##
	    ## As of this writing, the upstream error checking will flag non-overlapping sets of service-level
	    ## attributes as being inconsistent, because of the difference between undefined and defined values.
	    ## That will fail the processing then and there.  So until and unless we modify that code to allow
	    ## non-overlapping sets of attributes, we don't worry about that here.
	    ##
	    foreach my $service ( keys %new_services ) {
		my $service_id     = $new_services{$service}{service_id};
		my $sensor_class   = $new_services{$service}{sensor_class};
		my $sensor_item    = $new_services{$service}{sensor_item};
		my $sensor_results = $elemental_results->{$sensor_class}{$sensor_item};

		if ( defined $sensor_results->{check_command} ) {
		    ## The services.check_command field is an integer, not a text field.  So we need to first look up the
		    ## ID value to stuff into that field.  We further validate that this is a "check" command, not some
		    ## other type like a "notify" command that has no business being used as a service-check command.
		    my %command = StorProc->fetch_one_where( 'commands', { name => $sensor_results->{check_command}, type => 'check' } );
		    if ( not %command ) {
			$self->{logger}->error( "ERROR:  Cannot find an existing check command \"$sensor_results->{check_command}\""
			      . " to apply to service $service on host $elemental_results->{chosen_hostname}." );
			$is_data_problem = 1;
			die "cannot find an existing check command \"$sensor_results->{check_command}\""
			  . " to apply to service $service on host $elemental_results->{chosen_hostname}\n";
		    }

		    push @database_changes, "* updating check command for service $service on host $elemental_results->{chosen_hostname}\n";
		    my $result = StorProc->update_obj( 'services', 'service_id', $service_id, { check_command => $command{command_id} } );
		    if ( $result =~ /^Error/ ) {
			$self->{logger}->error( "ERROR:  Cannot update check_command for service $service"
			      . " on host $elemental_results->{chosen_hostname}:  $result" );
			$is_data_problem = 0;
			die "cannot update check_command for service $service on host $elemental_results->{chosen_hostname}\n";
		    }
		    ## FIX MAJOR:  also possibly deal with service overrides?
		}

		if ( defined $sensor_results->{command_arguments} ) {
		    ## There is no command_arguments field in the services table; there is only a command_line
		    ## field.  We must therefore go to some lengths to pick up the command name to prefix to the
		    ## command_arguments which are supplied in the discovery results.

		    my %host_service = StorProc->fetch_one( 'services', 'service_id', $service_id );
		    if ( not %host_service ) {
			$self->{logger}->error("ERROR:  Cannot fetch the host service $service on host $elemental_results->{chosen_hostname}.");
			$is_data_problem = 0;
			die "cannot fetch the host service $service on host $elemental_results->{chosen_hostname}\n";
		    }

		    ## The services.check_command field might be null and instead be inherited from the service
		    ## template.  In turn, the service_template.check_command field for the service template might
		    ## also be null and instead be inherited from its own parent service template.  This might be
		    ## true through several levels of parent service templates.
		    ##
		    my $command_id         = $host_service{check_command};
		    my $servicetemplate_id = $host_service{servicetemplate_id};

		    ## The "monarch" database does not impose a "not null" constraint on the services.servicetemplate_id
		    ## field, but the Monarch UI code requires that this field be populated before it allows you to
		    ## create a new service, and does not allow this field to be cleared (there is no notion that having
		    ## it cleared would mean that the template should be inherited instead from the generic service on
		    ## which this host service is based).  So we may as well check to ensure that condition still holds.
		    ##
		    if ( not defined $servicetemplate_id ) {
			$self->{logger}->error(
			    "ERROR:  Cannot find the service template for service $service on host $elemental_results->{chosen_hostname}.");
			$is_data_problem = 0;
			die "cannot find the service template for service $service on host $elemental_results->{chosen_hostname}\n";
		    }

		    while ( not defined($command_id) and defined($servicetemplate_id) ) {
			my %service_template = StorProc->fetch_one( 'service_templates', 'servicetemplate_id', $servicetemplate_id );
			if ( not %service_template ) {
			    $self->{logger}->error("ERROR:  Cannot fetch service template ID $servicetemplate_id.");
			    $is_data_problem = 0;
			    die "cannot fetch service template ID $servicetemplate_id\n";
			}
			$command_id         = $service_template{check_command};
			$servicetemplate_id = $service_template{parent_id};
		    }
		    if ( not defined $command_id ) {
			$self->{logger}
			  ->error("ERROR:  Cannot find the check command for service $service on host $elemental_results->{chosen_hostname}.");
			## This may be a setup problem with a newly established service, rathar than an issue with not
			## being able to retrieve data from the database.  So we classify it as a logical problem with
			## the discovery results.
			$is_data_problem = 1;
			die "cannot find the check command for service $service on host $elemental_results->{chosen_hostname}\n";
		    }

		    my %command = StorProc->fetch_one( 'commands', 'command_id', $command_id );
		    if ( not %command ) {
			$self->{logger}
			  ->error("ERROR:  Cannot fetch the check command for service $service on host $elemental_results->{chosen_hostname}.");
			$is_data_problem = 0;
			die "cannot fetch the check command for service $service on host $elemental_results->{chosen_hostname}\n";
		    }

		    push @database_changes, "* updating command line for service $service on host $elemental_results->{chosen_hostname}\n";
		    my $result = StorProc->update_obj(
			'services',
			'service_id',
			$service_id,
			{
			        command_line => $command{name}
			      . ( $sensor_results->{command_arguments} eq '' ? '' : '!' )
			      . $sensor_results->{command_arguments}
			}
		    );
		    if ( $result =~ /^Error/ ) {
			$self->{logger}->error( "ERROR:  Cannot update command_line for service $service"
			      . " on host $elemental_results->{chosen_hostname}:  $result" );
			$is_data_problem = 0;
			die "cannot update command_line for service $service on host $elemental_results->{chosen_hostname}\n";
		    }
		    ## FIX MAJOR:  also possibly deal with service overrides?
		}

		if ( defined $sensor_results->{externals_arguments} ) {
		    push @database_changes,
		      "* updating externals_arguments for service $service on host $elemental_results->{chosen_hostname}\n";
		    my $result =
		      StorProc->update_obj( 'services', 'service_id', $service_id,
			{ externals_arguments => $sensor_results->{externals_arguments}, inherit_ext_args => '00' } );
		    if ( $result =~ /^Error/ ) {
			$self->{logger}->error( "ERROR:  Cannot update externals_arguments for service $service"
			      . " on host $elemental_results->{chosen_hostname}:  $result" );
			$is_data_problem = 0;
			die "cannot update externals_arguments for service $service on host $elemental_results->{chosen_hostname}\n";
		    }
		    ## FIX MAJOR:  also possibly deal with service overrides?
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) make sure that host externals are added to the host if they do not exist there, if they
    #     are assigned to the host profile that the discovery results mention and that host profile
    #     was not earlier applied to the host because it was already applied

    unless ($failed) {
	eval {
	    my @host_profile_external_ids =
	      StorProc->fetch_unique( 'external_host_profile', 'external_id', 'hostprofile_id', $discovered_host_profile{hostprofile_id} );
	    my @host_external_ids = StorProc->fetch_unique( 'external_host', 'external_id', 'host_id', $host_details{host_id} );
	    my %existing_host_external_ids = ();
	    @existing_host_external_ids{@host_external_ids} = (undef) x @host_external_ids;
	    foreach my $host_external_id (@host_profile_external_ids) {
		if ( not exists $existing_host_external_ids{$host_external_id} ) {
		    my %external = StorProc->fetch_one( 'externals', 'external_id', $host_external_id );
		    if ( $external{type} ne 'host' ) {
			die "external '$external{name}' assigned to host profile $discovered_host_profile_name is not a host external\n";
		    }
		    push @database_changes, "* adding host external '$external{name}' to host $elemental_results->{chosen_hostname}\n";
		    my $result =
		      StorProc->insert_obj( 'external_host', [ $host_external_id, $host_details{host_id}, $external{display}, \'0+0' ] );
		    if ( $result =~ /^Error/ ) {
			$is_data_problem = 0;
			die "$result\n";
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) make sure that service externals are added to the host services if they do not exist
    #     there, for all services indirectly or directly referenced by the discovery results,
    #     whether or not those host services were already present before the discovery results
    #     were processed

    unless ($failed) {
	eval {
	    foreach my $service ( keys %all_service_instances ) {
		my @generic_service_external_ids =
		  StorProc->fetch_unique( 'external_service_names', 'external_id', 'servicename_id', $servicename_id_by_service{$service} );
		my @host_service_external_ids =
		  StorProc->fetch_list_where( 'external_service', 'external_id',
		    { host_id => $host_details{host_id}, service_id => $service_id_by_service{$service} } );
		my %existing_host_service_external_ids = ();
		@existing_host_service_external_ids{@host_service_external_ids} = (undef) x @host_service_external_ids;
		foreach my $service_external_id (@generic_service_external_ids) {
		    if ( not exists $existing_host_service_external_ids{$service_external_id} ) {
			my %external = StorProc->fetch_one( 'externals', 'external_id', $service_external_id );
			if ( $external{type} ne 'service' ) {
			    die "external '$external{name}' assigned to generic service $service is not a service external\n";
			}
			push @database_changes,
			  "* adding service external '$external{name}' to host $elemental_results->{chosen_hostname} service $service\n";
			my $result =
			  StorProc->insert_obj( 'external_service',
			    [ $service_external_id, $host_details{host_id}, $service_id_by_service{$service}, $external{display}, \'0+0' ] );
			if ( $result =~ /^Error/ ) {
			    $is_data_problem = 0;
			    die "$result\n";
			}
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) make sure the host is a member of some particular well-known hostgroup, so it shows up
    #     categorized both in Monarch and in Foundation under at least one hostgroup name

    unless ($failed) {
	eval {
	    if ( $change_policy eq 'from_scratch' ) {
		## FIX MAJOR:  delete all existing membership in hostgroups for this host
	    }

	    # Hostgroup assignment should always be done for a newly added host, so the Status Viewer will operate correctly.
	    # For an existing host, hostgroup assignment should normally only be done if there is not already some existing
	    # hostgroup containing that host, so as to preserve any local changes to the setup after the host got initially
	    # registered and not have the host pop back up where it is no longer wanted.
	    my $assign_hostgroups = 1;
	    if (%existing_host and !$assign_hostgroups_to_existing_hostgroup_hosts) {
		my @host_hostgroups = StorProc->get_host_hostgroups( $host_details{name} );
		$assign_hostgroups = 0 if @host_hostgroups;
	    }
	    if ($assign_hostgroups) {
		foreach my $hostgroup (@$hostgroups_to_assign) {
		    my %properties = StorProc->fetch_one( 'hostgroups', 'name', $hostgroup );
		    if ( not %properties ) {
			$is_data_problem = 1;
			die "hostgroup $hostgroup does not exist\n";
		    }

		    # We look before we leap, to avoid duplicate-key insertion errors.
		    my @hostgroup_ids = StorProc->fetch_unique( 'hostgroup_host', 'hostgroup_id', 'host_id', $host_details{host_id} );
		    local $_;
		    if ( not grep { $_ == $properties{hostgroup_id} } @hostgroup_ids ) {
			push @database_changes, "* adding host $host_details{name} to hostgroup $hostgroup\n";
			my $result = StorProc->insert_obj( 'hostgroup_host', [ $properties{hostgroup_id}, $host_details{host_id} ] );
			if ( $result =~ /^Error/ ) {
			    $is_data_problem = 0;
			    die "$result\n";
			}
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    # (*) make sure the host is a member of some particuler well-known Monarch Group, so the
    #     process of building externals will know where to put them

    # FIX MINOR:
    # There is an alternative way to get the assurance we need that the host is a member of a
    # Monarch Group.  And that is, to ensure that at least one of the hostgroups we have assigned
    # to the host is assigned to some Monarch group.  If that is the case, we can depend on that
    # indirect association of the host with a Monarch Group, and not have to take any action here.
    # Thus it is not necessarily required that we actually do any work in this step, as long as
    # the customer has made such a hostgroup-to-Monarch-Group assignment.  If the customer wants
    # to handle Monarch Group assignments in that way, we should support having an empty value
    # for the default_monarch_group in the config file.

    unless ($failed) {
	eval {
	    if ( $change_policy eq 'from_scratch' ) {
		## FIX MAJOR:  delete all existing membership in Monarch Groups for this host
	    }

	    ## Monarch Group assignment should be done both for a newly added host and for an existing host.
	    my @existing_monarchgroup_ids = StorProc->fetch_unique( 'monarch_group_host', 'group_id', 'host_id', $host_details{host_id} );

	    # Interpretation of the $assign_monarch_groups_to_existing_group_hosts flag must also take into account
	    # indirect assignment of the host to Monarch Groups via hostgroups.  However, we shouldn't need to worry
	    # about child Monarch Groups as well.
	    #
	    # The way to handle this is to add to @existing_monarchgroup_ids any Monarch Group group_id values for
	    # which the host is a member of some hostgroup assigned to the Monarch Group.  So we need to first
	    # figure out what hostgroups the host is a member of, then figure out what Monarch Groups have at least
	    # one of those hostgroups assigned to it, append the group_id values of those Monarch Groups to the
	    # @existing_monarchgroup_ids array, and finally de-dup that array.
	    #
	    # FIX MAJOR:  This processing somewhat belies the notion that the list of Monarch Groups to assign would
	    # have been completely determined by the caller, based possibly on logic in some custom customer-provided
	    # package.  Think that through.
	    #
	    my @hostgroup_ids = StorProc->fetch_unique( 'hostgroup_host', 'hostgroup_id', 'host_id', $host_details{host_id} );
	    foreach my $hostgroup_id (@hostgroup_ids) {
		my @hostgroup_monarchgroup_ids = StorProc->fetch_unique( 'monarch_group_hostgroup', 'group_id', 'hostgroup_id', $hostgroup_id );
		push @existing_monarchgroup_ids, @hostgroup_monarchgroup_ids;
	    }
	    my %unique = ();
	    @unique{@existing_monarchgroup_ids} = (undef) x @existing_monarchgroup_ids;
	    @existing_monarchgroup_ids = keys %unique;
	    my $force_monarch_group_assignment = $assign_monarch_groups_to_existing_group_hosts || @existing_monarchgroup_ids == 0;

	    # We must de-dup the list of Monarch Groups in order to avoid possible duplicate-key errors during insertion.
	    # Otherwise, the protections within the loop won't be good enough by themselves.
	    my %unique_monarch_groups_to_assign = ();
	    @unique_monarch_groups_to_assign{@$monarch_groups_to_assign} = (undef) x @$monarch_groups_to_assign;

	    foreach my $monarch_group ( keys %unique_monarch_groups_to_assign ) {
		my %properties = StorProc->fetch_one( 'monarch_groups', 'name', $monarch_group );
		if ( not %properties ) {
		    $is_data_problem = 1;
		    die "Monarch Group $monarch_group does not exist\n";
		}

		# We look before we leap, to avoid duplicate-key insertion errors.
		#
		# Note that association of hosts with Monarch Groups can be indirect in several ways:  via a
		# hostgroup assigned to the Monarch Group, or via some child Monarch Group.  For purposes of
		# the configuration we do here, we're going to ignore all that and just make direct assignments
		# of hosts to specific Monarch Groups, ignoring both hostgroups and child Monarch Groups.
		# That may or may not be ideal, so this code may be subject to future evolution.
		#
		local $_;
		if ( not grep { $_ == $properties{group_id} } @existing_monarchgroup_ids and $force_monarch_group_assignment ) {
		    push @database_changes, "* adding host $host_details{name} to Monarch Group $monarch_group\n";
		    my $result = StorProc->insert_obj( 'monarch_group_host', [ $properties{group_id}, $host_details{host_id} ] );
		    if ( $result =~ /^Error/ ) {
			$is_data_problem = 0;
			die "$result\n";
		    }
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    ## There are various ways that we can check the details of the overall setup beyond what we just
    ## put in place, in anticipation of more-intensive checking by Nagios during a pre-flight test.
    ## These tests can be extended over time based on the most common mistakes we see being made, to
    ## provide guidance as to how to best and most quickly address them.
    ##
    ## One thing we can do here is to validate that every host service on the host has certain
    ## fields defined as they need to be for a Nagios Commit to succeed.  For instance, the service
    ## must have a "Check command" defined, whether directly on that host service, inherited from
    ## the service template which is being referenced by that host service, or ultimately inherited
    ## from some ancestor service template of the directly-assigned service template.
    ##
    unless ($failed) {
	eval {
	    my %services = StorProc->get_host_services_detail( $host_details{host_id} );
	    foreach my $service_id ( keys %services ) {
		if ( not defined $services{$service_id}{check_command} ) {
		    if ( defined $services{$service_id}{servicetemplate_id} ) {
			my %properties = StorProc->get_template_properties( $services{$service_id}{servicetemplate_id} );
			next if defined $properties{check_command};
		    }
		    my $service_name = StorProc->get_service_name($service_id);
		    $is_data_problem = 1;
		    die "host '$elemental_results->{chosen_hostname}' service '$service_name' has no check command defined;"
		      . "\n  hint: this is most commonly set via a service template, not via a check_command directive in the discovery instructions\n";
		}
	    }
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    ## FIX MAJOR:  Is there anything else to do, perhaps under different change policies?
    ##
    unless ($failed) {
	eval {
	    ## FIX MAJOR:  fill this in
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  Database changes failed ($@).");
	    $failed         = 1;
	    $failure_reason = "encountered a database error ($@)";
	}
    }

    ## This test could have been done up front, but that would not allow us to test the code for other change policies
    ## as we gradually implement it.
    ##
    unless ($failed) {
	if ( $change_policy ne 'non_destructive' ) {
	    $is_data_problem = 0;
	    $failed          = 1;
	    $failure_reason  = "only the 'non_destructive' change policy is currently supported";
	}
    }

=pod

the commentary and data structures here are still a work in progress; don't trust any of it yet

Rules:
(*) a host profile mentions a host template; a host template does not mention a host profile
(*) a host does not necessarily have to have a host profile applied
(*) the reason we are insisting on having a host profile applied during auto-setup is because
    that is an intermediary that will force the application of some specific host template,
    perhaps along with some non-inherited customization, along with a bunch of other possible
    configuration data (parents, hostgroups, escalation trees, host externals, service profiles)
(*) a host can have parents          applied independently of the host profile
(*) a host can have hostgroups       applied independently of the host profile
(*) a host can have escalation trees applied independently of the host profile
(*) a host can have host externals   applied independently of the host profile
(*) a host can have service profiles applied independently of the host profile
(*) a host can have services         applied independently of the host profile

make data structures of the following forms, so we can walk them easily as we proceed and
take the necessary actions at each step:

# this data structure includes both the existing host profile applied to the host
# (if the host already exists), and the one that should be applied (if different)
existing_host_profiles: {
    _host_profile_name_: [ _service_profile_name_ ... ]
    ...
}

# this data structure lists service profiles which are indirectly applied to the host
# (if the host already exists)
existing_indirect_service_profiles: {
    _service_profile_name_: [ _service_name_ ... ]
    ...
}

# this data structure includes both existing service profiles directly applied to the host
# (if the host already exists), and the ones that should be applied (if different)
existing_direct_service_profiles: {
    _service_profile_name_: [ _service_name_ ... ]
    ...
}

# FIX MAJOR:  for each service, mark which service profiles applied to this host reference it
# FIX MAJOR:  for each service, mark whether the service is considered to be applied via some
#             set of service profiles or via independent assignment to the host
host_status: {
    old_objects: {
	host:  _hostname_
	host_profile:  _host_profile_name_
	service_profiles: {
	    _service_profile_name_: [ _service_name_ ... ]
	    ...
	}
	services: {
	    _service_name_: {
		check_command:  _check_cmmand_string_
		command_arguments:  _command_arguments_string_
		externals_arguments:  _externals_arguments_string_
		instances: [
		    _instance_suffix_: {
			instance_cmd_args:  _instance_cmd_args_string_
			instance_ext_args:  _instance_ext_args_string_
		    }
		    ...
		]
	    }
	    ...
	}
    }
    new_objects: {
	host:  _hostname_
	host_profile:  _host_profile_name_
	service_profiles: [
	    _service_profile_name_: [ _service_name_ ... ]
	    ...
	]
	services: {
	    _service_name_: {
		check_command:  _check_cmmand_string_
		command_arguments:  _command_arguments_string_
		externals_arguments:  _externals_arguments_string_
		instances: [
		    _instance_suffix_: {
			instance_cmd_args:  _instance_cmd_args_string_
			instance_ext_args:  _instance_ext_args_string_
		    }
		    ...
		]
	    }
	    ...
	}
    }
}

=cut

    # We sometimes disable commits here during development, so as to keep the database in good condition for repeated testing.
    if (0) {
	## $persistence = 'rollback';
	## $persistence = 'commit';
    }

    if ( !$failed and $persistence eq 'commit' ) {
	$self->{logger}->notice("NOTICE:  Changes for host \"$elemental_results->{chosen_hostname}\" are being committed.");
	my $problem;
	push @database_changes, "Committing database changes.\n" if @database_changes;
	( $outcome, $problem ) = $self->commit_config_changes();
	if ( not $outcome ) {
	    $failed          = 1;
	    $failure_reason  = "encountered a database error ($problem)";
	    $is_data_problem = 0;
	    push @database_changes, "Encountered a database error ($problem).\n" if @database_changes;
	}
    }
    else {
	push @database_changes, "? $failure_reason\n" if $failure_reason;
	## The caller should set $persistence to 'revert' or 'rescind', not 'rollback', for manually initiated
	## results-analysis runs for which we never intend to create persistent changes in the database.  In
	## that case, it's up to the caller to report the intentional reversion, not this code.
	if ( $persistence eq 'commit' || $persistence eq 'rollback' ) {
	    $self->{logger}->notice( "NOTICE:  Changes for host \"$elemental_results->{chosen_hostname}\" are being rolled back "
		  . ( $failed ? 'due to previous failures.' : 'because this was just a test run.' ) );
	}
	## Rolling back won't undo the effects of bumping up sequence numbers during the transaction, so we will
	## likely get some visible effects in that regard even if changes in the large do not take effect.
	my $problem;
	push @database_changes,
	  "Rolling back database changes, " . ( $failed ? 'due to previous failures' : 'because this was just a test run' ) . ".\n"
	  if @database_changes;
	( $outcome, $problem ) = $self->roll_back_config_changes();
	if ( not $outcome ) {
	    ## We give a failure reason only if there was no previous failure that forced this rollback.
	    $failed = 1;
	    if ( not $failure_reason ) {
		$failure_reason  = "encountered a database error ($problem)";
		$is_data_problem = 0;
		push @database_changes, "Encountered a database error ($problem).\n" if @database_changes;
	    }
	}
    }

    # FIX MAJOR:  What to do with this outcome?  Also, perhaps don't disconnect now if we're
    # going to build externals, but somehow otherwise manage the database connection.
    # $outcome = $self->database_disconnect();

    return !$failed, $failure_reason, $is_data_problem, do {
	local $Text::Wrap::columns = 125;
	join( '', map { wrap( '', '  ', $_ ) } @database_changes );
    };
}

# Build externals for just one host.  This doesn't affect Monarch itself, so in that sense
# it seems like it doesn't belong in the GDMA::Configuration package.  But it's part of the
# overall system configuration, including data made available outside of the database.  One
# other reason to include this capability here is a technical one:  it should use the same
# database handle as we used to apply consolidated discovery results to the database.
#
sub build_externals_for_host {
    my $self           = shift;
    my $hostname       = shift;
    my $failed         = 0;
    my $failure_reason = '';
    my $outcome        = 0;

    eval {
	## By setting $force false, we avoid regenerating an externals file whose content has not significantly
	## changed, thereby not changing its last-modified timestamp and therefore not causing the GDMA client
	## that fetches the file to decide that its configuration has probably changed.
	my $force = 0;
	my ( $results, $errors ) =
	  Externals->build_some_externals( $self->{'user_acct'}, $self->{'session_id'}, $self->{'via_web_ui'}, [$hostname], $force );
	my @errors  = @{$errors};
	my @results = @{$results};
	if (@errors) {
	    die join( ' / ', @errors ) . "\n";
	}
    };
    if ($@) {
	chomp $@;
	$failed         = 1;
	$failure_reason = $@;
    }

    return !$failed, $failure_reason;
}

1;
