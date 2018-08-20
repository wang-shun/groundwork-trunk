-- $Id: $
-- Copyright (C) 2004-2016  GroundWork Open Source, Inc. (www.groundworkopensource.com)
--
--    This program is free software; you can redistribute it and/or modify
--    it under the terms of version 2 of the GNU General Public License
--    as published by the Free Software Foundation and reprinted below;
--
--    This program is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with this program; if not, write to the Free Software
--    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
--


DELETE FROM ApplicationType;
DELETE FROM EntityType;
DELETE FROM PropertyType;
DELETE FROM ApplicationEntityProperty;

INSERT INTO ApplicationType(ApplicationTypeID, Name, DisplayName, Description, StateTransitionCriteria) 
VALUES (1,'SYSTEM', 'SYSTEM', 'Properties that exist regardless of the Application being monitored', 'Device');
INSERT INTO ApplicationType(ApplicationTypeID, Name, DisplayName, Description, StateTransitionCriteria) 
VALUES (100,'NAGIOS', 'NAGIOS', 'System monitored by Nagios', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(ApplicationTypeID, Name, DisplayName, Description, StateTransitionCriteria) 
VALUES (200,'VEMA', 'VEMA', 'Virtual Environment Monitor Agent', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(ApplicationTypeID, Name, DisplayName, Description, StateTransitionCriteria) 
VALUES (300,'DOWNTIME', 'DOWN', 'In Downtime Management', 'Device;Host;ServiceDescription');


-- If you change the values of EntityTypeID here, 
-- YOU MUST CHANGE THEM IN THE SOURCE CODE AS WELL
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (1,'HOST_STATUS',    'com.groundwork.collage.model.impl.HostStatus');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (2,'SERVICE_STATUS', 'com.groundwork.collage.model.impl.ServiceStatus');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (3,'LOG_MESSAGE',    'com.groundwork.collage.model.impl.LogMessage');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (4,'DEVICE',         'com.groundwork.collage.model.impl.Device');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (5,'HOST',         	'com.groundwork.collage.model.impl.Host');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (6,'HOSTGROUP',     	'com.groundwork.collage.model.impl.HostGroup');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (7,'APPLICATION_TYPE', 'com.groundwork.collage.model.impl.ApplicationType');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (8,'CATEGORY', 'com.groundwork.collage.model.impl.Category');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (9,'CHECK_TYPE', 'com.groundwork.collage.model.impl.CheckType');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (10,'COMPONENT', 'com.groundwork.collage.model.impl.Component');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (11,'MONITOR_STATUS', 'com.groundwork.collage.model.impl.MonitorStatus');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (12,'OPERATION_STATUS', 'com.groundwork.collage.model.impl.OperationStatus');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (13,'PRIORITY', 'com.groundwork.collage.model.impl.Priority');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (14,'SEVERITY', 'com.groundwork.collage.model.impl.Severity');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (15,'STATE_TYPE', 'com.groundwork.collage.model.impl.StateType');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (16,'TYPE_RULE', 'com.groundwork.collage.model.impl.TypeRule');
INSERT INTO EntityType(EntityTypeID, Name, Description) VALUES (17,'MONITOR_SERVER', 'com.groundwork.collage.model.impl.MonitorServer');

INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (18,'LOG_MESSAGE_STATISTICS', 'com.groundwork.collage.model.impl.LogMessageStatistic', true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (19,'HOST_STATISTICS', 'com.groundwork.collage.model.impl.HostStatistic', true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (20,'SERVICE_STATISTICS', 'com.groundwork.collage.model.impl.ServiceStatistic', true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (21,'HOST_STATE_TRANSITIONS', 'com.groundwork.collage.model.impl.HostStateTransition', true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES (22,'SERVICE_STATE_TRANSITIONS', 'com.groundwork.collage.model.impl.ServiceStateTransition', true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(23,'SERVICE_GROUP','com.groundwork.collage.model.impl.ServiceGroup',true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(24,'CUSTOM_GROUP','com.groundwork.collage.model.impl.CustomGroup',true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(25,'HOST_CATEGORY','com.groundwork.collage.model.impl.HostCategory',true);
INSERT INTO EntityType(EntityTypeID, Name, Description, IsLogicalEntity) VALUES(26,'SERVICE_CATEGORY','com.groundwork.collage.model.impl.ServiceCategory',true);

-- Populate Action Tables
INSERT INTO ActionType (Name, ClassName) VALUES( 'LOG_MESSAGE_OPERATION_STATUS', 'org.groundwork.foundation.bs.actions.UpdateOperationStatusAction');
INSERT INTO ActionType (Name, ClassName) VALUES( 'SCRIPT_ACTION', 'org.groundwork.foundation.bs.actions.ShellScriptAction');
INSERT INTO ActionType (Name, ClassName) VALUES( 'HTTP_REQUEST_ACTION', 'org.groundwork.foundation.bs.actions.HttpRequestAction');
INSERT INTO ActionType (Name, ClassName) VALUES( 'NAGIOS_ACKNOWLEDGE_ACTION', 'org.groundwork.foundation.bs.actions.NagiosAcknowledgeAction');

-- Actions and Action Properties

-- NAGIOS Acknowledge
INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'NAGIOS_ACKNOWLEDGE_ACTION'), 'Nagios Acknowledge', 'Acknowledge Nagios Log Message');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Nagios Acknowledge'), 'NagiosCommandFile', '/usr/local/groundwork/nagios/var/spool/nagios.cmd');

-- Log Message Operation Status Actions
INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'), 'Accept Log Message', 'Update Log Message Operation Status To Accepted');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Accept Log Message'), 'OperationStatus', 'ACCEPTED');

-- Close

INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'), 'Close Log Message', 'Update Log Message Operation Status To Closed');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Close Log Message'), 'OperationStatus', 'CLOSED');

-- Notify

INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'), 'Notify Log Message', 'Update Log Message Operation Status To Notified');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Notify Log Message'), 'OperationStatus', 'NOTIFIED');

-- Open

INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'), 'Open Log Message', 'Update Log Message Operation Status To Open');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Open Log Message'), 'OperationStatus', 'OPEN');

-- Acknowledge

INSERT INTO Action (ActionTypeID, Name, Description) 
VALUES( (SELECT ActionTypeID FROM ActionType WHERE Name = 'LOG_MESSAGE_OPERATION_STATUS'), 'Acknowledge Log Message', 'Update Log Message Operation Status To Acknowledged');

INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = 'Acknowledge Log Message'), 'OperationStatus', 'ACKNOWLEDGED');


-- System Application Actions - Common to all application types
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Accept Log Message'));

INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Close Log Message'));

INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Notify Log Message'));

INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Open Log Message'));

INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (1 /* SYSTEM */, (SELECT ActionID FROM Action WHERE Name = 'Acknowledge Log Message'));


-- Nagios Application Actions
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID)
VALUES (100 /* NAGIOS */, (SELECT ActionID FROM Action WHERE Name = 'Nagios Acknowledge'));


-- Performance Data Label information 

INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('Current Load_load1','Load average last minute','Load factor','load' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('Current Load_load5','Load average last 5 minutes','Load factor','load' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('Current Load_load15','Load average last 15 minutes','Load factor','load' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('Current Users_users','Users on System','users','users' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('Root Partition_/','Used space on Root partition','Percentage used','%' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('icmp_ping_alive_rta','Ping round trip average','rta','ms' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('icmp_ping_alive_pl','Process load','pl','%' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('http_alive_time','Web Server time','time','sec' );
INSERT INTO PerformanceDataLabel(PerformanceName,ServiceDisplayName,MetricLabel,Unit) 
Values('http_alive_size','Web Server size','size','kb' );

-- Plugin Management
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Multiplatform', 32, 'Multiplatform 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Multiplatform', 64, 'Multiplatform 64 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('AIX-PowerPC',   32, 'AIX PowerPC 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('AIX-PowerPC',   64, 'AIX PowerPC 64 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Linux-Intel',   32, 'Linux Intel 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Linux-Intel',   64, 'Linux Intel 64 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris-Intel', 32, 'Solaris Intel 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris-Intel', 64, 'Solaris Intel 64 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris-SPARC', 32, 'Solaris SPARC 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Solaris-SPARC', 64, 'Solaris SPARC 64 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Windows-Intel', 32, 'Windows Intel 32 bit');
INSERT INTO PluginPlatform (Name, Arch, Description) VALUES ('Windows-Intel', 64, 'Windows Intel 64 bit');

-- GDMA Auto Register stuff

INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('GDMA', 'GDMA', 'System monitored by GDMA', 'Device;Host;ServiceDescription');
INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),'Register Agent','Invoke a script for the selected message');
INSERT INTO ActionProperty (ActionID, Name, Value) VALUES( (SELECT ActionID FROM Action WHERE Name = 'Register Agent'), 'Script', '/usr/local/groundwork/foundation/scripts/registerAgent.pl');
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'), (SELECT ActionID FROM Action WHERE Name = 'Register Agent'));
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent')  ,'agent-type','agent-type');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-name','host-name');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent')  ,'host-ip','host-ip');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-mac','host-mac');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent')  ,'operating-system','operating-system');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent'),'host-characteristic','host-characteristic');

INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),'Register Agent by Profile','Invoke a script for the selected message');
INSERT INTO ActionProperty (ActionID, Name, Value) VALUES( (SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'), 'Script', '/usr/local/groundwork/foundation/scripts/registerAgentByProfile.pl');
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'), (SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'));
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile')  ,'agent-type','agent-type');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-name','host-name');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile')  ,'host-ip','host-ip');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-mac','host-mac');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile')  ,'operating-system','operating-system');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'host-profile-name','host-profile-name');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Register Agent by Profile'),'service-profile-name','service-profile-name');

-- NoMa Notification Stuff
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('NOMA', 'NOMA', 'NoMa Notification', 'Device;Host;ServiceDescription');
INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),'Noma Notify For Host','Invoke a script for the selected message');
INSERT INTO ActionProperty (ActionID, Name, Value) VALUES( (SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'), 'Script', '/usr/local/groundwork/noma/notifier/alert_via_noma.pl');
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'), (SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host'));
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-c', '-c');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'notifyType', 'notifyType');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-s', '-s');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hoststate','hoststate');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-H', '-H');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hostname','hostname');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-G', '-G');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hostgroupnames', 'hostgroupnames');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-n', '-n');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'notificationtype','notificationtype');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-i', '-i');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hostaddress', 'hostaddress');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-o', '-o');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hostoutput', 'hostoutput');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-t', '-t');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'shortdatetime', 'shortdatetime');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-u', '-u');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'hostnotificationid', 'hostnotificationid');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-A', '-A');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'notificationauthoralias', 'notificationauthoralias');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-C', '-C');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'notificationcomment', 'notificationcomment');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'-R', '-R');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Host')  ,'notificationrecipients', 'notificationrecipients');		


INSERT INTO Action (ActionTypeID,Name,Description) values((SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),'Noma Notify For Service','Invoke a script for the selected message');
INSERT INTO ActionProperty (ActionID, Name, Value) VALUES( (SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'), 'Script', '/usr/local/groundwork/noma/notifier/alert_via_noma.pl');
INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'), (SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service'));
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-c', '-c');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'notifyType','notifyType');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-s', '-s');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'servicestate','servicestate');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-H', '-H');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'hostname', 'hostname');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-G', '-G');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'hostgroupnames','hostgroupnames');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-E', '-E');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'servicegroupnames', 'servicegroupnames');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-S', '-S');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'servicedescription','servicedescription');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-o', '-o');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'serviceoutput', 'serviceoutput');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-n', '-n');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'notificationtype', 'notificationtype');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-a', '-a');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'hostalias', 'hostalias');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-i', '-i');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'hostaddress', 'hostaddress');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-t', '-t');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'shortdatetime', 'shortdatetime');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-u', '-u');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'servicenotificationid', 'servicenotificationid');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-A', '-A');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'notificationauthoralias', 'notificationauthoralias');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-C', '-C');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'notificationcomment', 'notificationcomment');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'-R', '-R');
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = 'Noma Notify For Service')  ,'notificationrecipients', 'notificationrecipients');

INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('CHRHEV', 'CHRHEV', 'Cloud Hub for Red Hat Virtualization', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('ARCHIVE', 'ARCHIVE', 'Archiving related messages', 'Device;Host');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('SEL', 'SEL', 'Groundwork Selenium Agent Connector', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('OS', 'OS', 'Cloud Hub for Open Stack Virtualization', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('SHIFT', 'SHIFT', 'Cloud Hub for OpenSHIFT Virtualization', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('DOCK', 'DOCK', 'Cloud Hub for Docker Containers', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('CISCO', 'CISCO', 'Net Hub for CISCO ACI', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('NSX', 'NSX', 'Net Hub for VMWare NSX', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('ODL', 'ODL', 'Net Hub for Open Daylight SDN', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('AWS', 'AWS', 'Cloud Hub for Amazon Web Services', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('NETAPP', 'NETAPP', 'Cloud Hub for NetApp storage appliance', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('ICINGA2', 'ICINGA2', 'Cloud Hub for Icinga2 Monitoring', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('CLOUDERA', 'CLOUDERA', 'Cloud Hub for Cloudera Monitoring', 'Device;Host;ServiceDescription');
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('AZURE', 'AZURE', 'Azure for Cloudera Monitoring', 'Device;Host;ServiceDescription');

-- Audit trail

INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('AUDIT', 'AUDIT', 'Audit Events from all SubSystems', 'Device;Host');



-- Application Type for Business Service Monitoring (BSM)

INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('BSM', 'BSM', 'Business Service Monitoring', 'Device;Host');

-- Application Type for Cacti (CACTI)

INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('CACTI', 'CACTI', 'CACTI Events from all SubSystems', 'Device;Host;ServiceDescription');

-- Application Type for NeDi (NEDI)
INSERT INTO ApplicationType(Name, DisplayName, Description, StateTransitionCriteria) VALUES ('NEDI', 'NEDI', 'NeDi Application Feed', 'Device;Host');

-- Preliminary Consolidation Criteria for NeDi.  If we had lots of stuff to set up for NeDi, this
-- would go into a separate nedi-properties.sql file with that stuff, and we would have the BitRock
-- installer invoke that file at install time.  But we don't, so it's easiest to just add it here.
INSERT INTO ConsolidationCriteria (Name, Criteria) VALUES ('NEDIEVENT', 'Device;MonitorStatus;OperationStatus;TextMessage');

