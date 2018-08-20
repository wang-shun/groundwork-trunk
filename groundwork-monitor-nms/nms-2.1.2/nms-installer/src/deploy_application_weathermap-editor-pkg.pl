#!/usr/bin/perl
##
##      deploy_application_weathermap-editor-pkg.pl
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
	print "= deploy_application_weathermap-editor-pkg.pl -- deploy local configuration =\n";
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

	deploy_application_weathermap_editor_pkg($type, $subtype, $instance);
	leave_normal();
}

#
#	Deploy weathermap-editor package.
#

sub
deploy_application_weathermap_editor_pkg()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        print "  Deploying Weathermap Editor Instance [$instance]\n";
        $package_host = find_property($type, $subtype, $instance, "host");
	$weathermap_editor_host = find_property($type, $subtype, $instance, "weathermap-editor:httpd:host");
	$weathermap_editor_port = find_property($type, $subtype, $instance, "weathermap-editor:httpd:port");

        if ($package_host == -1 || $weathermap_editor_host == -1 || $weathermap_editor_port == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy Weathermap Editor Package.");
        }
        print "  {\n";

        print "    weathermap-editor:httpd:host = $weathermap_editor_host\n";
        print "    weathermap-editor:httpd:port = $weathermap_editor_port\n";
        print "  }\n";

        $gw_httpd_conf_dir = "/usr/local/groundwork/apache2/conf";
        if (!(-d $gw_httpd_conf_dir))
        {
                leave_with_error("Directory '$gw_httpd_conf_dir' not found, Aborting.");
        }

        #
        # Weathermap Editor
        #

#1        my $sourcedir = "$enterprise_home/bin/components/guava-packages/weathermap-editor";
#1my $guavapackagedir="$gw_home/core/guava/htdocs/guava/packages";
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


        my $dirname="weathermap-editor_$weathermap_editor_short_hostname";
#1	my $patchfile="$guavapackagedir/$dirname/views/weathermapeditor.inc.php";

#1        if (! -d $guavapackagedir)
#1        {
#1                leave_with_error("Can only install a Weathermap Editor package if Groundwork is installed.");
#1        }

	# 1. Delete Old Version
	print "  Cleaning previous Weathermap Editor Package.\n";
#1	`rm -rf $guavapackagedir/$dirname`;

	# 2. Installing New version.
	print "  Installing new Weathermap Editor Package.\n";
#1	`mkdir $guavapackagedir/$dirname`;
#1	`cp -rf $sourcedir/* $guavapackagedir/$dirname`;

	# 3. Patch with correct location of server.
	print "  Configuring for server location.\n";
#1	$command = 'sed ' . "'" . 's/\\$this->baseURL =.*/\\$this->baseURL = "http:\\/\\/' . $weathermap_editor_host . ':' . $weathermap_editor_port . '";/g' . "'" . " $patchfile >$patchfile.new";
#1	`$command`;
#1	`rm -f $patchfile`;
#1	`mv $patchfile.new $patchfile`;

        # Patch the guava package

#1        print "  Patching guava package.\n";
#1        my $description = "Weathermap Editor [$weathermap_editor_short_hostname]";
#1        $patchfile = "$guavapackagedir/$dirname/package.pkg";
#1        $command = "sed 's/\tname = .*/\tname = $description/g' -i $patchfile";
#1        `$command`;
#1        $command = "sed 's/shortname = .*/shortname = weathermap_editor_$weathermap_editor_short_hostname_as_var/g' -i $patchfile";
#1        `$command`;
#1        $command = "sed 's/classname = .*/classname = weathermapeditorView$weathermap_editor_short_hostname_as_var/g' -i $patchfile";
#1        `$command`;
#1        $patchfile = "$guavapackagedir/$dirname/views/weathermapeditor.inc.php";
#1        $command = "sed 's/class weathermapeditorView .*/class weathermapeditorView$weathermap_editor_short_hostname_as_var extends GuavaApplication/g' -i $patchfile";
#1        `$command`;
#1	`chown -R nagios:nagios $guavapackagedir/$dirname`;

	print "  Patching other files.\n";
	my $patch = "$enterprise_home/bin/components/cacti";
	`cp -f $patch/auto-overlib.pl /usr/local/groundwork/nms/applications/cacti/plugins/weathermap/random-bits`;

        # Configure httpd server.
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

#1        print "    Patching guava.\n";
        $domain = @host_domain[1];
#1$cmd = "sed 's/setrawcookie(\$cookie_name, \$ticket, null, \$path);/setrawcookie(\$cookie_name,\$ticket, null, \$path, \"$domain\");/g' -i $gw_home/core/guava/htdocs/guava/includes/guava.inc.php";
#1        `$cmd`;

        print "  Building Weathermap Editor Configuration File.\n";

	$cactidir = "/usr/local/groundwork/nms/applications/cacti";
        $editorconfig = "$cactidir/plugins/weathermap/editor-config.php";
        `echo '<?php' > $editorconfig`;
        `echo '    \$cacti_base = "/usr/local/groundwork/nms/applications/cacti";' >>$editorconfig`;
        `echo '    \$cacti_url = "http://$weathermap_editor_host:$weathermap_editor_port/cacti/";' >>$editorconfig`;
        `echo '    \$mapdir="configs";' >>$editorconfig`;
        `echo '    \$ignore_cacti = FALSE;' >>$editorconfig`;
        `echo '?>' >>$editorconfig`;
	`chown nagios:nagios $editorconfig`;

        # X. Finish Up.
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

