#!/bin/bash -x 
#Copyright (C) 2013  GroundWork Open Source Solutions info@groundworkopensource.com
#
# Building JBoss Enterprise Portal, Enterprise Foundation

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
PORTAL_BUILD_DIR=groundwork-jpp
FOUNDATION_BUILD_DIR=foundation
OS_FOUNDATION_BUILD_DIR=$HOME/build7/OS-foundation
CLOUDHUB_BUILD_DIR=cloudhub

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
            echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "6.4 Build FAILED in  `hostname` - $DATE" build-info@gwos.com
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
            echo "BUILD FAILED: There has been a problem trying to commit groundwork files." | mail -s "6.4 Build FAILED in  `hostname` - $DATE" build-info@gwos.com
            exit 1
        fi
        sleep 30
    done

}

echo "Building Enterprise Foundation with maven 3"
echo "Checkout Foundation Enterprise source code ..."

cd $BUILD_BASE
svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/enterprise-foundation $FOUNDATION_BUILD_DIR

cd $BUILD_BASE/$FOUNDATION_BUILD_DIR
echo "Start building foundation enterprise with maven 3"

mvn clean
mvn install

echo "Foundation Enterprise build with maven 3 is done at `date`"


echo "Starting JBoss Enterprise Portal (JPP) build..."

echo "Checkout EPP and Extension source code ..."
cd $BUILD_BASE

svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/monitor-framework/epp $PORTAL_BUILD_DIR


cd $BUILD_BASE/$PORTAL_BUILD_DIR
mvn clean install

echo "JBoss Enterprise Portal (JPP) build is done at `date`"

#######################################################################################
echo "Patching JBoss portal"

echo "Copy patched jar files...."

rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/wci-jboss7-2.3.0.Final-redhat-1.jar
rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/wci-jboss7-2.3.0.Final-redhat-1.jar.index
rm -f $GW_HOME/jpp/modules/org/gatein/wci/main/module.xml

cp patches/6.0.0-session-invalidation/wci-jboss7/*.jar $GW_HOME/jpp/modules/org/gatein/wci/main
cp patches/6.0.0-session-invalidation/wci-jboss7/module.xml $GW_HOME/jpp/modules/org/gatein/wci/main

rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/module.xml
rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/exo.portal.webui.portal-3.5.2.Final-redhat-4.jar
rm -f $GW_HOME/jpp/modules/org/gatein/lib/main/exo.portal.webui.portal-3.5.2.Final-redhat-4.jar.index

cp patches/6.0.0-session-invalidation/exo.portal.webui.portal/*.jar $GW_HOME/jpp/modules/org/gatein/lib/main/
cp patches/6.0.0-session-invalidation/exo.portal.webui.portal/module.xml $GW_HOME/jpp/modules/org/gatein/lib/main/

#Remoting fix
rm -f $GW_HOME/jpp/modules/org/jboss/remoting3/main/jboss-remoting-3.2.14.GA-redhat-1.jar
cp patches/6.0.0-remote/jboss-remoting-3.2.17.GA-SNAPSHOT.jar $GW_HOME/jpp/modules/org/jboss/remoting3/main/jboss-remoting-3.2.14.GA-redhat-1.jar


echo "Portal patch done"

#######################################################################
# Build the Portal Administration application with the GroundWork changes
#

echo "Building the eXO Admin & rest application with the GroundWork updates for extended Roles attributes .."

cd $BUILD_BASE/$PORTAL_BUILD_DIR/portal-instance-base

echo " UNZIP file that was downloaded by the main project (use the m2 cache)..."
unzip jboss-jpp-6.0.0-src.zip

cp -f custom_scripts/exoadmin/src/main/java/org/exoplatform/organization/webui/component/*.java jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/java/org/exoplatform/organization/webui/component
cp -f custom_scripts/exoadmin/src/main/resources/locale/portlet/exoadmin/*.properties jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/WEB-INF/classes/locale/portlet/exoadmin
cp -f custom_scripts/exoadmin/*.xml jboss-jpp-6.0.0-src/portal/portlet/exoadmin
cp -f custom_scripts/exoadmin/src/main/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl
cp -f custom_scripts/exoadmin/src/main/groovy/portal/webui/application/UIPortlet.gtmpl jboss-jpp-6.0.0-src/portal/web/portal/src/main/webapp/groovy/portal/webui/application/UIPortlet.gtmpl
cp -f custom_scripts/rest/src/main/webapp/WEB-INF/*.xml jboss-jpp-6.0.0-src/portal/web/rest/src/main/webapp/WEB-INF/
cp -f custom_scripts/rest/pom.xml jboss-jpp-6.0.0-src/portal/web/rest/
cd jboss-jpp-6.0.0-src/portal/portlet/exoadmin
mvn clean install -Dgatein.dev -DskipTests
cp target/exoadmin.war $GW_HOME/jpp/gatein/gatein.ear/
cd $BUILD_BASE/$PORTAL_BUILD_DIR/portal-instance-base/jboss-jpp-6.0.0-src/portal/web/rest
mvn clean install -Dgatein.dev -DskipTests
cp target/rest.war $GW_HOME/jpp/gatein/gatein.ear/
rm -rf $BUILD_BASE/$PORTAL_BUILD_DIR/portal-instance-base/jboss-jpp-6.0.0-src

echo "eXo Admin Custom build done at `date`"

#################################################################################################################################
# CloudHub requires Java 6 version of API
#

echo "Java 6 version of Foundation done at `date`"

cd $BUILD_BASE/$FOUNDATION_BUILD_DIR
mvn clean install -P java6

echo "Java 6 version of Foundation done at `date`"


echo "GroundWork Cloud Hub build ..."

cd $BUILD_BASE
svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/monitor-agent/cloudhub $CLOUDHUB_BUILD_DIR

cd $BUILD_BASE/$CLOUDHUB_BUILD_DIR
echo "Start building Cloud Hub ..."

mvn clean
# Tomcat build requires the profile
#mvn install -P tomcat

# Default is JBoss build
mvn install

echo " Upload Cloud Hub to marat file server .."
scp target/cloudhub.war root@morat:/var/www/html/cloudhub/1.1/7.0/

echo "GroundWork Cloud Hub build done at `date`"

###################################################################################################################################

echo "Starting Foundation Enterprise build..."
echo "Source code already checked-out for maven 3 build"

#echo "Checkout Foundation Enterprise source code ..."
#cd $BUILD_BASE

#svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/enterprise-foundation $FOUNDATION_BUILD_DIR

cd $BUILD_BASE/$FOUNDATION_BUILD_DIR
. maven allClean &>/dev/null
. maven allBuild
. maven deploy


echo "Foundation Enterprise build is done at `date`"

# JPP dependency should get the correct collage-api
echo "Copy latest collage api into the JBoss modules structure"
cp -f $BUILD_BASE/$FOUNDATION_BUILD_DIR/collage/api/target/collage-api-*.jar $GW_HOME/jpp/modules/com/groundwork/collage/main/

###################################################################################################################################

echo "Build Foundation API files"
# Build foundation/api

mkdir $OS_FOUNDATION_BUILD_DIR
cd $OS_FOUNDATION_BUILD_DIR
svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation/collage/api

mkdir -p $GW_HOME/foundation/api/perl
mkdir -p $GW_HOME/foundation/api/php
cp -p $OS_FOUNDATION_BUILD_DIR/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
cp -rp $OS_FOUNDATION_BUILD_DIR/api/php/adodb $GW_HOME/foundation/api/php
cp -rp $OS_FOUNDATION_BUILD_DIR/api/php/collageapi $GW_HOME/foundation/api/php
cp -rp $OS_FOUNDATION_BUILD_DIR/api/php/DAL $GW_HOME/foundation/api/php

# Placeholder for bookshelf
mkdir -p $GW_HOME/bookshelf
mkdir -p $GW_HOME/bookshelf/docs


echo "Backup GroundWork common build ..."
rm -rf /usr/local/groundwork-common.ent
mv $GW_HOME /usr/local/groundwork-common.ent




# GWME 7.0.0 will no longer include Bookshelf files
# Check out Bookshelf from subversion
#cd $HOME
#svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf groundwork-bookshelf
#cd $HOME/groundwork-bookshelf
#svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf-data bookshelf-data

# Increment bookshelf-build number
#release=$(fgrep "org.groundwork.rpm.release.number" $BASE_BSH/data-build/project.properties |awk '{ print $3; }')
#new_release=`expr $release + 1`

# Set new bookshelf-build release number
#sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_BSH/data-build/project.properties >  $BASE_BSH/data-build/project.properties.tmp
#mv  $BASE_BSH/data-build/project.properties.tmp  $BASE_BSH/data-build/project.properties

# Commit bookshelf project.properties back to subversion 
#echo "Increment build(release) number" > svnmessage
#svn_commit --username build --password bgwrk08 $BASE_BSH/data-build/project.properties -F svnmessage
#rm -rf svnmessage

# Start master build script
#cd $BASE_BSH
#maven allBuild allDeploy

# Apply patches
#cp -rf $BASE_BSH/patches/* /usr/local/groundwork/docs

# Save Bookshelf release number
#echo "$new_release" > /usr/local/groundwork/bookshelf_release.txt

#echo "Bookshelf build is done at `date`"
################################################################################

echo "Open Source Foundation build was replaced with Enterprise Foundation that was adapted to JBoss Hornet queue"

#echo "Starting Foundation build..."

#cd $HOME
# Check out Foundation from subversion
#svn_co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
#svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation groundwork-monitor/foundation


# Check if any foundaation java or xml file is updated,
# then update Foundation, Framework, and Monitor Portal's build number.
#if [ -f "/root/build/logs/FoundationIsUpdated.txt" ] ; then
  # Increment foundation build number
#  release=$(fgrep "org.groundwork.rpm.release.number" $BASE/foundation/project.properties |awk '{ print $3; }')
#  new_release=`expr $release + 1`

  # Increment foundation OS version
#  OldfoundationOs=$(fgrep "org.groundwork.os.version" $BASE/foundation/project.properties | sed 's/\./ /g' | awk '{ print $6; }')

  # Set new foundation-build release number
#  sed -i 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE/foundation/project.properties 
#  sed -i 's/org.groundwork.os.version=3.0.'$OldfoundationOs'/org.groundwork.os.version=3.0.'$new_release'/' $BASE/foundation/project.properties 

  # Commit foundation project.properties back to subversion
#  echo "Increment build(release) number" > $HOME/svnmessage
#  svn_commit --username build --password bgwrk08 $BASE/foundation/project.properties -F $HOME/svnmessage

  # Cleanup Maven repository from the old jar files
#  find /root/.maven -name *-3.0.*.jar -exec rm -f {} \;

#  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for CE
#  cd $HOME
#  svn_co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
#  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
#  svn_commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage

#  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for EE
#  cd $HOME
#  svn_co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-portal
#  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
#  svn_commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage
#fi



#cd $BASE/foundation
#. maven allClean &>/dev/null
#. maven allBuild

#new_release=$(grep "org.groundwork.rpm.release.number" /home/nagios/groundwork-monitor/foundation/project.properties | awk '{ print $3; }')
#echo "$new_release" > /usr/local/groundwork/foundation_release.txt

#rm -rf $HOME/groundwork-foundation
#rm -rf /usr/local/groundwork-foundation
#cp -rp $HOME/groundwork-monitor $HOME/groundwork-foundation
#cp -rp $GW_HOME /usr/local/groundwork-foundation

#echo "Foundation build is done at `date`"
###############################################################################

echo "JBoss Portal 2.7.2 is replaced with JBoss EPP 5"

#echo "Starting monitor-framwork build for Enterprise"

#cd $BASE
# Check out Framework from core-subversion
#svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework

# Remove core subdirectory
#rm -rf monitor-framework/core/src
#rm -rf monitor-framework/core-identity/src
#rm -rf monitor-framework/core-identity/build.xml

#mkdir $BASE/tmp
#cd $BASE/tmp
#svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-framework

#mv -f $BASE/tmp/monitor-framework/core/src  $BASE/monitor-framework/core

#mv -f $BASE/tmp/monitor-framework/core-identity/src $BASE/monitor-framework/core-identity
#mv -f $BASE/tmp/monitor-framework/core-identity/build.xml $BASE/monitor-framework/core-identity

#mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-core-sar/conf/data/default-object.xml $BASE/monitor-framework/core/src/resources/portal-core-sar/conf/data
#mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-server-war/login.jsp $BASE/monitor-framework/core/src/resources/portal-server-war

#
# GWMON-8114 Make sure that the local.properties for the Enterprise build is included
#
#mv -f $BASE/tmp/monitor-framework/build/local.properties $BASE/monitor-framework/build

#cd $BASE/monitor-framework/build
#ant -f build-gwportal.xml deploy

# Build foundation/api
#mkdir -p $GW_HOME/foundation/api/perl
#mkdir -p $GW_HOME/foundation/api/php
#cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
#cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

#rm -rf $HOME/groundwork-common.ent
#rm -rf /usr/local/groundwork-common.ent
#mv $BASE $HOME/groundwork-common.ent
#mv $GW_HOME /usr/local/groundwork-common.ent

#echo "Jboss Portal build fot Ent is done at `date`"
###############################################################################
#echo "Starting monitor-framwork build for CE"

#cp -rp $HOME/groundwork-foundation $HOME/groundwork-monitor
#cp -rp /usr/local/groundwork-foundation $GW_HOME

#cd $BASE
# Check out Framework from pro-subversion
#svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework

# GWMON-7345
# Update title page
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/default-dashboard/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/generic/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/svlayout/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/3columns/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/1column/index.jsp

#cd $BASE/monitor-framework/build
#ant -f build-gwportal.xml deploy

# Build foundation/api
#mkdir -p $GW_HOME/foundation/api/perl
#mkdir -p $GW_HOME/foundation/api/php
#cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
#cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

#rm -rf $HOME/groundwork-common.ce
#rm -rf /usr/local/groundwork-common.ce
#mv $BASE $HOME/groundwork-common.ce
#mv $GW_HOME /usr/local/groundwork-common.ce

#echo "Jboss Portal build fot CE is done at `date`"
################################################################################

# Bookshelf, Fundation, and Framework build is done.
# 32bit build server can start the build now.
#ssh horw touch /root/build/logs/start_32bit

date
echo "CommonBuild.sh is done."
