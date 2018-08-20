#!/bin/sh -x
export name=groundwork-monitor-core
export version=5.0
export release=0.sles10
#For RedHat
#export distro=redhat
#For SuSE
export distro=packages
builddir=/home/nagios/groundwork-monitor/monitor-core/spec

# Build script for creating GroundWork 5.0 RPM
mkdir -p /usr/local/groundwork/tmp
rm -rf /groundwork-monitor-core-5.0/*
rm -rf /usr/src/$distro/BUILD/*
rm -rf /usr/local/groundwork/filelist
cd /
tar -czpf $name-$version-$release.tar.gz /usr/local/groundwork
rm -rf groundwork-monitor-core-5.0
mkdir groundwork-monitor-core-5.0
mv $name-$version-$release.tar.gz groundwork-monitor-core-5.0/
cd groundwork-monitor-core-5.0/
rm -rf usr
tar -xzpf $name-$version-$release.tar.gz
rm -rf $name-$version-$release.tar.gz
cd ..
tar -czpf $name-$version-$release.tar.gz groundwork-monitor-core-5.0/
rm /usr/src/$distro/SOURCES/$name-$version.$release.tar.gz
mv $name-$version-$release.tar.gz /usr/src/$distro/SOURCES/
rm -rf /usr/src/$distro/SPECS/groundwork-monitor-core-5.0.spec
cp $builddir/groundwork-monitor-core-5.0.spec /usr/src/$distro/SPECS/
cd /usr/src/$distro/SPECS/
rpmbuild --sign -ba /usr/src/$distro/SPECS/groundwork-monitor-core-5.0.spec
