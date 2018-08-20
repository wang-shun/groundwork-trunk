#!/usr/local/groundwork/perl/bin/perl

# COPYRIGHT:
#
# This software is Copyright (c) 2016      RealStuff Informatik AG, Andreas Wenger
#
#
# LICENSE:GPL2
# see noma_daemon.pl in parent directory for full details.
# Please do not distribute without the above file!

my $downApi;

# if we look for downtimes in GroundWork
if (1) {
    use GWDOWN;
     
    # The application name by which the test.pl process
    # will be known to the Foundation REST API.
    my $rest_api_requestor = "NotificationManager";
    
    # Where to find credentials for accessing the Foundation REST API.
    my $ws_client_config_file = "/usr/local/groundwork/config/ws_client.properties";

    # The noma_daemon.pl process is multi-threaded, so we MUST specify the multithreaded
    # option here with a true value.  Otherwise, the REST API calls won't work.
    #
    my %rest_api_options = (
        access        => $ws_client_config_file,
        multithreaded => 1
    );
    
    $downApi = GWDOWN->new($rest_api_requestor, \%rest_api_options);
    die "FATAL:  Cannot configure the GWDOWN package." if not $downApi;
}

# Check if host / service is in Downtime
sub getInDowntime {
    my ($host, $service) = @_;
    
    if ($service ne "") {
        debug("Host: $host Service: $service",1);
        return $downApi->getInDowntime($host,$service);
    }
    else {
        debug("Host: $host",1);
        return $downApi->getInDowntime($host);
    }
}

1;
