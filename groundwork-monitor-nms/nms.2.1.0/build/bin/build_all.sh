#!/bin/bash
##
##      build_all.sh
##
##      Daniel Emmanuel Feinsmith
##      Groundwork Open Source
##
##      Modification History
##
##              Created 2/15/08
##
##      Method:
##              1. Build Tools
##		2. Build Components
##

source ./error_handling.sh

#
#	Set global variables.
#

BINDIR=$PWD

#
#	Opening Banner.
#

echo "======================================================================"
echo "Building All Tools and Components."

#
#	Build Tools
#

./build_rrdtool.sh		|| bomb_out rrdtool
./build_net-snmp.sh		|| bomb_out net-snmp
./build_httpd.sh		|| bomb_out httpd
./build_mod_auth_tkt.sh		|| bomb_out mod_auth_tkt
./build_php.sh			|| bomb_out php
./build_perl.sh			|| bomb_out perl
./build_perl-modules.sh		|| bomb_out perl-modules

#
#	Build Components
#

./build_enterprise.sh		|| bomb_out enterprise
./build_automation.sh		|| bomb_out automation

#
#	Updated filelist, this will remove after filelist is set with all add-in modules
#

cd /usr/local/groundwork
find /usr/local/groundwork/nms -name ".svn" -exec rm -rf {} \; -prune
find /usr/local/groundwork/enterprise -name ".svn" -exec rm -rf {} \; -prune
chown -R nagios:nagios /usr/local/groundwork/enterprise
chown -R nagios:nagios /usr/local/groundwork/nms

echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms" > $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms/tools" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms/applications" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/enterprise" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist

cd $GWDIR/enterprise
$BINDIR/rpm-filelist.pl >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
cd $NMSDIR/tools
$BINDIR/rpm-filelist.pl >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist

echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms/applications/cacti-spine" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms/applications/cacti-spine/bin" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) %dir /usr/local/groundwork/nms/applications/cacti-spine/etc" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0755,nagios,nagios) /usr/local/groundwork/nms/applications/cacti-spine/bin/spine" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist
echo "%attr(0644,nagios,nagios) /usr/local/groundwork/nms/applications/cacti-spine/etc/spine.conf" >> $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist

cp -p $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0.filelist $BINDIR/../rpmsetup/groundwork-nms-core-2.1.0-x86_64.filelist
cd $BINDIR

#
#	Build None-Core Components
#

./build_ntop.sh			|| bomb_out ntop
./build_cacti-spine.sh		|| bomb_out cacti-spine
./build_cacti.sh		|| bomb_out cacti
./build_cacti-plugin-arch.sh	|| bomb_out cacti-plugin-arch
./build_cacti_ping_executor.sh	|| bomb_out build_cacti_ping_executor
./build_weathermap.sh		|| bomb_out weathermap
./build_settings.sh		|| bomb_out settings
./build_thold.sh		|| bomb_out thold
./build_discovery.sh		|| bomb_out discovery
./build_nedi.sh			|| bomb_out nedi

#
#	Remove Build Artifacts.
#

#rm -rf ../tmp
find $GWDIR -name ".svn" -exec rm -rf {} \; -prune

#
#	Closing Banner.
#

echo "======================================================================"
echo "Done Building All."
exit
