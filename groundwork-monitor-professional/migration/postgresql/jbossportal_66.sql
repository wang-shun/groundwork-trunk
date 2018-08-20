--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
\set ON_ERROR_STOP 1

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
-- Name: gw_ext_role_attributes; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE gw_ext_role_attributes (
    jbp_rid bigint NOT NULL,
    jbp_name character varying(255),
    dashboard_links_disabled boolean,
    hg_list character varying(4096),
    sg_list character varying(4096),
    defaulthostgroup character varying(255),
    defaultservicegroup character varying(255),
    restrictiontype character varying(255),
    actions_enabled boolean
);


ALTER TABLE public.gw_ext_role_attributes OWNER TO jboss;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO jboss;

--
-- Name: instance_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE instance_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.instance_seq OWNER TO jboss;

--
-- Name: jbp_context; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_context (
    pk bigint NOT NULL
);


ALTER TABLE public.jbp_context OWNER TO jboss;

--
-- Name: jbp_instance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_instance (
    pk bigint NOT NULL,
    id character varying(255) NOT NULL,
    portlet_ref character varying(255),
    modifiable boolean NOT NULL,
    ser_state bytea
);


ALTER TABLE public.jbp_instance OWNER TO jboss;

--
-- Name: jbp_instance_display_names; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_instance_display_names (
    instance_pk bigint NOT NULL,
    text character varying(255),
    locale character varying(255) NOT NULL
);


ALTER TABLE public.jbp_instance_display_names OWNER TO jboss;

--
-- Name: jbp_instance_per_user; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_instance_per_user (
    pk bigint NOT NULL,
    instance_pk bigint,
    ser_state bytea,
    user_id character varying(160) NOT NULL,
    portlet_ref character varying(170) NOT NULL
);


ALTER TABLE public.jbp_instance_per_user OWNER TO jboss;

--
-- Name: jbp_instance_security; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_instance_security (
    pk bigint NOT NULL,
    "ROLE" character varying(255) NOT NULL,
    instance_pk bigint
);


ALTER TABLE public.jbp_instance_security OWNER TO jboss;

--
-- Name: jbp_instance_security_actions; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_instance_security_actions (
    pk bigint NOT NULL,
    actions character varying(255)
);


ALTER TABLE public.jbp_instance_security_actions OWNER TO jboss;

--
-- Name: jbp_nagvis_perm_membership; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_nagvis_perm_membership (
    jbp_rid bigint NOT NULL,
    nv_pid integer NOT NULL
);


ALTER TABLE public.jbp_nagvis_perm_membership OWNER TO jboss;

--
-- Name: jbp_nagvis_perms; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_nagvis_perms (
    nv_pid integer NOT NULL,
    nv_mod character varying(100),
    nv_act character varying(100),
    nv_obj character varying(100)
);


ALTER TABLE public.jbp_nagvis_perms OWNER TO jboss;

--
-- Name: jbp_nagvis_perms_nv_pid_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE jbp_nagvis_perms_nv_pid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.jbp_nagvis_perms_nv_pid_seq OWNER TO jboss;

--
-- Name: jbp_nagvis_perms_nv_pid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jboss
--

ALTER SEQUENCE jbp_nagvis_perms_nv_pid_seq OWNED BY jbp_nagvis_perms.nv_pid;


--
-- Name: jbp_object_node; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_object_node (
    pk bigint NOT NULL,
    "PATH" character varying(255),
    name character varying(255),
    parent_key bigint
);


ALTER TABLE public.jbp_object_node OWNER TO jboss;

--
-- Name: jbp_object_node_sec; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_object_node_sec (
    pk bigint NOT NULL,
    "ROLE" character varying(255) NOT NULL,
    node_key bigint
);


ALTER TABLE public.jbp_object_node_sec OWNER TO jboss;

--
-- Name: jbp_object_node_sec_actions; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_object_node_sec_actions (
    pk bigint NOT NULL,
    actions character varying(255)
);


ALTER TABLE public.jbp_object_node_sec_actions OWNER TO jboss;

--
-- Name: jbp_page; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_page (
    pk bigint NOT NULL
);


ALTER TABLE public.jbp_page OWNER TO jboss;

--
-- Name: jbp_portal; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal (
    pk bigint NOT NULL
);


ALTER TABLE public.jbp_portal OWNER TO jboss;

--
-- Name: jbp_portal_mode; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal_mode (
    pk bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.jbp_portal_mode OWNER TO jboss;

--
-- Name: jbp_portal_object; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal_object (
    pk bigint NOT NULL,
    listener character varying(255)
);


ALTER TABLE public.jbp_portal_object OWNER TO jboss;

--
-- Name: jbp_portal_object_dnames; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal_object_dnames (
    instance_pk bigint NOT NULL,
    text character varying(255),
    locale character varying(255) NOT NULL
);


ALTER TABLE public.jbp_portal_object_dnames OWNER TO jboss;

--
-- Name: jbp_portal_object_props; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal_object_props (
    object_key bigint NOT NULL,
    jbp_value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.jbp_portal_object_props OWNER TO jboss;

--
-- Name: jbp_portal_window_state; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portal_window_state (
    pk bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.jbp_portal_window_state OWNER TO jboss;

--
-- Name: jbp_portlet_consumer; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_consumer (
    pk bigint NOT NULL,
    id character varying(255) NOT NULL,
    name character varying(255),
    agent character varying(255),
    status integer,
    group_pk bigint
);


ALTER TABLE public.jbp_portlet_consumer OWNER TO jboss;

--
-- Name: jbp_portlet_group; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_group (
    pk bigint NOT NULL,
    id character varying(255) NOT NULL,
    status integer
);


ALTER TABLE public.jbp_portlet_group OWNER TO jboss;

--
-- Name: jbp_portlet_reg; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_reg (
    pk bigint NOT NULL,
    handle character varying(255),
    status integer,
    consumer_pk bigint NOT NULL
);


ALTER TABLE public.jbp_portlet_reg OWNER TO jboss;

--
-- Name: jbp_portlet_reg_properties; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_reg_properties (
    registration_pk bigint NOT NULL,
    value character varying(255),
    qname character varying(255) NOT NULL
);


ALTER TABLE public.jbp_portlet_reg_properties OWNER TO jboss;

--
-- Name: jbp_portlet_state; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_state (
    pk bigint NOT NULL,
    portlet_id character varying(255) NOT NULL,
    registration_id bigint,
    registration_time timestamp without time zone,
    termination_time timestamp without time zone,
    parent_pk bigint
);


ALTER TABLE public.jbp_portlet_state OWNER TO jboss;

--
-- Name: jbp_portlet_state_entry; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_state_entry (
    pk bigint NOT NULL,
    name character varying(255),
    type integer NOT NULL,
    read_only boolean NOT NULL,
    entry_key bigint
);


ALTER TABLE public.jbp_portlet_state_entry OWNER TO jboss;

--
-- Name: jbp_portlet_state_entry_value; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_portlet_state_entry_value (
    pk bigint NOT NULL,
    jbp_value character varying(255),
    idx integer NOT NULL
);


ALTER TABLE public.jbp_portlet_state_entry_value OWNER TO jboss;

--
-- Name: jbp_role_membership; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_role_membership (
    jbp_uid bigint NOT NULL,
    jbp_rid bigint NOT NULL
);


ALTER TABLE public.jbp_role_membership OWNER TO jboss;

--
-- Name: jbp_roles; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_roles (
    jbp_rid bigint NOT NULL,
    jbp_name character varying(255),
    jbp_displayname character varying(255)
);


ALTER TABLE public.jbp_roles OWNER TO jboss;

--
-- Name: jbp_user_prop; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_user_prop (
    jbp_uid bigint NOT NULL,
    jbp_value character varying(255),
    jbp_name character varying(255) NOT NULL
);


ALTER TABLE public.jbp_user_prop OWNER TO jboss;

--
-- Name: jbp_users; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_users (
    jbp_uid bigint NOT NULL,
    jbp_uname character varying(255),
    jbp_givenname character varying(255),
    jbp_familyname character varying(255),
    jbp_password character varying(255),
    jbp_realemail character varying(255),
    jbp_fakeemail character varying(255),
    jbp_regdate timestamp without time zone,
    jbp_viewrealemail boolean,
    jbp_enabled boolean
);


ALTER TABLE public.jbp_users OWNER TO jboss;

--
-- Name: jbp_window; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbp_window (
    pk bigint NOT NULL,
    instance_ref character varying(255)
);


ALTER TABLE public.jbp_window OWNER TO jboss;

--
-- Name: jbpm_action; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_action (
    id_ bigint NOT NULL,
    class character(1) NOT NULL,
    name_ character varying(255),
    ispropagationallowed_ boolean,
    actionexpression_ character varying(255),
    isasync_ boolean,
    referencedaction_ bigint,
    actiondelegation_ bigint,
    event_ bigint,
    processdefinition_ bigint,
    timername_ character varying(255),
    duedate_ character varying(255),
    repeat_ character varying(255),
    transitionname_ character varying(255),
    timeraction_ bigint,
    expression_ character varying(4000),
    eventindex_ integer,
    exceptionhandler_ bigint,
    exceptionhandlerindex_ integer
);


ALTER TABLE public.jbpm_action OWNER TO jboss;

--
-- Name: jbpm_bytearray; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_bytearray (
    id_ bigint NOT NULL,
    name_ character varying(255),
    filedefinition_ bigint
);


ALTER TABLE public.jbpm_bytearray OWNER TO jboss;

--
-- Name: jbpm_byteblock; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_byteblock (
    processfile_ bigint NOT NULL,
    bytes_ bytea,
    index_ integer NOT NULL
);


ALTER TABLE public.jbpm_byteblock OWNER TO jboss;

--
-- Name: jbpm_comment; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_comment (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    actorid_ character varying(255),
    time_ timestamp without time zone,
    message_ character varying(4000),
    token_ bigint,
    taskinstance_ bigint,
    tokenindex_ integer,
    taskinstanceindex_ integer
);


ALTER TABLE public.jbpm_comment OWNER TO jboss;

--
-- Name: jbpm_decisionconditions; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_decisionconditions (
    decision_ bigint NOT NULL,
    transitionname_ character varying(255),
    expression_ character varying(255),
    index_ integer NOT NULL
);


ALTER TABLE public.jbpm_decisionconditions OWNER TO jboss;

--
-- Name: jbpm_delegation; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_delegation (
    id_ bigint NOT NULL,
    classname_ character varying(4000),
    configuration_ character varying(4000),
    configtype_ character varying(255),
    processdefinition_ bigint
);


ALTER TABLE public.jbpm_delegation OWNER TO jboss;

--
-- Name: jbpm_event; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_event (
    id_ bigint NOT NULL,
    eventtype_ character varying(255),
    type_ character(1),
    graphelement_ bigint,
    processdefinition_ bigint,
    node_ bigint,
    transition_ bigint,
    task_ bigint
);


ALTER TABLE public.jbpm_event OWNER TO jboss;

--
-- Name: jbpm_exceptionhandler; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_exceptionhandler (
    id_ bigint NOT NULL,
    exceptionclassname_ character varying(4000),
    type_ character(1),
    graphelement_ bigint,
    processdefinition_ bigint,
    graphelementindex_ integer,
    node_ bigint,
    transition_ bigint,
    task_ bigint
);


ALTER TABLE public.jbpm_exceptionhandler OWNER TO jboss;

--
-- Name: jbpm_id_group; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_id_group (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(255),
    type_ character varying(255),
    parent_ bigint
);


ALTER TABLE public.jbpm_id_group OWNER TO jboss;

--
-- Name: jbpm_id_membership; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_id_membership (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(255),
    role_ character varying(255),
    user_ bigint,
    group_ bigint
);


ALTER TABLE public.jbpm_id_membership OWNER TO jboss;

--
-- Name: jbpm_id_permissions; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_id_permissions (
    entity_ bigint NOT NULL,
    class_ character varying(255),
    name_ character varying(255),
    action_ character varying(255)
);


ALTER TABLE public.jbpm_id_permissions OWNER TO jboss;

--
-- Name: jbpm_id_user; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_id_user (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(255),
    email_ character varying(255),
    password_ character varying(255)
);


ALTER TABLE public.jbpm_id_user OWNER TO jboss;

--
-- Name: jbpm_job; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_job (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    version_ integer NOT NULL,
    duedate_ timestamp without time zone,
    processinstance_ bigint,
    token_ bigint,
    taskinstance_ bigint,
    issuspended_ boolean,
    isexclusive_ boolean,
    lockowner_ character varying(255),
    locktime_ timestamp without time zone,
    exception_ character varying(4000),
    retries_ integer,
    name_ character varying(255),
    repeat_ character varying(255),
    transitionname_ character varying(255),
    action_ bigint,
    graphelementtype_ character varying(255),
    graphelement_ bigint,
    node_ bigint
);


ALTER TABLE public.jbpm_job OWNER TO jboss;

--
-- Name: jbpm_log; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_log (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    index_ integer,
    date_ timestamp without time zone,
    token_ bigint,
    parent_ bigint,
    message_ character varying(4000),
    exception_ character varying(4000),
    action_ bigint,
    node_ bigint,
    enter_ timestamp without time zone,
    leave_ timestamp without time zone,
    duration_ bigint,
    newlongvalue_ bigint,
    transition_ bigint,
    child_ bigint,
    sourcenode_ bigint,
    destinationnode_ bigint,
    variableinstance_ bigint,
    oldbytearray_ bigint,
    newbytearray_ bigint,
    olddatevalue_ timestamp without time zone,
    newdatevalue_ timestamp without time zone,
    olddoublevalue_ double precision,
    newdoublevalue_ double precision,
    oldlongidclass_ character varying(255),
    oldlongidvalue_ bigint,
    newlongidclass_ character varying(255),
    newlongidvalue_ bigint,
    oldstringidclass_ character varying(255),
    oldstringidvalue_ character varying(255),
    newstringidclass_ character varying(255),
    newstringidvalue_ character varying(255),
    oldlongvalue_ bigint,
    oldstringvalue_ character varying(4000),
    newstringvalue_ character varying(4000),
    taskinstance_ bigint,
    taskactorid_ character varying(255),
    taskoldactorid_ character varying(255),
    swimlaneinstance_ bigint
);


ALTER TABLE public.jbpm_log OWNER TO jboss;

--
-- Name: jbpm_moduledefinition; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_moduledefinition (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(4000),
    processdefinition_ bigint,
    starttask_ bigint
);


ALTER TABLE public.jbpm_moduledefinition OWNER TO jboss;

--
-- Name: jbpm_moduleinstance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_moduleinstance (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    version_ integer NOT NULL,
    processinstance_ bigint,
    taskmgmtdefinition_ bigint,
    name_ character varying(255)
);


ALTER TABLE public.jbpm_moduleinstance OWNER TO jboss;

--
-- Name: jbpm_node; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_node (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(255),
    description_ character varying(4000),
    processdefinition_ bigint,
    isasync_ boolean,
    isasyncexcl_ boolean,
    action_ bigint,
    superstate_ bigint,
    subprocname_ character varying(255),
    subprocessdefinition_ bigint,
    decisionexpression_ character varying(255),
    decisiondelegation bigint,
    script_ bigint,
    signal_ integer,
    createtasks_ boolean,
    endtasks_ boolean,
    nodecollectionindex_ integer
);


ALTER TABLE public.jbpm_node OWNER TO jboss;

--
-- Name: jbpm_pooledactor; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_pooledactor (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    actorid_ character varying(255),
    swimlaneinstance_ bigint
);


ALTER TABLE public.jbpm_pooledactor OWNER TO jboss;

--
-- Name: jbpm_processdefinition; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_processdefinition (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    name_ character varying(255),
    description_ character varying(4000),
    version_ integer,
    isterminationimplicit_ boolean,
    startstate_ bigint
);


ALTER TABLE public.jbpm_processdefinition OWNER TO jboss;

--
-- Name: jbpm_processinstance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_processinstance (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    key_ character varying(255),
    start_ timestamp without time zone,
    end_ timestamp without time zone,
    issuspended_ boolean,
    processdefinition_ bigint,
    roottoken_ bigint,
    superprocesstoken_ bigint
);


ALTER TABLE public.jbpm_processinstance OWNER TO jboss;

--
-- Name: jbpm_runtimeaction; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_runtimeaction (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    eventtype_ character varying(255),
    type_ character(1),
    graphelement_ bigint,
    processinstance_ bigint,
    action_ bigint,
    processinstanceindex_ integer
);


ALTER TABLE public.jbpm_runtimeaction OWNER TO jboss;

--
-- Name: jbpm_swimlane; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_swimlane (
    id_ bigint NOT NULL,
    name_ character varying(255),
    actoridexpression_ character varying(255),
    pooledactorsexpression_ character varying(255),
    assignmentdelegation_ bigint,
    taskmgmtdefinition_ bigint
);


ALTER TABLE public.jbpm_swimlane OWNER TO jboss;

--
-- Name: jbpm_swimlaneinstance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_swimlaneinstance (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    name_ character varying(255),
    actorid_ character varying(255),
    swimlane_ bigint,
    taskmgmtinstance_ bigint
);


ALTER TABLE public.jbpm_swimlaneinstance OWNER TO jboss;

--
-- Name: jbpm_task; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_task (
    id_ bigint NOT NULL,
    name_ character varying(255),
    description_ character varying(4000),
    processdefinition_ bigint,
    isblocking_ boolean,
    issignalling_ boolean,
    condition_ character varying(255),
    duedate_ character varying(255),
    priority_ integer,
    actoridexpression_ character varying(255),
    pooledactorsexpression_ character varying(255),
    taskmgmtdefinition_ bigint,
    tasknode_ bigint,
    startstate_ bigint,
    assignmentdelegation_ bigint,
    swimlane_ bigint,
    taskcontroller_ bigint
);


ALTER TABLE public.jbpm_task OWNER TO jboss;

--
-- Name: jbpm_taskactorpool; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_taskactorpool (
    taskinstance_ bigint NOT NULL,
    pooledactor_ bigint NOT NULL
);


ALTER TABLE public.jbpm_taskactorpool OWNER TO jboss;

--
-- Name: jbpm_taskcontroller; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_taskcontroller (
    id_ bigint NOT NULL,
    taskcontrollerdelegation_ bigint
);


ALTER TABLE public.jbpm_taskcontroller OWNER TO jboss;

--
-- Name: jbpm_taskinstance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_taskinstance (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    version_ integer NOT NULL,
    name_ character varying(255),
    description_ character varying(4000),
    actorid_ character varying(255),
    create_ timestamp without time zone,
    start_ timestamp without time zone,
    end_ timestamp without time zone,
    duedate_ timestamp without time zone,
    priority_ integer,
    iscancelled_ boolean,
    issuspended_ boolean,
    isopen_ boolean,
    issignalling_ boolean,
    isblocking_ boolean,
    task_ bigint,
    token_ bigint,
    procinst_ bigint,
    swimlaninstance_ bigint,
    taskmgmtinstance_ bigint
);


ALTER TABLE public.jbpm_taskinstance OWNER TO jboss;

--
-- Name: jbpm_token; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_token (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    name_ character varying(255),
    start_ timestamp without time zone,
    end_ timestamp without time zone,
    nodeenter_ timestamp without time zone,
    nextlogindex_ integer,
    isabletoreactivateparent_ boolean,
    isterminationimplicit_ boolean,
    issuspended_ boolean,
    lock_ character varying(255),
    node_ bigint,
    processinstance_ bigint,
    parent_ bigint,
    subprocessinstance_ bigint
);


ALTER TABLE public.jbpm_token OWNER TO jboss;

--
-- Name: jbpm_tokenvariablemap; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_tokenvariablemap (
    id_ bigint NOT NULL,
    version_ integer NOT NULL,
    token_ bigint,
    contextinstance_ bigint
);


ALTER TABLE public.jbpm_tokenvariablemap OWNER TO jboss;

--
-- Name: jbpm_transition; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_transition (
    id_ bigint NOT NULL,
    name_ character varying(255),
    description_ character varying(4000),
    processdefinition_ bigint,
    from_ bigint,
    to_ bigint,
    condition_ character varying(255),
    fromindex_ integer
);


ALTER TABLE public.jbpm_transition OWNER TO jboss;

--
-- Name: jbpm_variableaccess; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_variableaccess (
    id_ bigint NOT NULL,
    variablename_ character varying(255),
    access_ character varying(255),
    mappedname_ character varying(255),
    processstate_ bigint,
    taskcontroller_ bigint,
    index_ integer,
    script_ bigint
);


ALTER TABLE public.jbpm_variableaccess OWNER TO jboss;

--
-- Name: jbpm_variableinstance; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE jbpm_variableinstance (
    id_ bigint NOT NULL,
    class_ character(1) NOT NULL,
    version_ integer NOT NULL,
    name_ character varying(255),
    converter_ character(1),
    token_ bigint,
    tokenvariablemap_ bigint,
    processinstance_ bigint,
    bytearrayvalue_ bigint,
    datevalue_ timestamp without time zone,
    doublevalue_ double precision,
    longidclass_ character varying(255),
    longvalue_ bigint,
    stringidclass_ character varying(255),
    stringvalue_ character varying(255),
    taskinstance_ bigint
);


ALTER TABLE public.jbpm_variableinstance OWNER TO jboss;

--
-- Name: nav_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE nav_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.nav_seq OWNER TO jboss;

--
-- Name: portal_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE portal_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.portal_seq OWNER TO jboss;

--
-- Name: portlet_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE portlet_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.portlet_seq OWNER TO jboss;

--
-- Name: sec_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE sec_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sec_seq OWNER TO jboss;

--
-- Name: user_navigation; Type: TABLE; Schema: public; Owner: jboss; Tablespace: 
--

CREATE TABLE user_navigation (
    id integer NOT NULL,
    user_id bigint NOT NULL,
    node_id integer NOT NULL,
    node_name character varying(254) NOT NULL,
    node_type character varying(50) NOT NULL,
    parent_info character varying(600),
    tooltip character varying(254) NOT NULL,
    app_type character varying(50),
    tab_history character varying(1500),
    node_label character varying(254)
);


ALTER TABLE public.user_navigation OWNER TO jboss;

--
-- Name: user_seq; Type: SEQUENCE; Schema: public; Owner: jboss
--

CREATE SEQUENCE user_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_seq OWNER TO jboss;

--
-- Name: nv_pid; Type: DEFAULT; Schema: public; Owner: jboss
--

ALTER TABLE jbp_nagvis_perms ALTER COLUMN nv_pid SET DEFAULT nextval('jbp_nagvis_perms_nv_pid_seq'::regclass);


--
-- Name: gw_ext_role_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY gw_ext_role_attributes
    ADD CONSTRAINT gw_ext_role_attributes_pkey PRIMARY KEY (jbp_rid);


--
-- Name: jbp_context_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_context
    ADD CONSTRAINT jbp_context_pkey PRIMARY KEY (pk);


--
-- Name: jbp_instance_display_names_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance_display_names
    ADD CONSTRAINT jbp_instance_display_names_pkey PRIMARY KEY (instance_pk, locale);


--
-- Name: jbp_instance_id_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance
    ADD CONSTRAINT jbp_instance_id_key UNIQUE (id);


--
-- Name: jbp_instance_per_user_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance_per_user
    ADD CONSTRAINT jbp_instance_per_user_pkey PRIMARY KEY (pk);


--
-- Name: jbp_instance_per_user_user_id_portlet_ref_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance_per_user
    ADD CONSTRAINT jbp_instance_per_user_user_id_portlet_ref_key UNIQUE (user_id, portlet_ref);


--
-- Name: jbp_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance
    ADD CONSTRAINT jbp_instance_pkey PRIMARY KEY (pk);


--
-- Name: jbp_instance_security_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_instance_security
    ADD CONSTRAINT jbp_instance_security_pkey PRIMARY KEY (pk);


--
-- Name: jbp_nagvis_perm_membership_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_nagvis_perm_membership
    ADD CONSTRAINT jbp_nagvis_perm_membership_pkey PRIMARY KEY (jbp_rid, nv_pid);


--
-- Name: jbp_nagvis_perms_nv_mod_nv_act_nv_obj_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_nagvis_perms
    ADD CONSTRAINT jbp_nagvis_perms_nv_mod_nv_act_nv_obj_key UNIQUE (nv_mod, nv_act, nv_obj);


--
-- Name: jbp_nagvis_perms_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_nagvis_perms
    ADD CONSTRAINT jbp_nagvis_perms_pkey PRIMARY KEY (nv_pid);


--
-- Name: jbp_object_node_PATH_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_object_node
    ADD CONSTRAINT "jbp_object_node_PATH_key" UNIQUE ("PATH");


--
-- Name: jbp_object_node_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_object_node
    ADD CONSTRAINT jbp_object_node_pkey PRIMARY KEY (pk);


--
-- Name: jbp_object_node_sec_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_object_node_sec
    ADD CONSTRAINT jbp_object_node_sec_pkey PRIMARY KEY (pk);


--
-- Name: jbp_page_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_page
    ADD CONSTRAINT jbp_page_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portal_object_dnames_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portal_object_dnames
    ADD CONSTRAINT jbp_portal_object_dnames_pkey PRIMARY KEY (instance_pk, locale);


--
-- Name: jbp_portal_object_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portal_object
    ADD CONSTRAINT jbp_portal_object_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portal_object_props_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portal_object_props
    ADD CONSTRAINT jbp_portal_object_props_pkey PRIMARY KEY (object_key, name);


--
-- Name: jbp_portal_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portal
    ADD CONSTRAINT jbp_portal_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portlet_consumer_id_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_consumer
    ADD CONSTRAINT jbp_portlet_consumer_id_key UNIQUE (id);


--
-- Name: jbp_portlet_consumer_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_consumer
    ADD CONSTRAINT jbp_portlet_consumer_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portlet_group_id_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_group
    ADD CONSTRAINT jbp_portlet_group_id_key UNIQUE (id);


--
-- Name: jbp_portlet_group_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_group
    ADD CONSTRAINT jbp_portlet_group_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portlet_reg_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_reg
    ADD CONSTRAINT jbp_portlet_reg_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portlet_reg_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_reg_properties
    ADD CONSTRAINT jbp_portlet_reg_properties_pkey PRIMARY KEY (registration_pk, qname);


--
-- Name: jbp_portlet_state_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_state_entry
    ADD CONSTRAINT jbp_portlet_state_entry_pkey PRIMARY KEY (pk);


--
-- Name: jbp_portlet_state_entry_value_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_state_entry_value
    ADD CONSTRAINT jbp_portlet_state_entry_value_pkey PRIMARY KEY (pk, idx);


--
-- Name: jbp_portlet_state_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_portlet_state
    ADD CONSTRAINT jbp_portlet_state_pkey PRIMARY KEY (pk);


--
-- Name: jbp_role_membership_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_role_membership
    ADD CONSTRAINT jbp_role_membership_pkey PRIMARY KEY (jbp_uid, jbp_rid);


--
-- Name: jbp_roles_jbp_displayname_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_roles
    ADD CONSTRAINT jbp_roles_jbp_displayname_key UNIQUE (jbp_displayname);


--
-- Name: jbp_roles_jbp_name_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_roles
    ADD CONSTRAINT jbp_roles_jbp_name_key UNIQUE (jbp_name);


--
-- Name: jbp_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_roles
    ADD CONSTRAINT jbp_roles_pkey PRIMARY KEY (jbp_rid);


--
-- Name: jbp_user_prop_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_user_prop
    ADD CONSTRAINT jbp_user_prop_pkey PRIMARY KEY (jbp_uid, jbp_name);


--
-- Name: jbp_users_jbp_uname_key; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_users
    ADD CONSTRAINT jbp_users_jbp_uname_key UNIQUE (jbp_uname);


--
-- Name: jbp_users_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_users
    ADD CONSTRAINT jbp_users_pkey PRIMARY KEY (jbp_uid);


--
-- Name: jbp_window_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbp_window
    ADD CONSTRAINT jbp_window_pkey PRIMARY KEY (pk);


--
-- Name: jbpm_action_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT jbpm_action_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_bytearray_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_bytearray
    ADD CONSTRAINT jbpm_bytearray_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_byteblock_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_byteblock
    ADD CONSTRAINT jbpm_byteblock_pkey PRIMARY KEY (processfile_, index_);


--
-- Name: jbpm_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_comment
    ADD CONSTRAINT jbpm_comment_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_decisionconditions_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_decisionconditions
    ADD CONSTRAINT jbpm_decisionconditions_pkey PRIMARY KEY (decision_, index_);


--
-- Name: jbpm_delegation_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_delegation
    ADD CONSTRAINT jbpm_delegation_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_event_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_event
    ADD CONSTRAINT jbpm_event_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_exceptionhandler_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_exceptionhandler
    ADD CONSTRAINT jbpm_exceptionhandler_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_id_group_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_id_group
    ADD CONSTRAINT jbpm_id_group_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_id_membership_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_id_membership
    ADD CONSTRAINT jbpm_id_membership_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_id_user_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_id_user
    ADD CONSTRAINT jbpm_id_user_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_job_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT jbpm_job_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_log_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT jbpm_log_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_moduledefinition_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_moduledefinition
    ADD CONSTRAINT jbpm_moduledefinition_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_moduleinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_moduleinstance
    ADD CONSTRAINT jbpm_moduleinstance_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_node_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT jbpm_node_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_pooledactor_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_pooledactor
    ADD CONSTRAINT jbpm_pooledactor_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_processdefinition_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_processdefinition
    ADD CONSTRAINT jbpm_processdefinition_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_processinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_processinstance
    ADD CONSTRAINT jbpm_processinstance_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_runtimeaction_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_runtimeaction
    ADD CONSTRAINT jbpm_runtimeaction_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_swimlane_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_swimlane
    ADD CONSTRAINT jbpm_swimlane_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_swimlaneinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_swimlaneinstance
    ADD CONSTRAINT jbpm_swimlaneinstance_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_task_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT jbpm_task_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_taskactorpool_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_taskactorpool
    ADD CONSTRAINT jbpm_taskactorpool_pkey PRIMARY KEY (taskinstance_, pooledactor_);


--
-- Name: jbpm_taskcontroller_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_taskcontroller
    ADD CONSTRAINT jbpm_taskcontroller_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_taskinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT jbpm_taskinstance_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_token_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_token
    ADD CONSTRAINT jbpm_token_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_tokenvariablemap_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_tokenvariablemap
    ADD CONSTRAINT jbpm_tokenvariablemap_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_transition_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_transition
    ADD CONSTRAINT jbpm_transition_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_variableaccess_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_variableaccess
    ADD CONSTRAINT jbpm_variableaccess_pkey PRIMARY KEY (id_);


--
-- Name: jbpm_variableinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT jbpm_variableinstance_pkey PRIMARY KEY (id_);


--
-- Name: user_navigation_pkey; Type: CONSTRAINT; Schema: public; Owner: jboss; Tablespace: 
--

ALTER TABLE ONLY user_navigation
    ADD CONSTRAINT user_navigation_pkey PRIMARY KEY (id);


--
-- Name: idx_action_actndl; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_action_actndl ON jbpm_action USING btree (actiondelegation_);


--
-- Name: idx_action_event; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_action_event ON jbpm_action USING btree (event_);


--
-- Name: idx_action_procdf; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_action_procdf ON jbpm_action USING btree (processdefinition_);


--
-- Name: idx_comment_token; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_comment_token ON jbpm_comment USING btree (token_);


--
-- Name: idx_comment_tsk; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_comment_tsk ON jbpm_comment USING btree (taskinstance_);


--
-- Name: idx_deleg_prcd; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_deleg_prcd ON jbpm_delegation USING btree (processdefinition_);


--
-- Name: idx_job_prinst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_job_prinst ON jbpm_job USING btree (processinstance_);


--
-- Name: idx_job_token; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_job_token ON jbpm_job USING btree (token_);


--
-- Name: idx_job_tskinst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_job_tskinst ON jbpm_job USING btree (taskinstance_);


--
-- Name: idx_moddef_procdf; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_moddef_procdf ON jbpm_moduledefinition USING btree (processdefinition_);


--
-- Name: idx_modinst_prinst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_modinst_prinst ON jbpm_moduleinstance USING btree (processinstance_);


--
-- Name: idx_node_action; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_node_action ON jbpm_node USING btree (action_);


--
-- Name: idx_node_procdef; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_node_procdef ON jbpm_node USING btree (processdefinition_);


--
-- Name: idx_node_suprstate; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_node_suprstate ON jbpm_node USING btree (superstate_);


--
-- Name: idx_pldactr_actid; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_pldactr_actid ON jbpm_pooledactor USING btree (actorid_);


--
-- Name: idx_procdef_strtst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_procdef_strtst ON jbpm_processdefinition USING btree (startstate_);


--
-- Name: idx_procin_key; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_procin_key ON jbpm_processinstance USING btree (key_);


--
-- Name: idx_procin_procdef; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_procin_procdef ON jbpm_processinstance USING btree (processdefinition_);


--
-- Name: idx_procin_roottk; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_procin_roottk ON jbpm_processinstance USING btree (roottoken_);


--
-- Name: idx_procin_sproctk; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_procin_sproctk ON jbpm_processinstance USING btree (superprocesstoken_);


--
-- Name: idx_pstate_sbprcdef; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_pstate_sbprcdef ON jbpm_node USING btree (subprocessdefinition_);


--
-- Name: idx_rtactn_action; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_rtactn_action ON jbpm_runtimeaction USING btree (action_);


--
-- Name: idx_rtactn_prcinst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_rtactn_prcinst ON jbpm_runtimeaction USING btree (processinstance_);


--
-- Name: idx_swimlinst_sl; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_swimlinst_sl ON jbpm_swimlaneinstance USING btree (swimlane_);


--
-- Name: idx_task_actorid; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_task_actorid ON jbpm_taskinstance USING btree (actorid_);


--
-- Name: idx_task_procdef; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_task_procdef ON jbpm_task USING btree (processdefinition_);


--
-- Name: idx_task_taskmgtdf; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_task_taskmgtdf ON jbpm_task USING btree (taskmgmtdefinition_);


--
-- Name: idx_task_tsknode; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_task_tsknode ON jbpm_task USING btree (tasknode_);


--
-- Name: idx_taskinst_tokn; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_taskinst_tokn ON jbpm_taskinstance USING btree (token_);


--
-- Name: idx_taskinst_tsk; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_taskinst_tsk ON jbpm_taskinstance USING btree (task_, procinst_);


--
-- Name: idx_tkvarmap_ctxt; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_tkvarmap_ctxt ON jbpm_tokenvariablemap USING btree (contextinstance_);


--
-- Name: idx_tkvvarmp_token; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_tkvvarmp_token ON jbpm_tokenvariablemap USING btree (token_);


--
-- Name: idx_token_node; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_token_node ON jbpm_token USING btree (node_);


--
-- Name: idx_token_parent; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_token_parent ON jbpm_token USING btree (parent_);


--
-- Name: idx_token_procin; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_token_procin ON jbpm_token USING btree (processinstance_);


--
-- Name: idx_token_subpi; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_token_subpi ON jbpm_token USING btree (subprocessinstance_);


--
-- Name: idx_trans_procdef; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_trans_procdef ON jbpm_transition USING btree (processdefinition_);


--
-- Name: idx_transit_from; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_transit_from ON jbpm_transition USING btree (from_);


--
-- Name: idx_transit_to; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_transit_to ON jbpm_transition USING btree (to_);


--
-- Name: idx_tskinst_slinst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_tskinst_slinst ON jbpm_taskinstance USING btree (swimlaninstance_);


--
-- Name: idx_tskinst_swlane; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_tskinst_swlane ON jbpm_pooledactor USING btree (swimlaneinstance_);


--
-- Name: idx_tskinst_tminst; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_tskinst_tminst ON jbpm_taskinstance USING btree (taskmgmtinstance_);


--
-- Name: idx_varinst_prcins; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_varinst_prcins ON jbpm_variableinstance USING btree (processinstance_);


--
-- Name: idx_varinst_tk; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_varinst_tk ON jbpm_variableinstance USING btree (token_);


--
-- Name: idx_varinst_tkvarmp; Type: INDEX; Schema: public; Owner: jboss; Tablespace: 
--

CREATE INDEX idx_varinst_tkvarmp ON jbpm_variableinstance USING btree (tokenvariablemap_);


--
-- Name: fk271583e55a3b9242; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_state
    ADD CONSTRAINT fk271583e55a3b9242 FOREIGN KEY (registration_id) REFERENCES jbp_portlet_reg(pk);


--
-- Name: fk271583e59fdc8b8f; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_state
    ADD CONSTRAINT fk271583e59fdc8b8f FOREIGN KEY (parent_pk) REFERENCES jbp_portlet_state(pk);


--
-- Name: fk32b557981881a1ad; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal_object_dnames
    ADD CONSTRAINT fk32b557981881a1ad FOREIGN KEY (instance_pk) REFERENCES jbp_portal_object(pk);


--
-- Name: fk3856045695cb95c3; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_page
    ADD CONSTRAINT fk3856045695cb95c3 FOREIGN KEY (pk) REFERENCES jbp_portal_object(pk);


--
-- Name: fk454d40e8ddff4202; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_reg
    ADD CONSTRAINT fk454d40e8ddff4202 FOREIGN KEY (consumer_pk) REFERENCES jbp_portlet_consumer(pk);


--
-- Name: fk4a7fee6895cb95c3; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_context
    ADD CONSTRAINT fk4a7fee6895cb95c3 FOREIGN KEY (pk) REFERENCES jbp_portal_object(pk);


--
-- Name: fk7bb0d07395cb95c3; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal
    ADD CONSTRAINT fk7bb0d07395cb95c3 FOREIGN KEY (pk) REFERENCES jbp_portal_object(pk);


--
-- Name: fk874c23f795cb95c3; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_window
    ADD CONSTRAINT fk874c23f795cb95c3 FOREIGN KEY (pk) REFERENCES jbp_portal_object(pk);


--
-- Name: fk8b5da5e188e068d0; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_instance_security_actions
    ADD CONSTRAINT fk8b5da5e188e068d0 FOREIGN KEY (pk) REFERENCES jbp_instance_security(pk);


--
-- Name: fk8ba7b7787e826f1; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_state_entry
    ADD CONSTRAINT fk8ba7b7787e826f1 FOREIGN KEY (entry_key) REFERENCES jbp_portlet_state(pk);


--
-- Name: fk9349b3d0e7e819e7; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_instance_per_user
    ADD CONSTRAINT fk9349b3d0e7e819e7 FOREIGN KEY (instance_pk) REFERENCES jbp_instance(pk);


--
-- Name: fk93cc461066f4da65; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_user_prop
    ADD CONSTRAINT fk93cc461066f4da65 FOREIGN KEY (jbp_uid) REFERENCES jbp_users(jbp_uid);


--
-- Name: fk9cbbd94dd209e280; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_object_node_sec
    ADD CONSTRAINT fk9cbbd94dd209e280 FOREIGN KEY (node_key) REFERENCES jbp_object_node(pk);


--
-- Name: fk_action_actndel; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_action_actndel FOREIGN KEY (actiondelegation_) REFERENCES jbpm_delegation(id_);


--
-- Name: fk_action_event; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_action_event FOREIGN KEY (event_) REFERENCES jbpm_event(id_);


--
-- Name: fk_action_expthdl; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_action_expthdl FOREIGN KEY (exceptionhandler_) REFERENCES jbpm_exceptionhandler(id_);


--
-- Name: fk_action_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_action_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_action_refact; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_action_refact FOREIGN KEY (referencedaction_) REFERENCES jbpm_action(id_);


--
-- Name: fk_bytearr_fildef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_bytearray
    ADD CONSTRAINT fk_bytearr_fildef FOREIGN KEY (filedefinition_) REFERENCES jbpm_moduledefinition(id_);


--
-- Name: fk_byteblock_file; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_byteblock
    ADD CONSTRAINT fk_byteblock_file FOREIGN KEY (processfile_) REFERENCES jbpm_bytearray(id_);


--
-- Name: fk_byteinst_array; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT fk_byteinst_array FOREIGN KEY (bytearrayvalue_) REFERENCES jbpm_bytearray(id_);


--
-- Name: fk_comment_token; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_comment
    ADD CONSTRAINT fk_comment_token FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_comment_tsk; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_comment
    ADD CONSTRAINT fk_comment_tsk FOREIGN KEY (taskinstance_) REFERENCES jbpm_taskinstance(id_);


--
-- Name: fk_crtetimeract_ta; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_action
    ADD CONSTRAINT fk_crtetimeract_ta FOREIGN KEY (timeraction_) REFERENCES jbpm_action(id_);


--
-- Name: fk_deccond_dec; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_decisionconditions
    ADD CONSTRAINT fk_deccond_dec FOREIGN KEY (decision_) REFERENCES jbpm_node(id_);


--
-- Name: fk_decision_deleg; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_decision_deleg FOREIGN KEY (decisiondelegation) REFERENCES jbpm_delegation(id_);


--
-- Name: fk_delegation_prcd; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_delegation
    ADD CONSTRAINT fk_delegation_prcd FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_event_node; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_event
    ADD CONSTRAINT fk_event_node FOREIGN KEY (node_) REFERENCES jbpm_node(id_);


--
-- Name: fk_event_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_event
    ADD CONSTRAINT fk_event_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_event_task; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_event
    ADD CONSTRAINT fk_event_task FOREIGN KEY (task_) REFERENCES jbpm_task(id_);


--
-- Name: fk_event_trans; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_event
    ADD CONSTRAINT fk_event_trans FOREIGN KEY (transition_) REFERENCES jbpm_transition(id_);


--
-- Name: fk_id_grp_parent; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_id_group
    ADD CONSTRAINT fk_id_grp_parent FOREIGN KEY (parent_) REFERENCES jbpm_id_group(id_);


--
-- Name: fk_id_memship_grp; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_id_membership
    ADD CONSTRAINT fk_id_memship_grp FOREIGN KEY (group_) REFERENCES jbpm_id_group(id_);


--
-- Name: fk_id_memship_usr; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_id_membership
    ADD CONSTRAINT fk_id_memship_usr FOREIGN KEY (user_) REFERENCES jbpm_id_user(id_);


--
-- Name: fk_job_action; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT fk_job_action FOREIGN KEY (action_) REFERENCES jbpm_action(id_);


--
-- Name: fk_job_node; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT fk_job_node FOREIGN KEY (node_) REFERENCES jbpm_node(id_);


--
-- Name: fk_job_prinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT fk_job_prinst FOREIGN KEY (processinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_job_token; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT fk_job_token FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_job_tskinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_job
    ADD CONSTRAINT fk_job_tskinst FOREIGN KEY (taskinstance_) REFERENCES jbpm_taskinstance(id_);


--
-- Name: fk_log_action; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_action FOREIGN KEY (action_) REFERENCES jbpm_action(id_);


--
-- Name: fk_log_childtoken; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_childtoken FOREIGN KEY (child_) REFERENCES jbpm_token(id_);


--
-- Name: fk_log_destnode; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_destnode FOREIGN KEY (destinationnode_) REFERENCES jbpm_node(id_);


--
-- Name: fk_log_newbytes; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_newbytes FOREIGN KEY (newbytearray_) REFERENCES jbpm_bytearray(id_);


--
-- Name: fk_log_node; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_node FOREIGN KEY (node_) REFERENCES jbpm_node(id_);


--
-- Name: fk_log_oldbytes; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_oldbytes FOREIGN KEY (oldbytearray_) REFERENCES jbpm_bytearray(id_);


--
-- Name: fk_log_parent; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_parent FOREIGN KEY (parent_) REFERENCES jbpm_log(id_);


--
-- Name: fk_log_sourcenode; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_sourcenode FOREIGN KEY (sourcenode_) REFERENCES jbpm_node(id_);


--
-- Name: fk_log_swiminst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_swiminst FOREIGN KEY (swimlaneinstance_) REFERENCES jbpm_swimlaneinstance(id_);


--
-- Name: fk_log_taskinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_taskinst FOREIGN KEY (taskinstance_) REFERENCES jbpm_taskinstance(id_);


--
-- Name: fk_log_token; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_token FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_log_transition; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_transition FOREIGN KEY (transition_) REFERENCES jbpm_transition(id_);


--
-- Name: fk_log_varinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_log
    ADD CONSTRAINT fk_log_varinst FOREIGN KEY (variableinstance_) REFERENCES jbpm_variableinstance(id_);


--
-- Name: fk_moddef_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_moduledefinition
    ADD CONSTRAINT fk_moddef_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_modinst_prcinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_moduleinstance
    ADD CONSTRAINT fk_modinst_prcinst FOREIGN KEY (processinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_node_action; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_node_action FOREIGN KEY (action_) REFERENCES jbpm_action(id_);


--
-- Name: fk_node_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_node_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_node_script; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_node_script FOREIGN KEY (script_) REFERENCES jbpm_action(id_);


--
-- Name: fk_node_superstate; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_node_superstate FOREIGN KEY (superstate_) REFERENCES jbpm_node(id_);


--
-- Name: fk_pooledactor_sli; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_pooledactor
    ADD CONSTRAINT fk_pooledactor_sli FOREIGN KEY (swimlaneinstance_) REFERENCES jbpm_swimlaneinstance(id_);


--
-- Name: fk_procdef_strtsta; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_processdefinition
    ADD CONSTRAINT fk_procdef_strtsta FOREIGN KEY (startstate_) REFERENCES jbpm_node(id_);


--
-- Name: fk_procin_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_processinstance
    ADD CONSTRAINT fk_procin_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_procin_roottkn; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_processinstance
    ADD CONSTRAINT fk_procin_roottkn FOREIGN KEY (roottoken_) REFERENCES jbpm_token(id_);


--
-- Name: fk_procin_sproctkn; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_processinstance
    ADD CONSTRAINT fk_procin_sproctkn FOREIGN KEY (superprocesstoken_) REFERENCES jbpm_token(id_);


--
-- Name: fk_procst_sbprcdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_node
    ADD CONSTRAINT fk_procst_sbprcdef FOREIGN KEY (subprocessdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_rtactn_action; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_runtimeaction
    ADD CONSTRAINT fk_rtactn_action FOREIGN KEY (action_) REFERENCES jbpm_action(id_);


--
-- Name: fk_rtactn_procinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_runtimeaction
    ADD CONSTRAINT fk_rtactn_procinst FOREIGN KEY (processinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_swimlaneinst_sl; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_swimlaneinstance
    ADD CONSTRAINT fk_swimlaneinst_sl FOREIGN KEY (swimlane_) REFERENCES jbpm_swimlane(id_);


--
-- Name: fk_swimlaneinst_tm; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_swimlaneinstance
    ADD CONSTRAINT fk_swimlaneinst_tm FOREIGN KEY (taskmgmtinstance_) REFERENCES jbpm_moduleinstance(id_);


--
-- Name: fk_swl_assdel; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_swimlane
    ADD CONSTRAINT fk_swl_assdel FOREIGN KEY (assignmentdelegation_) REFERENCES jbpm_delegation(id_);


--
-- Name: fk_swl_tskmgmtdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_swimlane
    ADD CONSTRAINT fk_swl_tskmgmtdef FOREIGN KEY (taskmgmtdefinition_) REFERENCES jbpm_moduledefinition(id_);


--
-- Name: fk_task_assdel; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_assdel FOREIGN KEY (assignmentdelegation_) REFERENCES jbpm_delegation(id_);


--
-- Name: fk_task_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_task_startst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_startst FOREIGN KEY (startstate_) REFERENCES jbpm_node(id_);


--
-- Name: fk_task_swimlane; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_swimlane FOREIGN KEY (swimlane_) REFERENCES jbpm_swimlane(id_);


--
-- Name: fk_task_taskmgtdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_taskmgtdef FOREIGN KEY (taskmgmtdefinition_) REFERENCES jbpm_moduledefinition(id_);


--
-- Name: fk_task_tasknode; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_task_tasknode FOREIGN KEY (tasknode_) REFERENCES jbpm_node(id_);


--
-- Name: fk_taskactpl_tski; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskactorpool
    ADD CONSTRAINT fk_taskactpl_tski FOREIGN KEY (taskinstance_) REFERENCES jbpm_taskinstance(id_);


--
-- Name: fk_taskinst_slinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT fk_taskinst_slinst FOREIGN KEY (swimlaninstance_) REFERENCES jbpm_swimlaneinstance(id_);


--
-- Name: fk_taskinst_task; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT fk_taskinst_task FOREIGN KEY (task_) REFERENCES jbpm_task(id_);


--
-- Name: fk_taskinst_tminst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT fk_taskinst_tminst FOREIGN KEY (taskmgmtinstance_) REFERENCES jbpm_moduleinstance(id_);


--
-- Name: fk_taskinst_token; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT fk_taskinst_token FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_taskmgtinst_tmd; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_moduleinstance
    ADD CONSTRAINT fk_taskmgtinst_tmd FOREIGN KEY (taskmgmtdefinition_) REFERENCES jbpm_moduledefinition(id_);


--
-- Name: fk_tkvarmap_ctxt; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_tokenvariablemap
    ADD CONSTRAINT fk_tkvarmap_ctxt FOREIGN KEY (contextinstance_) REFERENCES jbpm_moduleinstance(id_);


--
-- Name: fk_tkvarmap_token; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_tokenvariablemap
    ADD CONSTRAINT fk_tkvarmap_token FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_token_node; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_token
    ADD CONSTRAINT fk_token_node FOREIGN KEY (node_) REFERENCES jbpm_node(id_);


--
-- Name: fk_token_parent; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_token
    ADD CONSTRAINT fk_token_parent FOREIGN KEY (parent_) REFERENCES jbpm_token(id_);


--
-- Name: fk_token_procinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_token
    ADD CONSTRAINT fk_token_procinst FOREIGN KEY (processinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_token_subpi; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_token
    ADD CONSTRAINT fk_token_subpi FOREIGN KEY (subprocessinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_trans_procdef; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_transition
    ADD CONSTRAINT fk_trans_procdef FOREIGN KEY (processdefinition_) REFERENCES jbpm_processdefinition(id_);


--
-- Name: fk_transition_from; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_transition
    ADD CONSTRAINT fk_transition_from FOREIGN KEY (from_) REFERENCES jbpm_node(id_);


--
-- Name: fk_transition_to; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_transition
    ADD CONSTRAINT fk_transition_to FOREIGN KEY (to_) REFERENCES jbpm_node(id_);


--
-- Name: fk_tsk_tskctrl; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_task
    ADD CONSTRAINT fk_tsk_tskctrl FOREIGN KEY (taskcontroller_) REFERENCES jbpm_taskcontroller(id_);


--
-- Name: fk_tskactpol_plact; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskactorpool
    ADD CONSTRAINT fk_tskactpol_plact FOREIGN KEY (pooledactor_) REFERENCES jbpm_pooledactor(id_);


--
-- Name: fk_tskctrl_deleg; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskcontroller
    ADD CONSTRAINT fk_tskctrl_deleg FOREIGN KEY (taskcontrollerdelegation_) REFERENCES jbpm_delegation(id_);


--
-- Name: fk_tskdef_start; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_moduledefinition
    ADD CONSTRAINT fk_tskdef_start FOREIGN KEY (starttask_) REFERENCES jbpm_task(id_);


--
-- Name: fk_tskins_prcins; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_taskinstance
    ADD CONSTRAINT fk_tskins_prcins FOREIGN KEY (procinst_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_var_tskinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT fk_var_tskinst FOREIGN KEY (taskinstance_) REFERENCES jbpm_taskinstance(id_);


--
-- Name: fk_varacc_procst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableaccess
    ADD CONSTRAINT fk_varacc_procst FOREIGN KEY (processstate_) REFERENCES jbpm_node(id_);


--
-- Name: fk_varacc_script; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableaccess
    ADD CONSTRAINT fk_varacc_script FOREIGN KEY (script_) REFERENCES jbpm_action(id_);


--
-- Name: fk_varacc_tskctrl; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableaccess
    ADD CONSTRAINT fk_varacc_tskctrl FOREIGN KEY (taskcontroller_) REFERENCES jbpm_taskcontroller(id_);


--
-- Name: fk_varinst_prcinst; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT fk_varinst_prcinst FOREIGN KEY (processinstance_) REFERENCES jbpm_processinstance(id_);


--
-- Name: fk_varinst_tk; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT fk_varinst_tk FOREIGN KEY (token_) REFERENCES jbpm_token(id_);


--
-- Name: fk_varinst_tkvarmp; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbpm_variableinstance
    ADD CONSTRAINT fk_varinst_tkvarmp FOREIGN KEY (tokenvariablemap_) REFERENCES jbpm_tokenvariablemap(id_);


--
-- Name: fkb0c61d43e7e819e7; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_instance_security
    ADD CONSTRAINT fkb0c61d43e7e819e7 FOREIGN KEY (instance_pk) REFERENCES jbp_instance(pk);


--
-- Name: fkb7fb4c9cd5674127; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal_object_props
    ADD CONSTRAINT fkb7fb4c9cd5674127 FOREIGN KEY (object_key) REFERENCES jbp_portal_object(pk);


--
-- Name: fkbe83b7ebf2685fb6; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_object_node_sec_actions
    ADD CONSTRAINT fkbe83b7ebf2685fb6 FOREIGN KEY (pk) REFERENCES jbp_object_node_sec(pk);


--
-- Name: fkbed69ef5f8180a4; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal_mode
    ADD CONSTRAINT fkbed69ef5f8180a4 FOREIGN KEY (pk) REFERENCES jbp_portal(pk);


--
-- Name: fkc21677ae5f8180a4; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal_window_state
    ADD CONSTRAINT fkc21677ae5f8180a4 FOREIGN KEY (pk) REFERENCES jbp_portal(pk);


--
-- Name: fkc8efec8b8f1445d9; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portal_object
    ADD CONSTRAINT fkc8efec8b8f1445d9 FOREIGN KEY (pk) REFERENCES jbp_object_node(pk);


--
-- Name: fkce6c8f5b8083a928; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_object_node
    ADD CONSTRAINT fkce6c8f5b8083a928 FOREIGN KEY (parent_key) REFERENCES jbp_object_node(pk);


--
-- Name: fkd0571802b257e1bc; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_consumer
    ADD CONSTRAINT fkd0571802b257e1bc FOREIGN KEY (group_pk) REFERENCES jbp_portlet_group(pk);


--
-- Name: fkd64ff928e7e819e7; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_instance_display_names
    ADD CONSTRAINT fkd64ff928e7e819e7 FOREIGN KEY (instance_pk) REFERENCES jbp_instance(pk);


--
-- Name: fke17e6cea5a3b9322; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_reg_properties
    ADD CONSTRAINT fke17e6cea5a3b9322 FOREIGN KEY (registration_pk) REFERENCES jbp_portlet_reg(pk);


--
-- Name: fkf410173866f3164d; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_role_membership
    ADD CONSTRAINT fkf410173866f3164d FOREIGN KEY (jbp_rid) REFERENCES jbp_roles(jbp_rid);


--
-- Name: fkf410173866f4da65; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_role_membership
    ADD CONSTRAINT fkf410173866f4da65 FOREIGN KEY (jbp_uid) REFERENCES jbp_users(jbp_uid);


--
-- Name: fkf9a539ca28d29fce; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_portlet_state_entry_value
    ADD CONSTRAINT fkf9a539ca28d29fce FOREIGN KEY (pk) REFERENCES jbp_portlet_state_entry(pk);


--
-- Name: perm; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_nagvis_perm_membership
    ADD CONSTRAINT perm FOREIGN KEY (nv_pid) REFERENCES jbp_nagvis_perms(nv_pid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: role; Type: FK CONSTRAINT; Schema: public; Owner: jboss
--

ALTER TABLE ONLY jbp_nagvis_perm_membership
    ADD CONSTRAINT role FOREIGN KEY (jbp_rid) REFERENCES jbp_roles(jbp_rid) ON UPDATE CASCADE ON DELETE CASCADE;

--Now reset the following jbossportal sequences manually as they are not associated by the hibernate while creation. In reality, autoincrement values
-- are in 100s. It is safe to bump the sequences to 10001

SELECT setval('portal_seq', 10001);
SELECT setval('portlet_seq', 10001);
SELECT setval('sec_seq', 10001);
SELECT setval('user_seq', 10001);
SELECT setval('nav_seq', 10001);
SELECT setval('instance_seq', 10001);
    
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

