.. Copyright 2016-2017 GroundWork Open Source, Inc. (GroundWork)
   All rights reserved. This program is free software; you can redistribute
   it and/or modify it under the terms of the GNU General Public License
   version 2 as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

.. include:: definitions.txt

Installation Guide
==================

Prerequisites
-------------

This guide assumes that you have
`installed Fuel <https://docs.mirantis.com/openstack/fuel/fuel-8.0/quickstart-guide.html>`_
and all the nodes of your future environment are discovered and functional.

Obtaining the Fuel plugin for GroundWork Monitor
------------------------------------------------

The Fuel plugin for GroundWork Monitor may
be obtained from the `Mirantis Fuel Plugins Catalog
<https://www.mirantis.com/validated-solution-integrations/fuel-plugins/>`_.
It does not come already bundled with the GroundWork installer.
You must obtain the GroundWork installer directly from
`GroundWork <https://www.gwos.com>`_.

Installing the Fuel plugin for GroundWork Monitor
-------------------------------------------------

#.  Copy the Fuel plugin RPM you obtained in the previous section
    to the Fuel Master node:

    .. parsed-literal::

	scp |groundwork_rpm| *FuelMasterHostname*:/tmp

#.  Copy the GroundWork installer you obtained in the previous
    section to the Fuel Master node.  The installer is large,
    and will require 1GB of space to be available in whatever disk
    partition you choose, before you make this copy.

    The ``tmp`` directory on the Fuel Master must be used as the
    logical location of the copy, as that is where the Fuel plugin
    will look for it during the installation process.  However,
    it is not necessary for the file to physically reside there.
    You may instead create a symlink in the ``/tmp`` directory of
    exactly the same name as the GroundWork installer, pointing
    to wherever you actually drop it on your Fuel Master machine.
    Here in our example, we just show parking the file directly in
    the ``/tmp`` directory.

    .. parsed-literal::

	scp |groundwork_installer| *FuelMasterHostname*:/tmp

#.  Log into the Fuel Master node as the ``root`` user and install
    the plugin:

    .. parsed-literal::

	ssh -l root *FuelMasterHostname*
	fuel plugins --install /tmp/|groundwork_rpm|

    You should get the following trailing output (wrapped here for
    easier viewing):

    .. parsed-literal::

	Complete!
	Plugin /tmp/|groundwork_rpm|
	    was successfully installed.

#.  You can verify that the plugin is installed by running this
    command on the Fuel Master:

    .. parsed-literal::

	fuel plugins --list |space|

    Alternatively, you can visit the Plugins page in the Fuel Web
    UI, and see that the plugin is present.

    .. image:: images/InstalledPlugins.png
       :width: 80%

Configuring the Fuel plugin for GroundWork Monitor
--------------------------------------------------

A Fuel plugin is enabled and configured separately for each Fuel
environment.  We do not explain here the process for setting up
a Fuel environment; we assume that you will use other sources of
information for those steps.  The discussion here is restricted to
getting the Fuel plugin enabled for deployment within an existing
Fuel environment.

#.  Navigate to **Settings** > **Other**, and activate the Fuel
    plugin for GroundWork Monitor by clicking the checkbox shown.
    The plugin currently takes no configuration values, so none are
    shown in this screen other than selection of the plugin version.

    .. image:: images/FuelPlugin.png
       :width: 80%

    .. raw:: html

	<br>
	<br>

#.    Click on **Save Settings**.

Deploying the Fuel plugin for GroundWork Monitor
------------------------------------------------

The following steps will deploy the Fuel plugin for GroundWork
Monitor to a chosen node within your OpenStack environment.

#.  Visit the **Nodes** tab in the Fuel UI for your chosen environment.

#.  Click the **+ Add Nodes** button at the top of the page.

#.  Select the **Monitoring - GroundWork** role.  If this node
    you are about to deploy to has never been configured before,
    the **Operating System** role also makes sense now.

#.  Select a single Discovered (i.e., uncommitted) node at the
    bottom of the page, on which to install the software using the
    **Monitoring - GroundWork** role.

#.  Ensure that this new node has sufficient resources allocated
    (CPU, RAM, Disk).  It must at least meet the minimum
    :doc:`system_requirements`.  If necessary, cancel the action
    in the current page, make adjustments elsewhere as needed to
    configure such a node, then come back to this page and run
    through these steps again.

#.  Click the **Apply Changes** button at the top of the page.

#.  Visit the **Dashboard** tab in the Fuel UI.

#.  Click the **Deploy Changes** button to force your pending Fuel
    configuration changes out to the nodes.

#.  Click the **Deploy** button in the pop-up dialog box.

#.  Wait until the deployment is complete, and check that you see
    Success declared.  This may take some time.

After all deployment is finished, the GroundWork Web UI can be
accessed at the hostname or IP address for the node you have deployed
it on.  For details and troubleshooting, see :ref:`logging-in`.

.. image:: images/GroundWorkLoginSplashScreen.png
   :width: 60%

.. _configuring-groundwork-monitor:

Configuring GroundWork Monitor
------------------------------

Deployment is just the first step in having a working, useful GroundWork Monitor setup.

#.  Immediately after deployment, there are a number of steps you
    should take to check your setup and secure the installation.
    The overall process is described in `Initial System Setup
    <https://kb.groundworkopensource.com/display/DOC71/Initial+System+Setup>`_.

#.  In particular, there are a number of places where you should
    modify various passwords in the deployed software so they
    are site-specific, not the generic passwords shipped with
    the software.  See the following documents for details:

    *   `Changing default passwords
	<https://kb.groundworkopensource.com/display/DOC71/Initial+System+Setup#InitialSystemSetup-2.1ChangeDefaultPasswords>`_.

    *   `Default login information
	<https://kb.groundworkopensource.com/display/SUPPORT/Installing+or+Upgrading+to+GroundWork+Monitor+7.1.1#InstallingorUpgradingtoGroundWorkMonitor7.1.1-DefaultLoginInformation>`_.

    *   `How to change the PostgreSQL database administrative password
	<https://kb.groundworkopensource.com/display/DOC71/How+to+reset+PostgreSQL+password>`_.

    *	`An overview of the primary user
	accounts and their respective permissions
	<https://kb.groundworkopensource.com/display/DOC71/About+System+Administration>`_.

    *	The following document might not be necessary
	for initial setup, but you should be aware
	of it for possible later reference.  `How to
	integrate with AD and LDAP for user authentication
	<https://kb.groundworkopensource.com/display/DOC71/How+to+AD+and+LDAP+configuration>`_.

    *   `How to change a user's password
	<https://kb.groundworkopensource.com/display/DOC71/How+to+users#Howtousers-3.0HowToChangeaUser%27sPassword>`_.

    *   `Password maintenance
	<https://kb.groundworkopensource.com/display/DOC71/GroundWork+License#GroundWorkLicense-2.0PasswordMaintenance>`_.

    *	This capability is not likely to apply to many sites,
	but you should be aware of it nonetheless if you wish to
	change the default ``nagiosadmin`` password along with all
	the other default passwords.  `How to configure access to Nagios WAP
	<https://kb.groundworkopensource.com/display/DOC71/How+to+access+to+Nagios+WAP>`_.

    *	Finally, the issues described in the following section
	are not directly related to an initial install, but
	may be important to know about in the same general
	realm of password control.  `Post-upgrade tasks
	<https://kb.groundworkopensource.com/display/SUPPORT/Installing+or+Upgrading+to+GroundWork+Monitor+7.1.1#InstallingorUpgradingtoGroundWorkMonitor7.1.1-PostUpgradeTasks>`_.

#.  You will probably also want to establish a password for the Linux
    ``nagios`` user account on the GroundWork server.  This account
    is used for most administration tasks.  It is created by the
    GroundWork installer, but with no default password.

#.  You will need to tell GroundWork Monitor about
    the computing resources you wish to monitor.
    See :ref:`configuring-monitoring-data` for details.
