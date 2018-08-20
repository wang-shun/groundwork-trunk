#
################################################################################
#Cacti Database Migration Script for settings
###############################################################################
#

use cacti;

REPLACE INTO `settings` VALUES ('path_rrdtool','/usr/local/groundwork/common/bin/rrdtool');
REPLACE INTO `settings` VALUES ('path_php_binary','/usr/local/groundwork/php/bin/php');
REPLACE INTO `settings` VALUES ('path_snmpwalk','/usr/local/groundwork/common/bin/snmpwalk');
REPLACE INTO `settings` VALUES ('path_snmpget','/usr/local/groundwork/common/bin//snmpget');
REPLACE INTO `settings` VALUES ('path_snmpbulkwalk','/usr/local/groundwork/common/bin/snmpbulkwalk');
REPLACE INTO `settings` VALUES ('path_snmpgetnext','/usr/local/groundwork/common/bin/snmpgetnext');
REPLACE INTO `settings` VALUES ('path_cactilog','/usr/local/groundwork/cacti/htdocs/log/cacti.log');
REPLACE INTO `settings` VALUES ('date',NOW());
REPLACE INTO `settings` VALUES ('snmp_version','net-snmp');
REPLACE INTO `settings` VALUES ('path_rrdtool_default_font','');
REPLACE INTO `settings` VALUES ('path_spine','/usr/local/groundwork/common/bin/spine');
REPLACE INTO `settings` VALUES ('extended_paths','');

REPLACE INTO `settings` VALUES ('rrdtool_version','rrd-1.4.x');
REPLACE INTO `settings` VALUES ('path_webroot','/usr/local/groundwork/cacti/htdocs');

#
# Host Availability settings
# Method 4 means SNMP OR PING
#

REPLACE INTO `settings` VALUES ('availability_method',4);
REPLACE INTO `settings` VALUES ('ping_retries',3);

REPLACE INTO `settings` VALUES ('alert_deadnotify','');
REPLACE INTO `settings` VALUES ('alert_email','');

REPLACE INTO `settings` VALUES ('alert_base_url','http://localhost/nms-cacti');


