-- $Id: nagios-properties.sql 18070 2010-09-30 17:06:50Z rruttimann $
-- Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com
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

-- Metadata for Nagios 1.2/2.0 Feeders

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('LastPluginOutput', 'Last output received', true);
INSERT INTO PropertyType(Name, Description, isDate)    VALUES ('LastStateChange', 'The time of the last change of state', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isAcknowledged', 'Has the current state been acknowledged?', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeUp', 'The amount of time that the host has been UP', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeDown', 'The amount of time that the host has been DOWN', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeUnreachable', 'The amount of time that the host has been UNREACHABLE', true);
INSERT INTO PropertyType(Name, Description, isDate)    VALUES ('LastNotificationTime', 'The time of the last notification', true);
INSERT INTO PropertyType(Name, Description, isInteger) VALUES ('CurrentNotificationNumber', 'The count of notifications', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isNotificationsEnabled', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isChecksEnabled', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isEventHandlersEnabled', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isFlapDetectionEnabled', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isHostFlapping', '', true);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ('PercentStateChange', '', true);
INSERT INTO PropertyType(Name, Description, isInteger) VALUES ('ScheduledDowntimeDepth', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isFailurePredictionEnabled', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isProcessPerformanceData', '', true);

INSERT INTO PropertyType(Name, Description, isInteger) VALUES ('RetryNumber', 'The number of times an attempt has been made to contact the service', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isAcceptPassiveChecks', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isProblemAcknowledged', '', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeOK', 'The amount of time that the entity has had a status of OK', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeUnknown', 'The amount of time that the entity has had a status of UNKNOWN', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeWarning', 'The amount of time that the entity has had a status of WARNING', true);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ('TimeCritical', 'The amount of time that the entity has had a status of CRITICAL', true);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ('Latency', '', true);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ('ExecutionTime', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isServiceFlapping', '', true);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ('isObsessOverService', '', true);

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('ApplicationName', '', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('ApplicationCode', '', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('SubComponent', '', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('LoggerName', '', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('ErrorType', '', true);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ('30DayMovingAvg', '', true);

INSERT INTO PropertyType(Name, Description, isBoolean)  VALUES ('isPassiveChecksEnabled', 'Nagios 2.0', true);

-- Acknowledgment of Events

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('AcknowledgedBy', '', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('AcknowledgeComment', '', true);

-- Properties for GroundWork Monitor 5.3 --
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('Parent', 'List of parent hosts separated by commas', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('Alias', 'Host Alias information', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('RRDPath', 'fully qualified path to RRD image', true);

-- Properties for RRD --
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('RRDLabel', 'Label for Graph', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('RRDCommand', 'Custom RRD command', true);


-- Properties added for GWMON 6.0
INSERT INTO PropertyType(Name, Description, isLong)  VALUES ('CurrentAttempt', 'Current attempt running check', true);
INSERT INTO PropertyType(Name, Description, isLong)  VALUES ('MaxAttempts', 'Max attempts configured', true);

-- For 6.0.JIRA 76
INSERT INTO PropertyType(Name, Description, isInteger) VALUES ('isObsessOverHost', '', true);
INSERT INTO PropertyType(Name, Description, isString) VALUES ('ServiceDependencies', '', true);

-- For 6.0 JIRA GWMON-5719
INSERT INTO PropertyType(Name, Description, isString) VALUES ('ExtendedInfo', '', true);

INSERT INTO PropertyType(Name, Description, isString) VALUES ('Comments', 'Host or Service Comments in XML format', true);

-- For 6.0.1 JIRA 8002
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('CactiRRDCommand', 'Cacti RRD Command', true);

-- For 6.2 JIRA 8544
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('RemoteRRDCommand', 'Remote RRD Command', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('Notes', 'Configuration Notes field', true);

-- New for cloud conector

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('DeactivationTime', 'The time when the host was deactivated',true);

-- For 6.7 a new property PerformanceData was added 

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('PerformanceData', 'The last Nagios performance data',true);


-- define properties of HostStatus for NAGIOS monitoring

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'), 1); 
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastStateChange'), 4);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 5);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeUp'), 6);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeDown'), 7);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeUnreachable'), 8);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastNotificationTime'), 9);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentNotificationNumber'),10);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isNotificationsEnabled'),11);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isChecksEnabled'),12);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isEventHandlersEnabled'),13);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isFlapDetectionEnabled'),14);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isHostFlapping'),15);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PercentStateChange'),16);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ScheduledDowntimeDepth'),17);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isFailurePredictionEnabled'),18);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isProcessPerformanceData'),19);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = '30DayMovingAvg'),20);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Latency'),20);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExecutionTime'),21);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isPassiveChecksEnabled'),22);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Parent'),23);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Alias'),24);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDPath'),25);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentAttempt'),26);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'MaxAttempts'),27);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExtendedInfo'),28);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),29);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Notes'),72);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'DeactivationTime'),74);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),75);

-- define properties of ServiceStatus for NAGIOS monitoring

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),30);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RetryNumber'),31);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isChecksEnabled'),33);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcceptPassiveChecks'),34);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isEventHandlersEnabled'),35);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isProblemAcknowledged'),37);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeOK'),39);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeUnknown'),40);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeWarning'),41);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'TimeCritical'),42);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastNotificationTime'),43);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentNotificationNumber'),44);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isNotificationsEnabled'),45);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Latency'),46);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExecutionTime'),47);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isFlapDetectionEnabled'),48);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isServiceFlapping'),49);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PercentStateChange'),50);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ScheduledDowntimeDepth'),51);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isFailurePredictionEnabled'),52);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isProcessPerformanceData'),53);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isObsessOverService'),54);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = '30DayMovingAvg'),55);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDPath'),56);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'CurrentAttempt'),57);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'MaxAttempts'),58);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ExtendedInfo'),67);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),68);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDLabel'),69);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RRDCommand'),70);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'RemoteRRDCommand'),71);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Notes'),73);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),76);

-- define properties of LogMessage for NAGIOS monitoring

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ApplicationName'),59);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ApplicationCode'),60);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'SubComponent'),61);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LoggerName'),62);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ErrorType'),63);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'),65);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgeComment'),66);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT EntityTypeID FROM EntityType WHERE Name='LOG_MESSAGE'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'),67);

-- Consolidation criteria for Nagios Events

INSERT INTO ConsolidationCriteria(Name, Criteria)  VALUES ('NAGIOSEVENT', 'Device;MonitorStatus;OperationStatus;SubComponent;ErrorType');

-- Define VEMA (Cloud Hub) properties associations for properties that are shared with NAGIOS
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'VEMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'HOST_STATUS') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'), 80); 

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),81);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 82);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),83);

-- Define Cloud Hub for Red Hat properties associations for properties that are shared with NAGIOS

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'CHRHEV'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'HOST_STATUS') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'), 85); 

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),86);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 87);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'),88);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'OS'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'HOST_STATUS') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'), 89);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='OS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'LastPluginOutput'),90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='OS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 91);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='OS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'PerformanceData'), 92);

