#!/usr/bin/perl
package GWNMSInstaller::AL::Cacti;
@ISA = qw(GWInstaller::AL::Software);
use GWNMSInstaller::AL::NMSProperties;
use Socket;

sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		rpm_name => 'groundwork-nms-cacti'
	};
 	
	bless($self,$class);
	$self->init();
	return $self;		
}

sub init{
	$self = shift;
	$self->set_database_name("cacti");
	$self->set_database_user("cactiuser");
	$self->set_database_password("cactiuser");
	$self->set_httpd("httpd_main");
	$self->set_database("mysql_main");
	$self->set_GWM_host("gwm_main");
}

sub save_values{
	my $self = shift;
	#UI Collection
	my $UICollection = $self->{UICollection};
	
	if($debug){GWInstaller::AL::GWLogger::log("save_values():SIZE: " . $UICollection->{array_size});}
	
	#GWM
	my $gwmObj = $UICollection->next();
	if($debug){GWInstaller::AL::GWLogger::log("save_values():OLD VALUE:" . $self->get_GWM_host() );}
     
	$self->set_GWM_host($gwmObj->get());
	if($debug){GWInstaller::AL::GWLogger::log("save_values():NEW VALUE:" . $self->get_GWM_host());}
	
	#MySQL
	my $dbObj = $UICollection->next();
	if($debug){GWInstaller::AL::GWLogger::log("save_values():OLD VALUE:" . $self->get_database());}
	$self->set_database($dbObj->get());
	if($debug){GWInstaller::AL::GWLogger::log("save_values():NEW VALUE:" . $self->get_database());}
	
	#DB Name
	$uiObj = $UICollection->next();
	if($debug){GWInstaller::AL::GWLogger::log("save_values():OLD VALUE:" . $self->get_database_name());}
	$self->set_database_name($uiObj->get());
	if($debug){GWInstaller::AL::GWLogger::log("save_values():NEW VALUE:" . $self->get_database_name());}
	
	#DB Username
	$uiObj = $UICollection->next();
	if($debug){GWInstaller::AL::GWLogger::log("save_values():OLD VALUE:" . $self->get_database_user());}
	$self->set_database_user($uiObj->get());
	if($debug){GWInstaller::AL::GWLogger::log("save_values():NEW VALUE:" . $self->get_database_user());}
		
	#DB Password
	$uiObj = $UICollection->next();
	if($debug){GWInstaller::AL::GWLogger::log("save_values():OLD VALUE:" . $self->get_database_password());}
	$self->set_database_password($uiObj->get());
	if($debug){GWInstaller::AL::GWLogger::log("save_values():NEW VALUE:" . $self->get_database_password());}
		
 }
sub configure{
	# configure(): this is a placeholder method. all functionality for this version will be in deploy()
	$success = 1;
	return $success;
}

sub deploy
{
    my ($self,$deploy_profile,$installer) = @_;
 	unless($deploy_profile){$deploy_profile = "local";}
 	my $propfile = $installer->get_properties();
    my @properties = ();
    my $result = 0;
    my $host_name = $installer->{hostname}; #GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn();

    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
	$num_properties = @properties;

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "nms", "cacti", $host_name);
        if (!($instance eq null))
        {
            $result = deploy_nms_cacti(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

#	========================
#	Deploy the Cacti service
#	========================

sub deploy_nms_cacti()
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "nms";
    $subtype = "cacti";

    my $enterprise_home = "/usr/local/groundwork/enterprise";
    my $nms_home = "/usr/local/groundwork/nms";
    my $cacti_home = "$nms_home/applications/cacti";
    my $cacti_spine_home = "$nms_home/applications/cacti-spine";
    my $php_home = "$nms_home/tools/php";
    my $patch = "$nms_home/tools/installer/cacti";

    GWInstaller::AL::GWLogger::log("\tDeploying Cacti Instance [$instance]");
    $db_type = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "database:type");
    $db_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database:port");
    $db_root_user = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database:root_user");
    $db_root_password = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database:root_password");
    $httpd_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "httpd:port");
    $database_name = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database_name");
    $database_user = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database_user");
    $database_password = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database_password");

    if ($deploy_profile eq 'local')
    {
        $db_host = GWNMSInstaller::AL::NMSProperties::shortname();
        $httpd_host = GWNMSInstaller::AL::NMSProperties::shortname();

    }
    else
    {
        $db_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database:host");
        $httpd_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "httpd:host");
    }

    if ($db_host == -1 || $db_type == -1 || $db_port == -1 || $db_root_user == -1 || $db_root_password == -1
    || $httpd_host == -1 || $httpd_port == -1 || $database_name == -1 || $database_user == -1 || $database_password == -1)
    {
	GWInstaller::AL::GWLogger::log("Unable to find complete set of properties to deploy Cacti.");
	return(0);
    }
    GWInstaller::AL::GWLogger::log("\t{");

    GWInstaller::AL::GWLogger::log("\tdb_host = $db_host");
    GWInstaller::AL::GWLogger::log("\tdb_type = $db_type");
    GWInstaller::AL::GWLogger::log("\tdb_port = $db_port");
    GWInstaller::AL::GWLogger::log("\tdb_root_user = $db_root_user");
    GWInstaller::AL::GWLogger::log("\tdb_root_password = [hidden]");
    GWInstaller::AL::GWLogger::log("\tdatabase_name = $database_name");
    GWInstaller::AL::GWLogger::log("\tdatabase_user = $database_user");
    GWInstaller::AL::GWLogger::log("\tdatabase_password = [hidden]");
    GWInstaller::AL::GWLogger::log("\thttpd_host = $httpd_host");
    GWInstaller::AL::GWLogger::log("\thttpd_port = $httpd_port");
    GWInstaller::AL::GWLogger::log("\t}");

    #
    # Cacti
    #

    if ((! -d $cacti_home) || (! -d $cacti_spine_home))
    {
        GWInstaller::AL::GWLogger::log("Cacti not installed at $cacti_home and $cacti_spine_home.");
	return(0);
    }

    #
    #   First, find out if there already is a cacti database.
    #

    if ($deploy_profile eq 'local')
    {
        `mysqldump --user=$db_root_user --password=$db_root_password cacti --no-data=true >/tmp/cacti_dump.tmp 2>&1`;
    }
    else
    {
        `mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host cacti --no-data=true >/tmp/cacti_dump.tmp 2>&1`;
    }

    $there_is_a_previous_cacti_database = find_in_file("/tmp/cacti_dump.tmp", "poller");
    $previous_database_is_up_to_date = find_in_file("/tmp/cacti_dump.tmp", "plugin_config");

    GWInstaller::AL::GWLogger::log("  There Is a Previous Cacti Database: " . $there_is_a_previous_cacti_database . "");
    GWInstaller::AL::GWLogger::log("  The Database Is Current: " . $previous_database_is_up_to_date . "");

    #
    #   Now proceed with figuring out what needs to be done:
    #   (1) Previous Database in earlier schema.
    #   (2) Previous database with up to date schema.
    #   (3) No Previous Database.
    #

    if (!$there_is_a_previous_cacti_database)
    {
        GWInstaller::AL::GWLogger::log("  No previous Cacti database found, Building new database.");
        if ($deploy_profile eq 'local')
        {
            `mysqladmin --user=$db_root_user --password=$db_root_password create $database_name`;
            `mysql --user=$db_root_user --password=$db_root_password $database_name < $cacti_home/cacti.sql`;
            `mysql --user=$db_root_user --password=$db_root_password $database_name < $patch/cacti.sql`;
        }
        else
        {
            `mysqladmin --user=$db_root_user --password=$db_root_password --host=$db_host create $database_name  >> nms.log 2>&1`;
            `mysql --user=$db_root_user --password=$db_root_password $database_name < $cacti_home/cacti.sql  >> nms.log 2>&1`;
            `mysql --user=$db_root_user --password=$db_root_password $database_name < $patch/cacti.sql  >> nms.log 2>&1`;
        }
    }
    else
    {
        if (!$previous_database_is_up_to_date)
        {
            my $dsn;

            GWInstaller::AL::GWLogger::log("  Migrating previous Cacti database to new schema.");
            GWInstaller::AL::GWLogger::log("    Backing up database from previous version of cacti.");
            `mkdir -p /usr/local/groundwork/nms/backups/cacti >/dev/null 2>/dev/null`;

            if ( -d "/usr/local/groundwork/cacti" )
            {
                GWInstaller::AL::GWLogger::log("    Copying previous version of cacti to nms/backups directory"); 
		`cp -rf /usr/local/groundwork/cacti /usr/local/groundwork/nms/backups >/dev/null`;
            }

            if ($deploy_profile eq 'local')
            {
                $dsn = "dbi:mysql:$database_name:localhost:$db_port";
                `mysqldump --user=$db_root_user --password=$db_root_password $database_name >/usr/local/groundwork/nms/backups/cacti/cacti.sql 2>&1`;
            }
            else
            {
                $dsn = "dbi:mysql:$database_name:$db_host:$db_port";
                `mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host $database_name >/usr/local/groundwork/nms/backups/cacti/cacti.sql 2>&1`;
            }

            `cp $patch/upgrade_database.php $cacti_home/install`;
            GWInstaller::AL::GWLogger::log("    Migrating database from earlier version of Cacti.");
            `pushd $cacti_home/install`;
            `$php_home/php upgrade_database.php  >> nms.log 2>&1`;
            `popd`;

            GWInstaller::AL::GWLogger::log("    Correcting rrd path location in database.");
            my $user = $db_root_user;
            my $pass = $db_root_password;

            my $dbh = DBI->connect($dsn, $user, $pass) or die "Can't connect to the DB: $DBI::errstr" ;
            my $sth = $dbh->prepare('select rrd_path,arg1 from poller_item');
            $sth->execute;

            while (@row = $sth->fetchrow_array())
            {
                #
                #   Update the RRD Paths.
                #

                $old_rrd_path = $row[0];
                $new_rrd_path = $row[0];
                $new_rrd_path =~ s|groundwork/cacti/rra|groundwork/nms/applications/cacti/rra|g;

                $usth = $dbh->prepare("update poller_item set rrd_path='" . $new_rrd_path . "' where rrd_path='" . $old_rrd_path . "'");
                $usth->execute;
                GWInstaller::AL::GWLogger::log("      Updated rrd path to: $new_rrd_path");

                #
                #   Now update the scripts location.
                #

                $old_arg1 = $row[1];
                $new_arg1 = $row[1];
                $new_arg1 =~ s|groundwork/cacti|groundwork/nms/applications/cacti|g;

                $usth = $dbh->prepare("update poller_item set arg1='" . $new_arg1 . "' where arg1='" . $old_arg1 . "'");
                $usth->execute;
                GWInstaller::AL::GWLogger::log("      Updated scripts location to: $new_arg1");
            }

            if ($deploy_profile eq 'local')
            {
                $alert_base_url = "http://" . shortname() . ":" . $httpd_port . "/cacti/";
            }
            else
            {
                $alert_base_url = "http://" . fqdn() . ":" . $httpd_port . "/cacti/";
            }

            #
            #   Now, Update the alert base url in the settings table to correctly point to our instance.
            #

            $usth = $dbh->prepare("update settings set value='" . $alert_base_url . "' where name='alert_base_url'");
            $usth->execute();
            GWInstaller::AL::GWLogger::log("      Updated alert base url to: $alert_base_url");
        }
	else
	{
		`mkdir -p /usr/local/groundwork/nms/backups/cacti >/dev/null 2>/dev/null`;
		`cp -rf /usr/local/groundwork/nms/applications/cacti/* /usr/local/groundwork/nms/backups/cacti`;
	}
    }

    #
    #   Okay, now the database itself should exist in the new schema
    #   format, whether it was created anew, or whether it
    #   was migrated from a previous version.
    #

    GWInstaller::AL::GWLogger::log("  Creating database user permissions and settings.");
    if (!open(SQL, ">/tmp/build_$instance.tmp"))
	{
		GWInstaller::AL::GWLogger::log("Couldn't create temporary build file."); return(0);
	}
    printf SQL "grant all on $database_name.* to $database_user identified by '$database_password';";
    printf SQL "flush privileges;";
    printf SQL "lock tables settings write;";
    printf SQL "UPDATE settings SET value='$nms_home/applications/cacti-spine/bin/spine' WHERE name='path_spine';";
    printf SQL "UPDATE settings SET value='2' WHERE name='poller_type';";
    printf SQL "unlock tables;";
    printf SQL "lock tables version write;";
    printf SQL "UPDATE version SET cacti='0.8.7b';";
    printf SQL "unlock tables;";
    close(SQL);

    if ($deploy_profile eq 'local')
    {
            `mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp >> nms.log 2>&1`;
    }
    else
    {
            `mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp  >> nms.log 2>&1`;
    }

    #
    #   Patch the plugin database if needed.
    #

    GWInstaller::AL::GWLogger::log("  Patching plugin database.");
    if ($deploy_profile eq 'local')
    {
        `mysql --user=$database_user --password=$database_password $database_name < $patch/pa.sql 2>>nms.log`;
    }
    else
    {
        `mysql --user=$database_user --password=$database_password --host=$db_host $database_name < $patch/pa.sql 2>>nms.log`;
    }

    #
    #   Move existing rrd's
    #

    GWInstaller::AL::GWLogger::log("  Checking for existing rrd's.");

    if ( -d "/usr/local/groundwork/cacti/rra" )
    {
        GWInstaller::AL::GWLogger::log("    Copying rrd's from previous deployment to new location.");
        `cp -r /usr/local/groundwork/cacti/rra/* /usr/local/groundwork/nms/applications/cacti/rra`;
    }

    #
    # Patch Files
    #

    GWInstaller::AL::GWLogger::log("  Patching files.");
    GWInstaller::AL::GWLogger::log("    Patching PHP files.");
#   `cp -f $patch/0_8_6j_to_0_8_7.php /usr/local/groundwork/nms/applications/cacti/install`;
    `cp -f $patch/index.php /usr/local/groundwork/nms/applications/cacti/install`;
    `cp -f $patch/add_tree.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_graphs.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_device.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_perms.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/ss_fping.php /usr/local/groundwork/nms/applications/cacti/scripts`;

    GWInstaller::AL::GWLogger::log("    Patching Perl files.");
    `cp -f $patch/loadavg.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/unix_tcp_connections.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/diskfree.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/weatherbug.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/webhits.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/loadavg_multi.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/unix_users.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/ping.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/query_unix_partitions.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/3com_cable_modem.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/unix_processes.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/linux_memory.pl /usr/local/groundwork/nms/applications/cacti/scripts`;
    `cp -f $patch/weathermap-cacti-rebuild.php /usr/local/groundwork/nms/applications/cacti/plugins/weathermap`;
            
    GWInstaller::AL::GWLogger::log("    Patching configuration files.");
    $cmd = "sed \'s/database_hostname.*/database_hostname = \"$db_host\";/g\' -i $cacti_home/include/config.php";
    `$cmd`; 
                
    GWInstaller::AL::GWLogger::log("    Importing Cacti Data Query Template.");
    $cmd = "$php_home/bin/php $enterprise_home/bin/components/cacti/cacti_data_query_add.php >/dev/null 2>&1";
    `$cmd`; 
                
    # X. Set Permissions.
    GWInstaller::AL::GWLogger::log("  Setting permissions.");
    `chown -R nagios:nagios $cacti_home`;
    `chown -R nagios:nagios $cacti_spine_home`;
    `chmod u+s $cacti_home/cli/cacti_ping_executor`;

            
    # X. Create crontab.
    GWInstaller::AL::GWLogger::log("  Adding entry to crontab.");
            
    my $file1="/tmp/build1_$instance.tmp";
    my $file2="/tmp/build2_$instance.tmp";
#    my $cronentry="*/5 * * * * ($php_home/bin/php $cacti_home/poller.php ; $nms_home/tools/automation/scripts/extract_cacti.pl ) > /dev/null 2>&1";
    my $cronentry="*/5 * * * * /usr/local/groundwork/common/bin/cacti_cron.sh > /dev/null 2>&1";

    `crontab -u nagios -l >$file1 2>/dev/null`;
    `cat $file1 | grep -v "poller.php" > $file2`;
    `echo "$cronentry" >>$file2`;
    `crontab -u nagios $file2`;

    # Finish Up.
    GWInstaller::AL::GWLogger::log("\tDone.");
}

#	==============================
#	Deploy the Cacti Guava Package
#	==============================

sub deploy_package {
	
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

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "application", "cacti-pkg", $host_name);
        if (!($instance eq null))
        {
	    #printf "DEBUG: {deploy} instance not null, instance=$instance";
            $result = deploy_application_cacti_pkg(\@properties, $instance, $deploy_profile);
        }
    }
    
  my ($shortname,undef) = split('\.',$host_name);
    ### Do Cacti & Cacti SV Graph GW Monitor Application Install
    if(-e '/usr/local/groundwork/php/bin/php'){
		#cacti application
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php Cacti install $shortname  >/dev/null 2>&1";
    	print `$cmd`;
    	
    	#cacti sv graphs
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php svcactigraph install $shortname  >/dev/null 2>&1";
    	print `$cmd`;
    }
    else{
    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to install Cacti GW Monitor Application.");

    }
 
    return($result);
}


sub remove_gwm_application{
    my ($installer) = @_;
    my $propfile = $installer->get_properties();
    my @properties = ();
    my $result = 0;
    my $host_name = $propfile->get_fqdn();
  	my ($shortname,undef) = split('\.',$host_name);
  	my ($shortname,undef) = split('\.',$host_name);

    ### Do Database remove
    if(-e '/usr/local/groundwork/php/bin/php'){
		#cacti gwm application
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php cacti remove $shortname >/dev/null 2>&1";
    	print `$cmd`;

    	#cacti sv graphs
    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php svcactigraph remove $shortname  >/dev/null 2>&1";
    	print `$cmd`;
    	
    	#cacti sv graphs
    	$cmd = "rm -rf /usr/local/groundwork/core/guava/htdocs/guava/packages/svcactigraph  >/dev/null 2>&1";
    	print `$cmd`;
    	
    }
    else{

    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to remove Cacti GW Monitor Application.");
    }	
    
 
}


sub deploy_application_cacti_pkg()
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};
    $type = "application";
    $subtype = "cacti-pkg";

        GWInstaller::AL::GWLogger::log("\tDeploying Cacti Instance [$instance] $deploy_profile");

	$package_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "host");
	$cacti_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:httpd:host");
	$cacti_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:httpd:port");
	$cacti_db_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:database:host");
	$cacti_db_name = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:database_name");
	$cacti_db_user = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:database_user");
	$cacti_db_pass = GWNMSInstaller::AL::NMSProperties::find_property(\@properties, $type, $subtype, $instance, "cacti:database_password");

        if ($package_host == -1 || $cacti_host == -1 || $cacti_port == -1 || $cacti_db_host == -1 || $cacti_db_name == -1 || $cacti_db_user == -1 || $cacti_db_pass == -1)
        {
                GWInstaller::AL::GWLogger::log("WARNING:Unable to find complete set of properties to deploy Cacti Package, Aborting.");
		return(0);
        }

	$gw_httpd_conf_dir = "/usr/local/groundwork/apache2/conf";
	if (!(-d $gw_httpd_conf_dir))
	{
                GWInstaller::AL::GWLogger::log("WARNING:Directory '$gw_httpd_conf_dir' not found, Aborting.");
	}

        GWInstaller::AL::GWLogger::log("\t{");
        GWInstaller::AL::GWLogger::log("\tcacti:httpd:host = $cacti_host");
        GWInstaller::AL::GWLogger::log("\tcacti:httpd:port = $cacti_port");
        GWInstaller::AL::GWLogger::log("\tcacti:database:host = $cacti_db_host");
	GWInstaller::AL::GWLogger::log("\tcacti:database_name = $cacti_db_name");
	GWInstaller::AL::GWLogger::log("\tcacti:database_user = $cacti_db_user");
	GWInstaller::AL::GWLogger::log("\tcacti:database_pass = $cacti_db_pass");
        GWInstaller::AL::GWLogger::log("\t}");

        #
        # Cacti
        #

	my $gw_home = "/usr/local/groundwork";
        my $sourcedir = "$gw_home/nms/tools/installer/guava-packages/cacti";
	my $guavapackagedir="$gw_home/core/guava/htdocs/guava/packages";
	my $c_dot = index($cacti_host, ".");
	my $cacti_short_hostname=$cacti_host;
	my $cacti_short_hostname_as_var;

	my $p_dot = index($package_host, ".");
	my $package_short_hostname=$package_host;


	if ($c_dot != -1)
	{
		$cacti_short_hostname = substr($cacti_host,0,$c_dot);
	}
	if ($p_dot != -1)
	{
		$package_short_hostname = substr($package_host,0,$p_dot);
	}
 	$cacti_short_hostname_as_var = $cacti_short_hostname;
	$cacti_short_hostname_as_var =~ s/-/_/g;

	my $dirname="cacti_$cacti_short_hostname";
	my $patchfile="$guavapackagedir/$dirname/views/cacti.inc.php";

        if (! -d $guavapackagedir)
        {
                GWInstaller::AL::GWLogger::log("WARNING:Can only install a Cacti package if Groundwork is installed.");
        }

	# Delete Old Version
	GWInstaller::AL::GWLogger::log("\tCleaning previous Cacti Package.");
	`rm -rf $guavapackagedir/$dirname`;
	`rm -rf $guavapackagedir/svcactigraph`;

	# Installing New version.
	GWInstaller::AL::GWLogger::log("\tInstalling new Cacti Package.");
	`mkdir $guavapackagedir/$dirname`;
	`cp -rf $sourcedir/* $guavapackagedir/$dirname`;
	`mkdir $guavapackagedir/svcactigraph`;
	`cp -rf $gw_home/nms/tools/installer/guava-packages/svcactigraph/* $guavapackagedir/svcactigraph`;

	# Now Patch the svcactigraph package.
	$cacti_url = "http:\\/\\/$cacti_host:$cacti_port\\/cacti\\/";
	$cmd = 'sed "s/this->cactiurl =.*/this->cactiurl = \"' . $cacti_url . '\";/g" -i ' . $guavapackagedir . '/svcactigraph/support/svcactigraphs.inc.php';
	`$cmd`;

	$cmd = 'sed "s/dbHost =.*/dbHost = ' . "'" . $cacti_db_host . "'" . ';/g" -i ' . $guavapackagedir . '/svcactigraph/sysmodules/svcactigraphs.inc.php';
	`$cmd`;
	$cmd = 'sed "s/dbUsername =.*/dbUsername = ' . "'" . $cacti_db_user . "'" . ';/g" -i ' . $guavapackagedir . '/svcactigraph/sysmodules/svcactigraphs.inc.php';
	`$cmd`;
	$cmd = 'sed "s/dbPassword =.*/dbPassword = ' . "'" . $cacti_db_pass . "'" . ';/g" -i ' . $guavapackagedir . '/svcactigraph/sysmodules/svcactigraphs.inc.php';
	`$cmd`;
	$cmd = 'sed "s/dbDatabase =.*/dbDatabase = ' . "'" . $cacti_db_name . "'" . ';/g" -i ' . $guavapackagedir . '/svcactigraph/sysmodules/svcactigraphs.inc.php';
	`$cmd`;

	# Install plugins.
	GWInstaller::AL::GWLogger::log("\tInstalling new Cacti plugins and supporting files.");
	`cp -f $gw_home/nms/tools/installer/plugins/cacti/scripts/*.pl $gw_home/nagios/libexec`;
	`chown nagios:nagios $gw_home/nagios/libexec/check_cacti*pl`;
	#`chown nagios:nagios $gw_home/nagios/libexec/*.pl`;
	`cp -f $gw_home/nms/tools/installer/plugins/cacti/config/check_cacti.conf $gw_home/common/etc`;
	`sed 's/cacti_db_host.*/cacti_db_host = $cacti_db_host/g' -i $gw_home/common/etc/check_cacti.conf`;
	`sed 's/cacti_db_name.*/cacti_db_name = $cacti_db_name/g' -i $gw_home/common/etc/check_cacti.conf`;
	`sed 's/cacti_db_user.*/cacti_db_user = $cacti_db_user/g' -i $gw_home/common/etc/check_cacti.conf`;
	`sed 's/cacti_db_pass.*/cacti_db_pass = $cacti_db_pass/g' -i $gw_home/common/etc/check_cacti.conf`;
	`sed 's/enable_processing.*/enable_processing = yes/g' -i $gw_home/common/etc/check_cacti.conf`;
	`sed 's/check_cacti_host.*/check_cacti_host = "$package_short_hostname"/g' -i $gw_home/common/etc/check_cacti.conf`;
	`chown nagios:nagios $gw_home/common/etc/check_cacti.conf`;
	`chmod 600 $gw_home/common/etc/check_cacti.conf`;

	# Patch with correct location of server.
	GWInstaller::AL::GWLogger::log("\tConfiguring for server location.");
	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $cacti_host . ':' . $cacti_port . '";/g' . "'" . " $patchfile >$patchfile.new";
	`$command`;
	`rm -f $patchfile`;
	`mv $patchfile.new $patchfile`;

	my $description = "Cacti [$cacti_short_hostname]";
	$patchfile = "$guavapackagedir/$dirname/package.pkg";
	$command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
	`$command`;
	$command = "sed 's/shortname = .*/shortname = cacti_$cacti_short_hostname_as_var/g' -i $patchfile";
	`$command`;
	$command = "sed 's/classname = .*/classname = cactiView$cacti_short_hostname_as_var/g' -i $patchfile";
	`$command`;
	$patchfile = "$guavapackagedir/$dirname/views/cacti.inc.php";
	$command = "sed 's/class cactiView .*/class cactiView$cacti_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
	`$command`;
	`chown -R nagios:nagios $guavapackagedir/$dirname`;

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
	`$cmd`;
	$cmd = "sed 's/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork.*/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork\\/\*\.conf\\nInclude \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/nms\\/\*\.conf/g' -i $gw_httpd_conf_dir/httpd.conf";
	`$cmd`;

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
        printf NMS_HTTPD "####\tnms-httpd.conf####\tAuto-Generated by Enterprise Deployment Subsystem.##\t(C) 2008 Groundwork Open Source.####\tDo Not Manually Edit!##\n";
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
	`chown -R nagios:nagios $gw_nms_httpd_conf_dir`;

	GWInstaller::AL::GWLogger::log("\tPatching guava.");
	$domain = @host_domain[1];
	$cmd = "sed 's/setrawcookie(\$cookie_name, \$ticket, null, \$path);/setrawcookie(\$cookie_name,\$ticket, null, \$path, \"$domain\");/g' -i $gw_home/core/guava/htdocs/guava/includes/guava.inc.php";
	`$cmd`;

        # X. Finish Up.
	GWInstaller::AL::GWLogger::log("\tRestarting Groundwork Apache Server.");
	`/etc/init.d/httpd restart  >/dev/null 2>&1`;
	GWInstaller::AL::GWLogger::log("\tDone.");
}

sub find_in_file
{
    $filename = shift;
    $search_string = shift;

    open(MYINPUTFILE, "<$filename");
    my(@lines) = <MYINPUTFILE>;
    @lines = sort(@lines);

    my($line);
    foreach $line (@lines) # loop thru list
    {
        $result = index($line, $search_string);
        if ($result != -1)
        {
            close(MYINPUTFILE);
            return(1);
        }
    }
    close(MYINPUTFILE);
    return(0);
}

sub is_functional
{
	$self = shift;
	$retval = 0;
	$functionality = 0;
	my $STATUS_GOOD			= 0;
	my $STATUS_NOT_INSTALLED	= 1;
	my $STATUS_CORRUPT_INSTALLATION	= 2;
	my $STATUS_NOT_RUNNING		= 3;
	my @status_text =
	(
		"Good.",
		"RPM Package Not Installed.",
		"Corrupt Installation.",
		"Package Installed, Not Running."
	);
			
	$rpm_package_prefix = 'groundwork-nms-cacti';
	$gw_home = '/usr/local/groundwork';
	$nms_home = $gw_home . '/nms';
	$component_home = $nms_home . '/applications/cacti';

	#
	#	First, check to see if the package is installed.
	#

	`rpm -qa $rpm_package_prefix`;
	$package_installed = ($? == 0 ? 1 : 0);
	if (!$package_installed)
	{
		$functionality = $STATUS_NOT_INSTALLED;
	}

	#
	#	Next, check to see if the component
	#	is operational.
	#

	# Check to see if it is installed correctly.

	if ( ! (-e "$component_home/poller.php" ))
	{
		$functionality =$STATUS_CORRUPT_INSTALLATION;
	}

	# Check to see if it is in the crontab.

	`crontab -u nagios -l | grep poller`;
	$component_in_crontab = ($? == 0 ? 1 : 0);

	if ($component_in_crontab)
	{
		$functionality =$STATUS_GOOD;
	}
	else
	{
		return($STATUS_NOT_RUNNING);
	}
	if($func != 0){
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
