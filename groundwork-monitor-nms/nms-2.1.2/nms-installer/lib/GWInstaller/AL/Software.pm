#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::Software; 

sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
	};
	$self->{'section'} = $section;
	foreach $param (@parameters){
 		($key,$value) = split(/=/,$param);
		$self->{ $key } = $value;		
 	}
	bless($self,$class);

	return $self;		
}

# get_status:  Static Method
# Params: GWInstaller::AL::Software installedSoftwareObj , GWInstaller::AL::Software availableSoftwareObj
# Returns: status: 0 (Not Installed), 1 (Installed), 2 (Upgrade), -1 (Invalid- e.g. downgrade)
sub compare_status{
	
	
}

sub get_status{
	
}

sub set_status{
	
}
sub configure{
	#abstract method
    die "This method must be overridden by a subclass of __PACKAGE__";
}
sub deploy{
	#abstract method
    die "This method must be overridden by a subclass of __PACKAGE__";
}
sub is_functional{
	#abstract method
    die "This method must be overridden by a subclass of __PACKAGE__";
}

sub get_identifier{
	$self = shift;
	return $self->{identifier};
}
sub set_identifier{
	$self = shift;
	$self->{identifier} = shift;
}

sub get_port{
	$self = shift;
	return $self->{port};
}
sub set_port{
	my ($self,$newport) = @_;
	$self->{port} = $newport;
}

sub get_host{
	$self = shift;
	return $self->{host};
}
sub set_host{
	$self = shift;
	$self->{host} = shift;
}

sub get_database{
	$self = shift;
	return $self->{database};
}
sub set_database{
	$self = shift;
	$self->{database} = shift;
}
sub is_installed{
	my $self = shift;
	
	
	
	my $command = $self->{'version_command'};
    my $pkg = $self->{'rpm_name'};
    my $other_installed = 0;
	my $is_installed = 0;
	my $rpm_check;
	my $rpm_installed = 0;
	
	
     unless($pkg eq ""){
    	GWInstaller::AL::GWLogger::log("\tchecking if $pkg installed");
     	$rpm_check= `rpm -qa $pkg 2> /dev/null`;
     }
     $other = `$command`;
     chomp($other);
     if(($other ne "") && ($self->{'valid_version'} ne "") &&  (($other =~ /$self->{'valid_version'}/) || ($self->{'valid_version'} eq "ANY" ))){
     	$other_installed = 1;
     }
     
     if(($pkg ne "") && ($rpm_check ne "") && ($rpm_check =~ /$pkg/)){
     	$rpm_installed = 1;
     	
     }
    $debug=0;
    if($debug){		    
	    GWInstaller::AL::GWLogger::log("rpminstalled=$rpm_installed");
    	GWInstaller::AL::GWLogger::log("other_installed=$other_installed");
    	GWInstaller::AL::GWLogger::log("valid ver = $self->{'valid_version'}");
    	GWInstaller::AL::GWLogger::log("Checking other: $other");
    }
    
    if($rpm_installed || $other_installed){
		$is_installed = 1;
    }
    else{
		$is_installed =  0;
    }
    
    return $is_installed;
}

#sub is_supported{
#	my $self = shift;
#	$retval = 0;
#    my $vc = $self->{'version_command'};
#    $version = `$vc 2> /dev/null`;
#    if($version =~ $self->{valid_version}){
#		$retval = 1;
#	}
#	# GWLogger::log("comparing $self->{valid_version} to $version");
#	return $retval;
#	
#}
 
sub uninstall{
	$self = shift;
    $pkg = $self->{'rpm_name'};
    GWInstaller::AL::GWLogger::log("Uninstalling $self->{'rpm_name'}");
    print `rpm -e $pkg >>  nms.log 2>&1`;
   

}

sub install{
	$self = shift;
    $pkg = "./packages/" . $self->{'rpm_name'} . "*";
  
    print `rpm -ivh $pkg >> nms.log 2>&1`;
	sleep $sleep; 
	
 
	
}
sub get_version{
	$self = shift;
	$version = get_rpm_version($self->{'rpm_name'});
	unless($version){
		$version = $self->get_other_version();
	}	
	
	return $version;
	
}

sub get_installed_version{
	$self = shift;
	$version = $self->get_installed_rpm_version();
	unless($version){
		$cmd = $self->{'version_command'};
		$version = `$cmd`;
		chomp $version;
	}
	#GWLogger::log("Version command = $cmd");
	GWLogger::log("\t\tinstalled version = $version");
	return $version;
}

sub get_installed_rpm_version{
	$self = shift;
	$rpm_name = $self->{'rpm_name'};

	$cmd = "rpm -qi $rpm_name  2>/dev/null | grep Version  2>> nms.log";

 
	$version = `$cmd`;
	$version =~ s/Vendor.*//;
	$version =~ s/Version\s+://;
	$version =~ s/\s+//g;

	return $version
	
}
sub get_rpm_version{
	$rpm_name = shift;
     chomp($rpm_name);
	$cmd = "rpm -qip $rpm_name  2>/dev/null | grep Version  2>> nms.log";

 
	$version = `$cmd`;
	$version =~ s/Vendor.*//;
	$version =~ s/Version\s+://;
	$version =~ s/\s+//g;

	return $version	
}

sub set_httpd{
	my ($self,$obj) = @_;
	$self->{httpd} = $obj;
}

sub get_httpd{
	$self = shift;
	return $self->{httpd};
}

# for databases associated with the software package

sub set_database_name{
	my($self,$value) = @_;
	$self->{database_name} = $value;	
}
sub get_database_name{
	my $self = shift;
	return $self->{database_name};
}
sub set_database_user{
	my($self,$value) = @_;
	$self->{database_user} = $value;	
}
sub get_database_user{
	my $self = shift;
	return $self->{database_user};	
}
sub set_database_password{
	my($self,$value) = @_;
	$self->{database_password} = $value;	
}
sub get_database_password{
	my $self = shift;
	return $self->{database_password};	
}

sub get_name{
  	my ($self) = shift;
 	my $ref = ref $self;
 	my (undef,undef,$className) = split(/::/,$ref);
 	my $methname = lc($className);
 	return $methname;	
}

sub get_do_install{
	$self = shift;
	return $self->{do_install};
}
sub set_do_install{
	my($self,$value) = @_;
	$self->{do_install} = $value;
	
}

1;