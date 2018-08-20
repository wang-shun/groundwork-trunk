# Shell wrapper around consumer
#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
export JARPATH=./lib
echo JORAM Home: $JORAM_HOME/ship/lib

exec $JAVA_HOME/bin/java \
-Xms128m -Xmx512m \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=8005 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.naming.factory.initial=fr.dyade.aaa.jndi2.client.NamingContextFactory \
-Djava.naming.factory.host=localhost -Djava.naming.factory.port=16400 \
-cp $JARPATH/collagenet-api-2.0.0.jar:$JARPATH/collagenet-impl-2.0.0.jar:$JARPATH/collage-api-2.0.0.jar:$JORAM_HOME/ship/lib/activation.jar:$JORAM_HOME/ship/lib/JCup.jar:$JORAM_HOME/ship/lib/jmxtools.jar:$JORAM_HOME/ship/lib/joram-config.jar:$JORAM_HOME/ship/lib/joram-kclient.jar:\
$JARPATH/joram-shared.jar:$JORAM_HOME/ship/lib/mail.jar:$JORAM_HOME/ship/lib/soap.jar:$JORAM_HOME/ship/lib/jakarta-regexp-1.2.jar:$JARPATH/jms.jar:$JARPATH/jndi.jar:$JORAM_HOME/ship/lib/joram-connector.jar:$JARPATH/joram-mom.jar:$JORAM_HOME/ship/lib/jta.jar:$JORAM_HOME/ship/lib/midpapi.jar:$JORAM_HOME/ship/lib/soap.war:javagroups-all.jar:$JORAM_HOME/ship/lib/jmxri.jar:$JARPATH/joram-client.jar:$JORAM_HOME/ship/lib/joram-gui.jar:$JORAM_HOME/ship/lib/joram-raconfig.jar:$JORAM_HOME/ship/lib/kxml.jar:$JARPATH/ow_monolog.jar:$JARPATH/log4j-1.2.8.jar:$JARPATH/commons-logging-1.0.4.jar org.groundwork.foundation.jms.test.JMSTestServer
