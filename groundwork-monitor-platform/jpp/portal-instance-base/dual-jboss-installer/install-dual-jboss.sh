#!/bin/bash

#####################################################################################
#
#    Simple script to convert single-JBoss to dual-JBoss for a GWMEE 7.x.x system.
#
#    Copyright (c) 2013-2014 GroundWork Inc. (www.groundworkopensource.com)
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#	$Id: install.sh 11/26/2013 Arul Shanmugam$
#	(and subsequently modified)
#
#####################################################################################

# Setup environment
JPP_1=/usr/local/groundwork/foundation/container/jpp
JPP_2=/usr/local/groundwork/foundation/container/jpp2
collageurl=$(grep -Po "(?<=^collage.url=).*" /usr/local/groundwork/config/db.properties)

# Allow only root execution
if [ `id|sed -e s/uid=//g -e s/\(.*//g` -ne 0 ]; then 
    program=${0##*/}
    echo "ERROR:  This script ($program) requires root privileges."
    exit 1  
fi

# Unattended mode called from the installer
if [ "x$1" = "x--unattended" ]; then
  echo "Dual JBoss installation starting..."
else

    # Validation for valid version
    valid_version=$GROUNDWORK_VERSION
    version=$(grep -Po "(?<=^version= ).*" /usr/local/groundwork/Info.txt)
    if [ $version != $valid_version ]; then
        echo "GroundWork version $version detected.  This script can only be used on a $valid_version system!"
        exit 1
    fi

    backup_tool_pattern='/usr/local/groundwork/gw-backup-br*-linux-*'
    backup_tool=`ls $backup_tool_pattern | tail -1`
    if [ -z "$backup_tool" ]; then
        echo "ERROR:  No full-system backup tool ($backup_tool_pattern) is available."
        exit 1
    fi

    Backupfile=/usr/local/groundwork/jpp/dual-jboss-installer/$version.single-jboss.backup.tgz

    while true; do
        echo "********************************************************************************"
        echo "WARNING #1:  Though a PARTIAL backup will be taken, and put here:"
        echo ""
        echo "    $Backupfile"
        echo ""
        echo "it is strongly recommended you take a FULL backup before converting to"
        echo "a dual-JBoss setup!  To take a full backup, exit this installation and run"
        echo "this program (with additional arguments, depending on your local situation):"
        echo ""
        echo "    $backup_tool"
        echo ""
        echo "See https://kb.groundworkopensource.com/display/SUPPORT/Backup+utility for more"
        echo "information on running this tool, including the proper command-line arguments."
        echo ""
        echo "WARNING #2:  The GroundWork server will be restarted during this installation."
        echo "This installation might take several minutes, depending on your system."
        echo "********************************************************************************"
        read -p "Do you wish to continue (y/n)? " yn
        case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit 1;;
        * ) echo "Please answer yes or no.";;
        esac
    done

    # Check if dual jboss is already installed
    if [ -d "$JPP_2" ]; then
        echo ""
        echo "Dual-JBoss setup detected.  To use this script, you should be running"
        echo "GroundWork $valid_version in single-JBoss configuration.  Exiting!"
        exit 1
    fi

    # Validation for local or remote database. Replacing postgres settings for local database only.
    collageurl=$(grep -Po "(?<=^collage.url=).*" /usr/local/groundwork/config/db.properties)
    echo ""
    echo "INFO:  Collage URL is: " $collageurl
    if [[ "$collageurl" =~ "localhost" ]]
    then
        echo "Local Database.  Continuing ..."
    else
        echo "Remote Database setup detected!"
        while true; do
        echo "************************************************************************"
        echo "NOTICE:  In a remote database setup, before running this installer,
        echo "ssh to the remote database server, then edit"
        echo "    /usr/local/groundwork/postgresql/data/postgresql.conf"
        echo "and change the following settings to:
        echo "    max_connections = 160"
        echo "    shared_buffers = 4096MB"
        echo "and restart postgresql!"
        echo "************************************************************************"
        read -p "Do you wish to continue this installation (y/n)? " yn
        case $yn in
            [Yy]* ) break;;
            [Nn]* ) exit 1;;
            * ) echo "Please answer yes or no.";;
        esac
        done
    fi

    echo ""
    echo "Creating $Backupfile as a backup file."
    echo "This is NOT a full backup ..."
    tar czvf $Backupfile										\
        /usr/local/groundwork/foundation/container/jpp/standalone/configuration/standalone.xml	\
        /usr/local/groundwork/apache2/conf/httpd.conf						\
        /usr/local/groundwork/config/status-viewer.properties					\
        /usr/local/groundwork/config/console.properties						\
        /usr/local/groundwork/config/foundation.properties						\
        /usr/local/groundwork/config/ws_client.properties						\
        /usr/local/groundwork/foundation/container/jpp/standalone/configuration/check-listener.conf	\
        /usr/local/groundwork/foundation/container/jpp/bin/standalone.conf				\
        /usr/local/groundwork/postgresql/data/postgresql.conf
    if [ $? -ne 0 ]; then
        echo ""
        echo "ERROR:  Partial backup failed.  GroundWork JBoss configuration"
        echo "        has not been changed.  Exiting!"
        exit 1
    fi

    echo ""
    echo "Stopping GroundWork ..."
    /etc/init.d/groundwork stop

fi

echo ""
echo "Creating a second JBoss EAP for Foundation ..."

# There ought not to be any deployment marker files lying around in $JPP_1 while gwservices is down,
# but just in case there are, we get rid of all of them in $JPP_2 during this copying.  Any marker
# files that we actually need in $JPP_2 will be placed there dynamically by our startup scripting.

# GWMON-12789
#shopt -s nullglob
#cp -r $JPP_1 $JPP_2
#rm -rf $JPP_2/gatein
#rm -rf $JPP_2/modules
#rm -rf $JPP_2/standalone/log/*
#rm -rf $JPP_2/standalone/tmp
#rm -rf $JPP_2/standalone/deployments/*.{war,ear}.{dodeploy,skipdeploy,isdeploying,deployed,failed,isundeploying,undeployed,pending}
#rm -rf $JPP_2/standalone/data/*
#rm -rf $JPP_2/webapps
#rm -rf $JPP_2/standalone/configuration/check-listener.conf
#ln -s $JPP_1/modules $JPP_2/modules
#find $JPP_2/standalone/deployments/ ! -name foundation-webapp.war ! -name legacy-rest.war -name "*.war" -delete
#find $JPP_2/standalone/deployments/ ! -name foundation-webapp.war ! -name legacy-rest.war -name "*.ear" -delete
#cp -f /usr/local/groundwork/jpp/dual-jboss-installer/standalone.xml  $JPP_1/standalone/configuration/
#cp -f /usr/local/groundwork/jpp/dual-jboss-installer/standalone2.xml $JPP_2/standalone/configuration/standalone.xml
#ln -s $JPP_2 /usr/local/groundwork/jpp2
#shopt -u nullglob

shopt -s nullglob

# create the target jpp2 directory if it doesn't exist
if [ ! -d $JPP_2 ]; then
    mkdir $JPP_2 
    chown nagios:nagios $JPP_2 
fi

# get into the source directoty for the tar copy 
pushd $JPP_1 >> /dev/null 

# tar pipeline copy with exclusions pipeline with exclusions for things not required, eg massive piles of logs
tar --exclude=gatein \
    --exclude=modules \
    --exclude=standalone/log \
    --exclude=standalone/tmp \
    --exclude=standalone/data \
    --exclude=standalone/webapps \
    --exclude=standalone/configuration/check-listener.conf \
    -cf - . | (cd $JPP_2 && tar xf - )

rm -rf $JPP_2/standalone/deployments/*.{war,ear}.{dodeploy,skipdeploy,isdeploying,deployed,failed,isundeploying,undeployed,pending}
ln -s $JPP_1/modules $JPP_2/modules
find $JPP_2/standalone/deployments/ ! -name foundation-webapp.war ! -name legacy-rest.war -name "*.war" -delete
find $JPP_2/standalone/deployments/ ! -name foundation-webapp.war ! -name legacy-rest.war -name "*.ear" -delete
cp -f /usr/local/groundwork/jpp/dual-jboss-installer/standalone.xml  $JPP_1/standalone/configuration/
cp -f /usr/local/groundwork/jpp/dual-jboss-installer/standalone2.xml $JPP_2/standalone/configuration/standalone.xml
ln -s $JPP_2 /usr/local/groundwork/jpp2
popd >> /dev/null
shopt -u nullglob

echo "Moving files into place ..."
rm -rf $JPP_1/modules/com/groundwork/security/main/groundwork-jboss-security-$valid_version{,-SNAPSHOT}.jar.index
mv $JPP_2/bin/standalone.sh $JPP_2/bin/standalone2.sh
mv $JPP_1/standalone/deployments/foundation-webapp.war $JPP_2/standalone/deployments/
mv $JPP_1/standalone/deployments/legacy-rest.war       $JPP_2/standalone/deployments/
rm -rf $JPP_1/standalone/deployments/foundation-webapp*
rm -rf $JPP_1/standalone/deployments/legacy-rest*
chown -hR nagios.nagios $JPP_2

## Remove the obsolete supervise run tree.  (It should not be present in the
## 7.0.2 release in the first place, so this removal is just a precautionary
## holdover from installation under the 7.0.1 release.)
if [ -d "/usr/local/groundwork/core/services/foundation" ]; then
    echo "Removing obsolete service files ..."
    rm -rf /usr/local/groundwork/core/services/foundation
fi

## Add the new supervise run tree that supports the separate JVM for Foundation.
if [ ! -d "/usr/local/groundwork/core/services/service-foundation" ]; then
    echo "Adding a new service-foundation service ..."
    su - nagios /usr/local/groundwork/common/bin/mkservice nagios nagios            /usr/local/groundwork/core/services/service-foundation
    cp -p /usr/local/groundwork/jpp/dual-jboss-installer/service-foundation.run     /usr/local/groundwork/core/services/service-foundation/run
    cp -p /usr/local/groundwork/jpp/dual-jboss-installer/service-foundation.log.run /usr/local/groundwork/core/services/service-foundation/log/run
fi

echo "Updating the new JBoss remoting port and new Foundation ports in the config files ..."
sed -i 's/4447/4547/g' /usr/local/groundwork/config/status-viewer.properties
sed -i 's/4447/4547/g' /usr/local/groundwork/config/console.properties
sed -i 's/4447/4547/g' /usr/local/groundwork/config/foundation.properties
sed -i 's/localhost:8080\/foundation-webapp/localhost:8180\/foundation-webapp/g' /usr/local/groundwork/config/status-viewer.properties
sed -i 's/localhost:8080\/foundation-webapp/localhost:8180\/foundation-webapp/g' /usr/local/groundwork/config/console.properties
sed -i 's/localhost:8080\/foundation-webapp/localhost:8180\/foundation-webapp/g' /usr/local/groundwork/apache2/conf/httpd.conf
sed -i 's/localhost:8080\/legacy-rest/localhost:8180\/legacy-rest/g'             /usr/local/groundwork/apache2/conf/httpd.conf
sed -i 's/9999/10099/g' $JPP_2/bin/jboss-cli.xml
sed -i 's/localhost:8080\/foundation-webapp/localhost:8180\/foundation-webapp/g' /usr/local/groundwork/config/ws_client.properties
sed -e 's/jpp\/standalone\/deployments\/foundation-webapp.war/jpp2\/standalone\/deployments\/foundation-webapp.war/g'	\
    -e 's/jpp\/standalone\/deployments\/legacy-rest.war/jpp2\/standalone\/deployments\/legacy-rest.war/g'		\
    -i $JPP_1/standalone/configuration/check-listener.conf

echo "Adjusting the JVM Heap size ..."
sed -i 's/-Xms1024m -Xmx2048m/-Xms2048m -Xmx4096m/g' $JPP_1/bin/standalone.conf
sed -i 's/-Xms1024m -Xmx2048m/-Xms2048m -Xmx4096m/g' $JPP_2/bin/standalone.conf

echo "Adding portal JOSSO configuration system properties ..."
GATEIN_SSO_JOSSO_PROPERTIES_FILE=file:/usr/local/groundwork/foundation/container/jpp/standalone/configuration/gatein/configuration.properties

#GATEIN_SSO_JOSSO_PROPERTIES_FILE=$(grep "^gatein.sso.josso.properties.file=" /usr/local/groundwork/config/configuration.properties | sed -e 's/^.*= *//' | sed -e 's/[$]{jboss.home.dir}/\/usr\/local\/groundwork\/foundation\/container\/jpp/')
sed -i '/JAVA_OPTS="$JAVA_OPTS.*-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true/a \ \ \ JAVA_OPTS="$JAVA_OPTS -Dgatein.sso.josso.properties.file='${GATEIN_SSO_JOSSO_PROPERTIES_FILE}'"' $JPP_2/bin/standalone.conf

## Perform PostgreSQL tuning only for a local database.
if [[ "$collageurl" =~ "localhost" ]]
then
    echo "Performing PostgreSQL tuning (only for a local database) ..."
    sed -i 's/max_connections = 100/max_connections = 160/g'    /usr/local/groundwork/postgresql/data/postgresql.conf
    sed -i 's/shared_buffers = 240MB/shared_buffers = 4096MB/g' /usr/local/groundwork/postgresql/data/postgresql.conf
fi

echo "Changing ports for all reports ..."
find /usr/local/groundwork/ -name "*.rptdesign" -exec sed -i 's/localhost:8080\/foundation-webapp/localhost:8180\/foundation-webapp/g' {} \;

# Unattended mode called from the installer
if [ "x$1" = "x--unattended" ]; then
  echo "Services will be started by installer ..."
else
    echo ""
    echo "Starting GroundWork ..."
    /etc/init.d/groundwork start

    echo ""
    echo "**************************************************************************************************************************"
    echo "DUAL-JBOSS CONVERSION IS SUCCESSFUL AND THE SYSTEM IS READY TO USE!"
    echo ""
    echo "INFO:  Foundation http port has been moved from 8080 to 8180.  Any custom scripts accessing Foundation"
    echo "       (using SOAP/REST) via localhost:8080 must be manually changed to localhost:8180 instead."
    echo "       If accessing via hostname:80 they should be fine."
    echo "INFO:  Portal log4j config:        $JPP_1/standalone/configuration/standalone.xml"
    echo "INFO:  Portal logs:                $JPP_1/standalone/log/framework.log"
    echo "INFO:  Foundation log4j config:    $JPP_2/standalone/configuration/standalone.xml"
    echo "INFO:  Foundation logs:            $JPP_2/standalone/log/framework.log"
    echo "INFO:  JOSSO log4j config:         /usr/local/groundwork/josso-1.8.4/conf/logging.properties"
    echo "INFO:  JOSSO authentication logs:  /usr/local/groundwork/josso-1.8.4/logs/catalina.out"
    echo "**************************************************************************************************************************"
fi
