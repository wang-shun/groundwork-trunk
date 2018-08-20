# This is the jobtracking spec file needed to construct the
# GroundWork Distributed Management Agent RPM.

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
# %define debug_package %{nil}

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

# We set up the external build structure to either define the customer
# macro or let it be defaulted.  The purpose of this value is to tag the
# RPM name so we know when that build has been customized to specify
# particular gid/uid values for the "gdma" UNIX login account.

%define	name		gdma%{?customer:-%{customer}}
%define	major_release	2
%define	minor_release	0
%define	patch_release	4
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

%define	etcinitd			/etc/init.d
%define usrlocalgroundworklib		/usr/local/groundwork/lib
%define usrlocalgroundworklib64		/usr/local/groundwork/lib64
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
Buildroot: %{_installroot}
Packager: Daniel Emmanuel Feinsmith <dfeinsmith@groundworkopensource.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We have no pre-requisites for this package, aside from what the RPM system
# will detect as it analyzes the binaries and scripts that ship inside the RPM.
# PreReq: 

# We need to explicitly declare this capability, to support the case where
# the RPM name includes a customer name.  In that case, we still want the
# associated gdmakey RPM to depend on this generic capability, which won't
# be supplied automatically by the RPM name, so we don't have to customize
# the prerequisites of that customer's gdmakey RPM.
%if %{!?customer:0}%{?customer:1}
Provides: gdma = %{version}-%{release}
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

# Suppress all forms of binary stripping, so we can build one platform's RPM
# on another platform using precompiled binaries without fear of the strip
# program mangling the binaries because of platform-specific differences.
# This will leave the included binaries somewhat larger than they would have
# been with the stripping, but the overall size of this package is too small
# to worry about that.  When we move to per-platform builds from scratch,
# this hacking ought to be disabled.
%define __spec_install_post /usr/lib/rpm/brp-compress || :
%define debug_package %{nil}

exit 0

# ================================================================

%build

# ================================================================

%install

# ================================================================

%clean

# ================================================================

%pre

# We have to pre-make the /usr/local/groundwork directory because useradd won't
# by itself make any of these pathname components when it tries to create the
# final gdma home directory.
if [ ! -d /usr/local/groundwork ]; then
    if /bin/mkdir -p /usr/local/groundwork; then
        : it worked, so we do nothing special here
    else
        echo 'Cannot create the "/usr/local/groundwork" directory; aborting!'
        # Kill the install right now, before we do some damage
        # that we cannot automatically back out later.
        exit 1
    fi
fi
# Let's enforce security regardless of whether the directory previously existed.
/bin/chmod 755 /usr/local/groundwork

# We check to see if the "gdma" group already exists before we try to create it,
# so that doesn't become a problem blocking installation.
#
# A potential source of trouble is that we need to construct a reliable test for
# the group's existence, not just in the local /etc/group file, but also possibly
# instead in LDAP or NIS or some other external source.  We haven't yet tested to
# see whether the Linux groupmod command would return the result we're seeking in
# such cases, and we haven't yet figured out some other way to run this test.
#
# The Solaris groupmod(1M) man page is very clear on this:
#
#     The groupmod utility only modifies group definitions in  the
#     /etc/group  file.  If  a network name service such as NIS or
#     NIS+ is being used to supplement the local  /etc/group  file
#     with  additional entries, groupmod cannot change information
#     supplied by the network name service. The  groupmod  utility
#     will, however, verify the uniqueness of group name and group
#     ID against the external name service.
#
# groupmod invoked without any desired modifications just tests for the group's
# existence, though the Linux man page is not clear about that.  (The Solaris
# man page is very clear on this point.)
if /usr/sbin/groupmod gdma 1>/dev/null 2>&1; then
    # The gdma group already exists.
    : do nothing
else
    # The gdma group does not exist.
    #
    echo 'Adding the "gdma" group ...'
    if /usr/sbin/groupadd %{?gid_number:-g %{gid_number} -o} -f gdma >/dev/null 2>&1; then
        : it worked, so we do nothing special here
    else
        echo 'Cannot create the "gdma" group; aborting!'
        # Kill the install right now, before we do some damage
        # that we cannot automatically back out later.
        exit 1
    fi
fi

# We test to see if the "gdma" user already exists, before we try to create it.
# We intentionally use a test that does not depend on an entry in the local /etc/passwd
# file, because the user might exist instead in LDAP or some other external source.
if /usr/bin/id gdma 1>/dev/null 2>&1; then
    # The gdma user already exists.
    : do nothing
else
    # The gdma user does not exist.  Build it up, in stages.  Use the
    # home directory expected by our package, and a disabled password.
    #
    echo 'Adding the "gdma" user ...'
    if /usr/sbin/useradd -c "GroundWork Agent" -d /usr/local/groundwork/gdma %{?uid_number:-u %{uid_number}} -g gdma -m gdma >/dev/null 2>&1; then
        : it worked, so we do nothing special here
    else
        echo 'Cannot create the "gdma" user; aborting!'
        # Kill the install right now, before we do some damage
        # that we cannot automatically back out later.
        exit 1
    fi
    #
    echo 'Disabling the "gdma" user'"'"'s password ...'
    passwdlockoption=-l
    if /usr/bin/passwd $passwdlockoption gdma; then
        : it worked, so we do nothing special here
    else
        echo 'Cannot set the "gdma" user password; aborting!'
        # Kill the install right now, before we do some damage
        # that we cannot automatically back out later.
        exit 1
    fi
fi

exit 0

# ================================================================

%post

exit 0

# ================================================================

%preun

# = 0 means I'm completely deleting the package.
# non-zero means I'm doing an upgrade.

if [ "$1" = 0 ]; then
    # Last uninstall.

    chkconfig --del gdma
fi

# ================================================================

%postun

# ================================================================

%files

# Okay, the problem now is that it is not sourcing this from the right location.
# Should this be to the gdma user and group?
# Default Attributes, owner and group.
%defattr(0644,gdma,nobody)

# Directories:
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/config
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/log
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/spool
%dir %attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/bin

# Libexec Files:
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ircd.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_log2.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/negate
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_apt
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dl_size.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_sockets.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dell_hw.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_log
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mssql_errorlog.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_udp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_wave
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nwstat.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ica_master_browser.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_sensors.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/rblcheck-web
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_pop3.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_smb.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_sap.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dns
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_disk_smb
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_breeze.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_hpjd
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_qmailq.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_asterisk.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_vcs.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_flexlm
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_real
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_joy.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dig
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_lotus.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_hprsc.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nt
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_spop
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ups
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_smart.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nwstat
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_file_age.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_if.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_procs
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ircd
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_hw.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dummy
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_wave.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_apc_ups.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_backup.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_tcp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_users
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_oracle
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ftp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_lmmon.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_flexlm.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_linux_raid.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dns_random.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mssql2000.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_appletalk.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_remote_nagios_status.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nagios
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/rblcheck-dns
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mailq
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_inodes-freebsd.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ntp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_javaproc.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ica_metaframe_pub_apps.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nagios_latency.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nntps
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_load_remote.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_traceroute.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_simap
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_procl.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/restrict.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mssql2.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_snmp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ssh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_cpu.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_procr.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_file_age
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_bandwidth.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/packet_utils.pm
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_http
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_icmp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nagios_status_log.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_time
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_smtp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/mrtgext.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_digitemp.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_game
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_inodes.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_connections.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nrpe
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mssql.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_nntp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_disk_smb.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_clamd
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/utils.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_logs.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_jabber
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_axis.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_pop
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_fping
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mssql_log.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/urlize.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_sensors
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_by_ssh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_rpc
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_dhcp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/utils.pm
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ping
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_imap
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_wins.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mrtgtraf
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/sched_downtime.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ms_spooler.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_pfstate
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_load
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_swap_remote.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_breeze
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ssmtp
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ftpget.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mailq.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_rpc.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mrtg
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_email_loop.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_adptraid.sh
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_disk
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_overcr
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_mem.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_disk_remote.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/urlize
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_swap
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/libexec/check_ntp.pl

# ./bin files:
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/bin/send_nsca.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/bin/gdma_check.pl
%attr(0755,gdma,gdma) %{usrlocalgroundworkgdma}/bin/gdma_getconfig.pl

# /etc/init.d Files:
%attr(0755,gdma,gdma) %{etcinitd}/gdma

%if %{build_arch} == x86_64

# /usr/local/groundwork/lib64 files:
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libcrypto.so
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libcrypto.so.0
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libcrypto.so.0.9.7
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libssl.so
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libssl.so.0
%attr(0755,gdma,gdma) %{usrlocalgroundworklib64}/libssl.so.0.9.7

%else

# /usr/local/groundwork/lib files:
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libcrypto.so
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libcrypto.so.0
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libcrypto.so.0.9.7
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libssl.so
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libssl.so.0
%attr(0755,gdma,gdma) %{usrlocalgroundworklib}/libssl.so.0.9.7

%endif

# ================================================================

%changelog
* Wed Jun 25 2008 Glenn Herteg <gherteg@groundworkopensource.com> 2.0.4
- replaced all the platform-specific plugin binaries, to fix anomalous
  issues of those binaries not running properly on some platforms; in so
  doing, we upgraded the plugins version from 1.4.5 to 1.4.10 as is now
  used in the GW 5.2.1 product, so some number of bug fixes will also
  now be folded into GDMA

* Sun Jun 15 2008 Glenn Herteg <gherteg@groundworkopensource.com> 2.0.3
- modified to suppress stripping of debug symbols when we build each RPM,
  since strip run on one platform doesn't necessarily do the right thing
  for binaries compiled on other platforms; this adjustment is needed to
  support our current mechanism for building RPMs for all platforms on
  just a single platform, using precompiled binaries, and should be
  dropped once we move to per-platform builds from scratch

* Thu Apr 24 2008 Glenn Herteg <gherteg@groundworkopensource.com> 2.0.2
- modified to test for the presence of an existing group/user, and to not
  try to create such if they already exist
- modified to use specified numeric GID/UID values and home directory when
  creating the gdma group/user, if said values are supplied during the build
  of this RPM

* Wed Oct 31 2007 Daniel Feinsmith <dfeinsmith@groundworkopensource.com> 1.0.0
- initial package construction
