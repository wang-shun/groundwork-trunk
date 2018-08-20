#!/usr/local/groundwork/helpdesk/perl/bin/perl -w

use ExtUtils::Installed;

my $instmod = ExtUtils::Installed->new();
my $host    = `/bin/hostname`;

print "\nPerl Modules Installed On Host $host \n";

foreach my $module ($instmod->modules()) {
	my $version = $instmod->version($module) || "???";
       	print "$module -- $version\n";
}

print "\n";

