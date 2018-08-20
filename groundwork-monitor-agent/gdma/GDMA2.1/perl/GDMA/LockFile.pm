################################################################################
#
# GDMA::LockFile
#
# This library contains routines that support file locking to prevent
# concurrent access to critical resources from multiple independent programs.
#
# Copyright (c) 2017-2018 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#
################################################################################

package GDMA::LockFile;

use strict;
use warnings;

our $VERSION = '0.8.0';

use Fcntl qw(:DEFAULT :seek :flock);

our $EXCLUSIVE    = 'exclusive';
our $SHARED       = 'shared';
our $BLOCKING     = 0;
our $NON_BLOCKING = 1;

# FIX LATER:  Compare this implementation to the routines provided in the Log::Dispatch::FileRotate::Flock package.

sub open_and_lock {
    my $handle = $_[0];  # passed in to allow locks on multiple files
    my $path   = $_[1];  # path to lock file
    my $type   = $_[2];  # 'shared' or 'exclusive'
    my $try    = $_[3];  # optional; if set, don't block
    my @errors = ();

    require File::FcntlLock;

    # O_NOFOLLOW is apparently not implemented on the Windows platform.
    my $o_nofollow = ( $^O eq 'MSWin32' ) ? 0 : O_NOFOLLOW;
    my $old_umask = umask 0117;
    if ( !sysopen( $handle, $path, (($type eq $EXCLUSIVE) ? (O_WRONLY | O_APPEND) : (O_RDONLY)) | O_CREAT | $o_nofollow, 0660 ) ) {
	umask $old_umask;
	push @errors, "Cannot open $path: $!";
	return \@errors;
    }
    umask $old_umask;
    my $fs = new File::FcntlLock;
    $fs->l_type( ($type eq $SHARED) ? F_RDLCK : F_WRLCK );
    $fs->l_whence(SEEK_SET);
    $fs->l_start(0);
    $fs->l_len(0);
    # usually a blocking call ...
    if ( !$fs->lock( $handle, $try ? F_SETLK : F_SETLKW ) ) {
	push @errors, "Cannot lock $path: $! (" . $fs->error() . ')';
	return \@errors;
    }

    return \@errors;
}

sub lock_file_exists {
    my $handle = $_[0];
    my $path   = $_[1];  # path to lock file
    my @errors = ();

    my ($handdev, $handino) = stat($handle);
    my ($pathdev, $pathino) = stat($path);
    if ( !defined($pathdev) || !defined($handdev) || $pathdev != $handdev || $pathino != $handino) {
	push @errors, "Error:  bad file ($path) is being used for locking.";
	return \@errors;
    }

    return \@errors;
}

sub close_and_unlock {
    close $_[0];
}

sub unlink_and_close {
    my $handle = $_[0];
    my $path   = $_[1];  # path to lock file

    unlink $path if $path;
    close $handle;
}

# get an exclusive-access lock to protect all auto-setup activity
sub get_file_lock {
    my $handle = $_[0];    # passed in to allow locks on multiple files
    my $path   = $_[1];    # path to lock file
    my $type   = $_[2];    # 'shared' or 'exclusive'
    my $try    = $_[3];    # optional; if set, don't block
    my @errors = ();

    if ( $^O eq 'linux' || $^O eq 'solaris' || $^O eq 'aix' || $^O eq 'hpux' ) {
	my $errors = open_and_lock( $handle, $path, $type, $try );
	if (@$errors) {
	    if ( defined fileno $handle ) {
		## We were able to open the file, but not obtain the lock.
		## So some other actor must be dealing with this file already.
		push @errors, "Cannot obtain a lock to protect against concurrent updates.";
		push @errors, @$errors;
		close_and_unlock($handle);
	    }
	    else {
		## We couldn't even open the file.
		push @errors, "Cannot open a lockfile to protect against concurrent updates.";
		push @errors, @$errors;
	    }
	    return \@errors;
	}
	elsif ( @{ lock_file_exists( $handle, $path ) } ) {
	    ## We got the lock, but too late -- somebody else locked and then
	    ## destroyed the file after we opened it and before we got the lock.
	    push @errors, "Encountered contention for the $path lockfile.";
	    close_and_unlock($handle);
	    return \@errors;
	}
	elsif ( !-f $handle || !-O _ ) {
	    ## This situation won't clear by itself, and it will therefore block future updates for
	    ## this host.  But it's clear evidence of some sort of tampering, so we want to leave
	    ## the evidence around for human inspection.  If not for that, we would be tempted to
	    ## attempt to unlink the file, to self-heal and allow updates again without additional
	    ## human interaction.
	    push @errors, "Lockfile $path is not a regular file owned by " . ( scalar getpwuid($<) ) . '.';
	    close_and_unlock($handle);
	    return \@errors;
	}
    }
    elsif ( $^O eq 'MSWin32' ) {
	## We'll either need to use flock() or some platform-specific CPAN package.
	##
	## Check to see whether flock() actually returns right away on a non-blocking lock attempt,
	## starting with Windows Server 2003.
	##
	## Compare the LockFile and LockFileEx functions, and check how they are accessed from
	## within some Perl package.
	##
	## Pay attention to whether file locking is preserved across a fork() and exec(); perhaps
	## set FD_CLOEXEC on the lockfile handle (O_CLOEXEC during file open).
	##
	## FIX MAJOR:  this implementation is provisional, and needs testing

	# O_NOFOLLOW is apparently not implemented on the Windows platform.
	my $o_nofollow = ( $^O eq 'MSWin32' ) ? 0 : O_NOFOLLOW;
	my $old_umask = umask 0117;
	if ( !sysopen( $handle, $path, ( ( $type eq $EXCLUSIVE ) ? ( O_WRONLY | O_APPEND ) : (O_RDONLY) ) | O_CREAT | $o_nofollow, 0660 ) ) {
	    umask $old_umask;
	    push @errors, "Cannot open $path: $!";
	    return \@errors;
	}
	umask $old_umask;

	# Note that we are not taking any trouble to handle interrupt signals and retries.
	# Perhaps that might come with some future version, if we have some program in which
	# we actively try to manage signal handling.
	if ( not flock( $handle, ( ( $type eq $SHARED ) ? LOCK_SH : LOCK_EX ) | ( $try ? LOCK_NB : 0 ) ) ) {
	    push @errors, "Cannot lock $path: $!";
	    close $handle;
	}
    }

    return \@errors;
}

# release an exclusive-access lock that protects all auto-setup activity
sub release_file_lock {
    my $handle = $_[0];
    my $path   = $_[1];  # path to lock file

    my $outcome = 0;

    if ( $^O eq 'linux' || $^O eq 'solaris' || $^O eq 'aix' || $^O eq 'hpux' ) {
	## FIX MAJOR:  is this really what we want?
	unlink_and_close( $handle, $path );
	$outcome = 1;
    }
    elsif ( $^O eq 'MSWin32' ) {
	## We'll either need to use flock() or some platform-specific CPAN package.
	## As noted above, signals are not handled here gracefully.
	$outcome = flock( $handle, LOCK_UN );
    }

    return $outcome;
}
