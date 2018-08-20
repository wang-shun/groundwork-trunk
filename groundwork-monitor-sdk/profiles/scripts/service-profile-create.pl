#!/usr/bin/perl

$USAGE = << 'stop';
----------------------------------------------------------------------
- service-profile-create                 to make service profiles    -
----------------------------------------------------------------------
- USAGE: service-profile-create [{FLAGS}] -i {profile}               -
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
- AS IN: -i exch_std.servpro                                         -
-                                                                    -
- WHICH: reads 'exch_std.servpro', and parses out the generator data -
-        within, then generates 'service-profile-exch_std.xml' as the-
-        profile itself from the generator output.  Errors to STDOUT -
----------------------------------------------------------------------
- Copyright (C) 2012 by Groundwork Open Source, Inc.   San Francisco -
----------------------------------------------------------------------
stop

$TEMPLATE = << 'stump';
----------------------------------------------------------------------
- service-profile-create                 TEMPLATE help pages...      -
----------------------------------------------------------------------
- The idea with the templates is that they minimize the repetitious  -
- entry of very similar data (which, being repetitious, often is     -
- prone to errors)                                                   -
-                                                                    -
- The format of [exch_std.profile]:                                  -
-                                                                    -
----------------------------------------------------------------------
# service_profile    = {name}|{description}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  command           = {name}|{type}|{commandline}
#  time_period       = {name}|{alias}               ... internal subroutine expands
#  service_template  = {name}
#  extended_service_info_template = {name}|||{image}|{notes}|{alt_image}
# ----------------------------------------------------------------------

  service_profile    = exch_std|Exchange Standard Checks for All Roles
   service_name      = ad_ldap|gdma|percent_graph|desc-gdma_wmi_cpu
    service_external = service
     service_data    = Enable="ON"
     service_data    = Service="{{profile}}_{{service_name}}"
     service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\{{profile}}.ps1 -warning 50 -critical 100  -waittime 2 ; exit $LASTEXITCODE "
     service_data    = Check_Interval="1"
  
   time_period       = 24x7|Tired and Swamped Old Computer
   service_template  = gdma
   command           = check_gdma_fresh|check|check_dummy 3 $ARG1$
   extended_service_info_template = percent_graph|||services.gif|/graphs/cgi-bin/label_graph.cgi?host=$HOSTNAME$&service=$SERVICENAME$|Service Detail
  
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

&main;

sub main
{
	die $USAGE unless @ARGV;

    print STDERR "service-profile-create.pl    Version $version\n";

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

		if( "service_profile" eq $id )
		{
			if( $current_profile_name ne "" )
			{
				die "ERROR: Only ONE 'service_profile' declaration allowed";
			}
			$current_profile_name   = $args[0];
			$profile_name           = $args[0];
			$profile{ name }        = $args[0];
			$profile{ description } = $args[1];
			next;
		}

		if( "service_name" eq $id )
		{
			$current_service_name                                   = $args[0];  
            $complex_service_name                                   = $current_profile_name . "_" . $args[0];
			$service_name{ $complex_service_name }{ name        }   = $c;
			$service_name{ $complex_service_name }{ template    }   = $args[1];
            $current_service_template                               = $args[1];
			$service_name{ $complex_service_name }{ extinfo     }   = $args[2];
			$service_name{ $complex_service_name }{ description }   = $args[3];
			next;
		}

		if( "service_external" eq $id )
		{
			if( $current_service_name eq "" )
			{
				die "Bare external service... needs to be under a 'service_name' section";
			}
			$current_service_external                              = $args[0];
			$service_external{ $complex_service_name }{ type }     = $args[0];
			next;
		}

		if( "service_data" eq $id )
		{
			if( $complex_service_name eq "" )
			{
				die "Bare external service data... (no service name!)";
			}
			if( $current_service_external eq "" )
			{
				die "Bare external service data... (no external name!)";
			}
            my $c = sprintf "Check_%s_%s_%s[1]_", $current_service_template, $profile_name, $current_service_name; 
			push @{$service_external{ $complex_service_name }{ data }}, $c . $value;
			next;
		}

        if( "command" eq $id )
        {
            $command_template{ name }        = $args[0];
            $command_template{ type }        = $args[1]; 
            $command_template{ commandline } = $args[2]; 
            next;
        }

        if( "extended_service_info_template" eq $id )
        {
            $extended_service_info_template{ name           } = $args[0];
            $extended_service_info_template{ script         } = $args[1];
            $extended_service_info_template{ comment        } = $args[2];
            $extended_service_info_template{ icon_image     } = $args[3];
            $extended_service_info_template{ notes_url      } = $args[4];
            $extended_service_info_template{ icon_image_alt } = $args[5];
            next;
        }

        if( "time_period" eq $id )
        {
            if( $args[0] eq "24x7" )
            {
                $time_template{ comment   } = "All day, every day.";
                $time_template{ name      } = $args[0];
                $time_template{ alias     } = "24 Hours A Day, 7 Days A Week";
                $time_template{ monday    } = "00:00-24:00";
                $time_template{ tuesday   } = "00:00-24:00";
                $time_template{ wednesday } = "00:00-24:00";
                $time_template{ thursday  } = "00:00-24:00";
                $time_template{ friday    } = "00:00-24:00";
                $time_template{ saturday  } = "00:00-24:00";
                $time_template{ sunday    } = "00:00-24:00";
                next;
            }
            else
            {
                printf STDERR "Unknown time-period '%s'\n", $args[0];
            }
        }

        if( "service_template" eq $id )
        {
            if( $args[0] eq "gdma" )
            {
# ----------------------------------------------------------------------
# these parameters were shamelessly lifted from another configuration file.
# I could see a need to do this... with perhpas "types of configurations"
# ----------------------------------------------------------------------
                $service_template{ "retry_check_interval"         } = 1;
                $service_template{ "flap_detection_enabled"       } = 1;
                $service_template{ "check_freshness"              } = 1;
                $service_template{ "event_handler_enabled"        } = 1;
                $service_template{ "notifications_enabled"        } = 1;
                $service_template{ "command_line"                 } = 'check_gdma_fresh!"Stale Status"'; 
                $service_template{ "process_perf_data"            } = 1;
                $service_template{ "active_checks_enabled"        } = 0;
                $service_template{ "check_period"                 } = "24x7";
                $service_template{ "is_volatile"                  } = 0;
                $service_template{ "freshness_threshold"          } = 900;
                $service_template{ "passive_checks_enabled"       } = 1;
                $service_template{ "notification_period"          } = "24x7";
                $service_template{ "max_check_attempts"           } = 1;
                $service_template{ "retain_status_information"    } = 1;
                $service_template{ "notification_options"         } = "u,c,w,r";
                $service_template{ "name"                         } = "gdma";
                $service_template{ "retain_nonstatus_information" } = 1;
                $service_template{ "check_command"                } = "check_gdma_fresh";
                $service_template{ "normal_check_interval"        } = 10;
                $service_template{ "obsess_over_service"          } = 1;
                $service_template{ "notification_interval"        } = 15;
                next;
            }
            else
            {
                printf STDERR "Unknown service_template '%s'\n", $args[0];
            }
        }
		printf STDERR "Unknown ID: '%s' / '%s'\n", $id, $value;

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
	push @xml, '<profile>';
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
	push @xml, ' <service_profile>';
	push @xml, '  <prop name="name">'        . cdata( $profile{ name } )        . '</prop>';
	push @xml, '  <prop name="description">' . cdata( $profile{ description } ) . '</prop>';
	foreach my $service ( sort keys %service_name )
	{
    push @xml, '  <prop name="service">'     . cdata( $service )                . '</prop>';
	}
	push @xml, ' </service_profile>';
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    if( defined %command_template )
    {
        push @xml, ' <command>';
        push @xml, '  <prop name="name">'        . cdata( $command_template{ name } )        . '</prop>';
        push @xml, '  <prop name="type">'        . cdata( $command_template{ type } )        . '</prop>';
        push @xml, '  <prop name="command_line">'. cdata( $command_template{ commandline } ) . '</prop>';
        push @xml, ' </command>';
    }
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    if( defined %time_template )
    {
        push @xml, ' <time_period>';
        foreach my $component ( keys %time_template )
        {
            push @xml, '  <prop name="' . $component . '">' 
                . cdata( $time_template{ $component } ) . '</prop>';
        }
        push @xml, ' </time_period>';
    }
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    if( defined %service_template )
    {
        push @xml, ' <service_template>';
        foreach my $component ( keys %service_template )
        {
            push @xml, '  <prop name="' . $component . '">' 
                . cdata( $service_template{ $component } ) . '</prop>';
        }
        push @xml, ' </service_template>';
    }
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    if( defined %extended_service_info_template )
    {
        push @xml, ' <extended_service_info_template>';
        foreach my $component ( keys %extended_service_info_template )
        {
            push @xml, '  <prop name="' . $component . '">' 
                . cdata( $extended_service_info_template{ $component } ) . '</prop>';
        }
        push @xml, ' </extended_service_info_template>';
    }
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
    foreach my $sname ( keys %service_name )
    {
        push @xml, ' <service_name>';
        foreach my $parts ( keys %{$service_name{ $sname }} )
        {
            push @xml, '  <prop name="' . $parts . '">' 
                . cdata( $service_name{ $sname }{ $parts } ) . '</prop>';
        }
        if( exists $service_external{ $sname } )
        {
            push @xml, '  <prop name="service_external">' 
                . cdata( $sname ) . '</prop>';
        }
        push @xml, ' </service_name>';

        if( exists $service_external{ $sname } )
        {
            push @xml, ' <service_external>';
            push @xml, '  <prop name="name">' . cdata( $sname ) . '</prop>';
            push @xml, '  <prop name="type">' . cdata( $service_external{ $sname }{ type } ) . '</prop>';
            push @xml, '  <prop name="data">'
               . cdata( join( "\n", @{$service_external{ $sname }{ data }} ) ) . '</prop>';

            push @xml, ' </service_external>';
        }
    }
    # ----------------------------------------------------------------------
    # ----------------------------------------------------------------------
	push @xml, '</profile>';
}

sub write_output
{
    my $ofp = STDOUT;

    if( !$dout_file )
    {
        $dout_file = sprintf "service-profile-%s-new.xml", $profile_name;
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



# service_profile    = exch_std|Exchange Standard Checks for All Roles
#  service_name      = ad_ldap|gdma|percent_graph|desc-gdma_wmi_cpu
#   service_external = service
#    service_data    = Enable="ON"
#    service_data    = Service="{{service_profile}}_{{service_name}}"
#    service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\{{service_profile}}.ps1 -warning 50 -critical 100  -waittime 2 ; exit $LASTEXITCODE "
#    service_data    = Check_Interval="1"
#    
# service_profile    = {name}|{description}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  command           = {name}|{type}|{commandline}
#  time_period       = {name}|{alias}               ... internal subroutine expands
#  service_template  = {name}

