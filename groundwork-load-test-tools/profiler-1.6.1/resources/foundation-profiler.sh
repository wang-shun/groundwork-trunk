#!/bin/sh
# Copyright (C) 2006 Groundwork Open Source Solutions
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
#

while [ $# -ge 1 ]; do
        args="$args $1"
        shift
done

export BINDIR=./
java -DprofilerConfig=$BINDIR/foundation-profiler.xml -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger \
-cp .:FoundationProfiler-1.0.jar:commons-logging-1.0.4.jar:log4j-1.2.8.jar:mysql-connector-java-3.1.13.jar:axis-1.3.jar:jaxrpc-1.1.jar:commons-discovery-0.2.jar:saaj.jar:wsdl4j-1.5.1.jar:commons-logging-1.0.4.jar:xml-apis-2.0.2.jar:collage-api-1.6.jar \
org.groundwork.foundation.profiling.ProfileFoundation $args

