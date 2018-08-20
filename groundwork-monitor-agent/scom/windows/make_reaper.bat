@echo off
setlocal


for /f "tokens=1* delims=" %%t in ('perl -e "print scalar localtime"') do set now=%%t
set compiletime=Compile time: %now%
set info=--info "Comments=%compiletime%;CompanyName=GroundWork Open Source, Inc."
set bind=--bind "compile_time[data=%compiletime%]"

set perl_reaper_opts=--force --nologo --norunlib --nocompress %info% %bind% --perl C:\Perl\bin\perl.exe --lib src
perlapp %perl_reaper_opts% --exe build\gwscom_2012_reaper.exe  src\gwscom_2012_reaper.pl

rem set perl_serv_opts=--force --nologo --norunlib --nocompress %info% %bind% --perl C:\Perl\bin\perl.exe
rem perlsvc %perl_serv_opts% --exe build\gwscom_2012_service.exe   src\gwscom_2012_service.pl

