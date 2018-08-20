#!/usr/bin/perl
##
##      deploy_ntop.pl
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
	print "= deploy_nms_ntop.pl -- deploy local configuration                          =\n";
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

configuration_load($configuration_file);
if (!defined $options{i})
{
	leave_with_error("Instance Not Provided.");
}
else
{
	($type,$subtype,$instance) = split(/\./, $options{i}, 3);

	deploy_nms_ntop($type, $subtype, $instance);
	leave_normal();
}

sub
deploy_nms_ntop()
{
        $type = shift;
        $subtype = shift;
        $instance = shift;

        my $nms_home = "/usr/local/groundwork/nms";
        my $ntop_home = "$nms_home/applications/ntop";
        my $php_home = "$nms_home/tools/php";
	my $etc_initd = "/etc/init.d";

        print "  Deploying ntop Instance [$instance]\n";
	$ntop_port = find_property($type,$subtype,$instance,"port");

        if ($ntop_port == -1)
        {
                leave_with_error("Unable to find complete set of properties to deploy ntop.");
        }
        print "  {\n";

        print "    ntop_port = $ntop_port\n";
        print "  }\n";

        #
        # Ntop
        #

        if ((! -d $ntop_home))
        {
                leave_with_error("Ntop not installed at $ntop_home.");
        }

	# 3. Patch

        print "  Patching files.\n";
	`sed "s/APPPORT=.*/APPPORT=$ntop_port/g" $enterprise_home/bin/components/ntop/nms-ntop >$etc_initd/nms-ntop`;

        # X. Set Permissions.
        print "  Setting permissions.\n";
        `chown -R nagios:nagios $ntop_home`;
	`chmod +x $etc_initd/nms-ntop`;

	# Set up IPTables (for installer later, most likely)
	#print "  Setting firewall rules to allow port: $ntop_port\n";
	#`iptables -A RH-Firewall-1-INPUT -m state --state NEW -m tcp -p tcp --dport $ntop_port -j ACCEPT >/dev/null 2>&1`;
	#`iptables-save >/dev/null 2>&1`;

	if ( -d "/usr/local/groundwork/ntop" )
       	{
                `mkdir -p /usr/local/groundwork/nms/backups >/dev/null 2>/dev/null`;
        	print "  Moving previous version of ntop to nms/backups directory\n";
        	`mv /usr/local/groundwork/ntop /usr/local/groundwork/nms/backups`;
        }

        # Finish Up.
	`chkconfig --add nms-ntop`;
        if (status_ntop() == 0)
        {
                print "  Restarting nms-ntop\n";
                `/etc/init.d/nms-ntop stop >/tmp/nms-ntop.out 2>&1`;
	}
	else
	{
                print "  Starting nms-ntop\n";
	}
        `/etc/init.d/nms-ntop start >/tmp/nms-ntop.out 2>&1`;
        print "  Done.\n";
}

#
#       Miscellany
#


sub status_ntop()
{
        $ntop_PID = `ps -ef |grep -v grep|grep "ntop"|grep "nms"|awk '{print \$2}'`;
        if ($ntop_PID == "")
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

