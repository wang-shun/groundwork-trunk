#!/usr/bin/perl
package GWNMSInstaller::AL::NeDi;
@ISA = qw(GWInstaller::AL::Software);
#use GWNMSInstaller::AL::NMSProperties;
#use GWInstaller::AL::GWLogger::log;
use Socket;
sub new{
	 
 	my ($invocant,$section,@parameters) = @_;
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		rpm_name => 'groundwork-nms-nedi'
	};
 	
	bless($self,$class);
	$self->init();
	return $self;		
}
sub init{
	$self = shift;
	$self->set_database_name("nedi");
	$self->set_database_user("nediuser");
	$self->set_database_password("nediuser");
	$self->set_httpd("httpd_main");
	$self->set_database("mysql_main");
	$self->set_GWM_host("gwm_main");
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
sub deploy
{
    my ($self,$deploy_profile,$installer) = @_;
	if($deploy_profile eq ""){$deploy_profile = "local";}
    my @properties = ();
    my $result = 0;
    my $host_name = $installer->{hostname}; # GWNMSInstaller::AL::NMSProperties::configuration_get_fqdn();
	my $propfile = $installer->get_properties();
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
        $num_properties = @properties;

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "nms", "nedi", $host_name);
        if (!($instance eq null))
        {
            $result = deploy_nms_nedi(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

#     ===================
#     Deploy NeDi Service
#     ===================

sub deploy_nms_nedi
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};

    $type = "nms";
    $subtype = "nedi";

    my $enterprise_home = "/usr/local/groundwork/enterprise";
    my $nms_home = "/usr/local/groundwork/nms";
    my $nedi_home = "$nms_home/applications/nedi";
    my $php_home = "$nms_home/tools/php";

	##get properties
	#################
    GWInstaller::AL::GWLogger::log("\tDeploying NeDi Instance [$instance ] $deploy_profile");
    $nedi_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "host");
    $db_type = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "database:type");
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

    if ($db_host == -1 || $db_type == -1 || $db_port == -1 || $db_root_user == -1 || $db_root_pass == -1
    || $httpd_host == -1 || $httpd_port == -1 || $database_name == -1 || $database_user == -1 || $database_password == -1)
    {
        GWInstaller::AL::GWLogger::log("Unable to find complete set of properties to deploy NeDi.");
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
    # NeDi
    #

    if ((! -d $nedi_home))
    {
	GWInstaller::AL::GWLogger::log("NeDi not installed at $nedi_home.");
	return(0);
    }

    #
    #   Patch Perl Modules
    #

    GWInstaller::AL::GWLogger::log("  Patching files.");
    my $patch = "$enterprise_home/bin/components/nedi";
    my $INSTALLDIR = $nedi_home;

    GWInstaller::AL::GWLogger::log("    Patching Perl files.");
    `cp -f $patch/moni.pl $INSTALLDIR`;
    `cp -f $patch/syslog.pl $INSTALLDIR`;
    `cp -f $patch/trap.pl $INSTALLDIR`;
    `cp -f $patch/nedi.pl $INSTALLDIR`;
    `cp -f $patch/Devsend.pl $INSTALLDIR/html/inc`;
    `cp -f $patch/nediportcapacity.pl $INSTALLDIR/contrib`;
    `cp -f $patch/flood.pl $INSTALLDIR/contrib`;
    `cp -f $patch/nediDeviceConnections.pl $INSTALLDIR/contrib`;
    `cp -f $patch/nbt.pl $INSTALLDIR/contrib`;
    `cp -f $patch/libmisc.pl $INSTALLDIR/inc`;
    `cp -f $patch/index.php $INSTALLDIR/html`;

    #
    #   Patch PHP Modules.
    #

    GWInstaller::AL::GWLogger::log("    Patching PHP files.");
    `cp -f $patch/libmisc.php $INSTALLDIR/html/inc`;

    #
    #   Patch other.def
    #

    GWInstaller::AL::GWLogger::log("    Patching other.def.");
    `sed "s/Dispro.*/Dispro CDP/g" -i $nedi_home/sysobj/other.def`;

    #
    #   Prepare for database initialization.
    #

    GWInstaller::AL::GWLogger::log("  Copying database settings to nedi.conf.");
    `sed 's/dbuser.*/dbuser\t\t$database_user/g' $nedi_home/nedi.conf >/tmp/deploy_nedi1.conf`;
    `sed 's/dbname.*/dbname\t\t$database_name/g' /tmp/deploy_nedi1.conf >$nedi_home/nedi.conf`;
    `sed 's/dbhost.*/dbhost\t\t$db_host/g' $nedi_home/nedi.conf >/tmp/deploy_nedi1.conf`;
    `sed 's/dbpass.*/dbpass\t\t$database_password/g' /tmp/deploy_nedi1.conf >$nedi_home/nedi.conf`;
    `sed 's/rrdstep.*/rrdstep\t\t14400/g' -i $nedi_home/nedi.conf`;
    `sed 's/usr/#usr/g' -i $nedi_home/nedi.conf`;

    #
    #   Determine state of current database if there is one.
    #

    if ($deploy_profile eq 'local')
    {
        `/usr/local/groundwork/mysql/bin/mysqldump --user=$db_root_user --password=$db_root_password $database_name --no-data=true >/tmp/nedi_dump.tmp 2>&1`;
    }
    else
    {
        `/usr/local/groundwork/mysql/bin/mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host $database_name --no-data=true >/tmp/nedi_dump.tmp 2>&1`;
    }

    $there_is_a_previous_nedi_database = find_in_file("/tmp/nedi_dump.tmp", "configs");
    $previous_database_is_up_to_date = find_in_file("/tmp/nedi_dump.tmp", "iftrack");
    GWInstaller::AL::GWLogger::log("  There Is a Previous NeDi Database: " . $there_is_a_previous_nedi_database . "");
    GWInstaller::AL::GWLogger::log("  The Database Is Current: " . $previous_database_is_up_to_date . "");

    #
    #   Now, normalize the database (create or migrate it)
    #

    GWInstaller::AL::GWLogger::log("  Creating backup directory.");
    `mkdir -p /usr/local/groundwork/nms/backups/nedi >/dev/null 2>/dev/null`;

    #
    #   If there is a previous database that is out of date,
    #   then back it up and migrate it.
    #

    if ($there_is_a_previous_nedi_database && !$previous_database_is_up_to_date)
    {
        #
        #   Back up previous database.
        #

        GWInstaller::AL::GWLogger::log("  Backing up database from previous installation of nedi.");
        if ($deploy_profile eq 'local')
        {
            `/usr/local/groundwork/mysql/bin/mysqldump --skip-opt --no-create-db --no-create-info --user=$db_root_user --password=$db_root_password $database_name user configs devices monitoring links >/usr/local/groundwork/nms/backups/nedi/nedi.sql 2>&1`;
        }
        else
        {
            `/usr/local/groundwork/mysql/bin/mysqldump --skip-opt --no-create-db --no-create-info --user=$db_root_user --password=$db_root_password --host=$db_host $database_name user devices configs monitoring links >/usr/local/groundwork/nms/backups/nedi/nedi.sql 2>&1`;
        }

        #
        #   As an addendum to the above mysqldump, we need to change all INSERT's to REPLACE,
        #   since not all versions of mysqldump allow for the --replace option.
        #

        `sed s/INSERT/REPLACE/g -i /usr/local/groundwork/nms/backups/nedi/nedi.sql`;

        #
        #   Now, drop old database, since we're going to
        #   have nedi create the new one.
        #

        GWInstaller::AL::GWLogger::log("  Cleaning old database and user.");
        if (!open(SQL, ">/tmp/build_$instance.tmp"))
	{
		GWInstaller::AL::GWLogger::log("Couldn't create temporary build file."); return(0);
	}
        printf SQL "drop database $database_name;";
        printf SQL "drop user $database_user;";
        printf SQL "flush privileges;";
        close(SQL);

        if ($deploy_profile eq 'local')
        {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password $database_name </tmp/build_$instance.tmp >/dev/null 2>&1`;
        }
        else
        {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name </tmp/build_$instance.tmp >/dev/null 2>&1`;
        }
    }

    #
    #   If there is a previous database that is already up to date,
    #   then we leave that database alone. Otherwise, we
    #   create it anew.
    #

    if ($there_is_a_previous_nedi_database && $previous_database_is_up_to_date)
    {
        GWInstaller::AL::GWLogger::log("  Existing Database with up-to-date schema, skipping the initialization.");
    }
    else
    {
        # X. Initialize Database.
        GWInstaller::AL::GWLogger::log("  Initializing NeDi Database.");

        `echo "$db_root_user" >/tmp/build_nedi.tmp`;
        `echo "$db_root_password" >>/tmp/build_nedi.tmp`;
        `echo "$nedi_host" >>/tmp/build_nedi.tmp`;
        `mysqladmin flush-hosts >/dev/null 2>&1`;
        `$nedi_home/nedi.pl -i </tmp/build_nedi.tmp`;

        #
        #   Okay, we've created it. Do we need
        #   to migrate a previous database?
        #

        if ($there_is_a_previous_nedi_database)
        {
            GWInstaller::AL::GWLogger::log("  Migrating previous database to new schema.");
            GWInstaller::AL::GWLogger::log("    Copying data from previous nedi database to new database.");

            if ($deploy_profile eq 'local')
            {
                `cat /usr/local/groundwork/nms/backups/nedi/nedi.sql | mysql --user=$db_root_user --password=$db_root_password $database_name  >/dev/null 2>&1`;
            }
            else
            {
                `cat /usr/local/groundwork/nms/backups/nedi/nedi.sql | mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name  >/dev/null 2>&1`;
            }

            #
            #   Now, update the database.
            #

            GWInstaller::AL::GWLogger::log("    Updating users table with new themes and language preferences.");
            GWInstaller::AL::GWLogger::log("    Updating devices table with new columns.");
            if (!open(SQL, ">/tmp/build_$instance.tmp"))
			{
				GWInstaller::AL::GWLogger::log("Couldn't create temporary build file."); return(0);
			}
            printf SQL "UPDATE user SET language='english' WHERE language='eng';";
            printf SQL "UPDATE user SET theme='default';";
            printf SQL "UPDATE devices SET cpu='',memcpu='',memio='',temp='';";
            close(SQL);
            if ($deploy_profile eq 'local')
            {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
            }
            else
            {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
            }

            #
            #   Build new user privileges to the database to make sure
            #   NeDi has the rights to access it.
            #

            GWInstaller::AL::GWLogger::log("    Building new user privileges.");
            if (!open(SQL, ">/tmp/build_$instance.tmp"))
			{
				GWInstaller::AL::GWLogger::log("Couldn't create temporary build file."); return(0);
			}
            printf SQL "grant all on $database_name.* to '$database_user' identified by '$database_password';";
            printf SQL "flush privileges;";
            close(SQL);
            if ($deploy_profile eq 'local')
            {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
            }
            else
            {
                `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
            }
        }
    }

    #   
    #   If there are existing nedi files in the expected location for 5.1.3 NeDi,
    #   then copy them to the backups directory.
    #   

    if ( -d "/usr/local/groundwork/nedi" )
    {   
        GWInstaller::AL::GWLogger::log("  Copying previous version of nedi to nms/backups directory");
        `cp -rf /usr/local/groundwork/nedi /usr/local/groundwork/nms/backups`;
    }   
        
    #
    #   Copy old seedlist and rrds.
    #   
                
    if ( -e "/usr/local/groundwork/nedi/seedlist" )
    {   
        GWInstaller::AL::GWLogger::log("  Copy seedlist and rrds from previous installation.");
        `cp -f /usr/local/groundwork/nedi/seedlist /usr/local/groundwork/nms/applications/nedi >/dev/null`;
        `cp -rf /usr/local/groundwork/nedi/rrd/* /usr/local/groundwork/nms/applications/nedi/rrd >/dev/null`;
    }

    #
    #   Create extended permissions for network users.
    #
    
    GWInstaller::AL::GWLogger::log("  Building new user privileges.");
    open(SQL, ">/tmp/build_$instance.tmp") || GWInstaller::AL::GWLogger::log("Couldn't create temporary build file.");
    printf SQL "grant all on $database_name.* to '$database_user' identified by '$database_password';";
    printf SQL "flush privileges;";
    close(SQL);
    if ($deploy_profile eq 'local')
    {
            `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
    }   
    else
    {
            `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp  >/dev/null 2>&1`;
    }   
        
    #   
    #   Set Permissions.
    #
        
    GWInstaller::AL::GWLogger::log("  Setting filesystem permissions.");
    `chown -R nagios:nagios $nedi_home`;
        
    #
    #   Create crontab.
    #   
            
    GWInstaller::AL::GWLogger::log("  Adding entry to crontab.");

    my $file1="/tmp/build1_$instance.tmp";
    my $file2="/tmp/build2_$instance.tmp";
    my $perl_home="/usr/local/groundwork/nms/tools/perl";
    my $cronentry_one="0 4,8,12,16,20 * * * ($perl_home/bin/perl $nedi_home/nedi.pl -clo ; $nms_home/tools/automation/scripts/extract_nedi.pl )> /dev/null 2>&1";
    my $cronentry_two="0 0 * * * $perl_home/bin/perl $nedi_home/nedi.pl -clob > /dev/null 2>&1";
            
    `crontab -u nagios -l >$file1 2>/dev/null`;
    `cat $file1 | grep -v "nedi.pl" > $file2`;
    `echo "$cronentry_one" >>$file2`;
    `echo "$cronentry_two" >>$file2`;
    `crontab -u nagios $file2`;
            
    #
    #   Finish Up.
    #       

        GWInstaller::AL::GWLogger::log("\tDone.");
}

#     =========================
#     Deploy NeDi Guava Package
#     =========================

sub deploy_package
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
	# GWInstaller::AL::GWLogger::log("DEBUG: {deploy} properties not null NEW ID: " . $self->get_identifier());
	$num_properties = @properties;
	# GWInstaller::AL::GWLogger::log("DEBUG: {deploy} num_properties=$num_properties");

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "application", "nedi-pkg", $host_name);
        if (!($instance eq null))
        {
	   #GWInstaller::AL::GWLogger::log("DEBUG: {deploy} instance not null, instance=$instance");
            $result = deploy_application_nedi_pkg(\@properties, $instance, $deploy_profile);
        }
    }
      my ($shortname,undef) = split('\.',$host_name);
    
    ### Do Database install
#1    if(-e '/usr/local/groundwork/php/bin/php'){
#1    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php NeDi install $shortname >/dev/null 2>&1";
#1    	print `$cmd`;
#1    }
#1    else{

#1    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
#1    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to install NeDi GW Monitor Application.");
#1    }

    return($result);
}

#1sub remove_gwm_application{
#1    my ($installer) = @_;
#1    my $propfile = $installer->get_properties();
#1    my @properties = ();
#1    my $result = 0;
#1    my $host_name = $propfile->get_fqdn();
#1      my ($shortname,undef) = split('\.',$host_name);

    ### Do Database install
#1    if(-e '/usr/local/groundwork/php/bin/php'){
#1    	$cmd = "/usr/local/groundwork/php/bin/php ./bin/install_application.php nedi remove $shortname >/dev/null 2>&1";
#1    	print `$cmd`;
#1    }
#1    else{
#1
#1    	GWInstaller::AL::GWLogger::log("WARNING: There may be something wrong with your GW Monitor installation. Unable to find php binary.");
#1    	GWInstaller::AL::GWLogger::log("WARNING: Installer will not be able to remove NeDi GW Monitor Application.");
#1    }
    
    
    #Remove the files from the Guava Packages Directory
    
#1     $rmCMD = "rm -rf /usr/local/groundwork/core/guava/htdocs/guava/packages/nedi_" . $host_name . " >/dev/null 2>&1";
#1     print `$rmCMD`;	
#1}

sub deploy_application_nedi_pkg
{
	my ($ref_properties, $instance, $deploy_profile) = @_;
    	my @properties = @{$ref_properties};

	$type = "application";
	$subtype = "nedi-pkg";

    GWInstaller::AL::GWLogger::log("\tDeploying Nedi Instance [$instance]");
    $package_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type, $subtype, $instance, "host");
	$nedi_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type, $subtype, $instance, "nedi:httpd:host");
	$nedi_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type, $subtype, $instance, "nedi:httpd:port");
        if ($package_host == -1 || $nedi_host == -1 || $nedi_port == -1)
        {
                GWInstaller::AL::GWLogger::log("Unable to find complete set of properties to deploy Nedi Package.");
		return(0);
        }
        GWInstaller::AL::GWLogger::log("\t{");

        GWInstaller::AL::GWLogger::log("\tnedi:httpd:host = $nedi_host");
        GWInstaller::AL::GWLogger::log("\tnedi:httpd:port = $nedi_port");
        GWInstaller::AL::GWLogger::log("\t}");

	$gw_httpd_conf_dir = "/usr/local/groundwork/apache2/conf";
        if (!(-d $gw_httpd_conf_dir))
        {
                GWInstaller::AL::GWLogger::log("Directory '$gw_httpd_conf_dir' not found, Aborting.");
        }

        #
        # Nedi
        #

#1        my $sourcedir = "/usr/local/groundwork/nms/tools/installer/guava-packages/nedi";
#1	my $guavapackagedir="/usr/local/groundwork/core/guava/htdocs/guava/packages";
	my $dot = index($nedi_host, ".");
	my $nedi_short_hostname=$nedi_host;
        my $nedi_short_hostname_as_var;
        my $p_dot = index($package_host, ".");
        my $package_short_hostname=$package_host;

	if ($dot != -1)
        {
                $nedi_short_hostname = substr($nedi_host,0,$dot);
        }
        if ($p_dot != -1)
        {
                $package_short_hostname = substr($package_host,0,$p_dot);
        }

        $nedi_short_hostname_as_var = $nedi_short_hostname;
        $nedi_short_hostname_as_var =~ s/-/_/g;

	my $dirname="nedi_$nedi_short_hostname";
#1	my $patchfile="$guavapackagedir/$dirname/views/nedi.inc.php";

#1        if (! -d $guavapackagedir)
#1        {
#1                GWInstaller::AL::GWLogger::log("Can only install a Nedi package if Groundwork is installed.");
#1        }

	# Delete Old Version
	GWInstaller::AL::GWLogger::log("\tCleaning previous Nedi Package.");
#1	`rm -rf $guavapackagedir/$dirname`;

	# Installing New version.
	GWInstaller::AL::GWLogger::log("\tInstalling new Nedi Package.");
#1	`mkdir $guavapackagedir/$dirname`;
#1	`cp -rf $sourcedir/* $guavapackagedir/$dirname`;

#1	# Patch with correct location of server.
#1	GWInstaller::AL::GWLogger::log("\tConfiguring for server location.");
#1	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $nedi_host . ':' . $nedi_port . '";/g' . "'" . " $patchfile >$patchfile.new";
#1	`$command`;
#1	`rm -f $patchfile`;
#1	`mv $patchfile.new $patchfile`;

	# Patch the guava package

#1	GWInstaller::AL::GWLogger::log("\tPatching guava package.");
        my $description = "NeDi [$nedi_short_hostname]";
#1        $patchfile = "$guavapackagedir/$dirname/package.pkg";
#1        $command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
#1        `$command`;
#1        $command = "sed 's/shortname = .*/shortname = nedi_$nedi_short_hostname_as_var/g' -i $patchfile";
#1        `$command`;
#1        $command = "sed 's/classname = .*/classname = nediView$nedi_short_hostname_as_var/g' -i $patchfile";
#1        `$command`;
#1        $patchfile = "$guavapackagedir/$dirname/views/nedi.inc.php";
#1        $command = "sed 's/class nediView .*/class nediView$nedi_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
#1        `$command`;
#1	`chown -R nagios:nagios $guavapackagedir/$dirname`;


	# Configure the httpd server.

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

        open(NMS_HTTPD, ">$gw_nms_httpd_conf_dir/nms-httpd.conf") || GWInstaller::AL::GWLogger::log("Couldn't create nms httpd configuration file.");
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
        `chown -R nagios:nagios $gw_nms_httpd_conf_dir`;

#1        GWInstaller::AL::GWLogger::log("\tPatching guava.");
        $domain = @host_domain[1];
#1        $cmd = "sed 's/setrawcookie(\$cookie_name, \$ticket, null, \$path);/setrawcookie(\$cookie_name,\$ticket, null, \$path, \"$domain\");/g' -i  $gw_home/core/guava/htdocs/guava/includes/guava.inc.php";
#1        `$cmd`;

        # Finish Up.
        GWInstaller::AL::GWLogger::log("\tRestarting Groundwork Apache Server.");
        `/etc/init.d/httpd restart  >/dev/null 2>&1`;
        GWInstaller::AL::GWLogger::log("\tDone.");
}

#
#       Miscellany
#

sub find_in_file() {
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

sub is_functional{
	$self = shift;
	my $functionality = 0;
	
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
			
	$rpm_package_prefix = 'groundwork-nms-nedi';
	$gw_home = '/usr/local/groundwork';
	$nms_home = $gw_home . '/nms';
	$component_home = $nms_home . '/applications/nedi';

	#
	#	First, check to see if the package is installed.
	#

	`rpm -qa|grep $rpm_package_prefix`;
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

	if ( ! (-e "$component_home/nedi.pl" ))
	{
		$functionality = $STATUS_CORRUPT_INSTALLATION;
	}

	# Check to see if it is in the crontab.

	`crontab -u nagios -l | grep nedi`;
	$component_in_crontab = ($? == 0 ? 1 : 0);

	if ($component_in_crontab)
	{
		$functionality = $STATUS_GOOD;
	}
	else
	{
		$functionality = $STATUS_NOT_RUNNING;
	}
	
	$retval = 0;
	
	if($functionality !=0 ){
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
