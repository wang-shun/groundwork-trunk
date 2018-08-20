--Collage - The ultimate data integration framework.
--Copyright (c) 2004-2011  GroundWork Open Source (www.groundworkopensource.com)
--
--    This program is free software; you can redistribute it and/or modify
--    it under the terms of version 2 of the GNU General Public License 
--    as published by the Free Software Foundation.
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
--$Id: create-production-db.sql 15497 2011-11-22 18:43:15Z gherteg $

-- To get a DROP USER to function effectively, we need REVOKE commands
-- to destroy any privileges previously granted to the user, before we
-- drop that user.  Therefore, we ought to apply an IF EXISTS clause
-- to the REVOKE commands in addition to applying it to the DROP USER
-- commands, but no such clause exists in the REVOKE syntax.

DROP DATABASE IF EXISTS gwcollagedb;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public from collage;
DROP USER IF EXISTS collage;
CREATE USER collage WITH PASSWORD 'gwrk';
CREATE DATABASE gwcollagedb ENCODING='LATIN1' OWNER=collage;

-- set permissions

GRANT ALL PRIVILEGES ON DATABASE gwcollagedb to collage;
--flush privileges;
-- jboss databaase creation was moved to create-fresh-jboss-db.sql


-- Dashboards

DROP DATABASE IF EXISTS dashboard;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public from ir;
DROP USER IF EXISTS ir;
CREATE USER ir WITH PASSWORD 'gwrk';
CREATE DATABASE dashboard ENCODING='LATIN1' OWNER=ir;
GRANT ALL PRIVILEGES ON DATABASE dashboard to ir;
