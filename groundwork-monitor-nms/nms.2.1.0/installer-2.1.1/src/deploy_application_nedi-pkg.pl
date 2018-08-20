#!/usr/bin/perl
##
##      deploy_application_nedi-pkg.pl
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
	print "= deploy_application_nedi-pkg.pl -- deploy local configuration                         =\n";
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

	 deploy_application_nedi_pkg($type, $subtype, $instance);
	leave_normal();
}

#
#	Deploy nedi package.
#

sub
deploy_application_nedi_pkg()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        print "  Deploying Nedi Instance [$instance]\n";
        $package_host = find_property($type, $subtype, $instance, "host");
	$nedi_host = find_property($type, $subtype, $instance, "nedi:httpd:host");
	$nedi_port = find_property($type, $subtype, $instance, "nedi:httpd:port");

        if ($package_host == -1 || $nedi_host == -1 || $nedi_port == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy Nedi Package.");
        }
        print "  {\n";

        print "    nedi:httpd:host = $nedi_host\n";
        print "    nedi:httpd:port = $nedi_port\n";
        print "  }\n";

	$gw_httpd_conf_dir = "/usr/local/groundwork/apache2/conf";
        if (!(-d $gw_httpd_conf_dir))
        {
                leave_with_error("Directory '$gw_httpd_conf_dir' not found, Aborting.");
        }

        #
        # Nedi
        #

        my $sourcedir = "$enterprise_home/bin/components/guava-packages/nedi";
my $guavapackagedir="$gw_home/core/guava/htdocs/guava/packages";
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
	my $patchfile="$guavapackagedir/$dirname/views/nedi.inc.php";

        if (! -d $guavapackagedir)
        {
                leave_with_error("Can only install a Nedi package if Groundwork is installed.");
        }

	# Delete Old Version
	print "  Cleaning previous Nedi Package.\n";
	`rm -rf $guavapackagedir/$dirname`;

	# Installing New version.
	print "  Installing new Nedi Package.\n";
	`mkdir $guavapackagedir/$dirname`;
	`cp -rf $sourcedir/* $guavapackagedir/$dirname`;

	# Patch with correct location of server.
	print "  Configuring for server location.\n";
	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $nedi_host . ':' . $nedi_port . '";/g' . "'" . " $patchfile >$patchfile.new";
	`$command`;
	`rm -f $patchfile`;
	`mv $patchfile.new $patchfile`;

	# Patch the guava package

	print "  Patching guava package.\n";
        my $description = "NeDi [$nedi_short_hostname]";
        $patchfile = "$guavapackagedir/$dirname/package.pkg";
        $command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
        `$command`;
        $command = "sed 's/shortname = .*/shortname = nedi_$nedi_short_hostname_as_var/g' -i $patchfile";
        `$command`;
        $command = "sed 's/classname = .*/classname = nediView$nedi_short_hostname_as_var/g' -i $patchfile";
        `$command`;
        $patchfile = "$guavapackagedir/$dirname/views/nedi.inc.php";
        $command = "sed 's/class nediView .*/class nediView$nedi_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
        `$command`;
	`chown -R nagios:nagios $guavapackagedir/$dirname`;


	# Configure the httpd server.

        print "  Configuring httpd server.\n";
        print "    Creating directory structure if needed.\n";
        $gw_nms_httpd_conf_dir = "$gw_httpd_conf_dir/nms";
        if (!(-d $gw_nms_httpd_conf_dir))
        {
                mkdir($gw_nms_httpd_conf_dir);
        }

        print "    Modifying groundwork apache configuration file.\n";
        $cmd = "sed 's/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/nms.*//g' -i $gw_httpd_conf_dir/httpd.conf";
        `$cmd`;
        $cmd = "sed 's/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork.*/Include \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/groundwork\\/\*\.conf\\nInclude \\/usr\\/local\\/groundwork\\/apache2\\/conf\\/nms\\/\*\.conf/g' -i $gw_httpd_conf_dir/httpd.conf";
        `$cmd`;

        print "    Creating httpd configuration file.\n";
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

        open(NMS_HTTPD, ">$gw_nms_httpd_conf_dir/nms-httpd.conf") || leave_with_error("Couldn't create nms httpd configuration file.");
        printf NMS_HTTPD "##\n##\tnms-httpd.conf\n##\n##\tAuto-Generated by Enterprise Deployment Subsystem.\n##\t(C) 2008 Groundwork Open Source.\n##\tDo Not Manually Edit!\n##\n\n";
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
        if ($deploy_profile eq 'local')        {       
                    printf NMS_HTTPD "rewriteRule (.*) http://$package_short_hostname/\$1 [R=301,L]\n";
        }       
        else    
        {       
                    printf NMS_HTTPD "rewriteRule (.*) http://$package_host/\$1 [R=301,L]\n";        }  
        }

	if ($deploy_profile eq 'distributed')
	{
        	printf NMS_HTTPD "rewriteCond %{HTTP_HOST} ^" . @host_domain[0] . "\$ [NC]\n";
        	printf NMS_HTTPD "rewriteRule (.*) http://$package_host/\$1 [R=301,L]\n";
	}
        close(NMS_HTTPD);

        print "    Adjusting permissions.\n";
        `chown -R nagios:nagios $gw_nms_httpd_conf_dir`;

        print "    Patching guava.\n";
        $domain = @host_domain[1];
$cmd = "sed 's/setrawcookie(\$cookie_name, \$ticket, null, \$path);/setrawcookie(\$cookie_name,\$ticket, null, \$path, \"$domain\");/g' -i $gw_home/core/guava/htdocs/guava/includes/guava.inc.php";
        `$cmd`;

        # Finish Up.
        print "    Restarting Groundwork Apache Server.\n";
        `/etc/init.d/httpd restart`;
        print "    Done.\n";

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

