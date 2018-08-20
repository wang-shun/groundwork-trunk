# JBoss Portal 6.0.0 Session Invalidation Patch

JBoss Portal 6.0.0 has a bug - when you logout out of the portal - all the portlet applications do not get logged out.
Subsequently, their sessions are not destroyed, leaving long lasting sessions and memory usage that would otherwise
be cleaned up.

See the JIRA issue here: <http://jira/browse/GWMON-10979>

## Build Instructions

This bug was fixed JBoss Portal 6.1.0. The fixes are to two Java files.
This patch applies the 6.1.0 fixes are applied to 6.0.0.

In order to build from the Enterprise source, you have to massage the POM names to point at actual artifacts available
from Nexus. I had to replace the "redhat-" suffixed artifacts with 'normal' artifacts.

On my mac (sed might work differently on Linux), here is an example command:
```bash
cd jboss-jpp-6.0.0-src
find . -name pom.xml -exec sed -i '' 's/-redhat-.//g' '{}' \;
```

And then build it. But ... there is no master pom :-(.
So you will need to build projects individually.
Fortunately, the portal sub-project has its own parent pom. Make sure you build with Java 6

```bash
export JAVA_HOME=`/usr/libexec/java_home -v 1.6`
mvn clean install -Dgatein.dev -DskipTests
```

Note these Jars are created minus the -redhat-n in the artifact name.
So if you are patching any of their jars, you will need to copy the jar into the JBoss modules,
and either add the redhat suffix back onto the version or modify the module.xml for that module.


Also see comments here from my discussions with JBoss Enterprise support:
<https://access.redhat.com/support/cases/00886378>


## Status of August 2, 2013

I've finally got the session bug fixed here locally.
I had to dig into the 6.1.0 pre-release code to figure it out.
After applying two patches to two different jars,
the StatusViewerHttpSessionListener is now correctly receiving session invalidation notifications from JBoss Portal.
This *should* address a big memory leak issue, as the sessions were holding on to quite a bit of data until
they finally timed out.

## What Files were Modified?

Source Files Modified:

1. ./portal/webui/portal/src/main/java/org/exoplatform/portal/application/PortalLogoutLifecycle.java
2. ./portal-components/wci/jboss/jboss7/src/main/java/org/gatein/wci/jboss/JB7ServletContainerContext.java

Producing:

3. wci-jboss7-2.3.0.Final.jar
4. exo.portal.webui.portal-3.5.2.Final.jar


To test, simply copy the two jars into jpp/modules :


cp wci-jboss7-2.3.0.Final.jar /usr/local/groundwork/jpp/modules/org/gatein/wci/main/wci-jboss7-2.3.0.Final-redhat-1.jar
cp exo.portal.webui.portal-3.5.2.Final.jar /usr/local/groundwork/jpp/modules/org/gatein/lib/main/exo.portal.webui.portal-3.5.2.Final-redhat-4.jar

