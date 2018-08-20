#!/bin/bash 

# Get the CPAN stuff installed, probably do this one time only

source /usr/local/groundwork/scripts/setenv.sh

# as of release 6.7.0 beta the next section is already in the product; if you are working with older version you can uncomment

#perl -e "use CPAN; install Bundle::CPAN"
#perl -e "use CPAN; install Spreadsheet::ParseExcel"

# Once, make a copy of the original NagVis map files
# Remove the originals
# Add new maps to NagVis Views along with image backgrounds

# as of release 6.7.0 beta the next section is already in the product; if you are working with older version you can uncomment

#if [[ ! -f ../input/$1/old-maps.tar.gz ]] ; then
#	tar czvf ../input/$1/old-maps.tar.gz  /usr/local/groundwork/nagvis/etc/maps/{GW_Architecture.cfg,Datacenter_Floorplan.cfg,GW_Monitor_Stack.cfg,Geographic_map.cfg,GW_Mon_Soft_Arch.cfg,Single_Rack_Face.cfg}
#fi
#tar xvzf ../input/$1/views.tar.gz -C /
#chown -R nagios.nagios /usr/local/groundwork/nagvis
#rm -f /usr/local/groundwork/nagvis/etc/maps/{GW_Architecture.cfg,Datacenter_Floorplan.cfg,GW_Monitor_Stack.cfg,Geographic_map.cfg,GW_Mon_Soft_Arch.cfg,Single_Rack_Face.cfg}

# Copy the profiles and plugins into the GW stack

chown nagios.nagios ../profiles/*xml
cp -a ../profiles/*xml /usr/local/groundwork/core/profiles/Uploaded
chown nagios.nagios ../plugins/*
chmod +x ../plugins/*
cp -a ../plugins/* /usr/local/groundwork/nagios/libexec

# The operator must manually import them from the UI; there is no Monarch API to do that

echo "take a moment to import the added profiles from the Configuration tab"
echo "it is ok to import all"
exit
