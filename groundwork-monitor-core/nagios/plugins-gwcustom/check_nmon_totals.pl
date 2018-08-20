#!/usr/local/groundwork/perl/bin/perl --
#
#
#	Copyright 2006, 2011 GroundWork Open Source Solutions, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#
use Time::Local;
use strict;
use Getopt::Std;
my %options=();
my $debug = 0;
my $test = 0;
my $sev = 0;
my $sevtext = 0;
my $outmsg = undef;
my $nmon_tmp_dir = "/home/gwrk/log";
my $nmon_program = "/home/gwrk/libexec/nmon_x86_rhel4";
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $dayofmonth=sprintf("%02d",$mday);
my $nmonfile = "nmon.file.$dayofmonth";
#my $nmonfile = "nmon.file";
#my $nmon_program_start = "$nmon_program -ft -s 600 -F $nmon_tmp_dir/$nmonfile -c 144";

my ($critical_threshold,$warning_threshold) = ();
my $find_keys_ref = undef;
my $helpstring = "
This script will check for a running NMON program if Nmon is running it will read its data files and produce alarms based on the contents of that file.
Options:
 -F     NMON data file.
 -D     Directory containing NMON data file.  Will look for a file with the format dd.nmon
        where dd is the day of the month. -F or -D must be specified.
 -k     Key inidicating metric to report.
           CPU_ALL - Show metrics for all CPUs.
           CPUxx - Show metrics for CPUs where xx is the processor number, ie CPU01, CPU02
                Thresholds are based on Total CPU %.
           MEMORY - Show metrics for memory.
                Thresholds are based on Real Free Memory in MB.
           PAGING - Show metrics for paging.
                Thresholds are based on Memory Pages In + Memory Pages Out .
           DISKIO - Show metrics for disks.
                Thresholds are based on average Disk Busy for all disks.
           TOP - Show top processes.
 -c     Critical threshold
 -w     Warning threshold
 -h     Displays help message.
 -d     Debug mode on. Prints debug messages.
 -t     Test mode. List metric and sub-metrics in the specified file.
 -p     Add performance data

";
my %month = (
	"JAN"=>1,
	"FEB"=>2,
	"MAR"=>3,
	"APR"=>4,
	"MAY"=>5,
	"JUN"=>6,
	"JUL"=>7,
	"AUG"=>8,
	"SEP"=>9,
	"OCT"=>10,
	"NOV"=>11,
	"DEC"=>12
);

getopts("F:D:k:s:w:c:hdtp",\%options);
# like the shell getopt, "d:" means d takes an argument
if (defined $options{h}) {
	print $helpstring;
	exit;
}
if (defined $options{d}) {
	$debug = 1;
}
if (defined $options{t}) {
	$test = 1;
} else {
	if (defined $options{k}) {
		# Set keys to search based on the
		if ($options{k}=~/^(cpu_all|cpu\d\d)$/i) {
			$find_keys_ref->{uc($1)}->{SUBKEYS}->{"User%"}->{DEFINED} = 1;
			$find_keys_ref->{uc($1)}->{SUBKEYS}->{"Sys%"}->{DEFINED} = 1;
			$find_keys_ref->{uc($1)}->{SUBKEYS}->{"Wait%"}->{DEFINED} = 1;
			$find_keys_ref->{"TOP"}->{ALL_SUBKEYS}->{DEFINED} = 1;
		} elsif ($options{k}=~/^memory$/i) {
			# Original AIX settings
			$find_keys_ref->{"MEM"}->{SUBKEYS}->{"Real free(MB)"}->{DEFINED} = 1;
			$find_keys_ref->{"MEM"}->{SUBKEYS}->{"Virtual free(MB)"}->{DEFINED} = 1;
			$find_keys_ref->{"MEMUSE"}->{SUBKEYS}->{"%numperm"}->{DEFINED} = 1;
			# LINUX settings
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"memtotal"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"hightotal"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"lowtotal"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"memfree"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"highfree"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"lowfree"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"swaptotal"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEM"}->{SUBKEYS}->{"swapfree"}->{DEFINED} = 1;
			#$find_keys_ref->{"MEMUSE"}->{SUBKEYS}->{"%numperm"}->{DEFINED} = 1;
		} elsif ($options{k}=~/^paging$/i) {
			$find_keys_ref->{"PAGE"}->{SUBKEYS}->{"pgin"}->{DEFINED} = 1;
			$find_keys_ref->{"PAGE"}->{SUBKEYS}->{"pgout"}->{DEFINED} = 1;
			$find_keys_ref->{"PAGE"}->{SUBKEYS}->{"pgsin"}->{DEFINED} = 1;
			$find_keys_ref->{"PAGE"}->{SUBKEYS}->{"pgsout"}->{DEFINED} = 1;
		} elsif ($options{k}=~/^diskio$/i) {
			$find_keys_ref->{"DISKBUSY"}->{ALL_SUBKEYS}->{DEFINED} = 1;
			$find_keys_ref->{"DISKREAD"}->{ALL_SUBKEYS}->{DEFINED} = 1;
			$find_keys_ref->{"DISKWRITE"}->{ALL_SUBKEYS}->{DEFINED} = 1;
			$find_keys_ref->{"DISKXFER"}->{ALL_SUBKEYS}->{DEFINED} = 1;
			for (my $i=0;$i<10 ;$i++) {
				$find_keys_ref->{"DISKBUSY$i"}->{ALL_SUBKEYS}->{DEFINED} = 1;
				$find_keys_ref->{"DISKREAD$i"}->{ALL_SUBKEYS}->{DEFINED} = 1;
				$find_keys_ref->{"DISKWRITE$i"}->{ALL_SUBKEYS}->{DEFINED} = 1;
				$find_keys_ref->{"DISKXFER$i"}->{ALL_SUBKEYS}->{DEFINED} = 1;
			}
		} elsif ($options{k}=~/^top/i) {
			$find_keys_ref->{"TOP"}->{ALL_SUBKEYS}->{DEFINED} = 1;
		} else {
			print "Required key parameter $options{k} unknown. Must be cpu_all, cpu01, memory, paging, diskio or top\n";
			exit 3;
		}
	} else {
		print "Required key parameter not specified.\n";
		exit 3;
	}
	if (defined $options{c}) {
		$critical_threshold = $options{c};
	} else {
		print "Required critical threshold not specified.\n";
		exit 3;
	}
	if (defined $options{w}) {
		$warning_threshold = $options{w};
	} else {
		print "Required warning threshold not specified.\n";
		exit 3;
	}
}

if (defined $options{F}) {
	$nmonfile = $options{F};
}

if (defined $options{D}) {
	$nmon_tmp_dir = $options{D};
}

$nmonfile = "$nmon_tmp_dir/$nmonfile";

if ($debug) {
	print "Output NMON log file to $nmon_tmp_dir\$nmonfile \n";
	foreach my $key (sort keys %{$find_keys_ref}) {
		print "Find major key: $key\n";
		foreach my $subkey (sort keys %{$find_keys_ref->{$key}->{SUBKEYS}}) {
			print "\tFind subkey: $subkey\n";
		}
	}
}

# Check to make sure nmon is running. If not, start it.
my @lines = `ps -ef | grep $nmon_program | grep -v grep` ;
my $nmon_running = 0;
foreach my $line (@lines) {
	if ($line =~ /$nmon_program/) {
		$nmon_running = 1;
		last;
	}
}
if (!$nmon_running) {
	$sev = "3";
	$sevtext="UNKNOWN";
	$outmsg = "NMON not running";
	print $outmsg ;
exit $sev;
}

my $metric_ref = undef;
my $timestamp_ref = undef;
open (IN,$nmonfile) or die "Unable to open NMON file $nmonfile\n";
while (my $line=<IN>) {
	chomp $line;
	my @keys=split /\s*,\s*/,$line;
	# Modify for AIX bug that shows missing comma in MEM
	#       MEM header shows:
	#               MEM,Memory aixtest01,Real Free Virtual free Real free(MB),Virtual free(MB),Real total(MB),Virtual total(MB)
	#       Should be a comma after "Real Free" and Virtual free
	if (($keys[0] eq "MEM") and ($keys[2] !~ /^[\d\.]+$/)) {
			#$keys[1] = "Memory aixtest01"; # Already set properly
			$keys[2] = "Real Free";
			$keys[3] = "Virtual free";
			$keys[4] = "Real free(MB)";
			$keys[5] = "Virtual free(MB)";
			$keys[6] = "Real total(MB)";
			$keys[7] = "Virtual total(MB)";
	}

	#       End fix for AIX bug
	#

	if ($keys[0] =~ /^AAA/) { next; }		# Skip lines starting with this
	if ($keys[0] =~ /^BBB/) { next; }		# Skip lines starting with this
	if ($keys[0] =~ /^ZZZ/) { 		# Process timestamp lines
		$timestamp_ref->{$keys[1]}->{DAY} =	$keys[3];
		my ($day,$monthtext,$year) = split /-/,$keys[3];
		$timestamp_ref->{$keys[1]}->{TIME} = $keys[2];
		my ($hours,$minutes,$seconds) = split /:/,$keys[2];
		$timestamp_ref->{$keys[1]}->{UTS} = timelocal($seconds, $minutes, $hours, $day, $month{$monthtext}-1, $year-1900);
		next;
	}

	if (!$test) {	# Skip if test. Else only process keys in validkeys array
		my $found = undef;
		foreach my $findkey (keys %{$find_keys_ref}) {
		#	print "Checking line key $keys[0] with findkey $findkey \n" if $debug;
			if ($keys[0] =~/^$findkey$/i) {
		#		print "Matched line key $keys[0] with findkey $findkey \n" if $debug;
				$found = 1;
				last;
			}
		}
		if (!$found) { next	}
	}

	if ($keys[0] ne "TOP") {
		my $found = 0;
		foreach my $findkey (keys %{$find_keys_ref}) {
			if ($findkey =~ /^$keys[0]$/) {
				$found = 1;
				last;
			}
		}
		if (!$found) {
			next;
		}
		if (!defined($metric_ref->{$keys[0]}) ) {
			print "Defining new metric ref for $keys[0] in line: $line\n" if $debug;
			$metric_ref->{$keys[0]}->{DESC} = $keys[1];
			if ($find_keys_ref->{$keys[0]}->{ALL_SUBKEYS}->{DEFINED}){
				for (my $i=2;$i<=$#keys;$i++) {
					$metric_ref->{$keys[0]}->{SUBKEYS}->{$i}->{NAME} = $keys[$i];
				}
			} else {
				foreach my $findsubkey (keys %{$find_keys_ref->{$keys[0]}->{SUBKEYS}}) {
					for (my $i=2;$i<=$#keys;$i++) {
						print "Looking for '$findsubkey', in subkey '$keys[$i]' in line: $line\n" if $debug;
						#if ($keys[$i] =~ /^$findsubkey\d?$/i) {
						if ($keys[$i] eq $findsubkey) {
							print "\tFound '$findsubkey', found subkey '$keys[$i]' in line: $line\n" if $debug;
							$metric_ref->{$keys[0]}->{SUBKEYS}->{$i}->{NAME} = $keys[$i];
							last;
						}
					}
				}
			}
			next;
		} elsif ($keys[1] =~ /^(T\d+)$/) {
			my $timekey = $1;
			foreach my $subkey (keys %{$metric_ref->{$keys[0]}->{SUBKEYS}}) {
				#$metric_ref->{$keys[0]}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$timekey} = $keys[$subkey];
				$metric_ref->{$keys[0]}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{LAST} = $keys[$subkey];
				print "Defining $keys[0], subkey $subkey metric value $keys[$subkey] in line: $line\n" if $debug;
			}
			next;
		}
	} else {	# Process TOP.  Top has multiple header lines. Why????
		#TOP,0876666,T0004,0.10,0.00,0.10,1,64,0,60,0,0,13,aioserver,Unclassified
		if ($keys[2] =~ /^(T\d+)$/) {		# Process TOP time stamped
			my $timekey = $1;
			for (my $i=3;$i<=$#keys;$i++) {
				#$metric_ref->{$keys[0]}->{PID}->{$keys[1]}->{SUBKEYS}->{$i}->{TIMESAMPLES}->{$timekey} = $keys[$i];
				$metric_ref->{$keys[0]}->{PID}->{$keys[1]}->{SUBKEYS}->{$i}->{TIMESAMPLES}->{LAST} = $keys[$i];
			}
			next;
		} else	{			# Process TOP header line. Different than others
		#TOP,+PID,Time,%CPU,%Usr,%Sys,Threads,Size,ResText,ResData,CharIO,%RAM,Paging,Command,WLMclass
			for (my $i=1;$i<=$#keys;$i++) {		# Get all keys for top
				$metric_ref->{$keys[0]}->{SUBKEYS}->{$i}->{NAME} = $keys[$i];
				print "Setting subkey $keys[$i] \n" if $debug;
			}
			next;
		}
	}
}
close IN;
# if  debug, print all found keys, subkeys and time samples
if ($debug) {
	foreach my $key (sort keys %{$metric_ref}) {
		print "Major key: $key, Description: ".$metric_ref->{$key}->{DESC}."\n";
		foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
			print "\tSubkey: $subkey, Name: ".$metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME}."\n";
			foreach my $timesample (sort keys %{$metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}}) {
				print "\t\tTime: ".$timestamp_ref->{$timesample}->{DAY}." ".$timestamp_ref->{$timesample}->{TIME}
					.", value=".$metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$timesample}."\n";
			}
		}
	}
}
#
# if test mode, only show the keys and subkeys, then exit.
if ($test) {
	print "Available key  and subkey values in file $nmonfile:\n";
	foreach my $key (sort keys %{$metric_ref}) {
		print "Major key: $key, Description: ".$metric_ref->{$key}->{DESC}."\n";
		print "Sub-keys:\n";
		foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
			print "\tSubkey: ".$metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME}."\n";
		}
	}
}
#
#	Get last metric and compare to thresholds
#
#	Sort time keys by descending values
my $lasttimestamp = undef;
my $nexttolasttimestamp = undef;
foreach my $timestamp (sort {$timestamp_ref->{$b}->{UTS} <=> $timestamp_ref->{$a}->{UTS}} keys %{$timestamp_ref}) {
	$lasttimestamp = $nexttolasttimestamp;
	$nexttolasttimestamp = $timestamp;
	if ($lasttimestamp) { last }
}
print "Found next to Last time key $nexttolasttimestamp\n"  if $debug;
print "Found last time key $lasttimestamp\n"  if $debug;
my $timestamp = $lasttimestamp;
$lasttimestamp = "LAST";

my $perfstring = undef;
if ($options{k}=~/^cpu/i) {
	foreach my $key (sort keys %{$metric_ref}) {	# Should only be one key. Need foreach because could be CPU_ALL, CPU_01, etc
		if ($key eq "TOP") { next }	#	Don't process top info here.
		my ($cpu_usr,$cpu_sys,$cpu_wait) = ();
		foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
			if ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "User%") {
				#$cpu_usr = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				$cpu_usr = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{LAST}	;
			} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "Sys%") {
				#$cpu_sys = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				$cpu_sys = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{LAST}	;
			} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "Wait%") {
				#$cpu_wait = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				$cpu_wait = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{LAST}	;
			}
		}
		my $cpu_total = $cpu_usr + $cpu_sys + $cpu_wait ;
		if ($cpu_total > $critical_threshold) {
			$sev = "2";
			$sevtext="CRITICAL";
		} elsif ($cpu_total > $warning_threshold) {
			$sev = "1";
			$sevtext="WARNING";
		} else {
			$sev = "0";
			$sevtext="OK";
		}
		$outmsg .= "$sevtext - $key Total=$cpu_total%: User=$cpu_usr%, Sys=$cpu_sys%, Wait=$cpu_wait%.  ";
		$perfstring .= "Total=$cpu_total% User=$cpu_usr Sys=$cpu_sys Wait=$cpu_wait%";
	}
} elsif ($options{k}=~/^memory$/i) {
	my ($mem_real,$mem_virt,$mem_numperm) = ();
	foreach my $key (sort keys %{$metric_ref}) {
		if ($key eq "MEM") {
			foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
#				AIX Settings
				if ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "Real free(MB)") {
					$mem_real = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "Virtual free(MB)") {
					$mem_virt = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				}

#				LINUX Settings
#				if ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "memfree") {
#					$mem_real = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
#				} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "swapfree") {
#					$mem_virt = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
#				}
			}
		} elsif ($key eq "MEMUSE") {
			foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
				if ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "%numperm") {
					$mem_numperm = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
				}
			}
		}
	}
	if ($mem_real < $critical_threshold) {
		$sev = "2";
		$sevtext="CRITICAL";
	} elsif ($mem_real < $warning_threshold) {
		$sev = "1";
		$sevtext="WARNING";
	} else {
		$sev = "0";
		$sevtext="OK";
	}
	$outmsg .= "$sevtext - Memory: Real free=$mem_real(MB), Virtual free=$mem_virt(MB), Numperm=$mem_numperm%.";
	$perfstring .= "Real_free_MB=$mem_real Virt_free_MB=$mem_virt, Numperm=$mem_numperm";
} elsif ($options{k}=~/^paging$/i) {
	my ($page_pgin,$page_pgout,$page_pgsin,$page_pgsout) = ();
	foreach my $key (sort keys %{$metric_ref}) {
		foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {
			if ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "pgin") {
				$page_pgin = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
			} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "pgout") {
				$page_pgout = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
			} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "pgsin") {
				$page_pgsin = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
			} elsif ($metric_ref->{$key}->{SUBKEYS}->{$subkey}->{NAME} eq "pgsout") {
				$page_pgsout = $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp}	;
			}
		}
	}
	my $tmp = $page_pgsin + $page_pgsout;
	if ($tmp > $critical_threshold) {
		$sev = "2";
		$sevtext="CRITICAL";
	} elsif ($tmp > $warning_threshold) {
		$sev = "1";
		$sevtext="WARNING";
	} else {
		$sev = "0";
		$sevtext="OK";
	}
	$outmsg .= "$sevtext - Paging: Memory Pages In=$page_pgsin/sec, Out=$page_pgsout/sec; ".
				" File Pages In=$page_pgin/sec, Out=$page_pgout/sec.";
	$perfstring .= "Mem_Pages_In_per_sec=$page_pgsin Mem_Pages_Out_per_sec=$page_pgsout ".
				" File_Pages_In_per_sec=$page_pgin File_Pages_Out_per_sec=$page_pgout";
} elsif ($options{k}=~/^diskio$/i) {
	# Process for DISKBUSY, DISKREAD, DISKWRITE and DISKXFER
	#DISKBUSY,T0008,4.5,0.0,0.0,5.0,4.6,5.3,11.9,0.0,4.7,24.4,5.4,8.4,24.3,4.7,4.5,24.3,4.6,25.3,...........
	#DISKBUSY1,T0008,26.7,25.9,25.0,26.5,28.8,26.6,29.1,30.1,30.6,18.9,19.3,0.3,38.0,24.4,25.2,...........
	#DISKREAD,T0008,469.8,0.0,0.0,444.0,452.6,483.1,1644.7,0.0,461.3,3073.3,480.8,492.5,3068.5,...........
	#DISKREAD1,T0008,5527.5,5516.0,5452.5,5406.8,5468.6,5461.4,5565.9,5570.6,5545.9,3315.2,...........
	#DISKWRITE,T0008,0.0,0.0,0.0,0.0,0.0,0.0,6.9,0.0,0.0,4.3,0.0,232.7,3.1,0.0,0.0,...........
	#DISKWRITE1,T0008,25.6,15.2,9.8,43.5,16.0,7.1,4.9,2.3,2.3,17.3,10.8,0.0,15.0,...........
	#DISKXFER,T0008,4.2,0.0,0.0,4.0,4.1,4.3,17.0,0.0,4.3,35.5,4.4,12.6,36.2,4.3,4.0,...........
	#DISKXFER1,T0008,11.4,11.3,11.0,11.7,11.2,11.0,11.1,11.1,11.0,9.9,9.3,0.5,12.9,...........
	foreach my $key (sort keys %{$metric_ref}) {
		foreach my $subkey (sort keys %{$metric_ref->{$key}->{SUBKEYS}}) {  # sum all disks
			#$metric_ref->{$key}->{TOTAL} += $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{$lasttimestamp};
			$metric_ref->{$key}->{TOTAL} += $metric_ref->{$key}->{SUBKEYS}->{$subkey}->{TIMESAMPLES}->{LAST};
			$metric_ref->{$key}->{COUNT}++;
		}
	}
	my($diskbusyavg,$diskreadavg,$diskwriteavg,$diskxferavg) = ();
	my $diskbusycount =  $metric_ref->{DISKBUSY}->{COUNT};
	my $diskreadcount =  $metric_ref->{DISKREAD}->{COUNT};
	my $diskwritecount =  $metric_ref->{DISKWRITE}->{COUNT};
	my $diskxfercount =  $metric_ref->{DISKXFER}->{COUNT};
	my $diskbusytotal =  $metric_ref->{DISKBUSY}->{TOTAL};
	my $diskreadtotal =  $metric_ref->{DISKREAD}->{TOTAL};
	my $diskwritetotal =  $metric_ref->{DISKWRITE}->{TOTAL};
	my $diskxfertotal =  $metric_ref->{DISKXFER}->{TOTAL};
	for (my $i=1;$i<10;$i++) {
		$diskbusycount += $metric_ref->{"DISKBUSY$i"}->{COUNT};
		$diskreadcount += $metric_ref->{"DISKREAD$i"}->{COUNT};
		$diskwritecount += $metric_ref->{"DISKWRITE$i"}->{COUNT};
		$diskxfercount += $metric_ref->{"DISKXFER$i"}->{COUNT};
		$diskbusytotal += $metric_ref->{"DISKBUSY$i"}->{TOTAL};
		$diskreadtotal += $metric_ref->{"DISKREAD$i"}->{TOTAL};
		$diskwritetotal += $metric_ref->{"DISKWRITE$i"}->{TOTAL};
		$diskxfertotal += $metric_ref->{"DISKXFER$i"}->{TOTAL};
	}
	if ($diskbusycount > 0) { $diskbusyavg = sprintf "%0.2f",$diskbusytotal / $diskbusycount; } else { $diskbusyavg = "NA" }
	if ($diskreadcount > 0) { $diskreadavg = sprintf "%0.2f",$diskreadtotal / $diskreadcount; } else { $diskreadavg = "NA" }
	if ($diskwritecount > 0) { $diskwriteavg = sprintf "%0.2f",$diskwritetotal / $diskwritecount; } else { $diskwriteavg = "NA" }
	if ($diskxfercount > 0) { $diskxferavg = sprintf "%0.2f",$diskxfertotal / $diskxfercount; } else { $diskxferavg = "NA" }
	if ($diskbusyavg > $critical_threshold) {
		$sev = "2";
		$sevtext="CRITICAL";
	} elsif ($diskbusyavg > $warning_threshold) {
		$sev = "1";
		$sevtext="WARNING";
	} else {
		$sev = "0";
		$sevtext="OK";
	}
#	$outmsg = "$sevtext - Avg for $diskbusycount Disks: Busy=$diskbusyavg%, Read=$diskreadavg kb/s, Write=$diskwriteavg kb/s, Xfer=$diskxferavg transfers/sec.";
#	$perfstring = "Number_Disks=$diskbusycount DiskBusy=$diskbusyavg DiskRead=$diskreadavg DiskWrite=$diskwriteavg DiskXfer=$diskxferavg";
	$outmsg = "$sevtext - For $diskbusycount Disks: AvgDiskBusy=$diskbusyavg TotalRead=$diskreadtotal kb/s, TotalWrite=$diskwritetotal kb/s, TotalXfer=$diskxfertotal transfers/sec.";
	$perfstring = "Number_Disks=$diskbusycount DiskBusy=$diskbusyavg DiskRead=$diskreadtotal DiskWrite=$diskwritetotal DiskXfer=$diskxfertotal";
}

if (($options{k}=~/^top/i) or ($options{k}=~/^cpu/i)) {
	#TOP,+PID,Time,%CPU,%Usr,%Sys,Threads,Size,ResText,ResData,CharIO,%RAM,Paging,Command,WLMclass
	#TOP,0000000,T0001,2730.72,0.00,2730.72,1,64,0,56,0,0,33,Swapper,Unclassified
	$outmsg .= "Top Processes - PID:Command=CPU: ";
	# Get list of Commands and %CPU for the last timestamp
	foreach my $key (sort keys %{$metric_ref}) {
		if ($key =~ /^cpu/i) { next }	#	Don't process top info here.
		my $cmdkey = 13;
		my $cpukey = 3;
		my $maxprocs = 10;	# Maximum # of procs to list in output message
		my $currentprocs = 0;
		# Get PIDs sorted by descending %cpu for last time stamp
		foreach my $pid (sort {$metric_ref->{$key}->{PID}->{$b}->{SUBKEYS}->{$cpukey}->{TIMESAMPLES}->{$lasttimestamp} <=>
							   $metric_ref->{$key}->{PID}->{$a}->{SUBKEYS}->{$cpukey}->{TIMESAMPLES}->{$lasttimestamp}
							   }
						keys %{$metric_ref->{$key}->{PID}}) {
			if ($metric_ref->{$key}->{PID}->{$pid}->{SUBKEYS}->{$cmdkey}->{TIMESAMPLES}->{$lasttimestamp}) {
				$outmsg .= "$pid:".$metric_ref->{$key}->{PID}->{$pid}->{SUBKEYS}->{$cmdkey}->{TIMESAMPLES}->{$lasttimestamp} ."=".
							  $metric_ref->{$key}->{PID}->{$pid}->{SUBKEYS}->{$cpukey}->{TIMESAMPLES}->{$lasttimestamp} ."% ";
				$currentprocs++;
				if ($currentprocs >= $maxprocs ) {
					last;
				}
			}
		}

	}
}
#$outmsg .= " Sample from ".$timestamp_ref->{$nexttolasttimestamp}->{TIME}." to ".$timestamp_ref->{$lasttimestamp}->{TIME}." ".$timestamp_ref->{$lasttimestamp}->{DAY};
$outmsg .= " Sample from ".$timestamp_ref->{$timestamp}->{TIME}." ".$timestamp_ref->{$timestamp}->{DAY};
#
#	Add Perf output
print $outmsg ;
if ($options{p}) {
	print " | ".$perfstring;
}
exit $sev

__END__

