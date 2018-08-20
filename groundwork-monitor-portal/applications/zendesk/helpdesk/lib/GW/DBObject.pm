package GW::DBObject;

use vars qw($VERSION); $VERSION = '1.0';

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
        my ($self, $host) = @_;
        if (defined($host)) { $self->{_dBHost} = $host; }
}

sub setDBName {
	my ($self, $name) = @_;
	if (defined($name)) { $self->{_dBName} = $name; }
}

sub setDBUser {
	my ($self, $user) = @_;
	if (defined($user)) { $self->{_dBUser} = $user; }
}

sub setDBPass {
	my ($self, $pass) = @_;
	if (defined($pass)) { $self->{_dBPass} = $pass; }
}

sub setDBParams {
	my ($self, $params) = @_;
	if (defined($params)) { $self->{_dBParams} = $params; }
}

sub connect {
	my $self = shift(@_);

	my $dBName   = $self->{_dBName};
	my $dBHost   = $self->{_dBHost};
	my $dBUser   = $self->{_dBUser};
	my $dBPass   = $self->{_dBPass};
	my $dBParams = $self->{_dBParams};

	$self->{_dBHandle} = DBI->connect_cached("DBI:mysql:$dBName:$dBHost", 
	                                         $dBUser, $dBPass, $dBParams);

	if ($DBI::errstr) {
		print "\nCould not access database: $dBName on host $dBHost \n\n";
		exit;
	}
}

sub disconnect {
	my $self = shift(@_);

	if (defined($self->{_dBHandle})) {
		$self->{_dBHandle}->disconnect();
	}
}

1;
__END__


