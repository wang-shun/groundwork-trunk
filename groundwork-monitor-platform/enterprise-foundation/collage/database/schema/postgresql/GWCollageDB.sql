--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.0
-- Dumped by pg_dump version 9.1.0
-- Started on 2011-09-29 15:23:12

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--
-- TODO: enable UUID generate extension once available.
-- 
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--
-- TOC entry 240 (class 3079 OID 11638)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 164 (class 1259 OID 24592)
-- Dependencies: 5
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
-- TOC entry 163 (class 1259 OID 24590)
-- Dependencies: 164 5
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
-- TOC entry 2432 (class 0 OID 0)
-- Dependencies: 163
-- Name: action_actionid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE action_actionid_seq OWNED BY action.actionid;


--
-- TOC entry 2433 (class 0 OID 0)
-- Dependencies: 163
-- Name: action_actionid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('action_actionid_seq', 1,true);


--
-- TOC entry 168 (class 1259 OID 24625)
-- Dependencies: 5
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
-- TOC entry 167 (class 1259 OID 24623)
-- Dependencies: 168 5
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
-- TOC entry 2434 (class 0 OID 0)
-- Dependencies: 167
-- Name: actionparameter_actionparameterid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actionparameter_actionparameterid_seq OWNED BY actionparameter.actionparameterid;


--
-- TOC entry 2435 (class 0 OID 0)
-- Dependencies: 167
-- Name: actionparameter_actionparameterid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('actionparameter_actionparameterid_seq', 1,true);


--
-- TOC entry 170 (class 1259 OID 24643)
-- Dependencies: 5
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
-- TOC entry 169 (class 1259 OID 24641)
-- Dependencies: 170 5
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
-- TOC entry 2436 (class 0 OID 0)
-- Dependencies: 169
-- Name: actionproperty_actionpropertyid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actionproperty_actionpropertyid_seq OWNED BY actionproperty.actionpropertyid;


--
-- TOC entry 2437 (class 0 OID 0)
-- Dependencies: 169
-- Name: actionproperty_actionpropertyid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('actionproperty_actionpropertyid_seq', 1,true);


--
-- TOC entry 162 (class 1259 OID 24579)
-- Dependencies: 5
-- Name: actiontype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE actiontype (
    actiontypeid integer NOT NULL,
    name character varying(256) NOT NULL,
    classname character varying(256) NOT NULL
);


ALTER TABLE public.actiontype OWNER TO collage;

--
-- TOC entry 161 (class 1259 OID 24577)
-- Dependencies: 5 162
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
-- TOC entry 2438 (class 0 OID 0)
-- Dependencies: 161
-- Name: actiontype_actiontypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE actiontype_actiontypeid_seq OWNED BY actiontype.actiontypeid;


--
-- TOC entry 2439 (class 0 OID 0)
-- Dependencies: 161
-- Name: actiontype_actiontypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('actiontype_actiontypeid_seq', 1,true);


--
-- TOC entry 228 (class 1259 OID 25197)
-- Dependencies: 5
-- Name: applicationaction; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationaction (
    applicationtypeid integer NOT NULL,
    actionid integer NOT NULL
);


ALTER TABLE public.applicationaction OWNER TO collage;

--
-- TOC entry 194 (class 1259 OID 24866)
-- Dependencies: 2102 5
-- Name: applicationentityproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationentityproperty (
    applicationentitypropertyid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    entitytypeid integer NOT NULL,
    propertytypeid integer NOT NULL,
    sortorder integer DEFAULT 999::numeric NOT NULL
);


ALTER TABLE public.applicationentityproperty OWNER TO collage;

--
-- TOC entry 193 (class 1259 OID 24864)
-- Dependencies: 5 194
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
-- TOC entry 2440 (class 0 OID 0)
-- Dependencies: 193
-- Name: applicationentityproperty_applicationentitypropertyid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE applicationentityproperty_applicationentitypropertyid_seq OWNED BY applicationentityproperty.applicationentitypropertyid;


--
-- TOC entry 2441 (class 0 OID 0)
-- Dependencies: 193
-- Name: applicationentityproperty_applicationentitypropertyid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('applicationentityproperty_applicationentitypropertyid_seq', 1,true);


--
-- TOC entry 166 (class 1259 OID 24612)
-- Dependencies: 5
-- Name: applicationtype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE applicationtype (
    applicationtypeid integer NOT NULL,
    name character varying(128) NOT NULL,
    displayname character varying(128),
    description character varying(254),
    statetransitioncriteria character varying(512)
);


ALTER TABLE public.applicationtype OWNER TO collage;

--
-- TOC entry 165 (class 1259 OID 24610)
-- Dependencies: 5 166
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
-- TOC entry 2442 (class 0 OID 0)
-- Dependencies: 165
-- Name: applicationtype_applicationtypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE applicationtype_applicationtypeid_seq OWNED BY applicationtype.applicationtypeid;


--
-- TOC entry 2443 (class 0 OID 0)
-- Dependencies: 165
-- Name: applicationtype_applicationtypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('applicationtype_applicationtypeid_seq', 1,true);


--
-- Name: auditlog; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE auditlog (
    auditlogid integer NOT NULL,
    subsystem character varying(254) NOT NULL,
    action character varying(32) NOT NULL,
    description character varying(4096) NOT NULL,
    username character varying(254) NOT NULL,
    logtimestamp timestamp with time zone NOT NULL,
    hostname character varying(254),
    servicedescription character varying(254),
    hostgroupname character varying(254),
    servicegroupname character varying(254)
);


ALTER TABLE public.auditlog OWNER TO collage;

--
-- Name: auditlog_auditlogid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE auditlog_auditlogid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.auditlog_auditlogid_seq OWNER TO collage;

--
-- Name: auditlog_auditlogid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE auditlog_auditlogid_seq OWNED BY auditlog.auditlogid;


CREATE TABLE comment (
    commentid integer NOT NULL,
    notes text NOT NULL,
    author character varying(254) NOT NULL,
    createdon timestamp with time zone NOT NULL,
    hostid integer,
    servicestatusid integer
);
CREATE INDEX comment_hostid ON comment USING btree (hostid);
CREATE INDEX comment_servicestatusid ON comment USING btree (servicestatusid);

ALTER TABLE public.comment OWNER TO collage;

CREATE SEQUENCE comment_commentid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.comment_commentid_seq OWNER TO collage;

ALTER SEQUENCE comment_commentid_seq OWNED BY comment.commentid;

--
-- TOC entry 176 (class 1259 OID 24690)
-- Dependencies: 5
-- Name: category; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE category (
    categoryid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(4096),
    entitytypeid integer NOT NULL,
    applicationtypeid integer,
    agentid character varying(128),
    root boolean DEFAULT true NOT NULL
);


ALTER TABLE public.category OWNER TO collage;

--
-- TOC entry 175 (class 1259 OID 24688)
-- Dependencies: 5 176
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
-- TOC entry 2444 (class 0 OID 0)
-- Dependencies: 175
-- Name: category_categoryid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE category_categoryid_seq OWNED BY category.categoryid;


--
-- TOC entry 2445 (class 0 OID 0)
-- Dependencies: 175
-- Name: category_categoryid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('category_categoryid_seq', 1,true);


--
-- TOC entry 179 (class 1259 OID 24727)
-- Dependencies: 2085 2086 2087 5
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
-- TOC entry 178 (class 1259 OID 24725)
-- Dependencies: 5 179
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
-- TOC entry 2446 (class 0 OID 0)
-- Dependencies: 178
-- Name: categoryentity_categoryentityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE categoryentity_categoryentityid_seq OWNED BY categoryentity.categoryentityid;


--
-- TOC entry 2447 (class 0 OID 0)
-- Dependencies: 178
-- Name: categoryentity_categoryentityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('categoryentity_categoryentityid_seq', 1,true);

--
-- Name: categoryancestry; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE categoryancestry (
    categoryid integer DEFAULT 0 NOT NULL,
    ancestorid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.categoryancestry OWNER TO collage;

--
-- TOC entry 177 (class 1259 OID 24707)
-- Dependencies: 2082 2083 5
-- Name: categoryhierarchy; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE categoryhierarchy (
    categoryid integer DEFAULT 0 NOT NULL,
    parentid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.categoryhierarchy OWNER TO collage;

--
-- TOC entry 181 (class 1259 OID 24750)
-- Dependencies: 5
-- Name: checktype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE checktype (
    checktypeid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.checktype OWNER TO collage;

--
-- TOC entry 180 (class 1259 OID 24748)
-- Dependencies: 181 5
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
-- TOC entry 2448 (class 0 OID 0)
-- Dependencies: 180
-- Name: checktype_checktypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE checktype_checktypeid_seq OWNED BY checktype.checktypeid;


--
-- TOC entry 2449 (class 0 OID 0)
-- Dependencies: 180
-- Name: checktype_checktypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('checktype_checktypeid_seq', 1,true);


--
-- TOC entry 196 (class 1259 OID 24894)
-- Dependencies: 5
-- Name: component; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE component (
    componentid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.component OWNER TO collage;

--
-- TOC entry 195 (class 1259 OID 24892)
-- Dependencies: 196 5
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
-- TOC entry 2450 (class 0 OID 0)
-- Dependencies: 195
-- Name: component_componentid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE component_componentid_seq OWNED BY component.componentid;


--
-- TOC entry 2451 (class 0 OID 0)
-- Dependencies: 195
-- Name: component_componentid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('component_componentid_seq', 1,true);


--
-- TOC entry 185 (class 1259 OID 24776)
-- Dependencies: 5
-- Name: consolidationcriteria; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE consolidationcriteria (
    consolidationcriteriaid integer NOT NULL,
    name character varying(254) NOT NULL,
    criteria character varying(512) NOT NULL
);


ALTER TABLE public.consolidationcriteria OWNER TO collage;

--
-- TOC entry 184 (class 1259 OID 24774)
-- Dependencies: 5 185
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
-- TOC entry 2452 (class 0 OID 0)
-- Dependencies: 184
-- Name: consolidationcriteria_consolidationcriteriaid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE consolidationcriteria_consolidationcriteriaid_seq OWNED BY consolidationcriteria.consolidationcriteriaid;


--
-- TOC entry 2453 (class 0 OID 0)
-- Dependencies: 184
-- Name: consolidationcriteria_consolidationcriteriaid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('consolidationcriteria_consolidationcriteriaid_seq', 1,true);


--
-- TOC entry 183 (class 1259 OID 24763)
-- Dependencies: 5
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
-- TOC entry 182 (class 1259 OID 24761)
-- Dependencies: 5 183
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
-- TOC entry 2454 (class 0 OID 0)
-- Dependencies: 182
-- Name: device_deviceid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE device_deviceid_seq OWNED BY device.deviceid;


--
-- TOC entry 2455 (class 0 OID 0)
-- Dependencies: 182
-- Name: device_deviceid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('device_deviceid_seq', 1,true);


--
-- Name: devicetemplateprofile; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE devicetemplateprofile (
    devicetemplateprofileid integer NOT NULL,
    deviceidentification character varying(128) NOT NULL,
    devicedescription character varying(254),
    cactihosttemplate character varying(254),
    monarchhostprofile character varying(254),
    changedtimestamp timestamp with time zone NOT NULL
);


ALTER TABLE public.devicetemplateprofile OWNER TO collage;

--
-- Name: devicetemplateprofile_devicetemplateprofileid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE devicetemplateprofile_devicetemplateprofileid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.devicetemplateprofile_devicetemplateprofileid_seq OWNER TO collage;

--
-- Name: devicetemplateprofile_devicetemplateprofileid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE devicetemplateprofile_devicetemplateprofileid_seq OWNED BY devicetemplateprofile.devicetemplateprofileid;

--
-- Name: devicetemplateprofile_devicetemplateprofileid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('devicetemplateprofile_devicetemplateprofileid_seq', 1, true);


--
-- TOC entry 186 (class 1259 OID 24787)
-- Dependencies: 5
-- Name: deviceparent; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE deviceparent (
    deviceid integer NOT NULL,
    parentid integer NOT NULL
);


ALTER TABLE public.deviceparent OWNER TO collage;

--
-- TOC entry 174 (class 1259 OID 24673)
-- Dependencies: 5
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
-- TOC entry 173 (class 1259 OID 24671)
-- Dependencies: 174 5
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
-- TOC entry 2456 (class 0 OID 0)
-- Dependencies: 173
-- Name: entity_entityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE entity_entityid_seq OWNED BY entity.entityid;


--
-- TOC entry 2457 (class 0 OID 0)
-- Dependencies: 173
-- Name: entity_entityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('entity_entityid_seq', 1, false);


--
-- TOC entry 226 (class 1259 OID 25171)
-- Dependencies: 2124 5
-- Name: entityproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE entityproperty (
    entitytypeid integer NOT NULL,
    objectid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(4096),
    valuedate timestamp with time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp with time zone DEFAULT now() NOT NULL,
    createdon timestamp with time zone NOT NULL
);


ALTER TABLE public.entityproperty OWNER TO collage;

--
-- TOC entry 172 (class 1259 OID 24661)
-- Dependencies: 2078 2079 5
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
-- TOC entry 171 (class 1259 OID 24659)
-- Dependencies: 5 172
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
-- TOC entry 2458 (class 0 OID 0)
-- Dependencies: 171
-- Name: entitytype_entitytypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE entitytype_entitytypeid_seq OWNED BY entitytype.entitytypeid;


--
-- TOC entry 2459 (class 0 OID 0)
-- Dependencies: 171
-- Name: entitytype_entitytypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('entitytype_entitytypeid_seq', 1,true);


--
-- TOC entry 190 (class 1259 OID 24824)
-- Dependencies: 5
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
-- TOC entry 189 (class 1259 OID 24822)
-- Dependencies: 5 190
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
-- TOC entry 2460 (class 0 OID 0)
-- Dependencies: 189
-- Name: host_hostid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE host_hostid_seq OWNED BY host.hostid;


--
-- TOC entry 2461 (class 0 OID 0)
-- Dependencies: 189
-- Name: host_hostid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('host_hostid_seq', 1,true);


--
-- Name: hostblacklist; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostblacklist (
    hostblacklistid integer NOT NULL,
    hostname character varying(254) NOT NULL
);


ALTER TABLE public.hostblacklist OWNER TO collage;


--
-- Name: hostblacklist_hostblacklistid_seq; Type: SEQUENCE; Schema: public; Owner: collage
--

CREATE SEQUENCE hostblacklist_hostblacklistid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.hostblacklist_hostblacklistid_seq OWNER TO collage;

--
-- Name: hostblacklist_hostblacklistid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE hostblacklist_hostblacklistid_seq OWNED BY hostblacklist.hostblacklistid;

--
-- Name: hostblacklist_hostblacklistid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('hostblacklist_hostblacklistid_seq', 1, true);


--
-- TOC entry 188 (class 1259 OID 24805)
-- Dependencies: 5
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
-- TOC entry 187 (class 1259 OID 24803)
-- Dependencies: 5 188
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
-- TOC entry 2462 (class 0 OID 0)
-- Dependencies: 187
-- Name: hostgroup_hostgroupid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE hostgroup_hostgroupid_seq OWNED BY hostgroup.hostgroupid;


--
-- TOC entry 2463 (class 0 OID 0)
-- Dependencies: 187
-- Name: hostgroup_hostgroupid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('hostgroup_hostgroupid_seq', 1,true);


--
-- TOC entry 229 (class 1259 OID 25213)
-- Dependencies: 5
-- Name: hostgroupcollection; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostgroupcollection (
    hostid integer NOT NULL,
    hostgroupid integer NOT NULL
);


ALTER TABLE public.hostgroupcollection OWNER TO collage;


--
-- Name: hostidentity; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostidentity (
--
--  TODO: enable UUID generate extension once available.
-- 
--  hostidentityid uuid NOT NULL DEFAULT uuid_generate_v1(),
    hostidentityid uuid NOT NULL,
    hostname character varying(254) NOT NULL,
    hostid integer NULL
);


ALTER TABLE public.hostidentity OWNER TO collage;


--
-- Name: hostname; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hostname (
    hostidentityid uuid NOT NULL,
    hostname character varying(254) NOT NULL
);

ALTER TABLE public.hostname OWNER TO collage;


--
-- TOC entry 203 (class 1259 OID 24976)
-- Dependencies: 5
-- Name: hoststatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hoststatus (
    hoststatusid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    monitorstatusid integer NOT NULL,
    lastchecktime timestamp with time zone,
    checktypeid integer,
    statetypeid integer,
    nextchecktime timestamp with time zone
);


ALTER TABLE public.hoststatus OWNER TO collage;

--
-- TOC entry 208 (class 1259 OID 25025)
-- Dependencies: 2109 5
-- Name: hoststatusproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE hoststatusproperty (
    hoststatusid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(32768),
    valuedate timestamp with time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp with time zone DEFAULT now() NOT NULL,
    createdon timestamp with time zone NOT NULL
);


ALTER TABLE public.hoststatusproperty OWNER TO collage;

--
-- TOC entry 231 (class 1259 OID 25231)
-- Dependencies: 2126 2127 2128 2129 5
-- Name: logmessage; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logmessage (
    logmessageid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    deviceid integer NOT NULL,
    hoststatusid integer,
    servicestatusid integer,
    textmessage character varying(4096) NOT NULL,
    msgcount integer DEFAULT 1::numeric NOT NULL,
    firstinsertdate timestamp with time zone NOT NULL,
    lastinsertdate timestamp with time zone NOT NULL,
    reportdate timestamp with time zone NOT NULL,
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
-- TOC entry 230 (class 1259 OID 25229)
-- Dependencies: 5 231
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
-- TOC entry 2464 (class 0 OID 0)
-- Dependencies: 230
-- Name: logmessage_logmessageid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE logmessage_logmessageid_seq OWNED BY logmessage.logmessageid;


--
-- TOC entry 2465 (class 0 OID 0)
-- Dependencies: 230
-- Name: logmessage_logmessageid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('logmessage_logmessageid_seq', 1,true);


--
-- TOC entry 233 (class 1259 OID 25336)
-- Dependencies: 2131 5
-- Name: logmessageproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logmessageproperty (
    logmessageid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(4096),
    valuedate timestamp with time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp with time zone DEFAULT now() NOT NULL,
    createdon timestamp with time zone NOT NULL
);


ALTER TABLE public.logmessageproperty OWNER TO collage;

--
-- TOC entry 219 (class 1259 OID 25110)
-- Dependencies: 2116 2117 2118 2119 5
-- Name: logperformancedata; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE logperformancedata (
    logperformancedataid integer NOT NULL,
    servicestatusid integer NOT NULL,
    lastchecktime timestamp with time zone NOT NULL,
    maximum double precision DEFAULT 0,
    minimum double precision DEFAULT 0,
    average double precision DEFAULT 0,
    measurementpoints integer DEFAULT 0,
    performancedatalabelid integer
);


ALTER TABLE public.logperformancedata OWNER TO collage;

--
-- TOC entry 218 (class 1259 OID 25108)
-- Dependencies: 219 5
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
-- TOC entry 2466 (class 0 OID 0)
-- Dependencies: 218
-- Name: logperformancedata_logperformancedataid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE logperformancedata_logperformancedataid_seq OWNED BY logperformancedata.logperformancedataid;


--
-- TOC entry 2467 (class 0 OID 0)
-- Dependencies: 218
-- Name: logperformancedata_logperformancedataid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('logperformancedata_logperformancedataid_seq', 1,true);


--
-- TOC entry 210 (class 1259 OID 25047)
-- Dependencies: 2111 5
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
-- TOC entry 209 (class 1259 OID 25045)
-- Dependencies: 210 5
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
-- TOC entry 2468 (class 0 OID 0)
-- Dependencies: 209
-- Name: messagefilter_messagefilterid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE messagefilter_messagefilterid_seq OWNED BY messagefilter.messagefilterid;


--
-- TOC entry 2469 (class 0 OID 0)
-- Dependencies: 209
-- Name: messagefilter_messagefilterid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('messagefilter_messagefilterid_seq', 1, false);


--
-- TOC entry 213 (class 1259 OID 25072)
-- Dependencies: 5
-- Name: monitorlist; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE monitorlist (
    monitorserverid integer NOT NULL,
    deviceid integer NOT NULL
);


ALTER TABLE public.monitorlist OWNER TO collage;

--
-- TOC entry 207 (class 1259 OID 25016)
-- Dependencies: 5
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
-- TOC entry 206 (class 1259 OID 25014)
-- Dependencies: 5 207
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
-- TOC entry 2470 (class 0 OID 0)
-- Dependencies: 206
-- Name: monitorserver_monitorserverid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE monitorserver_monitorserverid_seq OWNED BY monitorserver.monitorserverid;


--
-- TOC entry 2471 (class 0 OID 0)
-- Dependencies: 206
-- Name: monitorserver_monitorserverid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('monitorserver_monitorserverid_seq', 1,true);


--
-- TOC entry 198 (class 1259 OID 24904)
-- Dependencies: 5
-- Name: monitorstatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE monitorstatus (
    monitorstatusid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.monitorstatus OWNER TO collage;

--
-- TOC entry 197 (class 1259 OID 24902)
-- Dependencies: 5 198
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
-- TOC entry 2472 (class 0 OID 0)
-- Dependencies: 197
-- Name: monitorstatus_monitorstatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE monitorstatus_monitorstatusid_seq OWNED BY monitorstatus.monitorstatusid;


--
-- TOC entry 2473 (class 0 OID 0)
-- Dependencies: 197
-- Name: monitorstatus_monitorstatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('monitorstatus_monitorstatusid_seq', 1,true);


--
-- TOC entry 205 (class 1259 OID 25006)
-- Dependencies: 5
-- Name: operationstatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE operationstatus (
    operationstatusid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.operationstatus OWNER TO collage;

--
-- TOC entry 204 (class 1259 OID 25004)
-- Dependencies: 205 5
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
-- TOC entry 2480 (class 0 OID 0)
-- Dependencies: 204
-- Name: operationstatus_operationstatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE operationstatus_operationstatusid_seq OWNED BY operationstatus.operationstatusid;


--
-- TOC entry 2481 (class 0 OID 0)
-- Dependencies: 204
-- Name: operationstatus_operationstatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('operationstatus_operationstatusid_seq', 1,true);


--
-- TOC entry 212 (class 1259 OID 25061)
-- Dependencies: 5
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
-- TOC entry 211 (class 1259 OID 25059)
-- Dependencies: 212 5
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
-- TOC entry 2482 (class 0 OID 0)
-- Dependencies: 211
-- Name: performancedatalabel_performancedatalabelid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE performancedatalabel_performancedatalabelid_seq OWNED BY performancedatalabel.performancedatalabelid;


--
-- TOC entry 2483 (class 0 OID 0)
-- Dependencies: 211
-- Name: performancedatalabel_performancedatalabelid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('performancedatalabel_performancedatalabelid_seq', 1,true);


--
-- TOC entry 223 (class 1259 OID 25144)
-- Dependencies: 2122 5
-- Name: plugin; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE plugin (
    pluginid integer NOT NULL,
    name character varying(128) NOT NULL,
    url character varying(254),
    platformid integer NOT NULL,
    dependencies character varying(254),
    lastupdatetimestamp timestamp with time zone DEFAULT now() NOT NULL,
    checksum character varying(254) NOT NULL,
    lastupdatedby character varying(254)
);


ALTER TABLE public.plugin OWNER TO collage;

--
-- TOC entry 222 (class 1259 OID 25142)
-- Dependencies: 223 5
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
-- TOC entry 2484 (class 0 OID 0)
-- Dependencies: 222
-- Name: plugin_pluginid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE plugin_pluginid_seq OWNED BY plugin.pluginid;


--
-- TOC entry 2485 (class 0 OID 0)
-- Dependencies: 222
-- Name: plugin_pluginid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('plugin_pluginid_seq', 1, false);


--
-- TOC entry 221 (class 1259 OID 25134)
-- Dependencies: 5
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
-- TOC entry 220 (class 1259 OID 25132)
-- Dependencies: 221 5
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
-- TOC entry 2486 (class 0 OID 0)
-- Dependencies: 220
-- Name: pluginplatform_platformid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE pluginplatform_platformid_seq OWNED BY pluginplatform.platformid;


--
-- TOC entry 2487 (class 0 OID 0)
-- Dependencies: 220
-- Name: pluginplatform_platformid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('pluginplatform_platformid_seq', 1,true);


--
-- TOC entry 215 (class 1259 OID 25090)
-- Dependencies: 5
-- Name: priority; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE priority (
    priorityid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.priority OWNER TO collage;

--
-- TOC entry 214 (class 1259 OID 25088)
-- Dependencies: 215 5
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
-- TOC entry 2488 (class 0 OID 0)
-- Dependencies: 214
-- Name: priority_priorityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE priority_priorityid_seq OWNED BY priority.priorityid;


--
-- TOC entry 2489 (class 0 OID 0)
-- Dependencies: 214
-- Name: priority_priorityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('priority_priorityid_seq', 1,true);


--
-- TOC entry 192 (class 1259 OID 24849)
-- Dependencies: 2094 2095 2096 2097 2098 2099 2100 5
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
-- TOC entry 191 (class 1259 OID 24847)
-- Dependencies: 192 5
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
-- TOC entry 2490 (class 0 OID 0)
-- Dependencies: 191
-- Name: propertytype_propertytypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE propertytype_propertytypeid_seq OWNED BY propertytype.propertytypeid;


--
-- TOC entry 2491 (class 0 OID 0)
-- Dependencies: 191
-- Name: propertytype_propertytypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('propertytype_propertytypeid_seq',1,true);


--
-- TOC entry 227 (class 1259 OID 25191)
-- Dependencies: 5
-- Name: schemainfo; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE schemainfo (
    name character varying(254),
    value character varying(254)
);


ALTER TABLE public.schemainfo OWNER TO collage;

--
-- TOC entry 202 (class 1259 OID 24930)
-- Dependencies: 5
-- Name: servicestatus; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE servicestatus (
    servicestatusid integer NOT NULL,
    applicationtypeid integer NOT NULL,
    servicedescription character varying(254) NOT NULL,
    hostid integer NOT NULL,
    monitorstatusid integer NOT NULL,
    lastchecktime timestamp with time zone,
    nextchecktime timestamp with time zone,
    laststatechange timestamp with time zone,
    lasthardstateid integer NOT NULL,
    statetypeid integer NOT NULL,
    checktypeid integer NOT NULL,
    metrictype character varying(254),
    domain character varying(254),
    agentid character varying(128),
    applicationhostname character varying(254)
);


ALTER TABLE public.servicestatus OWNER TO collage;

--
-- TOC entry 201 (class 1259 OID 24928)
-- Dependencies: 202 5
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
-- TOC entry 2492 (class 0 OID 0)
-- Dependencies: 201
-- Name: servicestatus_servicestatusid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE servicestatus_servicestatusid_seq OWNED BY servicestatus.servicestatusid;


--
-- TOC entry 2493 (class 0 OID 0)
-- Dependencies: 201
-- Name: servicestatus_servicestatusid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('servicestatus_servicestatusid_seq', 1,true);


--
-- TOC entry 232 (class 1259 OID 25316)
-- Dependencies: 2130 5
-- Name: servicestatusproperty; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE servicestatusproperty (
    servicestatusid integer NOT NULL,
    propertytypeid integer NOT NULL,
    valuestring character varying(16384),
    valuedate timestamp with time zone,
    valueboolean boolean,
    valueinteger integer,
    valuelong bigint,
    valuedouble double precision,
    lasteditedon timestamp with time zone DEFAULT now() NOT NULL,
    createdon timestamp with time zone NOT NULL
);


ALTER TABLE public.servicestatusproperty OWNER TO collage;

--
-- TOC entry 217 (class 1259 OID 25100)
-- Dependencies: 5
-- Name: severity; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE severity (
    severityid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.severity OWNER TO collage;

--
-- TOC entry 216 (class 1259 OID 25098)
-- Dependencies: 217 5
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
-- TOC entry 2494 (class 0 OID 0)
-- Dependencies: 216
-- Name: severity_severityid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE severity_severityid_seq OWNED BY severity.severityid;


--
-- TOC entry 2495 (class 0 OID 0)
-- Dependencies: 216
-- Name: severity_severityid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('severity_severityid_seq', 1,true);


--
-- TOC entry 200 (class 1259 OID 24917)
-- Dependencies: 5
-- Name: statetype; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE statetype (
    statetypeid integer NOT NULL,
    name character varying(254) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.statetype OWNER TO collage;

--
-- TOC entry 199 (class 1259 OID 24915)
-- Dependencies: 5 200
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
-- TOC entry 2496 (class 0 OID 0)
-- Dependencies: 199
-- Name: statetype_statetypeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE statetype_statetypeid_seq OWNED BY statetype.statetypeid;


--
-- TOC entry 2497 (class 0 OID 0)
-- Dependencies: 199
-- Name: statetype_statetypeid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('statetype_statetypeid_seq', 1,true);


--
-- TOC entry 225 (class 1259 OID 25163)
-- Dependencies: 5
-- Name: typerule; Type: TABLE; Schema: public; Owner: collage; Tablespace: 
--

CREATE TABLE typerule (
    typeruleid integer NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(254)
);


ALTER TABLE public.typerule OWNER TO collage;

--
-- TOC entry 224 (class 1259 OID 25161)
-- Dependencies: 225 5
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
-- TOC entry 2498 (class 0 OID 0)
-- Dependencies: 224
-- Name: typerule_typeruleid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: collage
--

ALTER SEQUENCE typerule_typeruleid_seq OWNED BY typerule.typeruleid;


--
-- TOC entry 2499 (class 0 OID 0)
-- Dependencies: 224
-- Name: typerule_typeruleid_seq; Type: SEQUENCE SET; Schema: public; Owner: collage
--

--SELECT pg_catalog.setval('typerule_typeruleid_seq', 1,true);


--
-- TOC entry 2073 (class 2604 OID 24595)
-- Dependencies: 163 164 164
-- Name: actionid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE action ALTER COLUMN actionid SET DEFAULT nextval('action_actionid_seq'::regclass);


--
-- TOC entry 2075 (class 2604 OID 24628)
-- Dependencies: 167 168 168
-- Name: actionparameterid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE actionparameter ALTER COLUMN actionparameterid SET DEFAULT nextval('actionparameter_actionparameterid_seq'::regclass);


--
-- TOC entry 2076 (class 2604 OID 24646)
-- Dependencies: 169 170 170
-- Name: actionpropertyid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE actionproperty ALTER COLUMN actionpropertyid SET DEFAULT nextval('actionproperty_actionpropertyid_seq'::regclass);


--
-- TOC entry 2072 (class 2604 OID 24582)
-- Dependencies: 162 161 162
-- Name: actiontypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE actiontype ALTER COLUMN actiontypeid SET DEFAULT nextval('actiontype_actiontypeid_seq'::regclass);


--
-- TOC entry 2101 (class 2604 OID 24869)
-- Dependencies: 194 193 194
-- Name: applicationentitypropertyid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE applicationentityproperty ALTER COLUMN applicationentitypropertyid SET DEFAULT nextval('applicationentityproperty_applicationentitypropertyid_seq'::regclass);


--
-- TOC entry 2074 (class 2604 OID 24615)
-- Dependencies: 166 165 166
-- Name: applicationtypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE applicationtype ALTER COLUMN applicationtypeid SET DEFAULT nextval('applicationtype_applicationtypeid_seq'::regclass);


--
-- TOC entry 2081 (class 2604 OID 24693)
-- Dependencies: 176 175 176
-- Name: categoryid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE category ALTER COLUMN categoryid SET DEFAULT nextval('category_categoryid_seq'::regclass);


--
-- TOC entry 2084 (class 2604 OID 24730)
-- Dependencies: 179 178 179
-- Name: categoryentityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE categoryentity ALTER COLUMN categoryentityid SET DEFAULT nextval('categoryentity_categoryentityid_seq'::regclass);


--
-- TOC entry 2088 (class 2604 OID 24753)
-- Dependencies: 181 180 181
-- Name: checktypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE checktype ALTER COLUMN checktypeid SET DEFAULT nextval('checktype_checktypeid_seq'::regclass);


--
-- TOC entry 2103 (class 2604 OID 24897)
-- Dependencies: 196 195 196
-- Name: componentid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE component ALTER COLUMN componentid SET DEFAULT nextval('component_componentid_seq'::regclass);


--
-- TOC entry 2090 (class 2604 OID 24779)
-- Dependencies: 185 184 185
-- Name: consolidationcriteriaid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE consolidationcriteria ALTER COLUMN consolidationcriteriaid SET DEFAULT nextval('consolidationcriteria_consolidationcriteriaid_seq'::regclass);


--
-- TOC entry 2089 (class 2604 OID 24766)
-- Dependencies: 183 182 183
-- Name: deviceid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE device ALTER COLUMN deviceid SET DEFAULT nextval('device_deviceid_seq'::regclass);


--
-- TOC entry 2080 (class 2604 OID 24676)
-- Dependencies: 174 173 174
-- Name: entityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE entity ALTER COLUMN entityid SET DEFAULT nextval('entity_entityid_seq'::regclass);


--
-- TOC entry 2077 (class 2604 OID 24664)
-- Dependencies: 171 172 172
-- Name: entitytypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE entitytype ALTER COLUMN entitytypeid SET DEFAULT nextval('entitytype_entitytypeid_seq'::regclass);


--
-- TOC entry 2092 (class 2604 OID 24827)
-- Dependencies: 190 189 190
-- Name: hostid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE host ALTER COLUMN hostid SET DEFAULT nextval('host_hostid_seq'::regclass);


--
-- TOC entry 2091 (class 2604 OID 24808)
-- Dependencies: 187 188 188
-- Name: hostgroupid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE hostgroup ALTER COLUMN hostgroupid SET DEFAULT nextval('hostgroup_hostgroupid_seq'::regclass);


--
-- TOC entry 2125 (class 2604 OID 25234)
-- Dependencies: 230 231 231
-- Name: logmessageid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE logmessage ALTER COLUMN logmessageid SET DEFAULT nextval('logmessage_logmessageid_seq'::regclass);


--
-- TOC entry 2115 (class 2604 OID 25113)
-- Dependencies: 218 219 219
-- Name: logperformancedataid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE logperformancedata ALTER COLUMN logperformancedataid SET DEFAULT nextval('logperformancedata_logperformancedataid_seq'::regclass);


--
-- TOC entry 2110 (class 2604 OID 25050)
-- Dependencies: 210 209 210
-- Name: messagefilterid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE messagefilter ALTER COLUMN messagefilterid SET DEFAULT nextval('messagefilter_messagefilterid_seq'::regclass);


--
-- TOC entry 2108 (class 2604 OID 25019)
-- Dependencies: 206 207 207
-- Name: monitorserverid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE monitorserver ALTER COLUMN monitorserverid SET DEFAULT nextval('monitorserver_monitorserverid_seq'::regclass);


--
-- TOC entry 2104 (class 2604 OID 24907)
-- Dependencies: 198 197 198
-- Name: monitorstatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE monitorstatus ALTER COLUMN monitorstatusid SET DEFAULT nextval('monitorstatus_monitorstatusid_seq'::regclass);





--
-- TOC entry 2107 (class 2604 OID 25009)
-- Dependencies: 204 205 205
-- Name: operationstatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE operationstatus ALTER COLUMN operationstatusid SET DEFAULT nextval('operationstatus_operationstatusid_seq'::regclass);


--
-- TOC entry 2112 (class 2604 OID 25064)
-- Dependencies: 212 211 212
-- Name: performancedatalabelid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE performancedatalabel ALTER COLUMN performancedatalabelid SET DEFAULT nextval('performancedatalabel_performancedatalabelid_seq'::regclass);


--
-- TOC entry 2121 (class 2604 OID 25147)
-- Dependencies: 223 222 223
-- Name: pluginid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE plugin ALTER COLUMN pluginid SET DEFAULT nextval('plugin_pluginid_seq'::regclass);


--
-- TOC entry 2120 (class 2604 OID 25137)
-- Dependencies: 220 221 221
-- Name: platformid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE pluginplatform ALTER COLUMN platformid SET DEFAULT nextval('pluginplatform_platformid_seq'::regclass);


--
-- TOC entry 2113 (class 2604 OID 25093)
-- Dependencies: 215 214 215
-- Name: priorityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE priority ALTER COLUMN priorityid SET DEFAULT nextval('priority_priorityid_seq'::regclass);


--
-- TOC entry 2093 (class 2604 OID 24852)
-- Dependencies: 192 191 192
-- Name: propertytypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE propertytype ALTER COLUMN propertytypeid SET DEFAULT nextval('propertytype_propertytypeid_seq'::regclass);


--
-- TOC entry 2106 (class 2604 OID 24933)
-- Dependencies: 202 201 202
-- Name: servicestatusid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE servicestatus ALTER COLUMN servicestatusid SET DEFAULT nextval('servicestatus_servicestatusid_seq'::regclass);


--
-- TOC entry 2114 (class 2604 OID 25103)
-- Dependencies: 216 217 217
-- Name: severityid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE severity ALTER COLUMN severityid SET DEFAULT nextval('severity_severityid_seq'::regclass);


--
-- TOC entry 2105 (class 2604 OID 24920)
-- Dependencies: 199 200 200
-- Name: statetypeid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE statetype ALTER COLUMN statetypeid SET DEFAULT nextval('statetype_statetypeid_seq'::regclass);


--
-- TOC entry 2123 (class 2604 OID 25166)
-- Dependencies: 225 224 225
-- Name: typeruleid; Type: DEFAULT; Schema: public; Owner: collage
--

ALTER TABLE typerule ALTER COLUMN typeruleid SET DEFAULT nextval('typerule_typeruleid_seq'::regclass);



--
-- TOC entry 2146 (class 2606 OID 24602)
-- Dependencies: 164 164
-- Name: action_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_name_key UNIQUE (name);


--
-- TOC entry 2148 (class 2606 OID 24600)
-- Dependencies: 164 164
-- Name: action_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_pkey PRIMARY KEY (actionid);


--
-- TOC entry 2154 (class 2606 OID 24635)
-- Dependencies: 168 168 168
-- Name: actionparameter_actionid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_actionid_name_key UNIQUE (actionid, name);


--
-- TOC entry 2156 (class 2606 OID 24633)
-- Dependencies: 168 168
-- Name: actionparameter_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_pkey PRIMARY KEY (actionparameterid);


--
-- TOC entry 2158 (class 2606 OID 24653)
-- Dependencies: 170 170 170
-- Name: actionproperty_actionid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_actionid_name_key UNIQUE (actionid, name);


--
-- TOC entry 2160 (class 2606 OID 24651)
-- Dependencies: 170 170
-- Name: actionproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_pkey PRIMARY KEY (actionpropertyid);


--
-- TOC entry 2140 (class 2606 OID 24589)
-- Dependencies: 162 162
-- Name: actiontype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actiontype
    ADD CONSTRAINT actiontype_name_key UNIQUE (name);


--
-- TOC entry 2142 (class 2606 OID 24587)
-- Dependencies: 162 162
-- Name: actiontype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY actiontype
    ADD CONSTRAINT actiontype_pkey PRIMARY KEY (actiontypeid);


--
-- TOC entry 2291 (class 2606 OID 25201)
-- Dependencies: 228 228 228
-- Name: applicationaction_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_pkey PRIMARY KEY (applicationtypeid, actionid);


--
-- TOC entry 2211 (class 2606 OID 24874)
-- Dependencies: 194 194 194 194
-- Name: applicationentityproperty_applicationtypeid_entitytypeid_pr_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_applicationtypeid_entitytypeid_pr_key UNIQUE (applicationtypeid, entitytypeid, propertytypeid);


--
-- TOC entry 2214 (class 2606 OID 24872)
-- Dependencies: 194 194
-- Name: applicationentityproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_pkey PRIMARY KEY (applicationentitypropertyid);


--
-- TOC entry 2150 (class 2606 OID 24622)
-- Dependencies: 166 166
-- Name: applicationtype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationtype
    ADD CONSTRAINT applicationtype_name_key UNIQUE (name);


--
-- TOC entry 2152 (class 2606 OID 24620)
-- Dependencies: 166 166
-- Name: applicationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY applicationtype
    ADD CONSTRAINT applicationtype_pkey PRIMARY KEY (applicationtypeid);


--
-- Name: auditlog_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY auditlog
    ADD CONSTRAINT auditlog_pkey PRIMARY KEY (auditlogid);


--
-- TOC entry 2170 (class 2606 OID 24700)
-- Dependencies: 176 176
-- Name: category_name_entitytypeid_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_name_entitytypeid_key UNIQUE (name, entitytypeid);


--
-- TOC entry 2172 (class 2606 OID 24698)
-- Dependencies: 176 176
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (categoryid);

--
-- GWMON-11714
--

ALTER TABLE ONLY category
ADD CONSTRAINT category_ibfk_2 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;

--
-- TOC entry 2179 (class 2606 OID 24735)
-- Dependencies: 179 179
-- Name: categoryentity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_pkey PRIMARY KEY (categoryentityid);


--
-- Name: categoryancestry_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY categoryancestry
    ADD CONSTRAINT categoryancestry_pkey PRIMARY KEY (categoryid, ancestorid);


--
-- TOC entry 2175 (class 2606 OID 24713)
-- Dependencies: 177 177 177
-- Name: categoryhierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_pkey PRIMARY KEY (categoryid, parentid);


--
-- TOC entry 2181 (class 2606 OID 24760)
-- Dependencies: 181 181
-- Name: checktype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY checktype
    ADD CONSTRAINT checktype_name_key UNIQUE (name);


--
-- TOC entry 2183 (class 2606 OID 24758)
-- Dependencies: 181 181
-- Name: checktype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY checktype
    ADD CONSTRAINT checktype_pkey PRIMARY KEY (checktypeid);

ALTER TABLE ONLY comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (commentid);

--
-- TOC entry 2217 (class 2606 OID 24901)
-- Dependencies: 196 196
-- Name: component_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT component_name_key UNIQUE (name);


--
-- TOC entry 2219 (class 2606 OID 24899)
-- Dependencies: 196 196
-- Name: component_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT component_pkey PRIMARY KEY (componentid);


--
-- TOC entry 2189 (class 2606 OID 24786)
-- Dependencies: 185 185
-- Name: consolidationcriteria_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY consolidationcriteria
    ADD CONSTRAINT consolidationcriteria_name_key UNIQUE (name);


--
-- TOC entry 2191 (class 2606 OID 24784)
-- Dependencies: 185 185
-- Name: consolidationcriteria_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY consolidationcriteria
    ADD CONSTRAINT consolidationcriteria_pkey PRIMARY KEY (consolidationcriteriaid);


--
-- TOC entry 2185 (class 2606 OID 24773)
-- Dependencies: 183 183
-- Name: device_identification_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_identification_key UNIQUE (identification);


--
-- TOC entry 2187 (class 2606 OID 24771)
-- Dependencies: 183 183
-- Name: device_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pkey PRIMARY KEY (deviceid);


--
-- Name: devicetemplateprofile_deviceidentification_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY devicetemplateprofile
    ADD CONSTRAINT devicetemplateprofile_deviceidentification_key UNIQUE (deviceidentification);

--
-- Name: devicetemplateprofile_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY devicetemplateprofile
    ADD CONSTRAINT devicetemplateprofile_pkey PRIMARY KEY (devicetemplateprofileid);


--
-- TOC entry 2194 (class 2606 OID 24791)
-- Dependencies: 186 186 186
-- Name: deviceparent_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_pkey PRIMARY KEY (deviceid, parentid);


--
-- TOC entry 2167 (class 2606 OID 24681)
-- Dependencies: 174 174
-- Name: entity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (entityid);


--
-- TOC entry 2287 (class 2606 OID 25179)
-- Dependencies: 226 226 226 226
-- Name: entityproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_pkey PRIMARY KEY (entitytypeid, objectid, propertytypeid);


--
-- TOC entry 2162 (class 2606 OID 24670)
-- Dependencies: 172 172
-- Name: entitytype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entitytype
    ADD CONSTRAINT entitytype_name_key UNIQUE (name);


--
-- TOC entry 2164 (class 2606 OID 24668)
-- Dependencies: 172 172
-- Name: entitytype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY entitytype
    ADD CONSTRAINT entitytype_pkey PRIMARY KEY (entitytypeid);


--
-- TOC entry 2205 (class 2606 OID 24832)
-- Dependencies: 190 190
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (hostid);


--
-- Name: hostblacklist_hostname_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostblacklist
    ADD CONSTRAINT hostblacklist_hostname_key UNIQUE (hostname);

--
-- Name: hostblacklist_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostblacklist
    ADD CONSTRAINT hostblacklist_pkey PRIMARY KEY (hostblacklistid);


--
-- TOC entry 2197 (class 2606 OID 24815)
-- Dependencies: 188 188
-- Name: hostgroup_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_name_key UNIQUE (name);


--
-- TOC entry 2199 (class 2606 OID 24813)
-- Dependencies: 188 188
-- Name: hostgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_pkey PRIMARY KEY (hostgroupid);


--
-- TOC entry 2294 (class 2606 OID 25217)
-- Dependencies: 229 229 229
-- Name: hostgroupcollection_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_pkey PRIMARY KEY (hostid, hostgroupid);


--
-- Name: hostidentity_hostname_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostidentity
    ADD CONSTRAINT hostidentity_hostname_key UNIQUE (hostname);

--
-- Name: hostidentity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hostidentity
    ADD CONSTRAINT hostidentity_pkey PRIMARY KEY (hostidentityid);

--
-- Name: hostidentity_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostidentity
    ADD CONSTRAINT hostidentity_ibfk_1 FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: hostname_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostname
    ADD CONSTRAINT hostname_ibfk_1 FOREIGN KEY (hostidentityid) REFERENCES hostidentity(hostidentityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2240 (class 2606 OID 24980)
-- Dependencies: 203 203
-- Name: hoststatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_pkey PRIMARY KEY (hoststatusid);


--
-- TOC entry 2249 (class 2606 OID 25033)
-- Dependencies: 208 208 208
-- Name: hoststatusproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_pkey PRIMARY KEY (hoststatusid, propertytypeid);


--
-- TOC entry 2310 (class 2606 OID 25243)
-- Dependencies: 231 231
-- Name: logmessage_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_pkey PRIMARY KEY (logmessageid);


--
-- TOC entry 2318 (class 2606 OID 25344)
-- Dependencies: 233 233 233
-- Name: logmessageproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_pkey PRIMARY KEY (logmessageid, propertytypeid);


--
-- TOC entry 2272 (class 2606 OID 25119)
-- Dependencies: 219 219
-- Name: logperformancedata_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_pkey PRIMARY KEY (logperformancedataid);


--
-- TOC entry 2252 (class 2606 OID 25058)
-- Dependencies: 210 210
-- Name: messagefilter_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY messagefilter
    ADD CONSTRAINT messagefilter_name_key UNIQUE (name);


--
-- TOC entry 2254 (class 2606 OID 25056)
-- Dependencies: 210 210
-- Name: messagefilter_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY messagefilter
    ADD CONSTRAINT messagefilter_pkey PRIMARY KEY (messagefilterid);


--
-- TOC entry 2261 (class 2606 OID 25076)
-- Dependencies: 213 213 213
-- Name: monitorlist_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_pkey PRIMARY KEY (monitorserverid, deviceid);


--
-- TOC entry 2247 (class 2606 OID 25024)
-- Dependencies: 207 207
-- Name: monitorserver_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorserver
    ADD CONSTRAINT monitorserver_pkey PRIMARY KEY (monitorserverid);


--
-- TOC entry 2221 (class 2606 OID 24914)
-- Dependencies: 198 198
-- Name: monitorstatus_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorstatus
    ADD CONSTRAINT monitorstatus_name_key UNIQUE (name);


--
-- TOC entry 2223 (class 2606 OID 24912)
-- Dependencies: 198 198
-- Name: monitorstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY monitorstatus
    ADD CONSTRAINT monitorstatus_pkey PRIMARY KEY (monitorstatusid);


--
-- TOC entry 2243 (class 2606 OID 25013)
-- Dependencies: 205 205
-- Name: operationstatus_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY operationstatus
    ADD CONSTRAINT operationstatus_name_key UNIQUE (name);


--
-- TOC entry 2245 (class 2606 OID 25011)
-- Dependencies: 205 205
-- Name: operationstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY operationstatus
    ADD CONSTRAINT operationstatus_pkey PRIMARY KEY (operationstatusid);


--
-- TOC entry 2256 (class 2606 OID 25071)
-- Dependencies: 212 212
-- Name: performancedatalabel_performancename_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY performancedatalabel
    ADD CONSTRAINT performancedatalabel_performancename_key UNIQUE (performancename);


--
-- TOC entry 2258 (class 2606 OID 25069)
-- Dependencies: 212 212
-- Name: performancedatalabel_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY performancedatalabel
    ADD CONSTRAINT performancedatalabel_pkey PRIMARY KEY (performancedatalabelid);


--
-- TOC entry 2279 (class 2606 OID 25153)
-- Dependencies: 223 223
-- Name: plugin_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_pkey PRIMARY KEY (pluginid);


--
-- TOC entry 2281 (class 2606 OID 25155)
-- Dependencies: 223 223 223
-- Name: plugin_platformid_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_platformid_name_key UNIQUE (platformid, name);


--
-- TOC entry 2275 (class 2606 OID 25141)
-- Dependencies: 221 221 221
-- Name: pluginplatform_name_arch_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY pluginplatform
    ADD CONSTRAINT pluginplatform_name_arch_key UNIQUE (name, arch);


--
-- TOC entry 2277 (class 2606 OID 25139)
-- Dependencies: 221 221
-- Name: pluginplatform_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY pluginplatform
    ADD CONSTRAINT pluginplatform_pkey PRIMARY KEY (platformid);


--
-- TOC entry 2263 (class 2606 OID 25097)
-- Dependencies: 215 215
-- Name: priority_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT priority_name_key UNIQUE (name);


--
-- TOC entry 2265 (class 2606 OID 25095)
-- Dependencies: 215 215
-- Name: priority_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT priority_pkey PRIMARY KEY (priorityid);


--
-- TOC entry 2207 (class 2606 OID 24863)
-- Dependencies: 192 192
-- Name: propertytype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY propertytype
    ADD CONSTRAINT propertytype_name_key UNIQUE (name);


--
-- TOC entry 2209 (class 2606 OID 24861)
-- Dependencies: 192 192
-- Name: propertytype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY propertytype
    ADD CONSTRAINT propertytype_pkey PRIMARY KEY (propertytypeid);


--
-- TOC entry 2231 (class 2606 OID 24940)
-- Dependencies: 202 202 202
-- Name: servicestatus_hostid_servicedescription_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_hostid_servicedescription_key UNIQUE (hostid, servicedescription);


--
-- TOC entry 2235 (class 2606 OID 24938)
-- Dependencies: 202 202
-- Name: servicestatus_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_pkey PRIMARY KEY (servicestatusid);


--
-- TOC entry 2315 (class 2606 OID 25324)
-- Dependencies: 232 232 232
-- Name: servicestatusproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_pkey PRIMARY KEY (servicestatusid, propertytypeid);


--
-- TOC entry 2267 (class 2606 OID 25107)
-- Dependencies: 217 217
-- Name: severity_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY severity
    ADD CONSTRAINT severity_name_key UNIQUE (name);


--
-- TOC entry 2269 (class 2606 OID 25105)
-- Dependencies: 217 217
-- Name: severity_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY severity
    ADD CONSTRAINT severity_pkey PRIMARY KEY (severityid);


--
-- TOC entry 2225 (class 2606 OID 24927)
-- Dependencies: 200 200
-- Name: statetype_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY statetype
    ADD CONSTRAINT statetype_name_key UNIQUE (name);


--
-- TOC entry 2227 (class 2606 OID 24925)
-- Dependencies: 200 200
-- Name: statetype_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY statetype
    ADD CONSTRAINT statetype_pkey PRIMARY KEY (statetypeid);


--
-- TOC entry 2283 (class 2606 OID 25170)
-- Dependencies: 225 225
-- Name: typerule_name_key; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY typerule
    ADD CONSTRAINT typerule_name_key UNIQUE (name);


--
-- TOC entry 2285 (class 2606 OID 25168)
-- Dependencies: 225 225
-- Name: typerule_pkey; Type: CONSTRAINT; Schema: public; Owner: collage; Tablespace: 
--

ALTER TABLE ONLY typerule
    ADD CONSTRAINT typerule_pkey PRIMARY KEY (typeruleid);


--
-- TOC entry 2143 (class 1259 OID 24608)
-- Dependencies: 164
-- Name: action_actiontypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX action_actiontypeid ON action USING btree (actiontypeid);


--
-- TOC entry 2144 (class 1259 OID 24609)
-- Dependencies: 164
-- Name: action_idx_action_name; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX action_idx_action_name ON action USING btree (name);


--
-- TOC entry 2289 (class 1259 OID 25212)
-- Dependencies: 228
-- Name: applicationaction_actionid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationaction_actionid ON applicationaction USING btree (actionid);


--
-- TOC entry 2212 (class 1259 OID 24890)
-- Dependencies: 194
-- Name: applicationentityproperty_entitytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationentityproperty_entitytypeid ON applicationentityproperty USING btree (entitytypeid);


--
-- TOC entry 2215 (class 1259 OID 24891)
-- Dependencies: 194
-- Name: applicationentityproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX applicationentityproperty_propertytypeid ON applicationentityproperty USING btree (propertytypeid);


--
-- TOC entry 2168 (class 1259 OID 24706)
-- Dependencies: 176
-- Name: category_entitytypeid_ibfk1_1; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX category_entitytypeid_ibfk1_1 ON category USING btree (entitytypeid);

--
-- GWMON-11714
--

CREATE INDEX category_applicationtypeid ON category USING btree (applicationtypeid);

--
-- TOC entry 2176 (class 1259 OID 24746)
-- Dependencies: 179
-- Name: categoryentity_categoryid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryentity_categoryid ON categoryentity USING btree (categoryid);


--
-- TOC entry 2177 (class 1259 OID 24747)
-- Dependencies: 179
-- Name: categoryentity_entitytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryentity_entitytypeid ON categoryentity USING btree (entitytypeid);


--
-- Name: categoryancestry_categoryid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryancestry_categoryid ON categoryancestry USING btree (categoryid);

--
-- Name: categoryancestry_ancestorid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryancestry_ancestorid ON categoryancestry USING btree (ancestorid);


--
-- Name: categoryhierarchy_categoryid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryhierarchy_categoryid ON categoryhierarchy USING btree (categoryid);


--
-- Name: categoryhierarchy_parentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX categoryhierarchy_parentid ON categoryhierarchy USING btree (parentid);


--
-- TOC entry 2192 (class 1259 OID 24802)
-- Dependencies: 186
-- Name: deviceparent_parentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX deviceparent_parentid ON deviceparent USING btree (parentid);


--
-- TOC entry 2165 (class 1259 OID 24687)
-- Dependencies: 174
-- Name: entity_applicationtypeid_ibfk1_1; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX entity_applicationtypeid_ibfk1_1 ON entity USING btree (applicationtypeid);


--
-- TOC entry 2288 (class 1259 OID 25190)
-- Dependencies: 226
-- Name: entityproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX entityproperty_propertytypeid ON entityproperty USING btree (propertytypeid);


--
-- TOC entry 2200 (class 1259 OID 24846)
-- Dependencies: 190
-- Name: host_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX host_applicationtypeid ON host USING btree (applicationtypeid);


--
-- TOC entry 2201 (class 1259 OID 24845)
-- Dependencies: 190
-- Name: host_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX host_deviceid ON host USING btree (deviceid);

--
-- Name: host_hostname; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE UNIQUE INDEX host_hostname ON host USING btree (lower(hostname));

--
-- TOC entry 2195 (class 1259 OID 24821)
-- Dependencies: 188
-- Name: hostgroup_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hostgroup_applicationtypeid ON hostgroup USING btree (applicationtypeid);


--
-- TOC entry 2292 (class 1259 OID 25228)
-- Dependencies: 229
-- Name: hostgroupcollection_hostgroupid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hostgroupcollection_hostgroupid ON hostgroupcollection USING btree (hostgroupid);


--
-- Name: hostidentity_hostid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE UNIQUE INDEX hostidentity_hostid ON hostidentity USING btree (hostid);


--
-- Name: hostname_hostname; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE UNIQUE INDEX hostname_hostname ON hostname USING btree (lower(hostname));

--
-- Name: hostname_hostidentityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hostname_hostidentityid ON hostname USING btree (hostidentityid);


--
-- TOC entry 2237 (class 1259 OID 25002)
-- Dependencies: 203
-- Name: hoststatus_checktypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_checktypeid ON hoststatus USING btree (checktypeid);


--
-- TOC entry 2238 (class 1259 OID 25001)
-- Dependencies: 203
-- Name: hoststatus_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_monitorstatusid ON hoststatus USING btree (monitorstatusid);


--
-- TOC entry 2241 (class 1259 OID 25003)
-- Dependencies: 203
-- Name: hoststatus_statetypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatus_statetypeid ON hoststatus USING btree (statetypeid);


--
-- TOC entry 2250 (class 1259 OID 25044)
-- Dependencies: 208
-- Name: hoststatusproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX hoststatusproperty_propertytypeid ON hoststatusproperty USING btree (propertytypeid);


--
-- TOC entry 2295 (class 1259 OID 25305)
-- Dependencies: 231
-- Name: logmessage_applicationseverityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_applicationseverityid ON logmessage USING btree (applicationseverityid);


--
-- TOC entry 2296 (class 1259 OID 25299)
-- Dependencies: 231
-- Name: logmessage_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_applicationtypeid ON logmessage USING btree (applicationtypeid);


--
-- TOC entry 2297 (class 1259 OID 25308)
-- Dependencies: 231
-- Name: logmessage_componentid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_componentid ON logmessage USING btree (componentid);


--
-- TOC entry 2298 (class 1259 OID 25300)
-- Dependencies: 231
-- Name: logmessage_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_deviceid ON logmessage USING btree (deviceid);


--
-- TOC entry 2299 (class 1259 OID 25302)
-- Dependencies: 231
-- Name: logmessage_fk_logmessage_hoststatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_fk_logmessage_hoststatusid ON logmessage USING btree (hoststatusid);


--
-- TOC entry 2300 (class 1259 OID 25301)
-- Dependencies: 231
-- Name: logmessage_fk_logmessage_servicestatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_fk_logmessage_servicestatusid ON logmessage USING btree (servicestatusid);


--
-- TOC entry 2301 (class 1259 OID 25310)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_consolidationhash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_consolidationhash ON logmessage USING btree (consolidationhash);


--
-- TOC entry 2302 (class 1259 OID 25312)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_firstinsertdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_firstinsertdate ON logmessage USING btree (firstinsertdate);


--
-- TOC entry 2303 (class 1259 OID 25313)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_lastinsertdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_lastinsertdate ON logmessage USING btree (lastinsertdate);


--
-- TOC entry 2304 (class 1259 OID 25314)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_reportdate; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_reportdate ON logmessage USING btree (reportdate);


--
-- TOC entry 2305 (class 1259 OID 25311)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_statelesshash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_statelesshash ON logmessage USING btree (statelesshash);


--
-- TOC entry 2306 (class 1259 OID 25315)
-- Dependencies: 231
-- Name: logmessage_idx_logmessage_statetransitionhash; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_idx_logmessage_statetransitionhash ON logmessage USING btree (statetransitionhash);


--
-- TOC entry 2307 (class 1259 OID 25303)
-- Dependencies: 231
-- Name: logmessage_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_monitorstatusid ON logmessage USING btree (monitorstatusid);


--
-- TOC entry 2308 (class 1259 OID 25309)
-- Dependencies: 231
-- Name: logmessage_operationstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_operationstatusid ON logmessage USING btree (operationstatusid);


--
-- TOC entry 2311 (class 1259 OID 25306)
-- Dependencies: 231
-- Name: logmessage_priorityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_priorityid ON logmessage USING btree (priorityid);


--
-- TOC entry 2312 (class 1259 OID 25304)
-- Dependencies: 231
-- Name: logmessage_severityid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_severityid ON logmessage USING btree (severityid);


--
-- TOC entry 2313 (class 1259 OID 25307)
-- Dependencies: 231
-- Name: logmessage_typeruleid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessage_typeruleid ON logmessage USING btree (typeruleid);


--
-- TOC entry 2319 (class 1259 OID 25355)
-- Dependencies: 233
-- Name: logmessageproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logmessageproperty_propertytypeid ON logmessageproperty USING btree (propertytypeid);


--
-- TOC entry 2270 (class 1259 OID 25131)
-- Dependencies: 219
-- Name: logperformancedata_performancedatalabelid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logperformancedata_performancedatalabelid ON logperformancedata USING btree (performancedatalabelid);


--
-- TOC entry 2273 (class 1259 OID 25130)
-- Dependencies: 219
-- Name: logperformancedata_servicestatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX logperformancedata_servicestatusid ON logperformancedata USING btree (servicestatusid);


--
-- TOC entry 2259 (class 1259 OID 25087)
-- Dependencies: 213
-- Name: monitorlist_deviceid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX monitorlist_deviceid ON monitorlist USING btree (deviceid);


--
-- Name: servicestatus_servicedescription; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_servicedescription ON servicestatus USING btree (servicedescription);


--
-- TOC entry 2228 (class 1259 OID 24971)
-- Dependencies: 202
-- Name: servicestatus_applicationtypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_applicationtypeid ON servicestatus USING btree (applicationtypeid);


--
-- TOC entry 2229 (class 1259 OID 24973)
-- Dependencies: 202
-- Name: servicestatus_checktypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_checktypeid ON servicestatus USING btree (checktypeid);


--
-- TOC entry 2232 (class 1259 OID 24974)
-- Dependencies: 202
-- Name: servicestatus_lasthardstateid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_lasthardstateid ON servicestatus USING btree (lasthardstateid);


--
-- TOC entry 2233 (class 1259 OID 24975)
-- Dependencies: 202
-- Name: servicestatus_monitorstatusid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_monitorstatusid ON servicestatus USING btree (monitorstatusid);


--
-- TOC entry 2236 (class 1259 OID 24972)
-- Dependencies: 202
-- Name: servicestatus_statetypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatus_statetypeid ON servicestatus USING btree (statetypeid);


--
-- TOC entry 2316 (class 1259 OID 25335)
-- Dependencies: 232
-- Name: servicestatusproperty_propertytypeid; Type: INDEX; Schema: public; Owner: collage; Tablespace: 
--

CREATE INDEX servicestatusproperty_propertytypeid ON servicestatusproperty USING btree (propertytypeid);


--
-- TOC entry 2326 (class 2606 OID 24603)
-- Dependencies: 2141 162 164
-- Name: action_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY action
    ADD CONSTRAINT action_ibfk_1 FOREIGN KEY (actiontypeid) REFERENCES actiontype(actiontypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2327 (class 2606 OID 24636)
-- Dependencies: 168 2147 164
-- Name: actionparameter_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionparameter
    ADD CONSTRAINT actionparameter_ibfk_1 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2328 (class 2606 OID 24654)
-- Dependencies: 164 170 2147
-- Name: actionproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY actionproperty
    ADD CONSTRAINT actionproperty_ibfk_1 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2362 (class 2606 OID 25202)
-- Dependencies: 166 2151 228
-- Name: applicationaction_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2363 (class 2606 OID 25207)
-- Dependencies: 2147 228 164
-- Name: applicationaction_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationaction
    ADD CONSTRAINT applicationaction_ibfk_2 FOREIGN KEY (actionid) REFERENCES action(actionid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2340 (class 2606 OID 24875)
-- Dependencies: 2151 194 166
-- Name: applicationentityproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 2341 (class 2606 OID 24880)
-- Dependencies: 172 2163 194
-- Name: applicationentityproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_2 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 2342 (class 2606 OID 24885)
-- Dependencies: 194 2208 192
-- Name: applicationentityproperty_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY applicationentityproperty
    ADD CONSTRAINT applicationentityproperty_ibfk_3 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 2329 (class 2606 OID 24682)
-- Dependencies: 166 2151 174
-- Name: applicationtypeid_ibfk1_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT applicationtypeid_ibfk1_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: category_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_ibfk_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2333 (class 2606 OID 24736)
-- Dependencies: 2171 179 176
-- Name: categoryentity_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_ibfk_1 FOREIGN KEY (categoryid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2334 (class 2606 OID 24741)
-- Dependencies: 179 2163 172
-- Name: categoryentity_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryentity
    ADD CONSTRAINT categoryentity_ibfk_2 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: categoryancestry_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryancestry
    ADD CONSTRAINT categoryancestry_ibfk_1 FOREIGN KEY (ancestorid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;

--
-- Name: categoryancestry_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryancestry
    ADD CONSTRAINT categoryancestry_ibfk_2 FOREIGN KEY (categoryid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2331 (class 2606 OID 24714)
-- Dependencies: 176 2171 177
-- Name: categoryhierarchy_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_ibfk_1 FOREIGN KEY (parentid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2332 (class 2606 OID 24719)
-- Dependencies: 2171 176 177
-- Name: categoryhierarchy_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY categoryhierarchy
    ADD CONSTRAINT categoryhierarchy_ibfk_2 FOREIGN KEY (categoryid) REFERENCES category(categoryid) ON UPDATE RESTRICT ON DELETE CASCADE;


ALTER TABLE ONLY comment
    ADD CONSTRAINT comment_hostid_fkey FOREIGN KEY (hostid) REFERENCES host(hostid) ON DELETE CASCADE;

ALTER TABLE ONLY comment
    ADD CONSTRAINT comment_servicestatusid_fkey FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON DELETE CASCADE;


--
-- TOC entry 2335 (class 2606 OID 24792)
-- Dependencies: 186 183 2186
-- Name: deviceparent_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_ibfk_1 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2336 (class 2606 OID 24797)
-- Dependencies: 2186 183 186
-- Name: deviceparent_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY deviceparent
    ADD CONSTRAINT deviceparent_ibfk_2 FOREIGN KEY (parentid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2360 (class 2606 OID 25180)
-- Dependencies: 2163 226 172
-- Name: entityproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_ibfk_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2361 (class 2606 OID 25185)
-- Dependencies: 226 192 2208
-- Name: entityproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY entityproperty
    ADD CONSTRAINT entityproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2330 (class 2606 OID 24701)
-- Dependencies: 2163 176 172
-- Name: entitytypeid_ibfk1_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY category
    ADD CONSTRAINT entitytypeid_ibfk1_1 FOREIGN KEY (entitytypeid) REFERENCES entitytype(entitytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;

--
-- TOC entry 2369 (class 2606 OID 25259)
-- Dependencies: 203 231 2239
-- Name: fk_logmessage_hoststatusid; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT fk_logmessage_hoststatusid FOREIGN KEY (hoststatusid) REFERENCES hoststatus(hoststatusid) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- TOC entry 2368 (class 2606 OID 25254)
-- Dependencies: 2234 231 202
-- Name: fk_logmessage_servicestatusid; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT fk_logmessage_servicestatusid FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- TOC entry 2338 (class 2606 OID 24835)
-- Dependencies: 2186 190 183
-- Name: host_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_ibfk_1 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2339 (class 2606 OID 24840)
-- Dependencies: 166 2151 190
-- Name: host_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_ibfk_2 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2337 (class 2606 OID 24816)
-- Dependencies: 188 166 2151
-- Name: hostgroup_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroup
    ADD CONSTRAINT hostgroup_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2364 (class 2606 OID 25218)
-- Dependencies: 190 229 2204
-- Name: hostgroupcollection_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_ibfk_1 FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2365 (class 2606 OID 25223)
-- Dependencies: 2198 229 188
-- Name: hostgroupcollection_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hostgroupcollection
    ADD CONSTRAINT hostgroupcollection_ibfk_2 FOREIGN KEY (hostgroupid) REFERENCES hostgroup(hostgroupid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2349 (class 2606 OID 24981)
-- Dependencies: 203 190 2204
-- Name: hoststatus_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_1 FOREIGN KEY (hoststatusid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2350 (class 2606 OID 24986)
-- Dependencies: 203 2222 198
-- Name: hoststatus_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_2 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2351 (class 2606 OID 24991)
-- Dependencies: 203 2182 181
-- Name: hoststatus_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_3 FOREIGN KEY (checktypeid) REFERENCES checktype(checktypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2352 (class 2606 OID 24996)
-- Dependencies: 200 203 2226
-- Name: hoststatus_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatus
    ADD CONSTRAINT hoststatus_ibfk_4 FOREIGN KEY (statetypeid) REFERENCES statetype(statetypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2353 (class 2606 OID 25034)
-- Dependencies: 203 208 2239
-- Name: hoststatusproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_ibfk_1 FOREIGN KEY (hoststatusid) REFERENCES hoststatus(hoststatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2354 (class 2606 OID 25039)
-- Dependencies: 2208 208 192
-- Name: hoststatusproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY hoststatusproperty
    ADD CONSTRAINT hoststatusproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2366 (class 2606 OID 25244)
-- Dependencies: 166 231 2151
-- Name: logmessage_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 2367 (class 2606 OID 25249)
-- Dependencies: 183 231 2186
-- Name: logmessage_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_2 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2370 (class 2606 OID 25264)
-- Dependencies: 231 2222 198
-- Name: logmessage_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_3 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2371 (class 2606 OID 25269)
-- Dependencies: 231 217 2268
-- Name: logmessage_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_4 FOREIGN KEY (severityid) REFERENCES severity(severityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2372 (class 2606 OID 25274)
-- Dependencies: 231 217 2268
-- Name: logmessage_ibfk_5; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_5 FOREIGN KEY (applicationseverityid) REFERENCES severity(severityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2373 (class 2606 OID 25279)
-- Dependencies: 215 2264 231
-- Name: logmessage_ibfk_6; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_6 FOREIGN KEY (priorityid) REFERENCES priority(priorityid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2374 (class 2606 OID 25284)
-- Dependencies: 231 2284 225
-- Name: logmessage_ibfk_7; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_7 FOREIGN KEY (typeruleid) REFERENCES typerule(typeruleid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2375 (class 2606 OID 25289)
-- Dependencies: 196 2218 231
-- Name: logmessage_ibfk_8; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_8 FOREIGN KEY (componentid) REFERENCES component(componentid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2376 (class 2606 OID 25294)
-- Dependencies: 205 2244 231
-- Name: logmessage_ibfk_9; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessage
    ADD CONSTRAINT logmessage_ibfk_9 FOREIGN KEY (operationstatusid) REFERENCES operationstatus(operationstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2379 (class 2606 OID 25345)
-- Dependencies: 2309 231 233
-- Name: logmessageproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_ibfk_1 FOREIGN KEY (logmessageid) REFERENCES logmessage(logmessageid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2380 (class 2606 OID 25350)
-- Dependencies: 2208 233 192
-- Name: logmessageproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logmessageproperty
    ADD CONSTRAINT logmessageproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2357 (class 2606 OID 25120)
-- Dependencies: 219 2234 202
-- Name: logperformancedata_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_ibfk_1 FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2358 (class 2606 OID 25125)
-- Dependencies: 219 2257 212
-- Name: logperformancedata_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY logperformancedata
    ADD CONSTRAINT logperformancedata_ibfk_2 FOREIGN KEY (performancedatalabelid) REFERENCES performancedatalabel(performancedatalabelid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2355 (class 2606 OID 25077)
-- Dependencies: 213 2246 207
-- Name: monitorlist_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_ibfk_1 FOREIGN KEY (monitorserverid) REFERENCES monitorserver(monitorserverid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2356 (class 2606 OID 25082)
-- Dependencies: 213 2186 183
-- Name: monitorlist_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY monitorlist
    ADD CONSTRAINT monitorlist_ibfk_2 FOREIGN KEY (deviceid) REFERENCES device(deviceid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2359 (class 2606 OID 25156)
-- Dependencies: 221 2276 223
-- Name: plugin_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY plugin
    ADD CONSTRAINT plugin_ibfk_1 FOREIGN KEY (platformid) REFERENCES pluginplatform(platformid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2343 (class 2606 OID 24941)
-- Dependencies: 2151 166 202
-- Name: servicestatus_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_1 FOREIGN KEY (applicationtypeid) REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 2344 (class 2606 OID 24946)
-- Dependencies: 202 2204 190
-- Name: servicestatus_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_2 FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2345 (class 2606 OID 24951)
-- Dependencies: 2226 200 202
-- Name: servicestatus_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_3 FOREIGN KEY (statetypeid) REFERENCES statetype(statetypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2346 (class 2606 OID 24956)
-- Dependencies: 202 181 2182
-- Name: servicestatus_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_4 FOREIGN KEY (checktypeid) REFERENCES checktype(checktypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2347 (class 2606 OID 24961)
-- Dependencies: 202 2222 198
-- Name: servicestatus_ibfk_5; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_5 FOREIGN KEY (lasthardstateid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2348 (class 2606 OID 24966)
-- Dependencies: 202 198 2222
-- Name: servicestatus_ibfk_6; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatus
    ADD CONSTRAINT servicestatus_ibfk_6 FOREIGN KEY (monitorstatusid) REFERENCES monitorstatus(monitorstatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2377 (class 2606 OID 25325)
-- Dependencies: 232 2234 202
-- Name: servicestatusproperty_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_ibfk_1 FOREIGN KEY (servicestatusid) REFERENCES servicestatus(servicestatusid) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- TOC entry 2378 (class 2606 OID 25330)
-- Dependencies: 192 2208 232
-- Name: servicestatusproperty_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: collage
--

ALTER TABLE ONLY servicestatusproperty
    ADD CONSTRAINT servicestatusproperty_ibfk_2 FOREIGN KEY (propertytypeid) REFERENCES propertytype(propertytypeid) ON UPDATE RESTRICT ON DELETE CASCADE;


    
CREATE SEQUENCE hibernate_sequence
INCREMENT 1
MINVALUE 1000
MAXVALUE 9223372036854775807
START 1000
CACHE 1; 

CREATE OR REPLACE FUNCTION pg_catalog.text(bigint) RETURNS text STRICT IMMUTABLE LANGUAGE SQL AS 'SELECT textin(int8out($1));';
CREATE CAST (bigint AS text) WITH FUNCTION pg_catalog.text(bigint) AS IMPLICIT;
COMMENT ON FUNCTION pg_catalog.text(bigint) IS 'convert bigint to text';
UPDATE pg_cast SET castcontext = 'i' WHERE oid IN (SELECT c.oid FROM pg_cast c	inner join pg_type src ON src.oid = c.castsource inner join pg_type tgt ON tgt.oid = c.casttarget WHERE src.typname LIKE 'int%' AND tgt.typname LIKE 'bool%');
--
-- TOC entry 2430 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: collage
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM collage;
GRANT ALL ON SCHEMA public TO collage;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2011-09-29 15:23:12

--
-- PostgreSQL database dump complete
--

