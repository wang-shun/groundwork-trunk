#!/bin/sh
#Copyright (C) 2004-2006  GroundWork Open Source Solutions info@groundworkopensource.com
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation and reprinted below;
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
# starts all feeder services as background tasks
#TODO: Check if processes are running only start if not running

#Start listener
./collage-db-service.sh &

#sleep for 5 seconds
sleep 5

#Start Nagios feeders
./nagios2collage_eventlog.pl &
./nagios2collage_socket.pl &

#TODO: Should install a cron job executing it once an hour
./nagios2collage_availability_RRD.pl &