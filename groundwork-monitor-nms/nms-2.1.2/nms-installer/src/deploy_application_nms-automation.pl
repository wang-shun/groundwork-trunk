#!/usr/bin/perl
##
##      deploy_application_nms-automation.pl
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
	print "= deploy_application_nms-automation.pl -- deploy local configuration       =\n";
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
        leave_with_error("Incorrect profile type indicated. Must be either local or distributed");
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

	deploy_application_nms_automation($type, $subtype, $instance);
	leave_normal();
}

#
#	Deploy cacti package.
#

sub
deploy_application_nms_automation()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        print "  Deploying NMS Automation Instance [$instance]\n";
	$package_host = find_property($type, $subtype, $instance, "host");
	$cacti_db_host = find_property($type,$subtype,$instance, "cacti:database:host");
	$cacti_db_port = find_property($type,$subtype,$instance, "cacti:database:port");
	$cacti_db_name = find_property($type,$subtype,$instance, "cacti:database_name");
	$cacti_db_user = find_property($type,$subtype,$instance, "cacti:database_user");
	$cacti_db_password = find_property($type,$subtype,$instance, "cacti:database_password");

	$nedi_db_host = find_property($type,$subtype,$instance, "nedi:database:host");
	$nedi_db_port = find_property($type,$subtype,$instance, "nedi:database:port");
	$nedi_db_name = find_property($type,$subtype,$instance, "nedi:database_name");
	$nedi_db_user = find_property($type,$subtype,$instance, "nedi:database_user");
	$nedi_db_password = find_property($type,$subtype,$instance, "nedi:database_password");

        $db_root_user = find_property($type,$subtype,$instance, "cacti:database:root_user");
        $db_root_password = find_property($type,$subtype,$instance, "cacti:database:root_password");

        if ($package_host == -1
        || $cacti_db_host == -1 || $cacti_db_port == -1 || $cacti_db_name == -1 || $cacti_db_user == -1 || $cacti_db_password == -1
        || $nedi_db_host == -1 || $nedi_db_port == -1 || $nedi_db_name == -1 || $nedi_db_user == -1 || $nedi_db_password == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy NMS Automation Package, Aborting.");
        }

	print "  {\n";
	print "    cacti_db_host = $cacti_db_host\n";
	print "    cacti_db_port = $cacti_db_port\n";
	print "    cacti_db_name = $cacti_db_name\n";
	print "    cacti_db_user = $cacti_db_user\n";
	print "    cacti_db_password = [hidden]\n";
	print "    nedi_db_host = $nedi_db_host\n";
	print "    nedi_db_port = $nedi_db_port\n";
	print "    nedi_db_name = $nedi_db_name\n";
	print "    nedi_db_user = $nedi_db_user\n";
	print "    nedi_db_password = [hidden]\n";
        print "    db_root_user = $db_root_user\n";
        print "    db_root_password = [hidden]\n";
	print "  }\n";

	$gw_home = "/usr/local/groundwork";
	$nms_automation_scripts_dir = "$gw_home/nms/tools/automation/scripts";
	$gw_monarch_automation_templates_dir = "$gw_home/core/monarch/automation/templates";
	$gw_profiles_dir = "$gw_home/core/profiles";

	if (!(-d $gw_profiles_dir) && !(-d $gw_monarch_automation_templates_dir ))
	{
                leave_with_error("Expected Monarch Automation and Profiles Directories Missing, Aborting.");
	}

        print "  {\n";
        print "    host = $package_host\n";
        print "  }\n";

        #
        # NMS Automation
        #

	print "  Installing NMS Automation Components.\n";
	print "    Installing Scripts.\n";
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

	print "    Installing Schema Templates.\n";
	`cp -rf $gw_home/nms/tools/automation/templates/schema-* $gw_monarch_automation_templates_dir`;
	print "    Installing Service Profiles.\n";
	`cp -rf $gw_home/nms/tools/automation/templates/service_* $gw_profiles_dir`;
	`cp -rf $gw_home/nms/tools/automation/templates/host-profile* $gw_profiles_dir`;
	`chown -R nagios:nagios $gw_profiles_dir`;

	#
        # Updated import_schema database with defaults.
	#

        print "    Updating import_schema table with defaults.\n";
        `/usr/local/groundwork/mysql/bin/mysql --user=$db_root_user --password=$db_root_password monarch < $gw_home/nms/tools/automation/templates/import_schema.sql`;

        # Finish Up.
        print "  Done.\n";
}

#
#       Miscellany
#

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

