#!/usr/bin/perl
############################################################################
### DB Methods: To be used after install is complete for obvious reasons ###
############################################################################
package GWDB;
use DBI;
sub init {
	$component = shift;
	
	my ($databaseName,$databaseHost,$databaseUser,$databasePassword) = getDBConfig($component);
 
	# Create database handle
	#
	
 	$dbh = DBI->connect( "DBI:mysql:$databaseName:$databaseHost",
		$databaseUser, $databasePassword )
	  or die "Can't connect to database $databaseName. Error:" . $DBI::errstr;
 
	return 1;
}  


sub getDBConfig{
	$component=shift;
	
	my ($dbName,$dbHost,$user,$password);
	open(DBFILE,"/usr/local/groundwork/config/db.properties");
	while(<DBFILE>){
		$line = $_;
		$line = trim($line);
		if($line =~ "${component}.database"){
			(undef,$dbName) = split(/=/,$line);	
		}
		elsif($line =~ "${component}.dbhost"){
			(undef,$dbHost) = split(/=/,$line);
		}
		elsif($line =~ "${component}.username"){
			(undef,$user) = split(/=/,$line);
		}
		elsif($line =~ "${component}.password"){
			(undef,$password) = split(/=/,$line);
		}

	}#end while	
	close(DBFILE);
		@retArray = ($dbName,$dbHost,$user,$password);
		return @retArray;
}

sub executeQuery{
	# $query = $db->query($sql_query);
	my $query = $_[0];
		#debug("executing $query");	 
	$sth = $dbh->prepare($query)
	  || reportError( "Error preparing query: $query: : ". $dbh->errstr );
	 # print "QUERY: " . $query . "\n"; 
	$sth->{'PrintError'}=0;
	$sth->execute()
	  || reportError( "Error executing query: $query :  " . $dbh->errstr );	
 	return $sth;
}

sub reportError{
 $text = shift;
 print "$text\n";
}

sub trim{
	my $string = shift;
	$string =~ s/\s+$//;
	return $string;
}
1;