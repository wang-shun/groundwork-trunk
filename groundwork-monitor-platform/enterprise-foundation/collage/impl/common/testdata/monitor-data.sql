--$Id: monitor-data.sql 6700 2007-05-31 20:17:38Z glee $
-- Monitoring test data 
-- Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com
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


\c gwtest;

--Reset AuditLog

delete FROM AuditLog;

ALTER SEQUENCE auditlog_auditlogid_seq RESTART WITH 1;

--Populate some MonitorServers

delete from DeviceParent;
delete FROM MonitorList;
delete FROM MonitorServer;
delete FROM LogMessage;
delete FROM HostGroup;
delete FROM Host;
delete FROM HostIdentity;
delete FROM EntityProperty;
delete FROM Device;
delete FROM ConsolidationCriteria;
delete FROM Category;

ALTER SEQUENCE device_deviceid_seq RESTART WITH 1;
ALTER SEQUENCE monitorserver_monitorserverid_seq RESTART WITH 1;


INSERT INTO MonitorServer(MonitorServerID,MonitorServerName,IP, Description) VALUES(default,'groundwork-monitor1', '192.168.1.251', 'Test Monitor 1');
INSERT INTO MonitorServer(MonitorServerID,MonitorServerName,IP, Description) VALUES(default,'groundwork-monitor2', '192.168.1.252', 'Test Monitor 2');
INSERT INTO MonitorServer(MonitorServerID,MonitorServerName,IP, Description) VALUES(default,'groundwork-monitor3', '192.168.1.253', 'Test Monitor 3');

-- Populate some devices

INSERT INTO Device(DeviceID,DisplayName,Identification, Description) VALUES(default,'groundwork', '192.168.1.100', 'Nagios Server');
INSERT INTO Device(DeviceID,DisplayName,Identification, Description) VALUES(default,'ex-svr-1', '192.168.1.101', 'Exchange Server');
INSERT INTO Device(DeviceID,DisplayName,Identification, Description) VALUES(default,'mysql-svr-1', '192.168.1.102', 'Database backend');
INSERT INTO Device(DeviceID,DisplayName,Identification, Description) VALUES(default,'app-svr-1', '192.168.1.103', 'Application server');
INSERT INTO Device(DeviceID,DisplayName,Identification, Description) VALUES(default,'app-svr-2', '192.168.1.104', 'Application Server');

INSERT INTO ConsolidationCriteria(Name,Criteria) VALUES('SYSTEM', 'OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage');
INSERT INTO ConsolidationCriteria(Name,Criteria) VALUES('NAGIOSEVENT', 'Device;MonitorStatus;OperationStatus;SubComponent');

INSERT INTO Category(Name, Description,EntityTypeID) VALUES('Category1', 'First test category',(SELECT EntityTypeID FROM EntityType WHERE Name='HOSTGROUP'));
INSERT INTO Category(Name, Description,EntityTypeID) VALUES('Category2', 'Second test category',(SELECT EntityTypeID FROM EntityType WHERE Name='TYPE_RULE'));

INSERT INTO EntityProperty(EntityTypeID, ObjectID, PropertyTypeID, ValueString, CreatedOn) 
VALUES ((SELECT EntityTypeID FROM EntityType WHERE Name='DEVICE'), (SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='Location'), 'Bay Area', NOW());

INSERT INTO EntityProperty(EntityTypeID, ObjectID, PropertyTypeID, ValueString, CreatedOn) 
VALUES ((SELECT EntityTypeID FROM EntityType WHERE Name='DEVICE'), (SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ContactPerson'), 'Roger Ruttimann', NOW());

INSERT INTO EntityProperty(EntityTypeID, ObjectID, PropertyTypeID, ValueString, CreatedOn) 
VALUES ((SELECT EntityTypeID FROM EntityType WHERE Name='DEVICE'), (SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ContactNumber'), '510.899.7700', NOW());

-- Assign Devices to MonitorServers

INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(1,1);
INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(1,2);
INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(1,3);
INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(1,4);
INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(2,3);
INSERT INTO MonitorList(MonitorServerID, DeviceID) VALUES(2,5);


-- Create device parent/child relationships

INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(1,3);
INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(2,3);
INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(4,3);
INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(5,3);

INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(1,2);
INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(1,4);
INSERT INTO DeviceParent(DeviceID, ParentID) VALUES(1,5);

-- Create some hosts

INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), 'nagios', 'Nagios Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.101'), 'exchange', 'exchange Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.102'), 'db-svr', 'Database Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.103'), 'app-svr-tomcat', 'tomcat Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.103'), 'gwrk-allApps', 'app Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.102'), 'gwrk-allSites', 'site Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.101'), 'gwrk-ITServices', 'IT Server');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), 'gwrk-organizations', 'Organizations');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.101'), 'gwrk-wan', 'WAN');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.102'), 'gwrk-storage', 'Storage');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.103'), 'gwrk-fnp', 'File and Print');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.104'), 'gwrk-email', 'Email');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.103'), 'gwrk-emailAtlanta', 'Email Atlanta');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.102'), 'gwrk-emailNY', 'Email NY');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.101'), 'gwrk-emailMiami', 'Email Miami');
INSERT INTO Host(ApplicationTypeID,DeviceID, HostName, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),(SELECT DeviceID FROM Device WHERE Identification='192.168.1.100'), 'gwrk-emailMinneapolis', 'Email Minneapolis');
 
-- Host Status

INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='nagios'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='exchange'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='db-svr'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()) ;
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-allApps'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-allSites'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-ITServices'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-organizations'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-wan'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-storage'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-fnp'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-email'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 
INSERT INTO HostStatus(HostStatusID, ApplicationTypeID, MonitorStatusID, LastCheckTime) VALUES( (SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'),(SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT MonitorStatusID FROM MonitorStatus WHERE NAME='UP' ), NOW()); 

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-18 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-18 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='exchange'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-18 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='exchange'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-18 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='db-svr'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-20 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='db-svr'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-20 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-21 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-21 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-allApps'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-21 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-allApps'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-21 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-allSites'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-22 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-allSites'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-22 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-ITServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-22 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-ITServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-22 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-22 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-22 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-23 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-23 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-23 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'),'2006-11-23 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-email'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-email'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastStateChange'), '2006-11-25 00:00:00', NOW());
INSERT INTO HostStatusProperty(HostStatusID, PropertyTypeID, ValueDate, CreatedOn) VALUES ((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), '2006-11-25 00:00:00', NOW());

-- Service Status


INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'local_disk',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='nagios'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'local_procs',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='WARNING'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='WARNING'),(SELECT HostID FROM Host WHERE HostName='nagios'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'local_users',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='nagios'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'network_users',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='nagios'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'network_users',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='exchange'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'network_users',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'apps2',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-allApps'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'site',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-allSites'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'itServices',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-ITServices'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'organizations',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNKNOWN'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-organizations'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'wan',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-wan'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'storage',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-storage'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'fnp',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'email atlanta',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='UNKNOWN'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'email ny',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'email miami',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), NOW(), NOW(), NOW());

INSERT INTO ServiceStatus(ApplicationTypeID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, HostID, LastStateChange, NextCheckTime, LastCheckTime)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), 'email minn',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='CRITICAL'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'),(SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), NOW(), NOW(), NOW());


/* ServiceStatus using Property tables */


-- INSERT INTO ServiceStatus(ApplicationTypeID, HostID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, LastNotificationTime, LastStateChange, NextCheckTime, LastCheckTime) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'), (SELECT HostID FROM Host WHERE HostName='nagios'), 'local_disk2',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), NOW(), NOW(), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND ServiceDescription ='local_disk' AND h.HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'DISK OK - free space: / 12524 MB (34%)', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND ServiceDescription ='local_disk' AND h.HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND ServiceDescription ='local_disk' AND h.HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND ServiceDescription ='local_disk' AND h.HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 100.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND ServiceDescription ='local_disk' AND h.HostName='nagios'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_procs'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'System call sent warnings to stderr', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_procs'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_procs'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_procs'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 100.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_procs'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 100.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allApps' AND ServiceDescription='apps2'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allApps' AND ServiceDescription='apps2'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allApps' AND ServiceDescription='apps2'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allApps' AND ServiceDescription='apps2'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.5, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allApps' AND ServiceDescription='apps2'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allSites' AND ServiceDescription='site'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allSites' AND ServiceDescription='site'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allSites' AND ServiceDescription='site'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allSites' AND ServiceDescription='site'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.95, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-allSites' AND ServiceDescription='site'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-ITServices' AND ServiceDescription='itServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-ITServices' AND ServiceDescription='itServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-ITServices' AND ServiceDescription='itServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-ITServices' AND ServiceDescription='itServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-ITServices' AND ServiceDescription='itServices'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-organizations' AND ServiceDescription='organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-organizations' AND ServiceDescription='organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-organizations' AND ServiceDescription='organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-organizations' AND ServiceDescription='organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 97.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-organizations' AND ServiceDescription='organizations'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-wan' AND ServiceDescription='wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-wan' AND ServiceDescription='wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-wan' AND ServiceDescription='wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-wan' AND ServiceDescription='wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 98.80, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-wan' AND ServiceDescription='wan'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-storage' AND ServiceDescription='storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-storage' AND ServiceDescription='storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-storage' AND ServiceDescription='storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-storage' AND ServiceDescription='storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.80, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-storage' AND ServiceDescription='storage'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-fnp' AND ServiceDescription='fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-fnp' AND ServiceDescription='fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-fnp' AND ServiceDescription='fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-fnp' AND ServiceDescription='fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.90, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-fnp' AND ServiceDescription='fnp'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailAtlanta' AND ServiceDescription='email atlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailAtlanta' AND ServiceDescription='email atlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 14420000, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailAtlanta' AND ServiceDescription='email atlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), false, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailAtlanta' AND ServiceDescription='email atlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 99.90, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailAtlanta' AND ServiceDescription='email atlanta'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailNY' AND ServiceDescription='email ny'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailNY' AND ServiceDescription='email ny'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailNY' AND ServiceDescription='email ny'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailNY' AND ServiceDescription='email ny'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 97.90, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailNY' AND ServiceDescription='email ny'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMiami' AND ServiceDescription='email miami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMiami' AND ServiceDescription='email miami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMiami' AND ServiceDescription='email miami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), true, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMiami' AND ServiceDescription='email miami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 98.90, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMiami' AND ServiceDescription='email miami'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());

INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastPluginOutput'), 'USERS OK - 2 users currently logged in', NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeUnknown'), 0, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='TimeCritical'), 4000000, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='isProblemAcknowledged'), false, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='30DayMovingAvg'), 97.00, NOW());
INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='gwrk-emailMinneapolis' AND ServiceDescription='email minn'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LastNotificationTime'), NOW(), NOW());


-- Create ServiceStatus for Sample JMX Application

-- INSERT INTO ServiceStatus(ApplicationTypeID, HostID, ServiceDescription, MonitorStatusID, StateTypeID, CheckTypeID, LastHardStateID, LastStateChange, NextCheckTime, LastCheckTime) 
-- VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SYSTEM'), (SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'), 'Runtime Attributes',(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), (SELECT StateTypeID FROM StateType WHERE Name='HARD'),(SELECT CheckTypeID FROM CheckType WHERE Name='ACTIVE'),(SELECT MonitorStatusID FROM MonitorStatus WHERE Name='OK'), NOW(), NOW(), NOW());

-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueInteger, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxInteger1'), 99, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxLong1'), 999999999, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxDouble1'), 99.99, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueString,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxString2'), 'Jmx String Attribute', NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDate,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxDate2'), '2005-05-01', NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueBoolean, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxBoolean2'), 1, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueInteger, CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxInteger2'), 299, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueLong,    CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxLong2'), 299999999, NOW());
-- INSERT INTO ServiceStatusProperty(ServiceStatusID, PropertyTypeID, ValueDouble,  CreatedOn) VALUES ((select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='XXXX' ServiceDescription='Runtime Attributes'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='JmxDouble2'), 299.99, NOW());

-- HostGroup

INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'demo-system', 'System to demo the application');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'All_Infrastructure', 'Top Level gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'All_Applications', 'Level One gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'All_Sites', 'Level One gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'All_IT_Services', 'Level One gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'All_Organizations', 'Level One gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Wide_Area_Network', 'Level Two gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Storage', 'Level Two gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'File_And_Print', 'Level Two gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Email', 'Level Two gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Email_Atlanta', 'Level Three gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Email_New_York', 'Level Three gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Email_Miami', 'Level Three gwrk Test node');
INSERT INTO HostGroup(ApplicationTypeID,Name, Description) VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),'Email_Minneapolis', 'Level Three gwrk Test node');

--HostGroupCollection

INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='nagios'), (SELECT HostGroupID FROM HostGroup Where Name='demo-system'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='exchange'), (SELECT HostGroupID FROM HostGroup Where Name='demo-system'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='db-svr'), (SELECT HostGroupID FROM HostGroup Where Name='demo-system'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='app-svr-tomcat'), (SELECT HostGroupID FROM HostGroup Where Name='demo-system'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-allApps'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-allSites'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-organizations'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-wan'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-storage'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT HostGroupID FROM HostGroup Where Name='All_Infrastructure'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-allApps'), (SELECT HostGroupID FROM HostGroup Where Name='All_Applications'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-allSites'), (SELECT HostGroupID FROM HostGroup Where Name='All_Sites'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-wan'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-storage'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT HostGroupID FROM HostGroup Where Name='All_IT_Services'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-organizations'), (SELECT HostGroupID FROM HostGroup Where Name='All_Organizations'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-wan'), (SELECT HostGroupID FROM HostGroup Where Name='Wide_Area_Network'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-storage'), (SELECT HostGroupID FROM HostGroup Where Name='Storage'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-fnp'), (SELECT HostGroupID FROM HostGroup Where Name='File_And_Print'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT HostGroupID FROM HostGroup Where Name='Email'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT HostGroupID FROM HostGroup Where Name='Email'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT HostGroupID FROM HostGroup Where Name='Email'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT HostGroupID FROM HostGroup Where Name='Email'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailAtlanta'), (SELECT HostGroupID FROM HostGroup Where Name='Email_Atlanta'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailNY'), (SELECT HostGroupID FROM HostGroup Where Name='Email_New_York'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMiami'), (SELECT HostGroupID FROM HostGroup Where Name='Email_Miami'));
INSERT INTO HostGroupCollection(HostID, HostGroupID) VALUES((SELECT HostID FROM Host WHERE HostName='gwrk-emailMinneapolis'), (SELECT HostGroupID FROM HostGroup Where Name='Email_Minneapolis'));

     
INSERT INTO LogMessage(ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount, StateTransitionHash)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_disk'),1,3,3,2,2,4,5, 'message_1', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1, -12345678);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name1', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE1', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent1', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType1', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName1', NOW());

INSERT INTO LogMessage(ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount, StateTransitionHash)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_disk'),1,2,3,4,1,4,5, 'message_2', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1, -12345678);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%2%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name2', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%2%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE2', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%2%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent2', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%2%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType2', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%2%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName2', NOW());

INSERT INTO LogMessage(ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'),1,3,3,4,2,3,5, 'message_3', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%3%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name3', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%3%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE3', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%3%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent3', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%3%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType3', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%3%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName3', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_disk'),1,3,3,4,2,3,5, 'message_4', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%4%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name4', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%4%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE4', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%4%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent4', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%4%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType4', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%4%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName4', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'),1,3,3,4,2,3,5, 'message_5', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%5%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name5', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%5%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE5', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%5%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent5', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%5%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType5', NOW());
INSERT INTO LogMessageProperty( LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%5%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName5', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='local_users'),1,3,3,4,2,3,5, 'message_6', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%6%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name6', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%6%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE6', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%6%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent6', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%6%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType6', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%6%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName6', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1, (SELECT HostID FROM Host WHERE HostName='nagios'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='network_users' ),1,3,4,2,5,4,5, 'message_7', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%7%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name7', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%7%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE7', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%7%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent7', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%7%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType7', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%7%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName7', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),1,(SELECT HostID FROM Host WHERE HostName='nagios'),(select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='nagios' AND ServiceDescription='network_users' ),1,3,6,2,3,4,5, 'message_8', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%8%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name8', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%8%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE8', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%8%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent8', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%8%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType8', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%8%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName8', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES( (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),2, (SELECT HostID FROM Host WHERE HostName='exchange'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='exchange' AND ServiceDescription='network_users' ),1,3,6,2,3,4,5, 'message_9', (NOW() - INTERVAL '60 DAYS'), '2006-11-23 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%9%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name9', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%9%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE9', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%9%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent9', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%9%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType9', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ( (SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%9%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName9', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),2, (SELECT HostID FROM Host WHERE HostName='exchange'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='exchange' AND ServiceDescription='network_users' ),1,3,6,2,3,4,5, 'message_10', (NOW() - INTERVAL '60 DAYS'), '2006-11-17 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%10%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name10', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%10%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE10', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%10%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent10', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%10%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType10', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%10%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName10', NOW());

INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'),2, (SELECT HostID FROM Host WHERE HostName='exchange'), (select ss.ServiceStatusID  from ServiceStatus ss, Host h  where h.HostID = ss.HostID AND h.HostName='exchange' AND ServiceDescription='network_users' ),1,3,6,2,3,4,5, 'message_11', (NOW() - INTERVAL '60 DAYS'), '2006-11-18 11:04:58', NOW(), 1);

INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%11%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationName'), 'App Name11', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%11%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ApplicationCode'), 'APPCODE11', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%11%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='SubComponent'), 'SubComponent11', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%11%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ErrorType'), 'ErrorType11', NOW());
INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%11%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='LoggerName'), 'LoggerName11', NOW());

-- DELETE FROM ApplicationType where Name='VMS';
-- DELETE FROM MonitorStatus where Name in ('OPEN','ACK','CLEAR');
-- DELETE FROM Severity where Name='CLEARED';
-- insert into ApplicationType (Name,Description,StateTransitionCriteria) values ( 'VMS','Comverse HUB Traps','Device;Event_OID_numeric' );
-- insert into PropertyType (Name, Description, isDate,isBoolean,isString,isInteger,isLong,isDouble, isVisible) values ('VMSEventID','Unique VMS Event Identifier, used for correlation', 0,0,1,0,0,0,1);
-- insert into PropertyType (Name, Description, isDate,isBoolean,isString,isInteger,isLong,isDouble, isVisible) values ('ObjectClass','VMS Managed Object Class',0,0,1,0,0,0,1);
-- insert into PropertyType (Name, Description, isDate,isBoolean,isString,isInteger,isLong,isDouble, isVisible) values ('AdditionalInfo', 'VMS Additional Info',0,0,1,0,0,0,1);
-- insert into  ConsolidationCriteria (Name,Criteria) values ('VMS','Device;MonitorStatus;VMSEventID');
-- insert into MonitorStatus (Name,Description) values ('OPEN', 'OPEN');
-- insert into MonitorStatus (Name,Description) values ('ACK', 'ACK');
-- insert into MonitorStatus (Name,Description) values ('CLEAR', 'CLEAR');
-- insert into Severity (Name,Description) values ('CLEARED', 'CLEARED');
-- insert into ApplicationEntityProperty (ApplicationTypeID,EntityTypeID,PropertyTypeID,SortOrder) values ((select ApplicationTypeID from ApplicationType where Name='VMS'), (select EntityTypeID from EntityType where Name='LOG_MESSAGE'),(select PropertyTypeID from PropertyType where Name='VMSEventID'),1);
-- insert into ApplicationEntityProperty (ApplicationTypeID,EntityTypeID,PropertyTypeID,SortOrder) values ((select ApplicationTypeID from ApplicationType where Name='VMS'), (select EntityTypeID from EntityType where Name='LOG_MESSAGE'),(select PropertyTypeID from PropertyType where Name='ObjectClass'),2);
-- insert into ApplicationEntityProperty (ApplicationTypeID,EntityTypeID,PropertyTypeID,SortOrder) values ((select ApplicationTypeID from ApplicationType where Name='VMS'), (select EntityTypeID from EntityType where Name='LOG_MESSAGE'),(select PropertyTypeID from PropertyType where Name='AdditionalInfo'),3);
-- insert into ApplicationEntityProperty (ApplicationTypeID,EntityTypeID,PropertyTypeID,SortOrder) values ((select ApplicationTypeID from ApplicationType where Name='VMS'), (select EntityTypeID from EntityType where Name='LOG_MESSAGE'),(select PropertyTypeID from PropertyType where Name='AcknowledgedBy'),4);

-- INSERT INTO LogMessage( ApplicationTypeID, DeviceID, HostStatusID, ServiceStatusID, SeverityID, ApplicationSeverityID, PriorityID, ComponentID, MonitorStatusID, OperationStatusID, TypeRuleID, TextMessage, FirstInsertDate, LastInsertDate, ReportDate, MsgCount)
-- VALUES((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VMS'),1, null,null,17,17,1,4,20,1,5, 'VMS_message_1', (NOW() - INTERVAL '60 DAYS'), '2011-03-23 11:04:58', NOW(), 1);

--INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%VMS_message_1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='VMSEventID'), '1234', NOW());
--INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%VMS_message_1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='ObjectClass'), 'CMS', NOW());
--INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%VMS_message_1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='AdditionalInfo'), 'test', NOW());
--INSERT INTO LogMessageProperty(LogMessageID, PropertyTypeID, ValueString, CreatedOn) VALUES ((SELECT LogMessageID FROM LogMessage WHERE TextMessage LIKE '%VMS_message_1%'), (SELECT PropertyTypeID FROM PropertyType WHERE Name='AcknowledgedBy'), 'user', NOW());

INSERT INTO Category(Name, EntityTypeID, ApplicationTypeID) VALUES('SG1',(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_GROUP'), (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'));
INSERT INTO Category(Name, EntityTypeID, ApplicationTypeID) VALUES('SG2',(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_GROUP'), (SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NAGIOS'));

INSERT INTO CategoryEntity(ObjectID, CategoryID, EntityTypeID) VALUES((SELECT ServiceStatusID FROM ServiceStatus WHERE ServiceDescription='local_disk' AND HostID=(SELECT HostID FROM Host WHERE HostName='nagios')),(SELECT CategoryID FROM Category WHERE Name='SG1'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'));
INSERT INTO CategoryEntity(ObjectID, CategoryID, EntityTypeID) VALUES((SELECT ServiceStatusID FROM ServiceStatus WHERE ServiceDescription='local_procs' AND HostID=(SELECT HostID FROM Host WHERE HostName='nagios')),(SELECT CategoryID FROM Category WHERE Name='SG1'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'));

INSERT INTO Category(Name, Root, EntityTypeID) VALUES('CG1',true,(SELECT EntityTypeID FROM EntityType WHERE Name='CUSTOM_GROUP'));
INSERT INTO Category(Name, Root, EntityTypeID) VALUES('CG2',false,(SELECT EntityTypeID FROM EntityType WHERE Name='CUSTOM_GROUP'));
INSERT INTO Category(Name, Root, EntityTypeID) VALUES('CG3',false,(SELECT EntityTypeID FROM EntityType WHERE Name='CUSTOM_GROUP'));
INSERT INTO CategoryEntity(ObjectID, CategoryID, EntityTypeID) VALUES((SELECT HostGroupID FROM HostGroup WHERE Name='All_Infrastructure'),(SELECT CategoryID FROM Category WHERE Name='CG2'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOSTGROUP'));
INSERT INTO CategoryEntity(ObjectID, CategoryID, EntityTypeID) VALUES((SELECT HostGroupID FROM HostGroup WHERE Name='All_Applications'),(SELECT CategoryID FROM Category WHERE Name='CG2'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOSTGROUP'));
INSERT INTO CategoryEntity(ObjectID, CategoryID, EntityTypeID) VALUES((SELECT CategoryID FROM Category WHERE Name='SG1'),(SELECT CategoryID FROM Category WHERE Name='CG3'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_GROUP'));
INSERT INTO CategoryHierarchy(ParentID, CategoryID) VALUES((SELECT CategoryID FROM Category WHERE Name='CG1'),(SELECT CategoryID FROM Category WHERE Name='CG2'));
INSERT INTO CategoryHierarchy(ParentID, CategoryID) VALUES((SELECT CategoryID FROM Category WHERE Name='CG1'),(SELECT CategoryID FROM Category WHERE Name='CG3'));

