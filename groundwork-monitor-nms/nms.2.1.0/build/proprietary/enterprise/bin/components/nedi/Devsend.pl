#!/usr/local/groundwork/nms/tools/perl/bin/perl
#============================================================================
#
# Program: Devsend.pl
# Programmer: Remo Rickli
#
# -> Send commands to devices via libcli-sshnet <-
#
# DATE	COMMENT
# --------------------------------------------------------------------------
# 14/04/05	initial version
# 24/07/07	 better OS handling & cmd output
# 29/11/07	 Nortel support
# 16/01/08	Simplified approach; now exlusively for new libcli-sshnet.pl
#============================================================================
#use strict;
use Net::Telnet::Cisco;
use vars qw($p $now $guiauth @users %login %opt);

select(STDOUT); $| = 1;

die "6 arguments needed not " . @ARGV . "!\n" if @ARGV != 6;

$p = "/var/nedi";											# Adapt, if necessary
$now = time;
$misc::timeout = 0;											# Dummy to avoid warning

require "$p/inc/libmisc.pl" || die "Can't open libmisc.pl!";
&misc::ReadConf();
require "$p/inc/libcli-netssh.pl" || die "Can't open libcli-netssh.pl!";

my $ip = $ARGV[0];
my $po = $ARGV[1];
my $us = $ARGV[2];
my $pw = $ARGV[3];
my $os = $ARGV[4];
my $cf = $ARGV[5];

if(defined $guiauth and $guiauth =~ /i/){
	$login{$us}{pw} = $pw;
}

#$opt{d} = "1";												# Turn debugging on

open  (CFG, "$cf" );
my @cmd = <CFG>;
close(CFG);
chomp @cmd;

open  (LOG, ">$cf-$ip.log" ) or print " can't write to $cf-$ip.log";

my $session = Net::Telnet::Cisco->new(	Host	=> $ip,
					Port	=> $po,
					Prompt  => '/.+?[#>]\s?(?:\(enable\)\s*)?$/',
					Timeout => $misc::timeout,
					Errmode => "return",
					);
if(defined $session){
	$session->max_buffer_length(8 * 1024 * 1024);							# Increase buffer to 8Mb
	if( &cli::EnableDev($session,$us) ){
		$session->close;
	}else{
		print " (".localtime($now).") ";
		$session->cmd($cli::cmd{$os}{'page'}) if $cli::cmd{$os}{'page'};
		foreach my $c (@cmd){
			print LOG "$c\n";
			print LOG join("", $session->cmd($c) );
			print ".";
			if( $session->errmsg ){
				$session->close;
				close (LOG);
				die " command $c: " . $session->errmsg;
			}
		}
	}
}else{
	$session->close;
	close (LOG);
	print " telnet $ip:$po " . $session->errmsg;
}
