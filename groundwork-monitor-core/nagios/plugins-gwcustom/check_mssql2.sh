#!/bin/sh
# This script is designed to be used by Nagios. It checks for the availability of both Microsoft SQL Server 7 and 2000.
#
# Requirements:
#
# Sqsh (http://www.sqsh.org/)
# FreeTDS (http://www.freetds.org/)
#
# It was written by Tom De Blende (tom.deblende@village.uunet.be) in 2003. 
#
# Version 1.0.
# Version 1.1: rewritten the initial script so that it not only works from the CLI but also from within Nagios. Always helpful...
# Version 1.2: grouped output so things look a bit better.
#
# You might want to change these values:

sqshcmd="/usr/local/bin/sqsh"
catcmd=`which cat`
grepcmd=`which grep`
rmcmd=`which rm`
mktempcmd=`which mktemp`
wccmd=`which wc`
sedcmd=`which sed`
trcmd=`which tr`
uniqcmd=`which uniq`

###################################################################################################################


hostname=$1
usr=$2
pswd=$3
srv=$4

# $BASH_VERSION is a shell variable in bash, not an environment variable.
# We use it to tell whether we're running a bash shell instead of some other variant of the Bourne shell.
if [ -n "$BASH_VERSION" ]; then 
        escapes=-e
else
        escapes=
fi

if [ ! "$#" -eq "4" ]; then
        echo $escapes "\nYou did not supply enough arguments. \nUsage: $0 <host> <username> <password> <version> \n \n$0 checks Microsoft SQL Server connectivity. It works with versions 7 and 2000.\n\nYou need a working version of Sqhs (http://www.sqsh.org/) and FreeTDS (http://www.freetds.org/) to connect to the SQL server. \nIt was written by Tom De Blende (tom.deblende@village.uunet.be) in 2003. \n\nExample:\n $0 dbserver sa f00bar 2000\n" && exit "3"

elif [ -z "$sqshcmd" ]; then
	echo $escapes "Sqsh not found! Please verify you have a working version of Sqsh (http://www.sqsh.org/) and enter the full path in the script." && exit "3"

fi

exit="3"


# Creating the command file that contains the sql statement that has to be run on the SQL server. Normally one would use the -C parameter of sqsh, but it seems that there is a bug that doesn't allow statements with more than one blanc.

tmpfile=`$mktempcmd /tmp/$hostname.XXXXXX`

if [ $srv = "7" ]; then
        spid=7
elif [ $srv = "2000" ]; then
        spid=50
else
	echo $escapes "$srv is not a supported MS SQL Server version!" && exit "3"
fi

# echo $escapes "select loginame from sysprocesses where spid > $spid order by loginame asc\ngo" > $tmpfile

echo $escapes "EXEC sp_helpdb" > $tmpfile

# Running sqsh to get the results back.

resultfile=`$mktempcmd /tmp/$hostname.XXXXXX`
$sqshcmd -S $hostname -U $usr -P $pswd -w 100000 -i $tmpfile -o $resultfile -b -h 2>/dev/null

if [ ! -s $resultfile ]; then
	$rmcmd -f $tmpfile $resultfile;
	echo CRITICAL - Could not make connection to SQL server.;
	exit 2;
else

# Parse sp_helpdb output and detect errors
db_count=`wc -l $resultfile | awk '{print $1}'`


#	nmbr=`$catcmd $resultfile | $grepcmd -v "\-\-\-\-\-" | $grepcmd -v "loginame" | $grepcmd -v "affected" | $sedcmd '/^$/d' | $sedcmd 's/ //g' | $wccmd -l | sed 's/ //g'`;
#	users=`$catcmd $resultfile | $grepcmd -v "\-\-\-\-\-" | $grepcmd -v "loginame" | $grepcmd -v "affected" | $sedcmd '/^$/d' | $sedcmd 's/ //g' | $uniqcmd -c | $trcmd \\\n , | $sedcmd 's/,$/./g' | $sedcmd 's/,/, /g' | $sedcmd 's/  //g' | $trcmd \\\t " "`;
        $rmcmd -f $tmpfile $resultfile;
        echo "OK - MS SQL Server $srv has $nmbr user(s) connected: $users" | sed 's/: $/./g';
        exit 0;
fi

# Cleaning up.

$rmcmd -f $tmpfile $resultfile
echo $stdio
exit $exit
