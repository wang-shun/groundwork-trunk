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


Introduction
============

This document contains instructions for installing and configuring
the GroundWork Monitor plugin for Fuel.  By itself, it does not cover
the more-extensive process of configuring GroundWork Monitor itself,
once it has been deployed on a node.

Key terms, acronyms, and abbreviations
--------------------------------------

+--------------------+--------------------------------------------------------------+
| GroundWork Monitor | GroundWork Monitor provides system, device, and application  |
|                    | monitoring for many types of computer resources.             |
+--------------------+--------------------------------------------------------------+
| MOS                | Mirantis OpenStack                                           |
+--------------------+--------------------------------------------------------------+

Overview
--------

The GroundWork Monitor plugin for Fuel provides the functionality
to add GroundWork Monitor to Mirantis OpenStack as a monitoring
backend option, using the Fuel Web UI in a user-friendly manner.
GroundWork Monitor is a monitoring solution that can be used to
watch over many different types of resources, including computer
hardware, network devices, application presence and responsiveness,
and so forth.

GroundWork Monitor features:

*   Unified monitoring collects data from multiple sources and
    integrates it for display.

*   Multiple types of popular cloud infrastructure are supported
    for automated monitoring configuration.

*   Monitoring can scale to many thousands of nodes.

*   Analytic tools are both integrated directly into the product,
    and supported via data feeds to certain popular external tools.

This Fuel plugin is hot-pluggable, meaning it can be installed on
a deployed cluster (in ready state), not just on an undeployed
cluster.  Then to obtain a node running GroundWork Monitor, it
is only necessary to enable the plugin under the Settings tab for
the given environment in the Fuel Web UI, add a node with the new
GROUNDWORK_MONITOR role, and deploy changes.  More information on
the installation and configuration process is presented later in
this documentation.  Once the GroundWork Monitor software itself
is configured, the local monitoring setup on the GroundWork Monitor
machine will constitute precious cargo, and that node should never
be deleted capriciously.

Licensing information
---------------------

GroundWork Monitor is itself a commercial product; see the
`GroundWork website <https://www.gwos.com>`_
for licensing details.

+------------------------------------+-----------------+
| GroundWork Monitor                 | Commercial      |
+------------------------------------+-----------------+
| Fuel plugin for GroundWork Monitor | Apache 2.0      |
+------------------------------------+-----------------+

The GroundWork Monitor installation includes code from a
variety of projects.  For details on their respective licenses,
see the `What opensource licenses are used in GroundWork
<https://kb.groundworkopensource.com/display/SUPPORT/What+opensource+licenses+are+used+in+Groundwork>`_
page.
