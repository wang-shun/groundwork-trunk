#!/usr/bin/perl

package TestDatabase;
use lib qw(../);
use GWInstaller::AL::Database;
use GWTest::GWTest;

@ISA = qw(GWTest);

  
 
sub test_init{
	$pass = 0;
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->isa(GWInstaller::AL::Database)?($pass = 1):($pass = 0);
 
	
	return $pass;
}

sub test_get_identifier{
	$self = shift;
	return $self->testGet("GWInstaller::AL::Database","identifier");
}

sub test_set_identifier{
	$self = shift;
	return $self->testSet("GWInstaller::AL::Database","identifier");
		
}

sub test_get_host{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$host = $db->get_host();
	($host eq "localhost")?($pass = 1):($pass = 0);	
}

sub test_set_host{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_host("alabama");
	$host = $db->get_host();
    ($host eq "alabama")?($pass = 1):($pass = 0);		
}

sub test_get_type{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$type = $db->get_type();
	($type eq "MySQL")?($pass = 1):($pass = 0);	
}

sub test_set_type{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_type("DB2");
	$type = $db->get_type();
    ($type eq "DB2")?($pass = 1):($pass = 0);		
}

sub test_get_port{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$port = $db->get_port();
	($port == 3306)?($pass = 1):($pass = 0);	
} 

sub test_set_port{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_port(9999);
	$port = $db->get_port();
    ($port == 9999)?($pass = 1):($pass = 0);		
}

sub test_get_user{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$user= $db->get_user();
	($user eq "testuser")?($pass = 1):($pass = 0);	
}

sub test_set_user{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_user("otheruser");
	$user= $db->get_user();
    ($user eq "otheruser")?($pass = 1):($pass = 0);		
}

sub test_get_password{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$password = $db->get_password();
	($password eq "testpassword")?($pass = 1):($pass = 0);	
}

sub test_set_password{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_password("otherpw");
	$password = $db->get_password();
    ($password eq "otherpw")?($pass = 1):($pass = 0);		
}

sub test_get_name{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$name = $db->get_name();
	($name eq "testdb")?($pass = 1):($pass = 0);	
}

sub test_set_name{
	$db = GWInstaller::AL::Database->new("Test Database","MySQL","localhost",3306,"testdb","testuser","testpassword");
	$db->set_name("Other dbName");
	$name = $db->get_name();
    ($name eq "Other dbName")?($pass = 1):($pass = 0);		
}

1;