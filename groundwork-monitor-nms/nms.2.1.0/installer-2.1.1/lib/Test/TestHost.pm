#!/usr/bin/perl

package TestHost;
use lib qw(../);
use GWInstaller::AL::Host;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
        $pass = 0;
        $host = GWInstaller::AL::Host->new();
        $host->isa(GWInstaller::AL::Host)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_get_hostname{
        $pass = 0;
        $hostname = `hostname`;
        chomp($hostname);
        $host = GWInstaller::AL::Host->new("jersey");
        (($host->get_hostname()) eq $hostname)?($pass = 1):($pass = 0);
        return $pass;
}

## Needs to be updated 
sub test_port_in_use{
        $pass = 0;
        
        unless(-e "/usr/local/groundwork"){
        	print `mkdir /usr/local/groundwork`;
       		print `mkdir /usr/local/groundwork/common/bin`;
        }
        unless(-e "/usr/local/groundwork/common/bin"){
       		print `mkdir /usr/local/groundwork/common/bin`;        	
        }
        $nmapbin = `/usr/bin/which nmap 2>/dev/null`;
        chomp($nmapbin);
        unless($nmapbin){
        	print "Host::port_in_use() will fail unless you install a copy of nmap\n";
        }
        
        $linkcmd = "/bin/ln -s $nmapbin /usr/local/groundwork/common/bin/nmap 2>/dev/null";
        $linkres = `$linkcmd`;
        if($linkres){return 0;}
        $host = GWInstaller::AL::Host->new("localhost");
        $actual = `echo exit | telnet localhost 22 2>/dev/null | grep -c Connected`; 
        chomp($actual);
        $testVal = $host->port_in_use("TCP", 22);
        if($actual == $testVal){$pass = 1;}
        return $pass;
}

