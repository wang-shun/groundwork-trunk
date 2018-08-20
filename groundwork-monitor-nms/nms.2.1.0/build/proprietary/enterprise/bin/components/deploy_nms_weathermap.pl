#!/usr/bin/perl
##
##      deploy_application_weathermap.pl
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

getopts("qp:i:f:", \%options);

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
	print "= deploy_application_weathermap.pl -- deploy local configuration                         =\n";
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

	deploy_application_weathermap($type, $subtype, $instance);
	leave_normal();
}

#
#	Deploy weathermap package.
#

sub
deploy_application_weathermap()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        print "  Deploying Weathermap Instance [$instance]\n";

        $package_host = find_property($type, $subtype, $instance, "host");
        if ($package_host == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy Weathermap.");
        }

	$package_host_shortname = package_shorten($package_host);
	print "  {\n";
        print "    host = $package_host ($package_host_shortname)\n";
        print "  }\n";

	$nms_home = "$gw_home/nms";
	$cactidir = "$nms_home/applications/cacti";
	$weathermapdir = "$nms_home/applications/weathermap";
	$configfile = "$cactidir/include/config.php";

	print "  Attaching to Cacti Instance.\n";
        `rm -rf $cactidir/plugins/weathermap`;
        `cp -rf $weathermapdir $cactidir/plugins`;

	print "  Building Configuration File.\n";
	`grep -v "?>" $configfile > /tmp/build_weathermap.tmp`;
	$str = "\$plugins" . "[]" . " = 'weathermap';";
	$cmd = "echo " . '"\\' . $str . '"' . " >>/tmp/build_weathermap.tmp";
	`$cmd`;
	`echo "?>" >>/tmp/build_weathermap.tmp`;
	`cp -f /tmp/build_weathermap.tmp $configfile`;

	# Upgrade if needed.
	my $nms_513_weathermap_dir = "/usr/local/groundwork/weathermap";
	if ( -d $nms_513_weathermap_dir )
	{
		print "  Upgrading previous weathermap configuration.\n";
		print "    Copying images\n";
		`cp -f $nms_513_weathermap_dir/images/* $cactidir/plugins/weathermap/images >/dev/null 2>&1`;
		`cp -f $nms_513_weathermap_dir/*.jpg $cactidir/plugins/weathermap >/dev/null 2>&1`;
		`cp -f $nms_513_weathermap_dir/*.png $cactidir/plugins/weathermap >/dev/null 2>&1`;
		
		print "    Copying maps.\n";
		`cp -rf $nms_513_weathermap_dir/configs/* $cactidir/plugins/weathermap/configs >/dev/null 2>&1`;

		print "    Fixing URLs in configuration file(s).\n";
		if ($deploy_profile eq 'local')
		{
			`sed 's|INFOURL http://[^ ]*/cacti|INFOURL http://$package_host_shortname:81/cacti|g' -i $cactidir/plugins/weathermap/configs/*`;
		}
		else
		{
			`sed 's|INFOURL http://[^ ]*/cacti|INFOURL http://$package_host:81/cacti|g' -i $cactidir/plugins/weathermap/configs/*`;
		}



                `mkdir /usr/local/groundwork/nms/backups >/dev/null 2>/dev/null`;
                `rm -rf /usr/local/groundwork/nms/backups/weathermap >/dev/null 2>/dev/null`;
                if ( -d "/usr/local/groundwork/weathermap" )
                {
                        print "  Moving previous version of weathermap to nms/backups directory\n";
                        `mv /usr/local/groundwork/weathermap /usr/local/groundwork/nms/backups`;
                }
	}

	print "  Updating file permissions.\n";
        `chown -R nagios:nagios $cactidir/plugins/weathermap`;

        # X. Finish Up.
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

