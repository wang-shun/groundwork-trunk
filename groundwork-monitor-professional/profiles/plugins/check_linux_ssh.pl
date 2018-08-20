#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use host_func ();
use nagios_func ();
use parse_func ();
use shared_func ();
use ssh_func ();

use constant PLUGIN => 'check_linux_ssh';

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'loadavg'      => \&load_average,
                            'mem'          => \&memory,
                            'proc'         => \&process_check,
                            'proc_mem'     => \&process_check_memory,
                            'procs'        => \&processes_count,
                            'uptime'       => \&uptime,
                          };

use constant OPTIONS => { 'h'  => 'hostname',
                          'k?' => 'ssh private key',
                          'l?' => 'levels [warning:critical]',
                          'p?' => 'process name',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my @sshopts = do {
   if ($args->{k} && ref $args->{k}) {
      ( sshkeys => $args->{k} );
   }
   elsif ($args->{k} && $args->{k} ne 'SSHKEY') {
      ( sshkeys => [ $args->{k} ] );
   }
   else {
      (password => $host->decrypt( 'backup_pass' ));
   }
};
my $ssh  = ssh_func->new( hostname => $args->{h},
                          username => $host->get( 'backup_user' ),
                          @sshopts,
                        ) or do {
   print "ERROR - $@";
   exit 3;
};
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $ssh->die3("Unknown check type: $args->{t}");
exit 0;


################################################################################
# cpu - check smp cpu utilization in percentage                                #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();
   my @order = qw(user nice system idle iowait irq softirq);
   my $cpu = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve first round of cpu ticks from /proc/stat
   my $one = $ssh->cmd( '/bin/cat /proc/stat' ) or do {
      print "CRITICAL - Unable to retrieve /proc/stat (1)";
      exit 2;
   };

   # parse output and store in hash array
   while ($one =~ /^cpu([0-9]+)[ ](.*?)$/msg) {
      my $core = $1;
      my @fields = split /[ ]+/ => $2;
      @{ $cpu->{ 1 }->{ $core } }{ @order } = @fields; 
   }

   # sleep for 2 seconds
   sleep 2;
     
   # retrieve second round of cpu ticks from /proc/stat
   my $two = $ssh->cmd( '/bin/cat /proc/stat' ) or do {
      print "CRITICAL - Unable to retrieve /proc/stat (2)";
      exit 2;
   };

   # parse output and store in hash array
   while ($two =~ /^cpu([0-9]+)[ ](.*?)$/msg) {
      my $core = $1;
      my @fields = split /[ ]+/ => $2;
      @{ $cpu->{ 2 }->{ $core } }{ @order } = @fields; 
   }

   # compare number of cpus from each parse event
   if (keys %{ $cpu->{1} } != keys %{ $cpu->{2} }) {
      print "CRITICAL - Error parsing /proc/stat";
      exit 2;
   }

   # calculate timeticks
   foreach my $core (keys %{ $cpu->{1} }) {
      foreach my $type (keys %{ $cpu->{1}->{$core} }) {
         defined $cpu->{1}->{$core}->{$type} or next;
         defined $cpu->{2}->{$core}->{$type} or next;
         my $diff = sprintf "%d", $cpu->{2}->{$core}->{$type} - 
                                  $cpu->{1}->{$core}->{$type};
         $cpu->{total}->{$core}->{ticks} += $diff;
         $cpu->{total}->{$core}->{$type} = $diff;
      }
   }

   # calculate percentages
   foreach my $core (keys %{ $cpu->{total} }) {
      foreach my $type (keys %{ $cpu->{total}->{$core} }) {
         $type eq 'ticks' and next;
         my $ticks = $cpu->{total}->{$core}->{ticks};
         my $value = $cpu->{total}->{$core}->{$type};
         $cpu->{percent}->{$core}->{$type} = sprintf "%d", 100 * $value / $ticks;
      }
   }

   # test against thresholds
   foreach my $cpucore (sort { $a <=> $b } keys %{ $cpu->{percent} }) {
      my $core    = $cpu->{percent}->{$cpucore};
      my $percent = sprintf "%d", 100 - $core->{idle};
      push @perfdata, "cpu$cpucore=$percent";
      my @data = map { defined $core->{$_} ? "$_=$core->{$_}" : () } @order;
      if ($percent >= $crit) {
         push @output, "CRITICAL - CPU $cpucore utilization at $percent% " .
                       "(threshold $crit%) [@data]";
      }
      elsif ($percent >= $warn) {
         push @output, "WARNING - CPU $cpucore utilization at $percent% " .
                       "(threshold $warn%) [@data]";
      }
      else {
         push @output, "OK - CPU $cpucore utilization at $percent% " .
                       "(threshold $warn%) [@data]";
      }
   }

   # generate output  
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) { 
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      if ((my $ok = @sorted) == 1) {
         print shift(@sorted), "|@perfdata";
      }
      else {
         print "OK - $ok cpus healthy [@perfdata]|@perfdata\n";
         print join "\n" => @sorted;
      }
   }
}


################################################################################
# disk - check disk partition utilization                                      #
################################################################################
sub disk {
   # instantiate variables
   my @output = my @perfdata = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve partition status via df
   my $df = $ssh->cmd( '/bin/df -k -P' ) or do {
      print "CRITICAL - Unable to retreive 'df' output";
      exit 2;
   };

   # parse df output
   foreach my $line (split /\n/ => $df) {
      my ($fs, $total, $used, $avail, $cap, $mount) = split /[ ]+/ => $line;
      $cap =~ /%/ or next;   # skip header line
      $fs eq 'devfs'  and next;   # skip /dev
      $fs eq 'procfs' and next;   # skip /proc
      $fs eq 'udev'   and next;   # skip /dev (udev)
      chop $cap;                  # remove % symbol
      if ($cap >= $crit) {
         push @output, "CRITICAL - Partition $mount at $cap% utilization " .
                       "(threshold $crit%)";
      }
      elsif ($cap >= $warn) {
         push @output, "WARNING - Partition $mount at $cap% utilization " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - Partition $mount at $cap% utilization";
      }
      $mount = substr( $mount, 1 ) || 'root';
      $mount =~ tr|/|_|;
      push @perfdata, "$mount=$cap"; 
   } 

   # generate output
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) { 
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = @sorted;
      print "OK - $ok partitions healthy [@perfdata]|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# interface - check interface problems and statistics                          #
################################################################################
sub interface {
   # instantiate variables
   my @order = qw(int_name int_in_oct int_in_pkt int_in_err int_in_drp
                  int_in_fifo int_in_frame int_in_comp int_in_mcast
                  int_out_oct int_out_pkt int_out_err int_out_drp int_out_fifo
                  int_out_coll int_out_carr int_out_comp);
   my @int_list = ();

   # instantiate nagios object for submitting passive check results
   my $nagios = nagios_func->new( $args );

   # retrieve /proc/net/dev 
   my $dev = $ssh->cmd( '/bin/cat /proc/net/dev' ) or do {
      print "CRITICAL - Unable to retrieve /proc/net/dev";
      exit 2;
   };

   # parse /proc/net/dev
   foreach my $line (split /\n/ => $dev) {
      $line =~ /:/ or next;                       # skip header lines
      my $int = {};                               # instantiate $int hash
      $line =~ s/\G //g;                          # remove beginning spaces
      @{$int}{@order} = split /[ :]+/ => $line;   # populate $int hash
      $int->{int_name} eq 'lo' and next;
      next unless ( ($int->{int_in_oct} && $int->{int_in_oct} ne 'U') || 
                    ($int->{int_out_oct} && $int->{int_out_oct} ne 'U') );
      push @int_list, $int->{int_name};
      $nagios->interface_stats_passive( $int );
      $nagios->interface_problems_passive( $int );
   }
   print "OK - Interfaces with traffic counters: @int_list";
}


################################################################################
# load - check system load average                                             #
################################################################################
sub load_average {
   # default multiplier is 2 unless otherwise specified
   my $multiplier = ($args->{l} && $args->{l} =~ /^[0-9.]+$/) ? $args->{l} : 2;

   # retreive cpu count (/proc/cpuinfo)
   my $cpuinfo = $ssh->cmd( '/bin/cat /proc/cpuinfo' ) or do {
      print "CRITICAL - Unable to retrieve /proc/cpuinfo";
      exit 2;
   };
   my $procs = () = $cpuinfo =~ /^processor/msg;

   # retrieve load averages (/proc/loadavg)
   my $loadavg = $ssh->cmd( '/bin/cat /proc/loadavg' ) or do {
      print "CRITICAL - Unable to retrieve /proc/loadavg";
      exit 2;
   };

   # the threshold is procs * multiplier
   my $threshold = sprintf("%.1f", $procs * $multiplier);

   # parse load averages
   my ($one, $five, $fifteen) = $loadavg =~ /^([0-9.]+) ([0-9.]+) ([0-9.]+)/;
   my @perfdata = ( "1min=$one", "5min=$five", "15min=$fifteen" );

   # test against thresholds and generate output
   if ($fifteen >= $threshold) {
      print "WARNING - High load average $one $five $fifteen " .
            "(threshold $threshold)|@perfdata";
   }
   else {
      print "OK - Load average $one $five $fifteen|@perfdata";
   }
}


################################################################################
# memory - check memory utilization                                            #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve /proc/meminfo
   my $meminfo = $ssh->cmd( '/bin/cat /proc/meminfo' ) or do {
      print "CRITICAL - Unable to retrieve /proc/meminfo";
      exit 2;
   };

   # parse /proc/meminfo
   my $mem = do {
      $_ = {};
      while ($meminfo =~ /^([a-zA-Z0-9_]+):[ ]+([0-9]+)/msg) {
         $_->{$1} = $2;
      }
      $_;
   };

   my $vmtotal   = sprintf "%d", $mem->{MemTotal} + $mem->{SwapTotal};
   my $realused  = sprintf "%d", $mem->{MemTotal} - $mem->{MemFree} -
                                 $mem->{Buffers}  - $mem->{Cached};
   my $swapused  = sprintf "%d", $mem->{SwapTotal} - $mem->{SwapFree};
   my $vmpercent = sprintf "%d", 100 * ($realused + $swapused) / $vmtotal;

   if ($vmpercent >= $crit) {
      print "CRITICAL - Memory utilization at $vmpercent% (threshold $crit%)" .
            "|percent=$vmpercent";
      exit 2;
   }
   elsif ($vmpercent >= $warn) {
      print "WARNING - Memory utilization at $vmpercent% (threshold $warn%)" .
            "|percent=$vmpercent";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $vmpercent%|percent=$vmpercent";
   }
}


################################################################################
# process_check - check number of running named processes                      #
################################################################################
sub process_check {
   # verify 'p' argument passed
   if (!$args->{p}) {
      print "ERROR - Missing argument 'p' for process name";
      exit 3;
   }

   # set warning/critical thresholds
   my ($low, $high) = grep /^\d+$/ => split /:/ => $args->{l};
   $low  ||= 0;       # default 0
   $high ||= 32000;   # default 32000

   # get pids for process name
   my $ps = $ssh->cmd( "/bin/ps -C $args->{p} -o pid" );
   $ps ||= '';   # set to nothing if not defined

   # count number of processes returned
   my $procs = () = $ps =~ /^[ ]*[0-9]+$/msg;

   # test against thresholds and generate output
   if ($procs < $low) {
      print "CRITICAL - Found $procs '$args->{p}' processes running " .
            "(threshold <$low)|count=$procs";
      exit 2;
   }
   elsif ($procs > $high) {
      print "CRITICAL - Found $procs '$args->{p}' processes running " .
            "(threshold >$high)|count=$procs";
      exit 2;
   }
   else {
      print "OK - Found $procs '$args->{p}' processes running|count=$procs";
   }
}


################################################################################
# process_check_memory - check memory utilization of named processes           #
################################################################################
sub process_check_memory {
   # instantiate variables
   my $pid  = {};
   my $ppid = {};
   my $vsz = ();
   
   # verify 'p' argument passed
   if (!$args->{p}) {
      print "ERROR - Missing argument 'p' for process name";
      exit 3;
   }

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 2000;   # default 2.0 GB
   $crit ||= 2500;   # default 2.5 GB

   # get a list of all matching named processes (via ps)
   my $ps = $ssh->cmd( "ps -C $args->{p} -o pid,ppid,vsz" );

   # parse ps output
   while ($ps =~ /^[ ]?([0-9]+)[ ]+([0-9]+)[ ]+([0-9]+)$/msg) {
      $pid->{ $1 } = $3;
      $ppid->{ $2 }++;
   }

   # find pid of master process
   if (scalar keys %$pid == scalar keys %$ppid) {
      ($vsz) = map { $pid->{$_} } keys %$pid;
   }
   else {
      my ($master) = sort { $ppid->{$b} <=> $ppid->{$a} } keys %$ppid;
      $vsz = $pid->{ $master };
   }
 
   # generate output
   if (!$vsz) {
      print "CRITICAL - No processes named '$args->{p}' found";
      exit 2;
   }
   else {
      my $mb = sprintf "%d", $vsz / 1024;
      print "OK - Process '$args->{p}' using $mb MB of memory|mb=$mb";
   }
}


################################################################################
# processes_count - count total processes running on system                    #
################################################################################
sub processes_count {
   # retreive 'ps' output
   my $ps = $ssh->cmd( 'ps -eo pid' ) or do {
      print "CRITICAL- Unable to retrieve 'ps' output";
      exit 2;
   };

   # count number of processes returned
   my $procs = () = $ps =~ /^[ ]*[0-9]+$/msg;

   # generate output
   print "OK - $procs running processes found|count=$procs";
}


################################################################################
# uptime - check system uptime
################################################################################
sub uptime {
   # retrieve /proc/uptime
   my $uptime = $ssh->cmd( '/bin/cat /proc/uptime' ) or do {
      print "CRITICAL - Unable to retrieve /proc/uptime";
      exit 2;
   };
   
   # parse seconds from uptime output
   my ($seconds) = $uptime =~ /^([0-9.]+)/m;

   # convert seconds to days 
   if ( (my $days = $seconds / 60 / 60 / 24) >= 1 ) {
      # if >=1 days then make integer
      $uptime = sprintf "%d", $days;
   }
   else {
      # if <1 days then make float
      $uptime = sprintf "%.1f", $days;
   }  

   # call shared_uptime
   shared_func::shared_uptime( $args, $uptime );
}
