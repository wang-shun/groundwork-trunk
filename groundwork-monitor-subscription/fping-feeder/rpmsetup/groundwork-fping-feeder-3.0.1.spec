# This is the groundwork fping-feeder spec file needed to construct the
# Groundwork Monitor Fping Feeder Module RPM.

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

%define	name		groundwork-fping-feeder
%define	major_release	3
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

Summary: GroundWork Monitor Fping Feeder Module
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
# groundwork-fping-feeder RPM because we're adding certain new files and
# directories to the installation without creating the parent directories
# ourselves.
# FIX LATER:  We have not yet delved into exactly which of these possible
# external RPMs actually contain the pathnames, files, and databases we need.
# FIX LATER:  We might be able to relax the version constraints specified here.
PreReq: groundwork-foundation-pro >= 1.6.1, groundwork-monitor-core >= 5.1.3, groundwork-monitor-pro >= 5.1.3, gawk

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying a feeder script to manage bulk ping operations.

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
# make fping_feeder_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre

if [ "$1" = 1 ]; then

    # First install.

    if [ -f /usr/local/groundwork/bin/fping ]; then
	# Check the ownership and permissions.
	if [ "`ls -l /usr/local/groundwork/bin/fping | gawk '{print $1, $3, $4}'`" != '-rwsr-sr-x root nagios' ]; then
	    # Since this wasn't installed correctly, we don't trust it not to be some
	    # kind of Trojan horse.  It's up to the administrator to check it out and
	    # correct the situation, rather than us taking on that responsibility.
	    echo "================================================================"
	    echo "ERROR:  /usr/local/groundwork/bin/fping is already present"
	    echo "        but does not have correct ownership (root.nagios) or"
	    echo "        permissions (-rwsr-sr-x or 6755).  You must either move"
	    echo "        this file aside or correct such problems before this RPM"
	    echo "        installation will run."
	    echo "================================================================"
	    exit 1
	fi
    else
        if [ -f /usr/local/groundwork/sbin/fping ]; then
	    # We only do the setuid setup if we think we know the provenance of the
	    # fping binary.  Otherwise, we leave ourselves open a little too wide
	    # to the possibility of a Trojan horse.
	    cp -p /usr/local/groundwork/sbin/fping /usr/local/groundwork/bin/fping
	    chown root.nagios                      /usr/local/groundwork/bin/fping
	    chmod 6755                             /usr/local/groundwork/bin/fping
	else
	    echo "================================================================"
	    echo "ERROR:  You have no copy of fping already installed.  It must be"
	    echo "        installed as /usr/local/groundwork/bin/fping before this"
	    echo "        package is installed.  The groundwork-fping-feeder RPM"
	    echo "        installation is being aborted."
	    echo "================================================================"
	    exit 1
	fi
    fi

fi

if [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  Theoretically, we ought to shut down the feeder if it's running,
    # so the upgrade can go smoothly.  I'm not sure how to do that in a way that
    # will cause gwservices not to start it up again immediately and that will be
    # easy to reverse later on.

    : do nothing

fi

# ================================================================

%post

if [ "$1" = 1 ]; then

    # First install.

    # Make sure the log file is owned by nagios.nagios and writable by that user,
    # in case it was owned by root from some previous installation.
    if [ -f /usr/local/groundwork/foundation/container/logs/fping.log ]; then
	chown nagios:nagios /usr/local/groundwork/foundation/container/logs/fping.log
	chmod 0644          /usr/local/groundwork/foundation/container/logs/fping.log
    fi

    echo "================================================================"
    echo "NOTICE:  The fping feeder is disabled in the default config file"
    echo "         ( /usr/local/groundwork/etc/fping_process.conf ) and"
    echo "         will not operate until you edit the file to localize"
    echo "         the settings and modify the enable_processing value."
    echo "================================================================"

fi

# ================================================================

%preun

if [ "$1" = 0 ]; then

    # Last uninstall.

    # FIX LATER:  Theoretically, we ought to shut down the feeder if it's running.

    # FIX LATER:  The configuration file should be backed up somewhere before the
    # entire package is deleted, since it may represent valuable local configuration
    # data you might want to preserve for a later re-addition of the package.

    : do nothing

fi

# ================================================================

%postun

# ================================================================

%files

%defattr(0644,nobody,nagios)

# We handle this in the %pre section, not in the %files section.
# %attr(6755,root,nagios) %{gwpath}/bin/fping

# This configuration file is intentionally only readable by the owner,
# to enforce security precautions (it contains database-access credentials).
# The associated script must run as the "nagios" user to read this file.
%config(noreplace) %attr(0600,nagios,nagios) %{gwpath}/etc/fping_process.conf

%dir %attr(0755,nagios,nagios) %{gwpath}/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{gwpath}/doc/%{name}-%{version}/groundwork-fping-feeder.3.0.pdf

# We install the same wrapper script in two locations, for operation
# either as a Nagios plugin or as a daemon feeder.
%attr(0755,nagios,nagios) %{gwpath}/nagios/libexec/fping_process.pl
%attr(0755,nagios,nagios) %{gwpath}/foundation/feeder/fping_process.pl

%attr(0644,nagios,nagios) %{gwpath}/profiles/service_profile_fping_feeder.xml

%dir %attr(0755,nagios,nagios) %{gwpath}/services/feeder-nagios-fping
%dir %attr(0755,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log
%dir %attr(0755,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/main
%dir %attr(0700,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/supervise
%dir %attr(0700,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/supervise

%attr(0644,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/main/log
%attr(0755,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/run
%attr(0644,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/supervise/status
%attr(0600,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/supervise/lock
%attr(0755,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/run
%attr(0644,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/supervise/status
%attr(0600,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/supervise/lock

# These two files are actually named pipes.
%attr(0600,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/log/supervise/control
%attr(0600,nagios,nagios) %{gwpath}/services/feeder-nagios-fping/supervise/control

# ================================================================

%changelog
* Tue Dec 11 2007 Glenn Herteg <support@groundworkopensource.com> 3.0.1
- fixed a problem with performance-data reporting that broke the script

* Sat Dec  8 2007 Glenn Herteg <support@groundworkopensource.com> 3.0.0
- upgraded to package as an RPM
- added new features, as described in the documentation
