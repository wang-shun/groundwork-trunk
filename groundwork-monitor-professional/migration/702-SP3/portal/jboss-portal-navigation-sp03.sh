#!/bin/bash
#####################################################################################
#
#    Copyright (C) 2015  GroundWork Inc. (www.groundworkopensource.com)
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#	$Id: post_upgrade7x.sh 04/22/2014 Arul Shanmugam$
#
# 	Simple script to upgrade portal objects. This script should be only run on
#   groundwork 7x systems.Especially on upgrades. This should be the last step
#	after final GW server restart is done. Requires GW server up in running.
#	This script use GW root user privileges to change portal
#   components. Make sure the root credentials are already validated
#   before calling this script. This script doesnt validate it. Also the credentials
#	are unencrypted.
#
#####################################################################################
## Usage function
print_usage () {
    echo "usage: ./jboss-portal-navigation-sp03.sh <GW root user> <GW root password>"
}

if [ $# != 2 ]; then
    print_usage
    exit 1
fi
## The following credential are already validated by caller by calling http://localhost/api/auth/validateRootCredentials
root_user=$1
root_password=$2
old_IFS=${IFS}
export IFS=","

portal_objects_paths="monitor-professional/migration/portal-objects-702SP03"
for portal_objects_path in $portal_objects_paths
do
	echo "Processing $portal_objects_path"
	portal_objects_files=$portal_objects_path/*
	for portal_objects_file in $portal_objects_files
	do
        echo "Processing $portal_objects_file file..."
        # take action on each file. $f store current file name
        curl --user $1:$2 -H 'Content-Type: application/zip' http://localhost/rest/private/managed-components/mop --upload-file $portal_objects_file
        if [ $? -ne 0 ]; then
                exit 1;
        fi
	done
done
echo "Updated portal objects successfully.."
export IFS=${old_IFS}
