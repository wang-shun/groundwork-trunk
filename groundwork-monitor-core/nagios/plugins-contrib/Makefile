CC=gcc
CFLAGS= -DHAVE_CONFIG_H -I.. -I../lib -I/usr/include/ldap -I../plugins 

all: check_cluster check_cluster2 check_timeout

check_cluster:
	$(CC) $(CFLAGS) -o $@ $@.c
check_cluster2:
	$(CC) $(CFLAGS) -o $@ $@.c
check_timeout:
	$(CC) $(CFLAGS) -o $@ $@.c

