#!/usr/local/groundwork/perl/bin/perl -w --
# MonArch - Groundwork Monitor Architect
# monarch_clouds.cgi
#
############################################################################
# Release 3.4
# November 2010
############################################################################
#
# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

use strict;

use CGI;
$CGI::POST_MAX = 1024 * 1024;  # max 1M posts, for security

use lib qq(/usr/local/groundwork/core/monarch/lib);
use MonarchClouds;
use MonarchForms;
use MonarchStorProc;
use MonarchDoc;
use MonarchValidation;
use MonarchInstrument;
use URI::Escape;
use Fcntl;
$|++;

#
############################################################################
# Global Declarations
#

my $debug = undef;

# Uncomment this next line to spill out details of each query at the end of the result screen.
# $debug = 'Query Parameters:';

# This parameter might need local tuning under adverse circumstances.
my $max_commit_lock_attempts = 20;

my @errors    = ();
my $query     = CGI->new(\&Clouds::file_upload_hook, undef, 0);;
my $cgi_error = $query->cgi_error();

if (defined($cgi_error)) {
    my $page = "Content-type: text/html\n\n";
    $page .= Forms->header( 'Error', 'no_session', 'no_menu' );
    $page .= Forms->form_top( 'Interaction Error', '' );
    if ($cgi_error eq '413 Request entity too large') {
	# We exceeded POST_MAX.
	push @errors, 'You have tried to upload a file which is too large.';
    }
    elsif ($cgi_error eq '400 Bad request (malformed multipart POST)') {
	push @errors, 'A file upload has failed.';
    }
    else {
	push @errors, $cgi_error;
    }
    $page .= Forms->form_errors( \@errors );
    $page .= Forms->footer();
    print $page;
    exit 0;
}

our $shutdown_requested = 0;

my $top_menu   = $query->param('top_menu');
my $view       = $query->param('view');
my $obj        = $query->param('obj');
my $obj_view   = $query->param('obj_view');
my $task       = $query->param('task');
my $submit     = $query->param('submit');
my $user_acct  = $query->param('user_acct');
my $password   = $query->param('password');
my $session_id = $query->param('CGISESSID');
unless ($session_id) { $session_id = $query->cookie("CGISESSID") }
my $ez         = $query->param('ez');
my $page_title = 'Monarch';
my ( %auth_add, %auth_modify, %auth_delete, %properties, %authentication, %hidden ) = ();
my (
    $nagios_ver, $nagios_bin,  $nagios_etc,       $monarch_home,  $backup_dir, $is_portal,
    $upload_dir, $monarch_ver, $enable_externals, $enable_groups, $enable_ez,  $nagios_share
) = 0;
$hidden{'selected'} = $query->param('selected');
$hidden{'top_menu'} = $query->param('top_menu');
$hidden{'view'}     = $view;
$hidden{'obj'}      = $obj;
$hidden{'nocache'}  = time;
unless ($top_menu) { $top_menu = 'hosts' }
if ( $task && $task ne 'No' ) { $hidden{'task'} = $task }
my $table = $obj;
if ( $obj eq 'commands' ) { $table = 'commands' }
my @messages = ();
my ( $body, $javascript ) = undef;
my $doc_root     = $ENV{'DOCUMENT_ROOT'};
my $userid       = undef;
my $refresh_url  = undef;
my $refresh_left = undef;
my $tab          = 1;

my %property_list = StorProc->property_list();
my %db_values     = StorProc->db_values();

# Some buttons

my %add                 = ( 'name' => 'add',                 'value' => 'Add' );
my %save                = ( 'name' => 'save',                'value' => 'Save' );
my %refresh             = ( 'name' => 'refresh',             'value' => 'Refresh' );
my %delete              = ( 'name' => 'delete',              'value' => 'Delete' );
my %remove              = ( 'name' => 'remove',              'value' => 'Remove' );
my %select              = ( 'name' => 'select',              'value' => 'Select' );
my %cancel              = ( 'name' => 'cancel',              'value' => 'Cancel' );
my %close               = ( 'name' => 'close',               'value' => 'Close' );
my %next                = ( 'name' => 'next',                'value' => 'Next >>' );
my %back                = ( 'name' => 'back',                'value' => '<< Back' );
my %continue            = ( 'name' => 'continue',            'value' => 'Continue' );
my %abort               = ( 'name' => 'abort',               'value' => 'Abort' );
my %host_vitals         = ( 'name' => 'obj_view',            'value' => 'Host Vitals' );
my %service_list        = ( 'name' => 'obj_view',            'value' => 'Service List' );
my %service_detail      = ( 'name' => 'obj_view',            'value' => 'Service Detail' );
my %rename              = ( 'name' => 'rename',              'value' => 'Rename' );
my %yes                 = ( 'name' => 'yes',                 'value' => 'Yes' );
my %no                  = ( 'name' => 'no',                  'value' => 'No' );
my %save_contact_groups = ( 'name' => 'save_contact_groups', 'value' => 'Save' );

# my %assign_contacts = ( 'name' => 'assign_contacts', 'value' => 'Assign Contacts' );
my %upload  = ( 'name' => 'upload',       'value' => 'Upload' );
my %sync    = ( 'name' => 'sync',         'value' => 'Sync With Main' );
my %default = ( 'name' => 'set_defaults', 'value' => 'Set Defaults' );

#
############################################################################
# Clouds
#

sub clouds() {
    my $page = undef;

    my $cloud_path        = Clouds->cloud_paths();
    my $cloud_config_file = "$cloud_path->{config}/cloud_connector.conf";

    my $got_form = 0;

    if ( $query->param('close') ) {
	$page .= Forms->header( $page_title, $session_id, $top_menu );
	$obj = 'close';
    }
    elsif ( $query->param('continue') ) {
	$page .= Forms->header( $page_title, $session_id, $top_menu );
	$obj = 'connector';
    }
    elsif ( $query->param('cancel') ) {
	foreach my $name ( $query->param ) {
	    if ( $name =~ /^upload_region_(.+)/ ) {
		$query->delete($name);
		last;
	    }
	}
    }
    elsif ( $query->param('upload') ) {
	# We cannot use StorProc->upload() because it assumes the incoming file is text and
	# unconditionally performs a dos2unix filtering, which could corrupt a binary file.
	my $region = $query->param('region');
	my $type   = $query->param("type_$region");
	my $host   = $query->param("host_$region");

	if ($type eq 'Eucalyptus') {
	    my $file = $query->param('upload_file');
	    if ($file eq '') {
		push @errors, 'Error:  No file was specified for uploading.';
	    }
	    else {
		my $filedata = Clouds->uploaded_filedata($file);
		if (defined $filedata) {
		    my $errors = Clouds->install_eucalyptus_credentials($region, $filedata);
		    push @errors, @$errors if @$errors;
		}
		else {
		    push @errors, "Upload of file \"$file\" failed.";
		}
	    }
	}
	elsif ($type eq 'EC2') {
	    my $public_key_file  = $query->param('public_key_file');
	    my $private_key_file = $query->param('private_key_file');
	    if (!defined($public_key_file) || !defined($private_key_file) || $public_key_file eq '' || $private_key_file eq '') {
		push @errors, 'Error:  Both key files must be specified for uploading.';
	    }
	    else {
		if (Clouds->is_bad_filename($public_key_file)) {
		    push @errors, "Error:  The public key filename must not contain spaces or shell metacharacters.";
		}
		if (Clouds->is_bad_filename($private_key_file)) {
		    push @errors, "Error:  The private key filename must not contain spaces or shell metacharacters.";
		}
		if ($public_key_file eq $private_key_file) {
		    push @errors, "Error:  The public and private keys cannot be the same filename.";
		}
		if ($public_key_file =~ /^pk-[A-Z0-9]+\.pem$/) {
		    push @errors, "You have specified a private key filename ($public_key_file) for the public key.";
		}
		if ($private_key_file =~ /^cert-[A-Z0-9]+\.pem$/) {
		    push @errors, "You have specified a public key filename ($private_key_file) for the private key.";
		}
		unless (@errors) {
		    my $public_key  = Clouds->uploaded_filedata($public_key_file);
		    my $private_key = Clouds->uploaded_filedata($private_key_file);
		    # These checks suffice to identify both non-existent files and empty files.
		    if (not defined $public_key) {
			push @errors, "Error:  Upload of public key file \"$public_key_file\" failed.";
		    }
		    if (not defined $private_key) {
			push @errors, "Error:  Upload of private key file \"$private_key_file\" failed.";
		    }
		    unless (@errors) {
			my $errors = Clouds->install_ec2_credentials($region, $host, $public_key_file, $public_key, $private_key_file, $private_key);
			push @errors, @$errors if @$errors;
		    }
		}
	    }
	}
	else {
	    push @errors, "Error:  Cloud type \"$type\" is not supported.";
	}

	$query->delete("upload_region_$region") if !@errors;
    }
    elsif ( $query->param('delete') ) {
	my @instances = $query->param('instance');
	foreach my $instance (@instances) {
	    my $result = StorProc->delete_all( 'hosts', 'name', $instance );
	    if ( $result =~ /^Error/ ) { push @errors, $result }
	}
    }

    my @output = ();
    my @orphan_docs = (
	'The Orphaned Host Disposition settings in the Cloud Configuration screen',
	'allow you to manage terminated instances automatically.',
	'However, you might sometimes wish to manually delete some instances before',
	'the orphaned host retention period you selected there has expired.',
	'This screen allows you to quickly identify and delete such cloud hosts.',
	'These deletions will occur during the next Commit operation',
	'(perhaps one which is run during cloud-configuration batch processing).',
	'<p class=append>',
	'Terminated instances will not show up here until after a subsequent run of the',
	'cloud-configuration batch processing, whether automatically or manually initiated.',
	'That is when the Deactivation Time is assigned.',
	'</p>',
	'<p class=append>',
	'Instances shown with no region are no longer being claimed by any of the configured regions.',
	'This may be because the instance has been down for a long time,',
	'or because the cloud server has been bounced and no longer knows of the instance,',
	'or because the cloud server itself is currently down or inaccessible.',
	'</p>',
	'<p class=append>',
	'Select the instances you wish to delete, then click the Delete button.',
	'For convenience, the checkbox in the heading allows you to select or clear',
	'all the individual checkboxes at once.',
	'</p>',
    );

    if ( $obj eq 'connector' ) {
	if ( $query->param('reset') ) {
	    ## Nothing to do here, except block any other action from occurring before the page is created.
	}
	elsif ( $query->param('save') ) {
	    my %cloud_config = ();
	    $cloud_config{enable_cloud_processing}                   = StorProc->sanitize_string( $query->param('enable_cloud_processing') );
	    $cloud_config{default_host_profile}                      = StorProc->sanitize_string( $query->param('default_host_profile') );
	    $cloud_config{ec2_availability_zone_host_profile}        = StorProc->sanitize_string( $query->param('ec2_availability_zone_host_profile') );
	    $cloud_config{eucalyptus_availability_zone_host_profile} = StorProc->sanitize_string( $query->param('eucalyptus_availability_zone_host_profile') );
	    $cloud_config{orphaned_hosts_disposition}                = StorProc->sanitize_string( $query->param('orphaned_hosts_disposition') );
	    $cloud_config{inactive_hosts_hostgroup}                  = StorProc->sanitize_string( $query->param('inactive_hosts_hostgroup') );
	    $cloud_config{orphaned_host_retention_period}            = StorProc->sanitize_string( $query->param('orphaned_host_retention_period') );

	    $cloud_config{enable_cloud_processing}            = 0  if not defined $cloud_config{enable_cloud_processing};
	    $cloud_config{default_host_profile}               = '' if not defined $cloud_config{default_host_profile};
	    $cloud_config{ec2_availability_zone_host_profile} = '' if not defined $cloud_config{ec2_availability_zone_host_profile};
	    $cloud_config{eucalyptus_availability_zone_host_profile} = ''
	      if not defined $cloud_config{eucalyptus_availability_zone_host_profile};
	    $cloud_config{orphaned_hosts_disposition} = 'move' if not defined $cloud_config{orphaned_hosts_disposition};
	    $cloud_config{inactive_hosts_hostgroup}   = ''     if not defined $cloud_config{inactive_hosts_hostgroup};

	    # FIX MINOR:  possibly add additional sanitizization of the incoming string values,
	    # including stripping out backslashes and quotes, and possibly spaces from some fields

	    foreach my $name ( $query->param ) {
		if ( $name =~ /^region_(.+)/ ) {
		    my $region = StorProc->sanitize_string( $1 );
		    if ( not exists $cloud_config{regions}{$region} ) {
			$cloud_config{regions}{$region} = {};
		    }
		}
		elsif ( $name =~ /^type_(.+)/ ) {
		    my $region = StorProc->sanitize_string( $1 );
		    $cloud_config{regions}{$region}{type} = StorProc->sanitize_string( $query->param($name) );
		}
		elsif ( $name =~ /^host_(.+)/ ) {
		    my $region = StorProc->sanitize_string( $1 );
		    $cloud_config{regions}{$region}{host} = StorProc->sanitize_string( $query->param($name) );
		}
		elsif ( $name =~ /^enable_region_(.+)/ ) {
		    my $region = StorProc->sanitize_string( $1 );
		    $cloud_config{regions}{$region}{enabled} = StorProc->sanitize_string( $query->param($name) );
		}
	    }
	    foreach my $region ( keys %{ $cloud_config{regions} } ) {
		$cloud_config{regions}{$region}{type}    = 'unknown' if not exists $cloud_config{regions}{$region}{type};
		$cloud_config{regions}{$region}{host}    = ''        if not exists $cloud_config{regions}{$region}{host};
		$cloud_config{regions}{$region}{enabled} = 0         if not exists $cloud_config{regions}{$region}{enabled};
		if ( !defined($region) || $region =~ /^\s*$/ ) {
		    delete $cloud_config{regions}{$region};
		}
		elsif ( $cloud_config{regions}{$region}{enabled} && !-f "$cloud_path->{credentials}/$region/access.conf" ) {

		    # FIX MINOR
		    # This is a soft error that, by itself, should not cause the writing of the config file to be denied,
		    # since we revert the bad setting before performing the write operation.
		    push @errors, "Region \"$region\" cannot be enabled until its access credentials are uploaded.";
		    $cloud_config{regions}{$region}{enabled} = 0;
		}
	    }
	    if ( $cloud_config{default_host_profile} eq '' ) {
		push @errors, "You must select a default host profile (typically, \"host-profile-cloud-machine-default\").";
	    }
	    if ( $cloud_config{ec2_availability_zone_host_profile} eq '' ) {
		push @errors, "You must select an availability zone host profile for EC2 regions (typically, \"host-profile-ec2-availability-zone\").";
	    }
	    if ( $cloud_config{eucalyptus_availability_zone_host_profile} eq '' ) {
		push @errors, "You must select an availability zone host profile for Eucalyptus regions (typically, \"host-profile-eucalyptus-availability-zone\").";
	    }
	    if ( $cloud_config{orphaned_hosts_disposition} eq 'move' && $cloud_config{inactive_hosts_hostgroup} eq '' ) {
		push @errors,
"To use \"Move\" as the orphaned host disposition, you must select an inactive hosts hostgroup (typically, \"Inactive Cloud Hosts\").";
	    }

	    unless (@errors) {
		my $errors = Clouds->write_config_file( \%cloud_config, $cloud_config_file );
		push @errors, @$errors if @$errors;
	    }
	    unless (@errors) {
		my $form_title = 'Cloud Connector Configuration';
		$page .= Forms->header( $page_title, $session_id, $top_menu );
		$page .= Clouds->form_top( $form_title, '', '3' );
		$page .= Forms->display_hidden( 'Saved:', '', 'Changes to the Cloud Connector configuration have been saved.' );
		$page .= Forms->hidden( \%hidden );
		$page .= Forms->form_bottom_buttons( \%continue, $tab++ );
		$got_form = 1;
	    }
	}
	elsif ( $query->param('refreshed') ) {
	    # FIX MINOR:  re-work this to capture both the output and the exit status,
	    # by using a piped open() instead of backslashes, and then carefully checking
	    # the return value from close(), $!, and $?
	    @output = `/usr/local/groundwork/cloud/scripts/cloud_config.pl -i -o 2>&1`;
	    if (not @output) {
		push @output, "Cloud configuration processing is done.";
	    }
	    $obj = 'results';
	}
	elsif ( $query->param('batch') ) {
	    $obj = 'run';
	}
	elsif ( $query->param('add_region') ) {
	    my $new_region = StorProc->sanitize_string( $query->param('new_region') );
	    my $new_type   = StorProc->sanitize_string( $query->param('new_type') );
	    my $new_host   = StorProc->sanitize_string( $query->param('new_host') );
	    my ($errors, $cloud_config) = Clouds->read_config_file( $cloud_config_file );
	    push @errors, @$errors if @$errors;
	    unless (@errors) {
		if ($new_region eq '') {
		    push @errors, "A region name must be non-blank.";
		}
		elsif (Clouds->is_bad_filename($new_region)) {
		    push @errors, "A region name must not contain spaces or shell metacharacters.";
		}
		if ( !defined($new_type) || ($new_type ne 'Eucalyptus' && $new_type ne 'EC2') ) {
		    push @errors, "The cloud type must be either EC2 or Eucalyptus.";
		}
		if ($new_host eq '') {
		    push @errors, "A region endpoint/controller must be non-blank.";
		}
		elsif ($new_host !~ /^[A-Za-z0-9_](?:[-.A-Za-z0-9_]*[A-Za-z0-9_])?$/) {
		    push @errors, "A region endpoint/controller must be a valid hostname or IP address.";
		}
		elsif ($new_type eq 'EC2' && $new_host !~ /.\.amazonaws.com$/) {
		    push @errors, "An EC2 service endpoint must be in the <b>amazonaws.com</b> domain.";
		}
	    }
	    unless (@errors) {
		if (exists $cloud_config->{regions}{$new_region}) {
		    push @errors, "Region \"$new_region\" already exists.";
		}
	    }
	    unless (@errors) {
		# We don't bother making the credentials directory for the new region right away,
		# because that will be taken care of when we upload credentials.
		$cloud_config->{regions}{$new_region}{type}    = $new_type;
		$cloud_config->{regions}{$new_region}{host}    = $new_host;
		$cloud_config->{regions}{$new_region}{enabled} = 0;
		my $errors = Clouds->write_config_file($cloud_config, $cloud_config_file);
		push @errors, @$errors if @$errors;
	    }
	}
	else {
	    foreach my $name ( $query->param ) {
		if ( $name =~ /^upload_region_(.+)/ ) {
		    my $region = $1;
		    my $form_title = 'Cloud Access Credentials';
		    my $type = $query->param("type_$region");
		    my $host = $query->param("host_$region");
		    my $good_cloud_type = defined($type) && ($type eq 'Eucalyptus' || $type eq 'EC2');
		    if (!$good_cloud_type) {
			push @errors, "Error:  Cloud type \"$type\" is not supported.";
		    }
		    $page .= Forms->header( $page_title, $session_id, $top_menu );
		    $page .= Clouds->form_top_file( "$form_title Upload", '', '3' );
		    $page .= Forms->form_errors( \@errors ) if @errors;
		    if ($type eq 'Eucalyptus') {
			$page .= Forms->wizard_doc('Upload Eucalyptus credentials file',
			    "Select the credentials file you wish to install for this region. This will be a ZIP file named <b>euca2-admin-x509.zip</b> which is available for download from the Eucalyptus web page immediately after login (typically, at <a target='_blank' href='https://$host:8443/'>https://$host:8443/</a>, then Download Credentials). The set of values in your file will replace the complete set of access values for this region.");
		    }
		    elsif ($type eq 'EC2') {
			$page .= Forms->wizard_doc('Upload EC2 credentials files',
			    'Select the access credentials files you wish to install for this region. This will be a pair of public/private key files with original names like <b>cert-NLHHFJVPP42WF7KMIVAF66QQEHLOIOQ6.pem</b> and <b>pk-NLHHFJVPP42WF7KMIVAF66QQEHLOIOQ6.pem</b>, respectively. (They may have been renamed on your system.)<p class="append">These files are available for download as X.509 Certificates from your Amazon Web Services account (<a href="http://aws.amazon.com/account/">http://aws.amazon.com/account/</a>). They must be installed as a matched pair.</p>');
		    }
		    $page .= Forms->display_hidden( 'Region name:', "upload_region_$region", $region );
		    $page .= Forms->display_hidden( 'Cloud type:', "type_$region", $type );
		    if ($type eq 'Eucalyptus') {
			$page .= Forms->display_hidden( 'Cloud controller:', "host_$region", $host );
			$page .= Forms->form_file( $tab++ );
		    }
		    elsif ($type eq 'EC2') {
			my %docs = ();
			$docs{public_key_file} =
			  'A file containing a public key, usually named something like: cert-NLHHFJVPP42WF7KMIVAF66QQEHLOIOQ6.pem';
			$docs{private_key_file} =
			  'A file containing a private key, usually named something like: pk-NLHHFJVPP42WF7KMIVAF66QQEHLOIOQ6.pem';
			$page .= Forms->display_hidden( 'Service endpoint:', "host_$region", $host );
			$page .= Clouds->form_named_file( 'Public key file:', 'public_key_file', $docs{public_key_file}, $tab++ );
			$page .= Clouds->form_named_file( 'Private key file:', 'private_key_file', $docs{private_key_file}, $tab++ );
		    }
		    $hidden{region} = $region;
		    $page .= Forms->hidden( \%hidden );
		    if ($good_cloud_type) {
			$page .= Forms->form_bottom_buttons( \%upload, \%cancel, $tab++ );
		    }
		    else {
			$page .= Forms->form_bottom_buttons( \%cancel, $tab++ );
		    }
		    $got_form = 1;
		    last;
		}
		elsif ( $name =~ /^remove_(.+)/ ) {
		    my $region = $1;
		    my ($errors, $cloud_config) = Clouds->read_config_file( $cloud_config_file );
		    push @errors, @$errors if @$errors;
		    unless (@errors) {
			delete $cloud_config->{regions}{$region};
			my $errors = Clouds->write_config_file($cloud_config, $cloud_config_file);
			push @errors, @$errors if @$errors;
			# We wait until after updating the config file to remove the credentials, because of the possibility
			# that this routine could die.  We at least want to carry out the basic requested operation in any case.
			$errors = Clouds->remove_region_credentials($region);
			push @errors, @$errors if @$errors;
		    }
		    last;
		}
	    }
	}
    }
    elsif ( $obj eq 'orphaned_hosts' ) {
	if ( $query->param('find_orphans') ) {
	    # FIX MINOR:  re-work this to capture both the output and the exit status,
	    # by using a piped open() instead of backslashes, and then carefully checking
	    # the return value from close(), $!, and $?
	    @output = `/usr/local/groundwork/cloud/scripts/cloud_config.pl -i -o -z 2>&1`;
	    @output = grep /^orphan#/, @output;
	    $obj = 'show_orphaned_hosts';
	}
    }
    unless ($got_form) {
	if ( $obj eq 'connector' ) {
	    # FIX LATER:  Call Forms->form_top() instead, once we have extended it to support monarch_clouds.cgi.
	    $page .= Forms->header( $page_title, $session_id, $top_menu );
	    my $form_title = 'Cloud Connector Configuration';
	    $page .= Clouds->form_top( $form_title, '', '3' );
	    my ( $errors, $cloud_config ) = Clouds->read_config_file($cloud_config_file);

	    # We allow for the possibility that we may have had errors earlier in this routine.
	    push @errors, @$errors if @$errors;
	    $page .= Forms->form_errors( \@errors ) if @errors;

	    # FIX MINOR:  move these doc elements into MonarchClouds.pm, and someday into MonarchDoc.pm
	    my %docs = ();
	    $docs{enable_cloud_processing} = 'Cloud connector processing can be enabled or disabled at a global level, for all clouds.';
	    $docs{default_host_profile} =
'The default host profile that will be applied to each machine instance if there is no specific host profile that applies to its instance type. A typical choice is host-profile-cloud-machine-default.';
	    $docs{ec2_availability_zone_host_profile} =
	      'The host profile that will be applied to a virtual host that will be established to represent an entire EC2 availability zone. The typical choice is host-profile-ec2-availability-zone.';
	    $docs{eucalyptus_availability_zone_host_profile} =
'The host profile that will be applied to a virtual host that will be established to represent an entire Eucalyptus availability zone. The typical choice is host-profile-eucalyptus-availability-zone.';
	    $docs{orphaned_host_retention_period} =
'How long orphaned hosts should be retained in the inactive hosts hostgroup or in their respective original hostgroups before being automatically deleted.';
	    $page .= Forms->checkbox(
		'Enable cloud processing:',
		'enable_cloud_processing',
		$cloud_config->{enable_cloud_processing},
		$docs{'enable_cloud_processing'},
		'', $tab++
	    );
	    $page .= Forms->wizard_doc( 'Host Profiles',
		'These host profiles are automatically applied by the cloud connector in certain circumstances.' );
	    my @host_profiles = StorProc->fetch_list( 'profiles_host', 'name' );
	    my @default_host_profiles =
		grep !/^(host-profile-ec2-availability-zone|host-profile-eucalyptus-availability-zone)$/, @host_profiles;
	    my @ec2_zone_host_profiles =
		grep !/^(host-profile-cloud-machine-default|host-profile-eucalyptus-availability-zone)$/, @host_profiles;
	    my @eucalyptus_zone_host_profiles =
		grep !/^(host-profile-cloud-machine-default|host-profile-ec2-availability-zone)$/, @host_profiles;
	    $page .= Forms->list_box(
		'Default host profile:',
		'default_host_profile', \@default_host_profiles, $cloud_config->{default_host_profile},
		'', $docs{'default_host_profile'},
		'', $tab++
	    );
	    $page .= Forms->list_box(
		'EC2 availability zone host profile:',
		'ec2_availability_zone_host_profile',
		\@ec2_zone_host_profiles, $cloud_config->{ec2_availability_zone_host_profile},
		'', $docs{'ec2_availability_zone_host_profile'},
		'', $tab++
	    );
	    $page .= Forms->list_box(
		'Eucalyptus availability zone host profile:',
		'eucalyptus_availability_zone_host_profile',
		\@eucalyptus_zone_host_profiles, $cloud_config->{eucalyptus_availability_zone_host_profile},
		'', $docs{'eucalyptus_availability_zone_host_profile'},
		'', $tab++
	    );
	    my $orphaned_hosts_disposition = $cloud_config->{orphaned_hosts_disposition};
	    my $inactive_hosts_hostgroup   = $cloud_config->{inactive_hosts_hostgroup};
	    my @hostgroups                 = StorProc->fetch_list( 'hostgroups', 'name' );
	    unshift @hostgroups, '';
	    $page .= Clouds->orphaned_hosts_options( $orphaned_hosts_disposition, \@hostgroups, $inactive_hosts_hostgroup, $tab++ );
	    my $orphaned_host_retention_period = $cloud_config->{orphaned_host_retention_period};

	    # FIX LATER:  the retention period should perhaps be generalized so the user can select both the quantity and the units;
	    # to do so, use some combination of widgets other than a list box for this setup; move this to MonarchClouds.pm and
	    # implement some JavaScript to manage widgets in parallel to enforce consistency of values between quantity and units
	    my $figure_space      = '&#8199;';
	    my %retention_periods = (
		3600              => "${figure_space}1 hour",
		3600 * 2          => "${figure_space}2 hours",
		3600 * 6          => "${figure_space}6 hours",
		3600 * 12         => '12 hours',
		3600 * 24         => "${figure_space}1 day",
		3600 * 24 * 2     => "${figure_space}2 days",
		3600 * 24 * 4     => "${figure_space}4 days",
		3600 * 24 * 7     => "${figure_space}1 week",
		3600 * 24 * 14    => "${figure_space}2 weeks",
		3600 * 24 * 31    => "${figure_space}1 month",
		3600 * 24 * 61    => "${figure_space}2 months",
		3600 * 24 * 183   => "${figure_space}6 months",
		3600 * 24 * 365   => "${figure_space}1 year",
		3600 * 24 * 99999 => 'forever'
	    );
	    $page .= Clouds->list_box_keyed(
		'Orphaned host retention period:', 'orphaned_host_retention_period',
		\%retention_periods,                   sub { $Clouds::a <=> $Clouds::b },
		$orphaned_host_retention_period,       '',
		$docs{orphaned_host_retention_period}, '',
		$tab++
	    );
	    $page .= Clouds->monitored_regions( $cloud_config->{regions}, $tab );
	    $tab += 2;
	    $page .= Forms->hidden( \%hidden );
	    my %reset = ( 'name' => 'reset', 'value' => 'Reset to Current Configuration' );
	    my %batch = ( 'name' => 'batch', 'value' => 'Run Batch Processing' );
	    $page .= Forms->form_bottom_buttons( \%reset, \%save, \%batch, $tab++ );
	}
	elsif ( $obj eq 'run' ) {
	    my $now = time;
	    $refresh_url = "?update_main=1&nocache=$now&refreshed=1";
	    foreach my $name ( keys %hidden ) {
		$refresh_url .= qq(&$name=) . (defined( $hidden{$name} ) ? $hidden{$name} : '');
	    }
	    $page .= Forms->header( $page_title, $session_id, $top_menu, $refresh_url );
	    my $form_title = 'Cloud Connector Batch Processing';
	    $page .= Clouds->form_top( $form_title, '', '3' );
	    my $errors = StorProc->check_version( $monarch_ver );
	    push @errors, @$errors if @$errors;
	    $page .= Forms->form_errors( \@errors ) if @errors;
	    $page .= Forms->form_doc('Running batch processing ...');
	    $page .= Forms->form_bottom_buttons();
	}
	elsif ( $obj eq 'results' ) {
	    # FIX LATER:  Call Forms->form_top() instead, once we have extended it to support monarch_clouds.cgi.
	    $page .= Forms->header( $page_title, $session_id, $top_menu );
	    my $form_title = 'Cloud Connector Batch Processing';
	    $page .= Clouds->form_top( $form_title, '', '3' );
	    $page .= Forms->form_errors( \@errors ) if @errors;
	    $page .= Forms->form_message( 'Results:', \@output, 'row1' ) if @output;
	    $page .= Forms->hidden( \%hidden );
	    $page .= Forms->form_bottom_buttons( \%continue, $tab++ );
	}
	elsif ( $obj eq 'orphaned_hosts' ) {
	    my $now = time;
	    $refresh_url = "?update_main=1&nocache=$now&find_orphans=1";
	    foreach my $name ( keys %hidden ) {
		$refresh_url .= qq(&$name=) . (defined( $hidden{$name} ) ? $hidden{$name} : '');
	    }
	    # FIX MINOR:  $refresh_url should be looked at in detail; it may contain some duplicate elements
	    $page .= Forms->header( $page_title, $session_id, $top_menu, $refresh_url );
	    my $form_title = 'Orphaned Cloud Hosts';
	    $page .= Clouds->form_top( $form_title, '', '3' );
	    my $errors = StorProc->check_version( $monarch_ver );
	    push @errors, @$errors if @$errors;
	    $page .= Forms->form_errors( \@errors ) if @errors;
	    $page .= Forms->wizard_doc('Orphaned Host Deletion', join (' ', @orphan_docs));
	    $page .= Forms->form_doc('Finding orphaned hosts (this may take a minute) ...');
	    $page .= Forms->form_bottom_buttons();
	}
	elsif ( $obj eq 'show_orphaned_hosts' ) {
	    $page .= Forms->header( $page_title, $session_id, $top_menu );
	    $page .= Clouds->form_top( 'Orphaned Cloud Hosts', '', '3' );
	    $page .= Forms->wizard_doc('Orphaned Host Deletion', join (' ', @orphan_docs));

	    ## An orphaned cloud host is one that satisfies all of these conditions:
	    ## (*) has a Monarch hostname matching this pattern: /^i-[0-9A-Fa-f]{8}$/
	    ## (*) has a non-NULL DeactivationTime host property stored in Foundation
	    my %orphans = ();
	    foreach my $details (@output) {
		##
		## orphan#Region#Image#Instance#Address#Hostgroups#HostProfile#Status#DeactivationTime
		##        $1     $2    $3       $4      $5         $6          $7     $8
		##
		if ($details =~ /^orphan#([^#]*)#([^#]*)#(i-[0-9A-Fa-f]{8})#([^#]*)#([^#]*)#([^#]*)#([^#]*)#([^#]+)#$/) {
		    %{ $orphans{$3} } = ();
		    my $orphan = $orphans{$3};
		    $orphan->{region}           = $1;
		    $orphan->{image}            = $2;
		    $orphan->{hostname}         = $3;
		    $orphan->{address}          = $4;
		    $orphan->{hostgroups}       = $5;
		    $orphan->{hostprofile}      = $6;
		    $orphan->{status}           = $7;
		    $orphan->{deactivationtime} = $8;
		}
	    }
	    # FIX LATER:  in some future release, pass in the desired sort order instead of hardcoding it within this routine;
	    # allow the user to select any desired sort order (though intuitively specifying a multi-field sort order is tricky)
	    $page .= Clouds->orphaned_hosts(\%orphans);
	    $hidden{'view'} = 'clouds';
	    $page .= Forms->hidden( \%hidden );
	    $page .= Forms->form_bottom_buttons( \%refresh, \%delete, $tab++ );
	}
    }
    return $page;
}

#
# Begin processing request
#

# db connection
my $auth = StorProc->dbconnect();

# get config info
my %where = ();
my %objects = StorProc->fetch_list_hash_array( 'setup', \%where );
if ( -e '/usr/local/groundwork/config/db.properties' ) { $is_portal = 1 }
$nagios_ver       = $objects{'nagios_version'}[2];
$nagios_bin       = $objects{'nagios_bin'}[2];
$nagios_etc       = $objects{'nagios_etc'}[2];
$monarch_home     = $objects{'monarch_home'}[2];
$monarch_ver      = $objects{'monarch_version'}[2];
$backup_dir       = $objects{'backup_dir'}[2];
$upload_dir       = $objects{'upload_dir'}[2];
$enable_externals = $objects{'enable_externals'}[2];
$enable_groups    = $objects{'enable_groups'}[2];
$enable_ez        = $objects{'enable_ez'}[2];
$nagios_share     = $objects{'physical_html_path'}[2];
if ($is_portal) {
    $doc_root = '/usr/local/groundwork/core/monarch/htdocs';
}

#
# Check user
#

my $deny_access     = 0;
my $show_login      = 0;
my $session_timeout = 0;

if ( $view eq 'logout' ) {
    $show_login = 1;
    ( $userid, $session_id ) = undef;
}
elsif ( $is_portal || $auth == 1 ) {

    # Auth level 1 = full access no login.
    $user_acct = $ENV{'REMOTE_USER'};

    if ($user_acct) {
	my %super_user = ();
	if ($session_id) {
	    ( $userid, $user_acct, $session_id ) = StorProc->get_session($session_id);
	}
	else {
	    ( $userid, $session_id ) = StorProc->set_gwm_session($user_acct);
	}
	my ( $auth_add, $auth_modify, $auth_delete ) = StorProc->auth_matrix( $userid, $auth );
	%auth_add    = %{$auth_add};
	%auth_modify = %{$auth_modify};
	%auth_delete = %{$auth_delete};
    }
    else {
	$deny_access = 1;
    }
}
elsif ( $auth == 2 ) {

    # Auth level 2 = active login.
    if ( $query->param('process_login') ) {
	$userid = StorProc->check_user( $user_acct, $password );
	my ( $auth_add, $auth_modify, $auth_delete ) = StorProc->auth_matrix($userid);
	%auth_add    = %{$auth_add};
	%auth_modify = %{$auth_modify};
	%auth_delete = %{$auth_delete};
	if ( $auth_add{'enable_ez'}
	    && ( $auth_add{'ez_main'} || $auth_add{'ez'} ) )
	{
	    $ez = 1;
	}
	if ( $userid =~ /^\d+/ ) {
	    $session_id = StorProc->set_session( $userid, $user_acct );
	}
	else {
	    $show_login = 1;
	}
    }
    elsif ($session_id) {
	( $userid, $user_acct, $session_id ) = StorProc->get_session($session_id);
	if ($user_acct) {
	    my ( $auth_add, $auth_modify, $auth_delete ) = StorProc->auth_matrix($userid);
	    %auth_add    = %{$auth_add};
	    %auth_modify = %{$auth_modify};
	    %auth_delete = %{$auth_delete};
	}
	else {
	    $session_timeout = 1;
	}
    }
    else {
	$show_login = 1;
	( $userid, $session_id ) = undef;
    }
}
elsif ( $auth == 3 ) {

    # Auth level 3 = passive login - single sign on.
    # first check if we have a new user being passed
    my $new_user_acct = $ENV{'REMOTE_USER'};
    unless ($new_user_acct) { $new_user_acct = $query->param('user_acct') }
    if ($new_user_acct) {

	# now check for session info
	( $userid, $user_acct, $session_id ) = StorProc->get_session( $session_id, $auth );

	# does stored user = new user? -- if there is one stored
	unless ( $new_user_acct eq $user_acct ) {

	    # no? then see if new user is valid and give them a sessionid if so
	    my %user = StorProc->fetch_one( 'users', 'user_acct', $new_user_acct );
	    $session_id = StorProc->set_session( $user{'user_id'}, $new_user_acct );
	    $user_acct = $new_user_acct;
	}
    }
    else {
	( $userid, $user_acct, $session_id ) = StorProc->get_session( $session_id, $auth );
    }
    if ($session_id) {
	my ( $auth_add, $auth_modify, $auth_delete ) = StorProc->auth_matrix($userid);
	%auth_add    = %{$auth_add};
	%auth_modify = %{$auth_modify};
	%auth_delete = %{$auth_delete};
    }
    else {
	$show_login = 1;
	( $userid, $session_id ) = undef;
    }
}

#
# Create frames and content
#

unless ( $view =~ /search/ || $show_login ) {
    my $cookie = $query->cookie( CGISESSID => $session_id );
    print $query->header( -cookie => $cookie );
}
unless ($enable_externals) {
    delete $auth_add{'externals'};
    delete $auth_modify{'externals'};
    delete $auth_delete{'externals'};
}
unless ($enable_groups) {
    delete $auth_add{'groups'};
    delete $auth_modify{'groups'};
    delete $auth_delete{'groups'};
}

if ($show_login) {
    print "Content-type: text/html \n\n";
    print Forms->login( $page_title, $userid );
}
elsif ($deny_access) {
    print "Content-type: text/html \n\n";
    print Forms->form_top( 'Configuration', '' );
    print Forms->wizard_doc( 'Access Denied', 'You must log in to use this feature.' );
    print Forms->form_bottom_buttons();
    print Forms->footer($debug);
}
elsif ( $query->param('update_top') ) {
    my @top_menus = ();
    my $login     = $query->param('login');
    if ($session_timeout) {
	print Forms->login_redirect;
    }
    elsif ($ez) {
	for my $object (qw(hosts host_groups profiles notifications commit setup)) {
	    no strict 'refs';
	    push( @top_menus, $object )
	      if ( $auth == 1 || $auth_add{ 'ez_' . $object } );
	}
	my $login = $query->param('login');
    }
    else {
	for my $object ( qw(profiles services hosts contacts escalations time_periods commands) ) {
	    push( @top_menus, $object )
	      if ( $auth == 1 || $auth_add{$object} || $auth_modify{$object} );
	}
	for my $object (qw(groups control)) {
	    push( @top_menus, $object ) if ( $auth == 1 || $auth_add{$object} );
	}
	push @top_menus, 'clouds';
    }
    push @top_menus, 'help';
    print Forms->top_frame( $session_id, $top_menu, \@top_menus, $auth, $monarch_ver, $enable_ez, $ez, \%auth_add, $login );
}
elsif ( $query->param('update_main') ) {
    $hidden{'update_main'} = 1;
    if ($debug) {
	$debug .= '<pre>';
	foreach my $name ( sort $query->param ) {
	    my @values = $query->param($name);
	    $debug .= "$name = '" . join( "', '", @values ) . "'" . '<br>';

	    # ought to be:
	    # $debug .= HTML::Entities::encode("$name = '" . join("', '", @values) . "'") . '<br>';
	}
	$debug .= '</pre>';
    }
    if ($session_timeout) {
	if ( $view =~ /search/ ) { print "Content-type: text/html \n\n" }
	$body .= Forms->login_redirect;
    }
    elsif ( $view eq 'clouds' ) {
	$body .= clouds();
    }
    else {
	$body .= Forms->header( $page_title, $session_id, $top_menu );
    }

    print $body;
    print Forms->footer($debug);
}
else {
    print Forms->frame( $session_id, $top_menu, $is_portal, $ez );
}

my $result = StorProc->dbdisconnect();

