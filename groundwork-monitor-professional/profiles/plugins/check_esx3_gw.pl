#!/usr/local/groundwork/perl/bin/perl -w
# vim:ts=4
# check_esx Version 4.0
#
# Check the status of a virtual machine on a VMware ESX server, via SNMP
# Return status in format for either Nagios or MRTG
#
# Steve Shipway (www.steveshipway.org) Nov 2004, Dec 2006, Aug 2007
# Released under GNU GPL
#
# Version 2.0: Added SNMP agent extension to get memory split and ready time
#         2.1: Corrected some bugs.  Use >0.01 instead of >0.
#         2.2: corrected opt_r bug, fa bug
#         2.3:
#         2.4: simpler guest names for list report
#         2.5: Thresholds for LIST given more sensible defaults
#              Added -a alternate for MRTG/Nagios in MEM and CPU
#         2.6: Final tests under ESX3
#         3.0: Merge in GW additions, change -v to -V to standardise
#		  4.0 Complete re-write to use esxtop output
#		  4.1:

use strict;
use Net::SNMP;
use Getopt::Std;
#use Getopt::Long;

my($VERSION) = "4.1";
######## CONFIGURABLE
my($community) = 'public'; # Default community string
my($TRUNC) = 16; # truncte guest names in report to this length (use 99 to stop)
######## END
my($warn,$crit) = (undef,undef);   # usage warn/crit: 70/90 is virtualcentre default
my($readywarn,$readycrit) = (undef,undef); # cpu readytime warn/crit: VMWare say to crit at 5%
my($VMOID) = "1.3.6.1.4.1.6876";          # VMware MIB
my($UCDOID) = "1.3.6.1.4.1.2021.1000.10"; # where to find the agent plugin
my($SYSOID) = "1.3.6.1.2.1.1.1.0";        # system object to test SNMP working
my($OK,$WARNING,$CRITICAL,$UNKNOWN) = (0,1,2,3);
my(%VisibleStatus) = ($OK => "OK", $WARNING => "WARNING", $CRITICAL => "CRITICAL", $UNKNOWN => "UNKNOWN");
my($TIMEOUT) = 5;
my($RETRIES) = 1;
my($hostname) = '';
my($vhost) = '';
my($A, $B, $MSG) = ('U','U','');
my($STATUS) = $UNKNOWN;
my($VMID) = -1; # set to -1 if not running
my($VMNO) = -1; # set to -1 if not defined
my(%lookup) = ();
my($resp);
my($snmp);
my($snmperr);
my($state) = "UNKNOWN"; # Used in finding the state of a guest vm
my(%states) = ();
my(%stats) = ();
my($tput) = ();
#my($fa,$sa,$fb,$sb);
my(@perf) = (); # for performance stats
my($esx_version) = '';
my($vmGuestState) = '';
# For debugging
my($DEBUG) = 0;
#my($SNMPFILE) = "testdata/snmp.txt"; # for test/debug mode only
#my($VMWARESTATS) = "./vmware-stats -d"; # for test/debug mode only
# End

use vars qw($opt_C $opt_H $opt_n $opt_N $opt_M $opt_h $opt_c $opt_t $opt_i $opt_d $opt_w $opt_l $opt_v $opt_r $opt_R $opt_a $opt_V $opt_m $opt_n);

sub base($) {
	return '?' if(!$_[0]);
	return $1 if( $_[0]=~/^(\S+)/ );
	return $_[0];
}

sub dohelp {
    print "Usage: $0 [-h] [-v] [-d] -H host [-C community] \n";
    print "          [-l check [-V vhost] [-w warn[-m ready warn] -c crit [-n ready crit]]]\n";
    print "          [-t timeout] [-R retries]\n";
    print "    -h: just prints this help message\n";
    print "    -v: just prints the script version number\n";
    print "    -d: puts the script into debug mode\n";
    print "    -H host: ESX server machine\n";
    print "    -C community: the SNMP community string (default is \"public\")\n";
    print "    -l check: can be CPU MEM STATE LIST NET DISK (default is LIST)\n";
    print "    -V virtualhost: restrict probing to that one guest host; required for STATE;\n";
    print "        if not specified, probes total ESX system statistics\n";
    print "    -w warn -c crit: Nagios thresholds\n";
    print "    -m ready warn -n ready crit: Ready CPU thresholds, only valid if check is CPU\n";
	print "    and checking a guest.\n";
    print "    -t timeout: ([1..60] seconds) for individual SNMP queries\n";
    print "    -R retries: # of retries ([0..20]) for individual SNMP queries\n";
    print "\nSpecify thresholds as follows.\n";
    print "    MEM for the ESX server is free physical memory in megabytes.\n";
	print "    e.g.: -l MEM -w 2048 -c 1024 \n"; 
    print "    will return critical when less than 1 GB is free.\n";
    print "    MEM for a guest is the instantaneous estimate of active memory in percent.\n";
    print "    e.g.: -l MEM -V vhost -w 80% -c 90%\n";
	print "    will return critical when > 90% of memory is estimated to be active.\n";
    print "    STATE is CRITICAL if vhost is down.\n";
    print "    LIST will return warning if some guests are down, critical if all guests are down\n";
	print "	   when called without thresholds. Thresholds for LIST are in percent.\n";
    print "    NET is Mbits/sec over the sampling interval. \n";
    print "	   Thresholds are raw Mbits/sec.\n";
    print "    e.g.: -l NET -w 45 -c 99\n";
    print "    will return a warning when the network interface is running at > 45 Mbits/sec.\n";
    print "    CPU is percentage of allocated CPU (for vhosts) and of total CPU (if no vhost).\n";
    print "    Thresholds for CPU are in % (the trailing % symbol is optional)\n";
    print "    e.g.: -l CPU -w 80 -c 90\n";
    print "    Thresholds may now also be specified for Ready CPU as follows\n";
    print "    e.g.: -l CPU -w 80 -c 90 -m 10 -n 20\n";
	print "    If both sets are specified, then CPU will take precedence and be checked first.\n";
	print "    DISK is megabytes/sec over the sampling interval. \n";
    print "    Thresholds are raw megabytes/sec.\n";
    print "    e.g.: -l DISK -w 45 -c 99\n";
    print "    will return a warning when the thrashing at > 45 megabytes/sec.\n";

    exit 0;
}
# Subroutines #
###########################################################################
sub dooutput {
			# Nagios: now supporting performance stats
		print "".($VisibleStatus{$STATUS} || "UNKNOWN").": $MSG"
			.(scalar @perf ? "|" . join(" ",@perf) : ""), "\n";
		exit $STATUS;
}

###########################################################################
sub makesnmp() {
	($snmp,$snmperr) = Net::SNMP->session( -hostname=>$hostname,
		-community=>$community, -timeout=>$TIMEOUT, -retries=>$RETRIES );
	print "($snmp)\n" if($DEBUG);


	if($snmperr) {
		$A = $B = 'U';
		print "($snmperr)\n" if($DEBUG);
		$MSG = "Error: $snmperr";
		$STATUS = $UNKNOWN;
		dooutput; # exit
		exit(0);
	}
}

###########################################################################
# Read detailed data from esxtop output, exported to  snmp daemon

sub readagent {
	$MSG = "";
	$resp = $snmp->get_request( -varbindlist=>["$UCDOID.2.1"] ); 
	if(!$resp) {  # Exit with an Error - you need this to be working
		$MSG = "Not able to see agent OID: $UCDOID.2.1";
		$STATUS = $UNKNOWN;
		return 1;
	}
	if( $resp->{"$UCDOID.2.1"} ne 'vmware' ) {
		$MSG = "Incorrect SNMPD configuration: found '".$resp->{"$UCDOID.2.1"}."' when expected 'vmware'";
		$STATUS = $UNKNOWN;
		return 1;
	}
	$resp = $snmp->get_table( -baseoid=>"$UCDOID.101" ); 
	if(!$resp) {  # Exit with an Error - you need this to be working
		$MSG =  "Failed to get table at $UCDOID.101";
		$STATUS = $UNKNOWN;
		return 1;
	} else {
		print "Succeeded in getting table from $UCDOID.101 \n" if ($DEBUG);
		print "$resp \n" if ($DEBUG);
	}
	# Convert the retrieved values to lookup hash
	foreach my $oid ( keys %$resp ) {
		#print "Considering OID $oid and  $resp->{$oid}\n" if $DEBUG;
		if( $oid =~ /\.101\.\d+$/ ) {
			my @stuff = split(/=/,$resp->{$oid});
			if (defined $stuff[1]) {
				$stats{$stuff[0]}=$stuff[1]; 
			}
		}
	}
	return "";
}

###########################################################################
sub getesxversion {
	makesnmp  if(!$snmp);
    print "(snmp lookup)\n" if($DEBUG);
    print "getting $VMOID.1.2.0\n" if($DEBUG);
	$resp = $snmp->get_request( -varbindlist=>[ "$VMOID.1.2.0" ] );
    print "ESX $resp\n" if($DEBUG);
	if(!$resp) {
    	$MSG = "Error: No VMWare SNMP sub-agent running (vmware-snmpd)";
        $STATUS = $UNKNOWN;
        dooutput; # exit
        exit(0);
    } else {
        $esx_version = $resp->{"$VMOID.1.2.0"};
        $esx_version =~ s/\..*//;
    }
}

# Read all the VM IDs from the vmware-snmpd MIB
##########################################################################
sub getvmid {
	print "(snmp lookup)\n" if($DEBUG);
	print "I think snmp is $snmp\n" if($DEBUG);
	$resp = $snmp->get_table( -baseoid=>"$VMOID.2.1.1");
	print "pulling table of VMs: $VMOID.2.1.1 \n" if($DEBUG);
	$resp = $snmp->get_request( -varbindlist=>[ "$VMOID.1.1.0" ] );
	if(!$resp) {
		$MSG = "Error: No VMWare SNMP sub-agent running (vmware-snmpd)";
		$STATUS = $UNKNOWN;
		dooutput; # exit 
		exit(0);
	}
	foreach my $oid ( keys %$resp ) {
		$oid =~ /(\d+)\.(\d+)$/;
		if( $1 == 2 ) {
			$lookup{$resp->{$oid}} = $2;
			$lookup{$2} = $resp->{"$VMOID.2.1.1.7.$2"};
			$lookup{$resp->{"$VMOID.2.1.1.7.$2"}} = $resp->{$oid};
      $lookup{"vmGuestState-$2"} = $resp->{"$VMOID.2.1.1.8.$2"};
		}
	}
	return if(!$vhost); # we're just getting the table
	if(defined $lookup{$vhost}) {
		$VMNO = $lookup{$vhost};
		if( defined $lookup{$VMNO} ) {
			$VMID = $lookup{$VMNO};
      if ( defined $lookup{"vmGuestState-$VMNO"} ) {
      	$vmGuestState = $lookup{"vmGuestState-$VMNO"};
      }
		} else {
			$STATUS = $CRITICAL;
			$MSG = "Virtual host $vhost($VMNO) is not running!";
		}
	} else {
		# lets see if they just gave part of the vhost name?
		$VMNO = "U";
		foreach ( keys %lookup ) {
			if( /^$vhost/i ) {
				$VMNO = $lookup{$_};
				if( defined $lookup{$VMNO} ) {
					$VMID = $lookup{$VMNO};
          if ( defined $lookup{"vmGuestState-$VMNO"} ) {
          	$vmGuestState = $lookup{"vmGuestState-$VMNO"};
          }
					$vhost = $_;
				} else {
					$STATUS = $CRITICAL;
					$MSG = "Virtual host $vhost($VMNO) is not running!";
				}
				last;
			}
		}
		if($VMNO eq "U") {
			$STATUS = $UNKNOWN;
			$MSG = "Virtual host $vhost is not defined!";
			dooutput; # exit 
			exit(0);
		}
	}

	print "(hostno=$VMNO, ID=$VMID)\n" if($DEBUG);
}


### List out vms, state, etc using VMware SNMP agent ###
#############################################################################
sub listvm {
	my(@vh);
	%lookup = (); @vh = ();
	print "(snmp lookup)\n" if($DEBUG);
	makesnmp() if(!$snmp);
	$resp = $snmp->get_table( -baseoid=>"$VMOID.2.1.1");
	if(!$resp) {
    	$MSG = "Error: No VMWare SNMP sub-agent running (vmware-snmpd)";
     	$STATUS = $UNKNOWN;
     	dooutput; 
     	exit(0);
	} else {
	   foreach my $oid ( sort keys %$resp ) {
	   	$oid =~ /(\d+)\.(\d+)$/;
	   		if( $1 == 2 ) {
	    		$lookup{$resp->{$oid}} = $2;
	   			push @vh, $resp->{$oid};
	  		} elsif( $esx_version == 2 && $1 == 7 ) {
	    		$lookup{$2} = $resp->{$oid};
	    	} elsif( $esx_version == 3 && $1 == 6 ) {
	    		$lookup{$2} = $resp->{$oid};
	    	}
	 	}
	}
	$A = $B = 0;
	foreach ( @vh ) { 
		next if(!$_);
		$B++; 
    	if ( $esx_version == 2 ) {
    		if( defined $lookup{$lookup{$_}} and ($lookup{$lookup{$_}} > 0)) {
      			$_ = (substr $_,0,$TRUNC)."(".$lookup{$lookup{$_}}.")"; 
				$A++;
			  	if ($_ =~ /$vhost/) {
                    $state = "UP";
                }

      		} else {
      			$_ = (substr $_,0,$TRUNC)."(DOWN)";
				if ($_ =~ /$vhost/) {
                    $state = "DOWN";
                }

      		}
    	} else {
    		if( defined $lookup{$lookup{$_}} and ($lookup{$lookup{$_}} eq "poweredOn")) {
      			$_ = (substr $_,0,$TRUNC)."(UP)"; 
				$A++;
				if ($_ =~ /$vhost/) {
					$state = "UP";
				}
      		} else {
      			$_ = (substr $_,0,$TRUNC)."(DOWN)";
				if ($_ =~ /$vhost/) {
                    $state = "DOWN";
                }

      	}
    }
    	$_ =~ s/ *\([^\)]+\)(\(.*\))/$1/;
	}
   	if(!$vhost && !($opt_l =~ /CPU/i ) && !($opt_l =~ /MEM/i) && !($opt_l =~ /NET/i) && !($opt_l =~ /DISK/i)) {
		$MSG = "VHosts: $A/$B up: ".(join ", ",@vh);
  		push @perf, "allvms_up_ct=$A;;;0;$B";
  		push @perf, "allvms_up_pc=". int($A/$B*10000)/100.0 ."%;;;0;100";
  		$STATUS = $OK;
	}

}

#####################################################################################
sub readxcpu {
	my($key,$A,$B,$C);
	$MSG = ""; $A = "U"; $B = "U"; $STATUS = 0;
	if($vhost) {
		# Get CPU stats for the requested virtual host
		print "looking for stats for $vhost\n" if $DEBUG; 
		foreach $key ( keys %stats ) {
			# Handle those pesky special characters in host display names
			my $vhost_pattern = undef;
			$vhost_pattern = $vhost;
			$vhost_pattern =~ s/(\W)/\\$1/g;
			if (($key =~ /$vhost_pattern/) and ($key =~ /Group Cpu(.+)% Used/)) {
                $A = $stats{$key};
				print "FOUND key: $key, stats: $stats{$key} for vhost $vhost\n" if $DEBUG;
            }
			if (($key =~ /$vhost_pattern/) and ($key =~ /Group Cpu(.+)% Ready/)) {
				$B = $stats{$key};
				print "FOUND key: $key, stats: $stats{$key} for vhost $vhost\n" if $DEBUG;
			}
		}
		if($A =~ /U/ or $B =~ /U/) {
        	$MSG="No CPU info found for $vhost. Please wait for next poll";
        	$STATUS = 3;
			push @perf, "vhost_cpu_used_pc=U%;;;0;100";
        	push @perf, "vhost_cpu_ready_pc=U%;;;0;100";
			dooutput; exit 3;
		} else {
			$MSG = "Guest $vhost CPU used=$A% ready=$B%";
	        push @perf, "vhost_cpu_used_pc=$A%;;;0;100";
    	    push @perf, "vhost_cpu_ready_pc=$B%;;;0;100";
		}
	} else {
		# Get total stats for ESX server
		foreach $key ( keys %stats ) {
        	print "key: $key, stats: $stats{$key} \n" if $DEBUG;
        	if ($key =~ /Physical Cpu\(_Total\)/) {
        		$A = $stats{$key};
				$MSG = "ESX server $hostname CPU running at $A percent";
				push @perf, "sys_cpu_used_pc=$A%;;;0;100";
        	}
		}
		# Check if we found any stats at all.
		if($A =~ /U/) {
			# No data found. Report error.
			$A=$B='U'; $MSG="No CPU info found for ESX server $hostname. Please wait for next poll";
			$STATUS = 3;
			# Fill in some dummy performance data anyway, to keep downstream processes somewhat happy.
			push @perf, "sys_cpu_used_pc=U%;;;0;100";
			dooutput;
			exit 3;
		}
	}
	# Set status according to thresholds, if present
	if ($warn && $crit) {
			$warn =~ s/%//;
			$crit =~ s/%//;
			if ($A>$crit) {
				$STATUS = 2;
				dooutput;
				exit 2;
			} elsif ($A>$warn) {
				$STATUS = 1;
				dooutput;
				exit 1;
			} 
	}
	if ($readywarn && $readycrit && !($B=~ /U/)) {
			print "Read tholds: rw  $readywarn rc $readycrit value $B\n" if $DEBUG; 
			$readywarn =~ s/%//;
            $readycrit =~ s/%//;
            if ($B>$readycrit) {
				$STATUS = 2;
                dooutput;
                exit 2;
            } elsif ($B>$readywarn) {
				$STATUS = 1;
                dooutput;
                exit 1;
            } 
	}
	# No thresholds, or no violations, so exit with normal output
	$STATUS = 0;
    dooutput;
    exit 0;
}

############################################################################################	
sub readxmem {
    my($key,$A,$B,$C,$k1,$k2);
    $MSG = ""; $A = "U"; $B = "U"; $C = "U"; $STATUS = 0;
	# first off, handle the case where we are checking memory use on a guest
	if($vhost) {	
	 # Get memory stats for the requested virtual host
        print "looking for memory stats for $vhost\n" if $DEBUG;
        foreach $key ( keys %stats ) {
            print "key: $key, stats: $stats{$key} vhost $vhost}\n" if $DEBUG;
			# Handle those pesky special characters in host display names
            my $vhost_pattern = undef;
            $vhost_pattern = $vhost;
            $vhost_pattern =~ s/(\W)/\\$1/g;
            if (($key =~ /$vhost_pattern/) and ($key =~ /Group Memory(.+),% Active Estimate/)) {
                $A = $stats{$key};
            }
            if (($key =~ /$vhost_pattern/) and ($key =~ /Group Memory(.+),% Active Slow Estimate/)) {
                $B = $stats{$key};
            }
        }
		if($A =~ /U/ or $B =~ /U/) {
            $MSG="No memory info found for $vhost. Please wait for next poll";
            $STATUS = 3;
            push @perf, "vhost_mem_active_pc=U%;;;0;100";
            push @perf, "vhost_mem_ave_pc=U%;;;0;100";
            dooutput; exit 3;
        } else {
            $MSG = "Guest $vhost active memory=$A% Long term average active memory=$B%";
            push @perf, "vhost_mem_active_pc=$A%;;;0;100";
            push @perf, "vhost_mem_ave_pc=$B%;;;0;100";
        }
	} else {
    	# Get total memory stats for ESX server
    	foreach $key ( keys %stats ) {
        	if ($key =~ /^Memory,Free MBytes/) {
            	$A = $stats{$key};
			}
			if ($key =~ /^Memory,Memctl Current MBytes/) {
            	$B = $stats{$key};
        	}
			if ($key =~ /^Memory,Console MBytes/) {
            	$k1 = $stats{$key};
        	}
			if ($key =~ /^Memory,Machine MBytes/) {
            	$k2 = $stats{$key};
        	}
		}
		# if there is no data at all, wait a bit.
		if($A =~ /U/ or $B =~ /U/) {
        	$MSG="No memory info found for ESX server $hostname. Please wait for next poll";
        	$STATUS = 3;
        	push @perf, "sys_mem_free_mb=U;;;0;0";
        	push @perf, "sys_ballon_mem_mb=U;;;0;0";
			push @perf, "sys_console_mem_pc=U%;;;0;100";
        	dooutput; exit 3;
		} else {
			$C = int($k1/$k2*10000)/100.0;
        	$MSG = "ESX server $hostname has $A megabytes of memory free, with $B reclaimed by balloning, and $C% used by console";
			push @perf, "sys_mem_free_mb=$A;;;0;0";
        	push @perf, "sys_ballon_mem_mb=$B;;;0;0";
        	push @perf, "sys_console_mem_pc=$C%;;;0;100";
    	}
 	}
	# Determine status via thresholds (free memory for ESX, active memory for vhost)
	if ($warn && $crit) {
		if ($vhost) {	# Percentage thresholds are crit>warn
			$warn =~ s/%//;
			$crit =~ s/%//;
			if ($A>$crit) {
                $STATUS = 2;
                dooutput;
                exit 2;
            } elsif ($A>$warn) {
                $STATUS = 1;
                dooutput;
                exit 1;
            }
		} else {		# Free memory thresholds are crit<warn
			if ($A<$crit) {
                $STATUS = 2;
                dooutput;
                exit 2;
            } elsif ($A<$warn) {
                $STATUS = 1;
                dooutput;
                exit 1;
            }
		}
    }
    # No thresholds, or no violations, so exit with normal output
    $STATUS = 0;
    dooutput;
    exit 0;
}

sub readxnet {
    my($key,$A,$B,$C,$k1,$k2);
    $MSG = ""; $A = "U"; $B = "U"; $C = "U"; $STATUS = 0;
    # first off, handle the case where we are checking network use on a guest
    if($vhost) {
     # Get network stats for the requested virtual host
        print "looking for network stats for $vhost\n" if $DEBUG;
        foreach $key ( keys %stats ) {
             # Handle those pesky special characters in host display names
            my $vhost_pattern = undef;
            $vhost_pattern = $vhost;
            $vhost_pattern =~ s/(\W)/\\$1/g;
			print "key: $key, stats: $stats{$key} vhost $vhost}\n" if $DEBUG;
            if (($key =~ /$vhost_pattern/) and ($key =~ /Network Port(.+),MBits Received/)) {
                $A = $stats{$key};
            }
            if (($key =~ /$vhost_pattern/) and ($key =~ /Network Port(.+),MBits Transmitted/)) {
                $B = $stats{$key};
            }
        }
        if($A =~ /U/ or $B =~ /U/) {
            $MSG="No network info found for $vhost. Please wait for next poll";
            $STATUS = 3;
			push @perf, "vhost_net_mbps=U;;;;";
            push @perf, "vhost_net_mbps_in=U;;;;";
            push @perf, "vhost_net_mbps_out=U;;;;";
            dooutput; exit 3;
        } else {
			$tput = $A + $B;
            $MSG = "Guest $vhost network total throughput is $tput MBits per second, with $A received and $B transmitted";
			push @perf, "vhost_net_mbps=$tput;;;;";
            push @perf, "vhost_net_mbps_in=$A;;;;";
            push @perf, "vhost_net_mbps_out=$B;;;;";
        }
    } else {
        # Get total network stats for all guests
		$tput = 0;
        foreach $key ( keys %stats ) {
            if ($key =~ /Network Port(.+),MBits Received/) {
				$A = 0 if ($A =~/U/);
                $A = $A + $stats{$key};
            }
            if ($key =~ /Network Port(.+),MBits Transmitted/) {
				$B = 0 if ($B =~/U/);
                $B = $B + $stats{$key};
            }
        }
        # if there is no data at all, wait a bit.
        if($A =~ /U/ or $B =~ /U/) {
            $MSG="No network info found for ESX server $hostname. Please wait for next poll";
            $STATUS = 3;
            push @perf, "sys_net_mbps=U;;;;";
            push @perf, "sys_net_mbps_in=U;;;;";
            push @perf, "sys_net_mbps_out=U;;;;";
			dooutput; exit 3;
        } else {
            $tput = $A + $B;
            $MSG = "ESX server $hostname is using $tput MBits per second for all guests, with $A received and $B transmitted.";
			push @perf, "sys_net_mbps=$tput;;;;";
            push @perf, "sys_net_mbps_in=$A;;;;";
            push @perf, "sys_net_mbps_out=$B;;;;";
        }
    }
    # Determine status via thresholds 
    if ($warn && $crit) {
        $warn =~ s/%//;
        $crit =~ s/%//;
        if ($A>$crit) {
            $STATUS = 2;
            dooutput;
            exit 2;
        } elsif ($A>$warn) {
            $STATUS = 1;
            dooutput;
            exit 1;
        }
    }
    # No thresholds, or no violations, so exit with normal output
    $STATUS = 0;
    dooutput;
    exit 0;
}

sub readxdisk {
    my($key,$A,$B,$C,$k1,$k2);
    $MSG = ""; $A = "U"; $B = "U"; $C = "U"; $STATUS = 0;
    # first off, handle the case where we are checking disk use on a guest
    if($vhost) {
     # Get disk stats for the requested virtual host
        print "looking for disk stats for $vhost\n" if $DEBUG;
        foreach $key ( keys %stats ) {
            # Handle those pesky special characters in host display names
            my $vhost_pattern = undef;
            $vhost_pattern = $vhost;
            $vhost_pattern =~ s/(\W)/\\$1/g;
			print "key: $key, stats: $stats{$key} vhost $vhost}\n" if $DEBUG;
            if (($key =~ /$vhost_pattern/) and ($key =~ /Physical Disk(.+),MBytes Read/)) {
				$A = 0 if ($A =~/U/);
                $A = $A + $stats{$key};
            }
            if (($key =~ /$vhost_pattern/) and ($key =~ /Physical Disk(.+),MBytes Written/)) {
				$A = 0 if ($A =~/U/);
                $B = $B + $stats{$key};
            }
        }
        if($A =~ /U/ or $B =~ /U/) {
            $MSG="No disk info found for $vhost. Please wait for next poll";
            $STATUS = 3;
            push @perf, "vhost_disk_mbps=U;;;;";
            push @perf, "vhost_disk_mbps_rd=U;;;;";
            push @perf, "vhost_disk_mbps_wr=U;;;;";
            dooutput; exit 3;
        } else {
            $tput = $A + $B;
            $MSG = "Guest $vhost total disk throughput is $tput megabytes per second, with $A read and $B written";
            push @perf, "vhost_disk_mbps=$tput;;;;";
            push @perf, "vhost_disk_mbps_rd=$A;;;;";
            push @perf, "vhost_disk_mbps_wr=$B;;;;";
        }
    } else {
        # Get total disk stats for ESX server
        $tput = 0;
        foreach $key ( keys %stats ) {
            if ($key =~ /Physical Disk(.+),MBytes Read/) {
				$A = 0 if ($A =~/U/);
                $A = $A + $stats{$key};
            }
            if ($key =~ /Physical Disk(.+),MBytes Written/) {
				$B = 0 if ($B =~/U/);
                $B = $B + $stats{$key};
            }
        }
        # if there is no data at all, wait a bit.
        if($A =~ /U/ or $B =~ /U/) {
            $MSG="No disk info found for ESX server $hostname. Please wait for next poll";
            $STATUS = 3;
            push @perf, "sys_disk_mbps=U;;;;";
            push @perf, "sys_disk_mbps_rd=U;;;;";
            push @perf, "sys_disk_mbps_wr=U;;;;";
            dooutput; exit 3;
        } else {
            $tput = $A + $B;
            $MSG = "ESX server $hostname is processing $tput megabytes per second to and from virtual disks, with $A read and $B written.";
            push @perf, "sys_disk_mbps=$tput;;;;";
            push @perf, "sys_disk_mbps_rd=$A;;;;";
            push @perf, "sys_disk_mbps_wr=$B;;;;";
        }
    }
    # Determine status via thresholds
    if ($warn && $crit) {
        $warn =~ s/%//;
        $crit =~ s/%//;
        if ($A>$crit) {
            $STATUS = 2;
            dooutput;
            exit 2;
        } elsif ($A>$warn) {
            $STATUS = 1;
            dooutput;
            exit 1;
        }
    }
    # No thresholds, or no violations, so exit with normal output
    $STATUS = 0;
    dooutput;
    exit 0;
}

########################### MAIN ############################


getopts('vahrdiNMH:c:t:V:w:C:l:i:R:m:n:');
$hostname = $opt_H if($opt_H);
$vhost = $opt_V if($opt_V);
#$vhost =~ s/\"//g;
if ($opt_w) {
	$warn = $opt_w;
}
if($opt_c) {
	$crit = $opt_c;
}
$TIMEOUT = $opt_t if($opt_t);
$RETRIES = $opt_R if($opt_R);
$community = $opt_C if($opt_C);
$readywarn = $opt_m if($opt_m);
$readycrit = $opt_n if($opt_n);
$DEBUG = 1 if($opt_d);
dohelp if($opt_h);
my($donereadagent) = 0;

# show me the version of this plugin
if($opt_v) {
	print "(did you mean to use -V?) " if($opt_C or $opt_H);
	print "check_esx version $VERSION\n";
	exit 0;
}
# Check for errors in the arguments
if(!$hostname) {
	$MSG = "No ESX server hostname specified with -H";
	dooutput;
	exit 0;
}
if( !$opt_l  ) {
	$opt_l = "LIST";
}

if( $opt_l =~ /LISTNET/i ) {
    $MSG = "The LISTNET option is no longer supported.";
    dooutput;
    exit 3;
}

if( $opt_l !~ /NET|CPU|MEM|STAT|LIST|DISK/i ) {
    $MSG = "Bad command $opt_l!";
    dooutput;
    exit 3;
}

# Now start working on getting statistics

# ESX version. Might turn out to be a pre-requisiste for some measures
getesxversion;

# List the VMs. Also called for other purposes. 
if( $opt_l =~ /LIST/i ) {
	listvm;
	if(!$opt_w) { $warn = $B - 1; }
	if(!$opt_c) { $crit = 0; }
	if($warn =~ /(\d+)\%/) {
		$warn = $B * $1 / 100;
	} elsif( $warn < 0 ) { 
		$warn = $B - 1; 
	}
	if($crit =~ /(\d+)\%/) {
		$crit = $B * $1 / 100;
	} elsif( $crit < 0 ) { 
		$crit = 0; 
	}
	$STATUS = $WARNING if($A<=$warn); # If SOME are down
	$STATUS = $CRITICAL if($A<=$crit); # If NONE are up
	$STATUS = $OK if(!$B); # No guests at all
	dooutput;
	exit 3;
}


# Report that state of one guest vm. You have to specify the vm) with a -V option as well
if( $opt_l =~ /STATE/i ) {
	if (!$vhost) {
		$MSG = "No guest specified for STATE. Use -V guestname";
    	dooutput;
    	exit 3;
	}
	listvm;
	if ($state =~ /DOWN/) {
		$MSG = "Guest $vhost is DOWN";
		$STATUS = $CRITICAL;
		dooutput;
		exit 2;
	} elsif ($state =~ /UNKNOWN/) {
		$MSG = "Could not determine the state of guest $vhost";
		$STATUS = $UNKNOWN;
		dooutput;
		exit 3;
	} else {
		$MSG = "Guest $vhost is UP";
        $STATUS = $OK;
        dooutput;
        exit 0;
	}
}

# Report on cpu  - terminal subroutine: we never come back here.
if( $opt_l =~ /CPU/i ) {
	listvm;
	readagent;
	readxcpu;
}

# Report on memory - terminal subroutine
if( $opt_l =~ /MEM/i ) {
    listvm;
    readagent;
    readxmem;
}

# Report on network use - terminal subroutine
if( $opt_l =~ /NET/i ) {
    listvm;
    readagent;
    readxnet;
}

# Report on disk use - terminal subroutine
if( $opt_l =~ /DISK/i ) {
    listvm;
    readagent;
    readxdisk;
}

