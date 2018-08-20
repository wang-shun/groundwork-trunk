package GW::DBObject;

# Copyright (c) 2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

use vars qw($VERSION);
$VERSION = '2.1';

use strict;
use warnings;
use DBI;
use Data::Dumper;

sub new {
    my $packageName = shift;

    my $self = {
	_dBHost   => undef,
	_dBName   => undef,
	_dBUser   => undef,
	_dBPass   => undef,
	_dBHandle => undef,
	_dBParams => undef
    };

    # Bless the Hash
    bless $self, $packageName;

    # Pass the Reference
    return $self;
}

# Get / Set Routines

sub getDBHost   { my $self = shift(@_); return $self->{_dBHost};   }
sub getDBName   { my $self = shift(@_); return $self->{_dBName};   }
sub getDBUser   { my $self = shift(@_); return $self->{_dBUser};   }
sub getDBPass   { my $self = shift(@_); return $self->{_dBPass};   }
sub getDBParams { my $self = shift(@_); return $self->{_dBParams}; }

sub getHandle { my $self = shift(@_); return $self->{_dBHandle}; }

sub setDBHost {
    my ( $self, $host ) = @_;
    if ( defined($host) ) { $self->{_dBHost} = $host; }
}

sub setDBName {
    my ( $self, $name ) = @_;
    if ( defined($name) ) { $self->{_dBName} = $name; }
}

sub setDBUser {
    my ( $self, $user ) = @_;
    if ( defined($user) ) { $self->{_dBUser} = $user; }
}

sub setDBPass {
    my ( $self, $pass ) = @_;
    if ( defined($pass) ) { $self->{_dBPass} = $pass; }
}

sub setDBParams {
    my ( $self, $params ) = @_;
    if ( defined($params) ) { $self->{_dBParams} = $params; }
}

sub connect {
    my $self = shift(@_);

    my $dbname   = $self->{_dBName};
    my $dbhost   = $self->{_dBHost};
    my $dbuser   = $self->{_dBUser};
    my $dbpass   = $self->{_dBPass};
    my $dbparams = $self->{_dBParams};

    # FIX LATER:  We could restore the ability to select the database type at run time.
    # $dbparams might be a hashref, containing attributes similar to these:
    #     { 'AutoCommit' => 0, 'RaiseError' => 1, 'PrintError' => 0 }
    # depending on the needs and instrumentation of the calling application.
    # my $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    my $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    $self->{_dBHandle} = DBI->connect_cached( $dsn, $dbuser, $dbpass, $dbparams );

    if ($DBI::errstr) {
	# FIX LATER:  we ought to pass back an error message so the
	# calling application can log it, instead of just dying in
	# a way where the diagnostic message is likely to get lost
	print "\nCould not access database: $dbname on host $dbhost\n\n";
	exit;
    }
}

sub disconnect {
    my $self = shift(@_);

    if ( defined( $self->{_dBHandle} ) ) {
	$self->{_dBHandle}->disconnect();
    }
}

1;

__END__

