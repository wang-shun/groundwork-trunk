--Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
--All rights reserved. Use is subject to GroundWork commercial license terms. 

insert into LogDirectory(logDirectory,isProcessed) values('/tmp/log','0');
insert into LogfileType(typeName) values('Linux');
insert into LogFile(logFileName,logfileTypeID,logDirectoryID) values('sample.log','1','1');
insert into LogFilenameFilter(regex,logfileTypeID) values('*.log','1');
insert into LogMessageType(logMessageTypeName) values('SSH');
insert into ParsingRule(parsingRuleName,logfileTypeID,parsingRuleText,logMessageTypeID) values('Authentication failure','1','sshd.?(\d+) more authentication failures;.*?\s+rhost=(.*?)\s+user=(\S+)','1');
insert into ComponentType(componentTypeName) values('user');
