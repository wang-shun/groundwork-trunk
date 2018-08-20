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


System Requirements
===================

This plugin has the following requirements for software and hardware:

+----------------------------+------------------------------------------------------------------+
| Fuel version               | 8.0                                                              |
+----------------------------+------------------------------------------------------------------+
| GroundWork Monitor version | |version|                                                        |
|                            |                                                                  |
|                            | The GroundWork Monitor installer package is not included in      |
|                            | the Fuel plugin itself.  It must be obtained separately from     |
|                            | GroundWork, and placed in the ``/tmp`` directory on the Fuel     |
|                            | Master before the plugin is installed.  A copy of the            |
|                            | GroundWork Monitor installer will then be cached along with      |
|                            | the plugin, and it will be run when the plugin is deployed       |
|                            | to a node.                                                       |
+----------------------------+------------------------------------------------------------------+
| Hardware                   | The items listed below specify minimum requirements.             |
|                            | Monitoring of ongoing activity at reasonable frequency is a      |
|                            | resource-intensive activity, so application to significant       |
|                            | infrastructure may require increasing these minimums             |
|                            | substantially to provide adequate performance.  Special          |
|                            | configuration of disk space across multiple spindles might also  |
|                            | be required to achieve acceptable monitoring performance.        |
+----------------------------+------------------------------------------------------------------+

Hardware requirements:

*   At least 1 additional server for GroundWork Monitor (to serve the monitoring role).  Large deployments might require additional child servers.

*   GroundWork recommends at least 4GB of memory.

*   GroundWork recommends at least 160GB of free disk space.

*   GroundWork recommends at least 2 CPUs, running at 3 GHz.

The table above represents just a basic overview.
See `System Requirements`_, `Installation Options`_, and `Remote Database`_ for a complete discussion of current requirements.

.. _System Requirements: https://kb.groundworkopensource.com/display/SUPPORT/System+Requirements+for+7.1.1

.. _Installation Options: https://kb.groundworkopensource.com/display/SUPPORT/Installation+Options+for+7.1.1

.. _Remote Database: https://kb.groundworkopensource.com/display/SUPPORT/Remote+Database+Installation+Instructions
