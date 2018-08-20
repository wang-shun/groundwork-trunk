#!/bin/bash

# remove the build stuff
echo Removing ./dist
rm -rf ./dist

# build
echo mvn package
mvn package

GW_DS="/usr/local/groundwork/grafana/public/app/plugins/datasource/groundwork"

# remove old installed stuff
echo Removing old installed groundwork datasource
rm -rf $GW_DS

# install new build stuff
echo Installing new dist
cp -a dist $GW_DS
chown -R nagios.nagios $GW_DS

echo restarting grafana
/usr/local/groundwork/ctlscript.sh restart grafana
