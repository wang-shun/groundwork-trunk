// commit_check.c

// Copyright (c) 2010-2013 by GroundWork Open Source, Inc.
// All rights reserved.

// For operation in any mode:
// (0) Look at argv[0] to tell what mode to operate in (commit_check or add_check).  If the program is invoked as something
//     else, emit nothing and return an exit code of 5.

// For operation as commit_check:
// (1) Form an opinion as to how large the final GWCollageDB configuration will be after a commit.
//     Calculation to run:
//     monarch_hosts = "select count(*) from monarch.hosts"
//     extra_devices = "select count(*) from GWCollageDB.Device where DeviceID not in (select DeviceID from GWCollageDB.Host)"
//     total_devices = monarch_hosts + extra_devices
// (2) Figure out the max number of devices allowed by the license key, and whether that limit is being enforced.
// (3) If you were unable to run the calculations, emit a slightly cryptic error message and return a non-zero exit code.
// (4) If the total after commit will be larger than the allowed number, emit a warning message and return a zero exit code.
// (5) Otherwise, emit nothing (or just a fixed palliative message) and return a zero exit code.

// For operation as add_check:
// (1) Accept a single non-negative numeric parameter on the command line ("requested_devices").
//     If this fails, then emit nothing, and return an exit code of 4.
// (2) Form an opinion as to how large the final GWCollageDB configuration would be after a commit,
//     if that many devices were added to the present configuration.
//     Calculation to run:
//     monarch_hosts = "select count(*) from monarch.hosts"
//     extra_devices = "select count(*) from GWCollageDB.Device where DeviceID not in (select DeviceID from GWCollageDB.Host)"
//     total_devices = monarch_hosts + extra_devices + requested_devices
// (3) Figure out the soft limit of the number of devices allowed by the license key, and whether that limit is being enforced.
// (4) If you were unable to run the calculations, then emit nothing, and return an exit code of 3.
// (5) If the total, including the requested number, would be larger than the allowed number, then emit nothing, and return an
//     exit code of 2.
// (6) Otherwise, emit nothing, and return a zero exit code.
// (7) An exit code of 1 is reserved for other general failure types, unspecified here.

// Use the complex (de)obfuscation algorithm.
#define OBFN_SCRAMBLE	1

#define _GNU_SOURCE

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <regex.h>
#include <errno.h>

#define	VERSION "3.0.0"

// Identifying strings so we can distinguish different compilations of the programs.  These
// are intentionally not obfuscated, so the "ident" program can dig them out and print them.
static char version[]      = "$Version: " VERSION " $";
static char compile_time[] = "$CompileTime: " __TIME__ " on " __DATE__ " $";

#if OBFN_SCRAMBLE
#define	OBFN_NUL_TERM_LEN	1	// space for 1 byte (for '\0' string termination)
#endif

#define OBFN_NO_REG_EFLAGS	0

#if SUPPORT_MYSQL

// This header should be drawn from "/usr/local/groundwork/mysql/include/mysql.h"
// and not from "/usr/include/mysql/mysql.h".  That depends on whatever -I options
// are used for the compilation.
#include "mysql.h"

typedef MYSQL     *OBFN_MYSQL_DB_HANDLE;
typedef MYSQL_RES *OBFN_MYSQL_DB_RESULT;
typedef MYSQL_ROW  OBFN_MYDQL_DB_ROW;

// A couple of values to use as MySQL my_bool values. 
#define OBFN_my_false        0
#define OBFN_my_true         (! OBFN_my_false)

#endif

#if SUPPORT_POSTGRESQL

// This header should be drawn from "/usr/local/groundwork/postgresql/include/libpq-fe.h"
// and not from "/opt/PostgreSQL/9.1/include/libpq-fe.h".  That depends on whatever -I options
// are used for the compilation.
#include "libpq-fe.h"

typedef PGconn   *OBFN_POSTGRESQL_DB_HANDLE;
typedef PGresult *OBFN_POSTGRESQL_DB_RESULT;

#endif

enum OBFN_database_type {OBFN_DB_IS_UNKNOWN, OBFN_DB_IS_MYSQL, OBFN_DB_IS_POSTGRESQL};

static enum OBFN_database_type OBFN_db_type = OBFN_DB_IS_UNKNOWN;

static int OBFN_show_detail = 0;

#if OBFN_SCRAMBLE

// Optional selection of possible additional algorithm steps
// (which are not really needed for effective obfuscation,
// and are not currently included in the Java implementation).
// #define OBFN_SWAP_NIBBLES
// #define OBFN_XOR_NIBBLES

static char *OBFN_xor_chars = NULL;
#ifdef OBFN_XOR_NIBBLES
static char *OBFN_nib_chars = NULL;
#endif

// Why use 0123456789ABCDEF when lots more entropy is available?
// 0123456789ABCDEF  simple hex chars (the all-too-obvious choice) ...
// QlZBASPXHdvsuomt  ... or in the alternative, some visually similar chars ...
// SPXHdvsuomtQlZBA  ... rotated for slight obfuscation ...
// nbTFmHsaLdJiYVRw  ... or random unique chars
static char *OBFN_hex_chars = NULL;

static char OBFN_ordinal[256];

struct OBFN_values
    {
    char *OBFN_name;
    char **OBFN_value;
    };

static void OBFN_prepare () {
    int OBFN_i;
    OBFN_xor_chars = OBFS_string("\xA5\xD2\x69\xB4\x5A\x2D\x96\x4B");
#ifdef OBFN_XOR_NIBBLES
    OBFN_nib_chars = OBFS_string("\xF0\x78\x3C\x1E\x0F\x87\xC3\xE1");
#endif
    OBFN_hex_chars = OBFS_string("nbTFmHsaLdJiYVRw");
    for (OBFN_i = 256; --OBFN_i >= 0; ) {
	// First, fill the entire array with invalid values,
	// to detect any inappropriate incoming values.
	OBFN_ordinal[OBFN_i] = '\xff';
    }
    for (OBFN_i = strlen(OBFN_hex_chars); --OBFN_i >= 0; ) {
	// Then populate the positions we actually care about.
	OBFN_ordinal[OBFN_hex_chars[OBFN_i]] = OBFN_i;
    }
}

// The buffer returned by this routine must be free()d by the caller.
// Returning a NULL pointer means the input was indecipherable.
static char *OBFN_clarify (char *OBFN_buf, int OBFN_buflen) {
    int OBFN_i;
    int OBFN_len = OBFN_buflen / 2;
    if (OBFN_len * 2 != OBFN_buflen) {
	return (NULL);
    }
    // unscramble nibbles and dehexify
    char *OBFN_str = malloc(OBFN_len + OBFN_NUL_TERM_LEN);
    char *OBFN_l = OBFN_buf + OBFN_len;
    char *OBFN_h = OBFN_buf + OBFN_buflen;
    char *OBFN_s = OBFN_str + OBFN_len;
    *OBFN_s = '\0';
    for (OBFN_i = OBFN_len; --OBFN_i >= 0; ) {
	int OBFN_hi = OBFN_ordinal[*--OBFN_h];
	int OBFN_lo = OBFN_ordinal[*--OBFN_l];
	if (OBFN_hi == '\xff' || OBFN_lo == '\xff') {
	    free(OBFN_str);
	    return (NULL);
	}
	*--OBFN_s = (OBFN_hi << 4) + OBFN_lo;
    }
    OBFN_s = OBFN_str + OBFN_len - 1;
    for (OBFN_i = OBFN_len - 1; --OBFN_i >= 0; ) {
	--OBFN_s;
	OBFN_s[1] -= OBFN_s[0];
    }
    char *OBFN_xor;
#ifdef OBFN_XOR_NIBBLES
    OBFN_xor = OBFN_nib_chars;
    OBFN_s = OBFN_str + OBFN_len;
    for (OBFN_i = OBFN_len; --OBFN_i >= 0; ) {
	*--OBFN_s ^= *OBFN_xor++;
	if (!*OBFN_xor) OBFN_xor = OBFN_nib_chars;
    }
#endif
#ifdef OBFN_SWAP_NIBBLES
    OBFN_s = OBFN_str;
    for (OBFN_i = OBFN_len; --OBFN_i >= 0; ) {
	*OBFN_s = ((*OBFN_s & 0x00f0) >> 4) | ((*OBFN_s & 0x0f) << 4);
	++OBFN_s;
    }
#endif
    OBFN_s = OBFN_str;
    for (OBFN_i = 1; OBFN_i < OBFN_len; ++OBFN_i) {
	++OBFN_s;
	OBFN_s[-1] -= OBFN_s[0];
    }
    OBFN_xor = OBFN_xor_chars;
    OBFN_s = OBFN_str;
    for (OBFN_i = 0; OBFN_i < OBFN_len; ++OBFN_i) {
	*OBFN_s ^= *OBFN_xor++;
	*OBFN_s -= *OBFN_s << 4;
	if (!*OBFN_xor) OBFN_xor = OBFN_xor_chars;
	++OBFN_s;
    }
    return (OBFN_str);
}

#endif

#if SUPPORT_MYSQL

// Returns a valid OBFN_MYSQL_DB_HANDLE on success, a null pointer on error.
static OBFN_MYSQL_DB_HANDLE OBFN_mysql_dbconnect (const char *OBFN_host, int OBFN_port, const char *OBFN_user, const char *OBFN_pass, const char *OBFN_data)
    {
    static const my_bool OBFN_my_bool_true = OBFN_my_true;
    // static pthread_once_t mysql_library_init_just_once = PTHREAD_ONCE_INIT;
    OBFN_MYSQL_DB_HANDLE OBFN_mysql;
    unsigned int OBFN_connect_timeout = 30;  // seconds; on Linux, this timeout may also be used for waiting for the first answer from the server
    unsigned int OBFN_connect_protocol = MYSQL_PROTOCOL_TCP;

    // pthread_once (&mysql_library_init_just_once, call_mysql_library_init_once);
    
    if (mysql_thread_init ())
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot initialize thread-specific data for MySQL\n"));
	return (NULL);
	}
    
    if (! (OBFN_mysql = mysql_init (NULL)))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot obtain a handle for the database\n"));
	mysql_thread_end ();
	return (NULL);
	} 
    
    // Among other option settings, if the selected port is non-zero, force the use of a TCP connection,
    // thereby overriding the default selection of protocol based on the form of the selected hostname.
    // Such a setup allows us to use port forwarding to access a remote database in a development environment.

    if ((OBFN_port != 0 &&
	mysql_options (OBFN_mysql, MYSQL_OPT_PROTOCOL,        (char *) &OBFN_connect_protocol)) ||
	mysql_options (OBFN_mysql, MYSQL_OPT_CONNECT_TIMEOUT, (char *) &OBFN_connect_timeout  ) ||
	mysql_options (OBFN_mysql, MYSQL_OPT_RECONNECT,       &OBFN_my_bool_true              ))
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot set mysql options (%s)\n"), mysql_error (OBFN_mysql));
	mysql_close (OBFN_mysql);
	mysql_thread_end ();
	return (NULL);
	}

    if (! mysql_real_connect (OBFN_mysql, OBFN_host, OBFN_user, OBFN_pass, OBFN_data, OBFN_port,
	/* const char *unix_socket */ NULL, CLIENT_IGNORE_SPACE | CLIENT_MULTI_STATEMENTS)) 
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot connect to the %s database (%s)\n"), OBFN_data, mysql_error (OBFN_mysql));
	mysql_close (OBFN_mysql);
	mysql_thread_end ();
	return (NULL);
	}

    return (OBFN_mysql);
    }

static int OBFN_mysql_dbdisconnect (OBFN_MYSQL_DB_HANDLE OBFN_database)
    {
    if (OBFN_database)
	{
	mysql_close (OBFN_database);
	mysql_thread_end ();
	}
    return (0);
    }

#endif

#if SUPPORT_POSTGRESQL

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
    if (0 && OBFN_show_detail) {
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
    if (OBFN_connect_keywords == NULL || OBFN_connect_values == NULL) {
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

#endif

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

static int OBFN_new_config_size ()
    {
    char OBFN_long_line[10000];
    int OBFN_monarch_hosts = -1;
    int OBFN_extra_devices = -1;
    int OBFN_total_devices = -1;

    char *OBFN_global_db_type = NULL;

    char *OBFN_monarch_host = NULL;
    char *OBFN_monarch_PORT = NULL;	// not actually present in db.properties, at least for now
    int   OBFN_monarch_port = 0;	// not actually present in db.properties, at least for now
    char *OBFN_monarch_user = NULL;
    char *OBFN_monarch_pass = NULL;
    char *OBFN_monarch_data = NULL;

    char *OBFN_collage_host = NULL;
    char *OBFN_collage_PORT = NULL;	// not actually present in db.properties, at least for now
    int   OBFN_collage_port = 0;	// not actually present in db.properties, at least for now
    char *OBFN_collage_user = NULL;
    char *OBFN_collage_pass = NULL;
    char *OBFN_collage_data = NULL;

    struct OBFN_values OBFN_config_values[] =
	{
	{ OBFS_string("global.db.type"),   &OBFN_global_db_type },
	{ OBFS_string("monarch.dbhost"),   &OBFN_monarch_host   },
	{ OBFS_string("monarch.dbport"),   &OBFN_monarch_PORT   },
	{ OBFS_string("monarch.username"), &OBFN_monarch_user   },
	{ OBFS_string("monarch.password"), &OBFN_monarch_pass   },
	{ OBFS_string("monarch.database"), &OBFN_monarch_data   },
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

#if (SUPPORT_MYSQL) && !(SUPPORT_POSTGRESQL)
    OBFN_db_type = OBFN_DB_IS_MYSQL;
#elif !(SUPPORT_MYSQL) && (SUPPORT_POSTGRESQL)
    OBFN_db_type = OBFN_DB_IS_POSTGRESQL;
#elif (SUPPORT_MYSQL) && (SUPPORT_POSTGRESQL)
    // Default to MySQL, but allow for PostgreSQL, which must be explicitly specified to be chosen.
    OBFN_db_type = OBFN_global_db_type == NULL ? OBFN_DB_IS_MYSQL : strcmp (OBFN_global_db_type, OBFS_string("postgresql")) == 0 ? OBFN_DB_IS_POSTGRESQL : OBFN_DB_IS_MYSQL;
#elif !(SUPPORT_MYSQL) && !(SUPPORT_POSTGRESQL)
    if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: Cannot analyze your database type.\n"));
    return (-1);
#else
    #error Bad compilation; this cannot happen!
#endif

    OBFN_monarch_port = OBFN_monarch_PORT ? atoi(OBFN_monarch_PORT) : (OBFN_db_type == OBFN_DB_IS_POSTGRESQL) ? 5432 : 3306;
    OBFN_collage_port = OBFN_collage_PORT ? atoi(OBFN_collage_PORT) : (OBFN_db_type == OBFN_DB_IS_POSTGRESQL) ? 5432 : 3306;

#if SUPPORT_MYSQL
    OBFN_MYSQL_DB_HANDLE OBFN_mysql_monarch;
    OBFN_MYSQL_DB_HANDLE OBFN_mysql_collage;
#endif
#if SUPPORT_POSTGRESQL
    OBFN_POSTGRESQL_DB_HANDLE OBFN_postgresql_monarch;
    OBFN_POSTGRESQL_DB_HANDLE OBFN_postgresql_collage;
#endif

    switch (OBFN_db_type)
	{
	case OBFN_DB_IS_MYSQL:
#if SUPPORT_MYSQL
	    OBFN_mysql_monarch = OBFN_mysql_dbconnect (OBFN_monarch_host, OBFN_monarch_port, OBFN_monarch_user, OBFN_monarch_pass, OBFN_monarch_data);
	    if (OBFN_mysql_monarch)
		{
		do  {
		    //     OBFN_monarch_hosts = "select count(*) from hosts"
		    int OBFN_result = mysql_query(OBFN_mysql_monarch, OBFS_string("select count(*) from hosts"));
		    if (OBFN_result) break;
		    OBFN_MYSQL_DB_RESULT OBFN_RESULT = mysql_store_result(OBFN_mysql_monarch);
		    if (!OBFN_RESULT) break;
		    OBFN_MYDQL_DB_ROW OBFN_ROW = mysql_fetch_row(OBFN_RESULT);
		    if (!OBFN_ROW) break;
		    OBFN_monarch_hosts = atoi(OBFN_ROW[0] ? OBFN_ROW[0] : OBFS_string("-1"));
		    mysql_free_result(OBFN_RESULT);
		    } while (0);
		OBFN_mysql_dbdisconnect (OBFN_mysql_monarch);
		}

	    OBFN_mysql_collage = OBFN_mysql_dbconnect (OBFN_collage_host, OBFN_collage_port, OBFN_collage_user, OBFN_collage_pass, OBFN_collage_data);
	    if (OBFN_mysql_collage)
		{
		do  {
		    //     OBFN_extra_devices = "select count(*) from Device where DeviceID not in (select DeviceID from Host)"
		    int OBFN_result = mysql_query(OBFN_mysql_collage, OBFS_string("select count(*) from Device where DeviceID not in (select DeviceID from Host)"));
		    if (OBFN_result) break;
		    OBFN_MYSQL_DB_RESULT OBFN_RESULT = mysql_store_result(OBFN_mysql_collage);
		    if (!OBFN_RESULT) break;
		    OBFN_MYDQL_DB_ROW OBFN_ROW = mysql_fetch_row(OBFN_RESULT);
		    if (!OBFN_ROW) break;
		    OBFN_extra_devices = atoi(OBFN_ROW[0] ? OBFN_ROW[0] : OBFS_string("-1"));
		    mysql_free_result(OBFN_RESULT);
		    } while (0);
		OBFN_mysql_dbdisconnect (OBFN_mysql_collage);
		}
#endif
	    break;

	case OBFN_DB_IS_POSTGRESQL:
#if SUPPORT_POSTGRESQL
	    OBFN_postgresql_monarch = OBFN_postgresql_dbconnect (OBFN_monarch_host, OBFN_monarch_port, OBFN_monarch_user, OBFN_monarch_pass, OBFN_monarch_data);
	    if (OBFN_postgresql_monarch)
		{
		do  {
		    //     OBFN_monarch_hosts = "select count(*) from hosts"
		    OBFN_POSTGRESQL_DB_RESULT OBFN_RESULT = PQexecParams(OBFN_postgresql_monarch,
			OBFS_string("select count(*) from hosts"), 0, NULL, NULL, NULL, NULL, 0);
		    if (!OBFN_RESULT)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: Cannot count hosts:  %s"), PQerrorMessage(OBFN_postgresql_monarch));
			break;
			}
		    if (PQresultStatus (OBFN_RESULT) != PGRES_TUPLES_OK)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: Cannot count hosts:  %s"), PQresultErrorMessage(OBFN_RESULT));
			break;
			}
		    OBFN_monarch_hosts = atoi(PQgetvalue(OBFN_RESULT, 0, 0));
		    PQclear(OBFN_RESULT);
		    } while (0);
		OBFN_postgresql_dbdisconnect (OBFN_postgresql_monarch);
		}

	    OBFN_postgresql_collage = OBFN_postgresql_dbconnect (OBFN_collage_host, OBFN_collage_port, OBFN_collage_user, OBFN_collage_pass, OBFN_collage_data);
	    if (OBFN_postgresql_collage)
		{
		do  {
		    //     OBFN_extra_devices = "select count(*) from Device where DeviceID not in (select DeviceID from Host)"
		    OBFN_POSTGRESQL_DB_RESULT OBFN_RESULT = PQexecParams(OBFN_postgresql_collage,
			OBFS_string("select count(*) from Device where DeviceID not in (select DeviceID from Host)"), 0, NULL, NULL, NULL, NULL, 0);
		    if (!OBFN_RESULT)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: Cannot count devices:  %s"), PQerrorMessage(OBFN_postgresql_collage));
			break;
			}
		    if (PQresultStatus (OBFN_RESULT) != PGRES_TUPLES_OK)
			{
			if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: Cannot count devices:  %s"), PQresultErrorMessage(OBFN_RESULT));
			break;
			}
		    OBFN_extra_devices = atoi(PQgetvalue(OBFN_RESULT, 0, 0));
		    PQclear(OBFN_RESULT);
		    } while (0);
		OBFN_postgresql_dbdisconnect (OBFN_postgresql_collage);
		}
#endif
	    break;

	default:
	    break;
	}

    if (OBFN_monarch_hosts >= 0 && OBFN_extra_devices >= 0)
	{
	OBFN_total_devices = OBFN_monarch_hosts + OBFN_extra_devices;
	}

    // printf (OBFS_string("monarch_hosts = %d, extra_devices = %d, total_devices = %d\n"), OBFN_monarch_hosts, OBFN_extra_devices, OBFN_total_devices);
    return (OBFN_total_devices);
    }

static int OBFN_hexdigit (int OBFN_byte)
    {
    if (OBFN_byte >= '0' && OBFN_byte <= '9') return (OBFN_byte - '0');
    if (OBFN_byte >= 'A' && OBFN_byte <= 'F') return (OBFN_byte - 'A' + 10);
    if (OBFN_byte >= 'a' && OBFN_byte <= 'f') return (OBFN_byte - 'a' + 10);
    return (-1);
    }

static int OBFN_license_limit ()
    {
    int OBFN_config_limit = -1;
#if ADD_CHECK
    int OBFN_soft_config_limit = -1;
#endif
    int OBFN_hard_config_limit = -1;
    char OBFN_long_line[10000];

#if ADD_CHECK
    char *OBFN_property_param_5  = NULL;
#endif
    char *OBFN_property_param_6  = NULL;
    char *OBFN_property_param_11 = NULL;

    struct OBFN_values OBFN_param_values[] =
	{
#if ADD_CHECK
	{ OBFS_string("property_param_5"),  &OBFN_property_param_5  },
#endif
	{ OBFS_string("property_param_6"),  &OBFN_property_param_6  },
	{ OBFS_string("property_param_11"), &OBFN_property_param_11 },
	};

    // read the license file
    // extract the value of OBFN_property_param_6=313235
    // return that value, or -1 if you encountered any errors
    FILE *OBFN_license = fopen (OBFS_string("/usr/local/groundwork/config/groundwork.lic"), OBFS_string("r"));
    if (!OBFN_license)
	{
	if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: cannot open license file\n"));
	return (-1);
	}
    while (fgets(OBFN_long_line, sizeof(OBFN_long_line), OBFN_license) != NULL)
	{
	if (regexec (&OBFN_param_value_expr, OBFN_long_line, sizeof (OBFN_matched_substrings) / sizeof (regmatch_t), OBFN_matched_substrings, OBFN_NO_REG_EFLAGS) == 0)
	    {
	    int OBFN_i;
	    char *OBFN_key = strndup(OBFN_long_line + OBFN_matched_substrings[1].rm_so, OBFN_matched_substrings[1].rm_eo - OBFN_matched_substrings[1].rm_so);
	    char *OBFN_val = strndup(OBFN_long_line + OBFN_matched_substrings[2].rm_so, OBFN_matched_substrings[2].rm_eo - OBFN_matched_substrings[2].rm_so);
	    // printf (OBFS_string("key '%s' = val '%s'\n"), OBFN_key, OBFN_val);

	    for (OBFN_i = 0; OBFN_i < (sizeof (OBFN_param_values) / sizeof (struct OBFN_values)); ++OBFN_i)
		{
		if (strcmp(OBFN_key, OBFN_param_values[OBFN_i].OBFN_name) == 0)
		    {
		    *OBFN_param_values[OBFN_i].OBFN_value = OBFN_val;
		    }
		}
	    }
	}
    fclose (OBFN_license);

#if ADD_CHECK
    if (OBFN_property_param_5)
	{
	int OBFN_number = 0;

#if OBFN_SCRAMBLE
	char *OBFN_dec = OBFN_clarify(OBFN_property_param_5,strlen(OBFN_property_param_5));
	if (OBFN_dec == NULL) {
	    if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
	    return (-1);
	}
	OBFN_number = atoi(OBFN_dec);
#else
	char *OBFN_pos = OBFN_property_param_5;
	while (OBFN_pos[0] && OBFN_pos[1])
	    {
	    int OBFN_upper = OBFN_hexdigit(OBFN_pos[0]);
	    int OBFN_lower = OBFN_hexdigit(OBFN_pos[1]);
	    if (OBFN_upper < 0 || OBFN_lower < 0)
		{
		if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
		return (-1);
		}
	    OBFN_number = OBFN_number * 10 + ((OBFN_upper * 16 + OBFN_lower) - '0');
	    OBFN_pos += 2;
	    }
#endif

	OBFN_soft_config_limit = OBFN_number;
	// printf (OBFS_string("Config soft limit is %d\n"), OBFN_soft_config_limit);
	}
#endif

    if (OBFN_property_param_6)
	{
	int OBFN_number = 0;

#if OBFN_SCRAMBLE
	char *OBFN_dec = OBFN_clarify(OBFN_property_param_6,strlen(OBFN_property_param_6));
	if (OBFN_dec == NULL) {
	    if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
	    return (-1);
	}
	OBFN_number = atoi(OBFN_dec);
#else
	char *OBFN_pos = OBFN_property_param_6;
	while (OBFN_pos[0] && OBFN_pos[1])
	    {
	    int OBFN_upper = OBFN_hexdigit(OBFN_pos[0]);
	    int OBFN_lower = OBFN_hexdigit(OBFN_pos[1]);
	    if (OBFN_upper < 0 || OBFN_lower < 0)
		{
		if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
		return (-1);
		}
	    OBFN_number = OBFN_number * 10 + ((OBFN_upper * 16 + OBFN_lower) - '0');
	    OBFN_pos += 2;
	    }
#endif

	OBFN_hard_config_limit = OBFN_number;
	// printf (OBFS_string("Config hard limit is %d\n"), OBFN_hard_config_limit);
	}

    if (OBFN_property_param_11)
	{

#if OBFN_SCRAMBLE
	char *OBFN_dec = OBFN_clarify(OBFN_property_param_11,strlen(OBFN_property_param_11));
	if (OBFN_dec == NULL) {
	    if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
	    return (-1);
	}
	strcpy(OBFN_property_param_11, OBFN_dec);
#else
	char *OBFN_pos = OBFN_property_param_11;
	char *OBFN_one_char = OBFN_property_param_11;
	while (OBFN_pos[0] && OBFN_pos[1])
	    {
	    int OBFN_upper = OBFN_hexdigit(OBFN_pos[0]);
	    int OBFN_lower = OBFN_hexdigit(OBFN_pos[1]);
	    if (OBFN_upper < 0 || OBFN_lower < 0)
		{
		if (OBFN_show_detail) fprintf (stderr, OBFS_string("ERROR: character out of bounds\n"));
		return (-1);
		}
	    *OBFN_one_char++ = OBFN_upper * 16 + OBFN_lower;
	    OBFN_pos += 2;
	    }
	*OBFN_one_char = '\0';
#endif

	// printf (OBFS_string("param 11 = %s\n"), OBFN_property_param_11);
#if ADD_CHECK
	if (!strstr(OBFN_property_param_11, OBFS_string("param_5")))
	    {
	    OBFN_soft_config_limit = 1<<30;
	    }
#endif
	if (!strstr(OBFN_property_param_11, OBFS_string("param_6")))
	    {
	    OBFN_hard_config_limit = 1<<30;
	    }
#if COMMIT_CHECK
	OBFN_config_limit = OBFN_hard_config_limit;
#endif
#if ADD_CHECK
	OBFN_config_limit =
	    OBFN_hard_config_limit < 0 ? -1 :
	    OBFN_soft_config_limit < 0 ? -1 :
	    OBFN_soft_config_limit < OBFN_hard_config_limit ?
	    OBFN_soft_config_limit : OBFN_hard_config_limit;
#endif
	}
    else
	{
	// We need to see what parameters are being constrained.
	// If there is no such specification, then the license key is invalid.
	OBFN_config_limit = -1;
	}

    return (OBFN_config_limit);
    }

#define	OBFN_BAD_INVOCATION	5
#define	OBFN_BAD_PARAMETER	4
#define	OBFN_BAD_CALCULATIONS	3
#define	OBFN_OVER_LICENSE_LIMIT	2

int main (int OBFN_argc, char *OBFN_argv[])
    {
    int OBFN_failed = 0;
    int OBFN_proposed_config_size = 0;
    int OBFN_config_limit = 0;

    int OBFN_operate_as_commit_check = 0;
    int OBFN_operate_as_add_check    = 0;

    char *OBFN_last_slash = strrchr(OBFN_argv[0], '/');

    OBFN_operate_as_commit_check = !strcmp(OBFN_last_slash ? OBFN_last_slash + 1 : OBFN_argv[0], OBFS_string("commit_check"));
    OBFN_operate_as_add_check    = !strcmp(OBFN_last_slash ? OBFN_last_slash + 1 : OBFN_argv[0], OBFS_string("add_check"));

    if (!OBFN_operate_as_commit_check && !OBFN_operate_as_add_check)
        {
	return (OBFN_BAD_INVOCATION);
	}

#if COMMIT_CHECK
    if (--OBFN_argc == 1 && strcmp(OBFN_argv[1], OBFS_string("--detail")) == 0)
	{
	OBFN_show_detail = 1;
	}
#endif
#if ADD_CHECK
    long OBFN_requested_device_count = -1;
    char *OBFN_endptr = "";
    errno = 0;
    if (--OBFN_argc == 1)
	{
	OBFN_requested_device_count = strtol(OBFN_argv[1], &OBFN_endptr, 10);
	}
    if ( OBFN_requested_device_count < 0 || OBFN_endptr == OBFN_argv[1] || *OBFN_endptr != '\0' || errno != 0 )
	{
	return (OBFN_BAD_PARAMETER);
	}
#endif

    // We want each message to be distinguishable from the other error messages,
    // and not to be specifically indicative of looking at the license-key file
    // or at any other particular source of information.
    if (OBFN_initialize() < 0)
	{
#if COMMIT_CHECK
	printf (OBFS_string("ERROR: Internal error; cannot analyze your database configuration.\n"));
#endif
	OBFN_failed = 1;
	}
    else
	{
	OBFN_proposed_config_size = OBFN_new_config_size();
	OBFN_config_limit = OBFN_license_limit();

#if COMMIT_CHECK
	if (OBFN_proposed_config_size < 0)
	    {
	    printf (OBFS_string("ERROR: Cannot analyze your database configuration.\n"));
	    OBFN_failed = 1;
	    }
	else if (OBFN_config_limit < 0)
	    {
	    printf (OBFS_string("ERROR: Cannot analyze your configuration.\n"));
	    OBFN_failed = 1;
	    }
	else if (OBFN_proposed_config_size > OBFN_config_limit)
	    {
	    // The extra level of indirection in the link (using the javascript: pseudo-protocol to
	    // set the location instead of just directly using href="mailto:foo@bar.com" with the
	    // _blank target window) is needed to force opening the email tool in a separate window,
	    // leaving the GroundWork Monitor window intact.  For some reason, while a direct http:
	    // link would have forced a new window (or tab), mailto: doesn't do so on its own.  The
	    // only problem now is that the browser presents this as a link rather than as an email
	    // address, meaning the user cannot copy/paste the email address into some other email
	    // tool such as Thunderbird without also picking up the extra gobbledegook that will
	    // then have to be manually edited out.  But that is a better choice than having the
	    // GroundWork Monitor window disappear.
	    printf
		(
		OBFS_string("%s%s"),
		OBFS_string("WARNING: Committing this configuration will exceed your license limit.\n"),
		OBFS_string("Please contact GroundWork Open Source (<a style='text-decoration: none;' href='javascript:location=\"mailto:support@gwos.com?subject=need%20help%20with%20license%20limit\"' target='_blank'>support@gwos.com</a>) before committing this configuration.\n")
		);
	    }
	else
	    {
	    if (OBFN_show_detail)
		{
		fprintf (stderr, OBFS_string("Database type is %s.\n"),
		    OBFN_db_type == OBFN_DB_IS_UNKNOWN    ? OBFS_string("unknown")    :
		    OBFN_db_type == OBFN_DB_IS_MYSQL      ? OBFS_string("MySQL")      :
		    OBFN_db_type == OBFN_DB_IS_POSTGRESQL ? OBFS_string("PostgreSQL") : OBFS_string("broken")
		);
		// fprintf (stderr, OBFS_string("Config size = %d, config limit = %d.\n"), OBFN_proposed_config_size, OBFN_config_limit);
		}
	    printf (OBFS_string("Configuration looks okay.\n"));
	    }
#endif
#if ADD_CHECK
	if (OBFN_proposed_config_size < 0)
	    {
	    return (OBFN_BAD_CALCULATIONS);
	    }
	else if (OBFN_config_limit < 0)
	    {
	    return (OBFN_BAD_CALCULATIONS);
	    }
	else if (OBFN_proposed_config_size + OBFN_requested_device_count > OBFN_config_limit)
	    {
	    return (OBFN_OVER_LICENSE_LIMIT);
	    }
#endif
	}
    return (OBFN_failed ? EXIT_FAILURE : EXIT_SUCCESS);
    }
