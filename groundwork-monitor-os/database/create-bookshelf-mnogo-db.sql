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
#
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
# Create bookshelf
################################################
use mysql;
drop DATABASE IF EXISTS bookshelf;
#create DATABASE bookshelf;

#GRANT ALL ON bookshelf.* to root ;
#GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
#GRANT ALL PRIVILEGES ON bookshelf.* TO bookshelf@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
#flush privileges;
