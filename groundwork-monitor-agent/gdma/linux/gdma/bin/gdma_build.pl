#!/usr/bin/perl -w --

##
##  gdma_build.pl
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
##      GH Modified on Apr 25, 2008, 1:30 PM; extended to add support for per-customer customizations
##
##  Description:
##   	Builds a GDMA Client RPM for each supported LINUX architecture.
##
##		rhel4.i386       (Red Hat 4, 32 bit)
##		rhel4_64.x86_64  (Red Hat 4, 64 bit)
##		rhel5.i386       (Red Hat 5, 32 bit)
##		rhel5_64.x86_64  (Red Hat 5, 64 bit)
##		sles9.i586       (SuSe  9, 32 bit)
##		sles10.i586      (SuSe 10, 32 bit)
##		sles10_64.x86_64 (SuSe 10, 64 bit)
##

use POSIX;

my $gdma_version;

if (scalar(@ARGV) == 1) {
    $gdma_version = $ARGV[0];
    $customer   = undef;
    $gid_number = undef;
    $uid_number = undef;
} elsif (scalar(@ARGV) == 4) {
    $gdma_version = $ARGV[0];
    $customer     = $ARGV[1];
    $gid_number   = $ARGV[2];
    $uid_number   = $ARGV[3];
    # In addition to the constraint listed in the error message, a customer name cannot begin or end with a dash.
    $customer   =~ /^[a-z]+(-[a-z]+)*$/ || die "ERROR:  Customer Name '$customer' contains characters other than lowercase and dashes; aborting!\n";
    $gid_number =~ /^\d+$/              || die "ERROR:  Group ID '$gid_number' is not numeric; aborting!\n";
    $uid_number =~ /^\d+$/              || die  "ERROR:  User ID '$uid_number' is not numeric; aborting!\n";
    # To further qualify these ID values, in particular to prevent specifying
    # the gdma user as being root, and more generally to reduce the likelihood
    # of collisions with standard system IDs, we limit their possible values.
    # If the customer insists on using particular numeric values that violate these
    # constraints, just comment out these two error checks before you run this script.
    die "ERROR:  Group ID '$gid_number' is too small (must be 100 or greater); aborting!\n" if ($gid_number < 100);
    die  "ERROR:  User ID '$uid_number' is too small (must be 100 or greater); aborting!\n" if ($uid_number < 100);
} else {
    print "usage:    gdma_build.pl gdma_version [customer_name gid_number uid_number]\n";
    print "where:    customer_name, if given, must include only lowercase and dashes.\n";
    print "          If no customer/gid/uid values are given, the RPM's internal scripting\n";
    print "          will simply cause the gdma account gid/uid values to assume local\n";
    print "          default values on each machine where the gdma RPM is installed.\n";
    print "example:  gdma_build.pl 2.0.5 advance-internet 31341 31341\n";
    exit 1;
}

$gdma_version =~ /^\d+[.]\d+[.]\d+$/ || die "ERROR:  GDMA version '$gdma_version' is invalid; aborting!\n";

# Perhaps fix this:  There is some redundancy in the architecture naming here.
# I believe that "rhel4.x86_64" would be sufficient to tell that this is
# definitively a 64-bit architecture.  But we may be dealing with trying to
# match a convention used for the base product; perhaps we need to get that
# fixed first.
my @architectures = (
    "rhel4.i386",
    "rhel4_64.x86_64",
    "rhel5.i386",
    "rhel5_64.x86_64",
    "sles9.i586",
    "sles10.i586",
    "sles10_64.x86_64"
);
my $num_architectures = scalar(@architectures);

##
##	Build RPM package for a given architecture
##

sub build_client_rpm_for_arch
{
    my $cmd;
    my $arch = shift;
    my $build_root;
    my $cwd = getcwd();
    my $install_dir;
    my $macro;
    my $os_rpm_arch_root;
    my $os_rpm_root;
    my $status;
    my $result;
    my $build_arch;

    # Retain just the the last portion of the complete platform architecture as the build architecture.
    # We need to reference this within the specfile to determine whether or not we're building for a 64-bit platform.
    # We have to do this munging here because specfile syntax doesn't allow for pattern matching.
    ($build_arch = $arch) =~ s/.*\.//;

    print "  ============================\n";
    print "  Building RPM for [$arch]\n";
    print "  ============================\n";

    chdir("..");
    $build_root = getcwd(); # . "/dependencies";
    print "    Build Root: $build_root\n";

    die "ERROR:  Specfile for GDMA version '$gdma_version' does not exist; aborting!\n" if (! -f "rpmsetup/gdma-$gdma_version.spec");

    # Set default macro set.

    $os_rpm_root = "/usr/lib/rpm";

    # Need to unpack the tar file for this arch.

    $install_dir = $build_root . "/rpmbuild";
    mkdir($install_dir) || die "ERROR:  Cannot make the install root directory '$install_dir'!" if ! -d $install_dir;
    $install_dir .= "/gdma" . (defined($customer) ? ('-' . $customer) : '') . "-$gdma_version";
    mkdir($install_dir) || die "ERROR:  Cannot make the install root directory '$install_dir'!" if ! -d $install_dir;
    $install_dir .= "/INSTALL_ROOT";

    # Clean up the install root and the generated macros file before trying to make them, to remove
    # any debris left over after dying before cleanup during some failed previous attempt to build RPMs.
    $cmd = "rm -rf $install_dir";
    $result = `$cmd`;
    $cmd = "rm -f $build_root/rpmsetup/gdma_arch.rpmmacros";
    $result = `$cmd`;

    mkdir($install_dir) || die "ERROR:  Cannot make the install root directory '$install_dir'!" if ! -d $install_dir;
    chdir($install_dir) || die "ERROR:  Cannot chdir to the install root!";

    print "    Unpacking architecture-independent dependencies.\n";
    $cmd = "(echo === untarring dep.noarch.tar; tar xvf $build_root/dependencies/dep.noarch.tar) >/tmp/out.$arch 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  un-tar of $build_root/dependencies/dep.noarch.tar failed;\nsee /tmp/out.$arch for details\n" if $status != 0;

    # =================================================================================================================
    # NOTE:  This is a temporary hack until this version of check_mem.pl is folded into our standard GW Monitor builds.
    # =================================================================================================================
    print "    Overlaying architecture-independent dependencies.\n";
    $cmd = "(echo === overlaying dependencies; cp -p $build_root/dependencies/check_mem.pl usr/local/groundwork/gdma/libexec) >>/tmp/out.$arch 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  overlaying of dependencies failed;\nsee /tmp/out.$arch for details\n" if $status != 0;
    # =================================================================================================================

    print "    Unpacking architecture-specific dependencies.\n";
    $cmd = "(echo === untarring dep.$arch.tar; tar xvf $build_root/dependencies/dep.$arch.tar) >>/tmp/out.$arch 2>&1";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  un-tar of $build_root/dependencies/dep.$arch.tar failed;\nsee /tmp/out.$arch for details\n" if $status != 0;

    print "    Creating macro file specific for this architecture\n";
    open(MACROS, '>', "$build_root/rpmsetup/gdma_arch.rpmmacros")
	|| die "ERROR:  Can't create gdma_arch.rpmmacros file\n";
    print MACROS "##\n## gdma_arch.rpmmacros\n## Repeatedly auto-generated for every individual architecture by [gdma_build.pl]\n##\n\n";
    print MACROS "%customer\t$customer\n"     if $customer;
    print MACROS "%gid_number\t$gid_number\n" if $gid_number;
    print MACROS "%uid_number\t$uid_number\n" if $uid_number;
    print MACROS "%arch\t\t$arch\n";
    print MACROS "%build_arch\t$build_arch\n";
    print MACROS "\n## EOF\n";
    close(MACROS);

    print "    Building " . $arch . ", build root is: $build_root\n";

    chdir($build_root);
    $cmd = "rpmbuild --rcfile $os_rpm_root/rpmrc:$os_rpm_root/redhat/rpmrc:$build_root/rpmsetup/gdma.rpmrc -bb --short-circuit $build_root/rpmsetup/gdma-$gdma_version.spec >>/tmp/out.$arch 2>&1";

    # Execute.
    print "    Build command is: $cmd\n";
    $result = `$cmd`;
    $status = $?;
    die "ERROR:  RPM build for architecture $arch failed; see /tmp/out.$arch for details\n" if $status != 0;

    print "    Cleaning up.\n";
    $cmd = "rm -rf $install_dir";
    $result = `$cmd`;
    $cmd = "rm -f $build_root/rpmsetup/gdma_arch.rpmmacros";
    $result = `$cmd`;

    chdir($cwd);
    print "    Done.\n";
}

##
##	Build an RPM for each client.
##

sub build_all_client_rpms
{
    my $arch;

    print "Building $num_architectures architectures\n";
    for ($arch=0; $arch < $num_architectures; $arch++) {
	build_client_rpm_for_arch($architectures[$arch]);
    }
}

##
##	Initialization
##

sub initialize
{
    print "Initializing\n";
}

##
##	Main
##

print "====================================\n";
print "==  gdma_build.pl                 ==\n";
print "==  Build GDMA Linux client RPMs  ==\n";
print "==                                ==\n";
print "==  Groundwork Open Source, Inc.  ==\n";
print "==  Daniel Emmanuel Feinsmith     ==\n";
print "====================================\n";

initialize();
build_all_client_rpms();
exit;
