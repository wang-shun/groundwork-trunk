#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWNMSInstaller::AL::GWNMS;
use lib qw(../../);
use GWInstaller::AL::Host;
use GWNMSInstaller::AL::NMSProperties;

sub new{
	 
	my ($invocant,$hostname) = @_;
	my $class = ref($invocant) || $invocant;
 	unless($hostname){$hostname = "localhost";} #localhost if no hostname specified.
 	$host = GWInstaller::AL::Host->new($hostname);
    
	 	my $self = {
		host=>$host,
		is_valid=>$is_valid,
		is_master=>0
	};
 
 
	bless($self,$class);
 
	return $self;		
}



sub get_installed_version{
	$self = shift;
	$rpm_name = "groundwork-nms-core";
	
	$cmd = "rpm -qi $rpm_name  2>/dev/null | grep Version  2>> nms.log";

 
	$version = `$cmd`;
	$version =~ s/Vendor.*//;
	$version =~ s/Version\s+://;
	$version =~ s/\s+//g;

	return $version
	
}
#
# has_config(): checks for the config in the default location.
#
sub has_config{
	$self = shift;
	my $has_config = 0;
	if (-e "/usr/local/groundwork/enterprise/config/enterprise.properties"){
		$has_config = 1;
	}
	return $has_config;
	
}

sub is_complete{
	$has_core = 0;
	$rpm_count = 0;
	$is_complete = 0;
	
	@files = <./packages/groundwork-nms*rpm>;
	foreach $rpm_file (@files){
		if($rpm_file =~ groundwork-nms-core){
			$has_core = 1;
			$rpm_count++;
		}
		else{ $rpm_count++;}
	}
	
	if($has_core && ($rpm_count >=2)){
		$is_complete = 1;
	}
}

sub get_config{
	$self = shift;
 	$config = GWNMSInstaller::AL::NMSProperties->new(); 
 	return $config
}

sub get_host{
	$self = shift;
	return $self->{host};
}

sub get_installed_components{

# Check for ntop, Weathermap, Cacti, NeDi
@nms_components = ("ntop","Weathermap","Cacti","NeDi");
@installed_components = ();

	#IFF component is installed, add a new instance of the subcomponent object to an array to be returned
	foreach $component(@nms_components){
		$componentClass = "GWNMSInstaller::AL::" . $component;
		$component = $componentClass->new();
		if($component->is_installed()){
			push(@installed_components,$component->new());	
			#GWInstaller::AL::GWLogger::log("$component->{rpm_name} is installed");		
		}
		else{
			#GWInstaller::AL::GWLogger::log("$component->{rpm_name} is NOT installed");
		}
	}
	return @installed_components;
	
}

sub get_available_components{
@available_packages = ();
#@rpms = `ls -1 packages/*rpm 2>/dev/null`;
	@rpms = <./packages/groundwork-nms*rpm>;

	# Ask each RPM what the name is
	foreach $rpm_name (@rpms){
		chomp($rpm_name);
		$cmd = "rpm -qip $rpm_name | grep Name | sed s/' '//g | sed s/Name:// | sed s/Relocations.*//";
		#GWInstaller::AL::GWLogger::log("COMMAND: $cmd");
		$pkg_name = `$cmd`;
		chomp($pkg_name);
 		if($rpm_name =~ /core/){next;}
 		$pkg_name =~ s/groundwork-nms-//;
		push(@available_packages,$pkg_name);
	}
return @available_packages;

}

sub set_is_valid{
	my ($self,$validity) = @_;
	$self->{is_valid} = $validity;
	
}

sub is_master{
	$self = shift;
	
#	unless(($self->{is_master} == 1) || ($self->{is_master} == 0)){
		my $configuration_filename = "/usr/local/groundwork/enterprise/config/enterprise.properties";
		
		my $host = "MASTER=" . `hostname`;
		chomp($host);
		#GWInstaller::AL::GWLogger::log("Checking for: $host");
		$hasthis = `grep -c $host $configuration_filename >/dev/null 2>&1`;
		chomp($hasthis);
		
		$self->{is_master} = $hasthis;
		
#	}
	return $self->{is_master};
}
sub get_release_string{
 
	$release =  `rpm -qip packages/groundwork-nms-core* | grep ^Release | sed s/Build.*//   | sed s/' '//g   | sed s/^Release:.*\\\\.// | sed s/_64//`;
	chomp($release);
 	return $release;
	
}
sub install_status{
	return 1;
}
 

1;
