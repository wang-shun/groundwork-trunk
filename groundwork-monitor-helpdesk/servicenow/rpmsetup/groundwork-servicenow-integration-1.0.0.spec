# This is the groundwork-servicenow-integration spec file needed to construct
# the Groundwork Monitor ServiceNow Integration RPM.

# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# The construction of this spec file is a bit unusual in that it is
# intended to be self-contained; that is, we don't want to depend on
# the particular user's dot-files (~/.rpmrc or ~/.rpmmacros) and build
# structure (pre-established ~/rpmbuild/*/ directories).  Also, we don't
# start with a source RPM or tarball.  Rather, we assume the source code
# is already splayed out, and all we're trying to do at this stage is to
# construct the RPM(s).

# ================================================================

# TO DO:
# (*) ...

# ================================================================

# Note:  If you need to debug the specfile or the associated macro files,
# it may help to invoke the dump macro.  Uncomment the following line and
# change the at-sign to a percent sign (rpmbuild invokes macros even when
# they are embedded in comments, so we couldn't just leave a commented-out,
# directly-useable invocation here).  That will print out all the macro
# values for your inspection.
# @dump

# I'm not sure yet what this is for, except that it suppresses errors about
# not finding a
# %define debug_package	%{nil}

# This setting allows us to redefine the dependency analyzer below, so we can
# filter out some "requires" dependencies on the GroundWork Monitor RPMs that
# are not yet properly available as "provides" capabilities in those RPMs.
#
# %define _use_internal_dependency_generator     0
#
# Unfortunately, this control flag has contradictory effects, depending on
# how it is set, and here we logically need to invoke both of these mutually
# exclusive cases:
#
# defining _use_internal_dependency_generator as 0 means:  __find_requires works, __perl_requires does not
# defining _use_internal_dependency_generator as 1 means:  __perl_requires works, __find_requires does not
#
# Fortunately, the __find_requires processing can handle everything that the
# __perl_requires processing could do, so there is no loss of functionality.
# All we need do is to apply the same editing patterns using __find_requires,
# that we would have applied instead using __perl_requires, and we can still
# edit out all the dependencies we need to eliminate.

# ================================================================

%define	name		groundwork-servicenow-integration
%define	major_release	1
%define	minor_release	0
%define	patch_release	0
%define	version		%{major_release}.%{minor_release}.%{patch_release}

# We would disable the dist definition by replacing % with # to comment out this next line, if this were a noarch RPM.
# (A define is recognized by rpmbuild even within a comment, if it is preceded by a percent sign.)
%define	dist		%(if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)

%define	gwpath		/usr/local/groundwork
%define	servicenowpath	%{gwpath}/servicenow

# ================================================================

# Note:	The "Source" specification is only used to build a source RPM.  In the code
#	below, we have turned off its use in building a binary RPM (%setup -T).  By
#	commenting it out, completely, it's not even accessed for a source RPM.
#	FIX MINOR:  But then, we're getting only the spec file included in the
#       source RPM, not the rest of the code.
# Note:	Buildroot becomes %{buildroot} for referencing elsewhere in this spec file.

Summary: GroundWork Monitor ServiceNow Integration Software
License: All rights reserved.  Use is subject to GroundWork commercial license terms.
Group: Applications/Monitoring
Name: %{name}
Prefix: %{gwpath}
Release: %(svn info -r HEAD | fgrep Revision | gawk '{print $2}')%{?dist}
# Source: %{name}-%{version}.tar.gz
Version: %{version}
# If we had any compiled libraries or programs in this RPM,
# we would comment out the next (BuildArchitectures) line.
BuildArchitectures: noarch
Buildroot: %{_installroot}
Packager: GroundWork <support@gwos.com>
Vendor: GroundWork Open Source, Inc.
URL:  http://www.groundworkopensource.com/

# FIX MINOR:  We should put the BuildPrereq back, once we disable the
# "rpmbuild --dbpath" option in the makefile.
# BuildPreReq: patch

# We require no RPM pre-requisites for the groundwork-servicenow-integration RPM
# because it is intended to overlay an existing GroundWork Monitor product
# which has been installed by the Bitrock installer instead of via RPMs.
# PreReq:
# AutoReq: no
# AutoProv: no

# ================================================================

%description
This software extends the base GroundWork Monitor product by
supplying software to integrate with ServiceNow helpdesk ticketing.

# ================================================================

%prep

%make_all_rpm_build_dirs

# WARNING:  The "%setup -D" option is critical here, so we don't recursively
# delete the entire source file tree before the build can even begin.
%setup -D -T -n %(echo $PWD)

# ================================================================

# Here we filter out unwanted Requires, which are known to be provided in the
# base GroundWork Monitor packages which would be part of our own package's
# PreReq if we still depended on those packages being provided as RPMs.
# So we're working around a deficiency, by actually defeating the dependency
# analysis at install time.

# In the present case, we need to handle both certain Perl modules, and
# some binary-program dependencies for programs of a different architecture
# that are included in this package for distribution to other run-time
# machines but will not actually be run on the machine where this package
# is installed.
#
# Well, that would be the case, except that this alternate __find_requires
# for some reason already suppresses the library dependencies that are of
# concern, so as long as we use __find_requires instead of _perl_requires,
# we need only pay attention to the Perl-module dependency edits here.

# The script below references the original __find_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_find_requires %{_tmppath}/%{name}-%{version}-find_requires

cat << \EOF > %{edit_find_requires}
#!/bin/sh
%{__find_requires} $* |\
    sed -e '/\/usr\/local\/groundwork\/perl\/bin\/perl/d'
EOF
chmod +x %{edit_find_requires}

# Note:  The __find_requires macro is only used if we set
# _use_internal_dependency_generator to zero above.  Otherwise,
# this redefinition wouldn't make any difference.
%define __find_requires %{edit_find_requires}

# THE FOLLOWING IS NOT USED, because we have enabled __find_requires above.

# The script below references the original __perl_requires macro,
# so we cannot redefine it here before creating that script.
%define edit_perl_requires %{_tmppath}/%{name}-%{version}-requires

cat << \EOF > %{edit_perl_requires}
#!/bin/sh
%{__perl_requires} $* |\
    sed -e '/perl(TypedConfig)/d'		\
	-e '/perl(utils)/d'
EOF
chmod +x %{edit_perl_requires}

%define __perl_requires %{edit_perl_requires}

# ================================================================

%build

# make rpmclean
# make rpms

# ================================================================

%install

# This action is already encoded as a dependency for the "make rpms"
# target in the makefile.
# make servicenow_integration_install

# ================================================================

%clean

# make buildclean

# ================================================================

%pre
#!/bin/bash -e
# The bash -e option is needed so we intentionally fail if we have
# difficulty creating the temporary database credentials file.

PATH=/bin:/usr/bin

# First, we check to make sure that the install/upgrade instructions were
# followed -- namely, that certain environment variables are defined and
# contain sensible values:
#     MYSQL_HOST (optional; will default later on to "localhost")
#     MYSQL_PORT (not yet supported here, but it ought to be)
#     MYSQL_PASS (required; the MySQL root account password on MYSQL_HOST)

# We must (unfortunately) allow an empty password here, which is why this
# test is a bit complicated.  We also need to cope with the stupid exit
# code returned by egrep if it doesn't find what it's looking for.
if [ `env | egrep -c '^MYSQL_PASS=' || true` -ne 1 ]; then
    echo "FATAL:  The MYSQL_PASS environment variable is not defined."
    echo "        See the SERVICENOW_INTEGRATION_INSTALL_NOTES file."
    exit 1
fi

default=""
if [ -z "$MYSQL_HOST" ]; then
    default=" (defaulted to localhost)"
fi

hostname="${MYSQL_HOST:-localhost}"
database="mysql"
username="root"
password="$MYSQL_PASS"

# Security check:  Make sure $hostname only contains a valid hostname,
# before we go blindly substituting it into a command line.
# Reference:  http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names
# * FQDN max length:  255 characters
# * FQDN structure:  a series of FQDN components, separated by single "." characters
# * FQDN component length:  1 to 63 characters
# * FQDN component character set:  [-a-zA-Z0-9]
# * FQDN component structure:  cannot start or end with a hyphen

/usr/local/groundwork/perl/bin/perl -e '
sub is_valid_hostname {
    my $name = shift;
    return 0 if not defined $name;
    my $name_length = length $name;
    return 0 if $name_length < 1 || $name_length > 255;
    foreach my $part ( split(/\./, $name, -1) ) {
	my $part_length = length $part;
	return 0 if $part_length < 1 || $part_length > 63;
	return 0 if $part =~ /[^-a-zA-Z0-9]/;
	return 0 if $part =~ /^-/;
	return 0 if $part =~ /-$/;
    }
    return 1;
}

# We need to turn success upside down for the exit code to properly reflect it.
exit ! is_valid_hostname $ARGV[0];
' "$hostname"

if [ $? -ne 0 ]; then
    echo "ERROR:  The MYSQL_HOST environment variable does not contain"
    echo "        a legal hostname.  Execution is being aborted."
    exit 1;
fi

# This selection ought to be configured via the MYSQL_PORT environment
# variable (both here and in the db.properties file), but until it is,
# we need to compute this ourselves.  The mysql client program is
# configured to do this for us in its my.cnf file, but we cannot use
# that because we need to use the --defaults-extra-file argument,
# which is not supported by the Bitrock "mysql" wrapper.
if [ "$hostname" = 'localhost' ]; then 
    port_or_sock="--socket=/usr/local/groundwork/mysql/tmp/mysql.sock"
else
    port_or_sock="--port=3306"
fi

# FIX LATER:  there are improved ways to name a temporary file
# that provide even better security
my_local_cnf="/usr/local/groundwork/tmp/my_local.cnf.$$"
trap "/bin/rm -f $my_local_cnf;" EXIT
/bin/rm -f $my_local_cnf
touch $my_local_cnf
chmod 600 $my_local_cnf
cat > $my_local_cnf << eof
[mysql]
user=$username
password=$password
eof

gwpath=/usr/local/groundwork
mysql=$gwpath/mysql/bin/mysql.bin

# It can take over 3 minutes to attempt to contact a non-existent
# remote MySQL server, so we'd better warn the user what's going on.
echo "Attempting to contact MySQL on host '$hostname' ..."
$mysql --defaults-extra-file=$my_local_cnf -h $hostname $port_or_sock -s $database -e ''
if [ $? -ne 0 ]; then
    echo "FATAL:  The MYSQL_HOST environment variable $default"
    echo "        does not refer to an active MySQL server, or"
    echo "        the MYSQL_PASS password is incorrect."
    echo "        See the SERVICENOW_INTEGRATION_INSTALL_NOTES file."
    exit 1
fi
# And now we announce the end of the possible wait, so it's
# clear that any further waiting is due to some other cause.
echo "The MySQL access credentials check out; will proceed with installation."

# FIX THIS:
# $1 will be a number when we install an RPM, but will be a string when this
# is converted to a .deb file on Ubuntu using alien.  Possible values are:
#
#     abort-deconfigure
#     abort-install
#     abort-remove
#     abort-upgrade
#     configure
#     deconfigure
#     disappear
#     failed-upgrade
#     install
#     purge
#     remove
#     upgrade
#
# See http://www.debian.org/doc/debian-policy/ch-maintainerscripts.html for details.
if [ "$1" = 1 ]; then

    # First install.

    : do nothing

    # echo "INFO:  This installation of %{name} is considered a first install;"
    # echo "       no attempt will be made to kill a previous ServiceNow Integration."

fi

if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  We ought to shut down the ServiceNow Integration Software if it's running,
    # so the upgrade can go smoothly.  Make sure we do that in a way that won't cause
    # it to be started up again immediately by some daemon-keeper process, and that
    # will be easy to reverse later on.

    : do nothing

fi

# ================================================================

%post
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

newline='
'

if [ "$1" = 1 -o "$1" = "configure" -o "$1" = "abort-remove" ]; then

    # First install, or aborted remove (under Unbuntu).

    # Everything we want to do here is fairly independent of the conditions we test for above,
    # and we want to ensure that this scripting attempts to repair any damage it can during an
    # upgrade, so all the system changes here are run idempotently outside of those tests.
    : do nothing

else

    # FIX LATER:  should we be testing this condition here?
    # FIX THIS:  How do we make a portable test here, to support Unbuntu as well?
    # if [ `expr match "$1" '[0-9]*'` -gt 0 ] && [ "$1" -gt 1 ]; then

    # Upgrading.

    # FIX LATER:  Shut down any old processes associated with the ServiceNow Integration software.
    # (But why would we do so only now?)

    : do nothing

fi

# While developing this specfile, we tried to install the console-admin-config.xml
# directly in place using a %%config(noreplace) directive, overwriting the existing file
# from the GWMEE base product.  That approach evoked a variety of complex interactions
# with .rpmnew and .rpmsave files that appear at certain times, along with undesirable
# deletion of the base file under some conditions.  So to avoid unexpected surprises that
# might occur under conditions we forgot to test, we are instead using RPM to install the
# file under our own extension-specific directory and then manually copying it into place.

# Regardless of whether this is a fresh install or an upgrade, back up the current copy of
# console-admin-config.xml, which we will replace with our own copy.

timestamp=`date +"%%Y-%%m-%%d.%%H_%%M_%%S"`
old_file=%{gwpath}/config/console-admin-config.xml
sav_file=%{gwpath}/config/console-admin-config.xml.$timestamp.pre_servicenow

if [ -f $old_file ]; then
    cp -p $old_file $sav_file
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not back up the console-admin-config.xml file." 1>&2
	exit 1
    fi
fi

# Now do the replacement.

rpm_file=%{servicenowpath}/config/console-admin-config.xml

if [ -f $rpm_file ]; then
    cp -p $rpm_file $old_file
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not install the console-admin-config.xml file." 1>&2
	exit 1
    fi
else
    echo "FATAL:  ServiceNow Integration RPM could not find the new console-admin-config.xml file." 1>&2
    exit 1
fi

# Make sure the ServiceNow Integration nagios cron job is in place.  We carry out this action
# in small steps to minimize the likelihood of leaving the system in a mangled state,
# having corrupted the existing content and not having installed the new content.
old_crontab=`crontab -l -u nagios`
if [ $? -ne 0 ]; then
    echo "FATAL:  ServiceNow Integration RPM could not fetch the old nagios crontab" 1>&2
    exit 1
fi
if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/twoway_helpdesk.pl '`" -eq 0 ]; then
    # The cron job is not installed.  Do so now.  We don't bother to try to delete
    # an existing cron job if it is currently commented out; we just add a new one.
    new_cron_job='*/5 * * * * /usr/local/groundwork/servicenow/bin/twoway_helpdesk.pl > /dev/null 2>&1'
    echo "$old_crontab$newline$new_cron_job" | crontab -u nagios -
    if [ $? -ne 0 ]; then
	echo "FATAL:  ServiceNow Integration RPM could not install its nagios cron job" 1>&2
	exit 1
    fi
else
    echo "NOTICE:  The ServiceNow Integration nagios cron job looks like it was already in place."
fi

# Here we invoke scripting to adjust a property in Foundation.
# gwservices must be bounced to pick up the change, but that will
# be done just below when we make changes to the databases.

# We need to modify /usr/local/groundwork/config/foundation.properties to inject
# the fas.executor.interrupt property if it is not already present there.
#
#	# Interrupt the action wait thread after this long, in seconds
#	fas.executor.interrupt = 20
#
# If possible, we put those lines after these lines:
#
#	# Keep Thread Alive In Seconds
#	fas.executor.keep.alive = 30
#
# Otherwise, we just append the new lines to the end of the file.
#
# To avoid race conditions with other processes reading this file while we are in
# the middle of updating it, we do the atomic shuffle to update this file, similar
# to what we do with cron jobs, except with a rename (mv) in this case.  Thus a
# reading process will either see a complete and consistent copy of the old file
# or a complete and consistent copy of the new file.

new_property='\n# Interrupt the action wait thread after this long, in seconds\nfas.executor.interrupt = 20'

timestamp=`date +"%%Y-%%m-%%d.%%H_%%M_%%S"`
old_file=/usr/local/groundwork/config/foundation.properties
# FIX LATER:  there are improved ways to name a temporary file
# that provide even better security
new_file=/usr/local/groundwork/config/foundation.properties.new.$$
sav_file=/usr/local/groundwork/config/foundation.properties.$timestamp.pre_servicenow

old_properties=`cat $old_file`
if [ $? -ne 0 ]; then 
    echo "FATAL:  ServiceNow Integration RPM could not read the old foundation.properties" 1>&2
    exit 1  
fi
if [ "`echo \"$old_properties\" | sed -e 's/#.*//' | fgrep -c 'fas.executor.interrupt'`" -eq 0 ]; then 
    # The new property is not installed.  Do so now.  We don't bother to try to delete
    # an existing property if it is currently commented out; we just add a new one.

    # First, prepare the new file by copying the ownership and permissions.
    cp -p $old_file $new_file
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not copy the foundation.properties file." 1>&2
	exit 1
    fi
    if [ "`echo \"$old_properties\" | sed -e 's/#.*//' | fgrep -c 'fas.executor.keep.alive'`" -eq 0 ]; then 
	# Just append to the file, as there is no obvious place to put the new property.
	(echo "$old_properties"; echo "$new_property\\n" | sed -e 's/\\n/\n/g') > $new_file
    else    
	# Insert the new property immediately after the designated existing property.
	echo "$old_properties" | sed -e "/fas.executor.keep.alive/a\\$new_property" > $new_file
    fi
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not create a revised foundation.properties file." 1>&2
	exit 1
    fi
    cp -p $old_file $sav_file
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not back up the foundation.properties file." 1>&2
	exit 1
    fi
    mv $new_file $old_file
    if [ $? -ne 0 ]; then 
	echo "FATAL:  ServiceNow Integration RPM could not install a new foundation.properties file." 1>&2
	exit 1
    fi
else
    echo "NOTICE:  The ServiceNow Integration special Foundation property"
    echo "    (fas.executor.interrupt) looks like it was already in place." 
fi

# Put the expected database objects in place, whether this is a fresh install or an
# upgrade.  (The prepare_databases_for_helpdesk script is idempotent, so that's not
# an issue.)  We could have included this script right here into the specfile (using
# %include), and not bothered to ship the script as a separate file, but doing so
# allows us to re-run it later on if that should prove necessary or convenient.
#
# This script depends on possibly having the MYSQL_HOST environment variable defined
# (inside the prepare_databases_for_helpdesk script, it will default to localhost),
# and definitely having the MYSQL_PASS environment variable defined (representing
# the MySQL root-account password on the MYSQL_HOST).  In theory, we might also want
# the MYSQL_PORT environment variable defined (it will default to port 3306), but in
# practice we don't currently allow the port to be configurable.  These settings were
# checked above during the pre-install script, to guarantee that we don't get this
# far without having them available.
#
# The -e option says the script should insist on drawing MYSQL_HOST and MYSQL_PASS
# from environment variables and failing if the script doesn't get what it needs via
# that route, rather than prompting the user for them if they are not so provided.
%{servicenowpath}/db/prepare_databases_for_helpdesk -e
if [ $? -ne 0 ]; then
    # Although correct operation of the ServiceNow Integration depends on the
    # database content changes made by this script, this is not a fatal error,
    # because we can continue on and recover manually later on.
    echo "ERROR:  ServiceNow Integration RPM could not make required database changes." 1>&2
fi

# ================================================================

%preun
#!/bin/bash

PATH=/bin:/usr/bin

# This option requires bash 3.0 or later.  All of our supported platforms include such a release.
set -o pipefail

# FIX LATER:  The configuration files should be backed up somewhere before the
# entire package is deleted, since they may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.  (Actually,
# I believe rpm will not delete a locally modified copy of any %%config(noreplace)
# file, when the package is removed, instead renaming it with a .rpmsave extension.)

# Stop the ServiceNow Integration.
#
# For this package, there are no long-running processes, so there is no need to take any
# action to shut down such processes.  At most, we might want to bounce gwservices after
# the uninstall, to remove any residual related data cached within Hibernate.

if [ "$1" = 0 -o "$1" = "remove" ]; then

    # Last uninstall.

    # Make sure the ServiceNow Integration nagios cron job is removed.  We carry out this action
    # in small steps to minimize the likelihood of leaving the system in a mangled state,
    # having corrupted the existing content and not having installed the new content.

    old_crontab=`crontab -l -u nagios`
    if [ $? -ne 0 ]; then
	echo "FATAL:  ServiceNow Integration RPM could not fetch the old nagios crontab" 1>&2
	exit 1
    fi
    if [ "`echo "$old_crontab" | sed -e 's/#.*//' | fgrep -c '/twoway_helpdesk.pl '`" -eq 0 ]; then
	# The cron job is not installed.  We don't bother to try to delete
	# an existing cron job if it is currently commented out.
	echo "NOTICE:  The ServiceNow Integration nagios cron job looks like it was already commented out or removed."
    else
	# new_cron_job='*/5 * * * * /usr/local/groundwork/servicenow/bin/twoway_helpdesk.pl > /dev/null 2>&1'
	new_crontab=`echo "$old_crontab" | sed -e '/\/twoway_helpdesk.pl /d'`
	if [ $? -ne 0 ]; then
	    echo "FATAL:  ServiceNow Integration RPM could not deal with its nagios cron job" 1>&2
	    exit 1
	fi
	echo "$new_crontab" | crontab -u nagios -
	if [ $? -ne 0 ]; then
	    echo "FATAL:  ServiceNow Integration RPM could not remove its nagios cron job" 1>&2
	    exit 1
	fi
    fi

    # Remove the related database objects, so they don't continue to contaminate the rest
    # of the system (e.g., the Event Console Actions menu) after the package is removed.
    # We could have included this script right here into the specfile (using %include),
    # and not bothered to ship the script as a separate file, but doing so allows us to
    # run it at other times if that should prove useful.
    %{servicenowpath}/db/scrub_databases_for_helpdesk
    if [ $? -ne 0 ]; then
	# We make this a fatal error to force proper cleanup now, because the scripting
	# to do this won't be available for a re-run after the package is removed.
	# If this is really a persistent problem that makes it nearly impossible to remove
	# the package, you can always run the rpm removal with the --noscripts option.
	echo "FATAL:  ServiceNow Integration RPM could not make required database changes." 1>&2
    fi

else

    # Upgrading.

    : do nothing

fi

# ================================================================

%postun
#!/bin/bash

PATH=/bin:/usr/bin

# FIX LATER:  The configuration files should be backed up somewhere before the
# entire package is deleted, since they may represent valuable local configuration
# data you might want to preserve for a later re-addition of the package.  (Actually,
# I believe rpm will not delete a locally modified copy of any %%config(noreplace)
# file, when the package is removed, instead renaming it with a .rpmsave extension.)

# FIX LATER:
# We don't bother to test whether this is the last uninstall or an upgrade.
# Either way, we would want to kill all outstanding copies of the ServiceNow
# Integration software, if we had any persistent processes.

# Under Ubuntu, don't run this if "$1" = "upgrade".
# Under RHEL, don't run this if "$1" = "1" (also an upgrade situation).
#
# Also note that Ubuntu (dpkg) runs the un-install script before the upgrade script,
# while RHEL (rpm) runs the upgrade script before the un-install script, for whatever
# difference that might make to our construction here.
#
# 'purge' is only specified when Ubuntu dpkg wants to delete only any remaining
# config and log files, after a previous 'remove' operation.  In contrast,
# 'remove' is specified when Ubuntu dpkg wants to delete the rest of the package.
# 
if [ "$1" = 0 -o "$1" = "remove" -o "$1" = "abort-remove" ]; then
    : do nothing
fi
# We don't bother testing the exit status of that script here, because in a true
# post-uninstall operation (where we really are removing the last copy of the
# package, with no intent to install a replacement copy), there's really nothing
# downstream to block and no useful recovery action to take if we do find a failure.
# We might want to revisit that decision in the Ubuntu environment, if a (partial?)
# failure of the uninstall of the previous package perhaps ought to be somehow
# reflected in the system's notion of the package state.

# ================================================================

%files

%defattr(0644,nagios,nagios)

# The tree of directories containing this software.
%dir %attr(0755,nagios,nagios) %{servicenowpath}
%dir %attr(0755,nagios,nagios) %{servicenowpath}/bin
%dir %attr(0755,nagios,nagios) %{servicenowpath}/config
%dir %attr(0755,nagios,nagios) %{servicenowpath}/db
%dir %attr(0755,nagios,nagios) %{servicenowpath}/doc
%dir %attr(0755,nagios,nagios) %{servicenowpath}/info
%dir %attr(0755,nagios,nagios) %{servicenowpath}/logs
%dir %attr(0755,nagios,nagios) %{servicenowpath}/perl

%attr(0444,nagios,nagios) %{servicenowpath}/db/delete_helpdesk_actions.sql
%attr(0444,nagios,nagios) %{servicenowpath}/db/helpdesk_actions_seed_file.sql
%attr(0444,nagios,nagios) %{servicenowpath}/db/helpdesk_dynamic_property_types.sql
%attr(0444,nagios,nagios) %{servicenowpath}/db/helpdesk_initialize_bridge_db.sql
%attr(0754,nagios,nagios) %{servicenowpath}/db/prepare_databases_for_helpdesk
%attr(0754,nagios,nagios) %{servicenowpath}/db/scrub_databases_for_helpdesk

%attr(0444,nagios,nagios) %{servicenowpath}/info/build_info

# If a config file contains secret access credentials, we protect it via 0600 permissions.
# But if there's nothing especially critical in it, we allow anyone to read it.
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/authorized_users.conf
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/console-admin-config.xml
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/groundwork_server_to_location_mapping.conf
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/hostgroup_and_service_to_assignment_group_mapping.conf
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/hostgroup_to_category_and_subcategory_mapping.conf
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/oneway_helpdesk.conf
%config(noreplace) %attr(0600,nagios,nagios) %{servicenowpath}/config/servicenow_module.conf
%config(noreplace) %attr(0644,nagios,nagios) %{servicenowpath}/config/twoway_helpdesk.conf

%dir %attr(0755,nagios,nagios) %{servicenowpath}/doc/%{name}-%{version}
%doc %attr(0444,nagios,nagios) %{servicenowpath}/doc/%{name}-%{version}/SERVICENOW_INTEGRATION_INSTALL_NOTES
# FIX LATER:  create these files and enable their inclusion here
# %doc %attr(0444,nagios,nagios) %{servicenowpath}/doc/%{name}-%{version}/SERVICENOW_INTEGRATION_RELEASE_NOTES
# %doc %attr(0444,nagios,nagios) %{servicenowpath}/doc/%{name}-%{version}/groundwork-servicenow-integration.1.0.pdf

%attr(0754,nagios,nagios) %{servicenowpath}/bin/oneway_helpdesk.pl
%attr(0754,nagios,nagios) %{servicenowpath}/bin/twoway_helpdesk.pl

# In similar applications, we might provide a file like this and enable its
# inclusion here.  In the present integration, we just rotate the respective
# log files within the oneway_helpdesk.pl and twoway_helpdesk.pl scripts.
# %attr(0644,root,root) /etc/logrotate.d/groundwork-servicenow-integration

# We include everything Perl-related by an automated inclusion based
# on an externally-generated and infrequently-updated file list, rather
# than specifying here the entire file trees rooted in these locations:
# %dir %attr(0755,nagios,nagios) %{servicenowpath}/perl/bin
# %dir %attr(0755,nagios,nagios) %{servicenowpath}/perl/lib
# %dir %attr(0755,nagios,nagios) %{servicenowpath}/perl/man
# %dir %attr(0755,nagios,nagios) %{servicenowpath}/perl/share

%include %(echo $PWD)/rpmsetup/%{name}-%{version}-%{_arch}.perl_filelist

# ================================================================

%changelog
* Sat Jun  4 2011 Glenn Herteg <support@groundworkopensource.com> 1.0.0
- initial RPM construction
