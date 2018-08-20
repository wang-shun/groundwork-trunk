#!/bin/bash 

# version 1.1 2012-05-02 added error checks and look for pg password
# version 1.3 2013-01-15 work with 6.7.0-6 ear file
# generate new dashboards from list(s) of HostGroups

source /usr/local/groundwork/scripts/setenv.sh

echo "Please enter the postgres password you assigned on installation of GW"
read replied
export PGPASSWORD=$replied

# create the top level lists of hosts in APP, ALLGEO and WKGRP
# These are input to creating three kinds of Dashboard. If there is not content in these (no hostgroups)
# then we do not generate dashboard

cat ../input/$1/GEO{1,2} | egrep -ve "#"  > ../input/$1/ALLGEO
cat ../input/$1/APPS | egrep -ve "#" > ../input/$1/temp-APPS
mv ../input/$1/temp-APPS ../input/$1/APPS
cat ../input/$1/WKGRP | egrep -ve "#" > ../input/$1/temp-WKGRP
mv ../input/$1/temp-WKGRP ../input/$1/WKGRP

# we make copies of the original files here so that the dashboard creation process can be re run many times from the same source material
# the source is in "save-files"
# Note if you are doing this on a pre 6.7 machine, you might have to edit the 5 xml files in "save-files" to place the marker
# for additions
# Note "layouts.tar" contains 6.7 layouts

if [ ! -d save-files ] ; then 
    echo "has not been run from this directory previously, copying the war and xml files to holding area" >> /tmp/GWLive.log

    mkdir save-files
    mkdir save-files/GB
    mkdir save-files/SV
    mkdir save-files/EA
    mkdir tmp
    mkdir tmp/GB
    mkdir tmp/SV
    mkdir tmp/DF
    mkdir tmp/EA

    tar -czf save-files/old-layouts.tar.gz /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/layouts /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/WEB-INF/portal-layouts.xml
    cp -a /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/conf/data/default-object.xml /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/{portal-groundwork-base,portal-statusviewer}.war  save-files
    pg_dump -f save-files/jbossportal_backup.sql.tar -F t -c -E LATIN1 jbossportal

    cp -a -f /usr/local/groundwork/foundation/container/webapps/groundwork-enterprise-6.7.ear tmp/groundwork-enterprise-6.7-ORIG.ear
    cp -a /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-groundwork-base.war tmp/portal-groundwork-base-ORIG.war
    cp -a /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/conf/data/default-object.xml tmp

    echo "created working build directories for ea (sv) and gb war files in tmp, one time" >> /tmp/GWLive.log
    echo "saved the jbossportal database for later restore in save-files" >> /tmp/GWLive.log
    echo " to restore the system to the state it is in now use the restore-dash.sh script provided" >> /tmp/GWLive.log
    echo " do not remove or alter the files in tmp or save-files" >> /tmp/GWLive.log

# we only do this once because we want the as installed set of xml for future recreation of the whole set
# we always start at the same place.

    cd tmp/GB; jar -xf ../portal-groundwork-base-ORIG.war; cd ../.. 
    cd tmp/EA; jar -xf ../groundwork-enterprise-6.7-ORIG.ear; mv portal-statusviewer-6.7.war ../portal-statusviewer-6.7-ORIG.war;  cd ../..
    cd tmp/SV; jar -xf ../portal-statusviewer-6.7-ORIG.war; cd ../.. 

    cp tmp/GB/WEB-INF/portlet*.xml save-files/GB
    cp tmp/SV/WEB-INF/portlet*.xml save-files/SV
    cp tmp/default-object.xml save-files

# if this happens to be a 6.6.1 or earlier machine the following will put a new set of layouts in place that are needed

#    if [ ! -e /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/layouts/RaceToTheTop/index.jsp ] ; then
#        tar -xvf ../input/$1/layouts.tar -C /
#    fi
fi

# makes GroundWork Base Portal war xml changes., using the save-files set as starting point
# note this is not a cumulative process, we are beginning from scratch each time

echo "Depending on DNS issues you may want to supply localhost in answer to the next question for purposes of the dashboard NagVis Views assignment"
read replied
if [ -z "$replied" ] ; then
replied=`hostname`
fi
echo "Using $replied in the NagVis maps in Geographic Dash Portlets"
sed -e s/localhost/$replied/g ../input/$1/GB-portlet-core > ../input/$1/GB-portlet-core.modified
./gen-gb.pl save-files/GB/portlet-instances.xml ALLGEO tmp/GB/WEB-INF/portlet-instances.xml GB-portlet-instances-core ../input/$1/ '<!-- Custom Addition Starts -->'
./gen-gb.pl save-files/GB/portlet.xml ALLGEO tmp/GB/WEB-INF/portlet.xml GB-portlet-core.modified ../input/$1/ '<!-- Custom Addition Starts -->'

# create the default-objects.xml file

./gen-def.pl save-files/default-object.xml DEF tmp/default-object.xml ../input/$1/ '<!-- Custom Addition Starts -->'

# create the Status View war file

./gen-sv.pl save-files/SV/portlet.xml SV tmp/SV/WEB-INF/portlet.xml ../input/$1/ '<!-- Custom Addition Starts -->'
./gen-sv.pl save-files/SV/portlet-instances.xml SVI tmp/SV/WEB-INF/portlet-instances.xml ../input/$1/ '<!-- Custom Addition Starts -->'

/etc/init.d/groundwork stop gwservices

# place the default-objects.xml in the right spot
# repack the archives for gb and sv

mv tmp/default-object.xml /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/conf/data
cd tmp/GB; jar -cf ../portal-groundwork-base.war *; cd ../..
cd tmp/SV; jar -cf ../portal-statusviewer-6.7.war *; mv ../portal-statusviewer-6.7.war ../EA/; cd ../..
cd tmp/EA; jar -cf ../groundwork-enterprise-6.7.ear *; cd ../..

# copy the new war  and ear file into place

cp -a tmp/portal-groundwork-base.war /usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-groundwork-base.war
cp -a tmp/groundwork-enterprise-6.7.ear /usr/local/groundwork/foundation/container/webapps/

# we drop and recreate the jbossportal database.
# Note this means you lose any previously created users, roles, and dashboards that are not a part of the original install
# In short we are creating a new "fresh install" with new dashboards as a base

dropdb jbossportal
if [[ $? -ne 0 ]]; then 
echo "failed to drop jbossportal database" >> /tmp/GWLive.log
cat /tmp/GWLive.log
exit
fi

createdb jbossportal
if [[ $? -ne 0 ]]; then 
echo "failed to create jbossportal database" >> /tmp/GWLive.log
cat /tmp/GWLive.log
exit
fi

psql -f /usr/local/groundwork/core/databases/postgresql/postgres-xtra-functions.sql jbossportal
if [[ $? -ne 0 ]]; then 
echo "failed to populate jbossportal database" >> /tmp/GWLive.log
cat /tmp/GWLive.log
exit
fi

/etc/init.d/groundwork start gwservices
exit
