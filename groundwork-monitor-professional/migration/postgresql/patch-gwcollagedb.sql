# $Id: $
#
# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork")
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
# Foundation Database Patch Script
# This script should be used only for customers who skipped 6.3 upgrade and jumped to 6.4 or 6.5 from 6.2
# When should you run this patch? : After 6.4 upgrade, when upgrading to 6.4. And before 6.5 upgrade if upgrading to 6.5.
# CAUTION: PLEASE BACKUP YOUR GWCOLLAGEDB BEFORE APPLYING THIS PATCH.
################################################################################
delimiter $$

DROP PROCEDURE IF EXISTS `GWCollageDB`.`sp_Patch`$$
CREATE PROCEDURE `GWCollageDB`.`sp_Patch` ()

BEGIN

    DECLARE currentVersion varchar(16);
    DECLARE constraintName varchar(64);
    DECLARE temp_65_feature varchar(16);
    DECLARE temp_63_feature varchar(16);
    DECLARE lastSortOrder int(11);

    # Begin Transaction
    START TRANSACTION; 
 
    # Query current version of the schema
    SELECT VALUE INTO currentVersion FROM SchemaInfo WHERE Name = 'CurrentSchemaVersion';
    SELECT count(*) INTO temp_65_feature FROM PropertyType WHERE Name='UpdatedBy';
    SELECT TABLE_NAME INTO temp_63_feature FROM information_schema.tables where TABLE_NAME='PluginPlatform' and TABLE_SCHEMA='GWCollageDB';
   
    IF temp_63_feature is null THEN
	    ########################
	    # Cloud connector property
	    ########################
		SELECT max(SortOrder) +1 INTO lastSortOrder FROM ApplicationEntityProperty ;
	    REPLACE INTO PropertyType(Name, Description, isString) VALUES ("DeactivationTime", "The time when the host was deactivated",1);
	    REPLACE INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
	        (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
	        (SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
	        (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'DeactivationTime'), lastSortOrder );

        ########################################################################
        # Delete all Host and service comments so that they are feed into the system with the new format
        ########################################################################
		 IF EXISTS (
            SELECT NULL FROM ServiceStatusProperty ssp INNER JOIN PropertyType pt
                WHERE ssp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND ssp.ValueString like '#%'
        ) THEN
        	DELETE ssp FROM ServiceStatusProperty ssp INNER JOIN PropertyType pt WHERE ssp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND ssp.ValueString like '#%';
        END IF;
        
        IF EXISTS (
            SELECT NULL FROM HostStatusProperty hsp INNER JOIN PropertyType pt 
            	WHERE hsp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND hsp.ValueString like '#%'
        ) THEN
        	DELETE hsp FROM HostStatusProperty hsp INNER JOIN PropertyType pt WHERE hsp.PropertyTypeID=pt.PropertyTypeID AND pt.Name='Comments' AND hsp.ValueString like '#%';
        END IF;
        
        ##########################################################
        # Fix for JIRA 8890(Host list not sorting troubled services to the top in all cases)
        ##########################################################
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
       
        SET @@foreign_key_checks = 0;

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
        SET @@foreign_key_checks = 1;
        ### End fix for JIRA 8890

        IF temp_63_feature is null THEN
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
	        # The replacement values arent supplied by this migration script, though.  A separate
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
		END IF;
        
        IF temp_65_feature <> null THEN
	     	# Changes needed to fully support GDMA plugin uploads (GDMA-272).
	    	# Ensure that our plugin platforms are uniquely stored in the database.
	    	# These changes are for 6.5 customers only.
	
	    	IF EXISTS (
	        	SELECT NULL FROM information_schema.columns
	            	WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'PluginPlatform' AND COLUMN_NAME = 'Arch' AND IS_NULLABLE = 'YES'
	    	) THEN
	        	ALTER TABLE PluginPlatform CHANGE Arch Arch INTEGER NOT NULL;
	    	END IF;
	
		    IF NOT EXISTS (
		        SELECT NULL FROM information_schema.statistics
		            WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'PluginPlatform' AND INDEX_NAME = 'idx_PluginPlatform_Name_Arch'
		    ) THEN
		        ALTER TABLE PluginPlatform ADD UNIQUE INDEX idx_PluginPlatform_Name_Arch USING BTREE (Name, Arch);
		    END IF;
	
		    # We thought we would add a unique (Name, PlatformID) index to the Plugin table, but it
		    # turns out that because PlatformID is a foreign key reference, and perhaps also because
		    # of the ON DELETE CASCADE clause applied to that reference, it must be the first element
		    # in some index on this table.  So we punt and reverse the order of fields in this index.
		    # Hopefully the database will figure out that it can still use this index efficiently when
		    # it needs to.  We shouldnt need a Name-only index because we should only be accessing
		    # plugins in conjunction with specific platforms, so we drop that index.  And the first
		    # element in the idx_Plugin_PlatformID_Name index can be used to replace the single-element
		    # PlatformID index that originally supported the foreign key reference, so we drop that too.
		    #
		    # The need to have at least one index with PlatformID as the first element means we must
		    # add the new idx_Plugin_PlatformID_Name index before we drop the old PlatformID index.
		
		    # We test to see if these indexes already exist before we change them, to provide some
		    # measure of safe idempotency to this portion of the script.
	
		    IF NOT EXISTS (
		        SELECT NULL FROM information_schema.statistics
		            WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'idx_Plugin_PlatformID_Name'
		    ) THEN
		        ALTER TABLE Plugin ADD UNIQUE INDEX idx_Plugin_PlatformID_Name USING BTREE (PlatformID, Name);
		    END IF;
		
		    IF EXISTS (
		        SELECT NULL FROM information_schema.statistics
		            WHERE TABLE_SCHEMA = 'GWCollageDB' AND TABLE_NAME = 'Plugin' AND INDEX_NAME = 'Name'
		    ) THEN
		        ALTER TABLE Plugin DROP INDEX Name;
		    END IF;
		
		    # MySQL implicitly drops the PlatformID index when it creates the idx_Plugin_PlatformID_Name
		    # index above, if the PlatformID index was actually just a non-unique KEY CONSTRAINT instead
		    # of a true index.  And if we attempt to drop an index that doesnt exist, the script will
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
      	
		END IF;
      	IF temp_65_feature <> null THEN
      		SET currentVersion = '3.0.6';
      	ELSE
      		SET currentVersion = '3.0.5';
      	END IF;
      	IF EXISTS (
        	SELECT NULL FROM SchemaInfo where Name = 'CurrentSchemaVersion'
         ) THEN
        	Update SchemaInfo set Value=currentVersion WHERE Name = 'CurrentSchemaVersion';
        	Update SchemaInfo set Value=Now() WHERE Name = 'SchemaUpdated';
        ELSE
        	INSERT INTO SchemaInfo (Name, Value) VALUES ('CurrentSchemaVersion', currentVersion);
			INSERT INTO SchemaInfo (Name, Value) VALUES ('SchemaUpdated', CAST(NOW() AS CHAR));       	
        END IF;
    ELSE
    	SELECT concat('Nothing to update! Will just optimize the tables! Version: ', Value) AS UpdateCompleted FROM SchemaInfo WHERE Name = 'CurrentSchemaVersion';
        
	END IF;                
COMMIT;
    
# After commit, optimize the 3 tables.
OPTIMIZE TABLE LogMessage;
OPTIMIZE TABLE HostStatus;
OPTIMIZE TABLE ServiceStatus;

END$$

delimiter ;

# Execute Migration Stored Procedure
call sp_Patch();

# Remove Stored Procedure
DROP PROCEDURE IF EXISTS `GWCollageDB`.`sp_Patch`;

# Output the new version the database
SELECT concat('GWCollageDB Updated To Version: ', Value) AS UpdateCompleted FROM SchemaInfo WHERE Name = 'CurrentSchemaVersion';
