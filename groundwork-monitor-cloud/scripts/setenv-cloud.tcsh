# setenv-cloud.tcsh
# Set up the environment for using Amazon EC2 API tools with a specified cloud.
# This file must be sourced, not executed.

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

if ( $# != 1) then
    echo "usage:  setenv-cloud {region}"
else
    set region = "$1"

    # Access file placed under the credentials/ directory by the administrator
    # (generally via our configuration UI).
    set ACCESS_FILE = /usr/local/groundwork/cloud/credentials/$region/access.tcsh

    # The adjustments here are to fix a problem with PATH in the GW6.1.X nagios crontab.
    # The complexity here is to avoid altering $PATH if it already contains the path
    # component of interest, so this script can be run idempotently.
    if ( `/usr/bin/expr match ":${PATH}:" ".*:/bin:"     || /bin/true` == 0 ) set path = ($path /bin)
    if ( `/usr/bin/expr match ":${PATH}:" ".*:/usr/bin:" || /bin/true` == 0 ) set path = ($path /usr/bin)

    if ( -f $ACCESS_FILE ) then
	source $ACCESS_FILE
    else
	echo 'ERROR:  "'$region'" is not a known region.'
	echo "        ($ACCESS_FILE does not exist)"
    endif

    set EC2_API_TOOLS_DIR = /usr/local/groundwork/cloud/ec2-api-tools-{EC2_API_TOOLS_RELEASE}

    if ( -d $EC2_API_TOOLS_DIR ) then
	setenv EC2_HOME $EC2_API_TOOLS_DIR
	if ( `expr match ":${PATH}:" ".*:$EC2_HOME/bin:" || true` == 0 ) set path = ($path $EC2_HOME/bin)
    endif
endif
