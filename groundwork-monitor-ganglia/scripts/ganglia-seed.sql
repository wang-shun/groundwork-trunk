--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: cluster_clusterid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('cluster_clusterid_seq', 1, true);


--
-- Name: clusterhost_clusterhostid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('clusterhost_clusterhostid_seq', 1, false);


--
-- Name: host_hostid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('host_hostid_seq', 1, true);


--
-- Name: hostinstance_hostinstanceid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('hostinstance_hostinstanceid_seq', 1, false);


--
-- Name: location_locationid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('location_locationid_seq', 1, true);


--
-- Name: metric_metricid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('metric_metricid_seq', 1, false);


--
-- Name: metricinstance_metricinstanceid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('metricinstance_metricinstanceid_seq', 1, false);


--
-- Name: metricvalue_metricvalueid_seq; Type: SEQUENCE SET; Schema: public; Owner: ganglia
--

SELECT pg_catalog.setval('metricvalue_metricvalueid_seq', 1, false);


--
-- Data for Name: cluster; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY cluster (clusterid, name, description, regex) FROM stdin;
1	Default	Default definitions for all clusters.	0
\.


--
-- Data for Name: host; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY host (hostid, name, ipaddress, description, regex) FROM stdin;
1	Default		Default definitions for all hosts.	0
\.


--
-- Data for Name: clusterhost; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY clusterhost (clusterhostid, clusterid, hostid) FROM stdin;
\.


--
-- Data for Name: location; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY location (locationid, name, description, regex) FROM stdin;
1	Default	Default definitions for all locations	0
\.


--
-- Data for Name: hostinstance; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY hostinstance (hostinstanceid, clusterid, hostid, locationid) FROM stdin;
\.


--
-- Data for Name: metric; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY metric (metricid, name, description, units, critical, warning, duration) FROM stdin;
\.


--
-- Data for Name: metricinstance; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY metricinstance (metricinstanceid, hostinstanceid, metricid, description, laststate, lastupdatetime, laststatechangetime, lastvalue) FROM stdin;
\.


--
-- Data for Name: metricvalue; Type: TABLE DATA; Schema: public; Owner: ganglia
--

COPY metricvalue (metricvalueid, clusterid, hostid, locationid, metricid, description, critical, warning, duration) FROM stdin;
\.


--
-- PostgreSQL database dump complete
--

