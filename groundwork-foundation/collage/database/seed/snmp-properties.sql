-- $Id: snmp-properties.sql 1157 2006-03-23 17:34:03 -0800 (Thu, 23 Mar 2006) rruttimann $
-- Database changes for SNMPTRAP messages
--
-- Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
-- All rights reserved. Use is subject to GroundWork commercial license terms.
--

--Add new ApplicationType for SNMPTRAP

DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP') && EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');
DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG') && EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');

REPLACE INTO ApplicationType (Name, Description, StateTransitionCriteria) VALUES("SNMPTRAP","SNMP Trap application", "Device;Event_OID_numeric");

-- Add the properties specific to SNMPTRAP

REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("ipaddress", "ipdddress of snmp device", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("Event_OID_numeric", "Event_OID_numeric", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("Event_OID_symbolic", "Event_OID_symbolic of snmp device", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("Event_Name", "Event_Name", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("Category", "Category of snmp device", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("Variable_Bindings", "Variable_Bindings", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("UpdatedBy", "UpdatedBy", 1);


-- Assign the SNMP properties to Application Type SNMPTRAP and Entity LOG_MESSAGE

REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ipaddress'), 1); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_OID_numeric'), 2);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_OID_symbolic'), 3); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_Name'), 4);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Category'), 5); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Variable_Bindings'), 6);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 7);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 8);
--Create consolidation criteria

REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('SNMPTRAP', 'OperationStatus;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings');

--Configuration for SNMPTRAP passive check reset from console

INSERT INTO ActionType (Name, ClassName) VALUES( "PASSIVE_CHECK_ACTION", "org.groundwork.foundation.bs.actions.ShellScriptAction");

INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = "PASSIVE_CHECK_ACTION"),"Submit Passive Check","Submit a passive check for this service");

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"), "Script", "/usr/local/groundwork/foundation/scripts/reset_passive_check.sh");


INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "SNMPTRAP"), (SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"));

-- Script for Action parameters
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check") ,"nsca_host","nsca_host");
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"),"user","user");
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"),"comment","comment");
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"),"host","host");
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"),"service","service");
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"),"state","state");

