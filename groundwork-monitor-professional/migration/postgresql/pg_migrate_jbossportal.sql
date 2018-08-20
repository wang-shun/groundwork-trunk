--
-- pg_migrate_jbossportal.sql
--
-- Copyright 2011-2013 GroundWork, Inc. ("GroundWork")
-- All rights reserved.
--

-- This script adjusts the sequences for the jbossportal database, based on the max value
-- for the sequence columns in the jbossportal tables.
-- This is a patch script to fix GWMON-10534. Common symptoms after the upgrade to postgres
-- include missing foundation, nagvis admin tabs, Unable to create new portal pages or add
-- new portlets to the existing pages, etc.
-- This script should be run after successful completion of the
-- "/usr/local/groundwork/core/migration/postgresql/master_migration_to_pg.pl -m" script,
-- i.e., after successful MySQL to PostgreSQL Data Migration.

-- Everything we do in this function must be done conditionally, which is to say idempotently, so
-- as not to cause failure if the object is already in play as intended.  That's really the purpose
-- of a migration script, to take an unknown configuration and bring it into a known state.
CREATE FUNCTION fn_migrate_jbossportal() RETURNS VOID AS $$
DECLARE
	uidTemp varchar;
BEGIN

--
-- Adjust values for existing sequences.
--

PERFORM setval('portal_seq', (select max(pk)+1 from jbp_object_node));
PERFORM setval('portlet_seq', (select max(pk)+1 from ((select max(pk) as pk from jbp_portlet_state) union (select max(pk) as pk from jbp_portlet_state_entry) union (select max(pk) as pk from jbp_portlet_group) union (select max(pk) as pk from jbp_portlet_consumer) union (select max(pk) as pk from jbp_portlet_reg)) t));
PERFORM setval('sec_seq', (select max(pk)+1 from jbp_object_node_sec));
PERFORM setval('user_seq', (select max(uid)+1 from ((select max(jbp_uid) as uid from jbp_users) union (select max(jbp_rid) as uid from jbp_roles)) t));
PERFORM setval('nav_seq', (select max(id)+1 from user_navigation));
PERFORM setval('instance_seq', (select max(pk)+1 from ((select max(pk) as pk from jbp_instance) union (select max(pk) as pk from jbp_instance_per_user) union (select max(pk) as pk from jbp_instance_security)) as t));


SELECT jbp_name INTO uidTemp FROM jbp_roles WHERE jbp_name= 'gdma';
IF (uidTemp IS NULL) THEN
    INSERT INTO jbp_roles (jbp_rid,jbp_name, jbp_displayname)
    VALUES ((SELECT nextval('user_seq')),'gdma','GDMA user');
END IF;

SELECT jbp_uname INTO uidTemp FROM jbp_users WHERE jbp_uname= 'gdma';
IF (uidTemp IS NULL) THEN
    INSERT INTO jbp_users (jbp_uid, jbp_uname, jbp_password, jbp_realemail, jbp_regdate, jbp_viewrealemail, jbp_enabled, jbp_familyname)
    VALUES (
	(SELECT nextval('user_seq')),
	'gdma',
	MD5('gdma'),
	'gdma@example.com',
	NOW(),
	'1',
	'1',
	'DO NOT disable/delete this user! If you change the password, don''t forget to update GDMA clients!'
    );
END IF;

SELECT jbp_uid INTO uidTemp FROM jbp_role_membership WHERE jbp_uid= (select jbp_uid from jbp_users where jbp_uname='gdma');
IF (uidTemp IS NULL) THEN
    INSERT INTO jbp_role_membership (jbp_uid, jbp_rid)
    VALUES ((select jbp_uid from jbp_users where jbp_uname='gdma'), (select jbp_rid from jbp_roles where jbp_name='gdma'));
END IF;

END;
$$ LANGUAGE plpgsql;

SELECT fn_migrate_jbossportal();

DROP FUNCTION IF EXISTS fn_migrate_jbossportal();

