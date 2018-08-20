#!/bin/bash 

# delete the hosts that were added so you can try again without too much trouble
# delete the hostgroups too
# assumes that the HOSTS and ALLGROUPS files exist from that attempt
# format of HOSTS to be name alias address hostprofile with tabs
# format of HOSTGROUPS to be hostgroup name
# We are only using the hostname and hostgroupname ...

source /usr/local/groundwork/scripts/setenv.sh
echo "Please enter the postgres password you assigned on installation of GW"
read replied
export PGPASSWORD=$replied
./del_host.pl -f ../input/$1/HOSTS
./del_hostgroup.pl -f ../input/$1/ALLGROUPS
./commit_to_nagios.pl
`chown nagios.nagios /usr/local/groundwork/core/monarch/workspace/*`	
`chown nagios.nagios /usr/local/groundwork/nagios/etc/*`	
`chown nagios.nagios /usr/local/groundwork/foundation/container/logs/monarch_foundation_sync.log`


# Remove the cron job to submit the results regularly
touch /var/spool/cron/root
sed -e 's/.*submit_demo_state.*//' -i /var/spool/cron/root

if [ ! -d save-files ] ; then 
    echo "can not locate save-files, are you sure you ran gen-dash?"
    exit
fi

/etc/init.d/groundwork stop gwservices

cp save-files/default-object.xml /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/conf/data/default-object.xml
cp save-files/portal-statusviewer.war /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-statusviewer.war
cp save-files/portal-groundwork-base.war /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-groundwork-base.war

dropdb jbossportal
createdb jbossportal
psql -f /usr/local/groundwork/core/databases/postgresql/postgres-xtra-functions.sql jbossportal
pg_restore -d jbossportal -F t -c save-files/jbossportal_backup.sql.tar

/etc/init.d/groundwork start gwservices
