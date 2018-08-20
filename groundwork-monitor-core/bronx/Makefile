#
# Makefile for Bronx, a Nagios Event Broker
#
# The main targets that can be executed directly are:
#
#     clean     remove built files from a configuration
#     all       build all configurations
#
# Copyright (c) 2007-2017 Groundwork Open Source
#
#	2007-09-20 DEF;	Created.
#	20??-??-?? ???;	Lots of improvements along the way.
#	2017-01-31 GH;	Updated to support Nagios 4.2.4.
#

# Note 0:  Bronx references Nagios header files, so the full Nagios
# distribution must be prepared separately, outside of this Makefile,
# before you attempt to compile Bronx.  The steps to do so for Nagios
# 3.X are:
#
# (a) Un-tar the Nagios distribution.
#
# (b) If you were actually going to compile Nagios itself, you would
#     need to apply whatever patches that GroundWork supplies to fix
#     various problems and improve performance.  This page has details:
#     http://gwiki/index.php/Nagios_patches
#
# (c) You need to run "./configure" in the nagios distribution,
#     specifying the location where it would reside.  This command:
#
#         ./configure --prefix=/usr/local/groundwork/nagios
#
#     will produce the properly configured Nagios header files for your
#     platform.
#
# (d) The generated include/locations.h header fle will have inappropriate
#     600 permissions.  Change them to 644 at this time.
#
# (e) The Nagios header files must then must be copied from the include/
#     subdirectory to the /usr/local/groundwork/common/include/nagios/
#     directory (which you will need to make first) so they are accessible
#     to the Event Broker (Bronx) compilation.
#
# For Nagios 4.X, the complete set of build and installation steps for
# Nagios, including application of all of our GroundWork patches, has all
# been encapsulated into a separate Makefile associated with the patches
# that we apply to each specific Nagios release.  The whole procedure is
# considerably more complicated, but it's all automated now.  The only
# part that is not automated there is dealing with whatever BitRock does
# to impose their wrapper (splitting the "nagios" binary into a small
# "nagios" wrapper and a separate ".nagios.bin" binary for the rest of
# the program).
#
# As of at least Nagios 4.2.4, certain other Nagios header files must be
# copied over to where they are publicly available (all but the lib/t-utils.h
# header are referenced by other header files that are pulled into the Bronx
# build).  Installation of these headers is also handled automatically by the
# separate Nagios-build Makefile.
#
#     mkdir /usr/local/groundwork/common/include/nagios/lib
#     chown nagios:nagios /usr/local/groundwork/common/include/nagios/lib
#     cp -p nagios-4.2.4/lib/*.h /usr/local/groundwork/common/include/nagios/lib

# Note 1:  Compiling Bronx against Nagios 3.3.1 or later requires the prior
# application of nagios-3.5.0-non-static-logging.patch (or an equivalent patch
# for the particular Nagios release), which makes write_to_logs_and_console()
# once again a non-static function.  Other Nagios patches are required as well,
# to make Bronx function correctly after it is compiled.

# Note 2:  The NAGIOS_3_4_4_OR_LATER flag must be defined in this Makefile,
# as it is now by default, for the indicated releases.  If you need to go
# back and compile Bronx for some earlier release, this flag will need to
# be disabled for such a compilation.

# Note 3:  The NAGIOS_4_2_4_OR_LATER flag must be defined in this Makefile,
# as it is now by default, for the indicated releases.  If you need to go
# back and compile Bronx for some earlier release, this flag will need to
# be disabled for such a compilation.  Note that we have not attempted to
# compile Bronx against any Nagios release after 3.5.1 and before 4.2.4,
# so the fact that the flag says "4.2.4 or later" simply reflects what we
# have tested against, not necessarily the exact transition point at which
# the compilation changes would need to be effective in the full train of
# possible Nagios releases we might have compiled Bronx against.

# Note 4:  When compling Bronx, this line below:
#     LDLIBSOPTIONS += -Wl,-R$(GROUNDWORK_BASE)/common/lib
# should generally be uncommented, to force the use of GroundWork-supplied
# linked-to libraries instead of system-supplied copies.  I'm not sure why
# this option has previously been commented out in our standard checked-in
# copy of this Makefile.

# Environment
MKDIR = mkdir
CP = cp
CC = gcc
CCC = g++
CXX = g++

# Where we live.
GROUNDWORK_BASE = /usr/local/groundwork

# Object Directory
OBJECTDIR = build

# Target dir
TARGETDIR = dist

# Object Files
OBJECTFILES = \
        ${OBJECTDIR}/bronx_nagios.o		\
        ${OBJECTDIR}/bronx_listener_utils.o	\
        ${OBJECTDIR}/bronx_listener.o		\
        ${OBJECTDIR}/bronx_cmd_acceptor.o	\
        ${OBJECTDIR}/bronx.o			\
        ${OBJECTDIR}/bronx_thread.o		\
        ${OBJECTDIR}/bronx_listener_netutils.o	\
        ${OBJECTDIR}/bronx_log.o		\
        ${OBJECTDIR}/bronx_config.o		\
        ${OBJECTDIR}/bronx_admin.o		\
        ${OBJECTDIR}/bronx_processors.o		\
        ${OBJECTDIR}/bronx_utils.o		\
        ${OBJECTDIR}/bronx_cmd_acceptor_utils.o	\
        ${OBJECTDIR}/bronx_safe_fork.o

# C Compiler Flags
#
# In Nagios 3.X, _REENTRANT is defined in one of the Nagios header files that we include
# in all our Bronx code.  But we don't want to depend on that to guarantee the safety of
# our Bronx compilation, because for one thing, that symbol is no longer so defined in
# the Nagios 4.X header files.  We do need that symbol in play in the compilations of
# both Nagios and Bronx to ensure that both are set up for multi-threaded code.  So we
# define this symbol explicitly here.
#
# _GNU_SOURCE is similarly defined in one of the Nagios header files (this time in both
# Nagios 3.X and Nagios 4.X), and again we want to guarantee compatibility with that
# compilation, so we set it explictly here.
CFLAGS = -D_REENTRANT -D_GNU_SOURCE -fPIC -Wall -Werror
#
# Add the following flag if compiling Bronx to be used with Nagios 3.4.4 or a later release.
CFLAGS += -DNAGIOS_3_4_4_OR_LATER
#
# Add the following flag if compiling Bronx to be used with Nagios 4.2.4 or a later release.
CFLAGS += -DNAGIOS_4_2_4_OR_LATER
#
# Add the following flag if compiling Bronx to be used with Nagios 4.2.4 or a later release,
# and you wish to switch to the double-linked check_result list (which was implemented for
# possibly greater efficiency during list insertions).  For Bronx to link with Nagios and run
# successfully, this setting must match the way that Nagios itself was compiled -- if Nagios
# was compiled with this flag, then Bronx must be compiled with this flag, and vice versa.
CFLAGS += -DUSE_CHECK_RESULT_DOUBLE_LINKED_LIST

# Link Libraries and Options
#
# Base options to access libraries needed by Bronx.
LDLIBSOPTIONS = -L$(GROUNDWORK_BASE)/common/lib -lapr-1 -laprutil-1 -lmcrypt
#
# We use the following as well to nail down linkages to GroundWork-supplied libraries.
LDLIBSOPTIONS += -Wl,-R$(GROUNDWORK_BASE)/common/lib

# Header files
INCLUDEFILES = \
	-I$(GROUNDWORK_BASE)/common/lib/glib-2.0/include/	\
	-I$(GROUNDWORK_BASE)/common/include/glib-2.0/		\
	-I$(GROUNDWORK_BASE)/common/include			\
	-I$(GROUNDWORK_BASE)/common/include/apr-1		\
	-I$(GROUNDWORK_BASE)/common/include/nagios

# Let's make the default make target always safe to run.
#
default	: help

help	:
	@echo ""
	@echo "Primary build targets:"
	@echo ""
	@echo "    make help"
	@echo "    make all"
	@echo "    make verify"
	@echo "    make clean"
	@echo ""
	@echo "Primary install target:"
	@echo "(must be run as root or nagios)"
	@echo ""
	@echo "    make install"
	@echo ""

all	: ${TARGETDIR}/libbronx.so

${TARGETDIR}/libbronx.so	: ${OBJECTFILES}
	${MKDIR} -p ${TARGETDIR}
	${LINK.c} -shared -o ${TARGETDIR}/libbronx.so -fPIC ${OBJECTFILES} ${LDLIBSOPTIONS}

# We could be more discriminating about exactly which header files are used to compile each
# object file, but this thing as a whole compiles so quickly that there's no point in doing so.
# The important thing is to at least establish some superset of the existing dependencies, so
# we don't get misled into believing that everything is up-to-date when it's not.
${OBJECTFILES}		: *.h

${OBJECTDIR}/%.o	: %.c
	${MKDIR} -p ${OBJECTDIR}
	$(COMPILE.c) -O2 ${INCLUDEFILES} ${CFLAGS} -o $@ $(*F).c

verify	: ${TARGETDIR}/libbronx.so
	@echo "Checking for a reference to the pthread library in the compiled Bronx library ..."
	@if ldd ${TARGETDIR}/libbronx.so | fgrep libpthread.so; then \
	    echo "... pthread library reference is present."; \
	else \
	    echo "ERROR:  pthread library reference is missing in ${TARGETDIR}/libbronx.so !"; \
	    false; \
	fi  

install	: ${TARGETDIR}/libbronx.so
	@if [ "`id -u`" -ne 0 -a "`id -un`" != nagios ]; then \
	    echo "ERROR:  You must be either root or nagios to install the Bronx library."; \
	    exit 1; \
	fi
	cp -p ${TARGETDIR}/libbronx.so $(GROUNDWORK_BASE)/common/lib
	chown nagios:nagios            $(GROUNDWORK_BASE)/common/lib/libbronx.so

clean	:
	${RM} -r ${OBJECTDIR} ${TARGETDIR}
	${RM} ${TARGETDIR}/libbronx.so
