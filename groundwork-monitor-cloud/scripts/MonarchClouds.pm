# MonArch - Groundwork Monitor Architect
# MonarchClouds.pm
#
############################################################################
# Release 3.4
# November 2010
############################################################################
#
# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Perl setup.
# ================================================================

use strict;

# To access IO::Uncompress::Unzip and our updated File::Path, we need to set
# the lib path to include the separately-installed cloud Perl modules.  This
# should go away once the separate Perl packages are merged into the base product.
use lib '/usr/local/groundwork/cloud/perl/lib';

use TypedConfig;
use IO::Uncompress::Unzip qw($UnzipError);
use File::Path;

package Clouds;

# We seem to need this inside the Clouds package for the package to see the
# unquoted symbols we need (at least, if we want to use them without prefixing
# them with their Fcntl:: package name).
use Fcntl;

#-----------------------#
# FIX THIS:  This is just a hack until we fold the form_top() and form_top_file() change
# (supporting monarch_clouds.cgi) into MonarchForms.pm, at which point we will drop the
# version here and call that one.

my $cgi_exe = 'monarch.cgi';

my $SERVER_SOFTWARE = $ENV{SERVER_SOFTWARE};

my $monarch_cgi;
my $cloud_cgi;
if ( defined($SERVER_SOFTWARE) && $SERVER_SOFTWARE eq 'TOMCAT' ) {
    $monarch_cgi = '/monarch';
    $cloud_cgi   = '/cloud-connector';
}
elsif ( -e '/usr/local/groundwork/config/db.properties' ) {
    $monarch_cgi = '/monarch/cgi-bin';
    $cloud_cgi   = '/monarch/cgi-bin';
}
else {
    # Standalone Monarch (outside of GW Monitor) is no longer supported.
}
#-----------------------#

# These next variables are copied from MonarchForms.pm, and would be
# eliminated should we ever fold this code into that file.
my $form_class      = 'row1';
my $global_cell_pad = 3;

my %cloud_path = (
    config      => '/usr/local/groundwork/cloud/config',
    credentials => '/usr/local/groundwork/cloud/credentials',
    scripts     => '/usr/local/groundwork/cloud/scripts',
);
my %filedata = ();

# ================================================================
# Temporary subroutines.
# ================================================================

# These routines are just residing here until we can migrate them
# to their permanent home in MonarchForms.pm.

# FIX THIS:  This is just a hack until we fold the change (supporting monarch_clouds.cgi)
# into MonarchForms.pm, at which point we will drop this version and call that one.
sub form_top(@) {
    my $caption         = $_[1];
    my $onsubmit_action = $_[2];
    my $ez              = $_[3];
    my $boxwidth        = $_[4] || '90%';
    my $align           = 'left';
    my $cgi_dir         = $monarch_cgi;
    if ( $ez eq '1' ) { $cgi_dir = $monarch_cgi; $cgi_exe = 'monarch_ez.cgi'; }
    if ( $ez eq '2' ) { $cgi_dir = $monarch_cgi; $cgi_exe = 'monarch_auto.cgi' }
    if ( $ez eq '3' ) { $cgi_dir = $cloud_cgi;   $cgi_exe = 'monarch_clouds.cgi' }
    return qq(@{[&$Instrument::show_trace_as_html_comment()]}
<form name=form action="$cgi_dir/$cgi_exe" method=post $onsubmit_action generator=form_top>
<table class=data width="$boxwidth" cellpadding=0 cellspacing=1 border=0>
<tr>
<td class=data>
<table width="100%" cellpadding=3 cellspacing=0 align=left border=0>
<tr>
<td class=head colspan=3>&nbsp;$caption</td>
</tr>
</table>
</td>
</tr>);
}

# FIX THIS:  Same as for form_top().
sub form_top_file(@) {
    my $caption         = $_[1];
    my $onsubmit_action = $_[2];
    my $ez              = $_[3];
    my $align           = 'left';
    my $cgi_dir         = $monarch_cgi;

    # next line commented out because now the caller provides the ' onsubmit=' along with the action
    #if ($onsubmit_action) { $onsubmit_action = qq(@{[&$Instrument::show_trace_as_html_comment()]} onsubmit="$onsubmit_action") }

    if ( $ez eq '1' ) { $cgi_dir = $monarch_cgi; $cgi_exe = 'monarch_ez.cgi' }
    if ( $ez eq '2' ) { $cgi_dir = $monarch_cgi; $cgi_exe = 'monarch_auto.cgi' }
    if ( $ez eq '3' ) { $cgi_dir = $cloud_cgi;   $cgi_exe = 'monarch_clouds.cgi' }
    return qq(@{[&$Instrument::show_trace_as_html_comment()]}
<form name=form ENCTYPE="multipart/form-data" action="$cgi_dir/$cgi_exe" method=post $onsubmit_action generator=form_top_file>
<table class=data width="90%" cellpadding=0 cellspacing=1 border=0>
<tr>
<td class=data>
<table width="100%" cellpadding=3 cellspacing=0 align=left border=0>
<tr>
<td class=head colspan=3>&nbsp;$caption</td>
</tr>
</table>
</td>
</tr>);
}

# This routine is a derivative of Forms->form_file() that allows for multiple purpose-named files
# to be uploaded in one page, by making separate calls to this function with different names.
sub form_named_file(@) {
    my $title = $_[1];
    my $name  = $_[2];
    my $doc   = $_[3];
    my $tab   = $_[4];
    my $tabindex = $tab ? "tabindex=\"$tab\"" : '';
    my $detail = qq(@{[&$Instrument::show_trace_as_html_comment()]}
<tr>
<td class=data>
<table width="100%" cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width="15%">$title</td>);
    if ($doc) {
	$detail .=
	  "\n<td class=$form_class width='3%' valign=top align=center>\n<a class=orange href='#doc' title=\"$doc\" tabindex=-1>&nbsp;?&nbsp;</a>";
    }
    else {
	$detail .= "\n<td class=$form_class width='3%' align=center>\n&nbsp;";
    }
    $detail .= qq(@{[&$Instrument::show_trace_as_html_comment()]}
<td class=$form_class>
<input type=file name=$name size=60 maxlength=100 $tabindex>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub list_box_keyed(@) {
    my $title    = $_[1];
    my $name     = $_[2];
    my $list     = $_[3];
    my $compare  = $_[4];  # coderef
    my $selected = $_[5];
    my $req      = $_[6];
    my $doc      = $_[7];
    my $override = $_[8];
    my $tab      = $_[9];
    my $tabindex = $tab ? "tabindex=\"$tab\"" : '';
    $req = $req ? '<font color=#CC0000>&nbsp;* required</font>' : '';
    my %list    = %$list;
    my $display = $title;
    $display =~ s/://g;
    if ( $display =~ /^use$/i ) { $display = "template" }
    my $detail = qq(@{[&$Instrument::show_trace_as_html_comment()]}
<tr>
<td class=data>
<table width="100%" cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>);

    if ($override) {
	if ( $override eq 'checked' ) {
	    $detail .= "\n<td class=$form_class width='2%'><input class=$form_class type=checkbox name=$name\_override checked></td>";
	}
	else {
	    $detail .= "\n<td class=$form_class width='2%'><input class=$form_class type=checkbox name=$name\_override></td>";
	}
    }
    $detail .= qq(@{[&$Instrument::show_trace_as_html_comment()]}
<td class=$form_class width="25%">$title</td>);
    if ($doc) {
	$detail .=
	  "\n<td class=$form_class width='3%' align=center>\n<a class=orange href='#doc' title=\"$doc\" tabindex=-1>&nbsp;?&nbsp;</a>\n</td>";
    }
    else {
	$detail .= "\n<td class=$form_class width='3%' align=center>\n&nbsp;</td>";
    }
    $detail .= qq(@{[&$Instrument::show_trace_as_html_comment()]}
<td class=$form_class align=left>
<select name=$name $tabindex> $req);
    if ( !%list ) {
	$detail .= "\n<option selected value=''></option>";
	$detail .= "\n<option value=''>-- no \L$display" . "s --</option>";
    }
    else {
	foreach my $item ( sort $compare keys %list ) {
	    if ( defined($selected) && $item eq $selected ) {
		$detail .= "\n<option selected value=\"$item\">$list{$item}</option>";
	    }
	    else {
		$detail .= "\n<option value=\"$item\">$list{$item}</option>";
	    }
	}
    }
    $detail .= qq(@{[&$Instrument::show_trace_as_html_comment()]}
</select>$req
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

# ================================================================
# Supporting subroutines.
# ================================================================

sub cloud_paths {
    return \%cloud_path;
}

# Report a CGI.pm bug:  this hook gets called for the $filename with $bytes_read == 0 even if the file doesn't exist.
# Is there any way to distinguish this from an empty file being successfully uploaded?
sub file_upload_hook {
    my ($filename, $buffer, $bytes_read, $data) = @_;
    if ($bytes_read > 0) {
	if (not exists $filedata{$filename}) {
	    $filedata{$filename} = [];
	}
	push @{ $filedata{$filename} }, $buffer;
    }
}

sub uploaded_filedata {
    my $filename = $_[1];
    return exists( $filedata{$filename} ) ? join('', @{ $filedata{$filename} }) : undef;
}

sub is_bad_filename {
    my $filename = $_[1];
    # "@" might not be a shell metacharacter, but it is so often interpreted by
    # programs as separating a username and a hostname that we treat it as such.
    return $filename =~ m=[ \[\]<>(){}|~;&!*?\\/\$"'`%^@]=;
}

# Internal routine only, for the time being.
sub write_file {
    my $filepath = $_[0];
    my $filemode = $_[1];
    my $filedata = $_[2];  # arrayref

    my @errors = ();

    # Before opening the file, we get rid of any old copy of the file and any
    # dangerous symlink that might cause us to clobber some unintended file.
    # Then we open with safety checks turned on.
    unlink $filepath;
    if (sysopen (FILE, $filepath, O_WRONLY | O_CREAT | O_EXCL | O_NOFOLLOW, $filemode)) {
	if (not print FILE @$filedata) {
	    push @errors, "Error:  Cannot write to $filepath ($!)";
	    unlink $filepath;
	}
	if (not close FILE) {
	    push @errors, "Error:  $filepath cannot be fully created ($!)";
	    unlink $filepath;
	}
    }
    else {
	push @errors, "Error:  Cannot open $filepath ($!)";
    }

    return \@errors;
}

sub install_eucalyptus_credentials {
    my $region              = $_[1];
    my $credentials_content = $_[2];

    my @errors = ();

    # For a Eucalyptus region, we should have in hand a euca2-admin-x509.zip file, and we want to install its component files:
    #     cloud-cert.pem
    #     euca2-admin-{alnum+}-cert.pem
    #     euca2-admin-{alnum+}-pk.pem
    #     eucarc
    #     jssecacerts
    # under this directory:
    #     $cloud_path{credentials}/$region/
    # Then we run the equivalent of:
    #     cat                     $cloud_path{credentials}/$region/eucarc > $cloud_path{credentials}/$region/access.bash
    #     convert_eucarc_for_tcsh $cloud_path{credentials}/$region/eucarc > $cloud_path{credentials}/$region/access.tcsh
    #     convert_eucarc_for_perl $cloud_path{credentials}/$region/eucarc > $cloud_path{credentials}/$region/access.conf
    # to set up the credentials for our own use.

    my $u = new IO::Uncompress::Unzip \$credentials_content;
    if (not $u) {
	push @errors, "Error:  cannot create an Unzip object for uploaded region \"$region\" credentials.";
    }
    else {
	my $status;
	my %eucalyptus_file_content = ();
	# These filename patterns reflect what is expected for Eucalyptus 1.6.2; later releases might require changes here.
	my %eucalyptus_file_patterns = (
	    qr/^eucarc$/                             => 1,
	    qr/^cloud-cert.pem$/                     => 1,
	    qr/^jssecacerts$/                        => 1,
	    qr/^euca2-admin-(?:[a-z0-9]+)-pk.pem$/   => 1,
	    qr/^euca2-admin-(?:[a-z0-9]+)-cert.pem$/ => 1,
	);
	for ($status = 1; $status > 0 && not $u->eof(); $status = $u->nextStream()) {
	    my $info = $u->getHeaderInfo();
	    if (not defined $info) {
		push @errors, "Error:  Your uploaded credentials file is malformed.";
		push @errors, "Hint:  It should be a ZIP file downloaded as euca2-admin-x509.zip from your Eucalyptus web site.";
		last;
	    }
	    else {
		my $name = $u->getHeaderInfo()->{Name};
		my $is_expected_file = 0;
		# Validate that this is a pure filename, not something with preceding path component(s),
		# and that it is one of the files we need to have in place for this cloud type.
		foreach my $pattern (keys %eucalyptus_file_patterns) {
		    if ($name =~ $pattern) {
			$is_expected_file = 1;
		    }
		}
		if ($is_expected_file) {
		    my $buffer;
		    my @buffer;
		    while (($status = $u->read($buffer)) > 0) {
			push @buffer, $buffer;
		    }
		    if ($status == 0) {
			$eucalyptus_file_content{$name} = join('', @buffer);
		    }
		    else {
			# The error message for this case is shown after the loop.
			# That covers the case of not finding a proper stream as well.
			last;
		    }
		}
		else {
		    push @errors, "Error:  zipfile member $name is an unexpected filename";
		}
	    }
	}
	push @errors, "Error:  error processing uploaded credentials for region \"$region\" ($!)" if $status < 0;
	if (@errors == 0) {
	    if (keys %eucalyptus_file_content == keys %eucalyptus_file_patterns) {
		## Now and only now that we have validated everything, we write out a complete set of files.
		## First, create the region's credentials directory if it doesn't already exist.
		my $credentials_dir = "$cloud_path{credentials}/$region";
		if (! -e $credentials_dir) {
		    mkdir ($credentials_dir, 0755) or push @errors, "Error:  Cannot make directory $credentials_dir ($!)";
		}
		elsif ( ! -d _ ) {
		    push @errors, "Error:  $credentials_dir already exists but is not a directory!";
		}

		# Now that we know we have a clean set of files to install, wipe out any previous credentials
		# for this region, so we don't end up with a mixture of old and new files left in the directory.
		my $errors = remove_region_credentials('', $region, 1);
		push @errors, @$errors if @$errors;

		unless (@errors) {
		    foreach my $name (keys %eucalyptus_file_content) {
			my $credentials_file = "$cloud_path{credentials}/$region/$name";
			if (not open (CREDENTIALS, '>', $credentials_file)) {
			    push @errors, "Error:  Cannot open $credentials_file ($!)";
			}
			else {
			    print CREDENTIALS $eucalyptus_file_content{$name} or push @errors, "Error:  Cannot write to $credentials_file ($!)";
			    close CREDENTIALS or do { push @errors, "Error:  Cannot close $credentials_file ($!)" unless @errors; };
			}
			last if @errors;
		    }
		}
	    }
	    else {
		push @errors, "Error:  did not find all expected files in uploaded credentials for region \"$region\".";
	    }
	}
    }
    unless (@errors) {
	# Create the derivative access.bash file.
	my @access = ();
	if (open(CONVERT, '<', "$cloud_path{credentials}/$region/eucarc")) {
	    @access = <CONVERT>;
	    if (not close CONVERT) {
		push @errors, "Error:  Cannot read the eucarc file ($!)";
	    }
	}
	else {
	    push @errors, "Error:  Cannot open the eucarc file ($!)";
	}
	unless (@errors) {
	    my $errors = write_file ("$cloud_path{credentials}/$region/access.bash", 0644, \@access);
	    push @errors, @$errors if @$errors;
	}
    }
    unless (@errors) {
	# Create the derivative access.tcsh file.
	my @access = ();
	if (open(CONVERT, '-|', "$cloud_path{scripts}/convert_eucarc_for_tcsh", "$cloud_path{credentials}/$region/eucarc")) {
	    while (<CONVERT>) {
		if (/^\s*#/ || /^\s*\w+\s+\S+\s+/) {
		    push @access, $_;
		}
		else {
		    push @errors, 'Error:  eucarc file fails conversion to a tcsh-useable format.';
		    last;
		}
	    }
	    if (not close CONVERT) {
		push @errors, "Error:  Cannot read the content of the converted eucarc file ($!)";
	    }
	}
	else {
	    push @errors, "Error:  Cannot run the convert_eucarc_for_tcsh script ($!)";
	}
	unless (@errors) {
	    my $errors = write_file ("$cloud_path{credentials}/$region/access.tcsh", 0644, \@access);
	    push @errors, @$errors if @$errors;
	}
    }
    unless (@errors) {
	# Create the derivative access.conf file.
	# This should be the last file we generate, because it is the file
	# that we check elsewhere to see if the credentials are installed.
	my @access = ();
	if (open(CONVERT, '-|', "$cloud_path{scripts}/convert_eucarc_for_perl", "$cloud_path{credentials}/$region/eucarc")) {
	    while (<CONVERT>) {
		if (/^\s*#/ || /^\s*\w+\s*=\s*".*"\s*$/) {
		    push @access, $_;
		}
		else {
		    push @errors, 'Error:  eucarc file fails conversion to a Perl-useable format.';
		    last;
		}
	    }
	    if (not close CONVERT) {
		push @errors, "Error:  Cannot read the content of the converted eucarc file ($!)";
	    }
	}
	else {
	    push @errors, "Error:  Cannot run the convert_eucarc_for_perl script ($!)";
	}
	unless (@errors) {
	    my $errors = write_file ("$cloud_path{credentials}/$region/access.conf", 0644, \@access);
	    push @errors, @$errors if @$errors;
	}
    }

    return \@errors;
}

sub install_ec2_credentials {
    my $region           = $_[1];
    my $host             = $_[2];
    my $public_key_file  = $_[3];
    my $public_key       = $_[4];
    my $private_key_file = $_[5];
    my $private_key      = $_[6];

    my @errors = ();

    my @pub_lines  = split /[\r\n]+/, $public_key;
    my @priv_lines = split /[\r\n]+/, $private_key;
    if ($pub_lines[0] ne '-----BEGIN CERTIFICATE-----' || $pub_lines[$#pub_lines] ne '-----END CERTIFICATE-----') {
	push @errors, "Error:  File \"$public_key_file\" does not contain a valid public key.";
    }
    if ($priv_lines[0] ne '-----BEGIN PRIVATE KEY-----' || $priv_lines[$#priv_lines] ne '-----END PRIVATE KEY-----') {
	push @errors, "Error:  File \"$private_key_file\" does not contain a valid private key.";
    }

    # Create the region's credentials directory if it doesn't already exist.
    unless (@errors) {
	my $credentials_dir = "$cloud_path{credentials}/$region";
	if (! -e $credentials_dir) {
	    mkdir ($credentials_dir, 0755) or push @errors, "Error:  Cannot make directory $credentials_dir ($!)";
	}
	elsif ( ! -d _ ) {
	    push @errors, "Error:  $credentials_dir already exists but is not a directory!";
	}
    }

    # Wipe out any previous credentials for this region, so we don't end up with a mixture of
    # old and new files left in the directory.
    unless (@errors) {
	my $errors = remove_region_credentials('', $region, 1);
	push @errors, @$errors if @$errors;
    }

    # Install the key files, using restrictive permissions on the created files.
    unless (@errors) {
	my $errors = write_file ("$cloud_path{credentials}/$region/$public_key_file", 0644, [ $public_key ] );
	push @errors, @$errors if @$errors;
    }
    unless (@errors) {
	my $errors = write_file ("$cloud_path{credentials}/$region/$private_key_file", 0600, [ $private_key ]);
	push @errors, @$errors if @$errors;
    }

    # Create the derivative access.bash file.
    unless (@errors) {
	my @access = ();
	push @access, "export EC2_URL=\"https://$host\"\n";
	push @access, "export EC2_CERT=\"$cloud_path{credentials}/$region/$public_key_file\"\n";
	push @access, "export EC2_PRIVATE_KEY=\"$cloud_path{credentials}/$region/$private_key_file\"\n";
	my $errors = write_file ("$cloud_path{credentials}/$region/access.bash", 0644, \@access);
	push @errors, @$errors if @$errors;
    }
    # Create the derivative access.tcsh file.
    unless (@errors) {
	my @access = ();
	push @access, "setenv EC2_URL         \"https://$host\"\n";
	push @access, "setenv EC2_CERT        \"$cloud_path{credentials}/$region/$public_key_file\"\n";
	push @access, "setenv EC2_PRIVATE_KEY \"$cloud_path{credentials}/$region/$private_key_file\"\n";
	my $errors = write_file ("$cloud_path{credentials}/$region/access.tcsh", 0644, \@access);
	push @errors, @$errors if @$errors;
    }
    # Create the derivative access.conf file.
    # This should be the last file we generate, because it is the file
    # that we check elsewhere to see if the credentials are installed.
    unless (@errors) {
	my @access = ();
	push @access, "EC2_URL         = \"https://$host\"\n";
	push @access, "EC2_CERT        = \"$cloud_path{credentials}/$region/$public_key_file\"\n";
	push @access, "EC2_PRIVATE_KEY = \"$cloud_path{credentials}/$region/$private_key_file\"\n";
	my $errors = write_file ("$cloud_path{credentials}/$region/access.conf", 0644, \@access);
	push @errors, @$errors if @$errors;
    }

    return \@errors;
}

sub remove_region_credentials {
    my $region    = $_[1];
    my $keep_root = $_[2];  # optional parameter
    my $credentials_dir = "$cloud_path{credentials}/$region";
    my @errors = ();

    # First check that $credentials_dir is a directory and not a symlink.  Failure on either count would be evidence of mischief.
    if ( -e $credentials_dir ) {
	if ( -d _ ) {
	    if ( -l $credentials_dir ) {
		push @errors, "Error:  $credentials_dir is a symlink, not a directory.";
	    }
	    else {
		# We remove the old access.conf file first, since that is the file we check elsewhere to see
		# that the credentials are properly installed.
		unlink "$credentials_dir/access.conf";
		# Note:  It is possible for remove_tree() to simply die().  See the File::Path documentation.
		# We could catch that using an eval{} here if need be, but see the doc for why not in general.
		my $errors = undef;
		my %opts = (safe => 1, error => \$errors);
		$opts{keep_root} = 1 if $keep_root;
		File::Path::remove_tree ($credentials_dir, \%opts);
		for my $error (@$errors) {
		    my ($file, $message) = %$error;
		    my $prefix = $file ne '' ? "problem unlinking $file:  " : '';
		    push @errors, "Error:  $prefix$message";
		}
	    }
	}
	else {
	    push @errors, "Error:  $credentials_dir is something other than a directory.";
	}
    }

    return \@errors;
}

sub read_config_file {
    my $cloud_config_file = $_[1];

    my %cloud_config = ();
    my @errors       = ();

    eval {
	my $config = TypedConfig->new($cloud_config_file);
	$cloud_config{enable_cloud_processing}                   = $config->get_boolean('enable_cloud_processing');
	$cloud_config{default_host_profile}                      = $config->get_scalar('default_host_profile');
	$cloud_config{ec2_availability_zone_host_profile}        = $config->get_scalar('ec2_availability_zone_host_profile');
	$cloud_config{eucalyptus_availability_zone_host_profile} = $config->get_scalar('eucalyptus_availability_zone_host_profile');
	$cloud_config{orphaned_hosts_disposition}                = $config->get_scalar('orphaned_hosts_disposition');
	$cloud_config{inactive_hosts_hostgroup}                  = $config->get_scalar('inactive_hosts_hostgroup');
	$cloud_config{orphaned_host_retention_period}            = $config->get_scalar('orphaned_host_retention_period');

	# Convert legacy values.
	if ( $cloud_config{orphaned_host_retention_period} =~ /(\d+) (\w+)/ ) {
	    my ( $quantity, $units ) = ( $1, $2 );
	    my $orphaned_host_retention_period =
	      $quantity *
	      (   ( $units =~ /^hour/ ) ? 3600
		: ( $units =~ /^day/ )   ? 3600 * 24
		: ( $units =~ /^week/ )  ? 3600 * 24 * 7
		: ( $units =~ /^month/ ) ? 3600 * 24 * 31
		: ( $units =~ /^year/ )  ? 3600 * 365
		:                          0 );

	    # Round up to one of the standard periods presented in the UI.
	    my @standard_periods = (
		3600,
		3600 * 2,
		3600 * 6,
		3600 * 12,
		3600 * 24,
		3600 * 24 * 2,
		3600 * 24 * 4,
		3600 * 24 * 7,
		3600 * 24 * 14,
		3600 * 24 * 31,
		3600 * 24 * 61,
		3600 * 24 * 183,
		3600 * 24 * 365,
		3600 * 24 * 99999
	    );
	    foreach my $period (@standard_periods) {
		if ( $orphaned_host_retention_period <= $period ) {
		    $orphaned_host_retention_period = $period;
		    last;
		}
	    }
	    $cloud_config{orphaned_host_retention_period} = $orphaned_host_retention_period;
	}

	my %clouds = $config->get_hash('clouds');
	my %regions = $clouds{'region'} ? %{ $clouds{'region'} } : ();

	foreach my $region ( keys %regions ) {
	    ## $cloud_config{regions}{$region} will be autovivified as a result of these assignments.
	    $cloud_config{regions}{$region}{type}    = $regions{$region}{type};
	    $cloud_config{regions}{$region}{host}    = $regions{$region}{host};
	    $cloud_config{regions}{$region}{enabled} = $regions{$region}{enabled};
	}
    };
    if ($@) {
	$@ =~ s/^ERROR:\s+//i;
	$@ =~ s/\n$//;
	push @errors, "Error:  Cannot read config file $cloud_config_file ($@)";
    }

    return \@errors, \%cloud_config;
}

sub write_config_file {
    my $cloud_config      = $_[1];
    my $cloud_config_file = $_[2];

    # We write to a temporary file, then rename to the final filename, so the entire write looks
    # like an atomic change from the standpoint of a process which tries to read the file.  The
    # reader will see either the old copy or the full new copy, but never an incomplete file.
    my $temp_config_file = "$cloud_config_file.tmp";
    my @errors = ();
    my $status = 1;

    # Before opening the file, we get rid of any old copy of the file and any
    # dangerous symlink that might cause us to clobber some unintended file.
    # Then we open with safety checks turned on.
    unlink $temp_config_file;
    if (!sysopen (CONFIG, $temp_config_file, O_WRONLY | O_CREAT | O_EXCL | O_NOFOLLOW, 0644)) {
	push @errors, "Error:  Cannot open $temp_config_file ($!)";
	return \@errors;
    }

    my $enable_cloud_processing        = $cloud_config->{enable_cloud_processing} ? 'yes' : 'no';
    my $orphaned_host_retention_period = $cloud_config->{orphaned_host_retention_period} || 0;
    $status = print CONFIG <<"EOF";
# ================================================================
# Configuration file for the Cloud Connector configuration
# batch update script (euca-config.pl).
# ================================================================
# DO NOT DIRECTLY EDIT THIS FILE.  It is automatically generated
# by the Monarch UI, and changes made here will be overwritten by
# the UI as option values are modified through that interface.
# ================================================================

# Whether to process anything.  Turn this off if you want to disable cloud
# processing completely.  This option is turned off in the initially installed
# configuration file simply so the software can be safely installed before it
# is locally configured.  To get the software to run, it must be turned on
# here once the rest of the setup is correct for your installation.
# [yes/no]
enable_cloud_processing = $enable_cloud_processing

# The default host profile that will be applied to each machine instance
# if there is no specific host profile that applies to its instance type.
default_host_profile = "$cloud_config->{default_host_profile}"

# The host profile that will be applied to a virtual host that will
# be established to represent an entire EC2 availability zone.
ec2_availability_zone_host_profile = "$cloud_config->{ec2_availability_zone_host_profile}"

# The host profile that will be applied to a virtual host that will
# be established to represent an entire Eucalyptus availability zone.
eucalyptus_availability_zone_host_profile = "$cloud_config->{eucalyptus_availability_zone_host_profile}"

# What to do with orphaned hosts (those that have disappeared from their
# respective availability zones and regions).  Choices are "delete" (remove
# entirely from GroundWork's view), "move" (default; move into the
# inactive_hosts_hostgroup), or "keep" (leave them in the same hostgroups
# as they were in while they were present).
orphaned_hosts_disposition = "$cloud_config->{orphaned_hosts_disposition}"

# The hostgroup into which hosts will be placed if they are not currently
# active, if the orphaned_hosts_disposition is "move".
inactive_hosts_hostgroup = "$cloud_config->{inactive_hosts_hostgroup}"

# How long moved and kept hosts will remain in their hostgroups before
# being deleted.  Specified in seconds.  Expressions (e.g., 3600 * 24 * 7
# for one week) are allowed here.  The user interface may restrict the
# selection to a set of predetermined time intervals.
orphaned_host_retention_period = $orphaned_host_retention_period

# List of clouds to be monitored.
#
# Each "cloud" is known by its region name, per Amazon EC2 standards.
# Eucalyptus has no notion of regions, but it does have the notion of
# a Cloud Controller (CLC), which effectively serves the same purpose.
# However, a ec2-describe-regions listing won't show the values we
# need to use here for a Eucalyptus cloud.  For that type of cloud,
# just use the hostname on which the CLC is running as the region name.

<clouds>

EOF
    push @errors, "Error:  Cannot write to $temp_config_file ($!)" if !$status;

    if ($status) {
	my $regions = $cloud_config->{regions};
	foreach my $region ( sort keys %{ $regions } ) {
	    my $enabled = $regions->{$region}{enabled} ? 'yes' : 'no';
	    $status = print CONFIG <<"EOF";
    <region $region>
	type    = "$regions->{$region}{type}"
	host    = "$regions->{$region}{host}"
	enabled = $enabled
    </region>
EOF
	    if (!$status) {
		push @errors, "Error:  Cannot write to $temp_config_file ($!)";
		last;
	    }
	}
    }

    if ($status) {
	$status = print CONFIG <<"EOF";

</clouds>
EOF
	push @errors, "Error:  Cannot write to $temp_config_file ($!)" if !$status;
    }

    if (close CONFIG) {
	# We roll out a small number of numbered backup copies of the config file.
	if ( -f $cloud_config_file ) {
	    # We don't bother with checking errors on this rollout, since we don't want
	    # any such errors to interfere with installing the new file (whose install
	    # will be error-checked).
	    rename "$cloud_config_file.4", "$cloud_config_file.5";
	    rename "$cloud_config_file.3", "$cloud_config_file.4";
	    rename "$cloud_config_file.2", "$cloud_config_file.3";
	    rename "$cloud_config_file.1", "$cloud_config_file.2";
	    rename  $cloud_config_file,    "$cloud_config_file.1";
	}
	$status = rename $temp_config_file, $cloud_config_file;
	push @errors, "Error:  Cannot rename $temp_config_file as $cloud_config_file ($!)" if !$status;
    }
    else {
	# We suppress this message if we already complained above.
	push @errors, "Error:  Cannot close $temp_config_file ($!)" if $status;
    }

    return \@errors;
}

sub orphaned_hosts_options(@) {
    my $orphaned_hosts_disposition = $_[1];
    my $hostgroups                 = $_[2];
    my $inactive_hosts_hostgroup   = $_[3];
    my $tab                        = $_[4];
    my $tabindex                   = $tab ? "tabindex=\"$tab\"" : '';
    my %selected                   = (
	'delete' => '',
	'move'   => '',
	'keep'   => '',
    );
    if ( !defined($orphaned_hosts_disposition) || !exists($selected{$orphaned_hosts_disposition})) {
	$orphaned_hosts_disposition = 'move';
    }
    if ( !$inactive_hosts_hostgroup ) {
	## FIX MINOR:  this is perhaps not an appropriate place to bury a default value,
	## especially if it didn't get saved that way (which makes it misleading)
	$inactive_hosts_hostgroup = 'Inactive Cloud Hosts';
    }
    $selected{$orphaned_hosts_disposition} = 'checked';
    my $chosen = '';
    my @options = ();
    foreach my $hg (@$hostgroups) {
	$chosen = ($hg eq $inactive_hosts_hostgroup) ? 'selected' : '';
	push @options, "<option value=\"$hg\" $chosen>$hg</option>";
    }
    my $options = join("\n", @options);
    return qq(@{[&$Instrument::show_trace_as_html_comment()]}
<tr>
<td class=data>
<table width="100%" cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=wizard_title valign=top>Orphaned Host Disposition</td>
</tr>
<tr>
<td class=wizard_body>
<table width="100%" cellpadding=7 cellspacing=0 align=left border=0>
<tr>
<td class=wizard_body width="3%" valign=top>
<input class=radio type=radio name=orphaned_hosts_disposition value=delete $selected{'delete'} $tabindex></td>
</td>
<td class=wizard_body colspan="2"><i>Delete</i>:
Deleting a host immediately when it is orphaned will wipe all traces of that host as soon as the instance disappears from the cloud (i.e., when it is terminated).
</td>
</tr>
<tr>
<td class=wizard_body width="3%" valign=top>
<input class=radio type=radio name=orphaned_hosts_disposition value=move $selected{'move'} $tabindex></td>
</td>
<td class=wizard_body colspan="2"><i>Move</i> (default):
Moving the host will transfer it into the specified single inactive-hosts hostgroup, which will apply to all instances regardless of their machine type.
</td>
</tr>
<tr>
<td class=wizard_body width="3%">&nbsp;</td>
<td class=wizard_body colspan="2">Inactive hosts hostgroup:&nbsp;
<select name=inactive_hosts_hostgroup id=inactive_hosts_hostgroup $tabindex>
$options
</select>
</td>
</tr>
<tr>
<td class=wizard_body width="3%" valign=top>
<input class=radio type=radio name=orphaned_hosts_disposition value=keep $selected{'keep'} $tabindex></td>
</td>
<td class=wizard_body colspan="2"><i>Keep</i>:
Keeping the host will cause it to remain in the hostgroup to which it belonged while it was still active.
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>);
}

sub monitored_regions(@) {
    my $regions = defined($_[1]) ? $_[1] : {};
    my $tab     = $_[2];
    my %regions = %$regions;

    my $tabindex = $tab ? "tabindex=\"$tab\"" : '';
    my $next_tab = $tab ? "tabindex=\"".($tab+1)."\"" : '';
    my $detail .= qq(
    <tr>
    <td class=data>
    <table width="100%" cellpadding=7 cellspacing=0 align=left border=0>
	<tr>
	<td class=row1><b>Monitored Clouds</b></td>
	</tr>
	<tr>
	<td class=row1>
	    Configure the clouds that you will monitor.
	    Monitoring of configured clouds may be enabled or disabled on a per-region basis.
	</td>
	</tr>
	<tr>
	<td>
	<table width="100%" cellpadding=5 cellspacing=0 align=left border=0>
	<tr>
	<td class=column_head align=left>Region&nbsp;Name</td>
	<td class=column_head align=left>Cloud&nbsp;Type</td>
	<td class=column_head align=left>Endpoint&nbsp;/&nbsp;Controller</td>
	<td class=column_head align=left>Credentials</td>
	<td class=column_head align=center>Enabled</td>
	<td class=column_head align=right>&nbsp;</td>
	</tr>);
    my $color = 'dk';
    foreach my $region ( sort keys %regions ) {
	$color = $color eq 'lt' ? 'dk' : 'lt';
	my $credentials_needed = !-f "$cloud_path{credentials}/$region/access.conf" ? '&nbsp; (needed)' : '';
	my $checked = $regions{$region}{enabled} ? 'checked' : '';
	$detail .= qq(
	<tr>
	<td class="row_$color">$region<input type=hidden name="region_$region" value="$region"></td>
	<td class="row_$color">$regions{$region}{type}<input type=hidden name="type_$region" value="$regions{$region}{type}"></td>
	<td class="row_$color">$regions{$region}{host}<input type=hidden name="host_$region" value="$regions{$region}{host}"></td>
	<td class="row_$color"><input class="submitbutton" type="submit" name="upload_region_$region" value="Upload" $tabindex>$credentials_needed</td>
	<td class="row_$color" align=center><input type=checkbox name="enable_region_$region" value="$region" $checked $tabindex></td>
	<td class="row_$color" align=right valign=top><input type=submit class=removebutton_$color name="remove_$region" value="remove" $tabindex></td>
	</tr>);
    }
    unless ( keys %regions ) {
	$detail .= qq(
	<tr>
	<td class=row_lt colspan=6>None defined</td>
	</tr>);
    }
    $detail .= qq(
    </table>
    </td>
    </tr>
    <tr>
    <td>
    <table width="100%" cellpadding=7 cellspacing=0 align=left border=0>
	<tr>
	<td class=data>
	<table width="100%" cellpadding=3 cellspacing=0 align=left border=0>
	    <tr>
	    <td class=row1 align=left colspan=5>
	    To configure another cloud, enter the data requested and click Add.
	    <p class=append>
	    <ul>
	    <li>
	    An EC2 region name must be specified exactly as it is used within EC2.
	    Note that we really do mean a region (e.g., <b>us-east-1</b>), not an availability zone (e.g., <b>us-east-1b</b>).
	    </li>
	    <li>Eucalyptus has no explicit "region" concept, so use a descriptive name for this cloud instead.</li>
	    <li>A region name should not include spaces or shell metacharacters.</li>
	    <li>Region names must be unique across all endpoints/controllers.</li>
	    <li>For an EC2 region, the Endpoint/Controller is the
	    <a target="_blank"
	    href="http://docs.amazonwebservices.com/AWSEC2/latest/DeveloperGuide/concepts-regions-availability-zones.html">
	    region service endpoint</a> machine.
	    (See the full <a target="_blank" href="http://aws.amazon.com/articles/3912">list of Amazon EC2 Endpoints</a>.)
	    This hostname must be fully qualified, and will look something like <b>us-east-1.ec2.amazonaws.com</b>
	    or <b>ec2.us-east-1.amazonaws.com</b>.</li>
	    <li>For a Eucalyptus region, the Endpoint/Controller is the machine that runs a Eucalyptus Cloud Controller.</li>
	    </ul>
	    </p>
	    <hr>
	    </td>
	    </tr>
	    <tr>
	    <td class=row1>Region&nbsp;name:&nbsp;</td>
	    <td class=row1 align=left><input type=text size=30 name=new_region value="" $tabindex></td>
	    <td class=row1>&nbsp;&nbsp;Cloud&nbsp;type:&nbsp;</td>
	    <td class=row1 align=left>
		<select name=new_type id=new_type $tabindex>);
		    my @types = qw(Eucalyptus EC2);
		    @types = sort { lc($a) cmp lc($b) } @types;
		    foreach my $type (@types) {
			$detail .= "\n<option value=\"$type\">$type</option>";
		    }
		    $detail .= qq(@{[&$Instrument::show_trace_as_html_comment()]}
		</select>
	    </td>
	    <td rowspan=2 class=row1 align=left width="65%">&nbsp;&nbsp;<input class="submitbutton" type="submit" name="add_region" value="Add" $next_tab></td>
	    </tr>
	    <tr>
	    <td class=row1>Endpoint&nbsp;/&nbsp;Controller:&nbsp;</td>
	    <td class=row1 colspan=3 align=left><input type=text size=54 name=new_host value="" $tabindex></td>
	    </tr>
	    <tr>
	    <td class=row1 align=left colspan=5>
	    <hr>
	    You will then need to upload credentials and enable the region to begin actively monitoring this new region.
	    </td>
	    </tr>
	</table>
	</td>
	</tr>
    </table>
    </td>
    </tr>
</table>
</td>
</tr>);
}

sub orphaned_hosts(@) {
    my $instances = defined($_[1]) ? $_[1] : {};
    my $tab       = $_[2];
    my %instances = %$instances;

    my $tabindex = $tab ? "tabindex=\"$tab\"" : '';
    my $next_tab = $tab ? "tabindex=\"".($tab+1)."\"" : '';
    my $detail .= qq(
    <tr>
    <td class=data>
    <table width="100%" cellpadding=7 cellspacing=0 align=left border=0>
	<tr>
	<td>
	<table width="100%" cellpadding=5 cellspacing=0 align=left border=0>
	<tr>
	<td class=column_head align=left width=15px><input class=checkbox_black type=checkbox name=choose_instances id=choose_instances value="" onClick="chooseInstances()"></td>
	<td class=column_head align=left>Region</td>
	<td class=column_head align=left>Machine&nbsp;Image</td>
	<td class=column_head align=left>Instance</td>
	<td class=column_head align=left>Address</td>
	<td class=column_head align=left>Hostgroup(s)</td>
	<td class=column_head align=left>Host&nbsp;Profile</td>
	<td class=column_head align=left>Status</td>
	<td class=column_head align=left>Deactivation&nbsp;Time</td>
	</tr>
<script language=JavaScript>
function chooseInstances()
{
  box = eval("document.form.choose_instances");
  if (box.checked == false) {
    with (document.form) {
      for (var i=0; i < elements.length; i++) {
	if (elements[i].type == 'checkbox' && (elements[i].id == 'instance_checked'))
	  elements[i].checked = false;
      }
    }
  } else {
    with (document.form) {
      for (var i=0; i < elements.length; i++) {
	if (elements[i].type == 'checkbox' && (elements[i].id == 'instance_checked'))
	  elements[i].checked = true;
      }
    }
  }
}
</script>);
    my $color = 'dk';
    # The default sort order should be:
    #     Region, Image, Status, Deactivation Time, Instance
    # with ascending values for each sort field.
    my @instances = sort {
	my $A = $instances{$a};
	my $B = $instances{$b};
	$A->{region}           cmp $B->{region}           ||
	$A->{image}            cmp $B->{image}            ||
	$A->{status}           cmp $B->{status}           ||
	$A->{deactivationtime} cmp $B->{deactivationtime} ||
	$A->{hostname}         cmp $B->{hostname}
    } keys %instances;
    foreach my $instance (@instances) {
	my $orphan = $instances{$instance};
	$color = $color eq 'lt' ? 'dk' : 'lt';
	my $checked = $orphan->{enabled} ? 'checked' : '';
	$detail .= qq(
	<tr>
	<td class="row_$color"><input type=checkbox name=instance id=instance_checked value="$instance" $checked $tabindex></td>
	<td class="row_$color">$orphan->{region}</td>
	<td class="row_$color">$orphan->{image}</td>
	<td class="row_$color">$orphan->{hostname}</td>
	<td class="row_$color">$orphan->{address}</td>
	<td class="row_$color">$orphan->{hostgroups}</td>
	<td class="row_$color">$orphan->{hostprofile}</td>
	<td class="row_$color">$orphan->{status}</td>
	<td class="row_$color">$orphan->{deactivationtime}</td>
	</tr>);
    }
    unless ( keys %instances ) {
	$detail .= qq(
	<tr>
	<td class=row_lt colspan=9>There are no orphaned hosts.</td>
	</tr>);
    }
    $detail .= qq(
    </table>
    </td>
    </tr>
</table>
</td>
</tr>);
}

1;

