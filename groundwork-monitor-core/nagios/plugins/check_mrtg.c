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

 $Id: check_mrtg.c,v 1.1.1.1 2005/02/07 19:33:32 hmann Exp $
 
******************************************************************************/

const char *progname = "check_mrtg";
const char *revision =  "$Revision: 1.1.1.1 $";
const char *copyright = "1999-2004";
const char *email = "nagiosplug-devel@lists.sourceforge.net";

#include "common.h"
#include "utils.h"

int process_arguments (int, char **);
int validate_arguments (void);
void print_help (void);
void print_usage (void);

char *log_file = NULL;
int expire_minutes = 0;
int use_average = TRUE;
int variable_number = -1;
unsigned long value_warning_threshold = 0L;
unsigned long value_critical_threshold = 0L;
char *label;
char *units;

int
main (int argc, char **argv)
{
	int result = STATE_UNKNOWN;
	FILE *fp;
	int line;
	char input_buffer[MAX_INPUT_BUFFER];
	char *temp_buffer;
	time_t current_time;
	char* message;
	time_t timestamp = 0L;
	unsigned long average_value_rate = 0L;
	unsigned long maximum_value_rate = 0L;
	unsigned long rate = 0L;

	setlocale (LC_ALL, "");
	bindtextdomain (PACKAGE, LOCALEDIR);
	textdomain (PACKAGE);

	if (process_arguments (argc, argv) == ERROR)
		usage4 (_("Could not parse arguments\n"));

	/* open the MRTG log file for reading */
	fp = fopen (log_file, "r");
	if (fp == NULL) {
		printf (_("Unable to open MRTG log file\n"));
		return STATE_UNKNOWN;
	}

	line = 0;
	while (fgets (input_buffer, MAX_INPUT_BUFFER - 1, fp)) {

		line++;

		/* skip the first line of the log file */
		if (line == 1)
			continue;

		/* break out of read loop if we've passed the number of entries we want to read */
		if (line > 2)
			break;

		/* grab the timestamp */
		temp_buffer = strtok (input_buffer, " ");
		timestamp = strtoul (temp_buffer, NULL, 10);

		/* grab the average value 1 rate */
		temp_buffer = strtok (NULL, " ");
		if (variable_number == 1)
			average_value_rate = strtoul (temp_buffer, NULL, 10);

		/* grab the average value 2 rate */
		temp_buffer = strtok (NULL, " ");
		if (variable_number == 2)
			average_value_rate = strtoul (temp_buffer, NULL, 10);

		/* grab the maximum value 1 rate */
		temp_buffer = strtok (NULL, " ");
		if (variable_number == 1)
			maximum_value_rate = strtoul (temp_buffer, NULL, 10);

		/* grab the maximum value 2 rate */
		temp_buffer = strtok (NULL, " ");
		if (variable_number == 2)
			maximum_value_rate = strtoul (temp_buffer, NULL, 10);
	}

	/* close the log file */
	fclose (fp);

	/* if we couldn't read enough data, return an unknown error */
	if (line <= 2) {
		result = STATE_UNKNOWN;
		asprintf (&message, _("Unable to process MRTG log file\n"));
	}

	/* make sure the MRTG data isn't too old */
	if (result == STATE_OK) {
		time (&current_time);
		if (expire_minutes > 0
				&& (current_time - timestamp) > (expire_minutes * 60)) {
			result = STATE_WARNING;
			asprintf (&message, _("MRTG data has expired (%d minutes old)\n"),
							 (int) ((current_time - timestamp) / 60));
		}
	}

	/* else check the incoming/outgoing rates */
	if (result == STATE_OK) {
		if (use_average == TRUE)
			rate = average_value_rate;
		else
			rate = maximum_value_rate;

		if (rate > value_critical_threshold)
			result = STATE_CRITICAL;
		else if (rate > value_warning_threshold)
			result = STATE_WARNING;

		asprintf (&message, "%s. %s = %lu %s|%s",
		          (use_average == TRUE) ? _("Avg") : _("Max"),
		          label, rate, units,
		          perfdata(label, (long) rate, units,
		                   (int) value_warning_threshold, (long) value_warning_threshold,
		                   (int) value_critical_threshold, (long) value_critical_threshold,
		                   0, 0, 0, 0));
	}

	printf ("%s\n", message);

	return result;
}



/* process command-line arguments */
int
process_arguments (int argc, char **argv)
{
	int c;

	int option = 0;
	static struct option longopts[] = {
		{"logfile", required_argument, 0, 'F'},
		{"expires", required_argument, 0, 'e'},
		{"aggregation", required_argument, 0, 'a'},
		{"variable", required_argument, 0, 'v'},
		{"critical", required_argument, 0, 'c'},
		{"warning", required_argument, 0, 'w'},
		{"label", required_argument, 0, 'l'},
		{"units", required_argument, 0, 'u'},
		{"verbose", no_argument, 0, 'v'},
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
		c = getopt_long (argc, argv, "hVF:e:a:v:c:w:l:u:", longopts,
									 &option);

		if (c == -1 || c == EOF)
			break;

		switch (c) {
		case 'F':									/* input file */
			log_file = optarg;
			break;
		case 'e':									/* ups name */
			expire_minutes = atoi (optarg);
			break;
		case 'a':									/* port */
			if (!strcmp (optarg, "MAX"))
				use_average = FALSE;
			else
				use_average = TRUE;
			break;
		case 'v':
			variable_number = atoi (optarg);
			if (variable_number < 1 || variable_number > 2)
				usage4 (_("Invalid variable number"));
			break;
		case 'w':									/* critical time threshold */
			value_warning_threshold = strtoul (optarg, NULL, 10);
			break;
		case 'c':									/* warning time threshold */
			value_critical_threshold = strtoul (optarg, NULL, 10);
			break;
		case 'l':									/* label */
			label = optarg;
			break;
		case 'u':									/* timeout */
			units = optarg;
			break;
		case 'V':									/* version */
			print_revision (progname, revision);
			exit (STATE_OK);
		case 'h':									/* help */
			print_help ();
			exit (STATE_OK);
		case '?':									/* help */
			usage2 (_("Unknown argument"), optarg);
		}
	}

	c = optind;
	if (log_file == NULL && argc > c) {
		log_file = argv[c++];
	}

	if (expire_minutes <= 0 && argc > c) {
		if (is_intpos (argv[c]))
			expire_minutes = atoi (argv[c++]);
		else
			die (STATE_UNKNOWN,
			           _("%s is not a valid expiration time\nUse '%s -h' for additional help\n"),
			           argv[c], progname);
	}

	if (argc > c && strcmp (argv[c], "MAX") == 0) {
		use_average = FALSE;
		c++;
	}
	else if (argc > c && strcmp (argv[c], "AVG") == 0) {
		use_average = TRUE;
		c++;
	}

	if (argc > c && variable_number == -1) {
		variable_number = atoi (argv[c++]);
		if (variable_number < 1 || variable_number > 2) {
			printf ("%s :", argv[c]);
			usage (_("Invalid variable number\n"));
		}
	}

	if (argc > c && value_warning_threshold == 0) {
		value_warning_threshold = strtoul (argv[c++], NULL, 10);
	}

	if (argc > c && value_critical_threshold == 0) {
		value_critical_threshold = strtoul (argv[c++], NULL, 10);
	}

	if (argc > c && strlen (label) == 0) {
		label = argv[c++];
	}

	if (argc > c && strlen (units) == 0) {
		units = argv[c++];
	}

	return validate_arguments ();
}

int
validate_arguments (void)
{
	if (variable_number == -1)
		usage4 (_("You must supply the variable number"));

	if (label == NULL)
		label = strdup ("value");

	if (units == NULL)
		units = strdup ("");

	return OK;
}



void
print_help (void)
{
	print_revision (progname, revision);

	printf ("Copyright (c) 1999 Ethan Galstad <nagios@nagios.org>\n");
	printf (COPYRIGHT, copyright, email);

	printf(_("\
This plugin will check either the average or maximum value of one of the\n\
two variables recorded in an MRTG log file.\n\n"));

	print_usage ();

	printf (_(UT_HELP_VRSN));

	printf (_("\
 -F, --logfile=FILE\n\
   The MRTG log file containing the data you want to monitor\n\
 -e, --expires=MINUTES\n\
   Minutes before MRTG data is considered to be too old\n\
 -a, --aggregation=AVG|MAX\n\
   Should we check average or maximum values?\n\
 -v, --variable=INTEGER\n\
   Which variable set should we inspect? (1 or 2)\n\
 -w, --warning=INTEGER\n\
   Threshold value for data to result in WARNING status\n\
 -c, --critical=INTEGER\n\
   Threshold value for data to result in CRITICAL status\n"));

	printf (_("\
 -l, --label=STRING\n\
   Type label for data (Examples: Conns, \"Processor Load\", In, Out)\n\
 -u, --units=STRING\n\
   Option units label for data (Example: Packets/Sec, Errors/Sec, \n\
   \"Bytes Per Second\", \"%% Utilization\")\n"));

	printf (_("\
If the value exceeds the <vwl> threshold, a WARNING status is returned.  If\n\
the value exceeds the <vcl> threshold, a CRITICAL status is returned.  If\n\
the data in the log file is older than <expire_minutes> old, a WARNING\n\
status is returned and a warning message is printed.\n\n"));

	printf(_("This plugin is useful for monitoring MRTG data that does not correspond to\n\
bandwidth usage.  (Use the check_mrtgtraf plugin for monitoring bandwidth).\n\
It can be used to monitor any kind of data that MRTG is monitoring - errors,\n\
packets/sec, etc.  I use MRTG in conjuction with the Novell NLM that allows\n\
me to track processor utilization, user connections, drive space, etc and\n\
this plugin works well for monitoring that kind of data as well.\n\n"));

	printf (_("Notes:\n\
- This plugin only monitors one of the two variables stored in the MRTG log\n\
  file.  If you want to monitor both values you will have to define two\n\
  commands with different values for the <variable> argument.  Of course,\n\
  you can always hack the code to make this plugin work for you...\n\
- MRTG stands for the Multi Router Traffic Grapher.  It can be downloaded from\n\
  http://ee-staff.ethz.ch/~oetiker/webtools/mrtg/mrtg.html\n"));

	printf (_(UT_SUPPORT));
}



/* original command line: 
	 <log_file> <expire_minutes> <AVG|MAX> <variable> <vwl> <vcl> <label> [units] */

void
print_usage (void)
{
	printf ("\
Usage: %s -F log_file -a <AVG | MAX> -v variable -w warning -c critical\n\
                  [-l label] [-u units] [-e expire_minutes] [-t timeout]\n\
                  [-v]\n", progname);
}
