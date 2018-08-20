# $Id: $
#
# Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

%define filelist @PREFIX@/../@EXPORT_FILELIST@
%define prefix @PREFIX@
%define release @RELEASE_NUMBER@
Summary: GroundWork Bookshelf.
Name: @PACKAGE_NAME@
Version: @PACKAGE_VERSION@
Release: @RELEASE_NUMBER@
License: Copyright 2008 GroundWork Open Source, Inc. (GroundWork). All rights reserved. Use is subject to GroundWork commercial license terms.
Group: System Environment/Base
Source: %{name}-%{version}-%{release}.tar.gz
BuildRoot: %{_tmppath}/%{name}
Prefix: %{prefix}
BuildArch: noarch
#Requires: groundwork-monitor-core >= 5.2.0
#Requires: @DEPENDENCY@

AutoReqProv: no
%description
Bookshelf contains GroundWork Monitor product reference for; Operators regarding the use of the system applications and other resources via the user interface; Administrators who need to verify an installation, configure, customize, and maintain the system as part of its initial implementation or to manage the system, and for Developers who may need to customize the system and refer to information on GroundWork Foundation, which provides access to the underlying data in the GroundWork Monitor package.
%prep
%setup -q

%install
rm -rf %{_tmppath}/%{name}
mkdir %{_tmppath}/%{name}/
mkdir %{_tmppath}/%{name}/etc
mkdir %{_tmppath}/%{name}/usr
mkdir %{_tmppath}/%{name}/usr/local
mkdir %{_tmppath}/%{name}%{prefix}

cp -r usr %{_tmppath}/%{name}/

find . -type d | sed '1,2d;s,^\.,\%attr(-\,nagios\,nagios) \%dir ,' >  %{filelist}
find . -type f | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}
find . -type l | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}

sed '1d' %{filelist} > %{filelist}.tmp
/bin/mv -f %{filelist}.tmp %{filelist}

%pre

# Check if groundwork-monitor-core-5.3.0 is installed, due to rpm bug
if !(rpm -qa | /bin/fgrep -l 'groundwork-monitor-core-5.3.0');then
  bin/echo  "groundwork-monitor is missing..."
  exit 1
fi

/bin/rm -rf %{prefix}/docs/bookshelf-data/*

%post
if [ -f /usr/local/groundwork/bin/php ] ; then
 /usr/local/groundwork/bin/php  /usr/local/groundwork/migration/gw-bookshelf-install.php localhost guava guava gwrk
fi

/bin/ln -sf /usr/local/groundwork/docs/bookshelf-data /usr/local/groundwork/guava/packages/bookshelf/bookshelf-data &>/dev/null

/bin/cp -fp /usr/local/groundwork/docs/whphost.js /usr/local/groundwork/docs/bookshelf-data
/bin/cp -fp /usr/local/groundwork/docs/whskin_frmset01.htm /usr/local/groundwork/docs/bookshelf-data
/bin/cp -fp /usr/local/groundwork/docs/whnjs.htm /usr/local/groundwork/docs/bookshelf-data

%preun
install_mode="$1"
if [ "$install_mode" = "0" ] ; then
if [ -f /usr/local/groundwork/bin/php ] ; then
  /usr/local/groundwork/bin/php  /usr/local/groundwork/migration/gw-bookshelf-install.php localhost guava guava gwrk remove
fi
  /bin/rm -rf %{prefix}/docs/bookshelf-data/bookshelf-data/*
fi

%clean
rm -rf %{_tmppath}/%{name}

%files -f %{filelist}
%defattr(-, nagios, nagios, 0755)

%changelog
