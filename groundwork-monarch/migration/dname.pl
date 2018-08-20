#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2008-2012 GroundWork Open Source, Inc. (GroundWork)
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

# dname.pl -- This is a utility script to clean up the Foundation database
# for device display names and identification fields that were inconsistently
# fed into the database.  This can cause some issues in the display for the
# event console, especially when upgrading an older database.  In addition
# to performing other cleanup work, the script deletes devices that have no
# references from hosts (including VEMA hosts), i.e., it deletes orphan-device
# entries.
#
# Use in consultation with GroundWork Support!

# TO DO:
# (*) Fix the exception handling in this script, to properly check for and
#     deal with database-access errors.  We have, for instance, a number of
#     routines here that return no result, while the calling code is capturing
#     the returned value and doing nothing with it.

use lib qq(/usr/local/groundwork/core/monarch/lib);
use strict;
use warnings;
use Carp;
use Data::Dumper;
use DBI;
use MonarchStorProc;
use CollageQuery;

my $monarch_home = '/usr/local/groundwork/core/monarch';
my $debug        = 0;
my $foundation   = undef;

sub getApplicationTypes {
    my $self     = shift;
    my $apptypes = undef;
    my $sql      = "select
	ApplicationTypeID	as \"ApplicationTypeID\",
	Name			as \"Name\"
	from ApplicationType";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();

    while ( my @values = $sth->fetchrow_array() ) {
	$apptypes->{ $values[0] } = $values[1];
    }
    $sth->finish();

    # This return value might be undefined; the caller always needs to test before dereferencing.
    return $apptypes;
}

sub foundation_query_devices {
    my $self       = shift;
    my $device_ref = undef;

    # get hosts known to foundation
    my $use_alternate_code = 1;
    if ($use_alternate_code) {
	## For efficiency, we use a bulk retrieval of device info here, not an individual retrieval for every host.
	$device_ref = $self->getDevicesForHosts();
    }
    else {
	## Old code construction.  Left here for comparison and possible emergency use only.
	my $f_hosts = $self->getHostsByType(undef);
	if ( ref($f_hosts) eq 'HASH' ) {
	    foreach my $host ( keys %{$f_hosts} ) {
		my %device_hash = $self->getDeviceForHost($host);
		$device_ref->{$host} = {%device_hash};
	    }
	}
    }

    # This return value might be undefined; the caller always needs to test before dereferencing.
    return $device_ref;
}

# return Identification of devices that match (there can only be at most one such device,
# since Device.Identification is a non-null field with a uniqueness constraint).
sub getDeviceMatch {
    my $self         = shift;
    my $ident        = shift;
    my $quoted_ident = $self->{dbh}->quote($ident);
    my $sql          = "select
	DeviceID	as \"DeviceID\",
	DisplayName	as \"DisplayName\",
	Identification	as \"Identification\",
	DESCRIPTION	as \"Description\"
	from Device where Identification = $quoted_ident;";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    my $device_ref = $sth->fetchrow_hashref();
    $sth->finish();
    print "Matched device identification: $ident\n" if $debug and $device_ref;

    # This return value might be undefined; the caller always needs to test before dereferencing.
    return $device_ref;
}

# return device ID for host
sub getDeviceID {
    my $self     = shift;
    my $host     = shift;
    my $sql      = "select DeviceID as \"DeviceID\" from Host where HostName = '$host';";
    my $deviceid = $self->{dbh}->selectrow_array($sql);
    return $deviceid;
}

# move log messages to a new device
sub moveLogMessages {
    my $self       = shift;
    my $id_old     = shift;
    my $id_new     = shift;
    my $device     = undef;
    my $device_ref = undef;
    my $sql        = "update LogMessage set DeviceID=$id_new Where DeviceID=$id_old;";
    my $sth        = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();
    return;
}

# update device id on a host
sub updateDeviceID {
    my $self   = shift;
    my $id_old = shift;
    my $id_new = shift;
    my $host   = shift;

    ## Since Host.HostName is a non-null field with a uniqueness constraint, comparing the
    ## HostName field alone is sufficient to identify the one row of interest.  Comparing
    ## the DeviceID field as well just ensures that we only update the row if it still
    ## contains the value we found it did when we earlier probed for such data.
    my $sql = "update Host set DeviceID=$id_new Where DeviceID='$id_old' and HostName='$host';";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();
    return;
}

# remove device
sub removeDevice {
    my $self = shift;
    my $id   = shift;
    my $sql  = "delete from Device where DeviceID=$id;";
    my $sth  = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();
    return;
}

# update device Identification
sub updateDeviceIdent {
    my $self      = shift;
    my $id_old    = shift;
    my $new_ident = shift;
    my $sql       = "update Device set Identification='$new_ident' Where DeviceID='$id_old';";
    my $sth       = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();
    return;
}

# count multiple hosts with same device
sub countHostsDevice {
    my $self  = shift;
    my $id    = shift;
    my $sql   = "select count(*) from Host where DeviceID=$id;";
    my $count = $self->{dbh}->selectrow_array($sql);
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# find all hosts with same device
sub getHostsDevice {
    my $self  = shift;
    my $id    = shift;
    my $hosts = undef;
    my $host  = undef;
    my $sql   = "select
	HostID			as \"HostID\",
	DeviceID		as \"DeviceID\",
	HostName		as \"HostName\",
	Description		as \"Description\",
	ApplicationTypeID	as \"ApplicationTypeID\"
	from Host where DeviceID=$id;";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();

    while ( my $hashref = $sth->fetchrow_hashref() ) {
	$host = $hashref->{HostName};
	foreach my $key ( keys %{$hashref} ) {
	    $hosts->{$host}->{$key} = $hashref->{$key};
	}
    }
    $sth->finish();

    print "Found duplicate hosts for Device ID $id: " . ( join( " ", keys %$hosts ) ) . "\n" if $debug and $hosts and keys %$hosts > 1;

    # This return value might be undefined; the caller always needs to test before dereferencing.
    return $hosts;
}

# create a new device
sub createDevice {
    my $self  = shift;
    my $ident = shift;
    my $dname = shift;
    my $sql   = "insert into Device (DisplayName, Identification) values ('$dname', '$ident');";
    my $sth   = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();

    $sql = "select DeviceID as \"DeviceID\" from Device where Identification='$ident' and DisplayName='$dname';";
    my $id = $self->{dbh}->selectrow_array($sql);

    # This return value might be undefined; the caller always needs to test before using the value.
    return $id;
}

# update device Displayname
sub updateDeviceDisplay {
    my $self      = shift;
    my $ident     = shift;
    my $new_dname = shift;
    my $sql       = "update Device set DisplayName='$new_dname' Where Identification='$ident';";
    my $sth       = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish();
    return;
}

# Main Program

# Initialize case counters.
my $duplicate_devices_condensed               = 0;
my $devices_with_hostname_as_identification   = 0;
my $correctly_identified_devices              = 0;
my $devices_created_to_split_host_assignments = 0;
my $devices_with_unmatched_display_name       = 0;
my $devices_with_same_ip_address              = 0;
my $devices_with_unfixable_conditions         = 0;

eval {
    $foundation = CollageQuery->new();
};
if ($@) {
    print "FATAL:  Cannot open a connection to Foundation.\n";
    exit 1;
}

my $app_types = getApplicationTypes($foundation);
if ( not defined $app_types ) {
    print "FATAL:  Cannot find application types in Foundation.\n";
    exit 1;
}

# Get the device table associated with each host.
my $device_by_host = foundation_query_devices($foundation);
if (not defined $device_by_host) {
    print "FATAL:  Cannot find any hosts in Foundation.\n";
    exit 1;
}
if ( $debug > 1 ) {
    print "printing host-device hash\n";
    print Data::Dumper->Dump( [ $device_by_host ], [qw($device_by_host)] );
    print "end of host-device hash\n";
}
StorProc->dbconnect();

# Instead of running one or more StorProc->fetch_host_address($hostname) calls per host,
# we instead just run one bulk retrieval, then feed off that.  This is far more efficient.
my $host_address = StorProc->fetch_map('hosts', 'name', 'address');  # $host_address->{$hostname} = $address;

# Sorting the hostnames here is pretty much a waste of time.  We only
# do so to force any log messages to be in somewhat more readable order.
foreach my $hostname ( sort keys %$device_by_host ) {

    # Figure out the various cases of incorrect config
    # first, if everything is fine, go on to the next
    print "Display Name: $device_by_host->{$hostname}{'DisplayName'}\n"      if $debug > 1;
    print "Identification: $device_by_host->{$hostname}{'Identification'}\n" if $debug > 1;

    # Get the address from the monarch database.  But it might be a VEMA host, so it might not be in Monarch.
    # However, if it does have an ApplicationType of NAGIOS, then Monarch should be the controlling application.
    # Note that if we find a host which has NAGIOS as its application type but is not in the monarch database,
    # we should not delete it here, because want to leave that to the next Commit operation instead, presuming
    # that the Commit action will remove the appropriate host object at that time.
    my $ip_address = $host_address->{$hostname};
    print "Address: $ip_address\n" if $debug > 1 and defined $ip_address;
    if (   defined($ip_address)
	&& ( $device_by_host->{$hostname}{'DisplayName'} eq $hostname )
	&& ( $device_by_host->{$hostname}{'Identification'} eq $ip_address ) )
    {
	++$correctly_identified_devices;
	next;
    }

    if ( $device_by_host->{$hostname}{'Identification'} eq $hostname ) {
	## The host name is used as Identification instead of the IP address we normally expect;
	## the IP address is missing in the Device entry.
	print "Found a device with hostname as Identification: $hostname\n" if $debug;

	if ( not defined $ip_address ) {
	    ## The host is not in Monarch, so we do nothing.  In terms of classifying this situation,
	    ## "unfixable" might mean a bad condition, or just one that we expect and tolerate.
	    ++$devices_with_unfixable_conditions;
	}
	else {
	    ## Get the full device info for this host's device, including its current DeviceID field.
	    my $old_device = getDeviceMatch( $foundation, $device_by_host->{$hostname}{'Identification'} );
	    if (not defined $old_device) {
		print "ERROR:  Cannot find device for host \"$hostname\" by Identification \"$device_by_host->{$hostname}{'Identification'}\".\n\n" if $debug;
		## We'll count this as an unfixable device just so this case is included somewhere in
		## the final statistics, even though there's apparently no actual device in play here.
		++$devices_with_unfixable_conditions;
	    }
	    else {
		## Determine if the same IP address is used somewhere else in the Device table.
		my $matched_device = getDeviceMatch($foundation, $ip_address);
		if ( defined $matched_device ) {
		    if ($debug) {
			print "PROBLEM:  You have host \"$hostname\" in Foundation, which Monarch has at IP address \"$ip_address\",\n";
			print "  but that IP address is also used by Foundation device \"$matched_device->{'DisplayName'}\".\n";
			print Data::Dumper->Dump( [ \%{$matched_device} ], [qw(\%{$matched_device})] );
		    }
		    print "Moving log messages to the correctly identified host.\n\n" if $debug;
		    print Data::Dumper->Dump( [ \%{$old_device} ], [qw(\%{$old_device})] ) if $debug;
		    my $old_dev_id = $old_device->{'DeviceID'};
		    my $new_dev_id = $matched_device->{'DeviceID'};
		    my $res        = moveLogMessages( $foundation, $old_dev_id, $new_dev_id );

		    my $hostcount  = countHostsDevice($foundation, $old_dev_id);
		    if ( $hostcount == 1 ) {
			$res = updateDeviceID( $foundation, $old_dev_id, $new_dev_id, $hostname );
			$res = removeDevice($foundation, $old_dev_id);
			++$duplicate_devices_condensed;    # IP address is used on another device.  De-duplicated device records.
		    }
		    else {
			print "Multiple hosts refer to device \"$old_device->{'DisplayName'}\" (identified as \"$old_device->{'Identification'}\").\n" if $debug;
			print Data::Dumper->Dump( [ \%{$old_device} ], [qw(\%{$old_device})] ) if $debug;
			my $duphosts = getHostsDevice($foundation, $old_dev_id);
			if (not defined $duphosts) {
			    ## We found multiple hosts when we counted just above, so if we didn't find them again when we tried
			    ## to get their details, somebody else is messing with the database while we're trying to fix it.
			    print "ERROR:  Cannot find hosts that refer to device \"$old_device->{'DisplayName'}\".\n" if $debug;
			    ++$devices_with_unfixable_conditions;
			}
			else {
			    print Data::Dumper->Dump( [ \%{$duphosts} ], [qw(\%{$duphosts})] ) if $debug;

			    # Loop through these hosts and create a separate device for each one except the original.
			    foreach my $duphost ( sort keys %{$duphosts} ) {
				next if $duphost eq $hostname;

				# Get the IP address (hopefully) from Monarch.
				my $ip_address_dup = $host_address->{$duphost};
				if ( defined $ip_address_dup ) {
				    if ( $ip_address_dup ne $ip_address ) {
					## Attempt to create a new device, using $ip_address_dup as the Identification.
					## However, note that the insertion will fail if such a device already exists.
					## If it already exists with the same hostname as the display name, there's no
					## problem; we'll get back the device ID for that device, and we can pretend it
					## was new.  If it already exists with some other display name, the insertion
					## will fail and we get back an undefined $dev_id value.
					my $dev_id = createDevice( $foundation, $ip_address_dup, $duphost );
					if ( defined $dev_id ) {
					    ++$devices_created_to_split_host_assignments;
					    print "Created device ID: $dev_id, Identification: $ip_address_dup, DisplayName: $duphost\n" if $debug;
					    $res = updateDeviceID( $foundation, $old_dev_id, $dev_id, $duphost );
					}
					else {
					    print
					      "ERROR:  Cannot create new device for IP address (Identification) \"$ip_address_dup\", display name \"$duphost\".\n"
					      if $debug;
					    ++$devices_with_unfixable_conditions;
					}
				    }
				    else {
					## $hostname and $duphost are different hostnames that share the same IP address.
					print "WARNING:  Could not create new device.  You have two hosts with the same address.\n";
					print "Please Check $hostname and $duphost (both with IP address $ip_address).\n";
					print "__________________ No action possible ____________________\n\n";
					++$devices_with_same_ip_address;
				    }
				}
				else {
				    ## The host is not currently known to Monarch.
				    print "WARNING:  Could not create new device, because we don't know the IP address to use.\n";
				    print "Please check $hostname ($ip_address) and compare it to $duphost (unknown IP address).\n";
				    print "__________________ No action possible ____________________\n\n";
				    ++$devices_with_unfixable_conditions;
				}
			    }
			}
		    }
		}
		else {
		    ## The IP address is known to Monarch (associated with this host there), but not used elsewhere in
		    ## Foundation.  For a host that has an ApplicationType of 'NAGIOS' (meaning it is being managed
		    ## by Monarch), we would like to update the device Identification field with the IP address.  For
		    ## hosts with other ApplicationType values, such as 'VEMA', we won't update the Identfication field
		    ## because we assume that the controlling application for this device has its own notion of what the
		    ## Identification field should contain, and that it properly maintains its associated devices.
		    my $old_dev_id = $old_device->{'DeviceID'};
		    my $duphosts = getHostsDevice($foundation, $old_dev_id);
		    if ( !defined($duphosts) or scalar keys %$duphosts == 0 ) {
			## For whatever reason, we can't find any hosts associated with this device,
			## so we cannot check whether it is managed by Monarch.
			print "ERROR:  Cannot find hosts that refer to device \"$old_device->{'DisplayName'}\".\n" if $debug;
			++$devices_with_unfixable_conditions;
		    }
		    else {
			## This present construction defers to any other owner of the device.  We might want to
			## change this so we apply the update if any owner is NAGIOS, not only if all owners match.
			my $change_id = 1;
			foreach my $duphost ( keys %$duphosts ) {
			    my $apptype = $app_types->{ $duphosts->{$duphost}{ApplicationTypeID} };
			    $change_id = 0 if !defined($apptype) or $apptype ne 'NAGIOS';
			}
			if ($change_id) {
			    print "Updating device \"$hostname\" to have Identification \"$ip_address\".\n" if $debug;
			    my $res = updateDeviceIdent( $foundation, $old_dev_id, $ip_address );
			    ++$devices_with_hostname_as_identification;
			}
			else {
			    ++$devices_with_unfixable_conditions;
			}
		    }
		}
	    }
	}
    }
    elsif ( $device_by_host->{$hostname}{'DisplayName'} ne $hostname ) {
	print "Found device for $hostname with Display Name that does not match hostname\n" if $debug;
	print "Checking if more than one host shares this device ID ...\n"                  if $debug;
	my $old_dev_id = getDeviceID($foundation, $hostname);
	my $hostcount  = countHostsDevice($foundation, $old_dev_id);
	if ( $hostcount == 1 ) {
	    print "Just this host for this device.  Updating Display Name.\n" if $debug;
	    my $res = updateDeviceDisplay( $foundation, $device_by_host->{$hostname}{'Identification'}, $hostname );
	    ++$devices_with_unmatched_display_name;
	}
	else {
	    print "WARNING:  Multiple hosts have device ID $old_dev_id.\n" if $debug;
	    my $duphosts = getHostsDevice($foundation, $old_dev_id);
	    if (not defined $duphosts) {
		## We found multiple hosts when we counted just above, so if we didn't find them again when we tried
		## to get their details, somebody else is messing with the database while we're trying to fix it.
		print "ERROR:  Cannot find hosts that refer to device \"$device_by_host->{$hostname}{'DisplayName'}\".\n" if $debug;
		++$devices_with_unfixable_conditions;
	    }
	    else {
		if ( not defined $ip_address ) {
		    ## The host is not in Monarch, so we cannot find the IP address we should be avoiding when we want
		    ## to create new devices for all the other hosts that share this same Device ID, so we do nothing.
		    ## Cleanup for this case will need to be done manually.  But we can at least spill out the list of
		    ## hosts that share the same device, to make that process easier.
		    print "These hosts are: " . join (', ', sort keys %{$duphosts}) . "\n";
		    print "This issue will require manually updating the Foundation database.\n";
		    ++$devices_with_unfixable_conditions;
		}
		else {
		    ## Loop through these hosts and create a separate device for each one except the original.
		    foreach my $duphost ( sort keys %{$duphosts} ) {
			next if $duphost eq $hostname;

			# Get the IP address (hopefully) from Monarch.
			my $ip_address_dup = $host_address->{$duphost};
			if ( defined $ip_address_dup ) {
			    if ( $ip_address_dup ne $ip_address ) {
				my $dev_id = createDevice( $foundation, $ip_address_dup, $duphost );
				if (defined $dev_id) {
				    ++$devices_created_to_split_host_assignments;
				    print "Created device ID: $dev_id, Identification: $ip_address_dup, DisplayName: $duphost\n" if $debug;
				    my $res = updateDeviceID( $foundation, $old_dev_id, $dev_id, $duphost );
				}
				else {
				    print
				      "ERROR:  Cannot create new device for IP address (Identification) \"$ip_address_dup\", display name \"$duphost\".\n"
				      if $debug;
				    ++$devices_with_unfixable_conditions;
				}
			    }
			    else {
				## $hostname and $duphost are different hostnames that share the same IP address.
				print "WARNING:  Could not create new device.  You have two hosts with the same address.\n";
				print "Please Check $hostname and $duphost (both with IP address $ip_address).\n";
				print "__________________ No action possible ____________________\n\n";
				++$devices_with_same_ip_address;
			    }
			}
			else {
			    ## The host is not currently known to Monarch.
			    print "WARNING:  Could not create new device, because we don't know the IP address to use.\n";
			    print "Please check $hostname ($ip_address) and compare it to $duphost (unknown IP address).\n";
			    print "__________________ No action possible ____________________\n\n";
			    ++$devices_with_unfixable_conditions;
			}
		    }
		}
	    }
	}
    }
    elsif ( defined($ip_address) and $device_by_host->{$hostname}{'Identification'} ne $ip_address ) {
	print "Found device for $hostname with IP address that does not match Monarch.\n" if $debug;

	# Determine if the IP address is used somewhere else in the Device table.
	my $matched_device = getDeviceMatch($foundation, $ip_address);
	if ( defined $matched_device ) {
	    print "PROBLEM:  You have host \"$hostname\" in Foundation, which Monarch has at IP address \"$ip_address\",\n";
	    print "  but that IP address is also used by Foundation device \"$matched_device->{'DisplayName'}\".\n";
	    print "This issue will require manually updating the Foundation database.\n";
	    ++$devices_with_unfixable_conditions;
	}
	else {
	    ## The IP address is known to Monarch (associated with this host there), but not used elsewhere in Foundation.
	    ## Just update the device Identification field with the IP address.
	    my $old_device = getDeviceMatch( $foundation, $device_by_host->{$hostname}{'Identification'} );
	    if ( not defined $old_device ) {
		print "ERROR:  Cannot find host \"$hostname\" by Identification \"$device_by_host->{$hostname}{'Identification'}\".\n\n"
		  if $debug;
		++$devices_with_unfixable_conditions;
	    }
	    else {
		## The IP address is known to Monarch (associated with this host there), but not used elsewhere in
		## Foundation.  For a host that has an ApplicationType of 'NAGIOS' (meaning it is being managed
		## by Monarch), we would like to update the device Identification field with the IP address.  For
		## hosts with other ApplicationType values, such as 'VEMA', we won't update the Identfication field
		## because we assume that the controlling application for this device has its own notion of what
		## the Identification field should contain, and that it properly maintains its associated devices.
		my $old_dev_id = $old_device->{'DeviceID'};
		my $duphosts = getHostsDevice($foundation, $old_dev_id);
		if ( !defined($duphosts) or scalar keys %$duphosts == 0 ) {
		    ## For whatever reason, we can't find any hosts associated with this device,
		    ## so we cannot check whether it is managed by Monarch.
		    print "ERROR:  Cannot find hosts that refer to device \"$old_device->{'DisplayName'}\".\n" if $debug;
		    ++$devices_with_unfixable_conditions;
		}
		else {
		    ## This present construction defers to any other owner of the device.  We might want to
		    ## change this so we apply the update if any owner is NAGIOS, not only if all owners match.
		    my $change_id = 1;
		    foreach my $duphost ( keys %$duphosts ) {
			my $apptype = $app_types->{ $duphosts->{$duphost}{ApplicationTypeID} };
			$change_id = 0 if !defined($apptype) or $apptype ne 'NAGIOS';
		    }
		    if ($change_id) {
			print "Updating device \"$hostname\" to have Identification \"$ip_address\".\n" if $debug;
			my $res = updateDeviceIdent( $foundation, $old_dev_id, $ip_address );
			++$devices_with_hostname_as_identification;
		    }
		    else {
			++$devices_with_unfixable_conditions;
		    }
		}
	    }
	}
    }
    else {
	print "Found host with unknown condition: $hostname\n Please check the database - this program does not recognize how to deal with this host!\n"
	  if $debug;
	print Data::Dumper->Dump( [ \%{ $device_by_host->{$hostname} } ], [qw(\%{$device_by_host->{$hostname}})] ) if $debug;
	++$devices_with_unfixable_conditions;
    }
}

# Output summary data
my $total_cases =
  $duplicate_devices_condensed +
  $devices_with_hostname_as_identification +
  $correctly_identified_devices +
  $devices_created_to_split_host_assignments +
  $devices_with_unmatched_display_name +
  $devices_with_same_ip_address +
  $devices_with_unfixable_conditions;
my $max_digits = length "$total_cases";

# There may be some duplicate device counting that goes on above under error conditions,
# so the "unfixable" count is not necessarily a completely accurate indicator.
print "========================================================================\n";
print sprintf "%${max_digits}d devices found duplicated, condensed to one\n", $duplicate_devices_condensed;
print sprintf "%${max_digits}d devices found with hostname for Identification, not duplicate, assigned an address\n", $devices_with_hostname_as_identification;
print sprintf "%${max_digits}d devices correctly Identified -- no action taken\n", $correctly_identified_devices;
print sprintf "%${max_digits}d devices created for hosts assigned to the same device\n", $devices_created_to_split_host_assignments;
print sprintf "%${max_digits}d devices with unmatched display name, changed display name to hostname\n", $devices_with_unmatched_display_name;
print sprintf "%${max_digits}d devices with the same IP address, can't change device Display Name\n", $devices_with_same_ip_address;
print sprintf "%${max_digits}d devices could not be fixed for unknown conditions (might be okay, might be broken)\n", $devices_with_unfixable_conditions;
print sprintf "%${max_digits}d Total device-cases processed\n", $total_cases;
print sprintf "%${max_digits}d Total hosts processed\n", scalar keys %$device_by_host;
print "========================================================================\n";

StorProc->dbdisconnect();
exit;
