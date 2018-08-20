--
-- show-similar-hosts.sql
--
-- Copyright (c) 2016 GroundWork, Inc.  All rights reserved.
--
-- Be sure to keep this up-to-date!
--
\set version '1.0 of 2016-10-17'
\echo ''
\echo show-similar-hosts.sql version :version
\echo ''

-- GENERAL DESCRIPTION
-- -------------------
--
-- The purpose of this script is to display hosts with similar hostnames,
-- differing only in lettercase, and also any so-called "duplicate" services
-- which are assigned to more than one host in a set of similar hostnames.
-- These must all be cleaned up before an upgrade from GMMEE 7.0.2-SP02 or
-- earlier to GWMEE 7.0.2-SP03, 7.1.0, or later releases.  The cleanup can be
-- handled automatically by the companion merge-similar-hosts.sql script, or by
-- manual work to delete and/or rename specific hosts outside of these scripts.
--
-- If any of the issues found here are left unresolved before an upgrade that
-- crosses the indicated release boundary, the processing of such similar
-- hosts would be handled differently than would be done by the companion
-- merge-similar-hosts.sql script.  A different host may be chosen to retain,
-- and any remaining duplicate services on similar hosts will seriously
-- interfere with the upgrade, silently leaving the system in a broken state.


-- DOCUMENTATION
-- -------------
--
-- See the companion README.show-similar-hosts file for a description of how
-- this script works and how to interpret its output.  The main thing to know is
-- that you need both of the two tables it prints to be empty before you proceed
-- with an upgrade of GroundWork Monitor.  So after you run the merge script,
-- come back and run the show script again to prove the system has been fully
-- cleaned up.


-- HOW TO RUN THIS SCRIPT
-- ----------------------
--
-- This script probes the gwcollagedb database but makes no changes to it.  It
-- is to be run from a terminal window while logged in as the nagios user, with
-- the following command (assuming that a copy of this script has been placed
-- into the /tmp directory):
--
-- /usr/local/groundwork/postgresql/bin/psql -U collage -d gwcollagedb -f /tmp/show-similar-hosts.sql > /tmp/similar_hosts
--
-- We recommend capturing the output in a file as shown, because the data
-- displayed may be voluminuous.


-- OPERATIONAL STEPS
-- -----------------

\echo ''
\echo 'Similar hostnames to resolve'
\echo '----------------------------'
\echo ''

-- Here we are making a choice of which host to prefer for retention.  The
-- choice we implement here is very different from the choice made by the
-- upgrade processing, which prefers uppercase hostnames.  Our criterion here
-- was selected in an attempt to preserve as much event history as possible.
-- See the extensive notes above for an explanation of this choice.
--
SELECT
    h_old.hostname AS preferred_host,
    h_new.hostname AS similar_host_to_delete
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
ORDER BY preferred_host, similar_host_to_delete;

\echo ''
\echo Duplicate services to resolve
\echo -----------------------------
\echo ''

-- There is a pathological case we need to consider.  What if hosts AHOST,
-- Ahost, and ahost all exist, AHOST is chosen for retention, and Ahost and
-- ahost (but not AHOST) have duplicate_service attached?  If we only look
-- at the pairs {AHOST, Ahost} and {AHOST, ahost}, no collisions will be
-- apparent.  But when the upgrade scripting attempts to fold together all of
-- these hosts into AHOST, the duplicate_service will cause a collision as the
-- second secondary host is folded in.  So we need to display this condition
-- in the output here, to ensure that it gets properly cleaned up.  That is
-- accomplished by the following query.  It means that a "preferred_host" in
-- this output might not be listed as the "preferred_host" in the earlier
-- "Similar hostnames to resolve" output, which might be a bit confusing.  But
-- if the duplicate service conflict is eliminated, no matter which one host in
-- the set of similar hosts ends up retaining its existing service, problems
-- with this aspect of an upgrade will also be eliminated.
--
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
    h_preferred.hostname         AS "preferred_host"
    -- , h_preferred.hostid         AS "pref_hostid"

    , h_similar.hostname         AS "similar_host"
    -- , h_similar.hostid           AS "sim_hostid"

    , s_preferred.servicedescription AS "duplicate_service"
    -- , s_preferred.servicestatusid    AS "preferred_service"
    -- , s_duplicate.servicestatusid    AS "duplicate_service_to_delete"
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
-- If there is no collision, uniqueification will be done without problems during
-- migration to the newer release.
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

-- Let's make this human-readable as much as possible, given the possibly messy data.
ORDER BY preferred_host, duplicate_service, similar_host;

-- That's all, folks!
\q

