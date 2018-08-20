#!/usr/bin/perl

package TestFoundation;
use lib qw(../);
use GWInstaller::AL::Foundation;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
	$pass = 0;
	$prop = GWInstaller::AL::Foundation->new();
	$prop->isa(GWInstaller::AL::Foundation)?($pass = 1):($pass = 0);
	return $pass;
}

sub test_is_functional{
	$pass = 0;
	$host = GWInstaller::AL::Host->new("localhost");
	$actual = $host->port_in_use("TCP",4913);
	$prop = GWInstaller::AL::Foundation->new();
	$isThere = $prop->is_functional("localhost");
	if($isThere == $actual){
		$pass = 1;
	}
	return $pass;
}
1;