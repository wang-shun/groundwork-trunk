#!/usr/local/groundwork/perl/bin/perl --

use lib qq(/usr/local/groundwork/monarch/lib);
use strict;
use Getopt::Long;
use MonarchAPI;
use File::Copy;
my @errors = ();

my $PROGNAME = 'auto_import_xml.pl';
my ($opt_c, $opt_f, $opt_h, $opt_v, $opt_d);

Getopt::Long::Configure('bundling');
GetOptions(
	"v"   => \$opt_v, "verbose" => \$opt_v,
	"c"   => \$opt_c, "commit" => \$opt_c,
	"d"   => \$opt_d, "debug" => \$opt_d,
	"h"   => \$opt_h, "help" => \$opt_h,
	"f=s" => \$opt_f, "file" => \$opt_f);

if (!$opt_f || $opt_h) {
	print_usage ();
} 

unless (-f $opt_f) {
	print "Error: File $opt_f not found.\n";
	exit;
}

sub print_usage() {
	print "Usage:\n";
	print "  $PROGNAME [-d debug] [-v verbose] [-c commit] -f <file>\n";
	print "  $PROGNAME [-h | --help]\n";
	exit;
}

######################################
# Parse file
######################################
#
my %input = API->parse_input_xml($opt_f);

######################################
# Connect to db
######################################
#
my $connect = API->dbconnect();
print "\n$connect" if $opt_v;

######################################
# Get existing objects and setup info
######################################
#
my %objects = API->get_objects();
my %config_settings = API->config_settings();

################################
# Add supporting objects
################################
#
# service groups
#

foreach my $name (keys %{$input{'service_groups'}}) { 
	print "\nProcessing service group: $name alias: $input{'service_groups'}{$name}{'alias'} escalation: $input{'service_groups'}{$name}{'service_escalation'}" if $opt_v;
	# check to see if it already exists while ignoring case 
	my $got_group = 0;
	foreach my $group (keys %{$objects{'service_groups'}}) {
		if ($name =~ /$group/i) { $got_group = 1 }
	}
	unless ($got_group) {
		my $id = API->add_service_group(\%{$input{'service_groups'}{$name}});
		if ($id =~ /error/i) {
			print "\n $id" if $opt_v;
		} else {
			$objects{'service_groups'}{$name} = $id;
			print "\n Added: service group $name" if $opt_v;
		}
	} else {
		print "\n Exists: service group $name" if $opt_v;
	}
}

#
# contact groups
#

foreach my $name (keys %{$input{'contact_groups'}}) { 
	print "\nProcessing contact group $name alias $input{'contact_groups'}{$name}{'alias'}" if $opt_v;
	# check to see if it already exists while ignoring case 
	my $got_group = 0;
	foreach my $group (keys %{$objects{'contact_groups'}}) {
		if ($name =~ /$group/i) { $got_group = 1 }
	}
	unless ($got_group) {
		my $id = API->add_contact_group(\%{$input{'contact_groups'}{$name}});
		if ($id =~ /error/i) {
			print "\n $id" if $opt_v;
		} else {
			$objects{'contact_groups'}{$name} = $id;
			print "\n Added: contact group $name" if $opt_v;
		}
	} else {
		print "\n Exists: contact group $name" if $opt_v;
	}
}

#
# host groups
#

foreach my $name (keys %{$input{'host_groups'}}) { 
	print "\nProcessing host group: $name alias: $input{'host_groups'}{$name}{'alias'} profile: $input{'host_groups'}{$name}{'host_profile'} escalations: $input{'host_groups'}{$name}{'host_escalation'} $input{'host_groups'}{$name}{'service_escalation'}" if $opt_v;
	# check to see if it already exists while ignoring case 
	my $got_group = 0;
	foreach my $group (keys %{$objects{'host_groups'}}) {
		if ($name =~ /$group/i) { $got_group = 1 }
	}
	unless ($got_group) {
		foreach my $group (keys %{$input{'host_groups'}{$name}{'groups'}}) {
			if ($objects{'groups'}{$group}) {
				$input{'host_groups'}{$name}{'groups'}{$group} = $objects{'groups'}{$group};
			} else {
				print "\n Nonfatal error: Group $group does not exist for host group $name." if $opt_v;
				delete $input{'host_groups'}{$name}{'groups'}{$group};
			}
		}
		my @results = API->add_host_group(\%{$input{'host_groups'}{$name}});
		if ($results[0] =~ /error/i && $opt_v) {
			foreach my $res (@results) {
				print "\n $res";
			}
		} else {
			$objects{'host_groups'}{$name} = $results[0];
			print "\n Added: host group $name" if $opt_v;
		}
	} else {
		print "\n Exists: host group $name" if $opt_v;
	}
}

##############################################
# Determine what is new and attempt to import 
##############################################
#
foreach my $name (keys %{$input{'hosts'}}) {
	print "\nProcessing host: $name alias: $input{'hosts'}{$name}{'alias'} address: $input{'hosts'}{$name}{'address'} profile: $input{'hosts'}{$name}{'host_profile'} escalations: $input{'hosts'}{$name}{'host_escalation'} $input{'hosts'}{$name}{'service_escalation'}" if $opt_v;
	if ($input{'hosts'}{$name}{'alias'} && $input{'hosts'}{$name}{'address'}) {
		# check to see if profile exists and if not import from source file if it exists
		my $got_obj = 0;
		foreach my $obj (keys %{$objects{'host_profiles'}}) {
			if ($input{'hosts'}{$name}{'host_profile'} =~ /^$obj$/i) { $got_obj = 1 }
		}
		unless ($got_obj) {
			my $file = "host-profile-$input{'hosts'}{$name}{'host_profile'}.xml";
			if (-e "$config_settings{'groundwork_home'}/profiles/$file") {
				my @result = API->import_profile("$config_settings{'groundwork_home'}/profiles",$file);
				if ($opt_v) {
					print "\n Found file $file";
					print "\n-----------------------------------------------------";
					foreach my $msg (@result) {
						print "\n $msg";
					}
				}
			} else {
				print "\n Nonfatal error: host profile $input{'hosts'}{$name}{'host_profile'} does not exist and there is no source file to import ($config_settings{'groundwork_home'}/profiles/$file)" if $opt_v;
			}
		}
		# Match case insensitive objects - MySQL is case insensitive
		# Escalations
		foreach my $obj (keys %{$objects{'escalations'}}) {
			if ($input{'hosts'}{$name}{'host_escalation'} =~ /^$obj$/i) { $input{'hosts'}{$name}{'host_escalation'} = $obj }
		}
		foreach my $obj (keys %{$objects{'escalations'}}) {
			if ($input{'hosts'}{$name}{'service_escalation'} =~ /^$obj$/i) { $input{'hosts'}{$name}{'service_escalation'} = $obj }
		}
		# group (configuration)
		foreach my $obj (keys %{$objects{'groups'}}) {
			if ($input{'hosts'}{$name}{'group'} =~ /^$obj$/i) { $input{'hosts'}{$name}{'group'} = $obj }
		}
		# Host groups
		foreach my $group (keys %{$input{'hosts'}{$name}{'host_groups'}}) {
			foreach my $obj (keys %{$objects{'host_groups'}}) {
				if ($group =~ /^$obj$/i) { $input{'hosts'}{$name}{'host_group'}{$group} = $obj }
			}
		}
		# Contact groups
		foreach my $group (keys %{$input{'hosts'}{$name}{'contact_groups'}}) {
			foreach my $obj (keys %{$objects{'contact_groups'}}) {
				if ($group =~ /^$obj$/i) { $input{'hosts'}{$name}{'contact_group'}{$group} = $obj }
			}
		}
		# Host externals
		$got_obj = 0;
		foreach my $ext (keys %{$input{'hosts'}{$name}{'host_externals'}}) {
			foreach my $obj (keys %{$objects{'externals'}}) {
				if ($ext =~ /^$obj$/i) { 
					$input{'hosts'}{$name}{'host_externals'}{$ext}{'name'} = $obj;
					$got_obj = 1;
				}
			}
			unless ($got_obj) {
				print "\n Adding host external $ext" if $opt_v;
				my $result = API->add_external($ext,'host',$input{'hosts'}{$name}{'host_externals'}{$ext}{'value'});
				print "\n $result" if $opt_v;
				$objects{'externals'}{$ext} = 1;
			}
		}
		# Service profiles - check to see if service profiles exist and if not import them from source files if they exists
		foreach my $sp (keys %{$input{'hosts'}{$name}{'service_profiles'}}) {
			my $got_obj = 0;
			foreach my $obj (keys %{$objects{'service_profiles'}}) {
				if ($sp =~ /^$obj$/i) { 
					$got_obj = 1;
					$input{'hosts'}{$name}{'service_profile'}{$sp} = $obj;
				}
			}
			unless ($got_obj) {
				my $file = "service-profile-$sp.xml";
				if (-e "$config_settings{'groundwork_home'}/profiles/$file") {
					my @result = API->import_profile("$config_settings{'groundwork_home'}/profiles",$file);
					if ($opt_v) {
						print "\n Found file $file";
						print "\n -----------------------------------------------------";
						foreach my $msg (@result) {
							print "\n $msg";
						}
					}
				} else {
					print "\n Nonfatal error: Service profile $sp does not exist and there is no source file to import ($config_settings{'groundwork_home'}/profiles/$file)" if $opt_v;
					delete $input{'hosts'}{$name}{'service_profile'}{$sp};
				}
			}
		}
		# Services - check to see if services exist and if not import them from source files if they exists
		foreach my $service (keys %{$input{'hosts'}{$name}{'services'}}) {
			my $got_obj = 0;
			foreach my $obj (keys %{$objects{'service_names'}}) {
				$obj =~ s/\*//g;
				if ($service =~ /^$obj$/i) { 
					$got_obj = 1;
					$input{'hosts'}{$name}{'services'}{$service}{'name'} = $obj;
				}
			}
			unless ($got_obj) {
				my $file = "service-$service.xml";
				if (-e "$config_settings{'groundwork_home'}/profiles/$file") {
					my @result = API->import_profile("$config_settings{'groundwork_home'}/profiles",$file);
					if ($opt_v) {
						print "\n Found file $file";
						print "\n-----------------------------------------------------";
						foreach my $msg (@result) {
							print "\n $msg";
						}
					}
				} else {
					print "\n Nonfatal error: Service $service does not exist and there is no source file to import ($config_settings{'groundwork_home'}/profiles/$file)" if $opt_v;
					delete $input{'hosts'}{$name}{'services'}{$service};
					next;
				}
			}
			# Service escalation -service
			foreach my $obj (keys %{$objects{'escalations'}}) {
				if ($input{'hosts'}{$name}{'services'}{$service}{'service_escalation'} =~ /^$obj$/i) { $input{'hosts'}{$name}{'services'}{$service}{'service_escalation'} = $obj }
			}

			# Service externals
			foreach my $ext (keys %{$input{'hosts'}{$name}{'services'}{$service}{'service_externals'}}) {
				$got_obj = 0;
				foreach my $obj (keys %{$objects{'externals'}}) {
					if ($ext =~ /^$obj$/i) { 
						$input{'hosts'}{$name}{'services'}{$service}{'service_external'}{$ext}{'name'} = $obj;
						$got_obj = 1;
					} 
				}
				unless ($got_obj) {
					print "\n Adding service external $ext" if $opt_v;
					my $result = API->add_external($ext,'service',$input{'hosts'}{$name}{'services'}{$service}{'service_external'}{$ext}{'value'});
					print "\n $result" if $opt_v;
					$objects{'externals'}{$ext} = 1;
				}
			}

			# Contact groups
			foreach my $group (keys %{$input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}}) {
				foreach my $obj (keys %{$objects{'contact_groups'}}) {
					if ($group =~ /^$obj$/i) { $input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}{$group} = $obj }
				}
			}

			# Service groups
			foreach my $group (keys %{$input{'hosts'}{$name}{'services'}{$service}{'service_groups'}}) {
				foreach my $obj (keys %{$objects{'service_groups'}}) {
					if ($group =~ /^$obj$/i) { $input{'hosts'}{$name}{'services'}{$service}{'service_groups'}{$group} = $obj }
				}
			}
		}
	} else {
		print "\n Error - Missing required data for host $name: alias and/or address" if $opt_v;
		delete $input{'hosts'}{$name};
	}		
		
}

################################
# Process hosts
################################
#
# Reload objects to pick up the new ones (import_profiles)
#
%objects = API->get_objects();

#
# Set db id's to values
#
foreach my $name (keys %{$input{'hosts'}}) {
	my %host = API->get_host($name);
	$input{'hosts'}{$name}{'hostprofile_id'} = $objects{'host_profiles'}{$input{'hosts'}{$name}{'host_profile'}};
	$input{'hosts'}{$name}{'group_id'} = $objects{'groups'}{$input{'hosts'}{$name}{'group'}};
	$input{'hosts'}{$name}{'host_escalation_id'} = $objects{'escalations'}{$input{'hosts'}{$name}{'host_escalation'}};
	$input{'hosts'}{$name}{'service_escalation_id'} = $objects{'escalations'}{$input{'hosts'}{$name}{'service_escalation'}};

	print "\n-$name host profile $input{'hosts'}{$name}{'hostprofile_id'}" if $opt_d;		
	print "\n-$name group  $input{'hosts'}{$name}{'group_id'}" if $opt_d;		
	print "\n-$name host esc $input{'hosts'}{$name}{'host_escalation_id'}" if $opt_d;		
	print "\n-$name service esc $input{'hosts'}{$name}{'service_escalation_id'}" if $opt_d;		



	foreach my $group (keys %{$input{'hosts'}{$name}{'host_groups'}}) {
		$input{'hosts'}{$name}{'host_groups'}{$group} = $objects{'host_groups'}{$input{'hosts'}{$name}{'host_groups'}{$group}};
		print "\n-$name $group--$input{'hosts'}{$name}{'host_groups'}{$group}" if $opt_d;		
	}
	
	foreach my $sp (keys %{$input{'hosts'}{$name}{'service_profiles'}}) {
		$input{'hosts'}{$name}{'service_profiles'}{$sp} = $objects{'service_profiles'}{$input{'hosts'}{$name}{'service_profiles'}{$sp}};
		print "\n-$name $sp--$input{'hosts'}{$name}{'service_profiles'}{$sp}" if $opt_d;		

	}

	foreach my $group (keys %{$input{'hosts'}{$name}{'contact_groups'}}) {
		$input{'hosts'}{$name}{'contact_groups'}{$group} = $objects{'contact_groups'}{$input{'hosts'}{$name}{'contact_groups'}{$group}};
		print "\n-$name $group--$input{'hosts'}{$name}{'contact_groups'}{$group}" if $opt_d;		
	}

	foreach my $ext (keys %{$input{'hosts'}{$name}{'host_externals'}}) {
		$input{'hosts'}{$name}{'host_externals'}{$ext}{'external_id'} = $objects{'externals'}{$input{'hosts'}{$name}{'host_externals'}{$ext}{'name'}};
		print "\n-$name ext $ext--$input{'hosts'}{$name}{'host_externals'}{$ext}{'external_id'}" if $opt_d;		
	}
	foreach my $service (keys %{$input{'hosts'}{$name}{'services'}}) {
		$input{'hosts'}{$name}{'services'}{$service}{'servicename_id'} = $objects{'service_names'}{$input{'hosts'}{$name}{'services'}{$service}{'name'}};
		print "\n-$name $service $service-$input{'hosts'}{$name}{'services'}{$service}{'servicename_id'}" if $opt_d;		
		$input{'hosts'}{$name}{'services'}{$service}{'service_escalation_id'} = $objects{'escalations'}{$input{'hosts'}{$name}{'services'}{$service}{'service_escalation'}};
		print "\n-$name $service se-$input{'hosts'}{$name}{'services'}{$service}{'service_escalation_id'}" if $opt_d;		
		if ($input{'hosts'}{$name}{'services'}{$service}{'check_command'}) {
			if ($input{'hosts'}{$name}{'services'}{$service}{'check_command'} =~ /\!/) {
				my @command = split(/\!/, $input{'hosts'}{$name}{'services'}{$service}{'check_command'});
				$input{'hosts'}{$name}{'services'}{$service}{'check_command_id'} = $objects{'commands'}{$command[0]};
			} else {
				$input{'hosts'}{$name}{'services'}{$service}{'check_command_id'} = $objects{'commands'}{$input{'hosts'}{$name}{'services'}{$service}{'check_command'}};
			}
		}
		print "\n-$name $service check_command-$input{'hosts'}{$name}{'services'}{$service}{'check_command_id'} $input{'hosts'}{$name}{'services'}{$service}{'check_command'}" if $opt_d;		
		foreach my $group (keys %{$input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}}) {
			$input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}{$group} = $objects{'contact_groups'}{$input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}{$group}};
			print "\n-$name $service $group--$input{'hosts'}{$name}{'services'}{$service}{'contact_groups'}{$group}" if $opt_d;		
		}
		foreach my $group (keys %{$input{'hosts'}{$name}{'services'}{$service}{'service_groups'}}) {
			$input{'hosts'}{$name}{'services'}{$service}{'service_groups'}{$group} = $objects{'service_groups'}{$input{'hosts'}{$name}{'services'}{$service}{'service_groups'}{$group}};
			print "\n-$name $service $group--$input{'hosts'}{$name}{'services'}{$service}{'service_groups'}{$group}" if $opt_d;		
		}
		foreach my $ext (keys %{$input{'hosts'}{$name}{'services'}{$service}{'service_externals'}}) {
			$input{'hosts'}{$name}{'services'}{$service}{'service_externals'}{$ext}{'external_id'} = $objects{'externals'}{$input{'hosts'}{$name}{'services'}{$service}{'service_externals'}{$ext}{'name'}};
			print "\n-$name $service $ext--$input{'hosts'}{$name}{'services'}{$service}{'service_externals'}{$ext}{'external_id'}" if $opt_d;	
		}
	}
#
# Add/update/delete hosts
#
	if ($host{'name'} && $input{'hosts'}{$name}{'delete'}) {
		print "\n Delete: host $name flagged for deletion" if $opt_v;
		my $result = API->delete_host($name);
		delete $input{'hosts'}{$name};
		print "\n $result" if $opt_v;
	} elsif ( $input{'hosts'}{$name}{'delete'}) {
		delete $input{'hosts'}{$name};
	} elsif ($host{'name'} && ($input{'hosts'}{$name}{'overwrite'} || $input{'hosts'}{$name}{'service_overwrites'})) {
		$input{'hosts'}{$name}{'exists'} = 1;
		$input{'hosts'}{$name}{'host_id'} = $host{'host_id'};
		print "\n Exists: host $name and overwrite one or more properties = yes" if $opt_v;
		my @results = API->import_host(\%{$input{'hosts'}{$name}});
		if ($opt_v) {
			foreach my $msg (@results) { print "\n-$msg" }
		}
	} elsif ($host{'name'}) {
		print "\n Exists: host $name and overwrite = no" if $opt_v;
		delete $input{'hosts'}{$name};
	} else {
		$input{'hosts'}{$name}{'new'} = 1;
		print "\n Adding host $name" if $opt_v;
		my @results = API->import_host(\%{$input{'hosts'}{$name}});
		if ($opt_v) {
			foreach my $msg (@results) { print "\n-$msg" }
		}
	}
}

#
# Set parent child
#

my @results = API->set_host_parent(\%{$input{'hosts'}});
if ($opt_v) {
	foreach my $msg (@results) { print "\n-$msg" }
}

################################
# Commit option
################################
#
# This is sample code 
#

if ($opt_c) {
	my %config_settings = API->config_settings();
	my %file_ref = ();
	$file_ref{'user_acct'} = 'automated'; # user name in file headers
	my %groups = API->get_groups();
	foreach my $group (keys %groups) {
	# Preflight
		$file_ref{'commit_step'} = 'preflight';
		$file_ref{'location'} = '/usr/local/groundwork/monarch/workspace'; # where to put the files
		$file_ref{'nagios_etc'} = '/usr/local/groundwork/monarch/workspace';
		$file_ref{'group'} = $group;
		my ($files, $errors) = API->build_files(\%file_ref,\%config_settings);
		my $preflight_check = API->pre_flight_check(\%config_settings);
		if ($preflight_check) {
			print "\n preflight rc=$preflight_check";
			$file_ref{'commit_step'} = 'commit';
			$file_ref{'location'} = $groups{'location'}; # where to put the files
			$file_ref{'nagios_etc'} = '/usr/local/groundwork/nagios/etc';
			my ($files, $errors) = API->build_files(\%file_ref,\%config_settings);
### Note here the files are built and placed in a folder from where they can be copied to the child server
		
		} else {
			print "\n Preflight check failed! Exiting...\n\n" if $opt_v;
		}	
	}

### This will preflight and commit the parent
	delete $file_ref{'group'};
	$file_ref{'commit_step'} = 'preflight';
	$file_ref{'location'} = '/usr/local/groundwork/monarch/workspace';
	$file_ref{'nagios_etc'} = '/usr/local/groundwork/monarch/workspace';
	my ($files, $errors) = API->build_files(\%file_ref,\%config_settings);
	my $preflight_check = API->pre_flight_check(\%config_settings);
	if ($preflight_check) {
		print "\n preflight rc=$preflight_check";
		$file_ref{'commit_step'} = 'commit';
		$file_ref{'location'} = '/usr/local/groundwork/nagios/etc';
		$file_ref{'nagios_etc'} = '/usr/local/groundwork/nagios/etc';
		my ($files, $errors) = API->build_files(\%file_ref,\%config_settings);
		my @commit_results = API->commit(\%config_settings);
		foreach my $msg (@commit_results) {
			print "\n $msg";
		}
	} else {
		print "\n Preflight check failed! Exiting...\n\n";
	}
}

my $disconnect = API->dbdisconnect();






