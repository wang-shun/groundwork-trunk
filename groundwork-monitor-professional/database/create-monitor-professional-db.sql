#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
###################################
# Cleanup of obsolete databases
###################################

use mysql;
drop DATABASE IF EXISTS bookshelf;



#######################################
#Create old standalone DASHBOARD for Guava
#######################################
#drop database
use mysql;
drop DATABASE IF EXISTS dashboards;


#################################################
# Create Mnogosearch
################################################
use mysql;
drop DATABASE IF EXISTS mnogosearch;


#################################################
# drop old  Sv
################################################
use mysql;
drop DATABASE IF EXISTS sv;

#################################################
# Create log-reports
################################################
use mysql;
drop DATABASE IF EXISTS logreports;
create DATABASE logreports;

GRANT ALL ON logreports.* to root ;
GRANT ALL PRIVILEGES ON logreports.* TO logreporting@localhost IDENTIFIED BY 'gwrk' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO root@localhost WITH GRANT OPTION;
flush privileges;

