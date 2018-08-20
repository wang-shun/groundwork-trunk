CC=gcc 
CFLAGS=
cacti_ping_executor: cacti_ping_executor.o

clean:
	rm -f cacti_ping_executor cacti_ping_executor.o

install: copy_executor copy_php chown chmod

copy_executor:
	cp cacti_ping_executor /usr/local/groundwork/nms/applications/cacti/cli
copy_php:
	cp host_icmp_ping.php /usr/local/groundwork/nms/applications/cacti/cli
chown:
	chown root:root /usr/local/groundwork/nms/applications/cacti/cli/cacti_ping_executor
chmod:
	chmod u+s /usr/local/groundwork/nms/applications/cacti/cli/cacti_ping_executor

all: cacti_ping_executor install

