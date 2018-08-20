# This is the groundwork-perl-typedconfig spec file needed to construct the
# Groundwork Monitor Perl TypedConfig Package RPM.

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
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

%define	name		groundwork-perl-typedconfig
%define	major_release	1
%define	minor_release	0
%define	patch_release	1
%define	version		%{major_release}.%{minor_release}.%{patch_release}

%define	gwpath		/usr/local/groundwork

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor Perl TypedConfig Package
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')
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
# groundwork-perl-typedconfig RPM because we're adding certain new files
# and directories to the installation without creating the parent directories
# ourselves.
# FIX LATER:  We have not yet delved into exactly which of these possible
# external RPMs actually contain the pathnames, files, and databases we need.
# FIX LATER:  We might be able to relax the version constraints specified here.
PreReq: groundwork-foundation-pro >= 1.6.1, groundwork-monitor-core >= 5.1.3, groundwork-monitor-pro >= 5.1.3

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying a Perl module which makes locally configuring other
Perl packages a lot easier.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)

# ================================================================

%build

# make rpmclean
# make rpms

# ================================================================

%install

# This action is already encoded as a dependency for the "make rpms"
# target in the makefile.
# make perl_typedconfig_install

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

    : do nothing

fi

# ================================================================

%preun

if [ "$1" = 0 ]; then

    # Last uninstall.

    : do nothing

fi

# ================================================================

%postun

# ================================================================

%files

%defattr(0644,nobody,nagios)

# We lock down the permissions to help enforce security, since this
# module is responsible for securely reading sensitive config files.
%attr(0444,nagios,nobody) %{gwpath}/nagios/libexec/TypedConfig.pm

# ================================================================

%changelog
* Sat Dec  8 2007 Glenn Herteg <support@groundworkopensource.com> 1.0.1
- first packaged as an RPM
