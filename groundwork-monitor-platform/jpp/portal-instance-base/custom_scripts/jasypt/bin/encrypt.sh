#!/bin/bash

# Copyright (c) 2017 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

if [[ ${EUID} -ne 0 ]]; then
    echo "ERROR: This script must be run as root." >&2
    exit 1
fi

GW_HOME="/usr/local/groundwork"
JAVA="$GW_HOME/java/bin/java"
MODULES="$GW_HOME/jpp/modules"

COLLAGE_MODULE=`grep resource-root $MODULES/com/groundwork/collage/main/module.xml | cut -d\" -f2`

CLASSPATH="$GW_HOME/josso-1.8.4/lib/commons-logging-1.1.1.jar"
CLASSPATH+=":$MODULES/org/jasypt/main/jasypt-1.9.2.jar"
CLASSPATH+=":$MODULES/org/apache/commons/codec/main/commons-codec-1.4-redhat-2.jar"
CLASSPATH+=":$MODULES/com/groundwork/collage/main/$COLLAGE_MODULE"
CLASSPATH+=":$MODULES/org/apache/commons/configuration/main/commons-configuration-1.6-redhat-2.jar"
CLASSPATH+=":$MODULES/org/apache/commons/lang/main/commons-lang-2.6-redhat-2.jar"
CLASSPATH+=":$MODULES/org/apache/commons/collections/main/commons-collections-3.2.1-redhat-2.jar"
CLASSPATH+=":$MODULES/org/apache/commons/lang3/main/commons-lang3-3.2.jar"
CLASSPATH+=":$MODULES/com/chrylis/base58-codec/main/base58-codec-1.2.0.jar"

LOGGING="org.apache.commons.logging"
DEFINES="-D$LOGGING.Log=$LOGGING.impl.SimpleLog -D$LOGGING.simplelog.defaultlog=error"
JASYPT="org.groundwork.foundation.ws.impl.JasyptUtils"

#. "$GW_HOME/scripts/setenv.sh"

$JAVA -cp $CLASSPATH $DEFINES $JASYPT $@
