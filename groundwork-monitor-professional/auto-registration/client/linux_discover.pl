#!/usr/bin/perl

# ----------------------------------------------------------------------
# NOTE - putting a '*' in the first non-space position hides the line 
#        when output as HELP.  I have LEFT IN the 'documentation' though
#        for use if needed by field-techs for "undocumented but useful"
#        capabilities.
# ----------------------------------------------------------------------

$USAGE = << 'end';
    ----------------------------------------------------------------------
    - autodisplug                Autodiscovery of a variety of parameters-
    -                            that fingerprints a machine.            -
    ----------------------------------------------------------------------
    - USAGE: autodisplug {options}                                       -
    -                                                                    -
    - WHERE: {options}           notes                                   -
    -        -----------------  ------------------------------------------
    -        -all               triggers all of the statistics gathering -
    -                           subroutines.  Comprehensive system stats -
    -                                                                    -
    -        -raw               is -noblocks  plus -noversion            -
    -        -noblocks          suppresses XML blocks                    -
    -        -noversion         suppresses versioning information        -
#   -        -quiet             suppresses output                        -
    -                                                                    -
#   -        -psaux             runs [ps -aux] or equivalent to get      -
#   -                           process list from running machine        -
#   -        -ps                defaults to [-pseo args]                 -
#   -        -pseo "args"       have to encapsulate args...              -
#   -        -interval ###      interval, in seconds, with which to run  -
#   -        -poll     ###                'ditto'                        -
    -        -sethost  {string} sets the override hostname.              -
    -        -setip    {string} sets the override IP address.            -
    -        -setip6   {string} sets the override IP6 address.           -
    -        -pass     {string} sets up output hashes for {strings}      -
    -                                                                    -
#   -        -gdma              sets up a block for the -set... data     -
#   -        -hostname          finds hostname, reports it.              -
#   -        -hosts             brings along /etc/hosts                  -
#   -        -env               captures the shell environment vars      -
#   -        -uname             sends raw [uname -a] data                -
    -        -services          interprets what services in [ps aux]     -
#   -        -network           queries system about net interface caps  -
#   -        -chkconfig         returns results of [chkconfig]           -
#   -        -netstat           returns results of [netstat -ln]         -
#   -        -uptime            returns results of [uptime]              -
#   -        -free              returns results of [free]                -
#   -        -mount             returns results of [mount]               -
#   -        -df                returns results of [df] (parsed!)        -
    -                                                                    -
    -        --version          just print version and exit              -
    -        --help             print this                               -
    ----------------------------------------------------------------------
    - Copyright (C) 2012 by Groundwork Open Systems, Inc.  San Francisco -
    ----------------------------------------------------------------------
end

%optslist = (
# ----------------------------------------------------------------------
#   parameter        -all?, 
# ----------------------------------------------------------------------
    "-all"       => [ 0,       ],   # must be 0 or recursive forever!
    "-chkconfig" => [ 0,       ],   # 120607.rlynch: turned OFF per KStone request
    "-df"        => [ 1,       ],   #
    "-env"       => [ 1,       ],   #
    "-free"      => [ 1,       ],   #
    "-gdma"      => [ 0,       ],   # it happens automatically in code
    "-hostname"  => [ 1,       ],   #
    "-hosts"     => [ 1,       ],   #
    "-interval"  => [ 0,       ],   #
    "-mount"     => [ 1,       ],   #
    "-netstat"   => [ 1,       ],   #
    "-network"   => [ 1,       ],   #
    "-noblocks"  => [ 0,       ],   #
    "-noversion" => [ 0,       ],   #
    "-pass"      => [ 0,       ],   #
    "-ps"        => [ 1,       ],   #
    "-psaux"     => [ 0,       ],   #
    "-pseo"      => [ 0,       ],   #
    "-quiet"     => [ 0,       ],   #
    "-services"  => [ 1,       ],   #
    "-sethost"   => [ 0,       ],   #
    "-setip"     => [ 0,       ],   #
    "-setip6"    => [ 0,       ],   #
    "-badcmd"    => [ 0,       ],   # for REGRESSION testing of an illegal command
    "-uname"     => [ 1,       ],   #
    "-uptime"    => [ 1,       ],   #
);

# ----------------------------------------------------------------------
$NAME        = "autodisplug" ; # name of this application
# ---------------------------- # VERSION HISTORY
$VERSION     = "0.9";          # 120529.rlynch: first write of code
$VERSION     = "0.9.2";        # 120530.rlynch: update with KStone's wishes
$VERSION     = "0.9.3";        # 120601.rlynch: added 'smart exec' engine
$VERSION     = "0.9.3b";       # 120601.rlynch: added [ps -eo] support.
$VERSION     = "0.9.4a";       # 120604.rlynch: ps = ps -eo args, uptime, etc.
$VERSION     = "0.9.4b";       # 120604.rlynch: free, mount, netstat, df+parser
$VERSION     = "0.9.4c";       # 120604.rlynch: chkconfig  added
$VERSION     = "0.9.4d";       # 120604.rlynch: -gdma added.  -setip, -setip6
$VERSION     = "0.9.4e";       # 120604.rlynch: -pass added.  Much better than above
$VERSION     = "0.9.4f";       # 120604.rlynch: non-execute reporting when path-fails
$VERSION     = "0.9.5a";       # 120606.rlynch: shorten HELP, add -env, -pass, netstat -ln
$VERSION     = "0.9.6a";       # 120607.rlynch: elim -chkconfig in -all; create ERROR=reason
                               #                for failed command execution
$VERSION     = "0.9.6b";       # 120607.rlynch: fixup of [-all/--version] issue
$VERSION     = "0.9.7a";       # 120703.rlynch: Lots of SOLARIS patches
$VERSION     = "0.9.8a";       # 120703.rlynch: Fixup of VERSION per GLEN request.

$blocks      = 1;              # blocks turnd on by default
$verbose     = 0;              # a verbose operation/debugging level
$interval    = 5 * 60;         # 5 minutes, by default
$versiondone = 0;              # to ensure 'version' information is first
$outputflag  = 1;              # suppresses output if '0'

$sethost     = "";             # for overriding the autodetection of hostname
$setip       = "";             # for overriding IP address, or setting a default
$setip6      = "";             # for overriding IP6 address...
%passthru    = ();             # for a whole lot of 'em

$hosting_oracle   = 0;         # flags for things found in PS AUX
$hosting_postgres = 0;         # 
$hosting_mysql    = 0;         # 
$hosting_apache   = 0;         # 

$PATHENTRY{ $_ }++ foreach split /:/, $ENV{ "PATH" };  # as a hash... is smarter!
$PATHENTRY{ "/bin"             }++;  # now, add in all the 'well known' loc'ns
$PATHENTRY{ "/usr/bin"         }++;  # 
$PATHENTRY{ "/usr/local/bin"   }++;  # 
$PATHENTRY{ "/sbin"            }++;  # 
$PATHENTRY{ "/usr/sbin"        }++;  # 
$PATHENTRY{ "/usr/local/sbin"  }++;  # 

push @ps_fodder, smartrun( "ps -eo args" );     # needed by the printing routines;
$OS = uc `uname -s`;                            # helpful everwhere; universally OK

$starttime = time;      # always useful

@output = ();           # collects the output strings, in order. 

&main;                  # where things get off to a bang!

# ----------------------------------------------------------------------
# main()                execute the commandline args in order.
#                       all output is buffered to an array.
#                       print the array at the end.
# ----------------------------------------------------------------------
sub main
{
    my $exitvalue = 0;

    for( my $i = 0; $i < @ARGV; $i++ )
    {
        $_ = $ARGV[ $i ];

        if(    /^-int(erval)?$/ )    { $interval          = $ARGV[ ++$i ] ; next; }
        elsif( /^-poll$/        )    { $interval          = $ARGV[ ++$i ] ; next; }
        elsif( /^-sethost$/     )    { &setpass("hostname", $ARGV[ ++$i ]); next; }
        elsif( /^-setip$/       )    { &setpass("ip",       $ARGV[ ++$i ]); next; }
        elsif( /^-setip6$/      )    { &setpass("ip6",      $ARGV[ ++$i ]); next; }
        elsif( /^-gdma$/        )    { &run_gdma(1);                        next; }  #force it
        elsif( /^-noblocks?$/   )    {                      $blocks = 0;    next; }
        elsif( /^-raw$/         )    { $versiondone = 1;    $blocks = 0;    next; }
        elsif( /^-noversion$/   )    { $versiondone = 1;                    next; }
        elsif( /^-psaux$/       )    { &run_psaux;                          next; }
        elsif( /^-pseo$/        )    { &run_pseo($ARGV[ ++$i ]);            next; }
        elsif( /^-pass$/        )    { &setpass( $ARGV[ ++$i ]);            next; }
        elsif( /^-ps$/          )    { &run_ps;                             next; }
        elsif( /^-quiet$/       )    { $outputflag = 0;                     next; }
        elsif( /^-hosts$/       )    { &run_gethosts;                       next; }
        elsif( /^-uname$/       )    { &run_uname;                          next; }
        elsif( /^-services$/    )    { &run_services;                       next; }
        elsif( /^-network$/     )    { &run_network;                        next; }
        elsif( /^-netstat$/     )    { &run_netstat;                        next; }
        elsif( /^-mount$/       )    { &run_mount;                          next; }
        elsif( /^-badcmd$/      )    { &run_badcmd;                         next; }
        elsif( /^-free$/        )    { &run_free;                           next; }
        elsif( /^-env$/         )    { &run_env;                            next; }
        elsif( /^-df$/          )    { &run_df;                             next; }
        elsif( /^-chkconfig$/   )    { &run_chkconfig;                      next; }
        elsif( /^-uptime$/      )    { &run_uptime;                         next; }
        elsif( /^-hostname$/    )    { &run_gethostname;                    next; }
        elsif( /^--?help$/      )    { exit( &usage( 0 ) );                       }
        elsif( /^--?version$/   )    { print $VERSION; exit(0);                   }
        elsif( /^-all$/         )
        {
            my @dolist = ();    # splice is a beast. Can't be used here
            foreach my $option ( sort keys %optslist )
            {
                push @dolist, $option if $optslist{ $option }[ 0 ];
            }
            splice @ARGV, $i, 1, @dolist;

            $i--;   # now that we "replaced" -all with all the [-{option}] list
        }
        else
        {
            &usage( 0 ) unless $exitvalue;          # get only one copy
            warn "Don't know cmd option '$_'\n";
            $exitvalue = 1;
        }
    }
    &run_gdma(0);  # don't force.

    print join "", @output if $outputflag;

    if( !@output || !@ARGV )
    {
        print "instructions?  Use '--help'!\n";
    }

    exit( $exitvalue );
}

# ----------------------------------------------------------------------
# usage( comprehensive_flag )
#                            outputs the usage block (or more)
# ----------------------------------------------------------------------
sub usage
{
    my $comprehensive = shift;

    my @lines = split /[\n\r]+/, $USAGE;
    my @out = ();

    foreach my $line ( @lines )
    {
        next if !$comprehensive && $line =~ /^#/;
        $line =~ s/^#/ /;
        push @out, "$line\n";
    }
    print join "", @out;
}

# ----------------------------------------------------------------------
# encapsulate( sectionname, data, ... )
#                            wraps the [data] in an XML like section
# ----------------------------------------------------------------------
sub encapsulate
{
    if( ! $versiondone )
    {
        $versiondone = 1;   # must be set first! (otherwise infinite recursion)
        encapsulate( "autodisplug", "version=$VERSION\n" );
    }

    my $capsule = shift;

    $capsule =~ s/^\s+//;
    $capsule =~ s/\s+$//;

    push @output, "<$capsule>\n" if $blocks;
    foreach my $line ( @_ )
    {
        $line =~ s/\s+$//;
        push @output, "$line\n";
    }
    push @output, "</$capsule>\n" if $blocks;
    push @output, "\n"            if $blocks;   # extra, for "pretty view"
}

# ----------------------------------------------------------------------
# smartrun                   run commands even if not on $PATHENTRY
# ----------------------------------------------------------------------
sub smartrun
{
    my $cmd = shift;           # assumes command is in a single string
    my $arg0 = $cmd;
    my $rest = "";

    $arg0 =~ s/^(\S+)(\s.*)/$1/;    # assumes there can NOT be space in cmdstring
    $rest = $2;

    if( $arg0 =~ m/^(\/|\S+\/)/ )   # if the cmd looks to have a path in it...
    {
        if( ! -x $arg0 ) { return "ERROR=couldn't find/execute command '$cmd'\n"; }
        else             { return `$cmd`; }
    }
    else
    {
        my $firstpath = "";
        my $buildpath = "";     # to show paths looked at; good for debugging;

        foreach my $p ( keys %PATHENTRY )
        {
            $p =~ s/(.)\/$/$1/;
            $buildpath .= ":" if $buildpath;
            $buildpath .= $p;

            $firstpath = "$p/$arg0" if -x "$p/$arg0";
            last if $firstpath;
        }
        if( $firstpath )
        {
            my $newcmd = "$firstpath $rest";
            return `$newcmd`;
        }
        else
        {
            return "ERROR=cmd '$arg0' not found on paths: $buildpath\n";
        }
    }
}

sub setpass
{
    my $name = shift;   # either "name" or "name=data" form
    my $data = shift;

    if( $name =~ /^([^=]+)=(.*)/ )  # this detects difference
    {
        $name = $1;
        $data = $2;
    }
    $name =~ s/^\s+//;  # trim for good behavior
    $name =~ s/\s+$//;  #

    $sethost = $data if $name =~ /^hostname/;
    $setip   = $data if $name =~ /^ip(addr)?$/;
    $setip6  = $data if $name =~ /^ip(6addr|addr6)$/;

    $passthru{ $name } = $data;
}

# ----------------------------------------------------------------------
# run_gdma()                 a smart block.  Aggregates hostname, ip, ip6
# ----------------------------------------------------------------------
sub run_gdma
{
    my $force = shift;
    my @fodder = ();

    if( $force || (( !$done_gdma ) && ( keys %passthru )))
    {
        push @fodder, "$_=" . $passthru{ $_ } foreach keys %passthru;
        encapsulate( "gdma", @fodder );
        $done_gdma = 1;
    }
}

# ----------------------------------------------------------------------
# run_ps()                   queues @ps_fodder for output
# ----------------------------------------------------------------------
sub run_ps
{
    encapsulate( "ps", @ps_fodder );    # whatever the default is...
}

sub run_psaux
{
    encapsulate( "psaux", smartrun( "ps aux" ) );
}

sub run_pseo
{
    my $arg = shift;

    encapsulate( "pseo", smartrun( "ps -eo $arg" ) );
}


# ----------------------------------------------------------------------
# run_services()             inspects @ps_fodder to figure out what's running
# ----------------------------------------------------------------------
sub run_services
{
    my @fodder = ();

    my $linecount = 0;

    foreach my $line ( @ps_fodder )
    {
        next unless $linecount++;   # skip header
        my $working = $line;

#       if( $working =~ s/[^:]+:\d+\s+(.*)/$1/ )  # for [ps -aux]
        if( $working =~ m/./                   )  # for [ps -eo arg], no parse!
        {
            $hosting_postgres++ if $working =~ /^\S*postgres/i;
            $hosting_mysql++    if $working =~ /^\S*mysql/i;
            $hosting_oracle++   if $working =~ /^\S*oracle/i;
            $hosting_apache++   if $working =~ /classpath\s+\S+\/apache/i;
        }
        else
        {
            print STDERR "run_ps: unrecognized ps line: '$working'\n";
        }

    }
    push @fodder, sprintf "postgres=%d\n", $hosting_postgres;
    push @fodder, sprintf "mysql=%d\n",    $hosting_mysql;
    push @fodder, sprintf "oracle=%d\n",   $hosting_oracle;
    push @fodder, sprintf "apache=%d\n",   $hosting_apache;

    encapsulate( "services", @fodder );
}

# ----------------------------------------------------------------------
# run_chkconfig()            runs [chkconfig] for more info.
# ----------------------------------------------------------------------
sub run_chkconfig
{
    encapsulate( "chkconfig", smartrun( "chkconfig" ) );
}

# ----------------------------------------------------------------------
# run_uname()                runs [uname -a] for more info.
# ----------------------------------------------------------------------
sub run_uname
{
    encapsulate( "uname", smartrun( "uname -a" ) );
}

# ----------------------------------------------------------------------
# run_gethostname()          runs [uname] to get info.  queues it for output
# ----------------------------------------------------------------------
sub run_gethostname
{
    my @fodder = ();

    push @fodder, sprintf "osfamily=%s\n",    smartrun( "uname -s" );
    push @fodder, sprintf "machinetype=%s\n", smartrun( "uname -m" );
    push @fodder, sprintf "hostname=%s\n",    $sethost ne "" ? $sethost : `/bin/hostname`;

    encapsulate( "hostname", @fodder );
}

# ----------------------------------------------------------------------
# run_messages()             grabs /var/log/messages
# ----------------------------------------------------------------------
sub run_messages
{
    encapsulate( "messages", `cat /var/log/messages` );
}

# ----------------------------------------------------------------------
# run_gethosts()             grabs /etc/hosts.  queues for output.
# ----------------------------------------------------------------------
sub run_gethosts
{
    encapsulate( "hosts", `cat /etc/hosts` );
}

# ----------------------------------------------------------------------
# run_netstat()              runs [netstat -ln] or a variant             
# ----------------------------------------------------------------------
sub run_netstat
{
    encapsulate( "netstat", 
            ( $OS =~ /linux/i ) ? smartrun( "netstat -ln" ) :
            ( $OS =~ /sunos/i ) ? smartrun( "netstat -a"  ) :
            ( $OS =~ /aix/i   ) ? smartrun( "netstat -r"  ) :
                                  smartrun( "netstat -r"  )
    );
}

# ----------------------------------------------------------------------
# run_mount()                runs [mount] 
# ----------------------------------------------------------------------
sub run_mount
{
    encapsulate( "mount", smartrun( "mount" ) );
}

# ----------------------------------------------------------------------
# run_badcmd()               runs [badcmd]
# 
#                            this is an INTENTIONAL regression-test case
#                            to show how an unreachable program will
#                            affect the output.
# ----------------------------------------------------------------------
sub run_badcmd
{
    encapsulate( "badcmd", smartrun( "badcmd" ) );
}

# ----------------------------------------------------------------------
# run_free()                 runs [free] 
# ----------------------------------------------------------------------
sub run_free
{
    encapsulate( "free", 
        ( $OS =~ /linux/i ) ? smartrun( "free" ) :
        ( $OS =~ /sunos/i ) ? smartrun( "prtdiag -a" ) :
        ( $OS =~ /aix/i   ) ? smartrun( "free" ) :
                              smartrun( "free" )
    );
}

# ----------------------------------------------------------------------
# run_df()                   runs [df] 
# ----------------------------------------------------------------------
# This has a parser to "re-join" lines that DF for some ridiculous reason
# insists on splitting apart.  OK for humans, terrible for computer 
# programs.  This rejoining makes it easier on the receiving end.
# ----------------------------------------------------------------------
sub run_df
{
    my @fodder = ();
    my $buildup = "";

    foreach my $line ( smartrun( "df -k" ) )
    {
        chomp $line;
        next if $line =~ /^\s*Filesystem.*Avail.*Moun/i;  # if is a header

        if ($line !~ /\d+\s+\d+\s+\d+/ )   # if NOT a statistics-bearing line
        {
            $buildup = $line;
            next;
        }
        if ($line =~ /^\s{5,}.*\d+\s+\d+\s+\d+/)  # if a follow-on line
        {
            push @fodder, $buildup . $line;
            $buildup = "";
            next;
        }

        # must now be a 'regular line'
        push @fodder, $buildup if $buildup; # don't put if empty
        $buildup = "";                      # but make empty
        push @fodder, $line;                # and send off the line
    }
    encapsulate( "df", @fodder );
}


# ----------------------------------------------------------------------
# run_uptime()               runs [uptime]                              
# ----------------------------------------------------------------------
sub run_uptime
{
    encapsulate( "uptime", smartrun( "uptime" ) );
}

# ----------------------------------------------------------------------
# run_env()                  gets the environment variables posted.     
# ----------------------------------------------------------------------
sub run_env
{
    my @fodder = ();

    foreach ( sort keys %ENV )
    {
        next if( /[\n\r]/s );
        push @fodder, "${_}=" . $ENV{ $_ };
    }

    encapsulate( "env", @fodder );
}


# ----------------------------------------------------------------------
# run_gethosts()             grabs /etc/hosts.  queues for output.
# ----------------------------------------------------------------------
sub run_network
{
    my @fodder   = ();
    my $ifname   = "";
    my $ipaddr   = "";
    my $ipmask   = "";
    my $inet6    = "";
    my $macaddr  = "";

    my @ifconfig = smartrun( 
        ( $OS =~ /linux/i ) ? "ifconfig -a" :     # all same now, but may/could be 
        ( $OS =~ /sunos/i ) ? "ifconfig -a" :     # different later.
        ( $OS =~ /aix/i   ) ? "ifconfig -a" :     # 
                              "ifconfig -a"   );  # 

    sub run_network_flush
    {
        if( $ifname ne "" ) # flush prior pending
        {
            push @fodder, sprintf "%s:ipaddr=%s",  $ifname, $ipaddr;  $ipaddr   = "";
            push @fodder, sprintf "%s:ipaddr6=%s", $ifname, $inet6;   $inet6    = "";
            push @fodder, sprintf "%s:ipmask=%s",  $ifname, $ipmask;  $ipmask   = "";
            push @fodder, sprintf "%s:macaddr=%s", $ifname, $macaddr; $macaddr  = "";
        }
        $ifname = "";
    }

    foreach my $line ( @ifconfig )
    {
        if( ( $OS =~ /linux/i ) ? $line =~ m/^(\S+)\s+link/i : 
            ( $OS =~ /sunos/i ) ? $line =~ m/^([^: ]+):/i :
            ( $OS =~ /aix/i   ) ? $line =~ m/^(\S+)\s+link/i : 
                                  $line =~ m/^(\S+)\s+link/i 
        )
        {
            my $holdit = $1;    #protection since $1 isn't well conserved
            &run_network_flush;
            $ifname = $holdit;
        }
        $ipaddr  = $1 if $OS =~ /linux/i && $line =~ /inet\s+addr:\s*(\S+)/i;
        $ipaddr  = $1 if $OS =~ /sunos/i && $line =~ /inet\s(\S+)/i;
        $ipaddr  = $1 if $OS =~ /aix/i   && $line =~ /inet\s(\S+)/i;

        $ipmask  = $1 if $OS =~ /linux/i && $line =~ /inet.*mask:\s*(\S+)/i;
        $ipmask  = $1 if $OS =~ /sunos/i && $line =~ /inet.*netmask\s*(\S+)/i;
        $ipmask  = $1 if $OS =~ /aix/i   && $line =~ /inet.*netmask\s*(\S+)/i;

        $inet6   = $1 if $OS =~ /linux/i && $line =~ /inet6.*addr:\s*(\S+)/i;
        $inet6   = $1 if $OS =~ /sunos/i && $line =~ /inet6.*addr:\s*(\S+)/i;
        $inet6   = $1 if $OS =~ /aix/i   && $line =~ /inet6.*addr:\s*(\S+)/i;

        $macaddr = $1 if $OS =~ /linux/i && $line =~ /hwaddr\s+(\S+)/i;
        $macaddr = $1 if $OS =~ /sunos/i && $line =~ /hwaddr\s+(\S+)/i;
        $macaddr = $1 if $OS =~ /aix/i   && $line =~ /hwaddr\s+(\S+)/i;
    }
    &run_network_flush;

    encapsulate( "network", @fodder );
}

