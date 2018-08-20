
if [ -z $1 ]
then
    echo "Missing wsdl name parameter!"
    echo "USAGE: WSDL_NAME LOCATION CLASS MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be generated."
    echo "           LOCATION is the eventual location of the service"
    echo "           CLASS is the name of the java class to be used to generate the .wsdl"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi

if [ -z $2 ]
then
    echo "Missing location parameter"
    echo "USAGE: WSDL_NAME LOCATION MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be generated."
    echo "           LOCATION is the eventual location of the service"
    echo "           CLASS is the name of the java class to be used to generate the .wsdl"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi
if [ -z $3 ]
then
    echo "Missing class name parameter"
    echo "USAGE: WSDL_NAME LOCATION MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be generated."
    echo "           LOCATION is the eventual location of the service"
    echo "           CLASS is the name of the java class to be used to generate the .wsdl"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi
if [ -z $4 ]
then
    echo "Missing maven repository parameter"
    echo "USAGE: WSDL_NAME LOCATION MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be generated."
    echo "           LOCATION is the eventual location of the service"
    echo "           CLASS is the name of the java class to be used to generate the .wsdl"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi

# fwsevent, for example
export WSDL_NAME=$1
#http://localhost:8080/axis/services/event, for example
export LOCATION=$2
# Event for example - no extension needed
export CLASS=$3
# Generate Web Service Event API
export MAVEN_REPO=$4

java -classpath $MAVEN_REPO/org.itgroundwork/jars/collage-api-1.5-M1-dev.jar:$MAVEN_REPO/saaj/jars/saaj.jar:$MAVEN_REPO/wsdl4j/jars/wsdl4j-1.5.1.jar:$MAVEN_REPO/commons-discovery/jars/commons-discovery-0.2.jar:$MAVEN_REPO/commons-logging/jars/commons-logging-1.0.4.jar:$MAVEN_REPO/axis/jars/axis-1.3.jar:$MAVEN_REPO/jaxrpc/jars/jaxrpc-1.1.jar org.apache.axis.wsdl.Java2WSDL -o ../../../api/wsdl/$WSDL_NAME -l"$LOCATION" -n urn:fws -p"org.groundwork.foundation.ws.api" urn:fws org.groundwork.foundation.ws.api.$CLASS