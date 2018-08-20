#!/usr/local/groundwork/perl/bin/perl -w
#
#       Copyright 2012 GroundWork Open Source Solutions, Inc.
#

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;
use dassmonarch;
use Time::HiRes;
my $line;

my $path = $ARGV[0];
my $active_checks = $ARGV[1];

my $etc ='/etc/hosts';
my $datestamp;
my $hostname;
my ($hostgroup, $hostspec, $action);

# if ($#ARGV  != 0 ) { print "$#ARGV need more arguments\n"; exit (2); }

my $svpath = $path . "SV";
my $svipath = $path . "SVI";
my $defaultobj = $path . "DEF";
my $control = $path . "CONTROL";
my $hostfile = $path . "HOSTS";
my $hostgroupmembersfile = $path . "HOST-HOSTGROUPS";
my $logfile = $path . "debug.out";
my $result;
my $appdone = 1;
my $geodone = 1;
my $wkgrpdone = 1;
my $host_hashref = {};

open (LOG, '>', $logfile);
open (input,'<', $control);
open (DEF, '>', $defaultobj);
open (SV, '>', $svpath);
open (SVI, '>', $svipath);

# Construct an instance of class dassmonarch
my $monarchapi = dassmonarch->new();

while ( $line = <input> ) {
# groupfile \t hostspec \t overwrite flag 
# GEO1	TYPE1	OVERWRITE
	chomp $line;
	if ($line =~ /^#/ ) {next;}
	( $hostgroup, $hostspec, $action) = split( /\t/, $line );
	$result = gen_host ($hostgroup, $hostspec, $action);
	print LOG "$result for $hostgroup\n";
	if ($result ) { print LOG "error in creating the HOSTS and HOST-HOSTGROUP files\n"; `echo error > ../input/$1/ERROR`; }

# create the control file for generating dashboards default object
	
	{
		$hostgroup =~ /APP/ && $appdone && do { print DEF $hostgroup. "\tdefault-object-Apps-first\tdefault-object-Apps-core\n"; $appdone = 0; last; };
		$hostgroup =~ /GEO/ && $geodone && do { print DEF "ALLGEO\tdefault-object-Geo-first\tdefault-object-Geo-core\n"; $geodone = 0; last; };
		$hostgroup =~ /WKGRP/ && $wkgrpdone && do { print DEF $hostgroup. "\tdefault-object-Wkgrp-first\tdefault-object-Wkgrp-core\n"; $wkgrpdone = 0; last; };
	}
	{
		$hostgroup =~ /APP/ && do { print SVI $hostgroup. "\tSV-portlet-instances-app-core\n"; last; };
		$hostgroup =~ /GEO/ && do { print SVI $hostgroup. "\tSV-portlet-instances-geo-core\n"; last; };
		$hostgroup =~ /WKGRP/ && do { print SVI $hostgroup. "\tSV-portlet-instances-wkgrp-core\n"; last; };
	}
	{
		$hostgroup =~ /APP/ && do { print SV $hostgroup. "\tSV-portlet-app-core\n"; last; };
		$hostgroup =~ /GEO/ && do { print SV $hostgroup. "\tSV-portlet-geo-core\n"; last; };
		$hostgroup =~ /WKGRP/ && do { print SV $hostgroup. "\tSV-portlet-wkgrp-core\n"; last; };
	}
}

close SVI;
close SV;
close DEF;
close input;
close LOG;
exit; 

# this is called as many times as there are rows in the file named CONTROL
# for each pass through the sub we add to /etc/hosts, HOSTS, HOST-HOSTGROUP, and HOSTGROUPS files
# when we're all done those files are the basis for the next steps of disabling the active checks, creating lists 
# of responses for the cron job, and starting the show 

sub gen_host {
	my $bare_geofile = $_[0];
	my $bare_typefile = $_[1];
	my $addto = $_[2];

	my $typefile = $path . $bare_typefile;
	my $geofile = $path . $bare_geofile;

# these are the variables we will populate from the "typefile", to tell us how to create the host specification
# the host specification output file hangs aroudn so we can form an idea of which hosts to send fake status to

	my ($hosttype, $hostalias, $hostipaddress, $hostprofile, $hostgroups, $realhost, $parent, $mapref) = ();

# If the 2nd Argument is not present as the word "active" we
# will first deal with the hosts file, we are going to add to it
# check that there is a startup never been modified copy of /etc/hosts and make one if it does not exist
# we want to be able to go back to that if we want to
# make the user stop here and look at the contents because it is important that the /etc/hosts file is OK

	if ( $active_checks =~ /passive/ ) {
		my $saved_etc_hosts = $path . "hosts-CLEAN";
		if (open(ETC,'<',$saved_etc_hosts)) {
			close ETC;
		} else {
			print "examine /etc/hosts and $saved_etc_hosts before proceeding\n"; 
			exit (2);
		}
	
# we copied the standard one for safety (so we have two backups)
# we copy over the standard one
# with that pristine copy and add to it
	
		$datestamp = time();
		
		if ($addto =~ /^OVERWRITE$/ ) {
			`cp  $saved_etc_hosts /etc/hosts`;
		}
		open (ETC,'>>','/etc/hosts') or die ("no /etc/hosts file present\n");
	}
	
	open(TYPE,'<',$typefile ) or die ("could not open $typefile for list of host types and profiles\n");
	while ( $line = <TYPE> ) {
	    ## host type \t alias \t address \t host profile \t g1,g2,n \t realhost \t parent
		chomp $line;
		if ($line =~ /^#/ ) {next;}
		( $hosttype, $hostalias, $hostipaddress, $hostprofile, $hostgroups, $realhost, $parent, $mapref) = split( /\t/, $line );
		$host_hashref->{$hosttype}->{hostalias} = $hostalias;
		$host_hashref->{$hosttype}->{hostipaddress} = $hostipaddress;
		$host_hashref->{$hosttype}->{hostprofile} = $hostprofile;
		$host_hashref->{$hosttype}->{hostgroups} = $hostgroups;
		$host_hashref->{$hosttype}->{realhost} = $realhost;
		$host_hashref->{$hosttype}->{parent} = $parent;
		$host_hashref->{$hosttype}->{mapref} = $mapref;
	}
	close TYPE;
	
	if ($addto =~ /^OVERWRITE$/ ) {
		open(HOST,'>',$hostfile); 
		open (HOSTGROUP,'>',$hostgroupmembersfile); 
	} else { 
		open(HOST,'>>',$hostfile); 
		open (HOSTGROUP,'>>',$hostgroupmembersfile); 
	}
	
	open(HGTYPE,$geofile) or die ("could not open $geofile\n");
	print LOG "processing $geofile $typefile\n";
	
	while ($line= <HGTYPE> ) {

# also remove any leading white space

		$line =~ s/^\s+//;
		chomp $line;
		if ($line =~ /^#/ ) {next;}
		my ($shortname, $mapname)  = split('\t',$line);

# the column woth mapnames in it can have 0, 1 or more as a comma separated list
# the names are template files for NagVis to use
# if they are Geo maps then there is location specific replacement to do; name changes, content changes
# if they are non geographic there is no replacement and the name is not changed

		my @mapnames = split(',',$mapname);
		my $sizeof_mapnames = @mapnames;
		my @realmapnames;
		my $realmapnames;
		my $realmapname;
		my $mapnames;
		if ( $sizeof_mapnames ) {
			foreach $mapnames (@mapnames) {
				print LOG "$mapnames\n";
				`cp -a /usr/local/groundwork/nagvis/etc/maps-template/$mapnames /usr/local/groundwork/nagvis/etc/maps/`;
				if ($mapnames =~ /Geo/) { 
					$realmapname = "/usr/local/groundwork/nagvis/etc/maps/" . $shortname . ".cfg";
					`mv /usr/local/groundwork/nagvis/etc/maps/$mapnames $realmapname`;
					`sed -e 's/GGGG/$shortname/' -i $realmapname`;
				} else { $realmapname = "/usr/local/groundwork/nagvis/etc/maps/" . $mapnames;}
				print LOG "$realmapname\n";
				push (@realmapnames, $realmapname);
			}
		}
		my $short = $shortname;
		$short =~ s/^Geo[_,-]//;

# walk through the previously created array of host specifications to make an entry for each host
# we will add the hosts to Monarch as we go and also write the spec into a file for reference

		foreach $hosttype ( keys %{$host_hashref} ) {
			$realhost = $host_hashref->{$hosttype}->{realhost};

# a VIRT spec means the host is virtual not real; we name them differently.  example "SaaSAmazon"
# a BOX spec means the name is a BB or WB that goes with the HG name, specially for the dashboards. example "BB_Apps_All"
# a REAL sepc means combine group name and spec, example "NewYork_infra_ad_01"
# yes, complicated but this is all supposed to be automatic
# $hosttype has to be unique per spec file or the hash will be goofy....

			{
				$realhost =~ /VIRT/ && do { $hostname = $hosttype; last; };
				$realhost =~ /BOX/ && do { $hostname = $hosttype. "_" .$shortname; last; };
				$realhost =~ /REAL/ && do { $hostname = $short. "_" .$hosttype; last; };
				print "$hosttype has no spec for REAL, BOX or VIRT, please correct file $geofile\n";
				print LOG $hostname. "FAILED \n"; next;
			}
			print LOG "gen_host processing $typefile $hostname\n";
# walk through the array of maps if there are any and replace the map reference in each with the associated hostname
			$mapref = $host_hashref->{$hosttype}->{mapref};
			if ($mapref && $sizeof_mapnames ) {
				foreach $realmapnames (@realmapnames) { 
					print LOG "$hostname mapped to $mapref in $realmapnames\n"; 
					if ( -e $realmapnames ) { `sed -e 's/$mapref/$hostname/g' -i $realmapnames`; }
				}
			}
			$hostprofile = $host_hashref->{$hosttype}->{hostprofile};
			$hostalias = $host_hashref->{$hosttype}->{hostalias};
			$hostipaddress = $host_hashref->{$hosttype}->{hostipaddress};
			if ( $hostalias =~ /same/ ) { $hostalias = $hostname; } 
			if ( $hostipaddress =~ /same/ ) { $hostipaddress = $hostname; } 
			print HOST $hostname. "\t" .$hostipaddress. "\t" .$hostalias. "\t" .$hostprofile. "\t" .$parent. "\n";
			$hostgroups = $host_hashref->{$hosttype}->{hostgroups};

# create the entry relating host to hostgroup; one lien per hostgroup that the host is a member of
			if ($hostgroups) {
				my @hostgroup_names = split(',',$hostgroups);
				foreach my $hostgroup_name (@hostgroup_names) { 
					$hostgroup_name =~ s/\"//g;
# this keyword SELF identifies that we want the host in the same hostgroup as the Regional one driving our script 
					{
						$hostgroup_name =~ /SELF/ && do { print HOSTGROUP $hostname. "\t" .$shortname. "\n"; last; };
						$hostgroup_name =~ /\w+/ && do { print HOSTGROUP $hostname. "\t" .$hostgroup_name. "\n"; last; };
					} 
				}
			} else {
				print LOG "$typefile entry $hosttype has no hostgroup assigned\n";
			}
			# add an entry to /etc/hosts
			if ( $active_checks =~ /passive/ ) { print ETC "127.0.0.101\t" .$hostname. "\n";}
		}
		if ( $active_checks =~ /passive/ ) { print ETC "\n";}
	}
	
	close HGTYPE;
	close HOST;
	close HOSTGROUP;
	if ( $active_checks =~ /passive/ ) { close ETC; }
# clear the hash
	for (keys %$host_hashref) { delete $host_hashref->{$_}; }
	return 0;
}
