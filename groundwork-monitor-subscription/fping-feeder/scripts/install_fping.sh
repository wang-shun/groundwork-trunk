#!/bin/bash

PATH=/bin:/usr/bin

if [ `/usr/bin/id -u` != 0 ]; then
    echo "You must run this install script as root."
    exit 1
fi

while true; do
    echo ""
    echo "This script installs and enables the fping feeder setup."
    echo -n "Do you wish to proceed [y/n]? "
    read answer
    if [ $answer = n ]; then
	echo ""
	echo "Exiting without making any changes."
	echo ""
	exit 0
    fi
    if [ $answer = y ]; then
	break
    fi
    echo ""
    echo "I don't understand your response; you must answer y or n,"
    echo "or interrupt this script."
    echo ""
done

check_for_local_file() {
    if [ ! -f $1 ]; then
	echo ""
	echo "The $PWD/ directory"
	echo "contains no \"$1\" binary to copy; exiting!"
	echo ""
	exit 1
    fi
}

fping_source=""
# We try to do all our checking up front, to minimize the possibility
# of a partial-install failure.
if [ -f /usr/local/groundwork/common/bin/fping ]; then
    # We're looking for:
    # -rwsr-sr-x  1 root nagios 26488 Jul  6 18:37 /usr/local/groundwork/common/bin/fping
    if [ "`ls -l /usr/local/groundwork/common/bin/fping | gawk '{print $1, $3, $4}'`" == '-rwsr-sr-x root nagios' ]; then
	echo ""
	echo "/usr/local/groundwork/common/bin/fping is already present and will not be replaced."
    else
	echo ""
	echo "/usr/local/groundwork/common/bin/fping is already present but does not have"
	echo "correct ownership (root.nagios) or permissions (-rwsr-sr-x or 6755)."
	echo "You must either move this file aside or correct such problems before"
	echo "this install script will run."
	echo ""
	exit 1
    fi
else
    if [ -f /usr/local/groundwork/common/sbin/fping ]; then
	# Use the copy that gets shipped with GW Monitor 5.1.X or later.
	fping_source=/usr/local/groundwork/common/sbin/fping
    else
	# If it's appropriate (i.e., if we're on the proper platform), use the
	# copy that ships with our wrapper script.  That copy is for RHEL 4, 32-bit.
	if [ -f /etc/redhat-release					\
	    -a `fgrep -c 'release 4' /etc/redhat-release` = 1	\
	    -a `uname -i` = "i386"					\
	    -a -f fping.rhel4.i386 ]; then
	    check_for_local_file fping.rhel4.i386
	    fping_source=$PWD/fping.rhel4.i386
	else
	    # If you've got a local copy here from your own compilation, we'll use that.
	    if [ -f fping ]; then
		fping_source=$PWD/fping
	    else
		echo ""
		echo "We cannot find a copy of fping in any of the standard locations."
		echo "Either interrupt this command, or enter the full path to your own"
		echo "compiled copy of fping:"
		read fullpath
		if [ -z "$fullpath" -o ! -f "$fullpath" ]; then
		    echo ""
		    echo "We cannot find the fping binary as '$fullpath'; exiting!"
		    echo ""
		    exit 1
		fi
		fping_source=$fullpath
	    fi
	fi
    fi
fi
check_for_local_file fping_process.conf
check_for_local_file fping_process.pl

if [ -n "$fping_source" ]; then
    cp -p $fping_source /usr/local/groundwork/common/bin/fping
fi
chown root.nagios   /usr/local/groundwork/common/bin/fping
chmod 6755          /usr/local/groundwork/common/bin/fping

echo ""
echo "The installed fping binary looks like this:"
ls -l /usr/local/groundwork/common/bin/fping
echo ""

overwrite=0
if [ -f /usr/local/groundwork/common/etc/fping_process.conf ]; then
    while true; do
	echo "/usr/local/groundwork/common/etc/fping_process.conf already exists; should we"
	echo -n "overwrite it with the copy in $PWD [y/n]? "
	read answer
	if [ $answer = n ]; then
	    echo ""
	    echo "We'll use the existing copy of /usr/local/groundwork/common/etc/fping_process.conf ."
	    echo ""
	    break
	fi
	if [ $answer = y ]; then
	    overwrite=1
	    old_copy=/usr/local/groundwork/common/etc/fping_process.conf.`date +%F_%H.%M.%S`
	    mv /usr/local/groundwork/common/etc/fping_process.conf $old_copy
	    if [ $? -ne 0 ]; then
	        echo ""
	        echo "Failed to move aside the old copy of /usr/local/groundwork/common/etc/fping_process.conf;"
		echo "exiting!"
	        echo ""
		exit 1
	    else
		echo ""
		echo "The previous copy of /usr/local/groundwork/common/etc/fping_process.conf"
		echo "has been moved to $old_copy ."
		echo ""
	    fi
	    break
	fi
	echo ""
	echo "I don't understand your response; you must answer y or n,"
	echo "or interrupt this script."
	echo ""
    done
else
    overwrite=1
fi
if [ $overwrite -eq 1 ]; then
    cp -p fping_process.conf /usr/local/groundwork/common/etc
fi
chown nagios.nagios      /usr/local/groundwork/common/etc/fping_process.conf
chmod 600                /usr/local/groundwork/common/etc/fping_process.conf

# Install the wrapper script in a location which is appropriate for
# operation as a Nagios plugin.
cp fping_process.pl /usr/local/groundwork/nagios/libexec/
chown nagios.nagios /usr/local/groundwork/nagios/libexec/fping_process.pl
chmod 755           /usr/local/groundwork/nagios/libexec/fping_process.pl

# Install the wrapper script in a location which is appropriate for
# operation as a persistent-daemon service.
cp fping_process.pl /usr/local/groundwork/foundation/feeder/
chown nagios.nagios /usr/local/groundwork/foundation/feeder/fping_process.pl
chmod 755           /usr/local/groundwork/foundation/feeder/fping_process.pl

# Install the service profile.
cp service_profile_fping_feeder.xml /usr/local/groundwork/core/profiles
chown nagios.nagios                 /usr/local/groundwork/core/profiles/service_profile_fping_feeder.xml
chmod 644                           /usr/local/groundwork/core/profiles/service_profile_fping_feeder.xml

while true; do
    echo -n "Do you wish to install a persistent-daemon service [y/n]? "
    read answer
    if [ $answer = n ]; then
	echo ""
	echo "Exiting without installing the feeder-nagios-fping service."
	echo ""
	exit 0
    fi
    if [ $answer = y ]; then
	break
    fi
    echo ""
    echo "I don't understand your response; you must answer y or n,"
    echo "or interrupt this script."
    echo ""
done

# Before we install the service, let's take it for a trial run, so we don't
# install a service that will continually fail as soon as it's installed.
echo ""
echo "Testing the fping_process.pl script ..."
$PWD/fping_process.pl -p
# An exit code of 4 says the script is disabled in the config file,
# so it should be safe to install the service.
if [ $? != 0 -a $? != 4 ]; then
    echo ""
    echo "... fping_process.pl fails; aborting without installing as a service!"
    echo ""
    exit 1
else
    echo "... fping_process.pl test worked; will install as a service."
    echo ""
fi

# Install a persistent-daemon service right now.
cp -R services/* /usr/local/groundwork/core/services

remove_fping_service () {
    rm -rf /usr/local/groundwork/core/services/feeder-nagios-fping/
    pids=`ps -ef | egrep 'fping_process.pl|supervise feeder-nagios-fping' | fgrep -v fgrep | gawk '{print $2}'`
    if [ -n "$pids" ]; then
	kill -TERM $pids > /dev/null 2>&1
    fi
}

echo "Watching the feeder-nagios-fping service ..."
bounced=0
total_loops=0
good_loops=0
old_pid=""
while [ -z "$old_pid" -o $good_loops -lt 3 ]; do
    ((++total_loops))
    if [ $total_loops -gt 7 ]; then
	if [ $bounced -eq 0 ]; then
	    echo ""
	    echo "Bouncing daemons to attempt to start the fping service ..."
	    echo ""
	    /etc/init.d/httpd stop
	    /etc/init.d/snmpttd stop
	    /etc/init.d/gwservices restart
	    /etc/init.d/snmpttd start
	    /etc/init.d/httpd start
	    bounced=1
	    total_loops=0
	    good_loops=0
	    old_pid=""
	else
	    echo ""
	    echo "ERROR:  fping_process.pl is not starting; will remove this service!"
	    remove_fping_service
	    echo ""
	    exit 1
	fi
    fi
    echo ========================================================================
    ps -ef | fgrep fping | fgrep -v fgrep | fgrep -v " vim " | fgrep -v install_fping.sh
    pid=`ps -ef | fgrep fping_process | fgrep -v fgrep | fgrep -v vim | gawk '{print $2}'`
    if [ -n "$pid" -a -n "$old_pid" ]; then
	((++good_loops))
	if [ "$pid" != "$old_pid" ]; then
	    echo ""
	    echo "ERROR:  fping_process.pl is restarting; will remove this service!"
	    remove_fping_service
	    echo ""
	    exit 1
	fi
    fi
    old_pid=$pid
    sleep 3
done

echo ""
echo "Everything is installed successfully."
echo ""
exit 0
