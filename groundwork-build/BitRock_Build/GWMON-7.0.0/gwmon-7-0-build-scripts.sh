#!/bin/bash -x

# Copyright (c) 2009-2013 GroundWork Open Source Solutions info@groundworkopensource.com

date

#Build main directory
cd /home/build/build7


# Checkout function
svn_co () {
    for i in 1 0; do
	svn co $1 $2 $3 $4 $5 $6 $7
	SVN_EXIT_CODE=$?
	if [ $SVN_EXIT_CODE -eq 0 ]; then
	    break;
	elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "7.1.0 Enterprise Build FAILED in  `hostname` - $DATE" build-info@gwos.com
	    exit 1
	fi
	sleep 30
    done
}

echo "Get the build scripts for GroundWork Monitor 7.1.0 from subversion"

rm -rf GWMON-7.0.0-OLD
mv GWMON-7.0.0 GWMON-7.0.0-OLD

svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/build/BitRock_Build/GWMON-7.0.0

echo "Update build script update to latest version .."
cp -f GWMON-7.0.0/gwmon-7-0-build-scripts.sh .

echo " Checkout done -- start the build invoking GWMON-7.0.0/master-nightlyBuild.sh "
 
 