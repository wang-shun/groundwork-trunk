-- SQL to create a scom db, a scomuser user and create tables in the scom db
-- This version also populates schema_info with one row : version = 1.0
-- Revision history
-- DN   1.0  :  Initial version of this script, combining earlier sql scripts etc
-- DN   1.1  :  Added missing TimeResolved column to scom_events and scom_save tables

-- create scom database
CREATE DATABASE scom;

-- create default scom user and password
CREATE USER scomuser WITH PASSWORD 'scompass';

-- select the scom db
\c scom;

-- create tables
SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;

-- create config table - for possibly configuration future use
CREATE TABLE scom_config (
    hostname character varying,
    servicename character varying,
    monitorstate integer,
    notifystate integer
);
ALTER TABLE public.scom_config OWNER TO postgres;
REVOKE ALL ON TABLE scom_config FROM PUBLIC;
REVOKE ALL ON TABLE scom_config FROM postgres;
GRANT ALL ON TABLE scom_config TO postgres;
GRANT ALL ON TABLE scom_config TO scomuser;

-- create schema info table - useful for migration to other versions of GW
CREATE TABLE schema_info (
    name character varying(254),
    value character varying(254)
);
INSERT INTO schema_info ( name, value ) VALUES ('version', '1.0'); -- put a schema version in now
ALTER TABLE public.schema_info OWNER TO postgres;
REVOKE ALL ON TABLE schema_info FROM PUBLIC;
REVOKE ALL ON TABLE schema_info FROM postgres;
GRANT ALL ON TABLE schema_info TO postgres;
GRANT ALL ON TABLE schema_info TO scomuser;

-- create scom_events key sequence
-- DN not sure this is even needed since it all looks default
CREATE SEQUENCE scom_events_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; 
ALTER TABLE public.scom_events_seq OWNER TO postgres;
REVOKE ALL ON SEQUENCE scom_events_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE scom_events_seq FROM postgres;
GRANT ALL ON SEQUENCE scom_events_seq TO postgres;
GRANT ALL ON SEQUENCE scom_events_seq TO scomuser;

-- create scom_events table
CREATE TABLE scom_events (
    id integer DEFAULT nextval('scom_events_seq'::regclass),
    alertid character varying,
    connectorversion character varying,
    category character varying,
    computerdomain character varying,
    computername character varying,
    context character varying,
    description character varying,
    eventtype integer,
    lastmodifiedbynonconnector character varying,
    maintenancemodelastmodified character varying,
    managementgroupname character varying,
    managementpack character varying,
    managementserver character varying,
    modifiedby character varying,
    monitoringclassid character varying,
    monitoringclassname character varying,
    monitoringobjectdisplayname character varying,
    monitoringobjectfullname character varying,
    monitoringobjecthealthstate character varying,
    monitoringobjectid character varying,
    monitoringobjectinmaintenancemode character varying,
    monitoringobjectname character varying,
    monitoringobjectpath character varying,
    monitoringruleid character varying,
    name character varying,
    principalname character varying,
    priority character varying,
    problemid character varying,
    productknowledge character varying,
    repeatcount character varying,
    resolutionstate character varying,
    rulename character varying,
    ruletarget character varying,
    severity character varying,
    statelastmodified character varying,
    timeadded character varying,
    timeoflastevent character varying,
    timeresolutionstatelastmodified character varying,
    timeresolved character varying,
    webconsoleurl character varying
);
ALTER TABLE public.scom_events OWNER TO postgres;
REVOKE ALL ON TABLE scom_events FROM PUBLIC;
REVOKE ALL ON TABLE scom_events FROM postgres;
GRANT ALL ON TABLE scom_events TO postgres;
GRANT ALL ON TABLE scom_events TO scomuser;

-- create scom_save key sequence - again not sure if this is really strictly required
CREATE SEQUENCE scom_save_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE public.scom_save_seq OWNER TO postgres;
REVOKE ALL ON SEQUENCE scom_save_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE scom_save_seq FROM postgres;
GRANT ALL ON SEQUENCE scom_save_seq TO postgres;
GRANT ALL ON SEQUENCE scom_save_seq TO scomuser;

-- create scom_save table
CREATE TABLE scom_save (
    id integer DEFAULT nextval('scom_save_seq'::regclass),
    alertid character varying,
    connectorversion character varying,
    category character varying,
    computerdomain character varying,
    computername character varying,
    context character varying,
    description character varying,
    eventtype integer,
    lastmodifiedbynonconnector character varying,
    maintenancemodelastmodified character varying,
    managementgroupname character varying,
    managementpack character varying,
    managementserver character varying,
    modifiedby character varying,
    monitoringclassid character varying,
    monitoringclassname character varying,
    monitoringobjectdisplayname character varying,
    monitoringobjectfullname character varying,
    monitoringobjecthealthstate character varying,
    monitoringobjectid character varying,
    monitoringobjectinmaintenancemode character varying,
    monitoringobjectname character varying,
    monitoringobjectpath character varying,
    monitoringruleid character varying,
    name character varying,
    principalname character varying,
    priority character varying,
    problemid character varying,
    productknowledge character varying,
    repeatcount character varying,
    resolutionstate character varying,
    rulename character varying,
    ruletarget character varying,
    savereason character varying, -- note that this is an additional field compared to scom_events
    severity character varying,
    statelastmodified character varying,
    timeadded character varying,
    timeoflastevent character varying,
    timeresolutionstatelastmodified character varying,
    timeresolved character varying,
    webconsoleurl character varying
);
ALTER TABLE public.scom_save OWNER TO postgres;
REVOKE ALL ON TABLE scom_save FROM PUBLIC;
REVOKE ALL ON TABLE scom_save FROM postgres;
GRANT ALL ON TABLE scom_save TO postgres;
GRANT ALL ON TABLE scom_save TO scomuser;


-- removing this for now - not sure it's strictly required
--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--
-- REVOKE ALL ON SCHEMA public FROM PUBLIC;
-- REVOKE ALL ON SCHEMA public FROM postgres;
-- GRANT ALL ON SCHEMA public TO postgres;
-- GRANT ALL ON SCHEMA public TO PUBLIC;


