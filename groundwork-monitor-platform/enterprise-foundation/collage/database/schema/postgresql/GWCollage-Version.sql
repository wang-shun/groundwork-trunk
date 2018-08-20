---- $id:$
-- Copyright (C) 2004-2017 GroundWork Open Source, Inc. (www.groundworkopensource.com)
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

-- Version Info
INSERT INTO SchemaInfo (Name, Value) VALUES ('Schema Version', '${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}');
INSERT INTO SchemaInfo (Name, Value) VALUES ('Schema created', date_trunc('second', now()));
INSERT INTO schemainfo (name, Value) VALUES ('CurrentSchemaVersion','${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}');

-- Other Settings
INSERT INTO SchemaInfo (Name, Value) VALUES ('AvailabilityUpdateInterval', '60');
INSERT INTO SchemaInfo (Name, Value) VALUES ('AvailabilityDataPoints', '720');

