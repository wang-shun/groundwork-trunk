#!/bin/bash -x
#Copyright (C) 2013  GroundWork Open Source Solutions info@groundworkopensource.com
#
# Master build script invoked by a crontab to build GroundWork Enterprise
#

# Get the date for today's directory
export DATE=$(date +%Y-%m-%d)
RUN_DIR=/home/build
LOGDIR=$RUN_DIR/logs
BUILD_SCRIPTS=$RUN_DIR/build7/GWMON-7.0.0
MoratDir=/var/www/html/tools/DEVELOPMENT

arch=$(arch)
if [ "$arch" == "x86_64" ] ; then
  bitrock_os=64
else
  bitrock_os=32
fi

# Make a list of updated files
#$RUN_DIR/UpdatedFiles/logs/svn_updated.sh

date
# Build Common modules
cd $BUILD_SCRIPTS
echo "Start Building Common modules including Bookshelf, Foundation, and Framework"
. CommonBuild.sh

date
# Build Ntop files
cd $BUILD_SCRIPTS
echo "Build the Ntop tree"
./PrepareNtopBuild.sh

date
# Build NeDi files
cd $BUILD_SCRIPTS
echo "Build the NeDi tree from distribution and patch files"
./PrepareNeDiBuild.sh

date
# Build Cacti files
cd $BUILD_SCRIPTS
echo "Build the Cacti tree from distribution and patch files"
./PrepareCactiBuild.sh

date
# Build Weathermap files
cd $BUILD_SCRIPTS
echo "Build the Weathermap tree from distribution and patch files"
./PrepareWeathermapBuild.sh

date
# Build Monitor Enterprise Edition
cd $BUILD_SCRIPTS
echo "Start Building EE using BitRock Installer"
. nightlyBuildEE.sh

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


date
echo "master-nightlyBuild.sh is done..."
