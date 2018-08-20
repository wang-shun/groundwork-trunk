#!/usr/local/groundwork/perl/bin/perl -w --

# cacti_graphs.cgi
# Copyright (c) 2011 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. Use is subject to GroundWork commercial license terms.

# BE SURE TO KEEP THIS UP-TO-DATE!
my $VERSION = '0.3.0 (October 23, 2011)';

# ========================
# Documentation
# ========================

# This script maps simple CGI query parameters into a Cacti URL that yields
# a full set of graphs for a given host.  All the Cacti databases which are
# configured in the cacti.properties file are searched until we find one which
# contains both the specified host and a matching graph-tree setup for that
# host.  If the specified graph tree is not found in a database where the
# specified host is found, we attempt to find a default graph tree setup for
# that host instead.

# Use of this script requires setup in both cacti.properties and
# status-viewer.properties to operate as intended.

# To do:
# (*) Code to read the cacti.properties file is stolen and adapted from the
#     find_cacti_graphs script.  Abstract portions of that script into a GW::Cacti
#     package, so we can more readily share the code between applications.
# (*) The present version of this script accepts a graph_tree_name query parameter
#     and backs that up with a built-in, hardcoded default graph tree name.  A
#     future version of this script could perhaps generalize the graph_tree_name
#     query parameter to allow specifying an ordered list of possible graph tree
#     names to look for, possibly with "*" allowed as a completely wildcarded last
#     entry.  The value of the parameter could be set as either multiple separate
#     values in the query URL (if the CGI package is guaranteed to maintain that
#     same ordering when it fetches the values for use in the script), or via a
#     punctuation-separated list of graph tree names.  A comma or semicolon, or
#     perhaps whatever punctuation is either disallowed by Cacti in a graph tree
#     name, or some character which is otherwise unlikely to appear in a name,
#     would be the appropriate name separator.

# ========================
# Perl setup
# ========================

use strict;

use CGI;
$CGI::POST_MAX        = 1024 * 1024;    # max 1M posts, for security
$CGI::DISABLE_UPLOADS = 1;              # no uploads, for security

use DBI;
use HTML::Entities;

# ========================
# Configuration values
# ========================

my $cacti_props = '/usr/local/groundwork/config/cacti.properties';

my $default_graph_tree_name = 'Default Tree';

# These values will perhaps go away in a future release, when we will depend instead
# on values drawn from cacti.properties.
my $default_cacti_protocol = 'http';
my $default_cacti_urlpath  = 'nms-cacti';

# You can set this flag to 1 to display more detailed diagnostics,
# if we ever run into problems in the field.
my $debug = 0;

# ========================
# Working global variables
# ========================

my @debug_text = ();

# ========================
# Program
# ========================

my $query = new CGI;

# Success has opposite polarity in the outside world.
exit (main() ? 0 : 1);

# ========================
# Supporting routines
# ========================

sub main {
    my $cgi_error = $query->cgi_error();
    if (defined $cgi_error) {
	print $query->header(-status => $cgi_error);
	if ($cgi_error eq '413 Request entity too large') {
	    ## We exceeded POST_MAX.
	    print_error(413, 'You have tried to upload a file which is too large.');
	}
	elsif ($cgi_error eq '400 Bad request (malformed multipart POST)') {
	    ## Heck, file uploads don't even make sense for this script.  Are we being attacked?
	    print_error(400, 'A file upload has failed.');
	}
	elsif ($cgi_error =~ /\s*(\d+) (.*)/) {
	    print_error($1, $2);
	}
	else {
	    print_error(400, $cgi_error);
	}
	return 0;
    }

    return cacti_graph_redirect();
}

# Security check:  Make sure we have a valid hostname,
# before we go blindly substituting it into strings.
# Reference:  http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names
# * FQDN max length:  255 characters
# * FQDN structure:  a series of FQDN components, separated by single "." characters
# * FQDN component length:  1 to 63 characters
# * FQDN component character set:  [-a-zA-Z0-9]
# * FQDN component structure:  cannot start or end with a hyphen

sub is_valid_hostname {
    my $name = shift;
    return 0 if not defined $name;
    my $name_length = length $name;
    return 0 if $name_length < 1 || $name_length > 255;
    foreach my $part ( split(/\./, $name, -1) ) {
	my $part_length = length $part;
	return 0 if $part_length < 1 || $part_length > 63;
	return 0 if $part =~ /[^-a-zA-Z0-9]/;
	return 0 if $part =~ /^-/;
	return 0 if $part =~ /-$/;
    }
    return 1;
}

sub cacti_graph_redirect {
    my ( $dbhost, $dbport, $dbname, $dbuser, $dbpass, $dbtype );
    my $error              = undef;
    my $host_id            = undef;
    my $graph_tree_id      = undef;
    my $graph_tree_item_id = undef;

    # The cacti.host.hostname field contains an IP address.  What looks to the outside world
    # like a hostname is actually stored in the cacti.host.description field instead.  If
    # use_address is true, we will look up the host name someplace to find its address, then
    # search for that in the cacti.host.hostname field.  If use_address is false or missing
    # (the common case), we will search for the hostname in the cacti.host.description field.

    my $graph_tree_name = $query->param('graph_tree_name');
    my $host_name       = $query->param('host_name');
    my $use_address     = $query->param('use_address');

    # Let's not allow any injection vulnerabilities here.
    $host_name = undef if not is_valid_hostname($host_name);
    if ( !defined($graph_tree_name) or !defined($host_name) ) {
	print_error( 400,
	    $debug ? (
		!defined($graph_tree_name) ? 'Invalid graph tree name.' :
		!defined($host_name)       ? 'Invalid host name.'       :
					     'Unknown failure.'
	    ) : 'Invalid query parameters.'
	);
	return 0;
    }
    my $host = $use_address ? ip_address($host_name) : $host_name;

    my $config = read_config($cacti_props);
    if (!defined $config) {
	print_error( 500,
	    $debug
	    ? "Cannot read the \"$cacti_props\" config file."
	    : 'Cannot read internal configuration.'
	);
	return 0;
    }

    # Here we work out how many Cacti instances are defined in the cacti.properties file
    # and then loop through each to find database content that matches the query parameters.

    my @instance_labels = ();
    foreach my $parameter (keys %$config) {
	if ($parameter =~ /^cacti\.(\w+)\.host$/) {
	    push @instance_labels, $1;
	}
    }

    foreach my $instance (@instance_labels) {
	my $successful = 1;
	my $protocol   = config_value( $config, "cacti.$instance.protocol" );
	my $domain     = config_value( $config, "cacti.$instance.host" );
	my $port       = config_value( $config, "cacti.$instance.port" );
	my $urlpath    = config_value( $config, "cacti.$instance.urlpath" );
	my $webserver  = ( defined $port ) ? "$domain:$port" : $domain;
	$protocol = $default_cacti_protocol if not defined $protocol;
	$urlpath  = $default_cacti_urlpath  if not defined $urlpath;
	my $cacti_base_url = "$protocol://$webserver/$urlpath";
	$cacti_base_url =~ s{/+$}{};

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
	    print_error( 500,
		$debug
		? "Cannot find Cacti database access parameters for instance '$instance'."
		: 'Internal misconfiguration.'
	    );
	    return 0;
	}

	my $dbh     = undef;
	my $sth     = undef;
	my $sqlstmt = undef;

	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost;port=$dbport";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost;port=$dbport";
	}

	eval {
	    $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1, 'RaiseError' => 1, 'PrintError' => 0 } );
	};
	if ( !$dbh ) {
	    ## Possibly we can move on and find what we're looking for in some other database, and only
	    ## report this error if we are unable to find the data we seek in all other databases, so for
	    ## now we don't immediately treat this as a fatal exception.  The difference is mostly in what
	    ## might happen if you have multiple Cacti databases, and say the first one is down but that
	    ## shouldn't matter because it's the second one that happens to contain what we need anyway.
	    my $errstr = $DBI::errstr;
	    chomp $errstr;
	    $error = "Cannot connect to the \"$dbname\" database on \"$dbhost\" ($errstr).";
	    $successful = 0;
	}

	if ($successful) {
	    $sqlstmt = 'select id from host where ' . ($use_address ? 'hostname' : 'description') . ' = ?';
	    eval {
		$sth = $dbh->prepare($sqlstmt);
		$sth->execute($host);
		my @values = $sth->fetchrow_array();
		$sth->finish();
		if (@values) {
		    $host_id = $values[0];
		}
		else {
		    ## Cannot find a suitable host in this database.
		    push @debug_text, "Cannot find host \"$host\" in the \"$dbname\" database on \"$dbhost\"." if $debug;
		    $successful = 0;
		}
	    };
	    if ($@) {
		chomp $@;
		$error = "Searching for a host in the \"$dbname\" database on \"$dbhost\" failed ($@).";
		$successful = 0;
	    }
	}

	my $found_graph_tree_name = undef;
	if ($successful) {
	    $sqlstmt = 'select id from graph_tree where name = ?';
	    eval {
		$sth = $dbh->prepare($sqlstmt);
		$sth->execute($graph_tree_name);
		my @values = $sth->fetchrow_array();
		$sth->finish();
		if (@values) {
		    $graph_tree_id         = $values[0];
		    $found_graph_tree_name = $graph_tree_name;
		}
		elsif ($graph_tree_name ne $default_graph_tree_name) {
		    $sth->execute($default_graph_tree_name);
		    @values = $sth->fetchrow_array();
		    $sth->finish();
		    if (@values) {
			$graph_tree_id         = $values[0];
			$found_graph_tree_name = $default_graph_tree_name;
		    }
		    else {
			## Cannot find a suitable graph tree in this database.
			push @debug_text,
			    "Cannot find graph tree \"$graph_tree_name\" or \"$default_graph_tree_name\" in the \"$dbname\" database on \"$dbhost\"."
			    if $debug;
			$successful = 0;
		    }
		}
		else {
		    ## Cannot find a suitable graph tree in this database.
		    push @debug_text, "Cannot find graph tree \"$graph_tree_name\" in the \"$dbname\" database on \"$dbhost\"." if $debug;
		    $successful = 0;
		}
	    };
	    if ($@) {
		chomp $@;
		$error = "Searching for a graph tree in the \"$dbname\" database on \"$dbhost\" failed ($@).";
		$successful = 0;
	    }
	}

	if ($successful) {
	    $sqlstmt = 'select id from graph_tree_items where graph_tree_id = ? and host_id = ?';
	    eval {
		$sth = $dbh->prepare($sqlstmt);
		$sth->execute($graph_tree_id, $host_id);
		my @values = $sth->fetchrow_array();
		$sth->finish();
		if (@values) {
		    $graph_tree_item_id = $values[0];
		}
		elsif ($found_graph_tree_name ne $default_graph_tree_name) {
		    my $sqlstmt2 = 'select id from graph_tree where name = ?';
		    eval {
			my $sth2 = $dbh->prepare($sqlstmt2);
			$sth2->execute($default_graph_tree_name);
			@values = $sth2->fetchrow_array();
			$sth2->finish();
			if (@values) {
			    $graph_tree_id         = $values[0];
			    $found_graph_tree_name = $default_graph_tree_name;
			}
			else {
			    ## Cannot find a suitable graph tree in this database.
			    push @debug_text,
				"Cannot find default graph tree \"$default_graph_tree_name\" in the \"$dbname\" database on \"$dbhost\"."
				if $debug;
			    $successful = 0;
			}
		    };
		    if ($@) {
			chomp $@;
			$error = "Searching for a default graph tree in the \"$dbname\" database on \"$dbhost\" failed ($@).";
			$successful = 0;
		    }
		    if ($successful) {
			$sth->execute($graph_tree_id, $host_id);
			@values = $sth->fetchrow_array();
			$sth->finish();
			if (@values) {
			    $graph_tree_item_id = $values[0];
			}
			else {
			    ## Cannot find a suitable graph tree item in this database.
			    push @debug_text,
				"Cannot find graph tree item for graph tree \"$graph_tree_name\" or \"$default_graph_tree_name\" and host \"$host\" in the \"$dbname\" database on \"$dbhost\"."
				if $debug;
			    $successful = 0;
			}
		    }
		}
		else {
		    ## Cannot find a suitable graph tree item in this database.
		    push @debug_text,
			"Cannot find graph tree item for graph tree \"$graph_tree_name\" and host \"$host\" in the \"$dbname\" database on \"$dbhost\"."
			if $debug;
		    $successful = 0;
		}
	    };
	    if ($@) {
		chomp $@;
		$error = "Searching for a graph tree item for host \"$host\" in the \"$dbname\" database on \"$dbhost\" failed ($@).";
		$successful = 0;
	    }
	}

	# We catch possible disconnect errors here, but they're not worth reporting.
	eval {
	    $dbh->disconnect();
	};

	if ( defined($cacti_base_url) and defined($graph_tree_id) and defined($graph_tree_item_id) ) {
	    print_redirect( $cacti_base_url, $graph_tree_id, $graph_tree_item_id );
	    return 1;
	}
    }

    print_error(
	$error ? 500 : 404,
	$debug && $error
	? $error
	: "Cannot find graphs for host \"$host_name\" under graph tree \"$graph_tree_name\""
	  . ( $graph_tree_name ne $default_graph_tree_name ? " or \"$default_graph_tree_name\"" : '' )
	  . " in any configured Cacti database."
    );
    return 0;
}

sub ip_address {
    ## FIX MINOR:  Implement some means of translating a host name to a sensible host address
    ## that Cacti might know about, keeping in mind that we might have a multi-homed machine
    ## (one with multiple network interfaces, and thus multiple addresses, potentially only
    ## one of which is known to Cacti for graphing purposes).  Possibilities include:
    ## (*) using Perl's built-in gethostbyname() function, either called in scalar context
    ##     to return only a single address (presumptively assumed to be the right one), or
    ##     called in list context to return a full set of alternate addresses and then
    ##     returning the full set of alternatives and iterating over them in the caller; or
    ## (*) searching Monarch for this host and its configured address there (subject to GWMON-9076).
    ## FIX MINOR:  Once we do support such translation, make sure we support IPv6 addresses, too.
    print_error(501, 'IP address lookup is not yet supported.');
    exit 1;
}

sub print_redirect {
    my $cacti_base_url     = shift;
    my $graph_tree_id      = shift;
    my $graph_tree_item_id = shift;
    print $query->redirect("$cacti_base_url/graph_view.php?action=tree&tree_id=$graph_tree_id&leaf_id=$graph_tree_item_id");
}

sub print_error {
    my $status = shift;
    my $text   = shift;

    my %status_code = (
	400 => '400 Bad Request',
	404 => '404 Not Found',
	413 => '413 Request Entity Too Large',
	500 => '500 Internal Server Error',
	501 => '501 Not Implemented'
    );
    my $http_status = $status_code{$status} || '500 Internal Server Error';
    print $query->header( -type => 'text/html', -status => $http_status );
    print $query->start_html( -title => 'Cacti Graphs Lookup Error' );
    print $http_status;

    if (@debug_text) {
	print $query->hr;
	print HTML::Entities::encode($_), "<br>\n" for @debug_text;
    }
    if ( defined $text ) {
	print $query->hr;
	print HTML::Entities::encode($text);
    }
    print $query->end_html();
}

sub read_config {
    my $config_file = shift;
    my %config      = ();

    if ( !open(CONFIG, '<', $config_file) ) {
	print_error( 500,
	    $debug
	    ? "Unable to open configuration file $config_file ($!)."
	    : 'Cannot open configuration file.'
	);
	exit 1;
	# return undef;
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
# FIX THIS:  Compare the ability to support indirection in configuration-key
# components to what TypedConfig and Config::General can do in that respect, and
# perhaps generalize the capabilities of TypedConfig to match what we have here.
sub config_value {
    my $config = shift;
    my $key    = shift;
    my $subkey = shift;
    my $level  = shift || 0;

    if (++$level > 100) {
	my $fullkey = (defined $subkey) ? "$key.$subkey" : $key;
	print_error( 500,
	    $debug
	    ? "Too many levels of indirection found in config file when searching for \"$fullkey\"."
	    : 'Malformed configuration file.'
	);
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

__END__

# Implementation notes:
#
# It is unfortunately the case that the "cacti" database contains NO foreign key constraints
# that would ensure consistency between tables enforced by the database itself.  It is up to
# the Cacti application to do so.  Furthermore, there are a variety of unique key constraints
# that are NOT present but ought to be, which make it possible for multiple rows to appear
# where we would expect just one.  We have to cope with those circumstances in this script,
# by making somewhat arbitrary selections (generally just the first row found) if we do
# encounter duplicate data.

