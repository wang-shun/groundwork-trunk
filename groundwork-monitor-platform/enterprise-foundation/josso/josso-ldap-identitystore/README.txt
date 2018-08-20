Fork of JOSSO 1.8.9 josso-ldap-identitystore
--------------------------------------------

This fork is to implement decoding of jasypt encoded password stored
in the josso-gateway-ldap-stores.xml configuration file.

Note that forks of the following Groundwork files are also included:

WSClientConfiguration.java
FoundationConfiguration.java

These are Java 1.5 compatible versions of these files that do not employ
a file watcher. Do not upgrade these without taking the Java 1.5
requirement for JOSSO 1 into account.

This fork was pulled from the JOSSO 1.8.9 source branch located here:

https://github.com/atricore/josso1/tree/1.8.9

in this directory:

components/josso-ldap-identitystore

The POM has been modified to support building just this project using
maven 3, propagating plugin configurations and dependencies from the
original parent POM, (including the compiler plugin java 1.5 setting).

Base58 support added using forked 1.2.0 version of
com.chrylis.codec.base58.Base58Codec. Fork was necessary to compile using
Java 1.5. Dependency on 3.2 commons-lang3 was also removed as well because
it, like 1.2.0 base58-codec, is compiled with Java 1.6. Forking and
removing dependencies also reduces upgrade footprint. The base58-codec
sources are available here:

https://github.com/chrylis/base58-codec

This project should be built via the normal means:

> mvn clean install

This artifact should be uploaded to nexus, (groundwork-ee-m2-repo):

> mvn deploy

The original version of this patch was stored in SVN here:

http://geneva/groundwork-professional/trunk/monitor-platform/jpp/josso/josso-ldap-identitystore-gwpatch-src-7.1.0.zip

Here were commit histories of this file as of 4/17/2015:

------------------------------------------------------------------------
r23927 | ashanmugam | 2014-09-25 22:13:58 -0600 (Thu, 25 Sep 2014) | 1 line

GWMON-11762. Jasypt LDAP changes related to moving mainkey to foundation.properties
------------------------------------------------------------------------
r23909 | ashanmugam | 2014-09-22 12:46:24 -0600 (Mon, 22 Sep 2014) | 1 line

GWMON-11762. Adding source code related to josso-ldap-identitystore-gwpatch-7.1.0.jar. Instructions to build the jar is in README.txt file inside zip.
------------------------------------------------------------------------
