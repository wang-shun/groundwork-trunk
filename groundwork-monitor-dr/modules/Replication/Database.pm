package Replication::Database;

# Database functions for a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# This package contains routines that encapsulate database access
# activities needed by our Replication software.

# To do:
# * add routines to make a list of included/excluded tables for
#   replication, given the config-file specification data and probing
#   the database for table name pattern matching
# * add routines to make a cleanly formatted database dump file
# * add routines to load a database dump file
# * perhaps make a companion Application package to create lists
#   of files from the configuration data application patterns for
#   including and excluding trees and files, and copy the files
#   around as needed

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &db_credentials
    &db_credentials_file
    &db_dump_command
    &db_load_command
);

our @EXPORT_OK = qw(
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use DBI;
use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Global configuration variables.
# ================================================================

# Some of our databases are accessed via aliases in the config files,
# and so we must find out what alias to use.
# FIX LATER:  In a future release, this needs to be generalized, to handle
# multiple Cacti instances and corresponding multiple Cacti databases.

my $mysql_path     = '/usr/local/groundwork/mysql/bin/mysql.bin';
my $mysqldump_path = '/usr/local/groundwork/mysql/bin/mysqldump.bin';
my $mysql_socket   = '/usr/local/groundwork/mysql/tmp/mysql.sock';

my %credentials_file_db_alias = (
    monarch     => 'monarch',
    GWCollageDB => 'collage',
    jbossportal => 'jbossportal',
    cacti       => 'nms.cacti.cacti_main',
    nedi        => 'nms.nedi.nedi_main',
);

# Where to place database access credentials files that we can safely
# store a useful copy of the credentials in.
my $db_credentials_file_base = '/usr/local/groundwork/replication/var';

# ================================================================
# Global working variables.
# ================================================================

# ================================================================
# Supporting subroutines.
# ================================================================

# This internal routine allows indirection at each key component level.
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
	log_timed_message 'ERROR:  Too many levels of indirection found in config file when searching for ', $fullkey;
	return undef;
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

# Find the access credentials for a given database, by analyzing the particular
# format used by the supplied credentials file to pull out the desired info.
sub db_credentials {
    my $database_name    = shift;
    my $credentials_file = shift;
    my $credentials_type = shift;

    my $db_host = undef;
    my $db_port = undef;
    my $db_sock = undef;
    my $db_name = undef;
    my $db_user = undef;
    my $db_pass = undef;

    my $db_alias = $credentials_file_db_alias{$database_name} || $database_name;

    if ($credentials_type eq 'db-properties') {
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    log_timed_message "ERROR:  Unable to open credentials file $credentials_file ($!)";
	    return undef;
	}
	# FIX LATER:  Someday the port ought to be specified in the credentials file rather than defaulted here.
	# But bear in mind that this will be ignored in favor of a named-socket path if $db_host is 'localhost'.
	$db_port = 3306;
	while (my $line = <CREDENTIALS>) {
	    chomp $line;
	    if ($line =~ /\s*$db_alias\.(\S+)\s*=\s*(\S*)\s*/) {
		if ($1 eq 'dbhost') {
		    $db_host = $2;
		} elsif (($1 eq 'database') or ($1 eq 'dbdatabase')) {
		    $db_name = $2;
		} elsif (($1 eq 'username') or ($1 eq 'dbusername')) {
		    $db_user = $2;
		} elsif (($1 eq 'password') or ($1 eq 'dbpassword')) {
		    $db_pass = $2;
		}
	    }
	}
	close CREDENTIALS;
    }
    elsif ($credentials_type eq 'portal-ds-xml') {
	# Perhaps in some future release, we will parse the XML as such, to validate the
	# entire construct, and verify that we are analyzing the correct section of the
	# file.  In the meantime, we'll just go with a quick and dirty analysis here.
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    log_timed_message "ERROR:  Unable to open credentials file $credentials_file ($!)";
	    return undef;
	}
	while (my $line = <CREDENTIALS>) {
	    if ($line =~ m{<connection-url>jdbc:mysql://(\w+):(\d+)/(\w+)\?[^<]*</connection-url>}) {
		$db_host = $1;
		$db_port = $2;
		$db_name = $3;
	    }
	    if ($line =~ m{<user-name>(\S+)</user-name>}) {
		$db_user = $1;
	    }
	    if ($line =~ m{<password>(\S+)</password>}) {
		$db_pass = $1;
	    }
	}
	close CREDENTIALS;
    }
    elsif ($credentials_type eq 'enterprise-properties') {
	# FIX THIS:  Look to see whether we can and should be using TypedConfig for all
	# reading, interpretation, and validation of the credentials file, including the
	# handling of name-component subkeys (which I'm not sure TypedConfig will handle).
	# Also look to see if we could take advantange of its features for defining arrays
	# of instances, to support such things as multiple Cacti servers.  Bear in mind
	# how the present construction handles multiple levels of indirection within the
	# credentials file.
	my %config = ();
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    log_timed_message "ERROR:  Unable to open credentials file $credentials_file ($!)";
	    return undef;
	}
	while (my $line = <CREDENTIALS>) {
	    chomp $line;
	    if ( $line =~ /^\s*([^#]\S*)\s*=\s*(\S+)\s*$/ ) {
		$config{$1} = $2;
	    }
	}
	close CREDENTIALS; 

	$db_host = config_value(\%config, "$db_alias.database.host");
	$db_port = config_value(\%config, "$db_alias.database.port");
	$db_name = config_value(\%config, "$db_alias.database_name");
	$db_user = config_value(\%config, "$db_alias.database_user");
	$db_pass = config_value(\%config, "$db_alias.database_password");
    }
    else {
        log_timed_message "ERROR:  Invalid credentials type \"$credentials_type\" for the \"$database_name\" database.";
        return undef;
    }

    # We need to work around a special case embedded within the MySQL client libraries,
    # and perhaps the server itself.  If the host is localhost, then it will generally
    # ignore the supplied port and instead try to connect using a local named pipe.
    # FIX THIS:  The path to this pipe should be drawn from /usr/local/groundwork/mysql/my.cnf
    # or some similar location, perhaps on a per-database basis, instead of being hardcoded
    # here. But that would mean we would need to parse that file and pick out the
    # "socket=/usr/local/groundwork/mysql/tmp/mysql.sock" option within the [client]
    # section.  At the moment, we don't want to go to that much trouble.
    # FIX THIS:  How does Monarch handle this?  Answer:  It doesn't.  How it manages
    # to avoid this problem is currently a mystery, as it does not specify either a
    # port or a mysql_socket option when it connects.  Perhaps we have installed more
    # Perl packages in the GroundWork copy of Perl that somehow solve the problem.
    # PROBABLE REAL ANSWER:  See GWMON-8538; probably we need to fix our copy of
    # /usr/local/groundwork/mysql/bin/mysql_config to return the correct result for
    # the --socket option, so the DBD library gets built pointing to the right place.
    $db_sock = $mysql_socket if $db_host eq 'localhost';

    if ( !defined($db_host)
      || !defined($db_port)
      || !defined($db_name)
      || !defined($db_user)
      || !defined($db_pass) ) {
        log_timed_message "ERROR:  Cannot find \"$database_name\" database access parameters.";
        return undef;
    }

    my %credentials = ();
    $credentials{db_host} = $db_host;
    $credentials{db_port} = $db_port;
    $credentials{db_sock} = $db_sock;
    $credentials{db_name} = $db_name;
    $credentials{db_user} = $db_user;
    $credentials{db_pass} = $db_pass;
    return \%credentials;
}

# Make a database credentials file we can refer to in mysql and mysqldump commands.
sub db_credentials_file {
    my $credentials = shift;

    my $db_name = $credentials->{db_name};
    my $db_user = $credentials->{db_user};
    my $db_pass = $credentials->{db_pass};

    # We need to deal with the database credentials by establishing a temporary credentials file
    # and protecting it, rather than by passing credentials (in particular the password) on the
    # command line.  We use the following as a simple model.  We need to call mysqldump.bin
    # instead of mysqldump and mysql.bin instead of mysql in order to force --defaults-extra-file
    # as an initial argument onto the front of the command-line options.

    #   my_local_cnf=`mktemp -q /var/tmp/my_local.cnf.XXXXXXXXXX` || {
    #       echo "Cannot create a temporary file -- aborting!"
    #       exit 1  
    #   }
    #   trap "/bin/rm -f $my_local_cnf;" EXIT
    #   touch $my_local_cnf
    #   chmod 600 $my_local_cnf
    #   cat > $my_local_cnf << eof
    #   [mysql]
    #   user=$db_user
    #   password=$db_pass
    #   eof

    # FIX LATER:  Possibly use File::Temp to create the temporary file, both for
    # security reasons and for automated cleanup under exception conditions.

    my $db_credentials_file_path = "$db_credentials_file_base/$db_name.cnf";
    my $temporary_file_path      = "$db_credentials_file_path.tmp";
    my $temp_file                = undef;
    if (!open($temp_file, '>', $temporary_file_path)) {
	log_timed_message "FATAL:  Cannot create credentials file \"$temporary_file_path\" ($!).";
	return undef;
    }
    # We'd like to use the file handle on the chmod() call instead of the filename, but that
    # depends on Perl recognizing OS support for the fchmod() system call, which is apparently
    # not the case for Perl v5.8.5 under RHEL4.4 (at least).  Later releases, such as Perl v5.8.8
    # under CentOS 5.3, do support this, but we want to be portable across platforms.
    my $count = chmod(0600, $temporary_file_path);
    if ($count != 1) {
	log_timed_message "FATAL:  Cannot change mode of credentials file \"$temporary_file_path\" ($!).";
        close $temp_file;
	unlink $temporary_file_path;
	return undef;
    }
    print $temp_file "[mysqldump]\n";
    print $temp_file "user=$db_user\n";
    print $temp_file "password=$db_pass\n";
    print $temp_file "\n";
    print $temp_file "[mysql]\n";
    print $temp_file "user=$db_user\n";
    print $temp_file "password=$db_pass\n";
    if (not close($temp_file)) {
	log_timed_message "FATAL:  Cannot close credentials file \"$temporary_file_path\" ($!).";
	unlink $temporary_file_path;
	return undef;
    }
    if (not rename($temporary_file_path, $db_credentials_file_path)) {
	log_timed_message "FATAL:  Cannot rename credentials file \"$temporary_file_path\" ($!).";
	unlink $temporary_file_path, $db_credentials_file_path;
	return undef;
    }

    return $db_credentials_file_path;
}

# Make a list of included/excluded tables for replication, given the config-file
# specification data and probing the database for table name pattern matching.
sub db_dump_command {
    my $credentials      = shift;
    my $credentials_path = shift;
    my $include_tables   = shift;
    my $exclude_tables   = shift;

    my $db_host = $credentials->{db_host};
    my $db_port = $credentials->{db_port};
    my $db_sock = $credentials->{db_sock};
    my $db_name = $credentials->{db_name};
    my $db_user = $credentials->{db_user};
    my $db_pass = $credentials->{db_pass};
    my $db_sock_option = $db_sock ? "mysql_socket=$db_sock;" : '';

    my $dbh = undef;
    my $sth;

    # We suppress errors reported straight out of DBI->connect() because we will capture and report them ourselves.
    # FIX THIS:  Figure out how Monarch manages not to make the $db_sock_option adjustment.  See GWMON-8538.
    $dbh = DBI->connect( "DBI:mysql:database=$db_name;host=$db_host;port=$db_port;$db_sock_option", $db_user, $db_pass,
    # $dbh = DBI->connect( "DBI:mysql:database=$db_name;host=$db_host;port=$db_port", $db_user, $db_pass,
    # $dbh = DBI->connect( "DBI:mysql:database=$db_name;host=$db_host", $db_user, $db_pass,
    # $dbh = DBI->connect( "DBI:mysql:$db_name;$db_host", $db_user, $db_pass,
      { 'RaiseError' => 0, 'PrintError' => 0 } );
    if (!$dbh) {
	my $errstr = $DBI::errstr;
	log_timed_message "ERROR:  Cannot connect to the \"$db_name\" database ($errstr).";
	return undef;
    }

    my @tables = ();

    # FIX LATER:  protect against possible SQL injection attacks here

    # Common special cases, for simplicity:
    if (!ref($include_tables) && $include_tables eq '%') {
	if (defined($exclude_tables)) {
	    # The entire database, except for a few tables.
	    # log_timed_message "DEBUG:  Special Case #1:  whole database with a few exclusions";
	    if (not ref $exclude_tables) {
		$exclude_tables = [ $exclude_tables ];
	    }
	    foreach my $pattern (@$exclude_tables) {
		$sth = $dbh->prepare("show tables like '$pattern'");
		$sth->execute;
		while ( my @values = $sth->fetchrow_array() ) {
		    push @tables, "--ignore-table=$db_name.$values[0]";
		}
		$sth->finish;
	    }
	}
	else {
	    # The entire database, which is the default.
	    # log_timed_message "DEBUG:  Special Case #2:  whole database";
	}
    }
    elsif (ref($include_tables) && !defined($exclude_tables)) {
	# Just a few particular tables.
	# log_timed_message "DEBUG:  Special Case #3:  just a few particular tables";
	foreach my $pattern (@$include_tables) {
	    $sth = $dbh->prepare("show tables like '$pattern'");
	    $sth->execute;
	    while ( my @values = $sth->fetchrow_array() ) {
		push @tables, $values[0];
	    }
	    $sth->finish;
	}
    }
    else {
	# This is the full-bore complexity that will handle any case, including
	# those above, though perhaps in a more general and uglier way.
	# log_timed_message "DEBUG:  General Case:  any combination of included and excluded tables";
	if (not ref $include_tables) {
	    $include_tables = [ $include_tables ];
	}
	if (defined($exclude_tables) && not ref $exclude_tables) {
	    $exclude_tables = [ $exclude_tables ];
	}

	my %include_tables = ();

	foreach my $pattern (@$include_tables) {
	    $sth = $dbh->prepare("show tables like '$pattern'");
	    $sth->execute;
	    while ( my @values = $sth->fetchrow_array() ) {
		$include_tables{ $values[0] } = 1;
	    }
	    $sth->finish;
	}

	if (defined $exclude_tables) {
	    foreach my $pattern (@$exclude_tables) {
		$sth = $dbh->prepare("show tables like '$pattern'");
		$sth->execute;
		while ( my @values = $sth->fetchrow_array() ) {
		    delete $include_tables{ $values[0] };
		}
		$sth->finish;
	    }
	}

	if (keys %include_tables == 0) {
	    log_timed_message "ERROR:  No tables are specified for the \"$db_name\" database dump.";
	    return undef;
	}

	push @tables, sort keys %include_tables;
    }

    $dbh->disconnect(); 

    # The mysqldump command we construct here goes beyond just dumping the database
    # (or just certain tables thereof).  We wrap long lines in sensible places to
    # make the file usefully editable and to make possible running a diff on a pair
    # of dumps to see what is different between them.
    #
    # The transformations we make do the following:
    # (1) Introduce newlines between successive rows to be inserted into tables. 
    #     This is a huge win for readability.
    # (2) Introduce a newline before the first row to be inserted into a table,
    #     for consistent alignment with the rest of the rows.
    # (3) Drop the table-level AUTO_INCREMENT value which mysqldump produces as a
    #     means of reserving initial sequence values in the table for use with the
    #     rows which are inserted immediately after the table is created.  We delete
    #     this specification because it is fragile:  it is unlikely that someone who
    #     is maintaining the output file manually, by adding new rows to the table
    #     in the INSERT INTO section for that table, will think to update this initial 
    #     table-level AUTO_INCREMENT value, leaving it inconsistent with the rest of 
    #     the file.  Dropping this value should have no untoward effect; all it will 
    #     do is cause the same value to be computed dynamically as the initial set of
    #     rows is inserted.

    # The $user_option and $pass_option are deprecated; we pass credentials via a safe file instead.
    my $host_option = "--host=$db_host";
    my $port_option = $db_sock ? '' : "--port=$db_port";
    my $sock_option = $db_sock ? "--socket=$mysql_socket" : '';
    my $user_option = "--user=$db_user";
    my $pass_option = "--password=$db_pass";

    # The --net_buffer_length=512K option should already be set in the [mysqldump] secion of the
    # /usr/local/groundwork/mysql/my.cnf file; we just give it explicitly here for safety's sake,
    # so we can do our editing without fear of exceeding the buffer length accepted when the file
    # is read back in.
    my $mysql_dump_command = "$mysqldump_path --defaults-extra-file=$credentials_path "
      . "$host_option $port_option $sock_option --net_buffer_length=512K $db_name "
      . join (' ', @tables)
      . " | sed -e 's/),(/),\\n\\t(/g' -e 's/ VALUES (/ VALUES\\n\\t(/' -e 's/AUTO_INCREMENT=[0-9]\+ //'";

    return $mysql_dump_command;
}

sub db_load_command {
    my $credentials      = shift;
    my $credentials_path = shift;

    my $db_host = $credentials->{db_host};
    my $db_port = $credentials->{db_port};
    my $db_sock = $credentials->{db_sock};
    my $db_name = $credentials->{db_name};
    my $db_user = $credentials->{db_user};
    my $db_pass = $credentials->{db_pass};
    my $db_sock_option = $db_sock ? "mysql_socket=$db_sock;" : '';

    # The $user_option and $pass_option are deprecated; we pass credentials via a safe file instead.
    my $host_option = "--host=$db_host";
    my $port_option = $db_sock ? '' : "--port=$db_port";
    my $sock_option = $db_sock ? "--socket=$mysql_socket" : '';
    my $user_option = "--user=$db_user";
    my $pass_option = "--password=$db_pass";

    my $mysql_load_command = "$mysql_path --defaults-extra-file=$credentials_path $host_option $port_option $sock_option $db_name";

    return $mysql_load_command;
}

1;
