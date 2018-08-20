#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2016 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
# Fifth Floor, Boston, MA 02110-1301, USA.
#
#--------------------------------------------------------------------------------#
use lib "/usr/local/groundwork/lib";
use strict;
use RRDs;
use CGI;
use locale;

#--------------------------------------------------------------------------------#
# GwCGInit is a common module for CGI's
#--------------------------------------------------------------------------------#
use lib "/usr/local/groundwork/apache2/cgi-bin" ;
use gwcginit ;
use vars qw( $CssStyle $graphdir $graphpath $GWRoot $rrddir ) ;

#--------------------------------------------------------------------------------#
# Read CGI parameters. Sometimes we'll get host, sometimes name.
#--------------------------------------------------------------------------------#
my $query   = new CGI;
my $host    = $query->param('host');
my $hnam    = $query->param('name');
$host       = $hnam if ($host eq '') ;
my $service = $query->param('service');
my $ifspeed = $query->param('ifspeed');

#--------------------------------------------------------------------------------#
# Declare local vars and functions
#--------------------------------------------------------------------------------#
my ( $i, $start, $end, $defstring, $def, $line, $key, $errstr, $graphfile, $title
   , $ifspeed_line, $ifspeed_line2, $ifspeed_line3, $title_prefix ) = undef ;
my @graphs = () ;
sub body() ;

#--------------------------------------------------------------------------------#
# You never have to worry about the name of the script again.
# This var, cgi_name, is used in the form for the refresh button.
#--------------------------------------------------------------------------------#
my $cgi_name       = $query->url(-relative=>1) ;
my $form_spec      = "<form action=/nagios/cgi-bin/".$cgi_name." method=get>" ;
my $hidden_host    = "<input type=hidden name=name value=".$host.">" ;
my $hidden_service = "<input type=hidden name=service value=".$service.">" ;
my $hidden_ifspeed = ( $ifspeed eq '' ? ""
                       : "<input type=hidden name=ifspeed value=".$ifspeed.">" ) ;
my $submit_refresh = "<input type=submit value=Refresh>" ;

#--------------------------------------------------------------------------------#
# Vars that include Host_Service ($hs) in their strings.
#--------------------------------------------------------------------------------#
my $hs     = $host."_".$service ;
my $rrd    = $rrddir."/".$hs.".rrd" ;
my $gfnam  = $hs."_bwidth" ;

#--------------------------------------------------------------------------------#
# Titles. vlabel is the vertical left side label.
#--------------------------------------------------------------------------------#
my $vlabel       = "Percent Utilization";
my $title_core   = "Bandwidth Utilization" ;

#================================================================================#
# This is where you specify your graphs.
# 'title' is the time period that is appended to the title.
# 'start' is now - n seconds. you specify the n number of seconds in 'start'.
#================================================================================#
$graphs[0]{'title'} = "2 Hours"  ;	$graphs[0]{'start'} = 7200     ; # 2*60*60
$graphs[1]{'title'} = "48 Hours" ;	$graphs[1]{'start'} = 172800   ; # 48*60*60
$graphs[2]{'title'} = "14 Days"  ;	$graphs[2]{'start'} = 1209600  ; # 14*24*60*60
$graphs[3]{'title'} = "60 Days"  ;	$graphs[3]{'start'} = 5184000  ; # 60*24*60*60
$graphs[4]{'title'} = "360 Days" ;	$graphs[4]{'start'} = 31104000 ; # 360*24*60*60

#================================================================================#
# Do the output.
#--------------------------------------------------------------------------------#

print cgi_header( $host, $service, $title_core )
     .body()
     ."\n</html>\n"
     ;

exit 0 ;

#--------------------------------------------------------------------------------#
# Local Subroutines.
#--------------------------------------------------------------------------------#
sub body() {
    #----------------------------------------------------------------------------#
    # If interface speed was specified as a input parameter, use it in RRD
    # specification. Otherwise use what is in the Date Source.
    #----------------------------------------------------------------------------#
    if ($ifspeed eq '') {
        $ifspeed_line  = "\"DEF:ifspeed=$rrd:ifspeed:AVERAGE\", ";
        $ifspeed_line2 = "\"CDEF:inutl=intmp,ifspeed,/\", " ;
        $ifspeed_line3 = "\"CDEF:oututl=outtmp,ifspeed,/\", " ;
    }
    else {
        $ifspeed_line  = "\"DEF:ifspeed=$rrd:ifspeed:AVERAGE\", ";
        $ifspeed_line2 = "\"CDEF:inutl=intmp,".$ifspeed.",/\", " ;
        $ifspeed_line3 = "\"CDEF:oututl=outtmp,".$ifspeed.",/\", " ;
    }

    #----------------------------------------------------------------------------#
    # Finish off the RRD Graph specifications
    #----------------------------------------------------------------------------#
	$def .= "\"DEF:in=$rrd:in:AVERAGE\", ";
	$def .= $ifspeed_line ;
	$def .= "\"CDEF:intmp=in,8,*,100,*\", ";
	$def .= $ifspeed_line2;

	$line .= "\"AREA:inutl#00FF00:in_util\", ";
	$line .= "\"GPRINT:inutl:MIN:(min=%.0lf\", ";
	$line .= "\"GPRINT:inutl:AVERAGE:ave=%.0lf\", ";
	$line .= "\"GPRINT:inutl:MAX:max=%.0lf)\", ";

	$def .= "\"DEF:out=$rrd:out:AVERAGE\", ";
	$def .= "\"CDEF:outtmp=out,8,*,100,*\",";
	$def .= $ifspeed_line3;

	$line .= "\"LINE2:oututl#FF00FF:out_util\", ";
	$line .= "\"GPRINT:oututl:MIN:(min=%.0lf\", ";
	$line .= "\"GPRINT:oututl:AVERAGE:ave=%.0lf\", ";
	$line .= "\"GPRINT:oututl:MAX:max=%.0lf)\", ";

	$defstring = "$def $line";
	$defstring =~ s/,\s$//;
	$end = time ;

    $title_prefix = $host." ".$service." ".$title_core." Last " ;

    #----------------------------------------------------------------------------#
    # Here we set other data for each graph and actually create the .png file
    #----------------------------------------------------------------------------#
    for ($i=0; $i<=$#graphs; $i++) {

        $start     = $end - $graphs[$i]{'start'} ;
        $graphfile = $graphdir."/".$gfnam.$i.".png" ;
        $title     = $title_prefix.$graphs[$i]{'title'} ;

		my($averages,$xsize,$ysize);
		my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", "$vlabel", "-X", "0", "-t", "$title", "-w", 750, "-h", 200, "-l", 0, "-u", 5, '.$defstring.");";
		eval($evalstring);
		my $err = RRDs::error;
        $errstr .= "$err<br>" if ($err) ;
    }

    #----------------------------------------------------------------------------#
    # Begin constructing the HTML to display either the graphs or the error msg
    # from attempting to create the graphs.
    #----------------------------------------------------------------------------#
    my $return_body = qq(
        <table width=70% bgcolor=#000000 cellspacing=0 cellpadding=0 border=1>
        <tr><td class=valign=top>
                <table width=100% bgcolor=#ffffff cellpadding=1 cellspacing=1 border=0>
                $form_spec
                    $hidden_host
                    $hidden_service
                    $hidden_ifspeed
                <tr><td class=head align=center>).$host ;

    #----------------------------------------------------------------------------#
    # Success ....
    #----------------------------------------------------------------------------#
	if ($errstr !~ /\w+/) {
		$return_body .= " ".$service." ".$title_core.qq(</td></tr>
		)."\n" ;

        #------------------------------------------------------------------------#
        # Loop thru the graphs and create a table row for each image.
        #------------------------------------------------------------------------#
        for ($i=0; $i<=$#graphs; $i++) {
            $return_body .= qq(
                <tr><td align=center><img src=/rrd/).$gfnam.$i.qq(.png></td>
                    <td>$submit_refresh</td>
                    </tr>
                ) ;
        }

        #------------------------------------------------------------------------#
        # Place a refresh form that preserves all CGI parameters
        #------------------------------------------------------------------------#
        $return_body .= qq(
                <tr><td align=center>
                        $submit_refresh
                        </td></tr>);

    #----------------------------------------------------------------------------#
    # or Errors ....
    #----------------------------------------------------------------------------#
	} else {
        #------------------------------------------------------------------------#
        # Write out the error strings.
        #------------------------------------------------------------------------#
		$return_body .= qq( Errors!</td></tr>
                    <td align=center>
                        <h2>The following occurred while generating the report:</h2>
                        </td></tr>
                <tr><td align=center>).$errstr."</td></tr>" ;
	}

    #----------------------------------------------------------------------------#
    # Finish off the Table and Body Tags.
    #----------------------------------------------------------------------------#
	return $return_body.qq(
                </form></table>
                </td></tr>
        </table>
</body>) ;
}
#--------------------------------------------------------------------------------#
#--------------------------------------------------------------------------------#
exit 0 ;
