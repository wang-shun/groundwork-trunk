package Replication::Application;

# Application functions for a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# This package contains routines that encapsulate database access
# activities needed by our Replication software.

# To do:
# * create lists of patterns suitable for passing to the selective_copy
#   script from the configuration data application patterns for including
#   and excluding trees and files

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &app_replication_patterns
);

our @EXPORT_OK = qw(
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Global configuration variables.
# ================================================================

# ================================================================
# Global working variables.
# ================================================================

# ================================================================
# Supporting subroutines.
# ================================================================

# Make a list of included/excluded trees and files for replication,
# given the config-file specification data.
sub app_replication_patterns {
  my $application_name = shift;
  my $include_trees    = shift;
  my $exclude_trees    = shift;
  my $include_files    = shift;
  my $exclude_files    = shift;

  # Protect the caller from whatever implicit changes we make to this otherwise global variable.
  local $_;

  # Some of the provided inclusion or exclusion specifications may be missing.
  # (Either include-trees or include-files must be present, but that will be checked outside of this routine.)
  $include_trees = [ ] if (not defined $include_trees);
  $exclude_trees = [ ] if (not defined $exclude_trees);
  $include_files = [ ] if (not defined $include_files);
  $exclude_files = [ ] if (not defined $exclude_files);

  # Each of the provided inclusion or exclusion specifications may be either a scalar (string) or an arrayref.
  $include_trees = [ $include_trees ] if (not ref $include_trees);
  $exclude_trees = [ $exclude_trees ] if (not ref $exclude_trees);
  $include_files = [ $include_files ] if (not ref $include_files);
  $exclude_files = [ $exclude_files ] if (not ref $exclude_files);

  my $got_bad_config = 0;
  if (ref($include_trees) ne 'ARRAY') {
      log_timed_message "FATAL:  Application \"$application_name\" include-trees is improperly specified; must be string or array.";
      $got_bad_config = 1;
  }
  if (ref($exclude_trees) ne 'ARRAY') {
      log_timed_message "FATAL:  Application \"$application_name\" exclude-trees is improperly specified; must be string or array.";
      $got_bad_config = 1;
  }
  if (ref($include_files) ne 'ARRAY') {
      log_timed_message "FATAL:  Application \"$application_name\" include-files is improperly specified; must be string or array.";
      $got_bad_config = 1;
  }
  if (ref($exclude_files) ne 'ARRAY') {
      log_timed_message "FATAL:  Application \"$application_name\" exclude-files is improperly specified; must be string or array.";
      $got_bad_config = 1;
  }
  return undef if $got_bad_config;

  # Handle trailing slashes:  make sure they're present for trees, and (for the time being, at least) not present for files.
  foreach (@{ $include_trees }) {
      if (m{^\s*$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" include-trees lists an empty element.";
	  $got_bad_config = 1;
      }
      if (m{^[^/]}) {
	  log_timed_message "FATAL:  Application \"$application_name\" include-trees lists a relative path (all paths must be absolute).";
	  $got_bad_config = 1;
      }
      # Declare a directory as such by ensuring that the spec ends with a slash.  This helps downstream processing.
      s{([^/])$}{$1/};
  }
  foreach (@{ $exclude_trees }) {
      if (m{^\s*$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" exclude-trees lists an empty element.";
	  $got_bad_config = 1;
      }
      if (m{^[^/]}) {
	  log_timed_message "FATAL:  Application \"$application_name\" exclude-trees lists a relative path (all paths must be absolute).";
	  $got_bad_config = 1;
      }
      # Declare a directory as such by ensuring that the spec ends with a slash.  This helps downstream processing.
      s{([^/])$}{$1/};
  }
  foreach (@{ $include_files }) {
      if (m{^\s*$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" include-files lists an empty element.";
	  $got_bad_config = 1;
      }
      if (m{^[^/]}) {
	  log_timed_message "FATAL:  Application \"$application_name\" include-files lists a relative path (all paths must be absolute).";
	  $got_bad_config = 1;
      }
      if (m{/$} || m{/\.$} || m{/\.\.$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" include-files lists a directory, not just files.";
	  $got_bad_config = 1;
      }
  }
  foreach (@{ $exclude_files }) {
      if (m{^\s*$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" exclude-files lists an empty element.";
	  $got_bad_config = 1;
      }
      if (m{^[^/]}) {
	  log_timed_message "FATAL:  Application \"$application_name\" exclude-files lists a relative path (all paths must be absolute).";
	  $got_bad_config = 1;
      }
      if (m{/$} || m{/\.$} || m{/\.\.$}) {
	  log_timed_message "FATAL:  Application \"$application_name\" exclude-files lists a directory, not just files.";
	  $got_bad_config = 1;
      }
  }
  return undef if $got_bad_config;

  my @replication_patterns = ();
  push @replication_patterns, map { "+$_" } @$include_trees;
  push @replication_patterns, map { "-$_" } @$exclude_trees;
  push @replication_patterns, map { "+$_" } @$include_files;
  push @replication_patterns, map { "-$_" } @$exclude_files;
  return \@replication_patterns;
}

1;
