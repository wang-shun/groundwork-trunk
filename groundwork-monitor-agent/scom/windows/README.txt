GroundWork SCOM 2012 Windows SCOM Reaper 
========================================

Summary
-------

NOTE:
For details about SCOM feeder, assets, installation etc see https://kb.groundworkopensource.com/display/GWENG/SCOM+Feeder


SCOM events are processed by the GW SCOM runbook, which dumps each event as an xml file into a directory.
The Windows SCOM 2012 connector watches that directory and processes the events when they appear.
The SCOM 2012 Windows connector is a pair of exes :
  - gwscom_2012_service.exe : this starts/stops the reaper application
  - gwscom_2012_reaper.exe  : this does the event processing and is started/stopped by gwscom_2012_service.exe.

Build Environment Requirements
------------------------------
The following are required for building the Windows SCOM 2012 connector :
  - Activestate Perl
  - Activestate Perl Licensed Perl PDK
  - Module General::Config (install with PPM)

Building
--------
Run the make_all.bat script: 
  - compiles src\gwscom_2012_service.pl into build\gwscom_2012_service.exe
  - compiles src\gwscom_2012_reaper.pl into build\gwscom_2012_reaper.exe
Or, run make_gwscom.bat or make_reaper.bat to build individual binaries.

Installing
----------
Install SCOM and SCORCH and the GroundWork SCOM 2012 Runbook.
Configure the SCOM connector on the GroundWork server, and ensure database connectivity works.
There is no Windows installer - just take the build directory and copy onto the target system.

Configuring
-----------
Configuration of the reaper is done through gwscom.properties, which must be located
in the same directory as gwscom_2012_reaper.exe.
Carefully configure properties before running the service.

Running
-------
To install the Windows GroundWork SCOM 2012 service as an auto starting service, called GWSCOM
 - gwscom_2012_service.exe --install auto
See gwscom_2012_service.exe -h for more details.
If successful, the reaper service will start and you should see things in the log.

Uninstalling
------------
To uninstall, run gwscom_2012_service.exe --remove

Debugging
---------
Tweak the debug options in the properties file.
Run gwscom_2012_reaper.exe -x to have it execute once. Output will be sent to stdout.
An example real 2012 SCOM event is in the test-events folder for testing with - plop it
in the events folder and run gwscom_2012_reaper.exe -x to see what happens.

