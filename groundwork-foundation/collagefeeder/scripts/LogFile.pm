#!/usr/local/groundwork/bin/perl
# $Id: $
#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

package LogFile;
$debug = 0;

sub new {
	my ($invocant,$name,$inode,$seekPos,$size) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
			name => $name,
			inode => $inode,
			seekPos => $seekPos,
			size => $size
	};
 	bless($self,$class);
	return $self;
}

sub hasBeenRotated{
   my $self = shift;
   open(LOG,">>/usr/local/groundwork/foundation/container/logs/nagios2collage_eventlog.log");

    print LOG "NAME: $self->{name} \n" if($debug);  
   close(LOG);
 if($self->{name} =~ /.*nagios.log/){   
      return 0;
   }
   else{
    return 1;
   }

}

 
1;
