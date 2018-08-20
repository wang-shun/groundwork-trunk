#!/bin/sh
#Copyright (C) 2004-2006  GroundWork Open Source Solutions info@groundworkopensource.com
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation and reprinted below;
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
# Shell wrapper around feeder service and adapters
export JARPATH=@JARLIBPATH@
export BINDIR=@BINDIR@
export CONFIGDIR=@CONFIG_DIR@

exec java -DadapterConfig=$BINDIR/adapter.properties -DserviceConfig=$BINDIR/service.properties -classpath $CONFIGDIR:$BINDIR:$JARPATH/collage-adapter-genericlog-@VERSION@.jar:$JARPATH/collage-adapter-syslog-@VERSION@.jar:$JARPATH/collage-adapter-admin-@VERSION@.jar:$JARPATH/collage-adapter-nagios-@VERSION@.jar:$JARPATH/collage-adapter-snmp-@VERSION@.jar:$JARPATH/aopalliance-1.0.jar:$JARPATH/commons-pool-1.2.jar:$JARPATH/commons-dbcp-1.2.1.jar:$JARPATH/cglib-full-2.0.2.jar:$JARPATH/collage-adapter-api-@VERSION@.jar:$JARPATH/collage-admin-impl-@VERSION@.jar:$JARPATH/collage-api-@VERSION@.jar:$JARPATH/collage-common-impl-@VERSION@.jar:$JARPATH/commons-collections-3.0.jar:$JARPATH/commons-lang-2.0.jar:$JARPATH/@COMMONSLOGGING@-@COMMONSLOGGINGVER@.jar:$JARPATH/concurrent-1.3.4.jar:$JARPATH/DataFeederService-@VERSION@.jar:$JARPATH/@DOM4J@-@DOM4JVER@.jar:$JARPATH/ehcache-1.1.jar:$JARPATH/@HIBERNATE@-@HIBERNATEVER@.jar:$JARPATH/jta1.0.1.jar:$JARPATH/log4j-1.2.8.jar:$JARPATH/mysql-connector-java-3.1.7-bin.jar:$JARPATH/odmg-3.0.jar:$JARPATH/@SPRING@-@SPRINGVER@.jar:$JARPATH/@ANTLR@-@ANTLRVER@.jar:$JARPATH/@C3P0@-@C3P0VER@.jar com.groundwork.feeder.service.DataFeederService
 