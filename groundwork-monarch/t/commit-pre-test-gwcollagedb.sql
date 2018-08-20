--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

ALTER TABLE ONLY public.servicestatusproperty DROP CONSTRAINT servicestatusproperty_ibfk_2;
ALTER TABLE ONLY public.servicestatusproperty DROP CONSTRAINT servicestatusproperty_ibfk_1;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_6;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_5;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_4;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_3;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_2;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_ibfk_1;
ALTER TABLE ONLY public.plugin DROP CONSTRAINT plugin_ibfk_1;
ALTER TABLE ONLY public.monitorlist DROP CONSTRAINT monitorlist_ibfk_2;
ALTER TABLE ONLY public.monitorlist DROP CONSTRAINT monitorlist_ibfk_1;
ALTER TABLE ONLY public.logperformancedata DROP CONSTRAINT logperformancedata_ibfk_2;
ALTER TABLE ONLY public.logperformancedata DROP CONSTRAINT logperformancedata_ibfk_1;
ALTER TABLE ONLY public.logmessageproperty DROP CONSTRAINT logmessageproperty_ibfk_2;
ALTER TABLE ONLY public.logmessageproperty DROP CONSTRAINT logmessageproperty_ibfk_1;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_9;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_8;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_7;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_6;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_5;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_4;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_3;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_2;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_ibfk_1;
ALTER TABLE ONLY public.hoststatusproperty DROP CONSTRAINT hoststatusproperty_ibfk_2;
ALTER TABLE ONLY public.hoststatusproperty DROP CONSTRAINT hoststatusproperty_ibfk_1;
ALTER TABLE ONLY public.hoststatus DROP CONSTRAINT hoststatus_ibfk_4;
ALTER TABLE ONLY public.hoststatus DROP CONSTRAINT hoststatus_ibfk_3;
ALTER TABLE ONLY public.hoststatus DROP CONSTRAINT hoststatus_ibfk_2;
ALTER TABLE ONLY public.hoststatus DROP CONSTRAINT hoststatus_ibfk_1;
ALTER TABLE ONLY public.hostgroupcollection DROP CONSTRAINT hostgroupcollection_ibfk_2;
ALTER TABLE ONLY public.hostgroupcollection DROP CONSTRAINT hostgroupcollection_ibfk_1;
ALTER TABLE ONLY public.hostgroup DROP CONSTRAINT hostgroup_ibfk_1;
ALTER TABLE ONLY public.host DROP CONSTRAINT host_ibfk_2;
ALTER TABLE ONLY public.host DROP CONSTRAINT host_ibfk_1;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT fk_logmessage_servicestatusid;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT fk_logmessage_hoststatusid;
ALTER TABLE ONLY public.category DROP CONSTRAINT entitytypeid_ibfk1_1;
ALTER TABLE ONLY public.entityproperty DROP CONSTRAINT entityproperty_ibfk_2;
ALTER TABLE ONLY public.entityproperty DROP CONSTRAINT entityproperty_ibfk_1;
ALTER TABLE ONLY public.deviceparent DROP CONSTRAINT deviceparent_ibfk_2;
ALTER TABLE ONLY public.deviceparent DROP CONSTRAINT deviceparent_ibfk_1;
ALTER TABLE ONLY public.categoryhierarchy DROP CONSTRAINT categoryhierarchy_ibfk_2;
ALTER TABLE ONLY public.categoryhierarchy DROP CONSTRAINT categoryhierarchy_ibfk_1;
ALTER TABLE ONLY public.categoryentity DROP CONSTRAINT categoryentity_ibfk_2;
ALTER TABLE ONLY public.categoryentity DROP CONSTRAINT categoryentity_ibfk_1;
ALTER TABLE ONLY public.category DROP CONSTRAINT category_ibfk_2;
ALTER TABLE ONLY public.entity DROP CONSTRAINT applicationtypeid_ibfk1_1;
ALTER TABLE ONLY public.applicationentityproperty DROP CONSTRAINT applicationentityproperty_ibfk_3;
ALTER TABLE ONLY public.applicationentityproperty DROP CONSTRAINT applicationentityproperty_ibfk_2;
ALTER TABLE ONLY public.applicationentityproperty DROP CONSTRAINT applicationentityproperty_ibfk_1;
ALTER TABLE ONLY public.applicationaction DROP CONSTRAINT applicationaction_ibfk_2;
ALTER TABLE ONLY public.applicationaction DROP CONSTRAINT applicationaction_ibfk_1;
ALTER TABLE ONLY public.actionproperty DROP CONSTRAINT actionproperty_ibfk_1;
ALTER TABLE ONLY public.actionparameter DROP CONSTRAINT actionparameter_ibfk_1;
ALTER TABLE ONLY public.action DROP CONSTRAINT action_ibfk_1;
DROP INDEX public.servicestatusproperty_propertytypeid;
DROP INDEX public.servicestatus_statetypeid;
DROP INDEX public.servicestatus_monitorstatusid;
DROP INDEX public.servicestatus_lasthardstateid;
DROP INDEX public.servicestatus_checktypeid;
DROP INDEX public.servicestatus_applicationtypeid;
DROP INDEX public.monitorlist_deviceid;
DROP INDEX public.logperformancedata_servicestatusid;
DROP INDEX public.logperformancedata_performancedatalabelid;
DROP INDEX public.logmessageproperty_propertytypeid;
DROP INDEX public.logmessage_typeruleid;
DROP INDEX public.logmessage_severityid;
DROP INDEX public.logmessage_priorityid;
DROP INDEX public.logmessage_operationstatusid;
DROP INDEX public.logmessage_monitorstatusid;
DROP INDEX public.logmessage_idx_logmessage_statetransitionhash;
DROP INDEX public.logmessage_idx_logmessage_statelesshash;
DROP INDEX public.logmessage_idx_logmessage_reportdate;
DROP INDEX public.logmessage_idx_logmessage_lastinsertdate;
DROP INDEX public.logmessage_idx_logmessage_firstinsertdate;
DROP INDEX public.logmessage_idx_logmessage_consolidationhash;
DROP INDEX public.logmessage_fk_logmessage_servicestatusid;
DROP INDEX public.logmessage_fk_logmessage_hoststatusid;
DROP INDEX public.logmessage_deviceid;
DROP INDEX public.logmessage_componentid;
DROP INDEX public.logmessage_applicationtypeid;
DROP INDEX public.logmessage_applicationseverityid;
DROP INDEX public.hoststatusproperty_propertytypeid;
DROP INDEX public.hoststatus_statetypeid;
DROP INDEX public.hoststatus_monitorstatusid;
DROP INDEX public.hoststatus_checktypeid;
DROP INDEX public.hostgroupcollection_hostgroupid;
DROP INDEX public.hostgroup_applicationtypeid;
DROP INDEX public.host_deviceid;
DROP INDEX public.host_applicationtypeid;
DROP INDEX public.entityproperty_propertytypeid;
DROP INDEX public.entity_applicationtypeid_ibfk1_1;
DROP INDEX public.deviceparent_parentid;
DROP INDEX public.categoryhierarchy_parentid;
DROP INDEX public.categoryentity_entitytypeid;
DROP INDEX public.categoryentity_categoryid;
DROP INDEX public.category_entitytypeid_ibfk1_1;
DROP INDEX public.category_applicationtypeid;
DROP INDEX public.applicationentityproperty_propertytypeid;
DROP INDEX public.applicationentityproperty_entitytypeid;
DROP INDEX public.applicationaction_actionid;
DROP INDEX public.action_idx_action_name;
DROP INDEX public.action_actiontypeid;
ALTER TABLE ONLY public.typerule DROP CONSTRAINT typerule_pkey;
ALTER TABLE ONLY public.typerule DROP CONSTRAINT typerule_name_key;
ALTER TABLE ONLY public.statetype DROP CONSTRAINT statetype_pkey;
ALTER TABLE ONLY public.statetype DROP CONSTRAINT statetype_name_key;
ALTER TABLE ONLY public.severity DROP CONSTRAINT severity_pkey;
ALTER TABLE ONLY public.severity DROP CONSTRAINT severity_name_key;
ALTER TABLE ONLY public.servicestatusproperty DROP CONSTRAINT servicestatusproperty_pkey;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_pkey;
ALTER TABLE ONLY public.servicestatus DROP CONSTRAINT servicestatus_hostid_servicedescription_key;
ALTER TABLE ONLY public.propertytype DROP CONSTRAINT propertytype_pkey;
ALTER TABLE ONLY public.propertytype DROP CONSTRAINT propertytype_name_key;
ALTER TABLE ONLY public.priority DROP CONSTRAINT priority_pkey;
ALTER TABLE ONLY public.priority DROP CONSTRAINT priority_name_key;
ALTER TABLE ONLY public.pluginplatform DROP CONSTRAINT pluginplatform_pkey;
ALTER TABLE ONLY public.pluginplatform DROP CONSTRAINT pluginplatform_name_arch_key;
ALTER TABLE ONLY public.plugin DROP CONSTRAINT plugin_platformid_name_key;
ALTER TABLE ONLY public.plugin DROP CONSTRAINT plugin_pkey;
ALTER TABLE ONLY public.performancedatalabel DROP CONSTRAINT performancedatalabel_pkey;
ALTER TABLE ONLY public.performancedatalabel DROP CONSTRAINT performancedatalabel_performancename_key;
ALTER TABLE ONLY public.operationstatus DROP CONSTRAINT operationstatus_pkey;
ALTER TABLE ONLY public.operationstatus DROP CONSTRAINT operationstatus_name_key;
ALTER TABLE ONLY public.network_service_status DROP CONSTRAINT network_service_status_pkey;
ALTER TABLE ONLY public.network_service_short_news DROP CONSTRAINT network_service_short_news_pkey;
ALTER TABLE ONLY public.network_service_notifications DROP CONSTRAINT network_service_notifications_pkey;
ALTER TABLE ONLY public.monitorstatus DROP CONSTRAINT monitorstatus_pkey;
ALTER TABLE ONLY public.monitorstatus DROP CONSTRAINT monitorstatus_name_key;
ALTER TABLE ONLY public.monitorserver DROP CONSTRAINT monitorserver_pkey;
ALTER TABLE ONLY public.monitorlist DROP CONSTRAINT monitorlist_pkey;
ALTER TABLE ONLY public.messagefilter DROP CONSTRAINT messagefilter_pkey;
ALTER TABLE ONLY public.messagefilter DROP CONSTRAINT messagefilter_name_key;
ALTER TABLE ONLY public.logperformancedata DROP CONSTRAINT logperformancedata_pkey;
ALTER TABLE ONLY public.logmessageproperty DROP CONSTRAINT logmessageproperty_pkey;
ALTER TABLE ONLY public.logmessage DROP CONSTRAINT logmessage_pkey;
ALTER TABLE ONLY public.hoststatusproperty DROP CONSTRAINT hoststatusproperty_pkey;
ALTER TABLE ONLY public.hoststatus DROP CONSTRAINT hoststatus_pkey;
ALTER TABLE ONLY public.hostgroupcollection DROP CONSTRAINT hostgroupcollection_pkey;
ALTER TABLE ONLY public.hostgroup DROP CONSTRAINT hostgroup_pkey;
ALTER TABLE ONLY public.hostgroup DROP CONSTRAINT hostgroup_name_key;
ALTER TABLE ONLY public.host DROP CONSTRAINT host_pkey;
ALTER TABLE ONLY public.host DROP CONSTRAINT host_hostname_key;
ALTER TABLE ONLY public.entitytype DROP CONSTRAINT entitytype_pkey;
ALTER TABLE ONLY public.entitytype DROP CONSTRAINT entitytype_name_key;
ALTER TABLE ONLY public.entityproperty DROP CONSTRAINT entityproperty_pkey;
ALTER TABLE ONLY public.entity DROP CONSTRAINT entity_pkey;
ALTER TABLE ONLY public.deviceparent DROP CONSTRAINT deviceparent_pkey;
ALTER TABLE ONLY public.device DROP CONSTRAINT device_pkey;
ALTER TABLE ONLY public.device DROP CONSTRAINT device_identification_key;
ALTER TABLE ONLY public.consolidationcriteria DROP CONSTRAINT consolidationcriteria_pkey;
ALTER TABLE ONLY public.consolidationcriteria DROP CONSTRAINT consolidationcriteria_name_key;
ALTER TABLE ONLY public.component DROP CONSTRAINT component_pkey;
ALTER TABLE ONLY public.component DROP CONSTRAINT component_name_key;
ALTER TABLE ONLY public.checktype DROP CONSTRAINT checktype_pkey;
ALTER TABLE ONLY public.checktype DROP CONSTRAINT checktype_name_key;
ALTER TABLE ONLY public.categoryhierarchy DROP CONSTRAINT categoryhierarchy_pkey;
ALTER TABLE ONLY public.categoryentity DROP CONSTRAINT categoryentity_pkey;
ALTER TABLE ONLY public.category DROP CONSTRAINT category_pkey;
ALTER TABLE ONLY public.category DROP CONSTRAINT category_name_key;
ALTER TABLE ONLY public.applicationtype DROP CONSTRAINT applicationtype_pkey;
ALTER TABLE ONLY public.applicationtype DROP CONSTRAINT applicationtype_name_key;
ALTER TABLE ONLY public.applicationentityproperty DROP CONSTRAINT applicationentityproperty_pkey;
ALTER TABLE ONLY public.applicationentityproperty DROP CONSTRAINT applicationentityproperty_applicationtypeid_entitytypeid_pr_key;
ALTER TABLE ONLY public.applicationaction DROP CONSTRAINT applicationaction_pkey;
ALTER TABLE ONLY public.actiontype DROP CONSTRAINT actiontype_pkey;
ALTER TABLE ONLY public.actiontype DROP CONSTRAINT actiontype_name_key;
ALTER TABLE ONLY public.actionproperty DROP CONSTRAINT actionproperty_pkey;
ALTER TABLE ONLY public.actionproperty DROP CONSTRAINT actionproperty_actionid_name_key;
ALTER TABLE ONLY public.actionparameter DROP CONSTRAINT actionparameter_pkey;
ALTER TABLE ONLY public.actionparameter DROP CONSTRAINT actionparameter_actionid_name_key;
ALTER TABLE ONLY public.action DROP CONSTRAINT action_pkey;
ALTER TABLE ONLY public.action DROP CONSTRAINT action_name_key;
ALTER TABLE public.typerule ALTER COLUMN typeruleid DROP DEFAULT;
ALTER TABLE public.statetype ALTER COLUMN statetypeid DROP DEFAULT;
ALTER TABLE public.severity ALTER COLUMN severityid DROP DEFAULT;
ALTER TABLE public.servicestatus ALTER COLUMN servicestatusid DROP DEFAULT;
ALTER TABLE public.propertytype ALTER COLUMN propertytypeid DROP DEFAULT;
ALTER TABLE public.priority ALTER COLUMN priorityid DROP DEFAULT;
ALTER TABLE public.pluginplatform ALTER COLUMN platformid DROP DEFAULT;
ALTER TABLE public.plugin ALTER COLUMN pluginid DROP DEFAULT;
ALTER TABLE public.performancedatalabel ALTER COLUMN performancedatalabelid DROP DEFAULT;
ALTER TABLE public.operationstatus ALTER COLUMN operationstatusid DROP DEFAULT;
ALTER TABLE public.network_service_status ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.network_service_short_news ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.network_service_notifications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.monitorstatus ALTER COLUMN monitorstatusid DROP DEFAULT;
ALTER TABLE public.monitorserver ALTER COLUMN monitorserverid DROP DEFAULT;
ALTER TABLE public.messagefilter ALTER COLUMN messagefilterid DROP DEFAULT;
ALTER TABLE public.logperformancedata ALTER COLUMN logperformancedataid DROP DEFAULT;
ALTER TABLE public.logmessage ALTER COLUMN logmessageid DROP DEFAULT;
ALTER TABLE public.hostgroup ALTER COLUMN hostgroupid DROP DEFAULT;
ALTER TABLE public.host ALTER COLUMN hostid DROP DEFAULT;
ALTER TABLE public.entitytype ALTER COLUMN entitytypeid DROP DEFAULT;
ALTER TABLE public.entity ALTER COLUMN entityid DROP DEFAULT;
ALTER TABLE public.device ALTER COLUMN deviceid DROP DEFAULT;
ALTER TABLE public.consolidationcriteria ALTER COLUMN consolidationcriteriaid DROP DEFAULT;
ALTER TABLE public.component ALTER COLUMN componentid DROP DEFAULT;
ALTER TABLE public.checktype ALTER COLUMN checktypeid DROP DEFAULT;
ALTER TABLE public.categoryentity ALTER COLUMN categoryentityid DROP DEFAULT;
ALTER TABLE public.category ALTER COLUMN categoryid DROP DEFAULT;
ALTER TABLE public.applicationtype ALTER COLUMN applicationtypeid DROP DEFAULT;
ALTER TABLE public.applicationentityproperty ALTER COLUMN applicationentitypropertyid DROP DEFAULT;
ALTER TABLE public.actiontype ALTER COLUMN actiontypeid DROP DEFAULT;
ALTER TABLE public.actionproperty ALTER COLUMN actionpropertyid DROP DEFAULT;
ALTER TABLE public.actionparameter ALTER COLUMN actionparameterid DROP DEFAULT;
ALTER TABLE public.action ALTER COLUMN actionid DROP DEFAULT;
DROP SEQUENCE public.typerule_typeruleid_seq;
DROP TABLE public.typerule;
DROP SEQUENCE public.statetype_statetypeid_seq;
DROP TABLE public.statetype;
DROP SEQUENCE public.severity_severityid_seq;
DROP TABLE public.severity;
DROP TABLE public.servicestatusproperty;
DROP SEQUENCE public.servicestatus_servicestatusid_seq;
DROP TABLE public.servicestatus;
DROP TABLE public.schemainfo;
DROP SEQUENCE public.propertytype_propertytypeid_seq;
DROP TABLE public.propertytype;
DROP SEQUENCE public.priority_priorityid_seq;
DROP TABLE public.priority;
DROP SEQUENCE public.pluginplatform_platformid_seq;
DROP TABLE public.pluginplatform;
DROP SEQUENCE public.plugin_pluginid_seq;
DROP TABLE public.plugin;
DROP SEQUENCE public.performancedatalabel_performancedatalabelid_seq;
DROP TABLE public.performancedatalabel;
DROP SEQUENCE public.operationstatus_operationstatusid_seq;
DROP TABLE public.operationstatus;
DROP SEQUENCE public.network_service_status_id_seq;
DROP TABLE public.network_service_status;
DROP SEQUENCE public.network_service_short_news_id_seq;
DROP TABLE public.network_service_short_news;
DROP SEQUENCE public.network_service_notifications_id_seq;
DROP TABLE public.network_service_notifications;
DROP SEQUENCE public.monitorstatus_monitorstatusid_seq;
DROP TABLE public.monitorstatus;
DROP SEQUENCE public.monitorserver_monitorserverid_seq;
DROP TABLE public.monitorserver;
DROP TABLE public.monitorlist;
DROP SEQUENCE public.messagefilter_messagefilterid_seq;
DROP TABLE public.messagefilter;
DROP SEQUENCE public.logperformancedata_logperformancedataid_seq;
DROP TABLE public.logperformancedata;
DROP TABLE public.logmessageproperty;
DROP SEQUENCE public.logmessage_logmessageid_seq;
DROP TABLE public.logmessage;
DROP TABLE public.hoststatusproperty;
DROP TABLE public.hoststatus;
DROP TABLE public.hostgroupcollection;
DROP SEQUENCE public.hostgroup_hostgroupid_seq;
DROP TABLE public.hostgroup;
DROP SEQUENCE public.host_hostid_seq;
DROP TABLE public.host;
DROP SEQUENCE public.hibernate_sequence;
DROP SEQUENCE public.entitytype_entitytypeid_seq;
DROP TABLE public.entitytype;
DROP TABLE public.entityproperty;
DROP SEQUENCE public.entity_entityid_seq;
DROP TABLE public.entity;
DROP TABLE public.deviceparent;
DROP SEQUENCE public.device_deviceid_seq;
DROP TABLE public.device;
DROP SEQUENCE public.consolidationcriteria_consolidationcriteriaid_seq;
DROP TABLE public.consolidationcriteria;
DROP SEQUENCE public.component_componentid_seq;
DROP TABLE public.component;
DROP SEQUENCE public.checktype_checktypeid_seq;
DROP TABLE public.checktype;
DROP TABLE public.categoryhierarchy;
DROP SEQUENCE public.categoryentity_categoryentityid_seq;
DROP TABLE public.categoryentity;
DROP SEQUENCE public.category_categoryid_seq;
DROP TABLE public.category;
DROP SEQUENCE public.applicationtype_applicationtypeid_seq;
DROP TABLE public.applicationtype;
DROP SEQUENCE public.applicationentityproperty_applicationentitypropertyid_seq;
DROP TABLE public.applicationentityproperty;
DROP TABLE public.applicationaction;
DROP SEQUENCE public.actiontype_actiontypeid_seq;
DROP TABLE public.actiontype;
DROP SEQUENCE public.actionproperty_actionpropertyid_seq;
DROP TABLE public.actionproperty;
DROP SEQUENCE public.actionparameter_actionparameterid_seq;
DROP TABLE public.actionparameter;
DROP SEQUENCE public.action_actionid_seq;
DROP TABLE public.action;
DROP EXTENSION plpgsql;
DROP SCHEMA public;
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: action; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE action (
    actionid integer NOT NULL,
    actiontypeid integer NOT NULL,
    name character varying(256) NOT NULL,
    description character varying(512)
);


ALTER TABLE public.action OWNER TO collage;

--
-- Name: action_actionid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE action_actionid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.action_actionid_seq OWNER TO collage;

--
-- Name: action_actionid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE action_actionid_seq OWNED BY action.actionid;


--
-- Name: actionparameter; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE actionparameter (
    actionparameterid integer NOT NULL,
    actionid integer NOT NULL,
    name character varying(128) NOT NULL,
    value text
);


ALTER TABLE public.actionparameter OWNER TO collage;

--
-- Name: actionparameter_actionparameterid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE actionparameter_actionparameterid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actionparameter_actionparameterid_seq OWNER TO collage;

--
-- Name: actionparameter_actionparameterid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actionparameter_actionparameterid_seq OWNED BY actionparameter.actionparameterid;


--
-- Name: actionproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE actionproperty (
    actionpropertyid integer NOT NULL,
    actionid integer NOT NULL,
    name character varying(128) NOT NULL,
    value text
);


ALTER TABLE public.actionproperty OWNER TO collage;

--
-- Name: actionproperty_actionpropertyid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE actionproperty_actionpropertyid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actionproperty_actionpropertyid_seq OWNER TO collage;

--
-- Name: actionproperty_actionpropertyid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actionproperty_actionpropertyid_seq OWNED BY actionproperty.actionpropertyid;


--
-- Name: actiontype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE actiontype (
    actiontypeid integer NOT NULL,
    name character varying(256) NOT NULL,
    classname character varying(256) NOT NULL
);


ALTER TABLE public.actiontype OWNER TO collage;

--
-- Name: actiontype_actiontypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE actiontype_actiontypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actiontype_actiontypeid_seq OWNER TO collage;

--
-- Name: actiontype_actiontypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actiontype_actiontypeid_seq OWNED BY actiontype.actiontypeid;


--
-- Name: applicationaction; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationaction (
    applicationtypeid integer NOT NULL,
    actionid integer NOT NULL
);


ALTER TABLE public.applicationaction OWNER TO collage;

--
-- Name: applicationentityproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationentityproperty (
    applicationentitypropertyid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    entitytypeid integer NOT NULL,
    propertytypeid integer NOT NULL,
    sortorder integer DEFAULT (999)::numeric NOT NULL
);


ALTER TABLE public.applicationentityproperty OWNER TO collage;

--
-- Name: applicationentityproperty_applicationentitypropertyid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE applicationentityproperty_applicationentitypropertyid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.applicationentityproperty_applicationentitypropertyid_seq OWNER TO collage;

--
-- Name: applicationentityproperty_applicationentitypropertyid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE applicationentityproperty_applicationentitypropertyid_seq OWNED BY applicationentityproperty.applicationentitypropertyid;


--
-- Name: applicationtype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationtype (
    applicationtypeid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254),
    statetransitioncriteria character varying(512)
);


ALTER TABLE public.applicationtype OWNER TO collage;

--
-- Name: applicationtype_applicationtypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE applicationtype_applicationtypeid_seq
    START WITH 101
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.applicationtype_applicationtypeid_seq OWNER TO collage;

--
-- Name: applicationtype_applicationtypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE applicationtype_applicationtypeid_seq OWNED BY applicationtype.applicationtypeid;


--
-- Name: category; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE category (
    categoryid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(4096),
    entitytypeid integer NOT NULL,
    applicationtypeid integer,
    agentid character varying(128)
);


ALTER TABLE public.category OWNER TO collage;

--
-- Name: category_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE category_categoryid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_categoryid_seq OWNER TO collage;

--
-- Name: category_categoryid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE category_categoryid_seq OWNED BY category.categoryid;


--
-- Name: categoryentity; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE categoryentity (
    categoryentityid integer NOT NULL,
    objectid integer DEFAULT 0 NOT NULL,
    categoryid integer DEFAULT 0 NOT NULL,
    entitytypeid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.categoryentity OWNER TO collage;

--
-- Name: categoryentity_categoryentityid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE categoryentity_categoryentityid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.categoryentity_categoryentityid_seq OWNER TO collage;

--
-- Name: categoryentity_categoryentityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE categoryentity_categoryentityid_seq OWNED BY categoryentity.categoryentityid;


--
-- Name: categoryhierarchy; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE categoryhierarchy (
    categoryid integer DEFAULT 0 NOT NULL,
    parentid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.categoryhierarchy OWNER TO collage;

--
-- Name: checktype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE checktype (
    checktypeid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.checktype OWNER TO collage;

--
-- Name: checktype_checktypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE checktype_checktypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.checktype_checktypeid_seq OWNER TO collage;

--
-- Name: checktype_checktypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE checktype_checktypeid_seq OWNED BY checktype.checktypeid;


--
-- Name: component; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE component (
    componentid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.component OWNER TO collage;

--
-- Name: component_componentid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE component_componentid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.component_componentid_seq OWNER TO collage;

--
-- Name: component_componentid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE component_componentid_seq OWNED BY component.componentid;


--
-- Name: consolidationcriteria; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE consolidationcriteria (
    consolidationcriteriaid integer NOT NULL,
    name character varying(254) NOT NULL,
    criteria character varying(512) NOT NULL
);


ALTER TABLE public.consolidationcriteria OWNER TO collage;

--
-- Name: consolidationcriteria_consolidationcriteriaid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE consolidationcriteria_consolidationcriteriaid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consolidationcriteria_consolidationcriteriaid_seq OWNER TO collage;

--
-- Name: consolidationcriteria_consolidationcriteriaid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE consolidationcriteria_consolidationcriteriaid_seq OWNED BY consolidationcriteria.consolidationcriteriaid;


--
-- Name: device; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE device (
    deviceid integer NOT NULL,
    displayname character varying(254),
    identification character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.device OWNER TO collage;

--
-- Name: device_deviceid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE device_deviceid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.device_deviceid_seq OWNER TO collage;

--
-- Name: device_deviceid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE device_deviceid_seq OWNED BY device.deviceid;


--
-- Name: deviceparent; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE deviceparent (
    deviceid integer NOT NULL,
    parentid integer NOT NULL
);


ALTER TABLE public.deviceparent OWNER TO collage;

--
-- Name: entity; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE entity (
    entityid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254) NOT NULL,
    class character varying(254) NOT NULL,
    applicationtypeid integer NOT NULL
);


ALTER TABLE public.entity OWNER TO collage;

--
-- Name: entity_entityid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE entity_entityid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.entity_entityid_seq OWNER TO collage;

--
-- Name: entity_entityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE entity_entityid_seq OWNED BY entity.entityid;


--
-- Name: entityproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE entityproperty (
    entitytypeid integer NOT NULL,
    objectid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(4096),
    valuedate timestamp without time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp without time zone DEFAULT now() NOT NULL,
    createdon timestamp without time zone NOT NULL
);


ALTER TABLE public.entityproperty OWNER TO collage;

--
-- Name: entitytype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE entitytype (
    entitytypeid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254),
    islogicalentity boolean DEFAULT false NOT NULL,
    isapplicationtypesupported boolean DEFAULT false NOT NULL
);


ALTER TABLE public.entitytype OWNER TO collage;

--
-- Name: entitytype_entitytypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE entitytype_entitytypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.entitytype_entitytypeid_seq OWNER TO collage;

--
-- Name: entitytype_entitytypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE entitytype_entitytypeid_seq OWNED BY entitytype.entitytypeid;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO postgres;

--
-- Name: host; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE host (
    hostid integer NOT NULL,
    deviceid integer NOT NULL,
    hostname character varying(254) NOT NULL,
    description character varying(4096),
    applicationtypeid integer,
    agentid character varying(128)
);


ALTER TABLE public.host OWNER TO collage;

--
-- Name: host_hostid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE host_hostid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.host_hostid_seq OWNER TO collage;

--
-- Name: host_hostid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE host_hostid_seq OWNED BY host.hostid;


--
-- Name: hostgroup; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostgroup (
    hostgroupid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(4096),
    applicationtypeid integer,
    alias character varying(254),
    agentid character varying(128)
);


ALTER TABLE public.hostgroup OWNER TO collage;

--
-- Name: hostgroup_hostgroupid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE hostgroup_hostgroupid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hostgroup_hostgroupid_seq OWNER TO collage;

--
-- Name: hostgroup_hostgroupid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE hostgroup_hostgroupid_seq OWNED BY hostgroup.hostgroupid;


--
-- Name: hostgroupcollection; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostgroupcollection (
    hostid integer NOT NULL,
    hostgroupid integer NOT NULL
);


ALTER TABLE public.hostgroupcollection OWNER TO collage;

--
-- Name: hoststatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hoststatus (
    hoststatusid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    monitorstatusid integer NOT NULL,
    lastchecktime timestamp without time zone,
    checktypeid integer,
    statetypeid integer,
    nextchecktime timestamp without time zone
);


ALTER TABLE public.hoststatus OWNER TO collage;

--
-- Name: hoststatusproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hoststatusproperty (
    hoststatusid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(32768),
    valuedate timestamp without time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp without time zone DEFAULT now() NOT NULL,
    createdon timestamp without time zone NOT NULL
);


ALTER TABLE public.hoststatusproperty OWNER TO collage;

--
-- Name: logmessage; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logmessage (
    logmessageid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    deviceid integer NOT NULL,
    hoststatusid integer,
    servicestatusid integer,
    textmessage character varying(4096) NOT NULL,
    msgcount integer DEFAULT (1)::numeric NOT NULL,
    firstinsertdate timestamp without time zone NOT NULL,
    lastinsertdate timestamp without time zone NOT NULL,
    reportdate timestamp without time zone NOT NULL,
    monitorstatusid integer,
    severityid integer NOT NULL,
    applicationseverityid integer NOT NULL,
    priorityid integer NOT NULL,
    typeruleid integer NOT NULL,
    componentid integer NOT NULL,
    operationstatusid integer NOT NULL,
    isstatechanged boolean DEFAULT false NOT NULL,
    consolidationhash integer DEFAULT 0 NOT NULL,
    statelesshash integer DEFAULT 0 NOT NULL,
    statetransitionhash integer
);


ALTER TABLE public.logmessage OWNER TO collage;

--
-- Name: logmessage_logmessageid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE logmessage_logmessageid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logmessage_logmessageid_seq OWNER TO collage;

--
-- Name: logmessage_logmessageid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE logmessage_logmessageid_seq OWNED BY logmessage.logmessageid;


--
-- Name: logmessageproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logmessageproperty (
    logmessageid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(4096),
    valuedate timestamp without time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp without time zone DEFAULT now() NOT NULL,
    createdon timestamp without time zone NOT NULL
);


ALTER TABLE public.logmessageproperty OWNER TO collage;

--
-- Name: logperformancedata; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logperformancedata (
    logperformancedataid integer NOT NULL,
    servicestatusid integer NOT NULL,
    lastchecktime timestamp without time zone NOT NULL,
    maximum double precision DEFAULT 0,
    minimum double precision DEFAULT 0,
    average double precision DEFAULT 0,
    measurementpoints integer DEFAULT 0,
    performancedatalabelid integer
);


ALTER TABLE public.logperformancedata OWNER TO collage;

--
-- Name: logperformancedata_logperformancedataid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE logperformancedata_logperformancedataid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logperformancedata_logperformancedataid_seq OWNER TO collage;

--
-- Name: logperformancedata_logperformancedataid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE logperformancedata_logperformancedataid_seq OWNED BY logperformancedata.logperformancedataid;


--
-- Name: messagefilter; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE messagefilter (
    messagefilterid integer NOT NULL,
    name character varying(254) NOT NULL,
    regexpresion character varying(512) NOT NULL,
    ischangeseveritytostatistic boolean DEFAULT false
);


ALTER TABLE public.messagefilter OWNER TO collage;

--
-- Name: messagefilter_messagefilterid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE messagefilter_messagefilterid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.messagefilter_messagefilterid_seq OWNER TO collage;

--
-- Name: messagefilter_messagefilterid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE messagefilter_messagefilterid_seq OWNED BY messagefilter.messagefilterid;


--
-- Name: monitorlist; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE monitorlist (
    monitorserverid integer NOT NULL,
    deviceid integer NOT NULL
);


ALTER TABLE public.monitorlist OWNER TO collage;

--
-- Name: monitorserver; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE monitorserver (
    monitorserverid integer NOT NULL,
    monitorservername character varying(254) NOT NULL,
    ip character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.monitorserver OWNER TO collage;

--
-- Name: monitorserver_monitorserverid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE monitorserver_monitorserverid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitorserver_monitorserverid_seq OWNER TO collage;

--
-- Name: monitorserver_monitorserverid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE monitorserver_monitorserverid_seq OWNED BY monitorserver.monitorserverid;


--
-- Name: monitorstatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE monitorstatus (
    monitorstatusid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.monitorstatus OWNER TO collage;

--
-- Name: monitorstatus_monitorstatusid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE monitorstatus_monitorstatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitorstatus_monitorstatusid_seq OWNER TO collage;

--
-- Name: monitorstatus_monitorstatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE monitorstatus_monitorstatusid_seq OWNED BY monitorstatus.monitorstatusid;


--
-- Name: network_service_notifications; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE network_service_notifications (
    id integer NOT NULL,
    created_at timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone,
    guid integer,
    type character varying(20),
    title character varying(255) DEFAULT ''::character varying,
    critical integer,
    description text DEFAULT ''::text,
    webpage_url text DEFAULT ''::text,
    webpage_url_description text DEFAULT ''::text,
    update_md5 character varying(255) DEFAULT ''::character varying,
    update_url character varying(255) DEFAULT ''::character varying,
    update_cmd_switch character varying(255) DEFAULT ''::character varying,
    update_instruction text DEFAULT ''::text,
    update_size integer,
    update_type character varying(255) DEFAULT ''::character varying,
    update_os character varying(255) DEFAULT ''::character varying,
    is_read integer DEFAULT 0,
    is_archived integer DEFAULT 0
);


ALTER TABLE public.network_service_notifications OWNER TO collage;

--
-- Name: network_service_notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE network_service_notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.network_service_notifications_id_seq OWNER TO collage;

--
-- Name: network_service_notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE network_service_notifications_id_seq OWNED BY network_service_notifications.id;


--
-- Name: network_service_short_news; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE network_service_short_news (
    id integer NOT NULL,
    status integer,
    title character varying(255) DEFAULT ''::character varying,
    message text DEFAULT ''::text,
    url character varying(255) DEFAULT ''::character varying,
    url_description text DEFAULT ''::text,
    is_archived integer DEFAULT 0
);


ALTER TABLE public.network_service_short_news OWNER TO collage;

--
-- Name: network_service_short_news_id_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE network_service_short_news_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.network_service_short_news_id_seq OWNER TO collage;

--
-- Name: network_service_short_news_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE network_service_short_news_id_seq OWNED BY network_service_short_news.id;


--
-- Name: network_service_status; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE network_service_status (
    id integer NOT NULL,
    last_checked timestamp without time zone
);


ALTER TABLE public.network_service_status OWNER TO collage;

--
-- Name: network_service_status_id_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE network_service_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.network_service_status_id_seq OWNER TO collage;

--
-- Name: network_service_status_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE network_service_status_id_seq OWNED BY network_service_status.id;


--
-- Name: operationstatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE operationstatus (
    operationstatusid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.operationstatus OWNER TO collage;

--
-- Name: operationstatus_operationstatusid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE operationstatus_operationstatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.operationstatus_operationstatusid_seq OWNER TO collage;

--
-- Name: operationstatus_operationstatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE operationstatus_operationstatusid_seq OWNED BY operationstatus.operationstatusid;


--
-- Name: performancedatalabel; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE performancedatalabel (
    performancedatalabelid integer NOT NULL,
    performancename character varying(254),
    servicedisplayname character varying(128),
    metriclabel character varying(128),
    unit character varying(64)
);


ALTER TABLE public.performancedatalabel OWNER TO collage;

--
-- Name: performancedatalabel_performancedatalabelid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE performancedatalabel_performancedatalabelid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.performancedatalabel_performancedatalabelid_seq OWNER TO collage;

--
-- Name: performancedatalabel_performancedatalabelid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE performancedatalabel_performancedatalabelid_seq OWNED BY performancedatalabel.performancedatalabelid;


--
-- Name: plugin; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE plugin (
    pluginid integer NOT NULL,
    name character varying(128) NOT NULL,
    url character varying(254),
    platformid integer NOT NULL,
    dependencies character varying(254),
    lastupdatetimestamp timestamp without time zone DEFAULT now() NOT NULL,
    checksum character varying(254) NOT NULL,
    lastupdatedby character varying(254)
);


ALTER TABLE public.plugin OWNER TO collage;

--
-- Name: plugin_pluginid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE plugin_pluginid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_pluginid_seq OWNER TO collage;

--
-- Name: plugin_pluginid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE plugin_pluginid_seq OWNED BY plugin.pluginid;


--
-- Name: pluginplatform; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE pluginplatform (
    platformid integer NOT NULL,
    name character varying(128) NOT NULL,
    arch integer NOT NULL,
    description character varying(254)
);


ALTER TABLE public.pluginplatform OWNER TO collage;

--
-- Name: pluginplatform_platformid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE pluginplatform_platformid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pluginplatform_platformid_seq OWNER TO collage;

--
-- Name: pluginplatform_platformid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE pluginplatform_platformid_seq OWNED BY pluginplatform.platformid;


--
-- Name: priority; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE priority (
    priorityid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.priority OWNER TO collage;

--
-- Name: priority_priorityid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE priority_priorityid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.priority_priorityid_seq OWNER TO collage;

--
-- Name: priority_priorityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE priority_priorityid_seq OWNED BY priority.priorityid;


--
-- Name: propertytype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE propertytype (
    propertytypeid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254),
    isdate boolean DEFAULT false,
    isboolean boolean DEFAULT false,
    isstring boolean DEFAULT false,
    isinteger boolean DEFAULT false,
    islong boolean DEFAULT false,
    isdouble boolean DEFAULT false,
    isvisible boolean DEFAULT true
);


ALTER TABLE public.propertytype OWNER TO collage;

--
-- Name: propertytype_propertytypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE propertytype_propertytypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertytype_propertytypeid_seq OWNER TO collage;

--
-- Name: propertytype_propertytypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE propertytype_propertytypeid_seq OWNED BY propertytype.propertytypeid;


--
-- Name: schemainfo; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE schemainfo (
    name character varying(254),
    value character varying(254)
);


ALTER TABLE public.schemainfo OWNER TO collage;

--
-- Name: servicestatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE servicestatus (
    servicestatusid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    servicedescription character varying(254) NOT NULL,
    hostid integer NOT NULL,
    monitorstatusid integer NOT NULL,
    lastchecktime timestamp without time zone,
    nextchecktime timestamp without time zone,
    laststatechange timestamp without time zone,
    lasthardstateid integer NOT NULL,
    statetypeid integer NOT NULL,
    checktypeid integer NOT NULL,
    metrictype character varying(254),
    domain character varying(254),
    agentid character varying(128)
);


ALTER TABLE public.servicestatus OWNER TO collage;

--
-- Name: servicestatus_servicestatusid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE servicestatus_servicestatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.servicestatus_servicestatusid_seq OWNER TO collage;

--
-- Name: servicestatus_servicestatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE servicestatus_servicestatusid_seq OWNED BY servicestatus.servicestatusid;


--
-- Name: servicestatusproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE servicestatusproperty (
    servicestatusid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(16384),
    valuedate timestamp without time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp without time zone DEFAULT now() NOT NULL,
    createdon timestamp without time zone NOT NULL
);


ALTER TABLE public.servicestatusproperty OWNER TO collage;

--
-- Name: severity; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE severity (
    severityid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.severity OWNER TO collage;

--
-- Name: severity_severityid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE severity_severityid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.severity_severityid_seq OWNER TO collage;

--
-- Name: severity_severityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE severity_severityid_seq OWNED BY severity.severityid;


--
-- Name: statetype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE statetype (
    statetypeid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.statetype OWNER TO collage;

--
-- Name: statetype_statetypeid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE statetype_statetypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.statetype_statetypeid_seq OWNER TO collage;

--
-- Name: statetype_statetypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE statetype_statetypeid_seq OWNED BY statetype.statetypeid;


--
-- Name: typerule; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE typerule (
    typeruleid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.typerule OWNER TO collage;

--
-- Name: typerule_typeruleid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE typerule_typeruleid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.typerule_typeruleid_seq OWNER TO collage;

--
-- Name: typerule_typeruleid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE typerule_typeruleid_seq OWNED BY typerule.typeruleid;


--
-- Name: actionid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY action ALTER COLUMN actionid SET DEFAULT nextval('action_actionid_seq'::regclass);


--
-- Name: actionparameterid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionparameter ALTER COLUMN actionparameterid SET DEFAULT nextval('actionparameter_actionparameterid_seq'::regclass);


--
-- Name: actionpropertyid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionproperty ALTER COLUMN actionpropertyid SET DEFAULT nextval('actionproperty_actionpropertyid_seq'::regclass);


--
-- Name: actiontypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actiontype ALTER COLUMN actiontypeid SET DEFAULT nextval('actiontype_actiontypeid_seq'::regclass);


--
-- Name: applicationentitypropertyid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty ALTER COLUMN applicationentitypropertyid SET DEFAULT nextval('applicationentityproperty_applicationentitypropertyid_seq'::regclass);


--
-- Name: applicationtypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationtype ALTER COLUMN applicationtypeid SET DEFAULT nextval('applicationtype_applicationtypeid_seq'::regclass);


--
-- Name: categoryid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY category ALTER COLUMN categoryid SET DEFAULT nextval('category_categoryid_seq'::regclass);


--
-- Name: categoryentityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryentity ALTER COLUMN categoryentityid SET DEFAULT nextval('categoryentity_categoryentityid_seq'::regclass);


--
-- Name: checktypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY checktype ALTER COLUMN checktypeid SET DEFAULT nextval('checktype_checktypeid_seq'::regclass);


--
-- Name: componentid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY component ALTER COLUMN componentid SET DEFAULT nextval('component_componentid_seq'::regclass);


--
-- Name: consolidationcriteriaid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY consolidationcriteria ALTER COLUMN consolidationcriteriaid SET DEFAULT nextval('consolidationcriteria_consolidationcriteriaid_seq'::regclass);


--
-- Name: deviceid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY device ALTER COLUMN deviceid SET DEFAULT nextval('device_deviceid_seq'::regclass);


--
-- Name: entityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entity ALTER COLUMN entityid SET DEFAULT nextval('entity_entityid_seq'::regclass);


--
-- Name: entitytypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entitytype ALTER COLUMN entitytypeid SET DEFAULT nextval('entitytype_entitytypeid_seq'::regclass);


--
-- Name: hostid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY host ALTER COLUMN hostid SET DEFAULT nextval('host_hostid_seq'::regclass);


--
-- Name: hostgroupid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroup ALTER COLUMN hostgroupid SET DEFAULT nextval('hostgroup_hostgroupid_seq'::regclass);


--
-- Name: logmessageid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage ALTER COLUMN logmessageid SET DEFAULT nextval('logmessage_logmessageid_seq'::regclass);


--
-- Name: logperformancedataid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logperformancedata ALTER COLUMN logperformancedataid SET DEFAULT nextval('logperformancedata_logperformancedataid_seq'::regclass);


--
-- Name: messagefilterid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY messagefilter ALTER COLUMN messagefilterid SET DEFAULT nextval('messagefilter_messagefilterid_seq'::regclass);


--
-- Name: monitorserverid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorserver ALTER COLUMN monitorserverid SET DEFAULT nextval('monitorserver_monitorserverid_seq'::regclass);


--
-- Name: monitorstatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorstatus ALTER COLUMN monitorstatusid SET DEFAULT nextval('monitorstatus_monitorstatusid_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY network_service_notifications ALTER COLUMN id SET DEFAULT nextval('network_service_notifications_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY network_service_short_news ALTER COLUMN id SET DEFAULT nextval('network_service_short_news_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY network_service_status ALTER COLUMN id SET DEFAULT nextval('network_service_status_id_seq'::regclass);


--
-- Name: operationstatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY operationstatus ALTER COLUMN operationstatusid SET DEFAULT nextval('operationstatus_operationstatusid_seq'::regclass);


--
-- Name: performancedatalabelid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY performancedatalabel ALTER COLUMN performancedatalabelid SET DEFAULT nextval('performancedatalabel_performancedatalabelid_seq'::regclass);


--
-- Name: pluginid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY plugin ALTER COLUMN pluginid SET DEFAULT nextval('plugin_pluginid_seq'::regclass);


--
-- Name: platformid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY pluginplatform ALTER COLUMN platformid SET DEFAULT nextval('pluginplatform_platformid_seq'::regclass);


--
-- Name: priorityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY priority ALTER COLUMN priorityid SET DEFAULT nextval('priority_priorityid_seq'::regclass);


--
-- Name: propertytypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY propertytype ALTER COLUMN propertytypeid SET DEFAULT nextval('propertytype_propertytypeid_seq'::regclass);


--
-- Name: servicestatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus ALTER COLUMN servicestatusid SET DEFAULT nextval('servicestatus_servicestatusid_seq'::regclass);


--
-- Name: severityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY severity ALTER COLUMN severityid SET DEFAULT nextval('severity_severityid_seq'::regclass);


--
-- Name: statetypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY statetype ALTER COLUMN statetypeid SET DEFAULT nextval('statetype_statetypeid_seq'::regclass);


--
-- Name: typeruleid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE ONLY typerule ALTER COLUMN typeruleid SET DEFAULT nextval('typerule_typeruleid_seq'::regclass);


--
-- Data for Name: action; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY action (actionid, actiontypeid, name, description) FROM stdin;
1	4	Nagios Acknowledge	Acknowledge Nagios Log Message
2	1	Accept Log Message	Update Log Message Operation Status To Accepted
3	1	Close Log Message	Update Log Message Operation Status To Closed
4	1	Notify Log Message	Update Log Message Operation Status To Notified
5	1	Open Log Message	Update Log Message Operation Status To Open
6	1	Acknowledge Log Message	Update Log Message Operation Status To Acknowledged
7	2	Register Agent	Invoke a script for the selected message
8	2	Register Agent by Profile	Invoke a script for the selected message
9	2	Noma Notify For Host	Invoke a script for the selected message
10	2	Noma Notify For Service	Invoke a script for the selected message
11	5	Submit Passive Check	Submit a passive check for this service
\.


--
-- Name: action_actionid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('action_actionid_seq', 11, true);


--
-- Data for Name: actionparameter; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY actionparameter (actionparameterid, actionid, name, value) FROM stdin;
1	7	agent-type	agent-type
2	7	host-name	host-name
3	7	host-ip	host-ip
4	7	host-mac	host-mac
5	7	operating-system	operating-system
6	7	host-characteristic	host-characteristic
7	8	agent-type	agent-type
8	8	host-name	host-name
9	8	host-ip	host-ip
10	8	host-mac	host-mac
11	8	operating-system	operating-system
12	8	host-profile-name	host-profile-name
13	8	service-profile-name	service-profile-name
14	9	-c	-c
15	9	notifyType	notifyType
16	9	-s	-s
17	9	hoststate	hoststate
18	9	-H	-H
19	9	hostname	hostname
20	9	-G	-G
21	9	hostgroupnames	hostgroupnames
22	9	-n	-n
23	9	notificationtype	notificationtype
24	9	-i	-i
25	9	hostaddress	hostaddress
26	9	-o	-o
27	9	hostoutput	hostoutput
28	9	-t	-t
29	9	shortdatetime	shortdatetime
30	9	-u	-u
31	9	hostnotificationid	hostnotificationid
32	9	-A	-A
33	9	notificationauthoralias	notificationauthoralias
34	9	-C	-C
35	9	notificationcomment	notificationcomment
36	9	-R	-R
37	9	notificationrecipients	notificationrecipients
38	10	-c	-c
39	10	notifyType	notifyType
40	10	-s	-s
41	10	servicestate	servicestate
42	10	-H	-H
43	10	hostname	hostname
44	10	-G	-G
45	10	hostgroupnames	hostgroupnames
46	10	-E	-E
47	10	servicegroupnames	servicegroupnames
48	10	-S	-S
49	10	servicedescription	servicedescription
50	10	-o	-o
51	10	serviceoutput	serviceoutput
52	10	-n	-n
53	10	notificationtype	notificationtype
54	10	-a	-a
55	10	hostalias	hostalias
56	10	-i	-i
57	10	hostaddress	hostaddress
58	10	-t	-t
59	10	shortdatetime	shortdatetime
60	10	-u	-u
61	10	servicenotificationid	servicenotificationid
62	10	-A	-A
63	10	notificationauthoralias	notificationauthoralias
64	10	-C	-C
65	10	notificationcomment	notificationcomment
66	10	-R	-R
67	10	notificationrecipients	notificationrecipients
68	11	nsca_host	nsca_host
69	11	user	user
70	11	comment	comment
71	11	host	host
72	11	service	service
73	11	state	state
\.


--
-- Name: actionparameter_actionparameterid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('actionparameter_actionparameterid_seq', 73, true);


--
-- Data for Name: actionproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY actionproperty (actionpropertyid, actionid, name, value) FROM stdin;
1	1	NagiosCommandFile	/usr/local/groundwork/nagios/var/spool/nagios.cmd
2	2	OperationStatus	ACCEPTED
3	3	OperationStatus	CLOSED
4	4	OperationStatus	NOTIFIED
5	5	OperationStatus	OPEN
6	6	OperationStatus	ACKNOWLEDGED
7	7	Script	/usr/local/groundwork/foundation/scripts/registerAgent.pl
8	8	Script	/usr/local/groundwork/foundation/scripts/registerAgentByProfile.pl
9	9	Script	/usr/local/groundwork/noma/notifier/alert_via_noma.pl
10	10	Script	/usr/local/groundwork/noma/notifier/alert_via_noma.pl
11	11	Script	/usr/local/groundwork/foundation/scripts/reset_passive_check.sh
\.


--
-- Name: actionproperty_actionpropertyid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('actionproperty_actionpropertyid_seq', 11, true);


--
-- Data for Name: actiontype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY actiontype (actiontypeid, name, classname) FROM stdin;
1	LOG_MESSAGE_OPERATION_STATUS	org.groundwork.foundation.bs.actions.UpdateOperationStatusAction
2	SCRIPT_ACTION	org.groundwork.foundation.bs.actions.ShellScriptAction
3	HTTP_REQUEST_ACTION	org.groundwork.foundation.bs.actions.HttpRequestAction
4	NAGIOS_ACKNOWLEDGE_ACTION	org.groundwork.foundation.bs.actions.NagiosAcknowledgeAction
5	PASSIVE_CHECK_ACTION	org.groundwork.foundation.bs.actions.ShellScriptAction
\.


--
-- Name: actiontype_actiontypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('actiontype_actiontypeid_seq', 5, true);


--
-- Data for Name: applicationaction; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY applicationaction (applicationtypeid, actionid) FROM stdin;
1	2
1	3
1	4
1	5
1	6
100	1
101	7
101	8
102	9
102	10
110	11
111	11
\.


--
-- Data for Name: applicationentityproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY applicationentityproperty (applicationentitypropertyid, applicationtypeid, entitytypeid, propertytypeid, sortorder) FROM stdin;
1	100	1	1	1
2	100	1	2	4
3	100	1	3	5
4	100	1	4	6
5	100	1	5	7
6	100	1	6	8
7	100	1	7	9
8	100	1	8	10
9	100	1	9	11
10	100	1	10	12
11	100	1	11	13
12	100	1	12	14
13	100	1	13	15
14	100	1	14	16
15	100	1	15	17
16	100	1	16	18
17	100	1	17	19
18	100	1	34	20
19	100	1	25	20
20	100	1	26	21
21	100	1	35	22
22	100	1	38	23
23	100	1	39	24
24	100	1	40	25
25	100	1	43	26
26	100	1	44	27
27	100	1	47	28
28	100	1	48	29
29	100	1	51	72
30	100	1	52	74
31	100	1	53	75
32	100	2	1	30
33	100	2	18	31
34	100	2	10	33
35	100	2	19	34
36	100	2	11	35
37	100	2	20	37
38	100	2	21	39
39	100	2	22	40
40	100	2	23	41
41	100	2	24	42
42	100	2	7	43
43	100	2	8	44
44	100	2	9	45
45	100	2	25	46
46	100	2	26	47
47	100	2	12	48
48	100	2	27	49
49	100	2	14	50
50	100	2	15	51
51	100	2	16	52
52	100	2	17	53
53	100	2	28	54
54	100	2	34	55
55	100	2	40	56
56	100	2	43	57
57	100	2	44	58
58	100	2	47	67
59	100	2	48	68
60	100	2	41	69
61	100	2	42	70
62	100	2	50	71
63	100	2	51	73
64	100	2	53	76
65	100	3	29	59
66	100	3	30	60
67	100	3	31	61
68	100	3	32	62
69	100	3	33	63
70	100	3	36	65
71	100	3	37	66
72	100	3	48	67
73	200	1	1	80
74	200	2	1	81
75	200	1	3	82
76	200	2	53	83
77	103	1	1	85
78	103	2	1	86
79	103	1	3	87
80	103	2	53	88
81	106	1	1	89
82	106	2	1	90
83	106	1	3	91
84	106	2	53	92
85	1	4	54	1
86	1	4	55	2
87	1	4	56	3
88	1	3	48	1
89	1	3	3	2
90	1	3	36	3
91	110	3	57	1
92	110	3	58	2
93	110	3	59	3
94	110	3	60	4
95	110	3	61	5
96	110	3	62	6
97	110	3	63	7
98	110	3	48	8
99	110	3	36	9
100	110	1	3	90
101	110	2	3	90
102	111	3	57	1
103	111	3	31	2
104	111	3	33	3
105	111	3	63	4
106	111	3	48	5
107	111	3	36	6
108	111	1	3	90
109	111	2	3	90
110	200	3	48	1
111	200	3	36	2
113	200	2	3	90
114	101	3	48	1
115	101	3	36	2
116	101	1	3	90
117	101	2	3	90
118	102	3	48	1
119	102	3	36	2
120	102	1	3	90
121	102	2	3	90
122	103	3	48	1
123	103	3	36	2
125	103	2	3	90
126	106	3	48	1
127	106	3	36	2
129	106	2	3	90
130	104	3	48	1
131	104	3	36	2
132	104	1	3	90
133	104	2	3	90
134	108	3	48	1
135	108	3	36	2
136	108	1	3	90
137	108	2	3	90
138	105	3	48	1
139	105	3	36	2
140	105	1	3	90
141	105	2	3	90
142	107	3	48	1
143	107	3	36	2
144	107	1	3	90
145	107	2	3	90
146	109	3	48	1
147	109	3	36	2
148	109	1	3	90
149	109	2	3	90
\.


--
-- Name: applicationentityproperty_applicationentitypropertyid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('applicationentityproperty_applicationentitypropertyid_seq', 149, true);


--
-- Data for Name: applicationtype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY applicationtype (applicationtypeid, name, description, statetransitioncriteria) FROM stdin;
1	SYSTEM	Properties that exist regardless of the Application being monitored	Device
100	NAGIOS	System monitored by Nagios	Device;Host;ServiceDescription
200	VEMA	Virtual Environment Monitor Agent	Device;Host;ServiceDescription
101	GDMA	System monitored by GDMA	Device;Host;ServiceDescription
102	NOMA	NoMa Notification	Device;Host;ServiceDescription
103	CHRHEV	Cloud Hub for Red Hat Virtualization	Device;Host;ServiceDescription
104	ARCHIVE	Archiving related messages	Device;Host
105	SEL	Groundwork Selenium Agent Connector	Device;Host;ServiceDescription
106	OS	Cloud Hub for Open Stack Virtualization	Device;Host;ServiceDescription
107	AUDIT	Audit Events from all SubSystems	Device;Host
108	BSM	Business Service Monitoring	Device;Host
110	SNMPTRAP	SNMP Trap application	Device;Event_OID_numeric
111	SYSLOG	SYSLOG application	Device
109	CACTI	Feeder cacti_feeder application type	Device;Host
\.


--
-- Name: applicationtype_applicationtypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('applicationtype_applicationtypeid_seq', 111, true);


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY category (categoryid, name, description, entitytypeid, applicationtypeid, agentid) FROM stdin;
1	servicegroup-042	\N	23	100	\N
2	servicegroup-005	\N	23	100	\N
3	servicegroup-011	\N	23	100	\N
4	servicegroup-064	\N	23	100	\N
5	servicegroup-009	\N	23	100	\N
6	servicegroup-010	\N	23	100	\N
7	servicegroup-041	\N	23	100	\N
\.


--
-- Name: category_categoryid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('category_categoryid_seq', 7, true);


--
-- Data for Name: categoryentity; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY categoryentity (categoryentityid, objectid, categoryid, entitytypeid) FROM stdin;
1	43	1	2
2	31	2	2
3	60	3	2
4	65	3	2
5	71	3	2
6	37	4	2
7	40	5	2
8	48	5	2
9	53	6	2
10	52	7	2
\.


--
-- Name: categoryentity_categoryentityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('categoryentity_categoryentityid_seq', 10, true);


--
-- Data for Name: categoryhierarchy; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY categoryhierarchy (categoryid, parentid) FROM stdin;
\.


--
-- Data for Name: checktype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY checktype (checktypeid, name, description) FROM stdin;
1	ACTIVE	Active Check
2	PASSIVE	Passive Check
\.


--
-- Name: checktype_checktypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('checktype_checktypeid_seq', 2, true);


--
-- Data for Name: component; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY component (componentid, name, description) FROM stdin;
1	SNMP	SNMP Component
2	MQ	MessageQueue component
3	JMSLISTENER	JMSListener component
4	UNDEFINED	Undefined component
\.


--
-- Name: component_componentid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('component_componentid_seq', 4, true);


--
-- Data for Name: consolidationcriteria; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY consolidationcriteria (consolidationcriteriaid, name, criteria) FROM stdin;
1	NAGIOSEVENT	Device;MonitorStatus;OperationStatus;SubComponent;ErrorType
2	SYSTEM	OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage
3	SNMPTRAP	OperationStatus;Device;ipaddress;MonitorStatus;Event_OID_numeric;Event_Name;Category;Variable_Bindings
4	SYSLOG	OperationStatus;Device;MonitorStatus;ipaddress;ErrorType;SubComponent
\.


--
-- Name: consolidationcriteria_consolidationcriteriaid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('consolidationcriteria_consolidationcriteriaid_seq', 4, true);


--
-- Data for Name: device; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY device (deviceid, displayname, identification, description) FROM stdin;
1	127.0.0.1	127.0.0.1	Device localhost
2	cacti_feeder_host	cacti_feeder_host	\N
3	bsm-host	bsm-host	\N
4	host-006-a	127.0.6.1	\N
5	host-008-b	127.0.8.2	\N
6	host-061	127.0.61.1	\N
7	host-041-a	127.0.41.1	\N
8	host-005	127.0.5.1	\N
9	host-007-a	127.0.7.1	\N
10	host-038-b	127.0.38.2	\N
11	host-002	127.0.2.1	\N
12	host-064-b	127.0.64.2	\N
13	host-039	127.0.39.1	\N
14	host-009-a	127.0.9.1	\N
15	host-042-b	127.0.42.2	\N
16	host-064-a	127.0.64.1	\N
17	host-008-a	127.0.8.1	\N
18	host-009-b	127.0.9.2	\N
19	host-063-b	127.0.63.2	\N
20	host-037-a	127.0.37.1	\N
21	host-041-b	127.0.41.2	\N
22	host-010-a	127.0.10.1	\N
23	host-037-b	127.0.37.2	\N
24	host-043	127.0.43.1	\N
25	host-006-b	127.0.6.2	\N
26	host-008-c	127.0.8.3	\N
27	host-011-a	127.0.11.1	\N
28	host-001	127.0.1.1	\N
29	host-003	127.0.3.1	\N
30	host-011-b	127.0.11.2	\N
31	host-062	127.0.62.1	\N
32	test-host-pattern	127.0.254.1	\N
33	host-004	127.0.4.1	\N
34	host-011-c	127.0.11.3	\N
\.


--
-- Name: device_deviceid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('device_deviceid_seq', 34, true);


--
-- Data for Name: deviceparent; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY deviceparent (deviceid, parentid) FROM stdin;
\.


--
-- Data for Name: entity; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY entity (entityid, name, description, class, applicationtypeid) FROM stdin;
\.


--
-- Name: entity_entityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('entity_entityid_seq', 1, false);


--
-- Data for Name: entityproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY entityproperty (entitytypeid, objectid, propertytypeid, valuestring, valuedate, valueboolean, valueinteger, valuelong, valuedouble, lasteditedon, createdon) FROM stdin;
\.


--
-- Data for Name: entitytype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY entitytype (entitytypeid, name, description, islogicalentity, isapplicationtypesupported) FROM stdin;
1	HOST_STATUS	com.groundwork.collage.model.impl.HostStatus	f	f
2	SERVICE_STATUS	com.groundwork.collage.model.impl.ServiceStatus	f	f
3	LOG_MESSAGE	com.groundwork.collage.model.impl.LogMessage	f	f
4	DEVICE	com.groundwork.collage.model.impl.Device	f	f
5	HOST	com.groundwork.collage.model.impl.Host	f	f
6	HOSTGROUP	com.groundwork.collage.model.impl.HostGroup	f	f
7	APPLICATION_TYPE	com.groundwork.collage.model.impl.ApplicationType	f	f
8	CATEGORY	com.groundwork.collage.model.impl.Category	f	f
9	CHECK_TYPE	com.groundwork.collage.model.impl.CheckType	f	f
10	COMPONENT	com.groundwork.collage.model.impl.Component	f	f
11	MONITOR_STATUS	com.groundwork.collage.model.impl.MonitorStatus	f	f
12	OPERATION_STATUS	com.groundwork.collage.model.impl.OperationStatus	f	f
13	PRIORITY	com.groundwork.collage.model.impl.Priority	f	f
14	SEVERITY	com.groundwork.collage.model.impl.Severity	f	f
15	STATE_TYPE	com.groundwork.collage.model.impl.StateType	f	f
16	TYPE_RULE	com.groundwork.collage.model.impl.TypeRule	f	f
17	MONITOR_SERVER	com.groundwork.collage.model.impl.MonitorServer	f	f
18	LOG_MESSAGE_STATISTICS	com.groundwork.collage.model.impl.LogMessageStatistic	t	f
19	HOST_STATISTICS	com.groundwork.collage.model.impl.HostStatistic	t	f
20	SERVICE_STATISTICS	com.groundwork.collage.model.impl.ServiceStatistic	t	f
21	HOST_STATE_TRANSITIONS	com.groundwork.collage.model.impl.HostStateTransition	t	f
22	SERVICE_STATE_TRANSITIONS	com.groundwork.collage.model.impl.ServiceStateTransition	t	f
23	SERVICE_GROUP	com.groundwork.collage.model.impl.ServiceGroup	t	f
\.


--
-- Name: entitytype_entitytypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('entitytype_entitytypeid_seq', 1, false);


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('hibernate_sequence', 1000, false);


--
-- Data for Name: host; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY host (hostid, deviceid, hostname, description, applicationtypeid, agentid) FROM stdin;
2	2	cacti_feeder_host	Added by feeder cacti_feeder	109	662cbe06-0bb2-11e4-adb0-7dfbf6f6a4e2
3	3	bsm-host	bsm-host	108	\N
4	4	host-006-a	host-006-a	100	\N
5	5	host-008-b	host-008-b	100	\N
6	6	host-061	host-061	100	\N
7	7	host-041-a	host-041-a	100	\N
8	8	host-005	host-005	100	\N
9	9	host-007-a	host-007-a	100	\N
10	10	host-038-b	host-038-b	100	\N
11	11	host-002	host-002	100	\N
12	12	host-064-b	host-064-b	100	\N
13	13	host-039	host-039	100	\N
14	14	host-009-a	host-009-a	100	\N
15	15	host-042-b	host-042-b	100	\N
16	16	host-064-a	host-064-a	100	\N
17	17	host-008-a	host-008-a	100	\N
18	18	host-009-b	host-009-b	100	\N
19	19	host-063-b	host-063-b	100	\N
20	20	host-037-a	host-037-a	100	\N
21	21	host-041-b	host-041-b	100	\N
22	22	host-010-a	host-010-a	100	\N
23	23	host-037-b	host-037-b	100	\N
24	24	host-043	host-043	100	\N
25	25	host-006-b	host-006-b	100	\N
26	26	host-008-c	host-008-c	100	\N
27	27	host-011-a	host-011-a	100	\N
28	28	host-001	host-001	100	\N
29	29	host-003	host-003	100	\N
30	30	host-011-b	host-011-b	100	\N
31	31	host-062	host-062	100	\N
32	32	test-host-pattern	test-host-pattern	100	\N
33	33	host-004	host-004	100	\N
34	34	host-011-c	host-011-c	100	\N
1	1	localhost	localhost	100	\N
\.


--
-- Name: host_hostid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('host_hostid_seq', 34, true);


--
-- Data for Name: hostgroup; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY hostgroup (hostgroupid, name, description, applicationtypeid, alias, agentid) FROM stdin;
2	cacti_feeder	cacti_feeder virtual hostgroup	109	cacti_feeder	662cbe06-0bb2-11e4-adb0-7dfbf6f6a4e2
3	BSM:Business Objects	Business Objects	108	Business Objects	\N
4	hostgroup-007	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
5	hostgroup-038	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
6	hostgroup-004	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
7	hostgroup-006	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
8	hostgroup-008	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
9	hostgroup-037	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
10	hostgroup-063	For simplicity in establishing new hostgroups in test databases, clone this test-hostgroup-pattern to create new hostgroups.	100	a hostgroup for integration commit testing	\N
1	Linux Servers	 	100	Linux Servers	\N
\.


--
-- Name: hostgroup_hostgroupid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('hostgroup_hostgroupid_seq', 10, true);


--
-- Data for Name: hostgroupcollection; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY hostgroupcollection (hostid, hostgroupid) FROM stdin;
2	2
3	3
9	4
10	5
33	6
4	7
25	7
26	8
17	8
5	8
23	9
19	10
31	1
6	1
19	1
24	1
16	1
12	1
26	1
17	1
13	1
5	1
1	1
32	1
30	1
27	1
10	1
34	1
15	1
20	1
23	1
9	1
14	1
4	1
18	1
25	1
8	1
33	1
29	1
11	1
28	1
7	1
21	1
22	1
\.


--
-- Data for Name: hoststatus; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY hoststatus (hoststatusid, applicationtypeid, monitorstatusid, lastchecktime, checktypeid, statetypeid, nextchecktime) FROM stdin;
2	109	1	2014-07-14 16:55:57	1	3	\N
1	100	1	2014-07-14 17:00:25	1	2	2014-07-14 17:00:25
3	108	1	2014-07-14 17:05:08.193	1	2	2014-07-14 17:05:08.193
4	100	5	\N	1	2	\N
5	100	5	\N	1	2	\N
6	100	5	\N	1	2	\N
7	100	5	\N	1	2	\N
8	100	5	\N	1	2	\N
10	100	5	\N	1	2	\N
9	100	5	\N	1	2	\N
11	100	5	\N	1	2	\N
12	100	5	\N	1	2	\N
13	100	5	\N	1	2	\N
14	100	5	\N	1	2	\N
15	100	5	\N	1	2	\N
16	100	5	\N	1	2	\N
17	100	5	\N	1	2	\N
19	100	5	\N	1	2	\N
18	100	5	\N	1	2	\N
21	100	5	\N	1	2	\N
20	100	5	\N	1	2	\N
22	100	5	\N	1	2	\N
23	100	5	\N	1	2	\N
24	100	5	\N	1	2	\N
25	100	5	\N	1	2	\N
26	100	5	\N	1	2	\N
28	100	5	\N	1	2	\N
27	100	5	\N	1	2	\N
29	100	5	\N	1	2	\N
30	100	5	\N	1	2	\N
31	100	5	\N	1	2	\N
32	100	5	\N	1	2	\N
33	100	5	\N	1	2	\N
34	100	5	\N	1	2	\N
\.


--
-- Data for Name: hoststatusproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY hoststatusproperty (hoststatusid, propertytypeid, valuestring, valuedate, valueboolean, valueinteger, valuelong, valuedouble, lasteditedon, createdon) FROM stdin;
2	39	Added by feeder cacti_feeder	\N	\N	\N	\N	\N	2014-07-14 16:55:57.922	2014-07-14 16:55:57.922
1	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	25	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.296	2014-07-14 16:56:16.296
1	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	44	\N	\N	\N	\N	10	\N	2014-07-14 16:56:16.186	2014-07-14 16:56:16.186
1	35	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	3	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.297	2014-07-14 16:56:16.297
1	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.297	2014-07-14 16:56:16.297
1	2	\N	2014-07-14 16:54:34	\N	\N	\N	\N	2014-07-14 16:56:16.188	2014-07-14 16:56:16.188
1	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.186	2014-07-14 16:56:16.186
1	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.187	2014-07-14 16:56:16.187
1	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.297	2014-07-14 16:56:16.297
1	1	OK - 127.0.0.1: rta 0.031ms, lost 0%	\N	\N	\N	\N	\N	2014-07-14 16:56:16.296	2014-07-14 16:56:16.296
1	26	\N	\N	\N	\N	\N	2	2014-07-14 16:56:16.296	2014-07-14 16:56:16.296
1	53	rta=0.031ms;3000.000;5000.000;0; pl=0%;80;100;; rtmax=0.031ms;;;; rtmin=0.031ms;;;;	\N	\N	\N	\N	\N	2014-07-14 16:56:16.186	2014-07-14 16:56:16.186
3	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:04.995	2014-07-14 17:05:04.995
3	53		\N	\N	\N	\N	\N	2014-07-14 17:05:08.203	2014-07-14 17:05:08.203
3	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:08.204	2014-07-14 17:05:08.204
3	44	\N	\N	\N	\N	1	\N	2014-07-14 17:05:08.203	2014-07-14 17:05:08.203
3	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:08.204	2014-07-14 17:05:08.204
3	1	UP - running	\N	\N	\N	\N	\N	2014-07-14 17:05:04.995	2014-07-14 17:05:04.995
3	2	\N	2014-07-14 17:05:08.193	\N	\N	\N	\N	2014-07-14 17:05:04.995	2014-07-14 17:05:04.995
4	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.095	2014-07-14 17:05:39.095
4	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.095	2014-07-14 17:05:39.095
4	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.095	2014-07-14 17:05:39.095
5	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.125	2014-07-14 17:05:39.125
5	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.125	2014-07-14 17:05:39.125
5	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.125	2014-07-14 17:05:39.125
6	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.145	2014-07-14 17:05:39.145
6	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.145	2014-07-14 17:05:39.145
6	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.145	2014-07-14 17:05:39.145
7	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.165	2014-07-14 17:05:39.165
7	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.165	2014-07-14 17:05:39.165
7	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.165	2014-07-14 17:05:39.165
8	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.185	2014-07-14 17:05:39.185
8	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.185	2014-07-14 17:05:39.185
8	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.185	2014-07-14 17:05:39.185
9	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.208	2014-07-14 17:05:39.208
9	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.208	2014-07-14 17:05:39.208
9	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.208	2014-07-14 17:05:39.208
10	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.228	2014-07-14 17:05:39.228
10	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.228	2014-07-14 17:05:39.228
10	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.228	2014-07-14 17:05:39.228
11	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.248	2014-07-14 17:05:39.248
11	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.248	2014-07-14 17:05:39.248
11	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.248	2014-07-14 17:05:39.248
12	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.268	2014-07-14 17:05:39.268
12	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.268	2014-07-14 17:05:39.268
12	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.268	2014-07-14 17:05:39.268
13	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.288	2014-07-14 17:05:39.288
13	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.288	2014-07-14 17:05:39.288
13	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.288	2014-07-14 17:05:39.288
14	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.309	2014-07-14 17:05:39.309
14	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.31	2014-07-14 17:05:39.31
14	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.31	2014-07-14 17:05:39.31
15	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.333	2014-07-14 17:05:39.333
15	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.333	2014-07-14 17:05:39.333
15	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.333	2014-07-14 17:05:39.333
16	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.353	2014-07-14 17:05:39.353
16	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.353	2014-07-14 17:05:39.353
16	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.353	2014-07-14 17:05:39.353
17	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.374	2014-07-14 17:05:39.374
17	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.375	2014-07-14 17:05:39.375
17	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.375	2014-07-14 17:05:39.375
18	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.398	2014-07-14 17:05:39.398
18	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.398	2014-07-14 17:05:39.398
18	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.398	2014-07-14 17:05:39.398
19	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.42	2014-07-14 17:05:39.42
19	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.42	2014-07-14 17:05:39.42
19	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.42	2014-07-14 17:05:39.42
20	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.449	2014-07-14 17:05:39.449
20	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.449	2014-07-14 17:05:39.449
20	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.45	2014-07-14 17:05:39.45
21	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.472	2014-07-14 17:05:39.472
21	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.472	2014-07-14 17:05:39.472
21	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.472	2014-07-14 17:05:39.472
22	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.494	2014-07-14 17:05:39.494
22	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.494	2014-07-14 17:05:39.494
22	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.494	2014-07-14 17:05:39.494
23	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.527	2014-07-14 17:05:39.527
23	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.527	2014-07-14 17:05:39.527
23	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.527	2014-07-14 17:05:39.527
24	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.549	2014-07-14 17:05:39.549
24	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.549	2014-07-14 17:05:39.549
24	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.549	2014-07-14 17:05:39.549
26	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.611	2014-07-14 17:05:39.611
26	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.611	2014-07-14 17:05:39.611
26	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.611	2014-07-14 17:05:39.611
32	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.758	2014-07-14 17:05:39.758
32	39	pattern for new test hosts	\N	\N	\N	\N	\N	2014-07-14 17:05:39.758	2014-07-14 17:05:39.758
32	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.758	2014-07-14 17:05:39.758
33	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.793	2014-07-14 17:05:39.793
33	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.793	2014-07-14 17:05:39.793
33	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.793	2014-07-14 17:05:39.793
1	39	Linux Server #1	\N	\N	\N	\N	\N	2014-07-14 17:05:48.374	2014-07-14 17:05:48.374
4	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.248	2014-07-14 17:05:54.248
4	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.248	2014-07-14 17:05:54.248
4	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
4	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.246	2014-07-14 17:05:54.246
5	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
5	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.255	2014-07-14 17:05:54.255
6	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.266	2014-07-14 17:05:54.266
6	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
6	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.267	2014-07-14 17:05:54.267
7	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
7	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
7	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
7	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
7	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
7	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.325	2014-07-14 17:05:54.325
7	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.324	2014-07-14 17:05:54.324
8	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
8	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
10	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
10	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.34	2014-07-14 17:05:54.34
9	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
25	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.58	2014-07-14 17:05:39.58
25	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.58	2014-07-14 17:05:39.58
25	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.58	2014-07-14 17:05:39.58
27	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.639	2014-07-14 17:05:39.639
27	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.639	2014-07-14 17:05:39.639
27	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.639	2014-07-14 17:05:39.639
28	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.666	2014-07-14 17:05:39.666
28	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.666	2014-07-14 17:05:39.666
28	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.666	2014-07-14 17:05:39.666
29	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.69	2014-07-14 17:05:39.69
29	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.69	2014-07-14 17:05:39.69
29	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.69	2014-07-14 17:05:39.69
30	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.713	2014-07-14 17:05:39.713
30	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.714	2014-07-14 17:05:39.714
30	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.714	2014-07-14 17:05:39.714
31	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.737	2014-07-14 17:05:39.737
31	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.737	2014-07-14 17:05:39.737
31	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.737	2014-07-14 17:05:39.737
34	51	For simplicity in establishing new hosts in test databases, clone this test-host-pattern to create new hosts.	\N	\N	\N	\N	\N	2014-07-14 17:05:39.82	2014-07-14 17:05:39.82
34	39	a host for integration commit testing	\N	\N	\N	\N	\N	2014-07-14 17:05:39.82	2014-07-14 17:05:39.82
34	2	\N	2014-07-14 17:05:38	\N	\N	\N	\N	2014-07-14 17:05:39.82	2014-07-14 17:05:39.82
9	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
9	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
9	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
9	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
9	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.353	2014-07-14 17:05:54.353
9	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
9	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
11	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
11	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.365	2014-07-14 17:05:54.365
12	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
12	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.374	2014-07-14 17:05:54.374
13	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
13	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.395	2014-07-14 17:05:54.395
14	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
14	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.404	2014-07-14 17:05:54.404
15	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
15	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.425	2014-07-14 17:05:54.425
16	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
16	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.435	2014-07-14 17:05:54.435
17	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
17	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.45	2014-07-14 17:05:54.45
19	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.472	2014-07-14 17:05:54.472
19	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
19	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.473	2014-07-14 17:05:54.473
18	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.483	2014-07-14 17:05:54.483
18	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.483	2014-07-14 17:05:54.483
18	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.483	2014-07-14 17:05:54.483
18	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
18	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.482	2014-07-14 17:05:54.482
21	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
21	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.492	2014-07-14 17:05:54.492
20	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
20	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.505	2014-07-14 17:05:54.505
22	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.526	2014-07-14 17:05:54.526
22	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.526	2014-07-14 17:05:54.526
22	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
22	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.527	2014-07-14 17:05:54.527
23	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
23	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.536	2014-07-14 17:05:54.536
24	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
24	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.563	2014-07-14 17:05:54.563
25	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
25	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
26	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.583	2014-07-14 17:05:54.583
26	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.583	2014-07-14 17:05:54.583
26	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.583	2014-07-14 17:05:54.583
26	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.584	2014-07-14 17:05:54.584
26	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.583	2014-07-14 17:05:54.583
28	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
28	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
28	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
28	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.594	2014-07-14 17:05:54.594
28	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
28	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
28	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
27	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.602	2014-07-14 17:05:54.602
27	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.602	2014-07-14 17:05:54.602
27	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
27	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.603	2014-07-14 17:05:54.603
29	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
29	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
29	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
29	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
30	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.62	2014-07-14 17:05:54.62
30	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.62	2014-07-14 17:05:54.62
30	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.62	2014-07-14 17:05:54.62
30	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.62	2014-07-14 17:05:54.62
30	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
30	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.621	2014-07-14 17:05:54.621
31	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
31	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
32	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
32	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.64	2014-07-14 17:05:54.64
33	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.653	2014-07-14 17:05:54.653
33	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
33	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.654	2014-07-14 17:05:54.654
34	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	3	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	35	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
34	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.671	2014-07-14 17:05:54.671
\.


--
-- Data for Name: logmessage; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY logmessage (logmessageid, applicationtypeid, deviceid, hoststatusid, servicestatusid, textmessage, msgcount, firstinsertdate, lastinsertdate, reportdate, monitorstatusid, severityid, applicationseverityid, priorityid, typeruleid, componentid, operationstatusid, isstatechanged, consolidationhash, statelesshash, statetransitionhash) FROM stdin;
1	100	1	1	\N	PENDING	1	2014-07-14 16:52:49.813546	2014-07-14 16:52:49.813546	2014-07-14 16:52:49.813546	5	3	3	1	6	4	4	f	0	0	\N
2	100	1	1	1	PENDING	1	2014-07-14 16:52:49.820681	2014-07-14 16:52:49.820681	2014-07-14 16:52:49.820681	5	3	3	1	6	4	4	f	0	0	\N
3	100	1	1	2	PENDING	1	2014-07-14 16:52:49.823714	2014-07-14 16:52:49.823714	2014-07-14 16:52:49.823714	5	3	3	1	6	4	4	f	0	0	\N
4	100	1	1	3	PENDING	1	2014-07-14 16:52:49.825752	2014-07-14 16:52:49.825752	2014-07-14 16:52:49.825752	5	3	3	1	6	4	4	f	0	0	\N
5	100	1	1	4	PENDING	1	2014-07-14 16:52:49.827749	2014-07-14 16:52:49.827749	2014-07-14 16:52:49.827749	5	3	3	1	6	4	4	f	0	0	\N
6	100	1	1	5	PENDING	1	2014-07-14 16:52:49.82977	2014-07-14 16:52:49.82977	2014-07-14 16:52:49.82977	5	3	3	1	6	4	4	f	0	0	\N
7	100	1	1	6	PENDING	1	2014-07-14 16:52:49.831821	2014-07-14 16:52:49.831821	2014-07-14 16:52:49.831821	5	3	3	1	6	4	4	f	0	0	\N
8	100	1	1	7	PENDING	1	2014-07-14 16:52:49.833843	2014-07-14 16:52:49.833843	2014-07-14 16:52:49.833843	5	3	3	1	6	4	4	f	0	0	\N
9	100	1	1	8	PENDING	1	2014-07-14 16:52:49.835851	2014-07-14 16:52:49.835851	2014-07-14 16:52:49.835851	5	3	3	1	6	4	4	f	0	0	\N
10	100	1	1	9	PENDING	1	2014-07-14 16:52:49.837866	2014-07-14 16:52:49.837866	2014-07-14 16:52:49.837866	5	3	3	1	6	4	4	f	0	0	\N
11	100	1	1	10	PENDING	1	2014-07-14 16:52:49.839922	2014-07-14 16:52:49.839922	2014-07-14 16:52:49.839922	5	3	3	1	6	4	4	f	0	0	\N
12	100	1	1	11	PENDING	1	2014-07-14 16:52:49.841948	2014-07-14 16:52:49.841948	2014-07-14 16:52:49.841948	5	3	3	1	6	4	4	f	0	0	\N
13	100	1	1	12	PENDING	1	2014-07-14 16:52:49.843953	2014-07-14 16:52:49.843953	2014-07-14 16:52:49.843953	5	3	3	1	6	4	4	f	0	0	\N
14	100	1	1	13	PENDING	1	2014-07-14 16:52:49.845971	2014-07-14 16:52:49.845971	2014-07-14 16:52:49.845971	5	3	3	1	6	4	4	f	0	0	\N
15	100	1	1	14	PENDING	1	2014-07-14 16:52:49.847988	2014-07-14 16:52:49.847988	2014-07-14 16:52:49.847988	5	3	3	1	6	4	4	f	0	0	\N
16	100	1	1	15	PENDING	1	2014-07-14 16:52:49.850016	2014-07-14 16:52:49.850016	2014-07-14 16:52:49.850016	5	3	3	1	6	4	4	f	0	0	\N
17	100	1	1	16	PENDING	1	2014-07-14 16:52:49.852029	2014-07-14 16:52:49.852029	2014-07-14 16:52:49.852029	5	3	3	1	6	4	4	f	0	0	\N
18	100	1	1	17	PENDING	1	2014-07-14 16:52:49.854084	2014-07-14 16:52:49.854084	2014-07-14 16:52:49.854084	5	3	3	1	6	4	4	f	0	0	\N
19	100	1	1	18	PENDING	1	2014-07-14 16:52:49.856112	2014-07-14 16:52:49.856112	2014-07-14 16:52:49.856112	5	3	3	1	6	4	4	f	0	0	\N
20	100	1	1	19	PENDING	1	2014-07-14 16:52:49.858117	2014-07-14 16:52:49.858117	2014-07-14 16:52:49.858117	5	3	3	1	6	4	4	f	0	0	\N
21	100	1	1	20	PENDING	1	2014-07-14 16:52:49.860135	2014-07-14 16:52:49.860135	2014-07-14 16:52:49.860135	5	3	3	1	6	4	4	f	0	0	\N
22	100	1	1	21	PENDING	1	2014-07-14 16:52:49.862214	2014-07-14 16:52:49.862214	2014-07-14 16:52:49.862214	5	3	3	1	6	4	4	f	0	0	\N
24	109	2	2	\N	Host creation PENDING event	1	2014-07-14 16:55:56	2014-07-14 16:55:56	2014-07-14 16:55:57.984	5	9	9	1	6	4	1	f	0	0	1134307552
25	109	2	2	\N	Host creation initial state event	1	2014-07-14 16:55:57	2014-07-14 16:55:57	2014-07-14 16:55:58.001	1	9	9	1	6	4	1	f	0	0	1134307552
26	109	2	2	22	Service creation PENDING event	1	2014-07-14 16:55:57	2014-07-14 16:55:57	2014-07-14 16:55:58.263	5	9	9	1	6	4	1	f	0	0	1134307552
27	109	2	2	22	Service creation initial state event	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.348	2	9	9	1	6	4	1	f	0	0	1134307552
28	109	2	2	23	Service creation PENDING event	1	2014-07-14 16:55:57	2014-07-14 16:55:57	2014-07-14 16:55:58.379	5	9	9	1	6	4	1	f	0	0	1134307552
29	109	2	2	23	Service creation initial state event	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.407	2	9	9	1	6	4	1	f	0	0	\N
30	109	2	2	24	Service creation PENDING event	1	2014-07-14 16:55:57	2014-07-14 16:55:57	2014-07-14 16:55:58.435	5	9	9	1	6	4	1	f	0	0	1134307552
31	109	2	2	24	Service creation initial state event	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.476	2	9	9	1	6	4	1	f	0	0	\N
32	107	2	2	\N	Hostgroup cacti_feeder created by feeder cacti_feeder running on montana	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.579	1	9	9	1	6	4	1	f	0	0	1134307552
33	107	2	2	\N	Host 'cacti_feeder_host' created by feeder cacti_feeder running on montana	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.596	1	9	9	1	6	4	1	f	0	0	\N
34	107	2	2	\N	Service 'cacti_feeder_health' created by feeder cacti_feeder running on montana	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.615	1	9	9	1	6	4	1	f	0	0	\N
35	107	2	2	\N	Service 'events_processed' created by feeder cacti_feeder running on montana	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.635	1	9	9	1	6	4	1	f	0	0	\N
36	107	2	2	\N	Service 'cycle_elapsed_time' created by feeder cacti_feeder running on montana	1	2014-07-14 16:55:58	2014-07-14 16:55:58	2014-07-14 16:55:58.658	1	9	9	1	6	4	1	f	0	0	\N
38	100	1	1	\N	OK - 127.0.0.1: rta 0.031ms, lost 0%	1	2014-07-14 16:55:05	2014-07-14 16:55:05	2014-07-14 16:56:01.049	1	9	9	1	6	4	1	f	0	0	-559460682
39	100	1	1	3	OK - total %CPU for process nagios : 0.0	1	2014-07-14 16:55:30	2014-07-14 16:55:30	2014-07-14 16:56:01.106	2	9	9	1	6	4	1	f	0	0	1440976122
40	100	1	1	1	OK - total %CPU for process httpd : 0.5	1	2014-07-14 16:54:34	2014-07-14 16:54:34	2014-07-14 16:56:01.144	2	9	9	1	6	4	1	f	0	0	41520699
41	100	1	1	4	OK - total %CPU for process perl : 14.3	1	2014-07-14 16:55:59	2014-07-14 16:55:59	2014-07-14 16:56:31.328	2	9	9	1	6	4	1	f	0	0	-1245362768
42	100	1	1	5	OK - total %CPU for process syslog-ng : 0.0	1	2014-07-14 16:56:27	2014-07-14 16:56:27	2014-07-14 16:57:01.405	2	9	9	1	6	4	1	f	0	0	497650638
43	100	1	1	2	CRITICAL - total %CPU for process java : 70.4	1	2014-07-14 16:57:05	2014-07-14 16:57:05	2014-07-14 16:57:08.782	20	8	8	1	6	4	1	f	-1672380329	2141641750	-1245545245
44	100	1	1	7	DISK OK - free space: / 130758 MB (61% inode=97%):	1	2014-07-14 16:56:57	2014-07-14 16:56:57	2014-07-14 16:57:31.489	2	9	9	1	6	4	1	f	0	0	1774429434
45	100	1	1	8	OK - load average: 1.45, 1.99, 1.48	1	2014-07-14 16:57:25	2014-07-14 16:57:25	2014-07-14 16:58:01.571	2	9	9	1	6	4	1	f	0	0	-794399632
46	100	1	1	10	OK - total %MEM for process httpd : 0.8	1	2014-07-14 16:57:53	2014-07-14 16:57:53	2014-07-14 16:58:16.63	2	9	9	1	6	4	1	f	0	0	-1354294104
47	100	1	1	12	OK - total %MEM for process nagios : 0.0	1	2014-07-14 16:58:50	2014-07-14 16:58:50	2014-07-14 16:59:16.706	2	9	9	1	6	4	1	f	0	0	1120390189
48	109	2	2	22	cacti_feeder feeder was terminated with an terminate (SIGTERM) signal !!!	1	2014-07-14 16:59:21	2014-07-14 16:59:21	2014-07-14 16:59:21.487	10	7	7	1	6	4	1	f	0	0	1134307552
23	1	1	\N	\N	JMS Queue is initialized and all incoming messages are routed through the persistence store.	2	2014-07-14 16:55:54	2014-07-14 17:02:14	2014-07-14 17:02:14.787	2	9	9	1	6	4	4	f	1898696635	1978529951	1505998205
49	100	1	1	11	WARNING: process java not running !	1	2014-07-14 17:00:25	2014-07-14 17:00:25	2014-07-14 17:02:19.457	3	10	10	1	6	4	1	f	-1423918477	-1671876861	1896017110
50	100	1	1	16	PROCS OK: 1 process with args 'groundwork/foundation/container/jpp/standalone'	1	2014-07-14 17:01:13	2014-07-14 17:01:13	2014-07-14 17:02:27.61	2	9	9	1	6	4	1	f	0	0	165730441
51	100	1	1	14	OK - total %MEM for process syslog-ng : 0.0	1	2014-07-14 16:59:47	2014-07-14 16:59:47	2014-07-14 17:02:27.663	2	9	9	1	6	4	1	f	0	0	1929386939
52	100	1	1	17	NAGIOS OK: 2 processes, status log updated 7 seconds ago	1	2014-07-14 17:01:42	2014-07-14 17:01:42	2014-07-14 17:02:27.711	2	9	9	1	6	4	1	f	0	0	-1963246253
53	100	1	1	18	SWAP OK - 100% free (5829 MB out of 5887 MB)	1	2014-07-14 17:02:10	2014-07-14 17:02:10	2014-07-14 17:02:27.758	2	9	9	1	6	4	1	f	0	0	-794183395
54	100	1	1	13	OK - total %MEM for process perl : 9.4	1	2014-07-14 16:59:19	2014-07-14 16:59:19	2014-07-14 17:02:27.826	2	9	9	1	6	4	1	f	0	0	1896199587
55	100	1	1	9	Memory OK - 9.7% (383140 kB) used	1	2014-07-14 17:00:16	2014-07-14 17:00:16	2014-07-14 17:02:27.877	2	9	9	1	6	4	1	f	0	0	1105897995
56	100	1	1	15	OK: Nagios latency: Min=0.000, Max=1.396, Avg=0.283	1	2014-07-14 17:00:45	2014-07-14 17:00:45	2014-07-14 17:02:27.931	2	9	9	1	6	4	1	f	0	0	942609474
57	100	1	1	6	USERS OK - 4 users currently logged in	1	2014-07-14 17:02:39	2014-07-14 17:02:39	2014-07-14 17:02:58.034	2	9	9	1	6	4	1	f	0	0	1151850430
58	100	1	1	19	TCP OK - 0.001 second response time on port 4913	1	2014-07-14 17:03:07	2014-07-14 17:03:07	2014-07-14 17:03:28.11	2	9	9	1	6	4	1	f	0	0	-33523121
59	100	1	1	20	HTTP OK: HTTP/1.1 200 OK - 1269 bytes in 0.001 second response time	1	2014-07-14 17:03:36	2014-07-14 17:03:36	2014-07-14 17:03:58.166	2	9	9	1	6	4	1	f	0	0	-709099812
60	100	1	1	21	TCP OK - 0.000 second response time on port 5667	1	2014-07-14 17:04:05	2014-07-14 17:04:05	2014-07-14 17:04:28.217	2	9	9	1	6	4	1	f	0	0	-708922569
61	108	3	3	\N	PENDING - host bsm-host is waiting for checks	1	2014-07-14 17:05:04	2014-07-14 17:05:04	2014-07-14 17:05:05.058	5	3	3	1	6	4	1	f	0	0	-1350659022
62	108	3	3	25	PENDING - service: bsm-service-01 waiting for\n          checkresults	1	2014-07-14 17:05:05	2014-07-14 17:05:05	2014-07-14 17:05:05.077	5	3	3	1	6	4	1	f	0	0	\N
63	108	3	3	\N	UP - BSM host running	1	2014-07-14 17:05:08	2014-07-14 17:05:08	2014-07-14 17:05:09.073	1	3	3	1	6	4	1	f	0	0	-1350659022
64	108	3	3	25	OK - 0 problem(s) of 6 members	1	2014-07-14 17:05:08	2014-07-14 17:05:08	2014-07-14 17:05:09.103	2	3	3	1	6	4	1	f	0	0	-1350659022
65	100	4	4	\N	New host-006-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.116	5	3	3	1	6	4	1	f	0	0	-1197204888
66	100	5	5	\N	New host-008-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.137	5	3	3	1	6	4	1	f	0	0	714006958
67	100	6	6	\N	New host-061 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.157	5	3	3	1	6	4	1	f	0	0	-2054415174
68	100	7	7	\N	New host-041-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.177	5	3	3	1	6	4	1	f	0	0	-1891934190
69	100	8	8	\N	New host-005 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.197	5	3	3	1	6	4	1	f	0	0	-2061273390
70	100	9	9	\N	New host-007-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.22	5	3	3	1	6	4	1	f	0	0	-1490606934
71	100	10	10	\N	New host-038-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.239	5	3	3	1	6	4	1	f	0	0	-942203740
72	100	11	11	\N	New host-002 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.259	5	3	3	1	6	4	1	f	0	0	-965386612
73	100	12	12	\N	New host-064-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.279	5	3	3	1	6	4	1	f	0	0	-1285182058
74	100	13	13	\N	New host-039 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.3	5	3	3	1	6	4	1	f	0	0	362594896
75	100	14	14	\N	New host-009-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.323	5	3	3	1	6	4	1	f	0	0	-2077411026
76	100	15	15	\N	New host-042-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.345	5	3	3	1	6	4	1	f	0	0	312679702
77	100	16	16	\N	New host-064-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.365	5	3	3	1	6	4	1	f	0	0	511769300
78	100	17	17	\N	New host-008-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.387	5	3	3	1	6	4	1	f	0	0	-1784008980
79	100	18	18	\N	New host-009-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.411	5	3	3	1	6	4	1	f	0	0	420604912
80	100	19	19	\N	New host-063-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.432	5	3	3	1	6	4	1	f	0	0	-991780012
81	100	20	20	\N	New host-037-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.463	5	3	3	1	6	4	1	f	0	0	1148149664
82	100	21	21	\N	New host-041-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.484	5	3	3	1	6	4	1	f	0	0	606081748
83	100	22	22	\N	New host-010-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.517	5	3	3	1	6	4	1	f	0	0	-81945642
84	100	23	23	\N	New host-037-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.54	5	3	3	1	6	4	1	f	0	0	-648801694
85	100	24	24	\N	New host-043 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.57	5	3	3	1	6	4	1	f	0	0	-1611516094
86	100	25	25	\N	New host-006-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.599	5	3	3	1	6	4	1	f	0	0	1300811050
87	100	26	26	\N	New host-008-c host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.628	5	3	3	1	6	4	1	f	0	0	-1082944400
88	100	27	27	\N	New host-011-a host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.656	5	3	3	1	6	4	1	f	0	0	-375347688
89	100	28	28	\N	New host-001 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.681	5	3	3	1	6	4	1	f	0	0	831564746
90	100	29	29	\N	New host-003 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.704	5	3	3	1	6	4	1	f	0	0	1532629326
91	100	30	30	\N	New host-011-b host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.727	5	3	3	1	6	4	1	f	0	0	2122668250
92	100	31	31	\N	New host-062 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.749	5	3	3	1	6	4	1	f	0	0	443600764
93	100	32	32	\N	New test-host-pattern host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.772	5	3	3	1	6	4	1	f	0	0	1711102120
94	100	33	33	\N	New host-004 host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.807	5	3	3	1	6	4	1	f	0	0	-264322032
95	100	34	34	\N	New host-011-c host is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:39.832	5	3	3	1	6	4	1	f	0	0	325716892
96	100	4	4	26	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.781	5	3	3	1	6	4	1	f	0	0	-2113476198
97	100	5	5	27	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.81	5	3	3	1	6	4	1	f	0	0	-801641068
101	100	8	8	31	New service-005 service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.928	5	3	3	1	6	4	1	f	0	0	-1242757877
102	100	8	8	32	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.953	5	3	3	1	6	4	1	f	0	0	432663536
103	100	9	9	33	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.001	5	3	3	1	6	4	1	f	0	0	929682200
108	100	12	12	38	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.124	5	3	3	1	6	4	1	f	0	0	-675097428
109	100	13	13	39	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.155	5	3	3	1	6	4	1	f	0	0	704410802
116	100	17	17	46	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.324	5	3	3	1	6	4	1	f	0	0	-322126698
117	100	18	18	47	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.348	5	3	3	1	6	4	1	f	0	0	-2053449966
118	100	18	18	48	New service-009-b service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.369	5	3	3	1	6	4	1	f	0	0	-2102047034
119	100	19	19	49	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.39	5	3	3	1	6	4	1	f	0	0	576711470
120	100	20	20	50	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.416	5	3	3	1	6	4	1	f	0	0	798068066
122	100	21	21	52	New service-041-b service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.473	5	3	3	1	6	4	1	f	0	0	1943940566
124	100	22	22	54	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.519	5	3	3	1	6	4	1	f	0	0	1273536108
125	100	23	23	55	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.541	5	3	3	1	6	4	1	f	0	0	318553696
126	100	24	24	56	New service-043 service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.563	5	3	3	1	6	4	1	f	0	0	136224533
127	100	24	24	57	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.584	5	3	3	1	6	4	1	f	0	0	1601453440
128	100	25	25	58	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.605	5	3	3	1	6	4	1	f	0	0	1701976728
129	100	26	26	59	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.626	5	3	3	1	6	4	1	f	0	0	-1281155438
130	100	27	27	60	New service-011-a service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.648	5	3	3	1	6	4	1	f	0	0	-133387276
131	100	27	27	61	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.683	5	3	3	1	6	4	1	f	0	0	21727210
132	100	28	28	62	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.706	5	3	3	1	6	4	1	f	0	0	-1944246280
133	100	29	29	63	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.728	5	3	3	1	6	4	1	f	0	0	1391692276
134	100	29	29	64	New service-003 service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.748	5	3	3	1	6	4	1	f	0	0	-1719778035
135	100	30	30	65	New service-011-b service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.771	5	3	3	1	6	4	1	f	0	0	-705952653
139	100	32	32	69	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.857	5	3	3	1	6	4	1	f	0	0	264332122
140	100	33	33	70	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.878	5	3	3	1	6	4	1	f	0	0	912177906
98	100	6	6	28	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.838	5	3	3	1	6	4	1	f	0	0	-1399604984
99	100	7	7	29	New service-041-a service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.87	5	3	3	1	6	4	1	f	0	0	-1778461353
100	100	7	7	30	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:41.896	5	3	3	1	6	4	1	f	0	0	-432383312
104	100	10	10	34	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.029	5	3	3	1	6	4	1	f	0	0	-933255202
105	100	11	11	35	New service-002 service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.052	5	3	3	1	6	4	1	f	0	0	189195534
106	100	11	11	36	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.074	5	3	3	1	6	4	1	f	0	0	1871206646
107	100	12	12	37	New service-064-b service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.097	5	3	3	1	6	4	1	f	0	0	840340053
110	100	14	14	40	New service-009-a service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.183	5	3	3	1	6	4	1	f	0	0	-1529481657
111	100	14	14	41	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.209	5	3	3	1	6	4	1	f	0	0	-1573935596
112	100	15	15	42	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.233	5	3	3	1	6	4	1	f	0	0	2131260716
113	100	15	15	43	New service-042-b service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.256	5	3	3	1	6	4	1	f	0	0	1464427157
114	100	16	16	44	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.278	5	3	3	1	6	4	1	f	0	0	-195583058
115	100	16	16	45	New service-064-a service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.301	5	3	3	1	6	4	1	f	0	0	1412905430
123	100	22	22	53	New service-010-a service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.497	5	3	3	1	6	4	1	f	0	0	346126133
136	100	30	30	66	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.793	5	3	3	1	6	4	1	f	0	0	-457787160
137	100	31	31	67	New service-062 service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.814	5	3	3	1	6	4	1	f	0	0	-347046184
138	100	31	31	68	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.835	5	3	3	1	6	4	1	f	0	0	-1879119354
141	100	34	34	71	New service-011-c service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.902	5	3	3	1	6	4	1	f	0	0	-1278518030
142	100	34	34	72	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.926	5	3	3	1	6	4	1	f	0	0	-937301530
143	1	1	\N	\N	Foundation-Monarch synch process completed. It might take up to 30 sec. for changes to show up in the status pages.	1	2014-07-14 17:05:48	2014-07-14 17:05:48	2014-07-14 17:05:48.533	2	9	9	1	6	4	1	f	818390582	-1585726254	\N
37	1	1	1	\N	Foundation-Nagios status check process started.	3	2014-07-14 16:56:00	2014-07-14 17:05:53	2014-07-14 17:05:53.665	2	9	9	1	6	4	4	f	-399862680	-197314300	\N
121	100	21	21	51	New icmp_ping_alive service is awaiting first check result.	1	2014-07-14 17:05:38	2014-07-14 17:05:38	2014-07-14 17:05:42.442	5	3	3	1	6	4	1	f	0	0	-911897682
\.


--
-- Name: logmessage_logmessageid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('logmessage_logmessageid_seq', 143, true);


--
-- Data for Name: logmessageproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY logmessageproperty (logmessageid, propertytypeid, valuestring, valuedate, valueboolean, valueinteger, valuelong, valuedouble, lasteditedon, createdon) FROM stdin;
38	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 16:56:01.05	2014-07-14 16:56:01.05
38	31	localhost	\N	\N	\N	\N	\N	2014-07-14 16:56:01.05	2014-07-14 16:56:01.05
39	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:56:01.106	2014-07-14 16:56:01.106
39	31	localhost:local_cpu_nagios	\N	\N	\N	\N	\N	2014-07-14 16:56:01.106	2014-07-14 16:56:01.106
40	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:56:01.145	2014-07-14 16:56:01.145
40	31	localhost:local_cpu_httpd	\N	\N	\N	\N	\N	2014-07-14 16:56:01.145	2014-07-14 16:56:01.145
41	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:56:31.328	2014-07-14 16:56:31.328
41	31	localhost:local_cpu_perl	\N	\N	\N	\N	\N	2014-07-14 16:56:31.328	2014-07-14 16:56:31.328
42	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:57:01.405	2014-07-14 16:57:01.405
42	31	localhost:local_cpu_syslog-ng	\N	\N	\N	\N	\N	2014-07-14 16:57:01.406	2014-07-14 16:57:01.406
43	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:57:08.782	2014-07-14 16:57:08.782
43	31	localhost:local_cpu_java	\N	\N	\N	\N	\N	2014-07-14 16:57:08.782	2014-07-14 16:57:08.782
44	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:57:31.49	2014-07-14 16:57:31.49
44	31	localhost:local_disk_root	\N	\N	\N	\N	\N	2014-07-14 16:57:31.49	2014-07-14 16:57:31.49
45	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:58:01.571	2014-07-14 16:58:01.571
45	31	localhost:local_load	\N	\N	\N	\N	\N	2014-07-14 16:58:01.571	2014-07-14 16:58:01.571
46	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:58:16.631	2014-07-14 16:58:16.631
46	31	localhost:local_mem_httpd	\N	\N	\N	\N	\N	2014-07-14 16:58:16.631	2014-07-14 16:58:16.631
47	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 16:59:16.706	2014-07-14 16:59:16.706
47	31	localhost:local_mem_nagios	\N	\N	\N	\N	\N	2014-07-14 16:59:16.706	2014-07-14 16:59:16.706
49	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:19.494	2014-07-14 17:02:19.494
49	31	localhost:local_mem_java	\N	\N	\N	\N	\N	2014-07-14 17:02:19.494	2014-07-14 17:02:19.494
50	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.61	2014-07-14 17:02:27.61
50	31	localhost:local_process_gw_listener	\N	\N	\N	\N	\N	2014-07-14 17:02:27.61	2014-07-14 17:02:27.61
51	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.663	2014-07-14 17:02:27.663
51	31	localhost:local_mem_syslog-ng	\N	\N	\N	\N	\N	2014-07-14 17:02:27.663	2014-07-14 17:02:27.663
52	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.711	2014-07-14 17:02:27.711
52	31	localhost:local_process_nagios	\N	\N	\N	\N	\N	2014-07-14 17:02:27.711	2014-07-14 17:02:27.711
53	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.758	2014-07-14 17:02:27.758
53	31	localhost:local_swap	\N	\N	\N	\N	\N	2014-07-14 17:02:27.758	2014-07-14 17:02:27.758
54	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.826	2014-07-14 17:02:27.826
54	31	localhost:local_mem_perl	\N	\N	\N	\N	\N	2014-07-14 17:02:27.826	2014-07-14 17:02:27.826
55	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.877	2014-07-14 17:02:27.877
55	31	localhost:local_memory	\N	\N	\N	\N	\N	2014-07-14 17:02:27.877	2014-07-14 17:02:27.877
56	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:27.931	2014-07-14 17:02:27.931
56	31	localhost:local_nagios_latency	\N	\N	\N	\N	\N	2014-07-14 17:02:27.932	2014-07-14 17:02:27.932
57	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:02:58.034	2014-07-14 17:02:58.034
57	31	localhost:local_users	\N	\N	\N	\N	\N	2014-07-14 17:02:58.034	2014-07-14 17:02:58.034
58	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:03:28.11	2014-07-14 17:03:28.11
58	31	localhost:tcp_gw_listener	\N	\N	\N	\N	\N	2014-07-14 17:03:28.11	2014-07-14 17:03:28.11
59	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:03:58.166	2014-07-14 17:03:58.166
59	31	localhost:tcp_http	\N	\N	\N	\N	\N	2014-07-14 17:03:58.166	2014-07-14 17:03:58.166
60	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:04:28.217	2014-07-14 17:04:28.217
60	31	localhost:tcp_nsca	\N	\N	\N	\N	\N	2014-07-14 17:04:28.217	2014-07-14 17:04:28.217
62	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:05.077	2014-07-14 17:05:05.077
62	48	 	\N	\N	\N	\N	\N	2014-07-14 17:05:05.077	2014-07-14 17:05:05.077
64	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:09.104	2014-07-14 17:05:09.104
64	48	 	\N	\N	\N	\N	\N	2014-07-14 17:05:09.104	2014-07-14 17:05:09.104
65	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.117	2014-07-14 17:05:39.117
65	31	host-006-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.117	2014-07-14 17:05:39.117
66	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.137	2014-07-14 17:05:39.137
66	31	host-008-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.137	2014-07-14 17:05:39.137
67	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.157	2014-07-14 17:05:39.157
67	31	host-061	\N	\N	\N	\N	\N	2014-07-14 17:05:39.157	2014-07-14 17:05:39.157
68	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.177	2014-07-14 17:05:39.177
68	31	host-041-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.177	2014-07-14 17:05:39.177
69	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.197	2014-07-14 17:05:39.197
69	31	host-005	\N	\N	\N	\N	\N	2014-07-14 17:05:39.197	2014-07-14 17:05:39.197
70	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.22	2014-07-14 17:05:39.22
70	31	host-007-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.22	2014-07-14 17:05:39.22
71	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.24	2014-07-14 17:05:39.24
71	31	host-038-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.24	2014-07-14 17:05:39.24
72	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.26	2014-07-14 17:05:39.26
72	31	host-002	\N	\N	\N	\N	\N	2014-07-14 17:05:39.26	2014-07-14 17:05:39.26
73	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.279	2014-07-14 17:05:39.279
73	31	host-064-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.279	2014-07-14 17:05:39.279
74	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.3	2014-07-14 17:05:39.3
74	31	host-039	\N	\N	\N	\N	\N	2014-07-14 17:05:39.3	2014-07-14 17:05:39.3
75	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.323	2014-07-14 17:05:39.323
75	31	host-009-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.323	2014-07-14 17:05:39.323
76	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.345	2014-07-14 17:05:39.345
76	31	host-042-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.345	2014-07-14 17:05:39.345
77	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.365	2014-07-14 17:05:39.365
77	31	host-064-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.365	2014-07-14 17:05:39.365
78	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.387	2014-07-14 17:05:39.387
78	31	host-008-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.387	2014-07-14 17:05:39.387
79	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.411	2014-07-14 17:05:39.411
79	31	host-009-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.411	2014-07-14 17:05:39.411
80	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.432	2014-07-14 17:05:39.432
80	31	host-063-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.433	2014-07-14 17:05:39.433
81	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.463	2014-07-14 17:05:39.463
81	31	host-037-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.463	2014-07-14 17:05:39.463
82	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.484	2014-07-14 17:05:39.484
82	31	host-041-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.484	2014-07-14 17:05:39.484
83	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.518	2014-07-14 17:05:39.518
83	31	host-010-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.518	2014-07-14 17:05:39.518
84	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.54	2014-07-14 17:05:39.54
84	31	host-037-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.54	2014-07-14 17:05:39.54
85	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.57	2014-07-14 17:05:39.57
85	31	host-043	\N	\N	\N	\N	\N	2014-07-14 17:05:39.57	2014-07-14 17:05:39.57
86	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.599	2014-07-14 17:05:39.599
86	31	host-006-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.599	2014-07-14 17:05:39.599
87	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.628	2014-07-14 17:05:39.628
87	31	host-008-c	\N	\N	\N	\N	\N	2014-07-14 17:05:39.628	2014-07-14 17:05:39.628
88	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.656	2014-07-14 17:05:39.656
88	31	host-011-a	\N	\N	\N	\N	\N	2014-07-14 17:05:39.656	2014-07-14 17:05:39.656
89	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.681	2014-07-14 17:05:39.681
89	31	host-001	\N	\N	\N	\N	\N	2014-07-14 17:05:39.681	2014-07-14 17:05:39.681
90	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.704	2014-07-14 17:05:39.704
90	31	host-003	\N	\N	\N	\N	\N	2014-07-14 17:05:39.704	2014-07-14 17:05:39.704
91	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.728	2014-07-14 17:05:39.728
91	31	host-011-b	\N	\N	\N	\N	\N	2014-07-14 17:05:39.728	2014-07-14 17:05:39.728
92	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.749	2014-07-14 17:05:39.749
92	31	host-062	\N	\N	\N	\N	\N	2014-07-14 17:05:39.749	2014-07-14 17:05:39.749
93	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.772	2014-07-14 17:05:39.772
93	31	test-host-pattern	\N	\N	\N	\N	\N	2014-07-14 17:05:39.772	2014-07-14 17:05:39.772
94	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.807	2014-07-14 17:05:39.807
94	31	host-004	\N	\N	\N	\N	\N	2014-07-14 17:05:39.807	2014-07-14 17:05:39.807
95	33	HOST ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:39.832	2014-07-14 17:05:39.832
95	31	host-011-c	\N	\N	\N	\N	\N	2014-07-14 17:05:39.832	2014-07-14 17:05:39.832
96	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.781	2014-07-14 17:05:41.781
96	31	host-006-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:41.781	2014-07-14 17:05:41.781
97	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.81	2014-07-14 17:05:41.81
97	31	host-008-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:41.81	2014-07-14 17:05:41.81
101	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.928	2014-07-14 17:05:41.928
101	31	host-005:service-005	\N	\N	\N	\N	\N	2014-07-14 17:05:41.928	2014-07-14 17:05:41.928
102	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.953	2014-07-14 17:05:41.953
102	31	host-005:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:41.953	2014-07-14 17:05:41.953
103	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.002	2014-07-14 17:05:42.002
103	31	host-007-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.002	2014-07-14 17:05:42.002
108	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.124	2014-07-14 17:05:42.124
108	31	host-064-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.124	2014-07-14 17:05:42.124
109	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.155	2014-07-14 17:05:42.155
109	31	host-039:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.155	2014-07-14 17:05:42.155
116	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.324	2014-07-14 17:05:42.324
116	31	host-008-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.325	2014-07-14 17:05:42.325
117	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.348	2014-07-14 17:05:42.348
117	31	host-009-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.348	2014-07-14 17:05:42.348
118	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.369	2014-07-14 17:05:42.369
118	31	host-009-b:service-009-b	\N	\N	\N	\N	\N	2014-07-14 17:05:42.369	2014-07-14 17:05:42.369
119	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.391	2014-07-14 17:05:42.391
119	31	host-063-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.391	2014-07-14 17:05:42.391
120	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.416	2014-07-14 17:05:42.416
120	31	host-037-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.416	2014-07-14 17:05:42.416
122	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.473	2014-07-14 17:05:42.473
122	31	host-041-b:service-041-b	\N	\N	\N	\N	\N	2014-07-14 17:05:42.473	2014-07-14 17:05:42.473
124	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.519	2014-07-14 17:05:42.519
124	31	host-010-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.519	2014-07-14 17:05:42.519
125	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.542	2014-07-14 17:05:42.542
125	31	host-037-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.542	2014-07-14 17:05:42.542
126	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.563	2014-07-14 17:05:42.563
126	31	host-043:service-043	\N	\N	\N	\N	\N	2014-07-14 17:05:42.563	2014-07-14 17:05:42.563
127	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.584	2014-07-14 17:05:42.584
127	31	host-043:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.585	2014-07-14 17:05:42.585
128	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.606	2014-07-14 17:05:42.606
128	31	host-006-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.606	2014-07-14 17:05:42.606
129	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.627	2014-07-14 17:05:42.627
129	31	host-008-c:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.627	2014-07-14 17:05:42.627
130	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.648	2014-07-14 17:05:42.648
130	31	host-011-a:service-011-a	\N	\N	\N	\N	\N	2014-07-14 17:05:42.648	2014-07-14 17:05:42.648
131	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.683	2014-07-14 17:05:42.683
131	31	host-011-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.683	2014-07-14 17:05:42.683
132	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.706	2014-07-14 17:05:42.706
132	31	host-001:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.706	2014-07-14 17:05:42.706
133	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.728	2014-07-14 17:05:42.728
133	31	host-003:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.728	2014-07-14 17:05:42.728
134	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.749	2014-07-14 17:05:42.749
134	31	host-003:service-003	\N	\N	\N	\N	\N	2014-07-14 17:05:42.749	2014-07-14 17:05:42.749
135	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.772	2014-07-14 17:05:42.772
135	31	host-011-b:service-011-b	\N	\N	\N	\N	\N	2014-07-14 17:05:42.772	2014-07-14 17:05:42.772
139	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.857	2014-07-14 17:05:42.857
139	31	test-host-pattern:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.857	2014-07-14 17:05:42.857
140	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.878	2014-07-14 17:05:42.878
140	31	host-004:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.878	2014-07-14 17:05:42.878
98	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.838	2014-07-14 17:05:41.838
98	31	host-061:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:41.838	2014-07-14 17:05:41.838
99	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.87	2014-07-14 17:05:41.87
99	31	host-041-a:service-041-a	\N	\N	\N	\N	\N	2014-07-14 17:05:41.87	2014-07-14 17:05:41.87
100	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:41.897	2014-07-14 17:05:41.897
100	31	host-041-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:41.897	2014-07-14 17:05:41.897
104	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.029	2014-07-14 17:05:42.029
104	31	host-038-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.029	2014-07-14 17:05:42.029
105	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.052	2014-07-14 17:05:42.052
105	31	host-002:service-002	\N	\N	\N	\N	\N	2014-07-14 17:05:42.052	2014-07-14 17:05:42.052
106	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.074	2014-07-14 17:05:42.074
106	31	host-002:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.074	2014-07-14 17:05:42.074
107	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.098	2014-07-14 17:05:42.098
107	31	host-064-b:service-064-b	\N	\N	\N	\N	\N	2014-07-14 17:05:42.098	2014-07-14 17:05:42.098
110	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.183	2014-07-14 17:05:42.183
110	31	host-009-a:service-009-a	\N	\N	\N	\N	\N	2014-07-14 17:05:42.183	2014-07-14 17:05:42.183
111	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.209	2014-07-14 17:05:42.209
111	31	host-009-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.209	2014-07-14 17:05:42.209
112	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.233	2014-07-14 17:05:42.233
112	31	host-042-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.233	2014-07-14 17:05:42.233
113	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.256	2014-07-14 17:05:42.256
113	31	host-042-b:service-042-b	\N	\N	\N	\N	\N	2014-07-14 17:05:42.256	2014-07-14 17:05:42.256
114	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.278	2014-07-14 17:05:42.278
114	31	host-064-a:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.278	2014-07-14 17:05:42.278
115	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.301	2014-07-14 17:05:42.301
115	31	host-064-a:service-064-a	\N	\N	\N	\N	\N	2014-07-14 17:05:42.301	2014-07-14 17:05:42.301
123	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.497	2014-07-14 17:05:42.497
123	31	host-010-a:service-010-a	\N	\N	\N	\N	\N	2014-07-14 17:05:42.497	2014-07-14 17:05:42.497
136	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.793	2014-07-14 17:05:42.793
136	31	host-011-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.793	2014-07-14 17:05:42.793
137	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.814	2014-07-14 17:05:42.814
137	31	host-062:service-062	\N	\N	\N	\N	\N	2014-07-14 17:05:42.814	2014-07-14 17:05:42.814
138	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.835	2014-07-14 17:05:42.835
138	31	host-062:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.835	2014-07-14 17:05:42.835
141	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.902	2014-07-14 17:05:42.902
141	31	host-011-c:service-011-c	\N	\N	\N	\N	\N	2014-07-14 17:05:42.902	2014-07-14 17:05:42.902
142	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.926	2014-07-14 17:05:42.926
142	31	host-011-c:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.926	2014-07-14 17:05:42.926
121	33	SERVICE ALERT	\N	\N	\N	\N	\N	2014-07-14 17:05:42.442	2014-07-14 17:05:42.442
121	31	host-041-b:icmp_ping_alive	\N	\N	\N	\N	\N	2014-07-14 17:05:42.442	2014-07-14 17:05:42.442
\.


--
-- Data for Name: logperformancedata; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY logperformancedata (logperformancedataid, servicestatusid, lastchecktime, maximum, minimum, average, measurementpoints, performancedatalabelid) FROM stdin;
\.


--
-- Name: logperformancedata_logperformancedataid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('logperformancedata_logperformancedataid_seq', 1, false);


--
-- Data for Name: messagefilter; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY messagefilter (messagefilterid, name, regexpresion, ischangeseveritytostatistic) FROM stdin;
\.


--
-- Name: messagefilter_messagefilterid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('messagefilter_messagefilterid_seq', 1, false);


--
-- Data for Name: monitorlist; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY monitorlist (monitorserverid, deviceid) FROM stdin;
1	1
1	2
1	3
1	4
1	5
1	6
1	7
1	8
1	9
1	10
1	11
1	12
1	13
1	14
1	15
1	16
1	17
1	18
1	19
1	20
1	21
1	22
1	23
1	24
1	25
1	26
1	27
1	28
1	29
1	30
1	31
1	32
1	33
1	34
\.


--
-- Data for Name: monitorserver; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY monitorserver (monitorserverid, monitorservername, ip, description) FROM stdin;
1	localhost	127.0.0.1	Default Monitor Server
\.


--
-- Name: monitorserver_monitorserverid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('monitorserver_monitorserverid_seq', 1, true);


--
-- Data for Name: monitorstatus; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY monitorstatus (monitorstatusid, name, description) FROM stdin;
1	UP	UP
2	OK	OK
3	UNKNOWN	UNKNOWN
4	SCHEDULED CRITICAL	SCHEDULED CRITICAL
5	PENDING	PENDING
6	SCHEDULED DOWN	SCHEDULED DOWN
7	UNREACHABLE	UNREACHABLE
8	UNSCHEDULED DOWN	UNSCHEDULED DOWN
9	WARNING	WARNING
10	UNSCHEDULED CRITICAL	UNSCHEDULED CRITICAL
11	ACKNOWLEDGEMENT (WARNING)	ACKNOWLEDGEMENT (WARNING)
12	ACKNOWLEDGEMENT (CRITICAL)	ACKNOWLEDGEMENT (CRITICAL)
13	ACKNOWLEDGEMENT (DOWN)	ACKNOWLEDGEMENT (DOWN)
14	ACKNOWLEDGEMENT (UP)	ACKNOWLEDGEMENT (UP)
15	ACKNOWLEDGEMENT (OK)	ACKNOWLEDGEMENT (OK)
16	ACKNOWLEDGEMENT (UNREACHABLE)	ACKNOWLEDGEMENT (UNREACHABLE)
17	ACKNOWLEDGEMENT (UNKNOWN)	ACKNOWLEDGEMENT (UNKNOWN)
18	ACKNOWLEDGEMENT (PENDING)	ACKNOWLEDGEMENT (PENDING)
19	ACKNOWLEDGEMENT (MAINTENANCE)	ACKNOWLEDGEMENT (MAINTENANCE)
20	CRITICAL	CRITICAL
21	DOWN	DOWN
22	MAINTENANCE	MAINTENANCE
23	SUSPENDED	Virtual Environment specific Host status
\.


--
-- Name: monitorstatus_monitorstatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('monitorstatus_monitorstatusid_seq', 23, true);


--
-- Data for Name: network_service_notifications; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY network_service_notifications (id, created_at, guid, type, title, critical, description, webpage_url, webpage_url_description, update_md5, update_url, update_cmd_switch, update_instruction, update_size, update_type, update_os, is_read, is_archived) FROM stdin;
\.


--
-- Name: network_service_notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('network_service_notifications_id_seq', 1, false);


--
-- Data for Name: network_service_short_news; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY network_service_short_news (id, status, title, message, url, url_description, is_archived) FROM stdin;
\.


--
-- Name: network_service_short_news_id_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('network_service_short_news_id_seq', 1, false);


--
-- Data for Name: network_service_status; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY network_service_status (id, last_checked) FROM stdin;
1	\N
\.


--
-- Name: network_service_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('network_service_status_id_seq', 1, false);


--
-- Data for Name: operationstatus; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY operationstatus (operationstatusid, name, description) FROM stdin;
1	OPEN	Status OPEN
2	CLOSED	Status CLOSED
3	NOTIFIED	Status NOTIFIED
4	ACCEPTED	Status accepted
5	ACKNOWLEDGED	Status Acknowledged
\.


--
-- Name: operationstatus_operationstatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('operationstatus_operationstatusid_seq', 5, true);


--
-- Data for Name: performancedatalabel; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY performancedatalabel (performancedatalabelid, performancename, servicedisplayname, metriclabel, unit) FROM stdin;
1	Current Load_load1	Load average last minute	Load factor	load
2	Current Load_load5	Load average last 5 minutes	Load factor	load
3	Current Load_load15	Load average last 15 minutes	Load factor	load
4	Current Users_users	Users on System	users	users
5	Root Partition_/	Used space on Root partition	Percentage used	%
6	icmp_ping_alive_rta	Ping round trip average	rta	ms
7	icmp_ping_alive_pl	Process load	pl	%
8	http_alive_time	Web Server time	time	sec
9	http_alive_size	Web Server size	size	kb
\.


--
-- Name: performancedatalabel_performancedatalabelid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('performancedatalabel_performancedatalabelid_seq', 9, true);


--
-- Data for Name: plugin; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY plugin (pluginid, name, url, platformid, dependencies, lastupdatetimestamp, checksum, lastupdatedby) FROM stdin;
\.


--
-- Name: plugin_pluginid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('plugin_pluginid_seq', 1, false);


--
-- Data for Name: pluginplatform; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY pluginplatform (platformid, name, arch, description) FROM stdin;
1	Multiplatform	32	Multiplatform 32 bit
2	Multiplatform	64	Multiplatform 64 bit
3	AIX-PowerPC	32	AIX PowerPC 32 bit
4	AIX-PowerPC	64	AIX PowerPC 64 bit
5	Linux-Intel	32	Linux Intel 32 bit
6	Linux-Intel	64	Linux Intel 64 bit
7	Solaris-Intel	32	Solaris Intel 32 bit
8	Solaris-Intel	64	Solaris Intel 64 bit
9	Solaris-SPARC	32	Solaris SPARC 32 bit
10	Solaris-SPARC	64	Solaris SPARC 64 bit
11	Windows-Intel	32	Windows Intel 32 bit
12	Windows-Intel	64	Windows Intel 64 bit
\.


--
-- Name: pluginplatform_platformid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('pluginplatform_platformid_seq', 12, true);


--
-- Data for Name: priority; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY priority (priorityid, name, description) FROM stdin;
1	1	Lowest Priority in a scale from 1 -10
2	2	Low priority in a scale from 1 -10
3	3	Low priority in a scale from 1 -10
4	4	Low priority in a scale from 1 -10
5	5	Medium priority in a scale from 1 -10
6	6	Medium priority in a scale from 1 -10
7	7	Medium-High priority in a scale from 1 -10
8	8	Medium-High priority in a scale from 1 -10
9	9	High priority in a scale from 1 -10
10	10	Highest priority in a scale from 1 -10
\.


--
-- Name: priority_priorityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('priority_priorityid_seq', 10, true);


--
-- Data for Name: propertytype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY propertytype (propertytypeid, name, description, isdate, isboolean, isstring, isinteger, islong, isdouble, isvisible) FROM stdin;
1	LastPluginOutput	Last output received	f	f	t	f	f	f	t
2	LastStateChange	The time of the last change of state	t	f	f	f	f	f	t
3	isAcknowledged	Has the current state been acknowledged?	f	t	f	f	f	f	t
4	TimeUp	The amount of time that the host has been UP	f	f	f	f	t	f	t
5	TimeDown	The amount of time that the host has been DOWN	f	f	f	f	t	f	t
6	TimeUnreachable	The amount of time that the host has been UNREACHABLE	f	f	f	f	t	f	t
7	LastNotificationTime	The time of the last notification	t	f	f	f	f	f	t
8	CurrentNotificationNumber	The count of notifications	f	f	f	t	f	f	t
9	isNotificationsEnabled		f	t	f	f	f	f	t
10	isChecksEnabled		f	t	f	f	f	f	t
11	isEventHandlersEnabled		f	t	f	f	f	f	t
12	isFlapDetectionEnabled		f	t	f	f	f	f	t
13	isHostFlapping		f	t	f	f	f	f	t
14	PercentStateChange		f	f	f	f	f	t	t
15	ScheduledDowntimeDepth		f	f	f	t	f	f	t
16	isFailurePredictionEnabled		f	t	f	f	f	f	t
17	isProcessPerformanceData		f	t	f	f	f	f	t
18	RetryNumber	The number of times an attempt has been made to contact the service	f	f	f	t	f	f	t
19	isAcceptPassiveChecks		f	t	f	f	f	f	t
20	isProblemAcknowledged		f	t	f	f	f	f	t
21	TimeOK	The amount of time that the entity has had a status of OK	f	f	f	f	t	f	t
22	TimeUnknown	The amount of time that the entity has had a status of UNKNOWN	f	f	f	f	t	f	t
23	TimeWarning	The amount of time that the entity has had a status of WARNING	f	f	f	f	t	f	t
24	TimeCritical	The amount of time that the entity has had a status of CRITICAL	f	f	f	f	t	f	t
25	Latency		f	f	f	f	f	t	t
26	ExecutionTime		f	f	f	f	f	t	t
27	isServiceFlapping		f	t	f	f	f	f	t
28	isObsessOverService		f	t	f	f	f	f	t
29	ApplicationName		f	f	t	f	f	f	t
30	ApplicationCode		f	f	t	f	f	f	t
31	SubComponent		f	f	t	f	f	f	t
32	LoggerName		f	f	t	f	f	f	t
33	ErrorType		f	f	t	f	f	f	t
34	30DayMovingAvg		f	f	f	f	f	t	t
35	isPassiveChecksEnabled	Nagios 2.0	f	t	f	f	f	f	t
36	AcknowledgedBy		f	f	t	f	f	f	t
37	AcknowledgeComment		f	f	t	f	f	f	t
38	Parent	List of parent hosts separated by commas	f	f	t	f	f	f	t
39	Alias	Host Alias information	f	f	t	f	f	f	t
40	RRDPath	fully qualified path to RRD image	f	f	t	f	f	f	t
41	RRDLabel	Label for Graph	f	f	t	f	f	f	t
42	RRDCommand	Custom RRD command	f	f	t	f	f	f	t
43	CurrentAttempt	Current attempt running check	f	f	f	f	t	f	t
44	MaxAttempts	Max attempts configured	f	f	f	f	t	f	t
45	isObsessOverHost		f	f	f	t	f	f	t
46	ServiceDependencies		f	f	t	f	f	f	t
47	ExtendedInfo		f	f	t	f	f	f	t
48	Comments	Host or Service Comments in XML format	f	f	t	f	f	f	t
49	CactiRRDCommand	Cacti RRD Command	f	f	t	f	f	f	t
50	RemoteRRDCommand	Remote RRD Command	f	f	t	f	f	f	t
51	Notes	Configuration Notes field	f	f	t	f	f	f	t
52	DeactivationTime	The time when the host was deactivated	f	f	t	f	f	f	t
53	PerformanceData	The last Nagios performance data	f	f	t	f	f	f	t
54	Location	Last output received	f	f	t	f	f	f	t
55	ContactPerson	Last output received	f	f	t	f	f	f	t
56	ContactNumber	Last output received	f	f	t	f	f	f	t
57	ipaddress	ipdddress of snmp device	f	f	t	f	f	f	t
58	Event_OID_numeric	Event_OID_numeric	f	f	t	f	f	f	t
59	Event_OID_symbolic	Event_OID_symbolic of snmp device	f	f	t	f	f	f	t
60	Event_Name	Event_Name	f	f	t	f	f	f	t
61	Category	Category of snmp device	f	f	t	f	f	f	t
62	Variable_Bindings	Variable_Bindings	f	f	t	f	f	f	t
63	UpdatedBy	UpdatedBy	f	f	t	f	f	f	t
\.


--
-- Name: propertytype_propertytypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('propertytype_propertytypeid_seq', 63, true);


--
-- Data for Name: schemainfo; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY schemainfo (name, value) FROM stdin;
Schema Version	${groundwork.version}
Schema created	2
CurrentSchemaVersion	${groundwork.version}
AvailabilityUpdateInterval	60
AvailabilityDataPoints	720
\.


--
-- Data for Name: servicestatus; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY servicestatus (servicestatusid, applicationtypeid, servicedescription, hostid, monitorstatusid, lastchecktime, nextchecktime, laststatechange, lasthardstateid, statetypeid, checktypeid, metrictype, domain, agentid) FROM stdin;
17	100	local_process_nagios	1	2	2014-07-14 16:52:49.806742	2014-07-14 17:11:42	2014-07-14 17:01:42	5	2	1	\N	\N	\N
18	100	local_swap	1	2	2014-07-14 16:52:49.808064	2014-07-14 17:12:10	2014-07-14 17:02:10	5	2	1	\N	\N	\N
11	100	local_mem_java	1	3	2014-07-14 16:58:22	2014-07-14 17:10:22	2014-07-14 17:00:22	5	2	1	\N	\N	\N
13	100	local_mem_perl	1	2	2014-07-14 16:52:49.801418	2014-07-14 17:09:19	2014-07-14 16:59:19	5	2	1	\N	\N	\N
9	100	local_memory	1	2	2014-07-14 16:52:49.796018	2014-07-14 17:10:16	2014-07-14 17:00:16	5	2	1	\N	\N	\N
15	100	local_nagios_latency	1	2	2014-07-14 16:52:49.804089	2014-07-14 17:10:45	2014-07-14 17:00:45	5	2	1	\N	\N	\N
6	100	local_users	1	2	2014-07-14 17:02:39	2014-07-14 17:12:39	2014-07-14 17:02:39	5	2	1	\N	\N	\N
19	100	tcp_gw_listener	1	2	2014-07-14 17:03:07	2014-07-14 17:13:07	2014-07-14 17:03:07	5	2	1	\N	\N	\N
20	100	tcp_http	1	2	2014-07-14 17:03:36	2014-07-14 17:13:36	2014-07-14 17:03:36	5	2	1	\N	\N	\N
21	100	tcp_nsca	1	2	2014-07-14 17:04:05	2014-07-14 17:14:05	2014-07-14 17:04:05	5	2	1	\N	\N	\N
1	100	local_cpu_httpd	1	2	2014-07-14 17:04:34	2014-07-14 17:14:34	2014-07-14 16:54:34	5	2	1	\N	\N	\N
4	100	local_cpu_perl	1	2	2014-07-14 16:55:59	2014-07-14 17:05:59	2014-07-14 16:55:59	5	2	1	\N	\N	\N
5	100	local_cpu_syslog-ng	1	2	2014-07-14 16:56:27	2014-07-14 17:06:27	2014-07-14 16:56:27	5	2	1	\N	\N	\N
2	100	local_cpu_java	1	10	2014-07-14 16:57:02	2014-07-14 17:07:02	2014-07-14 16:55:02	5	2	1	\N	\N	\N
7	100	local_disk_root	1	2	2014-07-14 16:56:57	2014-07-14 17:06:57	2014-07-14 16:56:57	5	2	1	\N	\N	\N
8	100	local_load	1	2	2014-07-14 16:57:25	2014-07-14 17:07:25	2014-07-14 16:57:25	5	2	1	\N	\N	\N
10	100	local_mem_httpd	1	2	2014-07-14 16:57:53	2014-07-14 17:07:53	2014-07-14 16:57:53	5	2	1	\N	\N	\N
12	100	local_mem_nagios	1	2	2014-07-14 16:58:50	2014-07-14 17:08:50	2014-07-14 16:58:50	5	2	1	\N	\N	\N
22	109	cacti_feeder_health	2	2	2014-07-14 17:02:15	\N	\N	2	2	2	\N	\N	662cbe06-0bb2-11e4-adb0-7dfbf6f6a4e2
25	108	bsm-service-01	3	2	2014-07-14 17:05:08	2014-07-14 17:10:08	2014-07-14 17:05:08	5	2	1	\N	\N	\N
23	109	cycle_elapsed_time	2	2	2014-07-14 17:05:17	\N	\N	2	2	2	\N	\N	662cbe06-0bb2-11e4-adb0-7dfbf6f6a4e2
16	100	local_process_gw_listener	1	2	2014-07-14 16:52:49.805436	2014-07-14 17:11:13	2014-07-14 17:01:13	5	2	1	\N	\N	\N
14	100	local_mem_syslog-ng	1	2	2014-07-14 16:52:49.802723	2014-07-14 17:09:47	2014-07-14 16:59:47	5	2	1	\N	\N	\N
24	109	events_processed	2	2	2014-07-14 17:05:17	\N	\N	2	2	2	\N	\N	662cbe06-0bb2-11e4-adb0-7dfbf6f6a4e2
26	100	icmp_ping_alive	4	5	\N	2014-07-14 17:16:02	2014-07-14 17:05:38	5	2	1	\N	\N	\N
27	100	icmp_ping_alive	5	5	\N	2014-07-14 17:09:34	2014-07-14 17:05:38	5	2	1	\N	\N	\N
28	100	icmp_ping_alive	6	5	\N	2014-07-14 17:17:30	2014-07-14 17:05:38	5	2	1	\N	\N	\N
29	100	service-041-a	7	5	\N	2014-07-14 17:13:50	2014-07-14 17:05:38	5	2	1	\N	\N	\N
30	100	icmp_ping_alive	7	5	\N	2014-07-14 17:10:27	2014-07-14 17:05:38	5	2	1	\N	\N	\N
31	100	service-005	8	5	\N	2014-07-14 17:12:39	2014-07-14 17:05:38	5	2	1	\N	\N	\N
32	100	icmp_ping_alive	8	5	\N	2014-07-14 17:09:16	2014-07-14 17:05:38	5	2	1	\N	\N	\N
34	100	icmp_ping_alive	10	5	\N	2014-07-14 17:13:41	2014-07-14 17:05:38	5	2	1	\N	\N	\N
33	100	icmp_ping_alive	9	5	\N	2014-07-14 17:12:48	2014-07-14 17:05:38	5	2	1	\N	\N	\N
35	100	service-002	11	5	\N	2014-07-14 17:15:44	2014-07-14 17:05:38	5	2	1	\N	\N	\N
36	100	icmp_ping_alive	11	5	\N	2014-07-14 17:12:21	2014-07-14 17:05:38	5	2	1	\N	\N	\N
37	100	service-064-b	12	5	\N	2014-07-14 17:11:11	2014-07-14 17:05:38	5	2	1	\N	\N	\N
38	100	icmp_ping_alive	12	5	\N	2014-07-14 17:17:48	2014-07-14 17:05:38	5	2	1	\N	\N	\N
39	100	icmp_ping_alive	13	5	\N	2014-07-14 17:17:04	2014-07-14 17:05:38	5	2	1	\N	\N	\N
40	100	service-009-a	14	5	\N	2014-07-14 17:09:43	2014-07-14 17:05:38	5	2	1	\N	\N	\N
41	100	icmp_ping_alive	14	5	\N	2014-07-14 17:16:20	2014-07-14 17:05:38	5	2	1	\N	\N	\N
3	100	local_cpu_nagios	1	2	2014-07-14 16:52:49.787893	2014-07-14 17:15:30	2014-07-14 16:55:30	5	2	1	\N	\N	\N
42	100	icmp_ping_alive	15	5	\N	2014-07-14 17:13:58	2014-07-14 17:05:38	5	2	1	\N	\N	\N
43	100	service-042-b	15	5	\N	2014-07-14 17:17:21	2014-07-14 17:05:38	5	2	1	\N	\N	\N
44	100	icmp_ping_alive	16	5	\N	2014-07-14 17:11:02	2014-07-14 17:05:38	5	2	1	\N	\N	\N
45	100	service-064-a	16	5	\N	2014-07-14 17:14:25	2014-07-14 17:05:38	5	2	1	\N	\N	\N
46	100	icmp_ping_alive	17	5	\N	2014-07-14 17:16:11	2014-07-14 17:05:38	5	2	1	\N	\N	\N
49	100	icmp_ping_alive	19	5	\N	2014-07-14 17:17:39	2014-07-14 17:05:38	5	2	1	\N	\N	\N
47	100	icmp_ping_alive	18	5	\N	2014-07-14 17:13:06	2014-07-14 17:05:38	5	2	1	\N	\N	\N
48	100	service-009-b	18	5	\N	2014-07-14 17:16:28	2014-07-14 17:05:38	5	2	1	\N	\N	\N
51	100	icmp_ping_alive	21	5	\N	2014-07-14 17:17:13	2014-07-14 17:05:38	5	2	1	\N	\N	\N
52	100	service-041-b	21	5	\N	2014-07-14 17:10:36	2014-07-14 17:05:38	5	2	1	\N	\N	\N
50	100	icmp_ping_alive	20	5	\N	2014-07-14 17:16:55	2014-07-14 17:05:38	5	2	1	\N	\N	\N
53	100	service-010-a	22	5	\N	2014-07-14 17:13:14	2014-07-14 17:05:38	5	2	1	\N	\N	\N
54	100	icmp_ping_alive	22	5	\N	2014-07-14 17:09:51	2014-07-14 17:05:38	5	2	1	\N	\N	\N
55	100	icmp_ping_alive	23	5	\N	2014-07-14 17:10:18	2014-07-14 17:05:38	5	2	1	\N	\N	\N
56	100	service-043	24	5	\N	2014-07-14 17:14:07	2014-07-14 17:05:38	5	2	1	\N	\N	\N
57	100	icmp_ping_alive	24	5	\N	2014-07-14 17:10:44	2014-07-14 17:05:38	5	2	1	\N	\N	\N
58	100	icmp_ping_alive	25	5	\N	2014-07-14 17:09:25	2014-07-14 17:05:38	5	2	1	\N	\N	\N
59	100	icmp_ping_alive	26	5	\N	2014-07-14 17:12:57	2014-07-14 17:05:38	5	2	1	\N	\N	\N
62	100	icmp_ping_alive	28	5	\N	2014-07-14 17:08:58	2014-07-14 17:05:38	5	2	1	\N	\N	\N
60	100	service-011-a	27	5	\N	2014-07-14 17:10:00	2014-07-14 17:05:38	5	2	1	\N	\N	\N
61	100	icmp_ping_alive	27	5	\N	2014-07-14 17:16:37	2014-07-14 17:05:38	5	2	1	\N	\N	\N
63	100	icmp_ping_alive	29	5	\N	2014-07-14 17:09:07	2014-07-14 17:05:38	5	2	1	\N	\N	\N
64	100	service-003	29	5	\N	2014-07-14 17:12:30	2014-07-14 17:05:38	5	2	1	\N	\N	\N
65	100	service-011-b	30	5	\N	2014-07-14 17:16:46	2014-07-14 17:05:38	5	2	1	\N	\N	\N
66	100	icmp_ping_alive	30	5	\N	2014-07-14 17:13:23	2014-07-14 17:05:38	5	2	1	\N	\N	\N
67	100	service-062	31	5	\N	2014-07-14 17:14:16	2014-07-14 17:05:38	5	2	1	\N	\N	\N
68	100	icmp_ping_alive	31	5	\N	2014-07-14 17:10:53	2014-07-14 17:05:38	5	2	1	\N	\N	\N
69	100	icmp_ping_alive	32	5	\N	2014-07-14 17:14:34	2014-07-14 17:05:38	5	2	1	\N	\N	\N
70	100	icmp_ping_alive	33	5	\N	2014-07-14 17:15:53	2014-07-14 17:05:38	5	2	1	\N	\N	\N
71	100	service-011-c	34	5	\N	2014-07-14 17:13:32	2014-07-14 17:05:38	5	2	1	\N	\N	\N
72	100	icmp_ping_alive	34	5	\N	2014-07-14 17:10:09	2014-07-14 17:05:38	5	2	1	\N	\N	\N
\.


--
-- Name: servicestatus_servicestatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('servicestatus_servicestatusid_seq', 72, true);


--
-- Data for Name: servicestatusproperty; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY servicestatusproperty (servicestatusid, propertytypeid, valuestring, valuedate, valueboolean, valueinteger, valuelong, valuedouble, lasteditedon, createdon) FROM stdin;
16	26	\N	\N	\N	\N	\N	421	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
3	25	\N	\N	\N	\N	\N	61	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
6	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
16	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
16	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.246	2014-07-14 16:56:16.246
16	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.246	2014-07-14 16:56:16.246
16	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
16	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.246	2014-07-14 16:56:16.246
16	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.246	2014-07-14 16:56:16.246
16	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.246	2014-07-14 16:56:16.246
16	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
16	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
16	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
16	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
20	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
10	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.343	2014-07-14 16:56:16.343
14	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.373	2014-07-14 16:56:16.373
14	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
2	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
3	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.436	2014-07-14 16:56:16.436
3	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	53	%CPU=0.0;40;50	\N	\N	\N	\N	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	1	OK - total %CPU for process nagios : 0.0	\N	\N	\N	\N	\N	2014-07-14 16:56:16.436	2014-07-14 16:56:16.436
3	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.436	2014-07-14 16:56:16.436
3	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.436	2014-07-14 16:56:16.436
3	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.436	2014-07-14 16:56:16.436
3	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
3	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
1	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.47	2014-07-14 16:56:16.47
1	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.47	2014-07-14 16:56:16.47
1	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.47	2014-07-14 16:56:16.47
1	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
10	25	\N	\N	\N	\N	\N	75	2014-07-14 16:56:16.342	2014-07-14 16:56:16.342
10	26	\N	\N	\N	\N	\N	156	2014-07-14 16:56:16.343	2014-07-14 16:56:16.343
24	1	0 events processed out of a total possible 0 events	\N	\N	\N	\N	\N	2014-07-14 16:55:58.209	2014-07-14 16:55:58.209
2	14	\N	\N	\N	\N	\N	6.25	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	1	CRITICAL - total %CPU for process java : 70.4	\N	\N	\N	\N	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	25	\N	\N	\N	\N	\N	7	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
2	26	\N	\N	\N	\N	\N	77	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
22	1	OK - started at Mon Jul 14 17:02:14 2014	\N	\N	\N	\N	\N	2014-07-14 16:55:58.175	2014-07-14 16:55:58.175
16	25	\N	\N	\N	\N	\N	429	2014-07-14 16:56:16.247	2014-07-14 16:56:16.247
14	26	\N	\N	\N	\N	\N	51	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
14	25	\N	\N	\N	\N	\N	483	2014-07-14 16:56:16.374	2014-07-14 16:56:16.374
6	25	\N	\N	\N	\N	\N	279	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
6	26	\N	\N	\N	\N	\N	21	2014-07-14 16:56:16.207	2014-07-14 16:56:16.207
20	25	\N	\N	\N	\N	\N	180	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
20	26	\N	\N	\N	\N	\N	6	2014-07-14 16:56:16.284	2014-07-14 16:56:16.284
1	26	\N	\N	\N	\N	\N	145	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	25	\N	\N	\N	\N	\N	262	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	1	OK - total %CPU for process httpd : 0.0	\N	\N	\N	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
1	53	%CPU=0.0;40;50	\N	\N	\N	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
23	1	Cycle 2 elapsed processing time : 1.12 seconds	\N	\N	\N	\N	\N	2014-07-14 16:55:58.195	2014-07-14 16:55:58.195
3	26	\N	\N	\N	\N	\N	56	2014-07-14 16:56:16.435	2014-07-14 16:56:16.435
1	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.469	2014-07-14 16:56:16.469
5	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.504	2014-07-14 16:56:16.504
5	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.504	2014-07-14 16:56:16.504
5	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
5	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
17	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
18	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
18	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
18	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
18	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
18	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
18	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.58	2014-07-14 16:56:16.58
12	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
12	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
21	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
4	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
19	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
7	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
7	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
11	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
11	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
13	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
5	26	\N	\N	\N	\N	\N	56	2014-07-14 16:56:16.504	2014-07-14 16:56:16.504
5	25	\N	\N	\N	\N	\N	162	2014-07-14 16:56:16.503	2014-07-14 16:56:16.503
7	25	\N	\N	\N	\N	\N	1000	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
19	26	\N	\N	\N	\N	\N	9	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
19	25	\N	\N	\N	\N	\N	148	2014-07-14 16:56:16.748	2014-07-14 16:56:16.748
12	26	\N	\N	\N	\N	\N	55	2014-07-14 16:56:16.646	2014-07-14 16:56:16.646
12	25	\N	\N	\N	\N	\N	146	2014-07-14 16:56:16.645	2014-07-14 16:56:16.645
17	25	\N	\N	\N	\N	\N	118	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
17	26	\N	\N	\N	\N	\N	311	2014-07-14 16:56:16.536	2014-07-14 16:56:16.536
18	26	\N	\N	\N	\N	\N	58	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
18	25	\N	\N	\N	\N	\N	575	2014-07-14 16:56:16.581	2014-07-14 16:56:16.581
11	43	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.795	2014-07-14 16:56:16.795
11	14	\N	\N	\N	\N	\N	6.25	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
21	26	\N	\N	\N	\N	\N	3	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
21	25	\N	\N	\N	\N	\N	230	2014-07-14 16:56:16.682	2014-07-14 16:56:16.682
13	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.816	2014-07-14 16:56:16.816
13	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.816	2014-07-14 16:56:16.816
9	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
8	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.882	2014-07-14 16:56:16.882
8	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.882	2014-07-14 16:56:16.882
8	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
8	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
15	14	\N	\N	\N	\N	\N	0	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	8	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	11	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	19	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	20	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	10	\N	\N	t	\N	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	44	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	12	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.909	2014-07-14 16:56:16.909
15	9	\N	\N	f	\N	\N	\N	2014-07-14 16:56:16.91	2014-07-14 16:56:16.91
15	43	\N	\N	\N	\N	1	\N	2014-07-14 16:56:16.91	2014-07-14 16:56:16.91
15	15	\N	\N	\N	0	\N	\N	2014-07-14 16:56:16.91	2014-07-14 16:56:16.91
4	1	OK - total %CPU for process perl : 14.3	\N	\N	\N	\N	\N	2014-07-14 16:56:31.386	2014-07-14 16:56:31.386
4	53	%CPU=14.3;40;50	\N	\N	\N	\N	\N	2014-07-14 16:56:31.386	2014-07-14 16:56:31.386
4	25	\N	\N	\N	\N	\N	915	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
4	26	\N	\N	\N	\N	\N	223	2014-07-14 16:56:16.725	2014-07-14 16:56:16.725
5	1	OK - total %CPU for process syslog-ng : 0.0	\N	\N	\N	\N	\N	2014-07-14 16:57:01.426	2014-07-14 16:57:01.426
5	53	%CPU=0.0;40;50	\N	\N	\N	\N	\N	2014-07-14 16:57:01.426	2014-07-14 16:57:01.426
2	43	\N	\N	\N	\N	3	\N	2014-07-14 16:56:16.405	2014-07-14 16:56:16.405
2	53	%CPU=70.4;40;50	\N	\N	\N	\N	\N	2014-07-14 16:56:16.404	2014-07-14 16:56:16.404
7	53	/=82757MB;191365;202622;0;225136	\N	\N	\N	\N	\N	2014-07-14 16:57:31.47	2014-07-14 16:57:31.47
7	1	DISK OK - free space: / 130758 MB (61% inode=97%):	\N	\N	\N	\N	\N	2014-07-14 16:57:31.47	2014-07-14 16:57:31.47
7	26	\N	\N	\N	\N	\N	81	2014-07-14 16:56:16.769	2014-07-14 16:56:16.769
8	53	load1=1.450;5.000;10.000;0; load5=1.990;4.000;8.000;0; load15=1.480;3.000;6.000;0;	\N	\N	\N	\N	\N	2014-07-14 16:58:01.562	2014-07-14 16:58:01.562
8	1	OK - load average: 1.45, 1.99, 1.48	\N	\N	\N	\N	\N	2014-07-14 16:58:01.561	2014-07-14 16:58:01.561
8	26	\N	\N	\N	\N	\N	9	2014-07-14 16:56:16.882	2014-07-14 16:56:16.882
8	25	\N	\N	\N	\N	\N	35	2014-07-14 16:56:16.881	2014-07-14 16:56:16.881
10	1	OK - total %MEM for process httpd : 0.8	\N	\N	\N	\N	\N	2014-07-14 16:58:16.609	2014-07-14 16:58:16.609
10	53	%MEM=0.8;20;30	\N	\N	\N	\N	\N	2014-07-14 16:58:16.609	2014-07-14 16:58:16.609
1	40	/usr/local/groundwork/rrd/localhost_local_cpu_httpd.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.085	2014-07-14 16:59:06.085
1	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_cpu_httpd.rrd":CPU:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_cpu_httpd.rrd":CPU_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_cpu_httpd.rrd":CPU_cr:AVERAGE   CDEF:cdefa=a   CDEF:cdefb=a,0.99,*   AREA:cdefa#7D1B7E:"Process CPU Utilization"   GPRINT:cdefa:LAST:Current=%.2lf   GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf   GPRINT:cdefa:MAX:max="%.2lf\\l"   AREA:cdefb#571B7E:   CDEF:cdefw=w  CDEF:cdefc=c   CDEF:cdefm=cdefc,1.01,*   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"   COMMENT:"Service\\: local_cpu_httpd"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 --rigid -u 100 -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.085	2014-07-14 16:59:06.085
1	41	CPU Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.085	2014-07-14 16:59:06.085
1	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.085	2014-07-14 16:59:06.085
2	41	CPU Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.106	2014-07-14 16:59:06.106
2	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_cpu_java.rrd":CPU:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_cpu_java.rrd":CPU_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_cpu_java.rrd":CPU_cr:AVERAGE   CDEF:cdefa=a   CDEF:cdefb=a,0.99,*   AREA:cdefa#7D1B7E:"Process CPU Utilization"   GPRINT:cdefa:LAST:Current=%.2lf   GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf   GPRINT:cdefa:MAX:max="%.2lf\\l"   AREA:cdefb#571B7E:   CDEF:cdefw=w  CDEF:cdefc=c   CDEF:cdefm=cdefc,1.01,*   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"   COMMENT:"Service\\: local_cpu_java"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 --rigid -u 100 -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.106	2014-07-14 16:59:06.106
2	40	/usr/local/groundwork/rrd/localhost_local_cpu_java.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.106	2014-07-14 16:59:06.106
2	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.106	2014-07-14 16:59:06.106
11	53		\N	\N	\N	\N	\N	2014-07-14 16:58:46.676	2014-07-14 16:58:46.676
11	1	WARNING: process java not running !	\N	\N	\N	\N	\N	2014-07-14 16:58:46.676	2014-07-14 16:58:46.676
13	25	\N	\N	\N	\N	\N	288	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
13	26	\N	\N	\N	\N	\N	203	2014-07-14 16:56:16.817	2014-07-14 16:56:16.817
9	25	\N	\N	\N	\N	\N	517	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
9	26	\N	\N	\N	\N	\N	114	2014-07-14 16:56:16.852	2014-07-14 16:56:16.852
15	26	\N	\N	\N	\N	\N	46	2014-07-14 16:56:16.91	2014-07-14 16:56:16.91
15	25	\N	\N	\N	\N	\N	553	2014-07-14 16:56:16.91	2014-07-14 16:56:16.91
26	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
3	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_cpu_nagios.rrd":CPU:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_cpu_nagios.rrd":CPU_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_cpu_nagios.rrd":CPU_cr:AVERAGE   CDEF:cdefa=a   CDEF:cdefb=a,0.99,*   AREA:cdefa#7D1B7E:"Process CPU Utilization"   GPRINT:cdefa:LAST:Current=%.2lf   GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf   GPRINT:cdefa:MAX:max="%.2lf\\l"   AREA:cdefb#571B7E:   CDEF:cdefw=w  CDEF:cdefc=c   CDEF:cdefm=cdefc,1.01,*   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"   COMMENT:"Service\\: local_cpu_nagios"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 --rigid -u 100 -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.123	2014-07-14 16:59:06.123
3	41	CPU Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.123	2014-07-14 16:59:06.123
3	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.123	2014-07-14 16:59:06.123
3	40	/usr/local/groundwork/rrd/localhost_local_cpu_nagios.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.123	2014-07-14 16:59:06.123
4	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.139	2014-07-14 16:59:06.139
4	41	CPU Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.139	2014-07-14 16:59:06.139
4	40	/usr/local/groundwork/rrd/localhost_local_cpu_perl.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.139	2014-07-14 16:59:06.139
4	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_cpu_perl.rrd":CPU:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_cpu_perl.rrd":CPU_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_cpu_perl.rrd":CPU_cr:AVERAGE   CDEF:cdefa=a   CDEF:cdefb=a,0.99,*   AREA:cdefa#7D1B7E:"Process CPU Utilization"   GPRINT:cdefa:LAST:Current=%.2lf   GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf   GPRINT:cdefa:MAX:max="%.2lf\\l"   AREA:cdefb#571B7E:   CDEF:cdefw=w  CDEF:cdefc=c   CDEF:cdefm=cdefc,1.01,*   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"   COMMENT:"Service\\: local_cpu_perl"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 --rigid -u 100 -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.139	2014-07-14 16:59:06.139
5	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_cpu_syslog-ng.rrd":CPU:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_cpu_syslog-ng.rrd":CPU_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_cpu_syslog-ng.rrd":CPU_cr:AVERAGE   CDEF:cdefa=a   CDEF:cdefb=a,0.99,*   AREA:cdefa#7D1B7E:"Process CPU Utilization"   GPRINT:cdefa:LAST:Current=%.2lf   GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf   GPRINT:cdefa:MAX:max="%.2lf\\l"   AREA:cdefb#571B7E:   CDEF:cdefw=w  CDEF:cdefc=c   CDEF:cdefm=cdefc,1.01,*   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"   COMMENT:"Service\\: local_cpu_syslog-ng"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 --rigid -u 100 -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.154	2014-07-14 16:59:06.154
5	41	CPU Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.154	2014-07-14 16:59:06.154
5	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.154	2014-07-14 16:59:06.154
5	40	/usr/local/groundwork/rrd/localhost_local_cpu_syslog-ng.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.154	2014-07-14 16:59:06.154
7	40	/usr/local/groundwork/rrd/localhost_local_disk_root.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.17	2014-07-14 16:59:06.17
7	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_disk_root.rrd":root:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_disk_root.rrd":root_wn:AVERAGE  DEF:c="/usr/local/groundwork/rrd/localhost_local_disk_root.rrd":root_cr:AVERAGE  DEF:m="/usr/local/groundwork/rrd/localhost_local_disk_root.rrd":root_mx:AVERAGE  CDEF:cdefa=a,m,/,100,*   CDEF:cdefb=a,0.99,*  CDEF:cdefw=w  CDEF:cdefc=c  CDEF:cdefm=m    AREA:a#C35617:"Space Used\\: "  LINE:cdefa#FFCC00:  GPRINT:a:LAST:"%.2lf MB\\l"  LINE2:cdefw#FFFF00:"Warning Threshold\\:"  GPRINT:cdefw:AVERAGE:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:AVERAGE:"%.2lf\\l"   GPRINT:cdefa:AVERAGE:"Percentage Space Used"=%.2lf  GPRINT:cdefm:AVERAGE:"Maximum Capacity"=%.2lf  CDEF:cdefws=a,cdefw,GT,a,0,IF  AREA:cdefws#FFFF00  CDEF:cdefcs=a,cdefc,GT,a,0,IF  AREA:cdefcs#FF0033  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y  -l 0	\N	\N	\N	\N	\N	2014-07-14 16:59:06.17	2014-07-14 16:59:06.17
7	41	Disk Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.169	2014-07-14 16:59:06.169
7	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.17	2014-07-14 16:59:06.17
8	40	/usr/local/groundwork/rrd/localhost_local_load.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.185	2014-07-14 16:59:06.185
8	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.185	2014-07-14 16:59:06.185
8	42	rrdtool graph - --imgformat=PNG --slope-mode   DEF:a=/usr/local/groundwork/rrd/localhost_local_load.rrd:load1:AVERAGE   DEF:aw="/usr/local/groundwork/rrd/localhost_local_load.rrd":load1_wn:AVERAGE  DEF:ac="/usr/local/groundwork/rrd/localhost_local_load.rrd":load1_cr:AVERAGE  DEF:b=/usr/local/groundwork/rrd/localhost_local_load.rrd:load5:AVERAGE   DEF:bw="/usr/local/groundwork/rrd/localhost_local_load.rrd":load5_wn:AVERAGE  DEF:bc="/usr/local/groundwork/rrd/localhost_local_load.rrd":load5_cr:AVERAGE  DEF:c=/usr/local/groundwork/rrd/localhost_local_load.rrd:load15:AVERAGE  DEF:cw="/usr/local/groundwork/rrd/localhost_local_load.rrd":load15_wn:AVERAGE  DEF:cc="/usr/local/groundwork/rrd/localhost_local_load.rrd":load15_cr:AVERAGE  CDEF:cdefa=a   CDEF:cdefb=b   CDEF:cdefc=c   AREA:cdefa#FF6600:"One Minute Load Average" GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf    GPRINT:cdefa:MAX:"max=%.2lf\\l"  LINE:aw#FFCC33:"1 min avg Warning Threshold"   GPRINT:aw:LAST:"%.1lf"  LINE:ac#FF0000:"1 min avg Critical Threshold"  GPRINT:ac:LAST:"%.1lf\\l"  LINE2:cdefb#3300FF:"Five Minute Load Average"  GPRINT:cdefb:MIN:min=%.2lf  GPRINT:cdefb:AVERAGE:avg=%.2lf  GPRINT:cdefb:MAX:"max=%.2lf\\l"   LINE:bw#6666CC:"5 min avg Warning Threshold"  GPRINT:bw:LAST:"%.1lf"  LINE:bc#CC0000:"5 min avg Critical Threshold"  GPRINT:bc:LAST:"%.1lf\\l"  LINE3:cdefc#999999:"Fifteen Minute Load Average"     GPRINT:cdefc:MIN:min=%.2lf  GPRINT:cdefc:AVERAGE:avg=%.2lf   GPRINT:cdefc:MAX:"max=%.2lf\\l"   LINE:cw#CCCC99:"15 min avg Warning Threshold"  GPRINT:cw:LAST:"%.1lf"  LINE:cc#990000:"15 min avg Critical Threshold"  GPRINT:cc:LAST:"%.1lf\\l"  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF-Y --height 120	\N	\N	\N	\N	\N	2014-07-14 16:59:06.185	2014-07-14 16:59:06.185
8	41	Load Averages	\N	\N	\N	\N	\N	2014-07-14 16:59:06.185	2014-07-14 16:59:06.185
26	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
26	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
27	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.29	2014-07-14 17:05:54.29
27	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
10	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_mem_httpd.rrd":MEM:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_mem_httpd.rrd":MEM_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_mem_httpd.rrd":MEM_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_mem_httpd"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 16:59:06.2	2014-07-14 16:59:06.2
10	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.2	2014-07-14 16:59:06.2
10	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.2	2014-07-14 16:59:06.2
10	40	/usr/local/groundwork/rrd/localhost_local_mem_httpd.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.2	2014-07-14 16:59:06.2
11	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.216	2014-07-14 16:59:06.216
11	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_mem_java.rrd":MEM:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_mem_java.rrd":MEM_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_mem_java.rrd":MEM_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_mem_java"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 16:59:06.216	2014-07-14 16:59:06.216
11	40	/usr/local/groundwork/rrd/localhost_local_mem_java.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.216	2014-07-14 16:59:06.216
11	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.216	2014-07-14 16:59:06.216
12	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 16:59:06.231	2014-07-14 16:59:06.231
12	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_mem_nagios.rrd":MEM:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_mem_nagios.rrd":MEM_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_mem_nagios.rrd":MEM_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_mem_nagios"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 16:59:06.231	2014-07-14 16:59:06.231
12	40	/usr/local/groundwork/rrd/localhost_local_mem_nagios.rrd	\N	\N	\N	\N	\N	2014-07-14 16:59:06.231	2014-07-14 16:59:06.231
12	50	 	\N	\N	\N	\N	\N	2014-07-14 16:59:06.231	2014-07-14 16:59:06.231
12	53	%MEM=0.0;20;30	\N	\N	\N	\N	\N	2014-07-14 16:59:16.703	2014-07-14 16:59:16.703
12	1	OK - total %MEM for process nagios : 0.0	\N	\N	\N	\N	\N	2014-07-14 16:59:16.703	2014-07-14 16:59:16.703
16	1	PROCS OK: 1 process with args 'groundwork/foundation/container/jpp/standalone'	\N	\N	\N	\N	\N	2014-07-14 17:02:43.138	2014-07-14 17:02:43.138
14	1	OK - total %MEM for process syslog-ng : 0.0	\N	\N	\N	\N	\N	2014-07-14 17:02:43.261	2014-07-14 17:02:43.261
14	53	%MEM=0.0;20;30	\N	\N	\N	\N	\N	2014-07-14 17:02:43.261	2014-07-14 17:02:43.261
17	1	NAGIOS OK: 2 processes, status log updated 7 seconds ago	\N	\N	\N	\N	\N	2014-07-14 17:02:43.531	2014-07-14 17:02:43.531
18	1	SWAP OK - 100% free (5829 MB out of 5887 MB)	\N	\N	\N	\N	\N	2014-07-14 17:02:43.58	2014-07-14 17:02:43.58
18	53	swap=5829MB;1177;588;0;5887	\N	\N	\N	\N	\N	2014-07-14 17:02:43.58	2014-07-14 17:02:43.58
11	26	\N	\N	\N	\N	\N	15	2014-07-14 16:56:16.795	2014-07-14 16:56:16.795
11	25	\N	\N	\N	\N	\N	525	2014-07-14 16:56:16.794	2014-07-14 16:56:16.794
13	1	OK - total %MEM for process perl : 9.4	\N	\N	\N	\N	\N	2014-07-14 17:02:43.924	2014-07-14 17:02:43.924
13	53	%MEM=9.4;20;30	\N	\N	\N	\N	\N	2014-07-14 17:02:43.924	2014-07-14 17:02:43.924
9	1	Memory OK - 9.7% (383140 kB) used	\N	\N	\N	\N	\N	2014-07-14 17:02:43.944	2014-07-14 17:02:43.944
9	53	pct=9.7;95;99;0;100	\N	\N	\N	\N	\N	2014-07-14 17:02:43.944	2014-07-14 17:02:43.944
15	1	OK: Nagios latency: Min=0.000, Max=1.396, Avg=0.283	\N	\N	\N	\N	\N	2014-07-14 17:02:44.022	2014-07-14 17:02:44.022
15	53	Min=0.000;;;; Max=1.396;;;; Avg=0.283;300;900;;	\N	\N	\N	\N	\N	2014-07-14 17:02:44.023	2014-07-14 17:02:44.023
6	53	users=4;5;20;0	\N	\N	\N	\N	\N	2014-07-14 17:02:58.041	2014-07-14 17:02:58.041
6	1	USERS OK - 4 users currently logged in	\N	\N	\N	\N	\N	2014-07-14 17:02:58.041	2014-07-14 17:02:58.041
19	53	time=0.000552s;5.000000;9.000000;0.000000;10.000000	\N	\N	\N	\N	\N	2014-07-14 17:03:28.087	2014-07-14 17:03:28.087
19	1	TCP OK - 0.001 second response time on port 4913	\N	\N	\N	\N	\N	2014-07-14 17:03:28.087	2014-07-14 17:03:28.087
20	53	time=0.000513s;3.000000;5.000000;0.000000 size=1269B;;;0	\N	\N	\N	\N	\N	2014-07-14 17:03:58.175	2014-07-14 17:03:58.175
20	1	HTTP OK: HTTP/1.1 200 OK - 1269 bytes in 0.001 second response time	\N	\N	\N	\N	\N	2014-07-14 17:03:58.175	2014-07-14 17:03:58.175
13	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 17:04:06.611	2014-07-14 17:04:06.611
13	40	/usr/local/groundwork/rrd/localhost_local_mem_perl.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.611	2014-07-14 17:04:06.611
13	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.611	2014-07-14 17:04:06.611
13	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_mem_perl.rrd":MEM:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_mem_perl.rrd":MEM_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_mem_perl.rrd":MEM_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_mem_perl"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 17:04:06.611	2014-07-14 17:04:06.611
14	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 17:04:06.63	2014-07-14 17:04:06.63
27	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.29	2014-07-14 17:05:54.29
27	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
14	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_mem_syslog-ng.rrd":MEM:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_mem_syslog-ng.rrd":MEM_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_mem_syslog-ng.rrd":MEM_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_mem_syslog-ng"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 17:04:06.63	2014-07-14 17:04:06.63
14	40	/usr/local/groundwork/rrd/localhost_local_mem_syslog-ng.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.63	2014-07-14 17:04:06.63
14	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.63	2014-07-14 17:04:06.63
9	40	/usr/local/groundwork/rrd/localhost_local_memory.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.648	2014-07-14 17:04:06.648
9	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_memory.rrd":pct:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_memory.rrd":pct_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_memory.rrd":pct_cr:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=a,0.99,*   CDEF:cdefw=w   CDEF:cdefc=c   CDEF:cdefm=c,1.05,*  AREA:a#33FFFF   AREA:cdefb#3399FF:"Memory Utilized\\:"   GPRINT:a:LAST:"%.2lf Percent"  GPRINT:cdefa:MIN:min=%.2lf  GPRINT:cdefa:AVERAGE:avg=%.2lf  GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:LAST:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Service\\: local_memory"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  CDEF:cdefwt=a,cdefw,GT,cdefw,0,IF LINE:cdefwt#000000 CDEF:cdefct=a,cdefc,GT,cdefc,0,IF LINE:cdefct#000000  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -u 100 -l 0 --rigid	\N	\N	\N	\N	\N	2014-07-14 17:04:06.648	2014-07-14 17:04:06.648
9	41	Memory Utilization	\N	\N	\N	\N	\N	2014-07-14 17:04:06.648	2014-07-14 17:04:06.648
9	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.648	2014-07-14 17:04:06.648
15	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.667	2014-07-14 17:04:06.667
15	41	Nagios Service Check Latency in Seconds	\N	\N	\N	\N	\N	2014-07-14 17:04:06.667	2014-07-14 17:04:06.667
15	40	/usr/local/groundwork/rrd/localhost_local_nagios_latency.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.667	2014-07-14 17:04:06.667
15	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_nagios_latency.rrd":min:AVERAGE   DEF:b="/usr/local/groundwork/rrd/localhost_local_nagios_latency.rrd":max:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_nagios_latency.rrd":avg:AVERAGE   CDEF:cdefa=a  CDEF:cdefb=b    CDEF:cdefc=c   AREA:cdefb#66FFFF:"Maximum Latency\\: "  GPRINT:cdefb:LAST:"%.2lf sec"  GPRINT:cdefb:MIN:min=%.2lf   GPRINT:cdefb:AVERAGE:avg=%.2lf     GPRINT:cdefb:MAX:max="%.2lf\\l"   LINE:cdefb#999999  AREA:cdefc#006699:"Average Latency\\: "   GPRINT:c:LAST:"%.2lf sec"  GPRINT:cdefc:MIN:min=%.2lf   GPRINT:cdefc:AVERAGE:avg=%.2lf     GPRINT:cdefc:MAX:max="%.2lf\\l"    LINE:cdefc#999999  AREA:a#333366:"Minimum Latency\\: "   GPRINT:a:LAST:"%.2lf sec"  GPRINT:cdefa:MIN:min=%.2lf   GPRINT:cdefa:AVERAGE:avg=%.2lf     GPRINT:cdefa:MAX:max="%.2lf\\l"   LINE:cdefa#999999   -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.667	2014-07-14 17:04:06.667
16	42	rrdtool graph - DEF:a="/usr/local/groundwork/rrd/localhost_local_process_gw_listener.rrd":number:AVERAGE CDEF:cdefa=a AREA:cdefa#0000FF:"Number of Processes" GPRINT:cdefa:MIN:min=%.2lf GPRINT:cdefa:AVERAGE:avg=%.2lf GPRINT:cdefa:MAX:max=%.2lf  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.683	2014-07-14 17:04:06.683
16	40	/usr/local/groundwork/rrd/localhost_local_process_gw_listener.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.683	2014-07-14 17:04:06.683
16	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.683	2014-07-14 17:04:06.683
16	41	Process Count	\N	\N	\N	\N	\N	2014-07-14 17:04:06.683	2014-07-14 17:04:06.683
17	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.708	2014-07-14 17:04:06.708
17	40	/usr/local/groundwork/rrd/localhost_local_process_nagios.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.708	2014-07-14 17:04:06.708
17	41	Process Count	\N	\N	\N	\N	\N	2014-07-14 17:04:06.708	2014-07-14 17:04:06.708
17	42	rrdtool graph - DEF:a="/usr/local/groundwork/rrd/localhost_local_process_nagios.rrd":number:AVERAGE CDEF:cdefa=a AREA:cdefa#0000FF:"Number of Processes" GPRINT:cdefa:MIN:min=%.2lf GPRINT:cdefa:AVERAGE:avg=%.2lf GPRINT:cdefa:MAX:max=%.2lf  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120 -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.708	2014-07-14 17:04:06.708
18	40	/usr/local/groundwork/rrd/localhost_local_swap.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.729	2014-07-14 17:04:06.729
18	41	Swap Utilization	\N	\N	\N	\N	\N	2014-07-14 17:04:06.729	2014-07-14 17:04:06.729
18	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_local_swap.rrd":swap:AVERAGE   DEF:w="/usr/local/groundwork/rrd/localhost_local_swap.rrd":swap_wn:AVERAGE   DEF:c="/usr/local/groundwork/rrd/localhost_local_swap.rrd":swap_cr:AVERAGE   DEF:m="/usr/local/groundwork/rrd/localhost_local_swap.rrd":swap_mx:AVERAGE   CDEF:cdefa=a,m,/,100,*   CDEF:cdefw=w  CDEF:cdefc=c  CDEF:cdefm=m   AREA:a#9900FF:"Swap Free\\: "   LINE2:a#6600FF:   GPRINT:a:LAST:"%.2lf MB\\l"   CDEF:cdefws=a,cdefw,LT,a,0,IF  AREA:cdefws#FFFF00  CDEF:cdefcs=a,cdefc,LT,a,0,IF  AREA:cdefcs#FF0033   LINE2:cdefw#FFFF00:"Warning Threshold\\:"   GPRINT:cdefw:AVERAGE:"%.2lf"   LINE2:cdefc#FF0033:"Critical Threshold\\:"   GPRINT:cdefc:AVERAGE:"%.2lf\\l"   GPRINT:cdefa:AVERAGE:"Percentage Swap Free"=%.2lf   GPRINT:cdefm:AVERAGE:"Total Swap Space=%.2lf"   -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.729	2014-07-14 17:04:06.729
18	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.729	2014-07-14 17:04:06.729
6	40	/usr/local/groundwork/rrd/localhost_local_users.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.749	2014-07-14 17:04:06.749
6	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.749	2014-07-14 17:04:06.749
6	41	Current Users	\N	\N	\N	\N	\N	2014-07-14 17:04:06.749	2014-07-14 17:04:06.749
6	42	rrdtool graph - --imgformat=PNG --slope-mode DEF:a=/usr/local/groundwork/rrd/localhost_local_users.rrd:users:AVERAGE  CDEF:cdefa=a  AREA:cdefa#0033CC:"Number of logged in users" -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF-Y --height 120	\N	\N	\N	\N	\N	2014-07-14 17:04:06.749	2014-07-14 17:04:06.749
19	41	Foundation Listener Response Time	\N	\N	\N	\N	\N	2014-07-14 17:04:06.791	2014-07-14 17:04:06.791
19	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.791	2014-07-14 17:04:06.791
21	53	time=0.000215s;5.000000;9.000000;0.000000;10.000000	\N	\N	\N	\N	\N	2014-07-14 17:04:28.209	2014-07-14 17:04:28.209
21	1	TCP OK - 0.000 second response time on port 5667	\N	\N	\N	\N	\N	2014-07-14 17:04:28.209	2014-07-14 17:04:28.209
25	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:08.9	2014-07-14 17:05:08.9
25	1	OK - 0 problem(s) of 6 members	\N	\N	\N	\N	\N	2014-07-14 17:05:08.899	2014-07-14 17:05:08.899
25	51	GroundWork Monitor Core Services	\N	\N	\N	\N	\N	2014-07-14 17:05:08.899	2014-07-14 17:05:08.899
25	44	\N	\N	\N	\N	1	\N	2014-07-14 17:05:08.899	2014-07-14 17:05:08.899
25	53	problems=0;0;0;0;6	\N	\N	\N	\N	\N	2014-07-14 17:05:08.899	2014-07-14 17:05:08.899
25	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:08.9	2014-07-14 17:05:08.9
25	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:08.9	2014-07-14 17:05:08.9
26	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.272	2014-07-14 17:05:54.272
19	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_tcp_gw_listener.rrd":time:AVERAGE  DEF:w="/usr/local/groundwork/rrd/localhost_tcp_gw_listener.rrd":time_wn:AVERAGE  DEF:c="/usr/local/groundwork/rrd/localhost_tcp_gw_listener.rrd":time_cr:AVERAGE  CDEF:cdefa=a CDEF:cdefb=a,0.99,*  CDEF:cdefw=w  CDEF:cdefc=c   AREA:a#33FFFF  AREA:cdefb#00CF00:"Response Time\\:"  GPRINT:a:LAST:"%.4lf Seconds"    GPRINT:a:MIN:min=%.2lf  GPRINT:a:AVERAGE:avg=%.2lf  GPRINT:a:MAX:max="%.2lf\\l"  LINE2:cdefw#FFFF00:"Warning Threshold\\:"  GPRINT:cdefw:LAST:"%.2lf"  LINE2:cdefc#FF0033:"Critical Threshold\\:"  GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Host\\: localhost\\l" COMMENT:"Service\\: tcp_gw_listener"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.791	2014-07-14 17:04:06.791
19	40	/usr/local/groundwork/rrd/localhost_tcp_gw_listener.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.791	2014-07-14 17:04:06.791
20	40	/usr/local/groundwork/rrd/localhost_tcp_http.rrd	\N	\N	\N	\N	\N	2014-07-14 17:04:06.813	2014-07-14 17:04:06.813
20	42	rrdtool graph -   DEF:a="/usr/local/groundwork/rrd/localhost_tcp_http.rrd":time:AVERAGE  DEF:w="/usr/local/groundwork/rrd/localhost_tcp_http.rrd":time_wn:AVERAGE  DEF:c="/usr/local/groundwork/rrd/localhost_tcp_http.rrd":time_cr:AVERAGE  CDEF:cdefa=a CDEF:cdefb=a,0.99,*  CDEF:cdefw=w  CDEF:cdefc=c   AREA:a#33FFFF  AREA:cdefb#00CF00:"Response Time\\:"  GPRINT:a:LAST:"%.4lf Seconds"    GPRINT:a:MIN:min=%.2lf  GPRINT:a:AVERAGE:avg=%.2lf  GPRINT:a:MAX:max="%.2lf\\l"  LINE2:cdefw#FFFF00:"Warning Threshold\\:"  GPRINT:cdefw:LAST:"%.2lf"  LINE2:cdefc#FF0033:"Critical Threshold\\:"  GPRINT:cdefc:LAST:"%.2lf\\l"    COMMENT:"Host\\: localhost\\l" COMMENT:"Service\\: tcp_http"  CDEF:cdefws=a,cdefw,GT,a,0,IF AREA:cdefws#FFFF00 CDEF:cdefcs=a,cdefc,GT,a,0,IF AREA:cdefcs#FF0033  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y -l 0	\N	\N	\N	\N	\N	2014-07-14 17:04:06.813	2014-07-14 17:04:06.813
20	50	 	\N	\N	\N	\N	\N	2014-07-14 17:04:06.813	2014-07-14 17:04:06.813
20	41	HTTP Response Time	\N	\N	\N	\N	\N	2014-07-14 17:04:06.813	2014-07-14 17:04:06.813
27	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
27	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.291	2014-07-14 17:05:54.291
28	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.332	2014-07-14 17:05:54.332
28	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.331	2014-07-14 17:05:54.331
28	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.331	2014-07-14 17:05:54.331
29	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
29	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
29	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
29	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
29	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
29	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.349	2014-07-14 17:05:54.349
29	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.348	2014-07-14 17:05:54.348
30	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
30	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.384	2014-07-14 17:05:54.384
31	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
31	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.408	2014-07-14 17:05:54.408
32	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
32	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.426	2014-07-14 17:05:54.426
34	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.444	2014-07-14 17:05:54.444
34	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.445	2014-07-14 17:05:54.445
34	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.444	2014-07-14 17:05:54.444
33	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
33	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.476	2014-07-14 17:05:54.476
35	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.509	2014-07-14 17:05:54.509
35	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.509	2014-07-14 17:05:54.509
35	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.509	2014-07-14 17:05:54.509
35	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.51	2014-07-14 17:05:54.51
35	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.509	2014-07-14 17:05:54.509
36	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.553	2014-07-14 17:05:54.553
36	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.552	2014-07-14 17:05:54.552
36	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.553	2014-07-14 17:05:54.553
36	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.551	2014-07-14 17:05:54.551
36	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.553	2014-07-14 17:05:54.553
36	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.553	2014-07-14 17:05:54.553
37	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
37	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.575	2014-07-14 17:05:54.575
38	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
38	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
38	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
38	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.592	2014-07-14 17:05:54.592
38	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.593	2014-07-14 17:05:54.593
39	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
39	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.612	2014-07-14 17:05:54.612
39	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.611	2014-07-14 17:05:54.611
40	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.629	2014-07-14 17:05:54.629
40	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
40	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.63	2014-07-14 17:05:54.63
41	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
41	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:54.686	2014-07-14 17:05:54.686
42	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.086	2014-07-14 17:05:55.086
42	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
42	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.08	2014-07-14 17:05:55.08
57	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
57	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.507	2014-07-14 17:05:55.507
58	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
58	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.53	2014-07-14 17:05:55.53
59	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
59	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.554	2014-07-14 17:05:55.554
62	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
43	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
43	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.118	2014-07-14 17:05:55.118
44	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.177	2014-07-14 17:05:55.177
44	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
44	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.178	2014-07-14 17:05:55.178
45	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
45	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.199	2014-07-14 17:05:55.199
46	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
46	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.221	2014-07-14 17:05:55.221
49	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.243	2014-07-14 17:05:55.243
49	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.243	2014-07-14 17:05:55.243
49	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.243	2014-07-14 17:05:55.243
49	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.243	2014-07-14 17:05:55.243
49	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.244	2014-07-14 17:05:55.244
49	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.243	2014-07-14 17:05:55.243
47	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.268	2014-07-14 17:05:55.268
47	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
47	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.267	2014-07-14 17:05:55.267
48	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
48	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.289	2014-07-14 17:05:55.289
51	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
51	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.327	2014-07-14 17:05:55.327
51	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.328	2014-07-14 17:05:55.328
52	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
52	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.356	2014-07-14 17:05:55.356
50	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
50	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.384	2014-07-14 17:05:55.384
53	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
53	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.409	2014-07-14 17:05:55.409
54	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
54	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.434	2014-07-14 17:05:55.434
55	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
55	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.458	2014-07-14 17:05:55.458
56	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
56	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.481	2014-07-14 17:05:55.481
68	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
68	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.783	2014-07-14 17:05:55.783
69	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
69	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.811	2014-07-14 17:05:55.811
70	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
70	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.847	2014-07-14 17:05:55.847
71	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
71	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.874	2014-07-14 17:05:55.874
72	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
62	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
62	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.578	2014-07-14 17:05:55.578
60	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.601	2014-07-14 17:05:55.601
60	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.601	2014-07-14 17:05:55.601
60	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.601	2014-07-14 17:05:55.601
60	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.601	2014-07-14 17:05:55.601
60	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
60	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.602	2014-07-14 17:05:55.602
61	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
61	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.625	2014-07-14 17:05:55.625
63	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
63	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.649	2014-07-14 17:05:55.649
64	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.673	2014-07-14 17:05:55.673
64	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
64	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.672	2014-07-14 17:05:55.672
65	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.697	2014-07-14 17:05:55.697
65	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.697	2014-07-14 17:05:55.697
65	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.697	2014-07-14 17:05:55.697
65	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.697	2014-07-14 17:05:55.697
65	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.698	2014-07-14 17:05:55.698
65	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.697	2014-07-14 17:05:55.697
66	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
66	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.724	2014-07-14 17:05:55.724
67	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.752	2014-07-14 17:05:55.752
67	26	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
67	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
67	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
67	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
67	19	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.752	2014-07-14 17:05:55.752
67	44	\N	\N	\N	\N	3	\N	2014-07-14 17:05:55.751	2014-07-14 17:05:55.751
67	12	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.751	2014-07-14 17:05:55.751
67	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
67	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.752	2014-07-14 17:05:55.752
67	14	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.752	2014-07-14 17:05:55.752
67	8	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.751	2014-07-14 17:05:55.751
67	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.755	2014-07-14 17:05:55.755
72	43	\N	\N	\N	\N	1	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	15	\N	\N	\N	0	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	9	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	20	\N	\N	f	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	11	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	10	\N	\N	t	\N	\N	\N	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
72	25	\N	\N	\N	\N	\N	0	2014-07-14 17:05:55.916	2014-07-14 17:05:55.916
\.


--
-- Data for Name: severity; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY severity (severityid, name, description) FROM stdin;
1	FATAL	Severity FATAL
2	HIGH	Severity HIGH
3	LOW	Severity LOW
4	WARNING	Severity WARNING
5	PERFORMANCE	Severity PERFORMANCE
6	STATISTIC	Severity STATISTIC
7	SERIOUS	Severity SERIOUS
8	CRITICAL	GroundWork Severity CRITICAL. Also MIB standard
9	OK	GroundWork Severity OK
10	UNKNOWN	GroundWork Severity UNKNOWN
11	NORMAL	Standard MIB type for Severity
12	MAJOR	Standard MIB type for MonitorStatus
13	MINOR	Standard MIB type for MonitorStatus
14	INFORMATIONAL	Standard MIB type
15	UP	Severity UP
16	DOWN	Severity DOWN
17	UNREACHABLE	Severity unreachable
18	ACKNOWLEDGEMENT (WARNING)	ACKNOWLEDGEMENT (WARNING)
19	ACKNOWLEDGEMENT (CRITICAL)	ACKNOWLEDGEMENT (CRITICAL)
20	ACKNOWLEDGEMENT (DOWN)	ACKNOWLEDGEMENT (DOWN)
21	ACKNOWLEDGEMENT (UP)	ACKNOWLEDGEMENT (UP)
22	ACKNOWLEDGEMENT (OK)	ACKNOWLEDGEMENT (OK)
23	ACKNOWLEDGEMENT (UNREACHABLE)	ACKNOWLEDGEMENT (UNREACHABLE)
24	ACKNOWLEDGEMENT (UNKNOWN)	ACKNOWLEDGEMENT (UNKNOWN)
25	ACKNOWLEDGEMENT (PENDING)	ACKNOWLEDGEMENT (PENDING)
26	ACKNOWLEDGEMENT (MAINTENANCE)	ACKNOWLEDGEMENT (MAINTENANCE)
\.


--
-- Name: severity_severityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('severity_severityid_seq', 26, true);


--
-- Data for Name: statetype; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY statetype (statetypeid, name, description) FROM stdin;
1	SOFT	State Soft
2	HARD	State Hard
3	UNKNOWN	State UNKNOWN
\.


--
-- Name: statetype_statetypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('statetype_statetypeid_seq', 3, true);


--
-- Data for Name: typerule; Type: TABLE DATA; Schema: public; Owner: collage
--

COPY typerule (typeruleid, name, description) FROM stdin;
1	NETWORK	Network
2	HARDWARE	Hardware
3	SERVICE	Service
4	APPLICATION	Application
5	FILTERED	Message filtered by GroundWork-Bridge
6	UNDEFINED	Undefined type
7	NAGIOS_LOG	NAGIOS_LOG type
8	ACKNOWLEDGE	ACKNOWLEDGE type
9	UNACKNOWLEDGE	UNACKNOWLEDGE type
\.


--
-- Name: typerule_typeruleid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

SELECT pg_catalog.setval('typerule_typeruleid_seq', 9, true);


--
-- Name: action_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_name_key UNIQUE (name);


--
-- Name: action_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_pkey PRIMARY KEY (actionid);


--
-- Name: actionparameter_actionid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_actionid_name_key UNIQUE (actionid, name);


--
-- Name: actionparameter_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_pkey PRIMARY KEY (actionparameterid);


--
-- Name: actionproperty_actionid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_actionid_name_key UNIQUE (actionid, name);


--
-- Name: actionproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_pkey PRIMARY KEY (actionpropertyid);


--
-- Name: actiontype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actiontype
    ADD CONSTRAINT actiontype_name_key UNIQUE (name);


--
-- Name: actiontype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actiontype
    ADD CONSTRAINT actiontype_pkey PRIMARY KEY (actiontypeid);


--
-- Name: applicationaction_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_pkey PRIMARY KEY (applicationtypeid, actionid);


--
-- Name: applicationentityproperty_applicationtypeid_entitytypeid_pr_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_applicationtypeid_entitytypeid_pr_key UNIQUE (applicationtypeid, entitytypeid, propertytypeid);


--
-- Name: applicationentityproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_pkey PRIMARY KEY (applicationentitypropertyid);


--
-- Name: applicationtype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationtype
    ADD CONSTRAINT applicationtype_name_key UNIQUE (name);


--
-- Name: applicationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationtype
    ADD CONSTRAINT applicationtype_pkey PRIMARY KEY (applicationtypeid);


--
-- Name: category_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_name_key UNIQUE (name);


--
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (categoryid);


--
-- Name: categoryentity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_pkey PRIMARY KEY (categoryentityid);


--
-- Name: categoryhierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_pkey PRIMARY KEY (categoryid, parentid);


--
-- Name: checktype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY checktype
    ADD CONSTRAINT checktype_name_key UNIQUE (name);


--
-- Name: checktype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY checktype
    ADD CONSTRAINT checktype_pkey PRIMARY KEY (checktypeid);


--
-- Name: component_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT component_name_key UNIQUE (name);


--
-- Name: component_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT component_pkey PRIMARY KEY (componentid);


--
-- Name: consolidationcriteria_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY consolidationcriteria
    ADD CONSTRAINT consolidationcriteria_name_key UNIQUE (name);


--
-- Name: consolidationcriteria_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY consolidationcriteria
    ADD CONSTRAINT consolidationcriteria_pkey PRIMARY KEY (consolidationcriteriaid);


--
-- Name: device_identification_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_identification_key UNIQUE (identification);


--
-- Name: device_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pkey PRIMARY KEY (deviceid);


--
-- Name: deviceparent_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_pkey PRIMARY KEY (deviceid, parentid);


--
-- Name: entity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (entityid);


--
-- Name: entityproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_pkey PRIMARY KEY (entitytypeid, objectid, propertytypeid);


--
-- Name: entitytype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entitytype
    ADD CONSTRAINT entitytype_name_key UNIQUE (name);


--
-- Name: entitytype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entitytype
    ADD CONSTRAINT entitytype_pkey PRIMARY KEY (entitytypeid);


--
-- Name: host_hostname_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_hostname_key UNIQUE (hostname);


--
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (hostid);


--
-- Name: hostgroup_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_name_key UNIQUE (name);


--
-- Name: hostgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_pkey PRIMARY KEY (hostgroupid);


--
-- Name: hostgroupcollection_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_pkey PRIMARY KEY (hostid, hostgroupid);


--
-- Name: hoststatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_pkey PRIMARY KEY (hoststatusid);


--
-- Name: hoststatusproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_pkey PRIMARY KEY (hoststatusid, propertytypeid);


--
-- Name: logmessage_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_pkey PRIMARY KEY (logmessageid);


--
-- Name: logmessageproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_pkey PRIMARY KEY (logmessageid, propertytypeid);


--
-- Name: logperformancedata_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_pkey PRIMARY KEY (logperformancedataid);


--
-- Name: messagefilter_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY messagefilter
    ADD CONSTRAINT messagefilter_name_key UNIQUE (name);


--
-- Name: messagefilter_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY messagefilter
    ADD CONSTRAINT messagefilter_pkey PRIMARY KEY (messagefilterid);


--
-- Name: monitorlist_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_pkey PRIMARY KEY (monitorserverid, deviceid);


--
-- Name: monitorserver_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorserver
    ADD CONSTRAINT monitorserver_pkey PRIMARY KEY (monitorserverid);


--
-- Name: monitorstatus_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorstatus
    ADD CONSTRAINT monitorstatus_name_key UNIQUE (name);


--
-- Name: monitorstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorstatus
    ADD CONSTRAINT monitorstatus_pkey PRIMARY KEY (monitorstatusid);


--
-- Name: network_service_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY network_service_notifications
    ADD CONSTRAINT network_service_notifications_pkey PRIMARY KEY (id);


--
-- Name: network_service_short_news_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY network_service_short_news
    ADD CONSTRAINT network_service_short_news_pkey PRIMARY KEY (id);


--
-- Name: network_service_status_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY network_service_status
    ADD CONSTRAINT network_service_status_pkey PRIMARY KEY (id);


--
-- Name: operationstatus_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY operationstatus
    ADD CONSTRAINT operationstatus_name_key UNIQUE (name);


--
-- Name: operationstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY operationstatus
    ADD CONSTRAINT operationstatus_pkey PRIMARY KEY (operationstatusid);


--
-- Name: performancedatalabel_performancename_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY performancedatalabel
    ADD CONSTRAINT performancedatalabel_performancename_key UNIQUE (performancename);


--
-- Name: performancedatalabel_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY performancedatalabel
    ADD CONSTRAINT performancedatalabel_pkey PRIMARY KEY (performancedatalabelid);


--
-- Name: plugin_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_pkey PRIMARY KEY (pluginid);


--
-- Name: plugin_platformid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_platformid_name_key UNIQUE (platformid, name);


--
-- Name: pluginplatform_name_arch_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY pluginplatform
    ADD CONSTRAINT pluginplatform_name_arch_key UNIQUE (name, arch);


--
-- Name: pluginplatform_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY pluginplatform
    ADD CONSTRAINT pluginplatform_pkey PRIMARY KEY (platformid);


--
-- Name: priority_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT priority_name_key UNIQUE (name);


--
-- Name: priority_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT priority_pkey PRIMARY KEY (priorityid);


--
-- Name: propertytype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY propertytype
    ADD CONSTRAINT propertytype_name_key UNIQUE (name);


--
-- Name: propertytype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY propertytype
    ADD CONSTRAINT propertytype_pkey PRIMARY KEY (propertytypeid);


--
-- Name: servicestatus_hostid_servicedescription_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_hostid_servicedescription_key UNIQUE (hostid, servicedescription);


--
-- Name: servicestatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_pkey PRIMARY KEY (servicestatusid);


--
-- Name: servicestatusproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_pkey PRIMARY KEY (servicestatusid, propertytypeid);


--
-- Name: severity_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY severity
    ADD CONSTRAINT severity_name_key UNIQUE (name);


--
-- Name: severity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY severity
    ADD CONSTRAINT severity_pkey PRIMARY KEY (severityid);


--
-- Name: statetype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY statetype
    ADD CONSTRAINT statetype_name_key UNIQUE (name);


--
-- Name: statetype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY statetype
    ADD CONSTRAINT statetype_pkey PRIMARY KEY (statetypeid);


--
-- Name: typerule_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY typerule
    ADD CONSTRAINT typerule_name_key UNIQUE (name);


--
-- Name: typerule_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY typerule
    ADD CONSTRAINT typerule_pkey PRIMARY KEY (typeruleid);


--
-- Name: action_actiontypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX action_actiontypeid ON action USING btree (actiontypeid);


--
-- Name: action_idx_action_name; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX action_idx_action_name ON action USING btree (name);


--
-- Name: applicationaction_actionid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationaction_actionid ON applicationaction USING btree (actionid);


--
-- Name: applicationentityproperty_entitytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationentityproperty_entitytypeid ON applicationentityproperty USING btree (entitytypeid);


--
-- Name: applicationentityproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationentityproperty_propertytypeid ON applicationentityproperty USING btree (propertytypeid);


--
-- Name: category_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX category_applicationtypeid ON category USING btree (applicationtypeid);


--
-- Name: category_entitytypeid_ibfk1_1; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX category_entitytypeid_ibfk1_1 ON category USING btree (entitytypeid);


--
-- Name: categoryentity_categoryid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryentity_categoryid ON categoryentity USING btree (categoryid);


--
-- Name: categoryentity_entitytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryentity_entitytypeid ON categoryentity USING btree (entitytypeid);


--
-- Name: categoryhierarchy_parentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryhierarchy_parentid ON categoryhierarchy USING btree (parentid);


--
-- Name: deviceparent_parentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX deviceparent_parentid ON deviceparent USING btree (parentid);


--
-- Name: entity_applicationtypeid_ibfk1_1; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX entity_applicationtypeid_ibfk1_1 ON entity USING btree (applicationtypeid);


--
-- Name: entityproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX entityproperty_propertytypeid ON entityproperty USING btree (propertytypeid);


--
-- Name: host_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX host_applicationtypeid ON host USING btree (applicationtypeid);


--
-- Name: host_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX host_deviceid ON host USING btree (deviceid);


--
-- Name: hostgroup_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hostgroup_applicationtypeid ON hostgroup USING btree (applicationtypeid);


--
-- Name: hostgroupcollection_hostgroupid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hostgroupcollection_hostgroupid ON hostgroupcollection USING btree (hostgroupid);


--
-- Name: hoststatus_checktypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_checktypeid ON hoststatus USING btree (checktypeid);


--
-- Name: hoststatus_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_monitorstatusid ON hoststatus USING btree (monitorstatusid);


--
-- Name: hoststatus_statetypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_statetypeid ON hoststatus USING btree (statetypeid);


--
-- Name: hoststatusproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatusproperty_propertytypeid ON hoststatusproperty USING btree (propertytypeid);


--
-- Name: logmessage_applicationseverityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_applicationseverityid ON logmessage USING btree (applicationseverityid);


--
-- Name: logmessage_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_applicationtypeid ON logmessage USING btree (applicationtypeid);


--
-- Name: logmessage_componentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_componentid ON logmessage USING btree (componentid);


--
-- Name: logmessage_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_deviceid ON logmessage USING btree (deviceid);


--
-- Name: logmessage_fk_logmessage_hoststatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_fk_logmessage_hoststatusid ON logmessage USING btree (hoststatusid);


--
-- Name: logmessage_fk_logmessage_servicestatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_fk_logmessage_servicestatusid ON logmessage USING btree (servicestatusid);


--
-- Name: logmessage_idx_logmessage_consolidationhash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_consolidationhash ON logmessage USING btree (consolidationhash);


--
-- Name: logmessage_idx_logmessage_firstinsertdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_firstinsertdate ON logmessage USING btree (firstinsertdate);


--
-- Name: logmessage_idx_logmessage_lastinsertdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_lastinsertdate ON logmessage USING btree (lastinsertdate);


--
-- Name: logmessage_idx_logmessage_reportdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_reportdate ON logmessage USING btree (reportdate);


--
-- Name: logmessage_idx_logmessage_statelesshash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_statelesshash ON logmessage USING btree (statelesshash);


--
-- Name: logmessage_idx_logmessage_statetransitionhash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_statetransitionhash ON logmessage USING btree (statetransitionhash);


--
-- Name: logmessage_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_monitorstatusid ON logmessage USING btree (monitorstatusid);


--
-- Name: logmessage_operationstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_operationstatusid ON logmessage USING btree (operationstatusid);


--
-- Name: logmessage_priorityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_priorityid ON logmessage USING btree (priorityid);


--
-- Name: logmessage_severityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_severityid ON logmessage USING btree (severityid);


--
-- Name: logmessage_typeruleid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_typeruleid ON logmessage USING btree (typeruleid);


--
-- Name: logmessageproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessageproperty_propertytypeid ON logmessageproperty USING btree (propertytypeid);


--
-- Name: logperformancedata_performancedatalabelid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logperformancedata_performancedatalabelid ON logperformancedata USING btree (performancedatalabelid);


--
-- Name: logperformancedata_servicestatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logperformancedata_servicestatusid ON logperformancedata USING btree (servicestatusid);


--
-- Name: monitorlist_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX monitorlist_deviceid ON monitorlist USING btree (deviceid);


--
-- Name: servicestatus_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_applicationtypeid ON servicestatus USING btree (applicationtypeid);


--
-- Name: servicestatus_checktypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_checktypeid ON servicestatus USING btree (checktypeid);


--
-- Name: servicestatus_lasthardstateid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_lasthardstateid ON servicestatus USING btree (lasthardstateid);


--
-- Name: servicestatus_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_monitorstatusid ON servicestatus USING btree (monitorstatusid);


--
-- Name: servicestatus_statetypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_statetypeid ON servicestatus USING btree (statetypeid);


--
-- Name: servicestatusproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatusproperty_propertytypeid ON servicestatusproperty USING btree (propertytypeid);


--
-- Name: action_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_ibfk_1 FOREIGN KEY (actiontypeid) REFERENCES actiontype(actiontypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: actionparameter_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_ibfk_1 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: actionproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_ibfk_1 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: applicationaction_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: applicationaction_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_ibfk_2 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: applicationentityproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: applicationentityproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_2 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: applicationentityproperty_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_3 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: applicationtypeid_ibfk1_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT applicationtypeid_ibfk1_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: category_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_ibfk_2 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: categoryentity_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_ibfk_1 FOREIGN KEY (categoryid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: categoryentity_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_ibfk_2 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: categoryhierarchy_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_ibfk_1 FOREIGN KEY (parentid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: categoryhierarchy_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_ibfk_2 FOREIGN KEY (categoryid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: deviceparent_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_ibfk_1 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: deviceparent_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_ibfk_2 FOREIGN KEY (parentid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: entityproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_ibfk_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: entityproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: entitytypeid_ibfk1_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY category
    ADD CONSTRAINT entitytypeid_ibfk1_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: fk_logmessage_hoststatusid; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT fk_logmessage_hoststatusid FOREIGN KEY (hoststatusid) REFERENCES hoststatus(hoststatusid) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: fk_logmessage_servicestatusid; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT fk_logmessage_servicestatusid FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: host_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_ibfk_1 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: host_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_ibfk_2 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hostgroup_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hostgroupcollection_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_ibfk_1 FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hostgroupcollection_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_ibfk_2 FOREIGN KEY (hostgroupid) REFERENCES hostgroup(hostgroupid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatus_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_1 FOREIGN KEY (hoststatusid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatus_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_2 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatus_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_3 FOREIGN KEY (checktypeid) REFERENCES checktype(checktypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatus_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_4 FOREIGN KEY (statetypeid) REFERENCES statetype(statetypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatusproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_ibfk_1 FOREIGN KEY (hoststatusid) REFERENCES hoststatus(hoststatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: hoststatusproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: logmessage_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_2 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_3 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_4 FOREIGN KEY (severityid) REFERENCES severity(severityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_5; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_5 FOREIGN KEY (applicationseverityid) REFERENCES severity(severityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_6; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_6 FOREIGN KEY (priorityid) REFERENCES priority(priorityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_7; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_7 FOREIGN KEY (typeruleid) REFERENCES typerule(typeruleid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_8; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_8 FOREIGN KEY (componentid) REFERENCES component(componentid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessage_ibfk_9; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_9 FOREIGN KEY (operationstatusid) REFERENCES operationstatus(operationstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessageproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_ibfk_1 FOREIGN KEY (logmessageid) REFERENCES logmessage(logmessageid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logmessageproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logperformancedata_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_ibfk_1 FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: logperformancedata_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_ibfk_2 FOREIGN KEY (performancedatalabelid) REFERENCES performancedatalabel(performancedatalabelid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: monitorlist_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_ibfk_1 FOREIGN KEY (monitorserverid) REFERENCES monitorserver(monitorserverid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: monitorlist_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_ibfk_2 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: plugin_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_ibfk_1 FOREIGN KEY (platformid) REFERENCES pluginplatform(platformid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatus_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: servicestatus_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_2 FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatus_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_3 FOREIGN KEY (statetypeid) REFERENCES statetype(statetypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatus_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_4 FOREIGN KEY (checktypeid) REFERENCES checktype(checktypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatus_ibfk_5; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_5 FOREIGN KEY (lasthardstateid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatus_ibfk_6; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_6 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatusproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_ibfk_1 FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: servicestatusproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO collage;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: network_service_notifications; Type: ACL; Schema: public; Owner: collage
--

REVOKE ALL ON TABLE network_service_notifications FROM PUBLIC;
REVOKE ALL ON TABLE network_service_notifications FROM collage;
GRANT ALL ON TABLE network_service_notifications TO collage;


--
-- Name: network_service_short_news; Type: ACL; Schema: public; Owner: collage
--

REVOKE ALL ON TABLE network_service_short_news FROM PUBLIC;
REVOKE ALL ON TABLE network_service_short_news FROM collage;
GRANT ALL ON TABLE network_service_short_news TO collage;


--
-- Name: network_service_status; Type: ACL; Schema: public; Owner: collage
--

REVOKE ALL ON TABLE network_service_status FROM PUBLIC;
REVOKE ALL ON TABLE network_service_status FROM collage;
GRANT ALL ON TABLE network_service_status TO collage;


--
-- PostgreSQL database dump complete
--

