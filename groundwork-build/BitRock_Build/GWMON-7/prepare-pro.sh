#!/bin/bash
# Obsolete script
# Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

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
#HOME=/home/nagios
HOME=/home/build

# Clean up previous build
cd $HOME
rm -rf groundwork-monitor

# Check out groundwork-opensource 
PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

# FIX MAJOR:  How is it that we are able to check out code from the
# PRO repository without supplying any credentials?

# Check out the top-level Maven-related files that used to live at the top level of our OS repository.
svn co -N $PRO_ARCHIVE/archive-groundwork-monitor groundwork-monitor

cd $HOME/groundwork-monitor
svn co $PRO_ARCHIVE/monitor-core/bronx
svn co $PRO_ARCHIVE/build
svn co $PRO_ARCHIVE/images
svn co $PRO_ARCHIVE/monarch
svn co $PRO_ARCHIVE/monitor-core
svn co $PRO_ARCHIVE/monitor-os
svn co $PRO_ARCHIVE/reports


#Foundation files
mkdir $HOME/groundwork-monitor/foundation
mkdir $HOME/groundwork-monitor/foundation/collage
mkdir $HOME/groundwork-monitor/foundation/collage/database
mkdir $HOME/groundwork-monitor/foundation/misc
mkdir $HOME/groundwork-monitor/foundation/collagefeeder

cd $HOME/groundwork-monitor/foundation/collage/database
svn co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/collage/database/seed

cd $HOME/groundwork-monitor/foundation/misc
svn co $PRO_ARCHIVE//monitor-platform/enterprise-foundation/misc/web-application

cd $HOME/groundwork-monitor/foundation/
svn co $PRO_ARCHIVE//monitor-platform/enterprise-foundation/resources

# Feeders are coming from monitor-platform
#cd $HOME/groundwork-monitor/foundation/collagefeeder
#svn co $PRO_ARCHIVE//monitor-platform/enterprise-foundation/collagefeeder/scripts


cd $HOME

# Copy groundwork-professional delta files into build directory
cp -r groundwork-professional/* groundwork-monitor/

# Remove closing </project> tag from maven.xml files 
## sed -e 's/<\/project>//g'  groundwork-monitor/foundation/maven.xml > groundwork-monitor/foundation/maven.xml.tmp
## mv groundwork-monitor/foundation/maven.xml.tmp groundwork-monitor/foundation/maven.xml

## sed -e 's/<\/project>//g' groundwork-monitor/foundation/collagefeeder/adapters/maven.xml > groundwork-monitor/foundation/collagefeeder/adapters/maven.xml.tmp
## mv groundwork-monitor/foundation/collagefeeder/adapters/maven.xml.tmp groundwork-monitor/foundation/collagefeeder/adapters/maven.xml

# Append groundwork-professional delta goals into maven.xml files
## cat groundwork-monitor/foundation/maven-pro.xml >> groundwork-monitor/foundation/maven.xml
## cat groundwork-monitor/foundation/collagefeeder/adapters/maven-pro.xml >> groundwork-monitor/foundation/collagefeeder/adapters/maven.xml

# Clean up delta files
## rm -rf groundwork-monitor/foundation/maven-pro.xml
## rm -rf groundwork-monitor/foundation/collagefeeder/adapters/maven-pro.xml
