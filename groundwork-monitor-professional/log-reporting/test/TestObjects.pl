#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use Term::ANSIColor;
use Term::ANSIColor qw(:constants);
use lib qw(/usr/local/groundwork/log-reporting/lib);
 
use TestLogFile;
use TestLogDirectory;
use TestParsingRule;
use TestComponentType;
use TestLogFileType; 

use TestLogMessageClass;
use TestLogMessageFilter;
use TestLogMessageType;
use TestLogType;
use TestDBLib;
use TestFrame;
use GWTest;
use TestConsolidator;
use TestGWLogger;
use TestImporter; 
  
$testEnv = TestFrame->new("Log Reporting");
$testEnv->addTest("TestParsingRule");
$testEnv->addTest("TestComponentType");
$testEnv->addTest("TestLogFile");
$testEnv->addTest("TestLogDirectory");
$testEnv->addTest("TestLogMessageFilter");
$testEnv->addTest("TestLogMessageType");
$testEnv->addTest("TestDBLib");
$testEnv->addTest("TestLogFileType");
$testEnv->addTest("TestLogMessageClass");

$testEnv->addTest("TestConsolidator");
$testEnv->addTest("TestImporter");
$testEnv->addTest("TestGWLogger");
print "Content-type: text/html\n\n";
$testEnv->run();


