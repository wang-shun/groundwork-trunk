#!/bin/bash
#####################################################################################
#
#    Copyright (C) 2012-2015 GroundWork Inc. (www.groundworkopensource.com)
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
#	$Id: ldap-sync-prepare.sh 01/14/2015 Arul Shanmugam$
#
# 	Simple script to prepare portal configuration based on
#   the ldap-mapping-directives.properties. Before running this script
#   please backup your jboss-jcr database.
#   Since 7.1.0
#
#####################################################################################
GROUNDWORK_HOME=/usr/local/groundwork
. $GROUNDWORK_HOME/config/ldap-mapping-directives.properties
## Usage function
print_usage () {
    echo "usage: ./ldap-sync-prepare.sh <root-user> <root-password> <postgres-user> <postgres-password> "
}

if [ $# != 4 ]; then
    print_usage
    exit 1
fi
USERNAME=$1
PASSWORD=$2
PSQL_USER=$3
PSQL_PASSWD=$4
HOSTNAME=localhost
PORT=8080
JPP_HOME=$GROUNDWORK_HOME/jpp
JAVA_HOME=$GROUNDWORK_HOME/java
TEMP_HOME=/tmp/ldap_sync_prepare_7_1_0
TEMP_HOME_UPDATE=/tmp/ldap_sync_prepare_7_1_0_update
## Old group names
OLD_ROOT_GROUP=GWRoot
OLD_ADMIN_GROUP=GWAdmin
OLD_OPERATOR_GROUP=GWOperator
OLD_USER_GROUP=GWUser

GW_EXT_EAR_FILE=`echo /usr/local/groundwork/jpp/gatein/extensions/*.ear | xargs -n 1 basename`
BACKUP_FILE=LDAP_SYNC_BACKUP_710.zip

PSQL_PATH=$GROUNDWORK_HOME/postgresql/bin
PSQL_XTRA_FUNC_SQL=$GROUNDWORK_HOME/core/databases/postgresql/postgres-xtra-functions.sql
JAR=$JAVA_HOME/bin/jar

PSQL=$PSQL_PATH/psql
PSQL_CMD="$PSQL -U '$PSQL_USER'"
export PGPASSWORD=$PSQL_PASSWD

## Check is LDAP is configured. If not then exit
resources=( $( $GROUNDWORK_HOME/common/bin/xmllint --xpath "//*[local-name()='import']/@resource" $GROUNDWORK_HOME/config/josso-gateway-config.xml | grep -Po '".*?"' | tr -d \" ) )
#echo ${resources[@]}
isLDAPEnabled="false"
for i in "${resources[@]}"
do
    if [ "$i" == "josso-gateway-ldap-stores.xml" ] ; then
        isLDAPEnabled="true"
    fi
done
if [ $isLDAPEnabled == "false" ] ; then
        echo "******************************************************"
        echo "LDAP IS NOT CONFIGURED. NOTHING TO DO. EXITING.....!"
        echo "*****************************************************"
        exit -1;
fi

if [ ! -f $GROUNDWORK_HOME/config/ldap-mapping-directives.properties ]; then
        echo "****************************************************************************************************"
        echo "CANNOT FIND $GROUNDWORK_HOME/config/ldap-mapping-directives.properties. NOTHING TO DO. EXITING.....!"
        echo "****************************************************************************************************"
        exit -1;
fi
## First backup the file before making any changes.
if [ ! -f $GROUNDWORK_HOME/backup/$BACKUP_FILE ]; then
    echo "No backup file found. Taking backup $GROUNDWORK_HOME/backup/$BACKUP_FILE..."
    eval "$PSQL_PATH/pg_dump -U '$PSQL_USER' jboss-jcr > $GROUNDWORK_HOME/backup/jboss-jcr.sql"
    if [ ! -f $GROUNDWORK_HOME/backup/jboss-jcr.sql ]; then
        echo "**************************************************************************"
        echo "CANNOT BACKUP jboss-jcr DATABASE. PLEASE CHECK JBOSS CREDENTIALS.....!"
        echo "**************************************************************************"
        exit -1;
    fi
    tar cvfz $GROUNDWORK_HOME/backup/$BACKUP_FILE $JPP_HOME/gatein/gatein.ear/rest.war $JPP_HOME/gatein/gatein.ear/portal.war $JPP_HOME/gatein/extensions/$GW_EXT_EAR_FILE $GROUNDWORK_HOME/backup/jboss-jcr.sql
else
    echo "Backup file found. First restoring from backup..."
    tar xvfz $GROUNDWORK_HOME/backup/$BACKUP_FILE -C /
    /etc/init.d/groundwork stop gwservices
    eval "$PSQL_PATH/dropdb -U '$PSQL_USER' jboss-jcr"
    eval "$PSQL_PATH/createdb -U '$PSQL_USER' jboss-jcr"
    eval "$PSQL_CMD --quiet -f $PSQL_XTRA_FUNC_SQL" -d jboss-jcr
    eval "$PSQL_PATH/psql -U '$PSQL_USER' -f $GROUNDWORK_HOME/backup/jboss-jcr.sql -d jboss-jcr"
    /etc/init.d/groundwork start gwservices
fi

echo "*********************************************************************************************************************************************************"
echo "GROUNDWORK SERVER WILL BE RESTARTED SEVERAL TIMES DURING THIS PROCESS. PLEASE WAIT UNTIL YOU SEE 'SYSTEM IS NOW READY WITH NEW LDAP CHANGES' MESSAGE!"
echo "*********************************************************************************************************************************************************"
## Now cleanup the local folder and disable the security constraint
echo "Updating rest.war.."
rm -rf $TEMP_HOME
mkdir $TEMP_HOME
cp $JPP_HOME/gatein/gatein.ear/rest.war $TEMP_HOME/
cd $TEMP_HOME/
$JAR -xf rest.war
rm -rf rest.war
#sed -ie "s/<security-role-ref>/<!-- <security-role-ref>/g; s/<\/security-role-ref>/<\/security-role-ref> -->/g; s/<security-constraint>/<!-- <security-constraint>/g; s/<\/security-role>/<\/security-role> -->/g" WEB-INF/web.xml
sed -ie "s/$OLD_ROOT_GROUP/*/g" WEB-INF/web.xml
$JAR  -cMf rest.war *
mv rest.war $JPP_HOME/gatein/gatein.ear/
/etc/init.d/groundwork stop gwservices
/etc/init.d/groundwork start gwservices
## First download the portal pages,navigation and portal to capture customer custom pages
rm -rf $TEMP_HOME_UPDATE
mkdir $TEMP_HOME_UPDATE
cd $TEMP_HOME_UPDATE
echo "Downloading portal site...."
wget --http-user=$USERNAME --http-password=$PASSWORD http://$HOSTNAME:$PORT/rest/private/managed-components/mop/portalsites/classic.zip
if [ ! -f classic.zip ]; then
    echo "*****************************************************"
    echo "CANNOT DOWNLOAD PORTAL SITE. PLEASE TRY AGAIN!"
    echo "*****************************************************"
    exit -1;
fi
unzip classic.zip
rm -rf classic.zip
echo "Updating portal, pages configuration...."
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g; s/\/$OLD_ROOT_GROUP/\/$root_group/g; s/\/$OLD_OPERATOR_GROUP/\/$operator_group/g; s/\/$OLD_USER_GROUP/\/$user_group/g" portal/classic/portal.xml
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g; s/\/$OLD_ROOT_GROUP/\/$root_group/g; s/\/$OLD_OPERATOR_GROUP/\/$operator_group/g; s/\/$OLD_USER_GROUP/\/$user_group/g" portal/classic/pages.xml
## Now stop gwservices
/etc/init.d/groundwork stop gwservices

## Now cleanup jcr database
eval "$PSQL_PATH/dropdb -U '$PSQL_USER' jboss-jcr"
eval "$PSQL_PATH/createdb -U '$PSQL_USER' jboss-jcr"
eval "$PSQL_CMD --quiet -f $PSQL_XTRA_FUNC_SQL" -d jboss-jcr

## Now cleanup the local folder and make changes to the Updating groundwork-container-ext-7.x.x-SNAPSHOT
echo "Updating $GW_EXT_EAR_FILE.."
rm -rf $TEMP_HOME/*
cp $JPP_HOME/gatein/extensions/$GW_EXT_EAR_FILE $TEMP_HOME
cd $TEMP_HOME
$JAR -xf $GW_EXT_EAR_FILE
rm -rf $GW_EXT_EAR_FILE
mkdir tmp
mv groundwork-container-ext.war tmp/
cd tmp
$JAR  -xf groundwork-container-ext.war
rm -rf groundwork-container-ext.war
sed -ie "s/$OLD_ADMIN_GROUP/$admin_group/g" WEB-INF/conf/groundwork-ext/portal/portal-configuration.xml
cp -r WEB-INF/conf/groundwork-ext/portal/group/$OLD_ADMIN_GROUP WEB-INF/conf/groundwork-ext/portal/group/$admin_group
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g" WEB-INF/conf/groundwork-ext/portal/group/$admin_group/navigation.xml
cp WEB-INF/classes/locale/navigation/group/$OLD_ADMIN_GROUP.properties WEB-INF/classes/locale/navigation/group/$admin_group.properties
cp WEB-INF/classes/locale/navigation/group/${OLD_ADMIN_GROUP}_en.properties WEB-INF/classes/locale/navigation/group/${admin_group}_en.properties
cp WEB-INF/classes/locale/navigation/group/${OLD_ADMIN_GROUP}_es.properties WEB-INF/classes/locale/navigation/group/${admin_group}_es.properties
cp WEB-INF/classes/locale/navigation/group/${OLD_ADMIN_GROUP}_fr.properties WEB-INF/classes/locale/navigation/group/${admin_group}_fr.properties
cp WEB-INF/classes/locale/navigation/group/${OLD_ADMIN_GROUP}_ja.properties WEB-INF/classes/locale/navigation/group/${admin_group}_ja.properties
cp WEB-INF/classes/locale/navigation/group/${OLD_ADMIN_GROUP}_de.properties WEB-INF/classes/locale/navigation/group/${admin_group}_de.properties
sed -ie "s/$OLD_ADMIN_GROUP/$admin_group/g; s/$OLD_ROOT_GROUP/$root_group/g" WEB-INF/conf/groundwork-ext/common/common-configuration.xml
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g" WEB-INF/conf/groundwork-ext/portal/group/$admin_group/pages.xml
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g; s/\/$OLD_ROOT_GROUP/\/$root_group/g; s/\/$OLD_OPERATOR_GROUP/\/$operator_group/g; s/\/$OLD_USER_GROUP/\/$user_group/g" WEB-INF/conf/groundwork-ext/portal/portal/classic/pages.xml
sed -ie "s/\/$OLD_ADMIN_GROUP/\/$admin_group/g; s/\/$OLD_ROOT_GROUP/\/$root_group/g; s/\/$OLD_OPERATOR_GROUP/\/$operator_group/g; s/\/$OLD_USER_GROUP/\/$user_group/g" WEB-INF/conf/groundwork-ext/portal/portal/classic/portal.xml
$JAR  -cMf groundwork-container-ext.war *
mv groundwork-container-ext.war ../
cd ..
rm -rf tmp
$JAR -cMf $GW_EXT_EAR_FILE *
mv $GW_EXT_EAR_FILE $JPP_HOME/gatein/extensions
## Now cleanup the local folder and make changes to rest.war
echo "Updating rest.war.."
rm -rf $TEMP_HOME/*
cp $JPP_HOME/gatein/gatein.ear/rest.war $TEMP_HOME/
$JAR -xf rest.war
rm -rf rest.war
sed -ie "s/$OLD_ROOT_GROUP/$root_group/g" WEB-INF/web.xml
$JAR  -cMf rest.war *
mv rest.war $JPP_HOME/gatein/gatein.ear/
## Now cleanup the local folder and make changes to portal.war for sharedlayout.xml change
echo "Updating portal.war.."
rm -rf $TEMP_HOME/*
cp $JPP_HOME/gatein/gatein.ear/portal.war $TEMP_HOME/
$JAR -xf portal.war
rm -rf portal.war
sed -ie "s/\/$OLD_ROOT_GROUP/\/$root_group/g; s/\/$OLD_ADMIN_GROUP/\/$admin_group/g" WEB-INF/conf/portal/portal/sharedlayout.xml
sed -ie "s/$OLD_ROOT_GROUP/$root_group/g; s/$OLD_ADMIN_GROUP/$admin_group/g" WEB-INF/conf/portal/portal-configuration.xml
sed -ie "s/$OLD_ROOT_GROUP/$root_group/g; s/$OLD_ADMIN_GROUP/$admin_group/g; s/$OLD_OPERATOR_GROUP/$operator_group/g; s/$OLD_USER_GROUP/$user_group/g" WEB-INF/conf/organization/organization-configuration.xml
$JAR -cMf portal.war *
mv portal.war $JPP_HOME/gatein/gatein.ear/
rm -rf $TEMP_HOME/*
/etc/init.d/groundwork start gwservices
cd $TEMP_HOME_UPDATE
zip classic.zip portal/ portal/classic portal/classic/portal.xml portal/classic/pages.xml portal/classic/navigation.xml
echo "Uploading new portal site"
curl -i --user $USERNAME:$PASSWORD -H 'Content-Type: application/zip' http://$HOSTNAME:$PORT/rest/private/managed-components/mop --upload-file classic.zip
/etc/init.d/groundwork stop gwservices
## Now cleanup the local folder and disable the security constraint
echo "Updating rest.war.."
rm -rf $TEMP_HOME/*
cd $TEMP_HOME
cp $JPP_HOME/gatein/gatein.ear/rest.war $TEMP_HOME/
$JAR -xf rest.war
rm -rf rest.war
sed -ie "s/<role-link>\*/<role-link>$root_group/g; s/<role-name>\*/<role-name>$root_group/g" WEB-INF/web.xml
$JAR  -cMf rest.war *
mv rest.war $JPP_HOME/gatein/gatein.ear/
/etc/init.d/groundwork start gwservices
echo "*********************************************"
echo "SYSTEM IS NOW READY WITH NEW LDAP CHANGES."
echo "*********************************************"
