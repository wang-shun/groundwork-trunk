#!/bin/bash -x

# Copyright (C) 2014  GroundWork Inc. info@groundworkopensource.com
#
# usage: config-deployment.sh CONFIGDIRECTORY DEPLOYMENT-DIRECTORY dev
# 		CONFIGDIRECTORY		Base directory where all the config file are stored
#       DEPLOYMENT-DIRECTORY	Application server deployment directory
#       dev	  (Optional)        If defined the script will create links into JOSSO configuration directory
#

export CONFIGDIR=$1
export DEPLOYMENTDIR=$2
export DEVBUILD=$3

echo "Input checking .."

if [ "$CONFIGDIR" == "" ] ; then
  echo "Config directory not defined! usage: config-deployment.sh CONFIG-DIRECTORY DEPLOYMENT-DIRECTORY dev"
  exit -1
fi

if [ "$DEPLOYMENTDIR" == "" ] ; then
  echo "Deployment directory not defined! usage: config-deployment.sh CONFIG_DIRECTORY DEPLOYMENT-DIRECTORY dev"
  exit -1
fi

echo "Copy configuration files into $CONFIGDIR"
echo "Make sure config directory exists .."
mkdir $CONFIGDIR
mkdir $CONFIGDIR/resources

cp -p target/classes/*.properties $CONFIGDIR
cp -p target/classes/*.conf $CONFIGDIR
cp -p target/classes/*.dtd $CONFIGDIR
cp -p target/classes/lang/* $CONFIGDIR/resources
cp -p target/classes/*.xml $CONFIGDIR
cp -p target/classes/*.lic $CONFIGDIR


echo "Deploy web applications into $DEPLOYMENTDIR"

cp ../enterprise-foundation/webapps/legacy-rest/target/legacy-rest.war $DEPLOYMENTDIR
cp ../enterprise-foundation/webapps/foundation/target/foundation-webapp.war $DEPLOYMENTDIR
cp ../enterprise-foundation/webapps/birtviewer/target/birtviewer.war $DEPLOYMENTDIR
cp ../agents/cloudhub/target/cloudhub.war $DEPLOYMENTDIR
cp ../monitor-apps/groundwork-enterprise/target/groundwork-enterprise.ear $DEPLOYMENTDIR
cp ../monitor-apps/rstools/target/nms-rstools.war $DEPLOYMENTDIR
cp ../monitor-apps/groundwork-base/target/portal-groundwork-base.war $DEPLOYMENTDIR
cp ../monitor-apps/monarch/target/monarch.war $DEPLOYMENTDIR
cp ../monitor-apps/nagvis/target/nagvis.war $DEPLOYMENTDIR
cp ../monitor-apps/reportserver/target/portal-reportviewer.war $DEPLOYMENTDIR
cp ../monitor-apps/grafana/target/grafana-app.war $DEPLOYMENTDIR
cp target/icefaces-push-server-1.8.2-P06-EE.war $DEPLOYMENTDIR


if [ "$DEVBUILD" == "dev" ] ; then
  echo "Create symlinks from the config directories to JOSSO configuration files. SUDO will be used"
  sudo ln -s $CONFIGDIR/../foundation/container/jpp/standalone/configuration/gatein/configuration.properties $CONFIGDIR/configuration.properties
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/webapps/josso/WEB-INF/classes/gatein.properties $CONFIGDIR/gatein.properties
  sudo ln -s $CONFIGDIR/../foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml $CONFIGDIR/test-josso-agent-config.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-credentials.xml $CONFIGDIR/josso-credentials.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-auth.xml $CONFIGDIR/josso-gateway-auth.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-config.xml $CONFIGDIR/josso-gateway-config.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-db-stores.xml $CONFIGDIR/josso-gateway-db-stores.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-gatein-stores.xml $CONFIGDIR/josso-gateway-gatein-stores.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-jmx.xml $CONFIGDIR/osso-gateway-jmx.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-ldap-stores.xml $CONFIGDIR/josso-gateway-ldap-stores.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-memory-stores.xml $CONFIGDIR/josso-gateway-memory-stores.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-selfservices.xml $CONFIGDIR/josso-gateway-selfservices.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-stores.xml $CONFIGDIR/josso-gateway-stores.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-gateway-web.xml $CONFIGDIR/josso-gateway-web.xml
  sudo ln -s $CONFIGDIR/../foundation/container/josso-1.8.4/lib/josso-users.xml $CONFIGDIR/josso-users.xml
  
else
  echo "No symlinks created. Assuming installer will take care of it"
fi
