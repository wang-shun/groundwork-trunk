##
##	Cacti Database Migration
##	from 0.8.6j to 0.8.7b
##
##	(C) Copyright 2008, Groundwork Open Source
##	Daniel Emmanuel Feinsmith
##

#
#	Migrate the Schema.
#

ALTER TABLE host MODIFY COLUMN status_last_error VARCHAR(255);
ALTER TABLE data_template_rrd MODIFY COLUMN rrd_maximum VARCHAR(20) NOT NULL DEFAULT 0, MODIFY COLUMN rrd_minimum VARCHAR(20) NOT NULL DEFAULT 0;
ALTER TABLE host ADD INDEX disabled(disabled);
ALTER TABLE poller_item ADD INDEX rrd_next_step(rrd_next_step);
ALTER TABLE poller_item ADD INDEX action(action);
ALTER TABLE user_auth ADD INDEX username(username);
ALTER TABLE user_auth ADD INDEX realm(realm);
ALTER TABLE user_log ADD INDEX username(username);
ALTER TABLE data_input ADD INDEX name(name);
ALTER TABLE user_auth ADD COLUMN enabled CHAR(2) DEFAULT 'on';
ALTER TABLE user_auth ADD INDEX enabled(enabled);
ALTER TABLE host ADD COLUMN availability_method SMALLINT(5) UNSIGNED NOT NULL default 2 AFTER snmp_timeout;
ALTER TABLE host ADD COLUMN ping_method SMALLINT(5) UNSIGNED default 0 AFTER availability_method;
ALTER TABLE host ADD COLUMN ping_port INT(12) UNSIGNED default 0 AFTER ping_method;
ALTER TABLE host ADD COLUMN ping_timeout INT(12) UNSIGNED default 500 AFTER ping_port;
ALTER TABLE host ADD COLUMN ping_retries INT(12) UNSIGNED default 2 AFTER ping_timeout;
ALTER TABLE host ADD COLUMN max_oids INT(12) UNSIGNED default 10 AFTER ping_retries;
ALTER TABLE host ADD COLUMN notes TEXT AFTER hostname;
ALTER TABLE host ADD COLUMN snmp_auth_protocol CHAR(5) default '' AFTER snmp_password;
ALTER TABLE host ADD COLUMN snmp_priv_passphrase varchar(200) default '' AFTER snmp_auth_protocol;
ALTER TABLE host ADD COLUMN snmp_priv_protocol CHAR(6) default '' AFTER snmp_priv_passphrase;
ALTER TABLE host ADD COLUMN snmp_context VARCHAR(64) default '' AFTER snmp_priv_protocol;
ALTER TABLE poller_item ADD COLUMN snmp_auth_protocol CHAR(5) default '' AFTER snmp_password;
ALTER TABLE poller_item ADD COLUMN snmp_priv_passphrase varchar(200) default '' AFTER snmp_auth_protocol;
ALTER TABLE poller_item ADD COLUMN snmp_priv_protocol CHAR(6) default '' AFTER snmp_priv_passphrase;
ALTER TABLE poller_item ADD COLUMN snmp_context VARCHAR(64) default '' AFTER snmp_priv_protocol;

#
#	New Alter's not previously included.
#

ALTER TABLE data_input_data MODIFY COLUMN t_value varchar(2);

ALTER TABLE data_input_fields MODIFY COLUMN input_output varchar(3);
ALTER TABLE data_input_fields MODIFY COLUMN update_rra varchar(2);
ALTER TABLE data_input_fields MODIFY COLUMN allow_nulls varchar(2);

ALTER TABLE data_template_data MODIFY COLUMN t_name varchar(2);
ALTER TABLE data_template_data MODIFY COLUMN t_active varchar(2);
ALTER TABLE data_template_data MODIFY COLUMN active varchar(2);
ALTER TABLE data_template_data MODIFY COLUMN t_rrd_step varchar(2);
ALTER TABLE data_template_data MODIFY COLUMN t_rra_id varchar(2);

ALTER TABLE data_template_rrd MODIFY COLUMN t_rrd_maximum varchar(2);
ALTER TABLE data_template_rrd MODIFY COLUMN rrd_maximum varchar(20);
ALTER TABLE data_template_rrd MODIFY COLUMN t_rrd_minimum varchar(2);
ALTER TABLE data_template_rrd MODIFY COLUMN rrd_minimum varchar(20);
ALTER TABLE data_template_rrd MODIFY COLUMN t_rrd_heartbeat varchar(2);
ALTER TABLE data_template_rrd MODIFY COLUMN t_data_source_type_id varchar(2);
ALTER TABLE data_template_rrd MODIFY COLUMN t_data_source_name varchar(2);
ALTER TABLE data_template_rrd MODIFY COLUMN t_data_input_field_id varchar(2);

ALTER TABLE graph_templates_graph MODIFY COLUMN t_image_format_id varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_title varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_height varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_width varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_upper_limit varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_lower_limit varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_vertical_label varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_auto_scale varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN auto_scale varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_auto_scale_opts varchar(2);
ALTER TABLE graph_templates_graph ADD COLUMN t_slope_mode varchar(2) DEFAULT 0 AFTER vertical_label, ADD COLUMN slope_mode varchar(2) DEFAULT 'on' AFTER t_slope_mode, ADD COLUMN t_scale_log_units varchar(2) default '0' after auto_scale_log, ADD COLUMN scale_log_units varchar(2) default NULL AFTER t_scale_log_units;
ALTER TABLE graph_templates_graph MODIFY COLUMN t_auto_scale_log varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN auto_scale_log varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_auto_scale_rigid varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN auto_scale_rigid varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_auto_padding varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN auto_padding varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_base_value varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_grouping varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN grouping varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_export varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN export varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_unit_value varchar(2);
ALTER TABLE graph_templates_graph MODIFY COLUMN t_unit_exponent_value varchar(2);
ALTER TABLE graph_templates_item ADD COLUMN alpha varchar(2) default 'FF' AFTER color_id;
ALTER TABLE graph_templates_item MODIFY COLUMN hard_return varchar(2);

ALTER TABLE host MODIFY COLUMN status_last_error varchar(255);

#
#	Add SNMPv3 to SNMP Input Methods 
#

INSERT INTO data_input_fields VALUES (DEFAULT, '20832ce12f099c8e54140793a091af90',1,'SNMP Authenticaion Protocol (v3)','snmp_auth_protocol','in','',0,'snmp_auth_protocol','','');
INSERT INTO data_input_fields VALUES (DEFAULT, 'c60c9aac1e1b3555ea0620b8bbfd82cb',1,'SNMP Privacy Passphrase (v3)','snmp_priv_passphrase','in','',0,'snmp_priv_passphrase','','');
INSERT INTO data_input_fields VALUES (DEFAULT, 'feda162701240101bc74148415ef415a',1,'SNMP Privacy Protocol (v3)','snmp_priv_protocol','in','',0,'snmp_priv_protocol','','');
INSERT INTO data_input_fields VALUES (DEFAULT, '2cf7129ad3ff819a7a7ac189bee48ce8',2,'SNMP Authenticaion Protocol (v3)','snmp_auth_protocol','in','',0,'snmp_auth_protocol','','');
INSERT INTO data_input_fields VALUES (DEFAULT, '6b13ac0a0194e171d241d4b06f913158',2,'SNMP Privacy Passphrase (v3)','snmp_priv_passphrase','in','',0,'snmp_priv_passphrase','','');
INSERT INTO data_input_fields VALUES (DEFAULT, '3a33d4fc65b8329ab2ac46a36da26b72',2,'SNMP Privacy Protocol (v3)','snmp_priv_protocol','in','',0,'snmp_priv_protocol','','');

#
#	Rename cactid path to spine path
#

UPDATE settings SET name='path_spine' WHERE name='path_cactid';

delete from settings where name='path_rrdtool';
delete from settings where name='path_php_binary';
delete from settings where name='path_snmpwalk';
delete from settings where name='path_snmpget';
delete from settings where name='path_snmpbulkwalk';
delete from settings where name='path_snmpgetnext';
delete from settings where name='path_cactilog';
delete from settings where name='snmp_version';
delete from settings where name='rrdtool_version';

INSERT INTO `settings` VALUES ('path_rrdtool','/usr/local/groundwork/nms/tools/rrdtool/bin/rrdtool'),('path_php_binary','/usr/local/groundwork/nms/tools/php/bin/php'),('path_snmpwalk','/usr/local/groundwork/nms/tools/net-snmp/bin/snmpwalk'),('path_snmpget','/usr/local/groundwork/nms/tools/net-snmp/bin/snmpget'),('path_snmpbulkwalk','/usr/local/groundwork/nms/tools/net-snmp/bin/snmpbulkwalk'),('path_snmpgetnext','/usr/local/groundwork/nms/tools/net-snmp/bin/snmpgetnext'),('path_cactilog','/usr/local/groundwork/nms/applications/cacti/log/cacti.log'),('snmp_version','net-snmp'),('rrdtool_version','rrd-1.2.x');

