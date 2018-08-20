#!/bin/bash

# init file for noma
#
# NoMa: the NETWAYS NOtification MAnager

#
#       APP Specific Definitions.
#

# Many of these variables date from an older version of this script,
# and are no longer directly relevant.  We only leave them in place
# to document certain commands in comments.
GWPATH=/usr/local/groundwork
SERVICEPATH=$GWPATH/core/services/notification-noma
APPNAME=noma_daemon.pl
APPBIN=$GWPATH/noma/notifier
APPVAR=$GWPATH/noma/var
APPEXECUTABLE=$APPBIN/$APPNAME
APPPIDFILE=$APPVAR/noma.pid
APPSTART="$APPEXECUTABLE"
ERROR=0

RUNUSER="nagios"
RUNGROUP="nagios"

# We are currently a freedt distribution that does not include the svup
# program which is included in the daemontools-encore distribution.  So
# we have to make do with the combination of svok and svstat instead.
SVC=$GWPATH/common/bin/svc
SVOK=$GWPATH/common/bin/svok
SVSTAT=$GWPATH/common/bin/svstat
SVUP=$GWPATH/common/bin/svup

RETVAL=0

start() {
    # Here's the "official" way to start NoMa on many platforms.
    # daemon --pidfile=$APPPIDFILE --user $RUNUSER $APPEXECUTABLE

    start_noma
}

start_noma() {
    is_noma_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then 
	echo "noma is already running"
	exit
    fi
    /bin/rm -f $SERVICEPATH/down
    output=`$SVC -u $SERVICEPATH 2>&1`
    if [ -n "$output" ]; then
	echo "$output"
    fi
    if [[ "$output" =~ ' no supervise running for service' ]]; then
	echo "It looks like gwservices is down, so NoMa will not run."
    else
	is_noma_running
	RUNNING=$?
	COUNTER=10
	while [ $RUNNING -eq 0 ] && [ $COUNTER -ne 0 ]; do 
	    COUNTER=`expr $COUNTER - 1`
	    sleep 2
	    is_noma_running
	    RUNNING=$?
	done    
	if [ $RUNNING -eq 0 ]; then 
	    ERROR=2
	fi
	if [ $ERROR -eq 0 ]; then 
	    echo "noma started"
	else    
	    echo "noma could not be started"
	    ERROR=3
	fi
    fi
}

is_noma_running() {
    # Here's the "official" way to check the status of NoMa on many platforms.
    # status -p $APPPIDFILE $APPNAME

    # We'd like to use svup from the daemontools-encore distribution:
    #     $SVUP $SERVICEPATH
    #     STATUS=$?
    # but we don't currently have svup in our distribution (of freedt instead).
    # So we have to make do with a combination of svok and svstat instead.
    $SVOK $SERVICEPATH && $SVSTAT $SERVICEPATH | fgrep -q "${SERVICEPATH}: up "
    STATUS=$?
    if [ $STATUS -eq 0 ]; then 
	NOMA_STATUS="noma is already running"
	RUNNING=1
    else    
	NOMA_STATUS="noma is not running"
	RUNNING=0
    fi

    return $RUNNING
}

stop() {
    # Here's the "official" way to start NoMa on many platforms.
    # killproc -p $APPPIDFILE $APPEXECUTABLE 2>/dev/null
    # RETVAL=$?

    stop_noma
}

stop_noma() {
    NO_EXIT_ON_ERROR=$1
    is_noma_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	touch $SERVICEPATH/down
	echo "$NOMA_STATUS"
	if [ "x$NO_EXIT_ON_ERROR" != "xno_exit" ]; then
	    exit
	else
	    return
	fi
    fi
    $SVC -d $SERVICEPATH
    touch $SERVICEPATH/down
    sleep 3
    is_noma_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
	echo "noma stopped"
    else
	echo "noma could not be stopped"
	ERROR=4
    fi
}

restart () {
    stop
    start
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
  status)
	is_noma_running
	RUNNING=$?
	if [ $RUNNING -ne 0 ]; then
	    RETVAL=0
	else
	    RETVAL=1
	fi
	echo "$NOMA_STATUS"
	;;
  *)
	echo $"Usage: $0 {start|stop|restart|status}"
	RETVAL=1
esac

exit $RETVAL

