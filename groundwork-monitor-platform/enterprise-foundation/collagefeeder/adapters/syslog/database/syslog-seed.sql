-- $Id: $
--
-- Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
-- All rights reserved. Use is subject to GroundWork commercial license terms.
--
-- Database changes for SYSLOG messages

--Add new ApplicationType for SYSLOG

DELETE FROM ApplicationEntityProperty WHERE ApplicationTypeID = (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG') && EntityTypeID = (SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE');


REPLACE INTO ApplicationType (Name, Description) VALUES("SYSLOG","SYSLOG application");

-- Add the properties specific to SNMPTRAP

REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("ipaddress", "ipdddress of message source", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("SubComponent", "", 1);
REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("ErrorType", "", 1);


-- Use Nagios properties for SubComponent and ErrorType

REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ipaddress'), 1); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'SubComponent'), 1); 
REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ErrorType'), 1);

--Create consolidation criteria

REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('SYSLOG', 'Device;Host;ipaddress;Severity;ErrorType;SubComponent');
