-- $Id: $
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

-- Metadata for System entities such as Devices and MonitorServers

INSERT INTO PropertyType(Name, Description, isString)  VALUES ('Location', 'Last output received', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('ContactPerson', 'Last output received', true);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ('ContactNumber', 'Last output received', true);

-- define Device properties

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) 
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'DEVICE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Location'), 1); 

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'DEVICE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ContactPerson'), 2); 

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'DEVICE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'ContactNumber'), 3); 

INSERT INTO ConsolidationCriteria (Name,Criteria) values ('SYSTEM','OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage');
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) 
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) 
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 2);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) 
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SYSTEM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 3);
