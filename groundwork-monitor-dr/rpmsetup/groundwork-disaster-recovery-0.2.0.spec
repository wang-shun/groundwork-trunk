# This is the groundwork disaster-recovery spec file needed to construct
# the Groundwork Monitor Disaster Recovery RPM.

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
#     upgrades, and uninstalls of the Disaster Recovery software

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

%define	name		groundwork-disaster-recovery
%define	major_release	0
%define	minor_release	2
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}
%define	dist		%(if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)

%define	gwpath		/usr/local/groundwork
%define	reppath		%{gwpath}/replication

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Disaster Recovery Software
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')%{?dist}
# Source: %{name}-%{version}.tar.gz
Version: %{version}
Buildroot: %{_installroot}
Packager: GroundWork <support@gwos.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require no RPM pre-requisites for the groundwork-disaster-recovery RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying software to manage a Disaster Recovery capability.

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
	-e '/perl(TypedConfig)/d'				\
	-e '/perl(MonarchLocks)/d'				\
	-e '/perl(MonarchStorProc)/d'				\
	-e '/libmysqlclient.so.15/d'				\
	-e '/perl(Win32::ODBC)/d'
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
    sed -e '/perl(CollageQuery)/d'	\
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
# make disaster_recovery_install

# ================================================================

%clean

# make buildclean

# ================================================================

# Just as important as understanding the arguments to the pre/post install/uninsall scripts
# is understanding exactly when they will each be called, and with what arguments in those
# situations.  The following is the result of our testing on CentOS 5.3; additional testing
# would need to be done to check the arguments and sequencing for Ubuntu.
#
# fresh install:
#     pre-install 1
#     (install)
#     post_install 1
#
# upgrade:
#     pre-install 2
#     (upgrade)
#     post-install 2
#     pre-uninstall 1
#     (presumably, uninstall any leftover components from the original RPM?)
#     post-uninstall 1
#
# erase:
#     pre-uninstall 0
#     (uninstall)
#     post-uninstall 0
#
# Note in particular that during an upgrade, the install/uninstall invocations are not nested,
# and the uninstall of the first package comes *after* the install of the upgraded package.
# This matters when deciding what actions should be taken in each script.

%pre
#!/bin/bash
# echo "pre-install arguments:  $*"

PATH=/bin:/usr/bin

# FIX MINOR:  accommodate Debian keywords here for install and upgrade actions

if [ "$1" = 1 ]; then

    # First install.

    # Add the REPLICATION ApplicationType we need for log messages sent to Foundation
    # by the Disaster Recovery Replication Engine.  If that fails, there's no sense in
    # going further.
%include scripts/add_application_type
    if [ $? -ne 0 ]; then
	echo "FATAL:  Disaster Recovery RPM could not update the GWCollage database" 1>&2
	exit 1
    fi

    # echo "INFO:  This installation of %{name} is considered a first install;"
    # echo "       no attempt will be made to kill a previous Replication Engine."

fi

if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then 

    # Upgrading.

    # Adding the REPLICATION ApplicationType should not be necessary here,
    # but it will ensure that we end up with a working system if we are
    # upgrading from an early development RPM that did not include it.
    #
    # Add the REPLICATION ApplicationType we need for log messages sent to Foundation
    # by the Disaster Recovery Replication Engine.  If that fails, there's no sense in
    # going further.
%include scripts/add_application_type
    if [ $? -ne 0 ]; then
	echo "FATAL:  Disaster Recovery RPM could not update the GWCollage database" 1>&2
	exit 1
    fi

    # FIX LATER:  We ought to shut down the Disaster Recovery Software if it's running,
    # so the upgrade can go smoothly.  Make sure we do that in a way that won't cause
    # it to be started up again immediately by some daemon-keeper process, and that
    # will be easy to reverse later on.  Perhaps the simplest way is to call the
    # recover client and issue commands to stop all sync operations.

    # Make sure the Disaster Recovery nagios cron job is removed so it does not interfere
    # with our upgrade actions while they are ongoing.  We carry out this action in small
    # steps to minimize the likelihood of leaving the system in a mangled state, having
    # corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  Disaster Recovery RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/control_replication_engine '`" -eq 0 ]; then
	# The cron job is not installed.  We don't bother to try to delete
	# an existing cron job if it is currently commented out.
	echo "NOTICE:  The Disaster Recovery nagios cron job looks like it was already commented out or removed."
    else
	# new_cron_job='*/5 * * * * /usr/local/groundwork/replication/bin/control_replication_engine start'
	new_crontab=`echo "$old_crontab" | sed -e '/\/control_replication_engine /d'`
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Disaster Recovery RPM could not deal with its nagios cron job" 1>&2
	    exit 1
	fi
	echo "$new_crontab" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Disaster Recovery RPM could not remove its nagios cron job" 1>&2
	    exit 1
	fi
    fi

    # FIX LATER:  The following formulation of stopping the Replication Engine
    # will need to change when we change the Perl it uses from /usr/bin/perl to
    # /usr/local/groundwork/perl/bin/perl as part of the Bitrock packaging.

    # FIX LATER:  Perhaps we should just call "control_replication_engine stop" instead.

    # Stop the Disaster Recovery Replication Engine.
    pids=`ps -C replication_state_engine --no-headers -o pid`
    if [ -n "$pids" ]; then 
	echo "Upgrading; first shutting down the existing Replication Engine ..."
	kill -TERM $pids
    fi

fi

# ================================================================

%post
#!/bin/bash
# echo "post-install arguments:  $*"

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

newline='
'

if [ "$1" = 1 -o "$1" = "configure" -o "$1" = "abort-remove" ]; then 

    # First install, or aborted remove (under Unbuntu).

    : do nothing

else

    # FIX LATER:  should we be testing this condition here?
    # FIX THIS:  How do we make a portable test here, to support Unbuntu as well?
    # if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then 

    # Upgrading.

    : do nothing

fi

# Make sure the Disaster Recovery nagios cron job is in place.  We carry out this action
# in small steps to minimize the likelihood of leaving the system in a mangled state,
# having corrupted the existing content and not having installed the new content.

old_crontab=`crontab -l -u nagios` 
if [ $? -ne 0 ]; then 
    echo "FATAL:  Disaster Recovery RPM could not fetch the old nagios crontab" 1>&2
    exit 1  
fi
if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/control_replication_engine '`" -eq 0 ]; then 
    # The cron job is not installed.  Do so now.  We don't bother to try to delete
    # an existing cron job if it is currently commented out; we just add a new one.
    new_cron_job='*/5 * * * * /usr/local/groundwork/replication/bin/control_replication_engine start'
    echo "$old_crontab$newline$new_cron_job" | crontab -u nagios -
    if [ $? -ne 0 ]; then 
	echo "FATAL:  Disaster Recovery RPM could not install its nagios cron job" 1>&2
	exit 1  
    fi      
else
    echo "NOTICE:  The Disaster Recovery nagios cron job looks like it was already in place." 
fi

# ================================================================

%preun
#!/bin/bash
# echo "pre-uninstall arguments:  $*"

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

if [ "$1" = 0 -o "$1" = "remove" ]; then

    # Last uninstall.

    # FIX LATER:  The configuration file should be backed up somewhere before the
    # entire package is deleted, since it may represent valuable local configuration
    # data you might want to preserve for a later re-addition of the package.

    # Make sure the Disaster Recovery nagios cron job is removed.  We carry out this action
    # in small steps to minimize the likelihood of leaving the system in a mangled state,
    # having corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  Disaster Recovery RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/control_replication_engine '`" -eq 0 ]; then
	# The cron job is not installed.  We don't bother to try to delete
	# an existing cron job if it is currently commented out.
	echo "NOTICE:  The Disaster Recovery nagios cron job looks like it was already commented out or removed."
    else
	# new_cron_job='*/5 * * * * /usr/local/groundwork/replication/bin/control_replication_engine start'
	new_crontab=`echo "$old_crontab" | sed -e '/\/control_replication_engine /d'`
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Disaster Recovery RPM could not deal with its nagios cron job" 1>&2
	    exit 1
	fi
	echo "$new_crontab" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  Disaster Recovery RPM could not remove its nagios cron job" 1>&2
	    exit 1
	fi
    fi

    # FIX LATER:  The following formulation of stopping the Replication Engine
    # will need to change when we change the Perl it uses from /usr/bin/perl to
    # /usr/local/groundwork/perl/bin/perl as part of the Bitrock packaging.

    # FIX LATER:  Perhaps we should just call "control_replication_engine stop" instead.

    # Stop the Disaster Recovery Replication Engine.
    pids=`ps -C replication_state_engine --no-headers -o pid`
    if [ -n "$pids" ]; then 
	echo "Uninstalling; shutting down the existing Replication Engine ..."
	kill -TERM $pids
    fi

else

    # Upgrading.

    # We intentionally leave in place the cron job from the install.

    : do nothing

fi

# ================================================================

%postun
#!/bin/bash
# echo "post-uninstall arguments:  $*"

PATH=/bin:/usr/bin

# FIX LATER:
# We don't bother to test whether this is the last uninstall or an upgrade.

# ================================================================

%files

%defattr(0644,nagios,nagios)

# The tree of directories containing this software.
%dir %attr(0755,nagios,nagios) %{reppath}
%dir %attr(0755,nagios,nagios) %{reppath}/backups
%dir %attr(0755,nagios,nagios) %{reppath}/bin
%dir %attr(0755,nagios,nagios) %{reppath}/config
%dir %attr(0755,nagios,nagios) %{reppath}/doc
%dir %attr(0755,nagios,nagios) %{reppath}/info
%dir %attr(0755,nagios,nagios) %{reppath}/logs
%dir %attr(0755,nagios,nagios) %{reppath}/pending
%dir %attr(0755,nagios,nagios) %{reppath}/perl
%dir %attr(0755,nagios,nagios) %{reppath}/scripts
%dir %attr(0755,nagios,nagios) %{reppath}/var

%attr(0754,nagios,nagios) %{reppath}/bin/control_replication_engine
%attr(0754,nagios,nagios) %{reppath}/bin/recover
%attr(0754,nagios,nagios) %{reppath}/bin/replication_state_engine

%attr(0754,nagios,nagios) %{reppath}/scripts/erase_readies_obj
%attr(0754,nagios,nagios) %{reppath}/scripts/generic_app.capture
%attr(0754,nagios,nagios) %{reppath}/scripts/generic_app.deploy
%attr(0754,nagios,nagios) %{reppath}/scripts/generic_db.capture
%attr(0754,nagios,nagios) %{reppath}/scripts/generic_db.deploy
%attr(0754,nagios,nagios) %{reppath}/scripts/make_backup_obj
%attr(0754,nagios,nagios) %{reppath}/scripts/make_ready_obj
%attr(0754,nagios,nagios) %{reppath}/scripts/make_replica_app
%attr(0754,nagios,nagios) %{reppath}/scripts/make_replica_db
%attr(0754,nagios,nagios) %{reppath}/scripts/make_staged_app
%attr(0754,nagios,nagios) %{reppath}/scripts/make_staged_db
%attr(0754,nagios,nagios) %{reppath}/scripts/make_working_app
%attr(0754,nagios,nagios) %{reppath}/scripts/make_working_db
%attr(0754,nagios,nagios) %{reppath}/scripts/monarch_preflight_and_commit
%attr(0754,nagios,nagios) %{reppath}/scripts/prune_backups_obj
%attr(0754,nagios,nagios) %{reppath}/scripts/selective_copy

# These symlinks need special treatment.
#                         %{reppath}/scripts/make_shadow_app -> make_staged_app
%attr(0777,nagios,nagios) %{reppath}/scripts/make_shadow_app
#                         %{reppath}/scripts/make_shadow_db -> make_staged_db
%attr(0777,nagios,nagios) %{reppath}/scripts/make_shadow_db

%attr(0444,nagios,nagios) %{reppath}/info/build_info

# This configuration file does not itself contain any sensitive information;
# it only points to other files that contain such data.  So there is no reason
# to force this file to only be readable by the owner.
%config(noreplace) %attr(0644,nagios,nagios) %{reppath}/config/replication.conf

# I'm not sure why we're disallowing search access to the world for these directories.
# Is that just some historical mistake?
%dir %attr(0754,nagios,nagios) %{reppath}/actions
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/cacti
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/foundation
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/monarch
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/nagios
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/nedi
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/ntop
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/snmp-trap-handling
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/syslog-ng
%dir %attr(0754,nagios,nagios) %{reppath}/actions/app/weathermap
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db/GWCollageDB
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db/cacti
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db/jbossportal
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db/monarch
%dir %attr(0754,nagios,nagios) %{reppath}/actions/db/nedi

%attr(0754,nagios,nagios) %{reppath}/actions/app/cacti/cacti.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/cacti/cacti.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/cacti/cacti.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/cacti/cacti.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/foundation/foundation.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/foundation/foundation.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/foundation/foundation.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/foundation/foundation.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/monarch/monarch.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/monarch/monarch.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/monarch/monarch.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/monarch/monarch.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/nagios/nagios.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/nagios/nagios.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/nagios/nagios.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/nagios/nagios.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/nedi/nedi.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/nedi/nedi.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/nedi/nedi.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/nedi/nedi.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/ntop/ntop.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/ntop/ntop.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/ntop/ntop.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/ntop/ntop.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/snmp-trap-handling/snmp-trap-handling.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/snmp-trap-handling/snmp-trap-handling.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/snmp-trap-handling/snmp-trap-handling.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/snmp-trap-handling/snmp-trap-handling.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/syslog-ng/syslog-ng.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/syslog-ng/syslog-ng.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/syslog-ng/syslog-ng.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/syslog-ng/syslog-ng.stop
%attr(0754,nagios,nagios) %{reppath}/actions/app/weathermap/weathermap.capture
%attr(0754,nagios,nagios) %{reppath}/actions/app/weathermap/weathermap.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/app/weathermap/weathermap.start
%attr(0754,nagios,nagios) %{reppath}/actions/app/weathermap/weathermap.stop
%attr(0754,nagios,nagios) %{reppath}/actions/db/GWCollageDB/GWCollageDB.capture
%attr(0754,nagios,nagios) %{reppath}/actions/db/GWCollageDB/GWCollageDB.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/db/GWCollageDB/GWCollageDB.start
%attr(0754,nagios,nagios) %{reppath}/actions/db/GWCollageDB/GWCollageDB.stop
%attr(0754,nagios,nagios) %{reppath}/actions/db/cacti/cacti.capture
%attr(0754,nagios,nagios) %{reppath}/actions/db/cacti/cacti.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/db/cacti/cacti.start
%attr(0754,nagios,nagios) %{reppath}/actions/db/cacti/cacti.stop
%attr(0754,nagios,nagios) %{reppath}/actions/db/jbossportal/jbossportal.capture
%attr(0754,nagios,nagios) %{reppath}/actions/db/jbossportal/jbossportal.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/db/jbossportal/jbossportal.start
%attr(0754,nagios,nagios) %{reppath}/actions/db/jbossportal/jbossportal.stop
%attr(0754,nagios,nagios) %{reppath}/actions/db/monarch/monarch.capture
%attr(0754,nagios,nagios) %{reppath}/actions/db/monarch/monarch.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/db/monarch/monarch.start
%attr(0754,nagios,nagios) %{reppath}/actions/db/monarch/monarch.stop
%attr(0754,nagios,nagios) %{reppath}/actions/db/nedi/nedi.capture
%attr(0754,nagios,nagios) %{reppath}/actions/db/nedi/nedi.deploy
%attr(0754,nagios,nagios) %{reppath}/actions/db/nedi/nedi.start
%attr(0754,nagios,nagios) %{reppath}/actions/db/nedi/nedi.stop

%dir %attr(0755,nagios,nagios) %{reppath}/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{reppath}/doc/%{name}-%{version}/INSTALLATION
%doc %attr(0444,nagios,nagios) %{reppath}/doc/%{name}-%{version}/OPERATION
# FIX THIS:  create these files and enable their inclusion here
# %doc %attr(0444,nagios,nagios) %{reppath}/doc/%{name}-%{version}/RECOVERY_RELEASE_NOTES
# %doc %attr(0444,nagios,nagios) %{reppath}/doc/%{name}-%{version}/groundwork-disaster-recovery.0.0.pdf

# FIX THIS:  include everything Perl-related by some automated inclusion for the time being,
# rather than specifying the entire file trees rooted in these locations:
# %dir %attr(0755,nagios,nagios) %{reppath}/perl/bin
# %dir %attr(0755,nagios,nagios) %{reppath}/perl/lib
# %dir %attr(0755,nagios,nagios) %{reppath}/perl/man
# %dir %attr(0755,nagios,nagios) %{reppath}/perl/share

%attr(0755,nagios,nagios) %{gwpath}/nagios/libexec/check_replication
%attr(0444,nagios,nagios) %{gwpath}/core/profiles/service-profile-disaster-recovery.xml

# Perhaps someday, provide this file and enable its inclusion here, if we ever need anything
# more extensive than the logfile rotation which is already run by the Replication Engine.
# %attr(0644,root,root) /etc/logrotate.d/groundwork-disaster-recovery

%include %(echo $PWD)/rpmsetup/%{name}-%{version}-%{_arch}.perl_filelist

# ================================================================

%changelog
* Mon Aug 16 2010 Glenn Herteg <support@groundworkopensource.com> 0.2.0
- cleaned up and corrected replication client/server reconnection logic
- added deadlines to remote requests, so stale requests can be ignored
- fixed POSIX::setpgid() error detection
- added FERVID logging level; changed some log messages to use it
- updated replication.conf comments

* Fri May 28 2010 Glenn Herteg <support@groundworkopensource.com> 0.1.0
- small edits to documentation
- added a CLI commit script
- call the commit script from within a monarch database deploy action
- updated replication obtain/capture timeouts to account for a slow wire
- /usr/local/groundwork/core/monarch/backup/ is not replicated
- action script invocations are now logged with their command-line parameters

* Wed May 26 2010 Glenn Herteg <support@groundworkopensource.com> 0.1.0
- First Customer Ship of the DR software package

* Tue Mar  2 2010 Glenn Herteg <support@groundworkopensource.com> 0.0.1
- initial, very-much-unclean version of the specfile
