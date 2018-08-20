#!/usr/local/groundwork/perl/bin/perl --
#
# Database updater for Application log files
# Based on check_log2.pl by Aaron Bostick (abostick@mydoconline.com)
# Written by Thomas Stocking (tstocking@gwos.com)
#
# Major rewrite to fit Shaw Satellite requirements 12/15/08 by Tony Hansmann
#     (tony@opensourceconsulting.com)
# Significant revision to fix performance and functional problems in May 2009
# Last modified: 2011-03-22
#
# Thanks and acknowledgements to Ethan Galstad for Nagios and the check_log
# plugin this is modeled after.
#
# 2010-01-29 Thomas Stocking
#	Added substitution of legal XML representaions of illegal characters in XML
#	to messages posted to Foundation.
# 2010-08-11 Glenn Herteg
#	Cleaned up XML encoding substitutions.
# 2011-10-23 Glenn Herteg
#	Ported to support PostgreSQL.

BEGIN {
    if ( $0 =~ m:^(.*?)[/\\]([^/\\]+)$: ) {
	$prog_dir  = $1;
	$prog_name = $2;
    }
    else {
	$prog_dir  = '.';
	$prog_name = $0;
    }
}

require 5.004;

use lib qw(/usr/local/groundwork/nagios/libexec /usr/local/groundwork/lib/perl5/site_perl);

use lib $main::prog_dir;
use utils qw(%ERRORS &print_revision &support &usage);
use Getopt::Long;
use IO::Socket;
use IO::File;
use DBI;
use CollageQuery;
use POSIX qw(strftime);

# Initialize strings
my $debug           = 0;
my $log_file        = '';
my $seek_file       = '';
my $script_revision = '2.00';
my $serverip        = '';
my $host            = '';
my $remote_host     = "localhost";
my $remote_port     = 4913;
my $thisnagios      = "localhost";

my $DEV     = 0;
my $INO     = 1;
my $MODE    = 2;
my $NLINK   = 3;
my $UID     = 4;
my $GID     = 5;
my $RDEV    = 6;
my $SIZE    = 7;
my $ATIME   = 8;
my $MTIME   = 9;
my $CTIME   = 10;
my $BLKSIZE = 11;
my $BLOCKS  = 12;

###############
# easier to read syslog regexp
my $syslog_regexp = qr{
	 (\w+)            # $1 the month
	 \s+              # space or more, do not retain
	 (\d+)            # $2 day of the month, one or two digits
	 \s               # space, do not retain
	 (\d\d:\d\d:\d\d) # $3 the time
	 \s               # space, do not retain
	 (.*?)            # $4 component
	 \s               # space, do not retain
	 (.*?)            # $5 message type
	 :                # literal colon, do not retain
	 (.*)             # $6 text message of syslog.
     }xms;    # xms options to allow regexps to be spread out with comments, etc

# easy index vars for refering to syslog line components.
# Syslog line example 'Dec  2 18:44:20 gwos1 kernel: SELinux:  Initializing.'
my $MONTH     = 0;    # $1 the month
my $MDAY      = 1;    #sprintf "%02d",$2; # translate single digit date to leading zero
my $TIME      = 2;    # $3;
my $COMPONENT = 3;    # $4 component
my $MSGTYPE   = 4;    # $5  message type
my $TEXT      = 5;    # $6;
###############

my %monthhash = (
    "Jan" => "01",
    "Feb" => "02",
    "Mar" => "03",
    "Apr" => "04",
    "May" => "05",
    "Jun" => "06",
    "Jul" => "07",
    "Aug" => "08",
    "Sep" => "09",
    "Oct" => "10",
    "Nov" => "11",
    "Dec" => "12",
);

# Absolute pathname of the Nagios command pipe.
my $nagios_cmd_pipe = "/usr/local/groundwork/nagios/var/spool/nagios.cmd";

# The number of slots to pre-allocate for queueing Nagios service messages
# before they are sent to the command pipe.  This number should be a little
# more than the total number of log messages you expect in each cycle.
my $initial_nagios_messages_size = 500;

# The maximum time in seconds to wait for any single write to the nagios command pipe
# to complete.
my $max_command_pipe_wait_time = 60;

# The maximum size in bytes for any single write operation to the output command pipe.
# The value chosen here must be no larger than PIPE_BUF (getconf -a | fgrep PIPE_BUF)
# on your platform, unless you have an absolute guarantee that no other process will
# ever write to the command pipe.
my $max_command_pipe_write_size = 4096;

# Look for these message types

# it ignores case by default - config to make different case a different option. -Tony
Getopt::Long::Configure("no_ignore_case");

# Grab options from command line
GetOptions(

    # required options
    "a|ipaddress=s"          => \$ipaddress,
    "l|logfile=s"            => \$log_file,
    "s|seekfile=s"           => \$seek_file,
    "x|critical_regx_file=s" => \$critical_regx_file,

    # optional files
    "r|rotation_logfile=s"  => \$rotation_logfile,
    "i|ignore_regx_file=s"  => \$ignore_regx_file,
    "o|ok_regx_file=s"      => \$ok_regx_file,
    "w|warning_regx_file=s" => \$warning_regx_file,
    "t|matches_outfile=s"   => \$matches_outfile,

    # optional modifiers
    "b|host=s"           => \$host,
    "T|timeout=i"        => \$TIMEOUT,
    "c|no_consolidation" => \$no_consolidation,
    "g|generic_logfile"  => \$generic_logfile,
    "D|debug"            => \$DEBUG,
    "n|negate-test"      => \$negate,
    "v|version"          => \$version,
    "h|help"             => \$help
);

###############
# if we dont have the right options, exit.
!($version) || print_version();
!($help)    || print_help();

# Make sure certain files are specified.
($log_file)           || usage("Log file not specified.\n");
($seek_file)          || usage("Seek file not specified.\n");
($critical_regx_file) || usage("Critical Regexp file specified.\n");
###############

###############
# Set a timeout. Just in case of problems, let's not hang Nagios
# # took out the throttle stuff and replaced it with this. Will leave
# # stuff in a messy state if it alarm exits. -Tony
if ( $TIMEOUT > 0 ) {
    $SIG{'ALRM'} = sub {
	print("ERROR: $0 timed out, exiting (alarm timeout)\n");
	exit $ERRORS{"UNKNOWN"};
    };

    if ( $max_command_pipe_wait_time > $TIMEOUT ) {
	$max_command_pipe_wait_time = $TIMEOUT;
    }

    alarm($TIMEOUT);
}
###############

# if the host is not defined grab it from monarch
$host ||= get_ip_from_monarch($ipaddress);

# This is the lynchpin array - it's passed all over AND there are other reference to it
# that will have to be hand maintained if we need to change categories. -Tony
# Look for '${eval $category}' that create hash_refs like $WARNING.
# egrep "CRITICAL|WARNING|OK|IGNORE|NOT_MATCHED|eval" check_syslog_gw.pl to find the hand codes.
my @matching_category = qw/IGNORE OK WARNING CRITICAL NOT_MATCHED/;

# figures out which files to read and in what order, *writes the seek_file for next time*.
# Does the right thing if $seek_file and/or $rotation_logfile are not
# defined. returns an array of syslog lines to operate on. -Tony
@raw_syslog_lines = LOG_READ::do_all_log_reads( "$log_file", "$seek_file", "$rotation_logfile" );

# get all the regexp files and output options - all
$match_obj_hash_ref =
  LOG_SEIVE::do_all_log_matching( \@matching_category, \@raw_syslog_lines, $critical_regx_file, $warning_regx_file, $ok_regx_file,
    $ignore_regx_file, $matches_outfile, );

# all matching is done at this point -tony

###
# set the severity.
my $severity = 'OK';
for my $category (qw/CRITICAL WARNING/) {

    # if we have any matches set the severity and exit the loop
    # if no count for CRIT or WARN, fall throught and set to OK.
    if ( $match_obj_hash_ref->{$category}->count_matches() ) {
	$severity = $category;
	last;
    }

    # if we make it here we are at severity OK
    $severity = 'OK';
}

# this is the standard output required by the nagios plugin standard.
$nagios_formatted_output_line = format_nagios_output( \@matching_category, $match_obj_hash_ref, $severity );

# the negate-test option DOES NOT WRITE TO Nagios or Foundation - only for testing.
# exits if passed. returns everything that did not match a passed-in file.
# IF $negate WE ARE EXITING IN THIS BLOCK
if ($negate) {
    print join "\n", $match_obj_hash_ref->{'NOT_MATCHED'}->get_matches(), "\n";
    print "$nagios_formatted_output_line\n";
    exit $ERRORS{'UNKNOWN'};
}

###############
# Overwrite the match output file every time
-f $matches_outfile && zero_out_file("$matches_outfile");

my @xml_messages    = ();
my @nagios_messages = ();
$#nagios_messages = $initial_nagios_messages_size;    # pre-extend the array, for efficiency
$#nagios_messages = -1;                               # truncate the array, since we don't have any Nagios messages yet

for my $category (qw/CRITICAL WARNING OK/) {

    # handle the commandline option to write matches out to a file. - tony
    if ($matches_outfile) {
	## write_matches does an append.  can add some flexibility by appending
	## the $category to the outfile name to get a unique file for each.
	$match_obj_hash_ref->{$category}->write_matches("$matches_outfile");
    }

    # do the xml for foundation and passive post for nagios in one pass.
    for my $syslog_line ( $match_obj_hash_ref->{$category}->get_matches() ) {

	# parse the syslog line
	my @syslog_component = parse_syslog_line($syslog_line);

	# queue the passive check result to Nagios
	push @nagios_messages, format_nagios_passive_alert( $host, $category, $syslog_component[$TEXT] );

	# Sub out "bad" characters from $syslog_line
	my $xmlmsg = $syslog_line;
	$xmlmsg =~ s/\n/ /g;
	$xmlmsg =~ s/\f/ /g;
	$xmlmsg =~ s/<br>/ /ig;
	$xmlmsg =~ s/&/&amp;/g;
	$xmlmsg =~ s/"/&quot;/g;
	$xmlmsg =~ s/'/&apos;/g;
	$xmlmsg =~ s/</&lt;/g;
	$xmlmsg =~ s/>/&gt;/g;

	# queue a write to GroundWork Foundation
	push @xml_messages, Format_Nagios_XML( "$host", "$ipaddress", "$category", "$xmlmsg", "$generic_logfile" );
    }
}

if ( scalar @xml_messages ) {

    # send Foundation the "we are done streaming data" lines.
    push @xml_messages, CommandClose();

    # We do this final work outside of the period when the socket is open.
    my $full_message = join( '', @xml_messages );

    # open a socket to the foundation server.
    # FIX THIS:  return a proper plugin error to the standard output if we fail to open the socket,
    # or if the print() or close() returns an error
    my $socket = IO::Socket::INET->new(
	PeerAddr => $remote_host,
	PeerPort => $remote_port,
	Proto    => "tcp",
	Type     => SOCK_STREAM
    ) or die "ERROR: Can't open TCP socket $remote_port to host $remote_host\n";
    print $socket $full_message;
    close($socket);
}

# FIX THIS:  sense whether we were able to send the passive results to Nagios,
# and change an OK severity to WARNING and insert a warning message in the plugin output if not
send_passive_alerts_to_nagios( \@nagios_messages );

# the exit report for nagios.
print "$nagios_formatted_output_line\n";

# use the nagios plug-in exit hash. -Tony
if ($severity) {
    exit $ERRORS{$severity};
}
else {
    exit $ERRORS{'UNKNOWN'};
}

# End process logic sections

##############################
# Subroutines
##############################

# This is based on Shaw Satelliete request from Nov 2008.  -Tony
sub format_nagios_output {

    my $matching_category_arr_ref = shift @_;
    my $match_objects_hash_ref    = shift @_;
    my $severity                  = shift @_;

    my @matching_category = @{$matching_category_arr_ref};

    # these variables are hash_ref and are autocreated via an eval below.
    my $OK;
    my $WARNING;
    my $CRITICAL;
    my $IGNORE;
    my $NOT_MATCHED;

    # Several examples of output are shown below:

    my $T = 0;    # T	Total number of messages matched or not matched
    my $I = 0;    # I	Total number of ignored messages matched
    my $O = 0;    # O	Total number of OK messages matched
    my $W = 0;    # W	Total number of Warning messages matched
    my $C = 0;    # C	Total number of Critical messages matched
    my $A = 0;    # A	Total number of OK, Warning and Critical messages matched
    my $U = 0;    # U	Total number of unmatched messages

    # OK, Warning and Critical messages matched: (exit code 0):
    #  OK: Matched no messages: T=13, I=13, (O=0, W=0, C=0) A=0 U=0 | T=13;;;; I=13;;;; O=0;;;; W=0;;;; C=0;;;; A=0;;;; U=0;;;;

    # 2 OK messages matched: (exit code 0):
    #  OK: Matched messages: T=13, I=11, (O=2, W=0, C=0) A=2 U=0 | T=13;;;; I=11;;;; O=2;;;; W=0;;;; C=0;;;; A=2;;;; U=0;;;;

    # 3 OK and Warning messages matched: (exit code 1):
    #  WARNING: Matched messages: T=8, I=5, (O=1, W=2, C=0) A=3 U=0 | T=8;;;; I=5;;;; O=1;;;; W=2;;;; C=0;;;; A=3;;;; U=0;;;;

    # 5 OK, Warning and Critical messages matched: (exit code 2):
    #  CRITICAL: Matched messages: T=7, I=2, (O=0, W=1, C=4) A=5 U=0 | T=7;;;; I=2;;;; O=0;;;; W=1;;;; C=4;;;; A=5;;;; U=0;;;;

=pod

From the Shaw Satellite SOW (corrected here)

     CRITICAL:              # state based on hightest level alert
     Matched messages:      # static string
	T=7,                # T	Total number of messages matched or not matched (DERIVED, = I + A + U)
	I=2,                # I	Total number of ignored messages matched
	    (               # literal paren
	      O=0,          # O	Total number of OK messages matched
	      W=1,          # W	Total number of Warning messages matched
	      C=4           # C	Total number of Critical messages matched
	    )               # literal paren
	A=5                 # A	Total number of OK, Warning and Critical messages matched (DERIVED, = O + W + C)
	U=0                 # U	Total number of unmatched messages DERIVED
	|                   # literal vert bar
	T=7;;;;
	I=2;;;;
	O=0;;;;
	W=1;;;;
	C=4;;;;
	A=5;;;;
	U=0;;;;

the unique values we need are
    CRITICAL
    WARNING
    OK
    IGNORE
    NOT_MATCHED

A = CRITICAL + WARNING + OK
U = NOT_MATCHED

=cut

    # make the vars: $CRITICAL $WARNING $OK $IGNORE $NOT_MATCHED out the hash passed for easy of use.
    for my $category (@matching_category) {

	# make some easy use reference for each type of object
	# Caveat programmer - this eval springs each of these in existance $CRITICAL $WARNING $OK $IGNORE $NOT_MATCHED
	# youll see them reference below -Tony
	${ eval $category } = $match_objects_hash_ref->{$category};
    }

    # 'I' Total number of ignored messages matched
    $I = $match_objects_hash_ref->{'IGNORE'}->count_matches();

    # O	Total number of OK messages matched
    $O = $match_objects_hash_ref->{'OK'}->count_matches();

    # W	Total number of Warning messages matched
    $W = $match_objects_hash_ref->{'WARNING'}->count_matches();

    # C	Total number of Critical messages matched
    $C = $match_objects_hash_ref->{'CRITICAL'}->count_matches();

    # FIX THIS:  just calculate A=O+W+C instead
    # A	Total number of OK, Warning and Critical messages matched DERIVED
    for my $category (qw/CRITICAL WARNING OK/) {
	$A += $match_objects_hash_ref->{$category}->count_matches();
    }

    # 'U' Total number of unmatched messages DERIVED
    $U = $match_objects_hash_ref->{'NOT_MATCHED'}->count_matches();

    # FIX THIS:  just calculate T=I+A+U instead
    # 'T' Total number of messages looked at
    for my $category (@matching_category) {
	$T += $match_objects_hash_ref->{$category}->count_matches();
    }

    return
"$severity: Matched messages: T=$T, I=$I, ( O=$O, W=$W, C=$C ) A=$A U=$U | T=$T;;;; I=$I;;;; O=$O;;;; W=$W;;;; C=$C;;;; A=$A;;;; U=$U;;;; ";
}

##############################

sub parse_syslog_line {
    my $syslog_line = shift @_;
    my @syslog_component;

    # if the syslog line matches the pattern, return the components and assign them
    # to the @syslog_component array.
    if ( ( @syslog_component = $syslog_line =~ $syslog_regexp ) ) {
	## return the list or a reference to the @syslog_component array depending on
	## how the sub was called.
	wantarray ? return @syslog_component : return \@syslog_component;
    }
    else {
	## Ignore unrecognizable syslog line.
	return undef;
    }
}

##############################

sub Format_Nagios_XML {

    # pull this in from the def at the top of the file. -Tony
    my $host        = shift @_;
    my $ipaddress   = shift @_;
    my $severity    = shift @_;
    my $syslog_line = shift @_;
    my $generic_log = shift @_;

    my @message_parts = ();
    $#message_parts = 20;    # pre-extend the array, for efficiency
    $#message_parts = -1;    # truncate the array, since we don't have any messages components yet

    my $xml_message;
    my @syslog_component;
    my $year;
    my $month;
    my $mday;
    my $text;
    my $msgtype;
    my $datetime;
    my $reportdate;

    $reportdate = strftime "%Y-%m-%d %T", localtime;
    if ($generic_log) {

	# this means are not doing any parsing on the line, the whole
	# thing just goes to Foundation -Tony
	# do all the date math
	$text    = $syslog_line;
	$msgtype = "generic";

	# needs to looklike LastInsertDate="2008-12-02 18:44:19"
	$datetime = $reportdate;

    }
    else {
	@syslog_component = parse_syslog_line($syslog_line);

	# do all the date math
	$year  = (localtime)[5] + 1900;
	$month = $syslog_component[$MONTH];

	# We fix the day of month so a single-digit day number is zero-padded.
	$mday     = sprintf "%02d", $syslog_component[$MDAY];
	$text     = $syslog_component[$TEXT];
	$msgtype  = $syslog_component[$MSGTYPE];
	$datetime = "$year-$monthhash{$month}-$mday $syslog_component[$TIME]";    # Use current year. No year in log!
    }

    # Make the text XML-safe.
    $text =~ s/^\s+//g;                                                           # delete leading space
    $text =~ s/\n/ /g;                                                            # replace newline with a space
    $text =~ s/<br>/ /ig;                                                         # replace breaks with a space
    $text =~ s/["']/&quot;/g;                                                     # replace either quote with html safe &quot
    $text =~ s/</&lt;/g;                                                          # replace open angle brace with html safe &lt
    $text =~ s/>/&gt;/g;                                                          # replace close angle brace with html safe &gt

    if ($no_consolidation) {
	push @message_parts, "<SYSLOG  ";                                         # Start message tag.  Consolidation is OFF
    }
    else {
	push @message_parts, "<SYSLOG consolidation='SYSLOG' ";                   # Start message tag.  Consolidation is ON
    }
    push @message_parts, "MonitorServerName=\"$thisnagios\" ";                    # Default Identification
										  # Device should always be IP everywhere
    push @message_parts, "Device=\"$ipaddress\" ";                                # Default Identification
										  # Nagios construct - ignored by syslog rules
    push @message_parts, "Host=\"$host\" ";                                       # Default Identification
    push @message_parts, "Severity=\"$severity\" ";
    push @message_parts, "MonitorStatus=\"$severity\" ";
    push @message_parts, "ReportDate=\"$reportdate\" ";                           # set ReportDate to current local time
    push @message_parts, "LastInsertDate=\"$reportdate\" ";
    push @message_parts, "ipaddress=\"$ipaddress\" ";
    push @message_parts, "ErrorType=\"$msgtype\" ";
    push @message_parts, "SubComponent=\"$msgtype\" ";
    push @message_parts, "TextMessage=\"$text\" ";
    push @message_parts, "/>";                                                    # End message tag

    $xml_message = join( '', @message_parts );

    # print $xml_message . "\n";
    if ($main::DEBUG) {
	if ( !open( FILE, '>>', "foundation_out.xml" ) ) {
	    warn "foundation_out.xml: $!";
	}
	else {
	    print FILE $xml_message, "\n";
	    close FILE;
	}
    }
    return $xml_message;
}

##############################

sub CommandClose {

    # Create XML stream - Format:
    #	<SERVICE-MAINTENANCE     command="close" />
    return "<SERVICE-MAINTENANCE command=\"close\" />";
}

##############################

sub print_usage {
    print "
Usage: $prog_name
	    -l <logfile>             # required
	    -s <seekfile>            # required
	    -x <critical.regexfile>  # required
	    -a <hostaddress>         # required
	    -b <hostname>            # optional, but strongly recommended
	    -r <rotation.logfile>    # optional
	    -i <ignore.regexfile>    # optional
	    -o <ok.regexfile>        # optional
	    -w <warning.regexfile>   # optional
	    -t <matches.outfile>     # optional
	    -T <seconds>             # optional; set a timeout for program to abort
	    -c     # optional; do not consolidate messages in Foundation
	    -g     # optional; use generic logfile format, not syslog format
	    -D     # optional; turn on debugging
	    -n     # optional; negate-test option, only for debugging

Usage: $prog_name [ -v | --version ]
Usage: $prog_name [ -h | --help ]
";
}

##############################

sub print_version {
    print_revision( $prog_name, $script_revision );
    exit $ERRORS{'OK'};
}

##############################

sub print_help {
    print_revision( $prog_name, $script_revision );
    print "\n";
    print "Send syslog messages into the GroundWork Foundation database.\n";
    print_usage();

    print '
Description:

    This script will read a specified logfile and compare each line, from
    the seekfile marker (or the start of the file if the marker was not set).
    If the rotation.logfile is specified, and if the seekfile last modified
    time is older than the last modified time of the rotation.logfile then the
    script will read from the seekfile marker position in the rotation.logfile
    and NOT the logfile.  The next iteration of plugin execution will cause the
    seekfile last modified time to be updated and the plugin will then set the
    marker to zero and read the specified logfile to the EOF.  The comparison
    of each line will be made with patterns in regular expression files.
    Each regexfile is checked in the following order if specified:

	1. ignore.regexfile is checked.  If a match is found, the message
	   is ignored, counters are set, and we move on to the next message.
	   If no match is found, the next regexfile is checked.

	2. ok.regexfile is checked.  If a match is found, the message is
	   sent as OK, counters are set, and we move on to the next message.
	   If no match is found, the next regexfile is checked.

	3. warning.regexfile is checked.  If a match is found, the message
	   is sent as WARNING, counters are set, and we move on to the next
	   message.  If no match is found, the next regexfile is checked.

	4. critical.regexfile is checked.  If a match is found, the message
	   is sent as CRITICAL, counters are set, and we move on to the next
	   message.  If no match is found, an unmatched counter is set, and
	   we move on to the next message.

	5. If matches.outfile is specified then the complete log lines
	   for any matches in critical.regexfile and warning.regexfile are
	   written to matches.outfile.	Each time the plugin is executed,
	   the matches.outfile file is opened in write-mode, i.e., it is
	   overwritten.

Examples:

    Simple:
	check_syslog_gw.pl                 \
	    -a 127.0.0.1                   \
	    -l /work/syslog_file           \
	    -s /work/seek_file             \
	    -x /work/regex_files/CRIT

    Full:
	check_syslog_gw.pl                 \
	    -T 500                         \
	    -a 127.0.0.1                   \
	    -b loghost                     \
	    -l /work/syslog_file           \
	    -r /work/syslog_file.old.1     \
	    -s /work/seek_file             \
	    -t /work/matches_outfile       \
	    -i /work/regex_files/IGNORE    \
	    -o /work/regex_files/OK        \
	    -w /work/regex_files/WARN      \
	    -x /work/regex_files/CRIT

';
    exit $ERRORS{'OK'};
}

##############################

# Send to Nagios command pipe

sub format_nagios_passive_alert {
    my $host     = shift;
    my $severity = shift;
    my $msg      = shift;
    my $datetime = time;

    # FIX THIS:  use a join instead.
    return "[$datetime] PROCESS_SERVICE_CHECK_RESULT;$host;syslog_last;$ERRORS{$severity};$msg";
}

sub catch_signal {
    my $signame = shift;

    die "Caught a SIG$signame signal!";
}

sub send_passive_alerts_to_nagios {
    my $messages_ref = shift;
    my $status       = 1;

    # We will extend whatever encompassing timeout is in play for the duration of our
    # attempts to write to the pipe, but put back whatever remains of that timeout afterward,
    # so it can still rule the general operation of the script.  There is some truncation of
    # time to whole-second values in these computations, so that may introduce some degree of
    # inaccuracy in the overall effect.
    my $time_left  = alarm(0);
    my $start_time = time();

    if ( scalar(@$messages_ref) ) {

	# We don't want to open an actual file if the expected pipe does not exist.  The workaround is
	# this strange '+<" open mode that allows us write access, but won't create a nonexisting file.
	# The :unix discipline says we should perform unbuffered i/o.  This helps if we ever try to exit
	# gracefully after a timeout, so buffering doesn't kick in again causing the program to re-execute
	# a failed write operation and quite likely hang again.  It should also avoid the extra overhead
	# of copying from our string into an i/o buffer.
	if ( !open( FIFO, '+<:unix', $nagios_cmd_pipe ) ) {

	    # die "Could not open the Nagios command pipe: $!";
	    $status = 0;
	}
	else {

	    # Note:  dealing with the Nagios command pipe is fraught with possible race conditions.
	    # One of them is that we might open a file descriptor and write to it, only to block
	    # during that write and have no reader ever come around to read it.  To get around that,
	    # we set an alarm so we can break out of an otherwise infinite wait.  Our current approach
	    # to handling the alarm is distinctly unsophisticated:  we simply report the failure to
	    # our caller, without indicating how many messages were left unsent.
	    #
	    # Here's a weird thing we have to cope with.  If we just return from our signal handler
	    # in a non-eval context, Perl would just restart the system call on which we were hung.
	    # If instead we die here and use an eval context to catch the error, then eventually finish
	    # up by shutting down with a subsequent "die" or "exit", Perl tries to close the FIFO file
	    # descriptor, and if we're using buffered i/o, that close will try to flush the buffer and
	    # that flush won't complete until the write completes, which means the process will hang
	    # again, this time outside the control of an alarm context.  If we try to send SIGTERM to
	    # this process, that will only work if we don't have a signal handler in place for that
	    # signal that likewise tries to die or exit.
	    #
	    # To reliably avoid all these problems, we must send data to Nagios only from within an eval
	    # context, using unbuffered i/o (the :unix discipline), and die from within the signal handler.
	    #
	    local $SIG{ALRM} = \&catch_signal;

	    # To guarantee atomicity of the pipe writes, we can write no more than PIPE_BUF
	    # bytes in a single write operation.  This avoids having the pipe reader interleave
	    # messages from multiple sources at places other than message boundaries.
	    my $first = 0;
	    my $last  = $first;
	    my $message_size;
	    my $buffer_size    = 0;
	    my $index_past_end = scalar(@$messages_ref);
	    for ( my $index = 0 ; $index <= $index_past_end ; ++$index ) {
		if ( $index < $index_past_end ) {
		    $message_size = length( $messages_ref->[$index] );
		}
		else {
		    $message_size = 0;
		}
		if ( $index < $index_past_end && $buffer_size + $message_size <= $max_command_pipe_write_size ) {
		    $buffer_size += $message_size;
		}
		else {
		    if ( $buffer_size > 0 ) {

			# The nested eval{}s protect against race conditions.
			eval {
			    alarm($max_command_pipe_wait_time);

			    # We might die here either explicitly or because of a timeout and the signal
			    # handler action.  If we get the alarm signal and die because of it, we need
			    # not worry about resetting the alarm before exiting the eval, because it has
			    # already expired.
			    eval {
				print FIFO join( '', @{$messages_ref}[ $first .. $last ] )
				  or die "ERROR:  Cannot write to the Nagios command pipe: $!";
			    };
			    alarm(0);
			    die "$@" if ($@);
			};
			if ($@) {

			    # die "$@";
			    $status = 0;
			    last;
			}
		    }
		    $first       = $index;
		    $buffer_size = $message_size;
		}
		$last = $index;
	    }
	    close(FIFO);
	}
    }
    my $end_time = time();
    if ($time_left) {
	my $time_til_alarm = $time_left - ( $end_time - $start_time );
	alarm( $time_til_alarm > 0 ? $time_til_alarm : 1 );
    }
    return $status;
}

##############################

# --todo-- double check that this works. -Tony
sub get_ip_from_monarch {
    my $ipaddress = shift @_;
    my $host      = shift @_;
    ######################################################################
    #	This section attempts to get the IP address from Monarch
    ######################################################################
    # If no host supplied
    if ( !$host ) {
	## Get hosts->IPaddress from Monarch
	my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
	my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
	if ($dbh) {
	    my $query = "select name,address from hosts where address=\"$ipaddress\"; ";
	    my $sth   = $dbh->prepare($query);
	    $sth->execute() or die $sth->errstr;
	    while ( my $row = $sth->fetchrow_hashref() ) {
		$host = $$row{name};
	    }
	    $sth->finish();
	    $dbh->disconnect();
	}
    }

    return $host || $ipaddress;
}

#### do the dirty work away from the main program.
sub zero_out_file {
    my $file_to_zero_out = shift @_;
    if ( -f $file_to_zero_out ) {
	truncate $file_to_zero_out, 0;
    }
}

##############################
####### NEW PACKAGE DEF ######
##############################

package LOG_READ;

# use diagnostics;
# use strict;
use Carp;

my $end_of_logfile;
my @rotated_log_entries;
my @log_entries;
my %FILE_STAT_for;

my $TRUE  = 1;
my $FALSE = 0;

##############################

# We keep track of where we left off in the logfile on the last cycle of
# running this script by storing the file position in the seekfile.  But that
# position is subject to external upset by logfile rotation, at the very least.
# So we might not see the same file under the same name the next time we come
# around.  To better ensure we do have the same file, we look at several pieces
# of evidence: the device number, the inode number, and the file last-modify time
# when last we read it.  Bear in mind that the log file can be written to while
# we're in the middle of reading it, so capturing a file size and last-modified
# timestamp can be tricky.  (For that matter, the file metadata might only be
# updated after the data blocks, so the available data and the timestamp might
# not be truly synchronized, ahd the last-modified timestamp only has per-second
# time resolution.)  All of this is really just to say that you shouldn't get
# too comfortable with an oversimplified model of what files you're looking at.
#
# The device and inode numbers uniquely identify a file, but only in a static
# view of the system.  It's very easy for a file to be removed and them another
# file come into existence possessing exactly the same device/inode numbers.
# Since the whole point of our activity here is to handle a dynamically changing
# file, we have to be aware of that sort of thing.
#
# We settle on the following content for the seekfile, with all but the first
# field being optional, for backward compatibility when this revised script is
# first installed at a given site:
#     {file_position} {device#} {inode#} {file_size} {modification_time}
# which will hopefully give us enough information not only to seek to where we
# left off, but also to ensure that we are truly still seeking in the same file.
#
# There are also race conditions that arise from probing file status while
# asynchronous logfile rotation may be occurring.  It is therefore important
# that we only probe the status of open file descriptors, which constitute a
# fixed handle on any given file, rather than probing pathnames and then trying
# to separately manipulate files.

sub do_all_log_reads {
    my $log_file     = shift @_;
    my $seek_file    = shift @_;
    my $rotated_file = shift @_;

    my $seek_position;
    my $seek_file_position;
    my $seek_device;
    my $seek_inode;
    my $seek_file_size;
    my $seek_mod_time;

    # get all the stat info at one time. store it in hash %FILE_STAT_for
    stat_files( \%FILE_STAT_for, $rotated_file, $log_file, $seek_file );

    #   {file_position} {device#} {inode#} {file_size} {modification_time}
    $seek_position = get_seek_position($seek_file);
    if ( $seek_position =~ m/^(\d+)\s(\d+)\s(\d+)\s(\d+)\s(\d+)$/ ) {
	$seek_file_position = $1;
	$seek_device        = $2;
	$seek_inode         = $3;
	$seek_file_size     = $4;
	$seek_mod_time      = $5;
    }
    elsif ( $seek_position =~ m/^(\d+)$/ ) {
	$seek_file_position = $1;
	$seek_device        = -1;
	$seek_inode         = -1;
	$seek_file_size     = -1;
	$seek_mod_time      = -1;
    }
    else {
	$seek_file_position = 0;
	$seek_device        = -1;
	$seek_inode         = -1;
	$seek_file_size     = -1;
	$seek_mod_time      = -1;
    }

    # NOTE:  The current state of this code is that we now place more data into the
    # seek file, but we still only extract the seek_file_position and use that,
    # according to the previously implemented logic.  The new logic to properly
    # analyze all the seek file data is not yet implemented.
    #
    # If we have:
    #	no seek position:
    #		just read the logfile from the beginning
    #	only a file position (transitional state, just after this new copy is installed):
    #		if we have no rotated-logfile name:
    #			.
    #		if we have a rotated-logfile name:
    #			.
    #	full file statistics:
    #		if we have no rotated-logfile name:
    #			if the seek device/inode matches the logfile device/inode:
    #				seek to the given position in the logfile and start reading from there
    #			if the seek device/inode does not match the logfile device/inode:
    #				read the logfile from the beginning
    #		if we have a rotated-logfile name:
    #			if the seek device/inode matches the rotated-logfile device/inode:
    #				seek to the given position in the rotated-logfile and start reading from there,
    #				then read the logfile from the beginning
    #			if the seek device/inode does not match the rotated-logfile device/inode:
    #				if the seek device/inode matches the logfile device/inode:
    #					seek to the given position in the logfile and start reading from there
    #				if the seek device/inode does not match the logfile device/inode:
    #					read the logfile from the beginning
    #				.

    ###################################################################
    # if we have:
    #    a seek_file_position and there is rotated file that is older
    #    than our seek_file. Open the rotated file, seek to the right point
    #    and read in the lines. Set the var to have the whole main logfile read.
    # if I dont have a seek file I ignore a rotated file even if it's passed in.
    if ( $seek_file_position && -f $rotated_file && -f $seek_file ) {
	if ( $FILE_STAT_for{"$rotated_file"}->[$MTIME] >= $FILE_STAT_for{"$seek_file"}->[$MTIME] ) {
	    $main::DEBUG && do {
		warn "The rotated file is younger than the seek file.\n",
		  "That means we are going to:\n",
		  "\tload the rotated file\n",
		  "\tseek to point listed in the seek_file\n",
		  "\tread rotated file from seek to EOF\n",
		  "\topen the logfile\n",
		  "\tread logfile to EOF\n";
	    };

	    $ROTATED_FILE = open_file_and_seek( "$rotated_file", $seek_file_position );

	    # get the info from the rotated file and the set the main log_file to read from the beginning.
	    chomp( @rotated_log_entries = <$ROTATED_FILE> );
	    close $ROTATED_FILE;

	    # set seek_file_position to zero so we don't check log_file length
	    $seek_file_position = 0;
	}
    }

    # double check to make sure our $seek_file_position is not past the end of the $log_file.
    if ( $seek_file_position > $FILE_STAT_for{"$log_file"}->[$SIZE] ) {
	carp qq/"$log_file" is shorter than seek position "$seek_file_position", reading whole file/;
	$LOG_FILE = open_file_and_seek( "$log_file", 0 );
    }
    else {
	$LOG_FILE = open_file_and_seek( "$log_file", "$seek_file_position" );
    }

    chomp( @log_entries = <$LOG_FILE> );

    $main::DEBUG && do {
	warn "the rotated file is older than the seek file\n";
	warn "that means we are going to\n", "\topen the logfile\n", "\tseek to point listed in the seek_file\n", "\tread logfile to EOF\n",;
    };

    # create a single array with all entries.
    if (@rotated_log_entries) {
	unshift @log_entries, @rotated_log_entries;
    }

    ## DEBUG - remove before prod. tony
    $main::DEBUG && do {
	open( OUTFILE, '>', "outfile.$$" ) or die "ERROR: outfile.$$: $!\n";
	print OUTFILE join "\n", @log_entries, "\n";
	close OUTFILE;
    };

    write_file_position( $LOG_FILE, $seek_file );
    close $LOG_FILE;

    wantarray ? return @log_entries : \@log_entries;
}

##############################

sub write_file_position {
    my $FILE          = shift @_;
    my $seek_file     = shift @_;
    my $file_position = tell $FILE;
    my $device;
    my $inode;
    my $mode;
    my $link_count;
    my $uid;
    my $gid;
    my $rdev;
    my $file_size;
    my $acc_time;
    my $mod_time;

    ( $device, $inode, $mode, $link_count, $uid, $gid, $rdev, $file_size, $acc_time, $mod_time ) = stat $FILE;

    open( SEEK_FILE, '>', "$seek_file" ) or die "ERROR: $seek_file: $!\n";
    print SEEK_FILE "$file_position $device $inode $file_size $mod_time\n";
    close SEEK_FILE;
}

##############################

sub stat_files {
    my $hash_ref = shift;
    my (@file_list) = @_;

    # get all the stat info at one time. store it in hash %hash
    for my $file (@file_list) {

	# if file exists stat it
	if ( -f $file ) {
	    ( @{ $hash_ref->{$file} } ) = stat $file;
	}
	else {
	    ( @{ $hash_ref->{$file} } ) = [];
	}
    }
}

##############################

# open the seekfile and get the position, if it does not exist return 0 which means read the whole file.
sub get_seek_position {
    use strict;

    my $seek_file = shift @_;
    my @seek_pos;

    # Try to open log seek file.  If open fails, we seek from beginning of the
    # file by default.
    if ( -f $seek_file ) {
	open( SEEK_FILE, '<', $seek_file ) || do {
	    carp qq/$!: cannot open seek file "$seek_file", reading whole logfile/;

	    # customer might want us to die here - can change;
	    return 0;
	};
	chomp( @seek_pos = <SEEK_FILE> );
	close(SEEK_FILE);

	# FIX THIS:  review this logic
	#  If file is empty, no need to seek...
	$seek_pos[0] <= 0 ? return 0 : return $seek_pos[0];
    }
    else {
	carp qq/$!: seek file "$seek_file" does not exist, reading whole logfile/;
	return 0;
    }
}

##############################

# open and file and seek to the correct position. returns a file handle that is ready to read from.
sub open_file_and_seek {
    my $file       = shift;
    my $seek_bytes = shift;

    $fh = new IO::File "< $file" or die "ERROR: $file: $!\n";
    seek( $fh, $seek_bytes, 0 );
    return $fh;
}

######################################################################
# Open log file

# $Header: /home/tony/work/groundwork/shaw_satellite/RCS/check_syslog_gw.pl,v 1.23 2009/01/13 02:04:50 tony Exp $
# $Author: tony $
# $Date: 2009/05/10 02:26:21 $
#
# $Log: check_syslog_gw.pl,v $
# Revision 1.25  2009/05/11 11:20:30  gherteg
# Cut down the socket-open time even further.
# Use -r for the rotated logfile, as the usage says, instead of -f.
# Further improvements to efficiency, error handling, and usage message.
#
# Revision 1.24  2009/05/10 02:04:50  gherteg
# Eliminated the forking used to send results to Nagios.
# Also, now we only keep the socket to Foundation open as long as needed to send the results.
#
# Revision 1.23  2009/01/13 02:04:50  tony
# I was import $TIMEOUT from the nagios 'utils.pm' which is set for 15 seconds. Removed the import.
# Now -T requires an integer arguement and is case sensitive and $TIMEOUT is not imported from anywhere.
# -Tony
#
# Revision 1.22  2009/01/13 01:58:47  tony
# made the commnand line arguements case sensitive and changed the -T option to require a integer.
#
# Revision 1.21  2009/01/13 01:34:10  tony
# new version setup here
# -Tony
#
# Revision 1.1  2009/01/13 00:45:22  tony
# Initial revision
#
# Revision 1.8  2009/01/06 19:11:32  tony
# updated more usage.
# -Tony
#
# Revision 1.7  2009/01/06 19:04:42  tony
# Added -T option and updated the usage.
# -Tony
#
# Revision 1.6  2008/12/29 23:00:31  tony
# tiny bit more cleanup.
# -Tony
#
# Revision 1.5  2008/12/27 00:30:01  tony
# little more cleanup
#
# Revision 1.4  2008/12/25 20:45:33  tony
# cleaned up, commented, just about totally done. -Tony
#
# Revision 1.3  2008/12/24 19:45:03  tony
# factored the code so that the matching is done in the LIST_MATCH object.
# -Tony
#
# Revision 1.2  2008/12/23 21:07:25  tony
# working - sending to Dave as version 3.
#
# Revision 1.1  2008/12/23 15:52:50  tony
# Initial revision
#
# Revision 1.1  2008/12/23 15:46:17  tony
# Initial revision
#
# Revision 1.19  2008/12/17 22:17:21  tony
# version 2 I sent to Dr Dave
#
# Revision 1.18  2008/12/17 18:58:39  tony
# cleaned up a little stuff.
# onto handleing the -g option.
# -Tony
#
# Revision 1.17  2008/12/16 20:34:36  tony
# version i sent to Dave Blunt on 12/16
#
# Revision 1.16  2008/12/16 07:58:01  tony
# cleaned up all the debug to write to stderr.
# -Tony
#
# Revision 1.15  2008/12/16 07:50:48  tony
# About ready to put a bow on it.
# -Tony
#
# Revision 1.14  2008/12/16 07:16:09  tony
# working - gone through the whole thing.
# -Tony
#
# Revision 1.13  2008/12/16 07:07:14  tony
# -T
#
# Revision 1.12  2008/12/16 06:51:54  tony
# working good - lots of clean up.
# -Tony
#
# Revision 1.11  2008/12/16 05:18:36  tony
# right before pulling sub print_gwlog_summary out the code. It's commented out and it's going.
# -Tony
#
# tony-ws:shaw_satellite> grep print_gwlog_summary factory_check_syslog_gw.pl
# #print_gwlog_summary($serverip,$host);
# sub print_gwlog_summary {
#
# Revision 1.10  2008/12/16 00:35:39  tony
# working. heading out.
#
# Revision 1.9  2008/12/16 00:22:18  tony
# foo
#
# Revision 1.8  2008/12/13 02:11:14  tony
# works - going to class.
# -tony
#
# Revision 1.7  2008/12/13 02:07:05  tony
# broke.
# -Tony
#
# Revision 1.6  2008/12/11 18:55:48  tony
# going to change all CRITICAL,WARNING, OK to full names:
# -Tony
#
# Revision 1.5  2008/12/11 03:43:37  tony
# working.
# -Tony
#
# Revision 1.4  2008/12/10 19:59:57  tony
# may be broken, sytax error around 250
# -Tony
#
# Revision 1.3  2008/12/10 18:44:34  tony
# working.
# -T
#
# Revision 1.2  2008/12/09 22:51:45  tony
# added in the LOG_SEIVE section - checking in, not tested.
#
# Revision 1.1  2008/12/09 21:44:08  tony
# Initial revision
#
# Revision 1.7  2008/12/09 00:03:57  tony
# gold2 - has some debug descrimination.
# -Tony
#
# Revision 1.6  2008/12/08 23:57:09  tony
# gold copy - works.
# -tony
#
# Revision 1.5  2008/12/08 21:34:46  tony
# -t
#
# Revision 1.4  2008/12/08 20:32:38  tony
# done, doing checkout to make sure it works.
#
# -tony
#
# Revision 1.3  2008/12/06 00:51:05  tony
# works but not fully functional.
# -Tony
#
# Revision 1.2  2008/12/06 00:44:18  tony
# after running thru perltidy.
# -Tony
# $cc$

=pod

 Not all fields are supported on all filesystem types.  Here are
	       the meanings of the fields:

		 0 dev      device number of filesystem
		 1 ino      inode number
		 2 mode     file mode  (type and permissions)
		 3 nlink    number of (hard) links to the file
		 4 uid      numeric user ID of file's owner
		 5 gid      numeric group ID of file's owner
		 6 rdev     the device identifier (special files only)
		 7 size     total size of file, in bytes
		 8 atime    last access time in seconds since the epoch
		 9 mtime    last modify time in seconds since the epoch
		10 ctime    inode change time in seconds since the epoch (*)
		11 blksize  preferred block size for file system I/O
		12 blocks   actual number of blocks allocated

=cut

1;    ## <- critical for end-of-package

##############################
####### NEW PACKAGE DEF ######
##############################

package LOG_SEIVE;

use Data::Dumper;

sub do_all_log_matching {

    # list of category of matches - can expand here and the rest of the program will do
    # the right thing. DO NOT 'my' this array - it causes the 'eval' behavior not to work.
    # tony - 12/03/08
    # example of how it's called.
    #     $match_obj_hash_ref = LOG_SEIVE::do_all_log_matching(
    #         \@matching_category,
    #         \@raw_syslog_lines,
    #         $critical_regx_file,
    #         $warning_regx_file,
    #         $ok_regx_file,
    #         $ignore_regx_file,
    #         $matches_outfile,
    #     );

    my $matching_category_ref = shift @_;
    my @matching_category     = @{$matching_category_ref};

    my $raw_syslog_arr_ref = shift @_;

    # take the passed in array ref and copy it locally
    my @syslog_entry_list = @{$raw_syslog_arr_ref};

    my $critical_regx_file = shift @_;
    my $warning_regx_file  = shift @_;
    my $ok_regx_file       = shift @_;
    my $ignore_regx_file   = shift @_;
    my $matches_outfile    = shift @_;

    my %match_objects;

    for my $category (@matching_category) {
	my $match_object_ref;

	# to make the code flexible and able to take new arbitrary
	# categories as they are added to %match_objects create the
	# set of LIST_MATCH objects from the array.

	# the new anon object on the hash
	# no arrow operator here
	$match_objects{$category} = new LIST_MATCH "$category";
	$match_object_ref = $match_objects{$category};

	# make an easy-to-use reference for each type of object. CAVEAT
	# PROGRAMMER - this eval autovivifies each of these in existence:
	# $CRITICAL $WARNING $OK $IGNORE $NOT_MATCHED; you will see them
	# referenced below -Tony
	${ eval $category } = $match_object_ref;
    }

    $critical_regx_file && $CRITICAL->read_regexp_file("$critical_regx_file");
    $warning_regx_file  && $WARNING->read_regexp_file("$warning_regx_file");
    $ok_regx_file       && $OK->read_regexp_file("$ok_regx_file");
    $ignore_regx_file   && $IGNORE->read_regexp_file("$ignore_regx_file");

    my $match_object_ref;
    my $category;
    my $syslog_line;
    my @temp_not_matched;

  MATCHING_CATEGORY: for $category (@matching_category) {

	# get the match object with regexps and container for matched lines
	$match_object_ref = $match_objects{$category};

	# not even bothering to see what we were asked to do on the commandline - if
	# there are regexps in the array, we are doing matching.
	# skip the category if there are not regexps listed.
	next MATCHING_CATEGORY if not scalar( $match_object_ref->get_regexp_array() );

	# the second arg is an array to put not-matched lines in. the
	# matches are written to the object and managed from there.
	$match_object_ref->do_matching( \@syslog_entry_list, \@temp_not_matched );

	# set @syslog_entry_list and the NOT_MATCHED object to lines from this run
	# that did not match anything. @temp_not_matched is filled in LIST_MATCH::do_matching.
	@syslog_entry_list = @{ $NOT_MATCHED->{matches} } = @temp_not_matched;

	# clear the array for the next run
	@temp_not_matched = ();

	# quit the loop if we've run out of lines to match.
	@syslog_entry_list or last MATCHING_CATEGORY;
    }

    $main::DEBUG and do {
	for my $category (@matching_category) {
	    warn "DEBUG writing category [$category] to $ {matches_outfile}_$ {category}\n";
	    $match_objects{$category}->write_matches("${matches_outfile}_$category");
	}
	$NOT_MATCHED->write_matches("not_matched.txt");
    };

    # this will write the match_objects hash so a programmer can see
    # all the moving pieces in one place
    $main::DEBUG and do {
	if ( !open( HASH_DUMP, '>', "hash_dump.txt" ) ) {
	    warn "hash_dump.txt: $!";
	}
	else {
	    print HASH_DUMP Data::Dumper->Dump( [ \%match_objects ] );
	    close HASH_DUMP;
	}
    };

    # give back a ref to the hash of objects we just populated.
    return \%match_objects;
}

1;    ## <<=== end package LOG_SEIVE

##############################
####### NEW PACKAGE DEF ######
##############################

package LIST_MATCH;
my $VERSION = "1.1";

##############################

# requires one args, can take 3 ["Match_category_name", "regexfile", arr_ref to match against]
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $this  = {};
    bless( $this, $class );

    # init me
    $this->_init(shift);
    return $this;
}

##############################

sub _init {

    my $this = shift;
    if (@_) {
	$this->{'NAME'} = shift;
    }
    $this->{'regexp_array'} = [];
    $this->{'regexp_file'}  = '';
    $this->{'input_file'}   = '';
    $this->{'matches'}      = [];
}

sub list_to_read { }

##############################

sub read_regexp_file ($) {

    # shell/perl comment line regexp
    my $hash_comment_line_regexp = qr{
			       \A    # from the beginning of the string
			       \s*   # any number of spaces type char
			       \#    # a hash-sign
			   }xms;

    my $this = shift;
    $this->{'regexp_file'} = shift @_;
    open( my $FILE, '<', $this->regexp_file ) or die "ERROR: " . $this->regexp_file . ": $!\n";
    while ( my $line = <$FILE> ) {
	next if $line =~ m/$hash_comment_line_regexp/;
	chomp $line;
	push @{ $this->regexp_array }, qr/$line/;
    }
    close $FILE;
}

##############################

sub get_regexp_array () {
    my $this = shift @_;
    return @{ $this->regexp_array };
}

##############################

sub put_regexp_array (\@) {
    my $this = shift @_;
    push @{ $this->regexp_array }, @_;
    return $#{ $this->regexp_array };
}

##############################

sub put_match(\@) {
    my $this = shift @_;
    push @{ $this->matches }, @_;
}

##############################

sub get_matches() {
    my $this = shift @_;
    wantarray ? return @{ $this->matches } : \@{ $this->matches };
}

##############################

sub write_matches($) {
    my $this             = shift @_;
    my $file_to_write_to = shift @_;

    # does an append!
    if ( !open( FILE, '>>', "$file_to_write_to" ) ) {
	warn "$file_to_write_to: $!";
    }
    else {
	## have the trailing '' because the file was not getting the final newline -- this fixed it
	print FILE join "\n", $this->get_matches(), '';
	close FILE;
    }
}

##############################

sub count_matches () {
    my $this = shift @_;
    return scalar @{ $this->matches };

}

##############################

# one arg required: the list of line to match regexps against.
# two is optional - an array ref to put the lines that did not match into.
# it puts matched lines into its obj structure
sub do_matching (\@) {
    my $this                  = shift @_;
    my $syslog_entry_list_ref = shift @_;
    my $non_matched_ref       = shift @_;
    my $fill_non_matched      = 0;

    if ( ref($non_matched_ref) eq "ARRAY" ) {
	$fill_non_matched = 1;
    }

    my $syslog_line;

    next MATCHING_CATEGORY if not scalar( $this->get_regexp_array() );

    # for each line in our passed in syslog
  SYSLOG_LINE: for $syslog_line ( @{$syslog_entry_list_ref} ) {

	# get all regexps for the category and test against the line
      REGEXP_ITERATION: for my $regexp ( $this->get_regexp_array() ) {

	    # if the line matches
	    if ( $syslog_line =~ /$regexp/ ) {
		$main::DEBUG && warn "DEBUG $category: $syslog_line\n";

		# put the line in the match_objects container
		$this->put_match($syslog_line);
		next SYSLOG_LINE;
	    }
	}

	# if we make it here the syslog_line did not match anything in
	# this category, so add it to the unmatched lines
	$fill_non_matched && push @{$non_matched_ref}, $syslog_line;
    }
}

##############################

sub AUTOLOAD {
    no strict;
    my ($this)    = shift;
    my ($program) = $AUTOLOAD;

    # p 112 of adv. perl says:
    # Never propagate DESTROY methods
    return if $AUTOLOAD =~ /::DESTROY$/;
    $program =~ s/^.*:://;
    scalar return $this->{$program};
}

=pod

data struct picture.

    $Match_object = {
	'WARNING' => bless( {
	    'input_file' => '',
	    'NAME' => 'WARNING',
	    'matches' => [
		'Dec  2 18:44:20 gwos1 kernel: SELinux:  Initializing.',
		'Dec  2 18:44:21 gwos1 kernel: SELinux:  Disabled at runtime.'
	    ],
	    'regexp_file' => '/home/tony/work/groundwork/shaw_satellite/regex_files/WARNING',
	    'regexp_array' => [
		qr/(?-xism:year)/,
		qr/(?-xism:wisdom)/,
		qr/(?-xism:walked)/,
		qr/(?-xism:SELinux)/
	    ]
	}, 'LIST_MATCH' )

=cut

1;

###############
# $Header@#$
# $Author@#$
# $Date@#$
#
# $Log@#$
###############

