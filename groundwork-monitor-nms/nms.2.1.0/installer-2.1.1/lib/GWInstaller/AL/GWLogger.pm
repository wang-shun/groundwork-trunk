#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::GWLogger;
use Switch;

my $logFile;

sub new {
	my ($invocant,$logfile) = @_;
	my $class = ref($invocant) || $invocant;

	unless($logfile){$logfile = "system.log";}	 

	my $self = {
		logfile => $logfile
	};
  
	bless($self,$class);
    
   return $self;
}
   
# Writes log messages to logFile
# Arguments passed : log level, log message
# Returns : 1 on success and 0 on failure
#sub log{
#   #my($self,$logMessage,$logLevel) = @_;
#   my($logMessage,$logLevel) = @_;
#   my $retVal = 1;
#	unless($logLevel){$logLevel = 3;}
#	
#   switch($logLevel) {
#       case 0   {$logMessage = "Critical: ". $logMessage;}
#       case 1   {$logMessage = "Error: ". $logMessage;}       
#       case 2   {$logMessage = "Warning: ". $logMessage;}   
#       case 3   {$logMessage = "". $logMessage;}
#
#   }
#
#   open(LOGFILE,">>nms.log") || ($retVal = 0);
#   print LOGFILE $logMessage . "\n";
#   close(LOGFILE);
#   return $retVal;
#}

sub log{
	my $thislog;
	my $logMessage;
	my $argc = @_;
	
 
 	if($argc == 2){
    	 ($self,$logMessage,$logLevel) = @_;
  		$thislog = $self->{logfile};
 	}
	elsif($argc ==1){
		$logMessage = shift;
		$thislog = "nms.log";
	}
    
     my $retVal = 1;
	unless($logLevel){$logLevel = 3;}
	
   switch($logLevel) {
       case 0   {$logMessage = "Critical: ". $logMessage;}
       case 1   {$logMessage = "Error: ". $logMessage;}       
       case 2   {$logMessage = "Warning: ". $logMessage;}   
     #  case 3   {$logMessage = "". $logMessage;}

   } 
   open(LOGFILE,">>$thislog") || ($retVal = 0);
   print LOGFILE $logMessage . "\n";
   close(LOGFILE);
   return $retVal;
}


# Sets log message level to Critical and call log method accordingly
# Arguments passed: 
# Returns : 1 on success and 0 on failure 
sub logCritical{
   my ($self,$logMessage) = @_;
   return $self->log(0, $logMessage);

}


# Sets log message level to Error and call log method accordingly
# Arguments passed:
# Returns : 1 on success and 0 on failure

sub logError{
 	my ($self,$logMessage) = @_;
	return $self->log(1, $logMessage);
        
}  

# Sets log message level to Warning and call log method accordingly
# Arguments passed:
# Returns : 1 on success and 0 on failure

sub logWarning{
 	my ($self,$logMessage) = @_;
	return $self->log(2, $logMessage);

}

# Sets log message level to Info and call log method accordingly
# Arguments passed:
# Returns : 1 on success and 0 on failure

sub logInfo{
 	my ($self,$logMessage) = @_;
	return $self->log(3, $logMessage);

}
 
1;
