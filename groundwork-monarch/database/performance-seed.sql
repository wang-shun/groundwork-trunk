-- Copyright 2007 GroundWork Open Source, Inc. (�GroundWork�)  
-- All rights reserved. This program is free software; you can redistribute it and/or 
-- modify it under the terms of the GNU General Public License version 2 as published 
-- by the Free Software Foundation.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT ANY 
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
-- PARTICULAR PURPOSE.  See the GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with this 
-- program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, 
-- Fifth Floor, Boston, MA 02110-1301, USA.
--
-- $Id: performance-config-seed.sql,v 1.5 2006/01/11 19:47:30 rogerrut Exp $
-- Performance configuration seed

DROP TABLE IF EXISTS `performanceconfig`;
CREATE TABLE `performanceconfig` (
  `performanceconfig_id` smallint(4) unsigned NOT NULL auto_increment,
  `host` varchar(100) NOT NULL default '',
  `service` varchar(100) NOT NULL default '',
  `type` varchar(100) NOT NULL default '',
  `enable` tinyint(1) default '0',
  `parseregx_first` tinyint(1) default '0',
  `service_regx` tinyint(1) default '0',
  `label` varchar(100) NOT NULL default '',
  `rrdname` varchar(100) NOT NULL default '',
  `rrdcreatestring` text NOT NULL,
  `rrdupdatestring` text NOT NULL,
  `graphcgi` varchar(255) NOT NULL default '',
  `perfidstring` varchar(100) NOT NULL default '',
  `parseregx` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`performanceconfig_id`),
  UNIQUE KEY `host` (`host`,`service`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


LOCK TABLES `performanceconfig` WRITE;


REPLACE INTO `performanceconfig` VALUES (1,'*','Current Load','nagios', 1, 0, 0,'Current Load - 15 Minute Average','/usr/local/groundwork/rrd/$HOST$_Current_Load.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:$LABEL3$:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE3$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),
(2,'*','Current Users','nagios', 1, 0, 0,'Current Users','/usr/local/groundwork/rrd/$HOST$_Current_Users.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:$LABEL1$:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),
(3,'*','Root Partition','nagios', 1, 0, 0,'Disk Utilization','/usr/local/groundwork/rrd/$HOST$_Root_Partition.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:root:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),
(4,'*','snmp_if_','nagios',1,1,1,'Interface Statistics','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:in:COUNTER:1800:U:U DS:out:COUNTER:1800:U:U DS:indis:COUNTER:1800:U:U DS:outdis:COUNTER:1800:U:U DS:inerr:COUNTER:1800:U:U  DS:outerr:COUNTER:1800:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032','$RRDTOOL$ update $RRDNAME$ -t in:out:indis:outdis:inerr:outerr $LASTCHECK$:$VALUE1$:$VALUE2$:$VALUE3$:$VALUE4$:$VALUE5$:$VALUE6$  2>&1','/nagios/cgi-bin/if_graph2.cgi',' ','SNMP OK - (\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)'),
(5,'*','snmp_ifbandwidth_','nagios',1,0,1,'Interface Bandwidth Utilization','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:in:COUNTER:1800:U:U DS:out:COUNTER:1800:U:U DS:ifspeed:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ -t in:out:ifspeed $LASTCHECK$:$VALUE1$:$VALUE2$:$VALUE3$ 2>&1','/nagios/cgi-bin/if_bandwidth_graph.cgi',' ','SNMP OK - (\\d+)\\s+(\\d+)\\s+(\\d+)'),
(6,'*','ssh_memory','nagios',1,0,0,'Memory Utilization','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:percent:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/percent_graph.cgi',' ','pct:\\s+([\\d\\.]+)'),
(7,'*','ssh_swap','nagios',1,1,0,'Swap Utilization','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:percent:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/percent_graph.cgi',' ','([\\d\\.]+)% free'),
(8,'*','ssh_disk','nagios',1,0,1,'Disk Utilization','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:disk:GAUGE:1800:U:U DS:warning:GAUGE:1800:U:U DS:critical:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$:$WARN1$:$CRIT1$ 2>&1','/nagios/cgi-bin/number_graph.cgi',' ',' '),
(9,'*','ssh_load','nagios',1,0,0,'Load Averages','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:load1:GAUGE:1800:U:U DS:load5:GAUGE:1800:U:U DS:load15:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ -t load1:load5:load15 $LASTCHECK$:$VALUE1$:$VALUE2$:$VALUE3$ 2>&1','/nagios/cgi-bin/load_graph.cgi',' ',' '),
(10,'*','tcp_ssh','nagios',1,0,0,'SSH Response Time','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:number:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/number_graph.cgi',' ',' '),
(11,'*','ssh_process','nagios',1,1,1,'Process Count','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:number:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/number_graph.cgi',' ','(\\d+) process'),
(12,'*','icmp_ping_alive','nagios',1,0,0,'ICMP Ping Response Time','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr DS:$LABEL1$:GAUGE:1800:U:U RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$ 2>&1','/nagios/cgi-bin/label_graph.cgi',' ',' '),
(13,'*','RDS\.','nagios',1,0,1,'AWS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(14,'*','EC2\.','nagios',1,0,1,'AWS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(15,'*','EBS\.','nagios',1,0,1,'AWS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(16,'*','memory','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(17,'*','memory-actual','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(18,'*','memory-rss','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(19,'*','syn(.)cpu','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(20,'*','tap(.+)_rx','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(21,'*','tap(.+)_rx_drop','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(22,'*','tap(.+)_rx_errors','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(23,'*','tap(.+)_rx_packets','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(24,'*','tap(.+)_tx','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(25,'*','tap(.+)_tx_drop','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(26,'*','tap(.+)_tx_errors','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(27,'*','tap(.+)_tx_packets','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(28,'*','vd(.)_read','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(29,'*','vd(.)_read_req','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(30,'*','vd(.)_write','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(31,'*','vd(.)_write_req','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(32,'*','cpu(.)_time','nagios',1,0,1,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(33,'*','free_disk_gb','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(34,'*','free_ram_mb','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(35,'*','running_vms','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(36,'*','cpu_util','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(37,'*','disk\.read\.bytes','nagios',1,0,0,'OS','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(38,'*','summary\.quick','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(39,'*','syn\.host','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(40,'*','perfcounter\.','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(41,'*','summary\.runtime','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(42,'*','summary\.storage','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(43,'*','syn\.vm\.','nagios',1,0,1,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(44,'*','summary\.capacity','nagios',1,0,0,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(45,'*','summary\.freeSpace','nagios',1,0,0,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(46,'*','summary\.uncommitted','nagios',1,0,0,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' '),
(47,'*','syn\.storage\.percent\.used','nagios',1,0,0,'VM','/usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd','$RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$ DS:$LABEL#$:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480','$RRDTOOL$ update $RRDNAME$ --template $LABELLIST$ $LASTCHECK$:$VALUELIST$ 2>&1',' ',' ',' ');


UNLOCK TABLES;
