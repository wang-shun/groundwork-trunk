# This is the groundwork cloud-connector spec file needed to construct
# the Groundwork Monitor Cloud Connector RPM.

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# The construction of this spec file is a bit unusual in that it is
# intended to be self-contained; that is, we don't want to depend on
# the particular user's dot-files (~/.rpmrc or ~/.rpmmacros) and build
# structure (pre-established ~/rpmbuild/*/ directories).  Also, we don't
# start with a source RPM or tarball.  Rather, we assume the source code
# is already splayed out, and all we're trying to do at this stage is to
# construct the RPM(s).

# ================================================================

# TO DO:
# (*) supply all the desired pre/post scripting for installs,
#     upgrades, and uninstalls of the Cloud Connector software

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

%define	name		groundwork-cloud-connector
%define	major_release	0
%define	minor_release	0
%define	patch_release	1
%define	version		%{major_release}.%{minor_release}.%{patch_release}

# We would disable the dist definition by replacing % with # to comment out this next line, if this were a noarch RPM.
%define	dist		%(if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)

%define	gwpath		/usr/local/groundwork
%define	cloudpath	%{gwpath}/cloud

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Cloud Connector Software
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')%{?dist}
# Source: %{name}-%{version}.tar.gz
Version: %{version}
# BuildArchitectures: noarch
Buildroot: %{_installroot}
Packager: GroundWork <support@gwos.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require no RPM pre-requisites for the groundwork-cloud-connector RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying software to integrate with various cloud-management
products.  This provides dynamic provisioning of monitoring
for the cloud infrastructure.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)

# ================================================================

# Here we filter out unwanted Requires, which are known to be provided in the
# base GroundWork Monitor packages which would be part of our own package's
# PreReq if we still depended on those packages being provided as RPMs.
# So we're working around a deficiency, by actually defeating the dependency
# analysis at install time.

# In the present case, we need to handle both certain Perl modules, and
# some binary-program dependencies for programs of a different architecture
# that are included in this package for distribution to other run-time
# machines but will not actually be run on the machine where this package
# is installed.
#
# Well, that would be the case, except that this alternate __find_requires
# for some reason already suppresses the library dependencies that are of
# concern, so as long as we use __find_requires instead of _perl_requires,
# we need only pay attention to the Perl-module dependency edits here.

# The script below references the original __find_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_find_requires %{_tmppath}/%{name}-%{version}-find_requires

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'	\
	-e '/perl(CGI::Ajax)/d'					\
	-e '/perl(MonarchClouds)/d'				\
	-e '/perl(MonarchDoc)/d'				\
	-e '/perl(MonarchForms)/d'				\
	-e '/perl(MonarchInstrument)/d'				\
	-e '/perl(MonarchStorProc)/d'				\
	-e '/perl(MonarchValidation)/d'				\
	-e '/perl(TypedConfig)/d'				\
	-e '/perl(dassmonarch)/d'				\
	-e '/perl(monarchWrapper)/d'				\
	-e '/perl(utils)/d'
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
    sed -e '/perl(TypedConfig)/d'		\
	-e '/perl(utils)/d'
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
# make cloud_connector_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre
#!/bin/bash

PATH=/bin:/usr/bin

# FIX THIS:
# $1 will be a number when we install an RPM, but will be a string when this
# is converted to a .deb file on Ubuntu using alien.  Possible values are:
#
#     abort-deconfigure
#     abort-install
#     abort-remove
#     abort-upgrade
#     configure
#     deconfigure
#     disappear
#     failed-upgrade
#     install
#     purge
#     remove
#     upgrade
#
# See http://www.debian.org/doc/debian-policy/ch-maintainerscripts.html for details.
if [ "$1" = 1 ]; then

    # First install.

    : do nothing

    # echo "INFO:  This installation of %{name} is considered a first install;"
    # echo "       no attempt will be made to kill a previous Cloud Connector."

fi

if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  We ought to shut down the Cloud Connector Software if it's running,
    # so the upgrade can go smoothly.  Make sure we do that in a way that won't cause
    # it to be started up again immediately by some daemon-keeper process, and that
    # will be easy to reverse later on.

    : do nothing

fi

# ================================================================

%post
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

newline='
'

if [ "$1" = 1 -o "$1" = "configure" -o "$1" = "abort-remove" ]; then

    # First install, or aborted remove (under Unbuntu).

    # Make sure the Cloud Connector nagios cron job is in place.  We carry out this action
    # in small steps to minimize the likelihood of leaving the system in a mangled state,
    # having corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  Cloud Connector RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/cloud_config.pl '`" -eq 0 ]; then
	# The cron job is not installed.  Do so now.  We don't bother to try to delete
	# an existing cron job if it is currently commented out; we just add a new one.
	new_cron_job='*/15 * * * * /usr/local/groundwork/cloud/scripts/cloud_config.pl > /dev/null 2>&1'
	echo "$old_crontab$newline$new_cron_job" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Cloud Connector RPM could not install its nagios cron job" 1>&2
	    exit 1
	fi
    else
	echo "NOTICE:  The Cloud Connector nagios cron job looks like it was already in place."
    fi

    # Make sure the jbossportal database includes the latest copy of the Cloud Connector portlet.
    # FIX MAJOR:  Roger claims this should be unnecessary, but experience shows otherwise,
    # at least after a separate uninstall of a previous RPM.
%include scripts/recreate_jbossportal
    if [ $? -ne 0 ]; then
	echo "FATAL:  Cloud Connector RPM could not re-create the jbossportal database" 1>&2
	exit 1
    fi

else

    # FIX LATER:  should we be testing this condition here?
    # FIX THIS:  How do we make a portable test here, to support Unbuntu as well?
    # if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  Shut down any old processes associated with the Cloud Connector software.
    # (But why would we do so only now?)

    # Make sure the jbossportal database includes the latest copy of the Cloud Connector portlet.
%include scripts/recreate_jbossportal
    if [ $? -ne 0 ]; then
	echo "FATAL:  Cloud Connector RPM could not re-create the jbossportal database" 1>&2
	exit 1
    fi

fi

# Currently, we don't bother removing this added line during an uninstall.
HTTPD_CONF=/usr/local/groundwork/apache2/conf/httpd.conf
if [ "`fgrep -c monarch_clouds.cgi $HTTPD_CONF`" -eq 0 ]; then
    /usr/local/groundwork/perl/bin/perl -p -i.pre_clouds \
	-e 'print "SetEnvIf Referer ^https?://\\S+/monarch/cgi-bin/monarch_clouds.cgi[?]? framework_referer\n" if /monarch_tree.cgi/;' \
	$HTTPD_CONF
fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

# FIX MAJOR:  we should probably kill the Cloud Connector software here

# FIX LATER:  The configuration file should be backed up somewhere before the
# entire package is deleted, since it may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.

# FIX LATER:  The following formulation of stopping the Cloud Connector
# will need to change when we change the Perl it uses from /usr/bin/perl to
# /usr/local/groundwork/perl/bin/perl as part of the Bitrock packaging.

# Stop the Cloud Connector.
# FIX THIS:  fill in here as needed

if [ "$1" = 0 -o "$1" = "remove" ]; then

    # Last uninstall.

    # Make sure the Cloud Connector nagios cron job is removed.  We carry out this action
    # in small steps to minimize the likelihood of leaving the system in a mangled state,
    # having corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  Cloud Connector RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/cloud_config.pl '`" -eq 0 ]; then
	# The cron job is not installed.  We don't bother to try to delete
	# an existing cron job if it is currently commented out.
	echo "NOTICE:  The Cloud Connector nagios cron job looks like it was already commented out or removed."
    else
	# new_cron_job='*/15 * * * * /usr/local/groundwork/cloud/scripts/cloud_config.pl > /dev/null 2>&1'
	new_crontab=`echo "$old_crontab" | sed -e '/\/cloud_config.pl /d'`
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Cloud Connector RPM could not deal with its nagios cron job" 1>&2
	    exit 1
	fi
	echo "$new_crontab" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Cloud Connector RPM could not remove its nagios cron job" 1>&2
	    exit 1
	fi
    fi

else

    # Upgrading.

    : do nothing

fi

# ================================================================

%postun
#!/bin/bash

PATH=/bin:/usr/bin

# FIX LATER:  The configuration file should be backed up somewhere before the
# entire package is deleted, since it may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.

# FIX LATER:
# We don't bother to test whether this is the last uninstall or an upgrade.
# Either way, we should kill all outstanding copies of the Cloud Connector
# software.

%include scripts/recreate_jbossportal
# We don't bother testing the exit status of that script here, because
# in a post-uninstall operation, there's really nothing downstream to
# block and no useful recovery action to take if we do find a failure.

# ================================================================

%files

%defattr(0644,nagios,nagios)

# The tree of directories containing this software.
%dir %attr(0755,nagios,nagios) %{cloudpath}
# %dir %attr(0755,nagios,nagios) %{cloudpath}/bin
%dir %attr(0755,nagios,nagios) %{cloudpath}/config
%dir %attr(0755,nagios,nagios) %{cloudpath}/credentials
%dir %attr(0755,nagios,nagios) %{cloudpath}/doc
%dir %attr(0755,nagios,nagios) %{cloudpath}/info
# %dir %attr(0755,nagios,nagios) %{cloudpath}/logs
%dir %attr(0755,nagios,nagios) %{cloudpath}/perl
%dir %attr(0755,nagios,nagios) %{cloudpath}/scripts
# %dir %attr(0755,nagios,nagios) %{cloudpath}/var

# %attr(0754,nagios,nagios) %{cloudpath}/bin/whatever

%attr(0444,nagios,nagios) %{cloudpath}/info/build_info

# If this file contained secret access credentials, we would protect it via 0600 permissions.
# But there's nothing especially critical in it, so we allow anyone to read it.
%config(noreplace) %attr(0644,nagios,nagios) %{cloudpath}/config/cloud_connector.conf

%dir %attr(0755,nagios,nagios) %{cloudpath}/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{cloudpath}/doc/%{name}-%{version}/CLOUD_INSTALL_NOTES
# FIX THIS:  create these files and enable their inclusion here
# %doc %attr(0444,nagios,nagios) %{cloudpath}/doc/%{name}-%{version}/CLOUD_RELEASE_NOTES
# %doc %attr(0444,nagios,nagios) %{cloudpath}/doc/%{name}-%{version}/groundwork-cloud-connector.0.0.pdf

# FIX THIS:  include everything Perl-related by some automated inclusion for the time being,
# rather than specifying the entire file trees rooted in these locations:
# %dir %attr(0755,nagios,nagios) %{cloudpath}/perl/bin
# %dir %attr(0755,nagios,nagios) %{cloudpath}/perl/lib
# %dir %attr(0755,nagios,nagios) %{cloudpath}/perl/man
# %dir %attr(0755,nagios,nagios) %{cloudpath}/perl/share

%attr(0754,nagios,nagios) %{cloudpath}/scripts/convert_eucarc_for_perl
%attr(0754,nagios,nagios) %{cloudpath}/scripts/convert_eucarc_for_tcsh
%attr(0754,nagios,nagios) %{cloudpath}/scripts/cloud_config.pl
%attr(0754,nagios,nagios) %{cloudpath}/scripts/setenv-cloud
%attr(0754,nagios,nagios) %{cloudpath}/scripts/setenv-cloud.bash
%attr(0754,nagios,nagios) %{cloudpath}/scripts/setenv-cloud.tcsh

%attr(0755,nagios,nagios) %{gwpath}/nagios/libexec/check_eucalyptus_availability_zone.pl

%attr(0444,nagios,nagios) %{gwpath}/core/profiles/host-profile-cloud-availability-zone.xml
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/host-profile-cloud-machine-default.xml
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/service-profile-cloud-availability-zone.xml
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/service-profile-ssh-hadoop.xml

%attr(0755,nagios,nagios) %{gwpath}/core/monarch/cgi-bin/monarch/monarch_clouds.cgi
%attr(0755,nagios,nagios) %{gwpath}/core/monarch/lib/MonarchClouds.pm

%attr(0444,nagios,nagios) %{gwpath}/foundation/container/webapps/jboss/jboss-portal.sar/gwos-cloud-connector-%{version}.war

# FIX THIS:  also provide these profiles:
#     host profile:     host-profile-eucalyptus-server
#     service profile:  eucalyptus-server
# for a Eucalyptus server and its components.

# FIX THIS:  provide this file and enable its inclusion here
# %attr(0644,root,root) /etc/logrotate.d/groundwork-cloud-connector

%include %(echo $PWD)/rpmsetup/%{name}-%{version}-%{_arch}.ec2_filelist
%include %(echo $PWD)/rpmsetup/%{name}-%{version}-%{_arch}.perl_filelist

# ================================================================

%changelog
* Wed Mar 24 2010 Glenn Herteg <support@groundworkopensource.com> 0.0.1
- initial, very-much-unclean version of the specfile
