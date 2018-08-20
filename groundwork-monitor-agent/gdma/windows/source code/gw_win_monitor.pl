#!/usr/bin/perl --
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
# Changes
# DSN 4/2009 : added per-service check frequencies; added spooling
#
#This script will load the SQL dashboard database used by the Groundwork Insight Reports.
#Options: 
#-c <CONFIG FILE>	Nagios config file containing contact group definitions. 
#                This program will run if this is not set, however th notification contact reports 
#                will not work properly. The default is \"<NAGIOS_ETC>/contactgroups.cfg\".
#-d              Debug mode. Will log additional messages to the log file. 
#-h or -help     Displays help message.
#-v 		 display version

#use strict;
use Time::Local;
use Getopt::Std;
use IO::Handle;
$|=1;

my $version = "2.0.20090424-No_Timestamps";
my $debug = 1;
my $start_program_time=time;
my $host;
my $Logfile;
my $Configfile;
my %opt = ();
my $default_service_check_interval = 600;

my %return_codes = ("OK" => "0","WARNING" => "1","CRITICAL" => "2","UNKNOWN" => "3" );
my %exit_codes = ("0" => "0","256" => "1","512" => "2","768" => "3" , "-1"=>"512");

my $helpstring = "
Version $version
This script will monitor system statistics on this server.
Options: 
-c <CONFIG FILE>	Config file containing monitoring parameters. 
-l <LOG FILE>	Log file for this script. 
-d              Debug mode. Will log additional messages to the log file. 
-h or -help     Displays help message.
-v              Displays version.

Copyright 2003-2008 Groundwork Open Source, Inc. 
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software distributed under the License 
is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
or implied. See the License for the specific language governing permissions and limitations under
the License.
";

my $local_spool_filename;
my $spooling;

my $hostname = `hostname`;
chomp $hostname;

getopts("vdhc:",\%opt);
if ($opt{h} or $opt{help}) {
	print $helpstring;
	exit;
}

if ($opt{d}) {
	$debug = 1;
} 

if ($opt{v}) {
	print "Version $version\n"; exit;
} 

if ($opt{c}) {
	$Configfile = $opt{c};
	if ($Configfile =~ /gwmon_(.*?).cfg/) {
		$hostname = $1;
	} elsif ($Configfile =~ /(.*?).cfg/) {
		$hostname = $1;
	}
} else {
	die "No configuration file specified.\n";
}
print "Using configuration file $Configfile\n" if ($debug);

#	Configuration file and create reference for $config
my $config = read_config($Configfile);

#	print config values
if ($debug) {
	foreach my $param (keys %{$config}) {
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
}

if (!$config->{Output_Logfile}) 
{
	$config->{Output_Logfile} = $config->{Output_Logdir}."/$hostname.log";
}
if (!$config->{Monitor_Host}) 
{
	if ($config->{Target_Host}) 
        {
		$config->{Monitor_Host} = $config->{Target_Host};
	} 
        else 
        {
		$config->{Monitor_Host} = $hostname;
	}
}
if (!$config->{Target_Host}) 
{ 
   $config->{Target_Host} = $hostname; 
}

$local_spool_filename = $config->{Spool_File};
$config->{Spooling}  ||= $spooling; 
if ( defined $config->{Spooling} ) 
{
    if ( $config->{Spooling} =~ /^on$/i ) { $spooling = 1 ; } else { $spooling = 0 ; }
}
else 
{ 
   $spooling = 0 ; 
}

if (!$config->{Status_File}) { $config->{Status_File} = "c:/groundwork/winagent/gdma.status"; }

# open log file
open(LOG,">$config->{Output_Logfile}") or die "Can't open log file $config->{Output_Logfile}.\n";
print "Using $config->{Output_Logfile} as a log file\n" if ($debug);
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];
print LOG "GroundWork gw_win_monitor version $version starting at $thisday, $month $mday, $year. $timestring.\n";    
#print LOG "GroundWork gw_win_monitor version $version starting at $timestring.\n";    
print LOG "Debug set to $debug\n";
print LOG "Using configuration file $Configfile\n";
print LOG "Spooling is " , ($spooling)? "On" : "Off" , "\n";
print LOG "Spooling file name is '$local_spool_filename'\n";

# Default service check interval 
if ( ! $config->{Default_Service_Check_Interval} ) 
{ 
   print LOG "No Default_Service_Check_Interval defined - services with no check interval defined will use a preset of $default_service_check_interval\n";
   $config->{Default_Service_Check_Interval} = $default_service_check_interval ; 
}
else
{
   print LOG "Services with no check interval defined will use a default check interval of $config->{Default_Service_Check_Interval}\n";
}

if ( $config->{Default_Service_Check_Interval} !~ /^\s*\d+\s*$/ ) 
{
   print LOG "Invalid Default_Service_Check_Interval - should be numeric (found $config->{Default_Service_Check_Interval}) - using preset of $default_service_check_interval\n";
   $config->{Default_Service_Check_Interval} = $default_service_check_interval ; 
}



########################################################################
#	Start executing system checks if defined in configuration
########################################################################
foreach my $checkname (sort keys %{$config}) {
	#print Dumper(%{$config}); next;
	if (defined($config->{$checkname})) {		# if in config file
		if  (($checkname =~ /Check_SAR/) or ($checkname =~ /Check_SAR2/) or ($checkname =~ /Check_SAR3/)) # See if special check 
                {
                   # Execute but don't send_nsca. Handled in script
		   Check_GENERIC_NOSEND($checkname);	
		} 
                else 
                {
                   # Generic, get check result and send to Nagios
	   	   Check_GENERIC($checkname) if ( $checkname =~ /^Check/ ) ; # Added the Check_ condition DSN 1/2009
		}
	}
}

#### check for a spool file and try and empty it ####
flush_spool() if ( $spooling == 1 );

exit;

# =========== per check frequency mods ===========

# ------------------------------------
sub run_check_now
{
   # Sees if its time to run a check by looking in the gdma check status file
   # for the last run time of this check. 
   # Returns 1 for yes go run it now, 0 for don't run it now.
   
   # If the status file does not yet exist, then this is the first time we're running so
   # If the status file is unreadable then thats an error condition - don't run the check
   # If the last configured frequency of this check smaller than the frequency in the cfg, then run it now 
   #    (Note that the incoming check should already have been checked for a defined frequency (aka check interval))
   # If now minus the last run time >= last configured check frequency, then run it now

   my ( $checkname, $gdmastatfile, $current_check_frequency ) = @_;

   my ( $last_run_time, $last_configured_frequency ) ;

   if ( ! -e $gdmastatfile ) 
   {
      # the stat file doesn't exist yet so assuming its the first time any check ran so run the check
      # also accounts for if the stat file was accidentally removed
      print LOG "GDMA status file $gdmastatfile does not exist - assuming first run\n" if $debug;
      return 1;
   }

   if ( ! -r $gdmastatfile ) 
   {
      # the stat file isn't readable then thats an error and don't run the check
      print LOG "Error - GDMA status file $gdmastatfile is not readable - the check will not be run\n" ;
      return 0;
   }
   

   # get the status details from when the check was last run. In the case of the check not having run before,
   # the last run time and last configured frequency will be set to "hasnotrun" by get_last_check_details(),
   # in which case run it now.
   get_last_check_details( $checkname, , $gdmastatfile, \$last_run_time, \$last_configured_frequency ) ;

   # check to see if the check has been run before according to the status file
   if ( $last_run_time eq "hasnotrun" ) 
   {
      print LOG "Check $checkname last run time indicates it has not been run before - will run it now\n";
      return 1;
   }

   # If the last configured frequency of this check smaller than the frequency in the cfg, then run it now 
   if ( $current_check_frequency < $last_configured_frequency )
   {
      print LOG "Last run check frequency ($last_configured_frequency) is greater than the incoming configured check frequency ($current_check_frequency) - check will run now\n";
      return 1;
   }

   # If now minus the last run time >= last configured check frequency, then run it now
   if ( (time - $last_run_time ) >= $last_configured_frequency )
   {
      printf LOG "Check $checkname ran more than %d seconds ago, which exceeds or equals the last configured check interval ($last_configured_frequency) - check will run now\n", time - $last_run_time;
      return 1;
   }
   else
   {
      printf LOG "Check $checkname ran %d seconds ago, which does not exceed the last configured check interval ($last_configured_frequency) - check will not run\n", time - $last_run_time;
      return 0;

   }

}

# ------------------------------------------
sub get_last_check_details
{
   # looks in the gdma status file and returns by ref details for this check name
   my ( $checkname , $gdmastatfile, $ref_last_run_time, $ref_last_configured_frequency ) = @_;
   my ( $fh, @statfile, $statline, $statcheckname, $statlastfrequency, $statlastruntime , $statlastlocalruntime) ;

   chomp $checkname; chomp $gdmastatfile;

   $fh = new IO::Handle;
   open( $fh, "$gdmastatfile" ) or nsexit( "get_last_check_details : Could not get a handle on the status file $gdmastatfile : $!", 3 );
   @statfile = <$fh>;  
   close $fh or nsexit( "get_last_check_details : Could not close the status file $gdmastatfile : $!", 3 );

   $$ref_last_run_time = $$ref_last_configured_frequency = "hasnotrun"; # assume the check has not yet run and disprove
   foreach $statline ( @statfile ) 
   {
       # format is check name:last configured frequency:last run time in epoch seconds
       chomp $statline; 
       ( $statcheckname, $statlastfrequency, $statlastruntime, $statlastlocalruntime ) = ( split (/:/, $statline ) );
       if ( $statcheckname eq $checkname ) 
       {
           $$ref_last_run_time = $statlastruntime ;
           $$ref_last_configured_frequency = $statlastfrequency;
           chomp $ref_last_run_time ; chomp $ref_last_configured_frequency ;
           last;
       }
   }


}

# ------------------------------------------
sub update_last_run_time
{
   # Updates the last run time of a check.
   # The check times are stored in a gdma status file called ./gdma.status.
   # This status file contains rows, one per check, of the following format :
   # 
   #    check name : configured frequency : last run time in epoch seconds

   my ( $checkname , $gdmastatfile, $configured_frequency ) = @_;
   my ( $fh , @statusfile, $statcheckname, $statfreq, $statcheckname, $statlastfrequency, $statlastruntime, $checkfound, $updatedstat , $statlastlocalruntime);
   # open the status file for update
   $fh = new IO::Handle;

   # create a new row to be used for updating
   $updatedstat =  "${checkname}:${configured_frequency}:" . time. ":" . localtime() . "\n" ;

   # if this is the first time running the agent, then no stat file will exist so just create one with this check's status
   if ( ! -e $gdmastatfile ) 
   {

      open($fh, "> $gdmastatfile") or nsexit( "update_last_run_time : Can't open $gdmastatfile for writing: $!");
      print $fh  "$updatedstat" or nsexit ("update_last_run_time : can't write to status file $gdmastatfile: $!");
      close($fh) or nsexit( "update_last_run_time : Can't close $gdmastatfile after writing: $!");
      return; # we're done 
   }


   open($fh, "+< $gdmastatfile") or nsexit( "update_last_run_time : Can't read $gdmastatfile: $!");

   # suck in the status file which should be pretty tiny so no significant overhead here
   @statusfile = <$fh>;

   # update the frequency and last run time status of the check
   $checkfound = 0; # keep tabs on if the check was found in the stat file or not
   foreach (my $i=0; $i<=$#statusfile ;$i++)
   {
      ( $statcheckname, $statlastfrequency, $statlastruntime, $statlastlocalruntime ) = ( split (/:/, $statusfile[$i] ) );
      if ( $checkname eq $statcheckname )
      {
         $checkfound = 1; 
         $statusfile[$i] = "$updatedstat";
      }
   }

   # if there was no stat info for this check, add it now
   if ( $checkfound == 0 ) 
   {
      push ( @statusfile, "$updatedstat" );
   }

   # write the updated status file
   seek($fh, 0, 0) or nsexit( "update_last_run_time : can't seek to start of $gdmastatfile: $!");
   print $fh @statusfile or nsexit( "update_last_run_time : can't print to $gdmastatfile: $!");
   truncate($fh, tell($fh)) or nsexit( "update_last_run_time : can't truncate $gdmastatfile: $!");
   close($fh) or nsexit( "update_last_run_time : can't close $gdmastatfile: $!");

}

# ------------------------------------------
sub nsexit
{
   my ( $msg, $code ) = @_;
   print "$msg\n"; 
   print LOG "$msg\n"; 
   exit $code;
}


# =========== end per check frequency mods ===========


########################################################################
#	Finished executing system checks. 
########################################################################
my $program_end_time = time;
$program_end_time -= $start_program_time;
print LOG "Total processing time $program_end_time seconds.\n";
exit;
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
		# if ($line=~/^\s*(\S+)\s*=\s*"(.*?)"/) {				# changed this back to greedy behavior on 12/17/2007
		if ($line=~/^\s*(\S+)\s*=\s*"(.*)"/) {					# changed this back to greedy behavior on 12/17/2007
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
########################################################################
#	Generic plugin processing
########################################################################
sub Check_GENERIC {
	my $check_name = shift;
	#if (!defined(%{$config->{$check_name}})) { return } # removed this line - its incorrect - DSN 1/2009
	foreach (my $i=1; $i<=$#{$config->{$check_name}} ;$i++) {
		my $exit = undef; my $text = ""; 
		my $execute_string = "";
		if (!ref($config->{$check_name}->[$i])) { next  }
		if ($config->{$check_name}->[$i]->{Enable} ne "OFF") {


                   # check that there is a Check interval defined for this check and set default one if not
		   if ( not defined $config->{$check_name}->[$i]->{Check_Interval} ) 
                   {
                      #print LOG "Error - check command $check_name\[$i\] does not have a check interval defined - skipping check\n";
                      #return;
                      print LOG "Check command $check_name\[$i\] does not have a check interval defined - using default of $config->{Default_Service_Check_Interval}\n";
		      $config->{$check_name}->[$i]->{Check_Interval}  = $config->{Default_Service_Check_Interval};
                   }

		   if ( $config->{$check_name}->[$i]->{Check_Interval}  !~ /^\s*\d+\s*$/ ) 
                   {
                        print LOG "Check command $check_name\[$i\] has an invalid check interval defined - should be numeric (found $config->{$check_name}->[$i]->{Check_Interval}) - using default of $config->{Default_Service_Check_Interval}\n";
                        $config->{$check_name}->[$i]->{Check_Interval} = $config->{Default_Service_Check_Interval};
                   }
                   
                   if (   run_check_now( $check_name, $config->{Status_File}, $config->{$check_name}->[$i]->{Check_Interval} )   )
                   {
                        update_last_run_time( $check_name, $config->{Status_File}, $config->{$check_name}->[$i]->{Check_Interval} );


			print LOG "Executing $check_name - iteration $i\n";
			#my $execute_string = $config->{Plugin_Directory} ; 
			#$execute_string .= "/".$config->{$check_name}->[$i]->{Command} ; 

			my $execute_string = $config->{$check_name}->[$i]->{Command} ; 			
			$execute_string =~ s/\$Plugin_Directory\$/$config->{Plugin_Directory}/g;
			$execute_string =~ s/\$Target_Host\$/$config->{Target_Host}/g;

			foreach my $option (keys %{$config->{$check_name}->[$i]}) {
				#if ($config->{$check_name}->[$i]->{$option}) {
				if (defined($config->{$check_name}->[$i]->{$option})) {
					print LOG $check_name."[$i]_$option = ".$config->{$check_name}->[$i]->{$option}."\n" ;
					if ($option =~ /^Parm_(--.*)/) {
						$execute_string .= " $1=".$config->{$check_name}->[$i]->{$option} ; 
					} elsif ($option =~ /^Parm_(-.*)/) {
						$execute_string .= " $1 ".$config->{$check_name}->[$i]->{$option} ; 
					} 
				} else {
					if ($option =~ /^Parm_(--.*)/) {
						$execute_string .= " $1" ; 
					} elsif ($option =~ /^Parm_(-.*)/) {
						$execute_string .= " $1" ; 
					}
				}
			}
			print LOG "PLUGIN COMMAND STRING: $execute_string \n";
			my @lines = `$execute_string 2>&1`;
			$exit=$?;	# Exit code
			$text = "";
			if ($exit < 0) {
				$text = "Plugin execution error";
			} else {
				foreach my $line (@lines) {
					if ($debug) { print LOG "PLUGIN OUTPUT: $line"; }
					if ($line =~ /ERROR/) {
						$exit = "512";		# If ERROR found, force status to CRITICAL
					}
					if ($line =~ /(\S.*)/) {
						$text .= $1;
					}
				}
			}
			if ($debug) { print LOG "PLUGIN RESULTS: exit=$exit; text=$text \n"; }
			# <gwhost name>[tab]<service description>[tab]<return code> [tab]<plugin output> | <performance metrics> 
			# echo "<send command text>" | send_nsca -H <monitor server> -c send_nsca.cfg`;

			$text =~ s/\n/ /g;
			$text =~ s/\t/ /g;
			$text =~ s/\"/'/g;
			$text =~ s/\|/^^^|/g;		# Need for perfdata.  Escape the redirection "|"
			$text =~ s/\</^^^</g;			# Escape windows command characters
			$text =~ s/\>/^^^>/g;			# Escape windows command characters
			$text =~ s/\&/^^^&/g;			# Escape windows command characters

			foreach (my $j=1; $j<=$#{$config->{Monitor_Server}} ;$j++) {
				my $send_string = "echo " . time() . "\t";
                                # some customers wish to turn off timestamping altogether
		                if ( $config->{No_Timestamps} ) { print LOG "WARNING : Timestamping is turned off\n" ; $send_string = "echo ";  }

				$send_string .= $config->{Monitor_Host}."\t";
				$send_string .= $config->{$check_name}->[$i]->{Service}."\t";
				$send_string .= $exit_codes{$exit}."\t";
				
				$send_string .= $text." | ";
				$send_string .= $config->{NSCA_Program};
				$send_string .= " -H ".$config->{Monitor_Server}->[$j];	# Only send to primary. Need to add send to backups,
				$send_string .= " -p ".$config->{NSCA_Port};		# added by DAB 12/04/2007 to support NSCA port option
				$send_string .= " -c ".$config->{NSCA_Configuration};	# -c option not needed for Perl script
				print LOG "SEND STRING: $send_string\n";
				send_to_nagios($send_string);
			}
		}

            }
	}
}

########################################################################
#	Generic plugin processing
#		Executes plugin but doesn't send output.
#		The plugin will execute the send_nsca
########################################################################
sub Check_GENERIC_NOSEND {
	my $check_name = shift;
	foreach (my $i=1; $i<=$#{$config->{$check_name}} ;$i++) {
		my $exit = undef; my $text = ""; 
		my $execute_string = "";
		if ($config->{$check_name}->[$i]->{Enable} ne "OFF") {
			print LOG "Executing $check_name - iteration $i\n";
			my $execute_string = $config->{Plugin_Directory} ; 
			$execute_string .= "/".$config->{$check_name}->[$i]->{Command} ; 
			foreach my $option (keys %{$config->{$check_name}->[$i]}) {
				#if ($config->{$check_name}->[$i]->{$option}) {
				if (defined($config->{$check_name}->[$i]->{$option})) {
					print LOG $check_name."[$i]_$option = ".$config->{$check_name}->[$i]->{$option}."\n" ;
					if ($option =~ /^Parm_(--.*)/) {
						$execute_string .= " $1=".$config->{$check_name}->[$i]->{$option} ; 
					} elsif ($option =~ /^Parm_(-.*)/) {
						$execute_string .= " $1 ".$config->{$check_name}->[$i]->{$option} ; 
					} 
				} else {
					if ($option =~ /^Parm_(--.*)/) {
						$execute_string .= " $1" ; 
					} elsif ($option =~ /^Parm_(-.*)/) {
						$execute_string .= " $1" ; 
					}
				}
			}
			print LOG "PLUGIN COMMAND STRING: $execute_string \n";
			my @lines = `$execute_string 2>&1`;
			$text = "";
			foreach my $line (@lines) {
				if ($debug) { print LOG "PLUGIN OUTPUT: $line"; }
				if ($line =~ /(\S.*)/) {
					$text .= $1."\n";
				}
			}
			print LOG "PLUGIN RESULTS: text=$text \n"; 
		}
	}
}

########################################################################
#	send_to_nagios
#
#	Send plug-in output to nagios through send_nsca. If send_nsca
#	fails, the plug-in output is spooled to a local file for retry
#	later.
########################################################################
sub send_to_nagios 
{
	my $send_string = shift;

        if ( ! send_nsca ( $send_string )  )
        { 
           if ( $spooling == 1 ) 
           {
              print LOG "Send to NSCA failed - NSCA command will be spooled\n";
              local_spool($send_string); 
           }
           else
           {
              print LOG "Send to NSCA failed (and spooling is disabled)\n";
           }
        }
	return;
}


########################################################################
# send results over NSCA 
########################################################################
sub send_nsca
{
    my $send_string = shift;
    my $send_result = 0;
    # send_nsca
    my @lines = `$send_string 2>&1`;

    foreach my $line (@lines) 
    {
        # OK response for send_nsca.pl perl script
        if ($line =~ /\d data packet\(s\) sent to host successfully/) 
        {
 		$send_result = 1; 
		last; # dont bother checking other lines for match if we've found success already
	}
    }
    if ($debug) { foreach my $line (@lines) { print LOG "Send-to-NSCA : $line"; } }
    if (!$send_result) 
    {
	 print LOG "Failed to send result with NSCA.\n" if $debug;
         $send_result = 0;
    }
    return $send_result;
}

########################################################################
#	local_spool
#
#	shove failed send_nsca's in the spool for later 
########################################################################

sub local_spool
{
    my $line = shift(@_);

    if (open(SPOOL_FILE, '>>', "$local_spool_filename")) 
    {
	if (print (SPOOL_FILE $line."\n")) 
        {
	    print LOG "Spooled line: $line\n";
	} 
        else 
        {
	    print LOG "Unable to write to spool file $!\n";
	}
	close (SPOOL_FILE);
    } 
    else 
    {
	print LOG "Unable to open spool file $!\n";
    }
}

# ------------------------------------------------------------------------------------------------------------
sub flush_spool 
{
   my ( $spool_line, @failedresends, $spoolcount, $remove_spool, $resent, $lspool );

   print LOG "SPOOLING : Checking for spool file $local_spool_filename\n" if $debug;
   if (  ! -e $local_spool_filename )
   {
       print LOG "SPOOLING : No spool file $local_spool_filename found\n" if $debug;
       return;
   }

   print LOG "SPOOLING : Processing spool entries.\n";

   # process the spool fifo fashion, line at a time.
   # 	try resending the line 
   #    optional : do some smarter processing here of the spool line, for example, compare its destination against the list of target servers
   #               and shove it in a duff spool file if the targets moved for example. 
   #	if doesn't send, remember it
   # replace the spool file with the remembered list of spool entries wich didn't send

   #if ( not open ( SPOOL, "< $local_spool_filename" )  )
   if ( not open ( SPOOL, "+< $local_spool_filename" )  )
   {
      print LOG "SPOOLING : Unable to open spool file for update ($local_spool_filename $!)\n";
      print LOG "Unable to perform spool processing.\n";
      return;
   }

   $spoolcount = $resent = 0; # # of commands in the spool, and # of successfully resent spool entries
   while ( $spool_line = <SPOOL> )
   {

        # do some smarted processing of the spool line here 
        # For now, we'll just convert the destination NSCA host address to that which is defined TBD
 
        # try re-executing the send-nsca command
        $lspool = $spool_line; chomp $lspool; print LOG "SPOOLING : Attempting to resend : '$lspool'\n";
        if ( ! send_nsca ( $spool_line )  )
        {     
            print LOG "SPOOLING : Resend failed\n";
            push(@failedresends, $spool_line) ; # make a note of the spool line if it failed to resend
        }     
        else
        {
            $resent++;
        }

        $spoolcount ++;
   }

   print LOG "SPOOLING : $spoolcount entries were found in the spool, $resent were resent successfully\n";

   # if there were no commands in the spool to run, then log that info and return. This would be in the case
   # where there was an empty spool file that could not be unlinked after being processed
   if ( $spoolcount <= 0 ) 
   {
       print LOG "SPOOLING : Spool file was empty - no resends were necessary.\n";
       return;
   }

   # if there are no failed resends, but resends did occur, then remove the spool file so its not processed again next time
   if ( $#failedresends < 0 ) 
   {
      print LOG "SPOOLING : all spool entries were processed - the spool file $local_spool_filename will be removed\n";
      $remove_spool = 1;
   }
   
   # otherwise, replace the old spool file with the list of spooled entries that failed to resend, so that they might be resent again in future attempts
   else
   {
       $remove_spool = 0;
       print LOG "SPOOLING : replacing contents of spool file with entries that failed to resend\n";
       if ( not (seek( SPOOL, 0, 0 ))          ) { print LOG "SPOOLING : could not seek to start of spool file - spooling aborted : $!\n"; return; }
 
       # don't print the array directory, else you end up with spaces at the beginning of spool lines, instead do one line at a time
       foreach $spool_line ( @failedresends ) 
       {
          if ( not (print SPOOL "$spool_line") ) { print LOG "SPOOLING : could not print updates to spool file - spooling aborted : $!\n"; return; }
       }

       if ( not (truncate(SPOOL, tell(SPOOL))) ) { print LOG "SPOOLING : could not truncation spool file - spooling aborted : $!\n";       return; }
   }
   
   if ( not close ( SPOOL ) ) { print LOG "SPOOLING : unable to close spool file $local_spool_filename $!\n"; }

   if ( $remove_spool == 1 ) 
   {
      unlink ("$local_spool_filename");
      # at least lets hear about not being able to remove the spool file - we want to hear if we are trying to "re-resend" things 
      if ( -e "$local_spool_filename" )    
      { 
          print LOG "SPOOLING : Unable to remove spool file $local_spool_filename ($!)\n";
          print LOG "SPOOLING : This means that the spool will be reprocessed again - might want to manually remove it\n";
      }
   }

}

# ------------------------------------------------------------------------------------------------------------
__END__

sub orig_send_to_nagios {
	my $debug = 1;
	my $send_string = shift;
	my $OK = 0;
	my $try = 0;
	my $maxtry = 2;
	my $minwait = 2;
	my $maxwait = 5;	# will wait between 2 and 7 seconds
	while (!$OK and ($try < $maxtry)) {  
		my @lines = `$send_string 2>&1`;
		foreach my $line (@lines) {
			if ($line =~ /\d data packet\(s\) sent to host successfully/) {		# OK response for send_nsca.pl perl script
				$OK = 1;
			}
		}
		if ($debug) { foreach my $line (@lines) { print LOG $line; } }
		$try++;
		if (!$OK) {
			sleep int(rand($maxwait)) + $minwait;
			print LOG "Failed attempt $try to send to nsca.\n" if $debug;
		}
	} 
	return;
}

__END__

