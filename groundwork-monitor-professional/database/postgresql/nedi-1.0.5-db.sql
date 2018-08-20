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
    name character varying(64) NOT NULL,
    ip bigint,
    serial character varying(32),
    type character varying(32),
    firstseen integer,
    lastseen integer,
    services smallint,
    description character varying(255),
    os character varying(16),
    bootimage character varying(64),
    location character varying(255),
    contact character varying(255),
    vtpdomain character varying(32),
    vtpmode smallint,
    snmpversion smallint,
    community character varying(32),
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
    logalarm character varying(255),
    flags smallint
);


ALTER TABLE public.devices OWNER TO nedi;

--
-- Name: iftrack; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE iftrack (
    mac character varying(12),
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
    device character varying(64),
    deps integer,
    firstseen integer,
    lastseen integer,
    who character varying(32),
    "time" integer,
    category smallint,
    comment character varying(255)
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
    device character varying(64),
    ifname character varying(32),
    ifidx bigint,
    linktype character varying(4),
    type bigint,
    mac character varying(12),
    description character varying(255),
    alias character varying(64),
    status smallint,
    speed bigint,
    duplex character varying(2),
    vlid integer,
    inoct bigint,
    inerr bigint,
    outoct bigint,
    outerr bigint,
    dinoct bigint,
    dinerr integer,
    doutoct bigint,
    douterr integer,
    comment character varying(255)
);


ALTER TABLE public.interfaces OWNER TO nedi;

--
-- Name: iptrack; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE iptrack (
    mac character varying(12),
    ipupdate integer,
    name character varying(64),
    ip bigint
);


ALTER TABLE public.iptrack OWNER TO nedi;

--
-- Name: links; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE links (
    id integer NOT NULL,
    device character varying(64),
    ifname character varying(32),
    neighbour character varying(64),
    nbrifname character varying(32),
    bandwidth bigint,
    type character varying(4),
    power integer,
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
    comment character varying(64)
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
-- Name: messages; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE messages (
    id integer NOT NULL,
    level smallint,
    "time" integer,
    source character varying(64),
    info character varying(255)
);


ALTER TABLE public.messages OWNER TO nedi;

--
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.messages_id_seq OWNER TO nedi;

--
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE messages_id_seq OWNED BY messages.id;


--
-- Name: modules; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE modules (
    device character varying(64),
    slot character varying(64),
    model character varying(32),
    description character varying(255),
    serial character varying(32),
    hw character varying(128),
    fw character varying(128),
    sw character varying(128),
    modidx integer
);


ALTER TABLE public.modules OWNER TO nedi;

--
-- Name: monitoring; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE monitoring (
    device character varying(64) NOT NULL,
    status integer,
    depend character varying(64),
    sms integer,
    mail integer,
    lastchk integer,
    uptime integer,
    lost integer,
    ok integer,
    delay integer
);


ALTER TABLE public.monitoring OWNER TO nedi;

--
-- Name: networks; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE networks (
    device character varying(64),
    ifname character varying(32),
    ip bigint,
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
    ip bigint,
    mac character varying(12) NOT NULL,
    oui character varying(32),
    firstseen integer,
    lastseen integer,
    device character varying(64),
    ifname character varying(32),
    vlanid integer,
    ifmetric smallint,
    ifupdate integer,
    ifchanges integer,
    ipupdate integer,
    ipchanges integer,
    iplost integer,
    arp integer,
    tcpports character varying(64),
    udpports character varying(64),
    os character varying(32) DEFAULT '-'::character varying,
    type character varying(32) DEFAULT '-'::character varying,
    osupdate integer DEFAULT 0
);


ALTER TABLE public.nodes OWNER TO nedi;

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
    lastseen integer,
    source character varying(32) DEFAULT '-'::character varying
);


ALTER TABLE public.stock OWNER TO nedi;

--
-- Name: stolen; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE stolen (
    name character varying(64),
    ip bigint,
    mac character varying(12) NOT NULL,
    device character varying(64),
    ifname character varying(32),
    who character varying(32),
    "time" integer
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
-- Name: user; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE "user" (
    name character varying(32) NOT NULL,
    password character varying(32) NOT NULL,
    groups smallint DEFAULT 0 NOT NULL,
    email character varying(64),
    phone character varying(32),
    "time" integer,
    lastseen integer,
    comment character varying(255),
    language character varying(16) DEFAULT 'english'::character varying NOT NULL,
    theme character varying(16) DEFAULT 'default'::character varying NOT NULL,
    volume smallint DEFAULT 10 NOT NULL,
    columns smallint DEFAULT 5 NOT NULL,
    msglimit smallint DEFAULT 5 NOT NULL,
    graphs smallint DEFAULT 2 NOT NULL
);


ALTER TABLE public."user" OWNER TO nedi;

--
-- Name: vlans; Type: TABLE; Schema: public; Owner: nedi; Tablespace: 
--

CREATE TABLE vlans (
    device character varying(64),
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
-- Name: id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE messages ALTER COLUMN id SET DEFAULT nextval('messages_id_seq'::regclass);


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
    ADD CONSTRAINT devices_pkey PRIMARY KEY (name);


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
-- Name: messages_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: monitoring_device_key; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY monitoring
    ADD CONSTRAINT monitoring_device_key UNIQUE (device);


--
-- Name: nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT nodes_pkey PRIMARY KEY (mac);


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
-- Name: system_name_key; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY system
    ADD CONSTRAINT system_name_key UNIQUE (name);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (name);


--
-- Name: chat_time; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX chat_time ON chat USING btree ("time");


--
-- Name: chat_user; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX chat_user ON chat USING btree ("user");


--
-- Name: configs_device_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX configs_device_2 ON configs USING btree (device);


--
-- Name: devdel_device_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX devdel_device_2 ON devdel USING btree (device);


--
-- Name: devices_name_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX devices_name_2 ON devices USING btree (name);


--
-- Name: iftrack_ifupdate; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iftrack_ifupdate ON iftrack USING btree (ifupdate);


--
-- Name: iftrack_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iftrack_mac ON iftrack USING btree (mac);


--
-- Name: incidents_id; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX incidents_id ON incidents USING btree (id);


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
-- Name: iptrack_ipupdate; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iptrack_ipupdate ON iptrack USING btree (ipupdate);


--
-- Name: iptrack_mac; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX iptrack_mac ON iptrack USING btree (mac);


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
-- Name: links_neighbour; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX links_neighbour ON links USING btree (neighbour);


--
-- Name: locations_region; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX locations_region ON locations USING btree (region);


--
-- Name: messages_id; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX messages_id ON messages USING btree (id);


--
-- Name: messages_level; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX messages_level ON messages USING btree (level);


--
-- Name: messages_source; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX messages_source ON messages USING btree (source);


--
-- Name: messages_time; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX messages_time ON messages USING btree ("time");


--
-- Name: modules_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX modules_device ON modules USING btree (device);


--
-- Name: modules_slot; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX modules_slot ON modules USING btree (slot);


--
-- Name: monitoring_device_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX monitoring_device_2 ON monitoring USING btree (device);


--
-- Name: networks_device; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_device ON networks USING btree (device);


--
-- Name: networks_ifname; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_ifname ON networks USING btree (ifname);


--
-- Name: networks_ip; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX networks_ip ON networks USING btree (ip);


--
-- Name: nodes_ip; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_ip ON nodes USING btree (ip);


--
-- Name: nodes_mac_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_mac_2 ON nodes USING btree (mac);


--
-- Name: nodes_name; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_name ON nodes USING btree (name);


--
-- Name: nodes_vlanid; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX nodes_vlanid ON nodes USING btree (vlanid);


--
-- Name: stock_serial_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX stock_serial_2 ON stock USING btree (serial);


--
-- Name: stolen_mac_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX stolen_mac_2 ON stolen USING btree (mac);


--
-- Name: system_name_2; Type: INDEX; Schema: public; Owner: nedi; Tablespace: 
--

CREATE INDEX system_name_2 ON system USING btree (name);


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

