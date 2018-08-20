# This is the jobtracking spec file needed to construct the
# GroundWork Distributed Management Agent Key RPM.

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# The construction of this spec file is a bit unusual in that it is
# intended to be self-contained; that is, we don't want to depend on
# the particular user's dot-files (~/.rpmrc or ~/.rpmmacros) and build
# structure (pre-established ~/rpmbuild/*/ directories).  Also, we don't
# start with a source RPM or tarball.  Rather, we assume the source code
# is already splayed out, and all we're trying to do at this stage is to
# construct the RPM(s).

#@dump

# ================================================================

# To do when filling out this RPM specfile skeleton in the general case:
# (*) add prep instructions
# (*) add build instructions
# (*) add install instructions
# (*) add pre-install and post-install scripts
# (*) add pre-uninstall and post-uninstall scripts
# (*) fill in a complete list of files and their attributes
# (*) have the RPM install/uninstall cron jobs as needed

# ================================================================

%define	name		gdmakey%{?server_name:-%{server_name}}
%define	major_release	2
%define	minor_release	0
%define	patch_release	3
%define	version		%{major_release}.%{minor_release}.%{patch_release}

%define	usrlocalgroundworkgdma		/usr/local/groundwork/gdma 

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Distributed Monitoring Agent
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{usrlocalgroundworkgdma}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')
# Source: %{name}-%{version}.tar.gz
Version: %{version}
BuildArchitectures: noarch
Buildroot: %{_installroot}
Packager: Daniel Emmanuel Feinsmith <dfeinsmith@groundworkopensource.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require the gdma capability as a pre-requisite for the gdmakey RPM because
# we're adding new files and directories to the installation without creating
# the parent directories ourselves.  This capability can be supplied either by
# the standard, uncustomized gdma RPM or by a customer-specific gdma-customer-name
# RPM (which is rigged to still declare this same capability, precisely so this
# single dependency declaration here will work).
PreReq: gdma

# We want to explicitly declare this capability, to support the case where
# the RPM name includes a server name.  In that case, we still want the
# associated gdmakey RPM to supply this generic capability, which won't
# be supplied automatically by the RPM name, so any future RPM which must
# depend on this GDMA key RPM will have an unambiguous capability to use
# as its prerequisite.
%if %{!?server_name:0}%{?server_name:1}
Provides: gdmakey = %{version}-%{release}
%endif

# ================================================================

%description
This software extends the base GroundWork Monitor Professional
product with a monitoring agent that is distributed to monitored
hosts, for efficient collection of monitoring data.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)
exit 0

# ================================================================

%build

# ================================================================

%install

# ================================================================

%clean

# ================================================================

%pre

exit 0

# ================================================================

%post

exit 0

# ================================================================

%preun

# ================================================================

%postun

# ================================================================

%files

# Default Attributes, owner and group.
%defattr(0644,gdma,nobody)

# Directories:
# The gdma home directory and its config/ subdirectory should already
# be provided by the base gdma package, which is now a prerequisite.  So
# there's no reason for us to claim ownership here of those directories.
# %dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}
# %dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/config
%dir %attr(0700,gdma,gdma) %{usrlocalgroundworkgdma}/.ssh

# Files:
# We don't declare the gdma_server.conf file here with %config(noreplace)
# because that would defeat the purpose of using an automated remote
# install to distribute/update the key to a bunch of client machines
# without a lot of manual fixup.
%attr(0644,gdma,gdma) %{usrlocalgroundworkgdma}/config/gdma_server.conf
%attr(0600,gdma,gdma) %{usrlocalgroundworkgdma}/.ssh/id_dsa
%attr(0644,gdma,gdma) %{usrlocalgroundworkgdma}/.ssh/id_dsa.pub

# We only include a known_hosts file if it exists in our build root.
%if %{have_customer_known_hosts} == 1
%attr(0644,gdma,gdma) %{usrlocalgroundworkgdma}/.ssh/known_hosts
%endif

# ================================================================

%changelog
* Fri Jun 27 2008 Glenn Herteg <gherteg@groundworkopensource.com> 2.0.3
- added support for distributing a pre-established, customer-specific
  ~gdma/.ssh/known_hosts file

* Mon Apr 28 2008 Glenn Herteg <gherteg@groundworkopensource.com> 2.0.2
- added a dependency on the gdma capability, to ensure these RPMs get
  installed in the correct sequence
- dropped the gdma home directory and its config subdirectory, as those
  must be supplied and owned by the gdma RPM instead
- fixed permissions on the id_dsa.pub and gdma_server.conf files
- cleaned up the RPM package name and lots of other build stuff

* Wed Oct 31 2007 Daniel Feinsmith <dfeinsmith@groundworkopensource.com> 1.0.0
- initial package construction
