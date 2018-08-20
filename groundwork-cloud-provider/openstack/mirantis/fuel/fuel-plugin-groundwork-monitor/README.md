Fuel plugin for GroundWork Monitor
==================================

The Fuel plugin for GroundWork Monitor is a tool that allows
a GroundWork Monitor instance to be conveniently deployed in a
Mirantis OpenStack environment.

Compatible versions:

- Mirantis Fuel 8.0 (available at https://www.mirantis.com/)
- GroundWork Monitor 7.1.0 (available at http://www.gwos.com/)


How to build this plugin
------------------------

You will need a development machine on which you can install the
various development-tools packages that are required for the build.
An Ubuntu 16.04 setup is recommended for this purpose, but a CentOS
6.7 machine has been shown to work as well.  Specifics of the
development tools used in these two contexts are listed here in the
`README.md` file in the plugin source code distribution.

* Install the Mirantis fuel plugin builder (`fpb`) on your development
  machine.

    - If you are building under Ubuntu 16.04, install the following
      packages from the Ubuntu repositories:

        ```
        make
        createrepo
        rpm
        dpkg-dev
        git
        python-pip
        sphinx-common
        alien          (optional; only for local development testing)
        fakeroot       (optional; only for local development testing)
        ```

      If you wish to make a PDF form of the documentation for this
      plugin, you will also need at least the following packages
      as well.  Note that in total, these packages are quite large
      (about 20 MB of direct download in total as of this writing,
      plus whatever other dependencies they pull in), and will take
      awhile to download to your machine.

        ```
        texlive-latex-recommended
        texlive-latex-extra
        texlive-fonts-recommended
        ```

    - If you are building under CentOS 6.7, install the following
      packages from the CentOS repositories:

        ```
        make
        createrepo
        rpm
        rpm-build
        git
        python-sphinx
        python-argparse
        texlive-latex
        # epel-release is needed before installing dpkg-dev
        # epel-release is needed before installing python-pip
        epel-release
        dpkg-dev
        python-pip
        # The sphinx-php package is apparently no longer available.
        # I don't know why.  It seems to no longer be required.
        sphinx-php
        ```

    - Run as the development user (not as `root`):

        ```
        sudo pip install fuel-plugin-builder
        cd
        git clone https://github.com/openstack/fuel-plugins
        cd fuel-plugins
        # This following specific commit is needed to suppress
        # requiring the obsolete tasks.yaml file in the build.
        git checkout a22bc32
        sudo python setup.py install
        ```

* Clone the plugin repo to your local development machine, if you
  have not already done so:

    ```
    cd
    git clone https://github.com/gwos/fuel-plugin-groundwork-monitor
    ```

* Obtain the GroundWork Monitor BitRock installer from GroundWork,
  Inc. (http://gwos.com), if you have not already done so, after
  agreeing to the appropriate End-User License Agreement.  It will
  be a file named something like:

    ```
    groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run
    ```

* Enter the top-level plugin source-code directory, where you will
  remain for the rest of these steps.

    ```
    cd fuel-plugin-groundwork-monitor
    ```

* Adjust the `GENUINE_FILENAME` value in the top-level `Makefile`
  of the plugin source code, to reflect the exact name of the
  GroundWork Monitor BitRock installer you just obtained.

* Place the GroundWork Monitor BitRock installer into the
  `deployment_scripts/` subdirectory.  Ordinarily, it must reside
  there during the build process, but it will not end up as part
  of the constructed RPM.

  If you do not copy the GroundWork Monitor installer into the
  `deployment_scripts/` subdirectory, you can still make the plugin.
  But then it will instead only run a very simple emulation of the
  installer, for simple testing of the rest of the plugin.

* General advice:  you can type `make` at the top level to see a
  list of all useful make targets.

* Calculate the proper checksums for validating the Fuel plugin:

    ```
    make checksums
    ```

* Take the lines printed out by the previous step, and replace the
  corresponding checksum-value lines in the Makefile.

* Create the plugin RPM file:

    ```
    make clean plugin
    ```

* Check for the plugin RPM file in the current directory:

    ```
    fuel-plugin-groundwork-monitor-7.1-7.1.0-1.noarch.rpm
    ```


How to manage this plugin
-------------------------

* The plugin gets installed on the Fuel Master machine, and it then
  gets deployed on other machines in the OpenStack environment.
  You must transfer the plugin RPM file to the Fuel Master machine
  before you can install the plugin there.  Typically, the RPM file
  is just placed in the `/tmp` directory, since you won't need this
  RPM again once it is installed.

* If you wish the GroundWork Monitor BitRock installer to be
  available to the plugin when it is deployed to OpenStack nodes, you
  must next place it into the `/tmp` directory on the Fuel Master.
  Once the Fuel plugin is installed on the Fuel Master, you can
  remove this temporary copy in the `/tmp` directory.

* The following commands are useful in managing the plugin on the
  Fuel Master machine, assuming the RPM has been parked in the
  `/tmp` directory on that machine:

    ```
    fuel plugins --install /tmp/fuel-plugin-groundwork-monitor-7.1-7.1.0-1.noarch.rpm
    fuel plugins --list
    fuel plugins --remove fuel-plugin-groundwork-monitor==7.1.0
    ```

  In order to apply the plugin to a node, it must first be installed
  on the Fuel Master, then enabled by checking its box in the Plugins
  screen in the Fuel Web UI.  In order to remove the plugin from the
  Fuel Master, you must have no nodes in your environment which are
  currently using it, and the plugin must already be disabled (that
  is, unchecked in the Plugins screen in the Fuel Web UI).

  Note that GroundWork Monitor setup contains a LOT of configuration
  data, which over time can represent tens to hundreds of hours
  of tuning for your infrastructure.  It can also contain a lot of
  valuable historical operational data for the resources it monitors.
  Bear those facts in mind when you want to remove this Fuel plugin:
  that configuration is precious cargo.  Before you delete any such
  node, BACK UP THE GROUNDWORK SYSTEM(S) FIRST!


Documentation
-------------

Full documentation on the plugin is available within the Fuel plugin
code.  It is formatted in HTML automatically, with appropriate
field-value substitutions, as part of building the plugin, and
appears in the `doc/source/_build/html/` subdirectory.

You can also create a PDF of the documentation, by running one `make`
command at the top level:

    ```
    cd fuel-plugin-groundwork-monitor
    make pdf
    ```

The resulting `fuel-plugin-groundwork-monitor.pdf` file will appear
in the top-level directory.


License
-------

The Fuel plugin code itself is open-source.  The GroundWork Monitor
software that it deploys contains commercially licensed components.
See http://gwos.com for full details.


Copyright
---------

Copyright 2016-2017 GroundWork Open Source, Inc. (GroundWork)
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

