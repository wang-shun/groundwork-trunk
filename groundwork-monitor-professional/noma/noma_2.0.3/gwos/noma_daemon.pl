#!/usr/local/groundwork/perl/bin/perl -w

# nagios: -epn

# COPYRIGHT:
#
# This software is Copyright (c) 2007-2009 NETWAYS GmbH, Christian Doebler
#                 some parts (c) 2009      NETWAYS GmbH, William Preston
#                                <support@netways.de>
#
# (Except where explicitly superseded by other copyright notices)
#
#
# LICENSE:
#
# This work is made available to you under the terms of Version 2 of
# the GNU General Public License. A copy of that license should have
# been provided with this software, but in any event can be snarfed
# from http://www.fsf.org.
#
# This work is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301 or visit their web page on the internet at
# http://www.fsf.org.
#
#
# CONTRIBUTION SUBMISSION POLICY:
#
# (The following paragraph is not intended to limit the rights granted
# to you to modify and distribute this software under the terms of
# the GNU General Public License and is only of importance to you if
# you choose to contribute your changes and enhancements to the
# community by submitting them to NETWAYS GmbH.)
#
# By intentionally submitting any modifications, corrections or
# derivatives to this work, or any other work intended for use with
# this Software, to NETWAYS GmbH, you confirm that
# you are the copyright holder for those contributions and you grant
# NETWAYS GmbH a nonexclusive, worldwide, irrevocable,
# royalty-free, perpetual, license to use, copy, create derivative
# works based on those contributions, and sublicense and distribute
# those contributions and any derivatives thereof.
#
# Nagios and the Nagios logo are registered trademarks of Ethan Galstad.

=head1 NAME

noma_daemon.pl  -  NETWAYS Notification Manager - Daemon

=head1 SYNOPSIS

=head1 OPTIONS

This script uses a configuration file

=item   B<--nodaemonize>

run NoMa in the foreground (for debugging, when a perl error is suspected)

=back

=head1 CAVEATS

This script uses Thread Queues which may have memory leak problems in Perl versions
< 5.8.1

Note that your DateTime::TimeZone package may have old time zone data in it, since the
perl zones are maintained separately from the system timezones!

If using MySQL Replication setups, be aware of the "ON DUPLICATE KEY UPDATE" bug.
As a workaround you could use "REPLACE" -> code changed to do select / update


=cut

use strict;
use warnings;

die
"WARNING: Your Perl is too old. I cannot guarantee that NoMa will be stable. Comment out this check if you want to continue anyway"
  if ( $] and $] < 5.008001 );

use Getopt::Long;
use Pod::Usage;
use POSIX;
use Digest::MD5 qw(md5_hex);
use FindBin;
use lib "$FindBin::Bin";
use lib "$FindBin::Bin".'/lib';
use thread_procs;
use escalations;
use bundler;
use array_hash;
use contacts;
use database;
use downtime;
use time_frames;
use debug;
use datetime;

use Data::Dumper;
$Data::Dumper::Sortkeys = 1;
# use threads ('yield', 'stack_size' => 16*4096);
use threads;
use Thread::Queue;
use IO::Select;
use Fcntl qw(O_RDWR);
use IO::Socket;
use YAML::Syck;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::Strptime;
use Storable;

# use Proc::ProcessTable;
use DBI;

our $processStart = time();
our %suppressionHash;
my $versionStr = 'current (2.0.3)';

my %stati_service = (
    'OK'       => 'on_ok',
    'WARNING'  => 'on_warning',
    'CRITICAL' => 'on_critical',
    'UNKNOWN'  => 'on_unknown'
);

# Used for state transitins
my %stati_previous = (
    'OK'       => 2,
    'WARNING'  => 4,
    'CRITICAL' => 8,
    'UNKNOWN'  => 16
);
my %stati_host = (
    'UP'          => 'on_host_up',
    'UNREACHABLE' => 'on_host_unreachable',
    'DOWN'        => 'on_host_down'
);
my %stati_type = (
    'PROBLEM'   	=> 'on_type_problem',
    'RECOVERY'		=> 'on_type_recovery',
    'DOWNTIMESTART'     => 'on_type_downtimestart',
    'DOWNTIMEEND'       => 'on_type_downtimeend',
    'DOWNTIMECANCELLED' => 'on_type_downtimecancelled',
    'FLAPPINGSTART'     => 'on_type_flappingstart',
    'FLAPPINGSTOP'      => 'on_type_flappingstop',
    'FLAPPINGDISABLED'  => 'on_type_flappingdisabled',
    'ACKNOWLEDGEMENT'   => 'on_type_acknowledgement',
    'CUSTOM'            => 'on_type_custom'
);
my %check_type_str = (
    'h' => 'Host',
    's' => 'Service',
    ''  => '',
);

my $recipients        = '';
my $host              = '';
my $host_alias        = '';
my $host_address      = '';
my $service           = '';
my $check_type        = '';
my $status            = '';
my $datetime          = '';
my $output            = '';
my $notification_type = '';
my $verbose           = undef;
my $version           = undef;	# command option
my $help              = undef;	# command option

my $query               = '';
my $notificationCounter = 0;
my $notifierPID         = 0;
my $notifierUser        = 'nagios';
my $notifierBin         = 'noma_notifier.pl';
my $notifierConfig	= '/usr/local/groundwork/noma/etc/NoMa.yaml';
my $now                 = 0;

my $reloop_delay    = 1;
my $acknowledged    = 0;
my $loop_until_ack  = 0;
my $sleep           = 0;
my $keep_on_looping = 1;
our $ignore         = 0;

my $log_count = 0;
my @triesPerID;
my $additional_run          = 0;
my $whoami     = 'notifier';

our $conf = LoadFile($notifierConfig);
my $cache = $conf->{path}->{cache};

my $debug = $conf->{debug}->{logging};
my $debug_queries = $conf->{debug}->{queries};
my $do_not_send   = undef;
my $debug_file = $conf->{debug}->{file};
my $paramlog = $conf->{debug}->{paramlog};
my $daemonize = $conf->{debug}->{daemonize};
my $pidfile = $conf->{path}->{pidfile};
$ignore = $conf->{escalator}{internalEscalation}
    if (defined($conf->{escalator}{internalEscalation}));
my $nap_time = $conf->{notifier}->{nap_time};
$nap_time = 1 if not defined $nap_time;
my $sleep_time = $conf->{notifier}->{sleep_time};


our %queue;                          # thread message queues
our %thread;                         # thread hash

##############################################################################
# HANDLING OF COMMAND-LINE PARAMETERS
##############################################################################

# log all command-line parameters
if ( defined($paramlog) )
{
    open( OUT, ">> $paramlog" );
    print OUT '[' . localtime() . ']  ' . join( ' ', @ARGV ) . "\n";
    close(OUT);
}

# TODO MySQL cache
# open(LOG, "+< $cache") or die "Offline cache file cannot be created";
# close(LOG);

Getopt::Long::Configure('bundling');
my $clps = GetOptions(

    "V|version"  => \$version,
    "h|help"     => \$help,
    "daemonize!" => \$daemonize

);

# display help?
if ( defined($help) )
{
    pod2usage( -verbose => 1 );
    exit(0);
}

# print version?
if ( defined($version) )
{
    print 'Version: ' . $versionStr . "\n";
    exit 0;
}

if(!($<))
# it may be better here to use the following
# if(!($>))
{
	die "This script should not be run as root";
}

if ( defined($daemonize) and $daemonize == 1)
{
	# fork, etc.
	my $parent;
	defined($parent = fork()) or die "Failed to fork";
	exit(0) if $parent;
	chdir('/');
    # create our PID file here
    # don't check that we are already running - that is a job for the init script
    if (defined($pidfile))
    {
        open (PID, ">$pidfile") or die "Can't create PIDfile $pidfile";
        print PID "$$\n";
        close(PID);
    }
	setsid();
	close(STDIN);
	close(STDOUT);
	close(STDERR);
}

# TODO: read escalations in and push into Queue???
# deleteFromEscalations();

##############################################################################
# NOMA STARTUP LOG
##############################################################################
debug('NoMa '.$versionStr.' starting @ '.localtime, 1);

##############################################################################
# DATABASE VERSION VERIFICATION
##############################################################################
# Check version before anything else connects to the database!
my $expecteddbversion = '2000'; # Do NOT change this value!
if (dbVersion($expecteddbversion,0) ne $expecteddbversion){
	print("Wrong schema version!! Please upgrade DB schema, see documentation and log for more information...");
        debug( ' Schema is wrong version: '.dbVersion($expecteddbversion,0).' expected: '.$expecteddbversion, 1);
        exit;
}


##############################################################################
# THREAD CREATION
##############################################################################

# create queues for replies from notifier plugins, commands, and escalations
my $msgq = Thread::Queue->new;
my $cmdq = Thread::Queue->new;
my $escq = Thread::Queue->new;
my $dbquery = Thread::Queue->new;
my $dbreply = Thread::Queue->new;

# create a thread for each notification type

foreach my $method ( getMethods() )
{

    my $proc = $$method{'command'};

    $queue{$proc} = Thread::Queue->new;
    $thread{$proc} =
      threads->new( \&spawnNotifierThread, $queue{$proc}, $msgq, $proc,
        $conf->{command}{$proc} );
    debug( 'spawned ' . $proc . ' with ID ' . $thread{$proc}->tid, 1 );
}

# the escalation thread (for internal escalation)
$thread{'escalator'} =
    threads->new(\&spawnEscalationThread, $cmdq, $escq);

if ($conf->{input}{pipeEnabled})
{
	$thread{'commandPipeThread'} =
	  threads->new(\&spawnCommandPipeThread, $cmdq, $conf->{input});
}

if ($conf->{input}{socketEnabled})
{
	$thread{'commandSocketThread'} =
	  threads->new(\&spawnCommandSocketThread, $cmdq, $conf->{input});
}

if ($conf->{debug}{watchdogEnabled})
{
	$thread{'watchdogThread'} =
	  threads->new(\&spawnWatchdogThread, $conf->{debug});
}

if (0==1)
{
	$thread{'bundlerThread'} =
   threads->new(\&spawnBundlerThread, \%queue, $conf->{notifier});
}

my $cmd;
my $msg;

##############################################################################
# GLOBAL LOOP
##############################################################################
do
{

    if ( $cmd = $cmdq->dequeue_nb )
    {
        {
            debug( 'processing command ' . $cmd , 1);

            my %cmdh = parseCommand($cmd);
            if (!%cmdh)
            {
                debug("Ignoring invalid command $cmd", 1);
                next;
            }
            next if ( !defined $cmdh{host} or $cmdh{host} eq '');
            debug(debugHash(%cmdh), 2);
#                 "host = $host, incident_id = $incident_id, host_alias = $host_alias, host_address = $host_address, service = $service, check_type = $check_type, status = $status, datetime = $datetime, notification_type = $notification_type, output = $output"

            # hosts and services in lower case
            #$cmdh{host} = lc($cmdh{host});
            #$cmdh{service} = lc($cmdh{service}) if ( $cmdh{check_type} eq 's' );

##############################################################################
            # CHECK IF HOST / SERVICE IN DOWNTIME
##############################################################################

            if ( getInDowntime( $cmdh{host}, $cmdh{service} ) ) {
                debug( "Suppress Notification because " . $cmdh{host} . ( $cmdh{service} ne '' ? '/' . $cmdh{service} : '' ) . " is having Downtime", 1 );
                next;
            }
            else {
                debug( "No Downtime for " . $cmdh{host} . ( $cmdh{service} ne '' ? '/' . $cmdh{service} : '' ), 1 );
            }

##############################################################################
            # GENERATE LIST OF CONTACTS TO NOTIFY
##############################################################################

            # TODO DB not reachable? - add cacheing code
            #




            # retrieve the previous check result to allow for transition notifications
            # e.g. WARNING -> CRITICAL
            my $last_state = getLastState($cmdh{host}, $cmdh{service});

            # generate query and get list of possible users to notify
            my $query =
            'SELECT id,recipients_include,recipients_exclude,hosts_include,hosts_exclude,hostgroups_include,hostgroups_exclude,services_include,services_exclude,servicegroups_include,servicegroups_exclude FROM notifications';
            if ( $cmdh{check_type} eq 'h' )
            {
                $query .= ' where ' . $stati_host{$cmdh{status}} . '=\'1\'';
            } elsif ( $cmdh{notification_type} eq 'PROBLEM' || $cmdh{notification_type} eq 'RECOVERY')
            {
                ## Outer enclosing parenthesization is critical here because these clauses will be
                ## followed by an AND clause for checking that the selected rule is active, and that
                ## additional clause is supposed to apply uniformly across all of the ORed clauses here.
                $query .= ' where ( (' . $stati_service{$cmdh{status}} . ' & 1) = 1';
                $query .= ' OR (' . $stati_service{$cmdh{status}} . ' & ' .$stati_previous{$last_state} .') = ' .$stati_previous{$last_state} . ' )';
            } else
            {
                $query .= ' where (' . $stati_service{$cmdh{status}} . ' & 1) = 1';
            }

            # only active rules!
            $query .= ' and active=\'1\'';
            my %dbResult = queryDB($query);

            # Suppress double of same method to same contact
            my %sentList = ();

            # filter out unneeded users by using exclude lists
            my @ids_all =
	    generateNotificationList( $cmdh{check_type}, $cmdh{recipients}, $cmdh{host},  $cmdh{service}, $cmdh{hostgroups}, $cmdh{servicegroups},
                %dbResult );
            debug( 'Rule IDs collected (unfiltered): ' . join( '|', @ids_all ), 2 );

            unless ($cmdh{status} eq 'OK' || $cmdh{status} eq 'UP' || $cmdh{notification_type} eq 'ACKNOWLEDGEMENT' || $cmdh{notification_type} eq 'CUSTOM')
            {
                if (scalar(@ids_all) < 1)
		{
			# deleteFromCommands($cmdh{external_id});
			next;
		}
            }
            # We need to split the rules into 2 types
            # those that escalate internally - and normal rules
            #

            my @ids =();
            my @contactsArr = ();

            # first handle normal rules
            debug("now handling normal rules", 2);
            ############ NORMAL RULES ####################

            # only consider "real" alerts
            if ($cmdh{operation} eq 'notification')
            {
                @ids = getUnhandledRules(\@ids_all);
                debug( 'Unhandled(normal) rule IDs (unfiltered): ' . join( '|', @ids ), 2 );


                $notificationCounter = getNotificationCounter($cmdh{host}, $cmdh{service});
                debug("Counter from notification_stati for $cmdh{host} / $cmdh{service} is $notificationCounter", 2);

                if ($notificationCounter > 0)
                {
                    # notification already active
                    debug('-> already active', 2);

                    ## TODO: escalation handled internally - ignore it here

                    if ($cmdh{status} eq 'OK' || $cmdh{status} eq 'UP')
                    {
                        # clear counter
                        #
                        debug('  -> Clearing counter', 2);
                        clearNotificationCounter($cmdh{host}, $cmdh{service});
                        clearEscalationCounter($cmdh{host}, $cmdh{service});
                        deleteFromActiveByName($cmdh{host}, $cmdh{service});
                    }
                    elsif ($cmdh{notification_type} eq 'ACKNOWLEDGEMENT' || $cmdh{notification_type} eq 'CUSTOM')
                    {
                        # clear counter
                        #
                        debug('  -> Acknowledgement or custom notification, Clear counters', 2);
                        clearNotificationCounter($cmdh{host}, $cmdh{service});
                        clearEscalationCounter($cmdh{host}, $cmdh{service});
                        deleteFromActiveByName($cmdh{host}, $cmdh{service});
                    }
                    else
                    {
                        # increment counter
                        debug('  -> Incrementing counter', 2);
                        $notificationCounter =
                            incrementNotificationCounter( $cmdh{status}, $cmdh{host}, $cmdh{service},$cmdh{check_type});
                    }
                } else {
                    # notification returned 0
                    if ($cmdh{status} eq 'OK' || $cmdh{status} eq 'UP')
                    {
                        ## The original code here reset $notificationCounter to 1 in an attempt to match
                        ## some notificaton rule, to try to get the recovery notification out to somebody.

                        # debug('Received recovery for a problem we never saw - will try to match against notification no. 1', 2);
                        # $notificationCounter = 1;

                        # But in fact, that reasoning and the correction are specious.  There are really
                        # only three possible ways we can get to this point:
                        #
                        # (1) We somehow missed a previous alert for the non-UP/OK state for which this is a
                        #     recovery.
                        #
                        # (2) We did receive the previous alert for the non-UP/OK state for which this is a
                        #     recovery, and we processed it enough to send out some notification, but somehow
                        #     the database has been cleaned out since then and we no longer remember that fact.
                        #
                        # (3) We did receive a previous alert for the non-UP/OK state for which this is a
                        #     recovery, but there was no notification rule that said we should send out a
                        #     notification for that state.
                        #
                        # It's that third case that reveals the previous logic here as bogus.  For instance,
                        # you can have a notification rule that only fires for OK and CRITICAL alerts, but
                        # not for WARNING states.  If you get only a series of WARNING alerts followed by a
                        # final OK alert, everything prior was properly processed (no notifications went out).
                        # But in this case, you DO NOT want a recovery notice to go out, since nothing was
                        # complained about.  That would just be needlessly verbose and confusing.  In this
                        # case, the debug message in the original code here ("a problem we never saw") is not
                        # actually correct.
                        #
                        # So the situation is, if case (1) or (2) holds, there's really nothing much we should
                        # be doing; it's too late now.  So no correction is called for in those cases.  And if
                        # case (3) holds, we most definitely don't want to send out a recovery notification,
                        # because it would just be a continuing annoyance with the setup described.  That very
                        # real possibility, not involving any prior system failure or unexpected cleanup, has
                        # to be allowed for and operate correctly.
                        #
                        # Consequently, we have disabled the original logic in this branch.  However, this
                        # code change does not completely prevent the user from receiving notifications
                        # in this situation.  If that is really what is desired, it can be enabled on a
                        # per-notification-rule basis by specifying the special value 0 as one of the "Notify
                        # after # number of notifications" numbers in the rule configuration.  It's best to
                        # do that using a separate digit, not as part of a range.  That will effectively
                        # re-enable the generation of a notification for the situation covered by this branch.
                        #
                        # That all said, for debugging purposes we won't be completely silent here.  We
                        # simply change the debug message to accurately describe the situation (ignoring the
                        # exceptional/unlikely possibility of case (2)).
                        #
                        debug('Received recovery for a problem we either never saw or previously ignored.', 2);
                    }
                    elsif ($cmdh{notification_type} eq 'ACKNOWLEDGEMENT' || $cmdh{notification_type} eq 'CUSTOM')
                    {
                        debug('Acknowledgement or custom notification for a problem we never saw - will try to match against notification no. 1', 2);
                        $notificationCounter = 1;
                    }
                    else
                    {
                        debug('-> setting to active', 2);
                        $notificationCounter =
                            incrementNotificationCounter( $cmdh{status}, $cmdh{host}, $cmdh{service}, $cmdh{check_type}, $cmdh{tmp_commands_id}, \$cmdh{external_id} );
                    }
                }
                # no matches!?
                my $idCount = @ids;
                if ( $idCount < 1 )
                {
                    debug('No rule matches!', 2);
                    # TODO: clear stati??
                }
                else
                {
                    debug($idCount.' rules matched', 2);

                    # do we need to rollover the counter?
                    # the various rules may rollover at different times, so handle them individually
                    foreach my $ruleid (@ids)
                    {
                        my @id_arr;
                        push @id_arr, $ruleid;
                        # - this is a local check
                        my $rolloverCounter = counterExceededMax( \@id_arr, $notificationCounter );
                        debug( "No more alerts are possible for rule $ruleid; rolling over the counter", 2 ) if $rolloverCounter;

                        # get contact data
                        push @contactsArr,
                          getContacts( \@id_arr, $rolloverCounter || $notificationCounter, $cmdh{status}, $cmdh{notification_type}, $cmdh{external_id}, $rolloverCounter );
                    }
                }
            }
            # now handle escalation rules
            debug("now handling internal escalation rules", 2);
            ############ ESCALATION RULES ####################

            @ids = getHandledRules(\@ids_all);
            debug( 'Handled by NoMa(internal escalation) rule IDs (unfiltered): ' . join( '|', @ids ), 2);

            # the various rules may be at different stages, so handle them individually
            foreach my $esc_rule (@ids)
            {
                my @esc_arr;
                push @esc_arr, $esc_rule;
                debug("looking at rule $esc_rule", 3);
                $notificationCounter = getEscalationCounter($cmdh{host}, $cmdh{service}, $esc_rule);

                if ($notificationCounter > 0)
                {
                    # notification already active
                    debug("rule $esc_rule is currently escalating", 3);
                    incrementEscalationCounter($cmdh{host}, $cmdh{service}, $esc_rule);
                    $notificationCounter += 1;

                    # is this a faked alert? otherwise ignore it!
                    if ($cmdh{operation} eq 'escalation')
                    {
                        debug("rule $esc_rule is faked - checking for overflow", 3);
                        # $notificationCounter = resetEscalationCounter($cmdh{host}, $cmdh{service}, $esc_rule)
                        my $rolloverCounter = counterExceededMax(\@esc_arr, $notificationCounter);

                        push @contactsArr,
                          getContacts( \@esc_arr, $rolloverCounter || $notificationCounter, $cmdh{status}, $cmdh{notification_type}, $cmdh{external_id}, $rolloverCounter );
                    }

                }
                elsif ($cmdh{status} ne 'OK' and $cmdh{status} ne 'UP' and $cmdh{notification_type} ne 'ACKNOWLEDGEMENT' and $cmdh{notification_type} ne 'CUSTOM')
                {
                    debug("creating a new escalation for rule $esc_rule", 2);
                    # create status entry
                    createEscalationCounter($esc_rule, %cmdh);
#                         $incident_id,  $host,        $host_alias,
#                         $host_address, $service,     $check_type,
#                         $status,       $datetime,    $notification_type,
#                         $output
#                     );
                    debug("adding contacts to array", 2);
                    push @contactsArr, getContacts( \@esc_arr, 1, $cmdh{status}, $cmdh{notification_type}, $cmdh{external_id}, 0 );
                }

            }


##############################################################################
            # SEND COMMANDS
##############################################################################
            # loop through list of contacts
            for my $contact (@contactsArr)
            {
		debug('contact dump: '.Dumper(@contactsArr),3);
                my $user   = $contact->{username};
                my $method = $contact->{method};
                my $cmd    = $contact->{command};
                my $dest   = $contact->{ $contact->{contact_field} };
                my $sender = $contact->{sender};
                my $id    = unique_id();
                my $flag  = 0;
                my $notifyUnique = 1;

		# Create a string thats unique to a user, method and notification.
		my $userAndMethod = "$user.$method.$cmdh{external_id}";

                # insert into DB
                createLog(
                    '1', $id, $cmdh{external_id}, $contact->{rule},
                    $check_type_str{$cmdh{check_type}},          $cmdh{status},$cmdh{notification_type},
                    $cmdh{host},                $cmdh{service},
                    $method,           $contact->{mid}, $user,
                    'processing notification'
                );

                # Fgure out if the user has got a notification from the same method and external_ID already.
                foreach my $userNotification (keys %sentList) {
                    if ( $sentList{$userNotification} eq $userAndMethod ){
                        updateLog($id, ', already sent in previous notification');
                        debug('User and method already notified: ' . $user . ' and ' .  $method, 2);
                        $flag++;
                        $notifyUnique = 0;
                    }
                }
                # Should be unique by now, update log
                unless ($flag) {
                        #updateLog($id, ' uniq2user&method');
                        debug('User and method unique: ' . $user . ' ,' . $method, 2);
                }
 
                # Exit if its not unique!
                if ($notifyUnique == 0){
                        debug('Unique, next!', 3);
                        next;
                }
 
                # Save the method the user is notified with to the hashlist.
                $sentList{$userAndMethod} = $userAndMethod;

                # TODO consider using timezones and converting time to user configurable format e.g. 
                # M/D/YY for USA
                # DD/MM/YYYY or DD.MM.YYYY for most of the World
                # until this is implemented we just use what we were given
		        # TODO do not queue here -> this is the job of the bundler thread
			# (this prepareNotification() call is missing the the initial $incident_id parameter, anyway,
			# along with a bunch of other parameters)
                #$queue{$cmd}->enqueue(prepareNotification($user, $method, $cmd, $dest, $from, $id, $datetime, $check_type, $status,
                #        $notification_type, $host, $host_alias, $host_address, $service, $output));
                if (suppressionIsActive($cmd, $conf->{methods}->{$cmd}->{suppression}))
                {
                    updateLog($id, ' was suppressed');
                } else {
# TODO: pass hashes?
                    prepareNotification($cmdh{external_id}, $user, $method, 
			$cmd, $dest, $sender, $id, $cmdh{stime}, $cmdh{check_type}, $cmdh{status},
                        $cmdh{notification_type}, $cmdh{host}, $cmdh{host_alias}, $cmdh{host_address}, 
			$cmdh{hostgroups}, $cmdh{service}, $cmdh{servicegroups}, $cmdh{authors}, $cmdh{comments}, $cmdh{output}, 
			$contact->{rule}, undef, $cmdh{tmp_commands_id});
                }
            }

       # Clear the list over notifications, so the same external ID can be notified again if it is escalated to same users with the same methods.
       %sentList = ();
        }
    }



    # check for notification results
    RESULTSLOOP: if ( $msg = $msgq->dequeue_nb )
    {{
        # id= unique ID (per notification)
        my ( $id, $retval, @retstr ) = split( ';', $msg );
# TODO: Storable::thaw
        my $retstr = join( ';', @retstr );

        debug("received message from notifier: id=$id, retval=$retval, retstr=$retstr", 2);

        # retrieve details from DB

        # check if this was a bundled alert, and split it, pushing the individual results back onto the queue

        if (is_a_bundle($id))
        {
            debug("Bundled reply received", 3);
            foreach my $item (unbundle($id))
            {
                # delete the bundle from tmp active
                # remove the bundle id
                # push back onto queue
                $msgq->enqueue("$item;$retval;$retstr");
            }

            deleteFromActive($id);
            deleteFromCommands($id);
        }
        else
        {
            # check whether sending was successful
            if ( $retval != 0 )
            {

                # sending was NOT successful

                # foreach id;

                if (getRetryCounter($id) < $conf->{notifier}->{maxAttempts})
                {
                    # requeue notification and increment counter
            debug("requeueing notification $id", 2);
                    requeueNotification($id);
                }
                else
                {
                    # retrieve the contact data

                    # try to get next method (method escalation)
                    my ($nextMethod, $nextMethodName, $nextMethodCmd, $nextFrom, $nextTo) = getNextMethod($id);

                    if ($nextMethod eq '0')
                    {

                        debug("no more methods for $id", 3);
                        if ( $retstr eq '' )
                        {
                            $retstr = ' failed - no methods left';
                        } else
                        {
                            $retstr .= ' - failed - no methods left';
                        }

                        updateLog( $id, $retstr );
                        deleteFromActive($id);
                        # deleteFromCommands($id);

                    }
                    else
                    {

                        if ( $retstr eq '' )
                        {
                            $retstr = " failed\nTrying next method";
                        } else
                        {
                            $retstr .= " - failed\nTrying next method";
                        }

                        updateLog( $id, $retstr );
                        # $queue{$nextMethodName}->enqueue(getNextMethodCmd($id, $nextMethod));
                        # alter method for $id
                        # TODO: really try next method -> code here is wrong? last_method referenced in log....
                        $query = 'update tmp_active set method=\''.$nextMethodName.'\', notify_cmd=\''.$nextMethodCmd.'\', progress=\'0\', from_user=\''.$nextFrom.'\', dest=\''.$nextTo.'\', retries=\'0\' where notify_id=\''.$id.'\'';
                        updateDB($query);

                    }


                }
            }
            else
            {

                # sending was successful -> write to log
                if ( $retstr eq '' )
                {
                    $retstr = ' successful';
                } else
                {
                    $retstr .= ' - successful';
                }

                updateLog( $id, $retstr );

                # if this particular notification method was successful (e.g. email)
                # delete it from the tmp_active table
                # but first retrieve the incident_id which created this notification
                my $incident_id = getIncidentIDfromNotificationID($id);
                deleteFromActive($id);
                # deleteFromCommands($id);

                 # if the method is flagged as ACKable then additionally remove it from the status
                 # table (i.e. Voicealert)
                 if (notificationAcknowledgable($id))
                 {
                     # feedback acknowledgement to nagios
                     sendAckToPipe($incident_id);
                     deleteFromStati($id);
                     deleteFromEscalations($incident_id);
                 }
    #             else
    #             {
    #                 # pass to escalator
    #                 debug("The ACK flag is not set for this method: internally escalating $id");
    #                 escalate($id);
    #             }
            }
        }

    }}

    # check for bundling / send commands
    # do this in the main loop to avoid any race conditions

    sendNotifications(\%queue, $conf->{notifier});

    # here we check if there are any events that we need to escalate ->
    # i.e anything in the escalation_stati table
    # We requeue the notifications in the future
    escalate($cmdq, $conf->{escalator});


    # remove any orphans from the tmp_command table
    deleteOrphanCommands();
    # sleep for a bit; but if we just processed a command, then only nap briefly so
    # we can cycle back right away to quickly drain the queue of incoming commands
    select( undef, undef, undef, $cmd ? $nap_time : $sleep_time );
    # sleep 1;

} while (1);

#
# END - GLOBAL LOOP
#

exit 0;

##############################################################################
# SUBROUTINES START HERE
##############################################################################

# parse a command into host array
sub parseCommand
{
    my $cmd = shift;
    my %cmdh;
    my $sql;
    my @dbResult;

    # It's a bad idea to use the Nagios internal macros XXXNOTIFICATIONID for the
    # alert_via_noma.pl -u option, because they increment on every single alert instead
    # of remaining stable across consecutive non-UP/OK alerts.  The latter property
    # is needed in order to drive the incoming-alert-counting logic within NoMa.  Use
    # $HOSTPROBLEMID$ and $SERVICEPROBLEMID$ instead, or the more-robust forms:
    #
    #     -u "$$(( $HOSTPROBLEMID$ ? $HOSTPROBLEMID$ : $LASTHOSTPROBLEMID$ ))"
    #     -u "$$(( $SERVICEPROBLEMID$ ? $SERVICEPROBLEMID$ : $LASTSERVICEPROBLEMID$ ))"
    #
    # where the doubled dollar sign is needed to escape Nagios' own interpretation of the
    # dollar-sign, allowing the shell to interpret the arithmetic conditional expression.
    # Leaving the field blank (an empty alert_via_noma.pl -u option) is not appropriate
    # because then NoMa will generate a completely unique ID for every incoming alert,
    # and again, that will not clock the alert-counting logic within NoMa for consecutive
    # non-UP/OK states on the same host or service.

    # TODO convert datetime if necessary
    
    if ( $cmd =~ /^notification;/i
        || $cmd =~ /^escalation;/i )
    {
        (
            	$cmdh{operation},	$cmdh{external_id},	$cmdh{recipients},
		$cmdh{host},   		$cmdh{host_alias},      $cmdh{host_address}, 	$cmdh{hostgroups}, 
		$cmdh{service},        	$cmdh{servicegroups},	$cmdh{check_type},
	        $cmdh{status},    	$cmdh{stime},		$cmdh{notification_type}, 
                $cmdh{authors},         $cmdh{comments},         $cmdh{output}
        ) = split( ';', $cmd,16);

		## FIX MINOR:  STILL TO DO:  Fix Noma properly (all throughout) so that it
		## can deal with all non-Nagios event types that GroundWork can generate
		## (like the ones we test for here, that we're just squashing down to the
		## ones that it does currently support, as a kind of quick fix).

		if ( $cmdh{status} eq "SCHEDULED DOWN" )       { $cmdh{status} = "DOWN";     debug( "Noma Changing SCHEDULED DOWN to DOWN",           2 ); }
		if ( $cmdh{status} eq "UNSCHEDULED DOWN" )     { $cmdh{status} = "DOWN";     debug( "Noma Changing UNSCHEDULED DOWN to DOWN",         2 ); }
		if ( $cmdh{status} eq "SUSPENDED" )            { $cmdh{status} = "DOWN";     debug( "Noma Changing SUSPENDED to DOWN",                2 ); }
		if ( $cmdh{status} eq "MAINTENANCE" )          { $cmdh{status} = "UNKNOWN";  debug( "Noma Changing MAINTENANCE to DOWN",              2 ); }
		if ( $cmdh{status} eq "PENDING" )              { $cmdh{status} = "UNKNOWN";  debug( "Noma Changing PENDING to UNKNOWN",               2 ); }
		if ( $cmdh{status} eq "UNSCHEDULED CRITICAL" ) { $cmdh{status} = "CRITICAL"; debug( "Noma Changing UNSCHEDULED CRITICAL to CRITICAL", 2 ); }
		if ( $cmdh{status} eq "SCHEDULED CRITICAL" )   { $cmdh{status} = "CRITICAL"; debug( "Noma Changing SCHEDULED CRITICAL to CRITICAL",   2 ); }

		# sanity checks
		if ( $cmdh{check_type} eq 'h' )
		{
			if (!defined($stati_host{$cmdh{status}}))
			{
				return;
			}
		}
		elsif ( $cmdh{check_type} eq 's' )
		{
			if (!defined($stati_service{$cmdh{status}}))
			{
				return;
			}
		}
		else
		{
			return;
		}

	if ( $cmd =~ /^notification;/i && $conf->{notifier}->{generate_IDs} ) {
	    ## In this case, we will either get back an existing problem ID for this host/service,
	    ## or some random number that we may briefly stuff into the tmp_commands table just below
	    ## and then replace later on once we generate a standard problem ID for this host/service.
	    $cmdh{external_id} = unique_id( $cmdh{host}, $cmdh{service} );
	}
	elsif ( $cmdh{external_id} eq '' or $cmdh{external_id} < 1 ) {
	    ## Legacy behavior, generating only a random number and then only when the incoming alert
	    ## did not already include a problem ID for this host/service.  This branch is also used
	    ## intentionally for processing escalations.
	    $cmdh{external_id} = unique_id();
	}

        $cmdh{operation} = lc($cmdh{operation});
        if (($cmdh{stime} eq ""))
        {
             debug("Empty date $cmdh{stime} for notification - using time()", 2);
             $cmdh{stime} = time();
        }
        elsif (($cmdh{stime} =~ /\D/) or ($cmdh{stime} < 1000000000))
        {
            debug("Invalid date $cmdh{stime} for notification - using time()", 2);
            $cmdh{stime} = time();
        }

    if ( $cmd =~ /^notification;/i)
    {
	## FIX MAJOR:  The adjustments of $cmdh{output} here are ugly hacks.  This code ought to be
	## using $dbh->quote() throughout, for ALL fields, in ALL queries that use string literals,
	## instead of attempting to quote strings manually here and in other queries.  Either that, or
	## query parameters should be passed to prepared queries that are specified with ?-character
	## placeholders for such parameters.  The only reason we're not doing such things is because the
	## whole NoMa code infrastructure is not currently equipped to make $dbh available when it's
	## needed for such work.  Handling the one $cmdh{output} parameter here just deals with the
	## single parameter which is most likely to cause a problem, as a crude temporary workaround.
	##
	## All three supported databases (sqlite3, mysql, and postgresql) support doubled single-quote
	## characters as meaning one single-quote character within a single-quoted string.
	$cmdh{output} =~ s/'/''/g;
	if ($conf->{db}->{type} eq 'mysql') {
	    ## MySQL also supports backslash escapes in strings.  But we don't want to risk interpretation
	    ## of backslash escapes, so we turn them here into literal characters by doubling them.
	    ## Once we move to the use of $dbh->quote(), this transform must be revisited to ensure that
	    ## it is still effectively performed.
	    $cmdh{output} =~ s/\\/\\\\/g;
	}
	$sql = sprintf('insert into tmp_commands (operation, external_id, recipients, host, host_alias, host_address, hostgroups, service, servicegroups, check_type, status, stime, notification_type, authors, comments, output) values (\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\', \'%s\')',
                $cmdh{operation},       $cmdh{external_id},	$cmdh{recipients},
		$cmdh{host},            $cmdh{host_alias},      $cmdh{host_address},    $cmdh{hostgroups},
                $cmdh{service},         $cmdh{servicegroups},   $cmdh{check_type},
                $cmdh{status},          $cmdh{stime},           $cmdh{notification_type},
		$cmdh{authors},		$cmdh{comments},         $cmdh{output});
	  $cmdh{tmp_commands_id} = updateDB($sql, undef, 'id');
    }
        return %cmdh;
    }

    if ( $cmd =~ /^status/i )
    {

        foreach my $i (keys %queue)
        {
            debug("Queue $i has ".$queue{$i}->pending." pending jobs", 1);
        }

        # In some databases, running a full-table count is a rather expensive operation.
        # So there's no reason to do so unless we're actually going to print the result.
        if ( $debug >= 3 ) {
            $sql = 'select count(*) as count from tmp_active';
            @dbResult = queryDB($sql, 1);
            debug("There are ".$dbResult[0]{count}." active escalations", 3);
        }

	if ($conf->{db}->{type} eq 'mysql') {
	    $sql = 'select count(*) as count from notification_logs where timestamp > date_sub(now(), interval 1 hour)';
	} elsif ($conf->{db}->{type} eq 'sqlite3') {
	    $sql = "select count(*) as count from notification_logs where timestamp > datetime('now' , '-1 hour')";
	} elsif ($conf->{db}->{type} eq 'postgresql') {
	    $sql = "select count(*) as count from notification_logs where timestamp > now() - interval '1 hour'";
	} else {
	    debug( "Your configuration specifies an unsupported \"db\" database type; exiting!", 1);
	    exit;
	}
        @dbResult = queryDB($sql, 1);
        debug($dbResult[0]{count}." notifications were sent in the last hour", 1);


    }

    if ( $cmd =~ /^suppress;([^;]*);*(.*)/i )
    {

        $suppressionHash{$1} = time();
        createLog('1', unique_id(), unique_id(), 0, '(internal)','OK','(supression)','localhost','NoMa','(none)', '0',$2, "All $1 alerts have been suppressed by $2");
	deleteAllFromActive();
	deleteAllFromEscalations();
	deleteAllFromCommands();
    }

    return;

}


# ignores internally escalated rules
sub getNotificationCounter
{
    my ($host, $svc, $flag) = @_;
    my $counter;

    $counter = 0 unless defined($flag);

    my $query = 'select counter from notification_stati where host=\''.$host.'\'';

    if (defined($svc) and $svc ne '')
    {
        # service alert
        $query .= ' and service=\''.$svc.'\'';
    }
    else
    {
        $query .= ' and check_type=\'h\'';
    }

    my %dbResult = queryDB($query);

    $counter = $dbResult{0}->{counter}
      if ( defined( $dbResult{0}->{counter} ) );

    return $counter;


}


sub getNotificationID
{
    my ( $host, $service, $flag ) = @_;
    my $id;

    $id = 0 unless defined($flag);

    my $query = "select id from notification_stati where host='$host'";

    if ( defined($service) and $service ne '' )
    {
	## service alert
	$query .= " and service='$service'";
    }
    else
    {
	$query .= " and check_type='h'";
    }

    my %dbResult = queryDB($query);

    $id = $dbResult{0}->{id} if ( defined( $dbResult{0}->{id} ) );

    return $id;
}


# returns UP or OK if the state is not in the table already, otherwise returns the last known state
sub getLastState
{
    my ($host, $svc) = @_;
    my $check_result;


    my $query = 'select check_result from notification_stati where host=\''.$host.'\'';

    if (defined($svc) and $svc ne '')
    {
        # service alert
        $query .= ' and service=\''.$svc.'\'';
        $check_result = "OK";
    }
    else
    {
        $query .= ' and check_type=\'h\'';
        $check_result = "UP";
    }

    my %dbResult = queryDB($query);

    $check_result = $dbResult{0}->{check_result}
      if ( defined( $dbResult{0}->{check_result} ) );

    return $check_result;


}
sub prepareNotification
{
	my ($incident_id, $user, $method, $short_cmd, $dest, $sender, $id,
	$datetime, $check_type, $status,
	$notification_type, $host, $host_alias, $host_address, $hostgroups, $service, $servicegroups,
	$authors, $comments, $output, $rule, $nodelay, $tmp_commands_id) = @_;

	# start of the notification
	my $start = time();

	my $cmd = $conf->{command}->{$short_cmd};
	my $error = undef;

	# error if something is missing
	if ( defined($cmd) )
	{
	    # error if script is missing
	    unless ( -x $cmd )
	    {
		$error .= ' Missing or unexecutable script: ' . $cmd;
	    }
	}
	else
	{
	    $error .= ' Missing ' . ( defined($short_cmd) ? $short_cmd : 'undefined-type' ) . ' command for notification belonging to: ' . $user;
	}
	unless ( defined($dest) )
	{
	    $error .= ' Missing destination for notification belonging to: ' . $user;
	}

    if (defined($error))
    {
        debug($error, 1);
        updateLog($id, $error);
        return 0;
    }

	# default 'sender' (previously $from)
	unless ( defined($sender) )
	{
	    my $sender = '';
	}

	# create parameter (FROM DESTINATION CHECK-TYPE DATETIME STATUS NOTIFICATION-TYPE HOST-NAME HOST-ALIAS HOST-IP OUTPUT [SERVICE])
	# my $param = sprintf(
#"\"%s\" \"%s\" \"%s\" \"%s\" \"%s\" \"%s\" \"%s\" \"%s\" \"%s\" \"%s\"",
#	    $from,  $dest,    $check_type,
#	    $datetime, $status,     $notification_type,
#	    $host,     $host_alias, $host_address,
#	    $output
#	);
#	$param .= " \"$service\"" if ( $check_type eq 's' );
#
#	debug("$whoami: BEFORE call - $method  $param");

    # if there is a configured delay, add it to the start time
    my $delay = $conf->{notifier}->{delay};
    $delay = 0 unless (defined($delay) and not defined($nodelay));

	# insert the command into our active notification list
	# NOTE:  Multiple rows can have the same tmp_commands.external_id
	# ($incident_id) field here.  The only thing really distinguishing
	# multiple rows we might get back here is the tmp_commands.id field.
	my $query = sprintf('SELECT \'%s\' AS user,\'%s\' AS method,\'%s\' AS notify_cmd, \'%s\' AS time_string,\'%s\' AS notify_id,\'%s\' AS dest, \'%s\' AS from_user, \'%s\' AS rule, id,(stime+\'%s\') AS stime FROM tmp_commands WHERE external_id = \'%s\'',
		$user, $method, $short_cmd, $datetime, $id, $dest, $sender, $rule, $delay, $incident_id);
	## This "id=$tmp_commands_id" clause should always be operative, in
	## order to restrict the number of returned rows to at most one (the
	## exact one we are interested in, as opposed to several that might
	## otherwise be returned), to make the [0] subscript below actually
	## reflect the one row we care about in this pass.  This comparison
	## makes the legacy checking of the external_id field in the query
	## WHERE clause superfluous.
	$query .= " and id = $tmp_commands_id" if defined($tmp_commands_id) and $tmp_commands_id > 0;
	my %dbResult = queryDB($query);
	if (%dbResult) {
	    my $query2 = sprintf('INSERT INTO tmp_active ('.quoteIdentifier('user').', method, notify_cmd, time_string, notify_id, dest, from_user, rule, command_id, stime) VALUES (\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\')',
		$dbResult{0}{user},
		$dbResult{0}{method},
		$dbResult{0}{notify_cmd},
		$dbResult{0}{time_string},
                $dbResult{0}{notify_id},
                $dbResult{0}{dest},
                $dbResult{0}{from_user},
                $dbResult{0}{rule},
                $dbResult{0}{id},
                $dbResult{0}{stime}
	    );
    	
	    updateDB($query2);
	}
	else {
	    ## The original code was expecting a result to always appear, because it never checked
	    ## %dbResult before attempting to use it.  So we log here if that doesn't happen.
	    debug("NOTICE:  Query failed to yield any results:", 1);
	    debug("    $query", 1);
	}

	# return("$id;$start;1;$param");
	return 1;

}

sub deleteFromActive
{
    my ($id) = @_;

    return if (!$id);
    my $query = "delete from tmp_active where notify_id=$id";

    updateDB($query);
}

sub deleteAllFromActive
{
    my $query = "delete from tmp_active";
    updateDB($query);
}

sub deleteAllFromEscalations
{
    my $query = "delete from escalation_stati";
    updateDB($query);
}

sub deleteFromActiveByName
{
    my ($host, $service) = @_;

    my $query = 'select a.notify_id as notify_id from tmp_active as a left join notification_logs as l on a.notify_id=l.unique_id where l.host=\''.$host.'\' and l.service=\''.$service.'\'';
    my %dbResult = queryDB($query);
    foreach my $index (keys %dbResult)
    {
        deleteFromActive($dbResult{$index}{notify_id});
    }

}

sub deleteFromCommands
{
    my ($id) = @_;
    my $query;
    my %dbResult;

    return if (!$id);

    $query = 'select count(a.notify_id) as count from tmp_active as a left join notification_logs as l on a.notify_id=l.unique_id where l.incident_id=(select incident_id from notification_logs where unique_id='.$id.')';
    %dbResult = queryDB($query);

    if(!($dbResult{0}->{count}>0))
    {
      $query = "delete from tmp_commands where external_id in (select incident_id from notification_logs where unique_id=$id)";
    }

    updateDB($query);
}



sub deleteAllFromCommands
{
    my $query = "delete from tmp_commands";
    updateDB($query);
}

sub deleteOrphanCommands
{
    #my $query = "delete from tmp_commands where external_id not in (select distinct incident_id from notification_logs right join tmp_active on notification_logs.unique_id=tmp_active.notify_id union select distinct incident_id from escalation_stati)";# DEFAULT
#    my $query = "delete from tmp_commands where external_id not in (select distinct incident_id from notification_logs left join tmp_active on notification_logs.unique_id=tmp_active.notify_id union select distinct incident_id from escalation_stati)"; #RUNE
    my $query = 'DELETE FROM tmp_commands WHERE external_id NOT IN (SELECT DISTINCT incident_id FROM tmp_active LEFT JOIN notification_logs ON tmp_active.notify_id=notification_logs.unique_id UNION SELECT DISTINCT incident_id FROM escalation_stati)'; # MJ
    updateDB($query, 1);
}



# check whether a result came from an acknowledgeable method
sub notificationAcknowledgable
{
    my ($id) = @_;

    my $query = 'select nm.ack_able as ack_able from notification_logs as l left join
                    notification_methods as nm on l.last_method=nm.id
                    where l.unique_id=\''.$id.'\'';

    my %dbResult = queryDB($query);

    my $ackable = $dbResult{0}->{ack_able};

    return 1 if (defined($ackable) && ($ackable>0));
    debug("notification not ackable", 2);
    return 0;
}

sub sendAckToPipe
{
    my ($id) = @_;

    my $file = $conf->{notifier}->{ackPipe};
    return unless (defined($file) and $file ne '');
    my $ackstr;
    my $host;
    my $svc;
    my $contact;

    my $query = "select host,service,".quoteIdentifier('user')." from notification_logs where incident_id=$id";
    my %dbResult = queryDB($query);

    $host = $dbResult{0}->{host};
    $svc = $dbResult{0}->{service};
    $contact = $dbResult{0}->{user};
    if ($svc eq '')
    {
        $ackstr = "[".time()."] ACKNOWLEDGE_HOST_PROBLEM;$host;1;0;0;NoMa;Acknowledged by $contact\n";
    } else {
        $ackstr = "[".time()."] ACKNOWLEDGE_SVC_PROBLEM;$host;$svc;1;0;0;NoMa;Acknowledged by $contact\n";
    }

    if (!sysopen(PIPE, $file, O_WRONLY | O_APPEND | O_NONBLOCK))
    {
	debug("Failed to open Ack Pipe $file", 1);
	return;
    }

    debug("Writing $ackstr to $file", 1);
    syswrite(PIPE,$ackstr);
}


sub getMethods
{

    my $query = undef;
    my @dbResult;

    $query =
	'select command from notification_methods group by command';
    @dbResult = queryDB( $query, 1 );

    return (@dbResult);

}

sub clearNotificationCounter
{

    my ( $host, $svc ) = @_;

    my $query = 'delete from notification_stati
			where host=\''.$host.'\'';
                
    if (defined($svc) and $svc ne '')
    {
        # service alert
        $query .= ' and service=\''.$svc.'\'';
    }
    else
    {
        $query .= ' and check_type=\'h\'';
    }

    updateDB($query);

}

# clear the counter given an unique_id
sub deleteFromStati
{
    my ($id) = @_;

    my $query = 'select host,service from notification_logs
                    where unique_id=\''.$id.'\'';

    my %dbResult = queryDB($query);

    # TODO: check that host alerts work!
    clearNotificationCounter($dbResult{0}->{host}, $dbResult{0}->{service});

}

# return unique_id for a notification_id
sub getIncidentIDfromNotificationID
{
    my ($id) = @_;

    my $query = 'select c.external_id as id from tmp_commands as c inner join tmp_active as t on c.id=t.command_id
                    where t.notify_id=\''.$id.'\'';

    my %dbResult = queryDB($query);
    return $dbResult{0}->{id};

}

##############################################################################
# MISC FUNCTIONS
##############################################################################
sub unique_id
{
    my ( $host, $service ) = @_;

    if ( $conf->{notifier}->{generate_IDs} and defined $host ) {
	## In this situation, use the existing notification-ID value,
	## if available, as the unique "external" problem ID.
	my $id = getNotificationID( $host, $service );
	return $id if $id;
    }

    # we don't use MySQL UUID() to generate IDs
    # because this won't work in offline mode
    return sprintf( "%d%05d", time(), int( rand(99999) ) );
}



sub incrementNotificationCounter
{

    my ( $status, $host, $service, $check_type, $tmp_commands_id, $external_id_ref ) = @_;
    my $notificationCounter =
        getNotificationCounter($host, $service, 1);

    my $do_final_update = 1;
    if ( defined($notificationCounter) )
    {
        $query = 'update notification_stati set counter=counter+1,
            check_result=\'' . $status . '\'
            where host=\'' . $host . '\' and ' . 'service=\'' . $service . '\'';
    }
    else
    {
        $notificationCounter = 0;
        $query =
            'insert into notification_stati (host,service,check_type,check_result,counter,pid)
            values (' . "'$host','$service','$check_type','$status','1','0')";
	if ( $conf->{notifier}->{generate_IDs} && $tmp_commands_id ) {
	    ## In this case, we previously stuffed a temporary filler value into the tmp_commands.external_id
	    ## column in one new row, because that's all the data we had in hand.  But now that we will obtain
	    ## the proper value to use for that field, it's time to both update the tmp_commands table and
	    ## percolate that value back up into the calling code, replacing the filler value for further use.
	    ## It's up to the calling code to guarantee that no other use was made of the filler value between
	    ## the time that it was created and this invocation of incrementNotificationCounter().
	    my $problem_id = updateDB( $query, undef, 'id' );
	    if ($problem_id) {
		$query = "update tmp_commands set external_id = $problem_id where id = $tmp_commands_id";
		if ( defined updateDB($query) ) {
		    $$external_id_ref = $problem_id;
		}
		else {
		    ## As in the code branch just below, we probably had a database-access problem, and there
		    ## is nothing we can do about it here except log the occurrence.
		    debug( "ERROR:  Cannot update the tmp_commands table external_id value to $problem_id for tmp_commands.id $tmp_commands_id, host $host"
		      . ( $service ? " service $service" : '' ), 1 );
		}
	    }
	    else {
		## We probably had some sort of database-access problem when trying to insert the new row
		## into the notification_stati table.  So there is no way to update the caller's external_id
		## value in this case; we'll just have to live with the random number previously generated.
		## This means that incoming-alert counting (otherwise referred to inside the NoMa code as
		## notification counting) won't work, since that external_id value will never be replicated on
		## any future incoming alert.  Thus while this alert will be properly treated as the first in
		## a possible sequence, there cannot be any future elements in that same sequence.  Meaning,
		## the next alert for the same host/service will once again be treated as the first in a
		## potential sequence.  There's nothing we can do about that here except log the occurrence.
		debug( "ERROR:  Cannot update the tmp_commands table external_id value for tmp_commands.id $tmp_commands_id, host $host"
		  . ( $service ? " service $service" : '' ), 1 );
	    }
	    $do_final_update = 0;
	}
    }

    updateDB($query) if $do_final_update;
    return ( $notificationCounter + 1 );
}

sub resetNotificationCounter
{

    my ( $host, $service ) = @_;

    $query = 'update notification_stati set counter=1
        where host=\'' . $host . '\' and ' . 'service=\'' . $service . '\'';

    updateDB($query);
    return ( 1 );

}


sub createLog
{

    # get parameter values
    my (
        $counter, $cur_id, $incident_id, $rule, $check_type_str, $status, $notification_type,$host,
        $service, $method, $mid, $user,       $result
    ) = @_;

    if ( $cur_id eq '' )
    {
        $cur_id = unique_id();
    }

    # create timestamp
    my ( $sec, $min, $hour, $day, $mon, $year ) =
      (localtime)[ 0, 1, 2, 3, 4, 5 ];
    $year += 1900;
    $mon++;
    my $timestamp = sprintf( "%d-%02d-%02d %02d:%02d:%02d",
        $year, $mon, $day, $hour, $min, $sec );

    # get delimiter
    my $delimiter = '';
    if ( defined( $conf->{'log'}->{delimiter} ) )
    {
        $delimiter = $conf->{'log'}->{delimiter};
    }

    # check for verbosity of logging and populate result string if necessary
    if ( !defined( $conf->{'log'}->{pluginOutput} ) )
    {
        $result = '';
    }

    my $query = sprintf(
	'insert into notification_logs (unique_id, incident_id, notification_rule, '
	  . quoteIdentifier('timestamp')
	  . ',counter,check_type,check_result,notification_type,host,service,method,last_method,'
	  . quoteIdentifier('user')
	  . ',result)
			values (\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\')',
        $cur_id,
	$incident_id,
	$rule,
        $timestamp,
        $counter,
        $check_type_str,
        $status,
	$notification_type,
        $host,
        $service,
        $method,
	$mid,
        $user,
        $result
    );

    updateDB($query);

}

# add a string to a log entry identified by a unique id
sub updateLog
{
    my ( $id, $result ) = @_;
    my $query;
    my %dbResult;

    # Because SQLite and MySQL doesnt use the same form of concat syntax, it needs to select, process and then update.

    # Query to find the existing result
    $query = 'select result from notification_logs where unique_id=\''.$id.'\'';
    %dbResult = queryDB($query);

    # Create the new result, by merging existing and new.
    $result = $dbResult{0}->{result}.$result;
    debug('Updated result: '.$result.' for unique_id: '.$id,3);
    # Update logentry.
    $query = sprintf(
	'update notification_logs set result=\'%s\' where unique_id=\'%s\'',
        $result, $id );
    updateDB($query);
}

# return true if we are beyond the last notification
sub counterExceededMax
{
    my ($ids, $counter) = @_;

    my $query = 'select notify_after_tries from notifications where id in ('.join(',',@$ids).') and rollover=1 and active=1';
    $query .= ' union select e.notify_after_tries from escalations_contacts as e';
    $query .= ' left join notifications as n on e.notification_id=n.id where notification_id in ('.join(',',@$ids).') and rollover=1 and active=1';

	my @dbResult = queryDB($query, '1');

    my $maxval = 0;

	foreach my $tries (@dbResult)
	{
        my $max = getMaxValue($tries->{notify_after_tries});
		$maxval = $max if ($max > $maxval);
	}
    return 0 if ($maxval >= $counter);
    return 0 if ($maxval == 0);
    my $retval = $counter % $maxval;
    $retval = $maxval if($retval == 0);

    debug("notification counter rollover: $counter exceeds $maxval -> continuing at $retval", 2);
    return $retval;
}

# vim: ts=4 sw=4 expandtab
# EOF
