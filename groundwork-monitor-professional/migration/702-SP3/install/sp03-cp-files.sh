#/bin/sh
echo "################################################################################"
echo "Copying files into GroundWork install. Start time:  `date`"
echo "################################################################################"
echo "Prepare Info.txt for SP03 ..."

sed '/TB7.0.2-2/ a PatchLevel= TB7.0.2-3' /usr/local/groundwork/Info.txt > Info.txt
yes |cp -ip Info.txt /usr/local/groundwork/Info.txt

echo "Josso file updates..."
yes | cp -ip ./foundation/container/josso-1.8.4/webapps/gwos-tomcat-monitoringAgent.war /usr/local/groundwork/josso-1.8.4/webapps
rm -rf /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-1.8.4.jar
yes | cp -ip ./foundation/container/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-gwpatch-7.1.0.jar	 /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-gwpatch-7.1.0.jar
yes | cp -ip ./foundation/container/josso-1.8.4/webapps/josso/WEB-INF/lib/jasypt-1.9.2.jar /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/jasypt-1.9.2.jar

chown -R nagios:nagios /usr/local/groundwork/josso-1.8.4/webapps

echo "GroundWork Applications..."
yes | cp -ip ./foundation/container/jpp/standalone/deployments/portal-reportviewer.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/portal-reportviewer.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/portal-groundwork-base.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/portal-groundwork-base.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/nms-rstools.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/nms-rstools.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/nagvis.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/nagvis.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/monarch.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/monarch.war
if [ -f /usr/local/groundwork/foundation/container/jpp/standalone/deployments/legacy-rest.war ] ; then
    yes | cp -ip ./foundation/container/jpp/standalone/deployments/legacy-rest.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/legacy-rest.war
fi
if [ -f /usr/local/groundwork/foundation/container/jpp2/standalone/deployments/legacy-rest.war ] ; then
    yes | cp -ip ./foundation/container/jpp/standalone/deployments/legacy-rest.war	 /usr/local/groundwork/foundation/container/jpp2/standalone/deployments/legacy-rest.war
fi
yes | cp -ip ./foundation/container/jpp/standalone/deployments/gwos-jbossas7-monitoringAgent.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/gwos-jbossas7-monitoringAgent.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/groundwork-enterprise.ear	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/groundwork-enterprise.ear
if [ -f /usr/local/groundwork/foundation/container/jpp/standalone/deployments/foundation-webapp.war ] ; then
    yes | cp -ip ./foundation/container/jpp/standalone/deployments/foundation-webapp.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/foundation-webapp.war
fi
if [ -f /usr/local/groundwork/foundation/container/jpp2/standalone/deployments/foundation-webapp.war ] ; then
    yes | cp -ip ./foundation/container/jpp/standalone/deployments/foundation-webapp.war	 /usr/local/groundwork/foundation/container/jpp2/standalone/deployments/foundation-webapp.war
fi
yes | cp -ip ./foundation/container/jpp/standalone/deployments/cloudhub.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/cloudhub.war
yes | cp -ip ./foundation/container/jpp/standalone/deployments/birtviewer.war	 /usr/local/groundwork/foundation/container/jpp/standalone/deployments/birtviewer.war

echo "Portal updates..."
rm -rf /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/META-INF/MANIFEST.MF
yes | cp -ip ./monitor-platform/jpp/portal-instance-base/custom_scripts/gatein-ear-jboss-deployment-structure.xml	 /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/META-INF/jboss-deployment-structure.xml
yes | cp -ip ./foundation/container/jpp/gatein/gatein.ear/rest.war	 /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/rest.war
yes | cp -ip ./foundation/container/jpp/gatein/gatein.ear/portal.war	 /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/portal.war
yes | cp -ip ./foundation/container/jpp/gatein/gatein.ear/gwtGadgets.war	 /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/gwtGadgets.war
yes | cp -ip ./foundation/container/jpp/gatein/gatein.ear/exoadmin.war	 /usr/local/groundwork/foundation/container/jpp/gatein/gatein.ear/exoadmin.war

yes | cp -ip ./foundation/container/jpp/gatein/extensions/groundwork-skin.war	 /usr/local/groundwork/foundation/container/jpp/gatein/extensions/groundwork-skin.war
rm -rf /usr/local/groundwork/foundation/container/jpp/gatein/extensions/groundwork-container-ext-7.0.2.ear
yes | cp -ip ./foundation/container/jpp/gatein/extensions/groundwork-container-ext-7.1.0-SNAPSHOT.ear	 /usr/local/groundwork/foundation/container/jpp/gatein/extensions/groundwork-container-ext-7.1.0-SNAPSHOT.ear

echo "Modules updates JOSSO..."

mv -f /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml /tmp/josso-agent-config.xml
rm -rf /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main
mv -f /tmp/josso-agent-config.xml /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/..index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/..index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-beans-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-beans-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-protocol-client-1.8.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-protocol-client-1.8.8.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-core-1.8.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-core-1.8.8.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-1.8.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-1.8.8.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-common-1.8.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-common-1.8.8.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-servlet-agent-1.8.9-gwpatch-09152014-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-servlet-agent-1.8.9-gwpatch-09152014-SNAPSHOT.jar


echo "Modules updates Spring..."
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-core-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-core-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-protocol-client-1.8.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-protocol-client-1.8.8.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-beans-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-beans-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-ws-1.8.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-ws-1.8.8.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-ws-1.8.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-ws-1.8.8.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/xbean-spring-3.4.3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/xbean-spring-3.4.3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-1.8.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-1.8.8.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-core-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-core-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/xbean-spring-3.4.3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/xbean-spring-3.4.3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-expression-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-expression-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-context-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-context-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-common-1.8.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-common-1.8.8.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-servlet-agent-1.8.9-gwpatch-09152014-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-servlet-agent-1.8.9-gwpatch-09152014-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-expression-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-expression-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-core-1.8.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-core-1.8.8.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-context-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/spring-context-3.2.3.RELEASE.jar.index

mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/org/jasypt/main/

yes | cp -ip ./foundation/container/jpp/modules/org/jasypt/main/jasypt-1.9.2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/jasypt/main/jasypt-1.9.2.jar
yes | cp -ip ./foundation/container/jpp/modules/org/jasypt/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/jasypt/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/org/jasypt/main/jasypt-1.9.2.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/jasypt/main/jasypt-1.9.2.jar.index

mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/ibm/icu4j/main/

yes | cp -ip ./foundation/container/jpp/modules/com/ibm/icu4j/main/icu4j-3.8.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/ibm/icu4j/main/icu4j-3.8.jar
yes | cp -ip ./foundation/container/jpp/modules/com/ibm/icu4j/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/ibm/icu4j/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/com/ibm/icu4j/main/icu4j-3.8.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/ibm/icu4j/main/icu4j-3.8.jar.index

echo "Modules updates gatein..."
rm -rf /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-integration-1.3.1.Final-redhat-3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-integration-1.3.1.Final-redhat-3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-saml-plugin-1.3.1.Final-redhat-3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-saml-plugin-1.3.1.Final-redhat-3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/..index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/..index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-1.3.1.Final-redhat-3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-1.3.1.Final-redhat-3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-beans-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-beans-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/axis-1.4.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/axis-1.4.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-assertionstore-1.8.4.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-assertionstore-1.8.4.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-auth-callback-1.3.1.Final-redhat-3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-auth-callback-1.3.1.Final-redhat-3.jar
ln -s /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-memory-assertionstore-1.8.4.jar /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-assertionstore-1.8.4.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-core-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-core-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-saml-plugin-1.3.1.Final-redhat-3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-saml-plugin-1.3.1.Final-redhat-3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-beans-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-beans-3.2.3.RELEASE.jar
ln -s /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-memory-identitystore-1.8.4.jar /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-identitystore-1.8.4.jar
ln -s /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-memory-sessionstore-1.8.4.jar /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-sessionstore-1.8.4.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-agents-bin-1.8.5.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-agents-bin-1.8.5.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/xbean-spring-3.4.3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/xbean-spring-3.4.3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-ldap-identitystore-gwpatch-7.1.0.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-ldap-identitystore-gwpatch-7.1.0.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/portal-josso-agent-config.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/portal-josso-agent-config.xml
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-core-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-core-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-integration-1.3.1.Final-redhat-3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-integration-1.3.1.Final-redhat-3.jar.index
ln -s /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-ldap-identitystore-gwpatch-7.1.0.jar /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-ldap-identitystore-gwpatch-7.1.0.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/xbean-spring-3.4.3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/xbean-spring-3.4.3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-1.3.1.Final-redhat-3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-1.3.1.Final-redhat-3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-agent-shared-1.8.5.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-agent-shared-1.8.5.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-expression-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-expression-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-context-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-context-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-sessionstore-1.8.4.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-sessionstore-1.8.4.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-expression-3.2.3.RELEASE.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-expression-3.2.3.RELEASE.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-identitystore-1.8.4.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-identitystore-1.8.4.jar.index
#yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-memory-sessionstore-1.8.4.jar /usr/local/groundwork/josso-1.8.4/webapps/josso/WEB-INF/lib/josso-memory-sessionstore-1.8.4.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/commons-discovery-0.2.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/commons-discovery-0.2.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-auth-callback-1.3.1.Final-redhat-3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-auth-callback-1.3.1.Final-redhat-3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-agent-shared-1.8.5.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-agent-shared-1.8.5.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/spring-context-3.2.3.RELEASE.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/spring-context-3.2.3.RELEASE.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-josso182-1.3.1.Final-redhat-3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-josso182-1.3.1.Final-redhat-3.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/josso-agents-bin-1.8.5.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/josso-agents-bin-1.8.5.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/commons-discovery-0.2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/commons-discovery-0.2.jar
yes | cp -ip ./foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-josso182-1.3.1.Final-redhat-3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/sso/main/sso-agent-josso182-1.3.1.Final-redhat-3.jar.index

yes | cp -ip ./foundation/container/jpp/modules/org/gatein/lib/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/lib/main/module.xml
rm -rf /usr/local/groundwork/foundation/container/jpp/modules/org/gatein/lib/main/icu4j-3.8.jar

echo "Modules updates HTTP Component..."
rm -rf /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.2.5.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.2.5.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.2.6.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.2.6.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.2.6.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.2.6.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.2.5.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.2.5.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.1.4-redhat-2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpcore-4.1.4-redhat-2.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.2.6.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.2.6.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.2.6.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.2.6.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.1.3-redhat-2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpclient-4.1.3-redhat-2.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.1.3-redhat-2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/httpcomponents/main/httpmime-4.1.3-redhat-2.jar


mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/org/apache/commons/lang3/main

yes | cp -ip ./foundation/container/jpp/modules/org/apache/commons/lang3/main/commons-lang3-3.2.jar	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/commons/lang3/main/commons-lang3-3.2.jar
yes | cp -ip ./foundation/container/jpp/modules/org/apache/commons/lang3/main/commons-lang3-3.2.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/org/apache/commons/lang3/main/commons-lang3-3.2.jar.index
yes | cp -ip ./foundation/container/jpp/modules/org/apache/commons/lang3/main/module.xml       /usr/local/groundwork/foundation/container/jpp/modules/org/apache/commons/lang3/main/module.xml

rm -rf /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/security/main
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/security/main
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/security/main/groundwork-jboss-security-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/security/main/groundwork-jboss-security-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/security/main/groundwork-jboss-security-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/security/main/groundwork-jboss-security-7.1.0-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/security/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/security/main/module.xml

mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main

yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-rest-client-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-rest-client-7.1.0-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/joda-time-2.3.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/joda-time-2.3.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-model-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-model-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-client-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-client-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-common-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-common-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-rest-client-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-rest-client-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-common-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-common-7.1.0-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/joda-time-2.3.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/joda-time-2.3.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-model-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/groundwork-container-ext-model-7.1.0-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-client-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/portal/extension/main/collagerest-client-7.1.0-SNAPSHOT.jar.index

echo "JDMA modules ..."
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main

yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/..index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/..index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/gwos_jbossas7.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/jdma/jbossas7/main/gwos_jbossas7.xml

echo " Encryption modules ..."
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/chrylis/base58-codec/main

rm -rf /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/collage/main
mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/collage/main
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/collage/main/collage-api-7.1.0-SNAPSHOT.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/collage/main/collage-api-7.1.0-SNAPSHOT.jar.index
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/collage/main/collage-api-7.1.0-SNAPSHOT.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/collage/main/collage-api-7.1.0-SNAPSHOT.jar
yes | cp -ip ./foundation/container/jpp/modules/com/groundwork/collage/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/groundwork/collage/main/module.xml

mkdir -p /usr/local/groundwork/foundation/container/jpp/modules/com/chrylis/base58-codec/main
yes | cp -ip ./foundation/container/jpp/modules/com/chrylis/base58-codec/main/base58-codec-1.2.0.jar	 /usr/local/groundwork/foundation/container/jpp/modules/com/chrylis/base58-codec/main/base58-codec-1.2.0.jar
yes | cp -ip ./foundation/container/jpp/modules/com/chrylis/base58-codec/main/module.xml	 /usr/local/groundwork/foundation/container/jpp/modules/com/chrylis/base58-codec/main/module.xml
yes | cp -ip ./foundation/container/jpp/modules/com/chrylis/base58-codec/main/base58-codec-1.2.0.jar.index	 /usr/local/groundwork/foundation/container/jpp/modules/com/chrylis/base58-codec/main/base58-codec-1.2.0.jar.index

if [ -d /usr/local/groundwork/foundation/container/jpp2 ] ; then
    chown -R nagios:nagios /usr/local/groundwork/foundation/container/jpp2/standalone
fi
chown -R nagios:nagios /usr/local/groundwork/foundation/container/jpp/gatein
chown -R nagios:nagios /usr/local/groundwork/foundation/container/jpp/modules
chown -R nagios:nagios /usr/local/groundwork/foundation/container/jpp/standalone

echo " Updating Feeders ..."
yes | cp -ip ./monitor-professional/perl/GW/RAPID.pm /usr/local/groundwork/perl/lib/site_perl/5.8.9/GW/RAPID.pm
yes | cp -ip ./monitor-professional/perl/GW/Feeder.pm /usr/local/groundwork/perl/lib/site_perl/5.8.9/GW/Feeder.pm
yes | cp -ip ./monitor-platform/enterprise-foundation/collagefeeder/scripts/cacti_feeder.pl /usr/local/groundwork/foundation/feeder/cacti_feeder.pl
yes | cp -ip ./monitor-platform/enterprise-foundation/collagefeeder/scripts/nagios2collage_eventlog.pl /usr/local/groundwork/foundation/feeder/nagios2collage_eventlog.pl
yes | cp -ip ./monitor-platform/enterprise-foundation/collagefeeder/scripts/nagios2collage_socket.pl /usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl

chown -R nagios:nagios /usr/local/groundwork/foundation/feeder
chown -R nagios:nagios /usr/local/groundwork/perl/lib/site_perl/5.8.9/GW

echo "Make sure feeders are executable ..."
chmod +x /usr/local/groundwork/foundation/feeder/nagios2collage_eventlog.pl
chmod +x /usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl
chmod +x /usr/local/groundwork/foundation/feeder/cacti_feeder.pl

echo "Monarch libraries and dependencies..."

yes |cp -ip monarch/monarch_auto.cgi /usr/local/groundwork/core/monarch/cgi-bin/monarch/
yes |cp -ip monarch/monarch_discover.cgi /usr/local/groundwork/core/monarch/cgi-bin/monarch/
yes |cp -ip monarch/monarch_ez.cgi /usr/local/groundwork/core/monarch/cgi-bin/monarch/
yes |cp -ip monarch/monarch_tree.cgi /usr/local/groundwork/core/monarch/cgi-bin/monarch/
yes |cp -ip monarch/MonarchAPI.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchAudit.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchAutoConfig.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchConf.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchDoc.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchExternals.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchFile.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchForms.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchFoundationREST.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchFoundationSync.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchProfileImport.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip monarch/MonarchStorProc.pm /usr/local/groundwork/core/monarch/lib

yes |cp -ip  monarch/dassmonarch/dassmonarch.pm /usr/local/groundwork/core/monarch/lib
yes |cp -ip  monarch/dassmonarch/monarchWrapper.pm /usr/local/groundwork/core/monarch/lib

chown -R nagios:nagios /usr/local/groundwork/core/monarch/lib
chown -R nagios:nagios /usr/local/groundwork/core/monarch/cgi-bin/monarch

echo "Install RSTools commandline utilities..."

rm -rf /usr/local/groundwork/foundation/container/rstools/*
cp -r foundation/container/rstools/* /usr/local/groundwork/foundation/container/rstools
chown -R nagios:nagios /usr/local/groundwork/foundation/container/rstools

echo "Performance data processing for RRD needs additional connectors configured..."
yes |cp -ip monitor-professional/migration/702-SP3/install/perfdata.properties /usr/local/groundwork/config/perfdata.properties
chown nagios:nagios /usr/local/groundwork/config/perfdata.properties

echo "Update Noma Notification script..."
rm -f /usr/local/groundwork/noma/notifier/alert_via_noma.pl
cp -ip monitor-professional/migration/702-SP3/install/alert_via_noma.pl /usr/local/groundwork/noma/notifier/alert_via_noma.pl
chown nagios:nagios /usr/local/groundwork/noma/notifier/alert_via_noma.pl
chmod +x /usr/local/groundwork/noma/notifier/alert_via_noma.pl

echo "################################################################################"
echo "Copying files into GroundWork install. Start time:  `date`"
echo "################################################################################"
