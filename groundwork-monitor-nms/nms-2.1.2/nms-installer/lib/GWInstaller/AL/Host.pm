#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::Host;
use lib("../");
use GWInstaller::AL::GWLogger;


sub new{

        my ($invocant,$hostname) = @_;
        my $class = ref($invocant) || $invocant;
        my $self = {
                hostname=>$hostname,
                componentCollection=>$componentCollection,
                nedi=>$nedi,
                cacti=>$cacti,
                ntop=>$ntop,
                weathermap=>$weathermap,
                installCollection=>$installCollection,
                UICollection=>$UICollection
		};
	bless($self,$class);
	
 	our $nmslog = GWInstaller::AL::GWLogger->new("nms.log");
 
	
	return $self;		
}
sub mysql_is_running{
	$processes = `/bin/ps -ef | grep 'mysqld' | grep -v 'grep' | wc -l`;
	chomp($processes);
	return $processes;
}

sub mysql_passwd_set{
	$retval = 0;
	$mysql_login = `/usr/local/groundwork/mysql/bin/mysql -e  'select User from mysql.user limit 1' 2>&1`;
	chomp($mysql_login);
	if($mysql_login eq "ERROR 1045 (28000): Access denied for user 'root'\@'localhost' (using password: NO)"){
		$retval = 1;		
	GWInstaller::AL::GWLogger::log("\tmysql password is set.")
	}
	else{
		GWInstaller::AL::GWLogger::log("\tmysql password not set");
	}
	 
	return $retval;
}

sub mysql_can_connect{
	$validity = 0;
	my $mysql_password = shift;
	
	if($mysql_password ne ""){
        $did_connect =  `/bin/echo 'SELECT host,user from mysql.user LIMIT 1' | mysql -p${mysql_password} --batch mysql 2>&1 | grep -v '^ERROR' | head -1`;
        }
    else{
        $did_connect = `/bin/echo 'SELECT host,user from mysql.user LIMIT 1' | mysql --batch mysql 2>&1 | grep -v '^ERROR' | head -1`;
        }
	
	if($did_connect){
		$validity = 1;
	}
	return $validity;
}
sub set_properties{
	my ($self,$properties) = @_;
	$self->{properties} = $properties;
}
#saves UI values to the data model
sub save_values{
	my $self = shift;
	my $properties = $self->{properties};
	#get component Collection
	my $componentCollection = $self->get_component_collection($properties);
	while($componentCollection->has_next()){

		my $component = $componentCollection->next();

		if ( ($component->get_name() eq "nedi") || ($component->get_name eq "ntop") || ($component->get_name eq "weathermap") || ($component->get_name eq "cacti")   ){
			$component->save_values();
		}
	}

}

sub is_valid{
	my ($self) = @_;
	my $valid = 1;
	my $hostname = $self->{hostname};
	
	# if IP address verify format
	if($hostname =~ /^\d/){
		if($hostname =~ /(^\d+)\.(\d+)\.(\d+)\.(\d+)$/ ){
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
	#elsif($hostname !~ /\W/){
		 
		$results = `host $hostname 2>&1`;
		chomp($results);
		GWInstaller::AL::GWLogger::log("RESULTS for $hostname:$results");
		if($results =~ /not found/){
			$valid = 0;
		}
	#}
	#else{
	#	$valid = 0;
	#}
	return $valid;
	
}
sub get_identifier{
	$self = shift;
	return $self->{identifier};
}
sub set_identifier{
	$self = shift;
	$self->{identifier} = shift;
}

sub get_install_collection{
my ($self,$properties) = @_;	
	$self->{installCollection} = null;
	my $installCollection = GWInstaller::AL::Collection->new();
	
	my @potentials = GWNMSInstaller::AL::GWNMS::get_available_components();
	foreach $component(@potentials){
				
		#get identifier for particular component for this host
		my $method_name = "get_" . $component;
		my $ident = $self->$method_name;

	if($debug){ GWInstaller::AL::GWLogger::log("** ident: $ident");}
	if($debug){ 	GWInstaller::AL::GWLogger::log("** method: $method_name");}

		
		$compObj = GWInstaller::AL::Software->new();
		
		$compObj->set_identifier($component);
		if($ident){
			$compObj->set_do_install(1);
		}
		else{
			$compObj->set_do_install(0);			
		}
		$installCollection->add($compObj);		 
	}#end foreach
	
	
	$self->{installCollection} = $installCollection;
	return $self->{installCollection};	
}

sub get_component_collection{
	my ($self,$properties) = @_;		
	my $componentCollection = GWInstaller::AL::Collection->new();
	my @potentials = ("nedi","cacti","ntop","weathermap","nedi_pkg","cacti_pkg","ntop_pkg","weathermap_editor_pkg","nmshttpd","foundation");
	foreach $component(@potentials){
		
		if($debug){ GWInstaller::AL::GWLogger::log("gcc: component = $component");}
		
		#get identifier for particular component for this host
		my $method_name = "get_" . $component;
		my $ident = $self->$method_name;
		if($debug){ GWInstaller::AL::GWLogger::log("gcc: ident = $ident")}
		;		
		#extract the object from the appropriate collection (if exists)
		my $collection = $properties->get_collection($component);
		if($debug){GWInstaller::AL::GWLogger::log("gcc: colect class:" . ref $collection);}
		my $obj = $collection->get_by_identifier($ident);
		if($debug){GWInstaller::AL::GWLogger::log("gcc: obj class for ident=$ident:" . ref $obj);}
		#if obj is empty, skip
		my $classname = ref $obj;
		if($classname eq ""){
			if($debug){ GWInstaller::AL::GWLogger::log("gcc: skipping for class:" . ref $obj);}

			next;
		}
		#if obj is valid, add to componentCollection
		else{
					if($debug){ GWInstaller::AL::GWLogger::log("gcc: adding object to collection:" . ref $obj);}
			my $checkhost = $obj->get_host();
			if($checkhost eq ""){$obj->set_host($self->get_identifier());}
 			$componentCollection->add($obj);
		}
	}
	$self->{componentCollection} = $componentCollection;
	
	return $self->{componentCollection};
}


sub service_is_running{
	$service = shift;
	$status = "";
	if( -e "/etc/init.d/$service" ){
	$cmd = "/etc/init.d/" . $service . " status | grep -c 'running'";
	$status = `$cmd`;
	chomp($status);
	}
	return $status;
	
}

sub start_service{
	$service = shift;
	$start_cmd = "/etc/init.d/" . $service . " start";
	print `$start_cmd`;	
}

sub stop_service{
	$service = shift;
	$stop_cmd = "/etc/init.d/" . $service . " stop";
	print `$stop_cmd`;	
}

sub restart_service{
	$service = shift;
	$restart_cmd = "/etc/init.d/" . $service . " restart";
	print `$restart_cmd`;	
}

sub set_host{
	my ($self,$hostname) = @_;
	$self->{hostname} = $hostname;	
}

sub get_hostname{
	$self = shift;
	unless($self->{hostname}){
		$host = `hostname 2>> nms.log`;
		chomp($host);
		$self->{hostname} = $host;
	}
	return $self->{hostname};
}

sub get_avail_disk{
	
	$available = 0;
	
	$space_buf  =  `df -h /usr/local 2>>nms.log | grep -v Avail`;
	($filesystem,$size,$used,$avail,$usepercent,$mountedon) = split(/\s+/,$space_buf);
	$avail =~ /(\d+\.*\d*)([A-Z])/;
	$num = $1;
	$notation = $2;
	
	if($notation eq "G"){
		$available = $num;
	}
	else{
		$available = 0;
	}
	return $available;
	
}

sub get_mem_total{
	unless($self{mem_total}){
		#use top in batch mode
		#Mem:   2076888k total,  2021156k used,    55732k free,    88892k buffers
	
	    $cmd = "top -b -n1 | grep Mem | head -1";
	    $results = `$cmd`;
	    $results =~ /Mem:\s+(.+?)k.+?,\s+(.+?)k.+/;
	    $memTotal = $1;
	    $memUsed = $2;
	    
		$self->{mem_total} = 	$memTotal;
	}

    return $self->{mem_total};
}

# returns FileSystem object containing all the info for the filesystem on which the
# target install directory is located
sub get_target_fs{
	
	unless($self->{target_fs}){
		$install_dir = shift;
		$cnt=0;
		@fs_array;
		open(DF, "df -lh |");
		while(<DF>){
			if($cnt==0){$cnt++;next;}
			$line = $_;
			($fs_name,$size,$used,$avail,$up,$mount) = split(/\s+/,$line);
			$fs = FileSystem->new($fs_name,$size,$used,$avail,$up,$mount);
			push(@fs_array,$fs);
		}
		close(DF);
		
		$potential_match = "";
		
		foreach $f(@fs_array){
			if(($f->{mount} =~ $install_dir) && (length($f->{mount}) > length($potential_match)) ){
				$potential_match = $f;
			}
		}
		$self->{target_fs} = $potential_match;
	}
	return $self->{target_fs};
}

sub get_host{
	my ($self) = shift;
	return $self->{hostname};
}

sub get_cpu_type{
	unless($self->{cpu_type}){
	    $info = `cat /proc/cpuinfo | grep 'model name'`;
    	(undef,$cpu) = split(': ',$info);
    	$self->{cpu_type} = $cpu;
	}
	
	return $self->{cpu_type};
}

sub get_cpu_speed{
	unless($self->{cpu_speed}){
	    $info = `cat /proc/cpuinfo | grep 'cpu MHz'`;
    	(undef,$speed) = split(': ',$info);
    	$self->{cpu_speed} = $speed;
	}
	return $self->{cpu_speed};
}

 
sub get_os{
 	
	if( -e "/etc/redhat-release"){
		$os = `cat /etc/redhat-release 2> /dev/null`;
	}
	elsif( -e "/etc/SuSE-release"){
		$os = `cat /etc/SuSE-release 2> /dev/null`;
	}
	elsif( -e "/etc/slackware-version"){
		$os = `cat /etc/slackware-version 2> /dev/null`;
	}
	elsif( -e "/etc/gentoo-release"){
		$os = `cat /etc/gentoo-release 2> /dev/null`;
	}
	elsif( -e "/etc/fedora-release"){
		$os = `cat /etc/fedora-release 2> /dev/null`;
	}
	elsif( -e "/etc/debian_version"){
		$os = `cat /etc/debian_version 2> /dev/null`;
	}
	elsif( -e "/etc/mandrake-release"){
		$os = `cat /etc/mandrake-release 2> /dev/null`;
	}
	elsif( -e "/etc/release"){
		$os = `head -1 /etc/release 2> /dev/null`;
	}
	elsif( -e "/etc/lsb-release"){
		$os = `cat /etc/lsb-release 2> /dev/null`;
	}
	elsif(-e "/etc/issue"){
		$os = `head -1 /etc/issue 2> /dev/null`;
	}
	
	$os  =~ s/^\s+//; #remove leading spaces
	$os  =~ s/\s+$//; #remove trailing spaces
	if($os eq ""){
		$os = "UNIDENTIFIED";
	}
	
	return $os;
	
}

sub get_os_obj{
  	$prefs = GWInstaller::AL::Prefs->new();
	$prefs->load_software_prefs();
 	my $local_os = null;
 	my $is_supported="unsupported";
 	$osref = $prefs->{operating_systems};
 	@oses = @$osref;
 	$osnum = @oses;
 	#$nmslog->log("Testing $osnum OSes");
	foreach $os (@oses){
		#$nmslog->log("Testing ". $os->{'name'});
		if($os->{'valid_version'} eq ""){next;}
		
		if($os->is_installed() && $os->{'production_use'}){
			$is_supported="production";
			$local_os = $os;	
 			return $os;	
 		}
		elsif($os->is_installed() && !($os->{'production_use'})){
			$is_supported="eval";
			$local_os = $os;
 			return $os;		
 		}
	}	 
	
	$invalid_os = new GWInstaller::AL::Software("os","unsupported=1");
	
	return $invalid_os;
	
} 
sub get_current_user{
	unless($self->{current_user}){
		$user = `whoami 2> /dev/null`;
		chomp($user);
		$self->{current_user} = $user;
	}
	return $self->{current_user};
}

sub ssl_is_enabled{

$is_enabled = "";

$string = `grep 'Include conf/extra/httpd-ssl.conf' /usr/local/groundwork/apache2/conf/httpd.conf`;
$string =~ s/^\s+//; #remove leading spaces
chomp($string);
if($string =~ /^#/){
	$is_enabled = 0;
}
else{
	$is_enabled = 1;
}
}

sub verify_hosts_file{
	$nameserviceOK = shift;
	$verify=0;
	$loopback_verified=0;
	$hostname = `hostname 2>> nms.log`;
	chomp($hostname);
 	
	#/etc/hosts
	$nmslog->log("Verifying /etc/hosts file...");
	
	#verify loopback entry
	$localhost_buf = `grep -i localhost /etc/hosts | grep -v '^#' | grep -v '^::' 2>> nms.log`;
	chomp($localhost_buf);
	($lhip,$lhname,$lhdom) = split(/\s+/,$localhost_buf);

	if(($lhip eq "127.0.0.1") && ($lhname eq "localhost")  ){
		$loopback_verified=1;
		$nmslog->log("\tloopback verified");
	}
	else{
		$nmslog->log("\tloopback not verified");
	}	
	
   
	if($nameserviceOK){
		$nmslog->log("\tremote name service configured. skipping host file entry check");
	}
	else{
		$nmslog->log("\tremote name service not configured. checking for host file entry");
		$hostentry = `grep \`hostname\` /etc/hosts | head -1`;
		
		 
		($ip,@othercrap) = split(/\s+/,$hostentry);
		if(($hostentry ne "") && ($ip =~ /\d+\.\d+\.\d+\.\d+/) && ($ip ne "127.0.0.1") ){
			$nameserviceOK =1 ;
			$nmslog->log("\thostname entry in /etc/hosts verified");
			}
		else{
			$nmslog->log("\thostname entry in /etc/hosts not verified");
		}	
	}
	
	if($loopback_verified && $nameserviceOK){
		$verify=1;
		#$nmslog->log("\tVerified hosts file");
	}
	else{
		#$nmslog->log("\thosts file not compliant.");
	}
	
	return $verify;
}

sub port_in_use{
        $self = shift;
        $protocol = shift; #TCP/UDP
        $port = shift; #port number
        $port_status = "";

        if($protocol eq "TCP"){
                $TorU = "T";
        }
        else{
                $TorU = "U";
        }

        $port_check_cmd = "/usr/local/groundwork/common/bin/nmap -s${TorU} localhost -p $port  2>/dev/null | grep $port 2>/dev/null";
        #514/udp open|filtered syslog
        $port_check = `$port_check_cmd`;
        ($portstring,$state,$service) = split(/\s+/,$port_check);

        if($state =~ /open/){
                $port_status = 1;
        }
        elsif($state =~ /closed/){
                $port_status = 0;
        }
        else{
        	#print "check command: $port_check_cmd\n";
        	$port_status = $state;
        }
        return $port_status;
}

sub is_selinux_enabled{
	$retVal = 0;
	$seStatus = `sestatus -v 2>&1 | grep status`;
	chomp($seStatus);
	unless($seStatus =~ "command not found"){
        (undef,$myStatus) = split(/\:\s+/,$seStatus);
         if($myStatus ne "disabled"){
              $retVal = 1;
        }
        
	}
	return $retVal;
}
sub is_rpm_installed{
		my $self = shift;
		my $type = ref $self;
		my $pkg = "";
		
		if($type eq "GWInstaller::AL::Host"){
			$pkg = shift;
		}
		else{
			$pkg = $self;
		}
		
        my $is_installed = 0;
     unless($pkg eq ""){
        	$is_installed = `rpm -qi $pkg 2> /dev/null | grep Name | wc -l`;
	    	chomp($is_installed);
        	if($is_installed){
        		GWInstaller::AL::GWLogger::log("\t$pkg is INSTALLED.");
        	}
        	else{
        		GWInstaller::AL::GWLogger::log("\t$pkg is NOT installed");
        	}
         }
    return $is_installed;
}

sub install_rpm{
	$rpm_name = shift;
	GWInstaller::AL::GWLogger::log("Installing RPM $rpm_name");	
	$cmd = "rpm -ivh ./packages/$rpm_name* >> nms.log 2>&1";
 	print `$cmd`;
}

sub uninstall_rpm{
	$rpm_name = shift;	
	GWInstaller::AL::GWLogger::log("Uninstalling $rpm_name");
	$cmd = "rpm -e $rpm_name >> nms.log 2>&1";	
	print `$cmd`;
}


sub install_package{
	
	$rpm = shift;
	$rpm_name = shift;
	$cui = shift;
	
	chomp $rpm;
	GWCursesUI::status_msg("Installing $rpm_name ...",$cui);
		     $nmslog->log("after status");
	
    sleep $sleep;
	print `rpm -ivh $rpm >> nms.log 2>&1`;
	     $nmslog->log("after install");

	sleep $sleep;
	return 1;
	
	
}

sub set_nedi{
	my ($self,$nedi) = @_;
	$self->{nedi} = $nedi;
}

sub get_nedi{
	my $self = shift;
	return $self->{nedi};
}

sub set_cacti{
	my ($self,$cacti) = @_;
	$self->{cacti} = $cacti;
}

sub get_cacti{
	my $self = shift;
	return $self->{cacti};
}

sub set_weathermap{
	my ($self,$weathermap) = @_;
	$self->{weathermap} = $weathermap;
}

sub get_weathermap{
	my $self = shift;
	return $self->{weathermap};
}

sub set_ntop{
	my ($self,$ntop) = @_;
	$self->{ntop} = $ntop;
}

sub get_ntop{
	my $self = shift;
	return $self->{ntop};
}

sub set_cacti_pkg{
	my ($self,$cacti_pkg) = @_;
	$self->{cacti_pkg} = $cacti_pkg;
}

sub get_cacti_pkg{
	my $self = shift;
	return $self->{cacti_pkg};
}

sub set_nedi_pkg{
	my ($self,$nedi_pkg) = @_;
	$self->{nedi_pkg} = $nedi_pkg;
}

sub get_nedi_pkg{
	my $self = shift;
	return $self->{nedi_pkg};
}

sub set_ntop_pkg{
	my ($self,$ntop_pkg) = @_;
	$self->{ntop_pkg} = $ntop_pkg;
}

sub get_ntop_pkg{
	my $self = shift;
	return $self->{ntop_pkg};
}

sub set_weathermap_editor_pkg{
	my ($self,$weathermap_pkg) = @_;
	$self->{weathermap_pkg} = $weathermap_pkg;
}

sub get_weathermap_editor_pkg{
	my $self = shift;
	return $self->{weathermap_pkg};
}


sub set_gwmonitor{
	my ($self,$gwmonitorpackage) = @_;
	$self->{gwmonitorpackage} = $gwmonitorpackage;
}

sub get_gwmonitor{
	my $self = shift;
	return $self->{gwmonitorpackage};
}

sub set_nmshttpd{
	my ($self,$nmshttpdpackage) = @_;
	$self->{nmshttpdpackage} = $nmshttpdpackage;
}

sub get_nmshttpd{
	my $self = shift;
	return $self->{nmshttpdpackage};
}

sub set_foundation{
	my ($self,$foundationpackage) = @_;
	$self->{foundationpackage} = $foundationpackage;
}

sub get_foundation{
	my $self = shift;
	return $self->{foundationpackage};
}

sub set_eventbroker{
	my ($self,$eventbrokerpackage) = @_;
	$self->{eventbrokerpackage} = $eventbrokerpackage;
}

sub get_eventbroker{
	my $self = shift;
	return $self->{eventbrokerpackage};
}
sub set_automation_pkg{
	my ($self,$automationpackagepackage) = @_;
	$self->{automationpackagepackage} = $automationpackagepackage;
}

sub get_automation_pkg{
	my $self = shift;
	return $self->{automationpackagepackage};
}

sub set_database{
	my ($self,$databasepackage) = @_;
	$self->{databasepackage} = $databasepackage;
}

sub get_database{
	my $self = shift;
	return $self->{databasepackage};
}

sub verify_MySQL_config{
	 
	$passwd = shift;
	$retval = 1;
	
 	if($passwd){
 		$mysql_cmd = "/usr/local/groundwork/mysql/bin/mysql -p$passwd -e 'select User from mysql.user limit 1' 2>&1";
		$mysql_login = `/usr/local/groundwork/mysql/bin/mysql -p$passwd -e 'select User from mysql.user limit 1' 2>&1`;
	}
	else{
		
		$mysql_login = `/usr/local/groundwork/mysql/bin/mysql -e 'select User from mysql.user limit 1' 2>&1`;
	}
	chomp($mysql_login);
	
	
	
	if($mysql_login eq "ERROR 1045 (28000): Access denied for user 'root'\@'localhost' (using password: YES)"){
		$retval = 0;	
		
	}
	 
	 
	return $retval;
	
	
}

1;
