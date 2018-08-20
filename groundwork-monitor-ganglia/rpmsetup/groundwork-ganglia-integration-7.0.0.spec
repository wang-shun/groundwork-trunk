# This is the groundwork-ganglia-integration spec file needed to construct
# the Groundwork Monitor Ganglia Integration Module RPM.

# Copyright 2008-2017 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# The construction of this spec file is a bit unusual in that it is
# intended to be self-contained; that is, we don't want to depend on
# the particular user's dot-files (~/.rpmrc or ~/.rpmmacros) and build
# structure (pre-established ~/rpmbuild/*/ directories).  Also, we don't
# start with a source RPM or tarball.  Rather, we assume the source code
# is already splayed out, and all we're trying to do at this stage is to
# construct the RPM(s).

# ================================================================

# Note:  If you need to debug the specfile or the associated macro files,
# it may help to invoke the dump macro.  Uncomment the following line and
# change the at-sign to a percent sign (rpmbuild invokes macros even when
# they are embedded in comments, so we couldn't just leave a commented-out,
# directly-useable invocation here).  That will print out all the macro
# values for your inspection.
# @dump

# I'm not sure yet what this is for, except that it suppresses errors about
# not finding a 
# %define debug_package	%{nil}

# This setting allows us to redefine the dependency analyzer below, so we can
# filter out some "requires" dependencies on the GroundWork Monitor RPMs that
# are not yet properly available as "provides" capabilities in those RPMs.
#
# %define _use_internal_dependency_generator     0
#
# Unfortunately, this control flag has contradictory effects, depending on
# how it is set, and here we logically need to invoke both of these mutually
# exclusive cases:
#
# defining _use_internal_dependency_generator as 0 means:  __find_requires works, __perl_requires does not
# defining _use_internal_dependency_generator as 1 means:  __perl_requires works, __find_requires does not
#
# Fortunately, the __find_requires processing can handle everything that the
# __perl_requires processing could do, so there is no loss of functionality.
# All we need do is to apply the same editing patterns using __find_requires,
# that we would have applied instead using __perl_requires, and we can still
# edit out all the dependencies we need to eliminate.

# ================================================================

%define	name		groundwork-ganglia-integration
%define	major_release	7
%define	minor_release	0
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

%define	gwpath		/usr/local/groundwork
%define	gangliapath	%{gwpath}/ganglia

# There are several portability issues with this RPM, with respect to
# installing it over various GWMEE releases.
#
# (*) The java_files_for_XXX lists below are intended to deal with the
#     differences between base-product releases on what jar-files are
#     needed to support the GWOSIFramePortlet.class file that is the
#     basis for running the Ganglia portlets, through whatever chain
#     of dependencies is needed for the Java compilation of this class
#     on each release.  However, because of the other portability issues
#     we list here, we have not gotten far enough on most of the GWMEE
#     releases to completely fill in the corresponding list of required
#     files.
#
# (*) The pg_migrate_jboss-idm.sql.ganglia-patch patch was targeted
#     during development for the 7.1.1 and later releases.  It does
#     not apply properly to some earlier releases, notably 7.0.2 and
#     most probably all releases previous to that.
#
# (*) The modify_navigation_objects script was written using the Perl
#     XML::Smart package for convenience, and it turns out we did not
#     include that package in our GroundWork Perl 5.8.9 releases.  So
#     the manual post-install running of the add-ganglia-portal-objects
#     script cannot be carried out in GWMEE 7.1.0 or earlier.
#
# The upshot is that we really only support this RPM for the time being
# on GWMEE 7.1.1 and later, until and unless we work out and test all of
# those issues.

# We list a bunch of Java files that will be extracted from local resources
# on the base-product GWMEE release, and inserted into a new file tree created
# for the Ganglia Integration Module.  Having this list defined in one place
# in the specfile ensures that all uses of this list consistently include the
# same files.  Nonetheless, some run-time analysis is necessary because the
# correct list to use depends on the GWMEE release in place at the time of
# the installation.
#
# The java_files_for_710 list of files, defined below, works or does not work
# for installing this RPM on certain GWMEE releases:
#
# GWMEE 7.0.0:  fails
# GWMEE 7.0.1:  fails
# GWMEE 7.0.2:  fails
# GWMEE 7.1.0:  works
# GWMEE 7.1.1:  works
# GWMEE 7.2.0:  works
#
# If we needed to support any of the failing releases, we would need to test on
# those releases, find the set of jar-files appropriate to each release, and do
# some run-time testing of the installed GWMEE version to choose the appropriate
# list of files for that version.

%define	java_files_for_700							\
    WEB-INF/classes/org/groundwork/portlet/iframe/GWOSIFramePortlet.class	\
    WEB-INF/classes/org/jboss/portlet/iframe/IFramePortlet.class

# We presume without yet testing that this equivalence holds.
%define	java_files_for_701	%{java_files_for_700}

%define	java_files_for_702							\
    WEB-INF/classes/org/groundwork/portlet/iframe/GWOSIFramePortlet.class	\
    WEB-INF/classes/org/jboss/portlet/iframe/IFramePortlet.class

%define	java_files_for_710							\
    WEB-INF/classes/org/groundwork/portlet/iframe/GWOSIFramePortlet.class	\
    WEB-INF/classes/org/jboss/portlet/iframe/IFramePortlet.class		\
    WEB-INF/lib/collagerest-client-*.jar					\
    WEB-INF/lib/collagerest-common-*.jar					\
    WEB-INF/lib/groundwork-container-ext-model-*.jar				\
    WEB-INF/lib/groundwork-container-ext-rest-client-*.jar			\
    WEB-INF/lib/gw-portal-common-*.jar						\
    WEB-INF/lib/jackson-mapper-asl-*.jar					\
    WEB-INF/lib/jackson-xc-*.jar

%define	java_files_for_711	%{java_files_for_710}

%define	java_files_for_72X	%{java_files_for_710}

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Ganglia Integration Module
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')
# Source: %{name}-%{version}.tar.gz
Version: %{version}
BuildArchitectures: noarch
Buildroot: %{_installroot}
Packager: GroundWork <support@gwos.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.  But due to changes in later
# releases of rpmbuild, we may be tied into using --dbpath forever.  See
# comments in the makefile about changes to rpmbuild usage.
# BuildPreReq: patch

# We require no RPM pre-requisites for the groundwork-ganglia-integration RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying Perl scripts which integrate data from Ganglia into
the Nagios data feeds, and which manage the maintenance of
metric thresholds.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)

# Here we filter out unwanted Requires, which are known to be provided in the
# base GroundWork Monitor packages which would be part of our own package's
# PreReq if we still depended on those packages being provided as RPMs.
# So we're working around a deficiency, by actually defeating the dependency
# analysis at install time.

# In the present case, we need to handle both certain Perl modules, and
# some binary-program dependencies.
#
# Well, that would be the case, except that this alternate __find_requires
# for some reason already suppresses the library dependencies that are of
# concern, so as long as we use __find_requires instead of _perl_requires,
# we need only pay attention to the Perl-module dependency edits here.

# The script below references the original __find_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_find_requires %{_tmppath}/%{name}-%{version}-find_requires

# FIX MAJOR:  This is also stripping out the
# config(groundwork-ganglia-integration) = 7.0.0-28081
# dependency, which is not an action we want.

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'	\
	-e '/perl(DBI)/d'					\
	-e '/perl(Data::Dumper)/d'				\
	-e '/perl(Exporter)/d'					\
	-e '/perl(Fcntl)/d'					\
	-e '/perl(Getopt::Long)/d'				\
	-e '/perl(HTML::Entities)/d'				\
	-e '/perl(HTML::Tooltip::Javascript)/d'			\
	-e '/perl(IO::Handle)/d'				\
	-e '/perl(IO::Socket)/d'				\
	-e '/perl(POSIX)/d'					\
	-e '/perl(Safe)/d'					\
	-e '/perl(Term::ReadLine)/d'				\
	-e '/perl(Time::HiRes)/d'				\
	-e '/perl(Time::Local)/d'				\
	-e '/perl(TypedConfig)/d'				\
	-e '/perl(XML::LibXML)/d'				\
	-e '/perl(XML::Smart)/d'				\
	-e '/perl(lib)/d'					\
	-e '/perl(strict)/d'					\
	-e '/perl(vars)/d'					\
	-e '/perl(warnings)/d'
EOF

chmod +x %{edit_find_requires}

# Note:  The __find_requires macro is only used if we set
# _use_internal_dependency_generator to zero above.  Otherwise,
# this redefinition wouldn't make any difference.
%define __find_requires %{edit_find_requires}

# THE FOLLOWING IS NOT USED, because we have enabled __find_requires above.

# The script below references the original __perl_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_perl_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_perl_requires}
#!/bin/sh
%{__perl_requires} $* |\
    sed -e '/perl(HTML::Tooltip::Javascript)/d'			\
	-e '/perl(Time::HiRes)/d'
EOF
chmod +x %{edit_perl_requires}

%define __perl_requires %{edit_perl_requires}

# ================================================================

%build

# make rpmclean
# make rpms

# ================================================================

%install

# This action is already encoded as a dependency for the "make rpms"
# target in the makefile.
# make ganglia_integration_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre
#!/bin/bash

PATH=/bin:/usr/bin

if [ "$1" = 1 ]; then

    # First install.

    : do nothing

fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    : do nothing

fi

# ================================================================

%post
#!/bin/bash

PATH=/bin:/usr/bin

# FIX MAJOR:  Some of the actions below should be done only on first install.
# So we should either restructure this code or make it all idempotent.

if [ "$1" = 1 ]; then

    # First install.

    # We don't create the "ganglia" database on a first-time install,
    # because we don't necessarily know on what machine the user wants
    # to install it.  That is a later, manual task.

    : do nothing

fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    # In theory, we should probably kill an existing check-ganglia service,
    # so the new copy starts up instead.  But that will happen anyway below,
    # when we stop gwservices.

    : do nothing

fi

# Extract a bunch of Java code that we need for this application from an existing war-file
# in the installed GWMEE release, both to make this RPM more portable (to ensure that we get
# Java code that is completely compatible with the GWMEE version we're installing on) and to
# simplify the building of the RPM.  All the artifacts we need can be found in one war-file.
# We extract as the nagios user because otherwise everything will end up being owned by root.

PORTAL_WARFILE=/usr/local/groundwork/foundation/container/jpp/standalone/deployments/portal-groundwork-base.war

gwmee_version() {
    if [ -f /usr/local/groundwork/Info.txt ]; then
	awk '/version[[:space:]]*=[[:space:]]*[[:digit:]]\.[[:digit:]]\.[[:digit:]]/{print $NF}' /usr/local/groundwork/Info.txt
    else
        echo "(none)"
    fi
}

groundwork_version=`gwmee_version`
case "$groundwork_version" in
    (7.0.0) LOCAL_JAVA_FILES=( %{java_files_for_700} ) ;;
    (7.0.1) LOCAL_JAVA_FILES=( %{java_files_for_701} ) ;;
    (7.0.2) LOCAL_JAVA_FILES=( %{java_files_for_702} ) ;;
    (7.1.0) LOCAL_JAVA_FILES=( %{java_files_for_710} ) ;;
    (7.1.1) LOCAL_JAVA_FILES=( %{java_files_for_711} ) ;;
    (7.2.*) LOCAL_JAVA_FILES=( %{java_files_for_72X} ) ;;
    (*) echo "ERROR:  GroundWork version $groundwork_version is not supported by this RPM."
	exit 1
	;;
esac

# This command has to be done very carefully to get the quoting interpreted
# when and as desired, so the jar-file wildcards are not expanded too early
# or not at all.  We need those wildcards to be in play to accommodate
# filename changes across various GWMEE releases.
#
# We check the exit code from the unzip to ensure that we obtained all the
# files we expected.  Unfortunately, even though we then exit this script
# with a non-zero exit code, that exit code is not propagated through to
# the exit code of the "rpm" install command itself.  So the best we can
# do is to emit an error message.
#
echo "UNZIP= UNZIPOPT= unzip -q -b -X -d %{gangliapath} $PORTAL_WARFILE ${LOCAL_JAVA_FILES[@]}" | su nagios
if [ $? -ne 0 ]; then
    echo "ERROR:  RPM installation failed to find all expected files inside"
    echo "        the local portal-groundwork-base.war file; will abort."
    exit 1
fi

# Currently, we don't bother removing these added lines during an uninstall.
HTTPD_CONF=/usr/local/groundwork/apache2/conf/httpd.conf
if [ "`fgrep -c /ganglia-app/ $HTTPD_CONF`" -eq 0 ]; then
    echo "NOTICE:  Saving httpd.conf as httpd.conf.pre_ganglia and editing,"
    echo "         to allow access to Ganglia Integration screens ..."
    # FIX MINOR:  Some of this editing is rather a hack.  Clean it up for the next GWMEE release.
    #
    # The Referer stuff is no longer used in GWMEE 7.X.X, not because it wouldn't still
    # serve a useful purpose, but because a decision was made that we would rely exclusively
    # on other levels of protection for web access.  Disabling the additional layer of
    # protection seems silly to me, but that's they way it is for now.
    #
    #	-e 'print "SetEnvIf Referer ^https?://\\S+/ganglia-app/ framework_referer\n" if m{SetEnvIf\s+Referer\s.*/monarch/\s+framework_referer};' \
    #
    # The /monarch/js alias is no longer needed in GWMEE 7.X.X, given the way we package
    # our monarch.war file and have it deployed.  So including that adjustment here is
    # no longer necessary.  It was put into the original RPM so the Ganglia Integration
    # Module CGI scripts could access the wz_tooltip.js script to support tooltips.
    #
    #	-e 'print "Alias /monarch/js \"/usr/local/groundwork/core/monarch/htdocs/monarch\"\n" if m{^\s*Alias\s+/monarch\s+};' \
    #
    /usr/local/groundwork/perl/bin/perl -p -i.pre_ganglia \
	-e 'print "ProxyPass /ganglia-app/                   http://localhost:8080/ganglia-app/\n" if m{ProxyPass\s+/reports/\s+};' \
	$HTTPD_CONF
    echo "NOTICE:  Restarting Apache to pick up configuration change ..."
    /usr/local/groundwork/ctlscript.sh restart apache
fi

# Stuff to do, idempotently:
# (*) Edit a copy of the $JBOSS_SQLFILE.
# (*) Edit a copy of the $JOSSO_AGENT_FILE.
# (*) Stop gwservices.
# (*) Edit the check-listener.conf file to reference the ganglia-app.war file.
# (*) Put in place the revised $JOSSO_AGENT_FILE.
# (*) Put in place the revised $JBOSS_SQLFILE as a record of what got done.
# (*) Execute the revised $JBOSS_SQLFILE.
# (*) Start gwservices.

GANGLIA_EDIT_DIR=/usr/local/groundwork/tmp/ganglia-edits
JBOSS_SQLFILE=/usr/local/groundwork/core/migration/postgresql/pg_migrate_jboss-idm.sql
JBOSS_SQLFILE_PATCH=/usr/local/groundwork/ganglia/portal/pg_migrate_jboss-idm.sql.ganglia-patch
JOSSO_AGENT_FILE=/usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml
JOSSO_AGENT_FILE_BASENAME=`basename $JOSSO_AGENT_FILE`
JOSSO_AGENT_BACKUP_FILE=$JOSSO_AGENT_FILE.pre_ganglia
JOSSO_EDITED_AGENT_FILE=$JOSSO_AGENT_FILE.edited
JBOSS_SQLFILE_BASENAME=`basename $JBOSS_SQLFILE`
DEPLOYMENTS_DIRECTORY=/usr/local/groundwork/foundation/container/jpp/standalone/deployments
GANGLIA_WARFILE=ganglia-app.war
CHECK_LISTENER_CONFIG_FILE=/usr/local/groundwork/foundation/container/jpp/standalone/configuration/check-listener.conf

# (*) Edit a copy of the $JBOSS_SQLFILE.
if ! egrep -q "idempotently_add_gw_resource.*Ganglia" $JBOSS_SQLFILE; then
    rm -rf   $GANGLIA_EDIT_DIR
    mkdir -p $GANGLIA_EDIT_DIR
    cd $GANGLIA_EDIT_DIR
    cp -p $JBOSS_SQLFILE_PATCH .
    cp -p $JBOSS_SQLFILE .
    patch -b -V simple -z .pre_ganglia -p0 < $JBOSS_SQLFILE_PATCH
    chown nagios:nagios $JBOSS_SQLFILE_BASENAME

    # Apparently, all /usr/local/groundwork/tmp/* file trees get cleaned out when
    # we start gwservices, so some subsequent startup scripting won't work if we are
    # still in some directory under that location when we attempt to start the system.
    cd /tmp
fi

# (*) Edit a copy of the $JOSSO_AGENT_FILE.
rm -f $JOSSO_EDITED_AGENT_FILE
if ! egrep -q "<value>ganglia-app</value>" $JOSSO_AGENT_FILE; then
    echo "Augmenting $JOSSO_AGENT_FILE ..."

    # Make a backup of the original input file.
    cp -p $JOSSO_AGENT_FILE $JOSSO_AGENT_BACKUP_FILE

    # Copy ownership and permissions of the original file to the output file.
    cp -p $JOSSO_AGENT_FILE $JOSSO_EDITED_AGENT_FILE

    if /usr/local/groundwork/perl/bin/perl -w -- - $JOSSO_AGENT_FILE <<'EOF' > $JOSSO_EDITED_AGENT_FILE; then

	# This script edits the josso-agent-config.xml file to add
	# authentication support for the Ganglia Integration Module.

	# Copyright (c) 2017 GroundWork, Inc.  All rights reserved.

	# We cannot easily use a standard patch file for our purposes here, because part
	# of what we need to do is to substitute in a hostname which exactly matches
	# the hostname used in this file for other applications.  And also, we don't
	# necessarily have an exact knowledge of the precise context around the lines we
	# need to insert.  So Perl's very flexible pattern matching comes to the rescue.

	use strict;
	use warnings;

	# Because the patterns we need to process span multiple lines,
	# we need to slurp in the entire file in one go.
	my $config_lines;
	do {
	    local $/;
	    $config_lines = <ARGV>;
	};

	if ( not close ARGV ) {
	    print STDERR "ERROR:  Could not read $ARGV ($!).\n";
	    exit 1;
	}

	# We copy the form of the last <bean>...</bean> section in the appropriate section, without knowing
	# precisely what application it is for.  So our editing has to be specified in a flexible manner.
	if ( $config_lines !~ m{name="ssoPartnerApps".*\n(\s*<bean.*?</bean>\s*\n)(?=\s*</list>)}s ) {
	    print STDERR "ERROR:  Could not find <bean>...</bean> pattern to copy in the $ARGV file.\n";
	    exit 1;
	}
	my $bean_pattern = $1;

	# My first "sex"y patterns, ever!
	$bean_pattern =~ s{(name="id"     .*?<value>)(.*?)(?=</value>)}{$1. "ganglia-app"}sex;
	$bean_pattern =~ s{(name="context".*?<value>)(.*?)(?=</value>)}{$1."/ganglia-app"}sex;

	if ( $config_lines !~ s{(name="ssoPartnerApps".*\n)(?=\s*</list>)}{$1.$bean_pattern}se ) {
	    print STDERR "ERROR:  Could not find <bean>...</bean> pattern in the $ARGV file.\n";
	    exit 1;
	}

	print $config_lines;

EOF

	echo "Editing of $JOSSO_AGENT_FILE succeeded."
    else
	# Clean up the debris so we don't confuse ourselves.
	rm -f $JOSSO_AGENT_BACKUP_FILE $JOSSO_EDITED_AGENT_FILE
	echo "Editing of $JOSSO_AGENT_FILE failed."
	exit 1
    fi
fi

# Atomically install the revised copy of the gwservices script we supply in this package.
# But for safety, always save the previous copy first, in a manner that does not risk
# losing any previous copy.  We avoid colons in the saved filename because they can
# interfere with copying files between systems (scp and the like will try to interpret
# before-and-after strings in certain ways).
echo "NOTICE:  Installing a modified gwservices script ..."
timestamp=`date +%%F_%%H.%%M.%%S`
GWSERVICES=/usr/local/groundwork/core/services/gwservices
cp -p $GWSERVICES $GWSERVICES.pre_ganglia.$timestamp
cp -p $GWSERVICES $GWSERVICES.new
cp /usr/local/groundwork/ganglia/scripts/gwservices $GWSERVICES.new
mv $GWSERVICES.new $GWSERVICES

# We need the "|| true" at the end to sidestep the bash -e flag for this one command,
# primarily because fgrep clumsily changes its return code depending on whether or not
# it matches anything, regardless of whether the command otherwise ran successfully.
dead=`/usr/local/groundwork/ctlscript.sh status gwservices | egrep -v '(is|are|not|copies) running' | fgrep -c dead || true`

# Make sure gwservices is down, so bringing it back up will allow it to
# recognize the Ganglia Integration portal we just installed above.
/usr/local/groundwork/ctlscript.sh stop gwservices

# (*) Edit the check-listener.conf file to reference the ganglia-app.war file.
# Do so idempotently in case this package has been repeatedly installed and uninstalled
# without having this line cleaned up during an uninstall.
if ! egrep -q "^[^#]*/$GANGLIA_WARFILE" $CHECK_LISTENER_CONFIG_FILE; then
    echo "NOTICE:  Adding $GANGLIA_WARFILE to the startup scripting configuration ..."
    cp -p $CHECK_LISTENER_CONFIG_FILE $CHECK_LISTENER_CONFIG_FILE.pre_ganglia
    echo "tertiary_deployment = $DEPLOYMENTS_DIRECTORY/$GANGLIA_WARFILE" >> $CHECK_LISTENER_CONFIG_FILE
fi

# (*) Put in place the revised $JOSSO_AGENT_FILE.
# Apparently, putting this in place before any of the menu items we want to establish are
# defined causes it to actively interfere with correct operation.  So we must wait until
# afterward, bringing down gwservices while we put this in play at that time.  Later on,
# we don't bother to remove the /ganglia-app line from the josso-agent-config.xml file
# during an uninstall, because leaving it there has no deleterious effect.  The change we
# make here will be picked up the next time gwservices is restarted.
if [ -f $JOSSO_EDITED_AGENT_FILE ]; then
    echo "NOTICE:  Installing a modified $JOSSO_AGENT_FILE_BASENAME file ..."
    mv $JOSSO_EDITED_AGENT_FILE $JOSSO_AGENT_FILE
fi

# (*) Put in place the revised $JBOSS_SQLFILE as a record of what got done.
if [ -f $GANGLIA_EDIT_DIR/$JBOSS_SQLFILE_BASENAME ]; then
    echo "NOTICE:  Installing a modified $JBOSS_SQLFILE_BASENAME file ..."
    cp -p $JBOSS_SQLFILE $JBOSS_SQLFILE.pre_ganglia
    cp -p $GANGLIA_EDIT_DIR/$JBOSS_SQLFILE_BASENAME $JBOSS_SQLFILE
fi

# (*) Execute the revised $JBOSS_SQLFILE.
su nagios -c "/usr/local/groundwork/postgresql/bin/psql -U jboss jboss-idm < $JBOSS_SQLFILE"

# Restart gwservices only if it was already running (maybe "broken"
# [partially running], but still not dead) before we made this fix.
if [ $dead -eq 0 ]; then
    /usr/local/groundwork/ctlscript.sh start gwservices
fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

gwmee_version() {
    if [ -f /usr/local/groundwork/Info.txt ]; then
	awk '/version[[:space:]]*=[[:space:]]*[[:digit:]]\.[[:digit:]]\.[[:digit:]]/{print $NF}' /usr/local/groundwork/Info.txt
    else
        echo "(none)"
    fi
}

groundwork_version=`gwmee_version`
case "$groundwork_version" in
    (7.0.0) LOCAL_JAVA_FILES=( %{java_files_for_700} ) ;;
    (7.0.1) LOCAL_JAVA_FILES=( %{java_files_for_701} ) ;;
    (7.0.2) LOCAL_JAVA_FILES=( %{java_files_for_702} ) ;;
    (7.1.0) LOCAL_JAVA_FILES=( %{java_files_for_710} ) ;;
    (7.1.1) LOCAL_JAVA_FILES=( %{java_files_for_711} ) ;;
    (7.2.*) LOCAL_JAVA_FILES=( %{java_files_for_72X} ) ;;
    (*) echo "ERROR:  GroundWork version $groundwork_version is not supported by this RPM."
	# Hey, we're trying to uninstall here.  So if we cannot be precise about
	# the Java files we want to delete, well then, so be it.  That's too bad,
	# but it shouldn't hold up the entire uninstall, which would happen if we
	# exited here with a non-zero return code.  So instead, we just define
	# the set of Java files to be empty, which will mean that the actual
	# Java files we have in place will be left behind by the uninstall.
	# Not pretty, but not terribly harmful, either.
	LOCAL_JAVA_FILES=( )
	;;
esac

if [ "$1" = 0 ]; then

    # Last uninstall.

    # We ought to kill an existing check-ganglia service, somehow.  But that
    # is very hard to do.  If we place core/services/check-ganglia/down and
    # core/services/check-ganglia/log/down files, then try to shut down the
    # check-ganglia service:
    #
    #    touch /usr/local/groundwork/core/services/check-ganglia/down
    #    touch /usr/local/groundwork/core/services/check-ganglia/log/down
    #    /usr/local/groundwork/common/bin/svc -d -x /usr/local/groundwork/core/services/check-ganglia
    #    /usr/local/groundwork/common/bin/svc -d -x /usr/local/groundwork/core/services/check-ganglia/log
    #
    # (or with the -X option instead of -x), the supervise processes will go
    # away, but then pop back up again.  And the presence of the "down" files
    # will interfere with fully deleting the core/services/check-ganglia/
    # file tree.  So the best thing to do is to not do anything at this time,
    # and just wait until the post-uninstall script to perform final cleanup.
    #
    # Also note that even without explicit cleanup in the post-uninstall script,
    # the check-ganglia service will be stopped the next time all of gwservices
    # is stopped.

    # Since during the install, we unzipped some externally-obtained files into our
    # exploded-warfile directory, there will be some files left installed after an
    # uninstall if we don't clean them up explicitly.  Deleting the files at this
    # time will then allow RPM to remove the entire empty directory tree.
    for file in "${LOCAL_JAVA_FILES[@]}"; do
	# Here we must not quote the filepath, so as to allow the wildcards
	# we have specified to finally take effect in filename matching.
	rm -f %{gangliapath}/$file
    done

    GANGLIA_WARFILE=ganglia-app.war
    CHECK_LISTENER_CONFIG_FILE=/usr/local/groundwork/foundation/container/jpp/standalone/configuration/check-listener.conf

    # (*) Edit the check-listener.conf file to remove any reference to the ganglia-app.war file.
    if egrep -q "^[^#]*/$GANGLIA_WARFILE" $CHECK_LISTENER_CONFIG_FILE; then
	sed -i -e "/^[^#]*\/$GANGLIA_WARFILE/d" $CHECK_LISTENER_CONFIG_FILE
    fi

fi

# ================================================================

%postun
#!/bin/bash

PATH=/bin:/usr/bin

# Kill any check-ganglia service processes still remaining:
#
# supervise check-ganglia
# /usr/local/groundwork/perl/bin/.perl.bin -w -- /usr/local/groundwork/nagios/libexec/check_ganglia.pl
#
# supervise check-ganglia/log
# dumblog main/log
#
# If the entire core/services/check-ganglia/... file tree has been removed
# during the uninstall, these processes will not pop back up again.  But
# note that if that file tree no longer exists, we cannot use "svc" to
# shut down these processes; we must hunt them down ourselves.  We need
# not worry about the dumblog process; if we kill its parent supervise
# process with a SIGTERM, the dumblog will go away as well.  But we do
# need to shut down the check_ganglia.pl script separately.
#
# So we need to:
# (*) Find and kill the "supervise check-ganglia" process.
# (*) Find and kill the check_ganglia.pl process.
# (*) Find and kill the "supervise check-ganglia/log" process.
#
# We can combine the first and third of these steps without any danger.
#
# We're not picky here about whether this post-uninstall situation is after
# a final uninstall, or whether this is after an upgrade.  In either case,
# it's fine to kill all of the designated processes.  If we are operating
# after an upgrade, the system will soon automatically restart affected
# processes using the latest code, which puts the system back into working
# order while guaranteeing no interference from old copies of the software.

supervise_pids=`ps -C supervise --no-headers -o pid,args | awk '/check-ganglia/{print $1}'`
script_pids=`ps -C .perl.bin --no-headers -o pid,args | awk '/check_ganglia.pl/{print $1}'`

if [ -n "$supervise_pids" ]; then
    kill -TERM $supervise_pids
fi
if [ -n "$script_pids" ]; then
    kill -TERM $script_pids
fi

if [ "$1" = 0 ]; then

    # Last uninstall.

    # FIX MAJOR:  We need some code here to remove the "Configuration > Ganglia Thresholds"
    # and "Advanced > Ganglia Web Server" menu items.  This is tricky, because the only way
    # to delete such menu items is either manually, through the Portal administrative UI, or
    # by exporting the site, deleting the undesired navigational elements, and then importing
    # the modified site.  We do not yet have such edits automated.  See comments in the
    # modify_navigation_objects and add-ganglia-portal-objects scripts about the special
    # importMode parameter needed for such work.

    DEPLOYMENTS_DIRECTORY=/usr/local/groundwork/foundation/container/jpp/standalone/deployments
    GANGLIA_WARFILE=ganglia-app.war

    # Under some odd circumstances, a ganglia-app.war.dodeploy file can be left behind.
    # It's probably harmless, but it's better to completely clean up after ourselves.
    rm -f $DEPLOYMENTS_DIRECTORY/$GANGLIA_WARFILE.*deploy*

    # Having removed an application, we must restart gwservices so the system doesn't get
    # confused about having it missing, and consequently wrap itself up and disallow logins.
    /usr/local/groundwork/ctlscript.sh restart gwservices

fi

# ================================================================

%files

%defattr(0644,nobody,nagios)

# Certain configuration files are intentionally only readable by the owner,
# to enforce security precautions (they contain database-access credentials).
# One effect of this is that the associated scripts must run as the "nagios"
# user to read these files.

# FIX MAJOR:  from the previous open-source release for ganglia integration; to be subsumed by the RPM
# -rwxr-xr-x  1 gherteg operations 2372 Oct  5 10:05 /home/gherteg/WesternGeco/svn/gwlabs/modules/ganglia/check_ganglia/install.sh

%dir %attr(0755,nagios,nagios) %{gangliapath}
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/groundwork
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/groundwork/portlet
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/groundwork/portlet/iframe
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/jboss
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/jboss/portlet
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/classes/org/jboss/portlet/iframe
%dir %attr(0755,nagios,nagios) %{gangliapath}/WEB-INF/lib
%dir %attr(0700,nagios,nagios) %{gangliapath}/backups
%dir %attr(0755,nagios,nagios) %{gangliapath}/cgi-bin
%dir %attr(0755,nagios,nagios) %{gangliapath}/info
%dir %attr(0755,nagios,nagios) %{gangliapath}/jsp
%dir %attr(0755,nagios,nagios) %{gangliapath}/portal
%dir %attr(0755,nagios,nagios) %{gangliapath}/scripts

%attr(0444,nagios,nagios) %{gangliapath}/info/build_info
%attr(0755,nagios,nagios) %{gangliapath}/scripts/add-ganglia-portal-objects
%attr(0755,nagios,nagios) %{gangliapath}/scripts/gwservices
%attr(0755,nagios,nagios) %{gangliapath}/scripts/master_migration_to_pg.pl.6.7.0_extended
%attr(0755,nagios,nagios) %{gangliapath}/scripts/mysql2postgresql.sh.6.7.0_extended
%attr(0644,nagios,nagios) %{gangliapath}/scripts/mysql_ganglia_show_duplicates.sql
%attr(0644,nagios,nagios) %{gangliapath}/scripts/mysql_ganglia_unique_constraints.sql

%attr(0500,nagios,nagios) %{gwpath}/core/migration/modify_navigation_objects

%dir %attr(0755,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/GroundWork_Monitor_and_Ganglia_Integration_System_Administrator_Guide_%{version}.pdf

# Everything under %{gangliapath} essentially constitutes the exploded ganglia-app.war file.
# Some stuff under %{gangliapath} is not used to support the web GUI application, though.

%attr(0755,nagios,nagios) %{gangliapath}/cgi-bin/GangliaConfigAdmin.cgi
%attr(0755,nagios,nagios) %{gangliapath}/cgi-bin/GangliaWebServers.cgi

%attr(0644,nagios,nagios) %{gangliapath}/WEB-INF/context.xml
%attr(0644,nagios,nagios) %{gangliapath}/WEB-INF/jboss-deployment-structure.xml
%attr(0644,nagios,nagios) %{gangliapath}/WEB-INF/jboss-web.xml
%attr(0644,nagios,nagios) %{gangliapath}/WEB-INF/portlet.xml
%attr(0644,nagios,nagios) %{gangliapath}/WEB-INF/web.xml
%attr(0644,nagios,nagios) %{gangliapath}/jsp/iframe.jsp

# The ownership and permissions of these files are set so the "nagios"
# user can securely read the files (according to TypedConfig's notion
# of security).  We would perhaps prefer that the files be owned by
# root and be readable by the nagios group, so the nagios user can read
# but not edit the content, but that would take an extension to the
# TypedConfig package.
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/GangliaConfigAdmin.conf
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/GangliaWebServers.conf
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/check_ganglia.conf

%config %attr(0644,nagios,nagios) %{gangliapath}/portal/navigation-ganglia-thresholds.xml
%config %attr(0644,nagios,nagios) %{gangliapath}/portal/navigation-ganglia-web-servers.xml
%config %attr(0644,nagios,nagios) %{gangliapath}/portal/pages.xml
%config %attr(0600,nagios,nagios) %{gangliapath}/portal/pg_migrate_jboss-idm.sql.ganglia-patch

%attr(0744,nagios,nagios) %{gwpath}/nagios/libexec/check_ganglia.pl

# FIX LATER:  Revisit this section to see how much can be eliminated by just having
# "supervise" dynamically create what it needs, when it first starts this service.
#
%dir %attr(3700,root,root)     %{gwpath}/core/services/check-ganglia
%dir %attr(2755,root,root)     %{gwpath}/core/services/check-ganglia/log
%dir %attr(2755,nagios,nagios) %{gwpath}/core/services/check-ganglia/log/main
     %attr(0644,nagios,nagios) %{gwpath}/core/services/check-ganglia/log/main/log
     %attr(0755,root,root)     %{gwpath}/core/services/check-ganglia/log/run
%dir %attr(2700,root,root)     %{gwpath}/core/services/check-ganglia/log/supervise
     %attr(0644,root,root)     %{gwpath}/core/services/check-ganglia/log/supervise/status
     %attr(0600,root,root)     %{gwpath}/core/services/check-ganglia/log/supervise/lock
     %attr(0755,root,root)     %{gwpath}/core/services/check-ganglia/run
%dir %attr(2700,root,root)     %{gwpath}/core/services/check-ganglia/supervise
     %attr(0644,root,root)     %{gwpath}/core/services/check-ganglia/supervise/status
     %attr(0600,root,root)     %{gwpath}/core/services/check-ganglia/supervise/lock

# These two files are actually named pipes.
# Possibly, we don't need to include them in the RPM if "supervise" will create them
# if they do not already exist.  That remains to be tested (see above).
%attr(0600,nagios,nagios) %{gwpath}/core/services/check-ganglia/log/supervise/control
%attr(0600,nagios,nagios) %{gwpath}/core/services/check-ganglia/supervise/control

# Executing this script is a destructive operation (it will wipe out any previous
# "ganglia" database).  Creating this database is not done automatically during
# RPM installation because we cannot assume where the customer wants to have this
# database reside.
#
# We should no longer include this script because it is a MySQL dump, which has no
# real utility in a PostgreSQL-based GWMEE release.  I'm still doing so only in
# case it might serve some useful purpose during a migration from a MySQL-based
# installation to a PostgreSQL-based installation.
#
%attr(0644,nagios,nagios) %{gwpath}/core/databases/ganglia_db_create.sql

# Executing this script is a destructive operation (it will wipe out any previous
# "ganglia" database).  Creating this database is not done automatically during
# RPM installation because we cannot assume where the customer wants to have this
# database reside.  Permission of this file is restricted because it contains a
# cleartext initial password.
%attr(0600,nagios,nagios) %{gwpath}/core/databases/postgresql/create-ganglia-db.sql

# The ganglia-db.sql script creates all the "ganglia"-database objects (tables,
# sequences, indexes, and constraints) for a PostgreSQL version of the database.
# The ganglia-seed.sql script adds a few initial rows in some tables, representing
# default values.
%attr(0644,nagios,nagios) %{gwpath}/core/databases/postgresql/ganglia-db.sql
%attr(0644,nagios,nagios) %{gwpath}/core/databases/postgresql/ganglia-seed.sql

# These symlinks need special treatment.  In CentOS7 rpmbuild, if we specify the
# 0777 mode in the %attr() explicitly, that generates an "Explicit %attr() mode
# not applicaple to symlink" warning message at build time.  It seems to me that
# it ought to accept a 0777 mode silently, but that is not currently the case.
# So we suppress that warning by using a dash as the mode, to just accept the
# mode as created under the build root.
#
# check_ganglia.pl log file (script is run as a service, and prints to stdout, which is redirected here):
#                         %{gwpath}/logs/check_ganglia.log -> ../core/services/check-ganglia/log/main/log
# %attr(0777,nagios,nagios) %{gwpath}/logs/check_ganglia.log
%attr(-,nagios,nagios) %{gwpath}/logs/check_ganglia.log
#
# The virtual war-file for this application, really just a symlink to already-exploded
# static content installed by this RPM.  This path suffices for both single-JBoss and
# dual-JBoss systems.
%attr(-,nagios,nagios) %{gwpath}/foundation/container/jpp/standalone/deployments/ganglia-app.war

# These files used to be included in releases of the Ganglia Integration Module for
# at least the PostgreSQL-based GWMEE 6.X.X releases.  They are no longer needed to
# support the Ganglia Integration Module for the GWMEE 7.X.X releases, because we
# have included all the portal stuff as statically exploded files in the WEB-INF/
# and jsp/ directories, plus some install-time scripting that otherwise sets up the
# portal menu items and the portal pages that back them up.
#
# %attr(0444,nagios,nagios) %{gwpath}/foundation/container/webapps/jboss/jboss-portal.sar/portal-ganglia-integration.war
# %attr(0444,nagios,nagios) %{gwpath}/foundation/container/webapps/ganglia-integration.war

%config %attr(0644,root,root) /etc/logrotate.d/groundwork-ganglia

# FIX MAJOR:  Include here my scripting and files that take care of on-site editing at
# install time, to add the "Ganglia" role, to add the pages.xml stuff, and so forth.

# ================================================================

%changelog
* Thu Jun  8 2017 Glenn Herteg <support@groundworkopensource.com> 7.0.0
- Port to run under the GWMEE 7.X.X portal structure, specifically
  targeting the GWMEE 7.1.1 release.  Also update the look and feel of
  the CGI screens to match the appearance of current Monarch and related
  UI screens.  Renamed the check_ganglia service to be the check-ganglia
  service to comport better with the naming of other gwservices.

* Fri May 10 2013 Glenn Herteg <support@groundworkopensource.com> 6.0.1
- Strengthen the XML parsing to prevent external entity references from
  being recognized.  This will only work with a sufficiently new libxml2
  library, such as we will have available in the GWMEE 7.0.0 release.

* Wed Nov 14 2012 Glenn Herteg <support@groundworkopensource.com> 6.0.0
- Ported to support PostgreSQL.
- Fixed a few minor bugs.
- Added the /etc/logrotate.d/groundwork-ganglia file, to get log files
  properly rotated.

* Fri Mar 25 2011 Glenn Herteg <support@groundworkopensource.com> 5.1.0
- Cleaned up Perl warnings in the CGI scripting so logging them doesn't
  slow down the user interaction.

* Wed Mar  2 2011 Glenn Herteg <support@groundworkopensource.com> 5.1.0
- Added support for a default Ganglia web server page in Ganglia Views.

* Sun Feb 27 2011 Glenn Herteg <support@groundworkopensource.com> 5.0.0
- Added the full revised documentation.

* Mon Feb 21 2011 Glenn Herteg <support@groundworkopensource.com> 5.0.0
- Added a "validate configuration" capability, to detect odd setups that
  might cause confusion.

* Fri Feb 18 2011 Glenn Herteg <support@groundworkopensource.com> 5.0.0
- ported to be compatible with the GWMEE 6.4 environment

* Wed Jan 23 2008 Glenn Herteg <support@groundworkopensource.com> 4.2.1
- added a Perl warning flag, to catch compilation errors
- fixed a compilation warning about logging an error message to an unopened channel

* Thu Dec 13 2007 Glenn Herteg <support@groundworkopensource.com> 4.2.0
- added an "enable_processing" option to the config file, to provide safe installs

* Wed Dec 12 2007 Glenn Herteg <support@groundworkopensource.com> 4.1.0
- first packaged as an RPM
- check_ganglia.conf format changed as some options are moved out of the script
