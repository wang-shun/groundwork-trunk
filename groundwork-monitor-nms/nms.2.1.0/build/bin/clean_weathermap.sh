#!/bin/bash
##
##      clean_weathermap.sh
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

COMPONENT=weathermap
rm -rf $NMSDIR/applications/$COMPONENT
