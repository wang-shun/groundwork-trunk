#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::GWMonitor;

use GWInstaller::AL::Host;
use GWInstaller::AL::GWLogger;
sub new{

        my ($invocant) = @_;
        my $class = ref($invocant) || $invocant;

        my $self = {
                identifier=>$identifier,
                host=>$host,
                port=>$port,
                software_class=>$software_class, #Open Source or Professional
                version=>$version
        };

        bless($self,$class);
        $self->init();
        return $self;
}

sub init{
	$self = shift;
	$self->set_port(80);
}

# Function to get port number
sub get_port{
        $self = shift;
        return $self->{port};

}

# Function to set port number
sub set_port{
        $self = shift;
        $self->{port} = shift;
}

# Function to get hostname
sub get_host{
        $self = shift;
        return $self->{host};

}

# Function to set hostname
sub set_host{
        $self = shift;
        $self->{host} = shift;
}

# Function to get identifier
sub get_identifier{
        $self = shift;
        return $self->{identifier};
}

# Function to set identifier
sub set_identifier{
        $self = shift;
        $self->{identifier} = shift;
}


sub close_sessions{
   GWInstaller::AL::GWLogger::log("Cleaning up template files...");
   print `rm -rf /tmp/tpl* > /dev/null 2>&1`;
   GWInstaller::AL::GWLogger::log("Cleaning up session files...");
   print `rm -rf /tmp/sess* > /dev/null 2>&1`;
}


# check if GWMPRO is installed
sub is_installed{
        $self = shift;
        $retval = 0;
        
        #first test for bitrock
#1        if(-e " /usr/local/groundwork/core/guava/htdocs/guava/includes/guava.inc.php"){
#1        	$retval = 1;	
#1        }
        #else test for RPMs
#        else{ 
	        my $host = GWInstaller::AL::Host->new("localhost");
	        if(($host->is_rpm_installed("groundwork-foundation-pro")) &&
	           ($host->is_rpm_installed("groundwork-monitor-core")) &&
	           ($host->is_rpm_installed("groundwork-monitor-pro"))
	           ){
	                $self->{software_class} = "Professional";
	            $retval = 1;
	           }
	        elsif(($host->is_rpm_installed("groundwork-foundation-pro")) &&
	              ($host->is_rpm_installed("groundwork-monitor-core"))
	              ){
	                $self->{software_class} = "Community Edition";
	                $retval = 1;
	              }
	
	         if($retval){
	                my      $version = `rpm -qi groundwork-monitor-core | grep Version | sed s/' '//g | sed s/Version:// | sed s/Vendor.*//`;
	                $self->{version} = $version;
	        }
#1        }

        return $retval;

}

#1sub update_guava_menu{
#1  	GWDB::init("guava");
#1  	$selectViewQuery = "SELECT view_id " . 
#1  					   "FROM guava_views " .
#1  					   "ORDER BY viewname";
#1  					    
#1  	$sth = GWDB::executeQuery($selectViewQuery);
#1    $sth->bind_col(1,\$view_id);
#1    $cnt = 1;
#1    while ($sth->fetch()){
#1	
#1	    $updateRoleViewQuery =  "UPDATE guava_roleviews" . 
#1    							"SET vieworder=$cnt" . 
#1    							"WHERE view_id=$view_id";
#1		$cnt++;
#1		$sth = GWDB::executeQuery($updateRoleViewQuery);
#1		$sth->finish();
#1		
#1	    }
#1    $sth->finish();	
#1}

sub get_os_status{
  	$prefs = GWInstaller::AL::Prefs->new();
	$prefs->load_software_prefs();
 	my $local_os = null;
 	my $is_supported="unsupported";
 	$osref = $prefs->{operating_systems};
 	@oses = @$osref;
 	$osnum = @oses;
 	#GWLogger::log("Testing $osnum OSes");
	foreach $os (@oses){
		#GWLogger::log("Testing ". $os->{'name'});
		if($os->{'valid_version'} eq ""){next;}
		
		if($os->is_installed() && $os->{'production_use'}){
			$is_supported="production";
			$local_os = $os;		
			last;
		}
		elsif($os->is_installed() && !($os->{'production_use'})){
			$is_supported="eval";
			$local_os = $os;		
			last;
		}
	}	 
	$host_os = $local_os;
	return $is_supported;
	
} 

sub install_foundation{
	$retval =1; #success
	print `rpm -ivh packages/groundwork-foundation*  2>&1  | bin/strip_ansi.pl >> installer.log`;	
	return $retval;
}

sub install_core{
	print `rpm -ivh   packages/groundwork-monitor-core*  2>&1 | bin/strip_ansi.pl >> nms.log`;
	return 1;
	
}

sub install_pro{
	print `rpm -ivh  packages/groundwork-monitor-pro*  2>&1 | bin/strip_ansi.pl  >> nms.log `;
	return 1;
}

sub install_bookshelf{
	print `rpm -ivh  packages/groundwork-bookshelf*  2>&1 | bin/strip_ansi.pl  >> nms.log `;
	return 1;
}

sub uninstall_pro{
		GWLogger::log("Uninstalling Pro RPM");
		print `rpm -e groundwork-monitor-pro 2>&1 | bin/strip_ansi.pl >> nms.log`;
}

sub uninstall_core{
		GWLogger::log("Uninstalling Core RPM");
		print `rpm -e groundwork-monitor-core  2>&1 | bin/strip_ansi.pl >> nms.log`;
}

sub uninstall_foundation{
		GWLogger::log("Uninstalling Foundation RPM");
		print `rpm -e groundwork-foundation-pro 2>&1 | bin/strip_ansi.pl >> nms.log 2>&1`;
}

sub uninstall_bookshelf{
		GWLogger::log("Uninstalling Bookshelf RPM");
		print `rpm -e groundwork-bookshelf 2>&1 | bin/strip_ansi.pl >> nms.log 2>&1`;
					  
} 



sub upgrade_pro{
	GWLogger::log("UPGRADING groundwork-monitor-pro");
	print `rpm -Uvh packages/groundwork-monitor-pro*  2>&1 | bin/strip_ansi.pl >> nms.log`;
}
sub upgrade_core{
	GWLogger::log("UPGRADING groundwork-monitor-core");
	print `rpm -Uvh packages/groundwork-monitor-core*  2>&1 | bin/strip_ansi.pl >> nms.log`;
}
sub upgrade_foundation{
	GWLogger::log("UPGRADING groundwork-foundation");
	print `rpm -Uvh packages/groundwork-foundation*  2>&1 | bin/strip_ansi.pl >> nms.log`;
}
sub upgrade_bookshelf{
	GWLogger::log("UPGRADING groundwork-bookshelf");
	print `rpm -Uvh packages/groundwork-bookshelf*  2>&1 | bin/strip_ansi.pl >> nms.log`;	
}

sub verify_nagios_link{
	return 1;
	
}

sub verify_snmp{
	$retval = 0;
	$snmptrapd = `ps -ef | grep  snmptrapd | grep -v 'grep' | grep -c snmptrapd`;
	if($snmptrapd){
		GWLogger::log("Found snmptrapd process.");
	}
	else{
		GWLogger::log("ERROR: snmptrapd process NOT RUNNING");
	}
	$snmptt = `ps -ef | grep  snmptt | grep -v 'grep' | grep -c snmptt`;
	if($snmptrapd){
		GWLogger::log("Found snmptt process");
	}
	else{
		GWLogger::log("ERROR: snmptt process NOT RUNNING");
	}
	
	if($snmptrapd && $snmptt){
		$retval = 1;
	}
	
	return $retval;
}

sub is_valid{
	my ($self) = @_;
	my $valid = 1;
	my $hostname = $self->get_host();
	
	# if IP address verify format
	if($hostname =~ /^\d/){
		if($hostname =~ /(^\d+)\.(\d+)\.(\d+)\.(\d+)/ ){
			foreach $octet (($1,$2,$3,$4)){
				if($octet < 0 || $octet > 255){
					$valid=0;
					last;
				}
			}#end foreach
		}#end if matches pattern
	   else{
	   	$valid = 0;
	   }	
	}
	
	# if hostname verify nameservice
	else{ 
		$results = `host $hostname 2>&1`;
		chomp($results);
		GWInstaller::AL::GWLogger::log("RESULTS for $hostname:$results");
		if($results =~ /not found/){
			$valid = 0;
		}
	}
	return $valid;
	
}
sub send_test_trap{
	GWLogger::log("Sending test trap");
	$retval = 0;
	print `cp /usr/local/groundwork/common/var/log/snmp/snmptrapd.log /tmp/snmptrapd.log >> nms.log 2>&1`;
	print `/usr/local/groundwork/common/bin/snmptrap -v 1 -c public localhost "" "" 0 0 "" >> nms.log 2>&1`;
	sleep 1;
	$trapArrived = `diff  /usr/local/groundwork/common/var/log/snmp/snmptrapd.log /tmp/snmptrapd.log  2> /dev/null | grep 'Cold Start Trap'`;
	if($trapArrived){
		$retval = 1;
	}
	return $retval;	
}

 
sub is_nms_installed{
	$is_installed=0;
		if( (GWInstaller::Host::is_rpm_installed("groundwork-nms-integration")) || 
		 	(GWInstaller::Host::is_rpm_installed("groundwork-nms-weathermap")) ||
		 	(GWInstaller::Host::is_rpm_installed("groundwork-nms-nedi")) ||
		 	(GWInstaller::Host::is_rpm_installed("groundwork-nms-cacti")) ) {
		 	
		 	$is_installed = 1;
		 }
		 
		 return $is_installed;
	
}
 

sub is_partially_installed{
	$retval = 0;
	if(-e '/usr/local/groundwork'){
		
		$hasPHP = `find /usr/local/groundwork/ -type f  | grep '\.php' | wc -l`;
		chomp($hasPHP);
		$hasPL = `find /usr/local/groundwork/ -type f  | grep '\.pl' | wc -l`;
		chomp($hasPL);
		if($hasPHP || $hasPL){
			$retval = 1;
		}
	}
	return $retval;	
}

sub wipe_partial_install{
	$self = shift;
	
	#remove /usr/local/groundwork/
	print `rm -rf /usr/local/groundwork 2> /dev/null`;
	
	#remove tmp files
	print `rm -rf /tmp/sess* 2>/dev/null`;
	print `rm -rf /tmp/tpl*php 2>/dev/null`;
	
	#verify
	unless($self->is_partially_installed){
		$retval = 1; #success;	
	}

	return $retval;
	
}

 
sub get_installed_version{
	
	$filebuf = `rpm -qa groundwork-monitor-core`;
	chomp $filebuf;
	$filebuf =~ /groundwork-monitor-core-(\d+)\.(\d+)\.(\d+)-(\d+)\..*/;
	$major = $1;
	$minor = $2;
	$patch = $3;
	$build = $4;
	#GWLogger::log("maj: $1 min: $2 patch: $3");
	$version = "${major}.${minor}.${patch}";
	return $version;
}

sub get_release_string{
 
	$release =  `rpm -qip packages/groundwork-monitor-core* | grep ^Release | sed s/Build.*//   | sed s/' '//g   | sed s/^Release:.*\\\\.// | sed s/_64//`;
	chomp($release);
 	return $release;
	
}

1;
