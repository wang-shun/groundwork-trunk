#!/usr/bin/perl

package TestGWMonitor;
use lib qw(../);
use GWInstaller::AL::GWMonitor;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
        $pass = 0;
        $prop = GWInstaller::AL::GWMonitor->new();
        $prop->isa(GWInstaller::AL::GWMonitor)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_get_port{
        $prop = GWInstaller::AL::GWMonitor->new();
        $prop->set_port(4913);
        (($prop->get_port()) == 4913)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_port{
        $prop = GWInstaller::AL::GWMonitor->new();
        return test_get_port();
}

sub test_get_hostname{
        $prop = GWInstaller::AL::GWMonitor->new();
        $prop->set_hostname("localhost");
        (($prop->get_hostname()) eq "localhost")?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_hostname{
        $prop = GWInstaller::AL::GWMonitor->new();
        return test_get_hostname();
}

sub test_get_identifier{
        $prop = GWInstaller::AL::GWMonitor->new();
        $prop->set_identifier("test_identifier");
        (($prop->get_identifier()) eq "test_identifier")?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_identifier{
        $prop = GWInstaller::AL::GWMonitor->new();
        return test_get_identifier();
}

sub test_is_installed{
 	$pass = 0;
	$isThere = `rpm -qi groundwork-monitor-core  | grep -v 'not installed' | wc -l`;
	chomp($isThere);
	$mon = GWInstaller::AL::GWMonitor->new();
	if($mon->is_installed() == $isThere){
		$pass = 1;
	}     
	
    return $pass;
}

1;
