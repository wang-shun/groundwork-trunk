---- $id:$
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
-- 

-- populate LogEvent Console tables

INSERT INTO Priority (Name, Description) VALUES('1',  'Lowest Priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('2',  'Low priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('3',  'Low priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('4',  'Low priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('5',  'Medium priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('6',  'Medium priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('7',  'Medium-High priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('8',  'Medium-High priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('9',  'High priority in a scale from 1 -10');
INSERT INTO Priority (Name, Description) VALUES('10',  'Highest priority in a scale from 1 -10');

INSERT INTO Component (Name, Description) VALUES( 'SNMP', 'SNMP Component');
INSERT INTO Component (Name, Description) VALUES( 'MQ', 'MessageQueue component');
INSERT INTO Component (Name, Description) VALUES( 'JMSLISTENER', 'JMSListener component');
INSERT INTO Component (Name, Description) VALUES( 'UNDEFINED', 'Undefined component');

INSERT INTO Severity (Name, Description) VALUES( 'FATAL',  'Severity FATAL');
INSERT INTO Severity (Name, Description) VALUES( 'HIGH', 'Severity HIGH');
INSERT INTO Severity (Name, Description) VALUES( 'LOW', 'Severity LOW');
INSERT INTO Severity (Name, Description) VALUES( 'WARNING', 'Severity WARNING');
INSERT INTO Severity (Name, Description) VALUES( 'PERFORMANCE',  'Severity PERFORMANCE');
INSERT INTO Severity (Name, Description) VALUES( 'STATISTIC',  'Severity STATISTIC');
INSERT INTO Severity (Name, Description) VALUES('SERIOUS', 'Severity SERIOUS');
INSERT INTO Severity (Name, Description) VALUES( 'CRITICAL',  'GroundWork Severity CRITICAL. Also MIB standard');
INSERT INTO Severity (Name, Description) VALUES( 'OK',  'GroundWork Severity OK');
INSERT INTO Severity (Name, Description) VALUES('UNKNOWN', 'GroundWork Severity UNKNOWN');
INSERT INTO Severity (Name, Description) VALUES('NORMAL', 'Standard MIB type for Severity');
INSERT INTO Severity (Name, Description) VALUES('MAJOR', 'Standard MIB type for MonitorStatus');
INSERT INTO Severity (Name, Description) VALUES('MINOR', 'Standard MIB type for MonitorStatus');
INSERT INTO Severity (Name, Description) VALUES('INFORMATIONAL', 'Standard MIB type');
INSERT INTO Severity (Name, Description) VALUES('UP', 'Severity UP');
INSERT INTO Severity (Name, Description) VALUES('DOWN', 'Severity DOWN');
INSERT INTO Severity (Name, Description) VALUES('UNREACHABLE', 'Severity unreachable');



INSERT INTO TypeRule (Name, Description) VALUES( 'NETWORK', 'Network');
INSERT INTO TypeRule (Name, Description) VALUES( 'HARDWARE', 'Hardware');
INSERT INTO TypeRule (Name, Description) VALUES( 'SERVICE', 'Service');
INSERT INTO TypeRule (Name, Description) VALUES( 'APPLICATION', 'Application');
INSERT INTO TypeRule (Name, Description) VALUES( 'FILTERED', 'Message filtered by GroundWork-Bridge');
INSERT INTO TypeRule (Name, Description) VALUES( 'UNDEFINED', 'Undefined type');
INSERT INTO TypeRule (Name, Description) VALUES( 'NAGIOS_LOG', 'NAGIOS_LOG type');
INSERT INTO TypeRule (Name, Description) VALUES( 'ACKNOWLEDGE', 'ACKNOWLEDGE type');
INSERT INTO TypeRule (Name, Description) VALUES( 'UNACKNOWLEDGE', 'UNACKNOWLEDGE type');


INSERT INTO OperationStatus (Name, Description) VALUES( 'OPEN',  'Status OPEN');
INSERT INTO OperationStatus (Name, Description) VALUES( 'CLOSED',  'Status CLOSED');
INSERT INTO OperationStatus (Name, Description) VALUES( 'NOTIFIED',  'Status NOTIFIED');
INSERT INTO OperationStatus (Name, Description) VALUES( 'ACCEPTED',  'Status accepted');
INSERT INTO OperationStatus (Name, Description) VALUES('ACKNOWLEDGED','Status Acknowledged');
