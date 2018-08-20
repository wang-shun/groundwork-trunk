================================================================
README for the Nagios Plugins on Windows
================================================================

This directory contains the Nagios Plugins 1.4.16 release, compiled for
Windows / Cygwin by GroundWork Open Source, Inc.  These programs should
run on a Windows machine that does not have Cygwin installed.

Compared to the previous (Nagios Plugins 1.4.5) release we have shipped
in earlier versions of our GroundWork Distributed Monitoring Agents
(GDMA) package for Windows:

  * This compilation brings all the plugins up-to-date with the current
    public release.  This should fix a variety of bugs seen with the
    older plugins.

  * A few new plugins are now supplied:

	check_cluster.exe
	check_ldap.exe
	check_ldaps.exe
	check_ntp_peer.exe
	check_ntp_time.exe
	check_pgsql.exe
	check_smtp.exe

  * The check_ping plugin has been specially patched and built to call
    the native Windows ping.exe command instead of the Cygwin ping command.
    So this plugin now works as intended in the Windows environment.  (See
    the caveat below for usage notes.)

  * All compiled-program filenames have been normalized and are now
    provided with the standard Windows .exe extension, so there will be
    no confusion as to their content.  I believe these files will still
    run as expected using the unadorned base filenames.

  * Some of the compiled plugins are now provided with localized messages
    for French and German locales, per the upstream distribution.

  * This distribution contains no Windows shortcuts masquerading as
    symlinks.  Instead, we provide separate executables for all programs,
    so all of them should run instead of having the old .lnk files fail.

  * All required Cygwin DLLs are included in the distribution.  More such
    DLLs are required now.

  * We have also compiled all the Perl scripts (which would not have run
    on a bare Windows machine).  We supply both source-code .pl and .pm
    files for inspection, along with corresponding compiled .exe files.
    The Perl source files won't run, since we don't provide Perl as well.
    However, they will serve as a good source of debugging information
    when an invocation of one of the binaries emits an error message
    referring to a particular line number.  As with the rest of the
    compiled Nagios plugins, the executables for these Perl scripts
    should run as native Windows binaries.

  * Shell scripts are provided as-is, and are not expected to run,
    since there is no corresponding Windows shell to run them.

  * Note that the check_ntp.exe plugin is deprecated, in favor of either
    the check_ntp_peer.exe or check_ntp_time.exe plugin, depending on
    what you're trying to measure.

  * The check_icmp plugin still doesn't compile, due to the unavailability
    of required resources on the Windows platform.

  * Some support is required from the calling context.  In particular,
    to allow relocating these plugins, the following two environment
    variables must be defined to specify appropriate paths:

	NAGIOS_PLUGIN_LOCALE_DIRECTORY  ( {path-to}/nagios/share/locale )
	NAGIOS_PLUGIN_STATE_DIRECTORY   ( {path-to}/nagios/var )

    This is handled automatically when these plugins are called by the
    GDMA scripting.

----------------------------------------------------------------
Test Results and Caveats
----------------------------------------------------------------

These compilations have not been thoroughly tested, but are assumed
to be in better shape than the older release.  Whether each plugin
is appropriate for running in the Windows environment is also a
separate question.

We do have some test results that may be of use to end-users in
deciding whether to use these plugins.

check_disk -w 10% -c 5% -t 10 -p /
DISK OK - free space: / 13694 MB (33% inode=100%);| /=27163MB;36771;38814;0;40857

    This was run on a disk where the "dir" command in Windows reports
    "14,359,896,064 bytes free".  So the measurement in the status
    message presents the most useful data and looks okay, but the
    performance-data figures look odd for a plugin that is supposedly
    measuring free-space.  In fact, it looks like the performance metrics
    are for the USED space, NOT the FREE space.  This is a confusing
    part of the design of the plugin.  Looking carefully at all the
    parts of the performance data, we see that the values presented are
    in the standard value;warn;crit;min;max format.  So this being a
    disk that Windows says is 39.8GB, the measurements make sense once
    one recognizes that the performance data represents USED space.

C:\Program Files (x86)\groundwork\gdma\libexec\nagios>check_disk -w 10% -c 5% -t 10 -p c:\
cygwin warning:
  MS-DOS style path detected: c:\
  Preferred POSIX equivalent is: /cygdrive/c
  CYGWIN environment variable option "nodosfilewarning" turns off this warning.
  Consult the user's guide for more details about POSIX paths:
    http://cygwin.com/cygwin-ug-net/using.html#using-pathnames
DISK OK - free space: / 13694 MB (33% inode=100%);| /=27163MB;36771;38814;0;40857

    That's a terribly misleading message in the Windows-only context.
    What you need to do in a DOS CMD shell is this command:

	set CYGWIN=nodosfilewarning

    That will set up the enviroment to suppress the warning:

	check_disk -w 10% -c 5% -t 10 -p c:\
	DISK OK - free space: / 13694 MB (33% inode=100%);| /=27163MB;36771;38814;0;40857

    Currently, GDMA is not set up to provide this setting of the
    enviroment variable, so if you need it, you must provide your own
    wrapper script.

check_file_age -f C:\Program Files (x86)\groundwork\gdma\libexec\nagios\check_file_age.exe
FILE_AGE CRITICAL: File not found - C:\Program

check_file_age -f "C:\Program Files (x86)\groundwork\gdma\libexec\nagios\check_file_age.exe"
FILE_AGE CRITICAL: C:\Program Files (x86)\groundwork\gdma\libexec\nagios\check_file_age.exe is 218254 seconds old and 4227156 bytes

    This command may fail to recognize filenames containing spaces
    unless they are properly quoted so the shell passes the full path
    as a single argument to the plugin.  If any of the other plugins
    also take file paths as arguments, the same will probably also
    apply to them.

    This needs to be tested in the context of running as Windows GDMA
    service check, so we can be sure that quotes are being handled
    properly in that situation.

check_load -r -w 90,75,60 -c 100,95,90
CRITICAL - You need more args!!!
Error opening

    This command fails during an attempt to spawn a separate process (an
    empty command) to report the load statistics over the recent past.
    But Windows doesn't provide a mechanism to provide such data, which
    is why the command was defined as empty when this plugin was built.
    An approximation of such load averages could be built from data that
    Windows makes available, but it would take a persistent daemon to
    collect such data on a frequent basis so it could be reported to
    this plugin.

check_ping -H 10.0.0.15 -w 10,5% -c 20,10%
PING OK - Packet loss = 0%, RTA = 4.00 ms|rta=4.000000ms;10.000000;20.000000;0.000000 pl=0%;5;10;0

check_ping -6 -H fe80::250:77ff:fe8d:6b43 -w 10,5% -c 20,10%
PING CRITICAL - Packet loss = 0%, RTA = 23.00 ms|rta=23.000000ms;10.000000;20.000000;0.000000 pl=0%;5;10;0

    This build has IPv6 support included.  The code tries to use IPv6
    by default if it can resolve the given host name to an IPv6 address.
    That can fail if your host is not fully set up to use IPv6.  If there
    is any possible confusion on this point, you should explicitly specify
    the protocol to use ("-4" for IPv4, "-6" for IPv6).  For instance:

    check_ping -H www.google.com -w 10,5% -c 20,10%
    CRITICAL - Host Unreachable (www.google.com)

    check_ping -6 -H www.google.com -w 10,5% -c 20,10%
    CRITICAL - Host Unreachable (www.google.com)

    check_ping -4 -H www.google.com -w 10,5% -c 20,10%
    PING CRITICAL - Packet loss = 0%, RTA = 23.00 ms|rta=23.000000ms;10.000000;20.000000;0.000000 pl=0%;5;10;0

check_time -H 10.0.0.23
TIME OK - 37 second time difference|time=0s;;;0 offset=37s;;;0

    With regard to the performance-data metrics, it looks like "time" is
    how long it took to get a response, while "offset" is the value of the
    response.  The latter metric is perhaps generally of greater interest.
