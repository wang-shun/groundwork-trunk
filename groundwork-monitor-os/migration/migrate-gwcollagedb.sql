#
# Migration script for GWCollageDB 4.5.0 to 4.5.3
#

use GWCollageDB;

# Update under-defined consolidation criterias
 
REPLACE INTO ConsolidationCriteria(Name, Criteria) VALUES ('SNMPTRAP','OperationStatus;isStateChanged;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings');
REPLACE INTO ConsolidationCriteria(Name, Criteria) VALUES ('SYSLOG','OperationStatus;isStateChanged;Device;MonitorStatus;ipaddress;ErrorType;SubComponent');

# Make sure consolidation can take place by the next insert

UPDATE LogMessageProperty lmp, LogMessage lm set lmp.ValueBoolean=1 WHERE lm.ApplicationTypeID IN ((SELECT ApplicationTypeID FROM ApplicationType Where Name='SNMPTRAP') , (SELECT ApplicationTypeID FROM ApplicationType Where Name='SYSLOG')) AND lmp.LogMessageID=lm.LogMessageID AND lmp.PropertyTypeID=45;

# Missing Monitoring Status for acknowledged messages

REPLACE INTO MonitorStatus (Name, Description) VALUES("ACKNOWLEDGEMENT (CRITICAL)",  "Nagios Acknowledgement");
REPLACE INTO MonitorStatus (Name, Description) VALUES("ACKNOWLEDGEMENT (UNKNOWN)",  "Nagios Acknowledgement");
REPLACE INTO MonitorStatus (Name, Description) VALUES("ACKNOWLEDGEMENT (WARNING)",  "Nagios Acknowledgement");

# New severity 

REPLACE INTO Severity (Name, Description) VALUES("UP", "Severity UP");
REPLACE INTO Severity (Name, Description) VALUES("DOWN", "Severity DOWN");
REPLACE INTO Severity (Name, Description) VALUES("UNREACHABLE", "Severity unreachable");

