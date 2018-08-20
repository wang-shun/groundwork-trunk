#!/usr/bin/perl
##
##      deploy_nms_httpd.pl
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

#
#       Print Header.
#

if (!(defined $options{q}))
{
	print "=============================================================================\n";
	print "= deploy_nms_httpd.pl -- deploy local configuration                         =\n";
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

	deploy_nms_httpd($type, $subtype, $instance);
	leave_normal();
}

sub
deploy_nms_httpd()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        my $nms_home = "/usr/local/groundwork/nms";
        my $httpd_home = "$nms_home/tools/httpd";

        print "  Deploying Httpd Instance [$instance]\n";
        $httpd_port = find_property($type,$subtype,$instance, "port");
	$httpd_auth_login_port = find_property($type,$subtype,$instance, "auth_login:port");
	$httpd_auth_domain = find_property($type,$subtype,$instance, "auth_domain");

	if ($deploy_profile eq 'local')
	{
        	$httpd_host = shortname();
		$httpd_auth_login_host = shortname();
	}
	else
	{
        	$httpd_host = find_property($type,$subtype,$instance, "host");
		$httpd_auth_login_host = find_property($type,$subtype,$instance, "auth_login:host");
	}

        if ($httpd_host == -1 || $httpd_port == -1 || $httpd_auth_login_host == -1 || $httpd_auth_login_port == -1 || $httpd_auth_domain == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy Httpd.");
        }
        print "  {\n";

        print "    httpd_host = $httpd_host\n";
        print "    httpd_port = $httpd_port\n";
	print "    http_auth_login:host = $httpd_auth_login_host\n";
	print "    http_auth_login:port = $httpd_auth_login_port\n";
	print "    httpd_auth_domain = $httpd_auth_domain\n";
        print "  }\n";

        #
        # Httpd
        #

        if ((! -d $httpd_home))
        {
                leave_with_error("Httpd not installed at $httpd_home.");
        }

        # Patch

        print "  Setting Listener Port.\n";
	$cmd = "sed \"s/Listen .*/Listen $httpd_port/g\" -i $httpd_home/conf/httpd.conf";
	`$cmd`;

        print "  Setting Server Name.\n";
	$cmd = "sed \"s/#ServerName.*/ServerName $httpd_host:$httpd_port/g\" -i $httpd_home/conf/httpd.conf";
	`$cmd`;

	# Setting Authorization Tickets.
	print "  Setting authorization ticketing information.\n";
	$cmd = "sed \"s/TKTAuthLoginURL .*/TKTAuthLoginURL http:\\/\\/$httpd_auth_login_host:$httpd_auth_login_port/g\" -i $httpd_home/conf/httpd.conf";
	`$cmd`;
	$cmd = "sed \"s/TKTAuthDomain .*/TKTAuthDomain $httpd_auth_domain/g\" -i $httpd_home/conf/httpd.conf";
	`$cmd`;

	# Copy init script
	print "  Copying init.d script.\n";
	my $patch = "$enterprise_home/bin/components/httpd";
        my $INSTALLDIR = "/etc/init.d";
        `cp -f $patch/nms-httpd $INSTALLDIR`;

        # Set Permissions.
        print "  Setting permissions.\n";
        `chown -R nagios:nagios $httpd_home/conf/httpd.conf`;

	# Set IPTables for now (will be done by the installer later, most likely)
	#print "  Setting firewall rules to allow port: $httpd_port\n";
	#`iptables -A RH-Firewall-1-INPUT -m state --state NEW -m tcp -p tcp --dport $httpd_port -j ACCEPT >/dev/null 2>&1`;
	#`iptables-save >/dev/null 2>&1`;

        # Finish Up.
	`chkconfig --add nms-httpd`;
	if (status_httpd() == 0)
	{
		print "  Restarting nms-httpd\n";
		`/etc/init.d/nms-httpd stop >/dev/null 2>&1`;
	}
	else
	{
		print "  Starting nms-httpd\n";
	}
	`/etc/init.d/nms-httpd start >/dev/null 2>&1`;
        print "  Done.\n";
}

#
#       Miscellany
#

sub status_httpd()
{
        $httpd_PID = `ps -ef |grep -v grep|grep "httpd"|grep "nms"|awk '{print \$2}'`;
        if ($httpd_PID == "")
        {
		return 0;
        }
        else
        {
		return 1;
        }
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

