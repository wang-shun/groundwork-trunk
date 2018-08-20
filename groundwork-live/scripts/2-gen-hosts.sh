#!/bin/bash

# generate the control files from the Excel spreadsheet

./get-worksheets.pl $1.xls ../input/$1/ 

# Keep a reference copy of the "hosts" file

if [[ ! -f ../input/$1/hosts-CLEAN ]] ; then
	cp /etc/hosts ../input/$1/hosts-CLEAN
fi
if [[ $2 == "active" ]] ; then
	echo "You are creating ACTIVE hosts with what should be REAL addresses. Is this correct?"
	read replied
	if [ $replied != "y" ]; then
		echo "nothing done"
		exit
	fi
fi
# generate the HOSTS HOST-HOSTGROUPS and ALLGROUPS files and then use them to add those entities to Monarch 

./gen.pl ../input/$1/ $2
if [ -f ../input/$1/ERROR ] ; then
	echo "error, stopping now"
	rm -f ../input/$1/ERROR
	exit
fi

# add the new objects to Monarch

./add_hostgroup.pl -f ../input/$1/ALLGROUPS
./add_host.pl -f ../input/$1/HOSTS
./assign_hostgroup.pl -f ../input/$1/HOST-HOSTGROUPS

# assemble service list
# disable all active service checks by hostgroup
# perform commit

./create_hosts_services_results.pl ../input/$1/
# If there is no second argument "active" then the content is to be PASSIVE and we disable the checks by hostgroup
# we also create the FAIL file for a nifty rotating outage
if [ $2 == "passive" ] ; then
	echo $2
	./disable_checks.pl ../input/$1/
	`grep snmp_hpux_disk_root ../input/$1/SERVICES | sed -e 's/0\tOK status is good | value = 50;60;70/2\tCRITICAL FAILING | value = 0;0;0/' > ../input/$1/FAIL`
else 
	echo did no see $2
fi
./commit_to_nagios.pl
`chown -R nagios.nagios /usr/local/groundwork/core/monarch/workspace/ /usr/local/groundwork/nagios/etc/`
`chown nagios.nagios /usr/local/groundwork/foundation/container/logs/monarch_foundation_sync.log`

# Create a cron job to submit the results regularly
# remove any existing so as not to duplicate
# cron job proceeds with updates every 10 minutes

# If there is no second argument "active" then the content is to be PASSIVE and we start the scheduled job 
if [ $2 == "passive" ] ; then
	touch /var/spool/cron/root
	sed -e 's/.*submit_demo_state.*//' -i /var/spool/cron/root
	sed -e 's/.*submit_error.*//' -i /var/spool/cron/root
	echo "*/10 * * * * `pwd`/submit_demo_state.pl `pwd`/../input/$1/" >> /var/spool/cron/root
	echo "11-22,41-52 * * * * `pwd`/submit_error.pl `pwd`/../input/$1/" >> /var/spool/cron/root
fi
exit
