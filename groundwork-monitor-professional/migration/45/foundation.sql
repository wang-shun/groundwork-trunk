# Migration script for upgrading Foundation GWCollageDB from version 4.5.x to 5.0

use GWCollageDB;

# Update under-defined consolidation criterias
 
REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('NAGIOSEVENT', 'Device;MonitorStatus;OperationStatus;SubComponent');
REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('SNMPTRAP', 'OperationStatus;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings');
REPLACE INTO ConsolidationCriteria(Name, Criteria)  VALUES ('SYSLOG', 'OperationStatus;Device;MonitorStatus;ipaddress;ErrorType;SubComponent');

# New severity 

REPLACE INTO Severity (Name, Description) VALUES("UP", "Severity UP");
REPLACE INTO Severity (Name, Description) VALUES("DOWN", "Severity DOWN");
REPLACE INTO Severity (Name, Description) VALUES("UNREACHABLE", "Severity unreachable");

# ApplicationType is required

update Host set ApplicationTypeID=100;
update HostGroup set ApplicationTypeID=100;

# Add new fields to log message database

ALTER TABLE `GWCollageDB`.`LogMessage` ADD COLUMN `ConsolidationHash` INTEGER  NOT NULL DEFAULT 0 AFTER `OperationStatusID`,
 ADD COLUMN `StatelessHash` INTEGER  NOT NULL DEFAULT 0 AFTER `ConsolidationHash`,
 ADD COLUMN `isStateChanged` BOOLEAN  NOT NULL DEFAULT false AFTER `StatelessHash`;

#LogPerformance table update

drop table if exists LogPerformanceData;

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

# Update version

Update SchemaInfo set Value='1.5.1' WHERE Name = 'Schema Version';