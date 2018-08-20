#!/usr/local/groundwork/nms/tools/perl/bin/perl
##
##      deploy_cacti.pl
##      Copyright (C) 2008, Groundwork Open Source, Inc.
##
##      Deploy a local configuration based on the enterprise.properties file.
##
##      History
##          03/07/08    Created Daniel Emmanuel Feinsmith
##

use Getopt::Std;
use Socket;
use Sys::Hostname;
use DBI;
use DBD::mysql;

#
#       Globals
#

my %options=();
my $gw_home="/usr/local/groundwork";
my $nms_home="$gw_home/nms";
my $enterprise_home="$gw_home/enterprise";
my @properties = ();
my $configuration_file;
my $deploy_profile;

#
#       Parse Command Line Options.
#

getopts("qf:i:p:", \%options);

if (defined $options{q})
{
	$quiet = true;
}

#
#       Print Header.
#

if (!(defined $options{q}))
{
	print "=============================================================================\n";
	print "= deploy_nms_cacti.pl -- deploy local configuration             =\n";
	print "= Copyright (C) 2008 Groundwork Open Source, Inc.               =\n";
	print "=============================================================================\n";
}

#
#       Process Options.
#

if (!defined $options{f})
{
    $configuration_file = "$enterprise_home/config/enterprise.properties";
}
else
{
    $configuration_file = $options{f};
}

if (!defined $options{p})
{
      $deploy_profile = 'local';
}
else
{
    $deploy_profile = $options{p};
    $deploy_profile =~ tr/A-Z/a-z/;

    if ($deploy_profile ne 'local' && $deploy_profile ne 'distributed')
    {
        leave_normal();
    }
}

configuration_load($configuration_file);
if (!defined $options{i})
{
	leave_with_error("Instance Not Provided.");
}
else
{
	($type,$subtype,$instance) = split(/\./, $options{i}, 3);

	deploy_nms_cacti($type, $subtype, $instance);
	leave_normal();
}

sub
deploy_nms_cacti()
{
    $type = shift;
    $subtype = shift;
    $instance = shift;

    my $nms_home = "/usr/local/groundwork/nms";
    my $cacti_home = "$nms_home/applications/cacti";
    my $cacti_spine_home = "$nms_home/applications/cacti-spine";
    my $php_home = "$nms_home/tools/php";
    my $patch = "$enterprise_home/bin/components/cacti";

    print "  Deploying Cacti Instance [$instance]\n";
    $db_type = find_property($type,$subtype,$instance, "database:type");
    $db_port = find_property($type,$subtype,$instance, "database:port");
    $db_root_user = find_property($type,$subtype,$instance, "database:root_user");
    $db_root_password = find_property($type,$subtype,$instance, "database:root_password");
    $httpd_port = find_property($type,$subtype,$instance, "httpd:port");
    $database_name = find_property($type,$subtype,$instance, "database_name");
    $database_user = find_property($type,$subtype,$instance, "database_user");
    $database_password = find_property($type,$subtype,$instance, "database_password");

	if ($deploy_profile eq 'local')
	{
		$db_host = shortname();
		$httpd_host = shortname();

	}
	else
	{
    	$db_host = find_property($type,$subtype,$instance, "database:host");
    	$httpd_host = find_property($type,$subtype,$instance, "httpd:host");
	}

    if ($db_host == -1 || $db_type == -1 || $db_port == -1 || $db_root_user == -1 || $db_root_pass == -1
    || $httpd_host == -1 || $httpd_port == -1 || $database_name == -1 || $database_user == -1 || $database_password == -1)
    {
        leave_with_error("Unable to find complete set of properties to deploy Cacti.");
    }
    print "  {\n";

    print "    db_host = $db_host\n";
    print "    db_type = $db_type\n";
    print "    db_port = $db_port\n";
    print "    db_root_user = $db_root_user\n";
    print "    db_root_password = [hidden]\n";
    print "    database_name = $database_name\n";
    print "    database_user = $database_user\n";
    print "    database_password = [hidden]\n";
    print "    httpd_host = $httpd_host\n";
    print "    httpd_port = $httpd_port\n";
    print "  }\n";

    #
    # Cacti
    #

    if ((! -d $cacti_home) || (! -d $cacti_spine_home))
    {
        leave_with_error("Cacti not installed at $cacti_home and $cacti_spine_home.");
    }

	#
	#	First, find out if there already is a cacti database.
	#

	if ($deploy_profile eq 'local')
	{
		`mysqldump --user=$db_root_user --password=$db_root_password cacti --no-data=true >/tmp/cacti_dump.tmp 2>/dev/null`;
	}
	else
	{
		`mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host cacti --no-data=true >/tmp/cacti_dump.tmp 2>/dev/null`;
	}

	$there_is_a_previous_cacti_database = find_in_file("/tmp/cacti_dump.tmp", "poller");
	$previous_database_is_up_to_date = find_in_file("/tmp/cacti_dump.tmp", "plugin_config");

	print "  There Is a Previous Cacti Database: " . $there_is_a_previous_cacti_database . "\n";
	print "  The Database Is Current: " . $previous_database_is_up_to_date . "\n";

	#
	#	Now proceed with figuring out what needs to be done:
	#	(1) Previous Database in earlier schema.
	#	(2) Previous database with up to date schema.
	#	(3) No Previous Database.
	#

	if (!$there_is_a_previous_cacti_database)
	{
		print "  No previous Cacti database found, Building new database.\n";
		if ($deploy_profile eq 'local')
		{
    		`mysqladmin --user=$db_root_user --password=$db_root_password create $database_name`;
    		`mysql --user=$db_root_user --password=$db_root_password $database_name < $cacti_home/cacti.sql`;
			`mysql --user=$db_root_user --password=$db_root_password $database_name < $patch/cacti.sql`;
		}
		else
		{
    		`mysqladmin --user=$db_root_user --password=$db_root_password --host=$db_host create $database_name`;
    		`mysql --user=$db_root_user --password=$db_root_password $database_name < $cacti_home/cacti.sql`;
			`mysql --user=$db_root_user --password=$db_root_password $database_name < $patch/cacti.sql`;
		}
	}
	else
	{
		if (!$previous_database_is_up_to_date)
		{
			my $dsn;

			print "  Migrating previous Cacti database to new schema.\n";
        	print "    Backing up database from previous version of cacti.\n";
        	`mkdir -p /usr/local/groundwork/nms/backups/cacti >/dev/null 2>/dev/null`;

    		if ( -d "/usr/local/groundwork/cacti" )
    		{                           
        		print "    Copying previous version of cacti to nms/backups directory\n"; 
				`cp -rf /usr/local/groundwork/cacti /usr/local/groundwork/nms/backups >/dev/null`;
    		}     

			if ($deploy_profile eq 'local')
			{
				$dsn = "dbi:mysql:$database_name:localhost:$db_port";
        		`mysqldump --user=$db_root_user --password=$db_root_password $database_name >/usr/local/groundwork/nms/backups/cacti/cacti.sql`;
			}
			else
			{
				$dsn = "dbi:mysql:$database_name:$db_host:$db_port";
        		`mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host $database_name >/usr/local/groundwork/nms/backups/cacti/cacti.sql`;
			}

			`cp $patch/upgrade_database.php $cacti_home/install`;
			print "    Migrating database from earlier version of Cacti.\n";
			`pushd $cacti_home/install`;
			`$php_home/php upgrade_database.php`;
			`popd`;

			print "    Correcting rrd path location in database.\n";
			my $user = $db_root_user;
			my $pass = $db_root_password;

			my $dbh = DBI->connect($dsn, $user, $pass) or die "Can't connect to the DB: $DBI::errstr\n";
			my $sth = $dbh->prepare('select rrd_path,arg1 from poller_item');
			$sth->execute;

			while (@row = $sth->fetchrow_array())
			{
				#
				#	Update the RRD Paths.
				#

    			$old_rrd_path = $row[0];
    			$new_rrd_path = $row[0];
    			$new_rrd_path =~ s|groundwork/cacti/rra|groundwork/nms/applications/cacti/rra|g;

    			$usth = $dbh->prepare("update poller_item set rrd_path='" . $new_rrd_path . "' where rrd_path='" . $old_rrd_path . "'");
    			$usth->execute;
				print "      Updated rrd path to: $new_rrd_path\n";

				#
				#	Now update the scripts location.
				#

				$old_arg1 = $row[1];
				$new_arg1 = $row[1];
				$new_arg1 =~ s|groundwork/cacti|groundwork/nms/applications/cacti|g;

    			$usth = $dbh->prepare("update poller_item set arg1='" . $new_arg1 . "' where arg1='" . $old_arg1 . "'");
    			$usth->execute;
				print "      Updated scripts location to: $new_arg1\n";
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
			#	Now, Update the alert base url in the settings table to correctly point to our instance.
			#

			$usth = $dbh->prepare("update settings set value='" . $alert_base_url . "' where name='alert_base_url'");
			$usth->execute();
			print "      Updated alert base url to: $alert_base_url\n";
		}
	}

	#
	#	Okay, now the database itself should exist in the new schema
	#	format, whether it was created anew, or whether it
	#	was migrated from a previous version.
	#

	print "  Creating database user permissions and settings.\n";
	open(SQL, ">/tmp/build_$instance.tmp") || leave_with_error("Couldn't create temporary build file.");
	printf SQL "grant all on $database_name.* to $database_user identified by '$database_password';\n";
	printf SQL "flush privileges;\n";
	printf SQL "lock tables settings write;\n";
	printf SQL "UPDATE settings SET value='$nms_home/applications/cacti-spine/bin/spine' WHERE name='path_spine';\n";
	printf SQL "UPDATE settings SET value='2' WHERE name='poller_type';\n";
	printf SQL "unlock tables;\n";
	printf SQL "lock tables version write;\n";
	printf SQL "UPDATE version SET cacti='0.8.7b';\n";
	printf SQL "unlock tables;\n";
	close(SQL);

	if ($deploy_profile eq 'local')
	{
       		`mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp`;
	}
	else
	{
       		`mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp`;
	}

	#
	#	Patch the plugin database if needed.
	#

	print "  Patching plugin database.\n";
	if ($deploy_profile eq 'local')
	{
		`mysql --user=$database_user --password=$database_password $database_name < $patch/pa.sql`;
	}
	else
	{
		`mysql --user=$database_user --password=$database_password --host=$db_host $database_name < $patch/pa.sql`;
	}

	#
	#	Move existing rrd's
	#

	print "  Checking for existing rrd's.\n";

	if ( -d "/usr/local/groundwork/cacti/rra" )
	{
		print "    Copying rrd's from previous deployment to new location.\n";
		`cp -r /usr/local/groundwork/cacti/rra/* /usr/local/groundwork/nms/applications/cacti/rra`;
	}

	#
	# Patch Files
	#

    print "  Patching files.\n";
    print "    Patching PHP files.\n";
#	`cp -f $patch/0_8_6j_to_0_8_7.php /usr/local/groundwork/nms/applications/cacti/install`;
    `cp -f $patch/index.php /usr/local/groundwork/nms/applications/cacti/install`;
    `cp -f $patch/add_tree.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_graphs.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_device.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/add_perms.php /usr/local/groundwork/nms/applications/cacti/cli`;
    `cp -f $patch/ss_fping.php /usr/local/groundwork/nms/applications/cacti/scripts`;

    print "    Patching Perl files.\n";
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

    print "    Patching configuration files.\n";
    $cmd = "sed \'s/database_hostname.*/database_hostname = \"$db_host\";/g\' -i $cacti_home/include/config.php";
    `$cmd`;

    print "    Importing Cacti Data Query Template.\n";
    $cmd = "$php_home/bin/php $enterprise_home/bin/components/cacti/cacti_data_query_add.php >/dev/null 2>&1";
    `$cmd`;

    # X. Set Permissions.
    print "  Setting permissions.\n";
    `chown -R nagios:nagios $cacti_home`;
    `chown -R nagios:nagios $cacti_spine_home`;
    `chown root:root $cacti_home/cli/cacti_ping_executor`;
    `chmod u+s $cacti_home/cli/cacti_ping_executor`;

    # X. Create crontab.
    print "  Adding entry to crontab.\n";

    my $file1="/tmp/build1_$instance.tmp";
    my $file2="/tmp/build2_$instance.tmp";
    my $cronentry="*/5 * * * * ($php_home/bin/php $cacti_home/poller.php ; $nms_home/tools/automation/scripts/extract_cacti.pl ) > /dev/null 2>&1";

    `crontab -u nagios -l >$file1 2>/dev/null`;
    `cat $file1 | grep -v "poller.php" > $file2`;
    `echo "$cronentry" >>$file2`;
    `crontab -u nagios $file2`;

    # Finish Up.
    print "  Done.\n";
}

#
#       Miscellany
#

sub find_in_file()
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

sub find_property()
{
    $type = shift;
    $subtype = shift;
    $instance = shift;
    $property = shift;

    $num_properties = @properties;
    for ($pnum=0; $pnum < $num_properties; $pnum++)
    {
        $t = $properties[$pnum]->{'type'};
        $s = $properties[$pnum]->{'subtype'};
        $i = $properties[$pnum]->{'instance'};
        $p = $properties[$pnum]->{'property'};
        $v = $properties[$pnum]->{'value'};
        if ($t eq $type && $s eq $subtype && $i eq $instance && $p eq $property)
        {
            return($v);
        }
    }
    return(-1);
}

sub trim($)
{
        my $string = shift;

        chomp($string);
        $string =~ s/^\s+//;
        $string =~ s/\s+$//;
        return $string;
}

sub configuration_load()
{
    $configuration_filename=shift;

    open(CONFIG,$configuration_file) || leave_with_error("cannot open configuration file.");
    my $line;
    my $lnum = 1;
    #my @properties = ();
    my $pnum = 0;
    while ($line = <CONFIG>)
    {
        $line = trim($line);
        $len=length($line);
        if ($len != 0)
        {
            if (!(substr($line,0,1) eq "#"))
            {
                #
                #       First, remove the comments and trailing
                #       spaces from the end of the line.
                #

                my $comment = index($line, "#");
                if ($comment != -1)
                {
                    $line = substr($line,0,$comment);
                }
                $line =~ s/ *$//;

                #
                #       Parse name/value pair
                #       And strip out any spaces from the
                #       beginning and end of the line.
                #

                $count = ($line =~ tr/=//);
                if ($count == 1)
                {
                    @pair = split(/=/,$line);
                    $name = $pair[0]; $value = $pair[1];
                    $name =~ s/^\s+//; $name =~ s/\s+$//;
                    $value =~ s/^\s+//; $value =~ s/\s+$//;

		    if (index($name, '.') == -1) { next; }

                    #
                    #       Substitute Macros in VALUE.
                    #

                    if ($value eq '%LOCAL_HOSTNAME')
                    {
                        $value = fqdn();
                    }

                    #
                    #       Now, split the name apart
                    #       into type, subtype,instance,property
                    #

                    $count = ($name =~ tr/\.//);
                    if ($count == 3)
                    {
                        ($type,$subtype,$instance,$property) = split(/\./, $name, 4);

                        # Okay, what's the next step? Well, it is to
                        # Take apart variables.

                        if (substr($value,0,1) eq "\$")
                        {
                            #
                            #       Okay, we have a reference. First, take
                            #       apart the value, and then look it up.
                            #

                            ($t,$s,$i) = split(/\./,substr($value,1,length($value)-1),3);
                            $num_properties = @properties;
                            for ($j=0; $j < $num_properties; $j++)
                            {
                                if ($properties[$j]->{'type'} eq $t
                                && $properties[$j]->{'subtype'} eq $s
                                && $properties[$j]->{'instance'} eq $i)
                                {
                                    $property_name = $property . ":" . $properties[$j]->{'property'};
                                    $properties[$pnum] =
                                    {
                                        'type' => $type,
                                        'subtype' => $subtype,
                                        'instance' => $instance,
                                        'property' => $property_name, #$properties[$j]->{'property'},
                                        'value' => $properties[$j]->{'value'}
                                    };
                                    $pnum++;
                                }

                            }
                        }
                        else
                        {
                            $properties[$pnum] =
                            {
                                'type' => $type,
                                'subtype' => $subtype,
                                'instance' => $instance,
                                'property' => $property,
                                'value' => $value
                            };
                            $pnum++;
                        }
                    }
                    else
                    {
                        leave_with_error("Line $lnum: Expecting type.subtype.instance.property=value.");
                    }
                }
                else
                {
                    leave_with_error("Line $lnum: Expecting name=value pair.");
                }
            }
        }
        $lnum++;
    }

}

sub leave_with_error()
{
    $error_string = shift;
    print "  ERROR: $error_string\n";
    leave_normal();
}

sub leave_normal()
{
	if (!(defined $options{q}))
	{
    	print "=============================================================================\n";
	}
    exit;
}

sub shortname()
{
    my $host_name = hostname();
    my $domain_present = index($host_name, ".");

    if ($domain_present != -1)
    {
		$host_name = substr($host_name, 0, $domain_present);
    }
	return($host_name);
}

sub fqdn()
{
    my $fqdn;
    my $host_name = hostname();
    my $domain_present = index($host_name, ".");

    if ($deploy_profile eq 'local')
    {
        if ($domain_present != -1)
        {
            $fqdn = substr($host_name, 0, $domain_present);
        }
        else
        {
            $fqdn = $host_name;
        }
    }
    else
    {
        if ($domain_present == -1)
        {
            $domain_name = `nisdomainname`;
            if ($domain_name == "" || $domain_name == "(none)")
            {
                $domain_name = `domainname`;
                if ($domain_name == "(none)")
                {
                    $domain_name = "";
                }
            }
            chomp($domain_name);
            if ($domain_name == "")
            {
                $fqdn = $host_name;
            }
            else
            {
                $fqdn = "$host_name.$domain_name";
            }
        }
        else
        {
            $fqdn = $host_name;
        }
    }

    return $fqdn;
}

