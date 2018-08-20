#!/usr/bin/perl
package GWNMSInstaller::AL::Core;
@ISA = qw(GWInstaller::AL::Software);
use GWNMSInstaller::AL::NMSProperties;


sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		rpm_name => 'groundwork-nms-core'
	};
 	
	bless($self,$class);

	return $self;		
}


#	=====================
#	Deploy nms automation
#	=====================

sub deploy
{
    my ($self,$deploy_profile,$installer) = @_;
  #  unless($deploy_profile){$deploy_profile = "local";}
	#$self = shift;
    my @properties = ();
    $propfile = $installer->get_properties();
    my $result = 0;
    my $host_name = $installer->{hostname}; # GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn();

   #GWInstaller::AL::GWLogger::log("DEBUG: {deploy} host_name=$host_name");
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
      # GWInstaller::AL::GWLogger::log("DEBUG: {deploy} properties not null");
        $num_properties = @properties;
       #GWInstaller::AL::GWLogger::log("DEBUG: {deploy} num_properties=$num_properties");

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "application", "nms-automation", $host_name);
        if (!($instance eq null))
        {
         #  GWInstaller::AL::GWLogger::log(" DEBUG: {deploy} instance not null, instance=$instance");
            $result = deploy_application_nms_automation(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

sub deploy_application_nms_automation
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "application";
    $subtype = "nms-automation";


        GWInstaller::AL::GWLogger::log("\tDeploying NMS Automation Instance [$instance]");
	$package_host =GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type, $subtype, $instance, "host");
	$cacti_db_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "cacti:database:host");
	$cacti_db_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "cacti:database:port");
	$cacti_db_name = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "cacti:database_name");
	$cacti_db_user = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "cacti:database_user");
	$cacti_db_password = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "cacti:database_password");

	$nedi_db_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "nedi:database:host");
	$nedi_db_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "nedi:database:port");
	$nedi_db_name = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "nedi:database_name");
	$nedi_db_user = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "nedi:database_user");
	$nedi_db_password = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "nedi:database_password");

        if ($package_host == -1
        || $cacti_db_host == -1 || $cacti_db_port == -1 || $cacti_db_name == -1 || $cacti_db_user == -1 || $cacti_db_password == -1
        || $nedi_db_host == -1 || $nedi_db_port == -1 || $nedi_db_name == -1 || $nedi_db_user == -1 || $nedi_db_password == -1)
        {
                GWInstaller::AL::GWLogger::log("WARNING:Unable to find complete set of properties to deploy NMS Automation Package, Aborting.");
		return(0);
        }

	GWInstaller::AL::GWLogger::log("\t{");
	GWInstaller::AL::GWLogger::log("\tcacti_db_host = $cacti_db_host");
	GWInstaller::AL::GWLogger::log("\tcacti_db_port = $cacti_db_port");
	GWInstaller::AL::GWLogger::log("\tcacti_db_name = $cacti_db_name");
	GWInstaller::AL::GWLogger::log("\tcacti_db_user = $cacti_db_user");
	GWInstaller::AL::GWLogger::log("\tcacti_db_password = [hidden]");
	GWInstaller::AL::GWLogger::log("\tnedi_db_host = $nedi_db_host");
	GWInstaller::AL::GWLogger::log("\tnedi_db_port = $nedi_db_port");
	GWInstaller::AL::GWLogger::log("\tnedi_db_name = $nedi_db_name");
	GWInstaller::AL::GWLogger::log("\tnedi_db_user = $nedi_db_user");
	GWInstaller::AL::GWLogger::log("\tnedi_db_password = [hidden]");
	GWInstaller::AL::GWLogger::log("\t}");

	$gw_home = "/usr/local/groundwork";
	$gw_monarch_automation_templates_dir = "$gw_home/core/monarch/automation/templates";
	$gw_profiles_dir = "$gw_home/core/profiles";
	$nms_automation_scripts_dir = "$gw_home/nms/tools/automation/scripts";

	if (!(-d $gw_profiles_dir) && !(-d $gw_monarch_automation_templates_dir ))
	{
                GWInstaller::AL::GWLogger::log("WARNING:Expected Monarch Automation Templates and Profiles Directories Missing, Aborting.");
	}

        GWInstaller::AL::GWLogger::log("\t{");
        GWInstaller::AL::GWLogger::log("\thost = $package_host");
        GWInstaller::AL::GWLogger::log("\t}");

        #
        # NMS Automation
        #

	GWInstaller::AL::GWLogger::log("\tInstalling NMS Automation Components.");
	GWInstaller::AL::GWLogger::log("\tConfiguring Scripts.");
	$cmd = "sed \'s/nedi_database =.*/nedi_database = \"$nedi_db_name\";/g\' -i $nms_automation_scripts_dir/extract_nedi.pl";
    	`$cmd`;
	$cmd = "sed \'s/nedi_host =.*/nedi_host = \"$nedi_db_host\";/g\' -i $nms_automation_scripts_dir/extract_nedi.pl";
    	`$cmd`;
	$cmd = "sed \'s/nedi_dbuser =.*/nedi_dbuser = \"$nedi_db_user\";/g\' -i $nms_automation_scripts_dir/extract_nedi.pl";
    	`$cmd`;
	$cmd = "sed \'s/nedi_dbpwd =.*/nedi_dbpwd = \"$nedi_db_password\";/g\' -i $nms_automation_scripts_dir/extract_nedi.pl";
    	`$cmd`;
	$cmd = "sed \'s/cacti_database =.*/cacti_database = \"$cacti_db_name\";/g\' -i $nms_automation_scripts_dir/extract_cacti.pl";
    	`$cmd`;
	$cmd = "sed \'s/cacti_host =.*/cacti_host = \"$cacti_db_host\";/g\' -i $nms_automation_scripts_dir/extract_cacti.pl";
    	`$cmd`;
	$cmd = "sed \'s/cacti_dbuser =.*/cacti_dbuser = \"$cacti_db_user\";/g\' -i $nms_automation_scripts_dir/extract_cacti.pl";
    	`$cmd`;
	$cmd = "sed \'s/cacti_dbpwd =.*/cacti_dbpwd = \"$cacti_db_password\";/g\' -i $nms_automation_scripts_dir/extract_cacti.pl";
    	`$cmd`;

	`chown -R nagios:nagios $nms_automation_scripts_dir`;

	GWInstaller::AL::GWLogger::log("\tInstalling Schema Templates.");
	`cp -rf $gw_home/nms/tools/automation/templates/schema-* $gw_monarch_automation_templates_dir >> /dev/null 2>&1`;
	`chown -R nagios:nagios $gw_monarch_automation_templates_dir >> /dev/null 2>&1`;
	GWInstaller::AL::GWLogger::log("\tInstalling Service Profiles.");
	`cp -rf $gw_home/nms/tools/automation/templates/service_* $gw_profiles_dir >> /dev/null 2>&1 `;
        `cp -rf $gw_home/nms/tools/automation/templates/host-profile* $gw_profiles_dir >> /dev/null 2>&1 `;
	`chown -R nagios:nagios $gw_profiles_dir >> /dev/null 2>&1`;

        GWInstaller::AL::GWLogger::log("\tUpdating import_schema table with defaults.\n");
        `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password monarch < $gw_home/nms/tools/automation/templates/import_schema.sql >/dev/null 2>&1`;

        # Finish Up.
        GWInstaller::AL::GWLogger::log("\tDone.");
}

sub is_functional{
	return 1;
}
1;
