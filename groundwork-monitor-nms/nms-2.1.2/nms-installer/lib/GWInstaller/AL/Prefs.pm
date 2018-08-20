#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWInstaller::AL::Prefs;
use lib qw(../../);
use GWInstaller::AL::Software; 
use GWInstaller::AL::GWLogger;
our($nmslog);
$debug=0;
my($min_memory,$min_cpu_speed,$supported_cpus,$software_name,$version,$software_class,$operating_systems_ref,$prerequisites_ref,$conflicts_ref);
sub new{
	 
	my ($invocant) = @_;
	my $class = ref($invocant) || $invocant;
	
 
   
   #	init_prefs(); 
   
	my $self = {
		min_memory=>$min_memory,
		supported_processors=> $supported_cpus,
		min_cpu_speed=>$min_cpu_speed,	
		min_cpu_count=>$min_cpu_count,
		software_name=>$software_name,
		version=>$version,
		software_class=>$software_class,
		operating_systems=>$operating_systems_ref,
		prerequisites=>$prerequisites_ref,
		conflicts=>$conflicts_ref,
		java_home=>$java_home,
		java_rpm=>$java_rpm 
	};
 
    $nmslog = GWInstaller::AL::GWLogger->new("nms.log");
	bless($self,$class);
 
	
	return $self;		
}

sub init_prefs{
	 
	if($debug){$nmslog->log("## Initializing Preferences ##");}

	$software_name = get_pref('software_name');
	$software_class = whatami();
	$version = get_version_from_core();
	$min_cpu_speed = get_pref('min_cpu_speed');
	$min_cpu_count = get_pref('min_cpu_count');
	$min_memory = get_pref('min_memory');
    $operating_systems_ref = \@osar;
   	$prerequisites_ref = \@par;
   	$conflicts_ref = \@car;
 	$supported_cpus = \@cpus;
 	
    $java_home = get_java_home();
   	$nmslog->log("\n## Reading Software Preferences ##");
  
 
    	
}

sub get_version_from_core{
	$filebuf = `ls -1 packages/groundwork-monitor-core* 2>/dev/null`;
	chomp $filebuf;
	#(undef,$ver) = split('monitor-core-',$filebuf);
	#$nmslog->log("filebuf: $filebuf");
	$filebuf =~ /groundwork-monitor-core-(\d+)\.(\d+)\.(\d+)-(\d+)\..*/;
	$major = $1;
	$minor = $2;
	$patch = $3;
	#$nmslog->log("maj: $1 min: $2 patch: $3");
	$version = "${major}.${minor}.${patch}";
	return $version;
}

sub get_java_home{
	$home_cmd = "find / -type d -name jdk* | head -1";
	$home = `$home_cmd`;
	chomp($home);
	
	unless($home){
		$home = $ENV{JAVA_HOME} if($ENV{JAVA_HOME});
	}
	
	
	return $home;	
}

sub load_software_prefs{
my $self = shift;
  
$in_section = 0;
my $myParams = (); 
 
 
open(PREF,"conf/installer.properties") || $nmslog->log("ERROR: Can't open installer.properties");
while(<PREF>){
	$line = $_;
	 
	
	if ($line =~ /^#/) {next} #ignore comments
	chomp($line); 
	
	
	if($line =~ /Section\s+\"(.+)\"/){
		$in_section = 1; 
		$section = $1; 
		next;
	}
	
	if($line =~ /EndSection/){
	    $prereqs_ref = $self->{prerequisites};
	    @prerequisites = @$prereqs_ref;
	    $conflicts_ref = $self->{conflicts};
	    @conflicts = @$conflicts_ref;
	    $os_ref = $self->{operating_systems};
	    @operating_systems = @$os_ref;
	    
		$in_section = 0;
 	  	$obj = new GWInstaller::AL::Software($section,@myParams);
  	  	
		@myParams = ();
		if($debug){
			$nmslog->log("section: $section");
		  	$nmslog->log("name: $obj->{'name'}");
		  	$nmslog->log("version_command: $obj->{'version_command'}");
		  	$nmslog->log("rpm_name: $obj->{'rpm_name'} ");
		  	$nmslog->log("valid_version $obj->{'valid_version'}");
		  	$nmslog->log("valid_vers_arch $obj->{'valid_vers_arch'}\n\n");
		}
		
		if($section eq "Prerequisite"){
			push(@prerequisites,$obj);	
			$self->{prerequisites} = \@prerequisites;
			$size = @prerequisites;
			if($debug){$nmslog->log("found prereq  $obj->{'name'} . size=$size");}		
		}
		elsif($section eq "Conflict"){
			push(@conflicts,$obj);
			$self->{conflicts} = \@conflicts;
			$size = @conflicts;
			if($debug){$nmslog->log("found conflict $obj->{'name'} . size=$size");	}
		}
		elsif($section eq "Operating System"){
			push(@operating_systems,$obj);
			$self->{operating_systems} = \@operating_systems;
			$size = @operating_systems;
			if($debug){$nmslog->log("found os  $obj->{'name'}  $obj->{'arch'} $obj->{'production_use'} size=$size");}
		}

		${'name'} = "";
		${'rpm_name'} = "";
		${'valid_version'} = "";
		${'version_command'} = "";
		${'valid_vers_arch'} = "";
 
		next;
}

	if($in_section){
		
		$line =~ s/^\s+//; #remove leading spaces
		$line =~ s/\s+$//; #remove trailing spaces
		push(@myParams,$line);
	}
 
	
}	#endwhile
 

return @struct_collection;	
}

sub get_pref{
	$pref_name = shift;
	
	if(-e "conf/installer.properties"){
 		$pref_buf = `grep $pref_name conf/installer.properties`;
	
 		chomp($pref_buf);
		(undef,$pref_val) = split(/=/,$pref_buf);
		chomp($pref_val);
		if($debug){$nmslog->log("$pref_name = $pref_val");}
	}
 	return $pref_val;
}

sub get_valid_upgrades{
	$valid = get_pref('valid_upgrades');
	return $valid;
}
sub whatami{

	$software_class = 0; #invalid by default
	
	$has_foundation = `ls  packages/groundwork-foundation* 2> /dev/null`;
	$has_core = `ls  packages/groundwork-monitor-core* 2> /dev/null`;
	$has_pro = `ls  packages/groundwork-monitor-pro* 2> /dev/null`;
		
	if($has_pro && $has_core && $has_foundation){
		$software_class = "Professional";
	}
	elsif($has_core && $has_foundation){
		$software_class = "Community Edition";
	}
	
	
	if($debug){$nmslog->log("software_class = $software_class");}
	
	return $software_class;
}
 
1;