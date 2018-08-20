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
#=================================================================================
# GW CGI Initialization Stuff.
#=================================================================================

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw( Exporter );

@EXPORT = qw( $CssStyle $graphdir $graphpath $GWRoot $rrddir cgi_header ) ;

$GWRoot    = "/usr/local/groundwork" ;
$rrddir    = $GWRoot."/rrd" ;
$graphdir  = $GWRoot."/apache2/htdocs/rrd";
$graphpath = "/rrd" ;

#=================================================================================
# CssStyle 'could' be a separate file called with a <link> tag. Currently it's
# included inline.
#=================================================================================
$CssStyle  = qq(
<style>
.body
        {
            background-color:           #ffffff                            ;
            scrollbar-face-color:       #990000                            ;
            scrollbar-shadow-color:     #660000                            ;
            scrollbar-highlight-color:  #990000                            ;
            scrollbar-3dlight-color:    #660000                            ;
            scrollbar-darkshadow-color: #990000                            ;
            scrollbar-track-color:      #FFFFFF                            ;
            scrollbar-arrow-color:      #FFFFFF                            ;
        }
td
        {
            color:                      #000000                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.head
        {
            background-color:           #FFCC00                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            font-weight:                bold                               ;
            color:                      #000000                            ;
        }
td.head2
        {
            background-color:           #FFCC66                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            font-weight:                bold                               ;
            color:                      #000000                            ;
        }
td.row1
        {
            background-color:           #ffffcc                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.row2
        {
            background-color:           #eeeeee                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.warn
        {
            background-color:           #fff                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            color:                      #dd0000                            ;
        }
td.nav
        {
            background-color:           #fff                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
input, textarea, select
        {
            border:                     1px solid #990000                  ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  11px                               ;
            font-weight:                bold                               ;
            background-color:           #eeeeee                            ;
            color:                      #000000                            ;
        }
/*Left nav link*/
a.left:link
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:visited
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:active
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:hover
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }

/*Standard link*/
a.std:link
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:visited
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:active
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:hover
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
/*Standard link*/
a.head:link
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:visited
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:active
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:hover
        {
            color:                      #ffffcc                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
/*Center paragraph*/
p.center
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                normal                             ;
        }
/*Itallic paragraph*/
p.italic
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-style:                 italic                             ;
            font-size:                  12px                               ;
            font-weight:                normal                             ;
        }
/*Center bottom*/
p.bottom
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
/*Center bottom*/
p.slide
        {
            color:                      #ffffff                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
p.quote
        {
            font-size:                  14px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            color:                      #000                               ;
            line-height:                18pt                               ;
            text-align:                 left                               ;
            font-style:                 italic                             ;
            font-weight:                bold                               ;
        }
h1
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  18px                               ;
            font-weight:                bold                               ;
        }
h2
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  14px                               ;
            font-weight:                bold                               ;
        }
h3
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                bold                               ;
        }
h4
        {
            color:                      #ffffff                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                bold                               ;
        }
h5
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
</style>
) ;
sub cgi_header(@) {
    my ( $host, $service, $title_core ) = @_ ;
    return qq(Content-type: text/html

<HTML>
<HEAD>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=windows-1252">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="-1">
<TITLE>).$host." ".$service." ".$title_core.qq(</TITLE>
<style>
.body
        {
            background-color:           #ffffff                            ;
            scrollbar-face-color:       #990000                            ;
            scrollbar-shadow-color:     #660000                            ;
            scrollbar-highlight-color:  #990000                            ;
            scrollbar-3dlight-color:    #660000                            ;
            scrollbar-darkshadow-color: #990000                            ;
            scrollbar-track-color:      #FFFFFF                            ;
            scrollbar-arrow-color:      #FFFFFF                            ;
        }
td
        {
            color:                      #000000                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.head
        {
            background-color:           #FFCC00                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            font-weight:                bold                               ;
            color:                      #000000                            ;
        }
td.head2
        {
            background-color:           #FFCC66                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            font-weight:                bold                               ;
            color:                      #000000                            ;
        }
td.row1
        {
            background-color:           #ffffcc                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.row2
        {
            background-color:           #eeeeee                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
td.warn
        {
            background-color:           #fff                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
            color:                      #dd0000                            ;
        }
td.nav
        {
            background-color:           #fff                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12                                 ;
        }
input, textarea, select
        {
            border:                     1px solid #990000                  ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  11px                               ;
            font-weight:                bold                               ;
            background-color:           #eeeeee                            ;
            color:                      #000000                            ;
        }
/*Left nav link*/
a.left:link
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:visited
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:active
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.left:hover
        {
            color:                      #CC3300                            ;
            font-size:                  10px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }

/*Standard link*/
a.std:link
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:visited
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:active
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
a.std:hover
        {
            color:                      #CC3300                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                normal                             ;
        }
/*Standard link*/
a.head:link
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:visited
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:active
        {
            color:                      #ffffff                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
a.head:hover
        {
            color:                      #ffffcc                            ;
            font-size:                  12px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            text-decoration:            underline                          ;
            font-weight:                bold                               ;
        }
/*Center paragraph*/
p.center
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                normal                             ;
        }
/*Itallic paragraph*/
p.italic
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-style:                 italic                             ;
            font-size:                  12px                               ;
            font-weight:                normal                             ;
        }
/*Center bottom*/
p.bottom
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
/*Center bottom*/
p.slide
        {
            color:                      #ffffff                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
p.quote
        {
            font-size:                  14px                               ;
            font-family:                arial, helvetica, sans-serif       ;
            color:                      #000                               ;
            line-height:                18pt                               ;
            text-align:                 left                               ;
            font-style:                 italic                             ;
            font-weight:                bold                               ;
        }
h1
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  18px                               ;
            font-weight:                bold                               ;
        }
h2
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  14px                               ;
            font-weight:                bold                               ;
        }
h3
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                bold                               ;
        }
h4
        {
            color:                      #ffffff                            ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  12px                               ;
            font-weight:                bold                               ;
        }
h5
        {
            color:                      #000                               ;
            font-family:                arial, helvetica, sans-serif       ;
            font-size:                  10px                               ;
            font-weight:                normal                             ;
        }
</style>
</HEAD>
) ;
}
#=================================================================================
1 ;
