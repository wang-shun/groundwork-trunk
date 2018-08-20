#!/usr/bin/perl

##
##	deploy.pl
##	Copyright (C) 2008, Groundwork Open Source, Inc.
##
##	Deploy a local configuration based on the enterprise.properties file.
##
##	History
##		03/07/08	Created	Daniel Emmanuel Feinsmith
##		December 2008 Bugfixes for distributed deploy
##

use Getopt::Std;
use Socket;
use Sys::Hostname;

#
#	Globals
#

my %options=();
my $version="1.0";
my $gw_home="/usr/local/groundwork";
my $enterprise_home="$gw_home/enterprise";
my $quiet_mode = 0;
my $deploy_profile;
my @properties = ();

#
#	Print Header.
#

print "=============================================================================\n";
print "= deploy.pl -- deploy local configuration                                   =\n";
print "= Copyright (C) 2008 Groundwork Open Source, Inc.                           =\n"; 
print "=============================================================================\n";

#
#	Parse Command Line Options.
#

getopts("qaf:p:dm:i:v", \%options);

#
#	Get global configuration options.
#

if (defined $options{q})
{
	$quiet_mode = 1;
}

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
		help();
		leave_normal();
	}
}

#
#	Get run option.
#

if (defined $options{a})
{
        configuration_load($configuration_file);
	deploy_all_local();
	leave_normal();
}
if (defined $options{d})
{
        configuration_load($configuration_file);
        configuration_dump();
	leave_normal();
}
if (defined $options{m})
{
        $map_option = $options{m};

        if ($map_option eq 'l' || $map_option eq 'a')
        {
                configuration_load($configuration_file);
                host_map($options{m});
        }
        else
        {
                help();
        }
	leave_normal();
}
if (defined $options{i})
{
        configuration_load($configuration_file);
        deploy_local($options{i});
	leave_normal();
}
if (defined $options{v})
{
        print "  deploy v$version\n";
	leave_normal();
}

#
#	Fall through here if
#	there is nothing left to do.
#

help();
leave_normal();

##
##	Subroutines.
##

sub deploy_all_local()
{
        my $host_name = fqdn();

        print"  Deploying All Local Components\n";
        $num_properties = @properties;
        for ($pnum=0; $pnum < $num_properties; $pnum++)
        {
                $t = $properties[$pnum]->{'type'};
                $s = $properties[$pnum]->{'subtype'};
                $i = $properties[$pnum]->{'instance'};
                $p = $properties[$pnum]->{'property'};
                $v = $properties[$pnum]->{'value'};
                if ($p eq 'host' && $v eq $host_name)
                {
			deploy_local("$t.$s.$i");
                }
        }
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

sub deploy_local()
{
	$deploy_component = shift;

	($type,$subtype,$instance) = split(/\./,$deploy_component, 3);
	print "  Deploying Component: [$type.$subtype.$instance]\n";

	#
	#	First, figure out what host we're on.
	#

	my $host_name = fqdn();
	my $host_ipaddr = "";

	$aton = inet_aton($host_name);
	if ($aton != "")
	{
		$host_ipaddr = inet_ntoa($aton);
	}

	#
	#	Now, deploy based on that configuration.
	#

	$host = find_property($type, $subtype, $instance, "host");
	if ($host eq $host_name || $host eq $host_ipaddr)
	{
		$deploy_executor = "./components/deploy_" . $type . "_" . $subtype . ".pl";
		if (-e $deploy_executor)
		{
			print "  Deploying to Local Host: $host_name ($host_ipaddr)\n";
			$cmd_line = "./$deploy_executor -q -i $type.$subtype.$instance -p $deploy_profile";
			system($cmd_line);
		}
		else
		{
			print "    No Deployment Executor for this Component.\n";
		}
	}
	else
	{
		leave_with_error("Can't find property [$type.$subtype.$instance] for host '$host_name'");
	}
	return;
}

##
##	Sub-Routines
##

#
#	configuration function set.
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
	print "  Loading Configuration File: '$configuration_file'\n";

	open(CONFIG,$configuration_file) or leave_with_error("cannot open configuration file.");
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
				if (index($line, '.') == -1) {
					next;
				}

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
				#	And strip out any spaces from the
				#	beginning and end of the line.
				#

				$count = ($line =~ tr/=//);
				if ($count == 1)
				{
					@pair = split(/=/,$line);
					$name = $pair[0]; $value = $pair[1];
					$name =~ s/^\s+//; $name =~ s/\s+$//;
					$value =~ s/^\s+//; $value =~ s/\s+$//;

					#
					#	Substitute Macros in VALUE.
					#

					if ($value eq '%LOCAL_HOSTNAME')
					{
						$value = fqdn();
					}

					#
					#	Now, split the name apart
					#	into type,subtype,instance,property
					#

					$count = ($name =~ tr/\.//);
					if ($count == 3)
					{
						($type,$subtype,$instance,$property) = split(/\./, $name, 4);

						# Now, Take apart variables.

						if (substr($value,0,1) eq "\$")
						{
							#
							#	Okay, we have a reference. First, take
							#	apart the value, and then look it up.
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

sub configuration_dump()
{
	#
	#	Dump Properties
	#

	print"  Enterprise Configuration Dump:\n";
	$num_properties = @properties;
	for ($pnum=0; $pnum < $num_properties; $pnum++)
	{
		$t = $properties[$pnum]->{'type'};
		$s = $properties[$pnum]->{'subtype'};
		$i = $properties[$pnum]->{'instance'};
		$p = $properties[$pnum]->{'property'};
		$v = $properties[$pnum]->{'value'};
		print "    $t.$s.$i.$p = $v\n";
	}
}

sub host_map()
{
	my $option = shift;

	#
	# First, create a list of all of the hosts in the system.
	#

	my @hosts = ();
	my $host_name = fqdn();

	print"  Enterprise Configuration Map for Host '$host_name':\n";
	$num_properties = @properties;
	for ($pnum=0; $pnum < $num_properties; $pnum++)
	{
		$t = $properties[$pnum]->{'type'};
		$s = $properties[$pnum]->{'subtype'};
		$i = $properties[$pnum]->{'instance'};
		$p = $properties[$pnum]->{'property'};
		$v = $properties[$pnum]->{'value'};
		if ($p eq 'host')
		{
			$good_host = 1;

			if ($option eq 'l' && !($v eq $host_name))
			{
				$good_host = 0;
			}

			if ($good_host == 1)
			{
				$num_hosts = @hosts;
				$found = 0;	
				for ($hnum=0; $hnum < $num_hosts; $hnum++)
				{
					if ($hosts[$hnum] eq $v)
					{
						++$found;
					}
				}
				if ($found == 0)
				{
					$hosts[$num_hosts] = $v;
				}
			}
		}
	}

	$num_hosts = @hosts;
	for ($hnum=0; $hnum < $num_hosts; $hnum++)
	{
		$host = $hosts[$hnum];
		# Now, for this host, display everything
		print "    $host\n";

		$num_properties = @properties;
		for ($pnum=0; $pnum < $num_properties; $pnum++)
		{
			$t = $properties[$pnum]->{'type'};
			$s = $properties[$pnum]->{'subtype'};
			$i = $properties[$pnum]->{'instance'};
			$p = $properties[$pnum]->{'property'};
			$v = $properties[$pnum]->{'value'};
			if ($p eq 'host' && $v eq $host)
			{
				print "      $t.$s.$i\n";
				
			}
		}
	}
}

#
#	Miscellany
#

sub leave_with_error()
{
	$error_string = shift;
	print "  ERROR: $error_string\n";
	leave_normal();
}

sub leave_normal()
{
	print "=============================================================================\n";
	exit;
}

sub help()
{
	my $hostname = fqdn();

	print "  Usage: deploy.pl <options>\n\n";
	print "    -f <file>      Use configuration file specified as <file>\n";
	print "    -i <instance>  Deploy a specific instance (currently only local)\n";
	print "    -a             Deploy all instances for this host ($hostname)\n";
	print "    -p <profile>   <profile> = local or distributed\n";
	print "    -d             Show dump of configuration\n";
	print "    -ma            Show Deploy Map for all hosts\n";
	print "    -ml            Show Deploy Map for this host\n";
	print "    -v             Show Version Information\n";
}

sub deploy_log_clear()
{
	`rm -f /usr/local/groundwork/enterprise/deploy.log`;
}

sub deploy_log()
{
	$message = shift;

	print $message;
	`echo $message >>/usr/local/groundwork/enterprise/deploy.log`;
}

