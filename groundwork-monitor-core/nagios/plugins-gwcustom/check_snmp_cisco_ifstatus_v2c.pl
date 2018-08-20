#!/usr/local/groundwork/perl/bin/perl
# CVS: @(#): $Id: check_snmp_cisco_ifstatus,v 1.1 2005/11/07 16:23:16 jamespeel Exp $
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
use Net::SNMP qw(:snmp);
use Getopt::Std;

$script    = "check_snmp_cisco_ifstatus_v2c.pl";
$script_version = "2.1.0.1";

# SNMP options
$version = "2c";
$timeout = 2;

$number_of_interfaces = 0;
$target_interface_index = 0;
$oid_ifphysaddress_octets = 11;

$oid_sysdescr 		= ".1.3.6.1.2.1.1.1.0";
$oid_ifnumber		= ".1.3.6.1.2.1.2.1.0";		# number of interfaces on device
$oid_ifdescr 		= ".1.3.6.1.2.1.2.2.1.2.";	# need to append integer for specific interface
$oid_iftype		= ".1.3.6.1.2.1.2.2.1.3.";	# need to append integer for specific interface
$oid_ifmtu		= ".1.3.6.1.2.1.2.2.1.4.";	# need to append integer for specific interface
$oid_ifspeed		= ".1.3.6.1.2.1.2.2.1.5.";	# need to append integer for specific interface
$oid_ifphysaddress	= ".1.3.6.1.2.1.2.2.1.6.";	# need to append integer for specific interface
$oid_ifadminstatus	= ".1.3.6.1.2.1.2.2.1.7.";	# need to append integer for specific interface
$oid_ifoperstatus	= ".1.3.6.1.2.1.2.2.1.8.";	# need to append integer for specific interface
$oid_iflastchange	= ".1.3.6.1.2.1.2.2.1.9.";	# need to append integer for specific interface
$oid_ifinerrors		= ".1.3.6.1.2.1.2.2.1.14.";	# need to append integer for specific interface
$oid_ifouterrors	= ".1.3.6.1.2.1.2.2.1.20.";	# need to append integer for specific interface
$oid_ifoutqlen		= ".1.3.6.1.2.1.2.2.1.21.";	# need to append integer for specific interface

# Cisco Specific

$oid_locIfIntBitsSec	= "1.3.6.1.4.1.9.2.2.1.1.6.";	# need to append integer for specific interface
$oid_locIfOutBitsSec	= "1.3.6.1.4.1.9.2.2.1.1.8.";	# need to append integer for specific interface

$ifdescr 		= "n/a";
$iftype			= "n/a";
$ifmtu			= "n/a";
$ifspeed		= "n/a";
$ifphysaddress		= "n/a";
$ifadminstatus		= "n/a";
$ifoperstatus		= "n/a";
$iflastchange		= "n/a";
$ifinerrors		= "n/a";
$ifouterrors		= "n/a";
$ifoutqlen		= "n/a";
$ifoutbitssec		= "n/a";
$ifoutbits              = "n/a";

$warning		= 100000000;	# Warning threshold
$critical		= 100000000;	# Critical threshold

$hostname = "192.168.10.21";
#$hostname = "212.113.28.134";
$returnstring = "";

$community = "public"; 		# Default community string
$configfilepath = "/usr/local/groundwork/nagios/etc";


# Do we have enough information?
if (@ARGV < 1) {
     print "Too few arguments\n";
     usage();
}

getopts("h:H:C:i:w:c:");
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
    exit(0);
}
if ($opt_C){
    $community = $opt_C;
}
if ($opt_i){
    $target_interface = $opt_i;
}
if ($opt_w){
    $warning = $opt_w;
}
if ($opt_c){
    $critical = $opt_c;
}
else {
    # print "Using community $community\n";
}


# Create the SNMP session

$oid_sysDescr = ".1.3.6.1.2.1.1.1.0";		# Used to check whether SNMP is actually responding

$version = "snmpv2c";
($s, $e) = Net::SNMP->session(
   -community    =>  $community,
   -hostname     =>  $hostname,
   -version      =>  $version,
   -timeout      =>  $timeout,
   -nonblocking	 =>  1,
);

if (!defined $s) {
  # If we can't connect using SNMPv1 lets try as SNMPv2
  $s->close();
  sleep 0.5;
  $version = "2c";
  ($s, $e) = Net::SNMP->session(
    -community    =>  $community,
    -hostname     =>  $hostname,
    -version      =>  $version,
    -timeout      =>  $timeout,
    -nonblocking  =>  1,
  );
  if (!defined $s) {
    print "Agent not responding, tried SNMP v1 and v2\n";
    exit(2);
  }
}


if (find_match() == 0){
    probe_interface();
}
else {
    $status = 2;
    print "Interface $target_interface not found on device $hostname\n";
    exit $status;
}
    
# Close the session
$s->close();

if($status == 0){
    print "Status is OK - $returnstring\n";
    exit $status;
}
elsif($status == 1){
    print "Status is a Warning Level - $returnstring\n";
    exit $status;
}
elsif($status == 2){
    print "Status is CRITICAL - $returnstring\n";
    exit $status;
}
else{
    print "Plugin error! SNMP status unknown\n";
    exit $status;
}

exit 2;


#################################################
# Finds match for supplied interface name
#################################################

sub find_match {

    if (!defined($s->get_request( -varbindlist => [ $oid_ifnumber ], -callback => [ \&get_callback, $hostname ]))) {
        if (!defined($s->get_request( -varbindlist => [ $oid_sysdescr ], -callback => [ \&get_callback, $hostname ]))) {
            print "Status is a Warning Level - SNMP agent not responding\n";
            exit 2;
        }
        else {
            print "Status is a Warning Level - SNMP OID does not exist\n";
            exit 2;
        }
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $number_of_interfaces = $s->var_bind_list()->{$_};
            if ($number_of_interfaces == 0){
                return 1;
            }
        }
    }
    
    my %table;
    my $result= $s->get_bulk_request(
        	-varbindlist => [ $oid_ifdescr ],
        	-callback => [ \&table_callback, \%table ],
        	-maxrepetitions => 10,
   );
   if (!defined $result) {
      printf "Error: %s\n", $s->error();
      exit 1;
   }  
   snmp_dispatcher();
   for my $oid (oid_lex_sort(keys %table)) {
      if (!oid_base_match($oid_ifphysaddress, $oid)) {
         if (lc($table{$oid}) eq lc($target_interface)) {
            my @myoctets = split(/\./,$oid); 
            $target_interface_index=$myoctets[$oid_ifphysaddress_octets];
            last if (lc($table{$oid}) eq lc($target_interface));
         }
      }
   }
    if ($target_interface_index == 0){
        return 1;
    }
    else {
        return 0;
    }
}

####################################################################
# Gathers data about target interface                              #
####################################################################


sub probe_interface {

    $oid_temp = $oid_ifdescr . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifdescr = $s->var_bind_list()->{$_};
        }
    }
    ############################
    
    $oid_temp = $oid_iftype . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $iftype = $s->var_bind_list()->{$_};
        }
    }
    ############################
    
     
    $oid_temp = $oid_ifmtu . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifmtu = $s->var_bind_list()->{$_};
        }
    }
    ############################
        
    $oid_temp = $oid_ifspeed . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifspeed = $s->var_bind_list()->{$_};
        }
    }
    ############################
                
    $oid_temp = $oid_ifadminstatus . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifadminstatus = $s->var_bind_list()->{$_};
        }
    }
    ############################
        
    $oid_temp = $oid_ifoperstatus . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifoperstatus = $s->var_bind_list()->{$_};
        }
    }
    ############################
        
    $oid_temp = $oid_iflastchange . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $iflastchange = $s->var_bind_list()->{$_};
        }
    }
    ############################
        
    $oid_temp = $oid_ifinerrors . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifinerrors = $s->var_bind_list()->{$_};
        }
    }
    ############################
        
    $oid_temp = $oid_ifouterrors . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifouterrors = $s->var_bind_list()->{$_};
        }
    }
    ############################

    $oid_temp = $oid_ifoutqlen . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifoutqlen = $s->var_bind_list()->{$_};
            if ($ifoutqlen eq "noSuchInstance") {
               $ifoutqlen="n/a";
            }
        }
    }
    ############################

    $oid_temp = $oid_locIfOutBitsSec . $target_interface_index;    
    if (!defined($s->get_request( -varbindlist => [ $oid_temp ], -callback => [ \&get_callback, $hostname ]))) {
      $ifoutbitssec = -1;
    }
    else {
        snmp_dispatcher();
        foreach ($s->var_bind_names()) {
            $ifoutbitssec = $s->var_bind_list()->{$_};
        }
    }
    ############################
    
    
        
    $errorstring = "";
    
    # Sets warning / critical levels if interface down
    
    if ($ifadminstatus eq "1"){
    }
    else {
        $status = 1;
        $errorstring = "INTERFACE ADMINISTRATIVELY DOWN:";
    }

        
    if ($ifoperstatus eq "1"){
    }
    else {
        $status = 2;
        $errorstring = "INTERFACE DOWN:";
    }
     
    # Sets warning / critical levels if ifoutbitssec exceeds threshold

    if ($status < 1){
      # Don't bother is status is down
      if ($ifoutbitssec > $critical){
        $errorstring = "HIGH THROUGHPUT:";
        $status = 2;
      }
      elsif ($ifoutbitssec > $warning) {
        $errorstring = "HIGH THROUGHPUT:";
        $status = 1;
      }
    }
    
    
    
     # Mangles interface speed 
     
     my $mbps = $ifspeed / 1000000;
     if ($mbps < 1){
         $ifspeed = $ifspeed / 1000;
         $ifspeed = "$ifspeed Kbps";
     }
     else {
         $ifspeed = "$mbps Mbps";
     } 
     
     # Mangles IfOutBitsSec stat to bits / kbits 
     if ($ifoutbitssec > 0){
         my $mbps = $ifoutbitssec / 1000000;
         $ifoutbits=$mbps;
#         if ($mbps < 1){
#             $ifoutbitssec = $ifoutbitssec / 1000;
#             $ifoutbitssec = "$ifoutbitssec Kbps";
#         }
#         else {
             $ifoutbitssec = "$mbps Mbps";
#         } 
     }
     elsif ($ifoutbitssec < 0){
         $ifoutbitssec = "N/A";
     }
     else {
         $ifoutbitssec = "0 bps";
     }
     
     
     # Returns string relating to interface media     
     my $iftype_string = return_interfacetype($iftype);

    
    if ($status == 0){    
        $temp = sprintf "$ifdescr ($iftype_string) - Current: $ifoutbitssec, Limit: $ifspeed, MTU: $ifmtu, Last change: $iflastchange, STATS:(in errors: $ifinerrors, out errors: $ifouterrors, queue length: $ifoutqlen)|Mbps=$ifoutbits;%d;%d",$warning/1000000,$critical/1000000;
        append($temp);
    }
    else {
        $temp = sprintf "$errorstring $ifdescr ($iftype_string) - Current: $ifoutbitssec, Limit: $ifspeed, MTU: $ifmtu, Last change: $iflastchange, STATS:(in errors: $ifinerrors, out errors: $ifouterrors, queue length: $ifoutqlen)|Mbps=$ifoutbits;%d;%d",$warning/1000000,$critical/1000000;
        append($temp);    
    }
}

####################################################################
# help and usage information                                       #
####################################################################

sub usage {
    print << "USAGE";
--------------------------------------------------------------------
$script v$script_version

Monitors status of specific Ethernet interface. 

Usage: $script -H <hostname> -c <community> [...]

Options: -H 	Hostname or IP address
         -C 	Community (default is public)
	 -i 	Target interface name 
		Eg: Serial0/0, FastEthernet0/12
	 -w	Warning threshold - Out bits / sec
	 -c	Critical threshold - Out bits / sec

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

####################################################################
# Returns the interface type for given IANA metric                 #
####################################################################

sub return_interfacetype {
    my $iftype_int = @_[0];
    my @iana = ();
    my $returnstring = $iftype_int;
    
    $iana[0] = "";
    $iana[1] = "other";
    $iana[2] = "regular1822";
    $iana[3] = "hdh1822";
    $iana[4] = "ddnX25";
    $iana[5] = "rfc877x25";
    $iana[6] = "ethernet";
    $iana[7] = "";
    $iana[8] = "TokenBus";
    $iana[9] = "TokenRing";
    $iana[10] = "iso88026Man";
    $iana[11] = "starLan";
    $iana[12] = "proteon10Mbit";
    $iana[13] = "proteon80Mbit";
    $iana[14] = "hyperchannel";
    $iana[15] = "fddi";
    $iana[16] = "lapb";
    $iana[17] = "sdlc";
    $iana[18] = "DSL";
    $iana[19] = "E1";
    $iana[20] = "basicISDN";
    $iana[21] = "primaryISDN";
    $iana[22] = "propritory PointToPointSerial";
    $iana[23] = "PPP";
    $iana[24] = "softwareLoopback";
    $iana[25] = "CLNP over IP ";
    $iana[26] = "ethernet3Mbit";
    $iana[27] = "XNS over IP";
    $iana[28] = "SLIP";
    $iana[29] = "ultra";
    $iana[30] = "DS3";
    $iana[31] = "SMDS";
    $iana[32] = "frameRelay DTE";
    $iana[33] = "RS232";
    $iana[34] = "Parallel port";
    $iana[35] = "arcnet";
    $iana[36] = "arcnetPlus";
    $iana[37] = "ATM";
    $iana[38] = "miox25";
    $iana[39] = "SONET / SDH ";
    $iana[40] = "X25PLE";
    $iana[41] = "iso88022llc";
    $iana[42] = "localTalk";
    $iana[43] = "SMDSDxi";
    $iana[44] = "frameRelayService";
    $iana[45] = "v35";
    $iana[46] = "hssi";
    $iana[47] = "hippi";
    $iana[48] = "Generic MODEM";
    $iana[49] = "";
    $iana[50] = "sonetPath";
    $iana[51] = "sonetVT";
    $iana[52] = "SMDSIcip";
    $iana[53] = "propVirtual";
    $iana[54] = "propMultiplexor";
    $iana[55] = "ieee80212 100BaseVG";
    $iana[56] = "fibreChannel";
    $iana[57] = "HIPPI interface";
    $iana[58] = "";
    $iana[59] = "ATM Emulated LAN for 802.3";
    $iana[60] = "ATM Emulated LAN for 802.5";
    $iana[61] = "ATM Emulated circuit          ";
    $iana[62] = "";
    $iana[63] = "ISDN / X.25           ";
    $iana[64] = "CCITT V.11/X.21             ";
    $iana[65] = "CCITT V.36                  ";
    $iana[66] = "CCITT G703 at 64Kbps";
    $iana[67] = "G703 at 2Mb";
    $iana[68] = "SNA QLLC ";
    $iana[69] = "";
    $iana[70] = "channel  ";
    $iana[71] = "ieee80211 radio spread spectrum       ";
    $iana[72] = "IBM System 360/370 OEMI Channel";
    $iana[73] = "IBM Enterprise Systems Connection";
    $iana[74] = "Data Link Switching";
    $iana[75] = "ISDN S/T interface";
    $iana[76] = "ISDN U interface";
    $iana[77] = "Link Access Protocol D";
    $iana[78] = "IP Switching Objects";
    $iana[79] = "Remote Source Route Bridging";
    $iana[80] = "ATM Logical Port";
    $iana[81] = "Digital Signal Level 0";
    $iana[82] = "ds0 Bundle (group of ds0s on the same ds1)";
    $iana[83] = "Bisynchronous Protocol";
    $iana[84] = "Asynchronous Protocol";
    $iana[85] = "Combat Net Radio";
    $iana[86] = "ISO 802.5r DTR";
    $iana[87] = "Ext Pos Loc Report Sys";
    $iana[88] = "Appletalk Remote Access Protocol";
    $iana[89] = "Proprietary Connectionless Protocol";
    $iana[90] = "CCITT-ITU X.29 PAD Protocol";
    $iana[91] = "CCITT-ITU X.3 PAD Facility";
    $iana[92] = "frameRelay MPI";
    $iana[93] = "CCITT-ITU X213";
    $iana[94] = "ADSL";
    $iana[95] = "Rate-Adapt. DSL";
    $iana[96] = "SDSL";
    $iana[97] = "Very High-Speed DSL";
    $iana[98] = "ISO 802.5 CRFP";
    $iana[99] = "Myricom Myrinet";
    $iana[100] = "voiceEM voice recEive and transMit";
    $iana[101] = "voiceFXO voice Foreign Exchange Office";
    $iana[102] = "voiceFXS voice Foreign Exchange Station";
    $iana[103] = "voiceEncap voice encapsulation";
    $iana[104] = "VoIP";
    $iana[105] = "ATM DXI";
    $iana[106] = "ATM FUNI";
    $iana[107] = "ATM IMA";
    $iana[108] = "PPP Multilink Bundle";
    $iana[109] = "IBM ipOverCdlc";
    $iana[110] = "IBM Common Link Access to Workstn";
    $iana[111] = "IBM stackToStack";
    $iana[112] = "IBM VIPA";
    $iana[113] = "IBM multi-protocol channel support";
    $iana[114] = "IBM IP over ATM";
    $iana[115] = "ISO 802.5j Fiber Token Ring";
    $iana[116] = "IBM twinaxial data link control";
    $iana[117] = "";
    $iana[118] = "HDLC";
    $iana[119] = "LAP F";
    $iana[120] = "V.37";
    $iana[121] = "X25 Multi-Link Protocol";
    $iana[122] = "X25 Hunt Group";
    $iana[123] = "Transp HDLC";
    $iana[124] = "Interleave channel";
    $iana[125] = "Fast channel";
    $iana[126] = "IP (for APPN HPR in IP networks)";
    $iana[127] = "CATV Mac Layer";
    $iana[128] = "CATV Downstream interface";
    $iana[129] = "CATV Upstream interface";
    $iana[130] = "Avalon Parallel Processor";
    $iana[131] = "Encapsulation interface";
    $iana[132] = "Coffee pot (no, really)";
    $iana[133] = "Circuit Emulation Service";
    $iana[134] = "ATM Sub Interface";
    $iana[135] = "Layer 2 Virtual LAN using 802.1Q";
    $iana[136] = "Layer 3 Virtual LAN using IP";
    $iana[137] = "Layer 3 Virtual LAN using IPX";
    $iana[138] = "IP over Power Lines";
    $iana[139] = "Multimedia Mail over IP";
    $iana[140] = "Dynamic Synchronous Transfer Mode";
    $iana[141] = "Data Communications Network";
    $iana[142] = "IP Forwarding Interface";
    $iana[143] = "Multi-rate Symmetric DSL";
    $iana[144] = "IEEE1394 High Performance Serial Bus";
    $iana[145] = "HIPPI-6400 ";
    $iana[146] = "DVB-RCC MAC Layer";
    $iana[147] = "DVB-RCC Downstream Channel";
    $iana[148] = "DVB-RCC Upstream Channel";
    $iana[149] = "ATM Virtual Interface";
    $iana[150] = "MPLS Tunnel Virtual Interface";
    $iana[151] = "Spatial Reuse Protocol";
    $iana[152] = "Voice Over ATM";
    $iana[153] = "Voice Over Frame Relay ";
    $iana[154] = "Digital Subscriber Loop over ISDN";
    $iana[155] = "Avici Composite Link Interface";
    $iana[156] = "SS7 Signaling Link ";
    $iana[157] = "Prop. P2P wireless interface";
    $iana[158] = "Frame Forward Interface";
    $iana[159] = "Multiprotocol over ATM AAL5";
    $iana[160] = "USB Interface";
    $iana[161] = "IEEE 802.3ad Link Aggregate";
    $iana[162] = "BGP Policy Accounting";
    $iana[163] = "FRF .16 Multilink Frame Relay ";
    $iana[164] = "H323 Gatekeeper";
    $iana[165] = "H323 Voice and Video Proxy";
    $iana[166] = "MPLS";
    $iana[167] = "Multi-frequency signaling link";
    $iana[168] = "High Bit-Rate DSL - 2nd generation";
    $iana[169] = "Multirate HDSL2";
    $iana[170] = "Facility Data Link 4Kbps on a DS1";
    $iana[171] = "Packet over SONET/SDH Interface";
    $iana[172] = "DVB-ASI Input";
    $iana[173] = "DVB-ASI Output ";
    $iana[174] = "Power Line Communtications";
    $iana[175] = "Non Facility Associated Signaling";
    $iana[176] = "TR008";
    $iana[177] = "Remote Digital Terminal";
    $iana[178] = "Integrated Digital Terminal";
    $iana[179] = "ISUP";
    $iana[180] = "Cisco proprietary MAC layer";
    $iana[181] = "Cisco proprietary Downstream";
    $iana[182] = "Cisco proprietary Upstream";
    $iana[183] = "HIPERLAN Type 2 Radio Interface";
    $iana[184] = "PropBroadbandWirelessAccesspt2multipt";
    $iana[185] = "SONET Overhead Channel";
    $iana[186] = "Digital Wrapper";
    $iana[187] = "ATM adaptation layer 2";
    $iana[188] = "MAC layer over radio links";
    $iana[189] = "ATM over radio links   ";
    $iana[190] = "Inter Machine Trunks";
    $iana[191] = "Multiple Virtual Lines DSL";
    $iana[192] = "Long Reach DSL";
    $iana[193] = "Frame Relay DLCI End Point";
    $iana[194] = "ATM VCI End Point";
    $iana[195] = "Optical Channel";
    $iana[196] = "Optical Transport";
    $iana[197] = "Proprietary ATM       ";
    $iana[198] = "Voice Over Cable Interface";
    $iana[199] = "Infiniband";
    $iana[200] = "TE Link";
    $iana[201] = "Q.2931";
    $iana[202] = "Virtual Trunk Group";
    $iana[203] = "SIP Trunk Group";
    $iana[204] = "SIP Signaling   ";
    $iana[205] = "CATV Upstream Channel";
    $iana[206] = "Acorn Econet";
    $iana[207] = "FSAN 155Mb Symetrical PON interface";
    $iana[208] = "FSAN622Mb Symetrical PON interface";
    $iana[209] = "Transparent bridge interface";
    $iana[210] = "Interface common to multiple lines";
    $iana[211] = "voice E&M Feature Group D";
    $iana[212] = "voice FGD Exchange Access North American";
    $iana[213] = "voice Direct Inward Dialing";
    $iana[214] = "MPEG transport interface";
    $iana[215] = "6to4 interface";
    $iana[216] = "GTP (GPRS Tunneling Protocol)";
    $iana[217] = "Paradyne EtherLoop 1";
    $iana[218] = "Paradyne EtherLoop 2";
    $iana[219] = "Optical Channel Group";
    $iana[220] = "HomePNA ITU-T G.989";
    $iana[221] = "Generic Framing Procedure (GFP)";
    $iana[222] = "Layer 2 Virtual LAN using Cisco ISL";
    $iana[223] = "Acteleis proprietary MetaLOOP High Speed Link ";
    $iana[224] = "FCIP Link ";
    $iana[225] = "Resilient Packet Ring Interface Type";
    $iana[226] = "RF Qam Interface";
    $iana[227] = "Link Management Protocol";
    
    
    if ($iftype_int > 227){
        $returnstring = "$iftype_int";
        }
    else{ 
        $returnstring = $iana[$iftype_int];
    }
    
    return($returnstring);
    
}

sub get_callback
   {
      my ($session, $location) = @_;

      my $result = $session->var_bind_list();

      if (!defined $result) {
         printf "ERROR: Get request failed for host '%s': %s.\n",
                $session->hostname(), $session->error();
         return;
      }
      return;
   }

sub table_callback
   {
      my ($session, $table) = @_;

      my $list = $session->var_bind_list();

      if (!defined $list) {
         printf "ERROR: %s\n", $session->error();
         return;
      }

      # Loop through each of the OIDs in the response and assign
      # the key/value pairs to the reference that was passed with
      # the callback.  Make sure that we are still in the table
      # before assigning the key/values.

      my @names = $session->var_bind_names();
      my $next  = undef;

      while (@names) {
         $next = shift @names;
         if (!oid_base_match($oid_ifdescr, $next)) {
            return; # Table is done.
         }
         $table->{$next} = $list->{$next};
      }

      # Table is not done, send another request, starting at the last
      # OBJECT IDENTIFIER in the response.  No need to include the
      # calback argument, the same callback that was specified for the
      # original request will be used.

      my $result = $session->get_bulk_request(
         -varbindlist    => [ $next ],
         -maxrepetitions => 10,
      );

      if (!defined $result) {
         printf "ERROR: %s.\n", $session->error();
      }

      return;
   }

