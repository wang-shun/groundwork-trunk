-- Copyright (C) 2010-2018 RealStuff Informatik AG
--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.9
-- Dumped by pg_dump version 9.6.9

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: audittrail; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.audittrail (
    idaudittrail bigint NOT NULL,
    old_value text,
    new_value text,
    action character varying(255) NOT NULL,
    model character varying(255) NOT NULL,
    field character varying(255),
    stamp timestamp(6) without time zone NOT NULL,
    user_id character varying(255),
    model_id character varying(255) NOT NULL
);


ALTER TABLE public.audittrail OWNER TO slareport;

--
-- Name: audittrail_idaudittrail_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.audittrail_idaudittrail_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.audittrail_idaudittrail_seq OWNER TO slareport;

--
-- Name: audittrail_idaudittrail_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.audittrail_idaudittrail_seq OWNED BY public.audittrail.idaudittrail;


--
-- Name: calendar; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.calendar (
    idcalendar bigint NOT NULL,
    description text,
    timezone character varying(50)
);


ALTER TABLE public.calendar OWNER TO slareport;

--
-- Name: calendar_has_timevacationsdays; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.calendar_has_timevacationsdays (
    calendar_idcalendar bigint NOT NULL,
    timevacationsdays_idtimevacationsdays bigint NOT NULL
);


ALTER TABLE public.calendar_has_timevacationsdays OWNER TO slareport;

--
-- Name: calendar_has_timevacationsdaysonetime; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.calendar_has_timevacationsdaysonetime (
    timevacationsdaysonetime_idtimevacationsdaysonetime bigint NOT NULL,
    calendar_idcalendar bigint NOT NULL
);


ALTER TABLE public.calendar_has_timevacationsdaysonetime OWNER TO slareport;

--
-- Name: calendar_idcalendar_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.calendar_idcalendar_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.calendar_idcalendar_seq OWNER TO slareport;

--
-- Name: calendar_idcalendar_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.calendar_idcalendar_seq OWNED BY public.calendar.idcalendar;


--
-- Name: classified; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.classified (
    idclassified bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.classified OWNER TO slareport;

--
-- Name: classified_idclassified_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.classified_idclassified_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.classified_idclassified_seq OWNER TO slareport;

--
-- Name: classified_idclassified_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.classified_idclassified_seq OWNED BY public.classified.idclassified;


--
-- Name: dashboard; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.dashboard (
    iddashboard bigint NOT NULL,
    title text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.dashboard OWNER TO slareport;

--
-- Name: dashboard_has_widget; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.dashboard_has_widget (
    fk_iddashboard bigint NOT NULL,
    fk_idwidget bigint NOT NULL,
    xpos integer,
    ypos integer,
    width integer,
    height integer,
    textoffsetx integer,
    textoffsety integer,
    titlevisibility boolean,
    zindex integer,
    datevisibility boolean,
    periodvisibility boolean,
    fk_idiconsize bigint
);


ALTER TABLE public.dashboard_has_widget OWNER TO slareport;

--
-- Name: dashboard_iddashboard_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.dashboard_iddashboard_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dashboard_iddashboard_seq OWNER TO slareport;

--
-- Name: dashboard_iddashboard_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.dashboard_iddashboard_seq OWNED BY public.dashboard.iddashboard;


--
-- Name: downtime; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.downtime (
    iddowntime bigint NOT NULL,
    starttime timestamp(6) without time zone,
    endtime timestamp(6) without time zone,
    comment text,
    summary character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    fk_idclassified bigint,
    impact character varying(255)
);


ALTER TABLE public.downtime OWNER TO slareport;

--
-- Name: downtime_iddowntime_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.downtime_iddowntime_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.downtime_iddowntime_seq OWNER TO slareport;

--
-- Name: downtime_iddowntime_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.downtime_iddowntime_seq OWNED BY public.downtime.iddowntime;


--
-- Name: downtimeactive; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.downtimeactive (
    iddowntimeactive bigint NOT NULL,
    start timestamp(6) without time zone NOT NULL,
    "end" timestamp(6) without time zone NOT NULL,
    fk_iddowntimeschedule bigint NOT NULL,
    gwstuff text NOT NULL
);


ALTER TABLE public.downtimeactive OWNER TO slareport;

--
-- Name: downtimeactive_iddowntimeactive_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.downtimeactive_iddowntimeactive_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.downtimeactive_iddowntimeactive_seq OWNER TO slareport;

--
-- Name: downtimeactive_iddowntimeactive_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.downtimeactive_iddowntimeactive_seq OWNED BY public.downtimeactive.iddowntimeactive;


--
-- Name: downtimeschedule; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.downtimeschedule (
    iddowntimeschedule bigint NOT NULL,
    fixed boolean,
    host text,
    service text,
    hostgroup text,
    servicegroup text,
    author text NOT NULL,
    description text NOT NULL,
    start timestamp(6) without time zone NOT NULL,
    "end" timestamp(6) without time zone,
    duration bigint,
    apptype text
);


ALTER TABLE public.downtimeschedule OWNER TO slareport;

--
-- Name: downtimeschedule_iddowntimeschedule_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.downtimeschedule_iddowntimeschedule_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.downtimeschedule_iddowntimeschedule_seq OWNER TO slareport;

--
-- Name: downtimeschedule_iddowntimeschedule_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.downtimeschedule_iddowntimeschedule_seq OWNED BY public.downtimeschedule.iddowntimeschedule;


--
-- Name: downtimeschedulerepeat; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.downtimeschedulerepeat (
    iddowntimeschedulerepeat bigint NOT NULL,
    year character varying(4),
    month character varying(2),
    day character varying(2),
    week character varying(1),
    weekday_0 boolean,
    weekday_1 boolean,
    weekday_2 boolean,
    weekday_3 boolean,
    weekday_4 boolean,
    weekday_5 boolean,
    weekday_6 boolean,
    count smallint,
    enddate date,
    fk_iddowntimeschedule bigint
);


ALTER TABLE public.downtimeschedulerepeat OWNER TO slareport;

--
-- Name: downtimeschedulerepeat_iddowntimeschedulerepeat_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.downtimeschedulerepeat_iddowntimeschedulerepeat_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.downtimeschedulerepeat_iddowntimeschedulerepeat_seq OWNER TO slareport;

--
-- Name: downtimeschedulerepeat_iddowntimeschedulerepeat_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.downtimeschedulerepeat_iddowntimeschedulerepeat_seq OWNED BY public.downtimeschedulerepeat.iddowntimeschedulerepeat;


--
-- Name: eventidentification; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.eventidentification (
    ideventidentification bigint NOT NULL,
    identity character varying(255) NOT NULL,
    fk_iddowntime bigint
);


ALTER TABLE public.eventidentification OWNER TO slareport;

--
-- Name: eventidentification_ideventidentification_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.eventidentification_ideventidentification_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventidentification_ideventidentification_seq OWNER TO slareport;

--
-- Name: eventidentification_ideventidentification_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.eventidentification_ideventidentification_seq OWNED BY public.eventidentification.ideventidentification;


--
-- Name: excludedate; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.excludedate (
    idexcludedate bigint NOT NULL,
    date date NOT NULL,
    fk_iddowntimeschedulerepeat bigint NOT NULL
);


ALTER TABLE public.excludedate OWNER TO slareport;

--
-- Name: excludedate_idexcludedate_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.excludedate_idexcludedate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.excludedate_idexcludedate_seq OWNER TO slareport;

--
-- Name: excludedate_idexcludedate_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.excludedate_idexcludedate_seq OWNED BY public.excludedate.idexcludedate;


--
-- Name: exporttemplate; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.exporttemplate (
    idexporttemplate bigint NOT NULL,
    fk_idexporttype bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text NOT NULL,
    content text NOT NULL
);


ALTER TABLE public.exporttemplate OWNER TO slareport;

--
-- Name: exporttemplate_idexporttemplate_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.exporttemplate_idexporttemplate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.exporttemplate_idexporttemplate_seq OWNER TO slareport;

--
-- Name: exporttemplate_idexporttemplate_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.exporttemplate_idexporttemplate_seq OWNED BY public.exporttemplate.idexporttemplate;


--
-- Name: exporttype; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.exporttype (
    idexporttype bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.exporttype OWNER TO slareport;

--
-- Name: exporttype_idexporttype_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.exporttype_idexporttype_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.exporttype_idexporttype_seq OWNER TO slareport;

--
-- Name: exporttype_idexporttype_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.exporttype_idexporttype_seq OWNED BY public.exporttype.idexporttype;


--
-- Name: group; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public."group" (
    description text NOT NULL,
    display text NOT NULL,
    note text,
    infotext text,
    infourl text,
    primarygroup boolean NOT NULL,
    idgroup bigint NOT NULL,
    critical smallint DEFAULT 0,
    warning smallint DEFAULT 0,
    percentthresholds boolean,
    idpriority bigint NOT NULL,
    hostdefinition text,
    servicedefinition text,
    hostgroup text,
    stateproperty text
);


ALTER TABLE public."group" OWNER TO slareport;

--
-- Name: group_has_group; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.group_has_group (
    group_idgroup bigint NOT NULL,
    essential boolean DEFAULT false,
    idgroup_child_group bigint NOT NULL
);


ALTER TABLE public.group_has_group OWNER TO slareport;

--
-- Name: group_has_host; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.group_has_host (
    group_idgroup bigint NOT NULL,
    essential boolean DEFAULT false,
    host_idhost bigint NOT NULL
);


ALTER TABLE public.group_has_host OWNER TO slareport;

--
-- Name: group_has_service; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.group_has_service (
    group_idgroup bigint NOT NULL,
    essential boolean DEFAULT false,
    service_idservice bigint NOT NULL
);


ALTER TABLE public.group_has_service OWNER TO slareport;

--
-- Name: group_has_servicegroup; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.group_has_servicegroup (
    group_idgroup bigint NOT NULL,
    essential boolean,
    servicegroup_idservicegroup bigint NOT NULL
);


ALTER TABLE public.group_has_servicegroup OWNER TO slareport;

--
-- Name: group_idgroup_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.group_idgroup_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.group_idgroup_seq OWNER TO slareport;

--
-- Name: group_idgroup_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.group_idgroup_seq OWNED BY public."group".idgroup;


--
-- Name: host; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.host (
    idhost bigint NOT NULL,
    hostname text NOT NULL,
    hostid bigint NOT NULL
);


ALTER TABLE public.host OWNER TO slareport;

--
-- Name: host_idhost_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.host_idhost_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.host_idhost_seq OWNER TO slareport;

--
-- Name: host_idhost_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.host_idhost_seq OWNED BY public.host.idhost;


--
-- Name: iconsize; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.iconsize (
    idiconsize bigint NOT NULL,
    name text NOT NULL,
    size integer
);


ALTER TABLE public.iconsize OWNER TO slareport;

--
-- Name: iconsize_idiconsize_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.iconsize_idiconsize_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.iconsize_idiconsize_seq OWNER TO slareport;

--
-- Name: iconsize_idiconsize_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.iconsize_idiconsize_seq OWNED BY public.iconsize.idiconsize;


--
-- Name: manualevents; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.manualevents (
    idmanualevents bigint NOT NULL,
    firstinsertdate timestamp without time zone,
    status character varying(255),
    textmessage text,
    previous bigint
);


ALTER TABLE public.manualevents OWNER TO slareport;

--
-- Name: manualevents_idmanualevents_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.manualevents_idmanualevents_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.manualevents_idmanualevents_seq OWNER TO slareport;

--
-- Name: manualevents_idmanualevents_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.manualevents_idmanualevents_seq OWNED BY public.manualevents.idmanualevents;


--
-- Name: monitoredservice; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.monitoredservice (
    idmonitoredservice bigint NOT NULL,
    servicestatusid bigint NOT NULL,
    servicedescription text,
    info text,
    active boolean
);


ALTER TABLE public.monitoredservice OWNER TO slareport;

--
-- Name: monitoredservice_has_monitoredserviceclient; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.monitoredservice_has_monitoredserviceclient (
    idmonitoredservice bigint NOT NULL,
    idmonitoredserviceclient bigint NOT NULL
);


ALTER TABLE public.monitoredservice_has_monitoredserviceclient OWNER TO slareport;

--
-- Name: monitoredservice_has_monitoredservicecli_idmonitoredservice_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.monitoredservice_has_monitoredservicecli_idmonitoredservice_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitoredservice_has_monitoredservicecli_idmonitoredservice_seq OWNER TO slareport;

--
-- Name: monitoredservice_has_monitoredservicecli_idmonitoredservice_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.monitoredservice_has_monitoredservicecli_idmonitoredservice_seq OWNED BY public.monitoredservice_has_monitoredserviceclient.idmonitoredservice;


--
-- Name: monitoredservice_idmonitoredservice_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.monitoredservice_idmonitoredservice_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitoredservice_idmonitoredservice_seq OWNER TO slareport;

--
-- Name: monitoredservice_idmonitoredservice_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.monitoredservice_idmonitoredservice_seq OWNED BY public.monitoredservice.idmonitoredservice;


--
-- Name: monitoredserviceclient; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.monitoredserviceclient (
    idmonitoredserviceclient bigint NOT NULL,
    filename character varying(256) NOT NULL,
    name text NOT NULL,
    description text NOT NULL,
    transfer_method text,
    tm_ssh_prk text,
    tm_ssh_pub text,
    tm_user text,
    tm_pass text,
    tm_path text,
    tm_host text,
    export_types character varying(255) DEFAULT 'json'::character varying
);


ALTER TABLE public.monitoredserviceclient OWNER TO slareport;

--
-- Name: monitoredserviceclient_has_exporttemplate; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.monitoredserviceclient_has_exporttemplate (
    idexporttemplate bigint NOT NULL,
    idmonitoredserviceclient bigint NOT NULL
);


ALTER TABLE public.monitoredserviceclient_has_exporttemplate OWNER TO slareport;

--
-- Name: monitoredserviceclient_idmonitoredserviceclient_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.monitoredserviceclient_idmonitoredserviceclient_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitoredserviceclient_idmonitoredserviceclient_seq OWNER TO slareport;

--
-- Name: monitoredserviceclient_idmonitoredserviceclient_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.monitoredserviceclient_idmonitoredserviceclient_seq OWNED BY public.monitoredserviceclient.idmonitoredserviceclient;


--
-- Name: monitoredservicecomment; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.monitoredservicecomment (
    idmonitoredservicecomment bigint NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    message text NOT NULL,
    fk_idmonitoredservice bigint NOT NULL,
    active boolean
);


ALTER TABLE public.monitoredservicecomment OWNER TO slareport;

--
-- Name: monitoredservicecomment_idmonitoredservicecomment_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.monitoredservicecomment_idmonitoredservicecomment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monitoredservicecomment_idmonitoredservicecomment_seq OWNER TO slareport;

--
-- Name: monitoredservicecomment_idmonitoredservicecomment_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.monitoredservicecomment_idmonitoredservicecomment_seq OWNED BY public.monitoredservicecomment.idmonitoredservicecomment;


--
-- Name: object; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.object (
    idobject bigint NOT NULL,
    servicename text,
    hostname text,
    description text,
    targetavailability numeric(4,2) DEFAULT NULL::numeric,
    idsla bigint,
    servicegroupname text,
    hostgroupname text,
    customgroupname text,
    custom01 text,
    custom02 text,
    fk_idpriority bigint,
    inclservicestatus boolean
);


ALTER TABLE public.object OWNER TO slareport;

--
-- Name: object_has_downtime; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.object_has_downtime (
    object_idobject bigint NOT NULL,
    downtime_iddowntime bigint NOT NULL
);


ALTER TABLE public.object_has_downtime OWNER TO slareport;

--
-- Name: object_has_manualevents; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.object_has_manualevents (
    fk_idobject bigint,
    fk_idmanualevents bigint
);


ALTER TABLE public.object_has_manualevents OWNER TO slareport;

--
-- Name: object_idobject_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.object_idobject_seq
    START WITH 4
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.object_idobject_seq OWNER TO slareport;

--
-- Name: object_idobject_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.object_idobject_seq OWNED BY public.object.idobject;


--
-- Name: period; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.period (
    idperiod bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.period OWNER TO slareport;

--
-- Name: period_idperiod_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.period_idperiod_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.period_idperiod_seq OWNER TO slareport;

--
-- Name: period_idperiod_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.period_idperiod_seq OWNED BY public.period.idperiod;


--
-- Name: priority; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.priority (
    idpriority bigint NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.priority OWNER TO slareport;

--
-- Name: priority_idpriority_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.priority_idpriority_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.priority_idpriority_seq OWNER TO slareport;

--
-- Name: priority_idpriority_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.priority_idpriority_seq OWNED BY public.priority.idpriority;


--
-- Name: refreshfreq; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.refreshfreq (
    idrefreshfreq bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.refreshfreq OWNER TO slareport;

--
-- Name: refreshfreq_idrefreshfreq_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.refreshfreq_idrefreshfreq_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.refreshfreq_idrefreshfreq_seq OWNER TO slareport;

--
-- Name: refreshfreq_idrefreshfreq_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.refreshfreq_idrefreshfreq_seq OWNED BY public.refreshfreq.idrefreshfreq;


--
-- Name: reprocessflag; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.reprocessflag (
    date date NOT NULL,
    fk_idobject bigint NOT NULL
);


ALTER TABLE public.reprocessflag OWNER TO slareport;

--
-- Name: schemainfo; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.schemainfo (
    name character varying(254),
    value character varying(254)
);


ALTER TABLE public.schemainfo OWNER TO slareport;

--
-- Name: service; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.service (
    servicedescription text NOT NULL,
    servicestatusid bigint NOT NULL,
    idservice bigint NOT NULL,
    idhost bigint
);


ALTER TABLE public.service OWNER TO slareport;

--
-- Name: service_idservice_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.service_idservice_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.service_idservice_seq OWNER TO slareport;

--
-- Name: service_idservice_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.service_idservice_seq OWNED BY public.service.idservice;


--
-- Name: servicegroup; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.servicegroup (
    idservicegroup bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.servicegroup OWNER TO slareport;

--
-- Name: servicegroup_idservicegroup_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.servicegroup_idservicegroup_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.servicegroup_idservicegroup_seq OWNER TO slareport;

--
-- Name: servicegroup_idservicegroup_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.servicegroup_idservicegroup_seq OWNED BY public.servicegroup.idservicegroup;


--
-- Name: sla; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.sla (
    idsla bigint NOT NULL,
    description text,
    idcalendar bigint NOT NULL,
    idtimeworkinghours bigint NOT NULL,
    rules json DEFAULT '{}'::json
);


ALTER TABLE public.sla OWNER TO slareport;

--
-- Name: sla_idsla_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.sla_idsla_seq
    START WITH 8
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sla_idsla_seq OWNER TO slareport;

--
-- Name: sla_idsla_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.sla_idsla_seq OWNED BY public.sla.idsla;


--
-- Name: sladaily; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.sladaily (
    idsladaily bigint NOT NULL,
    total_op_time integer NOT NULL,
    total_time_ok integer NOT NULL,
    total_time_wa integer NOT NULL,
    total_time_cr integer NOT NULL,
    total_time_cs integer NOT NULL,
    total_time_un integer NOT NULL,
    total_time_fu integer NOT NULL,
    total_time_no integer NOT NULL,
    date date NOT NULL,
    slainfo json NOT NULL,
    fk_idobject bigint
);


ALTER TABLE public.sladaily OWNER TO slareport;

--
-- Name: sladaily_idsladaily_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.sladaily_idsladaily_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sladaily_idsladaily_seq OWNER TO slareport;

--
-- Name: sladaily_idsladaily_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.sladaily_idsladaily_seq OWNED BY public.sladaily.idsladaily;


--
-- Name: slalogs; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.slalogs (
    idslalogs bigint NOT NULL,
    firstinsertdate timestamp without time zone,
    monitored character varying(255),
    status character varying(255),
    textmessage text,
    fk_iddowntime bigint,
    fk_idsladaily bigint,
    duration integer,
    enddate timestamp without time zone,
    fk_idslalogs bigint
);


ALTER TABLE public.slalogs OWNER TO slareport;

--
-- Name: slalogs_idslalogs_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.slalogs_idslalogs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.slalogs_idslalogs_seq OWNER TO slareport;

--
-- Name: slalogs_idslalogs_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.slalogs_idslalogs_seq OWNED BY public.slalogs.idslalogs;


--
-- Name: statusproperties; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.statusproperties (
    idstatusproperties bigint NOT NULL,
    host text,
    service text,
    hostgroup text,
    servicegroup text,
    customgroup text,
    columns json,
    sortpriority text DEFAULT 'laststatechange'::text NOT NULL
);


ALTER TABLE public.statusproperties OWNER TO slareport;

--
-- Name: statusproperties_has_status; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.statusproperties_has_status (
    fk_idstatusproperties bigint NOT NULL,
    status text NOT NULL
);


ALTER TABLE public.statusproperties_has_status OWNER TO slareport;

--
-- Name: statusproperties_has_status_fk_idstatusproperties_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.statusproperties_has_status_fk_idstatusproperties_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.statusproperties_has_status_fk_idstatusproperties_seq OWNER TO slareport;

--
-- Name: statusproperties_has_status_fk_idstatusproperties_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.statusproperties_has_status_fk_idstatusproperties_seq OWNED BY public.statusproperties_has_status.fk_idstatusproperties;


--
-- Name: statusproperties_idstatusproperties_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.statusproperties_idstatusproperties_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.statusproperties_idstatusproperties_seq OWNER TO slareport;

--
-- Name: statusproperties_idstatusproperties_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.statusproperties_idstatusproperties_seq OWNED BY public.statusproperties.idstatusproperties;


--
-- Name: tbl_migration; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.tbl_migration (
    version character varying(180) NOT NULL,
    apply_time integer
);


ALTER TABLE public.tbl_migration OWNER TO slareport;

--
-- Name: timevacationsdays; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.timevacationsdays (
    idtimevacationsdays bigint NOT NULL,
    month character varying(2) DEFAULT NULL::character varying,
    day character varying(2) DEFAULT NULL::character varying,
    description text
);


ALTER TABLE public.timevacationsdays OWNER TO slareport;

--
-- Name: TABLE timevacationsdays; Type: COMMENT; Schema: public; Owner: slareport
--

COMMENT ON TABLE public.timevacationsdays IS 'Vacations Days in every Year (like new Year)';


--
-- Name: timevacationsdays_idtimevacationsdays_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.timevacationsdays_idtimevacationsdays_seq
    START WITH 7
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.timevacationsdays_idtimevacationsdays_seq OWNER TO slareport;

--
-- Name: timevacationsdays_idtimevacationsdays_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.timevacationsdays_idtimevacationsdays_seq OWNED BY public.timevacationsdays.idtimevacationsdays;


--
-- Name: timevacationsdaysonetime; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.timevacationsdaysonetime (
    idtimevacationsdaysonetime bigint NOT NULL,
    year character varying(4) DEFAULT NULL::character varying,
    month character varying(2) DEFAULT NULL::character varying,
    day character varying(2) DEFAULT NULL::character varying,
    description text
);


ALTER TABLE public.timevacationsdaysonetime OWNER TO slareport;

--
-- Name: TABLE timevacationsdaysonetime; Type: COMMENT; Schema: public; Owner: slareport
--

COMMENT ON TABLE public.timevacationsdaysonetime IS 'Einmalige Feiertage';


--
-- Name: timevacationsdaysonetime_idtimevacationsdaysonetime_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.timevacationsdaysonetime_idtimevacationsdaysonetime_seq
    START WITH 8
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.timevacationsdaysonetime_idtimevacationsdaysonetime_seq OWNER TO slareport;

--
-- Name: timevacationsdaysonetime_idtimevacationsdaysonetime_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.timevacationsdaysonetime_idtimevacationsdaysonetime_seq OWNED BY public.timevacationsdaysonetime.idtimevacationsdaysonetime;


--
-- Name: timeworkinghours; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.timeworkinghours (
    idtimeworkinghours bigint NOT NULL,
    monday character varying(20),
    tuesday character varying(20),
    wednesday character varying(20),
    thursday character varying(20),
    friday character varying(20),
    saturday character varying(20),
    sunday character varying(20),
    description text
);


ALTER TABLE public.timeworkinghours OWNER TO slareport;

--
-- Name: timeworkinghours_idtimeworkinghours_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.timeworkinghours_idtimeworkinghours_seq
    START WITH 8
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.timeworkinghours_idtimeworkinghours_seq OWNER TO slareport;

--
-- Name: timeworkinghours_idtimeworkinghours_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.timeworkinghours_idtimeworkinghours_seq OWNED BY public.timeworkinghours.idtimeworkinghours;


--
-- Name: widget; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.widget (
    idwidget bigint NOT NULL,
    titleproperties text,
    title text NOT NULL,
    startdate date,
    enddate date,
    fk_idperiod bigint,
    fk_idobject bigint,
    fk_idwidgettype bigint,
    fk_idrefreshfreq bigint,
    fk_idstatusproperties bigint
);


ALTER TABLE public.widget OWNER TO slareport;

--
-- Name: widget_idwidget_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.widget_idwidget_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.widget_idwidget_seq OWNER TO slareport;

--
-- Name: widget_idwidget_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.widget_idwidget_seq OWNED BY public.widget.idwidget;


--
-- Name: widgetgroup; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.widgetgroup (
    idgroup bigint NOT NULL,
    fk_iddashboard bigint NOT NULL
);


ALTER TABLE public.widgetgroup OWNER TO slareport;

--
-- Name: widgetgroup_has_group; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.widgetgroup_has_group (
    fk_idgrouper bigint NOT NULL,
    fk_idgrouped bigint NOT NULL
);


ALTER TABLE public.widgetgroup_has_group OWNER TO slareport;

--
-- Name: widgetgroup_has_widget; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.widgetgroup_has_widget (
    fk_idgroup bigint NOT NULL,
    fk_idwidget bigint NOT NULL
);


ALTER TABLE public.widgetgroup_has_widget OWNER TO slareport;

--
-- Name: widgetgroup_idgroup_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.widgetgroup_idgroup_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.widgetgroup_idgroup_seq OWNER TO slareport;

--
-- Name: widgetgroup_idgroup_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.widgetgroup_idgroup_seq OWNED BY public.widgetgroup.idgroup;


--
-- Name: widgettype; Type: TABLE; Schema: public; Owner: slareport
--

CREATE TABLE public.widgettype (
    idwidgettype bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.widgettype OWNER TO slareport;

--
-- Name: widgettype_idwidgettype_seq; Type: SEQUENCE; Schema: public; Owner: slareport
--

CREATE SEQUENCE public.widgettype_idwidgettype_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.widgettype_idwidgettype_seq OWNER TO slareport;

--
-- Name: widgettype_idwidgettype_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: slareport
--

ALTER SEQUENCE public.widgettype_idwidgettype_seq OWNED BY public.widgettype.idwidgettype;


--
-- Name: audittrail idaudittrail; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.audittrail ALTER COLUMN idaudittrail SET DEFAULT nextval('public.audittrail_idaudittrail_seq'::regclass);


--
-- Name: calendar idcalendar; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar ALTER COLUMN idcalendar SET DEFAULT nextval('public.calendar_idcalendar_seq'::regclass);


--
-- Name: classified idclassified; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.classified ALTER COLUMN idclassified SET DEFAULT nextval('public.classified_idclassified_seq'::regclass);


--
-- Name: dashboard iddashboard; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard ALTER COLUMN iddashboard SET DEFAULT nextval('public.dashboard_iddashboard_seq'::regclass);


--
-- Name: downtime iddowntime; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtime ALTER COLUMN iddowntime SET DEFAULT nextval('public.downtime_iddowntime_seq'::regclass);


--
-- Name: downtimeactive iddowntimeactive; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeactive ALTER COLUMN iddowntimeactive SET DEFAULT nextval('public.downtimeactive_iddowntimeactive_seq'::regclass);


--
-- Name: downtimeschedule iddowntimeschedule; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeschedule ALTER COLUMN iddowntimeschedule SET DEFAULT nextval('public.downtimeschedule_iddowntimeschedule_seq'::regclass);


--
-- Name: downtimeschedulerepeat iddowntimeschedulerepeat; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeschedulerepeat ALTER COLUMN iddowntimeschedulerepeat SET DEFAULT nextval('public.downtimeschedulerepeat_iddowntimeschedulerepeat_seq'::regclass);


--
-- Name: eventidentification ideventidentification; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.eventidentification ALTER COLUMN ideventidentification SET DEFAULT nextval('public.eventidentification_ideventidentification_seq'::regclass);


--
-- Name: excludedate idexcludedate; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.excludedate ALTER COLUMN idexcludedate SET DEFAULT nextval('public.excludedate_idexcludedate_seq'::regclass);


--
-- Name: exporttemplate idexporttemplate; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.exporttemplate ALTER COLUMN idexporttemplate SET DEFAULT nextval('public.exporttemplate_idexporttemplate_seq'::regclass);


--
-- Name: exporttype idexporttype; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.exporttype ALTER COLUMN idexporttype SET DEFAULT nextval('public.exporttype_idexporttype_seq'::regclass);


--
-- Name: group idgroup; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public."group" ALTER COLUMN idgroup SET DEFAULT nextval('public.group_idgroup_seq'::regclass);


--
-- Name: host idhost; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.host ALTER COLUMN idhost SET DEFAULT nextval('public.host_idhost_seq'::regclass);


--
-- Name: iconsize idiconsize; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.iconsize ALTER COLUMN idiconsize SET DEFAULT nextval('public.iconsize_idiconsize_seq'::regclass);


--
-- Name: manualevents idmanualevents; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.manualevents ALTER COLUMN idmanualevents SET DEFAULT nextval('public.manualevents_idmanualevents_seq'::regclass);


--
-- Name: monitoredservice idmonitoredservice; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice ALTER COLUMN idmonitoredservice SET DEFAULT nextval('public.monitoredservice_idmonitoredservice_seq'::regclass);


--
-- Name: monitoredservice_has_monitoredserviceclient idmonitoredservice; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice_has_monitoredserviceclient ALTER COLUMN idmonitoredservice SET DEFAULT nextval('public.monitoredservice_has_monitoredservicecli_idmonitoredservice_seq'::regclass);


--
-- Name: monitoredserviceclient idmonitoredserviceclient; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredserviceclient ALTER COLUMN idmonitoredserviceclient SET DEFAULT nextval('public.monitoredserviceclient_idmonitoredserviceclient_seq'::regclass);


--
-- Name: monitoredservicecomment idmonitoredservicecomment; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservicecomment ALTER COLUMN idmonitoredservicecomment SET DEFAULT nextval('public.monitoredservicecomment_idmonitoredservicecomment_seq'::regclass);


--
-- Name: object idobject; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object ALTER COLUMN idobject SET DEFAULT nextval('public.object_idobject_seq'::regclass);


--
-- Name: period idperiod; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.period ALTER COLUMN idperiod SET DEFAULT nextval('public.period_idperiod_seq'::regclass);


--
-- Name: priority idpriority; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.priority ALTER COLUMN idpriority SET DEFAULT nextval('public.priority_idpriority_seq'::regclass);


--
-- Name: refreshfreq idrefreshfreq; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.refreshfreq ALTER COLUMN idrefreshfreq SET DEFAULT nextval('public.refreshfreq_idrefreshfreq_seq'::regclass);


--
-- Name: service idservice; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.service ALTER COLUMN idservice SET DEFAULT nextval('public.service_idservice_seq'::regclass);


--
-- Name: servicegroup idservicegroup; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.servicegroup ALTER COLUMN idservicegroup SET DEFAULT nextval('public.servicegroup_idservicegroup_seq'::regclass);


--
-- Name: sla idsla; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sla ALTER COLUMN idsla SET DEFAULT nextval('public.sla_idsla_seq'::regclass);


--
-- Name: sladaily idsladaily; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sladaily ALTER COLUMN idsladaily SET DEFAULT nextval('public.sladaily_idsladaily_seq'::regclass);


--
-- Name: slalogs idslalogs; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.slalogs ALTER COLUMN idslalogs SET DEFAULT nextval('public.slalogs_idslalogs_seq'::regclass);


--
-- Name: statusproperties idstatusproperties; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.statusproperties ALTER COLUMN idstatusproperties SET DEFAULT nextval('public.statusproperties_idstatusproperties_seq'::regclass);


--
-- Name: statusproperties_has_status fk_idstatusproperties; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.statusproperties_has_status ALTER COLUMN fk_idstatusproperties SET DEFAULT nextval('public.statusproperties_has_status_fk_idstatusproperties_seq'::regclass);


--
-- Name: timevacationsdays idtimevacationsdays; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timevacationsdays ALTER COLUMN idtimevacationsdays SET DEFAULT nextval('public.timevacationsdays_idtimevacationsdays_seq'::regclass);


--
-- Name: timevacationsdaysonetime idtimevacationsdaysonetime; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timevacationsdaysonetime ALTER COLUMN idtimevacationsdaysonetime SET DEFAULT nextval('public.timevacationsdaysonetime_idtimevacationsdaysonetime_seq'::regclass);


--
-- Name: timeworkinghours idtimeworkinghours; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timeworkinghours ALTER COLUMN idtimeworkinghours SET DEFAULT nextval('public.timeworkinghours_idtimeworkinghours_seq'::regclass);


--
-- Name: widget idwidget; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget ALTER COLUMN idwidget SET DEFAULT nextval('public.widget_idwidget_seq'::regclass);


--
-- Name: widgetgroup idgroup; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup ALTER COLUMN idgroup SET DEFAULT nextval('public.widgetgroup_idgroup_seq'::regclass);


--
-- Name: widgettype idwidgettype; Type: DEFAULT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgettype ALTER COLUMN idwidgettype SET DEFAULT nextval('public.widgettype_idwidgettype_seq'::regclass);


--
-- Name: audittrail audittrail_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.audittrail
    ADD CONSTRAINT audittrail_pkey PRIMARY KEY (idaudittrail);


--
-- Name: calendar_has_timevacationsdays calendar_has_timevacationsdays_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdays
    ADD CONSTRAINT calendar_has_timevacationsdays_pkey PRIMARY KEY (calendar_idcalendar, timevacationsdays_idtimevacationsdays);


--
-- Name: calendar_has_timevacationsdaysonetime calendar_has_timevacationsdaysonetime_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdaysonetime
    ADD CONSTRAINT calendar_has_timevacationsdaysonetime_pkey PRIMARY KEY (timevacationsdaysonetime_idtimevacationsdaysonetime, calendar_idcalendar);


--
-- Name: calendar calendar_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar
    ADD CONSTRAINT calendar_pkey PRIMARY KEY (idcalendar);


--
-- Name: classified classified_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.classified
    ADD CONSTRAINT classified_pkey PRIMARY KEY (idclassified);


--
-- Name: dashboard_has_widget dashboard_has_widget_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard_has_widget
    ADD CONSTRAINT dashboard_has_widget_pkey PRIMARY KEY (fk_iddashboard, fk_idwidget);


--
-- Name: dashboard dashboard_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard
    ADD CONSTRAINT dashboard_pkey PRIMARY KEY (iddashboard);


--
-- Name: group display_must_be_unique; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT display_must_be_unique UNIQUE (display);


--
-- Name: downtime downtime_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtime
    ADD CONSTRAINT downtime_pkey PRIMARY KEY (iddowntime);


--
-- Name: downtimeactive downtimeactive_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeactive
    ADD CONSTRAINT downtimeactive_pkey PRIMARY KEY (iddowntimeactive);


--
-- Name: downtimeschedule downtimeschedule_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeschedule
    ADD CONSTRAINT downtimeschedule_pkey PRIMARY KEY (iddowntimeschedule);


--
-- Name: downtimeschedulerepeat downtimeschedulerepeat_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeschedulerepeat
    ADD CONSTRAINT downtimeschedulerepeat_pkey PRIMARY KEY (iddowntimeschedulerepeat);


--
-- Name: eventidentification eventidentification_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.eventidentification
    ADD CONSTRAINT eventidentification_pkey PRIMARY KEY (ideventidentification);


--
-- Name: excludedate excludedate_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.excludedate
    ADD CONSTRAINT excludedate_pkey PRIMARY KEY (idexcludedate);


--
-- Name: exporttemplate exporttemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.exporttemplate
    ADD CONSTRAINT exporttemplate_pkey PRIMARY KEY (idexporttemplate);


--
-- Name: exporttype exporttype_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.exporttype
    ADD CONSTRAINT exporttype_pkey PRIMARY KEY (idexporttype);


--
-- Name: group_has_group group_has_group_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_group
    ADD CONSTRAINT group_has_group_pkey PRIMARY KEY (group_idgroup, idgroup_child_group);


--
-- Name: group_has_host group_has_host_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_host
    ADD CONSTRAINT group_has_host_pkey PRIMARY KEY (group_idgroup, host_idhost);


--
-- Name: group_has_service group_has_service_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_service
    ADD CONSTRAINT group_has_service_pkey PRIMARY KEY (group_idgroup, service_idservice);


--
-- Name: group_has_servicegroup group_has_servicegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_servicegroup
    ADD CONSTRAINT group_has_servicegroup_pkey PRIMARY KEY (group_idgroup, servicegroup_idservicegroup);


--
-- Name: group group_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT group_pkey PRIMARY KEY (idgroup);


--
-- Name: host host_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.host
    ADD CONSTRAINT host_pkey PRIMARY KEY (idhost);


--
-- Name: iconsize iconsize_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.iconsize
    ADD CONSTRAINT iconsize_pkey PRIMARY KEY (idiconsize);


--
-- Name: manualevents manualevents_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.manualevents
    ADD CONSTRAINT manualevents_pkey PRIMARY KEY (idmanualevents);


--
-- Name: monitoredservice_has_monitoredserviceclient monitoredservice_has_monitoredserviceclient_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice_has_monitoredserviceclient
    ADD CONSTRAINT monitoredservice_has_monitoredserviceclient_pkey PRIMARY KEY (idmonitoredservice, idmonitoredserviceclient);


--
-- Name: monitoredservice monitoredservice_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice
    ADD CONSTRAINT monitoredservice_pkey PRIMARY KEY (idmonitoredservice);


--
-- Name: monitoredserviceclient_has_exporttemplate monitoredserviceclient_has_exporttemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredserviceclient_has_exporttemplate
    ADD CONSTRAINT monitoredserviceclient_has_exporttemplate_pkey PRIMARY KEY (idmonitoredserviceclient, idexporttemplate);


--
-- Name: monitoredserviceclient monitoredserviceclient_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredserviceclient
    ADD CONSTRAINT monitoredserviceclient_pkey PRIMARY KEY (idmonitoredserviceclient);


--
-- Name: monitoredservicecomment monitoredservicecomment_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservicecomment
    ADD CONSTRAINT monitoredservicecomment_pkey PRIMARY KEY (idmonitoredservicecomment);


--
-- Name: object_has_downtime object_has_downtime_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object_has_downtime
    ADD CONSTRAINT object_has_downtime_pkey PRIMARY KEY (object_idobject, downtime_iddowntime);


--
-- Name: object object_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object
    ADD CONSTRAINT object_pkey PRIMARY KEY (idobject);


--
-- Name: period period_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.period
    ADD CONSTRAINT period_pkey PRIMARY KEY (idperiod);


--
-- Name: priority priority_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.priority
    ADD CONSTRAINT priority_pkey PRIMARY KEY (idpriority);


--
-- Name: refreshfreq refreshfreq_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.refreshfreq
    ADD CONSTRAINT refreshfreq_pkey PRIMARY KEY (idrefreshfreq);


--
-- Name: reprocessflag reprocessflag_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.reprocessflag
    ADD CONSTRAINT reprocessflag_pkey PRIMARY KEY (date, fk_idobject);


--
-- Name: service service_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.service
    ADD CONSTRAINT service_pkey PRIMARY KEY (idservice);


--
-- Name: servicegroup servicegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.servicegroup
    ADD CONSTRAINT servicegroup_pkey PRIMARY KEY (idservicegroup);


--
-- Name: sla sla_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sla
    ADD CONSTRAINT sla_pkey PRIMARY KEY (idsla);


--
-- Name: sladaily sladaily_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sladaily
    ADD CONSTRAINT sladaily_pkey PRIMARY KEY (idsladaily);


--
-- Name: slalogs slalogs_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.slalogs
    ADD CONSTRAINT slalogs_pkey PRIMARY KEY (idslalogs);


--
-- Name: statusproperties_has_status statusproperties_has_status_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.statusproperties_has_status
    ADD CONSTRAINT statusproperties_has_status_pkey PRIMARY KEY (fk_idstatusproperties, status);


--
-- Name: statusproperties statusproperties_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.statusproperties
    ADD CONSTRAINT statusproperties_pkey PRIMARY KEY (idstatusproperties);


--
-- Name: tbl_migration tbl_migration_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.tbl_migration
    ADD CONSTRAINT tbl_migration_pkey PRIMARY KEY (version);


--
-- Name: timevacationsdays timevacationsdays_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timevacationsdays
    ADD CONSTRAINT timevacationsdays_pkey PRIMARY KEY (idtimevacationsdays);


--
-- Name: timevacationsdaysonetime timevacationsdaysonetime_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timevacationsdaysonetime
    ADD CONSTRAINT timevacationsdaysonetime_pkey PRIMARY KEY (idtimevacationsdaysonetime);


--
-- Name: timeworkinghours timeworkinghours_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.timeworkinghours
    ADD CONSTRAINT timeworkinghours_pkey PRIMARY KEY (idtimeworkinghours);


--
-- Name: widget widget_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT widget_pkey PRIMARY KEY (idwidget);


--
-- Name: widgetgroup_has_group widgetgroup_has_group_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_group
    ADD CONSTRAINT widgetgroup_has_group_pkey PRIMARY KEY (fk_idgrouper, fk_idgrouped);


--
-- Name: widgetgroup_has_widget widgetgroup_has_widget_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_widget
    ADD CONSTRAINT widgetgroup_has_widget_pkey PRIMARY KEY (fk_idgroup, fk_idwidget);


--
-- Name: widgetgroup widgetgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup
    ADD CONSTRAINT widgetgroup_pkey PRIMARY KEY (idgroup);


--
-- Name: widgettype widgettype_pkey; Type: CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgettype
    ADD CONSTRAINT widgettype_pkey PRIMARY KEY (idwidgettype);


--
-- Name: calendar_has_timevacationsdays_calendar_idcalendar_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX calendar_has_timevacationsdays_calendar_idcalendar_idx ON public.calendar_has_timevacationsdays USING btree (calendar_idcalendar);


--
-- Name: calendar_has_timevacationsdaysonetime_calendar_idcalendar_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX calendar_has_timevacationsdaysonetime_calendar_idcalendar_idx ON public.calendar_has_timevacationsdaysonetime USING btree (calendar_idcalendar);


--
-- Name: has_timevacationsdays_timevacationsdays_idtimevacationsdays_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX has_timevacationsdays_timevacationsdays_idtimevacationsdays_idx ON public.calendar_has_timevacationsdays USING btree (timevacationsdays_idtimevacationsdays);


--
-- Name: object_has_downtime_downtime_iddowntime_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX object_has_downtime_downtime_iddowntime_idx ON public.object_has_downtime USING btree (downtime_iddowntime);


--
-- Name: object_has_downtime_object_idobject_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX object_has_downtime_object_idobject_idx ON public.object_has_downtime USING btree (object_idobject);


--
-- Name: object_idsla_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX object_idsla_idx ON public.object USING btree (idsla);


--
-- Name: onetime_timevacationsdaysonetime_idtimevacationsdaysonetime_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX onetime_timevacationsdaysonetime_idtimevacationsdaysonetime_idx ON public.calendar_has_timevacationsdaysonetime USING btree (timevacationsdaysonetime_idtimevacationsdaysonetime);


--
-- Name: sla_idcalendar_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX sla_idcalendar_idx ON public.sla USING btree (idcalendar);


--
-- Name: sla_idtimeworkinghours_idx; Type: INDEX; Schema: public; Owner: slareport
--

CREATE INDEX sla_idtimeworkinghours_idx ON public.sla USING btree (idtimeworkinghours);


--
-- Name: calendar_has_timevacationsdays calendar_has_timevacationsday_timevacationsdays_idtimevaca_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdays
    ADD CONSTRAINT calendar_has_timevacationsday_timevacationsdays_idtimevaca_fkey FOREIGN KEY (timevacationsdays_idtimevacationsdays) REFERENCES public.timevacationsdays(idtimevacationsdays) ON DELETE RESTRICT;


--
-- Name: calendar_has_timevacationsdaysonetime calendar_has_timevacationsday_timevacationsdaysonetime_idt_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdaysonetime
    ADD CONSTRAINT calendar_has_timevacationsday_timevacationsdaysonetime_idt_fkey FOREIGN KEY (timevacationsdaysonetime_idtimevacationsdaysonetime) REFERENCES public.timevacationsdaysonetime(idtimevacationsdaysonetime) ON DELETE RESTRICT;


--
-- Name: calendar_has_timevacationsdays calendar_has_timevacationsdays_calendar_idcalendar_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdays
    ADD CONSTRAINT calendar_has_timevacationsdays_calendar_idcalendar_fkey FOREIGN KEY (calendar_idcalendar) REFERENCES public.calendar(idcalendar) ON DELETE CASCADE;


--
-- Name: calendar_has_timevacationsdaysonetime calendar_has_timevacationsdaysonetime_calendar_idcalendar_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.calendar_has_timevacationsdaysonetime
    ADD CONSTRAINT calendar_has_timevacationsdaysonetime_calendar_idcalendar_fkey FOREIGN KEY (calendar_idcalendar) REFERENCES public.calendar(idcalendar) ON DELETE CASCADE;


--
-- Name: downtime downtime_fk_idclassified_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtime
    ADD CONSTRAINT downtime_fk_idclassified_fkey FOREIGN KEY (fk_idclassified) REFERENCES public.classified(idclassified);


--
-- Name: eventidentification eventidentification_fk_iddowntime_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.eventidentification
    ADD CONSTRAINT eventidentification_fk_iddowntime_fkey FOREIGN KEY (fk_iddowntime) REFERENCES public.downtime(iddowntime) ON DELETE CASCADE;


--
-- Name: group_has_group fk_group_has_group_group_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_group
    ADD CONSTRAINT fk_group_has_group_group_1 FOREIGN KEY (group_idgroup) REFERENCES public."group"(idgroup);


--
-- Name: group_has_group fk_group_has_group_group_2; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_group
    ADD CONSTRAINT fk_group_has_group_group_2 FOREIGN KEY (idgroup_child_group) REFERENCES public."group"(idgroup);


--
-- Name: group_has_host fk_group_has_host_group_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_host
    ADD CONSTRAINT fk_group_has_host_group_1 FOREIGN KEY (group_idgroup) REFERENCES public."group"(idgroup);


--
-- Name: group_has_host fk_group_has_host_host_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_host
    ADD CONSTRAINT fk_group_has_host_host_1 FOREIGN KEY (host_idhost) REFERENCES public.host(idhost);


--
-- Name: group_has_service fk_group_has_service_group_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_service
    ADD CONSTRAINT fk_group_has_service_group_1 FOREIGN KEY (group_idgroup) REFERENCES public."group"(idgroup);


--
-- Name: group_has_service fk_group_has_service_service_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_service
    ADD CONSTRAINT fk_group_has_service_service_1 FOREIGN KEY (service_idservice) REFERENCES public.service(idservice);


--
-- Name: group_has_servicegroup fk_group_has_servicegroup_group_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_servicegroup
    ADD CONSTRAINT fk_group_has_servicegroup_group_1 FOREIGN KEY (group_idgroup) REFERENCES public."group"(idgroup);


--
-- Name: group_has_servicegroup fk_group_has_servicegroup_servicegroup_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.group_has_servicegroup
    ADD CONSTRAINT fk_group_has_servicegroup_servicegroup_1 FOREIGN KEY (servicegroup_idservicegroup) REFERENCES public.servicegroup(idservicegroup);


--
-- Name: group fk_group_priority_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT fk_group_priority_1 FOREIGN KEY (idpriority) REFERENCES public.priority(idpriority);


--
-- Name: dashboard_has_widget fk_iddashboard; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard_has_widget
    ADD CONSTRAINT fk_iddashboard FOREIGN KEY (fk_iddashboard) REFERENCES public.dashboard(iddashboard);


--
-- Name: widgetgroup fk_iddashboard; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup
    ADD CONSTRAINT fk_iddashboard FOREIGN KEY (fk_iddashboard) REFERENCES public.dashboard(iddashboard);


--
-- Name: downtimeschedulerepeat fk_iddowntimeschedule; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeschedulerepeat
    ADD CONSTRAINT fk_iddowntimeschedule FOREIGN KEY (fk_iddowntimeschedule) REFERENCES public.downtimeschedule(iddowntimeschedule);


--
-- Name: downtimeactive fk_iddowntimeschedule; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.downtimeactive
    ADD CONSTRAINT fk_iddowntimeschedule FOREIGN KEY (fk_iddowntimeschedule) REFERENCES public.downtimeschedule(iddowntimeschedule);


--
-- Name: excludedate fk_iddowntimeschedulerepeat; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.excludedate
    ADD CONSTRAINT fk_iddowntimeschedulerepeat FOREIGN KEY (fk_iddowntimeschedulerepeat) REFERENCES public.downtimeschedulerepeat(iddowntimeschedulerepeat);


--
-- Name: monitoredserviceclient_has_exporttemplate fk_idexporttemplate; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredserviceclient_has_exporttemplate
    ADD CONSTRAINT fk_idexporttemplate FOREIGN KEY (idexporttemplate) REFERENCES public.exporttemplate(idexporttemplate);


--
-- Name: exporttemplate fk_idexporttype; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.exporttemplate
    ADD CONSTRAINT fk_idexporttype FOREIGN KEY (fk_idexporttype) REFERENCES public.exporttype(idexporttype);


--
-- Name: widgetgroup_has_widget fk_idgroup; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_widget
    ADD CONSTRAINT fk_idgroup FOREIGN KEY (fk_idgroup) REFERENCES public.widgetgroup(idgroup);


--
-- Name: widgetgroup_has_group fk_idgrouped; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_group
    ADD CONSTRAINT fk_idgrouped FOREIGN KEY (fk_idgrouped) REFERENCES public.widgetgroup(idgroup);


--
-- Name: widgetgroup_has_group fk_idgrouper; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_group
    ADD CONSTRAINT fk_idgrouper FOREIGN KEY (fk_idgrouper) REFERENCES public.widgetgroup(idgroup);


--
-- Name: dashboard_has_widget fk_idiconsize; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard_has_widget
    ADD CONSTRAINT fk_idiconsize FOREIGN KEY (fk_idiconsize) REFERENCES public.iconsize(idiconsize);


--
-- Name: monitoredservice_has_monitoredserviceclient fk_idmonitoredservice; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice_has_monitoredserviceclient
    ADD CONSTRAINT fk_idmonitoredservice FOREIGN KEY (idmonitoredservice) REFERENCES public.monitoredservice(idmonitoredservice);


--
-- Name: monitoredservicecomment fk_idmonitoredservice; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservicecomment
    ADD CONSTRAINT fk_idmonitoredservice FOREIGN KEY (fk_idmonitoredservice) REFERENCES public.monitoredservice(idmonitoredservice);


--
-- Name: monitoredservice_has_monitoredserviceclient fk_idmonitoredserviceclient; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredservice_has_monitoredserviceclient
    ADD CONSTRAINT fk_idmonitoredserviceclient FOREIGN KEY (idmonitoredserviceclient) REFERENCES public.monitoredserviceclient(idmonitoredserviceclient) ON DELETE CASCADE;


--
-- Name: monitoredserviceclient_has_exporttemplate fk_idmonitoredserviceclient; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.monitoredserviceclient_has_exporttemplate
    ADD CONSTRAINT fk_idmonitoredserviceclient FOREIGN KEY (idmonitoredserviceclient) REFERENCES public.monitoredserviceclient(idmonitoredserviceclient);


--
-- Name: widget fk_idobject; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT fk_idobject FOREIGN KEY (fk_idobject) REFERENCES public.object(idobject);


--
-- Name: reprocessflag fk_idobject; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.reprocessflag
    ADD CONSTRAINT fk_idobject FOREIGN KEY (fk_idobject) REFERENCES public.object(idobject) ON DELETE CASCADE;


--
-- Name: widget fk_idperiod; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT fk_idperiod FOREIGN KEY (fk_idperiod) REFERENCES public.period(idperiod);


--
-- Name: object fk_idpriority; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object
    ADD CONSTRAINT fk_idpriority FOREIGN KEY (fk_idpriority) REFERENCES public.priority(idpriority) ON UPDATE SET NULL ON DELETE SET NULL;


--
-- Name: widget fk_idrefreshfreq; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT fk_idrefreshfreq FOREIGN KEY (fk_idrefreshfreq) REFERENCES public.refreshfreq(idrefreshfreq);


--
-- Name: slalogs fk_idslalogs; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.slalogs
    ADD CONSTRAINT fk_idslalogs FOREIGN KEY (fk_idslalogs) REFERENCES public.slalogs(idslalogs) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: statusproperties_has_status fk_idstatusproperties; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.statusproperties_has_status
    ADD CONSTRAINT fk_idstatusproperties FOREIGN KEY (fk_idstatusproperties) REFERENCES public.statusproperties(idstatusproperties);


--
-- Name: widget fk_idstatusproperties; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT fk_idstatusproperties FOREIGN KEY (fk_idstatusproperties) REFERENCES public.statusproperties(idstatusproperties);


--
-- Name: dashboard_has_widget fk_idwidget; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.dashboard_has_widget
    ADD CONSTRAINT fk_idwidget FOREIGN KEY (fk_idwidget) REFERENCES public.widget(idwidget);


--
-- Name: widgetgroup_has_widget fk_idwidget; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widgetgroup_has_widget
    ADD CONSTRAINT fk_idwidget FOREIGN KEY (fk_idwidget) REFERENCES public.widget(idwidget);


--
-- Name: widget fk_idwidgettype; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT fk_idwidgettype FOREIGN KEY (fk_idwidgettype) REFERENCES public.widgettype(idwidgettype);


--
-- Name: service fk_service_host_1; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.service
    ADD CONSTRAINT fk_service_host_1 FOREIGN KEY (idhost) REFERENCES public.host(idhost);


--
-- Name: object_has_downtime object_has_downtime_downtime_iddowntime_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object_has_downtime
    ADD CONSTRAINT object_has_downtime_downtime_iddowntime_fkey FOREIGN KEY (downtime_iddowntime) REFERENCES public.downtime(iddowntime) ON DELETE CASCADE;


--
-- Name: object_has_downtime object_has_downtime_object_idobject_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object_has_downtime
    ADD CONSTRAINT object_has_downtime_object_idobject_fkey FOREIGN KEY (object_idobject) REFERENCES public.object(idobject) ON DELETE RESTRICT;


--
-- Name: object_has_manualevents object_has_manualevents_fk_idmanualevents_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object_has_manualevents
    ADD CONSTRAINT object_has_manualevents_fk_idmanualevents_fkey FOREIGN KEY (fk_idmanualevents) REFERENCES public.manualevents(idmanualevents);


--
-- Name: object_has_manualevents object_has_manualevents_fk_idobject_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object_has_manualevents
    ADD CONSTRAINT object_has_manualevents_fk_idobject_fkey FOREIGN KEY (fk_idobject) REFERENCES public.object(idobject);


--
-- Name: object object_idsla_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.object
    ADD CONSTRAINT object_idsla_fkey FOREIGN KEY (idsla) REFERENCES public.sla(idsla);


--
-- Name: manualevents previous; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.manualevents
    ADD CONSTRAINT previous FOREIGN KEY (previous) REFERENCES public.manualevents(idmanualevents) ON UPDATE SET NULL ON DELETE SET NULL;


--
-- Name: sla sla_idcalendar_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sla
    ADD CONSTRAINT sla_idcalendar_fkey FOREIGN KEY (idcalendar) REFERENCES public.calendar(idcalendar);


--
-- Name: sla sla_idtimeworkinghours_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sla
    ADD CONSTRAINT sla_idtimeworkinghours_fkey FOREIGN KEY (idtimeworkinghours) REFERENCES public.timeworkinghours(idtimeworkinghours);


--
-- Name: sladaily sladaily_fk_idobject_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.sladaily
    ADD CONSTRAINT sladaily_fk_idobject_fkey FOREIGN KEY (fk_idobject) REFERENCES public.object(idobject) ON DELETE CASCADE;


--
-- Name: slalogs slalogs_fk_iddowntime_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.slalogs
    ADD CONSTRAINT slalogs_fk_iddowntime_fkey FOREIGN KEY (fk_iddowntime) REFERENCES public.downtime(iddowntime) ON DELETE SET NULL;


--
-- Name: slalogs slalogs_fk_idsladaily_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slareport
--

ALTER TABLE ONLY public.slalogs
    ADD CONSTRAINT slalogs_fk_idsladaily_fkey FOREIGN KEY (fk_idsladaily) REFERENCES public.sladaily(idsladaily) ON DELETE CASCADE;


-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

