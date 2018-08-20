#!/bin/bash -x 
#Copyright (C) 2016  GroundWork Open Source Solutions info@groundworkopensource.com
#
# Building JBoss Enterprise Portal, Enterprise Foundation

# BE SURE TO CHANGE THIS FOR A NEW GROUNDWORK MONITOR RELEASE NUMBER!
# This is a version number corresponding to the directory in which this
# script resides (e.g., GWMON-7 for the 7.1.1 release).
GWMEE_VERSION=7
BUILD_MAIL_ADDRESSES="build-info@gwoslabs.com"

# Set this to reflect the Subversion credentials we need to commit files.
SVN_CREDENTIALS="--username build --password bgwrk08"

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

echo "Starting common build at `date`"

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

PATH=$PATH:$HOME/bin
export GW_HOME=/usr/local/groundwork
#export JAVA_HOME=$(which java|sed 's/\/bin\/java//')
#export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
#export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')

#export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/build
#BASE_BSH=$HOME/groundwork-bookshelf
#BASE=$HOME/groundwork-monitor

BUILD_BASE=$HOME/build7
PORTAL_BUILD_DIR=monitor-platform
FOUNDATION_BUILD_DIR=foundation
OS_FOUNDATION_BUILD_DIR=$HOME/build7/OS-foundation
CLOUDHUB_BUILD_DIR=cloudhub
PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

check_chdir () {
    dir="$1"
    if ! cd $dir ; then
	echo "BUILD FAILED: There was an error trying to change to $dir as the current working directory." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_pushd () {
    dir="$1"
    if ! pushd $dir ; then
        echo "BUILD FAILED: There was an error trying to change to $dir as the current working directory." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
        exit 1
    fi
}

check_popd () {
    if ! popd ; then
        echo "BUILD FAILED: There was an error trying to popd." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
        exit 1
    fi
}

check_mkdir () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    dir="${!#}"
    if ! mkdir "$@"; then
	echo "BUILD FAILED: There was an error trying to create the $dir directory." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_cp () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /bin/cp "$@"; then
	echo "BUILD FAILED: There was an error trying to copy to the $target location." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_mv () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /bin/mv "$@"; then
	echo "BUILD FAILED: There was an error trying to mv to the $target location." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_mvn () {
    export MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
    if ! mvn "$@"; then
	echo "BUILD FAILED: There was an error trying to run Maven." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# Clean up previous Bookshelf, Foundation, and JBoss builds
rm -rf $GW_HOME/*
#rm -rf $BASE_BSH
rm -rf $BASE
#ssh horw rm -f /root/build/logs/start_32bit


# Cleanup of EPP an Foundation Enterprise build directories
rm -rf $BUILD_BASE/$PORTAL_BUILD_DIR
rm -rf $BUILD_BASE/$FOUNDATION_BUILD_DIR
rm -rf $OS_FOUNDATION_BUILD_DIR

# Cleanup of local maven repo's 
echo "cleanup of local maven repo's (m1 and m2)"

rm -rf /home/build/.maven/repository/org.itgroundwork
rm -rf /home/build/.maven/repository/com.groundworkopensource.portal

rm -rf /home/build/.m2/repository/com/groundwork/collage-api


# Checkout function
svn_co () {
    for i in 1 0; do
        svn co $1 $2 $3 $4 $5 $6
        SVN_EXIT_CODE=$?
        if [ $SVN_EXIT_CODE -eq 0 ]; then
            break;
        elif [ $i -eq 0 ]; then
            echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "GWMON-$GWMEE_VERSION Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
            exit 1
        fi
        sleep 30
    done

}

# SVN commit function
svn_commit () {
    for i in 1 0; do
        svn commit $1 $2 $3 $4 $5 $6 $7
        SVN_EXIT_CODE=$?
        if [ $SVN_EXIT_CODE -eq 0 ]; then
            break;
        elif [ $i -eq 0 ]; then
            echo "BUILD FAILED: There has been a problem trying to commit groundwork files." | mail -s "GWMON-$GWMEE_VERSION Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
            exit 1
        fi
        sleep 30
    done

}


# All Java builds have been centralized in monitor-platform which simplifies the 
# builds.
echo "Starting JBoss Enterprise Portal (JPP) build ..."


echo "Checkout monitor-platform (Foundation, JPP, Applications, Agents) source code ..."
check_chdir $BUILD_BASE

# Development repository
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-platform $PORTAL_BUILD_DIR

# Release TAG for that will be used for the RELEASE  GWME 7.1.0
#svn_co http://geneva/groundwork-professional/tags/MONITOR-PLATFORM-RELEASE-7.1.0 $PORTAL_BUILD_DIR


check_chdir $BUILD_BASE/$PORTAL_BUILD_DIR
check_mvn clean install

# Place the built jpp directory at GW_HOME for post-processing
mv jpp/portal-instance-base/target/jpp $GW_HOME
mv jpp/portal-instance-base/target/josso-1.8.4 $GW_HOME

echo "Move Multi-instance setup scripts into the JPP folder."
mv jpp/portal-instance-base/target/dual-jboss-installer $GW_HOME/jpp

# Copy separate-foundation-JVM service start/stop scripting into the same location where the
# rest of the dual-jboss installation support resides at this point in the build process.
check_cp -p jpp/portal-instance-base/custom_scripts/groundwork-services/service-foundation.log.run $GW_HOME/jpp/dual-jboss-installer
check_cp -p jpp/portal-instance-base/custom_scripts/groundwork-services/service-foundation.run     $GW_HOME/jpp/dual-jboss-installer

#echo "JBoss Enterprise Portal (JPP) build is done at `date`"

#######################################################################################
echo "Patching JBoss portal"

echo "Copy patched jar files...."

rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/wci-jboss7-2.3.0.Final-redhat-1.jar
rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/wci-jboss7-2.3.0.Final-redhat-1.jar.index
rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/module.xml

check_cp jpp/patches/6.0.0-session-invalidation/wci-jboss7/*.jar $GW_HOME/jpp/modules/org/gatein/wci/main
check_cp jpp/patches/6.0.0-session-invalidation/wci-jboss7/module.xml $GW_HOME/jpp/modules/org/gatein/wci/main

rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/module.xml
rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/exo.portal.webui.portal-3.5.2.Final-redhat-4.jar
rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/exo.portal.webui.portal-3.5.2.Final-redhat-4.jar.index

check_cp jpp/patches/6.0.0-session-invalidation/exo.portal.webui.portal/*.jar $GW_HOME/jpp/modules/org/gatein/lib/main/
check_cp jpp/patches/6.0.0-session-invalidation/exo.portal.webui.portal/module.xml $GW_HOME/jpp/modules/org/gatein/lib/main/

#Remoting fix
rm -f $GW_HOME/jpp/modules/org/jboss/remoting3/main/jboss-remoting-3.2.14.GA-redhat-1.jar
check_cp jpp/patches/6.0.0-remote/jboss-remoting-3.2.17.GA-SNAPSHOT.jar $GW_HOME/jpp/modules/org/jboss/remoting3/main/jboss-remoting-3.2.14.GA-redhat-1.jar


echo "Portal patch done"

#######################################################################
# Build the Portal Administration application with the GroundWork changes
#

echo "Building the eXO Admin & rest application with the GroundWork updates for extended Roles attributes .."

check_chdir jpp/portal-instance-base

echo " UNZIP file that was downloaded by the main project (use the m2 cache)..."
unzip target/jboss-jpp-6.0.0-src.zip

check_cp -f custom_scripts/exoadmin/src/main/java/org/exoplatform/organization/webui/component/*.java jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/java/org/exoplatform/organization/webui/component
check_cp -f custom_scripts/exoadmin/src/main/resources/locale/portlet/exoadmin/*.properties jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/WEB-INF/classes/locale/portlet/exoadmin
check_cp -f target/pom.xml jboss-jpp-6.0.0-src/portal/portlet/exoadmin
#cp -f custom_scripts/exoadmin/*.xml jboss-jpp-6.0.0-src/portal/portlet/exoadmin
check_cp -f custom_scripts/exoadmin/src/main/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl
check_cp -f custom_scripts/exoadmin/src/main/groovy/portal/webui/application/UIPortlet.gtmpl jboss-jpp-6.0.0-src/portal/web/portal/src/main/webapp/groovy/portal/webui/application/UIPortlet.gtmpl
check_cp -f custom_scripts/rest/src/main/webapp/WEB-INF/*.xml jboss-jpp-6.0.0-src/portal/web/rest/src/main/webapp/WEB-INF/
check_cp -f custom_scripts/rest/pom.xml jboss-jpp-6.0.0-src/portal/web/rest/

#######################################################################################################################
# Build and install sso-josso-plugin
#

echo "Building and installing sso-josso-plugin ..."
check_pushd custom_scripts/josso/sso-josso-plugin
check_mvn clean install
JOSSO_LIBS=$GW_HOME/josso-1.8.4/webapps/josso/WEB-INF/lib
RH_JAR=sso-josso-plugin-1.3.1.Final-redhat-3.jar
GW_JAR=sso-josso-plugin-1.3.1.Final-groundwork-1.jar
check_cp target/$GW_JAR $JOSSO_LIBS
# Set sso-josso-plugin jar aside to avoid conflict
check_mv $JOSSO_LIBS/$RH_JAR $JOSSO_LIBS/$RH_JAR.original
check_popd
echo "sso-josso-plugin build done at `date`"

#######################################################################################################################
# Adjust GroundWork artifact version
#
echo "These new lines are setting the GroundWork artifact versions used to patch the portal REST build"
GROUNDWORK_VERSION=$(/bin/grep '<version>' pom.xml | /usr/bin/head -n 1 | /bin/sed -e 's/^.*<version>//;s/<\/version>.*$//')
/bin/sed -ie "s/\$GROUNDWORK_VERSION/${GROUNDWORK_VERSION}/" jboss-jpp-6.0.0-src/portal/web/rest/pom.xml
/bin/sed -ie "s/\$GROUNDWORK_VERSION/${GROUNDWORK_VERSION}/" jboss-jpp-6.0.0-src/portal/portlet/exoadmin/pom.xml

check_chdir jboss-jpp-6.0.0-src/portal/portlet/exoadmin
check_mvn clean install -Dgatein.dev -DskipTests
check_cp target/exoadmin.war $GW_HOME/jpp/gatein/gatein.ear/
check_chdir $BUILD_BASE/$PORTAL_BUILD_DIR/jpp/portal-instance-base/jboss-jpp-6.0.0-src/portal/web/rest
check_mvn clean install -Dgatein.dev -DskipTests
check_cp target/rest.war $GW_HOME/jpp/gatein/gatein.ear/
rm -rf $BUILD_BASE/$PORTAL_BUILD_DIR/jpp/portal-instance-base/jboss-jpp-6.0.0-src

echo "eXo Admin Custom build done at `date`"

#################################################################################################################################
# Web application & Configuration DEPLOYMENT
#
echo "Deploy Web applications and configuration files ..." 
check_chdir $BUILD_BASE/$PORTAL_BUILD_DIR/gw-config
chmod +x config-deployment.sh
./config-deployment.sh $GW_HOME/config $GW_HOME/jpp/standalone/deployments


#################################################################################################################################
# CloudHub DEPLOYMENT
#
check_chdir $BUILD_BASE/$PORTAL_BUILD_DIR
echo " Upload Cloud Hub to morat file server .."
scp agents/cloudhub/target/cloudhub.war root@morat:/var/www/html/cloudhub/1.3

echo "GroundWork Cloud Hub deployment done at `date`"

###################################################################################################################################

echo "Deploy all Postgres database files ..."

check_mkdir $GW_HOME/databases
check_mkdir $GW_HOME/databases/postgresql

check_cp -rp $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/database/schema/postgresql/* $GW_HOME/databases/postgresql
###### Now overwrite the GWCollage-Version.sql as it has the latest schema version updated by maven 3 
check_cp -rp $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/database/target/classes/GWCollage-Version.sql $GW_HOME/databases/postgresql

###################################################################################################################################
echo "Deploy Feeder files ..."

check_mkdir $GW_HOME/foundation
check_mkdir $GW_HOME/foundation/feeder
check_mkdir $GW_HOME/foundation/scripts

check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/check-listener.pl          $GW_HOME/foundation/feeder
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/nagios2collage_eventlog.pl $GW_HOME/foundation/feeder
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/nagios2collage_socket.pl   $GW_HOME/foundation/feeder
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/find_cacti_graphs          $GW_HOME/foundation/feeder
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/cacti_feeder.pl            $GW_HOME/foundation/feeder
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collagefeeder/scripts/nedi_feeder.pl             $GW_HOME/foundation/feeder

check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/resources/reset_passive_check.sh $GW_HOME/foundation/scripts
echo "Creating jasypt scripts.."
check_cp -f $BUILD_BASE/$PORTAL_BUILD_DIR/jpp/portal-instance-base/custom_scripts/jasypt/bin/encrypt.sh $GW_HOME/foundation/scripts
chmod +x $GW_HOME/foundation/scripts/encrypt.sh

####################################################################################################################################
echo "Build Foundation API files"

check_mkdir -p $GW_HOME/foundation/api/perl
check_mkdir -p $GW_HOME/foundation/api/php

check_cp -p  $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
check_cp -rp $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
check_cp -rp $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
check_cp -rp $BUILD_BASE/$PORTAL_BUILD_DIR/enterprise-foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

###############################################################################################################################
echo "Migration scripts for web applications"

check_mkdir -p $GW_HOME/migration
check_mkdir -p $GW_HOME/migration/cloudhub
check_mkdir -p $GW_HOME/migration/ldap

check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/monitor-apps/nagvis/migration/migrate-nagvis* $GW_HOME/migration
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/agents/cloudhub/cloudhub-configs.sh $GW_HOME/migration/cloudhub
check_cp -p $BUILD_BASE/$PORTAL_BUILD_DIR/jpp/portal-instance-base/custom_scripts/ldap-sync-prepare.sh $GW_HOME/migration/ldap

# Placeholder for bookshelf
check_mkdir -p $GW_HOME/bookshelf
check_mkdir -p $GW_HOME/bookshelf/docs


echo "Backup GroundWork common build ..."
rm -rf /usr/local/groundwork-common.ent
mv $GW_HOME /usr/local/groundwork-common.ent


date
echo "CommonBuild.sh is done."
