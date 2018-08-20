#!/usr/local/groundwork/perl/bin/perl
my $info = <<INFO;
Send NSCA Perl Client 1.14 for NSCA 2.3
Last modified 7/16/03 by Jason Lancaster <jason\@teklabs.net>
Data routines ported with help by Dennis Morsani and Donnie Roberts from Ethan Galstad's NSCA package

Usage: $0 -H <host_address> -S <source host> -C <source service> -U <status code> -T <status text>
\t[-f file_to_send] [-p port] [-t timeout] [-d] [h] [-s timestamp]

Options:
<host_address>\t= The IP address of the host running the NSCA daemon
<source host>\t= The host for which sending the result
<source service>\t= The service name for which sending the result
<status code>\t= Service status code
<status text>\t= Status information text
[file_to_send]\t= Filename to read and send
\t\t  (use instead of STDIN)
[port]\t\t= The port on which the daemon is running - default is 5667
[timeout]\t= Number of seconds before connection attempt times out
\t\t  (default timeout is 10 seconds)
[d]\t\t= Debug mode
[h]\t\t= Print this help file
[timestamp]\t= The timestamp to be used for this result

Note:
The only supported data encryption method is XOR.
This program can accept data through STDIN using tab delimited values.
 eg, cat stats.out | $0

Modified to take args for convenience by Thomas Stocking, GroundWork open source, 12/17/2006

INFO

use strict;
use Getopt::Std;
use Socket; 

# Main program's ugly global variables 
my (@crc32_table);    # we really don't need this to be global
my (%arg, $arg);

# Define options
getopts("df:H:hp:t:S:C:U:T:s:", \%arg);

# Set option defaults
$arg{d} = 0 unless $arg{d};
$arg{f} = "" unless $arg{f};
$arg{h} = 0 unless $arg{h};
$arg{p} = 5667 unless $arg{p};
$arg{t} = 10 unless $arg{t};
$arg{s} = 0 unless $arg{s};

# Do some basic option handling and set requirements
if ($arg{h} == 1) { &helpit; }
unless ($arg{H}) { warn "Missing required option -H <host_address>\n\n"; &helpit; }
unless ($arg{S}) { warn "Missing required option -S <source host>\n\n"; &helpit; }
unless ($arg{C}) { warn "Missing required option -C <source service>\n\n"; &helpit; }
unless ($arg{U}||$arg{U} ==0) { warn "Missing required option -U <status code>\n\n"; &helpit; }
unless ($arg{T}) { warn "Missing required option -T <status text>\n\n"; &helpit; }

#######################
#     subroutines     # 
#######################

sub debugit {
    my ($msg) = @_;

    if ($arg{d} == 1) { 
      printf("# DEBUG (%d)# %s\n", $$, $msg);
    } 

   return;
}

sub helpit {
    print $info;

    exit 0;
}


# xor the data (the only type of "encryption" we currently use)
sub myxor {
    my ($xor_key, $str) = @_;
 
    my $xor_str = $str ^ ($xor_key x int((length($str) + 127) / 128));
 
    return substr($xor_str, 0, length($str));
}

# build the crc table - must be called before calculating the crc value 
sub generate_crc32_table {
    my ($crc, $poly, $i, $j);

    $poly = 0xEDB88320;
    for($i = 0; $i < 256; $i++) {
        $crc = $i;
        for($j = 8; $j > 0; $j--) {
            if ($crc & 1) {
                $crc = ($crc >> 1) ^ $poly;
            } else {
                $crc >>= 1;
            }
        }
        $crc32_table[$i] = $crc;
    }

    return;
}

# calculates the CRC 32 value for a buffer
sub calculate_crc32 {
    my ($buffer) = @_;
    my ($crc, $this_char, $current_index);

    $crc = 0xFFFFFFFF;

    for($current_index = 0; $current_index < length($buffer); $current_index++) {
        $this_char = ord(substr($buffer, $current_index, 1));
        $crc = (($crc >> 8) & 0x00FFFFFF) ^ $crc32_table[($crc ^ $this_char) & 0xFF];
    }

    return ($crc ^ 0xFFFFFFFF);
}

# Read STDIN and parse it
sub getit {
    my ($iaddr, $paddr, $proto, $xor_key, $timestamp);
    my ($d);
    my $packet_version = "3";
    my $failure = sprintf("Could not establish a connection to %s! Are you an allowed_host?\n\n", $arg{H});

    $iaddr   = inet_aton($arg{H});
    $paddr   = sockaddr_in($arg{p}, $iaddr);
    $proto   = getprotobyname('tcp');

    # Start the timer. We don't want stale processes!
    local $SIG{ALRM} = sub { die sprintf("Connection to %s timed out after %d seconds\n\n", $arg{H}, $arg{t}); };       
    alarm $arg{t};
    
    # Open the socket
    socket (SOCK, PF_INET, SOCK_STREAM, $proto) || die $failure;
    connect(SOCK, $paddr) || die $failure;

    # Get 128bit xor key and 4bit timestamp.
    read(SOCK,$xor_key,128) || die $failure;
    read(SOCK,$timestamp, 4) || die $failure;

    # If a timestamp was explicitly specified, use it.
    # Convert the timestamp to network byte order.
    if ($arg{s} != 0) {
        $timestamp = pack("N", $arg{s});
    } 
 
    # Generate the crc table. This will be used to sign the packet.
    # We ported the code for this from the nsca_send.c file
    generate_crc32_table();

    if ($arg{f} ne "") { open(STDIN, "<$arg{f}") || die "Cannot open $arg{f} for reading, $!\n\n"; }
    
    #while (my $line = <STDIN>) {
    #    chomp $line;
    #    my ($hostname,$service,$return_code,$status) = split(/\t/, $line);
	  my $hostname = $arg{S};
	  my $service = $arg{C};
	  my $return_code = $arg{U};
	  my $status = $arg{T};

        if ($hostname eq '' || $service eq '' || $return_code eq '' || $status eq '') { 
            #warn sprintf("Invalid input line: %s\n", $line); 
        }
        else { 
            # Reset the crc value
            my $crc = "0";
            $d++;

            &debugit("Read input: $hostname\t$service\t$return_code\t$status");

            # Build our packet.
            my $tobecrced = pack("nxx N a4 n a64 a128 a512xx",
                $packet_version, $crc, $timestamp, $return_code, $hostname, $service, $status);

            # Get a signature for the packet.
            $crc = calculate_crc32($tobecrced);

            # Build the final packet with the sig.
            my $str = pack("nxx N a4 n a64 a128 a512xx",
                $packet_version, $crc, $timestamp, $return_code, $hostname, $service, $status);

            # Xor the sucker.
            my $string_to_send = myxor($xor_key, $str);

            # Spank it...
            send(SOCK,$string_to_send,0) || warn sprintf("Could not send packet %d\n", $d);
            &debugit("Sent $return_code, $hostname, $service, $status to $arg{H}");
        }
   # } 

    # Goodbye
    close(STATS);
    close(SOCK);

    # Things went good. Reset alarm.
    alarm 0;
    
    print "Sent $d packets to $arg{H}\n";

    exit 0;
}

# Main program
&getit();
exit;

