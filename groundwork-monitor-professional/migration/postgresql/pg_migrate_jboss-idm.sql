
--
-- Copyright 2013-2017 GroundWork, Inc. ("GroundWork")
-- All rights reserved.
--
CREATE FUNCTION fn_migrate_jboss_idm() RETURNS VOID AS $$
BEGIN
	-- 7.1.0 change. Renaming memberships in portal
	update jbid_io_rel_name set name='gw-portal-administrator' where name='GWRoot';
	update jbid_io_rel_name set name='gw-monitoring-administrator' where name='GWAdmin';
	update jbid_io_rel_name set name='gw-monitoring-operator' where name='GWOperator';
	update jbid_io_rel_name set name='gw-portal-user' where name='GWUser';

	update gw_ext_role_attributes set jbp_name='gw-portal-administrator' where jbp_name='GWRoot';
	update gw_ext_role_attributes set jbp_name='gw-monitoring-administrator' where jbp_name='GWAdmin';
	update gw_ext_role_attributes set jbp_name='gw-monitoring-operator' where jbp_name='GWOperator';
	update gw_ext_role_attributes set jbp_name='gw-portal-user' where jbp_name='GWUser';
	-- deleting unwanted memberships
	delete from jbid_io_rel where name=(select id from jbid_io_rel_name where name='wsuser');
	delete from jbid_io_rel_name_props where prop_id=(select id from jbid_io_rel_name where name='wsuser');
	delete from jbid_io_rel_name where name='wsuser';
	delete from jbid_io_rel where name=(select id from jbid_io_rel_name where name='gdma');
	delete from jbid_io_rel_name_props where prop_id=(select id from jbid_io_rel_name where name='gdma');
	delete from jbid_io_rel_name where name='gdma';
	delete from gw_ext_role_attributes where jbp_name in ('wsuser','gdma');

  -- Check for the existence of permanent tables, and make much
  -- of the rest of this logic conditional on that test.
  IF NOT EXISTS (
	SELECT 1
	FROM   pg_catalog.pg_class c
	JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace
	WHERE  n.nspname = 'public'
	AND    c.relname = 'gw_resources'
	AND    c.relkind = 'r'
    )
  THEN
    CREATE TABLE IF NOT EXISTS gw_ext_role_permissions (
        jbp_rid bigint NOT NULL,
        resource_id smallint NOT NULL,
        perm_id smallint
    );


    ALTER TABLE public.gw_ext_role_permissions OWNER TO jboss;

    --
    -- Name: gw_permissions; Type: TABLE; Schema: public; Owner: jboss; Tablespace:
    --

    CREATE TABLE IF NOT EXISTS gw_permissions (
        perm_id smallint NOT NULL,
        action character varying(255)
    );


    ALTER TABLE public.gw_permissions OWNER TO jboss;

    --
    -- Name: gw_resources; Type: TABLE; Schema: public; Owner: jboss; Tablespace:
    --

    CREATE TABLE IF NOT EXISTS gw_resources (
        resource_id smallint NOT NULL,
        name character varying(255)
    );


    ALTER TABLE public.gw_resources OWNER TO jboss;

    --
    -- Name: gw_ext_role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace:
    --

    ALTER TABLE ONLY gw_ext_role_permissions
        ADD CONSTRAINT gw_ext_role_permissions_pkey PRIMARY KEY (jbp_rid, resource_id);


    --
    -- Name: gw_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace:
    --

    ALTER TABLE ONLY gw_permissions
        ADD CONSTRAINT gw_permissions_pkey PRIMARY KEY (perm_id);

    --
    -- Name: gw_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace:
    --

    ALTER TABLE ONLY gw_resources
        ADD CONSTRAINT gw_resources_pkey PRIMARY KEY (resource_id);


    --
    -- Name: fkc59694883992457d; Type: FK CONSTRAINT; Schema: public; Owner: jboss
    --

    ALTER TABLE ONLY gw_ext_role_permissions
        ADD CONSTRAINT fkey_gw_resources_resource_id FOREIGN KEY (resource_id) REFERENCES gw_resources(resource_id);


    --
    -- Name: fkc59694888645c0d8; Type: FK CONSTRAINT; Schema: public; Owner: jboss
    --

    ALTER TABLE ONLY gw_ext_role_permissions
        ADD CONSTRAINT fkey_gw_ext_role_att_jbp_id FOREIGN KEY (jbp_rid) REFERENCES gw_ext_role_attributes(jbp_rid);


    --
    -- Name: fkc5969488eca0005c; Type: FK CONSTRAINT; Schema: public; Owner: jboss
    --

    ALTER TABLE ONLY gw_ext_role_permissions
        ADD CONSTRAINT fkey_gw_permissions_perm_id FOREIGN KEY (perm_id) REFERENCES gw_permissions(perm_id);

    -- Now copy the data
    insert into gw_resources select 1,'Cacti' where not exists (select name from gw_resources where name='Cacti');
    insert into gw_resources select 2,'Nagios' where not exists (select name from gw_resources where name='Nagios');
    insert into gw_resources select 3,'Nagvis' where not exists (select name from gw_resources where name='Nagvis');
    insert into gw_resources select 4,'BSM-Admin' where not exists (select name from gw_resources where name='BSM-Admin');
    insert into gw_resources select 5,'BSM-User' where not exists (select name from gw_resources where name='BSM-User');
    insert into gw_resources select 6,'BIRT-Reports' where not exists (select name from gw_resources where name='BIRT-Reports');
    insert into gw_resources select 7,'Performance' where not exists (select name from gw_resources where name='Performance');
    insert into gw_resources select 8,'Performance-Reports' where not exists (select name from gw_resources where name='Performance-Reports');
    insert into gw_resources select 9,'Monarch' where not exists (select name from gw_resources where name='Monarch');
    insert into gw_resources select 10,'NeDi' where not exists (select name from gw_resources where name='NeDi');
    insert into gw_resources select 11,'CloudHub' where not exists (select name from gw_resources where name='CloudHub');
    insert into gw_resources select 12,'Grafana' where not exists (select name from gw_resources where name='Grafana');
    insert into gw_permissions select 1,'allow' where not exists (select action from gw_permissions where action='allow');
    insert into gw_permissions select 2,'deny' where not exists (select action from gw_permissions where action='deny');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Cacti'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Cacti');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Nagios'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Nagios');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Nagvis'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Nagvis');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='BSM-Admin'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='BSM-Admin');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='BSM-User'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='BSM-User');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='BIRT-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='BIRT-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Performance'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Performance');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Performance-Reports'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Performance-Reports');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Monarch'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Monarch');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='NeDi'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='NeDi');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='CloudHub'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='CloudHub');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-administrator'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-administrator' and r.resource_id=rp.resource_id and r.name='Grafana');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-monitoring-operator'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-monitoring-operator' and r.resource_id=rp.resource_id and r.name='Grafana');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-user'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-user' and r.resource_id=rp.resource_id and r.name='Grafana');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='gw-portal-administrator'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='allow') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='gw-portal-administrator' and r.resource_id=rp.resource_id and r.name='Grafana');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='ro-dashboard'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='ro-dashboard' and r.resource_id=rp.resource_id and r.name='Grafana');
    insert into gw_ext_role_permissions select(select jbp_rid from gw_ext_role_attributes where jbp_name='msp-sample'),(select resource_id from gw_resources where name='Grafana'),(select perm_id from gw_permissions where action='deny') where not exists (select rp.jbp_rid from gw_ext_role_permissions rp, gw_ext_role_attributes ra, gw_resources r where rp.jbp_rid=ra.jbp_rid and ra.jbp_name='msp-sample' and r.resource_id=rp.resource_id and r.name='Grafana');
  END IF;
END; 
$$ LANGUAGE plpgsql;

SELECT fn_migrate_jboss_idm();

DROP FUNCTION IF EXISTS fn_migrate_jboss_idm();
