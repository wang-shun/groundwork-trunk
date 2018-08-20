#!/usr/bin/perl -w --

# Based on the OpenSSL 1.01e version, with adjustments to work as a
# compiled program under Windows in the GroundWork GDMA client environment.


# Perl c_rehash script, scan all files in a directory
# and add symbolic links to their hash values.

my $openssl;

my $dir = "$PerlApp::RUNLIB/../../gdma";
my $prefix = $PerlApp::RUNLIB;

if(defined $ENV{OPENSSL}) {
	$openssl = $ENV{OPENSSL};
} else {
	$openssl = ( $^O eq 'MSWin32' ) ? "$PerlApp::RUNLIB/openssl.exe" : "openssl";
	$ENV{OPENSSL} = $openssl;
}

if ( $^O eq 'MSWin32' ) {
	$ENV{OPENSSL_CONF} = "$PerlApp::RUNLIB/../openssl/openssl.cnf";
	$ENV{CYGWIN} = 'nodosfilewarning';
}

my $pwd;
eval "require Cwd";
if (defined(&Cwd::getcwd)) {
	$pwd=Cwd::getcwd();
} else {
	$pwd=`pwd`; chomp($pwd);
}
my $path_delim = ($pwd =~ /^[a-z]\:/i) ? ';' : ':'; # DOS/Win32 or Unix delimiter?

$ENV{PATH} = (( $^O eq 'MSWin32' ) ? $prefix : "$prefix/bin") . ($ENV{PATH} ? $path_delim . $ENV{PATH} : ""); # prefix our path

if(! -x $openssl) {
	my $found = 0;
	foreach (split /$path_delim/, $ENV{PATH}) {
		if(-x "$_/$openssl") {
			$found = 1;
			$openssl = "$_/$openssl";
			last;
		}	
	}
	if($found == 0) {
		print STDERR "c_rehash: rehashing skipped ('openssl' program not available)\n";
		exit 0;
	}
}

if(@ARGV) {
	@dirlist = @ARGV;
} elsif($ENV{SSL_CERT_DIR}) {
	@dirlist = split /$path_delim/, $ENV{SSL_CERT_DIR};
} else {
	$dirlist[0] = "$dir/certs";
}

if (-d $dirlist[0]) {
	chdir $dirlist[0];
	$openssl="$pwd/$openssl" if (!-x $openssl);
	chdir $pwd;
}

foreach (@dirlist) {
	if(-d $_ and -w $_) {
		hash_dir($_);
	}
}

sub hash_dir {
	my %hashlist;
	print "Doing $_[0]\n";
	chdir $_[0];
	opendir(DIR, ".");
	my @flist = readdir(DIR);
	# Delete any existing symbolic links
	foreach (grep {/^[\da-f]+\.r{0,1}\d+$/} @flist) {
		if(-l $_) {
			unlink $_;
		}
	}
	closedir DIR;
	FILE: foreach $fname (grep {/\.pem$/} @flist) {
		# Check to see if certificates and/or CRLs present.
		my ($cert, $crl) = check_file($fname);
		if(!$cert && !$crl) {
			print STDERR "WARNING: $fname does not contain a certificate or CRL: skipping\n";
			next;
		}
		link_hash_cert($fname) if($cert);
		link_hash_crl($fname) if($crl);
	}
}

sub check_file {
	my ($is_cert, $is_crl) = (0,0);
	my $fname = $_[0];
	open IN, $fname;
	while(<IN>) {
		if(/^-----BEGIN (.*)-----/) {
			my $hdr = $1;
			if($hdr =~ /^(X509 |TRUSTED |)CERTIFICATE$/) {
				$is_cert = 1;
				last if($is_crl);
			} elsif($hdr eq "X509 CRL") {
				$is_crl = 1;
				last if($is_cert);
			}
		}
	}
	close IN;
	return ($is_cert, $is_crl);
}


# Link a certificate to its subject name hash value, each hash is of
# the form <hash>.<n> where n is an integer. If the hash value already exists
# then we need to up the value of n, unless its a duplicate in which
# case we skip the link. We check for duplicates by comparing the
# certificate fingerprints

sub link_hash_cert {
		my $fname = $_[0];
		$fname =~ s/'/'\\''/g;
		my ($hash, $fprint) = `"$openssl" x509 -hash -fingerprint -noout -in "$fname"`;
		chomp $hash;
		chomp $fprint;
		$fprint =~ s/^.*=//;
		$fprint =~ tr/://d;
		my $suffix = 0;
		# Search for an unused hash filename
		while(exists $hashlist{"$hash.$suffix"}) {
			# Hash matches: if fingerprint matches its a duplicate cert
			if($hashlist{"$hash.$suffix"} eq $fprint) {
				print STDERR "WARNING: Skipping duplicate certificate $fname\n";
				return;
			}
			$suffix++;
		}
		$hash .= ".$suffix";
		print "$fname => $hash\n";
		$symlink_exists=eval {symlink("",""); 1};
		if ($symlink_exists) {
			symlink $fname, $hash;
		} else {
			open IN,"<$fname" or die "can't open $fname for read";
			open OUT,">$hash" or die "can't open $hash for write";
			binmode IN;
			binmode OUT;
			print OUT <IN> or die "can't write to $hash";	# does the job for small text files
			close OUT or die "can't close $hash";
			close IN;
		}
		$hashlist{$hash} = $fprint;
}

# Same as above except for a CRL. CRL links are of the form <hash>.r<n>

sub link_hash_crl {
		my $fname = $_[0];
		$fname =~ s/'/'\\''/g;
		my ($hash, $fprint) = `"$openssl" crl -hash -fingerprint -noout -in '$fname'`;
		chomp $hash;
		chomp $fprint;
		$fprint =~ s/^.*=//;
		$fprint =~ tr/://d;
		my $suffix = 0;
		# Search for an unused hash filename
		while(exists $hashlist{"$hash.r$suffix"}) {
			# Hash matches: if fingerprint matches its a duplicate cert
			if($hashlist{"$hash.r$suffix"} eq $fprint) {
				print STDERR "WARNING: Skipping duplicate CRL $fname\n";
				return;
			}
			$suffix++;
		}
		$hash .= ".r$suffix";
		print "$fname => $hash\n";
		$symlink_exists=eval {symlink("",""); 1};
		if ($symlink_exists) {
			symlink $fname, $hash;
		} else {
			## system ("cp", $fname, $hash);
			open IN,"<$fname" or die "can't open $fname for read";
			open OUT,">$hash" or die "can't open $hash for write";
			binmode IN;
			binmode OUT;
			print OUT <IN> or die "can't write to $hash";
			close OUT or die "can't close $hash";
			close IN;
		}
		$hashlist{$hash} = $fprint;
}

