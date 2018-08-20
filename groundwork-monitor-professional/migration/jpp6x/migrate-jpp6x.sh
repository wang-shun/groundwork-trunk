#!/bin/bash
#####################################################################################
#
#    Copyright (C) 2013  GroundWork Inc. (www.groundworkopensource.com)
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
#	$Id: migrate-jpp6x.sh 10/07/2013 Arul Shanmugam$
#
# 	Simple script to migrate users, roles, memberships, custom groups, extended roles from
#	jboss portal 2.7.2 to JPP 6.0.
#
#####################################################################################
start_time=`date +%s`
OLDIFS=$IFS
export IFS=":"

## Usage function
print_usage () {
    echo "usage: ./migrate-jpp6x.sh <source_host:source_port:source_database:source_user:source_password> \\"
    echo "           <dest_host:dest_port:dest_database:dest_user:dest_password>"
}

if [ $# != 2 ]; then
    print_usage
    exit 1
fi

SOURCE=($1)
DEST=($2)

#### Begin Source Configuration ####
SRC_HOST=${SOURCE[0]}
SRC_PORT=${SOURCE[1]}
SRC_DB=${SOURCE[2]}
SRC_PSQL_USER=${SOURCE[3]}
SRC_PSQL_PASSWD=${SOURCE[4]}
#### End Source Configuration ####


#### Begin Destination Configuration ####
PSQL_HOST=${DEST[0]}
PSQL_PORT=${DEST[1]}
DEST_DB=${DEST[2]}
PSQL_USER=${DEST[3]}
PSQL_PASSWD=${DEST[4]}
PSQL="/usr/local/groundwork/postgresql/bin/psql"
PGDUMP="/usr/local/groundwork/postgresql/bin/pg_dump"
PSQL_DEST_BASE_CMD="$PSQL -U $PSQL_USER -h$PSQL_HOST -p$PSQL_PORT"
PSQL_DEST_FMT_CMD="$PSQL_DEST_BASE_CMD --dbname=$DEST_DB -P t -P format=unaligned -c "
#### End Destination Configuration ####


if [ ! -x $PSQL ]; then
	end_time=`date +%s`
	time_elapsed=$(($end_time-$start_time))
	echo "***************************************************"
	echo "MIGRATION FAILED! CANNOT EXECUTE $PSQL !"
	echo "Script execution took $time_elapsed seconds."
	echo "***************************************************"
	exit 1;
fi

if [ "$SRC_DB" != "jbossportal" ]; then
	echo "***************************************************"
	echo "MIGRATION FAILED! $SRC_DB IS INVALID !"
	echo "Script execution took $time_elapsed seconds."
	echo "***************************************************"
	exit 1;
fi

if [ "$DEST_DB" != "jboss-idm" ]; then
	echo "***************************************************"
	echo "MIGRATION FAILED! $DEST_DB IS INVALID !"
	echo "Script execution took $time_elapsed seconds."
	echo "***************************************************"
	exit 1;
fi

IFS=$OLDIFS

export PGPASSWORD=$PSQL_PASSWD

GLOBAL_COPY_SUCCESS="";


## Suppress the notices while truncating. This is good for the session only.
export PGOPTIONS='--client-min-messages=warning'

echo "Copying gw tables, please wait ..."
## At this point customgroup tables created by hibernate for 7.0.1 on destination side has no customer data 
## except seed data. First drop the custom group tables on the destination side.
## Then move the data. Later drop custom_group_seq as in 7.0.0 it is unified to hibernate_sequence.
eval "$PSQL_DEST_FMT_CMD \"DROP TABLE if exists gw_entitytype,gw_customgroup_element,gw_customgroup_collection,gw_customgroup\""
eval "$PSQL_DEST_FMT_CMD \"CREATE SEQUENCE custom_group_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1\""
eval "$PGDUMP -h$SRC_HOST -p$SRC_PORT -U$SRC_PSQL_USER $SRC_DB -t gw_entitytype -t gw_customgroup -t gw_customgroup_element -t gw_customgroup_collection | $PSQL_DEST_BASE_CMD $DEST_DB"
eval "$PSQL_DEST_FMT_CMD \"DROP SEQUENCE if exists custom_group_seq CASCADE\""
if [ $? -ne 0 ]; then
	exit 1;
fi

#####Now export the users, roles, memberships and import to JPP 6.x
echo "Copying idm tables, please wait ..."
eval "$PSQL_DEST_FMT_CMD \"DROP TABLE if exists jbp_roles,jbp_users,jbp_user_prop,jbp_role_membership,gw_ext_role_attributes\""
eval "$PGDUMP -h$SRC_HOST -p$SRC_PORT -U$SRC_PSQL_USER $SRC_DB -t jbp_roles -t jbp_users -t jbp_user_prop -t jbp_role_membership -t gw_ext_role_attributes | $PSQL_DEST_BASE_CMD $DEST_DB"
if [ $? -ne 0 ]; then
	exit 1;
fi
#####After dumping the tables. Fix the spaces in the rolenames GWMON-11396 
eval "$PSQL_DEST_FMT_CMD \"update jbp_roles set jbp_name=replace(jbp_name,' ','_')\""
if [ $? -ne 0 ]; then
	exit 1;
fi
echo "Downloaded idm tables from old database ..."
REALM_ID=`$PSQL_DEST_FMT_CMD "select id from jbid_realm where name = 'idm_realm_portal'"`
ROLES=`eval "$PSQL_DEST_FMT_CMD  \"SELECT jbp_name from jbp_roles\""`
ROOT_TYPE_ID=`eval "$PSQL_DEST_FMT_CMD \"select id from jbid_io_type where name = 'root_type'\""`
REL_MEM_TYPE_ID=`eval "$PSQL_DEST_FMT_CMD  \"select id from jbid_io_rel_type where name = 'JBOSS_IDENTITY_MEMBERSHIP'\""`
REL_ROLE_TYPE_ID=`eval "$PSQL_DEST_FMT_CMD \"select id from jbid_io_rel_type where name = 'JBOSS_IDENTITY_ROLE'\""`
NOW=`date +%s`
NOW=$[NOW*1000]
###Now populating roles
for ROLE in $ROLES
do
	ROLE_EXISTS=`$PSQL_DEST_FMT_CMD "SELECT name from jbid_io_rel_name where name = '$ROLE' and realm=$REALM_ID"`
	if [ -z "$ROLE_EXISTS" ]; then
		echo "Processing role $ROLE_EXISTS..."

		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel_name values((SELECT nextval('hibernate_sequence')),'$ROLE',$REALM_ID)\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel_name_props values((select id from jbid_io_rel_name where name = '$ROLE'),$NOW,'create_date')\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel_name_props values((select id from jbid_io_rel_name where name = '$ROLE'),$NOW,'modified_date')\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel_name_props values((select id from jbid_io_rel_name where name = '$ROLE'),(SELECT jbp_displayname from jbp_roles where jbp_name = '$ROLE'),'description')\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi

		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel values((SELECT nextval('hibernate_sequence')),(select id from jbid_io where name = 'GTN_ROOT_GROUP' and realm=$REALM_ID),null,(SELECT COALESCE((SELECT id FROM jbid_io WHERE name = '$USER_ROLE' and realm=$REALM_ID and identity_type=$ROOT_TYPE_ID), (SELECT id FROM jbid_io WHERE name='GWUser' and realm=$REALM_ID and identity_type=$ROOT_TYPE_ID)) FROM jbid_io LIMIT 1),$REL_MEM_TYPE_ID)\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
	fi
done

###Now populating users and memberships.Filter out any disabled user accounts.
USERS=`$PSQL_DEST_FMT_CMD "SELECT jbp_uname from jbp_users where jbp_enabled='t'"`
user_props=("lastLoginTime" "firstName" "email" "lastName" "createdDate")
declare -a prop_value
for USER in $USERS
do
	USER_EXISTS=`$PSQL_DEST_FMT_CMD "SELECT name from jbid_io where name = '$USER' and realm=$REALM_ID"`
	if [ -z "$USER_EXISTS" ]; then
		echo "Processing user $USER"
		##Order of user_props matters here
		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io values((SELECT nextval('hibernate_sequence')),(select id from jbid_io_type where name = 'USER'),'$USER',$REALM_ID)\""
		prop_value[0]=`$PSQL_DEST_FMT_CMD "select jbp_value from jbp_user_prop where jbp_name = 'portal.user.last-login-date' and jbp_uid=(select jbp_uid from jbp_users where jbp_uname = '$USER')"`
		prop_value[0]=${prop_value[0]:-$NOW}

		prop_value[1]=`$PSQL_DEST_FMT_CMD "SELECT jbp_givenname from jbp_users where jbp_uname = '$USER'"`
		prop_value[1]=${prop_value[1]:-$USER}

		prop_value[2]=`$PSQL_DEST_FMT_CMD "SELECT jbp_realemail from jbp_users where jbp_uname = '$USER'"`
		prop_value[2]=${prop_value[2]:-$USER@mycompany.com}

		prop_value[3]=`$PSQL_DEST_FMT_CMD "SELECT jbp_familyname from jbp_users where jbp_uname = '$USER'"`
		prop_value[3]=${prop_value[3]:-$USER}

		prop_value[4]=${prop_value[0]}

		CREDENTIALS=`$PSQL_DEST_FMT_CMD "SELECT jbp_password from jbp_users where jbp_uname = '$USER'"`
		JBID_IO_ID=`$PSQL_DEST_FMT_CMD "select id from jbid_io where name = '$USER' and identity_type=(select id from jbid_io_type where name = 'USER') and realm=$REALM_ID"`
		echo "userid $JBID_IO_ID"
		i=0
		for user_prop in "${user_props[@]}"
		do
  			echo "Processing user field $user_prop with ${prop_value[$i]}"
  			eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_attr values((SELECT nextval('hibernate_sequence')),$JBID_IO_ID,'$user_prop','text',null)\""
			JBID_IO_ATTR_ID=`$PSQL_DEST_FMT_CMD "select attribute_id from jbid_io_attr where identity_object_id=$JBID_IO_ID and name = '$user_prop'"`
			eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_attr_text_values values($JBID_IO_ATTR_ID,'${prop_value[$i]}')\""
			if [ $? -ne 0 ]; then
				exit 1;
			fi
			i=$i+1
  		done
  
  		### Insert credentials
		eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_creden values((SELECT nextval('hibernate_sequence')),null,$JBID_IO_ID,'$CREDENTIALS',(select id from jbid_io_creden_type where name = 'PASSWORD'))\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
		USER_ROLES=`$PSQL_DEST_FMT_CMD "select r.jbp_name from jbp_roles r,jbp_users u,jbp_role_membership m where r.jbp_rid=m.jbp_rid and u.jbp_uid=m.jbp_uid and u.jbp_uname='$USER'"`
		for USER_ROLE in $USER_ROLES
		do
			eval "$PSQL_DEST_FMT_CMD \"INSERT INTO jbid_io_rel values((SELECT nextval('hibernate_sequence')),(SELECT COALESCE((SELECT id FROM jbid_io WHERE name = '$USER_ROLE' and realm=$REALM_ID and identity_type=$ROOT_TYPE_ID), (SELECT id FROM jbid_io WHERE name='GWUser' and realm=$REALM_ID and identity_type=$ROOT_TYPE_ID)) FROM jbid_io LIMIT 1),(select id from jbid_io_rel_name where name='$USER_ROLE' and realm=$REALM_ID),(select id from jbid_io where name = '$USER' and identity_type=(select id from jbid_io_type where name = 'USER') and realm=$REALM_ID),$REL_ROLE_TYPE_ID)\""
			if [ $? -ne 0 ]; then
				exit 1;
			fi
		done
	else
		## If the user is already there just update the password. Particularly useful when password is changed for users = wsuser
		UPD_CREDENTIALS=`$PSQL_DEST_FMT_CMD "SELECT jbp_password from jbp_users where jbp_uname = '$USER'"`
		UPD_JBID_IO_ID=`$PSQL_DEST_FMT_CMD "select id from jbid_io where name = '$USER' and identity_type=(select id from jbid_io_type where name = 'USER') and realm=$REALM_ID"`
		eval "$PSQL_DEST_FMT_CMD \"UPDATE jbid_io_creden set text='$UPD_CREDENTIALS' where identity_object_id=$UPD_JBID_IO_ID\""
		if [ $? -ne 0 ]; then
			exit 1;
		fi
	fi
	GLOBAL_COPY_SUCCESS="0";
done


update_hibernate_sequence ()
{
	declare -a seq
	seq[0]=`$PSQL_DEST_FMT_CMD "select max(group_id) from gw_customgroup"`
	seq[1]=`$PSQL_DEST_FMT_CMD "select max(element_id) from gw_customgroup_element"`
	seq[2]=`$PSQL_DEST_FMT_CMD "select max(group_id) from gw_customgroup_collection"`
	seq[3]=`$PSQL_DEST_FMT_CMD "select max(jbp_rid) from gw_ext_role_attributes"`
	seq[4]=`$PSQL_DEST_FMT_CMD "select nextval('hibernate_sequence')"`
	IFS=$'\n'
	max=`echo "${seq[*]}" | sort -nr | head -n1`
	### Add some buffer to the max sequence value
	max=$[max+10]
	echo "Adjusting hibernate_sequence to $max"
	eval "$PSQL_DEST_FMT_CMD \"SELECT setval('hibernate_sequence', $max)\""
}


if [ -n "$GLOBAL_COPY_SUCCESS" ]; then
	### Now drop 6.7 jbossportal tables including Nagvis. Supervisions will take care of those tables.
	update_hibernate_sequence
	eval "$PSQL_DEST_FMT_CMD \"DROP TABLE jbp_role_membership,jbp_user_prop,jbp_users,jbp_roles\""
	eval "$PSQL_DEST_FMT_CMD \"ALTER TABLE gw_ext_role_attributes ADD CONSTRAINT uniq_gw_ext_role_attributes_jbp_name UNIQUE (jbp_name)\""

	# Several config files were changed in the GWMEE 7.0.0 release.  One in particular
	# has a change that is simple enough to be automated, so we do so here.  This will
	# prevent the annoyance of having users omit or make mistakes in performing this
	# portion of the upgrade.
	#
	# (*) In config/ws_client.properties, check for this line:
	#         status_restservice_url=http://localhost:8080/status-restservice/rest/entityStatus/statusInfo
	#     and if the line does not exist, append it to the file.
	#
	ws_client=/usr/local/groundwork/config/ws_client.properties
	if [ -f $ws_client ]; then
		# First, append a newline to the file if it does not already end with one,
		# so our new content is guaranteed to be on its own line.
		#
		# The "tail -c 1" command prints the last character of the file, if any, and the back-tick
		# operator removes the trailing newline from the output of the command embedded in it.
		# So if the last character of the file is not a newline, the string will be non-empty.
		if [ -n "`tail -c 1 $ws_client`" ]; then
			echo >> $ws_client
		fi
		# Then, append the desired new content to the file if it is not already present in the file.
		# We're careful to allow slightly more flexibility in our pattern matching than we actually
		# expect to see in the file, to robustly cover all cases.
		if [ `egrep -c '^\s*status_restservice_url\s*=\s*http://localhost:8080/status-restservice/rest/entityStatus/statusInfo\s*$' $ws_client` -eq 0 ]; then
			echo 'status_restservice_url=http://localhost:8080/status-restservice/rest/entityStatus/statusInfo' >> $ws_client
		fi
		
		#
		# GWME 7.0.2 addition for the REST services
		# 
		# foundation_rest_url=http://localhost:8080/foundation-webapp/api
		#
		if [ `egrep -c '^\s*foundation_rest_url\s*=\s*http://localhost:8080/foundation-webapp/api\s*$' $ws_client` -eq 0 ]; then
			echo 'foundation_rest_url=http://localhost:8080/foundation-webapp/api' >> $ws_client
		fi
	else
		echo "WARNING:  There is no $ws_client file to potentially modify."
	fi

	end_time=`date +%s`
	time_elapsed=$(($end_time-$start_time))
	echo "*****************************************************************"
	echo "Migration Completed SUCCESSFULLY!"
	echo "Script execution took $time_elapsed seconds."
	echo "*****************************************************************"
fi
