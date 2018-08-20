Copyright 2004-2007 GroundWork Open Source, Inc. ("GroundWork"). All
rights reserved. Use is subject to GroundWork commercial license terms.
For information on licensing and open source elements comprising
GroundWork Monitor Professional, please see a list of IP Ingredients
at http://www.groundworkopensource.com/products/pro-ipingredients.html. 
GroundWork, GroundWork Open Source, GroundWork Monitor Professional, 
GroundWork Foundation, GroundWork Status Viewer, Monarch, and 
GroundWork Guava are trademarks of GroundWork Open Source, Inc.
Other trademarks, logos and service marks (each, a "Mark") used in
Groundwork's products, including Nagios, which is a registered 
trademark of Ethan Galstad, are the property of other third parties.
These Marks may not be used without the prior written consent of 
GroundWork Open Source or the third party that owns the
respective Mark


GroundWork Monitor Professional 5.1.3 Readme
PURPOSE
The purpose of this document is to outline the Known Issues and Bug
Fixes in the current release of the GroundWork Monitor Professional
5.1.x product.

DISTRIBUTION NOTES
In addition to this Readme document, Release Notes and an Installation
Guide accompany this release. These documents contain important
information regarding new features and installation notes.

SUPPORT
Product support is available through a GroundWork subscription 
agreement. For more information, go to GroundWork Support at
http://www.groundworkopensource.com/support/.

CONTENTS
SECTION 1 - KNOWN ISSUES
SECTION 2 - BUG FIXES

--------------------
SECTION 1 - KNOWN ISSUES
--------------------

This section includes a description of the known issues in this
release of GroundWork Monitor Professional.

1. Installing and uninstalling the individual groundwork rpms
is not advised.
To install or uninstall GroundWork Monitor Professional, please
follow the instructions in "GroundWork Monitor Professional
5.1.3 Installation Guide".

2. Uninstalling GroundWork Monitor default Application Packages
is not advised
The GroundWork Monitor default Application Packages should never
be uninstalled. The default Application Packages can be viewed 
from within the Administration tab. If a default package is
removed, GroundWork Monitor will have to be reinstalled. This is
due to a lack of cross-package dependency checking when a package
is uninstalled.  Moreover, there is no "confirm" message presented
when removing packages.

3. Stop snmptt service before upgrading GroundWork
Before you perform an upgrade to 5.1.3, please make sure that all
instances of snmptt have been stopped. This task can be 
accomplished by executing the command:  /etc/init.d/snmpttd stop.
 
4. Advanced Reports - the Availability Reports
Immediately following an installation, there will be no data for
the Availability Reports. The data for these reports are generated
by the dashboard_data_load.pl and dashboard_avail_load.pl scripts
which are normally run once a day, at night, as a cron job.  This
is the expected behavior. These scripts can be run manually to
generate data earlier if required. Please refer to the Bookshelf
for further documentation under the Administrator's Guide,
Section 6, chapter 4 Insight Reports.

5. MySQL rpms for SuSE 9 and 10 platforms MySQL Professional for
SuSE 9 is used by GroundWork Monitor Professional 5.1.3 on both
the SuSE 9 and SuSE 10 platforms.  Please refer to the
installation guide for more information.

6. Configuration application (Monarch) and Performance application
directories accessible via the browser
The file system directories of the Configuration application
(Monarch) and the Performance application are directly accessible
via the browser.  These directories should not be accessed in
this way.

7. Data elements can be modified by multiple users at the same
time. 
GroundWork Monitor Professional does not prevent multiple users
from simultaneously modifying the same data element.  Data
elements include dashboards, Configuration, Administration,
Advanced Reports, hosts, host groups, services, etc.

8. Icon errors within the BIRT Viewer application
When clicking on the table of contents icon or the export icon
within the BIRT Viewer, an exception can occur.  If a user runs
any report that does not match the report criteria or that does
not return data, an error will be generated when selecting
either one of these icons.

9. In the 5.1.3 GroundWork Monitor product release, the
following files contain credentials to access some of the
GroundWork databases in clear text: 
/usr/local/groundwork/apache2/htdocs/reports/gwir.cfg  
/usr/local/groundwork/config/db.properties  
/usr/local/groundwork/foundation/feeder/nagios2collage_socket.pl  
/usr/local/groundwork/guava/includes/config.inc.php
These credentials are visible to a user who can log in to the
GroundWork server and has the correct permissions to read any
of these files.

10. When creating a custom report with the BIRT 2.1.0 report
designer, designers should avoid using Joint Datasets because
they may return incorrect result sets.
If the designer needs to return data from multiple data sources
then one workaround would be to use a stored procedure which
aggregates the data between the multiple databases. In general,
the use of Joint Datasets should be avoided with BIRT 2.1.0.

11. Session disconnect when the IP address changes
The GroundWork Monitor single sign on authentication credentials
include the user's current IP address. If the user's IP address
were to change while using the GroundWork Monitor product, the
user can no longer be authenticated and must sign back in.
This event can occur with dynamic IP addresses and a dropped
VPN connection or if a network cable is unplugged.

12. Upon installation, manual restart of gwservices or system
startup, gwservices may not be fully initialized when users are
first allowed to log in.
To work around this issue, either wait 30 seconds to 1 minute
before logging in or wait until gwservices status shows
"running".

13. Restarting /etc/init.d/gwservices service makes Web Service
components unresponsive in the browser.
In this release, gwservices runs and controls the entire middle
ware that includes the different feeders, the data integration
engine and the Web Service layer that drives the UI. It is not
advised to just restart the gwservices service since it puts
UI components consuming Web Services into an unknown state
and the components no longer respond in the browser. Some
examples of these components are Status Viewer, Dashboards,
and Console. To synchronize the system it is advised to stop
all services used by GroundWork Monitor and restart them in
the order below. Make sure that all users are logged out of
the application before stopping/starting the services
Stopping the services
Stop Apache2 -- /etc/init.d/httpd stop
Stop SNMP Trap translator -- /etc/init.d/snmpttd stop
Stop Foundation -- /etc/init.d/gwservices stop
Starting the services
Start Foundation -- /etc/init.d/gwservices start
Start SNMP Trap translator -- /etc/init.d/snmpttd start
Start Apache2 -- /etc/init.d/httpd start

14. Limitation of 15 concurrent users 
For the GroundWork Monitor Professional 5.1.3 release, we
support up to 15 concurrent users while maintaining acceptable
performance.

15. When restarting any of the services in /etc/init.d/, 
please make sure that all users have logged out of the
GroundWork Monitor server first.
The GroundWork Monitor services are:
syslog-ng - Syslog-ng logging
nsca - starting and stopping the NSCA daemon.
nagios - services that stop and start Nagios
httpd - service used to stop and start Apache
gwservices - service used to stop and start GroundWork Monitor
services.
snmpttd - service used to stop and start Simple Network
Management Protocol (SNMP) Daemon
In most cases, stopping and starting any of the /etc/init.d/
services is not advised. These services are interdependent
upon one another. When these services are randomly started,
the other services will be placed into unknown states. If 
you must stop and start one of these services have all
users log out of the server first. Moreover in most cases,
if an admin needs to stop one of these services, the safest
course of action is to stop all the services and start
them again thereby allowing all the Groundwork Monitor
services to resynchronize with one another.  Note starting
and stopping the nagios service also starts and stops the
nsca service, the syslog-ng service can be started and
stopped independently of the other services, and starting
and stopping the httpd, gwservices, and snmpttd services
should be done in the order documented above.

16. The syslogd process is replaced with syslog-ng.
Any customizations to syslog.conf must be moved to 
/usr/local/groundwork/etc/syslog-ng/syslog-ng.conf.

17. Installation error messages on the SuSE 9 Platform
During installation on the SuSE 9 platform, occasionally
the following error messages may be written to the console:
"Starting HTTP Server (98) Address already in use: make_sock:
could not bind to address [::]:80 Error"  or "error: cannot 
get shared lock on /var/lib/rpm/Packages. Error: cannot open
Packages index using db3 - Operation not permitted. 
Error: cannot open Packages database in /var/lib/rpm".  This
error messages can be ignored
 
18. Wrappit Application does not support special characters
Use only alphanumeric characters within the Wrappit Application.

19. Administrator must delete the config-last.log file when 
importing an older version of monarch.sql.
If you load an older version dump of the Monarch database in
to 5.1.3 you must delete 
/usr/local/groundwork/nagios/etc/config-last.log. 

20. Known issue in FireFox 2.x browser.
A known issue in the FireFox 2.x browser can cause pop-up
windows to become visually corrupted. . This may occur when
a new popup appears above an existing popup. The existing
popup's contents may appear scrambled. The popup contents
can be restored by refreshing the popup.

21. Insight Report Package error with the Outages Report
A known issue in the Insight Report package when selecting
the Outage Report option. The data query is not being
displayed.

22. Foundation database causes long migration time
A known issue in the migration of Foundation package might
take up to an hour. The migration time for the Foundation 
database varies depending on the size of the database
especially the number Events stored in the LogMessage table.
If a customer has less than 50000 messages the migration 
time is below 5 minutes. The migration of Foundation with 
200000 or more messages can take over an hour. 
To determine the number of events open a command window and
log into MySQL. Select the database GWCollageDB and run the
following query: select count (*) from LogMessage.

23. Using port Http 8080 from an URL allows access to links
without authentication
This is a known issue with Foundation package. By entering
http://groundworkserver:8080 brings up a web page that
allows access to these links without authentication.
http://gwserver:8080/reportserver/
http://gwserver:8080/birtviewer/
http://gwserver:8080/foundation-webapp/

24. Performance graphs won't be generated when renaming a
service using special characters or spaces.
This is a known issue with Performance package only use 
alphanumeric characters when renaming a service.

25. Installation error messages on the all platforms.
During installation on the all platforms, occasionally the 
following error messages may be written to the console:
"Setting up the permissions.../bin/chown: changing 
ownership of `/usr/local/groundwork/lib/20060613/20060613':
No such file or directory". This error messages can be ignored.

--------------------
SECTION 2 - BUG FIXES
--------------------
This section includes a description of the bugs fixed in this 
release of GroundWork Monitor Professional. The Bug Key is
listed first followed by the SalesForce Case number if there
is one, and a description.

Bug Fixes in 5.1.3 release

GWMON-2364
3982
Dashboard Functionality around Hostgroups has a major flaw.

GWMON-2239
3870
Security updates.

GWMON-2072
3543
service group escalation problem.

GWMON-2070
3757
Upgrade - document enhancement - Foundation DB migration time.

GWMON-2018
3701
Showing all host's troubled services in TroubleView if the
host is down

GWMON-1725
3455
The new version of Status Viewer doesn't include the console
for hostgroup that was there in 5.0.x releases.

Bug Fixes for 5.1.0 release

GWMON-1272
3147
New User password change - refused entry on new password

GWMON-1058
3057
Dashboard Services List Mishandles Deleted Services

GWMON-1200
3169
Traps are not being inserted into the Foundation layer

GWMON-823
3117
Hosts->host can't save host address change

GWMON-1361
3214
Day light savings patch for the groundwork mysql database
instance

GWMON-1065
3081
"service gwservices (stop|start)" are broken on rhel4

GWMON-1401
3233
When customer pulls up pop-up for Status Viewer when using
IE 6, the window resizes rapidly back and forth.

GWMON-1335
3150
GroundWork installs without mysql dependency check

GWMON-1316
3169
Traps are not being inserted into the Foundation layer

GWMON-1431
3255
Can't create service or host profile

GWMON-1362
3212
Setting mysql password for GroundWork.

GWMON-1440
3238
NodeManager.js JavaScript error contains stack trace with
guava login credentials

GWMON-1417
3175
All of the hosts monitored show status "Pending" instead
of status "Up" under Status->Overview.

GWMON-929
2965, 3074
Configuration->preflight fails when two groups share the
same host group.

GWMON-926
2724, 3239
Access to unprotected urls

GWMON-1421
3251
The check_nwstat provided with GW pro 5 RPMS for SLES10
doesn't return the performance data.

GWMON-1447
3261
Install Pre-requisites, please define them 

GWMON-1358
3121
Console not updating. snmp trap related

GWMON-1457
3153
Corrupted Network data from wmi proxy server caused CPU
@ 100%

GWMON-1308
3176
Cannot save new created Dashboards

GWMON-828
3245
Hostgroups cannot be empty when created, but if all hosts
in a group are deleted, the group stays as an empty group

GWMON-1363
3142
How to setup ldap auth on 5.5

GWMON-1418
3191
Groundwork Feeder service overloaded - most running 10sec
polling

GWMON-1310
3175
All of the hosts being monitored in our system show status
"Pending" instead of status "Up" under Status->Overview

GWMON-1330
3138
Missing libraries in 5.0.5

GWMON-1518
3072
Odd errors in the Console - "localhost DOWN FATAL SYSTEM
Error in Adapter [SERVICE_STATUS]"

GWMON-1514
3281
Nagios "as shipped" configuration issues

GWMON-1460
3297
Check_apache script in GW 5.0.5 - RHEL4 (apache 2.0.52)
– patch attached

GWMON-1458
3296
Import feature fails.

GWMON-1449
3237
Internal Server Error on Profile Tools application

GWMON-727
2650
LogMessageFilter Control: Adding a new filter gets set to
enable even if you specify disabled.

GWMON-844
2594
New host wizard clears hostgroup when setting extended
host info

GWMON-1040
2968
GWServices continually dies.

GWMON-965
2779
Service Group Escalations not being written to nagios
configuration.

GWMON-1360
2276
Bug Suspected: Acknowledge a Problem drop down

GWMON-950
2253
Performance Graph Presentation in (CGI or SV) does not
use the Graph Title from the Performance Configuration Table.

GWMON-884
2662
The check_syslog_gw.pl in GMon 5.0 does not post passive
check results to the syslog_last service.

GWMON-1038
2988
Upgrade from 4.5.26 to 5.04 with mysql root passwd set fails

GWMON-1136
2811
Error: 'opener.guava.core' is null or not an object - error
on login page

GWMON-1031
2921
Config tool un-usable due to slowness

GWMON-1515
2943
SOAP errors on reboot

GWMON-989
2787
Support for https ssl operation in the apache startup file
is missing.

GWMON-1048
2922
Console view does not sort properly

GWMON-734
2640
Nagios interface link to Hosts is really Service Detail,
and Host link is missing

GWMON-1553
2846
Remove "Extra Service Notes" icon & link in Nagios Interface

GWMON-929
2965, 3074
Configuration->preflight fails when two groups share the 
same host group.

GWMON-926
2724, 3239
Unauthorized access to unprotected urls

GWMON-853
Spontaneous Guava Failure Redux

GWMON-1593
3270
Service Check - test service function fails

GWMON-1460
3297
Check_apache script in GW 5.0.5

GWMON-1653
3409
Service test broken, not populating "host"

GWMON-1579
3306
process_service_perf.log grows rampantly

GWMON-1272
3147
New User password change - refused entry on new password

GWMON-1421
3251
The check_nwstat provided with GW pro 5 RPMS for SLES10 doesn't
return the performance data.

GWMON-1419
3247
Files contain passwords in clear text

GWMON-1678
3431
Distributed Groundwork config edits

GWMON-1658
3412
Foundation java process issues after 5.0.5 upgrade.

GWMON-1696
3437
Getting detailed debug information from 
process_service_perf_db.pl

GWMON-1519
3204
No delete button available to delete hosts
(only available to admin users)

GWMON-1417
3175
All of the hosts monitored show status "Pending" 
instead of status "Up" under Status->Overview.

GWMON-1000
2752
We are seeing major data base usage while only a few people
are logged in to the server

GWMON-1001
2878
Wrappit configured with non-standard character causes php
error.

GWMON-1753
Error when clicking on hosts or hostgroups in Status Viewer

GWMON-1745
The customer requested the change below to the base Nagios
system to enable them to use the Nagios host search cgi.

GWMON-1744
It looks like the /etc/init.d/nsca starts nsca in standalone
model (-s flag).

GWMON-1749
3450
Error submitting external commands to Nagios to Status Viewer

GWMON-1740
3465
Tactical overview settings don't save on Dashboards

GWMON-1709
3444
Can not login as administrator

GWMON-1748
3442
In status viewer, if the host or service you are looking at
is very close to the bottom of your browser window and you
try to view the status information mouse over popup, the 
popup will flash madly, and be unreadable 

GWMON-1754
I was in the status viewer and tried to expand a host group
and got a guava error

GWMON-1293
2908
Readme file should list, that there is a Known Issue that
more than the GW product allows that more than one user can
log in as user –"admin"

GWMON-871
2647
Security holes fixed

GWMON-1675
2925
ld.so.conf gets lines added either in the update or not
deleted in the uninstall

GWMON-1448
3260
Foundation listener wouldn't stay listening

GWMON-1803
3319
hosts.cfg duplication of notification periods

GWMON-1431
3355
Can't create service or host profile

GWMON-1833
3507
Bug fix for performance graphs

GWMON-1870
3565
Retention value can not be "0" while docs say to do this…

GWMON-1423
3257
User unable to log into GW via Firefox browser

GWMON-1847
3512
Guava exceptions while adding packages with Wrappit

GWMON-1851
3542
Session files size growing too large

GWMON-1896
3590
Put in the console a dashboard and it does not refresh 
properly.

GWMON-965
2779
Service group escalations not being written to nagios
configuration

GWMON-1828
2911
Advance Reports Documentation is incorrect-and does not
update correctly from db

Release: GroundWork Monitor Professional 5.1.3
Readme: rev 071207a	
GroundWork Open Source, Inc.