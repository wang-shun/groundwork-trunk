-- MySQL dump 10.10
--
-- Host: localhost    Database: GWProfilerDB
-- ------------------------------------------------------
-- Server version	5.0.22-standard

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
-- Table structure for table `MessageBatches`
--

DROP TABLE IF EXISTS `MessageBatches`;
CREATE TABLE `MessageBatches` (
  `MessageBatchID` int(11) NOT NULL auto_increment,
  `WorkloadID` int(11) NOT NULL default '0',
  `WorkloadBatchID` int(11) NOT NULL default '0',
  `MessageName` varchar(256) NOT NULL default 'MessageName',
  `Threshold` bigint(20) NOT NULL default '-1',
  `Latency` bigint(20) NOT NULL default '-1',
  `BatchStartTime` datetime NOT NULL default '0000-00-00 00:00:00',
  `BatchEndTime` datetime default NULL,
  `BStartTimeString` varchar(256) NOT NULL default '0000-00-00 00:00:00',
  `BEndTimeString` varchar(256) NOT NULL default '0000-00-00 00:00:00',
  `TimeRecorded` datetime default NULL,
  `NumberOfChecks` int(11) NOT NULL default '0',
  PRIMARY KEY  (`MessageBatchID`),
  KEY `workloadid_index` (`WorkloadID`),
  CONSTRAINT `workload_workloadid_fk_constraint` FOREIGN KEY (`WorkloadID`) REFERENCES `Workloads` (`WorkloadID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `WorkloadSessions`
--

DROP TABLE IF EXISTS `WorkloadSessions`;
CREATE TABLE `WorkloadSessions` (
  `SessionID` int(11) NOT NULL auto_increment,
  `Name` varchar(256) NOT NULL default 'SessionName',
  `StartTime` datetime NOT NULL default '0000-00-00 00:00:00',
  `EndTime` datetime default NULL,
  `LStartTime` bigint(20)  NOT NULL default '1210344527614',
  `LEndTime` bigint(20) NOT NULL default '1210344527614',
  PRIMARY KEY  (`SessionID`),
  UNIQUE(Name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `Workloads`
--

DROP TABLE IF EXISTS `Workloads`;
CREATE TABLE `Workloads` (
  `WorkloadID` int(11) NOT NULL auto_increment,
  `SessionID` int(11) NOT NULL default '0',
  `Name` varchar(256) NOT NULL default '',
  `StartTime` datetime NOT NULL default '0000-00-00 00:00:00',
  `EndTime` datetime default NULL,
  `LStartTime` bigint(20)  NOT NULL default '1210344527614',
  `LEndTime` bigint(20) NOT NULL default '1210344527614',
  PRIMARY KEY  (`WorkloadID`),
  KEY `session_sessionid_fk_constraint` (`SessionID`),
  CONSTRAINT `session_sessionid_fk_constraint` FOREIGN KEY (`SessionID`) REFERENCES `WorkloadSessions` (`SessionID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


