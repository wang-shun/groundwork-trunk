#!/usr/bin/perl

package TestGWLogger;
use lib qw(../);
use GWInstaller::AL::GWLogger;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
        $pass = 0;
        $prop = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
        $prop->isa(GWInstaller::AL::GWLogger)?($pass = 1):($pass = 0);
        return $pass;
}

 sub test_log{
    $myLog = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
    $retVal = $myLog->log("Whatever");
  #  ($prop->logNms(0, "Test log message"))?($pass = 1):(return 0);
  #  ($prop->logNms(1, "Test log message"))?($pass = 1):(return 0);
  #  ($prop->logNms(2, "Test log message"))?($pass = 1):(return 0);
  #  ($prop->logNms(3, "Test log message"))?($pass = 1):($pass = 0);
     return $retVal;
}

sub test_logCritical{
    $prop = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
    ($prop->logCritical("Test log message"))?($pass = 1):($pass = 0);
    return $pass;
}

sub test_logError{
    $prop = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
    ($prop->logError("Test log message"))?($pass = 1):($pass = 0);
    return $pass;
}

sub test_logWarning{
    $prop = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
    ($prop->logWarning("Test log message"))?($pass = 1):($pass = 0);
    return $pass;
}

sub test_logInfo{
    $prop = GWInstaller::AL::GWLogger->new("/tmp/nmsinstaller.log");
    ($prop->logInfo("Test log message"))?($pass = 1):($pass = 0);
    return $pass;
}
