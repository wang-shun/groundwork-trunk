#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use lib qq(/usr/local/groundwork/log-reporting/lib);
use DBLib;
use LogFileType;
use LogFileTypeControl;
use LogDirectoryControl;
use LogMessageTypeControl;
use LogMessageClassControl;
use ComponentType;
use ComponentTypeControl;
use LogMessageFilter;
use LogMessageFilterControl;
use LogDirectory;
use RegexTester; 
use RegexTesterControl;
use GWLogger;
use GWUtils;

$DEBUG = 0;

$CGIquery = CGI->new();
DBLib::initDB();
#
# set default params then set params
#
$struct =  $CGIquery->param("struct"); #tables in DB
$action =  $CGIquery->param("action"); #add, del, list
$listOption = $CGIquery->param("listOption"); #raw, htmlControl or htmlRefresh
 # struct=LogFileType&action=list&listOption=control
 
#Test Parameters
#$struct = "LogMessageClassEdit";
#$action = "edit";
#$logDirectory ="/var/log";
#$listOption = "Security";
GWLogger::log("struct=$struct act=$action lo=$listOption");
print `echo 'struct=$struct act=$action lo=$listOption' >> /tmp/logme`; 
main();
 
sub main{   
#debug("<PRE>");
#debug ("PARAMETERS: struct=$struct action=$action listOption=$listOption");
DBLib::initDB();
#print "Content-type: text/xml\n\n";

############################# PARSE PARAMETERS #################################
# LogDirectory
if($struct eq "LogDirectory"){
	#GWLogger::log("struct eq LogDirectory");
	$logDirectory = $CGIquery->param("listOption");	
	if($action eq "add"){
		GWLogger::log("reports.pl:Adding LogDirectory: " . $logDirectory);
		$valid = LogDirectory::add($logDirectory);
		LogDirectoryControl::draw($valid);
		}
	elsif($action eq "del"){
		$valid = LogDirectory::delete($logDirectory);
			LogDirectoryControl::draw($valid);
	}
	elsif($action eq "list"){
		if($listOption ne ""){
			#listLogDirectory();
			LogDirectoryControl::draw(1);
		}
	}

 
	else{ sendInvalidRequest();}
 }
 
#LogFilenameFilter
elsif($struct eq "LogFilenameFilter"){
		$logType = $CGIquery->param("LogFileType");
		$regex = $CGIquery->param("regex");	
		if($action eq "add"){
			addLogFileFilter($logType,$regex);
			printLogFileFilterCtl();
		}
		elsif($action eq "del"){
			deleteLogFileFilter($logType,$regex);
			printLogFileFilterCtl();
		}
		elsif($action eq "list"){	
			printLogFileFilterCtl();
			if($listOption ne ""){
				#listLogFilenameFilter();
				}
			 
		}
		 
}

#LogFileType #consolidate with lftFilter
elsif($struct eq "LogfileType"){
	#GWLogger::log("Table: LogfileType");
	$logfileType = $CGIquery->param("listOption");
	if($action eq "add"){
	    ($type,$fil) = split(/::/,$logfileType);
		LogFileType::addType($type,$fil);
		$lftCtl = LogFileTypeControl->new();
		$lftCtl->draw();
	}
	elsif($action eq "del"){
		LogFileType::deleteType($logfileType);
		LogFileTypeControl::draw();
		
		}

	elsif($action eq "save"){
		($type,$fil) = split(/::/,$logfileType);
		saveFilter($type,$fil);	
	}
 
	 
}

elsif($struct eq "lftFilter"){
	$logfileType = $CGIquery->param("listOption");
  if($action eq "edit"){
		LogFileTypeControl::edit($logfileType);
	}
	elsif($action eq "add"){
	    ($type,$fil) = split(/::/,$logfileType);
	     
		LogFileType::addType($type,$fil);
	    
		$lftCtl = LogFileTypeControl->new();
		$lftCtl->draw();
	}
	elsif($action eq "addFilter"){
			 ($filter,$typeName) = split(/::/,$logfileType);
			 if($filter ne ""){
		   	LogFileType::addFilter($typeName,$filter);
			 	    }
		   	LogFileTypeControl::edit($typeName);	
	}
	elsif($action eq "delFilter"){
		    ($filter,$typeName) = split(/::/,$logfileType);
		   	LogFileType::delFilter($typeName,$filter);
		   	LogFileTypeControl::edit($typeName);	
	}
	
}

#LogMessageClass
elsif($struct eq "LogMessageClass"){
	$logMessageClass= $CGIquery->param("listOption");
	if($action eq "add"){
		LogMessageClass::addClass($logMessageClass);
		LogMessageClassControl::draw();
		}
	elsif($action eq "del"){
		LogMessageClass::deleteClass($logMessageClass);
		LogMessageClassControl::draw();
	}
	elsif($action eq "list"){
		if($listOption ne ""){
			listLogMessageClass($listOption);
		}
    
	}
	 
}
 
#LogMessageFilter
elsif($struct eq "LogMessageFilter"){
	#GWLogger::log("matched LogmessageFilter action = ${action}");
	$logMessageFilter = $CGIquery->param("LogMessageFilter");

	 if($action eq "save"){
		#GWLogger::log("saving...");
	    LogMessageFilter::saveLogMessageFilter($listOption);	
	    #LogMessageFilterControl::draw(1);
  	}
	elsif($action eq "del"){
		GWLogger::log("Deleting " . $listOption);
		LogMessageFilter::deleteLogMessageFilter($listOption);
 		LogMessageFilterControl::draw(1);
		}
	elsif($action eq "refresh"){
		 LogMessageFilterControl::draw(1);
	}
	elsif($action eq "listXML"){
		print "Content-type: text/xml\n\n";
	 	LogMessageFilter::getFilterListXML();
	}
		else {  sendInvalidRequest();}
		
	
	 
}


elsif($struct eq "LogMessageFilterRefresh"){
	#GWLogger::log("matched LogmessageFilter action = ${action}");
	$logMessageFilter = $CGIquery->param("LogMessageFilter");

	 if($action eq "save"){
		#GWLogger::log("saving...");
		print "Content-type: text/html\n\n";
        print " ";
	    LogMessageFilter::saveLogMessageFilter($listOption);	
  	}
	elsif($action eq "del"){
		   print " ";
		GWLogger::log("Deleting " . $listOption);
 
		LogMessageFilter::deleteLogMessageFilter($listOption);
		print "Content-type: text/html\n\n";
		print " ";
 	#	LogMessageFilterControl::draw(1);
		}
	elsif($action eq "control"){
		 LogMessageFilterControl::draw("Control");
	}
		else {  sendInvalidRequest();}
	
	 
}

elsif($struct eq "LogMessageFilterForm"){
        #GWLogger::log("matched LogmessageFilter action = ${action}");
        $logMessageFilter = $CGIquery->param("LogMessageFilter");

         if($action eq "refresh"){
                 print "Content-type: text/html\n\n";
                 LogMessageFilterControl::printControlForm();
        }
        elsif($action eq "del"){
            print " ";
            GWLogger::log("Deleting " . $listOption);

            LogMessageFilter::deleteLogMessageFilter($listOption);
            print "Content-type: text/html\n\n";
            LogMessageFilterControl::printControlForm();
        }
        elsif($action eq "save"){
                #GWLogger::log("saving...");
            print "Content-type: text/html\n\n";
            LogMessageFilter::saveLogMessageFilter($listOption);
            LogMessageFilterControl::printControlForm();
        }

        else {  sendInvalidRequest();}



}


#LogMessageType
elsif($struct eq "LogMessageType"){
	$LogMessageType= $CGIquery->param("listOption");
	if($action eq "add"){
		LogMessageType::addType($LogMessageType);
		LogMessageTypeControl::draw();
		}
	elsif($action eq "del"){
		LogMessageType::deleteType($LogMessageType);
		LogMessageTypeControl::draw();
	}
	elsif($action eq "list"){
		if($listOption ne ""){
			listLogMessageType($listOption);
		}
		 
	}
 
}
#LogMEssageTypEdit
elsif($struct eq "LogMessageTypeEdit"){
	if($action eq "edit"){
		
	$type = $CGIquery->param("listOption");
	LogMessageTypeControl::edit($type);
	}
	
	elsif($action eq "save"){
		$listOption = $CGIquery->param("listOption");
		my ($type,$per,$grouping) = split(',',$listOption);
		LogMessageTypeControl::edit($type,$per,$grouping);
		}
}
#LogMessageClassEdit
elsif($struct eq "LogMessageClassEdit"){
	if($action eq "edit"){
		
	$class = $CGIquery->param("listOption");
	 LogMessageClassControl::edit($class);
	}
	
 	elsif($action eq "save"){
		$listOption = $CGIquery->param("listOption");
		my ($type,$per,$grouping) = split(',',$listOption);
		LogMessageClassControl::edit($type,$per,$grouping);
		}
	elsif($action eq "addSub"){
		my ($subType,$messageClass) = split('ZZ',$listOption);
	   #GWLogger::log("report : $subType, $messageClass, $listOption");
		LogMessageClass::addSubType($subType,$messageClass);	
	}
	elsif($action eq "remSub"){
		my ($subType,$messageClass) = split('ZZ',$listOption);
	   #GWLogger::log("report : $subType, $messageClass, $listOption");
		LogMessageClass::delSubType($subType,$messageClass);	
	}
}

#LogMEssageClassEditPersistence
elsif($struct eq "LogMessageClassEditPersistence"){
	if($action eq "edit"){
		$type = $CGIquery->param("listOption");
		printLogMessageClassEditPersistence($type);
		}
	
	elsif($action eq "save"){
		$listOption = $CGIquery->param("listOption");
		my ($type,$per) = split(',',$listOption);
		printLogMessageClassEditPersistence($type,$per);
		}
}

elsif($struct eq "ComponentType"){
	$componentType=$CGIquery->param("listOption");
	if($action eq "add"){
		ComponentType::addType($componentType);
		ComponentTypeControl::draw();
		}
	elsif($action eq "del"){
		ComponentType::deleteType($componentType);
		ComponentTypeControl::draw();
	}
	elsif($action eq "list"){
		if($listOption ne ""){
			listComponentType($listOption);
		}
	}
}

 
elsif($struct eq "dbControl"){
	#GWLogger::log("struct eq dbControl");
	if($action eq "save"){
		my ($dbName,$dbHost,$user,$password) = split('ZZZ',$listOption);	
		DBLib::saveDBConfig($dbName,$dbHost,$user,$password);
		#GWLogger::log("about to call saved");
		DBLib::printDatabaseCtl("Saved.");
		exit(0);
	}	
	elsif($action eq "test"){
		my ($dbName,$dbHost,$user,$password) = split('ZZZ',$listOption);
		$returnMsg = DBLib::testDBConfig($dbName,$dbHost,$user,$password);
		DBLib::printDatabaseCtl($returnMsg);
	}	
}

 
elsif($struct eq "RegexTester"){
 print "Content-type: text/html\n\n";
  if($action eq "test"){
   my ($regex,$testString) = split('ZZZZ',$listOption);
   @matches = RegexTester::test($regex,$testString);
   $mRef = \@matches;
   RegexTesterControl::updateStatus($mRef);
 } 
} 

}#main

sub sendInvalidRequest{
print "<error>Invalid Request</error>";
}

