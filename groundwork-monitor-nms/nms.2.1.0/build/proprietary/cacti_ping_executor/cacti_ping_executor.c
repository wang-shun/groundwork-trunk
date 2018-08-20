#include <stdio.h>

/*
*        change mode on this executable to be u+s.
*/

int getuid(void);
int geteuid(void);

int
main (int argc, char *argv[])
{
	char	command[256];
	char 	*ping_path = "/usr/local/groundwork/nms/applications/cacti/cli/host_icmp_ping.php";

	if (argc == 4)
	{
		sprintf(command, "%s --host-id=%s --timeout=%s --retries=%s", ping_path, argv[1], argv[2], argv[3]);
        	setuid(geteuid());
        	system(command);
	}
        exit (0);
}

