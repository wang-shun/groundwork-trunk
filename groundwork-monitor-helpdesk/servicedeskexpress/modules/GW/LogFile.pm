package GW::LogFile;

use vars qw($VERSION); $VERSION = '1.0';

use strict;
use warnings;

sub new {
	my $packageName = shift;

	my $self = {
		_logFileName => undef
	};

	# Bless the Hash
	bless $self, $packageName;

	# Pass the Reference
	return $self;
}

# Get / Set Routines

sub getLogFile { my $self = shift(@_); return $self->{_logFileName};   }

sub setLogFile {
	my ($self, $aLogFile) = @_;
	if (defined($aLogFile)) { $self->{_logFileName} = $aLogFile; }
}

sub log {
	my ($self, $msg) = @_;

	my $pid      = $$;
	my $dateTime = $self->getDateTimeStr();
	my $logFile  = $self->{_logFileName};

	# Return silently if log file can't be opened
	open(LOG, ">>$logFile") or return;
	print LOG "[$dateTime] [pid:$$] $msg \n";
	close(LOG);
}

sub getDateTimeStr {
	my ($sec,$min,$hour,$mday,$mon,$year,$wday, $yday,$isdst) = localtime(time);

	my $dateStr = sprintf("%4d-%02d-%02d %02d:%02d:%02d",$year+1900,$mon+1,$mday,$hour,$min,$sec);

	return $dateStr;
}

1;
__END__


