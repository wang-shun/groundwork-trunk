REM Windows batch script for making the binary files for Windows GDMA.

@echo off
REM
REM  Target build machine is currently:  lysithea
REM
REM  Perl-compiled targets are:
REM
REM      gdma_poll.exe
REM      gdma_run_checks.exe
REM      gdma_service.exe
REM      gdma_spool_processor.exe
REM      check_event_log.exe
REM      check_log2.exe
REM      c_rehash.exe
REM
REM  Note that gdma_service is a Windows service, so it gets compiled
REM  with a different Perl compiler and different compiler options.
REM  Also, the GDMA daemons reference libraries which are different
REM  from those used by the plugins we compile here, so that difference
REM  also affects their respective compiler options.
REM
echo on

REM Set variables only for the duration of this batch script.
setlocal

REM Ugly as hell, but this is how you do back-ticks in CMD.EXE under Windows.
for /f "tokens=1* delims=" %%t in ('perl -e "print scalar localtime"') do set now=%%t

set compiletime=Compile time: %now%
set info=--info "Comments=%compiletime%;CompanyName=GroundWork Open Source, Inc."
set bind=--bind "compile_time[data=%compiletime%]"

@echo off
REM
REM  Specify any Perl modules that must be included, which are not already
REM  automatically packaged into the wrapped program by the PDK tools.  Also
REM  specify any Perl modules which are located by the code analysis done by
REM  perlapp or perlsvc but should not actually be included in our code for
REM  various reasons noted below.
REM
REM  Perl 5.16 moves support for $[ out into a special "arybase" module, which is
REM  dynamically loaded when a reference to this variable is encountered.  But the
REM  PDK tools do not analyze the code in both the script and all the included
REM  modules sufficiently to recognize the appearance of this variable, so this
REM  special module is not included by default even when the code requires it.
REM
REM  The current URI::URL package (from URI 1.64) ends up with a run-time reference
REM  to URI::_query that is not satisfied by compile-time references.  More generally,
REM  the URI package has been reworked between URI 1.60 and URI 1.64 to change from
REM  "require" statements to the equivalent "use parent qw(...)" statements.  The
REM  ActiveState compile tools do not seem to recognize these statements, so we must
REM  include the relevant modules by hand (such as URI::http, URI::https, URI::_server).
REM  The exact set of packages is difficult to identify, since we only discover such
REM  failures at run time, via log messages.  We should really only add the URI-related
REM  packages to the particular application compiles that need them, but there's no
REM  real harm (other than a bit more space in each application) in a blanket approach.
REM
REM  The Log::Log4perl::Appender::File and GDMA::Logging::Wrapline packages are used
REM  but only referenced in the Log::Log4perl configuration data, so perlapp cannot
REM  find them on its own.  Since trouble referencing them is confined to the Windows
REM  platform, we don't bother to "use" these packages within the GDMA::Logging package;
REM  instead we just pre-load them only on this one platform.
REM
REM  The JSON::PP58 package is mentioned in the JSON::PP package, but only in certain
REM  comments, and it is not provided as an actual current standalone package.  The
REM  perlsvc program is too aggressive in looking for package names to understand that,
REM  so we must explicitly exclude it from consideration to suppress error messages.
REM
REM  The File::FcntlLock package is used by the GDMA::LockFile package, referenced by the
REM  GDMA poller, but only in a code branch that is used by UNIX-like operating systems.
REM  The same code running under Windows does not reference that package, so we ignore the
REM  runtime reference to it.  Such suppression is necessary anyway because this package
REM  is not available under Perl for Windows.
REM
REM  The Net::LDAP package is mentioned in the Log::Log4perl::Config package, but only
REM  on a dynamic basis for parsing LDAP URLs used to contain log-configuration data.
REM  Since we have no use for such URLS in our programs, we have not installed Net::LDAP
REM  into our build copy of Perl, and there is no reason to bundle it into our compiled
REM  programs.
REM
REM  The Log::Log4perl::Config::LDAPConfigurator package is in the same situation as the
REM  Net::LDAP package.
REM
REM  The Log::Log4perl::Config::DOMConfigurator package is require'd by Log::Log4perl::Config
REM  in the event that you supply an XML-formatted Log4perl configuration.  We have no need
REM  or desire to deal with that extra complexity, so we can safely ignore that additional
REM  package.
REM
REM  The XML::DOM package is in the same situation as the Log::Log4perl::Config::DOMConfigurator
REM  package, so we exclude that too.
REM
echo on

set modules= ^
    --add arybase ^
    --add URI::_query ^
    --add URI::http ^
    --add URI::https ^
    --add URI::_server

set log_modules= ^
    --add Log::Log4perl::Appender::File ^
    --add Log::Log4perl::Appender::Buffer ^
    --add Log::Log4perl::Appender::Screen ^
    --add GDMA::Logging::Wrapline

set no_modules= ^
    --trim JSON::PP58 ^
    --trim File::FcntlLock ^
    --trim Net::LDAP ^
    --trim Log::Log4perl::Config::LDAPConfigurator ^
    --trim Log::Log4perl::Config::DOMConfigurator ^
    --trim XML::DOM

set stdopts=--force --nologo --nocompress --perl C:\Perl\bin\perl.exe

set perl_gdma_opts=%stdopts% %info% %bind% %modules% %no_modules% --norunlib --lib ..\perl %log_modules%
set perl_plug_opts=%stdopts% %info% %bind% %modules% %no_modules% --norunlib --lib ..\windows\src\plugins
set perl_serv_opts=%stdopts% %info% %bind% %modules% %no_modules% --norunlib
set perl_hash_opts=%stdopts% %info% %bind% %modules% %no_modules%

perlapp %perl_gdma_opts% --exe ..\windows\bin\discover.exe             ..\auto-registration\client\discover
perlapp %perl_gdma_opts% --exe ..\windows\bin\gdma_poll.exe            ..\windows\src\gdma_poll.pl
perlapp %perl_gdma_opts% --exe ..\windows\bin\gdma_run_checks.exe      ..\windows\src\gdma_run_checks.pl
perlapp %perl_gdma_opts% --exe ..\windows\bin\gdma_spool_processor.exe ..\windows\src\gdma_spool_processor.pl
perlsvc %perl_serv_opts% --exe ..\windows\bin\gdma_service.exe         ..\windows\src\gdma_service.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\check_event_log.exe  ..\windows\src\plugins\check_event_log.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\check_log2.exe       ..\windows\src\plugins\check_log2.pl
perlapp %perl_hash_opts% --exe ..\windows-common\bin\c_rehash.exe      ..\windows\src\openssl\c_rehash.pl

