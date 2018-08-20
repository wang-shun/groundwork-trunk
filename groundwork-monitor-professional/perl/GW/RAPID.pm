package GW::RAPID;

# GW::RAPID - GroundWork REST API Perl Interface for Development
#
# Copyright 2013-2018 GroundWork Open Source, Inc. (GroundWork).
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# Author Emeritus:  Dominic Nicholas
#
# Revision history:
#
# 2014-01-31 DN	0.0.1	Original version.
# 2014-02-17 DN	0.0.2	first and count params were missing from 0.0.1. Added handling here.
#			Depth is now handled in the same way, and the depth arg is removed from get*()
# 2014-03-12 GH 0.1.0	Moved to the GW:: namespace, to avoid confusion considering many incompatible changes.
#			Converted to use a class instance, to condense and normalize function signatures, and
#			to separate results (returned-object data) from the outcome (success/failure) indication.
# 2014-03-18 GH 0.2.0	Made delete_services() work at least minimally.
#			Added {format => 'id'|'host,service'|'service,host'} option to get_services()
#			to control the structure of %results.
#			Added still-to-do list, to make it easier to track anticipated fixes and extensions.
# 2014-03-25 GH 0.3.0	Forced all user-interface routines to not throw exceptions.  (Internal routines can
#			throw exceptions, which are then caught by the user-interface routines.)
#			Change the logger from a package-global variable to an instance variable, in preparation
#			for better control over its usage.  Stripped out initialization of the logger within
#			this module, because global logger initialization belongs at the package level, not at
#			the library level.  Accept a logger handle from the calling application, partly so the
#			application can itself decide what application-relevant category to assign to messages
#			from this package.
# 2014-03-27 GH 0.3.1	Cleaned up basepath validation in the new() constructor to insist on it already ending
#			with "/api", per the existing doc.
#			Implemented automatic internal re-authorization if the server times out the session.
#			Added support for an external credentials file handed to the constructor.
# 2014-03-28 GH 0.4.0	Added ack_events() and unack_events() routines.
# 2014-03-29 GH 0.4.1	Fixed basepath processing.  Improved the documentation of get_services().
# 2014-03-31 GH 0.4.2	Marked changes for HTTP header usage.
#			Set the user-agent header to reflect the GW::RAPID package rather than REST::Client.
#			Added skeleton (very incomplete) support for checking SSL certificates.
# 2014-04-04 GH 0.4.3	Revised URL authentication parameter names.
# 2014-04-06 GH 0.5.0	Enabled JSON serialization of blessed objects.  (This requires the calling application
#			to ensure that the packages of which such objects are an instance have a TO_JSON()
#			method to carry out the serialization.)
# 2014-04-15 GH 0.5.1	Initial support for the REST API "version" call.  This may be used internally in the
#			future to adapt this package to whatever capabilities are supported by the REST API.
#			Initial support for fetching application types.
# 2014-04-16 GH 0.5.2	Added support for upserting and deleting application types.
#			Added support for creating host and service notifications.
#			Fixed a bunch of usage messages and some documentation.
#			Noted in the doc that upsert_hosts() and upsert_services() operate in asynchronous mode
#			by default, and that this mode may be controlled via the %options in these calls.
# 2014-04-18 GH 0.5.3	Modified hostname validation.
# 2014-04-30 GH 0.5.4	Internal bug fix to support application types.
# 2014-05-02 GH 0.5.5	Internal improvements to logging during global destruction.
# 2014-05-07 GH 0.5.6	Added interruptibility of long-running external actions.  Internal comments upgraded.
#			Added INFO-level logging of timing statistics.
# 2014-05-22 GH 0.5.7	Fix internal subroutine-caller determination to avoid Perl warnings.
# 2014-08-08 GH 0.6.0	Fixed some documentation.  Added preliminary support for new servicegroups calls.
# 2014-08-12 GH 0.6.1	Improved logging.
# 2014-08-13 GH 0.6.2	Simplified startup logging.
# 2014-08-22 GH 0.6.3	Improved logging.
# 2014-08-28 GH 0.6.4	Some general cleanup, plus some early code for bulk deletion of servicegroups.
# 2014-09-05 GH 0.6.5	Correct some documentation.  Enable bulk deletion of servicegroups;
#			convert host and hostgroup deletion to use POST data.
# 2014-09-25 GH 0.6.6	Updated credentials handling.
# 2014-09-30 GH 0.6.7	Clarified log messages.
# 2014-10-03 GH 0.6.8	Added support for sending performance data.
# 2014-10-22 GH 0.6.9	Added callbacks to some calls, to support better timing measurements.
#			Added support for creating and getting auditlog data.
# 2014-12-10 GH 0.7.0	Add a boolean "force_crl_check" option to the new() constructor, to allow not insisting
#			on having a SSL Certificate Revocation List file around to check.  (This option currently
#			defaults to being enabled, so GW::RAPID will by default insist on the existence of a CRL
#			file on the client side if the server has SSL enabled.)
# 2015-02-13 GH 0.7.1	Added check_license().
# 2015-02-17 GH 0.7.2	Improved some constructor error messages.
# 2015-02-20 GH 0.7.3	Removed documentation for unsupported asynchronous event handling.
# 2015-02-25 GH 0.7.4	Fixed Perl error when handling server error response.
# 2015-03-24 GH 0.7.5	Fixes to SSL support (certificate handling).
# 2015-04-16 DN 0.7.6	Mod to _API_GET() to better recognized object not found cases in light of GWMON-11397
# 2015-04-30 DN 0.7.7	Added : get_propertytypes() and upsert_propertytypes()
# 2015-09-30 DN 0.7.8	Added : get/upsert/delete_hostblacklists methods
# 2015-10    DN 0.7.9	Added : get/upsert/clear/delete_hostidentities methods
# 2015-10    DN 0.8.0	Fixed up get_categories to work with 710.
# 2015-10    DN 0.8.1	Added : set/clear/get_bizindowntime methods
# 2015-11    DN 0.8.2	Added : customgroups methods
# 2015-11    DN 0.8.3	Changed headers for logout changed to text/plain as per API update
# 2015-11    DN 0.8.4	Added support for upserting via biz/hosts
# 2016-01    DN 0.8.5	Added support for upserting via biz/services
# 2016-08-30 GH 0.8.6	Localize $@ in the DESTROY method, and $_ in subroutines, per Perl documentation.
# 2016-09-12 DN/RG 0.8.7 GWMON-12697. See 0.8.7 tags below.
# 2016-09-23 DN 0.8.8	GWMON-12728 fix - remove over restrictive hostname validation in _API_DELETE
# 2016-11-03 GH 0.8.9	GWMON-12790 implement a "multithreaded" option to allow GW::RAPID to be used in threaded code
# 2016-11-07 GH 0.9.0	GWMON-12778 force the use of TLS 1.2 and upgraded ciphers when an HTTPS connection is in use
# 2016-11-26 GH 0.9.1	Downgrade a not-found message from WARNING to INFO, since a missing object can never be a
#			cause for alarm at the level of this package; that's an application-level decision.
# 2017-02-13 GH 0.9.2	Support a re-useable authentication token.
# 2017-02-27 GH 0.9.3	Bump up the default timeout value; improve the timeout doc.
# 2017-02-28 GH 0.9.4	Fixed support for re-using auth tokens.
# 2018-06-20 GH 0.9.5	improve doc for interpreting results from get_version().

# Be sure to change $VERSION below, with every new release!  (And also the version number in the doc at the end.)

# TODO
#  - doc up get_propertytypes and upsert_propertytypes methods and write .t files
#  - doc up changes to get_categories

use warnings;
use strict;
use attributes;

our ( @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION );

BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw();
    @EXPORT_OK   = qw( is_valid_dns_hostname is_valid_object_name );
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.9.5";
}

# STILL TO DO (this is an old list; some of this might well be done already):
# * Verify all encoding of data (if needed) sent to the REST API in UTF-8.
#   In particular, test data that is valid ISO-8859-1 (8-bit characters
#   with the high bit set) but that is not a legal lead-in sequence for
#   UTF-8, to see how the API handles it end-to-end.
# * Verify all quoting of data (if needed) sent to the REST API.  In
#   particular, check how single quotes are handled in query-field values.
# * Verify that all %outcome settings are correct, for both success
#   and failure conditions.
# * Make sure the package never dies unless documented, as that will
#   kill the whole process unless the caller wraps the call in an eval{}.
#   (Within a calling application, if the package is only called in
#   thread context, note that every thread except the main thread is
#   automatically called in an eval context.)  Make sure the package
#   never exits, as again that will exit the entire process (unless
#   the application has prepared for this using an appropriate threads
#   option).
# * We need to verify that %outcome is properly populated the same way
#   in all _API_* routines upon an HTTP failure, or upon a REST API
#   failure of any other kind, or upon failure detected directly
#   within the GW::RAPID package.  Also check and document how it is
#   populated upon success.
# * Add more error checking to outlaw certain worst-case calls, such
#   as asking for all events in the system by using too-wide-open
#   wildcarding.
# * delete_services() and the underlying _API_DELETE() call for
#   services might still not be fully working.  (This has probably
#   been fixed now.)
# * Some Foundation REST API calls need to be converted from URLs
#   to POST data to avoid potential limits on URL lengths.  If there are
#   any cases where we cannot make such a conversion, we need to check
#   the URL length and return a failure if the construction is too long.
# * The internal usage messages are often wrong, and the documentation
#   is also often wrong and is still very incomplete (on available %options
#   for each call, and on the content of both %outcome and %results and
#   @results structures).
# * The constructor parameters are not fully documented.
# * Certain other parameters should be supported, such as a {file
#   => $filename} parameter on the constructor (to provide support for
#   the common case of wanting to parse ws_client.properties to obtain
#   credentials).
# * No routine yet validates the supported %options, either to constrain
#   them to those that are available in the Foundation REST API or in
#   some cases to avoid collisions with the specific uses of those options
#   within the GW::RAPID package itself.
# * For that matter, the structure of input arrays (for creating new
#   objects, for instance) is nowhere documented; the Foundation REST
#   API documentation is useless (only suggestive, not definitive) in
#   a Perl context.
# * There is still a lot of cleanup to do to eliminate redundant code.
# * There is still a lot of cleanup to do to eliminate early code used in
#   development debugging.
# * There are still many unit tests that need to be added, to cover
#   many cases that are just plain being ignored in the current test
#   scripts.
# * Each of the test scripts needs to be examined to see that the
#   existing tests actually do test what they claim to test.  And they
#   need to be extended to actually probe the returned %outcome, %results,
#   and @results structures to verify that they contain the expected
#   content in some detail.
# * The need to destroy the $rest_api object in some reasonable time is
#   not documented, if the calling application will not be needing it,
#   to release server resources.
# * The Foundation REST API will unceremoniously time out a perfectly
#   usable, still-active $rest_api object simply because the server thinks
#   it has been "too long" since that object was created, even if the
#   object has not been recently idle for long.  The GW::RAPID package
#   therefore needs to intercept and examine all return values, and retry
#   the operation internally at least once upon that type of failure.
#   To that end, we need Foundation REST API documentation on what type
#   of error value will be returned from the REST API when the problem is
#   no fault of the caller but the server decided to abandon the allocated
#   resource.
# * Later, more routines will be added to support other objects,
#   such as statistics (currently mentioned in the Foundation REST
#   API doc), and other objects not yet even mentioned in that doc.
# * Deal with $logger throughout this package.  Untie it from its current
#   status as a package-global variable, making it an instance variable;
#   make it entirely optional as to whether Log4perl will be used in that
#   capacity; allow the user to substitute his own logging package; untie
#   the use of this logger from a single config file used across all
#   applications and instances.
# * Clean up the documentation formatting, and move it all into a separate
#   RAPID.pod file.
# * Fold in some sort of call timeouts, first set globally in the constructor
#   and then optionally overrideable at the individual-call level.
# * Perhaps fold some aspect of thread-pool support into the GW::RAPID package.
#   (Although this is probably not necessary given that the careful use of Coro
#   and AnyEvent (or LWP::Protocol::AnyEvent::http, which seems to suffer far
#   less from race conditions than AnyEvent-HTTP) packages from CPAN is probably
#   a much better idea than use of Perl ithreads.)
# * Since we use PUT to update events, and we might include lengthy user comments
#   in such data, check to see whether any URL length limits might be violated.

# ================================ Modules ================================

# This is supposed to be the default, but we force it anyway, because we want to ensure that
# we have the default SSL_cipher_list from IO::Socket::SSL in play (and not whatever Net::SSL
# provides, if anything), even if somebody has set the PERL_NET_HTTPS_SSL_SOCKET_CLASS
# environment variable to something else.  See the Net::HTTPS documentation for information
# on this variable.
BEGIN {
    ## LWP::UserAgent "use"s LWP::Protocol and calls LWP::Protocol::create() to dynamically reference
    ## LWP::Protocol::https if we have an HTTPS connection configured, and that in turn "require"s
    ## Net::HTTPS at run time.  While this chain works just fine, the Perl compilation phase can't
    ## tell whether Net::HTTPS will be loaded, so it complains about "used only once: possible typo"
    ## for the following assignment, which will be the only reference to this variable at compilation
    ## time.  We disable that noisy warning about a known singleton use of the variable.
    no warnings 'once';
    $Net::HTTPS::SSL_SOCKET_CLASS = 'IO::Socket::SSL';
}

use IO::Socket::SSL 2.037;  # Make sure IO::Socket::SSL is used in preference to Net::SSL, and use a recent cipher list.

use Data::Dumper;    # For debugging for when Smart::Comments doesn't hack it
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use Time::HiRes;

use REST::Client;                       # REST::Client for REST API operation
use MIME::Base64;                       # Also for REST API operation
use List::MoreUtils qw(any);            # For subroutine argument checking
use Carp;                               # For exception handling
use HTTP::Status qw(status_message);    # For translation of API HTTP status codes
use URI::Escape;                        # For percent-encoding query URIs to the API
use Log::Log4perl;                      # For logging
use TypedConfig;

# ================================ Variables  ================================
# All variables are internal to the module. Some can be changed via setter functions.

# Redirect all warnings, including those from Perl compiler, into log4perl.
# THIS IS A BAD IDEA.  It's up to the application to make such a decision, not a library.
#BEGIN { $SIG{__WARN__} = sub { $logger->warn( "WARN @_" ); } }

# Global subroutine usage strings. Simply globals vs. use Readonly for performance reasons.
my $usage_new                               = 'Usage: $api = GW::RAPID->new( $protocol, $hostname, $username, $password, $requestor, \\%options );';
my $usage__API_GET                          = 'Usage: $status = $api->_API_GET( $api_method, \\@objects, \\%options, \\%outcome, \\%results );';
my $usage_get_version                       = 'Usage: $status = $api->get_version( [], \\%options, \\%outcome, \\%results );';
my $usage_get_application_types             = 'Usage: $status = $api->get_application_types( \@app_type_names, \\%options, \\%outcome, \\%results );';
my $usage_get_hostblacklists                = 'Usage: $status = $api->get_hostblacklists( \\@hostnames, \\%options, \\%outcome, \\%results );';
my $usage_get_hostidentities                = 'Usage: $status = $api->get_hostidentities( \\@identities, \\%options, \\%outcome, \\%results );';
my $usage_get_hostidentities_autocomplete   = 'Usage: $status = $api->get_hostidentities_autocomplete( \\@prefix, \\%options, \\%outcome, \\%results );';
my $usage_get_hosts                         = 'Usage: $status = $api->get_hosts( \\@hostnames, \\%options, \\%outcome, \\%results );';
my $usage_get_customgroups                  = 'Usage: $status = $api->get_customgroups( \\@customgroupnames, \\%options, \\%outcome, \\%results );';
my $usage_get_customgroups_autocomplete     = 'Usage: $status = $api->get_customgroups_autocomplete( \\@prefix, \\%options, \\%outcome, \\%results );';
my $usage_get_services                      = 'Usage: $status = $api->get_services( \\@servicenames, \\%options, \\%outcome, \\%results );';
my $usage_get_servicegroups                 = 'Usage: $status = $api->get_servicegroups( \\@servicegroupnames, \\%options, \\%outcome, \\%results );';
my $usage_get_devices                       = 'Usage: $status = $api->get_devices( \\@deviceidentifications, \\%options, \\%outcome, \\%results );';
my $usage_get_propertytypes                 = 'Usage: $status = $api->get_propertytypes( \\@propertiesdentification, \\%options, \\%outcome, \\%results );';
my $usage_get_events                        = 'Usage: $status = $api->get_events( \\@eventids, \\%options, \\%outcome, \\%results );';
my $usage_get_categories                    = 'Usage: $status = $api->get_categories( \\@categorynames, \\%options, \\%outcome, \\%results );';
my $usage_get_auditlogs                     = 'Usage: $status = $api->get_auditlogs( \\@auditlogids, \\%options, \\%outcome, \\%results );';
my $usage_get_hostgroups                    = 'Usage: $status = $api->get_hostgroups( \\@hostgroupnames, \\%options, \\%outcome, \\%results );';
my $usage_get_tokens                        = 'Usage: $status = $api->get_tokens( \\@tokenids, \\%options, \\%outcome, \\%results );';
my $usage__API_POST                         = 'Usage: $status = $api->_API_POST( $api_method, \@objects, \\%options, \\%outcome, \\@results );';
my $usage_create_noma_host_notifications    = 'Usage: $status = $api->create_noma_host_notifications( \\@host_notifications, \\%options, \\%outcome, \\@results );';
my $usage_create_noma_service_notifications = 'Usage: $status = $api->create_noma_service_notifications( \\@service_notifications, \\%options, \\%outcome, \\@results );';
my $usage_create_events                     = 'Usage: $status = $api->create_events( \\@events, \\%options, \\%outcome, \\@results );';
my $usage_create_performance_data           = 'Usage: $status = $api->create_performance_data( \\@perfdata, \\%options, \\%outcome, \\@results );';
my $usage_create_auditlogs                  = 'Usage: $status = $api->create_auditlogs( \\@auditlogs, \\%options, \\%outcome, \\@results );';
my $usage_upsert_application_types          = 'Usage: $status = $api->upsert_application_types( \\@app_types, \\%options, \\%outcome, \\@results );';
my $usage_upsert_hostblacklists             = 'Usage: $status = $api->upsert_hostblacklists( \\@blacklists, \\%options, \\%outcome, \\@results );';
my $usage_upsert_hostidentities             = 'Usage: $status = $api->upsert_hostidentities( \\@identities, \\%options, \\%outcome, \\@results );';
my $usage_get_indowntime                    = 'Usage: $status = $api->get_indowntime( \\%objects, \\%options, \\%outcome, \\@results );';
my $usage_set_indowntime                    = 'Usage: $status = $api->set_indowntime( \\%objects, \\%options, \\%outcome, \\@results );';
my $usage_clear_indowntime                  = 'Usage: $status = $api->clear_indowntime( \\%objects, \\%options, \\%outcome, \\@results );';
my $usage_upsert_hosts                      = 'Usage: $status = $api->upsert_hosts( \\@hosts, \\%options, \\%outcome, \\@results );';
my $usage_upsert_hostgroups                 = 'Usage: $status = $api->upsert_hostgroups( \\@hostgroups, \\%options, \\%outcome, \\@results );';
my $usage_upsert_services                   = 'Usage: $status = $api->upsert_services( \\@services, \\%options, \\%outcome, \\@results );';
my $usage_upsert_servicegroups              = 'Usage: $status = $api->upsert_servicegroups( \\@servicegroups, \\%options, \\%outcome, \\@results );';
my $usage_upsert_devices                    = 'Usage: $status = $api->upsert_devices( \\@devices, \\%options, \\%outcome, \\@results );';
my $usage_upsert_propertytypes              = 'Usage: $status = $api->upsert_propertytypes( \\@propertytypes, \\%options, \\%outcome, \\@results );';
my $usage_upsert_categories                 = 'Usage: $status = $api->upsert_categories( \\@categories, \\%options, \\%outcome, \\@results );';
my $usage_upsert_customgroups               = 'Usage: $status = $api->upsert_categories( \\@customgroups, \\%options, \\%outcome, \\@results );';
my $usage_upsert_bizhosts                   = 'Usage: $status = $api->upsert_bizhosts( \\@bizhosts, \\%options, \\%outcome, \\@results );';
my $usage_upsert_bizservices                = 'Usage: $status = $api->upsert_bizservices( \\@bizservices, \\%options, \\%outcome, \\@results );';
my $usage_ack_events                        = 'Usage: $status = $api->ack_events( \\@patterns, \\%options, \\%outcome, \\@results );';
my $usage_unack_events                      = 'Usage: $status = $api->unack_events( \\@patterns, \\%options, \\%outcome, \\@results );';
my $usage__API_DELETE                       = 'Usage: $status = $api->_API_DELETE( $api_method, \\@objects, \\%options, \\%outcome, \\@results );';
my $usage_delete_application_types          = 'Usage: $status = $api->delete_application_types( \\@app_type_names, \\%options, \\%outcome, \\@results );';
my $usage_delete_hostblacklists             = 'Usage: $status = $api->delete_hostblacklists( \\@hostnames, \\%options, \\%outcome, \\@results );';
my $usage_delete_hostidentities             = 'Usage: $status = $api->delete_hostidentities( \\@identities, \\%options, \\%outcome, \\@results );';
my $usage_delete_hosts                      = 'Usage: $status = $api->delete_hosts( \\@hostnames, \\%options, \\%outcome, \\@results );';
my $usage_delete_hostgroups                 = 'Usage: $status = $api->delete_hostgroups( \\@hostgroupnames, \\%options, \\%outcome, \\@results );';
my $usage_delete_services                   = 'Usage: $status = $api->delete_services( \\@servicenames, \\%options, \\%outcome, \\@results );';
my $usage_delete_servicegroups              = 'Usage: $status = $api->delete_servicegroups( \\@servicegroupnames, \\%options, \\%outcome, \\@results );';
my $usage_delete_devices                    = 'Usage: $status = $api->delete_devices( \\@deviceidentifications, \\%options, \\%outcome, \\@results );';
my $usage_delete_events                     = 'Usage: $status = $api->delete_events( \\@eventids, \\%options, \\%outcome, \\@results );';
my $usage_delete_categories                 = 'Usage: $status = $api->delete_categories( \\@categorynames, \\%options, \\%outcome, \\@results );';
my $usage_delete_customgroups               = 'Usage: $status = $api->delete_customgroups( \\@customgroups, \\%options, \\%outcome, \\@results );';
my $usage_clear_hostgroups                  = 'Usage: $status = $api->clear_hostgroups( \\@hostgroupnames, \\%options, \\%outcome, \\@results );';
my $usage_clear_servicegroups               = 'Usage: $status = $api->clear_servicegroups( \\@hostgroupnames, \\%options, \\%outcome, \\@results );';
my $usage_clear_hostidentities              = 'Usage: $status = $api->clear_hostidentities( \\@identities, \\%options, \\%outcome, \\@results );';
my $usage__API_PUT                          = 'Usage: $status = $api->_API_PUT( $api_method, \\@objects, \\%options, \\%outcome, \\@results );';
my $usage_update_events                     = 'Usage: $status = $api->update_events( \\@eventids, \\%options, \\%outcome, \\@results );';
my $usage_add_customgroups_members          = 'Usage: $status = $api->usage_add_customgroups_members( \\@members, \\%options, \\%outcome, \\@results );';
my $usage_delete_customgroups_members       = 'Usage: $status = $api->usage_delete_customgroups_members( \\@members, \\%options, \\%outcome, \\@results );';
my $usage_check_license                     = 'Usage: $status = $api->check_license( \\@deviceidentifications, \\%options, \\%outcome, \\%results );';
my $analyze_upsert_response                 = 'Usage: $status = $api->analyze_upsert_response( $ref_decoded_response );';
my $analyze_delete_response                 = 'Usage: $status = $api->analyze_delete_response( $ref_decoded_response );';
my $analyze_put_response                    = 'Usage: $status = $api->analyze_put_response( $ref_decoded_response );';
my $usage_response_content_matched          = 'Usage: $status = $api->response_content_matched( $api_method, $response_content );';

# ---------------------------------------------------------------------------- #

# We use the SSL_check_crl flag here (see IO::Socket::SSL), to ensure that the capability of
# checking a Certificate Revocation List (CRL) is available to customers who desire to use
# it.  If the $force_crl_check flag in the new() constructor is set to a true value, either
# defaulted or by explicit setting in the constructor %options hash, that (in conjunction
# with the gw_rapid_client_ssl_opts() function below) requires you (if using SSL) to have at
# least put in place a long-expiring, effectively-empty CRL file on the client machine.  But
# since it can be difficult for some customers to generate an empty CRL, we're not demanding
# that such a file be present; the calling application can set the $force_crl_check option
# to false in the %options hash.  In that case, if a CRL file is present, we will force the
# verification to use it; if not, we don't complain, and the verification will proceed without
# such additional checking.  It is generally recommended that all GW::RAPID applications
# provide a site-configurable, application-level $force_crl_check option to pass to the
# GW::RAPID::new() constructor, since the circumstances surrounding use of CRLs will be
# different from site to site.
#
# We used to support SSL_version => 'TLSv1' here, but that only allows TLS 1.0, which is now
# considered to be insufficiently secure, and not later versions of TLS.  So the present code
# only supports TLS 1.2 ('TLSv1_2').  This means that if you implement HTTPS on your server,
# it must be configured to enable the TLS 1.2 protocol.
#
# We used to use "SSL_cipher_list => 'RC4-SHA:HIGH:!ADH'," as well, but now that we are enforcing the
# use of an upgraded copy of IO::Socket::SSL, we can depend on its default cipher list instead.
#
# Apparently, we don't need to also specify "SSL_verify_mode => SSL_VERIFY_PEER",
# as that is implied within LWP::Protocol::https by the verify_hostname => 1 setting.
#
my %standard_ssl_opts = (
    verify_hostname => 1,
    SSL_ca_path     => get_ca_path(),
    SSL_version     => 'TLSv1_2',
    SSL_check_crl   => 1,
);

# Returns the local Certificate Authority directory path for the agent, which
# is where the agent can find any locally installed trusted certificates as
# separate files, along with an index of the certificates.
# FIX MINOR:  This should be overrideable in the GW::RAPID->new() constructor.
# FIX MINOR:  Should we be using /usr/local/groundwork/apache2/conf instead?
# Compare to our GWMEE-level SSL setup instructions, and where certificates are
# supposed to be stored for use with Apache.  Then test, test, test.
sub get_ca_path {
    return "/usr/local/groundwork/common/openssl/certs";
}

# Verify that the local Certificate Authority directory path for the agent,
# where the agent can find any locally installed trusted certificates, has
# sufficient protections applied that it can be reasonably trusted.  This
# routine must be called immediately before any attempt to use certificates,
# since this program is a long-running daemon and the situation in the
# filesystem can change over time.  I'm not 100% certain, but currently I
# believe this means that every call to the REST API must be checked; I'm
# not depending on the CA data being cached inside Perl during the initial
# GW::RAPID constructor login call.
#
sub is_valid_ca_path {
    my $errormsg  = shift;    # required argument
    my @urls      = @_;
    my $my_osname = $^O;
    my $ca_path   = get_ca_path();

    # We really only need to verify the CA path if we are using SSL (the HTTPS protocol)
    # in at least one of the URLs we are about to fetch.
    my $using_https = 0;
    foreach my $url (@urls) {
	if ($url =~ /^https:/i) {
	    $using_https = 1;
	    last;
	}
    }
    return 1 if not $using_https;

    $$errormsg = '';
    if ( $my_osname eq 'MSWin32' ) {
	## Windows is not our usual platform for applications using GW::RAPID, but perhaps it might
	## be so used in the future.  So this branch is for such theoretically possible cases.
	#
	# FIX MINOR:  Verify the following conditions; if any of them fail, set $$errormsg
	# and return false:
	# (*) The directory must be owned by some kind of trusted administrative entity.
	# (*) The directory must be readable by the current user, and perhaps not by any
	#     other users other than administrators.
	# (*) Permissions on the directory must disallow anyone but administrators and
	#     possibly (but preferably not) the current user itself from writing into
	#     the directory and altering the set of files there.
	# (*) Every file in the directory must have its permissions set as read-only.
	#
	if ( -l $ca_path ) {
	    $$errormsg = "CA directory \"$ca_path\" cannot be a symlink.";
	    return 0;
	}
	if ( !-d _ ) {
	    ## Perhaps $ca_path does not even exist, but we won't distinguish that condition here.
	    $$errormsg = "CA directory \"$ca_path\" is not a directory.";
	    return 0;
	}
	if ( !opendir( CAPATH, $ca_path ) ) {
	    $$errormsg = "Cannot open CA directory \"$ca_path\" ($!).";
	    return 0;
	}
	else {
	    my @files = readdir CAPATH;
	    closedir(CAPATH);
	    foreach my $file (@files) {
		next if $file eq '.' or $file eq '..';
		my $ca_filepath = "$ca_path/$file";
		if ( -l $ca_filepath ) {
		    my $basefile = readlink $ca_filepath;
		    if ( not defined $basefile ) {
			$$errormsg = "Cannot read CA symlink \"$ca_filepath\" ($!).";
			return 0;
		    }
		    elsif ( $basefile =~ m{[/\\]} ) {
			## We don't even support a "../thisdir/filename" type of symlink,
			## that goes outside the CA directory and tries to come back in.
			$$errormsg = "CA symlink \"$ca_filepath\" contains a reference to some other directory.";
			return 0;
		    }
		    elsif ( !-e $ca_filepath ) {
			$$errormsg = "CA symlink \"$ca_filepath\" points to a non-existent file.";
			return 0;
		    }
		    elsif ( !-f _ or -l "$ca_path/$basefile" ) {
			$$errormsg = "CA symlink \"$ca_filepath\" points to a non-file.";
			return 0;
		    }
		}
		elsif ( -f _ ) {
		    if ( -z _ ) {
			$$errormsg = "CA filepath \"$ca_filepath\" is an empty file.";
			return 0;
		    }
		}
		else {
		    $$errormsg = "CA filepath \"$ca_filepath\" is a non-symlink, non-file.";
		    return 0;
		}
	    }
	}
    }
    else {
	# FIX MINOR:  Verify the following conditions; if any of them fail, set $$errormsg
	# and return false:
	# (*) The directory must be owned by some kind of trusted administrative entity.
	# (*) The directory must be readable by the current user, and perhaps not by any
	#     other users other than administrators.
	# (*) Permissions on the directory must disallow anyone but administrators
	#     and possibly (but preferably not) the current user itself from writing
	#     into the directory and altering the set of files there.
	# (*) The directory permissions ought to include the sticky bit, for extra safety.
	# (*) Every symlink in the directory must only point directly to an existing file
	#     in the same directory.
	# (*) Every non-symlink file in the directory must be owned by some kind of trusted
	#     administrative entity.
	# (*) Every non-symlink file in the directory must have its permissions set as
	#     read-only, except possibly for being writable by the owner.
	# (*) Possibly, every non-symlink file in the directory should have only one hard
	#     link to that file.
	#
	if ( -l $ca_path ) {
	    $$errormsg = "CA directory \"$ca_path\" cannot be a symlink.";
	    return 0;
	}
	if ( !-d _ ) {
	    ## Perhaps $ca_path does not even exist, but we won't distinguish that condition here.
	    $$errormsg = "CA directory \"$ca_path\" is not a directory.";
	    return 0;
	}
	my ( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, @rest ) = stat(_);
	if ( $uid != $> and $uid != 0 ) {
	    ## We do our best to tell the caller that the directory must be owned by
	    ## either the current user (presumably nagios) or by root.
	    my $running_as_username = getpwuid $>;
	    my $file_owner_username = getpwuid $uid;
	    $running_as_username = defined($running_as_username) ? "\"$running_as_username\"" : "an unknown user (UID $>)";
	    $file_owner_username = defined($file_owner_username) ? "\"$file_owner_username\"" : "an unknown user (UID $uid)";
	    $$errormsg =
		"CA directory \"$ca_path\" is owned by $file_owner_username;\n"
	      . "    it must be owned by either the current user, or by root.\n"
	      . "    You are currently running your application as $running_as_username.";
	    return 0;
	}
	elsif ($mode & 022) {
	    $$errormsg = "CA directory \"$ca_path\" has excessively open permissions.";
	    return 0;
	}

	if ( !opendir( CAPATH, $ca_path ) ) {
	    $$errormsg = "Cannot open CA directory \"$ca_path\" ($!).";
	    return 0;
	}
	else {
	    my @files = readdir CAPATH;
	    closedir(CAPATH);
	    foreach my $file (@files) {
		next if $file eq '.' or $file eq '..';
		my $ca_filepath = "$ca_path/$file";
		if ( -l $ca_filepath ) {
		    my $basefile = readlink $ca_filepath;
		    if ( not defined $basefile ) {
			$$errormsg = "Cannot read CA symlink \"$ca_filepath\" ($!).";
			return 0;
		    }
		    elsif ( $basefile =~ m{/} ) {
			## For better security, we don't even support a "../thisdir/filename" type of symlink,
			## that goes outside the CA directory and tries to come back in.
			$$errormsg = "CA symlink \"$ca_filepath\" contains a reference to some other directory.";
			return 0;
		    }
		    elsif ( !-e $ca_filepath ) {
			$$errormsg = "CA symlink \"$ca_filepath\" points to a non-existent file.";
			return 0;
		    }
		    elsif ( !-f _ or -l "$ca_path/$basefile" ) {
			$$errormsg = "CA symlink \"$ca_filepath\" points to a non-file.";
			return 0;
		    }
		}
		elsif ( -f _ ) {
		    ( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, @rest ) = stat(_);
		    if ( $uid != $> and $uid != 0 ) {
			## We do our best to tell the caller that the filepath must be owned by
			## either the current user (presumably nagios) or by root.
			my $running_as_username = getpwuid $>;
			my $file_owner_username = getpwuid $uid;
			$running_as_username = defined($running_as_username) ? "\"$running_as_username\"" : "an unknown user (UID $>)";
			$file_owner_username = defined($file_owner_username) ? "\"$file_owner_username\"" : "an unknown user (UID $uid)";
			$$errormsg =
			    "CA filepath \"$ca_filepath\" is owned by $file_owner_username;\n"
			  . "    it must be owned by either the current user, or by root.\n"
			  . "    You are currently running your application as $running_as_username.";
			return 0;
		    }
		    elsif ( $mode & 022 ) {
			$$errormsg = "CA filepath \"$ca_filepath\" has excessively open permissions.";
			return 0;
		    }
		    elsif ( $size == 0 ) {
			$$errormsg = "CA filepath \"$ca_filepath\" is an empty file.";
			return 0;
		    }
		}
		else {
		    $$errormsg = "CA filepath \"$ca_filepath\" is a non-symlink, non-file.";
		    return 0;
		}
	    }
	}
    }

    return 1;
}

sub gw_rapid_client_ssl_opts {
    my $logger          = shift;
    my $Force_CRL_Check = shift;
    my %ssl_opts        = %standard_ssl_opts;

    $ssl_opts{SSL_check_crl} = 1;
    if ( not $Force_CRL_Check ) {
	## Check for the existence of at least one seemingly valid CRL file.
	## If we find cannot check properly, or if we find one, we leave the
	## SSL_check_crl flag enabled.  If we can check but find no such file,
	## we disable the SSL_check_crl flag.
	my $ca_path = get_ca_path();
	if ( !opendir( CAPATH, $ca_path ) ) {
	    $logger->error( "ERROR:  Cannot open CA directory \"$ca_path\" ($!)." );
	    ## If we cannot open the certificates directory, then we leave the constraints
	    ## as tight as possible, because this failure situation mitigates against
	    ## allowing a login until the client machine is correctly configured.
	}
	else {
	    my $have_crl_file = 0;
	    my @files         = readdir CAPATH;
	    foreach my $file (@files) {
		## This is a primitive check for the existence of a CRL.
		## We count on the customer not doing something dumb like
		## creating a directory that looks like a CRL file.
		if ( $file =~ /^[0-9a-fA-F]{8}\.r\d+$/ ) {
		    $have_crl_file = 1;
		    last;
		}
	    }
	    closedir(CAPATH);
	    if ( not $have_crl_file ) {
		$ssl_opts{SSL_check_crl} = 0;
	    }
	}
    }

    return \%ssl_opts;
}

# ---------------------------------------------------------------------------- #

# FIX MINOR:  Support some combination of "cert", "key", and "ca" options to provide proper SSL configurability.
sub new {
    my $invocant        = $_[0];
    my $protocol        = $_[1];
    my $hostname        = $_[2];
    my $username        = $_[3];
    my $password        = $_[4];
    my $requestor       = $_[5];
    my $options         = $_[6];
    my $class           = ref($invocant) || $invocant;    # object or class name
    my $timeout         = 60;                             # default value
    my $interruptible   = 0;
    my $force_crl_check = 1;                              # default value
    my $multithreaded   = 0;                              # default value
    my $JSON_package    = 'JSON';                         # default value; multithreaded code must use 'JSON::PP' instead
    my $restport        = undef;
    my $basepath        = undef;
    my $scrambled       = 0;
    my $self            = undef;
    local $_;

    eval {
	my $logger = (defined($options) ? $options->{logger} : undef) || Log::Log4perl::get_logger("GW.RAPID.module");

	my %valid_options = (
	    timeout         => 'integer',
	    logger          => 'logger handle',
	    restport        => 'integer',
	    basepath        => 'api path',
	    access          => 'file',
	    scrambled       => 'boolean',
	    interruptible   => 'refscalar',
	    force_crl_check => 'boolean',
	    multithreaded   => 'boolean',
	    token           => 'auth token'
	);

	if ( defined $options ) {
	    if ( attributes::reftype($options) ne 'HASH' ) {
		$logger->logcroak("ERROR:  Invalid REST API options hash.");
	    }
	    foreach my $key ( keys %$options ) {
		if ( not exists $valid_options{$key} ) {
		    $logger->logcroak("ERROR:  Unsupported REST API option '$key'.");
		}
		if ( $valid_options{$key} eq 'integer' ) {
		    if ( $options->{$key} !~ /^\d+$/ ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not an integer.");
		    }
		}
		elsif ( $valid_options{$key} eq 'logger handle' ) {
		    ## FIX MINOR:  Allow more flexibility in what is accepted as a logger handle.
		    ## Perhaps a simple open file handle would do the job, if we modify the rest of
		    ## this package to deal with such an object appropriately.
		    if ( ref $options->{$key} ne 'Log::Log4perl::Logger' ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not a logger handle.");
		    }
		}
		elsif ( $valid_options{$key} eq 'api path' ) {
		    if ( $options->{$key} !~ m{^(/[^/?&%][^?&%]*)?/api$} ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not a valid REST API base path.");
		    }
		}
		elsif ( $valid_options{$key} eq 'file' ) {
		    if ( !-f $options->{$key} or !-r _ ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not a valid readable file. Check that the permissions and ownership of the file are set correctly, for example, ownership is nagios:nagios for most conf and properties files.");
		    }
		}
		elsif ( $valid_options{$key} eq 'refscalar' ) {
		    if ( ref $options->{$key} ne 'SCALAR' ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not a valid scalar reference.");
		    }
		}
		elsif ( $valid_options{$key} eq 'boolean' ) {
		    ## There's nothing to check here.  We simply rely on Perl truth semantics.
		}
		elsif ( $valid_options{$key} eq 'auth token' ) {
		    ## We fully validate later on by trying to use the token.  But a bad token is a bad token, regardless.
		    if ( $options->{$key} !~ /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/ ) {
			$logger->logcroak("ERROR:  Invalid REST API option '$key':  \"$options->{$key}\" is not a possible auth token.");
		    }
		}
	    }
	    $restport = $options->{restport};
	    $basepath = $options->{basepath} if $options->{basepath};

	    # FIX MAJOR:  Should we allow a zero timeout here from the user?
	    $timeout         = $options->{timeout}         if defined $options->{timeout};
	    $interruptible   = $options->{interruptible}   if defined $options->{interruptible};
	    $force_crl_check = $options->{force_crl_check} if defined $options->{force_crl_check};
	    $multithreaded   = $options->{multithreaded}   if defined $options->{multithreaded};
	    $scrambled       = $options->{scrambled}       if defined $options->{scrambled};

	    if ($options->{access}) {
		## The internal eval{}; is here mostly to catch exceptions possibly thrown by TypedConfig
		## and turn them into logged messages.  But we also use it for some parameter validation.
		eval {
		    my $access_config = TypedConfig->new($options->{access});

		    if ( not defined $username ) {
			$username = $access_config->get_scalar('webservices_user');
			if ( $username =~ /^\s*$/ ) {
			    die "ERROR:  cannot find a valid \"webservices_user\" field\n";
			}
		    }

		    if ( not defined $password ) {
			$password = $access_config->get_scalar('webservices_password');
			if ( $password =~ /^\s*$/ ) {
			    die "ERROR:  cannot find a valid \"webservices_password\" field\n";
			}
		    }

		    $scrambled = 1;
		    # credentials.encryption.enabled is an optional flag
		    eval {
			$scrambled = $access_config->get_boolean('credentials.encryption.enabled');
		    };
		    if ($@ && $@ !~ / cannot find a config-file value /) {
			chomp $@;
			die "$@\n";
		    }

		    my $foundation_rest_url = $access_config->get_scalar('foundation_rest_url');
		    if ( $foundation_rest_url =~ m{^(http(?:s)?)://([-a-zA-Z0-9.]+)(?::(\d+))?((/[^/?&%][^?&%]*)?/api)$}i ) {
			$protocol = $1 if not defined $protocol;
			$hostname = $2 if not defined $hostname;
			$restport = $3 if not defined $restport;
			$basepath = $4 if not defined $basepath;
		    }
		    else {
			die "ERROR:  cannot find a valid \"foundation_rest_url\" field\n";
		    }
		};
		if ($@) {
		    chomp $@;
		    $@ =~ s/^ERROR:\s+//i;
		    $logger->logcroak("FATAL:  Cannot read config file $options->{access}:  $@\n");
		}
	    }
	}

	$JSON_package = 'JSON::PP' if $multithreaded;
	if ( not package_is_loaded($JSON_package) ) {
	    $logger->logcroak("ERROR:  Cannot load the \"$JSON_package\" package.");
	}

	$basepath = '/api' if not defined $basepath;

	$logger->logcroak("ERROR:  Invalid number of args. $usage_new") if @_ < 6 || @_ > 7;
	$logger->logcroak("ERROR:  Undefined arg(s). $usage_new")
	  if any { !defined $_ } $protocol, $hostname, $username, $password, $requestor;

	# Validate arguments, now that all option-specification precedences are resolved.
	$logger->logcroak("ERROR:  Invalid REST API protocol '$protocol'.")   if $protocol !~ m{ ^(http|https)$ }ixms;
	$logger->logcroak("ERROR:  Invalid REST API hostname '$hostname'.")   if not is_valid_dns_hostname($hostname);
	$logger->logcroak("ERROR:  Invalid REST API username.")               if $username =~ m{ ^\s*$ }xms;
	$logger->logcroak("ERROR:  Invalid REST API password.")               if $password =~ m{ ^\s*$ }xms;
	$logger->logcroak("ERROR:  Invalid REST API requestor '$requestor'.") if $requestor =~ m{ ^\s*$ }xms;

	$restport = 443 if not defined($restport) and defined($protocol) and $protocol =~ /https/i;

	# FIX MAJOR:  take this out
	# $logger->debug( "DEBUG:  REST API parameters:  protocol='$protocol' hostname='$hostname' restport='".($restport||'')."' basepath='$basepath'");

	# Create REST::Client object for use throughout.
	# There is very little error/exception handling available with REST::Client at this point.

	# We specify our own user agent initially so we can set the User-Agent header value explicitly,
	# rather than taking the default that Rest::Client provides.  But this may be helpful as well
	# for full control of SSL support, and for full control of redirects.
	my $ssl_opts = gw_rapid_client_ssl_opts( $logger, $force_crl_check );
	my $useragent = undef;
	eval {
	    $useragent = LWP::UserAgent->new( agent => $requestor, timeout => $timeout, ssl_opts => $ssl_opts, );
	};
	if ($@) {
	    chomp $@;
	    $@ =~ s/^ERROR:\s+//i;
	    $logger->logdie("FATAL:  Cannot create a user agent:  $@\n");
	}
	$useragent->requests_redirectable( [] );
	my $rest_url_base = base_url( $protocol, $hostname, $restport, $basepath );
	$logger->debug( "DEBUG:  REST API base url:  $rest_url_base" );
	my $rest_client = REST::Client->new(
	    {
		host      => $rest_url_base,
		timeout   => $timeout,
		useragent => $useragent
	    }
	);

	# At this point, $rest_client should be set but is not necessarily usable because settings need validating.
	if ( not defined $rest_client ) { $logger->logdie("INTERNAL ERROR:  REST::Client object was not defined.\n"); }

	# uri_requestor is here so we don't have to call uri_escape() during global destruction,
	# because it fails then because its own %Unsafe hash is gone before we would try to still use it.
	my %config = (
	    protocol        => $protocol,
	    hostname        => $hostname,
	    username        => $username,
	    password        => $password,
	    scrambled       => $scrambled,
	    requestor       => $requestor,
	    uri_requestor   => uri_escape($requestor),
	    logger          => $logger,
	    rest_client     => $rest_client,
	    rest_url_base   => $rest_url_base,
	    timeout         => $timeout,
	    interruptible   => $interruptible,
	    force_crl_check => $force_crl_check,
	    multithreaded   => $multithreaded,
	    JSON_package    => $JSON_package
	);
	$config{restport} = $restport if defined $restport;
	$config{basepath} = $basepath;
	$logger->debug("DEBUG:  REST::Client initialized and validated.");

	$self = bless \%config, $class;

	# Attempt to log in.  If we can do so, grab the token and save it for use in all future calls.
	# Otherwise, back down to using Basic Authentication.
	my %outcome = ();
	my %results = ();

	my $auth_status = undef;
	if ( $options->{token} ) {
	    ## Validate the token.  If the call fails, no problem; we simply log in below as usual
	    ## and in so doing, fetch a new token.  (The token is a recommendation, not a demand.)
	    $config{token} = $options->{token};
	    eval {
		if ( $self->_API_AUTH( 'validatetoken', [], {}, \%outcome, \%results ) ) {
		    ## The validatetoken call worked, so pretend we were able to log in (just below).
		    $results{token} = $options->{token};
		    $auth_status = 1;
		}
	    };
	    if ( 0 && $@ ) {
		my $exception = $@;
		chomp $exception;
		$logger->debug( "AUTH DEBUG:  the validatetoken call threw an exception:  $@" );
	    }
	    delete $config{token};
	}
	if ( not $auth_status ) {
	    eval {
		$auth_status = $self->_API_AUTH( 'login', [], {}, \%outcome, \%results );
	    };
	    if ($@) {
		$logger->logdie("ERROR:  $@\n");
	    }
	}
	if ($auth_status) {
	    $config{token} = $results{token};

	    # Without this, GET, POST and DELETE work, but PUT doesn't
	    $rest_client->addHeader( "Content-Type", "application/json" );

	    # Tell client we're ok accepting JSON data in requests
	    $rest_client->addHeader( "Accept", "application/json" );
	}
	elsif ($scrambled) {
	    ## Basic Authentication won't work in this regime.

	    my $response_error = '';
	    if ( defined $outcome{response_error}) {
		($response_error = $outcome{response_error}) =~ s/\n.*//s;
		$response_error =~ s{.*description.*<u>(.+?)</u>.*}{$1};
		$response_error .= '.' if $response_error and $response_error !~ /[.]$/;
	    }

	    $logger->logdie( "ERROR:  Foundation could not be contacted while trying to initialize the REST API.\n"
		  . ( $response_error ? "    ($response_error)\n" : '' )
		  . "    Perhaps Foundation is not yet fully running.\n" );
	}
	else {
	    ## FIX MAJOR:  drop these messages
	    foreach my $key (keys %outcome) {
		$logger->debug("AUTH DEBUG:  $key => $outcome{$key}");
	    }

	    # Without this, GET, POST and DELETE work, but PUT doesn't
	    $rest_client->addHeader( "Content-Type", "application/json" );

	    # Tell client we're ok accepting JSON data in requests
	    $rest_client->addHeader( "Accept", "application/json" );

	    # Add Authentication header
	    $rest_client->addHeader( "Authorization", "Basic " . encode_base64("${username}:${password}") );

	    my $errormsg = undef;
	    if ( not is_valid_ca_path( \$errormsg, $rest_url_base ) ) {
		$logger->logdie("ERROR:  $errormsg\n");
	    }

	    # Try to validate that the initialization will actually work.
	    # Try to get the WADL which is independent of Foundation objects existence.
	    # Getting meta stuff requires xml headers, not json headers.
	    $rest_client->request( 'GET', '/meta/wadl', '', { "Content-Type" => "application/xml", "Accept" => "application/xml" } );

	    # For certain conditions expected when Foundation is still starting up,
	    # we don't want to pollute the log file with excessive detail.
	    my $response_code    = $rest_client->responseCode();
	    my $response_content = $rest_client->responseContent();
	    if ( $response_content !~ /application\s+xmlns=/ ) {    # then we may have an authentication error possibility
		if ( $response_content =~ /This request requires HTTP authentication/ ) {
		    $logger->logdie("ERROR:  Authentication of REST API credentials failed.\n");
		}
		elsif ( $response_code == 500 ) {
		    ## The port number in the response content may be important for diagnostics.
		    $response_content =~ s/\n.*//s;
		    $logger->logdie( "ERROR:  Foundation could not be contacted while trying to initialize the REST API\n"
			  . "    ($response_code: $response_content).  Perhaps Foundation is not yet fully running.\n" );
		}
		elsif ($response_code == 404
		    || $response_code == 400 && $response_content =~ /The request sent by the client was syntactically incorrect/ )
		{
		    $logger->logdie( "ERROR:  Foundation could not be contacted while trying to initialize the REST API\n"
			  . "    ($response_code: " . status_message($response_code) . ").  Perhaps Foundation is not yet fully running.\n" );
		}
		else {
		    $logger->logdie( "ERROR:  An error occurred while trying to initialize the REST API.  The test was for /meta/wadl."
			  . "  The response status was $response_code, the response content was $response_content\n" );
		}
		die "INTERNAL ERROR";    # should never get here, but just in case
	    }
	}
    };
    if ($@) {
	return undef;
    }

    return $self;
}

# We only return success/failure so unit testing can check the outcome.
sub DESTROY {
    my $self = $_[0];
    local $@;

    $self->{logger}->debug("DEBUG:  REST API logout called.") if $self->{logger};

    my %outcome = ();
    my %results = ();

    if ( $self->{token} ) {
	my $auth_status;
	eval {
	    ## FIX MAJOR:  We might need to somehow back out the Content-Type and Accept headers before this call.
	    $auth_status = $self->_API_AUTH( "logout", [], {}, \%outcome, \%results );
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  REST API logout failed: $@") if $self->{logger};
	    return 0;
	}
	if ($auth_status) {
	    $self->{logger}->debug("DEBUG:  REST API logout succeeded.") if $self->{logger};
	    ## Prevent repeated logout attempts later on in case DESTROY was called explicitly by the user.
	    delete $self->{token};
	    return 1;
	}
	else {
	    $self->{logger}->error("ERROR:  REST API logout failed.") if $self->{logger};
	    return 0;
	}
    }
    else {
	## Nothing special to do in this case.
	return 1;
    }
}

sub package_is_loaded {
    my $package = shift;
    ## We're careful to use a form of the require that should provide some protection
    ## against Perl-injection attacks through external configuration, though of course
    ## there is no possible protection against what is in the loaded package itself.
    ## Note that we only "require" the package; we don't also "import" its symbols.
    return 0 if ! defined $package || ! $package;
    eval "require $package;";
    if ($@) {
	## 'require' died; $package is not available.
	return 0;
    } else {
	## 'require' succeeded; $package was loaded.
	return 1;
    }
}

# Internal utility routine.
sub base_url {
    my $protocol = shift;
    my $hostname = shift;
    my $restport = shift;
    my $basepath = shift;
    return "$protocol://$hostname" . ( $restport ? ":$restport" : '' ) . $basepath;
}

# Internal utility routine.
sub set_outcome {
    my $exception = $_[0];
    my $outcome   = $_[1];

    if ( $exception and ref $outcome eq 'HASH' ) {
	## Client errors are mapped into an HTTP Bad Request, so the caller
	## can have a uniform mechanism for checking the outcome hash.
	%$outcome = (
	    response_error  => $exception,
	    response_code   => 400,
	    response_status => status_message(400)
	);
    }
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting
# application types.
# get_application_types() specifics: none
# ---------------------------------------------------------------------------- #

sub get_application_types {
    my ( $self, $app_type_names, $options, $outcome, $results ) = @_;
    local $_;

    my $status = 0;
    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_application_types") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_application_types")
	  if any { !defined $_ } $self, $app_type_names, $options, $outcome, $results;

	if ( ref $app_type_names eq 'ARRAY' && @$app_type_names > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "name in ('" . join( "','", @$app_type_names ) . "') and ($options->{query})";
		$app_type_names = [];
	    }
	    elsif ( @$app_type_names > 1 ) {
		$options->{query} = "name in ('" . join( "','", @$app_type_names ) . "')";
		$app_type_names = [];
	    }
	}

	$status = $self->_API_GET( "applicationtypes", $app_type_names, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting
# the REST API version.
# get_version() specifics: none
# ---------------------------------------------------------------------------- #
sub get_version {
    my ( $self, $versions, $options, $outcome, $results ) = @_;
    local $_;

    my $status = 0;
    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_version") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_version")
	  if any { !defined $_ } $self, $versions, $options, $outcome, $results;

	if ( ref $versions eq 'ARRAY' && @$versions > 0 ) {
	    $self->{logger}->logcroak("ERROR:  Invalid number of versions (must be zero in this release). $usage_get_version");
	}

	$status = $self->_API_GET( "version", $versions, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting hosts
# get_hosts() specifics: none
# ---------------------------------------------------------------------------- #
sub get_hosts {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_hosts") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_hosts") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	if ( ref $hostnames eq 'ARRAY' && @$hostnames > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "') and ($options->{query})";
		$hostnames = [];
	    }
	    elsif ( @$hostnames > 1 ) {
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "')";
		$hostnames = [];
	    }
	}
	$status = $self->_API_GET( "hosts", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting host blacklists
# get_hostblacklists() specifics: none
# ---------------------------------------------------------------------------- #
sub get_hostblacklists {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_hostblacklists") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_hostblacklists") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	if ( ref $hostnames eq 'ARRAY' && @$hostnames > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "') and ($options->{query})";
		$hostnames = [];
	    }
	    elsif ( @$hostnames > 1 ) {
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "')";
		$hostnames = [];
	    }
	}
	$status = $self->_API_GET( "hostblacklists", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting host identities
# get_hostidentities() specifics: none
# ---------------------------------------------------------------------------- #
sub get_hostidentities {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_hostidentities") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_hostidentities") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	if ( ref $hostnames eq 'ARRAY' && @$hostnames > 0 ) {
	 # TBD test this for hostidentites
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "') and ($options->{query})";
		$hostnames = [];
	    }
	    elsif ( @$hostnames > 1 ) {  # note that this doesn't allow for mixing of uuid and hostname searching - TBD make a note in the doc for now and change it later if necessary
		$options->{query} = "hostName in ('" . join( "','", @$hostnames ) . "')";
		$hostnames = [];
	    }
	}
	$status = $self->_API_GET( "hostidentities", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting host identities autocompletion suggestions
# ---------------------------------------------------------------------------- #
sub get_hostidentities_autocomplete {
    my ( $self, $prefix, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_hostidentities_autocomplete") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_hostidentities_autocomplete") if any { !defined $_ } $self, $prefix, $options, $outcome, $results;
	$self->{logger}->logcroak("ERROR:  Prefix array should contain exactly one element - the prefix. $usage_get_hostidentities_autocomplete") if scalar @{$prefix} != 1;
	$status = $self->_API_GET( "hostidentities", $prefix, $options, $outcome, $results );
	# The _API_GET() routine internally senses its calling function's name and will do the right thing.
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting customgroups  autocompletion suggestions
# ---------------------------------------------------------------------------- #
sub get_customgroups_autocomplete {
    my ( $self, $prefix, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_customgroups_autocomplete") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_customgroups_autocomplete") if any { !defined $_ } $self, $prefix, $options, $outcome, $results;
	$self->{logger}->logcroak("ERROR:  Prefix array should contain exactly one element - the prefix. $usage_get_customgroups_autocomplete") if scalar @{$prefix} != 1;
	$status = $self->_API_GET( "customgroups", $prefix, $options, $outcome, $results );
	# The _API_GET() routine internally senses its calling function's name and will do the right thing.
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting customgroups
# get_hosts() specifics: none
# ---------------------------------------------------------------------------- #
sub get_customgroups {
    my ( $self, $cgnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_customgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_customgroups") if any { !defined $_ } $self, $cgnames, $options, $outcome, $results;
	if ( ref $cgnames eq 'ARRAY' && @$cgnames > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "name in ('" . join( "','", @$cgnames ) . "') and ($options->{query})";
		$cgnames = [];
	    }
	    elsif ( @$cgnames > 1 ) {
		$options->{query} = "name in ('" . join( "','", @$cgnames ) . "')";
		$cgnames = [];
	    }
	}
	$status = $self->_API_GET( "customgroups", $cgnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting hostsgroups
# get_hostgroups() specifics: none
# ---------------------------------------------------------------------------- #

sub get_hostgroups {
    my ( $self, $hostgroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_hostgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_hostgroups")
	  if any { !defined $_ } $self, $hostgroupnames, $options, $outcome, $results;

	if ( ref $hostgroupnames eq 'ARRAY' && @$hostgroupnames > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "name in ('" . join( "','", @$hostgroupnames ) . "') and ($options->{query})";
		$hostgroupnames = [];
	    }
	    elsif ( @$hostgroupnames > 1 ) {
		$options->{query} = "name in ('" . join( "','", @$hostgroupnames ) . "')";
		$hostgroupnames = [];
	    }
	}

	$status = $self->_API_GET( "hostgroups", $hostgroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting services
# get_services() specifics: - depth doesn't apply to GET /api/services
# ---------------------------------------------------------------------------- #

# FIX MINOR:  Deal with this condition:
# GWMON-11474:  Added API to retrieve services by a hostName parameter (hostName parameter
# is no longer ignored).  Note the query params hostName and query cannot be combined.

sub get_services {
    my ( $self, $servicenames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_services") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_services")
	  if any { !defined $_ } $self, $servicenames, $options, $outcome, $results;

	# FIX MAJOR:  drop these lines ONLY if we don't reference those objects in the rest of this routine
	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $servicenames ne 'ARRAY';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options      ne 'HASH';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome      ne 'HASH';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $results      ne 'HASH';

	# FIX MAJOR
	# Need to ensure that if $services_or_query is not a query, and not "",  that a ?hostName=something is supplied after the host
	#    if ( exists $options{query} !~ /^(query=|\s*)/ ) {
	#	     if ( $services_or_query !~ /^.*\?hostName=.*$/ ) {    # this might need reviewing for robustness TBD
	#	         $self->{logger}->logcroak(
	#                  "ERROR:  Incorrectly formatted argument. Expecting '?hostName=name' at end of arg. $usage_get_services"
	#                );
	#	         return 0;
	#	     }
	#    }

	# The Foundation REST API supports only limited forms of retrieval.  We must map our flexible,
	# extended, consistent-with-other-Perl-routines forms into the forms that the REST API can accept.
	my $query    = delete $options->{query};
	my $hostname = delete $options->{hostname};
	if ( ref $hostname eq 'ARRAY' ) {
	    if ( @$hostname == 0 ) {
		$hostname = undef;
	    }
	    elsif ( @$hostname == 1 ) {
		$hostname = $hostname->[0];
	    }
	}
	if ( @$servicenames == 0 ) {
	    if ( not defined $hostname ) {
		## No host specified.
		$options->{query} = $query if $query;
	    }
	    elsif ( ref $hostname ne 'ARRAY' ) {
		## One host specified.
		## FIX MAJOR:  validate that $hostname contains no quote characters
		$options->{query} = "hostName='$hostname'";
		$options->{query} .= " and ($query)" if $query;
	    }
	    else {
		## Multiple hosts specified.
		## FIX MAJOR:  validate that each @$hostname member contains no quote characters
		$options->{query} = "hostName in ('" . join( "','", @$hostname ) . "')";
		$options->{query} .= " and ($query)" if $query;
	    }
	}
	elsif ( @$servicenames == 1 ) {
	    if ( not defined $hostname ) {
		## No host specified.
		## FIX MAJOR:  validate that $servicenames->[0] contains no quote characters
		$options->{query} = "description='$servicenames->[0]'";
		$options->{query} .= " and ($query)" if $query;
		$servicenames = [];
	    }
	    elsif ( ref $hostname ne 'ARRAY' ) {
		## One host specified.
		if ($query) {
		    ## FIX MAJOR:  validate that $hostname and $servicenames->[0] contain no quote characters
		    $options->{query} = "hostName='$hostname' and description='$servicenames->[0]' and ($query)";
		    $servicenames = [];
		}
		else {
		    ## FIX MAJOR:  validate that $hostname contains no quote characters
		    $options->{hostName} = $hostname;
		}
	    }
	    else {
		## Multiple hosts specified.
		## FIX MAJOR:  validate that each @$hostname member contains no quote characters
		$options->{query} = "hostName in ('" . join( "','", @$hostname ) . "') and description='$servicenames->[0]'";
		$options->{query} .= " and ($query)" if $query;
		$servicenames = [];
	    }
	}
	else {
	    if ( not defined $hostname ) {
		## No host specified.
		## FIX MAJOR:  validate that each @$servicenames member contains no quote characters
		$options->{query} = "description in ('" . join( "','", @$servicenames ) . "')";
	    }
	    elsif ( ref $hostname ne 'ARRAY' ) {
		## One host specified.
		## FIX MAJOR:  validate that $hostname and each @$servicenames member contain no quote characters
		$options->{query} = "hostName='$hostname' and description in ('" . join( "','", @$servicenames ) . "')";
	    }
	    else {
		## Multiple hosts specified.
		## FIX MAJOR:  Note that we might want some sort of $option->{jointype} to specify that if the caller
		## provided exactly the same number of hostnames and servicenames, then they are intended to match up
		## as point-by-point vectors, not as a cross-product.
		## FIX MAJOR:  validate that each @hostname and @$servicenames member contains no quote characters
		$options->{query} =
		  "hostName in ('" . join( "','", @$hostname ) . "') and description in ('" . join( "','", @$servicenames ) . "')";
	    }
	    $options->{query} .= " and ($query)" if $query;
	    $servicenames = [];
	}

	$status = $self->_API_GET( "services", $servicenames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting servicesgroups
# get_servicegroups() specifics: none
# ---------------------------------------------------------------------------- #

sub get_servicegroups {
    my ( $self, $servicegroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_servicegroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_servicegroups")
	  if any { !defined $_ } $self, $servicegroupnames, $options, $outcome, $results;

	if ( ref $servicegroupnames eq 'ARRAY' && @$servicegroupnames > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "name in ('" . join( "','", @$servicegroupnames ) . "') and ($options->{query})";
		$servicegroupnames = [];
	    }
	    elsif ( @$servicegroupnames > 1 ) {
		$options->{query} = "name in ('" . join( "','", @$servicegroupnames ) . "')";
		$servicegroupnames = [];
	    }
	}

	$status = $self->_API_GET( "servicegroups", $servicegroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting events
# get_events() specifics:
# - $events_or_query can be: id,  id1,id2,id3...,  query=(query), or ""
# - in the case of id1,id2,id3... an array of event structures is returned for those events
#   that do exist, but no warning is given that the GET was only partially successful so
#   the user of get_events needs to check the existence each id submitted in the results.
# ---------------------------------------------------------------------------- #

# FIX MAJOR:  Handle recoding if the caller specifies both event IDs and a non-empty query.
sub get_events {
    my ( $self, $eventids, $options, $outcome, $results ) = @_;
    my $status = 0;  # Assume failure.
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_events")
	  if any { !defined $_ } $self, $eventids, $options, $outcome, $results;

	# We check certain parameters at this level because we are about to dereference them in this routine.
	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $eventids ne 'ARRAY';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options  ne 'HASH';

	if ( ref $eventids eq 'ARRAY' && @$eventids > 0 && ref $options eq 'HASH' && $options->{query} ) {
	    $options->{query} = "id in ('" . join( "','", @$eventids ) . "') and ($options->{query})";
	    $eventids = [];
	}

	# Basic sanity protection, so as not to overload the server with insensible requests.
	# Note that this trivial filtering is by no means sufficient to filter out other types
	# of long-running queries that might yield an enormous set of results.
	# FIX MAJOR:  is this the right treatment of $options->{count} ?  think about both zero and non-zero counts
	if ( not @$eventids and not $options->{query} and not $options->{count} ) {
	    $self->{logger}->logcroak("ERROR:  You are not allowed to retrieve all events in one call.");
	}

	$status = $self->_API_GET( "events", $eventids, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting devices
# ---------------------------------------------------------------------------- #
sub get_devices {
    my ( $self, $deviceidentifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_devices") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_devices")
	  if any { !defined $_ } $self, $deviceidentifications, $options, $outcome, $results;

	if ( ref $deviceidentifications eq 'ARRAY' && @$deviceidentifications > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "identification in ('" . join( "','", @$deviceidentifications ) . "') and ($options->{query})";
		$deviceidentifications = [];
	    }
	    elsif ( @$deviceidentifications > 1 ) {
		$options->{query} = "identification in ('" . join( "','", @$deviceidentifications ) . "')";
		$deviceidentifications = [];
	    }
	}

	$status = $self->_API_GET( "devices", $deviceidentifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting categories
# In 710, /api/categories changed so that you either get just one like this : /api/categories/{categoryName}/{entityType},
# or you get a bunch via a query.
# So - this routine should only accept a scalar with this structure  : x/y, or nothing but a query.
# ---------------------------------------------------------------------------- #
sub get_categories {
    my ( $self, $cat_ref, $options, $outcome, $results ) = @_;
    my $status = 0;
    my $categoryName_and_entityType;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_categories") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_categories") if any { !defined $_ } $self, $cat_ref, $options, $outcome, $results;

	if ( ref $cat_ref eq 'ARRAY' && scalar @{$cat_ref} > 1 ) {
	    $self->{logger}->logcroak("ERROR:  category array should contain exactly one element - the entry is of format 'catname/entityTypeName' - $usage_get_hostidentities") if scalar @{$cat_ref} != 1;
	}
	$categoryName_and_entityType = $cat_ref->[0];


	#if ( ref $categorynames eq 'ARRAY' && @$categorynames > 0 ) {
	#    if ( ref $options eq 'HASH' && $options->{query} ) {
	#	    $options->{query} = "name in ('" . join( "','", @$categorynames ) . "') and ($options->{query})";
	#	    $categorynames = [];
	#    }
	#    elsif ( @$categorynames > 1 ) {
	#	    $options->{query} = "name in ('" . join( "','", @$categorynames ) . "')";
	#	    $categorynames = [];
	#    }
	#}

	# If categoryName_and_entityType is set, test it's type is a scalar not referencing an array or hash
	if ( defined $categoryName_and_entityType and $categoryName_and_entityType )  {
		if ( ref $categoryName_and_entityType ) {
			$self->{logger}->logcroak("ERROR:  Invalid arg type - categoryName_and_entityType should be a non empty simple scalar - $usage_get_categories") ;
		}
		# check that the structure of the categoryName_and_entityType is valid
		my $slash_count = ($categoryName_and_entityType =~ tr/\///); # it just counts the number of '/'s in the string
		if ( $slash_count != 1 ) {
			$self->{logger}->logcroak("ERROR:  Invalid arg value - categoryName_and_entityType should have 1 forward slash - $usage_get_categories") ;
		}
		# Also to be safe, unset options->{query} if its set - cos its either/or.
		delete $options->{query} if exists $options->{query} ;
	}
	# otherwise must have options->{query} given
	else {
		if ( not defined $options->{query} ) {
			$self->{logger}->logcroak("ERROR:  Either categoryName_and_entityType should be a simple scalar - $usage_get_categories") ;
		}
	}

	$status = $self->_API_GET( "categories", $cat_ref, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting auditlog entries
# get_auditlogs() specifics: retrieval by auditlogids might not be supported by the REST API
# ---------------------------------------------------------------------------- #

sub get_auditlogs {
    my ( $self, $auditlogids, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_auditlogs") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_auditlogs")
	  if any { !defined $_ } $self, $auditlogids, $options, $outcome, $results;

	if ( ref $auditlogids eq 'ARRAY' && @$auditlogids > 0 ) {
	    my $id_query = @$auditlogids > 1 ? "auditLogId in (" . join( ",", @$auditlogids ) . ")" : "auditLogId = $auditlogids->[0]";
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "$id_query and ($options->{query})";
	    }
	    else {
		$options->{query} = $id_query;
	    }
	    $auditlogids = [];
	}

	$status = $self->_API_GET( "auditlogs", $auditlogids, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting property types ie dyn props
# ---------------------------------------------------------------------------- #
sub get_propertytypes {
    my ( $self, $propertiesdentifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_propertytypes") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_propertytypes") if any { !defined $_ } $self, $propertiesdentifications, $options, $outcome, $results;

	if ( ref $propertiesdentifications eq 'ARRAY' && @$propertiesdentifications > 0 ) {
	    if ( ref $options eq 'HASH' && $options->{query} ) {
		$options->{query} = "name in ('" . join( "','", @$propertiesdentifications ) . "') and ($options->{query})";
		$propertiesdentifications = [];
	    }
	    elsif ( @$propertiesdentifications > 1 ) {
		$options->{query} = "name in ('" . join( "','", @$propertiesdentifications ) . "')";
		$propertiesdentifications = [];
	    }
	}

	$status = $self->_API_GET( "propertytypes", $propertiesdentifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for getting tokens
# ---------------------------------------------------------------------------- #

sub get_tokens {
    my ( $self, $tokenids, $options, $outcome, $results ) = @_;
    my $status = 0;  # Assume failure.
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_tokens") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_tokens")
	  if any { !defined $_ } $self, $tokenids, $options, $outcome, $results;

	# We check certain parameters at this level because we are about to dereference them in this routine.
	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $tokenids ne 'ARRAY';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options  ne 'HASH';

	if ( ref $tokenids eq 'ARRAY' && @$tokenids > 0 && ref $options eq 'HASH' && $options->{query} ) {
	    $options->{query} = "value in ('" . join( "','", @$tokenids ) . "') and ($options->{query})";
	    $tokenids = [];
	}

	$status = $self->_API_GET( "settings/tokens", $tokenids, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting application types
# upsert_application_types() specifics: UPdates or inSERTs (i.e., adds) application type(s)
# ---------------------------------------------------------------------------- #
sub upsert_application_types {
    my ( $self, $app_types, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_application_types") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_application_types")
	  if any { !defined $_ } $self, $app_types, $options, $outcome, $results;

	$status = $self->_API_POST( "applicationtypes", $app_types, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting hosts
# upsert_hosts() specifics: UPdates or inSERTs (i.e., adds) host(s)
# ---------------------------------------------------------------------------- #
sub upsert_hosts {
    my ( $self, $hosts, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_hosts") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_hosts") if any { !defined $_ } $self, $hosts, $options, $outcome, $results;
	$status = $self->_API_POST( "hosts", $hosts, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting customgroups
# ---------------------------------------------------------------------------- #
sub upsert_customgroups {
    my ( $self, $customgroups, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_customgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_customgroups") if any { !defined $_ } $self, $customgroups, $options, $outcome, $results;
	$status = $self->_API_POST( "customgroups", $customgroups, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting hosts and their services via biz/hosts
# ---------------------------------------------------------------------------- #
sub upsert_bizhosts {
    my ( $self, $bizhosts, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_bizhosts") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_bizhosts") if any { !defined $_ } $self, $bizhosts, $options, $outcome, $results;
	$status = $self->_API_POST( "biz/hosts", $bizhosts, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting hosts and their services via biz/services
# ---------------------------------------------------------------------------- #
sub upsert_bizservices {
    my ( $self, $bizservices, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_bizservices") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_bizservices") if any { !defined $_ } $self, $bizservices, $options, $outcome, $results;
	$status = $self->_API_POST( "biz/services", $bizservices, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting host blacklists
# upsert_hostblacklists() specifics: UPdates or inSERTs (i.e., adds) host blacklist(s)
# ---------------------------------------------------------------------------- #
sub upsert_hostblacklists {
    my ( $self, $hosts, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_hostblacklists") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_hostblacklists") if any { !defined $_ } $self, $hosts, $options, $outcome, $results;
	$status = $self->_API_POST( "hostblacklists", $hosts, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting host identities
# upsert_hostidentities() specifics: UPdates or inSERTs (i.e., adds) host identities
# ---------------------------------------------------------------------------- #
sub upsert_hostidentities {
    my ( $self, $hosts, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_hostidentities") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_hostidentities") if any { !defined $_ } $self, $hosts, $options, $outcome, $results;
	$status = $self->_API_POST( "hostidentities", $hosts, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for POST /api/biz/setindowntime.
# $objects can refer to hosts, services, hostgroups, servicegroups
# ---------------------------------------------------------------------------- #
sub set_indowntime {
    my ( $self, $objects, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_set_indowntime") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_set_indowntime") if any { !defined $_ } $self, $objects, $options, $outcome, $results;
	$status = $self->_API_POST( "biz/setindowntime", $objects, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for POST /api/biz/getindowntime.
# Yes - thats right - a POST to get stuff. Go figure.
# $objects can refer to hosts, services, hostgroups, servicegroups
# ---------------------------------------------------------------------------- #
sub get_indowntime {
    my ( $self, $objects, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_get_indowntime") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_get_indowntime") if any { !defined $_ } $self, $objects, $options, $outcome, $results;
	$status = $self->_API_POST( "biz/getindowntime", $objects, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}
# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for POST /api/biz/clearindowntime.
# Yes - thats right - a POST to get stuff. Go figure.
# $objects can refer to hosts, services, hostgroups, servicegroups
# ---------------------------------------------------------------------------- #
sub clear_indowntime {
    my ( $self, $objects, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_clear_indowntime") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_clear_indowntime") if any { !defined $_ } $self, $objects, $options, $outcome, $results;
	$status = $self->_API_POST( "biz/clearindowntime", $objects, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}


# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting hostgroups
# upsert_hostgroups() specifics: UPdates or inSERTs (i.e., adds) hostgroup(s)
# ---------------------------------------------------------------------------- #

sub upsert_hostgroups {
    my ( $self, $hostgroups, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_hostgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_hostgroups")
	  if any { !defined $_ } $self, $hostgroups, $options, $outcome, $results;

	$status = $self->_API_POST( "hostgroups", $hostgroups, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting
# services as attached to hosts.
# upsert_services() specifics: - UPdates or inSERTs (i.e., adds) services attached to hosts
# ---------------------------------------------------------------------------- #

sub upsert_services {
    my ( $self, $services, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_services") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_services")
	  if any { !defined $_ } $self, $services, $options, $outcome, $results;

	$status = $self->_API_POST( "services", $services, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting servicegroups
# upsert_servicegroups() specifics: UPdates or inSERTs (i.e., adds) servicegroup(s)
# ---------------------------------------------------------------------------- #

sub upsert_servicegroups {
    my ( $self, $servicegroups, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_servicegroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_servicegroups")
	  if any { !defined $_ } $self, $servicegroups, $options, $outcome, $results;

	$status = $self->_API_POST( "servicegroups", $servicegroups, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for creating
# host notifications.
# ---------------------------------------------------------------------------- #

sub create_noma_host_notifications {
    my ( $self, $host_notifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_create_noma_host_notifications") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_create_noma_host_notifications")
	  if any { !defined $_ } $self, $host_notifications, $options, $outcome, $results;

	$status = $self->_API_POST( "notifications/hosts", $host_notifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for creating
# service notifications.
# ---------------------------------------------------------------------------- #

sub create_noma_service_notifications {
    my ( $self, $service_notifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_create_noma_service_notifications") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_create_noma_service_notifications")
	  if any { !defined $_ } $self, $service_notifications, $options, $outcome, $results;

	$status = $self->_API_POST( "notifications/services", $service_notifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for creating
# events (not updating tho - the API is different in this respect in that it doesnt
# allow upserting of events - instead POST to create, PUT to update).
# ---------------------------------------------------------------------------- #

sub create_events {
    my ( $self, $events, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_create_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_create_events")
	  if any { !defined $_ } $self, $events, $options, $outcome, $results;

	$status = $self->_API_POST( "events", $events, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for
# sending performance data to Foundation.
# ---------------------------------------------------------------------------- #

sub create_performance_data {
    my ( $self, $perfdata, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_create_performance_data") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_create_performance_data")
	  if any { !defined $_ } $self, $perfdata, $options, $outcome, $results;

	$status = $self->_API_POST( "perfdata", $perfdata, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for
# sending auditlog data to Foundation.
# ---------------------------------------------------------------------------- #

sub create_auditlogs {
    my ( $self, $auditlogs, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_create_auditlogs") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_create_auditlogs")
	  if any { !defined $_ } $self, $auditlogs, $options, $outcome, $results;

	$status = $self->_API_POST( "auditlogs", $auditlogs, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting devices
# upsert_devices() specifics: - UPdates or inSERTs (i.e., adds) devices(s)
# ---------------------------------------------------------------------------- #
sub upsert_devices {
    my ( $self, $devices, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_devices") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_devices")
	  if any { !defined $_ } $self, $devices, $options, $outcome, $results;

	$status = $self->_API_POST( "devices", $devices, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting propertytypes
# upsert_propertytypes() specifics: - UPdates or inSERTs (i.e., adds) propertytypes(s)
# ---------------------------------------------------------------------------- #
sub upsert_propertytypes {
    my ( $self, $propertytypes, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_propertytypes") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_propertytypes")
	  if any { !defined $_ } $self, $propertytypes, $options, $outcome, $results;

	$status = $self->_API_POST( "propertytypes", $propertytypes, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for upserting categories
# upsert_categories() specifics: UPdates or inSERTs (i.e., adds) categories
# ---------------------------------------------------------------------------- #

sub upsert_categories {
    my ( $self, $categories, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_upsert_categories") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_upsert_categories")
	  if any { !defined $_ } $self, $categories, $options, $outcome, $results;

	$status = $self->_API_POST( "categories", $categories, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for acknowledging events
# ack_events() specifics: acknowledges all open events for host or host-service patterns
# ---------------------------------------------------------------------------- #

sub ack_events {
    my ( $self, $patterns, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_ack_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_ack_events")
	  if any { !defined $_ } $self, $patterns, $options, $outcome, $results;

	$status = $self->_API_POST( "events/ack", $patterns, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_POST() - this is a wrapper around _API_POST() for unacknowledging events
# unack_events() specifics: unacknowledges all (selection criteria?) events for host or host-service patterns
# ---------------------------------------------------------------------------- #

sub unack_events {
    my ( $self, $patterns, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_unack_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_unack_events")
	  if any { !defined $_ } $self, $patterns, $options, $outcome, $results;

	$status = $self->_API_POST( "events/unack", $patterns, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_PUT() - this is a wrapper around _API_PUT() for updating events
# Example: PUT /api/events/50,51,52?opStatus=NOTIFIED&updatedBy=admin&comments=testing+123
# ---------------------------------------------------------------------------- #
sub update_events {
    my ( $self, $eventids, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_update_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_update_events") if any { !defined $_ } $self, $eventids, $options, $outcome, $results;
	$status = $self->_API_PUT( "events", $eventids, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_PUT() - this is a wrapper around _API_PUT() for adding
# hostgroup/servicegroup members from existing custom groups without resetting them like POST via upsert does
# ---------------------------------------------------------------------------- #
sub add_customgroups_members {
    my ( $self, $members, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_add_customgroups_members") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_add_customgroups_members") if any { !defined $_ } $self, $members, $options, $outcome, $results;
	$status = $self->_API_PUT( "customgroups/addmembers", $members, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_PUT() - this is a wrapper around _API_PUT() for deleting
# hostgroup/servicegroup members from existing custom groups without resetting them like POST via upsert does
# ---------------------------------------------------------------------------- #
sub delete_customgroups_members {
    my ( $self, $members, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_customgroups_members") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_customgroups_members") if any { !defined $_ } $self, $members, $options, $outcome, $results;
	$status = $self->_API_PUT( "customgroups/deletemembers", $members, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting application types
# ---------------------------------------------------------------------------- #

# FIX MAJOR:  It would perhaps be dangerous to allow easy deletion of all application types.  So disallow that.
# FIX MAJOR:  It would perhaps be dangerous to allow deletion of our standard application types.  So disallow that.
sub delete_application_types {
    my ( $self, $app_type_names, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_application_types") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_application_types")
	  if any { !defined $_ } $self, $app_type_names, $options, $outcome, $results;

	$status = $self->_API_DELETE( "applicationtypes", $app_type_names, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting hosts
# ---------------------------------------------------------------------------- #
# FIX MAJOR:  It would perhaps be dangerous to allow easy deletion of all hosts.  So disallow that.
sub delete_hosts {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_hosts") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_hosts") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	$status = $self->_API_DELETE( "hosts", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting host blacklists
# ---------------------------------------------------------------------------- #
# FIX ?  It might  be dangerous to allow easy deletion of all host black lists, so disallow that.
sub delete_hostblacklists {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_hostblacklists") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_hostblacklists") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	$status = $self->_API_DELETE( "hostblacklists", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting host identities
# ---------------------------------------------------------------------------- #
sub delete_hostidentities {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_hostidentities") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_hostidentities") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	$status = $self->_API_DELETE( "hostidentities", $hostnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting customgroups
# ---------------------------------------------------------------------------- #
sub delete_customgroups {
    my ( $self, $cgnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_customgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_customgroups") if any { !defined $_ } $self, $cgnames, $options, $outcome, $results;
	$status = $self->_API_DELETE( "customgroups", $cgnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for clearing host identities
# ---------------------------------------------------------------------------- #
sub clear_hostidentities {
    my ( $self, $hostnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_hostidentities") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_hostidentities") if any { !defined $_ } $self, $hostnames, $options, $outcome, $results;
	$status = $self->_API_DELETE( "hostidentities", $hostnames, $options, $outcome, $results );
	# This looks identical to the delete call within delete_hostidentities().  To tell them
	# apart, the _API_DELETE() routine internally senses its calling function's name.
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting hostgroups
# ---------------------------------------------------------------------------- #

sub delete_hostgroups {
    my ( $self, $hostgroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_hostgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_hostgroups")
	  if any { !defined $_ } $self, $hostgroupnames, $options, $outcome, $results;

	$status = $self->_API_DELETE( "hostgroups", $hostgroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting services from a host
# ---------------------------------------------------------------------------- #

sub delete_services {
    my ( $self, $servicenames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_services") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_services")
	  if any { !defined $_ } $self, $servicenames, $options, $outcome, $results;

	# FIX MAJOR:  drop these lines ONLY if we don't reference those objects in the rest of this routine
	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $servicenames ne 'ARRAY';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options      ne 'HASH';
	$self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome      ne 'HASH';
	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $results      ne 'ARRAY';

	# FIX MAJOR:  add logic here to handle hostname and servicename combinations similar to
	# what we did for get_services(), depending on what the Foundation REST API will support

	# FIX MAJOR:  ensure that not all services on all hosts get deleted in one call
	my $hostname = delete $options->{hostname};
	if ( not defined $hostname ) {
	    ## FIX MAJOR:  log an error, and return failure to the caller, with appropriate outcome and results
	    return 0;
	}
	elsif ( ref $hostname ne 'ARRAY' || @$servicenames == @$hostname ) {
	    ## Single hostname, or the number of hostnames matches the number of servicenames
	    $options->{hostName} = $hostname;
	}
	else {
	    ## FIX MAJOR:  log an error, and return failure to the caller, with appropriate outcome and results;
	    ## or pass this in to _API_DELETE and do the work there
	    return 0;
	}

	$status = $self->_API_DELETE( "services", $servicenames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting servicegroups
# ---------------------------------------------------------------------------- #

sub delete_servicegroups {
    my ( $self, $servicegroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_servicegroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_servicegroups")
	  if any { !defined $_ } $self, $servicegroupnames, $options, $outcome, $results;

	$status = $self->_API_DELETE( "servicegroups", $servicegroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting events
# ---------------------------------------------------------------------------- #

sub delete_events {
    my ( $self, $eventids, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_events") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_events")
	  if any { !defined $_ } $self, $eventids, $options, $outcome, $results;

	$status = $self->_API_DELETE( "events", $eventids, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting devices
# ---------------------------------------------------------------------------- #

sub delete_devices {
    my ( $self, $deviceidentifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_devices") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_devices")
	  if any { !defined $_ } $self, $deviceidentifications, $options, $outcome, $results;

	$status = $self->_API_DELETE( "devices", $deviceidentifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for deleting categories
# ---------------------------------------------------------------------------- #

sub delete_categories {
    my ( $self, $categorynames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_delete_categories") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_delete_categories")
	  if any { !defined $_ } $self, $categorynames, $options, $outcome, $results;

	$status = $self->_API_DELETE( "categories", $categorynames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for emptying but not deleting hostgroups
# ---------------------------------------------------------------------------- #

sub clear_hostgroups {
    my ( $self, $hostgroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_clear_hostgroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_clear_hostgroups")
	  if any { !defined $_ } $self, $hostgroupnames, $options, $outcome, $results;

	# This looks identical to the delete call within delete_hostgroups().  To tell them
	# apart, the _API_DELETE() routine internally senses its calling function's name
	# (actually, its caller's caller's subroutine name, since it has to account for
	# an extra calling layer for the eval{} we're now in).
	$status = $self->_API_DELETE( "hostgroups", $hostgroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_DELETE() - this is a wrapper around _API_DELETE() for emptying but not deleting servicegroups
# ---------------------------------------------------------------------------- #

sub clear_servicegroups {
    my ( $self, $servicegroupnames, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_clear_servicegroups") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_clear_servicegroups")
	  if any { !defined $_ } $self, $servicegroupnames, $options, $outcome, $results;

	# This looks identical to the delete call within delete_servicegroups().  To tell them
	# apart, the _API_DELETE() routine internally senses its calling function's name
	# (actually, its caller's caller's subroutine name, since it has to account for
	# an extra calling layer for the eval{} we're now in).
	$status = $self->_API_DELETE( "servicegroups", $servicegroupnames, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ---------------------------------------------------------------------------- #
# See comments for _API_GET() - this is a wrapper around _API_GET() for checking
# license-related status.
# check_license() specifics: none
# ---------------------------------------------------------------------------- #

sub check_license {
    my ( $self, $deviceidentifications, $options, $outcome, $results ) = @_;
    my $status = 0;
    local $_;

    eval {
	$self->{logger}->logcroak("ERROR:  Invalid number of args. $usage_check_license") if @_ != 5;
	$self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage_check_license")
	  if any { !defined $_ } $self, $deviceidentifications, $options, $outcome, $results;

	$status = $self->_API_GET( "license/check", $deviceidentifications, $options, $outcome, $results );
    };
    set_outcome( $@, $outcome );

    return $status;
}

# ================ private/internal module functions ================

# FIX MAJOR:  revise this routine and its doc
# ---------------------------------------------------------------------------- #
# Usage   : $api->_API_AUTH( $api_method, \@objects, \%options, \%outcome, \%results )
# Purpose : UPdates or inSERTs objects like hosts, hostgroups, etc
# Returns : 1 on success, plus results hash populated with response
#         : 0 on failure, warning, etc., with outcome hash populated with details
# Params  : $api_method - a REST API method, such as 'host', 'hostgroup', etc
#         : $objects - a ref to an array containing objects to be created or updated
#         : $options - a ref to a hash containing options
#         : $outcome - a ref to a hash where success/failure details will be returned
#         : $results - a ref to a hash where details of of created/updated objects will be returned
#            Note that the results hash has normal Perl data structures, not JSON.
# Throws  : see logdie()
# Comments: This module will perform JSON encoding so that the consumer
#            only has to focus on Perl data structures and not also JSON.
#            Validation of options will be done by the REST API.
# See also: upsert_hosts() for example
# ---------------------------------------------------------------------------- #

# $objects is here for consistency with other apis, and possible future extension
# (though we don't have a good idea what it might be used for).
#
# We test for $self->{logger} in a variety of places here because this routine might be
# called during global destruction, when $self->{logger} might no longer exist.  In that
# case, it's not terribly likely that the rest of the routine will succeed, but it's worth
# trying and not letting this one fact interfere.  At the very least, if this routine fails
# in that situation, we will at least generate a sensible error message on STDERR, which
# can also be captured into the log file.
sub _API_AUTH {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;
    local $_;

    my $caller = ( caller(2) )[3];
    $caller = ( caller(1) )[3] if not defined($caller) or $caller eq '(eval)';

    my $start_time;
    if ( $self->{logger} and $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering $caller");
    }

    local $SIG{INT}  = 'DEFAULT' if $self->{interruptible};
    local $SIG{QUIT} = 'DEFAULT' if $self->{interruptible};
    local $SIG{TERM} = 'DEFAULT' if $self->{interruptible};
    if ( $self->{interruptible} and ${ $self->{interruptible} } ) {
	## An interrupt must have come in before we could turn off the signal handlers.  If we
	## proceeded now in spite of that, we would end up calling the REST API in a possibly
	## long-running call, and it would be as though we had not even bothered to disable the
	## signal handlers.  So ordinarily, we would abort now, before we get that far.  But
	## the _API_AUTH routine is a special case, because the termination signal may have been
	## already recognized by other code, and we want to allow cleanup of the server session
	## if possible before we go down.  (If we get a second termination signal while waiting
	## for a logout to happen, then that will unceremoniously stop us.)
	$self->{logger}->logdie("FATAL:  a termination signal was recognized during a call to $caller") if $api_method ne 'logout';
    }

    # a hash of valid api POST methods
    # Unfortunately, the use of mixed case in the REST API object naming forces us to create a map here
    # of actual object names to use in the generated JSON, instead of just using $api_method directly.
    my %api_post_methods = ( 'login' => 1, 'logout' => 1, 'validatetoken' => 1 );

    # Validate arguments.
    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage__API_POST") if @_ != 6;
    $self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage__API_POST")
      if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;
    $self->{logger}->logcroak("ERROR:  unrecognized API command root '$api_method'")
      if not defined $api_post_methods{$api_method};

    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $results ne 'HASH';

    my (
	$response_content,              # the POST response content/body
	$http_response_code,            # the POST HTTP response code
	$ref_decoded_response,          # a HASH reference of the decoded JSON version of the response content
	$error_status,                  # stores some info about an error if the response was not 200, or 404
	## $json_encoded_instructions,     # JSON encoded instructions hash
	$encoded_instructions,          # encoded instructions hash
	$full_post_url,                 # the full post url, starting with /$api_method
    ) = undef;

    # A call to $self->{rest_client}->POST will fail during global destruction, bringing down
    # the entire application, because $self->{rest_client} will no longer exist.  So we work
    # around that as best we can.
    if ( $self->{rest_client} ) {
	## Construct POST url
	$full_post_url = "/auth/$api_method";
	$self->{logger}->debug("DEBUG:  Full POST URL:  '$full_post_url'") if $self->{logger};

	# Encode the instructions hash into JSON.
	# Encoding the readable instructions unconditionally is an expensive operation,
	# which should only be undertaken if we know that the data will be logged.
	my $is_stats = $self->{logger} && $self->{logger}->can('is_stats') && $self->{logger}->is_stats();
	if ( $self->{logger} && ( $is_stats || $self->{logger}->is_trace() ) ) {
	    ## my $level = $is_stats ? 'stats' : 'trace';
	    ## my $json_readable_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode( { $api_post_objects{$api_method} => $objects } );
	    ## $self->{logger}->$level("...$full_post_url POST JSON:\n$json_readable_instructions");
	}
	## $json_encoded_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode( { $api_post_objects{$api_method} => $objects } );

	if ($api_method eq 'login') {
	    $encoded_instructions =
		"gwos-app-name=" . $self->{uri_requestor}                 . '&'
	      . "user="          . encode_base64( $self->{username}, '' ) . '&'
	      . "password="      . ( $self->{scrambled} ? uri_escape( $self->{password} ) : encode_base64( $self->{password}, '' ) );
	}
	elsif ($api_method eq 'validatetoken') {
	    # Both the app name and the api token must match what's on the server side for validation to succeed.
	    $encoded_instructions = "gwos-app-name=$self->{uri_requestor}&gwos-api-token=$self->{token}";
	}
	elsif ($api_method eq 'logout') {
	    ## If the calling application did not take care to remove its handle to its instance of
	    ## GW::RAPID before the process ended, it is likely that (due to no guarantees of object
	    ## ordering during global destruction), the various $self->{...} objects will be gone by
	    ## the time we get here.  That can cause an error message during process shutdown (sent to
	    ## the log file by Perl via STDERR, not through the logger, which we probably won't still
	    ## have a valid handle to any more in this situation).  We sidestep one such error message
	    ## by substituting a dummy requestor name in such a case.  However, there is no guarantee
	    ## that anything else in the rest of this routine will still work.  In particular, any
	    ## $self->{logger} and $self->{rest_client} references may be gone in that situation, in
	    ## which case the code will fail anyway once we try to log anything or attempt to complete
	    ## the logout process.

	    $encoded_instructions = $self->{token}
	      ? "gwos-app-name=" . ( $self->{uri_requestor} || 'DEAD REQUESTOR' ) . "&gwos-api-token=$self->{token}"
	      : '';
	}
	else {
	    ## FIX MAJOR
	    $encoded_instructions = '';
	}

	# FIX MAJOR:  drop this after initial development, or perhaps convert to TRACE
	$self->{logger}->debug("DEBUG:  Full POST data:  '$encoded_instructions'") if $self->{logger};

	my $errormsg = undef;
	if ( not is_valid_ca_path( \$errormsg, $self->{rest_url_base} ) ) {
	    if ( $self->{logger} ) {
		$self->{logger}->logdie("ERROR:  $errormsg\n");
	    }
	    else {
		die("ERROR:  $errormsg\n");
	    }
	}

	# Set the output structures to empty (i.e., no data yet).
	%$outcome = ();
	%$results = ();
	my $ua_res;

	# DN 0.8.7 - replaces commented out block below.  This is for http://jira/browse/GWMON-12697.
	# The rest_client->POST call works for logout (and login) iff text/plain header is explicitly added.
	# The /api/auth/logout endpoint requires this header otherwise it pukes with a 406 no match for accept header error presumably because it's missing.
	# This adjustment doesn't effect login - it doesn't have a problem with it missing. I think the docs on this endpoint need revising.
	# The uri_requestor which was uri_escaped initially and then used for login.
	# The logout was failing though because the 0.8.3 fix wasn't doing the right thing for uri_requestors that encoded chars in them (like spaces),
	# when using usagent->post(). This fix works for such encoded uri_requestors for login and logout now.
	$self->{rest_client}->POST( $full_post_url, $encoded_instructions, { "Accept" => "text/plain", "Content-Type" => "application/x-www-form-urlencoded" } );
	$http_response_code = $self->{rest_client}->responseCode();
	$response_content   = $self->{rest_client}->responseContent();

#	# DN 0.8.3 As of some version of GW REST /auth/logout, the Accept header was corrected to use text/plain.
#	# Trying to do a REST::Client POST using format 'http://localhost:8080/foundation-webapp/api/auth/logout?gwos-api-token=<token>&gwos-app-name=<appname>'
#	# no longer works prob'y because of the Accept header now being text/plain. This results in an invalid gwos-app-name or gwos-api-token error.
#	# The REST::Client POST always seems to construct this format. To work around this, I dropped it back to using the underlying LWP::UserAgent instead
#	# which seems to do the right thing with a POST set of params.
#	# Probably should still check for $self->{token} being set and set DEAD REQUESTOR but it's totally minor and probably overkill
#	if ( $api_method eq 'logout' ) {  # 0.8.3
#		#$self->{rest_client}->POST( $full_post_url, $encoded_instructions, { "Accept" => "text/plain" } ); # no this doesn't work - see note above
#		$ua_res = $self->{rest_client}->{_config}->{useragent}->post(
#			"$self->{rest_client}->{_config}->{host}$full_post_url",
#			{ "gwos-app-name" => uri_unescape($self->{uri_requestor}),  "gwos-api-token" => $self->{token}  }  # RG 0.8.7
#		);
#
#		$http_response_code = $ua_res->{_rc};
#		$response_content   = $ua_res->{_content};
#	}
#	else {
#		$self->{rest_client}->POST( $full_post_url, $encoded_instructions, { "Content-Type" => "application/x-www-form-urlencoded" } );
#		$http_response_code = $self->{rest_client}->responseCode();
#		$response_content   = $self->{rest_client}->responseContent();
#	}
#
#	# These are now different depending on method being called - see above 083 mod.
#	#$http_response_code = $self->{rest_client}->responseCode();
#	#$response_content   = $self->{rest_client}->responseContent();

    }
    else {
	$http_response_code = 424;  # 424 => "Method Failure" according to Wikipedia; "Failed Dependency" in practice here.
	$response_content   = "Cannot make a REST call without a REST client object in play."
    }

    # With the case of a POST, HTTP status 200 on success of doing the POST
    # which does NOT necessarily mean successful host(s) upsert(s)
    if ( $http_response_code == 200 ) {
	## FIX MAJOR:  strip quotes from this debug message, later on;
	## for now, we want to see if we get any trailing newlines that we need to strip.
	$self->{logger}->debug("POST response:\n'$response_content'") if $self->{logger};
	$outcome->{successful} = 1;
	$outcome->{operation} = $api_method;
	if ($api_method eq 'login') {
	    ## FIX MAJOR:  consider chomping before assignment
	    $results->{token} = $response_content;
	}
	elsif ($api_method eq 'validatetoken') {
	    if ( $self->{logger} and $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting $caller (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    ## No need to chomp before comparison.
	    return $outcome->{valid} = $response_content eq 'true' ? 1 : 0;
	}
	elsif ($api_method eq 'logout') {
	    ## There won't be any useful data returned in this case;
	    ## the caller should just check the return code.
	}
	else {
	    ## FIX LATER:  throw an exception or somesuch in this case.
	}
	if ( $self->{logger} and $self->{logger}->is_info() ) {
	    $self->{logger}->info( "INFO:  exiting $caller (call took "
		  . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		  . " seconds)" );
	}
	return 1;
    }
    else {
	## For any HTTP-level error, we manufacture a fixed structure to return to the caller.
	$outcome->{response_error}  = $response_content;
	$outcome->{response_code}   = $http_response_code;
	$outcome->{response_status} = status_message($http_response_code);

	if ( $self->{logger} ) {
	    if ( $caller eq 'GW::RAPID::new' ) {
		## We want reduced logging in this common case because we know the calling code will handle it.
		$self->{logger}->error( "ERROR:  POST error during REST API $api_method (Status code $http_response_code:  " . status_message($http_response_code) . ")" );
	    }
	    else {
		$self->{logger}->error( "ERROR:  POST error during REST API $api_method:  " . $self->analyze_error( $http_response_code, \$response_content ) );
	    }
	}
	else {
	    ## Let's get the word out, somehow.  This is most likely to appear if you forgot to
	    ## "$rest_api = undef;" to destroy the GW::RAPID handle, before exiting your application.
	    print STDERR "ERROR:  POST error during REST API $api_method:  " . $self->analyze_error( $http_response_code, \$response_content ) . "\n";
	    print STDERR "ERROR:  Did you forget to undefine your GW::RAPID handle before exiting your application?\n"
	      if not $self->{rest_client};
	}
	if ( $self->{logger} and $self->{logger}->is_info() ) {
	    $self->{logger}->info( "INFO:  exiting $caller (call took "
		  . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		  . " seconds)" );
	}
	return 0;    # failed to pass muster
    }

    if ( $self->{logger} and $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting $caller (call took "
	      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
	      . " seconds)" );
    }
    return 0;    # should not get here; just in case, return failure
}

# Internal utility routine, generally called only when the server has
# timed out our existing token and we need to capture a new token.
sub _API_REAUTH {
    my ( $self, $outcome ) = @_;
    my %results = ();
    my $status  = 0;

    my $start_time;
    if ( $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering " . ( caller(2) )[3] );
    }

    if ( $self->{token} ) {
	my $auth_status;
	eval {
	    $auth_status = $self->_API_AUTH( 'login', [], {}, $outcome, \%results );
	};
	if ($@) {
	    chomp $@;
	    $self->{logger}->error("ERROR:  internal REST API re-authorization failed: $@");
	}
	if ( $auth_status ) {
	    $self->{token} = $results{token};
	    $self->{logger}->debug("DEBUG:  internal REST API re-authorization succeeded.");
	    $status = 1;
	}
	else {
	    $self->{logger}->error("ERROR:  internal REST API re-authorization failed.");
	}
    }
    else {
	## In the case where we're using non-token authentication, logically there is
	## nothing to do here, so logically we ought to return a success.  But relogin
	## will only be called from within GW::RAPID when authorization has failed in
	## some earlier call, and we don't want to now pretend that we have successfully
	## re-authorized.  So we simply return a failure in this case.
    }

    if ( $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting "
	      . ( caller(2) )[3]
	      . " (call took "
	      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
	      . " seconds)" );
    }
    return $status;
}

# FIX MAJOR:  this doc is just plain wrong
# ---------------------------------------------------------------------------- #
# Usage   : $api->GET_API( $api_method, \@objects, \%options, \%outcome, \%results );
# Purpose : Retrieve details using GET about things (things are hosts, hostgroups, etc)
# Returns : 0 and Empty, or 1 and populated results hash on failure or success respectively
#            Note that a ref to a hash is passed in, and then that ref'd to hash is populated.
#            Thats different to returning a reference to a results hash. Assumption is it will
#            be easier to use a populated hash, rather than a reference to a hash. Same approach
#            will be taken with other functions.
# Params  : $api_method - a REST API method, such as 'host', 'hostgroup', etc
#         : $thing_to_get - the name, query, etc. of thing being GET'd. If "", then returns all things
#                            (or whatever the API does). If query=... then query mode.
#         : \%results - ref to a hash which this routine will populate with results
#            optional depth, first and count params are embedded at the start of the thing to get
#            (e.g., ?depth=simple&query=xyz)
# Throws  : see logdie()s
# Comments: This is an abstraction of get_hosts(), get_hostgroups(), etc. - see those subs that call this
# See also: get_hosts(), get_hostgroups(),...
# ---------------------------------------------------------------------------- #
sub _API_GET {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;
    local $_;

    my $start_time;
    if ( $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering " . ( caller(2) )[3] );
    }

    local $SIG{INT}  = 'DEFAULT' if $self->{interruptible};
    local $SIG{QUIT} = 'DEFAULT' if $self->{interruptible};
    local $SIG{TERM} = 'DEFAULT' if $self->{interruptible};
    if ( $self->{interruptible} and ${ $self->{interruptible} } ) {
	## An interrupt must have come in before we could turn off the signal handlers.  If we
	## proceeded now in spite of that, we would end up calling the REST API in a possibly
	## long-running call, and it would be as though we had not even bothered to disable the
	## signal handlers.  So instead, we must abort now, before we get that far.
	$self->{logger}->logdie( "FATAL:  a termination signal was recognized during a call to " . ( caller(2) )[3] );
    }

    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage__API_GET") if @_ != 6;
    $self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage__API_GET")
      if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;

    # a hash of valid api GET objects
    my %api_get_objects = (
	"version"          => "version",
	"applicationtypes" => "applicationTypes",
	"hosts"            => "hosts",
	"hostblacklists"   => "hostBlacklists",
	"hostidentities"   => "hostIdentities",
	"hostgroups"       => "hostGroups",
	"services"         => "services",
	"servicegroups"    => "serviceGroups",
	"events"           => "events",
	"devices"          => "devices",
	"propertytypes"    => "propertyTypes",
	"categories"       => "categories",
	"customgroups"     => "customGroups",
	"auditlogs"        => "auditLogs",
	"settings/tokens"  => "tokens",
	"license/check"    => "top-level"           # dummy object name; we get no set of objects back
    );

    # The most human-friendly unique ID handles for supported objects that form a single-level hash key.
    # In the case of services, we perhaps ought to return a two-level $results->{description}{hostName}
    # or perhaps $results->{hostName}{description} hash.  But for the moment, until we hash that out
    # (so to speak), we are using what we do know to be a single unique identifier ('id').
    my %api_get_ids = (
	"version"          => "version",
	"applicationtypes" => "name",
	"hostblacklists"   => "hostBlacklists",
	"hostidentities"   => "hostIdentities",
	"customgroups"     => "customGroups",
	"hosts"            => "hostName",
	"hostgroups"       => "name",
	"services"         => "id",                 # may be overridden by $options->{format}
	"servicegroups"    => "name",
	"events"           => "id",
	"devices"          => "identification",
	"propertytypes"    => "name",
	"categories"       => "name",
	"auditlogs"        => "auditLogId",
	"settings/tokens"  => "value",
	"license/check"    => "dummy-id"            # dummy ID name; not used
    );

    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $results ne 'HASH';

    # Validate arguments.
    $self->{logger}->logcroak("ERROR:  unrecognized API command root '$api_method'")
      if not defined $api_get_objects{$api_method};

    my (
	$response_content,        # the GET response content/body
	$http_response_code,      # the GET HTTP response code
	$ref_decoded_response,    # a HASH reference of the decoded JSON version of the response content
	$full_get_url,            # the final REST GET url
    ) = undef;

    my %query_data = ();                               # hash for param list
    my $thing_to_get = join( ',', @$objects ) || $options->{query} || '';

    my $format = delete( $options->{format} ) || 'id';

    my $callback = delete $options->{callback};
    my $callbackargs = delete( $options->{callbackargs} ) || [];

    # A thing to get can either be something with a query or something without a query.
    # In either case, the thing to get may or may not have some other params such as depth, first, count, etc. all,
    # delimited by the usual uri param field separators [&?].
    # These are expected to occur BEFORE the query=xyz (not after).
    # The only thing that needs uri escaping is the xyz in query=<xyz>. Everything else can be passed through as-is.

    $full_get_url = "/$api_method";
    if ($api_method eq 'auditlogs' && $options->{hostname}) {
	my $hostname = delete $options->{hostname};
	$full_get_url .= "/$hostname";
	if ($options->{servicename}) {
	    my $servicename = delete $options->{servicename};
	    $full_get_url .= "/$servicename";
	}
    }
    if ($api_method eq 'license/check' && not $options->{allocate}) {
	$options->{allocate} = 0;
    }
    if ( ( caller(2) )[3] eq "GW::RAPID::get_hostidentities_autocomplete" or ( caller(2) )[3] eq "GW::RAPID::get_customgroups_autocomplete") {
	$full_get_url .= '/autocomplete';
    }

    if ($api_method eq 'categories' ) {
	my ( $cat_name, $cat_etype ) = split '/', @$objects[0]; # this should have already been checked by get_categories() earlier.
    	$full_get_url .= '/' . uri_escape( $cat_name ) . "/" . uri_escape( $cat_etype ); # don't escape the / that separates the two things
    }
    else {
    	$full_get_url .= '/' . join( ',', map { uri_escape($_) } @$objects ) if @$objects;
    }

    $full_get_url .= '?' . join( '&', map { uri_escape($_) . '=' . uri_escape( $options->{$_} ) } keys %$options ) if %$options;

    # FIX MAJOR
    $self->{logger}->debug("DEBUG:  $api_method request host:  " . $self->{rest_client}->getHost());

    $self->{logger}->debug("DEBUG:  $api_method full GET URL:  '$full_get_url'");

    # validation of depth, first, and count should be done by REST API
    # FIX MINOR:  create a JIRA to make that happen

    # FIX MAJOR:  drop this
    if (0) {
	my $headers = $self->{rest_client}{_headers} || {};
	foreach my $key (keys %$headers) {
	    $self->{logger}->debug("DEBUG:  $api_method request header:  $key => $headers->{$key}");
	}
    }

    my %headers = ();
    $headers{'GWOS-App-Name'} = $self->{requestor} if $self->{token};

    my $errormsg = undef;
    if ( not is_valid_ca_path( \$errormsg, $self->{rest_url_base} ) ) {
	$self->{logger}->logdie("ERROR:  $errormsg\n");
    }

    foreach my $retry ( ( 1, 0 ) ) {
	$self->{logger}->debug("DEBUG:  $api_method attempting a GET from the REST API");

	%$outcome = ();
	%$results = ();

	# We need to set this on each loop iteration because it might have changed due to re-authorization.
	$headers{'GWOS-API-Token'} = $self->{token} if $self->{token};

	# FIX MAJOR:  drop this
	$self->{logger}->trace("TRACE:  $api_method current token:  '$self->{token}'") if $self->{token};

	# Try to GET a result from API
	$self->{rest_client}->GET($full_get_url, \%headers);

	&$callback(@$callbackargs) if $callback;

	$http_response_code = $self->{rest_client}->responseCode();
	$response_content   = $self->{rest_client}->responseContent();

	# With the case of a GET, the only response code that indicates something was found is a 200,
	# otherwise process the code and produce info on that
	if ( $http_response_code == 200 ) {
	    my $status = 1;
	    $self->{logger}->trace("$api_method GET JSON response:\n$response_content");
	    $ref_decoded_response = $self->{JSON_package}->new->utf8(1)->decode($response_content);    # decode the JSON object back into a Perl structure
	    $self->{logger}->trace( "$api_method GET JSON response decoded back into Perl structure (Dumper() output):\n" . Dumper($ref_decoded_response) );
	    ## FIX MAJOR
	    ( my $object_type = $api_method ) =~ s/ies$/y/;            # categories ==> category
	    $object_type =~ s/s$//;                                    # plural to singular for all other types
	    my $object_id = $api_get_ids{$api_method};

#$self->{logger}->debug("$api_method GET JSON response:\n$response_content,  api method = $api_method,  get_objs{method} = $api_get_objects{$api_method} ");

	    if ( $ref_decoded_response->{ $api_get_objects{$api_method} } ) {
		## FIX MAJOR
		if ( $api_method eq 'version' ) {
		    if ( ref( $ref_decoded_response->{ $api_get_objects{$api_method} } ) eq 'ARRAY' ) {
			$self->{logger}->debug( "DEBUG:  have "
			      . ( scalar @{ $ref_decoded_response->{ $api_get_objects{$api_method} } } )
			      . " $api_get_objects{$api_method} objects returned" );
			foreach my $object ( @{ $ref_decoded_response->{ $api_get_objects{$api_method} } } ) {
			    ## FIX MINOR:  The substitution of "unknown $object_type" here for a misssing object key
			    ## compensates for a defect of the GWMEE 7.0.X Foundation REST API, which should itself
			    ## return such a key.
			    ##
			    ## Note that the use of a fixed "unknown $object_type" key means that the caller will
			    ## only receive a report on at most one such object, but which one is indeterminate.
			    ## Information on additional objects in the same situation is overwritten and thus
			    ## discarded here before it can reach the caller.
			    $results->{ $object->{$object_id} || "unknown $object_type" } = $object;
			}
		    }
		    else {
			$self->{logger}->debug( "DEBUG:  have 1 $api_get_objects{$api_method} object returned" );
			$results->{ $ref_decoded_response->{$object_id} || "unknown $object_type" } = $ref_decoded_response;
		    }
		}
		else {
		    $self->{logger}->debug( "DEBUG:  have "
			  . scalar @{ $ref_decoded_response->{ $api_get_objects{$api_method} } }
			  . " $api_get_objects{$api_method} objects returned" );

		    if ( $api_method eq 'services' and $format eq 'host,service' ) {
			foreach my $object ( @{ $ref_decoded_response->{ $api_get_objects{$api_method} } } ) {
			    $results->{ $object->{hostName} }{ $object->{description} } = $object;
			}
		    }
		    elsif ( $api_method eq 'services' and $format eq 'service,host' ) {
			foreach my $object ( @{ $ref_decoded_response->{ $api_get_objects{$api_method} } } ) {
			    $results->{ $object->{description} }{ $object->{hostName} } = $object;
			}
		    }
		    elsif ( $api_method eq 'hostblacklists' or $api_method eq 'hostidentities'  or $api_method eq 'customgroups' ) {
			# Sticking with  GH's assumption below...
			$results->{ "unknown $api_get_objects{$api_method}" }= $ref_decoded_response;
		    }
		    else {
			foreach my $object ( @{ $ref_decoded_response->{ $api_get_objects{$api_method} } } ) {
			    ## FIX MINOR:  The substitution of "unknown $object_type" here for a misssing object key
			    ## compensates for a defect of the GWMEE 7.0.X Foundation REST API, which should itself
			    ## return such a key.  DN: WHY ?? 9/30/15
			    ##
			    ## Note that the use of a fixed "unknown $object_type" key means that the caller will
			    ## only receive a report on at most one such object, but which one is indeterminate.
			    ## Information on additional objects in the same situation is overwritten and thus
			    ## discarded here before it can reach the caller.
			    $results->{ $object->{$object_id} || "unknown $object_type" } = $object;

			    # FIX MAJOR:  Allow the use of $format to re-organize the "services" component of a
			    # servicegroup to impose either a {host}{service} structure or a {service}{host}
			    # structure, for easy lookup by the calling program.
			}
		    }
		}
	    }
	    else {
		## We have only a single element in hand.
		$self->{logger}->debug("DEBUG:  We have at most only a single $api_get_objects{$api_method} object returned.");
		if ( $api_method eq 'services' and $format eq 'host,service' ) {
		    $results->{ $ref_decoded_response->{hostName} }{ $ref_decoded_response->{description} } = $ref_decoded_response;
		}
		elsif ( $api_method eq 'services' and $format eq 'service,host' ) {
		    $results->{ $ref_decoded_response->{description} }{ $ref_decoded_response->{hostName} } = $ref_decoded_response;
		}
		elsif ( $api_method eq 'license/check' ) {
		    ## We leave %$results empty in this case.
		    %$outcome = %$ref_decoded_response;
		    $status = $outcome->{success};
		}
		elsif ( $api_method eq 'hostblacklists' or $api_method eq 'hostidentities' ) {
		    # Sticking with  GH's assumption below...
	    	    $results->{ "unknown $api_get_objects{$api_method}" }{$api_get_objects{$api_method}}[0] = $ref_decoded_response;
		}
		else {
		    #$self->{logger}->error( Dumper $ref_decoded_response, $object_id);
		    $results->{ $ref_decoded_response->{$object_id} || "unknown $object_type" } = $ref_decoded_response ;

		    # FIX MAJOR:  Allow the use of $format to re-organize the "services" component of a
		    # servicegroup to impose either a {host}{service} structure or a {service}{host}
		    # structure, for easy lookup by the calling program.
		}
	    }
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting "
		      . ( caller(2) )[3]
		      . " (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    return $status;
	}
	elsif ( $http_response_code != 401 or not $retry ) {
	    ## Either not an authorization failure, or we won't try again to re-authorize.
	    ##
	    ## The REST API doesn't always return JSON formatted things for GETs.  It can
	    ## return a plain string, HTML or JSON (and possibly other stuff not yet seen).
	    $outcome->{response_error}  = $response_content;
	    $outcome->{response_code}   = $http_response_code;
	    $outcome->{response_status} = status_message($http_response_code);

	    # If the GET worked but the thing simply doesn't exist, then we'd expect a certain simple string in the response content
	    # and a 404 status. If that expected string is not there, then something else went wrong so we need to report on that.
	    # response_content_matched() checks for expected 'not found' strings
	    #
   	    # Update 4/16/15 : as of GWME 710, and as a fix to GWMON-11397, objects not found always return a 404, and the
   	    # error is properly packaged up with an error and a status property, rather than just being a flat error string.
   	    # The fix for now is to just detect 404 because error and status will now be set properly.
   	    # If that approach does fail, the logic here will drop through to being a more generalized error but still handled.

	    if ( $self->response_content_matched( $api_method, $response_content ) or $http_response_code == 404 ) {
		$self->{logger}->info("INFO:  $api_method object / query \"$thing_to_get\" returned 'not found'");    # simply wasn't found
	    }
	    else {
		$self->{logger}->error( "ERROR:  $api_method object / query \"$thing_to_get\" response indicated an error occurred.  Error analysis:  "
		      . $self->analyze_error( $http_response_code, \$response_content ) );
	    }

	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting "
		      . ( caller(2) )[3]
		      . " (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    return 0;    # failed to get thing, for whatever reason
	}
	else {
	    ## Got an authorization failure.  Try to re-authorize before retrying.
	    $self->{logger}->error("ERROR:  REST API authorization failed; will retry.");
	    if ( not $self->_API_REAUTH( $outcome ) ) {
		if ( $self->{logger}->is_info() ) {
		    $self->{logger}->info( "INFO:  exiting "
			  . ( caller(2) )[3]
			  . " (call took "
			  . sprintf( "%.3f", Time::HiRes::time() - $start_time )
			  . " seconds)" );
		}
		return 0;    # No luck.
	    }
	}
    }

    if ( $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting "
	      . ( caller(2) )[3]
	      . " (call took "
	      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
	      . " seconds)" );
    }
    return 0;    # should not get here - just in case return failure
}

# ---------------------------------------------------------------------------- #
# Usage   : $api->_API_POST( $api_method, \@objects, \%options, \%outcome, \@results )
# Purpose : UPdates or inSERTs objects like hosts, hostgroups, etc
# Returns : 1 on success, plus results hash populated with response
#         : 0 on failure, warning, etc., with outcome hash populated with details
# Params  : $api_method - a REST API method, such as 'host', 'hostgroup', etc
#         : $objects - a ref to an array containing objects to be created or updated
#         : $options - a ref to a hash containing options
#         : $outcome - a ref to a hash where success/failure details will be returned
#         : $results - a ref to a hash where details of of created/updated objects will be returned
#            Note that the results hash has normal Perl data structures, not JSON.
# Throws  : see logdie()
# Comments: This module will perform JSON encoding so that the consumer
#            only has to focus on Perl data structures and not also JSON.
#            Validation of options will be done by the REST API.
# See also: upsert_hosts() for example
# ---------------------------------------------------------------------------- #

sub _API_POST {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;
    local $_;

    my $start_time;
    if ( $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering " . ( caller(2) )[3] );
    }

    local $SIG{INT}  = 'DEFAULT' if $self->{interruptible};
    local $SIG{QUIT} = 'DEFAULT' if $self->{interruptible};
    local $SIG{TERM} = 'DEFAULT' if $self->{interruptible};
    if ( $self->{interruptible} and ${ $self->{interruptible} } ) {
	## An interrupt must have come in before we could turn off the signal handlers.  If we
	## proceeded now in spite of that, we would end up calling the REST API in a possibly
	## long-running call, and it would be as though we had not even bothered to disable the
	## signal handlers.  So instead, we must abort now, before we get that far.
	$self->{logger}->logdie( "FATAL:  a termination signal was recognized during a call to " . ( caller(2) )[3] );
    }

    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage__API_POST") if @_ != 6;
    $self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage__API_POST")
      if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;

    # a hash of valid api POST objects -- not surprisingly, mostly the same as api GET objects
    #
    # The use of mixed case in the REST API object naming, plus the special cases for
    # event ack and unack, forces us to create a map here of actual object names to use in
    # the generated JSON, instead of just using $api_method directly.
    my %api_post_objects = (
	"biz/setindowntime"	 => "", # no array for downtimes is used in the API
	"biz/getindowntime"	 => "", # no array for downtimes is used in the API # GET is also via POST for this
	"biz/clearindowntime"	 => "", # no array for downtimes is used in the API # GET is also via POST for this
	"biz/hosts"	         => "hosts",
	"biz/services"	         => "services",
	"notifications/hosts"    => "notifications",
	"notifications/services" => "notifications",
	"applicationtypes"       => "applicationTypes",
	"hostblacklists"         => "hostBlacklists",
	"hostidentities"         => "hostIdentities",
	"hosts"                  => "hosts",
	"hostgroups"             => "hostGroups",
	"services"               => "services",
	"servicegroups"          => "serviceGroups",
	"events"                 => "events",
	"events/ack"             => "acks",
	"events/unack"           => "unacks",
	"devices"                => "devices",
	"propertytypes"          => "propertyTypes",
	"categories"             => "categories",
	"perfdata"               => "perfDataList",
	"auditlogs"              => "auditLogs",
	"customgroups"           => "customGroups"
    );

    # biz/*indowntime are special - no array, but still needs to be a ref to a hash and not just a scalar
    if ( $api_method ne 'biz/setindowntime' and $api_method ne 'biz/getindowntime' and $api_method ne 'biz/clearindowntime' ) {
    	$self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    }
    else {
    	$self->{logger}->logcroak("ERROR:  Expecting HASH reference") if ref $objects ne 'HASH';
    }
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $results ne 'ARRAY';

    # Validate arguments.
    $self->{logger}->logcroak("ERROR:  unrecognized API command root '$api_method'")
      if not defined $api_post_objects{$api_method};

    my (
	$response_content,             # the POST response content/body
	$http_response_code,           # the POST HTTP response code
	$ref_decoded_response,         # a HASH reference of the decoded JSON version of the response content
	$error_status,                 # stores some info about an error if the response was not 200, or 404
	$json_encoded_instructions,    # JSON encoded instructions hash
	$full_post_url,                # the full post url, starting with /$api_method
    ) = undef;

    my $callback = delete $options->{callback};
    my $callbackargs = delete( $options->{callbackargs} ) || [];

    # Construct POST url
    $full_post_url = "/$api_method";
    $full_post_url .= '?' . join( '&', map { uri_escape($_) . '=' . uri_escape( $options->{$_} ) } keys %$options ) if %$options;

    $self->{logger}->debug("DEBUG:  Full POST URL:  '$full_post_url'");

    # Encode the instructions hash into JSON.
    # Encoding the readable instructions unconditionally is an expensive operation,
    # which should only be undertaken if we know that the data will be logged.
    my $is_stats = $self->{logger}->can('is_stats') && $self->{logger}->is_stats();
    if ( $is_stats || $self->{logger}->is_trace() ) {
	my $level = $is_stats ? 'stats' : 'trace';
	my $json_readable_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode(
	    $api_post_objects{$api_method} ? { $api_post_objects{$api_method} => $objects } : $objects
	);
	$self->{logger}->$level("...$full_post_url POST JSON:\n$json_readable_instructions");
    }

    $json_encoded_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode(
	$api_post_objects{$api_method} ? { $api_post_objects{$api_method} => $objects } : $objects
    );

    # FIX MAJOR:  drop this
    if (0) {
	my $headers = $self->{rest_client}{_headers} || {};
	foreach my $key (keys %$headers) {
	    $self->{logger}->debug("DEBUG:  Request header:  $key => $headers->{$key}");
	}
    }

    my %headers = ();
    $headers{'GWOS-App-Name'} = $self->{requestor} if $self->{token};

    my $errormsg = undef;
    if ( not is_valid_ca_path( \$errormsg, $self->{rest_url_base} ) ) {
	$self->{logger}->logdie("ERROR:  $errormsg\n");
    }

    foreach my $retry ( ( 1, 0 ) ) {
	$self->{logger}->debug("DEBUG:  attempting a POST to the REST API");

	%$outcome = ();
	@$results = ();

	# We need to set this on each loop iteration because it might have changed due to re-authorization.
	$headers{'GWOS-API-Token'} = $self->{token} if $self->{token};

	# FIX MAJOR:  drop this
	$self->{logger}->trace("TRACE:  current token:  '$self->{token}'") if $self->{token};

	# Try to POST a result to the API
	$self->{rest_client}->POST( $full_post_url, $json_encoded_instructions, \%headers );

	&$callback(@$callbackargs) if $callback;

	$http_response_code = $self->{rest_client}->responseCode();
	$response_content   = $self->{rest_client}->responseContent();

	# With the case of a POST, HTTP status 200 on success of doing the POST
	# does NOT necessarily mean successful object upsert(s).
	if ( $http_response_code == 200 ) {
	    $self->{logger}->trace("POST JSON response:\n$response_content");
	    $ref_decoded_response = $self->{JSON_package}->new->utf8(1)->decode($response_content);    # decode JSON back into Perl data structure
	    $self->{logger}->trace( "POST JSON response decoded back into Perl structure (Dumper() output):\n" . Dumper($ref_decoded_response) );
	    %$outcome = %{$ref_decoded_response};                      # copy the decoded response into the hash i.e., not a copy of the ref
	    if ( $api_method eq 'biz/setindowntime' or $api_method eq 'biz/getindowntime' or $api_method eq 'biz/clearindowntime' ) {
	    	@$results = delete $outcome->{results} ;
	    }
	    else {
	    	@$results = @{ delete $outcome->{results} };
            }

	    # The POST might be successful, but the create/update might have fully or partially failed.
	    # That is, we can't just return success here without first analyzing the response.
	    if ( $self->{logger}->is_info() ) { $self->{logger}->info( "INFO:  exiting " . ( caller(2) )[3] . " (call took " . sprintf( "%.3f", Time::HiRes::time() - $start_time ) . " seconds)" ); }
	    return $self->analyze_upsert_response($ref_decoded_response);    # this routine will log the details
	}
	elsif ( $http_response_code != 401 or not $retry ) {
	    ## Either not an authorization failure, or we won't try again to re-authorize.
	    ##
	    ## The REST API possibly might not return JSON-formatted things for POST errors.
	    ## So we manufacture a fixed structure to return to the caller.
	    $outcome->{response_error}  = $response_content;
	    $outcome->{response_code}   = $http_response_code;
	    $outcome->{response_status} = status_message($http_response_code);

	    $self->{logger}->error( "ERROR:  POST error for REST API $api_method:  " . $self->analyze_error( $http_response_code, \$response_content ) );
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting "
		      . ( caller(2) )[3]
		      . " (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    return 0;    # failed to upsert
	}
	else {
	    ## Got an authorization failure.  Try to re-authorize before retrying.
	    $self->{logger}->error("ERROR:  REST API authorization failed; will retry.");
	    if ( not $self->_API_REAUTH($outcome) ) {
		if ( $self->{logger}->is_info() ) {
		    $self->{logger}->info( "INFO:  exiting "
			  . ( caller(2) )[3]
			  . " (call took "
			  . sprintf( "%.3f", Time::HiRes::time() - $start_time )
			  . " seconds)" );
		}
		return 0;    # No luck.
	    }
	}
    }

    if ( $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting "
	      . ( caller(2) )[3]
	      . " (call took "
	      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
	      . " seconds)" );
    }
    return 0;    # should not get here; just in case, return failure
}

# FIX MAJOR:  this doc is just plain wrong
# ---------------------------------------------------------------------------- #
# Usage   : $api->_API_DELETE( $api_method, \@objects, \%options, \%outcome, \@results )
# Purpose : deletes objects like hosts, hostgroups, etc
# Returns : 1 on success, plus results hash populated with response
#         : 0 on failure, warning etc
# Params  : $api_method - a REST API method, such as 'host', 'hostgroup', etc
#         : $ref_objects_list - a ref to an array containing a list of objects (e.g., names, ids, etc)
#         : \%results - a ref to a hash that might contain response
#            Note that the results hash has normal perl data structures, not json
# Throws  : see logdie()s
# Comments: This module will do the JSON encoding so that the consumer
#            only has to focus on Perl data structures and not also JSON.
#            Validation of instructions data structure will be done by the REST API.
# See also: delete_hosts() for example
# ---------------------------------------------------------------------------- #

sub _API_DELETE {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;
    local $_;

    my $start_time;
    if ( $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering " . ( caller(2) )[3] );
    }

    local $SIG{INT}  = 'DEFAULT' if $self->{interruptible};
    local $SIG{QUIT} = 'DEFAULT' if $self->{interruptible};
    local $SIG{TERM} = 'DEFAULT' if $self->{interruptible};
    if ( $self->{interruptible} and ${ $self->{interruptible} } ) {
	## An interrupt must have come in before we could turn off the signal handlers.  If we
	## proceeded now in spite of that, we would end up calling the REST API in a possibly
	## long-running call, and it would be as though we had not even bothered to disable the
	## signal handlers.  So instead, we must abort now, before we get that far.
	$self->{logger}->logdie( "FATAL:  a termination signal was recognized during a call to " . ( caller(2) )[3] );
    }

    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage__API_DELETE") if @_ != 6;
    $self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage__API_DELETE")
      if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;

    my (
	$response_content,            # the POST response content/body
	$http_response_code,          # the POST HTTP response code
	$full_delete_url,             # the final REST DELETE url
	$ref_decoded_response,        # a HASH reference of the decoded JSON version of the response content
	$error_status,                # stores some info about an error if the response was not 200, or 404
	$json_encoded_instructions    # JSON encoded instructions hash
    ) = undef;

    # a hash of valid api DELETE objects, to be used if we delete via POST data
    my %api_delete_objects = (
	"applicationtypes" => "applicationTypes",
	"hostidentities"   => "hostIdentities",
	"hostblacklists"   => "hostBlacklists",
	"hosts"            => "hosts",
	"hostgroups"       => "hostGroups",
	"services"         => "services",
	"servicegroups"    => "serviceGroups",
	"events"           => "events",
	"devices"          => "devices",
	"categories"       => "categories",
	"customgroups"     => "customGroups"
    );

    # The unique ID handles that must be used when deleting objects if we do so via POST
    # data, unless special-cased later on.  Items such as services, which have two parts
    # (host and service names) are obviously special-cased.
    my %api_delete_ids = (
	"applicationtypes" => "name",
	"hostidentities"   => "hostIdentityId",
	"hostblacklists"   => "hostBlacklistId",
	"hosts"            => "hostName",
	"hostgroups"       => "name",
	"services"         => "id",
	"servicegroups"    => "name",
	"events"           => "id",
	"devices"          => "identification",
	"categories"       => "name",
	"customgroups"     => "name"
    );

    # a hash of valid api DELETE methods
    my %api_delete_methods = (
	"applicationtypes" => 1,
	"hostidentities"   => 1,
	"hostblacklists"   => 1,
	"hosts"            => 1,
	"hostgroups"       => 1,
	"services"         => 1,
	"servicegroups"    => 1,
	"events"           => 1,
	"devices"          => 1,
	"categories"       => 1,
	"customgroups"     => 1
    );

    # Validate arguments.
    $self->{logger}->logcroak("ERROR:  unrecognized API command root '$api_method'") if not defined $api_delete_methods{$api_method};
    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $results ne 'ARRAY';

    my $callback = delete $options->{callback};
    my $callbackargs = delete( $options->{callbackargs} ) || [];

    # FIX MAJOR:  do a more thorough job of validating possible options supplied by the end-user caller
    if ( $api_method eq 'services' ) {
	my @services = ();
	## validate hostname if given
	my $hostname = delete $options->{hostName};
	if ( defined $hostname ) {
	    ## FIX MAJOR:  ensure that @$objects != 0
	    if ( ref $hostname eq 'ARRAY' ) {
		my $i = 0;
		foreach my $name (@$hostname) {

                    # GWMON-12728 / V 0.8.8
                    #
                    # The validation is being performed is wrong. It should allow whitespaces.
		    # For example, FDOS-27, a host being used in cacti contains white spaces 'Clifton 2nd Floor Stack'.
		    # Direct use of the REST API allows crud ops on this hostname.
                    # Given the only place RAPID is doing such validation is here, I'm going to remove this for now.
		    # When we get agreement on what a valid hostname is, the is_valid_object_name routine can be updated
                    # and validation should be done in all _API routines too.

		    # ## FIX MAJOR:  If we're going to validate hostnames at this level,
		    # ## then we ought to validate servicenames as well.  Otherwise, we
		    # ## should leave all object-name validation to the REST API itself.
		    # if ( not is_valid_object_name($name) ) {
		    #	$name = 'undefined array element' if not defined $name;
		    #	$self->{logger}->logcroak("ERROR:  invalid virtual hostname '$name'");
		    # }

		    ## FIX MAJOR:  $servicename must be taken instead from the next element of @$objects,
		    ## and we have to check that (@$objects == @$hostname) somewhere in here
		    my $servicename = $objects->[$i++];
		    push @services, { description => $servicename, hostName => $name };
		}
	    }
	    else {
		# See GWMON-12728 note above.
                #
		# ## FIX MAJOR:  If we're going to validate hostnames at this level,
		# ## then we ought to validate servicenames as well.  Otherwise, we
		# ## should leave all object-name validation to the REST API itself.
		# if ( not is_valid_object_name($hostname) ) {
		#     $self->{logger}->logcroak("ERROR:  invalid virtual hostname '$hostname'");
		# }

		foreach my $servicename (@$objects) {
		    push @services, { description => $servicename, hostName => $hostname };
		}
	    }
	}
	else {
	    ## FIX MAJOR
	}
	$full_delete_url = "/$api_method";

	# Encode the instructions hash into JSON.

	# Encoding the readable instructions unconditionally is an expensive operation,
	# which should only be undertaken if we know that the data will be logged.
	my $is_stats = $self->{logger}->can('is_stats') && $self->{logger}->is_stats();
	if ( $is_stats || $self->{logger}->is_trace() ) {
	    my $level = $is_stats ? 'stats' : 'trace';
	    my $json_readable_instructions =
	      $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode( { 'services' => \@services } );

	    # If you want to display what came into this routine as opposed to what we did with it, use this instead:
	    # my $json_readable_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode( { 'services' => $objects } );

	    # FIX MINOR:  $full_delete_url here doesn't contain "?clear=true" if that is appended below.
	    $self->{logger}->$level("...$full_delete_url DELETE JSON:\n$json_readable_instructions");
	}

	$json_encoded_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode( { 'services' => \@services } );
    }
    ## FIX MINOR:  This next paragraph should work for both hosts and hostgroups as well,
    ## but we're getting an error message back from the REST API for both of those object types:
    ##   The request sent by the client was syntactically incorrect
    ##   (java.io.EOFException: No content to map to Object due to end of input).
    ## FIX MINOR:  Once we've cleared that up, test this construction for other object types as well.
    elsif ( $api_method eq 'servicegroups' ||
	    $api_method eq 'hosts' ||
	    $api_method eq 'hostgroups' ||
	    $api_method eq 'hostblacklists' ||
	    $api_method eq 'hostidentities' ||
	    $api_method eq 'categories' ||
	    $api_method eq 'customgroups'
           ) {
	## FIX MINOR:  ensure that @$objects != 0, if that would cause a problem (test that case)
	my @object_ids;
	if ( $api_method eq 'hostblacklists' or $api_method eq 'categories' ) {  # || $api_method eq 'hostidentities' ) { ...}
		@object_ids = @$objects; # don't do any insertion of the api method id into each element (see 'else' for explanation)
	}
	else {
		# This converts
		# [
		#   {
		#       'k1' => 2,
		#       'k2' => 3
		#   },
		#   {
		#       'k3' => 5,
		#       'k4' => 6
		#   }
		# ];
		#
		# Into (for example, id is $api_delete_ids('services')) ...
		# [
  		#   {
    		#     	'id' => {
      		# 		'k1' => 2,
      		# 		'k2' => 3
    		#       }
  		#   },
  		#   {
    		# 	'id' => {
      		# 		'k3' => 5,
      		# 		'k4' => 6
    		# 	}
  		#   }
		# ];

		@object_ids = map { { $api_delete_ids{$api_method} => $_ } } @$objects;
	}
	$full_delete_url = "/$api_method";

	# Encode the instructions hash into JSON.

	# Encoding the readable instructions unconditionally is an expensive operation, which should only be undertaken if we know that the data will be logged.
	my $is_stats = $self->{logger}->can('is_stats') && $self->{logger}->is_stats();
	if ( $is_stats || $self->{logger}->is_trace() ) {
	    my $level = $is_stats ? 'stats' : 'trace';
	    my $json_readable_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode(
		{ $api_delete_objects{$api_method} => \@object_ids }
	    );

	    # If you want to display what came into this routine as opposed to what we did with it, use this instead:
	    # my $json_readable_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->pretty(1)->encode( { $api_delete_objects{$api_method} => $objects } );

	    # FIX MINOR:  $full_delete_url here doesn't contain "?clear=true" if that is appended below.
	    $self->{logger}->$level("...$full_delete_url DELETE JSON:\n$json_readable_instructions");
	}

	$json_encoded_instructions =
	  $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode( { $api_delete_objects{$api_method} => \@object_ids } );
    }
    else {
	# FIX MAJOR:  There is some limit to the allowed length of the URL.  To avoid any hint of
	# problems with that limit, we should always be using the POST form of delete, not the URL form.
	# But we need to verify that all forms of object deletion (not just for services) will accept
	# a POST form, and we need to figure out how to structure the POST body in each case.
	#
	# FIX MAJOR:  The REST API supports a POST payload for deletion, at least for devices,
	# instead of parameters on a URL.  Verify whether that is supported for all other object
	# deletions, then upgrade the code here accordingly to relieve any issues of URL length.
	#
	# FIX MAJOR:  Log the JSON-encoded POST payload as we do in the previous branch.
	#
	# FIX MAJOR:  Encode the object list into JSON, instead of appending the object names to the URL.
	# FIX MAJOR:  When using the URL form, make sure that that @$objects == 0 is not dangerous.

	$full_delete_url = "/$api_method";
	$full_delete_url .= '/' . join( ',', map { uri_escape($_) } @$objects ) if @$objects;

	my $is_stats = $self->{logger}->can('is_stats') && $self->{logger}->is_stats();
	if ( $is_stats || $self->{logger}->is_trace() ) {
	    my $level = $is_stats ? 'stats' : 'trace';
	    $self->{logger}->$level("$api_method DELETE URL:\n    ...$full_delete_url");
	}
    }

    # If caller's caller is clear_hostgroups(), tag on the special clear=true parameter.
    # (clear_hostgroups() calls _API_DELETE() from within an eval{}, hence the additional
    # layer of calling we have to poke through.)  This approach might be nicer than using
    # a special option value, overloading the hostname parameter, etc.
    if ( ( caller(2) )[3] eq "GW::RAPID::clear_hostgroups" or ( caller(2) )[3] eq "GW::RAPID::clear_hostidentities" ) {
	$full_delete_url .= "?clear=true";
    }

    # If caller's caller is clear_servicegroups(), take special action to drop all
    # host-service members but not the servicegroup itself.
    # (clear_servicegroups() calls _API_DELETE() from within an eval{}, hence the additional
    # layer of calling we have to poke through.)
    elsif ( ( caller(2) )[3] eq "GW::RAPID::clear_servicegroups" ) {
	## FIX MAJOR:  Take evasive action here, to drop all host-service members of each
	## servicegroup without actually deleting the servicegroup itself.
	die "GW::RAPID::clear_servicegroups() is not yet supported.\n";
    }

    # FIX MINOR:  This ought not to be output if we already logged the URL above.
    $self->{logger}->debug("DEBUG:  Full DELETE URL:  $full_delete_url");

    # FIX MAJOR:  drop this
    if (0) {
	my $headers = $self->{rest_client}{_headers} || {};
	foreach my $key (keys %$headers) {
	    $self->{logger}->debug("DEBUG:  Request header:  $key => $headers->{$key}");
	}
    }

    my %headers = ();
    $headers{'GWOS-App-Name'} = $self->{requestor} if $self->{token};

    my $errormsg = undef;
    if ( not is_valid_ca_path( \$errormsg, $self->{rest_url_base} ) ) {
	$self->{logger}->logdie("ERROR:  $errormsg\n");
    }

    foreach my $retry ( ( 1, 0 ) ) {
	$self->{logger}->debug("DEBUG:  attempting a DELETE to the REST API");

	%$outcome = ();
	@$results = ();

	# We need to set this on each loop iteration because it might have changed due to re-authorization.
	$headers{'GWOS-API-Token'} = $self->{token} if $self->{token};

	# FIX MAJOR:  drop this
	$self->{logger}->trace("TRACE:  current token:  '$self->{token}'") if $self->{token};

	# Send a DELETE request with the delete URL to the API.
	$self->{rest_client}->request( 'DELETE', $full_delete_url, $json_encoded_instructions, \%headers );

	# FIX MAJOR:  deal with possible retries
	&$callback(@$callbackargs) if $callback;

	$http_response_code = $self->{rest_client}->responseCode();
	$response_content   = $self->{rest_client}->responseContent();

	# With the case of a DELETE, HTTP status 200 on success of doing the DELETE
	# does NOT necessarily mean successful object deletions.
	if ( $http_response_code == 200 ) {
	    $self->{logger}->trace("DELETE JSON response:\n$response_content");
	    $ref_decoded_response = $self->{JSON_package}->new->utf8(1)->decode($response_content);    # decode JSON back into Perl data structure
	    $self->{logger}->trace( "DELETE JSON response decoded back into Perl structure:\n" . Dumper($ref_decoded_response) );
	    %{$outcome} = %{$ref_decoded_response};                    # copy the decoded response into the hash i.e., not a copy of the ref
	    @$results = @{ delete $outcome->{results} };

	    # The DELETE might be successful, but the DELETE might have fully or partially failed.
	    # I.e., can't just return success here without first analyzing the response.
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting "
		      . ( caller(2) )[3]
		      . " (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    return $self->analyze_delete_response($ref_decoded_response);    # this routine will log the details
	}
	elsif ( $http_response_code != 401 or not $retry ) {
	    ## Either not an authorization failure, or we won't try again to re-authorize.
	    ##
	    ## The REST API possibly might not return JSON formatted things for POSTs.
	    ## So we manufacture a fixed structure to return to the caller.
	    $outcome->{response_error}  = $response_content;
	    $outcome->{response_code}   = $http_response_code;
	    $outcome->{response_status} = status_message($http_response_code);

	    $self->{logger}->error( "ERROR:  DELETE error:  " . $self->analyze_error( $http_response_code, \$response_content ) );
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting "
		      . ( caller(2) )[3]
		      . " (call took "
		      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
		      . " seconds)" );
	    }
	    return 0;    # failed to delete
	}
	else {
	    ## Got an authorization failure.  Try to re-authorize before retrying.
	    $self->{logger}->error("ERROR:  REST API authorization failed; will retry.");
	    if ( not $self->_API_REAUTH($outcome) ) {
		if ( $self->{logger}->is_info() ) {
		    $self->{logger}->info( "INFO:  exiting "
			  . ( caller(2) )[3]
			  . " (call took "
			  . sprintf( "%.3f", Time::HiRes::time() - $start_time )
			  . " seconds)" );
		}
		return 0;    # No luck.
	    }
	}
    }

    if ( $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting "
	      . ( caller(2) )[3]
	      . " (call took "
	      . sprintf( "%.3f", Time::HiRes::time() - $start_time )
	      . " seconds)" );
    }
    return 0;    # should not get here; just in case, return failure
}

# FIX MAJOR:  this doc is just plain wrong
# ---------------------------------------------------------------------------- #
# Usage   : $api->_API_PUT( $api_method, \@objects, \%options, \%outcome, \@results )
# Purpose : updates event objects, and possibly other things in the future
# Returns : 1 on success, plus results hash populated with response
#         : 0 on failure, warning etc
# Params  : $api_method - a REST API method, such as 'events'
#         : $ref_objects_list - a ref to an array containing a list of objects (e.g., events, etc)
#         : \%results - a ref to a hash that might contain response
#            Note that the results hash has normal perl data structures, not json
# Throws  : see logdie()s
# Comments: This module will do the JSON encoding so that the consumer
#            only has to focus on Perl data structures and not also JSON.
#            Validation of instructions data structure will be done by the REST API.
#            NOTE At the time of writing, only events api method uses PUT for updating events.
# See also: update_events() for example
# ---------------------------------------------------------------------------- #
sub _API_PUT {
    my ( $self, $api_method, $objects, $options, $outcome, $results ) = @_;
    local $_;

    my $start_time;
    if ( $self->{logger}->is_info() ) {
	$start_time = Time::HiRes::time();
	$self->{logger}->info( "INFO:  entering " . ( caller(2) )[3] );
    }

    local $SIG{INT}  = 'DEFAULT' if $self->{interruptible};
    local $SIG{QUIT} = 'DEFAULT' if $self->{interruptible};
    local $SIG{TERM} = 'DEFAULT' if $self->{interruptible};
    if ( $self->{interruptible} and ${ $self->{interruptible} } ) {
	## An interrupt must have come in before we could turn off the signal handlers.  If we
	## proceeded now in spite of that, we would end up calling the REST API in a possibly
	## long-running call, and it would be as though we had not even bothered to disable the
	## signal handlers.  So instead, we must abort now, before we get that far.
	$self->{logger}->logdie( "FATAL:  a termination signal was recognized during a call to " . ( caller(2) )[3] );
    }

    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage__API_PUT") if @_ != 6;
    $self->{logger}->logcroak("ERROR:  Undefined arg(s). $usage__API_PUT") if any { !defined $_ } $self, $api_method, $objects, $options, $outcome, $results;

    my (
	$response_content,        # the PUT response content/body
	$http_response_code,      # the PUT HTTP response code
	$ref_decoded_response,    # a HASH reference of the decoded JSON version of the response content
	$error_status,            # stores some info about an error if the response was not 200, or 404
	$full_put_url,            # the final REST PUT url
	$json_encoded_instructions,    # JSON encoded instructions hash
    ) = undef;

    my %query_data = ();          # hash for param list

    # a hash of valid api PUT methods
    my %api_put_objects = ( "events" => 1,
			    "customgroups/addmembers" => 1 ,
			    "customgroups/deletemembers" => 1
			  );

    # Validate arguments.
    $self->{logger}->logcroak("ERROR:  unrecognized API command root '$api_method'") if not defined $api_put_objects{$api_method};

    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $objects ne 'ARRAY';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $options ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting HASH reference")  if ref $outcome ne 'HASH';
    $self->{logger}->logcroak("ERROR:  Expecting ARRAY reference") if ref $results ne 'ARRAY';

    # If the method is for customgroups/[add|delete]members, expecting an array with just one element
    if ( $api_method =~ m{^customgroups/(add|delete)members$}  ) {
    	$self->{logger}->logcroak("ERROR:  Expecting array with just one element") if scalar @{$objects} != 1;
    }

    # Example: PUT /api/events/50,51,52?opStatus=NOTIFIED&updatedBy=admin&comments=testing+123
    # Construct the PUT url; we don't use $self->{rest_client}->buildQuery($options) because it
    # translates spaces to +'s instead of %20's.  We uri_escape the option names as a basic
    # security measure, not because we normally expect to need such encoding.
    $full_put_url = "/$api_method";
    if ( $api_method eq 'events' ) {
    	$full_put_url .= '/' . join( ',', map { uri_escape($_) } @$objects ) if @$objects;
    	$full_put_url .= '?' . join( '&', map { uri_escape($_) . '=' . uri_escape( $options->{$_} ) } keys %$options ) if %$options;
	$json_encoded_instructions = undef;
    }
    else { # this is currently just for add_customgroups_members and delete_customgroups_members
	$json_encoded_instructions = $self->{JSON_package}->new->utf8(1)->convert_blessed(1)->encode( $objects->[0] );
    }

    $self->{logger}->debug("DEBUG:  Full PUT URL:  '$full_put_url'");
    $self->{logger}->debug("DEBUG:  json encoded instructions :  '$json_encoded_instructions'");

    my %headers = ();
    $headers{'GWOS-App-Name'} = $self->{requestor} if $self->{token};

    my $errormsg = undef;
    if ( not is_valid_ca_path( \$errormsg, $self->{rest_url_base} ) ) {
	$self->{logger}->logdie("ERROR:  $errormsg\n");
    }

    foreach my $retry ( ( 1, 0 ) ) {
	$self->{logger}->debug("DEBUG:  attempting a PUT to the REST API");

	%$outcome = ();
	%$results = ();

	# We need to set this on each loop iteration because it might have changed due to re-authorization.
	$headers{'GWOS-API-Token'} = $self->{token} if $self->{token};

	# FIX MAJOR:  drop this
	$self->{logger}->trace("TRACE:  current token:  '$self->{token}'") if $self->{token};

	# Send a PUT request with the put url to the API
	# $self->{rest_client}->PUT( $full_put_url, undef, \%headers );
	$self->{rest_client}->PUT( $full_put_url, $json_encoded_instructions, \%headers );

	$http_response_code = $self->{rest_client}->responseCode();
	$response_content   = $self->{rest_client}->responseContent();

	# With the case of a PUT, the only response code that indicates the host was found is a 200,
	# otherwise process the code and produce info on that
	if ( $http_response_code == 200 ) {
	    $self->{logger}->trace("PUT JSON response:\n$response_content");
	    $ref_decoded_response = $self->{JSON_package}->new->utf8(1)->decode($response_content);    # decode the JSON object back into a Perl structure
	    $self->{logger}->trace( "PUT JSON response decoded back into Perl structure (Dumper() output):\n" . Dumper($ref_decoded_response) );
	    %$outcome = %{$ref_decoded_response};                         # copy the decoded response into the hash i.e., not a copy of the ref
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting " . ( caller(2) )[3] . " (call took " . sprintf( "%.3f", Time::HiRes::time() - $start_time ) . " seconds)" );
	    }
	    return $self->analyze_put_response($ref_decoded_response);    # this routine will log the details
	}
	elsif ( $http_response_code != 401 or not $retry ) {
	    ## Either not an authorization failure, or we won't try again to re-authorize.
	    ##
	    ## The REST API possibly might not return JSON formatted things for PUTs.
	    ## So we manufacture a fixed structure to return to the caller.
	    $outcome->{response_error}  = $response_content;
	    $outcome->{response_code}   = $http_response_code;
	    $outcome->{response_status} = status_message($http_response_code);
	    $self->{logger}->error( "ERROR:  PUT error:  " . $self->analyze_error( $http_response_code, \$response_content ) );
	    if ( $self->{logger}->is_info() ) {
		$self->{logger}->info( "INFO:  exiting " . ( caller(2) )[3] . " (call took " . sprintf( "%.3f", Time::HiRes::time() - $start_time ) . " seconds)" );
	    }
	    return 0;    # failed to update thing, for whatever reason
	}
	else {
	    ## Got an authorization failure.  Try to re-authorize before retrying.
	    $self->{logger}->error("ERROR:  REST API authorization failed; will retry.");
	    if ( not $self->_API_REAUTH($outcome) ) {
		if ( $self->{logger}->is_info() ) {
		    $self->{logger}->info( "INFO:  exiting " . ( caller(2) )[3] . " (call took " . sprintf( "%.3f", Time::HiRes::time() - $start_time ) . " seconds)" );
		}
		return 0;    # No luck.
	    }
	}
    }

    if ( $self->{logger}->is_info() ) {
	$self->{logger}->info( "INFO:  exiting " . ( caller(2) )[3] . " (call took " . sprintf( "%.3f", Time::HiRes::time() - $start_time ) . " seconds)" );
    }
    return 0;    # should not get here - just in case return failure
}

# ---------------------------------------------------------------------------- #
# Usage   : analyze_upsert_response ( $ref_to_perl_structure_response )
# Purpose : analyzes a successful upsert POST response looking for failures
# Returns : 1 - no problems found, 0 - problems found => failure
# Params  : a reference to a perl structure containing the response content from the REST API
# Throws  : see logdie()s - internal module errors only
# Comments: an upsert is considered to have failed if either the POST response's 'warning' property
#            or 'failed' property are non zero. The entire response structure is passed back to the
#            original caller via the results hash for further action.
# See also:
# ---------------------------------------------------------------------------- #

sub analyze_upsert_response {
    my ( $self, $response_ref ) = @_;

    $self->{logger}->logdie("INTERNAL ERROR:  Invalid number of args. $analyze_upsert_response") if @_ != 2;

    # bizHostServiceInDowntimes has a totally different way of responding, with a different structure completely, just the array header.
    if ( exists $response_ref->{bizHostServiceInDowntimes} ) {
	return 1;
    }
    return 0 if ( $response_ref->{warning} != 0 );    # Fail on warnings
    return 1 if ( $response_ref->{failed} == 0 );     # If failure == 0, then we're all good to go.
    return 0;                                         # all other cases - failure for now
}

# ---------------------------------------------------------------------------- #
# Usage   : analyze_delete_response ( $ref_to_perl_structure_response )
# Purpose : analyzes a successful delete_hosts DELETE response looking for failures
# Returns : 1 - no problems found, 0 - problems found => failure
# Params  : a reference to a perl structure containing the response content from the REST API
# Throws  : see logdie()s - internal module errors only
# Comments: a delete is considered to have failed if either the DELETE response's 'warning' property
#            or 'failed' property are non zero. The entire response structure is passed back to the
#            original caller via the results hash for further action.
# See also:
# ---------------------------------------------------------------------------- #

sub analyze_delete_response {
    my ($self, $response_ref) = @_;

    $self->{logger}->logdie("INTERNAL ERROR:  Invalid number of args. $analyze_delete_response") if @_ != 2;
    return 0 if ( $response_ref->{warning} != 0 );    # Fail on warnings
    return 1 if ( $response_ref->{failed} == 0 );     # If failure == 0, then we're all good to go.
    return 0;                                         # all other cases - failure for now
}

# ---------------------------------------------------------------------------- #
# Usage   : analyze_put_response ( $ref_to_perl_structure_response )
# Purpose : analyzes a successful upsert PUT response looking for failures
# Returns : 1 - no problems found, 0 - problems found => failure
# Params  : a reference to a perl structure containing the response content from the REST API
# Throws  : see logdie()s - internal module errors only
# Comments: an update with put is considered to have failed if either the PUT response's 'warning' property
#            or 'failed' property are non zero. The entire response structure is passed back to the
#            original caller via the results hash for further action.
# See also:
# ---------------------------------------------------------------------------- #

sub analyze_put_response {
    my ($self, $response_ref) = @_;

    $self->{logger}->logdie("INTERNAL ERROR:  Invalid number of args. $analyze_put_response") if @_ != 2;
    return 0 if ( $response_ref->{warning} != 0 );    # Fail on warnings
    return 1 if ( $response_ref->{failed} == 0 );     # If failure == 0, then we're all good to go.
    return 0;                                         # all other cases - failure for now
}

# ---------------------------------------------------------------------------- #
# Usage   : analyze_error( $http_response_code, $ref_response_content )
# Purpose : Checks to see if a rest client object has been defined yet
# Returns : 1 on success, logcroak()s otherwise
# Params  : none
# Throws  : see logcroak()s
# Comments: For now this gives a readable version of the status code and the full response content.
#           In the future, it might do more.
# See also: _API*()
# ---------------------------------------------------------------------------- #

sub analyze_error {
    my ( $self, $http_response_code, $ref_response_content ) = @_;

    my $analysis =
	"Status code $http_response_code: '"
      . status_message($http_response_code)
      . "'. Full response content : '${$ref_response_content}'. ";
    return $analysis;
}

# ---------------------------------------------------------------------------- #
# Usage   : is_valid_dns_hostname( $hostname )
# Purpose : Checks to see if a given string is a valid Internet hostname.
# Returns : 1 on success, 0 otherwise
# Params  : hostname - the string to test validity of
# Throws  : none
# See also:
# ---------------------------------------------------------------------------- #

sub is_valid_dns_hostname {
    my $hostname = $_[0];

    # See http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names for the tests we run here.
    my $label = '(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?)';
    return ( defined($hostname) and $hostname ne '' and length($hostname) <= 255 and $hostname =~ /^$label(?:\.$label)*$/o );
}

# ---------------------------------------------------------------------------- #
# Usage   : is_valid_object_name( $objectname )
# Purpose : Checks to see if a given string is a valid object name, which
#           is a more generalized concept than just an Internet hostname.
#           Virtual hostnames and service names are instances of object names.
#           NOTE THAT IF YOU USE ANY SPECIAL CHARACTERS BEYOND THOSE ALLOWED IN
#           AN INTERNET HOSTNAME, YOU ARE RESPONSIBLE FOR ANY DOWNSTREAM EFFECTS.
# Returns : 1 on success, 0 otherwise
# Params  : hostname - the string to test validity of
# Throws  : none
# See also:
# ---------------------------------------------------------------------------- #

sub is_valid_object_name {
    my $objectname = $_[0];

    # The set of characters we disallow in an object name is, at a minimum, those that might
    # be used as shell metacharacters (for obvious security reasons), including whitespace and
    # all control characters, and all non-ISO-8859-1 characters (both since they're not supported by
    # our current database definitions, and lots of Perl code would need to be upgraded to properly
    # handle full Unicode).  Whitespace is specifically excluded because downstream processors are
    # likely not to understand that you meant one long space-separated string instead of multiple
    # separate words.  Underscores and dashes can be used as suitable replacement characters.

    return (  defined($objectname)
	  and $objectname ne ''
	  and length($objectname) <= 255
	  and $objectname !~ /[\s\a\b\t\f\cK\c?\e\n\r\Q~!*?<>[]{}()`^|;&%#,='"\E\$\\\pC]/
	  and $objectname =~ /^[\x00-\xFF]+$/ );
}

# ---------------------------------------------------------------------------- #
# Usage   : response_content_matched( $api_method, $response_content )
# Purpose : Takes a GET API method and the response content from a call to that method
#            and checks to see if the response is indicative of the object not being found.
#            That's different from some other error occurring when the GET method was called,
#            e.g., if a 500 came back, then the 'not found' string would not show.
# Returns : 1 on success, 0 otherwise
# Params  : $api_method - e.g., hosts, hostgroups, devices, etc. - the API method
#            $response_content - the response string from the API method call
# Throws  : see logdie()s
# Comments: More regex's almost definitely need adding in here - we'll see during real-world use-cases.
# See also: _API_GET() which is where this is used
# ---------------------------------------------------------------------------- #

sub response_content_matched {
    my ( $self, $api_method, $response_content ) = @_;
    local $_;

    $self->{logger}->logdie("ERROR:  Invalid number of args. $usage_response_content_matched") if @_ != 3;
    $self->{logger}->logdie("ERROR:  Undefined arg(s). $usage_response_content_matched") if any { !defined $_ } $api_method, $response_content;

    # A hash of API methods and their expected object-not-found responses regex's.
    # Get these by doing something like:
    #
    #     curl -u wsuser:wsuser -H "Accept: application/json" -H "Content-Type: application/json" \
    #         'http://localhost/api/hosts/somehostthatdoesntexist'
    #
    # In the case of license/check, there is no equivalent for a "not found" object, except perhaps having
    # no license installed.  But that's an error condition, not a warning condition, so we specify a dummy
    # message here to ensure the actual response content doesn't get matched by this routine.
    my %api_methods_and_not_found_regexes = (
	## method          =>  recognized failure message to reduce to simple 'not found' message
	'hosts'            => '^(Host name \[.*\] was not found|Hosts not found for query criteria.*)$',
	'hostblacklists'   => '^.*$', # TBD fix
	'hostidentities'   => '^(HostIdentities not found.*)$',
	'hostgroups'       => '^(Host Group name \[.*\] was not found|Host Groups not found for query criteria.*)$',
	'services'         => '^(Service/Host names \[.*\] not found|Service statuses not found for query criteria.*)$',
	'servicegroups'    => '^(Service Group name \[.*\] was not found|Service Groups not found for query criteria.*)$',
	'events'           => '^(Events not found for given event list \[.*\]|Events not found for given event query.*)$',
	'devices'          => '^(Device name \[.*\] was not found|Devices not found for query criteria.*)$',
	'categories'       => '^(Category name \[.*\] was not found\s*|Category not found for query criteria.*)$',   # notice that this one has a trailing space
	'applicationtypes' => '^(ApplicationType name \[.*\] was not found|ApplicationTypes not found for query criteria.*)$',
	'license/check'    => '^(DUMMY MESSAGE)$',
        'propertytypes'    => '^(PropertyType name \[.*\] was not found)$', # TBD added query criteria too
	'customgroups'     => 'customgroups object / query .* returned \'not found\'$',
	# TBD add for biz/hosts and biz/services ?
    );

    my $regex = $api_methods_and_not_found_regexes{$api_method};
    if (not defined $regex) {
	$self->{logger}->error("ERROR:  response_content_matched() needs support for '$api_method' method, to match '$response_content'");
	return $response_content;
    }
    return $response_content =~ /$regex/;
}

1;

__END__

=head1 NAME

GW::RAPID - REST API Perl Interface for Development

=head1 SYNOPSIS

See code-examples/example1.pl for example. A better code synopsis will follow in a future version.

=head1 VERSION

0.9.5

=head1 DESCRIPTION

The GW::RAPID Perl module provides a Perl interface to the GroundWork REST API.
Documentation regarding the REST API can be found here in the the GroundWork portal
under Resources -> Documentation -> GroundWork Monitor -> Developer Reference ->
RESTful API Documentation.

=head1 APPLICATION CONTEXT

The GW::RAPID package uses certain facilities that presume it lives inside a larger context.
In particular, it logs messages using the Log4perl package.
It is therefore necessary for the calling code to set up the configuration for such logging,
before calling the GW::RAPID->new() constructor.
This is most properly done at the outside application level,
since Log4perl is so constructed that its package initialization can happen only once.

The calling application gets to determine where log messages from within the GW::RAPID package
are written, if any place at all.  This is done via a call to Log::Log4perl::init() with
configuration data to specify logging levels, formats, and message routing information.
A logging handle can be constructed within the calling application and passed to the
GW::RAPID->new() constructor for use by the returned package instance.  Alternatively,
the constructor will create its own logging handle, using the category "GW.RAPID.module".
In either case, the application's configuration must include setup for the chosen category
name if log messages from the GW::RAPID instance are ever to appear anywhere.  It is possible
to configure Log::Log4perl so application-level and GW::RAPID messages are both directed to
the same logfile, but using different logging levels.

=head1 FUNCTION SIGNATURES

Except for the class constructor, most of the user-visible routines in this package attempt to
follow a standard signature, one of:

    $status = $rest_api->routine_name(\@objects, \%options, \%outcome, \@results)
    $status = $rest_api->routine_name(\@objects, \%options, \%outcome, \%results)

depending on whether the number of returned results should be known by the caller (\@results)
because it matches the number of objects to be operated upon (@objects), or will instead be
determined on the server side (as for queries, which may return from 0 to a large number of
result objects).

The nature of the @objects elements varies from routine to routine, and will be described in
more detail below.

Varions %options are possible for many routines, to provide a more-general API.  In some cases,
the full flexibility of the underlying Foundation REST API is made more convenient to use by
supporting sensible options.  Details are provided below for particular routines.

=head1 RETURNED DATA

Data is returned from each call in three forms:

    * The function-call return $status.  This is a simple boolean value
      which is true if the call completely succeeded, and false if not.

    * Content of the %outcome hash, which is automatically cleared within
      the routine and then filled in with any interesting details.

    * Content of the @results array or %results hash, which will reflect
      a collection of objects of the expected type related to the nature
      of the called routine.

There are three types of hash-key collections which may appear in the %outcome hash.
The first relates to HTTP or request errors:

On a bad-request or transport-level error, the %outcome hash will look like this:

    {
	# numeric; 200 => success; other values per the HTTP standards
	'response_code'   => $http_status_code,

	# brief interpretation of the HTTP response code
	'response_status' => $http_status_brief_explanation,

	# full readable message, providing application-level detail if possible
	'response_error'  => $the_response_content
    }

In particular, requests which do not match the Foundation REST API requirements
(e.g., due to misspelled or miscapitalized parameter names) will return a 400 Bad Request error.
If you set the GW::RAPID logger's logging level high enough, your application log may contain further analysis.

If the call gets past the initial checks, then for almost all calls, %outcome will look something like this instead:

    {
	count      => 1,
	entityType => 'Ack',
	failed     => 1,
	operation  => 'Update',
	successful => 0,
	warning    => 0
    }

If $status is not true, your application should look at these values to determine the overall
status of the operation, in more detail than is provided by the simple boolean reture value
from the called routine.

If you got this far after a call to check_license(), %outcome will instead have this form:

    {
	devices          => 3,
	devicesRequested => 30,
	message          => 'OK to allocate 30 devices',
	success          => bless( do { \( my $o = 1 ) }, 'JSON::XS::Boolean' )
    }

The odd form of the "success" member of this hash in its stringified Perl form, as shown,
is irrelevant.  All that you care about is that it will be either true or false in the Perl
sense, reflecting the status of the device-allocation check.

If a @results array is returned from the call, elements in this array will correspond in order
to elements in the @objects array.

Each value in a @results array will be a hashref.  For object create or update operations, the
status of the operation on each individual object will be listed in each individual returned result
hashref within the @results array.  A typical individual result hash will look something like this:

    {
	entity  => 'localhost',
	status  => 'failure',
	message => 'failed to find NAGIOS acknowledgable Event for criteria'
    }

The "entity" field, if present, will contain a unique identifer for the object in question.
The "status" field should be one of:  'failure', 'success', or 'warning'.
The "message" field, if present, will contain more detail on the status of the operation.

If a %results hash is returned from the call, elements in the hash will use a unique identifier
for the object as the %results hash key.  The particular nature of the identifier depends
on the object type in question.  Services are a special case, which allows more flexibility;
see the documentation below on the get_services() call.

Each value in a %results hash will be a hashref.  For object queries, the set of keys is
determined by the type of object in question.
FIX MAJOR:  Specific documentation on these hash keys is yet to be provided.

=head1 FUNCTION CATEGORIES

Functions fall into the following categories:

    - Initialization Routines
    - Information Retrieval Routines
    - Create and Update (a.k.a. 'Upsert') Routines
    - Object Deletion Routines
    - Other Routines

=head2 Initialization Routines

These routines initialize the connection to the REST API and are required to
be done before using other functions.

=over

my $rest_api = GW::RAPID->new( $protocol, $hostname, $username, $password, $requestor, \%options );

This constructor creates a connection to the GroundWork REST API, and returns
an GW::RAPID instance, or undef if the constructor fails.  A call to the
constructor is required before using any of the GW::RAPID module functions.
Required constructor arguments are:

    $protocol  - HTTP protocol to use when using the REST API ('http' or 'https' only)
    $hostname  - hostname of where the Foundation REST API is listening; e.g., 'localhost'
    $username  - username to connect to the REST API with; e.g., 'wsuser'
    $password  - password of $username to connect to the REST API with
    $requestor - name of the calling application
    \%options  - optional additional parameters

    valid options within the %options hash:
	timeout         => integer (seconds), max time to wait for the server to
			   respond to a REST API call before aborting from the
			   client side; imposition of the client-side timeout on
			   a REST call will appear as an $outcome{response_status}
			   of "Internal Server Error" even though it's really a
			   client-side issue
	logger          => Log::Log4perl logger handle
	restport        => alternate port number for accessing the REST API
	basepath        => alternate leading pathname for accessing the REST API
			   (must begin with "/" and end in "/api")
	access          => path to a file containing REST API access credentials
			   and URL, using the same parameters as are found in
			   /usr/local/groundwork/config/ws_client.properties
	scrambled       => boolean flag; true means $password is being passed to
			   this routine in encrypted form
	interruptible   => reference to a scalar variable which will be set by the
			   calling application's signal handlers if a termination
			   signal has been received
	force_crl_check => boolean value; true means to insist on having a Certificate
			   Revocation List available on the client if the server has
			   SSL enabled
	multithreaded   => boolean value; this option MUST be set and true if this
			   program will be operating with more than one thread
	token           => authentication token available from some previous use
			   of the GW::RAPID package; use of this facility is complex,
			   not generally recommnded, and not documented here

If you specify the $options{access} parameter, you can leave $protocol, $hostname,
$username, and $password undefined in the constructor call, and you need not worry
about setting a value for the $options{scrambled} parameter.  These parameters will be
picked up from the file; this is generally easier than parsing your own resource file
to find such values.  Except for $options{scrambled}, any of these parameters that you
supply explicitly (including $options{restport} and $options{basepath}) will override
values from the access file.

The $options{interruptible} parameter is provided to allow a means to have possibly
long-running requests be interruptible even if the calling code normally operates
with signal handlers in play that do little else than set a flag.  In such a case
(the normal, clean mechanism by which Perl signal handlers are established), any
REST API requests will simply be restarted when they are interrupted, thereby not
achieving the desired termination effect.  If this parameter is specified as a
scalar reference to the flag just mentioned, then it will be tested at appropriate
times within the GW::RAPID package to ensure that incoming termination signals are
not overlooked due to race conditions.

The signal handling within the GW::RAPID package is transient and only affects
operation within the package.  If the $options{interruptible} parameter is set as
described, then for the duration of critical calls to this package, SIGINT, SIGQUIT,
and SIGTERM signal handling will be disabled, so the process will immediately die
if such signals are received while the thread of execution is within those calls.
This is clumsy and doesn't allow the calling application to intercede for a soft
landing.  But there doesn't seem to be any other way to get the process to terminate
quickly, because the internals of the REST::Client package that we use are written
in C and will otherwise just restart the failed network call instead of aborting the
application routine and returning to the caller with some sort of exception status.

The $options{force_crl_check} parameter is provided to allow a means to run a client
over an SSL connection even if a Certificate Revocation List file is not set up on
the client.  This option defaults to a true value; if you are comforable with running
the client without a CRL file, you must set it explicitly to a false value.

Return Values

    Returns a class instance on success, undef otherwise.  In the latter case,
    $@ contains an error message.

Example

    # For logging from the GW::RAPID package, if not also from your own application.
    use Log::Log4perl;

    use GW::RAPID;

    # Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here's where any logging data should end up.
    my $logfile = '/path_to/my_application.log';

    # There are six predefined log levels within the Log4perl package:
    # FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # Choose here the level at which you wish to log GW::RAPID messages.
    my $GW_RAPID_log_level = 'WARN';

    # As recommended in the Log4perl documentation, we DO NOT try to mirror Perl
    # package names here as logging category names.  A more sensible classification
    # of categories provides more intelligent control across applications.
    my $log4perl_config = <<EOF;
    # Use this to send everything from FATAL through $GW_RAPID_log_level to the logfile.
    log4perl.category.My.Application.GW.RAPID = $GW_RAPID_log_level, Logfile

    # Send all Log4perl lines to the same log file as the rest of this application.
    log4perl.appender.Logfile          = Log::Log4perl::Appender::File
    log4perl.appender.Logfile.filename = $logfile
    log4perl.appender.Logfile.layout   = Log::Log4perl::Layout::PatternLayout
    log4perl.appender.Logfile.layout.ConversionPattern = [%d{EEE MMM dd HH:mm:ss yyyy}] %m%n
    EOF

    # This logging setup is an application-global initialization for the
    # Log::Log4perl package, so it only makes sense to initialize it at the
    # application level, not in some lower-level package.
    #
    # It's not documented, but apparently Log::Log4perl::init() always
    # returns 1, even if it is handed a garbage configuration as a literal
    # string.  That makes it hard to tell if you really have it configured
    # correctly.  On the other hand, if it's handed the path to a missing
    # config file, it throws an exception (also undocumented).
    eval {
	## Different applications will have different means of storing and
	## accessing logging config data, so this example demonstrates some
	## flexibility.  If the $log4perl_config value starts with a leading
	## slash, we take it to refer to an external file containing the
	## logging configuration data.  Otherwise, that value is taken here
	## as the literal config data, as shown above.
	Log::Log4perl::init( $log4perl_config =~ m{^/} ? $log4perl_config : \$log4perl_config );
    };
    if ($@) {
	print STDERR "ERROR:  Could not initialize Log::Log4perl logging:\n$@";
	sleep 10;  # If this process is automatically restarted, don't do so too quickly.
	exit 1;
    }

    # The logger obtained here must have a category which matches a corresponding
    # category in your logging config data.  Don't just copy this literally and use
    # "My.Application"; modify as needed to reflect the specifics of your context.
    my %rest_api_options = (
	timeout => 30,
	logger  => Log::Log4perl::get_logger('My.Application.GW.RAPID'),
	access  => '/usr/local/groundwork/config/ws_client.properties'
    );

    # Adjust the requestor name here, at a minimum.
    my $rest_api = GW::RAPID->new( undef, undef, undef, undef,
	'my application daemon', \%rest_api_options );
    if (not $rest_api) {
	## constructor failed; $@ contains an error message
    }

    # Continue on, and make calls to the REST API as needed.
    ...;

    # IMPORTANT:  Before process exit, be sure to release your handle to the
    # REST API.  This will force GW::RAPID to call its destructor.  And that
    # will attempt to log out before Perl's global destruction pass wipes
    # out resources needed for logout to work properly.  We want to log out
    # in order to be polite and release server-side resources right away.
    $rest_api = undef;

=back

=head2 Information Retrieval Routines

These functions are primarily used for retrieving information about objects such as hosts, hostgroups, etc.
They mostly follow a similar invocation pattern:

    $status = $rest_api->get_objects( \@objects, \%options, \%outcome, \%results )

This generic signature is used in certain standard patterns:

    # retrieve one specific xyz object
    $status = $rest_api->get_xyz( [$thing], {}, \%outcome, \%results );

    # retrieve all xyz objects
    $status = $rest_api->get_xyz( [], {}, \%outcome, \%results );

    # retrieve all xyz objects matching a query
    $status = $rest_api->get_xyz( [], { query => $query }, \%outcome, \%results );

Where

    $thing   - some identifier, such as a hostname, a hostgroup name, etc.
    %options - a set of routine-specific parameters to adjust the nature of the call
    %outcome - a hash for detailed information about the call execution status
    %results - a hash for the REST API response (i.e., result or errors),
	       decoded from JSON back into a Perl structure.
    $query   - a query string as described in the REST API documentation, e.g.:
	       "property.LastPluginOutput like 'OK%'"

Return values

    All of these functions return true on success, false otherwise.

    On success, the %results hash will contain all the individual items that
    were retrieved.  The hash key value will depend on the type of object
    being retrieved:

	version    => version
	hosts      => hostName
	hostgroups => name
	services   => id               # may be overridden by $options{format}
	events     => id
	devices    => identification
	categories => name

	FIX MAJOR:  What about for application types and audit logs?

    For services, the hash structure returned can be selected in the get_services()
    call, by means of the $options{format} value.  It can be specified as one of:

	'id'           # returns a $results{$id}{$objectkey} hash (default)
	'host,service' # returns a $results{$host}{$service}{$objectkey} hash
	'service,host' # returns a $results{$service}{$host}{$objectkey} hash

    For license checks, currently the returned %results hash is always empty.

The functions are described as follows:

=over

    VERSION

	# Retrieve the version of the underlying REST API.
	$status = $rest_api->get_version( \@versions, \%options, \%outcome, \%results );

	where:
	* \@versions must currently be an arrayref to an empty array
	* no %options are currently supported

	It might seem odd to support this great generality in the form of
	the function call.  That is done for several reasons:

	* It is possible that in the future, the user might conceivably be
	  able to send a list of desired versions, to have the server check
	  compatibility against just that list.

	* It is possible that in the future, the user might receive in return
	  a set of compatible versions that are supported by the REST API,
	  and that at that time can be selected for use when interacting
	  with the REST API.

	* We use the %outcome and %results hashes in a manner which is
	  consistent with their treatment in the rest of this package's
	  routines.  This simplifies learning about and using the interface.

	The %results hash key will be the version number of the REST API,
	as a string.  The value of the hash entry for that key will be true,
	though its exact representation may contain much more detail than
	just a single numeric.  It is up to the calling application to
	turn this into whatever form is needed (e.g., a Perl v-string) for
	whatever comparisons are needed at the application level.  Note that
	there are no guarantees that any sort of greater-than-or-equal-to
	comparisons will yield the intended interpretation, since facilities
	in the REST API might eventually be deprecated as well as added.
	So if there is some particular feature you need, you will need to
	take special care to run proper comparisons to ensure that your code
	can run against a particular version of the REST API.

    Examples:

	my %outcome;
	my %results;

	$status = $rest_api->get_version( [], {}, \%outcome, \%results );

	# To compare against just a single known release level:
	my $found_compatible_release = 0;
	if ($status) {
	    foreach my $v ( keys %results ) {
		my $version = ( $v =~ /unknown/ ) ? $results{$v}{value} : $v;
		if ( $version eq '7.2.1' ) {
		    ## We are operating against version 7.2.1 of the REST API.
		    $found_compatible_release = 1;
		    last;
		}
	    }
	}
	if (not $found_compatible_release) {
	    ## Too bad for us.
	}

    LICENSE CHECK

	# Check possible changes against license limits.
	$status = $rest_api->check_license( \@deviceidentifications, \%options, \%outcome, \%results );

	where:
	* \@deviceidentifications must currently be a reference to an empty array
	* Only one %options value (allocate => integer) is supported.
	* In the current implementation, %results is always returned as an empty hash.

	It might seem odd to support this great generality in the form
	of the function call, even though much of it is currently unused.
	That is done for several reasons:

	* It is possible that in the future, the user might conceivably be able
	  to send a list of devices, to have the server check support against
	  just that list.  For instance, under control of some particular
	  option setting, the call might check whether those particular
	  devices are blacklisted and ought not to be added to the system.

	* It is possible that in the future, other forms of this call might
	  be supported, in which the user might receive in return a sensible
	  set of objects.

	* We use the %outcome and %results hashes in a manner which is
	  consistent with their treatment in the rest of this package's
	  routines.  This simplifies learning about and using the interface.

	In the current implementation, the %results hash will always be empty
	after the call.  You should instead be looking at the returned $status,
	and if that is not sufficient detail for you, at the returned %outcome.
	The $status will be true if the proposed number of new devices is
	acceptable within the license limit, so it is reasonable to attempt
	adding them.  It will be false if either the call failed or the
	proposed allocation is not acceptable.

	You may specify { allocate => 0 } to simply check whether the system is
	currently over the limit.  If you do not specify an "allocate" option,
	it will default to 0, and so will run this type of check.

	A non-zero "allocate" option value specifies how many new devices you
	would like to add to (positive) or delete from (negative) the system.
	If your license limit would be exceeded after such a change, the call
	will fail.  If the call succeeds, that information should be treated
	as only advisory, as a point-in-time probe -- there is no guarantee
	that an actual attempt to add that many devices to the system would
	actually succeed.  For instance, between the time of the check and the
	time of the addition, other actors in the system might soak up some
	number of the available device slots.  That means that any downstream
	code that attempts to add hosts after a successful check_license()
	call must still check the returned status of the add-host calls to
	see whether they succeeded.

    Examples:

	my %outcome;
	my %results;

	# Are we still within our limit?
	if ( not $rest_api->check_license( [], {}, \%outcome, \%results ) ) {
	    ## Either the call itself failed, or we're already over the limit.
	    if ( defined $outcome{success} ) {
		## "success" will be defined but false in this case; we're already over the limit.
	    }
	    else {
		## The call itself failed; perhaps the other side went down.
	    }
	}
	else {
	    ## All was okay, at that moment.
	}

	# Same thing:  just check current status.
	if ( not $rest_api->check_license( [], { allocate => 0 }, \%outcome, \%results ) ) {
	    ## as above ...
	}

	# See if there is room for a few new devices.
	if ( not $rest_api->check_license( [], { allocate => 5 }, \%outcome, \%results ) ) {
	    ## Either the call itself failed, or there was not room for that many.
	    if ( defined $outcome{success} ) {
		## "success" will be defined but false in this case; adding 5 devices would run us over the limit.
	    }
	    else {
		## The call itself failed; perhaps the other side went down.
	    }
	}
	else {
	    ## There was (momentarily, at least) still room for 5 new devices.
	}

    APPLICATION TYPES

	# Retrieve a set of application types
	$status = $rest_api->get_application_types( \@app_type_names, \%options, \%outcome, \%results );

	where:
	* an empty @app_type_names array retrieves all application types (subject to filtering via %options)
	* a single-element @app_type_names array retrieves just that one application type (if not filtered out via %options)
	* %options may be used to apply object filtering, via {query=>$query} and other constraints,
	  or to limit the amount of detail retrieved (via {depth=>'xxx'})

    Examples:

	my %outcome;
	my %results;

	# retrieve VEMA
	if ( not $rest_api->get_application_types( ['VEMA'], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# retrieve all application types
	if ( not $rest_api->get_application_types( [], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

    HOSTS

	# Retrieve a set of hosts
	$status = $rest_api->get_hosts( \@hostnames, \%options, \%outcome, \%results );

	where:
	* an empty @hostnames array retrieves all hosts (subject to filtering via %options)
	* a single-element @hostnames array retrieves just that one host (if not filtered out via %options)
	* %options may be used to apply object filtering, via {query=>$query} and other constraints,
	  or to limit the amount of detail retrieved (via {depth=>'xxx'})

    Examples:

	my %outcome;
	my %results;

	# retrieve localhost
	if ( not $rest_api->get_hosts( ['localhost'], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# retrieve all hosts
	if ( not $rest_api->get_hosts( [], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# retrieve hosts matching a criteria
	if (
	    not $rest_api->get_hosts(
		[], { query => "property.LastPluginOutput like 'OK%'" },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# like the previous example but with paging
	if (
	    not $rest_api->get_hosts(
		[],
		{
		    first => 1,
		    count => 5,
		    query => "property.LastPluginOutput like 'OK%'"
		},
		\%outcome,
		\%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve localhost with depth = simple
	if (
	    not $rest_api->get_hosts(
		['localhost'], { depth => 'simple' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve first 5 hosts with depth = simple
	if (
	    not $rest_api->get_hosts(
		[], { depth => 'simple', first => 1, count = 5 },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

    HOSTGROUPS

	# Retrieve one or more hostgroups.
	$status = $rest_api->get_hostgroups( \@hostgroupnames, \%options, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	# retrieve a single hostgroup
	if ( not $rest_api->get_hostgroups( ['Linux Servers'], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# retrieve all hostgroups
	if ( not $rest_api->get_hostgroups( [], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# FIX MAJOR:  come up with a better critera, other than matching names
	# retrieve hostgroups matching criteria
	if (
	    not $rest_api->get_hostgroups(
		[], { query => "name in ('hg1','hg2','hg3')" },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve first five hostgroups matching criteria, simple depth
	if (
	    not $rest_api->get_hostgroups(
		[ 'hg1', 'hg2', 'hg3' ],
		{ first => 1, count => 5, depth => 'simple' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve a specific hostgroup, shallow depth
	if (
	    not $rest_api->get_hostgroups(
		['Linux Servers'], { depth => 'shallow' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve first 3 hostgroups, deep depth
	if (
	    not $rest_api->get_hostgroups(
		[], { depth => 'deep', first => 1, count => 3 },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

    HOSTS BLACKLISTS

	# Retrieve a set of host blacklists
	$status = $rest_api->get_hostlists( \@hostnames, \%options, \%outcome, \%results );

	where:
	* an empty @hostnames array retrieves all host blacklists (subject to filtering via %options)
	* a single-element @hostnames array retrieves just that one host blacklist (if not filtered out via %options)
	* %options may be used to apply object filtering, via {query=>$query} and other constraints,
	  or to limit the amount of detail retrieved (via {depth=>'xxx'})

    HOST IDENTITIES

	# Retrieve host idendities by hostname list
	$status = $rest_api->get_hostidentities( \@hostnames, \%options, \%outcome, \%results );

	# Use query to retrieve host idendities by hostIdentityId's
	$status = $rest_api->get_hostidentities( [], { query => "hostIdentityId = '$id'" }, \%outcome, \%results );

    HOST IDENTITIES AUTOCOMPLETION

	# Retrieve host idendities autocompletion suggestions, by prefix
	$status = $rest_api->get_hostidentities_autocomplete( \@prefix, \%options, \%outcome, \%results );

	Note: @prefix is an array that contains just one entry - a string that is the prefix to autocomplete against.

    CUSTOMGROUPS

	# Retrieve all customgroups
	$status = $rest_api->get_customgroups( [ ], {}, \%outcome, \%results );

	# Retrieve specifc set of customgroups
	$status = $rest_api->get_customgroups( [ 'group1', 'group2', 'group3' ], {}, \%outcome, \%results );

    CUSTOMGROUPS AUTOCOMPLETION

	# Retrieve customgroups autocompletion suggestions, by prefix
	$status = $rest_api->get_customgroups_autocomplete( [ "group" ]  , {},  \%outcome, \%results );

	Note: @prefix is an array that contains just one entry - a string that is the prefix to autocomplete against.

    SERVICES

	# Retrieve host services:  general form
	$status = $rest_api->get_services( \@servicenames, \%options, \%outcome,
	    \%results );

	# Retrieve the specified service for just the named host
	$status = $rest_api->get_services( [$servicename], { hostname => $hostname },
	    \%outcome, \%results );

	# Retrieve the specified service for all the named hosts
	$status = $rest_api->get_services( [$servicename], { hostname => \@hostnames },
	    \%outcome, \%results );

	# Retrieve all services for just the named host
	$status = $rest_api->get_services( [], { hostname => $hostname },
	    \%outcome, \%results );

	# Retrieve all services for all the named hosts
	$status = $rest_api->get_services( [], { hostname => \@hostnames },
	    \%outcome, \%results );

	# Retrieve services for hosts based on query
	$status = $rest_api->get_services( [], { query => $query },
	    \%outcome, \%results );

	Note that the "hostname" option has a capitalization which is different
	from that used for the "hostName" parameter in the underlying Foundation
	REST API.  This has been done to emphasize the flexibility of this option
	at the Perl level.  Here, it can specify either a single hostname or an
	array of hostnames.

	Note that the structure of the returned %results hash can be selected
	via an optional $options{format} value, as noted earlier.

    Examples:

	my %outcome;
	my %results;

	if ( not $rest_api->get_services( ['local_load'], { hostname => 'localhost' }, \%outcome, \%results ) ) {
	    ## Failed.
	}
	if ( not $rest_api->get_services( [], { hostname => 'localhost' }, \%outcome, \%results ) ) {
	    ## Failed.
	}
	if ( not $rest_api->get_services( [], { hostname => [ 'localhost', 'remotehost' ] }, \%outcome, \%results ) ) {
	    ## Failed.
	}
	if ( not $rest_api->get_services( [], { query => "monitorStatus = 'OK'" }, \%outcome, \%results ) ) {
	    ## Failed.
	}

    SERVICEGROUPS

	# Retrieve one or more servicegroups.
	$status = $rest_api->get_servicegroups( \@servicegroupnames, \%options, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	# retrieve a single servicegroup
	if ( not $rest_api->get_servicegroups( ['databases'], {}, \%outcome, \%results ) ) {
	    ## Failed.
	}

	# retrieve all servicegroups associated with a specific agent
	if (
	    not $rest_api->get_servicegroups(
		[], { agentId => '772cbe06-1bb2-01e4-ade0-7dfbc6f6a3e2' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# FIX MAJOR:  come up with a better critera, other than matching names
	# retrieve servicegroups matching criteria
	if (
	    not $rest_api->get_servicegroups(
		[], { query => "name in ('sg1','sg2','sg3')" },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve first five servicegroups matching criteria, depth simple
	if (
	    not $rest_api->get_servicegroups(
		[ 'sg1', 'sg2', 'sg3' ],
		{ appType => 'NAGIOS', first => 1, count => 5, depth => 'simple' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve a specific servicegroup, simple depth
	if (
	    not $rest_api->get_servicegroups(
		['databases'], { depth => 'simple' },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

	# retrieve first 3 servicegroups, simple depth
	if (
	    not $rest_api->get_servicegroups(
		[], { depth => 'simple', first => 1, count => 3 },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed.
	}

    EVENTS

	# Retrieves event(s) (@eventids is a list of event IDs)
	$status = $rest_api->get_events( \@eventids, \%options, \%outcome, \%results );

	# Retrieves all events (not a good idea!)
	$status = $rest_api->get_events( [], {}, \%outcome, \%results );

	# Retrieves events based on query
	$status = $rest_api->get_events( [], { query => $query }, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	## FIX MAJOR:  This is just plain wrong; it's a copy/paste error.
	if ( not $rest_api->get_events( 'local_load?hostName=localhost', \%outcome, \%results ) ) {
	    ## Failed; look at $outcome{response_error} for an error message.
	}
	## FIX MAJOR:  Is hostName proper here?  Or do we support hostname and an arrayref instead?
	if ( not $rest_api->get_events( [], { hostName => 'localhost' }, \%outcome, \%results ) ) {
	    ## Failed; look at $outcome{response_error} for an error message.
	}
	if ( not $rest_api->get_events( [], { query => "monitorStatus = 'OK'" }, \%outcome, \%results ) ) {
	    ## Failed; look at $outcome{response_error} for an error message.
	}

	# Get the last logmessage.  But DON'T DO THIS!  This (retrieve EVERYTHING,
	# sort EVERYTHING, only pull back one item) would be horribly inefficient,
	# if the "id" field is not indexed.
	if (
	    not $rest_api->get_events(
		[], { first => 1, count => 1, query => "order by id desc" },
		\%outcome, \%results
	    )
	  )
	{
	    ## Failed; look at $outcome{response_error} for an error message.
	}

    DEVICES

	# Retrieve devices
	$status = $rest_api->get_devices( \@deviceidentifications, \%options, \%outcome, \%results );

	# Retrieve a device
	$status = $rest_api->get_devices( [$deviceidentification], \%options, \%outcome, \%results );

	# Retrieve all devices
	$status = $rest_api->get_devices( [], {}, \%outcome, \%results );

	# Retrieve device(s) based on query
	$status = $rest_api->get_devices( [], { query => $query }, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	if ( not $rest_api->get_devices( ['127.0.0.1'], \%options, \%outcome, \%results ) ) {
	    ## Failed.
	}
	if ( not $rest_api->get_devices( [], { depth => 'shallow' }, \%outcome, \%results ) ) {
	    ## Failed.
	}
	if ( not $rest_api->get_devices( [], { query => "description like '%localhost'" }, \%outcome, \%results ) ) {
	    ## Failed.
	}

    CATEGORIES

	# Retrieve categories
	$status = $rest_api->get_categories( \@categorynames, \%options, \%outcome, \%results );

	# Retrieve a category
	$status = $rest_api->get_categories( [$categoryname], \%options, \%outcome, \%results );

	# Retrieve all categories
	$status = $rest_api->get_categories( [], {}, \%outcome, \%results );

	# Retrieve categories based on query
	$status = $rest_api->get_categories( [], { query => $query }, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	if (not $rest_api->get_categories( ['some service group'], \%options, \%outcome, \%results )) {
	    ## Failed.
	}
	if (not $rest_api->get_categories( [], \%options, \%outcome, \%results )) {
	    ## Failed.
	}
	if (not $rest_api->get_categories( [], { query => "entityTypeName = 'SERVICE_GROUP'" }, \%outcome, \%results )) {
	    ## Failed.
	}

    AUDIT-LOG DATA

	# Retrieve audit-log data, general form.
	$status = $rest_api->get_auditlogs( \@auditlogids, \%options, \%outcome, \%results );

	# Retrieve a specific audit-log entry.  This is not a practical call because the only real
	# way to obtain a particular $auditlogid value is from a previous auditlog retrieval, in
	# which case you already had the entire entry already.  But we provide this capability
	# for completeness and for parallelism with other object types.
	$status = $rest_api->get_auditlogs( [$auditlogid], \%options, \%outcome, \%results );

	# Retrieve all audit-log data (NOT RECOMMENDED; potentially huge returned results)
	$status = $rest_api->get_auditlogs( [], {}, \%outcome, \%results );

	# Retrieve paged audit-log data
	$status = $rest_api->get_auditlogs( [], { first => 1, count => 10 }, \%outcome, \%results );

	# Retrieve paged audit-log data for a specific host
	$status = $rest_api->get_auditlogs( [], { hostname => 'my-host', first => 1, count => 10 }, \%outcome, \%results );

	# Retrieve paged audit-log data for a specific host and service
	$status = $rest_api->get_auditlogs( [], { hostname => 'my-host', servicename => 'my-service', first => 1, count => 10 }, \%outcome, \%results );

	# Retrieve categories based on query (see the RESTful Services doc for how to construct a useful query).
	$status = $rest_api->get_auditlogs( [], { query => $query }, \%outcome, \%results );

    Examples:

	my %outcome;
	my %results;

	# Retrieve a specific known auditlog entry.
	if (not $rest_api->get_auditlogs( [42], \%options, \%outcome, \%results )) {
	    ## Failed.
	}

	# Retrieve all auditlog entries for a given host.  Potentially huge result without
	# "first" and "count" option values supplied.
	if (not $rest_api->get_auditlogs( [], { hostname => 'my-host' }, \%outcome, \%results )) {
	    ## Failed.
	}

	# Retrieve the last few auditlog entries for one particular user.
	if (not $rest_api->get_auditlogs( [], { query => "userName = 'joeschoe' order by timestamp DESC", count => 4 }, \%outcome, \%results )) {
	    ## Failed.
	}

    BIZ DOWNTIME

	Retrieve scheduledDowntimeDepth values for hosts/services. For example, get the scheduledDowntimeDepth for host ahost4 :

	my $get = {
	    "bizHostServiceInDowntimes" => [
		{
		    "entityName" => "some_hostgroup",
		    "entityType" => "HOSTGROUP",
		    "hostName"   => "ahost4"
		}
	    ]
	};

	$rest_api->get_indowntime( $get, { }, \%outcome, \@results );

	See the GroundWork documentation -> Developer Reference -> RESTful Services for more details.

=back

=head2 Create and Update (a.k.a. 'Upsert') Routines

These functions are used for creating (i.e. inserting) or updating objects such as hosts,
hostgroups etc.  "UpSert'ing": If the object already exists, it gets updated. If the object
doesn't exist, it gets inserted (i.e., created).  They mostly follow a similar invocation pattern:

    $rest_api->upsert_xyz( \@objects, \%options, \%outcome, \@results );

Where

    \@objects - a reference to a Perl array. This data structure gets converted to
		 a JSON object and POST'ed to the REST API.
    \%options - a hash reference for desired REST API options (e.g., {async => 'true'},
		 for some types of objects).
    \%outcome - a hash reference for the REST API response (i.e., success/failure indicators),
		 decoded from JSON back into a Perl structure.
    \@results - an array reference for the REST API response (i.e., data on individual objects),
		 decoded from JSON back into a Perl structure.

Return Values

    If the HTTP status code from the POST operation is 200, then the results
    are returned in the \%outcome and \@results data structures.  Examples of
    these data structures (presented here as references) are:

    Example 1 - Successfully create a new host:

	$outcome = {
	    'count'      => 1,
	    'entityType' => 'Host',
	    'failed'     => 0,
	    'operation'  => 'Update',
	    'successful' => 1,
	    'warning'    => 0
	};

	$results = [
	    {
		'entity'   => 'my_test_host',
		'location' => 'http://localhost/foundation-webapp/api/hosts/my_test_host',
		'status'   => 'success'
	    }
	];

    Example 2 - Failed to create a host because of a missing required hostName property:

	$outcome = {
	    'count'      => 1,
	    'entityType' => 'Host',
	    'failed'     => 1,
	    'operation'  => 'Update',
	    'successful' => 0,
	    'warning'    => 0
	};

	$results = [
	    {
		'message' => 'com.groundwork.collage.exception.CollageException: Unable to create hosts - Error occurred in createHosts()',
		'status'  => 'failure'
	    }
	];

	Note in this example that no "entity" property is returned by the API.  This is because
	none was supplied in the attempt to create the host.  However, somehow this result is
	different from that obtained when the same error is made while adding more than one host
	(see the next example, where "unknown host" is returned for the same submission error).
	In general, when creating or updating an object, the results returned will be in the
	same number and order as the objects submitted, so you should use those associations
	to determine what worked and what failed (by looking at the "status" member of each
	individual returned-object hash within the @results array).

    Example 3 - Failed to create multiple hosts because of a missing
	required hostName or deviceIdentification property:

	$outcome = {
	    'count'      => 2,
	    'entityType' => 'Host',
	    'failed'     => 2,
	    'operation'  => 'Update',
	    'successful' => 0,
	    'warning'    => 0
	};

	$results = [
	    {
		'entity'  => 'unknown host',
		'message' => 'failed to find hostname property',
		'status'  => 'failure'
	    },
	    {
		'entity'  => '__RAPID_test_host_1395798127_3',
		'message' => 'com.groundwork.collage.exception.CollageException: Can\'t add host to system. Device Identification is required',
		'status'  => 'failure'
	    }
	];

	Note in this example that the general key "entity" is used to identify each object,
	rather than some key which is specific to the object type.  However, the unique
	object-identifying value is obviously object-type-specific.

	    # FIX MAJOR:  Verify this table; it's probably wrong for at least services.
	    hosts      => hostName
	    hostgroups => name
	    services   => id
	    events     => id
	    devices    => identification
	    categories => name

    Example 4 - Failed to update a hostgroup in a batch because of hostgroup __RHUBARB__ didn't exist:

	$outcome = {
	    'count'      => 2,
	    'entityType' => 'HostGroup',
	    'warning'    => 1,
	    'operation'  => 'Update',
	    'failed'     => 0,
	    'successful' => 1
	};

	# FIX MAJOR
	$results = {
	    '__RAPID_test_hostgroup_1391180132' => {
		'location' => 'http://localhost/foundation-webapp/api/hostgroups/__RAPID_test_hostgroup_1391180132',
		'status'   => 'success'
	    },
	    '__RHUBARB__' => {
		'status'  => 'warning',
		'message' => 'Hosts did not exist and were not processed'
	    }
	};

    The return value of the $rest_api->upsert_xyz() function is determined
    by looking at fields of the %outcome data structure in order as follows:

	1. If 'warning' is non zero, the return value is 0 (failure).
	2. If 'failed'  is zero,     the return value is 1 (success).
	3. If neither of the above conditions were met, the return value is 0 (failure).

    If the HTTP status code is not 200, the return value of the $rest_api->upsert_xyz() function
    will always be 0 (failure).  In this case, the \%outcome hash will look like this:

	{
	    'response_code'   => $http_response_code,
	    'response_status' => $interpreted_http_response_code,
	    'response_error'  => $response_content
	}

    where:

    * The response_code field will be a numeric HTTP response code (e.g., 404).
    * The response_status field will be a short message showing the general nature
      of the response_code (e.g., 'Not Found').
    * The response_error field will contain full details of the error.  This string
      may contain an HTML document, not a plaintext message.


The functions are described as follows:

=over

    APPLICATION TYPES

	# Create and/or update application type(s)
	$status = $rest_api->upsert_application_types( \@app_types, \%options, \%outcome, \@results );

    Example:

	my @app_types = (
	    {
		'name'                    => $app_type_name,
		'description'             => "RAPID test application type",
		'stateTransitionCriteria' => 'Device;Host'
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_application_types( \@app_types, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOSTS

	# Create and/or update host(s)
	$status = $rest_api->upsert_hosts( \@hosts, \%options, \%outcome, \@results );

	SPECIAL NOTE:  The upsert_hosts() call is most often used in high-traffic code paths,
	where end-to-end delays in waiting for final execution results could result in
	substantial application slowdown and consequent latency in overall status updates.
	For that reason, this call operates by default in an asynchronous mode, whereby the
	upsert request is accepted and queued immediately, and the call returns with @results
	reflecting that fact, while execution of the requests is deferred until the server can
	get to this work.  This strategy allows the client application to work more readily in
	parallel with the server, preparing more changes to be submitted.  Note, however, that the
	returned @results therefore do not reflect the final execution status of these requests.
	If your application needs to control the nature of the returned results, it can do so
	via the "async" option, which can be specified in the %options hash with a string value
	(not a boolean value) of either 'true' or 'false'.  See the example below.

    Example:

	# NOTE:  monitorStatus should probably be "PENDING" for a newly-created host.
	# FIX MAJOR:  Is "deviceDisplayName" legal here?  It's not documented as a
	# primary attribute of a host.  Is it available as a property instead?
	my @hosts = (
	    {
		"hostName"             => $hostname,
		"description"          => "CREATED at " . localtime,
		"monitorStatus"        => "UP",
		"appType"              => "NAGIOS",
		"deviceIdentification" => "10.20.30.40",
		"monitorServer"        => "localhost",
		"deviceDisplayName"    => $hostname,
		"properties" =>
		  { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_hosts( \@hosts, { async => 'true' }, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOSTGROUPS

	# Create and/or update hostgroup(s)
	$status = $rest_api->upsert_hostgroups( \@hostgroups, \%options, \%outcome, \@results );

    Example:

	my @hostgroups = (
	    {
		"name"        => $hostgroup,
		"description" => "CREATED at " . localtime,
		"alias"       => "Alias for $hostgroup",
		"hosts"       => [ { "hostName" => $hostname1 }, { "hostName" => $hostname2 } ]
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST BLACKLISTS

	# Create and/or update host blacklists
	$status = $rest_api->upsert_hostblacklists( \@bls, \%options, \%outcome, \@results );

    Example to create new blacklists:

	my @create_new_blacklists = (
        	{ 'hostName' => 'ablt1' },
        	{ 'hostName' => 'ablt2' },
        	{ 'hostName' => 'ablt3' }
	);
	my ( %outcome, @results );

	if ( not $rest_api->upsert_hostblacklists( \@create_new_blacklists, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    Example to update existing new blacklists:

	NOTE that the correct corresponding hostBlacklistId values are required to perform an update ...

	my @update_blacklists = (
        	{ 'hostName' => 'ablt1' , 'hostBlacklistId' => 123 },
        	{ 'hostName' => 'ablt2' , 'hostBlacklistId' => 124 }
	);

	if ( not $rest_api->upsert_hostblacklists( \@update_blacklists, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST IDENTITIES

	# Create host identities
	@create_new_identities = (
    		{
        		"hostName"  => "ahost1",
        		"hostNames" => ["ahost1-alias1", "ahost1-alias2"]
    		},
    		{
        		"hostName"  => "ahost2",
        		"hostNames" => ["ahost2-alias1", "ahost1-alias2"]
    		}
	);
	$rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results );

	NOTE : that if the hostName doesn't exist, the API creates the host automatically.

	# Update host identities - add another alias to an existing host identity, using hostName
	my @update_identities = (
	       {
	             "hostName"  => "ahost1",
	             "hostNames" => [ "another_alias" ]
	       }
	);
	$rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) ;

	# Update host identities - add another alias to an existing host identity, using hostIdentityId
	my @update_identities = (
	       {
	             "hostIdentityId" => $id,
	             "hostName"  => "ahost1",
	             "hostNames" => [ "yet_another_alias" ]
	       }
	);
	$rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) ;

	# Update host identities - change the hostName of the identity
	my @update_identities = (
	       {
	             "hostIdentityId" => $hostEntityId,
	             "hostName"  => "a_different_hostName",
	             "hostNames" => [ "and_yet_another_alias" ]
	       }
	);
	$rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) ;

    CUSTOMGROUPS

	Add/Update customgroups ...

	my @upsert = (    {
                            "name" => "group1",
                            "appType" => "NAGIOS"
                          },
                          {
                            "name" => "group3"
                          }
	   	     );

	$status = $rest_api->upsert_customgroups(  \@upsert, {}, \%outcome, \@results );

	Note that upserting will set things to what you upsert to. Use add_customgroups_members and
	delete_customgroups_members to change things (ie POST vs PUT respectively)

	Eg add hostgroup 'hg' to customgroup 'group1'

	my @members =  (
            {
                "name" => "group1",
                "hostGroupNames" => [ "hg" ]
            }
	);
	$status = $rest_api->add_customgroups_members( \@members, {}, \%outcome, \@results );


    SERVICES

	# Create and/or update host services
	$status = $rest_api->upsert_services( \@services, \%options, \%outcome, \@results );

	SPECIAL NOTE:  The upsert_services() call is most often used in high-traffic code
	paths, where end-to-end delays in waiting for final execution results could result in
	substantial application slowdown and consequent latency in overall status updates.
	For that reason, this call operates by default in an asynchronous mode, whereby the
	upsert request is accepted and queued immediately, and the call returns with @results
	reflecting that fact, while execution of the requests is deferred until the server can
	get to this work.  This strategy allows the client application to work more readily in
	parallel with the server, preparing more changes to be submitted.  Note, however, that the
	returned @results therefore do not reflect the final execution status of these requests.
	If your application needs to control the nature of the returned results, it can do so
	via the "async" option, which can be specified in the %options hash with a string value
	(not a boolean value) of either 'true' or 'false'.  See the example below.

    Example:

	my @services = (
	    {
		'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
		'deviceIdentification' => '127.0.0.1',                   # localhost
		'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
		'lastHardState'        => 'PENDING',
		'monitorStatus'        => 'OK',
		'description'          => $servicename,
		'properties'           => {
		    'Latency'          => '950',
		    'ExecutionTime'    => '7',
		    'MaxAttempts'      => '3',
		    'LastPluginOutput' => 'Message from service'
		},
		'stateType'       => 'HARD',
		'hostName'        => 'localhost',
		'appType'         => 'NAGIOS',
		'monitorServer'   => 'localhost',
		'checkType'       => 'ACTIVE',
		'lastStateChange' => '2013-05-22T09:36:47-07:00'
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_services( \@services, { async => 'true' }, \%outcome, \@results ) ) {
	    ## Failed.
	}

    SERVICEGROUPS

	# Create and/or update servicegroups
	$status = $rest_api->upsert_servicegroups( \@servicegroups, \%options, \%outcome, \@results );

	# FIX MAJOR:  Show here how an agentId string is generally created.

    Example:

	# This simple example just shows one servicegroup being passed to the call, but you may
	# extend @servicegroups with additional servicegroup definitions.
	#
	# The description, appType, and agentId fields are required for servicegroup creation but
	# not for servicegroup updates.  The services field is not required as such for either
	# create or update operations, but if specified, it completely replaces the current list of
	# host services in the servicegroup (this setting is treated as full, not incremental).
	my $app_type = 'NAGIOS';
	my $agent_id = '60ebc266-2bb0-4e11-0bda-2e4a6f6fbfd7';
	my @servicegroups = (
	    {
		name        => $servicegroup_name,
		description => "$servicegroup_name is for testing",
		appType     => $app_type,
		agentId     => $agent_id,
		services    => [
		    { host => 'localhost',  service => 'local_cpu_nagios' },
		    { host => 'childhost1', service => 'cpu_nagios' },
		    { host => 'childhost2', service => 'cpu_nagios' },
		    { host => 'childhost3', service => 'cpu_nagios' }
		]
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_servicegroups( \@servicegroups, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    EVENTS

	By design, events are not upserted but rather created by one method (which POSTs)
	and updated by another method (which PUTs).

	# Creates event(s)
	$status = $rest_api->create_events( \@events, \%options, \%outcome, \@results );

	Note that events are special in that they undergo consolidation within Foundation.
	So you might submit several @events and get back corresponding @results that all
	point to the same event ID within Foundation for all of the submitted events, if
	Foundation decided that they were all just successive instances of the "same" event.

	# FIX MAJOR
	# The update_events() call in its present implementation is discouraged, and might
	# be substantially modified or disappear.  Stay tuned.
	#
	# Update a list of events where:
	    \@eventids - is a reference to an array containing event IDs
	    \%options - is a reference to a hash containing properties that can be updated
	$status = $rest_api->update_events( \@eventids, \%options, \%outcome, \@results );

	# Acknowledge (currently open?) events.  These are all the pattern fields needed
	# to identify host or host-service events to acknowledge.
	my @ack_patterns = (
	    {
		appType            => 'NAGIOS',
		hostName           => 'localhost',
		serviceDescription => 'local_disk_root',
		acknowledgedBy     => 'admin',
		acknowledgeComment => 'Got rid of excess files.'
	    },
	    {
		appType            => 'VEMA',
		hostName           => 'cloudhubhost',
		acknowledgedBy     => 'operator',
		acknowledgeComment => 'Will bring this host back online when I get a chance.'
	    }
	);
	$status = $rest_api->ack_events( \@ack_patterns, \%options, \%outcome, \@results );

	# Unacknowledge (what type of?) events.  These are all the pattern fields needed
	# to identify host or host-service events to unacknowledge.
	my @unack_patterns = (
	    {
		appType            => 'NAGIOS',
		hostName           => 'localhost',
		serviceDescription => 'local_disk_root'
	    },
	    {
		appType  => 'VEMA',
		hostName => 'cloudhubhost'
	    }
	);
	$status = $rest_api->unack_events( \@unack_patterns, \%options, \%outcome, \@results );

    IMPORTANT NOTE

	There is currently a limitation with the underlying Foundation Java API that prevents
	events of application types NAGIOS and SYSTEM from being updated, so don't be surprised
	if trying to update those types of events doesn't work.

    Examples:

	my @events = (
	    {
		'consolidationName' => 'NAGIOSEVENT',
		'device'            => '127.0.0.1',
		'monitorStatus'     => 'UP',
		'service'           => 'local_load',
		'properties'    => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
		'host'          => 'localhost',
		'appType'       => 'NAGIOS',
		'textMessage'   => $eventmessage,
		'monitorServer' => 'localhost',
		'severity'      => 'SERIOUS',
		'reportDate'    => '2013-06-02T10:55:32.943'
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->create_events( \@events, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

	%params = ( "opStatus" => "CLOSED", "updatedBy" => "admin", "comments" => "Problem solved" );

	@eventids = ( 1, 2, 3 );

	if ( not $rest_api->update_events( \@eventids, \%params, \%outcome, \@results ) ) {
	    ## Failed.
	}

    PERFORMANCE DATA

	Performance data handling is a one-way ticket:  you can send such data into
	Foundation, but it is not directly retrievable through this API.

	# Creates performance data records.
	$status = $rest_api->create_performance_data( \@perfdata, \%options, \%outcome, \@results );

	Performance data is in general generated in large amounts, and it is advisable to
	send it to Foundation in large bundles, for the sake of amortizing the overhead
	across many data points.

    Examples:

	my @perfdata = (
	    {
		appType     => 'NAGIOS',
		serverName  => 'my-compute-server',
		serviceName => 'cpu_utilization',
		serverTime  => 1412377457,
		label       => 'cpu_utilization_percent',
		value       => 57,
		warning     => 90,
		critical    => 95
	    },
	    {
		appType     => 'VEMA',
		serverName  => 'my-db-server',
		serviceName => 'icmp_ping',
		serverTime  => 1412377465,
		label       => 'icmp_ping_ResponseTime',
		value       => 22,
		warning     => 80,
		critical    => 120
	    }
	);

	my %outcome;
	my @results;

	if ( not $rest_api->create_performance_data( \@perfdata, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    AUDIT-LOG DATA

	Audit-log data for significant events can be sent to Foundation to be permanently recorded there.

	# Create audit-log records.
	$status = $rest_api->create_auditlogs( \@auditlogs, \%options, \%outcome, \@results );

	Any number of audit-log entries can be sent in a single call.

	SPECIAL NOTE:  The create_auditlogs() call might be used (sparingly, as summary data)
	within high-traffic code paths, where end-to-end delays in waiting for final execution
	results could result in substantial application slowdown and consequent latency in
	overall updates.  For that reason, this call operates by default in an asynchronous mode,
	whereby the create request is accepted and queued immediately, and the call returns
	with @results reflecting that fact, while execution of the requests is deferred until
	the server can get to this work.  This strategy allows the client application to work
	more readily in parallel with the server, preparing more changes to be submitted.  Note,
	however, that the returned @results therefore do not reflect the final execution status of
	these requests.  If your application needs to control the nature of the returned results,
	it can do so via the "async" option, which can be specified in the %options hash with
	a string value (not a boolean value) of either 'true' or 'false'.  See the example below.

	If you create auditlog entries asynchronously, you will only get one @result entry back,
	representing the status of the queueing of the entire batch even if that batch consists
	of more than one auditlog entry.

    Examples:

	# The "action" in the entry is typically 'ADD', 'DELETE', or 'MODIFY'.
	# See the RESTful Services doc for the full set of allowed values.
	my @auditlogs = (
	    {
		subsystem   => 'Monarch',
		hostName    => 'my-groundwork-server',
		action      => 'SYNC',
		description => 'Commit operation was invoked.',
		username    => $username
	    },
	    {
		subsystem          => 'Cacti Feeder',
		hostName           => 'my-db-server',
		serviceDescription => 'icmp_ping',
		action             => 'ADD',
		description        => 'Added service icmp_ping to host my-db-server.',
		username           => $username
	    }
	);

	my %outcome;
	my @results;

	# Create new entries asynchronously; status, outcome, and results reflect queueing, not final execution.
	if ( not $rest_api->create_auditlogs( \@auditlogs, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

	# Create new entries, but do so synchronously so the status, outcome, and results reflect the actual execution.
	if ( not $rest_api->create_auditlogs( \@auditlogs, { async => 'false' }, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST NOTIFICATIONS

	# Creates host notification(s)
	$status = $rest_api->create_noma_host_notifications( \@host_notifications, \%options, \%outcome, \@results );

    Example:

	# FIX MAJOR:  The name and value of shortDateTime are subject to revision.  Stay tuned.
	my @host_notifications = (
	    {
		'hostName'            => 'localhost',
		'hostAddress'         => '127.0.0.1',
		'hostState'           => 'UP',
		'notificationType'    => 'PROBLEM',
		'hostOutput'          => $output_message,
		'hostGroupNames'      => 'Linux Servers',
		'notificationComment' => 'more rants and ramblings',
		'shortDateTime'       => '2013-06-02T10:55:32.943'
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->create_noma_host_notifications( \@host_notifications, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}


    SERVICE NOTIFICATIONS

	# Creates host notification(s)
	$status = $rest_api->create_noma_service_notifications( \@service_notifications, \%options, \%outcome, \@results );

    Example:

	# FIX MAJOR:  The name and value of shortDateTime are subject to revision.  Stay tuned.
	my @service_notifications = (
	    {
		'hostName'            => $host,
		'hostAddress'         => '127.0.0.1',
		'serviceDescription'  => $service,
		'serviceState'        => 'OK',
		'notificationType'    => 'PROBLEM',
		'serviceOutput'       => $output_message,
		'hostGroupNames'      => 'Linux Servers',
		'serviceGroupNames'   => 'My Service Group',
		'notificationComment' => 'more rants and ramblings',
		'shortDateTime'       => '2013-06-02T10:55:32.943'
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->create_noma_service_notifications( \@service_notifications, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    DEVICES

	# Create and/or update device(s)
	$status = $rest_api->upsert_devices( \@devices, \%options, \%outcome, \@results );

    Example:

	my @devices = (
	    {
		'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
		'identification' => '10.11.12.13',
		'displayName'    => $devicename,
		'description'    => "GW::RAPID test device"
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_devices( \@devices, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    CATEGORIES

	# Create and/or update categories
	$status = $rest_api->upsert_categories( \@categories, $options, \%outcome, \@results );

    Example:

	my @categories = (
	    {
		"name"           => $category_name,
		"description"    => "$category_name description",
		"entityTypeName" => "SERVICE_GROUP"
	    }
	);
	my %outcome;
	my @results;

	if ( not $rest_api->upsert_categories( \@categories, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    BIZ DOWNTIME

	Increment the scheduledDowntimeDepth value of hosts/servies. A value of 0 indicates
	the object is not in a scheduled downtime.

	For example, increment scheduled downtime depth for all hosts in hostgroup "some_hostgroup" :

	my $dt = {
            "hostNames" => [ "*" ],
            "hostGroupNames" => [ "some_hostgroup" ]
        };
	$rest_api->set_indowntime( $dt, {}, \%outcome, \@results );

	See the GroundWork documentation -> Developer Reference -> RESTful Services for more details.

=back

=head2 Object Deletion Routines

These functions are used for deleting objects such as hosts, hostgroups etc.
They mostly follow a similar invocation pattern:

    $status = $rest_api->delete_xyz( \@objects, \%options, \%outcome, \@results )

Where

    \@objects - a reference to a Perl array of object identifiers. This data structure
		gets converted to a JSON object and POST'ed to the REST API.
    \%options - a set of routine-specific parameters to adjust the nature of the call
    \%outcome - detailed information about the call execution status
    \@results - an array reference for the REST API response (i.e., result or errors),
		decoded from JSON back into a Perl structure.

Return Values

    Same logic as the "Create and Update (a.k.a. 'Upsert') Routines" section above.

The functions are described as follows:

=over

    APPLICATION TYPES

	# Delete application types
	$status = $rest_api->delete_application_types( \@app_type_names, \%options, \%outcome, \@results );

    Example:

	my @app_type_names = ( $apptype1, $apptype1 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_application_types( \@app_type_names, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOSTS

	# Delete hosts
	$status = $rest_api->delete_hosts( \@hostnames, \%options, \%outcome, \@results );

    Example:

	my @hostnames = ( $hostname1, $hostname2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOSTGROUPS

	# Deletes hostgroup(s)
	$status = $rest_api->delete_hostgroups( \@hostgroupnames, \%options, \%outcome, \@results );

    Example:

	my @hostgroupnames = ( $hg1, $hg2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST BLACKLISTTS

	# Deletes hostblacklists
	$status = $rest_api->delete_hostblacklists( \@hostgroupnames, \%options, \%outcome, \@results );

    Example:

	my @hostgroupnames = ( $hg1, $hg2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST IDENTITIES

	# Delete host identities by hostname list
	$rest_api->delete_hostidentities( \@hostnames, { }, \%outcome, \@results)

	# Delete host identities by hostEntityId list
	$rest_api->delete_hostidentities( \@entities_id_list, { }, \%outcome, \@results)
	Where @entities_id_list is a list of hostEntityId uuid's

	(See also clear_hostidentities() below.)

    CUSTOMGROUPS

	# Delete some customgroups completely
	$status = $rest_api->delete_customgroups( [ "group1", "group3" ], {}, \%outcome, \@results );

	Note if you want to just remove some members then use delete_customgroups_members() , for example :

	my @members =  (
            {
                "name" => "group1",
                "hostGroupNames" => [ "hg" ]
            }
        );

	$status = $rest_api->delete_customgroups_members( \@members, {}, \%outcome, \@results );

    SERVICES

	# Delete service(s) for host(s)
	$status = $rest_api->delete_services( \@servicenames, \%options, \%outcome, \@results );

    Example:

	my @services = ( $service1, $service2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_services( \@servicenamess, { hostname => $hostname }, \%outcome, \@results ) ) {
	    ## Failed.
	}

    SERVICEGROUPS

	# Deletes servicegroup(s)
	$status = $rest_api->delete_servicegroups( \@servicegroupnames, \%options, \%outcome, \@results );

    Example:

	my @servicegroupnames = ( $sg1, $sg2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_servicegroups( \@servicegroupnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    EVENTS

	# Delete events
	$status = $rest_api->delete_events( \@events, \%options, \%outcome, \@results );

    Example:

	my @eventids = ( $eventid1, $eventid2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_events( \@eventids, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    DEVICES

	# Delete devices
	$status = $rest_api->delete_devices( \@devices, \%options, \%outcome, \@results );

    Example:

	my @devices = ( $device1, $device2 );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_devices( \@devices, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    CATEGORIES

	# Delete categories
	$status =$rest_api->delete_categories( \@categories, \%options, \%outcome, \@results );

    Example:

	my @categories = ( "service_group_1", "service_group_2" );
	my %outcome;
	my @results;

	if ( not $rest_api->delete_categories( \@categories, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

=back

=head2 Other Routines

These functions are used for variery of other operations.
All of these functions return 1 on success, 0 otherwise.

The functions are described as follows:

=over

    HOSTGROUPS

	# Remove all members from a hostgroup
	$status = $rest_api->clear_hostgroups( \@hostgroupnames, \%options, \%outcome, \@results );

    Return Values

	Logic is the same as 'Deletion Routines' section above.

    Example:

	my @hostgroupnames = ( $hg1, $hg2 );
	my %outcome;
	my @results;

	if ( not $rest_api->clear_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

    HOST IDENTITIES

	# Remove all aliases from a hostidentity
	$status = $rest_api->clear_hostidentities( \@hostidentities_list, \%options, \%outcome, \@results );
	Where @hostidentities_list is a list of hostEntityId uuid's.

    SERVICEGROUPS

	# Remove all members from a servicegroup
	$status = $rest_api->clear_servicegroups( \@servicegroupnames, \%options, \%outcome, \@results );

    DOWNTIMES

	Decrement scheduledDowntimeDepth by 1 (down to no less than 0). For example :

	my %api_instructions =
	{
            "bizHostServiceInDowntimes" => [
                {
		    "entityName" => "test_group1",
		    "entityType" => "HOSTGROUP",
		    "hostName" => "ahost1"
                }
            ]
        };
	$rest_api->clear_indowntime( \%api_instructions, { }, \%outcome, \@results )

	See the GroundWork documentation -> Developer Reference -> RESTful Services for more details.

    Return Values

	Logic is the same as 'Deletion Routines' section above.

    Example:

	my @servicegroupnames = ( $sg1, $sg2 );
	my %outcome;
	my @results;

	if ( not $rest_api->clear_servicegroups( \@servicegroupnames, {}, \%outcome, \@results ) ) {
	    ## Failed.
	}

=back

=head1 SECURITY

When calling new(), it is not recommended that you store the username and password directly in your script.
Instead, read them from some protected resource.

At a minimum, the username and password are passed to the REST API using MIME base64 encoding.
See http://perldoc.perl.org/MIME/Base64.html for details.
Encryption is generally used instead when the credentials are stored in the ws_client.properties file.

=head1 LOGGING

# FIX MAJOR:  Revise this to reflect reality.
GW::RAPID uses Log::Log4perl for all logging. The logging configuration file is
/usr/local/groundwork/config/GW_RAPID.log4perl.conf .
Out of the box, logging is sent to: /usr/local/groundwork/logs/GW_RAPID.log
See http://search.cpan.org/~mschilli/Log-Log4perl-1.42/lib/Log/Log4perl.pm for details of log4perl.
If you're developing using GW::RAPID, you might want to modify the logging configuration to send
logging output to the terminal too, for the duration of development (but disable it for production!).
There is a line in the sample config file that can be used for that.

=head1 ERRORS

Errors, warnings, etc. are treated as exceptions and logged, including Perl compiler warnings.
Exceptions are sometimes generated within the package for easier logic control,
but they are all caught inside the package and returned to the caller as false status values.
Be sure to check the status of every routine you call!

=head1 PERL MODULE DEPENDENCIES

GW::RAPID depends upon the following Perl modules shipped with GroundWork Perl:

    - HTTP::Status
    - JSON
    - JSON::XS      (used for single-threaded Perl code)
    - JSON::PP      (required instead for multi-threaded Perl code)
    - MIME::Base64
    - REST::Client
    - URI::Escape
    - Carp
    - Data::Dumper
    - List::MoreUtils
    - TypedConfig

GW::RAPID depends upon some Perl modules that are not shipped with GroundWork 7.0.1
(but are with 7.0.2).  For convenience under 7.0.0 and 7.0.1, these are installed into
/usr/local/groundwork/core/foundation/api/perl/lib by the RAPID package installation script.
(Note that RAPID is an early prototype of GW::RAPID; the two packages are similar in function
but have much different calling sequences.)

    - Log::Log4perl

=head1 FILES

    # This is a sample GW::RAPID log4perl configuration, but NOT one that
    # your application code should depend on.  BUILD YOUR OWN CONFIG FILE,
    # one that meets the total logging needs of your entire application,
    # not just the logging needs of this one package.
    /usr/local/groundwork/config/GW_RAPID.log4perl.conf

    # BAD IDEA:  DO NOT put your own application logfiles in the
    # /usr/local/groundwork/logs/ directory!  That directory is intended to be populated
    # ONLY by symlinks to log files elsewhere in the system, where they actually belong.
    /usr/local/groundwork/logs/GW_RAPID.log

    # Where early RAPID and additional Perl modules were placed.  No longer relevant
    # starting with GWMEE 7.0.2, which bundles in the necessary modules.
    /usr/local/groundwork/core/foundation/api/perl/lib

=head1 TODO

# FIX MAJOR:  Move this up and out of here.
The REST API needs enhancing in the following ways:

    - return certain plain-string responses in JSON structures instead
    - fix event retrieval to produce error/warning if one of the events in the list fails to retrieve

The underlying Foundation REST API requires modification to allow events of appType NAGIOS and SYSTEM to be updatable.
FIX MAJOR:  Check to see if that has been done in the rewrite of the /api/events calls.

We need to test cases of when there are NO hosts, NO hostgroups, etc. -- the API responses appear to be different.
We need to flesh out t/internals.t.

=head1 BUGS

While this project was developed using a test-driven approach, bugs probably remain.
Many cases are not yet covered by tests, including particularly many combinations of objects and options.
If you find a bug, or want to submit a feature request, please open a case with GroundWork.

=head1 SEE ALSO

More information on GroundWork products and services can be found at: http://www.gwos.com

=head1 ORIGINAL AUTHOR

Dominic Nicholas  dnicholas@gwos.com

=head1 COPYRIGHT AND LICENSE

Copyright (c) 2013-2018 GroundWork Open Source, Inc.;
http://www.groundworkopensource.com

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.

