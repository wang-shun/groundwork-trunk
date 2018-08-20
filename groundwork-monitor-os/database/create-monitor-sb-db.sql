# $Id: $
#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.

####################################
# Cleanup of obsolete databases
###################################

use mysql;
drop DATABASE IF EXISTS bookshelf;


#######################################
#Create DASHBOARD Insight reports
#######################################
#drop database
use mysql;
drop DATABASE IF EXISTS dashboard;
create DATABASE dashboard;

GRANT ALL ON dashboard.* to root ;
GRANT ALL PRIVILEGES ON dashboard.* TO ir@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
flush privileges;

#################################################
# Create Monarch
################################################
use mysql;
drop DATABASE IF EXISTS monarch;
create DATABASE monarch;

GRANT ALL ON monarch.* to root ;
GRANT ALL PRIVILEGES ON monarch.* TO monarch@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
flush privileges;

#################################################
# Create Mnogosearch
################################################
use mysql;
drop DATABASE IF EXISTS mnogosearch;
#create DATABASE mnogosearch;

#GRANT ALL PRIVILEGES ON mnogosearch.* TO mnogosearch@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
#GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
#flush privileges;

#################################################
# Create Guava
################################################
use mysql;
drop DATABASE IF EXISTS guava;
#create DATABASE guava;

#GRANT ALL ON guava.* to root ;
#GRANT ALL PRIVILEGES ON guava.* TO guava@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
#GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
#flush privileges;

#################################################
# Create Sv
################################################
use mysql;
drop DATABASE IF EXISTS sv;
#create DATABASE sv;

#GRANT ALL ON sv.* to root ;
#GRANT ALL PRIVILEGES ON sv.* TO sv@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
#GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
#flush privileges;

################################################
# Read only access to foundation for UI user
###############################################
GRANT SELECT ON GWCollageDB.* TO foundation@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
flush privileges;
