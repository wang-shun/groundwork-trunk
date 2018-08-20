# This is the groundwork-nms-core spec file needed to construct the
# Groundwork Monitor NMS Core RPM.

# Copyright 2008 GroundWork Open Source, Inc. ("GroundWork").  All rights
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
%dump

# I'm not sure yet what this is for, except that it suppresses errors about
# not finding a 
# %define debug_package	%{nil}

# This setting allows us to redefine the dependency analyzer below, so we can
# filter out some "requires" dependencies on the GroundWork Monitor RPMs that
# are not yet properly available as "provides" capabilities in those RPMs.
# We appear to need this set to 0 to work around some rpm bugs, at least
# before rpm 4.4.4 on the target release platforms.  But setting this has an
# undesirable side effect.  It disable our ability to redefine __perl_requires
# below, which may mean we need to use --nodeps when installing this RPM.
%define _use_internal_dependency_generator	0

# ================================================================

%define	name		groundwork-nms-core
%define	major_release	2
%define	minor_release	1
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

# Here we override the standard RPM name defined in the gdma.rpmmacros file,
# because that construction references %%{ARCH} which refers to the machine
# architecture you're building on.  That's appropriate in most cases, because
# the target architecture generally reflects the build architecture.  But in
# our case, our current build process takes pre-built binaries for each target
# architecture and pulls them all together on a single machine on which all
# the RPMs are built, so the target architecture we want to reference in the
# RPM name is instead dealt with separately.  And therefore, we want to refer
# to %{arch} instead, as that macro will be defined individually for each
# target as it is built.

%define _rpmfilename   %%{NAME}-%%{VERSION}-%%{RELEASE}.%{arch}.rpm

%define	gwpath		/usr/local/groundwork

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor NMS Core Module
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(cat %(echo $PWD)/rpmsetup/project.properties | fgrep 'org.groundwork.rpm.release.core.number' | awk '{ print $3; }')
#Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')
# Source: %{name}-%{version}.tar.gz
Version: %{version}
#
# The groundwork-nms-core RPM will be built for the following architectures known
# as of this writing:  rh432, rh464, rh532, rh564, sles932, sles1032, sles1064
#
# The rest of the NMS RPMs will be built as "noarch" RPMs.  To do so,
# uncomment the following line in their respective specfiles.
# BuildArchitectures: noarch
#
Buildroot: %{_installroot}
Packager: GroundWork <support@groundworkopensource.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require at least one of these packages as a pre-requisite for the
# groundwork-nms-core RPM because we're adding certain new files and
# directories to the installation without creating the parent directories
# ourselves.  Also, this provides a primitive means of enforcing our license
# restriction that you need to have at least GroundWork Monitor Professional
# installed (Community Edition is not enough).
# FIX LATER:  We have not yet delved into exactly which of these possible
# external RPMs actually contain the pathnames, files, and databases we need.
# FIX LATER:  We might be able to relax the version constraints specified here.
AutoReq: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by supplying
compiled programs and supporting files needed to integrate certain
Network Management tools into the monitoring system.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)

# Here we filter out unwanted Requires, which are known to be provided in the
# base GroundWork Monitor packages which are part of our own package's PreReq, 
# but which are not yet part of the Provides declarations in those packages.
# So we're working around a deficiency, but because of the PreReq directive
# above we're not actually defeating the dependency analysis at install time.

# In the present case, we just need to handle certain Perl modules.

# The script below references the original __perl_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_perl_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_perl_requires}
#!/bin/sh
%{__perl_requires} $* |\
    sed -e '/perl(HTML::Tooltip::Javascript)/d'	\
	-e '/perl(Time::HiRes)/d' \
	-e '/perl(Mac::BuildTools)/d' \
	-e '/perl(Mac::InternetConfig)/d' \
	-e '/perl(MonarchAutoConfig)/d' \
	-e '/perl(MonarchStorProc)/d' \
	-e '/perl(.::inc/lib)/d' \
	-e '/perl(.::inc/libmisc.pl)/d' \
	-e '/perl(.::inc/libsnmp.pl)/d' \
	-e '/perl(Term::ReadKey)/d' \
	-e '/perl(TypedConfig)/d' \
	-e '/perl(Win32::ODBC)/d' \
	-e '/perl(Tk::LabRadio)/d' \
	-e '/perl(Tk::TextReindex)/d' \
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
# make nms_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre

if [ "$1" = 1 ]; then

    # First install.
	
	#
	#	if user doesn't exist, then add it.
	#

	# Create groups for nagios command
	if ! /usr/bin/id -g nagios &>/dev/null; then
		/bin/echo "Creating groups nagios..."
		/usr/sbin/groupadd nagios &>/dev/null
	fi
	
	if ! /usr/bin/id nagios &>/dev/null; then
        /usr/sbin/useradd -r -d %{prefix}/users/nagios -s /bin/bash -g "nagios" nagios || \
		%logmsg "Unexpected error adding user \"nagios\". Aborting installation."
	fi

	# IF REDHAT
	if [ -f /etc/redhat-release ] ; then
        /usr/sbin/usermod -G nagios nagios &>/dev/null
	fi

	# IF SuSE
	if [ -f /etc/SuSE-release ] ; then
        /usr/sbin/groupmod -A nagios nagios &>/dev/null
	fi
fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    : do nothing

fi

# ================================================================

%post

if [ "$1" = 1 ]; then

    # First install.

    # FIX MAJOR:  create whatever databases are needed on a first-time install,
    # if and only if those databases do not already exist

    : do nothing

fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX MAJOR:  we should probably kill any existing nms services,
    # so the new copy starts up instead

    : do nothing

fi

# ================================================================

%preun

if [ "$1" = 0 ]; then

	# Last uninstall.
	# Make sure the httpd service has been
	# shut down before removing the binaries.

	# All other services and cron entries
	# should have been removed by the RPMs with
	# dependencies upon cron (nedi, cacti, ntop,
	# weathermap), so httpd should be the only
	# thing that may still be running.

	# httpd
	if [ -e /etc/init.d/nms-httpd ]; then
		/etc/init.d/nms-httpd stop >/dev/null 2>&1
	fi

	# the only remaining item is the nagios user which
	# we may have created, but we will not delete that,
	# as there are too many possible dangers in doing
	# so.
fi

# ================================================================

%postun

# ================================================================

if [ "$1" = "0" ] ; then # last uninstall

	if ! ( rpm -qa | /bin/fgrep -l 'groundwork-monitor-core' &>/dev/null ) ; then
		/usr/sbin/userdel nagios || %logmsg "User \"nagios\" could not be deleted."
		/usr/sbin/groupdel nagios || %logmsg "Group \"nagios\" could not be deleted."
	fi
	
fi # End of last uninstall

%files -f %(echo $PWD)/rpmsetup/%{name}-%{version}-x86_64.filelist

%defattr(0644,nagios,nagios)

# The groundwork-nms-core RPM should include these file trees:
# %{gwpath}/nms/tools/...
# %{gwpath}/nms/enterprise/...

# ================================================================

%changelog
* Tue Jun 24 2008 Daniel E. Feinsmith <support@groundworkopensource.com> 2.0.1
- Created new revision from 2.0.0
- Changes for more platform compatibility.

* Wed Apr 16 2008 Glenn Herteg <support@groundworkopensource.com> 2.0.0
- first packaged as an RPM

