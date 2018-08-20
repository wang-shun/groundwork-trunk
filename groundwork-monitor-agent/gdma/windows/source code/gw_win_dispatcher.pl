#!perl -w
#
#	Copyright 2003-2009 Groundwork Open Source, Inc.
#	http://www.groundworkopensource.com
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#	License for the specific language governing permissions and limitations under
#	the License.
#
#This script will dispatch the GW monitor program for all hosts
#Options: 
#-c <CONFIG FILE>	Nagios config file containing contact group definitions. 
#                This program will run if this is not set, however the notification contact reports 
#                will not work properly. The default is \"<NAGIOS_ETC>/contactgroups.cfg\".
#-d              Debug mode. Will log additional messages to the log file. 
#-h or -help     Displays help message.
#-v 		 display version 

# NOTES on building with perlapp 4/15/2009 ----- DSN
# First make sure you have at least Activestate Perl 5.10.0.1004
# Do NOT install Crypt-SSLeay.  The Crypt-SSLeay from Winnipeg is NOT compatible with .1004 and will fail when using https.
# Also, do NOT install LWP::UserAgent module , this comes with .1004 also.
# You'll need to install the following perl modules :
# 	1. Win32-Process-Info
# Build using PDK 8.0.1 : perlapp --norunlib --nocompress --exe gw_win_dispatcher.exe gw_win_dispatcher.pl
#
# You will probably see something like this when you build it with perlapp :
#   Proc\ProcessTable.pm:
#       error: Can't locate Proc\ProcessTable.pm
#       refby: C:\Perl\site\lib\Win32\Process\Info.pm line 259
#       refby: C:\Perl\site\lib\Win32\Process\Info\PT.pm line 66
# I don't believe this is in issue however. Read the manual on Process\Info and esp'y the part on variants. 
# Now read the BUGS sections of that man page. 
# By default Win32::Process::Info uses the variant methods in this order : WMI, NT, PT (process table).  
# The code does this : 	my $allpids = Win32::Process::Info->new(); ie is therefore using WMI. So don't go fork()'ing - see BUGS
# Might be safer to do a Win32::Process::Info->new(undef, "NT")
# Perlapp doesn't know what the variant is set to at build time so tries to compile to allow PT method, but doesnt find the lib for it
# since it doesn't exist on windows.
# DSN

# Changes
# DSN 2008 HTTP and HTTPS pulls of config added amongst various other changes
# DSN 4/09 Hostname conversion option added

# TODO: consider case of where primary is down - do we want to try getting the cfgs from the standby ? will run externals need to happen etc ?
#       For now it pulls just from primary.

#use lib "c:\\groundwork\\winagent";
use strict;
use Time::Local;
use Getopt::Std;
use Win32::Process;  
use Win32::Process::Info;

use LWP::UserAgent; # --- DSN
use Sys::Hostname;  # --- DSN
use Fcntl; # --- DSN

my $debug = 0;
my $start_program_time = time;
my $Logfile;
my $Configfile;
my %opt = ();
my %return_codes = ("OK" => "0","WARNING" => "1","CRITICAL" => "2","UNKNOWN" => "3" );
my %exit_codes = ("0" => "0","256" => "1","512" => "2","768" => "3" , "-1"=>"512");
my $version = "2.0.20090420";

my $helpstring = "
This program will dispatch the monitoring agent program (eg gw_win_monitor.exe) for this host. 

Options: 
-c <config file>	Config file containing monitoring parameters. 
-d 			Debug mode. Will log additional messages to the log file. 
-h  			Displays help message.
-v 			Displays version of this program

Copyright 2003-2008 Groundwork Open Source, Inc. 
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software distributed under the License 
is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
or implied. See the License for the specific language governing permissions and limitations under
the License.
";
my $gdma_useragent;  # --- DSN




getopts("dhvc:",\%opt);
if ($opt{h} or $opt{help}) { print $helpstring; exit; }
if ($opt{v} or $opt{version}) { print "Version $version\n"; exit; } # --- DSN

my $program_timeout = 60 * 4;	# Program timeout in seconds - Default 4 minutes
my $host_timeout = 30;			# Host timeout in seconds - Default 30 seconds
my $retry_interval = 2;
my $max_concurrent_hosts = 50;
my $cfgurl_timeout = 5;  # default timeout to use when getting the cfg's through https --- DSN
my $cfg_pullcycle = 5;  # default pull cycle - eg 3 means pull cfg every 3 iterations of this dispatcher prog --- DSN

if ($opt{d}) { 	$debug = 1;  } 
if ($opt{c}) { 	$Configfile = $opt{c}; } 
	else { 	$Configfile = "gw_win_dispatcher.cfg"; }
print "Using configuration file $Configfile\n";
#	Configuration file and create reference for $config
my $config = read_config($Configfile);
#	print config values
if ($debug) {
	foreach my $param (sort keys %{$config}) {
		if (ref($config->{$param}) eq "ARRAY") {
			foreach (my $i=0; $i<=$#{$config->{$param}} ;$i++) {
				if (defined(%{$config->{$param}->[$i]})) {
					foreach my $option (keys %{$config->{$param}->[$i]}) {
						print $param."[$i]_$option = ".$config->{$param}->[$i]->{$option}."\n";
					}
				}
			}
		} else {
			print $param." = ".$config->{$param}."\n";
		}
	}
	print "\n";
}

if (!$config->{Output_Logfile}) {
	$config->{Output_Logfile} = $config->{Output_Logdir}."gw_win_dispatcher.log";
}

open(LOG,">$config->{Output_Logfile}") or die "Can't open log file $config->{Output_Logfile}.\n";		#	Open logfile
print "Using $config->{Output_Logfile} as a log file\n" if ($debug);
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];
print LOG "GroundWork gw_win_dispatcher version $version starting at $thisday, $month $mday, $year. $timestring.\n";    
print LOG "Debug set to $debug\n";
print LOG "Using configuration file $Configfile\n";

########################################################################
#	Read configuration directory and build host query list
########################################################################
if (!defined($config->{GW_Monitor_Program})) {
	print LOG "No GW Monitoring program defined. Exiting.\n";
	die "No GW Monitoring program defined. Exiting.\n";
}
if (!stat($config->{GW_Monitor_Program})) {
	print LOG "GW Monitoring program $config->{GW_Monitor_Program} not found. Exiting.\n";
	die "GW Monitoring program $config->{GW_Monitor_Program} not found. Exiting.\n";
}
if (!defined($config->{HostConfigFile_Directory})) {
	print LOG "No host configuration directory defined. Exiting.\n";
	die  "No host configuration directory defined. Exiting.\n";
}
if (defined($config->{Program_Timeout}) and ($config->{Program_Timeout}=~/(\d+)/)) {
	$program_timeout = $1;
	print LOG "Using program timeout $program_timeout seconds.\n";
} else {
	print LOG "Using default program timeout value $program_timeout seconds.\n";
}

if (defined($config->{Host_Timeout}) and ($config->{Host_Timeout}=~/(\d+)/)) {
	$host_timeout = $1;
	print LOG "Using host timeout $host_timeout seconds.\n";
} else {
	print LOG "Using default host timeout value $host_timeout seconds.\n";
}
if (defined($config->{Retry_Interval}) and ($config->{Retry_Interval}=~/(\d+)/)) {
	$retry_interval = $1;
	print LOG "Using retry interval $retry_interval seconds.\n";
} else {
	print LOG "Using default retry interval value $retry_interval seconds.\n";
}
if (defined($config->{Max_Concurrent_Hosts}) and ($config->{Max_Concurrent_Hosts}=~/(\d+)/)) {
	$max_concurrent_hosts = $1;
	print LOG "Using max concurrent hosts $max_concurrent_hosts.\n";
} else {
	print LOG "Using default max concurrent hosts value $max_concurrent_hosts.\n";
}

# --- DSN
if (defined($config->{ConfigFile_Use_HTTPS})) {
	print LOG "GDMA configuration files will be pulled using HTTPS.\n";
	print "GDMA configuration files will be pulled using HTTPS.\n" if $debug;

        if (defined($config->{ConfigFile_URL_Timeout}) and ($config->{ConfigFile_URL_Timeout}=~/^\d+$/) ) 
        {
           $cfgurl_timeout = $config->{ConfigFile_URL_Timeout} ;
           print LOG "Using config file URL timeout $cfgurl_timeout seconds.\n";
        } 
        else 
        {
           print LOG "Using default config file URL timeout value $cfgurl_timeout seconds.\n";
        }

        if (!defined($config->{ConfigFile_URL})) 
        {
           print LOG "No GW GDMA configuration file URL defined. Exiting.\n";
           die "No GW GDMA configuration file URL defined. Exiting.\n";
        }

        if (!defined($config->{ConfigFile_Pull_CounterFile})) 
        {
           print LOG "No GW GDMA configuration file pull counter file. Exiting.\n";
           die "No GW GDMA configuration file pull counter file defined. Exiting.\n";
        }

        if (defined($config->{ConfigFile_Pull_Cycle}) and ($config->{ConfigFile_Pull_Cycle}=~/^\d+$/) ) 
        {
           $cfg_pullcycle = $config->{ConfigFile_Pull_Cycle} ;
           print LOG "GDMA cfg file will be pulled every $cfg_pullcycle iterations of this dispatcher.\n";
        } 
        else 
        {
           print LOG "GDMA cfg file will be pulled using the default of every $cfg_pullcycle iterations of this dispatcher.\n";
        }

}
else
{
	print LOG "GDMA configuration files will NOT automatically be pulled using HTTPS - use scp or something else instead.\n";
	print "GDMA configuration files will NOT automatically be pulled using HTTPS - use scp or something else instead.\n" if $debug;
}

# ----------- hostname case conversion --------------
my $hostnamecfg;
if (defined($config->{Hostname_Case})) 
{
        if ($config->{Hostname_Case} =~ /^\s*lower\s*$/i  ) 
        {
           $hostnamecfg = lc Sys::Hostname::hostname(); 
	   print LOG "Hostname case conversion : lower case (host name was set to '$hostnamecfg')\n";
	   print "Hostname case conversion : lower case (host name was set to '$hostnamecfg')\n" if $debug;
        }
        elsif ($config->{Hostname_Case} =~ /^\s*upper\s*$/i  ) 
        {
           $hostnamecfg = uc Sys::Hostname::hostname(); 
	   print LOG "Hostname case conversion : UPPER case (host name was set to '$hostnamecfg')\n";
	   print "Hostname case conversion : UPPER case (host name was set to '$hostnamecfg')\n" if $debug;
        }
        else 
        {
           $hostnamecfg = Sys::Hostname::hostname(); 
	   print LOG "Hostname case conversion : Hostname_Case should be 'upper' or 'lower' - got '$config->{Hostname_Case}' - case will not be converted (host name is '$hostnamecfg')\n";
	   print "Hostname case conversion : Hostname_Case should be 'upper' or 'lower' - got '$config->{Hostname_Case}' - case will not be converted (host name is '$hostnamecfg')\n" if $debug;
        }
}
else
{
        $hostnamecfg = Sys::Hostname::hostname(); # leave as-is by default
	print LOG "No hostname case conversion was specified. Host name case be left unchanged (host name is '$hostnamecfg').\n";
	print "No hostname case conversion was specified. Host name case be left unchanged (host name is '$hostnamecfg').\n" if $debug;

}
$hostnamecfg = "gwmon_${hostnamecfg}.cfg";
# ----------- end hostname case conversion --------------



if (defined($config->{ConfigFile_Use_HTTPS})) 
{
   if ( gdma_pull_config( $config->{ConfigFile_Pull_CounterFile}, $cfg_pullcycle ) == 1 )
   {
      get_gdma_cfg_file_using_http("$config->{ConfigFile_URL}/$hostnamecfg", "$config->{HostConfigFile_Directory}/$hostnamecfg", $cfgurl_timeout ) ;
   }
}



my $host_ref = undef;
opendir DIR,$config->{HostConfigFile_Directory} or die "Can't open directory $config->{HostConfigFile_Directory}\n";
while (defined(my $file = readdir(DIR))) {
	# do something with "$dirname/$file"
	#if ($file =~ /gwmon_(.*?)\.cfg/) {
        # -----------  For gdma, only want to parse this hosts' cfg, not all .cfg's (thats windows child)  ---- DSN
	if ($file =~ /^$hostnamecfg$/) {  
		#$host_ref->{$1}->{CONFIG_FILE} = $file;
		$host_ref->{$hostnamecfg}->{CONFIG_FILE} = $file; # ---- DSN
	}
}
closedir(DIR);

########################################################################
#	Dispatch a gwmonitor program for each host
########################################################################
my $concurrent_host_count =  0;
my $host_failure_count =  0;
#
#	Start all hosts
#
foreach my $host (keys %{$host_ref}) {
	if (defined($host_ref->{$host}->{CONFIG_FILE})) {		# if a config file
		my $ProcessObj = undef;
		print LOG "Command: $config->{GW_Monitor_Program} -c $config->{HostConfigFile_Directory}/$host_ref->{$host}->{CONFIG_FILE}\n";
		if (Win32::Process::Create($ProcessObj, "$config->{GW_Monitor_Program}",
				" -c $config->{HostConfigFile_Directory}/$host_ref->{$host}->{CONFIG_FILE}",
				0,
				NORMAL_PRIORITY_CLASS,
				".")
			) {
			$host_ref->{$host}->{STATUS} = "STARTED";
			$host_ref->{$host}->{ProcessID} = $ProcessObj->GetProcessID();
			$host_ref->{$host}->{TimeStarted} = time;
			print LOG "Host $host Win32 Process started at program elapsed time ".(time-$start_program_time)." secs, ID=$host_ref->{$host}->{ProcessID}\n";
			$concurrent_host_count++;
		} else {
			print LOG "Host $host Win32 Process Create error: $! \n";
			print "Host $host Win32 Process Create error: $! \n";
			$host_ref->{$host}->{STATUS} = "Error: $!";
			$host_failure_count++;
		}
	} else {
		print LOG "Host $host undefined configuration file.\n";
		$host_ref->{$1}->{STATUS} = "Error: $!";
		$host_failure_count++;
	}
	while (($concurrent_host_count >= $max_concurrent_hosts) and 
			((time - $start_program_time) < $program_timeout))  {
		sleep $retry_interval;
		$concurrent_host_count = CheckProcs($host_ref,$concurrent_host_count);	
	}
}
print LOG "All hosts initiated at program elapsed time ".(time-$start_program_time)." secs.\n";
# Wait for hosts to complete or timeout reached
while (($concurrent_host_count > 0) and 
		((time - $start_program_time) < $program_timeout))  {
	sleep $retry_interval;
	$concurrent_host_count = CheckProcs($host_ref,$concurrent_host_count);	
}
if ((time - $start_program_time) >= $program_timeout) {
	print LOG "Max timeout exceeded at program elapsed time ".(time-$start_program_time)." secs.\n";
	print LOG "Hosts still processing= $concurrent_host_count. End processes check starting\n";
} else {
	print LOG "All host processing completed at program elapsed time ".(time-$start_program_time)." secs.\n";
}
$concurrent_host_count = EndProcs($host_ref,$concurrent_host_count);
########################################################################
#	Finished executing system checks. 
########################################################################
# Computed completed hosts 
my $completed_count = 0;
my $total_host_time = 0;
my $killed_count = 0;

foreach my $host (keys %{$host_ref}) {
	if ($host_ref->{$host}->{STATUS} eq "COMPLETED") { 
		$completed_count++	;
		$total_host_time += $host_ref->{$host}->{ELAPSED_TIME};
	}
	if ($host_ref->{$host}->{STATUS} eq "KILLED") { $killed_count++	}
}
my $program_end_time = time - $start_program_time;
print LOG "Hosts completed processing = $completed_count.\n";
print LOG "Host processes killed = $killed_count.\n";
print LOG "Total processing time $program_end_time seconds.\n";
print LOG "Avg. host processing time ".($total_host_time/$completed_count)." seconds.\n" if ($completed_count > 0);


print "Hosts not completed processing = $concurrent_host_count.\n";
print "Total processing time $program_end_time seconds.\n";
exit;
########################################################################
#	Wait loop until all processes finished or max timeout
########################################################################

sub EndProcs {
	my $host_ref = shift;
	my $concurrent_host_count = shift;
	my $allpids = Win32::Process::Info->new();
	#my $allpids = Info->new();
	my %listpids = ();
	foreach my $pid ($allpids->ListPids()) {
		$listpids{$pid} = 1;
	}
	foreach my $host (keys %{$host_ref}) {
		if ($host_ref->{$host}->{STATUS} =~ /(STARTED|RUNNING)/) {
			if ($listpids{$host_ref->{$host}->{ProcessID}}) {
				print LOG "Killing process for host $host. PID=$host_ref->{$host}->{ProcessID}. Exceeded timeout $program_timeout\n";
				# get subprocs 
				my %subs = $allpids->Subprocesses(($host_ref->{$host}->{ProcessID}));
				Win32::Process::KillProcess($host_ref->{$host}->{ProcessID}, 0);
				foreach my $sub (keys %subs) {
					print LOG "Proc $host_ref->{$host}->{ProcessID} has subprocess $sub.  Killing.\n" if ($debug);
					Win32::Process::KillProcess($sub, 0);
				}
				#$host_ref->{$host}->{ProcessObj}->Kill(0);
				$host_ref->{$host}->{STATUS} = "KILLED";
				#$concurrent_host_count--;
			} else {
				my $host_elapsed_time = time - $host_ref->{$host}->{TimeStarted};
				print LOG "Process for host $host completed in $host_elapsed_time secs.\n";
				$host_ref->{$host}->{ELAPSED_TIME} = $host_elapsed_time;
				$host_ref->{$host}->{STATUS} = "COMPLETED";
				$concurrent_host_count--;
			}
		}
	}
	return $concurrent_host_count;
}
########################################################################
sub CheckProcs {
	my $host_ref = shift;
	my $concurrent_host_count = shift;
	my $allpids = Win32::Process::Info->new();
	#my $allpids = Info->new();
	my %listpids = ();
	foreach my $pid ($allpids->ListPids()) {
		$listpids{$pid} = 1;
	}
	foreach my $host (keys %{$host_ref}) {
		if ($host_ref->{$host}->{STATUS} =~ /(STARTED|RUNNING)/) {
			my $host_elapsed_time = time - $host_ref->{$host}->{TimeStarted};
			if ($listpids{$host_ref->{$host}->{ProcessID}}) {
				print LOG "Checking process for host $host. PID=$host_ref->{$host}->{ProcessID}. Host elapsed time $host_elapsed_time secs.\n";
				if ($host_elapsed_time >= $host_timeout) {
					my %subs = $allpids->Subprocesses(($host_ref->{$host}->{ProcessID}));
					print LOG "\tKilling process for host $host. PID=$host_ref->{$host}->{ProcessID}. Host elapsed time $host_elapsed_time secs.\n";
					Win32::Process::KillProcess($host_ref->{$host}->{ProcessID}, 0);
					foreach my $sub (keys %subs) {
						print LOG "Proc $host_ref->{$host}->{ProcessID} has subprocess $sub.  Killing.\n" if ($debug);
						Win32::Process::KillProcess($sub, 0);
					}
					$host_ref->{$host}->{STATUS} = "KILLED";
					$concurrent_host_count--;
				} else {
					$host_ref->{$host}->{STATUS} = "RUNNING";
				}
			} else {
				print LOG "Process for host $host completed in $host_elapsed_time secs.\n";
				$host_ref->{$host}->{ELAPSED_TIME} = $host_elapsed_time;
				$host_ref->{$host}->{STATUS} = "COMPLETED";
				$concurrent_host_count--;
			}
		}
	}
	return $concurrent_host_count;
}
########################################################################
#	Read configuration file
########################################################################
sub read_config {
	my $configfile = shift;
	if (!open(CONFIG,$configfile) ) {
			print "Can't open configuration file $configfile\n";
			return "ERROR";
	}
	while (my $line = <CONFIG>) {
		# Discard comment and invalid lines
		if ($line=~/^\s*#/) {next }						# Comment if line starts with #
		if ($line=~/^\s*(\S+)\s*=\s*"(.*?)"/) {
			my $parameter = $1; my $value = $2;
			# Sample line: 
			#		Check_Disk[1]_Parm_--warning = "10%"
			if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
				$config->{$1}->[$2]->{$3} = $value;
			} elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
				$config->{$1}->[$2] = $value;
			} else {
				$config->{$parameter} = $value;
			}
		} elsif ($line=~/^\s*(\S+)\s*=\s*(\S+)/) { 	
		#
		#  Set to another parameter that has already been defined. No quotes after = 
		#	Monitor_Server[1]= "172.17.31.13"
		# 	Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1]
		#	Also support multiples, ie:
		# 	Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1],Monitor_Server[2]
		#
			my $parameter = $1; my $target = $2;  
			my @targetparameters = split /,/,$target;
			print "target = $2\n" if $debug;
			foreach my $targetparameter (@targetparameters) {
				my $value = "";
				print "Checking targetparameter=$targetparameter\n" if $debug;
				if ($targetparameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
					$value = $config->{$1}->[$2]->{$3};
				} elsif ($targetparameter =~ /^(.*?)\[(\d+)\]$/) {
					$value = $config->{$1}->[$2];
				} else {
					$value = $config->{$targetparameter};
				}
				print "value = $value\n" if $debug;
				if ($value) {
					if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
						$config->{$1}->[$2]->{$3} .= $value.",";
					} elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
						$config->{$1}->[$2] .= $value.",";
					} else {
						$config->{$parameter} .= $value.",";
					}
					print "setting parameter $parameter \n" if $debug;
				}
			}
			if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {	# get rid of trailing comma
				$config->{$1}->[$2]->{$3} =~ s/,$//;
			} elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
				$config->{$1}->[$2] =~ s/,$//; 
			} else {
				$config->{$parameter} =~ s/,$//; 
			}
		} elsif ($line=~/^\s*(\S+)\s*#?/) {
			my $parameter = $1; 
			# Sample line: 
			#		Check_Disk[1]_Parm_--errors-only 
			if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
				$config->{$1}->[$2]->{$3} = "";
			} elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
				$config->{$1}->[$2] = "";
			} else {
				$config->{$parameter} = "";
			}
		}
	}
	close CONFIG;
	return \%{$config} ;
}

# ----------------------------------------------------------------------------------------------------------  DSN
sub get_gdma_cfg_file_using_http
{
   # takes the following args :
   # - a url to the cfg file location
   # - the fully qualified name of where to save the cfg file
   # - timeout in seconds for the url
   # Globals - expects a global declaration for the browser useragent var "$gdma_useragent"

   my ( $cfgurl, $outfile, $urltimeout ) = @_;
   my ( $response ) ;

   $gdma_useragent = LWP::UserAgent->new(  );
   $gdma_useragent->timeout($urltimeout);  
   $gdma_useragent->requests_redirectable   (); # default is GET and HEAD redirect - thats fine - maybe param'ize this opt later if necessary

   print "Attempting to get $cfgurl to $outfile (timeout is set to " . $gdma_useragent->timeout()   . ")\n" if $debug;
   
   $response = $gdma_useragent->mirror($cfgurl, $outfile);
   if (not $response->is_success) 
   { 

       print LOG "Failed to get $cfgurl - ", $response->status_line;
       if ( $response->status_line =~ /304 not modified/i ) { print LOG " (304's are generally safe to ignore) \n"; } else { print LOG "\n" ; }
       if ( $debug )
       {
           print "Failed to get $cfgurl - " . $response->status_line ;
           if ( $response->status_line =~ /304 not modified/i ) { print " (304's are generally safe to ignore) \n"; } else { print "\n" ; }
       }
       #exit 2;  # don't exit - just return.
       return;
   }

   if ( $debug ) 
   {
      print "-" x 40 , "\nRetrieved content for $cfgurl : \n", $response->content, "\n" ;
   }

}

# ----------------------------------------------------------------------------------------------------------  DSN
sub gdma_pull_config
{
	# determines whether its time to pull the config down or not - returns 1 or 0 resp'y
	# takes the following args :
	# - the Nth poll number - eg if this is 3, then the cfg will be pulled every 3rd run of this dispatcher prog
	# - location of the counter file

	my ( $counterfile, $cycle ) = @_;
	my ( $pullnow, $filehandle, $counter );

	if ( not sysopen(FH, "$counterfile", O_RDWR|O_CREAT) )
	{ 
           print LOG "Can't open $counterfile for update : $!\n"; 
           print "Can't open $counterfile for update : $!" if $debug;
           return 0;
        }

	$counter = <FH> || 0;  
        chomp $counter;

        if ( not seek(FH, 0, 0) ) 
	{ 
           print LOG "Can't rewind $counterfile : $!\n"; 
           print "Can't rewind $counterfile : $!" if $debug;
           return 0;
        }
      
        if ( not truncate(FH, 0) )
	{ 
           print LOG "Can't truncate $counterfile : $!\n"; 
           print "Can't truncate $counterfile : $!" if $debug;
           return 0;
        }
      

	if ( not (print FH $counter+1, "\n")  )	
	{ 
           print LOG "Can't write $counterfile : $!\n"; 
           print "Can't write $counterfile : $!" if $debug;
           return 0;
        }

	if ( not close(FH) )
	{ 
           print LOG "Can't close $counterfile : $!\n"; 
           print "Can't close $counterfile : $!" if $debug;
           return 0;
        }

        if (  ($counter % $cycle) == 0 )  
        {
            print LOG "Pull cfg file\n";
            print "Pull cfg file\n" if $debug;
            return 1;  # go pull
        }
        else
        {
           print LOG "Do not pull cfg file (counter is $counter, cycle is $cycle)\n";
           print "Do not pull cfg file (counter is $counter, cycle is $cycle)\n" if $debug;
           return 0;
        }

}
   


__END__

