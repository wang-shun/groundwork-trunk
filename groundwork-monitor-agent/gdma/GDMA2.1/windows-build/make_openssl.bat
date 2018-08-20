REM  Windows batch script for capturing the Cygwin binary files for OpenSSL
REM  that will operate under Windows GDMA.
REM
REM  Target build machine is currently:  wingdma-dev
REM
REM  There is nothing to build here; this script only captures pre-established
REM  Cygwin libraries and executables.  The c_rehash.exe file is separately
REM  built by compiling our modified copy of the Perl C:\cygwin\bin\c_rehash
REM  script, in a different make script.
REM
REM  We capture the following files into Subversion:
REM
REM      C:\cygwin\bin\cygcrypto-1.0.0.dll
REM      C:\cygwin\bin\cyggcc_s-1.dll
REM      C:\cygwin\bin\cygssl-1.0.0.dll
REM      C:\cygwin\bin\cygwin1.dll
REM      C:\cygwin\bin\cygz.dll
REM      C:\cygwin\bin\openssl.exe
REM

mkdir C:\gdma_release_build
cd    C:\gdma_release_build

C:\cygwin\bin\svn checkout http://geneva/groundwork-professional/trunk/monitor-agent/gdma/GDMA2.1/windows-common

copy C:\cygwin\bin\cygcrypto-1.0.0.dll windows-common\bin
copy C:\cygwin\bin\cyggcc_s-1.dll      windows-common\bin
copy C:\cygwin\bin\cygssl-1.0.0.dll    windows-common\bin
copy C:\cygwin\bin\cygwin1.dll         windows-common\bin
copy C:\cygwin\bin\cygz.dll            windows-common\bin
copy C:\cygwin\bin\openssl.exe         windows-common\bin
copy C:\cygwin\usr\ssl\openssl.cnf     windows-common\openssl

echo Checking in updated Cygwin OpenSSL binaries and config file ...
C:\cygwin\bin\svn commit --message "Capturing updated Cygwin OpenSSL binaries and config file." --no-auth-cache windows-common

