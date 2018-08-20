# This is the groundwork-nms-cacti spec file needed to construct the
# Groundwork Monitor NMS Cacti RPM.

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
# @dump

# I'm not sure yet what this is for, except that it suppresses errors about
# not finding a 
# %define debug_package	%{nil}

# ================================================================

%define	name		groundwork-nms-cacti
%define	major_release	2
%define	minor_release	1
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

%define	gwpath		/usr/local/groundwork

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor NMS Cacti Module
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(cat %(echo $PWD)/rpmsetup/project.properties | fgrep 'org.groundwork.rpm.release.cacti.number' | awk '{ print $3; }')
#Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')
# Source: %{name}-%{version}.tar.gz
Version: %{version}
BuildArchitectures: noarch
Buildroot: %{_installroot}
Packager: GroundWork <support@groundworkopensource.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require at least one of these packages as a pre-requisite for the
# groundwork-nms-cacti RPM because we're adding certain new files and
# directories to the installation without creating the parent directories
# ourselves.  Also, this provides a primitive means of enforcing our license
# restriction that you need to have at least GroundWork Monitor Professional
# installed (Community Edition is not enough).
# FIX LATER:  We have not yet delved into exactly which of these possible
# external RPMs actually contain the pathnames, files, and databases we need.
# FIX LATER:  We might be able to relax the version constraints specified here.
PreReq: groundwork-nms-core >= 2.1.0

# ================================================================

%description
This software extends the base GroundWork Monitor product by supplying
a version of Cacti which is integrated into the monitoring system.

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
# make nms_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre

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
	# Remove cron entry for Cacti poller.

    crontab -u nagios -l | grep -v "poller.php" >/tmp/clean_cacti.tmp 2>/dev/null;
    crontab -u nagios /tmp/clean_cacti.tmp;
fi

# ================================================================

%postun

# ================================================================

%files -f %(echo $PWD)/rpmsetup/%{name}-%{version}.filelist

%defattr(0644,nagios,nagios)

# The groundwork-nms-cacti RPM should depend on the groundwork-nms-core RPM and include this file tree:
# %{gwpath}/applications/cacti/...

# ================================================================

%changelog
* Tue Jun 24 2008 Daniel E. Feinsmith <support@groundworkopensource.com> 2.0.1
- Created new revision with no changes from 2.0.0

* Wed Apr 16 2008 Glenn Herteg <support@groundworkopensource.com> 2.0.0
- first packaged as an RPM

