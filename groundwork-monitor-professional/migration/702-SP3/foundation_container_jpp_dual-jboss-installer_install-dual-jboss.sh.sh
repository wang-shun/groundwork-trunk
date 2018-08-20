#!/bin/bash
#
# Applies scripted patch to foundation/container/jpp/dual-jboss-installer/install-dual-jboss.sh.
# Returns 0 status on success and a non-zero status if a patch has failed and
# needs to be manually performed or reviewed.
#
# Usage: foundation_container_jpp_dual-jboss-installer_install-dual-jboss.sh.sh <absolute install directory>
#

INSTALL_DIR=$1
cd $INSTALL_DIR
echo "Patching foundation/container/jpp/dual-jboss-installer/install-dual-jboss.sh..."
sed -i -e '/^\s*\/usr\/local\/groundwork\/config\/foundation.properties/a \    /usr/local/groundwork/config/ws_client.properties \\' foundation/container/jpp/dual-jboss-installer/install-dual-jboss.sh
