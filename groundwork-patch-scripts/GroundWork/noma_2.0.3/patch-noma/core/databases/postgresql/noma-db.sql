--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

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
-- Name: contactgroups; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE contactgroups (
    id integer NOT NULL,
    name_short character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    view_only smallint DEFAULT 0 NOT NULL,
    timeframe_id integer DEFAULT 0 NOT NULL,
    timezone_id integer DEFAULT (372)::numeric NOT NULL
);


ALTER TABLE public.contactgroups OWNER TO noma;

--
-- Name: contactgroups_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE contactgroups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactgroups_id_seq OWNER TO noma;

--
-- Name: contactgroups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE contactgroups_id_seq OWNED BY contactgroups.id;


--
-- Name: contactgroups_to_contacts; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE contactgroups_to_contacts (
    contactgroup_id integer NOT NULL,
    contact_id integer NOT NULL
);


ALTER TABLE public.contactgroups_to_contacts OWNER TO noma;

--
-- Name: contacts; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE contacts (
    id integer NOT NULL,
    admin smallint NOT NULL,
    username character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    email character varying(255),
    phone character varying(255),
    mobile character varying(255),
    section character varying(255),
    growladdress character varying(255),
    password character varying(255),
    timeframe_id integer DEFAULT 0 NOT NULL,
    timezone_id integer DEFAULT (372)::numeric NOT NULL,
    restrict_alerts smallint
);


ALTER TABLE public.contacts OWNER TO noma;

--
-- Name: contacts_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE contacts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contacts_id_seq OWNER TO noma;

--
-- Name: contacts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE contacts_id_seq OWNED BY contacts.id;


--
-- Name: escalation_stati; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE escalation_stati (
    id integer NOT NULL,
    notification_rule integer,
    starttime integer NOT NULL,
    counter integer NOT NULL,
    incident_id bigint,
    recipients character varying(255),
    host character varying(255) NOT NULL,
    host_alias character varying(255),
    host_address character varying(255),
    hostgroups text,
    service character varying(255),
    servicegroups text,
    check_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    time_string integer NOT NULL,
    type character varying(255) NOT NULL,
    authors character varying(255),
    comments character varying(255),
    output character varying(4096)
);


ALTER TABLE public.escalation_stati OWNER TO noma;

--
-- Name: escalation_stati_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE escalation_stati_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.escalation_stati_id_seq OWNER TO noma;

--
-- Name: escalation_stati_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE escalation_stati_id_seq OWNED BY escalation_stati.id;


--
-- Name: escalations_contacts; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE escalations_contacts (
    id integer NOT NULL,
    notification_id integer NOT NULL,
    on_ok smallint DEFAULT 0,
    on_warning smallint DEFAULT 0,
    on_critical smallint DEFAULT 0,
    on_unknown smallint DEFAULT 0,
    on_host_up smallint DEFAULT 0,
    on_host_unreachable smallint DEFAULT 0,
    on_host_down smallint DEFAULT 0,
    on_type_problem smallint DEFAULT 0,
    on_type_recovery smallint DEFAULT 0,
    on_type_flappingstart smallint DEFAULT 0,
    on_type_flappingstop smallint DEFAULT 0,
    on_type_flappingdisabled smallint DEFAULT 0,
    on_type_downtimestart smallint DEFAULT 0,
    on_type_downtimeend smallint DEFAULT 0,
    on_type_downtimecancelled smallint DEFAULT 0,
    on_type_acknowledgement smallint DEFAULT 0,
    on_type_custom smallint DEFAULT 0,
    notify_after_tries character varying(255) DEFAULT '0'::character varying NOT NULL
);


ALTER TABLE public.escalations_contacts OWNER TO noma;

--
-- Name: escalations_contacts_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE escalations_contacts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.escalations_contacts_id_seq OWNER TO noma;

--
-- Name: escalations_contacts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE escalations_contacts_id_seq OWNED BY escalations_contacts.id;


--
-- Name: escalations_contacts_to_contactgroups; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE escalations_contacts_to_contactgroups (
    escalation_contacts_id integer NOT NULL,
    contactgroup_id integer NOT NULL
);


ALTER TABLE public.escalations_contacts_to_contactgroups OWNER TO noma;

--
-- Name: escalations_contacts_to_contacts; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE escalations_contacts_to_contacts (
    escalation_contacts_id integer NOT NULL,
    contacts_id integer NOT NULL
);


ALTER TABLE public.escalations_contacts_to_contacts OWNER TO noma;

--
-- Name: escalations_contacts_to_methods; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE escalations_contacts_to_methods (
    escalation_contacts_id integer NOT NULL,
    method_id integer NOT NULL
);


ALTER TABLE public.escalations_contacts_to_methods OWNER TO noma;

--
-- Name: holidays; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE holidays (
    id integer NOT NULL,
    holiday_name character varying(255),
    timeframe_id integer,
    contact_id integer,
    holiday_start timestamp without time zone NOT NULL,
    holiday_end timestamp without time zone NOT NULL
);


ALTER TABLE public.holidays OWNER TO noma;

--
-- Name: holidays_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE holidays_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.holidays_id_seq OWNER TO noma;

--
-- Name: holidays_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE holidays_id_seq OWNED BY holidays.id;


--
-- Name: information; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE information (
    id integer NOT NULL,
    type character varying(20) NOT NULL,
    content character varying(20) NOT NULL
);


ALTER TABLE public.information OWNER TO noma;

--
-- Name: notification_logs; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notification_logs (
    id integer NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    counter integer NOT NULL,
    check_type character varying(10) NOT NULL,
    check_result character varying(15) NOT NULL,
    host character varying(255) NOT NULL,
    service character varying(255) NOT NULL,
    notification_type character varying(255) NOT NULL,
    method character varying(255) NOT NULL,
    "user" character varying(255) NOT NULL,
    result character varying(1023) NOT NULL,
    unique_id bigint,
    incident_id bigint,
    notification_rule integer,
    last_method integer
);


ALTER TABLE public.notification_logs OWNER TO noma;

--
-- Name: notification_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE notification_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.notification_logs_id_seq OWNER TO noma;

--
-- Name: notification_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE notification_logs_id_seq OWNED BY notification_logs.id;


--
-- Name: notification_methods; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notification_methods (
    id integer NOT NULL,
    method character varying(255) NOT NULL,
    command character varying(255) NOT NULL,
    contact_field character varying(255) NOT NULL,
    sender character varying(255),
    on_fail integer NOT NULL,
    ack_able smallint NOT NULL
);


ALTER TABLE public.notification_methods OWNER TO noma;

--
-- Name: notification_methods_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE notification_methods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.notification_methods_id_seq OWNER TO noma;

--
-- Name: notification_methods_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE notification_methods_id_seq OWNED BY notification_methods.id;


--
-- Name: notification_stati; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notification_stati (
    id integer NOT NULL,
    host character varying(255) NOT NULL,
    service character varying(255) NOT NULL,
    check_type character varying(10) NOT NULL,
    check_result character varying(15) NOT NULL,
    counter integer NOT NULL,
    pid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.notification_stati OWNER TO noma;

--
-- Name: notification_stati_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE notification_stati_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.notification_stati_id_seq OWNER TO noma;

--
-- Name: notification_stati_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE notification_stati_id_seq OWNED BY notification_stati.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notifications (
    id integer NOT NULL,
    notification_name character varying(255),
    notification_description character varying(1024),
    active smallint NOT NULL,
    username character varying(255) NOT NULL,
    recipients_include character varying(255),
    recipients_exclude character varying(255),
    hosts_include text,
    hosts_exclude text,
    hostgroups_include text,
    hostgroups_exclude text,
    services_include text,
    services_exclude text,
    servicegroups_include text,
    servicegroups_exclude text,
    notify_after_tries character varying(10) DEFAULT '0'::character varying NOT NULL,
    let_notifier_handle smallint DEFAULT 0,
    rollover smallint DEFAULT 0,
    reloop_delay integer DEFAULT 0,
    on_ok smallint DEFAULT 0,
    on_warning smallint DEFAULT 0,
    on_unknown smallint DEFAULT 0,
    on_host_unreachable smallint DEFAULT 0,
    on_critical smallint DEFAULT 0,
    on_host_up smallint DEFAULT 0,
    on_host_down smallint DEFAULT 0,
    on_type_problem smallint DEFAULT 0,
    on_type_recovery smallint DEFAULT 0,
    on_type_flappingstart smallint DEFAULT 0,
    on_type_flappingstop smallint DEFAULT 0,
    on_type_flappingdisabled smallint DEFAULT 0,
    on_type_downtimestart smallint DEFAULT 0,
    on_type_downtimeend smallint DEFAULT 0,
    on_type_downtimecancelled smallint DEFAULT 0,
    on_type_acknowledgement smallint DEFAULT 0,
    on_type_custom smallint DEFAULT 0,
    timezone_id integer DEFAULT (372)::numeric NOT NULL,
    timeframe_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.notifications OWNER TO noma;

--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.notifications_id_seq OWNER TO noma;

--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE notifications_id_seq OWNED BY notifications.id;


--
-- Name: notifications_to_contactgroups; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notifications_to_contactgroups (
    notification_id integer NOT NULL,
    contactgroup_id integer NOT NULL
);


ALTER TABLE public.notifications_to_contactgroups OWNER TO noma;

--
-- Name: notifications_to_contacts; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notifications_to_contacts (
    notification_id integer NOT NULL,
    contact_id integer NOT NULL
);


ALTER TABLE public.notifications_to_contacts OWNER TO noma;

--
-- Name: notifications_to_methods; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE notifications_to_methods (
    notification_id integer NOT NULL,
    method_id integer NOT NULL
);


ALTER TABLE public.notifications_to_methods OWNER TO noma;

--
-- Name: timeframes; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE timeframes (
    id integer NOT NULL,
    timeframe_name character varying(60) NOT NULL,
    dt_validfrom timestamp without time zone NOT NULL,
    dt_validto timestamp without time zone NOT NULL,
    day_monday_all smallint DEFAULT 0,
    day_monday_1st smallint DEFAULT 0,
    day_monday_2nd smallint DEFAULT 0,
    day_monday_3rd smallint DEFAULT 0,
    day_monday_4th smallint DEFAULT 0,
    day_monday_5th smallint DEFAULT 0,
    day_monday_last smallint DEFAULT 0,
    day_tuesday_all smallint DEFAULT 0,
    day_tuesday_1st smallint DEFAULT 0,
    day_tuesday_2nd smallint DEFAULT 0,
    day_tuesday_3rd smallint DEFAULT 0,
    day_tuesday_4th smallint DEFAULT 0,
    day_tuesday_5th smallint DEFAULT 0,
    day_tuesday_last smallint DEFAULT 0,
    day_wednesday_all smallint DEFAULT 0,
    day_wednesday_1st smallint DEFAULT 0,
    day_wednesday_2nd smallint DEFAULT 0,
    day_wednesday_3rd smallint DEFAULT 0,
    day_wednesday_4th smallint DEFAULT 0,
    day_wednesday_5th smallint DEFAULT 0,
    day_wednesday_last smallint DEFAULT 0,
    day_thursday_all smallint DEFAULT 0,
    day_thursday_1st smallint DEFAULT 0,
    day_thursday_2nd smallint DEFAULT 0,
    day_thursday_3rd smallint DEFAULT 0,
    day_thursday_4th smallint DEFAULT 0,
    day_thursday_5th smallint DEFAULT 0,
    day_thursday_last smallint DEFAULT 0,
    day_friday_all smallint DEFAULT 0,
    day_friday_1st smallint DEFAULT 0,
    day_friday_2nd smallint DEFAULT 0,
    day_friday_3rd smallint DEFAULT 0,
    day_friday_4th smallint DEFAULT 0,
    day_friday_5th smallint DEFAULT 0,
    day_friday_last smallint DEFAULT 0,
    day_saturday_all smallint DEFAULT 0,
    day_saturday_1st smallint DEFAULT 0,
    day_saturday_2nd smallint DEFAULT 0,
    day_saturday_3rd smallint DEFAULT 0,
    day_saturday_4th smallint DEFAULT 0,
    day_saturday_5th smallint DEFAULT 0,
    day_saturday_last smallint DEFAULT 0,
    day_sunday_all smallint DEFAULT 0,
    day_sunday_1st smallint DEFAULT 0,
    day_sunday_2nd smallint DEFAULT 0,
    day_sunday_3rd smallint DEFAULT 0,
    day_sunday_4th smallint DEFAULT 0,
    day_sunday_5th smallint DEFAULT 0,
    day_sunday_last smallint DEFAULT 0,
    time_monday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_monday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_monday_invert smallint DEFAULT 0,
    time_tuesday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_tuesday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_tuesday_invert smallint DEFAULT 0,
    time_wednesday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_wednesday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_wednesday_invert smallint DEFAULT 0,
    time_thursday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_thursday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_thursday_invert smallint DEFAULT 0,
    time_friday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_friday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_friday_invert smallint DEFAULT 0,
    time_saturday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_saturday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_saturday_invert smallint DEFAULT 0,
    time_sunday_start time without time zone DEFAULT '00:00:00'::time without time zone,
    time_sunday_stop time without time zone DEFAULT '00:00:00'::time without time zone,
    time_sunday_invert smallint DEFAULT 0
);


ALTER TABLE public.timeframes OWNER TO noma;

--
-- Name: timeframes_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE timeframes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.timeframes_id_seq OWNER TO noma;

--
-- Name: timeframes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE timeframes_id_seq OWNED BY timeframes.id;


--
-- Name: timezones; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE timezones (
    id integer NOT NULL,
    timezone character varying(255) NOT NULL,
    time_diff smallint NOT NULL
);


ALTER TABLE public.timezones OWNER TO noma;

--
-- Name: tmp_active; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE tmp_active (
    id integer NOT NULL,
    notify_id bigint NOT NULL,
    command_id integer,
    dest character varying(255),
    from_user character varying(255),
    time_string character varying(255),
    "user" character varying(255),
    method character varying(255),
    notify_cmd character varying(255),
    retries integer DEFAULT 0,
    rule integer DEFAULT 0,
    progress smallint DEFAULT 0,
    esc_flag smallint DEFAULT 0,
    bundled bigint DEFAULT 0,
    stime integer DEFAULT 0
);


ALTER TABLE public.tmp_active OWNER TO noma;

--
-- Name: tmp_active_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE tmp_active_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmp_active_id_seq OWNER TO noma;

--
-- Name: tmp_active_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE tmp_active_id_seq OWNED BY tmp_active.id;


--
-- Name: tmp_commands; Type: TABLE; Schema: public; Owner: noma; Tablespace: 
--

CREATE TABLE tmp_commands (
    id integer NOT NULL,
    operation character varying(255),
    external_id bigint NOT NULL,
    recipients character varying(255) NOT NULL,
    host character varying(255),
    host_alias character varying(255),
    host_address character varying(255),
    hostgroups text NOT NULL,
    service character varying(255),
    servicegroups text,
    check_type character varying(255),
    status character varying(255),
    stime integer DEFAULT 0,
    notification_type character varying(255),
    authors character varying(255),
    comments character varying(255),
    output character varying(4096)
);


ALTER TABLE public.tmp_commands OWNER TO noma;

--
-- Name: tmp_commands_id_seq; Type: SEQUENCE; Schema: public; Owner: noma
--

CREATE SEQUENCE tmp_commands_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmp_commands_id_seq OWNER TO noma;

--
-- Name: tmp_commands_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: noma
--

ALTER SEQUENCE tmp_commands_id_seq OWNED BY tmp_commands.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY contactgroups ALTER COLUMN id SET DEFAULT nextval('contactgroups_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY contacts ALTER COLUMN id SET DEFAULT nextval('contacts_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY escalation_stati ALTER COLUMN id SET DEFAULT nextval('escalation_stati_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY escalations_contacts ALTER COLUMN id SET DEFAULT nextval('escalations_contacts_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY holidays ALTER COLUMN id SET DEFAULT nextval('holidays_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY notification_logs ALTER COLUMN id SET DEFAULT nextval('notification_logs_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY notification_methods ALTER COLUMN id SET DEFAULT nextval('notification_methods_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY notification_stati ALTER COLUMN id SET DEFAULT nextval('notification_stati_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY notifications ALTER COLUMN id SET DEFAULT nextval('notifications_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY timeframes ALTER COLUMN id SET DEFAULT nextval('timeframes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY tmp_active ALTER COLUMN id SET DEFAULT nextval('tmp_active_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: noma
--

ALTER TABLE ONLY tmp_commands ALTER COLUMN id SET DEFAULT nextval('tmp_commands_id_seq'::regclass);


--
-- Name: contactgroups_name_key; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY contactgroups
    ADD CONSTRAINT contactgroups_name_key UNIQUE (name);


--
-- Name: contactgroups_name_short_key; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY contactgroups
    ADD CONSTRAINT contactgroups_name_short_key UNIQUE (name_short);


--
-- Name: contactgroups_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY contactgroups
    ADD CONSTRAINT contactgroups_pkey PRIMARY KEY (id);


--
-- Name: contacts_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (id);


--
-- Name: contacts_username_key; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY contacts
    ADD CONSTRAINT contacts_username_key UNIQUE (username);


--
-- Name: escalation_stati_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY escalation_stati
    ADD CONSTRAINT escalation_stati_pkey PRIMARY KEY (id);


--
-- Name: escalations_contacts_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY escalations_contacts
    ADD CONSTRAINT escalations_contacts_pkey PRIMARY KEY (id);


--
-- Name: holidays_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY holidays
    ADD CONSTRAINT holidays_pkey PRIMARY KEY (id);


--
-- Name: notification_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY notification_logs
    ADD CONSTRAINT notification_logs_pkey PRIMARY KEY (id);


--
-- Name: notification_methods_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY notification_methods
    ADD CONSTRAINT notification_methods_pkey PRIMARY KEY (id);


--
-- Name: notification_stati_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY notification_stati
    ADD CONSTRAINT notification_stati_pkey PRIMARY KEY (id);


--
-- Name: notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: timeframes_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY timeframes
    ADD CONSTRAINT timeframes_pkey PRIMARY KEY (id);


--
-- Name: timezones_pkey; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY timezones
    ADD CONSTRAINT timezones_pkey PRIMARY KEY (id);


--
-- Name: tmp_active_id_key; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY tmp_active
    ADD CONSTRAINT tmp_active_id_key UNIQUE (id);


--
-- Name: tmp_commands_id_key; Type: CONSTRAINT; Schema: public; Owner: noma; Tablespace: 
--

ALTER TABLE ONLY tmp_commands
    ADD CONSTRAINT tmp_commands_id_key UNIQUE (id);


--
-- Name: contactgroups_to_contacts_contact_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX contactgroups_to_contacts_contact_id ON contactgroups_to_contacts USING btree (contact_id);


--
-- Name: contactgroups_to_contacts_contactgroup_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX contactgroups_to_contacts_contactgroup_id ON contactgroups_to_contacts USING btree (contactgroup_id);


--
-- Name: escalation_stati_host_service; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalation_stati_host_service ON escalation_stati USING btree (host, service);


--
-- Name: escalation_stati_incident_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalation_stati_incident_id ON escalation_stati USING btree (incident_id);


--
-- Name: escalations_contacts_notification_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_notification_id ON escalations_contacts USING btree (notification_id);


--
-- Name: escalations_contacts_to_contactgroups_contactgroup_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_contactgroups_contactgroup_id ON escalations_contacts_to_contactgroups USING btree (contactgroup_id);


--
-- Name: escalations_contacts_to_contactgroups_escalation_contacts_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_contactgroups_escalation_contacts_id ON escalations_contacts_to_contactgroups USING btree (escalation_contacts_id);


--
-- Name: escalations_contacts_to_contacts_contacts_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_contacts_contacts_id ON escalations_contacts_to_contacts USING btree (contacts_id);


--
-- Name: escalations_contacts_to_contacts_escalation_contacts_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_contacts_escalation_contacts_id ON escalations_contacts_to_contacts USING btree (escalation_contacts_id);


--
-- Name: escalations_contacts_to_methods_escalation_contacts_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_methods_escalation_contacts_id ON escalations_contacts_to_methods USING btree (escalation_contacts_id);


--
-- Name: escalations_contacts_to_methods_method_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX escalations_contacts_to_methods_method_id ON escalations_contacts_to_methods USING btree (method_id);


--
-- Name: holidays_contact_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX holidays_contact_id ON holidays USING btree (contact_id);


--
-- Name: holidays_timeframe_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX holidays_timeframe_id ON holidays USING btree (timeframe_id);


--
-- Name: information_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX information_id ON information USING btree (id);


--
-- Name: notification_logs_host_service; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notification_logs_host_service ON notification_logs USING btree (host, service);


--
-- Name: notification_logs_incident_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notification_logs_incident_id ON notification_logs USING btree (incident_id);


--
-- Name: notification_logs_unique_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notification_logs_unique_id ON notification_logs USING btree (unique_id);


--
-- Name: notification_stati_host_service; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notification_stati_host_service ON notification_stati USING btree (host, service);


--
-- Name: notifications_time; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_time ON notifications USING btree (timezone_id, timeframe_id);


--
-- Name: notifications_to_contactgroups_contactgroup_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_contactgroups_contactgroup_id ON notifications_to_contactgroups USING btree (contactgroup_id);


--
-- Name: notifications_to_contactgroups_notification_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_contactgroups_notification_id ON notifications_to_contactgroups USING btree (notification_id);


--
-- Name: notifications_to_contacts_contact_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_contacts_contact_id ON notifications_to_contacts USING btree (contact_id);


--
-- Name: notifications_to_contacts_notification_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_contacts_notification_id ON notifications_to_contacts USING btree (notification_id);


--
-- Name: notifications_to_methods_method_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_methods_method_id ON notifications_to_methods USING btree (method_id);


--
-- Name: notifications_to_methods_notification_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX notifications_to_methods_notification_id ON notifications_to_methods USING btree (notification_id);


--
-- Name: tmp_active_command_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX tmp_active_command_id ON tmp_active USING btree (command_id);


--
-- Name: tmp_active_notify_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX tmp_active_notify_id ON tmp_active USING btree (notify_id);


--
-- Name: tmp_commands_external_id; Type: INDEX; Schema: public; Owner: noma; Tablespace: 
--

CREATE INDEX tmp_commands_external_id ON tmp_commands USING btree (external_id);


--
-- Name: tmp_active_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: noma
--

ALTER TABLE ONLY tmp_active
    ADD CONSTRAINT tmp_active_ibfk_1 FOREIGN KEY (command_id) REFERENCES tmp_commands(id) ON UPDATE RESTRICT ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

