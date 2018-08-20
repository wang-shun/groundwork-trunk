#!/bin/sh

# Allow only root execution.
if [ `id|sed -e s/uid=//g -e s/\(.*//g` -ne 0 ]; then
    echo "This script requires root privileges."
    exit 1
fi

#
# Solaris/AIX version of GDMA start/stop script.
# Installed as, under Solaris:
#	/opt/groundwork/ctlscript.sh
# Called from this script, under Solaris:
#	/etc/init.d/gdma
#
# Installed as, under AIX:
#	/usr/local/groundwork/ctlscript.sh
# Called from this script, under AIX:
#	/usr/local/groundwork/scripts/gdma_aix_controller
#
# Starts and stops the GroundWork Monitor Distributed Agent (GDMA) daemons.
#
# description:  run status/metric checks on a periodic basis,
#               with results sent to GroundWork Monitor
# processname:  gdma
#
# Copyright (c) 2011-2017 GroundWork Open Source, Inc.  ("GroundWork").
# All rights reserved.

debug=
# Uncomment this next line to turn on simple debug logging.
# debug=-d1
# Uncomment this next line to turn on full debug logging.
# debug=-d2

user=gdma
os=`uname -s`
if [ $os = SunOS ]; then
    gwpath=/opt/groundwork
    zone=`zonename`
    ps_zone_opt='-o zone='
    awk_zone_match='$2 == "'"$zone"'"'
elif [ $os = AIX ]; then
    gwpath=/usr/local/groundwork
    ps_zone_opt=''
    awk_zone_match=''
else
    echo "The $os operating system is not supported."
    exit 1
fi

[ -x $gwpath/gdma/bin/gdma_poll.pl ] || exit 0
[ -x $gwpath/gdma/bin/gdma_spool_processor.pl ] || exit 0

path_to_perl=$gwpath/perl/bin/perl
path_to_perl_binary=$gwpath/perl/bin/.perl.bin
RETVAL=0

count() {
    echo $#
}

# We'd like to use the standard pgrep and pkill commands, but in the past, we needed
# to be portable back to at least Solaris 2.6, where they were not available.  As of
# GDMA 2.5.0, we are only supporting Solaris 10 and later, so we have more freedom
# now.  This code could therefore change in a later GDMA release.

# This implementation of gdma_pgrep() acts as if the "pgrep -f -u $user" option
# is implicit, and doesn't do full ERE pattern matching like a real pgrep (that
# is, pgrep invoked without the -x option).  But we need the arguments and
# not just the command to identify specific processes of interest, so this is
# actually broken.  (Solaris reports the command as perl [or .perl.bin in the
# Bitrock environment], not as the script name.)
#
# FIX MINOR:  The invocation of gdma_pgrep() has been hacked below to support the
# Bitrock environment.  Unfortunately, it senses and kills ANY and ALL scripts
# that are running the Bitrock version of Perl (.perl.bin) (and as the $user user),
# not just the gdma_poll.pl and gdma_spool_processor.pl scripts as it should (and
# used to, before changes to deal with the Bitrock environment).  This works for
# our present purposes, but is ugly and not robust against future evolution.
# GDMA-247 contains details.
#
gdma_pgrep() {
    /usr/bin/ps -u $user -o pid= $ps_zone_opt -o comm= | fgrep "$1" | nawk "$awk_zone_match"'{print $1}'
}

# This implementation of gdma_pkill() handles only very limited signalling,
# of process IDs and progress group IDs.  Send the signals as user $user.
gdma_pkill() {
    retval=0
    if [ "$1" = "-g" ]; then
	shift
	# protect against both 0 and 1, just in case ...
	for id in $*
	do
	    if [ "$id" -ne 0 -a "$id" -ne 1 ]; then
		/bin/su - $user -c "/usr/bin/kill -TERM -- -$id" >/dev/null 2>&1
		status=$?
		if [ $status -ne 0 ]; then
		    retval=$status
		fi
	    fi
	done
    else
	# protect against both 0 and 1, just in case ...
	echo "Processes we are about to stop:"
	pids_list=`echo "$@" | sed -e 's/ /,/g'`
	ps -p "$pids_list" -f
	for id in $*
	do
	    if [ "$id" -ne 0 -a "$id" -ne 1 ]; then
		/bin/su - $user -c "/usr/bin/kill -TERM $id" >/dev/null 2>&1
		status=$?
		if [ $status -ne 0 ]; then
		    retval=$status
		fi
	    fi
	done
    fi
    return $retval
}

start_gdma() {
    retval=0
    # Check if it is already running
    gdmapids=`gdma_pgrep '.perl.bin'`
    if [ -z "$gdmapids" ]; then
	echo "Starting the gdma daemon ..."
	# FIX MINOR:  We have sometimes seen the spooler not start, for an unknown reason, during package install.
	# Why should we throw away any error messages here, that might give us a clue as to why?  Figure out some
	# way to redirect stdout and stderr to the spooler log file, instead, or to some other standard place.
	# Also make sure the script itself forces logging on in such a situation (when it cannot stay up).
	/bin/su - $user -c "cd /; $path_to_perl -w $gwpath/gdma/bin/gdma_poll.pl            $debug &" </dev/null >/dev/null 2>&1
	poll_retval=$?
	/bin/su - $user -c "cd /; $path_to_perl -w $gwpath/gdma/bin/gdma_spool_processor.pl $debug &" </dev/null >/dev/null 2>&1
	spool_retval=$?
	if [ $poll_retval -ne 0 -o $spool_retval -ne 0 ]; then
	    retval=1
	fi
    # This count of 2 assumes without checking that the process(es) we found are actually the
    # ones we're looking for.  There's no proof of that by just using the System V ps command as
    # we are doing, so we need to use the /usr/ucb/ps command (on Solaris, if it is available)
    # or extended output from the ps command (on AIX) to precisely identify the process(es).
    elif [ `count $gdmapids` -eq 2 ]; then
	echo "The gdma daemon is already running.  PIDS:"
	echo $gdmapids
	echo "Processes:"
	pids_list=`echo $gdmapids | sed -e 's/ /,/g'`
	ps -p "$pids_list" -f
    elif [ "$1" = start ]; then
	echo "The gdma daemon is already partly running.  PIDS:"
	echo $gdmapids
	echo "Processes:"
	pids_list=`echo $gdmapids | sed -e 's/ /,/g'`
	ps -p "$pids_list" -f
	if [ $os = SunOS ]; then
	    # FIX LATER:  This message will need to change in the future, once we have
	    # Solaris GDMA running under the Service Management Facility on this platform.
	    echo 'NOTICE:  To get the daemon running properly,'
	    echo '         use "/etc/init.d/gdma restart" now instead of "/etc/init.d/gdma start".'
	elif [ $os = AIX ]; then
	    echo 'NOTICE:  To get the daemon running properly,'
	    echo '         use "stopsrc -s gdma" followed by "startsrc -s gdma".'
	else
	    echo "The $os operating system is not supported."
	    exit 1
	fi
	retval=1
    else
	echo "The gdma daemon is partly running.  PIDS:"
	echo $gdmapids
	echo "Processes:"
	pids_list=`echo $gdmapids | sed -e 's/ /,/g'`
	ps -p "$pids_list" -f
	retval=1
    fi
    return $retval
}

# Under AIX, /usr/bin/sh is really a hard link to ksh, and "stop" acts
# as though it were a shell built-in, taking precedence over a function
# definition.  So we have to use a different name for this function.
stop_gdma() {
    retval=0
    gdmapids=`gdma_pgrep '.perl.bin'`
    if [ -n "$gdmapids" ] ; then
	echo "Stopping the gdma daemon (PIDs "$gdmapids") ..."
	gdma_pkill $gdmapids
	retval=$?
    else
	echo "The gdma daemon is already down."
    fi

    return $retval
}

restart_gdma() {
    stop_gdma
    # Give the daemon time to quit before starting up again, or we'll see the old copy before it
    # shuts down and conclude we don't need to start it up again.  We cannot just sleep for some
    # fixed interval, as that is not a reliable way to ensure the daemon went down.
    i=1
    max_checks=6
    while [ $i -le $max_checks ]; do
	gdmapids=`gdma_pgrep '.perl.bin'`
	if [ -n "$gdmapids" ]; then
	    if [ $i -lt $max_checks ]; then
		echo "The GDMA daemons have not shut down yet; will wait ..."
		sleep 2
	    else
		echo "The GDMA daemons have not shut down yet."
	    fi
	else
	    start_gdma restart
	    return $?
	fi
	i=`expr $i + 1`
    done
    echo "Failed to shut down and start up the GDMA daemons."
    return 1
}

reload_gdma() {
    retval=0
    # Note:  Until the script is equipped to handle the HUP signal, this will just stop it.
    trap "" HUP
    gdmapids=`gdma_pgrep '.perl.bin'`
    if [ -n "$gdmapids" ]; then
	# protect against both 0 and 1, just in case ...
	for id in $gdmapids
	do
	    if [ "$id" -ne 0 -a "$id" -ne 1 ]; then
		/usr/bin/kill -HUP $id
		status=$?
		if [ $status -ne 0 ]; then
		    retval=$status
		fi
	    fi
	done
    fi
    return $retval
}

status_poller() {
    pids=`/usr/bin/ps -u $user -o pid= $ps_zone_opt -o args= | fgrep $path_to_perl_binary | fgrep $gwpath/gdma/bin/gdma_poll.pl | nawk "$awk_zone_match"'{print $1}'`
    if [ -n "$pids" ]; then
	return 0
    else
	return 1
    fi
}

status_spooler() {
    # We cannot specify the entire gdma_spool_processor.pl script filename because
    # Solaris truncates the process arguments to 80 characters, which does not allow
    # room for the entire path we are seeking once you realize that the args output
    # also includes the full path to Perl.  But we specify enough here to pretty
    # much guarantee that we're looking at the process we are searching for.
    #
    pids=`/usr/bin/ps -u $user -o pid= $ps_zone_opt -o args= | fgrep $path_to_perl_binary | fgrep $gwpath/gdma/bin/gdma_spool_ | nawk "$awk_zone_match"'{print $1}'`
    if [ -n "$pids" ]; then
	return 0
    else
	return 1
    fi
}

#
# See how we were called.
#

case "$1" in
start)
    start_gdma start
    RETVAL=$?
    ;;
stop)
    stop_gdma
    RETVAL=$?
    ;;
status)
    RETVAL=0
    status_poller
    if [ $? -eq 0 ]; then
	echo "Poller is Running"
    else
	echo "Poller is Not Running"
	RETVAL=1
    fi
    status_spooler
    if [ $? -eq 0 ]; then
	echo "Spooler is Running"
    else
	echo "Spooler is Not Running"
	RETVAL=1
    fi
    ;;
restart)
    restart_gdma
    RETVAL=$?
    ;;
reload)
    reload_gdma
    RETVAL=$?
    ;;
*)
    echo "Usage: $0 {start|stop|status|restart|reload}"
    exit 1
    ;;
esac
exit $RETVAL
