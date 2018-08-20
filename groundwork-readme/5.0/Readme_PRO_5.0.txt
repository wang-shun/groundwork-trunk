Readme rev 1.1	Current Release: GroundWork Monitor Professional 5.0.2

Copyright 2006 GroundWork Open Source Solutions, Inc. ("GroundWork"). All rights reserved. Use is subject to GroundWork 
commercial license terms. GroundWork Monitor Professional is released under the terms of the various public and 
commercial licenses. For information on licensing and open source elements please see GroundWork Monitor Professional 
IP Ingredients at http://www.groundworkopensource.com/products/pro-ipingredients.html. For a list of open source software 
and the associated licenses included with this software, see http://www.groundworkopensource.com/products/pro-
licensefaq.html. GroundWork, GroundWork Open Source, GroundWork Monitor Professional, GroundWork Monitor Small 
Business, GroundWork Monitor Open Source, GroundWork Foundation, GroundWork Status Viewer, Monarch, and 
GroundWork Guava are trademarks of GroundWork Open Source, Inc. Other trademarks, logos and service marks (each, a 
"Mark") used in GroundWork’s products, including Nagios, which is a registered trademark of Ethan Galstad, are the 
property of other third parties. These Marks may not be used without the prior written consent of GroundWork Open Source 
or the third party that owns the respective Mark.

Table of Contents
1	PURPOSE
2	BUG FIXES
3	KNOWN ISSUES
----------
1	PURPOSE
Important Note: The distributed Readme document should be reviewed prior to the installation or upgrade 
of GroundWork Monitor Professional 5.0.

The purpose of this “Readme” document is to outline the bug fixes and known issues in the current release 
of the GroundWork Monitor Professional 5.0 product. Additionally, the documents “Installation” and “Release 
Notes” outline system hardware requirements, pre-installation planning, and steps for a new installation or 
an upgrade; and new features in this release, respectively. All documents are distributed in .txt and .pdf 
formats.
----------
BUG FIXES
This section includes a description of the bugs fixed in this release of GroundWork Monitor Professional.
	Release 5.0.1
The following lists GWPRO 4.5 issues fixed in this release of GroundWork Monitor Professional.
1.	GWMPRO-391
GroundWork Commit timing-out.
2.	GWMPRO-390
Successfully assign an escalation tree to a service, and then try to remove this tree by leaving the field 
for an escalation tree blank. The escalation tree entry remains.
3.	GWMPRO-386
The system won't allow you to rename a service from uppercase to lowercase. It interprets these as the 
same name.
4.	GWMPRO-383
When attempting to create a new host profile, I get an error stating that a template is required, despite 
selecting a template.
5.	GWMPRO-382
Extended Host info not applied to host profile.
6.	GWMPRO-381
Rename a service and the box says "Reame".
7.	GWMPRO-377
Dashes not accepted in contact properties pager field.
8.	GWMPRO-374
Re-load from nagios.cfg loses perfdata processing.
9.	GWMPRO-371
When importing files using Control>Load, service group definitions are created but the groups are not 
populated with services.
10.	GWMPRO-369
Performance Tab displays Hosts and Services that have been deleted.
11.	GWMPRO-359
Copyright notices for the Bookshelf documentation in GW Professional should not be claiming copyright 
to the other open source projects MySQL, RRD, etc.
12.	GWMPRO-358
Name space problem when assigning hostgroup and contactgroup the same name.
13.	GWMPRO-352
Runaway Java adapter feeder CPU usage.
14.	GWMPRO-350
hostgroup membership not correct.
15.	GWMPRO-339
Problem with Discover Host Wizard.
16.	GWMPRO-337
Configuration>Control>Setup>Max Tree Nodes is misleading and ignored.
17.	GWMPRO-333
Nagios load failed.
18.	GWMPRO-328
Delete service template not cleaning up contactgroup_assign table.
19.	GWMPRO-327
Some navigation drill downs revert to being unexpanded on refresh.
20.	GWMPRO-314
If you attach more than two escalation trees, only the first two show up in Configuration.
21.	GWMPRO-300
I exported a standard service profile, which included a service template. I made a change so that the 
service profile with this value was not imported back to Monarch.
22.	GWMPRO-278
Configuration>Commands>Modify>Command Name, When you click the "Test" button it does 
something backwards.
23.	GWMPRO-273
After removing a service check in Configuration with a commit, a user is still able to access the service 
in Performance tab.
24.	GWMPRO-271
Import fails after Add button pressed on a partial selection.
25.	GWMPRO-270
Wrong directory path listed for nmap_scan_one.pl. Script with EZ-Configuration Host Discovery.
26.	GWMPRO-264
The Control>Nagios Main Config>View Edit mouse-over definition for use_retained_scheduling 
information is incorrect.
27.	GWMPRO-262
Spelling typo under Configuration>Service Check sub tab.
28.	GWMPRO-250
Configuration>Discover> add a host, re-run Discover wizard, add same host, get MySQL error.
29.	GWMPRO-248
SNMP_if performanceconfiguration row obscures SNMP_if_bandwidth row.
30.	GWMPRO-224
The notion of template values being inherited is not very clear in the Bookshelf documentation.
31.	GWMPRO-214
net_snmp_utils are no longer shipped with product; need to be added in order to perform SNMP trap.
32.	GWMPRO-213
Add a scroll bar so that when assigning more than 9-10 packages to a role, all the tabs can be viewed 
on the screen.
33.	GWMPRO-201
Please make the gwservices init script support "status" as well as start and stop.
-----
	Release 5.0.2
1.	GWMON-840
Trying to add a message filter, does not work.
2.	GWMON-832
After removing from MySQL 4.1.XX, the /etc/my.conf file should be removed. This file will conflict with 
the MySQL 5.0.XX install.
3.	GWMON-831
After a successful commit from within the UI, the apache2 error logs show a [Wed Nov 08 11:44:44 
2006] [error] [client 172.28.113.67] DBI::db=HASH(0x88fe918)->disconnect invalidates 1 active 
statement handle.
4.	GWMON-824
After upgrading to the most recent RPMs 5.0-5.0 Guava login produces: Class WidgetDaemon not 
found.
5.	GWMON-820
Documentation error in upgrade section of the 5.0 Installation.txt.
6.	GWMON-817
The Monarch db for GWMON 5.0 GA contains entries for RRD and ping services for test hosts 
(Sonicwall, Intel, Corporate).
7.	GWMON-813
Guava exception within bookshelf.
8.	GWMON-801 IE6
Monarch Escalation navigation pane don't show escalations and trees that were created.
9.	GWMON-785
Class 'LDAPAuthModule' not found in /usr/local/groundwork/guava/includes/guava.inc.php on line 2042.
10.	GWMON-769
Contact groups for host overrides not written to files.
11.	GWMON-768
Bookshelf says to download MySQL 5 mysql-connector-java-3.1.13.jar but doesn't say from where.
12.	GWMON-766
Profile tools, import option died at /usr/local/groundwork/apache2/cgi-bin/monarch/importcfg.pl line 760, 
<IMPORTFILE> line 79.
13.	GWMON-764
Command line arguments for service: local_process_gw_listener are wrong and service always reports 
CRITICAL.
14.	GWMON-763
Upgrading from 4.5-26 with a MySQL passwd already set to Small Business 5.0 rpms trying to complete 
the PRO build, experiencing MYSQL errors.
15.	GWMON-758
Save as Profile on the host wizard does not save host wizard.
16.	GWMON-751
5.0 to 5.0 migration does not retain installed packages, potentially causing critical error dialogs.
17.	GWMON-731
Service Instances not included in sync with foundation.
18.	GWMON-569
Guava re-draws menu items.
19.	GWMON-251
syslog still set to startup even though syslog-ng is also set to startup PLUS other syslog-ng config 
issues.
20.	GWMON-697
resource.cfg cannot be deployed from the build folder.
21.	GWMON-707
New host wizard>service profile doesn't apply service dependencies.
22.	GWMON-731
Service Instances not included in sync with foundation.
23.	GWMON-590
Host Delete Tool does not work for 192* hosts.
24.	GWMON-568
In EZ and Config no response given when host searched.
25.	GWMON-712
Cannot remove service escalation from host>service.
26.	GWMON-724
Successfully assign an escalation tree to a service, and then try to remove this tree by leaving the field 
for escalation tree blank. The escalation tree entry remains.
27.	GWMON-706
Spaces in Service Template names.
28.	GWMON-710
Delete host templates doesn't clean up contactgroup_assign table.
----------
2	KNOWN ISSUES
This section includes a description of the know issues in this release of GroundWork Monitor Professional. 
	Release 5.0.1
1.	Starting Groundwork Services 
During installation the "Starting Groundwork Services” may be reported as Failed. This is a known race 
condition that occurs during the install. If this occurs ignore the warring and continue with the install. 
Once the application is started, GroundWork Services will be restarted properly. At the end of install, 
verify that the gwservices is running using the command - /etc/init.d/service gwservices status. If 
gwservices process is not running, start it by using the command /etc/init.d/gwservices start
2.	IE 6 Compatibility Issues
Issues with IE6 error handling may cause the GWMON application to freeze. If this should occur kill and 
restart your browser and log back into the GWMON application.
3.	Application Timeout 
Leaving a session open for a long time, may cause a session time out. If this should occur logout and 
re-login to the GWMON application.
4.	Upgrading
Upgrading from 5.0 to a newer version of 5.0 does not preserve image files in 
/usr/local/groundwork/nagios/share/images/logos. To ensure that all files are backed up properly, follow 
the instructions in the installation that pertain to the 4.5 to 5.0 upgrade.
5.	Core Application Packages
Core Application Packages should never be uninstalled. If a core package is removed, the GWMON 
application has to be reinstalled. There is no "confirm" message presented when removing packages.
6.	Snmptt service
Before you perform an upgrade, please make sure that all instances of snmptt have been removed. Run 
the UNIX service scripts twice in order to stop the service, /etc/init.d/snmpttd stop.
7.	Advanced Reports – the Availability Reports
Initialization occurs only after the initial run of the dashboard_data_load.pl and dashboard_avail_load.pl 
scripts. The scripts run nightly as a cron job. The scripts can run manually.
	Release 5.0.2
1.	Upgrading does not retain installed packages
When migrating from 5.0 to 5.0, this process does not retain installed packages, potentially causing 
critical error dialogs. The known work around is to back up the previous the 
/usr/local/groundwork/guava/includes/config.inc.php file and restore it back after the new files are 
copied.

2.	Commit in Profile Tools sends large amounts of apache 2 errors
While trying to commit a number hosts using the Profile Tools application, a larger amount of apache2 
log errors are being generated. There is no impact to functionality of the product, just the unexpected 
impact of the log messages. These messages can be removed by using "vi" to edit them from the text 
log file.
3.	Monarch - Host Wizard: The template override values from a host profile are not applied. 
Host Wizard: The template override values from a host profile are not applied
The workaround is to apply the detail to the host(s) from the host profile after the host has been added.
4.	Nagios interface link to Hosts is really Service Detail, and Host link is missing.
		
© GroundWork Open Source, Inc.		Page 1 of 6
