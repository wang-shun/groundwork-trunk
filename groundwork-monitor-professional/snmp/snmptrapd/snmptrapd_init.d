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
# init file for snmptrapd
# Alex Burger - 8/29/02
# 	      - 9/8/03 - Added snmptt.pid support to Stop function
# Peter Loh - 10/17/2005 - Modified to work with GroundWork Monitor
#
# chkconfig: - 50 50
# description: Simple Network Management Protocol (SNMP) Daemon
#
# processname: /usr/local/groundwork/sbin/snmptrapd 
# pidfile: /var/run/snmptrapd.pid

# source function library
. /etc/init.d/functions

OPTIONS="-On -c /usr/local/groundwork/etc/snmp/snmptrapd.conf -o /usr/local/groundwork/var/log/snmp/snmptrapd.log"
RETVAL=0
prog="snmptrapd"

start() {
	echo -n $"Starting $prog: "
        daemon /usr/local/groundwork/sbin/snmptrapd $OPTIONS
	RETVAL=$?
	echo
	touch /var/lock/subsys/snmptrapd
	return $RETVAL
}

stop() {
	echo -n $"Stopping $prog: "
	killproc /usr/local/groundwork/sbin/snmptrapd 2>/dev/null
	RETVAL=$?
	echo
	rm -f /var/lock/subsys/snmptrapd
	if test -f /var/run/snmptrapd.pid ; then
	  [ $RETVAL -eq 0 ] && rm -f /var/run/snmptrapd.pid
	fi
	return $RETVAL
}

reload(){
        echo -n $"Reloading config file: "
        killproc snmptrapd -HUP
        RETVAL=$?
        echo
        return $RETVAL
}

restart(){
	stop
	start
}

condrestart(){
    [ -e /var/lock/subsys/snmptrapd ] && restart
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
