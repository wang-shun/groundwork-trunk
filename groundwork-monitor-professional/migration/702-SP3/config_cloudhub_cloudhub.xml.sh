#!/bin/bash
#
# Applies scripted patch to config/cloudhub/cloudhub-*.xml. Returns 0
# status on success and a non-zero status if a patch has failed and
# needs to be manually performed or reviewed.
#
# Usage: config_cloudhub_cloudhub.xml.sh <absolute install directory>
#

INSTALL_DIR=$1
cd $INSTALL_DIR
if ls config/cloudhub/cloudhub-*.xml > /dev/null 2>&1 ; then 
    for f in config/cloudhub/cloudhub-*.xml ; do
        echo "Patching $f..."
        sed -i -e 's@<gwosVersion>[^<]*</gwosVersion>@<gwosVersion>7.1</gwosVersion>@' $f
        sed -i -e 's@<wsUsername>[^<]*</wsUsername>@<wsUsername>RESTAPIACCESS</wsUsername>@' $f
        sed -i -e 's@<wsPassword>[^<]*</wsPassword>@<wsPassword>7UZZVvnLbuRNk12Yk5H33zeYdWQpnA7j9shir7QfJgwh</wsPassword>@' $f
    done
fi
