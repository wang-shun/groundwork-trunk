# makefile for the generate_quickstart_license package and all the
# Perl stuff it depends on that is either not shipped or out-of-date
# in a standard Perl distribution.

# Edit this definition if need be, when you add or remove image files in
# conjunction with editing the content of outgoing_email_html_template
# and outgoing_email_html_eval_template.
IMAGE_FILES =			\
	gwos-blank-header.gif	\
	key-email-header.gif	\
	keygen-cap.png

# The set of Perl modules installed here is chosen based on what packages
# we need that are either not available in the standard OS-installed Perl,
# or that we believe are probably out-of-date in that distribution.

PERL_TARBALLS =				\
	Net-SSLeay-1.35.tar.gz		\
	IO-Socket-SSL-1.31.tar.gz	\
	version-0.78.tar.gz		\
	Parse-RecDescent-1.962.2.tar.gz	\
	Mail-IMAPClient-3.21.tar.gz	\
	HTML-Parser-3.64.tar.gz		\
	TimeDate-1.20.tar.gz		\
	Test-Simple-0.94.tar.gz		\
	Pod-Escapes-1.04.tar.gz		\
	Pod-Simple-3.13.tar.gz		\
	Test-Pod-1.40.tar.gz		\
	MailTools-2.05.tar.gz		\
	MIME-Types-1.28.tar.gz		\
	Email-Date-Format-1.002.tar.gz	\
	MIME-Base64-3.08.tar.gz		\
	Authen-SASL-2.13.tar.gz		\
	Net-SMTP-SSL-1.01.tar.gz	\
	MIME-Lite-3.027.tar.gz		\
	XML-Simple-2.18.tar.gz		\
	XML-Parser-2.36.tar.gz		\
	libwww-perl-5.834.tar.gz	\
	Config-General-2.44.tar.gz	\
	TypedConfig-1.0.2.tar.gz

# The Perl packages are listed here in an order which should get all
# dependencies built before the packages that depend on them.
PERL_PACKAGES =			\
	Net-SSLeay		\
	IO-Socket-SSL		\
	version			\
	Parse-RecDescent	\
	Mail-IMAPClient		\
	HTML-Parser		\
	TimeDate		\
	Test-Simple		\
	Pod-Escapes		\
	Pod-Simple		\
	Test-Pod		\
	MailTools		\
	MIME-Types		\
	Email-Date-Format	\
	MIME-Base64		\
	Authen-SASL		\
	Net-SMTP-SSL		\
	MIME-Lite		\
	XML-Simple		\
	XML-Parser		\
	libwww-perl		\
	Config-General		\
	TypedConfig

PERL_PACKAGE_DIRECTORIES =		\
	Net-SSLeay-1.35			\
	IO-Socket-SSL-1.31		\
	version-0.78			\
	Parse-RecDescent-1.962.2	\
	Mail-IMAPClient-3.21		\
	HTML-Parser-3.64		\
	TimeDate-1.20			\
	Test-Simple-0.94		\
	Pod-Escapes-1.04		\
	Pod-Simple-3.13			\
	Test-Pod-1.40			\
	MailTools-2.05			\
	MIME-Types-1.28			\
	Email-Date-Format-1.002		\
	MIME-Base64-3.08		\
	Authen-SASL-2.13		\
	Net-SMTP-SSL-1.01		\
	MIME-Lite-3.027			\
	XML-Simple-2.18			\
	XML-Parser-2.36			\
	libwww-perl-5.834		\
	Config-General-2.44		\
	TypedConfig-1.0.2

PERL_INCLUDES =	\
	-I$(PWD)/perl/lib
# "Programming Perl", 3/e, page 497 claims that -I implicitly adds
# platform-sepcific directories.  But we find that not to be the case
# with Perl 5.8.5, so we need to add such directories explicitly here.
ifeq ($(shell uname -p),x86_64)
	PERL_INCLUDES += -I$(PWD)/perl/lib/x86_64-linux-thread-multi
endif
ifeq ($(shell uname -p),i686)
	PERL_INCLUDES += -I$(PWD)/perl/lib/i386-linux-thread-multi
endif

PERL_INSTALL =			\
	LIB=$(PWD)/perl/lib	\
	INSTALLSITEMAN3DIR=$(PWD)/perl/share/man/man3

PACKAGE_TREE =					\
	makefile				\
	generate_quickstart_license		\
	generate_quickstart_license.conf	\
	outgoing_email_html_eval_template	\
	outgoing_email_html_template		\
	outgoing_email_plain_eval_template	\
	outgoing_email_plain_template		\
	quickstart_automailer			\
	$(IMAGE_FILES)				\
	$(PERL_TARBALLS)

TARBALL = generate_quickstart_license.tar.gz

# The default make target here.
build	: perl $(PERL_PACKAGES) config logdirs

tarball	: build
	tar cvfz $(TARBALL) $(PACKAGE_TREE)

perl	:
	mkdir perl

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?
Net-SSLeay	: Net-SSLeay-1.35.tar.gz
	tar xvfz Net-SSLeay-1.35.tar.gz
	cd Net-SSLeay-1.35;				\
	export PERL_MM_USE_DEFAULT=1;			\
	/usr/bin/perl Makefile.PL $(PERL_INSTALL);	\
	make;						\
	make install

IO-Socket-SSL	: IO-Socket-SSL-1.31.tar.gz
	tar xvfz IO-Socket-SSL-1.31.tar.gz
	cd IO-Socket-SSL-1.31;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?
version	: version-0.78.tar.gz
	tar xvfz version-0.78.tar.gz
	cd version-0.78;				\
	/usr/bin/perl Makefile.PL $(PERL_INSTALL);	\
	make;						\
	make install

Parse-RecDescent	: Parse-RecDescent-1.962.2.tar.gz
	tar xvfz Parse-RecDescent-1.962.2.tar.gz
	cd Parse-RecDescent-1.962.2;					\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Mail-IMAPClient	: Mail-IMAPClient-3.21.tar.gz
	tar xvfz Mail-IMAPClient-3.21.tar.gz
	cd Mail-IMAPClient-3.21;					\
	export PERL_MM_USE_DEFAULT=1;					\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

HTML-Parser	: HTML-Parser-3.64.tar.gz
	tar xvfz HTML-Parser-3.64.tar.gz
	cd HTML-Parser-3.64;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

TimeDate	: TimeDate-1.20.tar.gz
	tar xvfz TimeDate-1.20.tar.gz
	cd TimeDate-1.20;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Test-Simple	: Test-Simple-0.94.tar.gz
	tar xvfz Test-Simple-0.94.tar.gz
	cd Test-Simple-0.94;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Pod-Escapes	: Pod-Escapes-1.04.tar.gz
	tar xvfz Pod-Escapes-1.04.tar.gz
	cd Pod-Escapes-1.04;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Pod-Simple	: Pod-Simple-3.13.tar.gz
	tar xvfz Pod-Simple-3.13.tar.gz
	cd Pod-Simple-3.13;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Test-Pod	: Test-Pod-1.40.tar.gz
	tar xvfz Test-Pod-1.40.tar.gz
	cd Test-Pod-1.40;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

MailTools	: MailTools-2.05.tar.gz
	tar xvfz MailTools-2.05.tar.gz
	cd MailTools-2.05;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

MIME-Types	: MIME-Types-1.28.tar.gz
	tar xvfz MIME-Types-1.28.tar.gz
	cd MIME-Types-1.28;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Email-Date-Format	: Email-Date-Format-1.002.tar.gz
	tar xvfz Email-Date-Format-1.002.tar.gz
	cd Email-Date-Format-1.002;					\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

MIME-Base64	: MIME-Base64-3.08.tar.gz
	tar xvfz MIME-Base64-3.08.tar.gz
	cd MIME-Base64-3.08;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Authen-SASL	: Authen-SASL-2.13.tar.gz
	tar xvfz Authen-SASL-2.13.tar.gz
	cd Authen-SASL-2.13;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Net-SMTP-SSL	: Net-SMTP-SSL-1.01.tar.gz
	tar xvfz Net-SMTP-SSL-1.01.tar.gz
	cd Net-SMTP-SSL-1.01;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

MIME-Lite	: MIME-Lite-3.027.tar.gz
	tar xvfz MIME-Lite-3.027.tar.gz
	cd MIME-Lite-3.027;						\
	export PERL_MM_USE_DEFAULT=1;					\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install
	# Local patch to support outgoing SSL connections, done manually
	# here until we get this incorporated into the standard release.
	cd perl/lib/MIME/;						\
	cp -p Lite.pm Lite.pm.orig;					\
	chmod u+w Lite.pm;						\
	sed -e 's/Net::SMTP/Net::SMTP::SSL/g' Lite.pm.orig > Lite.pm

XML-Simple	: XML-Simple-2.18.tar.gz
	tar xvfz XML-Simple-2.18.tar.gz
	cd XML-Simple-2.18;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

XML-Parser	: XML-Parser-2.36.tar.gz
	tar xvfz XML-Parser-2.36.tar.gz
	cd XML-Parser-2.36;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

libwww-perl	: libwww-perl-5.834.tar.gz
	tar xvfz libwww-perl-5.834.tar.gz
	cd libwww-perl-5.834;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

Config-General	: Config-General-2.44.tar.gz
	tar xvfz Config-General-2.44.tar.gz
	cd Config-General-2.44;				\
	/usr/bin/perl Makefile.PL $(PERL_INSTALL);	\
	make;						\
	make install

TypedConfig	: TypedConfig-1.0.2.tar.gz
	tar xvfz TypedConfig-1.0.2.tar.gz
	cd TypedConfig-1.0.2;						\
	/usr/bin/perl $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;								\
	make install

config	: generate_quickstart_license.conf
	chmod 600 generate_quickstart_license.conf

logdirs	:
	mkdir -p ../logs logs

clean	:
	rm -rf $(PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf perl
	rm -f $(TARBALL)
