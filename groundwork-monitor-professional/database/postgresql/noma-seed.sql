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
-- Data for Name: contactgroups; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY contactgroups (id, name_short, name, view_only, timeframe_id, timezone_id) FROM stdin;
1	group1	Group 1	0	1	305
\.


--
-- Name: contactgroups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('contactgroups_id_seq', 1, true);


--
-- Data for Name: contactgroups_to_contacts; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY contactgroups_to_contacts (contactgroup_id, contact_id) FROM stdin;
1	2
\.


--
-- Data for Name: contacts; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY contacts (id, admin, username, full_name, email, phone, mobile, section, growladdress, password, timeframe_id, timezone_id, restrict_alerts) FROM stdin;
1	0	[---]								0	0	\N
2	1	nagiosadmin	Nagios Administrator	nagios@localhost				192.168.1.109	9e2b1592bd13bea759dab1e3011cab7ef47930cd	1	0	0
\.


--
-- Name: contacts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('contacts_id_seq', 2, true);


--
-- Data for Name: escalation_stati; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY escalation_stati (id, notification_rule, starttime, counter, incident_id, recipients, host, host_alias, host_address, hostgroups, service, servicegroups, check_type, status, time_string, type, authors, comments, output) FROM stdin;
\.


--
-- Name: escalation_stati_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('escalation_stati_id_seq', 1, false);


--
-- Data for Name: escalations_contacts; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY escalations_contacts (id, notification_id, on_ok, on_warning, on_critical, on_unknown, on_host_up, on_host_unreachable, on_host_down, on_type_problem, on_type_recovery, on_type_flappingstart, on_type_flappingstop, on_type_flappingdisabled, on_type_downtimestart, on_type_downtimeend, on_type_downtimecancelled, on_type_acknowledgement, on_type_custom, notify_after_tries) FROM stdin;
\.


--
-- Name: escalations_contacts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('escalations_contacts_id_seq', 1, false);


--
-- Data for Name: escalations_contacts_to_contactgroups; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY escalations_contacts_to_contactgroups (escalation_contacts_id, contactgroup_id) FROM stdin;
\.


--
-- Data for Name: escalations_contacts_to_contacts; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY escalations_contacts_to_contacts (escalation_contacts_id, contacts_id) FROM stdin;
\.


--
-- Data for Name: escalations_contacts_to_methods; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY escalations_contacts_to_methods (escalation_contacts_id, method_id) FROM stdin;
\.


--
-- Data for Name: holidays; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY holidays (id, holiday_name, timeframe_id, contact_id, holiday_start, holiday_end) FROM stdin;
\.


--
-- Name: holidays_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('holidays_id_seq', 1, false);


--
-- Data for Name: information; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY information (id, type, content) FROM stdin;
0	dbversion	2000
\.


--
-- Data for Name: notification_logs; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notification_logs (id, "timestamp", counter, check_type, check_result, host, service, notification_type, method, "user", result, unique_id, incident_id, notification_rule, last_method) FROM stdin;
1	2015-01-15 07:18:37	1	(internal)	OK	localhost	NoMa	(none)	(none)	NoMa	NoMa successfully installed	123565999600001	123565999600001	0	1
\.


--
-- Name: notification_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('notification_logs_id_seq', 1, true);


--
-- Data for Name: notification_methods; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notification_methods (id, method, command, contact_field, sender, on_fail, ack_able) FROM stdin;
1	E-Mail	sendemail	email	root@localhost	0	0
2	SMS	sendsms	mobile		0	0
3	Voice	voicecall	phone		0	1
4	Voice + E-Mail fallback	voicecall	phone		1	1
5	Voice + SMS fallback	voicecall	phone		2	1
6	Growl	growl	growladdress		0	0
\.


--
-- Name: notification_methods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('notification_methods_id_seq', 6, true);


--
-- Data for Name: notification_stati; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notification_stati (id, host, service, check_type, check_result, counter, pid) FROM stdin;
\.


--
-- Name: notification_stati_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('notification_stati_id_seq', 1, false);


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notifications (id, notification_name, notification_description, active, username, recipients_include, recipients_exclude, hosts_include, hosts_exclude, hostgroups_include, hostgroups_exclude, services_include, services_exclude, servicegroups_include, servicegroups_exclude, notify_after_tries, let_notifier_handle, rollover, reloop_delay, on_ok, on_warning, on_unknown, on_host_unreachable, on_critical, on_host_up, on_host_down, on_type_problem, on_type_recovery, on_type_flappingstart, on_type_flappingstop, on_type_flappingdisabled, on_type_downtimestart, on_type_downtimeend, on_type_downtimecancelled, on_type_acknowledgement, on_type_custom, timezone_id, timeframe_id) FROM stdin;
1	default	default rule	1	nagiosadmin			*		*		*		*		1	0	1	0	1	1	1	1	1	1	1	1	1	1	1	1	1	1	1	1	1	0	1
\.


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('notifications_id_seq', 1, true);


--
-- Data for Name: notifications_to_contactgroups; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notifications_to_contactgroups (notification_id, contactgroup_id) FROM stdin;
\.


--
-- Data for Name: notifications_to_contacts; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notifications_to_contacts (notification_id, contact_id) FROM stdin;
1	2
\.


--
-- Data for Name: notifications_to_methods; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY notifications_to_methods (notification_id, method_id) FROM stdin;
1	1
\.


--
-- Data for Name: timeframes; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY timeframes (id, timeframe_name, dt_validfrom, dt_validto, day_monday_all, day_monday_1st, day_monday_2nd, day_monday_3rd, day_monday_4th, day_monday_5th, day_monday_last, day_tuesday_all, day_tuesday_1st, day_tuesday_2nd, day_tuesday_3rd, day_tuesday_4th, day_tuesday_5th, day_tuesday_last, day_wednesday_all, day_wednesday_1st, day_wednesday_2nd, day_wednesday_3rd, day_wednesday_4th, day_wednesday_5th, day_wednesday_last, day_thursday_all, day_thursday_1st, day_thursday_2nd, day_thursday_3rd, day_thursday_4th, day_thursday_5th, day_thursday_last, day_friday_all, day_friday_1st, day_friday_2nd, day_friday_3rd, day_friday_4th, day_friday_5th, day_friday_last, day_saturday_all, day_saturday_1st, day_saturday_2nd, day_saturday_3rd, day_saturday_4th, day_saturday_5th, day_saturday_last, day_sunday_all, day_sunday_1st, day_sunday_2nd, day_sunday_3rd, day_sunday_4th, day_sunday_5th, day_sunday_last, time_monday_start, time_monday_stop, time_monday_invert, time_tuesday_start, time_tuesday_stop, time_tuesday_invert, time_wednesday_start, time_wednesday_stop, time_wednesday_invert, time_thursday_start, time_thursday_stop, time_thursday_invert, time_friday_start, time_friday_stop, time_friday_invert, time_saturday_start, time_saturday_stop, time_saturday_invert, time_sunday_start, time_sunday_stop, time_sunday_invert) FROM stdin;
1	24x7	2011-08-01 00:00:00	2021-12-31 23:59:59	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	00:00:00	00:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1
2	workhours	2011-08-01 00:00:00	2021-12-31 23:59:59	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	08:00:00	16:00:00	0	08:00:00	16:00:00	0	08:00:00	16:00:00	0	08:00:00	16:00:00	0	08:00:00	16:00:00	0	00:00:00	00:00:00	0	00:00:00	00:00:00	0
3	outside workhours	2011-08-01 00:00:00	2021-12-31 23:59:59	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	1	0	0	0	0	0	0	08:00:00	16:00:00	1	08:00:00	16:00:00	1	08:00:00	16:00:00	1	08:00:00	16:00:00	1	08:00:00	16:00:00	1	00:00:00	00:00:00	1	00:00:00	00:00:00	1
\.


--
-- Name: timeframes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('timeframes_id_seq', 3, true);


--
-- Data for Name: timezones; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY timezones (id, timezone, time_diff) FROM stdin;
0	GMT	0
1	Africa/Abidjan	0
2	Africa/Accra	0
3	Africa/Addis_Ababa	3
4	Africa/Algiers	1
5	Africa/Asmera	3
6	Africa/Bamako	0
7	Africa/Bangui	1
8	Africa/Banjul	0
9	Africa/Bissau	0
10	Africa/Blantyre	2
11	Africa/Brazzaville	1
12	Africa/Bujumbura	2
13	Africa/Cairo	2
14	Africa/Casablanca	0
15	Africa/Ceuta	1
16	Africa/Conakry	0
17	Africa/Dakar	0
18	Africa/Dar_es_Salaam	3
19	Africa/Djibouti	3
20	Africa/Douala	1
21	Africa/El_Aaiun	0
22	Africa/Freetown	0
23	Africa/Gaborone	2
24	Africa/Harare	2
25	Africa/Johannesburg	2
26	Africa/Kampala	3
27	Africa/Khartoum	3
28	Africa/Kigali	2
29	Africa/Kinshasa	1
30	Africa/Lagos	1
31	Africa/Libreville	1
32	Africa/Lome	0
33	Africa/Luanda	1
34	Africa/Lubumbashi	2
35	Africa/Lusaka	2
36	Africa/Malabo	1
37	Africa/Maputo	2
38	Africa/Maseru	2
39	Africa/Mbabane	2
40	Africa/Mogadishu	3
41	Africa/Monrovia	0
42	Africa/Nairobi	3
43	Africa/Ndjamena	1
44	Africa/Niamey	1
45	Africa/Nouakchott	0
46	Africa/Ouagadougou	0
47	Africa/Porto-Novo	1
48	Africa/Sao_Tome	0
49	Africa/Tripoli	2
50	Africa/Tunis	1
51	Africa/Windhoek	2
52	America/Adak	-10
53	America/Anchorage	-9
54	America/Anguilla	-4
55	America/Antigua	-4
56	America/Araguaina	-3
57	America/Argentina/Buenos_Aires	-3
58	America/Argentina/Catamarca	-3
59	America/Argentina/Cordoba	-3
60	America/Argentina/Jujuy	-3
61	America/Argentina/La_Rioja	-3
62	America/Argentina/Mendoza	-3
63	America/Argentina/Rio_Gallegos	-3
64	America/Argentina/San_Juan	-3
65	America/Argentina/Tucuman	-3
66	America/Argentina/Ushuaia	-3
67	America/Aruba	-4
68	America/Asuncion	-3
69	America/Bahia	-3
70	America/Barbados	-4
71	America/Belem	-3
72	America/Belize	-6
73	America/Boa_Vista	-4
74	America/Bogota	-5
75	America/Boise	-7
76	America/Cambridge_Bay	-7
77	America/Campo_Grande	-3
78	America/Cancun	-6
79	America/Caracas	-4
80	America/Cayenne	-3
81	America/Cayman	-5
82	America/Chicago	-6
83	America/Chihuahua	-7
84	America/Coral_Harbour	-5
85	America/Costa_Rica	-6
86	America/Cuiaba	-3
87	America/Curacao	-4
88	America/Danmarkshavn	0
89	America/Dawson	-8
90	America/Dawson_Creek	-7
91	America/Denver	-7
92	America/Detroit	-5
93	America/Dominica	-4
94	America/Edmonton	-7
95	America/Eirunepe	-5
96	America/El_Salvador	-6
97	America/Fortaleza	-3
98	America/Glace_Bay	-4
99	America/Godthab	-3
100	America/Goose_Bay	-4
101	America/Grand_Turk	-5
102	America/Grenada	-4
103	America/Guadeloupe	-4
104	America/Guatemala	-6
105	America/Guayaquil	-5
106	America/Guyana	-4
107	America/Halifax	-4
108	America/Havana	-5
109	America/Hermosillo	-7
110	America/Indiana/Indianapolis	-5
111	America/Indiana/Knox	-6
112	America/Indiana/Marengo	-5
113	America/Indiana/Vevay	-5
114	America/Inuvik	-7
115	America/Iqaluit	-5
116	America/Jamaica	-5
117	America/Juneau	-9
118	America/Kentucky/Louisville	-5
119	America/Kentucky/Monticello	-5
120	America/La_Paz	-4
121	America/Lima	-5
122	America/Los_Angeles	-8
123	America/Maceio	-3
124	America/Managua	-6
125	America/Manaus	-4
126	America/Martinique	-4
127	America/Mazatlan	-7
128	America/Menominee	-6
129	America/Merida	-6
130	America/Mexico_City	-6
131	America/Miquelon	-3
132	America/Monterrey	-6
133	America/Montevideo	-2
134	America/Montreal	-5
135	America/Montserrat	-4
136	America/Nassau	-5
137	America/New_York	-5
138	America/Nipigon	-5
139	America/Nome	-9
140	America/Noronha	-2
141	America/North_Dakota/Center	-6
142	America/Panama	-5
143	America/Pangnirtung	-5
144	America/Paramaribo	-3
145	America/Phoenix	-7
146	America/Port-au-Prince	-5
147	America/Port_of_Spain	-4
148	America/Porto_Velho	-4
149	America/Puerto_Rico	-4
150	America/Rainy_River	-6
151	America/Rankin_Inlet	-6
152	America/Recife	-3
153	America/Regina	-6
154	America/Rio_Branco	-5
155	America/Santiago	-3
156	America/Santo_Domingo	-4
157	America/Sao_Paulo	-2
158	America/Scoresbysund	-1
159	America/St_Johns	-3
160	America/St_Kitts	-4
161	America/St_Lucia	-4
162	America/St_Thomas	-4
163	America/St_Vincent	-4
164	America/Swift_Current	-6
165	America/Tegucigalpa	-6
166	America/Thule	-4
167	America/Thunder_Bay	-5
168	America/Tijuana	-8
169	America/Toronto	-5
170	America/Tortola	-4
171	America/Vancouver	-8
172	America/Whitehorse	-8
173	America/Winnipeg	-6
174	America/Yakutat	-9
175	America/Yellowknife	-7
176	Antarctica/Casey	8
177	Antarctica/Davis	7
178	Antarctica/DumontDUrville	10
179	Antarctica/Mawson	6
180	Antarctica/McMurdo	13
181	Antarctica/Palmer	-3
182	Antarctica/Rothera	-3
183	Antarctica/Syowa	3
184	Antarctica/Vostok	6
185	Asia/Aden	3
186	Asia/Almaty	6
187	Asia/Amman	2
188	Asia/Anadyr	12
189	Asia/Aqtau	5
190	Asia/Aqtobe	5
191	Asia/Ashgabat	5
192	Asia/Baghdad	3
193	Asia/Bahrain	3
194	Asia/Baku	4
195	Asia/Bangkok	7
196	Asia/Beirut	2
197	Asia/Bishkek	6
198	Asia/Brunei	8
199	Asia/Calcutta	5
200	Asia/Choibalsan	9
201	Asia/Chongqing	8
202	Asia/Colombo	5
203	Asia/Damascus	2
204	Asia/Dhaka	6
205	Asia/Dili	9
206	Asia/Dubai	4
207	Asia/Dushanbe	5
208	Asia/Gaza	2
209	Asia/Harbin	8
210	Asia/Hong_Kong	8
211	Asia/Hovd	7
212	Asia/Irkutsk	8
213	Asia/Jakarta	7
214	Asia/Jayapura	9
215	Asia/Jerusalem	2
216	Asia/Kabul	4
217	Asia/Kamchatka	12
218	Asia/Karachi	5
219	Asia/Kashgar	8
220	Asia/Katmandu	5
221	Asia/Krasnoyarsk	7
222	Asia/Kuala_Lumpur	8
223	Asia/Kuching	8
224	Asia/Kuwait	3
225	Asia/Macau	8
226	Asia/Magadan	11
227	Asia/Makassar	8
228	Asia/Manila	8
229	Asia/Muscat	4
230	Asia/Nicosia	2
231	Asia/Novosibirsk	6
232	Asia/Omsk	6
233	Asia/Oral	5
234	Asia/Phnom_Penh	7
235	Asia/Pontianak	7
236	Asia/Pyongyang	9
237	Asia/Qatar	3
238	Asia/Qyzylorda	6
239	Asia/Rangoon	6
240	Asia/Riyadh	3
241	Asia/Saigon	7
242	Asia/Sakhalin	10
243	Asia/Samarkand	5
244	Asia/Seoul	9
245	Asia/Shanghai	8
246	Asia/Singapore	8
247	Asia/Taipei	8
248	Asia/Tashkent	5
249	Asia/Tbilisi	4
250	Asia/Tehran	3
251	Asia/Thimphu	6
252	Asia/Tokyo	9
253	Asia/Ulaanbaatar	8
254	Asia/Urumqi	8
255	Asia/Vientiane	7
256	Asia/Vladivostok	10
257	Asia/Yakutsk	9
258	Asia/Yekaterinburg	5
259	Asia/Yerevan	4
260	Atlantic/Azores	-1
261	Atlantic/Bermuda	-4
262	Atlantic/Canary	0
263	Atlantic/Cape_Verde	-1
264	Atlantic/Faeroe	0
265	Atlantic/Madeira	0
266	Atlantic/Reykjavik	0
267	Atlantic/South_Georgia	-2
268	Atlantic/St_Helena	0
269	Atlantic/Stanley	-3
270	Australia/Adelaide	10
271	Australia/Brisbane	10
272	Australia/Broken_Hill	10
273	Australia/Currie	11
274	Australia/Darwin	9
275	Australia/Hobart	11
276	Australia/Lindeman	10
277	Australia/Lord_Howe	11
278	Australia/Melbourne	11
279	Australia/Perth	8
280	Australia/Sydney	11
281	Europe/Amsterdam	1
282	Europe/Andorra	1
283	Europe/Athens	2
284	Europe/Belgrade	1
285	Europe/Berlin	1
286	Europe/Brussels	1
287	Europe/Bucharest	2
288	Europe/Budapest	1
289	Europe/Chisinau	2
290	Europe/Copenhagen	1
291	Europe/Dublin	0
292	Europe/Gibraltar	1
293	Europe/Helsinki	2
294	Europe/Istanbul	2
295	Europe/Kaliningrad	2
296	Europe/Kiev	2
297	Europe/Lisbon	0
298	Europe/London	0
299	Europe/Luxembourg	1
300	Europe/Madrid	1
301	Europe/Malta	1
302	Europe/Minsk	2
303	Europe/Monaco	1
304	Europe/Moscow	3
305	Europe/Oslo	1
306	Europe/Paris	1
307	Europe/Prague	1
308	Europe/Riga	2
309	Europe/Rome	1
310	Europe/Samara	4
311	Europe/Simferopol	2
312	Europe/Sofia	2
313	Europe/Stockholm	1
314	Europe/Tallinn	2
315	Europe/Tirane	1
316	Europe/Uzhgorod	2
317	Europe/Vaduz	1
318	Europe/Vienna	1
319	Europe/Vilnius	2
320	Europe/Warsaw	1
321	Europe/Zaporozhye	2
322	Europe/Zurich	1
323	Indian/Antananarivo	3
324	Indian/Chagos	6
325	Indian/Christmas	7
326	Indian/Cocos	6
327	Indian/Comoro	3
328	Indian/Kerguelen	5
329	Indian/Mahe	4
330	Indian/Maldives	5
331	Indian/Mauritius	4
332	Indian/Mayotte	3
333	Indian/Reunion	4
334	Pacific/Apia	-11
335	Pacific/Auckland	13
336	Pacific/Chatham	13
337	Pacific/Easter	-5
338	Pacific/Efate	11
339	Pacific/Enderbury	13
340	Pacific/Fakaofo	-10
341	Pacific/Fiji	12
342	Pacific/Funafuti	12
343	Pacific/Galapagos	-6
344	Pacific/Gambier	-9
345	Pacific/Guadalcanal	11
346	Pacific/Guam	10
347	Pacific/Honolulu	-10
348	Pacific/Johnston	-10
349	Pacific/Kiritimati	14
350	Pacific/Kosrae	11
351	Pacific/Kwajalein	12
352	Pacific/Majuro	12
353	Pacific/Marquesas	-9
354	Pacific/Midway	-11
355	Pacific/Nauru	12
356	Pacific/Niue	-11
357	Pacific/Norfolk	11
358	Pacific/Noumea	11
359	Pacific/Pago_Pago	-11
360	Pacific/Palau	9
361	Pacific/Pitcairn	-8
362	Pacific/Ponape	11
363	Pacific/Port_Moresby	10
364	Pacific/Rarotonga	-10
365	Pacific/Saipan	10
366	Pacific/Tahiti	-10
367	Pacific/Tarawa	12
368	Pacific/Tongatapu	13
369	Pacific/Truk	10
370	Pacific/Wake	12
371	Pacific/Wallis	12
\.


--
-- Data for Name: tmp_commands; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY tmp_commands (id, operation, external_id, recipients, host, host_alias, host_address, hostgroups, service, servicegroups, check_type, status, stime, notification_type, authors, comments, output) FROM stdin;
\.


--
-- Data for Name: tmp_active; Type: TABLE DATA; Schema: public; Owner: noma
--

COPY tmp_active (id, notify_id, command_id, dest, from_user, time_string, "user", method, notify_cmd, retries, rule, progress, esc_flag, bundled, stime) FROM stdin;
\.


--
-- Name: tmp_active_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('tmp_active_id_seq', 1, false);


--
-- Name: tmp_commands_id_seq; Type: SEQUENCE SET; Schema: public; Owner: noma
--

SELECT pg_catalog.setval('tmp_commands_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

