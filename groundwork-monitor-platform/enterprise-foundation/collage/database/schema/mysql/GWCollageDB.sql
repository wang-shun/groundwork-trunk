-- Collage - The ultimate data integration framework.
-- Copyright (C) 2004-2011  GroundWork Open Source, Inc. (www.groundworkopensource.com)
--
--    This program is free software; you can redistribute it and/or modify
--    it under the terms of version 2 of the GNU General Public License 
--    as published by the Free Software Foundation.
--
--    This program is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with this program; if not, write to the Free Software
--    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
-- $Id: GWCollageDB.sql 18469 2011-03-31 17:35:39Z gherteg $
-- 

-- Start from scratch
-- use GWCollageDB;

-- Updates: Nov 22, 2005 Added CheckType to HostStatus table. New in Nagios 2.0

# September 23, 2006 LogPerformanceData adjusted to accept feeds for Performance data

-- Drop existing tables

drop table if exists SchemaInfo;
drop table if exists DeviceParent;
drop table if exists LogPerformanceData;
drop table if exists PerformanceDataLabel;
drop table if exists LogMessageProperty;
drop table if exists LogMessage;
drop table if exists MonitorList;
drop table if exists Priority;
drop table if exists Component;
drop table if exists Severity;
drop table if exists TypeRule;
drop table if exists MonitorServer;
drop table if exists Server;
drop table if exists OperationStatus;

DROP TABLE IF EXISTS EntityProperty;
drop table if exists ApplicationEntityProperty;
drop table if exists ApplicationEntity;

drop table if exists ServiceAvailability;
drop table if exists HostAvailability;
drop table if exists HostGroupServiceAvailability;
drop table if exists HostGroupHostAvailability;
drop table if exists HostStatusProperty;
drop table if exists HostStatus;
drop table if exists ServiceStatusProperty;
drop table if exists ServiceStatus;

drop table if exists HostGroupCollection;
drop table if exists HostGroup;

drop table if exists Host;
drop table if exists Device;

drop table if exists StateType;
drop table if exists CheckType;
drop table if exists MonitorStatus;

DROP TABLE IF EXISTS CategoryHierarchy;
DROP TABLE IF EXISTS CategoryEntity;
DROP TABLE IF EXISTS Category;

drop table if exists EntityType;
drop table if exists PropertyType;
drop table if exists Entity;

drop table if exists ActionParameter;
drop table if exists ActionProperty;
drop table if exists ApplicationAction;
drop table if exists Action;
drop table if exists ActionType;

-- -----------------------------------------------------------------------
-- Filter & consolidation used for inserting messages into
-- the LogMessage table
-- -----------------------------------------------------------------------
drop table if exists ConsolidationCriteria;
drop table if exists MessageFilter;
drop table if exists ApplicationType;

-- ---------------------------------
-- Plugin Management
-- ---------------------------------
drop table if exists Plugin;
drop table if exists PluginPlatform;

-- Create tables

-- V E R S I O N  I N F O
-- -----------------------------------------------------------------------
-- SchemaInfo
-- -----------------------------------------------------------------------

CREATE TABLE SchemaInfo 
(
	Name VARCHAR (254),
	VALUE VARCHAR(254)
) ENGINE=InnoDB;


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
	Name VARCHAR (128) NOT NULL,
	Description VARCHAR (254),
	StateTransitionCriteria VARCHAR (512) NULL,

	PRIMARY KEY(ApplicationTypeID),
	UNIQUE(Name)
) ENGINE=InnoDB;


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
	Name VARCHAR (128) NOT NULL,
	Description VARCHAR (254),
	IsLogicalEntity BOOLEAN NOT NULL DEFAULT 0,

	PRIMARY KEY(EntityTypeID),
	UNIQUE(Name)
) ENGINE=InnoDB;


-- -----------------------------------------------------------------------
-- PropertyType
--
-- Stores definitions for 'soft-coded' properties of an Entity, namely the name
-- of the property and its type
-- -----------------------------------------------------------------------
CREATE TABLE PropertyType
(
	PropertyTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128) NOT NULL,
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
) ENGINE=InnoDB;


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
	ApplicationEntityPropertyID INTEGER NOT NULL AUTO_INCREMENT,
	ApplicationTypeID INTEGER NOT NULL,
	EntityTypeID INTEGER NOT NULL,
	PropertyTypeID INTEGER NOT NULL,
	SortOrder INTEGER NOT NULL DEFAULT 999,

	PRIMARY KEY(ApplicationEntityPropertyID),
	
	CONSTRAINT UNIQUE KEY (ApplicationTypeID, EntityTypeID, PropertyTypeID),

	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID),
	FOREIGN KEY (EntityTypeID) REFERENCES EntityType(EntityTypeID),
	FOREIGN KEY (PropertyTypeID) REFERENCES PropertyType(PropertyTypeID)

) ENGINE=InnoDB;


-- P H Y S I C A L  L A Y O U T  O F  N E T W O R K 

-- -------------------------------------------------------------------------------
-- Device -- can be server, router, switch, ...
-- The Identification defines the IP or MAC or other identification
-- -------------------------------------------------------------------------------

CREATE TABLE Device 
(
	DeviceID INTEGER NOT NULL AUTO_INCREMENT,
	DisplayName VARCHAR (254),
	Identification VARCHAR (128) NOT NULL,
	DESCRIPTION VARCHAR(254),
	
	PRIMARY KEY(DeviceID),
	UNIQUE(Identification)
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

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

) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;


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

) ENGINE=InnoDB;


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

) ENGINE=InnoDB;

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

) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

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
	HostName VARCHAR (254) NOT NULL,
	Description VARCHAR (4096),
	ApplicationTypeID INTEGER default NULL,

	PRIMARY KEY(HostID),
	UNIQUE(HostName),
	UNIQUE(HostID),
	
	FOREIGN KEY (DeviceID) REFERENCES Device(DeviceID)
        ON DELETE CASCADE,
    FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID)
        ON DELETE CASCADE 
 
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------
-- HostGroup
-- -----------------------------------------------------------------------
CREATE TABLE HostGroup 
(
	HostGroupID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254) NOT NULL,
	Description VARCHAR (4096),
	ApplicationTypeID INTEGER default NULL,
    Alias VARCHAR (254) default NULL,

	PRIMARY KEY(HostGroupID),
	UNIQUE(Name),
	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID)
        ON DELETE CASCADE 
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------
-- StateType
-- -----------------------------------------------------------------------
CREATE TABLE StateType 
(
	StateTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254) NOT NULL,
	Description VARCHAR (254),

	PRIMARY KEY(StateTypeID),
	UNIQUE(Name)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------
-- CheckType
-- -----------------------------------------------------------------------
CREATE TABLE CheckType 
(
	CheckTypeID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254) NOT NULL,
	Description VARCHAR (254),

	PRIMARY KEY(CheckTypeID),
	UNIQUE(Name)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------
-- MonitorStatus
-- -----------------------------------------------------------------------
CREATE TABLE MonitorStatus 
(
	MonitorStatusID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (254) NOT NULL,
	Description VARCHAR (254),

	PRIMARY KEY(MonitorStatusID),
	UNIQUE(Name)
) ENGINE=InnoDB;



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
	StateTypeID INTEGER DEFAULT NULL,
	NextCheckTime DATETIME,
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
		ON DELETE CASCADE,
	FOREIGN KEY (StateTypeID) REFERENCES StateType(StateTypeID)
		ON DELETE CASCADE
            
) ENGINE=InnoDB;

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
	ValueString    VARCHAR(32768),
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

) ENGINE=InnoDB;


-- -----------------------------------------------------------------------
-- ServiceStatus
-- -----------------------------------------------------------------------

CREATE TABLE ServiceStatus 
(
	ServiceStatusID INTEGER NOT NULL AUTO_INCREMENT,
	ApplicationTypeID INTEGER NOT NULL,
	ServiceDescription VARCHAR(254) NOT NULL,
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
) ENGINE=InnoDB;


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
	ValueString     VARCHAR(16384) DEFAULT NULL,
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

) ENGINE=InnoDB;


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
	TextMessage VARCHAR(4096)  NOT NULL,
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
	StateTransitionHash INTEGER,

	PRIMARY KEY(LogMessageID),

	FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType(ApplicationTypeID),
	FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID)
		ON DELETE CASCADE,
	CONSTRAINT `fk_LogMessage_ServiceStatusID` FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus (ServiceStatusID) ON DELETE SET NULL,
	CONSTRAINT `fk_LogMessage_HostStatusID` FOREIGN KEY (HostStatusID) REFERENCES HostStatus (HostStatusID) ON DELETE SET NULL,
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
		ON DELETE CASCADE,
	INDEX `idx_LogMessage_ConsolidationHash`(`ConsolidationHash`),
	INDEX `idx_LogMessage_StatelessHash`(`StatelessHash`),
	INDEX `idx_LogMessage_FirstInsertDate`(`FirstInsertDate`),
	INDEX `idx_LogMessage_LastInsertDate`(`LastInsertDate`),
	INDEX `idx_LogMessage_ReportDate`(`ReportDate`),
	INDEX `idx_LogMessage_StateTransitionHash`(`StateTransitionHash`)

) ENGINE=InnoDB;


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
	ValueString     VARCHAR(4096) DEFAULT NULL,
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

) ENGINE=InnoDB;


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
	ValueString     VARCHAR(4096) DEFAULT NULL,
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

) ENGINE=InnoDB;

-- -------------------------------------------
-- Map Performance data entries to a human
-- readable strings
--
-- ------------------------------------------
CREATE TABLE PerformanceDataLabel 
(
	PerformanceDataLabelID INTEGER NOT NULL AUTO_INCREMENT ,
	
	PerformanceName VARCHAR(254) DEFAULT "",
    ServiceDisplayName VARCHAR(128) DEFAULT "",
	MetricLabel VARCHAR(128) DEFAULT "",
	Unit VARCHAR(64) DEFAULT "",
	
    PRIMARY KEY(PerformanceDataLabelID),
    UNIQUE(PerformanceName)
) ENGINE = InnoDB;

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
--	PerformanceName VARCHAR(254) DEFAULT "",
    PerformanceDataLabelID INTEGER,
	
    PRIMARY KEY(LogPerformanceDataID),
    FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus(ServiceStatusID)
        ON DELETE CASCADE,

	FOREIGN KEY (PerformanceDataLabelID) REFERENCES PerformanceDataLabel(PerformanceDataLabelID)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------------------------------------------------------
-- Filter & consolidation used for inserting messages into
-- the LogMessage table

-- ConsolidationCriteria	
-- If a message to be inserted is equal to all the fields defined in the criteria
-- the msg count and the last insert date of an existing message in the 
-- LogMessagewould be changed.
-- FORMAT of criteria: table.field;table.field example 'LogMessage.SeverityID;LogMessage.ServerID'
-- ------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE ConsolidationCriteria
(
	ConsolidationCriteriaID INTEGER NOT NULL AUTO_INCREMENT ,
	Name VARCHAR (254) NOT NULL,
	Criteria VARCHAR(512) NOT NULL,
	
    PRIMARY KEY(ConsolidationCriteriaID),
    UNIQUE(Name)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------------------------------
-- Message filter.
-- If a regular expression matches the LogMessage the message
-- will not be inserted into the database unless the flag isChangeSeverityToStatistic
-- is set. In this case the message would be inserted but the severity would be
-- changed to STATISTIC
-- -----------------------------------------------------------------------------------------------------

CREATE TABLE MessageFilter
(
	MessageFilterID INTEGER NOT NULL AUTO_INCREMENT ,
	Name VARCHAR (254) NOT NULL,
	RegExpresion VARCHAR(512) NOT NULL,
	isChangeSeverityToStatistic BOOLEAN DEFAULT 0,
	
    PRIMARY KEY(MessageFilterID),
    UNIQUE(Name)
) ENGINE=InnoDB;

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
  Name varchar(254) NOT NULL,
  Description varchar(4096) default NULL,
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

--
-- Customizable Action Tables
--

CREATE TABLE ActionType (
  ActionTypeID INTEGER NOT NULL auto_increment,
  Name varchar(256) NOT NULL,
  ClassName varchar(256) NOT NULL,

  PRIMARY KEY(ActionTypeID),
  UNIQUE(Name)

) ENGINE=InnoDB;

CREATE TABLE Action (
  ActionID INTEGER NOT NULL auto_increment,
  ActionTypeID INTEGER NOT NULL,
  Name varchar(256) NOT NULL,
  Description varchar(512) NULL,

  PRIMARY KEY(ActionID),
  UNIQUE(Name),

  FOREIGN KEY (ActionTypeID) REFERENCES ActionType (ActionTypeID)
        ON DELETE CASCADE,

  INDEX `idx_Action_Name`(`Name`)
) ENGINE=InnoDB;

CREATE TABLE ApplicationAction (
  ApplicationTypeID INTEGER NOT NULL,
  ActionID INTEGER NOT NULL,

  PRIMARY KEY  (ApplicationTypeID, ActionID),

  FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType (ApplicationTypeID)
        ON DELETE CASCADE,
  FOREIGN KEY (ActionID) REFERENCES Action (ActionID)
        ON DELETE CASCADE

) ENGINE=InnoDB;

CREATE TABLE ActionProperty (
  ActionPropertyID INTEGER NOT NULL auto_increment,
  ActionID INTEGER NOT NULL,
  Name varchar(128) NOT NULL,
  Value text NULL,

  PRIMARY KEY  (ActionPropertyID),
  CONSTRAINT UNIQUE KEY(ActionID, Name),

  FOREIGN KEY (ActionID) REFERENCES Action (ActionID)
        ON DELETE CASCADE

) ENGINE=InnoDB;

CREATE TABLE ActionParameter (
  ActionParameterID INTEGER NOT NULL auto_increment,
  ActionID INTEGER NOT NULL,
  Name varchar(128) NOT NULL,
  Value text NULL,

  PRIMARY KEY  (ActionParameterID),
  CONSTRAINT UNIQUE KEY(ActionID, Name),

  FOREIGN KEY (ActionID) REFERENCES Action (ActionID)
        ON DELETE CASCADE

) ENGINE=InnoDB;


# Changes for category implementation
CREATE TABLE `GWCollageDB`.`Entity` (
  `EntityID` INT(11)  NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(254)  NOT NULL,
  `Description` VARCHAR(254)  NOT NULL,
  `Class` VARCHAR(254)  NOT NULL,
  `ApplicationTypeID` INT(11)  NOT NULL,
  PRIMARY KEY (`EntityID`),
  CONSTRAINT `ApplicationTypeID_ibfk1_1` FOREIGN KEY (`ApplicationTypeID`) REFERENCES `ApplicationType` (`ApplicationTypeID`) ON DELETE CASCADE ON UPDATE RESTRICT
)
ENGINE = InnoDB
COMMENT = 'New Entity Table to support service groups'
CHARSET=latin1;

ALTER TABLE `GWCollageDB`.`EntityType` ADD COLUMN `IsApplicationTypeSupported` TINYINT(1)  NOT NULL DEFAULT 0 AFTER `IsLogicalEntity`;

ALTER TABLE `GWCollageDB`.`Category` ADD COLUMN `EntityTypeID` INT(11)  NOT NULL AFTER `Description`,
 ADD CONSTRAINT `EntityTypeID_ibfk1_1` FOREIGN KEY (`EntityTypeID`)
    REFERENCES `EntityType` (`EntityTypeID`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT;
    
-- PLUGIN    T A B L E S

-- -----------------------------------------------------------------------
-- PluginPlatform
--
-- Stores an enumeration of the types of platforms that can be
-- configured.
-- -----------------------------------------------------------------------
CREATE TABLE PluginPlatform 
(
	PlatformID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128) NOT NULL,
	Arch INTEGER NOT NULL,
	Description VARCHAR (254),	

	PRIMARY KEY(PlatformID),
	UNIQUE KEY idx_PluginPlatform_Name_Arch USING BTREE (Name, Arch)
) ENGINE=InnoDB;    

-- -----------------------------------------------------------------------
-- PluginInfo
--
-- Stores plugin updates.
-- -----------------------------------------------------------------------
CREATE TABLE Plugin
(
	PluginID INTEGER NOT NULL AUTO_INCREMENT,
	Name VARCHAR (128) NOT NULL,
	Url VARCHAR (254) default NULL,	
	PlatformID INTEGER NOT NULL,
	Dependencies VARCHAR (254) default NULL,
	LastUpdateTimestamp TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
	Checksum VARCHAR(254) NOT NULL,
	LastUpdatedBy VARCHAR(254) default NULL,
	
	PRIMARY KEY (PluginID),
	UNIQUE KEY idx_Plugin_PlatformID_Name USING BTREE (PlatformID, Name),
	FOREIGN KEY (PlatformID) REFERENCES PluginPlatform (PlatformID) ON DELETE CASCADE
) ENGINE=InnoDB;    

