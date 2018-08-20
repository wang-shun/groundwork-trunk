#Collage - The ultimate data integration framework.
#Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com
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
#$Id: create-production-db.sql 15497 2009-06-08 18:43:15Z asoleymanzadeh $

#drop database 
#use mysql; 
drop DATABASE IF EXISTS GWCollageDB;
create DATABASE GWCollageDB;

# set permissions

GRANT ALL ON GWCollageDB.* to root ;
GRANT ALL PRIVILEGES ON GWCollageDB.* TO collage@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
flush privileges;

# JBoss and JBoss Portal databases
create user jboss identified by 'jboss';

create database if not exists jbossdb;
grant all on jbossdb.* to 'jboss'@'localhost' identified by 'jboss';

create database if not exists jbossportal;
grant all on jbossportal.* to 'jboss'@'localhost' identified by 'jboss';
