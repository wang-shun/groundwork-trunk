#!/bin/bash
##
##      clean.sh
##
##      Daniel Emmanuel Feinsmith
##      Groundwork Open Source
##
##      Modification History
##
##              Created 2/15/08
##
##      Method:
##              1. Clean Tools
##		2. Clean Components
##		3. Clean Datbase.
##

#
#	1. Clean Tools
#

./clean_rrdtool.sh
./clean_net-snmp.sh
./clean_httpd.sh
./clean_php.sh
./clean_perl.sh
./clean_perl-modules.sh

#
#	2. Clean Components
#

./clean_ntop.sh
./clean_cacti-spine.sh
./clean_cacti.sh
./clean_cacti-plugin-arch.sh
./clean_weathermap.sh
./clean_thold.sh
./clean_discovery.sh
./clean_nedi.sh

#
#	3. Clean Other.
#

./clean_enterprise.sh
./clean_automation.sh

rm -rf ../tmp

