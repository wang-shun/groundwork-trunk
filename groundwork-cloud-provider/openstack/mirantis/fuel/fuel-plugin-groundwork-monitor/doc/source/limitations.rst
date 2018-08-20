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


Limitations
===========

*   Deployment of a Remote PostgreSQL database is not supported
    by this Fuel plugin.  The working assumption is that you are
    installing a standalone monitoring server.

*   The GroundWork Monitor software is deployed using fixed
    standard administrative credentials, which are not settable
    during the install itself.  It is necessary to perform some
    manual post-install steps to establish site-local credentials
    for proper security.  See :ref:`configuring-groundwork-monitor`
    for details.

*   As installed by the Fuel plugin when it is deployed on a node,
    the GroundWork Monitor software is initially configured to
    only monitor the machine on which it runs.  Extending the
    setup to monitor a larger set of resources involves manual
    post-install steps.

*   The Fuel plugin can be used to install a child server,
    but it will not be configured as such immediately after the
    installation.  That is, manual configuration is required to
    make it interact as desired with a parent server.

*   This plugin is not equipped to handle
    an upgrade from a previous release of GroundWork
    Monitor.  See `Upgrading to 7.1.1 from a previous version
    <https://kb.groundworkopensource.com/display/SUPPORT/Installing+or+Upgrading+to+GroundWork+Monitor+7.1.1#InstallingorUpgradingtoGroundWorkMonitor7.1.1-Upgradeinstallation>`_
    for details of the procedures for upgrading to the current
    release.
