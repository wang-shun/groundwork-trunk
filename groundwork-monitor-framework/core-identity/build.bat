@echo off
rem
rem  Invokes a script of the same name in the 'tools' module.
rem  
rem  The 'tools' module is expected to be a peer directory of the directory
rem  in which this script lives.
rem
rem  @author Jason Dillon <jason@planet57.com>
rem

rem $Id: build.bat 1015 2005-11-04 20:15:13Z mholzner $

setlocal

set PROGNAME=%~nx0
set DIRNAME=%~dp0

rem Legacy shell support
if x%PROGNAME%==x set PROGNAME=build.bat
if x%DIRNAME%==x set DIRNAME=.\

set MODULE_ROOT=%DIRNAME%
if x%TOOLS_ROOT%==x set TOOLS_ROOT=%DIRNAME%..\tools
set TARGET=%TOOLS_ROOT%\bin\build.bat
set ARGS=%*

rem Start'er up yo
goto main

:debug
if not x%DEBUG%==x echo %PROGNAME%: %*
goto :EOF

:main
call :debug PROGNAME=%PROGNAME%
call :debug DIRNAME=%DIRNAME%
call :debug TOOLS_ROOT=%TOOLS_ROOT%
call :debug TARGET=%TARGET%

if exist %TARGET% call :call-script & goto :EOF
rem else fail, we can not go on

echo %PROGNAME%: *ERROR* The target executable does not exist:
echo %PROGNAME%:
echo %PROGNAME%:    %TARGET%
echo %PROGNAME%:
echo %PROGNAME%: Please make sure you have checked out the 'tools' module
echo %PROGNAME%: and make sure it is up to date.
goto :EOF

:call-script
call :debug Executing %TARGET% %ARGS%
call %TARGET% %ARGS%
goto :EOF
