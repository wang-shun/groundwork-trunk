-- MySQL dump 10.10
--
-- Host: localhost    Database: dashboard
-- ------------------------------------------------------
-- Server version	5.0.22-Debian_0ubuntu6.06-log

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
-- Table structure for table `dashboard`
--

DROP TABLE IF EXISTS `dashboard`;
CREATE TABLE `dashboard` (
  `id` int(7) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `background_color` varchar(20) default NULL,
  `background_image` varchar(255) default NULL,
  `global` enum('0','1') NOT NULL default '0',
  `uid` int(7) default NULL,
  `background_repeat_x` enum('0','1') default '0',
  `background_repeat_y` enum('0','1') default '0',
  `refresh` int(7) unsigned default '0',
  `isdefault` enum('0','1') NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Dashboard Properties';

-- get user_id for admin user
SET @admin_id = (SELECT user_id FROM guava_users WHERE username = 'admin');

--
-- Dumping data for table `dashboard`
--

/*!40000 ALTER TABLE `dashboard` DISABLE KEYS */;
LOCK TABLES `dashboard` WRITE, `guava_users` READ;
INSERT INTO `dashboard` (id,name,background_color,background_image,global,uid,background_repeat_x,background_repeat_y,refresh,isdefault) VALUES 
	(1,'systemdefault',	'#eeeeee',NULL,'0',(select user_id from guava.guava_users where username='admin'),'0','0',0,'1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dashboard` ENABLE KEYS */;


INSERT INTO `guava_preferences` (packagename,prefname,value) VALUES ('dashboards','systemdefault','1');


--
-- Table structure for table `widget`
--

DROP TABLE IF EXISTS `widget`;
CREATE TABLE `widget` (
  `id` int(7) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `class` varchar(255) NOT NULL,
  `y` int(7) NOT NULL default '0',
  `x` int(7) NOT NULL default '0',
  `width` int(7) NOT NULL default '0',
  `height` int(7) NOT NULL default '0',
  `zindex` int(7) NOT NULL default '0',
  `configuration` blob NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Dashboard Widgets';

--
-- Dumping data for table `widget`
--

/*!40000 ALTER TABLE `widget` DISABLE KEYS */;
LOCK TABLES `widget` WRITE;
-- Widgets 1-4 for default system wide dashboard
INSERT INTO `widget` VALUES 
(1,'Troubled Hosts List','GWWidgetsTroubledHostsListWidget',8,12,300,400,1,'N;'),
(2,'Troubled Services List','GWWidgetsTroubledServicesListWidget',8,328,300,400,1,'N;'),
(3,'Tactical Overview','GWWidgetsTacOverviewWidget',8,638,618,400,1,'s:17:\"SV2NagiosOverview\";'),
(4,'Console','GWWidgetsConsoleWidget',416,10,1238,400,1,'N;');
UNLOCK TABLES;
/*!40000 ALTER TABLE `widget` ENABLE KEYS */;

--
-- Table structure for table `widgetmap`
--

DROP TABLE IF EXISTS `widgetmap`;
CREATE TABLE `widgetmap` (
  `id` int(7) unsigned NOT NULL auto_increment,
  `dashboard_id` int(7) unsigned NOT NULL default '0',
  `widget_id` int(7) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Dashboard Widget Mapping';

--
-- Dumping data for table `widgetmap`
--

/*!40000 ALTER TABLE `widgetmap` DISABLE KEYS */;
LOCK TABLES `widgetmap` WRITE;
-- Default widget mappings for all default dashboards
INSERT INTO `widgetmap` (dashboard_id, widget_id) VALUES 
	(1,1),(1,2),(1,3),(1,4);
UNLOCK TABLES;
/*!40000 ALTER TABLE `widgetmap` ENABLE KEYS */;

--
-- Table structure for table `privileges`
--

DROP TABLE IF EXISTS `privileges`;
CREATE TABLE `privileges` (
  `id` int(7) NOT NULL auto_increment,
  `dashboard_id` int(7) NOT NULL default '0',
  `type` enum('group','role','user') NOT NULL default 'user',
  `target_id` int(7) NOT NULL default '0',
  `write` enum('0','1') default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Dashboard Privileges';

--
-- Dumping data for table `privileges`
--


/*!40000 ALTER TABLE `privileges` DISABLE KEYS */;
LOCK TABLES `privileges` WRITE;
-- Give administrator role write privileges on the default dashboards
INSERT INTO `privileges`
(dashboard_id, type,target_id,`write`) VALUES
(1,'role',1,'1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `privileges` ENABLE KEYS */;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

