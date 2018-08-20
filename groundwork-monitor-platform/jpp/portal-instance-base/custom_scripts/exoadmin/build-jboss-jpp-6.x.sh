#!/bin/bash -x
#######################################################################
# Build the Jboss-JPP-6.0 from source. This will populate all exoplatorm gatein jars in m2 repo
#
rm -rf /home/build/build7/groundwork-jpp/portal-instance-base/jboss-jpp-6.0.0-src
echo "Building Jboss-JPP-6.0 from source..."
cd ../..

echo " UNZIP file that was downloaded by the main project (use the m2 cache)..."
unzip jboss-jpp-6.0.0-src.zip

find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-1//g' {} +
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-2//g' {} +
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-3//g' {} +
find jboss-jpp-6.0.0-src/ -name "pom.xml" -exec sed -ie 's/-redhat-4//g' {} +
cd jboss-jpp-6.0.0-src/portal
mvn clean install -Dgatein.dev -DskipTests
pwd

echo "Jboss JPP 6.x Custom build done at `date`"
