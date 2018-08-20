Readme rev 1.1	Current Release: GroundWork Monitor Open Source 5.0.2

GroundWork Monitor Open Source 5.0 is free software; you can redistribute it and/or modify it under the terms of the GNU 
General Public License as published by the Free Software Foundation; either version 2, or (at your option) any later version. 
GroundWork Monitor Open Source is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General 
Public License for more details.

Table of Contents
1	PURPOSE
2	BUG FIXES
3	KNOWN ISSUES
----------
1	PURPOSE
Important Note: The distributed Readme document should be reviewed prior to the installation or upgrade 
of GroundWork Monitor Open Source 5.0.

The purpose of this “Readme” document is to outline the bug fixes and known issues in the current release 
of the GroundWork Monitor Open Source 5.0 product. Additionally, the documents “Installation” and 
“Release Notes” outline system hardware requirements, pre-installation planning, and steps for a new 
installation or an upgrade; and new features in this release, respectively. All documents are distributed in .txt 
and .pdf formats.
----------
2	BUG FIXES
This section includes a description of the bugs fixed in each maintenance release of GroundWork Monitor 
Open Source 5.0. 
	Release 5.0.1
The following lists GWOS 4.5 issues fixed in this release of GroundWork Monitor Open Source.
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
10.	GWMPRO-359
Copyright notices for the Bookshelf documentation in GW Monitor should not be claiming copyright to 
the other open source projects MySQL, RRD, etc.
11.	GWMPRO-358
Name space problem when assigning hostgroup and contactgroup the same name.
12.	GWMPRO-350
hostgroup membership not correct.
13.	GWMPRO-339
Problem with Discover Host Wizard.
14.	GWMPRO-337
Configuration>Control>Setup>Max Tree Nodes is misleading and ignored.
15.	GWMPRO-333
Nagios load failed.
16.	GWMPRO-328
Delete service template not cleaning up contactgroup_assign table.
17.	GWMPRO-327
Some navigation drill downs revert to being unexpanded on refresh.
18.	GWMPRO-314
If you attach more than two escalation trees, only the first two show up in Configuration.
19.	GWMPRO-300
I exported a standard service profile, which included a service template. I made a change so that the 
service profile with this value was not imported back to Monarch.
20.	GWMPRO-278
Configuration>Commands>Modify>Command Name, When you click the "Test" button it does 
something backwards.
21.	GWMPRO-273
After removing a service check in Configuration with a commit, a user is still able to access the service 
in Performance tab.
22.	GWMPRO-271
Import fails after Add button pressed on a partial selection.
23.	GWMPRO-270
Wrong directory path listed for nmap_scan_one.pl. Script with EZ-Configuration Host Discovery.
24.	GWMPRO-264
The Control>Nagios Main Config>View Edit mouse-over definition for use_retained_scheduling 
information is incorrect.
25.	GWMPRO-262
Spelling typo under Configuration>Service Check sub tab.
26.	GWMPRO-250
Configuration>Discover> add a host, re-run Discover wizard, add same host, get MySQL error.
27.	GWMPRO-224
The notion of template values being inherited is not very clear in the Bookshelf documentation.
28.	GWMPRO-213
Add a scroll bar so that when assigning more than 9-10 packages to a role, all the tabs can be viewed 
on the screen.
29.	GWMPRO-201
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
4.	GWMON-820
Documentation error in upgrade section of the 5.0 Installation.txt.
5.	GWMON-813
Guava exception within bookshelf.
6.	GWMON-801 IE6
Monarch Escalation navigation pane don't show escalations and trees that were created.
7.	GWMON-785
Class 'LDAPAuthModule' not found in /usr/local/groundwork/guava/includes/guava.inc.php on line 2042.
8.	GWMON-769
Contact groups for host overrides not written to files.
9.	GWMON-766
Profile tools, import option died at /usr/local/groundwork/apache2/cgi-bin/monarch/importcfg.pl line 760, 
<IMPORTFILE> line 79.
10.	GWMON-758
Save as Profile on the host wizard does not save host wizard.
11.	GWMON-569
Guava re-draws menu items.
12.	GWMON-697
resource.cfg cannot be deployed from the build folder.
13.	GWMON-707
New host wizard>service profile doesn't apply service dependencies.
14.	GWMON-590
Host Delete Tool does not work for 192* hosts.
15.	GWMON-568
In EZ and Config no response given when host searched.
16.	GWMON-712
Cannot remove service escalation from host>service.
17.	GWMON-724
Successfully assign an escalation tree to a service, and then try to remove this tree by leaving the field 
for escalation tree blank. The escalation tree entry remains.
18.	GWMON-706
Spaces in Service Template names.
19.	GWMON-710
Delete host templates doesn't clean up contactgroup_assign table.
----------
3	KNOWN ISSUES
This section includes a description of the know issues in this release of GroundWork Monitor Open Source.
	Release 5.0.1
1.	IE 6 Compatibility Issues
Issues with IE6 error handling may cause the GWMON application to freeze. If this should occur kill and 
restart your browser and log back into the GWMON application.
2.	Application Timeout 
Leaving a session open for a long time, may cause a session time out. If this should occur logout and 
re-login to the GWMON application.
3.	Configuration EZ and Configuration
If auto-completion of the Host Search query fails to return a result, the search gives no response when a 
host search fails.
4.	Core Application Packages
Core Application Packages should never be uninstalled. If a core package is removed, the GWMON 
application has to be reinstalled. There is no "confirm" message presented when removing packages.
-----
	Release 5.0.2
1.	Monarch - Host Wizard: The template override values from a host profile are not applied. 
Host Wizard: The template override values from a host profile are not applied
The workaround is to apply the detail to the host(s) from the host profile after the host has been added.
		
© GroundWork Open Source, Inc.		Page 1 of 4
