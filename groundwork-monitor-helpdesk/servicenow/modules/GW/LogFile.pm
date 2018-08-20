package GW::LogFile;

# Copyright (c) 2011 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

use vars qw($VERSION);
$VERSION = '1.1.1';

use strict;
use warnings;

sub new {
    my $packageName = shift;

    my $self = { _logFileName => undef };

    # Bless the Hash
    bless $self, $packageName;

    # Pass the Reference
    return $self;
}

# Get / Set Routines

sub getLogFile { my $self = shift(@_); return $self->{_logFileName}; }

sub setLogFile {
    my ( $self, $aLogFile ) = @_;
    $self->{_logFileName} = $aLogFile if defined($aLogFile);
}

sub setLogRotation {
    my ( $self, $maxLogFileSize, $maxLogFilesToRetain ) = @_;
    $self->{_maxLogFileSize}      = $maxLogFileSize      if defined($maxLogFileSize);
    $self->{_maxLogFilesToRetain} = $maxLogFilesToRetain if defined($maxLogFilesToRetain);
}

sub log {
    my ( $self, $msg ) = @_;

    my $pid      = $$;
    my $dateTime = $self->getDateTimeStr();
    my $logFile  = $self->{_logFileName};

    # Return silently if log file can't be opened
    open( LOG, '>>', $logFile ) or return;
    print LOG "[$dateTime] [pid:$$] $msg\n";
    close(LOG);
}

sub getDateTimeStr {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);

    my $dateStr = sprintf( "%4d-%02d-%02d %02d:%02d:%02d", $year + 1900, $mon + 1, $mday, $hour, $min, $sec );

    return $dateStr;
}

# FIX LATER:  If we cannot carry out some of the operations here, we ought to log that fact to syslog.
sub rotateLogFile {
    my ($self) = @_;
    ## Implement our own locally-controlled log rotation, so a long-running daemon doesn't
    ## fill the entire disk partition with a single huge log file.  (Bear in mind that
    ## with this model, as opposed to an externally imposed logfile rotation, it is the
    ## responsibility of the logging application to call this routine periodically.)
    my $max_logfile_size       = $self->{_maxLogFileSize}      || 10_000_000;
    my $max_logfiles_to_retain = $self->{_maxLogFilesToRetain} || 5;
    my $logFile                = $self->{_logFileName};
    open( LOG, '>>', $logFile ) or return;
    if ( tell(LOG) > $max_logfile_size ) {
	if ( $max_logfiles_to_retain > 1 ) {
	    my $dateTime = $self->getDateTimeStr();
	    print LOG "[$dateTime] [pid:$$] === Rotating logfiles. ===\n";
	    my $num     = $max_logfiles_to_retain - 1;
	    my $newname = "$logFile.$num";
	    while ( --$num >= 0 ) {
		my $oldname = $num ? "$logFile.$num" : $logFile;
		if ( -f $oldname && !rename( $oldname, $newname ) ) {
		    print LOG "ERROR:  Cannot rename $oldname to $newname ($!)";
		}
		$newname = $oldname;
	    }
	}
	else {
	    truncate LOG, 0;
	}
    }
    close(LOG);
}

1;

__END__

