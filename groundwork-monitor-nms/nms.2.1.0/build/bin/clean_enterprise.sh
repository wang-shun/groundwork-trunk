#!/bin/bash
##
##      clean_enterprise.sh
##
##      Daniel Emmanuel Feinsmith
##      Groundwork Open Source
##
##      Modification History
##
##              Created 2/15/08
##
##      Method:
##              1. Clean
##

source ./error_handling.sh

rm -rf $GWDIR/enterprise
rm -rf $GWDIR/nms/tools/installer
