#!/usr/bin/perl

package TestEventBroker;
use lib qw(../);
use GWInstaller::AL::EventBroker;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
	$pass = 0;
	$prop = GWInstaller::AL::EventBroker->new();
	$prop->isa(GWInstaller::AL::EventBroker)?($pass = 1):($pass = 0);
	return $pass;
}

sub test_is_functional{
	$prop = GWInstaller::AL::EventBroker->new();
	($prop->is_functional("localhost"))?($pass = 1):($pass = 0);
	return $pass;
}
1;
