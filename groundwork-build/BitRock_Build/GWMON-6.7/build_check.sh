#!/bin/bash -x
# GroundWork Monitor - The ultimate data integration framework.
# Copyright 2009 GroundWork Open Source, Inc. "GroundWork"
#
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
##
# Build properties for Monitor - Opensource
# The values have to be in sync with the settings in groundwork-private
#

#log_dir=/var/www/html/logs
log_dir=/var/www/html/tools/DEVELOPMENT/logs
DATE=$(date +%Y-%m-%d" "%H:%M:%S)
rm -rf $log_dir/Builds_Error.txt
rm -rf $log_dir/Builds_64_Error.txt
rm -rf $log_dir/Builds_32_Error.txt

scp -rp root@maloja:/root/build/logs/build.log $log_dir/build.log.64
scp -rp root@horw:/root/build/logs/build.log $log_dir/build.log.32
chmod 744 $log_dir/build.log.*

# Clean previous build errors
sed -e 's/error//' $log_dir/BitRock64bit_Revision > $log_dir/BitRock64bit_Revision.tmp
mv -f $log_dir/BitRock64bit_Revision.tmp $log_dir/BitRock64bit_Revision
sed -e 's/error//' $log_dir/BitRock32bit_Revision > $log_dir/BitRock32bit_Revision.tmp
mv -f $log_dir/BitRock32bit_Revision.tmp $log_dir/BitRock32bit_Revision

if (grep 'BUILD FAILED' $log_dir/build.log.64) ; then
  touch $log_dir/Builds_Error.txt
  echo "error" >> $log_dir/BitRock64bit_Revision
  echo "" > $log_dir/Builds_64_Error.txt
  grep -in --before-context=10 'BUILD FAILED' $log_dir/build.log.64 >> $log_dir/Builds_64_Error.txt
  echo "" >> $log_dir/Builds_64_Error.txt
  echo "==========================================================" >> $log_dir/Builds_64_Error.txt
  scp -rp $log_dir/Builds_64_Error.txt root@montana:/root/test
  touch $log_dir/Builds_Error.txt
else
  cp -p $log_dir/BitRock64bit_Revision $log_dir/TLSBitRock64bit_Revision
fi

if (grep 'BUILD FAILED' $log_dir/build.log.32) ; then
  touch $log_dir/Builds_Error.txt
  echo "error" >> $log_dir/BitRock32bit_Revision
  echo "" > $log_dir/Builds_32_Error.txt
  grep -in --before-context=10 'BUILD FAILED' $log_dir/build.log.32 >> $log_dir/Builds_32_Error.txt
  echo "" >> $log_dir/Builds_32_Error.txt
  echo "==========================================================" >> $log_dir/Builds_32_Error.txt
  scp -rp $log_dir/Builds_32_Error.txt root@kansas:/root/test
  scp -rp $log_dir/Builds_32_Error.txt root@florida:/root/test
  touch $log_dir/Builds_Error.txt
else
  cp -p $log_dir/BitRock32bit_Revision $log_dir/TLSBitRock32bit_Revision
fi

if (grep "No such file or directory" $log_dir/build.log.64 | grep scp) ; then
  if [ -f $log_dir/Builds_64_Error.txt ] ; then
    echo "" >> $log_dir/Builds_Error.txt
    grep "No such file or directory" $log_dir/build.log.64 | grep scp >> $log_dir/Builds_Error.txt
  else
    echo "" >$log_dir/Builds_Error.txt
    grep "No such file or directory" $log_dir/build.log.64 | grep scp >> $log_dir/Builds_Error.txt
  fi
fi

if [ -f $log_dir/Builds_64_Error.txt ] ; then
  cat $log_dir/Builds_64_Error.txt >> $log_dir/Builds_Error.txt
fi

if [ -f $log_dir/Builds_32_Error.txt ] ; then
  cat $log_dir/Builds_32_Error.txt >> $log_dir/Builds_Error.txt
fi

if [ -f $log_dir/Builds_Error.txt ] ; then
  cat $log_dir/Builds_Error.txt | mail -s "Daily build FAILED - $DATE" build-info@gwos.com
else
  cat $log_dir/buildsresult.txt | mail -s "Successful daily builds - $DATE" build-info@gwos.com
fi

# Check unused file/dir
grep "No such file" $log_dir/build.log.64 | grep -v "find:" | grep -v "^sh:" > $log_dir/NoSuchFile.txt
grep "No such file" $log_dir/build.log.32 | grep -v "find:" | grep -v "^sh:" >> $log_dir/NoSuchFile.txt
if ! [ -z $log_dir/NoSuchFile.txt ] ; then
  cat $log_dir/NoSuchFile.txt | mail -s "Un-used file/dir in build - $DATE" build-info@gwos.com
fi
