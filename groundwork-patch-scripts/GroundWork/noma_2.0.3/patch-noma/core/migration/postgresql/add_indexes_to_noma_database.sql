--
-- add_indexes_to_noma_database.sql
--

-- This is an SQL Script to add certain important indexes to a "noma" database, to improve the
-- performance of various queries run by the NoMa daemon.  All operations here must be idepotent,
-- because this script may be run more than once over time on a given customer database.

-- HOW TO RUN THIS SCRIPT
-- ----------------------
--
-- This script is to be run from a terminal window while logged in as the nagios user, with the
-- following command (assuming that a copy of this script has been placed into the /tmp directory):
--
-- /usr/local/groundwork/postgresql/bin/psql -U noma -d noma -f /tmp/add_indexes_to_noma_database.sql

-- ================================================================================================

-- Copyright (c) 2017 GroundWork Open Source, Inc.
-- www.groundworkopensource.com
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of version 2 of the GNU General Public License
-- as published by the Free Software Foundation.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program; if not, write to the Free Software
-- Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

-- ================================================================================================

--
-- Be sure to keep this up-to-date!
--
\set version '1.0 of 2017-08-06'
\echo ''
\echo add_indexes_to_noma_database.sql version :version
\echo ''

-- Make sure any error from this script gets reflected in the psql exit status.
\set ON_ERROR_STOP on

-- Suppress noise from "IF NOT EXISTS" clauses when the object does not exist.
\set QUIET on
SET client_min_messages=WARNING;
\set QUIET off

\set QUIET on
-- If this script was run before but errored out, we might still have this function lying around.
-- Drop it so we can ensure that we use the definition provided here.
DROP FUNCTION IF EXISTS add_indexes_to_noma_database();
\set QUIET off

\set QUIET on
CREATE FUNCTION add_indexes_to_noma_database() RETURNS VOID AS $$
BEGIN

    -- We need the following (host, service) indexes added to support efficient operation
    -- of various SELECT, UPDATE, and DELETE queries in the NoMa code.  Actions here must
    -- be protected with with standard idempotency guards, to prevent accidental creation
    -- of multiple copies of the desired indexes.

    -- PostgreSQL 9.4.X has no "CREATE INDEX IF NOT EXISTS" statement; that form is not
    -- available until PostgreSQL 9.5.x.  But the to_regclass() function for getting the
    -- OID of the named relation or NULL if it does not exist, used here to effectively
    -- test for the existence of an index without throwing an exception, is available as
    -- of PostgreSQL 9.4.X.
    --
    IF to_regclass('escalation_stati_host_service')   IS NULL THEN
	CREATE INDEX escalation_stati_host_service   ON escalation_stati   USING btree (host, service);
    END IF;
    IF to_regclass('notification_logs_host_service')  IS NULL THEN
	CREATE INDEX notification_logs_host_service  ON notification_logs  USING btree (host, service);
    END IF;
    IF to_regclass('notification_stati_host_service') IS NULL THEN
	CREATE INDEX notification_stati_host_service ON notification_stati USING btree (host, service);
    END IF;

END;
$$ LANGUAGE plpgsql;
\set QUIET off

\set QUIET on
begin transaction;
\pset tuples_only on
-- There seems not to be any way to prevent this SELECT from printing two blank lines, despite our
-- having turned tuples-only mode on.  I suppose we could redirect the output to /dev/null, but that
-- seems excessive and we don't want to suppress messages that might appear upon failure.
SELECT add_indexes_to_noma_database();
\pset tuples_only off
-- For script-debuggng purposes, uncomment the following line so we don't make any changes to the data.
-- rollback;
commit;
\set QUIET off

\set QUIET on
DROP FUNCTION IF EXISTS add_indexes_to_noma_database();
\set QUIET off

-- That's all, folks!
\echo ''
\echo Indexes have been added if necessary.
\echo ''
\q

