-- $Id: $
-- Copyright (C) 2004-2011  GroundWork Open Source, Inc. (www.groundworkopensource.com)
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
-- This sql should be called at the end. This script contains dynamic properties for other application types like VEMA,GDMA etc

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'VEMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'VEMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='VEMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'GDMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'GDMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='GDMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='GDMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'NOMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'NOMA'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NOMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='NOMA'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'CHRHEV'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'CHRHEV'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CHRHEV'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'OS'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'OS'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='OS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='OS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'ARCHIVE'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'ARCHIVE'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ARCHIVE'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ARCHIVE'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'BSM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'BSM'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='BSM'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='BSM'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);


INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SEL'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'SEL'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SEL'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SEL'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'AUDIT'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'AUDIT'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AUDIT'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AUDIT'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'CACTI'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'CACTI'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CACTI'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='CACTI'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'AWS'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'AWS'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AWS'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='AWS'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'DOCK'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'DOCK'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='DOCK'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='DOCK'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'ODL'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'Comments'), 1);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType where Name = 'ODL'), (SELECT EntityTypeID FROM EntityType WHERE Name = 'LOG_MESSAGE') , (SELECT PropertyTypeID FROM PropertyType WHERE Name = 'AcknowledgedBy'), 2);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ODL'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='ODL'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'isAcknowledged'), 90);
