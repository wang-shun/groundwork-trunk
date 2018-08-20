#!/bin/sh

NAGIOS_START="@@BITROCK_NAGIOS_ROOTDIR@@/bin/nagios -d @@BITROCK_NAGIOS_ROOTDIR@@/etc/nagios.cfg"
NAGIOS_PIDFILE=@@BITROCK_NAGIOS_ROOTDIR@@/nagios.pid
NAGIOS_LOGFILE=@@BITROCK_NAGIOS_ROOTDIR@@/var/nagios.log
NAGIOSBIN=".nagios.bin"
NAGIOS_LOCKFILE="@@BITROCK_NAGIOS_ROOTDIR@@/var/nagios.lock"

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
        NAGIOS_STATUS="nagios not running"
    else
        NAGIOS_STATUS="nagios already running"
    fi
    return $RUNNING
}


start_nagios() {
    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then
        echo "$0 $ARG: nagios  (pid $NAGIOS_PID) already running"
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
            echo "$0 $ARG: $PERFDATA_SCRIPT could not be stopped, so nagios could not be started"
            ERROR=5
            exit $ERROR
        fi
        sleep 2
        pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    done

    if [ `id|sed -e s/uid=//g -e s/\(.*//g` -eq 0 ]; then
	su nagios -c "$NAGIOS_START"
    else
	$NAGIOS_START
    fi
    sleep 2
    head -n 1 $NAGIOS_LOCKFILE > $NAGIOS_PIDFILE
    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        ERROR=1
    fi
    if [ $ERROR -eq 0 ]; then
	echo "$0 $ARG: nagios  started"
	sleep 2
    else
	echo "$0 $ARG: nagios  could not be started"
	ERROR=3
    fi
}

stop_nagios() {
    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        echo "$0 $ARG: $NAGIOS_STATUS"
        return
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
            echo "$0 $ARG: $PERFDATA_SCRIPT could not be stopped. Please stop this program and try again."
            ERROR=5
            exit $ERROR
        fi
        sleep 2
        pids=`ps -o pid,args --no-headers -C .perl.bin | fgrep $PERFDATA_PATH | awk '{print $1}'`
    done
	
    kill $NAGIOS_PID

    for i in 1 2 3 4 5 6 7 8 9 10
    do
        sleep 3
        is_nagios_running
        RUNNING=$?
        if [ $RUNNING -eq 0 ]; then
            break
        fi
    done

    NagiosProcs=`ps auwwx | grep -v grep| grep "$NAGIOSBIN -d" | grep "@@BITROCK_NAGIOS_ROOTDIR@@" | awk '{print $2}'`
    NagiosProcs=`for Proc in $NagiosProcs ; do echo -en "$Proc "; done`

    while test "X$NagiosProcs" != "X" ; do
        echo "Nagios hasn't died yet ($NagiosProcs)... Trying again."
        for Proc in $NagiosProcs ; do

            NagiosChildProcs=`/bin/ps --ppid $Proc|grep -v 'TTY' | awk '{print $1}'`
            NagiosChildProcs=`for ChildProc in $NagiosChildProcs ; do echo -en "$ChildProc "; done`

            while test "X$NagiosChildProcs" != "X" ; do
                echo "Nagios children haven't died yet ($NagiosChildProcs)... Trying again." 
                for ChildProc in $NagiosChildProcs ; do
                    kill -9 $ChildProc > /dev/null 2>&1
                done
                NagiosChildProcs=`/bin/ps --ppid $Proc|grep -v 'TTY' | awk '{print $1}'`
                NagiosChildProcs=`for ChildProc in $NagiosChildProcs ; do
                echo -en "$ChildProc "; done`
            done
            kill -9 $Proc > /dev/null 2>&1
        done
        sleep 1

        NagiosProcs=`ps auwwx | grep -v grep | grep "$NAGIOSBIN -d" | grep "@@BITROCK_NAGIOS_ROOTDIR@@" | awk '{print $2}'`
        NagiosProcs=`for Proc in $NagiosProcs ; do echo -en "$Proc "; done`
    done

    is_nagios_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        echo "$0 $ARG: nagios stopped"
        rm $NAGIOS_PIDFILE
    else
        echo "$0 $ARG: nagios could not be stopped"
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
fi

exit $ERROR
