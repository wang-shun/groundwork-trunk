// check_hosts.c

// Copyright (c) 2017 by GroundWork Open Source, Inc.
// All rights reserved.

// ================================================================
// Change Log
// ================================================================
// 2017-11-04	1.2.0	First C version, slavishly modeled after
//			the initial proof-of-concept Perl version.
// ================================================================

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <limits.h>
#include <regex.h>
#include <errno.h>

#define _GNU_SOURCE
#include <getopt.h>

// This header should be drawn from "/usr/local/groundwork/postgresql/include/libpq-fe.h"
// and not from "/opt/PostgreSQL/9.1/include/libpq-fe.h".  That depends on whatever -I options
// are used for the compilation.
#include "libpq-fe.h"

// BE SURE TO KEEP THIS UP-TO-DATE!
#define VERSION		"1.2.0"

#define PROGNAME	"check_hosts"

#define EXIT_OK		0
#define EXIT_WARNING	1
#define EXIT_CRITICAL	2
#define EXIT_UNKNOWN	3

#define OBFN_NO_REG_EFLAGS      0

// Identifying strings so we can distinguish different compilations of the programs.  These
// are intentionally not obfuscated, so the "ident" program can dig them out and print them.
static char version[]      = "$Version: " VERSION " $";
static char compile_time[] = "$CompileTime: " __TIME__ " on " __DATE__ " $";

typedef PGconn   *OBFN_POSTGRESQL_DB_HANDLE;
typedef PGresult *OBFN_POSTGRESQL_DB_RESULT;

enum OBFN_database_type {OBFN_DB_IS_UNKNOWN, OBFN_DB_IS_MYSQL, OBFN_DB_IS_POSTGRESQL};

static enum OBFN_database_type OBFN_db_type = OBFN_DB_IS_UNKNOWN;

static int OBFN_show_detail = 0;
static char *OBFN_database_error = NULL;

struct OBFN_values
    {
    char *OBFN_name;
    char **OBFN_value;
    };

static void OBFN_print_version ()
    {
    printf (OBFS_string( PROGNAME " v" VERSION "\n" ));
    }

static void OBFN_print_usage ()
    {
    printf (
	OBFS_string(
	    "usage:  "PROGNAME" [-c #] [-w #] [-p] [-D]\n"
	    "        "PROGNAME" [-h]\n"
	    "        "PROGNAME" [-V]\n"
	    "where:\n"
	    "        -c, --critical\n"
	    "            Number of hosts to consider a critical level (optional)\n"
	    "        -w, --warning\n"
	    "            Number of hosts to consider a warning level (optional)\n"
	    "        -p, --performance\n"
	    "            Report Nagios performance data after the output string\n"
	    "        -D  Print extra debug messages\n"
	    "        -h, --help\n"
	    "            Print this help message\n"
	    "        -V, --Version\n"
	    "            Print the program version\n"
	)
    );
    }

static void OBFN_print_help ()
    {
    OBFN_print_version ();
    OBFN_print_usage ();
    }

// Returns a valid OBFN_POSTGRESQL_DB_HANDLE on success, a null pointer on error.
static OBFN_POSTGRESQL_DB_HANDLE OBFN_postgresql_dbconnect (const char *OBFN_host, int OBFN_port, const char *OBFN_user, const char *OBFN_pass, const char *OBFN_data)
    {
    OBFN_POSTGRESQL_DB_HANDLE OBFN_postgresql;

    // Here we destroy a bunch of potentially-set environment variables that might
    // possibly provide unwanted defaults for parameters we don't set here explicitly,
    // to avoid any possibility of security issues or bypassing our intended connections.
    // This also means that we won't support more-secure connections than we have already
    // allowed for in the explicitly-set parameters below, so if we have some customer
    // who demands that (e.g., who wants only SSL connections to be valid), we will have
    // to come back here and revise this.
    unsetenv(OBFS_string("PGHOST"));
    unsetenv(OBFS_string("PGHOSTADDR"));
    unsetenv(OBFS_string("PGPORT"));
    unsetenv(OBFS_string("PGDATABASE"));
    unsetenv(OBFS_string("PGUSER"));
    unsetenv(OBFS_string("PGPASSWORD"));
    unsetenv(OBFS_string("PGPASSFILE"));
    unsetenv(OBFS_string("PGSERVICE"));
    unsetenv(OBFS_string("PGSERVICEFILE"));
    unsetenv(OBFS_string("PGREALM"));
    unsetenv(OBFS_string("PGOPTIONS"));
    unsetenv(OBFS_string("PGAPPNAME"));
    unsetenv(OBFS_string("PGSSLMODE"));
    unsetenv(OBFS_string("PGREQUIRESSL"));
    unsetenv(OBFS_string("PGSSLCERT"));
    unsetenv(OBFS_string("PGSSLKEY"));
    unsetenv(OBFS_string("PGSSLROOTCERT"));
    unsetenv(OBFS_string("PGSSLCRL"));
    unsetenv(OBFS_string("PGREQUIREPEER"));
    unsetenv(OBFS_string("PGKRBSRVNAME"));
    unsetenv(OBFS_string("PGGSSLIB"));
    unsetenv(OBFS_string("PGCONNECT_TIMEOUT"));
    unsetenv(OBFS_string("PGCLIENTENCODING"));
    unsetenv(OBFS_string("PGDATESTYLE"));
    unsetenv(OBFS_string("PGTZ"));
    unsetenv(OBFS_string("PGGEQO"));
    unsetenv(OBFS_string("PGSYSCONFDIR"));
    unsetenv(OBFS_string("PGLOCALEDIR"));

    char OBFN_port_string[10];
    int OBFN_outcome = snprintf (OBFN_port_string, sizeof(OBFN_port_string), "%d", OBFN_port);
    if (OBFN_outcome < 0 || OBFN_outcome >= sizeof (OBFN_port_string))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot process database parameters\n"));
	return (NULL);
	}

    // FIX MINOR:
    // Possible OBFN_postgres_options we might set:
    // default_transaction_read_only(on)	perhaps set this if all we're doing is reading, if this might be faster
    // statement_timeout(milliseconds)		time out long-running statements on the server side

    // Note that the database name must be specified as all-lowercase here, given that it actually exists within PostgreSQL
    // that way.  Mixed-case in this specification is not automatically lowercased during a call to PQconnectdbParams().
    // If we might not have lowercase in the db.properties file, we would have to lowercase the name explicitly here.
    char *OBFN_postgres_host                      = (char *) OBFN_host;
    char *OBFN_postgres_hostaddr                  = NULL;
    char *OBFN_postgres_port                      = OBFN_port_string;
    char *OBFN_postgres_dbname                    = (char *) OBFN_data;
    char *OBFN_postgres_user                      = (char *) OBFN_user;
    char *OBFN_postgres_password                  = (char *) OBFN_pass;
    char *OBFN_postgres_connect_timeout           = OBFS_string("30");
    char *OBFN_postgres_client_encoding           = NULL;
    char *OBFN_postgres_options                   = NULL;	// FIX MINOR:  check Chapter 18 in the PostgreSQL manual
    char *OBFN_postgres_application_name          = OBFS_string("commit_check");
    char *OBFN_postgres_failback_application_name = NULL;
    char *OBFN_postgres_keepalives                = NULL;
    char *OBFN_postgres_keepalives_idle           = NULL;
    char *OBFN_postgres_keepalives_interval       = NULL;
    char *OBFN_postgres_keepalives_count          = NULL;
    char *OBFN_postgres_tty                       = NULL;
    char *OBFN_postgres_sslmode                   = NULL;
    char *OBFN_postgres_requiressl                = NULL;
    char *OBFN_postgres_sslcert                   = NULL;
    char *OBFN_postgres_sslkey                    = NULL;
    char *OBFN_postgres_sslrootcert               = NULL;
    char *OBFN_postgres_sslcrl                    = NULL;
    char *OBFN_postgres_requirepeer               = OBFS_string("postgres");
    char *OBFN_postgres_krbsrvname                = NULL;
    char *OBFN_postgres_gsslib                    = NULL;
    char *OBFN_postgres_service                   = NULL;

    // For development debugging only.  We don't want to spill critical security information to the first passerby.
    if (0 && OBFN_show_detail)
	{
	// fprintf (stderr, OBFS_string("NOTICE: attempting connection to dbhost %s, dbport %s, dbuser %s, dbpass %s, dbname %s\n"),
	    // OBFN_postgres_host, OBFN_postgres_port, OBFN_postgres_user, OBFN_postgres_password, OBFN_postgres_dbname);
	}

    // We ignore fields that don't seem relevant to our purposes here.
    struct OBFN_values OBFN_postgres_connect_values[] =
	{
	{ OBFS_string("host"),                      &OBFN_postgres_host                      },
	// { OBFS_string("hostaddr"),                  &OBFN_postgres_hostaddr                  },
	{ OBFS_string("port"),                      &OBFN_postgres_port                      },
	{ OBFS_string("dbname"),                    &OBFN_postgres_dbname                    },
	{ OBFS_string("user"),                      &OBFN_postgres_user                      },
	{ OBFS_string("password"),                  &OBFN_postgres_password                  },
	{ OBFS_string("connect_timeout"),           &OBFN_postgres_connect_timeout           },
	// { OBFS_string("client_encoding"),           &OBFN_postgres_client_encoding           },
	// { OBFS_string("options"),                   &OBFN_postgres_options                   },
	{ OBFS_string("application_name"),          &OBFN_postgres_application_name          },
	// { OBFS_string("failback_application_name"), &OBFN_postgres_failback_application_name },
	// { OBFS_string("keepalives"),                &OBFN_postgres_keepalives                },
	// { OBFS_string("keepalives_idle"),           &OBFN_postgres_keepalives_idle           },
	// { OBFS_string("keepalives_interval"),       &OBFN_postgres_keepalives_interval       },
	// { OBFS_string("keepalives_count"),          &OBFN_postgres_keepalives_count          },
	// { OBFS_string("tty"),                       &OBFN_postgres_tty                       },
	// { OBFS_string("sslmode"),                   &OBFN_postgres_sslmode                   },
	// { OBFS_string("requiressl"),                &OBFN_postgres_requiressl                },
	// { OBFS_string("sslcert"),                   &OBFN_postgres_sslcert                   },
	// { OBFS_string("sslkey"),                    &OBFN_postgres_sslkey                    },
	// { OBFS_string("sslrootcert"),               &OBFN_postgres_sslrootcert               },
	// { OBFS_string("sslcrl"),                    &OBFN_postgres_sslcrl                    },
	{ OBFS_string("requirepeer"),               &OBFN_postgres_requirepeer               },
	// { OBFS_string("krbsrvname"),                &OBFN_postgres_krbsrvname                },
	// { OBFS_string("gsslib"),                    &OBFN_postgres_gsslib                    },
	// { OBFS_string("service"),                   &OBFN_postgres_service                   },
	};

    const char **OBFN_connect_keywords = calloc ((sizeof(OBFN_postgres_connect_values) / sizeof(struct OBFN_values)) + 1, sizeof(char *));
    const char **OBFN_connect_values   = calloc ((sizeof(OBFN_postgres_connect_values) / sizeof(struct OBFN_values)) + 1, sizeof(char *));
    if (OBFN_connect_keywords == NULL || OBFN_connect_values == NULL)
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot allocate memory\n"));
	return (NULL);
	}

    int OBFN_index;
    for (OBFN_index = 0; OBFN_index < sizeof(OBFN_postgres_connect_values) / sizeof(struct OBFN_values); ++OBFN_index)
	{
	OBFN_connect_keywords[OBFN_index] =  OBFN_postgres_connect_values[OBFN_index].OBFN_name;
	OBFN_connect_values  [OBFN_index] = *OBFN_postgres_connect_values[OBFN_index].OBFN_value;
	}

    int OBFN_expand_dbname = 0;
    if (! (OBFN_postgresql = PQconnectdbParams (OBFN_connect_keywords, OBFN_connect_values, OBFN_expand_dbname)))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot obtain a handle for the database\n"));
	return (NULL);
	}

    if (PQstatus (OBFN_postgresql) != CONNECTION_OK)
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot connect to the %s database:\n%s"), OBFN_data, PQerrorMessage (OBFN_postgresql));
	PQfinish (OBFN_postgresql);
	return (NULL);
	}

    return (OBFN_postgresql);
    }

static int OBFN_postgresql_dbdisconnect (OBFN_POSTGRESQL_DB_HANDLE OBFN_database)
    {
    if (OBFN_database)
	{
	PQfinish (OBFN_database);
	}
    return (0);
    }

static const char *OBFN_key_value_pattern = NULL;
static const char *OBFN_param_value_pattern = NULL;
static regex_t OBFN_key_value_expr;
static regex_t OBFN_param_value_expr;
static regmatch_t OBFN_matched_substrings[4];

static int OBFN_initialize ()
    {
#if OBFN_SCRAMBLE
    OBFN_prepare();
#endif
    // For the key-value pattern, the value must be able to at least match all valid characters in a fully-qualified hostname.
    //
    // For complete generality, since we will also be using this same regex pattern to match possible near-arbitrary characters
    // in a password, the only thing we will disallow in the value is an embedded whitespace character.
    //
    // At one point, we thought that perhaps we might want to generalize the key-value pattern to match enclosing balanced quotes
    // around the value, but in fact the Java code that reads properties files would just treat such enclosing quotes as part of
    // the value, so any attempt we might make here to strip them would itself be in error.
    OBFN_key_value_pattern   = OBFS_string("^[[:space:]]*([[:lower:]]+([.][[:lower:]]+)+)[[:space:]]*=[[:space:]]*([^[:space:]]+)[[:space:]]*$");
    OBFN_param_value_pattern = OBFS_string("^[[:space:]]*([[:alnum:]_]+)[[:space:]]*=[[:space:]]*([[:alnum:]]+)[[:space:]]*$");
    if (regcomp (&OBFN_key_value_expr, OBFN_key_value_pattern, REG_EXTENDED | REG_NEWLINE))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: regcomp() failed to compile key-value pattern\n"));
	return (-1);
	}
    if (regcomp (&OBFN_param_value_expr, OBFN_param_value_pattern, REG_EXTENDED | REG_NEWLINE))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: regcomp() failed to compile param-value pattern\n"));
	return (-1);
	}

    return (0);
    }

static int OBFN_count_host_devices ()
    {
    int OBFN_device_count = -1;
    OBFN_database_error = OBFS_string("unknown access error");

    char OBFN_long_line[10000];
    char *OBFN_global_db_type = NULL;

    char *OBFN_collage_host = NULL;
    char *OBFN_collage_PORT = NULL;     // not actually present in db.properties, at least for now
    int   OBFN_collage_port = 0;        // not actually present in db.properties, at least for now
    char *OBFN_collage_user = NULL;
    char *OBFN_collage_pass = NULL;
    char *OBFN_collage_data = NULL;

    struct OBFN_values OBFN_config_values[] =
	{
	{ OBFS_string("global.db.type"),   &OBFN_global_db_type },
	{ OBFS_string("collage.dbhost"),   &OBFN_collage_host   },
	{ OBFS_string("collage.dbport"),   &OBFN_collage_PORT   },
	{ OBFS_string("collage.username"), &OBFN_collage_user   },
	{ OBFS_string("collage.password"), &OBFN_collage_pass   },
	{ OBFS_string("collage.database"), &OBFN_collage_data   },
	};
    FILE *OBFN_properties = fopen (OBFS_string("/usr/local/groundwork/config/db.properties"), OBFS_string("r"));
    if (!OBFN_properties)
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot open db.properties\n"));
	OBFN_database_error = OBFS_string("cannot access database credentials");
	return (-1);
	}
    while (fgets(OBFN_long_line, sizeof(OBFN_long_line), OBFN_properties) != NULL)
	{
	if (regexec (&OBFN_key_value_expr, OBFN_long_line, sizeof (OBFN_matched_substrings) / sizeof (regmatch_t), OBFN_matched_substrings, OBFN_NO_REG_EFLAGS) == 0)
	    {
	    int OBFN_i;
	    char *OBFN_key = strndup(OBFN_long_line + OBFN_matched_substrings[1].rm_so, OBFN_matched_substrings[1].rm_eo - OBFN_matched_substrings[1].rm_so);
	    char *OBFN_val = strndup(OBFN_long_line + OBFN_matched_substrings[3].rm_so, OBFN_matched_substrings[3].rm_eo - OBFN_matched_substrings[3].rm_so);
	    // printf (OBFS_string("key '%s' = val '%s'\n"), OBFN_key, OBFN_val);

	    for (OBFN_i = 0; OBFN_i < (sizeof (OBFN_config_values) / sizeof (struct OBFN_values)); ++OBFN_i)
		{
		if (strcmp(OBFN_key, OBFN_config_values[OBFN_i].OBFN_name) == 0)
		    {
		    *OBFN_config_values[OBFN_i].OBFN_value = OBFN_val;
		    }
		}
	    }
	}
    fclose (OBFN_properties);

    OBFN_db_type = OBFN_global_db_type == NULL ? OBFN_DB_IS_POSTGRESQL :
	strcmp (OBFN_global_db_type, OBFS_string("postgresql")) == 0 ? OBFN_DB_IS_POSTGRESQL : OBFN_DB_IS_MYSQL;

    OBFN_collage_port = OBFN_collage_PORT ? atoi(OBFN_collage_PORT) : (OBFN_db_type == OBFN_DB_IS_POSTGRESQL) ? 5432 : 3306;

    OBFN_POSTGRESQL_DB_HANDLE OBFN_postgresql_collage;

    switch (OBFN_db_type)
	{
	case OBFN_DB_IS_POSTGRESQL:
	    OBFN_postgresql_collage = OBFN_postgresql_dbconnect (OBFN_collage_host, OBFN_collage_port, OBFN_collage_user, OBFN_collage_pass, OBFN_collage_data);
	    if (OBFN_postgresql_collage)
		{
		do  {
		    OBFN_POSTGRESQL_DB_RESULT OBFN_RESULT = PQexecParams(OBFN_postgresql_collage,
			OBFS_string("select count(*) from device where deviceid in (select deviceid from host)"), 0, NULL, NULL, NULL, NULL, 0);
		    if (!OBFN_RESULT)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR:  Internal error from PQexecParams:  %s"), PQerrorMessage(OBFN_postgresql_collage));
			snprintf (OBFN_long_line, sizeof(OBFN_long_line), OBFS_string("%s"), PQerrorMessage(OBFN_postgresql_collage));
			char *OBFN_newline_position = strchr(OBFN_long_line, '\n');
			if (OBFN_newline_position)
			    {
			    *OBFN_newline_position = '\0';
			    }
			// This will leak memory, but nobody will care.
			OBFN_database_error = strdup(OBFN_long_line);
			break;
			}
		    if (PQresultStatus (OBFN_RESULT) != PGRES_TUPLES_OK)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR:  Internal error from PQresultStatus:  %s"), PQresultErrorMessage(OBFN_RESULT));
			snprintf (OBFN_long_line, sizeof(OBFN_long_line), OBFS_string("%s"), PQresultErrorMessage(OBFN_RESULT));
			char *OBFN_newline_position = strchr(OBFN_long_line, '\n');
			if (OBFN_newline_position)
			    {
			    *OBFN_newline_position = '\0';
			    }
			// This will leak memory, but nobody will care.
			OBFN_database_error = strdup(OBFN_long_line);
			break;
			}
		    OBFN_device_count = atoi(PQgetvalue(OBFN_RESULT, 0, 0));
		    PQclear(OBFN_RESULT);
		    } while (0);
		OBFN_postgresql_dbdisconnect (OBFN_postgresql_collage);
		}
	    else
		{
		OBFN_database_error = OBFS_string("cannot connect to the database");
		}

	default:
	    break;
	}

    return OBFN_device_count;
    }

int main (int OBFN_argc, char *OBFN_argv[])
    {
    int OBFN_exit_status = EXIT_UNKNOWN;
    char OBFN_plugin_message[300];

    int OBFN_got_error    = 0;
    int OBFN_show_version = 0;
    int OBFN_show_help    = 0;
    long OBFN_warn        = 0;
    long OBFN_crit        = 0;
    int OBFN_debug        = 0;
    int OBFN_perf         = 0;

    // "--detail" is a hidden option for in-the-field debugging of severe problems.
    // We purposely do not document that option, in case it might expose more info
    // than we would like.  It is only for for use in extreme situations.
    int OBFN_c;
    char *OBFN_optstring = OBFS_string("+:Vhw:c:Dp");
    struct option OBFN_longopts[] =
	{
	{ OBFS_string("Version"),     no_argument,       NULL, 'V' },
	{ OBFS_string("help"),        no_argument,       NULL, 'h' },
	{ OBFS_string("warning"),     required_argument, NULL, 'w' },
	{ OBFS_string("critical"),    required_argument, NULL, 'c' },
	{ OBFS_string("debug"),       no_argument,       NULL, 'D' },
	{ OBFS_string("performance"), no_argument,       NULL, 'p' },
	{ OBFS_string("detail"),      no_argument,       NULL, '+' },
	{ 0, 0, 0, 0 }
	};

    opterr = 0;
    char *OBFN_endptr;
    while (1)
	{
	OBFN_c = OBFN_c = getopt_long(OBFN_argc, OBFN_argv, OBFN_optstring, OBFN_longopts, NULL);
	if (OBFN_c < 0)
	    {
	    break;
	    }
	switch (OBFN_c)
	    {
	    case 'V':
		OBFN_show_version = 1;
		break;
	    case 'h':
		OBFN_show_help = 1;
		break;
	    case 'w':
		OBFN_warn = strtol(optarg, &OBFN_endptr, 10);
		if (OBFN_endptr == optarg || *OBFN_endptr != '\0' || OBFN_warn == LONG_MIN || OBFN_warn == LONG_MAX)
		    {
		    printf(OBFS_string("Invalid option value specified for '%s'.\n"), OBFN_argv[optind - 2]);
		    OBFN_got_error = 1;
		    }
		break;
	    case 'c':
		OBFN_crit = strtol(optarg, &OBFN_endptr, 10);
		if (OBFN_endptr == optarg || *OBFN_endptr != '\0' || OBFN_crit == LONG_MIN || OBFN_crit == LONG_MAX)
		    {
		    printf(OBFS_string("Invalid option value specified for '%s'.\n"), OBFN_argv[optind - 2]);
		    OBFN_got_error = 1;
		    }
		break;
	    case 'D':
		OBFN_debug = 1;
		break;
	    case 'p':
		OBFN_perf = 1;
		break;
	    case '+':
		OBFN_show_detail = 1;
		break;
	    case ':':
		printf(OBFS_string("Missing argument: '%s'\n"), OBFN_argv[--optind]);
		OBFN_got_error = 1;
		break;
	    default:
		if (optopt)
		    {
		    // Bad single-character option.
		    printf(OBFS_string("Invalid option specified: '%c'\n"), optopt);
		    }
		else
		    {
		    // Bad long option.
		    printf(OBFS_string("Invalid option specified: '%s'\n"), OBFN_argv[--optind]);
		    }
		OBFN_got_error = 1;
		break;
	    }
	if (OBFN_got_error)
	    {
	    break;
	    }
	}
    if (!OBFN_got_error && optind < OBFN_argc)
	{
	printf(OBFS_string("Invalid option specified: '%s'\n"), OBFN_argv[optind] ? OBFN_argv[optind] : OBFN_argv[optind-1]);
	OBFN_got_error = 1;
	}

    if (!OBFN_got_error)
	{
	if (OBFN_debug)
	    {
	    printf(OBFS_string("Warning threshold:  %ld\n"), OBFN_warn);
	    printf(OBFS_string("Critical threshold:  %ld\n"), OBFN_crit);
	    }

	if (OBFN_show_version)
	    {
	    OBFN_print_version();
	    OBFN_exit_status = EXIT_OK;
	    }
	else if (OBFN_show_help)
	    {
	    OBFN_print_usage();
	    OBFN_exit_status = EXIT_UNKNOWN;
	    }
	else
	    {
	    if (OBFN_initialize() < 0)
		{
		OBFN_exit_status = EXIT_UNKNOWN;
		printf (OBFS_string("ERROR: Internal error; cannot analyze your database configuration.\n"));
		}
	    else
		{
		int OBFN_host_device_count = OBFN_count_host_devices ();
		if (OBFN_host_device_count < 0)
		    {
		    OBFN_exit_status = EXIT_UNKNOWN;
		    OBFN_host_device_count = 0;
		    snprintf (OBFN_plugin_message, sizeof(OBFN_plugin_message), OBFS_string("Unknown:  Cannot count hosts:  %s."), OBFN_database_error);
		    }
		else if (OBFN_crit > 0 && OBFN_host_device_count >= OBFN_crit)
		    {
		    OBFN_exit_status = EXIT_CRITICAL;
		    snprintf (OBFN_plugin_message, sizeof(OBFN_plugin_message),
			OBFS_string("Critical: You have %d hosts, which is equal to or greater than your critical threshold of %ld."),
			OBFN_host_device_count, OBFN_crit);
		    }
		else if (OBFN_warn > 0 && OBFN_host_device_count >= OBFN_warn)
		    {
		    OBFN_exit_status = EXIT_WARNING;
		    snprintf (OBFN_plugin_message, sizeof(OBFN_plugin_message),
			OBFS_string("Warning: You have %d hosts, which is equal to or greater than your warning threshold of %ld."),
			OBFN_host_device_count, OBFN_warn);
		    }
		else
		    {
		    OBFN_exit_status = EXIT_OK;
		    snprintf (OBFN_plugin_message, sizeof(OBFN_plugin_message), OBFS_string("OK: You have %d hosts."), OBFN_host_device_count);
		    }

		if (OBFN_perf)
		    {
		    printf (OBFS_string("%s | Hosts=%d;%ld;%ld\n"), OBFN_plugin_message, OBFN_host_device_count, OBFN_warn, OBFN_crit);
		    }
		else
		    {
		    printf ("%s\n", OBFN_plugin_message);
		    }
		}
	    }
	}

    return (OBFN_exit_status);
    }
