-- This script creates a "ganglia" user, a "ganglia" database, and associated
-- top-level objects from scratch, without populating the database with tables.
--
-- WARNING:  This will destroy any existing database and user, and you will lose
-- all your existing settings.  If you don't want that, don't run this script!

-- Copyright (C) 2004-2012  GroundWork Inc. info@groundworkopensource.com
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

-- To get a DROP USER to function effectively, we need REVOKE commands
-- to destroy any privileges previously granted to the user, before we
-- drop that user.  Therefore, we ought to apply an IF EXISTS clause
-- to the REVOKE commands in addition to applying it to the DROP USER
-- commands, but no such clause exists in the REVOKE syntax.

DROP DATABASE IF EXISTS ganglia;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public from ganglia;
DROP USER IF EXISTS ganglia;
CREATE USER ganglia WITH PASSWORD 'gwrk';
CREATE DATABASE ganglia ENCODING='LATIN1' OWNER=ganglia;

-- set permissions
GRANT ALL PRIVILEGES ON DATABASE ganglia to ganglia;

