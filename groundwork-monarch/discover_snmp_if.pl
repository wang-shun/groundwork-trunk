#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007, 2008, 2009 GroundWork Open Source, Inc. (GroundWork)  
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

use strict;
#-----------------------------------------------------------------------------
# Constant Declarations
#-----------------------------------------------------------------------------
my $GW_HOME   = "/usr/local/groundwork" ;
my $CSVIMPORT = $GW_HOME."/core/monarch/csvimport" ;

#-----------------------------------------------------------------------------
# Variable Declarations
#-----------------------------------------------------------------------------
my $sep             ; # Output file field separator.
my @hostipaddresses ; # List of IP addresses to try.
my $progname        ; # this program name.
my @tmp             ; # temporary utility array.
my $basename        ; # base of program name for use in other filenames.
my $outfile         ; # Output file name.
my $logfile         ; # Log file name.

#-----------------------------------------------------------------------------
# Default option values
#-----------------------------------------------------------------------------
my $debug           = 0        ; # "-d"
my $communitystring = "public" ; # "-c"
my $snmp_version    = "1"      ; # not implemented as a switch
my $output_type     = "csv"    ; # not implemented as a switch

#-----------------------------------------------------------------------------
# Utility Commands
#-----------------------------------------------------------------------------
my $snmpwalk = $GW_HOME."/common/bin/snmpwalk";
my $snmpget  = $GW_HOME."/common/bin/snmpget";

@tmp      = split('[\/]',$0)        ; $progname = $tmp[$#tmp] ;
@tmp      = split('[\.]',$progname) ; $basename = $tmp[0]     ;
$logfile  = $basename.".log" ;
$outfile  = $CSVIMPORT."/".$basename ;

#-----------------------------------------------------------------------------
# Process Arguments
#-----------------------------------------------------------------------------
# Any Args that aren't switches are IP Addresses or ranges.
#-----------------------------------------------------------------------------

for (my $i=0; $i<=$#ARGV; $i++) {
    my $A = $ARGV[$i] ;
    if ($A =~ /^-/) {
        #---------------------------------------------------------------------
        # Switch options
        #---------------------------------------------------------------------
        if ( $A =~ /^-C$/i ) {
            $i++ ; $communitystring = $ARGV[$i] ;
        }
        elsif ( $A =~ /^-O$/i ) {
            $i++ ; $outfile = $ARGV[$i] ;
        }
        elsif ( $A =~ /^-D$/i ) {
            $debug = 1 ;
        }
        elsif ( $A =~ /^-(H|-help)$/i ) {
            usage() ; exit 0 ;
        }
        else  {
            usage() ;
            die "Error: Bad Argument: ".$A."\n" ;
        }
        #---------------------------------------------------------------------
    }
    else {
        #---------------------------------------------------------------------
        # IP Addresses or Address Ranges
        #---------------------------------------------------------------------
        if ($A =~ /^(\d+)[\.](\d+)[\.](\d+)[\.](\d+)$/) {
            push @hostipaddresses, $A ;
        }
        #---------------------------------------------------------------------
        # This is a scan of an entire subnet for SNMP devices where:
        # ###.###.### is the subnet.
        #
        elsif ($A =~ /^(\d+)[\.](\d+)[\.](\d+)$/) {
            for (my $ip=1; $ip<=254; $ip++) {
                push @hostipaddresses, $A.".".$ip ;
            }
        }
        #---------------------------------------------------------------------
        # This is a scan of a range of IPs within a subnet. where:
        # ###.###.###.###-### is a range of addresses within the subnet
        #
        elsif ($A =~ /^(\d+)[\.](\d+)[\.](\d+)[\.](\d+)[-](\d+)$/) {
            my ($ip1,$ip2,$ip3,$r1,$r2) = ($1,$2,$3,$4,$5) ;
            $A = $ip1.".".$ip2.".".$ip3 ;
            for (my $ip=$r1; $ip<=$r2; $ip++) {
                push @hostipaddresses, $A.".".$ip ;
            }
        }
        #---------------------------------------------------------------------
        # We could implement other range constructs
        # here or error out.
        #
        else {
            die "Error: Illegal IP Address: ".$A."\n" ;
        }
        #---------------------------------------------------------------------
    }
}

#-----------------------------------------------------------------------------
# Set Output type vars
#-----------------------------------------------------------------------------
if ($output_type =~ /^csv$/i) {
    $sep      = "," ;    
    $outfile .= ".".$output_type ;
}
else {
    usage(); die "Error: Invalid Output type: ".$output_type."\n" ;
}
#-----------------------------------------------------------------------------

if ( -e $outfile ) {
    print "This script will overwrite ".$outfile.".\n" ;
    print "Is that OK ? (yes=cr,no) " ;
    my $input = <STDIN>;
    chomp $input;
    if ($input =~ /^(n|no)$/i) {
        die "OK, Exiting...\n" ;
    }
}

open (LOG, '>', $outfile) or die "Can't open log file $outfile\n" if $debug;
open (OUT, '>', $outfile) or die "Can't open output file $outfile\n";

if ($output_type eq "csv") {
    print OUT "Host_IPAddress,Host_Name,Service_Name,Service_Index,Service_Descr,Service_Speed\n";
    foreach my $hostipaddress (@hostipaddresses) {
        my $servicename = undef;
        my $host_name=undef;
        my %ifDescr = ();
        my %ifIndex = ();
        my %ifSpeed = ();
        my $command = "$snmpwalk -v $snmp_version -c \"$communitystring\" $hostipaddress";
        my @lines = `$command`;
        if ($#lines == -1) {
            print "No output from snmpwalk of ".$hostipaddress." - skipping.\n" ;
        }
        else {
            print @lines if $debug;
            foreach my $line (@lines) {
                if ($line =~ /sysName\.0 = STRING:\s+(.*)/i) {
                    $host_name = $1;
                }    
                if ($line =~ /ifDescr\.(\d+) = STRING:\s+(\S+)/i) {
                    $ifDescr{$1} = $2;
                }    
                if ($line =~ /ifIndex\.(\d+) = INTEGER:\s+(\d+)/i) {
                    $ifIndex{$1} = $2;
                }    
                if ($line =~ /ifSpeed\.(\d+) = Gauge32:\s+(\d+)/i) {
                    $ifSpeed{$1} = $2;
                }    
            }
            my $csv_prefix = $hostipaddress.$sep.$host_name.$sep."SNMP_" ;

            foreach my $index (sort keys %ifIndex) {
                my $csv_suffix = $ifIndex{$index}.$sep.$ifIndex{$index}.$sep
                                .$ifDescr{$index}.$sep.$ifSpeed{$index}."\n" ;
                print OUT $csv_prefix."if_"          .$csv_suffix;
                print OUT $csv_prefix."bandwidth"    .$csv_suffix;
                print OUT $csv_prefix."ifoperstatus_".$csv_suffix;
            }
        }
    }
}
close OUT;
close LOG if $debug;

exit 0 ;

#=====================================================================================
sub usage {
    print <<EOT
$progname

    - Discover thru SNMP the Network Interfaces for a given device
      and create an output file listing them in a particular format.
      Presently, CSV, (comma separated values), is the supported
      format.

  Switches and defaults:
  
      -c <communitystring>    Defaults to public, this is the SNMP
                              Community String.

      -d                      Switch on Debug mode. If switch is present,
                              debug is turned on. If switch is absent,
                              debug is off. Debug mode outputs the result
                              of the SNMP walk to stdout.

      -h or --help            Prints this usage message.

  IP Addresses and Ranges
  
    - Any command line argument that doesn't begin with a "-", or is an
      argument into a previous "-" option, such as the community string
      of the "-c" option, is evaluated as a possible IP address or as an
      IP address range specifier:

      ###.###.###.###     is a particular IP address.
      
      ###.###.###         is an entire subnet range of 1 thru 254.
      
      ###.###.###.###-### is a partial subnet range.
      
      So,
      
      192.168.1           will cause $progname to scan 192.168.1.1
                          thru 192.168.1.254
      
      192.168.1.1-10      will cause $progname to scan 192.168.1.1
                          thru 192.168.1.10

  Example:

      $progname -d -c notpublic 192.168.1.1-10 192.168.2.1 192.168.3

EOT
;
}
#======================================================================================
exit 0 ;
