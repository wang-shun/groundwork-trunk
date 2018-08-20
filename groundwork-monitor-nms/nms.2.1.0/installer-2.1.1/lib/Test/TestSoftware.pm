#!/usr/bin/perl

package TestSoftware;
use lib qw(../);
use GWInstaller::AL::Software;
use GWInstaller::AL::Database;

use GWTest::GWTest;

@ISA = qw(GWTest);
 
sub test_init{
	$pass = 0;
	$db = GWInstaller::AL::Software->new();
	$db->isa(GWInstaller::AL::Software)?($pass = 1):($pass = 0);
 
	
	return $pass;
}


sub test_get_host{
	$self = shift;
	return $self->testGet("GWInstaller::AL::Software","host");
}


sub test_set_host{
	$self = shift;
	return $self->testSet("GWInstaller::AL::Software","host");
}


sub test_get_port{
	$self = shift;
	return $self->testGet("GWInstaller::AL::Software","port");
}

sub test_set_port{ 
	$self = shift;
	return $self->testSet("GWInstaller::AL::Software","port");
}

 

sub test_get_database{
	my $soft = GWInstaller::AL::Software->new();
	my $db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$soft->set_database($db);
	$retdb = $soft->get_database();
	(($retdb->isa(GWInstaller::AL::Database)) && ($retdb->{'identifier'} eq "Test Database"))?($pass = 1):($pass = 0);		
}

sub test_set_database{
	$self = shift;
	return $self->test_get_database();		
}