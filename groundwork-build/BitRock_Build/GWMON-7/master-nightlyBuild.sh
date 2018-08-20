#!/bin/bash -x
#Copyright (C) 2014  GroundWork Open Source Solutions info@groundworkopensource.com
#
# Master build script invoked by a crontab to build GroundWork Enterprise
#

# BE SURE TO CHANGE THIS FOR A NEW GROUNDWORK MONITOR RELEASE NUMBER!
# This is a version number corresponding to the directory in which this
# script resides (e.g., GWMON-7 for the 7.1.0 release).
GWMEE_VERSION=7
GWMEE_FULL_VERSION=7.2.2
BUILD_MAIL_ADDRESSES="build-info@gwoslabs.com"

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

SCRIPT_START_TIME=`date +%s`

# Get the date for today's directory
export DATE=$(date +%Y-%m-%d)
RUN_DIR=/home/build
LOGDIR=$RUN_DIR/logs
BUILD_SCRIPTS=$RUN_DIR/build7/GWMON-$GWMEE_VERSION
MoratDir=/var/www/html/tools/DEVELOPMENT

arch=$(arch)
if [ "$arch" == "x86_64" ] ; then
  bitrock_os=64
else
  bitrock_os=32
fi

post_to_slack() {
    test -x /usr/bin/slackpost && /usr/bin/slackpost $@
}

# Make a list of updated files
#$RUN_DIR/UpdatedFiles/logs/svn_updated.sh

post_to_slack "Starting a build of $GWMEE_FULL_VERSION"

COMMON_BUILD_START_TIME=`date +%s`
date
# Build Common modules
cd $BUILD_SCRIPTS
echo "Start Building Common modules including Bookshelf, Foundation, and Framework"
. CommonBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
COMMON_BUILD_END_TIME=`date +%s`

NTOP_BUILD_START_TIME=`date +%s`
date
# Build Ntop files
cd $BUILD_SCRIPTS
echo "Build the Ntop tree"
./PrepareNtopBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
NTOP_BUILD_END_TIME=`date +%s`

NEDI_BUILD_START_TIME=`date +%s`
date
# Build NeDi files
cd $BUILD_SCRIPTS
echo "Build the NeDi tree from distribution and patch files"
./PrepareNeDiBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
NEDI_BUILD_END_TIME=`date +%s`

CACTI_BUILD_START_TIME=`date +%s`
date
# Build Cacti files
cd $BUILD_SCRIPTS
echo "Build the Cacti tree from distribution and patch files"
./PrepareCactiBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
CACTI_BUILD_END_TIME=`date +%s`

WEATHERMAP_BUILD_START_TIME=`date +%s`
date
# Build Weathermap files
cd $BUILD_SCRIPTS
echo "Build the Weathermap tree from distribution and patch files"
./PrepareWeathermapBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
WEATHERMAP_BUILD_END_TIME=`date +%s`

NIGHTLY_BUILD_START_TIME=`date +%s`
date
# Build Monitor Enterprise Edition
cd $BUILD_SCRIPTS
echo "Start Building EE using BitRock Installer"
. nightlyBuildEE.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH
NIGHTLY_BUILD_END_TIME=`date +%s`

CLEANUP_START_TIME=`date +%s`

NewBuildNumber=$(grep "<version>" $RUN_DIR/BitRock/groundwork/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/ //g')
# Make a backup of BitRock package directory
cd $RUN_DIR/BitRock
# Backup of build files fill up disk on morat. No need to store it forever
#tar zcf groundwork-ee.tar.gz groundwork
#scp -rp groundwork-ee.tar.gz root@morat:/var/www/html/builds/groundwork-ee-$NewBuildNumber-$bitrock_os.tar.gz

# Update Groundwork Stack
#grep -v groundwork-[fmb] $RUN_DIR/BitRock/groundwork/groundwork-linux-*.txt > /tmp/groundwork-linux-versions.txt
#echo "jetty 6.1.4" >> /tmp/groundwork-linux-versions.txt
#echo "axis 1.4" >> /tmp/groundwork-linux-versions.txt
#echo "springframework 2.0" >> /tmp/groundwork-linux-versions.txt
#echo "BIRT 2.2.1.r22x_v20070924" >> /tmp/groundwork-linux-versions.txt
#echo "hibernate 3.2.0.ga" >> /tmp/groundwork-linux-versions.txt
#echo "Sendpage 1.001" >> /tmp/groundwork-linux-versions.txt

#ssh horw "grep mysql $RUN_DIR/BitRock/groundwork/groundwork-linux-*.txt | grep linux" >> /tmp/groundwork-linux-versions.txt
cat $RUN_DIR/BitRock/groundwork/build.properties | sed 's/=/ /' | sed 's/bookshelf/groundwork-bookshelf/' | sed 's/foundation/groundwork-foundation/' | sed 's/core/groundwork-monitor-core/' | sed 's/pro/groundwork-monitor-pro/' >> /tmp/groundwork-linux-versions.txt
#ssh horw "grep core /root/build/BitRock/groundwork/build.properties | sed 's/core/groundwork-monitor-core-x86_64/' | sed 's/=/ /'" >> /tmp/groundwork-linux-versions.txt
scp -rp /tmp/groundwork-linux-versions.txt root@morat:$MoratDir/builds/BitRock/groundwork-linux-versions.txt


# Delete old tar files
OldTarFile=$(cat $RUN_DIR/logs/TarFileList.$bitrock_os | awk '{ print $1, $2 }')
ssh root@morat rm -f /var/www/html/builds/$OldTarFile
# Make a new list of tar files
cat $RUN_DIR/logs/TarFileList.$bitrock_os | awk '{ print $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20 }' > $RUN_DIR/logs/TarFileList.$bitrock_os.tmp
NewTarFileList=$(cat $RUN_DIR/logs/TarFileList.$bitrock_os.tmp)
echo "$NewTarFileList groundwork-ce-$NewBuildNumber-$bitrock_os.tar.gz groundwork-ee-$NewBuildNumber-$bitrock_os.tar.gz" > $RUN_DIR/logs/TarFileList.$bitrock_os

CLEANUP_END_TIME=`date +%s`

SCRIPT_END_TIME=`date +%s`

duration() {
    (( duration = $@ ))
    string=""
    if (( $duration > 60 * 60 * 24 )); then
        (( days = $duration / 86400 ))
        string="$days dy"
        (( duration = $duration % 86400 ))
    fi
    if (( $duration > 60 * 60 )); then
        (( hours = $duration / 3600 ))
        string="$string $hours hr"
        (( duration = $duration % 3600 ))
    fi
    if (( $duration > 60 )); then
        (( minutes = $duration / 60 ))
        string="$string $minutes min"
        (( duration = $duration % 60 ))
    fi
    if (( $duration != 0 )); then
        string="$string $duration sec"
    fi
    if [ -z "$string" ]; then
        string="No time at all."
    fi
    echo $string
}

timestamp() {
    perl -e "print scalar localtime($1)";
}

    COMMON_BUILD_DURATION=`duration     $COMMON_BUILD_END_TIME -     $COMMON_BUILD_START_TIME`
      NTOP_BUILD_DURATION=`duration       $NTOP_BUILD_END_TIME -       $NTOP_BUILD_START_TIME`
      NEDI_BUILD_DURATION=`duration       $NEDI_BUILD_END_TIME -       $NEDI_BUILD_START_TIME`
     CACTI_BUILD_DURATION=`duration      $CACTI_BUILD_END_TIME -      $CACTI_BUILD_START_TIME`
WEATHERMAP_BUILD_DURATION=`duration $WEATHERMAP_BUILD_END_TIME - $WEATHERMAP_BUILD_START_TIME`
   NIGHTLY_BUILD_DURATION=`duration    $NIGHTLY_BUILD_END_TIME -    $NIGHTLY_BUILD_START_TIME`
         CLEANUP_DURATION=`duration          $CLEANUP_END_TIME -          $CLEANUP_START_TIME`
          SCRIPT_DURATION=`duration           $SCRIPT_END_TIME -           $SCRIPT_START_TIME`

SCRIPT_START_TIMESTAMP=`timestamp $SCRIPT_START_TIME`
  SCRIPT_END_TIMESTAMP=`timestamp   $SCRIPT_END_TIME`

# FIX LATER:  We'd like breakdowns of the following aspects, which apply across certain components:
# * code checkout time
# * code build time
# * code copy time
# * code merge time
# * code upload time

# FIX LATER:  Add the various statistics to an RRD file, so we can graph the build performance
# over long periods of time.

echo "Build Statistics:
 GroundWork Monitor version:  $GWMEE_FULL_VERSION
           Build components:  $NewBuildComponents
                 Build size:  $NewPackageSize bytes
           Build start time:  $SCRIPT_START_TIMESTAMP
             Build end time:  $SCRIPT_END_TIMESTAMP
      Common build duration:  $COMMON_BUILD_DURATION
        Ntop build duration:  $NTOP_BUILD_DURATION
        NeDi build duration:  $NEDI_BUILD_DURATION
       Cacti build duration:  $CACTI_BUILD_DURATION
  Weathermap build duration:  $WEATHERMAP_BUILD_DURATION
     Nightly build duration:  $NIGHTLY_BUILD_DURATION
     Final cleanup duration:  $CLEANUP_DURATION
Total build script duration:  $SCRIPT_DURATION
" | mail -s "$GWMEE_FULL_VERSION Build Statistics for `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
post_to_slack "Completed build of $GWMEE_FULL_VERSION $NewBuildComponents"

date
echo "master-nightlyBuild.sh is done ..."
