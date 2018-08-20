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
-- Name: cluster; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE cluster (
    clusterid integer NOT NULL,
    name text NOT NULL,
    description text,
    regex smallint
);


ALTER TABLE public.cluster OWNER TO ganglia;

--
-- Name: cluster_clusterid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE cluster_clusterid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cluster_clusterid_seq OWNER TO ganglia;

--
-- Name: cluster_clusterid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE cluster_clusterid_seq OWNED BY cluster.clusterid;


--
-- Name: clusterhost; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE clusterhost (
    clusterhostid integer NOT NULL,
    clusterid integer NOT NULL,
    hostid integer NOT NULL
);


ALTER TABLE public.clusterhost OWNER TO ganglia;

--
-- Name: clusterhost_clusterhostid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE clusterhost_clusterhostid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.clusterhost_clusterhostid_seq OWNER TO ganglia;

--
-- Name: clusterhost_clusterhostid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE clusterhost_clusterhostid_seq OWNED BY clusterhost.clusterhostid;


--
-- Name: host; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE host (
    hostid integer NOT NULL,
    name text NOT NULL,
    ipaddress character varying(45),
    description text,
    regex smallint
);


ALTER TABLE public.host OWNER TO ganglia;

--
-- Name: host_hostid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE host_hostid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.host_hostid_seq OWNER TO ganglia;

--
-- Name: host_hostid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE host_hostid_seq OWNED BY host.hostid;


--
-- Name: hostinstance; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE hostinstance (
    hostinstanceid integer NOT NULL,
    clusterid integer NOT NULL,
    hostid integer NOT NULL,
    locationid integer NOT NULL
);


ALTER TABLE public.hostinstance OWNER TO ganglia;

--
-- Name: hostinstance_hostinstanceid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE hostinstance_hostinstanceid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hostinstance_hostinstanceid_seq OWNER TO ganglia;

--
-- Name: hostinstance_hostinstanceid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE hostinstance_hostinstanceid_seq OWNED BY hostinstance.hostinstanceid;


--
-- Name: location; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE location (
    locationid integer NOT NULL,
    name text NOT NULL,
    description text,
    regex smallint
);


ALTER TABLE public.location OWNER TO ganglia;

--
-- Name: location_locationid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE location_locationid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.location_locationid_seq OWNER TO ganglia;

--
-- Name: location_locationid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE location_locationid_seq OWNED BY location.locationid;


--
-- Name: metric; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE metric (
    metricid integer NOT NULL,
    name text NOT NULL,
    description text,
    units character varying(45),
    critical numeric(64,10),
    warning numeric(64,10),
    duration numeric(64,10)
);


ALTER TABLE public.metric OWNER TO ganglia;

--
-- Name: metric_metricid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE metric_metricid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metric_metricid_seq OWNER TO ganglia;

--
-- Name: metric_metricid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE metric_metricid_seq OWNED BY metric.metricid;


--
-- Name: metricinstance; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE metricinstance (
    metricinstanceid integer NOT NULL,
    hostinstanceid integer NOT NULL,
    metricid integer NOT NULL,
    description text,
    laststate text,
    lastupdatetime integer NOT NULL,
    laststatechangetime integer NOT NULL,
    lastvalue text
);


ALTER TABLE public.metricinstance OWNER TO ganglia;

--
-- Name: metricinstance_metricinstanceid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE metricinstance_metricinstanceid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metricinstance_metricinstanceid_seq OWNER TO ganglia;

--
-- Name: metricinstance_metricinstanceid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE metricinstance_metricinstanceid_seq OWNED BY metricinstance.metricinstanceid;


--
-- Name: metricvalue; Type: TABLE; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE TABLE metricvalue (
    metricvalueid integer NOT NULL,
    clusterid integer NOT NULL,
    hostid integer NOT NULL,
    locationid integer NOT NULL,
    metricid integer NOT NULL,
    description text,
    critical numeric(64,10),
    warning numeric(64,10),
    duration numeric(64,10)
);


ALTER TABLE public.metricvalue OWNER TO ganglia;

--
-- Name: metricvalue_metricvalueid_seq; Type: SEQUENCE; Schema: public; Owner: ganglia
--

CREATE SEQUENCE metricvalue_metricvalueid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metricvalue_metricvalueid_seq OWNER TO ganglia;

--
-- Name: metricvalue_metricvalueid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ganglia
--

ALTER SEQUENCE metricvalue_metricvalueid_seq OWNED BY metricvalue.metricvalueid;


--
-- Name: clusterid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE cluster ALTER COLUMN clusterid SET DEFAULT nextval('cluster_clusterid_seq'::regclass);


--
-- Name: clusterhostid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE clusterhost ALTER COLUMN clusterhostid SET DEFAULT nextval('clusterhost_clusterhostid_seq'::regclass);


--
-- Name: hostid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE host ALTER COLUMN hostid SET DEFAULT nextval('host_hostid_seq'::regclass);


--
-- Name: hostinstanceid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE hostinstance ALTER COLUMN hostinstanceid SET DEFAULT nextval('hostinstance_hostinstanceid_seq'::regclass);


--
-- Name: locationid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE location ALTER COLUMN locationid SET DEFAULT nextval('location_locationid_seq'::regclass);


--
-- Name: metricid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE metric ALTER COLUMN metricid SET DEFAULT nextval('metric_metricid_seq'::regclass);


--
-- Name: metricinstanceid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE metricinstance ALTER COLUMN metricinstanceid SET DEFAULT nextval('metricinstance_metricinstanceid_seq'::regclass);


--
-- Name: metricvalueid; Type: DEFAULT; Schema: public; Owner: ganglia
--

ALTER TABLE metricvalue ALTER COLUMN metricvalueid SET DEFAULT nextval('metricvalue_metricvalueid_seq'::regclass);


--
-- Name: cluster_name_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY cluster
    ADD CONSTRAINT cluster_name_key UNIQUE (name);


--
-- Name: cluster_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY cluster
    ADD CONSTRAINT cluster_pkey PRIMARY KEY (clusterid);


--
-- Name: clusterhost_hostid_clusterid_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY clusterhost
    ADD CONSTRAINT clusterhost_hostid_clusterid_key UNIQUE (hostid, clusterid);


--
-- Name: clusterhost_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY clusterhost
    ADD CONSTRAINT clusterhost_pkey PRIMARY KEY (clusterhostid);


--
-- Name: host_name_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_name_key UNIQUE (name);


--
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (hostid);


--
-- Name: hostinstance_hostid_clusterid_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY hostinstance
    ADD CONSTRAINT hostinstance_hostid_clusterid_key UNIQUE (hostid, clusterid);


--
-- Name: hostinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY hostinstance
    ADD CONSTRAINT hostinstance_pkey PRIMARY KEY (hostinstanceid);


--
-- Name: location_name_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_name_key UNIQUE (name);


--
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_pkey PRIMARY KEY (locationid);


--
-- Name: metric_name_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metric
    ADD CONSTRAINT metric_name_key UNIQUE (name);


--
-- Name: metric_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metric
    ADD CONSTRAINT metric_pkey PRIMARY KEY (metricid);


--
-- Name: metricinstance_hostinstanceid_metricid_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metricinstance
    ADD CONSTRAINT metricinstance_hostinstanceid_metricid_key UNIQUE (hostinstanceid, metricid);


--
-- Name: metricinstance_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metricinstance
    ADD CONSTRAINT metricinstance_pkey PRIMARY KEY (metricinstanceid);


--
-- Name: metricvalue_hostid_clusterid_metricid_key; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_hostid_clusterid_metricid_key UNIQUE (hostid, clusterid, metricid);


--
-- Name: metricvalue_pkey; Type: CONSTRAINT; Schema: public; Owner: ganglia; Tablespace: 
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_pkey PRIMARY KEY (metricvalueid);


--
-- Name: clusterhost_clusterhost_clusterfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX clusterhost_clusterhost_clusterfk ON clusterhost USING btree (clusterid);


--
-- Name: clusterhost_clusterhost_hostfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX clusterhost_clusterhost_hostfk ON clusterhost USING btree (hostid);


--
-- Name: hostinstance_hostinstance_clusterfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX hostinstance_hostinstance_clusterfk ON hostinstance USING btree (clusterid);


--
-- Name: hostinstance_hostinstance_hostfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX hostinstance_hostinstance_hostfk ON hostinstance USING btree (hostid);


--
-- Name: hostinstance_hostinstance_locationfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX hostinstance_hostinstance_locationfk ON hostinstance USING btree (locationid);


--
-- Name: metricinstance_metricinstance_hostinstancefk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricinstance_metricinstance_hostinstancefk ON metricinstance USING btree (hostinstanceid);


--
-- Name: metricinstance_metricinstance_metricfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricinstance_metricinstance_metricfk ON metricinstance USING btree (metricid);


--
-- Name: metricvalue_metricvalue_clusterfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricvalue_metricvalue_clusterfk ON metricvalue USING btree (clusterid);


--
-- Name: metricvalue_metricvalue_hostfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricvalue_metricvalue_hostfk ON metricvalue USING btree (hostid);


--
-- Name: metricvalue_metricvalue_locationfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricvalue_metricvalue_locationfk ON metricvalue USING btree (locationid);


--
-- Name: metricvalue_metricvalue_metricfk; Type: INDEX; Schema: public; Owner: ganglia; Tablespace: 
--

CREATE INDEX metricvalue_metricvalue_metricfk ON metricvalue USING btree (metricid);


--
-- Name: clusterhost_clusterfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY clusterhost
    ADD CONSTRAINT clusterhost_clusterfk FOREIGN KEY (clusterid) REFERENCES cluster(clusterid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: clusterhost_hostfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY clusterhost
    ADD CONSTRAINT clusterhost_hostfk FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: hostinstance_clusterfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY hostinstance
    ADD CONSTRAINT hostinstance_clusterfk FOREIGN KEY (clusterid) REFERENCES cluster(clusterid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: hostinstance_hostfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY hostinstance
    ADD CONSTRAINT hostinstance_hostfk FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: hostinstance_locationfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY hostinstance
    ADD CONSTRAINT hostinstance_locationfk FOREIGN KEY (locationid) REFERENCES location(locationid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricinstance_hostinstancefk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricinstance
    ADD CONSTRAINT metricinstance_hostinstancefk FOREIGN KEY (hostinstanceid) REFERENCES hostinstance(hostinstanceid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricinstance_metricfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricinstance
    ADD CONSTRAINT metricinstance_metricfk FOREIGN KEY (metricid) REFERENCES metric(metricid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricvalue_clusterfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_clusterfk FOREIGN KEY (clusterid) REFERENCES cluster(clusterid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricvalue_hostfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_hostfk FOREIGN KEY (hostid) REFERENCES host(hostid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricvalue_locationfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_locationfk FOREIGN KEY (locationid) REFERENCES location(locationid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: metricvalue_metricfk; Type: FK CONSTRAINT; Schema: public; Owner: ganglia
--

ALTER TABLE ONLY metricvalue
    ADD CONSTRAINT metricvalue_metricfk FOREIGN KEY (metricid) REFERENCES metric(metricid) ON UPDATE RESTRICT ON DELETE RESTRICT;


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

