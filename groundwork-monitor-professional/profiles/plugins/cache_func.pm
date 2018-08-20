#!/usr/local/groundwork/perl/bin/perl -w

package cache_func;

use strict;
use IO::File ();
use Storable ();

use constant CACHE_DIR => '/usr/local/groundwork/nagios/store';

################################################################################
# new - builds new cache object                                                #
#       expects a parse_func object to be passed                               #
#       returns blessed cache_func object                                      #
################################################################################
sub new {
   my $class = shift;

   # we must have a hostname argument
   my $host = shift or do {
      print "ERROR - cache_func->new missing 'host'";
      exit 3;
   };

   # we must have been called by a plugin with the constant PLUGIN defined
   my $plugin = eval { &main::PLUGIN } or do {
      print "ERROR - cache_func->new missing 'main::PLUGIN'";
      exit 3;
   };

   # retrieve calling function name
   my $sub = [ caller(1) ]->[3] or do {
      print "ERROR - cache_func->new missing 'sub'";
      exit 3;
   };

   # define directory structure variables
   my $hostdir   = sprintf "%s/%s", CACHE_DIR, $host;
   my $plugindir = sprintf "%s/%s/%s", CACHE_DIR, $host, $plugin;
   my $subdir    = sprintf "%s/%s/%s/%s", CACHE_DIR, $host, $plugin, $sub;
  
   # CACHE_DIR directory must already exist 
   -d CACHE_DIR or mkdir CACHE_DIR or do {
      print "ERROR - cache_func->new unable to create CACHE_DIR directory";
      exit 3;
   };

   # create host directory if it doesnt exist
   -d $hostdir or mkdir $hostdir or do {
      print "ERROR - cache_func->new unable to create HOST directory";
      exit 3;
   };

   # create plugin directory if it doesnt exist
   -d $plugindir or mkdir $plugindir or do {
      print "ERROR - cache_func->new unable to create PLUGIN directory";
      exit 3;
   };

   # create subroutine directory if it doesnt exist
   -d $subdir or mkdir $subdir or do {
      print "ERROR - cache_func->new unable to create SUBROUTINE directory";
      exit 3;
   };

   # bless and return object
   bless(my $self = \$subdir, $class);
   return $self;
}


################################################################################
# set - set cache                                                              #
#       expects a key and value to be passed; value may be scalar or reference #
#       returns nothing; dies on error                                         #
################################################################################
sub set {
   my ($self, $key, $value) = @_;
 
   # you must pass a key
   defined($key) or do {
      print "ERROR - cache_func->set missing 'key'";
      exit 3;
   }; 

   # you must pass a value; can be scalar or reference
   defined($value) or do {
      print "ERROR - cache_func->set missing 'value'";
      exit 3;
   }; 

   # define and open temporary file for writing
   my $file = sprintf "%s/%s", $$self, $key;
   my $fh = IO::File->new( "$file.tmp", 'w' ) or do {
      print "ERROR - cache_func->set $! on $file";
      exit 3;
   };

   # using eval, attempt to write data using storable
   eval {
      print $fh (ref $value ? Storable::nfreeze $value : 
                              Storable::nfreeze \$value );
   };

   # check for exceptions returned from eval
   if ($@) {
      # exception found
      print "ERROR - cache_func->set unable to freeze data";
      unlink "$file.tmp";
      exit 3;
   }
   else {
      unlink $file;
      rename "$file.tmp", $file;
   }
}


################################################################################
# get - get cache                                                              #
#       expects a key to be passed                                             #
#       returns scalar value or reference to array/hash                        #
################################################################################
sub get {
   my $self = shift;
   my $key  = shift;

   # a key must be passed
   defined($key) or do {
      print "ERROR - cache_func->get missing 'key'";
      exit 3;
   };

   # define and open cache file
   my $file = sprintf "%s/%s", $$self, $key;
   my $fh = IO::File->new( $file, 'r' );
  
   # check whether the file open was successful 
   if ($fh) {
      # using eval, retrieve data using storable
      my $thaw = eval { local $/=undef; Storable::thaw <$fh> };
      
      # check for error during thaw
      if ($@) {
         # eval caught an error during thaw
         # since its useless, delete it
         close $fh;
         unlink $file;
         return undef;
      }
      else {
         # return value or reference
         return (ref $thaw eq 'SCALAR' ? $$thaw : $thaw);
      }
   }
   else {
      # nothing to return
      return undef;
   }
}


return 1;
