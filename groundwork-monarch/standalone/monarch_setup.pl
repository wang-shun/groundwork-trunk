#!/usr/bin/perl --
# MonArch - Groundwork Monitor Architect
# monarch_setup.pl
#
###############################################################################
# Release 2.5
# 4-Apr-2008
###############################################################################
# Author: Scott Parris
#
# Copyright 2008 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

use DBI;
use strict;
use File::Copy;

my $version = '2.5';
my ( $dsn, $user, $passwd ) = 0;
my $drop_tables = $ARGV[0];

###########################################
# Begin Edit
#

my $database       = "monarch";
my $databaseuser   = "root";
my $databasepasswd = "";
my $databasehost   = 'localhost';
my $cgi_dir        = "/usr/local/apache2/cgi-bin";
my $doc_root       = "/usr/local/apache2/htdocs";
my $monarch_home   = "/usr/local/groundwork/monarch";
my $cgi_path       = 0;
my $nagios_etc     = "/usr/local/nagios/etc";
my $nagios_bin     = "/usr/sbin";
my $web_group      = "www";
my $web_user       = "wwwrun";
if ( -e '/etc/suseRegister.conf' ) {
    $cgi_dir   = "/srv/www/cgi-bin";
    $doc_root  = "/srv/www/htdocs";
    $web_group = "www";
    $web_user  = "wwwrun";
}
elsif ( -e '/etc/redhat-release' ) {
    $cgi_dir   = "/var/www/cgi-bin";
    $doc_root  = "/var/www/html";
    $web_group = "apache";
    $web_user  = "apache";
}
elsif ( -d '/etc/mach-init.d' ) {

    # OS X Tiger
    # There are no plans to support this atm. This is here only as a
    # developer convenience, and it doesn't even work as specified
    # here, because additional configuration is required. Should
    # put monarch code in doc_root/monarch and cgi code in
    # doc_root/monarch/cgi-bin, then in httpd.conf ScriptAlias
    # /monarch/ to be the monarch directory, and /cgi-bin/ to be
    # the monarch/cgi-bin directory.
    $cgi_dir   = "/Library/Webserver/CGI-Executables";
    $doc_root  = "/Library/Webserver/Documents";
    $web_group = "wheel";
    $web_user  = "www";
}

#
# end edit section
#############################################

my $set_perms  = 0;
my $is_portal  = 0;
my $nagios_ver = '2.x';
my $error      = 0;

sub check_prereqs() {
    my @missing_reqs = ();
    my $errors       = qx('./check_mods.pl');
    if ($errors) {
        push @missing_reqs, $errors;
        push @missing_reqs,
          "Missing perl module requirements. See README for list";
        push @missing_reqs, "To check installed modules do:";
        push @missing_reqs,
"perl -MFile::Find=find -MFile::Spec::Functions -lwe  'find { wanted => sub { print canonpath $_ if /\.pm\z/ }, no_chdir => 1 }, \@INC'";
    }
    my $ver = qx(mysql -V);
    unless ( $ver =~ /Distrib 5|Distrib 6/i ) {
        print "\n$ver";
        print
"\nWarning: MySQL ver 5.0 or higher required (ignore if database is remote).\n\n";
    }

    if (@missing_reqs) {
        print
"\nPrerequisite check failed (see README.txt). Please correct the following:\n";
        foreach (@missing_reqs) {
            print "\n\t$_\n";
        }
        exit;
    }
}

#
##############################################################################
# Drop Tables
##############################################################################
#
sub drop_tables() {
    my $dsn = "DBI:mysql:$database:$databasehost";
    my $dbh =
      DBI->connect( $dsn, $databaseuser, $databasepasswd,
        { 'RaiseError' => 1 } );

    # Associative tables
    eval { $dbh->do("DROP TABLE IF EXISTS external_host_profile") };
    print "\ndrop external_host_profile failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS profile_hostgroup") };
    print "\ndrop profile_hostgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS profile_parent") };
    print "\ndrop profile_parent failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS profile_host_profile_service") };
    print "\ndrop profile_host_profile_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS serviceprofile_hostgroup") };
    print "\ndrop serviceprofile_hostgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS serviceprofile_host") };
    print "\ndrop serviceprofile_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS serviceprofile") };
    print "\ndrop serviceprofile failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS escalation_tree_template") };
    print "\ndrop escalation_tree_template failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS tree_template_contactgroup") };
    print "\ndrop tree_template_contactgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS hostprofile_overrides") };
    print "\ndrop hostprofile_overrides failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS servicename_overrides") };
    print "\ndrop servicename_overrides failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS service_instance") };
    print "\ndrop service_instance failed: $@\n" if $@;

    # Externals
    eval { $dbh->do("DROP TABLE IF EXISTS external_host") };
    print "\ndrop external_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS external_service") };
    print "\ndrop external_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS external_service_names") };
    print "\ndrop external_service_names failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS externals") };
    print "\ndrop externals failed: $@\n" if $@;

    # Object req broker
    eval { $dbh->do("DROP TABLE IF EXISTS object_request_brokers") };
    print "\ndrop object_request_brokers failed: $@\n" if $@;

    # discover / import

    eval { $dbh->do("DROP TABLE IF EXISTS discover_group_filter") };
    print "\ndrop discover_group_filter failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS discover_group_method") };
    print "\ndrop discover_method_filter failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS discover_method_filter") };
    print "\ndrop discover_method_filter failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS discover_method") };
    print "\ndrop discover_method failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS discover_filter") };
    print "\ndrop discover_filter failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_servicename") };
    print "\ndrop import_match_servicename failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_serviceprofile") };
    print "\ndrop import_match_serviceprofile failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_contactgroup") };
    print "\ndrop import_match_contactgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_group") };
    print "\ndrop import_match_group failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_hostgroup") };
    print "\ndrop import_match_hostgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match_parent") };
    print "\ndrop import_match_parent failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_match") };
    print "\ndrop import_match failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_column") };
    print "\ndrop import_column failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_schema") };
    print "\ndrop import_schema failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS discover_group") };
    print "\ndrop discover_group failed: $@\n" if $@;

    # Groups
    eval { $dbh->do("DROP TABLE IF EXISTS monarch_group_props") };
    print "\ndrop monarch_group_props failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_group_host") };
    print "\ndrop monarch_group_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_group_hostgroup") };
    print "\ndrop monarch_group_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_group_child") };
    print "\ndrop monarch_group_child failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_group_macro") };
    print "\ndrop monarch_group_macro failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_macros") };
    print "\ndrop monarch_macros failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS monarch_groups") };
    print "\ndrop monarch_groups failed: $@\n" if $@;

    # contacts
    eval { $dbh->do("DROP TABLE IF EXISTS contact_command") };
    print "\ndrop contact_command failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_contact") };
    print "\ndrop contactgroup_contact failed: $@\n" if $@;

    # old table, pre-monarch 2.1 / pre-gwmon 5.2
    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_assign") };
    print "\ndrop contactgroup_assign failed: $@\n" if $@;

    # begin new tables split out from contactgroup_assign
    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_host") };
    print "\ndrop contactgroup_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_service") };
    print "\ndrop contactgroup_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_host_template") };
    print "\ndrop contactgroup_host_template failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_service_template") };
    print "\ndrop contactgroup_service_template failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_host_profile") };
    print "\ndrop contactgroup_host_profile failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_service_name") };
    print "\ndrop contactgroup_service_name failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_hostgroup") };
    print "\ndrop contactgroup_hostgroup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroup_group") };
    print "\ndrop contactgroup_group failed: $@\n" if $@;
    # end new tables split out from contactgroup_assign

    eval { $dbh->do("DROP TABLE IF EXISTS contact_overrides") };
    print "\ndrop contacts failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contact_command_overrides") };
    print "\ndrop contact_command_overrides failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contact_templates") };
    print "\ndrop contacts failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contactgroups") };
    print "\ndrop contact_groups failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS contacts") };
    print "\ndrop contacts failed: $@\n" if $@;

    # Services
    eval { $dbh->do("DROP TABLE IF EXISTS service_overrides") };
    print "\ndrop service_overrides failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS servicegroup_service") };
    print "\ndrop servicegroup_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS servicegroups") };
    print "\ndrop servicegroups failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS service_dependency") };
    print "\ndrop service_dependency failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS servicename_dependency") };
    print "\ndrop servicename_dependency failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS service_dependency_templates") };
    print "\ndrop service_dependency_templates failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS services") };
    print "\ndrop services failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS service_templates") };
    print "\ndrop service_templates failed: $@\n" if $@;

    # Hosts
    eval { $dbh->do("DROP TABLE IF EXISTS host_overrides") };
    print "\ndrop host_overrides failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS extended_info_coords") };
    print "\ndrop extended_info_coords failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS hostgroup_host") };
    print "\ndrop hostgroup_host failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS host_dependencies") };
    print "\ndrop host_dependencies failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS host_parent") };
    print "\ndrop host_parent failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS hostgroups") };
    print "\ndrop hostgroups failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS host_templates") };
    print "\ndrop host_templates failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS hosts") };
    print "\ndrop hosts failed: $@\n" if $@;

    # Escalations

    eval { $dbh->do("DROP TABLE IF EXISTS escalation_templates") };
    print "\ndrop escalation_templates failed: $@\n" if $@;

    # Profiles

    eval { $dbh->do("DROP TABLE IF EXISTS service_names") };
    print "\ndrop service_names failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS profiles_service") };
    print "\ndrop profiles_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS profiles_host") };
    print "\ndrop profiles_host failed: $@\n" if $@;

    # And the rest...

    eval { $dbh->do("DROP TABLE IF EXISTS extended_host_info_templates") };
    print "\ndrop extended_host_info_templates failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS extended_service_info_templates") };
    print "\ndrop extended_service_info_templates failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS setup") };
    print "\ndrop setup failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS user_group") };
    print "\ndrop user_group failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS access_list") };
    print "\ndrop access_list failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS users") };
    print "\ndrop users failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS user_groups") };
    print "\ndrop user_groups failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS stage_hosts") };
    print "\ndrop stage_hosts failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS stage_host_services") };
    print "\ndrop stage_host_services failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS stage_host_hostgroups") };
    print "\ndrop stage_host_hostgroups failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS stage_other") };
    print "\ndrop stage_other failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_hosts") };
    print "\ndrop import_hosts failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS import_services") };
    print "\ndrop import_services failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS datatype") };
    print "\ndrop datatype failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS host_service") };
    print "\ndrop host_service failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS performanceconfig") };
    print "\ndrop performanceconfig failed: $@\n" if $@;

    eval { $dbh->do("DROP TABLE IF EXISTS escalation_trees") };
    print "\ndrop escalation_trees failed: $@\n" if $@;

    # Commands
    eval { $dbh->do("DROP TABLE IF EXISTS commands") };
    print "\ndrop check_commands failed: $@\n" if $@;

    # Time Periods
    eval { $dbh->do("DROP TABLE IF EXISTS time_periods") };
    print "\ndrop time_periods failed: $@\n" if $@;

    # session

    eval { $dbh->do("DROP TABLE IF EXISTS sessions") };
    print "\ndrop sessions failed: $@\n" if $@;

    if ($dbh) {
        $dbh and $dbh->disconnect();
    }
}

sub create_db() {
    my $dsn = "DBI:mysql:$database:$databasehost";
    my $dbh =
      DBI->connect( $dsn, $databaseuser, $databasepasswd,
        { 'RaiseError' => 1 } );
    my $sth = $dbh->prepare('show tables');
    $sth->execute;
    my @tables = ();
    while ( my @values = $sth->fetchrow_array() ) {
        push @tables, $values[0];
    }
    $sth->finish;

    if (@tables) {
        print
"\n\n\tWARNING: Database $database is already populated. All tables will\n\tbe dropped if you proceed. Continue? 'yes' or 'no' : ";
        my $resp = <STDIN>;
        if ( $resp =~ /^yes$/i ) {
            drop_tables();
        }
        else {
            exit;
        }
    }

    #
    ###########################################################################
    # Create Tables
    ###########################################################################
    # Commands
    #

    $dbh->do(
		"CREATE TABLE commands (
			command_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			type VARCHAR(50),
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Time Periods
    #

    $dbh->do(
		"CREATE TABLE time_periods (
			timeperiod_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact Templates
    #

    $dbh->do(
		"CREATE TABLE contact_templates (
			contacttemplate_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			host_notification_period SMALLINT(4) UNSIGNED,
			service_notification_period SMALLINT(4) UNSIGNED,
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact commands
    #

    $dbh->do(
        "CREATE TABLE contact_command (
        	contacttemplate_id SMALLINT(4) UNSIGNED,
			type VARCHAR(50),
			command_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contacttemplate_id,type,command_id),
			FOREIGN KEY (command_id) REFERENCES commands(command_id) ON DELETE CASCADE,
			FOREIGN KEY (contacttemplate_id) REFERENCES contact_templates(contacttemplate_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact groups
    #

    $dbh->do(
		"CREATE TABLE contactgroups (
			contactgroup_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contacts
    #

    $dbh->do(
		"CREATE TABLE contacts (
			contact_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			email TEXT,
			pager TEXT,
			contacttemplate_id SMALLINT(4) UNSIGNED,
			status TINYINT(1),
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact overrides
    #

    $dbh->do(
		"CREATE TABLE contact_overrides (
			contact_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			host_notification_period SMALLINT(4) UNSIGNED,
			service_notification_period SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE)	TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact commands overrides
    #

    $dbh->do(
		"CREATE TABLE contact_command_overrides (
			contact_id SMALLINT(4) UNSIGNED,
			type VARCHAR(50),
			command_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contact_id,type,command_id),
			FOREIGN KEY (command_id) REFERENCES commands(command_id) ON DELETE CASCADE,
			FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # contact group Members
    #

    $dbh->do(
		"CREATE TABLE contactgroup_contact (
			contactgroup_id SMALLINT(4) UNSIGNED,
			contact_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,contact_id),
			FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Escalation Templates
    #

    $dbh->do(
		"CREATE TABLE escalation_templates (
			template_id SMALLINT(4) UNSIGNED AUTO_INCREMENT,
			name VARCHAR(255) UNIQUE NOT NULL,
			type VARCHAR(50) NOT NULL,
			data TEXT,
			comment TEXT,
			escalation_period  SMALLINT(4) UNSIGNED,
			PRIMARY KEY (template_id,name,type),
			FOREIGN KEY (escalation_period) REFERENCES time_periods(timeperiod_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Escalation Trees
    #

    $dbh->do(
		"CREATE TABLE escalation_trees (
			tree_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			description VARCHAR(100),
			type VARCHAR(50) NOT NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Escalation Trees Template
    #

    $dbh->do(
        "CREATE TABLE escalation_tree_template (
        	tree_id SMALLINT(4) UNSIGNED,
			template_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (tree_id,template_id),
			FOREIGN KEY (template_id) REFERENCES escalation_templates(template_id) ON DELETE CASCADE,
			FOREIGN KEY (tree_id) REFERENCES escalation_trees(tree_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Escalation Tree Template Contactgroup
    #

    $dbh->do(
        "CREATE TABLE tree_template_contactgroup (
        	tree_id SMALLINT(4) UNSIGNED,
			template_id SMALLINT(4) UNSIGNED,
			contactgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (tree_id,template_id,contactgroup_id),
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (template_id) REFERENCES escalation_templates(template_id) ON DELETE CASCADE,
			FOREIGN KEY (tree_id) REFERENCES escalation_trees(tree_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Host Templates
    #

    $dbh->do(
		"CREATE TABLE host_templates (
			hosttemplate_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Extended Host Info Template
    #

    $dbh->do(
		"CREATE TABLE extended_host_info_templates (
			hostextinfo_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			data TEXT,
			script VARCHAR(255),
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Host Profiles
    #

    $dbh->do(
		"CREATE TABLE profiles_host (
			hostprofile_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			description VARCHAR(255),
			host_template_id SMALLINT(4) UNSIGNED,
			host_extinfo_id SMALLINT(4) UNSIGNED,
			host_escalation_id SMALLINT(4) UNSIGNED,
			service_escalation_id SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (host_extinfo_id) REFERENCES extended_host_info_templates(hostextinfo_id) ON DELETE SET NULL,
			FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL,
			FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE hostprofile_overrides (
			hostprofile_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT, 
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Hosts
    #

    $dbh->do(
        "CREATE TABLE hosts (
        	host_id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			address VARCHAR(50) NOT NULL,
			os VARCHAR(50),
			hosttemplate_id SMALLINT(4) UNSIGNED,
			hostextinfo_id SMALLINT(4) UNSIGNED,
			hostprofile_id SMALLINT(4) UNSIGNED,
			host_escalation_id SMALLINT(4) UNSIGNED,
			service_escalation_id SMALLINT(4) UNSIGNED,
			status TINYINT(1),
			comment TEXT,
			FOREIGN KEY (hostextinfo_id) REFERENCES extended_host_info_templates(hostextinfo_id) ON DELETE SET NULL,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE SET NULL,
			FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL,
			FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE host_overrides (
        	host_id INT(6) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT, 
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Host Parent Members
    #

    $dbh->do(
        "CREATE TABLE host_parent (
        	host_id INT(6) UNSIGNED,
			parent_id INT(6) UNSIGNED,
			PRIMARY KEY (host_id,parent_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (parent_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Hostgroups
    #

    $dbh->do(
		"CREATE TABLE hostgroups (
			hostgroup_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			hostprofile_id SMALLINT(4) UNSIGNED,
			host_escalation_id SMALLINT(4) UNSIGNED,
			service_escalation_id SMALLINT(4) UNSIGNED,
			status TINYINT(1),
			comment TEXT,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE SET NULL,
			FOREIGN KEY (host_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL,
			FOREIGN KEY (service_escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Hostgroup Host
    #

    $dbh->do(
        "CREATE TABLE hostgroup_host (
        	hostgroup_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (hostgroup_id,host_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Templates
    #

    $dbh->do(
		"CREATE TABLE service_templates (
			servicetemplate_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			parent_id SMALLINT(4) UNSIGNED,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			command_line TEXT,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Extended Service Info Templates
    #

    $dbh->do(
		"CREATE TABLE extended_service_info_templates (
			serviceextinfo_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			data TEXT,
			script VARCHAR(255),
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Services
    #

    $dbh->do(
		"CREATE TABLE services (
			service_id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			host_id INT(6) UNSIGNED NOT NULL,
			servicename_id SMALLINT(4) UNSIGNED NOT NULL,
			servicetemplate_id SMALLINT(4) UNSIGNED,
			serviceextinfo_id SMALLINT(4) UNSIGNED,
			escalation_id SMALLINT(4) UNSIGNED,
			status TINYINT(1),
			check_command SMALLINT(4) UNSIGNED,
			command_line TEXT,
			comment TEXT, 
			INDEX (host_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (serviceextinfo_id) REFERENCES extended_service_info_templates(serviceextinfo_id) ON DELETE SET NULL,
			FOREIGN KEY (escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Services overrides
    #

    $dbh->do(
        "CREATE TABLE service_overrides (
        	service_id INT(8) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Services instance
    #

    $dbh->do(
		"CREATE TABLE service_instance (
			instance_id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			service_id INT(8) UNSIGNED,
			name VARCHAR(255) NOT NULL,
			status TINYINT(1) DEFAULT '0',
			arguments VARCHAR(255) NOT NULL,
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Servicegroups
    #

    $dbh->do(
		"CREATE TABLE servicegroups (
			servicegroup_id INT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			alias varchar(255) NOT NULL,
			escalation_id SMALLINT(4) UNSIGNED,
			FOREIGN KEY (escalation_id) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Servicegroups Service
    #

    $dbh->do(
        "CREATE TABLE servicegroup_service (
        	servicegroup_id INT(6) UNSIGNED,
			host_id INT(6) UNSIGNED,
			service_id INT(8) UNSIGNED,
			PRIMARY KEY (servicegroup_id,host_id,service_id),
			FOREIGN KEY (servicegroup_id) REFERENCES servicegroups(servicegroup_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Extended Info Coords
    #

    $dbh->do(
        "CREATE TABLE extended_info_coords (
        	host_id INT(4) UNSIGNED PRIMARY KEY,
			data TEXT,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Dependencies
    #

    $dbh->do(
		"CREATE TABLE service_dependency (
			id INT(8) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			service_id INT(8) UNSIGNED NOT NULL,
			host_id INT(6) UNSIGNED NOT NULL,
			depend_on_host_id INT(6) UNSIGNED NOT NULL,
			template SMALLINT(4) UNSIGNED NOT NULL,
			comment TEXT,
			INDEX (service_id),
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE,
			FOREIGN KEY (depend_on_host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Dependencies Templates
    #

    $dbh->do(
		"CREATE TABLE service_dependency_templates (
			id INT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) UNIQUE NOT NULL,
			servicename_id SMALLINT(4) UNSIGNED NOT NULL,
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Host Dependencies
    #

    $dbh->do(
        "CREATE TABLE host_dependencies (
        	host_id INT(6) UNSIGNED,
			parent_id INT(6) UNSIGNED,
			data TEXT,
			comment TEXT,
			PRIMARY KEY (host_id,parent_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (parent_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Setup
    #

    $dbh->do(
        "CREATE TABLE setup (
        	name VARCHAR(255) PRIMARY KEY,
			type VARCHAR(50),
			value TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Users
    #

    $dbh->do(
		"CREATE TABLE users (
			user_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			user_acct VARCHAR(20) NOT NULL,
			user_name VARCHAR(255) NOT NULL,
			password VARCHAR(20) NOT NULL,
			session VARCHAR(255)) TYPE=INNODB"
    );

    #
    ###########################################################################
    # User groups
    #

    $dbh->do(
		"CREATE TABLE user_groups (
			usergroup_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255) NOT NULL,
			description VARCHAR(100)) TYPE=INNODB"
    );

    #
    ###########################################################################
    # User - group
    #

    $dbh->do(
        "CREATE TABLE user_group (
        	usergroup_id SMALLINT(4) UNSIGNED,
			user_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (usergroup_id,user_id),
			FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
			FOREIGN KEY (usergroup_id) REFERENCES user_groups(usergroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Access List
    #

    $dbh->do(
        "CREATE TABLE access_list (
        	object VARCHAR(50),
			type VARCHAR(50),
			usergroup_id SMALLINT(4) UNSIGNED,
			access_values VARCHAR(20),
			PRIMARY KEY (object,type,usergroup_id),
			FOREIGN KEY (usergroup_id) REFERENCES user_groups(usergroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Stage Host
    #

    $dbh->do(
        "CREATE TABLE stage_hosts (
        	name VARCHAR(255),
			user_acct VARCHAR(50),
			PRIMARY KEY (name,user_acct),
			type VARCHAR(20),
			status TINYINT(1),
			alias varchar(255),
			address VARCHAR(50),
			os VARCHAR(50),
			hostprofile VARCHAR(50),
			serviceprofile VARCHAR(50),
			info VARCHAR(50)) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Stage Host-Services
    #

    $dbh->do(
        "CREATE TABLE stage_host_services (
        	name VARCHAR(255),
			user_acct VARCHAR(50),
			host VARCHAR(50),
			PRIMARY KEY (name,user_acct,host),
			type VARCHAR(20),
			status TINYINT(1),
			service_id INT UNSIGNED) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Stage Host-Hostgroups
    #

    $dbh->do(
        "CREATE TABLE stage_host_hostgroups (
        	name VARCHAR(255),
			user_acct VARCHAR(50),
			hostgroup VARCHAR(50),
			PRIMARY KEY (name,user_acct,hostgroup)) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Stage Other
    #

    $dbh->do(
        "CREATE TABLE stage_other (
        	name VARCHAR(255),
			type VARCHAR(50),
			parent VARCHAR(255),
			PRIMARY KEY (name,type,parent),
			data TEXT,
			comment TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Names
    #

    $dbh->do(
		"CREATE TABLE service_names (
			servicename_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(250) UNIQUE NOT NULL,
			description VARCHAR(100),
			template SMALLINT(4) UNSIGNED,
			check_command SMALLINT(4) UNSIGNED,
			command_line TEXT,
			escalation SMALLINT(4) UNSIGNED,
			extinfo SMALLINT(4) UNSIGNED,
			data TEXT,
			FOREIGN KEY (extinfo) REFERENCES extended_service_info_templates(serviceextinfo_id) ON DELETE SET NULL,
			FOREIGN KEY (escalation) REFERENCES escalation_trees(tree_id) ON DELETE SET NULL) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service name overrides
    #

    $dbh->do(
		"CREATE TABLE servicename_overrides (
			servicename_id SMALLINT(4) UNSIGNED PRIMARY KEY,
			check_period SMALLINT(4) UNSIGNED,
			notification_period SMALLINT(4) UNSIGNED,
			event_handler SMALLINT(4) UNSIGNED,
			data TEXT,

			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Name Dependencies
    #

    $dbh->do(
		"CREATE TABLE servicename_dependency (
			id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			servicename_id SMALLINT(4) UNSIGNED NOT NULL,
			depend_on_host_id INT(6) UNSIGNED,
			template SMALLINT(4) UNSIGNED NOT NULL,
			INDEX (servicename_id),
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
			FOREIGN KEY (depend_on_host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Profiles
    #

    $dbh->do(
		"CREATE TABLE profiles_service (
			serviceprofile_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(250) UNIQUE NOT NULL,
			description VARCHAR(100),
			data TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################    # Service Name Service Profile
    #

    $dbh->do(
		"CREATE TABLE serviceprofile (
			servicename_id SMALLINT(4) UNSIGNED,
			serviceprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (servicename_id,serviceprofile_id),
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Profile Host Profile
    #

    $dbh->do(
		"CREATE TABLE profile_host_profile_service (
			hostprofile_id SMALLINT(4) UNSIGNED,
			serviceprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (hostprofile_id,serviceprofile_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Profile Hostgroup
    #

    $dbh->do(
		"CREATE TABLE serviceprofile_hostgroup (
			serviceprofile_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (serviceprofile_id,hostgroup_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Service Profile Host
    #

    $dbh->do(
		"CREATE TABLE serviceprofile_host (
			serviceprofile_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (serviceprofile_id,host_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Profile Hostgroup
    #

    $dbh->do(
		"CREATE TABLE profile_hostgroup (
			hostprofile_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (hostprofile_id,hostgroup_id),
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Profile Parent
    #

    $dbh->do(
        "CREATE TABLE profile_parent (
        	hostprofile_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (hostprofile_id,host_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Externals
    #

    $dbh->do(
		"CREATE TABLE externals (
			external_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description VARCHAR(50),
			type VARCHAR(20) NOT NULL,
			display TEXT,
			handler TEXT) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE external_host (
        	external_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED NOT NULL,
			data TEXT,
			PRIMARY KEY (external_id,host_id),
			FOREIGN KEY (external_id) REFERENCES externals(external_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE external_service (
        	external_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED NOT NULL,
			service_id INT(8) UNSIGNED NOT NULL,
			data TEXT,
			PRIMARY KEY (external_id,host_id,service_id),
			FOREIGN KEY (external_id) REFERENCES externals(external_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE external_host_profile (
        	external_id SMALLINT(4) UNSIGNED,
			hostprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (external_id,hostprofile_id),
			FOREIGN KEY (external_id) REFERENCES externals(external_id) ON DELETE CASCADE,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE external_service_names (
        	external_id SMALLINT(4) UNSIGNED,
			servicename_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (external_id,servicename_id),
			FOREIGN KEY (external_id) REFERENCES externals(external_id) ON DELETE CASCADE,
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Object Request Brokers
    #

    $dbh->do(
		"CREATE TABLE object_request_brokers (
			orb_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			arguements TEXT) TYPE=INNODB"
    );

    #
    ###########################################################################
    # Groups
    #

    $dbh->do(
		"CREATE TABLE monarch_groups (
			group_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description VARCHAR(255),
			location TEXT,
			status TINYINT(1) DEFAULT '0',
			data TEXT) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE monarch_macros (
			macro_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			value VARCHAR(255),
			description VARCHAR(255)) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE monarch_group_host (
        	group_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (group_id,host_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE monarch_group_hostgroup (
        	group_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,hostgroup_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE monarch_group_child (
        	group_id SMALLINT(4) UNSIGNED,
			child_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,child_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (child_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE monarch_group_macro (
        	group_id SMALLINT(4) UNSIGNED,
			macro_id SMALLINT(4) UNSIGNED,
			value VARCHAR(255),
			PRIMARY KEY (group_id,macro_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (macro_id) REFERENCES monarch_macros(macro_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE monarch_group_props (
			prop_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			group_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			type VARCHAR(20),
			value VARCHAR(255),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    ###########################################################################
    # session
    #

    $dbh->do(
        "CREATE TABLE sessions (
        	id CHAR(32) NOT NULL UNIQUE,
			a_session TEXT NOT NULL)"
    );

    #
    # Tables to support integration with other tools 2007-Jan-16
    #

    $dbh->do(
		"CREATE TABLE import_schema (
			schema_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			delimiter VARCHAR(50),
			description TEXT,
			type VARCHAR(255),
			sync_object varchar(50),
			smart_name TINYINT(1) DEFAULT '0',
			hostprofile_id SMALLINT(4) UNSIGNED DEFAULT '0',
			data_source VARCHAR(255),
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE import_column (
			column_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			schema_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			position SMALLINT(4) UNSIGNED,
			delimiter VARCHAR(50),
			FOREIGN KEY (schema_id) REFERENCES import_schema(schema_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE import_match (
			match_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			column_id SMALLINT(4) UNSIGNED,
			name VARCHAR(255),
			match_order SMALLINT(4) UNSIGNED,
			match_type VARCHAR(255),
			match_string VARCHAR(255),
			rule VARCHAR(255),
			object VARCHAR(255),
			hostprofile_id SMALLINT(4) UNSIGNED,
			servicename_id SMALLINT(4) UNSIGNED,
			arguments VARCHAR(255),
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (column_id) REFERENCES import_column(column_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE import_match_parent (
        	match_id SMALLINT(4) UNSIGNED,
			parent_id INT(6) UNSIGNED,
			PRIMARY KEY (match_id,parent_id),
			FOREIGN KEY (parent_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE import_match_hostgroup (
        	match_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,hostgroup_id),
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE import_match_group (
        	match_id SMALLINT(4) UNSIGNED,
			group_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,group_id),
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE import_match_contactgroup (
        	match_id SMALLINT(4) UNSIGNED,
			contactgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,contactgroup_id),
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE import_match_serviceprofile (
			match_id SMALLINT(4) UNSIGNED,
			serviceprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (match_id,serviceprofile_id),
			FOREIGN KEY (serviceprofile_id) REFERENCES profiles_service(serviceprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (match_id) REFERENCES import_match(match_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    # Tables to support autodiscovery 2007-09-18
    #

    $dbh->do(
		"CREATE TABLE discover_group (
			group_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description TEXT,
			config TEXT,
			schema_id SMALLINT(4) UNSIGNED,
			FOREIGN KEY (schema_id) REFERENCES import_schema(schema_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE discover_method (
			method_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			description TEXT,
			config TEXT,
			type VARCHAR(50)) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE discover_filter (
			filter_id SMALLINT(4) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
			name VARCHAR(255),
			type VARCHAR(50),
			filter TEXT) TYPE=INNODB"
    );

    $dbh->do(
		"CREATE TABLE discover_group_filter (
	        	group_id SMALLINT(4) UNSIGNED,
			filter_id SMALLINT(4) UNSIGNED,
			enable tinyint(1) default '0',
			PRIMARY KEY (group_id,filter_id),
			FOREIGN KEY (group_id) REFERENCES discover_group(group_id) ON DELETE CASCADE,
			FOREIGN KEY (filter_id) REFERENCES discover_filter(filter_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE discover_group_method (
        	group_id SMALLINT(4) UNSIGNED,
			method_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (group_id,method_id),
			FOREIGN KEY (method_id) REFERENCES discover_method(method_id) ON DELETE CASCADE,
			FOREIGN KEY (group_id) REFERENCES discover_group(group_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    $dbh->do(
        "CREATE TABLE discover_method_filter (
        	method_id SMALLINT(4) UNSIGNED,
			filter_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (method_id,filter_id),
			FOREIGN KEY (method_id) REFERENCES discover_method(method_id) ON DELETE CASCADE,
			FOREIGN KEY (filter_id) REFERENCES discover_filter(filter_id) ON DELETE CASCADE) TYPE=INNODB"
    );

    #
    # Performance and import tables
    #

    $dbh->do(
		"CREATE TABLE import_hosts (
			import_hosts_id smallint(4) unsigned NOT NULL auto_increment,
			name varchar(255) default NULL,
			alias varchar(255) default NULL,
			address varchar(50) default NULL,
			hostprofile_id smallint(4) unsigned default NULL,
			PRIMARY KEY  (import_hosts_id),
			UNIQUE KEY name (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
    );

    $dbh->do(
		"CREATE TABLE import_services (
			import_services_id smallint(4) unsigned NOT NULL auto_increment,
			import_hosts_id smallint(4) unsigned default NULL,
			description varchar(255) default NULL,
			check_command_id smallint(4) unsigned default NULL,
			command_line varchar(255) default NULL,
			command_line_trans varchar(255) default NULL,
			servicename_id smallint(4) unsigned default NULL,
			serviceprofile_id smallint(4) unsigned default NULL,
			PRIMARY KEY  (import_services_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
    );

    $dbh->do(
		"CREATE TABLE datatype (
			datatype_id smallint(4) unsigned NOT NULL auto_increment,
			type varchar(100) NOT NULL default '',
			location varchar(255) NOT NULL default '',
			PRIMARY KEY  (datatype_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
    );

    $dbh->do(
		"CREATE TABLE host_service (
			host_service_id smallint(4) unsigned NOT NULL auto_increment,
			host varchar(100) NOT NULL default '',
			service varchar(100) NOT NULL default '',
			label varchar(100) NOT NULL default '',
			dataname varchar(100) NOT NULL default '',
			datatype_id smallint(4) default '0',
			PRIMARY KEY  (host_service_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
    );

    $dbh->do(
        "CREATE TABLE performanceconfig (
			performanceconfig_id smallint(4) unsigned NOT NULL auto_increment,
			host varchar(100) NOT NULL default '',
			service varchar(100) NOT NULL default '',
			type varchar(100) NOT NULL default '',
			enable tinyint(1) default '0',
			parseregx_first tinyint(1) default '0',
			service_regx tinyint(1) default '0',
			label varchar(100) NOT NULL default '',
			rrdname varchar(100) NOT NULL default '',
			rrdcreatestring text NOT NULL,
			rrdupdatestring text NOT NULL,
			graphcgi varchar(255) NOT NULL default '',
			perfidstring varchar(100) NOT NULL default '',
			parseregx varchar(255) NOT NULL default '',
			PRIMARY KEY  (performanceconfig_id),
			UNIQUE KEY host (host,service)) ENGINE=InnoDB DEFAULT CHARSET=latin1"
    );


    #
    # Tables split out from the old contactgroup_assign table
    #


    #
    ###########################################################################
    # contactgroup_host
    #

    $dbh->do(
		"CREATE TABLE contactgroup_host (
			contactgroup_id SMALLINT(4) UNSIGNED,
			host_id INT(6) UNSIGNED,
			PRIMARY KEY (contactgroup_id,host_id),
			FOREIGN KEY (host_id) REFERENCES hosts(host_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_service
    #

    $dbh->do(
		"CREATE TABLE contactgroup_service (
			contactgroup_id SMALLINT(4) UNSIGNED,
			service_id INT(8) UNSIGNED,
			PRIMARY KEY (contactgroup_id,service_id),
			FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_host_template
    #

    $dbh->do(
		"CREATE TABLE contactgroup_host_template (
			contactgroup_id SMALLINT(4) UNSIGNED,
			hosttemplate_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,hosttemplate_id),
			FOREIGN KEY (hosttemplate_id) REFERENCES host_templates(hosttemplate_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_service_template
    #

    $dbh->do(
		"CREATE TABLE contactgroup_service_template (
			contactgroup_id SMALLINT(4) UNSIGNED,
			servicetemplate_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,servicetemplate_id),
			FOREIGN KEY (servicetemplate_id) REFERENCES service_templates(servicetemplate_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_host_profile
    #

    $dbh->do(
		"CREATE TABLE contactgroup_host_profile (
			contactgroup_id SMALLINT(4) UNSIGNED,
			hostprofile_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,hostprofile_id),
			FOREIGN KEY (hostprofile_id) REFERENCES profiles_host(hostprofile_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_service_name
    #

    $dbh->do(
		"CREATE TABLE contactgroup_service_name (
			contactgroup_id SMALLINT(4) UNSIGNED,
			servicename_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,servicename_id),
			FOREIGN KEY (servicename_id) REFERENCES service_names(servicename_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_hostgroup
    #

    $dbh->do(
		"CREATE TABLE contactgroup_hostgroup (
			contactgroup_id SMALLINT(4) UNSIGNED,
			hostgroup_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,hostgroup_id),
			FOREIGN KEY (hostgroup_id) REFERENCES hostgroups(hostgroup_id) ON DELETE CASCADE,
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);

    #
    ###########################################################################
    # contactgroup_group
    #

    $dbh->do(
		"CREATE TABLE contactgroup_group (
			contactgroup_id SMALLINT(4) UNSIGNED,
			group_id SMALLINT(4) UNSIGNED,
			PRIMARY KEY (contactgroup_id,group_id), 
			FOREIGN KEY (group_id) REFERENCES monarch_groups(group_id) ON DELETE CASCADE, 
			FOREIGN KEY (contactgroup_id) REFERENCES contactgroups(contactgroup_id) ON DELETE CASCADE) TYPE=INNODB"
	);




    #
    ##########################################################################
    # Monarch
    ##########################################################################
    # Load default data
    #

    my @asset_list = (
        'host_templates',                  'host_dependencies',
        'extended_host_info_templates',    'hostgroups',
        'service_templates',               'service_dependency_templates',
        'extended_service_info_templates', 'contact_templates',
        'contacts',                        'contactgroups',
        'time_periods',                    'commands',
        'profiles',                        'hosts',
        'escalations',                     'services',
        'parent_child',                    'export',
        'host_delete_tool',                'service_delete_tool',
        'servicegroups',                   'externals'
    );

    my @options =
      ( 'login_authentication', 'session_timeout', 'super_user_password' );
    foreach my $opt (@options) {
        my $timeout = undef;
        if ( $opt eq 'session_timeout' ) { $timeout = 3600 }
        my $sqlstmt = "insert into setup values('$opt','','$timeout')";
        my $sth     = $dbh->prepare($sqlstmt);
        unless ( $sth->execute() ) {
            print "\n\n $sqlstmt $@\n\n";
            my $x = <STDIN>;
        }
        $sth->finish;
    }

    $dbh->do(
"insert into user_groups values(NULL,'super_users','System defined group granted complete access.')"
    );

    my $sqlstmt = "select max(usergroup_id) as lastrow from user_groups";
    my $gid     = $dbh->selectrow_array($sqlstmt);

    my $now = time;
    my @saltchars = ( 'a' .. 'z', 'A' .. 'Z', '0' .. '9', ',', '/' );
    srand( time() ^ ( $$ + ( $$ << 15 ) ) );
    my $salt = $saltchars[ int( rand(64) ) ];
    $salt .= $saltchars[ int( rand(64) ) ];
    my $newpw = crypt( 'password', $salt );

    $dbh->do(
"insert into users values(NULL,'super_user','Super User Account','$newpw','$now')"
    );
    my $sqlstmt = "select max(user_id) as lastrow from users";
    my $uid     = $dbh->selectrow_array($sqlstmt);
    $dbh->do("insert into user_group values ('$gid','$uid')");

    foreach my $asset (@asset_list) {
        $dbh->do(
"insert into access_list values('$asset','design_manage','$gid','add,modify,delete')"
        );
    }

    my @control_list = (
        'users',                     'user_groups',
        'setup',                     'nagios_cgi_configuration',
        'nagios_main_configuration', 'nagios_resource_macros',
        'load',                      'pre_flight_test',
        'commit',                    'run_external_scripts'
    );
    foreach my $asset (@control_list) {
        $dbh->do(
"insert into access_list values('$asset','control','$gid','full_control')"
        );
    }

    $dbh->do(
"insert into access_list values('host_delete_tool','tools','$gid','add,modify,delete')"
    );
    $dbh->do(
"insert into access_list values('service_delete_tool','tools','$gid','add,modify,delete')"
    );
    $dbh->do(
        "insert into access_list values('manage','group_macro','$gid','manage')"
    );

    my @ez_list = (
        'ez_enabled',  'main_ez',          'ez_hosts',  'ez_host_groups',
        'ez_profiles', 'ez_notifications', 'ez_commit', 'ez_setup',
        'ez_discover', 'ez_import'
    );
    foreach my $ez (@ez_list) {
        $dbh->do("insert into access_list values('$ez','ez','$gid','$ez')");
    }

    for ( my $i = 1 ; $i <= 32 ; $i++ ) {
        $dbh->do("insert into setup values('resource_label$i','resource','')");
        $dbh->do("insert into setup values('user$i','resource','')");
    }

    my %misc = (
        'nagios_version'   => $nagios_ver,
        'nagios_etc'       => $nagios_etc,
        'backup_dir'       => $monarch_home . '/backup',
        'monarch_version'  => $version,
        'monarch_home'     => $monarch_home,
        'upload_dir'       => '/tmp',
        'nagios_bin'       => $nagios_bin,
        'doc_root'         => $doc_root,
        'cgi_bin'          => $cgi_dir,
        'max_tree_nodes'   => '3000',
        'enable_externals' => '0'
    );
    foreach my $key ( keys %misc ) {
        $dbh->do("insert into setup values('$key','config','$misc{$key}')");
    }
    if ($dbh) {
        $dbh and $dbh->disconnect();
    }
}

sub distribution() {
    umask 0002;
    if ( !-e "$doc_root/monarch" ) {
        mkdir( "$doc_root/monarch", 0770 ) || print "\n\n$doc_root/monarch $!";
    }
    if ( !-e "$doc_root/monarch/download" ) {
        mkdir( "$doc_root/monarch/download", 0770 )
          || print "\n\n$doc_root/monarch/download $!";
    }
    if ( !-e "$doc_root/monarch/images" ) {
        mkdir( "$doc_root/monarch/images", 0770 )
          || print "\n\n$doc_root/monarch/images $!";
    }
    if ( !-e "$doc_root/monarch/doc" ) {
        mkdir( "$doc_root/monarch/doc", 0770 )
          || print "\n\n$doc_root/monarch/doc $!";
    }
    if ( !-e "$doc_root/monarch/doc/images" ) {
        mkdir( "$doc_root/monarch/doc/images", 0770 )
          || print "\n\n$doc_root/monarch/doc/images $!";
    }
    my $path = '/';
    my @folders = split( /\//, $monarch_home );
    foreach my $folder (@folders) {
        if ( !-e "$path$folder" ) {
            mkdir( "$path$folder", 0755 ) || print "\n\n$path$folder $!";
        }
        $path .= "$folder/";
    }
    if ( !-e "$monarch_home/workspace" ) {
        mkdir( "$monarch_home/workspace", 0770 )
          || print "\n\n$monarch_home/monarch/workspace $!";
    }
    if ( !-e "$monarch_home/upload" ) {
        mkdir( "$monarch_home/upload", 0770 )
          || print "\n\n$monarch_home/upload $!";
    }
    if ( !-e "$monarch_home/backup" ) {
        mkdir( "$monarch_home/backup", 0770 )
          || print "\n\n$monarch_home/backup $!";
    }
    if ( !-e "$monarch_home/bin" ) {
        mkdir( "$monarch_home/bin", 0770 ) || print "\n\n$monarch_home/bin $!";
    }
    if ( !-e "$monarch_home/lib" ) {
        mkdir( "$monarch_home/lib", 0770 ) || print "\n\n$monarch_home/lib $!";
    }
    if ( !-e "$monarch_home/conf" ) {
        mkdir( "$monarch_home/conf", 0770 )
          || print "\n\n$monarch_home/conf $!";
    }
    if ( !-e "$monarch_home/tools" ) {
        mkdir( "$monarch_home/tools", 0770 )
          || print "\n\n$monarch_home/tools $!";
    }
    opendir( DIR, './images' ) || print "\n\n./images $!";
    while ( my $file = readdir(DIR) ) {
        if ( $file =~ /^\./ ) { next }
        copy( "./images/$file", "$doc_root/monarch/images/$file" )
          || print "\n\n./images/$file $doc_root/monarch/images/$file $!";
    }
    close(DIR);
    opendir( DIR, './doc' ) || print "\n\n./doc $!";
    while ( my $file = readdir(DIR) ) {
        if ( $file =~ /^\.|^images$/ ) { next }
        copy( "./doc/$file", "$doc_root/monarch/doc/$file" )
          || print "\n\n./doc/$file $doc_root/monarch/doc/$file $!";
    }
    close(DIR);
    opendir( DIR, './doc/images' ) || print "\n\n./doc/images $!";
    while ( my $file = readdir(DIR) ) {
        if ( $file =~ /^\./ ) { next }
        copy( "./doc/images/$file", "$doc_root/monarch/doc/images/$file" )
          || print "\n\n./doc/$file $doc_root/monarch/doc/images/$file $!";
    }
    close(DIR);
    open( FILE, "< ./monarch.cgi" ) || print "\n\n./monarch.cgi $!";
    my $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $cgi_dir/monarch.cgi" ) || print "\n$cgi_dir/monarch.cgi $!";
    print FILE $out_to_file;
    close(FILE);

    open( FILE, "< ./monarch_ajax.cgi" ) || print "\n\n./monarch_ajax.cgi $!";
    $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $cgi_dir/monarch_ajax.cgi" )
      || print "\n$cgi_dir/monarch_ajax.cgi $!";
    print FILE $out_to_file;
    close(FILE);

    open( FILE, "< ./monarch_scan.cgi" ) || print "\n\n./monarch_scan.cgi $!";
    $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $cgi_dir/monarch_scan.cgi" )
      || print "\n$cgi_dir/monarch_scan.cgi $!";
    print FILE $out_to_file;
    close(FILE);

    open( FILE, "< ./monarch_tree.cgi" ) || print "\n\n./monarch_tree.cgi $!";
    $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $cgi_dir/monarch_tree.cgi" )
      || print "\n$cgi_dir/monarch_tree.cgi $!";
    print FILE $out_to_file;
    close(FILE);

    open( FILE, "< ./monarch_ez.cgi" ) || print "\n\n./monarch_ez.cgi $!";
    $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $cgi_dir/monarch_ez.cgi" )
      || print "\n$cgi_dir/monarch_ez.cgi $!";
    print FILE $out_to_file;
    close(FILE);

    open( FILE, "< ./monarch_auto.cgi" ) || print "\n\n ./monarch_auto.cgi $!";
    my $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    print "\n\tWriting $cgi_dir/monarch_auto.cgi";
    open( FILE, "> $cgi_dir/monarch_auto.cgi" ) || print "\n\nerror: $!";
    print FILE $out_to_file;
    close(FILE);
    system("chmod 755 $cgi_dir/monarch_auto.cgi");

    open( FILE, "< ./MonarchForms.pm" ) || print "\n\n ./MonarchForms.pm $!";
    my $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^\s*my\s+\$cgi_dir/ ) {
            $line = "my \$cgi_dir = \'$cgi_path\'\;\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $monarch_home/lib/MonarchForms.pm" )
      || print "\n\nerror: $!";
    print FILE $out_to_file;
    close(FILE);
    system("chmod 664  $monarch_home/lib/MonarchForms.pm");

    my @files = (
        'monarch.js',     'nicetitle.js',
	'DataFormValidator.js',
        'monarch.css', 'blank.html'
    );
    foreach my $file (@files) {
        copy( "./$file", "$doc_root/monarch/$file" )
          || print "\n\n./$file $doc_root/monarch/$file $!";
    }
    unless ( -e "$doc_root/favicon.ico" ) {
        copy( "./favicon.ico", "$doc_root/favicon.ico" )
          || print "\n\n./favicon.ico $doc_root/favicon.ico $!";
    }

    open( FILE, "< ./MonarchConf.pm" ) || print "\n\n./MonarchConf.pm $!";
    my $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /DATABASE/ ) {
            $line =~ s/DATABASE/$database/;
        }
        elsif ( $line =~ /DBHOST/ ) {
            $line =~ s/DBHOST/$databasehost/;
        }
        elsif ( $line =~ /DBUSER/ ) {
            $line =~ s/DBUSER/$databaseuser/;
        }
        elsif ( $line =~ /DBPASS/ ) {
            $line =~ s/DBPASS/$databasepasswd/;
        }
        $out_to_file .= $line;
    }

    close(FILE);
    open( FILE, "> $monarch_home/lib/MonarchConf.pm" )
      || print "\n$monarch_home/lib/MonarchConf.pm $!";
    print FILE $out_to_file;
    close(FILE);

    @files = (
        'MonarchFile.pm',          'MonarchStorProc.pm',
        'MonarchExternals.pm',     'MonarchDoc.pm',
        'MonarchLoad.pm',          'MonarchProfileExport.pm',
        'MonarchProfileImport.pm', 'MonarchTree.pm',
        'MonarchDeploy.pm',        'MonarchCallOut.pm',
        'MonarchAudit.pm',         'MonarchFoundationSync.pm',
        'MonarchAutoConfig.pm',
        'MonarchValidation.pm',    'MonarchInstrument.pm'
    );
    foreach my $file (@files) {
        copy( "./$file", "$monarch_home/lib/$file" )
          || print "\n\nnot copied ./$file $monarch_home/lib/$file $!";
    }
    @files = ( 'nagios_reload', 'nmap_scan_one' );
    foreach my $file (@files) {
        copy( "./$file", "$monarch_home/bin/$file" )
          || print "\n\nnot copied ./$file $monarch_home/bin/$file $!\n";
    }
    open( FILE, "< ./nmap_scan_one.pl" ) || print "\n\n./nmap_scan_one.pl $!";
    $out_to_file = undef;
    while ( my $line = <FILE> ) {
        if ( $line =~ /^#!/ ) {
            $line = "#!/usr/bin/perl --\n";
        }
        if ( $line =~ /^\s*use\s+lib\s+/ ) {
            $line = "use lib qq($monarch_home/lib);\n";
        }
        $out_to_file .= $line;
    }
    close(FILE);
    open( FILE, "> $monarch_home/bin/nmap_scan_one.pl" )
      || print "\n$monarch_home/bin/nmap_scan_one.pl $!";
    print FILE $out_to_file;
    close(FILE);

    system("chown $web_user:$web_group $cgi_dir/monarch.cgi");
    system("chmod 755 $cgi_dir/monarch.cgi");
    system("chmod 755 $cgi_dir/monarch_ajax.cgi");
    system("chmod 755 $cgi_dir/monarch_scan.cgi");
    system("chmod 755 $cgi_dir/monarch_tree.cgi");
    system("chmod 755 $cgi_dir/monarch_ez.cgi");
    system("chown -R $web_user:$web_group $doc_root/monarch");
    system("chown -R $web_user:$web_group $monarch_home");
    system("chown root:$web_group $monarch_home/bin/nagios_reload");
    system("chmod 4750 $monarch_home/bin/nagios_reload");
    system("chown root:$web_group $monarch_home/bin/nmap_scan_one");
    system("chmod 4750 $monarch_home/bin/nmap_scan_one");
    system("chown $web_user:$web_group $monarch_home/bin/nmap_scan_one.pl");
    system("chmod 755 $monarch_home/bin/nmap_scan_one.pl");

    my $errors = 0;
    if ( $set_perms =~ /yes/i ) {
        my @files         = ();
        my @dirs          = ();
        my $resource_file = undef;
        if ( -e "$nagios_etc/nagios.cfg" ) {
            system("chmod g+rw $nagios_etc/nagios.cfg");
            open( FILE, "< $nagios_etc/nagios.cfg" )
              || print "\n$nagios_etc/nagios.cfg $!";
            while ( my $line = <FILE> ) {
                if ( $line =~ /^cfg_file=(\S+)$/ ) { push @files, $1 }
                if ( $line =~ /^cfg_dir=(\S+)$/ )  { push @dirs,  $1 }
                if ( $line =~ /^resource_file=(\S+)$/ ) { $resource_file = $1 }
            }
            close(FILE);
            foreach my $dir (@dirs) {
                opendir( DIR, $dir )
                  || print "\nerror: cannot open $dir to read $!";
                while ( my $file = readdir(DIR) ) {
                    if ( $file =~ /(\S+\.cfg$)/ ) { push @files, "$dir/$file" }
                }
                close(DIR);
                system("chmod g+rwx $dir");
            }
        }
        else {
            print "\n\n\tError: $nagios_etc/nagios.cfg does not exist\n";
            $errors = 1;
        }
        if ( -e "$nagios_etc/cgi.cfg" ) {
            system("chmod g+rw $nagios_etc/cgi.cfg");
            open( FILE, "< $nagios_etc/cgi.cfg" )
              or print "error: cannot open $nagios_etc/cgi.cfg to read $!";
            while ( my $line = <FILE> ) {
                if ( $line =~ /^xedtemplate_config_file=(\S+)$/ ) {
                    push @files, $1;
                }
            }
            close(FILE);

        }
        else {
            print "\n\n\tError: $nagios_etc/cgi.cfg does not exist\n";
            $errors = 1;
        }
        unless ($errors) {
            foreach my $file (@files) {
                system("chmod g+rw $file");
            }
        }
        if ( $resource_file =~ /(.*)\/.*\.cfg/ ) {
            system("chmod g+rwx $1");
        }
        system("chmod g+rw $resource_file");
        system("chmod g+rwx $nagios_etc");
    }
}

sub collect_info() {
    my $validated = 0;
    print "\n\n\tThis script will help you setup Monarch on your system.";
    print "\n\n\tDefaults and guesses will be provided where possible.\n";
    print
"\n\n\n\tMySQL database server host name [$databasehost] : ";
    my $input = <STDIN>;
    chomp $input;
    if ($input) { $databasehost = $input }
    print
"\n\n\n\tDatabase user name [$databaseuser] : ";
    my $input = <STDIN>;
    chomp $input;
    if ($input) { $databaseuser = $input }
    print
"\n\n\n\tDatabase password for user $databaseuser [$databasepasswd] : ";
    my $input = <STDIN>;
    chomp $input;
    if ($input) { $databasepasswd = $input }
    print
"\n\n\n\tName of database for your Monarch installation [$database] : ";
    my $input = <STDIN>;
    chomp $input;
    if ($input) { $database = $input }
    $validated = 0;

    until ($validated) {
        print "\n\n\n\tUser account for httpd web server [$web_user] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $web_user = $input }
        my @user = getpwnam($web_user);
        if ( $user[0] ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid, user $input does not exist.";
        }
    }
    $validated = 0;
    until ($validated) {
        print "\n\n\n\tUNIX group for httpd web server [$web_group] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $web_group = $input }
        my @grp = getgrnam($web_group);
        if ( $grp[0] ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid, group $input does not exist.";
        }
    }

    unless ( -e $doc_root ) {
        if ( -e "/var/www/htdocs" ) {
            $doc_root = "/var/www/htdocs";
        }
        else {
            $doc_root = "unknown";
        }
    }
    $validated = 0;

    until ($cgi_path) {
        $cgi_path = "/cgi-bin";
        print
"\n\n\tWeb server's relative path to cgi-bin.\n\tExample: /nagios/cgi-bin [ /cgi-bin ] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $cgi_path = $input }
    }
    $validated = 0;

    until ($validated) {
        if ($doc_root) {
            print
              "\n\n\n\tWeb server's document root [$doc_root] : ";
        }
        else {
            print
"\n\n\n\tFull path to web server's document root : ";
        }
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $doc_root = $input }
        if ( -e $doc_root ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid entry $doc_root does not exist.";
        }
    }

    unless ( -e $cgi_dir ) { $cgi_dir = "unknown" }
    $validated = 0;
    until ($validated) {
        print
          "\n\n\n\tFull path of cgi-bin directory [$cgi_dir] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $cgi_dir = $input }
        if ( -e $cgi_dir ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid entry $cgi_dir does not exist.";
        }
    }

    print
      "\n\n\n\tFull installation path for Monarch [$monarch_home] : ";
    my $input = <STDIN>;
    chomp $input;
    if ($input) { $monarch_home = $input }

    if ( -e '/etc/nagios/nagios.cfg' ) {
        $nagios_etc = '/etc/nagios';
    }
    elsif ( -e '/usr/local/nagios/etc/nagios.cfg' ) {
        $nagios_etc = '/usr/local/nagios/etc';
    }
    else {
        $nagios_etc = undef;
    }

    $validated = 0;
    until ($validated) {
        print
"\n\n\n\tDirectory where nagios.cfg file is stored [$nagios_etc] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $nagios_etc = $input }
        if ( -e $nagios_etc ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid entry $nagios_etc does not exist.";
        }
    }

    if ( -e '/usr/sbin/nagios' ) {
        $nagios_bin = '/usr/sbin';
    }
    elsif ( -e '/usr/local/nagios/bin/nagios' ) {
        $nagios_bin = '/usr/local/nagios/bin';
    }
    else {
        $nagios_bin = undef;
    }

    $validated = 0;
    until ($validated) {
        print
"\n\n\n\tDirectory where nagios binary file resides [$nagios_bin] : ";
        my $input = <STDIN>;
        chomp $input;
        if ($input) { $nagios_bin = $input }
        if ( -e $nagios_bin ) {
            $validated = 1;
        }
        else {
            print "\n\n\n\tError: Invalid entry $nagios_bin does not exist.";
        }
    }
    if ( -e "$nagios_etc/nagios.cfg" ) {
        my @stats = stat("$nagios_etc/nagios.cfg");
        if ( $stats[5] eq '0' ) {
            print
"\n\n\n\tWARNING: Group ownership of $nagios_etc/nagios.cfg is root.";
            print
"\n\tYour web server account will need read and write access to the ";
            print
"\n\tnagios object files. The best method is to included the account ";
            print
"\n\tin a non root group (nagios),and make that group the owner of ";
	    print
"\n\tthe nagios object files.";
        }
        else {
            print
"\n\n\n\tYour web server account will need read and write access to ";
            print
"\n\tthe nagios object files. Would you like to set group permissions ";
	    print
"\n\tso that Monarch can read your nagios cfg files including ";
	    print
"\n\tresource.cfg? (Note: You will need to manage this manually if ";
	    print
"\n\tyour answer is no). yes|no : ";
            my $input = <STDIN>;
            chomp $input;
            if ( $input =~ /^yes$/i ) {
                $set_perms = $input;
            }
            else {
                $set_perms = 0;
            }
        }
    }
    print qq(

		Database host	= $databasehost
		Database name	= $database
		Database user	= $databaseuser
		Database passwd = $databasepasswd
		Document root	= $doc_root
		Web user	= $web_user
		Web user group	= $web_group
		Web cgi-bin	= $cgi_dir
		Nagios bin	= $nagios_bin
		Nagios cfg dir	= $nagios_etc
		Install path	= $monarch_home

	Confirmation: Be absolutely certain these values are correct. 
	Does the information look correct? 'yes' or 'no' : );
    my $response = <STDIN>;
    if ( $response =~ /^yes$/i ) {
        if ( $drop_tables == 1 ) { drop_tables() }
        distribution();
    }
    else {
        collect_info();
    }

}

check_prereqs();
collect_info();
create_db();

print "\n\tSetup completed.\n\n";

