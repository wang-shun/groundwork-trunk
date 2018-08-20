#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use CGI;
use lib qq(/usr/local/groundwork/log-reporting/lib);
use DBLib;
use LogFileTypeControl;
use LogDirectoryControl;
use LogMessageTypeControl;
use LogMessageClassControl;
use LogMessageFilterControl;
use ComponentTypeControl;
use RegexTesterControl;
use GWUtils;


DBLib::initDB();  
GWUtils::printHeader('html');  
  
$CGIquery = CGI->new();
$control =  $CGIquery->param("control");

if($control eq "LogFileType") {
	$lftControl = new LogFileTypeControl();
	$lftControl->draw();
	} 
	
elsif ($control eq "MessageFilters") {
	LogMessageFilterControl::draw();			 
			} 
elsif ($control eq "MessageClass") {
	LogMessageClassControl::draw();
			}
elsif ($control eq "LogDirectory") {

	
	print "<body><div id='LogDirectory'>";
	LogDirectoryControl::draw(1);
	print "</div></body>";
		} 
elsif ($control eq "Components") {
	ComponentTypeControl::draw();
			}
elsif ($control eq "MessageType") {
	LogMessageTypeControl::draw();
  
}					
elsif ($control eq "Database"){
	print "<body>";
	print "<div id='dbControl'>";
 	DBLib::printDatabaseCtl();						
 	print "</div>";
 	print "</body>";
}
elsif($control eq "RegexTester"){
    RegexTesterControl::draw();
}

else {
	print "<body>";
	print "<div id='dbControl'>";
 	DBLib::printDatabaseCtl();						
 	print "</div>";
 	print "</body>";
} 
				  