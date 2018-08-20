# This is the groundwork-nsca-bulk spec file needed to construct the
# Groundwork Monitor NSCA Bulk Send Package RPM.

# Copyright 2009, 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
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

%define	name		groundwork-nsca-bulk
%define	major_release	2
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

# We require no RPM pre-requisites for the groundwork-nsca-bulk RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

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
# base GroundWork Monitor packages which would be part of our own package's
# PreReq if we still depended on those packages being provided as RPMs.
# So we're working around a deficiency, by actually defeating the dependency
# analysis at install time.

# In the present case, we just need to handle certain Perl modules and the
# GroundWork Perl binary itself.  For filtering the latter, we cannot use
# an edited copy of __perl_requires, so we use __find_requires instead.

# The script below references the original __find_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_find_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'	\
	-e '/perl(Time::HiRes)/d'				\
	-e '/perl(Time::Local)/d'				\
	-e '/perl(strict)/d'
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

# These symlinks need special treatment.
# bulk_nsca_submit.pl log file (pathname set in the script):
#                         %{gwpath}/logs/bulk_nsca_submit.log -> ../nagios/var/log/bulk_nsca_submit.log
%attr(0777,nagios,nagios) %{gwpath}/logs/bulk_nsca_submit.log

# ================================================================

%changelog
* Sun Feb 27 2011 Glenn Herteg <support@groundworkopensource.com> 2.0.1
- moved the debug log file to a more sensible location; added a symlnk to it
- deleted remaining perl dependencies from the generated RPM

* Tue Feb 10 2009 Glenn Herteg <support@groundworkopensource.com> 2.0.0
- porting to the GroundWork Monitor 5.3 environment

* Sun Dec  9 2007 Glenn Herteg <support@groundworkopensource.com> 1.3.1
- first packaged as an RPM
