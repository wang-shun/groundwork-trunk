#!/usr/local/groundwork/perl/bin/perl -w --

# Master database-migration script to convert from a MySQL-based
# GroundWork Monitor installation to a PostgreSQL-based installation.

# Copyright 2012 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

use strict;

use Fcntl;
use POSIX qw(strftime :errno_h);
use IO::Handle;  # provides HANDLE->flush() and HANDLE->autoflush()

# ================================================================
# CPAN Packages
# ================================================================

# The following code is taken directly from the Term::ReadPassword
# module (version 0.11) on CPAN.  We fold it in here directly
# because we don't have that module already included in the Perl
# we supply with GroundWork Monitor, and because the GW installer
# won't be applying any new Perl modules before this script is to
# be run.  This way, this master migration script can be run as a
# standalone script.

package Term::ReadPassword;

use strict;
use Term::ReadLine;
use POSIX qw(:termios_h);
my %CC_FIELDS = (
	VEOF => VEOF,
	VEOL => VEOL,
	VERASE => VERASE,
	VINTR => VINTR,
	VKILL => VKILL,
	VQUIT => VQUIT,
	VSUSP => VSUSP,
	VSTART => VSTART,
	VSTOP => VSTOP,
	VMIN => VMIN,
	VTIME => VTIME,
    );

use vars qw(
    $VERSION @ISA @EXPORT @EXPORT_OK
    $ALLOW_STDIN %SPECIAL $SUPPRESS_NEWLINE $INPUT_LIMIT
    $USE_STARS $STAR_STRING $UNSTAR_STRING
);

require Exporter;

@ISA = qw(Exporter);
@EXPORT = qw(
	read_password 
);
$VERSION = '0.11';

# The special characters in the input stream
%SPECIAL = (
    "\x03"	=> 'INT',	# Control-C, Interrupt
    "\x15"	=> 'NAK',	# Control-U, NAK (clear buffer)
    "\x08"	=> 'DEL',	# Backspace
    "\x7f"	=> 'DEL',	# Delete
    "\x0d"	=> 'ENT',	# CR, Enter
    "\x0a"	=> 'ENT',	# LF, Enter
);

# The maximum amount of data for the input buffer to hold
$INPUT_LIMIT = 1000;

sub read_password {
    my($prompt, $idle_limit, $interruptable) = @_;
    $prompt = '' unless defined $prompt;
    $idle_limit = 0 unless defined $idle_limit;
    $interruptable = 0 unless defined $interruptable;

    # Let's open the TTY (rather than STDIN) if we can
    local(*TTY, *TTYOUT);
    my($in, $out) = Term::ReadLine->findConsole;
    die "No console available" unless $in;
    if (open TTY, "+<$in") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	open TTY, "<&STDIN"
	    or die "Can't re-open STDIN: $!";
    } else {
	die "Can't open '$in' read/write: $!";
    }

    # And let's send the output to the TTY as well
    if (open TTYOUT, ">>$out") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	# Well, let's allow STDOUT as well
	open TTYOUT, ">>&STDOUT"
	    or die "Can't re-open STDOUT: $!";
    } else {
	die "Can't open '$out' for output: $!";
    }

    # Don't buffer it!
    select( (select(TTYOUT), $|=1)[0] );
    print TTYOUT $prompt;

    # Okay, now remember where everything was, so we can put it back when
    # we're done 
    my $fd_tty = fileno(TTY);
    my $term = POSIX::Termios->new();
    $term->getattr($fd_tty);
    my $original_flags = $term->getlflag();
    my %original_cc;
    for my $field_name (keys %CC_FIELDS) {
	$original_cc{$field_name} = $term->getcc($CC_FIELDS{$field_name});
    }

    # What makes this setup different from the ordinary?
    # No keyboard-generated signals, no echoing, no canonical input
    # processing (like backspace handling)
    my $flags = $original_flags & ~(ISIG | ECHO | ICANON);
    $term->setlflag($flags);
    if ($idle_limit) {
	# $idle_limit is in seconds, so multiply by ten
	$term->setcc(VTIME, 10 * $idle_limit);
	# Continue running the program after that time, even if there
	# weren't any characters typed
	$term->setcc(VMIN, 0);
    } else {
	# No time limit, but...
	$term->setcc(VTIME, 0);
	# Continue as soon as one character has been struck
	$term->setcc(VMIN, 1);
    }

    # Optionally echo stars in place of password characters. The 
    # $unstar_string uses backspace characters.
    my $star_string = $USE_STARS ? ($STAR_STRING || '*') : '';
    my $unstar_string = $USE_STARS ? ($UNSTAR_STRING || "\b*\b \b") : '';

    # If there's anything already buffered, we should throw it out. This
    # is to discourage users from typing their password before they see
    # the prompt, since their keystrokes may be echoing on the screen. 
    #
    # So this statement supposedly makes sure the prompt goes out, the
    # unread input buffer is discarded, and _then_ the changes take
    # effect. Thus, everything they typed ahead is (probably) echoed.
    $term->setattr($fd_tty, TCSAFLUSH);

    my $input = '';
    my $return_value;
KEYSTROKE:
    while (1) {
	my $new_keys = '';
	my $count = sysread(TTY, $new_keys, 99);
	# We're here, so either the idle_limit expired, or the user typed
	# something.
	if ($count) {
	    for my $new_key (split //, $new_keys) {
		if (my $meaning = $SPECIAL{$new_key}) {
		    if ($meaning eq 'ENT') {
			# Enter/return key
			# Return what we have so far
			$return_value = $input;
			last KEYSTROKE;
		    } elsif ($meaning eq 'DEL') {
			# Delete/backspace key
			# Take back one char, if possible
			if (length $input) {
			    $input = substr $input, 0, length($input)-1;
			    print TTYOUT $unstar_string;
			}
		    } elsif ($meaning eq 'NAK') {
			# Control-U (NAK)
			# Clear what we have read so far
			for (1..length $input) {
			    print TTYOUT $unstar_string;
			}
			$input = '';
		    } elsif ($interruptable and $meaning eq 'INT') {
			# Breaking out of the program
			# Return early
			last KEYSTROKE;
		    } else {
			# Just an ordinary keystroke
			$input .= $new_key;
			print TTYOUT $star_string;
		    }
		} else {
		    # Not special
		    $input .= $new_key;
		    print TTYOUT $star_string;
		}
	    }
	    # Just in case someone sends a lot of data
	    $input = substr($input, 0, $INPUT_LIMIT)
		if length($input) > $INPUT_LIMIT;
	} else {
	    # No count, so something went wrong. Assume timeout.
	    # Return early
	    last KEYSTROKE;
	}
    }

    # Done with waiting for input. Let's not leave the cursor sitting
    # there, after the prompt.
    print TTYOUT "\n" unless $SUPPRESS_NEWLINE;

    # Let's put everything back where we found it.
    $term->setlflag($original_flags);
    while (my($field, $value) = each %original_cc) {
	$term->setcc($CC_FIELDS{$field}, $value);
    }
    $term->setattr($fd_tty, TCSAFLUSH);
    close(TTY);
    close(TTYOUT);
    $return_value;
}

package main;

# ================================================================
# Global Configuration Variables
# ================================================================

my $gwpath    = "/usr/local/groundwork";
my $mysql     = "$gwpath/mysql/bin/mysql.bin";
my $log_base  = "/tmp/migration";
my $semaphore = "$gwpath/tmp/OK_to_upgrade_to_PostgreSQL";

# Enable display of asterisks when entering passwords.
$Term::ReadPassword::USE_STARS = 1;

# ================================================================
# Global Operational Variables
# ================================================================

my %dbhost = ();
my %dbport = ();
my %dbuser = ();
my %dbpass = ();

my @my_local_config_files = ();
END {
    remove_mysql_credentials($_) foreach @my_local_config_files;
}

my $just_check_for_data_problems      = 0;
my $just_run_data_checks_and_upgrades = 0;

# "2012_01_31" or equivalent
my $date = strftime('%Y_%m_%d', localtime);

# "2012_02_09.10_31_27" or equivalent
my $datetime = strftime('%Y_%m_%d.%H_%M_%S', localtime);

my $logfile = "$log_base.$datetime.log";

my $force_bad_exit_code = 0;

# ================================================================
# Supporting subroutines
# ================================================================

sub print_usage {
    print "usage:  master_migration_to_pg.pl {-c|-u|-m}\n";
    print "where:  -c means just check for data problems; don't migrate\n";
    print "        -u means just run data checks and database-content upgrades;\n";
    print "            don't migrate the data to PostgreSQL databases\n";
    print "        -m means run the full migration:  data checks, database\n";
    print "            upgrades to conventions used in the last MySQL-based\n";
    print "            release, and conversion from MySQL to PostgreSQL\n";
}

sub exit_on_signal {
    my $sig = shift;
    print "\nFATAL:  Received SIG$sig signal; aborting!\n";
    exit 1;
}

# There's a lot of overhead here for just one message,
# so if you have multiple lines to print, you probably
# want to include them all in the same invocation.
sub print_tty {
    my @message = @_;
    local *TTYOUT;
    my ( $in, $out ) = Term::ReadLine->findConsole;
    if ( $out && open TTYOUT, ">>$out" ) {
	## Don't buffer it!
	select( ( select(TTYOUT), $| = 1 )[0] );
	print TTYOUT @message;
	close(TTYOUT);
    }
}

sub get_value {
    my $default     = shift;
    my $description = shift;
    my $is_password = shift;
    my $is_question = shift;

    # Yield the processor very briefly, to allow the Perl i/o layer (perhaps)
    # and the operating system and/or hypervisor a moment to take over and get
    # any buffered output actually sent to the receiving terminal, pseudo-terminal,
    # or socket.  Without this, we can sometimes get the prompt we're about to
    # produce be printed before output to STDOUT that was already queued up before
    # we got to this point.  We have only seen that to be an issue on a VM guest,
    # not on a bare-metal machine, but it was fairly reproducible there.  If we
    # ever see that behaviour again, perhaps the best solution will be to extend
    # this brief suspension to allow more time for all the queued output to appear.
    select undef, undef, undef, 0.01; # sleeps for 0.01 of a second

    my $ALLOW_STDIN   = 0;
    my $entered_value = '';
    my $repeat_value  = '';
    my $prompt_prefix = $is_question ? '' : 'Enter the ';

    # Let's open the TTYIN (rather than STDIN) if we can
    local(*TTYIN, *TTYOUT);
    my($in, $out) = Term::ReadLine->findConsole;
    die "No console available" unless $in;

    if (open TTYIN, "+<$in") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	open TTYIN, "<&STDIN"
	    or die "Can't re-open STDIN: $!";
    } else {
	die "Can't open '$in' read/write: $!";
    }

    # And let's send the output to the TTY as well
    if (open TTYOUT, ">>$out") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	# Well, let's allow STDOUT as well
	open TTYOUT, ">>&STDOUT"
	    or die "Can't re-open STDOUT: $!";
    } else {
	die "Can't open '$out' for output: $!";
    }

    # Don't buffer it!
    select( (select(TTYOUT), $|=1)[0] );

    while (1) {
	if ($is_password) {
	    # We apply a 60-second timeout between password characters mostly so that,
	    # if the user takes a very long time to type in the password, there is less
	    # chance that our earlier test to see whether the system was running is now
	    # no longer valid.
	    $entered_value = Term::ReadPassword::read_password("$prompt_prefix$description: ", 60, 1);
	    last if not defined $entered_value;
	}
	else {
	    print TTYOUT "$prompt_prefix$description [$default]: ";
	}

	if ($is_password) {
	    $repeat_value = Term::ReadPassword::read_password("Re-enter the $description: ", 60, 1);
	    if (not defined $repeat_value) {
		$entered_value = undef;
		last;
	    }
	    last if $repeat_value eq $entered_value;
	    print TTYOUT "ERROR:  Password mismatch.  Please try again.\n";
	}
	else {
	    $entered_value = readline TTYIN;
	    if (defined $entered_value) {
		chomp $entered_value;
		$entered_value =~ s/^\s+|\s+$//g;
		$entered_value = $default if $entered_value eq '';
		last;
	    }
	    print TTYOUT "\nInvalid input.  Please try again.\n";
	}
    }

    close(TTYIN);
    close(TTYOUT);
    return $entered_value;
}

sub save_mysql_credentials {
    my $host = shift;
    my $port = shift;
    my $user = shift;
    my $pass = shift;

    # This selection ought to be configured in the db.properties file, but
    # until it is, we need to compute this ourselves.  The mysql client
    # program is configured to do this for us in its my.cnf file, but we
    # cannot use that because we need to use the --defaults-extra-file
    # argument, which is not supported by the Bitrock "mysql" wrapper.
    my $port_or_sock = ( $host eq 'localhost' ) ? "--socket=/usr/local/groundwork/mysql/tmp/mysql.sock" : "--port=$port";

    # FIX LATER:  there are improved ways to name a temporary file
    # that provide even better security
    my $my_local_cnf = "/usr/local/groundwork/tmp/my_local.cnf.$$";
    if (-e $my_local_cnf) {
	unlink $my_local_cnf or die "FATAL:  Cannot remove MySQL credentials file: $!\n";
    }
    push @my_local_config_files, $my_local_cnf;
    sysopen (CONF, $my_local_cnf, O_WRONLY | O_NOFOLLOW | O_CREAT | O_EXCL, 0600) or die "FATAL:  Cannot open MySQL credentials file: $!\n";
    chmod (0600, $my_local_cnf) or die "FATAL:  Cannot set permissions on MySQL credentials file: $!\n";
    print CONF "[mysql]\n";
    print CONF "user=$user\n";
    print CONF "password=$pass\n";
    close CONF;

    return $my_local_cnf, $port_or_sock;
    }

sub remove_mysql_credentials {
    my $my_local_cnf = shift;
    unlink $my_local_cnf if defined $my_local_cnf;
}

sub run_sql_script {
    my $dbname     = shift;
    my $sql_script = shift;

    my ($my_local_cnf, $port_or_sock) = save_mysql_credentials($dbhost{$dbname}, $dbport{$dbname}, $dbuser{$dbname}, $dbpass{$dbname});
    my $outcome = system("$mysql --defaults-extra-file=$my_local_cnf -h $dbhost{$dbname} $port_or_sock $dbname < $sql_script");
    remove_mysql_credentials($my_local_cnf);

    return !$outcome;
}

# ================================================================
# Database access routines
# ================================================================

# ----------------------------------------------------------------
# Global configuration variables.
# ----------------------------------------------------------------

my $mysql_socket = '/usr/local/groundwork/mysql/tmp/mysql.sock';

# Some of our databases are accessed via aliases in the config files,
# and so we must find out what alias to use.
my %credentials_file_db_alias = (
    monarch          => 'monarch',
    GWCollageDB      => 'collage',
    dashboard        => 'insightreports',
    jbossportal      => 'jbossportal',
    cacti            => 'nms.cacti.cacti_main',
    nedi             => 'nms.nedi.nedi_main',
    ganglia          => 'ganglia',
    alertsite        => 'alertsite',
    HelpDeskBridgeDB => 'bridgeDB',
);

# Where to find certain database access credentials.  These groupings of specifications are
# checked in the order given, for each individual database, to provide flexibility in how the
# credentials are searched for.  You can add additional top-level groupings as needed.
#
# For Cacti and Nedi, we check first if the old unbundled NMS package is installed, primarily
# because nedi.conf in the newer add-on package might only contain dummy data; we only check
# the newer files if the older files don't yield what we're looking for.  (I'm not sure this
# is the sensible ordering in the case of Cacti.)  In contrast, for Ganglia, we check first if
# the config file for a newer version (>= 6.0.0) of the Ganglia Integration Module is installed,
# then fall back to looking for the config file for an older version.  This way, we grab the
# most-current ganglia database access data.
my @database_credentials = (
    {
	cacti => {
	    credentials_file => '/usr/local/groundwork/enterprise/config/enterprise.properties',
	    credentials_type => 'enterprise-properties'
	},
	nedi => {
	    credentials_file => '/usr/local/groundwork/enterprise/config/enterprise.properties',
	    credentials_type => 'enterprise-properties'
	},
	ganglia => {
	    credentials_file => '/usr/local/groundwork/config/GangliaConfigAdmin.conf',
	    credentials_type => 'ganglia-properties'
	}
    },
    {
	GWCollageDB => {
	    credentials_file => '/usr/local/groundwork/config/db.properties',
	    credentials_type => 'db-properties'
	},
	dashboard => {
	    credentials_file => '/usr/local/groundwork/config/db.properties',
	    credentials_type => 'db-properties'
	},
	jbossportal => {
	    credentials_file => '/usr/local/groundwork/foundation/container/webapps/jboss/portal-ds.xml',
	    credentials_type => 'portal-ds-xml'
	},
	monarch => {
	    credentials_file => '/usr/local/groundwork/config/db.properties',
	    credentials_type => 'db-properties'
	},
	cacti => {
	    credentials_file => '/usr/local/groundwork/config/cacti.properties',
	    credentials_type => 'cacti-properties'
	},
	nedi => {
	    credentials_file => '/usr/local/groundwork/nedi/nedi.conf',
	    credentials_type => 'nedi-properties'
	},
	ganglia => {
	    credentials_file => '/usr/local/groundwork/etc/GangliaConfigAdmin.conf',
	    credentials_type => 'ganglia-properties'
	},
	alertsite => {
	    credentials_file => '/usr/local/groundwork/config/db.properties',
	    credentials_type => 'db-properties'
	},
	HelpDeskBridgeDB => {
	    credentials_file => '/usr/local/groundwork/config/db.properties',
	    credentials_type => 'db-properties'
	}
    }
);

# ----------------------------------------------------------------
# Supporting subroutines.
# ----------------------------------------------------------------

# This internal routine allows indirection at each key component level.
# Normally, application code does not call this with a subkey or recursion level;
# that argument is only used for recursive calls.
sub config_value {
    my $config = shift;
    my $key    = shift;
    my $subkey = shift;
    my $level  = shift || 0;

    if (++$level > 100) {
	my $fullkey = (defined $subkey) ? "$key.$subkey" : $key;
	print "ERROR:  Too many levels of indirection found in config file when searching for $fullkey\n";
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
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
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
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
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
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
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
    elsif ($credentials_type eq 'cacti-properties') {
	# FIX THIS:  Look to see whether we can and should be using TypedConfig for all
	# reading, interpretation, and validation of the credentials file, including the
	# handling of name-component subkeys (which I'm not sure TypedConfig will handle).
	# Also look to see if we could take advantange of its features for defining arrays
	# of instances, to support such things as multiple Cacti servers.  Bear in mind
	# how the present construction handles multiple levels of indirection within the
	# credentials file.
	my %config = ();
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
	    return undef;
	}
	while (my $line = <CREDENTIALS>) {
	    chomp $line;
	    if ( $line =~ /^\s*([^#]\S*)\s*=\s*(\S+)\s*$/ ) {
		$config{$1} = $2;
	    }
	}
	close CREDENTIALS; 

	$db_host = config_value(\%config, "$database_name.1.dbhost");
	$db_port = config_value(\%config, "$database_name.1.dbport");
	$db_name = config_value(\%config, "$database_name.1.dbname");
	$db_user = config_value(\%config, "$database_name.1.dbuser");
	$db_pass = config_value(\%config, "$database_name.1.dbpass");
    }
    elsif ($credentials_type eq 'nedi-properties') {
	# FIX THIS:  Look to see whether we can and should be using TypedConfig for all
	# reading, interpretation, and validation of the credentials file, including the
	# handling of name-component subkeys (which I'm not sure TypedConfig will handle).
	# Also look to see if we could take advantange of its features for defining arrays
	# of instances, to support such things as multiple Cacti servers.  Bear in mind
	# how the present construction handles multiple levels of indirection within the
	# credentials file.
	my %config = ();
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
	    return undef;
	}
	while (my $line = <CREDENTIALS>) {
	    chomp $line;
	    if ( $line =~ /^\s*([^#;]\S*)\s+(\S+)\s*$/ ) {
		$config{$1} = $2;
	    }
	}
	close CREDENTIALS; 

	$db_host = config_value(\%config, "dbhost");
	$db_port = config_value(\%config, "dbport") || 3306;
	$db_name = config_value(\%config, "dbname");
	$db_user = config_value(\%config, "dbuser");
	$db_pass = config_value(\%config, "dbpass");
    }
    elsif ($credentials_type eq 'ganglia-properties') {
	# FIX THIS:  Look to see whether we can and should be using TypedConfig for all
	# reading, interpretation, and validation of the credentials file, including the
	# handling of name-component subkeys (which I'm not sure TypedConfig will handle).
	# Also look to see if we could take advantange of its features for defining arrays
	# of instances, to support such things as multiple Cacti servers.  Bear in mind
	# how the present construction handles multiple levels of indirection within the
	# credentials file.
	my %config = ();
	if ( !open(CREDENTIALS, '<', $credentials_file) ) {
	    print "ERROR:  Unable to open credentials file $credentials_file ($!)\n";
	    return undef;
	}
	my $option;
	my $value;
	while (my $line = <CREDENTIALS>) {
	    chomp $line;
	    if ( $line =~ /^\s*([^#]\S*)\s*=\s*(\S+)\s*$/ ) {
		$option = $1;
		$value  = $2;
		if ($value =~ /^"([^"]*)"$/) {
		    $value = $1;
		}
		$config{$option} = $value;
	    }
	}
	close CREDENTIALS; 

	# We allow for both new-format (Ganglia Integration Module 6.0.0 and above)
	# and old-format configuration files to be read.
	$db_host = config_value(\%config, "ganglia_dbhost") || config_value(\%config, "DatabaseHost");
	$db_port = config_value(\%config, "ganglia_dbport") || config_value(\%config, "DatabasePort") || 3306;
	$db_name = config_value(\%config, "ganglia_dbname") || config_value(\%config, "DatabaseName");
	$db_user = config_value(\%config, "ganglia_dbuser") || config_value(\%config, "DatabaseUser");

	# We use special handling for the password, which might be defined as the ganglia_dbpass option
	# with an empty-string value that we don't want to confuse with a missing value.  For the other
	# fields above, an empty value is A Bad Thing, so the fallback we use there is adequate.
	$db_pass = config_value(\%config, "ganglia_dbpass");
	$db_pass = config_value(\%config, "DatabasePass") if not defined $db_pass;
    }
    else {
	print "ERROR:  Invalid credentials type \"$credentials_type\" for the \"$database_name\" database.\n";
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
    $db_sock = $mysql_socket if defined($db_host) && $db_host eq 'localhost';

    if ( !defined($db_host)
      || !defined($db_port)
      || !defined($db_name)
      || !defined($db_user)
      || !defined($db_pass) ) {
	print "WARNING:  Cannot find \"$database_name\" database access parameters.\n";
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

# ================================================================
# Main program
# ================================================================

END {
    STDOUT->flush();
    ## Give up the CPU long enough for a child tee to read all its input and flush it.
    select undef, undef, undef, 0.01;
    print_tty "\nMaster Migration log file is:  $logfile\n";
    if ( ( $? || $force_bad_exit_code ) && !-t STDOUT ) {
	## Let's try to get a simple failure message out on TTYOUT,
	## since STDOUT has been redirected away from the terminal.
	if ($?) {
	    print_tty "FATAL:  master_migration_to_pg.pl failed; see the log file.\n";
	}
	else {
	    print_tty "WARNING:  master_migration_to_pg.pl had warnings; see the log file.\n";
	    ## Force a failure exit code here, per $force_bad_exit_code.
	    $? = 1;
	}
    }
}

# Use a "tee" as a child process to capture all output into our log file.
# We ignore SIGINT in the child process so that it stays alive long enough
# to capture all our dying output if we are ourselves terminated with SIGINT.
do { local $SIG{INT} = 'IGNORE'; open( STDOUT, '|-', "/usr/bin/tee -a $logfile" ) }
  or die "FATAL:  Cannot exec \"tee\": $!\n";

# We flush output as quickly as possible, to prevent any race conditions with
# parts of the i/o handling that fiddle with TTY in/out file descriptors.
# This is just a precaution; we don't really have evidence that it has any
# practical effect.
STDOUT->autoflush(1);

# We re-open the STDERR stream as a duplicate of the STDOUT stream, to
# capture any output written to STDERR (say, by failed system() calls,
# or by subsidiary scripts) if STDOUT was originally redirected to a file.
if (! open (STDERR, '>>&STDOUT')) {
    print "ERROR:  Can't redirect STDERR to STDOUT: $!\n";
}
else {
    # Autoflush the error output on every single write, to avoid problems
    # with block i/o and badly interleaved output lines on STDOUT and STDERR.
    STDERR->autoflush(1);
}

# First, let the user know where to look for possible voluminous errors.
print_tty "Master Migration log file is:  $logfile\n\n";

print "=== Master Migration is starting up at ".(scalar localtime)." ===\n";

# We turn signals we might expect to encounter into controlled exits,
# to ensure that all our END blocks executed as planned.  (END blocks
# are skipped when uncaught signals cause the process to die.)
$SIG{HUP}  = \&exit_on_signal;
$SIG{INT}  = \&exit_on_signal;
$SIG{QUIT} = \&exit_on_signal;
$SIG{TERM} = \&exit_on_signal;
$SIG{PIPE} = \&exit_on_signal;

if (@ARGV != 1) {
    print_usage();
    exit 1;
}

foreach my $arg (@ARGV) {
    if ($arg eq '-c') {
	# "check":  Run database checks ONLY.  Look for inconsistencies.
	$just_check_for_data_problems = 1;
    }
    elsif ($arg eq '-u') {
	# "upgrade":  Run database checks, and if those all pass, upgrade
	# the database content to conventions used in the last MySQL-based
	# release.  This prepares the databases for conversion to PostgreSQL,
	# but does not actually convert them.
	$just_run_data_checks_and_upgrades = 1;
    }
    elsif ($arg eq '-m') {
	# "migrate":  Run data checks, upgrade, and migrate to PostgreSQL.
	# There is no option to record here; this is effectively the default
	# behavior.  We only insist on the user specifying an explicit "-m"
	# option to get this behavior because we don't want an unadorned
	# command to run the migration; that's considered a bit too dangerous.
	# The user should explicitly specify his/her intent before the script
	# takes any action to modify any of the databases.
    }
    else {
	print_usage();
	exit 1;
    }
}

if ( ! -x '/usr/local/groundwork/ctlscript.sh' ) {
    print "ERROR:  Cannot sense whether gwservices is still running;\n";
    print "        processing aborted!\n";
    exit 1;
}

my $running=`/usr/local/groundwork/ctlscript.sh status gwservices | egrep -v '(is|not|copies) running' | fgrep -c running`;

# We refuse to run if gwservices is running; if it were, it might
# be dynamically modifying some databases we want to convert.
chomp $running;
if ( $running ne '0' ) {
    print "ERROR:  gwservices is still running; processing aborted!\n";
    exit 1;
}

# We have decided that this script can run.  Given that, we need to remove
# any evidence that the script has run before, so the result of a previously
# successful run, perhaps some time awhile ago, is not confused with this run
# of the script, which might fail and leave the migration in some incomplete
# state.  But if the user mistakenly re-ran this master migration script and
# did not really intend to wipe out the evidence of the previous successful
# run, we need to give them a last chance to abort before we take irreversible
# actions.
if (-e $semaphore) {
    ## We found the semaphore file, so this must be a re-run.  In that case, we
    ## must ask the user whether it is okay to continue.  Stop right here, if not.
    (my $re_run_notice = <<'NOTICE') =~ s/^[ \t]+//gm;

	 ............................... NOTICE ...............................

	 The master migration has previously been run to completion.  If you
	 choose to re-run the migration, you must wait for the entire migration
	 to complete before you can finish the upgrade to the next release.
	 Depending on the size of your databases, this might take a considerable
	 amount of time.

	 Think carefully before you answer the following question; this is your
	 only chance to stop before evidence of the previous run is removed.

NOTICE
    print_tty $re_run_notice;
    my $keep_going = 'no';
    $keep_going = get_value( $keep_going, "Do you wish to re-run the migration? (yes/no)", 0, 1 );
    if ( $keep_going =~ /^y(es)?$/i ) {
	print "\nThe Master Migration script was previously run to completion,\n";
	print "and now will be re-run from the beginning.\n";
    }
    else {
	print "\nThis run of the Master Migration script has been manually aborted.\n";
	exit 1;
    }
}
unlink $semaphore;

# Pick up access credentials for each database of interest, from wherever they live in the system.
foreach my $datasource (qw(GWCollageDB dashboard monarch jbossportal cacti nedi ganglia alertsite HelpDeskBridgeDB)) {
    my $credentials_file = undef;
    my $credentials_type = undef;
    foreach my $credentials_group (@database_credentials) {
	$credentials_file = $credentials_group->{$datasource}{'credentials_file'};
	$credentials_type = $credentials_group->{$datasource}{'credentials_type'};
	last if defined($credentials_file) and -f $credentials_file;
    }
    if (!defined($credentials_file) or !-f $credentials_file) {
	print "NOTICE:  no credentials file found for the $datasource data source.\n";
	next;
    }
    my $credentials = db_credentials( $datasource, $credentials_file, $credentials_type );
    if (defined $credentials) {
	my      $dbname  = $credentials->{db_name};
	$dbhost{$dbname} = $credentials->{db_host};
	$dbport{$dbname} = $credentials->{db_port};
	$dbuser{$dbname} = $credentials->{db_user};
	$dbpass{$dbname} = $credentials->{db_pass};
	#                  $credentials->{db_sock};
    }
}

# Default values; may be overridden by user input.
my $mysql_root_host = $dbhost{monarch};
my $mysql_root_port = 3306;
my $mysql_root_user = 'root';
my $mysql_root_pass = '';

$mysql_root_host = get_value($mysql_root_host, "MySQL host machine");
$mysql_root_port = get_value($mysql_root_port, "MySQL port");
$mysql_root_user = get_value($mysql_root_user, "MySQL administrator username");
$mysql_root_pass = get_value($mysql_root_pass, "MySQL administrator password", 1);
if (!defined($mysql_root_host) || !defined($mysql_root_port) || !defined($mysql_root_user) || !defined($mysql_root_pass)) {
    print "FATAL:  MySQL access credentials are incomplete; processing aborted!\n";
    exit 1;
}
print "Using MySQL host:  $mysql_root_host\n";
print "Using MySQL port:  $mysql_root_port\n";
print "Using MySQL user:  $mysql_root_user\n";

# This effort of finding the list of databases also serves to both validate the credentials
# entered by the user, and to check that the database is up, running, and accessible.

my ($my_local_cnf, $port_or_sock) = save_mysql_credentials($mysql_root_host, $mysql_root_port, $mysql_root_user, $mysql_root_pass);

my @databases = `$mysql --defaults-extra-file=$my_local_cnf -h $mysql_root_host $port_or_sock -s -s -e 'show databases'`;
if ($?) {
    print "ERROR:  Could not find a list of MySQL databases; processing aborted!\n";
    exit 1;
}
chomp @databases;

remove_mysql_credentials($my_local_cnf);

# Clever use of hash slice (Programming Perl, 3/e, pp. 94-95).
my %databases = ();
@databases{@databases} = (1) x @databases;

my %databases_to_convert = ();

# Scan the set of MySQL databases, and verify that we have the set we need.

# Databases that should be present, but that we intentionally ignore:
foreach my $dbname (qw(information_schema jbossdb mysql test)) {
    delete $databases{$dbname};
}

# Databases that must be present, and that we convert:
my $missing_required_database  = 0;
my $missing_access_credentials = 0;
foreach my $dbname (qw(GWCollageDB monarch jbossportal dashboard)) {
    if (not $databases{$dbname}) {
	print "ERROR:  Database \"$dbname\" needs conversion, but it was\n";
	print "        not found in the list of databases.\n";
	$missing_required_database = 1;
    }
    if (not defined $dbhost{$dbname}) {
	print "ERROR:  Access credentials for the \"$dbname\" database are missing.\n";
	$missing_access_credentials = 1;
    }
    $databases_to_convert{$dbname} = delete $databases{$dbname};
}
if ($missing_required_database) {
    print "FATAL:  At least one database requiring conversion was not found;\n";
    print "        processing aborted!\n";
    exit 1;
}

# Databases that may be present, and that we convert if found:
foreach my $dbname (qw(cacti nedi ganglia alertsite HelpDeskBridgeDB)) {
    if ($databases{$dbname}) {
	if (not defined $dbhost{$dbname}) {
	    print "ERROR:  Access credentials for the \"$dbname\" database are missing.\n";
	    $missing_access_credentials = 1;
	}
	$databases_to_convert{$dbname} = delete $databases{$dbname};
    }
    elsif (defined $dbhost{$dbname}) {
	if ($dbhost{$dbname} eq $mysql_root_host) {
	    ## This database is definitively not present, so let's not confuse
	    ## ourselves by carrying around the credentials for it.  Those
	    ## credentials are apparently present in the configuration files
	    ## even though the database itself is not present at that location.
	    print "WARNING:  The \"$dbname\" database is not present on $mysql_root_host,\n";
	    print "          although its access credentials indicate that it might be.\n";
	    print "          This database is being skipped during this migration.\n";
	    delete $dbhost{$dbname};
	    delete $dbport{$dbname};
	    delete $dbuser{$dbname};
	    delete $dbpass{$dbname};
	}
	else {
	    ## This database resides on some other host, not on $mysql_root_host.  Presumably, the
	    ## reason it got that way is because the local site knew what they were doing when they
	    ## moved it, unless the difference between $dbhost{$dbname} and $mysql_root_host is one
	    ## of aliasing (e.g., "localhost" vs. the actual machine name) and not one of actual
	    ## location.  In any case, this situation is sufficiently indicative of the database
	    ## actually existing that we will count it as such, without checking $dbhost{$dbname}
	    ## explicitly, and try to convert it.  If the database in fact does not exist on the
	    ## $dbhost{$dbname} machine, the later processing should fail.  In that case, a simple
	    ## workaround for our assumption here is to comment out the credentials for $dbname in
	    ## the respective configuration file, then run this master migration script again.
	    $databases_to_convert{$dbname} = 1;
	}
    }
    else {
	## No database, no credentials.  Just skip $dbname entirely.  Because there's no evidence of
	## the database anywhere, we don't even bother to warn the user that it won't be converted.
    }
}

# Databases that may be present, and that we intentionally ignore:
foreach my $dbname (qw(dashboards guava jobstatus j2 logreports sv)) {
    delete $databases{$dbname};
}

# Databases that may be present, and that we know we need to deal with, but don't yet:
my $found_database_not_yet_handled = 0;
## This list is now empty because we have put code in place above to handle all the
## databases that used to be named here.  There might still be issues of not having
## a PostgreSQL schema ready for certain such databases to be migrated to, but if so,
## that fact will be discovered later on in this script when we attempt to run the
## migration of such databases.
foreach my $dbname (qw()) {
    if ($databases{$dbname}) {
	print "ERROR:  Database \"$dbname\" needs conversion, but this master\n";
	print "        migration script is not yet equipped to do so.\n";
	$found_database_not_yet_handled = 1;
    }
}
if ($found_database_not_yet_handled) {
    print "FATAL:  Found at least one database that needs conversion that\n";
    print "        is not yet handled by this script; processing aborted!\n";
    exit 1;
}

if ($missing_access_credentials) {
    print "FATAL:  We are missing access credentials for at least one\n";
    print "        database requiring conversion; processing aborted!\n";
    exit 1;
}

# Any other database:  the presence of such a database should cause this
# script to fail with a warning message, after converting all the other
# data that it could handle from other databases.  We will deal with that
# when this script exits.

my $warn_if_extra_databases = 1;
END {
    if ($warn_if_extra_databases && %databases) {
	print "\n";
	foreach my $dbname (sort keys %databases) {
	    print "WARNING:  Found unknown database \"$dbname\"; this master\n";
	    print "          migration script is not equipped to handle it.\n";
	}
	print "WARNING:  Found at least one unknown database that has not been\n";
	print "          converted (see above).  Contact GroundWork Support for help\n";
	print "          in this regard, BEFORE you complete the GWMEE upgrade.\n";

	# We don't want to exit directly from this END block, as that
	# might possibly cause other END blocks not to execute.  But we
	# do want to force the exit status of the script to indicate
	# a failure in this case, even if we got all the way through
	# creating a semaphore file whose presence indicates that it is
	# okay to run the installer again to complete the full upgrade.
	$force_bad_exit_code = 1;
    }
}

# We save the outcome in a variable in case we might want to decode it.
my $validation_command = "/usr/local/groundwork/core/migration/validate_gw_db.pl";
$validation_command .= " '$dbhost{jbossportal}:jbossportal:$dbuser{jbossportal}:$dbpass{jbossportal}'" if defined $dbhost{jbossportal};
$validation_command .= " '$dbhost{nedi}:nedi:$dbuser{nedi}:$dbpass{nedi}'" if defined $dbhost{nedi};
$validation_command .= " '$dbhost{ganglia}:ganglia:$dbuser{ganglia}:$dbpass{ganglia}'" if defined $dbhost{ganglia};
my $validation_outcome = system($validation_command);
if ($validation_outcome) {
    print "FATAL:  The MySQL databases have failed validation tests\n";
    print "        (see above).  You must fix the stated problems\n";
    print "        before running this master migration script again.\n";
    exit 1;
}

if ($just_check_for_data_problems) {
    print "NOTICE:  Exiting early due to request to only run validation checks,\n";
    print "         not to run the data upgrades or database migration.\n";
    exit 0;
}

# Everything before this point has just involved read-only access to the MySQL databases,
# and those actions take a fairly small amount of time.  But from this point forward, we
# may be making changes to the databases, in preparation for the actual conversion to
# PostgreSQL.  So it makes sense now to ask for the access credentials for PostgreSQL,
# before we run scripts that might have a long running time.  That way, the administrator
# can enter the appropriate data and then walk away while the action completes.

# Default values; may be overridden by user input.
my $psql_root_host = 'localhost';
my $psql_root_port = '5432';
my $psql_root_user = 'postgres';
my $psql_root_pass = '';

# On the other hand, we don't bother asking for credentials if we won't be using them.
if (not $just_run_data_checks_and_upgrades) {
    $psql_root_host = get_value($psql_root_host, "PostgreSQL host machine");
    $psql_root_port = get_value($psql_root_port, "PostgreSQL port");
    $psql_root_user = get_value($psql_root_user, "PostgreSQL administrator username");
    $psql_root_pass = get_value($psql_root_pass, "PostgreSQL administrator password", 1);
    if (!defined($psql_root_host) || !defined($psql_root_port) || !defined($psql_root_user) || !defined($psql_root_pass)) {
	print "FATAL:  PostgreSQL access credentials are incomplete; processing aborted!\n";
	exit 1;
    }
    print "Using PostgreSQL host:  $psql_root_host\n";
    print "Using PostgreSQL port:  $psql_root_port\n";
    print "Using PostgreSQL user:  $psql_root_user\n";

    # We test the connection to PostgreSQL before attempting any migration.
    my $pg_check_outcome = system (
	'/usr/bin/env',
	    "PGPASSWORD=$psql_root_pass",
	    '/usr/local/groundwork/postgresql/bin/psql',
		'-h', $psql_root_host,
		'-p', $psql_root_port,
		'-U', $psql_root_user,
		'-w',
		'-X',
		'monarch',
		'-f', '/dev/null'
    );
    if ($pg_check_outcome) {
	print "FATAL:  Cannot access the PostgreSQL databases; processing aborted!\n";
	exit 1;
    }
}

# Run the useful current Perl migrate scripts in /usr/local/groundwork/core/migration:
foreach my $migration_script (qw(
  /usr/local/groundwork/core/migration/alter_graph_id_columns.pl
  /usr/local/groundwork/core/migration/migrate-dashboard.pl
  /usr/local/groundwork/core/migration/migrate-monarch.pl
  )) {
    my $outcome = system($migration_script);
    if ($outcome) {
	print "FATAL:  Error in running:  $migration_script\n";
	print "FATAL:  Updating of MySQL databases has failed; processing aborted!\n";
	exit 1;
    }
}

# We enforce conversion dependencies by running scripts in multiple passes.
# Within each pass, no specific ordering is required.
my @sql_script_passes = (
    {
	'/usr/local/groundwork/core/migration/migrate-gwcollagedb.sql' => 'GWCollageDB',
	'/usr/local/groundwork/core/migration/migrate-monarch.sql'     => 'monarch',
	'/usr/local/groundwork/core/migration/migrate_admin_roles.sql' => 'jbossportal',
    },
    {
	'/usr/local/groundwork/core/migration/patch-gwcollagedb.sql'   => 'GWCollageDB',
    }
);

# Run the useful current SQL migrate scripts in /usr/local/groundwork/core/migration:
foreach my $sql_script_pass (@sql_script_passes) {
    foreach my $sql_script (keys %$sql_script_pass) {
	if (not run_sql_script($sql_script_pass->{$sql_script}, $sql_script)) {
	    print "FATAL:  Error in running:  $sql_script\n";
	    print "FATAL:  Updating of MySQL databases has failed; processing aborted!\n";
	    exit 1;
	}
    }
}

if ($just_run_data_checks_and_upgrades) {
    print "NOTICE:  Exiting early due to request to only run validation checks\n";
    print "         and data upgrades, not to run database migration.\n";
    exit 0;
}

my $directory_umask = 0777;
my $base_export_file_path = '/usr/local/groundwork/pg_migration';
if (not mkdir ($base_export_file_path, $directory_umask) and $! != POSIX::EEXIST) {
    print "FATAL:  Error in creating the $base_export_file_path directory ($!).\n";
    print "FATAL:  Migration to PostgreSQL databases has failed; processing aborted!\n";
    exit 1;
}

# Run mysql2postgresql.sh on various databases.
$directory_umask = 0755;
foreach my $dbname (keys %databases_to_convert) {
    my $export_file_path = "$base_export_file_path/$dbname.$date";
    my $pg_dbname = lc($dbname);
    print "\n";
    print "INFO:  Converting the MySQL $dbname database to the PostgreSQL $pg_dbname database ...\n";
    if (not mkdir ($export_file_path, $directory_umask) and $! != POSIX::EEXIST) {
	print "FATAL:  Error in creating the $export_file_path directory ($!).\n";
	print "FATAL:  Migration to PostgreSQL databases has failed; processing aborted!\n";
	exit 1;
    }
    my $outcome = system (
	'/usr/local/groundwork/core/migration/postgresql/mysql2postgresql.sh',
	"$dbhost{$dbname}:$dbport{$dbname}:$dbname:$dbuser{$dbname}:$dbpass{$dbname}",
	"$psql_root_host:$psql_root_port:$pg_dbname:$psql_root_user:$psql_root_pass",
	$export_file_path
    );
    if ($outcome) {
	print "FATAL:  Error in running mysql2postgresql.sh for the $dbname database.\n";
	print "FATAL:  Migration to PostgreSQL databases has failed; processing aborted!\n";
	exit 1;
    }
}

# We got to the end without bombing out on an error.  It's time to create the
# semaphore file used to tell a subsequent invocation of the installer that it's
# okay to install the rest of the product and delete the MySQL server software.
if (sysopen (SEMAPHORE, $semaphore, O_WRONLY | O_NOFOLLOW | O_CREAT, 0600)) {
    close SEMAPHORE;
    if (-f $semaphore) {
	print "\n";
	print "NOTICE:  The migration-complete semaphore file has been created:\n";
	print "         $semaphore\n";
    }
    else {
	print "FATAL:  Error in creating the semaphore file for unknown reasons:\n";
	print "        $semaphore\n";
	print "FATAL:  Migration to PostgreSQL databases has failed; processing aborted!\n";
	exit 1;
    }
}
else {
    print "FATAL:  Error in creating the semaphore file $semaphore ($!).\n";
    print "FATAL:  Migration to PostgreSQL databases has failed; processing aborted!\n";
    exit 1;
}

__END__

