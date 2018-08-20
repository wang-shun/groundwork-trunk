#!/bin/bash
##
##	build_cacti.sh
##
##	Daniel Emmanuel Feinsmith
##	Groundwork Open Source
##
##	Modification History
##
##		Created 2/15/08
##
##

source ./error_handling.sh

#
#	Set global variables.
#

COMPONENT=automation
INSTALLDIR=$NMSDIR/tools/$COMPONENT
BUILD_ROOT=`pwd`/..

#
#	Preface Banner
#

echo "======================================================================"
echo "NMS Build System"
echo "----------------"
echo "  Building: $COMPONENT"
echo "  Build Root: $BUILD_ROOT"
echo

#
#	Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
if [ -d "$INSTALLDIR" ]
then
	rm -rf $INSTALLDIR
fi
mkdir $INSTALLDIR			|| bomb_out "automation mkdir $INSTALLDIR"
cp -r $COMPONENT/* $INSTALLDIR		|| bomb_out "automation recursive copy"
sed "s/select name, ip, oui, device from nodes/select name, ip, oui, device from nodes where ip not in (select ip from devices)/g" -i $INSTALLDIR/scripts/extract_nedi.pl
chown -R nagios:nagios $INSTALLDIR	|| bomb_out "automation recursive chown"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit
