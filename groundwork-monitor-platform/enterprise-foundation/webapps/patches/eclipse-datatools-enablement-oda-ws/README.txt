Patch for Birtviewer 2.5.2 org.eclipse.datatools.enablement.oda.ws Plugin
-------------------------------------------------------------------------

The artifact to be patched and dependencies are extracted from 
org.eclipse.birt:birtviewer:2.5.2 and uploaded to Nexus**:

org.eclipse.datatools:org.eclipse.datatools.enablement.oda.ws:1.2.2.v201001131420
org.eclipse.datatools:org.eclipse.datatools.connectivity.oda:3.2.2.v201001270833
org.apache.commons:codec:1.3.0.v20080530-1600
com.ibm:javax.wsdl:1.5.1.v200806030408

See the webapps birtviewer project overlay for details. Additional dependencies
are also added to this artifact to support wc_client.properties password decryption:

org.jasypt:jasypt
com.chrylis:base58-code
org.apache.commons:commons-lang3

The plugin sources are recompiled using java 1.5, but dependencies are assumed to
be executable in a java 1.7 environment adopted for 7.1.0.

The actual patch is made to:

org/eclipse/datatools/enablement/oda/ws/util/RawMessageSender.java
org/eclipse/datatools/enablement/oda/ws/util/WSDLAdvisor.java

It adds the standard HTTP Authorization Basic header with the WS service user and
decrypted password.

This patch is similar to the patch for JPP JOSSO josso-ldap-identitystore. It
includes a forked copy of WSClientConfiguration and FoundationConfiguration to
strip out logging and java 1.7 dependencies. These versions do not include a
file watcher and thus do not auto update. Unlike the josso-ldap-identitystore
patch, other dependencies are shaded into the plugin jar. This approach was
taken to avoid having to modify the META-INF/MANIFEST.MF plugin dependencies
configuration.

** Note: the org.eclipse.datatools.enablement.oda.ws artifact in Nexus was
   previously patched in 5/2010 to make a similar but outmoded modification.
   Source of this previous patch has been lost. Analysis of the class file
   sizes between the original artifact from BIRT Viewer 2.5.2 and the one in
   Nexus shows some minor size differences on class files other than the file
   repatched here. Consequently, this older version was kept in Nexus to reduce
   the chance of regression.
