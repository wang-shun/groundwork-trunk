#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright 2007-2011 GroundWork Open Source, Inc.
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# To do:
# (*) possibly include support for some kind of exclude_hosts capability,
#     perhaps based on a configured series of excluded-hostname patterns

use strict;

use DBI;

# Be sure to keep this up-to-date!
my $VERSION = '2.0.0';

my ( $dbhost, $dbport, $dbname, $dbuser, $dbpass, $dbtype );

my $cacti_props = '/usr/local/groundwork/config/cacti.properties';

my $data_file      = '/usr/local/groundwork/core/monarch/automation/data/cacti_data.txt';
my $temp_data_file = $data_file . '.new';

# $data_file = "/usr/local/groundwork/core/monarch/automation/data/auto-discovery-Cacti-Sync.txt";

my $config = read_config($cacti_props);
if (!defined $config) {
    print "Error:  Cannot read the \"$cacti_props\" config file.\n";
    exit 1;
}

# Here we work out how many Cacti instances are defined in the cacti.properties file
# and then loop through each to find database content that matches the query parameters.

my @instance_labels = ();
foreach my $parameter (keys %$config) {
    if ($parameter =~ /^cacti\.(\w+)\.host$/) {
	push @instance_labels, $1;
    }
}

my @output = ();

foreach my $instance (@instance_labels) {

    $dbhost = config_value($config, "cacti.$instance.dbhost");
    $dbport = config_value($config, "cacti.$instance.dbport");
    $dbname = config_value($config, "cacti.$instance.dbname");
    $dbuser = config_value($config, "cacti.$instance.dbuser");
    $dbpass = config_value($config, "cacti.$instance.dbpass");
    $dbtype = config_value($config, "cacti.$instance.dbtype");

    if ( !defined($dbhost)
      || !defined($dbport)
      || !defined($dbname)
      || !defined($dbuser)
      || !defined($dbpass) ) {
	print "Error:  Cannot find Cacti database access parameters for instance '$instance'.\n";
	exit 1;
    }

    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
    if ( !$dbh ) {
	print "Cannot connect to the \"$dbname\" database. Error: " . $DBI::errstr;
	exit 2;
    }

    my %host_templates      = ();
    my $default_template_id = undef;
    my $sqlstmt             = "select id, name from host_template";
    my $sth                 = $dbh->prepare($sqlstmt) or die $dbh->errstr;
    $sth->execute() or die $sth->errstr;
    while ( my @values = $sth->fetchrow_array() ) {
	$host_templates{ $values[0] } = $values[1];
	$default_template_id = $values[0] if $values[1] eq 'Generic SNMP-enabled Host';
    }
    $sth->finish;

    push @output, "# hostname;;description;;template_info;;disabled;;status\n";
    $sqlstmt = "select hostname, description, host_template_id, disabled, status from host";
    $sth = $dbh->prepare($sqlstmt) or die $dbh->errstr;
    $sth->execute() or die $sth->errstr;
    while ( my @values = $sth->fetchrow_array() ) {
	# It's possible to run the cacti discovery without assigning a host template,
	# which is fine, so we anticipate that, and assign a default template if we
	# find that case, if we have such a default template available.
	my $host_template = $host_templates{ $values[2] };
	if ( not defined $host_template ) {
	    $host_template = defined($default_template_id) ? $host_templates{$default_template_id} : '';
	}
	$values[0] = '' if not defined $values[0];  # cacti.host.hostname can be NULL
	$values[3] = '' if not defined $values[3];  # cacti.host.disabled can be NULL
	push @output, "$values[0];;$values[1];;$host_template;;$values[3];;$values[4]\n";
    }
    $sth->finish;

    $dbh->disconnect();
}

# We write to a temporary file so putting the new file in place
# is an atomic operation with respect to the eventual reader.
open( FILE, '>', $temp_data_file ) or die "Error:  Unable to open $temp_data_file ($!)";
print FILE @output or die "Error:  Unable to write to $temp_data_file ($!)";
close(FILE) or die "Error:  Unable to close $temp_data_file ($!)";
rename $temp_data_file, $data_file or die "Error:  Cannot rename $temp_data_file ($!)";

sub read_config {
    my $config_file = shift;
    my %config      = ();

    if ( !open(CONFIG, '<', $config_file) ) {
        print "Error:  Unable to open configuration file $config_file ($!).\n";
        return undef;
    }
    while (my $line = <CONFIG>) {
        chomp $line;
        if ( $line =~ /^\s*([^#]\S*)\s*=\s*(\S+)\s*$/ ) {
            $config{$1} = $2;
        }
    }
    close CONFIG;
    return \%config;
}

# This routine allows indirection at each key component level.
# Normally, application code does not call this with a subkey or recursion level;
# that argument is only used for recursive calls.
# FIX LATER:  Compare the ability to support indirection in configuration-key
# components to what TypedConfig and Config::General can do in that respect, and
# perhaps generalize the capabilities of TypedConfig to match what we have here.
sub config_value {
    my $config = shift;
    my $key    = shift;
    my $subkey = shift;
    my $level  = shift || 0;

    if (++$level > 100) {
        my $fullkey = (defined $subkey) ? "$key.$subkey" : $key;
        print "Error:  Too many levels of indirection found in config file when searching for \"$fullkey\".\n";
        exit 1;
    }

    if (!defined $subkey) {
        if (exists $config->{$key}) {
            return $config->{$key};
        }
        if ($key =~ /(\S+)\.(\S+)/) {
            return config_value($config,$1,$2,$level);
        }
        return undef;
    }
    if (exists $config->{"$key.$subkey"}) {
        return $config->{"$key.$subkey"};
    }
    if (exists $config->{$key}) {
        my $keyvalue = $config->{$key};
        if (defined($keyvalue) && $keyvalue =~ /^\$./) {
            $keyvalue =~ s/^\$//;
            return config_value($config,"$keyvalue.$subkey",undef,$level);
        }
        return undef;
    }
    if ($key =~ /(\S+)\.(\S+)/) {
        return config_value($config,$1,"$2.$subkey",$level);
    }
    return undef;
}
