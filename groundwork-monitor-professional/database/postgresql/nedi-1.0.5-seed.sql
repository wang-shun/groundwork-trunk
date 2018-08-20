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
-- Name: incidents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: nedi
--

SELECT pg_catalog.setval('incidents_id_seq', 1, false);


--
-- Name: links_id_seq; Type: SEQUENCE SET; Schema: public; Owner: nedi
--

SELECT pg_catalog.setval('links_id_seq', 1, false);


--
-- Name: locations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: nedi
--

SELECT pg_catalog.setval('locations_id_seq', 1, false);


--
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: nedi
--

SELECT pg_catalog.setval('messages_id_seq', 2, true);


--
-- Data for Name: chat; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY chat ("time", "user", message) FROM stdin;
\.


--
-- Data for Name: cisco_contracts; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY cisco_contracts (contract_number, service_level, contract_label, bill_to_name, address, city, state, zip_code, country, bill_to_contact, phone, email, site_id, site_name, site_address, address_line2, address_line3, site_city, site_state, site_zip, site_country, site_notes, site_label, site_contact, site_phone, site_email, product_number, serial_number, name_ip_address, description, product_type, begin_date, end_date, po_number, so_number) FROM stdin;
\.


--
-- Data for Name: configs; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY configs (device, config, changes, "time") FROM stdin;
\.


--
-- Data for Name: devdel; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY devdel (device, "user", "time") FROM stdin;
\.


--
-- Data for Name: devices; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY devices (name, ip, serial, type, firstseen, lastseen, services, description, os, bootimage, location, contact, vtpdomain, vtpmode, snmpversion, community, cliport, login, icon, origip, cpu, memcpu, temp, cusvalue, cuslabel, sysobjid, logalarm, flags) FROM stdin;
\.


--
-- Data for Name: iftrack; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY iftrack (mac, ifupdate, device, ifname, vlanid, ifmetric) FROM stdin;
\.


--
-- Data for Name: incidents; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY incidents (id, level, device, deps, firstseen, lastseen, who, "time", category, comment) FROM stdin;
\.


--
-- Data for Name: interfaces; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY interfaces (device, ifname, ifidx, linktype, type, mac, description, alias, status, speed, duplex, vlid, inoct, inerr, outoct, outerr, dinoct, dinerr, doutoct, douterr, comment) FROM stdin;
\.


--
-- Data for Name: iptrack; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY iptrack (mac, ipupdate, name, ip) FROM stdin;
\.


--
-- Data for Name: links; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY links (id, device, ifname, neighbour, nbrifname, bandwidth, type, power, nbrduplex, nbrvlanid) FROM stdin;
\.


--
-- Data for Name: locations; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY locations (id, region, city, building, x, y, comment) FROM stdin;
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY messages (id, level, "time", source, info) FROM stdin;
\.


--
-- Data for Name: modules; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY modules (device, slot, model, description, serial, hw, fw, sw, modidx) FROM stdin;
\.


--
-- Data for Name: monitoring; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY monitoring (device, status, depend, sms, mail, lastchk, uptime, lost, ok, delay) FROM stdin;
\.


--
-- Data for Name: networks; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY networks (device, ifname, ip, mask, vrfname, status) FROM stdin;
\.


--
-- Data for Name: nodes; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY nodes (name, ip, mac, oui, firstseen, lastseen, device, ifname, vlanid, ifmetric, ifupdate, ifchanges, ipupdate, ipchanges, iplost, arp, tcpports, udpports, os, type, osupdate) FROM stdin;
\.


--
-- Data for Name: stock; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY stock (serial, type, "user", "time", location, state, comment, lastseen, source) FROM stdin;
\.


--
-- Data for Name: stolen; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY stolen (name, ip, mac, device, ifname, who, "time") FROM stdin;
\.


--
-- Data for Name: system; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY system (name, value) FROM stdin;
nodlock	0
threads	2
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY "user" (name, password, groups, email, phone, "time", lastseen, comment, language, theme, volume, columns, msglimit, graphs) FROM stdin;
admin	21232f297a57a5a743894a0e4a801fc3	255	\N	\N	1325619638	\N	default admin	english	default	100	8	10	3
\.


--
-- Data for Name: vlans; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY vlans (device, vlanid, vlanname) FROM stdin;
\.


--
-- Data for Name: wlan; Type: TABLE DATA; Schema: public; Owner: nedi
--

COPY wlan (mac, "time") FROM stdin;
\.


--
-- PostgreSQL database dump complete
--

