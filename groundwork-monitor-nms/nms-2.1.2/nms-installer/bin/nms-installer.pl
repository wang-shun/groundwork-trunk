#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use lib "./lib"; 
use GWNMSInstaller::GWNMSInstaller;

use Curses::UI;
use GWNMSInstaller::AL::CactiPackage;
use GWNMSInstaller::AL::NeDiPackage;
use GWNMSInstaller::AL::ntopPackage;
use GWNMSInstaller::AL::WeathermapPackage;
use GWNMSInstaller::AL::automationPackage;

$| = 1;

print "starting...\n";
$inst= GWNMSInstaller::GWNMSInstaller->new();

if($inst->isa(GWNMSInstaller::GWNMSInstaller)){
print "running..\n";
 
	$inst->run();
	
print "here\n";
}
else{
	print "WTF\n";
}
