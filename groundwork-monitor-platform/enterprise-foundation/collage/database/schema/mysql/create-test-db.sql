--Collage - The ultimate data integration framework.
--Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com
--
--   This program is free software; you can redistribute it and/or modify
--   it under the terms of version 2 of the GNU General Public License 
--   as published by the Free Software Foundation.
--
--   This program is distributed in the hope that it will be useful,
--   but WITHOUT ANY WARRANTY; without even the implied warranty of
--   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--   GNU General Public License for more details.
--
--   You should have received a copy of the GNU General Public License
--   along with this program; if not, write to the Free Software
--   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
--$Id: create-test-db.sql 6397 2007-04-02 21:27:40Z glee $

#drop database
DROP DATABASE IF EXISTS GWCollageDB;
create DATABASE GWCollageDB;
use GWCollageDB;

-- Create tables

-- V E R S I O N  I N F O
-- -----------------------------------------------------------------------
-- SchemaInfo
-- -----------------------------------------------------------------------

CREATE TABLE SchemaInfo 
(
	Name VARCHAR (254),
	VALUE VARCHAR(254)
) TYPE = InnoDB;


-- M E T A D A T A    T A B L E S

-- -----------------------------------------------------------------------
-- ApplicationType
--
-- Stores an enumeration of the types of systems/applications that can be
-- monitored through the system (NAGIOS,SYSLOG,JMX,...)
-- -----------------------------------------------------------------------
CREATE TABLE ApplicationType 
(
	ApplicationTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128),
	Description VARCHAR (254),

	PRIMARY KEY(ApplicationTypeID),
	UNIQUE(Name)
) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- EntityType
--
-- Stores an enumeration of the types of entities that exist in the system
-- (Host,ServiceStatus,etc...); 
-- this enumeration is used to specify sets of PropertyTypes below that extend
-- the entities (in effect 'soft-coding' what would be columns in the entity
-- tables)
-- -----------------------------------------------------------------------
CREATE TABLE EntityType 
(
	EntityTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128),
	Description VARCHAR (254),

	PRIMARY KEY(EntityTypeID),
	UNIQUE(Name)
) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- PropertyType
--
-- Stores definitions for 'soft-coded' properties of an Entity, namely the name
-- of the property and its type
-- -----------------------------------------------------------------------
CREATE TABLE PropertyType
(
	PropertyTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128),
	Description VARCHAR (254),
	isDate    BOOLEAN DEFAULT 0,
	isBoolean BOOLEAN DEFAULT 0,
	isString  BOOLEAN DEFAULT 0,
	isInteger BOOLEAN DEFAULT 0,
	isLong    BOOLEAN DEFAULT 0,
	isDouble  BOOLEAN DEFAULT 0,
	isVisible BOOLEAN DEFAULT 1,

	PRIMARY KEY(PropertyTypeID),
	UNIQUE(Name)
) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- ApplicationEntityProperty
--
-- Ternary association between ApplicationType, EntityType, and PropertyType:
-- stores the PropertyTypes (data to be monitored) that have been defined for an 
-- EntityType (Host, ServiceStatus,....) in the context of an ApplicationType
-- (NAGIOS, SYSLOG, JMX...)
-- -----------------------------------------------------------------------
CREATE TABLE ApplicationEntityProperty
(
	ApplicationTypeID INTEGER NOT NULL,
	EntityTypeID INTEGER NOT NULL,
	PropertyTypeID INTEGER NOT NULL,
	SortOrder INTEGER NOT NULL DEFAULT 999,

	PRIMARY KEY(ApplicationTypeID, EntityTypeID, PropertyTypeID),

	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID),
	FOREIGN KEY (EntityTypeID) REFERENCES EntityType(EntityTypeID),
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)

) TYPE = InnoDB;


-- P H Y S I C A L  L A Y O U T  O F  N E T W O R K 

-- -------------------------------------------------------------------------------
-- Device -- can be server, router, switch, ...
-- The Identification defines the IP or MAC or other identification
-- -------------------------------------------------------------------------------

CREATE TABLE Device 
(
	DeviceID INTEGER NOT NULL AUTO_INCREMENT,
	DisplayName VARCHAR (254),
	Identification VARCHAR (128),
	DESCRIPTION VARCHAR(254),
	
	PRIMARY KEY(DeviceID),
	UNIQUE(Identification)
) TYPE = InnoDB;

-- -------------------------------------------------------------------------------
-- Parent Child ( Network topology)  information
--
-- DeviceChild relationship
-- -------------------------------------------------------------------------------

CREATE TABLE DeviceParent 
(
	DeviceID INTEGER NOT NULL,
	ParentID INTEGER NOT NULL,
	
	PRIMARY KEY(DeviceID,ParentID),
	
    FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID)
        ON DELETE CASCADE,

    FOREIGN KEY (ParentID) REFERENCES Device (DeviceID)
        ON DELETE CASCADE
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- MonitorServer
-- -----------------------------------------------------------------------

CREATE TABLE MonitorServer 
(
	MonitorServerID INTEGER NOT NULL AUTO_INCREMENT,
	MonitorServerName	VARCHAR(254) NOT NULL,
	IP VARCHAR (128) NOT NULL,
	Description VARCHAR (254),
	
	PRIMARY KEY(MonitorServerID)

) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- MonitorList
-- -----------------------------------------------------------------------

CREATE TABLE MonitorList 
(
	MonitorServerID INTEGER NOT NULL,
	DeviceID INTEGER NOT NULL,
	
    PRIMARY KEY(MonitorServerID,DeviceID),
    
    FOREIGN KEY (MonitorServerID) REFERENCES MonitorServer(MonitorServerID)
        ON DELETE CASCADE ,
    
    FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID)
        ON DELETE CASCADE
) TYPE = InnoDB;

-- L O G  I N F O R M A T I O N   C O N S O L E  V I E W
-- -----------------------------------------------------------------------
-- OperationStatus
-- -----------------------------------------------------------------------

CREATE TABLE OperationStatus 
(
	OperationStatusID INTEGER NOT NULL  AUTO_INCREMENT,
	Name VARCHAR(128) NOT NULL,
	Description VARCHAR(254),
    PRIMARY KEY(OperationStatusID),
    UNIQUE(Name)
) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- Priority
-- -----------------------------------------------------------------------

CREATE TABLE Priority 
(
	PriorityID INTEGER NOT NULL  AUTO_INCREMENT,
	Name VARCHAR(128) NOT NULL,
	Description VARCHAR(254),
    PRIMARY KEY(PriorityID),
    UNIQUE(Name)

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- Component
-- -----------------------------------------------------------------------

CREATE TABLE Component 
(
	ComponentID INTEGER NOT NULL  AUTO_INCREMENT,
	Name VARCHAR(128) NOT NULL,
	Description VARCHAR(254),
    PRIMARY KEY(ComponentID),
    UNIQUE(Name)

) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- Severity
-- -----------------------------------------------------------------------

CREATE TABLE Severity 
(
	SeverityID INTEGER NOT NULL  AUTO_INCREMENT, 
	Name VARCHAR(128) NOT NULL,
	Description VARCHAR(254),
    PRIMARY KEY(SeverityID),
    UNIQUE(Name)

) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- TypeRule
-- -----------------------------------------------------------------------

CREATE TABLE TypeRule 
(
	TypeRuleID INTEGER NOT NULL  AUTO_INCREMENT, 
	Name VARCHAR(128) NOT NULL,
	Description VARCHAR(254),
    PRIMARY KEY(TypeRuleID),
    UNIQUE(Name)
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- LogMessage has dependencies into the Monitor tables. For this
-- reason the table is defined within the Monitor tables
-- -----------------------------------------------------------------------


-- A V A I L A B I L I T Y  T A B L E S  (S T A T E / M O N I T O R)

-- -----------------------------------------------------------------------
-- Host
-- -----------------------------------------------------------------------
CREATE TABLE Host 
(
	HostID INTEGER NOT NULL AUTO_INCREMENT,
	DeviceID INTEGER NOT NULL,
	HostName VARCHAR (254),
	Description VARCHAR (254),
	ApplicationTypeID INTEGER default NULL,

	PRIMARY KEY(HostID),
	UNIQUE(HostName),
	UNIQUE(HostID),
	
	FOREIGN KEY (DeviceID) REFERENCES Device(DeviceID)
        ON DELETE CASCADE,
    FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID)
        ON DELETE CASCADE 
 
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostGroup
-- -----------------------------------------------------------------------
CREATE TABLE HostGroup 
(
	HostGroupID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254),
	Description VARCHAR (254),
	ApplicationTypeID INTEGER default NULL,

	PRIMARY KEY(HostGroupID),
	UNIQUE(Name),
	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID)
        ON DELETE CASCADE 
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostGroupCollection
-- -----------------------------------------------------------------------

CREATE TABLE HostGroupCollection 
(
	HostID INTEGER NOT NULL,
	HostGroupID INTEGER NOT NULL,
	
    PRIMARY KEY(HostID,HostGroupID),
    FOREIGN KEY (HostID) REFERENCES Host(HostID)
        ON DELETE CASCADE ,
    FOREIGN KEY (HostGroupID) REFERENCES HostGroup (HostGroupID)
        ON DELETE CASCADE
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- StateType
-- -----------------------------------------------------------------------
CREATE TABLE StateType 
(
	StateTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254),
	Description VARCHAR (254),

	PRIMARY KEY(StateTypeID),
	UNIQUE(Name)
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- CheckType
-- -----------------------------------------------------------------------
CREATE TABLE CheckType 
(
	CheckTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254),
	Description VARCHAR (254),

	PRIMARY KEY(CheckTypeID),
	UNIQUE(Name)
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- MonitorStatus
-- -----------------------------------------------------------------------
CREATE TABLE MonitorStatus 
(
	MonitorStatusID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254),
	Description VARCHAR (254),

	PRIMARY KEY(MonitorStatusID),
	UNIQUE(Name)
) TYPE = InnoDB;



-- -----------------------------------------------------------------------
-- HostStatus
-- HostStatus has a one-to-one dependent relationship to Host; hence, we 
-- use the HostID as the primary key of HostStatusID, and the two can be
-- used interchangeably throughout the application
-- -----------------------------------------------------------------------

CREATE TABLE HostStatus 
(
	HostStatusID INTEGER NOT NULL,
	-- HostStatusID INTEGER NOT NULL AUTO_INCREMENT,
	-- HostID INTEGER NOT NULL, 
	ApplicationTypeID INTEGER NOT NULL,
	MonitorStatusID INTEGER NOT NULL, 
	LastCheckTime DATETIME,
	CheckTypeID INTEGER DEFAULT NULL,
	-- LastStateChange DATETIME,
	-- isAcknowledged BOOLEAN DEFAULT 0,
	-- TimeUp INTEGER DEFAULT 0,
	-- TimeDown INTEGER DEFAULT 0,
	-- TimeUnreachable INTEGER DEFAULT 0,	
	-- LastNotificationTime DATETIME DEFAULT 0,
	-- isNotificationsEnabled BOOLEAN DEFAULT 0,
	-- isEventHandlersEnabled BOOLEAN DEFAULT 0,
	-- isChecksEnabled BOOLEAN DEFAULT 0,
	-- isFlapDetectionsEnabled BOOLEAN DEFAULT 0,
	-- isHostFlappingEnabled BOOLEAN DEFAULT 0,
	-- isFailurePredictionEnabled BOOLEAN DEFAULT 0,
	-- isProcessPerformanceData BOOLEAN DEFAULT 0,
	-- PercentStateChange FLOAT DEFAULT 0.0,
	-- CurrentNotificationNumber SMALLINT DEFAULT 0, 
	-- ScheduledDowntimeDepth SMALLINT DEFAULT 0,
	-- LastPluginOutput TEXT,
	-- 30DayMovingAvg FLOAT DEFAULT 0.0,

	PRIMARY KEY(HostStatusID),
	FOREIGN KEY (HostStatusID) REFERENCES Host(HostID)
		ON DELETE CASCADE,
	FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus(MonitorStatusID)
		ON DELETE CASCADE,
	FOREIGN KEY (CheckTypeID) REFERENCES CheckType(CheckTypeID)
		ON DELETE CASCADE
            
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostStatusProperty: Holds the values of the PropertyTypes defined for 
-- the HostStatus entity in the context of an ApplicationType
-- Note that the CreatedOn field is necessary to keep hibernate from thinking
-- that the mapped object (PropertyValue) is null when all the other fields are
-- null - do not remove this field
-- -----------------------------------------------------------------------

CREATE TABLE HostStatusProperty
(
	HostStatusID   INTEGER NOT NULL,
	PropertyTypeID INTEGER NOT NULL,
	ValueString    TEXT,
	ValueDate      DATETIME,
	ValueBoolean   BOOLEAN,
	ValueInteger   INTEGER,
	ValueLong      BIGINT,
	ValueDouble    DOUBLE,
	LastEditedOn   TIMESTAMP,
	CreatedOn      TIMESTAMP NOT NULL,

	PRIMARY KEY(HostStatusID, PropertyTypeID),

	FOREIGN KEY (HostStatusID) REFERENCES HostStatus(HostStatusID)
		ON DELETE CASCADE,
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)
		ON DELETE CASCADE

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- ServiceStatus
-- -----------------------------------------------------------------------

CREATE TABLE ServiceStatus 
(
	ServiceStatusID INTEGER NOT NULL AUTO_INCREMENT,
	ApplicationTypeID INTEGER NOT NULL,
	ServiceDescription VARCHAR(254),
	HostID INTEGER NOT NULL, 
	MonitorStatusID INTEGER NOT NULL,
	LastCheckTime DATETIME DEFAULT NULL,
	NextCheckTime DATETIME DEFAULT NULL,
	LastStateChange DATETIME DEFAULT NULL,
	LastHardStateID INTEGER NOT NULL,
	StateTypeID INTEGER NOT NULL,
	CheckTypeID INTEGER NOT NULL,
	MetricType VARCHAR(254) DEFAULT NULL,
	Domain VARCHAR(254) DEFAULT NULL,
	-- RetryNumber SMALLINT DEFAULT 1,
	-- TimeOK INTEGER DEFAULT 0,
	-- TimeUnknown INTEGER DEFAULT 0,
	-- TimeWarning INTEGER DEFAULT 0,
	-- TimeCritical INTEGER DEFAULT 0,
	-- LastNotificationTime DATETIME NOT NULL,
	-- Latency INTEGER DEFAULT 1,
	-- ExecutionTime INTEGER DEFAULT 0,
	-- isChecksEnabled BOOLEAN DEFAULT 0,
	-- isAcceptPassiveChecks BOOLEAN DEFAULT 0,
	-- isEventHandlersEnabled BOOLEAN DEFAULT 0,
	-- isProblemAcknowledged BOOLEAN DEFAULT 0,
	-- isNotificationsEnabled BOOLEAN DEFAULT 0,
	-- isFlapDetectionsEnabled BOOLEAN DEFAULT 0,
	-- isServiceFlapping BOOLEAN DEFAULT 0,
	-- isFailurePredictionEnabled BOOLEAN DEFAULT 0,
	-- isProcessPerformanceData BOOLEAN DEFAULT 0,
	-- isObsessOverService BOOLEAN DEFAULT 0,
	-- PercentStateChange FLOAT DEFAULT 0.0,
	-- CurrentNotificationNumber SMALLINT DEFAULT 0, 
	-- ScheduledDowntimeDepth SMALLINT DEFAULT 0,
	-- LastPluginOutput TEXT,
	-- 30DayMovingAvg FLOAT DEFAULT 0.0,
	
	PRIMARY KEY(ServiceStatusID),
	UNIQUE(HostID, ServiceDescription),

	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID),
	FOREIGN KEY (HostID) REFERENCES Host(HostID)
		ON DELETE CASCADE ,  
	FOREIGN KEY (StateTypeID) REFERENCES StateType(StateTypeID)
		ON DELETE CASCADE ,
	FOREIGN KEY (CheckTypeID) REFERENCES CheckType(CheckTypeID)
		ON DELETE CASCADE ,
	FOREIGN KEY (LastHardStateID) REFERENCES MonitorStatus(MonitorStatusID)
		ON DELETE CASCADE,
	FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus(MonitorStatusID)
		ON DELETE CASCADE
) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- ServiceStatusProperty: Holds the values of the PropertyTypes defined for 
-- the ServiceStatus entity in the context of an ApplicationType
-- Note that the CreatedOn field is necessary to keep hibernate from thinking
-- that the mapped object (PropertyValue) is null when all the other fields are
-- null - do not remove this field
-- -----------------------------------------------------------------------

CREATE TABLE ServiceStatusProperty
(
	ServiceStatusID INTEGER NOT NULL,
	PropertyTypeID	INTEGER NOT NULL,
	ValueString     TEXT,
	ValueDate       DATETIME,
	ValueBoolean    BOOLEAN,
	ValueInteger    INTEGER,
	ValueLong       BIGINT,
	ValueDouble     DOUBLE,
	LastEditedOn    TIMESTAMP,
	CreatedOn       TIMESTAMP NOT NULL,

	PRIMARY KEY(ServiceStatusID, PropertyTypeID),

	FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus(ServiceStatusID)
		ON DELETE CASCADE,
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)
		ON DELETE CASCADE

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- LogMessage has dependencies into the Monitor tables
-- -----------------------------------------------------------------------

CREATE TABLE LogMessage 
(
	LogMessageID INTEGER NOT NULL  AUTO_INCREMENT,
	ApplicationTypeID INTEGER NOT NULL,
	DeviceID INTEGER NOT NULL,
	HostStatusID INTEGER,
	ServiceStatusID INTEGER,
	TextMessage TEXT  NOT NULL,
	MsgCount INTEGER NOT NULL DEFAULT 1,
	FirstInsertDate DATETIME NOT NULL,
	LastInsertDate DATETIME NOT NULL,
	ReportDate DATETIME NOT NULL,
	MonitorStatusID INTEGER,
	SeverityID INTEGER NOT NULL,
	ApplicationSeverityID INTEGER NOT NULL,
	PriorityID INTEGER NOT NULL,
	TypeRuleID INTEGER NOT NULL,
	ComponentID INTEGER NOT NULL,
	OperationStatusID INTEGER NOT NULL,
	isStateChanged    BOOLEAN NOT NULL DEFAULT FALSE,
	ConsolidationHash    INTEGER NOT NULL DEFAULT 0,
	StatelessHash    INTEGER NOT NULL DEFAULT 0,

	PRIMARY KEY(LogMessageID),

	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID),
	FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID)
		ON DELETE CASCADE,
	FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus (ServiceStatusID),
	FOREIGN KEY (HostStatusID) REFERENCES HostStatus (HostStatusID),

	FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus (MonitorStatusID)
		ON DELETE CASCADE,
	FOREIGN KEY (SeverityID) REFERENCES Severity (SeverityID)
		ON DELETE CASCADE,
	FOREIGN KEY (ApplicationSeverityID) REFERENCES Severity (SeverityID)
		ON DELETE CASCADE,
	FOREIGN KEY (PriorityID) REFERENCES Priority (PriorityID)
		ON DELETE CASCADE,
	FOREIGN KEY (TypeRuleID) REFERENCES TypeRule (TypeRuleID)
		ON DELETE CASCADE,
	FOREIGN KEY (ComponentID) REFERENCES Component (ComponentID)
		ON DELETE CASCADE,
	FOREIGN KEY (OperationStatusID) REFERENCES OperationStatus (OperationStatusID)
		ON DELETE CASCADE

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- LogMessageProperty: Holds the values of the PropertyTypes defined for 
-- the LogMessage entity in the context of an ApplicationType
-- Note that the CreatedOn field is necessary to keep hibernate from thinking
-- that the mapped object (PropertyValue) is null when all the other fields are
-- null - do not remove this field
-- -----------------------------------------------------------------------

CREATE TABLE LogMessageProperty
(
	LogMessageID    INTEGER NOT NULL,
	PropertyTypeID	INTEGER NOT NULL,
	ValueString     TEXT,
	ValueDate       DATETIME,
	ValueBoolean    BOOLEAN,
	ValueInteger    INTEGER,
	ValueLong       BIGINT,
	ValueDouble     DOUBLE,
	LastEditedOn    TIMESTAMP,
	CreatedOn       TIMESTAMP NOT NULL,

	PRIMARY KEY(LogMessageID, PropertyTypeID),

	FOREIGN KEY (LogMessageID) REFERENCES LogMessage(LogMessageID)
		ON DELETE CASCADE,
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)
		ON DELETE CASCADE

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- EntityProperty: Holds the values of the PropertyTypes defined for 
-- an arbitrary PropertyExtensible entity in the context of an ApplicationType
-- Note that the CreatedOn field is necessary to keep hibernate from thinking
-- that the mapped object (PropertyValue) is null when all the other fields are
-- null - do not remove this field
-- -----------------------------------------------------------------------

CREATE TABLE EntityProperty
(
	EntityTypeID    INTEGER NOT NULL,
	ObjectID        INTEGER NOT NULL,
	PropertyTypeID	INTEGER NOT NULL,
	ValueString     TEXT,
	ValueDate       DATETIME,
	ValueBoolean    BOOLEAN,
	ValueInteger    INTEGER,
	ValueLong       BIGINT,
	ValueDouble     DOUBLE,
	LastEditedOn    TIMESTAMP,
	CreatedOn       TIMESTAMP NOT NULL,

	PRIMARY KEY(EntityTypeID, ObjectID, PropertyTypeID),

	FOREIGN KEY (EntityTypeID) REFERENCES EntityType(EntityTypeID)
		ON DELETE CASCADE,
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)
		ON DELETE CASCADE

) TYPE = InnoDB;


-- -----------------------------------------------------------------------
-- LogPerformanceData
-- Store performance data summary such as a minimum, maximum and average for
-- a day. The performance check is asoccociated with a ServiceStatus
-- The new Average is calculated for the same day as: 
-- ((Average * MeasurementPoints) + new Value)/(MeasurementPoints+1)
-- 
-- -----------------------------------------------------------------------

CREATE TABLE LogPerformanceData 
(
	LogPerformanceDataID INTEGER NOT NULL AUTO_INCREMENT ,
	ServiceStatusID INTEGER NOT NULL,
	LastCheckTime 	DATETIME NOT NULL,
	Maximum DOUBLE DEFAULT 0,
	Minimum DOUBLE DEFAULT 0,
	Average DOUBLE DEFAULT 0,
	MeasurementPoints INTEGER DEFAULT 0,
	PerformanceName VARCHAR(254) DEFAULT "",
	
    PRIMARY KEY(LogPerformanceDataID),
    FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus(ServiceStatusID)
        ON DELETE CASCADE
) TYPE = InnoDB;

------------------------------------------------------------------------------------------------------------------------------
-- Filter & consolidation used for inserting messages into
-- the LogMessage table

-- ConsolidationCriteria	
-- If a message to be inserted is equal to all the fields defined in the criteria
-- the msg count and the last insert date of an existing message in the 
-- LogMessagewould be changed.
-- FORMAT of criteria: table.field;table.field example 'LogMessage.SeverityID;LogMessage.ServerID'
--------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE ConsolidationCriteria
(
	ConsolidationCriteriaID INTEGER NOT NULL AUTO_INCREMENT ,
	Name VARCHAR (254) NOT NULL,
	Criteria TEXT NOT NULL,
	
    PRIMARY KEY(ConsolidationCriteriaID),
    UNIQUE(Name)
) TYPE = InnoDB;

-------------------------------------------------------------------------------------------------------
-- Message filter.
-- If a regular expression matches the LogMessage the message
-- will not be inserted into the database unless the flag isChangeSeverityToStatistic
-- is set. In this case the message would be inserted but the severity would be
-- changed to STATISTIC
-------------------------------------------------------------------------------------------------------

CREATE TABLE MessageFilter
(
	MessageFilterID INTEGER NOT NULL AUTO_INCREMENT ,
	Name VARCHAR (254) NOT NULL,
	RegExpresion TEXT NOT NULL,
	isChangeSeverityToStatistic BOOLEAN DEFAULT 0,
	
    PRIMARY KEY(MessageFilterID),
    UNIQUE(Name)
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- ServiceAvailability
-- -----------------------------------------------------------------------

CREATE TABLE ServiceAvailability 
(
	ServiceAvailabilityID INTEGER NOT NULL AUTO_INCREMENT ,
	ServiceStatusID INTEGER NOT NULL,
	TimeStart DATETIME NOT NULL,
	TimeEnd DATETIME NOT NULL,
	PERCENT_KNOWN_TIME_CRITICAL REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_OK_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_OK_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
  PERCENT_TIME_UNKNOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNKNOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_WARNING_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_WARNING_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_CRITICAL REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_OK REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNKNOWN REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_WARNING REAL NOT NULL default 0,
  TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  TIME_OK_SCHEDULED INTEGER NOT NULL default 0,
  TIME_OK_UNSCHEDULED INTEGER NOT NULL default 0,
  TIME_UNDETERMINED_NOT_RUNNING INTEGER NOT NULL default 0,
  TIME_UNDETERMINED_NO_DATA INTEGER NOT NULL default 0,
  TIME_UNKNOWN_SCHEDULED INTEGER NOT NULL default 0,
  TIME_UNKNOWN_UNSCHEDULED INTEGER NOT NULL default 0,
  TIME_WARNING_SCHEDULED INTEGER NOT NULL default 0,
  TIME_WARNING_UNSCHEDULED INTEGER NOT NULL default 0,
  TOTAL_TIME_CRITICAL INTEGER NOT NULL default 0,
  TOTAL_TIME_OK INTEGER NOT NULL default 0,
  TOTAL_TIME_UNDETERMINED INTEGER NOT NULL default 0,
  TOTAL_TIME_UNKNOWN INTEGER NOT NULL default 0,
  TOTAL_TIME_WARNING INTEGER NOT NULL default 0,

	
    PRIMARY KEY(ServiceAvailabilityID),
    FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus(ServiceStatusID)
        ON DELETE CASCADE
   
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostAvailability
-- -----------------------------------------------------------------------

CREATE TABLE HostAvailability 
(
	HostAvailabilityID INTEGER NOT NULL AUTO_INCREMENT ,
	HostStatusID INTEGER NOT NULL,
	TimeStart DATETIME NOT NULL,
	TimeEnd DATETIME NOT NULL,
	PERCENT_KNOWN_TIME_DOWN REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_DOWN_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UNREACHABLE REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UP REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UP_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_KNOWN_TIME_UP_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_DOWN_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_DOWN_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
	  PERCENT_TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
	  PERCENT_TIME_UNREACHABLE_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_UNREACHABLE_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_UP_SCHEDULED REAL NOT NULL default 0,
	  PERCENT_TIME_UP_UNSCHEDULED REAL NOT NULL default 0,
	  PERCENT_TOTAL_TIME_DOWN REAL NOT NULL default 0,
	  PERCENT_TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
	  PERCENT_TOTAL_TIME_UNREACHABLE REAL NOT NULL default 0,
	  PERCENT_TOTAL_TIME_UP REAL NOT NULL default 0,
	  TIME_DOWN_SCHEDULED INTEGER NOT NULL default 0,
	  TIME_DOWN_UNSCHEDULED INTEGER NOT NULL default 0,
	  TIME_UNDETERMINED_NOT_RUNNING INTEGER NOT NULL default 0,
	  TIME_UNDETERMINED_NO_DATA INTEGER NOT NULL default 0,
	  TIME_UNREACHABLE_SCHEDULED INTEGER NOT NULL default 0,
	  TIME_UNREACHABLE_UNSCHEDULED INTEGER NOT NULL default 0,
	  TIME_UP_SCHEDULED INTEGER NOT NULL default 0,
	  TIME_UP_UNSCHEDULED INTEGER NOT NULL default 0,
	  TOTAL_TIME_DOWN INTEGER NOT NULL default 0,
	  TOTAL_TIME_UNDETERMINED INTEGER NOT NULL default 0,
	  TOTAL_TIME_UNREACHABLE INTEGER NOT NULL default 0,
	  TOTAL_TIME_UP INTEGER NOT NULL default 0,
	
    PRIMARY KEY(HostAvailabilityID),
    FOREIGN KEY (HostStatusID) REFERENCES HostStatus(HostStatusID)
        ON DELETE CASCADE
   
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostGroupHostAvailability
-- -----------------------------------------------------------------------

CREATE TABLE HostGroupHostAvailability 
(
	HostGroupHostAvailabilityID INTEGER NOT NULL AUTO_INCREMENT ,
	HostGroupID INTEGER NOT NULL,
	TimeStart DATETIME NOT NULL,
	TimeEnd DATETIME NOT NULL,
	
	  PERCENT_KNOWN_TIME_DOWN REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_DOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNREACHABLE REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UP REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UP_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UP_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_DOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_DOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
  PERCENT_TIME_UNREACHABLE_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNREACHABLE_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UP_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UP_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_DOWN REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNREACHABLE REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UP REAL NOT NULL default 0,
  TIME_DOWN_SCHEDULED REAL NOT NULL default 0,
  TIME_DOWN_UNSCHEDULED REAL NOT NULL default 0,
  TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
  TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
  TIME_UNREACHABLE_SCHEDULED REAL NOT NULL default 0,
  TIME_UNREACHABLE_UNSCHEDULED REAL NOT NULL default 0,
  TIME_UP_SCHEDULED REAL NOT NULL default 0,
  TIME_UP_UNSCHEDULED REAL NOT NULL default 0,
  TOTAL_TIME_DOWN REAL NOT NULL default 0,
  TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
  TOTAL_TIME_UNREACHABLE REAL NOT NULL default 0,
  TOTAL_TIME_UP REAL NOT NULL default 0,
	
    PRIMARY KEY(HostGroupHostAvailabilityID),
    FOREIGN KEY (HostGroupID) REFERENCES HostGroup(HostGroupID)
        ON DELETE CASCADE
   
) TYPE = InnoDB;

-- -----------------------------------------------------------------------
-- HostGroupServiceAvailability
-- -----------------------------------------------------------------------

CREATE TABLE HostGroupServiceAvailability 
(
	HostGroupServiceAvailabilityID INTEGER NOT NULL AUTO_INCREMENT ,
	HostGroupID INTEGER NOT NULL,
	ServiceDescription VARCHAR (254) NOT NULL,
	TimeStart DATETIME NOT NULL,
	TimeEnd DATETIME NOT NULL,
	
	PERCENT_KNOWN_TIME_CRITICAL REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_OK_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING_SCHEDULED REAL NOT NULL default 0,
  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_OK_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_OK_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
  PERCENT_TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
  PERCENT_TIME_UNKNOWN_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_UNKNOWN_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_WARNING_SCHEDULED REAL NOT NULL default 0,
  PERCENT_TIME_WARNING_UNSCHEDULED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_CRITICAL REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_OK REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_UNKNOWN REAL NOT NULL default 0,
  PERCENT_TOTAL_TIME_WARNING REAL NOT NULL default 0,
  TIME_CRITICAL_SCHEDULED REAL NOT NULL default 0,
  TIME_CRITICAL_UNSCHEDULED REAL NOT NULL default 0,
  TIME_OK_SCHEDULED REAL NOT NULL default 0,
  TIME_OK_UNSCHEDULED REAL NOT NULL default 0,
  TIME_UNDETERMINED_NOT_RUNNING REAL NOT NULL default 0,
  TIME_UNDETERMINED_NO_DATA REAL NOT NULL default 0,
  TIME_UNKNOWN_SCHEDULED REAL NOT NULL default 0,
  TIME_UNKNOWN_UNSCHEDULED REAL NOT NULL default 0,
  TIME_WARNING_SCHEDULED REAL NOT NULL default 0,
  TIME_WARNING_UNSCHEDULED REAL NOT NULL default 0,
  TOTAL_TIME_CRITICAL REAL NOT NULL default 0,
  TOTAL_TIME_OK REAL NOT NULL default 0,
  TOTAL_TIME_UNDETERMINED REAL NOT NULL default 0,
  TOTAL_TIME_UNKNOWN REAL NOT NULL default 0,
  TOTAL_TIME_WARNING REAL NOT NULL default 0,
  
  PRIMARY KEY(HostGroupServiceAvailabilityID),
  FOREIGN KEY (HostGroupID) REFERENCES HostGroup(HostGroupID)
        ON DELETE CASCADE
   
) TYPE = InnoDB;

--
-- Table structure for table `EntityProperty`
--
--  CREATE TABLE EntityProperty (
--    PropertyTypeID INTEGER NOT NULL default '0',
--    EntityTypeID INTEGER NOT NULL default '0',
--    ObjectID INTEGER NOT NULL default '0',
--    ValueString varchar(255) default NULL,
--    ValueDate datetime default NULL,
--    ValueBoolean tinyint(1) default NULL,
--    ValueInteger INTEGER default NULL,
--    ValueLong bigint(20) default NULL,
--    ValueDouble double default NULL,
--    LastEditedOn timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
--    CreatedOn timestamp NOT NULL default '0000-00-00 00:00:00',
--    PRIMARY KEY  (ObjectID,PropertyTypeID),
--    
--    FOREIGN KEY (EntityTypeID) REFERENCES EntityType (EntityTypeID)
--          ON DELETE CASCADE,
--    FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType (PropertyTypeID)
--          ON DELETE CASCADE
--    
--    ) ENGINE=InnoDB;

--
-- Table structure for table `Category`
--

CREATE TABLE Category (
  CategoryID INTEGER NOT NULL auto_increment,
  Name varchar(254) default NULL,
  Description varchar(254) default NULL,
  PRIMARY KEY  (CategoryID),
  UNIQUE KEY Name (Name)
) ENGINE=InnoDB;


CREATE TABLE CategoryEntity (
	CategoryEntityID INTEGER NOT NULL auto_increment,
  ObjectID INTEGER NOT NULL default '0',
  CategoryID INTEGER NOT NULL default '0',
  EntityTypeID INTEGER NOT NULL default '0',
  
  PRIMARY KEY  (CategoryEntityID),  
  FOREIGN KEY (CategoryID) REFERENCES Category (CategoryID)
        ON DELETE CASCADE,
   FOREIGN KEY (EntityTypeID) REFERENCES EntityType (EntityTypeID)
        ON DELETE CASCADE
) ENGINE=InnoDB;

--
-- Table structure for table `CategoryHierarchy`
--

CREATE TABLE CategoryHierarchy (
  CategoryID INTEGER NOT NULL default '0',
  ParentID INTEGER NOT NULL default '0',
  PRIMARY KEY  (CategoryID,ParentID),
  
  FOREIGN KEY (ParentID) REFERENCES Category (CategoryID)
        ON DELETE CASCADE,
   FOREIGN KEY (CategoryID) REFERENCES Category(CategoryID)
        ON DELETE CASCADE
        
  ) ENGINE=InnoDB;
  
-- Add static (base case) data

-- populate State tables

INSERT INTO MonitorStatus (Name, Description) VALUES("OK",  "Status OK");
INSERT INTO MonitorStatus (Name, Description) VALUES("DOWN",  "Status DOWN");
INSERT INTO MonitorStatus (Name, Description) VALUES("UNREACHABLE",  "Status UNREACHABLE");
INSERT INTO MonitorStatus (Name, Description) VALUES("WARNING",  "Status WARNING");
INSERT INTO MonitorStatus (Name, Description) VALUES("CRITICAL",  "Status CRITICAL");
INSERT INTO MonitorStatus (Name, Description) VALUES("UNKNOWN",  "Status UNKNOWN");
INSERT INTO MonitorStatus (Name, Description) VALUES("UP",  "Status UP");
INSERT INTO MonitorStatus (Name, Description) VALUES("PENDING",  "Status PENDING");


INSERT INTO StateType (Name, Description) VALUES("SOFT",  "State Soft");
INSERT INTO StateType (Name, Description) VALUES("HARD",  "State Hard");

INSERT INTO CheckType (Name, Description) VALUES("ACTIVE",  "Active Check");
INSERT INTO CheckType (Name, Description) VALUES("PASSIVE",  "Passive Check");

-- populate LogEvent Console tables

INSERT INTO Priority (Name, Description) VALUES("1",  "Lowest Priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("2",  "Low priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("3",  "Low priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("4",  "Low priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("5",  "Medium priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("6",  "Medium priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("7",  "Medium-High priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("8",  "Medium-High priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("9",  "High priority in a scale from 1 -10");
INSERT INTO Priority (Name, Description) VALUES("10",  "Highest priority in a scale from 1 -10");

INSERT INTO Component (Name, Description) VALUES( "SNMP", "SNMP Component");
INSERT INTO Component (Name, Description) VALUES( "MQ", "MessageQueue component");
INSERT INTO Component (Name, Description) VALUES( "JMSLISTENER", "JMSListener component");
INSERT INTO Component (Name, Description) VALUES( "UNDEFINED", "Undefined component");

INSERT INTO Severity (Name, Description) VALUES( "FATAL",  "Severity FATAL");
INSERT INTO Severity (Name, Description) VALUES( "HIGH", "Severity HIGH");
INSERT INTO Severity (Name, Description) VALUES( "LOW", "Severity LOW");
INSERT INTO Severity (Name, Description) VALUES( "WARNING", "Severity WARNING");
INSERT INTO Severity (Name, Description) VALUES( "PERFORMANCE",  "Severity PERFORMANCE");
INSERT INTO Severity (Name, Description) VALUES( "STATISTIC",  "Severity STATISTIC");
INSERT INTO Severity (Name, Description) VALUES("SERIOUS", "Severity SERIOUS");
INSERT INTO Severity (Name, Description) VALUES( "CRITICAL",  "GroundWork Severity CRITICAL. Also MIB standard");
INSERT INTO Severity (Name, Description) VALUES( "OK",  "GroundWork Severity OK");
INSERT INTO Severity (Name, Description) VALUES("UNKNOWN", "GroundWork Severity UNKNOWN");
INSERT INTO Severity (Name, Description) VALUES("NORMAL", "Standard MIB type for Severity");
INSERT INTO Severity (Name, Description) VALUES("MAJOR", "Standard MIB type for MonitorStatus");
INSERT INTO Severity (Name, Description) VALUES("MINOR", "Standard MIB type for MonitorStatus");
INSERT INTO Severity (Name, Description) VALUES("INFORMATIONAL", "Standard MIB type");
INSERT INTO Severity (Name, Description) VALUES("UP", "Severity UP");
INSERT INTO Severity (Name, Description) VALUES("DOWN", "Severity DOWN");
INSERT INTO Severity (Name, Description) VALUES("UNREACHABLE", "Severity unreachable");

-- populate Metadata tables
INSERT INTO ApplicationType(ApplicationTypeID, Name, Description) VALUES (1,"SYSTEM", "Properties that exist regardless of the Application being monitored");
INSERT INTO ApplicationType(ApplicationTypeID, Name, Description) VALUES (100,"NAGIOS", "System monitored by Nagios");

-- If you change the values of EntityTypeID here, 
-- YOU MUST CHANGE THEM IN THE SOURCE CODE AS WELL
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (1,"HOST_STATUS",    "com.groundwork.collage.model.HostStatus");
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (2,"SERVICE_STATUS", "com.groundwork.collage.model.ServiceStatus");
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (3,"LOG_MESSAGE",    "com.groundwork.collage.model.LogMessage");
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (4,"DEVICE",         "com.groundwork.collage.model.Device");

-- Version Info
INSERT INTO SchemaInfo (Name, Value) VALUES ("Schema Version", "1.5.1");
INSERT INTO SchemaInfo (Name, Value) VALUES ("Schema created", CAST(NOW() AS CHAR));

-- Other Settings
INSERT INTO SchemaInfo (Name, Value) VALUES ("AvailabilityUpdateInterval", "60");
INSERT INTO SchemaInfo (Name, Value) VALUES ("AvailabilityDataPoints", "720");



