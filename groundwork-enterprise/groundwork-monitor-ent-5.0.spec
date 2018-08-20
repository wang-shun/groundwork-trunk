#$Id: $
# Copyright (C) 2008 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
%define filelist /usr/local/groundwork/../ent-filelist
%define prefix /usr/local/groundwork
%define release 9
Summary: GroundWork Monitor Enterprise.
Name: groundwork-monitor-ent
Version: 5.3.0
Release: 9
License: License: Copyright 2006 GroundWork Open Source, Inc. (GroundWork). All rights reserved. Use is subject to GroundWork commercial license terms.
Group: System Environment/Base
BuildArch: noarch
Source: %{name}-%{version}-%{release}.tar.gz
BuildRoot: %{_tmppath}/%{name}
Prefix: %{prefix}
Requires: groundwork-monitor-pro
AutoReqProv: no
%description 
GroundWork Monitor is a comprehensive IT infrastructure availability and performance monitoring system based on integrated
and enhanced open source software. GroundWork Monitor leverages the functionality of Nagios, an open source IT monitoring
tool,and transforms it into an enterprise-ready IT management solution by adding functional enhancements as well as deployment services and ongoing support.
This easy-to-use monitoring system lets you quickly identify and resolve system outages or performance slowdowns before they significantly impact business processes or customers. Often, GroundWork Monitor can detect a problem before it causes an outage.If you require an advanced or specialized monitoring solution, or simply prefer professional deployment of GroundWork Monitor, GroundWork's packaged deployment service, GroundWork Install, will deliver a complete design, installation and configuration of your monitoring system.
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
cp -f %{prefix}/guava/themes/gwmpro/images/logo.gif %{prefix}/guava/themes/gwmpro/images/logo.pro.gif

%post
cat %{prefix}/guava/packages/guava/templates/home.xml | sed 's/Welcome to GroundWork Monitor Professional/Welcome to GroundWork Monitor Enterprise/' > %{prefix}/guava/packages/guava/templates/home-ent.xml
cp -f %{prefix}/guava/packages/guava/templates/home-ent.xml %{prefix}/guava/packages/guava/templates/home.xml
cp -f %{prefix}/guava/includes/config.inc.php.ent %{prefix}/guava/includes/config.inc.php
cp -f %{prefix}/guava/themes/gwmpro/images/logo.ent.gif %{prefix}/guava/themes/gwmpro/images/logo.gif

%preun

%postun
if [ "$1" = "0" ] ; then # last uninstall
  cp -f %{prefix}/guava/packages/guava/templates/home-pro.xml %{prefix}/guava/packages/guava/templates/home.xml
  cp -f %{prefix}/guava/includes/config.inc.php.pro %{prefix}/guava/includes/config.inc.php
  cp -f %{prefix}/guava/themes/gwmpro/images/logo.pro.gif %{prefix}/guava/themes/gwmpro/images/logo.gif
fi #Last uninstall

%clean
rm -rf %{_tmppath}/%{name}
%files -f %{filelist}

%changelog
