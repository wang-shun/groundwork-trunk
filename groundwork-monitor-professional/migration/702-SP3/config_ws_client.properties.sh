#!/bin/bash
#
# Applies scripted patch to config/ws_client.properties. Returns 0
# status on success and a non-zero status if a patch has failed and
# needs to be manually performed or reviewed.
#
# Usage: config_ws_client.properties.sh <absolute install directory>
#

INSTALL_DIR=$1
cd $INSTALL_DIR
echo "Patching config/ws_client.properties..."
sed -i -e '/^webservices_user=/s/=.*$/=RESTAPIACCESS/' config/ws_client.properties
sed -i -e '/^webservices_password=/s/=.*$/=7UZZVvnLbuRNk12Yk5H33zeYdWQpnA7j9shir7QfJgwh/' config/ws_client.properties
sed -i -e '/^webservices_password=/a credentials.encryption.enabled=true' config/ws_client.properties
