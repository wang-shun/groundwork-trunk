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

-- populate State tables
-- JIRA 8890
INSERT INTO MonitorStatus(Name, Description)  VALUES ('UP', 'UP');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('OK', 'OK');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('UNKNOWN', 'UNKNOWN');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('SCHEDULED CRITICAL', 'SCHEDULED CRITICAL' );
INSERT INTO MonitorStatus(Name, Description)  VALUES ('PENDING', 'PENDING');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('SCHEDULED DOWN', 'SCHEDULED DOWN');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('UNREACHABLE', 'UNREACHABLE');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('UNSCHEDULED DOWN', 'UNSCHEDULED DOWN');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('WARNING', 'WARNING');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('UNSCHEDULED CRITICAL', 'UNSCHEDULED CRITICAL');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (WARNING)', 'ACKNOWLEDGEMENT (WARNING)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (CRITICAL)', 'ACKNOWLEDGEMENT (CRITICAL)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (DOWN)', 'ACKNOWLEDGEMENT (DOWN)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (UP)', 'ACKNOWLEDGEMENT (UP)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (OK)', 'ACKNOWLEDGEMENT (OK)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (UNREACHABLE)', 'ACKNOWLEDGEMENT (UNREACHABLE)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (UNKNOWN)', 'ACKNOWLEDGEMENT (UNKNOWN)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (PENDING)', 'ACKNOWLEDGEMENT (PENDING)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('ACKNOWLEDGEMENT (MAINTENANCE)', 'ACKNOWLEDGEMENT (MAINTENANCE)');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('CRITICAL', 'CRITICAL');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('DOWN', 'DOWN');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('MAINTENANCE', 'MAINTENANCE');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('SUSPENDED', 'Virtual Environment specific Host status');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('START DOWNTIME', 'START DOWNTIME');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('IN DOWNTIME', 'IN DOWNTIME');
INSERT INTO MonitorStatus(Name, Description)  VALUES ('END DOWNTIME', 'END DOWNTIME');

INSERT INTO StateType (Name, Description) VALUES('SOFT',  'State Soft');
INSERT INTO StateType (Name, Description) VALUES('HARD',  'State Hard');
INSERT INTO StateType (Name, Description) VALUES('UNKNOWN',  'State UNKNOWN');

INSERT INTO CheckType (Name, Description) VALUES('ACTIVE',  'Active Check');
INSERT INTO CheckType (Name, Description) VALUES('PASSIVE',  'Passive Check');



INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (WARNING)',  'ACKNOWLEDGEMENT (WARNING)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (CRITICAL)',  'ACKNOWLEDGEMENT (CRITICAL)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (DOWN)',  'ACKNOWLEDGEMENT (DOWN)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (UP)',  'ACKNOWLEDGEMENT (UP)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (OK)',  'ACKNOWLEDGEMENT (OK)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (UNREACHABLE)',  'ACKNOWLEDGEMENT (UNREACHABLE)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (UNKNOWN)',  'ACKNOWLEDGEMENT (UNKNOWN)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (PENDING)',  'ACKNOWLEDGEMENT (PENDING)');
INSERT INTO Severity (Name, Description) VALUES( 'ACKNOWLEDGEMENT (MAINTENANCE)',  'ACKNOWLEDGEMENT (MAINTENANCE)');
