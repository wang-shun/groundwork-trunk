# makefile for nms builds

# Copyright 2008 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must match the ${version}
# defined in each rpmsetup/groundwork-nms-*-%{version}.spec file.
NMS_RELEASE = 2.1.0

#---------------------------------------------------------------#

default         : release

install         :

clean           : rpmclean

buildclean      : clean

#---------------------------------------------------------------#

nms_build	:
		cd bin; ./clean_all.sh; ./build_all.sh

#---------------------------------------------------------------#

# Note:  The XXX_INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/groundwork-nms-xxx.rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
             BUILD_ROOT = $(PWD)/bin
      CORE_INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-nms-core-$(NMS_RELEASE)/INSTALL_ROOT
     CACTI_INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-nms-cacti-$(NMS_RELEASE)/INSTALL_ROOT
      NEDI_INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-nms-nedi-$(NMS_RELEASE)/INSTALL_ROOT
      NTOP_INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-nms-ntop-$(NMS_RELEASE)/INSTALL_ROOT
WEATHERMAP_INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-nms-weathermap-$(NMS_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork

# FIX THIS:  This set of commands for nms_install is a hack.  We should be building directly into these target
# directories [rather than into $(GWPATH)], so there should be no need to copy these file trees here.

# FIX THIS:  we had to manually give 644 permissions to this file:
# $(GWPATH/nms)/applications/cacti/cacti.sql
# so we could copy it here.

# FIX THIS:  put back this dependency
nms_install	: nms_build
# nms_install	:
		@:
		mkdir -p       $(CORE_INSTALL_ROOT)$(GWPATH)/enterprise
		mkdir -p       $(CORE_INSTALL_ROOT)$(GWPATH)/nms/applications
		mkdir -p       $(CORE_INSTALL_ROOT)$(GWPATH)/nms/tools
		mkdir -p      $(CACTI_INSTALL_ROOT)$(GWPATH)/nms/applications/cacti
		mkdir -p      $(CACTI_INSTALL_ROOT)$(GWPATH)/nms/applications/cacti-spine
		mkdir -p       $(NEDI_INSTALL_ROOT)$(GWPATH)/nms/applications/nedi
		mkdir -p       $(NTOP_INSTALL_ROOT)$(GWPATH)/nms/applications/ntop
		mkdir -p $(WEATHERMAP_INSTALL_ROOT)$(GWPATH)/nms/applications/weathermap
		@:
		cp -pr $(GWPATH)/enterprise                         $(CORE_INSTALL_ROOT)$(GWPATH)/
		cp -pr $(GWPATH)/nms/tools                          $(CORE_INSTALL_ROOT)$(GWPATH)/nms
		cp -pr $(GWPATH)/nms/applications/cacti-spine       $(CORE_INSTALL_ROOT)$(GWPATH)/nms/applications
		cp -pr $(GWPATH)/nms/applications/cacti            $(CACTI_INSTALL_ROOT)$(GWPATH)/nms/applications
		cp -pr $(GWPATH)/nms/applications/nedi              $(NEDI_INSTALL_ROOT)$(GWPATH)/nms/applications
		cp -pr $(GWPATH)/nms/applications/ntop              $(NTOP_INSTALL_ROOT)$(GWPATH)/nms/applications
		cp -pr $(GWPATH)/nms/applications/weathermap  $(WEATHERMAP_INSTALL_ROOT)$(GWPATH)/nms/applications

#---------------------------------------------------------------#

# Resolve some build-portability issues.

PLATFORM = $(shell /usr/lib/rpm/config.guess | /usr/bin/gawk -F- '{print $$2}')
ifeq ($(PLATFORM),redhat)
PLATFORM_RPMRC_FILES = /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc
endif
ifeq ($(PLATFORM),suse)
PLATFORM_RPMRC_FILES = /usr/lib/rpm/rpmrc
endif
ifndef PLATFORM_RPMRC_FILES
$(error This platform ("$(PLATFORM)") is not yet supported)
endif

#---------------------------------------------------------------#

rpms		: nms_install nms_core_rpm nms_cacti_rpm nms_nedi_rpm nms_ntop_rpm nms_weathermap_rpm

nms_core_rpm	:
		$(BUILD_ROOT)/make_core_rpm.sh $(NMS_RELEASE)

nms_cacti_rpm	:
		rpmbuild												\
			--rcfile $(PLATFORM_RPMRC_FILES):$(PWD)/rpmsetup/groundwork-nms-core.rpmrc	\
			--dbpath $(PWD)/rpmsetup							\
			--quiet									\
			-bb --short-circuit rpmsetup/groundwork-nms-cacti-$(NMS_RELEASE).spec 2>&1

nms_nedi_rpm	:
		rpmbuild												\
			--rcfile $(PLATFORM_RPMRC_FILES):$(PWD)/rpmsetup/groundwork-nms-core.rpmrc	\
			--dbpath $(PWD)/rpmsetup										\
			--quiet												\
			-bb --short-circuit rpmsetup/groundwork-nms-nedi-$(NMS_RELEASE).spec 2>&1

nms_ntop_rpm	:
		rpmbuild												\
			--rcfile $(PLATFORM_RPMRC_FILES):$(PWD)/rpmsetup/groundwork-nms-core.rpmrc	\
			--dbpath $(PWD)/rpmsetup										\
			--quiet												\
			-bb --short-circuit rpmsetup/groundwork-nms-ntop-$(NMS_RELEASE).spec 2>&1

nms_weathermap_rpm	:
		rpmbuild												\
			--rcfile $(PLATFORM_RPMRC_FILES):$(PWD)/rpmsetup/groundwork-nms-core.rpmrc	\
			--dbpath $(PWD)/rpmsetup										\
			--quiet												\
			-bb --short-circuit rpmsetup/groundwork-nms-weathermap-$(NMS_RELEASE).spec 2>&1

rpmclean	:
		rm -rf rpmbuild

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the NMS_RELEASE value above
# * make sure every rpmsetup/groundwork-nms-*-$(NMS_RELEASE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

SPECFILES =	\
		rpmsetup/groundwork-nms-core-$(NMS_RELEASE).spec	\
		rpmsetup/groundwork-nms-cacti-$(NMS_RELEASE).spec	\
		rpmsetup/groundwork-nms-nedi-$(NMS_RELEASE).spec	\
		rpmsetup/groundwork-nms-ntop-$(NMS_RELEASE).spec	\
		rpmsetup/groundwork-nms-weathermap-$(NMS_RELEASE).spec

release		: $(SPECFILES)
		make buildclean
		make rpms
