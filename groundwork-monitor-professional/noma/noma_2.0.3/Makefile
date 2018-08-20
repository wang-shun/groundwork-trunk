# ================================================================
# Makefile for NoMa, as built for GroundWork Monitor
# ================================================================

# ----------------------------------------------------------------
# Notes
# ----------------------------------------------------------------
#
# (*) This Makefile is here to assist GroundWork in development of
#     patches to stock NoMa releases.  It may also assist BitRock in
#     building the NoMa software using our patches.  BitRock is not
#     required to use this Makefile for their builds, but it should
#     provide the main guidance they need to integrate our work into
#     their build infrastructure.
#
# (*) This build assumes that you have a copy of GWMEE installed, so
#     as to access a copy of GroundWork Perl and its packages to be
#     referenced by the NoMa software.
#
# (*) The NoMa "configure" program demands that you have one of the
#     following UNIX groups defined on your build machine:
#         www www-data apache httpd
#     If you don't, you will need to use the --with-webgroup option
#     to specify the group, instead (though this doesn't seem to be
#     referenced anywhere within the actual code).  So we force the
#     issue here, by defining a value corresponding to our usage.
#
# (*) The build process below creates a directory under /tmp to hold
#     the untarred NoMa distribution.  That's because the distribution
#     anomalously hardcodes that placement in some places that come
#     into action when you go to clean up the distribution.  Rather
#     than bother patching their build scripting, we have simply
#     adapted to it.  So we use the same convention here.
#
# (*) The patches listed here will be revised to split the parts that
#     are for porting to PostgreSQL, which should be applied first,
#     from the parts that are for porting to the GroundWork context,
#     which should be applied second.  We want to make this split so
#     we can attempt to push the PostgreSQL porting changes upstream,
#     while not contaminating those changes with what we have done to
#     integrate NoMa into our own product.
#
# (*) Some stuff is not handled by our patches at this time.  Some of
#     this may come from GroundWork either soon or in a future release,
#     and some will be handled directly by BitRock's own build/install
#     processing.  Additional items may be added to this list as we
#     test the initial development builds.
#
#     GroundWork's responsibility:
#     (+) Moving the frontend.debug.file specified in the NoMa.yaml
#         file, changing it from /tmp/NoMa-logfile.log to perhaps be
#         /usr/local/groundwork/noma/var/NoMa-logfile.log instead.
#     (+) Log rotation of the two NoMa log files (NoMa-logfile.log and
#         noma_debug.log), synchronized in whatever way is necessary
#         with the writing of these files by NoMa.
#     (+) Extension of the alert_via_noma.pl script to accept Nagios
#         Custom Object Variable definitions, so they can be used to
#         modify the ways that notifications are produced and sent.
#     (+) Full support for a PostgreSQL database to replace our current
#         use of SQLite.  This would primarily involve defining the new
#         schema (tables, sequences, indexes, etc.).  All we have now is
#         PostgreSQL support for accessing NDO-like data from tables in
#         one of our existing PostgreSQL databases, used to create lists
#         of hosts, services, and the like in the NoMa web interface.
#         This is different from the main body of the NoMa data.
#     (+) Extracting database-access credentials from our standard
#         config/db.properties file instead of having them embedded
#         in the NoMa-specific NoMa.yaml file.  That would make it
#         unnecessary to impose 600 permissions on the NoMa.yaml file
#         as it is deployed at customer sites.  (Our patches to the
#         NoMa build scripting have imposed these restricted permissions.
#         We will check the builds to see whether that change is being
#         percolated through to the deployed file.)
#
#     BitRock's responsibility:
#     (+) Creation of the /usr/local/groundwork/noma/scripts/ directory
#         and populating it with the ctl.sh startup/shutdown script.
#         BitRock has been handling this in NoMa builds to date.
#     (+) Conversion of Perl shebang-line content from #!/usr/bin/perl
#         to #!/usr/local/groundwork/perl/bin/perl in all the NoMa Perl
#         scripts.
#     (+) Creating a symlink at install time for the primary NoMa log file:
#         /usr/local/groundwork/logs/noma_debug.log -> ../noma/var/noma_debug.log
#     (+) Creating a symlink at install time for the secondary NoMa log
#         file, specified in the NoMa.yaml file as the frontend.debug.file
#         category (a bit later, once GroundWork has modified our patches
#         to change the path, as described above):
#         /usr/local/groundwork/logs/NoMa-logfile.log -> ../noma/var/NoMa-logfile.log
#
#     GroundWork's responsibility:
#     (+) Certain files are added to our NoMa builds to complete the NoMa
#         package that we deliver in GWMEE, beyond what is described above.
#         Our EntBuild.sh build script is currently adding these files to
#         our distribution.
#             /usr/local/groundwork/noma/notifier/lib/GWDOWN.pm
#             /usr/local/groundwork/noma/notifier/lib/downtime.pm
#             /usr/local/groundwork/noma/notifier/lib/groupnames.pm
#             /usr/local/groundwork/noma/notifier/sendEmail_gwos.pl
#         These files are drawn from the following locations in Subversion
#         relative to the current directory:
#             gwos/lib/GWDOWN.pm
#             gwos/lib/downtime.pm
#             gwos/lib/groupnames.pm
#             gwos/sendEmail_gwos.pl
#     (+) Unfortunately, certain developers have hacked the clean structure
#         of the NoMa builds, replacing a disciplined patch-only construction
#         with the overlay of complete literally-modified files.  This practice
#         makes it extremely difficult to adapt to any new upstream releases,
#         since the literal files must then be reverse-engineered to figure out
#         what got changed and have those changes applied to the new release.
#         This has happened so far with the following files, irrespective of
#         the fact that in fact we already had a patch file for each of them
#         that is supposed to cover all the changes we make to each of these
#         files:
#             noma/etc/NoMa.yaml
#             noma/notifier/alert_via_noma.pl
#             noma/notifier/lib/bundler.pm
#             noma/notifier/lib/contacts.pm
#             noma/notifier/noma_daemon.pl
#         In the GroundWork-side builds, these files are currently drawn from
#         the following locations in Subversion relative to the current directory:
#             gwos/NoMa.yaml
#             gwos/alert_via_noma.pl
#             gwos/lib/bundler.pm
#             gwos/lib/contacts.pm
#             gwos/noma_daemon.pl
#         Once we have a clean build from BitRock that includes all of our patches
#         that generate the same final files, we will stop using most of the gwos/
#         copies of these files locally, and just use the clean files that BitRock
#         builds from our patches here.
#
# (*) These patches need some post-build, install-time, site-specific
#     modification by the BitRock installer.  In particular:
#     (+) The NoMa.yaml file will need to have POSTGRESQL_SERVER
#         replaced by the (fully qualified?) hostname of the GroundWork
#         PostgreSQL server, whether local or remote with respect to
#         the GroundWork Monitor server, in the same way that we make
#         a similar substitution elsewhere in our product.
#     (+) The NoMa.yaml file will need to have http://localhost/
#         references substituted with the same fully-qualified hostname
#         value as we use for one of the other files (<gatewayLoginUrl> and
#         <gatewayLogoutUrl> in config/resources/josso-agent-config.xml),
#         reflecting the name of the GroundWork Monitor server (not the
#         PostgreSQL server, unless that database is running on the same
#         machine).  And perhaps we might also also want the http: protocol
#         in these lines replaced with https: if we can tell at install time
#         that the GroundWork server will be set up to use SSL.
#     (+) We would like the "locale: en_US" line in the NoMa.yaml file
#         to be modified at install time to be set to the actual locale
#         of the customer's system, but only if the resulting locale is
#         one supported by NoMa.  For the time being, it is reasonable
#         to just leave en_US as a fixed value, and let customers make
#         an adjustment if desired and supported.
#     (+) We would like the "timezone: America/Los_Angeles" line in the
#         NoMa.yaml file to be modified at install time to be set to
#         the actual timezone of the customer's system.

# ----------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------

# You can uncomment the DEVELOPMENT_BUILD definition to run a
# development build that will set some other options below to values
# which are convenient for creating patches.  Leaving this line
# commented out runs a production build, which sets those options to
# values appropriate for the final build for the product.
# DEVELOPMENT_BUILD	:= yes

# We force the referencing of the GroundWork copy of Perl by putting
# it first in the command-search path, in case any build scripting
# depends on being able to locate Perl packages that are needed by
# the NoMa software.
GWPATH			:= /usr/local/groundwork
PATH			:= $(GWPATH)/perl/bin:$(PATH)

# The location of NOMA_DIRECTORY is unfortunately badly specified
# in the NoMa code as living under the /tmp directory.  That path
# is used in the rules specified in the distribution's Makefile.
# We haven't yet patched that, so we use that specification here.

# The location of NOMA_BUILD_DIRECTORY is our own choice.  We use this
# directory to store all the build artifacts in one place which is
# separate from the general /tmp directory, so all the files related
# to this one build can be readily identified and not intermixed with
# files for completely unrelated purposes.  It must be some temporary
# place that can be completely removed after the build is complete,
# without any danger of wiping out important files.

NOMA_VERSION		= 2.0.3
NOMA_TARBALL		= noma_$(NOMA_VERSION).tar.gz
NOMA_DIRECTORY		= /tmp/noma_$(NOMA_VERSION)
NOMA_BUILD_DIRECTORY	= /tmp/noma-build-files

# This alternate NOMA_PREFIX value (under /tmp) is useful in development
# testing, but should be ignored for production builds.
NOMA_PREFIX		= $(if $(DEVELOPMENT_BUILD),/tmp/groundwork/noma,$(GWPATH)/noma)

NAGIOS_PIPE		= $(GWPATH)/nagios/var/spool/nagios.cmd
NAGIOS_PLUGIN_DIRECTORY	= $(GWPATH)/nagios/libexec

# If you want to keep backups of the original files you patch here, while
# you are developing such patches, use this option.  This alternate
# NOMA_PATCH_BACKUP value should be ignored for production builds.
NOMA_PATCH_BACKUP	= $(if $(DEVELOPMENT_BUILD),-b)

NOMA_PATCHES		= \
			noma_2.0.3_NoMa.yaml.in.patch						\
			noma_2.0.3_alert_via_noma.pl.in.patch					\
			noma_2.0.3_config.php.in.patch						\
			noma_2.0.3_configconvert.php.patch					\
			noma_2.0.3_configure.ac.patch						\
			noma_2.0.3_content_funcs.php.patch					\
			noma_2.0.3_content_status_viewer.php.patch				\
			noma_2.0.3_contrib_Makefile.in.patch					\
			noma_2.0.3_database.pm.patch						\
			noma_2.0.3_database.pm.return_last_insert_id.patch.2			\
			noma_2.0.3_default_data.sql.patch					\
			noma_2.0.3_doc_DOCUMENTATION.txt.patch					\
			noma_2.0.3_doc_Makefile.in.patch					\
			noma_2.0.3_etc_Makefile.in.patch					\
			noma_2.0.3_general.php.patch						\
			noma_2.0.3_noma-backend-postgresql.php.patch				\
			noma_2.0.3_notifier_Makefile.in.patch					\
			noma_2.0.3_notifier_lib_bundler.pm.patch				\
			noma_2.0.3_notifier_lib_contacts.pm.patch				\
			noma_2.0.3_notifier_lib_contacts.pm.retrieve_groups.patch.2		\
			noma_2.0.3_notifier_lib_escalations.pm.patch				\
			noma_2.0.3_notifier_lib_thread_procs.pm.patch				\
			noma_2.0.3_notifier_lib_time_frames.pm.patch				\
			noma_2.0.3_notifier_noma_daemon.pl.in.patch				\
			noma_2.0.3_notifier_noma_daemon.pl.in.use_tmp_commands_id.patch.2	\
			noma_2.0.3_notifier_noma_daemon.pl.in.generate_ids.patch.3		\
			noma_2.0.3_preview_host_service.php.patch				\
			noma_2.0.3_share_Makefile.in.patch					\
			noma_2.0.3_share_config_Makefile.in.patch				\
			noma_2.0.3_share_inc_content_log_viewer.php.patch			\
			noma_2.0.3_share_inc_content_timeframe_manager.php.patch		\
			noma_2.0.3_share_index.php.patch					\
			noma_2.0.3_sql_mysql_mysql_new_install.sql.patch			\
			noma_2.0.3_sql_sqlite3_install_default_schema.sql.patch			\
			noma_2.0.3_sql_Makefile.in.patch
CONFIGURE_OPTIONS	= \
			--prefix=$(NOMA_PREFIX)				\
			--with-cmdpipe=$(NAGIOS_PIPE)			\
			--with-plugindir=$(NAGIOS_PLUGIN_DIRECTORY)	\
			--with-webgroup=nagios

# ----------------------------------------------------------------
# Build Rules
# ----------------------------------------------------------------

default	: build

$(NOMA_DIRECTORY)	:
	mkdir $(NOMA_BUILD_DIRECTORY)
	cp -p $(NOMA_TARBALL) $(NOMA_PATCHES) $(NOMA_BUILD_DIRECTORY)
	cd /tmp; tar xfz $(NOMA_BUILD_DIRECTORY)/$(NOMA_TARBALL)
	#
	# For no apparent reason, one file in the upstream distribution has inappropriate permissions.
	cd /tmp; chmod 644 noma_$(NOMA_VERSION)/notifier/lib/contacts.pm
	#
	cd /tmp; for patchfile in $(NOMA_PATCHES); do patch -p0 $(NOMA_PATCH_BACKUP) -z .orig$${patchfile##*.patch} < $(NOMA_BUILD_DIRECTORY)/$$patchfile; done
	#
	# We wish to get rid of all DOS line endings in our distributed NoMa files,
	# because they make something of a visual mess when such files are viewed
	# in an editor that makes CR characters visible.  Generally, we can just
	# apply dos2unix to such files.  We choose in general to keep the original
	# timestamps when we make these changes, as we're not fundamentally changing
	# the content.  However, there is one hiccup we need to accommodate in these
	# file-format conversions.  "dos2unix" will not convert the last line of a
	# file if it ends in a CR character without a trailing NL character.  So for
	# a couple of files that have this condition, we must prepare them beforehand
	# by appending a newline.
	#
	cd $(NOMA_DIRECTORY);						\
	echo >> share/templates/contactgroup_manager_delete.html;	\
	echo >> share/templates/contact_manager_admin_delete.html;	\
	dos2unix -k doc/README doc/VERSION.txt.in doc/*.txt;		\
	dos2unix -k `find . -name '*.php' -print`;			\
	dos2unix -k share/config/templates.php.in;			\
	dos2unix -k share/css/styles.css;				\
	dos2unix -k share/js/script.js;					\
	dos2unix -k `find . -name '*.html' -print`;

config	: $(NOMA_DIRECTORY)
	cd $(NOMA_DIRECTORY); \
	./configure $(CONFIGURE_OPTIONS)

build	: config
	cd $(NOMA_DIRECTORY); make all

# Installation must be done as root, because it tries to change
# the ownership of some files.  For that purpose, we might want
# to disable this "install: build" dependency, so this dependency
# doesn't force a complete re-build as root.
install	: build
	@if [ `id -u` != 0 ]; then echo "ERROR:  You must install as root."; exit 1; fi
	cd $(NOMA_DIRECTORY); make install

# Likewise, if you want to do this during development, you'll need to
# run this as root, because you won't own the files you want to remove.
installclean	:
	@if [ `id -u` != 0 ]; then echo "ERROR:  You must installclean as root."; exit 1; fi
	rm -rf $(NOMA_PREFIX)

# We have to take into account that the NOMA_DIRECTORY might not be there
# (say, because of a prior "make distclean").
clean	:
	cd $(NOMA_DIRECTORY); \
	if [ $$? = 0 ]; then make clean; else echo "ERROR:  Cannot chdir to $(NOMA_DIRECTORY)"; fi

distclean	:
	rm -rf $(NOMA_DIRECTORY)
	rm -rf $(NOMA_BUILD_DIRECTORY)

