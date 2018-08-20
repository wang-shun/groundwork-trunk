#!/bin/sh
################################################################################################
# Script to Slim down JBOSS JPP 6.0 per Red Hat slimming doc.
# Manual steps are listed in: https://community.jboss.org/wiki/JBossAS7TuningAndSlimmingSubsystems
# This script slims stock JBOSS JPP 6.0 for the given $JPP_HOME value.
# Sample $JPP_HOME value would look like /usr/local/groundwork/jpp
# Author : Arul Shanmugam
################################################################################################
echo "Running:  slim_epp.sh $@"
echo "Slimming epp started ..."
GROUNDWORK_HOME=$1
GROUNDWORK_VERSION=$2
JPP_HOME=$GROUNDWORK_HOME/jpp
SSO_HOME=$GROUNDWORK_HOME/gatein-sso
JOSSO_HOME=$GROUNDWORK_HOME/josso-1.8.4
JOSSO_GATEWAY_VERSION_REF=1.8.4
SPRING_VERSION=2.5.5
XBEAN_SPRING_VERSION=3.4.3
CORE_SERVICES=$GROUNDWORK_HOME/core/services
JASYPT_HOME=$GROUNDWORK_HOME/foundation/container/jasypt
JOSSO_LDAP_PATCH_VERSION_REF=1.8.9-gwpatch-${GROUNDWORK_VERSION}
###Removing WSRP as groundwork don't use it
rm -rf $JPP_HOME/gatein/extensions/gatein-wsrp-integration.ear
rm -rf $JPP_HOME/gatein/extensions/jpp-branding-extension.ear
rm -rf $JPP_HOME/modules/org/jboss/as/mail
echo "Slimming is complete."
echo "Adjusting ACL for Toolbar ..."
cp -f custom_scripts/sharedlayout.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/conf/portal/portal/sharedlayout.xml
cp -f custom_scripts/portal-configuration.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/conf/portal/portal-configuration.xml
echo "Replacing default tabbeddashboard permissions to everyone. Fix for users cannot delete my groundwork pages"
sed -ie 's/*:\/platform\/users/Everyone/g' $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/conf/portal/user/template/user/user.xml
echo "Adding predefined groundwork organization structure ..."
cp -f custom_scripts/organization-configuration.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/conf/organization/organization-configuration.xml
echo "Customizing stand-alone profile ..."
cp -f custom_scripts/standalone.xml $JPP_HOME/standalone/configuration/standalone.xml
cp -f custom_scripts/application-roles.properties $JPP_HOME/standalone/configuration/application-roles.properties
cp -f custom_scripts/application-users.properties $JPP_HOME/standalone/configuration/application-users.properties
cp -f custom_scripts/standalone.conf.default $JPP_HOME/bin/standalone.conf
cp -f custom_scripts/standalone.conf.large $JPP_HOME/bin/standalone.conf.large
echo "Customizing portal configuration ..."
cp -f custom_scripts/configuration.properties $JPP_HOME/standalone/configuration/gatein/configuration.properties
cp -f custom_scripts/web.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/web.xml
cp -f custom_scripts/idm-configuration.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/conf/organization/idm-configuration.xml
echo "Setting up the JOSSO Server ..."
cp -r $SSO_HOME/josso/josso-182/plugin/* $JOSSO_HOME/
sed -i 's/8080/8888/g' $JOSSO_HOME/conf/server.xml
sed -i 's/8005/8805/g' $JOSSO_HOME/conf/server.xml
sed -i 's/8009/8809/g' $JOSSO_HOME/conf/server.xml
sed -i 's/<Connector/<Connector address="127.0.0.1"/g' $JOSSO_HOME/conf/server.xml
chmod +x $JOSSO_HOME/bin/*.sh
cp -f custom_scripts/josso/agent/portal-josso-agent-config.xml $JPP_HOME/gatein/gatein.ear/portal.war/WEB-INF/classes/sso/josso/1.8/josso-agent-config.xml
echo "Setting up the JOSSO Client ..."
rm -rf $JPP_HOME/modules/org/gatein/sso
cp -r $SSO_HOME/josso/gatein-josso-182/modules/org/gatein/sso $JPP_HOME/modules/org/gatein/
cp -f custom_scripts/josso/agent/portal-josso-agent-config.xml $JPP_HOME/modules/org/gatein/sso/main/portal-josso-agent-config.xml
echo "Upgrade JOSSO Client Spring version ..."
rm -rf $JPP_HOME/modules/org/gatein/sso/main/spring-*.jar
cp -r $JPP_HOME/modules/org/josso/generic-ee/agent/main/spring-*.jar $JPP_HOME/modules/org/gatein/sso/main
echo "Patching modules to resolve eXo JAXRS WS conflict, Spring version upgrade, and introduction of global icu4j module..."
rm -rf $JPP_HOME/modules/org/gatein/lib/main/icu4j-3.8.jar
cp -f custom_scripts/org_gatein_lib_main_module.xml $JPP_HOME/modules/org/gatein/lib/main/module.xml
cp -f custom_scripts/org_gatein_sso_main_module.xml $JPP_HOME/modules/org/gatein/sso/main/module.xml
sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $JPP_HOME/modules/org/gatein/sso/main/module.xml
echo "Patching modules to resolve HttpComponents conflict with CloudHubAWS..."
cp -f custom_scripts/org_apache_httpcomponents_main_module.xml $JPP_HOME/modules/org/apache/httpcomponents/main/module.xml
echo "Patching modules to resolve missing file upload dependency..."
cp -f custom_scripts/org_apache_commons-fileupload_main_module.xml $JPP_HOME/modules/org/apache/commons-fileupload/main/module.xml
echo "Modifying login page ..."
rm -rf $JOSSO_HOME/webapps/ROOT
rm -rf $JOSSO_HOME/webapps/docs
rm -rf $JOSSO_HOME/webapps/host-manager
rm -rf $JOSSO_HOME/webapps/manager
rm -rf $JOSSO_HOME/webapps/partnerapp
rm -rf $JOSSO_HOME/webapps/josso/selfservices
cp -r custom_scripts/josso/resources/css/* $JOSSO_HOME/webapps/josso/resources/css/
cp -r custom_scripts/josso/resources/img/* $JOSSO_HOME/webapps/josso/resources/img/
cp -r custom_scripts/josso/josso-layout.jsp $JOSSO_HOME/webapps/josso/josso-layout.jsp
cp -r custom_scripts/josso/signon/usernamePasswordLogin.jsp $JOSSO_HOME/webapps/josso/signon/usernamePasswordLogin.jsp
sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $JOSSO_HOME/webapps/josso/signon/usernamePasswordLogin.jsp
cp -r custom_scripts/josso/gateway/josso-gateway-config.xml $JOSSO_HOME/lib/josso-gateway-config.xml
echo "Installing JOSSO generic-ee module ..."
cp -f custom_scripts/josso/agent/josso-agent-config-partnerapps.xml $JPP_HOME/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml
cp -f custom_scripts/josso/agent/spring-beans-2.5.xsd $JOSSO_HOME/conf/
cp -f custom_scripts/josso/agent/spring-osgi.xsd $JOSSO_HOME/conf/
cp -f custom_scripts/josso/agent/module.xml $JPP_HOME/modules/org/josso/generic-ee/agent/main/
sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $JPP_HOME/modules/org/josso/generic-ee/agent/main/module.xml
cp -f custom_scripts/favicon.ico $JPP_HOME/gatein/gatein.ear/portal.war/favicon.ico
echo updating groovy scripts ...
cp -f custom_scripts/exoadmin/src/main/groovy/portal/webui/application/UIPortlet.gtmpl $JPP_HOME/gatein/gatein.ear/portal.war/groovy/portal/webui/application/UIPortlet.gtmpl
echo GroundWork deployment descriptor ...
cp -f custom_scripts/check-listener.conf $JPP_HOME/standalone/configuration/check-listener.conf
echo "Copying LDAP JOSSO files"
cp -f custom_scripts/josso/gateway/josso-gateway-auth.xml $JOSSO_HOME/lib/josso-gateway-auth.xml
cp -f custom_scripts/josso/gateway/josso-gateway-ldap-stores.xml $JOSSO_HOME/lib/josso-gateway-ldap-stores.xml
echo "Swapping tomcat setclasspath script that includes jmxremote enabled"
cp -f custom_scripts/josso/setclasspath.sh $JOSSO_HOME/bin/setclasspath.sh
echo "Creating symlinks for the josso dependencies for com.groundwork.security module"
cp -f $JOSSO_HOME/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-$JOSSO_LDAP_PATCH_VERSION_REF.jar  $JPP_HOME/modules/org/gatein/sso/main/josso-ldap-identitystore-$JOSSO_LDAP_PATCH_VERSION_REF.jar
cp -f $JOSSO_HOME/webapps/josso/WEB-INF/lib/josso-memory-assertionstore-$JOSSO_GATEWAY_VERSION_REF.jar  $JPP_HOME/modules/org/gatein/sso/main/josso-memory-assertionstore-$JOSSO_GATEWAY_VERSION_REF.jar
cp -f $JOSSO_HOME/webapps/josso/WEB-INF/lib/josso-memory-identitystore-$JOSSO_GATEWAY_VERSION_REF.jar  $JPP_HOME/modules/org/gatein/sso/main/josso-memory-identitystore-$JOSSO_GATEWAY_VERSION_REF.jar
cp -f $JOSSO_HOME/webapps/josso/WEB-INF/lib/josso-memory-sessionstore-$JOSSO_GATEWAY_VERSION_REF.jar  $JPP_HOME/modules/org/gatein/sso/main/josso-memory-sessionstore-$JOSSO_GATEWAY_VERSION_REF.jar
# cp -f custom_scripts/com_groundwork_security_main_module.xml $JPP_HOME/modules/com/groundwork/security/main/module.xml
# sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $JPP_HOME/modules/com/groundwork/security/main/module.xml
cp -f custom_scripts/com_groundwork_portal_extension_module.xml $JPP_HOME/modules/com/groundwork/portal/extension/main/module.xml
sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $JPP_HOME/modules/com/groundwork/portal/extension/main/module.xml
mkdir $GROUNDWORK_HOME/dual-jboss-installer
cp -f dual-jboss-installer/install-dual-jboss.sh $GROUNDWORK_HOME/dual-jboss-installer/
sed -i "s/\$GROUNDWORK_VERSION/$GROUNDWORK_VERSION/g" $GROUNDWORK_HOME/dual-jboss-installer/install-dual-jboss.sh
cp -f dual-jboss-installer/standalone.xml $GROUNDWORK_HOME/dual-jboss-installer/
cp -f dual-jboss-installer/standalone2.xml $GROUNDWORK_HOME/dual-jboss-installer/

# Many of the lines just below are commented out not because we don't logically want them here,
# but to force us to generate some broken 7.1.0 builds so we track down where else in our builds
# we had the original foundation/ service established, and get that cleaned up before we take over
# the duty to maintain those services here.

# echo "Adjusting groundwork services"
# mkdir $CORE_SERVICES/service-foundation
# mkdir $CORE_SERVICES/service-foundation/log
# cp -f custom_scripts/groundwork-services/service-foundation.run $CORE_SERVICES/service-foundation/run
# mkdir $CORE_SERVICES/service-foundation/log/main
# cp -f custom_scripts/groundwork-services/service-foundation.log.run $CORE_SERVICES/service-foundation/log/run
# mkdir $CORE_SERVICES/service-jpp
# mkdir $CORE_SERVICES/service-jpp/log
# cp -f custom_scripts/groundwork-services/service-jpp.run $CORE_SERVICES/service-jpp/run
# mkdir $CORE_SERVICES/service-jpp/log/main
# cp -f custom_scripts/groundwork-services/service-jpp.log.run $CORE_SERVICES/service-jpp/log/run

cp -f custom_scripts/groundwork-services/standalone.sh $JPP_HOME/bin/
chown nagios:nagios $JPP_HOME/bin/standalone.sh

# FIX MINOR:  During development of the dual-JBoss patch for 7.0.1, we parked a copy
# of the updated gwservices script here locally because our OS repository was broken
# and wouldn't accept commits.  Now that we have moved all our builds to draw only
# from the PRO repository, this secondary copy of the gwservices script should be
# considered obsolete.  In fact, once we have 7.1.0 builds demonstrated to be working
# while drawing this script from its normal location in Subversion, we'll drop the
# local copy of this script, along with this comment and line of code.
## cp -f custom_scripts/groundwork-services/gwservices $CORE_SERVICES/gwservices

# rm -rf $CORE_SERVICES/foundation
echo "Creating jdma configuration module"
mkdir -p $JPP_HOME/modules/com/groundwork/jdma/jbossas7/main
cp -f custom_scripts/com_groundwork_jdma_jbossas7_main_module.xml $JPP_HOME/modules/com/groundwork/jdma/jbossas7/main/module.xml
cp -f custom_scripts/jdma/gwos_jbossas7.xml $JPP_HOME/modules/com/groundwork/jdma/jbossas7/main/
cp -f custom_scripts/jdma/gwos_tomcat.xml $JOSSO_HOME/conf/
cp -f custom_scripts/ldap-sync-prepare.sh $JPP_HOME/bin/
echo "Removing the orignal josso-ldap-identitystore-1.8.4.jar as we patched the jar"
rm -rf $JOSSO_HOME/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-1.8.4.jar
echo "Patching SAAJ module"
cp -f custom_scripts/com_sun_xml_messaging_saaj_main_module.xml $JPP_HOME/modules/com/sun/xml/messaging/saaj/main/module.xml
echo "Zipping portal war ### MUST ALWAYS BE AT THE END OF THE SCRIPT ###"
cd $JPP_HOME/gatein/gatein.ear/portal.war/
jar -cvf portal2.war *
mv portal2.war $JPP_HOME/gatein/gatein.ear/
cd $JPP_HOME/gatein/gatein.ear/
rm -rf portal.war
mv portal2.war portal.war
echo "All slim_epp.sh adjustments are complete."
