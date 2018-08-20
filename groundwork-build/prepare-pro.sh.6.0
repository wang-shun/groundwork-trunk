#!/bin/bash
# Obsolete script
#Copyright (C) 2004-2008  GroundWork Open Source Solutions info@groundworkopensource.com

# Check distro
if [ -f /etc/redhat-release ] ; then
        distro='rhel4'
        builddir=redhat
elif [ -f /etc/SuSE-release ] ; then
        VERSION=$(/bin/fgrep "VERSION" /etc/SuSE-release|awk '{print $3}')
        distro='sles'$VERSION
        builddir=packages
elif [ -f /etc/mandrake-release ] ; then
        distro='Mandrake'
        builddir=
echo "Plese set build directory in buildRPM.sh file..."
        exit 1
fi

# Set up build home directory
HOME=/home/nagios

# Clean up previous build
cd $HOME
rm -rf groundwork-monitor

# Check out groundwork-opensource 
svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
cd $HOME/groundwork-monitor
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/bronx
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/build
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/guavachat
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/images
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monarch
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-os
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/nightlybuild
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/reports
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/sv2tests
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/template
cd $HOME

# Copy groundwork-professional delta files into build directory
cp -r groundwork-professional/* groundwork-monitor/

# Remove closing </project> tag from maven.xml files 
sed -e 's/<\/project>//g'  groundwork-monitor/foundation/maven.xml > groundwork-monitor/foundation/maven.xml.tmp
mv groundwork-monitor/foundation/maven.xml.tmp groundwork-monitor/foundation/maven.xml

sed -e 's/<\/project>//g' groundwork-monitor/foundation/collagefeeder/adapters/maven.xml > groundwork-monitor/foundation/collagefeeder/adapters/maven.xml.tmp
mv groundwork-monitor/foundation/collagefeeder/adapters/maven.xml.tmp groundwork-monitor/foundation/collagefeeder/adapters/maven.xml

# Append groundwork-professional delta goals into maven.xml files
cat groundwork-monitor/foundation/maven-pro.xml >> groundwork-monitor/foundation/maven.xml
cat groundwork-monitor/foundation/collagefeeder/adapters/maven-pro.xml >> groundwork-monitor/foundation/collagefeeder/adapters/maven.xml

# Clean up delta files
rm -rf groundwork-monitor/foundation/maven-pro.xml
rm -rf groundwork-monitor/foundation/collagefeeder/adapters/maven-pro.xml
