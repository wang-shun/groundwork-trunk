-- Copyright (C) 2004-2013  GroundWork Inc. info@groundworkopensource.com
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

-- JBoss and JBoss Portal databases for JPP 6

DROP DATABASE IF EXISTS "jboss-jcr";
DROP DATABASE IF EXISTS "jboss-idm";
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public from jboss;
DROP USER IF EXISTS jboss;

CREATE USER jboss WITH PASSWORD 'jboss';

CREATE DATABASE "jboss-jcr" ENCODING='LATIN1' OWNER=jboss;
GRANT ALL PRIVILEGES ON DATABASE "jboss-jcr" to jboss;

CREATE DATABASE "jboss-idm" ENCODING='LATIN1' OWNER=jboss;
GRANT ALL PRIVILEGES ON DATABASE "jboss-idm" to jboss;
