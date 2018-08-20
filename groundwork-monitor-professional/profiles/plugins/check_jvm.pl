#!/usr/local/groundwork/perl/bin/perl -w
# check_jvm.pl
# Nagios Plugin that checks the tomcat Global Request Processor useage with a JMX call
# 
#
# Requirements:
# Needs the Tomcat Nagios Plugin Shell by GroundWork Open Source. Designed to run on a GroundWork server 
# either open source (with Java installed) or professional. 
# Having jconsole (part of the java sdk) installed is a good idea, just so you can see the context of what
# you are setting up to monitor.
# 
#
# usage:
#    check_jvm.pl -H <hostaddress> -p <jmx port> -u <optional:username> -P <optional:password>
#	 -d <optional:dump> -a <mbean attribute> -v <optional verbose> -w <warning value> -c <critical value>
#
#
# Copyright (c) 2000-2006 GroundWork Open Source
# Author Thomas Stocking (tstocking@groundworkopensource.com)
# 
# This plugin is FREE SOFTWARE. No warrenty of any kind is implied or granted. 
# You may use this software under the terms of the GNU General Public License only.
# See http://www.gnu.org/copyleft/gpl.html and usage sections of this code.
#
# Changelog: 
# 12/21/2006 - original version 0.1
# 2/13/2007 - fixed error handling when the specified mbean name was not found
# October 17 2008 - updated PATH environment variable to reference current JVM


#
use strict;	
use lib "/usr/local/groundwork/nagios/libexec";
require 'utils.pm';
# Set up environment

$ENV{'ENV'} = "";
$ENV{'PATH'} = "/usr/local/groundwork/common/lib:/bin:/usr/bin:/usr/local/groundwork/java/bin";

# Initialize Variables
my @vals;
my @lines = undef;
my $warn = -1;
my $crit = -1;
my $debug = 0;
my $state = "Unknown - Check did not return a value";
my $worst =3;
my $opt_H;
my $opt_D;
my $opt_V;
my $opt_p;
my $opt_u;
my $opt_pass; 
my $opt_d; 
my $opt_m;
my $opt_a; 
my $opt_v; 
my $opt_w; 
my $opt_c;
my $opt_h;
my $value = "null";
my $lo_warn;
my $hi_warn;
my $lo_crit;
my $hi_crit;
my $mbean;
my $attribute;

# Read the command line options

use Getopt::Long;
use vars qw($opt_H $opt_D $opt_V $opt_p $opt_u $opt_Pass $opt_d $opt_a $opt_v $opt_w $opt_c);
use vars qw($PROGNAME);
use utils qw($TIMEOUT %ERRORS &print_revision &support &usage);

sub print_help ();
sub print_usage ();

$PROGNAME = "check_jvm";

Getopt::Long::Configure('bundling');
my $status = GetOptions
        ("H=s"   => \$opt_H, "Hostname=s"         => \$opt_H,
         "p=s" => \$opt_p, "port=s"  => \$opt_p,
         "u=s" => \$opt_u, "user=s"  => \$opt_u,
         "P=s" => \$opt_pass, "pass=s"  => \$opt_pass,
         "d" => \$opt_d, "dump"  => \$opt_d,
	 "m=s" => \$opt_m, "mbean=s"  => \$opt_m,
         "a=s" => \$opt_a, "attribute=s"  => \$opt_a,
         "w=s" => \$opt_w, "warning=s"  => \$opt_w,
         "c=s" => \$opt_c, "crittcal=s"  => \$opt_c,
         "D"   => \$opt_D, "debug"            => \$opt_D,
         "h"   => \$opt_h, "help"            => \$opt_h,
	 "V"   => \$opt_V, "version"	=> \$opt_V);

if ($status == 0)
{
        print_usage() ;
        exit $ERRORS{'OK'};
}

if ($opt_V) {
        print_revision($PROGNAME,'$Revision: 0.1 $'); #'
        exit $ERRORS{'OK'};
}
if ($opt_D) {$debug = 1;}
if ($opt_h) {print_help(); exit $ERRORS{'OK'};}

# Options checking
# Warning
if ($opt_w) {
	if ($opt_w =~ /:/){
		@vals = split /:/, $opt_w;
		($vals[0]) || usage("Invalid value: low warning: $opt_w\n");
		($vals[1]) || usage("Invalid value: high warning: $opt_w\n");
		$lo_warn = $vals[0] if ($vals[0] =~ /^[0-9]+$/);
		$hi_warn = $vals[1] if ($vals[1] =~ /^[0-9]+$/);
		($lo_warn) || usage("Invalid value: low warning: $opt_w\n");
		($hi_warn) || usage("Invalid value: high warning: $opt_w\n");
	} else {
		$lo_warn = undef;
		$hi_warn = $opt_w if ($opt_w =~ /^[0-9]+$/);
        ($hi_warn) || usage("Invalid value: warning: $opt_w\n");
	}
} else { print "No warning level defined\n" if $debug }

# Critical
if ($opt_c) {
    if ($opt_c =~ /:/){
        @vals = split /:/, $opt_c;
        ($vals[0]) || usage("Invalid value: low critical: $opt_c\n");
        ($vals[1]) || usage("Invalid value: high critical: $opt_c\n");
        $lo_crit = $vals[0] if (($vals[0] =~ /^[0-9]+$/) && ($vals[0] < $lo_warn));
        $hi_crit = $vals[1] if (($vals[1] =~ /^[0-9]+$/) && ($vals[1] > $hi_warn));
        ($lo_crit) || usage("Invalid value: low critical: $opt_c\n");
        ($hi_crit) || usage("Invalid value: high critical: $opt_c\n");
    } else {
        $lo_crit = undef;
        $hi_crit = $opt_c if (($opt_c =~ /^[0-9]+$/)&& ($opt_c > $hi_warn));
        ($hi_crit) || usage("Invalid value: critical: $opt_c\n");
    }
} else { print "No critical level defined\n" if $debug }

# Get/set host to check 
if (!$opt_H){$opt_H = "localhost";}

# Get/set JMX port
if (!$opt_p){$opt_p = "8004";}

# Mbean name
if (!$opt_m){
        print "Invalid mbean name supplied\n";
	exit(3);
        } else {
        chomp ($opt_m);
        $mbean = $opt_m;
}

# Mbean Attributes
if (!$opt_a){
        $attribute = "!";
        } else {
        chomp ($opt_a);
	$attribute = $opt_a;
}

# Make sure we can find a java executable
my $java=`/usr/bin/which java 2>&1`;
if ($java =~ /no java in/) {
	print "No java executable in PATH environment variable\n";
	exit(3);
}

#  Call the java tests according to auth method
# No auth
if ($opt_u && $opt_pass) {
# Password Auth
	&call_jmx_pw ($opt_H,$opt_p,$attribute,$opt_u,$opt_pass,$mbean);
	} else {
# No auth
	&call_jmx ($opt_H,$opt_p,$attribute,$mbean); 
# SSL Auth - TO DO
}

# Exit Conditions (or not)
# If all we had to do was print, just exit

if ($attribute =~ "!") {exit(0);}

# If no warning or critical values were supplied, print result and exit OK
if (!($value =~ /null/)) {
	$worst = $ERRORS{'OK'};
	$state = "OK: $attribute is $value";
	$state .= "|$attribute=$value";
} else {	# we did not get a value - print out a message and exit critical
        $worst = $ERRORS{'CRITICAL'};
        $state = "CRITICAL: Query for $attribute did not return a value";
	print "$state\n";
	exit $worst;
}


# Otherwise, compare to thresholds if they exist 	 

if ($lo_crit && $value < $lo_crit) {
	$worst = $ERRORS{'CRITICAL'};
	$state = "CRITICAL: Too Low: $attribute is $value|$attribute=$value;$lo_warn;$lo_crit";
} else {
	if ($hi_crit && $value > $hi_crit) {
    		$worst = $ERRORS{'CRITICAL'};
    		$state = "CRITICAL: Too High: $attribute is $value|$attribute=$value;$hi_warn;$hi_crit";
	} else {
		if ($lo_warn && $value < $lo_warn) {
    			$worst = $ERRORS{'WARNING'};
    			$state = "WARNING: Low: $attribute is $value|$attribute=$value;$lo_warn;$lo_crit";
		} else {
			if ($hi_warn && $value > $hi_warn) {
    				$worst = $ERRORS{'WARNING'};
    				$state = "WARNING: High: $attribute is $value|$attribute=$value;$hi_warn;$hi_crit" ;
			} else {
				$worst = $ERRORS{'OK'};
				$state = "OK: $attribute is $value";
                                if ($hi_warn && $hi_crit) {
                                        $state .= "|$attribute=$value;$hi_warn;$hi_crit";
                                } else {
                                        $state .= "|$attribute=$value";
                                }
			}
		}
	}
}
print "$state\n";
exit $worst;

# Subroutines

sub call_jmx  {
        my $server = $_[0];
        my $port = $_[1];
        my $attribute = $_[2];
	my $mbean = $_[3];
        my @stuff;
        my $line;
	my $i;
        my @res= `java -classpath /usr/local/groundwork/nagios/libexec/nagtomcat.jar com.groundworkopensource.tomcat.nagios.plugin.Shell -s $server -p $port -m "$mbean" -a "$attribute" 2>&1`;
# Look for an error where the JVM needs a unmame and password
	 foreach $line  (@res) {
		if ($line =~ /username\/password/){
			print "$line \n";
			exit(3);
		}
	}	

# Decide what to do with results - just print, or do we test against a value?
        if ($attribute =~ "!") {
# Just print
                foreach $line  (@res) {print "$line \n";}
        } else {
        # If the query fails, make sure we don't generate a perl error, just a critical message.
                if ((!$res[0])||($res[0] =~ /Failed/)) {
                        $value = "null";
                        print " No valid value returned - bad args or host down?\n" if $debug;
                        } elsif ($res[0] =~ /failed/) {
                                $value = "null";
                                print "Query failed- bad args or host down?\n" if $debug;
                                print "Result: $res[0] \n" if $debug;
                        } else {
                               @stuff = split(/\s/,$res[0]);
                               for ($i= 0; $i < @stuff; $i++) {
                                        if ($stuff[$i] eq "=") {
                                                $value = $stuff[$i+1];
                                                last;
                                        }
                                }
                                print "Result: $res[0] \n" if $debug;
                                print "Value: $value \n" if $debug;
                }
        }
}

sub call_jmx_pw  {
        my $server = $_[0];
        my $port = $_[1];
        my $attribute = $_[2];
	my $user = $_[3];
	my $password = $_[4];
        my $mbean = $_[5];
	my @stuff;
	my $line;
	my $i;
	my @res;
        @res= `java -classpath /usr/local/groundwork/nagios/libexec/nagtomcat.jar com.groundworkopensource.tomcat.nagios.plugin.Shell -s $server -p $port -user $user -password $password -m "$mbean" -a "$attribute" 2>&1`;
# Look for an error where the JVM needs a DIFFERENT unmame and password
         foreach $line  (@res) {
                if ($line =~ /username\/password/){
                        print "$line \n";
                        exit(3);
                }
        }

# Decide what to do with results - just print, or do we test against a value?
	if ($attribute =~ "!") {
# Just print
		foreach $line  (@res) {print "$line \n";}
	} else {
# If the query fails, make sure we don't generate a perl error, just a critical message. 
		if  (!$res[0]) {
			$value = "null";
			print " No valid value returned - bad args or host down?\n" if $debug;
			} elsif (($res[0] =~ /ailed/)||($res[0] =~ /NullPointer/)) {
                       		$value = "null";
                        	print "Query failed- bad args or host down?\n" if $debug;
				print "Result: $res[0] \n" if $debug;
			} else {
                               @stuff = split(/\s/,$res[0]);
                               for ($i= 0; $i < @stuff; $i++) {
					if ($stuff[$i] eq "=") {
						$value = $stuff[$i+1];
						last;
    					}    
				}
                                print "Result: $res[0] \n" if $debug;
				print "Value: $value \n" if $debug;
		}
	}
}


# Help sub
sub print_help () {
        print_revision($PROGNAME,'$Revision: 0.1 $');
        print "Copyright (c) 2006 GroundWork Open Source
License: GPL

This is a Nagios(R) plugin that checks any JMX enabled JVM for any mbean attribute with a JMX call


 Requirements:
 Needs the Tomcat Nagios Plugin Shell by GroundWork Open Source. Designed to run on a GroundWork server
 either open source (with Java installed) or professional.
 Having jconsole (part of the java sdk) installed is a good idea, just so you can see the context of what
 you are setting up to monitor.

 Copyright (c) 2000-2006 GroundWork Open Source
 Author Thomas Stocking (tstocking\@groundworkopensource.com)

 This plugin is FREE SOFTWARE. No warrenty of any kind is implied or granted.
 You may use this software under the terms of the GNU General Public License only.
 See http://www.gnu.org/copyleft/gpl.html and usage sections of this code.

 Changelog:
 12/21/2006 - original version 0.1
 2/13/2007 - fixed error handling when the specified mbean name was not found


";
print_usage();
}

sub print_usage() {
print "
usage:
    check_jvm.pl -H <hostaddress> -p <jmx port> -u <optional:username> -P <optional:password>
     -m <mbean name>  -a <mbean attribute> -w <optional:warning value> -c <optional:critical value>


This plugin will accept arguments for any mbean and attributes supplied. 
! - lists all available attributes of the mbean supplied with -m

Example:
check_jvm.pl -H tomcat_host -p 9004 -m Catalina:type=ThreadPool,name=http-8080 -a currentThreadsBusy -w 3:20 -c 1:500

or

check_jvm.pl -H tomcat_host -p 9004 -m Catalina:type=ThreadPool,name=http-8080 -a !

to list all the attributes of the ThreadPool mbean. Note -a ! is optional (default is !)

Warning levels can be specified as an upper bound, or as a lower bound:upper bound. 
Single integers are treated as upper bounds

Critical levels can be specified in the same way. 

Upper bounds for critical MUST be greater than those for warning. Both warning and critical values are optional.

Other arguments accepted:
-h, --help
   Print  this help screen
-V, --Version
   Print version of plugin
-D Turn on debug messages (don't do this when running from Nagios)

Note: If you need to specify mbean names and/or attributes with spaces in them, use double quotes OR \\s, not both.
";
}


