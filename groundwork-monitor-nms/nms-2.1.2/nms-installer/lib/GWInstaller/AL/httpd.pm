#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::httpd;

sub new{
	 
	my ($invocant,$hostname,$port,$identifier) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		host=>$host,
		port=>$port,
		identifier=>$identifier
	};
	bless($self,$class);
	$self->init();
	return $self;		
}

sub init{
	$self = shift;
	#set defaults
	$self->set_identifier("httpd_main");
	$self->set_port(80);
	$self->set_auth_login("$application.gwm.gwm_main");
	$self->set_auth_domain("groundwork.groundworkopensource.com");
}

sub set_auth_login{
	my($self,$auth) = @_;
	$self->{auth_login} = $auth;
		
}
sub get_auth_login{
	my $self = shift;
	return $self->{auth_login};
}

sub set_auth_domain{
	my($self,$auth) = @_;
	$self->{auth_domain} = $auth;
		
}
sub get_auth_domain{
	my $self = shift;
	return $self->{auth_domain};
} 

# Function to get port number
sub get_port{
	shift;
    return $self->{port};	
}

# Function to set port number
sub set_port{
	$self = shift;
    $self->{port} = shift;
}

# Function to get hostname value
sub get_host{
	shift;
    return $self->{host};
}

# Function to set hostname value
sub set_host{
	$self = shift;
	$self->{host} = shift;
}

# Function to get identifier value
sub get_identifier{
    shift;
    return $self->{identifier};
}

# Function to set identifier value
sub set_identifier{
    $self = shift;
    $self->{identifier} = shift;	
}

1;
