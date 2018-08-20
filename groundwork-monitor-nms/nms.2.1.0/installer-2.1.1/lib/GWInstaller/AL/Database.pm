#!/usr/bin/perl
package GWInstaller::AL::Database;

sub new{
	my ($invocant,$identifier,$type,$host,$port,$name,$user,$password) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		identifier => $identifier,
		type => $type,
		host => $host,
		port => $port,
		name => $name,
		user => $user,
		root_user=> $root_user,
		root_password => $root_password,
		password => $password
	};

	bless($self,$class);
	$self->init();
	return $self;
}

sub can_connect{
	$self = shift;
	$properties = shift;
	$passwd = $self->{root_password};
	$retval = 1;
	$hostname = $self->get_host();
	$fqdn_status = $properties->get_fqdn_status();

	if ($fqdn_status eq "fqdn") {
		$host_segment = "--host $hostname";
	} else {
		$host_segment = "";
	}

 	if($passwd){
 		$mysql_cmd = "mysql -p$passwd $host_segment -e 'select User from mysql.user limit 1' 2>&1";
		$mysql_login = `$mysql_cmd`; #mysql -p$passwd -e 'select User from mysql.user limit 1' 2>&1`;
	}
	else{
		
		$mysql_cmd = "mysql $host_segment -e 'select User from mysql.user limit 1' 2>&1";
		$mysql_login = `$mysql_cmd`;
	}
	chomp($mysql_login);
	
	GWInstaller::AL::GWLogger::log("MSQL_CMD: $mysql_cmd");
	GWInstaller::AL::GWLogger::log("MSQL_LOGIN: $mysql_login");
	
	if($mysql_login =~ /ERROR/){
		$retval = 0;	
		
	}
	 
	 
	return $retval;	
}

sub init{
	$self = shift;
	#set default values
	$self->set_port(3306);
	$self->set_type("mysql");
	$self->set_root_user("root");
	$self->set_identifier("mysql_main");
}
sub get_identifier{
	$self = shift;
	return $self->{identifier};
}
sub set_identifier{
	$self = shift;
	$self->{identifier} = shift;
}
sub get_host{
	$self = shift;
	return $self->{host};
}
sub set_host{
	$self = shift;
	$self->{host} = shift;
	
}
sub get_type{
	$self = shift;
	return $self->{type};
}
sub set_type{
	$self = shift;
	$self->{type} = shift;
}
sub get_port{
	$self = shift;
	return $self->{port};
}
sub set_port{
	$self = shift;
	$self->{port} = shift;
}
sub get_user{
	$self = shift;
	return $self->{user};
}
sub set_user{
	$self = shift;
	$self->{user} = shift;
}
sub get_password{
	$self = shift;
	return $self->{password};
}
sub set_password{
	$self = shift;
	$self->{password} = shift;
}
sub get_name{
	$self = shift;
	return $self->{name}; 
}

sub set_name{
	$self = shift;
	$self->{name} = shift;
}

sub get_root_user{
	$self = shift;
	return $self->{root_user};
}
sub set_root_user{
	$self = shift;	
	$self->{root_user} = shift;
}

sub get_root_password{
	$self = shift;
	return $self->{root_password};
}
sub set_root_password{
	$self = shift;	
	$self->{root_password} = shift;
}
1;
