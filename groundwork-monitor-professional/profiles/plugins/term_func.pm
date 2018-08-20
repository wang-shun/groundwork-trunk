#!/usr/local/groundwork/perl/bin/perl -w
$|=1;

package term_func;

use strict;
use POSIX q(:termios_h);


sub readkbd {
   my $obfuscate = shift;   #  binary

   # instantiate variables
   my $str = my $key = '';
   my $stdin = fileno STDIN;

   # instantiate termios object
   my $term   = POSIX::Termios->new;

   # load current settings from stdin filehandle
   $term->getattr( $stdin );

   # save the default settings to restore later
   my $default  = $term->getlflag;

   # define arguments for disabling input echoing
   my $noecho = ECHO | ECHOK | ICANON;

   # set terminal to no echo and key input mode
   $term->setlflag( $default & ~$noecho );
   $term->setcc( VTIME, 0 );
   $term->setcc( VMIN, 1 );
   $term->setattr( 0, TCSANOW );

   # retrieve interactive keyboard input
   for (;;) {
      # read in a single character into $key
      sysread( STDIN, $key, 1 );

      # if the keypress is a carriage-return then quit
      $key eq "\n" and print $key and last;
     
      # check for backspace characters 
      if ($key eq "\x7f" || $key eq "\x08") {
         chop $str or next;
         print "\b \b";
         next;
      }
     
      # should be obfuscate the terminal output
      print $obfuscate ? '*' : $key;

      # append key onto string
      $str .= $key
   }

   # restore terminal to defaults
   $term->setlflag( $default );  
   $term->setattr( 0, TCSANOW );

   # return the captured string
   return $str;
}


return 1;
