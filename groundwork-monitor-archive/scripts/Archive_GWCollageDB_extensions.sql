-- Archive_GWCollageDB_extensions.sql
--
-- This script extends the schema for a runtime "gwcollagedb" database to
-- include extra columns, and changes in indexes (primary keys, unique
-- constraints, and unique indexes), which are necessary to accumulate
-- historical data in the "archive_gwcollagedb" database.
--
-- Essentially, we add "startvalidtime" and "endvalidtime" columns as the last
-- two columns in most of the tables that are periodically archived by copying
-- rows from the runtime database, and we adjust various indexes (all UNIQUE
-- CONSTRAINTs, UNIQUE indexes, and any PRIMARY KEYs that are not formed as
-- single-column, native-to-the-table-and-managed-by-a-table-specific-sequence
-- constructions) to include the "startvalidtime" column as the last column
-- in each respective constraint or index.  The specific modifications needed
-- for each table depend on its intended usage within the archive database,
-- as listed in the row_type designation for each archived table in the
-- log-archive-receive.conf configuration file.
--
-- PostgreSQL does not allow addition of new columns to some arbitrary specified
-- place within existing columns in a table; you can only add new columns at the
-- end.  So when, during an upgrade between GWMEE releases, we add more columns to
-- some of the tables in the gwcollagedb, we must take special steps to maintain
-- compatibility of the runtime and archive databases across the upgrade.  That
-- compatibility is needed so the data-archiving SQL commands can remain relatively
-- simple, keeping corresponding columns in each table in the same order.  Thus those
-- new columns need to be inserted in the archive_gwcollagedb database table before
-- the "startvalidtime" and "endvalidtime" columns we add here.  To make that happen,
-- special logic is used in the pg_migrate_archive_gwcollagedb.pl script.  Even so,
-- for that to work, any new columns in the gwcollagedb database tables must have
-- either default values explicitly specified in the schema, or not be declared NOT
-- NULL, since there will be no old data available to fill in the column values for
-- existing rows in the archive_gwcollagedb database tables.
--
-- Of course, if any of the changes in a newer release modify the fields used in
-- primary keys, unique constraints, or unique indexes, and any of the tables
-- which are extended here are affected, this script will need to be modified to
-- accommodate those changes.
--
-- This model of supporting dynamic changes in the runtime database via extra
-- "startvalidtime" and "endvalidtime" columns in many tables in the archive
-- database only makes sense if there aren't many games played with the data in
-- the runtime database.  Specifically, objects in the runtime database should
-- rarely, if ever, be renamed while using the same unique ID values in their
-- respective tables.  That might be reasonable for ongoing operations, but it
-- would definitely confuse the meanings of such objects within the long-term
-- archived data.  Instead, if you truly want a new name for some object, the
-- old name should be deleted from the runtime database and the new name should
-- be added, along with all desired associations with other tables.

-- Copyright (c) 2013-2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
-- Use is subject to GroundWork commercial license terms.

ALTER TABLE "public"."applicationentityproperty"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "applicationentityproperty_applicationtypeid_entitytypeid_pr_key",
    ADD UNIQUE (applicationtypeid, entitytypeid, propertytypeid, startvalidtime);

ALTER TABLE "public"."applicationtype"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "applicationtype_name_key",
    ADD UNIQUE (name, startvalidtime);


-- The following table requires special handling in the log-archiving software; that is implemented
-- by the auditlog table being marked as untimed_object in the log-archive-receive.conf file.
--
-- Table "public"."auditlog" is a secondary table (in the nomenclature of the log-archive scripting
-- configuration), and as such, no modifications are needed for this table in the archive database.


ALTER TABLE "public"."checktype"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "checktype_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."component"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "component_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."device"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "device_identification_key",
    ADD UNIQUE (identification, startvalidtime);

ALTER TABLE "public"."entitytype"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "entitytype_name_key",
    ADD UNIQUE (name, startvalidtime);


-- A DROP CONSTRAINT clause was used in this ALTER TABLE statement for GWMEE 7.0.X without
-- the IF EXISTS qualifier, but that is no longer really appropriate for fresh creation
-- of archive_gwcollagedb in 7.1.X or later because this constraint no longer exists in
-- gwcollagedb and it is therefore not copied over when archive_gwcollagedb is initially
-- created.  So we just add an IF EXISTS qualifier, to remind ourselves of what used to
-- happen without actually doing anything that would abort the ALTER TABLE command.
ALTER TABLE "public"."host"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT IF EXISTS "host_hostname_key",
    ADD UNIQUE (hostname, startvalidtime);

-- The following commands deal with a formal index, not a simple constraint.  So we cannot
-- use ALTER TABLE clauses to make these adjustments; we must use separate SQL commands.
--
-- The gwcollagedb database now has an index on the "host" table named "host_hostname",
-- that looks like this:
--
--     CREATE UNIQUE INDEX host_hostname ON host USING btree (lower((hostname)::text))
--
-- If we were to create an equivalent index on the archive_gwcollagedb "host" table,
-- it would look like this:
--
--     CREATE UNIQUE INDEX host_lowerhostname_startvalidtime_idx ON host
--         USING btree (lower(hostname), startvalidtime);
--
-- But in fact, there is no reason for the archive_gwcollagedb database copy of this table
-- to impose the same case-folding restriction.  Any restrictions imposed on the runtime
-- database will eventually be reflected in the archive database as updated and/or deleted
-- rows.  And those changes can be stored in the archive database without the extra
-- complications of trying to mirror the restrictive runtime database constraint.  So we
-- simply decline to do that.  Instead, we establish a UNIQUE constraint on (hostname,
-- startvalidtime), using the same ALTER TABLE clause above as we always used in previous
-- releases, without using the lower() function (which is not supported for a constraint
-- anyway, which is why an index is used in the gwcollagedb database).  And that should
-- suffice for archiving purposes.
--
DROP INDEX IF EXISTS "host_hostname";


-- FIX LATER:  hostblacklistid is the PRIMARY KEY for the hostblacklist table, but
-- there is no associated sequence in the database to manage this field.  What
-- software does that job?  Does that mean we need to drop the hostblacklist_pkey
-- constraint and add a revised hostblacklist_pkey primary key (without a name change,
-- as it seems to be customary to not name the fields included in a primary key) on
-- (hostblacklistid, startvalidtime) instead?  If so, we would need to do so not only
-- here, but also in the pg_migrate_archive_gwcollagedb.pl script, we would need to
-- drop the existing primary key and add the new definition.
--
ALTER TABLE "public"."hostblacklist"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "hostblacklist_hostname_key",
    ADD UNIQUE (hostname, startvalidtime);


ALTER TABLE "public"."hostgroup"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "hostgroup_name_key",
    ADD UNIQUE (name, startvalidtime);


-- The following table requires special handling in the log-archiving software; that is implemented by
-- the hostgroupcollection table being marked as timed_association in the log-archive-receive.conf file.
--
-- Table "public"."hostgroupcollection" has no private unique ID.  It is a purely associational table,
-- creating a possibly many-to-many mapping between rows in two other tables.  As such, any given
-- point in the mapping may appear and disappear over time in the runtime database, even while the
-- rows in the associated tables remain extant, and that history must be reflected in the archive
-- database.  So in this case we have to modify the PRIMARY KEY which forms the basis for the
-- linkage in this table between other tables in the database.
ALTER TABLE "public"."hostgroupcollection"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "hostgroupcollection_pkey",
    ADD PRIMARY KEY (hostid, hostgroupid, startvalidtime);


-- FIX LATER:  hostidentityid is the PRIMARY KEY for the hostidentity table, but
-- there is no associated sequence in the database to manage this field.  What
-- software does that job?  Does that mean we need to drop the hostidentity_pkey
-- constraint and add a revised hostidentity_pkey primary key (without a name change,
-- as it seems to be customary to not name the fields included in a primary key) on
-- (hostidentityid, startvalidtime) instead?  If so, we would need to do so not only
-- here, but also in the pg_migrate_archive_gwcollagedb.pl script, we would need to
-- drop the existing primary key and add the new definition.
--
-- Note the following; some ordering of table alterations or other adjustments
-- might be necessary to carry that out.
--
-- gwcollagedb=# alter table hostidentity drop constraint hostidentity_pkey;
-- ERROR:  cannot drop constraint hostidentity_pkey on table hostidentity because other objects depend on it
-- DETAIL:  constraint hostname_ibfk_1 on table hostname depends on index hostidentity_pkey
-- HINT:  Use DROP ... CASCADE to drop the dependent objects too.
--
ALTER TABLE "public"."hostidentity"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "hostidentity_hostname_key",
    ADD UNIQUE (hostname, startvalidtime);

-- I don't know why "hostidentity_hostid" is an explicit UNIQUE INDEX in the gwcollagedb
-- database instead of just an ordinary UNIQUE constraint, but so be it.  We'll make a
-- similar object here in the archive_gwcollagedb database instead of appending another
-- clause to the ALTER TABLE statement above, just for parallelism.
DROP INDEX IF EXISTS "hostidentity_hostid";
CREATE UNIQUE INDEX hostidentity_hostid_startvalidtime_idx ON hostidentity USING btree (hostid, startvalidtime);


ALTER TABLE "public"."hostname"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    -- No "hostname_hostname_key" constraint to drop here, because this table never existed with one.
    ADD UNIQUE (hostname, startvalidtime);

-- The following commands deal with a formal index, not a simple constraint.  So we cannot
-- use ALTER TABLE clauses to make these adjustments; we must use separate SQL commands.
--
-- The gwcollagedb database has an index on the "hostname" table named "hostname_hostname",
-- that looks like this:
--
--     CREATE UNIQUE INDEX hostname_hostname ON hostname USING btree (lower((hostname)::text))
--
-- If we were to create an equivalent index on the archive_gwcollagedb "hostname" table,
-- it would look like this:
--
--     CREATE UNIQUE INDEX hostname_lowerhostname_startvalidtime_idx ON hostname
--         USING btree (lower(hostname), startvalidtime);
--
-- But in fact, there is no reason for the archive_gwcollagedb database copy of this table to
-- impose the same case-folding restriction.  Any restrictions imposed on the runtime database
-- will eventually be reflected in the archive database as updated and/or deleted rows.  And
-- those changes can be stored in the archive database without the extra complications of
-- trying to mirror the restrictive runtime database constraint.  So we simply decline to do
-- that.  Instead, we establish a UNIQUE constraint on (hostname, startvalidtime), using the
-- ALTER TABLE command above, without using the lower() function (which is not supported for
-- a constraint anyway, which is why an index is used in the gwcollagedb database).  And that
-- should suffice for archiving purposes.
--
-- Note that this approach also neatly solves a knotty problem of what "UNIQUE ID KEY FIELDS"
-- field(s) to declare for the "hostname" table in its definition of tertiary_table_attributes
-- in the log-archive-receive.conf config file.  We are treating the "hostname" field as
-- though it were by itself a unique key field in the upstream runtime database, which it
-- effectively is (though it is also more severely restricted there).  We don't need to
-- attempt to use a lower() function as part of the tertiary_table_attributes definition,
-- which doesn't work anyway, at least not without substantial code changes.  But there's no
-- need for such code changes, since the archive database need not be as restrictive as the
-- runtime database.
--
DROP INDEX IF EXISTS "hostname_hostname";


-- The following table requires special handling in the log-archiving software; that is implemented
-- by the hoststatus table being marked as untimed_detail in the log-archive-receive.conf file.
--
-- Table "public"."hoststatus" has no private unique ID.  It is a simple projectional table, creating
-- a mapping from rows in one other (base) table to associated data in several other tables.  As
-- such, any given projection in the mapping may appear and disappear over time in the runtime
-- database, as the corresponding row in the runtime base table appears and disappears.  However,
-- since we will never be deleting rows in the archive-database copy of the base table, the
-- corresponding rows in the archive-database copy of the projectional table will never disappear
-- and will remain unique and not confused with any later-generation copy of a row in the base
-- table, since they always refer to an unvarying unique ID value in the base table, a value
-- which will remain static even if the object named in that base table is removed from the
-- runtime-database copy of the base table and added back using some other unique ID value in that
-- table.  The upshot is that there is no need to add "startvalidtime" and "endvalidtime" columns to
-- the archive-database copy of the "hoststatus" table to distinguish multiple generations of the
-- current rows in this table, nor is there any need to manage such values as data is archived.


-- The following table requires special handling in the log-archiving software; that is implemented
-- by the logmessage table being marked as untimed_object in the log-archive-receive.conf file.
--
-- Table "public"."logmessage" is a secondary table (in the nomenclature of the log-archive scripting
-- configuration), and as such, no modifications are needed for this table in the archive database.
-- Also, this table is special-cased with hardcoded logic when rows are deleted, to retain certain
-- rows over the long term that are still operationally useful (for availability graphing) even
-- though they would have otherwise been aged out of the table.


-- The following table requires special handling in the log-archiving software; that is implemented by
-- the logmessageproperty table being marked as untimed_detail in the log-archive-receive.conf file.
--
-- Table "public"."logmessageproperty" has no private unique ID.  It is a projectional table,
-- creating a mapping from rows in one other (base) table to associated data in generally
-- multiple rows of another table.  As such, any given projection in the mapping may appear and
-- disappear over time in the runtime database, as the corresponding row in the runtime base table
-- appears and disappears.  (The same is true if rows in the runtime associated table disappear,
-- though that would be less likely.)  However, since we will never be deleting rows in the
-- archive-database copy of either the base table or the associated table, the corresponding rows
-- in the archive-database copy of the projectional table will never disappear and will remain
-- unique and not confused with any later-generation copy of a row in either the base table or
-- the associated table, since they always refer to unvarying unique ID values in those tables,
-- values which will remain static even if the objects named in those tables are removed from the
-- runtime-database copies of those tables and added back using some other unique ID values in
-- those tables.  The upshot is that there is no need to add "startvalidtime" and "endvalidtime"
-- columns to the archive-database copy of the "logmessageproperty" table to distinguish multiple
-- generations of the current rows in this table, nor is there any need to manage such values as
-- data is archived.


-- The following table requires special handling in the log-archiving software; that is implemented by
-- the logperformancedata table being marked as untimed_object in the log-archive-receive.conf file.
--
-- Table "public"."logperformancedata" is a secondary table (in the nomenclature of the log-archive
-- scripting configuration), and as such, no modifications are needed for this table in the archive
-- database.


ALTER TABLE "public"."monitorstatus"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "monitorstatus_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."operationstatus"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "operationstatus_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."performancedatalabel"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "performancedatalabel_performancename_key",
    ADD UNIQUE (performancename, startvalidtime);

ALTER TABLE "public"."priority"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "priority_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."propertytype"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "propertytype_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."servicestatus"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "servicestatus_hostid_servicedescription_key",
    ADD UNIQUE (hostid, servicedescription, startvalidtime);

ALTER TABLE "public"."severity"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "severity_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."statetype"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "statetype_name_key",
    ADD UNIQUE (name, startvalidtime);

ALTER TABLE "public"."typerule"
    ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
    ADD COLUMN endvalidtime timestamp without time zone,
    DROP CONSTRAINT "typerule_name_key",
    ADD UNIQUE (name, startvalidtime);

