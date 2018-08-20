-- MySQL dump 10.10
--
-- Host: localhost    Database: dashboard
-- ------------------------------------------------------
-- Server version	5.0.26-pro

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table measurements
--

DROP TABLE IF EXISTS measurements;
CREATE TABLE measurements (
  timestamp varchar(100) NOT NULL default '',
  timestamp_uts integer NOT NULL default '0',
  name varchar(100) NOT NULL default '',
  component varchar(255) NOT NULL default '',
  measurement integer NOT NULL default '0',
  time_interval varchar(100) NOT NULL default ''
) ;

ALTER TABLE public.measurements OWNER TO ir;

--
-- Table structure for table host_availability
--

DROP TABLE IF EXISTS host_availability;
CREATE TABLE host_availability (
  TIMESTAMP varchar(100) NOT NULL default '',
  DATESTAMP date NOT NULL default '1970-01-01',
  HOST_NAME varchar(100) NOT NULL default '',
  TIME_INTERVAL varchar(100) NOT NULL default '',
  PERCENT_KNOWN_TIME_DOWN double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_DOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_DOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_DOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  PERCENT_TIME_UNREACHABLE_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNREACHABLE_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UP_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UP_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_DOWN double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNREACHABLE double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UP double precision NOT NULL default '0',
  TIME_DOWN_SCHEDULED integer NOT NULL default '0',
  TIME_DOWN_UNSCHEDULED integer NOT NULL default '0',
  TIME_UNDETERMINED_NOT_RUNNING integer NOT NULL default '0',
  TIME_UNDETERMINED_NO_DATA integer NOT NULL default '0',
  TIME_UNREACHABLE_SCHEDULED integer NOT NULL default '0',
  TIME_UNREACHABLE_UNSCHEDULED integer NOT NULL default '0',
  TIME_UP_SCHEDULED integer NOT NULL default '0',
  TIME_UP_UNSCHEDULED integer NOT NULL default '0',
  TOTAL_TIME_DOWN integer NOT NULL default '0',
  TOTAL_TIME_UNDETERMINED integer NOT NULL default '0',
  TOTAL_TIME_UNREACHABLE integer NOT NULL default '0',
  TOTAL_TIME_UP integer NOT NULL default '0',
  PRIMARY KEY  (HOST_NAME,DATESTAMP,TIME_INTERVAL)
) ;

ALTER TABLE public.host_availability OWNER TO ir;

--
-- Table structure for table service_availability
--

DROP TABLE IF EXISTS service_availability;
CREATE TABLE service_availability (
  TIMESTAMP varchar(100) NOT NULL default '',
  DATESTAMP date NOT NULL default '1970-01-01',
  HOST_NAME varchar(100) NOT NULL default '',
  SERVICE_NAME varchar(100) NOT NULL default '',
  TIME_INTERVAL varchar(100) NOT NULL default '',
  PERCENT_KNOWN_TIME_CRITICAL double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_OK_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_OK_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  PERCENT_TIME_UNKNOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNKNOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_WARNING_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_WARNING_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_CRITICAL double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_OK double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNKNOWN double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_WARNING double precision NOT NULL default '0',
  TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  TIME_OK_SCHEDULED integer NOT NULL default '0',
  TIME_OK_UNSCHEDULED integer NOT NULL default '0',
  TIME_UNDETERMINED_NOT_RUNNING integer NOT NULL default '0',
  TIME_UNDETERMINED_NO_DATA integer NOT NULL default '0',
  TIME_UNKNOWN_SCHEDULED integer NOT NULL default '0',
  TIME_UNKNOWN_UNSCHEDULED integer NOT NULL default '0',
  TIME_WARNING_SCHEDULED integer NOT NULL default '0',
  TIME_WARNING_UNSCHEDULED integer NOT NULL default '0',
  TOTAL_TIME_CRITICAL integer NOT NULL default '0',
  TOTAL_TIME_OK integer NOT NULL default '0',
  TOTAL_TIME_UNDETERMINED integer NOT NULL default '0',
  TOTAL_TIME_UNKNOWN integer NOT NULL default '0',
  TOTAL_TIME_WARNING integer NOT NULL default '0',
  PRIMARY KEY  (HOST_NAME,DATESTAMP,SERVICE_NAME,TIME_INTERVAL)
) ;

ALTER TABLE public.service_availability OWNER TO ir;


--
-- Table structure for table hostgroup_host_availability
--

DROP TABLE IF EXISTS hostgroup_host_availability;
CREATE TABLE hostgroup_host_availability (
  TIMESTAMP varchar(100) NOT NULL default '',
  DATESTAMP date NOT NULL default '1970-01-01',
  HOSTGROUP_NAME varchar(100) NOT NULL default '',
  TIME_INTERVAL varchar(100) NOT NULL default '',
  PERCENT_KNOWN_TIME_DOWN double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_DOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UP_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_DOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_DOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  PERCENT_TIME_UNREACHABLE_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNREACHABLE_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UP_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UP_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_DOWN double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNREACHABLE double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UP double precision NOT NULL default '0',
  TIME_DOWN_SCHEDULED double precision NOT NULL default '0',
  TIME_DOWN_UNSCHEDULED double precision NOT NULL default '0',
  TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  TIME_UNREACHABLE_SCHEDULED double precision NOT NULL default '0',
  TIME_UNREACHABLE_UNSCHEDULED double precision NOT NULL default '0',
  TIME_UP_SCHEDULED double precision NOT NULL default '0',
  TIME_UP_UNSCHEDULED double precision NOT NULL default '0',
  TOTAL_TIME_DOWN double precision NOT NULL default '0',
  TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  TOTAL_TIME_UNREACHABLE double precision NOT NULL default '0',
  TOTAL_TIME_UP double precision NOT NULL default '0',
  PRIMARY KEY  (HOSTGROUP_NAME,DATESTAMP,TIME_INTERVAL)
) ;

ALTER TABLE public.hostgroup_host_availability OWNER TO ir;
--
-- Table structure for table hostgroup_service_availability
--

DROP TABLE IF EXISTS hostgroup_service_availability;
CREATE TABLE hostgroup_service_availability (
  TIMESTAMP varchar(100) NOT NULL default '',
  DATESTAMP date NOT NULL default '1970-01-01',
  HOSTGROUP_NAME varchar(100) NOT NULL default '',
  TIME_INTERVAL varchar(100) NOT NULL default '',
  PERCENT_KNOWN_TIME_CRITICAL double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_OK_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING_SCHEDULED double precision NOT NULL default '0',
  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_OK_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_OK_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  PERCENT_TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  PERCENT_TIME_UNKNOWN_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_UNKNOWN_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_WARNING_SCHEDULED double precision NOT NULL default '0',
  PERCENT_TIME_WARNING_UNSCHEDULED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_CRITICAL double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_OK double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_UNKNOWN double precision NOT NULL default '0',
  PERCENT_TOTAL_TIME_WARNING double precision NOT NULL default '0',
  TIME_CRITICAL_SCHEDULED double precision NOT NULL default '0',
  TIME_CRITICAL_UNSCHEDULED double precision NOT NULL default '0',
  TIME_OK_SCHEDULED double precision NOT NULL default '0',
  TIME_OK_UNSCHEDULED double precision NOT NULL default '0',
  TIME_UNDETERMINED_NOT_RUNNING double precision NOT NULL default '0',
  TIME_UNDETERMINED_NO_DATA double precision NOT NULL default '0',
  TIME_UNKNOWN_SCHEDULED double precision NOT NULL default '0',
  TIME_UNKNOWN_UNSCHEDULED double precision NOT NULL default '0',
  TIME_WARNING_SCHEDULED double precision NOT NULL default '0',
  TIME_WARNING_UNSCHEDULED double precision NOT NULL default '0',
  TOTAL_TIME_CRITICAL double precision NOT NULL default '0',
  TOTAL_TIME_OK double precision NOT NULL default '0',
  TOTAL_TIME_UNDETERMINED double precision NOT NULL default '0',
  TOTAL_TIME_UNKNOWN double precision NOT NULL default '0',
  TOTAL_TIME_WARNING double precision NOT NULL default '0',
  PRIMARY KEY  (HOSTGROUP_NAME,DATESTAMP,TIME_INTERVAL)
) ;

ALTER TABLE public.hostgroup_service_availability OWNER TO ir;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-03-02 21:24:22
