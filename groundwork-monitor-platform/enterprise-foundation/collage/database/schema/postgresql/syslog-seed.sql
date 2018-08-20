-- $Id: $
--
-- Copyright 2007 GroundWork Open Source, Inc. (GroundWork)  
-- All rights reserved. Use is subject to GroundWork commercial license terms.
--

-- Database changes for SYSLOG messages

--Add new ApplicationType for SYSLOG

DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG') AND EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');

INSERT INTO ApplicationType (Name, DisplayName, Description, StateTransitionCriteria) VALUES ('SYSLOG', 'SYSLOG', 'SYSLOG application', 'Device');

-- The system will use the properties already defined for Nagios: SubComponent, ErrorType and SNMPTrap: ipadress

INSERT INTO ApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ipaddress'), 1); 
INSERT INTO ApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'SubComponent'), 2); 
INSERT INTO ApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ErrorType'), 3);
INSERT INTO ApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 4);
INSERT INTO ApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 5);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSLOG'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 6);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

--Create consolidation criteria

INSERT INTO ConsolidationCriteria (Name, Criteria)  VALUES ('SYSLOG', 'OperationStatus;Device;MonitorStatus;ipaddress;ErrorType;SubComponent');

--Configuration for SYSLOG passive check reset from console.Common scripts for snmptrap and syslog are in snmp-properties.sql.

INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'), (SELECT ActionID FROM Action WHERE Name = 'Submit Passive Check'));
