
#!/bin/bash -x
#Copyright (C) 2014  GroundWork Inc. info@groundworkopensource.com
#
# Main build script to build GroundWork Enterprise product
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

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

# Check distro
if [ -f /etc/redhat-release ] ; then
  RHEL_NO=$(fgrep "release" /etc/redhat-release | awk '{ print $7; }' | sed 's/\./ /g' | awk '{ print $1; }')
  distro=rhel5
  builddir=redhat
  qadir=RH
  machinearch=i386
elif [ -f /etc/SuSE-release ] ; then
  RHEL_NO=$(fgrep "VERSION" /etc/SuSE-release | awk '{ print $3; }' | sed 's/\./ /g' | awk '{ print $1; }')
  distro=rhel5
  builddir=packages
  qadir=SUSE
  machinearch=i586
elif [ -f /etc/mandrake-release ] ; then
  distro='Mandrake'
  builddir=
  echo "Please set the build directory in the buildRPM.sh file ..."
  exit 1
fi

qasubdir=32bit
bitrock_os=32
arch=$(arch)
if [ "$arch" == "x86_64" ] ; then
  export ARCH='_64'
  libdir=lib64
  qasubdir=64bit
  machinearch=x86_64
  bitrock_os=64
else
  libdir=lib
fi

PATH=$PATH:$HOME/bin
export BR_HOME=/home/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export RELEASE=$distro$ARCH
export DATE=$(date +%Y-%m-%d)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/build
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/home/build
INSTALLBUILDER_VERSION=17.9.0

BitRockDir=/opt/installbuilder-$INSTALLBUILDER_VERSION
BUILD_SCRIPT_DIR=$RUN_DIR/build7/GWMON-$GWMEE_VERSION

PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

# CEBuild script loads Bitrock package on system, Still needs to be called for with pro-option
date
# Call nightly to build Monitor CE
cd $BUILD_SCRIPT_DIR
. CEBuild.sh pro PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH


date
# Call nightly to build Monitor Pro
cd $BUILD_SCRIPT_DIR
. EntBuild.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH

# Generate the GroundWork build number in a central place using the product-version.properties file in
# the branch root. The file contains all product version and should become the central place for maintaining
# product version across all builds
###############################
# Start Build number generation

# Check-out 
mkdir $BASE/version-gen
cd $BASE/version-gen
svn_co -N $SVN_CREDENTIALS $PRO_ARCHIVE/

cd $PRO_ARCHIVE_BRANCH

br_release=$(fgrep "<version>" $BR_HOME/project.xml | sed -e 's/.*-br\([0-9]*\)-.*/\1/')

release=$(fgrep "GroundWork_Monitor_build" $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties |awk '{ print $3; }')

if [ "$release" == "" ]; then
    echo "BUILD FAILED: There was an error trying to get the version number from the product-version.properties file. Please check the $BASE_OS/project.properties file." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
    exit 1
fi

# Increment release number
new_release=`expr $release + 1`

old_release=$(fgrep "GroundWork_Monitor_build" $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties |awk '{ print $3; }')

# Set new core-build release number
sed -e 's/GroundWork_Monitor_build = '$old_release'/GroundWork_Monitor_build = '$new_release'/' $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties >  $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties.tmp1
mv  $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties.tmp1  $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties


# Commit project.properties back to subversion
echo "Increment build(release) number" > svnmessage
svn commit $SVN_CREDENTIALS $BASE/version-gen/$PRO_ARCHIVE_BRANCH/product-version.properties -F svnmessage

rm -rf svnmessage

#Cleanup
cd $BASE
rm -rf $BASE/version-gen


# Update Bitrock package number with full version
echo "Update Bitrock version GW full version number and gw build sequence number...."
sed -e '/^\s*<version>/s/-gwXXX/-gw'$new_release'/; /^\s*<version>/s/<version>\s*[0-9.]*-br/<version>'$GWMEE_FULL_VERSION'-br/' $BR_HOME/project.xml > $BR_HOME/project.xml.tmp2
mv -f $BR_HOME/project.xml.tmp2 $BR_HOME/project.xml

# Update build properties
grep -v core $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo core=$GWMEE_FULL_VERSION-br$br_release-gw$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-core.xml | sed 's/name="core_build" value="/name="core_build" value="$GWMEE_FULL_VERSION-br'$br_release'-gw'$new_release'/' > $BR_HOME/groundwork-core.xml.tmp
mv -f $BR_HOME/groundwork-core.xml.tmp $BR_HOME/groundwork-core.xml


# End Build number generation
#############################
 

# Build package using BitRock Builder
echo "Invoking Bitrock's installbuilder program to create the installer ..."
$BitRockDir/bin/builder build $BR_HOME/project.xml --license $BitRockDir/license.xml
echo "Done running Bitrock's installbuilder program."

NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/ //g')

# For use in printing end-of-build build statistics:
NewBuildComponents=$(echo $NewBuildNumber | sed "s/$GWMEE_FULL_VERSION-//")

# Validate output
echo "Listing of Install Builder output files for the current release ..."
ls -l $BitRockDir/output/groundworkenterprise-$GWMEE_FULL_VERSION-*


NewPackage=groundworkenterprise-$NewBuildNumber-linux-$bitrock_os-installer.run
NewPackagePath=$BitRockDir/output/$NewPackage

echo "New package=$NewPackage"
MoratDir=/var/www/html/tools/DEVELOPMENT

grep -v "grep" $RUN_DIR/logs/build.log | egrep "BUILD FAIL|Failed to execute goal"
GREP_ERROR=$?

if [ $GREP_ERROR -eq 0 ]; then
  # FIX MAJOR:  The i/o redirection here is occurring on our build machine, not on morat.
  # Based on the scp commands below that reference the EE_Revision file in the $MoratDir
  # (why don't we use that variable here, too?), that doesn't look like what was intended.
  ssh root@morat echo "error" >> /var/www/html/tools/DEVELOPMENT/logs/EE_Revision
  grep -v "grep" $RUN_DIR/logs/build.log | egrep -A 15 -B 45 "BUILD FAILED|Failed to execute goal" | mail -s "$GWMEE_FULL_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
  exit -1
elif [ -f $NewPackagePath ] ; then
    $NewPackagePath --version | mail -s "$GWMEE_FULL_VERSION Build OK in `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
else
    echo "Unknown error:  $NewPackagePath does not exist" | mail -s "$GWMEE_FULL_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
    exit -1
fi

# For use in printing end-of-build build statistics.  Commify the size to make it more readable.
NewPackageSize=$(ls -l $NewPackagePath | perl -e '$_=(split(/\s+/,<STDIN>))[4];s/(^[-+]?\d+?(?=(?>(?:\d{3})+)(?!\d))|\G\d{3}(?=\d))/$1,/g;print;')

# Copy package into morat
ssh root@morat rm -f $MoratDir/builds/BitRock/6.0/groundworkenterprise-$GWMEE_FULL_VERSION-*-linux-$bitrock_os-installer.run
ssh root@morat mv $MoratDir/builds/BitRock/last_good_build/groundworkenterprise-$GWMEE_FULL_VERSION-*-linux-$bitrock_os-installer.run /tmp
check_scp -p $NewPackagePath root@morat:$MoratDir/builds/BitRock/6.0
check_scp -p $NewPackagePath root@morat:$MoratDir/builds/BitRock/last_good_build
check_scp -p $NewPackagePath root@morat:/BitRock/$qasubdir

# FIX MAJOR:  This next line looks really suspicious.  Why are we copying a fixed "6.6.0" release level when we should
# probably be referring to the current build release instead?  Why are we copying a .bin file when we create .run files now?
ssh root@morat $MoratDir/builds/BitRock/comparer-linux.bin /tmp/groundworkenterprise-$GWMEE_FULL_VERSION-*-linux-$bitrock_os-installer.run $MoratDir/builds/BitRock/last_good_build/groundworkenterprise-6.6.0-*-linux-$bitrock_os-installer.bin > $RUN_DIR/logs/comparer-EE-$bitrock_os.log
check_scp -p $RUN_DIR/logs/comparer-EE-$bitrock_os.log root@morat:$MoratDir/logs
ssh root@morat rm -f /tmp/groundworkenterprise-$GWMEE_FULL_VERSION-*-linux-$bitrock_os-installer.run

if [ "$arch" == "x86_64" ] ; then
  check_scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs
  check_scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs/$qasubdir
fi


# Delete old package
OldPackage=$(cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $1; }')
ssh root@morat rm -f /BitRock/$qasubdir/$OldPackage
# Make a new list of packages
cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14}' > $RUN_DIR/logs/PackList.$bitrock_os.tmp
NewPackageList=$(cat $RUN_DIR/logs/PackList.$bitrock_os.tmp)
echo "$NewPackageList $NewPackage" > $RUN_DIR/logs/PackList.$bitrock_os

date
echo "nightlyBuildEnt.sh is done..."
