#!/usr/bin/perl
##
##      deploy_nedi.pl
##      Copyright (C) 2008, Groundwork Open Source, Inc.
##
##      Deploy a local configuration based on the enterprise.properties file.
##
##      History
##              03/07/08        Created Daniel Emmanuel Feinsmith
##

use Getopt::Std;
use Socket;
use Sys::Hostname;

#
#       Globals
#

my %options=();
my $gw_home="/usr/local/groundwork";
my $enterprise_home="$gw_home/enterprise";
my @properties = ();
my $configuration_file;
my $deploy_profile;

#
#       Parse Command Line Options.
#

getopts("qf:i:p:", \%options);

#
#       Print Header.
#

if (!(defined $options{q}))
{
	print "=============================================================================\n";
	print "= deploy_nms_nedi.pl -- deploy local configuration                          =\n";
	print "= Copyright (C) 2008 Groundwork Open Source, Inc.                           =\n";
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

	deploy_nms_nedi($type, $subtype, $instance);
	leave_normal();
}

sub
deploy_nms_nedi()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        my $nms_home = "/usr/local/groundwork/nms";
        my $nedi_home = "$nms_home/applications/nedi";
        my $php_home = "$nms_home/tools/php";

        print "  Deploying NeDi Instance [$instance]\n";
        $nedi_host = find_property($type,$subtype,$instance, "host");
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
                leave_with_error("Unable to find complete set of properties to deploy NeDi.");
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
        # NeDi
        #

        if ((! -d $nedi_home))
        {
                leave_with_error("NeDi not installed at $nedi_home.");
        }

	#
	#	Patch Perl Modules
	#

	print "  Patching files.\n";
	my $patch = "$enterprise_home/bin/components/nedi";
	my $INSTALLDIR = $nedi_home;

	print "    Patching Perl files.\n";
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
	#	Patch PHP Modules.
	#

	print "    Patching PHP files.\n";
	`cp -f $patch/libmisc.php $INSTALLDIR/html/inc`;

	#
	#	Patch other.def
	#

	print "    Patching other.def.\n";
	`sed "s/Dispro.*/Dispro CDP/g" -i $nedi_home/sysobj/other.def`;

	#
	#	Prepare for database initialization.
	#

	print "  Copying database settings to nedi.conf.\n";
	`sed 's/dbuser.*/dbuser\t\t$database_user/g' $nedi_home/nedi.conf >/tmp/deploy_nedi1.conf`;
	`sed 's/dbname.*/dbname\t\t$database_name/g' /tmp/deploy_nedi1.conf >$nedi_home/nedi.conf`;
	`sed 's/dbhost.*/dbhost\t\t$db_host/g' $nedi_home/nedi.conf >/tmp/deploy_nedi1.conf`;
	`sed 's/dbpass.*/dbpass\t\t$database_password/g' /tmp/deploy_nedi1.conf >$nedi_home/nedi.conf`;
	`sed 's/rrdstep.*/rrdstep\t\t14400/g' -i $nedi_home/nedi.conf`;
	`sed 's/usr/#usr/g' -i $nedi_home/nedi.conf`;

	#
	#	Determine state of current database if there is one.
	#

	if ($deploy_profile eq 'local')
	{
		`mysqldump --user=$db_root_user --password=$db_root_password $database_name --no-data=true >/tmp/nedi_dump.tmp 2>/dev/null`;
	}
	else
	{
		`mysqldump --user=$db_root_user --password=$db_root_password --host=$db_host $database_name --no-data=true >/tmp/nedi_dump.tmp 2>/dev/null`;
	}

	$there_is_a_previous_nedi_database = find_in_file("/tmp/nedi_dump.tmp", "configs");
	$previous_database_is_up_to_date = find_in_file("/tmp/nedi_dump.tmp", "iftrack");
	print "  There Is a Previous NeDi Database: " . $there_is_a_previous_nedi_database . "\n";
	print "  The Database Is Current: " . $previous_database_is_up_to_date . "\n";

	#
	#	Now, normalize the database (create or migrate it)
	#

	print "  Creating backup directory.\n";
    `mkdir -p /usr/local/groundwork/nms/backups/nedi >/dev/null 2>/dev/null`;

	#
	#	If there is a previous database that is out of date,
	#	then back it up and migrate it.
	#

    if ($there_is_a_previous_nedi_database && !$previous_database_is_up_to_date)
    {
		#
		#	Back up previous database.
		#

        print "  Backing up database from previous installation of nedi.\n";
        if ($deploy_profile eq 'local')
        {
        	`mysqldump --skip-opt --no-create-db --no-create-info --user=$db_root_user --password=$db_root_password $database_name user configs devices monitoring links >/usr/local/groundwork/nms/backups/nedi/nedi.sql`;
        }
        else
        {
        	`mysqldump --skip-opt --no-create-db --no-create-info --user=$db_root_user --password=$db_root_password --host=$db_host $database_name user devices configs monitoring links >/usr/local/groundwork/nms/backups/nedi/nedi.sql`;
        }

		#
		#	As an addendum to the above mysqldump, we need to change all INSERT's to REPLACE,
		#	since not all versions of mysqldump allow for the --replace option.
		#

		`sed s/INSERT/REPLACE/g -i /usr/local/groundwork/nms/backups/nedi/nedi.sql`;

		#
		#	Now, drop old database, since we're going to
		#	have nedi create the new one.
		#

     	print "  Cleaning old database and user.\n";
       	open(SQL, ">/tmp/build_$instance.tmp") || leave_with_error("Couldn't create temporary build file.");
        printf SQL "drop database $database_name;\n";
        printf SQL "drop user $database_user;\n";
		printf SQL "flush privileges;\n";
       	close(SQL);

		if ($deploy_profile eq 'local')
		{
        		`mysql --user=$db_root_user --password=$db_root_password $database_name </tmp/build_$instance.tmp >/dev/null 2>&1`;
		}
		else
		{
        		`mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name </tmp/build_$instance.tmp >/dev/null 2>&1`;
		}
	}

	#
	#	If there is a previous database that is already up to date,
	#	then we leave that database alone. Otherwise, we
	#	create it anew.
	#

	if ($there_is_a_previous_nedi_database && $previous_database_is_up_to_date)
	{
        print "  Existing Database with up-to-date schema, skipping the initialization.\n";
	}
	else
	{
        # X. Initialize Database.
        print "  Initializing NeDi Database.\n";

		`echo "$db_root_user" >/tmp/build_nedi.tmp`;
		`echo "$db_root_password" >>/tmp/build_nedi.tmp`;
		`echo "$nedi_host" >>/tmp/build_nedi.tmp`;	
		`mysqladmin flush-hosts >/dev/null 2>&1`;
		`$nedi_home/nedi.pl -i </tmp/build_nedi.tmp`;

		#
		#	Okay, we've created it. Do we need
		#	to migrate a previous database?
		#

		if ($there_is_a_previous_nedi_database)
		{
			print "  Migrating previous database to new schema.\n";	
			print "    Copying data from previous nedi database to new database.\n";

			if ($deploy_profile eq 'local')
        	{
				`cat /usr/local/groundwork/nms/backups/nedi/nedi.sql | mysql --user=$db_root_user --password=$db_root_password $database_name`;
			}
			else
        	{
				`cat /usr/local/groundwork/nms/backups/nedi/nedi.sql | mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name`;
			}

			#
			#	Now, update the database.
			#

			print "    Updating users table with new themes and language preferences.\n";
			print "    Updating devices table with new columns.\n";
    		open(SQL, ">/tmp/build_$instance.tmp") || leave_with_error("Couldn't create temporary build file.");
			printf SQL "UPDATE user SET language='english' WHERE language='eng';\n";
			printf SQL "UPDATE user SET theme='default';\n";
			printf SQL "UPDATE devices SET cpu='',memcpu='',memio='',temp='';\n";
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
			#	Build new user privileges to the database to make sure
			#	NeDi has the rights to access it.
			#

    		print "    Building new user privileges.\n";
    		open(SQL, ">/tmp/build_$instance.tmp") || leave_with_error("Couldn't create temporary build file.");
    		printf SQL "grant all on $database_name.* to '$database_user' identified by '$database_password';\n";
    		printf SQL "flush privileges;\n";
    		close(SQL);
    		if ($deploy_profile eq 'local')
    		{   
            	`mysql --user=$db_root_user --password=$db_root_password $database_name < /tmp/build_$instance.tmp`;
    		}   
    		else
    		{   
            	`mysql --user=$db_root_user --password=$db_root_password --host=$db_host $database_name < /tmp/build_$instance.tmp`;
    		}
		}
	}

	#
	#	If there are existing nedi files in the expected location for 5.1.3 NeDi,
	#	then copy them to the backups directory.
	#

	if ( -d "/usr/local/groundwork/nedi" )
	{
		print "  Copying previous version of nedi to nms/backups directory\n";
		`cp -rf /usr/local/groundwork/nedi /usr/local/groundwork/nms/backups`;
	}

	#
	#	Copy old seedlist and rrds.
	#

	if ( -e "/usr/local/groundwork/nedi/seedlist" )
	{
		print "  Copy seedlist and rrds from previous installation.\n";
		`cp -f /usr/local/groundwork/nedi/seedlist /usr/local/groundwork/nms/applications/nedi >/dev/null`;
		`cp -rf /usr/local/groundwork/nedi/rrd/* /usr/local/groundwork/nms/applications/nedi/rrd >/dev/null`;
	}

	#
	#	Create extended permissions for network users.
	#

	print "  Building new user privileges.\n";
   	open(SQL, ">/tmp/build_$instance.tmp") || leave_with_error("Couldn't create temporary build file.");
    printf SQL "grant all on $database_name.* to '$database_user' identified by '$database_password';\n";
    printf SQL "flush privileges;\n";
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
	#	Set Permissions.
	#

    print "  Setting filesystem permissions.\n";
    `chown -R nagios:nagios $nedi_home`;

	#
	#	Create crontab.
	#

    print "  Adding entry to crontab.\n";

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
	#	Finish Up.
	#

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
        $configuration_file=shift;

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

sub leave_normal()
{
    if (!(defined $options{q}))
    {   
        print "=============================================================================\n";
    }   
    exit;
}

sub leave_with_error()
{
        my ($error_string) = @_;
        print "  ERROR: $error_string\n";
        leave_normal();
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

