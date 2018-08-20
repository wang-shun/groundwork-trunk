#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWInstaller::UI::SoftwareCursesUI;
use lib qw(../../);
use GWInstaller::AL::GWLogger;
use GWInstaller::AL::Host;
use GWInstaller::AL::Software;
use GWInstaller::UI::GWCursesUI;
use File::Basename;

sub available_packages_dialog{
	
}

sub previous_version_dialog{
	$cui = shift;
	    $msg =  "A previous version of GroundWork Monitor was found. What would you like to do?\n\n\n";
	$msg .= "UNINSTALL: Removes GroundWork Monitor from your system.\n\n";
    $msg .= "UPGRADE: Upgrade current installation to the version provided by this package.\n\n";
    $msg .= "CLEAN INSTALL: Warning! Deletes all user data\n\n";
    $msg .= "CANCEL: Leaves system untouched and quits installer.";
   
        my $return = $cui->dialog(
                        -title     => "GroundWork Monitor",
                        -selected => "3",
                        -buttons   => [       
          			
            { 
              -label => '< Upgrade >',
              -value => 10,
              -shortcut => 'U' 
            },
            
            { 
              -label => '< Uninstall >',
              -value => 5,
              -shortcut => 'N' 
            },
                          
           { 
              -label => '< Clean Install >',
              -value => 20,
              -shortcut => 'I', 
              -onpress => undef
            }, 'cancel'],
                        -message => $msg
                                  );
        return $return;
}

sub conflict_dialog
{ 
    $pk = shift;
    $cui = shift;
    $msg = "The package " . $pk->{rpm_name} . " conflicts with GroundWork Monitor. Would you like to uninstall this package? (Answering NO will abort installation)";
        my $return = $cui->dialog(
                        -title     => "Conflicting Package Found",
                        -buttons   => ['yes', 'no'],
                        -message => $msg
				  );

    if($return){
    	GWCursesUI::status_msg("Uninstalling  $pk->{rpm_name}",$cui);
    	GWLogger::log("Uninstalling " . $pk->{rpm_name});
    	$pk->uninstall();
    	
    	GWCursesUI::status_msg("Verifying uninstall of " . $pk->{rpm_name},$cui);
    	GWLogger::log("Verifying uninstall of " . $pk->{rpm_name});
    	
    	if($pk->is_installed()){
    		GWCursesUI::error_msg("I was unable to uninstall " . $pk->{'rpm_name'} . ". Installer can not proceed until this package is removed. Please uninstall it manually.",$cui);
  			GWInstaller::exit_installer();
    	}
    }
    else{
    	GWCursesUI::error_msg("Please uninstall " . $pk->{rpm_name} . " manually",$cui);
        GWInstaller::exit_installer();
    }
}

sub prereq_dialog
{
    $pk = shift;
    $cui = shift;
    $msg = "The package " . $pk->{'name'} . ", version: " . $pk->{'valid_version'} .  " is a prerequisite to GroundWork Monitor. Would you like to install this package at this time? (Answering NO will abort installation)";
        my $return = $cui->dialog(
                        -title     => "Missing Prerequisite",
                        -buttons   => ['yes', 'no'],
                        -message => $msg
				  );

    if($return){
	    $my_package_file = GWCursesUI::locate_package_dialog($pk,$cui);
	    unless($my_package_file){GWInstaller::exit_installer();}
	    $rpm_ver = GWInstaller::Software::get_rpm_version($my_package_file);
	     GWLogger::log("Selected RPM version: $rpm_ver");
	      ($justrpm,undef) = fileparse($my_package_file); 
	     
	     unless($my_package_file =~ /$pk->{'rpm_name'}/){
	     	$msg = "Please select an RPM package for $pk->{'rpm_name'}";
	     	GWCursesUI::error_msg($msg,$cui);
	     	prereq_dialog($pk,$cui);
	     }
	     GWLogger::log("Package is correct");
	     if( !($rpm_ver =~ $pk->{'valid_version'}) && ($pk->{'valid_version'} ne "ANY")){
	     		$msg = "The package $justrpm is version $rpm_ver. You need to select a package containing version $pk->{'valid_version'} of $pk->{'rpm_name'} ";
	     		GWCursesUI::error_msg($msg,$cui);
	     		prereq_dialog($pk,$cui);
	     }
	     GWLogger::log("Version is correct");

	    if($my_package_file =~ /$pk->{'rpm_name'}/){
	    
	    	
		 	GWInstaller::Host::install_package($my_package_file,$pk->{'rpm_name'},$cui);
		 	
		 	
		 	unless($pk->is_installed()){
			    
		
		 		GWCursesUI::error_msg("Could not install the package $my_package_file.\nPlease check the log for errors and install this package manually.",$cui);
		 		GWInstaller::exit_installer();
		 	}
		 	}
		 else{
	

		 	GWCursesUI::error_msg("A valid RPM file was not selected.",$cui);
		 	prereq_dialog($pk,$cui);
		 	 }
    }
    else{
    	GWCursesUI::error_msg("Install cancelled.",$cui);
        exit(0);
    }
}
1;