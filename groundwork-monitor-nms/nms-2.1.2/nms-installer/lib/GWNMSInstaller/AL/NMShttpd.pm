#!/usr/bin/perl
package GWNMSInstaller::AL::NMShttpd;
@ISA = qw(GWInstaller::AL::httpd);
use Socket;

sub deploy
{
    my ($deploy_profile,$installer) = @_;
    my $props = $installer->get_properties();
	unless($deploy_profile){$deploy_profile = "local";}
    my @properties = ();
    my $result = 0;
    my $host_name = $props->get_fqdn();
    my $propfile = $installer->get_properties();
    @properties =  $propfile->configuration_load();
    if (@properties != null)
    {
        $num_properties = @properties;

        my $instance = GWNMSInstaller::AL::NMSProperties::find_instance(\@properties, "nms", "httpd", $host_name);
        if (!($instance eq null))
        {
            $result = deploy_nms_httpd(\@properties, $instance, $deploy_profile);
        }
    }

    return($result);
}

#     ========================
#     Deploy NMS Httpd Service
#     ========================

sub deploy_nms_httpd
{
    my ($ref_properties, $instance, $deploy_profile) = @_;
    my @properties = @{$ref_properties};
    $type = "nms";
    $subtype = "httpd";

        my $nms_home = "/usr/local/groundwork/nms";
        my $httpd_home = "$nms_home/tools/httpd";
	my $enterprise_home = "/usr/local/groundwork/enterprise";

        GWInstaller::AL::GWLogger::log("\tDeploying NMS httpd Instance [$instance] $deploy_profile");
        $httpd_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "port");
        $httpd_auth_login_port = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "auth_login:port");
        $httpd_auth_domain = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "auth_domain");
        if ($deploy_profile eq "local")
        {
        	GWInstaller::AL::GWLogger::log("local deploy");
                $httpd_host =  GWNMSInstaller::AL::NMSProperties::shortname();                
                $httpd_auth_login_host = GWNMSInstaller::AL::NMSProperties::shortname();
                
        }
        else
        {
        	GWInstaller::AL::GWLogger::log("distributed deploy");
                $httpd_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "host");
                $httpd_auth_login_host = GWNMSInstaller::AL::NMSProperties::find_property(\@properties,$type,$subtype,$instance, "auth_login:host");
        }

        if ($httpd_host == -1 || $httpd_port == -1 || $httpd_auth_login_host == -1 || $httpd_auth_login_port == -1 || $httpd_auth_domain == -1)
        {
                GWInstaller::AL::GWLogger::log("Unable to find complete set of properties to deploy httpd.");
		return(0);
        }
        GWInstaller::AL::GWLogger::log("{");

        GWInstaller::AL::GWLogger::log("httpd_host = $httpd_host");
        GWInstaller::AL::GWLogger::log("httpd_port = $httpd_port");
        GWInstaller::AL::GWLogger::log("http_auth_login:host = $httpd_auth_login_host");
        GWInstaller::AL::GWLogger::log("http_auth_login:port = $httpd_auth_login_port");
        GWInstaller::AL::GWLogger::log("httpd_auth_domain = $httpd_auth_domain");
        GWInstaller::AL::GWLogger::log("}");

        #
        # Httpd
        #

        if ((! -d $httpd_home))
        {
                GWInstaller::AL::GWLogger::log("Httpd not installed at $httpd_home.");
		return(0);
        }

        # Patch

        GWInstaller::AL::GWLogger::log("Setting Listener Port.");
        $cmd = "sed \"s/Listen .*/Listen $httpd_port/g\" -i $httpd_home/conf/httpd.conf";
        `$cmd 2>>/dev/null`;

        GWInstaller::AL::GWLogger::log("Setting Server Name.");
        $cmd = "sed \"s/#ServerName.*/ServerName $httpd_host:$httpd_port/g\" -i $httpd_home/conf/httpd.conf";
        `$cmd 2>>/dev/null`;

        # Setting Authorization Tickets.
        GWInstaller::AL::GWLogger::log("Setting authorization ticketing information.");
        $cmd = "sed \"s/TKTAuthLoginURL .*/TKTAuthLoginURL http:\\/\\/$httpd_auth_login_host:$httpd_auth_login_port/g\" -i $httpd_home/conf/httpd.conf";
        `$cmd 2>>/dev/null`;
        $cmd = "sed \"s/TKTAuthDomain .*/TKTAuthDomain $httpd_auth_domain/g\" -i $httpd_home/conf/httpd.conf";
        `$cmd 2>>/dev/null`;

        # Copy init script
        GWInstaller::AL::GWLogger::log("Copying init.d script.");
        my $patch = "$enterprise_home/bin/components/httpd";
        my $INSTALLDIR = "/etc/init.d";
        `cp -f $patch/nms-httpd $INSTALLDIR 2>>/dev/null`;

        # Set Permissions.
        GWInstaller::AL::GWLogger::log("Setting permissions.");
        `chown -R nagios:nagios $httpd_home/conf/httpd.conf 2>>/dev/null`;

        # Set IPTables for now (will be done by the installer later, most likely)
        #GWInstaller::AL::GWLogger::log("Setting firewall rules to allow port: $httpd_port");
        #`iptables -A RH-Firewall-1-INPUT -m state --state NEW -m tcp -p tcp --dport $httpd_port -j ACCEPT >/dev/null 2>&1 2>>/dev/null`;
        #`iptables-save >/dev/null 2>&1 2>>/dev/null`;

        # Finish Up.
        `chkconfig --add nms-httpd 2>>/dev/null`;
        if (status_httpd() == 0)
        {
                GWInstaller::AL::GWLogger::log("Restarting nms-httpd");
                `/etc/init.d/nms-httpd stop >/dev/null 2>&1 2>>/dev/null`;
        }
        else
        {
                GWInstaller::AL::GWLogger::log("Starting nms-httpd");
        }
        `/etc/init.d/nms-httpd start >/dev/null 2>&1 2>>/dev/null`;
        GWInstaller::AL::GWLogger::log("Done.");
}

#
#       Miscellany
#

sub status_httpd()
{
        $httpd_PID = `ps -ef |grep -v grep|grep "httpd"|grep "nms"|awk '{print \$2}' 2>>/dev/null`;
        if ($httpd_PID == "")
        {
                return 0;
        }
        else
        {
                return 1;
        }
}

sub set_auth_login{
	my($self,$auth) = @_;
	$self->{auth_login} = $auth;
		
}
sub get_auth_login{
	my $self = shift;
	return $self->{auth_login};
}

sub set_auth_domain{
	my($self,$auth) = @_;
	$self->{auth_domain} = $auth;
		
}
sub get_auth_domain{
	my $self = shift;
	return $self->{auth_domain};
} 
1;
