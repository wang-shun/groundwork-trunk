nagios-plugins 1.4.5, 
 Compiled for Windows / Cygwin by Chris (Wolf) <spamtrap@psychoticwolf.net>
    gcc version 3.4.4 (cygming special, gdc 0.12, using dmd 0.125)
    $ ./configure --disable-nls

==
Testing Notes:
==
check_apt - OK, but no apt-get, this isn't a linux box.
check_breeze - OK, but no breezecom hardware to check against.
check_by_ssh - OK.
check_clamd - OK, symlink to tcp
check_disk - OK.
check_disk_smb - OK. Testing Error, Permission denied, so not thoroughly tested.
check_dns - OK. 
check_dummy - OK.
check_file_age - OK.
check_flexlm - OK, but no license, so untested.
check_ftp - OK, symlink to tcp
check_http - OK
check_ifoperstatus - OK, perl, seems pretty linux specific.
check_ifstatus - OK, same as above.
check_imap - OK, symlink to tcp
check_ircd - OK.
check_jabber - OK, symlink to tcp. (need a jabber server to test more)
check_load - OK, linuxy
check_log - Runs, not sure how to test.
check_mailq - Runs, no mailq command this isn't a sendmail/qmail system.
check_mrtg - OK, didn't thoroughly test.
check_mrtgtraf - OK, didn't thoroughly test.
check_mysql - OK. doesn't like localhost though.
check_mysql_query - OK.
check_nntp - OK, symlink to tcp.
check_nntps - OK, tcp, unable to test due to no nttps server.
check_nt - OK, unable to test, no NSClient.
check_ntp - OK.
check_nwstat - OK, no novell server to test with.
check_oracle - Runs, no oracle server to test with.
check_overcr - Runs, no remote daemon to test with.
check_ping - ERROR. No ping command it likes.
check_pop - OK. tcp.
check_real - OK. no rtsp server to test with.
check_rpc - OK, unsure how to test.
check_sensors - OK, lmsensors not installed, no test.
check_simap - OK, tcp, no server to test with.
check_spop - OK, tcp, no server to test with
check_ssh - OK.
check_ssmtp - OK, tcp, no server to test with.
check_swap - OK.
check_tcp - OK.
check_time - Runs, unsure how to test.
check_udp - OK, tcp.
check_users - OK.
check_wave - Runs, missing snmpget, lots of errors
negate - OK, not sure how to test.
urlize - OK.

These 2 are not included because of build problems, the errors are included in this file:
check_icmp - ERROR, couldn't build, cygwin lacks dependendies this needs.
check_smtp - ERROR. Could not compile, unknown reason.

===============================
Cygwin Dependant DLLs Required:
===============================
cygwin1.dll - Cygwin Posix Emulation DLL (1005.24.0.0
cygcrypto-0.9.8.dll - Cygwin OpenSSL DLL (0.9.8e-3)
cygssl-0.9.8.dll - Cygwin OpenSSL DLL (0.9.8e-3)
cygz.dll - Cygwin zlib compression library. (1.2.3-2)

==============================
Tip to compiling under cygwin:
==============================
After running configure, go to plugins/makefile, and edit libexec_PROGRAMS (line 39) so that 
negate$(EXEEXT) urlize$(EXEEXT) are at the end. also append $(EXEEXT) to any that do not have
it, such as check_mysql.

===============
Compile Errors: (for reference, not expected to be actually useful.)
===============

check_smtp compile error:
make[1]: [check_smtp.exe] Error 1 (ignored)
make[2]: Entering directory `/cygdrive/g/nagios-dev/nagios-plugins-1.4.5/plugins
'
if gcc -DLOCALEDIR=\"/cygdrive/g/nagios-dev/nagios/share/locale\" -DHAVE_CONFIG_
H -I. -I. -I.. -I.. -I../lib -I../intl   -I/usr/include    -g -O2 -MT check_smtp
.o -MD -MP -MF ".deps/check_smtp.Tpo" -c -o check_smtp.o check_smtp.c; \
        then mv -f ".deps/check_smtp.Tpo" ".deps/check_smtp.Po"; else rm -f ".de
ps/check_smtp.Tpo"; exit 1; fi
check_smtp.c: In function `main':
check_smtp.c:194: warning: assignment makes pointer from integer without a cast
check_smtp.c:196: error: dereferencing pointer to incomplete type
check_smtp.c:386: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:391: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:401: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:411: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:424: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:433: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:445: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:454: warning: assignment discards qualifiers from pointer target ty
pe
check_smtp.c:461: warning: assignment discards qualifiers from pointer target ty
pe
make[2]: [check_smtp.o] Error 1


check_icmp error:
check_icmp.c:188: warning: "struct icmp" declared inside parameter list
check_icmp.c:188: warning: its scope is only this definition or declaration, whi
ch is probably not what you want
check_icmp.c: In function `get_icmp_error_msg':
check_icmp.c:244: error: `ICMP_UNREACH' undeclared (first use in this function)
check_icmp.c:244: error: (Each undeclared identifier is reported only once
check_icmp.c:244: error: for each function it appears in.)
check_icmp.c:246: error: `ICMP_UNREACH_NET' undeclared (first use in this functi
on)
check_icmp.c:247: error: `ICMP_UNREACH_HOST' undeclared (first use in this funct
ion)
check_icmp.c:248: error: `ICMP_UNREACH_PROTOCOL' undeclared (first use in this f
unction)
check_icmp.c:249: error: `ICMP_UNREACH_PORT' undeclared (first use in this funct
ion)
check_icmp.c:250: error: `ICMP_UNREACH_NEEDFRAG' undeclared (first use in this f
unction)
check_icmp.c:251: error: `ICMP_UNREACH_SRCFAIL' undeclared (first use in this fu
nction)
check_icmp.c:266: error: `ICMP_TIMXCEED' undeclared (first use in this function)

check_icmp.c:270: error: `ICMP_TIMXCEED_INTRANS' undeclared (first use in this f
unction)
check_icmp.c:271: error: `ICMP_TIMXCEED_REASS' undeclared (first use in this fun
ction)
check_icmp.c:276: error: `ICMP_SOURCEQUENCH' undeclared (first use in this funct
ion)
check_icmp.c:277: error: `ICMP_REDIRECT' undeclared (first use in this function)

check_icmp.c:278: error: `ICMP_PARAMPROB' undeclared (first use in this function
)
check_icmp.c:281: error: `ICMP_TSTAMP' undeclared (first use in this function)
check_icmp.c:282: error: `ICMP_TSTAMPREPLY' undeclared (first use in this functi
on)
check_icmp.c:283: error: `ICMP_IREQ' undeclared (first use in this function)
check_icmp.c:284: error: `ICMP_IREQREPLY' undeclared (first use in this function
)
check_icmp.c:285: error: `ICMP_MASKREQ' undeclared (first use in this function)
check_icmp.c:286: error: `ICMP_MASKREPLY' undeclared (first use in this function
)
check_icmp.c: At top level:
check_icmp.c:294: warning: "struct icmp" declared inside parameter list
check_icmp.c:295: error: conflicting types for 'handle_random_icmp'
check_icmp.c:188: error: previous declaration of 'handle_random_icmp' was here
check_icmp.c:295: error: conflicting types for 'handle_random_icmp'
check_icmp.c:188: error: previous declaration of 'handle_random_icmp' was here
check_icmp.c: In function `handle_random_icmp':
check_icmp.c:300: error: dereferencing pointer to incomplete type
check_icmp.c:300: error: `ICMP_ECHO' undeclared (first use in this function)
check_icmp.c:300: error: dereferencing pointer to incomplete type
check_icmp.c:318: error: dereferencing pointer to incomplete type
check_icmp.c:318: error: `ICMP_UNREACH' undeclared (first use in this function)
check_icmp.c:318: error: dereferencing pointer to incomplete type
check_icmp.c:318: error: `ICMP_TIMXCEED' undeclared (first use in this function)

check_icmp.c:319: error: dereferencing pointer to incomplete type
check_icmp.c:319: error: `ICMP_SOURCEQUENCH' undeclared (first use in this funct
ion)
check_icmp.c:319: error: dereferencing pointer to incomplete type
check_icmp.c:319: error: `ICMP_PARAMPROB' undeclared (first use in this function
)
check_icmp.c:327: error: dereferencing pointer to incomplete type
check_icmp.c:327: error: dereferencing pointer to incomplete type
check_icmp.c:328: error: dereferencing pointer to incomplete type
check_icmp.c:335: error: dereferencing pointer to incomplete type
check_icmp.c:338: error: dereferencing pointer to incomplete type
check_icmp.c:338: error: dereferencing pointer to incomplete type
check_icmp.c:349: error: dereferencing pointer to incomplete type
check_icmp.c:357: error: dereferencing pointer to incomplete type
check_icmp.c:358: error: dereferencing pointer to incomplete type
check_icmp.c: In function `main':
check_icmp.c:569: error: `ICMP_MINLEN' undeclared (first use in this function)
check_icmp.c:571: error: invalid application of `sizeof' to incomplete type `icm
p'
check_icmp.c:572: error: invalid application of `sizeof' to incomplete type `icm
p'
check_icmp.c: In function `wait_for_reply':
check_icmp.c:720: error: `ICMP_MINLEN' undeclared (first use in this function)
check_icmp.c:740: error: dereferencing pointer to incomplete type
check_icmp.c:741: warning: passing arg 1 of `handle_random_icmp' from incompatib
le pointer type
check_icmp.c:745: error: dereferencing pointer to incomplete type
check_icmp.c:745: error: `ICMP_ECHOREPLY' undeclared (first use in this function
)
check_icmp.c:745: error: dereferencing pointer to incomplete type
check_icmp.c:747: warning: passing arg 1 of `handle_random_icmp' from incompatib
le pointer type
check_icmp.c:752: error: dereferencing pointer to incomplete type
check_icmp.c:754: error: dereferencing pointer to incomplete type
check_icmp.c: In function `send_icmp_ping':
check_icmp.c:814: error: dereferencing pointer to incomplete type
check_icmp.c:814: error: `ICMP_ECHO' undeclared (first use in this function)
check_icmp.c:815: error: dereferencing pointer to incomplete type
check_icmp.c:816: error: dereferencing pointer to incomplete type
check_icmp.c:817: error: dereferencing pointer to incomplete type
check_icmp.c:818: error: dereferencing pointer to incomplete type
check_icmp.c:819: error: dereferencing pointer to incomplete type
check_icmp.c:822: error: dereferencing pointer to incomplete type
make[2]: [check_icmp.o] Error 1 (ignored)