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

DROP DATABASE IF EXISTS archive_gwcollagedb;
CREATE DATABASE archive_gwcollagedb ENCODING='LATIN1' OWNER=collage;

-- set permissions
GRANT ALL PRIVILEGES ON DATABASE archive_gwcollagedb to collage;

