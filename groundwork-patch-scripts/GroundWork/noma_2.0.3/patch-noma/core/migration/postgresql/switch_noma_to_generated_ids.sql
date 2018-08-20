--
-- switch_noma_to_generated_ids.sql
--

-- This is an SQL Script to convert the external-ID values in a "noma" database to reflect as much
-- as possible the values they would have had if the new notifier:generate_IDs config parameter had
-- been in effect since the beginning of time.  The conversion might not be perfect, but it's the
-- best we can do.

-- HOW TO RUN THIS SCRIPT
-- ----------------------
--
-- This script alters the "noma" database to convert existing alert unique-ID values to alternate
-- values that will either be consistent with new internally-generated unique-ID values when the
-- notifier:generate_IDs option is enabled, or moved aside to a safe range that is not likely to
-- conflict with future internally-generated values.  When applying a patch, this script is to be
-- run from a terminal window while logged in as the nagios user, with the following commands
-- (assuming that a copy of this script has been placed into the /tmp directory):
--
--     service groundwork stop noma
--     /usr/local/groundwork/postgresql/bin/psql -U noma -d noma -f /tmp/switch_noma_to_generated_ids.sql
--
-- Once the database is converted, the notifier:generate_IDs option must be set to a true value
-- (such as 1) in the noma/etc/NoMa.yaml configuration file, and then NoMa can be started up:
--
--     service groundwork start noma
--
-- This script will also be run automatically by the GWMEE installer in an upgrade to release 7.2.0
-- or later.  Its actions must therefore be effectively idempotent to the extent that it matters,
-- since we won't know in advance whether or not a patch has been installed over a previous GWMEE
-- release before the upgrade.

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

-- NOTES ON THE DEVELOPMENT OF THIS SCRIPT
-- ---------------------------------------
--
-- This script is rather sensitive from a performance point of view to variations in how the
-- SQL queries are written and sequenced.  We have spent a considerable amount of time in A/B
-- comparison testing to dramatically reduce the execution time of both individual phases and
-- the script execution as a whole.  That's why all the detailed timing data is being spilled
-- out as the script is executing.  If you think you need to change something, be sure to use
-- a very large "noma" database as the testbed for to check for performance regressions.  This
-- is important because this script will be run not just during the application of a NoMa patch
-- to an older GWMEE release, but also during every upgrade to a newer GWMEE release so we know
-- that NoMa will have good clean data to work with thereafter, regardless of whether a patch
-- was applied in an earlier release.  (Note that if in the future we restrict the possible
-- upgrade paths of the base product to always having included the GWMEE 7.2.0 release as an
-- explicit first-install or stopping point along the way, we may be able to skip running this
-- script unconditionally during upgrades to releases after that.)  Slow execution can take
-- many minutes, when the queries are not properly optimized.  Reasonable execution should take
-- under a minute, which is still not great for upgrading a single component, but is at least
-- acceptable.
--
-- There are some general lessons to be learned from our attempt to optimize the performance
-- of this script.  To make sure those lessons do get learned and used again in the future for
-- other database-intensive activities, we list them here.
--
-- (*) When performing massive updates of a table, the use of indexes may be problematic.  In
--     theory they might help to locate the data of interest, but they can also take an
--     enormous amount of time to keep updated as the processing proceeds.  If most of a table
--     will be visited during a pass of updating, the index-maintenance activity will dominate
--     and be a huge drag on performance.  In these kinds of situations, it is therefore
--     worthwhile to test both with and without the indexes in play.  Try dropping them before
--     the updating and re-creating them after updating.  In this regard, pay special attention
--     to foreign key references into the tables you are updating.  Collect performance data
--     both when the indexes are in play and when they are not, and compare timings.
--
-- (*) With respect to dropping and creating the existing indexes, look at this both from the
--     perspective of an individual updating query, and from the perspective of having them in
--     play (or not) across multiple independent queries.  You may want to drop them early in
--     the script and only re-create them late in the script, if multiple queries might benefit
--     from not having them in play.  You may find that re-creating the indexes is an expensive
--     action in itself, and be tempted not to drop them in the first place.  But you need to
--     look at the overall effect on script timing.  Do lots of A/B comparison testing.
--
-- (*) Look for opportunities to share calculations across a series of queries, and factor them
--     out, storing the results into script variables or temporary tables.  This can be a big
--     win for expensive calculations, such as producing large transient tables that are needed
--     by multiple updates.
--
-- (*) Break out complicated queries into separable subparts, using temporary tables or other
--     resources to connect up the parts as they would have been used in the original complex
--     queries.  You can drop such resources as soon as they are no longer needed, if you are
--     concerned about resource consumption.  This approach makes it much easier to measure
--     and optimize the performance of those individual subparts, and quite possibly easier to
--     identify and share partial results across queries.
--
-- (*) Look for possibly redundant activity, such as having duplicate rows.  Look for this in
--     places you might not expect it, because you might be surprised at what you find.
--     Restrict the data to be as small as possible as early as possible in the processing.
--
-- (*) Measuring the performance of individual queries is critical to understanding where all
--     the performance problems lie.  Keep the recording of detailed timings on during the
--     entire process of optimization, as you may see unexpected interactions between component
--     pieces as they are modified and resequenced.  It's best to find such interplay quickly
--     so you don't spend too much time running in circles wondering what changed over the
--     course of a long period of optimizing.
--
-- (*) In addition to timing, also record other basic statistics, such as the number of rows
--     processed.  This lets you gain some insight into the nature of the timing data, and
--     whether or not it looks reasonable.
--
-- (*) You might experiment with the use of a larger-than-default temp_buffers space.  In
--     the present script, that turned out to have no discernable effect, but perhaps under
--     other circumstances it might matter.  At least, it's a tunable parameter in the
--     performance-optimization world.
--
-- (*) In spite of everything else you do, there will be considerable uncontrollable variation
--     (ten or sometimes twenty percent) in execution time of individual query executions and
--     of the overall script between consecutive runs, no matter what you do.  Sometimes you'll
--     see an anomalously short or long timing that you cannot later reproduce.  All you can do
--     is live with it, and try to optimize those portions of the processing that consistently
--     take a long time.
--
-- (*) As you evolve the script and as you observe the re-running of individual parts, somehow
--     improvements in one area may well get balanced by unexplained degradation in other
--     areas.  This might be due to caching or cache eviction/misses or disk-drive layout or
--     other effects we don't know about in any detail.  Just don't get overly concerned; keep
--     your eye on the overall timing, and try lots of experiments even if you're not sure they
--     will work out.  You may eventually stumble into terra incognito and learn something new.

-- ================================================================================================

--
-- Be sure to keep this up-to-date!
--
\set version '2.0 of 2017-09-06'
\echo ''
\echo switch_noma_to_generated_ids.sql version :version
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
DROP FUNCTION IF EXISTS convert_noma_external_id_values();
\set QUIET off

\set QUIET on
CREATE FUNCTION convert_noma_external_id_values() RETURNS VOID AS $$
DECLARE

    began_timestamp timestamp;
    ended_timestamp timestamp;
    conversion_duration interval;

    antecedent_timestamp timestamp;
    subsequent_timestamp timestamp;
    activity_duration interval;

    ante_nl_timestamp timestamp;
    post_nl_timestamp timestamp;
    ante_es_timestamp timestamp;
    post_es_timestamp timestamp;
    ante_tc_timestamp timestamp;
    post_tc_timestamp timestamp;
    nl_duration interval;
    es_duration interval;
    tc_duration interval;

    notification_logs_rows_affected integer;
    escalation_stati_rows_affected integer;
    tmp_commands_rows_affected integer;
    temporary_table_rows_affected integer;

    max_rows_affected integer;
    max_rows_format text;

BEGIN

    SELECT clock_timestamp() INTO began_timestamp;

    -- What we want to end up with is the notification_stati.id field generally serving as the unique ID
    -- for a given host or service, and the tmp_commands.external_id field matching up with that value.
    -- There are also a couple of other fields in other tables that need to be updated as well.
    --
    -- Ideally, it would suffice to simply shut down NoMa, change all the existing incident ID values
    -- in the database to instead be the corresponding notification_stati.id values, and start up NoMa
    -- again.  (That would be the ideal situation, but in practice we find that some rows in other
    -- tables might not correspond to any rows in the notification_stati table.  So any such ID values
    -- will need to be left as-is.)  No further adjustment would be needed to the notification_stati row
    -- sequencing to bump up its next value.
    --
    -- So the question is, in what tables and fields is the unique notification ID stored, that it would
    -- need to be adjusted during a switchover to internally-generated unique ID values?
    --
    -- We find the following fields that definitely contain the external ID (unique ID, notification ID)
    -- as originally provided by the alerting agent via the -u option:
    --
    --     tmp_commands.external_id
    --     notification_logs.incident_id   (with some exceptions not matching tmp_commands.external_id)
    --     escalation_stati.incident_id
    --
    -- Other fields in various tables contain similar-looking numbers, but they are not the same as
    -- these specific ID values.
    --
    -- The values we are likely to encounter in an existing database are of two forms:
    --
    -- * low-numbered values, probably originating as the Nagios $HOSTNOTIFICATIONID$ macro
    --   or $SERVICENOTIFICATIONID$ value (both very poor choices to begin with) or the Nagios
    --   $HOSTPROBLEMID$ or $SERVICEPROBLEMID$ values (reasonable and acceptable macro values
    --   to pass as the alert-script -u option).
    --
    -- * high-numbered values, representing a 10-digit UNIX epoch timestamp appended by 1 to 5 random
    --   digits.  (The number of appeneded digits should have been normalized to always be 5; we have
    --   done so in our corrected copy of NoMa.)
    --
    -- As part of our processing, we map certain unique-ID values (that we want to move out of the way
    -- to avoid possible future collisions) to a range of 10-digit numbers which should never collide
    -- with either these existing ranges or with any future internally-generated unique IDs.  This range
    -- starts with 1000000000 (that is, 1,000,000,000).  Being a 10-digit number representing a UNIX
    -- epoch timestamp back in 2001, before NoMa was even born, these remapped values cannot conflict
    -- with any previously generated random values, which would have at least one additional digit
    -- appended, and also cannot reasonably conflict with low-numbered values from Nagios.
    --
    -- Here is the update process:
    --
    -- (1) Move all tmp_commands.external_id values less than 10000000000 to their existing values
    --     plus 1000000000.  This will get them into a numbering range which will be less than any
    --     auto-generated unique-ID values, so as not to be confused with them, while being greater
    --     than any future generated ID values if they do not get renumbered in the rest of this
    --     process.  That will effectively block future conflicts from happening.
    --
    -- (2) Move all notification_logs.incident_id values according to the same rule.
    --
    -- (3) Move all escalation_stati.incident_id values according to the same rule.
    --
    -- (4) Take the tmp_commands.external_id value as the definitive unique-ID value that all others are
    --     to be matched to.
    --
    -- (5) If the notification_logs.incident_id has a matching tmp_commands.external_id, map the
    --     notification_logs.incident_id value to the corresponding notification_stati.id value (by
    --     matching host and service name).
    --
    -- (6) If the escalation_stati.incident_id has a matching tmp_commands.external_id, map the
    --     escalation_stati.incident_id value to the corresponding notification_stati.id value (by
    --     matching host and service name).
    --
    -- (7) Map the tmp_commands.external_id value to the corresponding notification_stati.id value (by
    --     matching host and service name).
    --
    -- (8) Leave everything else alone.

    -- ================================================================================================

    -- (-1):  In some of the updates below, we are joining to the tmp_commands table on the host
    -- and service fields, but the standard database has no index on those fields.  I am therefore
    -- somewhat concerned about the efficiency of these operations.  There is no definitive evidence
    -- that an index would help, but possibly a customer site might have a rather large historical
    -- database that could take a long time to convert.  To give PostgreSQL every possible opportunity
    -- to optimize these operations, we consider creating a temporary index on the tmp_commands table
    -- for these fields (and test to see if that actually does help).  This is the only table I'm
    -- concerned with; other sensible indexes should already be part of the "noma" database.  The
    -- index we create here is apparently not needed by the NoMa code itself, which is why we don't
    -- just create it on a permanent basis outside of this script.
    --
    -- Though this is to some extent a schema change, it does not seem to violate the sanctity of the
    -- transaction within which this function will be executed.  If the transaction fails, the index
    -- will be dropped automatically.  However, PostgreSQL seems not to have a CREATE TEMPORARY INDEX
    -- variant that we can use (for a non-TEMPORARY table), so we do have code to drop it explicitly
    -- at the end of this function.
    --
    -- PostgreSQL 9.4.X has no "CREATE INDEX IF NOT EXISTS" statement; that form is not available until
    -- PostgreSQL 9.5.x.  But the to_regclass() function for getting the OID of the named relation or
    -- NULL if it does not exist, used here to effectively test for the existence of an index without
    -- throwing an exception, is available as of PostgreSQL 9.4.X.
    --
    -- NOTE:  I thought that creation of this extra temporary index would help speed up execution of this
    -- script.  In fact, A/B comparison testing shows that having this extra index in play significantly
    -- slows down overall execution, possibly because we're doing massive updates on the tables and the
    -- extra effort to keep an additional index up-to-date kills any advantage the index might otherwise
    -- provide.  So we no longer have this in play.
    --
    IF FALSE THEN
	IF to_regclass('tmp_commands_host_service') IS NULL THEN
	    SELECT clock_timestamp() INTO antecedent_timestamp;
	    RAISE INFO 'Began temporary index creation at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	    CREATE INDEX tmp_commands_host_service ON tmp_commands USING btree (host, service);
	    SELECT clock_timestamp() INTO subsequent_timestamp;
	    SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	    RAISE INFO 'Ended temporary index creation at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	    RAISE INFO E'      Temporary index creation activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
	END IF;
    END IF;

    -- (0):  Check to see if this script has been run before, with a useful but non-definitive test.
    -- If you get a (non-fatal) INFO message here, that will be an indication that this script has been
    -- run before.  The logic here is slightly forgiving and allows this script to be run up to about
    -- nine times before this becomes problematic.  This is a convenience warning; if you convert too
    -- many times, it will silently stop raising any alarm.  So you shouldn't just repeatedly convert
    -- the same database with abandon.  Eventually, once we know that all GWMEE upgrade paths have run
    -- through this script at least once, we will stop invoking it during an upgrade, and any issues
    -- with the number of times this script has been run will be sidestepped.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began converted-data detection at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	PERFORM
	    incident_id
	FROM
	    notification_logs
	WHERE
	    incident_id >= 1000000000
	AND incident_id < 10000000000
	;
	IF FOUND THEN
	    RAISE INFO 'Found some previously converted NoMa data:  some notification_logs rows have incident_id values in the reserved range.';
	END IF;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended converted-data detection at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Converted-data detection activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    -- Experimentation shows that dropping these indexes can be a HUGE win (saving up to several minutes) when
    -- remapping unique IDs in the notification_logs table, during the initial conversion.  It has perhaps a
    -- modest cost (around 10 seconds) in later runs.  So on balance, we want to keep this manipulation in play.
    --
    -- This is placed after the previously-converted test above, because that test ought to be able to use one
    -- of these indexes without accessing the table at all.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index deletion from the notification_logs table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	DROP INDEX IF EXISTS public.notification_logs_unique_id;
	DROP INDEX IF EXISTS public.notification_logs_incident_id;
	DROP INDEX IF EXISTS public.notification_logs_host_service;
	ALTER TABLE IF EXISTS ONLY public.notification_logs DROP CONSTRAINT IF EXISTS notification_logs_pkey;

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index deletion from the notification_logs table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index deletion from the notification_logs table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index deletion from the escalation_stati table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	DROP INDEX IF EXISTS public.escalation_stati_incident_id;
	DROP INDEX IF EXISTS public.escalation_stati_host_service;
	ALTER TABLE IF EXISTS ONLY public.escalation_stati DROP CONSTRAINT IF EXISTS escalation_stati_pkey;

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index deletion from the escalation_stati table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index deletion from the escalation_stati table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index deletion from the tmp_commands table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	ALTER TABLE IF EXISTS ONLY public.tmp_active DROP CONSTRAINT IF EXISTS tmp_active_ibfk_1;

	DROP INDEX IF EXISTS public.tmp_commands_external_id;
	ALTER TABLE IF EXISTS ONLY public.tmp_commands DROP CONSTRAINT IF EXISTS tmp_commands_id_key;

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index deletion from the tmp_commands table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index deletion from the tmp_commands table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    -- (1), (2), (3):  Move low-numbered unique incident ID values out of the way of both high-numbered
    -- values originating in timestamp.random construction and low-numbered values probably originating
    -- from external sources.  This should leave the field clear for future auto-incremented values of
    -- the notification_stat.id field to be used as stable incident ID values for specific host/service
    -- pairs as long as they remain in non-UP/OK states.  This transform preserves matches of ID values
    -- across tables.  Further transforms will re-map values to notification_stati.id values when that
    -- is possible, possibly leaving some fraction of the original values still in this middle range.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began existing-unique-ID shifting at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	SELECT clock_timestamp() INTO ante_nl_timestamp;
	update notification_logs set incident_id = incident_id + 1000000000 where incident_id < 10000000000; GET DIAGNOSTICS notification_logs_rows_affected = ROW_COUNT;
	SELECT clock_timestamp() INTO post_nl_timestamp;

	SELECT clock_timestamp() INTO ante_es_timestamp;
	update escalation_stati  set incident_id = incident_id + 1000000000 where incident_id < 10000000000; GET DIAGNOSTICS escalation_stati_rows_affected  = ROW_COUNT;
	SELECT clock_timestamp() INTO post_es_timestamp;

	SELECT clock_timestamp() INTO ante_tc_timestamp;
	update tmp_commands      set external_id = external_id + 1000000000 where external_id < 10000000000; GET DIAGNOSTICS tmp_commands_rows_affected      = ROW_COUNT;
	SELECT clock_timestamp() INTO post_tc_timestamp;

	max_rows_affected = GREATEST( notification_logs_rows_affected, escalation_stati_rows_affected, tmp_commands_rows_affected );
	max_rows_format = CASE
	    WHEN max_rows_affected <        10 THEN           '9'
	    WHEN max_rows_affected <       100 THEN          '99'
	    WHEN max_rows_affected <      1000 THEN         '999'
	    WHEN max_rows_affected <     10000 THEN       '9,999'
	    WHEN max_rows_affected <    100000 THEN      '99,999'
	    WHEN max_rows_affected <   1000000 THEN     '999,999'
	    WHEN max_rows_affected <  10000000 THEN   '9,999,999'
	    WHEN max_rows_affected < 100000000 THEN  '99,999,999'
	    ELSE                                    '999,999,999'
	END;

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (   post_nl_timestamp -    ante_nl_timestamp) INTO       nl_duration;
	SELECT (   post_es_timestamp -    ante_es_timestamp) INTO       es_duration;
	SELECT (   post_tc_timestamp -    ante_tc_timestamp) INTO       tc_duration;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;

	RAISE INFO 'Ended existing-unique-ID shifting at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO  '      Existing-unique-ID shifting activity took %', to_char(activity_duration, 'HH24:MI:SS.US');
	RAISE INFO  '      Existing-unique-ID shifting activity took % for the notification_logs table', to_char(nl_duration, 'HH24:MI:SS.US');
	RAISE INFO  '      Existing-unique-ID shifting activity took % for the escalation_stati  table', to_char(es_duration, 'HH24:MI:SS.US');
	RAISE INFO  '      Existing-unique-ID shifting activity took % for the tmp_commands      table', to_char(tc_duration, 'HH24:MI:SS.US');
	RAISE INFO  '      Existing-unique-ID shifting in the notification_logs table updated % rows',   to_char(notification_logs_rows_affected, max_rows_format);
	RAISE INFO  '      Existing-unique-ID shifting in the escalation_stati  table updated % rows',   to_char(escalation_stati_rows_affected,  max_rows_format);
	RAISE INFO E'      Existing-unique-ID shifting in the tmp_commands      table updated % rows\n', to_char(tmp_commands_rows_affected,      max_rows_format);
    END IF;

    -- (4):  There is nothing to do yet for tmp_commands.external_id; see step (7).  For the time being,
    -- we need to preserve the content of the tmp_commands table so it can be used as an intermediary
    -- while updating the notification_logs and escalation_stati tables.

    -- Increasing the temp_buffers allocation for this session was an experiment that turned out to have
    -- no discernable beneficial effect, and possibly a slightly negative impact.  (It's hard to tell,
    -- because overall run times are fairly variable over some range of times.)  So we leave it disabled.
    --
    -- SET temp_buffers = '100MB';

    -- (5):  Update notification_logs.incident_id if possible, to its final value.

    -- The action is tricky here.  We need to join the notification_logs table to the notification_stati
    -- table, using the tmp_commands table as an intermediary.  But some rows in the notification_logs
    -- may have no corresponding row(s) in the tmp_commands table, and some rows in the tmp_commands
    -- table may have no corresponding row in the notification_stati table.  So the SQL statement used
    -- to perform this updating must take those possibilities into account.  Otherwise, we could end up
    -- setting the notification_logs.incident_id field in some rows to a NULL value.

    -- It's rather mysterious, but this query runs much faster when all the relevant tmp_commands
    -- indexes are gone.  A/B comparison testing is used to establish that kind of understanding.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began creation of the temporary distinct_tc table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	CREATE TEMPORARY TABLE distinct_tc AS (
	    SELECT DISTINCT
		external_id,
		host,
		service
	    FROM
		tmp_commands
	);
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended creation of the temporary distinct_tc table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Creation of the temporary distinct_tc table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    -- If we're not extremely careful, construction of the following temporary table turns out to be a fairly
    -- expensive operation when the tmp_commands table is large.  And we will use the LEFT JOIN which this
    -- table encapsulates more than once during the following UPDATEs.  So rather than burying it inside
    -- those separate UPDATEs, either in WITH clauses or in FROM clauses, we create and populate this table
    -- just once.  It will only be valid for its intended use until we update the tmp_commands.external_id
    -- values at the end of this script, but that's long enough to get all the other updates done.
    --
    -- It turns out there's another good reason to have this join done separately.  The tmp_commands table
    -- can have many (we've seen hundreds) of rows with the same (external_id, host, service) tuples, so the
    -- "SELECT DISTINCT" collapsing of duplicate rows from the basic LEFT JOIN is a key part of optimizing
    -- the later updates.  So we would need to construct either the FROM join in an UPDATE very carefully,
    -- using a SELECT DISTINCT subquery instead of just a bare tmp_commands table reference, or use a WITH
    -- clause employing a similar construction.  But by the time we get that complex, it's better overall
    -- to just pull out the table creation into a separate statement.  That allows us to experiment with
    -- various forms of the table-creation query, to time exactly how long that particular part of the
    -- overall update processing runs under varying conditions (e.g., having or not having various indexes
    -- present), and to keep the timing data for the table creation separate from the timing of the updates
    -- that reference the temporary table in service of modifying some other (permanent) table.
    --
    -- On a separate note, there arises a question of how to perform the data joining here and the later
    -- data comparisons in UPDATE statements if either the host or service is a NULL in the source tables.
    -- In theory, we would then want to use "IS NOT DISTINCT FROM" operators rather than "=" operators
    -- when comparing fields from the joined tables.  Otherwise, we might be in some danger of having later
    -- UPDATE actions set unique-ID values to a NULL where this is inappropriate.  Alas, that turns out to
    -- make the query unbearably slow (though perhaps that might be less so if we had indexes in play; we
    -- haven't tried that).  We are saved, though, because it turns out that NoMa uses empty (zero-length)
    -- strings for missing host and service values instead of NULLs.  That means we can just use simple "="
    -- comparisons, and both the table-creation query here and later UPDATE data comparisons will run fast.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began creation of the temporary tc_ns table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	CREATE TEMPORARY TABLE tc_ns AS (
	    -- The DISTINCT keyword in this construction is absolutely critical for overall performance.
	    -- It can eliminate large numbers of duplicate rows in the joined table, which would cause an
	    -- inordinate number of redundant actions in the subsequent UPDATEs.  For best performance,
	    -- we need a DISTINCT either here in this outer SELECT or in an effective subselect in the
	    -- FROM clause (or both).  Putting a DISTINCT in the subselect restricts the data as early as
	    -- possible, so we effectively use that (by means of referencing the distinct_tc table, which
	    -- embodies the application of DISTINCT that we need) to avoid excess computation.
	    SELECT
		-- For host and service in the join-result table, we want NULL values here instead of
		-- empty strings if the LEFT JOIN produces nothing, so the subsequent UPDATEs won't
		-- find a NULL ns_id field and try to use that for updating.  Hence we use ns fields
		-- here for host and service instead of the corresponding distinct_tc fields.
	        distinct_tc.external_id AS tc_external_id,
		ns.host                 AS ns_host,
		ns.service              AS ns_service,
		ns.id                   AS ns_id
	    FROM
		-- Using the distinct_tc table here instead of the apparently simpler "tmp_commands tc",
		-- but using the temporary table with its de-duplicated data chokes off the repeated
		-- data as early as possible in the evaluation chain.  Using the temporaryy table here
		-- makes using DISTINCT in the outer SELECT redundant, so you don't see it there.
		distinct_tc -- that is, (SELECT DISTINCT external_id, host, service from tmp_commands)
		LEFT JOIN notification_stati ns USING (host, service)
	);
	-- I considered creating an index on this temporary table, to help with later performance.  As
	-- shown, I had in mind an index containing at least the first three fields, and perhaps all
	-- four if that would allow later UPDATEs to just use the index and not the actual data pages.
	-- But surprisingly, tests showed it was of no help.
	-- CREATE INDEX tc_ns_index ON tc_ns USING btree (tc_external_id, ns_host, ns_service, ns_id);
	GET DIAGNOSTICS temporary_table_rows_affected = ROW_COUNT;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended creation of the temporary tc_ns table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO '      Creation of the temporary tc_ns table activity took %', to_char(activity_duration, 'HH24:MI:SS.US');
	RAISE INFO E'      Creation of the temporary tc_ns table stored % rows\n', to_char(temporary_table_rows_affected, 'FM999,999,999');
    END IF;

    -- This query relates to claim (a) below, and should yield few rows, if any.  But even if it does
    -- find some duplicate use of the same external_id for different host/service combinations, that
    -- won't disable our ability to unambiguously assign a unique ID to existing rows in various tables.
    -- So this query is more informational than anything else, serving mostly to confirm or deny our
    -- suspicions about the distribution of pre-existing data in the database.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began shared-unique-ID detection at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	PERFORM
	    external_id,
	    count(*)
	FROM
	    distinct_tc -- that is, ( SELECT DISTINCT external_id, host, service FROM tmp_commands ) AS distinct_triples
	GROUP BY external_id
	HAVING count(*) > 1
	;
	IF FOUND THEN
	    RAISE INFO 'Found unusual NoMa data:  some tmp_command rows with identical external_id have different host and/or service.';
	END IF;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended shared-unique-ID detection at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Shared-unique-ID detection activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    -- This query should yield 0 rows, thereby proving claim (b) below.
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began multiple-unique-ID detection at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	PERFORM
	    host,
	    service,
	    count(*)
	FROM
	    notification_stati
	GROUP BY host, service
	HAVING count(*) > 1
	;
	IF FOUND THEN
	    RAISE EXCEPTION 'Found bad NoMa data:  have multiple notification_stati rows with identical host and service.';
	END IF;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended multiple-unique-ID detection at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Multiple-unique-ID detection activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began unique-ID remapping in the notification_logs table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	UPDATE notification_logs nl
	    SET incident_id = ns_id
	FROM
	    tc_ns
	WHERE
	    -- Ensure that we have a proper match between the notification_logs and tmp_commands tables.
	    -- It's true that there may be more than one tc row with the same external_id field, so if we
	    -- are not careful, this clause might match more than one row in the "tc left join ns" temporary
	    -- table.  We have in fact been very careful about that, because matching more than one row
	    -- in the temporary table has been proven to be a huge drag on performance.  That's why we
	    -- effectively used the DISTINCT constraint in the earlier query that constructed the tc_ns
	    -- table we reference here, to reduce the amount of data we will process here.  Logically, we
	    -- would expect that (a) all such rows will have exactly the same tc host and service fields,
	    -- and (b) there will be at most one matching ns row with those host and service fields, so (c)
	    -- there will be no ambiguity as to what ns.id value to update the nl.incident_id field with, if
	    -- a particular nl row can be updated at all.  We test those claims using earlier programmatic
	    -- queries in this script.  Claim (a) turns out to sometimes be false, but in fact that does
	    -- not impede our ability here to find the one unique ID value to update the notification_logs
	    -- table with, so it is not a problem in practice.  Claim (b) is an absolute necessity, and
	    -- thus the verification above makes a failure there an abortive exception instead of just an
	    -- informational message.
	    --
	    -- I don't know what kind of error would be thrown if there were some degree of ambiguity as to
	    -- what value to use to update a given row in the notification_logs table.  Possibly, we might
	    -- simply experience successive updates that would all overwrite one another, and some random
	    -- one of those values would be the final answer.
	    nl.incident_id = tc_external_id

	    -- Also ensure that we have a proper match between the tmp_commands and notification_stati
	    -- tables.  If we instead equated nl and tc table fields here, that would ignore the fact that
	    -- if the tc table has no matching ns table row, the ns fields will be NULL, which would mean
	    -- that the ns.id field would also be NULL.  We need to exclude such notification_logs rows from
	    -- being updated.  The same things could have been done by comparing nl and tc host and service
	    -- fields and then also adding any or all of "and ns.id IS NOT NULL", "and ns.host IS NOT NULL",
	    -- or "and ns.service IS NOT NULL" clauses to further filter the results.  But we choose the
	    -- simpler approach without those extra clauses, even if it does require this long comment to
	    -- ensure accuracy.
	AND nl.host    = ns_host
	AND nl.service = ns_service
	;
	GET DIAGNOSTICS notification_logs_rows_affected = ROW_COUNT;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended unique-ID remapping in the notification_logs table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO '      Unique-ID remapping in the notification_logs table activity took %', to_char(activity_duration, 'HH24:MI:SS.US');
	RAISE INFO E'      Unique-ID remapping in the notification_logs table updated % rows\n', to_char(notification_logs_rows_affected, 'FM999,999,999');
    END IF;

    -- (6):  Update escalation_stati.incident_id if possible, to its final value.

    -- The construction here parallels that of updating the notification_logs table, just above, for all
    -- the same reasons.
    --
    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began unique-ID remapping in the escalation_stati table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	UPDATE escalation_stati es
	    SET incident_id = ns_id
	FROM
	    tc_ns
	WHERE
	    es.incident_id = tc_external_id
	AND es.host        = ns_host
	AND es.service     = ns_service
	;
	GET DIAGNOSTICS escalation_stati_rows_affected = ROW_COUNT;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended unique-ID remapping in the escalation_stati table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO '      Unique-ID remapping in the escalation_stati table activity took %', to_char(activity_duration, 'HH24:MI:SS.US');
	RAISE INFO E'      Unique-ID remapping in the escalation_stati table updated % rows\n', to_char(escalation_stati_rows_affected, 'FM999,999,999');
    END IF;

    -- (7):  Update tmp_commands.external_id if possible, to its final value.

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began unique-ID remapping in the tmp_commands table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	update tmp_commands tc
	    set external_id = ns.id
	from
	    notification_stati ns
	where
	    ns.host    = tc.host
	and ns.service = tc.service
	;
	GET DIAGNOSTICS tmp_commands_rows_affected = ROW_COUNT;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended unique-ID remapping in the tmp_commands table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO '      Unique-ID remapping in the tmp_commands table activity took %', to_char(activity_duration, 'HH24:MI:SS.US');
	RAISE INFO E'      Unique-ID remapping in the tmp_commands table updated % rows\n', to_char(tmp_commands_rows_affected, 'FM999,999,999');
    END IF;

    -- (8):  Leave everything else alone.  Which is to say, put back everything else we moved aside
    --       for the duration of the data modifications in the interest of better performance.

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index creation on the tmp_commands table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	ALTER TABLE ONLY tmp_commands ADD CONSTRAINT tmp_commands_id_key UNIQUE (id);
	CREATE INDEX tmp_commands_external_id ON tmp_commands USING btree (external_id);

	ALTER TABLE ONLY tmp_active ADD CONSTRAINT tmp_active_ibfk_1 FOREIGN KEY (command_id) REFERENCES tmp_commands(id) ON UPDATE RESTRICT ON DELETE CASCADE;

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index creation on the tmp_commands table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index creation on the tmp_commands table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index creation on the escalation_stati table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	ALTER TABLE ONLY escalation_stati ADD CONSTRAINT escalation_stati_pkey PRIMARY KEY (id);
	CREATE INDEX escalation_stati_host_service ON escalation_stati USING btree (host, service);
	CREATE INDEX escalation_stati_incident_id ON escalation_stati USING btree (incident_id);

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index creation on the escalation_stati table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index creation on the escalation_stati table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    IF TRUE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began index creation on the notification_logs table at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');

	ALTER TABLE ONLY notification_logs ADD CONSTRAINT notification_logs_pkey PRIMARY KEY (id);
	CREATE INDEX notification_logs_host_service ON notification_logs USING btree (host, service);
	CREATE INDEX notification_logs_incident_id ON notification_logs USING btree (incident_id);
	CREATE INDEX notification_logs_unique_id ON notification_logs USING btree (unique_id);

	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended index creation on the notification_logs table at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Index creation on the notification_logs table activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    -- (9):  Clean up the scaffolding we used.

    -- Since experimentation showed that having an extra index in play on the tmp_commands table was a hindrance
    -- rather than a help, we no longer drop that index here.
    IF FALSE THEN
	SELECT clock_timestamp() INTO antecedent_timestamp;
	RAISE INFO 'Began temporary index deletion at %', to_char(antecedent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	DROP INDEX IF EXISTS tmp_commands_host_service;
	SELECT clock_timestamp() INTO subsequent_timestamp;
	SELECT (subsequent_timestamp - antecedent_timestamp) INTO activity_duration;
	RAISE INFO 'Ended temporary index deletion at %', to_char(subsequent_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
	RAISE INFO E'      Temporary index deletion activity took %\n', to_char(activity_duration, 'HH24:MI:SS.US');
    END IF;

    RAISE INFO E'=====================================================================\n';

    SELECT clock_timestamp() INTO ended_timestamp;
    SELECT (ended_timestamp - began_timestamp) INTO conversion_duration;
    RAISE INFO 'Began notification unique-ID conversion at %', to_char(began_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
    RAISE INFO 'Ended notification unique-ID conversion at %', to_char(ended_timestamp, 'YYYY-MM-DD HH24:MI:SS.US');
    RAISE INFO '      Notification unique-ID conversion full run took %', to_char(conversion_duration, 'HH24:MI:SS.US');

END;
$$ LANGUAGE plpgsql;
\set QUIET off

\set QUIET on
begin transaction;
\pset tuples_only on
-- There seems not to be any way to prevent this SELECT from printing two blank lines, despite our
-- having turned tuples-only mode on.  I suppose we could redirect the output to /dev/null, but that
-- seems excessive and we don't want to suppress messages that might appear upon failure.
SELECT convert_noma_external_id_values();
\pset tuples_only off
-- For script-debuggng purposes, uncomment the following line so we don't make any changes to the data.
-- rollback;
commit;
\set QUIET off

\set QUIET on
DROP FUNCTION IF EXISTS convert_noma_external_id_values();
\set QUIET off

-- That's all, folks!
\echo NoMa data has been converted.
\echo ''
\q

