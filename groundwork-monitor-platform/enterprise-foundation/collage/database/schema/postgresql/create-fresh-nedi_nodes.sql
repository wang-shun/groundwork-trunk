-- Copyright (C) 2018  GroundWork Inc.  www.gwos.com
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

DROP DATABASE IF EXISTS nedi_nodes;
CREATE DATABASE nedi_nodes ENCODING='LATIN1' OWNER=nedi;

-- This would make the nedi_nodes.public schema owned by nedi, not by postgres.
-- \connect nedi_nodes
-- ALTER SCHEMA public OWNER TO nedi;

-- Set permissions.
GRANT ALL PRIVILEGES ON DATABASE nedi_nodes to nedi;
