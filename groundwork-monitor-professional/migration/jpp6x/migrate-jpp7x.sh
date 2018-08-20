#!/bin/bash
#####################################################################################
#
#    Copyright (C) 2014  GroundWork Inc. (www.groundworkopensource.com)
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
#	$Id:$
#
# 	Simple script to migrate ws_client.properties
#
#####################################################################################
start_time=`date +%s`
OLDIFS=$IFS
export IFS=":"
GROUNDWORK_HOME=/usr/local/groundwork
JPP_MODULES_HOME=$GROUNDWORK_HOME/jpp/modules
COLLAGE_API_JAR=`find $JPP_MODULES_HOME -name "collage-api*.jar"`

	# Several config files were changed in the GWMEE 7.0.2 release.  One in particular
	# has a change that is simple enough to be automated, so we do so here.  This will
	# prevent the annoyance of having users omit or make mistakes in performing this
	# portion of the upgrade.
	#
	# (*) In config/ws_client.properties, check for this line:
	#         status_restservice_url=http://localhost:8080/status-restservice/rest/entityStatus/statusInfo
	#     and if the line does not exist, append it to the file.
	#
	# Since 7.1.0, upgrade script need to take care of encrypted credentials in the config files.
	ws_client=$GROUNDWORK_HOME/config/ws_client.properties
	foundation_prop=$GROUNDWORK_HOME/config/foundation.properties
	app_users_prop=$GROUNDWORK_HOME/foundation/container/jpp/standalone/configuration/application-users.properties
	app_roles_prop=$GROUNDWORK_HOME/foundation/container/jpp/standalone/configuration/application-roles.properties
	if [ -f $foundation_prop ]; then
	    if [ `egrep -c '^\s*jasypt.mainkey' $foundation_prop` -eq 0 ]; then
			echo 'jasypt.mainkey=3PHpHhaYzuc=R3IwdW5kVzByazEyMw==' >> $foundation_prop
			sed -i '/^webservices_user/d' $ws_client
			sed -i '/^webservices_password/d' $ws_client
			echo 'webservices_user=RESTAPIACCESS' >> $ws_client
			echo 'webservices_password=OAv8xVUnH8WeO2h0qzU2CIdH+1CJbrJxssv95GF4skE=' >> $ws_client
		fi
	else
		echo "WARNING:  There is no $foundation_prop file to potentially modify."
	fi
	if [ -f $ws_client ]; then
				#
		# GWME 7.0.2 addition for the REST services
		# 
		# foundation_rest_url=http://localhost:8080/foundation-webapp/api
		#
        	# DN 2016-01-22 avoid issues with adding more than one foundation_rest_url line in the case of dual jboss where have ...:8180.
        	#               There should only ever be zero or just one foundation_rest_url entry depending on GW 702 patch level
		# if [ `egrep -c '^\s*foundation_rest_url\s*=\s*http://localhost:8080/foundation-webapp/api\s*$' $ws_client` -eq 0 ]; then 
        	# Look for a non commented out foundation_rest_url prop, regardless of its value.
		if [ `egrep -c '^\s*foundation_rest_url\s*=' $ws_client` -eq 0 ]; then
			echo 'foundation_rest_url=http://localhost:8080/foundation-webapp/api' >> $ws_client
		fi
	else
		echo "WARNING:  There is no $ws_client file to potentially modify."
	fi

	if [ -f $app_users_prop ]; then
	    #
		# GWME 7.1.0. Addition of file based webservices username and password hashes.
		#
		# RESTAPIACCESS=xxxxxxxxxx
		# REMOTEAPIACCES=yyyyyyyyy
		#
		if [ `egrep -c '^\s*RESTAPIACCESS' $app_users_prop` -eq 0 ]; then
			echo 'RESTAPIACCESS=13f539a5ae8b2f3e56f96fa688f7d0df' >> $app_users_prop
		fi
		if [ `egrep -c '^\s*REMOTEAPIACCESS' $app_users_prop` -eq 0 ]; then
			echo 'REMOTEAPIACCESS=8e93b4f7648016796e4c5441731389f8' >> $app_users_prop
		fi
		if [ `egrep -c '^\s*gdma' $app_users_prop` -eq 0 ]; then
			echo 'gdma=8ae0d35b1f513c066178c3eaf805a0fa' >> $app_users_prop
		fi
	else
		echo "WARNING:  There is no $app_users_prop file to potentially modify."
	fi

	if [ -f $app_roles_prop ]; then
	    #
		# GWME 7.1.0. Addition of file based roles.
		#
		# RESTAPIACCESS=xxxxxxxxxx
		# REMOTEAPIACCES=yyyyyyyyy
		#
		if [ `egrep -c '^\s*RESTAPIACCESS' $app_roles_prop` -eq 0 ]; then
			echo 'RESTAPIACCESS=guest' >> $app_roles_prop
		fi
		if [ `egrep -c '^\s*REMOTEAPIACCESS' $app_roles_prop` -eq 0 ]; then
			echo 'REMOTEAPIACCESS=guest' >> $app_roles_prop
		fi
		if [ `egrep -c '^\s*gdma' $app_roles_prop` -eq 0 ]; then
			echo 'gdma=guest' >> $app_roles_prop
		fi
	else
		echo "WARNING:  There is no $app_roles_prop file to potentially modify."
	fi
