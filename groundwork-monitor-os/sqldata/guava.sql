-- MySQL dump 10.10
--
-- Host: localhost    Database: guava
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

--
-- Table structure for table `guava_group_assignments`
--

DROP TABLE IF EXISTS `guava_group_assignments`;
CREATE TABLE `guava_group_assignments` (
  `group_id` int(11) unsigned NOT NULL default '0',
  `user_id` int(11) unsigned NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `guava_group_assignments`
--


/*!40000 ALTER TABLE `guava_group_assignments` DISABLE KEYS */;
LOCK TABLES `guava_group_assignments` WRITE;
INSERT INTO `guava_group_assignments` VALUES (3,10);
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_group_assignments` ENABLE KEYS */;

--
-- Table structure for table `guava_groups`
--

DROP TABLE IF EXISTS `guava_groups`;
CREATE TABLE `guava_groups` (
  `group_id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`group_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Mango Groups';

--
-- Dumping data for table `guava_groups`
--


/*!40000 ALTER TABLE `guava_groups` DISABLE KEYS */;
LOCK TABLES `guava_groups` WRITE;
INSERT INTO `guava_groups` VALUES (3,'Admins','Admins for the Network');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_groups` ENABLE KEYS */;

--
-- Table structure for table `guava_packages`
--

DROP TABLE IF EXISTS `guava_packages`;
CREATE TABLE `guava_packages` (
  `package_id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `version_major` int(2) NOT NULL default '0',
  `version_minor` int(2) NOT NULL default '0',
  `configclassname` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`package_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `guava_packages`
--


/*!40000 ALTER TABLE `guava_packages` DISABLE KEYS */;
LOCK TABLES `guava_packages` WRITE;
INSERT INTO `guava_packages` VALUES (1,'Guava Core',1,0,'LDAPConfigureView'),(2,'PHP Utility Classes',1,0,''),(3,'Groundwork Monarch EZ',1,0,''),(4,'Groundwork Monarch',1,0,''),(5,'Nagios',1,0,''),(6,'Nagios Map',1,0,''),(7,'Nagios Reports',1,0,''),(8,'Bookshelf',1,0,''),(9,'Groundwork Status Viewer 2',2,0,'SV2ConfigureView'),(10,'Reports',1,2,''),(11,'Performance Configuration',1,0,''),(13,'Monitoring Server',1,0,''),(14,'Groundwork Foundation',1,0,'FoundationConfigureView'),(16,'Performance',1,0,'');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_packages` ENABLE KEYS */;

--
-- Table structure for table `guava_preferences`
--

DROP TABLE IF EXISTS `guava_preferences`;
CREATE TABLE `guava_preferences` (
  `preference_id` int(11) unsigned NOT NULL auto_increment,
  `packagename` varchar(255) NOT NULL default '',
  `prefname` varchar(255) NOT NULL default '',
  `value` blob NOT NULL,
  PRIMARY KEY  (`preference_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `guava_preferences`
--


/*!40000 ALTER TABLE `guava_preferences` DISABLE KEYS */;
LOCK TABLES `guava_preferences` WRITE;
INSERT INTO `guava_preferences` VALUES (1,'guava','theme','14'),(2,'bookshelf','address','localhost'),(3,'bookshelf','username','guava'),(4,'bookshelf','password','gwrk'),(5,'bookshelf','dbname','guava'),(6,'mnogosearch','address','localhost'),(7,'mnogosearch','username','guava'),(8,'mnogosearch','password','gwrk'),(9,'mnogosearch','dbname','guava'),(10,'mnogosearch','indexerpath','/usr/local/groundwork/sbin/indexer'),(17,'sv2','comment_file','/usr/local/groundwork/nagios/var/nagioscomment.log'),(18,'sv2','downtime_file','/usr/local/groundwork/nagios/var/nagiosdowntime.log'),(19,'sv2','command_file','/usr/local/groundwork/nagios/var/spool/nagios.cmd'),(20,'foundation','dbtype','mysql'),(21,'foundation','address','localhost'),(22,'foundation','port','8080'),(23,'foundation','username','foundation'),(24,'foundation','password','gwrk'),(25,'foundation','dbname','GWCollageDB'),(26,'foundation','feederurl','tcp://localhost:4913'),(27,'foundation','webserviceurl','http://localhost:8080/foundation-webapp/services'),(28,'foundation','webservicename','foundation-webapp/services'),(29,'sv2','address','localhost'),(30,'sv2','username','monarch'),(31,'sv2','password','gwrk'),(32,'sv2','dbname','monarch'),(33,'sv2','rrdtoolpath','/usr/local/groundwork/bin/rrdtool');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_preferences` ENABLE KEYS */;

--
-- Table structure for table `guava_qolt`
--

DROP TABLE IF EXISTS `guava_qolt`;
CREATE TABLE `guava_qolt` (
  `object_id` int(11) unsigned NOT NULL auto_increment,
  `session_id` varchar(255) NOT NULL default '',
  `sysmodule` varchar(255) NOT NULL default '',
  `section` varchar(255) NOT NULL default '',
  `name` varbinary(255) NOT NULL default '',
  `value` text NOT NULL,
  PRIMARY KEY  (`object_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Quick Object Lookup Table';

--
-- Dumping data for table `guava_qolt`
--


/*!40000 ALTER TABLE `guava_qolt` DISABLE KEYS */;
LOCK TABLES `guava_qolt` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_qolt` ENABLE KEYS */;

--
-- Table structure for table `guava_role_assignments`
--

DROP TABLE IF EXISTS `guava_role_assignments`;
CREATE TABLE `guava_role_assignments` (
  `role_id` int(11) unsigned NOT NULL default '0',
  `user_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`user_id`, `role_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


--
-- Dumping data for table `guava_role_assignments`
--

--role_id: 1 Administrators, 2 Operators
--user_id: 10 admin, 11 joe
/*!40000 ALTER TABLE `guava_role_assignments` DISABLE KEYS */;
LOCK TABLES `guava_role_assignments` WRITE;
INSERT INTO `guava_role_assignments` (role_id,user_id) VALUES (1,10),(2,11);
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_role_assignments` ENABLE KEYS */;

--
-- Table structure for table `guava_roles`
--

DROP TABLE IF EXISTS `guava_roles`;
CREATE TABLE `guava_roles` (
  `role_id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
   `canExecute` int(11) unsigned NULL default 1,
  UNIQUE KEY (`name`),
  PRIMARY KEY  (`role_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Mango Groups';
--
-- Dumping data for table `guava_roles`
--


/*!40000 ALTER TABLE `guava_roles` DISABLE KEYS */;
LOCK TABLES `guava_roles` WRITE;
INSERT INTO `guava_roles` VALUES (1, 'Administrators','Administrators',1),(2, 'Operators','Operators',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_roles` ENABLE KEYS */;

--
-- Table structure for table `guava_roleviews`
--

DROP TABLE IF EXISTS `guava_roleviews`;
CREATE TABLE `guava_roleviews` (
  `roleview_id` int(11) unsigned NOT NULL auto_increment,
  `role_id` int(11) unsigned NOT NULL default '0',
  `view_id` int(11) unsigned NOT NULL default '0',
  `vieworder` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`roleview_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Guava applications attached to Groups';

--
-- Dumping data for table `guava_roleviews`
--

LOCK TABLES `guava_roleviews` WRITE;
/*!40000 ALTER TABLE `guava_roleviews` DISABLE KEYS */;
INSERT INTO `guava_roleviews` VALUES (10,1,14,17),(9,1,13,2),(8,1,12,1),(34,1,28,3),(12,1,16,5),(13,1,17,11),(14,1,
18,12),(16,1,20,4),(17,2,12,1),(20,2,17,11),(21,2,18,12),(36,1,33,8),(23,2,20,4),(24,2,21,16),(25,2,22,15),(26,1,23
,13),(27,1,25,10),(31,2,27,8),(32,2,25,10),(35,1,32,14),(37,1,34,9),(38,0,33,8),(39,1,30,6),(40,1,29,7),(41,1,21,16
),(42,1,22,15);
/*!40000 ALTER TABLE `guava_roleviews` ENABLE KEYS */;
UNLOCK TABLES;
 

--
-- Table structure for table `guava_sysmodules`
--

DROP TABLE IF EXISTS `guava_sysmodules`;
CREATE TABLE `guava_sysmodules` (
  `module_id` int(11) unsigned NOT NULL auto_increment,
  `modname` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`module_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='System Modules for Mango';

--
-- Dumping data for table `guava_sysmodules`
--


/*!40000 ALTER TABLE `guava_sysmodules` DISABLE KEYS */;
LOCK TABLES `guava_sysmodules` WRITE;
INSERT INTO `guava_sysmodules` VALUES (1,'GuavaLDAPAuthModule'),(3,'SV2SystemModule'),(4,'FoundationSystemModule');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_sysmodules` ENABLE KEYS */;

--
-- Table structure for table `guava_theme_modules`
--

DROP TABLE IF EXISTS `guava_theme_modules`;
CREATE TABLE `guava_theme_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `theme_id` int(11) NOT NULL default '0',
  `name` varchar(255) NOT NULL default '',
  `file` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `guava_theme_modules`
--


/*!40000 ALTER TABLE `guava_theme_modules` DISABLE KEYS */;
LOCK TABLES `guava_theme_modules` WRITE;
INSERT INTO `guava_theme_modules` VALUES (32,14,'styles','themes/gwmos/styles/gwmos.css'),(33,14,'desktop','themes/gwmos/templates/desktop.xml'),(34,14,'login','themes/gwmos/templates/login.xml'),(35,14,'sidenav','themes/gwmos/templates/sidenavview.xml');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_theme_modules` ENABLE KEYS */;

--
-- Table structure for table `guava_themes`
--

DROP TABLE IF EXISTS `guava_themes`;
CREATE TABLE `guava_themes` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `title` varchar(255) NOT NULL default '',
  `version` varchar(255) NOT NULL default '',
  `author` varchar(255) NOT NULL default '',
  `email` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `guava_themes`
--


/*!40000 ALTER TABLE `guava_themes` DISABLE KEYS */;
LOCK TABLES `guava_themes` WRITE;
INSERT INTO `guava_themes` VALUES (14,'Groundwork Monitor Open Source','1.0','Taylor Dondich','tdondich@groundworkopensource.com');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_themes` ENABLE KEYS */;

--
-- Table structure for table `guava_users`
--

DROP TABLE IF EXISTS `guava_users`;
CREATE TABLE `guava_users` (
  `user_id` int(11) unsigned NOT NULL auto_increment,
  `username` varchar(255) NOT NULL default '',
  `password` varchar(255) NOT NULL default '',
  `enabled` enum('0','1') NOT NULL default '1',
  `password_expire` date NOT NULL default '0000-00-00',
  `password_change` enum('0','1') NOT NULL default '0',
  `group_id` int(11) NOT NULL default '1',
  `authmodule` varchar(255) NOT NULL default '',
  `default_role_id` int(11) unsigned NULL,
  UNIQUE KEY (`username`),
  PRIMARY KEY  (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Mango Users';

--
-- Dumping data for table `guava_users`
--


/*!40000 ALTER TABLE `guava_users` DISABLE KEYS */;
LOCK TABLES `guava_users` WRITE;
INSERT INTO `guava_users` VALUES (10, 'admin',md5('admin'),'1','0000-00-00','0',1,'',1),(11, 'joe',md5('joe'),'1','0000-00-00','0',1,'',2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_users` ENABLE KEYS */;

--
-- Table structure for table `guava_views`
--

DROP TABLE IF EXISTS `guava_views`;
CREATE TABLE `guava_views` (
  `view_id` int(11) unsigned NOT NULL auto_increment,
  `viewname` varchar(255) NOT NULL default '',
  `viewclass` varchar(255) NOT NULL default '',
  `viewdescription` varchar(255) default '',
  `viewicon` varchar(255) default NULL,
  PRIMARY KEY  (`view_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Mango Views';

--
-- Dumping data for table `guava_views`
--

UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_views` DISABLE KEYS */;
LOCK TABLES `guava_views` WRITE;
INSERT INTO `guava_views` VALUES (12,'Home','GuavaHomeView','','packages/guava/images/home.gif'),(13,'Administration','GuavaAdministrationView','Manage Users, Packages and Roles','packages/guava/images/config.gif'),(14,'Wrappit','GuavaWrappitView','Wrap around existing web applications.','packages/guava/images/wrappit.gif'),(15,'Configuration EZ','configurationezView','Configuration Made Simple','packages/ezmonarch/images/configuration.gif'),(16,'Configuration','configurationView','Utility to manage Nagios configuration files','packages/monarch/images/configuration.gif'),(17,'Nagios','nagiosView','Nagios Interface','packages/nagios/images/nagios.gif'),(18,'Nagios Map','nagiosmapView','Nagios Map Pages','packages/nagiosmap/images/nagios.gif'),(19,'Nagios Reports','nagiosreportsView','Nagios Report Pages','packages/nagiosreports/images/nagios.gif'),(20,'Bookshelf','bookshelfView','Product Documentation.','packages/bookshelf/images/bookshelf.gif'),(21,'Status','SV2Application','Enhanced View of your Network', 'packages/sv2/images/sv2.png'),(22,'Reports','reportsView','Insight Reports','packages/reports/images/reports.gif'),(23,'Performance Configuration','performanceconfigurationView','Utility to show and configure Performance Graphs','packages/performanceconfiguration/images/performance.gif'),(25,'Monitoring Server','monitoringserverView','Groundwork Monitoring Server Status Page','packages/monitoringserver/images/nagios_host.gif'),(27,'Performance','performanceView','Display data in RRD files as graphs.','packages/performance/images/performance.gif');
UNLOCK TABLES;
/*!40000 ALTER TABLE `guava_views` ENABLE KEYS */;

/*!40000 ALTER TABLE `guava_views` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

