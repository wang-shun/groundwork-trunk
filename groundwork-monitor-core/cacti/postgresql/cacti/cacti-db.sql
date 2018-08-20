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
-- Name: cdef; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE cdef (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.cdef OWNER TO cactiuser;

--
-- Name: cdef_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE cdef_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cdef_id_seq OWNER TO cactiuser;

--
-- Name: cdef_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE cdef_id_seq OWNED BY cdef.id;


--
-- Name: cdef_items; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE cdef_items (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    cdef_id integer DEFAULT 0 NOT NULL,
    sequence integer DEFAULT 0 NOT NULL,
    type smallint DEFAULT 0 NOT NULL,
    value character varying(150) NOT NULL
);


ALTER TABLE public.cdef_items OWNER TO cactiuser;

--
-- Name: cdef_items_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE cdef_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cdef_items_id_seq OWNER TO cactiuser;

--
-- Name: cdef_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE cdef_items_id_seq OWNED BY cdef_items.id;


--
-- Name: colors; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE colors (
    id integer NOT NULL,
    hex character varying(6) NOT NULL
);


ALTER TABLE public.colors OWNER TO cactiuser;

--
-- Name: colors_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE colors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.colors_id_seq OWNER TO cactiuser;

--
-- Name: colors_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE colors_id_seq OWNED BY colors.id;


--
-- Name: data_input; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_input (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(200) NOT NULL,
    input_string character varying(255),
    type_id smallint DEFAULT 0 NOT NULL
);


ALTER TABLE public.data_input OWNER TO cactiuser;

--
-- Name: data_input_data; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_input_data (
    data_input_field_id integer DEFAULT 0 NOT NULL,
    data_template_data_id integer DEFAULT 0 NOT NULL,
    t_value character varying(2),
    value text
);


ALTER TABLE public.data_input_data OWNER TO cactiuser;

--
-- Name: data_input_fields; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_input_fields (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    data_input_id integer DEFAULT 0 NOT NULL,
    name character varying(200) NOT NULL,
    data_name character varying(50) NOT NULL,
    input_output character varying(3) NOT NULL,
    update_rra character varying(2) DEFAULT '0'::character varying,
    sequence smallint DEFAULT 0 NOT NULL,
    type_code character varying(40),
    regexp_match character varying(200),
    allow_nulls character varying(2)
);


ALTER TABLE public.data_input_fields OWNER TO cactiuser;

--
-- Name: data_input_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_input_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_input_fields_id_seq OWNER TO cactiuser;

--
-- Name: data_input_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_input_fields_id_seq OWNED BY data_input_fields.id;


--
-- Name: data_input_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_input_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_input_id_seq OWNER TO cactiuser;

--
-- Name: data_input_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_input_id_seq OWNED BY data_input.id;


--
-- Name: data_local; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_local (
    id integer NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    host_id integer DEFAULT 0 NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL,
    snmp_index character varying(255) NOT NULL
);


ALTER TABLE public.data_local OWNER TO cactiuser;

--
-- Name: data_local_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_local_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_local_id_seq OWNER TO cactiuser;

--
-- Name: data_local_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_local_id_seq OWNED BY data_local.id;


--
-- Name: data_template; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_template (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(150) NOT NULL
);


ALTER TABLE public.data_template OWNER TO cactiuser;

--
-- Name: data_template_data; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_template_data (
    id integer NOT NULL,
    local_data_template_data_id integer DEFAULT 0 NOT NULL,
    local_data_id integer DEFAULT 0 NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    data_input_id integer DEFAULT 0 NOT NULL,
    t_name character varying(2),
    name character varying(250) NOT NULL,
    name_cache character varying(255) NOT NULL,
    data_source_path character varying(255),
    t_active character varying(2),
    active character varying(2),
    t_rrd_step character varying(2),
    rrd_step integer DEFAULT 0 NOT NULL,
    t_rra_id character varying(2)
);


ALTER TABLE public.data_template_data OWNER TO cactiuser;

--
-- Name: data_template_data_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_template_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_template_data_id_seq OWNER TO cactiuser;

--
-- Name: data_template_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_template_data_id_seq OWNED BY data_template_data.id;


--
-- Name: data_template_data_rra; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_template_data_rra (
    data_template_data_id integer DEFAULT 0 NOT NULL,
    rra_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.data_template_data_rra OWNER TO cactiuser;

--
-- Name: data_template_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_template_id_seq OWNER TO cactiuser;

--
-- Name: data_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_template_id_seq OWNED BY data_template.id;


--
-- Name: data_template_rrd; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE data_template_rrd (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    local_data_template_rrd_id integer DEFAULT 0 NOT NULL,
    local_data_id integer DEFAULT 0 NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    t_rrd_maximum character varying(2),
    rrd_maximum character varying(20) DEFAULT '0'::character varying NOT NULL,
    t_rrd_minimum character varying(2),
    rrd_minimum character varying(20) DEFAULT '0'::character varying NOT NULL,
    t_rrd_heartbeat character varying(2),
    rrd_heartbeat integer DEFAULT 0 NOT NULL,
    t_data_source_type_id character varying(2),
    data_source_type_id smallint DEFAULT 0 NOT NULL,
    t_data_source_name character varying(2),
    data_source_name character varying(19) NOT NULL,
    t_data_input_field_id character varying(2),
    data_input_field_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.data_template_rrd OWNER TO cactiuser;

--
-- Name: data_template_rrd_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE data_template_rrd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_template_rrd_id_seq OWNER TO cactiuser;

--
-- Name: data_template_rrd_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE data_template_rrd_id_seq OWNED BY data_template_rrd.id;


--
-- Name: graph_local; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_local (
    id integer NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL,
    host_id integer DEFAULT 0 NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL,
    snmp_index character varying(255) NOT NULL
);


ALTER TABLE public.graph_local OWNER TO cactiuser;

--
-- Name: graph_local_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_local_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_local_id_seq OWNER TO cactiuser;

--
-- Name: graph_local_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_local_id_seq OWNED BY graph_local.id;


--
-- Name: graph_template_input; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_template_input (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    column_name character varying(50) NOT NULL
);


ALTER TABLE public.graph_template_input OWNER TO cactiuser;

--
-- Name: graph_template_input_defs; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_template_input_defs (
    graph_template_input_id integer DEFAULT 0 NOT NULL,
    graph_template_item_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.graph_template_input_defs OWNER TO cactiuser;

--
-- Name: graph_template_input_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_template_input_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_template_input_id_seq OWNER TO cactiuser;

--
-- Name: graph_template_input_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_template_input_id_seq OWNED BY graph_template_input.id;


--
-- Name: graph_templates; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_templates (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.graph_templates OWNER TO cactiuser;

--
-- Name: graph_templates_gprint; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_templates_gprint (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(100) NOT NULL,
    gprint_text character varying(255)
);


ALTER TABLE public.graph_templates_gprint OWNER TO cactiuser;

--
-- Name: graph_templates_gprint_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_templates_gprint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_templates_gprint_id_seq OWNER TO cactiuser;

--
-- Name: graph_templates_gprint_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_templates_gprint_id_seq OWNED BY graph_templates_gprint.id;


--
-- Name: graph_templates_graph; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_templates_graph (
    id integer NOT NULL,
    local_graph_template_graph_id integer DEFAULT 0 NOT NULL,
    local_graph_id integer DEFAULT 0 NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL,
    t_image_format_id character varying(2) DEFAULT '0'::character varying,
    image_format_id smallint DEFAULT 0 NOT NULL,
    t_title character varying(2) DEFAULT '0'::character varying,
    title character varying(255) NOT NULL,
    title_cache character varying(255) NOT NULL,
    t_height character varying(2) DEFAULT '0'::character varying,
    height integer DEFAULT 0 NOT NULL,
    t_width character varying(2) DEFAULT '0'::character varying,
    width integer DEFAULT 0 NOT NULL,
    t_upper_limit character varying(2) DEFAULT '0'::character varying,
    upper_limit character varying(20) DEFAULT '0'::character varying NOT NULL,
    t_lower_limit character varying(2) DEFAULT '0'::character varying,
    lower_limit character varying(20) DEFAULT '0'::character varying NOT NULL,
    t_vertical_label character varying(2) DEFAULT '0'::character varying,
    vertical_label character varying(200),
    t_slope_mode character varying(2) DEFAULT '0'::character varying,
    slope_mode character varying(2) DEFAULT 'on'::character varying,
    t_auto_scale character varying(2) DEFAULT '0'::character varying,
    auto_scale character varying(2),
    t_auto_scale_opts character varying(2) DEFAULT '0'::character varying,
    auto_scale_opts smallint DEFAULT 0 NOT NULL,
    t_auto_scale_log character varying(2) DEFAULT '0'::character varying,
    auto_scale_log character varying(2),
    t_scale_log_units character varying(2) DEFAULT '0'::character varying,
    scale_log_units character varying(2),
    t_auto_scale_rigid character varying(2) DEFAULT '0'::character varying,
    auto_scale_rigid character varying(2),
    t_auto_padding character varying(2) DEFAULT '0'::character varying,
    auto_padding character varying(2),
    t_base_value character varying(2) DEFAULT '0'::character varying,
    base_value integer DEFAULT 0 NOT NULL,
    t_grouping character varying(2) DEFAULT '0'::character varying,
    grouping character varying(2) NOT NULL,
    t_export character varying(2) DEFAULT '0'::character varying,
    export character varying(2),
    t_unit_value character varying(2) DEFAULT '0'::character varying,
    unit_value character varying(20),
    t_unit_exponent_value character varying(2) DEFAULT '0'::character varying,
    unit_exponent_value character varying(5) NOT NULL
);


ALTER TABLE public.graph_templates_graph OWNER TO cactiuser;

--
-- Name: graph_templates_graph_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_templates_graph_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_templates_graph_id_seq OWNER TO cactiuser;

--
-- Name: graph_templates_graph_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_templates_graph_id_seq OWNED BY graph_templates_graph.id;


--
-- Name: graph_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_templates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_templates_id_seq OWNER TO cactiuser;

--
-- Name: graph_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_templates_id_seq OWNED BY graph_templates.id;


--
-- Name: graph_templates_item; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_templates_item (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    local_graph_template_item_id integer DEFAULT 0 NOT NULL,
    local_graph_id integer DEFAULT 0 NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL,
    task_item_id integer DEFAULT 0 NOT NULL,
    color_id integer DEFAULT 0 NOT NULL,
    alpha character varying(2) DEFAULT 'FF'::character varying,
    graph_type_id smallint DEFAULT 0 NOT NULL,
    cdef_id integer DEFAULT 0 NOT NULL,
    consolidation_function_id smallint DEFAULT 0 NOT NULL,
    text_format character varying(255),
    value character varying(255),
    hard_return character varying(2),
    gprint_id integer DEFAULT 0 NOT NULL,
    sequence integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.graph_templates_item OWNER TO cactiuser;

--
-- Name: graph_templates_item_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_templates_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_templates_item_id_seq OWNER TO cactiuser;

--
-- Name: graph_templates_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_templates_item_id_seq OWNED BY graph_templates_item.id;


--
-- Name: graph_tree; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_tree (
    id integer NOT NULL,
    sort_type smallint DEFAULT 1 NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.graph_tree OWNER TO cactiuser;

--
-- Name: graph_tree_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_tree_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_tree_id_seq OWNER TO cactiuser;

--
-- Name: graph_tree_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_tree_id_seq OWNED BY graph_tree.id;


--
-- Name: graph_tree_items; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE graph_tree_items (
    id integer NOT NULL,
    graph_tree_id integer DEFAULT 0 NOT NULL,
    local_graph_id integer DEFAULT 0 NOT NULL,
    rra_id integer DEFAULT 0 NOT NULL,
    title character varying(255),
    host_id integer DEFAULT 0 NOT NULL,
    order_key character varying(100) DEFAULT '0'::character varying NOT NULL,
    host_grouping_type smallint DEFAULT 1 NOT NULL,
    sort_children_type smallint DEFAULT 1 NOT NULL
);


ALTER TABLE public.graph_tree_items OWNER TO cactiuser;

--
-- Name: graph_tree_items_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE graph_tree_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.graph_tree_items_id_seq OWNER TO cactiuser;

--
-- Name: graph_tree_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE graph_tree_items_id_seq OWNED BY graph_tree_items.id;


--
-- Name: host; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host (
    id integer NOT NULL,
    host_template_id integer DEFAULT 0 NOT NULL,
    description character varying(150) NOT NULL,
    hostname character varying(250),
    notes text,
    snmp_community character varying(100),
    snmp_version smallint DEFAULT 1 NOT NULL,
    snmp_username character varying(50),
    snmp_password character varying(50),
    snmp_auth_protocol character varying(5),
    snmp_priv_passphrase character varying(200),
    snmp_priv_protocol character varying(6),
    snmp_context character varying(64),
    snmp_port integer DEFAULT 161 NOT NULL,
    snmp_timeout integer DEFAULT 500 NOT NULL,
    availability_method integer DEFAULT 1 NOT NULL,
    ping_method integer DEFAULT 0,
    ping_port integer DEFAULT 0,
    ping_timeout integer DEFAULT 500,
    ping_retries integer DEFAULT 2,
    max_oids integer DEFAULT 10,
    disabled character varying(2),
    status smallint DEFAULT 0 NOT NULL,
    status_event_count integer DEFAULT 0 NOT NULL,
    status_fail_date timestamp without time zone default 'epoch' NOT NULL,
    status_rec_date timestamp without time zone default 'epoch' NOT NULL,
    status_last_error character varying(255),
    min_time numeric(10,5) DEFAULT 9.99999::numeric,
    max_time numeric(10,5) DEFAULT 0,
    cur_time numeric(10,5) DEFAULT 0,
    avg_time numeric(10,5) DEFAULT 0,
    total_polls integer DEFAULT 0,
    failed_polls integer DEFAULT 0,
    availability numeric(8,5) DEFAULT 100::numeric NOT NULL
);


ALTER TABLE public.host OWNER TO cactiuser;

--
-- Name: host_graph; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_graph (
    host_id integer DEFAULT 0 NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.host_graph OWNER TO cactiuser;

--
-- Name: host_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE host_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.host_id_seq OWNER TO cactiuser;

--
-- Name: host_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE host_id_seq OWNED BY host.id;


--
-- Name: host_snmp_cache; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_snmp_cache (
    host_id integer DEFAULT 0 NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL,
    field_name character varying(50) NOT NULL,
    field_value character varying(255),
    snmp_index character varying(255) NOT NULL,
    oid text NOT NULL
);


ALTER TABLE public.host_snmp_cache OWNER TO cactiuser;

--
-- Name: host_snmp_query; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_snmp_query (
    host_id integer DEFAULT 0 NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL,
    sort_field character varying(50) NOT NULL,
    title_format character varying(50) NOT NULL,
    reindex_method smallint DEFAULT 0 NOT NULL
);


ALTER TABLE public.host_snmp_query OWNER TO cactiuser;

--
-- Name: host_template; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_template (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.host_template OWNER TO cactiuser;

--
-- Name: host_template_graph; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_template_graph (
    host_template_id integer DEFAULT 0 NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.host_template_graph OWNER TO cactiuser;

--
-- Name: host_template_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE host_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.host_template_id_seq OWNER TO cactiuser;

--
-- Name: host_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE host_template_id_seq OWNED BY host_template.id;


--
-- Name: host_template_snmp_query; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE host_template_snmp_query (
    host_template_id integer DEFAULT 0 NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.host_template_snmp_query OWNER TO cactiuser;

--
-- Name: plugin_config; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_config (
    id integer NOT NULL,
    directory character varying(32) NOT NULL,
    name character varying(64) NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    author character varying(64) NOT NULL,
    webpage character varying(255) NOT NULL,
    version character varying(8) NOT NULL
);


ALTER TABLE public.plugin_config OWNER TO cactiuser;

--
-- Name: plugin_config_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_config_id_seq OWNER TO cactiuser;

--
-- Name: plugin_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_config_id_seq OWNED BY plugin_config.id;


--
-- Name: plugin_db_changes; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_db_changes (
    id integer NOT NULL,
    plugin character varying(16) NOT NULL,
    "table" character varying(64) NOT NULL,
    "column" character varying(64) NOT NULL,
    method character varying(16) NOT NULL
);


ALTER TABLE public.plugin_db_changes OWNER TO cactiuser;

--
-- Name: plugin_db_changes_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_db_changes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_db_changes_id_seq OWNER TO cactiuser;

--
-- Name: plugin_db_changes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_db_changes_id_seq OWNED BY plugin_db_changes.id;


--
-- Name: plugin_discover_hosts; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_discover_hosts (
    hostname character varying(100) DEFAULT ''::character varying NOT NULL,
    ip character varying(17) DEFAULT ''::character varying NOT NULL,
    hash character varying(12) DEFAULT ''::character varying NOT NULL,
    community character varying(100) DEFAULT ''::character varying NOT NULL,
    snmp_version character varying(3) DEFAULT ''::character varying NOT NULL,
    snmp_username character varying(64) DEFAULT ''::character varying NOT NULL,
    snmp_password character varying(256) DEFAULT ''::character varying NOT NULL,
    snmp_auth_protocol character varying(6) DEFAULT ''::character varying NOT NULL,
    snmp_priv_passphrase character varying(256) DEFAULT ''::character varying NOT NULL,
    snmp_priv_protocol character varying(12) DEFAULT ''::character varying NOT NULL,
    snmp_context character varying(256) DEFAULT ''::character varying NOT NULL,
    "sysName" character varying(100) DEFAULT ''::character varying NOT NULL,
    "sysLocation" character varying(255) DEFAULT ''::character varying NOT NULL,
    "sysContact" character varying(255) DEFAULT ''::character varying NOT NULL,
    "sysDescr" character varying(255) DEFAULT ''::character varying NOT NULL,
    "sysUptime" integer DEFAULT 0 NOT NULL,
    os character varying(64) DEFAULT ''::character varying NOT NULL,
    snmp smallint DEFAULT 0 NOT NULL,
    known smallint DEFAULT 0 NOT NULL,
    up smallint DEFAULT 0 NOT NULL,
    "time" integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.plugin_discover_hosts OWNER TO cactiuser;

--
-- Name: plugin_discover_template; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_discover_template (
    id integer NOT NULL,
    host_template integer DEFAULT 0 NOT NULL,
    tree integer DEFAULT 0 NOT NULL,
    snmp_version smallint DEFAULT 0 NOT NULL,
    sysdescr character varying(255) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.plugin_discover_template OWNER TO cactiuser;

--
-- Name: plugin_discover_template_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_discover_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_discover_template_id_seq OWNER TO cactiuser;

--
-- Name: plugin_discover_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_discover_template_id_seq OWNED BY plugin_discover_template.id;


--
-- Name: plugin_hooks; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_hooks (
    id integer NOT NULL,
    name character varying(32) NOT NULL,
    hook character varying(64) NOT NULL,
    file character varying(255) NOT NULL,
    function character varying(128) NOT NULL,
    status integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.plugin_hooks OWNER TO cactiuser;

--
-- Name: plugin_hooks_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_hooks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_hooks_id_seq OWNER TO cactiuser;

--
-- Name: plugin_hooks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_hooks_id_seq OWNED BY plugin_hooks.id;


--
-- Name: plugin_realms; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_realms (
    id integer NOT NULL,
    plugin character varying(32) NOT NULL,
    file text NOT NULL,
    display character varying(64) NOT NULL
);


ALTER TABLE public.plugin_realms OWNER TO cactiuser;

--
-- Name: plugin_realms_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_realms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_realms_id_seq OWNER TO cactiuser;

--
-- Name: plugin_realms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_realms_id_seq OWNED BY plugin_realms.id;


--
-- Name: plugin_thold_contacts; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_thold_contacts (
    id integer NOT NULL,
    user_id integer NOT NULL,
    type character varying(32) NOT NULL,
    data text NOT NULL
);


ALTER TABLE public.plugin_thold_contacts OWNER TO cactiuser;

--
-- Name: plugin_thold_contacts_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_thold_contacts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_thold_contacts_id_seq OWNER TO cactiuser;

--
-- Name: plugin_thold_contacts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_thold_contacts_id_seq OWNED BY plugin_thold_contacts.id;


--
-- Name: plugin_thold_log; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_thold_log (
    id integer NOT NULL,
    "time" integer NOT NULL,
    host_id integer NOT NULL,
    graph_id integer NOT NULL,
    threshold_id integer NOT NULL,
    threshold_value character varying(64) NOT NULL,
    current character varying(64) NOT NULL,
    status integer NOT NULL,
    type integer NOT NULL,
    description character varying(255) NOT NULL
);


ALTER TABLE public.plugin_thold_log OWNER TO cactiuser;

--
-- Name: plugin_thold_log_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE plugin_thold_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_thold_log_id_seq OWNER TO cactiuser;

--
-- Name: plugin_thold_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE plugin_thold_log_id_seq OWNED BY plugin_thold_log.id;


--
-- Name: plugin_thold_template_contact; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_thold_template_contact (
    template_id integer NOT NULL,
    contact_id integer NOT NULL
);


ALTER TABLE public.plugin_thold_template_contact OWNER TO cactiuser;

--
-- Name: plugin_thold_threshold_contact; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE plugin_thold_threshold_contact (
    thold_id integer NOT NULL,
    contact_id integer NOT NULL
);


ALTER TABLE public.plugin_thold_threshold_contact OWNER TO cactiuser;

--
-- Name: poller; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller (
    id integer NOT NULL,
    hostname character varying(250) NOT NULL,
    ip_address bigint DEFAULT 0 NOT NULL,
    last_update timestamp without time zone NOT NULL
);


ALTER TABLE public.poller OWNER TO cactiuser;

--
-- Name: poller_command; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller_command (
    poller_id integer DEFAULT 0 NOT NULL,
    "time" timestamp without time zone NOT NULL,
    action smallint DEFAULT 0 NOT NULL,
    command character varying(200) NOT NULL
);


ALTER TABLE public.poller_command OWNER TO cactiuser;

--
-- Name: poller_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE poller_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.poller_id_seq OWNER TO cactiuser;

--
-- Name: poller_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE poller_id_seq OWNED BY poller.id;


--
-- Name: poller_item; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller_item (
    local_data_id integer DEFAULT 0 NOT NULL,
    poller_id integer DEFAULT 0 NOT NULL,
    host_id integer DEFAULT 0 NOT NULL,
    action smallint DEFAULT 1 NOT NULL,
    hostname character varying(250) NOT NULL,
    snmp_community character varying(100) NOT NULL,
    snmp_version smallint DEFAULT 0 NOT NULL,
    snmp_username character varying(50) NOT NULL,
    snmp_password character varying(50) NOT NULL,
    snmp_auth_protocol character varying(5) NOT NULL,
    snmp_priv_passphrase character varying(200) NOT NULL,
    snmp_priv_protocol character varying(6) NOT NULL,
    snmp_context character varying(64),
    snmp_port integer DEFAULT 161 NOT NULL,
    snmp_timeout integer DEFAULT 0 NOT NULL,
    rrd_name character varying(19) NOT NULL,
    rrd_path character varying(255) NOT NULL,
    rrd_num smallint DEFAULT 0 NOT NULL,
    rrd_step integer DEFAULT 300 NOT NULL,
    rrd_next_step integer DEFAULT 0 NOT NULL,
    arg1 text,
    arg2 character varying(255),
    arg3 character varying(255)
);


ALTER TABLE public.poller_item OWNER TO cactiuser;

--
-- Name: poller_output; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller_output (
    local_data_id integer DEFAULT 0 NOT NULL,
    rrd_name character varying(19) NOT NULL,
    "time" timestamp without time zone NOT NULL,
    output text NOT NULL
);


ALTER TABLE public.poller_output OWNER TO cactiuser;

--
-- Name: poller_reindex; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller_reindex (
    host_id integer DEFAULT 0 NOT NULL,
    data_query_id integer DEFAULT 0 NOT NULL,
    action smallint DEFAULT 0 NOT NULL,
    op character varying(1) NOT NULL,
    assert_value character varying(100) NOT NULL,
    arg1 character varying(255) NOT NULL
);


ALTER TABLE public.poller_reindex OWNER TO cactiuser;

--
-- Name: poller_time; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE poller_time (
    id integer NOT NULL,
    pid integer DEFAULT 0 NOT NULL,
    poller_id integer DEFAULT 0 NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL
);


ALTER TABLE public.poller_time OWNER TO cactiuser;

--
-- Name: poller_time_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE poller_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.poller_time_id_seq OWNER TO cactiuser;

--
-- Name: poller_time_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE poller_time_id_seq OWNED BY poller_time.id;


--
-- Name: rra; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE rra (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    name character varying(100) NOT NULL,
    x_files_factor double precision DEFAULT 0.1 NOT NULL,
    steps integer DEFAULT 1,
    rows integer DEFAULT 600 NOT NULL,
    timespan integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.rra OWNER TO cactiuser;

--
-- Name: rra_cf; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE rra_cf (
    rra_id integer DEFAULT 0 NOT NULL,
    consolidation_function_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.rra_cf OWNER TO cactiuser;

--
-- Name: rra_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE rra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rra_id_seq OWNER TO cactiuser;

--
-- Name: rra_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE rra_id_seq OWNED BY rra.id;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE settings (
    name character varying(50) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.settings OWNER TO cactiuser;

--
-- Name: settings_graphs; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE settings_graphs (
    user_id integer DEFAULT 0 NOT NULL,
    name character varying(50) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.settings_graphs OWNER TO cactiuser;

--
-- Name: settings_tree; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE settings_tree (
    user_id integer DEFAULT 0 NOT NULL,
    graph_tree_item_id integer DEFAULT 0 NOT NULL,
    status smallint DEFAULT 0 NOT NULL
);


ALTER TABLE public.settings_tree OWNER TO cactiuser;

--
-- Name: snmp_query; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE snmp_query (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    xml_path character varying(255) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    graph_template_id integer DEFAULT 0 NOT NULL,
    data_input_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.snmp_query OWNER TO cactiuser;

--
-- Name: snmp_query_graph; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE snmp_query_graph (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    snmp_query_id integer DEFAULT 0 NOT NULL,
    name character varying(100) NOT NULL,
    graph_template_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.snmp_query_graph OWNER TO cactiuser;

--
-- Name: snmp_query_graph_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE snmp_query_graph_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.snmp_query_graph_id_seq OWNER TO cactiuser;

--
-- Name: snmp_query_graph_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE snmp_query_graph_id_seq OWNED BY snmp_query_graph.id;


--
-- Name: snmp_query_graph_rrd; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE snmp_query_graph_rrd (
    snmp_query_graph_id integer DEFAULT 0 NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    data_template_rrd_id integer DEFAULT 0 NOT NULL,
    snmp_field_name character varying(50) DEFAULT '0'::character varying NOT NULL
);


ALTER TABLE public.snmp_query_graph_rrd OWNER TO cactiuser;

--
-- Name: snmp_query_graph_rrd_sv; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE snmp_query_graph_rrd_sv (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    snmp_query_graph_id integer DEFAULT 0 NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    sequence integer DEFAULT 0 NOT NULL,
    field_name character varying(100) NOT NULL,
    text character varying(255) NOT NULL
);


ALTER TABLE public.snmp_query_graph_rrd_sv OWNER TO cactiuser;

--
-- Name: snmp_query_graph_rrd_sv_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE snmp_query_graph_rrd_sv_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.snmp_query_graph_rrd_sv_id_seq OWNER TO cactiuser;

--
-- Name: snmp_query_graph_rrd_sv_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE snmp_query_graph_rrd_sv_id_seq OWNED BY snmp_query_graph_rrd_sv.id;


--
-- Name: snmp_query_graph_sv; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE snmp_query_graph_sv (
    id integer NOT NULL,
    hash character varying(32) NOT NULL,
    snmp_query_graph_id integer DEFAULT 0 NOT NULL,
    sequence integer DEFAULT 0 NOT NULL,
    field_name character varying(100) NOT NULL,
    text character varying(255) NOT NULL
);


ALTER TABLE public.snmp_query_graph_sv OWNER TO cactiuser;

--
-- Name: snmp_query_graph_sv_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE snmp_query_graph_sv_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.snmp_query_graph_sv_id_seq OWNER TO cactiuser;

--
-- Name: snmp_query_graph_sv_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE snmp_query_graph_sv_id_seq OWNED BY snmp_query_graph_sv.id;


--
-- Name: snmp_query_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE snmp_query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.snmp_query_id_seq OWNER TO cactiuser;

--
-- Name: snmp_query_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE snmp_query_id_seq OWNED BY snmp_query.id;


--
-- Name: thold_data; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE thold_data (
    id integer NOT NULL,
    name character varying(100),
    rra_id integer DEFAULT 0 NOT NULL,
    data_id integer DEFAULT 0 NOT NULL,
    graph_id integer DEFAULT 0 NOT NULL,
    graph_template integer DEFAULT 0 NOT NULL,
    data_template integer DEFAULT 0 NOT NULL,
    thold_hi character varying(100),
    thold_low character varying(100),
    thold_fail_trigger integer,
    thold_fail_count integer DEFAULT 0 NOT NULL,
    time_hi character varying(100),
    time_low character varying(100),
    time_fail_trigger integer DEFAULT 1 NOT NULL,
    time_fail_length integer DEFAULT 1 NOT NULL,
    thold_alert integer DEFAULT 0 NOT NULL,
    thold_enabled character varying(255) DEFAULT 'on'::character varying NOT NULL,
    thold_type integer DEFAULT 0 NOT NULL,
    bl_enabled character varying(255) DEFAULT 'off'::character varying NOT NULL,
    bl_ref_time integer,
    bl_ref_time_range integer,
    bl_pct_down integer,
    bl_pct_up integer,
    bl_fail_trigger integer,
    bl_fail_count integer,
    bl_alert integer DEFAULT 0 NOT NULL,
    lastread character varying(100),
    oldvalue character varying(100),
    repeat_alert integer,
    notify_default character varying(255),
    notify_extra character varying(255),
    host_id integer,
    syslog_priority integer DEFAULT 3 NOT NULL,
    data_type integer DEFAULT 0 NOT NULL,
    cdef integer DEFAULT 0 NOT NULL,
    percent_ds character varying(64) NOT NULL,
    template integer DEFAULT 0 NOT NULL,
    template_enabled character varying(3) NOT NULL,
    tcheck integer DEFAULT 0 NOT NULL,
    exempt character varying(3) DEFAULT 'off'::character varying NOT NULL,
    restored_alert character varying(3) DEFAULT 'off'::character varying NOT NULL
);


ALTER TABLE public.thold_data OWNER TO cactiuser;

--
-- Name: thold_data_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE thold_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.thold_data_id_seq OWNER TO cactiuser;

--
-- Name: thold_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE thold_data_id_seq OWNED BY thold_data.id;


--
-- Name: thold_template; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE thold_template (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    data_template_id integer DEFAULT 0 NOT NULL,
    data_template_name character varying(100) NOT NULL,
    data_source_id integer DEFAULT 0 NOT NULL,
    data_source_name character varying(100) NOT NULL,
    data_source_friendly character varying(100) NOT NULL,
    thold_hi character varying(100),
    thold_low character varying(100),
    thold_fail_trigger integer,
    time_hi character varying(100),
    time_low character varying(100),
    time_fail_trigger integer DEFAULT 1 NOT NULL,
    time_fail_length integer DEFAULT 1 NOT NULL,
    thold_enabled character varying(255) DEFAULT 'on'::character varying NOT NULL,
    thold_type integer DEFAULT 0 NOT NULL,
    bl_enabled character varying(255) DEFAULT 'off'::character varying NOT NULL,
    bl_ref_time integer,
    bl_ref_time_range integer,
    bl_pct_down integer,
    bl_pct_up integer,
    bl_fail_trigger integer,
    bl_fail_count integer,
    bl_alert integer DEFAULT 0 NOT NULL,
    repeat_alert integer,
    notify_default character varying(255),
    notify_extra character varying(255),
    data_type integer DEFAULT 0 NOT NULL,
    cdef integer DEFAULT 0 NOT NULL,
    percent_ds character varying(64) NOT NULL,
    exempt character varying(3) DEFAULT 'off'::character varying NOT NULL,
    restored_alert character varying(3) DEFAULT 'off'::character varying NOT NULL
);


ALTER TABLE public.thold_template OWNER TO cactiuser;

--
-- Name: thold_template_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE thold_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.thold_template_id_seq OWNER TO cactiuser;

--
-- Name: thold_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE thold_template_id_seq OWNED BY thold_template.id;


--
-- Name: user_auth; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE user_auth (
    id integer NOT NULL,
    username character varying(50) DEFAULT '0'::character varying NOT NULL,
    password character varying(50) DEFAULT '0'::character varying NOT NULL,
    realm integer DEFAULT 0 NOT NULL,
    full_name character varying(100) DEFAULT '0'::character varying,
    must_change_password character varying(2),
    show_tree character varying(2) DEFAULT 'on'::character varying,
    show_list character varying(2) DEFAULT 'on'::character varying,
    show_preview character varying(2) DEFAULT 'on'::character varying NOT NULL,
    graph_settings character varying(2),
    login_opts smallint DEFAULT 1 NOT NULL,
    policy_graphs smallint DEFAULT 1 NOT NULL,
    policy_trees smallint DEFAULT 1 NOT NULL,
    policy_hosts smallint DEFAULT 1 NOT NULL,
    policy_graph_templates smallint DEFAULT 1 NOT NULL,
    enabled character varying(2) DEFAULT 'on'::character varying NOT NULL
);


ALTER TABLE public.user_auth OWNER TO cactiuser;

--
-- Name: user_auth_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE user_auth_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_auth_id_seq OWNER TO cactiuser;

--
-- Name: user_auth_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE user_auth_id_seq OWNED BY user_auth.id;


--
-- Name: user_auth_perms; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE user_auth_perms (
    user_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    type smallint DEFAULT 0 NOT NULL
);


ALTER TABLE public.user_auth_perms OWNER TO cactiuser;

--
-- Name: user_auth_realm; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE user_auth_realm (
    realm_id integer DEFAULT 0 NOT NULL,
    user_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.user_auth_realm OWNER TO cactiuser;

--
-- Name: user_log; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE user_log (
    username character varying(50) DEFAULT '0'::character varying NOT NULL,
    user_id integer DEFAULT 0 NOT NULL,
    "time" timestamp without time zone NOT NULL,
    result smallint DEFAULT 0 NOT NULL,
    ip character varying(40) NOT NULL
);


ALTER TABLE public.user_log OWNER TO cactiuser;

--
-- Name: version; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE version (
    cacti character varying(20)
);


ALTER TABLE public.version OWNER TO cactiuser;

--
-- Name: weathermap_auth; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE weathermap_auth (
    userid integer DEFAULT 0 NOT NULL,
    mapid integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.weathermap_auth OWNER TO cactiuser;

--
-- Name: weathermap_data; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE weathermap_data (
    id integer NOT NULL,
    rrdfile character varying(255) NOT NULL,
    data_source_name character varying(19) NOT NULL,
    last_time integer NOT NULL,
    last_value character varying(255) NOT NULL,
    last_calc character varying(255) NOT NULL,
    sequence integer NOT NULL,
    local_data_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.weathermap_data OWNER TO cactiuser;

--
-- Name: weathermap_data_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE weathermap_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weathermap_data_id_seq OWNER TO cactiuser;

--
-- Name: weathermap_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE weathermap_data_id_seq OWNED BY weathermap_data.id;


--
-- Name: weathermap_groups; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE weathermap_groups (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    sortorder integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.weathermap_groups OWNER TO cactiuser;

--
-- Name: weathermap_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE weathermap_groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weathermap_groups_id_seq OWNER TO cactiuser;

--
-- Name: weathermap_groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE weathermap_groups_id_seq OWNED BY weathermap_groups.id;


--
-- Name: weathermap_maps; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE weathermap_maps (
    id integer NOT NULL,
    sortorder integer DEFAULT 0 NOT NULL,
    group_id integer DEFAULT 1 NOT NULL,
    active character varying(255) DEFAULT 'on'::character varying NOT NULL,
    configfile text NOT NULL,
    imagefile text NOT NULL,
    htmlfile text NOT NULL,
    titlecache text NOT NULL,
    filehash character varying(40) NOT NULL,
    warncount integer DEFAULT 0 NOT NULL,
    config text NOT NULL,
    thumb_width integer DEFAULT 0 NOT NULL,
    thumb_height integer DEFAULT 0 NOT NULL,
    schedule character varying(32) DEFAULT '*'::character varying NOT NULL,
    archiving character varying(255) DEFAULT 'off'::character varying NOT NULL
);


ALTER TABLE public.weathermap_maps OWNER TO cactiuser;

--
-- Name: weathermap_maps_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE weathermap_maps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weathermap_maps_id_seq OWNER TO cactiuser;

--
-- Name: weathermap_maps_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE weathermap_maps_id_seq OWNED BY weathermap_maps.id;


--
-- Name: weathermap_settings; Type: TABLE; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE TABLE weathermap_settings (
    id integer NOT NULL,
    mapid integer DEFAULT 0 NOT NULL,
    groupid integer DEFAULT 0 NOT NULL,
    optname character varying(128) NOT NULL,
    optvalue character varying(128) NOT NULL
);


ALTER TABLE public.weathermap_settings OWNER TO cactiuser;

--
-- Name: weathermap_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: cactiuser
--

CREATE SEQUENCE weathermap_settings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weathermap_settings_id_seq OWNER TO cactiuser;

--
-- Name: weathermap_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cactiuser
--

ALTER SEQUENCE weathermap_settings_id_seq OWNED BY weathermap_settings.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE cdef ALTER COLUMN id SET DEFAULT nextval('cdef_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE cdef_items ALTER COLUMN id SET DEFAULT nextval('cdef_items_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE colors ALTER COLUMN id SET DEFAULT nextval('colors_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_input ALTER COLUMN id SET DEFAULT nextval('data_input_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_input_fields ALTER COLUMN id SET DEFAULT nextval('data_input_fields_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_local ALTER COLUMN id SET DEFAULT nextval('data_local_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_template ALTER COLUMN id SET DEFAULT nextval('data_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_template_data ALTER COLUMN id SET DEFAULT nextval('data_template_data_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE data_template_rrd ALTER COLUMN id SET DEFAULT nextval('data_template_rrd_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_local ALTER COLUMN id SET DEFAULT nextval('graph_local_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_template_input ALTER COLUMN id SET DEFAULT nextval('graph_template_input_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_templates ALTER COLUMN id SET DEFAULT nextval('graph_templates_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_templates_gprint ALTER COLUMN id SET DEFAULT nextval('graph_templates_gprint_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_templates_graph ALTER COLUMN id SET DEFAULT nextval('graph_templates_graph_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_templates_item ALTER COLUMN id SET DEFAULT nextval('graph_templates_item_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_tree ALTER COLUMN id SET DEFAULT nextval('graph_tree_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE graph_tree_items ALTER COLUMN id SET DEFAULT nextval('graph_tree_items_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE host ALTER COLUMN id SET DEFAULT nextval('host_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE host_template ALTER COLUMN id SET DEFAULT nextval('host_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_config ALTER COLUMN id SET DEFAULT nextval('plugin_config_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_db_changes ALTER COLUMN id SET DEFAULT nextval('plugin_db_changes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_discover_template ALTER COLUMN id SET DEFAULT nextval('plugin_discover_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_hooks ALTER COLUMN id SET DEFAULT nextval('plugin_hooks_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_realms ALTER COLUMN id SET DEFAULT nextval('plugin_realms_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_thold_contacts ALTER COLUMN id SET DEFAULT nextval('plugin_thold_contacts_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE plugin_thold_log ALTER COLUMN id SET DEFAULT nextval('plugin_thold_log_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE poller ALTER COLUMN id SET DEFAULT nextval('poller_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE poller_time ALTER COLUMN id SET DEFAULT nextval('poller_time_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE rra ALTER COLUMN id SET DEFAULT nextval('rra_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE snmp_query ALTER COLUMN id SET DEFAULT nextval('snmp_query_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE snmp_query_graph ALTER COLUMN id SET DEFAULT nextval('snmp_query_graph_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE snmp_query_graph_rrd_sv ALTER COLUMN id SET DEFAULT nextval('snmp_query_graph_rrd_sv_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE snmp_query_graph_sv ALTER COLUMN id SET DEFAULT nextval('snmp_query_graph_sv_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE thold_data ALTER COLUMN id SET DEFAULT nextval('thold_data_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE thold_template ALTER COLUMN id SET DEFAULT nextval('thold_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE user_auth ALTER COLUMN id SET DEFAULT nextval('user_auth_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE weathermap_data ALTER COLUMN id SET DEFAULT nextval('weathermap_data_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE weathermap_groups ALTER COLUMN id SET DEFAULT nextval('weathermap_groups_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE weathermap_maps ALTER COLUMN id SET DEFAULT nextval('weathermap_maps_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: cactiuser
--

ALTER TABLE weathermap_settings ALTER COLUMN id SET DEFAULT nextval('weathermap_settings_id_seq'::regclass);


--
-- Name: cdef_items_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY cdef_items
    ADD CONSTRAINT cdef_items_pkey PRIMARY KEY (id);


--
-- Name: cdef_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY cdef
    ADD CONSTRAINT cdef_pkey PRIMARY KEY (id);


--
-- Name: colors_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY colors
    ADD CONSTRAINT colors_pkey PRIMARY KEY (id);


--
-- Name: data_input_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_input_data
    ADD CONSTRAINT data_input_data_pkey PRIMARY KEY (data_input_field_id, data_template_data_id);


--
-- Name: data_input_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_input_fields
    ADD CONSTRAINT data_input_fields_pkey PRIMARY KEY (id);


--
-- Name: data_input_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_input
    ADD CONSTRAINT data_input_pkey PRIMARY KEY (id);


--
-- Name: data_local_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_local
    ADD CONSTRAINT data_local_pkey PRIMARY KEY (id);


--
-- Name: data_template_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_template_data
    ADD CONSTRAINT data_template_data_pkey PRIMARY KEY (id);


--
-- Name: data_template_data_rra_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_template_data_rra
    ADD CONSTRAINT data_template_data_rra_pkey PRIMARY KEY (data_template_data_id, rra_id);


--
-- Name: data_template_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_template
    ADD CONSTRAINT data_template_pkey PRIMARY KEY (id);


--
-- Name: data_template_rrd_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY data_template_rrd
    ADD CONSTRAINT data_template_rrd_pkey PRIMARY KEY (id);


--
-- Name: graph_local_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_local
    ADD CONSTRAINT graph_local_pkey PRIMARY KEY (id);


--
-- Name: graph_template_input_defs_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_template_input_defs
    ADD CONSTRAINT graph_template_input_defs_pkey PRIMARY KEY (graph_template_input_id, graph_template_item_id);


--
-- Name: graph_template_input_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_template_input
    ADD CONSTRAINT graph_template_input_pkey PRIMARY KEY (id);


--
-- Name: graph_templates_gprint_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_templates_gprint
    ADD CONSTRAINT graph_templates_gprint_pkey PRIMARY KEY (id);


--
-- Name: graph_templates_graph_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_templates_graph
    ADD CONSTRAINT graph_templates_graph_pkey PRIMARY KEY (id);


--
-- Name: graph_templates_item_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_templates_item
    ADD CONSTRAINT graph_templates_item_pkey PRIMARY KEY (id);


--
-- Name: graph_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_templates
    ADD CONSTRAINT graph_templates_pkey PRIMARY KEY (id);


--
-- Name: graph_tree_items_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_tree_items
    ADD CONSTRAINT graph_tree_items_pkey PRIMARY KEY (id);


--
-- Name: graph_tree_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY graph_tree
    ADD CONSTRAINT graph_tree_pkey PRIMARY KEY (id);


--
-- Name: host_graph_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_graph
    ADD CONSTRAINT host_graph_pkey PRIMARY KEY (host_id, graph_template_id);


--
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (id);


--
-- Name: host_snmp_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_snmp_cache
    ADD CONSTRAINT host_snmp_cache_pkey PRIMARY KEY (host_id, snmp_query_id, field_name, snmp_index);


--
-- Name: host_snmp_query_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_snmp_query
    ADD CONSTRAINT host_snmp_query_pkey PRIMARY KEY (host_id, snmp_query_id);


--
-- Name: host_template_graph_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_template_graph
    ADD CONSTRAINT host_template_graph_pkey PRIMARY KEY (host_template_id, graph_template_id);


--
-- Name: host_template_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_template
    ADD CONSTRAINT host_template_pkey PRIMARY KEY (id);


--
-- Name: host_template_snmp_query_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY host_template_snmp_query
    ADD CONSTRAINT host_template_snmp_query_pkey PRIMARY KEY (host_template_id, snmp_query_id);


--
-- Name: plugin_config_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_config
    ADD CONSTRAINT plugin_config_pkey PRIMARY KEY (id);


--
-- Name: plugin_db_changes_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_db_changes
    ADD CONSTRAINT plugin_db_changes_pkey PRIMARY KEY (id);


--
-- Name: plugin_discover_hosts_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_discover_hosts
    ADD CONSTRAINT plugin_discover_hosts_pkey PRIMARY KEY (ip);


--
-- Name: plugin_discover_template_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_discover_template
    ADD CONSTRAINT plugin_discover_template_pkey PRIMARY KEY (id);


--
-- Name: plugin_hooks_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_hooks
    ADD CONSTRAINT plugin_hooks_pkey PRIMARY KEY (id);


--
-- Name: plugin_realms_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_realms
    ADD CONSTRAINT plugin_realms_pkey PRIMARY KEY (id);


--
-- Name: plugin_thold_contacts_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_thold_contacts
    ADD CONSTRAINT plugin_thold_contacts_pkey PRIMARY KEY (id);


--
-- Name: plugin_thold_log_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY plugin_thold_log
    ADD CONSTRAINT plugin_thold_log_pkey PRIMARY KEY (id);


--
-- Name: poller_command_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller_command
    ADD CONSTRAINT poller_command_pkey PRIMARY KEY (poller_id, action, command);


--
-- Name: poller_item_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller_item
    ADD CONSTRAINT poller_item_pkey PRIMARY KEY (local_data_id, rrd_name);


--
-- Name: poller_output_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller_output
    ADD CONSTRAINT poller_output_pkey PRIMARY KEY (local_data_id, rrd_name, "time");


--
-- Name: poller_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller
    ADD CONSTRAINT poller_pkey PRIMARY KEY (id);


--
-- Name: poller_reindex_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller_reindex
    ADD CONSTRAINT poller_reindex_pkey PRIMARY KEY (host_id, data_query_id, arg1);


--
-- Name: poller_time_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY poller_time
    ADD CONSTRAINT poller_time_pkey PRIMARY KEY (id);


--
-- Name: rra_cf_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY rra_cf
    ADD CONSTRAINT rra_cf_pkey PRIMARY KEY (rra_id, consolidation_function_id);


--
-- Name: rra_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY rra
    ADD CONSTRAINT rra_pkey PRIMARY KEY (id);


--
-- Name: settings_graphs_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY settings_graphs
    ADD CONSTRAINT settings_graphs_pkey PRIMARY KEY (user_id, name);


--
-- Name: settings_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (name);


--
-- Name: settings_tree_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY settings_tree
    ADD CONSTRAINT settings_tree_pkey PRIMARY KEY (user_id, graph_tree_item_id);


--
-- Name: snmp_query_graph_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY snmp_query_graph
    ADD CONSTRAINT snmp_query_graph_pkey PRIMARY KEY (id);


--
-- Name: snmp_query_graph_rrd_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY snmp_query_graph_rrd
    ADD CONSTRAINT snmp_query_graph_rrd_pkey PRIMARY KEY (snmp_query_graph_id, data_template_id, data_template_rrd_id);


--
-- Name: snmp_query_graph_rrd_sv_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY snmp_query_graph_rrd_sv
    ADD CONSTRAINT snmp_query_graph_rrd_sv_pkey PRIMARY KEY (id);


--
-- Name: snmp_query_graph_sv_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY snmp_query_graph_sv
    ADD CONSTRAINT snmp_query_graph_sv_pkey PRIMARY KEY (id);


--
-- Name: snmp_query_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY snmp_query
    ADD CONSTRAINT snmp_query_pkey PRIMARY KEY (id);


--
-- Name: thold_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY thold_data
    ADD CONSTRAINT thold_data_pkey PRIMARY KEY (id);


--
-- Name: thold_template_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY thold_template
    ADD CONSTRAINT thold_template_pkey PRIMARY KEY (id);


--
-- Name: user_auth_perms_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY user_auth_perms
    ADD CONSTRAINT user_auth_perms_pkey PRIMARY KEY (user_id, item_id, type);


--
-- Name: user_auth_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY user_auth
    ADD CONSTRAINT user_auth_pkey PRIMARY KEY (id);


--
-- Name: user_auth_realm_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY user_auth_realm
    ADD CONSTRAINT user_auth_realm_pkey PRIMARY KEY (realm_id, user_id);


--
-- Name: user_log_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY user_log
    ADD CONSTRAINT user_log_pkey PRIMARY KEY (username, user_id, "time");


--
-- Name: weathermap_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY weathermap_data
    ADD CONSTRAINT weathermap_data_pkey PRIMARY KEY (id);


--
-- Name: weathermap_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY weathermap_groups
    ADD CONSTRAINT weathermap_groups_pkey PRIMARY KEY (id);


--
-- Name: weathermap_maps_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY weathermap_maps
    ADD CONSTRAINT weathermap_maps_pkey PRIMARY KEY (id);


--
-- Name: weathermap_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: cactiuser; Tablespace: 
--

ALTER TABLE ONLY weathermap_settings
    ADD CONSTRAINT weathermap_settings_pkey PRIMARY KEY (id);


--
-- Name: cdef_items_cdef_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX cdef_items_cdef_id ON cdef_items USING btree (cdef_id);


--
-- Name: data_input_data_t_value; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_input_data_t_value ON data_input_data USING btree (t_value);


--
-- Name: data_input_fields_data_input_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_input_fields_data_input_id ON data_input_fields USING btree (data_input_id);


--
-- Name: data_input_fields_type_code; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_input_fields_type_code ON data_input_fields USING btree (type_code);


--
-- Name: data_input_name; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_input_name ON data_input USING btree (name);


--
-- Name: data_template_data_data_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_template_data_data_template_id ON data_template_data USING btree (data_template_id);


--
-- Name: data_template_data_local_data_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_template_data_local_data_id ON data_template_data USING btree (local_data_id);


--
-- Name: data_template_rrd_data_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_template_rrd_data_template_id ON data_template_rrd USING btree (data_template_id);


--
-- Name: data_template_rrd_local_data_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_template_rrd_local_data_id ON data_template_rrd USING btree (local_data_id);


--
-- Name: data_template_rrd_local_data_template_rrd_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX data_template_rrd_local_data_template_rrd_id ON data_template_rrd USING btree (local_data_template_rrd_id);


--
-- Name: graph_local_graph_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_local_graph_template_id ON graph_local USING btree (graph_template_id);


--
-- Name: graph_local_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_local_host_id ON graph_local USING btree (host_id);


--
-- Name: graph_local_snmp_index; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_local_snmp_index ON graph_local USING btree (snmp_index);


--
-- Name: graph_local_snmp_query_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_local_snmp_query_id ON graph_local USING btree (snmp_query_id);


--
-- Name: graph_templates_graph_graph_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_graph_graph_template_id ON graph_templates_graph USING btree (graph_template_id);


--
-- Name: graph_templates_graph_local_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_graph_local_graph_id ON graph_templates_graph USING btree (local_graph_id);


--
-- Name: graph_templates_graph_title_cache; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_graph_title_cache ON graph_templates_graph USING btree (title_cache);


--
-- Name: graph_templates_item_graph_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_item_graph_template_id ON graph_templates_item USING btree (graph_template_id);


--
-- Name: graph_templates_item_local_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_item_local_graph_id ON graph_templates_item USING btree (local_graph_id);


--
-- Name: graph_templates_item_task_item_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_item_task_item_id ON graph_templates_item USING btree (task_item_id);


--
-- Name: graph_templates_name; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_templates_name ON graph_templates USING btree (name);


--
-- Name: graph_tree_items_graph_tree_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_tree_items_graph_tree_id ON graph_tree_items USING btree (graph_tree_id);


--
-- Name: graph_tree_items_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_tree_items_host_id ON graph_tree_items USING btree (host_id);


--
-- Name: graph_tree_items_local_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_tree_items_local_graph_id ON graph_tree_items USING btree (local_graph_id);


--
-- Name: graph_tree_items_order_key; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX graph_tree_items_order_key ON graph_tree_items USING btree (order_key);


--
-- Name: host_disabled; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_disabled ON host USING btree (disabled);


--
-- Name: host_snmp_cache_field_name; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_snmp_cache_field_name ON host_snmp_cache USING btree (field_name);


--
-- Name: host_snmp_cache_field_value; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_snmp_cache_field_value ON host_snmp_cache USING btree (field_value);


--
-- Name: host_snmp_cache_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_snmp_cache_host_id ON host_snmp_cache USING btree (host_id, field_name);


--
-- Name: host_snmp_cache_snmp_index; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_snmp_cache_snmp_index ON host_snmp_cache USING btree (snmp_index);


--
-- Name: host_snmp_cache_snmp_query_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX host_snmp_cache_snmp_query_id ON host_snmp_cache USING btree (snmp_query_id);


--
-- Name: plugin_config_directory; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_config_directory ON plugin_config USING btree (directory);


--
-- Name: plugin_config_status; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_config_status ON plugin_config USING btree (status);


--
-- Name: plugin_db_changes_method; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_db_changes_method ON plugin_db_changes USING btree (method);


--
-- Name: plugin_db_changes_plugin; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_db_changes_plugin ON plugin_db_changes USING btree (plugin);


--
-- Name: plugin_discover_hosts_hash; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_hash ON plugin_discover_hosts USING btree (hash);


--
-- Name: plugin_discover_hosts_hostname; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_hostname ON plugin_discover_hosts USING btree (hostname);


--
-- Name: plugin_discover_hosts_known; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_known ON plugin_discover_hosts USING btree (known);


--
-- Name: plugin_discover_hosts_os; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_os ON plugin_discover_hosts USING btree (os);


--
-- Name: plugin_discover_hosts_snmp; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_snmp ON plugin_discover_hosts USING btree (snmp);


--
-- Name: plugin_discover_hosts_up; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_discover_hosts_up ON plugin_discover_hosts USING btree (up);


--
-- Name: plugin_hooks_hook; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_hooks_hook ON plugin_hooks USING btree (hook);


--
-- Name: plugin_hooks_status; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_hooks_status ON plugin_hooks USING btree (status);


--
-- Name: plugin_realms_plugin; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_realms_plugin ON plugin_realms USING btree (plugin);


--
-- Name: plugin_thold_contacts_type; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_contacts_type ON plugin_thold_contacts USING btree (type);


--
-- Name: plugin_thold_contacts_user_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_contacts_user_id ON plugin_thold_contacts USING btree (user_id);


--
-- Name: plugin_thold_log_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_graph_id ON plugin_thold_log USING btree (graph_id);


--
-- Name: plugin_thold_log_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_host_id ON plugin_thold_log USING btree (host_id);


--
-- Name: plugin_thold_log_status; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_status ON plugin_thold_log USING btree (status);


--
-- Name: plugin_thold_log_threshold_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_threshold_id ON plugin_thold_log USING btree (threshold_id);


--
-- Name: plugin_thold_log_time; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_time ON plugin_thold_log USING btree ("time");


--
-- Name: plugin_thold_log_type; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_log_type ON plugin_thold_log USING btree (type);


--
-- Name: plugin_thold_template_contact_contact_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_template_contact_contact_id ON plugin_thold_template_contact USING btree (contact_id);


--
-- Name: plugin_thold_template_contact_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_template_contact_template_id ON plugin_thold_template_contact USING btree (template_id);


--
-- Name: plugin_thold_threshold_contact_contact_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_threshold_contact_contact_id ON plugin_thold_threshold_contact USING btree (contact_id);


--
-- Name: plugin_thold_threshold_contact_thold_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX plugin_thold_threshold_contact_thold_id ON plugin_thold_threshold_contact USING btree (thold_id);


--
-- Name: poller_item_action; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX poller_item_action ON poller_item USING btree (action);


--
-- Name: poller_item_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX poller_item_host_id ON poller_item USING btree (host_id);


--
-- Name: poller_item_rrd_next_step; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX poller_item_rrd_next_step ON poller_item USING btree (rrd_next_step);


--
-- Name: snmp_query_graph_rrd_data_template_rrd_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX snmp_query_graph_rrd_data_template_rrd_id ON snmp_query_graph_rrd USING btree (data_template_rrd_id);


--
-- Name: snmp_query_graph_rrd_sv_data_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX snmp_query_graph_rrd_sv_data_template_id ON snmp_query_graph_rrd_sv USING btree (data_template_id);


--
-- Name: snmp_query_graph_rrd_sv_snmp_query_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX snmp_query_graph_rrd_sv_snmp_query_graph_id ON snmp_query_graph_rrd_sv USING btree (snmp_query_graph_id);


--
-- Name: snmp_query_graph_sv_snmp_query_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX snmp_query_graph_sv_snmp_query_graph_id ON snmp_query_graph_sv USING btree (snmp_query_graph_id);


--
-- Name: snmp_query_name; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX snmp_query_name ON snmp_query USING btree (name);


--
-- Name: thold_data_data_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_data_id ON thold_data USING btree (data_id);


--
-- Name: thold_data_graph_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_graph_id ON thold_data USING btree (graph_id);


--
-- Name: thold_data_host_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_host_id ON thold_data USING btree (host_id);


--
-- Name: thold_data_rra_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_rra_id ON thold_data USING btree (rra_id);


--
-- Name: thold_data_tcheck; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_tcheck ON thold_data USING btree (tcheck);


--
-- Name: thold_data_template; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_template ON thold_data USING btree (template);


--
-- Name: thold_data_template_enabled; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_template_enabled ON thold_data USING btree (template_enabled);


--
-- Name: thold_data_thold_enabled; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_data_thold_enabled ON thold_data USING btree (thold_enabled);


--
-- Name: thold_template_data_source_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_template_data_source_id ON thold_template USING btree (data_source_id);


--
-- Name: thold_template_data_template_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX thold_template_data_template_id ON thold_template USING btree (data_template_id);


--
-- Name: user_auth_by_realm; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX user_auth_by_realm ON user_auth USING btree (realm);


--
-- Name: user_auth_enabled; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX user_auth_enabled ON user_auth USING btree (enabled);


--
-- Name: user_auth_perms_user_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX user_auth_perms_user_id ON user_auth_perms USING btree (user_id, type);


--
-- Name: user_auth_realm_user_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX user_auth_realm_user_id ON user_auth_realm USING btree (user_id);


--
-- Name: user_auth_username; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX user_auth_username ON user_auth USING btree (username);


--
-- Name: weathermap_data_data_source_name; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX weathermap_data_data_source_name ON weathermap_data USING btree (data_source_name);


--
-- Name: weathermap_data_local_data_id; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX weathermap_data_local_data_id ON weathermap_data USING btree (local_data_id);


--
-- Name: weathermap_data_rrdfile; Type: INDEX; Schema: public; Owner: cactiuser; Tablespace: 
--

CREATE INDEX weathermap_data_rrdfile ON weathermap_data USING btree (rrdfile);


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

