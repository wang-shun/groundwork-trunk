/******************************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 $Id: check_udp.c,v 1.1.1.1 2005/02/07 19:33:32 hmann Exp $
 
*****************************************************************************/

const char *progname = "check_udp";
const char *revision = "$Revision: 1.1.1.1 $";
const char *copyright = "1999-2004";
const char *email = "nagiosplug-devel@lists.sourceforge.net";

#include "common.h"
#include "netutils.h"
#include "utils.h"

int process_arguments (int, char **);
void print_help (void);
void print_usage (void);

int warning_time = 0;
int check_warning_time = FALSE;
int critical_time = 0;
int check_critical_time = FALSE;
int verbose = FALSE;
int server_port = 0;
char *server_address = NULL;
char *server_expect = NULL;
char *server_send;

int
main (int argc, char **argv)
{
	int result = STATE_UNKNOWN;
	char recv_buffer[MAX_INPUT_BUFFER];

	setlocale (LC_ALL, "");
	bindtextdomain (PACKAGE, LOCALEDIR);
	textdomain (PACKAGE);

	if (process_arguments (argc, argv) == ERROR)
		usage4 (_("Could not parse arguments"));

	/* initialize alarm signal handling */
	signal (SIGALRM, socket_timeout_alarm_handler);

	/* set socket timeout */
	alarm (socket_timeout);

	time (&start_time);
	result = process_udp_request (server_address, server_port, server_send,
			recv_buffer, MAX_INPUT_BUFFER - 1);
			
	time (&end_time);

	if (result != STATE_OK) {
		printf (_("No response from host on port %d\n"), server_port);
		result = STATE_CRITICAL;
	}

	else {

		/* check to see if we got the response we wanted */
		if (server_expect) {
			if (!strstr (recv_buffer, server_expect)) {
				printf (_("Invalid response received from host on port %d\n"),
								server_port);
				result = STATE_CRITICAL;
			}
		}
	}

	/* we connected, so close connection before exiting */
	if (result == STATE_OK) {

		if (check_critical_time == TRUE
				&& (end_time - start_time) > critical_time) result = STATE_CRITICAL;
		else if (check_warning_time == TRUE
						 && (end_time - start_time) > warning_time) result =
				STATE_WARNING;

		printf (_("Connection %s on port %d - %d second response time\n"),
						(result == STATE_OK) ? _("accepted") : _("problem"), server_port,
						(int) (end_time - start_time));
	}

	/* reset the alarm */
	alarm (0);

	return result;
}



/* process command-line arguments */
int
process_arguments (int argc, char **argv)
{
	int c;

	int option = 0;
	static struct option longopts[] = {
		{"hostname", required_argument, 0, 'H'},
		{"critical", required_argument, 0, 'c'},
		{"warning", required_argument, 0, 'w'},
		{"timeout", required_argument, 0, 't'},
		{"port", required_argument, 0, 'p'},
		{"expect", required_argument, 0, 'e'},
		{"send", required_argument, 0, 's'},
		{"verbose", no_argument, 0, 'v'},
		{"version", no_argument, 0, 'V'},
		{"help", no_argument, 0, 'h'},
		{0, 0, 0, 0}
	};

	if (argc < 2)
		usage ("\n");

	for (c = 1; c < argc; c++) {
		if (strcmp ("-to", argv[c]) == 0)
			strcpy (argv[c], "-t");
		else if (strcmp ("-wt", argv[c]) == 0)
			strcpy (argv[c], "-w");
		else if (strcmp ("-ct", argv[c]) == 0)
			strcpy (argv[c], "-c");
	}

	while (1) {
		c = getopt_long (argc, argv, "+hVvH:e:s:c:w:t:p:", longopts, &option);

		if (c == -1 || c == EOF || c == 1)
			break;

		switch (c) {
		case '?':									/* print short usage statement if args not parsable */
			usage2 (_("Unknown argument"), optarg);
		case 'h':									/* help */
			print_help ();
			exit (STATE_OK);
		case 'V':									/* version */
			print_revision (progname, revision);
			exit (STATE_OK);
		case 'v':									/* verbose mode */
			verbose = TRUE;
			break;
		case 'H':									/* hostname */
			if (is_host (optarg) == FALSE)
				usage2 (_("Invalid hostname/address"), optarg);
			server_address = optarg;
			break;
		case 'c':									/* critical */
			if (!is_intnonneg (optarg))
				usage4 (_("Critical threshold must be a positive integer"));
			else
				critical_time = atoi (optarg);
			check_critical_time = TRUE;
			break;
		case 'w':									/* warning */
			if (!is_intnonneg (optarg))
				usage4 (_("Warning threshold must be a positive integer"));
			else
				warning_time = atoi (optarg);
			check_warning_time = TRUE;
			break;
		case 't':									/* timeout */
			if (!is_intnonneg (optarg))
				usage2 (_("Timeout interval must be a positive integer"), optarg);
			else
				socket_timeout = atoi (optarg);
			break;
		case 'p':									/* port */
			if (!is_intnonneg (optarg))
				usage4 (_("Port must be a positive integer"));
			else
				server_port = atoi (optarg);
			break;
		case 'e':									/* expect */
			server_expect = optarg;
			break;
		case 's':									/* send */
			server_send = optarg;
			break;
		}
	}

	c = optind;
	if (server_address == NULL && c < argc && argv[c]) {
		if (is_host (argv[c]) == FALSE)
			usage2 (_("Invalid hostname/address"), optarg);
		server_address = argv[c++];
	}

	if (server_address == NULL)
		usage4 (_("Hostname was not supplied"));

	if (server_send == NULL)
		server_send = strdup("");

	return c;
}



void
print_help (void)
{
	print_revision (progname, revision);

	printf ("Copyright (c) 1999 Ethan Galstad\n");
	printf (COPYRIGHT, copyright, email);

	printf (_("\
	This plugin tests an UDP connection with the specified host.\n\n"));

	print_usage ();

	printf (_(UT_HELP_VRSN));

	printf (_(UT_HOST_PORT), 'p', "none");

	printf (_("\
 -e, --expect=STRING <optional>\n\
    String to expect in first line of server response\n\
 -s, --send=STRING <optional>\n\
    String to send to the server when initiating the connection\n"));

	printf (_(UT_WARN_CRIT));

	printf (_(UT_TIMEOUT), DEFAULT_SOCKET_TIMEOUT);

	printf (_(UT_VERBOSE));

	printf (_("\
This plugin will attempt to connect to the specified port on the host.\n\
Successful connects return STATE_OK, refusals and timeouts return\n\
STATE_CRITICAL, other errors return STATE_UNKNOWN.\n\n"));

	printf(_(UT_SUPPORT));
}


/* Original Command line: 
   check_udp <host_address> [-p port] [-s send] [-e expect] \
   [-wt warn_time] [-ct crit_time] [-to to_sec] */
void
print_usage (void)
{
	printf ("\
Usage: %s -H <host_address> [-p port] [-w warn_time] [-c crit_time]\n\
                  [-e expect] [-s send] [-t to_sec] [-v]\n", progname);
}
