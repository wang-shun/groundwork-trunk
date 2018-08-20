#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWInstaller::UI::DBCursesUI;

use lib qw(../../);
use GWInstaller::AL::GWLogger;
use GWInstaller::AL::Host;
use GWInstaller::AL::Software;
use GWInstaller::UI::GWCursesUI;
use File::Basename;


sub configure_db_dialog{
	
	
}

sub passwd_dialog{
		$cui = shift;
	$mysql_passwd = $cui->question('Please enter your MySQL password.');  
	GWInstaller::AL::GWLogger::log("MSQL PASSWORD RETURNED: $mysql_passwd");  
  
	unless($mysql_passwd){
		GWInstaller::UI::DBCursesUI::mysql_dialog($cui);
	}
	 
	$is_verified = GWInstaller::AL::Host::verify_MySQL_config($mysql_passwd);
	GWInstaller::AL::GWLogger::log("ISVERIFIED:$is_verified X");
    unless($is_verified){
    	GWInstaller::UI::GWCursesUI::error_msg("Unable to connect to mysql instance with the password entered. Please verify your password and try again",$cui);
    	GWInstaller::UI::DBCursesUI::passwd_dialog($cui);
    }
    return $mysql_passwd;
}


sub mysql_dialog{
		$cui = shift;
    $msg = "Your MySQL root user's password has been set. Please provide it at this time to continue the installation.";
    $return = $cui->dialog(
                            -title     => "MySQL Password",
                        -buttons   => [
 { 
              -label => '< Provide Password >',
              -value => 10,
              -shortcut => 'P'
               
            }, 
              'cancel'
],
                        -message => $msg
                                  );
    if($return == 10){ #Specify password
    	
    	$mysql_passwd = passwd_dialog($cui);
    	$ENV{'MYSQL_ROOT'} = $mysql_passwd; 
    	return $mysql_passwd;
    	
    }
    else{
    		$cui->dialog("Install cancelled.");
    		GWInstaller::AL::GWLogger::log("Install cancelled.");
   	 		GWNMSInstaller::GWNMSInstaller::exit();
    }
    
}

1;