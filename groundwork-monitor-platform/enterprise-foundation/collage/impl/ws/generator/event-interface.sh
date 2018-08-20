# Generate Web Service Event API
export MAVEN_REPO=/home/rogerrut/.maven/repository

java -classpath $MAVEN_REPO/org.itgroundwork/jars/collage-api-1.5-M1-dev.jar:$MAVEN_REPO/saaj/jars/saaj.jar:$MAVEN_REPO/wsdl4j/jars/wsdl4j-1.5.1.jar:$MAVEN_REPO/commons-discovery/jars/commons-discovery-0.2.jar:$MAVEN_REPO/commons-logging/jars/commons-logging-1.0.4.jar:$MAVEN_REPO/axis/jars/axis-1.3.jar:$MAVEN_REPO/jaxrpc/jars/jaxrpc-1.1.jar org.apache.axis.wsdl.Java2WSDL -o ../../../api/wsdl/fwsevent.wsdl -l"http://localhost:8080/axis/services/event" -n urn:fws -p"org.groundwork.foundation.ws.api" urn:fws org.groundwork.foundation.ws.api.WSEvent
