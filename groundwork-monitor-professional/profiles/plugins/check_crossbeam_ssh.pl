#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Math::BigInt;
use Time::Local;
use lib q(/home/nagios /usr/local/groundwork/nagios/libexec);
use ssh_func;
use parse_func;
use nagios_func;
use fw_func;

use constant PLUGIN => 'check_crossbeam_xseries';

my %functions = ( 'apm_fw1-policy'  => \&apm_fw1_policy,
                  'apm_fw1-license' => \&apm_fw1_license,
                  'apm_fw1-sic'     => \&apm_fw1_sic,
		  'apm_int_list'    => \&apm_interface,
		  'apm_mem'         => \&apm_memory,
		  'apm_uptime'      => \&apm_uptime,
		  'cpm_disk'        => \&cpm_disk,
		  'cpm_int_list'    => \&cpm_interface,
                  'cpm_mem'         => \&cpm_memory,
                  'cpm_uptime'      => \&cpm_uptime,
		  'npm_uptime'      => \&npm_uptime,
		);

my %valid_args = ( 'h' => { 'desc' => 'hostname of device',
                            'required' => 1,
                          },
                   'i' => { 'desc'     => 'IP target host',
                            'required' => 1,
                          },
                   'k' => { 'desc'     => 'SSH private key',
                            'required' => 1,
                          },
                   'l' => { 'desc'     => 'warning/critical levels [warn:crit]',
                            'required' => 0,
                          },
		   'n' => { 'desc'     => 'name of blade (Core_FW-1, et al)',
		            'required' => 1,
			  },
                   't' => { 'desc'     => 'type of check',
                            'required' => 1,
                            'sub'      => \%functions,
                          },
                   'u' => { 'desc'     => 'SSH username',
                            'required' => 1,
                          },
                 );

our %args = parse_args(@ARGV);
foreach my $arg (keys %valid_args) {
   if ($valid_args{$arg}{required} == 1 && ! defined $args{$arg}) {
      print STDERR "\n!!! Missing argument $valid_args{$arg}{desc}\n";
      usage();
   }
   if ($arg eq 't' && ! defined $functions{$args{t}}) {
      print STDERR "\n!!! Invalid type provided: $args{t}\n";
      usage();
   }
}

$functions{$args{t}}() if defined $functions{$args{t}}; 
exit 0;


sub cpm_disk {
   my ($WARN, $CRIT) = parse_levels('disk');
   my $REGEX = qr/\S+\s+(\d+)\s+(\d+)\s+\d+\s+\S+\s+(\S+)/;
   my $RESERVESPACE = 5; # percent of disk reserved by default
   my $IGNOREDPARTITIONS = qr~^(?:/proc|/dev/shm)$~;
   my (@OUTPUT, @PERFDATA) = ();

   my $SSH = ssh_exec( "df -k" );

   foreach my $line (split /\n/, $SSH) {
      my ($blocks, $used, $mount) = $line =~ m/$REGEX/ or next;
      next if $mount =~ $IGNOREDPARTITIONS;
      my $util = sprintf "%.1f", 100*$used/($blocks*((100-$RESERVESPACE)/100));
      if ($util >= $CRIT) {
         push @OUTPUT, "CRITICAL - The parition \"$mount\" is running at " .
                       "$util% capacity (threshold $CRIT%)";
      }
      elsif ($util >= $WARN) {
         push @OUTPUT, "WARNING - The partition \"$mount\" is running at " .
                       "$util% capacity (threshold $WARN%)";
      } 
      else {
         push @OUTPUT, "OK - The partition \"$mount\" is running at $util% " .
                       "capacity";
      }

      push @PERFDATA, "$mount=$util";
   }

   if (my $CRITICAL = scalar grep /CRITICAL/, @OUTPUT) {
      print "CRITICAL - $CRITICAL partitions returned critical status|" .
            (join " ", @PERFDATA) . "\n";
      print join "\n", @OUTPUT;
      exit 2;
   }
   elsif (my $WARNING = scalar grep /WARNING/, @OUTPUT) {
      print "WARNING - $WARNING partitions returned warning status|" .
            (join " ", @PERFDATA) . "\n";
      print join "\n", @OUTPUT;
      exit 1;
   }
   else {
      my $OK = scalar @OUTPUT;
      print "OK - $OK partitions usage okay|" . (join " ", @PERFDATA) . "\n";
      print join "\n", @OUTPUT;
   }

   return;   
}


sub apm_interface {
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;

   my $SSH = ssh_exec( "echo | rsh -l root $BLADENAME 'cat /proc/net/dev'" );

   interface('apm', $SSH);

   return;
}


sub cpm_interface {
   my $SSH = ssh_exec( "cat /proc/net/dev" );

   interface('cpm', $SSH);

   return;
}


sub interface {
   my $TYPE = $_[0];
   my $SSH = $_[1];
   my (@OUTPUT, @traffic) = ();

   foreach my $line (split /\n/, $SSH) {
      next if $line =~ /\||lo:/;
      my ($int_name, $values) = $line =~ m/(\w+):(.*)/;
      my @arrint = split /\s+/, $values;

      next unless ($arrint[1] > 0 || $arrint[9] > 0);
      push @traffic, $int_name;

      my $STATSOUTPUT = "OK - $int_name interface throughput statistics " .
                        "collected";
      my $STATSPERFDATA = "in_bytes=$arrint[0] out_bytes=$arrint[8] " .
                          "int_speed=0";
      my $STATSLONGOUTPUT = "In Bytes: $arrint[0]\nOut Bytes: $arrint[8]";
      nagios_passive_alert($args{h},
                           PLUGIN . "_${TYPE}_interface_stats_${int_name}",
                           0,
                           "${STATSOUTPUT}|${STATSPERFDATA}",
                           $STATSLONGOUTPUT,
                          );

      my $PROBLEMSOUTPUT = "OK - $int_name interface problem statistics " .
                           "collected";
      my $PROBLEMSPERFDATA = "in_error=$arrint[2] out_error=$arrint[10] " .
                             "collisions=$arrint[13] drops=$arrint[3]";
      my $PROBLEMSLONGOUTPUT = "In Errors: $arrint[2]\n" .
                               "Out Errors: $arrint[10]\n" .
                               "Collisions: $arrint[13]\nDrops: $arrint[3]";
      nagios_passive_alert($args{h},
                           PLUGIN . "_${TYPE}_interface_problems_" .
			   "${int_name}",
                           0,
                           "${PROBLEMSOUTPUT}|${PROBLEMSPERFDATA}",
                           $PROBLEMSLONGOUTPUT,
                          );
   }

   print "OK - Interfaces with traffic counters: @traffic";
   return;
}


sub load_average {
   my ($MAXLOAD1MIN, $MAXLOAD5MIN, $MAXLOAD15MIN) = parse_levels('load');
   my $REGEX = qr/(\d+\.\d{2}), (\d+\.\d{2}), (\d+\.\d{2})$/;
   my @OUTPUT = ();

   my $SSH = ssh_exec( "uptime" );

   my ($load1min, $load5min, $load15min) = $SSH =~ /$REGEX/ or eval {
      print "Error - Unable to parse load averages";
   };

   if ($load1min >= $MAXLOAD1MIN) {
      push @OUTPUT, "WARNING - 1 MIN load ($load1min) is abnormally high " .
                    "(threshold $MAXLOAD1MIN)";
   }

   if ($load5min >= $MAXLOAD5MIN) {
      push @OUTPUT, "WARNING - 5 MIN load ($load5min) is abnormally high " .
                    "(threshold $MAXLOAD5MIN)";
   }

   if ($load15min >= $MAXLOAD15MIN) {
      push @OUTPUT, "WARNING - 5 MIN load ($load5min) is abnormally high " .
                    "(threshold $MAXLOAD5MIN)";
   }

   my $PERFDATA = "load1min=$load1min load5min=$load5min load15min=$load15min";

   if (my $WARNING = scalar grep /WARNING/, @OUTPUT) {
      print "WARNING - $WARNING load averages at warning status|$PERFDATA\n";
      print join "\n", @OUTPUT;
      exit 1;
   }
   else {
      my $OK = scalar @OUTPUT;
      print "OK - Load avarages $load1min $load5min $load15min";
   }

   return;
} 

sub apm_memory {
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;
   my $SSH = ssh_exec( "echo | rsh -l root $BLADENAME 'cat /proc/meminfo'" );
   memory($SSH);
   return;
}


sub cpm_memory {
   my $SSH = ssh_exec( "cat /proc/meminfo" );
   memory($SSH);
   return;
}


sub memory {
   my $SSH = shift;
   my ($WARN, $CRIT) = parse_levels('mem');
   my @MEMVAL = qw/MemTotal MemFree Buffers Cached SwapTotal SwapFree/;
   my (@OUTPUT, %HASH) = ();

   foreach my $line (split /\n/, $SSH) {
      foreach my $type (@MEMVAL) {
         $HASH{$type} = $1 if $line =~ /^${type}:\s+(\d+)/;
      }
   }

   my $MEMUSED = sprintf "%d", $HASH{MemTotal} - $HASH{MemFree} - 
                               $HASH{Buffers} - $HASH{Cached};
   my $SWAPUSED = sprintf "%d", $HASH{SwapTotal} - $HASH{SwapFree};
   my $VMTOTAL = sprintf "%d", $HASH{MemTotal} + $HASH{SwapTotal};
   my $VMUSED = sprintf "%d", $MEMUSED + $SWAPUSED;
   my $VMRATIO = sprintf "%d", 100 * $VMUSED / $VMTOTAL;

   my $PERFDATA = "mem_usage=$VMRATIO";

   if ($VMRATIO >= $CRIT) {
      print "CRITICAL - Memory usage at $VMRATIO% (threshold $CRIT%)|$PERFDATA";
      exit 2;
   }
   elsif ($VMRATIO >= $WARN) {
      print "WARNING - Memory usage at $VMRATIO% (threshold $WARN%)|$PERFDATA";
      exit 1;
   }
   else {
      print "OK - Memory usage at $VMRATIO%|$PERFDATA";
   }

   return;
}


sub apm_uptime {
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;
   my $SSH = ssh_exec( "echo | rsh -l root $BLADENAME 'cat /proc/uptime'" );
   uptime($SSH);
   return;
}


sub cpm_uptime {
   my $SSH = ssh_exec( "cat /proc/uptime" );
   uptime($SSH);
   return;
}


sub npm_uptime {
   my ($WARN, $CRIT) = parse_levels('uptime');

   $args{stdin} = <<'__NPMUPTIMESCRIPT';
use strict;
use IO::Socket::INET;
my $NPMSLOT = shift or die "Missing NPM slot as argument #1";
my ($arrPacket, $uptime) = ();
my $socket = IO::Socket::INET->new( PeerHost => "npm${NPMSLOT}",
                                    PeerPort => 23,
                                    Proto    => 'tcp',
                                    Timeout  => 5,
                                  );
$!=2;
die "Unable to TELNET to npm${NPMSLOT}" unless defined $socket;
print $socket "\n";
eval {
   local $SIG{'ALRM'} = sub { die "TELNET timed out waiting for prompt"; };
   alarm 10;
   do {
      unless ($socket->connected) {
         my $lastline = <$socket> || 'connection closed';
         die "TELNET Response: $lastline";
      }
      $arrPacket = <$socket>;
   } until defined $arrPacket && $arrPacket =~ /-> $/;
   alarm 0;
};
$!=2;
die $@ if $@;
print $socket "tickGet\n";
print $socket "\n";
do {
   $arrPacket = <$socket>;
   $uptime = $1 if $arrPacket =~ /value = -?\d+ = (0x[0-9a-z]+)/;
} until $arrPacket =~ /-> $/;
print $socket "logout\n";
print $socket "\n";
close $socket;
print $uptime;
exit 0;
__NPMUPTIMESCRIPT

   my $SSH = ssh_exec( "perl - $args{n}" );
   my $uptimesecs = sprintf "%d", hex($SSH) / 100;
   uptime($uptimesecs);
   return;
}


sub uptime {
   my $SSH = shift;
   my ($WARN, $CRIT) = parse_levels('uptime');

   my ($uptime) = sprintf "%d", $SSH =~ m/^(\d+)/o;
   $uptime /= 60*60*24;
   $uptime = sprintf "%d", $uptime;

   my $PERFDATA="uptime=$uptime";

   if ($uptime >= $CRIT) {
      print "CRITICAL - Device uptime is $uptime days (threshold $CRIT days) " .
            "|$PERFDATA";
   }
   elsif ($uptime >= $WARN) {
      print "WARNING - Device uptime is $uptime days (threshold $WARN days) " .
            "|$PERFDATA";
   }
   else {
      print "OK - Device uptime is $uptime days|$PERFDATA";
   }

   return;
}


sub apm_fw1_policy {
   my ($CONNWARN, $CONNCRIT, $DROPSWARN, $DROPSCRIT) = parse_levels();
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;
   my %FIREWALL = ();

   my $SSH = ssh_exec( "echo | rsh -l root $BLADENAME " .
                       "'source /etc/profile.d/CP.sh; cpstat fw -f policy'" );

   foreach my $line (split /\n/, $SSH) {
      if ($line =~ /^Policy name:\s+(\S+)/) {
         $FIREWALL{policy} = $1;
      }
      elsif ($line =~ /^Num. connections:\s+(\d+)/) {
         $FIREWALL{conns} = $1;
      }
      elsif ($line =~ /^Peak num. connections:\s+(\d+)/) {
         $FIREWALL{peakconns} = $1;
      }
      elsif ($line =~ /^\|\s+\|\s+\|(?:\s+)?(\d+)\|(?:\s+)?(\d+)\|(?:\s+)?(\d+)\|(?:\s+)?(\d+)\|/) {
         $FIREWALL{accepts} = sprintf "%u", $1;
         $FIREWALL{drops} = sprintf "%u", $2;
         $FIREWALL{rejects} = sprintf "%u", $3;
         $FIREWALL{logs} = sprintf "%u", $4;
      }
   }

   if (scalar keys %FIREWALL == 0) {
      print "ERROR - Unable to execute: cpstat fw -f policy";
      exit 2;
   }

   my $connlimit = checkpoint_fw1_state_table_size $args{h};
   if (! defined $connlimit) {
      my $SSH2 = ssh_exec( "echo | rsh -l root $BLADENAME " .
                           "'source /etc/profile.d/CP.sh; fw tab -t connections'" );
      ($connlimit) = $SSH2 =~ m/limit (\d+)/;
      $connlimit ? checkpoint_fw1_state_table_size $args{h}, $connlimit
                 : nagios_passive_alert($args{h},
                                        PLUGIN . '_apm_fw1_state',
                                        2,
                                        "ERROR - Unable to retrieve state " .
                                        "table connection limit"
                                       );
   }

   if (defined $connlimit && defined $FIREWALL{conns}) {
      my $connratio = sprintf "%d", 100*$FIREWALL{conns}/$connlimit;
      my $PERFDATA = "conns=$FIREWALL{conns} limit=0 percent=$connratio";
      my ($CONNOUTPUT, $CONNRETVAL) = ();

      if ($connratio >= $CONNCRIT) {
         $CONNOUTPUT = "CRITICAL - Concurrent connections at " .
                       "$FIREWALL{conns} ($connratio%) of limit $connlimit " .
                       "(threshold $CONNCRIT%)";
         $CONNRETVAL = 2;
      }
      elsif ($connratio >= $CONNWARN) {
         $CONNOUTPUT = "WARNING - Concurrent connections at " .
                       "$FIREWALL{conns} ($connratio%) of limit $connlimit " .
                       "(threshold $CONNWARN%)";
         $CONNRETVAL = 1;
      }
      else {
         $CONNOUTPUT = "OK - Concurrent connections at $FIREWALL{conns} " .
                       "($connratio%) of limit $connlimit";
         $CONNRETVAL = 0;
      }
      nagios_passive_alert($args{h},
                           PLUGIN . '_apm_fw1_state',
                           $CONNRETVAL,
                           "$CONNOUTPUT|$PERFDATA"
                          );
   }
   elsif (! defined $FIREWALL{conns}) {
      nagios_passive_alert($args{h},
                           PLUGIN . '_apm_fw1_state',
                           2,
                           "ERROR - Unable to retrieve FW-1 current connections"
                          );
   }

   if (defined $FIREWALL{accepts} && defined $FIREWALL{drops} && 
       defined $FIREWALL{rejects} && defined $FIREWALL{logs}) {
      $args{l} = "${DROPSWARN}:${DROPSCRIT}";
      my @PKTOUTPUT = checkpoint_fw1_drops($FIREWALL{accepts},
                                           $FIREWALL{drops},
                                           $FIREWALL{rejects},
                                           $FIREWALL{logs},
                                          );
      my $PERFDATA = shift @PKTOUTPUT;

      if (my $CRITICAL = scalar grep /CRITICAL/, @PKTOUTPUT) {
         nagios_passive_alert($args{h},
                              PLUGIN . '_apm_fw1_drops',
                              2,
                              "CRITICAL - $CRITICAL checks returned critical " .
                              "status|$PERFDATA",
                              join "\n", @PKTOUTPUT
                             );
      }
      else {
         my $OK = scalar @PKTOUTPUT;
         nagios_passive_alert($args{h},
                              PLUGIN . '_apm_fw1_drops',
                              0,
                              "OK - $OK checks okay|$PERFDATA",
                              join "\n", @PKTOUTPUT
                             );
      }
   }
   else {
      nagios_passive_alert($args{h},
                           PLUGIN . '_apm_fw1_drops',
                           2,
                           "ERROR - Unable to retrieve FW-1 packet statistics"
                          );
   }
   
   if (defined $FIREWALL{policy}) {
      if ($FIREWALL{policy} eq '-') {
         print "CRITICAL - No FW-1 policy loaded ($FIREWALL{policy})";
         exit 2;
      }
      elsif ($FIREWALL{policy} eq 'defaultfilter') {
         print "CRITICAL - FW-1 using default filter ($FIREWALL{policy})";
         exit 2;
      }
      elsif ($FIREWALL{policy} eq 'InitialPolicy') {
         print "CRITICAL - FW-1 using initial policy ($FIREWALL{policy})";
         exit 2;
      }
      else {
         print "OK - FW-1 using custom policy ($FIREWALL{policy})";
      }
   }
   else {
      print "ERROR - Unable to retrieve current policy";
      exit 3;
   }
      
   return; 
}


sub apm_fw1_license {
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;
   my (%LICENSE, @OUTPUT, %MONTHS) = ();
   my @ARRMONTHS = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
   $MONTHS{$ARRMONTHS[$_]} = $_ foreach (0 .. 11);

   my $SSH = ssh_exec( "echo | rsh -l root $BLADENAME " .
                       "'source /etc/profile.d/CP.sh; cplic print'" );
  
   foreach my $line (split /\n/, $SSH) {
      if ($line =~ /^\S+\s+never/) {
         $LICENSE{perm}++;
         push @OUTPUT, $line;
      }
      elsif ($line =~ /^\S+\s+([0-9]{1,2})([A-Z][a-z]{2})([0-9]{4})/) {
         my ($dd, $mm, $yyyy) = ($1, $2, $3);
         $LICENSE{temp}++;
         push @OUTPUT, $line;
         my $nowutime = time();
         my $exputime = timelocal(59, 59, 23, $dd, $MONTHS{$mm}, $yyyy);
         if ($nowutime > $exputime) {
            $LICENSE{exp}++;
         }
         elsif ($exputime < $nowutime + 86400 * 7) {
            $LICENSE{soon}++;
         }
      }
   }

   $LICENSE{perm} = (exists $LICENSE{perm}) ? $LICENSE{perm} : 0;
   $LICENSE{temp} = (exists $LICENSE{temp}) ? $LICENSE{temp} : 0;
   $LICENSE{soon} = (exists $LICENSE{soon}) ? $LICENSE{soon} : 0;
   $LICENSE{exp} = (exists $LICENSE{exp}) ? $LICENSE{exp} : 0;

   if ($LICENSE{exp} > 0) {
      print "CRITICAL - Found $LICENSE{exp} expired licenses " .
            "(p=$LICENSE{perm}; t=$LICENSE{temp}; s=$LICENSE{soon}; " .
            "x=$LICENSE{exp})\n";
      print join "\n", @OUTPUT;
      exit 2;
   }

   if ($LICENSE{soon} > 0) {
      print "WARNING - Found $LICENSE{soon} licenses expiring soon " .
            "(p=$LICENSE{perm}; t=$LICENSE{temp}; s=$LICENSE{soon}; " .
            "x=$LICENSE{exp})\n";
      print join "\n", @OUTPUT;
      exit 1;
   }

   if ($LICENSE{perm} == 0 && $LICENSE{temp} == 0) {
      print "WARNING - No licenses were found; 15 day eval? " .
            "(p=$LICENSE{perm}; t=$LICENSE{temp}; s=$LICENSE{soon}; " .
            "x=$LICENSE{exp})\n";
      print join "\n", @OUTPUT;
      exit 1;
   }

   print "OK - All licenses valid (p=$LICENSE{perm}; t=$LICENSE{temp}; " .
         "s=$LICENSE{soon}; x=$LICENSE{exp})\n";
   print join "\n", @OUTPUT;
   
   return;
}


sub apm_fw1_sic {
   my $BLADENAME = $args{n};
   $BLADENAME =~ y/-/_/;
  
   my $SSH = ssh_exec( "rsh -l root $BLADENAME " .
                       "'source /etc/profile.d/CP.sh; cp_conf sic state'" );

   my ($trust) = $SSH =~ m/Trust State: (.*)$/ms;
   $trust =~ s/\n//g;

   if (defined $trust && $trust eq 'Trust established') {
      print "OK - $trust";
   } 
   else {
      print "CRITICAL - SIC Error: $trust";
      exit 2;
   }

   return;
}


sub usage {
   print STDERR '
   The following options are available:

';
   foreach my $key (keys %valid_args) {
      print STDERR "      -$key  --  $valid_args{$key}{desc}\n";
      if (exists $valid_args{$key}{sub}) {
         foreach my $subkey (sort keys %{$valid_args{$key}{sub}}) {
            print STDERR "              - $subkey\n";
         }
      }
   }
   
   print STDERR "\n";
   exit 255;
}

