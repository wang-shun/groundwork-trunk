#!/usr/local/groundwork/perl/bin/perl -w

# A helper script to use when trying to get the basic REST stuff working via curl before implementing in RAPID.
# Fiddle as necessary.

use strict;
use MIME::Base64;
use JSON;
my ( $app_name, $token, $api_url, $cmd, $json );

$app_name = "RAPIDtesting"; 
auth( \$token, \$api_url );
logout();

exit;

#get( { "url" => "customgroups/autocomplete/test-" } );


$json = <<EOF;;
{
  "customGroups" : [ {
    "name" : "TEST-CUSTOM-GROUP-0",
    "description" : "TEST-CUSTOM-GROUP-0",
    "appType" : "NAGIOS",
    "agentId" : "TEST-AGENT",
    "hostGroupNames" : ["TEST-HOST-GROUP-0"]
  }, {
    "name" : "TEST-CUSTOM-GROUP-1",
    "agentId" : "TEST-AGENT",
    "serviceGroupNames" : ["TEST-SERVICE-GROUP-0"]
  }, {
    "name" : "TEST-CUSTOM-GROUP-2",
    "appType" : "NAGIOS"
  }, {
    "name" : "TEST-CUSTOM-GROUP-3"
  } ]
}
EOF

post(  { "url" => "customgroups", "json" => $json  } );

exit;
$json = <<EOF;;
{
  "customGroups" : [ { "name" : "TEST-CUSTOM-GROUP-2" }, { "name" : "TEST-CUSTOM-GROUP-3" } ]
}
EOF
api_delete( { "url" => "customgroups" , "json" => $json } );


exit;


# old stuff

$json = <<EOF;;
{
	"bizHostServiceInDowntimes" : [
    		{
      			"entityName" : "test_group",
      			"entityType" : "HOSTGROUP",
      			"hostName" : "ahost1"
    		},
    		{
      			"entityName" : "test_group",
      			"entityType" : "HOSTGROUP",
      			"hostName" : "ahost4"
    		}
	]
}
EOF

$cmd = "curl -i -k -H 'GWOS-API-TOKEN:" . auth() . "' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -X POST -d '$json' $api_url/biz/getindowntime";
#my $cmd = "curl -i -k -H 'GWOS-API-TOKEN:$token' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -X POST -d@./perf1.json $api_url/perfdata";
#my $cmd = "curl -i -k -H 'GWOS-API-TOKEN:$token' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -X POST -d@./perf2.json $api_url/perfdata";
#print "$cmd\n";
system( $cmd );




# ----------------------------------------------------------------
sub auth
{
	my ( $token_ref, $url_ref ) = @_;
	my $user = `grep ^webservices_user /usr/local/groundwork/config/ws_client.properties | cut -d= -f2`;
	my $password = `grep ^webservices_password /usr/local/groundwork/config/ws_client.properties | cut -d= -f2`;
	$$url_ref = `grep ^foundation_rest_url /usr/local/groundwork/config/ws_client.properties | cut -d= -f2`;
	chomp $user; chomp $password; chomp $$url_ref;
	$user =~ s/(^\s+|\s+$)//g;
	$password =~ s/(^\s+|\s+$)//g;
	$$url_ref =~ s/(^\s+|\s+$)//g;
	$user=encode_base64($user); # this adds a \n again
	chomp $user;
        #my $cmd = "curl -s -k -i -X POST  --data-urlencode \"user=$user\" --data-urlencode \"password=$password\" --data-urlencode \"gwos-app-name=$app_name\" $$url_ref/auth/login | tail -1";
	${$token_ref} = ` curl -s -k -i -X POST  --data-urlencode \"user=$user\" --data-urlencode \"password=$password\" --data-urlencode \"gwos-app-name=$app_name\" $$url_ref/auth/login | tail -1`;
}

# ----------------------------------------------------------------
sub logout
{
	#my $url =  $api_url . "/auth/logout?gwos-app-name=$app_name&gwos-api-token=$token" ;
	my $url =  $api_url . "/auth/logout?gwos-api-token=$token&gwos-app-name=$app_name" ;
	my $url2 = $api_url . "/auth/logout";
	my $cmd = "curl -i -s -X POST -H 'Accept: text/plain' '$url'";

	$token = $ARGV[0] if defined $ARGV[0] ;
	$cmd = "curl -i -s -X POST -d \"gwos-api-token=$token&gwos-app-name=$app_name\" $url2";
	
	print "CMD = $cmd\n";
	system ( $cmd ) ;
	#post( { "url" => "auth/logout" } );
}

# ----------------------------------------------------------------
sub get
{
	my ( $commands ) = @_;
	my $cmd;
	if ( $commands->{url} ) { 
		$cmd = "curl -i -H 'GWOS-API-TOKEN:$token' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -X GET $api_url/$commands->{url}";
	}
	else { 
		# more stuff tbd here 
 	}
	system ( $cmd ) ;
}

# ----------------------------------------------------------------
sub post
{
	my ( $commands ) = @_;
	my $cmd;
	if ( $commands->{url} ) { 
		$cmd = "curl -X POST -i -H 'GWOS-API-TOKEN:$token' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$commands->{json}' $api_url/$commands->{url}";
	}
	else { 
		# more stuff tbd here 
 	}
	system ( $cmd ) ;
}

# ----------------------------------------------------------------
sub api_delete
{
	my ( $commands ) = @_;
	my $cmd;
	$cmd = "curl -X DELETE -i -H 'GWOS-API-TOKEN:$token' -H 'GWOS-APP-NAME:$app_name' -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$commands->{json}' $api_url/$commands->{url}";
	system ( $cmd ) ;
}

