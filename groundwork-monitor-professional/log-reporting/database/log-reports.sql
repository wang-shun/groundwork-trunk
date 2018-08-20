-- MySQL dump 10.10
--
-- Host: localhost    Database: logreports
-- ------------------------------------------------------
-- Server version	5.0.21

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
-- Table structure for table `Component`
--

DROP TABLE IF EXISTS `Component`;
CREATE TABLE `Component` (
  `componentID` int(11) NOT NULL auto_increment,
  `componentTypeID` int(11) NOT NULL default '0',
  `logMessageID` int(11) NOT NULL default '0',
  `componentValueID` int(11) NOT NULL,
  PRIMARY KEY  (`componentID`,`logMessageID`),
  KEY `IDX_Component1` (`componentTypeID`),
  KEY `IDX_Component2` (`logMessageID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Component`
--


/*!40000 ALTER TABLE `Component` DISABLE KEYS */;
LOCK TABLES `Component` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Component` ENABLE KEYS */;

--
-- Table structure for table `ComponentType`
--

DROP TABLE IF EXISTS `ComponentType`;
CREATE TABLE `ComponentType` (
  `componentTypeID` int(11) NOT NULL auto_increment,
  `componentTypeName` varchar(40) NOT NULL,
  PRIMARY KEY  (`componentTypeID`),
  UNIQUE KEY `IDX_ComponentType1` (`componentTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ComponentType`
--


/*!40000 ALTER TABLE `ComponentType` DISABLE KEYS */;
LOCK TABLES `ComponentType` WRITE;
INSERT INTO `ComponentType` VALUES (2,'ruser'),(18,'host'),(1,'rhost'),(7,'disk'),(19,'user'),(20,'domain'),(21,'Authentication_Package'),(22,'Workstation_Name'),(23,'document'),(24,'printer'),(25,'pages'),(26,'source'),(27,'destination'),(28,'interface'),(29,'protocol'),(30,'access-group');
UNLOCK TABLES;
/*!40000 ALTER TABLE `ComponentType` ENABLE KEYS */;

--
-- Table structure for table `ComponentValue`
--

DROP TABLE IF EXISTS `ComponentValue`;
CREATE TABLE `ComponentValue` (
  `componentValueID` int(11) NOT NULL auto_increment,
  `componentValue` varchar(500) NOT NULL,
  PRIMARY KEY  (`componentValueID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ComponentValue`
--


/*!40000 ALTER TABLE `ComponentValue` DISABLE KEYS */;
LOCK TABLES `ComponentValue` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `ComponentValue` ENABLE KEYS */;

--
-- Table structure for table `FileIdentificationRule`
--

DROP TABLE IF EXISTS `FileIdentificationRule`;
CREATE TABLE `FileIdentificationRule` (
  `ruleID` int(11) NOT NULL auto_increment,
  `logfileTypeID` int(11) NOT NULL,
  `regex` varchar(500) NOT NULL,
  PRIMARY KEY  (`ruleID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `FileIdentificationRule`
--


/*!40000 ALTER TABLE `FileIdentificationRule` DISABLE KEYS */;
LOCK TABLES `FileIdentificationRule` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `FileIdentificationRule` ENABLE KEYS */;

--
-- Table structure for table `LogDirectory`
--

DROP TABLE IF EXISTS `LogDirectory`;
CREATE TABLE `LogDirectory` (
  `logDirectoryID` int(11) NOT NULL auto_increment,
  `logDirectory` varchar(1000) default NULL,
  PRIMARY KEY  (`logDirectoryID`),
  UNIQUE KEY `IDX_LogDirectory1` (`logDirectoryID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `LogFile`
--

DROP TABLE IF EXISTS `LogFile`;
CREATE TABLE `LogFile` (
  `logFileID` int(11) NOT NULL auto_increment,
  `logFileName` varchar(255) NOT NULL,
  `logfileTypeID` int(11) NOT NULL default '0',
  `LogDirectoryID` int(11) NOT NULL default '0',
  `isProcessed` int(11) NOT NULL default '0',
  `seekPos` int(11) NOT NULL default '0',
  `inode` int(11) default NULL,
  PRIMARY KEY  (`logFileID`),
  UNIQUE KEY `IDX_LogFile2` (`logFileID`),
  UNIQUE KEY `inode` (`inode`),
  KEY `IDX_LogFile1` (`logfileTypeID`),
  KEY `IDX_LogFile3` (`LogDirectoryID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `LogFilenameFilter`
--

DROP TABLE IF EXISTS `LogFilenameFilter`;
CREATE TABLE `LogFilenameFilter` (
  `regex` varchar(40) NOT NULL,
  `logfileTypeID` int(11) NOT NULL,
  PRIMARY KEY  (`regex`,`logfileTypeID`),
  KEY `IDX_LogFilenameFilter1` (`logfileTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


--
-- Table structure for table `LogMessage`
--

DROP TABLE IF EXISTS `LogMessage`;
CREATE TABLE `LogMessage` (
  `logMessageID` int(11) NOT NULL auto_increment,
  `parsingRuleID` int(11) NOT NULL default '0',
  `logFileID` int(11) NOT NULL default '0',
  `timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  `count` int(11) default '1',
  `componentID` int(11) NOT NULL default '0',
  PRIMARY KEY  (`logMessageID`),
  UNIQUE KEY `IDX_LogMessage3` (`logMessageID`),
  KEY `IDX_LogMessage1` (`parsingRuleID`),
  KEY `IDX_LogMessage2` (`logFileID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogMessage`
--


/*!40000 ALTER TABLE `LogMessage` DISABLE KEYS */;
LOCK TABLES `LogMessage` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogMessage` ENABLE KEYS */;

--
-- Table structure for table `LogMessageClass`
--

DROP TABLE IF EXISTS `LogMessageClass`;
CREATE TABLE `LogMessageClass` (
  `logMessageClassID` int(11) NOT NULL auto_increment,
  `logMessageClassName` varchar(40) NOT NULL,
  `logfileTypeID` int(11) NOT NULL default '0',
  `persistenceInDays` int(11) NOT NULL default '0',
  `groupBy` varchar(20) NOT NULL default 'Day',
  PRIMARY KEY  (`logMessageClassID`),
  UNIQUE KEY `IDX_LogMessageClass2` (`logMessageClassID`),
  KEY `IDX_LogMessageClass1` (`logfileTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogMessageClass`
--


/*!40000 ALTER TABLE `LogMessageClass` DISABLE KEYS */;
LOCK TABLES `LogMessageClass` WRITE;
INSERT INTO `LogMessageClass` VALUES (8,'Security',0,365,'Week'),(14,'Misc',0,14,'Day'),(19,'Sample',0,7,'Month');
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogMessageClass` ENABLE KEYS */;

--
-- Table structure for table `LogMessageClass_LogMessageType`
--

DROP TABLE IF EXISTS `LogMessageClass_LogMessageType`;
CREATE TABLE `LogMessageClass_LogMessageType` (
  `logMessageClassID` int(11) NOT NULL,
  `logMessageTypeID` int(11) NOT NULL,
  KEY `IDX_LogMessageClass_LogMessageType1` (`logMessageClassID`),
  KEY `IDX_LogMessageClass_LogMessageType2` (`logMessageTypeID`),
  PRIMARY KEY (`logMessageClassID`,`logMessageTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogMessageClass_LogMessageType`
--


/*!40000 ALTER TABLE `LogMessageClass_LogMessageType` DISABLE KEYS */;
LOCK TABLES `LogMessageClass_LogMessageType` WRITE;
INSERT INTO `LogMessageClass_LogMessageType` VALUES (19,49),(19,42),(14,42),(8,42),(8,49),(20,55);
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogMessageClass_LogMessageType` ENABLE KEYS */;

--
-- Table structure for table `LogMessageType`
--

DROP TABLE IF EXISTS `LogMessageType`;
CREATE TABLE `LogMessageType` (
  `logMessageTypeID` int(11) NOT NULL auto_increment,
  `logMessageTypeName` varchar(40) NOT NULL,
  `persistenceInDays` int(11) NOT NULL default '0',
  `groupBy` varchar(20) NOT NULL default 'Day',
  PRIMARY KEY  (`logMessageTypeID`),
  UNIQUE KEY `IDX_LogMessageType1` (`logMessageTypeID`),
  UNIQUE KEY `logMessageTypeName` (`logMessageTypeName`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogMessageType`
--


/*!40000 ALTER TABLE `LogMessageType` DISABLE KEYS */;
LOCK TABLES `LogMessageType` WRITE;
INSERT INTO `LogMessageType` VALUES (42,'FTP',10,'Day'),(49,'SSH',44,'Day'),(0,'Unkown',1,'Day'),(50,'Login',111,'Day'),(51,'Privileges',365,'Week'),(52,'General',7,'Day'),(53,'GDM',22,'Month'),(54,'Print',0,'Day');
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogMessageType` ENABLE KEYS */;

--
-- Table structure for table `LogMessageType_ComponentType`
--

DROP TABLE IF EXISTS `LogMessageType_ComponentType`;
CREATE TABLE `LogMessageType_ComponentType` (
  `logMessageTypeID` int(11) NOT NULL,
  `componentTypeID` int(11) NOT NULL,
  KEY `IDX_LogMessageType_ComponentType1` (`logMessageTypeID`),
  KEY `IDX_LogMessageType_ComponentType2` (`componentTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogMessageType_ComponentType`
--


/*!40000 ALTER TABLE `LogMessageType_ComponentType` DISABLE KEYS */;
LOCK TABLES `LogMessageType_ComponentType` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogMessageType_ComponentType` ENABLE KEYS */;

--
-- Table structure for table `LogfileType`
--

DROP TABLE IF EXISTS `LogfileType`;
CREATE TABLE `LogfileType` (
  `logfileTypeID` int(11) NOT NULL auto_increment,
  `typeName` varchar(40) NOT NULL,
  PRIMARY KEY  (`logfileTypeID`),
  UNIQUE KEY `IDX_LogfileType1` (`logfileTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LogfileType`
--


/*!40000 ALTER TABLE `LogfileType` DISABLE KEYS */;
LOCK TABLES `LogfileType` WRITE;
INSERT INTO `LogfileType` VALUES (1,'Windows'),(7,'Linux syslog'),(14,'Cisco VPN'),(1016,'Cisco PIX');
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogfileType` ENABLE KEYS */;

--
-- Table structure for table `ParsingRule`
--

DROP TABLE IF EXISTS `ParsingRule`;
CREATE TABLE `ParsingRule` (
  `parsingRuleID` int(11) NOT NULL auto_increment,
  `parsingRuleName` varchar(100) NOT NULL default '',
  `logfileTypeID` int(11) NOT NULL default '0',
  `parsingRuleText` varchar(500) NOT NULL,
  `logMessageTypeID` int(11) NOT NULL default '0',
  `isEnabled` int(11) NOT NULL default '1',
  `severityID` int(11) NOT NULL default '6',
  PRIMARY KEY  (`parsingRuleID`),
  UNIQUE KEY `IDX_ParsingRule3` (`parsingRuleID`),
  KEY `IDX_ParsingRule1` (`logfileTypeID`),
  KEY `IDX_ParsingRule2` (`logMessageTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ParsingRule`
--


/*!40000 ALTER TABLE `ParsingRule` DISABLE KEYS */;
LOCK TABLES `ParsingRule` WRITE;
INSERT INTO `ParsingRule` VALUES (86,'The logon attempt was made with an unknown user name or a known user name with a bad password.',1,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\sMSWinEventLog\\s+\\d\\s+\\w+\\s+.*?\\s529\\s.*User\\sName.\\s(\\w+)\\s+Domain.\\s+(.+?)\\s+.*Authentication\\sPackage.\\s+(\\w+)\\s+Workstation\\sName.\\s+(.+?)\\s+',50,1,6),(87,'An attempt was made to log on with the user account outside of the allowed time.',1,'\\s530\\s+Security',50,1,6),(88,'A logon attempt was made using a disabled account.',1,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\sMSWinEventLog\\s+\\d\\s+\\w+\\s+.*?\\s531\\s.*User\\sName.\\s(\\w+)\\s+Domain.\\s+(.+?)\\s+.*Authentication\\sPackage.\\s+(\\w+)\\s+Workstation\\sName.\\s+(.+?)\\s+',50,1,6),(89,'A logon attempt was made using an expired account.',1,'\\s532\\s+Security',50,1,6),(90,'The user is not allowed to log on at this computer.',1,'\\s533\\s+Security',50,1,6),(91,'The user attempted to log on with a logon type that is not allowed.',1,'\\s534\\s+Security',50,1,6),(92,'The logon attempt failed for other reasons.',1,'\\s537\\s+Security',50,1,6),(93,'A user logged off.',1,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\sMSWinEventLog\\s+\\d\\s+\\w+\\s+.*?\\s538\\s.*User\\sName.\\s(\\w+)\\s+Domain.\\s+(.+?)\\s+',50,0,6),(94,'The account was locked out at the time the logon attempt was made.',1,'\\s539\\s+Security',50,1,6),(95,'Pre-authentication failed',1,'\\s675\\s+Security',50,1,6),(96,'A TGS ticket was not granted.',1,'\\s677\\s+Security',50,1,6),(97,'A user attempted to perform a privileged system service operation',1,'\\s577\\s+Security',51,1,6),(98,'Privileges were used on an already open handle to a protected object.',1,'\\s578\\s+Security',51,1,6),(110,'Repeated wrong passwd while trying to ssh',7,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\ssshd.+?\\s(\\d+)\\smore\\sauthentication\\sfailures.+?ruser=(.*?)\\srhost=(.*)',49,1,6),(111,'FTP as root (and some others) is invalid',7,'FTP LOGIN REFUSED .*?\\sFROM (.*?),\\s(\\S+)',42,1,6),(112,'Wrong password while trying to open FTP session',7,'ftp.*?authentication failure;.*?\\s+rhost=(.*?)\\s+user=(\\S+)',42,1,6),(113,'Trying to open FTP session as nonexistant user',7,'ftp.*?check pass; user unknown',42,1,6),(114,'Trying to ssh with correct password but breaking Access rules',7,'access denied for user .(\\w+). from .(\\w+).',49,1,6),(115,'GDM login with wrong passwd',7,'gdm.*?authentication failure;.*?\\s+rhost=(.*?)\\s+user=(\\S+)',53,1,6),(116,'su  with wrong passwd',7,'su.*?authentication failure;.*?\\s+rhost=(.*?)\\s+user=(\\S+)',50,1,6),(117,'login via login with wrong passwd',7,'login.*?authentication failure;.*?\\s+rhost=(.*?)\\s+user=(\\S+)',50,1,6),(999,'No Data',999,'',999,1,6),(1008,'Deny IP spoof',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106016.*Deny.*from\\s+\\((\\w+.+?)\\)\\s+to\\s+(\\w+.+?)\\s.*interface\\s(.+)\\s',52,0,6),(1001,'Password Expired',1,'\\s535\\s+Security',50,1,6),(1002,'NetLogon service Down',1,'\\s536\\s+Security',50,1,6),(1003,'IPSec peer authentication failed',1,'\\s545\\s+Security',51,1,6),(1004,'IPSec security association establishment failed because peer sent invalid proposal',1,'\\s546\\s+Security',52,1,6),(1005,'IPSec secuirty association negotiation failed',1,'\\s547\\s+Security',52,1,6),(1006,'Authenication Ticket Request Failed',1,'\\s676\\s+Security',51,1,6),(1009,'Deny Filter Action',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-106023.*Deny\\s(.+?)\\ssrc\\s(\\w+.+?)\\s+dst\\s(.+?)\\s+by\\s+access-group\\s\\\"(.+)\\s\"',52,0,6),(1012,'Recieved packet is not an IPSEC packet.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-402106.+?dest_addr.\\s(.+?),\\ssrc_addr.\\s(.+?),\\s+prot.\\s(.+)',52,1,6),(1013,'Invalid transport field',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-500004.+?protocol=(.+?),\\s+from\\s(.+?)\\s+to\\s+(.+)',52,1,6),(1014,'Dropped UDP DNS reply',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-410001.+?Dropped.+?from\\s(.+?)\\s+to\\s+(.+)',52,1,6),(1015,'Successful Network Logon',1,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\sMSWinEventLog\\s+\\d\\s+\\w+\\s+.*?\\s540\\s.*User\\sName.\\s(\\w+)\\s+Domain.\\s+(.+?)\\s+.*Authentication\\sPackage.\\s+(\\w+)\\s+Workstation\\sName.\\s+(.+?)\\s+',50,0,6),(1016,'Document was printed',1,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(\\w+)\\sMSWinEventLog\\s+\\d\\s+\\w+\\s+.*?\\s10\\s.+Print\\s+(\\w+)\\s+User\\s+\\w+\\s+\\w+\\s+\\w+\\s+Document\\s+\\d+\\,\\s+(\\w+.+?)owned\\sby.*printed\\s+on\\s(\\w+.+?)via.*pages\\s+printed.\\s+(\\d+)',54,1,6),(1017,'IPSec security association establishment failed because peer could not authenicate',1,'\\s544\\s+Security',50,1,6),(1018,'Authentication Ticket Granted',1,'\\s672\\s+Security',50,0,6),(1019,'Service Ticket Granted',1,'\\s673\\s+Security',50,0,6),(1020,'Logon failed',1,'\\s681\\s+Security',50,1,6),(1024,'SSH Authentication Failure',7,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\ssshd.+?rhost=(.*?)\\suser=(.*)',49,1,6),(5783,'{TCP|UDP} access permitted from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-710002',52,1,6),(5784,'{TCP|UDP} access denied by ACL from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-710003',52,1,6),(5785,'TCP connection limit exceeded from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-710004',52,1,6),(5786,'{TCP|UDP} request discarded from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-710005',52,1,6),(5787,'/protocol/ request discarded from /source_address/ to ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-710006',52,1,6),(5773,'H.225 message received from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-703001',52,1,6),(5774,'Received H.225 Release Complete with ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-703002',52,1,6),(5775,'FO replication failed cmd=command returned=/code',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-709001',52,1,6),(5776,'FO unreplicable cmd=command',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-709002',52,1,6),(5777,'(Primary) Beginning configuration replication ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-709003',52,1,6),(5778,'(Primary) End Configuration Replication (ACT)',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-709004',52,1,6),(5779,'(Primary) Beginning configuration replication ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-709005',52,1,6),(5782,'TCP access requested from /source_address///source_port/ to ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-710001',52,1,6),(5781,'Configuration replication failed for command command',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-709007',52,1,6),(5780,'(Primary) End Configuration Replication (STB)',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-709006',52,1,6),(5771,'replay rollover detected...',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-702302',52,1,6),(5772,'sa_request...',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702303',52,1,6),(5768,'ISAKMP Phase 2 exchange completed(local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702211',52,1,6),(5769,'ISAKMP Phase 1 initiating rekey (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702212',52,1,6),(5770,'lifetime expiring... ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702301',52,1,6),(5767,'ISAKMP Phase 1 exchange completed(local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702210',52,1,6),(5766,'ISAKMP Phase 2 exchange started (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702209',52,1,6),(5764,'ISAKMP duplicate packet detected (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702207',52,1,6),(5765,'ISAKMP Phase 1 exchange started (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702208',52,1,6),(5763,'ISAKMP malformed payload received (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702206',52,1,6),(5758,'ISAKMP Phase 1 delete received (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702201',52,1,6),(5759,'ISAKMP Phase 1 delete sent (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702202',52,1,6),(5760,'ISAKMP DPD timed out (local ip (initiator|responder), ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702203',52,1,6),(5761,'ISAKMP Phase 1 retransmission (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702204',52,1,6),(5762,'ISAKMP Phase 2 retransmission (local ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-702205',52,1,6),(5757,'alloc_user() out of Tcp_user objects',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-701001',52,1,6),(5753,'Split DNS request patched from server /IP_address /to ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-614001',52,1,6),(5754,'Split DNS reply from server/IP_address/ reverse patched ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-614002',52,1,6),(5755,'Pre-allocate CTIQBE {RTP | RTCP} secondary channel for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-620001',52,1,6),(5756,'Unsupported CTIQBE version /hex/ from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-620002',52,1,6),(5752,'/IP_address netmask/ changed from area /string/ to area ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-613003',52,1,6),(5751,'interface /interface_name/ has zero bandwidth',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-613002',52,1,6),(5750,'Checksum Failure in database in area /string/ Link State ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-613001',52,1,6),(5749,'Auto Update failed to contact/url/, reason/reason/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-612003',52,1,6),(5748,'Auto Update failed/filename/, version/number/, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-612002',52,1,6),(5747,'Auto Update succeeded/filename/, version/number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-612001',52,1,6),(5746,'VPNClient Duplicate split nw entry',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611323',52,1,6),(5745,'VPNClient Extended XAUTH conversation initiated when ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611322',52,1,6),(5743,'VPNClient Device Pass Thru Enabled',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611320',52,1,6),(5744,'VPNClient Device Pass Thru Disabled',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611321',52,1,6),(5741,'VPNClient User Authentication Enabled Auth Server IP ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611318',52,1,6),(5742,'VPNClient User Authentication Disabled',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611319',52,1,6),(5739,'VPNClient Secure Unit Authentication Enabled',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611316',52,1,6),(5740,'VPNClient Secure Unit Authentication Disabled',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611317',52,1,6),(5738,'VPNClient Disconnecting from Load Balancing Cluster ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611315',52,1,6),(5736,'VPNClient Backup Server List Error /reason',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-611313',52,1,6),(5737,'VPNClient Load Balancing Cluster with Virtual IP ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611314',52,1,6),(5734,'VNPClient XAUTH Failed Peer /IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611311',52,1,6),(5735,'VPNClient Backup Server List /reason',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611312',52,1,6),(5733,'VNPClient XAUTH Succeeded Peer /IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611310',52,1,6),(5731,'VPNClient Split DNS Policy installed List of domains ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611308',52,1,6),(5732,'VPNClient Disconnecting from head end and uninstalling ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611309',52,1,6),(5729,'VPNClient Perfect Forward Secrecy Policy installed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611306',52,1,6),(5730,'VPNClient Head end  /IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611307',52,1,6),(5724,'VPNClient NAT configured for Client Mode with no split ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611301',52,1,6),(5725,'VPNClient NAT exemption configured for Network ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611302',52,1,6),(5726,'VPNClient NAT configured for Client Mode with split ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611303',52,1,6),(5727,'VPNClient NAT exemption configured for Network ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611304',52,1,6),(5728,'VPNClient DHCP Policy installed Primary DNS ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611305',52,1,6),(5723,'Serial console idle timeout exceeded',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-611104',52,1,6),(5722,'User logged out Uname /user',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-611103',52,1,6),(5721,'User authentication failed Uname /user',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611102',52,1,6),(5720,'User authentication succeeded Uname /user',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-611101',52,1,6),(5717,'NTP daemon interface interface_name Packet denied from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-610001',52,1,6),(5719,'Authorization failed Cmd /command/ Cmdtype ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-610101',52,1,6),(5718,'NTP daemon interface interface_name Authentication ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-610002',52,1,6),(5714,'Pre-allocate Skinny /connection_type/ secondary channel ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-608001',52,1,6),(5715,'Built local-host /interface_nameIP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-609001',52,1,6),(5716,'Teardown local-host /interface_nameIP_address/ duration ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-609002',52,1,6),(5713,'Pre-allocate SIP /connection_type/ secondary channel for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-607001',52,1,6),(5708,'DHCP daemon interface interface_name address released',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-604104',52,1,6),(5709,'Login denied from {/source_address///source_port /| serial} ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-605004',52,1,6),(5710,'Login permitted from {/source_address///source_port /| ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-605005',52,1,6),(5711,'PDM session number /number/ from IP_address started',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-606001',52,1,6),(5712,'PDM session number /number/ from IP_address ended',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-606002',52,1,6),(5706,'DHCP client interface interface_name address released',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-604102',52,1,6),(5707,'DHCP daemon interface interface_name address granted ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-604103',52,1,6),(5704,'Teardown PPPOE Tunnel at /interface_name/, tunnel-id = ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603109',52,1,6),(5705,'DHCP client interface interface_name Allocated ip = ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-604101',52,1,6),(5703,'Built PPTP Tunnel at /interface_name/, tunnel-id = ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603108',52,1,6),(5701,'L2TP Tunnel created, tunnel_id is number, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603106',52,1,6),(5702,'L2TP Tunnel deleted, tunnel_id = number, remote_peer_ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603107',52,1,6),(5698,'PPP virtual interface interface_name - user user aaa ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603103',52,1,6),(5699,'PPTP Tunnel created, tunnel_id is /number/, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603104',52,1,6),(5700,'PPTP Tunnel deleted, tunnel_id = number, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603105',52,1,6),(5697,'PPP virtual interface interface_name - user user aaa ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603102',52,1,6),(5696,'PPTP received out of seq or duplicate pkt, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-603101',52,1,6),(5695,'deleting sa',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-602302',52,1,6),(5693,'ISAKMP Phase 1 SA created (local ip/port ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-602201',52,1,6),(5694,'sa created... ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-602301',52,1,6),(5692,'Adjusting IPSec tunnel mtu...',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-602102',52,1,6),(5691,'PMTU-D packet number bytes greater than effective mtu ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-602101',52,1,6),(5690,'Process number, Nbr /IP_address/ on /interface_name/ from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-503001',52,1,6),(5687,'New user added to local dbase Uname /user/ Priv ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-502101',52,1,6),(5689,'User priv level changed Uname /user/ From ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-502103',52,1,6),(5688,'User deleted from local dbase Uname /user/ Priv ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-502102',52,1,6),(5684,'Bad TCP hdr length (hdrlen=bytes, pktlen=bytes) from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-500003',52,1,6),(5685,'Invalid transport field for protocol=protocol, from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-500004',52,1,6),(5686,'User transitioning priv level',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-501101',52,1,6),(5683,'Java content modified src IP_address dest IP_address on ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-500002',52,1,6),(5682,'ActiveX content modified src IP_address dest IP_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-500001',52,1,6),(5681,'Dropped UDP SNMP packet from source interface ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-416001',52,1,6),(5679,'Line protocol on interface interface_name changed state ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-411001',52,1,6),(5680,'Line protocol on interface interface_name changed state ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-411002',52,1,6),(5678,'UDP DNS packet dropped due to compression length check ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-410001',52,1,6),(5676,'UDP DNS packet dropped due to label length check of 63 ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-410001',52,1,6),(5677,'UDP DNS packet dropped due to packet length check of n ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-410001',52,1,6),(5675,'UDP DNS packet dropped due to domainname length check ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-410001',52,1,6),(5671,'OSPF detected duplicate router-id /IP_address/ from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409011',52,1,6),(5672,'Detected router with duplicate router ID /IP_address/ in ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409012',52,1,6),(5673,'Detected router with duplicate router ID /IP_address/ in ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409013',52,1,6),(5674,'Attempting AAA Fallback method method_name for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409023',52,1,6),(5670,'Virtual link information found in non-backbone area ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409010',52,1,6),(5669,'OSPF process number cannot start. There must be at least ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409009',52,1,6),(5668,'Found generating default LSA with non-zero mask LSA ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409008',52,1,6),(5667,'Found LSA with the same host bit set but using different ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409007',52,1,6),(5665,'Invalid length number in OSPF packet from /IP_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409005',52,1,6),(5666,'Invalid lsa /reason/ Type /number/, LSID /IP_address/ from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409006',52,1,6),(5664,'Received reason from unknown neighbor /IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409004',52,1,6),(5663,'Received invalid packet /reason/ from /IP_address/, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409003',52,1,6),(5658,'Deny traffic for local-host ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-407001',52,1,6),(5659,'Embryonic limit /neconns///elimit/ for through connections ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-407002',52,1,6),(5660,'IP route counter negative - /reason/, /IP_address/ Attempt ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-408001',52,1,6),(5662,'db_free external LSA /IP_address netmask',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409002',52,1,6),(5661,'Database scanner external LSA /IP_address netmask/ is ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-409001',52,1,6),(5652,'Unable to Pre-allocate H225 Call Signalling Connection ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405101',52,1,6),(5653,'Unable to Pre-allocate H245 Connection for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405102',52,1,6),(5654,'H225 message from src_ip/src_port to ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405103',52,1,6),(5655,'H225 message received from /outside_address///outside_port/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405104',52,1,6),(5656,'FTP port command low port /IP_address///port/ to ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-406001',52,1,6),(5657,'FTP port command different address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-406002',52,1,6),(5650,'Received ARP {request | response} collision from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405001',52,1,6),(5651,'Received mac mismatch collision from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-405002',52,1,6),(5649,'ISAKMP Failed to allocate address for client from pool ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-404101',52,1,6),(5648,'PPPoEfailed to assign PPP /IP_address/ netmask /netmask/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-403506',52,1,6),(5647,'PPPoEPPP - Unable to set default route to /IP_address/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-403505d',52,1,6),(5646,'PPPoEPPP link down/reason/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-403503',52,1,6),(5645,'PPPoE - Bad host-unique in PADS - dropping packet. ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-403502',52,1,6),(5644,'PPPoE - Bad host-unique in PADO - packet dropped. ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-403501',52,1,6),(5641,'PPP virtual interface interface_name missing aaa server ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403107',52,1,6),(5642,'PPP virtual interface interface_name missing client ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403108',52,1,6),(5643,'PPP virtual interface /interface_name/, user user ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403110',52,1,6),(5637,'PPP virtual interface interface_name rcvd pkt with ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403102',52,1,6),(5638,'PPP virtual interface max connections reached.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403103',52,1,6),(5639,'PPP virtual interface interface_name requires mschap ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403104',52,1,6),(5640,'PPP virtual interface interface_name requires RADIUS ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403106',52,1,6),(5636,'PPTP session state not established, but received an XGRE ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-403101',52,1,6),(5629,'IDSnumber /st/ring from IP_address to IP_address on interface interface_name',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-4000nn',52,1,6),(5630,'Shuns cleared',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-401001',52,1,6),(5631,'Shun added /IP_address IP_address port port',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-401002',52,1,6),(5632,'Shun deleted /IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-401003',52,1,6),(5633,'Shunned packet /IP_address/ == /IP_address/ on interface ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-401004',52,1,6),(5634,'Shun add failed unable to allocate resources for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-401005',52,1,6),(5635,'decapsulate packet missing {AH|ESP}, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-402102',52,1,6),(5627,'OSPF process /number/ is changing router-id. Reconfigure virtual link neighbors with our new router-',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318008',52,1,6),(5628,'The subject name of the peer cert is not allowed for connection',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-320001',52,1,6),(5624,'lsid /IP_address/ adv /IP_address/ type /number/ gateway ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318005',52,1,6),(5625,'if /interface_name/ if_state /number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318006',52,1,6),(5626,'OSPF is enabled on /interface_name/ during idb ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318007',52,1,6),(5623,'area /string/ lsid /IP_address/ mask /netmask/ adv /IP_address/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318004',52,1,6),(5620,'Internal error /reason',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318001',52,1,6),(5621,'Flagged as being an ABR without a backbone area',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318002',52,1,6),(5622,'Reached unknown state in neighbor state machine',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-318003',52,1,6),(5615,'No memory available for limit_slow',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-317001',52,1,6),(5616,'Bad path index of /number/ for /IP_address/, /number/ max',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-317002',52,1,6),(5617,'IP routing table creation failure - /reason',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-317003',52,1,6),(5618,'IP routing table limit warning',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-317004',52,1,6),(5619,'IP routing table limit exceeded - /reason/, /IP_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-317005',52,1,6),(5614,'Denied new tunnel to IP_address. VPN peer limit ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-316001',52,1,6),(5613,'SSH session from IP_address on interface interface_name for user user disconnected by SSH server, re',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-315011',52,1,6),(5612,'Fail to establish SSH session because PIX RSA host key ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-315004',52,1,6),(5611,'Pre-allocate RTSP UDP backconnection for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-314001',52,1,6),(5610,'Invalid destination for ICMP error',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-313003',52,1,6),(5609,'Denied ICMP type=number, code=code from IP_address on ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-313001',52,1,6),(5608,'RIP hdr failed from IP_address cmd=string, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-312001',52,1,6),(5605,'LU loading standby end',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-311002',52,1,6),(5606,'LU recv thread up',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-311003',52,1,6),(5607,'LU xmit thread up',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-311004',52,1,6),(5604,'LU loading standby start',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-311001',52,1,6),(5601,'PIX console enable password incorrect for number tries ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-308001',52,1,6),(5602,'static global_address inside_address netmask netmask overlapped with global_address inside_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-308002',52,1,6),(5603,'Permitted manager connection from IP_address.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-309002',52,1,6),(5600,'Teardown {dynamic|static} {TCP|UDP|ICMP} translation ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-305012',52,1,6),(5598,'Teardown {dynamic|static} translation from /interface_name [(acl-name)]real_address/ to /interface_n',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-305010',52,1,6),(5599,'Built {dynamic|static} {TCP|UDP|ICMP} translation from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-305011',52,1,6),(5597,'Teardown type translation from interfaceaddress ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-305009',52,1,6),(5594,'{outbound static|identity|portmap|regular) translation creation failed for protocol src interface_na',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-305006',52,1,6),(5595,'addrpool_free() Orphan IP IP_address on interface interface_number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-305007',52,1,6),(5596,'Free unallocated global IP address.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-305008',52,1,6),(5593,'No translation group found for /protocol/ src ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-305005',52,1,6),(5590,'URL Server IP_address not responding, ENTERING ALLOW ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-304007',52,1,6),(5591,'LEAVING ALLOW mode, URL Server is up.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-304008',52,1,6),(5592,'Ran out of buffer blocks specified by url-block command',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-304009',52,1,6),(5589,'URL Server IP_address not responding',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-304006',52,1,6),(5587,'URL Server IP_address request failed URL url',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-304004',52,1,6),(5588,'URL Server IP_address request pending URL url',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-304005',52,1,6),(5586,'URL Server IP_address timed out URL url',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-304003',52,1,6),(5585,'Access denied URL url SRC IP_address DEST IP_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-304002',52,1,6),(5584,'user source_address Accessed {JAVA URL|URL} dest_address url.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-304001',52,1,6),(5583,'source_address {Stored|Retrieved} dest_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-303002',52,1,6),(5582,'ACL = deny; no sa created',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-302302',52,1,6),(5581,'H.323 /library_name/ ASN Library failed to initialize, ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-302019',52,1,6),(5579,'Built {/inbound/|/outbound/} GRE connection /id/ from/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302017',52,1,6),(5580,'Teardown GRE connection /id/ from',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302018',52,1,6),(5577,'Built {inbound|outbound} UDP connection /number/ for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302015',52,1,6),(5578,'Teardown UDP connection /number/ for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302016',52,1,6),(5576,'Teardown TCP connection /number/ for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302014',52,1,6),(5575,'Built {inbound|outbound} TCP connection /number/ for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302013',52,1,6),(5574,'connections in use, connections most used',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302010',52,1,6),(5572,'Pre-allocate H323 UDP backconnection for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302004',52,1,6),(5573,'Rebuilt TCP connection number for foreign_address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302009',52,1,6),(5571,'Built H245 connection for foreign_address outside_address/outside_port local_address inside_address/',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-302003',52,1,6),(5569,'Terminating manager session from /IP_address/ on ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-214001',52,1,6),(5570,'Bad route_compress() call, sdb= /number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-215001',52,1,6),(5567,'PPTP tunnel hashtable insert failed, peer = IP_address.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-213002',52,1,6),(5568,'PPP virtual interface /interface_/number client ip ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-213004',52,1,6),(5566,'PPTP control daemon socket io string, errno = number.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-213001',52,1,6),(5565,'incoming SNMP request (number bytes) on interface interface_name exceeds data buffer size, discardin',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-212005',52,1,6),(5563,'Unable to receive an SNMP request on interface ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-212003',52,1,6),(5564,'Unable to send an SNMP response to IP Address IP_address Port port interface interface_number, error',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-212004',52,1,6),(5561,'Unable to open SNMP channel (UDP port port) on interface ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-212001',52,1,6),(5562,'Unable to open SNMP trap channel (UDP port port) on ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-212002',52,1,6),(5559,'Memory allocation Error',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-211001',52,1,6),(5560,'CPU utilization for /number/ seconds = /percent',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-211003',52,1,6),(5557,'LU create static xlate global_address ifc ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210021',52,1,6),(5558,'LU missed number updates',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-210022',52,1,6),(5556,'LU PAT port port reserve failed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210020',52,1,6),(5554,'LU no xlate for inside_address/inside_port ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210008',52,1,6),(5555,'LU make UDP connection for outside_addressoutside_port ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210010',52,1,6),(5553,'LU allocate xlate failed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210007',52,1,6),(5550,'Unknown LU Object/ number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210003',52,1,6),(5551,'LU allocate connection failed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210005',52,1,6),(5552,'LU look NAT for /IP_address/ failed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210006',52,1,6),(5548,'LU SW_Module_Name error = number',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210001',52,1,6),(5549,'LU allocate block (bytes) failed.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-210002',52,1,6),(5547,'Discard IP fragment set with more than number elements ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-209005',52,1,6),(5546,'Invalid IP fragment, size = bytes exceeds maximum ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-209004',52,1,6),(5545,'Fragment database limit of number exceeded src = ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-209003',52,1,6),(5544,'(functionline_num) pix clear command return code',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-208005',52,1,6),(5541,'TCP connection limit of /number/ for host /IP_address/ on ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-201009',52,1,6),(5542,'Out of address translation slots!',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-202001',52,1,6),(5543,'Non-embryonic in embryonic list outside_address/outside_port inside_address/inside_port',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-202005',52,1,6),(5539,'RCMD backconnection failed for IP_address/port',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-201006',52,1,6),(5540,'The PIX is disallowing new connections.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-201008',52,1,6),(5538,'FTP data connection failed for IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-201005',52,1,6),(5537,'Embryonic limit exceeded nconns/elimit for ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-201003',52,1,6),(5536,'Too many connections on {static|xlate} global_address! ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-201002',52,1,6),(5534,'PIX startup completed. Beginning operation.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-199002',52,1,6),(5535,'PIX Startup begin',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-199005',52,1,6),(5533,'PIX reload command executed from telnet (remote IP_address).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-199001',52,1,6),(5531,'User /user/ executed cmd/string',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-111009',52,1,6),(5532,'(/string//dec/) PIX Clear complete.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-112001',52,1,6),(5530,'User /user/ executed the command /string',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111008',52,1,6),(5528,'IP_address end configuration OK',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111005',52,1,6),(5529,'Begin configuration IP_address reading from device.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111007',52,1,6),(5526,'IP_address Erase configuration',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111003',52,1,6),(5527,'IP_address end configuration {FAILED|OK}',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111004',52,1,6),(5525,'Begin configuration IP_address writing to device',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-5-111002',52,1,6),(5524,'No route to dest_address from source_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-110001',52,1,6),(5523,'Authorization denied from source_IP_Address/src_port to dest_IP_Address/dest_port (not authenticated',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-109024',52,1,6),(5522,'User from src_IP_Adress/src_port to dest_IP_Address/dest_port on interface outside must authenticate',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109023',52,1,6),(5521,'exceeded HTTPS proxy process limit',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-109022',52,1,6),(5520,'Uauth null proxy error',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-109021',52,1,6),(5518,'Downloaded ACL /acl_ID/ has parsing error; ACE /string/',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109019',52,1,6),(5517,'Downloaded ACL /acl_ID/ is empty',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109018',52,1,6),(5519,'Downloaded ACL has config error; ACE',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109020',52,1,6),(5514,'User must authenticate before using this service',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109013',52,1,6),(5515,'uauth_lookup_net fail for uauth_in()',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-7-109014',52,1,6),(5516,'User at /IP_address/ exceeded auth proxy connection limit (max)',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-109017',52,1,6),(5512,'Auth from inside_address to outside_address/outside_port failed (all servers failed) on interface ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-109003',52,1,6),(5513,'Auth from inside_address/inside_port to outside_address/outside_port failed (too many pending auths)',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-109010',52,1,6),(5510,'Auth start for user user from inside_address//inside_port/ to outside_address//outside_port',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-109001',52,1,6),(5511,'Auth from inside_address/inside_port to outside_address/outside_port failed (server IP_address faile',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-109002',52,1,6),(5509,'SMTP replaced string out source_address in inside_address data string',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-108002',52,1,6),(5508,'RIP pkt failed from IP_address version=number on interface interface_name',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-107002',52,1,6),(5506,'The number of ACL log deny-flows has reached limit (/number/).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-106101',52,1,6),(5507,'RIP auth failed from IP_address version=number, type=string, mode=string, sequence=number on interfa',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-107001',52,1,6),(5504,'Dropping invalid echo {request|reply} from ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106028',52,1,6),(5505,'access-list /acl_ID/ {permitted | denied | est-allowed}/ ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-/n/-106100',52,1,6),(5503,'Deny protocol src ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-4-106023',52,1,6),(5502,'Deny protocol connection spoof from source_address to dest_address on interface interface_name',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-106022',52,1,6),(5501,'Deny protocol reverse path check from source_address to dest_address on interface interface_name',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-106021',52,1,6),(5500,'Deny IP teardrop fragment (size = number, offset = number) from IP_address to IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106020',52,1,6),(5499,'ICMP packet type ICMP_type denied by outbound list acl_ID src inside_address dest outside_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106018',52,1,6),(5497,'Deny IP spoof from (IP_address) to IP_address on  interface interface_name.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106016',52,1,6),(5498,'Deny IP due to Land Attack from IP_address to IP_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106017',52,1,6),(5495,'Deny inbound icmp src interface_name IP_address dst interface_name IP_address (type dec, code dec) ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-106014',52,1,6),(5496,'Deny TCP (no connection) from IP_address/port to IP_address/port flags tcp_flags on interface interf',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-6-106015',52,1,6),(5494,'Dropping echo request from IP_address to PAT address ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106013',52,1,6),(5492,'Deny inbound (No xlate) string',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-106011',52,1,6),(5493,'Deny IP from IP_address to IP_address, IP options hex.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106012',52,1,6),(5491,'Deny inbound /protocol/ src ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-106010',52,1,6),(5490,'Deny inbound UDP from outside_address/outside_port to  inside_address/inside_port due to DNS {Respon',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106007',52,1,6),(5489,'Deny inbound UDP from outside_address/outside_port to  inside_address/inside_port on interface inter',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106006',52,1,6),(5488,'protocol Connection denied by outbound list acl_ID src inside_address dest outside_address',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106002',52,1,6),(5487,'Inbound TCP connection denied from IP_address/port to IP_address/port flags tcp_flags on interface i',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-2-106001',52,1,6),(5485,'PIX dropped a LAN Failover command message.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105036',52,1,6),(5486,'The primary and standby units are switching back and ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105037',52,1,6),(5480,'(Primary) Incomplete/slow config replication',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105020',52,1,6),(5481,'Failover LAN interface is up',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105031',52,1,6),(5482,'LAN Failover interface is down',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105032',52,1,6),(5484,'Receive a LAN failover interface down msg from peer.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105035',52,1,6),(5483,'Receive a LAN_FAILOVER_UP message from peer.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105034',52,1,6),(5476,'(Primary) Testing interface interface_name.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105008',52,1,6),(5477,'(Primary) Testing on interface interface_name {Passed|Failed}.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105009',52,1,6),(5478,'(Primary) Failover message block alloc failed',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-3-105010',52,1,6),(5479,'(Primary) Failover cable communication failure',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105011',52,1,6),(5475,'(Primary) Lost Failover communications with mate on interface interface_name.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105005',52,1,6),(5473,'(Primary) Monitoring on interface interface_name waiting',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105003',52,1,6),(5474,'(Primary) Monitoring on interface interface_name normal',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105004',52,1,6),(5468,'(Primary) Switching to STNDBY (cause /string/).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-104002',52,1,6),(5469,'(Primary) Switching to FAILED.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-104003',52,1,6),(5470,'(Primary) Switching to OK.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-104004',52,1,6),(5472,'(Primary) Enabling failover.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105002',52,1,6),(5471,'(Primary) Disabling failover.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-105001',52,1,6),(5464,'(Primary) Other firewall network interface interface_number failed.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-103003',52,1,6),(5465,'(Primary) Other firewall reports this firewall failed.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-103004',52,1,6),(5466,'(Primary) Other firewall reporting failure.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-103005',52,1,6),(5467,'(Primary) Switching to ACTIVE (cause /string/).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-104001',52,1,6),(5456,'(Primary) Failover cable OK.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-101001',52,1,6),(5457,'(Primary) Bad failover cable. ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-101002',52,1,6),(5458,'(Primary) Failover cable not connected (this unit).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-101003',52,1,6),(5459,'(Primary) Failover cable not connected (other unit).',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-101004',52,1,6),(5460,'(Primary) Error reading failover cable status.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-101005',52,1,6),(5461,'(Primary) Power failure/System reload other side.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-102001',52,1,6),(5462,'(Primary) No response from other firewall (reason ',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-103001',52,1,6),(5463,'(Primary) Other firewall network interface interface_number OK.',1016,'\\w\\w\\w\\s+\\d\\d\\s+\\d\\d.\\d\\d.\\d\\d\\s+(.+?)\\s.*PIX-1-103002',52,1,6);
UNLOCK TABLES;
/*!40000 ALTER TABLE `ParsingRule` ENABLE KEYS */;

--
-- Table structure for table `ParsingRule_ComponentType`
--

DROP TABLE IF EXISTS `ParsingRule_ComponentType`;
CREATE TABLE `ParsingRule_ComponentType` (
  `parsingRuleID` int(11) NOT NULL,
  `componentTypeID` int(11) NOT NULL,
  `precedence` varchar(40) default NULL,
  PRIMARY KEY  (`parsingRuleID`,`componentTypeID`),
  KEY `IDX_ParsingRule_ComponentType1` (`parsingRuleID`),
  KEY `IDX_ParsingRule_ComponentType2` (`componentTypeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ParsingRule_ComponentType`
--


/*!40000 ALTER TABLE `ParsingRule_ComponentType` DISABLE KEYS */;
LOCK TABLES `ParsingRule_ComponentType` WRITE;
INSERT INTO `ParsingRule_ComponentType` VALUES (76,7,'1'),(76,2,'2'),(77,7,'1'),(77,18,'2'),(78,7,'1'),(78,18,'2'),(79,7,'1'),(80,7,'1'),(80,18,'2'),(81,1,'1'),(81,2,'2'),(82,7,'1'),(82,18,'2'),(83,1,'1'),(83,2,'2'),(84,7,'1'),(84,18,'2'),(84,2,'3'),(85,7,'1'),(86,0,'1'),(87,0,'1'),(88,0,'1'),(89,0,'1'),(90,0,'1'),(91,0,'1'),(92,0,'1'),(93,0,'1'),(94,0,'1'),(95,0,'1'),(96,0,'1'),(97,0,'1'),(98,0,'1'),(99,0,'1'),(100,0,'1'),(101,0,'1'),(102,0,'1'),(103,0,'1'),(104,0,'1'),(105,0,'1'),(106,0,'1'),(107,0,'1'),(108,0,'1'),(109,0,'1'),(110,1,'1'),(110,19,'2'),(111,1,'1'),(111,2,'2'),(112,1,'1'),(112,19,'2'),(113,0,'1'),(114,2,'1'),(114,1,'2'),(115,1,'1'),(115,19,'2'),(116,1,'1'),(116,19,'2'),(117,2,'1'),(117,19,'2'),(1000,7,'1'),(1001,0,'1'),(1002,0,'1'),(1003,0,'1'),(1004,0,'1'),(1005,0,'1'),(1006,0,'1'),(1007,0,'1'),(1008,0,'1'),(1009,0,'1'),(1010,0,'1'),(1012,0,'1'),(1013,0,'1'),(1014,0,'1'),(1015,0,'1'),(1016,0,'1'),(1017,0,'1'),(1018,0,'1'),(1019,0,'1'),(1020,0,'1'),(1021,0,'1'),(1022,0,'1'),(1024,0,'1'),(1025,0,'1'),(1026,0,'1'),(1023,7,'1'),(1023,20,'1'),(93,18,'1'),(93,19,'1'),(93,20,'1'),(1015,18,'1'),(1015,19,'1'),(1015,20,'1'),(1015,21,'1'),(1015,22,'1'),(1016,18,'1'),(1016,19,'1'),(1016,23,'1'),(1016,24,'1'),(1016,25,'1'),(86,18,'1'),(86,19,'1'),(86,20,'1'),(86,21,'1'),(86,22,'1'),(88,18,'1'),(88,19,'1'),(88,20,'1'),(88,21,'1'),(88,22,'1'),(1008,18,'1'),(1008,26,'1'),(1008,27,'1'),(1008,28,'1'),(1009,18,'1'),(1009,29,'1'),(1009,26,'1'),(1009,27,'1'),(1009,30,'1'),(1014,18,'1'),(1014,26,'1'),(1014,27,'1'),(1013,18,'1'),(1013,29,'1'),(1013,26,'1'),(1013,27,'1'),(1012,18,'1'),(1012,27,'1'),(1012,26,'1'),(1012,29,'1'),(1024,18,'1'),(1024,1,'1'),(1024,19,'1'),(1027,0,'1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `ParsingRule_ComponentType` ENABLE KEYS */;

--
-- Table structure for table `Severity`
--

DROP TABLE IF EXISTS `Severity`;
CREATE TABLE `Severity` (
  `severityID` int(11) NOT NULL auto_increment,
  `SeverityName` varchar(60) NOT NULL,
  PRIMARY KEY  (`severityID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Severity`
--


/*!40000 ALTER TABLE `Severity` DISABLE KEYS */;
LOCK TABLES `Severity` WRITE;
INSERT INTO `Severity` VALUES (1,'Emergency'),(2,'Alert'),(3,'Critical'),(4,'Error'),(5,'Warning'),(6,'Notification'),(7,'Informational'),(8,'Debug');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Severity` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

