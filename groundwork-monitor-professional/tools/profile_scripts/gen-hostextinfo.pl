#!/usr/local/groundwork/perl/bin/perl --

###############################################################################
# Release 4.0
# 01-Nov-2011
###############################################################################
#
# Original author: Scott Parris
#
# Copyright 2007-2011 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;

my %files = ();
my $dir = '/usr/local/groundwork/nagios/share/images/logos';
opendir(DIR, $dir) || print "error: cannot open $dir to read $!";
while (my $file = readdir(DIR)) {
	if ($file =~ /(\S+)\.(\S+)/) {
		$files{$1}{$2} = $file;
	}
}
close(DIR);

foreach my $name (keys %files) {
	my $output = undef;
	$output .= qq(<?xml version="1.0" encoding="iso-8859-1" ?>
<profile>
 <extended_host_info_template>
  <prop name="name"><![CDATA[$name]]></prop>
  <prop name="notes"><![CDATA[]]></prop>
  <prop name="notes_url"><![CDATA[]]></prop>
  <prop name="action_url"><![CDATA[]]></prop>
  <prop name="icon_image"><![CDATA[$files{$name}{gif}]]></prop>
  <prop name="icon_image_alt"><![CDATA[$name]]></prop>
  <prop name="vrml_image"><![CDATA[$files{$name}{png}]]></prop>
  <prop name="statusmap_image"><![CDATA[$files{$name}{gd2}]]></prop>
 </extended_host_info_template>
</profile>

);

	open(FILE, '>', "/usr/local/groundwork/core/profiles/hostextinfo-$name.xml")
	    || print "\nError: Unable to write /usr/local/groundwork/core/profiles/hostextinfo-$name.xml $!";
	print FILE $output;
	close (FILE);
}

__END__


Name: 	  	
Notes: 	 ?  	
Notes url: 	 ?  	
Action url: 	 ?  	
Icon image: 	 ?  	
Icon image alt: 	 ?  	
Vrml image: 	 ?  	
Statusmap image: 	 ?  	
2d coords: 	 ?  	
3d coords:

<data>
  <prop name="vrml_image"><![CDATA[3d_cloud.png]]>
  </prop>
  <prop name="statusmap_image"><![CDATA[3d_cloud.gd2]]>
  </prop>
  <prop name="icon_image"><![CDATA[3d_cloud.gif]]>
  </prop>
  <prop name="icon_image_alt"><![CDATA[ate]]>
  </prop>
 </data> |
