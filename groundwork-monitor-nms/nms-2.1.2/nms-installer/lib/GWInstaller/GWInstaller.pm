#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWInstaller;

use lib "./lib"; 
use Curses::UI;
use GWCursesUI;
use GWLogger;
use GWInstaller::Prefs;
use GWInstaller::GWMonitor;
use GWInstaller::Host;
use GWInstaller::Dialogs;
 my $sleep = 0;
our($prereq_path);
our($host);
$host = GWInstaller::Host->new();
 our($prefs);
 @conflicts = ();
 @prereqs = ();
  @oses = ();
 $host_os;
our($cui);
our($win);
our($gwmonitor);
our($whatami);

 sub new{
	 
	my ($invocant,$log_level) = @_;
	my $class = ref($invocant) || $invocant;
	 
   
	my $self = {
		log_level=>$log_level,
		cui=>$cui,
		win=>$win
 
	};
 
	bless($self,$class);
 
 	init();
 	
	return $self;		
}
 
 sub init{
	$date = `date`;
	$hostname = $host->get_hostname();
	GWLogger::log("\n*********************************");
	GWLogger::log("Installer Started on " . $hostname . " on ". $date); 
 
 	$prefs = GWInstaller::Prefs->new();
	$prefs->load_software_prefs();
    $whatami =  $prefs->{software_name} . " " . $prefs->{software_class} . " " . $prefs->{version}; 
   
 	
  $cui = new Curses::UI(-color_support=>1,-clear_on_exit=>0);
  $win = $cui->add('window_id', 'Window');
  Curses::UI::Color::define_color("orange",255,153,51);
 
 # title text
  $win->add(
        'label', 'Label',
        -width         => -1,
        -paddingspaces => 1,
        -textalignment => 'middle',
        -text          => 'GroundWork Monitor 5.2 Installer',
    );
 
   $win->add(
        'label2', 'Label',
        -width         => -1,
        -paddingspaces => 1,
        -y => 1,
        -textalignment => 'middle',
        -text          => '(c)2008 GroundWork Open Source, Inc.',
    );
 	
   $cui->progress( -max       => 100 );
   GWCursesUI::status_msg('Initializing Automated Installer...',$cui);
   
   #remove temporary files
   
   GWLogger::log("Cleaning up template files...");
   print `rm -rf /tmp/tpl* > /dev/null 2>&1`;
   GWLogger::log("Cleaning up session files...");
   print `rm -rf /tmp/sess* > /dev/null 2>&1`;
    
   
 }
 
 sub run{
 	scan_for_showstoppers();
 	verify_environment();
	smart_install();
	verify_install();
	show_user_msg();
 	
 }
 
 

sub scan_for_showstoppers{
GWCursesUI::status_msg("Scanning for showstoppers...",$cui);
# Must be ROOT 
#################
$user = `whoami`;
chomp($user);
if($user eq "root"){
	GWCursesUI::status_msg("\tUser is root",$cui);
 }
else{

    GWCursesUI::error_msg("You must be root to run this installer. Aborting Install!",$cui);
    exit_installer();
}
# CHECK SE LINUX STATUS
########################
GWCursesUI::status_msg("\tChecking for SE Linux",$cui);
if(GWInstaller::Host::is_selinux_enabled()){

  $errmsg ="You must fully disable SELinux before installing GroundWork Monitor. " .
                "Please edit /etc/selinux/config, change the value to: " .
                "SELINUX=disabled and reboot your computer. You may re-enable it after completing the installation.";
                
                GWCursesUI::error_msg($errmsg,$cui);
                
                exit_installer();
}
GWCursesUI::status_msg("\tSE Linux is disabled",$cui);

# if package incomplete, bail
###############################
if($prefs->{software_class}){
	GWCursesUI::status_msg("\t" . $prefs->{software_class} . " Installation package is complete",$cui);
}
else{
	GWCursesUI::error_msg('There are files missing from this installation package. Please download it again.',$cui);
	exit_installer();
}
 
 

 
}


################################################
### VERIFY ENVIRONMENT MEETS RECOMMENDATIONS ###
################################################ 
sub verify_environment{
	GWCursesUI::status_msg('Verifying Environment...',$cui);
	# MINIMUM HARDWARE
	###################
	GWCursesUI::status_msg("Verifying minimum hardware requirements...",$cui);
	sleep $sleep;
	verify_hardware();


	# OS REQUIREMENT
	##################
	GWCursesUI::status_msg("Verifying operating system requirements...",$cui);
 	
 	$hostOS = GWInstaller::Host::get_os();
 	GWCursesUI::status_msg("\tHost Operating System is $hostOS",$cui);
	$hostStatus = GWInstaller::GWMonitor::get_os_status($hostOS);

	if($hostStatus eq "production"){
		GWCursesUI::status_msg("\tHost OS is Certified for Production Use",$cui);
	}
	elsif($hostStatus eq "eval"){
		GWCursesUI::status_msg("\tHost OS is Certified for Evaluation Use",$cui);
		$msg = "$hostOS is not certified for Production Use. It is recommended for Evaluation installations.\n Do you wish to proceed with the installation?";
		unless(GWCursesUI::yes_or_no_dialog("Warning- Not Certified for Production Use",$msg,$cui)){
			GWInstaller::exit_installer();
		}

				

	}
	elsif($hostStatus eq "unsupported"){
		$msg = "$hostOS is not supported at this time. ";
		
 		if($prefs->{'software_class'} eq "Professional"){
 			$msg .= "Please see the GroundWork website at  http://support.groundworkopensource.com/downloads for information on supported Operating Systems and our complete 5 step Install procedure. Aborting Install!";
 			GWCursesUI::error_msg($msg,$cui);
 			GWInstaller::exit_installer();
 		}
 		elsif($prefs->{'software_class'} eq "Community Edition"){
 			$msg .= "Please refer to the Install Guide for a list of supported Operating Systems. You may attempt to install at your own risk.\nDo you wish to proceed with the installation?";
 			unless(GWCursesUI::yes_or_no_dialog()){
 				GWInstaller::exit_installer();
 			}
 					
 		}
	}
	
 
# if install package doesnt match host OS
	$installed_os = GWInstaller::Host::get_os_obj();
	$release_string = GWInstaller::GWMonitor::get_release_string();
	$myOS = $installed_os->{'name'};
	$myOS =~ s/CentOS/RHEL/i;
 	if($myOS =~ /$release_string/i){
		GWCursesUI::status_msg("\tInstallation package MATCHES target OS",$cui);
	}
	else{
		GWCursesUI::status_msg("\tInstallation package DOES NOT MATCH target OS",$cui);
		$msg = "Your installation package is intended to be installed on $release_string. You are attempting to install it on $installed_os->{'name'}. Please visit http://support.groundworkopensource.com/downloads and download a package intended for this Operating System.";
		GWCursesUI::error_msg($msg,$cui);
		GWInstaller::exit_installer();
	}
 

	GWCursesUI::status_msg("Checking hostname validity...",$cui);
	check_hostname();


	# SOFTWARE CONFLICTS
	######################
	GWCursesUI::status_msg("Checking for software conflicts...",$cui);
	check_conflicts();
	sleep $sleep;


	#SOFTWARE PREREQUISITES
	########################
	GWCursesUI::status_msg("Verifying Software Prerequisites...",$cui);
	check_prereqs();
	sleep $sleep;


	# VERIFY CONFIGURATION
	#######################
	GWCursesUI::status_msg("\tVerifying Configuration...",$cui);
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
 GWInstaller::Dialogs::user_msg_dialog($type,$whatami,$cui);
}

##################################### SUBROUTINES ################################################################

 
sub smart_install{
	
$is_upgrade=0;

GWCursesUI::status_msg('Checking for previous GroundWork Monitor install...',$cui);

sleep $sleep; 
$gwmonitor = GWInstaller::GWMonitor->new(); #when to create this object? earlier?
 
if($gwmonitor->is_installed()){
 	
 	# Prompt for Upgrade, Clean Install or Abort Install
 
 		$msg = "Found GroundWork Monitor " . $gwmonitor->{software_class} . " " . $gwmonitor->{version};
    	GWCursesUI::status_msg($msg,$cui);
    
        my $return = GWInstaller::Dialogs::previous_version_dialog($cui);
		handle_previous_version($return);      
	
	
}
elsif($gwmonitor->is_partially_installed){
	# Ask user if he wants to wipe it out? if so, wipe and continue, else quit
	$msg = "A partially uninstalled copy of GroundWork Monitor was found. The installer can not continue in this state.";
 	GWCursesUI::error_msg($msg,$cui);
	exit_installer();
 
	
}

#Has never been installed
else{


#Give user an opportunity to back out
$continue = GWInstaller::Dialogs::install_dialog($whatami,$cui);
unless($continue){exit_installer();}
 

#INSTALL
#################


	$cui->setprogress(20,'Installing GroundWork Monitor (this may take a minute)');
	sleep $sleep;
	GWLogger::log("Install Groundwork $prefs->{software_class}.");
			$cui->setprogress(40,'Installing foundation...');
			$gwmonitor->install_foundation();
			$cui->setprogress(60,'Installing core...');
			$gwmonitor->install_core();
			
			if($prefs->{software_class} eq "Professional"){
				$cui->setprogress(80,'Installing pro...');
				$gwmonitor->install_pro();
			}
			$cui->setprogress(90,'Installing bookshelf documentation...');
			$gwmonitor->install_bookshelf();

	#GWMonitor::update_guava_menu();
}
}
sub handle_previous_version{
	my 
	$return = shift;
	    #uninstall
    if($return == 5){
    	$msg = "Are you sure you want to UNINSTALL $prefs->{software_name} This may cause user data to be lost.";
    	if(GWCursesUI::yes_or_no_dialog("Are you sure?",$msg,$cui)){

	    	GWCursesUI::status_msg("UNINSTALL selected.",$cui);
    		uninstall_rpms();
    		$cui->setprogress(85,"Verifying Uninstall...");
    		GWLogger::log("Verifying uninstall");
    		if($gwmonitor->is_installed()){
    			
    			GWCursesUI::error_msg("I was unable to uninstall GroundWork Monitor. Please consult the installer.log for further details.",$cui);
    			exit_installer();
    		}
    		else{
    			$cui->setprogress(100,"Uninstall verified");
    			GWLogger::log("Uninstall verified");
	 			GWInstaller::Dialogs::uninstall_msg_dialog($cui);
	 			exit_installer();
    		}
 			 
    	}	
    	else{
    		my $return = GWInstaller::Dialogs::previous_version_dialog($cui);
			handle_previous_version($return);    
    	}
    	
    }
    
    #upgrade
    elsif($return == 10){  
    	GWCursesUI::status_msg("UPGRADE selected.",$cui);
 	 	$msg = "Checking validity of upgrade path...";
    	GWCursesUI::status_msg($msg,$cui);  
 # PRO to OS Upgrade Invalid
##############################
 sleep $sleep;

$pro_is_installed = GWInstaller::Host::is_rpm_installed("groundwork-monitor-pro");
if($pro_is_installed && ($prefs->{'software_class'} eq "Community Edition")){
	GWCursesUI::error_msg("Sorry, you can not use an open source installer to upgrade a Pro installation.",$cui);
	exit_installer();
}
    	if(is_valid_upgrade()){   
    		if($gwmonitor->is_nms_installed()){
    			GWCursesUI::error_msg("NMS packages have been detected. The installer does not currently support upgrading an installation with NMS. Please contact support for details about upgrading this type of installation.",$cui);
    			exit_installer();
    		}
    		$is_upgrade=1;
   			$msg = "Upgrading GroundWork Monitor $prefs->{'software_class'} from " . $gwmonitor->get_installed_version() . " to " . $prefs->get_version_from_core();
    		$cui->setprogress(20,$msg);  
 
	 		$cui->setprogress(40,"Upgrading Foundation RPM");               
	 		$gwmonitor->upgrade_foundation();
	 		
	 		$cui->setprogress(60,"Upgrading Core RPM");               
	 		$gwmonitor->upgrade_core();
	 	
	 	  if($prefs->{software_class} eq "Professional"){
	 		
	 		$cui->setprogress(80,"Upgrading Pro RPM");               
	 		$gwmonitor->upgrade_pro();
	 	  }
	 	  
	 	  $cui->setprogress(90,'Upgrading bookshelf documentation...');
	   	  $gwmonitor->upgrade_bookshelf();
 
	 		unless($gwmonitor->is_installed()){ 
				GWCursesUI::error_msg("The upgrade attempt was not successful. Please see log for further information.",$cui);
				exit_installer();
			}
			
			# After Successful Upgrade, move old feeder directories
    		# GWMON-4802
    		if($pro_is_installed){
    			
    			#stop gwservices
    			$gwstop = `/etc/init.d/gwservices stop 2>&1`;
    			GWLogger::log($gwstop);
    			#create backup directory if it is missing
    			unless(-d "/usr/local/groundwork/backup"){
	    			GWCursesUI::status_msg("Adding missing backup directory",$cui);
    				print `/bin/mkdir /usr/local/groundwork/backup`;
    				print `/bin/chown nagios:nagios /usr/local/groundwork/backup`; 
    			} 
    			
    			#move feeder directories to backup
    			@dirsToMove = ('/usr/local/groundwork/core/services/feeder-nagios-status','/usr/local/groundwork/core/services/feeder-nagios-log');
    			GWCursesUI::status_msg("Moving feeder directories...",$cui);
    			foreach $dir(@dirsToMove){ 
    			if( -d $dir){
    				GWLogger::log("Moving $dir to /usr/local/groundwork/backup");
    				$cmd = "/bin/mv $dir /usr/local/groundwork/backup/";
    				$result = `$cmd`;
    				if($result){GWLogger::log("WARNING:" . $result);}
    				}
    			}
    			#restart gwservices
    			$gwstart =  `/etc/init.d/gwservices start  2>&1`;
    			GWLogger::log($gwstart);
    		}
    		## END GWMON-4802
			
			
    	}
    	else{
    		$msg = "Upgrading from " . $gwmonitor->get_installed_version() . " to " . $prefs->get_version_from_core() . " is not a supported upgrade.";
    		GWCursesUI::error_msg($msg,$cui);
    		exit_installer();
    	}
    }
    
    #clean install
    elsif($return == 20){
 		$msg = "Are you sure you want to perform a clean install?\n";
		$msg .= "Warning! this action will delete all GroundWork Monitor user data from your system.";
    	if(GWCursesUI::yes_or_no_dialog("Confirm",$msg,$cui)){
    		GWCursesUI::status_msg("Performing Clean Install",$cui);
    		$cui->setprogress(20,"Removing previous install...");
			$gwmonitor->wipe_partial_install();
			
			uninstall_rpms();
    		
    		
			$cui->setprogress(40,'Installing foundation...');
			$gwmonitor->install_foundation();
			$cui->setprogress(60,'Installing core...');
			$gwmonitor->install_core();
			
			if($prefs->{software_class} eq "Professional"){
				$cui->setprogress(80,'Installing pro...');
				$gwmonitor->install_pro();
			}
			$cui->setprogress(90,'Installing bookshelf documentation...');
			$gwmonitor->install_bookshelf();
 	     }
    	else{
    		smart_install();
   			verify_install();
			show_user_msg();
    	}
    }                              
 
    
    else{
    		$cui->dialog("Install cancelled.");
    		GWLogger::log("Install cancelled.");
   	 		exit_installer();
    }
	
}
 
 
 
sub install_has_errors{
	$retVal = 0;
	
	$scriptlet_error = `grep -c scriptlet failed' installer.log`;
	chomp($scriptlet_error);
	$conflict_error = `grep -c 'conflicts with file' installer.log`;
	chomp($conflict_error);

	if($scriptlet_error || $conflict_error){
		$retVal = 1;
	}
	return $retVal;
}
sub verify_install{
	$retval = 1;

if(install_has_errors()){
	GWCursesUI::error_msg("Errors were detected during this install. Please review the installer.log for more information.",$cui);	
	GWInstaller::exit_installer();
}
	
$cui->setprogress(81,'Performing automated verification...');
GWLogger::log("Performing automated verification...");
sleep $sleep;

# verify that RPMs have been installed
$cui->setprogress(82,'Verifying RPM installation');
GWLogger::log("Verifying RPM installation...");

$cui->setprogress(84,"Verifying Foundation RPM installation...");
GWLogger::log("Verifying Foundation RPM installation...");
$found_is_installed = GWInstaller::Host::is_rpm_installed('groundwork-foundation-pro');
unless($found_is_installed){
	GWLogger::log("Foundation install failed.");
	install_failed();
}

$cui->setprogress(86,"Verifying Core RPM installation...");
GWLogger::log("Verifying Core RPM installation...");
$core_is_installed = GWInstaller::Host::is_rpm_installed('groundwork-monitor-core');
unless($core_is_installed){
	GWLogger::log("groundwork-monitor-core install failed.");
	install_failed();
}

if($prefs->{software_class} eq "Professional"){
	$cui->setprogress(88,"Verifying Pro RPM installation...");
	GWLogger::log("Verifying Pro RPM installation...");
	$pro_is_installed = GWInstaller::Host::is_rpm_installed('groundwork-monitor-pro');
	unless($pro_is_installed){
		GWLogger::log("groundwork-monitor-pro install failed.");
		install_failed();
	}
}

$cui->setprogress(90,'Nagios->GW link verified');
 sleep $sleep;
unless($gwmonitor->verify_nagios_link()){
	GWCursesUI::error_msg("Warning!: unable to verify Nagios -> GroundWork Monitor link. Please see the log for more information.",$cui);
	sleep $sleep;
	$retval = 0;
}

# For professional, verify SNMP functionality
if ($prefs->{software_class} eq "Professional"){
	$cui->setprogress(92,'SNMP processes running');
	unless($gwmonitor->verify_snmp()){
		$msg = "Warning! unable to verify SNMP functionality.  Please see the log for more information\n" .
				"It may also be useful to examine  /usr/local/groundwork/common/var/log/snmp/snmptrapd.log";
		GWCursesUI::error_msg($msg,$cui);
		sleep $sleep;
		$retval = 0;
	}
	
	$cui->setprogress(94,'Sending test trap...');
	if($gwmonitor->send_test_trap()){
		$cui->setprogress(96,'Sending test trap... verified');
		GWCursesUI::status_msg("Test trap verified.",$cui);
	}
	else{
		GWCursesUI::error_msg("Warning! unable to verify test trap.  Please see the log for more information.",$cui);
		sleep $sleep;
		$retval = 0;;
	}
	
	
	$cui->setprogress(96,'Checking crond');
	GWLogger::log("Checking crond...");
	if(GWInstaller::Host::service_is_running("crond") || GWInstaller::Host::service_is_running("cron")){
		GWLogger::log("\tcrond is running.");
	}
	else{
		GWLogger::log("\tcrond is stopped.");
		$title = "WARNING: crond is stopped";
		$msg = "The installer has detected that the crond service is currently stopped. GroundWork Monitor requires the crond to be running in order to perform certain tasks. Would you like to start the service at this time? ";
		if(GWCursesUI::yes_or_no_dialog($title,$msg,$cui)){
			GWCursesUI::status_msg("\tAttempting to start crond",$cui);
			GWInstaller::Host::start_service("crond");
			
			#if start failed
			if(GWInstaller::Host::service_is_running("crond")){
				GWCursesUI::status_msg("\tcrond service started",$cui);
					
			}
			else{
				$title = "WARNING: Unable to start service";
				$msg = "The installer was unable to start the service: crond. Please start it manually after the installation completes.";
				GWCursesUI::warning_msg($title,$msg,$cui);
			}
		} #end if attempt to start service
		else{
			$title = "Please start service manually";
			$msg = "Please start the crond service manually after the install completes.";
			GWCursesUI::warning_msg($title,$msg,$cui);
		}
	}#end else service not running
	
#################################################################
	$cui->setprogress(97,'Verifying Firewall config');
  	GWLogger::log("Verifying Firewall config...");
    
    ($fwcfg_verified,@ports_to_fix) = GWInstaller::Host::verify_firewall_config($cui);
    
    if($fwcfg_verified){
	    GWCursesUI::status_msg("\tFirewall configuration verified",$cui);
    }
   	else{
   	
    	GWCursesUI::status_msg("\tFirewall config non compliant.",$cui);
    	
    	$title = "WARNING: Necessary TCP/UDP Ports may be Closed on this host";
	 	$msg = "GroundWork Monitor requires certain ports to be open in order to function properly. On your system, some of those ports could not be verified as open." .
	 			"\n\nPlease ensure that the following ports are open in your firewall config:\n\n";
	 	$first = 1;		
	 	foreach $port (@ports_to_fix){
	 		if($first != 1){ $msg .= ", ";}
	 		$msg .= "$port";	
	 		$first=0;
	 	}
		
		GWCursesUI::warning_msg($title,$msg,$cui);

 	 
	 	
   	}
	
	#################################################################
	
	$cui->setprogress(98,'Connecting to installation...');
 	GWLogger::log("Checking status of web server");
	
	if(GWInstaller::Host::is_httpd_running() && GWInstaller::Host::httpd_can_connect()){
		GWLogger::log("\tweb server OK");
		$cui->setprogress(85,'Connecting to installation...success!');		
	}
	else{
		GWCursesUI::error_msg("Warning! Unable verify connection to GroundWork Monitor installation on port 80.",$cui);
	}
}
#endif open source
	GWCursesUI::status_msg("Installation complete",$cui);
	$cui->setprogress(100,'Installation process completed');
	
	GWCursesUI::status_msg("Saving a copy of installer and log",$cui);
	backup_installer();
	return $retval;
}

sub backup_installer{
	print `cp -r ../groundwork-installer* /usr/local/groundwork/ > /dev/null 2>&1`;
	
}
sub verify_hardware{
	$my_hostname = `hostname`;
	$target_host = GWInstaller::Host->new($my_hostname);
	
	#supported_cpus
    $info = `cat /proc/cpuinfo | grep 'model name' | head -1`;
    chomp($info);
    (undef,$cpu) = split(': ',$info);
    GWCursesUI::status_msg("\tFound " . $cpu,$cui);
    sleep $sleep;
    
    #number of cpus
    $cpu_count = `cat /proc/cpuinfo | grep processor | wc -l`;
    chomp $cpu_count;
   
    if($cpu_count < $prefs->{min_cpu_count}){
    	$msg = "WARNING: Your system currently has $cpu_count CPUs installed. A minimum of " . $prefs->{min_cpu_count} . " CPUs are recommended for a GroundWork Monitor production system.";
	    GWLogger::log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = GWCursesUI::yes_or_no_dialog("WARNING: Below recommended minimums.",$msg,$cui);
		unless($willContinue){
			exit_installer();
		} 

    }
    
    #cpu speed
    $cpu_speed = `cat /proc/cpuinfo | grep 'cpu MHz' | head -1`;
    chomp $cpu_speed;
    (undef,$speed) = split(': ',$cpu_speed);
    ($speed,undef) = split(/\./,$speed);
    if($speed < $prefs->{min_cpu_speed}){
    	$msg = "WARNING: Your CPU speed of ${speed}MHz is less than the minimum recommended CPU speed of " . $prefs->{min_cpu_speed} . "MHz for a GroundWork Monitor production system.";
	    GWLogger::log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = GWCursesUI::yes_or_no_dialog("WARNING: Below recommended minimums.",$msg,$cui);
		unless($willContinue){
			exit_installer();
		} 
    }
    
    #minimum memory
    $memtotal = $target_host->get_mem_total();
    $msg = "\tFound ${memtotal}k of memory";
    GWCursesUI::status_msg($msg,$cui);
    
    if($memtotal <$prefs->{min_memory}){
    	$msg = "WARNING: Your system has ${memtotal}k of memory. The minimum recommended memory for a GroundWork Monitor production system is " . $prefs->{min_memory} . "k.";
	    GWLogger::log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = GWCursesUI::yes_or_no_dialog("WARNING: Below recommended minimums.",$msg,$cui);
		unless($willContinue){
			exit_installer();
		} 
    }

	#check disk space
	GWCursesUI::status_msg("\tChecking available disk space...",$cui);
    $available_disk = $host->get_avail_disk();
    $min_avail = GWInstaller::Prefs::get_pref("min_avail_disk");
    if($available_disk >= $min_avail){
    	GWCursesUI::status_msg("\t$available_disk GB available. Minimum is $min_avail",$cui);
    }
    else{
    	$msg = "WARNING: The minimum recommended available disk space is $min_avail GB. You have only $available_disk GB available.";
	    GWLogger::log($msg);
		$msg .= "\n\nWould you like to continue anyway?";
		$willContinue = GWCursesUI::yes_or_no_dialog("WARNING: Below recommended minimums.",$msg,$cui);
		unless($willContinue){
			exit_installer();
		} 
    }
    sleep $sleep; 


}

 
 
 

 
 

sub check_hostname{
	$hostname = `hostname`;
	chomp($hostname);
	if($hostname =~ "localhost"){
		GWCursesUI::error_msg("localhost is not valid as a hostname. Please set the name of your host.",$cui);
		exit_installer();
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
		GWCursesUI::status_msg('Found conflicting package: ' . $pkg->{'rpm_name'},$cui);
		GWInstaller::Dialogs::conflict_dialog($pkg,$cui);
		return 0;
	}
		
}
    GWCursesUI::status_msg("\tNo conflicts found",$cui);
    return 1;
}
 
  
 



##################
# PREREQUISITES #
##################

sub check_prereqs{
	$prereq_ref = $prefs->{prerequisites};
	my @prerequisites = @$prereq_ref;
	$pnum = @prerequisites;
	GWLogger::log("\tFound $pnum prerequisites.");
	
	foreach $pkg(@prerequisites){
	 		
		if($pkg->is_installed()){
			$installed_version = $pkg->get_installed_version();
	
			#this is a hack to allow a range of MySQL versions to be installed.
			$mysqlver = $installed_version;
			$mysqlver =~ s/\.//g;
			if($pkg->{rpm_name} =~ /^MySQL/ && ( ($mysqlver >= 5018) && ($mysqlver <= 5026) )){                                       
				GWLogger::log("\tMySQL version within valid range");
				next;
			}
			#check for unsupported version
			unless( ($installed_version =~ /$pkg->{'valid_version'}/) || ($pkg->{'valid_version'} eq "ANY") ) {
		 
				if($pkg->{rpm_name} =~ /^MySQL/ ){
					$msg = "Your version of MySQL needs to be upgraded to version " . $pkg->{'valid_version'} . "\n" . 
							"Please see the INSTALL document for instructions on how to perform this upgrade while preserving any data you may have.";
					GWCursesUI::error_msg($msg,$cui);
					exit_installer();
				}
				
				
				$msg = "Version $installed_version of " . $pkg->{'name'} . " is installed. ";
				$msg .= "GroundWork recommends using version " . $pkg->{'valid_version'} . ". ";
				$msg .= "Shall the installer attempt to remove the unsupported version and install version " . $pkg->{'valid_version'} . "?\n\n";
				$msg .= "You may click NO to quit the installer and resolve the conflict manually.";
				$is_confirmed = GWCursesUI::yes_or_no_dialog("Uninstall Package?",$msg,$cui);
				if($is_confirmed){
					$pkg->uninstall();
					
					#uninstall failed	
					if($pkg->is_installed()){
						GWCursesUI::error_msg("I was unable to uninstall the package. Please uninstall $pkg->{'name'} manually before continuing.",$cui);
						check_prereqs();
					}
					#uninstall succeeded
					else{
						#install package
						$my_package_file = GWCursesUI::locate_package_dialog($pkg,$cui);
						unless($my_package_file){exit_installer();}
						$rpm_ver = Software::get_rpm_version($my_package_file);
			     GWLogger::log("Selected RPM version: $rpm_ver");
			      ($justrpm,undef) = fileparse($my_package_file); 
			     if(($rpm_ver ne $pk->{'valid_version'}) && ($pk->{'valid_version'} ne "ANY")){
			     		$msg = "The package $justrpm is version $rpm_ver. You need to select a package containing version $pk->{'valid_version'} of $pk->{'rpm_name'} ";
			     		GWCursesUI::error_msg($msg,$cui);
			     		GWInstaller::Dialogs::prereq_dialog($pk,$cui);
			     }
			     
		
			    if($my_package_file =~ /$pk->{'rpm_name'}/){
				 	GWInstaller::Host::install_package($my_package_file);
				 	unless($pk->is_installed()){
				
				 		GWCursesUI::error_msg("Could not install the package $my_package_file.\nPlease check the log for errors and install this package manually.",$cui);
				 		exit_installer();
				 	}
				 	}
				 else{
				 	GWCursesUI::error_msg("A valid RPM file was not selected.",$cui);
				 	GWInstaller::Dialogs::prereq_dialog($pk,$cui);
				 	 }
						#install succeeded	
						if($pkg->is_installed()){
							$cui->dialog("Install of $pkg->{'name'} succeeded. Click OK to continue");
							check_prereqs();
						}
						#install failed
						else{
							GWCursesUI::error_msg("I was unable to install the package.Please check the log for errors and install $pkg->{'name'} manually before continuing.",$cui);
						   exit_installer();
							}
						
					}
				}
				else{
					exit_installer();
				}
				
			}
					
		} #end if pkg->is_installed
		else{
			
			if($pkg->{'rpm_name'} =~ /^MySQL/){
				$msg = "A compatible version of MySQL is not installed on your system. It is a prerequisite to GroundWork Monitor. Please refer to the GroundWork Monitor 5 step installation process at:http://support.groundworkopensource.com/downloads.";
				GWCursesUI::error_msg($msg,$cui);
				exit_installer();
			}
 
			GWCursesUI::status_msg('Missing Prerequisite package: ' . $pkg->{'rpm_name'},$cui);
			GWInstaller::Dialogs::prereq_dialog($pkg,$cui);
			
		}
			
	}
		GWCursesUI::status_msg("All prerequisites satisfied",$cui);
	    sleep $sleep;
	    return 1;
}
	
	
 
 

 


sub verify_net_config{
 	$verify = 0;
 	
 	#hostname
 	GWCursesUI::status_msg("Verifying hostname...",$cui);
	$hostname = $host->get_hostname();
	if($hostname){
		GWLogger::log("\thostname is $hostname");
		$verify = 1;
	}
	else{
		GWCursesUI::error_msg("Hostname is not set. Please set hostname before attempting to install GroundWork Monitor.",$cui);
		exit_installer();
	}
	
	#name resolution
  	$has_dns_entry = `host $hostname | grep -cv 'not found'`;
  	chomp($has_dns_entry);
 	
	#/etc/hosts file
	$hostsfile_is_compliant = GWInstaller::Host::verify_hosts_file($has_dns_entry);
	 if($hostsfile_is_compliant){
	 	GWCursesUI::status_msg("\t/etc/hosts file verified.",$cui);
	 }
	 else{
	 	GWCursesUI::status_msg("\t/etc/hosts file not compliant.",$cui);
	 	
	 	$title = "hosts file needs fixing";
	 	$msg = "Your /etc/hosts file is not compliant. Shall the installer attempt to repair it?";
	 	$doIt = GWCursesUI::yes_or_no_dialog($title,$msg,$cui);

	 	unless($doIt){
	 		$errmsg = "You will need to manually repair your hosts file before continuing. Please see the log for details.";
	 		GWCursesUI::error_msg($errmsg,$cui);
	 		exit_installer();
	 	}
	 	
	 	
	 	GWCursesUI::status_msg("Attempting to rectify /etc/hosts file.",$cui);
	 	GWInstaller::Host::repair_hosts_file();
	 	unless(GWInstaller::Host::verify_hosts_file($has_dns_entry)){
	 		$msg = "Unable to rectify problems with /etc/hosts file.\n" .
	 				"Please make sure this entry is present in the file:\n" .
	 				"<your_ip_address> <your_hostname>";
	 		GWCursesUI::error_msg($msg,$cui);
	 		exit_installer();
	 	}
	 }
	 
	 
  
  return $verify;
	
}

 
 
sub verify_config{
    GWCursesUI::status_msg("Verifying network configuration...",$cui);
    sleep $sleep;
 	verify_net_config();
    
    GWCursesUI::status_msg("Network configuration verified",$cui);
    sleep $sleep;
    

   
	 
	
 
    sleep $sleep;
     GWCursesUI::status_msg('Verifying Java configuration...',$cui);
    sleep $sleep;
   @fixlist = GWInstaller::Host::verify_java_config($prefs);
   $fixSize = @fixlist;
 	if($fixlist[0] ne ""){
		GWCursesUI::status_msg('Java configuration is non compliant.',$cui);
		sleep $sleep;
	 	
	 	$title = "Java config";
	 	$msg = "Your java config is not compliant. Shall the installer attempt to repair it?";
	 	$doIt = GWCursesUI::yes_or_no_dialog($title,$msg,$cui);
	 	
	 	unless($doIt){
	 		$errmsg = "You will need to manually repair your Java config before continuing. Please take the following steps:\n" .
						  "1) correctly set JAVA_HOME in /etc/profile\n" .
	    	 			  "2) update /etc/alternatives/java to point to the installed Java SDK\n".
	    	 			  "3) update /usr/bin/java link to point to /etc/alternatives/java\n" . 
	    	 			  "4) reload environmental variables from /etc/profile with the command: `source /etc/profile`";

			GWCursesUI::error_msg($errmsg,$cui);
	 		exit_installer();
	 	}

		GWCursesUI::status_msg("Attempting to fix Java config.",$cui);
		  $fixSize = @fixlist;
		
 	 	
	 			    
		if(GWInstaller::Host::fix_java_config(\@fixlist)){ #@fixlist \@fixlist
			 GWCursesUI::status_msg('Re-verifying Java config',$cui);
			@fixlist = GWInstaller::Host::verify_java_config($prefs);
			if($fixlist[0] eq ""){
				 GWCursesUI::status_msg('Java configuration rectified.',$cui);
			}
			else{
				GWInstaller::Dialogs::java_not_verified_dialog($cui);
				exit_installer();
			}
	    	 sleep $sleep;
 		}
		 else{
	    	 	GWInstaller::Dialogs::java_not_verified_dialog($cui);
	    	 	sleep $sleep;
	    	 	GWCursesUI::error_msg("unable to rectify",$cui);
				exit_installer();
	    	 }
	}
	else{
		GWCursesUI::status_msg("\tJava installation verified.",$cui);
		sleep $sleep;
		 
	}
    	 
    	 
    
    sleep $sleep;
     GWCursesUI::status_msg('Verifying MySQL configuration...',$cui);
    sleep $sleep;
    
    #Check if MySQL processes are running
    if(GWInstaller::Host::mysql_is_running()){
    	GWCursesUI::status_msg("\tMySQL process is running.",$cui);
    }
    else{
    	$err = 	"MySQL is not currently running. Please start the mysql service before continuing.\n" .
    			"You may also need to configure MySQL to start with runlevel 3 and runlevel 5.";
    	GWCursesUI::error_msg($err,$cui);
    	exit_installer();
    	
    }
    
    
    #Get password if necessary
    if(GWInstaller::Host::mysql_passwd_set()){
    	GWInstaller::Dialogs::mysql_dialog($cui);
    }

	#Try out MySQL connection
	if(GWInstaller::Host::mysql_can_connect()){
		GWCursesUI::status_msg("\tConnection to MySQL: OK",$cui);
	}
	else{
		$msg = "Unable to connect to MySQL instance. Please refer to log for more information.\n" . 
				"Restarting the MySQL service may remedy this problem.";
		GWCursesUI::error_msg($msg,$cui);	
		exit_installer();
	}

 	 GWCursesUI::status_msg("\tMySQL installation verified.",$cui);
	 
    
    #$cui->setprogress(39,'Verifying syslog-ng configuration...');
    sleep $sleep;
}

 

sub is_valid_upgrade{
	$validity = 0;
	
	$installed_version = $gwmonitor->get_installed_version();
	($installed_major,$installed_minor,$installed_patch) = split(/\./,$installed_version);
	
	$package_version = $prefs->get_version_from_core();
	($package_major,$package_minor,$package_patch) = split(/\./,$package_version);
	
	$valid_upgrade_string = $prefs->get_valid_upgrades();
#	GWLogger::log("valid_upgrade_string =   $valid_upgrade_string");
	@valid_upgrades = split(/\,/,$valid_upgrade_string);
	foreach $ver (@valid_upgrades){
	 
		if($installed_version eq $ver){
			$validity = 1;
		}
	}
	
	if($installed_version eq $package_version){
		$validity = 0;
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
			
	    	if(GWInstaller::Host::is_rpm_installed("groundwork-bookshelf")){
	    		GWCursesUI::error_msg("There was a problem removing the groundwork-bookshelf RPM. Please see log for more details.",$cui);
	    		GWInstaller::exit_installer();
	    	}	    	
	    	
    		if(GWInstaller::Host::is_rpm_installed("groundwork-monitor-pro")){
	   			$cui->setprogress(20,"Removing GroundWork Pro RPM...");
	    	 	GWLogger::log("Removing GroundWork Pro RPM...");
	   			$gwmonitor->uninstall_pro();			
    		
    
    		
	    	if(GWInstaller::Host::is_rpm_installed("groundwork-monitor-pro")){
	    		GWCursesUI::error_msg("There was a problem removing the groundwork-monitor-pro RPM. Please see log for more details.",$cui);
	    		GWInstaller::exit_installer();
	    	}
    		}
    		
    		
    		$cui->setprogress(40,"Removing GroundWork Core RPM...");
    		GWLogger::log("Removing GroundWork Core RPM...");
    		$gwmonitor->uninstall_core();
   			if( GWInstaller::Host::is_rpm_installed("groundwork-monitor-core")){
	    		GWCursesUI::error_msg("There was a problem removing the groundwork-monitor-core RPM. Please see log for more details.",$cui);
   				GWInstaller::exit_installer();
   			}  
    		
    		$cui->setprogress(60,"Removing GroundWork Foundation RPM...");
    		GWLogger::log("Removing GroundWork Foundation RPM...");
    		$gwmonitor->uninstall_foundation();
    		
    		if(GWInstaller::Host::is_rpm_installed("groundwork-foundation-pro")){
	    		GWCursesUI::error_msg("There was a problem removing the groundwork-foundation-pro RPM. Please see log for more details.",$cui);
    			GWInstaller::exit_installer();
    		}
	  
    		
    	return $retval;
	
}

#rename Quit
sub exit_installer{
	$msg = "Exiting installer.";
	$cui->dialog($msg);
	GWLogger::log($msg);
	exit(0);
	
}
sub install_failed{
	GWCursesUI::error_msg("The installation has failed. Please consult the log for further details.",$cui);
	exit_installer();
}
1;
