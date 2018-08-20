#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use lib qq(/usr/local/groundwork/log-reporting/lib);
use DBLib;
use LogDirectory;
use LogFile;
use ParsingRule;
use GWLogger;
$logFileType;
@matches;

doImport();

sub doImport
{ 
 	DBLib::initDB();	
	@logDirectories = LogDirectory::getLogDirectories();
	
	$dirCnt = @logDirectories;
	GWLogger::log("Found $dirCnt Log Directories");
	if($dirCnt > 0){
		processLogDirs(@logDirectories);
	}
}

sub processLogDirs
{
	#my @logDirectories = shift;
	 
	foreach $dir (@logDirectories) { 	
		$fileCnt = 0;
		GWLogger::log("Processing DIR: " . $dir->getName());
		#GWLogger::log("Processing ID: " . $dir->getID());
		@fileList = $dir->getFileList();
		$fileCnt = @fileList;
		GWLogger::log("Found $fileCnt files.\n");
		foreach $file (@fileList){ 
		        
	 	 			#unless an Orphan, read file and import matches
				 	unless($file->getType() eq "Orphan"){ 
		 				importFile($file);  
				 	}#unless orphan	 	 
		 }#foreach file
	}#foreach dir
}

sub importFile
{ 
	$fileObj = shift;
	$dir = shift;
	
	GWLogger::log("file: " . $fileObj->getName());	
	my $matchCnt;
	my $line; 
	my $matchSubTotal = 0;
	my $skipSubTotal = 0;
	$filepath =  $fileObj->getPath() . $fileObj->getName();  #File Path/Name
	
	#Get Filters for this Type of Log 
	print "type: " . $fileObj->getType() . "\n";
	@parsingRuleList = ParsingRule::getRulesByLogType($fileObj->getType());

	open(LOGFILE,$filepath) || debug("Import Failed: COULDNT OPEN $filepath");
	seek(LOGFILE,$fileObj->getSeekPos(),0);
	
 	while(<LOGFILE>){
		$line = $_;
		chomp($line);	 
        my $hasMatched = 0;
        
		#for each line, Try All Filters for this Log Type 
		foreach $parsingRule (@parsingRuleList){
			$reg = $parsingRule->getRegex();
		 #	print "Trying $reg\n";
			@matches = ($line =~ /$reg/);
			$matchCnt = @matches;


			#If you have a Match, process line
			if($matchCnt > 0){
				
				$timestamp = calculateTimestamp($line);
				 
				if($timestamp != 0){
				    $hasMatched = 1;
				    processLine($parsingRule,$fileObj,$timestamp);
				}
				else{
				   print "Couldn't parse timestamp: $line\n";
				}
				next; #verify this breaks the foreach columbia
			} #if
			
		}#foreach
		
		
		if($hasMatched){
		 $matchSubTotal++;
		}
		else{
		 #print "Skipped: $line\n";
		$skipSubTotal++;
		}
	}#while
 	$fileObj->setSeekPos(tell(LOGFILE));
	$fileObj->setIsProcessed(1);
	print "matched $matchSubTotal\n";
	print "skipped $skipSubTotal\n\n";
}



sub processLine{
 ###need to pass @matches correctly match- global - kludge
 my ($parsingRule,$fileObj,$timestamp) = @_;    
    
# Insert Entry into LogMessage
	$query = qq{
				INSERT INTO LogMessage(parsingRuleID,logFileID,timestamp)
				VALUES('$parsingRule->{id}','$fileObj->{id}','$timestamp');
	};
	$sth = DBLib::executeQuery($query);
	$logMessageID = $sth->{mysql_insertid};
	
    
# Insert Component Values	

 	#get component types for the Parsing Rule that Matched  
	@componentList = $parsingRule->getComponentTypes();
	my $componentTypeCnt = @componentList;

	# iterate through component Types/matches and send the values to DB
	for($i = 0;$i<$componentTypeCnt;$i++){
		insertComponent($logMessageID,$componentList[$i]->{id},$matches[$i]);
	}#for
}

# calculateTimestamp()
# input format: Jul 19 05:18:28
# output format: YYYY-MM-DD HH:MM:SS
sub calculateTimestamp{


	$inLine = shift;
	my $thisTime = substr($inLine,0,15);
	($mm,$dd,$tt) = split(/\s+/,$thisTime);
	($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
	$mos{'Jan'} = 1;
	$mos{'Feb'} = 2;
	$mos{'Mar'} = 3;
	$mos{'Apr'} = 4;
	$mos{'May'} = 5;
	$mos{'Jun'} = 6;
	$mos{'Jul'} = 7;
	$mos{'Aug'} = 8;
	$mos{'Sep'} = 9;
	$mos{'Oct'} = 10;
	$mos{'Nov'} = 11;
	$mos{'Dec'} = 12;
	$year+=1900;
	$tStamp = "$year-$mos{$mm}-$dd $tt";
	 
	if(isNum($mos{$mm}) && isNum($dd)){
		return $tStamp;	
	}
	else{ return 0;}
}

 sub isNum{
    $num = shift;
    if($num =~ /^\d+$/){return 1;}
    else{return 0;}

}

  

#component class?
sub insertComponent{
	$logMessageID = shift;
	$componentTypeID = shift;
	$componentValue = shift;
   #print "CVL: $componentValue\n";
  #  //SELECT ComponentValue
  $componentValueID = null;
    $query = qq{
    	SELECT componentValueID 
    	FROM ComponentValue
    	WHERE componentValue = '$componentValue'
    };
    $sth = DBLib::executeQuery($query);
    $sth->bind_col(1,\$componentValueID);
    $sth->fetch();
   # print "COMPONENTVALUEID: $componentValueID\n";
 #   //IF BLANK, INSERT ComponentValue
    if($componentValueID == null){
    	$componentValueID = insertComponentValue($componentValue);
    }
    
	$query = qq{
			insert into Component(	componentTypeID,
									componentValueID,
									logMessageID)
						values(		'$componentTypeID',
									'$componentValueID',
									'$logMessageID');
			};
	$sth=DBLib::executeQuery($query);
	$sth->finish();
}
 
sub insertComponentValue{
	$componentValue = shift;
	GWLogger::log("CV: $componentValue");
#	print "CV: $componentValue\n";
	$query = qq{
			insert into ComponentValue(componentValue)
						values('$componentValue');
			};	
   	$sth = DBLib::executeQuery($query);
   	$componentValueID = $sth->{mysql_insertid};
   	$sth->finish();
	return $componentValueID;
}

 

 sub debug{
 	my $text = shift;
 	print $text . "\n";	
 }
