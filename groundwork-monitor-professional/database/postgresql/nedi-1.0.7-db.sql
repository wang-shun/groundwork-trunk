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

--
-- Name: _my_to_pg_time_format(text); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION _my_to_pg_time_format(text) RETURNS text
    LANGUAGE sql
    AS $_$
SELECT array_to_string(
    ARRAY(
	SELECT
	    CASE (element).e
	    WHEN '%%' THEN '%'
	    WHEN '%D' THEN 'FMDDth'
	    WHEN '%H' THEN 'HH24'
	    WHEN '%I' THEN 'HH'
	    WHEN '%M' THEN 'Month'
	    WHEN '%S' THEN 'SS'
	    WHEN '%T' THEN 'HH24:MI:SS'
	    WHEN '%U' THEN '%%U'
	    WHEN '%V' THEN '%%V'
	    WHEN '%W' THEN 'FMDay'
	    WHEN '%X' THEN '%%X'
	    WHEN '%Y' THEN 'YYYY'
	    WHEN '%a' THEN 'Dy'
	    WHEN '%b' THEN 'Mon'
	    WHEN '%c' THEN 'FMMM'
	    WHEN '%d' THEN 'DD'
	    WHEN '%e' THEN 'FMDD'
	    WHEN '%f' THEN 'US'
	    WHEN '%h' THEN 'HH12'
	    WHEN '%i' THEN 'MI'
	    WHEN '%j' THEN 'DDD'
	    WHEN '%k' THEN 'FMHH24'
	    WHEN '%l' THEN 'FMHH12'
	    WHEN '%m' THEN 'MM'
	    WHEN '%p' THEN 'am'
	    WHEN '%r' THEN 'HH:MI:SSam'
	    WHEN '%s' THEN 'SS'
	    WHEN '%u' THEN '%%u'
	    WHEN '%v' THEN '%%v'
	    WHEN '%w' THEN '%%w'
	    WHEN '%x' THEN '%%x'
	    WHEN '%y' THEN 'YY'
	    WHEN '"'  THEN '\"'
	    ELSE
		coalesce(substring((element).e from '^%(.)$'), (element).e)
	    END
	FROM (SELECT array_to_string(regexp_matches($1, '[^%"]+|"|%.|%$', 'g'),'') as e) AS element
    ),
    ''
)
$_$;


ALTER FUNCTION public._my_to_pg_time_format(text) OWNER TO nedi;

--
-- Name: _mysqlf_pgsql(text); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION _mysqlf_pgsql(text) RETURNS text
    LANGUAGE sql
    AS $_$
SELECT array_to_string(
    ARRAY(
	SELECT
	    CASE (element).e
	    WHEN '%%' THEN '%'
	    WHEN '%D' THEN 'FMDDth'
	    WHEN '%H' THEN 'HH24'
	    WHEN '%I' THEN 'HH'
	    WHEN '%M' THEN 'Month'
	    WHEN '%S' THEN 'SS'
	    WHEN '%T' THEN 'HH24:MI:SS'
	    WHEN '%U' THEN '%%U'
	    WHEN '%V' THEN '%%V'
	    WHEN '%W' THEN 'FMDay'
	    WHEN '%X' THEN '%%X'
	    WHEN '%Y' THEN 'YYYY'
	    WHEN '%a' THEN 'Dy'
	    WHEN '%b' THEN 'Mon'
	    WHEN '%c' THEN 'FMMM'
	    WHEN '%d' THEN 'DD'
	    WHEN '%e' THEN 'FMDD'
	    WHEN '%f' THEN 'US'
	    WHEN '%h' THEN 'HH12'
	    WHEN '%i' THEN 'MI'
	    WHEN '%j' THEN 'DDD'
	    WHEN '%k' THEN 'FMHH24'
	    WHEN '%l' THEN 'FMHH12'
	    WHEN '%m' THEN 'MM'
	    WHEN '%p' THEN 'am'
	    WHEN '%r' THEN 'HH:MI:SSam'
	    WHEN '%s' THEN 'SS'
	    WHEN '%u' THEN '%%u'
	    WHEN '%v' THEN '%%v'
	    WHEN '%w' THEN '%%w'
	    WHEN '%x' THEN '%%x'
	    WHEN '%y' THEN 'YY'
	    ELSE
		coalesce(substring((element).e from '^%(.)$'), (element).e)
	    END
	FROM (SELECT array_to_string(regexp_matches($1, '[^%]+|%.|%$', 'g'),'') as e) AS element
    ),
    ''
)
$_$;


ALTER FUNCTION public._mysqlf_pgsql(text) OWNER TO nedi;

--
-- Name: curdate(); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION curdate() RETURNS date
    LANGUAGE sql STABLE
    AS $$
SELECT CURRENT_DATE
$$;


ALTER FUNCTION public.curdate() OWNER TO nedi;

--
-- Name: datediff(date, date); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION datediff(date, date) RETURNS integer
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
SELECT $1 - $2
$_$;


ALTER FUNCTION public.datediff(date, date) OWNER TO nedi;

--
-- Name: inet_aton(text); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION inet_aton(text) RETURNS bigint
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
  DECLARE
      a text[];
      b text[4];
      up int;
      family int;
      i int;
  BEGIN
      IF position(':' in $1) > 0 THEN
        family = 6;
      ELSE
        family = 4;
      END IF;
      -- Even MySQL doesn't support IPv6, because the return type of
      -- bigint (64 bits) is insufficient for a 128-bit IPv6 address.
      IF family = 6 THEN
        RETURN NULL;
      END IF;
      a = pg_catalog.string_to_array($1, '.');
      up = array_upper(a, 1);
      IF up IS NULL THEN
        RETURN NULL;
      ELSIF up = 4 THEN
        -- nothing to do
        b = a;
      ELSIF up = 3 THEN
        -- 127.1.2 = 127.1.0.2
        b = array[a[1], a[2], '0', a[3]];
      ELSIF up = 2 THEN
        -- 127.1 = 127.0.0.1
        b = array[a[1], '0', '0', a[2]];
      ELSIF up = 1 THEN
        -- 127 = 0.0.0.127
        b = array['0', '0', '0', a[1]];
      END IF;
      i = 1;
      -- handle 127..1
      WHILE i <= 4 LOOP
        IF length(b[i]) = 0 THEN
          b[i] = '0';
        END IF;
        IF b[i] !~ '^[0-9]{1,3}$' THEN
          RETURN NULL;
        END IF;
        IF b[i]::int > 255 THEN
          RETURN NULL;
        END IF;
        i = i + 1;
      END LOOP;
      RETURN (b[1]::bigint << 24) | (b[2]::bigint << 16) | (b[3]::bigint << 8) | b[4]::bigint;
  END
$_$;


ALTER FUNCTION public.inet_aton(text) OWNER TO nedi;

--
-- Name: inet_ntoa(bigint); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION inet_ntoa(bigint) RETURNS character varying
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
SELECT CASE WHEN $1 > 4294967295 THEN NULL WHEN $1 < 0 THEN NULL ELSE
    (($1 >> 24) & 255) operator(pg_catalog.||) '.' operator(pg_catalog.||)
    (($1 >> 16) & 255) operator(pg_catalog.||) '.' operator(pg_catalog.||)
    (($1 >>  8) & 255) operator(pg_catalog.||) '.' operator(pg_catalog.||)
    (($1      ) & 255) END;
$_$;


ALTER FUNCTION public.inet_ntoa(bigint) OWNER TO nedi;

--
-- Name: str_to_date(text, text); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION str_to_date(text, text) RETURNS date
    LANGUAGE sql STABLE STRICT
    AS $_$
SELECT to_date($1, _my_to_pg_time_format($2))
$_$;


ALTER FUNCTION public.str_to_date(text, text) OWNER TO nedi;

--
-- Name: substring_index(text, text, integer); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION substring_index(str text, delim text, count integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
SELECT CASE
WHEN $2 = '' THEN ''
WHEN $3 > 0 THEN array_to_string((string_to_array($1,$2))[1:$3], $2)
WHEN $3 < 0 THEN array_to_string(
    ARRAY(
	SELECT unnest(string_to_array($1,$2))
	    OFFSET GREATEST(array_upper(string_to_array($1,$2),1) + $3, 0)
    ),
    $2
)
ELSE ''
END
$_$;


ALTER FUNCTION public.substring_index(str text, delim text, count integer) OWNER TO nedi;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: chat; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE chat (
    "time" integer,
    "user" character varying(32),
    message character varying(255)
);


ALTER TABLE public.chat OWNER TO nedi;

--
-- Name: cisco_contracts; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE cisco_contracts (
    contract_number character varying(16) NOT NULL,
    service_level character varying(40) NOT NULL,
    contract_label character varying(32),
    bill_to_name character varying(32),
    address character varying(40),
    city character varying(32),
    state character varying(16),
    zip_code character varying(16),
    country character varying(16),
    bill_to_contact character varying(32),
    phone character varying(16),
    email character varying(40),
    site_id character varying(15),
    site_name character varying(15),
    site_address character varying(40),
    address_line2 character varying(40),
    address_line3 character varying(40),
    site_city character varying(40),
    site_state character varying(16),
    site_zip character varying(16),
    site_country character varying(16),
    site_notes character varying(40),
    site_label character varying(40),
    site_contact character varying(40),
    site_phone character varying(16),
    site_email character varying(40),
    product_number character varying(32) NOT NULL,
    serial_number character varying(40) NOT NULL,
    name_ip_address character varying(32),
    description character varying(64),
    product_type character varying(32),
    begin_date character varying(16),
    end_date character varying(16),
    po_number character varying(16),
    so_number character varying(16)
);


ALTER TABLE public.cisco_contracts OWNER TO nedi;

--
-- Name: configs; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE configs (
    device character varying(64) NOT NULL,
    config text,
    changes text,
    "time" integer
);


ALTER TABLE public.configs OWNER TO nedi;

--
-- Name: devdel; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE devdel (
    device character varying(64) NOT NULL,
    "user" character varying(32),
    "time" integer
);


ALTER TABLE public.devdel OWNER TO nedi;

--
-- Name: devices; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE devices (
    device character varying(64) NOT NULL,
    devip bigint DEFAULT 0,
    serial character varying(32),
    type character varying(32),
    firstdis integer,
    lastdis integer,
    services smallint,
    description character varying(255),
    devos character varying(16),
    bootimage character varying(64),
    location character varying(255),
    contact character varying(255),
    vtpdomain character varying(32),
    vtpmode smallint,
    snmpversion smallint,
    readcomm character varying(32),
    cliport integer,
    login character varying(32),
    icon character varying(16),
    origip bigint,
    cpu smallint,
    memcpu bigint,
    temp smallint,
    cusvalue bigint,
    cuslabel character varying(32),
    sysobjid character varying(255),
    writecomm character varying(32)
);


ALTER TABLE public.devices OWNER TO nedi;

--
-- Name: events; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE events (
    id integer NOT NULL,
    level smallint,
    "time" integer,
    source character varying(64),
    info character varying(255),
    class character varying(4) DEFAULT 'dev'::character varying,
    device character varying(64)
);


ALTER TABLE public.events OWNER TO nedi;

--
-- Name: events_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.events_id_seq OWNER TO nedi;

--
-- Name: events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE events_id_seq OWNED BY events.id;


--
-- Name: iftrack; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE iftrack (
    mac character varying(12) NOT NULL,
    ifupdate integer,
    device character varying(64),
    ifname character varying(32),
    vlanid integer,
    ifmetric smallint
);


ALTER TABLE public.iftrack OWNER TO nedi;

--
-- Name: incidents; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE incidents (
    id integer NOT NULL,
    level smallint,
    name character varying(64),
    deps integer,
    start integer,
    "end" integer,
    "user" character varying(32),
    "time" integer,
    grp smallint,
    comment character varying(255),
    device character varying(64)
);


ALTER TABLE public.incidents OWNER TO nedi;

--
-- Name: incidents_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE incidents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.incidents_id_seq OWNER TO nedi;

--
-- Name: incidents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE incidents_id_seq OWNED BY incidents.id;


--
-- Name: interfaces; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE interfaces (
    device character varying(64) NOT NULL,
    ifname character varying(32) NOT NULL,
    ifidx bigint,
    linktype character varying(4),
    iftype bigint,
    ifmac character varying(12),
    ifdesc character varying(255),
    alias character varying(64),
    ifstat smallint,
    speed bigint,
    duplex character varying(2),
    pvid integer DEFAULT 0,
    inoct bigint,
    inerr bigint,
    outoct bigint,
    outerr bigint,
    dinoct bigint DEFAULT 0,
    dinerr integer DEFAULT 0,
    doutoct bigint DEFAULT 0,
    douterr integer DEFAULT 0,
    comment character varying(255),
    poe integer DEFAULT 0
);


ALTER TABLE public.interfaces OWNER TO nedi;

--
-- Name: iptrack; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE iptrack (
    mac character varying(12) NOT NULL,
    ipupdate integer,
    name character varying(64),
    nodip bigint,
    vlanid integer,
    device character varying(64) NOT NULL
);


ALTER TABLE public.iptrack OWNER TO nedi;

--
-- Name: links; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE links (
    id integer NOT NULL,
    device character varying(64) NOT NULL,
    ifname character varying(32),
    neighbor character varying(64) NOT NULL,
    nbrifname character varying(32),
    bandwidth bigint,
    linktype character varying(4),
    linkdesc character varying(255),
    nbrduplex character varying(2),
    nbrvlanid integer
);


ALTER TABLE public.links OWNER TO nedi;

--
-- Name: links_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE links_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.links_id_seq OWNER TO nedi;

--
-- Name: links_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE links_id_seq OWNED BY links.id;


--
-- Name: locations; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE locations (
    id integer NOT NULL,
    region character varying(32) NOT NULL,
    city character varying(32),
    building character varying(32),
    x integer,
    y integer,
    ns integer DEFAULT 0,
    ew integer DEFAULT 0,
    locdesc character varying(255)
);


ALTER TABLE public.locations OWNER TO nedi;

--
-- Name: locations_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE locations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.locations_id_seq OWNER TO nedi;

--
-- Name: locations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE locations_id_seq OWNED BY locations.id;


--
-- Name: modules; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE modules (
    device character varying(64) NOT NULL,
    slot character varying(64),
    model character varying(32),
    moddesc character varying(255),
    serial character varying(32),
    hw character varying(128),
    fw character varying(128),
    sw character varying(128),
    modidx character varying(32)
);


ALTER TABLE public.modules OWNER TO nedi;

--
-- Name: monitoring; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE monitoring (
    name character varying(64) NOT NULL,
    monip bigint,
    class character varying(4) DEFAULT 'dev'::character varying,
    test character varying(6),
    lastok integer DEFAULT 0,
    status integer DEFAULT 0,
    lost integer DEFAULT 0,
    ok integer DEFAULT 0,
    latency integer DEFAULT 0,
    latmax integer DEFAULT 0,
    latavg integer DEFAULT 0,
    uptime integer DEFAULT 0,
    alert smallint DEFAULT 0,
    eventfwd character varying(255),
    eventdel character varying(255),
    depend character varying(64) DEFAULT '-'::character varying,
    device character varying(64) NOT NULL
);


ALTER TABLE public.monitoring OWNER TO nedi;

--
-- Name: networks; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE networks (
    device character varying(64) NOT NULL,
    ifname character varying(32),
    ifip bigint,
    mask bigint,
    vrfname character varying(32),
    status smallint
);


ALTER TABLE public.networks OWNER TO nedi;

--
-- Name: nodes; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE nodes (
    name character varying(64),
    nodip bigint,
    mac character varying(12) NOT NULL,
    oui character varying(32),
    firstseen integer,
    lastseen integer,
    device character varying(64),
    ifname character varying(32),
    vlanid integer,
    ifmetric integer,
    ifupdate integer,
    ifchanges integer,
    ipupdate integer,
    ipchanges integer,
    iplost integer,
    arpval integer,
    tcpports character varying(64),
    udpports character varying(64),
    nodtype character varying(64) DEFAULT '-'::character varying,
    nodos character varying(64) DEFAULT '-'::character varying,
    osupdate integer DEFAULT 0
);


ALTER TABLE public.nodes OWNER TO nedi;

--
-- Name: nodetrack; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE nodetrack (
    device character varying(64),
    ifname character varying(32),
    value character varying(64),
    source character varying(8),
    "user" character varying(32),
    "time" integer
);


ALTER TABLE public.nodetrack OWNER TO nedi;

--
-- Name: stock; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE stock (
    serial character varying(32),
    type character varying(32),
    "user" character varying(32),
    "time" integer,
    location character varying(255),
    state smallint,
    comment character varying(255),
    lastwty integer,
    source character varying(32) DEFAULT '-'::character varying
);


ALTER TABLE public.stock OWNER TO nedi;

--
-- Name: stolen; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE stolen (
    name character varying(64),
    stlip bigint,
    mac character varying(12) NOT NULL,
    device character varying(64),
    ifname character varying(32),
    "user" character varying(32),
    "time" integer,
    comment character varying(255)
);


ALTER TABLE public.stolen OWNER TO nedi;

--
-- Name: system; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE system (
    name character varying(32) NOT NULL,
    value character varying(32)
);


ALTER TABLE public.system OWNER TO nedi;

--
-- Name: users; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE users (
    "user" character varying(32) NOT NULL,
    password character varying(32) NOT NULL,
    groups smallint DEFAULT 0 NOT NULL,
    email character varying(64),
    phone character varying(32),
    "time" integer,
    lastlogin integer,
    comment character varying(255),
    language character varying(16) DEFAULT 'english'::character varying NOT NULL,
    theme character varying(16) DEFAULT 'default'::character varying NOT NULL,
    volume smallint DEFAULT 34 NOT NULL,
    columns smallint DEFAULT 5 NOT NULL,
    msglimit smallint DEFAULT 5 NOT NULL,
    graphs smallint DEFAULT 2 NOT NULL,
    dateformat character varying(16) DEFAULT 'j.M y G:i'::character varying NOT NULL,
    viewdev character varying(255)
);


ALTER TABLE public.users OWNER TO nedi;

--
-- Name: vlans; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE vlans (
    device character varying(64) NOT NULL,
    vlanid integer,
    vlanname character varying(32)
);


ALTER TABLE public.vlans OWNER TO nedi;

--
-- Name: wlan; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE wlan (
    mac character varying(12),
    "time" integer
);


ALTER TABLE public.wlan OWNER TO nedi;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE events ALTER COLUMN id SET DEFAULT nextval('events_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE incidents ALTER COLUMN id SET DEFAULT nextval('incidents_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE links ALTER COLUMN id SET DEFAULT nextval('links_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE locations ALTER COLUMN id SET DEFAULT nextval('locations_id_seq'::regclass);


--
-- Name: cisco_contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY cisco_contracts
    ADD CONSTRAINT cisco_contracts_pkey PRIMARY KEY (serial_number);


--
-- Name: configs_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY configs
    ADD CONSTRAINT configs_pkey PRIMARY KEY (device);


--
-- Name: devdel_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY devdel
    ADD CONSTRAINT devdel_pkey PRIMARY KEY (device);


--
-- Name: devices_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY devices
    ADD CONSTRAINT devices_pkey PRIMARY KEY (device);


--
-- Name: events_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: incidents_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY incidents
    ADD CONSTRAINT incidents_pkey PRIMARY KEY (id);


--
-- Name: links_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY links
    ADD CONSTRAINT links_pkey PRIMARY KEY (id);


--
-- Name: locations_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: monitoring_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY monitoring
    ADD CONSTRAINT monitoring_pkey PRIMARY KEY (name);


--
-- Name: stock_serial_key; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_serial_key UNIQUE (serial);


--
-- Name: stolen_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY stolen
    ADD CONSTRAINT stolen_pkey PRIMARY KEY (mac);


--
-- Name: system_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY system
    ADD CONSTRAINT system_pkey PRIMARY KEY (name);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY ("user");


--
-- Name: chat_time; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX chat_time ON chat USING btree ("time");


--
-- Name: chat_user; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX chat_user ON chat USING btree ("user");


--
-- Name: events_class; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_class ON events USING btree (class);


--
-- Name: events_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_device ON events USING btree (device);


--
-- Name: events_id; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_id ON events USING btree (id);


--
-- Name: events_level; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_level ON events USING btree (level);


--
-- Name: events_source; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_source ON events USING btree (source);


--
-- Name: events_time; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX events_time ON events USING btree ("time");


--
-- Name: iftrack_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iftrack_device ON iftrack USING btree (device);


--
-- Name: iftrack_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iftrack_mac ON iftrack USING btree (mac);


--
-- Name: iftrack_vlanid; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iftrack_vlanid ON iftrack USING btree (vlanid);


--
-- Name: incidents_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX incidents_device ON incidents USING btree (device);


--
-- Name: incidents_id; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX incidents_id ON incidents USING btree (id);


--
-- Name: incidents_name; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX incidents_name ON incidents USING btree (name);


--
-- Name: interfaces_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX interfaces_device ON interfaces USING btree (device);


--
-- Name: interfaces_ifidx; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX interfaces_ifidx ON interfaces USING btree (ifidx);


--
-- Name: interfaces_ifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX interfaces_ifname ON interfaces USING btree (ifname);


--
-- Name: iptrack_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iptrack_device ON iptrack USING btree (device);


--
-- Name: iptrack_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iptrack_mac ON iptrack USING btree (mac);


--
-- Name: iptrack_vlanid; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iptrack_vlanid ON iptrack USING btree (vlanid);


--
-- Name: links_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_device ON links USING btree (device);


--
-- Name: links_id; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_id ON links USING btree (id);


--
-- Name: links_ifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_ifname ON links USING btree (ifname);


--
-- Name: links_nbrifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_nbrifname ON links USING btree (nbrifname);


--
-- Name: links_neighbor; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_neighbor ON links USING btree (neighbor);


--
-- Name: locations_region; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX locations_region ON locations USING btree (region);


--
-- Name: modules_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX modules_device ON modules USING btree (device);


--
-- Name: modules_slot; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX modules_slot ON modules USING btree (slot);


--
-- Name: monitoring_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX monitoring_device ON monitoring USING btree (device);


--
-- Name: networks_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_device ON networks USING btree (device);


--
-- Name: networks_ifip; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_ifip ON networks USING btree (ifip);


--
-- Name: networks_ifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_ifname ON networks USING btree (ifname);


--
-- Name: nodes_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_device ON nodes USING btree (device);


--
-- Name: nodes_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_mac ON nodes USING btree (mac);


--
-- Name: nodes_name; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_name ON nodes USING btree (name);


--
-- Name: nodes_nodip; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_nodip ON nodes USING btree (nodip);


--
-- Name: nodes_vlanid; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_vlanid ON nodes USING btree (vlanid);


--
-- Name: nodetrack_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodetrack_device ON nodetrack USING btree (device);


--
-- Name: nodetrack_ifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodetrack_ifname ON nodetrack USING btree (ifname);


--
-- Name: stolen_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX stolen_device ON stolen USING btree (device);


--
-- Name: stolen_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX stolen_mac ON stolen USING btree (mac);


--
-- Name: vlans_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX vlans_device ON vlans USING btree (device);


--
-- Name: vlans_vlanid; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX vlans_vlanid ON vlans USING btree (vlanid);


--
-- Name: wlan_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX wlan_mac ON wlan USING btree (mac);


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

