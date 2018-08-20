# Makefile for NoMa patches for GWMEE released versions

# This Makefile contains the instructions for making tarballs for
# our NoMa patches for the GWMEE 7.1.0 and 7.1.1 releases.  We do
# this to make sure we have a consistent and controlled method for
# building these patches while they are being developed.

# Copyright (c) 2017 GroundWork, Inc.  All rights reserved.

# These are the patch numbers assigned upstream.  Having these string specified here
# allows the rest of the Makefile to be independent of the particular patch numbers.
PATCH_FOR_710 = TB7.1.0-8
PATCH_FOR_711 = TB7.1.1-10

# Convert those strings to some that we can use within Makefile variable names.
PATCH_710_STRING := $(shell echo $(PATCH_FOR_710) | sed -e 's/TB/TB_/' -e 's/\.//g' -e 's/-/_/g')
PATCH_711_STRING := $(shell echo $(PATCH_FOR_711) | sed -e 's/TB/TB_/' -e 's/\.//g' -e 's/-/_/g')

# These are the patch tarballs we will create to update NoMa in previous
# GWMEE releases.  They will include the patch install and uninstall scripts.
PATCH_TARBALLS = \
	$(PATCH_FOR_710).noma_fixes.tar.gz	\
	$(PATCH_FOR_711).noma_fixes.tar.gz

# These are the NoMa tarballs that we will package inside the patch tarballs,
# containing just the updated NoMa files and not the patch scripting.
NOMA_TARBALLS = \
	$(PATCH_FOR_710).noma_fixes/groundwork-$(PATCH_FOR_710)-noma-fix-files.tar.gz	\
	$(PATCH_FOR_711).noma_fixes/groundwork-$(PATCH_FOR_711)-noma-fix-files.tar.gz

# These are the particular files we want to update in each patch.

$(PATCH_710_STRING)_NOMA_FILES = \
	core/databases/postgresql/noma-db.sql				\
	core/foundation/api/perl/CollageQuery.pm			\
	core/migration/postgresql/add_indexes_to_noma_database.sql	\
	core/migration/postgresql/pg_migrate_noma.pl			\
	core/migration/postgresql/switch_noma_to_generated_ids.sql	\
	noma/doc/DOCUMENTATION.txt					\
	noma/notifier/alert_via_noma.pl					\
	noma/notifier/lib/GWDOWN.pm					\
	noma/notifier/lib/bundler.pm					\
	noma/notifier/lib/contacts.pm					\
	noma/notifier/lib/database.pm					\
	noma/notifier/lib/downtime.pm					\
	noma/notifier/lib/escalations.pm				\
	noma/notifier/lib/groupnames.pm					\
	noma/notifier/lib/thread_procs.pm				\
	noma/notifier/lib/time_frames.pm				\
	noma/notifier/noma_daemon.pl

$(PATCH_711_STRING)_NOMA_FILES = \
	core/databases/postgresql/noma-db.sql				\
	core/foundation/api/perl/CollageQuery.pm			\
	core/migration/postgresql/add_indexes_to_noma_database.sql	\
	core/migration/postgresql/pg_migrate_noma.pl			\
	core/migration/postgresql/switch_noma_to_generated_ids.sql	\
	noma/doc/DOCUMENTATION.txt					\
	noma/notifier/alert_via_noma.pl					\
	noma/notifier/lib/bundler.pm					\
	noma/notifier/lib/contacts.pm					\
	noma/notifier/lib/database.pm					\
	noma/notifier/lib/escalations.pm				\
	noma/notifier/lib/groupnames.pm					\
	noma/notifier/lib/thread_procs.pm				\
	noma/notifier/lib/time_frames.pm				\
	noma/notifier/noma_daemon.pl

# FIX MAJOR:  deal with the pod file and perhaps the .packlist
# FIX MAJOR:  deal with any new directories
# FIX MAJOR:  test with the JSON::PP package added to the 7.1.0 release
#
# This picks up the complete JSON::PP package as installed in the 7.1.0 release.
#
# However, we omit the perl/lib/5.8.9/x86_64-linux-thread-multi/perllocal.pod
# file, which already exists in our 7.1.0 distribution and simply gets updated
# (appended to) if we install the JSON::PP package via CPAN.  I don't want to
# completely replace that file, because it's possible that the site may have
# locally installed other packages that we don't know about, and we don't want
# to lose track of them in this way.  It's better to just not have the JSON::PP
# package listed; there is apparently no operational consequence for having it
# omitted from this file, other than that it won't show up in the output of
# "perldoc perllocal".
#
# We want to specify new directories, so we pick up their ownership and permissions,
# but leave alone any existing directories.  Diretories handed to tar will
# automatically recursively include everything under them.  Here is that stuff:
#
# perl/lib/site_perl/5.8.9/JSON/PP/
# perl/lib/site_perl/5.8.9/JSON/PP/Boolean.pm
# perl/lib/site_perl/5.8.9/x86_64-linux-thread-multi/auto/JSON/PP/
# perl/lib/site_perl/5.8.9/x86_64-linux-thread-multi/auto/JSON/PP/.packlist
#
$(PATCH_710_STRING)_PERL_PATHS = \
	perl/bin/json_pp						\
	perl/lib/site_perl/5.8.9/JSON/PP.pm				\
	perl/lib/site_perl/5.8.9/JSON/PP				\
	perl/lib/site_perl/5.8.9/x86_64-linux-thread-multi/auto/JSON/PP	\
	perl/man/man1/json_pp.1						\
	perl/man/man3/JSON::PP.3					\
	perl/man/man3/JSON::PP::Boolean.3

PATCH_710_INSTALL_FILES = \
	$(PATCH_FOR_710).noma_fixes/$(PATCH_FOR_710)_install	\
	$(PATCH_FOR_710).noma_fixes/$(PATCH_FOR_710)_uninstall	\
	$(PATCH_FOR_710).noma_fixes/groundwork-$(PATCH_FOR_710)-noma-fix-files.tar.gz

PATCH_711_INSTALL_FILES = \
	$(PATCH_FOR_711).noma_fixes/$(PATCH_FOR_711)_install	\
	$(PATCH_FOR_711).noma_fixes/$(PATCH_FOR_711)_uninstall	\
	$(PATCH_FOR_711).noma_fixes/groundwork-$(PATCH_FOR_711)-noma-fix-files.tar.gz

# These are the full paths of the files we will include in tarballs, as seen
# from the current directory.  These full paths are useful for reference by the
# Makefile in dependency lists, not for constructing the tarballs themselves.
# Some extra work is needed for the perl paths because some of them contain
# colon characters, which will interfere with make's interpretation of the paths
# when it substitutes them into dependency lists.  Those colons must be escaped.
PATCH_710_TARBALL_PERL_PATHS := $(subst :,\:,$($(PATCH_710_STRING)_PERL_PATHS:%=patch-7.1.0-perl/%))
PATCH_710_TARBALL_NOMA_FILES := $($(PATCH_710_STRING)_NOMA_FILES:%=patch-noma/%)
PATCH_711_TARBALL_NOMA_FILES := $($(PATCH_711_STRING)_NOMA_FILES:%=patch-noma/%)

# These are our actual build targets.

patch_tarballs	: noma_tarballs $(PATCH_TARBALLS)

noma_tarballs	: $(NOMA_TARBALLS)

$(PATCH_FOR_710).noma_fixes/groundwork-$(PATCH_FOR_710)-noma-fix-files.tar.gz	: $(PATCH_710_TARBALL_NOMA_FILES) $(PATCH_710_TARBALL_PERL_PATHS)
	cd $(PWD)/patch-noma;       chown nagios:nagios $($(PATCH_710_STRING)_NOMA_FILES)
	cd $(PWD)/patch-7.1.0-perl; chown nagios:nagios $($(PATCH_710_STRING)_PERL_PATHS)
	tar cvfz $@ \
	    -C $(PWD)/patch-noma       $($(PATCH_710_STRING)_NOMA_FILES) \
	    -C $(PWD)/patch-7.1.0-perl $($(PATCH_710_STRING)_PERL_PATHS) \
	    --exclude-vcs

$(PATCH_FOR_711).noma_fixes/groundwork-$(PATCH_FOR_711)-noma-fix-files.tar.gz	: $(PATCH_711_TARBALL_NOMA_FILES)
	cd $(PWD)/patch-noma; chown nagios:nagios $($(PATCH_711_STRING)_NOMA_FILES)
	tar cvfz $@ \
	    -C $(PWD)/patch-noma $($(PATCH_711_STRING)_NOMA_FILES) \
	    --exclude-vcs

$(PATCH_FOR_710).noma_fixes.tar.gz	: $(PATCH_710_INSTALL_FILES)
	chown nagios:nagios $(subst .tar.gz,,$@)
	chown nagios:nagios $(subst .tar.gz,,$@)/$(PATCH_FOR_710)_install
	chown nagios:nagios $(subst .tar.gz,,$@)/$(PATCH_FOR_710)_uninstall
	chown nagios:nagios $(subst .tar.gz,,$@)/groundwork-$(PATCH_FOR_710)-noma-fix-files.tar.gz
	tar cvfz $@ $(subst .tar.gz,,$@) --exclude-vcs

$(PATCH_FOR_711).noma_fixes.tar.gz	: $(PATCH_711_INSTALL_FILES)
	chown nagios:nagios $(subst .tar.gz,,$@)
	chown nagios:nagios $(subst .tar.gz,,$@)/$(PATCH_FOR_711)_install
	chown nagios:nagios $(subst .tar.gz,,$@)/$(PATCH_FOR_711)_uninstall
	chown nagios:nagios $(subst .tar.gz,,$@)/groundwork-$(PATCH_FOR_711)-noma-fix-files.tar.gz
	tar cvfz $@ $(subst .tar.gz,,$@) --exclude-vcs
