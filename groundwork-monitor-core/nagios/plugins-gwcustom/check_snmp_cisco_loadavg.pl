#!/usr/local/groundwork/bin/perl
# CVS: @(#): $Id: check_snmp_cisco_loadavg,v 1.1 2005/11/07 16:23:16 jamespeel Exp $
#
# AUTHORS:
#	Copyright (C) 2004, 2005 Altinity Limited
#
#    This file is part of Opsview
#
#    Opsview is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    Opsview is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with Opsview; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#


use lib qw ( /usr/local/groundwork/nagios/libexec );
use Net::SNMP;
use Getopt::Std;

$script    = "check_snmp_cisco_loadavg";
$script_version = "2.1.0";

$metric = 1;
$oid_sysDescr = ".1.3.6.1.2.1.1.1.0";
$oid_cpu5min = "1.3.6.1.4.1.9.2.1.58.0";
$oid_tempState = "1.3.6.1.4.1.9.9.13.1.3.1.6.1";

$ipaddress = "192.168.1.1";		# Default IP address
$version = "1";				# SNMP version
$community = "public";
$timeout = 2;				# Response timeout (seconds)
$warning = 80;
$critical = 90;
$status = 0;
$returnstring = "";

$configfilepath = "/usr/local/groundwork/nagios/etc";

# Do we have enough information?
if (@ARGV < 1) {
     print "Too few arguments\n";
     usage();
}

getopts("h:H:C:w:c:");
if ($opt_h){
    usage();
    exit(0);
}
if ($opt_H){
    $hostname = $opt_H;
    # print "Hostname $opt_H\n";
}
else {
    print "No hostname specified\n";
    usage();
}
if ($opt_C){
    $community = $opt_C;
    # print "Using community $opt_C\n";
}
else {
    # print "Using community $community\n";
}
if ($opt_w){
    $warning = $opt_w;
    # print "Warning threshold: $opt_w%\n";
}
if ($opt_c){
    $critical = $opt_c;
    # print "Critical threshold: $opt_c%\n";
}


# Create the SNMP session
my ($s, $e) = Net::SNMP->session(
     -community  =>  $community,
     -hostname   =>  $hostname,
     -version    =>  $version,
     -timeout    =>  $timeout,
);

main();

# Close the session
$s->close();

if ($returnstring eq ""){
    $status = 3;
}

if ($status == 0){
    print "Status is OK - $returnstring\n";
    # print "$returnstring\n";
}
elsif ($status == 1){
    print "Status is a WARNING level - $returnstring\n";
}
elsif ($status == 2){
    print "Status is CRITICAL - $returnstring\n";
}
else{
    print "Status is UNKNOWN\n";
}
 
exit $status;

####################################################################
# This is where we gather data via SNMP and return results         #
####################################################################

sub main {

    if (!defined($s->get_request($oid_cpu5min))) {
        if (!defined($s->get_request($oid_sysDescr))) {
            $returnstring = "SNMP agent not responding";
            $status = 1;
            return 1;
        }
        else {
            $returnstring = "SNMP OID does not exist";
            $status = 1;
            return 1;
        }
    }
     foreach ($s->var_bind_names()) {
         $cpu5min = $s->var_bind_list()->{$_};
    }

    if (!defined($s->get_request($oid_tempState))) {
        if (!defined($s->get_request($oid_sysDescr))) {
            $returnstring = "SNMP agent not responding";
            $status = 1;
            return 1;
        }
        else {
            $tempstate = "5";
        }
    }
     foreach ($s->var_bind_names()) {
         $tempstate = $s->var_bind_list()->{$_};
    }
    
    if ($cpu5min >= $warning){
        $status = 1;
    }

    if ($tempstate eq "1"){
        $temperature = "normal";
    }
    elsif ($tempstate eq "2"){
        $temperature = "warning";
        $status = 1;
    }
    elsif ($tempstate eq "3"){
        $temperature = "critical";
        $status = 2;
    }
    elsif ($tempstate eq "4"){
        $temperature = "shutdown";
        $status = 2;
    }
    else {
        $temperature = "";
    } 
    
    if ($cpu5min >= $critical){
        $status = 2;
    }
    
    if ($temperature eq ""){
        $temp = "CPU load average (5 min): $cpu5min %";
    
    }
    else {
        $temp = "CPU load average (5 min): $cpu5min %, temperature $temperature";
    }
    
    
    append($temp);    
}

####################################################################
# help and usage information                                       #
####################################################################

sub usage {
    print << "USAGE";
--------------------------------------------------------------------	 
$script v$script_version

Returns the 5 minute CPU load average in %

Usage: $script -H <hostname> -c <community> [...]
Options: -H 		Hostname or IP address
         -C 		Community (default is public)
         -w 		Warning threshold (as %)
         -c 		Critical threshold (as %)
	 
--------------------------------------------------------------------	 
Copyright 2004 Altinity Limited	 
	 
This program is free software; you can redistribute it or modify
it under the terms of the GNU General Public License
--------------------------------------------------------------------

USAGE
     exit 1;
}

####################################################################
# Appends string to existing $returnstring                         #
####################################################################

sub append {
    my $appendstring =    @_[0];    
    $returnstring = "$returnstring$appendstring";
}

