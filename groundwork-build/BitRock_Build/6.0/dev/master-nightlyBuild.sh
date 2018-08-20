#!/bin/bash -x
#Copyright (C) 2008  GroundWork Open Source Solutions info@groundworkopensource.com
#

# Get the date for today's directory
export DATE=$(date +%d-%m-%y)
RUN_DIR=/root/build

date
cd $RUN_DIR
echo "Start Building Pro using BitRock Installer"
. nightlyBuildBitRockPro.sh
tar zcf $RUN_DIR/BitRock/groundwork-pro.tar.gz $RUN_DIR/BitRock/groundwork
date

# Update Groundwork Stack
grep -v groundwork-[fmb] $RUN_DIR/BitRock/groundwork/groundwork-linux-*.txt > /tmp/groundwork-linux-versions.txt
ssh 172.28.113.161 grep 64 /root/build/BitRock/groundwork/groundwork-linux-x64-* >> /tmp/groundwork-linux-versions.txt
cat $RUN_DIR/BitRock/groundwork/build.properties | sed 's/=/ /' | sed 's/bookshelf/groundwork-bookshelf/' | sed 's/foundation/groundwork-foundation/' | sed 's/core/groundwork-monitor-core/' | sed 's/pro/groundwork-monitor-pro/' >> /tmp/groundwork-linux-versions.txt
ssh 172.28.113.161 grep core /root/build/BitRock/groundwork/build.properties |  sed 's/core/groundwork-monitor-core-x86_64/' | sed 's/=/ /' >> /tmp/groundwork-linux-versions.txt
scp -rp /tmp/groundwork-linux-versions.txt root@morat:/var/www/html/builds/BitRock/groundwork-linux-versions.txt
scp -rp /tmp/groundwork-linux-versions.txt root@morat:/var/www/html/builds/BitRock/opensource_version/groundwork-linux-versions.txt.$NewBuildNumber

scp -rp $RUN_DIR/BitRock/groundwork-pro.tar.gz root@morat:/var/www/html/builds/groundwork-pro-$NewBuildNumber-$bitrock_os.tar.gz

# Delete old tar files
OldTarFile=$(cat $RUN_DIR/logs/TarFileList.$bitrock_os | awk '{ print $1; }')
ssh root@morat rm -f /var/www/html/builds/$OldTarFile
# Make a new list of tar files
cat $RUN_DIR/logs/TarFileList.$bitrock_os | awk '{ print $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14}' > $RUN_DIR/logs/TarFileList.$bitrock_os.tmp
NewTarFileList=$(cat $RUN_DIR/logs/TarFileList.$bitrock_os.tmp)
echo "$NewTarFileList groundwork-pro-$NewBuildNumber-$bitrock_os.tar.gz" > $RUN_DIR/logs/TarFileList.$bitrock_os

date

echo "master-nightlyBuild.sh is done..."

