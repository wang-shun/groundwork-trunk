# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork").
# All rights reserved.  Use is subject to commercial license terms.

################################################################################
# AlertSite Database File
# This script creates the web monitoring database (alertsite)
# along with the necessary user and access privileges.
################################################################################

# We need to break up the actions here into several procedures
# because "use {database};" is not allowed within a procedure.

# We want this script to operate idempotently, partly so as not to wipe
# out the database content during an upgrade.  That means we cannot drop
# the database nor drop individual tables in this script.  If we ever
# need to do so (e.g., if the database or table schema changes in some
# new release), that will need to be handled by a separate script.

# ================================================================

use mysql;

delimiter $$

DROP PROCEDURE IF EXISTS `mysql`.`Create_alertsite_Database`$$
CREATE PROCEDURE `mysql`.`Create_alertsite_Database` ()

BEGIN

    --
    -- Create the `alertsite` database
    --

    # Begin Transaction
    START TRANSACTION;

    -- DROP DATABASE IF EXISTS `alertsite`;
    CREATE DATABASE IF NOT EXISTS `alertsite`;

    --
    -- Grant access to the alertsite database.
    --

    IF NOT EXISTS (
        select NULL from mysql.user WHERE User='webmonitor'
    ) THEN
        SELECT 'Adding the webmonitor user to the database.';
        CREATE USER webmonitor@localhost IDENTIFIED BY 'gwrk';
    END IF;

    # FIX LATER:  we could/should probably restrict these privileges a bit,
    # to remove those privileges which won't actually be necessary
    GRANT ALL PRIVILEGES ON alertsite.* TO webmonitor@localhost IDENTIFIED BY 'gwrk';

    ###############################################
    # Put all the access changes above into play.
    ###############################################
    flush privileges;

    SELECT 'Committing the creation of the alertsite database.';
    # Commit All Changes
    #
    # Note that the default behavior of MySQL is to autocommit every database update, so
    # unless we called "set autocommit = 0;" earlier in this script, every change is being
    # individually and immediately committed anyway.   (FIX LATER:  check to see if the
    # "START TRANSACTION" at the top effectively does this for us.)
    #
    COMMIT;

END$$

delimiter ;

# Execute Stored Procedure
call Create_alertsite_Database();

# Remove Stored Procedure
DROP PROCEDURE IF EXISTS `mysql`.`Create_alertsite_Database`;

# ================================================================

use `alertsite`;

delimiter $$

DROP PROCEDURE IF EXISTS `alertsite`.`Populate_alertsite_Database`$$
CREATE PROCEDURE `alertsite`.`Populate_alertsite_Database` ()

BEGIN

    --
    -- Table structure for table `last_access`
    --

    -- DROP TABLE IF EXISTS `last_access`;
    SET @saved_cs_client     = @@character_set_client;
    SET character_set_client = utf8;
    CREATE TABLE IF NOT EXISTS `last_access` (
      `device_id` varchar(50) NOT NULL default '',
      `last_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
      PRIMARY KEY  (`device_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
    SET character_set_client = @saved_cs_client;

    SELECT 'Committing all alertsite changes.';
    # Commit All Changes
    #
    # Note that the default behavior of MySQL is to autocommit every database update, so
    # unless we called "set autocommit = 0;" earlier in this script, every change is being
    # individually and immediately committed anyway.   (FIX LATER:  check to see if the
    # "START TRANSACTION" at the top effectively does this for us.)
    #
    COMMIT;

END$$

delimiter ;

# Execute Stored Procedure
call Populate_alertsite_Database();

# Remove Stored Procedure
DROP PROCEDURE IF EXISTS `alertsite`.`Populate_alertsite_Database`;

# ================================================================

# Output a simple completion message.
SELECT 'The alertsite database has been created.';

