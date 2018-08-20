#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2011 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use DBI;
use GWLogger;
use File::Copy;

package DBLib;
### change to ConnectionManager #############################
  
sub initDB {
	my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = getDBConfig();
 
	# Create database handle
	
	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
          
	$dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
	  or die "Can't connect to database $dbname. Error: " . $DBI::errstr;

	#
	# Create 2nd database handle (why???)
	#
	$dbh1 = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
	  or die "Can't connect to database $dbname. Error: " . $DBI::errstr;
	
	return 1;
}  

sub initCollageDB{
	my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = getCollageDBConfig();
 
	# Create database handle
	
	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
          
	$collage_dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
	  or die "Can't connect to database $dbname. Error: " . $DBI::errstr;

	#
	# Create 2nd database handle (why???)
	#
	$collage_dbh1 = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
	  or die "Can't connect to database $dbname. Error: " . $DBI::errstr;
	
	return 1;
}

sub executeCollageQuery{
	# $query = $db->query($sql_query);
	my $query = shift;
	#debug("executing $query");	 
	my $sth = $collage_dbh->prepare($query) || reportError( "Error preparing query: $query: : ". $dbh->errstr );
	 # print "QUERY: " . $query . "\n"; 
	$sth->{'PrintError'}=0;
	$sth->execute() || reportError( "Error executing query: $query :  " . $dbh->errstr );	
 	return $sth;
}
sub executeQuery{
	# $query = $db->query($sql_query);
	my $query = $_[0];
	#debug("executing $query");	 
	$sth = $dbh->prepare($query) || reportError( "Error preparing query: $query: : ". $dbh->errstr );
	# print "QUERY: " . $query . "\n"; 
	$sth->{'PrintError'}=0;
	$sth->execute() || reportError( "Error executing query: $query :  " . $dbh->errstr );	
 	return $sth;
}

sub executeMySQLQuery{
	# $query = $db->query($sql_query);
	my $query = $_[0];
	# debug("executing $query");	 
	$sth = $dbh->prepare($query) || reportError( "Error preparing query: $query: : ". $dbh->errstr ); 
	$sth->execute() || reportError( "Error executing query: $query :  " . $dbh->errstr );	
	$myID = $sth->insert_id() || print "ERR: $dbh->errstr";
	return $myID;
}

sub reportError{
	$severity = $_[0];
	$errorText = $_[1];	
}
 
sub debug{
	if ($DEBUG == 1){	
		my $message = $_[0]; 
		print "$message\n\n";	
	}
}

sub printHeader{
	debug("printHeader($type);");
	$type = $_[0];
	$title = $_[1];
#	$popup = $_[2];
#	
#	if($popup ne ""){
#		$popText = qq{
#		   <body onload=alert('$popup');>
#		};
#	}
	
	print "Content-type: text/$type\n\n";
	print qq{
		<html>
		<head>
		<title>$title</title>
		<link rel='stylesheet' type='text/css' href=styles/groundwork.css></link>
		<link href="../aw/grid.css" rel="stylesheet" type="text/css" ></link>
		<script src="../aw/grid.js"></script>
		<script languange='JavaScript' src='reports.js'>
		<script language='JavaScript'>var listOpt= ''</script>
		</head>
	};
}

sub sendInvalidRequest
{
	print qq{
		<alarm>Invalid Request</alarm>
	};
}

  
sub printDatabaseCtl{
	$retMsg = shift;
	GWLogger::log("printDatabaseCtl(): retMsg = $retMsg");
	
	if($retMsg){
		#$pMsg = "<P><font color=red>$retMsg</font>";
		$pMsg = qq{
		<div style="position: absolute; right: 50px; top: 2px; height: 20px; width: 50px;  ">
		 <P><font color=red><B>$retMsg</b></font>
		</div> 
		};
	}
	
	my ($dbName,$dbHost,$dbUser,$dbPass,$dbtype) = DBLib::getDBConfig();
	print qq{
 
	<P>
	<form name='dbControlForm'>
	<table class=window>
	<tr><td>
	
	<table class=windowContent>
	<tr   class=windowHeader><td  class='windowHeader' colspan=2>Database</td></tr>
	<tr><td>Database Name:</td>  <td><input type=text name=dbName size=25 value=$dbName></td></tr>
	<tr><td>Host:</td>  <td> <input type=text value='$dbHost' name=host size=25></td></tr>
	<tr><td>User:</td>  <td> <input type=text value='$dbUser' name=user size=25></td></tr>
	<tr><td>Password:</td>  <td> <input type=password value='$dbPass' name=password size=25></td></tr>
	<tr><td colspan=2><input type=button name=test value='Test' onclick="javascript:getOpt();sendDataReq('dbControl','test',listOpt);">
	<input type=button name=save value='Save' onclick="javascript:getOpt();sendDataReq('dbControl','save',listOpt);"></td></tr>
	</table>
	
	</td></tr>
	</table>
	</form>
	$pMsg
 
 };
  
}

# saveDBConfig($dbName,$dbHost,$dbUser,$dbPass);

sub saveDBConfig{
	$dbName = shift;
	$dbHost = shift;
	$dbUser = shift;
	$dbPass = shift;
	$dbPropertiesFile = "/usr/local/groundwork/config/db.properties";
	$tmpFile = "/tmp/157915412901";
	open(DBFILE,$dbPropertiesFile) || return 0;
	open(TMP,">$tmpFile") || return 0;
	while(<DBFILE>){
	    $line = $_;
	    if($line =~ "^\s*logreporting\.password"){
		    print TMP "logreporting.password=$dbPass\n";
	    }
	    elsif($line =~ "^\s*logreporting\.username"){
		    print TMP "logreporting.username=$dbUser\n";
	    }
	    elsif($line =~ "^\s*logreporting\.dbhost"){
		    print TMP "logreporting.dbhost=$dbHost\n";
	    }
	    elsif($line =~ "^\s*logreporting\.database"){
		    print TMP "logreporting.database=$dbName\n";
	    }
	    else { 
		    print TMP $line;
	    }
	}
	close(DBFILE);
	close(TMP);
	File::Copy::copy($tmpFile,$dbPropertiesFile) || return 0;
	return 1;
}

sub getDBConfig{
	my ($dbName,$dbHost,$dbUser,$dbPass,$dbType);
	open(DBFILE,"/usr/local/groundwork/config/db.properties");
	while(<DBFILE>){
		$line = $_;
		$line = trim($line);
		if($line =~ "^\s*logreporting\.database"){
			(undef,$dbName) = split(/=/,$line);	
		}
		elsif($line =~ "^\s*logreporting\.dbhost"){
			(undef,$dbHost) = split(/=/,$line);
		}
		elsif($line =~ "^\s*logreporting\.username"){
			(undef,$dbUser) = split(/=/,$line);
		}
		elsif($line =~ "^\s*logreporting\.password"){
			(undef,$dbPass) = split(/=/,$line);
		}
		elsif($line =~ "^\s*global\.db\.type"){
			(undef,$dbType) = split(/=/,$line);
		}
	}
	close(DBFILE);
	@retArray = ($dbName,$dbHost,$dbUser,$dbPass,$dbType);
	return @retArray;
}

sub getCollageDBConfig{
	my ($dbName,$dbHost,$dbUser,$dbPass,$dbType);
	open(DBFILE,"/usr/local/groundwork/config/db.properties");
	while(<DBFILE>){
		$line = $_;
		$line = trim($line);
		if($line =~ "^\s*collage\.database"){
			(undef,$dbName) = split(/=/,$line);	
		}
		elsif($line =~ "^\s*collage\.dbhost"){
			(undef,$dbHost) = split(/=/,$line);
		}
		elsif($line =~ "^\s*collage\.username"){
			(undef,$dbUser) = split(/=/,$line);
		}
		elsif($line =~ "^\s*collage\.password"){
			(undef,$dbPass) = split(/=/,$line);
		}
		elsif($line =~ "^\s*global\.db\.type"){
			(undef,$dbType) = split(/=/,$line);
		}
	}
	close(DBFILE);
	@retArray = ($dbName,$dbHost,$dbUser,$dbPass,$dbType);
	return @retArray;
}

sub testDBConfig{
	my $dbname = shift;
	my $dbhost = shift;
	my $dbuser = shift;
	my $dbpass = shift;
	my $dbtype = shift;
	my $retError = "Connection Validated";

	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
	
	$dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
	  or $retError =  "Error: " . $DBI::errstr;
 
	return $retError;
}

sub trim{
	my $string = shift;
	$string =~ s/\s+$//;
	return $string;
}

1;

