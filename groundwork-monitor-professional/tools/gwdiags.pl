#!/usr/local/groundwork/perl/bin/perl -w --
#-----------------------------------------------------------------------
# Copyright 2013-2017 GroundWork Open Source, Inc.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; version 2
# of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# Initial version author Don Johnson
#
#  5 Sep 2008 Improved for GroundWork 5.2.1 Jeffery Chin
# 18 Jun 2009 Improved for GroundWork 6.0.0 Jeffery Chin
# 10 Mar 2010 improved for GroundWork 6.1.1 Jeffery Chin
#  2 Mar 2011 improved for GroundWork 6.4 Dave Blunt
# 14 Mar 2011 updates for PHP logging Dave Blunt
#  2 Nov 2011 initial changes for use with PostgreSQL Glenn Herteg
# 19 Jan 2012 add PostgreSQL dumps; other fixup, too Glenn Herteg
# 24 Jan 2012 fix backward compatibility; call pg_top; capture postmaster.log Glenn Herteg
#  4 Feb 2012 improve SUSE support; stop asking for unused PostgreSQL password Glenn Herteg
# 16 Aug 2013 support GroundWork 7.0.0 Glenn Herteg
# 17 Sep 2013 capture nagios2collage_socket.log; add Dual-JVM files to diag output Glenn Herteg
# 24 Sep 2013 capture Apache log files
# 29 Oct 2013 support revised paths for postmaster.log files
# 14 Apr 2014 capture configuration.properties and gatein.properties, not symlinks to them
# 29 Apr 2014 drop mention of a config2/ directory for GWMEE 7 or later, since it's not used there
# 09 Jan 2015 correct the tempfile filename template to include trailing X characters
# 08 Nov 2016 improve support for CentOS6; add initial support for CentOS7 and SLES12; capture NoMa.yaml
# 25 Aug 2017 improve handling of a corupted nagios.log file
# 06 Nov 2017 add new files needed for 7.2.0
# 08 Jun 2018 add new files needed for 7.2.1. change netstat output to numeric values. print monarch groups.

#
# Be sure to update $progversion below as well!
#
#-----------------------------------------------------------------------
# TO DO:
# (*) Extend Ask_About() with an extra optional parameter, that if given
#     as a true value will cause the routine to echo user input as "*"
#     characters (for non-visible password input).
#-----------------------------------------------------------------------

use strict;
use File::Copy;
use File::Temp qw/ tempfile tempdir /;
use POSIX;
use Fcntl;
use Sys::Hostname;

sub Valid_Utility (@);
sub run_cmd (@);
sub run_cmd_nolog (@);
sub Ask_About (@);
sub Usage ();
sub Print_Results (@);

#-----------------------------------------------------------------------
# Preliminaries
#-----------------------------------------------------------------------
my $gw_version_line = `/bin/fgrep version /usr/local/groundwork/Info.txt`;
my $gw_version = (split(/[= ]+/, $gw_version_line))[1];
$gw_version =~ s/\s+$//g;
my $gw_vstring = pack( 'U*', split( /\./, $gw_version ) );

#-----------------------------------------------------------------------
# Useful Variables
#-----------------------------------------------------------------------
my $progname               = "";
{
    my @tmp                = split( /\//, $0 );    # Throw away array.
    $progname              = $tmp[$#tmp];
}
my $progversion            = "2018-06-13";
my $progtitle              = "GroundWork Diagnostics Tool ($progname version $progversion)";
my $GW_HOME                = '/usr/local/groundwork';
my $FOUNDATION_ROOT        = "$GW_HOME/foundation/container";
my $FOUNDATION_HOME        = $gw_vstring ge v7.0.0 ? "$FOUNDATION_ROOT/jpp/standalone/log" : "$FOUNDATION_ROOT/logs";
my $datestamp              = strftime( ".%Y_%m_%d.%H_%M", localtime );
my $temptemplate           = 'gwdiagsXXXXXX';
my $tempdir                = "$GW_HOME/tmp/gwdiags";
my $tempfiledir            = "$tempdir/files";
my $tempfiledir2           = $gw_vstring ge v7.0.0 ? "$tempdir/standalone-foundation" : "$tempdir/dual-jvm";
my $tempsuffix             = '.log';
my $suppress_errors        = " 2>/dev/null;";
my $makedir                = "/bin/mkdir";
my $bitrocklog             = "/tmp/bitrock_installer*";
my $nagioslog              = "$GW_HOME/nagios/var/nagios.log";
my $diagsummaryfile        = "$tempdir/diagsummary$datestamp.log";
my $quicksummaryfile       = "$tempdir/quicksummary$datestamp.log";
my $diagoutfile            = "$GW_HOME/tmp/gwdiags$datestamp.tar.gz";
my $mysql_monarchdump      = "$tempdir/monarch$datestamp.sql";
my $postgresql_monarchdump = "$tempdir/monarch.postgresql$datestamp.sql";
my $mysql_devicetbl        = "$tempdir/device-table$datestamp.sql";
my $postgresql_devicetbl   = "$tempdir/device-table.postgresql$datestamp.sql";
my $mysql_globalstats      = "$tempdir/mysqlglobalstats$datestamp.log";
my $nagioslogrt            = "$tempfiledir/nagios.log";
my $setenv                 = "$GW_HOME/scripts/setenv.sh";
my $perlbin                = "$GW_HOME/perl/bin/perl";
my $input                  = "";
my $Dashes                 = "#--------------------------------------------------\n";
my $Nr_of_Lines            = 20;
my $env                    = "/bin/env";
my $lsof                   = "/usr/sbin/lsof";

my $ISSECRET   = 1;
my $ISNTSECRET = 0;

# Files related to the standard base product.
my %filehash = (
    "$FOUNDATION_HOME/framework.log"                                                     => $ISNTSECRET,
    "$FOUNDATION_HOME/boot.log"                                                          => $ISNTSECRET,
    "$FOUNDATION_ROOT/josso-1.8.4/conf/server.xml"                                       => $ISNTSECRET,
    "$FOUNDATION_ROOT/josso-1.8.4/conf/conf/logging.properties"                          => $ISNTSECRET,
    "$FOUNDATION_ROOT/jpp/standalone/configuration/application-users.properties"         => $ISSECRET,
    "$FOUNDATION_ROOT/jpp/standalone/configuration/standalone.xml"                       => $ISNTSECRET,
    "$FOUNDATION_ROOT/run.conf"                                                          => $ISNTSECRET,
    "$GW_HOME/apache2/conf/httpd.conf"                                                   => $ISNTSECRET,
    "$GW_HOME/apache2/conf/extra/httpd-ssl.conf"                                         => $ISNTSECRET,
    "$GW_HOME/apache2/logs/access_log"                                                   => $ISSECRET,
    "$GW_HOME/apache2/logs/error_log"                                                    => $ISSECRET,
    "$GW_HOME/apache2/logs/ssl_request_log"                                              => $ISSECRET,
    "$GW_HOME/cacti/htdocs/log/cacti.log"                                                => $ISNTSECRET,
    "$GW_HOME/common/etc/send_nsca.cfg"                                                  => $ISSECRET,
    "$GW_HOME/common/etc/snmp/*.conf"                                                    => $ISNTSECRET,
    "$GW_HOME/common/etc/snmp/snmptt.ini"                                                => $ISNTSECRET,
    "$GW_HOME/common/etc/syslog-ng.conf"                                                 => $ISNTSECRET,
    "$GW_HOME/common/var/patches/*.log"                                                  => $ISNTSECRET,
    "$GW_HOME/common/var/patches/*.installed"                                            => $ISNTSECRET,
    "$GW_HOME/config/*.properties"                                                       => $ISSECRET,
    "$GW_HOME/config/bronx.cfg"                                                          => $ISSECRET,
    "$GW_HOME/config/fping_process.conf"                                                 => $ISNTSECRET,
    "$GW_HOME/config/groundwork.lic"                                                     => $ISNTSECRET,
    "$GW_HOME/configjosso-gateway-ldap-stores.xml"                                       => $ISSECRET,
    "$GW_HOME/config/josso-gateway-config.xml"                                           => $ISNTSECRET,
    "$GW_HOME/config/josso-agent-config.xml"                                             => $ISNTSECRET,
    "$GW_HOME/config/log-archive-receive.conf"                                           => $ISSECRET,
    "$GW_HOME/config/log-archive-send.conf"                                              => $ISSECRET,
    "$GW_HOME/config/syslog2nagios.conf"                                                 => $ISNTSECRET,
    "$GW_HOME/influxdb/etc/influxdb.conf"                                                => $ISNTSECRET,
    "$GW_HOME/Info.txt"                                                                  => $ISNTSECRET,
    "$GW_HOME/grafana/conf/defaults.ini"                                                 => $ISNTSECRET,
    "$GW_HOME/groundwork-nms-installer-*/nms.log"                                        => $ISNTSECRET,
    "$GW_HOME/logs/influxd.log"                                                          => $ISSECRET,
    "$GW_HOME/logs/grafana.log"                                                          => $ISNTSECRET,
    "$GW_HOME/logs/JOSSO-Server.log"                                                     => $ISSECRET,
    "$GW_HOME/logs/log-archive-receive.log"                                              => $ISNTSECRET,
    "$GW_HOME/logs/log-archive-send.log"                                                 => $ISNTSECRET,
    "$GW_HOME/logs/monarch_foundation_sync.log"                                          => $ISSECRET,
    "$GW_HOME/logs/nagios2collage_socket.log"                                            => $ISSECRET,
    "$GW_HOME/logs/nagios2collage_eventlog.log"                                          => $ISSECRET,
    "$GW_HOME/logs/noma_debug.log"                                                       => $ISSECRET,
    "$GW_HOME/logs/Noma-logfile.log"                                                     => $ISSECRET,
    "$GW_HOME/logs/register_agent.log"                                                   => $ISSECRET,
    "$GW_HOME/logs/service-jpp.log"                                                      => $ISNTSECRET,
    "$GW_HOME/logs/snmptrapd.log"                                                        => $ISSECRET,
    "$GW_HOME/logs/snmptt.log"                                                           => $ISSECRET,
    "$GW_HOME/logs/snmptt.debug"                                                         => $ISSECRET,
    "$GW_HOME/logs/snmpttsystem.log"                                                     => $ISSECRET,
    "$GW_HOME/logs/snmpttunknown.log"                                                    => $ISSECRET,
    "$GW_HOME/logs/syslog2nagios.log"                                                    => $ISSECRET,
    "$GW_HOME/mysql/data/mysqld.log"                                                     => $ISNTSECRET,
    "$GW_HOME/mysql/data/safe_mysqld.log"                                                => $ISNTSECRET,
    "$GW_HOME/mysql/my.cnf"                                                              => $ISNTSECRET,
    "$GW_HOME/nagios/var/event_broker.log"                                               => $ISNTSECRET,
    "$GW_HOME/noma/etc/NoMa.yaml"                                                        => $ISSECRET,
    "$GW_HOME/php/etc/php.ini"                                                           => $ISNTSECRET,
    "$GW_HOME/php/tmp/php.log"                                                           => $ISNTSECRET,
    "$GW_HOME/postgresql/data/pg_hba.conf"                                               => $ISNTSECRET,
    "$GW_HOME/postgresql/data/postgresql.conf"                                           => $ISNTSECRET,
    "$GW_HOME/tools/system_setup/log/*"                                                  => $ISSECRET,
    "$GW_HOME/tools/system_setup/extra-vars.yml"                                         => $ISSECRET,
);

# Files related to the standard base product, but whose pathname is release-dependent.
if ( $gw_vstring ge v7.0.1 ) {
    $filehash{"$GW_HOME/postgresql/data/pg_log/postmaster.log"}   = $ISSECRET;
    $filehash{"$GW_HOME/postgresql/data/pg_log/postmaster.log.1"} = $ISSECRET;
}
elsif ( $gw_vstring ge v6.6 ) {
    $filehash{"$GW_HOME/postgresql/data/postmaster.log"} = $ISSECRET;
}

# Files related to Dual-JVM setups.
my %filehash2 = ();
if ( $gw_vstring ge v7.0.0 ) {
    %filehash2 = (
    "$GW_HOME/jpp2/bin/standalone.conf"                                    => $ISNTSECRET,
    "$GW_HOME/jpp2/standalone/configuration/standalone.xml"                => $ISNTSECRET,
    "$GW_HOME/jpp2/standalone/log/framework.log"                           => $ISNTSECRET,
    "$GW_HOME/jpp2/standalone/configuration/application-users.properties"  => $ISSECRET,
    );
}
else {
    %filehash2 = (
    "$GW_HOME/config2/*.properties"                     => $ISSECRET,
    "$GW_HOME/foundation/container2/logs/framework.log" => $ISNTSECRET,
    "$GW_HOME/foundation/container2/run.conf"           => $ISNTSECRET,
    );
}

#-----------------------------------------------------------------------
# These variables should be asked for from the user.
#-----------------------------------------------------------------------
my $gwserver         = hostname();
my $SendTo           = 'gwdiags@groundworkopensource.com';
my $SendFrom         = "gwdiags@".$gwserver."";
my $casenr           = 'New';
my $MServer          = 'localhost:25';
my $PostgreSQLPasswd = "";
my $MySQLPasswd      = "";
my $RootPasswd       = "";
my %Options;
$Options{'Verbose'} = 0;
$Options{'Secret'}  = 0;
$Options{'Mail'}    = 0;
my %Ask;
$Ask{'SendTo'  }    = (1==0);
$Ask{'SendFrom'}    = (1==0);
$Ask{'casenr'  }    = (1==1);
$Ask{'MServer' }    = (1==0);
$Ask{'Passwd'  }    = (1==1);

#-----------------------------------------------------------------------
# Process Command Line Arguments.  If certain options aren't input,
# mark them for 'Asking' about them. Others just get a silent default.
#-----------------------------------------------------------------------
for ( my $i=0; $i<=$#ARGV; $i++ ) {
    if ($ARGV[$i] =~ /^-/) {
    if   ($ARGV[$i]=~ /^-f/i){$SendFrom  =$ARGV[++$i]; $Ask{'SendFrom'}=(1==0);}
    elsif($ARGV[$i]=~ /^-t/i){$SendTo    =$ARGV[++$i]; $Ask{'SendTo'}  =(1==0);}
    elsif($ARGV[$i]=~ /^-c/i){$casenr    =$ARGV[++$i]; $Ask{'casenr'}  =(1==0);}
    elsif($ARGV[$i]=~ /^-s/ ){$MServer   =$ARGV[++$i]; $Ask{'MServer'} =(1==0);}
    elsif($ARGV[$i]=~ /^-p/i){$RootPasswd=$ARGV[++$i]; $Ask{'Passwd'}  =(1==0);}
    #
    elsif($ARGV[$i]=~ /^-v/i){
        if (defined ($ARGV[$i+1]) and $ARGV[$i+1] =~ /^-?\d+$/) {
        $Options{'Verbose'}=$ARGV[++$i];
        }
        else {
        print "ERROR:  The -v option was specified without a following number.\n";
        Usage();
        exit 1;
        }
    }
    elsif($ARGV[$i]=~ /^-q/i){$Options{'Verbose'}=-1;}
    elsif($ARGV[$i]=~ /^-S/ ){$Options{'Secret'}=1  ;}
    elsif($ARGV[$i]=~ /^-m/i){$Options{'Mail'}=1    ;}
    #
    elsif($ARGV[$i]=~ /^-(h|help|-help)/i){ Usage() ; exit 0 ; }
    }
    else {
    Usage();
    exit 1;
    }
}

print qq(\n    $progtitle.\n\n    Capturing data on a GroundWork Monitor version $gw_version system.\n\n);

# Get the db.properties values for monarch and foundation database credentials
# Use standard credentials for Monarch DB manipulation.

my $dbtype          = undef;
my $Foundation_Host = undef;
my $Foundation_User = undef;
my $Foundation_Pass = undef;
my $Monarch_Host    = undef;
my $Monarch_User    = undef;
my $Monarch_Pass    = undef;
my $properties_file = $GW_HOME."/config/db.properties";

if ( !open( FILE, '<', $properties_file ) ) {
    print "ERROR:  Cannot open $properties_file ($!).\n";
    exit 1;
}

while ( my $line = <FILE> ) {
    if    ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/  ) { $dbtype          = $1 }
    elsif ( $line =~ /^\s*collage\.dbhost\s*=\s*(\S+)/   ) { $Foundation_Host = $1 }
    elsif ( $line =~ /^\s*collage\.username\s*=\s*(\S+)/ ) { $Foundation_User = $1 }
    elsif ( $line =~ /^\s*collage\.password\s*=\s*(\S+)/ ) { $Foundation_Pass = $1 }
    elsif ( $line =~ /^\s*monarch\.dbhost\s*=\s*(\S+)/   ) { $Monarch_Host    = $1 }
    elsif ( $line =~ /^\s*monarch\.username\s*=\s*(\S+)/ ) { $Monarch_User    = $1 }
    elsif ( $line =~ /^\s*monarch\.password\s*=\s*(\S+)/ ) { $Monarch_Pass    = $1 }
}

close(FILE);

# Historical default.  Will be 'postgresql' as of GWMEE 6.6.
$dbtype = 'mysql' if not defined $dbtype;

#-----------------------------------------------------------------------
# Decide what OS we're using. Our needed utilities vary slighty
# as to location depending on OS.
#-----------------------------------------------------------------------
my $cat        = Valid_Utility( "/bin/cat" );
#-----------------------------------------------------------------------
my $OS         = "UNK" ; my $OS_Title = "Unknown OS";
# FIX MINOR:  Test the following automatic detection on SLES12.
# And when you do so, test also the $env and $lsof settings below.
#-----------------------------------------------------------------------
my @etc_issue  = run_cmd_nolog( "Is it RedHat, CentOS, SuSE, or Ubuntu?", $cat." /etc/issue" );
for ( my $i = 0 ; $i <= $#etc_issue ; $i++ ) {
    if ( $etc_issue[$i] =~ /(Red Hat Enterprise Linux Server|CentOS) release 4/i) {
    $OS = "RHEL4"; $OS_Title = $etc_issue[$i]; last;
    }
    elsif ( $etc_issue[$i] =~ /(Red Hat Enterprise Linux Server|Red Hat .* Enterprise Linux Server|CentOS) release 5/i) {
    $OS = "RHEL5"; $OS_Title = $etc_issue[$i]; last;
    }
    elsif ( $etc_issue[$i] =~ /(Red Hat Enterprise Linux Server|CentOS( Linux)?) release 6/i) {
    $OS = "RHEL6"; $OS_Title = $etc_issue[$i]; last;
    }
    elsif ( $etc_issue[$i] =~ /SUSE Linux Enterprise Server 10 /i ) {
    $OS = "SLES10"; $OS_Title = $etc_issue[$i]; last;
    }
    elsif ( ($etc_issue[$i] =~ /SUSE Linux Enterprise Server 11 /i) ||
        ($etc_issue[$i] =~ /\*\*\* GroundWork Monitor Enterprise Quickstart SUSE Powered Virtual Appliance/i ) ) {
    $OS = "SLES11"; $OS_Title = $etc_issue[$i]; last;
    }
    ## FIX MINOR:  test on SLES12, and add appropriate lines here
    elsif ( $etc_issue[$i] =~ /Ubuntu/i ) {
    $OS = "UBUNTU"; $OS_Title = $etc_issue[$i]; last;
    }
}
#-----------------------------------------------------------------------
# FIX MINOR:  This has been tested on CentOS7, but not yet on RHEL7.
if ( $OS =~ /UNK/ && -f '/etc/system-release' ) {
    my @etc_system_release = run_cmd_nolog( "Is it RedHat, CentOS, SuSE, or Ubuntu?", $cat . " /etc/system-release" );
    for ( my $i = 0 ; $i <= $#etc_system_release ; $i++ ) {
    if ( $etc_system_release[$i] =~ /(Red Hat Enterprise Linux Server|CentOS Linux) release 7/i ) {
        $OS = "RHEL7"; $OS_Title = $etc_system_release[$i]; last;
    }
    }
}
#-----------------------------------------------------------------------
# If we can't determine the OS, ask the user.
#-----------------------------------------------------------------------
if ( $OS =~ /UNK/ ) {

    print qq(.

    Please specify your Linux Distribution:

    1 - RHEL4/CentOS4
    2 - RHEL5/CentOS5
    3 - RHEL6/CentOS6
    4 - RHEL7/CentOS7
    5 - SLES10
    6 - SLES11
    7 - SLES12
    8 - UBUNTU

    );

    for (my $tmp = "" ; $tmp !~ /(1|2|3|4|5|6|7|8|RHEL4|RHEL5|RHEL6|RHEL7|SLES10|SLES11|SLES12|UBUNTU)/i ; ) {
    $tmp = Ask_About( qq(Enter 1=RHEL4 or CentOS4, 2=RHEL5 or CentOS5, 3=RHEL6 or CentOS6,\n    4=RHEL7 or CentOS7, 5=SLES10, 6=SLES11, 7=SLES12, or 8=UBUNTU), $OS );
    if    ( $tmp =~ /^(1|RHEL4)$/i  ) { $OS = "RHEL4"; }
    elsif ( $tmp =~ /^(2|RHEL5)$/i  ) { $OS = "RHEL5"; }
    elsif ( $tmp =~ /^(3|RHEL6)$/i  ) { $OS = "RHEL6"; }
    elsif ( $tmp =~ /^(4|RHEL7)$/i  ) { $OS = "RHEL7"; }
    elsif ( $tmp =~ /^(5|SLES10)$/i ) { $OS = "SLES10"; }
    elsif ( $tmp =~ /^(6|SLES11)$/i ) { $OS = "SLES11"; }
    elsif ( $tmp =~ /^(7|SLES12)$/i ) { $OS = "SLES12"; }
    elsif ( $tmp =~ /^(8|UBUNTU)$/i ) { $OS = "UBUNTU"; }
    }
}

# FIX MINOR:  This hasn't yet been tested on SLES12.
if ($OS eq "SLES10" || $OS eq "SLES11" || $OS eq "SLES12" || $OS eq "UBUNTU")
    {
    $env  = "/usr/bin/env";
    $lsof = "/usr/bin/lsof";
    }
#-----------------------------------------------------------------------
# Find the rest of the utilities we need.  If we can't find it,
# Valid_Utility() asks the user unless the optional second argument
# is true, in which case the location is substituted with a dummy
# command.  The option of substituting a dummy command (effectively,
# ignoring that utility) is supported to allow this script to run
# in both MySQL and PostgreSQL environments, without complaint in
# either environment that tools used in the opposing environment are
# not present.  Of course, this means that the tools we apply this
# option to must be in their expected, predetermined locations in the
# releases where we need them to operate.
#-----------------------------------------------------------------------
my $phpinfo      = Valid_Utility( $GW_HOME."/php/bin/php"           );
my $gwservice    = Valid_Utility( "/etc/init.d/groundwork"          );
my $devclean     = Valid_Utility( $GW_HOME."/tools/devclean.pl"     );
my $chkconfig    = Valid_Utility( "/sbin/chkconfig"                 );
my $cd           = Valid_Utility( "/bin/bash"                       );
my $cp           = Valid_Utility( "/bin/cp"                         );
my $date         = Valid_Utility( "/bin/date"                       );
my $shortdate    = Valid_Utility( "/bin/date"                       );
my $df           = Valid_Utility( "/bin/df"                         );
my $echo         = Valid_Utility( "/bin/echo"                       );
my $grep         = Valid_Utility( "/bin/grep"                       );
my $egrep        = Valid_Utility( "/bin/egrep"                      );
my $find         = Valid_Utility( "/usr/bin/find"                   );
my $gzip         = Valid_Utility( "/bin/gzip"                       );
my $hostname     = Valid_Utility( "/bin/hostname"                   );
my $ifconfig     = Valid_Utility( "/sbin/ifconfig"                  );
my $id           = Valid_Utility( "/usr/bin/id"                     );
my $iptablessave = Valid_Utility( "/sbin/iptables-save"             );
my $java         = Valid_Utility( $GW_HOME."/java/bin/java"         );
my $logrotate    = Valid_Utility( "/usr/sbin/logrotate"             );
my $du           = Valid_Utility( "/usr/bin/du"                     );
my $ls           = Valid_Utility( "/bin/ls"                         );
my $lsbrelease   = Valid_Utility( "/usr/bin/lsb_release"            );
my $pg_dump      = Valid_Utility( $GW_HOME."/postgresql/bin/pg_dump", 1 );
my $pg_top       = Valid_Utility( $GW_HOME."/postgresql/bin/pg_top",  1 );
my $psql         = Valid_Utility( $GW_HOME."/postgresql/bin/psql",    1 );
my $mysql        = Valid_Utility( $GW_HOME."/mysql/bin/mysql",        1 );
my $mysqldump    = Valid_Utility( $GW_HOME."/mysql/bin/mysqldump",    1 );
my $mytop        = Valid_Utility( $GW_HOME."/perl/bin/mytop",         1 );
my $nagiostats   = Valid_Utility( $GW_HOME."/nagios/bin/nagiostats" );
my $nagios       = Valid_Utility( $GW_HOME."/nagios/bin/nagios"     );
my $netstat      = Valid_Utility( "/bin/netstat"                    );
my $php          = Valid_Utility( $GW_HOME."/php/bin/php"           );
my $ps           = Valid_Utility( "/bin/ps"                         );
my $rm           = Valid_Utility( "/bin/rm"                         );
my $rpm          = Valid_Utility( "/bin/rpm"                        );
my $SendEmail    = Valid_Utility( $GW_HOME."/common/bin/sendEmail"  );
my $sort         = Valid_Utility( "/bin/sort"                       );
my $head         = Valid_Utility( "/usr/bin/head"                   );
my $tail         = Valid_Utility( "/usr/bin/tail"                   );
my $tar          = Valid_Utility( "/bin/tar"                        );
my $top          = Valid_Utility( "/usr/bin/top"                    );
my $uptime       = Valid_Utility( "/usr/bin/uptime"                 );
my $uname        = Valid_Utility( "/bin/uname"                      );
my $vmstat       = Valid_Utility( "/usr/bin/vmstat"                 );
my $which        = Valid_Utility( "/usr/bin/which"                  );
my $whoami       = Valid_Utility( "/usr/bin/whoami"                 );
$lsof            = Valid_Utility( "$lsof"                           );
#-------------------------------------------------------------------
# Ask the user about anything we need to know before continuing.
#-------------------------------------------------------------------
if ($Ask{'SendFrom'}) {
    if ($Options{'Verbose'}>-1) {
    print qq(

    A From eMail address is required.  This should be a real eMail
    address, because many mailservers will check for bogus From
    addresses as an indication of spam.  If you're sending the results
    of this tool to GroundWork, please use the email address that
    you use to communicate with GroundWork Customer Support.

    );}
    $SendFrom = Ask_About( qq(Send From Email Address), $SendFrom );
}

if ($Ask{'SendTo'}) {
    if ($Options{'Verbose'}>-1) {
    print qq(

    The SendTo eMail address defaults to a Diagnostic group at
    GroundWork OpenSource.  If your server can't send email to the
    outside world for security reasons, then you can use your own
    email address.  You could then forward the results on to us or
    use it yourself for diagnosing problems.

    );}
    $SendTo   = Ask_About( qq(Send To Email Address)  , $SendTo   );
}

if ($Ask{'casenr'}) {
    if ($Options{'Verbose'}>-1) {
    print qq(

    The Case number is assigned by the GroundWork Case Management System.
    If you know this number, input it here.  If this diagnostic is for a
    new case, you can leave it to the default.  If there is an existing
    case and you don't know its number, put your company name here.  Your
    input here will be prepended to the email's subject line.

    );}
    $casenr   = Ask_About( qq(Case Number),             $casenr   );
}

if ($Ask{'MServer'}) {
    if ($Options{'Verbose'}>-1) {
    print qq(

    The default mail server, $MServer, may or may not work on your
    GW Monitoring server.  It may not be authorized to relay mail
    externally.  Enter your company's SMTP eMail server's name.

    );}
    $MServer  = Ask_About( qq(Mail Server),             $MServer  );
}
if ($dbtype eq 'postgresql') {
    ## Unlike $MySQLPasswd, we don't actually use the $PostgreSQLPasswd value anywhere in
    ## the current version of this gwdiags.pl script, so there's no sense in asking for it.
    if (0) {
    $PostgreSQLPasswd = $RootPasswd;
    if ($Ask{'Passwd'}) {
        if ($Options{'Verbose'}>-1) {
        print qq(

    If your PostgreSQL postgres user has a password, enter it here.
    If it doesn't have a postgres-user password, just hit a <CR>.

    );}
        $PostgreSQLPasswd = Ask_About( qq(Optional: PostgreSQL postgres user password <CR=skip or no password>), $PostgreSQLPasswd );
        print "\n";
    }
    }
}
elsif ($dbtype eq 'mysql') {
    $MySQLPasswd = $RootPasswd;
    if ($Ask{'Passwd'}) {
    if ($Options{'Verbose'}>-1) {
    print qq(

    If your MySQL root user has a password, enter it here.
    If it doesn't have a root-user password, just hit a <CR>.

    );}
    $MySQLPasswd = Ask_About( qq(Optional: MySQL root user password <CR=skip or no password>), $MySQLPasswd );
    print "\n";
    }
}

#-----------------------------------------------------------------------
# Create Tempfile.
#------------------------------------------------------------------------

# First, clean up any cruft left over from either previous runs or manual unwrapping
# of previously created tarballs.  This both avoids confusing the results we generate
# in this run with stuff from previous runs, and prevents any stuff left over from a
# possible non-Secret previous run from contaminating a Secret run now.
print "Cleaning up temporary directories ...\n" if $Options{'Verbose'} > 0;
system $rm." -fr ".$tempfiledir;
system $rm." -fr ".$tempfiledir2;
system $rm." -f ".$tempdir."/*";

system $makedir." ".$tempdir.$suppress_errors;
system $makedir." ".$tempfiledir.$suppress_errors;
system $makedir." ".$tempfiledir2.$suppress_errors;
my ($tfh, $tfn) = tempfile(
     TEMPLATE => $temptemplate,
     DIR      => $tempdir,
     SUFFIX   => $tempsuffix
   );
print $tfh $progtitle."\n\n";

#-----------------------------------------------------------------------
# Convert nagios.log epoch time to readable time,
# if the nagios.log file exists.
#------------------------------------------------------------------------

if ( !open( NAGIOSIN, '<', $nagioslog ) ) {
    print "ERROR:  Cannot open $nagioslog ($!).\n";
}
else {
    if ( !open( NAGIOSOUT, '>', $nagioslogrt ) ) {
    print "ERROR:  Cannot open $nagioslogrt ($!).\n";
    }
    else {
    my $part1;
    my $part2;
    my $bad_line_count     = 0;
    my $bad_line_threshold = 1;
    while (<NAGIOSIN>) {
        if (/^\[\d+\] /) {
        ( $part1, $part2 ) = split( /\s+/, $_, 2 );
        $part1 =~ s/\[//;
        $part1 =~ s/\]//;
        print NAGIOSOUT "[", scalar localtime($part1), "]\;$part2";
        }
        else {
        if ( ++$bad_line_count >= $bad_line_threshold ) {
            print "ERROR:  The $nagioslog file is corrupt (instance $bad_line_count) at line $..\n";
            print "        See the surrounding lines for the source of the problem.\n";
            print "        The bad line is:  $_";
            print $tfh "ERROR:  The $nagioslog file is corrupt (instance $bad_line_count) at line $..\n";
            print $tfh "        See the surrounding lines for the source of the problem.\n";
            print $tfh "        The bad line is:  $_";
            ## Printing an error message on every single bad input line would be excessive.
            ## But it's reasonable to continually back off the threshold each time it is hit,
            ## so if there are lots more bad lines we do get some indication of that fact.
            $bad_line_threshold *= 10;
        }
        print NAGIOSOUT $_;
        }
    }
    ## We give a summary of the badness here because we probably skipped reporting most of it.
    ## It's helpful to make everybody be aware of the full extent.
    if ($bad_line_count) {
        print "ERROR:  $bad_line_count corrupted lines were found in the $nagioslog file.\n";
        print $tfh "ERROR:  $bad_line_count corrupted lines were found in the $nagioslog file.\n\n";
    }
    close(NAGIOSOUT);
    }
    close(NAGIOSIN);
}

system `/bin/bash $setenv.$suppress_errors`;

#-----------------------------------------------------------------------
# Run the Commands.
#-----------------------------------------------------------------------
my $cdGWH   = "cd ".$GW_HOME." ; ";
my $GWHFind = $cdGWH.$find." . -type";

########################################################################
# SYSTEM INFORMATION
########################################################################
print $tfh "########################################################################
# SYSTEM INFORMATION
########################################################################";

#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What is the System Date?"

    ,$date." -R"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What User are we running as?"

    ,$whoami

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What Linux Kernel are we running?"

    ,$uname." -a"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What OS Distribution are we running?"

    ,$lsbrelease." -a"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What's the Fully Qualified Hostname?"

    ,$hostname

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What interfaces are on this server?"

    ,$ifconfig

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Is /etc/hosts set correctly?"

    ,$cat." /etc/hosts"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What Firewall Rules are active?"

    ,$iptablessave

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Is SELinux disabled?"

    ,$cat." /etc/selinux/config"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What's our Environment?"

    ,$env.$suppress_errors

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Memory Size and Usage."

    ,$vmstat." -s "

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Processor Architecture"

    ,$cat." /proc/cpuinfo"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "All Filesystems"

    ,"cd / ; ".$df." -h ./"

    );
########################################################################
# GROUNDWORK INSTALL
########################################################################
print $tfh "########################################################################
# GROUNDWORK INSTALL
########################################################################";
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Installed GroundWork Version"

    ,$cat." $GW_HOME/Info.txt"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "GroundWork's Filesystem."

    ,$cdGWH.$df." -h ./"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    ## We might have a remote PostgreSQL database, in which case we won't
    ## be able to determine its size from here.
    if (-d "$GW_HOME/postgresql/data") {
        run_cmd( "PostgreSQL's Filesystem"

            ,"cd $GW_HOME/postgresql/data/; ".$df." -h ./"

            );
    }
    elsif ($Options{'Verbose'}> 1) {
        ## Possibly, in some future version of this script, we could ssh over
        ## to the database server, and check the filesystem size from there.
        print $Dashes;
        print "PostgreSQL's Filesystem:\n";
        print "There is no local data/ directory for the PostgreSQL database;\n";
        print "presumably, you must be running a remote PostgreSQL database.\n";
        print "So we cannot determine the size of that file tree from here.\n";
        print $Dashes;
    }
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "MySQL's Filesystem"

        ,"cd $GW_HOME/mysql/data/; ".$df." -h ./"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Correct environment for perl",$which." "."perl" );
run_cmd( "Correct environment for java",$which." "."java" );
run_cmd( "Correct environment for php",$which." "."php" );
run_cmd( "Correct environment for apache",$which." "."httpd" );
run_cmd( "Correct environment for rrdtool",$which." "."rrdtool" );
run_cmd( "Correct environment for snmptt",$which." "."snmptt" );
run_cmd( "Correct environment for snmptrapd",$which." "."snmptrapd" );
run_cmd( "Correct environment for syslog-ng",$which." "."syslog-ng" );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What are the UID/GID values for nagios?"

    ,$id." nagios"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "What are passwd and group entries for nagios?"

    ,$grep." -H nagios /etc/passwd /etc/group"

    );
#-----------------------------------------------------------------------
# Do an rpm -qa and analyze it.
#-----------------------------------------------------------------------
my @tmp = run_cmd_nolog( "What packages do we have installed?", $rpm." -qa | ".$sort );

my @rpm_groundwork = ();
my @rpm_postgresql = ();
my @rpm_mysql      = ();
my @rpm_qa         = ();

for (my $i=0; $i<=$#tmp; $i++) {
    if    ($tmp[$i] =~ /groundwork/i) { push (@rpm_groundwork,$tmp[$i]); }
    elsif ($tmp[$i] =~ /postgres/i  ) { push (@rpm_postgresql,$tmp[$i]); }
    elsif ($tmp[$i] =~ /mysql/i     ) { push (@rpm_mysql     ,$tmp[$i]); }
    else                              { push (@rpm_qa        ,$tmp[$i]); }
}
#-----------------------------------------------------------------------
# We've Split out the groundwork entries already. The command expressed
# now becomes - "What command line would the user use to verify what
# we're reporting?"
#-----------------------------------------------------------------------
Print_Results( "Any GroundWork rpms installed?"
          ,$rpm." -qa | ".$egrep." -i groundwork"
          ,join("",@rpm_groundwork)
          );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    Print_Results( "What PostgreSQL Version do we have installed?"
          ,$rpm." -qa | ".$egrep." -i postgres"
          ,join("",@rpm_postgresql)
          );
}
elsif ($dbtype eq 'mysql') {
    Print_Results( "What MySQL Version do we have installed?"
          ,$rpm." -qa | ".$egrep." -i mysql"
          ,join("",@rpm_mysql)
          );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
Print_Results( "What other java packages do we have installed?"
          ,$rpm." -qa | ".$grep." -i jcs"
          ,$rpm." -qa | ".$grep." -i java"
          );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Which version of Php?"

    ,$php." -version"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Which version of Java?"

    ,$java." -version"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "/usr/bin/java is usually a symbolic link to a specific version"

    ,$ls." -ll /usr/bin/java"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "check for /etc/alternatives symlink"

    ,$ls." -ll /etc/alternatives/java"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "All running processes."

    ,$ps." -aef"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Processes with usages."

    ,$top." -b -n 1"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show the groundwork logrotate config\nGroundWork Logrotate conf"

    ,$cat." /etc/logrotate.d/groundwork"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display Logrotate files"

    ,$ls." -ll " ."/etc/logrotate.d/"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show debug run of logrotate\nLogrotate Debug run"

    ,$cat." "."/etc/logrotate.conf"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show the last ".$Nr_of_Lines." lines of the\nsystem messages log"

    ,$tail." -".$Nr_of_Lines." "."/var/log/messages"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display init.d scripts"

    ,$ls." -ll ". "/etc/rc.d/init.d"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show nagios cron jobs\ncron jobs for nagios"

    ,$cat." "."/var/spool/cron/nagios"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show nobody cron jobs\ncron jobs for nobody"

    ,$cat." "."/var/spool/cron/nobody"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show root cron jobs\ncron jobs for root in /var/spool"

    ,$cat." "."/var/spool/cron/root"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show system root cron jobs\ncron jobs for root in /etc/crontab"

    ,$cat." "."/etc/crontab"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "All listening ports lsof"

    ,$lsof." -i "

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "All listening ports netstat"

    ,$netstat." -plunt "

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------

## We allow for the fact that $chkconfig might be a system Perl script which
## might not comport well with the value for PERL5LIB that we have in place
## at this time.  This happens on SUSE 11, for instance.  If we find that,
## we try running the script under the GroundWork Perl instead.
if ( $chkconfig =~ /Skipping Test/ ) {
    run_cmd( "Show what services are configured\nto start at boot time", "$chkconfig" );
}
elsif ( -x $head && `$head -1 $chkconfig` =~ m{^#!/usr/bin/perl} ) {
    run_cmd( "Show what services are configured\nto start at boot time", "$perlbin $chkconfig --list" );
}
else {
    run_cmd( "Show what services are configured\nto start at boot time", "$chkconfig --list" );
}

#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show groundwork services status"

    ,$gwservice." status"

    );
########################################################################
# PostgreSQL or MySQL
########################################################################
if ($dbtype eq 'postgresql') {
print $tfh "########################################################################
# POSTGRESQL
########################################################################";
}
elsif ($dbtype eq 'mysql') {
print $tfh "########################################################################
# MYSQL
########################################################################";
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "db.properties numeric ID file ownership"

    ,$ls." -ln ".$GW_HOME."/config/db.properties"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    run_cmd( "data sizes $GW_HOME/postgresql/data/"

        ,$du." -sk "."$GW_HOME/postgresql/data/*"

        );
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "ibdata sizes $GW_HOME/mysql/data/"

        ,$ls." -lhH "."$GW_HOME/mysql/data/"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    # pg_top is not available in a useable form in the GWMEE 6.6.0 release,
    # so this won't produce any useful results in that context.  It should
    # work in later releases, though.
    #
    # We su to nagios to reference its DB access credentials.
    run_cmd( "PostgreSQL process list (up to 50 shown)"

        ,"su nagios -c \"$pg_top -b -C -q -d monarch -U monarch 50\""

        );
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "MySQL process list"

        ,$mytop." -b --no-color"

        );
}
########################################################################
# NAGIOS
########################################################################
print $tfh "########################################################################
# NAGIOS
########################################################################";
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Nagios Performance - Active Check\nLatencies, nr of hosts/processes. etc."

    ,$nagiostats." -c ".$GW_HOME."/nagios/etc/nagios.cfg"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Nagios Scheduling Check."

    ,$nagios." -s ".$GW_HOME."/nagios/etc/nagios.cfg"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "is bronx listening?"

    ,$lsof." -i | grep -E 'nsca|5667' ".$suppress_errors

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display permissions of nagios plugins"

    ,$ls." -ll ".$GW_HOME."/nagios/libexec"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Check to see what kind of\neventhandlers we may have installed"

    ,$ls." -ial ".$GW_HOME."/nagios/eventhandlers/"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "nagios var directory permissions"

    ,$ls." -ial ".$GW_HOME."/nagios/var/"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "nagios var/log directory permissions"

    ,$ls." -ial ".$GW_HOME."/nagios/var/log"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "nagios var/spool directory permissions"

    ,$ls." -ial ".$GW_HOME."/nagios/var/spool"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show the last ".$Nr_of_Lines." lines of the\nprocess_service_perf.log"

    ,$tail." -".$Nr_of_Lines." ".$GW_HOME."/nagios/var/log/process_service_perfdata_file.log"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Check to see number of nagios logs\n"

    ,$ls." -ial ".$GW_HOME."/nagios/var/archives/"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display permissions of rrds"

    ,$ls." -lt ".$GW_HOME."/rrd"

    );
########################################################################
# FOUNDATION
########################################################################
print $tfh "########################################################################
# FOUNDATION
########################################################################";
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    run_cmd( "Show gwcollagedb Schema Version"

        ,$env." PGPASSWORD='".$Foundation_Pass."' ".$psql." -w -U ".$Foundation_User." -c 'select * from schemainfo;' gwcollagedb"

        );
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "Show GWCollageDB Schema Version"

        ,$mysql." -u ".$Foundation_User." -p".$Foundation_Pass." GWCollageDB -e 'select * from SchemaInfo;'"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    run_cmd( "Show Device Count"

        ,$env." PGPASSWORD='".$Foundation_Pass."' ".$psql." -w -U ".$Foundation_User." -c 'select count(*) from device;' gwcollagedb"

        );
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "Show Device Count"

        ,$mysql." -u ".$Foundation_User." -p".$Foundation_Pass." GWCollageDB -e 'select count(*) from Device;'"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show device count and type"

    ,$devclean

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    # WARNING:  Because of its internal support for concurrency (called
    # Multiversion Concurrency Control, or MVCC, instead of locking),
    # PostgreSQL does not keep around a global copy of the count of all
    # the rows in each table.  So if you do a "select count(*)" without any
    # qualifying WHERE clause, it will walk the full table at that time.
    # For a table such as the logmessage table, which can contain a truly
    # huge number of rows, this can be incredibly slow (and presumably,
    # it would consume a lot of resources).  We might want to find some
    # other means to collect an equivalently useful but different statistic
    # for this type of database.
    run_cmd( "Show LogMessage Count"

        ,$env." PGPASSWORD='".$Foundation_Pass."' ".$psql." -w -U ".$Foundation_User." -c 'select count(*) from logmessage;' gwcollagedb"

        );
}
elsif ($dbtype eq 'mysql') {
    run_cmd( "Show LogMessage Count"

        ,$mysql." -u ".$Foundation_User." -p".$Foundation_Pass." GWCollageDB -e 'select count(*) from LogMessage;'"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show all temporary foundation files"

    ,$ls." -ll ".$GW_HOME."/tmp"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display permissions of foundation logs"

    ,$ls." -lt ".$GW_HOME."/foundation/container/logs | ".$head." -10"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "is foundation listening?"

    ,$lsof." -i | grep 4913 "

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "foundation file permissions"

    ,$ls." -lhH ".$GW_HOME."/config/"

    );
########################################################################
# PORTAL
########################################################################
print $tfh "########################################################################
# PORTAL
########################################################################";
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show the last ".$Nr_of_Lines." lines of the\napache error_log"

    ,$tail." -".$Nr_of_Lines." ".$GW_HOME."/apache2/logs/error_log"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show permissions on login-config.xml"

    ,$ls." -ll ".$FOUNDATION_ROOT."/webapps/jboss/jboss-portal.sar/conf/"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Show PHP info details"

    ,"echo '<?php phpinfo(); ?>' |$phpinfo"

    );
########################################################################
# MISCELLANEOUS
########################################################################
print $tfh "########################################################################
# MISCELLANEOUS
########################################################################";
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    run_cmd( "Show monarch groups"

        ,$env." PGPASSWORD='".$Monarch_Pass."' ".$psql." -w -U ".$Monarch_User." -c 'select name from monarch_groups;' monarch"

        );
}
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
run_cmd( "Display snmp trap count"

    ,$ls." -ll ". "$GW_HOME/common/var/spool/snmptt/"." | wc -l"

    );
#-----------------------------------------------------------------------
#-----------------------------------------------------------------------
print $tfh "\n".$Dashes;
print "\n";
#-----------------------------------------------------------------------
# Close temp file, dump monarch database, create tar for all logs,
# mail to support, and exit.
#-----------------------------------------------------------------------
if ($dbtype eq 'postgresql') {
    my $wait_status;

    # FIX LATER:  possibly, we might want to use the --lock-wait-timeout option on the pg_dump commands,
    # to regain control if we cannot obtain a shared table lock within a reasonable time

    # FIX LATER:  possibly, we might want to use the --serializable-deferrable option on the pg_dump commands,
    # to guarantee consistency of the dumped values

    # FIX LATER:  we ought to support the --port=$port option on the pg_dump commands,
    # once we support specifying ports in db.properties

    # We "su nagios" below to run the dumps to avoid putting a password in the
    # environment.  But since the $tempdir directory (where the dump files are
    # placed) is created and owned by root (that is, by whatever user is running
    # this script), it won't necessarily be permitted to the nagios user to
    # write the dump files into.  Thus we need to create the dump files and
    # change their ownership, before running the dumps.

    print "Dumping the monarch database ...\n" if $Options{'Verbose'} > 0;
    unlink $postgresql_monarchdump;
    if (sysopen( DUMP, $postgresql_monarchdump, O_WRONLY | O_NOFOLLOW | O_EXCL | O_CREAT | O_TRUNC, 0600 )) {
    chown( (getpwnam('nagios'))[2,3], \*DUMP );
    $wait_status = system("su nagios -c \"$pg_dump --host='$Monarch_Host' --username=$Monarch_User --no-password --file=$postgresql_monarchdump --format=plain --clean --encoding=LATIN1 monarch\"");
    if ($wait_status) {
        print "ERROR:  Could not dump the monarch database.\n";
    }
    close DUMP;
    }
    else {
    print "ERROR:  Could not open the monarch dump file\n    ($postgresql_monarchdump; $!).\n";
    }

    print "Dumping the gwcollagedb device table ...\n" if $Options{'Verbose'} > 0;
    unlink $postgresql_devicetbl;
    if (sysopen( DUMP, $postgresql_devicetbl, O_WRONLY | O_NOFOLLOW | O_EXCL | O_CREAT | O_TRUNC, 0600 )) {
    chown( (getpwnam('nagios'))[2,3], \*DUMP );
    $wait_status = system("su nagios -c \"$pg_dump --host='$Foundation_Host' --username=$Foundation_User --no-password --file=$postgresql_devicetbl --format=plain --clean --encoding=LATIN1 --table=public.device gwcollagedb\"");
    if ($wait_status) {
        print "ERROR:  Could not dump the gwcollagedb.public.device table.\n";
    }
    close DUMP;
    }
    else {
    print "ERROR:  Could not open the gwcollagedb.public.device dump file\n    ($postgresql_devicetbl; $!).\n";
    }

    # FIX MINOR:  Also find how to display important database statistics.
}
elsif ($dbtype eq 'mysql') {
    print "Dumping the monarch database ...\n" if $Options{'Verbose'} > 0;
    system $mysqldump." -u $Monarch_User -p$Monarch_Pass monarch > ".$mysql_monarchdump;
    print "Dumping the GWCollageDB Device table ...\n" if $Options{'Verbose'} > 0;
    system $mysqldump." -u $Foundation_User -p$Foundation_Pass GWCollageDB Device > ".$mysql_devicetbl;
    print "Dumping database statistics ...\n" if $Options{'Verbose'} > 0;
    if ($MySQLPasswd eq "") {
    system $mysql." -u root -te 'show global status;' > ".$mysql_globalstats.$suppress_errors;
    }
    else {
    system $mysql." -u root -p".$MySQLPasswd." -te 'show global status;' > ".$mysql_globalstats.$suppress_errors;
    }
}
close $tfh;

if ($Options{'Outfile'}) {
    system $cp." ".$tfn." ".$Options{'Outfile'};
}
print "Summarizing results ...\n" if $Options{'Verbose'} > 0;
copy($tfn,$diagsummaryfile);
# Parse the results for a quick summary
my $dashes = "--------------------------------------------------------------------------------------\n";
if (open (RESULTS, '<', "$tfn")) {
    my ($tqh, $tqn) = tempfile(
    TEMPLATE => $temptemplate,
    DIR      => $tempdir,
    SUFFIX   => $tempsuffix
    );
    print $tqh "Quick Summary of Diagnostics:\n";
    while (my $line = <RESULTS>) {
    if ($line=~/^#/) {
        next;
    }
    # pull interesting lines into another file
    if ($dbtype eq 'postgresql') {
        ## FIX MINOR:  do the same for PostgreSQL; pull critical statistics
    }
    elsif ($dbtype eq 'mysql') {
        if ($line =~ /innodb_buffer_pool_size/) {
         print $tqh  "MySQL memory allocation: $line";
         print $tqh $dashes;
        }
    }
    if ($line =~ /^\s+JAVA_OPTS="-X/) {
         print $tqh  "Java memory allocation: $line";
         print $tqh $dashes;
    }
    if ($line =~ /Total Services:/) {
         print $tqh $dashes;
         print $tqh  "Nagios thinks that:  $line";
    }
    if ($line =~ /Active Service Latency:/) {
         print $tqh  "Nagios thinks that:  $line";
    }
    if ($line =~ /Total Hosts:/) {
         print $tqh  "Nagios thinks that:  $line";
         print $tqh $dashes;
    }
    if ($line =~ /Number of devices Total:/) {
         print $tqh  "Device Count:\n\t$line";
    }
    if ($line =~ /Total Unused Devices:/) {
         print $tqh  "\t$line";
         print $tqh $dashes;
    }
    if ($line =~ /max_execution_time =>/) {
         print $tqh  "PHP Configuration Limits:\t\tDirective => Local Value => Master Value\nPHP is limiting execution time to:\t$line";
    }
    if ($line =~ /max_input_time =>/) {
         print $tqh  "PHP is limiting script input to:\t$line";
    }
    if ($line =~ /memory_limit =>/) {
         print $tqh  "PHP is limiting memory to:\t\t$line";
         print $tqh $dashes;
    }
    }
    close $tqh;
    close RESULTS;
    copy($tqn,$quicksummaryfile);
    unlink $tqn;
} else {
    print "Error parsing results file $tfn\n";
}

unlink $tfn;

print "Copying files ...\n" if $Options{'Verbose'} > 0;
foreach my $key ( keys %filehash ) {
    if ( $filehash{$key} == $ISNTSECRET || !$Options{'Secret'} ) {
    system $cp . " -L -R --preserve=all $key $tempfiledir" . "$suppress_errors";
    }
}

my $separate_foundation_directory = $gw_vstring ge v7.0.0 ? "$GW_HOME/jpp2"         : "$GW_HOME/foundation/container2";
my $separate_foundation_name      = $gw_vstring ge v7.0.0 ? 'standalone-Foundation' : 'Dual-JVM';

if ( -d $separate_foundation_directory ) {
    print "Copying $separate_foundation_name files ...\n" if $Options{'Verbose'} > 0;
    foreach my $key ( keys %filehash2 ) {
    if ( $filehash2{$key} == $ISNTSECRET || !$Options{'Secret'} ) {
        system $cp . " -a $key $tempfiledir2/" . "$suppress_errors";
    }
    }
}

chdir($GW_HOME.'/tmp') or die "$!";
print "Creating the gzipped tarball ...\n" if $Options{'Verbose'} > 0;
system $tar." czf $diagoutfile gwdiags/".$suppress_errors;
if ($Options{'Mail'}) {
    print "Sending email ...\n" if $Options{'Verbose'} > 0;
    system $SendEmail." -m Diagnostics, logs and monarch database attached as: "
         .$diagoutfile
         ." -t ".$SendTo
         ." -f ".$SendFrom
         ." -u 'Case ".$casenr.", Diagnostic Results'"
         ." -s ".$MServer
         ." -q "
         ." -a $diagoutfile"
         ;
}

sleep 1;
print "Cleaning up temporary directories ...\n" if $Options{'Verbose'} > 0;
system $rm." -fr ".$tempfiledir;
system $rm." -fr ".$tempfiledir2;
system $rm." -f ".$tempdir."/*";

print "\n\tThe gwdiags results file has been saved as:\n";
print "\t$diagoutfile\n\n";
print "\tPlease attach the results file to your case at:\n";
print "\thttps://cases.groundworkopensource.com\n\n";

#-----------------------------------------------------------------------
exit 0;
#-----------------------------------------------------------------------
# Functions
#=======================================================================
# Valid_Utility
# -------------
#   Validate that the OS utility commands that we need to run are where
#   we expect them to be. If we can't find it, ask the user where it is.
#   Bail if user wants to.
#   --------------------------------------------------------------------
#   $c  -  The full path and filename of the command that we intend to
#          execute.
#   $ignore_if_not_found  -  If true, pretend the user asked to skip
#          the value if the command is not found at $c, instead of
#          asking the user.  To be used mainly for commands that are
#          not present in all releases.
#-----------------------------------------------------------------------
sub Valid_Utility(@) {
    my ($c, $ignore_if_not_found) = @_;
    my $oldc  = $c."";
    my $found = 0;
    my $res = "";
    my $cmd = undef;
#    for ($found = 0 ; $found == 0 ; ) {
    unless (( -e $c )) {
        # look with which
        $c =~ s{^/usr/bin/}{};
        $c =~ s{^/bin/}{};
        $c =~ s{^/sbin/}{};
        $cmd = "which $c > /dev/null 2>&1";
        $res = system($cmd);
        if ( $res > 0 ) {    # which could not find it
        if ($ignore_if_not_found) {
            $found = 2;
        }
        else {
            $c = Ask_About( qq/Can't find $c. Where is it? (filespec, S for Skip, or <CR>=exit)/, "" );
            $c =~ s/^\s+|\s+$//g;
            if ( $c eq 'S' || $c eq 's' ) {
            $found = 2;
            }
            elsif ( $c eq '' || !-f $c || !-x $c ) {
            $found = 3;
            }
        }
        }
        else {
        ## which did find it in the path
        $found = 1;
        }
    }
    else {
        $found = 1;
    }
#    }
    if ($found == 3) {
    die "\nCan't find ".$oldc." ...\n[Exiting ...]\n";
    }
    if ($found == 2) {
    $c = "echo '... [Skipping Test]'";
    }
    return $c;
}
#=======================================================================
# Print_Results
# -------------
#   Output to terminal and/or to the temp file.
#   --------------------------------------------------------------------
#   $com_ment   -   Comment on what the command is trying to accomplish.
#   $c          -   The Command line that was executed.
#   $result     -   Output from the command.
#-----------------------------------------------------------------------
sub Print_Results(@) {
    my ($com_ment, $c, $result) = @_;

    if ($Options{'Verbose'}>0) {
    #---------------------------------------------------------------
    # Print comment and command to the screen, if verbose > 0
    #---------------------------------------------------------------
    print $Dashes.$com_ment."\n".$Dashes.$c."\n".$Dashes."\n.";

    if ($Options{'Verbose'}> 1) {
        #-----------------------------------------------------------
        # Print results to the screen, if verbose > 1
        #-----------------------------------------------------------
        print $Dashes."\nResults:\n\n".$result.$Dashes;
    }
    }
    #-------------------------------------------------------------------
    # Print comment, command and results to the temp file.
    #-------------------------------------------------------------------
    print $tfh "\n".$Dashes.$com_ment."\n".$Dashes.$c."\n\n".$result;
}
#=======================================================================
# run_cmd
# -------
#   Print a comment and command line, execute the command and log the
#   results. The temp file will get the full output. Do this with vary-
#   ing verbosity to the screen.
#   --------------------------------------------------------------------
#   $com_ment   -   Comment on what the command is trying to accomplish.
#   $c          -   The Command line that was executed.
#-----------------------------------------------------------------------
sub run_cmd(@) {
    my ($com_ment, $c) = @_;
    my @results = ();
    if ($Options{'Verbose'}>0) {
    print $Dashes.$com_ment."\n".$Dashes.$c."\n".$Dashes."\n.";
    }
    print $tfh "\n".$Dashes.$com_ment."\n".$Dashes.$c."\n\n";
    die "\nCan't run ".$c."\n[Exiting...]\n" if (!open(TMP,$c." 2>&1 |"));
    while (<TMP>) {
    push @results, $_;
    print $tfh $_;
    if ($Options{'Verbose'}>-1) { print "."; }
    }
    close TMP;
    if ($Options{'Verbose'}>-1) { print "."; }
    if ($Options{'Verbose'}> 0) { print "\n"; }
    if ($Options{'Verbose'}> 1) {
    print $Dashes."\nResults:\n\n";
    print @results;
    print $Dashes;
    }
    return @results;
}
#=======================================================================
# run_cmd_nolog
# -------------
#   Print a comment and command line, execute the command. The temp file
#   will get no output. Do this with varying verbosity to the screen.
#   --------------------------------------------------------------------
#   $com_ment   -   Comment on what the command is trying to accomplish.
#   $c          -   The Command line that was executed.
#-----------------------------------------------------------------------
sub run_cmd_nolog(@) {
    my ($com_ment, $c) = @_;
    my @results = ();
    if ($Options{'Verbose'}>0) {
    print $Dashes.$com_ment."\n".$Dashes.$c."\n".$Dashes."\n.";
    }
    die "\nCan't run ".$c."\n[Exiting...]\n" if (!open(TMP,$c." 2>&1 |"));
    while (<TMP>) {
    push @results, $_;
    if ($Options{'Verbose'}>-1) { print "."; }
    }
    close TMP;
    if ($Options{'Verbose'}>-1) { print "."; }
    if ($Options{'Verbose'}> 0) { print "\n"; }
    if ($Options{'Verbose'}> 1) {
    print $Dashes."\nResults:\n\n";
    print @results;
    print $Dashes;
    }
    return @results;
}
#=======================================================================
# Ask_About
# ---------
#   Ask a question and return the answer.
#   --------------------------------------------------------------------
#   $lbl    -   Qustion to ask.
#   $valu   -   Default answer.
#-----------------------------------------------------------------------
sub Ask_About(@) {
    my ($lbl,$valu) = @_;
    print $lbl." <".$valu."> ? ";
    my $inp = <STDIN>; chomp $inp;
    $valu = $inp unless ($inp eq "");
    #print $lbl.": ".$valu."\n\n";
    return $valu;
}
#=======================================================================
# Usage
# -----
#   Print a usage message.
#-----------------------------------------------------------------------
sub Usage() {
    print qq(

    $progtitle

    $progname [ -f <email> -t <email> -c <case> -s <server> -v <digit> -q -S -m ]
       or
    $progname -h
       or
    $progname --help

    -f   :   From eMail address.

         A From eMail address is required.  This should be a real eMail
         address, because many mailservers will check for bogus From
         addresses as an indication of spam.  If you're sending the re-
         sults of this tool to GroundWork, please use the email address
         that you use to communicate with GroundWork Customer Support.

    -t   :   To eMail address.

         The SendTo eMail address defaults to a Diagnostic group at
         GroundWork OpenSource.  If your server can't send email to the
         outside world for security reasons, then you can use your own
         email address.  You could then forward the results on to us or
         use it yourself for diagnosing problems.

    -m   :   Send diagnostics by email.

         This flag is used if you want to email the diagnostics file to
         GroundWork Support.  The default behavior without this flag is
         that it will NOT email the diagnostics file.  Instead you will
         need to attach the file to the relevant Support case.

    -c   :   Case Number.

         The Case number is assigned by the GroundWork Case Management
         System.  If you know this number, input it here.  If this diag-
         nostic is for a new case, you can leave it to the default.  If
         there is an existing case and you don't know its number, put
         "existing".  Your input here will be prepended to the email's
         subject line.

    -s   :   Mail Server name or IP address.

         The default mail server, $MServer, may or may not work on your
         GW Monitoring server.  It may not be authorized to relay mail
         externally.  Put in your company's SMTP eMail server's name.

    -S   :   Secret

         If this flag is set then a reduced set of information will be
         collected from the system.  This allows organizations who
         operate in classified environments to still be able to provide
         some useful diagnostic information to GroundWork Support.

    -p   :   PostgreSQL postgres user password, or MySQL root user password.
         As a command line arg, enter a null string for 'no password' --
         '-p ""'. In the dialogue, just hit a <CR> at the prompt.

    -v   :   Verbose Flag (-1 thru 9).

         A value of -1 turns off all output to the screen.  As the value
         increases, the amount of output to the screen increases.

    -q   :   synonym for "-v -1".

    -h or --help   :    This usage message.

);
}
#--------------------------------------------------------------------------------
exit 0;
