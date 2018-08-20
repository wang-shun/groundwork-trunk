#!/bin/sh

# InfluxDB server control script 
# Based on /usr/local/groundwork/common/scripts/ctl-snmptt.sh

INFLUXDB_APPNAME="influxd"
INFLUXDB_BASEDIR="/usr/local/groundwork/influxdb"
INFLUXDB_PATH="$INFLUXDB_BASEDIR/bin/$INFLUXDB_APPNAME"
INFLUXDB_PIDFILE="$INFLUXDB_BASEDIR/var/run/$INFLUXDB_APPNAME.pid"
INFLUXDB_START="$INFLUXDB_PATH -pidfile $INFLUXDB_PIDFILE -config $INFLUXDB_BASEDIR/etc/influxdb.conf >> $INFLUXDB_BASEDIR/var/log/$INFLUXDB_APPNAME.log 2>&1 &"
RUNUSER="nagios"
INFLUXDB_STATUS==""
ERROR=0
OPEN_FILE_LIMIT=65536 # Max open files

is_service_running() {
    if pgrep -u $RUNUSER -x "$INFLUXDB_APPNAME" >/dev/null 2>&1 ; then
        RUNNING=1
    else
        RUNNING=0
    fi
    return $RUNNING
}

is_influxdb_running() {
    is_service_running 
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	INFLUXDB_STATUS="$INFLUXDB_APPNAME not running"
    else
	INFLUXDB_STATUS="$INFLUXDB_APPNAME already running"
    fi
    return $RUNNING
}

start_influxdb() {
    echo "Starting $INFLUXDB_APPNAME ..."
    is_influxdb_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then
	echo "$0 $ARG: $INFLUXDB_APPNAME already running"
	exit
    fi
    if [ -f $INFLUXDB_PIDFILE ]; then
	rm $INFLUXDB_PIDFILE
    fi
    # if running as root, the run the process as user $RUNUSER (typically nagios)
    if [ `id | sed -e 's/uid=//g' -e 's/(.*//g'` -eq 0 ]; then
	su $RUNUSER -c "$INFLUXDB_START"
    else # else run it as the current user
	$INFLUXDB_START
    fi

    # From the original sysv ctl script...
    # Bump the file limits, before launching the daemon. These will
    # carry over to launched processes.
    ulimit -n $OPEN_FILE_LIMIT
    if [ $? -ne 0 ]; then
        echo "Unable to do ulimit -n $OPEN_FILE_LIMIT - quitting"
        exit 1
    fi

    COUNTER=30
    RUNNING=0
    while [ $RUNNING -eq 0 ] && [ $COUNTER -ne 0 ]; do
	COUNTER=`expr $COUNTER - 1`
	sleep 2
	is_influxdb_running
	RUNNING=$?
    done
    is_influxdb_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	ERROR=2
    fi
    if [ $ERROR -eq 0 ]; then
	echo "$0 $ARG: $INFLUXDB_APPNAME started"
    else
	echo "$0 $ARG: $INFLUXDB_APPNAME could not be started"
	ERROR=3
    fi
}

stop_influxdb() {
    echo "Stopping $INFLUXDB_APPNAME ..."
    NO_EXIT_ON_ERROR=$1
    is_influxdb_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	echo "$0 $ARG: $INFLUXDB_STATUS"
	if [ "x$NO_EXIT_ON_ERROR" != "xno_exit" ]; then
	    exit
	else
	    return
	fi
    fi
    pids=`ps -o pid,args --no-headers -C $INFLUXDB_APPNAME | fgrep "$INFLUXDB_PATH" | awk '{print $1}'`
    if [ -n "$pids" ]; then
	kill -TERM $pids > /dev/null 2>&1
    fi
    sleep 3
    is_influxdb_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	    echo "$0 $ARG: $INFLUXDB_APPNAME stopped"
            # influx doesn't to remove its pid after stopping 
            if [ -f $INFLUXDB_PIDFILE ]; then
	        rm $INFLUXDB_PIDFILE
            fi
	else
	    echo "$0 $ARG: $INFLUXDB_APPNAME could not be stopped"
	    ERROR=4
    fi
}

if [ "x$1" = "xstart" ]; then
    start_influxdb
elif [ "x$1" = "xstop" ]; then
    stop_influxdb
elif [ "x$1" = "xstatus" ]; then
    is_influxdb_running
    echo "$INFLUXDB_STATUS"
else
    echo "usage:  $0 {start|stop|status}"
    ERROR=5
fi

exit $ERROR
