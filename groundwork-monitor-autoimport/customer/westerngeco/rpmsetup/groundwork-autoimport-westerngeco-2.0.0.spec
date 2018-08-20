# This is the groundwork-autoimport-westerngeco spec file needed to construct the
# Groundwork Monitor Auto-Import Package WesternGeco Extension RPM.

# Copyright 2007, 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
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

%define	name		groundwork-autoimport-westerngeco
%define	major_release	2
%define	minor_release	0
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

Summary: GroundWork Monitor Auto-Import WesternGeco Custom Hostgroup Package
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

# We require no RPM pre-requisites for the groundwork-autoimport-westerngeco RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software customizes the GroundWork Monitor Auto-Import
extension by supplying Perl modules which define hostgroups
in accordance with the WesternGeco host naming standard.

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

# The script below references the original __perl_requires macro,
# so we cannot redefine it here before creating that script. 
%define edit_perl_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_perl_requires}
#!/bin/sh
%{__perl_requires} $* |\
    sed -e '/perl(strict)/d'
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
# make autoimport_westerngeco_install

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

# FIX MINOR:  Is this really the best place for these modules to live now?
%attr(0644,nagios,nagios) %{gwpath}/nagios/libexec/WesternGecoHostgroupName.pm
%attr(0644,nagios,nagios) %{gwpath}/nagios/libexec/WesternGecoHostgroupName_Remote.pm

# This is an initial copy of the host-prefix-as-site-code to hostgroup-name mapping.
# No doubt it will be modified over time on installed systems, and thus this copy
# will become obsolete.  So we treat it like any other config file, making sure that
# any updates to this package won't wipe out local modifications to the file.
%config(noreplace) %attr(0644,nagios,nagios) %{gwpath}/config/Site_Code_Name.conf

# ================================================================

%changelog
* Sat Feb 26 2011 Glenn Herteg <support@groundworkopensource.com> 2.0.0
- modified to include WesternGeco-enhanced host type analysis
- repackaged for use with GWMEE 6.4

* Sun Dec  9 2007 Glenn Herteg <support@groundworkopensource.com> 1.0.0
- first packaged as an RPM
