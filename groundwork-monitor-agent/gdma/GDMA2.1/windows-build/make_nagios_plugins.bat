REM  Windows batch script for making the binary files for Nagios plugins
REM  that will operate under Windows GDMA.
REM
REM  Target build machine is currently:  lysithea
REM
REM  Perl-compiled targets are:
REM
REM      check_breeze.exe
REM      check_disk_smb.exe
REM      check_file_age.exe
REM      check_flexlm.exe
REM      check_ifoperstatus.exe
REM      check_ifstatus.exe
REM      check_ircd.exe
REM      check_mailq.exe
REM      check_rpc.exe
REM      check_wave.exe
REM

setlocal
set perl_plug_opts=--force --nologo --norunlib --nocompress --perl C:\Perl\bin\perl.exe --lib ..\windows\src\plugins

perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_breeze.exe       ..\windows\src\plugins\check_breeze.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_disk_smb.exe     ..\windows\src\plugins\check_disk_smb.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_file_age.exe     ..\windows\src\plugins\check_file_age.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_flexlm.exe       ..\windows\src\plugins\check_flexlm.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_ifoperstatus.exe ..\windows\src\plugins\check_ifoperstatus.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_ifstatus.exe     ..\windows\src\plugins\check_ifstatus.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_ircd.exe         ..\windows\src\plugins\check_ircd.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_mailq.exe        ..\windows\src\plugins\check_mailq.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_rpc.exe          ..\windows\src\plugins\check_rpc.pl
perlapp %perl_plug_opts% --exe ..\windows\libexec\nagios\check_wave.exe         ..\windows\src\plugins\check_wave.pl

