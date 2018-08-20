-- Copyright (C) 2015  GroundWork Inc.  www.gwos.com
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

-- PostgreSQL still doesn't have a "CREATE USER IF NOT EXISTS" clause,
-- so we have to manufacture an equivalent construction.  This will do.
DO $$
    BEGIN
	IF NOT EXISTS (
	    SELECT *
	    FROM   pg_catalog.pg_user
	    WHERE  usename = 'noma'
	) THEN
	    CREATE USER noma WITH PASSWORD 'nomapass';
	END IF;
    END
$$;

DROP DATABASE IF EXISTS noma;
CREATE DATABASE noma ENCODING='LATIN1' OWNER=noma;

-- This would make the noma.public schema owned by noma, not by postgres.
-- \connect noma
-- ALTER SCHEMA public OWNER TO noma;

-- Set permissions.
GRANT ALL PRIVILEGES ON DATABASE noma to noma;

