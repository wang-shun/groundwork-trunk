#!/usr/bin/perl
package GWNMSInstaller::AL::Weathermap;
@ISA = qw(GWInstaller::AL::Software);
use GWNMSInstaller::AL::NMSProperties;
use Socket;

sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		rpm_name => 'groundwork-nms-weathermap'
	};
 	
	bless($self,$class);
	$self->init();
	return $self;		
}

sub init{
	$self = shift;
	$self->set_cacti("cacti_main");	
	$self->set_GWM_host("gwm_main");
	
	
}
sub get_cacti{
	$self = shift;
	return $self->{cacti};
}

sub set_cacti{
	($self,$cacti) = @_;
	$self->{cacti} = $cacti;
}

sub configure{
	# configure(): this is a placeholder method. all functionality for this version will be in deploy()
	$success = 1;
	return $success;
}

sub save_values{
	my $self = shift;
	#UI Collection
	my $UICollection = $self->{UICollection};
	
	#GWM
	my $gwmObj = $UICollection->next();
	$self->set_GWM_host($gwmObj->get());
	
 
		
 }

#1sub remove_gwm_application{
#1    my ($installer) = @_;
#1    my $propfile = $installer->get_properties();
#1    my @properties = ();
#1    my $result = 0;
#1    my $host_name = $propfile->get_fqdn();
#1      my ($shortname,undef) = split('\.',$host_name);
#1
#1    ### Do Database install
#1    if(-e '/usr/local/groundwork/php/bin/php'){
#1    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php weathermap remove $shortname >> nms.log 2>&1";
#1    	print `$cmd`;
#1    }
#1    else{
#1
#1    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
#1    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to remove Weathermap GW Monitor Application.");
#1    }	
#1    
#1    #Remove the files from the Guava Packages Directory
#1    
#!     $rmCMD = "rm -rf /usr/local/groundwork/core/guava/htdocs/guava/packages/weathermap-editor_" . $shortname . " >> nms.log 2>&1";
#!     print `$rmCMD`;
#1}

sub deploy
{
    my ($self,$deploy_profile,$installer) = @_;
  #  unless($deploy_profile){$deploy_profile = "local";}
    my @properties = ();
    my $result = 0;
    my $host_name = $installer->{hostname}; #GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn();
	my $propfile = $installer->get_properties();
  if($debug){ GWInstaller::AL::GWLogger::log("DEBUG: {deploy} host_name=$host_name");}
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
        if($debug){ GWInstaller::AL::GWLogger::log("DEBUG: {deploy} properties not null");}
        $num_properties = @properties;
        if($debug){ GWInstaller::AL::GWLogger::log("DEBUG: {deploy} num_properties=$num_properties");}

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "nms", "weathermap", $host_name);
        if (!($instance eq null))
        {
            #printf "DEBUG: {deploy} instance not null, instance=$instance";
            $result = deploy_nms_weathermap(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

#	=========================
#	Deploy weathermap service
#	=========================

sub deploy_nms_weathermap()
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "nms";
    $subtype = "weathermap";

    GWInstaller::AL::GWLogger::log("\tDeploying Weathermap Instance [$instance]");

    $package_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "host");
    if ($package_host == -1)
    {
        GWInstaller::AL::GWLogger::log("WARNING:Unable to find complete set of properties to deploy Weathermap.");
		return(0);
    }

    $package_host_shortname = package_shorten($package_host);
    GWInstaller::AL::GWLogger::log("\t{");
    GWInstaller::AL::GWLogger::log("\thost = $package_host ($package_host_shortname)");
    GWInstaller::AL::GWLogger::log("\t}");
	$gw_home = "/usr/local/groundwork";
	$nms_home = "$gw_home/nms";
	$cactidir = "$nms_home/applications/cacti";
	$weathermapdir = "$nms_home/applications/weathermap";
	$configfile = "$cactidir/include/config.php";

	GWInstaller::AL::GWLogger::log("\tAttaching to Cacti Instance.");
    	`rm -rf $cactidir/plugins/weathermap 2>>nms.log 2>>nms.log`;
    	`cp -rf $weathermapdir $cactidir/plugins 2>>nms.log 2>>nms.log`;

	GWInstaller::AL::GWLogger::log("\tBuilding Configuration File.");
	`grep -v "?>" $configfile > /tmp/build_weathermap.tmp 2>>nms.log 2>>nms.log`;
	$str = "\$plugins" . "[]" . " = 'weathermap';";
	$cmd = "echo " . '"\\' . $str . '"' . " >>/tmp/build_weathermap.tmp";
	`$cmd 2>>nms.log 2>>nms.log`;
	`echo "?>" >>/tmp/build_weathermap.tmp 2>>nms.log 2>>nms.log`;
	`cp -f /tmp/build_weathermap.tmp $configfile 2>>nms.log`;

	#
	# Upgrade from NMS 5.1.3 if needed.
	#

	my $nms_513_weathermap_dir = "/usr/local/groundwork/nms/weathermap";
	if ( -d $nms_513_weathermap_dir )
	{
		GWInstaller::AL::GWLogger::log("\tUpgrading previous weathermap configuration.");
		GWInstaller::AL::GWLogger::log("\tCopying images");
		`cp -f $nms_513_weathermap_dir/images/* $cactidir/plugins/weathermap/images >/dev/null 2>&1 2>>nms.log`;
		`cp -f $nms_513_weathermap_dir/*.jpg $cactidir/plugins/weathermap >/dev/null 2>&1 2>>nms.log`;
		`cp -f $nms_513_weathermap_dir/*.png $cactidir/plugins/weathermap >/dev/null 2>&1 2>>nms.log`;
		
		GWInstaller::AL::GWLogger::log("\tCopying maps.");
		`cp -rf $nms_513_weathermap_dir/configs/* $cactidir/plugins/weathermap/configs >/dev/null 2>&1 2>>nms.log`;

		GWInstaller::AL::GWLogger::log("\tFixing URLs in configuration file(s).");
		if ($deploy_profile eq 'local')
		{
			`sed 's|INFOURL http://[^ ]*/cacti|INFOURL http://$package_host_shortname:81/cacti|g' -i $cactidir/plugins/weathermap/configs/* 2>>nms.log`;
		}
		else
		{
			`sed 's|INFOURL http://[^ ]*/cacti|INFOURL http://$package_host:81/cacti|g' -i $cactidir/plugins/weathermap/configs/* 2>>nms.log`;
		}

                `mkdir /usr/local/groundwork/nms/backups >/dev/null 2>/dev/null 2>>nms.log`;
                `rm -rf /usr/local/groundwork/nms/backups/weathermap >/dev/null 2>/dev/null 2>>nms.log`;
                if ( -d "/usr/local/groundwork/weathermap" )
                {
                        GWInstaller::AL::GWLogger::log("\tMoving previous version of weathermap to nms/backups directory");
                        `mv /usr/local/groundwork/weathermap /usr/local/groundwork/nms/backups 2>>nms.log`;
                }
	}

	#
	#	Do we need to upgrade from 2.0?
	#

	my $nms_20_backup_dir = "/usr/local/groundwork/nms/backups/cacti/plugins/weathermap";
	if ( -d $nms_20_backup_dir )
	{
		$weathermap_install_dir = "$cactidir/plugins/weathermap";
		`cp -f $nms_20_backup_dir/configs/* $weathermap_install_dir/configs`;
		`cp -f $nms_20_backup_dir/output/* $weathermap_install_dir/output`;
		`cp -f $nms_20_backup_dir/images/* $weathermap_install_dir/images`;
	}

	GWInstaller::AL::GWLogger::log("\tUpdating file permissions.");
        `chown -R nagios:nagios $cactidir/plugins/weathermap 2>>nms.log`;

        # X. Finish Up.
        GWInstaller::AL::GWLogger::log("\tDone.");
}

#	=========================
#	Deploy weathermap service
#	=========================


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

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "application", "weathermap-editor-pkg", $host_name);
        if (!($instance eq null))
        {
	    #printf "DEBUG: {deploy} instance not null, instance=$instance";
            $result = deploy_application_weathermap_editor_pkg(\@properties, $instance, $deploy_profile);
        }
    }
      my ($shortname,undef) = split('\.',$host_name);

    ### Do Database install
#1    if(-e '/usr/local/groundwork/php/bin/php'){
#1    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php Weathermap install $shortname  >> /dev/null 2>&1";
#1		print `$cmd`;
#1    }
#1    else{
#1    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
#1    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to install Weathermap GW Monitor Application.");
#1
#1    }
#1    return($result);
}

sub deploy_application_weathermap_editor_pkg {
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "application";
    $subtype = "weathermap-editor-pkg";

        GWInstaller::AL::GWLogger::log("\tDeploying Weathermap Editor Instance [$instance]");
        $package_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "host");
	$weathermap_editor_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "weathermap-editor:httpd:host");
	$weathermap_editor_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "weathermap-editor:httpd:port");

        if ($package_host == -1 || $weathermap_editor_host == -1 || $weathermap_editor_port == -1)
        {
               GWInstaller::AL::GWLogger::log("WARNING: Unable to find complete set of properties to deploy Weathermap Editor Package.");
		return(0);
        }

        GWInstaller::AL::GWLogger::log("\t{");
        GWInstaller::AL::GWLogger::log("\tweathermap-editor:httpd:host = $weathermap_editor_host");
        GWInstaller::AL::GWLogger::log("\tweathermap-editor:httpd:port = $weathermap_editor_port");
        GWInstaller::AL::GWLogger::log("\t}");

        $gw_httpd_conf_dir = "/usr/local/groundwork/apache2/conf";
        if (!(-d $gw_httpd_conf_dir))
        {
                GWInstaller::AL::GWLogger::log("WARNING: Directory '$gw_httpd_conf_dir' not found, Aborting.");
        }

        #
        # Weathermap Editor
        #

#!        my $sourcedir = "/usr/local/groundwork/nms/tools/installer/guava-packages/weathermap-editor";
#!		my $guavapackagedir="/usr/local/groundwork/core/guava/htdocs/guava/packages";
        my $dot = index($weathermap_editor_host, ".");
        my $weathermap_editor_short_hostname=$weathermap_editor_host;
        my $weathermap_editor_short_hostname_as_var;
        my $p_dot = index($package_host, ".");
        my $package_short_hostname=$package_host;

        if ($dot != -1)
        {
                $weathermap_editor_short_hostname = substr($weathermap_editor_host,0,$dot);
        }
        if ($p_dot != -1)
        {
                $package_short_hostname = substr($package_host,0,$p_dot);
        }

        $weathermap_editor_short_hostname_as_var = $weathermap_editor_short_hostname;
        $weathermap_editor_short_hostname_as_var =~ s/-/_/g;


        my $dirname="weathermap-editor_${weathermap_editor_short_hostname}";
#!	my $patchfile="$guavapackagedir/$dirname/views/weathermapeditor.inc.php";

#!        if (! -d $guavapackagedir)
#!        {
#!               GWInstaller::AL::GWLogger::log("WARNING:Can only install a Weathermap Editor package if Groundwork is installed.");
#!        }

	# 1. Delete Old Version
#!	GWInstaller::AL::GWLogger::log("\tCleaning previous Weathermap Editor Package.");
#!	`rm -rf $guavapackagedir/$dirname 2>>/dev/null`;

	# 2. Installing New version.
#!	GWInstaller::AL::GWLogger::log("\tInstalling new Weathermap Editor Package.");
#!	`mkdir $guavapackagedir/$dirname 2>>/dev/null`;
#!	`cp -rf $sourcedir/* $guavapackagedir/$dirname 2>>/dev/null`;

	# 3. Patch with correct location of server.
#!	GWInstaller::AL::GWLogger::log("\tConfiguring for server location.");
#!	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $weathermap_editor_host . ':' . $weathermap_editor_port . '";/g' . "'" . " $patchfile >$patchfile.new";
#!	`$command 2>>/dev/null`;
#!	`rm -f $patchfile 2>>/dev/null`;
#!	`mv $patchfile.new $patchfile 2>>/dev/null`;

        # Patch the guava package

#!        GWInstaller::AL::GWLogger::log("\tPatching guava package.");
        my $description = "Weathermap Editor [$weathermap_editor_short_hostname]";
#!        $patchfile = "$guavapackagedir/$dirname/package.pkg";
#!        $command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
#!        `$command 2>>/dev/null`;
#!        $command = "sed 's/shortname = .*/shortname = weathermap_editor_$weathermap_editor_short_hostname_as_var/g' -i $patchfile";
#!        `$command 2>>/dev/null`;
#!        $command = "sed 's/classname = .*/classname = weathermapeditorView$weathermap_editor_short_hostname_as_var/g' -i $patchfile";
#!        `$command 2>>/dev/null`;
#!        $patchfile = "$guavapackagedir/$dirname/views/weathermapeditor.inc.php";
#!        $command = "sed 's/class weathermapeditorView .*/class weathermapeditorView$weathermap_editor_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
#!        `$command 2>>/dev/null`;
#!	`chown -R nagios:nagios $guavapackagedir/$dirname 2>>/dev/null`;

	GWInstaller::AL::GWLogger::log("\tPatching other files.");
	my $patch = "/usr/local/groundwork/nms/tools/installer/cacti";
	`cp -f $patch/auto-overlib.pl /usr/local/groundwork/nms/applications/cacti/plugins/weathermap/random-bits 2>>/dev/null`;

        # Configure httpd server.
        GWInstaller::AL::GWLogger::log("\tConfiguring httpd server.");
        GWInstaller::AL::GWLogger::log("\tCreating directory structure if needed.");

        $gw_nms_httpd_conf_dir = "$gw_httpd_conf_dir/nms";
        if (!(-d $gw_nms_httpd_conf_dir))
        {
                mkdir($gw_nms_httpd_conf_dir);
        }

        GWInstaller::AL::GWLogger::log("\tModifying groundwork apache configuration file.");
        $cmd = "sed 's/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/nms.*//g' -i $gw_httpd_conf_dir/httpd.conf";
        `$cmd 2>>/dev/null`;
        $cmd = "sed 's/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork.*/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork\\/\*\.conf\\nInclude \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/nms\\/\*\.conf/g' -i $gw_httpd_conf_dir/httpd.conf";
        `$cmd 2>>/dev/null`;

        GWInstaller::AL::GWLogger::log("\tCreating httpd configuration file.");
        my $got_host;
        if (gethostbyname($package_host))
        {
                $got_host = true;
        }
        else
        {
                $got_host = false;
        }

        if ($got_host)
        {
                $my_ip_address = inet_ntoa((gethostbyname($package_host))[4]);
                @octets = split('\.',$my_ip_address);
        }
        @host_domain = split('\.',$package_host, 2);

        open(NMS_HTTPD, ">$gw_nms_httpd_conf_dir/nms-httpd.conf") || GWInstaller::AL::GWLogger::log("WARNING:Couldn't create nms httpd configuration file.");
        printf NMS_HTTPD "####\tnms-httpd.conf\n####\tAuto-Generated by Enterprise Deployment Subsystem.\n##\t(C) 2008 Groundwork Open Source.\n##\tDo Not Manually Edit!##\n";
        printf NMS_HTTPD "LoadModule rewrite_module modules/mod_rewrite.so\n";
        printf NMS_HTTPD "TKTAuthSecret \"changethistosomethingunique\"\n";
        printf NMS_HTTPD "rewriteEngine on\n";

        if ($got_host)
        {
                printf NMS_HTTPD "rewriteCond %{HTTP_HOST} ^" . $octets[0] . "\\." . $octets[1] . "\\." . $octets[2] . "\\." . $octets[3] . "\n";
		if ($deploy_profile eq 'local')
		{
                	printf NMS_HTTPD "rewriteRule (.*) http://$package_short_hostname/\$1 [R=301,L]\n";
		}
		else
		{
                	printf NMS_HTTPD "rewriteRule (.*) http://$package_host/\$1 [R=301,L]\n";
		}
		printf NMS_HTTPD "rewriteCond %{HTTP_HOST} localhost\n";
                if ($deploy_profile eq 'local')
                {
                        printf NMS_HTTPD "rewriteRule (.*) http://$package_short_hostname/\$1 [R=301,L]\n";
                }
                else
                {
                        printf NMS_HTTPD "rewriteRule (.*) http://$package_host/\$1 [R=301,L]\n";
                }
        }

	if ($deploy_profile eq 'distributed')
	{
        	printf NMS_HTTPD "rewriteCond %{HTTP_HOST} ^" . @host_domain[0] . "\$ [NC]\n";
        	printf NMS_HTTPD "rewriteRule (.*) http://$package_host/\$1 [R=301,L]\n";
	}
        close(NMS_HTTPD);

        GWInstaller::AL::GWLogger::log("\tAdjusting permissions.");
        `chown -R nagios:nagios $gw_nms_httpd_conf_dir 2>>/dev/null`;

#!        GWInstaller::AL::GWLogger::log("\tPatching guava.");
        $domain = @host_domain[1];
#!        $cmd = "sed 's/setrawcookie(\$cookie_name, \$ticket, null, \$path);/setrawcookie(\$cookie_name,\$ticket, null, \$path, \"$domain\");/g' -i  $gw_home/core/guava/htdocs/guava/includes/guava.inc.php";
#!        `$cmd 2>>/dev/null`;

        GWInstaller::AL::GWLogger::log("\tBuilding Weathermap Editor Configuration File.");

	$cactidir = "/usr/local/groundwork/nms/applications/cacti";
        $editorconfig = "$cactidir/plugins/weathermap/editor-config.php";
        `echo '<?php' > $editorconfig 2>>/dev/null`;
        `echo '    \$cacti_base = "/usr/local/groundwork/nms/applications/cacti";' >>$editorconfig 2>>/dev/null`;
        `echo '    \$cacti_url = "http://$weathermap_editor_host:$weathermap_editor_port/cacti/";' >>$editorconfig 2>>/dev/null`;
        `echo '    \$mapdir="configs";' >>$editorconfig 2>>/dev/null`;
        `echo '    \$ignore_cacti = FALSE;' >>$editorconfig 2>>/dev/null`;
        `echo '?>' >>$editorconfig 2>>/dev/null`;
	`chown nagios:nagios $editorconfig 2>>/dev/null`;

        # X. Finish Up.
        GWInstaller::AL::GWLogger::log("\tRestarting Groundwork Apache Server.");
        `/etc/init.d/httpd restart 2>>/dev/null`;
        GWInstaller::AL::GWLogger::log("\tDone.");
}

sub package_shorten()
{
    my $host_name = shift;
    my $domain_present = index($host_name, ".");

    if ($domain_present != -1)
    {
        $host_name = substr($host_name, 0, $domain_present);
    }
    return($host_name);
}

sub is_functional{
	$self = shift;
	$retval = 0;
	$func = 0;
	
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
	
	$rpm_package_prefix = 'groundwork-nms-weathermap';
	$gw_home = '/usr/local/groundwork';
	$nms_home = $gw_home . '/nms';
	$component_home = $nms_home . '/applications/cacti/plugins/weathermap';

	#
	#	First, check to see if the package is installed.


	`rpm -qa $rpm_package_prefix 2>>/dev/null`;
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

	if ( ! (-e "$component_home/editor.php" ))
	{
		$func = $STATUS_CORRUPT_INSTALLATION;
	}

	# Check to see if it has been integrated in as a cacti plugin.

	`grep weathermap $nms_home/applications/cacti/include/config.php 2>>/dev/null 2>>/dev/null`;
	$component_integrated = ($? == 0 ? 1 : 0);

	if ($component_integrated)
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
