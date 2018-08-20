# MonArch - Groundwork Monitor Architect
# cacti.import_schema.sql
#
############################################################################
# Release 3.5
# January 2011
############################################################################
#
# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

# This script is designed to safely add all the components of the "Cacti Host Profile Sync"
# automation schema to the monarch database, without depending on any preconceived notion
# of what object id's already exist in the database.

delimiter $$

DROP PROCEDURE IF EXISTS `monarch`.`cacti_migration`$$
CREATE PROCEDURE `monarch`.`cacti_migration` ()

BEGIN   
    SET @failure = '';

    changes: begin

	DECLARE cacti_schema_id       int unsigned;
	DECLARE cacti_hostprofile_id  int unsigned;
	DECLARE description_column_id int unsigned;
	DECLARE name_column_id        int unsigned;
	DECLARE address_column_id     int unsigned;

	# Begin Transaction
	START TRANSACTION;

	select schema_id into cacti_schema_id from import_schema where name = 'Cacti Host Profile Sync';
	IF cacti_schema_id is not null THEN
	    SET @failure = "NOTICE:  The \"Cacti Host Profile Sync\" automation schema already exists and is not being touched.";
	    LEAVE changes;
	END IF;
	select hostprofile_id into cacti_hostprofile_id from profiles_host where name = 'host-profile-cacti-host';
	IF cacti_hostprofile_id is null THEN
	    SET @failure = 'ERROR:  Cannot find host profile "host-profile-cacti-host".';
	    LEAVE changes;
	END IF;
	INSERT INTO `import_schema` VALUES (
	    '',
	    'Cacti Host Profile Sync',
	    ';;',
	    'This automation schema is designed to import Cacti data.  It applies a host profile named host-profile-cacti-host, which is part of the GroundWork Monitor release. Data to be synchronized is stored in cacti_data.txt. You can extract this data from Cacti using extract_cacti.pl. Note: this schema deletes hosts with the selected host profile that are not in the input data, to keep this portion of the Monarch configuration synchronized with the Cacti configuration.',
	    'host-profile-sync',
	    NULL,
	    1,
	    cacti_hostprofile_id,
	    '/usr/local/groundwork/core/monarch/automation/data/cacti_data.txt'
	);
	select schema_id into cacti_schema_id from import_schema where name = 'Cacti Host Profile Sync';
	IF cacti_schema_id is null THEN
	    SET @failure = 'ERROR:  Cannot find import schema "Cacti Host Profile Sync" after insert.';
	    LEAVE changes;
	END IF;

	INSERT INTO `import_column` VALUES ('',cacti_schema_id,'Description',4,NULL);
	INSERT INTO `import_column` VALUES ('',cacti_schema_id,'Name',2,NULL);
	INSERT INTO `import_column` VALUES ('',cacti_schema_id,'Address',1,NULL);

	select column_id into description_column_id from import_column where schema_id = cacti_schema_id and name = 'Description';
	select column_id into name_column_id        from import_column where schema_id = cacti_schema_id and name = 'Name';
	select column_id into address_column_id     from import_column where schema_id = cacti_schema_id and name = 'Address';

	IF description_column_id is null OR name_column_id is null OR address_column_id is null THEN
	    SET @failure = 'ERROR:  Cannot find either Description, Name, or Address import column after insertion.';
	    LEAVE changes;
	END IF;
	INSERT INTO `import_match` VALUES (
	    '',description_column_id,'Assign to description',1,'use-value-as-is',NULL,'Assign value to','Description',NULL,NULL,NULL
	);
	INSERT INTO `import_match` VALUES (
	    '',name_column_id,'Discard comments',1,'begins-with','#','Discard record',NULL,NULL,NULL,NULL
	);
	INSERT INTO `import_match` VALUES (
	    '',name_column_id,'Assign name',2,'use-value-as-is',NULL,'Assign value to','Name',NULL,NULL,NULL
	);
	INSERT INTO `import_match` VALUES (
	    '',address_column_id,'Discard comments',2,'begins-with','#','Discard record',NULL,NULL,NULL,NULL
	);
	INSERT INTO `import_match` VALUES (
	    '',address_column_id,'Set address',1,'use-value-as-is',NULL,'Assign value to','Address',NULL,NULL,NULL
	);
	INSERT INTO `import_match` VALUES (
	    '',address_column_id,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL
	);

    end changes;

    # Commit All Changes
    IF @failure != '' THEN
	ROLLBACK NO RELEASE;
	select @failure as '';
    ELSE
	COMMIT NO RELEASE;
	select 'Success:  The "Cacti Host Profile Sync" automation schema has been added.' as '';
    END IF;

END$$

delimiter ;

# Execute Migration Stored Procedure
call cacti_migration();

# Remove Stored Procedure
DROP PROCEDURE IF EXISTS `monarch`.`cacti_migration`;

