#!/bin/bash
echo "Running:  cloudhub-config.sh $@"
if [[ $# -ne 2 ]] ; then
    echo 'GROUNDWORK_HOME and PROFILES SOURCE_DIR parameters required'
    exit 0
fi
GROUNDWORK_HOME=$1
SOURCE_DIR=$2
CLOUDHUB_CONF=$GROUNDWORK_HOME/config/cloudhub
CLOUDHUB_VEMA=$GROUNDWORK_HOME/core/vema/profiles
# Create directories if none-existent
if [ ! -d $CLOUDHUB_VEMA ]; then
    echo "Creating Cloudhub (VEMA) Profile Templates Directory..."
    mkdir -p $CLOUDHUB_VEMA
fi
if [ ! -d $CLOUDHUB_CONF ]; then
    echo "Creating Cloudhub Configuration Directory..."
    mkdir -p $CLOUDHUB_CONF
fi
if [ ! -d $CLOUDHUB_CONF/profiles ]; then
    echo "Creating Cloudhub Profiles Directory..."
    mkdir -p $CLOUDHUB_CONF/profiles
fi
if [ ! -d $CLOUDHUB_CONF/profile-templates ]; then
    echo "Creating Cloudhub Profiles Directory..."
    mkdir -p $CLOUDHUB_CONF/profile-templates
fi
if [ ! -d $CLOUDHUB_CONF/statistics ]; then
    echo "Creating Cloudhub Statistics Directory..."
    mkdir -p $CLOUDHUB_CONF/statistics
fi
if [ ! -d $CLOUDHUB_CONF/azure ]; then
    echo "Creating Cloudhub Azure Directory..."
    mkdir -p $CLOUDHUB_CONF/azure
fi
# copy in latest profile-templates
echo "Copying Latest Profile Templates..."
cp -f $SOURCE_DIR/*.xml $CLOUDHUB_VEMA
cp -f $SOURCE_DIR/*.xml $CLOUDHUB_CONF/profile-templates
echo "...Cloudhub Profile Templates Copy complete"


