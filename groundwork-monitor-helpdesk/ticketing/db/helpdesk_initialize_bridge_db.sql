-- HelpDesk Bridge Database File
--
-- This script creates the helpdesk support database (HelpDeskBridgeDB)
-- along with the necessary user and access privileges.
--
-- Copyright 2013-2017 GroundWork Open Source, Inc. ("GroundWork").
-- All rights reserved.  Use is subject to commercial license terms.
--
-- We need to break up the actions here into several procedures
-- because "use {database};" is not allowed within a procedure.
--
-- We want this script to operate idempotently, partly so as not to wipe
-- out the database content during an upgrade.  That means we cannot drop
-- the database nor drop individual tables in this script.  If we ever
-- need to do so (e.g., if the database or table schema changes in some
-- new release), that will need to be handled by a separate script.

-- ================================================================

\connect postgres;

CREATE OR REPLACE FUNCTION create_helpdeskbridgedb_database()
RETURNS VOID
AS
$$
BEGIN
    RAISE INFO '';

    IF NOT EXISTS (
	SELECT NULL FROM pg_user WHERE usename='helpdesk'
    ) THEN
	-- We need to have already created the helpdesk user before we can use it in the
	-- CREATE DATABASE statement, so we take that action outside of this function,
	-- before this function is called.  But here, we do want to validate that the
	-- desired action actually completed successfully.
	RAISE EXCEPTION 'The helpdesk user does not exist.';
    END IF;

    --
    -- Create the HelpDeskBridgeDB database.
    --

    -- DROP DATABASE IF EXISTS HelpDeskBridgeDB;
    IF NOT EXISTS (
	SELECT NULL FROM pg_database WHERE datname='helpdeskbridgedb'
    ) THEN
	-- In PostgreSQL, CREATE DATABASE cannot be executed inside a transaction block.
	-- Therefore, that function cannot be called inside a function, which can only be
	-- invoked inside a (possibly implicit) transaction.  So we just do error-checking
	-- here, presumably having done the database creation before this function was called.
	RAISE EXCEPTION 'The HelpDeskBridgeDB database does not exist.';
    END IF;

    --
    -- Grant access to the HelpDeskBridgeDB database.
    --

    RAISE INFO 'Granting privileges to the helpdesk user.';
    -- We would revoke prior privileges, except that we want this script to run idempotently,
    -- so we don't want to disturb any other existing setup for this user.  Since we really
    -- only want this user to be associated with this one database, it's not clear that this
    -- is the right decision, but we'll go with it for now.
    -- REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM helpdesk;
    -- FIX LATER:  we could/should probably restrict these privileges a bit,
    -- to remove those privileges which won't actually be necessary.
    GRANT ALL PRIVILEGES ON DATABASE HelpDeskBridgeDB TO helpdesk;

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
	-- There is no need to explicitly roll back what we did here within this BEGIN block;
	-- that is implicit in the exception handling that got us here.  But we do want to
	-- report the failure to the user running this script.
	RAISE INFO '';
	RAISE WARNING '';
	RAISE WARNING 'Creating the helpdesk database failed.';
	RAISE WARNING '';
	-- Re-throw the exception, so the details of the specific failure will be printed
	-- where the administrator can see them.
	RAISE;

END;
$$
LANGUAGE PLPGSQL;

-- We turn off display of output headers and footers and such, because the SELECT
-- statements below don't produce anything interesting, and such decoration is just
-- confusing.
\pset tuples_only on
\pset border 0
\set VERBOSITY terse

-- Creating the helpdesk user must be done before we can reference it in the
-- CREATE DATABASE statement, so we do this as well outside of the preceding
-- function.  However, we do take care to check in that function whether this
-- action succeeded.
SELECT '';
SELECT 'INFO:  Creating the helpdesk user in the database.';
CREATE USER helpdesk WITH PASSWORD 'gwrk';

-- CREATE DATABASE will fail if any other sessions are connected to the template
-- database when we attempt to create the new database.  Therefore, we must ensure
-- that we have in place exception handling to catch and report such a failure,
-- and we must test that exception handling to show that it really works.
SELECT '';
SELECT 'INFO:  Creating the HelpDeskBridgeDB database.';
CREATE DATABASE HelpDeskBridgeDB OWNER=helpdesk
    ENCODING 'UTF8' LC_COLLATE='en_US.utf8' LC_CTYPE='en_US.utf8' TEMPLATE template0;

-- Execute Stored Procedure
SELECT create_helpdeskbridgedb_database();

-- Remove Stored Procedure
DROP FUNCTION IF EXISTS create_helpdeskbridgedb_database();

-- ================================================================

-- The database name in the \connect statement is implicitly quoted, so we
-- must use the capitalization exactly as PostgreSQL sees the database name.
\connect helpdeskbridgedb;

CREATE OR REPLACE FUNCTION populate_helpdeskbridgedb_database()
RETURNS VOID
AS
$$
BEGIN
    RAISE INFO '';

    --
    -- Table structure for table HelpDeskConcurrencyTable
    --

    RAISE INFO 'Creating the HelpDeskConcurrencyTable table.';

    -- DROP TABLE IF EXISTS HelpDeskConcurrencyTable;
    CREATE TABLE IF NOT EXISTS HelpDeskConcurrencyTable (
      LogMessageID int NOT NULL
    );
    ALTER TABLE public.HelpDeskConcurrencyTable OWNER TO helpdesk;

    -- We could depend on the IF EXISTS clause directly in the ALTER TABLE command, but that
    -- produces an ugly NOTICE: line in the script output if the object does not exist.  Such
    -- a notice seems absurd when we took explicit trouble to handle that case.  So to suppress
    -- the pointless message, we instead explicitly check whether the object exists, before
    -- running the DROP action.
    IF EXISTS (
	SELECT NULL FROM pg_constraint WHERE conname = 'helpdeskconcurrencytable_logmessageid_key'
    ) THEN
	ALTER TABLE ONLY public.HelpDeskConcurrencyTable DROP CONSTRAINT IF EXISTS helpdeskconcurrencytable_logmessageid_key;
    END IF;
    ALTER TABLE ONLY HelpDeskConcurrencyTable
	ADD CONSTRAINT helpdeskconcurrencytable_logmessageid_key UNIQUE (LogMessageID);

    --
    -- Table structure for table HelpDeskLookupTable
    --
    -- The automatically-maintained LastChangeTime gives us an idea of
    -- how old each row is, so we can intelligently purge ancient data
    -- that has no reasonable chance of ever being revived.
    --

    RAISE INFO 'Creating the HelpDeskLookupTable table.';

    -- DROP TABLE IF EXISTS HelpDeskLookupTable;
    CREATE TABLE IF NOT EXISTS HelpDeskLookupTable (
      LogMessageID int NOT NULL,
      DeviceIdentification varchar(128) default NULL,
      Operator varchar(64) NOT NULL,
      TicketNo varchar(64) NOT NULL,
      TicketStatus varchar(64) NOT NULL,
      ClientData text,
      LastChangeTime timestamp without time zone NOT NULL DEFAULT LOCALTIMESTAMP(0)
    );
    ALTER TABLE public.HelpDeskLookupTable OWNER TO helpdesk;

    -- We could depend on the IF EXISTS clause directly in the ALTER TABLE command, but that
    -- produces an ugly NOTICE: line in the script output if the object does not exist.  Such
    -- a notice seems absurd when we took explicit trouble to handle that case.  So to suppress
    -- the pointless message, we instead explicitly check whether the object exists, before
    -- running the DROP action.
    IF EXISTS (
	SELECT NULL FROM pg_constraint WHERE conname = 'helpdesklookuptable_pkey'
    ) THEN
	ALTER TABLE ONLY public.HelpDeskLookupTable DROP CONSTRAINT IF EXISTS helpdesklookuptable_pkey;
    END IF;
    ALTER TABLE ONLY HelpDeskLookupTable ADD CONSTRAINT helpdesklookuptable_pkey PRIMARY KEY (LogMessageID);

    IF EXISTS (
	SELECT NULL FROM pg_class, pg_index
	WHERE pg_class.relname = 'helpdesklookuptable_ticketstatus'
	AND pg_index.indexrelid = pg_class.oid
    ) THEN
	DROP INDEX IF EXISTS public.helpdesklookuptable_ticketstatus;
    END IF;
    CREATE INDEX helpdesklookuptable_ticketstatus
	ON HelpDeskLookupTable USING btree (TicketStatus);

    CREATE OR REPLACE FUNCTION update_lastchangetime() RETURNS TRIGGER AS $BODY$
    BEGIN
	IF NEW.lastchangetime IS NULL OR NEW.lastchangetime = OLD.lastchangetime THEN
	    NEW.lastchangetime = LOCALTIMESTAMP(0);
	END IF;
	RETURN NEW;
    END;
    $BODY$
    LANGUAGE PLPGSQL;

    IF EXISTS (
	SELECT NULL FROM pg_trigger, pg_class
	WHERE pg_trigger.tgname = 'update_helpdesklookuptable_lastchangetime'
	AND pg_class.oid = pg_trigger.tgrelid
	AND pg_class.relname = 'helpdesklookuptable'
    ) THEN
	DROP TRIGGER IF EXISTS update_helpdesklookuptable_lastchangetime ON HelpDeskLookupTable;
    END IF;
    CREATE TRIGGER update_helpdesklookuptable_lastchangetime
	BEFORE UPDATE ON HelpDeskLookupTable
	FOR EACH ROW EXECUTE PROCEDURE update_lastchangetime();

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
	-- There is no need to explicitly roll back what we did here within this BEGIN block;
	-- that is implicit in the exception handling that got us here.  But we do want to
	-- report the failure to the user running this script.
	RAISE INFO '';
	RAISE WARNING '';
	RAISE WARNING 'Creating the helpdesk tables failed.';
	RAISE WARNING '';
	-- Re-throw the exception, so the details of the specific failure will be printed
	-- where the administrator can see them.
	RAISE;

END;
$$
LANGUAGE PLPGSQL;

-- Begin Transaction
START TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Execute Stored Procedure
SELECT populate_helpdeskbridgedb_database();

-- Commit All Changes
-- If an error occurred while running the function, the following COMMIT will
-- actually ROLLBACK instead (and be printed that way when this executes).
COMMIT;

-- Remove Stored Procedure
DROP FUNCTION IF EXISTS populate_helpdeskbridgedb_database();

