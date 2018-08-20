#!/usr/bin/perl

$USAGE = << 'stop';
----------------------------------------------------------------------
- perfconfig-create                         to make service profiles -
----------------------------------------------------------------------
- USAGE: perfconfig-create  [{FLAGS}] -i {profile}                   -
-                                                                    -
- WHERE: -i {profile}           is the name of a generator profile   -
-                               with specific sections for each of   -
-                               the subsystems being specified.      -
-                                                                    -
- FLAGS: -v                     engages verbose operation            -
-                                                                    -
-        -o {filename}          creates a specific file, instead     -
-                               of the one composed of the profile-  -
-                               configuration name.                  -
-                                                                    -
-        -io                    turns on 'streaming mode', where     -
-                               the PROFILE comes from STDIN, and    -
-                               the OUTPUT goes to STDOUT.  This is  -
-                               helpful for some kind of scripts     -
-                               which may have a script forming the  -
-                               input stream, and the output is then -
-                               post-processed by another script.    -
-                                                                    -
-        -template              prints a help message that describes -
-                               how the templates or profiles are    -
-                               keyed in.                            -
-                                                                    -
- AS IN: perfconfig-create -i gdma.perfpro                           -
-                                                                    -
- WHICH: reads 'gdma.perfpro',     and parses out the generator data -
-        within, then generates 'perfconfig-gdma.xml' as the         -
-        profile itself from the generator output.  Errors to STDOUT -
----------------------------------------------------------------------
- Copyright (C) 2012 by Groundwork Open Source, Inc.   San Francisco -
----------------------------------------------------------------------
stop

$TEMPLATE = << 'stump';
----------------------------------------------------------------------
- perfconfig-create                      TEMPLATE help pages...      -
----------------------------------------------------------------------
- The idea with the templates is that they minimize the repetitious  -
- entry of very similar data (which, being repetitious, often is     -
- prone to errors)                                                   -
-                                                                    -
- The format of [gdma.perfpro]:                                      -
-                                                                    -
----------------------------------------------------------------------
# ----------------------------------------------------------------------
# performance_configuration = {name}
#  service_profile   = {name}
#   graph            = {name}|{host}|{service}|{type}|{enable}|{label}
#    rrdname         = {text}               # full string
#    rrdcreatestring = {text}               # full command
#    rrdupdatestring = {text}               # full command
#    graphcgi        = {text}
#    ...               {text}
#    graphcgi        = {text}               # each line concats to prior
#    parseregx       = {first}|{regx}       # first is 1|0
#    perfidstring    = {text}               # usually empty
# ----------------------------------------------------------------------
performance_configuration = gdma_21_linux
 service_profile   = disk
  graph            = graph|*|regx="1"|nagios|1|Disk Utilization
   rrdname         = /usr/local/groundwork/rrd/RHOST$_$SERVICE$.rrd
   rrdcreatestring = $RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$DS:$LABEL#$:GAUGE:1800:U:U DS:$LABEL#$_wn:GAUGE:1800:U:U DS:$LABEL#$_cr:GAUGE:1800:U:U DS:$LABEL#$_mx:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480
   rrdupdatestring = $RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$:$WARN1$:$CRIT1$:$MAX1$ 2>&1
   graphcgi        = 'rrdtool graph -
   graphcgi        = DEF:a="rrd_source":ds_source_0:AVERAGE
   graphcgi        = DEF:w="rrd_source":ds_source_1:AVERAGE
   ...
   graphcgi        = AREA:cdefcs#FF0033
   graphcgi        = -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y  -l 0'
   parseregx       = 0|
   perfidstring    = 
# ----------------------------------------------------------------------
- The {{profile}} and {{service_name}} macros are substituted with   -
- the service_profile name (first parameter) and the service_name    -
- name (first paramter as well).   This keeps your files much more   -
- generic and portable, minimizing the number of places to duplicate -
- information.                                                       -
-                                                                    -
- The 'comment block' does not need to be in the profile, but ought  -
- to be, just to remind yourself of formats, in the future.          -
-                                                                    -
- Indentation isn't important.  What IS important though is that     -
- there is a nested structure (in the sense of a sequence) in the    -
- data itself.                                                       -
-                                                                    -
- THERE IS MAGIC in the naming of the sections on output.  The       -
- conventions adopted by Groundwork Open Source as of August 2012    -
- were used to compose composite names.                              -
-                                                                    -
- "time_period" can at this point only refer to 24x7 as a name; the  -
- parameters are hard-coded into the PERL source.  It is the most    -
- common metric.                                                     -
-                                                                    -
- "service_template" being set to "gdma" is, like time period, a     -
- complex internal fixed-format setting.  If you need changes go to  -
- the original source code.                                          -
-                                                                    -
- "extended_service_info_template" is NOT a predefined setting, and  -
- rather has a list of parameters that become part of its XML block. -
-                                                                    -
----------------------------------------------------------------------
- Copyright (C) 2012 by Ground Work Open Source, Inc.    San Francisco
----------------------------------------------------------------------
stump

$streamingmode = 0;              # TRUE if to work in 'streaming' mode
$dpro_file     = "";             # sets input profile
$dout_file     = "";             # sets output filename (if set)
$profile_name  = "";
$verbose       = 0;
# ----------------------------------------------------------------------
# VERSION HISTORY... just maintain this, please.  There may be better
#                    methods, but this is simple, and to the point.   
# ----------------------------------------------------------------------
$version       = "1.1.2";        # 120821.rlynch - date of creation.
$version       = "1.1.3";        # 120821.rlynch - version, verbose, -template instrux
$version       = "1.1.4";        # 120822.rlynch - cleanup of comments
$version       = "1.1.5";        # 120822.rlynch - added [-o, -io] modes;
$version       = "1.1.6";        # 120823.rlynch - [performance profile branch]

&main;

sub main
{
	die $USAGE unless @ARGV;

    print STDERR "perfconfig-create.pl    Version $version\n";

	for( my $i = 0; $i < @ARGV; $i++ )
	{
		$_ = $ARGV[ $i ];
		my $next = $i + 1 < @ARGV ? $ARGV[ $i + 1 ] : "";

		if(    /^-i$/ )        { $dpro_file = $next; $i++; next; }
        elsif( /^-o$/ )        { $dout_file = $next; $i++; next; }
        elsif( /^-io$/)        { $streamingmode = 1;       next; }
		elsif( /^-v$/ )        { $verbose++;               next; }
		elsif( /^-template$/ ) { print $TEMPLATE; exit 1; }
		else
		{
			printf STDERR "unknown command argument '%s'\n", $_;
			exit(0);
		}
	}

    my $ifp = STDIN;
    if( ! $streamingmode )
    {
        die "No [-i {profile}] generator file" unless $dpro_file;
        die "Couldn't open profile '$dpro_file'" unless open PRO, "$dpro_file";
        $ifp = PRO;
    }

	&read_profile( $ifp );
	&generate_output;
	&write_output;

	close $ifp unless $streamingmode;
	exit(1);
}

sub get2parts
{
	my $line = shift;
	my $a; 
	my $b;

    # ---------------------------------------------------
    # look for an = sign.  take the left and right halves
    # ---------------------------------------------------
	if( $line =~ m/^([^=]+)=(.*)/ )
	{
		$a = lc $1;    # the '$id' side is always LC
		$b =    $2;

		$a =~ s/^\s+//; $a =~ s/\s+$//;  # trim spaces
		$b =~ s/^\s+//; $b =~ s/\s+$//;  # for both

		return( $a, $b );
	}
	return( $line, "" );  # OR, if no = sign, just return left
}

sub read_profile
{
    my $ifp = shift;
	my $current_profile_name     = "";
	my $current_service_name     = "";
	my $complex_service_name     = "";
	my $current_service_external = "";
    my $linecount = 0;

	while( <$ifp> )
	{
		chomp;
        $linecount++;
		next if /^\s*#/;	# skip comments
		next if /^\s*$/;	# skip blank lines

		my( $id, $value ) = get2parts( $_ );

        # --------------------------------------------------
        # magic substitution of a few variables
        # --------------------------------------------------
        1 while $current_service_name && $value =~ s/{{service_name}}/$current_service_name/;
        1 while $current_profile_name && $value =~ s/{{profile}}/$current_profile_name/;
        1 while $current_profile_name && $value =~ s/{{profile_name}}/$current_profile_name/;

		printf "%-30s: %s\n", $id, $value if $verbose > 1;

		my @args = split /\s*[|]\s*/, $value;

		if( "performance_configuration" eq $id )
		{
            die "ERROR: Only ONE 'performance_configuration' declaration allowed"
                if( $current_perfconfig ne "" );

			$current_perfconfig              = $args[0];
			$profile_name                    = $args[0];
			next ;
		}

		if( "service_profile" eq $id )
		{
            die "ERROR: MUST have 'performance_configuration' before 'service_profile'"
                if( $current_perfconfig eq "" );

            $current_service_host           = $profile_name . "_" . $args[0];
			$service_profile_name           = $args[0];
			next;
		}

		if( "graph" eq $id )
		{
            if( $current_service_host  eq "" )
            {
                die "ERROR: MUST have 'service_profile' before 'graph'";
            }
            $current_graph_name                                                       = $args[0];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ name    } = $args[0];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ host    } = $args[1];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ service } = $args[2];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ type    } = $args[3];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ enable  } = $args[4];
			$graph_profile{ $current_service_host }{ $current_graph_name }{ label   } = $args[5];
			next;
		}

        if(( "rrdname"         eq $id )
        || ( "rrdcreatestring" eq $id )
        || ( "rrdupdatestring" eq $id )
        )
        {
            die "ERROR: MUST have a 'graph' before '$id'"
                if( $current_graph_name eq "" );

            $graph_profile{ $current_service_host }{ $current_graph_name }{ $id } = $value;
            next;
        }

        if( "graphcgi" eq $id )
        {
            die "ERROR: MUST have a 'graph' before '$id'"
                if( $current_graph_name eq "" );

            push @{$graph_profile{ $current_service_host }{ $current_graph_name }{ $id }}, $value;
            next;
        }

        if( "parseregx" eq $id )
        {
            die "ERROR: MUST have a 'graph' before '$id'"
                if( $current_graph_name eq "" );

            my $regex = $value;
            $regex =~ s/^[^|]*[|]//;
            $graph_profile{ $current_service_host }{ $current_graph_name }{ $id }{ first } = $args[0];
            $graph_profile{ $current_service_host }{ $current_graph_name }{ $id }{ regex } = $regex;
            next;
        }

        if( "perfidstring" eq $id )
        {
            die "ERROR: MUST have a 'graph' before '$id'"
                if( $current_graph_name eq "" );

            $graph_profile{ $current_service_host }{ $current_graph_name }{ $id } = $args[0];
            next;
        }

        print STDERR "UNKNOWN: '$id' type at line '$linecount'\n";
	}
    print STDERR "Read in $linecount line(s)\n" if $verbose > 0;
}

sub cdata
{
	return '<![CDATA[' . shift() . ']]>';
}

sub generate_output
{
	@xml = ();

    # ----------------------------------------------------------------------
    # OUTPUT ... starts, and does the <profile> section, followed by others.
    # ----------------------------------------------------------------------
	push @xml, '<?xml version="1.0" encoding="iso-8859-1" ?>';
    push @xml, '<!--';
    push @xml, 'Copyright 2011 GroundWork Open Source, Inc.';
    push @xml, 'All rights reserved. Use is subject to GroundWork commercial license terms.';
    push @xml, '-->';

    # ----------------------------------------------------------------------
	push @xml, '<groundwork_performance_configuration>';
    foreach my $spname ( sort keys %graph_profile )  # spname = service_profile_name
    {
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
	push @xml, ' <service_profile name="' . $spname . ' profile">';
    foreach my $graph_name ( sort keys %{$graph_profile{ $spname }} )
    {
    my $x = \%{$graph_profile{ $spname }{ $graph_name }};   # a shortcut reference
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    push @xml, '  <graph name="' . $graph_name . '">';
    push @xml, '   <host>'    .        ${$x}{ host }     . '</host>';
    push @xml, '   <service ' .        ${$x}{ service }  . '>' . cdata( $spname ) . '</service>';
    push @xml, '   <type>'    .        ${$x}{ type }     . '</type>';
    push @xml, '   <enable>'  .        ${$x}{ enable }   . '</enable>';
    push @xml, '   <label>'   .        ${$x}{ label }    . '</label>';
    push @xml, '   <rrdname>' . cdata( ${$x}{ rrdname }) . '</rrdname>';
    push @xml, '   <rrdcreatestring>' . cdata( ${$x}{ rrdcreatestring } ) . '</rrdcreatestring>';
    push @xml, '   <rrdupdatestring>' . cdata( ${$x}{ rrdupdatestring } ) . '</rrdupdatestring>';
    push @xml, '   <graphcgi>' . cdata( join "\n   ", @{${$x}{ graphcgi }} ) . '</graphcgi>' 
        if @{${$x}{ graphcgi }};

    push @xml, '   <parseregx first="' . 
        ${$x}{ parseregx }{ first } . '">'
        . cdata( ${$x}{ parseregx }{ regex } )
        . '</parseregx>';

    push @xml, '   <perfidstring>' . ${$x}{ perfidstring } . '</perfidstring>';
    push @xml, '  </graph>';
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    }
	push @xml, ' </service_profile>';
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    }
	push @xml, '</groundwork_performance_configuration>';
}

sub write_output
{
    my $ofp = STDOUT;

    if( !$dout_file )
    {
        $dout_file = sprintf "perfconfig-%s-new.xml", $profile_name;
    }
    if( ! $streamingmode )
    {
        die "Couldn't make '$dout_file'" unless open OUT, ">", "$dout_file";
        $ofp = OUT;
    }

	print $ofp join "\n", @xml;
	print $ofp "\n";
	close $ofp unless $streamingmode;

    printf STDERR "Wrote out %d XML line(s)\n", scalar( @xml ) if $verbose > 0;
}


# performance_configuration = gdma_21_linux
#  service_profile          = disk
#   graph                   = graph|*|regx="1"|nagios|1|Disk Utilization
#    rrdname                = /usr/local/groundwork/rrd/RHOST$_$SERVICE$.rrd
#    rrdcreatestring        = $RRDTOOL$ create $RRDNAME$ --step 300 --start n-1yr $LISTSTART$DS:$LABEL#$:GAUGE:1800:U:U DS:$LABEL#$_wn:GAUGE:1800:U:U DS:$LABEL#$_cr:GAUGE:1800:U:U DS:$LABEL#$_mx:GAUGE:1800:U:U $LISTEND$ RRA:AVERAGE:0.5:1:8640 RRA:AVERAGE:0.5:12:9480
#    rrdupdatestring        = $RRDTOOL$ update $RRDNAME$ $LASTCHECK$:$VALUE1$:$WARN1$:$CRIT1$:$MAX1$ 2>&1
#    graphcgi               = 'rrdtool graph -
#    graphcgi               = DEF:a="rrd_source":ds_source_0:AVERAGE
#    graphcgi               = DEF:w="rrd_source":ds_source_1:AVERAGE
#    graphcgi               = DEF:c="rrd_source":ds_source_2:AVERAGE
#    graphcgi               = DEF:m="rrd_source":ds_source_3:AVERAGE
#    graphcgi               = CDEF:cdefa=a,m,/,100,*
#    graphcgi               = CDEF:cdefb=a,0.99,*
#    graphcgi               = CDEF:cdefw=w
#    graphcgi               = CDEF:cdefc=c
#    graphcgi               = CDEF:cdefm=m
#    graphcgi               = AREA:a#C35617:"Space Used\: "
#    graphcgi               = LINE:cdefa#FFCC00:
#    graphcgi               = GPRINT:a:LAST:"%.2lf MB\l"
#    graphcgi               = LINE2:cdefw#FFFF00:"Warning Threshold\:"
#    graphcgi               = GPRINT:cdefw:AVERAGE:"%.2lf"
#    graphcgi               = LINE2:cdefc#FF0033:"Critical Threshold\:"
#    graphcgi               = GPRINT:cdefc:AVERAGE:"%.2lf\l"
#    graphcgi               = GPRINT:cdefa:AVERAGE:"Percentage Space Used"=%.2lf
#    graphcgi               = GPRINT:cdefm:AVERAGE:"Maximum Capacity"=%.2lf
#    graphcgi               = CDEF:cdefws=a,cdefw,GT,a,0,IF
#    graphcgi               = AREA:cdefws#FFFF00
#    graphcgi               = CDEF:cdefcs=a,cdefc,GT,a,0,IF
#    graphcgi               = AREA:cdefcs#FF0033
#    graphcgi               = -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y  -l 0'
#    parseregx              = 0|
#    perfidstring           = 

#
# ----------------------------------------------------------------------
# performance_configuration = {name}
#  service_profile          = {name}
#   graph                   = {name}|{host}|{service}|{type}|{enable}|{label}
#    rrdname                = {text}               # full string
#    rrdcreatestring        = {text}               # full command
#    rrdupdatestring        = {text}               # full command
#    graphcgi               = {text}
#    ...                      {text}
#    graphcgi               = {text}               # each line concats to prior
#    parseregx              = {first}|{regx}       # first is 1|0
#    perfidstring           = {text}               # usually empty
# ----------------------------------------------------------------------
