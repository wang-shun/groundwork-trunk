#!/bin/sh
export GW_HOME=/usr/local/groundwork
GROUNDWORK_VERSION=$(grep '<version>' pom.xml | head -n 1 | sed -e 's/^.*<version>//;s/<\/version>.*$//')
unzip jboss-jpp-6.0.0-src.zip
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-1//g' {} \;
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-2//g' {} \;
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-3//g' {} \;
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-4//g' {} \;
cp -f custom_scripts/exoadmin/src/main/java/org/exoplatform/organization/webui/component/*.java jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/java/org/exoplatform/organization/webui/component
cp -f custom_scripts/exoadmin/src/main/resources/locale/portlet/exoadmin/*.properties jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/WEB-INF/classes/locale/portlet/exoadmin
cp -f custom_scripts/exoadmin/*.xml jboss-jpp-6.0.0-src/portal/portlet/exoadmin
sed -ie "s/\$GROUNDWORK_VERSION/${GROUNDWORK_VERSION}/" jboss-jpp-6.0.0-src/portal/portlet/exoadmin/pom.xml
cp -f custom_scripts/exoadmin/src/main/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl jboss-jpp-6.0.0-src/portal/portlet/exoadmin/src/main/webapp/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl
cp -f custom_scripts/exoadmin/src/main/groovy/portal/webui/application/UIPortlet.gtmpl jboss-jpp-6.0.0-src/portal/web/portal/src/main/webapp/groovy/portal/webui/application/UIPortlet.gtmpl
cp -f custom_scripts/rest/src/main/webapp/WEB-INF/*.xml jboss-jpp-6.0.0-src/portal/web/rest/src/main/webapp/WEB-INF/
cp -f custom_scripts/rest/pom.xml jboss-jpp-6.0.0-src/portal/web/rest/
sed -ie "s/\$GROUNDWORK_VERSION/${GROUNDWORK_VERSION}/" jboss-jpp-6.0.0-src/portal/web/rest/pom.xml
cd jboss-jpp-6.0.0-src/portal
mvn clean install -Dgatein.dev -DskipTests
cp portlet/exoadmin/target/exoadmin.war $GW_HOME/jpp/gatein/gatein.ear/
cp web/rest/target/rest.war $GW_HOME/jpp/gatein/gatein.ear/
cd ../..
rm -rf jboss-jpp-6.0.0-src
echo "Exo Admin Done"
