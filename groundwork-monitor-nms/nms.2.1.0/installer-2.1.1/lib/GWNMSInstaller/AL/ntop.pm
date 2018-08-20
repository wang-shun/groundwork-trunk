#!/usr/bin/perl
package GWNMSInstaller::AL::ntop;
@ISA = qw(GWInstaller::AL::Software);
use GWNMSInstaller::AL::NMSProperties;
use Socket;

sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		rpm_name => 'groundwork-nms-ntop'
	};
 	
	bless($self,$class);
	$self->init();
	
	return $self;		
}
sub init{
	$self = shift;
	
	#set default port
	$self->set_port(82);
	$self->set_GWM_host("gwm_main");
	
	
}

sub save_values{
	my $self = shift;
	#UI Collection
	my $UICollection = $self->{UICollection};
	
	#GWM
	my $gwmObj = $UICollection->next();
	$self->set_GWM_host($gwmObj->get());
	
	#port
	my $dbObj = $UICollection->next();
	$self->set_port($dbObj->get());
 
		
 }
 


sub configure{
	# configure(): this is a placeholder method. all functionality for this version will be in deploy()
	$success = 1;
	return $success;
}
 
sub deploy
{
    my ($self,$deploy_profile,$installer) = @_;
   # unless($deploy_profile){$deploy_profile = "local";}
    my @properties = ();
    my $result = 0;
    my $host_name =  $installer->{hostname}; #GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn();
	my $propfile = $installer->get_properties();
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
        $num_properties = @properties;

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "nms", "ntop", $host_name);
        if (!($instance eq null))
        {
            $result = deploy_nms_ntop(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

#	===================
#	Deploy ntop service
#	===================

sub deploy_nms_ntop()
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "nms";
    $subtype = "ntop";

    my $enterprise_home = "/usr/local/groundwork/enterprise";
    my $nms_home = "/usr/local/groundwork/nms";
    my $ntop_home = "$nms_home/applications/ntop";
    my $php_home = "$nms_home/tools/php";
    my $etc_initd = "/etc/init.d";

    GWInstaller::AL::GWLogger::log("\tDeploying ntop Instance [$instance]");
    $ntop_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type,$subtype,$instance,"port");

    if ($ntop_port == -1)
    {
        GWInstaller::AL::GWLogger::log("WARNING:Unable to find complete set of properties to deploy ntop.");
	return(0);
    }
    GWInstaller::AL::GWLogger::log("\t{");
    GWInstaller::AL::GWLogger::log("\tntop_port = $ntop_port");
    GWInstaller::AL::GWLogger::log("\t}");

       #
        # Ntop
        #

        if ((! -d $ntop_home))
        {
                GWInstaller::AL::GWLogger::log("WARNING:Ntop not installed at $ntop_home.");
		return(0);
        }

	# 3. Patch

        GWInstaller::AL::GWLogger::log("\tPatching files.");
	`sed "s/APPPORT=.*/APPPORT=$ntop_port/g" /usr/local/groundwork/nms/tools/installer/ntop/nms-ntop >$etc_initd/nms-ntop`;

        # X. Set Permissions.
        GWInstaller::AL::GWLogger::log("\tSetting permissions.");
        `chown -R nagios:nagios $ntop_home`;
	`chmod +x $etc_initd/nms-ntop`;

	# Set up IPTables (for installer later, most likely)
	#GWInstaller::AL::GWLogger::log("\tSetting firewall rules to allow port: $ntop_port");
	#`iptables -A RH-Firewall-1-INPUT -m state --state NEW -m tcp -p tcp --dport $ntop_port -j ACCEPT >/dev/null 2>&1`;
	#`iptables-save >/dev/null 2>&1`;

	if ( -d "/usr/local/groundwork/ntop" )
       	{
                `mkdir /usr/local/groundwork/nms/backups >/dev/null 2>/dev/null`;
                `rm -rf /usr/local/groundwork/nms/backups/ntop >/dev/null 2>/dev/null`;

        	GWInstaller::AL::GWLogger::log("\tMoving previous version of ntop to nms/backups directory");
        	`mv /usr/local/groundwork/ntop /usr/local/groundwork/nms/backups`;
        }

        # Finish Up.
        if (status_ntop() == 0)
        {
                GWInstaller::AL::GWLogger::log("\tRestarting nms-ntop");
                `/etc/init.d/nms-ntop stop >/dev/null 2>&1`;
	}
	else
	{
                GWInstaller::AL::GWLogger::log("\tStarting nms-ntop");
	}
        `/etc/init.d/nms-ntop start >/dev/null 2>&1`;
        GWInstaller::AL::GWLogger::log("\tDone.");
}

#	===================
#	Deploy ntop package
#	===================

sub deploy_package()
{
    my ($self,$deploy_profile,$installer) = @_;
    unless($deploy_profile){$deploy_profile = "local";}

    my @properties = ();
    my $result = 0;
	my $host_name = $installer->{hostname}; 
	my $propfile = $installer->get_properties();
    #printf "DEBUG: {deploy} host_name=$host_name";
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
	#printf "DEBUG: {deploy} properties not null";
	$num_properties = @properties;
	#printf "DEBUG: {deploy} num_properties=$num_properties";

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "application", "ntop-pkg", $host_name);
        if (!($instance eq null))
        {
	    #printf "DEBUG: {deploy} instance not null, instance=$instance";
            $result = deploy_application_ntop_pkg(\@properties, $instance, $deploy_profile);
        }
    }
      my ($shortname,undef) = split('\.',$host_name);
   
    ### Do Database install
    if(-e '/usr/local/groundwork/php/bin/php'){
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php ntop install $shortname  >> /dev/null 2>&1";
    	print `$cmd`;
    }
    else{
    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to install ntop GW Monitor Application.");

    }
    return($result);
}

sub remove_gwm_application{
    my ($installer) = @_;
    my @properties = ();
    my $result = 0;
    $propfile = $installer->get_properties();
    my $host_name = $propfile->get_fqdn();
      my ($shortname,undef) = split('\.',$host_name);

    ### Do Database removal
    GWInstaller::AL::GWLogger::log("Removing Guava Database items...");
    if(-e '/usr/local/groundwork/php/bin/php'){
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php ntop remove $shortname >> /dev/null 2>&1";
    	print `$cmd`;
    }
    else{

    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to remove ntop GW Monitor Application.");
    }	
    
    #Remove the files from the Guava Packages Directory
    
     $rmCMD = "rm -rf /usr/local/groundwork/core/guava/htdocs/guava/packages/ntop_" . $host_name . " >> /dev/null 2>&1";
     print `$rmCMD`;
}

sub deploy_application_ntop_pkg()
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "application";
    $subtype = "ntop-pkg";

        GWInstaller::AL::GWLogger::log("\tDeploying Ntop Instance [$instance]");
	$ntop_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "ntop:host");
	$ntop_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "ntop:port");

        if ($ntop_host == -1 || $ntop_port == -1)
        {
                GWInstaller::AL::GWLogger::log("WARNING:Unable to find complete set of properties to deploy Ntop Package.");
        }
        GWInstaller::AL::GWLogger::log("\t{");

        GWInstaller::AL::GWLogger::log("\tntop:host = $ntop_host");
        GWInstaller::AL::GWLogger::log("\tntop:port = $ntop_port");
        GWInstaller::AL::GWLogger::log("\t}");

        #
        # Ntop
        #

        my $sourcedir = "/usr/local/groundwork/nms/tools/installer/guava-packages/ntop";
	my $guavapackagedir="/usr/local/groundwork/core/guava/htdocs/guava/packages";
	my $dot = index($ntop_host, ".");
	my $ntop_short_hostname=$ntop_host;
        my $ntop_short_hostname_as_var;
        my $p_dot = index($package_host, ".");
        my $package_short_hostname=$package_host;

	if ($dot != -1)
	{
		$ntop_short_hostname = substr($ntop_host, 0, $dot);
	}
        if ($p_dot != -1)
        {
                $package_short_hostname = substr($package_host,0,$p_dot);
        }

        $ntop_short_hostname_as_var = $ntop_short_hostname;
        $ntop_short_hostname_as_var =~ s/-/_/g;

	my $dirname="ntop_$ntop_short_hostname";
	my $patchfile="$guavapackagedir/$dirname/views/ntop.inc.php";

        if (! -d $guavapackagedir)
        {
                GWInstaller::AL::GWLogger::log("WARNING:Can only install a Ntop package if Groundwork is installed.");
        }

	# 1. Delete Old Version
	GWInstaller::AL::GWLogger::log("\tCleaning previous Ntop Package.");
	`rm -rf $guavapackagedir/$dirname`;

	# 2. Installing New version.
	GWInstaller::AL::GWLogger::log("\tInstalling new Ntop Package.");
	`mkdir $guavapackagedir/$dirname`;
	`cp -rf $sourcedir/* $guavapackagedir/$dirname`;

	# 3. Patch with correct location of server.
	GWInstaller::AL::GWLogger::log("\tConfiguring for server location.");
	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $ntop_host . ':' . $ntop_port . '";/g' . "'" . " $patchfile >$patchfile.new";
	`$command`;
	`rm -f $patchfile`;
	`mv $patchfile.new $patchfile`;

        # Patch the guava package

        GWInstaller::AL::GWLogger::log("\tPatching guava package.");
        my $description = "Ntop [$ntop_short_hostname]";
        $patchfile = "$guavapackagedir/$dirname/package.pkg";
        $command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
        `$command`;
        $command = "sed 's/shortname = .*/shortname = ntop_$ntop_short_hostname_as_var/g' -i $patchfile";
        `$command`;
        $command = "sed 's/classname = .*/classname = ntopView$ntop_short_hostname_as_var/g' -i $patchfile";
        `$command`;
        $patchfile = "$guavapackagedir/$dirname/views/ntop.inc.php";
        $command = "sed 's/class ntopView .*/class ntopView$ntop_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
        `$command`;
	`chown -R nagios:nagios $guavapackagedir/$dirname`;

        # X. Finish Up.
        GWInstaller::AL::GWLogger::log("\tDone.");
}

sub status_ntop()
{
    $ntop_PID = `ps -ef |grep -v grep|grep "ntop"|grep "nms"|awk '{print \$2}'`;
    if ($ntop_PID == "")
    {
        return 0;
    }
    else
    {
        return 1;
    }
}

sub is_functional{
	$self = shift;
	$func = 0;
	$retval = 0;
	
	my $STATUS_GOOD			= 0;
my $STATUS_NOT_INSTALLED	= 1;
my $STATUS_CORRUPT_INSTALLATION	= 2;
my $STATUS_NOT_RUNNING		= 3;
my @status_text =	(
				"Good.",
				"RPM Package Not Installed.",
				"Corrupt Installation.",
				"Package Installed, Not Running."
			);
		$rpm_package_prefix = 'groundwork-nms-ntop';
	$gw_home = '/usr/local/groundwork';
	$nms_home = $gw_home . '/nms';
	$component_home = $nms_home . '/applications/ntop';

	#
	#	First, check to see if the package is installed.
	#

	`rpm $rpm_package_prefix 2>/dev/null`; # 
	$package_installed = ($? == 0 ? 1 : 0);
	if (!$package_installed)
	{
		$func = $STATUS_NOT_INSTALLED;
	}

	#
	#	Next, check to see if the component
	#	is operational.
	#

	# Check to see if it is installed correctly.

	if ( ! (-e "$component_home/bin/ntop" ))
	{
		$func = $STATUS_CORRUPT_INSTALLATION;
	}

	# Check to see if it is running.

	`ps -ef | grep ntop`;
	$component_running = ($? == 0 ? 1 : 0);

	if ($component_running)
	{
		$func = $STATUS_GOOD;
	}
	else
	{
		$func = $STATUS_NOT_RUNNING;
	}
	
	if($func !=0){
		$retval = 0;
	}
	else{
		$retval = 1;
	}
	return $retval;
}
 sub get_GWM_host{
	$self = shift;
	return $self->{GWM_host};
}

sub set_GWM_host{
	($self,$gwm_host) = @_;
	$self->{GWM_host} = $gwm_host;
	return 1;
}
1;
