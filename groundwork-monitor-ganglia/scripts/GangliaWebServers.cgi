#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2011-2017 GroundWork Open Source, Inc. ("GroundWork").
# All rights reserved.
# http://www.groundworkopensource.com
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

use strict;
use warnings;

use HTML::Entities;
use TypedConfig;

# For the time being, this isn't printed anywhere; it's only here as a marker
# for support purposes, to identify what release a customer has installed.
my $VERSION = "7.0.0";

my $config_file  = "/usr/local/groundwork/config/GangliaWebServers.conf";
my $debug_config = 0;

my $config = TypedConfig->secure_new( $config_file, $debug_config );

# Global Debug Mode Flag;  No debug = 0, Normal debug=1, Detail debug=2 (GMOND XML and metric attribute parsing)
my $debug = $config->get_number('debug_level');

# List of Ganglia web servers to allow access to.
my %ganglia_web_server_hash = $config->get_hash('ganglia_web_servers');

print Data::Dumper->Dump( [ \%ganglia_web_server_hash ], [qw(\%ganglia_web_server_hash)] ) if $debug_config;

my %ganglia_web_servers = %{ $ganglia_web_server_hash{'host'} };

print Data::Dumper->Dump( [ \%ganglia_web_servers ], [qw(\%ganglia_web_servers)] ) if $debug_config;

if ($debug_config) {
    foreach my $server ( keys %ganglia_web_servers ) {
	print "ganglia_web_servers.host = $server\n";
	my $g_host_url = $ganglia_web_servers{$server}{url};
	print "url for $server is $g_host_url\n" if defined $g_host_url;
    }
}

my $stylesheethtmlref = "";
print "Content-type: text/html\n\n";

print <<EOF;
<HTML>
<HEAD>
<META HTTP-EQUIV='Expires' CONTENT='0'>
<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
<TITLE>Ganglia Servers</TITLE>
EOF

# If $stylesheethtmlref is empty, then this href will load the current page again
# an extra time (as shown in the Apache access log) but try to interpret it as CSS
# (I suppose).  Whether or not that causes any hiccups in the browser, executing
# the user command twice on the server is not likely to be productive, so we block
# this <link> unless it's going to do something useful.
if ( $stylesheethtmlref ne '' ) {
    print <<EOF;
    <link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
EOF
}

printstyles();

my @links        = ();
my $default_link = '';
my $default_src  = '';

my $have_default_url = 0;
foreach my $server ( sort keys %ganglia_web_servers ) {
    my $g_host_url = $ganglia_web_servers{$server}{url};
    my $default    = $ganglia_web_servers{$server}{default};
    if ( defined $g_host_url ) {
	( my $server_id = $server ) =~ tr/ /_/s;
	$server_id =~ tr/-._a-zA-Z0-9//cd;
	push @links,
	    "<a href='$g_host_url' target='ganglia_frame' name='ganglia_links' id='$server_id' onclick='colorlinks(\"$server_id\");'><b><tt>"
	  . HTML::Entities::encode($server)
	  . "</tt></b></a>\n";
	if ($default) {
	    $default_link = $server_id;
	    $default_src  = "src='$g_host_url'";
	    ++$have_default_url;
	}
    }
}
if ( $have_default_url > 1 ) {
    $default_link = '';
    $default_src  = '';
}

print <<EOF;
</HEAD>
<BODY class=insight>
<script type="text/javascript" language=JavaScript>
function colorlinks(chosen) {
    var links = document.getElementsByName("ganglia_links");
    for (var i = 0; i < links.length; ++i) {
	links[i].style.color = links[i].id == chosen ? '#CC5700' : '#0000FF';
    }
}
window.onload = function () {
    colorlinks('$default_link');
}
</script>
<table width="100%" height="100%" cellpadding=0 cellspacing=0 border=0>
<tr>
<td style='padding-bottom: 0.4em; font-size: 12px;'>
EOF

if (@links) {
    print join( ' &nbsp;|&nbsp; ', @links );
}
else {
    print "No Ganglia servers are configured for access in this screen.";
}

print <<EOF;
</td>
</tr>
<tr>
<td height="100%">
<iframe id=ganglia_frame name=ganglia_frame $default_src width="100%" height="100%" frameborder=1>
    <p>Your browser does not support iframes.</p>
</iframe>
</td>
</tr>
</table>
</BODY>
</HTML>
EOF

exit;

sub printstyles {
    print <<EOF;
    <style>

    body.insight {
	background-color: #F0F0F0;
	scrollbar-face-color: #dcdcdc;
	scrollbar-shadow-color: #000099;
	scrollbar-highlight-color: #dcdcdc;
	scrollbar-3dlight-color: #000099;
	scrollbar-darkshadow-color: #dcdcdc;
	scrollbar-track-color: #dcdcdc;
	scrollbar-arrow-color: #dcdcdc;
    }

    table.insight {
	width: 100%;
	background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
	border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	text-align: center;
    }

    th.insight {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: center;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 2;
	background-color: #55609A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
    }

    td.insight {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
	border: 1px solid #666666;
	background-color: #D9D9D9;
	padding: 2;
	spacing: 2;
    }

    tr.insight {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
    }

    a {
	text-decoration: none;
    }

    a.insight:link {
	color: #414141;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insight:visited {
	color: #414141;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insight:active  {
	color: #919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insight:hover {
	color: #919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    /*Center paragraph*/
    p.insight {
	color: #000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: normal;
    }

    </style>
EOF
}
