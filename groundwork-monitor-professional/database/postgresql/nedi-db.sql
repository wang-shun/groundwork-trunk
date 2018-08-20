--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.8
-- Dumped by pg_dump version 9.6.8

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

DROP INDEX IF EXISTS public.wlan_mac;
DROP INDEX IF EXISTS public.vlans_vlanid;
DROP INDEX IF EXISTS public.vlans_device;
DROP INDEX IF EXISTS public.vlanport_vlopts;
DROP INDEX IF EXISTS public.vlanport_vlanid;
DROP INDEX IF EXISTS public.vlanport_ifname;
DROP INDEX IF EXISTS public.vlanport_device;
DROP INDEX IF EXISTS public.translations_srctype;
DROP INDEX IF EXISTS public.routes_nhip;
DROP INDEX IF EXISTS public.routes_dstip;
DROP INDEX IF EXISTS public.routes_device;
DROP INDEX IF EXISTS public.policies_status;
DROP INDEX IF EXISTS public.policies_class;
DROP INDEX IF EXISTS public.nodnd_nodip6;
DROP INDEX IF EXISTS public.nodnd_ndifname;
DROP INDEX IF EXISTS public.nodnd_nddevice;
DROP INDEX IF EXISTS public.nodnd_mac;
DROP INDEX IF EXISTS public.nodes_vlanid;
DROP INDEX IF EXISTS public.nodes_noduser;
DROP INDEX IF EXISTS public.nodes_mac;
DROP INDEX IF EXISTS public.nodes_ifname;
DROP INDEX IF EXISTS public.nodes_device;
DROP INDEX IF EXISTS public.nodarp_nodip;
DROP INDEX IF EXISTS public.nodarp_mac;
DROP INDEX IF EXISTS public.nodarp_arpifname;
DROP INDEX IF EXISTS public.nodarp_arpdevice;
DROP INDEX IF EXISTS public.networks_ifname;
DROP INDEX IF EXISTS public.networks_ifip;
DROP INDEX IF EXISTS public.networks_device;
DROP INDEX IF EXISTS public.netinfo_netip;
DROP INDEX IF EXISTS public.netinfo_country;
DROP INDEX IF EXISTS public.nbrtrack_time;
DROP INDEX IF EXISTS public.nbrtrack_neighbor;
DROP INDEX IF EXISTS public.nbrtrack_ifname;
DROP INDEX IF EXISTS public.nbrtrack_device;
DROP INDEX IF EXISTS public.monitoring_device;
DROP INDEX IF EXISTS public.monimap_usrname;
DROP INDEX IF EXISTS public.modules_serial;
DROP INDEX IF EXISTS public.modules_device;
DROP INDEX IF EXISTS public.locations_region;
DROP INDEX IF EXISTS public.links_neighbor;
DROP INDEX IF EXISTS public.links_nbrifname;
DROP INDEX IF EXISTS public.links_ifname;
DROP INDEX IF EXISTS public.links_device;
DROP INDEX IF EXISTS public.iptrack_mac;
DROP INDEX IF EXISTS public.iptrack_arpifname;
DROP INDEX IF EXISTS public.iptrack_arpdevice;
DROP INDEX IF EXISTS public.interfaces_ifname;
DROP INDEX IF EXISTS public.interfaces_ifidx;
DROP INDEX IF EXISTS public.interfaces_device_ifname;
DROP INDEX IF EXISTS public.incidents_name;
DROP INDEX IF EXISTS public.incidents_device;
DROP INDEX IF EXISTS public.iftrack_vlanid;
DROP INDEX IF EXISTS public.iftrack_mac;
DROP INDEX IF EXISTS public.iftrack_device;
DROP INDEX IF EXISTS public.events_time;
DROP INDEX IF EXISTS public.events_source;
DROP INDEX IF EXISTS public.events_level;
DROP INDEX IF EXISTS public.events_device;
DROP INDEX IF EXISTS public.events_class;
DROP INDEX IF EXISTS public.dns_nodip;
DROP INDEX IF EXISTS public.dns_aname;
DROP INDEX IF EXISTS public.dns6_nodip6;
DROP INDEX IF EXISTS public.dns6_aaaaname;
DROP INDEX IF EXISTS public.devices_location;
DROP INDEX IF EXISTS public.devices_contact;
DROP INDEX IF EXISTS public.chat_usrname;
DROP INDEX IF EXISTS public.chat_time;
DROP INDEX IF EXISTS public.cables_panel;
DROP INDEX IF EXISTS public.cables_nbrpanel;
DROP INDEX IF EXISTS public.cables_nbrjack;
DROP INDEX IF EXISTS public.cables_jack;
DROP INDEX IF EXISTS public.cables_cblopt;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.translations DROP CONSTRAINT IF EXISTS translations_pkey;
ALTER TABLE IF EXISTS ONLY public.system DROP CONSTRAINT IF EXISTS system_pkey;
ALTER TABLE IF EXISTS ONLY public.policies DROP CONSTRAINT IF EXISTS policies_pkey;
ALTER TABLE IF EXISTS ONLY public.nbrtrack DROP CONSTRAINT IF EXISTS nbrtrack_pkey;
ALTER TABLE IF EXISTS ONLY public.monitoring DROP CONSTRAINT IF EXISTS monitoring_pkey;
ALTER TABLE IF EXISTS ONLY public.monimap DROP CONSTRAINT IF EXISTS monimap_pkey;
ALTER TABLE IF EXISTS ONLY public.locations DROP CONSTRAINT IF EXISTS locations_pkey;
ALTER TABLE IF EXISTS ONLY public.links DROP CONSTRAINT IF EXISTS links_pkey;
ALTER TABLE IF EXISTS ONLY public.inventory DROP CONSTRAINT IF EXISTS inventory_pkey;
ALTER TABLE IF EXISTS ONLY public.install DROP CONSTRAINT IF EXISTS install_pkey;
ALTER TABLE IF EXISTS ONLY public.incidents DROP CONSTRAINT IF EXISTS incidents_pkey;
ALTER TABLE IF EXISTS ONLY public.events DROP CONSTRAINT IF EXISTS events_pkey;
ALTER TABLE IF EXISTS ONLY public.devices DROP CONSTRAINT IF EXISTS devices_pkey;
ALTER TABLE IF EXISTS ONLY public.configs DROP CONSTRAINT IF EXISTS configs_pkey;
ALTER TABLE IF EXISTS ONLY public.cables DROP CONSTRAINT IF EXISTS cables_pkey;
ALTER TABLE IF EXISTS public.translations ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.policies ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.nbrtrack ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.monimap ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.locations ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.links ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.incidents ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.events ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cables ALTER COLUMN id DROP DEFAULT;
DROP TABLE IF EXISTS public.wlan;
DROP TABLE IF EXISTS public.vlans;
DROP TABLE IF EXISTS public.vlanport;
DROP TABLE IF EXISTS public.users;
DROP SEQUENCE IF EXISTS public.translations_id_seq;
DROP TABLE IF EXISTS public.translations;
DROP TABLE IF EXISTS public.system;
DROP TABLE IF EXISTS public.routes;
DROP SEQUENCE IF EXISTS public.policies_id_seq;
DROP TABLE IF EXISTS public.policies;
DROP TABLE IF EXISTS public.nodnd;
DROP TABLE IF EXISTS public.nodes;
DROP TABLE IF EXISTS public.nodarp;
DROP TABLE IF EXISTS public.networks;
DROP TABLE IF EXISTS public.netinfo;
DROP SEQUENCE IF EXISTS public.nbrtrack_id_seq;
DROP TABLE IF EXISTS public.nbrtrack;
DROP TABLE IF EXISTS public.monitoring;
DROP SEQUENCE IF EXISTS public.monimap_id_seq;
DROP TABLE IF EXISTS public.monimap;
DROP TABLE IF EXISTS public.modules;
DROP SEQUENCE IF EXISTS public.locations_id_seq;
DROP TABLE IF EXISTS public.locations;
DROP SEQUENCE IF EXISTS public.links_id_seq;
DROP TABLE IF EXISTS public.links;
DROP TABLE IF EXISTS public.iptrack;
DROP TABLE IF EXISTS public.inventory;
DROP TABLE IF EXISTS public.interfaces;
DROP TABLE IF EXISTS public.install;
DROP SEQUENCE IF EXISTS public.incidents_id_seq;
DROP TABLE IF EXISTS public.incidents;
DROP TABLE IF EXISTS public.iftrack;
DROP SEQUENCE IF EXISTS public.events_id_seq;
DROP TABLE IF EXISTS public.events;
DROP TABLE IF EXISTS public.dns6;
DROP TABLE IF EXISTS public.dns;
DROP TABLE IF EXISTS public.devices;
DROP TABLE IF EXISTS public.configs;
DROP TABLE IF EXISTS public.chat;
DROP SEQUENCE IF EXISTS public.cables_id_seq;
DROP TABLE IF EXISTS public.cables;
DROP FUNCTION IF EXISTS public.inet_ntoa(bigint);
DROP EXTENSION IF EXISTS plpgsql;
DROP SCHEMA IF EXISTS public;
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


--
-- Name: inet_ntoa(bigint); Type: FUNCTION; Schema: public; Owner: nedi
--

CREATE FUNCTION public.inet_ntoa(bigint) RETURNS inet
    LANGUAGE sql IMMUTABLE
    AS $_$
	select '0.0.0.0'::inet+$1;$_$;


ALTER FUNCTION public.inet_ntoa(bigint) OWNER TO nedi;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: cables; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.cables (
    id integer NOT NULL,
    panel character varying(64) NOT NULL,
    jack character varying(32) DEFAULT ''::character varying,
    nbrpanel character varying(64) DEFAULT ''::character varying,
    nbrjack character varying(32) DEFAULT ''::character varying,
    cblopt character varying(8) DEFAULT ''::character varying,
    cbldesc character varying(64) DEFAULT ''::character varying,
    cbllength bigint DEFAULT 0,
    "time" bigint DEFAULT 0,
    usrname character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.cables OWNER TO nedi;

--
-- Name: cables_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.cables_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cables_id_seq OWNER TO nedi;

--
-- Name: cables_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.cables_id_seq OWNED BY public.cables.id;


--
-- Name: chat; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.chat (
    "time" bigint,
    usrname character varying(32) DEFAULT ''::character varying,
    message character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.chat OWNER TO nedi;

--
-- Name: configs; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.configs (
    device character varying(64) NOT NULL,
    config text,
    changes text,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.configs OWNER TO nedi;

--
-- Name: devices; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.devices (
    device character varying(64) NOT NULL,
    devip bigint DEFAULT 0,
    serial character varying(32) DEFAULT ''::character varying,
    type character varying(32) DEFAULT ''::character varying,
    firstdis bigint DEFAULT 0,
    lastdis bigint DEFAULT 0,
    services smallint DEFAULT 0,
    description character varying(255) DEFAULT ''::character varying,
    devos character varying(16) DEFAULT ''::character varying,
    bootimage character varying(64) DEFAULT ''::character varying,
    location character varying(255) DEFAULT ''::character varying,
    contact character varying(255) DEFAULT ''::character varying,
    devgroup character varying(32) DEFAULT ''::character varying,
    devmode smallint DEFAULT 0,
    snmpversion smallint DEFAULT 0,
    readcomm character varying(32) DEFAULT ''::character varying,
    cliport integer DEFAULT 0,
    login character varying(32) DEFAULT ''::character varying,
    icon character varying(16) DEFAULT ''::character varying,
    origip bigint DEFAULT 0,
    cpu smallint DEFAULT 0,
    memcpu bigint DEFAULT 0,
    temp smallint DEFAULT 0,
    cusvalue bigint DEFAULT 0,
    cuslabel character varying(32) DEFAULT ''::character varying,
    sysobjid character varying(255) DEFAULT ''::character varying,
    writecomm character varying(32) DEFAULT ''::character varying,
    devopts character varying(32) DEFAULT ''::character varying,
    size smallint DEFAULT 0,
    stack smallint DEFAULT 1,
    maxpoe integer DEFAULT 0,
    totpoe integer DEFAULT 0,
    cfgchange bigint DEFAULT 0,
    cfgstatus character varying(2) DEFAULT '--'::character varying,
    vendor character varying(32) DEFAULT ''::character varying,
    totmac bigint DEFAULT 0,
    totarp bigint DEFAULT 0,
    totnd bigint DEFAULT 0,
    devstatus smallint DEFAULT 0,
    cpualert smallint DEFAULT 0,
    memalert bigint DEFAULT 0,
    tempalert smallint DEFAULT 0,
    supplyalert bigint DEFAULT 0,
    poewarn smallint DEFAULT 0,
    arppoison integer DEFAULT 1
);


ALTER TABLE public.devices OWNER TO nedi;

--
-- Name: dns; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.dns (
    nodip bigint DEFAULT 0,
    aname character varying(64) DEFAULT ''::character varying,
    dnsupdate bigint DEFAULT 0
);


ALTER TABLE public.dns OWNER TO nedi;

--
-- Name: dns6; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.dns6 (
    nodip6 inet,
    aaaaname character varying(64) DEFAULT ''::character varying,
    dns6update bigint DEFAULT 0
);


ALTER TABLE public.dns6 OWNER TO nedi;

--
-- Name: events; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.events (
    id integer NOT NULL,
    level smallint DEFAULT 0,
    "time" bigint DEFAULT 0,
    source character varying(64) DEFAULT ''::character varying,
    info character varying(255) DEFAULT ''::character varying,
    class character varying(4) DEFAULT 'dev'::character varying,
    device character varying(64) DEFAULT ''::character varying
);


ALTER TABLE public.events OWNER TO nedi;

--
-- Name: events_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.events_id_seq OWNER TO nedi;

--
-- Name: events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.events_id_seq OWNED BY public.events.id;


--
-- Name: iftrack; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.iftrack (
    mac character varying(16) NOT NULL,
    ifupdate bigint DEFAULT 0,
    device character varying(64) DEFAULT ''::character varying,
    ifname character varying(32) DEFAULT ''::character varying,
    vlanid integer DEFAULT 0
);


ALTER TABLE public.iftrack OWNER TO nedi;

--
-- Name: incidents; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.incidents (
    id integer NOT NULL,
    level smallint DEFAULT 0,
    name character varying(64) DEFAULT ''::character varying,
    deps bigint DEFAULT 0,
    startinc bigint DEFAULT 0,
    endinc bigint DEFAULT 0,
    usrname character varying(32) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0,
    grp smallint DEFAULT 0,
    comment character varying(255) DEFAULT ''::character varying,
    device character varying(64) DEFAULT ''::character varying
);


ALTER TABLE public.incidents OWNER TO nedi;

--
-- Name: incidents_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.incidents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.incidents_id_seq OWNER TO nedi;

--
-- Name: incidents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.incidents_id_seq OWNED BY public.incidents.id;


--
-- Name: install; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.install (
    type character varying(32) DEFAULT ''::character varying,
    target character varying(32) DEFAULT ''::character varying,
    name character varying(64) NOT NULL,
    ipaddr character varying(15) DEFAULT ''::character varying,
    mask character varying(15) DEFAULT ''::character varying,
    gateway character varying(15) DEFAULT ''::character varying,
    vlanid integer DEFAULT 0,
    location character varying(255) DEFAULT ''::character varying,
    contact character varying(255) DEFAULT ''::character varying,
    login character varying(32) DEFAULT ''::character varying,
    template character varying(32) DEFAULT ''::character varying,
    status smallint DEFAULT 10
);


ALTER TABLE public.install OWNER TO nedi;

--
-- Name: interfaces; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.interfaces (
    device character varying(64) NOT NULL,
    ifname character varying(32) NOT NULL,
    ifidx bigint NOT NULL,
    linktype character varying(4) DEFAULT ''::character varying,
    iftype integer DEFAULT 0,
    ifmac character varying(12) DEFAULT ''::character varying,
    ifdesc character varying(255) DEFAULT ''::character varying,
    alias character varying(64) DEFAULT ''::character varying,
    ifstat smallint DEFAULT 0,
    speed bigint DEFAULT 0,
    duplex character varying(2) DEFAULT ''::character varying,
    pvid integer DEFAULT 0,
    inoct bigint DEFAULT 0,
    inerr bigint DEFAULT 0,
    outoct bigint DEFAULT 0,
    outerr bigint DEFAULT 0,
    dinoct bigint DEFAULT 0,
    dinerr bigint DEFAULT 0,
    doutoct bigint DEFAULT 0,
    douterr bigint DEFAULT 0,
    indis bigint DEFAULT 0,
    outdis bigint DEFAULT 0,
    dindis bigint DEFAULT 0,
    doutdis bigint DEFAULT 0,
    inbrc bigint DEFAULT 0,
    dinbrc bigint DEFAULT 0,
    lastchg bigint DEFAULT 0,
    poe integer DEFAULT 0,
    comment character varying(255) DEFAULT ''::character varying,
    trafalert smallint DEFAULT 0,
    brcalert integer DEFAULT 0,
    macflood integer DEFAULT 0
);


ALTER TABLE public.interfaces OWNER TO nedi;

--
-- Name: inventory; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.inventory (
    state smallint DEFAULT 0,
    serial character varying(32) NOT NULL,
    assetclass smallint DEFAULT 1,
    assettype character varying(32) DEFAULT 0,
    assetnumber character varying(32) DEFAULT ''::character varying,
    assetlocation character varying(255) DEFAULT ''::character varying,
    assetcontact character varying(255) DEFAULT ''::character varying,
    assetupdate bigint DEFAULT 0,
    pursource character varying(32) DEFAULT '-'::character varying,
    purcost bigint DEFAULT 0,
    purnumber character varying(32) DEFAULT ''::character varying,
    purtime bigint DEFAULT 0,
    maintpartner character varying(32) DEFAULT ''::character varying,
    maintsla character varying(32) DEFAULT ''::character varying,
    maintdesc character varying(32) DEFAULT ''::character varying,
    maintcost bigint DEFAULT 0,
    maintstatus smallint DEFAULT 0,
    startmaint bigint DEFAULT 0,
    endmaint bigint DEFAULT 0,
    endwarranty bigint DEFAULT 0,
    endsupport bigint DEFAULT 0,
    endlife bigint DEFAULT 0,
    comment character varying(255) DEFAULT ''::character varying,
    usrname character varying(32) DEFAULT '-'::character varying
);


ALTER TABLE public.inventory OWNER TO nedi;

--
-- Name: iptrack; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.iptrack (
    mac character varying(16) NOT NULL,
    ipupdate bigint DEFAULT 0,
    nodip bigint DEFAULT 0,
    arpdevice character varying(64) DEFAULT ''::character varying,
    arpifname character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.iptrack OWNER TO nedi;

--
-- Name: links; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.links (
    id integer NOT NULL,
    device character varying(64) NOT NULL,
    ifname character varying(32) DEFAULT ''::character varying,
    neighbor character varying(64) NOT NULL,
    nbrifname character varying(32) DEFAULT ''::character varying,
    bandwidth bigint DEFAULT 0,
    linktype character varying(4) DEFAULT ''::character varying,
    linkdesc character varying(255) DEFAULT ''::character varying,
    nbrduplex character varying(2) DEFAULT ''::character varying,
    nbrvlanid integer DEFAULT 0,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.links OWNER TO nedi;

--
-- Name: links_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.links_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.links_id_seq OWNER TO nedi;

--
-- Name: links_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.links_id_seq OWNED BY public.links.id;


--
-- Name: locations; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.locations (
    id integer NOT NULL,
    region character varying(32) NOT NULL,
    city character varying(32) DEFAULT ''::character varying,
    building character varying(32) DEFAULT ''::character varying,
    x integer DEFAULT 0,
    y integer DEFAULT 0,
    ns integer DEFAULT 0,
    ew integer DEFAULT 0,
    locdesc character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.locations OWNER TO nedi;

--
-- Name: locations_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.locations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.locations_id_seq OWNER TO nedi;

--
-- Name: locations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.locations_id_seq OWNED BY public.locations.id;


--
-- Name: modules; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.modules (
    device character varying(64) NOT NULL,
    slot character varying(64) DEFAULT ''::character varying,
    model character varying(32) DEFAULT ''::character varying,
    moddesc character varying(255) DEFAULT ''::character varying,
    serial character varying(32) DEFAULT ''::character varying,
    hw character varying(128) DEFAULT ''::character varying,
    fw character varying(128) DEFAULT ''::character varying,
    sw character varying(128) DEFAULT ''::character varying,
    modidx bigint DEFAULT 0,
    modclass smallint DEFAULT 1,
    status smallint DEFAULT 0,
    modloc character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.modules OWNER TO nedi;

--
-- Name: monimap; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.monimap (
    id integer NOT NULL,
    title character varying(32) DEFAULT ''::character varying,
    filter character varying(255) DEFAULT ''::character varying,
    args character varying(255) DEFAULT ''::character varying,
    mapopts character varying(8) DEFAULT ''::character varying,
    usrname character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.monimap OWNER TO nedi;

--
-- Name: monimap_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.monimap_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monimap_id_seq OWNER TO nedi;

--
-- Name: monimap_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.monimap_id_seq OWNED BY public.monimap.id;


--
-- Name: monitoring; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.monitoring (
    name character varying(64) NOT NULL,
    monip bigint,
    class character varying(4) DEFAULT 'dev'::character varying,
    test character varying(6) DEFAULT ''::character varying,
    testopt character varying(64) DEFAULT ''::character varying,
    testres character varying(64) DEFAULT ''::character varying,
    lastok bigint DEFAULT 0,
    status bigint DEFAULT 0,
    lost bigint DEFAULT 0,
    ok bigint DEFAULT 0,
    latency integer DEFAULT 0,
    latmax integer DEFAULT 0,
    latavg integer DEFAULT 0,
    uptime bigint DEFAULT 0,
    alert smallint DEFAULT 0,
    eventfwd character varying(255) DEFAULT ''::character varying,
    eventlvl smallint DEFAULT 0,
    eventdel character varying(255) DEFAULT ''::character varying,
    eventmax character varying(255) DEFAULT ''::character varying,
    depend1 character varying(64) DEFAULT ''::character varying,
    depend2 character varying(64) DEFAULT ''::character varying,
    device character varying(64) NOT NULL,
    notify character varying(32) DEFAULT ''::character varying,
    noreply smallint DEFAULT 2,
    latwarn integer DEFAULT 100
);


ALTER TABLE public.monitoring OWNER TO nedi;

--
-- Name: nbrtrack; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.nbrtrack (
    device character varying(64) DEFAULT ''::character varying,
    ifname character varying(32) DEFAULT ''::character varying,
    neighbor character varying(64) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0,
    id integer NOT NULL
);


ALTER TABLE public.nbrtrack OWNER TO nedi;

--
-- Name: nbrtrack_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.nbrtrack_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.nbrtrack_id_seq OWNER TO nedi;

--
-- Name: nbrtrack_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.nbrtrack_id_seq OWNED BY public.nbrtrack.id;


--
-- Name: netinfo; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.netinfo (
    netip bigint DEFAULT 0,
    netdomain character varying(64) DEFAULT ''::character varying,
    orgname character varying(32) DEFAULT ''::character varying,
    orgdomain character varying(32) DEFAULT ''::character varying,
    phone character varying(16) DEFAULT ''::character varying,
    address character varying(64) DEFAULT ''::character varying,
    country character varying(2) DEFAULT ''::character varying,
    description character varying(64) DEFAULT ''::character varying,
    origin character varying(32) DEFAULT ''::character varying,
    icon character varying(16) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.netinfo OWNER TO nedi;

--
-- Name: networks; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.networks (
    device character varying(64) NOT NULL,
    ifname character varying(32) DEFAULT ''::character varying,
    ifip bigint DEFAULT 0,
    ifip6 inet,
    prefix smallint DEFAULT 0,
    vrfname character varying(32) DEFAULT ''::character varying,
    vrfrd character varying(16) DEFAULT ''::character varying,
    status smallint DEFAULT 0
);


ALTER TABLE public.networks OWNER TO nedi;

--
-- Name: nodarp; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.nodarp (
    mac character varying(16) DEFAULT ''::character varying,
    nodip bigint DEFAULT 0,
    ipchanges bigint DEFAULT 0,
    ipupdate bigint DEFAULT 0,
    tcpports character varying(64) DEFAULT ''::character varying,
    udpports character varying(64) DEFAULT ''::character varying,
    srvtype character varying(255) DEFAULT ''::character varying,
    srvos character varying(64) DEFAULT ''::character varying,
    srvupdate bigint DEFAULT 0,
    arpdevice character varying(64) DEFAULT ''::character varying,
    arpifname character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.nodarp OWNER TO nedi;

--
-- Name: nodes; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.nodes (
    mac character varying(16) NOT NULL,
    oui character varying(32) DEFAULT ''::character varying,
    firstseen bigint DEFAULT 0,
    lastseen bigint DEFAULT 0,
    device character varying(64) DEFAULT ''::character varying,
    ifname character varying(32) DEFAULT ''::character varying,
    vlanid integer DEFAULT 0,
    metric character varying(10) DEFAULT ''::character varying,
    ifupdate bigint DEFAULT 0,
    ifchanges bigint DEFAULT 0,
    noduser character varying(32) DEFAULT ''::character varying,
    nodesc character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.nodes OWNER TO nedi;

--
-- Name: nodnd; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.nodnd (
    mac character varying(16) DEFAULT ''::character varying,
    nodip6 inet,
    ip6changes bigint DEFAULT 0,
    ip6update bigint DEFAULT 0,
    tcp6ports character varying(64) DEFAULT ''::character varying,
    udp6ports character varying(64) DEFAULT ''::character varying,
    srv6type character varying(255) DEFAULT ''::character varying,
    srv6os character varying(64) DEFAULT ''::character varying,
    srv6update bigint DEFAULT 0,
    nddevice character varying(64) DEFAULT ''::character varying,
    ndifname character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.nodnd OWNER TO nedi;

--
-- Name: policies; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.policies (
    id integer NOT NULL,
    status smallint DEFAULT 0,
    class character varying(4) DEFAULT ''::character varying,
    polopts character varying(8) DEFAULT ''::character varying,
    target character varying(64) DEFAULT ''::character varying,
    device character varying(64) DEFAULT ''::character varying,
    type character varying(128) DEFAULT ''::character varying,
    location character varying(32) DEFAULT ''::character varying,
    contact character varying(32) DEFAULT ''::character varying,
    devgroup character varying(64) DEFAULT ''::character varying,
    ifname character varying(32) DEFAULT ''::character varying,
    vlan character varying(32) DEFAULT ''::character varying,
    alert smallint DEFAULT 0,
    info character varying(255) DEFAULT ''::character varying,
    respolicy bigint DEFAULT 0,
    usrname character varying(32) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.policies OWNER TO nedi;

--
-- Name: policies_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.policies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.policies_id_seq OWNER TO nedi;

--
-- Name: policies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.policies_id_seq OWNED BY public.policies.id;


--
-- Name: routes; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.routes (
    device character varying(64) NOT NULL,
    dstip bigint DEFAULT 0,
    dstprefix smallint DEFAULT 0,
    nhip bigint DEFAULT 0,
    nhif bigint DEFAULT 0,
    rprot smallint DEFAULT 0
);


ALTER TABLE public.routes OWNER TO nedi;

--
-- Name: system; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.system (
    name character varying(32) NOT NULL,
    value character varying(32) DEFAULT ''::character varying
);


ALTER TABLE public.system OWNER TO nedi;

--
-- Name: translations; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.translations (
    id integer NOT NULL,
    srctype character varying(32) DEFAULT ''::character varying,
    tgtgroup character varying(32) DEFAULT ''::character varying,
    context character varying(32) DEFAULT ''::character varying,
    srccfg character varying(255) DEFAULT ''::character varying,
    dstcfg text,
    tropts character varying(4) DEFAULT ''::character varying,
    usrname character varying(32) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.translations OWNER TO nedi;

--
-- Name: translations_id_seq; Type: SEQUENCE; Schema: public; Owner: nedi
--

CREATE SEQUENCE public.translations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.translations_id_seq OWNER TO nedi;

--
-- Name: translations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nedi
--

ALTER SEQUENCE public.translations_id_seq OWNED BY public.translations.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.users (
    usrname character varying(32) NOT NULL,
    password character varying(64) DEFAULT ''::character varying NOT NULL,
    groups integer DEFAULT 0 NOT NULL,
    email character varying(64) DEFAULT ''::character varying,
    phone character varying(32) DEFAULT ''::character varying,
    "time" bigint DEFAULT 0,
    lastlogin bigint DEFAULT 0,
    comment character varying(255) DEFAULT ''::character varying,
    language character varying(16) DEFAULT 'english'::character varying NOT NULL,
    theme character varying(16) DEFAULT 'groundwork'::character varying NOT NULL,
    volume smallint DEFAULT '60'::smallint NOT NULL,
    columns smallint DEFAULT '6'::smallint NOT NULL,
    msglimit smallint DEFAULT '5'::smallint NOT NULL,
    miscopts integer DEFAULT 35 NOT NULL,
    dateformat character varying(16) DEFAULT 'j.M y G:i470'::character varying NOT NULL,
    viewdev character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.users OWNER TO nedi;

--
-- Name: vlanport; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.vlanport (
    device character varying(64) NOT NULL,
    ifname character varying(32) DEFAULT ''::character varying,
    vlanid integer DEFAULT 0,
    vlopts character varying(4) DEFAULT ''::character varying
);


ALTER TABLE public.vlanport OWNER TO nedi;

--
-- Name: vlans; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.vlans (
    device character varying(64) NOT NULL,
    vlanid integer DEFAULT 0,
    vlanname character varying(64) DEFAULT ''::character varying
);


ALTER TABLE public.vlans OWNER TO nedi;

--
-- Name: wlan; Type: TABLE; Schema: public; Owner: nedi
--

CREATE TABLE public.wlan (
    mac character varying(8) NOT NULL,
    "time" bigint DEFAULT 0
);


ALTER TABLE public.wlan OWNER TO nedi;

--
-- Name: cables id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.cables ALTER COLUMN id SET DEFAULT nextval('public.cables_id_seq'::regclass);


--
-- Name: events id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.events ALTER COLUMN id SET DEFAULT nextval('public.events_id_seq'::regclass);


--
-- Name: incidents id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.incidents ALTER COLUMN id SET DEFAULT nextval('public.incidents_id_seq'::regclass);


--
-- Name: links id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.links ALTER COLUMN id SET DEFAULT nextval('public.links_id_seq'::regclass);


--
-- Name: locations id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.locations ALTER COLUMN id SET DEFAULT nextval('public.locations_id_seq'::regclass);


--
-- Name: monimap id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.monimap ALTER COLUMN id SET DEFAULT nextval('public.monimap_id_seq'::regclass);


--
-- Name: nbrtrack id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.nbrtrack ALTER COLUMN id SET DEFAULT nextval('public.nbrtrack_id_seq'::regclass);


--
-- Name: policies id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.policies ALTER COLUMN id SET DEFAULT nextval('public.policies_id_seq'::regclass);


--
-- Name: translations id; Type: DEFAULT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.translations ALTER COLUMN id SET DEFAULT nextval('public.translations_id_seq'::regclass);


--
-- Name: cables cables_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.cables
    ADD CONSTRAINT cables_pkey PRIMARY KEY (id);


--
-- Name: configs configs_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.configs
    ADD CONSTRAINT configs_pkey PRIMARY KEY (device);


--
-- Name: devices devices_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.devices
    ADD CONSTRAINT devices_pkey PRIMARY KEY (device);


--
-- Name: events events_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: incidents incidents_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.incidents
    ADD CONSTRAINT incidents_pkey PRIMARY KEY (id);


--
-- Name: install install_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.install
    ADD CONSTRAINT install_pkey PRIMARY KEY (name);


--
-- Name: inventory inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (serial);


--
-- Name: links links_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.links
    ADD CONSTRAINT links_pkey PRIMARY KEY (id);


--
-- Name: locations locations_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: monimap monimap_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.monimap
    ADD CONSTRAINT monimap_pkey PRIMARY KEY (id);


--
-- Name: monitoring monitoring_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.monitoring
    ADD CONSTRAINT monitoring_pkey PRIMARY KEY (name);


--
-- Name: nbrtrack nbrtrack_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.nbrtrack
    ADD CONSTRAINT nbrtrack_pkey PRIMARY KEY (id);


--
-- Name: policies policies_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT policies_pkey PRIMARY KEY (id);


--
-- Name: system system_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.system
    ADD CONSTRAINT system_pkey PRIMARY KEY (name);


--
-- Name: translations translations_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.translations
    ADD CONSTRAINT translations_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: nedi
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (usrname);


--
-- Name: cables_cblopt; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX cables_cblopt ON public.cables USING btree (cblopt);


--
-- Name: cables_jack; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX cables_jack ON public.cables USING btree (jack);


--
-- Name: cables_nbrjack; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX cables_nbrjack ON public.cables USING btree (nbrjack);


--
-- Name: cables_nbrpanel; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX cables_nbrpanel ON public.cables USING btree (nbrpanel);


--
-- Name: cables_panel; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX cables_panel ON public.cables USING btree (panel);


--
-- Name: chat_time; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX chat_time ON public.chat USING btree ("time");


--
-- Name: chat_usrname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX chat_usrname ON public.chat USING btree (usrname);


--
-- Name: devices_contact; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX devices_contact ON public.devices USING btree (contact);


--
-- Name: devices_location; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX devices_location ON public.devices USING btree (location);


--
-- Name: dns6_aaaaname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX dns6_aaaaname ON public.dns6 USING btree (aaaaname);


--
-- Name: dns6_nodip6; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX dns6_nodip6 ON public.dns6 USING btree (nodip6);


--
-- Name: dns_aname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX dns_aname ON public.dns USING btree (aname);


--
-- Name: dns_nodip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX dns_nodip ON public.dns USING btree (nodip);


--
-- Name: events_class; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX events_class ON public.events USING btree (class);


--
-- Name: events_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX events_device ON public.events USING btree (device);


--
-- Name: events_level; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX events_level ON public.events USING btree (level);


--
-- Name: events_source; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX events_source ON public.events USING btree (source);


--
-- Name: events_time; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX events_time ON public.events USING btree ("time");


--
-- Name: iftrack_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iftrack_device ON public.iftrack USING btree (device);


--
-- Name: iftrack_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iftrack_mac ON public.iftrack USING btree (mac);


--
-- Name: iftrack_vlanid; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iftrack_vlanid ON public.iftrack USING btree (vlanid);


--
-- Name: incidents_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX incidents_device ON public.incidents USING btree (device);


--
-- Name: incidents_name; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX incidents_name ON public.incidents USING btree (name);


--
-- Name: interfaces_device_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX interfaces_device_ifname ON public.interfaces USING btree (device, ifname);


--
-- Name: interfaces_ifidx; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX interfaces_ifidx ON public.interfaces USING btree (ifidx);


--
-- Name: interfaces_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX interfaces_ifname ON public.interfaces USING btree (ifname);


--
-- Name: iptrack_arpdevice; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iptrack_arpdevice ON public.iptrack USING btree (arpdevice);


--
-- Name: iptrack_arpifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iptrack_arpifname ON public.iptrack USING btree (arpifname);


--
-- Name: iptrack_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX iptrack_mac ON public.iptrack USING btree (mac);


--
-- Name: links_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX links_device ON public.links USING btree (device);


--
-- Name: links_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX links_ifname ON public.links USING btree (ifname);


--
-- Name: links_nbrifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX links_nbrifname ON public.links USING btree (nbrifname);


--
-- Name: links_neighbor; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX links_neighbor ON public.links USING btree (neighbor);


--
-- Name: locations_region; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX locations_region ON public.locations USING btree (region);


--
-- Name: modules_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX modules_device ON public.modules USING btree (device);


--
-- Name: modules_serial; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX modules_serial ON public.modules USING btree (serial);


--
-- Name: monimap_usrname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX monimap_usrname ON public.monimap USING btree (usrname);


--
-- Name: monitoring_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX monitoring_device ON public.monitoring USING btree (device);


--
-- Name: nbrtrack_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nbrtrack_device ON public.nbrtrack USING btree (device);


--
-- Name: nbrtrack_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nbrtrack_ifname ON public.nbrtrack USING btree (ifname);


--
-- Name: nbrtrack_neighbor; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nbrtrack_neighbor ON public.nbrtrack USING btree (neighbor);


--
-- Name: nbrtrack_time; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nbrtrack_time ON public.nbrtrack USING btree ("time");


--
-- Name: netinfo_country; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX netinfo_country ON public.netinfo USING btree (country);


--
-- Name: netinfo_netip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX netinfo_netip ON public.netinfo USING btree (netip);


--
-- Name: networks_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX networks_device ON public.networks USING btree (device);


--
-- Name: networks_ifip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX networks_ifip ON public.networks USING btree (ifip);


--
-- Name: networks_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX networks_ifname ON public.networks USING btree (ifname);


--
-- Name: nodarp_arpdevice; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodarp_arpdevice ON public.nodarp USING btree (arpdevice);


--
-- Name: nodarp_arpifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodarp_arpifname ON public.nodarp USING btree (arpifname);


--
-- Name: nodarp_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodarp_mac ON public.nodarp USING btree (mac);


--
-- Name: nodarp_nodip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodarp_nodip ON public.nodarp USING btree (nodip);


--
-- Name: nodes_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodes_device ON public.nodes USING btree (device);


--
-- Name: nodes_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodes_ifname ON public.nodes USING btree (ifname);


--
-- Name: nodes_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodes_mac ON public.nodes USING btree (mac);


--
-- Name: nodes_noduser; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodes_noduser ON public.nodes USING btree (noduser);


--
-- Name: nodes_vlanid; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodes_vlanid ON public.nodes USING btree (vlanid);


--
-- Name: nodnd_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodnd_mac ON public.nodnd USING btree (mac);


--
-- Name: nodnd_nddevice; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodnd_nddevice ON public.nodnd USING btree (nddevice);


--
-- Name: nodnd_ndifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodnd_ndifname ON public.nodnd USING btree (ndifname);


--
-- Name: nodnd_nodip6; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX nodnd_nodip6 ON public.nodnd USING btree (nodip6);


--
-- Name: policies_class; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX policies_class ON public.policies USING btree (class);


--
-- Name: policies_status; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX policies_status ON public.policies USING btree (status);


--
-- Name: routes_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX routes_device ON public.routes USING btree (device);


--
-- Name: routes_dstip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX routes_dstip ON public.routes USING btree (dstip);


--
-- Name: routes_nhip; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX routes_nhip ON public.routes USING btree (nhip);


--
-- Name: translations_srctype; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX translations_srctype ON public.translations USING btree (srctype);


--
-- Name: vlanport_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlanport_device ON public.vlanport USING btree (device);


--
-- Name: vlanport_ifname; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlanport_ifname ON public.vlanport USING btree (ifname);


--
-- Name: vlanport_vlanid; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlanport_vlanid ON public.vlanport USING btree (vlanid);


--
-- Name: vlanport_vlopts; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlanport_vlopts ON public.vlanport USING btree (vlopts);


--
-- Name: vlans_device; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlans_device ON public.vlans USING btree (device);


--
-- Name: vlans_vlanid; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX vlans_vlanid ON public.vlans USING btree (vlanid);


--
-- Name: wlan_mac; Type: INDEX; Schema: public; Owner: nedi
--

CREATE INDEX wlan_mac ON public.wlan USING btree (mac);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

