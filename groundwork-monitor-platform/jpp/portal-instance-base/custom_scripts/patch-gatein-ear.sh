#!/bin/sh
################################################################################################
# Script to Patch JBOSS GateIN EAR
################################################################################################
echo "Patching GateIN EAR started..."
GROUNDWORK_HOME=$1
JPP_HOME=$GROUNDWORK_HOME/jpp
SSO_HOME=$GROUNDWORK_HOME/gatein-sso
GATEIN_EAR=$JPP_HOME/gatein/gatein.ear
GWT_GADGETS_WAR=$GATEIN_EAR/gwtGadgets.war
if [ ! -d "target" ]; then
  mkdir target
fi
cp $GWT_GADGETS_WAR ./target
cd target
if [ -d "gwtGadgets" ]; then
    rm -rf gwtGadgets
fi
mkdir gwtGadgets
cd gwtGadgets
jar xf ../gwtGadgets.war
sed -ie 's/administrators/GWRoot/g' ./WEB-INF/web.xml
echo "...replacing administators role with GWRoot ..."
grep "GWRoot" ./WEB-INF/web.xml
echo "...copying back gwtGadgets war file..."
jar cf gwtGadgets.war .
cp gwtGadgets.war $GWT_GADGETS_WAR
cd ../..
echo "...replacing META-INF/MANIFEST.MF with META-INF/jboss-deployment-structure.xml ..."
rm ${GATEIN_EAR}/META-INF/MANIFEST.MF
cp custom_scripts/gatein-ear-jboss-deployment-structure.xml ${GATEIN_EAR}/META-INF/jboss-deployment-structure.xml
echo "...Patched GateIN EAR completed."
