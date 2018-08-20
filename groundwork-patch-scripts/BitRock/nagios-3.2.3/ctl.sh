#!/bin/bash

# For safety's sake:
PATH=/bin:/usr/bin

NAGIOS_START="/usr/local/groundwork/nagios/bin/nagios -d /usr/local/groundwork/nagios/etc/nagios.cfg"
NAGIOS_PIDFILE=/usr/local/groundwork/nagios/nagios.pid
NAGIOS_LOGFILE=/usr/local/groundwork/nagios/var/nagios.log
NAGIOSBIN=".nagios.bin"
NAGIOS_LOCKFILE="/usr/local/groundwork/nagios/var/nagios.lock"
PERFDATA_SCRIPT="process_service_perfdata_file"
PERFDATA_PATH="/usr/local/groundwork/nagios/eventhandlers/$PERFDATA_SCRIPT"

NAGIOS_STATUS=""
NAGIOS_PID=""
PID=""
ERROR=0

get_pid() {
    PID=""
    PIDFILE=$1
    # check for pidfile
    if [ -f "$PIDFILE" ] ; then
	PID=`cat $PIDFILE`
    fi
}

get_nagios_pid() {
    get_pid $NAGIOS_PIDFILE
    if [ ! "$PID" ]; then
	return
    fi
    if [ "$PID" -gt 0 ]; then
	NAGIOS_PID=$PID
    fi
}

is_service_running() {
    PID=$1
    if [ "x$PID" != "x" ] && kill -0 $PID 2>/dev/null ; then
	RUNNING=1
    else
	RUNNING=0
    fi
    return $RUNNING
}

is_nagios_running() {
    get_nagios_pid
    is_service_running $NAGIOS_PID
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	NAGIOS_STATUS="nagios is not running"
    else
	NAGIOS_STATUS="nagios is already running"
    fi
    return $RUNNING
}

start_nagios() {
    # Before we start nagios, kill off the entire process groups of any legacy
    # copies of nagios, so they do not interfere with what we are about to start.
    # It might take a few seconds for the parent nagios process to shut down,
    # and we won't wait here for that to complete.
    # In ordinary operation, this should do nothing, as we should not have any legacy copies running.
    # This won't kill arbitrary descendants of nagios that are no longer in those process groups, but
    # we're not aware of any situations where that might be an issue.
    old_pids=`ps --noheaders -o pid -C nagios | awk '{if ($1 != 1) {print -$1}}'`
    if [ -n "$old_pids" ]; then
	kill -TERM $old_pids
    fi

    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then
	echo "$0: nagios (pid $NAGIOS_PID) is already running"
	exit
    fi

    # Set this so that multiplying it by the sleep time in the loop below
    # allows a reasonable time for $PERFDATA_SCRIPT to shut down.
    retries=5

    # This "pids" variable might include newlines, so we have to
    # watch out for how it gets handled in later processing.
    pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    while [ -n "$pids" ]; do
	kill -TERM $pids
	retries=`expr $retries - 1`
	if [ $retries -le 0 ]; then
	    echo "$0: Error: $PERFDATA_SCRIPT could not be stopped, so nagios could not be started"
	    ERROR=5
	    exit $ERROR
	fi
	sleep 2
	pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    done

    if [ `id | sed -e 's/uid=//g' -e 's/(.*//g'` -eq 0 ]; then
	su nagios -c "$NAGIOS_START"
    else
	$NAGIOS_START
    fi
    # Pause for a brief moment, long enough for Nagios to start operation,
    # daemonize itself, and create and populate the $NAGIOS_LOCKFILE with
    # the PID of the Nagios daemon process.  The period here is a bit
    # arbitrary, apparently set by experimentation, and possibly still
    # subject to a possible race condition with respect to our reading that
    # file to make our own independent copy of the Nagios PID.  Perhaps
    # someday we will find a better solution.
    sleep 2
    # This file descriptor shuffling gets any error message on the standard output stream, to
    # prevent downstream problems with handling newlines when output and error streams are combined.
    exec 3>&1
    head -n 1 $NAGIOS_LOCKFILE > $NAGIOS_PIDFILE 2>&3
    exec 3>&-
    # Allow the nagios user to later correct any oddity in $NAGIOS_PIDFILE
    # even if this time we started the nagios process as root.
    chown nagios $NAGIOS_PIDFILE
    chmod 644    $NAGIOS_PIDFILE
    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	ERROR=1
    fi
    if [ $ERROR -eq 0 ]; then
	echo "$0: nagios started"
	# If we needed to allow additional time for Nagios to internally
	# prepare itself before it can accept input, this would be the
	# place to sleep again for a short period.
	# sleep 2
    else
	echo "$0: Error: nagios could not be started"
	ERROR=3
    fi
}

stop_nagios() {
    # For good measure, before we stop nagios, kill off the entire process
    # groups of any legacy copies of nagios as well.  It might take a few
    # seconds for the parent nagios process to shut down, and we won't
    # wait here for that to complete.  In ordinary operation, this should
    # do nothing, as we should not have any legacy copies running.  This
    # won't kill arbitrary descendants of nagios that are no longer in those
    # process groups, but we're not aware of any situations where that might
    # be an issue.
    old_pids=`ps --noheaders -o pid -C nagios | awk '{if ($1 != 1) {print -$1}}'`
    if [ -n "$old_pids" ]; then
	kill -TERM $old_pids
    fi

    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	echo "$0: $NAGIOS_STATUS"
	return
    fi

    # First kill the process_service_perdata daemon.
    # This "pids" variable might include newlines, so we have to
    # watch out for how it gets handled in later processing.
    pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    if [ -n "$pids" ]; then
	kill -TERM $pids
    fi

    # Now kill Nagios ...

    kill $NAGIOS_PID

    for (( i = 35; i > 0 ; --i ))
    do
	sleep 1
	is_nagios_running
	RUNNING=$?
	if [ $RUNNING -eq 0 ]; then
	    break
	fi
    done

    while true; do
	NagiosProcs=`ps -o pid,args --no-headers -C $NAGIOSBIN | awk "/$NAGIOSBIN -d/"'{print $1}'`
	if [ -z "$NagiosProcs" ]; then
	    break
	fi
	echo "Nagios hasn't died yet (PID: "$NagiosProcs") ... trying again."
	for Proc in $NagiosProcs ; do
	    while true; do
		NagiosChildProcs=`ps -o pid,args --no-headers --ppid $Proc | awk '!/<defunct>/{print $1}'`
		if [ -z "$NagiosChildProcs" ]; then
		    break
		fi
		echo "Nagios children haven't died yet (PID: "$NagiosChildProcs") ... trying again."
		# This is impolite, but we figure we already waited long enough above.
		kill -KILL $NagiosChildProcs > /dev/null 2>&1
		sleep 1
	    done
	    # This is impolite, but we figure we already waited long enough above.
	    kill -KILL $Proc > /dev/null 2>&1
	done
	sleep 1
    done

    # Also clean up check result buffer for plugins that die a horrible death ...
    # With Kudos to Timothy Haggerty; revised to generate no error message when there are outstanding results to test.
    shopt -s nullglob
    for Res in `egrep -l 'return_code=(127|137)' /usr/local/groundwork/nagios/var/checkresults/* /dev/null` ; do
	echo "Removing killed plugin results ($Res and $Res.ok) ..."
	rm -f $Res
	rm -f $Res.ok
    done
    shopt -u nullglob
    
    # Just in case it started while we were waiting, kill the process_service_perfdata daemon again.
    pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    if [ -n "$pids" ]; then
	kill -TERM $pids
    fi

    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	echo "$0: nagios stopped"
	rm -f $NAGIOS_PIDFILE
    else
	echo "$0: Error: nagios could not be stopped"
	ERROR=4
    fi
}

if [ "x$1" = "xstart" ]; then
    start_nagios
elif [ "x$1" = "xstop" ]; then
    stop_nagios
elif [ "x$1" = "xrestart" ]; then
    stop_nagios
    start_nagios
elif [ "x$1" = "xstatus" ]; then
    is_nagios_running
    echo "$NAGIOS_STATUS"
else
    echo "usage:  ctl.sh [start|stop|restart|status]"
    ERROR=6
fi

exit $ERROR

