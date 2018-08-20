#!/usr/local/groundwork/nms/tools/perl/bin/perl
#============================================================================
# Program: moni.pl
# Programmer: Remo Rickli
#
# DATE     COMMENT
# ------------------------------------------------------------------------
# 15/06/05	initial version
# 17/05/06	non-blocking uptime queries and optimized dependency resolution
# 17/01/07	updated path handling
# 04/06/07	improved stability
# 04/09/07	added test function
#============================================================================
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
#============================================================================
# Visit http://nedi.sourceforge.net for more information.
#============================================================================

use strict;
use Getopt::Std;
use Net::SNMP qw(snmp_dispatcher ticks_to_time);

use vars qw($p $now $maxi);										# Global since access from functions is required
use vars qw(%opt %dev %usr %mon %depdevs %depdown %depcount); 

getopts('DtvV',\%opt) or &Help();

$p = $0;
$p =~ s/(.*)\/(.*)/$1/;
if($0 eq $p){$p = "."};
require "$p/inc/libmisc.pl";										# Include required libraries
&misc::ReadConf();
require "$p/inc/libdb-" . lc($misc::backend) . ".pl" || die "Backend error ($misc::backend)!";
require "$p/inc/libmon.pl";

if ($opt{t}) {												# Creates incident and bails
	$now = time;
	if( ! &db::Insert('messages','level,time,source,info',"\"200\",\"$now\",\"Test\",\"moni.pl test message\"") ){
		die "DB error messages!\n";
	}
	if( ! &db::Insert('incidents','level,device,deps,firstseen,lastseen,who,time,category,comment',"\"10\",\"Test\",\"0\",\"$now\",\"".($now+$misc::pause)."\",\"\",\"0\",\"1\",\"\"") ){
		die "DB error incidents!\n";
	}
	my $nsms = &mon::SendSMS("moni.pl test SMS");
	my $nmail = &mon::SendMail("Moni Test","moni.pl test mail");
	die "1 Message, 1 Incident, $nsms SMS and $nmail mail(s) generated...\n";
}elsif ($opt{D}) {											# Daemonize or...
	print "moni.pl daemonizing\n";
	&misc::Daemonize;
}else{
	select(STDOUT); $| = 1;										# ...disable buffering.
}

while(1) {
	$now = time;
	print "Scan:\t" . localtime($now) . "\n" if ($opt{v} or $opt{V});
	undef (%dev);											# Avoid memory leaking
	undef (%mon);
	&db::ReadDev();
	&db::ReadMon();

	$maxi  = 100;											# maximum iterations to prevent potential loops upon resolving misconfigured dependencies
	%depdown  = ();											# dead dependencies are set to 1
	%depdevs  = ();											# list of dependant devices
	%depcount = ();											#  count of dependants
	
	print "==================================================================================\n" if ($opt{v}  or $opt{V});

	foreach my $d (keys %mon){
		if(defined $dev{$d}{cm}){								# Query only if device has a community set
			if($mon{$d}{dp} ne 'none'){							# Does it have a dependency configured?
				if(exists $dev{$mon{$d}{dp}}){						# And does it  exist in devices?
					push (@{$depdevs{$mon{$d}{dp}}},$d);				# Add it to dependency tree
				}else{
					my %set = ();
					my %mat = ();
					$set{'depend'} = 'none';
					$mat{'device'} = $d;
					if( ! &db::Update('monitoring',\%set,\%mat) ){			# Remove dependency and notify
						die "DB error monitoring!\n";
					}
					if( ! &db::Insert('messages','level,time,source,info',"\"50\",\"$now\",\"$d\",\"Non existant dependency $mon{$d}{dp} removed.\"") ){
						die "DB error messages!\n";
					}
				}
			}
			my ($session, $error) = Net::SNMP->session(					# Get uptime
						-hostname    => $main::dev{$d}{ip},
						-community   => $main::dev{$d}{cm},
						-nonblocking => 0x1,   					# Create non-blocking objects
						-timeout     => $misc::timeout,
						-retries     => 1,
						-version     => $main::dev{$d}{sp},
						-translate   => [
						-timeticks => 0x0					# Turn off so sysUpTime is numeric
						]  
			);
			if (!defined($session)) {
				printf("ERROR: %s.\n", $error);
				exit 1;
			}
			$session->get_request(	-varbindlist => ['1.3.6.1.2.1.1.3.0'],
						-callback    => [\&ProcessUpt, $d]
			);
		}
	}
	snmp_dispatcher();										# Shoots out the queries above
	print "----------------------------------------------------------------------------------\n" if ($opt{v}  or $opt{V});
	print "Dependant Count:\n" if ($opt{v}  or $opt{V});
	foreach my $d (keys %depdevs){									# Count dependencies to optimize processing
		$depcount{$d} = &CountDep($d,0);
		print "$d = $depcount{$d}\n" if $opt{v};
		print " = $depcount{$d}\n" if $opt{V};
	}
	print "Optimized Processing:\n" if ($opt{v}  or $opt{V});
	foreach my $d (sort { $depcount{$b} <=> $depcount{$a} } keys %mon){				# Now let's have a look at the answers
		print "$d\t" if $opt{V};
		if($mon{$d}{nu}){									# Did we get an uptime?
			print "Up " if $opt{V};
			if($depdown{$d}){								# Wrong configuration, notify user
				print ", but dep is down! " if $opt{V};
				if( ! &db::Insert('messages','level,time,source,info',"\"50\",\"$now\",\"$d\",\"Is up even though a parent dependency is down?\"") ){
					die "DB error messages!\n";
				}
				&MarkDep($d,0,0);
			}
			print "o" if $opt{v};
			if($mon{$d}{st} >= $misc::thres){
				my $lchk = localtime($mon{$d}{lc});
				print "Recovered! " if $opt{V};
				if( ! &db::Insert('messages','level,time,source,info',"\"50\",\"$now\",\"$d\",\"Recovered (didn't respond since $lchk)\"") ){
					die "DB error messages!\n";
				}
				if( $mon{$d}{ml}){&mon::SendMail("Moni Recovery","Device $d Recovered!")}
				my %set = ();
				my %mat = ();
				$set{'lastseen'} = $now;
				$mat{'device'}   = $d;
				$mat{'lastseen'} = "0";
				if( ! &db::Update('incidents',\%set,\%mat) ){
					die "DB error incidents!\n";
				}
			}
			$mon{$d}{ok}++;
			my %set = ();
			my %mat = ();
			$set{'status'}  = '0';
			$set{'lastchk'} = $now;
			$set{'uptime'}  = $mon{$d}{nu};
			$set{'ok'}      = $mon{$d}{ok};
			$mat{'device'}  = $d;
			if( ! &db::Update('monitoring',\%set,\%mat) ){
				die "DB error monitoring!\n";
			}
			if($mon{$d}{ut} > $mon{$d}{nu}){
				print "Rebooted! " if $opt{V};
				if( ! &db::Insert('messages','level,time,source,info',"\"150\",\"$now\",\"$d\",\"Rebooted (was up for ". ticks_to_time($mon{$d}{ut}) .")!\"") ){
					die "DB error messages!\n";
				}
				if( $mon{$d}{ml}){&mon::SendMail("Moni Reboot Alert!","Device $d Rebooted!")}
			}
		}else{
			print "No answer, affects: " if $opt{V};
			&MarkDep($d,1,0);
			print "x" if $opt{v};
			$mon{$d}{st}++;
			$mon{$d}{lt}++;
			my %set = ();
			my %mat = ();
			$set{'status'}  = $mon{$d}{st};
			$set{'lost'}    = $mon{$d}{lt};
			$set{'lastchk'} = $now;
			$mat{'device'}  = $d;
			if( ! &db::Update('monitoring',\%set,\%mat) ){
				die "DB error monitoring!\n";
			}
			if(!$depdown{$d} and $mon{$d}{st} == $misc::thres){	
				print "For $mon{$d}{st} times! " if $opt{V};
				my $lvl = 200;
				my $cr  = "";
				if( $dev{$d}{lo} =~ /$misc::redbuild/){$lvl = 250;$cr = "Emergency"}
				my $downmsg = "Down!";
				if($depcount{$d}){
					$downmsg = "Down, affecting $depcount{$d} devices!!";
				}else{
					$depcount{$d} = 0;						# Prevents potential DB error
				}
				if( ! &db::Insert('messages','level,time,source,info',"\"$lvl\",\"$now\",\"$d\",\"$downmsg (no answer for $misc::thres times) \"") ){
					die "DB error messages!\n";
				}
				if( ! &db::Insert('incidents','level,device,deps,firstseen,lastseen,who,time,category,comment',"\"$lvl\",\"$d\",\"$depcount{$d}\",\"$now\",\"0\",\"\",\"0\",\"1\",\"\"") ){
					die "DB error incidents!\n";
				}
				if( $mon{$d}{ss}){&mon::SendSMS("$now: $d is down!")}
				if( $mon{$d}{ml}){&mon::SendMail("Moni $cr Alert!","Device $d is down!")}
			}

		}
		print "\n" if $opt{V};
	}
	print "\n==================================================================================\n" if ($opt{v}  or $opt{V});
	sleep($misc::pause);
}

#===================================================================
# Callback function for non blocking SNMP uptime query to process uptimes
#===================================================================
sub ProcessUpt{
	my ($session, $d) = @_;
	print "$d\t" if ($opt{v}  or $opt{V});
	if (!defined($session->var_bind_list)) {
		print "down!" if $opt{v};
		print "down, affects:" if $opt{V};
		&MarkDep($d,1,0);
		$mon{$d}{nu} = 0;
	} else {
		my $uptime = $session->var_bind_list->{'1.3.6.1.2.1.1.3.0'};
		print ticks_to_time($uptime) if ($opt{v}  or $opt{V});
		$mon{$d}{nu} = $uptime;
	}
	print "\n" if ($opt{v}  or $opt{V});
	$session->error_status;
}

#===================================================================
# Mark dependencies
#===================================================================
sub MarkDep {
	if($_[2] < $maxi and exists $depdevs{$_[0]} ){
		foreach my $d (@{$depdevs{$_[0]}}){
			print " $d" if $opt{V};
			$depdown{$d} = $_[1];
			&MarkDep($d,$_[1],$_[2]+1);
		}
	}
}

#===================================================================
# Recursively count dependants
#===================================================================
sub CountDep {	
	if($_[1] < $maxi and exists $depdevs{$_[0]} ){
		my $c = scalar @{$depdevs{$_[0]}};
		print "$_[1]:$_[0]+$c " if $opt{V};
		foreach my $d (@{$depdevs{$_[0]}}){
			$c += &CountDep($d,$_[1]+1);
		}
		return $c;
	}else{
		return 0;
	}
}

#===================================================================
# Display some help
#===================================================================
sub Help {
	print "\n";
	print "usage: moni.pl <Option(s)>\n\n";
	print "---------------------------------------------------------------------------\n";
	print "Options:\n";
	print "-t		test (creates mail(, SMS), message and incident)\n";
	print "-v/V		verbose/r output\n";
	print "-D		daemonize moni.pl\n\n";
	die "Moni 1.0 4.Sep 2007\n";
}
