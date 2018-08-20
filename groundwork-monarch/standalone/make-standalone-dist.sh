#!/bin/bash


export VERSION=2.5.0


# are we in the right place?
if [ ! -d 'standalone' ];
then
	echo "Run this script from the directory groundwork-opensource/monarch"
	exit
fi



#
# set up directories
#
mkdir -p standalone/monarch-$VERSION/{doc/images,images,migration,monarch_nagios,nagios_restart,nmap_scan}

cd standalone/monarch-$VERSION


#
# copy in the files
#
for type in cgi html css js pl pm xml;
do
	cp ../../*.$type ./
done

for binary in monarch_as_nagios nagios_reload nmap_scan_one;
do
	cp ../../$binary ./
	chmod +x ./$binary
done

cp ../../database/*.sql ./
cp ../../migration/*.pl ./migration/
cp ../check_mods.pl     ./
cp ../README.txt        ./
cp ../monarch_setup.pl  ./
cp ../monarch_update.pl ./
cp ../favicon.ico       ./
cp ../logo5.png  ./images/
cp ../home.jpg  ./images/
cp ../MFS_stub.pm       ./MonarchFoundationSync.pm

for subdir in monarch_nagios nagios_restart nmap_scan;
do
	cp ../../$subdir/*.c ./$subdir/
done

cp ../../images/*  ./images/
cp ../doc/*        ./doc/
cp ../doc/images/* ./doc/images/

dos2unix ./*.{cgi,css,html,js,pm,pl,sql,xml}
chmod +x ./*.pl


#
# package it up
#
cd ..
tar cf monarch-$VERSION.tar monarch-$VERSION
gzip monarch-$VERSION.tar

cd ..

