#!/bin/bash

# This helper script is expected to run when upgrading from GWMEE 7.1.0 to 7.1.1.
# It updates GW::RAPID Feeder based feeder master config files to work with the
# version of the Perl Config::General package we include in GWMEE 7.1.1.

echo "################################################################################"
echo "Updating RAPID feeder config files"
echo "################################################################################"

config_base="/usr/local/groundwork/config"

# Cacti feeder - required
config="$config_base/cacti_feeder.conf"
if [ -f $config ]; then
    echo "Updating $config"
    /usr/local/groundwork/perl/bin/perl -i -lpe 's/\$(\w+)/\${$1}/g' $config
else
    echo "ERROR:  There is no $config file to modify."
    exit 1
fi

# LogBridge feeders - may or may not exist
logbridge_configs=( "$config_base/gwevents_to_es.conf" "$config_base/logbridge_feeder.conf" )
for config in "${logbridge_configs[@]}"
do
    if [ -f $config ]; then
        echo "Updating $config"
        /usr/local/groundwork/perl/bin/perl -i -lpe 's/\$(\w+)/\${$1}/g' $config
    fi
done

# SCOM feeder - may or may not exist
config="$config_base/scom_feeder.conf"
if [ -f $config ]; then
    echo "Updating $config"
    /usr/local/groundwork/perl/bin/perl -i -lpe 's/\$(\w+)/\${$1}/g' $config
fi
