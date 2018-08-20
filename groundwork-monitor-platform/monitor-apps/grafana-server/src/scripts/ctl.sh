#!/bin/sh

# Grafana server control script 
# Based on /usr/local/groundwork/common/scripts/ctl-snmptt.sh

GRAFANA_APPNAME="grafana-server"
GRAFANA_BASEDIR="/usr/local/groundwork/grafana"
GRAFANA_PATH="$GRAFANA_BASEDIR/bin/$GRAFANA_APPNAME"
GRAFANA_PIDFILE="$GRAFANA_BASEDIR/var/run/grafana-server.pid"

# Grafana will create it's own grafana.log containing stdout, under default.paths.log dir (although in practice it seems that it always puts it in .../data/log regardless)
# To avoid duplication in the log, GRAFANA_START tosses stdout, but sends errors to the grafana.log.
# In tests, not sure what the cfg:default.paths.logs setting is doing. If the defaults.ini logs setting is set, it uses that, and if its not, then it uses GRAFANA_BASEDIR.
DAEMON_OPTS="cfg:default.paths.data=$GRAFANA_BASEDIR/var cfg:default.paths.logs=$GRAFANA_BASEDIR/data/log cfg:default.paths.plugins=$GRAFANA_BASEDIR/var/plugins"
GRAFANA_START="$GRAFANA_PATH -homepath $GRAFANA_BASEDIR -pidfile $GRAFANA_PIDFILE -config $GRAFANA_BASEDIR/conf/defaults.ini $DAEMON_OPTS >>/dev/null 2>>$GRAFANA_BASEDIR/data/log/grafana.log &"

RUNUSER="nagios"
GRAFANA_STATUS==""
ERROR=0
OPEN_FILE_LIMIT=65536 # Max open files 

# Need to do operations from in basedir to avoid these errors during GroundWork install from /root 
#    "grafana golang panic: error getting work directory: stat .: permission denied" errors when
cd $GRAFANA_BASEDIR

is_service_running() {
    if pgrep -u $RUNUSER -x "$GRAFANA_APPNAME" >/dev/null 2>&1 ; then
        RUNNING=1
    else
        RUNNING=0
    fi
    return $RUNNING
}

is_grafana_running() {
    is_service_running 
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	GRAFANA_STATUS="$GRAFANA_APPNAME not running"
    else
	GRAFANA_STATUS="$GRAFANA_APPNAME already running"
    fi
    return $RUNNING
}

# The grafana-server process will create and manage the pid file.
# But we won't depend on it here.
start_grafana() {
    echo "Starting $GRAFANA_APPNAME ..."
    is_grafana_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then
	echo "$0 $ARG: $GRAFANA_APPNAME already running"
	exit
    fi
    if [ -f $GRAFANA_PIDFILE ]; then
	rm $GRAFANA_PIDFILE
    fi
    # if running as root, the run the process as user $RUNUSER (typically nagios)
    if [ `id | sed -e 's/uid=//g' -e 's/(.*//g'` -eq 0 ]; then
	su $RUNUSER -c "$GRAFANA_START"
    else # else run it as the current user
	$GRAFANA_START
    fi


    # From the original sysv ctl script...
    # Bump the file limits, before launching the daemon. These will
    # carry over to launched processes.
    ulimit -n $OPEN_FILE_LIMIT
    if [ $? -ne 0 ]; then
        echo "Unable to do ulimit -n $OPEN_FILE_LIMIT - quitting"
        exit 1
    fi

    # 10 attempts after which we're in trouble enought to warrant an error and investigation
    COUNTER=10 
    RUNNING=0
    while [ $RUNNING -eq 0 ] && [ $COUNTER -ne 0 ]; do
	COUNTER=`expr $COUNTER - 1`
	sleep 2
	is_grafana_running
	RUNNING=$?
    done
    is_grafana_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	ERROR=2
    fi
    if [ $ERROR -eq 0 ]; then
	echo "$0 $ARG: $GRAFANA_APPNAME started"
    else
	echo "$0 $ARG: $GRAFANA_APPNAME could not be started"
	ERROR=3
    fi
}

stop_grafana() {
    echo "Stopping $GRAFANA_APPNAME ..."
    NO_EXIT_ON_ERROR=$1
    is_grafana_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	echo "$0 $ARG: $GRAFANA_STATUS"
	if [ "x$NO_EXIT_ON_ERROR" != "xno_exit" ]; then
	    exit
	else
	    return
	fi
    fi
    pids=`ps -o pid,args --no-headers -C $GRAFANA_APPNAME | fgrep "$GRAFANA_PATH" | awk '{print $1}'`
    if [ -n "$pids" ]; then
	kill -TERM $pids > /dev/null 2>&1
    fi
    sleep 3
    is_grafana_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	    echo "$0 $ARG: $GRAFANA_APPNAME stopped"
	else
	    echo "$0 $ARG: $GRAFANA_APPNAME could not be stopped"
	    ERROR=4
    fi
}

if [ "x$1" = "xstart" ]; then
    start_grafana
elif [ "x$1" = "xstop" ]; then
    stop_grafana
elif [ "x$1" = "xstatus" ]; then
    is_grafana_running
    echo "$GRAFANA_STATUS"
else
    echo "usage:  $0 {start|stop|status}"
    ERROR=5
fi

exit $ERROR
