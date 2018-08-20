#!/bin/bash

###########################################################################################
# rename-portal-group.sh - Renames Groundwork portal group in configuration files and the
#                          portal databases. This script is normally used with LDAP when
#                          the default group names do not follow local conventions.
#
# Example usage:
#
# > sudo ./rename-portal-group.sh GWRoot O_GROUNDWORK_INET_ROOT postgres
# > sudo ./rename-portal-group.sh GWAdmin O_GROUNDWORK_INET_ADMIN postgres
# > sudo ./rename-portal-group.sh GWOperator O_GROUNDWORK_INET_OPERATOR postgres
# > sudo ./rename-portal-group.sh -restart GWUser O_GROUNDWORK_INET_USER postgres
#
##########################################################################################
ID=$(which id)
MV=$(which mv)
RM=$(which rm)
SED=$(which sed)
SERVICE="/etc/init.d/groundwork"
ZIP=$(which zip)
LS=$(which ls)

# validate script usage
RESTART=0
if [ $# -gt 0 -a "$1" = "-restart" ] ; then
    RESTART=1
    shift 1
fi
if [ $# -ne 3 ] ; then
    echo "Usage: rename-portal-group.sh [-restart] <group name> <new group name> <postgres db password>"
    echo "Where: -restart restarts the groundwork services when done"
    exit 1
fi
if [ $($ID -u) -ne 0 ] ; then
    echo "Script must be run with root privileges"
    exit 1
fi
GROUP_NAME=$1
NEW_GROUP_NAME=$2
PG_PASSWORD=$3

GW_INSTALL_DIR=/usr/local/groundwork
JAVA_HOME=$GW_INSTALL_DIR/java
POSTGRESQL_HOME=$GW_INSTALL_DIR/postgresql
PG_USERNAME=postgres
BUILD_VERSION=$($LS $GW_INSTALL_DIR/jpp/gatein/extensions/groundwork-container-ext-*.ear | $SED -e 's/^.*groundwork-container-ext-//;s/^ear-//;s/[.]ear$//')
echo "groundwork build version detected: ${BUILD_VERSION}"

GROUNDWORK_CONTAINER_EXT_EAR=jpp/gatein/extensions/groundwork-container-ext-ear-${BUILD_VERSION}.ear
if [ ! -f $GW_INSTALL_DIR/$GROUNDWORK_CONTAINER_EXT_EAR ] ; then
    GROUNDWORK_CONTAINER_EXT_EAR=jpp/gatein/extensions/groundwork-container-ext-${BUILD_VERSION}.ear
fi
if [ ! -f $GW_INSTALL_DIR/$GROUNDWORK_CONTAINER_EXT_EAR ] ; then
    echo "cannot find groundwork-container-ext-ear file in jpp/gatein/extensions."
    exit 1
fi

# stop services and restart postgresql
if ! $SERVICE stop ; then
    exit 1
fi
if ! $SERVICE start postgresql ; then
    exit 1
fi

# rename groups in configuration files
function rename_group_in_file() {
    FILE=$1

    if [ -f $GW_INSTALL_DIR/$FILE ] ; then
        $SED -i "s/${GROUP_NAME}/${NEW_GROUP_NAME}/g" $GW_INSTALL_DIR/$FILE
        echo "renamed portal group in ${GW_INSTALL_DIR}/${FILE}"
    fi
}
rename_group_in_file config/ldap-mapping-directives.properties
rename_group_in_file foundation/container/rstools/php/bsmCheck/protected/data/auth.php
rename_group_in_file core/profiles/WEB-INF/web.xml
rename_group_in_file apache2/conf/groundwork/apache2-noma.conf
rename_group_in_file apache2/conf/groundwork/foundation-ui.conf
rename_group_in_file apache2/conf/groundwork/grafana.conf

# rename groups in war configuration files
function rename_group_in_war_file() {
    WAR=$1
    FILE=$2

    pushd /tmp > /dev/null
    $JAVA_HOME/bin/jar -xf $GW_INSTALL_DIR/$WAR $FILE
    if [ -f $FILE ] ; then
        $SED -i "s/${GROUP_NAME}/${NEW_GROUP_NAME}/g" $FILE
        $JAVA_HOME/bin/jar -uf $GW_INSTALL_DIR/$WAR $FILE
        $RM -rf ${FILE%%/*}
        echo "renamed portal group in ${GW_INSTALL_DIR}/${WAR}:${FILE}"
    fi
    popd > /dev/null
}
rename_group_in_war_file jpp/gatein/gatein.ear/gwtGadgets.war WEB-INF/web.xml
rename_group_in_war_file jpp/gatein/gatein.ear/portal.war WEB-INF/conf/portal/portal/sharedlayout.xml
rename_group_in_war_file jpp/gatein/gatein.ear/portal.war WEB-INF/conf/portal/portal-configuration.xml
rename_group_in_war_file jpp/gatein/gatein.ear/portal.war WEB-INF/conf/organization/organization-configuration.xml
rename_group_in_war_file jpp/gatein/gatein.ear/rest.war WEB-INF/web.xml
rename_group_in_war_file jpp/standalone/deployments/cloudhub.war WEB-INF/web.xml
rename_group_in_war_file jpp/standalone/deployments/monarch.war WEB-INF/web.xml
rename_group_in_war_file jpp/standalone/deployments/nms-rstools.war php/rstools/protected/data/auth.php

# rename groups in ear configuration files
function rename_group_in_ear_file() {
    EAR=$1
    WAR=$2
    FILE=$3
    
    pushd /tmp > /dev/null
    $JAVA_HOME/bin/jar -xf $GW_INSTALL_DIR/$EAR $WAR
    $JAVA_HOME/bin/jar -xf $WAR $FILE
    if [ -f $FILE ] ; then
        $SED -i "s/${GROUP_NAME}/${NEW_GROUP_NAME}/g" $FILE
        $JAVA_HOME/bin/jar -uf $WAR $FILE
        $JAVA_HOME/bin/jar -uf $GW_INSTALL_DIR/$EAR $WAR
        $RM -rf ${FILE%%/*}
        echo "renamed portal group in ${GW_INSTALL_DIR}/${EAR}:${WAR}:${FILE}"
    fi
    $RM -rf $WAR
    popd > /dev/null
}
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/portal-configuration.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/group/${GROUP_NAME}/pages.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/group/${GROUP_NAME}/navigation.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/portal/classic/pages.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/portal/classic/portal.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/application-registry-configuration.xml
rename_group_in_ear_file $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/common/common-configuration.xml
rename_group_in_ear_file jpp/standalone/deployments/groundwork-enterprise.ear status-viewer-${BUILD_VERSION}.war WEB-INF/portlet.xml

# rename configuration files and directories in ear
function rename_file_in_ear() {
    EAR=$1
    WAR=$2
    FILE=$3

    pushd /tmp > /dev/null
    $JAVA_HOME/bin/jar -xf $GW_INSTALL_DIR/$EAR $WAR
    $JAVA_HOME/bin/jar -xf $WAR $FILE
    if [ -e $FILE ] ; then
        if [ -d $FILE ] ; then
            $ZIP -qd $WAR "${FILE}/*"
        else
            $ZIP -qd $WAR $FILE
        fi
        NEW_FILE=${FILE/${GROUP_NAME}/${NEW_GROUP_NAME}}
        $MV $FILE $NEW_FILE
        $JAVA_HOME/bin/jar -uf $WAR $NEW_FILE
        $JAVA_HOME/bin/jar -uf $GW_INSTALL_DIR/$EAR $WAR
        $RM -rf ${FILE%%/*}
        echo "renamed file ${GW_INSTALL_DIR}/${EAR}:${WAR}:${FILE} to ${NEW_FILE}"
    fi
    $RM -rf $WAR
    popd > /dev/null
}
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/conf/groundwork-ext/portal/group/${GROUP_NAME}
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_ar.xml
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_cs.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_de.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_en.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_es.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_fr.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_it.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_ja.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_ko.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_ko.xml
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_ne.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_nl.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_pt_BR.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_pt_ru.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_pt_uk.properties
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_vi.xml
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_zh_TW.xml
rename_file_in_ear $GROUNDWORK_CONTAINER_EXT_EAR groundwork-container-ext.war WEB-INF/classes/locale/navigation/group/${GROUP_NAME}_zh.xml

# rename groups in jboss-idm database
export PGPASSWORD=$PG_PASSWORD
echo "renaming portal group in jbid_io jboss-idm database table..."
$POSTGRESQL_HOME/bin/psql -q -c "update jbid_io set name = '${NEW_GROUP_NAME}' where name = '${GROUP_NAME}';" jboss-idm $PG_USERNAME
echo "renaming portal group in jbid_io_attr_text_values jboss-idm database table..."
$POSTGRESQL_HOME/bin/psql -q -c "update jbid_io_attr_text_values set attr_value = 'the /${NEW_GROUP_NAME} group' where attr_value = 'the /${GROUP_NAME} group';" jboss-idm $PG_USERNAME
echo "renamed portal group in jboss-idm database"

# rename groups in jboss-jcr database
export PGPASSWORD=$PG_PASSWORD
echo "renaming portal group in jcr_ipsystem jboss-jcr database table..."
$POSTGRESQL_HOME/bin/psql -q -c "update jcr_ipsystem set name = '[http://www.gatein.org/jcr/mop/1.0/]%03${NEW_GROUP_NAME}' where name = '[http://www.gatein.org/jcr/mop/1.0/]%03${GROUP_NAME}';" jboss-jcr $PG_USERNAME
function to_hex() {
    for (( i=0 ; i<${#1} ; i++ )); do printf %02X \'${1:$i:1}; done | tr 'ABCDEF' 'abcdef'
}
GROUP_NAME_HEX=$(to_hex $GROUP_NAME)
NEW_GROUP_NAME_HEX=$(to_hex $NEW_GROUP_NAME)
function update_jcr_data() {
    TABLE=$1
    DATA_COLUMN=$2
    
    echo "renaming portal group in ${TABLE} jboss-jcr database table..."
    $POSTGRESQL_HOME/bin/psql -q -c "select id, ${DATA_COLUMN} from ${TABLE} where length(${DATA_COLUMN}) < 10240;" jboss-jcr $PG_USERNAME | {
        UPDATES=0
        while read -r LINE; do
            ID=${LINE% |*}
            if [ ${#ID} -ne ${#LINE} -a "$ID" != 'id' ]; then
                DATA=${LINE#*| }
                if [ "${DATA#*$GROUP_NAME_HEX}" != "${DATA}" ]; then
                    NEW_DATA=${DATA//$GROUP_NAME_HEX/$NEW_GROUP_NAME_HEX}
                    $POSTGRESQL_HOME/bin/psql -q -c "update ${TABLE} set ${DATA_COLUMN} = E'\\${NEW_DATA}' where id = ${ID};" jboss-jcr $PG_USERNAME
                    UPDATES=$((UPDATES+1))
                fi
            fi
        done
        echo "updated ${UPDATES} portal group containing rows in ${TABLE} jboss-jcr database table"
    }
}
update_jcr_data jcr_vpsystem data
update_jcr_data jcr_vpwork data
update_jcr_data jcr_vsystem data
echo "renamed portal group in jboss-jcr database"

# clean search indexes
$RM -rf $GW_INSTALL_DIR/jpp/standalone/data/gatein/jcr/lucene/portal-system_portal/*
echo "cleaned portal search indexes"

echo "portal group renamed"

# restart services
if [ $RESTART -ne 0 ] ; then
    $SERVICE start
fi
