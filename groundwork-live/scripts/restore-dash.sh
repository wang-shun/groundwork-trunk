#!/bin/bash 
# restore old dashboards after you made new ones with gen-dash.sh
source /usr/local/groundwork/scripts/setenv.sh
replied=

echo "restoring dashboards"
echo "proceed? (y/n)"
read replied
if [ $replied != "y" ]; then
echo "nothing done"
exit
fi

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
