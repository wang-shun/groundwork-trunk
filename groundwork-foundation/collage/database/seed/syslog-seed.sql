-- $Id: $
--
-- Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
-- All rights reserved. Use is subject to GroundWork commercial license terms.
--

-- Database changes for SYSLOG messages

--Add new ApplicationType for SYSLOG

DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG') && EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');

REPLACE INTO ApplicationType (Name, Description, StateTransitionCriteria) VALUES("SYSLOG","SYSLOG application", "Device");

-- The system will use the properties already defined for Nagios: SubComponent, ErrorType and SNMPTrap: ipadress

REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ipaddress'), 1); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'SubComponent'), 2); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ErrorType'), 3);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 4);
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 5);

--Create consolidation criteria

REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('SYSLOG', 'OperationStatus;Device;MonitorStatus;ipaddress;ErrorType;SubComponent');

--Configuration for SYSLOG passive check reset from console.Common scripts for snmptrap and syslog are in snmp-properties.sql.

INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "SYSLOG"), (SELECT ActionID FROM Action WHERE Name = "Submit Passive Check"));