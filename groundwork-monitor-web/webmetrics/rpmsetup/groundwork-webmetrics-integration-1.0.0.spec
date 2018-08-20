# This is the groundwork-webmetrics-integration spec file needed to construct
# the Groundwork Monitor Webmetrics Integration RPM.

# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
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
# (*) ...

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

%define	name		groundwork-webmetrics-integration
%define	major_release	1
%define	minor_release	0
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

# We would disable the dist definition by replacing % with # to comment out this next line, if this were a noarch RPM.
# (A define is recognized by rpmbuild even within a comment, if it is preceded by a percent sign.)
%define	dist		%(if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)

%define	gwpath		/usr/local/groundwork
%define	webmetricspath	%{gwpath}/webmetrics

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Webmetrics Integration Software
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

# We require no RPM pre-requisites for the groundwork-webmetrics-integration RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying software to integrate with Webmetrics web monitoring.

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
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/.perl.bin/d'	\
	-e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'	\
	-e '/perl(DBI)/d'					\
	-e '/perl(DateTimePPExtra)/d'				\
	-e '/perl(File::FcntlLock)/d'				\
	-e '/perl(HTTP::Request::Common)/d'			\
	-e '/perl(LWP::UserAgent)/d'				\
	-e '/perl(List::MoreUtils)/d'				\
	-e '/perl(MonarchLocks)/d'				\
	-e '/perl(MonarchStorProc)/d'				\
	-e '/perl(Params::Validate)/d'				\
	-e '/perl(RRDs)/d'					\
	-e '/perl(TypedConfig)/d'				\
	-e '/perl(URI::URL)/d'					\
	-e '/perl(Win32::TieRegistry)/d'			\
	-e '/perl(XML::Simple)/d'				\
	-e '/perl(dassmonarch)/d'
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
# make webmetrics_integration_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre
#!/bin/bash -e
# The bash -e option is needed so we intentionally fail if we have
# difficulty creating the temporary database credentials file.

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
    # echo "       no attempt will be made to kill a previous Webmetrics Integration."

fi

if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # We shut down the Webmetrics Integration daemons if they are running, so the upgrade
    # can go smoothly.
    # FIX LATER:  This is something of a half-hearted attempt, since the cron job from
    # the previous install might start them up again before our upgrade is complete.
    %{webmetricspath}/bin/control_webmetrics_integration stop

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

    # Everything we want to do here is fairly independent of the conditions we test for above,
    # and we want to ensure that this scripting attempts to repair any damage it can during an
    # upgrade, so all the system changes here are run idempotently outside of those tests.
    : do nothing

else

    # FIX LATER:  should we be testing this condition here?
    # FIX THIS:  How do we make a portable test here, to support Unbuntu as well?
    # if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # Shut down any old daemons associated with the Webmetrics Integration software,
    # in case they got restarted before our upgrade was complete.  The daemons will
    # be restarted shortly using the newly installed software, via cron job (below).
    %{webmetricspath}/bin/control_webmetrics_integration stop

fi

# Make sure the Webmetrics Integration nagios cron job is in place.  We carry out this action
# in small steps to minimize the likelihood of leaving the system in a mangled state,
# having corrupted the existing content and not having installed the new content.
old_crontab=`crontab -l -u nagios`
if [ $? -ne 0 ]; then
    echo "FATAL:  Webmetrics Integration RPM could not fetch the old nagios crontab" 1>&2
    exit 1
fi
if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/control_webmetrics_integration '`" -eq 0 ]; then
    # The cron job is not installed.  Do so now.  We don't bother to try to delete
    # an existing cron job if it is currently commented out; we just add a new one.
    new_cron_job='*/5 * * * * %{webmetricspath}/bin/control_webmetrics_integration start > /dev/null 2>&1'
    echo "$old_crontab$newline$new_cron_job" | crontab -u nagios -
    if [ $? -ne 0 ]; then
	echo "FATAL:  Webmetrics Integration RPM could not install its nagios cron job" 1>&2
	exit 1
    fi
else
    echo "NOTICE:  The Webmetrics Integration nagios cron job looks like it was already in place."
fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

# FIX LATER:  The configuration file should be backed up somewhere before the
# entire package is deleted, since it may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.  (Actually,
# I believe rpm will not delete a locally modified copy of any %%config(noreplace)
# file, when the package is removed, instead renaming it with a .rpmsave extension.)

if [ "$1" = 0 -o "$1" = "remove" ]; then

    # Last uninstall.

    # Make sure the Webmetrics Integration nagios cron job is removed.  We carry out this action
    # in small steps to minimize the likelihood of leaving the system in a mangled state,
    # having corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  Webmetrics Integration RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/control_webmetrics_integration '`" -eq 0 ]; then
	# The cron job is not installed.  We don't bother to try to delete
	# an existing cron job if it is currently commented out.
	echo "NOTICE:  The Webmetrics Integration nagios cron job looks like it was already commented out or removed."
    else
	# new_cron_job='*/5 * * * * %{webmetricspath}/bin/control_webmetrics_integration start > /dev/null 2>&1'
	new_crontab=`echo "$old_crontab" | sed -e '/\/control_webmetrics_integration /d'`
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Webmetrics Integration RPM could not deal with its nagios cron job" 1>&2
	    exit 1
	fi
	echo "$new_crontab" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Webmetrics Integration RPM could not remove its nagios cron job" 1>&2
	    exit 1
	fi
    fi

else

    # Upgrading.

    : do nothing

fi

# Stop the Webmetrics Integration daemons.  We do this only after the cron job has been uninstalled,
# to make sure it didn't get started by the cron job after we thought we had stopped it ourselves.
# If this is an upgrade and we didn't actually remove the cron job, this will stop the previous
# daemons, and the cron job will start new daemons using the newly-installed scripting (provided
# the pre-uninstall action of the previous version occurs after the install action of the new
# version of this package).
%{webmetricspath}/bin/control_webmetrics_integration stop

# ================================================================

%postun
#!/bin/bash

PATH=/bin:/usr/bin

# FIX LATER:  The configuration file should be backed up somewhere before the
# entire package is deleted, since it may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.

# We don't bother to test whether this is the last uninstall or an upgrade.
# Either way, all outstanding copies of the Webmetrics Integration daemons
# should have already been killed during pre-uninstall actions.

# Under Ubuntu, don't run this if "$1" = "upgrade".
# Under RHEL, don't run this if "$1" = "1" (also an upgrade situation).
#
# Also note that Ubuntu (dpkg) runs the un-install script before the upgrade script,
# while RHEL (rpm) runs the upgrade script before the un-install script, for whatever
# difference that might make to our construction here.
#
# 'purge' is only specified when Ubuntu dpkg wants to delete only any remaining
# config and log files, after a previous 'remove' operation.  In contrast,
# 'remove' is specified when Ubuntu dpkg wants to delete the rest of the package.
#
if [ "$1" = 0 -o "$1" = "remove" -o "$1" = "abort-remove" ]; then
    : do nothing
fi
# We don't bother testing the exit status of that script here, because in a true
# post-uninstall operation (where we really are removing the last copy of the
# package, with no intent to install a replacement copy), there's really nothing
# downstream to block and no useful recovery action to take if we do find a failure.
# We might want to revisit that decision in the Ubuntu environment, if a (partial?)
# failure of the uninstall of the previous package perhaps ought to be somehow
# reflected in the system's notion of the package state.

# ================================================================

%files

%defattr(0644,nagios,nagios)

# The tree of directories containing this software.
%dir %attr(0755,nagios,nagios) %{webmetricspath}
%dir %attr(0755,nagios,nagios) %{webmetricspath}/bin
%dir %attr(0755,nagios,nagios) %{webmetricspath}/config
%dir %attr(0755,nagios,nagios) %{webmetricspath}/doc
%dir %attr(0755,nagios,nagios) %{webmetricspath}/info
%dir %attr(0755,nagios,nagios) %{webmetricspath}/logs
%dir %attr(0755,nagios,nagios) %{webmetricspath}/perl

%attr(0444,nagios,nagios) %{webmetricspath}/info/build_info

# If a config file contains secret access credentials, we protect it via 0600 permissions.
# But if there's nothing especially critical in it, we allow anyone to read it.
%config(noreplace) %attr(0600,nagios,nagios) %{webmetricspath}/config/query_webmetrics.conf

%dir %attr(0755,nagios,nagios) %{webmetricspath}/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{webmetricspath}/doc/%{name}-%{version}/WEBMETRICS_INTEGRATION_INSTALL_NOTES
# FIX THIS:  create these files and enable their inclusion here
# %doc %attr(0444,nagios,nagios) %{webmetricspath}/doc/%{name}-%{version}/WEBMETRICS_INTEGRATION_RELEASE_NOTES
# %doc %attr(0444,nagios,nagios) %{webmetricspath}/doc/%{name}-%{version}/groundwork-webmetrics-integration.1.0.pdf

%attr(0754,nagios,nagios) %{webmetricspath}/bin/control_webmetrics_integration
%attr(0754,nagios,nagios) %{webmetricspath}/bin/query_webmetrics.pl
%attr(0755,nagios,nagios) %{webmetricspath}/bin/webmetrics_host

%attr(0444,nagios,nagios) %{gwpath}/core/profiles/host-profile-webmetrics-resource.xml
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/service-profile-webmetrics-daemons.xml
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/service-profile-webmetrics-probes.xml

# In similar applications, we might provide a file like this and enable its
# inclusion here.  In the present integration, we just rotate the respective
# log files within the query_webmetrics.pl script.
# %attr(0644,root,root) /etc/logrotate.d/groundwork-webmetrics-integration

# We include everything Perl-related by an automated inclusion based
# on an externally-generated and infrequently-updated file list, rather
# than specifying here the entire file trees rooted in these locations:
# %dir %attr(0755,nagios,nagios) %{webmetricspath}/perl/bin
# %dir %attr(0755,nagios,nagios) %{webmetricspath}/perl/lib
# %dir %attr(0755,nagios,nagios) %{webmetricspath}/perl/man
# %dir %attr(0755,nagios,nagios) %{webmetricspath}/perl/share

%include %(echo $PWD)/rpmsetup/%{name}-%{version}-%{_arch}.perl_filelist

# ================================================================

%changelog
* Mon Jul 18 2011 Glenn Herteg <support@groundworkopensource.com> 1.0.0
- initial RPM construction
