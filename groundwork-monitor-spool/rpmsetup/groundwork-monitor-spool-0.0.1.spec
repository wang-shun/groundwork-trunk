# This is the groundwork monitor-spool spec file needed to construct
# the Groundwork Monitor Spool RPM.

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

%define	name		groundwork-monitor-spool
%define	major_release	0
%define	minor_release	0
%define	patch_release	1
%define	version		%{major_release}.%{minor_release}.%{patch_release}

# We disable the dist definition by replacing % with # to comment out this next line, since this is a noarch RPM.
# (A define is recognized by rpmbuild even within a comment, if it is preceded by a percent sign.)
#define	dist		%(if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)

%define	gwpath		/usr/local/groundwork

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Spool Software
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')%{?dist}
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

# We require no RPM pre-requisites for the groundwork-monitor-spool RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by supplying
software to spool status results determined on a child server, which
are then sent to parent and/or parent-standby servers.  This improves
the reliability of parent-child setups.

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
	-e '/perl(Time::HiRes)/d'				\
	-e '/perl(GDMA::GDMAUtils)/d'				\
	-e '/perl(CollageQuery)/d'				\
	-e '/perl(MonarchLocks)/d'				\
	-e '/perl(TypedConfig)/d'
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
# make monitor_spool_install

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

    # Back up the existing copy of nagios2collage_socket.pl before we overwrite it.
    script_file=/usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl
    backup_file=/usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl.pre_spooler
    if [ -f $script_file -a ! -f $backup_file ]; then
	cp -p $script_file $backup_file
    fi

    # Back up the existing copy of gwservices before we overwrite it.
    script_file=/usr/local/groundwork/core/services/gwservices
    backup_file=/usr/local/groundwork/core/services/gwservices.pre_spooler
    if [ -f $script_file -a ! -f $backup_file ]; then
	cp -p $script_file $backup_file
    fi

    # echo "INFO:  This installation of %{name} is considered a first install;"
    # echo "       no attempt will be made to kill a previous copy of the spooler."

fi

if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  We ought to shut down the Monitor Spool Software if it's running,
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

    # Copy the just-installed copy of gwmon_localhost.cfg into a localized filename.

    # We use the same method as used by the spooler to determine the local hostname,
    # to guarantee we have a match whether the name is qualified or unqualified.
    thishost=`/usr/local/groundwork/perl/bin/perl -e 'use Sys::Hostname; print lc hostname();'`

    generic_file=/usr/local/groundwork/gdma/config/gwmon_localhost.cfg
    install_file=/usr/local/groundwork/gdma/config/gwmon_$thishost.cfg
    if [ -f $generic_file -a ! -f $install_file ]; then
	cp -p $generic_file $install_file
	chmod 644 $install_file
    fi

else

    # FIX LATER:  should we be testing this condition here?
    # FIX THIS:  How do we make a portable test here, to support Unbuntu as well?
    # if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    : do nothing

    # FIX LATER:  Shut down any old processes associated with the Monitor Spool software.
    # (But why would we do so only now?)

fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

# FIX MAJOR:  we should probably kill the Monitor Spool software here

# FIX LATER:  The configuration file should be backed up somewhere before the
# entire package is deleted, since it may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.

# Stop the Monitor Spool.
# FIX THIS:  fill in here as needed

if [ "$1" = 0 -o "$1" = "remove" ]; then

    # Last uninstall.

    : do nothing

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
# Either way, we should kill all outstanding copies of the Monitor Spool
# software.

# Under Ubuntu, don't run this if "$1" = "upgrade".
# Under RHEL, don't run this if "$1" = "1" (also an upgrade situation).
#
# Also note that Ubuntu (dpkg) runs the un-install script before the upgrade script,
# while RHEL (rpm) runs the upgrade script before the un-install script, for whatever
# difference that might make to our construction here.
#
# 'purge' is only specified when Ubuntu dpkg wants to delete only any remaining
# config and log files, after a previous 'remove' operation.  Such purging is not
# the point at which we want to restore the script from backup.  In contrast,
# 'remove' is specified when Ubuntu dpkg wants to delete the rest of the package,
# and that's when we should be be restore the script from backup.
# 
if [ "$1" = 0 -o "$1" = "remove" -o "$1" = "abort-remove" ]; then
    # Restore the previous copy of nagios2collage_socket.pl if it seems to make sense.
    script_file=/usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl
    backup_file=/usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl.pre_spooler
    if [ ! -f $script_file -a -f $backup_file ]; then
	cp -p $backup_file $script_file
    fi
fi

# We don't bother uninstalling the upgraded copy of the gwservices script that we
# installed as part of this RPM, because it contains bug fixes in addition to the
# new capability of optionally supporting the GDMA spooler.

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

# The directories containing this software.
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma/bin
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma/config
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma/log
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma/spool
%dir %attr(0755,nagios,nagios) %{gwpath}/gdma/tmp
%dir %attr(0755,nagios,nagios) %{gwpath}/perl/lib/site_perl/5.8.8/GDMA

%dir %attr(0755,nagios,nagios) %{gwpath}/core/services/spooler-gdma
%dir %attr(0755,nagios,nagios) %{gwpath}/core/services/spooler-gdma/log
%dir %attr(0755,nagios,nagios) %{gwpath}/core/services/spooler-gdma/log/main
%dir %attr(0700,root,root)     %{gwpath}/core/services/spooler-gdma/log/supervise
%dir %attr(0700,root,root)     %{gwpath}/core/services/spooler-gdma/supervise

# %attr(0444,nagios,nagios) %{gwpath}/info/build_info

# If this file contained secret access credentials, we would protect it via 0600 permissions.
# But there's nothing especially critical in it, so we allow anyone to read it.
%config(noreplace) %attr(0644,nagios,nagios) %{gwpath}/config/status-feeder.properties

%dir %attr(0755,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/SPOOL_INSTALL_NOTES
# FIX THIS:  create these files and enable their inclusion here
# %doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/SPOOL_RELEASE_NOTES
# %doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/groundwork-monitor-spool.1.0.pdf

%attr(0755,nagios,nagios) %{gwpath}/foundation/feeder/nagios2collage_socket.pl

%attr(0755,nagios,nagios) %{gwpath}/gdma/bin/gdma_spool_processor.pl
%attr(0444,nagios,nagios) %{gwpath}/gdma/config/gdma_auto.conf
%attr(0444,nagios,nagios) %{gwpath}/gdma/config/gwmon_localhost.cfg
%attr(0444,nagios,nagios) %{gwpath}/perl/lib/site_perl/5.8.8/GDMA/GDMAUtils.pm

%attr(0755,nagios,nagios) %{gwpath}/core/services/gwservices

%attr(0644,nagios,nagios) %{gwpath}/core/services/spooler-gdma/log/main/log
%attr(0755,nagios,nagios) %{gwpath}/core/services/spooler-gdma/log/run
%attr(0600,root,root)     %{gwpath}/core/services/spooler-gdma/log/supervise/lock
%attr(0755,nagios,nagios) %{gwpath}/core/services/spooler-gdma/run
%attr(0600,root,root)     %{gwpath}/core/services/spooler-gdma/supervise/lock

# These two files are actually named pipes.
%attr(0600,root,root) %{gwpath}/core/services/spooler-gdma/log/supervise/control
%attr(0600,root,root) %{gwpath}/core/services/spooler-gdma/supervise/control

# FIX THIS:  possibly, provide this file and enable its inclusion here
# (although we might instead just rotate the log file within the scripting)
# %attr(0644,root,root) /etc/logrotate.d/groundwork-monitor-spool

# ================================================================

%changelog
* Mon Oct 25 2010 Glenn Herteg <support@groundworkopensource.com> 0.0.1
- drop the perl(Time::HiRes) dependency from the RPM, as it will be
  provided by the GroundWork Perl without declaration

* Mon Oct 18 2010 Glenn Herteg <support@groundworkopensource.com> 0.0.1
- added a replacement gwservices script, to support start/stop of the
  included GDMA spooler (as well as fix certain race conditions)

* Tue Oct 12 2010 Glenn Herteg <support@groundworkopensource.com> 0.0.1
- initial release
