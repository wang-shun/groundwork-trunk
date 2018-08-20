-- mysql_ganglia_show_duplicates.sql
--
-- Script to check uniqueness constraints in the MySQL-based "ganglia" database
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

select name, clusterid, count(*) from cluster group by name
    having count(*) > 1 order by name;

select name, hostid, count(*) from host group by name
    having count(*) > 1 order by name;

select name, locationid, count(*) from location group by name
    having count(*) > 1 order by name;

select name, metricid, count(*) from metric group by name
    having count(*) > 1 order by name;

select hostid, clusterid, clusterhostid, count(*) from clusterhost group by hostid, clusterid
    having count(*) > 1 order by hostid, clusterid;

select hostid, clusterid, hostinstanceid, count(*) from hostinstance group by hostid, clusterid
    having count(*) > 1 order by hostid, clusterid;

select hostinstanceid, metricid, metricinstanceid, count(*) from metricinstance group by hostinstanceid, metricid
    having count(*) > 1 order by hostinstanceid, metricid;

select hostid, clusterid, metricid, metricvalueid, count(*) from metricvalue group by hostid, clusterid, metricid
    having count(*) > 1 order by hostid, clusterid, metricid;

