#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2011 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use Time::Local;
use XML::LibXML;
use lib "/usr/local/groundwork/core/monarch/lib";
use MonarchProfileImport;
use MonarchStorProc;

my $perfconfig_lump_script = "/usr/local/groundwork/core/profiles/tools/import_perfconfig.pl";
my $plugins_dir = "/usr/local/groundwork/nagios/libexec";
my $plugins_dest_dir = "/usr/local/groundwork/nagios/libexec";
my $cgi_dir = "/usr/local/groundwork/apache2/cgi-bin";
my $cgi_dest_dir = "/usr/local/groundwork/apache2/cgi-bin";

my $xmlfile = $ARGV[0];
my $current_dir = `pwd`;
chomp $current_dir;
print "Current directory = $current_dir \n";

# Read service profile XML to get monarch service definitions and performance configuration
print "Processing profile $xmlfile\n";
my $xmldata = read_XML_file($xmlfile);
my @servprof = ();
my @hostprof = ();
my @perfconfig = ();
if ($xmldata) {
		eval {
				my $parser = XML::LibXML->new();
				my $doc = $parser->parse_string($xmldata);
				my @nodes = $doc->findnodes("groundwork_service_profile");
				foreach my $node (@nodes) {
						foreach my $childnode ($node->findnodes("service_profile")) {
							push @servprof,$childnode->textContent;
						}
						foreach my $childnode ($node->findnodes("host_profile")) {
							push @hostprof,$childnode->textContent;
						}
						foreach my $childnode ($node->findnodes("perfconfig_profile")) {
							push @perfconfig,$childnode->textContent;
						}
				}	
		};		# end eval
}
if ($@) {
		print "\nError parsing XML data:\n $@ \n$xmldata\n";  
		exit;
}

# Check plugins in plugin subdirectory. If plugins doesn't exist, copy plugins to /usr/local/groundwork/nagios/libexec/
# Read the existing plugins on this system
#my %plugins;
#print "Checking existing plugins in directory $plugins_dest_dir\n";
#opendir DIR, "$plugins_dest_dir" or die "ERROR. Can't open plugin directory $plugins_dir: $!";
#foreach my $file  (readdir DIR) {
	#if ($file =~ /^\./) { next 	}
	#$plugins{$file}  = 1;
#}
#closedir DIR;
##  read the plugins to install
#print "Processing plugins in directory $plugins_dir\n";
#opendir DIR, "$plugins_dir" or die "ERROR. Can't open plugin directory $plugins_dir: $!";
#foreach my $file  (readdir DIR) {
	#if ($file =~ /^\./) { next 	}
	#print "Checking file $file\n";
	#if (!$plugins{$file}) {		# If it doesn't exist, then copy
		#print "Copying file $plugins_dir/$file to $plugins_dest_dir/.\n";
		#my @lines = `cp $plugins_dir/$file $plugins_dest_dir/.`;
		#print @lines;
	#} else {
		#print "Skip copying file $plugins_dir/$file to $plugins_dest_dir/.  File already exisits.\n";
	#}
#}
#closedir DIR;

# Check cgis in cgi subdirectory. If cgi doesn't exist, copy cgi to /usr/local/groundwork/apache2/cgi/graphs
## Read this profile's plugins
## Read the existing plugins on this system
#my %cgis;
#print "Checking existing cgis in directory $cgi_dest_dir\n";
#opendir DIR, "$cgi_dest_dir" or die "ERROR. Can't open cgi directory $cgi_dir: $!";
#foreach my $file  (readdir DIR) {
	#if ($file =~ /^\./) { next 	}
	#$cgis{$file}  = 1;
#}
#closedir DIR;
#  read the cgis to install
#print "Processing cgis in directory $cgi_dir\n";
#opendir DIR, "$cgi_dir" or die "ERROR. Can't open cgi directory $cgi_dir: $!";
#foreach my $file  (readdir DIR) {
	#if ($file =~ /^\./) { next 	}
	#print "Checking file $file\n";
	#if (!$cgis{$file}) {		# If it doesn't exist, then copy
		#print "Copying file $cgi_dir/$file to $cgi_dest_dir/.\n";
		#my @lines = `cp $cgi_dir/$file $cgi_dest_dir/.`;
		#print @lines;
	#} else {
		#print "Skip copying file $cgi_dir/$file to $cgi_dest_dir/.  File already exisits.\n";
	#}
#}
#closedir DIR;

# Connect to Monarch database
my $auth = StorProc->dbconnect();
# Import each host profile XML file
print "host profiles\n";
foreach my $key (@hostprof) {
	print "Processing host profile file $key\n";
	my @lines = ProfileImporter->import_profile($key,1);		# To update set flag 1
	print @lines;
}

# Import monarch profiles configuration XML file
# Read service profile XML file
foreach my $key (@servprof) {
	print "Processing service profile file $key\n";
	my @lines = ProfileImporter->import_profile($key,1);		# To update set flag 1
	print @lines;
}
# Import performance graph configuration XML file
foreach my $key (@perfconfig) {
	print "Processing performance config file $key\n";
	my @lines = `$perfconfig_lump_script $key`;
	print @lines;
}
exit;

sub read_XML_file {
	open (XMLFILE,"$xmlfile") or die "ERROR: Can't open XML file \"$xmlfile\".\n";
	my $data = undef;
	my $end_config = undef;
	while (my $line=<XMLFILE>) {
		chomp $line;
		$data .= $line;
	}
	return $data;
}

__END__

