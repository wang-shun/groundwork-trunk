#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms.
#

package GWNMSInstaller::AL::NMSProperties;

 use lib "./lib"; 
 
@ISA = (GWInstaller::AL::Properties);
use Socket;
use Sys::Hostname;
use IO::Handle;

  sub new {
         my($class) = shift;
        my($self) = GWInstaller::AL::Properties->new("/usr/local/groundwork/enterprise/config/enterprise.properties");
		
        bless($self, $class);
		return $self;
}

sub get_domain_name{
	$self = shift;
	
	unless($self->{fqdn_status}){
		
		$line = `grep DOMAIN_NAME /usr/local/groundwork/enterprise/config/enterprise.properties 2>/dev/null`;
		chomp($line);
		(undef,$domain) = split('=',$line);
		 $self->set_domain_name($domain); 
	}
	
	return $self->{domain_name};
	
}

sub set_domain_name{
	my($self,$fqdn) = @_;
	$self->{domain_name} = $fqdn;
}

sub set_fqdn{
	my($self,$fqdn) = @_;
	$self->{fqdn} = $fqdn;
}
sub get_fqdn_status{
	$self = shift;
	
	unless($self->{fqdn_status}){
		
		$line = `grep FQDN_STATUS /usr/local/groundwork/enterprise/config/enterprise.properties 2>/dev/null`;
		chomp($line);
		(undef,$status) = split('=',$line);
		 $self->set_fqdn_status($status); 
	}
	
	unless($self->{fqdn_status}){
		
	}
	return $self->{fqdn_status};
	
}

sub set_fqdn_status{
	($self,$status) = @_;
	$self->{fqdn_status} = $status;
}

sub match_hosts_and_applications{
	my $self = shift;
	my $debug=1;
	GWInstaller::AL::GWLogger::log("Matching Hosts and Components...");
	
	
	my @collectionNames = $self->get_collection_names();
	my $hostCollection = $self->get_collection("host");
	#@collectionNames = ("nedi");
  	#for each collection (ntop, weathermap, cacti,nedi, etc)
	foreach $cName (@collectionNames){
		if($debug){ GWInstaller::AL::GWLogger::log("Requesting collection: $cName");}		 
		 #skip host collection
		if($cName =~ "host"){next;}
		
		my $collect = $self->get_collection($cName);

		
		while($collect->has_next()){
			
			#get an object (nedi,cacti,etc) from the collection
			my $obj = $collect->next();
			if($debug){
				
		 	 GWInstaller::AL::GWLogger::log("Collection Size:" . $collect->{array_size});		 
			 GWInstaller::AL::GWLogger::log("Collection Obj Class:" . ref $obj);			
			}
			
			my $hostname = $obj->get_host();
			if($debug){GWInstaller::AL::GWLogger::log("Objects host: $hostname");			}
				
			#get corresponding HOST object from host collection
			$hcHost = $hostCollection->get_by_identifier($hostname);
			if($debug){GWInstaller::AL::GWLogger::log("Corresponding Host Obj Class:" . ref $hcHost);	}		


			
			#set value in HOST object
			$method = "set_" . $self->get_meth_name($obj);  
			if($debug){ GWInstaller::AL::GWLogger::log("$cName: $method to " . ref $obj);
			 GWInstaller::AL::GWLogger::log("$cName: OBJ ID: " . $obj->get_identifier());}

			#$hcHost->$method($obj->get_identifier());	
			
			#set object in collection
			$hostCollection->set_by_identifier($hostname,$hcHost);

			#set collection
		#	$self->{$cName} = $hostCollection;			
		}
	if($debug){	GWInstaller::AL::GWLogger::log(" ");
		GWInstaller::AL::GWLogger::log(" ");		}	
					
		
		$collect->reset_index();
	}
}
sub write_properties{
	my $self = shift;
	unless(-d "/usr/local/groundwork/enterprise"){
		print `mkdir /usr/local/groundwork/enterprise >>nms.log 2>&1`;
	}	
	unless(-d '/usr/local/groundwork/enterprise/config'){
		print `mkdir /usr/local/groundwork/enterprise/config >>nms.log 2>&1`;
	}
	
#	 eval{ 
#	 $self->match_hosts_and_applications();
#	 };
#	 if($@){GWInstaller::AL::GWLogger::log("Warning: $@");}
	 
	my $config_file = ">/usr/local/groundwork/enterprise/config/enterprise.properties";
	 GWInstaller::AL::GWLogger::log("Writing properties to $config_file...");
	#open file for writing;
	open(CONFIG,$config_file) || GWInstaller::AL::GWLogger::log("cant open $config_file: $!");
	CONFIG->autoflush(1);
	#write headers
	##############
	print CONFIG qq{
##	-----------------------------------
##	enterprise.properties
##	Enterprise configuration properties
##	-----------------------------------
##	Copyright 2008 Groundwork Open Source, Inc.
##  This file was generated by the GroundWork NMS Configuration Tool/Installer		
##	-----------------------------------
##
	};

	# Write Master Config Server
	############################
print CONFIG qq{ 
##########################
## Master Config Server ##
##########################
};
	my $master_server = $self->get_master();
	print CONFIG "MASTER=$master_server\n\n";	


print CONFIG qq{ 
##########
## FQDN ##
##########
}; 
print CONFIG "FQDN_STATUS=" . $self->get_fqdn_status() . "\n";
print CONFIG "DOMAIN_NAME=" . $self->get_domain_name() . "\n\n";

 	#write database
	###############
print CONFIG qq{ 
#########################
## Database Properties ##
#########################
};
	#get database collection
	my $dbCollection = $self->get_collection("database");
	while($dbCollection->has_next()){
		my $dbObj = $dbCollection->next();
		my $ident = $dbObj->get_identifier();

		print CONFIG "\nsystem.database.${ident}.host=" . $dbObj->get_host();
		print CONFIG "\nsystem.database.${ident}.type=" . $dbObj->get_type();
		print CONFIG "\nsystem.database.${ident}.port=" . $dbObj->get_port();
		print CONFIG "\nsystem.database.${ident}.root_user=" . $dbObj->get_root_user();
		print CONFIG "\nsystem.database.${ident}.root_password=" . $dbObj->get_root_password();
		print CONFIG "\n";
	}	

	

print CONFIG qq{ 
#############################
## Event Broker Properties ##
#############################
};
	my $bronxCollection = $self->get_collection("bronx");
	while($bronxCollection->has_next()){
		my $bronxObj = $bronxCollection->next();
		my $ident = $bronxObj->get_identifier();
		
		print CONFIG "\nsystem.bronx.${ident}.host=" . $bronxObj->get_host();
		print CONFIG "\nsystem.bronx.${ident}.port=" . $bronxObj->get_port();
		print CONFIG "\n";
	}


print CONFIG qq{ 
###########################
## Foundation Properties ##
###########################
};
	my $foundationCollection = $self->get_collection("foundation");
	while($foundationCollection->has_next()){
		my $foundationObj = $foundationCollection->next();
		my $ident = $foundationObj->get_identifier();
		
		print CONFIG "\nsystem.foundation.${ident}.host=" . $foundationObj->get_host();
		print CONFIG "\nsystem.foundation.${ident}.port=" . $foundationObj->get_port();
		print CONFIG "\n";
	}

 print CONFIG qq{ 
###################################
## GroundWork Monitor Properties ##
###################################
};
	my $gwmCollection = $self->get_collection("gwm");
	while($gwmCollection->has_next()){
		my $gwmObj = $gwmCollection->next();
		my $ident = $gwmObj->get_identifier();
		
		print CONFIG "\napplication.gwm.${ident}.host=" . $gwmObj->get_host();
		print CONFIG "\napplication.gwm.${ident}.port=" . $gwmObj->get_port();
		print CONFIG "\n";
	}
 
print CONFIG qq{ 
##########################
## NMS HTTPD Properties ##
##########################
};

 
	my $nmshttpdCollection = $self->get_collection("httpd");
	while($nmshttpdCollection->has_next()){
		my $nmshttpdObj = $nmshttpdCollection->next();
		my $ident = $nmshttpdObj->get_identifier();
		
		print CONFIG "\nnms.httpd.${ident}.host=" . $nmshttpdObj->get_host();
		print CONFIG "\nnms.httpd.${ident}.port=" . $nmshttpdObj->get_port();
		print CONFIG "\nnms.httpd.${ident}.auth_login=\$application.gwm." . $nmshttpdObj->get_auth_login();
		print CONFIG "\nnms.httpd.${ident}.auth_domain=" . $nmshttpdObj->get_auth_domain();
		print CONFIG "\n";
	}
 
print CONFIG qq{ 
######################
## Cacti Properties ##
######################
};

	my $cactiCollection = $self->get_collection("cacti");
	while($cactiCollection->has_next()){
		my $cactiObj = $cactiCollection->next();
		my $ident = $cactiObj->get_identifier();
		
		print CONFIG "\nnms.cacti.${ident}.host=" . $cactiObj->get_host();
		print CONFIG "\nnms.cacti.${ident}.database=\$system.database." . $cactiObj->get_database();
		print CONFIG "\nnms.cacti.${ident}.httpd=\$nms.httpd." . $cactiObj->get_httpd();
		print CONFIG "\nnms.cacti.${ident}.database_name=" . $cactiObj->get_database_name();
		print CONFIG "\nnms.cacti.${ident}.database_user=" . $cactiObj->get_database_user();
		print CONFIG "\nnms.cacti.${ident}.database_password=" . $cactiObj->get_database_password();
		print CONFIG "\n";
	}

print CONFIG qq{ 
#####################
## NeDi Properties ##
#####################
};
	my $nediCollection = $self->get_collection("nedi");
	while($nediCollection->has_next()){
		my $nediObj = $nediCollection->next();
		my $ident = $nediObj->get_identifier();
		
		print CONFIG "\nnms.nedi.${ident}.host=" . $nediObj->get_host();
		print CONFIG "\nnms.nedi.${ident}.database=\$system.database." . $nediObj->get_database();
		print CONFIG "\nnms.nedi.${ident}.httpd=\$nms.httpd." . $nediObj->get_httpd();
		print CONFIG "\nnms.nedi.${ident}.database_name=" . $nediObj->get_database_name();
		print CONFIG "\nnms.nedi.${ident}.database_user=" . $nediObj->get_database_user();
		print CONFIG "\nnms.nedi.${ident}.database_password=" . $nediObj->get_database_password();
		print CONFIG "\n";
	}

 print CONFIG qq{ 
#####################
## ntop Properties ##
#####################
};
	my $ntopCollection = $self->get_collection("ntop");
	while($ntopCollection->has_next()){
		my $ntopObj = $ntopCollection->next();
		my $ident = $ntopObj->get_identifier();
		
		print CONFIG "\nnms.ntop.${ident}.host=" . $ntopObj->get_host();
		print CONFIG "\nnms.ntop.${ident}.port=" . $ntopObj->get_port();
		print CONFIG "\n";
	} 


 print CONFIG qq{ 
############################
##  Weathermap Properties ##
############################
};
	my $weathermapCollection = $self->get_collection("weathermap");
	while($weathermapCollection->has_next()){
		my $weathermapObj = $weathermapCollection->next();
		my $ident = $weathermapObj->get_identifier();
		print CONFIG "\nnms.weathermap.${ident}.host=" . $weathermapObj->get_host();
		print CONFIG "\n";
	} 
 
############
# PACKAGES #
############

 print CONFIG qq{ 
##########################
## cacti-pkg Properties ##
##########################
};
	my $cacti_pkgCollection = $self->get_collection("cacti");
	while($cactiCollection->has_next()){
		my $cactiObj = $cactiCollection->next();
		my $gwm_host = $cactiObj->get_GWM_host();
		my $host;
		my $gwmCollection = $self->get_collection("gwm");
 
		if($gwmCollection->is_empty()){
		}
		else{
 		 $host = $gwmCollection->get_by_identifier($gwm_host);
 		 
		}
 
	# my $ident = $cacti_pkgObj->get_identifier();
		print CONFIG "\napplication.cacti-pkg.${gwm_host}.host=" . $host->get_host();
		print CONFIG "\napplication.cacti-pkg.${gwm_host}.cacti=\$nms.cacti." . $cactiObj->get_identifier();
		print CONFIG "\n";
	} 


 print CONFIG qq{ 
###################################
## nms-automation-pkg Properties ##
###################################
};
	my $nms_automationCollection = $self->get_collection("nms_automation");
	while($nms_automationCollection->has_next()){
		my $nms_automationObj = $nms_automationCollection->next();
		my $ident = $nms_automationObj->get_identifier();
		print CONFIG "\napplication.nms-automation.${ident}.host=" . $nms_automationObj->get_host();
		print CONFIG "\napplication.nms-automation.${ident}.cacti=\$nms.cacti." . $nms_automationObj->get_cacti();
		print CONFIG "\napplication.nms-automation.${ident}.nedi=\$nms.nedi." . $nms_automationObj->get_nedi();
		print CONFIG "\n";
	}  

 print CONFIG qq{ 
#########################
## nedi-pkg Properties ##
#########################
};
	my $nediCollection = $self->get_collection("nedi");
	
	while($nediCollection->has_next()){
		my $nediObj = $nediCollection->next();
		my $gwm_host = $nediObj->get_GWM_host();
		my $host;
		
		my $gwmCollection = $self->get_collection("gwm");
 
		if($gwmCollection->is_empty()){
		}
		else{
 		 $host = $gwmCollection->get_by_identifier($gwm_host);
 		 
		}
		print CONFIG "\napplication.nedi-pkg.${gwm_host}.host=" . $host->get_host();
		print CONFIG "\napplication.nedi-pkg.${gwm_host}.nedi=\$nms.nedi." . $nediObj->get_identifier();
		print CONFIG "\n";
	}   

 print CONFIG qq{ 
#########################
## ntop-pkg Properties ##
#########################
};


	my $ntopCollection = $self->get_collection("ntop");
	
	while($ntopCollection->has_next()){
		my $ntopObj = $ntopCollection->next();
		my $gwm_host = $ntopObj->get_GWM_host();
		my $host;
		
		my $gwmCollection = $self->get_collection("gwm");
 
		if($gwmCollection->is_empty()){
		}
		else{
 		 $host = $gwmCollection->get_by_identifier($gwm_host);
 		 
		}
		print CONFIG "\napplication.ntop-pkg.${gwm_host}.host=" . $host->get_host();
		print CONFIG "\napplication.ntop-pkg.${gwm_host}.ntop=\$nms.ntop." . $ntopObj->get_identifier();
		print CONFIG "\n";
	}
	
	

 print CONFIG qq{ 
######################################
## weathermap-editor-pkg Properties ##
######################################
};
	my $weathermapCollection = $self->get_collection("weathermap");


	while($weathermapCollection->has_next()){
		my $weathermapObj = $weathermapCollection->next();
		
		my $gwm_host = $weathermapObj->get_GWM_host();
		my $host;
		
		my $gwmCollection = $self->get_collection("gwm");
 
		if($gwmCollection->is_empty()){
		}
		else{
 		 $host = $gwmCollection->get_by_identifier($gwm_host);
 		 
		}
		$cacti_id = "cacti_" . $host->get_host();
		$cacti_id =~ s/\./_/g;
 		my $cactiCollection = $self->get_collection("cacti");
 		my $cacti = $cactiCollection->get_by_identifier($cacti_id);
 		if(ref $cacti eq "GWNMSInstaller::AL::Cacti"){
 			$cacti_instance = $cacti_id;
 		}
 		else{
 			$cacti_instance = $weathermapObj->get_cacti();
 		}
 		
		my $ident = $host->get_host();
		$ident =~ s/\./_/g;
		print CONFIG "\napplication.weathermap-editor-pkg.${ident}.host=" . $host->get_host();
		print CONFIG "\napplication.weathermap-editor-pkg.${ident}.weathermap-editor=\$nms.cacti." . $cacti_instance; 
		print CONFIG "\n\n";
	}     
 
}

sub erase_a_host{
	my($self,$deleted_host) = @_;
    my $properties = $self;
    #erase from host collection	
    my $hostCollection = $properties->get_collection("host");       		
    $hostCollection->remove_by_identifier($deletehost);
        		        			
	#erase from database collection
	my $dbCollection = $self->get_collection("database");
	while($dbCollection->has_next()){
		my $dbObj = $dbCollection->next();
		my $ident = $dbObj->get_identifier();

		if($deleted_host eq $dbObj->get_host()){
			$dbCollection->remove_by_identifier($ident);
		}
	}	


	#erase from bronx collection
	my $bronxCollection = $self->get_collection("bronx");
	while($bronxCollection->has_next()){
		my $bronxObj = $bronxCollection->next();
		my $ident = $bronxObj->get_identifier();

		if($deleted_host eq $bronxObj->get_host()){
			$bronxCollection->remove_by_identifier($ident);
		}
		
	}


	#erase from foundation collection

	my $foundationCollection = $self->get_collection("foundation");
	while($foundationCollection->has_next()){
		my $foundationObj = $foundationCollection->next();
		my $ident = $foundationObj->get_identifier();
		
 		if($deleted_host eq $foundationObj->get_host()){
			$foundationCollection->remove_by_identifier($ident);
		}
 
	}


	#erase from gwm collection
	my $gwmCollection = $self->get_collection("gwm");
	while($gwmCollection->has_next()){
		my $gwmObj = $gwmCollection->next();
		my $ident = $gwmObj->get_identifier();
		
		if($deleted_host eq $gwmObj->get_host()){
			$gwmCollection->remove_by_identifier($ident);
		}

	}
 



	#erase from nms httpd Collection 
	my $nmshttpdCollection = $self->get_collection("httpd");
	while($nmshttpdCollection->has_next()){
		my $nmshttpdObj = $nmshttpdCollection->next();
		my $ident = $nmshttpdObj->get_identifier();
		
		if($deleted_host eq $nmshttpdObj->get_host()){
			$nmshttpdCollection->remove_by_identifier($ident);
		}

	}
 


	#erase from cacti collection
	my $cactiCollection = $self->get_collection("cacti");
	while($cactiCollection->has_next()){
		my $cactiObj = $cactiCollection->next();
		my $ident = $cactiObj->get_identifier();
		
		if($deleted_host eq $cactiObj->get_host()){
			$cactiCollection->remove_by_identifier($ident);
		}

	}


	#erase from nedi collection
	my $nediCollection = $self->get_collection("nedi");
	while($nediCollection->has_next()){
		my $nediObj = $nediCollection->next();
		my $ident = $nediObj->get_identifier();
		
		if($deleted_host eq $nediObj->get_host()){
			$nediCollection->remove_by_identifier($ident);
		}

	}

	
	#erase from ntop collection
	my $ntopCollection = $self->get_collection("ntop");
	while($ntopCollection->has_next()){
		my $ntopObj = $ntopCollection->next();
		my $ident = $ntopObj->get_identifier();
		
		if($deleted_host eq $ntopObj->get_host()){
			$ntopCollection->remove_by_identifier($ident);
		}

	} 


	#erase from weathermap collection
	my $weathermapCollection = $self->get_collection("weathermap");
	while($weathermapCollection->has_next()){
		my $weathermapObj = $weathermapCollection->next();
		my $ident = $weathermapObj->get_identifier();

		if($deleted_host eq $weathermapObj->get_host()){
			$weathermapCollection->remove_by_identifier($ident);
		}


	} 
  

	#remove from nms automation collection
	my $nms_automationCollection = $self->get_collection("nms_automation");
	while($nms_automationCollection->has_next()){
		my $nms_automationObj = $nms_automationCollection->next();
		my $ident = $nms_automationObj->get_identifier();

		if($deleted_host eq $nms_automationObj->get_host()){
			$nms_automationCollection->remove_by_identifier($ident);
		}

	}  
   
  
	
}
sub get_master_properties{
	
}


sub get_hostCollection{
	$self = shift;
	return $self->{hostCollection};	
}

sub get_bronxCollection{
	$self = shift;
	return $self->{bronxCollection};
}

sub get_foundationCollection{
	$self = shift;
	return $self->{foundationCollection};	
}

sub get_httpdCollection{
	$self = shift;
	return $self->{httpdCollection};
}
 
 
sub set_hostCollection{
	($self,$val) = shift;
	$self->{hostCollection} = $val;	
}

sub set_bronxCollection{
	($self,$val) = shift;
	$self->{bronxCollection} = $val;
}

sub set_foundationCollection{
	($self,$val) = shift;
	$self->{foundationCollection} = $val;	
}

sub set_httpdCollection{
	($self,$val) = shift;
	$self->{httpdCollection} = $val;
}

sub get_master{
	my $self = shift;
	unless($self->{master}){
		my $local = `hostname`;
		chomp($local);
		$self->set_master($local);
	}
	return $self->{master};
}

sub set_master{
	my($self,$master) = @_;
	$self->{master} = $master;
}

sub get_mcs{
	
	$efile = "/usr/local/groundwork/enterprise/config/enterprise.properties";
	$cmd = "grep ^MASTER $efile > /dev/null 2>&1";
	$line = `$cmd`;
	chomp($line);
	
	(undef,$masterhost) = split(/\=/,$line);
	return $masterhost;
}

sub find_instance
{
    my ($ref_properties, $type, $subtype, $host_name) = @_;
    my @properties = @{$ref_properties};
    $num_properties = @properties; #@{$ref_properties};
    #printf "DEBUG: {find_instance} num_properties = $num_properties";
    for ($pnum=0; $pnum < $num_properties; $pnum++)
    {
        if ($properties[$pnum]->{'type'} eq $type
        && $properties[$pnum]->{'subtype'} eq $subtype
        && $properties[$pnum]->{'property'} eq 'host'
        && $properties[$pnum]->{'value'} eq $host_name)
        {
            $i = $properties[$pnum]->{'instance'};
            # GWInstaller::AL::GWLogger::log("DEBUG: {find_instance} for $type $subtype $host_name returning instance=$i");
            GWInstaller::AL::GWLogger::log("INSTANCE VALUE:"  . $properties[$pnum]->{'value'} . " HOSTNAME:" . $host_name);
            return($i);
        }
    }

    GWInstaller::AL::GWLogger::log("Warning: no instance found.");
    return(null);
}


sub read_properties{
	
	my $self = shift;
	
	$master = $self->get_mcs();
	$self->{master} = $master;

	$efile = "/usr/local/groundwork/enterprise/config/enterprise.properties";
	open(ENTERPRISE,$efile);
	
	while(<ENTERPRISE>){
		$line = $_;
		chomp($line);
		 #Skip comments and blank lines
		if(($line =~ /^#/) ||($line =~ /^(\s)*$/)){next;}
		
		# Capture configuration taxa.
#		elsif($line =~ /^(\w+)\.(\w+)\.(.+)\.(.+)\=(.*)/){  
		elsif($line =~ /^(.+)\.(.+)\.(.+)\.(.+)\=(.*)/){  
			my $superclass = $1;
			my $subclass = $2;
			my $identifier = $3;
			my $property = $4;
			my $value = $5;		
			
	    	$self->handle_line($subclass,$property,$value,$identifier);
		}	 
		else{
			if($line =~ /^MASTER/){
				
				(undef,$newmaster) = split(/=/,$line);
				
				$self->set_master($newmaster);
			}
			GWInstaller::AL::GWLogger::log("skipping line: $line");
		}
	} #end while <ENTERPRISE>
	
		#post processing: Add components to Hosts
	 $self->match_hosts_and_components();

	
} # end read_properties()


sub match_hosts_and_components{
	my $self = shift;
	
	GWInstaller::AL::GWLogger::log("Matching Hosts and Components...");
	
	
	my @collectionNames = $self->get_collection_names();
	my $hostCollection = $self->get_collection("host");
	#@collectionNames = ("nedi");
  	#for each collection (ntop, weathermap, cacti,nedi, etc)
	foreach $cName (@collectionNames){
		if($debug){ GWInstaller::AL::GWLogger::log("Requesting collection: $cName");}		 
		 #skip host collection
		if($cName =~ "host"){next;}
		
		my $collect = $self->get_collection($cName);

		
		while($collect->has_next()){
			
			#get an object (nedi,cacti,etc) from the collection
			my $obj = $collect->next();
			if($debug){
				
		 	 GWInstaller::AL::GWLogger::log("Collection Size:" . $collect->{array_size});		 
			 GWInstaller::AL::GWLogger::log("Collection Obj Class:" . ref $obj);			
			}
			
			my $hostname = $obj->get_host();
			if($debug){GWInstaller::AL::GWLogger::log("Objects host: $hostname");			}
				
			#get corresponding HOST object from host collection
			$hcHost = $hostCollection->get_by_identifier($hostname);
			if($debug){GWInstaller::AL::GWLogger::log("Corresponding Host Obj Class:" . ref $hcHost);	}		


			
			#set value in HOST object
			$method = "set_" . $self->get_meth_name($obj);  
			if($debug){ GWInstaller::AL::GWLogger::log("$cName: $method to " . ref $obj);
			 GWInstaller::AL::GWLogger::log("$cName: OBJ ID: " . $obj->get_identifier());}

			$hcHost->$method($obj->get_identifier());	
			
			#set object in collection
			$hostCollection->set_by_identifier($hostname,$hcHost);

			#set collection
		#	$self->{$cName} = $hostCollection;			
		}
	if($debug){	GWInstaller::AL::GWLogger::log(" ");
		GWInstaller::AL::GWLogger::log(" ");		}	
					
		
		$collect->reset_index();
	}
}

sub get_meth_name{
 	my ($self,$obj) = @_;
 	my $ref = ref $obj;
 	my (undef,undef,$className) = split(/::/,$ref);
 #	GWInstaller::AL::GWLogger::log("classname:" . $className);
 	if($className =~ /WeathermapPackage/){
 		$className = "weathermap_editor_pkg";
 	}
	else{
		$className =~ s/Package/_pkg/;
	} 	
 #	GWInstaller::AL::GWLogger::log("classname2:" . $className); 	
 	my $methname = lc($className);
 	return $methname;	
}

sub get_collection_names{
	my $self = shift;
	my @cNames = ();
	
	$tc = $self->get_collection("ntop");
	while( ($key,$value) = each(%$self) ){
		#GWInstaller::AL::GWLogger::log("**Comparing $key");
		
		if ($key =~ /Collection/){
			push(@cNames,$key);	
		if($debug){	GWInstaller::AL::GWLogger::log("**Adding $key to CollectionNames");}
		}	
	}
	return @cNames;
}




sub handle_line{
	my ($self,$type,$property,$value,$identifier) = @_;
	my $collectionType = $type . "Collection";
	my $installer = $self->{installer};
	$className = $self->get_classname($type);

	$type =~ s/-/_/g;
	
 	#correct for LOCALHOST variable
   if ($value eq '%LOCAL_HOSTNAME'){
		$value =  $self->get_fqdn();
   }
 
	#retrieve collection by type
	my $collection = $self->get_collection($type);
 	$myref = ref $collection;

	#retrieve object by identifier if available, otherwise create
	#GWInstaller::AL::GWLogger::log("handle_line(): identifier=$identifier");
	my $obj = $collection->get_by_identifier($identifier);
	$myref = ref $obj;
 	#GWInstaller::AL::GWLogger::log("*********handle_line(): $identifier ref=X${myref}X");
	if($myref eq ""){  
 		
		GWInstaller::AL::GWLogger::log("Creating new object of type $className for $identifier");
		$obj = $className->new($identifier);
		$obj->set_identifier($identifier);
		
		my $mref = ref $obj;
		GWInstaller::AL::GWLogger::log("adding $mref to $collectionType");
		
		$collection->add($obj);
	#	$self->set_collection($type,$collection); #DWNT?
		
 	
 		 #GWInstaller::AL::GWLogger::log("*********2handle_line(): ref=$myref id=" . $obj->get_identifier());

	}
	$method = "set_" . $property;
	my $hc;
	#build hostlist from property
	if($property eq "host"){
		$hc = $self->get_collection("host");
		
		my $host = GWInstaller::AL::Host->new($value);
		$host->set_identifier($value);
	
		if($hc->get_by_identifier($value)){
			#GWInstaller::AL::GWLogger::log("Found " . $host->get_identifier() .  "in hostCollection.");	
		
		}
		else{ 
			$hc->add($host);
			GWInstaller::AL::GWLogger::log("Adding " . $host->get_identifier() . " to hostCollection");
		}
		
	$self->{hostCollection} = $hc;
	}

	
	#special cases for database, httpd, set value to identifier
	if($value =~ /^\$/){
		my (undef,$otype,$id) = split(/\./,$value);
		 $value = $id;
	}
 	$method =~ s/\-/_/g;
	#set the value of the property
	$obj->$method($value);
	 GWInstaller::AL::GWLogger::log("\tSetting $property for $identifier to $value");
	 
	 
	 
	$collection->set_by_identifier($identifier,$obj);
	$self->{$collectionType} = $collection;
	
	#GWInstaller::AL::GWLogger::log("Current Count for $collectionType = " . $collection->{array_size});
	
 	
}

sub get_collection{

	my($self,$collectionType) = @_;
	if($collectionType =~ /Collection/)
	{
		$collectionName = $collectionType;
	}
	else{
		$collectionName =  $collectionType . "Collection";
	}
	if($debug){GWInstaller::AL::GWLogger::log("get_collection(): Requesting $collectionName");}
	
	my $collection = $self->{$collectionName};
	
	if($debug){ GWInstaller::AL::GWLogger::log("\n $collectionName SIZE:" . $collection->{array_size});}
		 
 
	$classType = ref $collection;
	unless($classType eq "GWInstaller::AL::Collection"){
		GWInstaller::AL::GWLogger::log("\nCreating new collection of type: $collectionName");
		$collection = GWInstaller::AL::Collection->new();
		$self->{$collectionName} = $collection;
		
	}
	
	
	if($collection->{array_size}){ 
   my $tstobj  = $collection->next();
   my $classname = ref $tstobj;
   $collection->reset_index();
   
   if($debug){GWInstaller::AL::GWLogger::log("get_collection() CLASS:" . $classname);}
	}
	
	
	
 	return $self->{$collectionName};	
}

sub get_classname{
	my ($self,$type) = @_;
	my $classname = "";
	
	if($type eq "database"){ $classname = "GWInstaller::AL::Database";}
	elsif($type eq "bronx"){$classname = "GWInstaller::AL::EventBroker";}
	elsif($type eq "foundation"){$classname = "GWInstaller::AL::Foundation";}
	elsif($type eq "gwm"){$classname = "GWInstaller::AL::GWMonitor";}

	#GWM Guava Applications
	elsif($type =~ /cacti-pkg/){$classname = "GWNMSInstaller::AL::CactiPackage";}
	elsif($type =~ /nedi-pkg/){$classname = "GWNMSInstaller::AL::NeDiPackage";}
	elsif($type =~ /ntop-pkg/){$classname = "GWNMSInstaller::AL::ntopPackage";}
	elsif($type =~ /weathermap-editor-pkg/){$classname = "GWNMSInstaller::AL::WeathermapPackage";}	
	elsif($type =~ /nms-automation/){$classname = "GWNMSInstaller::AL::automationPackage";}	

	#NMS Components
	elsif($type =~ /cacti/){$classname = "GWNMSInstaller::AL::Cacti";}
	elsif($type =~ /nedi/){$classname = "GWNMSInstaller::AL::NeDi";}
	elsif($type =~ /ntop/){$classname = "GWNMSInstaller::AL::ntop";}
	elsif($type =~ /weathermap/){$classname = "GWNMSInstaller::AL::Weathermap";}	

	elsif($type eq "httpd"){$classname = "GWNMSInstaller::AL::NMShttpd";}
	#GWInstaller::AL::GWLogger::log("got classname = $classname");
	return $classname;
}

 

sub set_collection{
	my ($self,$type,$collection) = @_;
	$collectionName = $type . "Collection";
	$self->{$collectionName} = $collection;
}

sub find_property
{
    my ($ref_properties, $type, $subtype, $instance, $property) = @_;
    my @properties = @{$ref_properties};

    $num_properties = @properties;
    for ($pnum=0; $pnum < $num_properties; $pnum++)
    {
        $t = $properties[$pnum]->{'type'};
        $s = $properties[$pnum]->{'subtype'};
        $i = $properties[$pnum]->{'instance'};
        $p = $properties[$pnum]->{'property'};
        $v = $properties[$pnum]->{'value'};
        if ($t eq $type && $s eq $subtype && $i eq $instance && $p eq $property)
        {
            return($v);
        }
    }
    return(-1);
}

sub configuration_load()
{
	$self = shift;
	#GWInstaller::AL::GWLogger::log("LOADING CONFIG");
	
    my @properties = (); 
    my $configuration_filename = "/usr/local/groundwork/enterprise/config/enterprise.properties";
    my $line;
    my $lnum = 1;
    my $pnum = 0;


    #printf "DEBUG: {configuration_load} filename=$configuration_filename";
	
    open(CONFIG, $configuration_filename) || return(null);
    #printf "DEBUG: {configuration_load} File Opened, looping";
    while ($line = <CONFIG>)
    {
     chomp($line);
     next if ($line =~ /^(\s)*$/); #skip blank lines
     next if ($line =~ /^#/); #skip comments
	 next if ($line =~ /^MASTER/);
     next if ($line =~ /^FQDN_STATUS/);
     next if ($line =~ /^DOMAIN_NAME/);
      
     if($debug){GWInstaller::AL::GWLogger::log("$line");}
        
          $len=length($line);
        
    
        #printf "DEBUG: {configuration_load} line=$line";
        if ($len != 0)
        {
            if (!(substr($line,0,1) eq "#"))
            {
                #
                #       First, remove the comments and trailing
                #       spaces from the end of the line.
                #

                my $comment = index($line, "#");
                if ($comment != -1)
                {
                    $line = substr($line,0,$comment);
                }
                $line =~ s/ *$//;

                #
                #       Parse name/value pair
                #       And strip out any spaces from the
                #       beginning and end of the line.
                #

                $count = ($line =~ tr/=//);
                if ($count == 1)
                {
                    @pair = split(/=/,$line);
                    $name = $pair[0]; $value = $pair[1];
                    $name =~ s/^\s+//; $name =~ s/\s+$//;
                    $value =~ s/^\s+//; $value =~ s/\s+$//;

                    #
                    #       Substitute Macros in VALUE.
                    #

                    if ($value eq '%LOCAL_HOSTNAME')
                    {
                        $value =  $self->get_fqdn();
                    }

                    #
                    #       Now, split the name apart
                    #       into type, subtype,instance,property
                    #

                    $count = ($name =~ tr/\.//);
                    if ($count == 3)
                    {
                        ($type,$subtype,$instance,$property) = split(/\./, $name, 4);

                        # Okay, now,
                        # Take apart variables.

                        if (substr($value,0,1) eq "\$")
                        {
                            #
                            #       Okay, we have a reference. First, take
                            #       apart the value, and then look it up.
                            #

                            ($t,$s,$i) = split(/\./,substr($value,1,length($value)-1),3);
                            $num_properties = @properties;
                            for ($j=0; $j < $num_properties; $j++)
                            {
                                if ($properties[$j]->{'type'} eq $t
                                && $properties[$j]->{'subtype'} eq $s
                                && $properties[$j]->{'instance'} eq $i)
                                {
                                    $property_name = $property . ":" . $properties[$j]->{'property'}; 
                                    $properties[$pnum] =
                                    {
                                        'type' => $type,
                                        'subtype' => $subtype,
                                        'instance' => $instance,
                                        'property' => $property_name, #$properties[$j]->{'property'},
                                        'value' => $properties[$j]->{'value'}
                                    };
                                    $pnum++;
                                }

                            }
                        }
                        else
                        {
                            $properties[$pnum] =
                            {
                                'type' => $type,
                                'subtype' => $subtype,
                                'instance' => $instance,
                                'property' => $property,
                                'value' => $value
                            };
                            $pnum++;
                        }
                    }
                    else
                    {
                        #printf "DEBUG: {configuration_load} (A) Incorrectly formed line# $lnum";
                        GWInstaller::AL::GWLogger::log("ERROR: Incorrectly formed line:\n$line");
                        return(null);
                    }
                }
                else
                {
                    #printf "DEBUG: {configuration_load} (B) Incorrectly formed line# $lnum";
                   GWInstaller::AL::GWLogger::log("ERROR:  Incorrectly formed line:\n$line");
                   
                    return(null);
                }
            }
        }
        $lnum++;
    }
    #printf "DEBUG: {configuration_load} Returning SUCCESS.";
    return(@properties);
}

sub shortname()
{
    my $host_name = hostname();
    my $domain_present = index($host_name, ".");

    if ($domain_present != -1)
    {
                $host_name = substr($host_name, 0, $domain_present);
    }
        return($host_name);
}

sub get_fqdn
{
	my ($self) = @_;
	my $fqdn_status = $self->get_fqdn_status();
	
 	if($fqdn_status eq ""){$fqdn_status = "shortname";}
	 
    my $fqdn;
    my $host_name = hostname();
    my $domain_present = index($host_name, ".");

	# Local Profile / shortname
 	if($fqdn_status eq "shortname")   {
        if ($domain_present != -1)
        {
            $fqdn = substr($host_name, 0, $domain_present);
        }
        else
        {
            $fqdn = $host_name;
        }
    }
    
    # Distributed Profile / FQDN
    else
    {
    	if($domain_present == -1){ 
	    	$fqdn = $host_name .".". $self->get_domain_name();
    	}
    	else{
    		$fqdn = $host_name;
    	}

    }

     GWInstaller::AL::GWLogger::log("fqdn: $fqdn");
    return $fqdn;
}

sub is_valid{
	$retVal = 0;
	if(-e "/usr/local/groundwork/enterprise/config/enterprise.properties"){
		$retVal = 1;
	}
	return $retVal;
}
 
 
1;