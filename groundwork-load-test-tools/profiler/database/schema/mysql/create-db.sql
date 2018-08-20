#Collage - The ultimate data integration framework.
#Copyright (C) 2004-2006  GroundWork Open Source Solutions info@groundworkopensource.com
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License 
#    as published by the Free Software Foundation.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#$Id: create-production-db.sql 4016 2006-08-17 21:57:48 +0000 (Thu, 17 Aug 2006) rruttimann $

#drop database 
use mysql; 
drop DATABASE IF EXISTS GWProfilerDB;
create DATABASE GWProfilerDB;

# set permissions

GRANT ALL ON GWProfilerDB.* to root ;
GRANT ALL PRIVILEGES ON GWProfilerDB.* TO collage@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
flush privileges;