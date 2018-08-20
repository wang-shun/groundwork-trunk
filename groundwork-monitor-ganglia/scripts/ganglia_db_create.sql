-- MySQL dump 10.10
--
-- Host: localhost    Database: ganglia
-- ------------------------------------------------------
-- Server version	5.0.18-pro

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


DROP DATABASE IF EXISTS `ganglia`;
CREATE DATABASE `ganglia`;
USE `ganglia`;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
CREATE TABLE `cluster` (
  `ClusterID` int(11) unsigned NOT NULL auto_increment,
  `Name` text collate latin1_general_ci NOT NULL,
  `Description` text collate latin1_general_ci,
  `Regex` tinyint(1) default NULL,
  PRIMARY KEY  (`ClusterID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `cluster`
--


/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
LOCK TABLES `cluster` WRITE;
INSERT INTO `cluster` VALUES (1,'Default','Default definitions for all clusters.',0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;

--
-- Table structure for table `clusterhost`
--

DROP TABLE IF EXISTS `clusterhost`;
CREATE TABLE `clusterhost` (
  `ClusterHostID` int(11) unsigned NOT NULL auto_increment,
  `ClusterID` int(11) unsigned NOT NULL,
  `HostID` int(11) unsigned NOT NULL,
  PRIMARY KEY  (`ClusterHostID`),
  KEY `ClusterHost_ClusterFK` (`ClusterID`),
  KEY `ClusterHost_HostFK` (`HostID`),
  CONSTRAINT `ClusterHost_ClusterFK` FOREIGN KEY (`ClusterID`) REFERENCES `cluster` (`ClusterID`),
  CONSTRAINT `ClusterHost_HostFK` FOREIGN KEY (`HostID`) REFERENCES `host` (`HostID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `clusterhost`
--


/*!40000 ALTER TABLE `clusterhost` DISABLE KEYS */;
LOCK TABLES `clusterhost` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `clusterhost` ENABLE KEYS */;

--
-- Table structure for table `host`
--

DROP TABLE IF EXISTS `host`;
CREATE TABLE `host` (
  `HostID` int(11) unsigned NOT NULL auto_increment,
  `Name` text collate latin1_general_ci NOT NULL,
  `IPAddress` varchar(45) collate latin1_general_ci default NULL,
  `Description` text collate latin1_general_ci,
  `Regex` tinyint(1) default NULL,
  PRIMARY KEY  (`HostID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `host`
--


/*!40000 ALTER TABLE `host` DISABLE KEYS */;
LOCK TABLES `host` WRITE;
INSERT INTO `host` VALUES (1,'Default','','Default definitions for all hosts.',0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `host` ENABLE KEYS */;

--
-- Table structure for table `hostinstance`
--

DROP TABLE IF EXISTS `hostinstance`;
CREATE TABLE `hostinstance` (
  `HostInstanceID` int(11) unsigned NOT NULL auto_increment,
  `ClusterID` int(11) unsigned NOT NULL,
  `HostID` int(11) unsigned NOT NULL,
  `LocationID` int(11) unsigned NOT NULL,
  PRIMARY KEY  (`HostInstanceID`),
  KEY `HostInstance_ClusterFK` (`ClusterID`),
  KEY `HostInstance_HostFK` (`HostID`),
  KEY `HostInstance_LocationFK` (`LocationID`),
  CONSTRAINT `HostInstance_ClusterFK` FOREIGN KEY (`ClusterID`) REFERENCES `cluster` (`ClusterID`),
  CONSTRAINT `HostInstance_HostFK` FOREIGN KEY (`HostID`) REFERENCES `host` (`HostID`),
  CONSTRAINT `HostInstance_LocationFK` FOREIGN KEY (`LocationID`) REFERENCES `location` (`LocationID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `hostinstance`
--


/*!40000 ALTER TABLE `hostinstance` DISABLE KEYS */;
LOCK TABLES `hostinstance` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `hostinstance` ENABLE KEYS */;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `LocationID` int(11) unsigned NOT NULL auto_increment,
  `Name` text collate latin1_general_ci NOT NULL,
  `Description` text collate latin1_general_ci,
  `Regex` tinyint(1) default NULL,
  PRIMARY KEY  (`LocationID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `location`
--


/*!40000 ALTER TABLE `location` DISABLE KEYS */;
LOCK TABLES `location` WRITE;
INSERT INTO `location` VALUES (1,'Default','Default definitions for all locations',0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `location` ENABLE KEYS */;

--
-- Table structure for table `metric`
--

DROP TABLE IF EXISTS `metric`;
CREATE TABLE `metric` (
  `MetricID` int(11) unsigned NOT NULL auto_increment,
  `Name` text collate latin1_general_ci NOT NULL,
  `Description` text collate latin1_general_ci,
  `Units` varchar(45) collate latin1_general_ci default NULL,
  `Critical` decimal(64,10) default NULL,
  `Warning` decimal(64,10) default NULL,
  `Duration` decimal(64,10) default NULL,
  PRIMARY KEY  (`MetricID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `metric`
--


/*!40000 ALTER TABLE `metric` DISABLE KEYS */;
LOCK TABLES `metric` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `metric` ENABLE KEYS */;

--
-- Table structure for table `metricinstance`
--

DROP TABLE IF EXISTS `metricinstance`;
CREATE TABLE `metricinstance` (
  `MetricInstanceID` int(11) unsigned NOT NULL auto_increment,
  `HostInstanceID` int(11) unsigned NOT NULL,
  `MetricID` int(11) unsigned NOT NULL,
  `Description` text collate latin1_general_ci,
  `LastState` text collate latin1_general_ci,
  `LastUpdateTime` int(11) unsigned NOT NULL,
  `LastStateChangeTime` int(11) unsigned NOT NULL,
  `LastValue` text collate latin1_general_ci,
  PRIMARY KEY  (`MetricInstanceID`),
  KEY `MetricInstance_HostInstanceFK` (`HostInstanceID`),
  KEY `MetricInstance_MetricFK` (`MetricID`),
  CONSTRAINT `MetricInstance_HostInstanceFK` FOREIGN KEY (`HostInstanceID`) REFERENCES `hostinstance` (`HostInstanceID`),
  CONSTRAINT `MetricInstance_MetricFK` FOREIGN KEY (`MetricID`) REFERENCES `metric` (`MetricID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `metricinstance`
--


/*!40000 ALTER TABLE `metricinstance` DISABLE KEYS */;
LOCK TABLES `metricinstance` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `metricinstance` ENABLE KEYS */;

--
-- Table structure for table `metricvalue`
--

DROP TABLE IF EXISTS `metricvalue`;
CREATE TABLE `metricvalue` (
  `MetricValueID` int(11) unsigned NOT NULL auto_increment,
  `ClusterID` int(11) unsigned NOT NULL,
  `HostID` int(11) unsigned NOT NULL,
  `LocationID` int(11) unsigned NOT NULL,
  `MetricID` int(11) unsigned NOT NULL,
  `Description` text collate latin1_general_ci,
  `Critical` decimal(64,10) default NULL,
  `Warning` decimal(64,10) default NULL,
  `Duration` decimal(64,10) default NULL,
  PRIMARY KEY  (`MetricValueID`),
  KEY `MetricValue_ClusterFK` (`ClusterID`),
  KEY `MetricValue_HostFK` (`HostID`),
  KEY `MetricValue_LocationFK` (`LocationID`),
  KEY `MetricValue_MetricFK` (`MetricID`),
  CONSTRAINT `MetricValue_ClusterFK` FOREIGN KEY (`ClusterID`) REFERENCES `cluster` (`ClusterID`),
  CONSTRAINT `MetricValue_HostFK` FOREIGN KEY (`HostID`) REFERENCES `host` (`HostID`),
  CONSTRAINT `MetricValue_LocationFK` FOREIGN KEY (`LocationID`) REFERENCES `location` (`LocationID`),
  CONSTRAINT `MetricValue_MetricFK` FOREIGN KEY (`MetricID`) REFERENCES `metric` (`MetricID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

--
-- Dumping data for table `metricvalue`
--


/*!40000 ALTER TABLE `metricvalue` DISABLE KEYS */;
LOCK TABLES `metricvalue` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `metricvalue` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

