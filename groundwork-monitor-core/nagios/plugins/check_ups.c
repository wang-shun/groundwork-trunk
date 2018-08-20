/******************************************************************************

 check_ups

 Program: Network UPS Tools plugin for Nagios
 License: GPL
 Copyright (c) 2000 Tom Shields
               2004 Alain Richard <alain.richard@equation.fr>
               2004 Arnaud Quette <arnaud.quette@mgeups.com>

 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or (at
 your option) any later version.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 $Id: check_ups.c,v 1.1.1.1 2005/02/07 19:33:32 hmann Exp $
 
******************************************************************************/

const char *progname = "check_ups";
const char *revision = "$Revision: 1.1.1.1 $";
const char *copyright = "2000-2004";
const char *email = "nagiosplug-devel@lists.sourceforge.net";

#include "common.h"
#include "netutils.h"
#include "utils.h"

enum {
	PORT = 3493
};

#define CHECK_NONE	 0

#define UPS_NONE     0   /* no supported options */
#define UPS_UTILITY  1   /* supports utility line voltage */
#define UPS_BATTPCT  2   /* supports percent battery remaining */
#define UPS_STATUS   4   /* supports UPS status */
#define UPS_TEMP     8   /* supports UPS temperature */
#define UPS_LOADPCT	16   /* supports load percent */

#define UPSSTATUS_NONE       0
#define UPSSTATUS_OFF        1
#define UPSSTATUS_OL         2
#define UPSSTATUS_OB         4
#define UPSSTATUS_LB         8
#define UPSSTATUS_CAL       16
#define UPSSTATUS_RB        32  /*Replace Battery */
#define UPSSTATUS_BYPASS    64
#define UPSSTATUS_OVER     128
#define UPSSTATUS_TRIM     256
#define UPSSTATUS_BOOST    512
#define UPSSTATUS_CHRG    1024
#define UPSSTATUS_DISCHRG 2048
#define UPSSTATUS_UNKOWN  4096

enum { NOSUCHVAR = ERROR-1 };

int server_port = PORT;
char *server_address;
char *ups_name = NULL;
double warning_value = 0.0;
double critical_value = 0.0;
int check_warn = FALSE;
int check_crit = FALSE;
int check_variable = UPS_NONE;
int supported_options = UPS_NONE;
int status = UPSSTATUS_NONE;

double ups_utility_voltage = 0.0;
double ups_battery_percent = 0.0;
double ups_load_percent = 0.0;
double ups_temperature = 0.0;
char *ups_status;
int temp_output_c = 0;

int determine_status (void);
int get_ups_variable (const char *, char *, size_t);

int process_arguments (int, char **);
int validate_arguments (void);
void print_help (void);
void print_usage (void);

int
main (int argc, char **argv)
{
	int result = STATE_UNKNOWN;
	char *message;
	char *data;
	char temp_buffer[MAX_INPUT_BUFFER];
	double ups_utility_deviation = 0.0;
	int res;

	setlocale (LC_ALL, "");
	bindtextdomain (PACKAGE, LOCALEDIR);
	textdomain (PACKAGE);

	ups_status = strdup ("N/A");
	data = strdup ("");
	message = strdup ("");

	if (process_arguments (argc, argv) == ERROR)
		usage4 (_("Could not parse arguments"));

	/* initialize alarm signal handling */
	signal (SIGALRM, socket_timeout_alarm_handler);

	/* set socket timeout */
	alarm (socket_timeout);

	/* get the ups status if possible */
	if (determine_status () != OK)
		return STATE_CRITICAL;
	if (supported_options & UPS_STATUS) {

		ups_status = strdup ("");
		result = STATE_OK;

		if (status & UPSSTATUS_OFF) {
			asprintf (&ups_status, "Off");
			result = STATE_CRITICAL;
		}
		else if ((status & (UPSSTATUS_OB | UPSSTATUS_LB)) ==
						 (UPSSTATUS_OB | UPSSTATUS_LB)) {
			asprintf (&ups_status, _("On Battery, Low Battery"));
			result = STATE_CRITICAL;
		}
		else {
			if (status & UPSSTATUS_OL) {
				asprintf (&ups_status, "%s%s", ups_status, _("Online"));
			}
			if (status & UPSSTATUS_OB) {
				asprintf (&ups_status, "%s%s", ups_status, _("On Battery"));
				result = STATE_WARNING;
			}
			if (status & UPSSTATUS_LB) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Low Battery"));
				result = STATE_WARNING;
			}
			if (status & UPSSTATUS_CAL) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Calibrating"));
			}
			if (status & UPSSTATUS_RB) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Replace Battery"));
				result = STATE_WARNING;
			}
			if (status & UPSSTATUS_BYPASS) {
				asprintf (&ups_status, "%s%s", ups_status, _(", On Bypass"));
			}
			if (status & UPSSTATUS_OVER) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Overload"));
			}
			if (status & UPSSTATUS_TRIM) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Trimming"));
			}
			if (status & UPSSTATUS_BOOST) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Boosting"));
			}
			if (status & UPSSTATUS_CHRG) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Charging"));
			}
			if (status & UPSSTATUS_DISCHRG) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Discharging"));
			}
			if (status & UPSSTATUS_UNKOWN) {
				asprintf (&ups_status, "%s%s", ups_status, _(", Unknown"));
			}
		}
		asprintf (&message, "%sStatus=%s ", message, ups_status);
	}

	/* get the ups utility voltage if possible */
	res=get_ups_variable ("input.voltage", temp_buffer, sizeof (temp_buffer));
	if (res == NOSUCHVAR) supported_options &= ~UPS_UTILITY;
	else if (res != OK)
		return STATE_CRITICAL;
	else {
		supported_options |= UPS_UTILITY;

		ups_utility_voltage = atof (temp_buffer);
		asprintf (&message, "%sUtility=%3.1fV ", message, ups_utility_voltage);

		if (ups_utility_voltage > 120.0)
			ups_utility_deviation = 120.0 - ups_utility_voltage;
		else
			ups_utility_deviation = ups_utility_voltage - 120.0;

		if (check_variable == UPS_UTILITY) {
			if (check_crit==TRUE && ups_utility_deviation>=critical_value) {
				result = STATE_CRITICAL;
			}
			else if (check_warn==TRUE && ups_utility_deviation>=warning_value) {
				result = max_state (result, STATE_WARNING);
			}
			asprintf (&data, "%s",
			          perfdata ("voltage", (long)(1000*ups_utility_voltage), "mV",
			                    check_warn, (long)(1000*warning_value),
			                    check_crit, (long)(1000*critical_value),
			                    TRUE, 0, FALSE, 0));
		} else {
			asprintf (&data, "%s",
			          perfdata ("voltage", (long)(1000*ups_utility_voltage), "mV",
			                    FALSE, 0, FALSE, 0, TRUE, 0, FALSE, 0));
		}
	}

	/* get the ups battery percent if possible */
	res=get_ups_variable ("battery.charge", temp_buffer, sizeof (temp_buffer));
	if (res == NOSUCHVAR) supported_options &= ~UPS_BATTPCT;
	else if ( res != OK)
		return STATE_CRITICAL;
	else {
		supported_options |= UPS_BATTPCT;
		ups_battery_percent = atof (temp_buffer);
		asprintf (&message, "%sBatt=%3.1f%% ", message, ups_battery_percent);

		if (check_variable == UPS_BATTPCT) {
			if (check_crit==TRUE && ups_battery_percent <= critical_value) {
				result = STATE_CRITICAL;
			}
			else if (check_warn==TRUE && ups_battery_percent<=warning_value) {
				result = max_state (result, STATE_WARNING);
			}
			asprintf (&data, "%s %s", data,
			          perfdata ("battery", (long)ups_battery_percent, "%",
			                    check_warn, (long)(1000*warning_value),
			                    check_crit, (long)(1000*critical_value),
			                    TRUE, 0, TRUE, 100));
		} else {
			asprintf (&data, "%s %s", data,
			          perfdata ("battery", (long)ups_battery_percent, "%",
			                    FALSE, 0, FALSE, 0, TRUE, 0, TRUE, 100));
		}
	}

	/* get the ups load percent if possible */
	res=get_ups_variable ("ups.load", temp_buffer, sizeof (temp_buffer));
	if ( res == NOSUCHVAR ) supported_options &= ~UPS_LOADPCT;
	else if ( res != OK)
		return STATE_CRITICAL;
	else {
		supported_options |= UPS_LOADPCT;
		ups_load_percent = atof (temp_buffer);
		asprintf (&message, "%sLoad=%3.1f%% ", message, ups_load_percent);

		if (check_variable == UPS_LOADPCT) {
			if (check_crit==TRUE && ups_load_percent>=critical_value) {
				result = STATE_CRITICAL;
			}
			else if (check_warn==TRUE && ups_load_percent>=warning_value) {
				result = max_state (result, STATE_WARNING);
			}
			asprintf (&data, "%s %s", data,
			          perfdata ("load", (long)ups_load_percent, "%",
			                    check_warn, (long)(1000*warning_value),
			                    check_crit, (long)(1000*critical_value),
			                    TRUE, 0, TRUE, 100));
		} else {
			asprintf (&data, "%s %s", data,
			          perfdata ("load", (long)ups_load_percent, "%",
			                    FALSE, 0, FALSE, 0, TRUE, 0, TRUE, 100));
		}
	}

	/* get the ups temperature if possible */
	res=get_ups_variable ("ups.temperature", temp_buffer, sizeof (temp_buffer));
	if ( res == NOSUCHVAR ) supported_options &= ~UPS_TEMP;
	else if ( res != OK)
		return STATE_CRITICAL;
	else {
 		supported_options |= UPS_TEMP;
		if (temp_output_c) {
		  ups_temperature = atof (temp_buffer);
		  asprintf (&message, "%sTemp=%3.1fC", message, ups_temperature);
		}
		else {
		  ups_temperature = (atof (temp_buffer) * 1.8) + 32;
		  asprintf (&message, "%sTemp=%3.1fF", message, ups_temperature);
		}

		if (check_variable == UPS_TEMP) {
			if (check_crit==TRUE && ups_temperature>=critical_value) {
				result = STATE_CRITICAL;
			}
			else if (check_warn == TRUE && ups_temperature>=warning_value) {
				result = max_state (result, STATE_WARNING);
			}
			asprintf (&data, "%s %s", data,
			          perfdata ("temp", (long)ups_temperature, "degF",
			                    check_warn, (long)(1000*warning_value),
			                    check_crit, (long)(1000*critical_value),
			                    TRUE, 0, FALSE, 0));
		} else {
			asprintf (&data, "%s %s", data,
			          perfdata ("temp", (long)ups_temperature, "degF",
			                    FALSE, 0, FALSE, 0, TRUE, 0, FALSE, 0));
		}
	}

	/* if the UPS does not support any options we are looking for, report an error */
	if (supported_options == UPS_NONE) {
		result = STATE_CRITICAL;
		asprintf (&message, _("UPS does not support any available options\n"));
	}

	/* reset timeout */
	alarm (0);

	printf ("UPS %s - %s|%s\n", state_text(result), message, data);
	return result;
}



/* determines what options are supported by the UPS */
int
determine_status (void)
{
	char recv_buffer[MAX_INPUT_BUFFER];
	char temp_buffer[MAX_INPUT_BUFFER];
	char *ptr;
	int res;

	res=get_ups_variable ("ups.status", recv_buffer, sizeof (recv_buffer));
	if (res == NOSUCHVAR) return OK;
	if (res != STATE_OK) {
		printf (_("Invalid response received from host\n"));
		return ERROR;
	}

	supported_options |= UPS_STATUS;

	strcpy (temp_buffer, recv_buffer);
	for (ptr = (char *) strtok (temp_buffer, " "); ptr != NULL;
			 ptr = (char *) strtok (NULL, " ")) {
		if (!strcmp (ptr, "OFF"))
			status |= UPSSTATUS_OFF;
		else if (!strcmp (ptr, "OL"))
			status |= UPSSTATUS_OL;
		else if (!strcmp (ptr, "OB"))
			status |= UPSSTATUS_OB;
		else if (!strcmp (ptr, "LB"))
			status |= UPSSTATUS_LB;
		else if (!strcmp (ptr, "CAL"))
			status |= UPSSTATUS_CAL;
		else if (!strcmp (ptr, "RB"))
			status |= UPSSTATUS_RB;
		else if (!strcmp (ptr, "BYPASS"))
			status |= UPSSTATUS_BYPASS;
		else if (!strcmp (ptr, "OVER"))
			status |= UPSSTATUS_OVER;
		else if (!strcmp (ptr, "TRIM"))
			status |= UPSSTATUS_TRIM;
		else if (!strcmp (ptr, "BOOST"))
			status |= UPSSTATUS_BOOST;
		else if (!strcmp (ptr, "CHRG"))
			status |= UPSSTATUS_CHRG;
		else if (!strcmp (ptr, "DISCHRG"))
			status |= UPSSTATUS_DISCHRG;
		else
			status |= UPSSTATUS_UNKOWN;
	}

	return OK;
}


/* gets a variable value for a specific UPS  */
int
get_ups_variable (const char *varname, char *buf, size_t buflen)
{
	/*  char command[MAX_INPUT_BUFFER]; */
	char temp_buffer[MAX_INPUT_BUFFER];
	char send_buffer[MAX_INPUT_BUFFER];
	char *ptr;
	int len;

	*buf=0;
	
	/* create the command string to send to the UPS daemon */
	sprintf (send_buffer, "GET VAR %s %s\n", ups_name, varname);

	/* send the command to the daemon and get a response back */
	if (process_tcp_request
			(server_address, server_port, send_buffer, temp_buffer,
			 sizeof (temp_buffer)) != STATE_OK) {
		printf (_("Invalid response received from host\n"));
		return ERROR;
	}

	ptr = temp_buffer;
	len = strlen(ptr);
	if (len > 0 && ptr[len-1] == '\n') ptr[len-1]=0;
	if (strcmp (ptr, "ERR UNKNOWN-UPS") == 0) {
		printf (_("CRITICAL - no such ups '%s' on that host\n"), ups_name);
		return ERROR;
	}

	if (strcmp (ptr, "ERR VAR-NOT-SUPPORTED") == 0) {
		//printf ("Error: Variable '%s' is not supported\n", varname);
		return NOSUCHVAR;
	}

	if (strcmp (ptr, "ERR DATA-STALE") == 0) {
		printf (_("CRITICAL - UPS data is stale\n"));
		return ERROR;
	}

	if (strncmp (ptr, "ERR", 3) == 0) {
		printf (_("Unknown error: %s\n"), ptr);
		return ERROR;
	}

	ptr = temp_buffer + strlen (varname) + strlen (ups_name) + 6;
	len = strlen(ptr);
	if (len < 2 || ptr[0] != '"' || ptr[len-1] != '"') {
		printf (_("Error: unable to parse variable\n"));
		return ERROR;
	}
	strncpy (buf, ptr+1, len - 2);
	buf[len - 2] = 0;

	return OK;
}


/* Command line: CHECK_UPS -H <host_address> -u ups [-p port] [-v variable] 
			   [-wv warn_value] [-cv crit_value] [-to to_sec] */


/* process command-line arguments */
int
process_arguments (int argc, char **argv)
{
	int c;

	int option = 0;
	static struct option longopts[] = {
		{"hostname", required_argument, 0, 'H'},
		{"ups", required_argument, 0, 'u'},
		{"port", required_argument, 0, 'p'},
		{"critical", required_argument, 0, 'c'},
		{"warning", required_argument, 0, 'w'},
		{"timeout", required_argument, 0, 't'},
		{"temperature", no_argument, 0, 'T'},
		{"variable", required_argument, 0, 'v'},
		{"version", no_argument, 0, 'V'},
		{"help", no_argument, 0, 'h'},
		{0, 0, 0, 0}
	};

	if (argc < 2)
		return ERROR;

	for (c = 1; c < argc; c++) {
		if (strcmp ("-to", argv[c]) == 0)
			strcpy (argv[c], "-t");
		else if (strcmp ("-wt", argv[c]) == 0)
			strcpy (argv[c], "-w");
		else if (strcmp ("-ct", argv[c]) == 0)
			strcpy (argv[c], "-c");
	}

	while (1) {
		c = getopt_long (argc, argv, "hVTH:u:p:v:c:w:t:", longopts,
									 &option);

		if (c == -1 || c == EOF)
			break;

		switch (c) {
		case '?':									/* help */
			usage2 (_("Unknown argument"), optarg);
		case 'H':									/* hostname */
			if (is_host (optarg)) {
				server_address = optarg;
			}
			else {
				usage2 (_("Invalid hostname/address"), optarg);
			}
			break;
		case 'T': /* FIXME: to be improved (ie "-T C" for Celsius or "-T F" for Farenheit) */ 
			temp_output_c = 1;
			break;
		case 'u':									/* ups name */
			ups_name = optarg;
			break;
		case 'p':									/* port */
			if (is_intpos (optarg)) {
				server_port = atoi (optarg);
			}
			else {
				usage2 (_("Port must be a positive integer"), optarg);
			}
			break;
		case 'c':									/* critical time threshold */
			if (is_intnonneg (optarg)) {
				critical_value = atoi (optarg);
				check_crit = TRUE;
			}
			else {
				usage2 (_("Critical time must be a positive integer"), optarg);
			}
			break;
		case 'w':									/* warning time threshold */
			if (is_intnonneg (optarg)) {
				warning_value = atoi (optarg);
				check_warn = TRUE;
			}
			else {
				usage2 (_("Warning time must be a positive integer"), optarg);
			}
			break;
		case 'v':									/* variable */
			if (!strcmp (optarg, "LINE"))
				check_variable = UPS_UTILITY;
			else if (!strcmp (optarg, "TEMP"))
				check_variable = UPS_TEMP;
			else if (!strcmp (optarg, "BATTPCT"))
				check_variable = UPS_BATTPCT;
			else if (!strcmp (optarg, "LOADPCT"))
				check_variable = UPS_LOADPCT;
			else
				usage2 (_("Unrecognized UPS variable"), optarg);
			break;
		case 't':									/* timeout */
			if (is_intnonneg (optarg)) {
				socket_timeout = atoi (optarg);
			}
			else {
				usage4 (_("Timeout interval must be a positive integer"));
			}
			break;
		case 'V':									/* version */
			print_revision (progname, revision);
			exit (STATE_OK);
		case 'h':									/* help */
			print_help ();
			exit (STATE_OK);
		}
	}


	if (server_address == NULL && argc > optind) {
		if (is_host (argv[optind]))
			server_address = argv[optind++];
		else
			usage2 (_("Invalid hostname/address"), optarg);
	}

	if (server_address == NULL)
		server_address = strdup("127.0.0.1");

	return validate_arguments();
}


int
validate_arguments (void)
{
	if (! ups_name) {
		printf (_("Error : no ups indicated\n"));
		return ERROR;
	}
	return OK;
}


void
print_help (void)
{
	char *myport;
	asprintf (&myport, "%d", PORT);

	print_revision (progname, revision);

	printf ("Copyright (c) 2000 Tom Shields");
	printf ("Copyright (c) 2004 Alain Richard <alain.richard@equation.fr>\n");
	printf ("Copyright (c) 2004 Arnaud Quette <arnaud.quette@mgeups.com>\n");
	printf (COPYRIGHT, copyright, email);

	printf (_("This plugin tests the UPS service on the specified host.\n\
Network UPS Tools from www.networkupstools.org must be running for this\n\
plugin to work.\n\n"));

	print_usage ();

	printf (_(UT_HELP_VRSN));

	printf (_(UT_HOST_PORT), 'p', myport);

	printf (_("\
 -u, --ups=STRING\n\
    Name of UPS\n"));

	printf (_("\
 -T, --temperature\n\
    Output of temperatures in Celsius\n"));

	printf (_(UT_WARN_CRIT));

	printf (_(UT_TIMEOUT), DEFAULT_SOCKET_TIMEOUT);

	printf (_(UT_VERBOSE));

	printf (_("\
This plugin attempts to determine the status of a UPS (Uninterruptible Power\n\
Supply) on a local or remote host. If the UPS is online or calibrating, the\n\
plugin will return an OK state. If the battery is on it will return a WARNING\n\
state.  If the UPS is off or has a low battery the plugin will return a CRITICAL\n\
state.\n\n"));

	printf (_("\
You may also specify a variable to check [such as temperature, utility voltage,\n\
battery load, etc.]  as well as warning and critical thresholds for the value of\n\
that variable.  If the remote host has multiple UPS that are being monitored you\n\
will have to use the [ups] option to specify which UPS to check.\n\n"));

	printf (_("Notes:\n\n\
This plugin requires that the UPSD daemon distributed with Russel Kroll's\n\
Smart UPS Tools be installed on the remote host.  If you do not have the\n\
package installed on your system, you can download it from\n\
http://www.networkupstools.org\n\n"));

	printf (_(UT_SUPPORT));
}


void
print_usage (void)
{
	printf ("\
Usage: %s -H host -u ups [-p port] [-v variable]\n\
                  [-wv warn_value] [-cv crit_value] [-to to_sec] [-T]\n", progname);
}
