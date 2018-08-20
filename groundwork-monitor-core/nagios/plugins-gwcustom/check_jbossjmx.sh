#!/bin/sh
## JBoss Nagios Plugin
##
## Modified September 2009, so that it works with GroundWork Monitor 6.0
##
export JBOSS_SERVER=$1
export JBOSS_VERSION=$2
export JBOSS_MBEAN=$3
export JMX_ATTR=$4
export WARN=$5
export CRITICAL=$6

#####################################################
## Configure these for your environment
#####################################################
export JBOSS4_HOME=/usr/local/groundwork/foundation/jboss
export JBOSS3_HOME=/home/jboss/jboss-3.2.7

#JAVA Home is already set for the user running this script
#export JAVA_HOME=/usr/java/jdk1.5.0_04
#####################################################




if [[ $1 = "--help" ||  $1 = "--h" || $1 = "-help" || $1 = "-h"  ]]; then
        echo check_jbossjmx Usage
        echo "jbossJMX_plugin <JBoss Server URL> <JBoss Version 3|4> <JBoss MBean Object Name> <JBoss MBean Attribute> <Warn Threshhold> <Critical Threshhold>"
        exit 3
fi

if [ "$JBOSS_VERSION" =  "3" ]; then
        export TWIDDLE=$JBOSS3_HOME/bin/twiddle.sh
else
        if [ "$JBOSS_VERSION" =  "4" ]; then
                export TWIDDLE=$JBOSS4_HOME/bin/twiddle.sh
        else
                echo "Unrecognized JBoss Version:" $JBOSS_VERSION
                exit 3
        fi
fi

export READING=`$TWIDDLE -s $JBOSS_SERVER  get $JBOSS_MBEAN $JMX_ATTR | awk '{split($1,names,"="); print names[2]; }' `

if [ $READING -ge $CRITICAL ]; then
  echo "JMX ATTRIBUTE CRITICAL" - $JBOSS_MBEAN-$JMX_ATTR:$READING
  exit 2
else
  if [ $READING -ge $WARN ]; then
        echo "JMX ATTRIBUTE WARNING" - $JBOSS_MBEAN-$JMX_ATTR:$READING
        exit 1
  else
        echo "JMX ATTRIBUTE OK" - $JBOSS_MBEAN-$JMX_ATTR:$READING
        exit  0
  fi
fi
