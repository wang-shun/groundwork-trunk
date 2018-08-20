
--
-- Copyright 2011-2018 GroundWork, Inc. ("GroundWork")
-- All rights reserved.
--

-- Stop immediately and emit a non-zero exit code if anything in this script fails.  That allows
-- the calling context (generally the BitRock installer) to understand that a failure has happened,
-- so it can notify the user immediately, not just bury the error output in a log file.  The
-- "stop immediately" aspect is no worse than if we didn't do so, because this script operates by
-- embedding all its actions in functions.  When the script calls the fn_migrate_gwcollagedb()
-- function as its only action, that one statement will automatically be embedded in a transaction.
-- If something within the function fails, the entire transaction will be rolled back, regardless
-- of whether or not we have ON_ERROR_STOP in play.  The only difference is that the cleanup code
-- that follows calling the function won't be run, but that is of little consequence since it will
-- eventually be cleaned up when the underlying problem is addressed and this script is run again.
--
\set ON_ERROR_STOP

set client_min_messages='warning';

-- Given that the GWMEE 7.1.1 release is restricted to upgrading from the 7.1.0 release, where
-- similar hostnames are already blocked by table constraint, the utility of running this
-- uniqueify_host_hostnames() function is now gone.  It never did a complete job of dealing with
-- similar hosts (it dies if they have duplicate services, and it doesn't handle servicegroup
-- re-assignment, for instance).  It has now been superseded by the show-similar-hosts.sql
-- and merge-similar-hosts.sql scripts that must be run outside of the upgrade process just
-- before the upgrade is run, when transitioning from GWMEE 7.0.2-SP02 or earlier to 7.0.2-SP03
-- or later.  Once that boundary has been crossed, this function won't find any more similar
-- hostnames, so it's effectively a big no-op.
--
-- The only situation in which this might still come into play is if a site runs an upgrade to
-- 7.1.0, fails to notice that this script silently failed during that upgrade (and thus did not
-- upgrade the gwcollagedb database at all), and then runs an upgrade to the 7.1.1 release.  In
-- that case, the function will either do all the imperfect cleanup it is capable of, or fail
-- because it cannot deal with duplicate services on similar hosts.  And this time, if it fails,
-- the preceding setting of ON_ERROR_STOP will cause the failure to be visibly flagged by the
-- installer.  In that case, it *might* be possible to recover without rolling back by shutting
-- down gwservices, running the show/merge scripts just described, then re-running this script,
-- and finally running the conflicting_archive_service_rows.pl script to make any necessary
-- corresponding changes in the archive_gwcollagedb database.  But that scenario has not been
-- tested, so there are no guarantees.
--
CREATE OR REPLACE FUNCTION uniqueify_host_hostnames(canonical_upper_case boolean) RETURNS integer AS $$
DECLARE
    scan_host RECORD;
    scan_skip integer;
    uniqueify_count integer;
    host_count integer;
    scan_duplicate_host RECORD;
    canonical_duplicate_host_hostname varchar;
    canonical_duplicate_host_hostid integer;
    scan_duplicate_host_group_collection RECORD;
BEGIN
    scan_skip := 0;
    uniqueify_count := 0;
    -- To make the scan_skip logic here work, we depend in this top-level FOR loop on the default collation of the host.hostname field
    -- selecting similar hosts in adjacent returned rows.  In fact, the default collation seems to be an initial case-insensitive sort
    -- followed by a case-sensitive sort, ordering lowercase-first, if the first-level sort finds case-insensitive duplicate values.
    FOR scan_host IN SELECT hostname FROM host ORDER BY hostname LOOP
        IF scan_skip > 0 THEN
           scan_skip := scan_skip - 1;
           CONTINUE;
        END IF;
        SELECT count(*) FROM host WHERE lower(hostname) = lower(scan_host.hostname) INTO host_count;
        IF host_count > 1 THEN
            RAISE INFO 'Duplicate host hostname: %, (count = %)', scan_host.hostname, host_count;
            FOR scan_duplicate_host IN SELECT hostid, hostname FROM host WHERE lower(hostname) = lower(scan_host.hostname) ORDER BY hostname COLLATE "C" LOOP
                canonical_duplicate_host_hostname := scan_duplicate_host.hostname;
                canonical_duplicate_host_hostid := scan_duplicate_host.hostid;
                IF canonical_upper_case THEN
                   EXIT;
                END IF;
            END LOOP;
            RAISE INFO 'Canonical duplicate host hostname: %, (id = %)', canonical_duplicate_host_hostname, canonical_duplicate_host_hostid;
            FOR scan_duplicate_host IN SELECT hostid, hostname FROM host WHERE lower(hostname) = lower(scan_host.hostname) LOOP
                IF canonical_duplicate_host_hostname != scan_duplicate_host.hostname OR canonical_duplicate_host_hostid != scan_duplicate_host.hostid THEN
                    RAISE INFO 'Merge duplicate host hostname: %, (id = %) -> %, (id = %)',
                        scan_duplicate_host.hostname, scan_duplicate_host.hostid, canonical_duplicate_host_hostname, canonical_duplicate_host_hostid;
                    FOR scan_duplicate_host_group_collection IN SELECT hostgroupid FROM hostgroupcollection WHERE hostid = scan_duplicate_host.hostid LOOP
                        PERFORM * FROM hostgroupcollection WHERE hostgroupid = scan_duplicate_host_group_collection.hostgroupid AND hostid = canonical_duplicate_host_hostid;
                        IF NOT FOUND THEN
                            UPDATE hostgroupcollection SET hostid = canonical_duplicate_host_hostid
                                WHERE hostgroupid = scan_duplicate_host_group_collection.hostgroupid AND hostid = scan_duplicate_host.hostid;
                        END IF;
                    END LOOP;
                    UPDATE logmessage SET hoststatusid = canonical_duplicate_host_hostid WHERE hoststatusid = scan_duplicate_host.hostid;

                    -- FIX MAJOR:  Updating the servicestatus table with the following "UPDATE servicestatus" command won't work
                    -- if the canonical_duplicate_host_hostid already has a service with exactly the same name as a service on the
                    -- scan_duplicate_host.hostid.  That's because the servicestatus table has a UNIQUE CONSTRAINT on (hostid,
                    -- servicedescription).  So such rows will not be updated, and presumably the entire UPDATE command will fail
                    -- rather than just the rows with collisions, which means that none of the services from the duplicate host
                    -- will be transferred to the canonical host.  The servicestatus.hostid field has a FOREIGN KEY reference to
                    -- the host(hostid) field with an ON DELETE CASCADE clause, which means that when we attempt to delete the
                    -- scan_duplicate_host.hostid using the subsequent "DELETE FROM host" command, all remaining services on that
                    -- host will just disappear.  Which will mean that none of them got transnferred.
                    --
                    -- See https://kb.groundworkopensource.com/display/SUPPORT/GWME-7.1.0-1+-+Removing+duplicate+host+entries+in+gwcollagedb+database+prior+to+upgrade+from+7.0.2.2
                    -- for more information on related failures.  See also GWMON-12039 and GWMON-12355.
                    --
                    UPDATE servicestatus SET hostid = canonical_duplicate_host_hostid WHERE hostid = scan_duplicate_host.hostid;

                    RAISE INFO 'Delete duplicate host hostname: %, (id = %)', scan_duplicate_host.hostname, scan_duplicate_host.hostid;
                    DELETE FROM host WHERE hostid = scan_duplicate_host.hostid;
                    IF NOT FOUND THEN
                        RAISE EXCEPTION 'Unable to delete duplicate host: %, (id = %)', scan_duplicate_host.hostname, scan_duplicate_host.hostid;
                    END IF;
                END IF;
            END LOOP;
            uniqueify_count := uniqueify_count + 1;
            scan_skip := host_count - 1;
        END IF;
    END LOOP;
    RETURN uniqueify_count;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION fn_migrate_gwcollagedb() RETURNS VOID AS $$
DECLARE
	currentSchemaVersion varchar;
	schemaUpdated varchar;
	-- Following variables are for 7.0.2 only. Define variables for each version if need. Furture versions can 
	-- reuse this however should not modify. If modified, then sequential upgrade might get corrupted.
	appTypes_702 varchar[] := ARRAY['VEMA','GDMA','NOMA','CHRHEV','ARCHIVE','BSM','SNMPTRAP','SYSLOG','SEL','AUDIT','CACTI'];
	appType_702 varchar;
	proptypeid_702 varchar;
	sortOrder_702 integer;
	uniqueify_count integer;
BEGIN
	SELECT value INTO currentSchemaVersion FROM schemainfo WHERE name = 'CurrentSchemaVersion';
	-- Here you are upgrading from 6.6.0 to 6.6.1. So just increment the version number.
	IF (currentSchemaVersion is NULL) THEN
		currentSchemaVersion = '6.6.1';
		INSERT INTO schemainfo (name, value) VALUES ('CurrentSchemaVersion',currentSchemaVersion);
		INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
	END IF;

	IF (currentSchemaVersion = '6.6.1') THEN
		-- GDMA Auto Register stuff
		INSERT INTO ApplicationType(Name, Description, StateTransitionCriteria) VALUES ('GDMA', 'System monitored by GDMA', 'Device;Host;ServiceDescription');
		INSERT INTO Action (ActionTypeID,Name,Description) VALUES(
			(SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),
			'Register Agent',
			'Invoke a script for the selected message'
		);
		INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
			(SELECT ActionID FROM Action WHERE Name = 'Register Agent'),
			'Script',
			'/usr/local/groundwork/foundation/scripts/registerAgent.pl'
		);
		INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'),
			(SELECT ActionID FROM Action WHERE Name = 'Register Agent')
		);
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'agent-type','agent-type');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-name','host-name');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-ip','host-ip');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-mac','host-mac');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'operating-system','operating-system');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-characteristic','host-characteristic');

		INSERT INTO Action (ActionTypeID,Name,Description) VALUES(
			(SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),'Register Agent by Profile','Invoke a script for the selected message'
		);
		INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
			(SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'), 'Script', '/usr/local/groundwork/foundation/scripts/registerAgentByProfile.pl'
		);
		INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'), (SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile')
		);
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'agent-type','agent-type');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-name','host-name');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-ip','host-ip');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-mac','host-mac');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'operating-system','operating-system');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-profile-name','host-profile-name');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'service-profile-name','service-profile-name');

		-- New property for GWMEE 6.7

		INSERT INTO PropertyType(Name, Description, isString)  VALUES ('PerformanceData', 'The last Nagios performance data',true);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),
			75
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),
			76
		);

		-- New Application type for VEMA

		INSERT INTO ApplicationType(ApplicationTypeID, Name, Description, StateTransitionCriteria)
		VALUES (200,'VEMA', 'Virtual Environment Monitor Agent', 'Device;Host;ServiceDescription');

		-- Monitor Status for virtual environments

		INSERT INTO MonitorStatus(Name, Description)  VALUES ('SUSPENDED', 'Virtual Environment specific Host status');

		-- Properties for Cloud Hub Agents

		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			82
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'VEMA'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),
			80
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),
			81
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),
			83
		);

		-- NoMa Notification Stuff
		INSERT INTO ApplicationType(Name, Description, StateTransitionCriteria) VALUES (
			'NOMA',
			'NoMa Notification',
			'Device;Host;ServiceDescription'
		);
		INSERT INTO Action (ActionTypeID,Name,Description) VALUES(
			(SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),
			'Noma Notify For Host',
			'Invoke a script for the selected message'
		);
		INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
			(SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'),
			'Script',
			'/usr/local/groundwork/noma/notifier/alert_via_noma.pl'
		);
		INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'),
			(SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')
		);
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-c', '-c');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'notifyType', 'notifyType');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-s', '-s');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hoststate','hoststate');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-H', '-H');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hostname','hostname');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-G', '-G');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hostgroupnames', 'hostgroupnames');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-n', '-n');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'notificationtype','notificationtype');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-i', '-i');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hostaddress', 'hostaddress');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-o', '-o');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hostoutput', 'hostoutput');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-t', '-t');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'shortdatetime', 'shortdatetime');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-u', '-u');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'hostnotificationid', 'hostnotificationid');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-A', '-A');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'notificationauthoralias', 'notificationauthoralias');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-C', '-C');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'notificationcomment', 'notificationcomment');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), '-R', '-R');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'notificationrecipients', 'notificationrecipients');

		INSERT INTO Action (ActionTypeID,Name,Description) VALUES(
			(SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),
			'Noma Notify For Service',
			'Invoke a script for the selected message'
		);
		INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
			(SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'),
			'Script',
			'/usr/local/groundwork/noma/notifier/alert_via_noma.pl'
		);
		INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'),
			(SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')
		);

		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-c', '-c');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'notifyType','notifyType');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-s', '-s');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'servicestate','servicestate');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-H', '-H');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'hostname', 'hostname');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-G', '-G');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'hostgroupnames','hostgroupnames');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-E', '-E');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'servicegroupnames', 'servicegroupnames');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-S', '-S');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'servicedescription','servicedescription');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-o', '-o');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'serviceoutput', 'serviceoutput');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-n', '-n');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'notificationtype', 'notificationtype');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-a', '-a');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'hostalias', 'hostalias');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-i', '-i');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'hostaddress', 'hostaddress');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-t', '-t');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'shortdatetime', 'shortdatetime');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-u', '-u');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'servicenotificationid', 'servicenotificationid');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-A', '-A');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'notificationauthoralias', 'notificationauthoralias');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-C', '-C');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'notificationcomment', 'notificationcomment');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), '-R', '-R');
		INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'notificationrecipients', 'notificationrecipients');

		currentSchemaVersion = '6.7';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
		ELSE
			UPDATE schemainfo set value = now() where name = 'SchemaUpdated';
		END IF;
	END IF;

	-- --------------------------------------
	-- Update from version 6.7.0 to 7.0.X  --
	-- --------------------------------------

	-- currentSchemaVersion is '6.7' if we previously upgraded to the 6.7.0 release (see just above),
	-- but '6.7.0' if we started fresh with the 6.7.0 release.  So we need to handle both cases here.
	IF (currentSchemaVersion = '6.7' OR currentSchemaVersion = '6.7.0') THEN

		-- New Application types for Cloud Hub RHEV-M and Archive Server
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'CHRHEV') THEN
			INSERT INTO ApplicationType(ApplicationTypeID, name, description, statetransitioncriteria)
			VALUES (201,'CHRHEV', 'Cloud Hub for Red Hat Virtualization', 'Device;Host;ServiceDescription');

			INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
			VALUES (
				(SELECT ApplicationTypeID FROM ApplicationType where Name = 'CHRHEV'),
				(SELECT EntityTypeID FROM EntityType WHERE Name = 'HOST_STATUS'),
				(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),
				85
			);

			INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
			VALUES (
				(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),
				(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
				(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),
				86
			);

			INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
			VALUES (
				(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),
				(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
				(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
				87
			);

			INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
			VALUES (
				(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),
				(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
				(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),
				88
			);
		END IF;

		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'ARCHIVE') THEN
			INSERT INTO ApplicationType(name, description, statetransitioncriteria)
			VALUES ('ARCHIVE', 'Archiving related messages', 'Device;Host');
		END IF;
		
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'BSM') THEN
			INSERT INTO ApplicationType(name, description, statetransitioncriteria)
			VALUES ('BSM', 'Business Service Monitoring', 'Device;Host');
		END IF;

		ALTER TABLE hostgroup ADD COLUMN agentid character varying(128);
		ALTER TABLE host ADD COLUMN agentid character varying(128);
		ALTER TABLE servicestatus ADD COLUMN agentid character varying(128);

		currentSchemaVersion = '7.0.0';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
		ELSE
			UPDATE schemainfo set value = now() where name = 'SchemaUpdated';
		END IF;
	END IF;
	
	IF (currentSchemaVersion = '7.0.0') THEN
		-- Fix for GWMON-11422
		IF NOT EXISTS (SELECT NULL FROM OperationStatus WHERE Name = 'ACKNOWLEDGED') THEN
			INSERT INTO OperationStatus (Name, Description) VALUES('ACKNOWLEDGED','Status Acknowledged');
		END IF;
		
		IF NOT EXISTS (SELECT NULL FROM Action WHERE Name = 'Acknowledge Log Message') THEN
			INSERT INTO Action (ActionTypeID, Name, Description) 
			VALUES(
				(SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'),
				'Acknowledge Log Message',
				'Update Log Message Operation Status To Acknowledged'
			);
		END IF;

		IF NOT EXISTS (SELECT NULL FROM ActionProperty WHERE Value = 'ACKNOWLEDGED') THEN
			INSERT INTO ActionProperty (ActionID, Name, Value)
			VALUES( (SELECT ActionID FROM Action WHERE Name = 'Acknowledge Log Message'), 'OperationStatus', 'ACKNOWLEDGED');
		END IF;

		IF NOT EXISTS (SELECT NULL FROM ApplicationAction WHERE ActionId= (select actionid from action where Name = 'Acknowledge Log Message')) THEN
			INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
			VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Acknowledge Log Message'));
		END IF;
		
		-- Adding Selenium Connector to 7.0.2 release  
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'SEL') THEN
			INSERT INTO ApplicationType(name, description, statetransitioncriteria)
			VALUES ('SEL', 'Groundwork Selenium Agent Connector', 'Device;Host;ServiceDescription');
		END IF;
		
		-- Adding Open Stack Virtualization to 7.0.2 release  
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'OS') THEN
			INSERT INTO ApplicationType(name, description, statetransitioncriteria)
			VALUES ('OS', 'Groundwork OpenStack Virtualization Connector', 'Device;Host;ServiceDescription');
		END IF;
		
		-- Adding Audit type to 7.0.2 release  
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'AUDIT') THEN
			INSERT INTO ApplicationType( Name, Description, StateTransitionCriteria)
			VALUES ('AUDIT', 'Audit Events from all SubSystems', 'Device;Host');
		END IF;
		
		-- Adding Audit type to 7.0.2 release  
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'CACTI') THEN
			INSERT INTO ApplicationType( Name, Description, StateTransitionCriteria)
			VALUES ('CACTI', 'CACTI Events from all SubSystems', 'Device;Host;ServiceDescription');
		END IF;
		
		sortOrder_702 := 101;
		FOREACH appType_702 IN ARRAY appTypes_702
		LOOP 
			-- for for LOG_MESSAGE entity and property Comments
			EXECUTE format('select p.propertytypeid from applicationentityproperty aep,propertytype p,applicationtype a,entitytype e where e.entitytypeid=aep.entitytypeid and e.name=''LOG_MESSAGE'' and aep.propertytypeid=p.propertytypeid and a.applicationtypeid=aep.applicationtypeid and a.name=%L and p.name=''Comments''',appType_702) INTO proptypeid_702;
			if proptypeid_702 is null then
				EXECUTE format('INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = %L), (SELECT EntityTypeID FROM EntityType WHERE Name = ''LOG_MESSAGE''), (SELECT PropertyTypeID FROM PropertyType WHERE Name = ''Comments''), %s)',appType_702,sortOrder_702);
			end if;
			sortOrder_702 := sortOrder_702 + 1;
			
			-- for LOG_MESSAGE entity and property AcknowledgedBy
			EXECUTE format('select p.propertytypeid from applicationentityproperty aep,propertytype p,applicationtype a,entitytype e where e.entitytypeid=aep.entitytypeid and e.name=''LOG_MESSAGE'' and aep.propertytypeid=p.propertytypeid and a.applicationtypeid=aep.applicationtypeid and a.name=%L and p.name=''AcknowledgedBy''',appType_702) INTO proptypeid_702;
			if proptypeid_702 is null then
				EXECUTE format('INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = %L), (SELECT EntityTypeID FROM EntityType WHERE Name = ''LOG_MESSAGE''), (SELECT PropertyTypeID FROM PropertyType WHERE Name = ''AcknowledgedBy''), %s)',appType_702,sortOrder_702);
			end if;
			sortOrder_702 := sortOrder_702 + 1;
			
			-- for HOST_STATUS entity and property isAcknowledged
			EXECUTE format('select p.propertytypeid from applicationentityproperty aep,propertytype p,applicationtype a,entitytype e where e.entitytypeid=aep.entitytypeid and e.name=''HOST_STATUS'' and aep.propertytypeid=p.propertytypeid and a.applicationtypeid=aep.applicationtypeid and a.name=%L and p.name=''isAcknowledged''',appType_702) INTO proptypeid_702;
			if proptypeid_702 is null then
				EXECUTE format('INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = %L), (SELECT EntityTypeID FROM EntityType WHERE Name = ''HOST_STATUS''), (SELECT PropertyTypeID FROM PropertyType WHERE Name = ''isAcknowledged''), %s)',appType_702,sortOrder_702);
			end if;
			sortOrder_702 := sortOrder_702 + 1;
			
			-- for SERVICE_STATUS entity and property isAcknowledged 
			EXECUTE format('select p.propertytypeid from applicationentityproperty aep,propertytype p,applicationtype a,entitytype e where e.entitytypeid=aep.entitytypeid and e.name=''SERVICE_STATUS'' and aep.propertytypeid=p.propertytypeid and a.applicationtypeid=aep.applicationtypeid and a.name=%L and p.name=''isAcknowledged''',appType_702) INTO proptypeid_702;
			if proptypeid_702 is null then
				EXECUTE format('INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = %L), (SELECT EntityTypeID FROM EntityType WHERE Name = ''SERVICE_STATUS''), (SELECT PropertyTypeID FROM PropertyType WHERE Name = ''isAcknowledged''), %s)',appType_702,sortOrder_702);
			end if;
			sortOrder_702 := sortOrder_702 + 1;
		END LOOP;
		
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'OS') THEN
			INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
				(SELECT ApplicationTypeID FROM ApplicationType where Name = 'OS'),
				(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
				(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
				3
			);
		END IF;

		currentSchemaVersion = '7.0.2';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
		ELSE
			UPDATE schemainfo set value = now() where name = 'SchemaUpdated';
		END IF;
	END IF;

	IF (currentSchemaVersion = '7.0.2' OR currentSchemaVersion = '${groundwork.version}') THEN
		-- All 7.1.0 changes go here ...

		-- GWMON-12058:  Add an ApplicationType.DisplayName column in the middle of the table.  This takes
		-- some extra work, because PostgreSQL does not support a "FIRST | AFTER col_name" clause like
		-- MySQL does to position the new column at some position other than at the end of the table.
		-- The new column will be populated later on in this script, via separate UPDATE commands.
		ALTER TABLE ApplicationType RENAME COLUMN description to old_description;
		ALTER TABLE ApplicationType RENAME COLUMN statetransitioncriteria to old_statetransitioncriteria;
		ALTER TABLE ApplicationType ADD COLUMN displayname             character varying(128);
		ALTER TABLE ApplicationType ADD COLUMN description             character varying(254);
		ALTER TABLE ApplicationType ADD COLUMN statetransitioncriteria character varying(512);
		UPDATE ApplicationType set (description, statetransitioncriteria) = (old_description, old_statetransitioncriteria);
		ALTER TABLE ApplicationType DROP COLUMN old_description;
		ALTER TABLE ApplicationType DROP COLUMN old_statetransitioncriteria;

		ALTER TABLE category ADD COLUMN applicationtypeid INTEGER;
		ALTER TABLE category ADD COLUMN agentid character varying(128);

		CREATE TABLE IF NOT EXISTS auditlog (
			auditlogid integer NOT NULL,
			subsystem character varying(254) NOT NULL,
			action character varying(32) NOT NULL,
			description character varying(4096) NOT NULL,
			username character varying(254) NOT NULL,
			logtimestamp timestamp without time zone NOT NULL,
			hostname character varying(254),
			servicedescription character varying(254),
			hostgroupname character varying(254),
			servicegroupname character varying(254)
		);
		ALTER TABLE public.auditlog OWNER TO collage;
		CREATE SEQUENCE auditlog_auditlogid_seq
			START WITH 1
			INCREMENT BY 1
			NO MINVALUE
			NO MAXVALUE
			CACHE 1;
		ALTER TABLE public.auditlog_auditlogid_seq OWNER TO collage;
		ALTER SEQUENCE auditlog_auditlogid_seq OWNED BY auditlog.auditlogid;

		CREATE TABLE IF NOT EXISTS devicetemplateprofile (
			devicetemplateprofileid integer NOT NULL,
			deviceidentification character varying(128) NOT NULL,
			devicedescription character varying(254),
			cactihosttemplate character varying(254),
			monarchhostprofile character varying(254),
			changedtimestamp timestamp without time zone NOT NULL
		);
		ALTER TABLE public.devicetemplateprofile OWNER TO collage;
		CREATE SEQUENCE devicetemplateprofile_devicetemplateprofileid_seq
			START WITH 1
			INCREMENT BY 1
			NO MINVALUE
			NO MAXVALUE
			CACHE 1;
		ALTER TABLE public.devicetemplateprofile_devicetemplateprofileid_seq OWNER TO collage;
		ALTER SEQUENCE devicetemplateprofile_devicetemplateprofileid_seq OWNED BY devicetemplateprofile.devicetemplateprofileid;

		CREATE TABLE IF NOT EXISTS hostblacklist (
			hostblacklistid integer NOT NULL,
			hostname character varying(254) NOT NULL
		);
		ALTER TABLE public.hostblacklist OWNER TO collage;
		CREATE SEQUENCE hostblacklist_hostblacklistid_seq
			START WITH 1
			INCREMENT BY 1
			NO MINVALUE
			NO MAXVALUE
			CACHE 1;
		ALTER TABLE public.hostblacklist_hostblacklistid_seq OWNER TO collage;
		ALTER SEQUENCE hostblacklist_hostblacklistid_seq OWNED BY hostblacklist.hostblacklistid;

		CREATE TABLE IF NOT EXISTS hostidentity (
			hostidentityid uuid NOT NULL,
			hostname character varying(254) NOT NULL,
			hostid integer NULL
		);
		ALTER TABLE public.hostidentity OWNER TO collage;

		CREATE TABLE IF NOT EXISTS hostname (
			hostidentityid uuid NOT NULL,
			hostname character varying(254) NOT NULL
		);
		ALTER TABLE public.hostname OWNER TO collage;

		ALTER TABLE ONLY auditlog
			ADD CONSTRAINT auditlog_pkey PRIMARY KEY (auditlogid);
		ALTER TABLE ONLY category
			ADD CONSTRAINT category_ibfk_2 FOREIGN KEY (applicationtypeid)
				REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;
		ALTER TABLE ONLY devicetemplateprofile
			ADD CONSTRAINT devicetemplateprofile_deviceidentification_key UNIQUE (deviceidentification);
		ALTER TABLE ONLY devicetemplateprofile
			ADD CONSTRAINT devicetemplateprofile_pkey PRIMARY KEY (devicetemplateprofileid);
		ALTER TABLE ONLY hostblacklist
			ADD CONSTRAINT hostblacklist_hostname_key UNIQUE (hostname);
		ALTER TABLE ONLY hostblacklist
			ADD CONSTRAINT hostblacklist_pkey PRIMARY KEY (hostblacklistid);
		ALTER TABLE ONLY hostidentity
			ADD CONSTRAINT hostidentity_hostname_key UNIQUE (hostname);
		ALTER TABLE ONLY hostidentity
			ADD CONSTRAINT hostidentity_pkey PRIMARY KEY (hostidentityid);
		ALTER TABLE ONLY hostidentity
			ADD CONSTRAINT hostidentity_ibfk_1 FOREIGN KEY (hostid)
				REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE SET NULL;
		ALTER TABLE ONLY hostname
			ADD CONSTRAINT hostname_ibfk_1 FOREIGN KEY (hostidentityid)
				REFERENCES hostidentity(hostidentityid) ON UPDATE RESTRICT ON DELETE CASCADE;

		CREATE INDEX category_applicationtypeid ON category USING btree (applicationtypeid);
		CREATE UNIQUE INDEX hostidentity_hostid ON hostidentity USING btree (hostid);
		CREATE UNIQUE INDEX hostname_hostname ON hostname USING btree (lower(hostname));
		CREATE INDEX hostname_hostidentityid ON hostname USING btree (hostidentityid);

		UPDATE ApplicationType set DisplayName = 'SYSTEM'   where name = 'SYSTEM';
		UPDATE ApplicationType set DisplayName = null       where name = 'NAGIOS';
		UPDATE ApplicationType set DisplayName = 'VEMA'     where name = 'VEMA';
		UPDATE ApplicationType set DisplayName = 'GDMA'     where name = 'GDMA';
		UPDATE ApplicationType set DisplayName = 'NOMA'     where name = 'NOMA';
		UPDATE ApplicationType set DisplayName = 'CHRHEV'   where name = 'CHRHEV';
		UPDATE ApplicationType set DisplayName = 'ARCHIVE'  where name = 'ARCHIVE';
		UPDATE ApplicationType set DisplayName = 'SEL'      where name = 'SEL';
		UPDATE ApplicationType set DisplayName = 'OS'       where name = 'OS';
		UPDATE ApplicationType set DisplayName = 'AUDIT'    where name = 'AUDIT';
		UPDATE ApplicationType set DisplayName = 'BSM'      where name = 'BSM';
		UPDATE ApplicationType set DisplayName = 'CACTI'    where name = 'CACTI';
		UPDATE ApplicationType set DisplayName = 'SYSLOG'   where name = 'SYSLOG';
		UPDATE ApplicationType set DisplayName = 'SNMPTRAP' where name = 'SNMPTRAP';

		INSERT INTO ApplicationType(ApplicationTypeID, Name, DisplayName, Description, StateTransitionCriteria)
			VALUES (300,'DOWNTIME', 'DOWN', 'In Downtime Management', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('SHIFT', 'SHIFT', 'Cloud Hub for OpenSHIFT Virtualization', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('DOCK', 'DOCK', 'Cloud Hub for Docker Containers', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('CISCO', 'CISCO', 'Net Hub for CISCO ACI', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('NSX', 'NSX', 'Net Hub for VMWare NSX', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('ODL', 'ODL', 'Net Hub for Open Daylight SDN', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('AWS', 'AWS', 'Cloud Hub for Amazon EC2 Infrastructure', 'Device;Host;ServiceDescription');
		INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria)
			VALUES ('NETAPP', 'NETAPP', 'Cloud Hub for NetAPP storage appliance', 'Device;Host;ServiceDescription');

		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'DOCK'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),
			1
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'DOCK'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'),
			2
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='DOCK'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='DOCK'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'ODL'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),
			1
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'ODL'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'),
			2
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ODL'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ODL'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'AWS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),
			1
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType where Name = 'AWS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'),
			2
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AWS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);
		INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES (
			(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AWS'),
			(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),
			(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'),
			90
		);

		INSERT INTO MonitorStatus(Name, Description)  VALUES ('START DOWNTIME', 'START DOWNTIME');
		INSERT INTO MonitorStatus(Name, Description)  VALUES ('IN DOWNTIME', 'IN DOWNTIME');
		INSERT INTO MonitorStatus(Name, Description)  VALUES ('END DOWNTIME', 'END DOWNTIME');

		ALTER TABLE category ADD COLUMN root boolean DEFAULT true NOT NULL;
		CREATE TABLE categoryancestry (
			categoryid integer DEFAULT 0 NOT NULL,
			ancestorid integer DEFAULT 0 NOT NULL
		);
		ALTER TABLE public.categoryancestry OWNER TO collage;
		ALTER TABLE ONLY category
			DROP CONSTRAINT IF EXISTS category_name_key;
		ALTER TABLE ONLY category
			ADD CONSTRAINT category_name_entitytypeid_key UNIQUE (name, entitytypeid);
		ALTER TABLE ONLY categoryancestry
			ADD CONSTRAINT categoryancestry_pkey PRIMARY KEY (categoryid, ancestorid);
		CREATE INDEX categoryancestry_categoryid ON categoryancestry USING btree (categoryid);
		CREATE INDEX categoryancestry_ancestorid ON categoryancestry USING btree (ancestorid);
		CREATE INDEX categoryhierarchy_categoryid ON categoryhierarchy USING btree (categoryid);
		ALTER TABLE ONLY category
			ADD CONSTRAINT category_ibfk_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid)
			ON UPDATE RESTRICT ON DELETE CASCADE;
		ALTER TABLE ONLY categoryancestry
			ADD CONSTRAINT categoryancestry_ibfk_1 FOREIGN KEY (ancestorid) REFERENCES category(categoryid)
			ON UPDATE RESTRICT ON DELETE CASCADE;
		ALTER TABLE ONLY categoryancestry
			ADD CONSTRAINT categoryancestry_ibfk_2 FOREIGN KEY (categoryid) REFERENCES category(categoryid)
			ON UPDATE RESTRICT ON DELETE CASCADE;

		INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(24,'CUSTOM_GROUP',    'com.groundwork.collage.model.impl.CustomGroup',    true);
		INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(25,'HOST_CATEGORY',   'com.groundwork.collage.model.impl.HostCategory',   true);
		INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(26,'SERVICE_CATEGORY','com.groundwork.collage.model.impl.ServiceCategory',true);

		-- Merge duplicate hosts preserving uppercase host since it is likely that
		-- the upper case version is the real host name introduced into the Collage
		-- database from feeders or agents that preserve case, (e.g. CloudHub). The
		-- lowercase version of the host was probably folded by Monarch.
		SELECT uniqueify_host_hostnames(true) INTO uniqueify_count;
		IF (uniqueify_count > 0) THEN
			RAISE INFO 'Uniqueified hosts count: %', uniqueify_count;
		END IF;
		-- Create unique index for host to prevent duplicate hosts.
		ALTER TABLE ONLY host
			DROP CONSTRAINT IF EXISTS host_hostname_key;
		CREATE UNIQUE INDEX host_hostname ON host USING btree (lower(hostname));

		CREATE INDEX servicestatus_servicedescription ON servicestatus USING btree (servicedescription);

		-- The following changes (up until changing currentSchemaVersion to '7.1.0') were
		-- done for the actual distributed GWME 7.1.0 release, but in that release were
		-- placed in the following section of this file, intended for changes to upgrade
		-- to the GWME 7.1.1 release.  And that following 7.1.0-to-7.1.1 upgrade section
		-- was anomalously included in the 7.1.0 release and executed in an upgrade to the
		-- 7.1.0 release, resulting in the internal database version number being declared
		-- as '7.1.1' in the GWME 7.1.0 release.  Sigh.  For this copy of the migration
		-- script in the GWME 7.1.1 release, we are now fixing that by moving these items
		-- back into the section for upgrades to the 7.1.0 version of the database, to more
		-- clearly indicate the time frame in which these changes became operative.  This
		-- restructuring helps when understanding what companion changes need to be done for
		-- the archive_gwcollagedb database, which is why I am forcing this issue.  Since
		-- because of the anomalous behavior for the GWME 7.1.0 release (spurting ahead to
		-- declare the database version to be 7.1.1), we never had any customer systems that
		-- declared themselves internally to be the 7.1.0 currentSchemaVersion, so this
		-- change of code location should not cause disruption for any customer upgrades.
		-- It leaves the 7.1.0-to-7.1.1 upgrade section empty except for the version-number
		-- upgrading, but that's okay, as it properly reflects what got done for the GWME
		-- 7.1.1 release (save for content-only fixup listed in the section after that).

		ALTER TABLE servicestatus ADD COLUMN applicationhostname character varying(254);

		-- Netapp appliance might not exist on GWME 7.0.2 SP03 systems
		INSERT INTO ApplicationType (Name, DisplayName, Description, StateTransitionCriteria)
			SELECT 'NETAPP', 'NETAPP', 'Cloud Hub for NetAPP storage appliance', 'Device;Host;ServiceDescription'
			WHERE NOT EXISTS ( SELECT Name FROM ApplicationType WHERE Name like 'NETAPP' );

		-- No reason not to make this Icinga2 insertion idempotent like the others.
		INSERT INTO ApplicationType (Name, DisplayName, Description, StateTransitionCriteria)
			SELECT 'ICINGA2', 'ICINGA2', 'Cloud Hub for Icinga2 Monitoring', 'Device;Host;ServiceDescription'
			WHERE NOT EXISTS ( SELECT Name FROM ApplicationType WHERE Name like 'ICINGA2' );

		-- Cacti appliance might not exist on GWME 7.0.2 SP03 systems
		INSERT INTO ApplicationType (Name, DisplayName, Description, StateTransitionCriteria)
			SELECT 'CACTI', 'CACTI', 'Cacti feeder statistics data', 'Device;Host;ServiceDescription'
			WHERE NOT EXISTS ( SELECT Name FROM ApplicationType WHERE Name like 'CACTI' );

		currentSchemaVersion = '7.1.0';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		UPDATE schemainfo set value = '7.0.2' where name = 'Schema Version';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
		ELSE
			UPDATE schemainfo set value = now() where name = 'SchemaUpdated';
		END IF;
	END IF;

	IF (currentSchemaVersion = '7.1.0') THEN
		-- All 7.1.1 changes go here ...

		-- Nothing is being done specifically for the 7.1.1 release.
		-- See the tail end of the previous section for other stuff that used to be here,
		-- and the following section for certain additional content changes.

		currentSchemaVersion = '7.1.1';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		UPDATE schemainfo set value = '7.1.0' where name = 'Schema Version';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',now());
		ELSE
			UPDATE schemainfo set value = now() where name = 'SchemaUpdated';
		END IF;
	END IF;

	IF (currentSchemaVersion = '7.1.1') THEN
		-- Here we have some special corrections mostly for some content-only fixup that ought
		-- to have been addressed in the GWME 7.1.0 timeframe, but was not.  No schema changes
		-- are allowed here, as that would invalidate keeping the currentSchemaVersion constant.
		-- Because of the version-number mismatch that was effective in the GWME 7.1.0 release,
		-- there is no version-number upgrade in this section, so in future releases we can get
		-- back to matching currentSchemaVersion with the GWME release numbering.  So in the
		-- sense that we are making some "untracked" changes here, one really needs to take the
		-- view that the tracking consists of both the external GWME version and the internal
		-- currentSchemaVersion.  All we are doing is correcting to bring an upgraded database
		-- into line with the content of a fresh-install database.  The changes applied here will
		-- be executed in any upgrade from GWME 7.1.0 or earlier to GWME 7.1.1 or later (which
		-- is desired and expected), and also (because of the currentSchemaVersion confusion
		-- in GWME 7.1.0, which makes it necessary to execute this when currentSchemaVersion
		-- = '7.1.1') from GWME 7.1.1 to some later release, and not in any other situations.
		-- Supporting the 7.1.1-to-later scenario, as well as the previously-upgraded-to-7.0.2
		-- scenario, both cases in which the data is already correct, forces us to ensure that
		-- these changes are idempotent without raising any exceptions.

		-- GWMON-12687:  A fresh GWME 7.0.2 install (but not an upgrade to GWME 7.0.2,
		-- which did set these fields correctly) set statetransitioncriteria for the CACTI
		-- application type to 'Device;Host', and previous upgrade processing did not
		-- correct that (and the accompanying description field) to its fresh-install value
		-- in the GWME 7.1.0 and GWME 7.1.1 releases.  So we need to fix that now.
		UPDATE applicationtype
			SET description = 'CACTI Events from all SubSystems'
			WHERE name = 'CACTI'
			AND description != 'CACTI Events from all SubSystems';
		UPDATE applicationtype
			SET statetransitioncriteria = 'Device;Host;ServiceDescription'
			WHERE name = 'CACTI'
			AND statetransitioncriteria != 'Device;Host;ServiceDescription';

		-- Here we also make some fresh content-only changes in the 7.1.1 release.  See
		-- the notes above for why this is acceptable, in what situations this will get
		-- executed, and why these changes must be made idempotent.

		-- GWMON-12716:  Add an application type and a consolidation criteria for use
		-- by the NeDi feeder.  (The value we set here for the consolidation criteria
		-- is preliminary, and may change in later releases.  We don't know yet what
		-- consolidation criteria to use for this feeder.  We might end up with a duplicate
		-- of some other existing criteria, but the NeDi feeder is still in development and
		-- its requirements might change over time.)
		--
		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'NEDI') THEN
			INSERT INTO ApplicationType( Name, DisplayName, Description, StateTransitionCriteria )
			VALUES ('NEDI', 'NEDI', 'NeDi Application Feed', 'Device;Host');
		END IF;
		IF NOT EXISTS (SELECT NULL FROM ConsolidationCriteria WHERE Name = 'NEDIEVENT') THEN
			INSERT INTO ConsolidationCriteria( Name, Criteria )
			VALUES ('NEDIEVENT', 'Device;MonitorStatus;OperationStatus;TextMessage');
		END IF;

		-- GWMON-11731:  Here we repair some historical damage.  The 7.0.2-SPO3 patch did
		-- not add the servicestatus_servicedescription index (which may or may not have
		-- made sense at the time), but it modified the gwcollagedb database in such a way
		-- that the addition of this index to the database would not subsequently occur in
		-- an upgrade to the 7.1.0 release.  We fix that here to ensure that a system which
		-- went through an upgrade path involving the 7.0.2-SPO3 patch is brought into full
		-- compliance with the database schema we would expect in a fresh 7.1.1 install.
		--
		-- PostgreSQL 9.4.X has no "CREATE INDEX IF NOT EXISTS" statement; that form is not
		-- available until PostgreSQL 9.5.x.  But the to_regclass() function for getting the
		-- OID of the named relation or NULL if it does not exist, used here to effectively
		-- test for the existence of an index without throwing an exception, is available as
		-- of PostgreSQL 9.4.X.
		--
		IF to_regclass('servicestatus_servicedescription') IS NULL THEN
			CREATE INDEX servicestatus_servicedescription ON servicestatus USING btree (servicedescription);
		END IF;

		-- GWMON-11731:  Here we repair an historical oversight.  As of the 7.1.0 release, we
		-- no longer include the network_service_notifications, network_service_short_news, and
		-- network_service_status tables and their associated constraints and sequences in the
		-- gwcollagedb database.  But the 7.1.0 version of this migration script took no notice
		-- of that change, to drop said tables.  We do so now.

		ALTER TABLE IF EXISTS ONLY network_service_notifications DROP CONSTRAINT IF EXISTS network_service_notifications_pkey;
		ALTER TABLE IF EXISTS ONLY network_service_short_news    DROP CONSTRAINT IF EXISTS network_service_short_news_pkey;
		ALTER TABLE IF EXISTS ONLY network_service_status        DROP CONSTRAINT IF EXISTS network_service_status_pkey;

		ALTER TABLE IF EXISTS network_service_notifications ALTER COLUMN id DROP DEFAULT;
		ALTER TABLE IF EXISTS network_service_short_news    ALTER COLUMN id DROP DEFAULT;
		ALTER TABLE IF EXISTS network_service_status        ALTER COLUMN id DROP DEFAULT;

		DROP SEQUENCE IF EXISTS network_service_notifications_id_seq;
		DROP SEQUENCE IF EXISTS network_service_short_news_id_seq;
		DROP SEQUENCE IF EXISTS network_service_status_id_seq;

		DROP TABLE IF EXISTS network_service_notifications;
		DROP TABLE IF EXISTS network_service_short_news;
		DROP TABLE IF EXISTS network_service_status;
	END IF;

	IF (currentSchemaVersion = '7.1.1') THEN
		-- All 7.2.0 changes go here ...

		-- GWMON-13064: Use timezoned timestamps to fix specific issues seen with grafana but also to ensure system
		-- operates properly in general

		ALTER TABLE logmessage
			ALTER COLUMN firstInsertDate TYPE timestamp with time zone,
			ALTER COLUMN lastInsertDate TYPE timestamp with time zone,
			ALTER COLUMN reportDate TYPE timestamp with time zone;

		ALTER TABLE auditlog
			ALTER COLUMN logtimestamp TYPE timestamp with time zone;

		ALTER TABLE devicetemplateprofile
			ALTER COLUMN changedtimestamp TYPE timestamp with time zone;

		ALTER TABLE entityproperty
			ALTER COLUMN valuedate TYPE timestamp with time zone,
			ALTER COLUMN lasteditedon TYPE timestamp with time zone,
			ALTER COLUMN createdon TYPE timestamp with time zone;

		ALTER TABLE hoststatus
			ALTER COLUMN lastchecktime TYPE timestamp with time zone,
			ALTER COLUMN nextchecktime TYPE timestamp with time zone;

		ALTER TABLE hoststatusproperty
			ALTER COLUMN valuedate TYPE timestamp with time zone,
			ALTER COLUMN lasteditedon TYPE timestamp with time zone,
			ALTER COLUMN createdon TYPE timestamp with time zone;

		ALTER TABLE logmessageproperty
			ALTER COLUMN valuedate TYPE timestamp with time zone,
			ALTER COLUMN lasteditedon TYPE timestamp with time zone,
			ALTER COLUMN createdon TYPE timestamp with time zone;

		ALTER TABLE logperformancedata
			ALTER COLUMN lastchecktime TYPE timestamp with time zone;

		ALTER TABLE plugin
			ALTER COLUMN lastupdatetimestamp TYPE timestamp with time zone;

		ALTER TABLE servicestatus
			ALTER COLUMN lastchecktime TYPE timestamp with time zone,
			ALTER COLUMN nextchecktime TYPE timestamp with time zone,
			ALTER COLUMN laststatechange TYPE timestamp with time zone;

		ALTER TABLE servicestatusproperty
			ALTER COLUMN valuedate TYPE timestamp with time zone,
			ALTER COLUMN lasteditedon TYPE timestamp with time zone,
			ALTER COLUMN createdon TYPE timestamp with time zone;

		-- GWMON-13104 and GWMON-13108: add host and service prefix properties for each ApplicationType.
		-- update NAGIOS display name to 'NAGIOS'
		UPDATE ApplicationType set DisplayName = 'NAGIOS' where name = 'NAGIOS';

		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'CLOUDERA') THEN
			INSERT INTO ApplicationType( Name, DisplayName, Description, StateTransitionCriteria )
			VALUES ('CLOUDERA', 'CLOUDERA', 'Cloud Hub for Cloudera Monitoring', 'Device;Host;ServiceDescription');
		END IF;

		currentSchemaVersion = '7.2.0';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		UPDATE schemainfo set value = '7.1.1' where name = 'Schema Version';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',date_trunc('second',now()));
		ELSE
			UPDATE schemainfo set value = date_trunc('second',now()) where name = 'SchemaUpdated';
		END IF;
	END IF;

	IF (currentSchemaVersion = '7.2.0') THEN
		-- All 7.2.1 changes go here ...

		IF NOT EXISTS (SELECT NULL FROM ApplicationType WHERE Name = 'AZURE') THEN
			INSERT INTO ApplicationType( Name, DisplayName, Description, StateTransitionCriteria )
			VALUES ('AZURE', 'AZURE', 'Cloud Hub for Azure Monitoring', 'Device;Host;ServiceDescription');
		END IF;

		CREATE TABLE IF NOT EXISTS comment (
			commentid integer NOT NULL,
			notes text NOT NULL,
			author character varying(254) NOT NULL,
			createdon timestamp with time zone NOT NULL,
			hostid integer,
			servicestatusid integer
		);
		ALTER TABLE public.comment OWNER TO collage;

		CREATE SEQUENCE comment_commentid_seq
			START WITH 1
			INCREMENT BY 1
			NO MINVALUE
			NO MAXVALUE
			CACHE 1;
		ALTER TABLE public.comment_commentid_seq OWNER TO collage;
		ALTER SEQUENCE comment_commentid_seq OWNED BY comment.commentid;

		ALTER TABLE ONLY comment ADD CONSTRAINT comment_pkey PRIMARY KEY (commentid);

		CREATE INDEX comment_hostid ON comment USING btree (hostid);
		CREATE INDEX comment_servicestatusid ON comment USING btree (servicestatusid);

		ALTER TABLE ONLY comment
			ADD CONSTRAINT comment_hostid_fkey FOREIGN KEY (hostid) REFERENCES host(hostid) ON DELETE CASCADE;
		ALTER TABLE ONLY comment
			ADD CONSTRAINT comment_servicestatusid_fkey FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON DELETE CASCADE;

		currentSchemaVersion = '7.2.1';
		UPDATE schemainfo set value = currentSchemaVersion where name = 'CurrentSchemaVersion';
		UPDATE schemainfo set value = '7.2.0' where name = 'Schema Version';
		SELECT value INTO schemaUpdated FROM schemainfo WHERE name = 'SchemaUpdated';
		IF (schemaUpdated is NULL) THEN
			INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated',date_trunc('second',now()));
		ELSE
			UPDATE schemainfo set value = date_trunc('second',now()) where name = 'SchemaUpdated';
		END IF;
	END IF;

	IF (currentSchemaVersion = '7.2.1') THEN
		-- All 7.2.2 changes go here ...

		-- To be filled in during GWME 7.2.2 development, including both any schema and
		-- content changes for the 7.2.2 release, and the usual updates at the end to the
		-- "CurrentSchemaVersion", "Schema Version", and "SchemaUpdated" values in the
		-- schemainfo table.
		--
		-- Note that any time changes are made in the pg_migrate_gwcollagedb.sql script,
		-- similar changes need to be considered in the pg_migrate_archive_gwcollagedb.pl
		-- script as well.  The archive_gwcollagedb database is not an exact mirror of the
		-- gwcollagedb database, so this is not a blind-copy operation.  Also, some content
		-- changes will be automatically mirrored during archiving, and some will not, so
		-- that aspect needs to be thought through.
	END IF;

END;
$$ LANGUAGE plpgsql;

SELECT fn_migrate_gwcollagedb();

DROP FUNCTION IF EXISTS fn_migrate_gwcollagedb();

DROP FUNCTION IF EXISTS uniqueify_host_hostnames(canonical_upper_case boolean);

-- This file is not typically edited using vim.  Whatever tool is used to do so by the Java
-- developers generally is set to use 4-character tabstops, which confuses the heck out of the
-- file's normal appearance in vim.  The following setting establishes the same setup for those
-- times when vim is used to view the file or make adjustments, to keep the format stable.  We
-- set this at the end of the file so the default vim setting of "modelines" (as 5) will make
-- this line visible for initializing vim when it starts up, as long as the user's initial
-- "modeline" setting (along with settings for vi compatibility mode) allows an embedded
-- modeline to be recognized at all.
--
-- vim:set tabstop=4 shiftwidth=4:

