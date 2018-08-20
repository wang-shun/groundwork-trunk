#!/bin/bash
################################################################################################
# Script to install jboss modules
# Author : Arul Shanmugam
################################################################################################
export IFS=","

MODULES_PATH=$1
MODULE_NAME=$2
MODULE_JAR_NAME=$3
MODULE_DEPENDENCIES=$4

echo "Processing $MODULE_NAME...."

DEFAULT_SLOT=main

MODULE_SUBPATH=${MODULE_NAME//./\/}
TARGET_DIR=$MODULES_PATH/$MODULE_SUBPATH/$DEFAULT_SLOT
DEPENDENCY_PREFIX='		<module name="'
DEPENDENCY_SUFFIX='"\/\>'
DEPENDENCY_WITH_METAINF_PREFIX='		<module name="'
DEPENDENCY_WITH_METAINF_SUFFIX='"\><imports\><include path="META-INF"\/\><\/imports\><\/module\>'

#DEPENDENCIES_STRING=''
#echo "$MODULE_DEPENDENCIES"

if [[ -n $MODULE_DEPENDENCIES ]]; then
	for DEPENDENCY in $MODULE_DEPENDENCIES; do
        if [ "$DEPENDENCY" = "org.gatein.sso" ] ; then
		    DEPENDENCIES_STRING+=$DEPENDENCY_WITH_METAINF_PREFIX$DEPENDENCY$DEPENDENCY_WITH_METAINF_SUFFIX
        else
		    DEPENDENCIES_STRING+=$DEPENDENCY_PREFIX$DEPENDENCY$DEPENDENCY_SUFFIX
        fi
	done	
else
    DEPENDENCIES_STRING=''
fi

cat custom_scripts/module_template.xml | sed -e 's/@module-name@/'$MODULE_NAME'/' | sed -e 's/@module-jar-name@/'$MODULE_JAR_NAME'/' | sed -e 's/@module-dependencies@/'$DEPENDENCIES_STRING'/' > $TARGET_DIR/module.xml
