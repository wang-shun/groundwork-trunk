#!/bin/bash
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
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin # Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

# init file for snmptt
# Alex Burger - 8/29/02
# 	      - 9/8/03 - Added snmptt.pid support to Stop function
# Peter Loh - 10/17/2005 - Modified to work with GroundWork Monitor
#
# chkconfig: - 50 50
# description: Simple Network Management Protocol (SNMP) Daemon
#
# processname: /usr/local/groundwork/sbin/snmptt 
# pidfile: /var/run/snmptt.pid

# source function library
. /etc/init.d/functions

OPTIONS="--daemon --ini /usr/local/groundwork/etc/snmp/snmptt.ini"
RETVAL=0
prog="snmptt"

start() {
	echo -n $"Starting $prog: "
        daemon /usr/local/groundwork/sbin/snmptt $OPTIONS
	RETVAL=$?
	echo
	touch /var/lock/subsys/snmptt
	return $RETVAL
}

stop() {
	echo -n $"Stopping $prog: "
	killproc /usr/local/groundwork/sbin/snmptt 2>/dev/null
	RETVAL=$?
	echo
	rm -f /var/lock/subsys/snmptt
	if test -f /var/run/snmptt.pid ; then
	  [ $RETVAL -eq 0 ] && rm -f /var/run/snmptt.pid
	fi
	return $RETVAL
}

reload(){
        echo -n $"Reloading config file: "
        killproc snmptt -HUP
        RETVAL=$?
        echo
        return $RETVAL
}

restart(){
	stop
	start
}

condrestart(){
    [ -e /var/lock/subsys/snmptt ] && restart
    return 0
}

case "$1" in
  start)
	start
	;;
  stop)
	stop
	;;
  restart)
	restart
        ;;
  reload)
	reload
        ;;
  condrestart)
	condrestart
	;;
  status)
        status snmptt
	RETVAL=$?
        ;;
  *)
	echo $"Usage: $0 {start|stop|restart|condrestart|reload}"
	RETVAL=1
esac

exit $RETVAL
