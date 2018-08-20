-- Copyright (C) 2010-2018 RealStuff Informatik AG
--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.9
-- Dumped by pg_dump version 9.6.9

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: audittrail; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: audittrail_idaudittrail_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.audittrail_idaudittrail_seq', 1, false);


--
-- Data for Name: calendar; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.calendar VALUES (1, 'Switzerland', NULL);
INSERT INTO public.calendar VALUES (2, 'Germany', NULL);
INSERT INTO public.calendar VALUES (3, 'USA', NULL);
INSERT INTO public.calendar VALUES (4, 'France', NULL);
INSERT INTO public.calendar VALUES (5, 'Italy', NULL);
INSERT INTO public.calendar VALUES (6, 'England', NULL);
INSERT INTO public.calendar VALUES (7, 'Belgium', NULL);
INSERT INTO public.calendar VALUES (8, 'Empty - for 24x7', NULL);


--
-- Data for Name: timevacationsdays; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.timevacationsdays VALUES (1, '01', '01', 'New Year''s Day');
INSERT INTO public.timevacationsdays VALUES (2, '05', '01', 'Labour Day');
INSERT INTO public.timevacationsdays VALUES (3, '12', '25', 'Christmas Day');
INSERT INTO public.timevacationsdays VALUES (4, '12', '26', 'St Stephen''s Day');
INSERT INTO public.timevacationsdays VALUES (5, '11', '01', 'All Hallows');
INSERT INTO public.timevacationsdays VALUES (6, '07', '21', 'National Day (BE)');
INSERT INTO public.timevacationsdays VALUES (7, '08', '15', 'Assumption Day');
INSERT INTO public.timevacationsdays VALUES (8, '11', '11', 'Memorial Day (BE, FR)');
INSERT INTO public.timevacationsdays VALUES (9, '01', '06', 'Epiphany (IT, CH, DE)');
INSERT INTO public.timevacationsdays VALUES (10, '04', '25', 'Liberation Day (IT)');
INSERT INTO public.timevacationsdays VALUES (11, '04', '28', 'Sa Die De Sa Sardigna (IT)');
INSERT INTO public.timevacationsdays VALUES (12, '06', '02', 'Day of the republic (IT)');
INSERT INTO public.timevacationsdays VALUES (13, '12', '08', 'Feast of the immaculate conception');
INSERT INTO public.timevacationsdays VALUES (14, '05', '08', 'Day of victory 1945 (FR)');
INSERT INTO public.timevacationsdays VALUES (15, '07', '14', 'National Day (FR)');
INSERT INTO public.timevacationsdays VALUES (16, '01', '02', 'Berchtoldstag (CH)');
INSERT INTO public.timevacationsdays VALUES (17, '03', '19', 'Josefstag (CH)');
INSERT INTO public.timevacationsdays VALUES (18, '08', '01', 'Bundesfeier (CH)');
INSERT INTO public.timevacationsdays VALUES (19, '08', '08', 'Friedensfest (DE)');
INSERT INTO public.timevacationsdays VALUES (20, '10', '03', 'Day of german unity');
INSERT INTO public.timevacationsdays VALUES (21, '10', '31', 'Reformation Day (DE)');
INSERT INTO public.timevacationsdays VALUES (22, '11', '11', 'Veterans day (USA)');
INSERT INTO public.timevacationsdays VALUES (23, '07', '04', 'Independance day');


--
-- Data for Name: calendar_has_timevacationsdays; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 2);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 4);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 5);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 7);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 9);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 13);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 16);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 17);
INSERT INTO public.calendar_has_timevacationsdays VALUES (1, 18);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 2);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 4);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 5);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 7);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 9);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 19);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 20);
INSERT INTO public.calendar_has_timevacationsdays VALUES (2, 21);
INSERT INTO public.calendar_has_timevacationsdays VALUES (3, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (3, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (3, 22);
INSERT INTO public.calendar_has_timevacationsdays VALUES (3, 23);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 2);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 4);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 5);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 7);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 8);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 14);
INSERT INTO public.calendar_has_timevacationsdays VALUES (4, 15);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 2);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 4);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 5);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 7);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 9);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 10);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 11);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 12);
INSERT INTO public.calendar_has_timevacationsdays VALUES (5, 13);
INSERT INTO public.calendar_has_timevacationsdays VALUES (6, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (6, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (6, 4);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 1);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 2);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 3);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 5);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 6);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 7);
INSERT INTO public.calendar_has_timevacationsdays VALUES (7, 8);


--
-- Data for Name: timevacationsdaysonetime; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.timevacationsdaysonetime VALUES (1, '2013', '04', '01', 'Easter monday');
INSERT INTO public.timevacationsdaysonetime VALUES (2, '2014', '03', '21', 'Easter monday');
INSERT INTO public.timevacationsdaysonetime VALUES (3, '2015', '04', '06', 'Easter monday');
INSERT INTO public.timevacationsdaysonetime VALUES (4, '2016', '03', '28', 'Easter monday');
INSERT INTO public.timevacationsdaysonetime VALUES (5, '2013', '05', '09', 'Ascension day');
INSERT INTO public.timevacationsdaysonetime VALUES (6, '2012', '04', '06', 'Karfreitag');
INSERT INTO public.timevacationsdaysonetime VALUES (7, '2012', '04', '09', 'Ostermontag');
INSERT INTO public.timevacationsdaysonetime VALUES (8, '2014', '05', '29', 'Ascension day');
INSERT INTO public.timevacationsdaysonetime VALUES (9, '2015', '05', '14', 'Ascension day');
INSERT INTO public.timevacationsdaysonetime VALUES (10, '2016', '05', '05', 'Ascension day');
INSERT INTO public.timevacationsdaysonetime VALUES (11, '2013', '05', '20', 'Withmonday');
INSERT INTO public.timevacationsdaysonetime VALUES (12, '2014', '06', '09', 'Withmonday');
INSERT INTO public.timevacationsdaysonetime VALUES (13, '2015', '05', '25', 'Withmonday');
INSERT INTO public.timevacationsdaysonetime VALUES (14, '2016', '05', '16', 'Withmonday');
INSERT INTO public.timevacationsdaysonetime VALUES (15, '2013', '05', '06', 'May day (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (16, '2014', '05', '05', 'May day (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (17, '2015', '05', '04', 'May day (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (18, '2016', '05', '02', 'May day (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (19, '2013', '05', '27', 'Spring holiday (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (20, '2014', '05', '26', 'Spring holiday (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (21, '2015', '05', '25', 'Spring holiday (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (22, '2016', '05', '30', 'Spring holiday (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (23, '2013', '08', '26', 'Summer holiday (GB)');
INSERT INTO public.timevacationsdaysonetime VALUES (24, '2014', '08', '25', 'Summer holiday');
INSERT INTO public.timevacationsdaysonetime VALUES (25, '2015', '08', '31', 'Summer holiday');
INSERT INTO public.timevacationsdaysonetime VALUES (26, '2016', '08', '29', 'Summer holiday');
INSERT INTO public.timevacationsdaysonetime VALUES (27, '2013', '03', '31', 'Easter');
INSERT INTO public.timevacationsdaysonetime VALUES (28, '2014', '04', '20', 'Easter');
INSERT INTO public.timevacationsdaysonetime VALUES (29, '2015', '04', '05', 'Easter');
INSERT INTO public.timevacationsdaysonetime VALUES (30, '2016', '03', '27', 'Easter');
INSERT INTO public.timevacationsdaysonetime VALUES (31, '2013', '03', '29', 'Good Friday');
INSERT INTO public.timevacationsdaysonetime VALUES (32, '2014', '04', '18', 'Good Friday');
INSERT INTO public.timevacationsdaysonetime VALUES (33, '2015', '04', '03', 'Good Friday');
INSERT INTO public.timevacationsdaysonetime VALUES (34, '2016', '03', '25', 'Good Friday');
INSERT INTO public.timevacationsdaysonetime VALUES (35, '2013', '01', '21', 'Birthday of Martin Luther King Jr.');
INSERT INTO public.timevacationsdaysonetime VALUES (36, '2014', '01', '20', 'Birthday of Martin Luther King Jr.');
INSERT INTO public.timevacationsdaysonetime VALUES (37, '2015', '01', '19', 'Birthday of Maritn Luther King Jr.');
INSERT INTO public.timevacationsdaysonetime VALUES (38, '2016', '01', '18', 'Birthday of Martin Luther King Jr.');
INSERT INTO public.timevacationsdaysonetime VALUES (39, '2013', '02', '18', 'Washington''s birthday');
INSERT INTO public.timevacationsdaysonetime VALUES (40, '2014', '02', '17', 'Washington''s birthday');
INSERT INTO public.timevacationsdaysonetime VALUES (41, '2015', '02', '16', 'Washington''s birthday');
INSERT INTO public.timevacationsdaysonetime VALUES (42, '2016', '02', '15', 'Washington''s birthday');
INSERT INTO public.timevacationsdaysonetime VALUES (43, '2013', '05', '27', 'Memorial day');
INSERT INTO public.timevacationsdaysonetime VALUES (44, '2014', '05', '26', 'Memorial day');
INSERT INTO public.timevacationsdaysonetime VALUES (45, '2015', '05', '25', 'Memorial day');
INSERT INTO public.timevacationsdaysonetime VALUES (46, '2016', '05', '30', 'Memorial day');
INSERT INTO public.timevacationsdaysonetime VALUES (47, '2013', '09', '02', 'Labor day');
INSERT INTO public.timevacationsdaysonetime VALUES (48, '2014', '09', '01', 'Labor day');
INSERT INTO public.timevacationsdaysonetime VALUES (49, '2015', '09', '07', 'Labor day');
INSERT INTO public.timevacationsdaysonetime VALUES (50, '2016', '09', '05', 'Labor day');
INSERT INTO public.timevacationsdaysonetime VALUES (51, '2013', '10', '14', 'Columbus day');
INSERT INTO public.timevacationsdaysonetime VALUES (52, '2014', '10', '13', 'Columbus day');
INSERT INTO public.timevacationsdaysonetime VALUES (53, '2015', '10', '12', 'Columbus day');
INSERT INTO public.timevacationsdaysonetime VALUES (54, '2016', '10', '10', 'Columbus day');
INSERT INTO public.timevacationsdaysonetime VALUES (55, '2013', '11', '28', 'Thanksgiving day');
INSERT INTO public.timevacationsdaysonetime VALUES (56, '2014', '11', '27', 'Thanksgiving day');
INSERT INTO public.timevacationsdaysonetime VALUES (57, '2015', '11', '26', 'Thanksgiving day');
INSERT INTO public.timevacationsdaysonetime VALUES (58, '2016', '11', '24', 'Thanksgiving day');
INSERT INTO public.timevacationsdaysonetime VALUES (59, '2013', '05', '30', 'Corpus Christi');
INSERT INTO public.timevacationsdaysonetime VALUES (60, '2014', '06', '19', 'Corpus Christi');
INSERT INTO public.timevacationsdaysonetime VALUES (61, '2015', '06', '04', 'Corpus Christi');
INSERT INTO public.timevacationsdaysonetime VALUES (62, '2016', '05', '26', 'Corpus Christi');
INSERT INTO public.timevacationsdaysonetime VALUES (63, '2013', '11', '20', 'Day of prayer and repentance');
INSERT INTO public.timevacationsdaysonetime VALUES (64, '2014', '11', '19', 'Day of prayer and repentance');
INSERT INTO public.timevacationsdaysonetime VALUES (65, '2015', '11', '18', 'Day of prayer and repentance');
INSERT INTO public.timevacationsdaysonetime VALUES (66, '2016', '11', '16', 'Day of prayer and repentance');
INSERT INTO public.timevacationsdaysonetime VALUES (67, '2012', '11', '21', 'Day of prayer and repentance');
INSERT INTO public.timevacationsdaysonetime VALUES (69, '2012', '10', '08', 'Columbus day');
INSERT INTO public.timevacationsdaysonetime VALUES (68, '2012', '09', '03', 'Labor day');
INSERT INTO public.timevacationsdaysonetime VALUES (70, '2012', '11', '12', 'Veterans day');
INSERT INTO public.timevacationsdaysonetime VALUES (71, '2012', '11', '22', 'Thanksgiving day');
INSERT INTO public.timevacationsdaysonetime VALUES (72, '2012', '08', '27', 'Summer holiday');


--
-- Data for Name: calendar_has_timevacationsdaysonetime; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (50, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (62, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (66, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (34, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (10, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (14, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (59, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (63, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (31, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (5, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (11, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (47, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (60, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (64, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (32, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (8, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (12, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (48, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (61, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (65, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (33, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (9, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (13, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (49, 2);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (62, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (34, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (10, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (14, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (59, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (31, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (5, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (11, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (60, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (32, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (8, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (12, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (61, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (33, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (9, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (13, 4);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (30, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (14, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (11, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (27, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (12, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (28, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (29, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (13, 5);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (62, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (14, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (59, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (11, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (60, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (12, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (61, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (13, 7);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (50, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (62, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (34, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (10, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (14, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (59, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (31, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (5, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (11, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (47, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (60, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (32, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (8, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (12, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (48, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (61, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (33, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (9, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (13, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (49, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (67, 1);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (50, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (54, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (58, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (38, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (42, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (46, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (51, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (55, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (59, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (35, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (39, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (43, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (47, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (52, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (56, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (36, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (40, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (44, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (48, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (53, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (57, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (37, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (41, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (45, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (49, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (69, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (68, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (70, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (71, 3);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (4, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (18, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (22, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (26, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (1, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (15, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (19, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (23, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (2, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (16, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (20, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (24, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (3, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (17, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (21, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (25, 6);
INSERT INTO public.calendar_has_timevacationsdaysonetime VALUES (72, 6);


--
-- Name: calendar_idcalendar_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.calendar_idcalendar_seq', 8, true);


--
-- Data for Name: classified; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.classified VALUES (1, 'SCHEDULED');
INSERT INTO public.classified VALUES (2, 'INFO');
INSERT INTO public.classified VALUES (3, 'UNSCHEDULED');
INSERT INTO public.classified VALUES (4, 'IGNORE');
INSERT INTO public.classified VALUES (5, 'PARTIAL');


--
-- Name: classified_idclassified_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.classified_idclassified_seq', 5, true);


--
-- Data for Name: dashboard; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: iconsize; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.iconsize VALUES (1, 'xs', 10);
INSERT INTO public.iconsize VALUES (2, 's', 15);
INSERT INTO public.iconsize VALUES (3, 'm', 20);
INSERT INTO public.iconsize VALUES (4, 'l', 25);
INSERT INTO public.iconsize VALUES (5, 'xl', 30);


--
-- Data for Name: priority; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.priority VALUES (1, 'High');
INSERT INTO public.priority VALUES (2, 'Medium');
INSERT INTO public.priority VALUES (3, 'Low');


--
-- Data for Name: timeworkinghours; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.timeworkinghours VALUES (1, '07:00-18:00', '07:00-18:00', '07:00-18:00', '07:00-18:00', '07:00-18:00', '', '', '7-18 x 5');
INSERT INTO public.timeworkinghours VALUES (2, '07:00-18:00', '07:00-18:00', '07:00-18:00', '07:00-18:00', '07:00-18:00', '07:00-18:00', NULL, '7-18 x 6');
INSERT INTO public.timeworkinghours VALUES (3, '06:00-22:00', '06:00-22:00', '06:00-22:00', '06:00-22:00', '06:00-22:00', '', '', '6-22 x 5');
INSERT INTO public.timeworkinghours VALUES (4, '06:00-22:00', '06:00-22:00', '06:00-22:00', '06:00-22:00', '06:00-22:00', '06:00-22:00', NULL, '6-22 x 6');
INSERT INTO public.timeworkinghours VALUES (5, '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', NULL, NULL, '24h x 5');
INSERT INTO public.timeworkinghours VALUES (6, '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', NULL, '24 x 6');
INSERT INTO public.timeworkinghours VALUES (7, '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '00:00-00:00', '24 x 7');


--
-- Data for Name: sla; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.sla VALUES (1, '7-18 x 5', 1, 1, '{}');
INSERT INTO public.sla VALUES (2, '7-18 x 6', 1, 2, '{}');
INSERT INTO public.sla VALUES (3, '6-22 x 5', 1, 3, '{}');
INSERT INTO public.sla VALUES (4, '6-22 x 6', 1, 4, '{}');
INSERT INTO public.sla VALUES (5, '24h x 5', 1, 5, '{}');
INSERT INTO public.sla VALUES (6, '24h x 6', 1, 6, '{}');
INSERT INTO public.sla VALUES (7, '24h x 7', 8, 7, '{}');


--
-- Data for Name: object; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.object VALUES (1, 'local_disk_root', 'localhost', 'Local Disk', 99.00, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO public.object VALUES (2, 'icmp_ping_alive', 'localhost', 'Localhost availablity', 98.00, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO public.object VALUES (3, 'local_load', 'localhost', 'CPU Load', 99.00, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL);


--
-- Data for Name: period; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.period VALUES (1, 'This Week');
INSERT INTO public.period VALUES (2, 'This Month');
INSERT INTO public.period VALUES (3, 'Last Week');
INSERT INTO public.period VALUES (4, 'Last Month');
INSERT INTO public.period VALUES (5, 'Last 30 Days');
INSERT INTO public.period VALUES (6, 'Last 7 Days');
INSERT INTO public.period VALUES (7, 'Last 5 Days');
INSERT INTO public.period VALUES (8, 'Custom');
INSERT INTO public.period VALUES (9, 'Last 2 Days');
INSERT INTO public.period VALUES (10, 'Last Day');
INSERT INTO public.period VALUES (11, 'Today');


--
-- Data for Name: refreshfreq; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.refreshfreq VALUES (1, '1 Hour');
INSERT INTO public.refreshfreq VALUES (2, '30 Minutes');
INSERT INTO public.refreshfreq VALUES (3, '15 Minutes');
INSERT INTO public.refreshfreq VALUES (4, '5 Minutes');
INSERT INTO public.refreshfreq VALUES (5, '1 Minute');
INSERT INTO public.refreshfreq VALUES (6, '30 Seconds');


--
-- Data for Name: statusproperties; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: widgettype; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.widgettype VALUES (1, 'Tacho');
INSERT INTO public.widgettype VALUES (2, 'Pie chart');
INSERT INTO public.widgettype VALUES (3, 'Timetable');
INSERT INTO public.widgettype VALUES (4, 'Log list');
INSERT INTO public.widgettype VALUES (5, 'Downtime bar');
INSERT INTO public.widgettype VALUES (6, 'Report table');
INSERT INTO public.widgettype VALUES (7, 'Status');
INSERT INTO public.widgettype VALUES (8, 'Image');
INSERT INTO public.widgettype VALUES (9, 'Service list');
INSERT INTO public.widgettype VALUES (10, 'Timeline');
INSERT INTO public.widgettype VALUES (12, 'Box');
INSERT INTO public.widgettype VALUES (11, 'Text');


--
-- Data for Name: widget; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: dashboard_has_widget; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: dashboard_iddashboard_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.dashboard_iddashboard_seq', 1, false);


--
-- Data for Name: downtime; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.downtime VALUES (1, '2012-01-12 07:00:00', '2012-01-12 11:00:00', 'Was Down', 'SYSTEM - rstools', 'SYSTEM - rstools', NULL, NULL);
INSERT INTO public.downtime VALUES (2, '2012-01-13 07:00:00', '2012-01-13 17:00:00', 'Was Down', 'SYSTEM - rstools', 'SYSTEM - rstools', NULL, NULL);


--
-- Name: downtime_iddowntime_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.downtime_iddowntime_seq', 3, false);


--
-- Data for Name: downtimeschedule; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: downtimeactive; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: downtimeactive_iddowntimeactive_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.downtimeactive_iddowntimeactive_seq', 1, false);


--
-- Name: downtimeschedule_iddowntimeschedule_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.downtimeschedule_iddowntimeschedule_seq', 1, false);


--
-- Data for Name: downtimeschedulerepeat; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: downtimeschedulerepeat_iddowntimeschedulerepeat_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.downtimeschedulerepeat_iddowntimeschedulerepeat_seq', 1, false);


--
-- Data for Name: eventidentification; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: eventidentification_ideventidentification_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.eventidentification_ideventidentification_seq', 1, false);


--
-- Data for Name: excludedate; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: excludedate_idexcludedate_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.excludedate_idexcludedate_seq', 1, false);


--
-- Data for Name: exporttype; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.exporttype VALUES (1, 'json');
INSERT INTO public.exporttype VALUES (2, 'html');
INSERT INTO public.exporttype VALUES (3, 'js');


--
-- Data for Name: exporttemplate; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.exporttemplate VALUES (1, 2, 'Default HTML Template', 'This is the default HTML Template. You can use this as example.', '<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
<style type="text/css">
    .more_info {
        position: absolute;
        z-index: 2;
        width: 100%;
        max-width: 300px;
        background-color: white;
        border-radius: 4px;
        box-shadow: 0 0 7px black;
        padding: 10px;
    }
    .more_info .close {
        position: absolute;
        top: 2px;
        right: 2px;
    }
</style>
<h2>[TITLE]</h2>
<p>[STATS]</p>
[INFOTEXT]<br/>
<hr/>
<strong>[TIMESTAMP]</strong>
<script>
    function toggleInfowindow(i_service, show){
        if (show == ''show'') {
            document.getElementById(''srv_info_''+i_service).style.display=''block'';
        } else {
            document.getElementById(''srv_info_''+i_service).style.display=''none'';
        }
    }
</script>');
INSERT INTO public.exporttemplate VALUES (2, 2, 'Default HTML Template 2', 'This is the default HTML Template 2. You can use this as example.', '<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
<style type="text/css">
    .more_info {
        position: absolute;
        z-index: 2;
        width: 100%;
        max-width: 300px;
        background-color: white;
        border-radius: 4px;
        box-shadow: 0 0 7px black;
        padding: 10px;
    }
    .more_info .close {
        position: absolute;
        top: 2px;
        right: 2px;
    }
    table {
        table-layout:fixed;
    }
  
    td {
        width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    td.MS.1 { background-color: #c32e04; }
    td.MS.2 { background-color: #c32e04; }
    td.MS.3 { background-color: #c32e04; }
    td.MS.4 { background-color: #c32e04; }
    td.MS.5 { background-color: #c32e04; }
    td.MS.6 { background-color: #c32e04; }
    td.MS.7 { background-color: #c32e33; }
    td.MS.8 { background-color: #c32eff; }
</style>
<h2>[TITLE]</h2>
<table>
[INLOOP]<tr><td>[SERVICEDESCRIPTION]</td><td class="MS [SERVICESTATUS]">[SERVICESTATUS]</td><td>[DOWNTIMEINFO]</td><td>[DOWNTIMECOMMENT]</td></tr>[/INLOOP]</table>
[INFOTEXT]<br/>
<strong>[TIMESTAMP]</strong>
<script>
    function toggleInfowindow(i_service, show){
        if (show == ''show'') {
            document.getElementById(''srv_info_''+i_service).style.display=''block'';
        } else {
            document.getElementById(''srv_info_''+i_service).style.display=''none'';
        }
    }
</script>');
INSERT INTO public.exporttemplate VALUES (3, 1, 'Default JSON Template', 'This is the default JSON Template. You can use this as example.', '{"created":"[TIMESTAMP]","client":"[TITLE]","services":[[INLOOP]{"service":"[SERVICEDESCRIPTION]","status":"[SERVICESTATUS]","downtime_comments":"[DOWNTIMECOMMENT]","downtime":"[DOWNTIMEINFO]","info":"[INFO]"}[/INLOOP]]}');
INSERT INTO public.exporttemplate VALUES (4, 1, 'Default JSON Template 2', 'This is the default JSON Template 2. You can use this as example.', '{"created":"[TIMESTAMP]","client":"[TITLE]","services":[[INLOOP]{"service":"[SERVICEDESCRIPTION]","host":"[HOSTNAME]","status":"[SERVICESTATUS]","downtime_comments":"[DOWNTIMECOMMENT]","downtime":"[DOWNTIMEINFO]","info":"[INFO]"}[/INLOOP]]}');
INSERT INTO public.exporttemplate VALUES (5, 3, 'Default JS Template', 'This is the default JavaScript Template. You can use this as example.', 'var jsonString = {"created":"[TIMESTAMP]","name":"[TITLE]","services":[[INLOOP]{"service":"[SERVICEDESCRIPTION]","host":"[HOSTNAME]","status":"[SERVICESTATUS]","downtime_comments":"[DOWNTIMECOMMENT]","downtime":"[DOWNTIMEINFO]","info":"[INFO]","lastchecktime":"[LASTCHECKTIME]","id":"[SERVICESTATUSID]"}[/INLOOP]]};');
INSERT INTO public.exporttemplate VALUES (6, 1, 'GroundWork Sample JSON Template', 'This is the default JSON Template. You can use this as example.', '{"created":"[TIMESTAMP]","name":"[INFOTEXT]","services":[[INLOOP]{"service":"[SERVICEDESCRIPTION]","host":"[HOSTNAME]","status":"[SERVICESTATUS]","downtime":"[DOWNTIMECOMMENT]","lastchecktime":"[LASTCHECKTIME]","info":"[INFO]","id":[SERVICESTATUSID]}[/INLOOP]]}');


--
-- Name: exporttemplate_idexporttemplate_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.exporttemplate_idexporttemplate_seq', 6, true);


--
-- Name: exporttype_idexporttype_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.exporttype_idexporttype_seq', 3, true);


--
-- Data for Name: group; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public."group" VALUES ('GroundWork Monitor', 'GroundWork Monitor', 'GroundWork Monitor Core Services', '', '', true, 1, 0, 0, false, 1, 'bsm-host', 'bsm-service-01', 'BSM:Business Objects', '{
		"UNSCHEDULED DOWN":{"weight":6,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},
		"UNSCHEDULED CRITICAL":{"weight":6,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},
		"ACKNOWLEDGED UNSCHEDULED CRITICAL":{"weight":5,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},
		"ACKNOWLEDGED UNSCHEDULED DOWN":{"weight":5,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},
		"PENDING":{"weight":4,"count":0,"problem":1,"bsm":"PENDING"},
		"UNKNOWN":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},
		"UNREACHABLE":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},
		"ACKNOWLEDGED PENDING":{"weight":4,"count":0,"problem":1,"bsm":"PENDING"},
		"ACKNOWLEDGED UNKNOWN":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},
		"ACKNOWLEDGED UNREACHABLE":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},
		"SCHEDULED DOWN":{"weight":3,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"SCHEDULED CRITICAL":{"weight":3,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"SUSPENDED":{"weight":3,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"ACKNOWLEDGED SCHEDULED CRITICAL":{"weight":2,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"ACKNOWLEDGED SCHEDULED DOWN":{"weight":2,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"ACKNOWLEDGED SUSPENDED":{"weight":2,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},
		"WARNING":{"weight":1,"count":0,"problem":1,"bsm":"WARNING"},
		"ACKNOWLEDGED WARNING":{"weight":1,"count":0,"problem":1,"bsm":"WARNING"},
		"UP":{"weight":0,"count":0,"problem":0,"bsm":"OK"},
		"OK":{"weight":0,"count":0,"problem":0,"bsm":"OK"}
	}');


--
-- Data for Name: group_has_group; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: host; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.host VALUES (1, 'localhost', 1);


--
-- Data for Name: group_has_host; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: service; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.service VALUES ('local_process_nagios', 17, 1, 1);
INSERT INTO public.service VALUES ('local_process_gw_listener', 16, 2, 1);
INSERT INTO public.service VALUES ('tcp_http', 20, 3, 1);
INSERT INTO public.service VALUES ('tcp_nsca', 21, 4, 1);
INSERT INTO public.service VALUES ('tcp_gw_listener', 19, 5, 1);
INSERT INTO public.service VALUES ('local_nagios_latency', 15, 6, 1);


--
-- Data for Name: group_has_service; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.group_has_service VALUES (1, false, 1);
INSERT INTO public.group_has_service VALUES (1, false, 2);
INSERT INTO public.group_has_service VALUES (1, false, 3);
INSERT INTO public.group_has_service VALUES (1, false, 4);
INSERT INTO public.group_has_service VALUES (1, false, 5);
INSERT INTO public.group_has_service VALUES (1, false, 6);


--
-- Data for Name: servicegroup; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: group_has_servicegroup; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: group_idgroup_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.group_idgroup_seq', 2, false);


--
-- Name: host_idhost_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.host_idhost_seq', 2, true);


--
-- Name: iconsize_idiconsize_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.iconsize_idiconsize_seq', 5, true);


--
-- Data for Name: manualevents; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: manualevents_idmanualevents_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.manualevents_idmanualevents_seq', 1, false);


--
-- Data for Name: monitoredservice; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.monitoredservice VALUES (1, 17, 'local_process_nagios', '', true);
INSERT INTO public.monitoredservice VALUES (2, 16, 'local_process_gw_listener', '', true);
INSERT INTO public.monitoredservice VALUES (3, 20, 'tcp_http', '', true);
INSERT INTO public.monitoredservice VALUES (4, 21, 'tcp_nsca', '', true);
INSERT INTO public.monitoredservice VALUES (5, 19, 'tcp_gw_listener', '', true);
INSERT INTO public.monitoredservice VALUES (6, 15, 'local_nagios_latency', '', true);


--
-- Name: monitoredservice_has_monitoredservicecli_idmonitoredservice_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.monitoredservice_has_monitoredservicecli_idmonitoredservice_seq', 1, false);


--
-- Data for Name: monitoredserviceclient; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.monitoredserviceclient VALUES (1, 'd1172168222a53e2a3c081f46f26ebf4', 'GroundWork Monitor', 'GroundWork Monitor Core Services', 'local', '', '', '', '', '/usr/local/groundwork/apache2/htdocs/publishStatusSamples', '', 'json');


--
-- Data for Name: monitoredservice_has_monitoredserviceclient; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (1, 1);
INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (2, 1);
INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (3, 1);
INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (4, 1);
INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (5, 1);
INSERT INTO public.monitoredservice_has_monitoredserviceclient VALUES (6, 1);


--
-- Name: monitoredservice_idmonitoredservice_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.monitoredservice_idmonitoredservice_seq', 6, true);


--
-- Data for Name: monitoredserviceclient_has_exporttemplate; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.monitoredserviceclient_has_exporttemplate VALUES (6, 1);


--
-- Name: monitoredserviceclient_idmonitoredserviceclient_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.monitoredserviceclient_idmonitoredserviceclient_seq', 1, true);


--
-- Data for Name: monitoredservicecomment; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: monitoredservicecomment_idmonitoredservicecomment_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.monitoredservicecomment_idmonitoredservicecomment_seq', 1, false);


--
-- Data for Name: object_has_downtime; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.object_has_downtime VALUES (1, 1);


--
-- Data for Name: object_has_manualevents; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: object_idobject_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.object_idobject_seq', 4, false);


--
-- Name: period_idperiod_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.period_idperiod_seq', 11, true);


--
-- Name: priority_idpriority_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.priority_idpriority_seq', 4, false);


--
-- Name: refreshfreq_idrefreshfreq_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.refreshfreq_idrefreshfreq_seq', 6, true);


--
-- Data for Name: reprocessflag; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: schemainfo; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.schemainfo VALUES ('CurrentSchemaVersion', '0.7');
INSERT INTO public.schemainfo VALUES ('SchemaUpdated', '2015-10-08 09:15:52.892414+02');


--
-- Name: service_idservice_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.service_idservice_seq', 7, true);


--
-- Name: servicegroup_idservicegroup_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.servicegroup_idservicegroup_seq', 1, false);


--
-- Name: sla_idsla_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.sla_idsla_seq', 8, false);


--
-- Data for Name: sladaily; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: sladaily_idsladaily_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.sladaily_idsladaily_seq', 1, false);


--
-- Data for Name: slalogs; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: slalogs_idslalogs_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.slalogs_idslalogs_seq', 1, false);


--
-- Data for Name: statusproperties_has_status; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: statusproperties_has_status_fk_idstatusproperties_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.statusproperties_has_status_fk_idstatusproperties_seq', 1, false);


--
-- Name: statusproperties_idstatusproperties_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.statusproperties_idstatusproperties_seq', 1, false);


--
-- Data for Name: tbl_migration; Type: TABLE DATA; Schema: public; Owner: slareport
--

INSERT INTO public.tbl_migration VALUES ('m000000_000000_base', 1447854188);
INSERT INTO public.tbl_migration VALUES ('m151111_110000_importRecurringDowntime', 1447854188);
INSERT INTO public.tbl_migration VALUES ('m151111_111101_publishService', 1447854188);
INSERT INTO public.tbl_migration VALUES ('m151111_151905_importNagiosDowntime', 1447854188);
INSERT INTO public.tbl_migration VALUES ('m151216_114455_statePropertySUSPENDED', 1450265191);
INSERT INTO public.tbl_migration VALUES ('m160209_102102_exportType', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m160303_135221_monitoredclientTemplate', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m161020_143639_slaDashboard', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m161109_152135_defaultExporttemplates', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170112_090400_slaDashboardRefFreq', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170316_162000_slaDashboardStatusWidget', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170412_111936_eventClassification', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170418_090607_eventClassification', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170614_095600_slaRefreshfreq', 1528188439);
INSERT INTO public.tbl_migration VALUES ('m170714_104332_slaCalculation', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170804_130245_widgetImage', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170809_064437_slaAttributes', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170809_064438_slaAttributes', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170816_085700_serviceList', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170912_115456_objectAttributes', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170919_081517_slaEmptyHoliday', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m170920_080129_slaRules', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171017_133808_slaEventsClassification', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171017_133908_slaEventsClassification', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171019_103700_widgetViewProperties', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171108_170302_slaEventsClassification', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171116_120500_timelineWidget', 1528188440);
INSERT INTO public.tbl_migration VALUES ('m171116_145152_slaLatin1ToUtf8', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171119_095000_textViewWidget', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171119_145500_widgetGrouping', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171122_192600_servicelistFilterOption', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171208_092618_splitSlaEvents', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171212_205251_boxView', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171213_074323_slaManualEventPrevious', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171215_121528_slaObjectPriority', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171220_085149_slaRelationObject', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m171220_134826_reprocessingFlag', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180118_111122_migrateToSignificantIdentity', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180122_135900_renameTextWidget', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180207_111500_IncludeServiceStatus', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180315_101500_servicelistAckStates', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180531_143837_createPublishServiceSamples', 1528188442);
INSERT INTO public.tbl_migration VALUES ('m180528_093500_statusWidgetIconsize', 1528969577);


--
-- Name: timevacationsdays_idtimevacationsdays_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.timevacationsdays_idtimevacationsdays_seq', 24, true);


--
-- Name: timevacationsdaysonetime_idtimevacationsdaysonetime_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.timevacationsdaysonetime_idtimevacationsdaysonetime_seq', 73, false);


--
-- Name: timeworkinghours_idtimeworkinghours_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.timeworkinghours_idtimeworkinghours_seq', 8, false);


--
-- Name: widget_idwidget_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.widget_idwidget_seq', 1, false);


--
-- Data for Name: widgetgroup; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: widgetgroup_has_group; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Data for Name: widgetgroup_has_widget; Type: TABLE DATA; Schema: public; Owner: slareport
--



--
-- Name: widgetgroup_idgroup_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.widgetgroup_idgroup_seq', 1, false);


--
-- Name: widgettype_idwidgettype_seq; Type: SEQUENCE SET; Schema: public; Owner: slareport
--

SELECT pg_catalog.setval('public.widgettype_idwidgettype_seq', 12, true);


--
-- PostgreSQL database dump complete
--

