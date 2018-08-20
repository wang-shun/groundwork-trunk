#$Id: create-gwcollage-db.sql 6397 2007-04-02 21:27:40Z glee $
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

#drop database 

use mysql; 
drop DATABASE IF EXISTS GWCollageDB;
create DATABASE GWCollageDB;

GRANT ALL ON GWCollageDB.* to root ;
GRANT ALL ON GWCollageDB.* to groundwork identified by 'groundwork' ;