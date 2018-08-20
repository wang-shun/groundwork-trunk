# $Id: $
#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
################################################################################
# Foundation Database Migration Script
# This script encapsulates all DB migration for 4.5.0 to the latest version of
# the Foundation database.
################################################################################
delimiter $$

DROP PROCEDURE IF EXISTS `GWCollageDB`.`sp_Migrate`$$
CREATE PROCEDURE `GWCollageDB`.`sp_Migrate` ()

BEGIN

    DECLARE currentVersion varchar(16);
    DECLARE constraintName varchar(64);
    DECLARE temp_65_feature varchar(16);

    # Begin Transaction
    START TRANSACTION;

    # Query current version of the schema
    SELECT VALUE INTO currentVersion FROM SchemaInfo WHERE Name = 'CurrentSchemaVersion';
    SELECT count(*) INTO temp_65_feature FROM PropertyType WHERE Name='UpdatedBy';	
    IF currentVersion is null THEN
        SELECT Value INTO currentVersion FROM SchemaInfo WHERE Name='Schema Version';

        ######################################################################################
        # GWMON-9505
        # GroundWork Monitor EE 6.3 clean install did not set the schema version to 3.0.4
        # and left it at 3.0.3
        # This stored proc will just upgrade the version number to 3.0.4 if current is 3.0.3
        ######################################################################################

        IF currentVersion = '3.0.3' THEN
            SET currentVersion = '3.0.4';
            SELECT concat('GWCollageDB Schema updated from 3.0.3 To Version: ', currentVersion) AS UpdateCompleted;
        END IF;

        INSERT INTO SchemaInfo (Name, Value) VALUES ('CurrentSchemaVersion', currentVersion);
        INSERT INTO SchemaInfo (Name, Value) (SELECT 'SchemaUpdated', Value FROM SchemaInfo WHERE Name='Schema created');
    END IF;

    ###############################################################################
    # 4.5.x to 5.0 Upgrade
    ###############################################################################
    IF currentVersion = '1.1-Beta1' THEN

        # Update under-defined consolidation criterias
        REPLACE INTO ConsolidationCriteria(Name, Criteria) VALUES ('NAGIOSEVENT', 'Device;MonitorStatus;OperationStatus;SubComponent');
        REPLACE INTO ConsolidationCriteria(Name, Criteria) VALUES ('SNMPTRAP', 'OperationStatus;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings');
        REPLACE INTO ConsolidationCriteria(Name, Criteria) VALUES ('SYSLOG', 'OperationStatus;Device;MonitorStatus;ipaddress;ErrorType;SubComponent');

        # New severity
        REPLACE INTO Severity (Name, Description) VALUES("UP", "Severity UP");
        REPLACE INTO Severity (Name, Description) VALUES("DOWN", "Severity DOWN");
        REPLACE INTO Severity (Name, Description) VALUES("UNREACHABLE", "Severity unreachable");

        # ApplicationType is required
        update Host set ApplicationTypeID=100 where ApplicationTypeID is null;
        update HostGroup set ApplicationTypeID=100 where ApplicationTypeID is null;

        # Add new fields to log message database
        ALTER TABLE `GWCollageDB`.`LogMessage`
            ADD COLUMN `ConsolidationHash` INTEGER NOT NULL DEFAULT 0 AFTER `OperationStatusID`,
            ADD COLUMN `StatelessHash` INTEGER NOT NULL DEFAULT 0 AFTER `ConsolidationHash`,
            ADD COLUMN `isStateChanged` BOOLEAN NOT NULL DEFAULT false AFTER `StatelessHash`;

        #LogPerformance table update
        drop table if exists LogPerformanceData;

        CREATE TABLE LogPerformanceData
        (
            LogPerformanceDataID INTEGER NOT NULL AUTO_INCREMENT,
            ServiceStatusID INTEGER NOT NULL,
            LastCheckTime DATETIME NOT NULL,
            Maximum DOUBLE DEFAULT 0,
            Minimum DOUBLE DEFAULT 0,
            Average DOUBLE DEFAULT 0,
            MeasurementPoints INTEGER DEFAULT 0,
            PerformanceName VARCHAR(254) DEFAULT "",

            PRIMARY KEY(LogPerformanceDataID),
            FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus(ServiceStatusID) ON DELETE CASCADE
        ) TYPE = InnoDB;

        ###############################################################################
        # UPDATE SCHEMA INFORMATION
        ###############################################################################
        SET currentVersion = '1.5.1';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    ###############################################################################
    # Upgrade from 1.5.1 to 1.5.2
    ###############################################################################
    IF currentVersion = '1.5.1' THEN

        # Create New Log Message Table - It is faster to create a new table and
        # rename it than drop and recreate constraints and indexes
        DROP TABLE IF EXISTS newLogMessage;
        CREATE TABLE `GWCollageDB`.`newLogMessage` LIKE `GWCollageDB`.`LogMessage`;

        # Add Indexes to LogMessage table for ConsolidationHash and StatelessHash

        IF NOT EXISTS (SELECT NULL FROM information_schema.statistics WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'newLogMessage' AND INDEX_NAME = 'idx_LogMessage_ConsolidationHash') THEN
            ALTER TABLE `GWCollageDB`.`newLogMessage` ADD INDEX idx_LogMessage_ConsolidationHash USING BTREE (ConsolidationHash);
        END IF;

        IF NOT EXISTS (SELECT NULL FROM information_schema.statistics WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'newLogMessage' AND INDEX_NAME = 'idx_LogMessage_StatelessHash') THEN
            ALTER TABLE `GWCollageDB`.`newLogMessage` ADD INDEX idx_LogMessage_StatelessHash USING BTREE (StatelessHash);
        END IF;

        # Copy Data From Old LogMessage Table
        INSERT INTO newLogMessage SELECT * FROM LogMessage;

        #Drop Old LogMessage Table and rename the new one to LogMessage
        SET FOREIGN_KEY_CHECKS=0;
        DROP TABLE `GWCollageDB`.`LogMessage`;
        SET FOREIGN_KEY_CHECKS=1;

        # Add Foreign Key Constraints to new LogMessage table b/c they are not copied with the CREATE TABLE LIKE
        # Do this after we drop the LogMessage table to insure there are no collisions with the foreign key constraints on the old table
        # Constraint names have global scope
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationTypeID` FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType (ApplicationTypeID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_DeviceID` FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_MonitorStatusID` FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus (MonitorStatusID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_SeverityID` FOREIGN KEY (SeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationSeverityID` FOREIGN KEY (ApplicationSeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_PriorityID` FOREIGN KEY (PriorityID) REFERENCES Priority (PriorityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_TypeRuleID` FOREIGN KEY (TypeRuleID) REFERENCES TypeRule (TypeRuleID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ComponentID` FOREIGN KEY (ComponentID) REFERENCES Component (ComponentID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_OperationStatusID` FOREIGN KEY (OperationStatusID) REFERENCES OperationStatus (OperationStatusID) ON DELETE CASCADE;

        # Add Updated Foreign Key Constraints
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_HostStatusID` FOREIGN KEY (HostStatusID) REFERENCES HostStatus (HostStatusID) ON DELETE SET NULL;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ServiceStatusID` FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus (ServiceStatusID) ON DELETE SET NULL;

        # Move New Table
        ALTER TABLE `GWCollageDB`.`newLogMessage` RENAME TO `GWCollageDB`.`LogMessage`;

        ###############################################################################
        # UPDATE SCHEMA INFORMATION
        ###############################################################################
        SET currentVersion = '1.5.2';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    ###############################################################################
    # Upgrade from 1.5.2 to 1.6.1
    ###############################################################################
    IF currentVersion = '1.5.2' THEN
        ALTER TABLE `GWCollageDB`.`ApplicationType` MODIFY COLUMN `Name` VARCHAR(128) NOT NULL;
        ALTER TABLE `GWCollageDB`.`HostGroup` MODIFY COLUMN `Name` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`Category` MODIFY COLUMN `Name` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`EntityType` MODIFY COLUMN `Name` VARCHAR(128) NOT NULL;
        ALTER TABLE `GWCollageDB`.`PropertyType` MODIFY COLUMN `Name` VARCHAR(128) NOT NULL;
        ALTER TABLE `GWCollageDB`.`Device` MODIFY COLUMN `Identification` VARCHAR(128) NOT NULL;
        ALTER TABLE `GWCollageDB`.`Host` MODIFY COLUMN `HostName` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`StateType` MODIFY COLUMN `Name` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`CheckType` MODIFY COLUMN `Name` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`MonitorStatus` MODIFY COLUMN `Name` VARCHAR(254) NOT NULL;
        ALTER TABLE `GWCollageDB`.`ServiceStatus` MODIFY COLUMN `ServiceDescription` VARCHAR(254) NOT NULL;

        # Cleanup log message table which may have been corrupted due to lack of constraints in upgrade to version 1.5.2
        DELETE FROM LogMessage WHERE ApplicationTypeID NOT IN (SELECT ApplicationTypeID From ApplicationType);
        DELETE FROM LogMessage WHERE DeviceID NOT IN (SELECT DeviceID From Device);
        DELETE FROM LogMessage WHERE MonitorStatusID <> null AND MonitorStatusID NOT IN (SELECT MonitorStatusID From MonitorStatus);
        DELETE FROM LogMessage WHERE SeverityID NOT IN (SELECT SeverityID From Severity);
        DELETE FROM LogMessage WHERE ApplicationSeverityID NOT IN (SELECT SeverityID From Severity);
        DELETE FROM LogMessage WHERE PriorityID NOT IN (SELECT PriorityID From Priority);
        DELETE FROM LogMessage WHERE TypeRuleID NOT IN (SELECT TypeRuleID From TypeRule);
        DELETE FROM LogMessage WHERE ComponentID NOT IN (SELECT ComponentID From Component);
        DELETE FROM LogMessage WHERE OperationStatusID NOT IN (SELECT OperationStatusID From OperationStatus);

        UPDATE LogMessage SET ServiceStatusID = null WHERE ServiceStatusID NOT IN (SELECT ServiceStatusID FROM ServiceStatus);
        UPDATE LogMessage SET HostStatusID = null WHERE HostStatusID NOT IN (SELECT HostStatusID FROM HostStatus);

        # Create New Log Message Table - It is faster to create a new table and
        # rename it than drop and recreate constraints and indexes
        DROP TABLE IF EXISTS newLogMessage;
        CREATE TABLE `GWCollageDB`.`newLogMessage` LIKE `GWCollageDB`.`LogMessage`;

        # Add Indexes to LogMessage table for Date Columns

        IF NOT EXISTS (SELECT NULL FROM information_schema.statistics WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'newLogMessage' AND INDEX_NAME = 'idx_LogMessage_FirstInsertDate') THEN
            ALTER TABLE `GWCollageDB`.`newLogMessage` ADD INDEX idx_LogMessage_FirstInsertDate USING BTREE (FirstInsertDate);
        END IF;

        IF NOT EXISTS (SELECT NULL FROM information_schema.statistics WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'newLogMessage' AND INDEX_NAME = 'idx_LogMessage_LastInsertDate') THEN
            ALTER TABLE `GWCollageDB`.`newLogMessage` ADD INDEX idx_LogMessage_LastInsertDate USING BTREE (LastInsertDate);
        END IF;

        IF NOT EXISTS (SELECT NULL FROM information_schema.statistics WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'newLogMessage' AND INDEX_NAME = 'idx_LogMessage_ReportDate') THEN
            ALTER TABLE `GWCollageDB`.`newLogMessage` ADD INDEX idx_LogMessage_ReportDate USING BTREE (ReportDate);
        END IF;

        # Copy Data From Old LogMessage Table
        INSERT INTO newLogMessage SELECT * FROM LogMessage;

        # Drop Old LogMessage Table and rename the new one to LogMessage
        SET FOREIGN_KEY_CHECKS=0;
        DROP TABLE `GWCollageDB`.`LogMessage`;
        SET FOREIGN_KEY_CHECKS=1;

        # Add Foreign Key Constraints to new LogMessage table b/c they are not copied with the CREATE TABLE LIKE
        # Do this after we drop the LogMessage table to insure there are no collisions with the foreign key constraints on the old table
        # Constraint names have global scope
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationTypeID` FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType (ApplicationTypeID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_DeviceID` FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ServiceStatusID` FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus (ServiceStatusID) ON DELETE SET NULL;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_HostStatusID` FOREIGN KEY (HostStatusID) REFERENCES HostStatus (HostStatusID) ON DELETE SET NULL;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_MonitorStatusID` FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus (MonitorStatusID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_SeverityID` FOREIGN KEY (SeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationSeverityID` FOREIGN KEY (ApplicationSeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_PriorityID` FOREIGN KEY (PriorityID) REFERENCES Priority (PriorityID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_TypeRuleID` FOREIGN KEY (TypeRuleID) REFERENCES TypeRule (TypeRuleID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_ComponentID` FOREIGN KEY (ComponentID) REFERENCES Component (ComponentID) ON DELETE CASCADE;
        ALTER TABLE `GWCollageDB`.`newLogMessage` ADD CONSTRAINT `fk_LogMessage_OperationStatusID` FOREIGN KEY (OperationStatusID) REFERENCES OperationStatus (OperationStatusID) ON DELETE CASCADE;

        # Move New Table
        ALTER TABLE `GWCollageDB`.`newLogMessage` RENAME TO `GWCollageDB`.`LogMessage`;

        #Drop tables for availability which are no longer used
        DROP TABLE IF EXISTS ServiceAvailability;
        DROP TABLE IF EXISTS HostAvailability;
        DROP TABLE IF EXISTS HostGroupServiceAvailability;
        DROP TABLE IF EXISTS HostGroupHostAvailability;

        ###############################################################################
        # Make sure that the Device DisplayName *always* has a value
        ###############################################################################
        UPDATE Device INNER JOIN Host ON Device.DeviceID = Host.DeviceID
        SET Device.DisplayName = Host.HostName
        WHERE Device.DisplayName IS NULL OR Device.DisplayName = '';

        UPDATE Device
        SET Device.DisplayName = Device.Identification
        WHERE Device.DisplayName IS NULL OR Device.DisplayName = '';

        #############################################################################
        # It seems that earlier upgrades did not set the ApplicationTypeID to
        # a no NULL value. WebService serialization will fail.
        ##############################################################################
        # ApplicationType is required
        update Host set ApplicationTypeID=100 where ApplicationTypeID is null;
        update HostGroup set ApplicationTypeID=100 where ApplicationTypeID is null;

        ###############################################################################
        # UPDATE SCHEMA INFORMATION
        ###############################################################################
        SET currentVersion = '1.6.1';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    ###############################################################################
    # Fix For 1.6 Beta Releases - Beta schemas were at 1.6 - So this section
    # fixes any issues with the Beta release schemas.  Clients upgrading from 1.5.2
    # or previous will not execute this section and will be properly upgraded
    ###############################################################################
    IF currentVersion = '1.6' THEN

        # Cleanup log message table which may have been corrupted due to lack of constraints
        DELETE FROM LogMessage WHERE ApplicationTypeID NOT IN (SELECT ApplicationTypeID From ApplicationType);
        DELETE FROM LogMessage WHERE DeviceID NOT IN (SELECT DeviceID From Device);
        DELETE FROM LogMessage WHERE MonitorStatusID <> null AND MonitorStatusID NOT IN (SELECT MonitorStatusID From MonitorStatus);
        DELETE FROM LogMessage WHERE SeverityID NOT IN (SELECT SeverityID From Severity);
        DELETE FROM LogMessage WHERE ApplicationSeverityID NOT IN (SELECT SeverityID From Severity);
        DELETE FROM LogMessage WHERE PriorityID NOT IN (SELECT PriorityID From Priority);
        DELETE FROM LogMessage WHERE TypeRuleID NOT IN (SELECT TypeRuleID From TypeRule);
        DELETE FROM LogMessage WHERE ComponentID NOT IN (SELECT ComponentID From Component);
        DELETE FROM LogMessage WHERE OperationStatusID NOT IN (SELECT OperationStatusID From OperationStatus);

        UPDATE LogMessage SET ServiceStatusID = null WHERE ServiceStatusID NOT IN (SELECT ServiceStatusID FROM ServiceStatus);
        UPDATE LogMessage SET HostStatusID = null WHERE HostStatusID NOT IN (SELECT HostStatusID FROM HostStatus);

        # Add Foreign Key Constraints back on LogMessage table.  The were removed erroneously
        # with the 1.5.2 and old 1.6 upgrades

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_ApplicationTypeID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationTypeID` FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType (ApplicationTypeID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_DeviceID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_DeviceID` FOREIGN KEY (DeviceID) REFERENCES Device (DeviceID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_ServiceStatusID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_ServiceStatusID` FOREIGN KEY (ServiceStatusID) REFERENCES ServiceStatus (ServiceStatusID) ON DELETE SET NULL;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_HostStatusID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_HostStatusID` FOREIGN KEY (HostStatusID) REFERENCES HostStatus (HostStatusID) ON DELETE SET NULL;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_MonitorStatusID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_MonitorStatusID` FOREIGN KEY (MonitorStatusID) REFERENCES MonitorStatus (MonitorStatusID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_SeverityID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_SeverityID` FOREIGN KEY (SeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_ApplicationSeverityID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_ApplicationSeverityID` FOREIGN KEY (ApplicationSeverityID) REFERENCES Severity (SeverityID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_PriorityID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_PriorityID` FOREIGN KEY (PriorityID) REFERENCES Priority (PriorityID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_TypeRuleID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_TypeRuleID` FOREIGN KEY (TypeRuleID) REFERENCES TypeRule (TypeRuleID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_ComponentID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_ComponentID` FOREIGN KEY (ComponentID) REFERENCES Component (ComponentID) ON DELETE CASCADE;
        END IF;

        SELECT K.CONSTRAINT_NAME INTO constraintName FROM information_schema.TABLE_CONSTRAINTS T
            INNER JOIN information_schema.KEY_COLUMN_USAGE K
                ON T.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA AND T.TABLE_SCHEMA = K.TABLE_SCHEMA
                AND T.CONSTRAINT_NAME = K.CONSTRAINT_NAME AND T.CONSTRAINT_TYPE = 'FOREIGN KEY'
            WHERE T.CONSTRAINT_SCHEMA = 'GWCollageDB' AND K.TABLE_NAME = 'LogMessage'
            AND K.CONSTRAINT_NAME = 'fk_LogMessage_OperationStatusID';

        IF constraintName is null THEN
            ALTER TABLE `GWCollageDB`.`LogMessage` ADD CONSTRAINT `fk_LogMessage_OperationStatusID` FOREIGN KEY (OperationStatusID) REFERENCES OperationStatus (OperationStatusID) ON DELETE CASCADE;
        END IF;

        ###############################################################################
        # UPDATE SCHEMA INFORMATION
        ###############################################################################
        SET currentVersion = '1.6.1';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    ###############################################################################
    # Upgrade from 1.6.1 to 2.0.0
    ###############################################################################
    IF currentVersion = '1.6.1' THEN
        # Create New ApplicationEntityProperty Table - Added ApplicationEntityPropertyID primary key column
        DROP TABLE IF EXISTS newApplicationEntityProperty;

        CREATE TABLE newApplicationEntityProperty
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
        ) TYPE = InnoDB;

        # Copy Old ApplicationEntityProperty data to new table
        INSERT INTO newApplicationEntityProperty (ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
        SELECT ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder FROM ApplicationEntityProperty;

        #Drop Old ApplicationEntityProperty Table and rename the new one to ApplicationEntityProperty
        SET FOREIGN_KEY_CHECKS=0;
        DROP TABLE `GWCollageDB`.`ApplicationEntityProperty`;
        SET FOREIGN_KEY_CHECKS=1;

        # Move New Table
        ALTER TABLE `GWCollageDB`.`newApplicationEntityProperty` RENAME TO `GWCollageDB`.`ApplicationEntityProperty`;

        # Update and Add New Entity Types
        ALTER TABLE `GWCollageDB`.`EntityType` ADD COLUMN `IsLogicalEntity` BOOLEAN NOT NULL DEFAULT 0;

        UPDATE EntityType SET Description = 'com.groundwork.collage.model.impl.HostStatus' WHERE Name='HOST_STATUS';
        UPDATE EntityType SET Description = 'com.groundwork.collage.model.impl.ServiceStatus' WHERE Name='SERVICE_STATUS';
        UPDATE EntityType SET Description = 'com.groundwork.collage.model.impl.LogMessage' WHERE Name='LOG_MESSAGE';
        UPDATE EntityType SET Description = 'com.groundwork.collage.model.impl.Device' WHERE Name='DEVICE';

        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (5,"HOST", "com.groundwork.collage.model.impl.Host");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (6,"HOSTGROUP", "com.groundwork.collage.model.impl.HostGroup");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (7,"APPLICATION_TYPE", "com.groundwork.collage.model.impl.ApplicationType");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (8,"CATEGORY", "com.groundwork.collage.model.impl.Category");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (9,"CHECK_TYPE", "com.groundwork.collage.model.impl.CheckType");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (10,"COMPONENT", "com.groundwork.collage.model.impl.Component");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (11,"MONITOR_STATUS", "com.groundwork.collage.model.impl.MonitorStatus");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (12,"OPERATION_STATUS", "com.groundwork.collage.model.impl.OperationStatus");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (13,"PRIORITY", "com.groundwork.collage.model.impl.Priority");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (14,"SEVERITY", "com.groundwork.collage.model.impl.Severity");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (15,"STATE_TYPE", "com.groundwork.collage.model.impl.StateType");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (16,"TYPE_RULE", "com.groundwork.collage.model.impl.TypeRule");
        INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (17,"MONITOR_SERVER", "com.groundwork.collage.model.impl.MonitorServer");

        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (18,"LOG_MESSAGE_STATISTICS", "com.groundwork.collage.model.impl.LogMessageStatistic", 1);
        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (19,"HOST_STATISTICS", "com.groundwork.collage.model.impl.HostStatistic", 1);
        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (20,"SERVICE_STATISTICS", "com.groundwork.collage.model.impl.ServiceStatistic", 1);
        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (21,"HOST_STATE_TRANSITIONS", "com.groundwork.collage.model.impl.HostStateTransition", 1);
        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (22,"SERVICE_STATE_TRANSITIONS", "com.groundwork.collage.model.impl.ServiceStateTransition", 1);

        # Remove various PropertyType and ApplicationEntityProperty from NAGIOS application.  They are built-in
        # properties and do not need to be listed as a dynamic property.
        # Note:  We are not updating the SortOrder for the existing properties.
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastCheckTime');
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'NextCheckTime');
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'StateType');
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CheckType');
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastHardState');
        DELETE FROM ApplicationEntityProperty WHERE PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'MonitorStatus');

        # Remove LastStateChange as a SERVICE_STATUS property.  It is still a HOST_STATUS property
        DELETE FROM ApplicationEntityProperty
            WHERE EntityTypeID IN (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS')
            AND PropertyTypeID IN (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastStateChange');

        DELETE FROM PropertyType WHERE Name = 'LastCheckTime';
        DELETE FROM PropertyType WHERE Name = 'NextCheckTime';
        DELETE FROM PropertyType WHERE Name = 'StateType';
        DELETE FROM PropertyType WHERE Name = 'CheckType';
        DELETE FROM PropertyType WHERE Name = 'LastHardState';
        DELETE FROM PropertyType WHERE Name = 'MonitorStatus';

        ##############################################################################
        # Add New Action Tables - TODO:  Populate out-of-the-box Actions when defined.
        ##############################################################################

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

            FOREIGN KEY (ActionTypeID) REFERENCES ActionType (ActionTypeID) ON DELETE CASCADE,

            INDEX `idx_Action_Name`(`Name`)
        ) ENGINE=InnoDB;

        CREATE TABLE ApplicationAction (
            ApplicationTypeID INTEGER NOT NULL,
            ActionID INTEGER NOT NULL,

            PRIMARY KEY (ApplicationTypeID, ActionID),

            FOREIGN KEY (ApplicationTypeID) REFERENCES ApplicationType (ApplicationTypeID) ON DELETE CASCADE,
            FOREIGN KEY (ActionID) REFERENCES Action (ActionID) ON DELETE CASCADE
        ) ENGINE=InnoDB;

        CREATE TABLE ActionProperty (
            ActionPropertyID INTEGER NOT NULL auto_increment,
            ActionID INTEGER NOT NULL,
            Name varchar(128) NOT NULL,
            Value text NULL,

            PRIMARY KEY (ActionPropertyID),
            CONSTRAINT UNIQUE KEY(ActionID, Name),

            FOREIGN KEY (ActionID) REFERENCES Action (ActionID) ON DELETE CASCADE
        ) ENGINE=InnoDB;

        CREATE TABLE ActionParameter (
            ActionParameterID INTEGER NOT NULL auto_increment,
            ActionID INTEGER NOT NULL,
            Name varchar(128) NOT NULL,
            Value text NULL,

            PRIMARY KEY (ActionParameterID),
            CONSTRAINT UNIQUE KEY(ActionID, Name),

            FOREIGN KEY (ActionID) REFERENCES Action (ActionID) ON DELETE CASCADE
        ) ENGINE=InnoDB;

        ###############################################################################
        # Populate out-of-the-box actions
        ###############################################################################

        -- Populate Action Tables
        INSERT INTO ActionType (Name, ClassName) VALUES( "LOG_MESSAGE_OPERATION_STATUS", "org.groundwork.foundation.bs.actions.UpdateOperationStatusAction");
        INSERT INTO ActionType (Name, ClassName) VALUES( "SCRIPT_ACTION", "org.groundwork.foundation.bs.actions.ShellScriptAction");
        INSERT INTO ActionType (Name, ClassName) VALUES( "HTTP_REQUEST_ACTION", "org.groundwork.foundation.bs.actions.HttpRequestAction");
        INSERT INTO ActionType (Name, ClassName) VALUES( "NAGIOS_ACKNOWLEDGE_ACTION", "org.groundwork.foundation.bs.actions.NagiosAcknowledgeAction");

        -- NAGIOS Acknowledge

        INSERT INTO Action (ActionTypeID, Name, Description) VALUES(
            (SELECT ActionTypeID FROM ActionType WHERE Name = 'NAGIOS_ACKNOWLEDGE_ACTION'),
            "Nagios Acknowledge",
            "Acknowledge Nagios Log Message"
        );

        INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
            (SELECT ActionID FROM Action WHERE Name = "Nagios Acknowledge"),
            "NagiosCommandFile",
            "/usr/local/groundwork/nagios/var/spool/nagios.cmd"
        );

        -- Log Message Operation Status Actions

        INSERT INTO Action (ActionTypeID, Name, Description) VALUES(
            (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'),
            "Accept Log Message",
            "Update Log Message Operation Status To Accepted"
        );

        INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
            (SELECT ActionID FROM Action WHERE Name = "Accept Log Message"),
            "OperationStatus",
            "ACCEPTED"
        );

        -- Close

        INSERT INTO Action (ActionTypeID, Name, Description) VALUES(
            (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'),
            "Close Log Message",
            "Update Log Message Operation Status To Closed"
        );

        INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
            (SELECT ActionID FROM Action WHERE Name = "Close Log Message"),
            "OperationStatus",
            "CLOSED"
        );

        -- Notify

        INSERT INTO Action (ActionTypeID, Name, Description) VALUES(
            (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'),
            "Notify Log Message",
            "Update Log Message Operation Status To Notified"
        );

        INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
            (SELECT ActionID FROM Action WHERE Name = "Notify Log Message"),
            "OperationStatus",
            "NOTIFIED"
        );

        -- Open

        INSERT INTO Action (ActionTypeID, Name, Description) VALUES(
            (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'),
            "Open Log Message",
            "Update Log Message Operation Status To Open"
        );

        INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
            (SELECT ActionID FROM Action WHERE Name = "Open Log Message"),
            "OperationStatus",
            "OPEN"
        );

        -- System Application Actions - Common to all application types
        BEGIN
            DECLARE appTypeSystemId INTEGER;
            DECLARE appTypeNagiosId INTEGER;

            SELECT ApplicationTypeID INTO appTypeSystemId FROM ApplicationType WHERE Name='SYSTEM';
            SELECT ApplicationTypeID INTO appTypeNagiosId FROM ApplicationType WHERE Name='NAGIOS';

            INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (appTypeSystemId, (SELECT ActionID FROM Action WHERE Name = "Accept Log Message"));
            INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (appTypeSystemId, (SELECT ActionID FROM Action WHERE Name = "Close Log Message"));
            INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (appTypeSystemId, (SELECT ActionID FROM Action WHERE Name = "Notify Log Message"));
            INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (appTypeSystemId, (SELECT ActionID FROM Action WHERE Name = "Open Log Message"));

            -- Nagios Application Actions
            INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (appTypeNagiosId, (SELECT ActionID FROM Action WHERE Name = "Nagios Acknowledge"));
        END;

        ###############################################################################
        # State Transition Changes
        # 1.  Add new StateTransitionCriteria column to ApplicationType table along with data
        # 2.  Add new StateTransitionHash column and index to LogMessage table
        # 3.  TODO:  Update existing LogMessage with StateTransitionHash value
        ###############################################################################

        # 1. Add new column to ApplicationType table
        ALTER TABLE `GWCollageDB`.`ApplicationType` ADD COLUMN `StateTransitionCriteria` VARCHAR(512) AFTER `Description`;

        UPDATE ApplicationType SET StateTransitionCriteria = 'Device' WHERE Name='SYSTEM';
        UPDATE ApplicationType SET StateTransitionCriteria = 'Device;Host;ServiceDescription' WHERE Name='NAGIOS';
        UPDATE ApplicationType SET StateTransitionCriteria = 'Device;Event_OID_numeric' WHERE Name='SNMPTRAP';
        UPDATE ApplicationType SET StateTransitionCriteria = 'Device' WHERE Name='SYSLOG';

        # 2. Add new column and index to LogMessage table
        ALTER TABLE `GWCollageDB`.`LogMessage` ADD COLUMN `StateTransitionHash` INTEGER AFTER `StatelessHash`;

        ALTER TABLE `GWCollageDB`.`LogMessage` ADD INDEX idx_LogMessage_StateTransitionHash USING BTREE (StateTransitionHash);

        ###############################################################################
        # StateType
        #  New supported
        ###############################################################################
        INSERT INTO StateType (Name, Description) VALUES("UNKNOWN", "State UNKNOWN");

        ##########################################################
        # MonitorStatus
        ##########################################################
        INSERT INTO MonitorStatus (Name, Description) VALUES("MAINTENANCE", "Status MAINTENANCE");

        ###############################################################################
        # UPDATE SCHEMA INFORMATION
        ###############################################################################
        SET currentVersion = '2.0.0';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    IF currentVersion = '2.0.0' THEN
        ####################################################################
        # Change text to varchar since text types are handled as BLOB types
        #####################################################################
        ALTER TABLE `GWCollageDB`.`LogMessage` MODIFY COLUMN `TextMessage` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;
        ALTER TABLE `GWCollageDB`.`HostStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`ServiceStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`LogMessageProperty` MODIFY COLUMN `ValueString` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`EntityProperty` MODIFY COLUMN `ValueString` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`ConsolidationCriteria` MODIFY COLUMN `Criteria` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;
        ALTER TABLE `GWCollageDB`.`MessageFilter` MODIFY COLUMN `RegExpresion` VARCHAR(512) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;

        # The service pack 4 set the schema version to the wrong value 2.0.2. Need to adjust so that the migration works

        SET currentVersion = '2.0.2';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    # Some test servers have the schema set to 2.0.1 . Need to upgrade to be in sync with what was released

    IF currentVersion = '2.0.1' THEN
        SET currentVersion = '2.0.2';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    IF currentVersion = '2.0.2' THEN
        ###############################################################################
        # Changes to Performance Data tables to have nicer more human readable counters
        ################################################################################

        CREATE TABLE PerformanceDataLabel
        (
            PerformanceDataLabelID INTEGER NOT NULL AUTO_INCREMENT,
            PerformanceName VARCHAR(254) DEFAULT "",
            ServiceDisplayName VARCHAR(128) DEFAULT "",
            MetricLabel VARCHAR(128) DEFAULT "",
            Unit VARCHAR(64) DEFAULT "",

            PRIMARY KEY(PerformanceDataLabelID),
            UNIQUE(PerformanceName)
        ) ENGINE = InnoDB;

        ALTER TABLE `GWCollageDB`.`LogPerformanceData`
            ADD COLUMN `PerformanceDataLabelID` INTEGER AFTER `MeasurementPoints`,
            ADD CONSTRAINT `fk_constraint_PERFORMANCE_DATA_LABEL` FOREIGN KEY `fk_constraint_PERFORMANCE_DATA_LABEL` (`PerformanceDataLabelID`)
            REFERENCES `PerformanceDataLabel` (`PerformanceDataLabelID`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT;

        ###############################################
        # Add seed data for the initialset
        ################################################
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('Current Load_load1','Load average last minute','Load factor','load' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('Current Load_load5','Load average last 5 minutes','Load factor','load' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('Current Load_load15','Load average last 15 minutes','Load factor','load' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('Current Users_users','Users on System','users','users' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('Root Partition_/','Used space on Root partition','Percentage used','%' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('icmp_ping_alive_rta','Ping round trip average','rta','ms' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('icmp_ping_alive_pl','Process load','pl','%' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('http_alive_time','Web Server time','time','sec' );
        REPLACE INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) Values('http_alive_size','Web Server size','size','kb' );

        ##################################################################
        # Copy the performance names and the data label id while migration
        ##################################################################
        INSERT INTO PerformanceDataLabel (PerformanceName,ServiceDisplayName)
            (select distinct PerformanceName,PerformanceName from LogPerformanceData where UPPER(PerformanceName) NOT IN (select UPPER(PerformanceName) from PerformanceDataLabel));
        UPDATE PerformanceDataLabel p,LogPerformanceData l SET l.PerformanceDataLabelID=p.PerformanceDataLabelID WHERE UPPER(l.PerformanceName)=UPPER(p.PerformanceName);
        #######################
        # Now Drop the column
        #######################
        ALTER TABLE `GWCollageDB`.`LogPerformanceData` DROP COLUMN `PerformanceName`;

        ###############################################
        # Changes for category implementation
        ###############################################
        CREATE TABLE `GWCollageDB`.`Entity` (
            `EntityID` INT(11) NOT NULL AUTO_INCREMENT,
            `Name` VARCHAR(254) NOT NULL,
            `Description` VARCHAR(254) NOT NULL,
            `Class` VARCHAR(254) NOT NULL,
            `ApplicationTypeID` INT(11) NOT NULL,

            PRIMARY KEY (`EntityID`),

            CONSTRAINT `ApplicationTypeID_ibfk1_1` FOREIGN KEY (`ApplicationTypeID`) REFERENCES `ApplicationType` (`ApplicationTypeID`) ON DELETE CASCADE ON UPDATE RESTRICT
        )
        ENGINE = InnoDB
        COMMENT = 'New Entity Table to support service groups'
        CHARSET=latin1;

        ALTER TABLE `GWCollageDB`.`EntityType` ADD COLUMN `IsApplicationTypeSupported` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsLogicalEntity`;

        ALTER TABLE `GWCollageDB`.`Category`
            ADD COLUMN `EntityTypeID` INT(11) NOT NULL AFTER `Description`,
            ADD CONSTRAINT `EntityTypeID_ibfk1_1` FOREIGN KEY (`EntityTypeID`) REFERENCES `EntityType` (`EntityTypeID`) ON DELETE CASCADE ON UPDATE RESTRICT;
        INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(23,"SERVICE_GROUP","com.groundwork.collage.model.impl.ServiceGroup",1);

        ####################################################
        # GWMON-5720
        ####################################################
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (WARNING)", "ACKNOWLEDGEMENT (WARNING)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (CRITICAL)", "ACKNOWLEDGEMENT (CRITICAL)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (DOWN)", "ACKNOWLEDGEMENT (DOWN)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (UP)", "ACKNOWLEDGEMENT (UP)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (OK)", "ACKNOWLEDGEMENT (OK)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (UNREACHABLE)", "ACKNOWLEDGEMENT (UNREACHABLE)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (UNKNOWN)", "ACKNOWLEDGEMENT (UNKNOWN)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (PENDING)", "ACKNOWLEDGEMENT (PENDING)");
        INSERT INTO MonitorStatus(Name, Description) VALUES ("ACKNOWLEDGEMENT (MAINTENANCE)", "ACKNOWLEDGEMENT (MAINTENANCE)");

        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (WARNING)", "ACKNOWLEDGEMENT (WARNING)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (CRITICAL)", "ACKNOWLEDGEMENT (CRITICAL)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (DOWN)", "ACKNOWLEDGEMENT (DOWN)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (UP)", "ACKNOWLEDGEMENT (UP)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (OK)", "ACKNOWLEDGEMENT (OK)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (UNREACHABLE)", "ACKNOWLEDGEMENT (UNREACHABLE)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (UNKNOWN)", "ACKNOWLEDGEMENT (UNKNOWN)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (PENDING)", "ACKNOWLEDGEMENT (PENDING)");
        INSERT INTO Severity (Name, Description) VALUES( "ACKNOWLEDGEMENT (MAINTENANCE)", "ACKNOWLEDGEMENT (MAINTENANCE)");

        ####################################################
        # Adjust result buffers for plugin output
        ####################################################
        ALTER TABLE `GWCollageDB`.`HostStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`ServiceStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`LogMessageProperty` MODIFY COLUMN `ValueString` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`EntityProperty` MODIFY COLUMN `ValueString` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;

        # Add new Properties for Parent, Alias and RRDPath

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("Parent", "List of parent hosts separated by commas", 1);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("Alias", "Host Alias information", 1);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("RRDPath", "fully qualified path to RRD image", 1);

        # Assign it to HostStatus

        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Parent'),
            23
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Alias'),
            24
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDPath'),
            25
        );

        # Assign it to service Status

        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDPath'),
            56
        );

        SET currentVersion = '2.0.3';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    # Fresh install used the Foundation version which is 2.3.0

    IF currentVersion = '2.0.3' THEN
        SET currentVersion = '2.3.0';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from Release 5.3
    #
    IF currentVersion = '2.3.0' THEN
        ###############################################
        # Added 4 new monitor status
        ###############################################
        INSERT INTO MonitorStatus (Name, Description) VALUES("SCHEDULED DOWN", "Scheduled Down");
        INSERT INTO MonitorStatus (Name, Description) VALUES("UNSCHEDULED DOWN", "UnScheduled Down");
        INSERT INTO MonitorStatus (Name, Description) VALUES("SCHEDULED CRITICAL", "Scheduled Critical");
        INSERT INTO MonitorStatus (Name, Description) VALUES("UNSCHEDULED CRITICAL", "UnScheduled Critical");

        ## New preoperties
        INSERT INTO PropertyType(Name, Description, isLong) VALUES ("CurrentAttempt", "Current attempt running check", 1);
        INSERT INTO PropertyType(Name, Description, isLong) VALUES ("MaxAttempts", "Max attempts configured", 1);
        -- For 6.0.JIRA 76
        INSERT INTO PropertyType(Name, Description, isInteger) VALUES ("isObsessOverHost", "", 1);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("ServiceDependencies", "", 1);

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("ExtendedInfo", "", 1);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("Comments", "", 1);

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("RRDLabel", "Label for Graph", 1);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("RRDCommand", "Custom RRD command", 1);

        #############################################
        # Nagios properties for Host Status
        ##############################################
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentAttempt'),
            26
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'MaxAttempts'),
            27
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExtendedInfo'),
            28
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),
            29
        );

        # Properties Service Status
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentAttempt'),
            57
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'MaxAttempts'),
            58
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExtendedInfo'),
            67
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),
            68
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDLabel'),
            69
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDCommand'),
            70
        );

        # Schema updates to HostStatus table

        ALTER TABLE `GWCollageDB`.`HostStatus`
            ADD COLUMN `StateTypeID` INTEGER DEFAULT 2 AFTER `CheckTypeID`,
            ADD COLUMN `NextCheckTime` DATETIME AFTER `StateTypeID`,
            ADD CONSTRAINT `StateType_ibfk_1` FOREIGN KEY `StateType_ibfk_1` (`StateTypeID`) REFERENCES `StateType` (`StateTypeID`) ON DELETE CASCADE ON UPDATE RESTRICT;

        # Schema updates for HostGroup
        ALTER TABLE `GWCollageDB`.`HostGroup` ADD COLUMN `Alias` VARCHAR(254) default NULL AFTER `ApplicationTypeID`;

        # HostStatus for objects pre 5.3 do not have the Alias property. Add it in order to make sure that the CollageQuery return correct result
        # GWMON-6805
        # The field CreatedOn needs to be set otherwise Hibernate throws an exception. Messages inserted through hibernate will set the field

        INSERT INTO HostStatusProperty(HostStatusID,PropertyTypeID,ValueString,CreatedOn)
            select h.HostID, pt.PropertyTypeID, h.HostName, NOW() from Host h, PropertyType pt
            Where pt.Name='Alias' AND h.HostID NOT IN (
                select h.HostID from Host h, HostStatusProperty hsp, PropertyType pt
                Where pt.Name='Alias' AND pt.PropertyTypeID=hsp.PropertyTypeID AND hsp.HostStatusID = h.HostID
            );

        SET currentVersion = '3.0.0';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from GWMON 6.0to GWMON 6.0.1
    #
    IF currentVersion = '3.0.0' THEN

        # Migration issue with older versions of GroundWork Monitor GWMON-7945

        INSERT INTO MonitorList Select 1,DeviceID from Device where DeviceID not in (select DeviceID from MonitorList);
        INSERT INTO PropertyType(Name, Description, isString) VALUES ("CactiRRDCommand", "Cacti RRD Command", 1);

        SET currentVersion = '3.0.1';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from GWMON 6.0to GWMON 6.1
    #
    IF currentVersion = '3.0.1' THEN

        ALTER TABLE `GWCollageDB`.`HostStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(32768) DEFAULT NULL;

        SET currentVersion = '3.0.2';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from GWMON 6.1 to GWMON 6.2
    #
    IF currentVersion = '3.0.2' THEN

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("RemoteRRDCommand", "Remote RRD Command", 1);
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RemoteRRDCommand'),
            71
        );

        #
        # Notes fields in Configuration are synched to Foundation and need to be bigger

        ALTER TABLE `GWCollageDB`.`Category` MODIFY COLUMN `Description` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`Host` MODIFY COLUMN `Description` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;
        ALTER TABLE `GWCollageDB`.`HostGroup` MODIFY COLUMN `Description` VARCHAR(4096) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL;

        #
        # Service Status has dynamic property for Description

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("Notes", "Configuration Notes field", 1);
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Notes'),
            73
        );
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Notes'),
            72
        );

        # Increase the size for plugin output

        ALTER TABLE `GWCollageDB`.`ServiceStatusProperty` MODIFY COLUMN `ValueString` VARCHAR(16384) DEFAULT NULL;

        SET currentVersion = '3.0.3';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from GWMON 6.2 to GWMON 6.3
    #
    IF currentVersion = '3.0.3' THEN

        #
        # Cloud connector property
        #

        INSERT INTO PropertyType(Name, Description, isString) VALUES ("DeactivationTime", "The time when the host was deactivated",1);
        INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
            (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
            (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
            (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'DeactivationTime'),
            74
        );

        #
        # Delete all Host and service comments so that they are feed into the system with the new format
        #

        DELETE ssp FROM ServiceStatusProperty ssp INNER JOIN PropertyType pt WHERE ssp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND ssp.ValueString like '#%';
        DELETE hsp FROM HostStatusProperty hsp INNER JOIN PropertyType pt WHERE hsp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND hsp.ValueString like '#%';
        # Fix for JIRA 8890
        # First insert temp values
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (199, '199', '199');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (299, '299', '299');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (399, '399', '399');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (499, '499', '499');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (599, '599', '599');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (699, '699', '699');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (799, '799', '799');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (899, '899', '899');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (999, '999', '999');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1099, '1099', '1099');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1199, '1199', '1199');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1299, '1299', '1299');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1399, '1399', '1399');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1499, '1499', '1499');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1599, '1599', '1599');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1699, '1699', '1699');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1799, '1799', '1799');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1899, '1899', '1899');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (1999, '1999', '1999');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (2099, '2099', '2099');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (2199, '2199', '2199');
        INSERT INTO MonitorStatus(MonitorStatusID, Name, Description) VALUES (2299, '2299', '2299');
        # Swap to temp values
        UPDATE ServiceStatus SET MonitorStatusID=299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK');
        UPDATE LogMessage SET MonitorStatusID=299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK');
        #Update DOWN
        UPDATE HostStatus SET MonitorStatusID=2199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='DOWN');
        UPDATE LogMessage SET MonitorStatusID=2199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='DOWN');
        #Update UNREACHABLE
        UPDATE HostStatus SET MonitorStatusID=799 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNREACHABLE');
        UPDATE LogMessage SET MonitorStatusID=799 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNREACHABLE');
        #Update WARNING
        UPDATE ServiceStatus SET MonitorStatusID=999 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='WARNING');
        UPDATE LogMessage SET MonitorStatusID=999 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='WARNING');
        #Update CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=2099 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='CRITICAL');
        UPDATE LogMessage SET MonitorStatusID=2099 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='CRITICAL');
        #Update UNKNOWN
        UPDATE ServiceStatus SET MonitorStatusID=399 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNKNOWN');
        UPDATE LogMessage SET MonitorStatusID=399 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNKNOWN');
        #Update UP
        UPDATE HostStatus SET MonitorStatusID=199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UP');
        UPDATE LogMessage SET MonitorStatusID=199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UP');
        #Update PENDING
        UPDATE ServiceStatus SET MonitorStatusID=599 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='PENDING');
        UPDATE HostStatus SET MonitorStatusID=599 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='PENDING');
        UPDATE LogMessage SET MonitorStatusID=599 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='PENDING');
        #Update MAINTENANCE
        UPDATE HostStatus SET MonitorStatusID=2299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='MAINTENANCE');
        UPDATE LogMessage SET MonitorStatusID=2299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='MAINTENANCE');
        #Update SCHEDULED DOWN
        UPDATE HostStatus SET MonitorStatusID=699 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='SCHEDULED DOWN');
        UPDATE LogMessage SET MonitorStatusID=699 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='SCHEDULED DOWN');
        #Update UNSCHEDULED DOWN
        UPDATE HostStatus SET MonitorStatusID=899 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNSCHEDULED DOWN');
        UPDATE LogMessage SET MonitorStatusID=899 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNSCHEDULED DOWN');
        #Update SCHEDULED CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=499 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='SCHEDULED CRITICAL');
        UPDATE LogMessage SET MonitorStatusID=499 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='SCHEDULED CRITICAL');
        #Update UNSCHEDULED CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=1099 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNSCHEDULED CRITICAL');
        UPDATE LogMessage SET MonitorStatusID=1099 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNSCHEDULED CRITICAL');
        #Update ACKNOWLEDGEMENT WARNING
        UPDATE ServiceStatus SET MonitorStatusID=1199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (WARNING)');
        UPDATE LogMessage SET MonitorStatusID=1199 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (WARNING)');
        #Update ACKNOWLEDGEMENT CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=1299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (CRITICAL)');
        UPDATE LogMessage SET MonitorStatusID=1299 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (CRITICAL)');
        #Update ACKNOWLEDGEMENT DOWN
        UPDATE HostStatus SET MonitorStatusID=1399 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (DOWN)');
        UPDATE LogMessage SET MonitorStatusID=1399 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (DOWN)');
        #Update ACKNOWLEDGEMENT UP
        UPDATE HostStatus SET MonitorStatusID=1499 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UP)');
        UPDATE LogMessage SET MonitorStatusID=1499 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UP)');
        #Update ACKNOWLEDGEMENT OK
        UPDATE ServiceStatus SET MonitorStatusID=1599 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (OK)');
        UPDATE LogMessage SET MonitorStatusID=1599 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (OK)');
        #Update ACKNOWLEDGEMENT REACHABLE
        UPDATE HostStatus SET MonitorStatusID=1699 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNREACHABLE)');
        UPDATE LogMessage SET MonitorStatusID=1699 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNREACHABLE)');
        #Update ACKNOWLEDGEMENTUNKNOWN
        UPDATE ServiceStatus SET MonitorStatusID=1799 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNKNOWN)');
        UPDATE LogMessage SET MonitorStatusID=1799 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNKNOWN)');
        #Update ACKNOWLEDGEMENT PENDING
        UPDATE HostStatus SET MonitorStatusID=1899 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (PENDING)');
        UPDATE ServiceStatus SET MonitorStatusID=1899 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (PENDING)');
        UPDATE LogMessage SET MonitorStatusID=1899 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (PENDING)');
        #Update ACKNOWLEDGEMENT MAINTENANCE
        UPDATE HostStatus SET MonitorStatusID=1999 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (MAINTENANCE)');
        UPDATE LogMessage SET MonitorStatusID=1999 WHERE MonitorStatusID=(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (MAINTENANCE)');
        ALTER TABLE MonitorStatus DROP INDEX Name;

        # Delete the existing entries
        DELETE FROM MonitorStatus WHERE NAME='UP';
        DELETE FROM MonitorStatus WHERE Name='OK';
        DELETE FROM MonitorStatus WHERE Name='UNKNOWN';
        DELETE FROM MonitorStatus WHERE Name='SCHEDULED CRITICAL';
        DELETE FROM MonitorStatus WHERE Name='PENDING';
        DELETE FROM MonitorStatus WHERE Name='SCHEDULED DOWN';
        DELETE FROM MonitorStatus WHERE Name='UNREACHABLE';
        DELETE FROM MonitorStatus WHERE Name='UNSCHEDULED DOWN';
        DELETE FROM MonitorStatus WHERE Name='WARNING';
        DELETE FROM MonitorStatus WHERE Name='UNSCHEDULED CRITICAL';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (WARNING)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (CRITICAL)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (DOWN)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UP)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (OK)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNREACHABLE)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (UNKNOWN)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (PENDING)';
        DELETE FROM MonitorStatus WHERE Name='ACKNOWLEDGEMENT (MAINTENANCE)';
        DELETE FROM MonitorStatus WHERE Name='CRITICAL';
        DELETE FROM MonitorStatus WHERE Name='DOWN';
        DELETE FROM MonitorStatus WHERE Name='MAINTENANCE';

        # Recreate the entries
        REPLACE INTO MonitorStatus (Name, Description, MonitorStatusID) Values
            ('UP', 'UP', 1),
            ('OK', 'OK', 2),
            ('UNKNOWN', 'UNKNOWN', 3),
            ('SCHEDULED CRITICAL', 'SCHEDULED CRITICAL', 4),
            ('PENDING', 'PENDING', 5),
            ('SCHEDULED DOWN', 'SCHEDULED DOWN', 6),
            ('UNREACHABLE', 'UNREACHABLE', 7),
            ('UNSCHEDULED DOWN', 'UNSCHEDULED DOWN', 8),
            ('WARNING', 'WARNING', 9),
            ('UNSCHEDULED CRITICAL', 'UNSCHEDULED CRITICAL', 10),
            ('ACKNOWLEDGEMENT (WARNING)', 'ACKNOWLEDGEMENT (WARNING)', 11),
            ('ACKNOWLEDGEMENT (CRITICAL)', 'ACKNOWLEDGEMENT (CRITICAL)', 12),
            ('ACKNOWLEDGEMENT (DOWN)', 'ACKNOWLEDGEMENT (DOWN)', 13),
            ('ACKNOWLEDGEMENT (UP)', 'ACKNOWLEDGEMENT (UP)', 14),
            ('ACKNOWLEDGEMENT (OK)', 'ACKNOWLEDGEMENT (OK)', 15),
            ('ACKNOWLEDGEMENT (UNREACHABLE)', 'ACKNOWLEDGEMENT (UNREACHABLE)', 16),
            ('ACKNOWLEDGEMENT (UNKNOWN)', 'ACKNOWLEDGEMENT (UNKNOWN)', 17),
            ('ACKNOWLEDGEMENT (PENDING)', 'ACKNOWLEDGEMENT (PENDING)', 18),
            ('ACKNOWLEDGEMENT (MAINTENANCE)', 'ACKNOWLEDGEMENT (MAINTENANCE)', 19),
            ('CRITICAL', 'CRITICAL', 20),
            ('DOWN', 'DOWN', 21),
            ('MAINTENANCE', 'MAINTENANCE', 22);

        ALTER TABLE MonitorStatus ADD UNIQUE KEY (Name);
        # Now swap from temp values
        UPDATE ServiceStatus SET MonitorStatusID=2 WHERE MonitorStatusID=299;
        UPDATE LogMessage SET MonitorStatusID=2 WHERE MonitorStatusID=299;
        #Update DOWN
        UPDATE HostStatus SET MonitorStatusID=21 WHERE MonitorStatusID=2199;
        UPDATE LogMessage SET MonitorStatusID=21 WHERE MonitorStatusID=2199;
        #Update UNREACHABLE
        UPDATE HostStatus SET MonitorStatusID=7 WHERE MonitorStatusID=799;
        UPDATE LogMessage SET MonitorStatusID=7 WHERE MonitorStatusID=799;
        #Update WARNING
        UPDATE ServiceStatus SET MonitorStatusID=9 WHERE MonitorStatusID=999;
        UPDATE LogMessage SET MonitorStatusID=9 WHERE MonitorStatusID=999;
        #Update CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=20 WHERE MonitorStatusID=2099;
        UPDATE LogMessage SET MonitorStatusID=20 WHERE MonitorStatusID=2099;
        #Update UNKNOWN
        UPDATE ServiceStatus SET MonitorStatusID=3 WHERE MonitorStatusID=399;
        UPDATE LogMessage SET MonitorStatusID=3 WHERE MonitorStatusID=399;
        #Update UP
        UPDATE HostStatus SET MonitorStatusID=1 WHERE MonitorStatusID=199;
        UPDATE LogMessage SET MonitorStatusID=1 WHERE MonitorStatusID=199;
        #Update PENDING
        UPDATE ServiceStatus SET MonitorStatusID=5 WHERE MonitorStatusID=599;
        UPDATE HostStatus SET MonitorStatusID=5 WHERE MonitorStatusID=599;
        UPDATE LogMessage SET MonitorStatusID=5 WHERE MonitorStatusID=599;
        #Update MAINTENANCE
        UPDATE HostStatus SET MonitorStatusID=22 WHERE MonitorStatusID=2299;
        UPDATE LogMessage SET MonitorStatusID=22 WHERE MonitorStatusID=2299;
        #Update SCHEDULED DOWN
        UPDATE HostStatus SET MonitorStatusID=6 WHERE MonitorStatusID=699;
        UPDATE LogMessage SET MonitorStatusID=6 WHERE MonitorStatusID=699;
        #Update UNSCHEDULED DOWN
        UPDATE HostStatus SET MonitorStatusID=8 WHERE MonitorStatusID=899;
        UPDATE LogMessage SET MonitorStatusID=8 WHERE MonitorStatusID=899;
        #Update SCHEDULED CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=4 WHERE MonitorStatusID=499;
        UPDATE LogMessage SET MonitorStatusID=4 WHERE MonitorStatusID=499;
        #Update UNSCHEDULED CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=10 WHERE MonitorStatusID=1099;
        UPDATE LogMessage SET MonitorStatusID=10 WHERE MonitorStatusID=1099;
        #Update ACKNOWLEDGEMENT WARNING
        UPDATE ServiceStatus SET MonitorStatusID=11 WHERE MonitorStatusID=1199;
        UPDATE LogMessage SET MonitorStatusID=11 WHERE MonitorStatusID=1199;
        #Update ACKNOWLEDGEMENT CRITICAL
        UPDATE ServiceStatus SET MonitorStatusID=12 WHERE MonitorStatusID=1299;
        UPDATE LogMessage SET MonitorStatusID=12 WHERE MonitorStatusID=1299;
        #Update ACKNOWLEDGEMENT DOWN
        UPDATE HostStatus SET MonitorStatusID=13 WHERE MonitorStatusID=1399;
        UPDATE LogMessage SET MonitorStatusID=13 WHERE MonitorStatusID=1399;
        #Update ACKNOWLEDGEMENT UP
        UPDATE HostStatus SET MonitorStatusID=14 WHERE MonitorStatusID=1499;
        UPDATE LogMessage SET MonitorStatusID=14 WHERE MonitorStatusID=1499;
        #Update ACKNOWLEDGEMENT OK
        UPDATE ServiceStatus SET MonitorStatusID=15 WHERE MonitorStatusID=1599;
        UPDATE LogMessage SET MonitorStatusID=15 WHERE MonitorStatusID=1599;
        #Update ACKNOWLEDGEMENT REACHABLE
        UPDATE HostStatus SET MonitorStatusID=16 WHERE MonitorStatusID=1699;
        UPDATE LogMessage SET MonitorStatusID=16 WHERE MonitorStatusID=1699;
        #Update ACKNOWLEDGEMENTUNKNOWN
        UPDATE ServiceStatus SET MonitorStatusID=17 WHERE MonitorStatusID=1799;
        UPDATE LogMessage SET MonitorStatusID=17 WHERE MonitorStatusID=1799;
        #Update ACKNOWLEDGEMENT PENDING
        UPDATE HostStatus SET MonitorStatusID=18 WHERE MonitorStatusID=1899;
        UPDATE ServiceStatus SET MonitorStatusID=18 WHERE MonitorStatusID=1899;
        UPDATE LogMessage SET MonitorStatusID=18 WHERE MonitorStatusID=1899;
        #Update ACKNOWLEDGEMENT MAINTENANCE
        UPDATE HostStatus SET MonitorStatusID=19 WHERE MonitorStatusID=1999;
        UPDATE LogMessage SET MonitorStatusID=19 WHERE MonitorStatusID=1999;
        # Now Delete the temp values
        DELETE FROM MonitorStatus WHERE MonitorStatusID>=199;
        ### End fix for JIRA 8890

        # We need to drop these two tables in this order, so we drop foreign key references
        # in the one table before we drop the other table that contains the keys.  In a
        # normal upgrade this won't matter, because neither table will already exist.  But
        # in some field-recovery situations where these tables do already exist, this order
        # is critical for successful execution of the migration.  An example might be when
        # a GW6.4 database is overlaid by a copy from an earlier release, and the database
        # must then have this script applied to bring it back up to the current release.
        drop table if exists Plugin;
        drop table if exists PluginPlatform;

        # Plugin tables
        CREATE TABLE PluginPlatform
        (
            PlatformID INTEGER NOT NULL AUTO_INCREMENT,
            Name VARCHAR (128) NOT NULL,
            Arch INTEGER,
            Description VARCHAR (254),

            PRIMARY KEY(PlatformID)
        ) ENGINE=InnoDB;

        # These values mostly don't make sense, and are mostly replaced in later releases.
        # The replacement values aren't supplied by this migration script, though.  A separate
        # script is used to rework the content of the PluginPlatform and Plugin tables in
        # conjunction with moving around any previously uploaded plugin files.
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('AIX', 32, 'AIX 32 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('AIX', 64, 'AIX 64 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Linux', 32, 'Linux 32 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Linux', 64, 'Linux 64 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Multiplatform', 32, 'Multiplatform 32 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Multiplatform', 64, 'Multiplatform 64 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris', 32, 'Solaris 32 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris', 64, 'Solaris 64 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Windows', 32, 'Windows 32 bit');
        INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Windows', 64, 'Windows 64 bit');

        CREATE TABLE Plugin
        (
            PluginID INTEGER NOT NULL AUTO_INCREMENT,
            Name VARCHAR (128) NOT NULL,
            Url VARCHAR (254),
            PlatformID INTEGER NOT NULL,
            Dependencies VARCHAR (254),
            LastUpdateDate TIMESTAMP NOT NULL,
            Checksum VARCHAR(254) NOT NULL,
            LastUpdatedBy VARCHAR(254),

            PRIMARY KEY(PluginID),
            UNIQUE(Name),
            FOREIGN KEY (PlatformID) REFERENCES PluginPlatform (PlatformID) ON DELETE CASCADE
        ) ENGINE=InnoDB;

        SET currentVersion = '3.0.4';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;

    #
    # Upgrade from GWMON 6.4 to GWMON 6.4.1
    #
    IF currentVersion = '3.0.4' THEN

        # Changes needed to fully support GDMA plugin uploads (GDMA-272).

        # Ensure that our plugin platforms are uniquely stored in the database.

        IF EXISTS (
            SELECT NULL FROM information_schema.columns
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'PluginPlatform' AND COLUMN_NAME = 'Arch' AND IS_NULLABLE = 'YES'
        ) THEN
            ALTER TABLE PluginPlatform CHANGE Arch Arch INTEGER NOT NULL;
        END IF;
		
		IF EXISTS (
            SELECT NULL FROM information_schema.columns
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'PluginPlatform' AND COLUMN_NAME = 'Arch' AND IS_NULLABLE = 'YES'
        ) THEN
	        IF NOT EXISTS (
	            SELECT NULL FROM information_schema.statistics
	                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'PluginPlatform' AND INDEX_NAME = 'idx_PluginPlatform_Name_Arch'
	        ) THEN
	            ALTER TABLE PluginPlatform ADD UNIQUE INDEX idx_PluginPlatform_Name_Arch USING BTREE (Name, Arch);
	        END IF;
		END IF;

        # We thought we would add a unique (Name, PlatformID) index to the Plugin table, but it
        # turns out that because PlatformID is a foreign key reference, and perhaps also because
        # of the ON DELETE CASCADE clause applied to that reference, it must be the first element
        # in some index on this table.  So we punt and reverse the order of fields in this index.
        # Hopefully the database will figure out that it can still use this index efficiently when
        # it needs to.  We shouldn't need a Name-only index because we should only be accessing
        # plugins in conjunction with specific platforms, so we drop that index.  And the first
        # element in the idx_Plugin_PlatformID_Name index can be used to replace the single-element
        # PlatformID index that originally supported the foreign key reference, so we drop that too.
        #
        # The need to have at least one index with PlatformID as the first element means we must
        # add the new idx_Plugin_PlatformID_Name index before we drop the old PlatformID index.

        # We test to see if these indexes already exist before we change them, to provide some
        # measure of safe idempotency to this portion of the script.
		
        IF EXISTS (
            SELECT NULL FROM information_schema.statistics
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'Name'
        ) THEN
	        IF NOT EXISTS (
	            SELECT NULL FROM information_schema.statistics
	                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'idx_Plugin_PlatformID_Name'
	        ) THEN
	            ALTER TABLE Plugin ADD UNIQUE INDEX idx_Plugin_PlatformID_Name USING BTREE (PlatformID, Name);
	        END IF;
	  	 END IF;

        IF EXISTS (
            SELECT NULL FROM information_schema.statistics
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'Name'
        ) THEN
            ALTER TABLE Plugin DROP INDEX Name;
        END IF;

        # MySQL implicitly drops the PlatformID index when it creates the idx_Plugin_PlatformID_Name
        # index above, if the PlatformID index was actually just a non-unique KEY CONSTRAINT instead
        # of a true index.  And if we attempt to drop an index that doesn't exist, the script will
        # die at this point.  So to be safe and cover all bases, not just complete script idempotency
        # as with the other indexes, we need to check if the PlatformID index exists at this point
        # before attempting to drop it.

        IF EXISTS (
            SELECT NULL FROM information_schema.statistics
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'PlatformID'
        ) THEN
            ALTER TABLE Plugin DROP INDEX PlatformID;
        END IF;

        # Rename a plugin-support column to properly reflect its actual content.
        # Again, we wrap this in a test to provide idempotency.

        IF EXISTS (
            SELECT NULL FROM information_schema.columns
                WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND COLUMN_NAME = 'LastUpdateDate'
        ) THEN
            ALTER TABLE Plugin CHANGE LastUpdateDate LastUpdateTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        END IF;

        # Other GWCollageDB changes (involving table content) are also needed to support GDMA
        # plugin downloads, but they are handled in a separate script (upgrade_to_gdma_222.pl)
        # because we need to coordinate changes in table content with corresponding changes in
        # the filesystem.  And we cannot do that from within an SQL procedure.

        SET currentVersion = '3.0.5';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;
	
	#
    # Upgrade from GWMON 6.4 to GWMON 6.4.1
    #
    IF currentVersion = '3.0.5' AND temp_65_feature is null THEN
		# New dynamic properties for SYSLOG and SNMPTRAP
        REPLACE INTO PropertyType(Name, Description, isString)  VALUES ("UpdatedBy", "UpdatedBy", 1);
		REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 7);
		REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SNMPTRAP'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 8);
		REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'UpdatedBy'), 4);
		REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSLOG'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 5);
		
		SET currentVersion = '3.0.6';
        Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
    END IF;
	
    # Commit All Changes
    #
    # Note that partial changes have probably already been automatically committed earlier
    # in this script, because ALTER TABLE, CREATE INDEX, DROP INDEX, DROP TABLE, RENAME
    # TABLE, and certain other statements implicitly end the current transaction.  See
    # http://dev.mysql.com/doc/refman/5.0/en/implicit-commit.html for details.  Also, the
    # default behavior of MySQL is to autocommit every database update, so unless we called
    # "set autocommit = 0;" earlier in this script, every change is being individually and
    # immediately committed anyway.
    #
    # Note that because of those implicit commits, much of this script in its current form
    # cannot be executed again if it should fail partway through.  On a second pass, various
    # ALTER TABLE commands would fail because the change would already be in place, and that
    # would prevent the script from completing.  If we truly want to make execution of this
    # script idempotent, then every potential change which is not implicitly idempotent must
    # be surrounded by logic to bypass the attempt if the modification is already in place.
    #
    COMMIT;
    # After commit, optimize the 3 tables since it is necessary for any 6.3 migration due to massive updates to the tables.
    OPTIMIZE TABLE LogMessage;
    OPTIMIZE TABLE HostStatus;
    OPTIMIZE TABLE ServiceStatus;

END$$

delimiter ;

# Execute Migration Stored Procedure
call sp_Migrate();

# Remove Stored Procedure
DROP PROCEDURE IF EXISTS `GWCollageDB`.`sp_Migrate`;

# Output the new version the database
SELECT concat('GWCollageDB Updated To Version: ', Value) AS UpdateCompleted FROM SchemaInfo WHERE Name = 'CurrentSchemaVersion';

