

use strict;
use Getopt::Long;

use vars qw( %ERRORS $VER $PROGNAME $CLUSTERCMD $DEBUG $HELP $VERSION $TIMEOUT $CLUSTERNAME );

# ----------------------------------------------------------------------------------
sub initialize
{
    $PROGNAME = "query_cluster";
    $CLUSTERCMD = "cluster";
    $VER = "1.0";
    %ERRORS=('OK'=>0,'WARNING'=>1,'CRITICAL'=>2,'UNKNOWN'=>3,'DEPENDENT'=>4);

    # Just in case of problems, let's not hang things
    $SIG{'ALRM'} = sub {
         print ("Plugin $PROGNAME error: operation timed out (alarm)\n");
         exit $ERRORS{"UNKNOWN"};
    };
    alarm($TIMEOUT);
}

# ----------------------------------------------------------------------------------
sub process_cli
{

    # process command line options

    GetOptions (
                 "cluster=s"      => \$CLUSTERNAME,
                 "debug"          => \$DEBUG,
                 "help"           => \$HELP,
                 "version"        => \$VERSION,
               );
       
    if ($HELP)     { show_help(); exit $ERRORS{'OK'} ; }
    if ($VERSION)  { print ("$PROGNAME Version: $VER\n"); exit $ERRORS{'OK'}; }

    if ( not $CLUSTERNAME )
    {
       print "Specify cluster name - see -h for help and options\n";
       exit $ERRORS{'UNKNOWN'};
    }
    chomp $CLUSTERNAME;

}
# ----------------------------------------------------------------------------------

# -------------------------------------------------------------------------------------------------
sub show_help
{
 
   # show help

   print <<HELP;


NAME
      $PROGNAME (version $VER)

SYNOPSIS
      $PROGNAME -cluster <clustername>  [optional options]

DESCRIPTION
      $PROGNAME attempts to
      This check will monitor and detect cluster resources that are not in an "online" state. 
      This check will retrieve all resources for a given cluster, regardless of which resource
      group they reside in, and produce a critical notification if one or more of those resources
      are not online.

     Options

      -cluster <clustername>   Specify cluster name to check resource states for

      -debug                   Produces debugging output

      -help                    Produces this usage/help message

      -version                 Show $PROGNAME version

NOTES

HELP
}

# -------------------------------------------------------------------------------------------------
sub check_cluster_resources
{
   my ( $command_node, @output_node, @nodes, $line_node, $node, $id, $status,
        $command_group, $output_group, $group, $status_group, @groups, @output_group,
        $command_resource, @output_resource, $line_resource, $status_resource, $resource, @not_online,
      );

   # prob'y want to check that the cluster command exists and is executable here - tbd

   $command_node = "$CLUSTERCMD /CLUSTER:$CLUSTERNAME NODE";
   @output_node = `$command_node`;

  print "\n\nCluster command results:\n @output_node\n\n" if defined $DEBUG;

  shift(@output_node);
  shift(@output_node);
  shift(@output_node);
  shift(@output_node);

  foreach my $line_node(@output_node) {
	chomp($line_node);
	($node,$id,$status) = split(/\s+/,$line_node);
	push(@nodes,$node);
	print "$node,$id,$status\n" if defined $DEBUG;
  }


   if ( $#nodes < 0 )
   {
       print "CRITICAL - no nodes found for cluster $CLUSTERNAME\n";
       exit $ERRORS{"CRITICAL"};
   }

   #print @nodes;
   print "\n\n" if defined $DEBUG;


   $command_group = "$CLUSTERCMD /CLUSTER:$CLUSTERNAME GROUP";
   @output_group = `$command_group`;

  print "\n\nCluster command results:\n @output_group\n\n" if defined $DEBUG;
  shift(@output_group);
  shift(@output_group);
  shift(@output_group);
  shift(@output_group);

   foreach $node(@nodes)
   {
	  foreach my $line_group(@output_group) {
	 	chomp($line_group);
	              if ($line_group =~ /$node/)
              	{
	                  $line_group =~ s/  //g;
              	    ($group,$status_group) = split(/$node/,$line_group);
	                  print "$node,$group,$status_group\n" if defined $DEBUG;
              	    push(@groups,$group);
	              }
	  }


       
    }

    if ( $#groups < 0 )
    {
        print "CRITICAL - no groups found for cluster $CLUSTERNAME\n";
        exit $ERRORS{"CRITICAL"};
    }

    #print @groups;
    print "\n\n" if defined $DEBUG;

    $command_resource = "$CLUSTERCMD /CLUSTER:$CLUSTERNAME RESOURCE";
    @output_resource = `$command_resource`;

  print "\n\nCluster command results:\n @output_resource\n\n" if defined $DEBUG;

  shift(@output_resource);
  shift(@output_resource);
  shift(@output_resource);
  shift(@output_resource);

            foreach my $line_resource(@output_resource)
            {
                chomp($line_resource);
	  push( @not_online, $line_resource ) if ( $line_resource !~ /online/i );
	}

    if ( $#not_online >=0 )
    {
        print "CRITICAL - cluster $CLUSTERNAME resource(s) are not online : @not_online\n";
        exit 2;
    }
    else    
    {
        print "OK - cluster $CLUSTERNAME resources are online";
        exit 0;
    }

}

# ----------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------
MAIN:
{

    initialize();
    process_cli();
    check_cluster_resources();

}

__END__



# ------ end script ------

