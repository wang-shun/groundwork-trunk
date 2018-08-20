-- MySQL dump 10.9
--
-- Host: localhost    Database: monarch
-- ------------------------------------------------------
-- Server version	4.1.8-standard

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE="NO_AUTO_VALUE_ON_ZERO" */;

--
-- Table structure for table `access_list`
--

DROP TABLE IF EXISTS `access_list`;
CREATE TABLE `access_list` (
  `object` varchar(50) NOT NULL default '',
  `type` varchar(50) NOT NULL default '',
  `usergroup_id` smallint(4) unsigned NOT NULL default '0',
  `access_values` varchar(20) default NULL,
  PRIMARY KEY  (`object`,`type`,`usergroup_id`),
  KEY `usergroup_id` (`usergroup_id`),
  CONSTRAINT `access_list_ibfk_1` FOREIGN KEY (`usergroup_id`) REFERENCES `user_groups` (`usergroup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `access_list`
--


/*!40000 ALTER TABLE `access_list` DISABLE KEYS */;
LOCK TABLES `access_list` WRITE;
INSERT INTO `access_list` VALUES ('commands','design_manage',1,'add,modify,delete'),('commit','control',1,'full_control'),('contactgroups','design_manage',1,'add,modify,delete'),('contacts','design_manage',1,'add,modify,delete'),('contact_templates','design_manage',1,'add,modify,delete'),('escalations','design_manage',1,'add,modify,delete'),('export','design_manage',1,'add,modify,delete'),('extended_host_info_templates','design_manage',1,'add,modify,delete'),('extended_service_info_templates','design_manage',1,'add,modify,delete'),('files','control',1,'full_control'),('hostgroups','design_manage',1,'add,modify,delete'),('hosts','design_manage',1,'add,modify,delete'),('host_dependencies','design_manage',1,'add,modify,delete'),('host_templates','design_manage',1,'add,modify,delete'),('import','discover',1,'full_control'),('load','control',1,'full_control'),('match_strings','discover',1,'full_control'),('nagios_configuration','control',1,'full_control'),('nmap','discover',1,'full_control'),('parent_child','design_manage',1,'add,modify,delete'),('pre_flight_test','control',1,'full_control'),('process_stage','discover',1,'full_control'),('profiles','design_manage',1,'add,modify,delete'),('resource','control',1,'full_control'),('run_external_scripts','control',1,'full_control'),('services','design_manage',1,'add,modify,delete'),('service_dependency_templates','design_manage',1,'add,modify,delete'),('service_templates','design_manage',1,'add,modify,delete'),('setup','control',1,'full_control'),('time_periods','design_manage',1,'add,modify,delete'),('users','control',1,'full_control'),('user_groups','control',1,'full_control');
UNLOCK TABLES;
/*!40000 ALTER TABLE `access_list` ENABLE KEYS */;

--
-- Table structure for table `commands`
--

DROP TABLE IF EXISTS `commands`;
CREATE TABLE `commands` (
  `command_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `type` varchar(50) default NULL,
  `data` text,
  `comment` text,
  PRIMARY KEY  (`command_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `commands`
--


/*!40000 ALTER TABLE `commands` DISABLE KEYS */;
LOCK TABLES `commands` WRITE;
INSERT INTO `commands` VALUES (1,'chk_nt_counter_mssql_bufcache_hits','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Buffer Manager\\\\Buffer cache hit ratio\",\"SQLServer:Buffer Manager Buffer cache hit ratio is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(2,'chk_alive','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_icmp -H $HOSTADDRESS$ -w 3000.0,80% -c 5000.0,100% -n 1]]>\n </prop>\n</data>','#====================================================================================================\n'),(3,'chk_by_ssh_log','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_log2.pl -l $ARG1$ -s $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(4,'chk_udp_http','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 80]]>\n </prop>\n</data>','#====================================================================================================\n'),(5,'chk_snmp_bandwidth','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -C $USER7$ -o IF-MIB::ifInOctets.$ARG1$,IF-MIB::ifOutOctets.$ARG1$,IF-MIB::ifSpeed.$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(6,'chk_nt_counter_mssql_memory_grants_pending','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Memory Manager\\\\Memory Grants Pending\",\"SQLServer:Memory Manager Memory Grants Pending is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(7,'report-nagios-status-bad','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/local/nagios/libexec/noreport.sh $HOSTNAME$]]>\n </prop>\n</data>',NULL),(8,'chk_ping','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_icmp -H $HOSTADDRESS$ -w $ARG1$ -c $ARG2$ -n 5]]>\n </prop>\n</data>','#====================================================================================================\n'),(9,'chk_https','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -S -I $HOSTADDRESS$ -f follow -a $USER13$:$USER14$ -u $ARG1$ -s $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(10,'chk_by_sshid_swap_stats','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_swap_stats -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(11,'check_dummy','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dummy 0]]>\n </prop>\n</data>','# \'check_dummy\' command definition\n'),(12,'chk_ifstatus_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ifstatus -C $USER8$ -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(13,'chk_nrpe_exch_mta_workq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter -a \"MSExchangeMTA\" \"Work Queue Length\" \"Work Queue Length is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(14,'chk_by_ssh_disks','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_disk -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(15,'chk_nt_counter_exch_publicrq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\MSExchangeIS Public(_Total)\\\\Receive Queue Size\",\"Receive Queue Size is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(16,'chk_tcp_ssh','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 22]]>\n </prop>\n</data>','#====================================================================================================\n'),(17,'chk_ntp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ntp.pl -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(18,'chk_udp_icmp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 5813]]>\n </prop>\n</data>','#====================================================================================================\n'),(19,'chk_udp_pop3','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 110]]>\n </prop>\n</data>','#====================================================================================================\n'),(20,'chk_by_sshid_process_cmd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -C $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(21,'chk_by_sshid_users','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_users $ARG1$ $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(22,'chk_nrpe_mssql_log_used','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Databases(_Total)\\\\Percent Log Used\" \"SQLServer::Databases(_Total) Percent Log Used is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(23,'chk_nw_tcb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v TCB]]>\n </prop>\n</data>','#====================================================================================================\n'),(24,'chk_tcp_http','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 80]]>\n </prop>\n</data>','#====================================================================================================\n'),(25,'chk_by_sshid_mailq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_mailq -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(26,'notify-by-email','notify','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/local/bin/sendEmail -s $USER3$ -f loki@itgroundwork.com -t $CONTACTEMAIL$ -u \"Problem: $HOSTNAME$: $SERVICEDESC$ State: $SERVICESTATE$\" -m \"$OUTPUT$ Date: $DATETIME$\"]]>\n </prop>\n</data>','# \'notify-by-email\' command definition\n'),(27,'chk_by_sshid_nagios','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_nagios -F /var/log/nagios/status.log -e 5 -C bin/nagios\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(28,'chk_nrpe_exch_mailbox_sendq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"MSExchangeIS Mailbox\" \"Send Queue Size\" \"_Total\" \"Send Queue Size is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(29,'chk_nw_nlm','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v NLM]]>\n </prop>\n</data>','#====================================================================================================\n'),(30,'chk_udp_nsclient','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p $USER19$]]>\n </prop>\n</data>','#====================================================================================================\n'),(31,'chk_http_noauth','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -I $HOSTADDRESS$ -f follow -u $ARG1$ -s $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(32,'chk_udp_imap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 143]]>\n </prop>\n</data>','#====================================================================================================\n'),(33,'chk_nrpe_iis_bytes_received','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Web Service\\\\Bytes Received/sec\" \"Web Service:Bytes Received/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(34,'chk_nw_abends','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v ABENDS]]>\n </prop>\n</data>','#====================================================================================================\n'),(35,'chk_nw_load5','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LOAD5]]>\n </prop>\n</data>','#====================================================================================================\n'),(36,'chk_nt_servicestate','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v SERVICESTATE -l $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(37,'chk_nt_useddiskspace','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v USEDDISKSPACE -l $ARG1$ -w $ARG2$ -c $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(38,'chk_nrpe_mssql_users','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:General Statistics\\\\User Connections\" \"SQLServer:General Statistics User Connections is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(39,'check_local_users','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_users -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','# \'check_local_users\' command definition\n'),(40,'chk_nw_puprb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v PUPRB]]>\n </prop>\n</data>','#====================================================================================================\n'),(41,'chk_nt_pagefile','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\Paging File(_Total)\\\\% Usage\",\"Paging File usage is %.2f %%\" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(42,'chk_by_sshid_connections','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_connections $ARG1$ $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(43,'chk_cluster_service','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_cluster --service $ARG1$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(44,'chk_by_ssh_process_count','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(45,'chk_tcp_https','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 443]]>\n </prop>\n</data>','#====================================================================================================\n'),(46,'chk_by_ssh_process_args','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -a $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(47,'chk_local_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_disk -w $ARG1$ -c $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(48,'chk_hpjetdirect_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_hpjd -H $HOSTADDRESS$ -C $USER8$]]>\n </prop>\n</data>','#====================================================================================================\n'),(49,'chk_snmp_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o $ARG1$ -s $ARG2$ -l $ARG3$ -C $USER8$]]>\n </prop>\n</data>','#====================================================================================================\n'),(50,'chk_udp_pop3s','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 995]]>\n </prop>\n</data>','#====================================================================================================\n'),(51,'chk_nrpe_local_memory','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -t $USER3$ -c check_mem_counter -a $ARG1$ $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(52,'chk_nrpe_iis_get_requests','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Web Service\\\\Get Requests/sec\" \"Web Service:Get Requests/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(53,'chk_nt_counter_mssql_lock_waits','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Locks(_Total)\\\\Lock Waits/sec\",\"SQLServer:Locks(_Total) Lock Waits/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(54,'chk_nrpe_iis_post_requests','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Web Service\\\\Post Requests/sec\" \"Web Service:Post Requests/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(55,'chk_nrpe_iis_bytes_sent','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Web Service\\\\Bytes Sent/sec\" \"Web Service:Bytes Sent/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(56,'chk_snmp_port_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o .1.3.6.1.2.1.1.3.0 -C $USER8$]]>\n </prop>\n</data>','#====================================================================================================\n'),(57,'chk_cluster_host','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_cluster --host $ARG1$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(58,'chk_udp_snmp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 161]]>\n </prop>\n</data>','#====================================================================================================\n'),(59,'chk_remote_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_disk_remote -e ssh -H $HOSTADDRESS$ -l $USER17$ -i $USER18$/.ssh/id_dsa -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(60,'chk_nw_uptime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v UPTIME]]>\n </prop>\n</data>','#====================================================================================================\n'),(61,'chk_by_ssh_nagios','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_nagios -F /var/log/nagios/status.log -e 5 -C bin/nagios\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(62,'chk_local_mem','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_mem -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(63,'chk_by_ssh_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_disk -w $ARG1$ -c $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(64,'chk_nrpe_exch_public_sendq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"MSExchangeIS Public\" \"Send Queue Size\" \"_Total\" \"Send Queue Size is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(65,'chk_nt_counter_disktransfers','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\PhysicalDisk(_Total)\\\\Disk Transfers/sec\",\"PhysicalDisk(_Total) Disk Transfers/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(66,'chk_udp_pop2','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 109]]>\n </prop>\n</data>','#====================================================================================================\n'),(67,'chk_tcp_pop3s','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 995]]>\n </prop>\n</data>','#====================================================================================================\n'),(68,'chk_udp_snmptrap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 162]]>\n </prop>\n</data>','#====================================================================================================\n'),(69,'check_local_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_load -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','# \'check_local_load\' command definition\n'),(70,'chk_http','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -I $HOSTADDRESS$ -f follow -a $USER13$:$USER14$ -u $ARG1$ -s $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(71,'chk_remote_swap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_swap_remote -e ssh -H $HOSTADDRESS$ -l $USER17$ -i $USER18$/.ssh/id_dsa -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(72,'check_ftp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ftp -H $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_ftp\' command definition\n'),(73,'chk_nt_clientversion','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v CLIENTVERSION]]>\n </prop>\n</data>','#====================================================================================================\n'),(74,'chk_by_sshid_process_count','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(75,'chk_nrpe_mssql_transactions','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Databases(_Total)\\\\Transactions/sec\" \"SQLServer:Databases(_Total) Transactions/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(76,'chk_udp_nsca','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 5667]]>\n </prop>\n</data>','#====================================================================================================\n'),(77,'check_http','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -H $HOSTADDRESS$ -f follow -L]]>\n </prop>\n</data>','# \'check_http\' command definition\n'),(78,'chk_dhcp_if','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dhcp -H $HOSTADDRESS$ -t $USER3$ -i $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(79,'chk_file_age','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_file_age -f $ARG1 -w $ARG2$ -c $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(80,'chk_nrpe_disktransfers','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\PhysicalDisk(_Total)\\\\Disk Transfers/sec\" \"PhysicalDisk(_Total) Disk Transfers/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(81,'chk_tcp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(82,'chk_snmp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o $ARG1$ -r $ARG2$ -l $ARG3$ -C $USER7$]]>\n </prop>\n</data>','#====================================================================================================\n'),(83,'chk_tcp_nsca','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 5667]]>\n </prop>\n</data>','#====================================================================================================\n'),(84,'chk_nrpe_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_disk_vb -a \"$HOSTADDRESS$\" \"$ARG2$\" \"$ARG3$\" \"$ARG4$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(85,'check_nntp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nntp -H $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_nntp\' command definition\n'),(86,'chk_nw_uprb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v UPRB]]>\n </prop>\n</data>','#====================================================================================================\n'),(87,'chk_nw_dsdb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v DSDB]]>\n </prop>\n</data>','#====================================================================================================\n'),(88,'chk_nt_counter_mssql_deadlocks','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Locks(_Total)\\\\Number of Deadlocks/sec\",\"SQLServer:Locks(_Total) Number of Deadlocks/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(89,'chk_by_sshid_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_load -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(90,'chk_by_sshid','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"$ARG1$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(91,'check_udp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p $ARG1$]]>\n </prop>\n</data>','# \'check_udp\' command definition\n'),(92,'chk_nw_load1','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LOAD1]]>\n </prop>\n</data>','#====================================================================================================\n'),(93,'chk_local_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_load -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(94,'chk_ica_master_browser','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ica_master_browser.pl -I $HOSTADDRESS$ -P $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(95,'chk_nagios','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nagios -F /var/log/nagios/status.log -e 5 -C bin/nagios]]>\n </prop>\n</data>','#====================================================================================================\n'),(96,'chk_disk_smb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_disk_smb.pl -H $HOSTADDRESS$ -s $ARG1$ -u \'$USER9$\' -p $USER10$ -w $ARG2$ -c $ARG3$ -W $ARG4$]]>\n </prop>\n</data>','#====================================================================================================\n'),(97,'chk_nt_counter_perf','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER  -l $ARG1$,\"$ARG2$ is %.f \"  -w $ARG3$ -c $ARG4$]]>\n </prop>\n</data>','#====================================================================================================\n'),(98,'chk_by_ssh_cpu','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_cpu $ARG1$:$ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(99,'chk_dns_expect','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dns -s $HOSTADDRESS$ -H $ARG1$ -a $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(100,'chk_nw_sapentries','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v SAPENTRIES]]>\n </prop>\n</data>','#====================================================================================================\n'),(101,'chk_by_ssh_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_load -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(102,'check_imap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_imap -H $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_imap\' command definition\n'),(103,'chk_by_sshid_process_usercmd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -u $ARG3$ -C $ARG4$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(104,'chk_by_sshid_mem','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_mem -u -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(105,'chk_by_ssh_connections','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$  -l $USER17$ -C \"libexec/check_connections $ARG1$ $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(106,'chk_by_ssh_uptime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_uptime $HOSTADDRESS$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(107,'chk_by_ssh_process_cmd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -C $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(108,'chk_nrpe_mssql_latch_wait_time','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Latches\\\\Average Latch Wait Time (ms)\" \"SQLServer:Latches Average Latch Wait Time (ms) is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(109,'chk_snmp_port','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o .1.3.6.1.2.1.1.3.0 -C $USER7$]]>\n </prop>\n</data>','#====================================================================================================\n'),(110,'chk_nw_dsver','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v DSVER]]>\n </prop>\n</data>','#====================================================================================================\n'),(111,'chk_nrpe_memory_pages','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Memory\\\\Pages/sec\" \"Pages per Sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(112,'check_local_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_disk -m -w $ARG1$ -c $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','# \'check_local_disk\' command definition\n'),(113,'chk_pop','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_pop -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(114,'chk_by_sshid_process_user','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -u $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(115,'chk_udp_nrpe','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 5666]]>\n </prop>\n</data>','#====================================================================================================\n'),(116,'chk_by_sshid_log','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_log2.pl -l $ARG1$ -s $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(117,'chk_nrpe_iis_bytes_total','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\Web Service\\\\Bytes Total/sec\" \"Web Service:Bytes Total/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(118,'chk_by_ssh_process_userargs','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -a $ARG3$ -u $ARG4$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(119,'chk_snmp_regex_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o $ARG1$ -r $ARG2$ -l $ARG3$ -C $USER8$]]>\n </prop>\n</data>','#====================================================================================================\n'),(120,'chk_by_sshid_process_args','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -a $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(121,'chk_oracle_tns','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_oracle --tns $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(122,'chk_nt_counter_mssql_latch_waits','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Latches\\\\Latch Waits/sec\",\"SQLServer:Latches Latch Waits/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(123,'chk_nrpe_local_pagefile','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -t $USER3$ -c check_pagefile_counter -a $ARG1$ $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(124,'chk_nrpe_print_queue','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -c check_counter_instance -a \"Print Queue\" \"Jobs\" \"_Total\" \"Print Queue has %f Jobs\" $ARG1$ $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(125,'chk_by_sshid_disks','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_disk -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(126,'chk_tcp_http-alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 8080]]>\n </prop>\n</data>','#====================================================================================================\n'),(127,'chk_nw_logins','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LOGINS]]>\n </prop>\n</data>','#====================================================================================================\n'),(128,'chk_nw_cdbuff','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v CDBUFF]]>\n </prop>\n</data>','#====================================================================================================\n'),(129,'check_dns','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dns -H www.google.com -s $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_dns\' command definition\n'),(130,'chk_nrpe_mssql_lock_waits','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Locks(_Total)\\\\Lock Waits/sec\" \"SQLServer:Locks(_Total) Lock Waits/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(131,'chk_by_sshid_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_disk -w $ARG1$ -c $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(132,'chk_nw_ltch','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LTCH]]>\n </prop>\n</data>','#====================================================================================================\n'),(133,'chk_demo','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_demo $ARG1$ $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(134,'chk_ica_metaframe_pub_apps','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ica_metaframe_pub_apps.pl -C $HOSTADDRESS$ -W  $ARG1$ -P $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(135,'chk_nt_procstate_process','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v PROCSTATE -l $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(136,'chk_smtp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_smtp -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(137,'check_smtp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_smtp -H $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_smtp\' command definition\n'),(138,'chk_nw_conns','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v CONNS]]>\n </prop>\n</data>','#====================================================================================================\n'),(139,'chk_by_ssh_users','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_users $ARG1$ $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(140,'chk_nrpe_exch_mailbox_receiveq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"MSExchangeIS Mailbox\" \"Receive Queue Size\" \"_Total\" \"Receive Queue Size is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(141,'chk_tcp_telnet','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 23]]>\n </prop>\n</data>','#====================================================================================================\n'),(142,'chk_nrpe_mssql_deadlocks','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Locks(_Total)\\\\Number of Deadlocks/sec\" \"SQLServer:Locks(_Total) Number of Deadlocks/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(143,'chk_oracle_login','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_oracle --login $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(144,'chk_dummy','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dummy 0]]>\n </prop>\n</data>','#====================================================================================================\n'),(145,'chk_nt_counter_disktime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\PhysicalDisk(_Total)\\\\\\% Disk Time\",\"% Disk Time is %.f\" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(146,'chk_nw_dcb','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v DCB]]>\n </prop>\n</data>','#====================================================================================================\n'),(147,'chk_by_ssh_mem','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$  -l $USER17$ -C \"libexec/check_mem -u -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(148,'chk_nt_counter_exch_mtawq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\MSExchangeMTA\\\\Work Queue Length\",\"Work Queue Length is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(149,'chk_nmap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nmap.py -H $HOSTADDRESS$ -p $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(150,'chk_nw_cbuff','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v CBUFF]]>\n </prop>\n</data>','#====================================================================================================\n'),(151,'notify-by-epager','notify','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/local/bin/sendEmail -s $USER3$ -f loki@itgroundwork.com -t $CONTACTPAGER$ -u \"Problem: $HOSTNAME$: $SERVICEDESC$ State: $SERVICESTATE$\" -m \"$OUTPUT$ Date: $DATETIME$\"]]>\n </prop>\n</data>','# \'notify-by-epager\' command definition\n'),(152,'chk_dns','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dns -s $HOSTADDRESS$ -H $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(153,'chk_wins','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_wins.pl -W $HOSTADDRESS$ -D $ARG1$ -C $ARG2$ -T $USER3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(154,'chk_nw_csprocs','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v CSPROCS]]>\n </prop>\n</data>','#====================================================================================================\n'),(155,'chk_tcp_nrpe','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 5666]]>\n </prop>\n</data>','#====================================================================================================\n'),(156,'chk_nt_cpuload_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v CPULOAD -l $ARG1$ -w $ARG2$ -c $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(157,'chk_nrpe_mssql_bufcache_hits','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Buffer Manager\\\\Buffer cache hit ratio\" \"SQLServer:Buffer Manager Buffer cache hit ratio is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(158,'chk_by_sshid_uptime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_uptime $HOSTADDRESS$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(159,'chk_by_ssh_process_user','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -u $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(160,'chk_nrpe','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -t $USER3$ -c $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(161,'chk_alive_tcp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 23]]>\n </prop>\n</data>','#====================================================================================================\n'),(162,'chk_ftp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ftp -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(163,'chk_nrpe_mssql_log_growths','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Databases(_Total)\\\\Log Growths\" \"SQLServer::Databases(_Total) Log Growths is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(164,'chk_udp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(165,'chk_nntp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nntp -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(166,'chk_udp_imap3','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 220]]>\n </prop>\n</data>','#====================================================================================================\n'),(167,'chk_by_ssh_mysql','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_mysql -H $HOSTADDRESS$ -d $ARG1$ -u $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(168,'chk_by_sshid_cpu','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_cpu $ARG1$:$ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(169,'chk_sweep','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_sweep]]>\n </prop>\n</data>','#====================================================================================================\n'),(170,'chk_snmp_regex','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -o $ARG1$ -r $ARG2$ -l $ARG3$ -C $USER7$]]>\n </prop>\n</data>','#====================================================================================================\n'),(171,'chk_nt_counter_exch_mailrq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\MSExchangeIS Mailbox(_Total)\\\\Receive Queue Size\",\"Receive Queue Size is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(172,'chk_nt_counter_diskquelength','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\PhysicalDisk(_Total)\\\\Current Disk Queue Length\",\"Current Disk Queue Length is %.f\" -w 2 -c 5]]>\n </prop>\n</data>','#====================================================================================================\n'),(173,'chk_by_sshid_swap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_swap -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(174,'check_ping','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ping -H $HOSTADDRESS$ -w $ARG1$ -c $ARG2$ -p 5]]>\n </prop>\n</data>','# \'check_ping\' command definition\n'),(175,'chk_nrpe_cpu','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_cpu_vb -a $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(176,'chk_tcp_pop2','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 109]]>\n </prop>\n</data>','#====================================================================================================\n'),(177,'chk_udp_telnet','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 23]]>\n </prop>\n</data>','#====================================================================================================\n'),(178,'chk_udp_smtp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 25]]>\n </prop>\n</data>','#====================================================================================================\n'),(179,'chk_citrix','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ica_master_browser.pl -I $HOSTADDRESS$ -P $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(180,'chk_uptime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_uptime -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(181,'check_telnet','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 23]]>\n </prop>\n</data>','# \'check_telnet\' command definition\n'),(182,'chk_udp_https','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 443]]>\n </prop>\n</data>','#====================================================================================================\n'),(183,'chk_nrpe_cpu_usrpwd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_cpu_vb -a $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(184,'chk_ifoperstatus_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ifoperstatus -k $ARG1$ -H $HOSTADDRESS$ -C $USER8$]]>\n </prop>\n</data>','#====================================================================================================\n'),(185,'chk_hpjetdirect','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_hpjd -H $HOSTADDRESS$ -C $USER7$]]>\n </prop>\n</data>','#====================================================================================================\n'),(186,'chk_nt_counter_exch_mailsq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\MSExchangeIS Mailbox(_Total)\\\\Send Queue Size\",\"Send Queue Size is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(187,'chk_nw_load15','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LOAD15]]>\n </prop>\n</data>','#====================================================================================================\n'),(188,'chk_dhcp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dhcp -s $HOSTADDRESS$ -t $USER3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(189,'chk_by_sshid_mysql','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_mysql -H $HOSTADDRESS$ -d $ARG1$ -u $ARG2$ -p $ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(190,'chk_nrpe_mssql_memory_grants_pending','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Memory Manager\\\\Memory Grants Pending\" \"SQLServer:Memory Manager Memory Grants Pending is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(191,'chk_nt_counter_memory_pages','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\Memory\\\\Pages/sec\",\"Pages per Sec is %.f\" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(192,'chk_nt_memuse','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v MEMUSE -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(193,'chk_nt_uptime','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v UPTIME]]>\n </prop>\n</data>','#====================================================================================================\n'),(194,'chk_apache','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_apache.pl -H $HOSTADDRESS$ -l]]>\n </prop>\n</data>','#====================================================================================================\n'),(195,'chk_log','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_log2.pl -l $ARG1$ -s $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(196,'chk_mssql','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_mssql.sh $HOSTADDRESS$:1433 $USER11$ $USER12$ $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(197,'chk_nw_lrus','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LRUS]]>\n </prop>\n</data>','#====================================================================================================\n'),(198,'chk_local_users','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_users -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(199,'chk_tcp_nsclient','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p $USER19$]]>\n </prop>\n</data>','#====================================================================================================\n'),(200,'chk_email_loop','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_email_loop.pl -poph=$HOSTADDRESS$ -popu=$USER20$ -pa=$USER21$ -statfile=/var/log/nagios/email_loop.stat -smtphost=localhost  -to=$ARG1$ -from=$ARG2$ -lostcrit=$ARG3$ -pendcrit=$ARG4$ -lostwarn=$ARG5$ -pendwarn=$ARG6$]]>\n </prop>\n</data>','#====================================================================================================\n'),(201,'chk_alive_dum','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_dummy 2]]>\n </prop>\n</data>','#====================================================================================================\n'),(202,'chk_nrpe_local_cpu','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -c check_proc_counter -a $ARG1$ $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(203,'chk_ping_ip','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_icmp -H $ARG1$ -w $ARG2$ -c $ARG3$ -n 5]]>\n </prop>\n</data>','#====================================================================================================\n'),(204,'chk_http_basic','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -I $HOSTADDRESS$ -f follow]]>\n </prop>\n</data>','#====================================================================================================\n'),(205,'chk_nrpe_mssql_full_scans','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Access Methods\\\\Full Scans/sec\" \"SQLServer:Access Methods Full Scans/sec is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(206,'chk_nw_vkf','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG2$ -c $ARG3$ -v VKF$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(207,'chk_http_ereg','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_http -I $HOSTADDRESS$ -f follow -a $USER13$:$USER14$ -u $ARG1$ -s $ARG2$ -p $ARG3$ -R $ARG4$]]>\n </prop>\n</data>','#====================================================================================================\n'),(208,'check_pop','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_pop -H $HOSTADDRESS$]]>\n </prop>\n</data>','# \'check_pop\' command definition\n'),(209,'chk_nt_counter_mssql_log_growths','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Databases(_Total)\\\\Log Growths\",\"SQLServer:Databases(_Total) Log Growths is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(210,'chk_nw_ofiles','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v OFILES]]>\n </prop>\n</data>','#====================================================================================================\n'),(211,'chk_by_ssh','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"$ARG1\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(212,'chk_tcp_imap3','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 220]]>\n </prop>\n</data>','#====================================================================================================\n'),(213,'chk_snmp_if_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -C $USER8$ -o IF-MIB::ifInOctets.$ARG1$,IF-MIB::ifOutOctets.$ARG1$ ,IF-MIB::ifInDiscards.$ARG1$,IF-MIB::ifOutDiscards.$ARG1$,IF-MIB::ifInErrors.$ARG1$,IF-MIB::ifOutErrors.$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(214,'check_ping_ip','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ping -H $ARG1$ -w $ARG2$ -c $ARG3$ -p 5]]>\n </prop>\n</data>','# \'check_ping_ip\' command definition\n'),(215,'chk_mysql','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_mysql -H $HOSTADDRESS$ -d $ARG1$ -u $ARG2$ -p $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(216,'chk_udp_http-alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 8080]]>\n </prop>\n</data>','#====================================================================================================\n'),(217,'chk_by_ssh_process_usercmd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -u $ARG3$ -C $ARG4$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(218,'chk_nt_counter_mssql_log_used','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Databases(_Total)\\\\Percent Log Used\",\"SQLServer:Databases(_Total) Percent Log Used is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(219,'chk_nt_counter_exch_publicsq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\MSExchangeIS Public(_Total)\\\\Send Queue Size\",\"Send Queue Size is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(220,'chk_tcp_snmptrap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 162]]>\n </prop>\n</data>','#====================================================================================================\n'),(221,'chk_ifoperstatus','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ifoperstatus -k $ARG1$ -H $HOSTADDRESS$ -C $USER7$]]>\n </prop>\n</data>','#====================================================================================================\n'),(222,'chk_nw_vpf','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG2$ -c $ARG3$ -v VPF$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(223,'chk_local_procs','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_procs -w $ARG1$ -c $ARG2$ -a $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(224,'chk_mysql_engine','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_mysql -H $HOSTADDRESS$ -u $ARG1$ -p $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(225,'check_tcp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p $ARG1$]]>\n </prop>\n</data>','# \'check_tcp\' command definition\n'),(226,'chk_nt_counter_pagingfile','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER  -l \"\\\\Paging File(_Total)\\\\% Usage\",\"Paging File usage is %.2f %%\" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(227,'process-host-perfdata','notify','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/bin/printf \"%b\" \"$LASTCHECK$\\t$HOSTNAME$\\t$HOSTSTATE$\\t$HOSTATTEMPT$\\t$STATETYPE$\\t$EXECUTIONTIME$\\t$OUTPUT$\\t$PERFDATA$\" >> /usr/local/nagios/var/host-perfdata.out]]>\n </prop>\n</data>','# \'process-host-perfdata\' command definition\n'),(228,'chk_by_ssh_swap_stats','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_swap_stats -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(229,'chk_tcp_imap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 143]]>\n </prop>\n</data>','#====================================================================================================\n'),(230,'chk_nt_counter_disksplitio','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\PhysicalDisk(_Total)\\\\Split IO/Sec\",\"Split IO per Sec is %.f\" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(231,'chk_oracle_tablespace','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_oracle --tablespace $USER15$ $USER16$ $ARG1$ $ARG2$ $ARG3$ $ARG4$]]>\n </prop>\n</data>','#====================================================================================================\n'),(232,'chk_nt_counter_mssql_transactions','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Databases(_Total)\\\\Transactions/sec\",\"SQLServer:Databases(_Total) Transactions/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(233,'host-notify-by-email','notify','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/local/bin/sendEmail -s $USER3$ -f loki@itgroundwork.com -t $CONTACTEMAIL$ -u]]>\n </prop>\n</data>','# \'host-notify-by-email\' command definition\n'),(234,'chk_tcp_smtp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 25]]>\n </prop>\n</data>','#====================================================================================================\n'),(235,'check-host-alive','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_icmp -H $HOSTADDRESS$ -w 3000.0,80% -c 5000.0,100% -p 1]]>\n </prop>\n</data>','# \'check-host-alive\' command definition\n'),(236,'chk_by_ssh_swap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_swap -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(237,'chk_nrpe_mssql_lock_wait_time','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"\\\\SQLServer:Locks(_Total)\\\\Lock Wait Time (ms)\" \"SQLServer:Locks(_Total) Lock Wait Time (ms) is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(238,'chk_tcp_pop3','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 110]]>\n </prop>\n</data>','#====================================================================================================\n'),(239,'chk_by_sshid_process_userargs','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -i $USER18$/.ssh/id_dsa -l $USER17$ -C \"libexec/check_procs -w $ARG1$ -c $ARG2$ -a $ARG3$ -u $ARG4$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(240,'host-notify-by-epager','notify','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[/usr/local/bin/sendEmail -s $USER3$ -f loki@itgroundwork.com -t $CONTACTPAGER$ -u \"Problem: $HOSTNAME$ State: $HOSTSTATE$\" -m \"Date: $DATETIME$\"]]>\n </prop>\n</data>','# \'host-notify-by-epager\' command definition\n'),(241,'chk_tcp_imap4ssl','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 993]]>\n </prop>\n</data>','#====================================================================================================\n'),(242,'chk_tcp_icmp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 5813]]>\n </prop>\n</data>','#====================================================================================================\n'),(243,'chk_nt_commvault','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v SERVICESTATE -l \'GxCVD($HOSTALIAS$)\',\'GxEvMgrC($HOSTALIAS$)\']]>\n </prop>\n</data>','#====================================================================================================\n'),(244,'chk_nw_tsync','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v TSYNC]]>\n </prop>\n</data>','#====================================================================================================\n'),(245,'chk_snmp_if','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -C \'$USER7$\' -o IF-MIB::ifInOctets.$ARG1$,IF-MIB::ifOutOctets.$ARG1$,IF-MIB::ifInDiscards.$ARG1$,IF-MIB::ifOutDiscards.$ARG1$,IF-MIB::ifInErrors.$ARG1$,IF-MIB::ifOutErrors.$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(246,'chk_snmp_bandwidth_alt','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_snmp -H $HOSTADDRESS$ -C $USER8$ -o IF-MIB::ifInOctets.$ARG1$,IF-MIB::ifOutOctets.$ARG1$,IF-MIB::ifSpeed.$ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(247,'chk_nt_cpuload','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v CPULOAD  -l $ARG1$]]>\n </prop>\n</data>','#====================================================================================================\n'),(248,'chk_nw_lrum','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nwstat -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -v LRUM]]>\n </prop>\n</data>','#====================================================================================================\n'),(249,'process-service-perfdata','notify','<?xml version=\"1.0\" ?>\n<data>\n  <prop name=\"command_line\"><![CDATA[$USER2$/process_service_perf.pl \"$LASTCHECK$\" \"$HOSTNAME$\" \"$SERVICEDESC$\" \"$OUTPUT$\" \"$PERFDATA$\"]]>\n  </prop>\n </data>','# \'process-service-perfdata\' command definition\n'),(250,'chk_tcp_snmp','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 161]]>\n </prop>\n</data>','#====================================================================================================\n'),(251,'chk_nt_counter_mssql_lock_wait_time','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\SQLServer:Locks(_Total)\\\\Lock Wait Time (ms)\",\"SQLServer:Locks(_Total) Lock Wait Time (ms) is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(252,'chk_ldap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ldap -H $HOSTADDRESS$ -t $USER3$ -w $ARG1$ -c $ARG2$ -b $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(253,'check_local_procs','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_procs -w $ARG1$ -c $ARG2$ -s $ARG3$]]>\n </prop>\n</data>','# \'check_local_procs\' command definition\n'),(254,'chk_by_ssh_mailq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_by_ssh -H $HOSTADDRESS$ -t $USER3$ -l $USER17$ -C \"libexec/check_mailq -w $ARG1$ -c $ARG2$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(255,'chk_ssh','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ssh -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(256,'check_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_load -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(257,'check_hpjd','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_hpjd -H $HOSTADDRESS$ -C public]]>\n </prop>\n</data>','# \'check_hpjd\' command definition\n'),(258,'chk_nrpe_exch_public_receiveq','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $ARG1$ -c check_counter_instance -a \"MSExchangeIS Public\" \"Receive Queue Size\" \"_Total\" \"Receive Queue Size is %.f \" $HOSTADDRESS$ $ARG2$ $ARG3$]]>\n </prop>\n</data>','#====================================================================================================\n'),(259,'chk_remote_load','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_load_remote -e ssh -H $HOSTADDRESS$ -l $USER17$ -i $USER18$/.ssh/id_dsa -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(260,'chk_nrpe_local_disk','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nrpe -H $HOSTADDRESS$ -c check_disk -a \"$ARG1$\" \"$ARG2$\" \"$ARG3$\"]]>\n </prop>\n</data>','#====================================================================================================\n'),(261,'chk_ifstatus','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_ifstatus -C $USER7$ -H $HOSTADDRESS$]]>\n </prop>\n</data>','#====================================================================================================\n'),(262,'chk_local_swap','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_swap -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(263,'chk_tcp_dns','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_tcp -H $HOSTADDRESS$ -p 53]]>\n </prop>\n</data>','#====================================================================================================\n'),(264,'chk_nt_counter_network_interface','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_nt -H $HOSTADDRESS$ -p $USER19$ -s $USER4$ -v COUNTER -l \"\\\\Network Interface(MS TCP Loopback interface)\\\\Bytes Total/sec\",\"Network Interface(MS TCP Loopback interface) Bytes Total/sec is %.f \" -w $ARG1$ -c $ARG2$]]>\n </prop>\n</data>','#====================================================================================================\n'),(265,'chk_udp_imap4ssl','check','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[$USER1$/check_udp -H $HOSTADDRESS$ -p 993]]>\n </prop>\n</data>','#====================================================================================================\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `commands` ENABLE KEYS */;

--
-- Table structure for table `contact_command`
--

DROP TABLE IF EXISTS `contact_command`;
CREATE TABLE `contact_command` (
  `contacttemplate_id` smallint(4) unsigned NOT NULL default '0',
  `type` varchar(50) NOT NULL default '',
  `command_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`contacttemplate_id`,`type`,`command_id`),
  KEY `command_id` (`command_id`),
  CONSTRAINT `contact_command_ibfk_1` FOREIGN KEY (`command_id`) REFERENCES `commands` (`command_id`) ON DELETE CASCADE,
  CONSTRAINT `contact_command_ibfk_2` FOREIGN KEY (`contacttemplate_id`) REFERENCES `contact_templates` (`contacttemplate_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contact_command`
--


/*!40000 ALTER TABLE `contact_command` DISABLE KEYS */;
LOCK TABLES `contact_command` WRITE;
INSERT INTO `contact_command` VALUES (1,'service',26),(2,'service',26),(1,'service',151),(2,'service',151),(1,'host',233),(2,'host',233),(1,'host',240),(2,'host',240);
UNLOCK TABLES;
/*!40000 ALTER TABLE `contact_command` ENABLE KEYS */;

--
-- Table structure for table `contact_templates`
--

DROP TABLE IF EXISTS `contact_templates`;
CREATE TABLE `contact_templates` (
  `contacttemplate_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `host_notification_period` smallint(4) unsigned default NULL,
  `service_notification_period` smallint(4) unsigned default NULL,
  `data` text,
  `comment` text,
  PRIMARY KEY  (`contacttemplate_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contact_templates`
--


/*!40000 ALTER TABLE `contact_templates` DISABLE KEYS */;
LOCK TABLES `contact_templates` WRITE;
INSERT INTO `contact_templates` VALUES (1,'generic-contact-1',3,3,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"host_notification_options\"><![CDATA[d,u,r]]>\n </prop>\n <prop name=\"service_notification_options\"><![CDATA[w,u,c,r]]>\n </prop>\n</data>',NULL),(2,'generic-contact-2',3,3,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"host_notification_options\"><![CDATA[d,u,r]]>\n </prop>\n <prop name=\"service_notification_options\"><![CDATA[w,u,c,r]]>\n </prop>\n</data>',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `contact_templates` ENABLE KEYS */;

--
-- Table structure for table `contactgroup_assign`
--

DROP TABLE IF EXISTS `contactgroup_assign`;
CREATE TABLE `contactgroup_assign` (
  `contactgroup_id` smallint(4) unsigned NOT NULL default '0',
  `type` varchar(50) NOT NULL default '',
  `object` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`contactgroup_id`,`type`,`object`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contactgroup_assign`
--


/*!40000 ALTER TABLE `contactgroup_assign` DISABLE KEYS */;
LOCK TABLES `contactgroup_assign` WRITE;
INSERT INTO `contactgroup_assign` VALUES (1,'hostgroups',1),(2,'hostgroups',2),(2,'hostgroups',3),(2,'service_templates',1),(2,'service_templates',3),(2,'service_templates',4),(2,'service_templates',5),(2,'service_templates',6),(2,'service_templates',7),(2,'service_templates',8),(2,'service_templates',9),(2,'service_templates',10),(2,'service_templates',11),(2,'service_templates',12),(2,'service_templates',13),(2,'service_templates',14),(2,'service_templates',15),(2,'service_templates',16),(2,'service_templates',17),(2,'service_templates',18),(2,'service_templates',19),(2,'service_templates',20),(2,'service_templates',21),(2,'service_templates',22),(2,'service_templates',23),(2,'service_templates',24),(2,'service_templates',25),(2,'service_templates',26),(2,'service_templates',27),(2,'service_templates',28),(2,'service_templates',29),(2,'service_templates',30),(2,'service_templates',31),(2,'service_templates',32),(2,'service_templates',33),(2,'service_templates',34),(2,'service_templates',35),(2,'service_templates',36),(2,'service_templates',37),(2,'service_templates',38),(2,'service_templates',39),(2,'service_templates',40),(2,'service_templates',41),(2,'service_templates',42),(2,'service_templates',43),(2,'service_templates',44),(2,'service_templates',45),(2,'service_templates',46),(2,'service_templates',47),(2,'service_templates',48),(2,'service_templates',49),(2,'service_templates',50),(2,'service_templates',51),(2,'service_templates',52),(2,'service_templates',53),(2,'service_templates',54),(2,'service_templates',55),(2,'service_templates',56),(2,'service_templates',57),(2,'service_templates',58),(2,'service_templates',59),(2,'service_templates',60),(2,'service_templates',61),(2,'service_templates',62),(2,'service_templates',63),(2,'service_templates',64),(2,'service_templates',65),(2,'service_templates',66),(2,'service_templates',67),(2,'service_templates',68),(2,'service_templates',69),(2,'service_templates',70),(2,'service_templates',71),(2,'service_templates',72),(2,'service_templates',73),(2,'service_templates',74),(2,'service_templates',75),(2,'service_templates',76),(2,'service_templates',77),(2,'service_templates',78),(2,'service_templates',79),(2,'service_templates',80),(2,'service_templates',81),(2,'service_templates',82),(2,'service_templates',83),(2,'service_templates',84),(2,'service_templates',85),(2,'service_templates',86),(2,'service_templates',87),(2,'service_templates',88),(2,'service_templates',89),(2,'service_templates',90),(2,'service_templates',91),(2,'service_templates',92),(2,'service_templates',93),(2,'service_templates',94),(2,'service_templates',95),(2,'service_templates',96),(2,'service_templates',97),(2,'service_templates',98),(2,'service_templates',99),(2,'service_templates',100),(2,'service_templates',101),(2,'service_templates',102),(2,'service_templates',103),(2,'service_templates',104),(2,'service_templates',105),(2,'service_templates',106),(2,'service_templates',107),(2,'service_templates',108),(2,'service_templates',109),(2,'service_templates',110),(2,'service_templates',111),(2,'service_templates',112),(2,'service_templates',113),(2,'service_templates',114),(2,'service_templates',115),(2,'service_templates',116),(2,'service_templates',117),(2,'service_templates',118),(2,'service_templates',119),(2,'service_templates',120),(2,'service_templates',121),(2,'service_templates',122),(2,'service_templates',124),(2,'service_templates',125),(2,'service_templates',126),(2,'service_templates',127),(2,'service_templates',128),(2,'service_templates',129),(2,'service_templates',130),(2,'service_templates',131),(2,'service_templates',132),(2,'service_templates',133),(2,'service_templates',134),(2,'service_templates',135),(2,'service_templates',136),(2,'service_templates',137),(2,'service_templates',138),(2,'service_templates',139),(2,'service_templates',140),(2,'service_templates',141),(2,'service_templates',142),(2,'service_templates',143),(2,'service_templates',144),(2,'service_templates',145),(2,'service_templates',146),(2,'service_templates',147),(2,'service_templates',148),(2,'service_templates',149),(2,'service_templates',150),(2,'service_templates',151),(2,'service_templates',152),(2,'service_templates',153),(2,'service_templates',154),(2,'service_templates',155),(2,'service_templates',156),(2,'service_templates',157),(2,'service_templates',158),(2,'service_templates',159),(2,'service_templates',160),(2,'service_templates',161),(2,'service_templates',162),(2,'service_templates',163),(2,'service_templates',164),(2,'service_templates',165),(2,'service_templates',166),(2,'service_templates',167),(2,'service_templates',168),(2,'service_templates',169),(2,'service_templates',170),(2,'service_templates',171),(2,'service_templates',172),(2,'service_templates',173),(2,'service_templates',174),(2,'service_templates',175),(2,'service_templates',176),(2,'service_templates',177),(2,'service_templates',178),(2,'service_templates',179),(2,'service_templates',180),(2,'service_templates',181),(2,'service_templates',182),(2,'service_templates',183),(2,'service_templates',184),(2,'service_templates',185),(2,'service_templates',186),(2,'service_templates',187),(2,'service_templates',188),(2,'service_templates',189),(2,'service_templates',190),(2,'service_templates',191),(2,'service_templates',192),(2,'service_templates',193),(2,'service_templates',194),(2,'service_templates',195),(2,'service_templates',196),(2,'service_templates',197),(2,'service_templates',198),(2,'service_templates',199),(2,'service_templates',200),(2,'service_templates',201),(2,'service_templates',202),(2,'service_templates',203),(2,'service_templates',204),(2,'service_templates',205),(2,'service_templates',206),(2,'service_templates',207),(2,'service_templates',208),(2,'service_templates',209),(2,'service_templates',210),(2,'service_templates',211),(2,'service_templates',212),(2,'service_templates',213);
UNLOCK TABLES;
/*!40000 ALTER TABLE `contactgroup_assign` ENABLE KEYS */;

--
-- Table structure for table `contactgroup_contact`
--

DROP TABLE IF EXISTS `contactgroup_contact`;
CREATE TABLE `contactgroup_contact` (
  `contactgroup_id` smallint(4) unsigned NOT NULL default '0',
  `contact_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`contactgroup_id`,`contact_id`),
  KEY `contact_id` (`contact_id`),
  CONSTRAINT `contactgroup_contact_ibfk_1` FOREIGN KEY (`contact_id`) REFERENCES `contacts` (`contact_id`) ON DELETE CASCADE,
  CONSTRAINT `contactgroup_contact_ibfk_2` FOREIGN KEY (`contactgroup_id`) REFERENCES `contactgroups` (`contactgroup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contactgroup_contact`
--


/*!40000 ALTER TABLE `contactgroup_contact` DISABLE KEYS */;
LOCK TABLES `contactgroup_contact` WRITE;
INSERT INTO `contactgroup_contact` VALUES (1,3),(2,3);
UNLOCK TABLES;
/*!40000 ALTER TABLE `contactgroup_contact` ENABLE KEYS */;

--
-- Table structure for table `contactgroups`
--

DROP TABLE IF EXISTS `contactgroups`;
CREATE TABLE `contactgroups` (
  `contactgroup_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(100) NOT NULL default '',
  `comment` text,
  PRIMARY KEY  (`contactgroup_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contactgroups`
--


/*!40000 ALTER TABLE `contactgroups` DISABLE KEYS */;
LOCK TABLES `contactgroups` WRITE;
INSERT INTO `contactgroups` VALUES (1,'admins','Administrators','# \'admins\' contact group definition\n'),(2,'gwcg-nobodyGroup','Nobody here but us chickens',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `contactgroups` ENABLE KEYS */;

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
CREATE TABLE `contacts` (
  `contact_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(50) NOT NULL default '',
  `email` varchar(50) default NULL,
  `pager` varchar(50) default NULL,
  `contacttemplate_id` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `comment` text,
  PRIMARY KEY  (`contact_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contacts`
--


/*!40000 ALTER TABLE `contacts` DISABLE KEYS */;
LOCK TABLES `contacts` WRITE;
INSERT INTO `contacts` VALUES (3,'nagios','Nagios Admin','emailnagios-admin@localhost.localdomain','pagenagiosadmin@localhost.localdomain',1,1,'# \'nagios\' contact definition\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `contacts` ENABLE KEYS */;

--
-- Table structure for table `escalation_templates`
--

DROP TABLE IF EXISTS `escalation_templates`;
CREATE TABLE `escalation_templates` (
  `template_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `type` varchar(50) NOT NULL default '',
  `data` text,
  `comment` text,
  PRIMARY KEY  (`template_id`,`name`,`type`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `escalation_templates`
--


/*!40000 ALTER TABLE `escalation_templates` DISABLE KEYS */;
LOCK TABLES `escalation_templates` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `escalation_templates` ENABLE KEYS */;

--
-- Table structure for table `escalation_tree_template`
--

DROP TABLE IF EXISTS `escalation_tree_template`;
CREATE TABLE `escalation_tree_template` (
  `tree_id` smallint(4) unsigned NOT NULL default '0',
  `template_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`tree_id`,`template_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `escalation_tree_template`
--


/*!40000 ALTER TABLE `escalation_tree_template` DISABLE KEYS */;
LOCK TABLES `escalation_tree_template` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `escalation_tree_template` ENABLE KEYS */;

--
-- Table structure for table `escalation_trees`
--

DROP TABLE IF EXISTS `escalation_trees`;
CREATE TABLE `escalation_trees` (
  `tree_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(100) default NULL,
  `type` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`tree_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `escalation_trees`
--


/*!40000 ALTER TABLE `escalation_trees` DISABLE KEYS */;
LOCK TABLES `escalation_trees` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `escalation_trees` ENABLE KEYS */;

--
-- Table structure for table `extended_host_info_templates`
--

DROP TABLE IF EXISTS `extended_host_info_templates`;
CREATE TABLE `extended_host_info_templates` (
  `hostextinfo_id` int(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `data` text,
  `script` varchar(255) default NULL,
  `comment` text,
  PRIMARY KEY  (`hostextinfo_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `extended_host_info_templates`
--


/*!40000 ALTER TABLE `extended_host_info_templates` DISABLE KEYS */;
LOCK TABLES `extended_host_info_templates` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `extended_host_info_templates` ENABLE KEYS */;

--
-- Table structure for table `extended_info_coords`
--

DROP TABLE IF EXISTS `extended_info_coords`;
CREATE TABLE `extended_info_coords` (
  `host_id` int(4) unsigned NOT NULL default '0',
  `data` text,
  PRIMARY KEY  (`host_id`),
  CONSTRAINT `extended_info_coords_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `extended_info_coords`
--


/*!40000 ALTER TABLE `extended_info_coords` DISABLE KEYS */;
LOCK TABLES `extended_info_coords` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `extended_info_coords` ENABLE KEYS */;

--
-- Table structure for table `extended_service_info_templates`
--

DROP TABLE IF EXISTS `extended_service_info_templates`;
CREATE TABLE `extended_service_info_templates` (
  `serviceextinfo_id` int(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `data` text,
  `script` varchar(255) default NULL,
  `comment` text,
  PRIMARY KEY  (`serviceextinfo_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `extended_service_info_templates`
--


/*!40000 ALTER TABLE `extended_service_info_templates` DISABLE KEYS */;
LOCK TABLES `extended_service_info_templates` WRITE;
INSERT INTO `extended_service_info_templates` VALUES (1,'number_graph','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"notes_url\"><![CDATA[/nagios/cgi-bin/number_graph.cgi?host=$HOSTNAME$&service=$SERVICEDESC$]]>\n </prop>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[Graph]]>\n </prop>\n</data>','','# extended_service_info_templates number_graph\n'),(2,'ping','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"icon_image\"><![CDATA[ping.gif]]>\n </prop>\n</data>','',NULL),(3,'if_graph','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"notes_url\"><![CDATA[/nagios/cgi-bin/if_graph.cgi?name=$HOSTNAME$&service=$SERVICEDESC$]]>\n </prop>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[Graph]]>\n </prop>\n</data>','','# extended_service_info_templates if_graph\n'),(4,'check-load-via-ssh','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"notes_url\"><![CDATA[/nagios/cgi-bin/$SERVICENAME$.cgi?name=$HOSTNAME$]]>\n </prop>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[View load graphs]]>\n </prop>\n</data>','',NULL),(5,'lru-sitting-time','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"icon_image\"><![CDATA[cache.gif]]>\n </prop>\n</data>','',NULL),(6,'percent_graph','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"notes_url\"><![CDATA[/nagios/cgi-bin/percent_graph.cgi?host=$HOSTNAME$&service=$SERVICEDESC$]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[Graph]]>\n </prop>\n</data>','','# extended_service_info_templates percent_graph\n'),(7,'UNIX_load_graph','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[Graph]]>\n </prop>\n</data>','','# extended_service_info_templates UNIX_load_graph\n'),(8,'sei1tcp-wrappers','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"icon_image\"><![CDATA[wrappers.gif]]>\n </prop>\n</data>','',NULL),(9,'if_bandwidth_graph','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"notes_url\"><![CDATA[/nagios/cgi-bin/if_bandwidth_graph.cgi?name=$HOSTNAME$&service=$SERVICEDESC$]]>\n </prop>\n <prop name=\"icon_image\"><![CDATA[graph.png]]>\n </prop>\n <prop name=\"icon_image_alt\"><![CDATA[Graph]]>\n </prop>\n</data>','','# extended_service_info_templates if_bandwidth_graph\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `extended_service_info_templates` ENABLE KEYS */;

--
-- Table structure for table `external_host`
--

DROP TABLE IF EXISTS `external_host`;
CREATE TABLE `external_host` (
  `external_id` smallint(4) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  `data` text,
  PRIMARY KEY  (`external_id`,`host_id`),
  KEY `host_id` (`host_id`),
  CONSTRAINT `external_host_ibfk_1` FOREIGN KEY (`external_id`) REFERENCES `externals` (`external_id`) ON DELETE CASCADE,
  CONSTRAINT `external_host_ibfk_2` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `external_host`
--


/*!40000 ALTER TABLE `external_host` DISABLE KEYS */;
LOCK TABLES `external_host` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `external_host` ENABLE KEYS */;

--
-- Table structure for table `external_host_profile`
--

DROP TABLE IF EXISTS `external_host_profile`;
CREATE TABLE `external_host_profile` (
  `external_id` smallint(4) unsigned NOT NULL default '0',
  `hostprofile_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`external_id`,`hostprofile_id`),
  KEY `hostprofile_id` (`hostprofile_id`),
  CONSTRAINT `external_host_profile_ibfk_1` FOREIGN KEY (`external_id`) REFERENCES `externals` (`external_id`) ON DELETE CASCADE,
  CONSTRAINT `external_host_profile_ibfk_2` FOREIGN KEY (`hostprofile_id`) REFERENCES `profiles_host` (`hostprofile_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `external_host_profile`
--


/*!40000 ALTER TABLE `external_host_profile` DISABLE KEYS */;
LOCK TABLES `external_host_profile` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `external_host_profile` ENABLE KEYS */;

--
-- Table structure for table `external_service`
--

DROP TABLE IF EXISTS `external_service`;
CREATE TABLE `external_service` (
  `external_id` smallint(4) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  `service_id` int(8) unsigned NOT NULL default '0',
  `data` text,
  PRIMARY KEY  (`external_id`,`host_id`,`service_id`),
  KEY `host_id` (`host_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `external_service_ibfk_1` FOREIGN KEY (`external_id`) REFERENCES `externals` (`external_id`) ON DELETE CASCADE,
  CONSTRAINT `external_service_ibfk_2` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE,
  CONSTRAINT `external_service_ibfk_3` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `external_service`
--


/*!40000 ALTER TABLE `external_service` DISABLE KEYS */;
LOCK TABLES `external_service` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `external_service` ENABLE KEYS */;

--
-- Table structure for table `external_service_names`
--

DROP TABLE IF EXISTS `external_service_names`;
CREATE TABLE `external_service_names` (
  `external_id` smallint(4) unsigned NOT NULL default '0',
  `servicename_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`external_id`,`servicename_id`),
  KEY `servicename_id` (`servicename_id`),
  CONSTRAINT `external_service_names_ibfk_1` FOREIGN KEY (`external_id`) REFERENCES `externals` (`external_id`) ON DELETE CASCADE,
  CONSTRAINT `external_service_names_ibfk_2` FOREIGN KEY (`servicename_id`) REFERENCES `service_names` (`servicename_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `external_service_names`
--


/*!40000 ALTER TABLE `external_service_names` DISABLE KEYS */;
LOCK TABLES `external_service_names` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `external_service_names` ENABLE KEYS */;

--
-- Table structure for table `externals`
--

DROP TABLE IF EXISTS `externals`;
CREATE TABLE `externals` (
  `external_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) default NULL,
  `description` varchar(50) default NULL,
  `type` varchar(20) NOT NULL default '',
  `display` text,
  `handler` text,
  PRIMARY KEY  (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `externals`
--


/*!40000 ALTER TABLE `externals` DISABLE KEYS */;
LOCK TABLES `externals` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `externals` ENABLE KEYS */;

--
-- Table structure for table `file_host`
--

DROP TABLE IF EXISTS `file_host`;
CREATE TABLE `file_host` (
  `file_id` smallint(4) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  PRIMARY KEY  (`file_id`,`host_id`),
  KEY `host_id` (`host_id`),
  CONSTRAINT `file_host_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_host`
--


/*!40000 ALTER TABLE `file_host` DISABLE KEYS */;
LOCK TABLES `file_host` WRITE;
INSERT INTO `file_host` VALUES (12,11),(12,12),(12,14),(12,16);
UNLOCK TABLES;
/*!40000 ALTER TABLE `file_host` ENABLE KEYS */;

--
-- Table structure for table `file_service`
--

DROP TABLE IF EXISTS `file_service`;
CREATE TABLE `file_service` (
  `file_id` smallint(4) unsigned NOT NULL default '0',
  `service_id` int(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`file_id`,`service_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `file_service_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_service`
--


/*!40000 ALTER TABLE `file_service` DISABLE KEYS */;
LOCK TABLES `file_service` WRITE;
INSERT INTO `file_service` VALUES (17,1),(17,2),(17,3),(18,38),(18,39),(18,40),(18,41),(18,42),(18,43),(18,48),(18,49),(18,50),(18,51),(18,52),(18,53),(18,54),(18,55),(18,56),(18,57),(18,58);
UNLOCK TABLES;
/*!40000 ALTER TABLE `file_service` ENABLE KEYS */;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
CREATE TABLE `files` (
  `file_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `path` varchar(255) NOT NULL default '',
  `type` varchar(50) default NULL,
  PRIMARY KEY  (`file_id`,`name`,`path`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `files`
--


/*!40000 ALTER TABLE `files` DISABLE KEYS */;
LOCK TABLES `files` WRITE;
INSERT INTO `files` VALUES (1,'checkcommands.cfg','/etc/nagios','command'),(2,'contact_templates.cfg','/etc/nagios','contact_template'),(3,'contactgroups.cfg','/etc/nagios','contactgroup'),(4,'contacts.cfg','/etc/nagios','contact'),(5,'escalations.cfg','/etc/nagios',NULL),(6,'extended_service_info.cfg','/etc/nagios','serviceextinfo'),(7,'extended_service_info_templates.cfg','/etc/nagios','serviceextinfo_template'),(8,'groundwork-switches-hosts-services.cfg','/etc/nagios','service'),(9,'groundwork-switches-hosts.cfg','/etc/nagios','host'),(10,'host_templates.cfg','/etc/nagios','host_template'),(11,'hostgroups.cfg','/etc/nagios','hostgroup-host'),(12,'hosts.cfg','/etc/nagios','host'),(13,'misccommands.cfg','/etc/nagios','command'),(14,'service_dependency.cfg','/etc/nagios','servicedependency'),(15,'service_dependency_templates.cfg','/etc/nagios','servicedependency_template'),(16,'service_templates.cfg','/etc/nagios','service_template'),(17,'services.cfg','/etc/nagios','service'),(18,'standard-service-profiles.cfg','/etc/nagios','service'),(19,'timeperiods.cfg','/etc/nagios','timeperiod'),(20,'extended_host_info.cfg','/etc/nagios','extended_host_info'),(21,'servicegroups.cfg','/etc/nagios','servicegroups'),(22,'escalation_templates.cfg','/etc/nagios','escalation_templates'),(23,'extended_host_info_templates.cfg','/etc/nagios','extended_host_info_templates'),(24,'host_dependencies.cfg','/etc/nagios','host_dependencies');
UNLOCK TABLES;
/*!40000 ALTER TABLE `files` ENABLE KEYS */;

--
-- Table structure for table `host_dependencies`
--

DROP TABLE IF EXISTS `host_dependencies`;
CREATE TABLE `host_dependencies` (
  `host_id` int(6) unsigned NOT NULL default '0',
  `parent_id` int(6) unsigned NOT NULL default '0',
  `data` text,
  `comment` text,
  PRIMARY KEY  (`host_id`,`parent_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `host_dependencies_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE,
  CONSTRAINT `host_dependencies_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `host_dependencies`
--


/*!40000 ALTER TABLE `host_dependencies` DISABLE KEYS */;
LOCK TABLES `host_dependencies` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `host_dependencies` ENABLE KEYS */;

--
-- Table structure for table `host_overrides`
--

DROP TABLE IF EXISTS `host_overrides`;
CREATE TABLE `host_overrides` (
  `host_id` int(6) unsigned NOT NULL default '0',
  `notification_period` smallint(4) unsigned default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `event_handler` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `data` text,
  PRIMARY KEY  (`host_id`),
  CONSTRAINT `host_overrides_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `host_overrides`
--


/*!40000 ALTER TABLE `host_overrides` DISABLE KEYS */;
LOCK TABLES `host_overrides` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `host_overrides` ENABLE KEYS */;

--
-- Table structure for table `host_parent`
--

DROP TABLE IF EXISTS `host_parent`;
CREATE TABLE `host_parent` (
  `host_id` int(6) unsigned NOT NULL default '0',
  `parent_id` int(6) unsigned NOT NULL default '0',
  PRIMARY KEY  (`host_id`,`parent_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `host_parent_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE,
  CONSTRAINT `host_parent_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `host_parent`
--


/*!40000 ALTER TABLE `host_parent` DISABLE KEYS */;
LOCK TABLES `host_parent` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `host_parent` ENABLE KEYS */;

--
-- Table structure for table `host_templates`
--

DROP TABLE IF EXISTS `host_templates`;
CREATE TABLE `host_templates` (
  `hosttemplate_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `notification_period` smallint(4) unsigned default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `event_handler` smallint(4) unsigned default NULL,
  `data` text,
  `comment` text,
  PRIMARY KEY  (`hosttemplate_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `host_templates`
--


/*!40000 ALTER TABLE `host_templates` DISABLE KEYS */;
LOCK TABLES `host_templates` WRITE;
INSERT INTO `host_templates` VALUES (1,'generic-host',NULL,NULL,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"flap_detection_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"retain_status_information\"><![CDATA[1]]>\n </prop>\n <prop name=\"process_perf_data\"><![CDATA[1]]>\n </prop>\n <prop name=\"notifications_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"retain_nonstatus_information\"><![CDATA[1]]>\n </prop>\n</data>','# Generic host definition template\n'),(2,'default-host',3,235,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"max_check_attempts\"><![CDATA[10]]>\n </prop>\n <prop name=\"flap_detection_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"retain_status_information\"><![CDATA[1]]>\n </prop>\n <prop name=\"process_perf_data\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[d,u,r]]>\n </prop>\n <prop name=\"notifications_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n <prop name=\"retain_nonstatus_information\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_interval\"><![CDATA[480]]>\n </prop>\n</data>',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `host_templates` ENABLE KEYS */;

--
-- Table structure for table `hostgroup_host`
--

DROP TABLE IF EXISTS `hostgroup_host`;
CREATE TABLE `hostgroup_host` (
  `hostgroup_id` smallint(4) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  PRIMARY KEY  (`hostgroup_id`,`host_id`),
  KEY `host_id` (`host_id`),
  CONSTRAINT `hostgroup_host_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE,
  CONSTRAINT `hostgroup_host_ibfk_2` FOREIGN KEY (`hostgroup_id`) REFERENCES `hostgroups` (`hostgroup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `hostgroup_host`
--


/*!40000 ALTER TABLE `hostgroup_host` DISABLE KEYS */;
LOCK TABLES `hostgroup_host` WRITE;
INSERT INTO `hostgroup_host` VALUES (2,11),(2,12),(2,14),(2,16);
UNLOCK TABLES;
/*!40000 ALTER TABLE `hostgroup_host` ENABLE KEYS */;

--
-- Table structure for table `hostgroups`
--

DROP TABLE IF EXISTS `hostgroups`;
CREATE TABLE `hostgroups` (
  `hostgroup_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(50) NOT NULL default '',
  `hostgroup_escalation_id` smallint(4) unsigned default NULL,
  `host_escalation_id` smallint(4) unsigned default NULL,
  `service_escalation_id` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `comment` text,
  PRIMARY KEY  (`hostgroup_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `hostgroups`
--


/*!40000 ALTER TABLE `hostgroups` DISABLE KEYS */;
LOCK TABLES `hostgroups` WRITE;
INSERT INTO `hostgroups` VALUES (2,'Monitoring_Servers','Monitoring Servers',NULL,NULL,NULL,1,'# \'monitoring servers\' host group definition\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `hostgroups` ENABLE KEYS */;

--
-- Table structure for table `hosts`
--

DROP TABLE IF EXISTS `hosts`;
CREATE TABLE `hosts` (
  `host_id` int(6) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(50) NOT NULL default '',
  `address` varchar(50) NOT NULL default '',
  `os` varchar(50) default NULL,
  `hosttemplate_id` smallint(4) unsigned default NULL,
  `hostextinfo_id` smallint(4) unsigned default NULL,
  `hostprofile_id` smallint(4) unsigned default NULL,
  `serviceprofile_id` smallint(4) unsigned default NULL,
  `host_escalation_id` smallint(4) unsigned default NULL,
  `service_escalation_id` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `comment` text,
  PRIMARY KEY  (`host_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `hosts`
--


/*!40000 ALTER TABLE `hosts` DISABLE KEYS */;
LOCK TABLES `hosts` WRITE;
INSERT INTO `hosts` VALUES (11,'localhost','localhost','127.0.0.1','n/a',2,NULL,0,0,0,0,1,'# \'localhost\' host definition\n'),(12,'Windows_Server','Windows_Server','192.168.2.52',NULL,2,NULL,NULL,16,NULL,NULL,1,NULL),(14,'Network_Router','Network Router','192.168.2.201',NULL,2,NULL,NULL,4,NULL,NULL,1,NULL),(16,'UNIX_Server','UNIX Server','192.168.2.60',NULL,2,NULL,NULL,3,NULL,NULL,1,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `hosts` ENABLE KEYS */;

--
-- Table structure for table `import_schemas`
--

DROP TABLE IF EXISTS `import_schemas`;
CREATE TABLE `import_schemas` (
  `name` varchar(50) NOT NULL default '',
  `field_separator` varchar(50) default NULL,
  `host` char(1) default NULL,
  `alias` char(1) default NULL,
  `address` char(1) default NULL,
  `os` char(1) default NULL,
  `service` char(1) default NULL,
  `info` char(1) default NULL,
  PRIMARY KEY  (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `import_schemas`
--


/*!40000 ALTER TABLE `import_schemas` DISABLE KEYS */;
LOCK TABLES `import_schemas` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `import_schemas` ENABLE KEYS */;

--
-- Table structure for table `match_strings`
--

DROP TABLE IF EXISTS `match_strings`;
CREATE TABLE `match_strings` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `string` varchar(50) default NULL,
  `type` varchar(20) default NULL,
  `class` varchar(50) default NULL,
  `object` varchar(50) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `match_strings`
--


/*!40000 ALTER TABLE `match_strings` DISABLE KEYS */;
LOCK TABLES `match_strings` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `match_strings` ENABLE KEYS */;

--
-- Table structure for table `profile_hostgroup`
--

DROP TABLE IF EXISTS `profile_hostgroup`;
CREATE TABLE `profile_hostgroup` (
  `hostprofile_id` smallint(4) unsigned NOT NULL default '0',
  `hostgroup_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`hostprofile_id`,`hostgroup_id`),
  KEY `hostgroup_id` (`hostgroup_id`),
  CONSTRAINT `profile_hostgroup_ibfk_1` FOREIGN KEY (`hostgroup_id`) REFERENCES `hostgroups` (`hostgroup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `profile_hostgroup`
--


/*!40000 ALTER TABLE `profile_hostgroup` DISABLE KEYS */;
LOCK TABLES `profile_hostgroup` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `profile_hostgroup` ENABLE KEYS */;

--
-- Table structure for table `profile_parent`
--

DROP TABLE IF EXISTS `profile_parent`;
CREATE TABLE `profile_parent` (
  `hostprofile_id` smallint(4) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  PRIMARY KEY  (`hostprofile_id`,`host_id`),
  KEY `host_id` (`host_id`),
  CONSTRAINT `profile_parent_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `profile_parent`
--


/*!40000 ALTER TABLE `profile_parent` DISABLE KEYS */;
LOCK TABLES `profile_parent` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `profile_parent` ENABLE KEYS */;

--
-- Table structure for table `profiles_host`
--

DROP TABLE IF EXISTS `profiles_host`;
CREATE TABLE `profiles_host` (
  `hostprofile_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(255) default NULL,
  `host_template_id` smallint(4) unsigned default NULL,
  `host_extinfo_id` smallint(4) unsigned default NULL,
  `host_escalation_id` smallint(4) unsigned default NULL,
  `service_escalation_id` smallint(4) unsigned default NULL,
  `serviceprofile_id` smallint(4) unsigned default NULL,
  `file_id` smallint(4) unsigned default NULL,
  PRIMARY KEY  (`hostprofile_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `profiles_host`
--


/*!40000 ALTER TABLE `profiles_host` DISABLE KEYS */;
LOCK TABLES `profiles_host` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `profiles_host` ENABLE KEYS */;

--
-- Table structure for table `profiles_service`
--

DROP TABLE IF EXISTS `profiles_service`;
CREATE TABLE `profiles_service` (
  `serviceprofile_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(100) default NULL,
  `file_id` smallint(4) unsigned default NULL,
  PRIMARY KEY  (`serviceprofile_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `profiles_service`
--


/*!40000 ALTER TABLE `profiles_service` DISABLE KEYS */;
LOCK TABLES `profiles_service` WRITE;
INSERT INTO `profiles_service` VALUES (3,'UNIX_server_ssh','UNIX server generic profile',18),(4,'NETWORK_snmp','Generic network device',18),(16,'WINDOWS_nrpe','Generic Windows server',18);
UNLOCK TABLES;
/*!40000 ALTER TABLE `profiles_service` ENABLE KEYS */;

--
-- Table structure for table `service_dependency`
--

DROP TABLE IF EXISTS `service_dependency`;
CREATE TABLE `service_dependency` (
  `id` int(8) unsigned NOT NULL auto_increment,
  `service_id` int(8) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  `depend_on_host_id` int(6) unsigned NOT NULL default '0',
  `template` smallint(4) unsigned NOT NULL default '0',
  `comment` text,
  PRIMARY KEY  (`id`),
  KEY `service_id` (`service_id`),
  KEY `depend_on_host_id` (`depend_on_host_id`),
  CONSTRAINT `service_dependency_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE,
  CONSTRAINT `service_dependency_ibfk_2` FOREIGN KEY (`depend_on_host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service_dependency`
--


/*!40000 ALTER TABLE `service_dependency` DISABLE KEYS */;
LOCK TABLES `service_dependency` WRITE;
INSERT INTO `service_dependency` VALUES (3,42,12,12,3,''),(7,48,14,14,3,''),(8,50,14,14,2,''),(9,52,14,14,2,'');
UNLOCK TABLES;
/*!40000 ALTER TABLE `service_dependency` ENABLE KEYS */;

--
-- Table structure for table `service_dependency_templates`
--

DROP TABLE IF EXISTS `service_dependency_templates`;
CREATE TABLE `service_dependency_templates` (
  `id` int(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `servicename_id` smallint(4) unsigned NOT NULL default '0',
  `data` text,
  `comment` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service_dependency_templates`
--


/*!40000 ALTER TABLE `service_dependency_templates` DISABLE KEYS */;
LOCK TABLES `service_dependency_templates` WRITE;
INSERT INTO `service_dependency_templates` VALUES (1,'gwsd-nrpe_tcp_port',100,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"execution_failure_criteria\"><![CDATA[u]]>\n </prop>\n <prop name=\"notification_failure_criteria\"><![CDATA[u]]>\n </prop>\n</data>','# service_dependency_templates gwsd-nrpe_tcp_port\n'),(2,'gwsd-snmp_alive',92,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"execution_failure_criteria\"><![CDATA[u]]>\n </prop>\n <prop name=\"notification_failure_criteria\"><![CDATA[u]]>\n </prop>\n</data>','# service_dependency_templates gwsd-snmp_alive\n'),(3,'gwsd-host_alive',6,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"execution_failure_criteria\"><![CDATA[u]]>\n </prop>\n <prop name=\"notification_failure_criteria\"><![CDATA[u]]>\n </prop>\n</data>','# service_dependency_templates gwsd-host_alive\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `service_dependency_templates` ENABLE KEYS */;

--
-- Table structure for table `service_names`
--

DROP TABLE IF EXISTS `service_names`;
CREATE TABLE `service_names` (
  `servicename_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(100) NOT NULL default '',
  `description` varchar(100) default NULL,
  `template` smallint(4) unsigned default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `command_line` varchar(255) default NULL,
  `dependency` smallint(4) unsigned default NULL,
  `escalation` smallint(4) unsigned default NULL,
  `extinfo` smallint(4) unsigned default NULL,
  PRIMARY KEY  (`servicename_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service_names`
--


/*!40000 ALTER TABLE `service_names` DISABLE KEYS */;
LOCK TABLES `service_names` WRITE;
INSERT INTO `service_names` VALUES (1,'DHCP','DHCP',1,188,'chk_dhcp!192.168.2.20',0,0,NULL),(2,'DNS','DNS_google',1,152,'chk_dns!www.google.com',0,0,NULL),(3,'DSL Border Ping','DSL Border Ping',1,203,'chk_ping_ip!69.107.174.246!200,25%!500,50%',0,0,NULL),(4,'Dummy_Service','gwsn-dummy_service',1,NULL,NULL,0,0,NULL),(5,'HTTP_Alive','gwsn-http_basic',7,204,'',0,0,NULL),(6,'Host_Alive','gwsn-alive',6,NULL,NULL,0,0,NULL),(7,'Local_Disk','gwsn-local_disk',1,112,'check_local_disk!10!5!/',0,0,NULL),(8,'PING','PING',6,NULL,NULL,NULL,NULL,NULL),(9,'Ping_host','gwsn-ping',10,2355,'chk_ping!3000.0,80%!5000.0,100%',0,0,NULL),(10,'SNMP_if','SNMP_if',99,245,'chk_snmp_if!1',2,NULL,3),(11,'SNMP_if_bandwidth','SNMP_if_bandwidth',66,5,'chk_snmp_bandwidth!1',2,0,9),(12,'SNMP_ifoperstatus','SNMP_ifoperstatus',20,221,'chk_ifoperstatus!1',2,0,NULL),(13,'T1 Border Ping','T1 Border Ping',1,214,'check_ping_ip!216.31.246.97!200,25%!500,50%',0,0,NULL),(14,'Tcp_Ssh','gwsn-tcp_ssh',16,16,'',0,0,NULL),(15,'UNIX_disk_ssh','Check UNIX server disk using check_by_ssh',45,14,'chk_by_ssh_disks!20%!10%',0,0,6),(16,'UNIX_load_ssh','Check UNIX server load using check_by_ssh',26,101,'chk_by_ssh_load!10!20',NULL,NULL,1),(17,'UNIX_memory_ssh','Check UNIX server memory using check_by_ssh',100,147,'chk_by_ssh_mem!80%!95%',0,0,6),(18,'UNIX_procs_cron_ssh','Check UNIX server cron process using check_by_ssh',1,20,'chk_by_sshid_process_cmd!1:!1:!cron',0,0,NULL),(19,'UNIX_swap_ssh','Check UNIX server disk using check_by_ssh',48,236,'chk_by_ssh_swap!20%!10%',0,0,6),(20,'Local_Procs','local_procs',1,253,'check_local_procs!150!200!RSZDT',0,0,NULL),(21,'Local_Users','local_users',1,39,'check_local_users!75!150',0,0,NULL),(22,'snmp_trap','snmp_trap',1,11,'',0,0,NULL),(23,'HTTP','HTTP',113,NULL,'check_http!200,25%!500,50%',0,NULL,NULL),(24,'IMAP','IMAP',2,NULL,'',0,NULL,NULL),(25,'POP','POP',2,NULL,'',0,NULL,NULL),(26,'SMTP','SMTP',2,NULL,'',0,NULL,NULL),(27,'NRPE_cpu','gwsn-nrpe_cpu',81,2521,'chk_nrpe_cpu!Arg1!Arg2!Arg3',0,NULL,NULL),(28,'NRPE_Exchange_MailRQ','gwsn-nrpe_exch_mailbox_receiveq',195,2487,'chk_nrpe_exch_mailbox_receiveq!Arg1!Arg2!Arg3',0,NULL,NULL),(29,'NRPE_Exchange_MailSQ','gwsn-nrpe_exch_mailbox_sendq',105,2375,'chk_nrpe_exch_mailbox_sendq!Arg1!Arg2!Arg3',0,NULL,NULL),(30,'NRPE_Exchange_MTAWQ','gwsn-nrpe_exch_mta_workq',139,2360,'chk_nrpe_exch_mta_workq!Arg1!Arg2!Arg3',0,NULL,NULL),(31,'NRPE_Exchange_PublicRQ','gwsn-nrpe_exch_public_receiveq',145,2604,'chk_nrpe_exch_public_receiveq!Arg1!Arg2!Arg3',0,NULL,NULL),(32,'NRPE_Exchange_PublicSQ','gwsn-nrpe_exch_public_sendq',165,2411,'chk_nrpe_exch_public_sendq!Arg1!Arg2!arg3',0,NULL,NULL),(33,'NsClient_disktransfers','gwsn-nt_counter_disktransfers',144,2412,'chk_nt_counter_disktransfers!50!100',0,NULL,NULL),(34,'NsClient_Exchange_MailRQ','gwsn-nt_counter_exch_mailrq',172,2518,'chk_nt_counter_exch_mailrq!50!100',0,NULL,NULL),(35,'NsClient_Exchange_MailSQ','gwsn-nt_counter_exch_mailsq',142,2533,'chk_nt_counter_exch_mailsq!50!100',0,NULL,NULL),(36,'NsClient_Exchange_MTAWQ','gwsn-nt_counter_exch_mtawq',89,2495,'chk_nt_counter_exch_mtawq!50!100',0,NULL,NULL),(37,'NsClient_Exchange_PublicRQ','gwsn-nt_counter_exch_publicrq',82,2362,'chk_nt_counter_exch_publicrq!50!100',0,NULL,NULL),(38,'NsClient_Exchange_PublicSQ','gwsn-nt_counter_exch_publicsq',167,2566,'chk_nt_counter_exch_publicsq!50!100',0,NULL,NULL),(39,'NsClient_memory_pages','gwsn-nt_counter_memory_pages',182,2538,'chk_nt_counter_memory_pages!50!100',0,NULL,NULL),(40,'NsClient_cpuload','gwsn-nt_cpuload',185,2594,'chk_nt_cpuload!10,50,80,60,50,80,1440,50,80',0,NULL,NULL),(41,'NsClient_network_interface','gwsn-nt_counter_network_interface',174,2610,'chk_nt_counter_network_interface!50!100',0,NULL,NULL),(42,'NsClient_memuse','gwsn-nt_memuse',187,2539,'chk_nt_memuse!80!90',0,NULL,NULL),(43,'NsClient_useddiskspace-c','gwsn-nt_useddiskspace-c',98,2384,'chk_nt_useddiskspace!c!80!90',0,NULL,NULL),(44,'NsClient_useddiskspace-d','gwsn-nt_useddiskspace-d',192,2384,'chk_nt_useddiskspace!d!80!90',0,NULL,NULL),(45,'NsClient_useddiskspace-e','gwsn-nt_useddiskspace-e',179,2384,'chk_nt_useddiskspace!e!80!90',0,NULL,NULL),(46,'NsClient_useddiskspace-f','gwsn-nt_useddiskspace-f',209,2384,'chk_nt_useddiskspace!f!80!90',0,NULL,NULL),(47,'NsClient_useddiskspace-g','gwsn-nt_useddiskspace-g',118,2384,'chk_nt_useddiskspace!g!80!90',0,NULL,NULL),(48,'NsClient_useddiskspace-h','gwsn-nt_useddiskspace-h',169,2384,'chk_nt_useddiskspace!h!80!90',0,NULL,NULL),(49,'NsClient_useddiskspace-i','gwsn-nt_useddiskspace-i',178,2384,'chk_nt_useddiskspace!i!80!90',0,NULL,NULL),(50,'NsClient_useddiskspace-j','gwsn-nt_useddiskspace-j',207,2384,'chk_nt_useddiskspace!j!80!90',0,NULL,NULL),(51,'NsClient_useddiskspace-k','gwsn-nt_useddiskspace-k',106,2384,'chk_nt_useddiskspace!k!80!90',0,NULL,NULL),(52,'NsClient_useddiskspace-l','gwsn-nt_useddiskspace-l',161,2384,'chk_nt_useddiskspace!l!80!90',0,NULL,NULL),(53,'NsClient_useddiskspace-m','gwsn-nt_useddiskspace-m',188,2384,'chk_nt_useddiskspace!m!80!90',0,NULL,NULL),(54,'NsClient_useddiskspace-n','gwsn-nt_useddiskspace-n',171,2384,'chk_nt_useddiskspace!n!80!90',0,NULL,NULL),(55,'NsClient_useddiskspace-o','gwsn-nt_useddiskspace-o',194,2384,'chk_nt_useddiskspace!o!80!90',0,NULL,NULL),(56,'NsClient_useddiskspace-p','gwsn-nt_useddiskspace-p',96,2384,'chk_nt_useddiskspace!p!80!90',0,NULL,NULL),(57,'NsClient_useddiskspace-q','gwsn-nt_useddiskspace-q',91,2384,'chk_nt_useddiskspace!q!80!90',0,NULL,NULL),(58,'NsClient_useddiskspace-r','gwsn-nt_useddiskspace-r',156,2384,'chk_nt_useddiskspace!r!80!90',0,NULL,NULL),(59,'NsClient_useddiskspace-s','gwsn-nt_useddiskspace-s',181,2384,'chk_nt_useddiskspace!s!80!90',0,NULL,NULL),(60,'NsClient_useddiskspace-t','gwsn-nt_useddiskspace-t',201,2384,'chk_nt_useddiskspace!t!80!90',0,NULL,NULL),(61,'NsClient_useddiskspace-u','gwsn-nt_useddiskspace-u',191,2384,'chk_nt_useddiskspace!u!80!90',0,NULL,NULL),(62,'NsClient_useddiskspace-v','gwsn-nt_useddiskspace-v',164,2384,'chk_nt_useddiskspace!v!80!90',0,NULL,NULL),(63,'NsClient_useddiskspace-w','gwsn-nt_useddiskspace-w',120,2384,'chk_nt_useddiskspace!w!80!90',0,NULL,NULL),(64,'NsClient_useddiskspace-x','gwsn-nt_useddiskspace-x',211,2384,'chk_nt_useddiskspace!x!80!90',0,NULL,NULL),(65,'NsClient_useddiskspace-y','gwsn-nt_useddiskspace-y',203,2384,'chk_nt_useddiskspace!y!80!90',0,NULL,NULL),(66,'NsClient_useddiskspace-z','gwsn-nt_useddiskspace-z',189,2384,'chk_nt_useddiskspace!z!80!90',0,NULL,NULL),(67,'SMTP_Alive','gwsn-smtp',58,2483,'chk_smtp',0,NULL,NULL),(68,'POP3_tcp_port','gwsn-tcp_pop3',214,2585,'chk_tcp_pop3',0,NULL,NULL),(69,'NRPE_udp_port','gwsn-udp_nrpe',43,2462,'chk_udp_nrpe',0,NULL,NULL),(70,'NsClient_udp_port','gwsn-udp_nsclient',3,2377,'chk_udp_nsclient',0,NULL,NULL),(71,'NsClient_MSSql_bufcache_hits','gwsn-nt_counter_mssql_bufcache_hits',175,2348,'chk_nt_counter_mssql_bufcache_hits!50!100',0,NULL,NULL),(72,'NsClient_MSSql_deadlocks','gwsn-nt_counter_mssql_deadlocks',199,2435,'chk_nt_counter_mssql_deadlocks!50!100',0,NULL,NULL),(73,'NsClient_MSSql_latch_waits','gwsn-nt_counter_mssql_latch_waits',NULL,2469,'chk_nt_counter_mssql_latch_waits!50!100',0,NULL,NULL),(74,'NsClient_MSSql_lock_wait_time','gwsn-nt_counter_mssql_lock_wait_time',193,2598,'chk_nt_counter_mssql_lock_wait_time!50!100',0,NULL,NULL),(75,'NsClient_MSSql_lock_waits','gwsn-nt_counter_mssql_lock_waits',197,2400,'chk_nt_counter_mssql_lock_waits!50!100',0,NULL,NULL),(76,'NsClient_MSSql_log_growths','gwsn-nt_counter_mssql_log_growths',158,2556,'chk_nt_counter_mssql_log_growths!50!100',0,NULL,NULL),(77,'NsClient_MSSql_log_used','gwsn-nt_counter_mssql_log_used',116,2565,'chk_nt_counter_mssql_log_used!50!100',0,NULL,NULL),(78,'NRPE_Print_Queue','gwsn-nrpe_print_queue',131,2472,'chk_nrpe_print_queue!4!5',0,NULL,NULL),(79,'DHCP_Alive','gwsn-dhcp',138,2535,'chk_dhcp',0,NULL,NULL),(80,'NRPE_disk','gwsn-nrpe_disk',146,2431,'chk_nrpe_disk!Arg1!Arg2!Arg3!Arg4',0,NULL,NULL),(81,'NRPE_disk_transfers','gwsn-nrpe_disktransfers',173,2427,'chk_nrpe_disktransfers!Arg1!Arg2!Arg3',0,NULL,NULL),(82,'ICMP_Ping','gwsn-icmp_ping',7,2349,'chk_alive',0,NULL,NULL),(83,'Local_load','gwsn-local_load',38,2440,'chk_local_load',0,NULL,NULL),(84,'Local_NTP','gwsn-local_ntp',127,2364,'chk_ntp',0,NULL,NULL),(85,'NTP_Alive','gwsn-ntp',127,2364,'chk_ntp',0,NULL,NULL),(86,'Ssh_Disk','gwsn-by_ssh_disk',80,2410,'chk_by_ssh_disk!Arg1!Arg2!Arg3',0,NULL,NULL),(87,'Ssh_Memory','gwsn-by_ssh_mem',102,2494,'chk_by_ssh_mem!Arg1!Arg2!Arg3',0,NULL,NULL),(88,'Ssh_Process_Crond','gwsn-by_ssh_process_crond',94,2454,'chk_by_ssh_process_cmd!Arg1!Arg2!crond',0,NULL,NULL),(89,'Ssh_Process_Ncipher','gwsn-by_ssh_process_ncipher',94,2454,'chk_by_ssh_process_cmd!Arg1!Arg2!Arg3',0,NULL,NULL),(90,'URL_get_ereg','gwsn-http_ereg',60,2554,'chk_http_ereg!Arg1!Arg2!Arg3!Arg4',0,NULL,NULL),(91,'Ssh_Cpu','gwsn-remote_load',30,2606,'chk_remote_load!50!60',0,NULL,NULL),(92,'SNMP_Alive','gwsn-snmp',119,82,'chk_snmp!system.sysName.0!Switch0!Switch02.itgroundwork.com',3,NULL,NULL),(93,'Tcp_DNS','gwsn-tcp_dns',69,2609,'chk_tcp_dns',0,NULL,NULL),(94,'Tcp_Http','gwsn-tcp_http',130,2371,'chk_tcp_http',0,NULL,NULL),(95,'Tcp_Https','gwsn-tcp_https',11,2392,'chk_tcp_https',0,NULL,NULL),(96,'Net_IF_OperStatus','gwsn-ifoperstatus',21,2568,'chk_ifoperstatus!Arg1',0,NULL,NULL),(97,'NsClient_MSSql_memory_grants_pending','gwsn-nt_counter_mssql_memory_grants_pending',159,2353,'chk_nt_counter_mssql_memory_grants_pending!50!100',0,NULL,NULL),(98,'NsClient_MSSql_transactions','gwsn-nt_counter_mssql_transactions',186,2579,'chk_nt_counter_mssql_transactions!50!100',0,NULL,NULL),(99,'Ssh_Process_Cmd','gwsn-by_ssh_process_cmd',94,2454,'chk_by_ssh_process_cmd!Arg1!Arg2!Arg3',0,NULL,NULL),(100,'NRPE_tcp_port','gwsn-tcp_nrpe',28,155,'',0,NULL,NULL),(101,'NRPE_local_cpu','gwsn-nrpe_locaL_cpu',204,202,'chk_nrpe_local_cpu!50!80',3,NULL,6),(102,'NRPE_local_memory','gwsn-nrpe_locaL_memory',195,51,'chk_nrpe_local_memory!500!300',NULL,NULL,1),(103,'NRPE_local_pagefile','gwsn-nrpe_locaL_pagefile',169,123,'chk_nrpe_local_pagefile!40!50',NULL,NULL,6),(104,'NRPE_local_disk','gwsn-nrpe_locaL_disk',162,260,'chk_nrpe_local_disk!C!50!80',NULL,NULL,6),(105,'SNMP_if_FastEthernet0_10_Fa0_10','SNMP_if_FastEthernet0_10_Fa0_10',123,3648,'chk_snmp_if!11',0,0,5),(106,'SNMP_if_FastEthernet0_11_Fa0_11','SNMP_if_FastEthernet0_11_Fa0_11',123,3648,'chk_snmp_if!12',0,0,5),(107,'SNMP_if_FastEthernet0_12_Fa0_12','SNMP_if_FastEthernet0_12_Fa0_12',123,3648,'chk_snmp_if!13',0,0,5),(108,'SNMP_if_FastEthernet0_13_Fa0_13','SNMP_if_FastEthernet0_13_Fa0_13',123,3648,'chk_snmp_if!14',0,0,5),(109,'SNMP_if_FastEthernet0_14_Fa0_14','SNMP_if_FastEthernet0_14_Fa0_14',123,3648,'chk_snmp_if!15',0,0,5),(110,'SNMP_if_FastEthernet0_15_Fa0_15','SNMP_if_FastEthernet0_15_Fa0_15',123,3648,'chk_snmp_if!16',0,0,5),(111,'SNMP_if_FastEthernet0_16_Fa0_16','SNMP_if_FastEthernet0_16_Fa0_16',123,3648,'chk_snmp_if!17',0,0,5),(112,'SNMP_if_FastEthernet0_17_Fa0_17','SNMP_if_FastEthernet0_17_Fa0_17',123,3648,'chk_snmp_if!18',0,0,5),(113,'SNMP_if_FastEthernet0_18_Fa0_18','SNMP_if_FastEthernet0_18_Fa0_18',123,3648,'chk_snmp_if!19',0,0,5),(114,'SNMP_if_FastEthernet0_19_Fa0_19','SNMP_if_FastEthernet0_19_Fa0_19',123,3648,'chk_snmp_if!20',0,0,5),(115,'SNMP_if_FastEthernet0_1_Fa0_1','SNMP_if_FastEthernet0_1_Fa0_1',123,3648,'chk_snmp_if!2',0,0,5),(116,'SNMP_if_FastEthernet0_20_Fa0_20','SNMP_if_FastEthernet0_20_Fa0_20',123,3648,'chk_snmp_if!21',0,0,5),(117,'SNMP_if_FastEthernet0_21_Fa0_21','SNMP_if_FastEthernet0_21_Fa0_21',123,3648,'chk_snmp_if!22',0,0,5),(118,'SNMP_if_FastEthernet0_22_Fa0_22','SNMP_if_FastEthernet0_22_Fa0_22',123,3648,'chk_snmp_if!23',0,0,5),(119,'SNMP_if_FastEthernet0_23_Fa0_23','SNMP_if_FastEthernet0_23_Fa0_23',123,3648,'chk_snmp_if!24',0,0,5),(120,'SNMP_if_FastEthernet0_24_Fa0_24','SNMP_if_FastEthernet0_24_Fa0_24',123,3648,'chk_snmp_if!25',0,0,5),(121,'SNMP_if_FastEthernet0_3_Fa0_3','SNMP_if_FastEthernet0_3_Fa0_3',123,3648,'chk_snmp_if!4',0,0,5),(122,'SNMP_if_FastEthernet0_4_Fa0_4','SNMP_if_FastEthernet0_4_Fa0_4',123,3648,'chk_snmp_if!5',0,0,5),(123,'SNMP_if_FastEthernet0_5_Fa0_5','SNMP_if_FastEthernet0_5_Fa0_5',123,3648,'chk_snmp_if!6',0,0,5),(124,'SNMP_if_FastEthernet0_6_Fa0_6','SNMP_if_FastEthernet0_6_Fa0_6',123,3648,'chk_snmp_if!7',0,0,5),(125,'SNMP_if_FastEthernet0_7_Fa0_7','SNMP_if_FastEthernet0_7_Fa0_7',123,3648,'chk_snmp_if!8',0,0,5),(126,'SNMP_if_FastEthernet0_8_Fa0_8','SNMP_if_FastEthernet0_8_Fa0_8',123,3648,'chk_snmp_if!9',0,0,5),(127,'SNMP_if_FastEthernet0_9_Fa0_9','SNMP_if_FastEthernet0_9_Fa0_9',123,3648,'chk_snmp_if!10',0,0,5),(128,'SNMP_if_bandwidth_FastEthernet0_10_Fa0_10','SNMP_if_bandwidth_FastEthernet0_10_Fa0_10',123,3408,'chk_snmp_bandwidth!11',0,0,7),(129,'SNMP_if_bandwidth_FastEthernet0_11_Fa0_11','SNMP_if_bandwidth_FastEthernet0_11_Fa0_11',123,3408,'chk_snmp_bandwidth!12',0,0,7),(130,'SNMP_if_bandwidth_FastEthernet0_12_Fa0_12','SNMP_if_bandwidth_FastEthernet0_12_Fa0_12',123,3408,'chk_snmp_bandwidth!13',0,0,7),(131,'SNMP_if_bandwidth_FastEthernet0_13_Fa0_13','SNMP_if_bandwidth_FastEthernet0_13_Fa0_13',123,3408,'chk_snmp_bandwidth!14',0,0,7),(132,'SNMP_if_bandwidth_FastEthernet0_14_Fa0_14','SNMP_if_bandwidth_FastEthernet0_14_Fa0_14',123,3408,'chk_snmp_bandwidth!15',0,0,7),(133,'SNMP_if_bandwidth_FastEthernet0_15_Fa0_15','SNMP_if_bandwidth_FastEthernet0_15_Fa0_15',123,3408,'chk_snmp_bandwidth!16',0,0,7),(134,'SNMP_if_bandwidth_FastEthernet0_16_Fa0_16','SNMP_if_bandwidth_FastEthernet0_16_Fa0_16',123,3408,'chk_snmp_bandwidth!17',0,0,7),(135,'SNMP_if_bandwidth_FastEthernet0_17_Fa0_17','SNMP_if_bandwidth_FastEthernet0_17_Fa0_17',123,3408,'chk_snmp_bandwidth!18',0,0,7),(136,'SNMP_if_bandwidth_FastEthernet0_18_Fa0_18','SNMP_if_bandwidth_FastEthernet0_18_Fa0_18',123,3408,'chk_snmp_bandwidth!19',0,0,7),(137,'SNMP_if_bandwidth_FastEthernet0_19_Fa0_19','SNMP_if_bandwidth_FastEthernet0_19_Fa0_19',123,3408,'chk_snmp_bandwidth!20',0,0,4),(138,'SNMP_if_bandwidth_FastEthernet0_1_Fa0_1','SNMP_if_bandwidth_FastEthernet0_1_Fa0_1',123,3408,'chk_snmp_bandwidth!2',0,0,7),(139,'SNMP_if_bandwidth_FastEthernet0_20_Fa0_20','SNMP_if_bandwidth_FastEthernet0_20_Fa0_20',123,3408,'chk_snmp_bandwidth!21',0,0,7),(140,'SNMP_if_bandwidth_FastEthernet0_21_Fa0_21','SNMP_if_bandwidth_FastEthernet0_21_Fa0_21',123,3408,'chk_snmp_bandwidth!22',0,0,7),(141,'SNMP_if_bandwidth_FastEthernet0_22_Fa0_22','SNMP_if_bandwidth_FastEthernet0_22_Fa0_22',123,3408,'chk_snmp_bandwidth!23',0,0,7),(142,'SNMP_if_bandwidth_FastEthernet0_23_Fa0_23','SNMP_if_bandwidth_FastEthernet0_23_Fa0_23',123,3408,'chk_snmp_bandwidth!24',0,0,7),(143,'SNMP_if_bandwidth_FastEthernet0_24_Fa0_24','SNMP_if_bandwidth_FastEthernet0_24_Fa0_24',123,3408,'chk_snmp_bandwidth!25',0,0,7),(144,'SNMP_if_bandwidth_FastEthernet0_3_Fa0_3','SNMP_if_bandwidth_FastEthernet0_3_Fa0_3',123,3408,'chk_snmp_bandwidth!4',0,0,7),(145,'SNMP_if_bandwidth_FastEthernet0_4_Fa0_4','SNMP_if_bandwidth_FastEthernet0_4_Fa0_4',123,3408,'chk_snmp_bandwidth!5',0,0,7),(146,'SNMP_if_bandwidth_FastEthernet0_5_Fa0_5','SNMP_if_bandwidth_FastEthernet0_5_Fa0_5',123,3408,'chk_snmp_bandwidth!6',0,0,7),(147,'SNMP_if_bandwidth_FastEthernet0_6_Fa0_6','SNMP_if_bandwidth_FastEthernet0_6_Fa0_6',123,3408,'chk_snmp_bandwidth!7',0,0,7),(148,'SNMP_if_bandwidth_FastEthernet0_7_Fa0_7','SNMP_if_bandwidth_FastEthernet0_7_Fa0_7',123,3408,'chk_snmp_bandwidth!8',0,0,7),(149,'SNMP_if_bandwidth_FastEthernet0_8_Fa0_8','SNMP_if_bandwidth_FastEthernet0_8_Fa0_8',123,3408,'chk_snmp_bandwidth!9',0,0,7),(150,'SNMP_if_bandwidth_FastEthernet0_9_Fa0_9','SNMP_if_bandwidth_FastEthernet0_9_Fa0_9',123,3408,'chk_snmp_bandwidth!10',0,0,7),(151,'SNMP_ifoperstatus_FastEthernet0_10_Fa0_10','SNMP_ifoperstatus_FastEthernet0_10_Fa0_10',20,3624,'chk_ifoperstatus!11',0,0,0),(152,'SNMP_ifoperstatus_FastEthernet0_11_Fa0_11','SNMP_ifoperstatus_FastEthernet0_11_Fa0_11',20,3624,'chk_ifoperstatus!12',0,0,0),(153,'SNMP_ifoperstatus_FastEthernet0_12_Fa0_12','SNMP_ifoperstatus_FastEthernet0_12_Fa0_12',20,3624,'chk_ifoperstatus!13',0,0,0),(154,'SNMP_ifoperstatus_FastEthernet0_13_Fa0_13','SNMP_ifoperstatus_FastEthernet0_13_Fa0_13',20,3624,'chk_ifoperstatus!14',0,0,0),(155,'SNMP_ifoperstatus_FastEthernet0_14_Fa0_14','SNMP_ifoperstatus_FastEthernet0_14_Fa0_14',20,3624,'chk_ifoperstatus!15',0,0,0),(156,'SNMP_ifoperstatus_FastEthernet0_15_Fa0_15','SNMP_ifoperstatus_FastEthernet0_15_Fa0_15',20,3624,'chk_ifoperstatus!16',0,0,0),(157,'SNMP_ifoperstatus_FastEthernet0_16_Fa0_16','SNMP_ifoperstatus_FastEthernet0_16_Fa0_16',20,3624,'chk_ifoperstatus!17',0,0,0),(158,'SNMP_ifoperstatus_FastEthernet0_17_Fa0_17','SNMP_ifoperstatus_FastEthernet0_17_Fa0_17',20,3624,'chk_ifoperstatus!18',0,0,0),(159,'SNMP_ifoperstatus_FastEthernet0_18_Fa0_18','SNMP_ifoperstatus_FastEthernet0_18_Fa0_18',20,3624,'chk_ifoperstatus!19',0,0,0),(160,'SNMP_ifoperstatus_FastEthernet0_19_Fa0_19','SNMP_ifoperstatus_FastEthernet0_19_Fa0_19',20,3624,'chk_ifoperstatus!20',0,0,0),(161,'SNMP_ifoperstatus_FastEthernet0_1_Fa0_1','SNMP_ifoperstatus_FastEthernet0_1_Fa0_1',20,3624,'chk_ifoperstatus!2',0,0,0),(162,'SNMP_ifoperstatus_FastEthernet0_20_Fa0_20','SNMP_ifoperstatus_FastEthernet0_20_Fa0_20',20,3624,'chk_ifoperstatus!21',0,0,0),(163,'SNMP_ifoperstatus_FastEthernet0_21_Fa0_21','SNMP_ifoperstatus_FastEthernet0_21_Fa0_21',20,3624,'chk_ifoperstatus!22',0,0,0),(164,'SNMP_ifoperstatus_FastEthernet0_22_Fa0_22','SNMP_ifoperstatus_FastEthernet0_22_Fa0_22',20,3624,'chk_ifoperstatus!23',0,0,0),(165,'SNMP_ifoperstatus_FastEthernet0_23_Fa0_23','SNMP_ifoperstatus_FastEthernet0_23_Fa0_23',20,3624,'chk_ifoperstatus!24',0,0,0),(166,'SNMP_ifoperstatus_FastEthernet0_24_Fa0_24','SNMP_ifoperstatus_FastEthernet0_24_Fa0_24',20,3624,'chk_ifoperstatus!25',0,0,0),(167,'SNMP_ifoperstatus_FastEthernet0_3_Fa0_3','SNMP_ifoperstatus_FastEthernet0_3_Fa0_3',20,3624,'chk_ifoperstatus!4',0,0,0),(168,'SNMP_ifoperstatus_FastEthernet0_4_Fa0_4','SNMP_ifoperstatus_FastEthernet0_4_Fa0_4',20,3624,'chk_ifoperstatus!5',0,0,0),(169,'SNMP_ifoperstatus_FastEthernet0_5_Fa0_5','SNMP_ifoperstatus_FastEthernet0_5_Fa0_5',20,3624,'chk_ifoperstatus!6',0,0,0),(170,'SNMP_ifoperstatus_FastEthernet0_6_Fa0_6','SNMP_ifoperstatus_FastEthernet0_6_Fa0_6',20,3624,'chk_ifoperstatus!7',0,0,0),(171,'SNMP_ifoperstatus_FastEthernet0_7_Fa0_7','SNMP_ifoperstatus_FastEthernet0_7_Fa0_7',20,3624,'chk_ifoperstatus!8',0,0,0),(172,'SNMP_ifoperstatus_FastEthernet0_8_Fa0_8','SNMP_ifoperstatus_FastEthernet0_8_Fa0_8',20,3624,'chk_ifoperstatus!9',0,0,0),(173,'SNMP_ifoperstatus_FastEthernet0_9_Fa0_9','SNMP_ifoperstatus_FastEthernet0_9_Fa0_9',20,3624,'chk_ifoperstatus!10',0,0,0),(174,'ssh_load','ssh_load',1,256,'check_load!5!10',0,0,4);
UNLOCK TABLES;
/*!40000 ALTER TABLE `service_names` ENABLE KEYS */;

--
-- Table structure for table `service_overrides`
--

DROP TABLE IF EXISTS `service_overrides`;
CREATE TABLE `service_overrides` (
  `service_id` int(8) unsigned NOT NULL default '0',
  `check_period` smallint(4) unsigned default NULL,
  `notification_period` smallint(4) unsigned default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `event_handler` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `data` text,
  PRIMARY KEY  (`service_id`),
  CONSTRAINT `service_overrides_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service_overrides`
--


/*!40000 ALTER TABLE `service_overrides` DISABLE KEYS */;
LOCK TABLES `service_overrides` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `service_overrides` ENABLE KEYS */;

--
-- Table structure for table `service_templates`
--

DROP TABLE IF EXISTS `service_templates`;
CREATE TABLE `service_templates` (
  `servicetemplate_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `parent_id` smallint(4) unsigned default NULL,
  `check_period` smallint(4) unsigned default NULL,
  `notification_period` smallint(4) unsigned default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `event_handler` smallint(4) unsigned default NULL,
  `data` text,
  `comment` text,
  PRIMARY KEY  (`servicetemplate_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service_templates`
--


/*!40000 ALTER TABLE `service_templates` DISABLE KEYS */;
LOCK TABLES `service_templates` WRITE;
INSERT INTO `service_templates` VALUES (1,'gws-generic',0,3,3,2,2,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"flap_detection_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"event_handler_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"notifications_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n <prop name=\"active_checks_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"process_perf_data\"><![CDATA[1]]>\n </prop>\n <prop name=\"passive_checks_enabled\"><![CDATA[1]]>\n </prop>\n <prop name=\"retain_status_information\"><![CDATA[1]]>\n </prop>\n <prop name=\"max_check_attempts\"><![CDATA[3]]>\n </prop>\n <prop name=\"parallelize_check\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[u,c,w,r]]>\n </prop>\n <prop name=\"retain_nonstatus_information\"><![CDATA[1]]>\n </prop>\n <prop name=\"normal_check_interval\"><![CDATA[5]]>\n </prop>\n <prop name=\"obsess_over_service\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_interval\"><![CDATA[120]]>\n </prop>\n</data>','#====================================================================================================\n'),(2,'gws-udp_nsclient',1,NULL,NULL,30,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(3,'gws-sweep',1,NULL,NULL,169,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(4,'gws-tcp_imap4ssl',1,NULL,NULL,241,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(5,'gws-nagios',1,NULL,NULL,95,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(6,'gws-alive',1,NULL,NULL,2,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(7,'gws-http_basic',1,NULL,NULL,204,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(8,'gws-tcp_imap',1,NULL,NULL,229,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(9,'gws-ping',1,NULL,NULL,8,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_ping!3000.0,80%!5000.0,100%]]>\n </prop>\n</data>','#====================================================================================================\n'),(10,'gws-tcp_https',1,NULL,NULL,45,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(11,'gws-udp_imap4ssl',1,NULL,NULL,265,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(12,'gws-by_ssh_nagios',1,NULL,NULL,61,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(13,'gws-snmp_if_alt',1,NULL,NULL,213,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp_if_alt!1]]>\n </prop>\n</data>','#====================================================================================================\n'),(14,'gws-by_sshid_connections',1,NULL,NULL,42,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_connections!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(15,'gws-udp_http-alt',1,NULL,NULL,216,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(16,'gws-tcp_ssh',1,NULL,NULL,16,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(17,'gws-by_sshid_mem',1,NULL,NULL,104,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_mem!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(18,'gws-by_ssh_process_count',1,NULL,NULL,44,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_count!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(19,'gws-by_ssh_uptime',1,NULL,NULL,106,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(20,'gws-ifoperstatus',1,NULL,NULL,221,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_ifoperstatus!arg]]>\n </prop>\n</data>','#====================================================================================================\n'),(21,'gws-snmp_port',1,NULL,NULL,109,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(22,'gws-tcp_icmp',1,NULL,NULL,242,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(23,'gws-by_sshid_swap_stats',1,NULL,NULL,10,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_swap_stats!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(24,'gws-local_disk',1,NULL,NULL,47,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_disk!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(25,'gws-tcp_imap3',1,NULL,NULL,212,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(26,'gws-by_ssh',1,NULL,NULL,211,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh!Arg1]]>\n </prop>\n</data>','#====================================================================================================\n'),(27,'gws-udp_nsca',1,NULL,NULL,76,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(28,'gws-tcp_nrpe',1,NULL,NULL,155,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(29,'gws-remote_load',1,NULL,NULL,259,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_remote_load!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(30,'gws-by_ssh_log',1,NULL,NULL,3,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_log!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(31,'gws-pop',1,NULL,NULL,113,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(32,'gws-by_ssh_mysql',1,NULL,NULL,NULL,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_myysql!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(33,'gws-by_sshid_load',1,NULL,NULL,89,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_load!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(34,'gws-by_ssh_mailq',1,NULL,NULL,254,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_mailq!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(35,'gws-by_sshid_swap',1,NULL,NULL,173,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_swap!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(36,'gws-udp_snmptrap',1,NULL,NULL,68,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(37,'gws-local_load',1,NULL,NULL,93,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_load!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(38,'gws-by_sshid_log',1,NULL,NULL,3,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_log!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(39,'gws-by_sshid_process_user',1,NULL,NULL,114,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_user!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(40,'gws-udp_smtp',1,NULL,NULL,178,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(41,'gws-tcp',1,NULL,NULL,81,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(42,'gws-udp_nrpe',1,NULL,NULL,115,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(43,'gws-nntp',1,NULL,NULL,165,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(44,'gws-ftp',1,NULL,NULL,162,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(45,'gws-by_ssh_disks',1,NULL,NULL,14,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_disks!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(46,'gws-alive_dum',1,NULL,NULL,201,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(47,'gws-by_sshid',1,NULL,NULL,90,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid!Arg1]]>\n </prop>\n</data>','#====================================================================================================\n'),(48,'gws-by_ssh_swap',1,NULL,NULL,236,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_swap!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(49,'gws-by_sshid_disk',1,NULL,NULL,131,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_disk!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(50,'gws-oracle_login',1,NULL,NULL,143,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_oracle_login!Arg1]]>\n </prop>\n</data>','#====================================================================================================\n'),(51,'gws-tcp_snmp',1,NULL,NULL,250,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(52,'gws-udp_pop3s',1,NULL,NULL,50,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(53,'gws-by_ssh_process_crond',1,NULL,NULL,107,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_cmd!Arg1!Arg2!crond]]>\n </prop>\n</data>','#====================================================================================================\n'),(54,'gws-udp_telnet',1,NULL,NULL,177,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(55,'gws-ifstatus',1,NULL,NULL,261,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(56,'gws-smtp',1,NULL,NULL,136,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(57,'gws-by_ssh_process_ncipher',1,NULL,NULL,107,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_cmd!Arg1!Arg2!ncipher]]>\n </prop>\n</data>','#====================================================================================================\n'),(58,'gws-http_ereg',1,NULL,NULL,207,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_http_ereg!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(59,'gws-by_sshid_mailq',1,NULL,NULL,25,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_mailq!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(60,'gws-by_sshid_process_userargs',1,NULL,NULL,239,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_userargs!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(61,'gws-uptime',1,NULL,NULL,180,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(62,'gws-snmp_port_alt',1,NULL,NULL,56,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(63,'gws-by_sshid_mysql',1,NULL,NULL,NULL,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_myysql!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(64,'gws-apache',1,NULL,NULL,194,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(65,'gws-by_ssh_swap_stats',1,NULL,NULL,228,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_swap_stats!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(66,'gws-snmp_bandwidth',1,NULL,NULL,5,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp_bandwidth!1]]>\n </prop>\n</data>','#====================================================================================================\n'),(67,'gws-tcp_dns',1,NULL,NULL,263,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(68,'gws-remote_disk',1,NULL,NULL,59,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_remote_disk!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(69,'gws-ifstatus_alt',1,NULL,NULL,12,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(70,'gws-local_swap',1,NULL,NULL,262,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_swap!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(71,'gws-tcp_pop3s',1,NULL,NULL,67,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(72,'gws-tcp_nsclient',1,NULL,NULL,199,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(73,'gws-by_sshid_process_args',1,NULL,NULL,120,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_args!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(74,'gws-alive_tcp',1,NULL,NULL,161,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(75,'gws-by_sshid_uptime',1,NULL,NULL,158,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(76,'gws-local_procs',1,NULL,NULL,223,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_procs!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(77,'gws-windows',1,NULL,NULL,247,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(78,'gws-by_ssh_disk',1,NULL,NULL,63,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_disk!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(79,'gws-nrpe_cpu',77,NULL,NULL,175,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_cpu!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(80,'gws-nt_counter_exch_publicrq',77,NULL,NULL,15,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_exch_publicrq!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(81,'gws-udp_imap3',1,NULL,NULL,166,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(82,'gws-udp_imap',1,NULL,NULL,32,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(83,'gws-by_sshid_process_count',1,NULL,NULL,74,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_count!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(84,'gws-udp_pop2',1,NULL,NULL,66,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(85,'gws-ica_master_browser',77,NULL,NULL,94,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(86,'gws-nrpe_mssql_bufcache_hits',77,NULL,NULL,157,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_bufcache_hits!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(87,'gws-nt_counter_exch_mtawq',77,NULL,NULL,148,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_exch_mtawq!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(88,'gws-nrpe_memory_pages',77,NULL,NULL,111,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_memory_pages!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(89,'gws-nt_useddiskspace-q',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!q!80!90]]>\n </prop>\n</data>',NULL),(90,'gws-by_ssh_process_args',1,NULL,NULL,46,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_args!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(91,'gws-tcp_snmptrap',1,NULL,NULL,220,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(92,'gws-by_ssh_process_cmd',1,NULL,NULL,107,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_cmd!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(93,'gws-ica_metaframe_pub_apps',77,NULL,NULL,134,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_ica_metaframe_pub_apps!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(94,'gws-nt_useddiskspace-p',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!p!80!90]]>\n </prop>\n</data>',NULL),(95,'gws-udp_snmp',1,NULL,NULL,58,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(96,'gws-nt_useddiskspace-c',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!c!80!90]]>\n </prop>\n</data>','#====================================================================================================\n'),(97,'gws-tcp_telnet',1,NULL,NULL,141,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(98,'gws-by_ssh_process_usercmd',1,NULL,NULL,217,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_usercmd!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(99,'gws-snmp_if',1,NULL,NULL,245,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp_if!1]]>\n </prop>\n</data>','#====================================================================================================\n'),(100,'gws-by_ssh_mem',1,NULL,NULL,147,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_mem!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(101,'gws-ifoperstatus_alt',1,NULL,NULL,184,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_ifoperstatus_alt!arg]]>\n </prop>\n</data>','#====================================================================================================\n'),(102,'gws-ssh',1,NULL,NULL,255,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(103,'gws-nrpe_exch_mailbox_sendq',77,NULL,NULL,28,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_exch_mailbox_sendq!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(104,'gws-nt_useddiskspace-k',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!k!80!90]]>\n </prop>\n</data>',NULL),(105,'gws-dummy',1,NULL,NULL,144,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(106,'gws-by_sshid_process_cmd',1,NULL,NULL,20,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_cmd!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(107,'gws-by_sshid_disks',1,NULL,NULL,125,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_disks!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(108,'gws-nrpe_mssql_users',77,NULL,NULL,38,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_users!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(109,'gws-udp',1,NULL,NULL,164,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(110,'gws-tcp_smtp',1,NULL,NULL,234,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(111,'gws-win_mssql_memory_grants_pending',77,NULL,NULL,6,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_memory_grants_pending!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(112,'gws-by_ssh_cpu',1,NULL,NULL,98,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_cpu!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(113,'gws-nrpe_mssql_full_scans',77,NULL,NULL,205,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_full_scans!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(114,'gws-nt_counter_mssql_log_used',77,NULL,NULL,218,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_log_used!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(115,'gws-tcp_http-alt',1,NULL,NULL,126,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(116,'gws-nt_useddiskspace-g',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!g!80!90]]>\n </prop>\n</data>',NULL),(117,'gws-hpjetdirect_alt',1,NULL,NULL,48,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(118,'gws-nt_useddiskspace-w',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!w!80!90]]>\n </prop>\n</data>',NULL),(119,'gws-snmp',1,NULL,NULL,82,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(120,'gws-nrpe_mssql_lock_waits',77,NULL,NULL,130,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_lock_waits!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(121,'gws-nrpe_mssql_transactions',77,NULL,NULL,75,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_transactions!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(122,'gws-hpjetdirect',77,NULL,NULL,185,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(123,'perf-check',1,NULL,NULL,NULL,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n</data>','# Generic Check for perf data ussing rrdtool\n'),(124,'gws-citrix',77,NULL,NULL,179,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(125,'gws-by_sshid_nagios',1,NULL,NULL,27,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(126,'gws-ntp',1,NULL,NULL,17,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(127,'gws-nt_commvault',77,NULL,NULL,243,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(128,'gws-remote_swap',1,NULL,NULL,71,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_remote_swap!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(129,'gws-tcp_http',1,NULL,NULL,24,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(130,'gws-nrpe_print_queue',77,NULL,NULL,124,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_print_queue!4!5]]>\n </prop>\n</data>','#====================================================================================================\n'),(131,'gws-by_ssh_load',1,NULL,NULL,101,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_load!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(132,'gws-local_mem',1,NULL,NULL,62,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_mem!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(133,'gws-by_sshid_process_usercmd',1,NULL,NULL,103,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_process_usercmd!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(134,'gws-by_ssh_process_user',1,NULL,NULL,159,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_user!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(135,'gws-snmp_bandwidth_alt',1,NULL,NULL,246,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp_bandwidth_alt!1]]>\n </prop>\n</data>','#====================================================================================================\n'),(136,'gws-udp_https',1,NULL,NULL,182,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(137,'gws-dhcp',1,NULL,NULL,188,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(138,'gws-nrpe_exch_mta_workq',77,NULL,NULL,13,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_exch_mta_workq!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(139,'gws-tcp_pop2',1,NULL,NULL,176,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(140,'gws-nw_abends',77,NULL,NULL,34,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nw_abends!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(141,'gws-nt_counter_exch_mailsq',77,NULL,NULL,186,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_exch_mailsq!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(142,'gws-wins',77,NULL,NULL,153,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_wins!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(143,'gws-nt_counter_disktransfers',77,NULL,NULL,65,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_disktransfers!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(144,'gws-nrpe_exch_public_receiveq',77,NULL,NULL,258,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_exch_public_receiveq!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(145,'gws-nrpe_disk',77,NULL,NULL,84,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_disk!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(146,'gws-snmp_alt',1,NULL,NULL,49,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"retry_check_interval\"><![CDATA[1]]>\n </prop>\n <prop name=\"notification_options\"><![CDATA[c,r]]>\n </prop>\n <prop name=\"command_line\"><![CDATA[chk_snmp_alt!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(147,'gws-local_users',1,NULL,NULL,198,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_local_users!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(148,'gws-dhcp_if',1,NULL,NULL,188,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_dhcp!Arg1]]>\n </prop>\n</data>','#====================================================================================================\n'),(149,'gws-oracle_tns',1,NULL,NULL,121,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(150,'gws-win_mssql_lock_wait_time',77,NULL,NULL,251,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_lock_wait_time!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(151,'gws-nrpe_mssql_latch_wait_time',77,NULL,NULL,108,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_latch_wait_time!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(152,'gws-by_ssh_process_userargs',1,NULL,NULL,118,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_process_userargs!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(153,'gws-by_ssh_connections',1,NULL,NULL,105,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_ssh_connections!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(154,'gws-by_sshid_cpu',1,NULL,NULL,168,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_by_sshid_cpu!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(155,'gws-nt_useddiskspace-r',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!r!80!90]]>\n </prop>\n</data>',NULL),(156,'gws-win_mssql_latch_waits',77,NULL,NULL,122,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_latch_waits!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(157,'gws-nt_counter_mssql_log_growths',77,NULL,NULL,209,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_log_growths!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(158,'gws-nt_counter_mssql_memory_grants_pending',77,NULL,NULL,6,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_memory_grants_pending!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(159,'gws-udp_http',77,NULL,NULL,4,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(160,'gws-nt_useddiskspace-l',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!l!80!90]]>\n </prop>\n</data>',NULL),(161,'gws-nrpe_mssql_memory_grants_pending',77,NULL,NULL,190,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_memory_grants_pending!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(162,'gws-nrpe_local_disk',77,NULL,NULL,260,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_local_disk!C!50!80]]>\n </prop>\n</data>','#====================================================================================================\n'),(163,'gws-nt_useddiskspace-v',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!v!80!90]]>\n </prop>\n</data>',NULL),(164,'gws-nrpe_exch_public_sendq',77,NULL,NULL,64,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_exch_public_sendq!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(165,'gws-nrpe_mssql_log_growths',77,NULL,NULL,163,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_log_growths!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(166,'gws-nt_counter_exch_publicsq',77,NULL,NULL,219,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_exch_publicsq!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(167,'gws-nrpe_iis_bytes_sent',77,NULL,NULL,55,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_iis_bytes_sent!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(168,'gws-nt_useddiskspace-h',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!h!80!90]]>\n </prop>\n</data>',NULL),(169,'gws-nrpe_local_pagefile',77,NULL,NULL,123,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_local_pagefile!Arg1!Arg2]]>\n </prop>\n</data>','#====================================================================================================\n'),(170,'gws-nt_useddiskspace-n',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!n!80!90]]>\n </prop>\n</data>',NULL),(171,'gws-nt_counter_exch_mailrq',77,NULL,NULL,171,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_exch_mailrq!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(172,'gws-nrpe_disktransfers',77,NULL,NULL,80,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_disktransfers!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(173,'gws-nt_counter_network_interface',77,NULL,NULL,264,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_network_interface!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(174,'gws-nt_counter_mssql_bufcache_hits',77,NULL,NULL,1,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_bufcache_hits!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(175,'gws-nrpe_mssql_deadlocks',77,NULL,NULL,142,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_deadlocks!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(176,'gws-nt_uptime',77,NULL,NULL,193,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(177,'gws-nt_useddiskspace-i',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!i!80!90]]>\n </prop>\n</data>',NULL),(178,'gws-nt_useddiskspace-e',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!e!80!90]]>\n </prop>\n</data>',NULL),(179,'gws-nrpe_iis_get_requests',77,NULL,NULL,52,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_iis_get_requests!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(180,'gws-nt_useddiskspace-s',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!s!80!90]]>\n </prop>\n</data>',NULL),(181,'gws-nt_counter_memory_pages',77,NULL,NULL,191,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_memory_pages!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(182,'gws-nrpe_iis_bytes_total',77,NULL,NULL,117,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_iis_bytes_total!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(183,'gws-win_mssql_transactions',77,NULL,NULL,232,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_transactions!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(184,'gws-nt_cpuload',77,NULL,NULL,247,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_cpuload!10,50,80,60,50,80,1440,50,80]]>\n </prop>\n</data>','#====================================================================================================\n'),(185,'gws-nt_counter_mssql_transactions',77,NULL,NULL,232,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_transactions!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(186,'gws-nt_memuse',77,NULL,NULL,192,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_memuse!80!90]]>\n </prop>\n</data>','#====================================================================================================\n'),(187,'gws-nt_useddiskspace-m',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!m!80!90]]>\n </prop>\n</data>',NULL),(188,'gws-nt_useddiskspace-z',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!z!80!90]]>\n </prop>\n</data>',NULL),(189,'gws-nrpe_iis_bytes_received',77,NULL,NULL,33,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_iis_bytes_received!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(190,'gws-nt_useddiskspace-u',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!u!80!90]]>\n </prop>\n</data>',NULL),(191,'gws-nt_useddiskspace-d',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!d!80!90]]>\n </prop>\n</data>',NULL),(192,'gws-nt_counter_mssql_lock_wait_time',77,NULL,NULL,251,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_lock_wait_time!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(193,'gws-nt_useddiskspace-o',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!o!80!90]]>\n </prop>\n</data>',NULL),(194,'gws-nrpe_exch_mailbox_receiveq',77,NULL,NULL,140,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_exch_mailbox_receiveq!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(195,'gws-nrpe_local_memory',77,NULL,NULL,51,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_local_memory!50!80]]>\n </prop>\n</data>','#====================================================================================================\n'),(196,'gws-nt_counter_mssql_lock_waits',77,NULL,NULL,53,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_lock_waits!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(197,'gws-udp_pop3',77,NULL,NULL,19,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(198,'gws-nt_counter_mssql_deadlocks',77,NULL,NULL,88,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_deadlocks!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(199,'gws-tcp_nsca',77,NULL,NULL,83,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(200,'gws-nt_useddiskspace-t',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!t!80!90]]>\n </prop>\n</data>',NULL),(201,'gws-udp_icmp',77,NULL,NULL,18,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(202,'gws-nt_useddiskspace-y',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!y!80!90]]>\n </prop>\n</data>',NULL),(203,'gws-nrpe_mssql_log_used',77,NULL,NULL,22,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_log_used!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(204,'gws-nrpe_local_cpu',77,NULL,NULL,202,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_local_cpu!50!80]]>\n </prop>\n</data>','#====================================================================================================\n'),(205,'gws-win_mssql_log_used',77,NULL,NULL,218,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_counter_mssql_log_used!50!100]]>\n </prop>\n</data>','#====================================================================================================\n'),(206,'gws-nt_useddiskspace-j',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!j!80!90]]>\n </prop>\n</data>',NULL),(207,'gws-nrpe_mssql_lock_wait_time',77,NULL,NULL,237,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_mssql_lock_wait_time!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(208,'gws-nt_useddiskspace-f',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!f!80!90]]>\n </prop>\n</data>',NULL),(209,'gws-nt_clientversion',77,NULL,NULL,73,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n'),(210,'gws-nt_useddiskspace-x',77,NULL,NULL,37,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nt_useddiskspace!x!80!90]]>\n </prop>\n</data>',NULL),(211,'gws-nrpe_iis_post_requests',77,NULL,NULL,54,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_iis_post_requests!Arg1!Arg2!Arg3]]>\n </prop>\n</data>','#====================================================================================================\n'),(212,'gws-nrpe_cpu_usrpwd',77,NULL,NULL,183,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[chk_nrpe_cpu_usrpwd!Arg1!Arg2!Arg3!Arg4]]>\n </prop>\n</data>','#====================================================================================================\n'),(213,'gws-tcp_pop3',77,NULL,NULL,238,NULL,'<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"command_line\"><![CDATA[]]>\n </prop>\n</data>','#====================================================================================================\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `service_templates` ENABLE KEYS */;

--
-- Table structure for table `servicegroup_service`
--

DROP TABLE IF EXISTS `servicegroup_service`;
CREATE TABLE `servicegroup_service` (
  `servicegroup_id` int(6) unsigned NOT NULL default '0',
  `host_id` int(6) unsigned NOT NULL default '0',
  `service_id` int(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`servicegroup_id`,`host_id`,`service_id`),
  KEY `host_id` (`host_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `servicegroup_service_ibfk_1` FOREIGN KEY (`servicegroup_id`) REFERENCES `servicegroups` (`servicegroup_id`) ON DELETE CASCADE,
  CONSTRAINT `servicegroup_service_ibfk_2` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE,
  CONSTRAINT `servicegroup_service_ibfk_3` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `servicegroup_service`
--


/*!40000 ALTER TABLE `servicegroup_service` DISABLE KEYS */;
LOCK TABLES `servicegroup_service` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `servicegroup_service` ENABLE KEYS */;

--
-- Table structure for table `servicegroups`
--

DROP TABLE IF EXISTS `servicegroups`;
CREATE TABLE `servicegroups` (
  `servicegroup_id` int(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`servicegroup_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `servicegroups`
--


/*!40000 ALTER TABLE `servicegroups` DISABLE KEYS */;
LOCK TABLES `servicegroups` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `servicegroups` ENABLE KEYS */;

--
-- Table structure for table `serviceprofile`
--

DROP TABLE IF EXISTS `serviceprofile`;
CREATE TABLE `serviceprofile` (
  `servicename_id` smallint(4) unsigned NOT NULL default '0',
  `serviceprofile_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`servicename_id`,`serviceprofile_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `serviceprofile`
--


/*!40000 ALTER TABLE `serviceprofile` DISABLE KEYS */;
LOCK TABLES `serviceprofile` WRITE;
INSERT INTO `serviceprofile` VALUES (4,11),(5,13),(6,4),(6,5),(6,6),(6,7),(6,9),(6,10),(6,12),(6,13),(6,14),(6,15),(6,16),(10,4),(11,4),(12,4),(14,3),(14,14),(15,3),(16,3),(17,3),(18,3),(19,3),(27,5),(27,6),(28,5),(29,5),(30,5),(31,5),(32,5),(33,10),(34,9),(35,9),(36,9),(37,9),(38,9),(39,10),(39,12),(40,9),(40,10),(40,12),(41,10),(41,12),(42,9),(42,12),(43,9),(43,12),(67,5),(67,9),(68,5),(68,9),(70,9),(70,10),(70,12),(71,10),(72,10),(73,10),(74,10),(75,10),(76,10),(77,10),(86,14),(87,14),(91,14),(92,4),(96,15),(97,10),(98,10),(100,5),(100,6),(100,7),(100,16),(101,7),(101,16),(102,7),(102,16),(103,7),(103,16),(104,7),(104,16);
UNLOCK TABLES;
/*!40000 ALTER TABLE `serviceprofile` ENABLE KEYS */;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
CREATE TABLE `services` (
  `service_id` int(8) unsigned NOT NULL auto_increment,
  `host_id` int(6) unsigned NOT NULL default '0',
  `servicename_id` smallint(4) unsigned NOT NULL default '0',
  `servicetemplate_id` smallint(4) unsigned default NULL,
  `serviceextinfo_id` smallint(4) unsigned default NULL,
  `escalation_id` smallint(4) unsigned default NULL,
  `status` tinyint(1) default NULL,
  `check_command` smallint(4) unsigned default NULL,
  `command_line` varchar(255) default NULL,
  `comment` text,
  PRIMARY KEY  (`service_id`),
  KEY `host_id` (`host_id`),
  CONSTRAINT `services_ibfk_1` FOREIGN KEY (`host_id`) REFERENCES `hosts` (`host_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `services`
--


/*!40000 ALTER TABLE `services` DISABLE KEYS */;
LOCK TABLES `services` WRITE;
INSERT INTO `services` VALUES (1,11,7,1,0,0,1,112,'check_local_disk!10!5!/',NULL),(2,11,21,1,0,0,1,39,'check_local_users!75!150',NULL),(3,11,20,1,0,0,1,253,'check_local_procs!150!200!RSZDT',NULL),(38,12,104,162,6,NULL,1,NULL,'chk_nrpe_local_disk!C!50!80',''),(39,12,6,6,NULL,0,1,NULL,NULL,''),(40,12,103,169,6,NULL,1,NULL,'chk_nrpe_local_pagefile!40!50',''),(41,12,102,195,1,NULL,1,NULL,'chk_nrpe_local_memory!500!300',''),(42,12,101,204,6,NULL,1,NULL,'chk_nrpe_local_cpu!50!80',''),(43,12,100,28,NULL,NULL,1,NULL,'',''),(48,14,92,119,NULL,NULL,1,NULL,'chk_snmp!system.sysName.0!Switch0!Switch02.itgroundwork.com',''),(49,14,6,6,NULL,0,1,NULL,NULL,''),(50,14,11,66,9,0,1,NULL,'chk_snmp_bandwidth!1',''),(51,14,10,99,3,NULL,1,245,'chk_snmp_if!1',''),(52,14,12,20,NULL,0,1,NULL,'chk_ifoperstatus!1',''),(53,16,14,16,NULL,0,1,16,'',''),(54,16,15,45,6,0,1,14,'chk_by_ssh_disks!20%!10%',''),(55,16,16,26,1,NULL,1,101,'chk_by_ssh_load!10!20',''),(56,16,17,100,6,0,1,147,'chk_by_ssh_mem!80%!95%',''),(57,16,18,1,NULL,0,1,20,'chk_by_sshid_process_cmd!1:!1:!cron',''),(58,16,19,48,6,0,1,236,'chk_by_ssh_swap!20%!10%','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `services` ENABLE KEYS */;

--
-- Table structure for table `setup`
--

DROP TABLE IF EXISTS `setup`;
CREATE TABLE `setup` (
  `name` varchar(50) NOT NULL default '',
  `type` varchar(50) default NULL,
  `value` varchar(100) default NULL,
  PRIMARY KEY  (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `setup`
--


/*!40000 ALTER TABLE `setup` DISABLE KEYS */;
LOCK TABLES `setup` WRITE;
INSERT INTO `setup` VALUES ('login_authentication','',''),('session_timeout','','3600'),('super_user_password','',''),('user1','resource','/usr/local/groundwork/nagios/libexec/plugins'),('user2','resource','/usr/local/groundwork/nagios/eventhandlers'),('user3','resource','60'),('user4','resource',''),('user5','resource',''),('user6','resource',''),('user7','resource','itgwrk'),('user8','resource',''),('user9','resource',''),('user10','resource',''),('user11','resource',''),('user12','resource',''),('user13','resource',''),('user14','resource',''),('user15','resource',''),('user16','resource',''),('user17','resource','nagios'),('user18','resource','/home/nagios'),('user19','resource',''),('user20','resource',''),('user21','resource',''),('user22','resource','libexec'),('user23','resource','192.168.2.52'),('user24','resource',''),('user25','resource',''),('user26','resource',''),('user27','resource',''),('user28','resource',''),('user29','resource',''),('user30','resource',''),('user31','resource',''),('user32','resource',''),('upload_home','config','/tmp'),('nagios_home','config','/usr/local/groundwork/nagios/etc'),('backup_home','config','/usr/local/groundwork/monarch/backup'),('workspace_home','config','/usr/local/groundwork/monarch/workspace'),('cgi_home','config','/usr/local/groundwork/nagios/etc'),('resource_home','config','/etc/nagios/private/resource.cfg'),('nagios_version','config','1.x'),('log_service_retries','nagios','1'),('ocsp_timeout','nagios','5'),('log_host_retries','nagios','1'),('log_external_commands','nagios','1'),('admin_email','nagios','nagios'),('use_agressive_host_checking','nagios','0'),('global_host_event_handler','nagios',''),('date_format','nagios','us'),('enable_event_handlers','nagios','1'),('check_service_freshness','nagios','1'),('global_service_event_handler','nagios',''),('lock_file','nagios','/usr/local/groundwork/nagios/var/nagios.lock'),('low_service_flap_threshold','nagios','5.0'),('interval_length','nagios','60'),('service_interleave_factor','nagios','s'),('illegal_macro_output_chars','nagios','`~$&|\'\"<>'),('log_initial_states','nagios','0'),('max_concurrent_checks','nagios','0'),('freshness_check_interval','nagios','60'),('nagios_group','nagios','nagios'),('resource_file','nagios','/usr/local/groundwork/nagios/etc/private/resource.cfg'),('event_handler_timeout','nagios','30'),('sleep_time','nagios','1'),('status_update_interval','nagios','15'),('obsess_over_services','nagios','0'),('comment_file','nagios','/usr/local/groundwork/nagios/var/log/comment.log'),('downtime_file','nagios','/usr/local/groundwork/nagios/var/log/downtime.log'),('log_notifications','nagios','1'),('check_external_commands','nagios','1'),('inter_check_delay_method','nagios','s'),('service_reaper_frequency','nagios','10'),('temp_file','nagios','/usr/local/groundwork/nagios/var/log/nagios.tmp'),('admin_pager','nagios','pagenagios'),('use_syslog','nagios','1'),('log_file','nagios','/usr/local/groundwork/nagios/var/nagios.log'),('illegal_object_name_chars=`~!$%^&*|\'\"<>?,()','nagios',''),('host_dependencies','file','24'),('extended_host_info_templates','file','23'),('escalation_templates','file','22'),('servicegroups','file','21'),('extended_host_info','file','20'),('time_periods','file','19'),('service_templates','file','16'),('service_dependency_templates','file','15'),('service_dependency','file','14'),('miscommands','file','13'),('hostgroups','file','11'),('host_templates','file','10'),('hosts','file','9'),('services','file','8'),('extended_service_info_templates','file','7'),('extended_service_info','file','6'),('escalations','file','5'),('contacts','file','4'),('contactgroups','file','3'),('contact_templates','file','2'),('commands','file','1'),('log_event_handlers','nagios','1'),('execute_service_checks','nagios','1'),('host_perfdata_command','nagios','process-host-perfdata'),('service_check_timeout','nagios','60'),('accept_passive_service_checks','nagios','1'),('ocsp_command','nagios',''),('high_host_flap_threshold','nagios','20.0'),('command_file','nagios','/usr/local/groundwork/nagios/var/spool/nagios.cmd'),('retention_update_interval','nagios','60'),('high_service_flap_threshold','nagios','20.0'),('service_perfdata_command','nagios','process-service-perfdata'),('status_file','nagios','/usr/local/groundwork/nagios/var/status.log'),('process_performance_data','nagios','1'),('log_archive_path','nagios','/usr/local/groundwork/nagios/var/log/archives'),('state_retention_file','nagios','/usr/local/groundwork/nagios/var/status.sav'),('enable_flap_detection','nagios','0'),('command_check_interval','nagios','-1'),('aggregate_status_updates','nagios','1'),('low_host_flap_threshold','nagios','5.0'),('check_for_orphaned_services','nagios','0'),('perfdata_timeout','nagios','5'),('enable_notifications','nagios','1'),('nagios_user','nagios','nagios'),('host_check_timeout','nagios','30'),('use_retained_program_state','nagios','0'),('notification_timeout','nagios','30'),('illegal_object_name_chars','nagios','`~!$%^&*|\'\"<>?,()\'='),('retain_state_information','nagios','1'),('log_rotation_method','nagios','d'),('log_passive_service_checks','nagios','1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `setup` ENABLE KEYS */;

--
-- Table structure for table `stage_escalations`
--

DROP TABLE IF EXISTS `stage_escalations`;
CREATE TABLE `stage_escalations` (
  `id` smallint(4) unsigned NOT NULL auto_increment,
  `objects` text,
  `template` smallint(4) default NULL,
  `contactgroups` varchar(50) default NULL,
  `first_notify` smallint(4) default NULL,
  `last_notify` smallint(4) default NULL,
  `interval_notify` smallint(4) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_escalations`
--


/*!40000 ALTER TABLE `stage_escalations` DISABLE KEYS */;
LOCK TABLES `stage_escalations` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_escalations` ENABLE KEYS */;

--
-- Table structure for table `stage_host_hostgroups`
--

DROP TABLE IF EXISTS `stage_host_hostgroups`;
CREATE TABLE `stage_host_hostgroups` (
  `name` varchar(50) NOT NULL default '',
  `user_acct` varchar(50) NOT NULL default '',
  `hostgroup` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`name`,`user_acct`,`hostgroup`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_host_hostgroups`
--


/*!40000 ALTER TABLE `stage_host_hostgroups` DISABLE KEYS */;
LOCK TABLES `stage_host_hostgroups` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_host_hostgroups` ENABLE KEYS */;

--
-- Table structure for table `stage_host_services`
--

DROP TABLE IF EXISTS `stage_host_services`;
CREATE TABLE `stage_host_services` (
  `name` varchar(50) NOT NULL default '',
  `user_acct` varchar(50) NOT NULL default '',
  `host` varchar(50) NOT NULL default '',
  `type` varchar(20) default NULL,
  `status` tinyint(1) default NULL,
  `service_id` int(10) unsigned default NULL,
  PRIMARY KEY  (`name`,`user_acct`,`host`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_host_services`
--


/*!40000 ALTER TABLE `stage_host_services` DISABLE KEYS */;
LOCK TABLES `stage_host_services` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_host_services` ENABLE KEYS */;

--
-- Table structure for table `stage_hosts`
--

DROP TABLE IF EXISTS `stage_hosts`;
CREATE TABLE `stage_hosts` (
  `name` varchar(50) NOT NULL default '',
  `user_acct` varchar(50) NOT NULL default '',
  `type` varchar(20) default NULL,
  `status` tinyint(1) default NULL,
  `alias` varchar(50) default NULL,
  `address` varchar(50) default NULL,
  `os` varchar(50) default NULL,
  `hostprofile` varchar(50) default NULL,
  `serviceprofile` varchar(50) default NULL,
  `info` varchar(50) default NULL,
  PRIMARY KEY  (`name`,`user_acct`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_hosts`
--


/*!40000 ALTER TABLE `stage_hosts` DISABLE KEYS */;
LOCK TABLES `stage_hosts` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_hosts` ENABLE KEYS */;

--
-- Table structure for table `stage_other`
--

DROP TABLE IF EXISTS `stage_other`;
CREATE TABLE `stage_other` (
  `name` varchar(255) NOT NULL default '',
  `type` varchar(50) NOT NULL default '',
  `parent` varchar(255) NOT NULL default '',
  `data` text,
  `comment` text,
  PRIMARY KEY  (`name`,`type`,`parent`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_other`
--


/*!40000 ALTER TABLE `stage_other` DISABLE KEYS */;
LOCK TABLES `stage_other` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_other` ENABLE KEYS */;

--
-- Table structure for table `stage_status`
--

DROP TABLE IF EXISTS `stage_status`;
CREATE TABLE `stage_status` (
  `id` smallint(4) unsigned NOT NULL auto_increment,
  `type` varchar(50) default NULL,
  `message` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `stage_status`
--


/*!40000 ALTER TABLE `stage_status` DISABLE KEYS */;
LOCK TABLES `stage_status` WRITE;
INSERT INTO `stage_status` VALUES (1,'start','2005-06-10 11:28:33'),(2,'info','Reading: /etc/nagios/nagios.cfg'),(3,'info','Reading: /etc/nagios/cgi.cfg'),(4,'info','File /etc/nagios/extended_service_info.cfg: 16 objects staged 0 object rejected.'),(5,'info','File /etc/nagios/checkcommands.cfg: 259 objects staged 0 object rejected.'),(6,'info','File /etc/nagios/hosts.cfg: 9 objects staged 0 object rejected.'),(7,'info','File /etc/nagios/hostgroups.cfg: 3 objects staged 0 object rejected.'),(8,'info','File /etc/nagios/groundwork-switches-hosts.cfg: 2 objects staged 0 object rejected.'),(9,'info','File /etc/nagios/contacts.cfg: 3 objects staged 0 object rejected.'),(10,'info','File /etc/nagios/host_templates.cfg: 2 objects staged 0 object rejected.'),(11,'info','File /etc/nagios/contactgroups.cfg: 2 objects staged 0 object rejected.'),(12,'info','File /etc/nagios/services.cfg: 10 objects staged 0 object rejected.'),(13,'info','File /etc/nagios/standard-service-profiles.cfg: 21 objects staged 0 object rejected.'),(14,'info','File /etc/nagios/contact_templates.cfg: 2 objects staged 0 object rejected.'),(15,'info','File /etc/nagios/misccommands.cfg: 6 objects staged 0 object rejected.'),(16,'info','File /etc/nagios/groundwork-switches-hosts-services.cfg: 10 objects staged 0 object rejected.'),(17,'info','File /etc/nagios/service_dependency_templates.cfg: 3 objects staged 0 object rejected.'),(18,'info','File /etc/nagios/escalations.cfg: 0 objects staged 0 object rejected.'),(19,'info','File /etc/nagios/extended_service_info_templates.cfg: 8 objects staged 0 object rejected.'),(20,'info','File /etc/nagios/timeperiods.cfg: 4 objects staged 0 object rejected.'),(21,'info','File /etc/nagios/service_templates.cfg: 213 objects staged 0 object rejected.'),(22,'info','File /etc/nagios/service_dependency.cfg: 5 objects staged 0 object rejected.'),(23,'info','2005-06-10 11:28:35 Stage completed.'),(24,'info','Reading: /etc/nagios/private/resource.cfg'),(25,'end','2005-06-10 11:28:35 Stage completed.'),(26,'start','2005-06-10 11:28:35 Processing.'),(27,'info','Loading commands'),(28,'info','Loading timeperiods'),(29,'info','Loading contact templates'),(30,'info','Loading contacts'),(31,'info','Loading contactgroups'),(32,'info','Loading host templates'),(33,'info','Loading service templates'),(34,'info','Loading hostextinfo templates'),(35,'info','Loading serviceextinfo templates'),(36,'info','Loading hostgroupescalation templates'),(37,'info','Loading hostescalation templates'),(38,'info','Loading serviceescalation templates'),(39,'info','Loading hosts'),(40,'info','Setting host parent relationships'),(41,'info','Loading host dependencies'),(42,'info','Loading hostextinfo'),(43,'info','Moving parent directive from templates to hosts'),(44,'info','Removing redundant templates'),(45,'info','Removing hostgroup templates'),(46,'info','Loading hostgroups'),(47,'info','Creating service names'),(48,'info','Loading services'),(49,'info','Loading servicegroups'),(50,'info','Loading service dependency templates'),(51,'info','Loading service dependencies'),(52,'info','Updating service names with dependency info'),(53,'info','Loading service extended info'),(54,'info','Loading hostgroup escalations'),(55,'info','Loading host escalations'),(56,'info','--hostgroups'),(57,'info','--hosts'),(58,'info','Loading service escalations'),(59,'info','--hostgroups'),(60,'info','--hosts'),(61,'info','--services'),(62,'completed','2005-06-10 11:28:43');
UNLOCK TABLES;
/*!40000 ALTER TABLE `stage_status` ENABLE KEYS */;

--
-- Table structure for table `time_periods`
--

DROP TABLE IF EXISTS `time_periods`;
CREATE TABLE `time_periods` (
  `timeperiod_id` int(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `alias` varchar(50) NOT NULL default '',
  `data` text,
  `comment` text,
  PRIMARY KEY  (`timeperiod_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `time_periods`
--


/*!40000 ALTER TABLE `time_periods` DISABLE KEYS */;
LOCK TABLES `time_periods` WRITE;
INSERT INTO `time_periods` VALUES (1,'workhours','\"Normal\" Working Hours','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"friday\"><![CDATA[09:00-17:00]]>\n </prop>\n <prop name=\"tuesday\"><![CDATA[09:00-17:00]]>\n </prop>\n <prop name=\"monday\"><![CDATA[09:00-17:00]]>\n </prop>\n <prop name=\"wednesday\"><![CDATA[09:00-17:00]]>\n </prop>\n <prop name=\"thursday\"><![CDATA[09:00-17:00]]>\n </prop>\n</data>','# \'workhours\' timeperiod definition\n'),(2,'nonworkhours','Non-Work Hours','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"sunday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"friday\"><![CDATA[00:00-09:00,17:00-24:00]]>\n </prop>\n <prop name=\"tuesday\"><![CDATA[00:00-09:00,17:00-24:00]]>\n </prop>\n <prop name=\"monday\"><![CDATA[00:00-09:00,17:00-24:00]]>\n </prop>\n <prop name=\"saturday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"wednesday\"><![CDATA[00:00-09:00,17:00-24:00]]>\n </prop>\n <prop name=\"thursday\"><![CDATA[00:00-09:00,17:00-24:00]]>\n </prop>\n</data>','# \'nonworkhours\' timeperiod definition\n'),(3,'24x7','24 Hours A Day, 7 Days A Week','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"sunday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"friday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"tuesday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"monday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"saturday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"wednesday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"thursday\"><![CDATA[00:00-24:00]]>\n </prop>\n</data>','# \'24x7\' timeperiod definition\n'),(4,'none','No Time Is A Good Time','<?xml version=\"1.0\" ?>\n<data>\n <prop name=\"sunday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"friday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"tuesday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"monday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"saturday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"wednesday\"><![CDATA[00:00-24:00]]>\n </prop>\n <prop name=\"thursday\"><![CDATA[00:00-24:00]]>\n </prop>\n</data>','# \'none\' timeperiod definition\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `time_periods` ENABLE KEYS */;

--
-- Table structure for table `tree_template_contactgroup`
--

DROP TABLE IF EXISTS `tree_template_contactgroup`;
CREATE TABLE `tree_template_contactgroup` (
  `tree_id` smallint(4) unsigned NOT NULL default '0',
  `template_id` smallint(4) unsigned NOT NULL default '0',
  `contactgroup_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`tree_id`,`template_id`,`contactgroup_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tree_template_contactgroup`
--


/*!40000 ALTER TABLE `tree_template_contactgroup` DISABLE KEYS */;
LOCK TABLES `tree_template_contactgroup` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `tree_template_contactgroup` ENABLE KEYS */;

--
-- Table structure for table `user_group`
--

DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `usergroup_id` smallint(4) unsigned NOT NULL default '0',
  `user_id` smallint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`usergroup_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_group_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_group_ibfk_2` FOREIGN KEY (`usergroup_id`) REFERENCES `user_groups` (`usergroup_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `user_group`
--


/*!40000 ALTER TABLE `user_group` DISABLE KEYS */;
LOCK TABLES `user_group` WRITE;
INSERT INTO `user_group` VALUES (1,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_group` ENABLE KEYS */;

--
-- Table structure for table `user_groups`
--

DROP TABLE IF EXISTS `user_groups`;
CREATE TABLE `user_groups` (
  `usergroup_id` smallint(4) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(100) default NULL,
  PRIMARY KEY  (`usergroup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `user_groups`
--


/*!40000 ALTER TABLE `user_groups` DISABLE KEYS */;
LOCK TABLES `user_groups` WRITE;
INSERT INTO `user_groups` VALUES (1,'super_users','System defined group granted complete access.');
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_groups` ENABLE KEYS */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` smallint(4) unsigned NOT NULL auto_increment,
  `user_acct` varchar(20) NOT NULL default '',
  `user_name` varchar(50) NOT NULL default '',
  `password` varchar(20) NOT NULL default '',
  `session` int(10) unsigned default NULL,
  PRIMARY KEY  (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--


/*!40000 ALTER TABLE `users` DISABLE KEYS */;
LOCK TABLES `users` WRITE;
INSERT INTO `users` VALUES (1,'super_user','Super User Account','password',1118528315);
UNLOCK TABLES;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

