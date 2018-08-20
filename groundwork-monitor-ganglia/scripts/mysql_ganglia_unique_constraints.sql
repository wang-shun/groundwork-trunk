-- mysql_ganglia_unique_constraints.sql
--
-- Script to apply uniqueness constraints to the MySQL-based "ganglia" database
-- before attempting to upgrade to a PostgreSQL database, which will already have
-- equivalent constraints in place.  Dealing with these issues on the source system
-- before the upgrade will greatly simplify the upgrade process if any of these
-- constraints are already violated on the source system.
--
-- The point is to find out if the source system has duplicate rows with respect to
-- these constraints.  If any duplicate rows are found, the extra copies will need
-- to be eliminated by effort outside of this script.  We don't really expect to
-- find any duplicates, so we don't have any scripting that will isolate just the
-- duplicate rows for inspection.  You'll need to handle that part yourself, figuring
-- out which rows to keep and which rows to delete, probably depending in part on
-- which rows are being referenced by rows in other tables.

use ganglia;

select 'Adding constraint to table: cluster'  as ''; ALTER TABLE cluster  ADD CONSTRAINT  cluster_name_key UNIQUE (name(255));
select 'Adding constraint to table: host'     as ''; ALTER TABLE host     ADD CONSTRAINT     host_name_key UNIQUE (name(255));
select 'Adding constraint to table: location' as ''; ALTER TABLE location ADD CONSTRAINT location_name_key UNIQUE (name(255));
select 'Adding constraint to table: metric'   as ''; ALTER TABLE metric   ADD CONSTRAINT   metric_name_key UNIQUE (name(255));

select 'Adding constraint to table: clusterhost'    as ''; ALTER TABLE clusterhost    ADD CONSTRAINT           clusterhost_hostid_clusterid_key UNIQUE (hostid, clusterid);
select 'Adding constraint to table: hostinstance'   as ''; ALTER TABLE hostinstance   ADD CONSTRAINT          hostinstance_hostid_clusterid_key UNIQUE (hostid, clusterid);
select 'Adding constraint to table: metricinstance' as ''; ALTER TABLE metricinstance ADD CONSTRAINT metricinstance_hostinstanceid_metricid_key UNIQUE (hostinstanceid, metricid);
select 'Adding constraint to table: metricvalue'    as ''; ALTER TABLE metricvalue    ADD CONSTRAINT  metricvalue_hostid_clusterid_metricid_key UNIQUE (hostid, clusterid, metricid);

