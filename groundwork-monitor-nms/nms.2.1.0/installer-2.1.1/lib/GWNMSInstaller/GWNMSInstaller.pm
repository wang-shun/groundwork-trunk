#!/usr/bin/perl
#
#Copyright 2009 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWNMSInstaller::GWNMSInstaller;
 
#use lib qw(../../); 
#use lib qw(../); 
use lib "./lib"; 
use Curses::UI;
use File::Basename;

use GWInstaller::AL::GWLogger;
use GWInstaller::AL::Prefs;
use GWInstaller::AL::GWMonitor;
use GWInstaller::AL::Host;
use GWInstaller::AL::httpd;

use GWNMSInstaller::AL::Core;
use GWNMSInstaller::AL::NeDi;
use GWNMSInstaller::AL::Cacti;
use GWNMSInstaller::AL::Weathermap;
use GWNMSInstaller::AL::ntop;
use GWNMSInstaller::AL::NMShttpd;

use GWInstaller::UI::GWCursesUI;
use GWInstaller::UI::Progress;
use GWInstaller::UI::SoftwareCursesUI;
use GWInstaller::UI::DBCursesUI;
use GWInstaller::UI::ListBox;
use GWInstaller::UI::DropdownMenu;

use GWNMSInstaller::UI::ComponentListBox;
use GWNMSInstaller::UI::NMSCursesUI;
use GWNMSInstaller::UI::HostConfigWin;
use GWNMSInstaller::UI::HostListBox;
use GWNMSInstaller::UI::NotebookWin;
use GWNMSInstaller::UI::DropdownConfigDialog;
use GWNMSInstaller::UI::databaseEditDialog;
use GWNMSInstaller::UI::gwmEditDialog;

use GWInstaller::AL::Properties;
use GWInstaller::AL::Software;
use GWInstaller::AL::Collection;
use GWInstaller::AL::Database;
use GWInstaller::AL::EventBroker;
use GWInstaller::AL::Foundation;

use GWNMSInstaller::AL::GWNMS;

use GWNMSInstaller::AL::CactiPackage;
use GWNMSInstaller::AL::NeDiPackage;
use GWNMSInstaller::AL::ntopPackage;
use GWNMSInstaller::AL::WeathermapPackage;
use GWNMSInstaller::AL::automationPackage;

use Error;
use Socket;

 my $sleep = 0;
our($prereq_path);
our($host);
 our($prefs);
 @conflicts = ();
 @prereqs = ();
  @oses = ();
 $host_os;
our($cui);
our($win);
our($gwmonitor);
our($whatami);
our($nmslog);
our $nms;
our $nmscui;
our $progress;
our $config;
our $properties;
 sub new{
	 
	my ($invocant) = @_;
	my $class = ref($invocant) || $invocant;
	 
   	$pRefr = {};
	my $self = {
		cui=>$cui,
		win=>$win,
		nmscui=>$nmscui,
		nmslog=>$nmslog,
		properties=>$properties,
		hostCollection=>$hostCollection,
		httpdCollection=>$httpdCollection,
		bronxCollection=>$bronxCollection,
		foundationCollection=>$foundationCollection,
		mysql_password=>$mysql_password
	};
 	#$host =  GWInstaller::AL::Host->new("localhost");
 
	bless($self,$class);
 	 $self->init();
 	#unless(init()){
 	#	$self = null;
 	#}
 	
	return $self;		
}
  sub init{
  	$self = shift;
	$date = `date`;
	$hostname = `hostname`; 
	chomp $hostname;
	$nmslog = GWInstaller::AL::GWLogger->new("nms.log");
	$self->{nmslog} = $nmslog;
	
	GWInstaller::AL::GWLogger::log("\n*********************************");
	GWInstaller::AL::GWLogger::log("Installer Started on " . $hostname . " on ". $date); 
 
 	$nmscui = GWNMSInstaller::UI::NMSCursesUI->new($self); #pass the installer to UI
 	$self->{nmscui} = $nmscui;
 	$prefs = GWInstaller::AL::Prefs->new();
	$prefs->load_software_prefs();
 	
	GWInstaller::AL::GWLogger::log("init HOSTNAME: $self->{hostname}");
 	$self->{properties} = GWNMSInstaller::AL::NMSProperties->new();
 	$properties = $self->{properties};
 	$properties->read_properties();
# 	$self->{prefs} = GWInstaller::AL::Prefs->new();
# 	$myRef = $self->{prefs};
# 	#%myObj = %$myRef;
# 	$tf = $myRef->isa(GWInstaller::AL::Prefs);
# #	$tf = {$self->{prefs}}->isa(GWInstaller::AL::Prefs);
# 	GWInstaller::AL::GWLogger::log("TF: $tf");
 
    $whatami =  $prefs->{software_name} . " " . $prefs->{software_class} . " " . $prefs->{version}; 
 	
  $cui =  Curses::UI->new(-color_support=>1,-clear_on_exit=>1);
   # print "init'd cursesUI..\n";
   $win = $cui->add('window_id', 'Window');
   $win->set_binding( sub {  my $return = $cui->dialog(
                        -message   => "Do you really want to quit? All unsaved configuration changes\nwill be lost.",
                        -title     => "Are you sure?", 
                        -selected  => 2,
                        -buttons   => ['yes', 'no'],

                );

        $self->exit() if $return; } , "\cQ");
        
        
    $self->{cui} = $cui;
   $self->{win} = $win;	 
   
  Curses::UI::Color::define_color("orange",255,153,51);
 # print "init'd cursesUI windowcolor..\n";
 # title text
  $win->add(
        'label', 'Label',
        -width         => -1,
        -paddingspaces => 1,
        -textalignment => 'middle',
        -text          => 'GroundWork NMS 2.1.1',
    );
 
   $win->add(
        'label2', 'Label',
        -width         => -1,
        -paddingspaces => 1,
        -y => 1,
        -textalignment => 'middle',
        -text          => '(c)2009 GroundWork Open Source, Inc.',
    );
 	
 	$progress = GWInstaller::UI::Progress->new($self,7000,24);
 	$self->{progress} = $progress;
 	
#   $cui->progress( -max       => 100 );
   $cui = $cui;
   GWInstaller::UI::GWCursesUI::status_msg('Initializing Installer...',$cui);
     $cui->set_binding( sub {  my $return = $cui->dialog(
                        -message   => "Do you really want to quit? All unsaved configuration changes\nwill be lost.",
                        -title     => "Are you sure?", 
                        -selected  => 2,
                        -buttons   => ['yes', 'no'],

                );

        $self->exit() if $return; } , "\cQ");
   #remove temporary files
   
 
   $nms = GWNMSInstaller::AL::GWNMS->new();
  
  #init gwmonitor object
  $gwmonitor = GWInstaller::AL::GWMonitor->new(); #when to create this object? earlier?
  
  
 }
  
sub get_properties{
	$self = shift;
	return $self->{properties};
}

sub is_locked{
	$self = shift;
	my $locked = 0;
	if(-e "./.lock"){
		$locked = 1;
	}
	return $locked;

}

sub lock{
	$self = shift;
	GWInstaller::AL::GWLogger::log("Creating lockfile for session...");
	print `touch ./.lock`;
	
}

sub unlock{
	$self = shift;
	GWInstaller::AL::GWLogger::log("Unlocking session...");
	print `rm -rf ./.lock`;
}

sub set_properties{
	my ($self,$prop) = @_;
	$self->{properties} = $prop;
}

#code duplication. this needs to be folded into config_or_abort with parameters passed.
sub select_or_create{
	my $self = shift;
	my $properties = $self->{properties};
	
 	$response = $nmscui->select_or_create_config();
 		GWInstaller::AL::GWLogger::log("selectorcreate RESP: $response");
 	
 	if($response eq "select"){
 		$configfile = $nmscui->filebrowse_config();
 		
 		if($configfile){
					GWInstaller::AL::GWLogger::log("User selected config file: $configfile");
				
					unless(-d "/usr/local/groundwork/enterprise/config"){ print `mkdir /usr/local/groundwork/enterprise/config >/dev/null 2>&1`;}
					#copy config file to /usr/local/groundwork/enterprise/config
					
					$cpcmd = "cp $configfile /usr/local/groundwork/enterprise/config/enterprise.properties >> nms.log 2>&1";
					unless(-d "/usr/local/groundwork"){ print `mkdir /usr/local/groundwork >/dev/null 2>&1 `;}				

					unless(-d "/usr/local/groundwork/enterprise"){ print `mkdir /usr/local/groundwork/enterprise >/dev/null 2>&1 `;}				
					unless(-d "/usr/local/groundwork/enterprise/config"){ print `mkdir /usr/local/groundwork/enterprise/config >/dev/null 2>&1 `;}				
					
					GWInstaller::AL::GWLogger::log("Copying Selected Config file to /usr/local/groundwork/enterprise/config/");
					print `$cpcmd`;	
					$properties->read_properties();
					}
		else{ #dialog canceled
 				$self->select_or_create();							
				}

 		 
 		
 	}
 	elsif($response eq "create"){
		  if(-e "/usr/local/groundwork/enterprise/config/enterprise.properties"){
 			 $is_accepted = $nmscui->yes_or_no_dialog("Overwrite","Are you sure you want to overwrite the existing enterprise.properties file?");
 			 if($is_accepted){
 			 	
 			 }
 			 else{
 			 	$self->select_or_create();
 			 }		
		  	}
 	}
 	elsif($response eq "abort"){
			$self->exit();
 		
 	}
 	
 }
sub config_or_abort{
 	$self = shift;
 	$sel_choice = $nmscui->select_config_or_abort();
 
	if($sel_choice eq "select"){
			$configfile = $nmscui->filebrowse_config();
 			if($configfile){
					GWInstaller::AL::GWLogger::log("User selected config file: $configfile");
					unless(-d "/usr/local/groundwork"){ print `mkdir /usr/local/groundwork >/dev/null 2>&1 `;}				
					unless(-d "/usr/local/groundwork/enterprise"){ print `mkdir /usr/local/groundwork/enterprise >/dev/null 2>&1 `;}				
					unless(-d "/usr/local/groundwork/enterprise/config"){ print `mkdir /usr/local/groundwork/enterprise/config >/dev/null 2>&1 `;}				
					#copy config file to /usr/local/groundwork/enterprise/config
					$cpcmd = "cp $configfile /usr/local/groundwork/enterprise/config/enterprise.properties >> nms.log 2>&1";
					GWInstaller::AL::GWLogger::log("Copying Selected Config file to /usr/local/groundwork/enterprise/config/");
					print `$cpcmd`;	
					$properties->read_properties();
					
					#do what config file says;
					$self->install_components();
					$self->exit();
					}
			else{ #dialog canceled
 				$self->config_or_abort();							
				}
			}
	elsif($sel_choice eq "abort"){
					$self->exit();
				}
 }
 sub run{
	$self = shift;
	my $cui = $self->{cui};
	my $nmscui = $self->{nmscui};
 	my $properties = $self->{properties};
	GWInstaller::AL::GWLogger::log("Scanning for showstoppers...");
	$self->scan_for_showstoppers();
	
	GWInstaller::AL::GWLogger::log("Verify environment..");
	$self->verify_environment();


   $self->fqdn_vs_shortname();
   

 		
 # 	GWInstaller::AL::GWLogger::log("HOSTNAME BEFORE:" . $self->{hostname});
 # 	GWInstaller::AL::GWLogger::log("HOSTNAME FQDN:" . $properties->get_domain_name());
 	GWInstaller::AL::GWLogger::log("HOSTNAME:" . $self->{hostname});
 	
	# has properties file
	###############################
	if($properties->exists()){
 		
    	
	   	#is it an upgrade or current
	   	my $installed_version = $nms->get_installed_version();
   		if($installed_version){
   			GWInstaller::AL::GWLogger::log("Found NMS version: $installed_version");
   		}
	   	#upgrade
	   	if($installed_version && ($installed_version < 2.1) ){
   			$yn = $nmscui->yes_or_no_dialog("Upgrade?","An older version of GroundWork NMS was discovered on this system. Do you wish to perform an upgrade?");
   			if($yn){
				$properties->read_properties();   				
 				#check mysql
				#############
				unless($self->mysql_can_connect()){
					$nmscui->error_dialog("Cannot connect to MySQL instance please refer to the GroundWork Bookshelf documentation for help in setting up MySQL");
					$self->exit();
				}  				
   				$self->remove_old_version();
   				
   			}
   			$properties->read_properties();
   			
   			$self->install_components();
   		    #successful upgrade message
   		    $nmscui->user_msg_dialog("upgraded","to Groundwork NMS 2.1");
   		    $properties->write_properties();
   		    $self->exit();
   		}
    
    	#if this IS THE MASTER
    	 if($properties->get_master() eq $self->{hostname}){
  			
  				#show error unless GWMONITOR is installed
##  				unless($gwmonitor->is_installed()){
##  					$nmscui->error_dialog("GroundWork Monitor Professional is required to be installed on the Master Configuration Server of your NMS deployment.");
##  					$self->exit();
##  				}
    	 }	
	  	#if   NOT master config server, prompt to perform actions
		else{   	
		  GWInstaller::AL::GWLogger::log("MASTER:" . $properties->get_master() . " HOSTNAME:" . $self->{hostname});
 	   			$msg = "The installer has detected a configuration file created on your Master Configuration Server (" . $properties->get_master() . "). Would you like the installer to perform the actions defined in your configuration file?";
   				$accepted = $nmscui->yes_or_no_dialog("Confirmation",$msg);
   				if($accepted){
   					#perform changes
   					GWInstaller::UI::GWCursesUI::status_msg("Scanning for Work...",$cui);
   					$self->install_components();
   					if($self->verify_install()){
   						$self->show_user_msg();
   					}
   					else{
   						GWInstaller::UI::GWCursesUI::error_msg("Errors were detected during this install. Please review the nms.log for more information.",$cui);	
						
   					}
   					
   				}
   				
				$self->exit(); 
  		   	
   			} #end is_not master
	}
	
	# Configuration Missing or Invalid
	##################################
	else{
		 
		  	 # Is this a single server Install?
			$nms->{is_single} = $nmscui->prompt_is_single();
			
			#Single Server Default Install
			if($nms->{is_single}){
				GWInstaller::AL::GWLogger::log("Single-System Install");
				$self->install_single();
   				if($self->verify_install()){
   						$self->show_user_msg();
   					}
   				else{
   						GWInstaller::UI::GWCursesUI::error_msg("Errors were detected during this install. Please review the nms.log for more information.",$cui);			
   					}
				$self->exit();
			}
			#Distributed Multi-System Install
			else{
				GWInstaller::AL::GWLogger::log("Multi-System Install");
		 		 	# Is this the master configuration Server?
 					$nms->{is_master} = $nmscui->prompt_is_master();			
  					
  					if($nms->{is_master}){
  						
 						$properties->set_master($self->{hostname});
 						GWInstaller::AL::GWLogger::log("MASTER=" . $properties->get_master() . " HOSTNAME=" . $self->{hostname});
 						
  						#show error unless GWMONITOR is installed
##  						unless($gwmonitor->is_installed()){
##  							$nmscui->error_dialog("GroundWork Monitor Professional is required to be installed on the Master Configuration Server of your NMS deployment.");
##  							$self->exit();
##  						}
 						
 						$self->select_or_create();
 					}	
 					else{
 						$self->config_or_abort();
					} 	
 			}# end Distributed Multi
 		}# ens is_valid
 
  
 

   	#if this IS the master config server, show configuration dialog
   	my $hostObj = GWInstaller::AL::Host->new($self->{hostname});
   	my $hc = $properties->get_collection("host");
   	$hostObj->set_identifier($self->{hostname});
   	if($hc->{array_size} == 0){ 
	   	$hc->add($hostObj);
   	}
   	$nmscui->hostlist_window($properties->get_collection("host"));
}

 sub fqdn_vs_shortname{ 
 	$self = shift;
 	$properties = $self->get_properties();
 	$nmscui = $self->{nmscui};
 	
 	#FQDN vs SHORTNAME
 	my $fqdn_status = $properties->get_fqdn_status();
 	
 	GWInstaller::AL::GWLogger::log("FQDN_STATUS: $fqdn_status");
 	#ask the user unless it is already set
 	unless($fqdn_status){
 	GWInstaller::AL::GWLogger::log("nostatus");

 		$fqdn_status = $nmscui->fqdn_status_dialog();				
  		
 		$properties->set_fqdn_status($fqdn_status);
 		
 		}
 		
 		# FQDN
 		if($fqdn_status eq "fqdn"){
 			my $domain_name = $nmscui->get_fqdn_dialog(); 
 			$properties->set_domain_name($domain_name);
 			$self->{deploy_profile} = "distributed";
 			$self->{hostname} = $properties->get_fqdn();
 
 			
		 	#unless($self->{hostname} =~ /$properties->get_domain_name()/){
 			#	$self->{hostname} = $self->{hostname} . "." . $properties->get_domain_name();
 			#}
 		}
 		
 		# SHORTNAME
 		else{
 			$self->{deploy_profile} = "local";
 			my $shortname = `hostname -s`;
 			chomp($shortname);
 			$self->{hostname} = $shortname;
 		}
 } 	
 
sub install_single{
	$self = shift;
	my $cui = $self->{cui};
	my $nmscui = $self->{nmscui};
	$properties->set_master($self->{hostname}); #single install can only be done on master
	my $mysql_password;
	#Get password if necessary
    if(GWInstaller::AL::Host::mysql_passwd_set()){
    	  $mysql_password = GWInstaller::UI::DBCursesUI::mysql_dialog($cui);
 		$self->{mysql_password} = $mysql_password;    	
    }

	#Try out MySQL connection
	if(GWInstaller::AL::Host::mysql_can_connect($mysql_password)){
		GWInstaller::UI::GWCursesUI::status_msg("\tConnection to MySQL: OK",$cui);
	}
	else{
		$msg = "Unable to connect to MySQL instance. Please refer to log for more information.\n" . 
				"Restarting the MySQL service may remedy this problem.";
		GWInstaller::AL::GWLogger::log($msg);
		$nmscui->error_dialog($msg);	
		$self->exit();
	}
	
	#confirmation screen (abort or continue)
	$is_confirmed = $nmscui->confirm_install();
	unless($is_confirmed){
		$self->exit();
	}

#	$self->install_components();
	
	$self->install_local_components();
}


sub do_component_changes{
	$self = shift;
	my @components = @_ ;

	my $installer = $self; #->{installer};
	my $cui = $installer->{cui};
	my $progbar = $installer->{progress};
	
	$progbar->reset();
	$changeditems=0;
	foreach $check (@components){
		if($check =~ "TO BE"){ 
			$changeditems++;
		}
	}
 	$progbar->set_total_steps($changeditems);
		
	#my $progbar = GWInstaller::UI::Progress->new($self,7000,$changeditems);
	
	foreach $comp (@components){
		($item,$status) = split(/\.+/,$comp);
		if($status eq "TO BE INSTALLED"){
			unless(GWInstaller::AL::Host::is_rpm_installed("groundwork-nms-core")){
				$changeditems++;
				$progbar->set_total_steps($changeditems);
				$progbar->increment("Installing Prerequisite groundwork-nms-core");
				GWInstaller::AL::Host::install_rpm("groundwork-nms-core");
			}
			$progbar->increment("Installing $item...");
			GWInstaller::AL::Host::install_rpm($item);
			$status = "INSTALLED";

			$updatedfiledir = "/tmp/nms_src_tmp";
			`cp -r ./src $updatedfiledir`;
			`chown nagios:nagios  $updatedfiledir/*`;
			`chmod +x $updatedfiledir/*`;
			`cp -pf $updatedfiledir/deploy_application*.pl /usr/local/groundwork/enterprise/bin/components`;
			`cp -pf $updatedfiledir/deploy.pl /usr/local/groundwork/enterprise/bin`;
			`cp -pf $updatedfiledir/check_cacti.pl /usr/local/groundwork/enterprise/plugins/cacti/scripts`;

#			$cpcmd = "cp -pf $updatedfiledir/deploy_application*.pl /usr/local/groundwork/enterprise/bin/components";
#			$cpcmd = "cp -pf $updatedfiledir/deploy.pl /usr/local/groundwork/enterprise/bin";
#			$cpcmd = "cp -pf $updatedfiledir/check_cacti.pl /usr/local/groundwork/enterprise/plugins/cacti/scripts";

			$pushme = $nmscui->format_listitem($item,35,$status);
			push(@retarray,$pushme);
		}
		elsif($status eq "TO BE UNINSTALLED"){
			
			if($nmscui->{corelast} && $item eq "groundwork-nms-core"){
				
				next;
			} 
		 			
			$progbar->increment("Removing $item...");
			GWInstaller::AL::Host::uninstall_rpm($item);
			$status = "NOT INSTALLED";
			
			if($item eq "groundwork-nms-cacti"){			
				GWNMSInstaller::AL::Cacti::remove_gwm_application();
			}
			elsif($item eq "groundwork-nms-ntop"){
				GWNMSInstaller::AL::ntop::remove_gwm_application();				
			}
			elsif($item eq "groundwork-nms-weathermap"){
				GWNMSInstaller::AL::Weathermap::remove_gwm_application();				
			}
			elsif($item eq "groundwork-nms-nedi"){
				GWNMSInstaller::AL::NeDi::remove_gwm_application();				
			}
			$pushme = $nmscui->format_listitem($item,35,$status);
			push(@retarray,$pushme);
		}
 
		else{
			push(@retarray,$comp);
		}
		
 
	} #end foreach $comp (@components)

	# Do core remove now	
	if($nmscui->{corelast}){
		
		$progbar->increment("Removing groundwork-nms-core...");
		
		GWInstaller::AL::Host::uninstall_rpm('groundwork-nms-core');
		$nmscui->{corelast} = 0;
		}
	$progbar->hide();
	#return @retarray;
	#GWInstaller::AL::GWLogger::log("CHANGED ITEM COUNT: " . $changeditems);
	
	return $changeditems;
}

sub mysql_can_connect{
	my $self = shift;
	my $properties = $self->get_properties();
	my $dbCollection = $properties->get_collection("database");
	
	GWInstaller::AL::GWLogger::log("dbcollection ref " . ref $dbCollection . " size:" . $dbCollection->{array_size});
	while($dbCollection->has_next()){
		$dbObj = $dbCollection->next();
		GWInstaller::AL::GWLogger::log("dbobj-host:" . $dbObj->get_host() . " FQDN:" . $properties->get_fqdn());
		
# I am commenting out this get_host to get_fqdn comparison, as I don't understand
# what its purpose is, and the end-result is that we never succeed in calling can_connect.
#		if($dbObj->get_host() eq $properties->get_fqdn()){
		
			$status =  $dbObj->can_connect($properties);
			GWInstaller::AL::GWLogger::log("MYSQL STATUS: $status");
			return $status;
#		}
	}
	GWInstaller::AL::GWLogger::log("no");

	return 0;
}
sub install_components{
	my $self = shift;
 	my $nmscui = $self->{nmscui};
 	my $cui = $self->{cui};
	my $properties = $self->{properties};
	eval{  
	#check mysql
	#############
	unless($self->mysql_can_connect()){
		$nmscui->error_dialog("Cannot connect to MySQL instance please refer to the GroundWork Bookshelf documentation for help in setting up MySQL");
		$self->exit();
	}
	};
	if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");$self->exit();}
	 	if($debug){ GWInstaller::AL::GWLogger::log("install_components(" . $self->get_localhost() . ")");}
	
	my $hostCollection = $properties->get_collection("host");
	if($debug){ GWInstaller::AL::GWLogger::log("hostColl CLASS:" . ref $hostCollection);}

	my $hostObj = $hostCollection->get_by_identifier($self->get_localhost());
	if($debug){ GWInstaller::AL::GWLogger::log("hostObj CLASS:" . ref $hostObj);}

	my $componentCollection = $hostObj->get_component_collection($properties);
	if($debug){ GWInstaller::AL::GWLogger::log("compColl CLASS:" . ref $componentCollection);}
	 
	#get the install collection See what needs to be done
	my $installCollection = $hostObj->get_install_collection($properties);
	if($debug){ GWInstaller::AL::GWLogger::log("compColl CLASS:" . ref $componentCollection);}
	
	#if there are no installs or all removals, dont bother installing core or httpd.
	my $will_install = 0;
	my $do_core_last = 0;
	while($installCollection->has_next()){
		my $compObj = $installCollection->next();
		if($compObj->get_do_install()){
			$will_install=1;
		}
	}
	
	#If any components are selected, make sure core/httpd are installed.
	if($will_install){ 		
		$self->install_core();
 	}
	else{
		$do_core_last = 1;		
	}
	
	$installCollection->reset_index(); #prob unecessary
	
	while($installCollection->has_next()){
		my $compObj = $installCollection->next();
		my $component_name = $compObj->get_identifier();
	
		my $newid = $component_name . "_" . $properties->get_fqdn();
 		$compObj->set_identifier($newid);
		if($compObj->get_do_install()){
			$actionType = "install";
		}
		else{
			$actionType = "remove";
		}
		# take the action indicated: install/remove
		my $method_name = $actionType . "_" . $component_name;		
		$self->$method_name();

	}
	
 	if($do_core_last){ 
		$self->remove_core();
	}
	
 
	
	$self->show_changes();
}

sub install_httpd{
	my $self = shift;
	my $progress = $self->{progress};
	#httpd deploy
	#############
	$progress->increment('Deploying httpd...');
	GWNMSInstaller::AL::NMShttpd::deploy($self->{deploy_profile},$self);
	
}
sub show_changes{
	my $self = shift;
	my $cui = $self->{cui};
	my $hostCollection = $properties->get_collection("host");
	GWInstaller::AL::GWLogger::log("hostColl CLASS:" . ref $hostCollection);

	my $hostObj = $hostCollection->get_by_identifier($self->get_localhost());
	GWInstaller::AL::GWLogger::log("hostObj CLASS:" . ref $hostObj);

		my $componentCollection = $hostObj->get_component_collection($properties);
	
		#Scanning for changes;
		$cui->nostatus();
		GWInstaller::UI::GWCursesUI::status_msg("Scanning for changes...",$cui);
		$componentCollection->reset_index();
		my @inst_array;
	
		while($componentCollection->has_next()){
			my $installObj = $componentCollection->next();
	#		my $component_name = $installObj->get_name();
			eval{ 
				if($installObj->is_installed()){
					push(@inst_array,$installObj->get_name());	
				}
			};	
		}
	
		my $arsize = @inst_array;
		if($arsize > 0){  	
			$msg = "The following ($arsize) NMS Components are currently installed on this system:\n";
			foreach $item (@inst_array){ 
				if($item =~ 'core'){next;}
				$msg .= "\t* $item\n";
			}
		}
		else{
			$msg = "There are no NMS Components currently installed on this system.\n";
		}
		$nmscui->info_dialog($msg);	
}
sub get_localhost{
	my($self,$type) = @_;
	#my $hostname = GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn($type); #local or no?
	
	return $self->{hostname};
}
sub remove_nedi{
	my($self) = shift;
	my $progress = $self->{progress};
 	
	#nedi
	#####
	$nedi = GWNMSInstaller::AL::NeDi->new();	
	$nediCollection = $properties->get_collection("nedi");
	while($nediCollection->has_next()){
		$nedi_tmp = $nediCollection->next();
		if($nedi_tmp->get_host() eq $self->{hostname}){
			$nedi = $nedi_tmp;
			last;
		}
	}
	unless($nedi->isa(GWNMSInstaller::AL::NeDi)){
 			$nmscui->error_dialog("Error initializing NeDi RPM");
 			$self->exit();		
	}		

	if($nedi->is_installed()){

		$progress->increment('Removing NeDi RPM...');
		$nedi->uninstall();
		$progress->increment('Verifying Removal of NeDi RPM...');
		
		if($nedi->is_installed()){
			$self->install_failed();
		}
	GWNMSInstaller::AL::NeDi::remove_gwm_application($self);

	}
	
}
sub install_nedi{
	my($self) = shift;
	my $progress = $self->{progress};
	
	#nedi
	#####
	$nedi = GWNMSInstaller::AL::NeDi->new();	
	$nediCollection = $properties->get_collection("nedi");
	while($nediCollection->has_next()){
		$nedi_tmp = $nediCollection->next();
		if($nedi_tmp->get_host() eq $self->{hostname}){
			$nedi = $nedi_tmp;
			last;
		}
	}

	$objtype = ref $nedi;

	#install rpm
	unless($objtype eq "GWNMSInstaller::AL::NeDi"){
 			#$nmscui->error_dialog("Error initializing NeDi RPM");
 			$nmslog->log("NeDi bad: $objtype");
 			$self->exit();				
	}
	
	unless($nedi->is_installed()){ 
		$progress->increment('Installing NeDi RPM...');
	
		$nedi->install();
		$progress->increment('Verifying Install of NeDi RPM...');
		unless($nedi->is_installed()){
			$self->install_failed();
		}	
	
		#deploy
		$progress->increment('Deploying NeDi RPM...');
		$nedi->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Deploy of NeDi RPM...');
		unless($nedi->is_functional()){
			$self->install_failed();
		}
	
		#install GWM Package (guava)
		$progress->increment('Installing NeDi GroundWork Monitor Application...');
		$nedi->deploy_package($self->{deploy_profile},$self);
		
	}
}

sub remove_core{
	my($self) = shift;
	my $progress = $self->{progress};
 	
	#core
	#####
	$core = GWNMSInstaller::AL::Core->new();

	unless($core->isa(GWNMSInstaller::AL::Core)){
 			$nmscui->error_dialog("Error initializing Core RPM");
 			$self->exit();		
	}		

	if($core->is_installed()){

		$progress->increment('Removing Core RPM...');
		$core->uninstall();
		$progress->increment('Verifying Removal of Core RPM...');
		
		if($core->is_installed()){
			$self->install_failed();
		}
	}
	
}

sub install_core{
	my($self) = shift;
	my $progress = $self->{progress};
 	
	#core
	#####
	$core = GWNMSInstaller::AL::Core->new();

	unless($core->isa(GWNMSInstaller::AL::Core)){
 			$nmscui->error_dialog("Error initializing Core RPM");
 			$self->exit();		
	}		

	unless($core->is_installed()){

		$progress->increment('Installing Core RPM...');

		$core->install();
		$progress->increment('Verifying Install of Core RPM...');
		
	
                unless($core->is_installed()){
                        $self->install_failed();
                }


		print `cp -rp ./src /tmp/nms_src_tmp`;
		print `chown nagios:nagios /tmp/nms_src_tmp/*`;
		print `chmod +x /tmp/nms_src_tmp/*`;
		print `cp -pf /tmp/nms_src_tmp/deploy_application*.pl /usr/local/groundwork/enterprise/bin/components`;
		print `cp -pf /tmp/nms_src_tmp/deploy.pl /usr/local/groundwork/enterprise/bin`;
		print `cp -pf /tmp/nms_src_tmp/check_cacti.pl /usr/local/groundwork/enterprise/bin/components/plugins/cacti/scripts`;
		print `cp -pf /tmp/nms_src_tmp/extract_cacti.pl /usr/local/groundwork/nms/tools/automation/scripts`;
		print `cp -pf /tmp/nms_src_tmp/extract_nedi.pl /usr/local/groundwork/nms/tools/automation/scripts`;
		print `cp -pf /tmp/nms_src_tmp/cacti_cron.sh /usr/local/groundwork/common/bin`;
		print `cp -pf /tmp/nms_src_tmp/nms-httpd /usr/local/groundwork/enterprise/bin/components/httpd`;
		print `cp -pf /tmp/nms_src_tmp/nms-httpd /usr/local/groundwork/nms/tools/installer/httpd`;
		print `rm -rf /tmp/nms_src_tmp`;


		$progress->increment('Deploying Core RPM...');
		$core->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Core Deploy...');
		unless($core->is_functional()){
			$self->install_failed();
		}


		$self->install_httpd();
		
	} #end unless $core->is_installed()
	
	my $properties = $self->{properties};
	$properties->write_properties();	
}

sub remove_old_version{
	my $self = shift;
	my $progress = $self->{progress};
	$progress->reset();
		
	$self->remove_ntop();
	$self->remove_weathermap();
	$self->remove_cacti();
	$self->remove_nedi();
	$self->remove_core();
	
}
sub remove_ntop{
	my($self) = shift;
	my $progress = $self->{progress};
 	
	#ntop
	#####
	$ntop = GWNMSInstaller::AL::ntop->new();	
	$ntopCollection = $properties->get_collection("ntop");
	while($ntopCollection->has_next()){
		$ntop_tmp = $ntopCollection->next();
		if($ntop_tmp->get_host() eq $self->{hostname}){
			$ntop = $ntop_tmp;
			last;
		}
	}
	unless($ntop->isa(GWNMSInstaller::AL::ntop)){
 			$nmscui->error_dialog("Error initializing  ntop RPM");
 			$self->exit();		
	}		

	if($ntop->is_installed()){

		$progress->increment('Removing ntop RPM...');
		$ntop->uninstall();
		$progress->increment('Verifying Removal of ntop RPM...');
		
		if($ntop->is_installed()){
			$self->install_failed();
		}
		
	GWNMSInstaller::AL::ntop::remove_gwm_application($self);

	}
	
}
sub install_ntop{
	my($self) = shift;
	my $progress = $self->{progress};
	#ntop
	#####
	$ntop = GWNMSInstaller::AL::ntop->new();	
	$ntopCollection = $properties->get_collection("ntop");
	while($ntopCollection->has_next()){
		$ntop_tmp = $ntopCollection->next();
		if($ntop_tmp->get_host() eq $self->{hostname}){
			$ntop = $ntop_tmp;
			last;
		}
	}
	
	unless($ntop->is_installed){ 
		$progress->increment('Installing ntop RPM...');
		$ntop->install();
		$progress->increment('Verifying Install of ntop RPM...');
		unless($ntop->is_installed()){
			$self->install_failed();
		}
		
		
		#deploy
		$progress->increment('Deploying ntop RPM...');
		$ntop->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Deploy of ntop RPM...');
		unless($ntop->is_functional()){
			$self->install_failed();
		}
		
		#install GWM Package (guava)
		$progress->increment('Installing ntop GroundWork Monitor Application...');
		$ntop->deploy_package($self->{deploy_profile},$self);
	}
}
sub remove_cacti{
	my($self) = shift;
	my $progress = $self->{progress};
 	
	#cacti
	#####
	$cacti = GWNMSInstaller::AL::Cacti->new();	
	$cactiCollection = $properties->get_collection("cacti");
	while($cactiCollection->has_next()){
		$cacti_tmp = $cactiCollection->next();
		if($cacti_tmp->get_host() eq $self->{hostname}){
			$cacti = $cacti_tmp;
			last;
		}
	}
	unless($cacti->isa(GWNMSInstaller::AL::Cacti)){
 			$nmscui->error_dialog("Error initializing Core RPM");
 			$self->exit();		
	}		

	if($cacti->is_installed()){

		$progress->increment('Removing Cacti RPM...');
		$cacti->uninstall();
		$progress->increment('Verifying Removal of Cacti RPM...');
		
		if($cacti->is_installed()){
			$self->install_failed();
		}
	GWNMSInstaller::AL::Cacti::remove_gwm_application($self);

	}
	
}
sub install_cacti{
	my($self) = shift;
	my $progress = $self->{progress};
	
	#cacti
	######
	$cacti = GWNMSInstaller::AL::Cacti->new();	
	$cactiCollection = $properties->get_collection("cacti");
	while($cactiCollection->has_next()){
		$cacti_tmp = $cactiCollection->next();
		if($cacti_tmp->get_host() eq $self->{hostname}){
			$cacti = $cacti_tmp;
			last;
		}
	}
	unless($cacti->is_installed()){ 
		$progress->increment('Installing Cacti RPM...');

		$cacti->install();
		$progress->increment('Verifying Install of Cacti RPM...');
		unless($cacti->is_installed()){
			$self->install_failed();
		}

		$progress->increment('Deploying Cacti RPM...');
		$cacti->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Deploy of Cacti RPM...');
		unless($cacti->is_functional()){
			$self->install_failed();
		}

		#install GWM Package (guava)
		$progress->increment('Installing Cacti GroundWork Monitor Application...');
		$cacti->deploy_package($self->{deploy_profile},$self);
	}		
}
sub remove_weathermap{
	my($self) = shift;
	my $progress = $self->{progress};
 	my $properties = $self->get_properties();
	#weathermap
	#####
	$weathermap = GWNMSInstaller::AL::Weathermap->new();	
	$weathermapCollection = $properties->get_collection("weathermap");
	while($weathermapCollection->has_next()){
		$weathermap_tmp = $weathermapCollection->next();
		if($weathermap_tmp->get_host() eq $self->{hostname}){
			$weathermap = $weathermap_tmp;
			last;
		}
	}	
	$weatherclass = ref $weathermap;
	unless($weatherclass eq "GWNMSInstaller::AL::Weathermap"){
 			$nmscui->error_dialog("Error initializing Core RPM");
 			$self->exit();		
	}		

	if($weathermap->is_installed()){

		$progress->increment('Removing Weathermap RPM...');
		$weathermap->uninstall();
		$progress->increment('Verifying Removal of Weathermap RPM...');
		
		if($weathermap->is_installed()){
			$self->install_failed();
		}
	GWNMSInstaller::AL::Weathermap::remove_gwm_application($self);

	}
	
}
sub install_weathermap{
	my($self) = shift;
	my $progress = $self->{progress};

	#weathermap
	###########
	$weather = GWNMSInstaller::AL::Weathermap->new();	
	$weathermapCollection = $properties->get_collection("Weathermap");
	while($weathermapCollection->has_next()){
		$weathermap_tmp = $weathermapCollection->next();
		if($weathermap_tmp->get_host() eq $self->{hostname}){
			$weather = $weathermap_tmp;
			last;
		}
	}	
	unless($weather->is_installed()){ 	
		#install rpm
		$progress->increment('Installing Weathermap RPM...');
		$weather->install();
		$progress->increment('Verifying Install of Weathermap RPM...');
		unless($weather->is_installed()){
			$self->install_failed();
		}
		
		#deploy
		$progress->increment('Deploying Weathermap RPM...');
		$weather->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Deploy of Weathermap RPM...');
		unless($weather->is_functional()){
			$self->install_failed();
		}
	
		#install GWM Package (guava)
		$progress->increment('Installing Weathermap GroundWork Monitor Application...');
		$weather->deploy_package($self->{deploy_profile},$self);
		
		GWInstaller::UI::GWCursesUI::status_msg("Saving a copy of installer and log",$cui);
		backup_installer();	
	}
}

sub install_local_components{
	my $self = shift;
#	my @component_list = $self->component_list;
	my $nmscui = $self->{nmscui};
	$properties = $self->get_properties();
 	$progress = $self->{progress};	
 	
	#close sessions and clean up tmp files in GW Monitor
	#$gwmonitor->close_sessions();
	eval{  
	#check mysql
	#############
	my $dbObj = GWInstaller::AL::Database->new();
	$dbObj->set_host($properties->get_fqdn());
	$dbObj->set_root_password($self->{mysql_password});
	my $dbColl = $properties->get_collection("database");
	$dbColl->add($dbObj);
	if($self->mysql_can_connect()){
		GWInstaller::AL::GWLogger::log("Connection to MySQL OK");
	}
	else{
		$nmscui->error_dialog("Cannot connect to MySQL instance please refer to the GroundWork Bookshelf documentation for help in setting up MySQL");
		$self->exit();
	}
	};
	if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");$self->exit();}
	 
 	#check for gw monitor (required)
 	#################################
 	
##	if($gwmonitor->is_installed()){		
## 		$msg = "Found GroundWork Monitor " . $gwmonitor->{software_class} . " " . $gwmonitor->{version};
##    	GWInstaller::UI::GWCursesUI::status_msg($msg,$cui);	
    	
##    	if($gwmonitor->{software_class} eq "Community Edition"){
##    		$msg = "GroundWork NMS may not be installed with GroundWork Monitor Community Edition. Please upgrade to GroundWork Monitor Professional or GroundWork Monitor Enterprise before continuing."; 
##    		$nmscui->error_dialog($msg);
##    	} 	
##	}
##	else{
##		$msg = "GroundWork Monitor Professional is required for a Default (Single Server) NMS installation. Please Install GroundWork Monitor before continuing.";
##		$nmscui->error_dialog($msg);
##		$self->exit();
##	}
 
 
 	
	#core
	#####
		$core = GWNMSInstaller::AL::Core->new();

	unless($core->is_installed()){

		$progress->increment('Installing Core RPM...');
	
		unless($core->isa(GWNMSInstaller::AL::Core)){
	 			$nmscui->error_dialog("Error initializing Core RPM");
	 			$self->exit();		
		}
		$core->install();
		$progress->increment('Verifying Install of Core RPM...');
		
		unless($core->is_installed()){
			$self->install_failed();
		}


		print `cp -rp ./src /tmp/nms_src_tmp`;
		print `chown nagios:nagios /tmp/nms_src_tmp/*`;
		print `chmod +x /tmp/nms_src_tmp/*`;
		print `cp -pf /tmp/nms_src_tmp/deploy_application*.pl /usr/local/groundwork/enterprise/bin/components`;
		print `cp -pf /tmp/nms_src_tmp/deploy.pl /usr/local/groundwork/enterprise/bin`;
		print `cp -pf /tmp/nms_src_tmp/check_cacti.pl /usr/local/groundwork/enterprise/bin/components/plugins/cacti/scripts`;
		print `cp -pf /tmp/nms_src_tmp/extract_cacti.pl /usr/local/groundwork/nms/tools/automation/scripts`;
		print `cp -pf /tmp/nms_src_tmp/extract_nedi.pl /usr/local/groundwork/nms/tools/automation/scripts`;
		print `cp -pf /tmp/nms_src_tmp/cacti_cron.sh /usr/local/groundwork/common/bin`;
		print `cp -pf /tmp/nms_src_tmp/nms-httpd /usr/local/groundwork/enterprise/bin/components/httpd`;
		print `cp -pf /tmp/nms_src_tmp/nms-httpd /usr/local/groundwork/nms/tools/installer/httpd`;
		print `rm -rf /tmp/nms_src_tmp`;


		#read in properties
		$properties->read_properties();
					GWInstaller::AL::GWLogger::log("after read ");
		
		$properties->{master} = $self->{hostname};
			GWInstaller::AL::GWLogger::log("setting master to=$self->{hostname} ");
			GWInstaller::AL::GWLogger::log("setting master to=" . $properties->get_master());
	

	# After Installing Core, Update Mysql settings
	###############################################
	my $mysql_password = $self->{mysql_password};

	GWInstaller::AL::GWLogger::log("MySQLPassword=$mysql_password ");
	if($mysql_password ne ""){  
			
			
		
		#get database collection
		my $dbCollection = $properties->get_collection("database");
		
		#get known mysql object from default properties file
		my $mysqlObj = $dbCollection->get_by_identifier("mysql_main")	;
		
		#set mysql password
		$mysqlObj->set_root_password($mysql_password);
		
	}
	


		#write out new enterprise.properties file
		$properties->write_properties();



		$progress->increment('Deploying Core RPM...');
		$core->deploy($self->{deploy_profile},$self);
		$progress->increment('Verifying Core Deploy...');
		unless($core->is_functional()){
			$self->install_failed();
		}
	}	
			
	#httpd deploy
	#############
	$progress->increment('Deploying httpd...');
	GWNMSInstaller::AL::NMShttpd::deploy($self->{deploy_profile},$self);

	#nedi
	#####
	$progress->increment('Installing NeDi RPM...');
	
	$nedi = GWNMSInstaller::AL::NeDi->new();	
	$objtype = ref $nedi;

	#install rpm
	unless($objtype eq "GWNMSInstaller::AL::NeDi"){
 			#$nmscui->error_dialog("Error initializing NeDi RPM");
 			$nmslog->log("NeDi bad: $objtype");
 			$self->exit();				
	}
	$nedi->install();
	$progress->increment('Verifying Install of NeDi RPM...');
	unless($nedi->is_installed()){
		$self->install_failed();
	}	
	
	#deploy
	$progress->increment('Deploying NeDi RPM...');
	$nedi->deploy($self->{deploy_profile},$self);
	$progress->increment('Verifying Deploy of NeDi RPM...');
	unless($nedi->is_functional()){
		$self->install_failed();
	}
	
	#install GWM Package (guava)
	$progress->increment('Installing NeDi GroundWork Monitor Application...');
	$nedi->deploy_package($self->{deploy_profile},$self);
		
 	
	
	#ntop
	#####
	
	#rpm
	$progress->increment('Installing ntop RPM...');
	$ntop = GWNMSInstaller::AL::ntop->new();
	$ntop->install();
	$progress->increment('Verifying Install of ntop RPM...');
	unless($ntop->is_installed()){
		$self->install_failed();
	}
	
	#deploy
	$progress->increment('Deploying ntop RPM...');
	$ntop->deploy($self->{deploy_profile},$self);
	$progress->increment('Verifying Deploy of ntop RPM...');
	unless($ntop->is_functional()){
		$self->install_failed();
	}
	
	#install GWM Package (guava)
	$progress->increment('Installing ntop GroundWork Monitor Application...');
	$ntop->deploy_package($self->{deploy_profile},$self);


	#cacti
	######
	$progress->increment('Installing Cacti RPM...');
	$cacti  = GWNMSInstaller::AL::Cacti->new();
	$cacti->install();
	$progress->increment('Verifying Install of Cacti RPM...');
	unless($cacti->is_installed()){
		$self->install_failed();
	}
	$progress->increment('Deploying Cacti RPM...');
	$cacti->deploy($self->{deploy_profile},$self);
	$progress->increment('Verifying Deploy of Cacti RPM...');
	unless($cacti->is_functional()){
		$self->install_failed();
	}

	#install GWM Package (guava)
	$progress->increment('Installing Cacti GroundWork Monitor Application...');
	$cacti->deploy_package($self->{deploy_profile},$self);
	
	#weathermap
	###########
	
	#install rpm
	$progress->increment('Installing Weathermap RPM...');
	$weather = GWNMSInstaller::AL::Weathermap->new();
	$weather->install();
	$progress->increment('Verifying Install of Weathermap RPM...');
	unless($weather->is_installed()){
		$self->install_failed();
	}
	
	#deploy
	$progress->increment('Deploying Weathermap RPM...');
	$weather->deploy($self->{deploy_profile},$self);
	$progress->increment('Verifying Deploy of Weathermap RPM...');
	unless($weather->is_functional()){
		$self->install_failed();
	}
	
	#install GWM Package (guava)
	$progress->increment('Installing Weathermap GroundWork Monitor Application...');
	$weather->deploy_package($self->{deploy_profile},$self);
	
	GWInstaller::UI::GWCursesUI::status_msg("Saving a copy of installer and log",$cui);
	backup_installer();
}


sub deploy_local_components{
	my $self = shift;
	my @component_list = $self->component_list;
	 
	foreach $component(@component_list){
		
		unless($component->deploy($self->{deploy_profile},$self)){
			#NMSCursesUI->error_dialog(Unable to deploy);
			
		}
	}
}
 
 

sub scan_for_showstoppers{
	my $self = shift;
	my $nmscui = $self->{nmscui};
	#return 0;
GWInstaller::UI::GWCursesUI::status_msg("Scanning for showstoppers...",$cui);
# Must be ROOT 
#################
$user = `whoami`;
chomp($user);
if($user eq "root"){
	GWInstaller::UI::GWCursesUI::status_msg("\tUser is root",$cui);
 }
else{

    GWInstaller::UI::GWCursesUI::error_msg("You must be root to run this installer. Aborting Install!",$cui);
    $self->exit();
}

if($self->is_locked()){
	$nmscui->error_dialog("The installer has detected another session. Only one user may run the installer at a time.");
	$self->exit("locked"); #exit but don't unlock	
}
else{
	$self->lock();
}

# CHECK SE LINUX STATUS
########################
GWInstaller::UI::GWCursesUI::status_msg("\tChecking for SE Linux",$cui);
if(GWInstaller::AL::Host::is_selinux_enabled()){

  $errmsg ="You must fully disable SELinux before installing GroundWork Monitor. " .
                "Please edit /etc/selinux/config, change the value to: " .
                "SELINUX=disabled and reboot your computer. You may re-enable it after completing the installation.";
                
                GWInstaller::UI::GWCursesUI::error_msg($errmsg,$cui);
                
                $self->exit();
}
GWInstaller::UI::GWCursesUI::status_msg("\tSE Linux is disabled",$cui);

# if package incomplete, bail
###############################
if(GWNMSInstaller::AL::GWNMS::is_complete()){
	GWInstaller::UI::GWCursesUI::status_msg("\tGW-NMS Installation package is complete",$cui);
}
else{
	GWInstaller::UI::GWCursesUI::error_msg('There are files missing from this installation package. Please download it again.',$cui);
 	$self->exit();
}
 
 

 
}


################################################
### VERIFY ENVIRONMENT MEETS RECOMMENDATIONS ###
################################################ 
sub verify_environment{
	$self = shift;
	my $nmscui = $self->{nmscui};
	GWInstaller::UI::GWCursesUI::status_msg('Verifying Environment...',$cui);
	# MINIMUM HARDWARE
	###################
	GWInstaller::UI::GWCursesUI::status_msg("Verifying minimum hardware requirements...",$cui);
	sleep $sleep;
	verify_hardware();


	# OS REQUIREMENT
	##################
	GWInstaller::UI::GWCursesUI::status_msg("Verifying operating system requirements...",$cui);
 	
 	$hostOS = GWInstaller::AL::Host::get_os();
 	GWInstaller::UI::GWCursesUI::status_msg("\tHost Operating System is:\n $hostOS",$cui);
	$hostStatus = GWInstaller::AL::GWMonitor::get_os_status($hostOS);


## 	if($prefs->{'software_class'} eq "Community Edition"){
## 			$msg = "This software can not be installed on a system running GroundWork monitor Community Edition.\nPlease install GroundWork Monitor Professional before continuing.";
## 			$nmscui->error_dialog($msg);
## 			$self->exit();
 			
 					
## 		}

##	if($gwmonitor->is_installed()){ 
#		GWInstaller::UI::GWCursesUI::status_msg("Found GroundWork Monitor; Checking Version...",$cui);
#		unless($self->is_gwm_valid()){
# 			$msg = "NMS 2.1 requires GroundWork Monitor: 5.2.1 (Service Pack 6) or higher.\nPlease upgrade before continuing.";
# 			$nmscui->error_dialog($msg);
# 		
# 			$self->exit();		
#		}
##	}
	
	if($hostStatus eq "production"){
		GWInstaller::UI::GWCursesUI::status_msg("\tHost OS is Certified for Production Use",$cui);
	}
	elsif($hostStatus eq "eval"){
		GWInstaller::UI::GWCursesUI::status_msg("\tHost OS is Certified for Evaluation Use",$cui);
		$msg = "$hostOS is not certified for Production Use. It is recommended for Evaluation installations.\n Do you wish to proceed with the installation?";
		unless($nmscui->yes_or_no_dialog("Warning- Not Certified for Production Use",$msg)){
			$self->exit();
		}

				

	}
	elsif($hostStatus eq "unsupported"){
		$msg = "$hostOS is not supported at this time. ";
		
 		if($prefs->{'software_class'} eq "Professional"){
 			$msg .= "Please see the GroundWork website at  http://support.groundworkopensource.com/downloads for information on supported Operating Systems and our complete 5 step Install procedure. Aborting Install!";
 			$nmscui->error_dialog($msg,$cui);
 			$self->exit();
 		}
 		elsif($prefs->{'software_class'} eq "Community Edition"){
 			$msg .= "Please refer to the Install Guide for a list of supported Operating Systems. You may attempt to install at your own risk.\nDo you wish to proceed with the installation?";
 			unless($nmcui->yes_or_no_dialog($msg)){
 				$self->exit();
 			}
 					
 		}
	}
	
 
# if install package doesnt match host OS
	$installed_os = GWInstaller::AL::Host::get_os_obj();
	$release_string = GWNMSInstaller::AL::GWNMS::get_release_string();
	$myOS = $installed_os->{'name'};
	$myOS =~ s/CentOS/RHEL/i;
	
 	if(($myOS =~ /$release_string/i) && $release_string ne ""){
		GWInstaller::UI::GWCursesUI::status_msg("\tInstallation package MATCHES target OS",$cui);
	}
	else{
		GWInstaller::UI::GWCursesUI::status_msg("\tInstallation package DOES NOT MATCH target OS",$cui);
		$msg = "Your installation package is intended to be installed on $release_string. You are attempting to install it on $installed_os->{'name'}. Please visit http://support.groundworkopensource.com/downloads and download a package intended for this Operating System.";
		$nmscui->error_dialog($msg);
		$self->exit();
	}
 

	GWInstaller::UI::GWCursesUI::status_msg("Checking hostname validity...",$cui);
	check_hostname();


}


sub pre_install_check{
	
	# SOFTWARE CONFLICTS
	######################
	GWInstaller::UI::GWCursesUI::status_msg("Checking for software conflicts...",$cui);
	check_conflicts();
	sleep $sleep;


	#SOFTWARE PREREQUISITES
	########################
	GWInstaller::UI::GWCursesUI::status_msg("Verifying Software Prerequisites...",$cui);
	check_prereqs();
	sleep $sleep;


	# VERIFY CONFIGURATION
	#######################
	GWInstaller::UI::GWCursesUI::status_msg("\tVerifying Configuration...",$cui);
	sleep $sleep;
	verify_config();
	sleep $sleep;
}

sub show_user_msg{
#####################
### USER MESSSAGE ###
#####################
if($is_upgrade){
	$type = "upgraded";
}
else{
	$type = "installed";
}
$nmscui->user_msg_dialog($type,"GroundWork NMS");
}

##################################### SUBROUTINES ################################################################
 
 
sub has_errors{
	$retVal = 0;
	
	$scriptlet_error = `grep -c 'scriptlet failed' nms.log 2>>nms.log`;
	chomp($scriptlet_error);
	$conflict_error = `grep -c 'conflicts with file' nms.log 2>>nms.log`;
	chomp($conflict_error);
	$other_error - `grep -ic 'error' nms.log 2>>nms.log`;
	chomp($other_error);
	if($scriptlet_error || $conflict_error || $other_error){
		$retVal = 1;
	}
	return $retVal;
}

sub get_errors{
 
 	
 	$scriptlet_error = `grep -A2 -B2 'scriptlet failed' nms.log 2>>nms.log`;
	chomp($scriptlet_error);
	$conflict_error = `grep  'conflicts with file' nms.log 2>>nms.log`;
	chomp($conflict_error);
	$other_error - `grep -i -A2 -B2  'error' nms.log 2>>nms.log`;
	chomp($other_error);
	
	$all_errors = "$scriptlet_error\n$conflict_error\n$other_error\n";
	return $all_errors;
	
}

sub verify_install{
	$self = shift;
	$retval = 1;
	
	GWInstaller::AL::GWLogger::log("Scanning for errors...");
	if($self->has_errors()){
		$retval = 0;
	}
	else{
		GWInstaller::AL::GWLogger::log("No errors were detected during the install.");
		$retval=1;
	}
	return $retval;

}

sub backup_installer{
	print `cp -r ../groundwork-nms-installer* /usr/local/groundwork/ > /dev/null 2>&1`;
	
}
sub verify_hardware{
	$my_hostname = `hostname`;
	$target_host = GWInstaller::AL::Host->new($my_hostname);
	
	#supported_cpus
    $info = `cat /proc/cpuinfo | grep 'model name' | head -1`;
    chomp($info);
    (undef,$cpu) = split(': ',$info);
    GWInstaller::UI::GWCursesUI::status_msg("\tFound " . $cpu,$cui);
    sleep $sleep;
    
    #number of cpus
    $cpu_count = `cat /proc/cpuinfo | grep processor | wc -l`;
    chomp $cpu_count;
   
    if($cpu_count < $prefs->{min_cpu_count}){
    	$msg = "WARNING: Your system currently has $cpu_count CPUs installed. A minimum of " . $prefs->{min_cpu_count} . " CPUs are recommended for a GroundWork Monitor production system.";
	    $nmslog->log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = $nmscui->yes_or_no_dialog("WARNING: Below recommended minimums.",$msg);
		unless($willContinue){
			$self->exit();
		} 

    }
    
    #cpu speed
    $cpu_speed = `cat /proc/cpuinfo | grep 'cpu MHz' | head -1`;
    chomp $cpu_speed;
    (undef,$speed) = split(': ',$cpu_speed);
    ($speed,undef) = split(/\./,$speed);
    if($speed < $prefs->{min_cpu_speed}){
    	$msg = "WARNING: Your CPU speed of ${speed}MHz is less than the minimum recommended CPU speed of " . $prefs->{min_cpu_speed} . "MHz for a GroundWork Monitor production system.";
	    $nmslog->log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = $nmscui->yes_or_no_dialog("WARNING: Below recommended minimums.",$msg);
		unless($willContinue){
			$self->exit();
		} 
    }
    
    #minimum memory
    $memtotal = $target_host->get_mem_total();
    $msg = "\tFound ${memtotal}k of memory";
    GWInstaller::UI::GWCursesUI::status_msg($msg,$cui);
    
    if($memtotal <$prefs->{min_memory}){
    	$msg = "WARNING: Your system has ${memtotal}k of memory. The minimum recommended memory for a GroundWork Monitor production system is " . $prefs->{min_memory} . "k.";
	    $nmslog->log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = $nmscui->yes_or_no_dialog("WARNING: Below recommended minimums.",$msg);
		unless($willContinue){
			$self->exit();
		} 
    }

	#check disk space
	GWInstaller::UI::GWCursesUI::status_msg("\tChecking available disk space...",$cui);
    $available_disk = GWInstaller::AL::Host::get_avail_disk();
    $min_avail = 4;
    if($available_disk >= $min_avail){
    	GWInstaller::UI::GWCursesUI::status_msg("\t$available_disk GB available. Minimum is $min_avail",$cui);
    }
    else{
    	$msg = "The minimum recommended available disk space is $min_avail GB. You have only $available_disk GB available.";
    	$msg .= " Please free up some space on your filesystem before continuing";
	    $nmslog->log($msg);
		$nmscui->error_dialog($msg);
		$self->exit();
    }	
	
	#hard minimum
	
	#soft minimum
	GWInstaller::UI::GWCursesUI::status_msg("\tChecking available disk space...",$cui);
    $available_disk = GWInstaller::AL::Host::get_avail_disk();
    $min_avail = GWInstaller::AL::Prefs::get_pref("min_avail_disk");
    if($available_disk >= $min_avail){
    	GWInstaller::UI::GWCursesUI::status_msg("\t$available_disk GB available. Minimum is $min_avail",$cui);
    }
    else{
    	$msg = "WARNING: The minimum recommended available disk space is $min_avail GB. You have only $available_disk GB available.";
	    $nmslog->log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = $nmscui->yes_or_no_dialog("WARNING: Below recommended minimums.",$msg);
		unless($willContinue){
			$self->exit();
		} 
    }
    sleep $sleep; 


}

 
 
 

 
 

sub check_hostname{
	$hostname = `hostname`;
	chomp($hostname);
	if($hostname =~ "localhost"){
		GWInstaller::UI::GWCursesUI::error_msg("localhost is not valid as a hostname. Please set the name of your host.",$cui);
		$self->exit();
	}
	
}

#############
# CONFLICTS #
#############
sub check_conflicts{

$conref = $prefs->{conflicts};
my @conflicts = @$conref;

foreach $pkg(@conflicts){
	
	if($pkg->is_installed()){
		GWInstaller::UI::GWCursesUI::status_msg('Found conflicting package: ' . $pkg->{'rpm_name'},$cui);
		GWInstaller::Dialogs::conflict_dialog($pkg,$cui);
		return 0;
	}
		
}
    GWInstaller::UI::GWCursesUI::status_msg("\tNo conflicts found",$cui);
    return 1;
}
 
  
 



##################
# PREREQUISITES #
##################

sub check_prereqs{
	$prereq_ref = $prefs->{prerequisites};
	my @prerequisites = @$prereq_ref;
	$pnum = @prerequisites;
	$nmslog->log("\tFound $pnum prerequisites.");
	
	foreach $pkg(@prerequisites){
	 		
		if($pkg->is_installed()){
			$installed_version = $pkg->get_installed_version();
	
			#this is a hack to allow a range of MySQL versions to be installed.
			$mysqlver = $installed_version;
			$mysqlver =~ s/\.//g;
			if($pkg->{rpm_name} =~ /^MySQL/ && ( ($mysqlver >= 5018) && ($mysqlver <= 5026) )){                                       
				$nmslog->log("\tMySQL version within valid range");
				next;
			}
			#check for unsupported version
			unless( ($installed_version =~ /$pkg->{'valid_version'}/) || ($pkg->{'valid_version'} eq "ANY") ) {
		 
				if($pkg->{rpm_name} =~ /^MySQL/ ){
					$msg = "Your version of MySQL needs to be upgraded to version " . $pkg->{'valid_version'} . "\n" . 
							"Please see the INSTALL document for instructions on how to perform this upgrade while preserving any data you may have.";
					GWInstaller::UI::GWCursesUI::error_msg($msg,$cui);
					$self->exit();
				}
				
				
				$msg = "Version $installed_version of " . $pkg->{'name'} . " is installed. ";
				$msg .= "GroundWork recommends using version " . $pkg->{'valid_version'} . ". ";
				$msg .= "Shall the installer attempt to remove the unsupported version and install version " . $pkg->{'valid_version'} . "?\n\n";
				$msg .= "You may click NO to quit the installer and resolve the conflict manually.";
				$is_confirmed = GWInstaller::UI::GWCursesUI::yes_or_no_dialog("Uninstall Package?",$msg,$cui);
				if($is_confirmed){
					$pkg->uninstall();
					
					#uninstall failed	
					if($pkg->is_installed()){
						GWInstaller::UI::GWCursesUI::error_msg("I was unable to uninstall the package. Please uninstall $pkg->{'name'} manually before continuing.",$cui);
						check_prereqs();
					}
					#uninstall succeeded
					else{
						#install package
						$my_package_file = GWInstaller::UI::GWCursesUI::locate_package_dialog($pkg,$cui);
						unless($my_package_file){$self->exit();}
						$rpm_ver = Software::get_rpm_version($my_package_file);
			     $nmslog->log("Selected RPM version: $rpm_ver");
			      ($justrpm,undef) = fileparse($my_package_file); 
			     if(($rpm_ver ne $pk->{'valid_version'}) && ($pk->{'valid_version'} ne "ANY")){
			     		$msg = "The package $justrpm is version $rpm_ver. You need to select a package containing version $pk->{'valid_version'} of $pk->{'rpm_name'} ";
			     		GWInstaller::UI::GWCursesUI::error_msg($msg,$cui);
			     		GWInstaller::Dialogs::prereq_dialog($pk,$cui);
			     }
			     
		
			    if($my_package_file =~ /$pk->{'rpm_name'}/){
				 	GWInstaller::Host::install_package($my_package_file);
				 	unless($pk->is_installed()){
				
				 		GWInstaller::UI::GWCursesUI::error_msg("Could not install the package $my_package_file.\nPlease check the log for errors and install this package manually.",$cui);
				 		$self->exit();
				 	}
				 	}
				 else{
				 	GWInstaller::UI::GWCursesUI::error_msg("A valid RPM file was not selected.",$cui);
				 	GWInstaller::Dialogs::prereq_dialog($pk,$cui);
				 	 }
						#install succeeded	
						if($pkg->is_installed()){
							$cui->dialog("Install of $pkg->{'name'} succeeded. Click OK to continue");
							check_prereqs();
						}
						#install failed
						else{
							GWInstaller::UI::GWCursesUI::error_msg("I was unable to install the package.Please check the log for errors and install $pkg->{'name'} manually before continuing.",$cui);
						   $self->exit();
							}
						
					}
				}
				else{
					$self->exit();
				}
				
			}
					
		} #end if pkg->is_installed
		else{
			
			if($pkg->{'rpm_name'} =~ /^MySQL/){
				$msg = "A compatible version of MySQL is not installed on your system. It is a prerequisite to GroundWork Monitor. Please refer to the GroundWork Monitor 5 step installation process at:http://support.groundworkopensource.com/downloads.";
				GWInstaller::UI::GWCursesUI::error_msg($msg,$cui);
				$self->exit();
			}
 
			GWInstaller::UI::GWCursesUI::status_msg('Missing Prerequisite package: ' . $pkg->{'rpm_name'},$cui);
			GWInstaller::Dialogs::prereq_dialog($pkg,$cui);
			
		}
			
	}
		GWInstaller::UI::GWCursesUI::status_msg("All prerequisites satisfied",$cui);
	    sleep $sleep;
	    return 1;
}
	
	
 
 

 


sub verify_net_config{
 	$verify = 0;
 	
 	#hostname
 	GWInstaller::UI::GWCursesUI::status_msg("Verifying hostname...",$cui);
	$hostname = $host->get_hostname();
	if($hostname){
		$nmslog->log("\thostname is $hostname");
		$verify = 1;
	}
	else{
		GWInstaller::UI::GWCursesUI::error_msg("Hostname is not set. Please set hostname before attempting to install GroundWork Monitor.",$cui);
		$self->exit();
	}
	
	#name resolution
  	$has_dns_entry = `host $hostname | grep -cv 'not found'`;
  	chomp($has_dns_entry);
 	
	#/etc/hosts file
	$hostsfile_is_compliant = GWInstaller::Host::verify_hosts_file($has_dns_entry);
	 if($hostsfile_is_compliant){
	 	GWInstaller::UI::GWCursesUI::status_msg("\t/etc/hosts file verified.",$cui);
	 }
	 else{
	 	GWInstaller::UI::GWCursesUI::status_msg("\t/etc/hosts file not compliant.",$cui);
	 	
	 	$title = "hosts file needs fixing";
	 	$msg = "Your /etc/hosts file is not compliant. Shall the installer attempt to repair it?";
	 	$doIt = GWInstaller::UI::GWCursesUI::yes_or_no_dialog($title,$msg,$cui);

	 	unless($doIt){
	 		$errmsg = "You will need to manually repair your hosts file before continuing. Please see the log for details.";
	 		GWInstaller::UI::GWCursesUI::error_msg($errmsg,$cui);
	 		$self->exit();
	 	}
	 	
	 	
	 	GWInstaller::UI::GWCursesUI::status_msg("Attempting to rectify /etc/hosts file.",$cui);
	 	GWInstaller::Host::repair_hosts_file();
	 	unless(GWInstaller::Host::verify_hosts_file($has_dns_entry)){
	 		$msg = "Unable to rectify problems with /etc/hosts file.\n" .
	 				"Please make sure this entry is present in the file:\n" .
	 				"<your_ip_address> <your_hostname>";
	 		GWInstaller::UI::GWCursesUI::error_msg($msg,$cui);
	 		$self->exit();
	 	}
	 }
	 
	 
  
  return $verify;
	
}

 
sub verteilt_ist_aktiviert{ 
	$aktiviert = 0;
	$uberprufung = 3*3+8 . "tMkM." . "bSoq" . "2g";
	
	if(-e './conf/.nms'){
		open(DATEI,"./conf/.nms");
		$anzahl = <DATEI>;
		chomp($anzahl);
		if($anzahl eq $uberprufung){
			$aktiviert = 1;
		}
		close(DATEI);
	}
	
	return $aktiviert;
}

sub verify_config{
    GWInstaller::UI::GWCursesUI::status_msg("Verifying network configuration...",$cui);
    sleep $sleep;
 	verify_net_config();
    
    GWInstaller::UI::GWCursesUI::status_msg("Network configuration verified",$cui);
    sleep $sleep;
    

   
	 
	
 
    sleep $sleep;
     GWInstaller::UI::GWCursesUI::status_msg('Verifying Java configuration...',$cui);
    sleep $sleep;
   @fixlist = GWInstaller::Host::verify_java_config($prefs);
   $fixSize = @fixlist;
 	if($fixlist[0] ne ""){
		GWInstaller::UI::GWCursesUI::status_msg('Java configuration is non compliant.',$cui);
		sleep $sleep;
	 	
	 	$title = "Java config";
	 	$msg = "Your java config is not compliant. Shall the installer attempt to repair it?";
	 	$doIt = GWInstaller::UI::GWCursesUI::yes_or_no_dialog($title,$msg,$cui);
	 	
	 	unless($doIt){
	 		$errmsg = "You will need to manually repair your Java config before continuing. Please take the following steps:\n" .
						  "1) correctly set JAVA_HOME in /etc/profile\n" .
	    	 			  "2) update /etc/alternatives/java to point to the installed Java SDK\n".
	    	 			  "3) update /usr/bin/java link to point to /etc/alternatives/java\n" . 
	    	 			  "4) reload environmental variables from /etc/profile with the command: `source /etc/profile`";

			GWInstaller::UI::GWCursesUI::error_msg($errmsg,$cui);
	 		$self->exit();
	 	}

		GWInstaller::UI::GWCursesUI::status_msg("Attempting to fix Java config.",$cui);
		  $fixSize = @fixlist;
		
 	 	
	 			    
		if(GWInstaller::Host::fix_java_config(\@fixlist)){ #@fixlist \@fixlist
			 GWInstaller::UI::GWCursesUI::status_msg('Re-verifying Java config',$cui);
			@fixlist = GWInstaller::Host::verify_java_config($prefs);
			if($fixlist[0] eq ""){
				 GWInstaller::UI::GWCursesUI::status_msg('Java configuration rectified.',$cui);
			}
			else{
				GWInstaller::Dialogs::java_not_verified_dialog($cui);
				$self->exit();
			}
	    	 sleep $sleep;
 		}
		 else{
	    	 	GWInstaller::Dialogs::java_not_verified_dialog($cui);
	    	 	sleep $sleep;
	    	 	GWInstaller::UI::GWCursesUI::error_msg("unable to rectify",$cui);
				$self->exit();
	    	 }
	}
	else{
		GWInstaller::UI::GWCursesUI::status_msg("\tJava installation verified.",$cui);
		sleep $sleep;
		 
	}
    	 
    	 
    
    sleep $sleep;
     GWInstaller::UI::GWCursesUI::status_msg('Verifying MySQL configuration...',$cui);
    sleep $sleep;
    
    #Check if MySQL processes are running
    if(GWInstaller::Host::mysql_is_running()){
    	GWInstaller::UI::GWCursesUI::status_msg("\tMySQL process is running.",$cui);
    }
    else{
    	$err = 	"MySQL is not currently running. Please start the mysql service before continuing.\n" .
    			"You may also need to configure MySQL to start with runlevel 3 and runlevel 5.";
    	GWInstaller::UI::GWCursesUI::error_msg($err,$cui);
    	$self->exit();
    	
    }
    
    
    #Get password if necessary
    if(GWInstaller::Host::mysql_passwd_set()){
    	GWInstaller::Dialogs::mysql_dialog($cui);
    }

	#Try out MySQL connection
	if(GWInstaller::Host::mysql_can_connect()){
		GWInstaller::UI::GWCursesUI::status_msg("\tConnection to MySQL: OK",$cui);
	}
	else{
		$msg = "Unable to connect to MySQL instance. Please refer to log for more information.\n" . 
				"Restarting the MySQL service may remedy this problem.";
		GWInstaller::UI::GWCursesUI::error_msg($msg,$cui);	
		$self->exit();
	}

 	 GWInstaller::UI::GWCursesUI::status_msg("\tMySQL installation verified.",$cui);
	 
    
    #$cui->setprogress(39,'Verifying syslog-ng configuration...');
    sleep $sleep;
}

 

sub is_gwm_valid{
	$validity = 0;
	
	$installed_version = $gwmonitor->get_installed_version();
#	($installed_major,$installed_minor,$installed_patch) = split(/\./,$installed_version);
	
#	$package_version = $prefs->get_version_from_core();
#	($package_major,$package_minor,$package_patch) = split(/\./,$package_version);
	
	$valid_upgrade_string = $prefs->get_valid_upgrades();
	GWInstaller::AL::GWLogger::log("valid_upgrade_string =   $valid_upgrade_string");
	@valid_upgrades = split(/\,/,$valid_upgrade_string);
	foreach $ver (@valid_upgrades){
	 
		if($installed_version eq "5.2.1"){
			if($self->servicepack_level_OK()){
				$validity = 1;
			}
		}
	
		elsif($installed_version eq $ver){
			$validity = 1;
		}
	}
	
 
	
	
		 
	return $validity;
}

sub uninstall_rpms{
	    	
	    $retval = 0;
	    
	    	$cui->setprogress(10,"Uninstalling GroundWork Monitor..");
	    	sleep 1;
	    	
	     
			if(GWInstaller::Host::is_rpm_installed("groundwork-bookshelf")){
				$cui->setprogress(15,'Uninstalling bookshelf documentation...');
				$gwmonitor->uninstall_bookshelf();
				}
			
	  
	  
    		
    	return $retval;
	
}

sub servicepack_level_OK{
	my $self = shift;
	$levelOK = 0;
	$sp_min_level = 6;
	$spname = "groundwork-monitor-sp";
	
	if(GWInstaller::AL::Host::is_rpm_installed($spname)){
		$spObj = GWInstaller::AL::Software->new();
		$spObj->{rpm_name} = $spname;
		$sp_level =  $spObj->get_installed_rpm_version();
		my($maj,$min,$patch,$sp_number) = split(/\./,$sp_level);
		chomp($sp_number);
		GWInstaller::AL::GWLogger::log("Service pack level: $sp_number :");
		if($sp_number >= $sp_min_level){
			$levelOK = 1;
			
		} 
	}#end if sp installed	

	return $levelOK;	
	
}
#rename Quit
sub exit{
	my ($self,$status) = @_;
	$unlock = 1;
	if($status eq "locked"){$unlock = 0;}	
	if($unlock == 1){ 
		$self->unlock();
	}

	($package, $filename, $line) = caller;

	$msg = "Exiting installer";
	$cui->dialog($msg);
	$nmslog->log($msg);

	exit(0);
}

sub exit_installer{
	#for backward compat. to be removed
	($package, $filename, $line) = caller;
	GWInstaller::AL::GWLogger::log("Deprecated call to exit_installer(): $package $filename $line");
	GWNMSInstaller::GWNMSInstaller::exit();
}

sub install_failed{
	
	GWInstaller::UI::GWCursesUI::error_msg("The installation has failed. Please consult the log for further details.",$cui);
	$self->exit();
}
1;
