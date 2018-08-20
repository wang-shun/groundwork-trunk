#!/usr/bin/perl --
#
#	Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#

#use strict;
use Time::Local;
use Sys::Hostname;
use Getopt::Long;
use vars qw($opt_c $opt_d $opt_S $opt_h $opt_l $opt_w  $opt_e);
Getopt::Long::Configure('bundling');
my $status = GetOptions
        ("S=s"   => \$opt_S, "Ssourcehost=s"         => \$opt_S,
         "c=s" => \$opt_c, "Config=s"  => \$opt_c,
         "d"   => \$opt_d, "debug"            => \$opt_d,
         "h"   => \$opt_h, "help"            => \$opt_h,
 		 "w"   => \$opt_w, "start_delay"            => \$opt_w,
		 "e"   => \$opt_e, "ref_date"            => \$opt_e,
		 "l=s" => \$opt_l, "Log=s"  => \$opt_l);
my $debug = 0;
my $start_program_time=time;
my $host;
my $Logfile;
my $Configfile;
my %return_codes = ("OK" => "0","WARNING" => "1","CRITICAL" => "2","UNKNOWN" => "3" );
my %exit_codes = ("0" => "0","256" => "1","512" => "2","768" => "3" , "-1"=>"512");
my $helpstring = "
This script will monitor system statistics on this server.
Options: 
-c <CONFIG FILE>	Config file containing monitoring parameters. 
-l <LOG FILE>	Log file for this script. 
-d              Debug mode. Will log additional messages to the log file. 
-h or -help     Displays help message.

Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms.
";

#old way:
#my $hostname = `hostname -s`;
#chomp $hostname;
#
#new way:
my $range = 50;

my $random_number = int(rand($range));

my $hostname = "random_counter_host_$random_number";

#@host_parseout = split(/\./, $fqhn);
#$hostname       = @host_parseout[0];

if ($opt_h)
{
	print $helpstring;
	exit;
}
if ($opt_d)
{
	$debug = 1;
} 
if ($opt_w)
{
    $delay = $opt_w;
}
else
{
	$delay = 60;
}
if ($opt_e) 
{
    $seconds = $opt_e;
}
else
{
    $seconds = time;
}

if ($opt_c)
{
	$Configfile = $opt_c;
}
else
{
#	$Configfile = "/usr/local/groundwork/super/config/super_$hostname.cfg";
	$Configfile = "/usr/local/groundwork/super/config/super_$hostname.cfg";
}
print "Using configuration file $Configfile\n";
if ($opt_S)
{
	$hostname = $opt_S;
}
#
#	Configuration file and create reference for $config
#

my $config = read_config($Configfile);

#
#	print config values
#

if ($debug)
{
	foreach my $param (keys %{$config})
	{
		if (ref($config->{$param}) eq "ARRAY")
		{
			foreach (my $i=0; $i<=$#{$config->{$param}} ;$i++)
			{
				foreach my $option (keys %{$config->{$param}->[$i]})
				{
					print $param."[$i]_$option = ".$config->{$param}->[$i]->{$option}."\n";
				}
			}
		}
		else
		{
			print $param." = ".$config->{$param}."\n";
		}
	}
}

if (!$config->{Output_Logfile})
{
	$config->{Output_Logfile} = "/usr/local/groundwork/super/log/super_$hostname.log";
}
if ($opt_l)
{
	$config->{Output_Logfile} = $opt_l;
}
if (!$config->{Monitor_Host})
{
	$config->{Monitor_Host} = $hostname;
}
if (!$config->{Target_Server})
{
	$config->{Target_Server} = "target.comany.com";
}

########################################################################
##	Open LogFile
########################################################################

open(LOG,">$config->{Output_Logfile}");
print "Using $config->{Output_Logfile} as a log file\n";

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];
print LOG "GroundWork Monitoring Script starting at $thisday, $month $mday, $year. $timestring.\n";    
print LOG "Starting Seconds: $seconds\n";
my $timestring = `date`;
chomp $timestring;
print LOG "GroundWork Monitoring Script starting at $timestring.\n";    
print LOG "Debug set to $debug\n";
print LOG "Using configuration file $Configfile\n";
print LOG "TIME START $hostname: $timestring\n";

########################################################################
#	Start executing system checks if defined in the
#	configuration file.
########################################################################
while (1) { #run forever until killed or you hit a snag
	forkoff();
	sleep $delay;
}


sub forkoff {
	foreach my $checkname (keys %{$config})	{
		if (defined($config->{$checkname})) {		# if in config file
			if ($checkname !~ /^Check_/i) { next; }		# Only process if name starts with "Check_"
				print "Checking $checkname\n";
				$pid = fork();
				if (not defined $pid) {
					print LOG "Resources not available. Check server load, and script length.\n";
					exit 3;
				} else {
					if ($pid == 0) { # This is a child process, so execute the script
				 		Check_GENERIC($checkname); 
						# time to leave this child process...	
						exit (0);
					}
				}
			}
		}


########################################################################
#	Finished executing system checks. 
########################################################################
	my $program_end_time = time;
	$program_end_time -= $start_program_time;
	print LOG "Total processing time $program_end_time seconds.\n";
	return;
}
#exit;
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
		#	Monitor_Server[1]= "groundwork.company.com"
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
sub Check_GENERIC
{
	my $check_name = shift;
	print "check_name=$check_name\n";
	foreach (my $i=1; $i<=$#{$config->{$check_name}} ;$i++) {
		my $exit = undef; my $text = ""; 
		my $execute_string = "";
		if ($config->{$check_name}->[$i]->{Enable} ne "OFF") {
			print LOG "Executing $check_name - iteration $i\n";
			my $execute_string = "$config->{Plugin_Directory}";
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
			$exit=$?;	# Exit code
			$text = "";
			if ($exit < 0) {
				$text = "Plugin execution error";
			}
			else {
				$num_lines=scalar @lines;
				$line_num=0;

				foreach $line (@lines)
				{
					$line_num=$line_num+1;
					if ($debug) { print LOG "PLUGIN OUTPUT: $line"; }
					if ($line =~ /ERROR/) {
						$exit = "512";		# If ERROR found, force status to CRITICAL
					}
					if ($line =~ /(\S.*)/) {
						$text .= $1;
						if ($line_num < $num_lines) {
							$text .= "/";
						}
					}
				}
			}
			if ($debug) { print LOG "PLUGIN RESULTS: exit=$exit; text=$text \n"; }
			# <gwhost name>[tab]<service description>[tab]<return code> [tab]<plugin output> | <performance metrics> 
			# echo "<send command text>" | send_nsca -H <monitor server> -c send_nsca.cfg`;

			my $send_string = "echo \"";
			$send_string .= $config->{Monitor_Host}."\t";
			$send_string .= $config->{$check_name}->[$i]->{Service}."\t";
			$send_string .= $exit_codes{$exit}."\t";
			$text =~ s/\r/\//g;
			$text =~ s/\n/\//g;
			$text =~ s/\t/\//g;
			$text =~ s/\"/'/g;
			#$text =~ s/\|/\//g;
			$send_string .= $text." \" | ";
			$send_string .= "$config->{NSCA_Program}";
#			$send_string .= "/usr/local/groundwork/super/send_nsca2.pl";
			$send_string .= " -t 60 -H ";
			$send_string .= $config->{Target_Server};

			##
			## Now send it to Nagios.
			##

			print LOG "SEND STRING: $send_string\n";
			# Now wait around for a bit to start communications unless it's already too late
			my $elapsed = time;
			$elapsed = $elapsed - $seconds;
			if ($elapsed < $delay ) {
				my $delay = $delay - $elapsed;
				print LOG "It's now $elapsed seconds later: sleeping $delay more seconds\n";
				sleep $delay;
			}
			send_to_nagios($send_string);
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
			foreach $line (@lines) {
				if ($debug) { print LOG "PLUGIN OUTPUT: $line"; }
				if ($line =~ /(\S.*)/) {
					$text .= $1."\n";
				}
			}
			print LOG "PLUGIN RESULTS: text=$text \n"; 
		}
	}
}

sub send_to_nagios
{
	my $debug = 1;
	my $send_string = shift;
	my $OK = 0;
	my $try = 0;
	my $maxtry = 2;
	my $minwait = 2;
	my $maxwait = 5;	# will wait between 2 and 7 seconds

	while (!$OK and ($try < $maxtry))
	{ 
		my @lines = `$send_string 2>&1`;
		foreach $line (@lines)
		{
			if ($line =~ /Sent \d+ packets to /)	# OK response for send_nsca perl script
			{
				$OK = 1;
			}
		}
		if ($debug)
		{
			foreach $line (@lines)
			{
				print LOG $line;
			}
		}

		$try++;
		if (!$OK) {
			sleep int(rand($maxwait)) + $minwait;
			print LOG "Failed attempt $try to send to nsca.\n" if $debug;
		} else { # Log the time taken for testing purposes
			my $exectime = `date`;
			print LOG "TIME DONE THIS MESSAGE $exectime\n" ;
		}
	} 
	return;
}

__END__


sub gettime {
	my $logtime = shift;
   ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=localtime($logtime);
	$year=$year+1900;
	$mon=$mon+1;
	$timet = sprintf "%04d-%02d-%02d %02d:%02d:%02d",$year,$mon,$mday,$hour,$min,$sec;
	$datet = sprintf "%04d-%02d-%02d",$year,$mon,$mday;
	return ($timet,$datet);
}

