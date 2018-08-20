#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
 
use lib qw(../GWTest);
use lib qw(../);

use TestFrame;
use GWTest; 

use GWInstaller::AL::Database;
use GWInstaller::AL::Properties;
use GWInstaller::AL::GWLogger;
use GWInstaller::AL::Software;
use GWInstaller::UI::SoftwareCursesUI;
use GWInstaller::UI::DBCursesUI;
use GWInstaller::AL::EventBroker;
use GWInstaller::AL::Foundation;
use GWInstaller::AL::GWMonitor;
use GWInstaller::AL::Host;
use GWInstaller::AL::httpd;

use GWNMSInstaller::AL::ntop;
use GWNMSInstaller::AL::Weathermap;
use GWNMSInstaller::AL::Cacti;
use GWNMSInstaller::AL::NeDi;
use GWNMSInstaller::AL::NMSProperties;
use GWNMSInstaller::AL::GWNMS;
use GWNMSInstaller::GWNMSInstaller;
use GWNMSInstaller::UI::NMSCursesUI;

use GWNMSInstaller::AL::CactiPackage;
use GWNMSInstaller::AL::NeDiPackage;
use GWNMSInstaller::AL::ntopPackage;
use GWNMSInstaller::AL::WeathermapPackage;
use GWNMSInstaller::AL::automationPackage;

use TestDatabase;
use TestProperties;
use TestGWLogger;
use TestSoftware;
use TestEventBroker;
use TestFoundation;
use TestGWMonitor;
use TestHost;
use Testhttpd;
use Testntop;
use TestWeathermap;
use TestCacti;
use TestNeDi;
use TestNMSProperties;
use TestGWNMS;
use TestGWNMSInstaller;
use TestNMSCursesUI;

use TestSoftwareCursesUI;
use TestDBCursesUI;


#
our $nmslog  =   GWInstaller::AL::GWLogger->new("/tmp/unittest.log");


# Default Values
$verbose = 0;
$format = "term";
$project = "NMS 2.1 Installer";
foreach $arg(@ARGV){ 
	chomp($arg);
	if($arg eq "-v"){
		$verbose=1;
	}
	elsif($arg eq "-html"){
		$format = "html";
	}
	elsif($arg eq "-term"){
		$format = "term";
	}
	elsif($arg eq "-build"){
		$format = "build";
	}
}


$testEnv = TestFrame->new($project,$format,$verbose);
$testEnv->addTest("TestDatabase");
$testEnv->addTest("TestProperties");
$testEnv->addTest("TestSoftware");
$testEnv->addTest("TestGWLogger");
$testEnv->addTest("TestEventBroker");
$testEnv->addTest("TestFoundation");
$testEnv->addTest("TestGWMonitor");
$testEnv->addTest("TestHost");
$testEnv->addTest("Testhttpd");
$testEnv->addTest("Testntop");
$testEnv->addTest("TestWeathermap");
$testEnv->addTest("TestCacti");
$testEnv->addTest("TestNeDi");
$testEnv->addTest("TestNMSProperties");
$testEnv->addTest("TestGWNMS");
$testEnv->addTest("TestGWNMSInstaller");
$testEnv->addTest("TestNMSCursesUI");
$testEnv->addTest("TestSoftwareCursesUI");
$testEnv->addTest("TestDBCursesUI");


$testEnv->run();


