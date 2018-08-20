-- $Id: snmp-properties.sql 1157 2006-03-23 17:34:03 -0800 (Thu, 23 Mar 2006) rruttimann $
-- Database changes for SNMPTRAP messages
--
-- Copyright 2007 GroundWork Open Source, Inc. (GroundWork)  
-- All rights reserved. Use is subject to GroundWork commercial license terms.
--

--Add new ApplicationType for SNMPTRAP

DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP') AND EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');
DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG') AND EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');

INSERT INTO ApplicationType (Name, DisplayName, Description, StateTransitionCriteria) VALUES ('SNMPTRAP', 'SNMPTRAP', 'SNMP Trap application', 'Device;Event_OID_numeric');

-- Add the properties specific to SNMPTRAP

INSERT INTO PropertyType (Name, Description, isString)  VALUES ('ipaddress', 'ipdddress of snmp device', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('Event_OID_numeric', 'Event_OID_numeric', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('Event_OID_symbolic', 'Event_OID_symbolic of snmp device', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('Event_Name', 'Event_Name', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('Category', 'Category of snmp device', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('Variable_Bindings', 'Variable_Bindings', true);
INSERT INTO PropertyType (Name, Description, isString)  VALUES ('UpdatedBy', 'UpdatedBy', true);

-- Assign the SNMP properties to Application Type SNMPTRAP and Entity LOG_MESSAGE

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ipaddress'), 1); 
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_OID_numeric'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_OID_symbolic'), 3); 
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Event_Name'), 4);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Category'), 5); 
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Variable_Bindings'), 6);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 7);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 8);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SNMPTRAP'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 9);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

--Create consolidation criteria

INSERT INTO ConsolidationCriteria (Name, Criteria)  VALUES ('SNMPTRAP', 'OperationStatus;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings');

--Configuration for SNMPTRAP passive check reset from console

INSERT INTO ActionType (Name, ClassName) VALUES( 'PASSIVE_CHECK_ACTION', 'org.groundwork.foundation.bs.actions.ShellScriptAction');

INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = 'PASSIVE_CHECK_ACTION'),'Submit Passive Check','Submit a passive check for this service');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'), 'Script', '/usr/local/groundwork/foundation/scripts/reset_passive_check.sh');


INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'), (SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'));

-- Script for Action parameters
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check') ,'nsca_host','nsca_host');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'),'user','user');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'),'comment','comment');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'),'host','host');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'),'service','service');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'),'state','state');

