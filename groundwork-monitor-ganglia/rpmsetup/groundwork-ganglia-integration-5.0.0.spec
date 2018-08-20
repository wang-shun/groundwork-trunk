# This is the groundwork-ganglia-integration spec file needed to construct
# the Groundwork Monitor Ganglia Integration Module RPM.

# Copyright 2008-2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
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
%define	major_release	5
%define	minor_release	0
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

%define	gwpath		/usr/local/groundwork
%define gangliapath	%{gwpath}/ganglia

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
# "rpmbuild --dbpath" option in the makefile.
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
# config(groundwork-ganglia-integration) = 5.0.0-17749
# dependency, which is not an action we want.

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'	\
	-e '/perl(DBI)/d'					\
	-e '/perl(Data::Dumper)/d'				\
	-e '/perl(Fcntl)/d'					\
	-e '/perl(Getopt::Long)/d'				\
	-e '/perl(HTML::Tooltip::Javascript)/d'			\
	-e '/perl(IO::Socket)/d'				\
	-e '/perl(Safe)/d'					\
	-e '/perl(Time::HiRes)/d'				\
	-e '/perl(Time::Local)/d'				\
	-e '/perl(TypedConfig)/d'				\
	-e '/perl(XML::LibXML)/d'				\
	-e '/perl(lib)/d'					\
	-e '/perl(strict)/d'					\
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

if [ "$1" = 1 ]; then

    # First install.

    # We don't create the "ganglia" database on a first-time install,
    # because we don't necessarily know on what machine the user wants
    # to install it.  That is a later, manual task.

    : do nothing

fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    # In theory, we should probably kill an existing check_ganglia service,
    # so the new copy starts up instead.  But that will happen anyway below,
    # when we stop gwservices.

    : do nothing

fi

# Currently, we don't bother removing these added lines during an uninstall.
HTTPD_CONF=/usr/local/groundwork/apache2/conf/httpd.conf
if [ "`fgrep -c /ganglia-integration/ $HTTPD_CONF`" -eq 0 ]; then
    echo "NOTICE:  Saving httpd.conf as httpd.conf.pre_ganglia and editing,"
    echo "         to allow access to Ganglia Integration screens ..."
    # FIX MINOR:  Some of this editing is rather a hack.  Clean it up for the next GWMEE release.
    /usr/local/groundwork/perl/bin/perl -p -i.pre_ganglia \
	-e 'print "SetEnvIf Referer ^https?://\\S+/ganglia-integration/ framework_referer\n" if m{SetEnvIf\s+Referer\s.*/monarch/\s+framework_referer};' \
	-e 'print "ProxyPass /ganglia-integration/  http://localhost:8080/ganglia-integration/\n" if m{ProxyPass\s+/monarch/\s+};' \
	-e 'print "Alias /monarch/js \"/usr/local/groundwork/core/monarch/htdocs/monarch\"\n" if m{^\s*Alias\s+/monarch\s+};' \
	$HTTPD_CONF
    echo "NOTICE:  Restarting Apache to pick up configuration change ..."
    /usr/local/groundwork/ctlscript.sh restart apache
fi

# Later on, we don't bother to remove the /ganglia-integration line from the
# /usr/local/groundwork/config/resources/josso-agent-config.xml file
# during an uninstall, because leaving it there has no deleterious effect.
# The change we make here will be picked up the next time gwservices is
    # restarted.  It will be stopped and perhaps started again via the script
# actions invoked below.
JOSSO_FILE=/usr/local/groundwork/config/resources/josso-agent-config.xml
if [ -f $JOSSO_FILE ]; then
    if [ "`fgrep -c /ganglia-integration $JOSSO_FILE`" -eq 0 ]; then
	echo "NOTICE:  Saving josso-agent-config.xml as josso-agent-config.xml.pre_ganglia"
	echo "         and editing, to allow access to Ganglia Integration screens ..."
	/usr/local/groundwork/perl/bin/perl -p -i.pre_ganglia \
	    -e 'print "\t\t\t\t\t<agent:partner-app id=\"ganglia-integration\" context=\"/ganglia-integration\"/>\n" if m{</agent:partner-apps>};' \
	    $JOSSO_FILE
    fi
fi

# Atomically install the revised copy of the gwservices script we supply in this package.
# But for safety, always save the previous copy first, in a manner that does not risk
# losing any previous copy.  We avoid colons in the saved filename because they can
# interfere with copying files between systems (scp and the like will try to interpret
# before-and-after strings in certain ways).
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

# Restart gwservices only if it was already running (maybe "broken"
# [partially running], but still not dead) before we made this fix.
if [ $dead -eq 0 ]; then
    /usr/local/groundwork/ctlscript.sh start gwservices
fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

if [ "$1" = 0 ]; then

    # Last uninstall.

    # FIX MAJOR:  We should probably kill an existing check_ganglia service, somehow.
    # That will happen anyway, though, the next time gwservices is stopped.

    : do nothing

fi

# ================================================================

%postun

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
%dir %attr(0755,nagios,nagios) %{gangliapath}/info
%dir %attr(0755,nagios,nagios) %{gangliapath}/scripts

%attr(0444,nagios,nagios) %{gangliapath}/info/build_info
%attr(0755,nagios,nagios) %{gangliapath}/scripts/gwservices

%dir %attr(0755,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/GroundWork_Monitor_and_Ganglia_Integration_System_Administrator_Guide_%{version}.pdf

# These copies won't actually be used in normal production; they're installed in
# this directory mainly to provide an easy way to inspect these scripts.  The
# copies in the deployed .war files are the ones that will really be active.
%attr(0755,nagios,nagios) %{gwpath}/core/monarch/cgi-bin/monarch/GangliaConfigAdmin.cgi
%attr(0755,nagios,nagios) %{gwpath}/core/monarch/cgi-bin/monarch/GangliaWebServers.cgi

# The ownership and permissions of these files are set so the "nagios"
# user can securely read the files (according to TypedConfig's notion
# of security).  We would perhaps prefer that the files be owned by
# root and be readable by the nagios group, so the nagios user can read
# but not edit the content, but that would take an extension to the
# TypedConfig package.
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/GangliaConfigAdmin.conf
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/GangliaWebServers.conf
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/config/check_ganglia.conf

%attr(0744,nagios,nagios) %{gwpath}/nagios/libexec/check_ganglia.pl

%dir %attr(3700,root,root) %{gwpath}/core/services/check_ganglia
%dir %attr(2755,root,root) %{gwpath}/core/services/check_ganglia/log
%dir %attr(2755,nagios,nagios) %{gwpath}/core/services/check_ganglia/log/main
%attr(0644,nagios,nagios) %{gwpath}/core/services/check_ganglia/log/main/log
%attr(0755,root,root) %{gwpath}/core/services/check_ganglia/log/run
%dir %attr(2700,root,root) %{gwpath}/core/services/check_ganglia/log/supervise
%attr(0644,root,root) %{gwpath}/core/services/check_ganglia/log/supervise/status
%attr(0600,root,root) %{gwpath}/core/services/check_ganglia/log/supervise/lock
%attr(0755,root,root) %{gwpath}/core/services/check_ganglia/run
%dir %attr(2700,root,root) %{gwpath}/core/services/check_ganglia/supervise
%attr(0644,root,root) %{gwpath}/core/services/check_ganglia/supervise/status
%attr(0600,root,root) %{gwpath}/core/services/check_ganglia/supervise/lock

# These two files are actually named pipes.
%attr(0600,nagios,nagios) %{gwpath}/core/services/check_ganglia/log/supervise/control
%attr(0600,nagios,nagios) %{gwpath}/core/services/check_ganglia/supervise/control

# Executing this script is a destructive operation (it will wipe out any previous
# "ganglia" database).  Creating this database is not done automatically during
# RPM installation because we cannot assume where the customer wants to have this
# database reside.
%attr(0644,nagios,nagios) %{gwpath}/core/databases/ganglia_db_create.sql

# These symlinks need special treatment.
# check_ganglia.pl log file (script is run as a service, and prints to stdout, which is redirected here):
#                         %{gwpath}/logs/check_ganglia.log -> ../core/services/check_ganglia/log/main/log
%attr(0777,nagios,nagios) %{gwpath}/logs/check_ganglia.log

# FIX MAJOR:  do we need a logrotate.d script, to clean up the check_ganglia.pl log file?
# FIX MAJOR:  Do we have any kind of /etc/logrotate.d/* file to include in any of these RPMs?
# %config %attr(0644,root,root) /etc/logrotate.d/ganglia-integration

%attr(0444,nagios,nagios) %{gwpath}/foundation/container/webapps/jboss/jboss-portal.sar/portal-ganglia-integration.war
%attr(0444,nagios,nagios) %{gwpath}/foundation/container/webapps/ganglia-integration.war

# ================================================================

%changelog
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
