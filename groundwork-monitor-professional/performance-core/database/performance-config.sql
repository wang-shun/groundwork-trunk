-- $Id: performance-config.sql,v 1.4 2006/01/11 19:50:36 rogerrut Exp $
-- Performance configuration stored in Foundation

--
-- Table structure for table `performanceconfig`
--

create table if not exists `performanceconfig` (
  `performanceconfig_id` smallint(4) unsigned NOT NULL auto_increment,
  `host` varchar(100) NOT NULL default '',
  `service` varchar(100) NOT NULL default '',
  `type` varchar(100) NOT NULL default '',
  `enable` tinyint(1) default '0',
  `parseregx_first` tinyint(1) default '0',
  `service_regx` tinyint(1) default '0',
  `label` varchar(100) NOT NULL default '',
  `rrdname` varchar(100) NOT NULL default '',
  `rrdcreatestring` TEXT NOT NULL default '',
  `rrdupdatestring` TEXT NOT NULL default '',
  `graphcgi` varchar(255) NOT NULL default '',
  `perfidstring` varchar(100) NOT NULL default '',
  `parseregx` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`performanceconfig_id`),
  UNIQUE KEY `host` (`host`,`service`)
) ENGINE=InnoDB;
