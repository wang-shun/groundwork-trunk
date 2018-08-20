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


==================================================
Fuel Plugin for the GroundWork Monitor integration
==================================================

FIX MAJOR:  This section of the documentation must be overhauled.

Contrail plugin for Fuel provides the functionality to add Сontrail SDN for
Mirantis OpenStack as networking backend option.

It uses Fuel pluggable architecture.
The plugin must be compatible with the version 6.1 of Mirantis OpenStack and Contrail 2.0.22.
Only HA mode for OpenStack and Contrail controllers is supported.
It will be allowed to deploy an only Contrail Controller (for test-bed environments)
and add more controllers later (to enable true HA).
Contrail Load-Balancing-as-a-Service feature available through OpenStack Neutron API can be enabled.

Problem description
===================

Proposed change
===============

Implement a Fuel plugin which will deploy Contrail SDN controllers and configure
Mirantis OpenStack to use Contrail as Neutron plugin.

Alternatives
------------

It also might be implemented as a part of Fuel core, but we decided to make it as
a plugin for several reasons:

* Any new additional functionality makes a project and testing more difficult, which is an additional risk for the FUEL release.

Data model impact
-----------------

REST API impact
---------------

Upgrade impact
--------------

Fuel currently supports upgrading of Fuel Master node, so it is necessary to
install a new version of plugin which supports new Fuel release.

Security impact
---------------

Notifications impact
--------------------

Other end user impact
---------------------

Contrail plugin uses Fuel pluggable architecture.
After it is installed, the user can enable the plugin on the Setting tab of the Fuel web UI
and customize plugins settings.

Performance Impact
------------------

Other deployer impact
---------------------

Developer impact
----------------

Implementation
==============

Assignee(s)
-----------

Primary assignee:

- Oleksandr Martsyniuk <omartsyniuk@mirantis.com> - feature lead, developer

Other contributors:

- Vyacheslav Struk <vstruk@mirantis.com> - developer

Project manager:

- Andrian Noga <anoga@mirantis.com>

Quality assurance:

- Oleksandr Kosse <okosse@mirantis.com> - qa
- Iryna Vovk <ivovk@mirantis.com> - qa

Docs Lead:

- Irina Povolotskaya <ipovolotskaya@mirantis.com> - technical writer

Work Items
----------

* Create pre-dev environment and manually deploy Contrail software
* Create Fuel plugin bundle, which contains deployments scripts, puppet modules and metadata
* Implement puppet module with the following functions:

 - Contrail controllers deployment and initial configuration
 - Mirantis OpenStack controllers configuration
 - vRouter deployment on OpenStack compute nodes

* Test Contrail plugin
* Create Documentation


Dependencies
============

* Fuel 6.1 and higher
* Base-os node role
* Node reboot task
* Advanced networking in Fuel

Testing
=======

* Sanity checks including plugin build
* Syntax check
* Functional testing
* Non-functional testing (Destructive and Negative)

Documentation Impact
====================

* Deployment Guide (how to prepare an env for installation, how to install the plugin, how to deploy OpenStack env with the plugin)
* User Guide (which features the plugin provides, how to use them in the deployed OS env)
* Test Plan
* Test Report

References
==========

* Fuel Plug-in Guide http://docs.mirantis.com/openstack/fuel/fuel-6.0/plugin-dev.html
* Juniper Networks TechWiki > Documentation > Contrail http://techwiki.juniper.net/Documentation/Contrail
* Advanced Networking feature in Fuel https://blueprints.launchpad.net/fuel/+spec/advanced-networking

