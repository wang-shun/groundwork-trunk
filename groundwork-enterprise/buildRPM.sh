#!/bin/sh -x
# Build script for creating GroundWork 5.0 RPM
#
# Copyright (C) 2008 GroundWork Open Source, Inc. (GroundWork)  
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
#

# Check distro
if [ -f /etc/redhat-release ] ; then
  distro='rhel4'
  builddir=redhat
elif [ -f /etc/SuSE-release ] ; then
  distro='sles10'
  builddir=packages
elif [ -f /etc/mandrake-release ] ; then
  distro='Mandrake'
  builddir=
  echo "Plese set build directory in buildRPM.sh file..."
  exit 1
fi

date=$(date -I|awk '{ print $1; }'|sed -e 's/2006.//' -e 's/-/./'g)

export release=9 
export prefix=/usr/local/groundwork
export name=groundwork-monitor-ent 
export version=5.3.0
export filelist=/usr/local/ent-filelist 
export specfile=groundwork-monitor-ent-5.0.spec
export rpmroot=/

export _tmppath=/var/tmp
export ROOT_DIR=/

#Clean up
rm -rf /$name-$version
rm -rf /usr/src/$builddir/BUILD/$name-$version
rm -rf /usr/src/$builddir/SOURCES/$name-$version-$release.tar.gz
rm -rf /usr/src/$builddir/SPECS/$specfile
rm -rf $filelist
rm -rf $prefix

mkdir -p $prefix/guava/includes
mkdir -p $prefix/guava/themes/gwmpro/images
cp ./config.inc.php.ent $prefix/guava/includes/config.inc.php.ent
cp ./logo.gif $prefix/guava/themes/gwmpro/images/logo.ent.gif

cp ./$specfile /usr/src/$builddir/SPECS/

cd $ROOT_DIR
tar -czpf $name-$version-$release.tar.gz $prefix$rpmroot
mkdir -p $name-$version

mv $name-$version-$release.tar.gz $name-$version/
cd $name-$version
rm -rf usr
tar -xzpf $name-$version-$release.tar.gz
rm -rf $name-$version-$release.tar.gz

cd ..
tar -czpf $name-$version-$release.tar.gz $name-$version
rm -rf /usr/src/$builddir/SOURCES/$name-$version-$release.tar.gz
mv $name-$version-$release.tar.gz /usr/src/$builddir/SOURCES/

rm -rf $_tmppath/$name
mkdir -p $_tmppath/$name/
mkdir -p $_tmppath/$name/etc
mkdir -p $_tmppath/$name/usr
mkdir -p $_tmppath/$name/usr/local
mkdir -p $_tmppath/$name/usr/local/groundwork

rpmbuild --sign -ba /usr/src/$builddir/SPECS/$specfile
