#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2008-2011 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved.  Use is subject to GroundWork commercial license terms.
#
# devclean.pl -- This is a utility script to report on and optionally
# clean up the Foundation database, removing unwanted or un-needed devices.
# Use in consultation with GroundWork Support!
#

use strict;
use warnings;

use Data::Dumper;
use Carp;
use Getopt::Long;
use CollageQuery;

my $PROGNAME = "devclean";
my $VERSION = "1.0.0";

my $debug         = 0;
my $print_help    = 0;
my $print_version = 0;
my $self          = undef;
my $cleanup       = undef;
my $cleanall      = undef;
my $cleansnmp     = undef;
my $cleansyslog   = undef;
my $fclean        = undef;

sub print_usage {
    print "usage:  devclean.pl [-h] [-v] [-d] [--cleanup | --cleanall | --cleansnmp | --cleansyslog] [--forceclean]\n";
    print "        -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -d:  turn on debug messages\n";
    print "        --cleanup:     Remove devices that are no longer used (removes Log Messages for these as well).\n";
    print "        --cleanall:    Remove unused, snmptrap only and syslog only devices (most removal possible).\n";
    print "        --forceclean:  Use with --cleanup or --cleanall. Removes devices that have no log messages associated.\n";
    print "        --cleansnmp:   Remove snmptrap devices only.\n";
    print "        --cleansyslog: Remove syslog devices only.\n";
    print " Examples:\n";
    print "    devclean.pl                        -> Prints a report of devices found. Does not delete any data.\n";
    print "    devclean.pl --cleanup              -> Typical use. This will remove devices added by Configuration\n";
    print "                                          and any log messages associated with them.\n";
    print "    devclean.pl --cleanup --forceclean -> Removes old devices, including those that have no log messages\n";
    print "                                          to determine how they were added. Use this option to remove\n";
    print "                                          devices when --cleanup alone does not get them all.\n";
}

Getopt::Long::Configure ("no_ignore_case");
if (! GetOptions (
    'help'         => \$print_help,
    'version'      => \$print_version,
    'debug-config' => \$debug,
    'cleanup'      => \$cleanup,
    'forceclean'   => \$fclean,
    'cleanall'     => \$cleanall,
    'cleansnmp'    => \$cleansnmp,
    'cleansyslog'  => \$cleansyslog
    )) {
    print "ERROR:  cannot parse command-line options!\n";
    print_usage;
    exit 1;
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2008-2011 GroundWork Open Source, Inc. (\"GroundWork\").  All rights\n";
    print "reserved.  Use is subject to GroundWork commercial license terms.\n";
}

if ($print_help) {
    print_usage;
}

exit 0 if $print_help or $print_version;

# Get device count
sub countDevices {
    my $count = undef;
    my $sql   = "select count(*) from Device;";
    my $sth   = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Get host device count
sub countHostDevices {
    my $count = undef;
    my $sql   = "select count(*) from Device where DeviceID in (select DeviceID from Host);";
    my $sth   = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Get free device count
sub countFreeDevices {
    my $count = undef;
    my $sql   = "select count(*) from Device where DeviceID not in (select DeviceID from Host);";
    my $sth   = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Get trap device count
sub countTrapDevices {
    my $count = undef;
    my $sql = "
	select count(distinct DeviceID) from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SNMPTRAP'
	and DeviceID not in (select DeviceID from Host);
    ";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Get Log device count
sub countLogDevices {
    my $count = undef;
    my $sql = "
	select count(distinct DeviceID) from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SYSLOG'
	and DeviceID not in (select DeviceID from Host);
    ";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Force clean the Cleanable Devices
sub clforce {
    my $count = undef;
    my $sql   = "select count(*) from Device where DeviceID not in (select DeviceID from Host);";
    my $sth   = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    $sql = "delete from Device where DeviceID not in (select DeviceID from Host);";
    $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Clean the Cleanable Devices
sub clfree {
    my $count = undef;
    my $sql = "
	select count(distinct DeviceID) from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and not (A.Name='SYSLOG' or A.Name='SNMPTRAP')
	and L.DeviceID not in (select DeviceID from Host);
    ";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    $sql = "
	delete from Device where DeviceID in (select distinct DeviceID from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and not (A.Name='SYSLOG' or A.Name='SNMPTRAP')
	and L.DeviceID not in (select DeviceID from Host));
    ";
    $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Clean just the SNMP created Devices
sub clsnmp {
    my $count = undef;
    my $sql = "
	select count(distinct DeviceID) from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SNMPTRAP'
	and L.DeviceID not in (select DeviceID from Host);
    ";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    $sql = "
	delete from Device where DeviceID in (select distinct DeviceID from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SNMPTRAP'
	and L.DeviceID not in (select DeviceID from Host));
    ";
    $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Clean the Syslog Created devices
sub clsyslog {
    my $count = undef;
    my $sql = "
	select count(distinct DeviceID) from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SYSLOG'
	and L.DeviceID not in (select DeviceID from Host);
    ";
    my $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    while ( my $hashref = $sth->fetchrow_hashref() ) {
	foreach my $key ( keys %{$hashref} ) {
	    $count = $hashref->{$key};
	}
    }
    $sth->finish;
    $sql = "
	delete from Device where DeviceID in (select distinct DeviceID from LogMessage as L, ApplicationType as A
	where L.ApplicationTypeID = A.ApplicationTypeID and A.Name='SYSLOG'
	and L.DeviceID not in (select DeviceID from Host));
    ";
    $sth = $self->{dbh}->prepare($sql);
    $sth->execute();
    $sth->finish;
    if ( !defined($count) ) {
	return "";
    }
    return $count;
}

# Main Program
eval {
    $self = CollageQuery->new();
};
if ($@) {
    chomp $@;
    print "$@\n";
    exit 1;
}

# Count All Devices
my $numDevices = countDevices;

# Count devices related to hosts
my $numHostDevices = countHostDevices;

# Count devices not related to hosts
my $numFreeDevices = countFreeDevices;

# Count devices not related to hosts, and probably created by SNMP traps
my $numTrapDevices = countTrapDevices;

# Count devices not related to hosts and probably created by SYSLOG messages
my $numLogDevices = countLogDevices;

print "========================================================\n";
print "Number of devices Total:  $numDevices\n";
print "Number of devices referenced by hosts:  $numHostDevices\n";
print "Number of devices NOT referenced by hosts:  $numFreeDevices\n";
print "Number of devices probably created by traps:  $numTrapDevices\n";
print "Number of devices probably created by syslog messages:  $numLogDevices\n";
print "========================================================\n";
my $cleanabletotal = $numFreeDevices - $numTrapDevices - $numLogDevices;
print "Total Unused Devices: $cleanabletotal\n";
my $cleaned = 0;

if ($cleanup) {
    print "Removing Devices that are no longer needed.\n" if $debug;
    if ($fclean) {
	$cleaned += clforce;
    }
    else {
	$cleaned += clfree;
    }
}
elsif ($cleanall) {
    if ($fclean) {
	$cleaned += clforce;
    }
    else {
	$cleaned += clfree;
    }
    $cleaned += clsnmp;
    $cleaned += clsyslog;
}
elsif ($cleansyslog) {
    $cleaned += clsyslog;
}
elsif ($cleansnmp) {
    $cleaned += clsnmp;
}

if ( $cleaned > 0 ) {
    $numDevices = countDevices;
    print "Removed $cleaned devices. Total devices now $numDevices\n";
}

exit 0;
