#!/usr/bin/perl -w --

##
##  gdmakey_build.pl
##
##  Copyright (C) 2008 Groundwork Open Source
##  Written by Daniel Emmanuel Feinsmith
## 
##  This program is free software; you can redistribute it and/or
##  modify it under the terms of the GNU General Public License
##  as published by the Free Software Foundation; either version 2
##  of the License, or (at your option) any later version.
##
##  This program is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##  GNU General Public License for more details.
##
##  You should have received a copy of the GNU General Public License
##  along with this program; if not, write to the Free Software
##  Foundation, Inc., 51 Franklin Street, Fifth Floor
##  Boston, MA  02110-1301, USA.
##
##  Change Log:
##      DEF Created on Dec 10, 2007, 1:00 PM
##	GH Modified on Apr 28, 2008, 4:30 PM; added error checking and other robustness improvements
##
##  Description:
##   	Builds GDMA Client Key RPM's
##

use POSIX;

##
##	Build RPM package
##

# We might override this later.
my $have_customer_known_hosts = 0;

sub build_client_key_rpm
{
    my $cmd;
    my $build_root = shift;
    my $install_root = shift;
    my $gdmakey_version = shift;
    my $server_name = shift;
    my $dashed_server_name = shift;
    my $macro;
    my $os_rpm_root;
    my $status;
    my $result;

    print "  ============================\n";
    print "  Building client key RPM\n";
    print "  ============================\n";

    print "    Build Root: $build_root\n";

    die "ERROR:  Specfile for GDMA key version '$gdmakey_version' does not exist; aborting!\n" if (! -f "rpmsetup/gdmakey-$gdmakey_version.spec");

    # Set OS RPM root.

    $os_rpm_root = "/usr/lib/rpm";

    # Create specific macro file.

    print "    Creating macro file specific for this client\n";
    open(MACROS, ">$build_root/rpmsetup/gdmakey_client.rpmmacros")
	|| die "Can't create gdmakey_client.rpmmacros file\n";
    print MACROS "##\n## gdmakey_client.rpmmacros\n## Repeatedly auto-generated for every individual client by [gdmakey_build.pl]\n##\n\n";
    print MACROS "%server_name\t$dashed_server_name\n";
    print MACROS "%have_customer_known_hosts\t$have_customer_known_hosts\n";
    print MACROS "\n## EOF\n";
    close(MACROS);

    print "    Building, build root is: $build_root\n";

    # Executing this command properly depends on the CWD being the $build_root, but that setup is assured by the caller.
    $cmd = "rpmbuild --rcfile $os_rpm_root/rpmrc:$os_rpm_root/redhat/rpmrc:$build_root/rpmsetup/gdmakey.rpmrc -bb --short-circuit $build_root/rpmsetup/gdmakey-$gdmakey_version.spec >/tmp/out.$server_name 2>&1";

    # Execute.

    #print "    Build command is: $cmd\n";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  RPM build for server $server_name failed; see /tmp/out.$server_name for details\n" if $status != 0;

    print "    Done.\n";
}

##
##      Make Key
##

sub make_key
{
    my $build_root = shift;
    my $install_root = shift;
    my $server_name = shift;
    my $customer_name = shift;
    my $base_dir = "$install_root/usr/local/groundwork/gdma/.ssh";
    my $cmd;
    my $status;
    my $result;

    print "Creating key file directory.\n";
    $cmd = "(echo 'Creating key file directory ...'; mkdir -p $base_dir) >>/tmp/out.$server_name 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  RPM build for server $server_name failed; see /tmp/out.$server_name for details\n" if $status != 0;

    print "Making Key, putting in: $base_dir\n";

    # 1. Remove old key.

    unlink($base_dir . '/id_dsa');
    unlink($base_dir . '/id_dsa.pub');
    unlink($base_dir . '/known_hosts');

    # 2. Generate New Key.

    my $keygen_command = 'ssh-keygen -t dsa -b 1024 -q -f ' . $base_dir . '/id_dsa -N ""';
    print "  " . $keygen_command . "\n";
    my $res = qx/$keygen_command 2>&1/;
    if ($res eq "") {
	print "  Done.\n";
    } else {
	print "  " . $res . "\n";
    }

    # 3. Copy the known_hosts file into place if we have one for this customer.
    #    This isn't really part of making a key, but it's convenient to do the work here.

    # Look for and process a "known_hosts" file for this customer.  We may or may not have one in stock.
    # If we have one, it will be stored in "$build_root/ssh/known_hosts.$customer_name"
    # and we need to copy it to ~gdma/.ssh/known_hosts in the RPM install root.
    # Then the specfile's filelist will need to conditionally include the file.
    if (defined ($customer_name)) {
        if (-f "$build_root/ssh/known_hosts.$customer_name") {
	    $have_customer_known_hosts = 1;
	    print "    Copying customer-specific known_hosts file.\n";
	    $cmd = "cp -p $build_root/ssh/known_hosts.$customer_name $install_root/usr/local/groundwork/gdma/.ssh/known_hosts";
	    $result = `$cmd`;
	    $status = $?;
	    die "ERROR:  copying of '$build_root/ssh/known_hosts.$customer_name' failed\n" if $status != 0;
	}
    }
}

##
##      Build gdma server configuration file.
##

sub build_server_config
{
    my $build_root = shift;
    my $install_root = shift;
    my $server_name = shift;
    my $server_ip_address = shift;
    my $config_dir = "$install_root/usr/local/groundwork/gdma/config";
    my $cmd;
    my $status;
    my $result;

    print "Creating server configuration file directory.\n";
    $cmd = "(echo 'Creating server configuration file directory ...'; mkdir -p $config_dir) >/tmp/out.$server_name 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  RPM build for server $server_name failed; see /tmp/out.$server_name for details\n" if $status != 0;

    print "Creating server configuration file; GW Server IP Address=$server_ip_address.\n";
    $cmd = "(echo 'Creating server configuration file ...'; echo $server_ip_address >$config_dir/gdma_server.conf) >>/tmp/out.$server_name 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  RPM build for server $server_name failed; see /tmp/out.$server_name for details\n" if $status != 0;
}

##
##	Initialization
##

sub initialize
{
    my $build_root = shift;

    print "Initializing\n";
}

##
##	Main
##

sub main
{
    my $gdmakey_version = shift;
    my $server_name = shift;
    my $server_ip_address = shift;
    my $customer_name = shift;
    my $build_root;
    my $install_root;
    my $dashed_server_name;
    my $cwd = getcwd();

    $gdmakey_version =~ /^\d+[.]\d+[.]\d+$/ || die "ERROR:  GDMA key version '$gdmakey_version' is invalid; aborting!\n";
    $server_ip_address =~ /^\d{1,3}[.]\d{1,3}[.]\d{1,3}[.]\d{1,3}$/ || die "ERROR:  GW server IP address '$server_ip_address' is invalid; aborting!\n";

    # Clean up the server name so we don't introduce any dots as unwanted
    # separators in the generated RPM package name.  Dashes are okay, though.
    ($dashed_server_name = $server_name) =~ s/[.]/-/g;

    chdir("..");
    $build_root = getcwd();
    $install_root = $build_root . "/rpmbuild/gdmakey-$dashed_server_name-$gdmakey_version/INSTALL_ROOT";

    initialize($build_root);
    build_server_config($build_root, $install_root, $server_name, $server_ip_address);
    make_key($build_root, $install_root, $server_name, $customer_name);
    build_client_key_rpm($build_root, $install_root, $gdmakey_version, $server_name, $dashed_server_name);

    chdir($cwd);
}

print "====================================\n";
print "==  gdmakey_build.pl              ==\n";
print "==  Build GDMA Linux Key RPM      ==\n";
print "==                                ==\n";
print "==  Groundwork Open Source, Inc.  ==\n";
print "==  Daniel Emmanuel Feinsmith     ==\n";
print "====================================\n";

if (scalar(@ARGV) == 3) {
    main ($ARGV[0], $ARGV[1], $ARGV[2], undef);
} elsif (scalar(@ARGV) == 4) {
    main ($ARGV[0], $ARGV[1], $ARGV[2], $ARGV[3]);
} else {
    print "usage:    gdmakey_build.pl {gdmakey_version} {gwserver_name} {gwserver_ip_address} [company_name]\n";
    print "example:  gdmakey_build.pl 2.0.3 myserver.my-company.com 123.45.67.89 [my-company]\n";
}
exit;
