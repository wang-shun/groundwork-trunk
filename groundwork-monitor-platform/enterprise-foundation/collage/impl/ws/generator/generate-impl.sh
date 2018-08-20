if [ -z $1 ];
then
    echo "Missing WSDL_NAME parameter!"
    echo "USAGE: WSDL_NAME LOCATION CLASS MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be used."
    echo "           LOCATION is the location where the generated source should be placed"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi

if [ -z $2 ];
then
    echo "Missing location parameter"
    echo "USAGE: WSDL_NAME LOCATION CLASS MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be used."
    echo "           LOCATION is the location where the generated source should be placed"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi
if [ -z $3 ];
then
    echo "Missing maven repository parameter"
    echo "USAGE: WSDL_NAME LOCATION CLASS MAVEN_REPO"
    echo "     Where WSDL_NAME is the name of the .wsdl file to be used."
    echo "           LOCATION is the location where the generated source should be placed"
    echo "           MAVEN_REPO is the full path to the maven repository to be used."
    exit 1
fi

# fwsevent.wsdl, for example
export WSDL_NAME=$1
#../event/src/java, for example
export LOCATION=$2
# Generate Web Service Event API
export MAVEN_REPO=$3

java -classpath $MAVEN_REPO/org.itgroundwork/jars/collage-api-1.5-M1-dev.jar:$MAVEN_REPO/saaj/jars/saaj.jar:$MAVEN_REPO/wsdl4j/jars/wsdl4j-1.5.1.jar:$MAVEN_REPO/commons-discovery/jars/commons-discovery-0.2.jar:$MAVEN_REPO/commons-logging/jars/commons-logging-1.0.4.jar:$MAVEN_REPO/axis/jars/axis-1.3.jar:$MAVEN_REPO/jaxrpc/jars/jaxrpc-1.1.jar org.apache.axis.wsdl.WSDL2Java -D -v -o$LOCATION -d Session -s -p org.groundwork.foundation.ws.impl ../../../api/wsdl/$WSDL_NAME
