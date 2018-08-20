-- #$Id: $
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

-- Metadata for Sample JMX application used for testing

INSERT INTO ApplicationType(Name, Description) VALUES ("SAMPLE_JMX", "Application monitored through JMX");

-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isString)  VALUES (35, "JmxString1", "sample string1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isDate)    VALUES (36, "JmxDate1", "sample date1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isBoolean) VALUES (37, "JmxBoolean1", "sample boolean1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isInteger) VALUES (38, "JmxInteger1", "sample integer1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isLong)    VALUES (39, "JmxLong1", "sample long1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isDouble)  VALUES (40, "JmxDouble1", "sample double1 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isString)  VALUES (41, "JmxString2", "sample string2 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isDate)    VALUES (42, "JmxDate2", "sample date2 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isBoolean) VALUES (43, "JmxBoolean2", "sample boolean2 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isInteger) VALUES (44, "JmxInteger2", "sample integer2 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isLong)    VALUES (45, "JmxLong2", "sample long2 property", 1);
-- INSERT INTO PropertyType(PropertyTypeID, Name, Description, isDouble)  VALUES (46, "JmxDouble2", "sample double2 property", 1);

INSERT INTO PropertyType(Name, Description, isString)  VALUES ("JmxString1", "sample string1 property", 1);
INSERT INTO PropertyType(Name, Description, isDate)    VALUES ("JmxDate1", "sample date1 property", 1);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ("JmxBoolean1", "sample boolean1 property", 1);
INSERT INTO PropertyType(Name, Description, isInteger) VALUES ("JmxInteger1", "sample integer1 property", 1);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ("JmxLong1", "sample long1 property", 1);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ("JmxDouble1", "sample double1 property", 1);
INSERT INTO PropertyType(Name, Description, isString)  VALUES ("JmxString2", "sample string2 property", 1);
INSERT INTO PropertyType(Name, Description, isDate)    VALUES ("JmxDate2", "sample date2 property", 1);
INSERT INTO PropertyType(Name, Description, isBoolean) VALUES ("JmxBoolean2", "sample boolean2 property", 1);
INSERT INTO PropertyType(Name, Description, isInteger) VALUES ("JmxInteger2", "sample integer2 property", 1);
INSERT INTO PropertyType(Name, Description, isLong)    VALUES ("JmxLong2", "sample long2 property", 1);
INSERT INTO PropertyType(Name, Description, isDouble)  VALUES ("JmxDouble2", "sample double2 property", 1);

-- define properties of HostStatus for sample JMX application

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxString1'),70);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDate1'),71);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxBoolean1'),72);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxInteger1'),73);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxLong1'),74);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDouble1'),75);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxString2'),76);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDate2'),77);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxBoolean2'),78);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='HOST_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = '30DayMovingAvg'),79);

-- define properties of ServiceStatus for sample JMX application

INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxInteger1'),90);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxLong1'),91);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDouble1'),92);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxString2'),93);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDate2'),94);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxBoolean2'),95);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxInteger2'),96);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxLong2'),97);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = 'JmxDouble2'),98);
INSERT INTO ApplicationEntityProperty(ApplicationTypeID, EntityTypeID, PropertyTypeID, SortOrder) VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name='SAMPLE_JMX'),(SELECT EntityTypeID FROM EntityType WHERE Name='SERVICE_STATUS'),(SELECT PropertyTypeID FROM PropertyType WHERE Name = '30DayMovingAvg'),99);
