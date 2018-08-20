#!/usr/bin/perl

# COPYRIGHT:
#
# This software is Copyright (c) 2007-2009 NETWAYS GmbH, Christian Doebler
#                 some parts (c) 2009      NETWAYS GmbH, William Preston
#                                <support@netways.de>
#
# (Except where explicitly superseded by other copyright notices)
#
#
# LICENSE:GPL2
# see noma_daemon.pl in parent directory for full details.
# Please do not distribute without the above file!


# DB query
# TODO: implement cacheing
# TODO: graceful recovery on SQL errors

# FIX MINOR:  This is something of a hack.  It doesn't handle embedded quote characters
# (not that we have any in the cases where this will be called); it's here mostly because
# we sometimes have table columns which are named using words that are keywords for some
# database engine, and we need identifier quoting to prevent confusion.  It should really
# be calling $dbh->quote_identifier($name); instead, which would fully solve the problem
# across all databases.  We're not currently doing that only because we don't have an open
# $dbh handle to reference.
#
# We might get 'table.column' sent to this function, not just a bare column name.
#
# Do not further quote the result of this function.
sub quoteIdentifier
{
    my $identifier = shift;
    my $id_quote = $conf->{db}->{type} eq 'mysql' ? '`' : '"';
    return $id_quote . join( "$id_quote.$id_quote", split( /\./, $identifier ) ) . $id_quote;
}


sub queryDB
{


    # "returning", if specified, must be the name of the auto-increment field in the table having a row inserted
    my ( $queryStr, $array, $nolog, $returning ) = @_;
    my $debug_queries = $conf->{debug}->{queries};
    my $database_type = $conf->{db}->{type};
    my $dbh;
    my $result = 0;

    debug('Database type: '.$database_type,3) unless defined($nolog);
    if ($database_type eq 'mysql'){
            debug('Going to use MySQL as backend...',3) unless defined($nolog);
	    $dbh = DBI->connect(
	        'DBI:mysql:host='
	          . $conf->{db}->{mysql}->{host}	# MySQL NoMa Host
	          . ';database='
	          . $conf->{db}->{mysql}->{database},	# MySQL NoMa DB
	        $conf->{db}->{mysql}->{user}, 		# MySQL Username
		$conf->{db}->{mysql}->{password}	# MySQL Password
	    ) or debug($DBI::errstr,1);
    } elsif ($database_type eq 'postgresql'){
            debug('Going to use PostgreSQL as backend...',3) unless defined($nolog);
	    $dbh = DBI->connect(
	        'DBI:Pg:host='
	          . $conf->{db}->{postgresql}->{host}	# PostgreSQL NoMa Host
	          . ';dbname='
	          . $conf->{db}->{postgresql}->{database},	# PostgreSQL NoMa DB
	        $conf->{db}->{postgresql}->{user}, 		# PostgreSQL Username
		$conf->{db}->{postgresql}->{password},	# PostgreSQL Password
		{ 'AutoCommit' => 1 }
	    ) or debug($DBI::errstr,1);
	    $queryStr .= " returning $returning" if $returning;
    } elsif ($database_type eq 'sqlite3'){
            debug('Going to use SQLite3 as backend...',3) unless defined($nolog);
	    $dbh = DBI->connect(
		"dbi:SQLite:dbname=$conf->{db}->{sqlite3}->{dbfile}","","") or debug($DBI::errstr,1);
    } else {
	debug(' Invalid database set: '.$database_type.' Fix your configuration!',1);
	return undef;
    }

    debug("QUERY: " . $queryStr, 2) if (defined($debug_queries) and ($debug_queries != 0) and not defined($nolog));
    my $query = $dbh->prepare($queryStr) or do {
	## Force out the query if it hasn't already been logged, so we always have something useful to debug.
	debug( "QUERY: " . $queryStr, 1 )
	  if $conf->{debug}->{logging} < 2 || not( defined($debug_queries) and ( $debug_queries != 0 ) and not defined($nolog) );
	debug('DB statement prepare error: '.$dbh->errstr,1);
	$dbh->disconnect();
	## FIX MAJOR:  See notes at the end of this routine.
	return undef;
    };
    $query->execute or do {
	## Force out the query if it hasn't already been logged, so we always have something useful to debug.
	debug( "QUERY: " . $queryStr, 1 )
	  if $conf->{debug}->{logging} < 2 || not( defined($debug_queries) and ( $debug_queries != 0 ) and not defined($nolog) );
	debug('DB statement execute error: '.$query->errstr,1);
	$dbh->disconnect();
	## FIX MAJOR:  See notes at the end of this routine.
	return undef;
    };

    my $cnt = 0;

    # FIX MINOR:  The use of $dbh->rows here is highly suspect, both by itself and
    # especially in conjunction with a SELECT statement.  First of all, the DBI doc
    # mentions no such routine for the $dbh handle; is $DBI::rows meant instead?
    # And the DBI doc says about Statement Handle Methods:
    #
    #     rows
    #
    #     $rv = $sth->rows;
    #
    #     Returns the number of rows affected by the last row affecting command, or
    #     -1 if the number of rows is not known or not available.
    #
    #     Generally, you can only rely on a row count after a non-SELECT execute
    #     (for some specific operations like UPDATE and DELETE), or after fetching
    #     all the rows of a SELECT statement.
    #
    #     For SELECT statements, it is generally not possible to know how
    #     many rows will be returned except by fetching them all. Some drivers
    #     will return the number of rows the application has fetched so far, but
    #     others may return -1 until all rows have been fetched. So use of the
    #     rows method or $DBI::rows with SELECT statements is not recommended.
    #
    if ( $dbh->rows && $queryStr =~ m/^\s*select/i )
    {
        if ( defined($array) )
        {
            my @dbResult;
            while ( my $row = $query->fetchrow_hashref )
            {
                push( @dbResult, \%{$row} );
            }
            $dbh->disconnect();
            return @dbResult;
        } else
        {
	    ## FIX MINOR:  This is strange design.  Why use the row number
	    ## as a hash key instead of just returning an array??
            my %dbResult;
            while ( my $row = $query->fetchrow_hashref )
            {
                $dbResult{ $cnt++ } = \%{$row};
            }
            $dbh->disconnect();
            return %dbResult;
        }
    }
    elsif ( $returning && $queryStr =~ m/^\s*insert/i )
    {
	if ($database_type eq 'mysql') {
	    $result = $dbh->last_insert_id();
	}
	elsif ($database_type eq 'postgresql') {
	    $result = $query->fetchrow_arrayref()->[0];
	    ## debug("update got back $returning as $result", 3);
	    $query->finish;
	}
	elsif ($database_type eq 'sqlite3') {
	    $result = $dbh->last_insert_id();
	}
    }
    $dbh->disconnect();

    # FIX MAJOR:  Returning a numeric zero when the caller is expecting an array or hash
    # is a terrible idea.  You're mixing types in a manner that can get extremely confusing
    # -- this makes it very difficult to correctly handle the variant possible return values
    # in the calling code.  Perhaps better would be to return undef, but even then it's hard
    # to know how to capture the routine results in a robust manner in the calling code.
    # Perhaps the code should not try to collapse the execution of SELECT statements and
    # other statement types into the same routine, but even for a SELECT, we still need to
    # have some means of distinguishing an error return from a successful return.  Probably
    # the best way to do so would be to return an arrayref, hashref, or undef -- all of them
    # being scalar values.  That would make it easier for the calling code to test the result
    # before attempting to use it.
    return $result;

}

# this function has been split from the queryDB to implement cacheing
# TODO: implement cacheing
sub updateDB
{
    my ($sql, $nolog, $returning) = @_;
    my $cache;
    my $result = queryDB($sql, undef, $nolog, $returning);

    if ( !defined( $result ) )
    {

	debug('Failed to query DB - serious error', 1);
        # DB not available, cache the SQL
        #open( LOG, ">> $cache" );
        #print LOG "$sql\n";
        #close(LOG);
    }
    
    # This won't work, because we use a separate database connection for every query.
    # my $query = $dbh->prepare('select LAST_INSERT_ID') or return undef;
    # $query->execute or return undef;

    return $result;
}

sub dbVersion
{
	my ($expecteddbversion,$loopstopper) = @_;
        my $database_type = $conf->{db}->{type};
	my $database_upgrade = $conf->{db}->{automatic_db_upgrade};

        debug(' Checking DB schema version ',2);
	# Create if not exists.
	my $query;
	if ( $database_type eq 'sqlite3' || $database_type eq 'mysql' ) {
	    ## $query = 'CREATE TABLE if not exists information (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, type varchar(20) NOT NULL, content varchar(20) NOT NULL);';
	    $query = 'CREATE TABLE IF NOT EXISTS information (id int(11) NOT NULL,  `type` varchar(20)   NOT NULL,  content varchar(20)   NOT NULL)';
	}
	elsif ( $database_type eq 'postgresql' ) {
	    $query = 'CREATE TABLE IF NOT EXISTS information (id integer NOT NULL, type character varying(20) NOT NULL, content character varying(20) NOT NULL)';
	}
	my $dbResult = queryDB($query);
	# Select, if empty, should detect that the rest is empty/mismatch
        $query = 'select content from information where type=\'dbversion\'';
        my %dbResult = queryDB($query);
        my $dbversion = $dbResult{0}{content};
	if(!$dbversion){
	$dbversion=0;};

        debug(' DB schema version: '.$dbversion,2) if ($dbversion);

        if ($loopstopper eq 0){
                $loopstopper=1;
        } else {
                debug('Preventing the infinite loop, stopping...',3);
                return $dbversion;
        }

	# Check first if its filled with data, if not, just return with dbversion.
	if (($expecteddbversion) and ($expecteddbversion > $dbversion) and ($database_upgrade eq 'yes')){
		# Is the expected version not equal to dbversion?
		if ($expecteddbversion ne $dbversion){
			debug ('Mismatch in schema versions',1);
			# Only be nice if its SQLite3!
			if ($database_type eq 'sqlite3'){
				# CHECK IF THERE IS ANYTHING THERE, LIKE AN OLDER VERSION
				if ($expecteddbversion < $dbversion){
					debug('The expected dbversion is lower than the actual db version, script mismatched to database?',1);
				}
				elsif ($dbversion ne '0' and $expecteddbversion > $dbversion){
					# its just outdated, update.
                                        debug('The expected dbversion is higher than the actual db version, will upgrade schema',1);
					if(dbSchemaUpdate('update') eq 1){
						exit;};
                                        $dbversion=dbVersion($expecteddbversion,$loopstopper);
				} else {
					debug('The database is empty, will try to create it from scracth!',1);
					# needs to be created and filled with the normal structure
					if(dbSchemaUpdate('create_structure') eq 1){
						exit; # Failed to create database schema structure.
					}

					# fill with data
					if(dbSchemaUpdate('fill_data') eq 1){
						exit; # failed to fill database with data.
					}
					$dbversion=dbVersion($expecteddbversion,$loopstopper);
				}
			}
			elsif($database_type eq 'mysql' or $database_type eq 'postgresql'){
                                # CHECK IF THERE IS ANYTHING THERE, LIKE AN OLDER VERSION
                                if ($expecteddbversion < $dbversion){
                                        debug('The expected dbversion is lower than the actual db version, script mismatched to database?',1);
                                }
                                elsif ($expecteddbversion > $dbversion){
                                        # its just outdated, update.
                                        debug('The expected dbversion is higher than the actual db version, will upgrade schema',1);
					if(dbchemaUpdate('update') eq 1){
						exit;}; # failed to update schema.
                                } else {
                                        debug('The database is empty, please create it and update credentials to it accordingly.',1);
                                }
                        }
		} else {
			# Versions match
			debug(' Database schema version OK',3);
		}
	}
	elsif($expecteddbversion and $expecteddbversion > $dbversion and $database_upgrade eq 'no'){
		debug(' Automatic DB upgrade turned off, upgrade manually. ',1);
	}
        return $dbversion;
}

sub dbSchemaUpdate
{
	my ($operation) = @_;
        my $database_type = $conf->{db}->{type};
        my $database_upgrade = $conf->{db}->{automatic_db_upgrade};
	my $database_example_dir = $conf->{db}->{db_example_dir};
	my %dbSchemaFiles = (
                'sqlite_new_install_structure'  => 'sqlite3/install/default_schema.sql',
                'sqlite_new_install_data'	=> 'sqlite3/install/default_data.sql',
		'mysql_upgrade_200'		=> '',
		'postgresql_upgrade_200'		=> ''
 	);

	if ($database_upgrade eq 'no'){ debug('Automatic upgrade is turned off, no automatic schema update!',1);return 1;}; # Its NO to automatic in configuration, this a safety measure.
	debug('Will try to create/upgrade the '.$database_type.' DB schema. ',1);

	if ($database_type eq 'sqlite3'){
		if ($operation eq 'create_structure'){
						# first ensure that the DB is group writeable
						chmod 0664, $conf->{db}->{sqlite3}->{dbfile};
                        # Read file, LINE BY LINE and query.
			debug('Creating new database schema structure',1);

                        # Read file, LINE BY LINE and query.
                        debug('Inserting default schema to database',1);
                        if (-e $database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_structure}){
                                open FILE, "<$database_example_dir/$dbSchemaFiles{sqlite_new_install_structure}" or die $!;
                                while (my $query = <FILE>){
                                        my $dbResult = queryDB($query); # this might take a while.
                                }
                                close(FILE);
                                debug('Inserted default database schema',1);
                        } else {
                                debug('Cant find the needed schema file! Does it exist? Permissions? '.$database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_structure},1);
                        }

		
			#if (-e $database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_structure}.'/contactgroups.sql'){
#				my $sqldir = $database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_structure};
#				debug('SQLdir: '.$sqldir,3);
#				opendir(DIR, "$sqldir");
#				my @files = grep(/\.sql$/,readdir(DIR));
#				closedir(DIR);
#				debug('Files read in folder: '.@files,2);
#				foreach my $file (@files){
#					debug('About to import file:'.$file,3);
#		                        open FILE, "<$database_example_dir/$dbSchemaFiles{sqlite_new_install_structure}/$file" or die $!;
#					my @query = <FILE>; # Read EVERYTHING
#					$query = "@query";
#					my $dbResult = queryDB($query); # this might take a while.
#					close(FILE);
#				}
#				debug('Inserted database schema structure',1);
			#} else {
			#	debug('Cant find the needed schema file! Does it exist? Permissions? '.$database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_structure}.'/contactgroups.sql',1);
			#}
		}
                elsif ($operation eq 'fill_data'){
                        # Read file, LINE BY LINE and query.
                        debug('Inserting default data to database',1);
                        if (-e $database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_data}){
                                open FILE, "<$database_example_dir/$dbSchemaFiles{sqlite_new_install_data}" or die $!;
				while (my $query = <FILE>){
					my $dbResult = queryDB($query); # this might take a while.
				}
				close(FILE);
				debug('Inserted database default data',1);
                        } else {
                                debug('Cant find the needed schema file! Does it exist? Permissions? '.$database_example_dir.'/'.$dbSchemaFiles{sqlite_new_install_data},1);
                        }

                }
		elsif ($operation eq 'update'){
			# Read file, LINE BY LINE and query.
			# If loop per version on future updates.
			#open FILE, "<", "$database_example_dir.$dbSchemaFiles{sqlite_update_sth}" or die $!;
			#while (my $query = <FILE>){
			#	my $dbResult = queryDB($query);
			#}
		}
	}
	elsif($database_type eq 'mysql' or $database_type eq 'postgresql'){
		# NO UPDATES FOR YOU (-:
	} else {
		debug('Unknown backend to create/update!',1);
	}

	return 0;
}

1;
# vim: ts=4 sw=4 expandtab
