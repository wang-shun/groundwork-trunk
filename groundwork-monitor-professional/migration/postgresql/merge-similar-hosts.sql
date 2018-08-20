--
-- merge-similar-hosts.sql
--
-- Copyright (c) 2016 GroundWork, Inc.  All rights reserved.
--
-- Be sure to keep this up-to-date!
--
\set version '1.0 of 2016-10-17'
\echo ''
\echo merge-similar-hosts.sql version :version
\echo ''


-- GENERAL DESCRIPTION
-- -------------------
--
-- The purpose of this script is to handle all the host and service collisions
-- identified by the companion show-similar-hosts.sql script.  That is done
-- by selecting one host of each set of similar hostnames (differing only in
-- lettercase) to be retained, while all the others are deleted.  Services from
-- the deleted hosts are transferred to the corresponding retained hosts.  When
-- all the adjustments here are complete, the show-similar-hosts.sql script
-- should display no remaining similar hostnames.
--
-- Similar considerations apply when similar hosts are found to have duplicate
-- services assigned.  One service in the set of duplicate services is selected
-- to be retained, while all the others are deleted.  Again, after this script
-- has been run, the show-similar-hosts.sql script should display no remaining
-- duplicate services.
--
-- To complete this work, retaining as much setup as possible for the hosts
-- to be deleted, adjustments are made in several tables.  The aim here is to
-- perform all necessary cleanup, but only in the runtime database.  Afterward,
-- the conflicting_archive_service_rows.pl script must be run to make a set of
-- corresponding adjustments in the archive database so daily log archiving will
-- run without error.
--
-- When crossing the GroundWork Montitor release boundary where this matters
-- (going from 7.0.2-SP02 or earlier to 7.0.2-SP03 or later), both of the
-- show-similar-hosts.sql and merge-similar-hosts.sql scripts must be run before
-- the upgrade so the upgrade scripting does not stumble over host and service
-- conflicts.  On the other hand, the conflicting_archive_service_rows.pl script
-- should generally be run immediately after the upgrade, to guarantee that the
-- archive database schema has been fully modified to its correct setup before
-- making modifications to the data in that database.


-- DOCUMENTATION
-- -------------
--
-- See the companion README.merge-similar-hosts file for a description of how
-- this script works, and the caveats involved in running it.


-- HOW TO RUN THIS SCRIPT
-- ----------------------
--
-- This script alters the gwcollagedb database to eliminate similar hosts and
-- duplicate services on such hosts.  It is to be run from a terminal window
-- while logged in as the nagios user, with the following command (assuming
-- that a copy of this script has been placed into the /tmp directory):
--
-- /usr/local/groundwork/postgresql/bin/psql -U collage -d gwcollagedb -f /tmp/merge-similar-hosts.sql


-- INTERNAL COMMENTARY
-- -------------------
--
-- Previous attempts to handle similar-host merging tried procedural code and complex
-- temporary tables.  In contrast, here we create only the most basic temporary
-- tables, and then use dynamic joins to still-existing data in other tables during
-- later processing as necessary for particular actions.  This is simpler than
-- trying to pre-analyze further associations between host and service objects.  It
-- dramatically simplifies the code and the whole approach.  (In truth, the queries
-- to get the temporary tables populated correctly for all possible cases of multple
-- similar hosts and multiple duplicate services on those similar hosts are in
-- themselves complicated.  But we're willing to pay that price up front, because
-- the meanings of those tables and their use in later statements are so clear.)  It
-- also means that these steps must be taken in precisely the order shown, so the
-- ancillary data is still around when we need it.

-- FIX MINOR:  Do any of the additional changes being done here, beyond those done
-- by the gwcollagedb migration script, violate any more archive constraints?


-- DATABASE MODIFICATION STEPS
-- ---------------------------

-- (*) Display start-of-run statistics.
--
\set QUIET on
CREATE TEMPORARY TABLE before_deletion AS (
    SELECT
	h.total_hosts_before_deletion,
	s.total_services_before_deletion
    FROM
	(select count(*) as total_hosts_before_deletion    from host         ) as h,
	(select count(*) as total_services_before_deletion from servicestatus) as s
);

\pset footer off
SELECT * FROM before_deletion;
\pset footer on
\set QUIET off

-- (*) begin transaction
--
\set QUIET on
BEGIN TRANSACTION;
\set QUIET off

-- (*) in the following two items, create temporary tables to be used to store critical
--     associations regarding similar hosts and duplicate services

-- (*) identify all groups of similar hosts, and the particular to-be-retained host in each
--     group
--
-- one-to-many host associations:
--
CREATE TEMPORARY TABLE host_deletion_mapping (
    hostid_to_retain,
    hostid_to_delete
) ON COMMIT DROP AS (
    SELECT
	h_old.hostid AS preferred_host,
	h_new.hostid AS similar_host_to_delete
    FROM
	host AS h_old,
	host AS h_new
    WHERE
	lower(h_new.hostname) = lower(h_old.hostname)
    AND h_old.hostid < h_new.hostid
    AND h_old.hostid NOT IN (
	SELECT  
	    h_newer.hostid
	FROM    
	    host AS h_older,
	    host AS h_newer 
	WHERE   
	    lower(h_newer.hostname) = lower(h_older.hostname)
	AND h_older.hostid < h_newer.hostid
    )
);
ALTER TABLE host_deletion_mapping ADD PRIMARY KEY (hostid_to_delete);
CREATE INDEX ON host_deletion_mapping (hostid_to_retain);

-- (*) identify all groups of duplicate services to resolve, and the particular to-be-retained
--     service in each group
--
-- one-to-many service associations:
--
CREATE TEMPORARY TABLE service_deletion_mapping (
    servicestatusid_to_retain,
    servicestatusid_to_delete
) ON COMMIT DROP AS (
    WITH preferred_hosts AS (
	SELECT
	    h_old.hostid
	FROM
	    host AS h_old,
	    host AS h_new
	WHERE
	    lower(h_new.hostname) = lower(h_old.hostname)
	AND h_old.hostid < h_new.hostid
	AND h_old.hostid NOT IN (
	    SELECT
		h_newer.hostid
	    FROM
		host AS h_older,
		host AS h_newer
	    WHERE
		lower(h_newer.hostname) = lower(h_older.hostname)
	    AND h_older.hostid < h_newer.hostid
	)
    )
    SELECT
	s_preferred.servicestatusid AS preferred_service,
	s_duplicate.servicestatusid AS duplicate_service_to_delete
    FROM
	host          AS h_preferred,
	host          AS h_similar,
	servicestatus AS s_preferred,
	servicestatus AS s_duplicate
    WHERE
	lower(h_similar.hostname) = lower(h_preferred.hostname)
    AND h_similar.hostid  != h_preferred.hostid
    AND s_preferred.hostid = h_preferred.hostid
    AND s_duplicate.hostid = h_similar.hostid

    -- This clause restricts our view to only those host pairs which actually have a
    -- collision if we try to blindly merge them, because they share the same service.
    --
    AND s_duplicate.servicedescription = s_preferred.servicedescription

    -- Here we are making a choice of which service to prefer for retention.
    --
    -- * If the duplicate service already resides on the to-be-retained host,
    --   retain that copy of the service, and discard all the other copies.
    --   This rule causes the least disruption overall in keeping synchrony
    --   with the archive database and other adjustments.
    -- * Otherwise, retain the service with the smaller servicestatusid
    --   value, and discard all the other copies.  As with the similar-host
    --   retention rule, this rule for services is simple, it is immune to
    --   regular event-data deletion and race conditions in status-check
    --   times, and it breaks all possible ties.
    --
    AND
	(
	    h_preferred.hostid IN (SELECT hostid from preferred_hosts)
	OR
	    (
		h_similar.hostid NOT IN (SELECT hostid from preferred_hosts)
	    AND
		s_preferred.servicestatusid < s_duplicate.servicestatusid
	    )
	)
    AND s_preferred.servicestatusid NOT IN (
	SELECT
	    inner_s_duplicate.servicestatusid AS inner_duplicate_service_to_delete
	FROM
	    host          AS inner_h_preferred,
	    host          AS inner_h_similar,
	    servicestatus AS inner_s_preferred,
	    servicestatus AS inner_s_duplicate
	WHERE
	    lower(inner_h_similar.hostname) = lower(inner_h_preferred.hostname)
	AND inner_h_similar.hostid  != inner_h_preferred.hostid
	AND inner_s_preferred.hostid = inner_h_preferred.hostid
	AND inner_s_duplicate.hostid = inner_h_similar.hostid
	AND inner_s_duplicate.servicedescription = inner_s_preferred.servicedescription
	AND
	    (
		inner_h_preferred.hostid IN (SELECT hostid from preferred_hosts)
	    OR
		(
		    inner_h_similar.hostid NOT IN (SELECT hostid from preferred_hosts)
		AND
		    inner_s_preferred.servicestatusid < inner_s_duplicate.servicestatusid
		)
	    )
    )
);
ALTER TABLE service_deletion_mapping ADD PRIMARY KEY (servicestatusid_to_delete);
CREATE INDEX ON service_deletion_mapping (servicestatusid_to_retain);

-- (*) for each to-be-deleted similar host, transfer (merge) its hostgroup memberships to the
--     associated to-be-retained host
--
-- We cannot easily update existing hostgroupcollection rows in a bulk operation,
-- because that might cause unique constraint violations when we attempt to update
-- multiple existing rows that all map into a single target-row set of values.  So
-- instead we just insert new rows as needed, and allow the old rows to be cascade
-- deleted with the unwanted hosts are deleted in later processing.  The inner_hgc
-- table is frozen at the start of the statement, so it never includes any of the
-- insertions made as the INSERT statement executes.
--
INSERT INTO hostgroupcollection (hostid, hostgroupid) (
    SELECT DISTINCT
        hdm.hostid_to_retain,
	hgc.hostgroupid
    FROM
	host_deletion_mapping AS hdm,
	hostgroupcollection   AS hgc
    WHERE
	hgc.hostid = hdm.hostid_to_delete
    AND NOT EXISTS (
	SELECT 1
	FROM hostgroupcollection AS inner_hgc
	WHERE
	    inner_hgc.hostid      = hdm.hostid_to_retain
	AND inner_hgc.hostgroupid = hgc.hostgroupid
    )
);

-- (*) for each to-be-deleted similar host, transfer its logmessage host attribution to the
--     associated to-be-retained host (update both hoststatusid and deviceid fields to match
--     the values from the to-be-retained host)
--
UPDATE logmessage AS lm
SET
    hoststatusid = hdm.hostid_to_retain,
    deviceid     = retained_host.deviceid
FROM
    host_deletion_mapping AS hdm,
    host                  AS retained_host
WHERE    lm.hoststatusid = hdm.hostid_to_delete
AND retained_host.hostid = hdm.hostid_to_retain;

-- (*) for each to-be-deleted duplicate service, transfer (merge) its servicegroup memberships
--     to the associated to-be-retained service (follow the chain of associations:
--         servicestatus.servicestatusid => categoryentity.objectid
--         categoryentity.categoryid => category.categoryid
--         category.entitytypeid => entitytype.entitytypeid
--         entitytype = 'SERVICE_GROUP'
--     to identify service groups to which a particular service belongs)
--
-- We cannot easily update existing categoryentity rows in a bulk operation, because
-- that might create effectively-duplicate rows (if we ignore the always-unique
-- categoryentityid value in each row) when we attempt to update multiple existing
-- rows that all map into a single target-row set of values.  So instead we just
-- insert new rows as needed.  However, since the categoryentity table is very
-- general and there is no foreign-key reference on the objectid column with a
-- cascade-delete clause, we cannot simply expect the old rows to be cascade deleted
-- with the unwanted services are deleted in later processing.  So we must explicitly
-- delete the categoryentity rows that will become obsolete.  The inner_ce table is
-- frozen at the start of the statement, so it never includes any of the insertions
-- made as the INSERT statement executes.
--
INSERT INTO categoryentity (objectid, categoryid, entitytypeid) (
    SELECT DISTINCT
        sdm.servicestatusid_to_retain,
	ce.categoryid,
	ce.entitytypeid
    FROM
	service_deletion_mapping AS sdm,
	categoryentity           AS ce,
	category                 AS c,
	entitytype               AS et
    WHERE
	ce.objectid     = sdm.servicestatusid_to_delete
    AND c.categoryid    = ce.categoryid
    AND et.entitytypeid = c.entitytypeid
    AND 'SERVICE_GROUP' = et.name
    AND NOT EXISTS (
	SELECT 1
	FROM categoryentity AS inner_ce
	WHERE
	    inner_ce.objectid     = sdm.servicestatusid_to_retain
	AND inner_ce.categoryid   = ce.categoryid
	AND inner_ce.entitytypeid = ce.entitytypeid
    )
);

-- FIX LATER:  If the to-be-deleted services belonged to any categories with an
-- entitytype other than SERVICE_GROUP, this DELETE statement may empty them out
-- without deleting the categories themselves.  We don't handle that situation right
-- now mostly because we don't know what other categories might be in play, and how
-- they ought to be treated.
--
DELETE FROM categoryentity AS ce
USING service_deletion_mapping AS sdm
WHERE ce.objectid = sdm.servicestatusid_to_delete;

-- (*) for each to-be-deleted duplicate service, transfer its logmessage service attribution to
--     the associated to-be-retained service (update just the servicestatusid field to match the
--     value from the to-be-retained service)
--
UPDATE logmessage AS lm
SET servicestatusid = sdm.servicestatusid_to_retain
FROM service_deletion_mapping AS sdm
WHERE lm.servicestatusid = sdm.servicestatusid_to_delete;

-- (*) for each to-be-deleted duplicate service, transfer its logperformancedata service
--     attribution to the associated to-be-retained service (update just the servicestatusid
--     field to match the value from the to-be-retained service)
--
UPDATE logperformancedata AS lpd
SET servicestatusid = sdm.servicestatusid_to_retain
FROM service_deletion_mapping AS sdm
WHERE lpd.servicestatusid = sdm.servicestatusid_to_delete;

-- (*) we do not attempt to transfer any of the servicestatusproperty values from to-be-deleted
--     duplicate services to the associated to-be-retained services; most notably, this will
--     lose "Comments" data for the to-be-deleted duplicate services
--
-- NOTHING TO DO HERE

-- (*) delete all to-be-deleted duplicate services
--
DELETE FROM servicestatus      AS ss
USING service_deletion_mapping AS sdm
WHERE ss.servicestatusid = sdm.servicestatusid_to_delete;

-- (*) transfer all host association in servicegroups for remaining services on to-be-deleted
--     similar hosts to the associated to-be-retained hosts (there's nothing actually to be
--     done for this; servicegroup membership is tracked directly against the services [in the
--     categoryentity entries], and it is only through them that host associations are made
--     as well; so when we transfer the remaining services on the to-be-deleted similar hosts
--     to the associated to-be-retained hosts in the next step, the host associations for the
--     servicegroups will automatically come along for the ride)
--
-- NOTHING TO DO HERE

-- (*) transfer all remaining services on to-be-deleted similar hosts to the associated
--     to-be-retained hosts (change the servicestatus.hostid field from the hostid of the
--     to-be-deleted similar host to the hostid of the to-be-retained host)
--
UPDATE servicestatus AS ss
SET hostid = hdm.hostid_to_retain
FROM host_deletion_mapping AS hdm
WHERE ss.hostid = hdm.hostid_to_delete;

-- (*) we do not attempt to transfer any of the hoststatusproperty values from the to-be-deleted
--     similar hosts to the associated to-be-retained hosts; most notably, this will lose
--     "Comments" data for the to-be-deleted similar hosts
--
-- NOTHING TO DO HERE

-- (*) delete all to-be-deleted similar hosts
--
DELETE FROM host            AS h
USING host_deletion_mapping AS hdm
WHERE h.hostid = hdm.hostid_to_delete;

-- (*) helpful in debugging this script, to give feedback before we roll back all the changes
--
-- \include_relative show-similar-hosts.sql

-- (*) Display end-of-run statistics.
--
\echo ''
\set QUIET on
CREATE TEMPORARY TABLE after_deletion AS (
    SELECT
	h.total_hosts_after_deletion,
	s.total_services_after_deletion
    FROM
	(select count(*) as total_hosts_after_deletion    from host         ) as h,
	(select count(*) as total_services_after_deletion from servicestatus) as s
);

\pset footer off
SELECT * FROM after_deletion;
\pset footer on
\set QUIET off

\set QUIET on
\pset footer off
SELECT
    (total_hosts_before_deletion    - total_hosts_after_deletion   ) AS total_hosts_deleted,
    (total_services_before_deletion - total_services_after_deletion) AS total_services_deleted
FROM
    before_deletion,
    after_deletion;
\pset footer on
\set QUIET off

-- (*) rollback (for development purposes, to allow easy re-tries) or commit (for production
--     purposes)
--
\set QUIET on
-- ROLLBACK;
COMMIT;
\set QUIET off

-- That's all, folks!
\q

