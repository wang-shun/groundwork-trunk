##############################################################################
#
# GROUNDWORK MONITOR ARCHITECT (MONARCH)
#
# Updated: 7-Apr-2008 Release: Monarch 2.5 (GroundWork Monitor Architect)
#
# Author: Scott Parris, GroundWork Open Source
##############################################################################


Note: Please be sure to check SourceForge or
      http://www.groundworkopensource.com/community/forums/ for the
      latest information on bugs and patches.



##############################################################################
#
# For a more integrated product, you may want to try GroundWork Monitor
# Community Edition, available as a free download at:
#
#                   http://www.groundworkopensource.com/
#
##############################################################################



##############################################################################
Contents
##############################################################################


I.      Installation
II.     Nagios 1.0 Support
III.    Migration from Nagios 1.2 to 2.x
IV.     Security
V.      EZ Interface
VI.     File Management
VII.    Multiple Instances of Nagios
VIII.   Issues



I.      Installation
##############################################################################

        Everyone:

                Install the following Perl modules:

                perl -MCPAN -e shell

                CGI-Session
                cpan> install CGI::Session

                If you wish to use the Nmap discovery feature in the
                EZ interface, you will need to install Nmap and:

                Nmap-Scanner
                cpan> install Class::Accessor

        Existing Installation:

                Prerequisites to upgrading Monarch

                If you do not have them already, download from CPAN
                and install these modules:

                perl -MCPAN -e shell

                 1.  Class-Accessor
                     cpan> install Class::Accessor

                2.  CGI-Ajax-0.697 (Note: this is the latest availabe
                    from CPAN as of 17 Oct 06)
                    cpan> install CGI::Ajax


                If you are working with an existing database and
                just want to update the distribution, please run
                monarch_update.pl.

                Pre-095c users: After running the update, go to
                Control->Nagios cgi configuration->Load from cgi.cfg
                before doing a commit or an export.

                Pre-097a users: User passwords encrypted. As a
                precaution, super_user password will be reset to
                'password'. Login as super_user and go to
                Control->Users to reset the super_user password.

        New Installation:

                Prerequisites to installing Monarch

                MySQL 5.0.26 or newer

                If you do not have them already, download from CPAN
                and install these Perl modules:

                perl -MCPAN -e shell

                 1.  XML-LibXML-Common
                     cpan> install XML::LibXML::Common

                 2.  XML-NamespaceSupport
                     cpan> install XML::NamespaceSupport

                 3.  XML-SAX
                     cpan> install XML::SAX

                 4.  XML-LibXML-1.58
                     Note: the version depends on the version of
                         libxml2 installed
                     1.61 may or may not work.
                     cpan> install P/PH/PHISH/XML-LibXML-1.58.tar.gz

                 5.  CGI - this should be included in the Perl
                     distribution
                     cpan> install CGI

                 6.  DBI
                     cpan> install DBI

                 7.  DBD-mysql
                     cpan> install DBD::mysql

                 8.  File::Copy - this should be included in the
                     Perl distribution
                     cpan> install File::Copy

                 9.  Class-Accessor
                     cpan> install Class::Accessor

                10.  CGI-Ajax-0.697
                     (Note: this is the latest availabe from CPAN
                     as of 17 Oct 06)
                     cpan> install CGI::Ajax

                11.  Data::FormValidator
                     cpan> install Data::FormValidator

                12.  JavaScript::DataFormValidator
                     cpan> install JavaScript::DataFormValidator

                13.  Carp - this should be included in the
                     Perl distribution
                     cpan> install Carp

                14.  IO::Socket - this should be included in the
                     Perl distribution
                     cpan> install IO::Socket

                15.  Time::Local - this should be included in the
                     Perl distribution
                     cpan> install Time::Local

                16.  URI::Escape
                     cpan> install URI::Escape


                If you wish to use Monarch to run pre-flight checks,
                your web server account will need to be a member of
                the nagios group.

                After satisfying the prerequisites

                1.  Create the database

                    Example:

                    # mysql
                    Your MySQL connection id is 4974 to server
                    version: 5.0.26-standard
                    Type 'help;' or '\h' for help. Type '\c' to clear
                    the buffer.

                    mysql> create database <database name>;

                    (Note: default = monarch)

                2.  Run monarch_setup.pl (if you rerun this script it
                    will drop all of the tables and recreate them).

                3.  Make sure your webserver user is a member of the
                    nagios group

                4.  Nagios cfgs need read/write permissions for
                    group and parent folders need to be searchable
                    (i.e. rwx) for group.

                5.  In your browser enter:
                    http://<hostname|address>/cgi-bin/monarch.cgi

                6.  Login account: super_user password: password

                7.  Go to Control->Setup to set nagios.cfg folder

                8.  Go to Control->Nagios Cgi Configuration and
                    choose one of:
                    a. Load from cgi.cfg - to start from an existing
                       Nagios configuration
                    b. Set default configuration - to start a new
                       Nagios installation

                9.  Go to Control->Nagios Main Configuration and
                    choose one of:
                    a. Load from nagios.cfg - to start from an
                       existing Nagios configuration
                    b. Set default configuration - to start a new
                       Nagios installation

                10. Do one of the following:
                    a. Go to Control->Load to populate the database
                       with an existing configuration.
                    b. Copy the files from samples into your nagios
                       cfg folder and go to Control->Load to populate
                       the db with some basic information. Note: you
                       will need to adjust the nagios.cfg directives
                       to fit your installation.

                If the parser hasn't stumbled on anything, you
                should be ready to go.


II. Nagios 1.0 Support
##############################################################################

        Go to Control -> Setup -> Nagios version.


III. Migration from Nagios 1.2 to 2.x
##############################################################################

        [Note: this information has been retained from the
        README for Monarch 2.0, but has not been tested with 2.5.]

        Load your 1.2 configuration: Go to
        Control->Setup->Nagios version. Go to Hosts->Host Templates
        and adjust the following 2.0 directives:

        Check period
        Active checks enabled
        Passive checks enabled
        Contact groups

            Note: You can still use hostgroups to manage
                  contact groups until you reload the 2.0
                  cfg's. Note also that you will need to
                  adjust the changes in Nagios macros on
                  your command definitions.


IV. Security
##############################################################################

        After logging in (username=super_user/password=password for
        new installs) go to Help and read the section "Chapter 2
        Security - Users and User Groups".


V.      EZ Interface
##############################################################################

        IMPORTANT: YOU MUST DEFINE AT LEAST ONE HOST PROFILE, ONE
        CONTACT GROUP AND ONE CONTACT TEMPLATE TO USE EZ. You will
        find information on granting access to the interface in the
        security section above. The first step, however, is to go to
        Control->Setup and check 'Enable EZ'. You will need to
        refresh your browser to see the upper left drop down option,
        Then go to EZ->Setup to define the default profile, contact
        group and contact template.


VI. File Management
##############################################################################

        The dysfunctional file management system from previous
        releases has been replaced by Groups. By default, all hosts
        will be written to hosts.cfg and all services to
        services.cfg. To enable Groups go to Control->Setup and
        check Enable groups. By assigning hosts or host groups to
        groups the files will be written: <group name>_hosts.cfg
        <group name>_services.cfg


VII. Multiple Instances of Nagios
##############################################################################

        You will find most of the information you need in the Help
        document. More information can be found in the MonarchDeploy
        module, which contains an example that has been implemented
        successfully.


VIII. Issues
##############################################################################

        1.  Web server times out on load. If your configuration is
            large and/or your server is slow you may need to adjust
            the web server time out value.
        2.  Passwords with spaces do not work with mysqldump (don't
            use spaces).
        3.  EZ->Hosts->Discover parses the fully qualified domain
            name and assigns the first part to the host name, so
            host.domain.com creates an entry for host. In most cases
            this is desirable. However, when hosts share the same
            name but have different domains there is a problem. Only
            the first host is picked up.
        3.  EZ->Hosts->Discover page faults if you have not defined
            EZ->Setup.

