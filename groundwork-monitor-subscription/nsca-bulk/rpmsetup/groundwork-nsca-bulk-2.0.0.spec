# This is the groundwork-nsca-bulk spec file needed to construct the
# Groundwork Monitor NSCA Bulk Send Package RPM.

# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork").  All rights
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
%define _use_internal_dependency_generator     0

# ================================================================

%define	name		groundwork-nsca-bulk
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

Summary: GroundWork Monitor NSCA Bulk Send Package
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
# groundwork-nsca-bulk RPM because we're adding certain new files
# and directories to the installation without creating the parent directories
# ourselves.
# FIX LATER:  We have not yet delved into exactly which of these possible
# external RPMs actually contain the pathnames, files, and databases we need.
# FIX LATER:  We might be able to relax the version constraints specified here.
# NOTE:  In the Bitrock environment, we can no longer depend on GroundWork RPMs
# to be present.
# PreReq: groundwork-foundation-pro >= 1.6.1, groundwork-monitor-core >= 5.1.3, groundwork-monitor-pro >= 5.1.3

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying a Perl script which bundles up a lot of host-check and
service-check data from a performance-data log, and sends it very
efficiently to Nagios, through high-volume connections to NSCA.

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

# In the present case, we just need to handle certain Perl modules and the
# GroundWork Perl binary itself.  For filtering the latter, we cannot use
# an edited copy of __perl_requires, so we use __find_requires instead.

# The script below references the original __find_requires macro,
# so we cannot redefine it here before creating that script. 
%define edit_find_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/perl(Time::HiRes)/d'	\
	-e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'
EOF
chmod +x %{edit_find_requires}

# Note:  The __find_requires macro is only used because we set
# _use_internal_dependency_generator to zero above.  Otherwise,
# this redefinition wouldn't make any difference.
%define __find_requires %{edit_find_requires}

# ================================================================

%build

# make rpmclean
# make rpms

# ================================================================

%install

# This action is already encoded as a dependency for the "make rpms"
# target in the makefile.
# make nsca_bulk_install

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

%dir %attr(0755,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{gwpath}/common/doc/%{name}-%{version}/bulk_nsca_submit_install

%attr(0755,nagios,nagios) %{gwpath}/nagios/eventhandlers/bulk_nsca_submit.pl

# ================================================================

%changelog
* Tue Feb 10 2009 Glenn Herteg <support@groundworkopensource.com> 2.0.0
- porting to the GroundWork Monitor 5.3 environment

* Sun Dec  9 2007 Glenn Herteg <support@groundworkopensource.com> 1.3.1
- first packaged as an RPM
