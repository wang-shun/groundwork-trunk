#!/usr/bin/perl --
# MonArch - Groundwork Monitor Architect
# monarch_update.pl
#
###############################################################################
# Release 2.5
# 8-Apr-2008
###############################################################################
# Author: Scott Parris
#
# Copyright 2008 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

use DBI;
use strict;
use XML::LibXML;
use File::Copy;

my $version = '2.5';
my $monarch_home = undef;
my $is_portal = 0;
my $monarch_home = 0;
my $nagios_bin = 0;
my $nagios_version = 0;
my $nagios_etc = 0;
my $cgi_bin = 0;
my $cgi_path = "/cgi-bin";
my $doc_root = 0;
my $web_group = "apache";

sub parse_xml($) {
	my $data = shift;
	my %properties = ();
	if ($data) {
		my $parser = XML::LibXML->new();
		my $doc = $parser->parse_string($data);
		my @nodes = $doc->findnodes( "//prop" );
		foreach my $node (@nodes) {
			if ($node->hasAttributes()) {
				my $property = $node->getAttribute('name');
				my $value = $node->textContent;
				$value =~ s/\s+$|\n//g;
				if ($property =~ /command$/) {
					my $command_line = '';
					if ($value) {
						my @command = split(/!/, $value);
						$properties{$property} = $command[0];
						if ($command[1]) {
							foreach my $c (@command) {
								$command_line .= "$c!";
							}
						}
					}
					$command_line =~ s/!$//;
					$properties{'command_line'} = $command_line;
				} elsif ($property =~ /last_notification$/) {
					my $value = $node->textContent;
					$value =~ s/\s+$|\n//g;
					if ($value == 0) {
						$properties{$property} = '-zero-';
					} else {
						$properties{$property} = $value;
					}
				} else {
					$properties{$property} = $value;
				}
			}
		}
		return %properties;
	} else {
		$properties{'error'} = "Empty String (parse_xml)";
	}
}
my ($dbhost, $database, $user, $passwd) = undef;
if (-e "/usr/local/groundwork/config/db.properties") {
	open(FILE, "< /usr/local/groundwork/config/db.properties");
	while (my $line = <FILE>) {
		if ($line =~ /\s*monarch\.dbhost\s*=\s*(\S+)/) { $dbhost = $1 }
		if ($line =~ /\s*monarch\.database\s*=\s*(\S+)/) { $database = $1 }
		if ($line =~ /\s*monarch\.username\s*=\s*(\S+)/) { $user = $1 }
		if ($line =~ /\s*monarch\.password\s*=\s*(\S+)/) { $passwd = $1 }
	}
	close(FILE);
	$is_portal = 1;
	$nagios_version = '2.x';
	$monarch_home = '/usr/local/groundwork/monarch';
} else {
	print "\n\tMonarch $version Update";
	print "\n=============================================================\n";
	print "\n\tReading configuration file...\n";

	until ($monarch_home) {
		if (-e "/usr/local/groundwork/monarch/lib/MonarchConf.pm") {
			$monarch_home = "/usr/local/groundwork/monarch";
			print "\n\tPlease enter the Monarch installation path [ $monarch_home ] : ";
			my $input = <STDIN>;
			chomp $input;
			if ($input) { $monarch_home = $input }
			my $monarch_test = $monarch_home.'/lib/MonarchConf.pm';
			unless (-e $monarch_test) {
				print "\n\tError: Cannot locate MonarchConf.pm in path $monarch_home [/lib]...\n";
				$monarch_home = 0;
			}
		} else {
			print "\n\tPlease enter the Monarch installation path : ";
			my $input = <STDIN>;
			chomp $input;
			if ($input) { $monarch_home = $input }
			my $monarch_test = $monarch_home.'/lib/MonarchConf.pm';
			unless (-e $monarch_test) {
				print "\n\tError: Cannot locate MonarchConf.pm in path $monarch_home [/lib]...\n";
				$monarch_home = 0;
			}
		}
	}
	open(FILE, "< $monarch_home/lib/MonarchConf.pm");
	while (my $line = <FILE>) {
		$line =~ s/\'|\"|;//g;
		if ($line =~ /\s*\$dbhost\s*=\s*(\S+)/) { $dbhost = $1 }
		if ($line =~ /\s*\$database\s*=\s*(\S+)/) { $database = $1 }
		if ($line =~ /\s*\$dbuser\s*=\s*(\S+)/) { $user = $1 }
		if ($line =~ /\s*\$dbpass\s*=\s*(\S+)/) { $passwd = $1 }
	}
	close(FILE);
}


# Connect to DB
##############################################################################

print "\n\tConnecting to $database with user $user...\n" unless $is_portal;

my $dsn = "DBI:mysql:$database:$dbhost";
my $dbh = undef;
eval {$dbh = DBI->connect($dsn, $user, $passwd, {'RaiseError' => 1}) };
if ($@) {
	print "\nError: $@\n";
	die;
}


#
##############################################################################
# Distribution
##############################################################################
#

unless ($is_portal) {
	print "\n\tDistributing files...\n";

	my $sqlstmt = "select name, value from setup where type = 'config'";
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %name_val = ();
	while(my @values = $sth->fetchrow_array()) {
		$name_val{$values[0]} = $values[1];
	}
	$sth->finish;

	until ($doc_root) {
		if (-e "$name_val{'doc_root'}") {
			$doc_root = $name_val{'doc_root'};
		} else {
			print "\n\n\n\tWhat is the full path to your web server's document root : ";
			my $input = <STDIN>;
			chomp $input;
			if (-e "$input") {
				$doc_root = $input;
			} else {
				print "\n\n\n\tError: Invalid entry $doc_root does not exist.";
			}
		}
	}

	until ($cgi_bin) {
		if (-e "$name_val{'doc_root'}") {
			$cgi_bin = $name_val{'cgi_bin'};
		} else {
			print "\n\n\n\tEnter the full path of your cgi-bin directory [$cgi_bin] : ";
			my $input = <STDIN>;
			chomp $input;
			if (-e "$input") {
				$cgi_bin = $input;
			} else {
				print "\n\n\n\tError: Invalid entry $cgi_bin does not exist.";
			}
		}
	}


	opendir(DIR, './images') || print "\n\n./images $!";
	while (my $file = readdir(DIR)) {
		if ($file =~/^\./) { next }
		print "\n\t$doc_root/monarch/images/$file";
		copy("./images/$file","$doc_root/monarch/images/$file") || print "\n\nerror: $!";
	}
	close(DIR);

	if (! -e "$doc_root/monarch/doc") {
		mkdir("$doc_root/monarch/doc", 0777) || print "\n\n$doc_root/monarch/doc $!";
		system("chmod 777 $doc_root/monarch/doc");
	}

	opendir(DIR, './doc') || print "\n\n./doc $!";
	while (my $file = readdir(DIR)) {
		if ($file =~/^\.|^images$/) { next }
		print "\n\t$doc_root/monarch/doc/$file";
		copy("./doc/$file","$doc_root/monarch/doc/$file") || print "\n\nerror: $!";
	}
	close(DIR);

	if (! -e "$doc_root/monarch/doc/images") {
		mkdir("$doc_root/monarch/doc/images", 0777) || print "\n\n$doc_root/monarch/doc/images $!";
		system("chmod 777 $doc_root/monarch/doc/images");
	}

	opendir(DIR, './doc/images') || print "\n\n./doc/images $!";
	while (my $file = readdir(DIR)) {
		if ($file =~/^\./) { next }
		copy("./doc/images/$file","$doc_root/monarch/doc/images/$file") || print "\n\n./doc/$file $doc_root/monarch/doc/images/$file $!";
	}
	close(DIR);

	my @files = (
	    'FormValidator.js',
	    'autosuggest.css',
	    'autosuggest2.js',
	    'blank.html',
	    'dtree.css',
	    'groundwork.css',
	    'monarch.css',
	    'monarch.js',
	    'nicetitle.js',
	    'wz_tooltip.js',
	    );
	foreach my $file (@files) {
		copy("./$file","$doc_root/monarch/$file") || print "\n\n./$file $doc_root/monarch/$file $!";
		print "\n\t$doc_root/monarch/$file";
	}

	my $web_group = "www";
	my $web_user = "wwwrun";
	if (-e '/etc/redhat-release') {
		$web_group = "apache";
		$web_user = "apache";
	}

	my $validated = 0;
	unless (-e "$monarch_home/lib/MonarchProfileImport.pm") {
		until ($validated) {
			print "\n\n\n\tEnter web server's user account [$web_user] : ";
			my $input = <STDIN>;
			chomp $input;
			if ($input) { $web_user = $input }
			my @user = getpwnam($web_user);
			if ($user[0]) {
				$validated = 1;
			} else {
				print "\n\n\n\tError: Invalid, user $input does not exist.";
			}
		}
		$validated = 0;
		until ($validated) {
			print "\n\n\n\tEnter web server's user group [$web_group] : ";
			my $input = <STDIN>;
			chomp $input;
			if ($input) { $web_group = $input }
			my @grp = getgrnam($web_group);
			if ($grp[0]) {
				$validated = 1;
			} else {
				print "\n\n\n\tError: Invalid, group $input does not exist.";
			}
		}
	}

	$validated = 0;
	until ($validated) {
		print "\n\n\n\tEnter web server's user group [$web_group] : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $web_group = $input }
		my @grp = getgrnam($web_group);
		if ($grp[0]) {
			$validated = 1;
		} else {
			print "\n\n\n\tError: Invalid, group $input does not exist.";
		}
	}

	@files = ('MonarchFile.pm','MonarchStorProc.pm','MonarchExternals.pm','MonarchDoc.pm','MonarchLoad.pm','MonarchProfileExport.pm',
		'MonarchProfileImport.pm','MonarchTree.pm','MonarchAudit.pm','MonarchFoundationSync.pm','MonarchAPI.pm','MonarchAutoConfig.pm');
	foreach my $file (@files) {
		copy("./$file","$monarch_home/lib/$file") || print "\n\nnot copied ./$file $monarch_home/lib/$file $!";
		print "\n\t$monarch_home/lib/$file";
	}

	unless (-e "$monarch_home/lib/MonarchCallOut.pm") {
		copy("./MonarchCallOut.pm","$monarch_home/lib/MonarchCallOut.pm") || print "\n\nnot copied ./MonarchCallOut.pm $monarch_home/lib/MonarchCallOut.pm $!";
		print "\n\t$monarch_home/lib/MonarchCallOut.pm";
	}

	unless (-e "$monarch_home/lib/MonarchDeploy.pm") {
		copy("./MonarchDeploy.pm","$monarch_home/lib/MonarchDeploy.pm") || print "\n\nnot copied ./MonarchDeploy.pm $monarch_home/lib/MonarchDeploy.pm $!";
		print "\n\t$monarch_home/lib/MonarchDeploy.pm";
	}

	unless (-e "$monarch_home/lib/MonarchExternals.pm") {
		copy("./MonarchExternals.pm","$monarch_home/lib/MonarchExternals.pm") || print "\n\nnot copied ./MonarchExternals.pm $monarch_home/lib/MonarchExternals.pm $!";
		print "\n\t$monarch_home/lib/MonarchExternals.pm";
	}
	unless (-e "$doc_root/favicon.ico") {
		copy("./favicon.ico","$doc_root/favicon.ico") || print "\n\n./favicon.ico $doc_root/favicon.ico $!";
	}

	if ($validated) { system("chown -R $web_user:$web_group $monarch_home") }

	@files = ('nagios_reload','nmap_scan_one');
	foreach my $file (@files) {
		copy("./$file","$monarch_home/bin/$file") || print "\n\nnot copied ./$file $monarch_home/bin/$file $!\n";
		print "\n\t$monarch_home/bin/$file";
	}
	system("chown root:$web_group $monarch_home/bin/nmap_scan_one");
	system("chmod 4750 $monarch_home/bin/nmap_scan_one");

	open(FILE, "< ./monarch.cgi") || print "\n\n ./monarch.cgi $!";
	my $out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	print "\n\tWriting $cgi_bin/monarch.cgi";
	open(FILE, "> $cgi_bin/monarch.cgi") || print "\n\nerror: $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch.cgi");

	open(FILE, "< ./monarch_tree.cgi") || print "\n\n ./monarch_tree.cgi $!";
	my $out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	print "\n\tWriting $cgi_bin/monarch_tree.cgi";
	open(FILE, "> $cgi_bin/monarch_tree.cgi") || print "\n\nerror: $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch_tree.cgi");

	print "\n\tWriting $cgi_bin/monarch_ez.cgi";
	open(FILE, "< ./monarch_ez.cgi") || print "\n\n./monarch_ez.cgi $!";
	$out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	open(FILE, "> $cgi_bin/monarch_ez.cgi") || print "\n$cgi_bin/monarch_ez.cgi $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch_ez.cgi");

	open(FILE, "< ./monarch_auto.cgi") || print "\n\n ./monarch_auto.cgi $!";
	my $out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	print "\n\tWriting $cgi_bin/monarch_auto.cgi";
	open(FILE, "> $cgi_bin/monarch_auto.cgi") || print "\n\nerror: $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch_auto.cgi");

	my $cgi_line = undef;
	open(FILE, "< $monarch_home/lib/MonarchForms.pm") || print "\n\n. $monarch_home/lib/MonarchForms.pm $!";
	while (my $line = <FILE>) {
		if ($line =~ /^\s*my\s+\$cgi_dir/) {
			$cgi_line = $line;
		}
	}
	close(FILE);

	print "\n\tWriting $monarch_home/lib/MonarchForms.pm";
	open(FILE, "< ./MonarchForms.pm") || print "\n\n ./MonarchForms.pm $!";
	my $out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^\s*my\s+\$cgi_dir/) {
			$line = $cgi_line;
		}
		$out_to_file .= $line;
	}
	close(FILE);
	open(FILE, "> $monarch_home/lib/MonarchForms.pm") || print "\n\nerror: $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 664  $monarch_home/lib/MonarchForms.pm");

	print "\n\tWriting $cgi_bin/monarch_ajax.cgi";
	open(FILE, "< ./monarch_ajax.cgi") || print "\n\n./monarch_ajax.cgi $!";
	my $out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	open(FILE, "> $cgi_bin/monarch_ajax.cgi") || print "\n\nerror: $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch_ajax.cgi");

	print "\n\tWriting $cgi_bin/monarch_scan.cgi";
	open(FILE, "< ./monarch_scan.cgi") || print "\n\n./monarch_scan.cgi $!";
	$out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	open(FILE, "> $cgi_bin/monarch_scan.cgi") || print "\n$cgi_bin/monarch_scan.cgi $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $cgi_bin/monarch_scan.cgi");

	print "\n\tWriting $monarch_home/bin/nmap_scan_one.pl";
	open(FILE, "< ./nmap_scan_one.pl") || print "\n\n./nmap_scan_one.pl $!";
	$out_to_file = undef;
	while (my $line = <FILE>) {
		if ($line =~ /^#!/) {
			$line = "#!/usr/bin/perl --\n";
		}
		if ($line =~ /^\s*use\s+lib\s+/) {
			$line = "use lib qq($monarch_home/lib);\n";
		}
		$out_to_file .= $line;
	}
	close(FILE);
	open(FILE, "> $monarch_home/bin/nmap_scan_one.pl") || print "\n$monarch_home/bin/nmap_scan_one.pl $!";
	print FILE $out_to_file;
	close(FILE);
	system("chmod 755 $monarch_home/bin/nmap_scan_one.pl");


}




#
##############################################################################
# Update setup
#

print "\n\tUpdating setup information...\n" unless $is_portal;


my $sqlstmt = "select value from setup where name = 'nagios_etc'";
my ($nagios_etc) = $dbh->selectrow_array($sqlstmt);

until ($nagios_etc) {
	if (-e "/etc/nagios/nagios.cfg") {
		$nagios_etc = "/etc/nagios";
		print "\n\tPlease enter the path to nagios.cfg [ $nagios_etc ] : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_etc = $input }
		unless (-e $nagios_etc) {
			print "\n\tError: Cannot locate nagios.cfg in path $nagios_etc...\n";
			$nagios_etc = 0;
		}
	} elsif (-e "/usr/local/nagios/etc/nagios.cfg") {
		$nagios_etc = "/usr/local/nagios/etc";
		print "\n\tPlease enter the path to nagios.cfg [ $nagios_etc ] : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_etc = $input }
		unless (-e $nagios_etc) {
			print "\n\tError: Cannot locate nagios.cfg in path $nagios_etc...\n";
			$nagios_etc = 0;
		}
	} else {
		print "\n\tPlease enter the path to nagios.cfg : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_etc = $input }
		unless (-e $nagios_etc) {
			print "\n\tError: Cannot locate nagios.cfg in path $nagios_etc...\n";
			$nagios_etc = 0;
		}
	}
}

my $sqlstmt = "select value from setup where name = 'nagios_version'";
$nagios_version = $dbh->selectrow_array($sqlstmt);
if ($is_portal) { $nagios_version  }
until ($nagios_version) {
	$nagios_version = "2";
	print "\n\tPlease enter 1 for Nagios version 1.x or 2 for Nagios version 2.x [ $nagios_version ] : ";
	my $input = <STDIN>;
	chomp $input;
	if ($input =~ /^(1|2)/) {
		$nagios_version = $1;
	}
	$nagios_version .= ".x";
}


$sqlstmt = "select value from setup where name = 'nagios_bin'";
my ($nagios_bin) = $dbh->selectrow_array($sqlstmt);

until ($nagios_bin) {
	if (-e "/usr/sbin/nagios") {
		$nagios_bin = "/usr/sbin";
		print "\n\tPlease enter the path to the nagios binary [ $nagios_bin ] : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_bin = $input }
		my $nagios_test = $nagios_bin.'/nagios';
		unless (-e $nagios_test) {
			print "\n\tError: Cannot locate nagios binary in path $nagios_bin...\n";
			$nagios_bin = 0;
		}
	} elsif (-e "/usr/local/nagios/bin") {
		$nagios_bin = "/usr/local/nagios/bin";
		print "\n\tPlease enter the path to the nagios binary [ $nagios_bin ] : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_bin = $input }
		my $nagios_test = $nagios_bin.'/nagios';
		unless (-e $nagios_test) {
			print "\n\tError: Cannot locate nagios binary in path $nagios_bin...\n";
			$nagios_bin = 0;
		}
	} else {
		print "\n\tPlease enter the path to nagios binary : ";
		my $input = <STDIN>;
		chomp $input;
		if ($input) { $nagios_bin = $input }
		my $nagios_test = $nagios_bin.'/nagios';
		unless (-e $nagios_test || $nagios_test eq '/tmp/nagios') {
			print "\n\tError: Cannot locate nagios binary in path $nagios_bin...";
			print "\n\tNote: If Nagios doesn't run on this machine try any valid path (i.e. /tmp).";
			$nagios_bin = 0;
		}
	}
}

my %ez = ();
my $sqlstmt = "select name, value from setup where type = 'monarch_ez'";
my $sth = $dbh->prepare($sqlstmt);
$sth->execute;
while(my @values = $sth->fetchrow_array()) {
	$ez{$values[0]} = $values[1];
}
$sth->finish;

my $sqlstmt = "select value from setup where name = 'backup_dir'";
my ($backup_dir) = $dbh->selectrow_array($sqlstmt);
unless ($backup_dir) { $backup_dir = "$monarch_home/backup" }
my $sqlstmt = "select value from setup where name = 'upload_dir'";
my ($upload_dir) = $dbh->selectrow_array($sqlstmt);
unless ($upload_dir) { $upload_dir = "/tmp" }
my $sqlstmt = "select value from setup where name = 'enable_groups'";
my ($enable_groups) = $dbh->selectrow_array($sqlstmt);
unless ($enable_groups) { $enable_groups = "0" }
my $sqlstmt = "select value from setup where name = 'enable_externals'";
my ($enable_externals) = $dbh->selectrow_array($sqlstmt);
unless ($enable_externals) { $enable_groups = "0" }

$sqlstmt = "delete from setup where type = 'monarch_ez' or type = 'config'";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "\n\n\tError: $sqlstmt $@" }
$sth->finish;

if ($is_portal) {
	$sqlstmt = "insert into setup values('is_portal','config','1')";
	my $sth = $dbh->prepare ($sqlstmt);
	unless ($sth->execute) { print "Error: $sqlstmt $@" }
}

$sqlstmt = "insert into setup values('enable_externals','config','$enable_externals')";
my $sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('enable_groups','config','$enable_groups')";
my $sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('nagios_version','config','$nagios_version')";
my $sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('monarch_version','config','$version')";
my $sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('monarch_home','config','$monarch_home')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('backup_dir','config','$backup_dir')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('upload_dir','config','$upload_dir')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('nagios_etc','config','$nagios_etc')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('nagios_bin','config','$nagios_bin')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('doc_root','config','$doc_root')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('cgi_bin','config','$cgi_bin')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('max_tree_nodes','config','3000')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('host_profile','monarch_ez','$ez{host_profile}')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('contactgroup','monarch_ez','$ez{contactgroup}')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }
$sqlstmt = "insert into setup values('contact_template','monarch_ez','$ez{contact_template}')";
$sth = $dbh->prepare ($sqlstmt);
unless ($sth->execute) { print "Error: $sqlstmt $@" }

#
##############################################################################
# clean up orphaned associations
#

print "\n\tChecking tables...\n";

$sth = $dbh->prepare('show tables');
$sth->execute;
my %tables = ();
while(my @values = $sth->fetchrow_array()) {
	$tables{$values[0]} = 1;
}
$sth->finish;

#
##############################################################################
# check for and set InnoDB
#

my %table_types = ();
$sqlstmt = 'show table status';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$table_types{$values[0]} = $values[1];
}
$sth->finish;

foreach my $table (keys %table_types) {
	unless ($table_types{$table} =~ /InnoDB/i) {
		$dbh->do("ALTER TABLE $table type = 'InnoDB'");
	}
}

#
##############################################################################
# New Tables
##############################################################################
# Service Profile Host Profile
#

unless ($tables{'profile_host_profile_service'}) {
	$dbh->do("CREATE TABLE profile_host_profile_service (hostprofile_id SMALLINT(4) UNSIGNED,
			serviceprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (hostprofile_id,serviceprofile_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# Service Profile Host
#

unless ($tables{'serviceprofile_host'}) {
	$dbh->do("CREATE TABLE serviceprofile_host (serviceprofile_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (serviceprofile_id,host_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# service profile hostgroup
#

unless ($tables{'serviceprofile_hostgroup'}) {
	$dbh->do("CREATE TABLE serviceprofile_hostgroup (serviceprofile_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (serviceprofile_id,hostgroup_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# host profile overrides
#

unless ($tables{'hostprofile_overrides'}) {
	$dbh->do("CREATE TABLE hostprofile_overrides (hostprofile_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# contact overrides
#

unless ($tables{'contact_overrides'}) {
	$dbh->do("CREATE TABLE contact_overrides (contact_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			host_notification_period SMALLINT(4) UNSIGNED,
			service_notification_period SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE)	TYPE=INNODB");
}

#
##############################################################################
# contact commands overrides
#

unless ($tables{'contact_command_overrides'}) {
	$dbh->do("CREATE TABLE contact_command_overrides (contact_id SMALLINT(4) UNSIGNED,
			type VARCHAR(50),
			command_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contact_id,type,command_id),
			FOREIGN KEY (command_id) REFERENCES commands(command_id) ON DELETE CASCADE,
			FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# service name overrides
#

unless ($tables{'servicename_overrides'}) {
	$dbh->do("CREATE TABLE servicename_overrides (servicename_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# service instance
#

unless ($tables{'service_instance'}) {
	$dbh->do("CREATE TABLE service_instance (instance_id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			service_id INT(8) UNSIGNED,
			name VARCHAR(255) NOT NULL,
			status TINYINT(1) DEFAULT '0',
			arguments VARCHAR(255),
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# service name dependency
#

unless ($tables{'servicename_dependency'}) {
	$dbh->do("CREATE TABLE servicename_dependency (id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			servicename_id SMALLINT(4) UNSIGNED NOT NULL,
			depend_on_host_id INT(6) UNSIGNED,
			template SMALLINT(4) UNSIGNED NOT NULL,
			INDEX (servicename_id),
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
			FOREIGN KEY (depend_on_host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
##############################################################################
# Performance and import tables
#

unless ($tables{'import_hosts'}) {
	$dbh->do("CREATE TABLE import_hosts (import_hosts_id smallint(4) unsigned NOT NULL auto_increment,
		name varchar(255) default NULL,
		alias varchar(255) default NULL,
		address varchar(255) default NULL,
		hostprofile_id smallint(4) unsigned default NULL,
		PRIMARY KEY  (import_hosts_id),
		UNIQUE KEY name (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
}

unless ($tables{'import_services'}) {
	$dbh->do("CREATE TABLE import_services (import_services_id smallint(4) unsigned NOT NULL auto_increment,
		import_hosts_id smallint(4) unsigned default NULL,
		description varchar(255) default NULL,
		check_command_id smallint(4) unsigned default NULL,
		command_line varchar(255) default NULL,
		command_line_trans varchar(255) default NULL,
		servicename_id smallint(4) unsigned default NULL,
		serviceprofile_id smallint(4) unsigned default NULL,
		PRIMARY KEY  (import_services_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
}

unless ($tables{'datatype'}) {
	$dbh->do("CREATE TABLE datatype (datatype_id smallint(4) unsigned NOT NULL auto_increment,
		  type varchar(100) NOT NULL default '',
		  location varchar(255) NOT NULL default '',
		  PRIMARY KEY  (datatype_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
}

unless ($tables{'host_service'}) {
	$dbh->do("CREATE TABLE host_service (host_service_id smallint(4) unsigned NOT NULL auto_increment,
		host varchar(100) NOT NULL default '',
		service varchar(100) NOT NULL default '',
		label varchar(100) NOT NULL default '',
		dataname varchar(100) NOT NULL default '',
		datatype_id smallint(4) default '0',
		PRIMARY KEY  (host_service_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
}

unless ($tables{'performanceconfig'}) {
	$dbh->do("CREATE TABLE performanceconfig (
		performanceconfig_id smallint(4) unsigned NOT NULL auto_increment,
		host varchar(100) NOT NULL default '',
		service varchar(100) NOT NULL default '',
		type varchar(100) NOT NULL default '',
		enable tinyint(1) default '0',
		parseregx_first tinyint(1) default '0',
		service_regx tinyint(1) default '0',
		label varchar(100) NOT NULL default '',
		rrdname varchar(100) NOT NULL default '',
		rrdcreatestring text NOT NULL,
		rrdupdatestring text NOT NULL,
		graphcgi varchar(255) NOT NULL default '',
		perfidstring varchar(100) NOT NULL default '',
		parseregx varchar(255) NOT NULL default '',
		PRIMARY KEY  (performanceconfig_id),
		UNIQUE KEY host (host,service)) ENGINE=InnoDB DEFAULT CHARSET=latin1");

		$dbh->do("LOCK TABLES performanceconfig WRITE");
		$dbh->do("INSERT INTO performanceconfig VALUES (1,'*','UNIX_disk_ssh','nagios',1,0,0,'Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','\\(([\\d\\.]+)%\\)'),(2,'*','UNIX_memory_ssh','nagios',1,0,0,'Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','pct:\\s+([\\d\\.]+)'),(3,'*','UNIX_swap_ssh','nagios',1,0,0,'Swap Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','([\\d\\.]+)% free'),(4,'*','UNIX_load_ssh','nagios',1,0,0,'CPU Load Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','','load=([\\d\\.]+)'),(5,'*','NRPE_local_cpu','nagios',1,0,0,'NRPE Local CPU Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(6,'*','NRPE_local_disk','nagios',1,0,0,'NRPE Local Disk  Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(7,'*','NRPE_local_pagefile','nagios',1,0,0,'NRPE Local Pagefile Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(8,'*','NRPE_local_memory','nagios',1,0,0,'NRPE Local Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(9,'*','SNMP_if','nagios',1,1,1,'SNMP Interface I/O Statistics','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:in:COUNTER:900:U:U DS:out:COUNTER:900:U:U DS:indis:COUNTER:900:U:U DS:outdis:COUNTER:900:U:U DS:inerr:COUNTER:900:U:U  DS:outerr:COUNTER:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032','\$RRDTOOL\$ update \$RRDNAME\$ -t in:out:indis:outdis:inerr:outerr \$LASTCHECK\$:\$VALUE1\$:\$VALUE2\$:\$VALUE3\$:\$VALUE4\$:\$VALUE5\$:\$VALUE6\$  2>&1','/nagios/cgi-bin/if_graph2.cgi','','SNMP OK - (\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)'),(10,'*','SNMP_if_bandwidth','nagios',1,0,1,'SNMP Interface Bandwidth Statistics','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:in:COUNTER:900:U:U DS:out:COUNTER:900:U:U DS:ifspeed:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ -t in:out:ifspeed \$LASTCHECK\$:\$VALUE1\$:\$VALUE2\$:\$VALUE3\$ 2>&1','/nagios/cgi-bin/if_bandwidth_graph.cgi','','SNMP OK - (\\d+)\\s+(\\d+)\\s+(\\d+)'),(11,'*','check_swap','nagios',1,0,0,'Swap Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(12,'*','ssh_disk','nagios',1,0,0,'SSH Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:disk:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARN1\$:\$CRIT1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(13,'*','snmp_Memory_Utilization','nagios',1,0,0,'SNMP Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','','Memory \\((\\d+)%\\)'),(14,'*','smtp_port','nagios',1,0,0,'SMTP Port Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(18,'*','check_cpu','nagios',1,0,0,'CPU Load Graph','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:user:GAUGE:900:U:U DS:nice:GAUGE:900:U:U DS:sys:GAUGE:900:U:U DS:idle:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ -t user:nice:sys:idle \$LASTCHECK\$:\$VALUE1\$:\$VALUE2\$:\$VALUE3\$:\$VALUE4\$','/nagios/cgi-bin/sar_cpu_graph.cgi','',''),(19,'*','check_mem','nagios',1,0,0,'Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(21,'*','check_wmi_cpu','nagios',1,0,0,'WMI CPU Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:cpu:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARNING1\$:\$CRITICAL1\$ 2>&1','/nagios/cgi-bin/wmi_cpu_graph.cgi','',''),(22,'*','check_wmi_disk','nagios',1,0,0,'WMI Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:disk:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARNING1\$:\$CRITICAL1\$ 2>&1','/nagios/cgi-bin/wmi_disk_graph.cgi','',''),(23,'*','check_wmi_printque','nagios',1,0,0,'WMI Print Queue','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:jobs:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARNING1\$:\$CRITICAL1\$ 2>&1','/nagios/cgi-bin/wmi_printque_graph.cgi','',''),(24,'*','check_wmi_mem','nagios',1,0,0,'WMI Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:mem:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARNING1\$:\$CRITICAL1\$ 2>&1','/nagios/cgi-bin/wmi_mem_graph.cgi','',''),(25,'*','check_wmi_swap','nagios',1,0,0,'WMI Swap','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:swap:GAUGE:900:U:U DS:warning:GAUGE:900:U:U DS:critical:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$WARNING1\$:\$CRITICAL1\$ 2>&1','/nagios/cgi-bin/wmi_swap_graph.cgi','',''),(26,'*','ssh_load','nagios',1,0,0,'SSH CPU Load','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:1min:GAUGE:900:U:U DS:5min:GAUGE:900:U:U DS:15min:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ -t 1min:5min:15min \$LASTCHECK\$:\$VALUE1\$:\$VALUE2\$:\$VALUE3\$ 2>&1','/nagios/cgi-bin/load_graph.cgi','',''),(36,'*','NRPE_local_swap','nagios',1,0,0,'NRPE Local Swap Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(38,'*','Tcp_Ssh','nagios',1,0,0,'SSH Port Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(40,'*','wmi_cpu','nagios',1,0,0,'WMI CPU Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','CPU Utilization ([\\d\\.]+)%'),(41,'*','wmi_disk','nagios',1,0,0,'WMI Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','Disk Utilization ([\\d\\.]+)%'),(42,'*','wmi_mem','nagios',1,0,0,'WMI Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','Memory Utilization ([\\d\\.]+)%'),(43,'*','Local_MySql_Engine','nagios',1,0,0,'Local MySQL Engine - Queries per second average','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','','Queries per second avg: ([\\d\\.]+)'),(44,'*','Local_Disk','nagios',1,0,1,'Local Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(45,'*','local_procs','nagios',1,1,0,'Number of Local Processes','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','','(\\d+) processes'),(46,'*','Local_Users','nagios',1,0,0,'Number of Local Users','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','','(\\d+) users'),(49,'*','GENERIC_NUMBER','nagios',1,0,0,'GENERIC_NUMBER','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(50,'*','GENERIC_PERCENT','nagios',1,0,0,'GENERIC_PERCENT','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','',''),(55,'*','DNS_Expect','nagios',1,0,0,'DNS Expect Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(56,'*','DNS_Server','nagios',1,0,0,'DNS Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(57,'*','Local_load','nagios',1,0,0,'Local CPU Load','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:load1:GAUGE:900:U:U DS:load5:GAUGE:900:U:U DS:load15:GAUGE:900:U:U  RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$:\$VALUE2\$:\$VALUE3\$ 2>&1','/nagios/cgi-bin/unixload_graph.cgi','',''),(58,'*','local_nagios_latency','nagios',1,0,0,'Local Nagios Service Check Average Latency (seconds)','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(59,'*','local_mem','nagios',1,1,0,'Local Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:percent:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/percent_graph.cgi','','([\\d\\.]+)%'),(60,'*','LDAP_Server','nagios',1,0,0,'LDAP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(61,'*','Local_swap','nagios',1,1,0,'Local Swap Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','','([\\d\\.]+)% free'),(62,'*','FTP_Alive','nagios',1,0,1,'FTP Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(63,'*','FTP_Server','nagios',1,0,1,'FTP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(64,'*','HTTP_Alive','nagios',1,0,0,'HTTP Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(65,'*','HTTPS_Alive','nagios',1,0,0,'HTTPS Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(66,'*','HTTPS_Server','nagios',1,0,0,'HTTPS Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(67,'*','HTTP_Server','nagios',1,0,0,'HTTP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(68,'*','IMAP_Server','nagios',1,0,0,'IMAP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(69,'*','IMAPS_Server','nagios',1,0,0,'IMAPS Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(70,'*','IMAPS_Alive','nagios',1,0,0,'IMAPS Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(71,'*','IMAP_Alive','nagios',1,0,0,'IMAP Alive Response Time ','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(72,'*','NNTP_Alive','nagios',1,0,0,'NNTP Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(73,'*','NNTPS_Alive','nagios',1,0,0,'NNTPS Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(74,'*','NNTPS_Server','nagios',1,0,0,'NNTPS Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(75,'*','NNTP_Server','nagios',1,0,0,'NNTP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(77,'*','NRPE_Alive','nagios',1,0,0,'NRPE Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/number_graph.cgi','',''),(78,'*','POP3_Alive','nagios',1,0,0,'POP3 Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(79,'*','POP3S_Alive','nagios',1,0,0,'POP3S Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(80,'*','POP3S_Server','nagios',1,0,0,'POP3S Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(81,'*','POP3_Server','nagios',1,0,0,'POP3 Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(82,'*','SMTP_Server','nagios',1,0,0,'SMTP Server Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(83,'*','SMTP_Alive','nagios',1,0,0,'SMTP Alive Server','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(84,'*','GENERIC_LABEL','nagios',1,0,0,'GENERIC_LABEL','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(85,'*','wmi_mssql_disk_transfers','nagios',1,0,0,'WMI MsSql Disk Transfers','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(86,'*','iis_bytes_received','nagios',1,0,1,'IIS Bytes Received','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(87,'*','iis_bytes_sent','nagios',1,0,1,'IIS Bytes Sent','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(88,'*','iis_bytes_total','nagios',1,0,1,'IIS Bytes Total','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(89,'*','iis_current_connections','nagios',1,0,1,'IIS Current Connections','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(90,'*','iis_current_nonanonymous_users','nagios',1,0,1,'IIS Current Non-anonymous users','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:CurNonAnonUsers:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(91,'*','iis_get_requests','nagios',1,0,1,'IIS Get Requests','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(92,'*','iis_maximum_connections','nagios',1,0,1,'IIS Maximimum Connections','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(93,'*','iis_post_requests','nagios',1,0,1,'IIS Post Requests','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(94,'*','iis_private_bytes','nagios',1,0,1,'IIS Private Bytes','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(95,'*','iis_total_not_found_errors','nagios',1,0,1,'IIS Total Not Found Errors','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(96,'*','nrpe_mem','nagios',1,0,1,'NRPE Memory Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(97,'*','DNS_Alive_TCP','nagios',1,0,0,'DNS Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(98,'*','Host_Alive','nagios',1,0,0,'Host Alive Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(99,'*','Current_Load','nagios',1,0,0,'Current Load - 15 Minute Average','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL3\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE3\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(100,'*','Current_Users','nagios',1,0,0,'Current_Users','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:\$LABEL1\$:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','',''),(101,'*','PING','nagios',1,1,0,'Ping Response Time','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:ping:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','','RTA = ([\\d\\.]+) ms'),(102,'*','Root_Partition','nagios',1,0,0,'Disk Utilization','/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd','\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:root:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640','\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1','/nagios/cgi-bin/label_graph.cgi','','')");
		$dbh->do("UNLOCK TABLES");
}

#
##############################################################################
# Groups
#
unless ($tables{'monarch_groups'}) {
	$dbh->do("CREATE TABLE monarch_groups (group_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description VARCHAR(255),
			location TEXT,
			status TINYINT(1) DEFAULT '0',
			data TEXT) TYPE=INNODB");
}
unless ($tables{'monarch_macros'}) {
	$dbh->do("CREATE TABLE monarch_macros (macro_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			value VARCHAR(255),
			description VARCHAR(255)) TYPE=INNODB");
}

unless ($tables{'monarch_group_host'}) {
	$dbh->do("CREATE TABLE monarch_group_host (group_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (group_id,host_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'monarch_group_hostgroup'}) {
	$dbh->do("CREATE TABLE monarch_group_hostgroup (group_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,hostgroup_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'monarch_group_child'}) {
	$dbh->do("CREATE TABLE monarch_group_child (group_id SMALLINT(4) UNSIGNED,
			child_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,child_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (child_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE) TYPE=INNODB");

}
unless ($tables{'monarch_group_macro'}) {
	$dbh->do("CREATE TABLE monarch_group_macro (group_id SMALLINT(4) UNSIGNED,
			macro_id SMALLINT(4) UNSIGNED,
			value VARCHAR(255),
			PRIMARY KEY (group_id,macro_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (macro_id) REFERENCES monarch_macros(macro_id) ON DELETE CASCADE) TYPE=INNODB");

}
unless ($tables{'monarch_group_props'}) {
	$dbh->do("CREATE TABLE monarch_group_props (prop_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			group_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			type VARCHAR(20),
			value VARCHAR(255),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'sessions'}) {
	$dbh->do("CREATE TABLE sessions (id CHAR(32) NOT NULL UNIQUE,
			a_session TEXT NOT NULL)");
}


#
# Tables to support integration with other tools 2007-Jan-16
#

unless ($tables{'import_schema'}) {
	$dbh->do("CREATE TABLE import_schema (schema_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			delimiter VARCHAR(50),
			description TEXT,
			type VARCHAR(255),
			sync_object varchar(50),
			smart_name TINYINT(1) DEFAULT '0',
			hostprofile_id SMALLINT(4) UNSIGNED DEFAULT '0',
			data_source VARCHAR(255),
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_column'}) {
	$dbh->do("CREATE TABLE import_column (column_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			schema_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			position SMALLINT(4) UNSIGNED,
			delimiter VARCHAR(50),
			FOREIGN KEY (schema_id) REFERENCES import_schema(schema_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match'}) {
	$dbh->do("CREATE TABLE import_match (match_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			column_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			match_order SMALLINT(4) UNSIGNED,
			match_type VARCHAR(255),
			match_string VARCHAR(255),
			rule VARCHAR(255),
			object VARCHAR(255),
			hostprofile_id SMALLINT(4) UNSIGNED,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (column_id) REFERENCES import_column(column_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match_parent'}) {
	$dbh->do("CREATE TABLE import_match_parent (match_id SMALLINT(4) UNSIGNED,
			parent_id INT(6) UNSIGNED,
			PRIMARY KEY (match_id,parent_id),
			FOREIGN KEY (parent_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match_hostgroup'}) {
	$dbh->do("CREATE TABLE import_match_hostgroup (match_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,hostgroup_id),
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match_group'}) {
	$dbh->do("CREATE TABLE import_match_group (match_id SMALLINT(4) UNSIGNED,
			group_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,group_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match_contactgroup'}) {
	$dbh->do("CREATE TABLE import_match_contactgroup (match_id SMALLINT(4) UNSIGNED,
			contactgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,contactgroup_id),
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'import_match_serviceprofile'}) {
	$dbh->do("CREATE TABLE import_match_serviceprofile (match_id SMALLINT(4) UNSIGNED,
			serviceprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,serviceprofile_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB");
}

#
# Tables to support autodiscovery 2007-Sep-18
#


unless ($tables{'discover_group'}) {
	$dbh->do("CREATE TABLE discover_group (group_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description TEXT,
			config TEXT,
			schema_id SMALLINT(4) UNSIGNED,
			FOREIGN KEY (schema_id) REFERENCES import_schema(schema_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'discover_method'}) {
	$dbh->do("CREATE TABLE discover_method (method_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description TEXT,
			config TEXT,
			type VARCHAR(50)) TYPE=INNODB");
}

unless ($tables{'discover_filter'}) {
	$dbh->do("CREATE TABLE discover_filter (filter_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			type VARCHAR(50),
			filter TEXT) TYPE=INNODB");

}

unless ($tables{'discover_group_filter'}) {
	$dbh->do("CREATE TABLE discover_group_filter (group_id SMALLINT(4) UNSIGNED,
			filter_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,filter_id),
			FOREIGN KEY (group_id) REFERENCES discover_group(group_id) ON DELETE CASCADE,
			FOREIGN KEY (filter_id) REFERENCES discover_filter(filter_id) ON DELETE CASCADE) TYPE=INNODB");
}

unless ($tables{'discover_group_method'}) {
	$dbh->do("CREATE TABLE discover_group_method (group_id SMALLINT(4) UNSIGNED,
			method_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,method_id),
			FOREIGN KEY (method_id) REFERENCES discover_method(method_id) ON DELETE CASCADE,
			FOREIGN KEY (group_id) REFERENCES discover_group(group_id) ON DELETE CASCADE) TYPE=INNODB");
}


unless ($tables{'discover_method_filter'}) {
	$dbh->do("CREATE TABLE discover_method_filter (method_id SMALLINT(4) UNSIGNED,
			filter_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (method_id,filter_id),
			FOREIGN KEY (method_id) REFERENCES discover_method(method_id) ON DELETE CASCADE,
			FOREIGN KEY (filter_id) REFERENCES discover_filter(filter_id) ON DELETE CASCADE) TYPE=INNODB");
}


#
##############################################################################

$dbh->do("delete from profile_host_profile_service where serviceprofile_id not in (select serviceprofile_id from profiles_service)");
$dbh->do("delete from profile_host_profile_service where hostprofile_id not in (select hostprofile_id from profiles_host)");
$dbh->do("delete from serviceprofile_host where host_id not in (select host_id from hosts)");
$dbh->do("delete from serviceprofile where serviceprofile_id not in (select serviceprofile_id from profiles_service)");
$dbh->do("delete from serviceprofile where servicename_id not in (select servicename_id from profiles_service)");
$dbh->do("delete from escalation_tree_template where template_id not in (select template_id from escalation_templates)");
$dbh->do("delete from escalation_tree_template where tree_id not in (select tree_id from escalation_trees)");
$dbh->do("delete from tree_template_contactgroup where tree_id not in (select tree_id from escalation_trees)");
$dbh->do("delete from tree_template_contactgroup where contactgroup_id not in (select contactgroup_id from contactgroups)");
$dbh->do("delete from tree_template_contactgroup where template_id not in (select template_id from escalation_templates)");

#
# Modify Existing Tables
#
##############################################################################
# Change column types to text
#

my %table_text = ('services' => 'command_line','service_names' => 'command_line','setup' => 'value');
foreach my $table (keys %table_text) {
	$sqlstmt = "describe $table";
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %fields = ();
	while(my @values = $sth->fetchrow_array()) {
		$fields{$values[0]} = $values[1];
	}
	$sth->finish;
	unless ($fields{$table_text{$table}} =~ /text/i) {
		$dbh->do("ALTER TABLE $table MODIFY $table_text{$table} TEXT");
	}
}

#
##############################################################################
# Convert to smallint
#

my %table_int = ('time_periods' => 'timeperiod_id','extended_service_info_templates' => 'serviceextinfo_id','extended_host_info_templates' => 'hostextinfo_id');
foreach my $table (keys %table_int) {
	$sqlstmt = "describe $table";
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %fields = ();
	while(my @values = $sth->fetchrow_array()) {
		$fields{$values[0]} = $values[1];
	}
	$sth->finish;
	unless ($fields{$table_int{$table}} =~ /smallint/i) {
		$dbh->do("ALTER TABLE $table MODIFY $table_int{$table} SMALLINT(4) UNSIGNED AUTO_INCREMENT");
	}
}


#
##############################################################################
# Change names to varchar 255
#

foreach my $table (keys %tables) {
	$sqlstmt = "describe $table";
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %fields = ();
	while(my @values = $sth->fetchrow_array()) {
		$fields{$values[0]} = $values[1];
	}
	$sth->finish;
	if ($fields{'name'}) {
		unless ($fields{'name'} =~ /varchar\(255\)/i) {
			$dbh->do("ALTER TABLE $table MODIFY name varchar(255)");
		}
	}
	if ($fields{'alias'}) {
		unless ($fields{'alias'} =~ /varchar\(255\)/i) {
			$dbh->do("ALTER TABLE $table MODIFY alias varchar(255)");
		}
	}
	if ($fields{'address'}) {
		unless ($fields{'address'} =~ /varchar\(255\)/i) {
			$dbh->do("ALTER TABLE $table MODIFY name varchar(255)");
		}
	}
}

#
##############################################################################
# Change contacts column pager and email to text
#

$sqlstmt = "describe contacts";
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = $values[1];
}
$sth->finish;
if ($fields{'pager'}) {
	unless ($fields{'pager'} =~ /TEXT/i) {
		$dbh->do("ALTER TABLE contacts MODIFY pager TEXT");
	}
}

if ($fields{'email'}) {
	unless ($fields{'email'} =~ /TEXT/i) {
		$dbh->do("ALTER TABLE contacts MODIFY email TEXT");
	}
}

#
##############################################################################
# Change users session to varchar
#

$sqlstmt = "describe users";
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = $values[1];
}
$sth->finish;
if ($fields{'session'}) {
	unless ($fields{'pager'} =~ /int/i) {
		$dbh->do("ALTER TABLE users MODIFY session varchar(255)");
	}
}


#
# Update service templates
#

$sqlstmt = "describe service_templates";
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = $values[1];
}
$sth->finish;
unless ($fields{'command_line'}) {
	$dbh->do("ALTER TABLE service_templates add command_line TEXT after check_command");
	my $sqlstmt = "select servicetemplate_id, data from service_templates";
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @values = $sth->fetchrow_array()) {
		my %data = parse_xml($values[1]);
		my $xml = qq(<?xml version="1.0" ?>
<data>);
		my $command_line = '';
		foreach my $name (keys %data) {

			if ($name eq 'command_line') {
				$command_line = $data{$name};
			} else {
				$xml .= qq(
  <prop name="$name"><![CDATA[$data{$name}]]>
  </prop>);
			}
		}
		$xml .= "\n</data>";
		$command_line = $dbh->quote($command_line);
		$xml = $dbh->quote($xml);
		$dbh->do("update service_templates set command_line = $command_line, data = $xml where servicetemplate_id = $values[0]");
	}
	$sth->finish;
}




# Add check_period for 2.0
$sqlstmt = 'describe host_templates';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;
unless (defined $fields{'check_period'}) {
	$dbh->do('alter table host_templates add check_period SMALLINT(4) UNSIGNED after name');
}



my $sqlstmt = "select value from setup where name = 'monarch_version'";
my ($monarch_ver) = $dbh->selectrow_array($sqlstmt);
unless ($monarch_ver) {
	print "\n\tConverting passwords...\n";
	$sqlstmt = 'select user_id, password from users';
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %fields = ();
	while(my @values = $sth->fetchrow_array()) {
		$fields{$values[0]} = 1;
	}
	$sth->finish;

	# encrypt passwords
	$sqlstmt = "select user_acct, password from users";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	%fields = ();
	while(my @values = $sth->fetchrow_array()) {
		$fields{$values[0]} = $values[1];
	}
	$sth->finish;
	foreach my $uid (keys %fields) {
		if ($uid eq 'super_user') { $fields{$uid} = 'password' }
		my @saltchars = ('a'..'z','A'..'Z','0'..'9',',','/');
		srand(time() ^ ($$ + ($$ << 15)));
		my $salt = $saltchars[int(rand(64))];
		$salt .= $saltchars[int(rand(64))];
		my $newpw = crypt($fields{$uid}, $salt);
		my $sql = "update users set password = '$newpw' where user_acct = '$uid'";
		$sth = $dbh->prepare($sqlstmt);
		$sth->execute();
		$sth->finish;
	}
}

#
#############################################
# Host_overrides changes
#

my $sqlstmt = 'describe host_overrides';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;


#
#  add check_period
#

unless (defined $fields{'check_period'}) {
	print "\n\tUpdating table host_overrides...\n";
	$dbh->do('alter table host_overrides add check_period SMALLINT(4) UNSIGNED after host_id');
}

#
#  drop status
#

if (defined $fields{'status'}) {
	$dbh->do("ALTER TABLE host_overrides drop column status");
}


$sqlstmt = 'describe service_overrides';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
%fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;


#
#  drop check_command
#

if (defined $fields{'check_command'}) {
	print "\n\tUpdating table service_overrides...\n";
	$dbh->do("ALTER TABLE service_overrides drop column check_command");
}

#
#  drop status
#

if (defined $fields{'status'}) {
	$dbh->do("ALTER TABLE service_overrides drop column status");
}

#
# Update access list for super user group
#

my $super_gid = $dbh->selectrow_array("select usergroup_id from user_groups where name = 'super_users'");

$sqlstmt = "select * from access_list where usergroup_id = '$super_gid'";
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %sgid_assets = ();
while(my @values = $sth->fetchrow_array()) {
	$sgid_assets{$values[0]}{$values[1]}{$values[3]} = 1;
}
$sth->finish;


#
# service groups
#

unless ($sgid_assets{'servicegroups'}) {
	$dbh->do("insert into access_list values('servicegroups','design_manage','$super_gid','add,modify,delete')");
}

#
# Add externals to asset list
#

unless ($sgid_assets{'externals'}) {
	$dbh->do("insert into access_list values('externals','design_manage','$super_gid','add,modify,delete')");
}

#
# Add host delete tool to asset list
#

unless ($sgid_assets{'host_delete_tool'}) {
	$dbh->do("insert into access_list values('host_delete_tool','tools','$super_gid','add,modify,delete')");
}

#
# Add service delete tool to asset list
#

unless ($sgid_assets{'service_delete_tool'}) {
	$dbh->do("insert into access_list values('service_delete_tool','tools','$super_gid','add,modify,delete')");
}

my @ez_list = ('ez_enabled','main_ez','ez_hosts','ez_host_groups','ez_profiles','ez_notifications','ez_commit','ez_setup','ez_discover','ez_import');
foreach my $ez (@ez_list) {
	unless ($sgid_assets{$ez}) { $dbh->do("insert into access_list values('$ez','ez','$super_gid','$ez')") }
}

#
# Add group macros to asset list
#

unless ($sgid_assets{'manage'}{'group_macro'}) {
	$dbh->do("insert into access_list values('manage','group_macro','$super_gid','manage')");
}

#
# Add servicename_id and arguments to import_match
#

$sqlstmt = 'describe import_match';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
%fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

unless (defined $fields{'servicename_id'}) {
	print "\n\tUpdating table import_match...\n";
	$dbh->do('alter table import_match add servicename_id SMALLINT(4) UNSIGNED after hostprofile_id');
	$dbh->do("alter table import_match add FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE SET NULL");
	$dbh->do('alter table import_match add arguments VARCHAR(255) after servicename_id');

}


###################################################
# Escalations
#

my $sqlstmt = 'describe escalation_templates';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

my @table_info = $dbh->selectrow_array("show create table escalation_templates");
if (defined $fields{'servicename_id'}) {
	my %tree_template_contactgroup = ();
	$sqlstmt = "select * from tree_template_contactgroup";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @vals = $sth->fetchrow_array()) { $tree_template_contactgroup{"$vals[0]-$vals[1]-$vals[2]"} = 1 }
	$sth->finish;
	$sqlstmt = "select servicename_id from service_names where name = '*'";
	my $splat_id = $dbh->selectrow_array($sqlstmt);
	$sqlstmt = "select template_id, servicename_id from escalation_templates";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @temp = $sth->fetchrow_array()) {
		my @template_contactgroups = ();
		$sqlstmt = "select contactgroup_id from contactgroup_assign where object = '$temp[0]' and type like '%escalation%'";
		my $sth2 = $dbh->prepare($sqlstmt);
		$sth2->execute();
		while(my @cg = $sth2->fetchrow_array()) { push @template_contactgroups, $cg[0] }
		$sth2->finish;
		$sqlstmt = "select tree_id from escalation_tree_template where template_id = '$temp[0]'";
		$sth2 = $dbh->prepare($sqlstmt);
		$sth2->execute();
		while(my @tree = $sth2->fetchrow_array()) {
			foreach my $cg (@template_contactgroups) {
				unless ($tree_template_contactgroup{"$tree[0]-$temp[1]-$cg"}) {
					$dbh->do("insert into tree_template_contactgroup values('$tree[0]','$temp[0]','$cg')");
				}
			}
			if ($temp[1] && $temp[1] ne $splat_id) {
				my @hosts = ();
				$sqlstmt = "select host_id from hosts where host_id in (select host_id from hostgroup_host left join hostgroups on hostgroup_host.hostgroup_id = hostgroups.hostgroup_id where hostgroups.service_escalation_id = '$tree[0]')";
				my $sth3 = $dbh->prepare($sqlstmt);
				$sth3->execute();
				while(my @host = $sth3->fetchrow_array()) { push @hosts, $host[0] }
				$sth3->finish;
				$sqlstmt = "select host_id from hosts where service_escalation_id = '$tree[0]'";
				$sth3 = $dbh->prepare($sqlstmt);
				$sth3->execute();
				while(my @host = $sth3->fetchrow_array()) { push @hosts, $host[0] }
				$sth3->finish;
				$dbh->do("update hostgroups set service_escalation_id = NULL where service_escalation_id =  '$tree[0]'");
				$dbh->do("update hosts set service_escalation_id = NULL where service_escalation_id = '$tree[0]'");
				foreach my $hid (@hosts) {
					$dbh->do("update services set escalation_id = '$tree[0]' where host_id = '$hid' and servicename_id = '$temp[1]'");
				}
			}
		}
		$sth2->finish;
	}
	$sth->finish;
	if ($table_info[1] =~ /CONSTRAINT\s*.(escalation_templates_\S+_\d+).\s+FOREIGN KEY\s+\(.servicename_id.\)/) {
		$dbh->do("alter table escalation_templates drop foreign key $1");
	}

	$dbh->do("ALTER TABLE escalation_templates drop column servicename_id");
	$dbh->do("delete from contactgroup_assign where type like '%escalation%'");
}

#
##############################################################################
# Convert contactgroup_assign

if ( $tables{'contactgroup_assign'} ) {

	# create new associative tables:

	# contactgroup_host
	$dbh->do("CREATE TABLE contactgroup_host (contactgroup_id SMALLINT(4) UNSIGNED,
		host_id INT(6) UNSIGNED,
		PRIMARY KEY (contactgroup_id,host_id),
		FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_service
	$dbh->do("CREATE TABLE contactgroup_service (contactgroup_id SMALLINT(4) UNSIGNED,
		service_id INT(8) UNSIGNED,
		PRIMARY KEY (contactgroup_id,service_id),
		FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_host_template
	$dbh->do("CREATE TABLE contactgroup_host_template (contactgroup_id SMALLINT(4) UNSIGNED,
		hosttemplate_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,hosttemplate_id),
		FOREIGN KEY (hosttemplate_id) REFERENCES host_templates(hosttemplate_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_service_template
	$dbh->do("CREATE TABLE contactgroup_service_template (contactgroup_id SMALLINT(4) UNSIGNED,
		servicetemplate_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,servicetemplate_id),
		FOREIGN KEY (servicetemplate_id) REFERENCES service_templates(servicetemplate_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_host_profile
	$dbh->do("CREATE TABLE contactgroup_host_profile (contactgroup_id SMALLINT(4) UNSIGNED,
		hostprofile_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,hostprofile_id),
		FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_service_name
	$dbh->do("CREATE TABLE contactgroup_service_name (contactgroup_id SMALLINT(4) UNSIGNED,
		servicename_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,servicename_id),
		FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_hostgroup
	$dbh->do("CREATE TABLE contactgroup_hostgroup (contactgroup_id SMALLINT(4) UNSIGNED,
		hostgroup_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,hostgroup_id),
		FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	# contactgroup_group
	$dbh->do("CREATE TABLE contactgroup_group (contactgroup_id SMALLINT(4) UNSIGNED,
		group_id SMALLINT(4) UNSIGNED,
		PRIMARY KEY (contactgroup_id,group_id),
		FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
		FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

	my %table_by_object = (
		'hosts'    			=> 'contactgroup_host',
		'monarch_group'    	=> 'contactgroup_group',
		'services'          => 'contactgroup_service',
		'host_templates'  	=> 'contactgroup_host_template',
		'service_templates' => 'contactgroup_service_template',
		'host_profiles'     => 'contactgroup_host_profile',
		'service_names'   	=> 'contactgroup_service_name',
		'hostgroups'       	=> 'contactgroup_hostgroup',
	);

	my %contactgroup_assign = ();
	$sqlstmt = "select * from contactgroup_assign";
	$sth     = $dbh->prepare($sqlstmt);
	$sth->execute();
	while ( my @values = $sth->fetchrow_array() ) {
		$contactgroup_assign{ $values[1] }{ $values[2] }{ $values[0] } = 1;
	}
	$sth->finish;
	my %objects = ();
	my @tables  = (
		'hosts',             'services',
		'hostgroups',        'contactgroups',
		'profiles_host',     'host_templates',
		'service_templates', 'service_names'
	);
	foreach my $table (@tables) {
		$sqlstmt = "select * from $table";
		$sth     = $dbh->prepare($sqlstmt);
		$sth->execute();
		while ( my @values = $sth->fetchrow_array() ) {
			$objects{$table}{ $values[0] } = 1;
		}
		$sth->finish;
	}

	# migrate objects to new tables
	foreach my $type ( keys %contactgroup_assign ) {
		my $table_name = $type;
		if ( $table_name eq 'host_profiles' ) { $table_name = 'profiles_host' }
		foreach my $oid ( keys %{ $contactgroup_assign{$type} } ) {
			if ( $objects{$table_name}{$oid} ) {
				foreach my $cgid ( keys %{ $contactgroup_assign{$type}{$oid} } )
				{
					if ( $objects{'contactgroups'}{$cgid} ) {
						$dbh->do("insert into $table_by_object{$type} values($cgid,$oid)");
					}
				}
			}
		}
	}

	# Drop contactgroup_assign table
	$dbh->do("drop table contactgroup_assign");


}

#
##############################################################################

$dbh->do("update escalation_templates set type = 'host' where type = 'hostgroup'");
$dbh->do("update escalation_trees set type = 'host' where type = 'hostgroup'");


unless (defined $fields{'escalation_period'}) {
	$dbh->do("ALTER TABLE escalation_templates add escalation_period SMALLINT(4) UNSIGNED after comment");
}
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.escalation_period.\)\s*REFERENCES\s+.time_periods.\s*\(.timeperiod_id.\)\s+ON DELETE SET NULL/i) {
	$dbh->do("ALTER TABLE escalation_templates add FOREIGN KEY (escalation_period) REFERENCES time_periods(timeperiod_id) ON DELETE SET NULL");
}


@table_info = $dbh->selectrow_array("show create table escalation_tree_template");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.template_id.\)\s*REFERENCES\s+.escalation_templates.\s*\(.template_id.\)\s+ON DELETE CASCADE/i) {
	$dbh->do("delete from escalation_tree_template where template_id not in (select template_id from escalation_templates)");
	$dbh->do("ALTER TABLE escalation_tree_template add FOREIGN KEY (template_id) REFERENCES escalation_templates(template_id) ON DELETE CASCADE");
	$dbh->do("delete from escalation_tree_template where tree_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE escalation_tree_template add FOREIGN KEY (tree_id) REFERENCES escalation_trees(tree_id) ON DELETE CASCADE");
}

@table_info = $dbh->selectrow_array("show create table tree_template_contactgroup");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.contactgroup_id.\)\s*REFERENCES\s+.contactgroups.\s*\(.contactgroup_id.\)\s+ON DELETE CASCADE/i) {
	$dbh->do("delete from tree_template_contactgroup where contactgroup_id not in (select contactgroup_id from contactgroups)");
	$dbh->do("ALTER TABLE tree_template_contactgroup add FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE");
	$dbh->do("delete from tree_template_contactgroup where template_id not in (select template_id from escalation_templates)");
	$dbh->do("ALTER TABLE tree_template_contactgroup add FOREIGN KEY (template_id) REFERENCES escalation_templates(template_id) ON DELETE CASCADE");
	$dbh->do("delete from tree_template_contactgroup where tree_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE tree_template_contactgroup add FOREIGN KEY (tree_id) REFERENCES escalation_trees(tree_id) ON DELETE CASCADE");
}



###################################################
# host

my $sqlstmt = 'describe hosts';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

if (defined $fields{'serviceprofile_id'}) {
	my $sqlstmt = "select host_id, serviceprofile_id from hosts";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @values = $sth->fetchrow_array()) {
		if ($values[1]) { $dbh->do("insert into serviceprofile_host values('$values[1]','$values[0]')") }
	}
	$sth->finish;
	$dbh->do("ALTER TABLE hosts drop column serviceprofile_id");
}
@table_info = $dbh->selectrow_array("show create table hosts");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.hostextinfo_id.\)\s*REFERENCES\s+.extended_host_info_templates.\s*\(.hostextinfo_id.\)\s+ON DELETE SET NULL/i) {
	$dbh->do("update hosts set hostextinfo_id = NULL where hostextinfo_id not in (select hostextinfo_id from extended_host_info_templates)");
	$dbh->do("ALTER TABLE hosts add FOREIGN KEY (hostextinfo_id) REFERENCES extended_host_info_templates (hostextinfo_id) ON DELETE SET NULL");
	$dbh->do("update hosts set hostprofile_id = NULL where hostprofile_id not in (select hostprofile_id from profiles_host)");
	$dbh->do("ALTER TABLE hosts add FOREIGN KEY (hostprofile_id) REFERENCES profiles_host (hostprofile_id) ON DELETE SET NULL");
	$dbh->do("update hosts set host_escalation_id = NULL where host_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE hosts add FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
	$dbh->do("update hosts set service_escalation_id = NULL where service_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE hosts add FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees (tree_id) ON DELETE SET NULL");
}

###################################################
# hostgroup

my $sqlstmt = 'describe hostgroups';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

if (defined $fields{'hostgroup_escalation_id'}) {
	my $sqlstmt = 'select hostgroup_id, hostgroup_escalation_id, host_escalation_id from hostgroups';
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @values = $sth->fetchrow_array()) {
		unless ($values[2]) { $dbh->do("update hostgroups set host_escalation_id = '$values[1]' where hostgroup_id = '$values[0]'") }
	}
	$sth->finish;
	$dbh->do("ALTER TABLE hostgroups drop column hostgroup_escalation_id");
}

unless (defined $fields{'hostprofile_id'}) {
	$dbh->do("ALTER TABLE hostgroups add hostprofile_id SMALLINT(4) UNSIGNED after alias");
}

@table_info = $dbh->selectrow_array("show create table hostgroups");

unless ($table_info[1] =~ /FOREIGN KEY\s*\(.host_escalation_id.\)\s*REFERENCES\s+.escalation_trees.\s*\(.tree_id.\)\s+ON DELETE SET NULL/) {
	$dbh->do("ALTER TABLE hostgroups add FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE SET NULL");
	$dbh->do("update hostgroups set host_escalation_id = NULL where host_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE hostgroups add FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
	$dbh->do("update hostgroups set service_escalation_id = NULL where service_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE hostgroups add FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
}

###################################################
# servicegroup

my $sqlstmt = 'describe servicegroups';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

unless (defined $fields{'escalation_id'}) {
	$dbh->do("ALTER TABLE servicegroups add escalation_id SMALLINT(4) UNSIGNED after alias");
}
@table_info = $dbh->selectrow_array("show create table servicegroups");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.escalation_id.\)\s*REFERENCES\s+.escalation_trees.\s*\(.tree_id.\)\s+ON DELETE SET NULL/) {
	$dbh->do("ALTER TABLE servicegroups add FOREIGN KEY (escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
}


###################################################
# service


@table_info = $dbh->selectrow_array("show create table services");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.escalation_id.\)\s*REFERENCES\s+.escalation_trees.\s*\(.tree_id.\)\s+ON DELETE SET NULL/) {
	$dbh->do("update services set serviceextinfo_id = NULL where serviceextinfo_id not in (select serviceextinfo_id from extended_service_info_templates)");
	$dbh->do("ALTER TABLE services add FOREIGN KEY (serviceextinfo_id) REFERENCES extended_service_info_templates(serviceextinfo_id) ON DELETE SET NULL");
	$dbh->do("update services set escalation_id = NULL where escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE services add FOREIGN KEY (escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
}

###################################################
# Service names
#


my $sqlstmt = 'describe service_names';
my $sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;

unless ($fields{'data'}) {
	$dbh->do("ALTER TABLE service_names add data TEXT after extinfo");
}

if (defined $fields{'dependency'}) {
	$sqlstmt = 'select servicename_id, dependency from service_names';
	my $sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @values = $sth->fetchrow_array()) {
		if ($values[1]) { $dbh->do("insert into servicename_dependency values(NULL,'$values[0]',NULL,'$values[1]')") }
	}
	$sth->finish;
	$dbh->do("ALTER TABLE service_names drop column dependency");
}

@table_info = $dbh->selectrow_array("show create table service_names");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.escalation.\)\s*REFERENCES\s+.escalation_trees.\s*\(.tree_id.\)\s+ON DELETE SET NULL/) {
	$dbh->do("update service_names set extinfo = NULL where extinfo not in (select serviceextinfo_id from extended_service_info_templates)");
	$dbh->do("ALTER TABLE service_names add FOREIGN KEY (extinfo) REFERENCES extended_service_info_templates(serviceextinfo_id) ON DELETE SET NULL");
	$dbh->do("update service_names set escalation = NULL where escalation not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE service_names add FOREIGN KEY (escalation) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
}

###################################################
# Host Profiles
#

@table_info = $dbh->selectrow_array("show create table profiles_host");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.host_extinfo_id.\)\s*REFERENCES\s+.extended_host_info_templates.\s*\(.hostextinfo_id.\)\s+ON DELETE SET NULL/) {
	$dbh->do("update profiles_host set host_extinfo_id = NULL where host_extinfo_id not in (select hostextinfo_id from extended_host_info_templates)");
	$dbh->do("ALTER TABLE profiles_host add FOREIGN KEY (host_extinfo_id) REFERENCES extended_host_info_templates(hostextinfo_id) ON DELETE SET NULL");
	$dbh->do("update profiles_host set host_escalation_id = NULL where host_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE profiles_host add FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
	$dbh->do("update profiles_host set service_escalation_id = NULL where service_escalation_id not in (select tree_id from escalation_trees)");
	$dbh->do("ALTER TABLE profiles_host add FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL");
}



# Add data column for saved settings
$sqlstmt = 'describe profiles_host';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;
unless (defined $fields{'data'}) {
	$dbh->do('alter table profiles_host add data TEXT after file_id');
}

if (defined $fields{'file_id'}) {
	$dbh->do('alter table profiles_host drop file_id');
}


# Add data column for saved settings
if (defined $fields{'serviceprofile_id'}) {
	my $sqlstmt = "select hostprofile_id, serviceprofile_id from profiles_host";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	my %fields = ();
	while(my @values = $sth->fetchrow_array()) {
		if ($values[1]) { $dbh->do("insert into profile_host_profile_service values('$values[0]','$values[1]')") }
	}
	$sth->finish;
	$dbh->do("ALTER TABLE profiles_host drop column serviceprofile_id");
}


###################################################
# Service Profiles
#

# Add data column for saved settings
$sqlstmt = 'describe profiles_service';
$sth = $dbh->prepare($sqlstmt);
$sth->execute();
my %fields = ();
while(my @values = $sth->fetchrow_array()) {
	$fields{$values[0]} = 1;
}
$sth->finish;
unless (defined $fields{'data'}) {
	$dbh->do('alter table profiles_service add data TEXT after file_id');
}

if (defined $fields{'file_id'}) {
	$dbh->do('alter table profiles_service drop file_id');
}


@table_info = $dbh->selectrow_array("show create table serviceprofile");
unless ($table_info[1] =~ /FOREIGN KEY\s*\(.serviceprofile_id.\)\s*REFERENCES\s+.profiles_service.\s*\(.serviceprofile_id.\) ON DELETE CASCADE/) {
	$dbh->do("ALTER TABLE serviceprofile add FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE");
	$dbh->do("ALTER TABLE serviceprofile add FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE");
}




#
##############################################################################
# drop obsolete tables
#

my @drop_list = ('stage_escalations','match_strings','import_schemas','stage_status','files','file_host','file_service');
foreach my $drop (@drop_list) {
	if ($tables{$drop}) { $dbh->do("drop table $drop") }
}

print "\n\n\tUpdate complete.\n\n";

#
##############################################################################
# convert 1.2 macros to 2.x
#

if ($is_portal) {
	$sqlstmt = "select command_id, data from commands";
	$sth = $dbh->prepare($sqlstmt);
	$sth->execute();
	while(my @values = $sth->fetchrow_array()) {
		my %command = parse_xml($values[1]);
		if ($command{'command_line'} =~ /\$PERFDATA\$|\$LASTCHECK\$|\$LASTSTATECHANGE\$|\$LATENCY\$|\$EXECUTIONTIME\$|\$OUTPUT\$|\$STATETYPE\$/) {
			if ($command{'command_line'} =~ /\$SERVICE/) {
				$command{'command_line'} =~ s/\$PERFDATA\$/\$SERVICEPERFDATA\$/g;
				$command{'command_line'} =~ s/\$LASTCHECK\$/\$LASTSERVICECHECK\$/g;
				$command{'command_line'} =~ s/\$LASTSTATECHANGE\$/\$LASTSERVICESTATECHANGE\$/g;
				$command{'command_line'} =~ s/\$LATENCY\$/\$SERVICELATENCY\$/g;
				$command{'command_line'} =~ s/\$EXECUTIONTIME\$/\$SERVICEEXECUTIONTIME\$/g;
				$command{'command_line'} =~ s/\$OUTPUT\$/\$SERVICEOUTPUT\$/g;
				$command{'command_line'} =~ s/\$STATETYPE\$/\$SERVICESTATETYPE\$/g;
			} else {
				$command{'command_line'} =~ s/\$PERFDATA\$/\$HOSTPERFDATA\$/g;
				$command{'command_line'} =~ s/\$LASTCHECK\$/\$LASTHOSTCHECK\$/g;
				$command{'command_line'} =~ s/\$LASTSTATECHANGE\$/\$LASTHOSTSTATECHANGE\$/g;
				$command{'command_line'} =~ s/\$LATENCY\$/\$HOSTLATENCY\$/g;
				$command{'command_line'} =~ s/\$EXECUTIONTIME\$/\$HOSTEXECUTIONTIME\$/g;
				$command{'command_line'} =~ s/\$OUTPUT\$/\$HOSTOUTPUT\$/g;
				$command{'command_line'} =~ s/\$STATETYPE\$/\$HOSTSTATETYPE\$/g;
			}
			my $data = qq(<?xml version="1.0" ?>
<data>
 <prop name="command_line"><![CDATA[$command{'command_line'}]]>
 </prop>
</data>);
			$data = $dbh->quote($data);
			$dbh->do("update commands set data = $data where command_id = '$values[0]'");
		}
	}
	$sth->finish;
}

#
##############################################################################
# GWMON-3363 As of version 5.2, GW Monitor now uses Nagios 2.10, so
# we can undo the workaround previously applied to deal with bug in
# Nagios 2.5 (fixed in Nagios 2.9) where the sense of the performance
# logging verbosity flag was reversed.
#

# first check whether we've already removed the workaround, because
# doing it again would toggle the flags back to the wrong settings.


# Read current installed nagios version from /tmp/nagiosversion.txt
my $data_file="/tmp/nagiosversion.txt";
open(DAT, $data_file) || die("Could not open file: $data_file");
my $nvn=<DAT>;
chomp($nvn);
close(DAT);



$sqlstmt = "select value from setup where name = 'perflogbug_workaround_removed'";
my ($workaround_removed) = $dbh->selectrow_array($sqlstmt);
if ( ! defined($workaround_removed) ) {
	print "Workaround not defined. Defining...\n";
    $sqlstmt = "insert into setup values('perflogbug_workaround_removed','nagios','0')";
    my $sth = $dbh->prepare ($sqlstmt);
    unless ($sth->execute) { print "Error: $sqlstmt $@" }
    $sth->finish;
    $workaround_removed = 0;
}

# TODO: this assumes that during a migration, the new version of
# Nagios is installed before this script runs. Is that true?
my $nagios_version_numeric = $nvn;

# need to swap the current values iff we haven't already AND nagios is < 2.9
if (! $workaround_removed && $nagios_version_numeric < 2.9) {
  print "Attempting to Swap Value...\n";
  $sqlstmt = "update setup set value = 'temp_w' where name = 'host_perfdata_file_mode' and value = 'a'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  $sqlstmt = "update setup set value = 'a' where name = 'host_perfdata_file_mode' and value = 'w'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  $sqlstmt = "update setup set value = 'w' where name = 'host_perfdata_file_mode' and value = 'temp_w'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  $sqlstmt = "update setup set value = 'temp_w' where name = 'service_perfdata_file_mode' and value = 'a'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  $sqlstmt = "update setup set value = 'a' where name = 'service_perfdata_file_mode' and value = 'w'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  $sqlstmt = "update setup set value = 'w' where name = 'service_perfdata_file_mode' and value = 'temp_w'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;

  # set the flag indicating that we have now removed (undone) the workaround.
  $sqlstmt = "update setup set value = '1' where name = 'perflogbug_workaround_removed'";
  $sth = $dbh->prepare($sqlstmt);
  unless ($sth->execute) { print "Error: $sqlstmt $@" }
  $sth->finish;


  #swap the values in the nagios.cfg file
  fix_nagios_cfg();
}

sub fix_nagios_cfg{
	open(NAGIOSCFG,"/usr/local/groundwork/nagios/etc/nagios.cfg") || die "Couldn't open nagios.cfg: $!";
	open(CFGTMP,">/tmp/nagios.cfg.tmp") || die "Couldn't open temp file for nagios.cfg: $!";
	#swap w & a
	while(<NAGIOSCFG>){
		my $line = $_;
		if( ($line =~ /host_perfdata_file_mode/) || ($line =~ /service_perfdata_file_mode/) ){
			my ($key,$value) = split(/=/,$line);
			chomp($value);
			if($value eq "w"){$value = "a";}
			elsif($value eq "a"){$value = "w";}

			print CFGTMP "${key}=${value}\n";
		}
		else{
			print CFGTMP $line;
		}
	}

	#replace old nagios.cfg with new
	print `/bin/cp /tmp/nagios.cfg.tmp /usr/local/groundwork/nagios/etc/nagios.cfg`;
	print `/rm -rf /tmp/nagios.cfg.tmp`;

}

sub get_nagios_version_numeric {
  my $command = '/usr/local/groundwork/nagios/bin/nagios';

  # TODO: safe to assume sed is available on path on all platforms?
  open (my $cmd, "($command | sed 's/^/STDOUT:/') 2>&1 |");
  my $results_stderr;
  my $results_stdout;
  while (<$cmd>) {
    if (s/^STDOUT://)  {
      $results_stdout .= $_;
    } else {
      $results_stderr .= $_;
    }
  }
  close($cmd);
  my $version_major;
  my $version_minor;
  my $version_numeric;
  if (defined($results_stdout)) {
    if ($results_stdout =~ /^[\n\r]*Nagios\s+(\d+)(?:\.(\d+))?/) {
      $version_major = $1;
      $version_minor = $2 || '0';
      $version_numeric = "$version_major.$version_minor";
    }
  }
  return $version_numeric;
}

