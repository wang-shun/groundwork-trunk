// Emulation of (a portion of) the MySQL client API under PostgreSQL.
//
// This emulation only includes as much as is needed by the Cacti Spine
// code, and even then some adjustments will need to be in place within
// the Spine code to only call this emulation with SQL which is valid
// within PostgreSQL.

// Copyright (c) 2011 by GroundWork Open Source, Inc.  All rights reserved.
// Use is subject to GroundWork commercial license terms.

#include <stdlib.h>
#include <string.h>
#include <pthread.h>

#include "postgresql_as_mysql.h"

// ---------------------------------------------------------------- //

struct values {
    const char *name;
    const char **value;
};

// ---------------------------------------------------------------- //

static void unset_environment () {
    // Here we destroy a bunch of potentially-set environment variables that might
    // possibly provide unwanted defaults for parameters we don't set here explicitly,
    // to avoid any possibility of security issues or bypassing our intended connections.
    // This also means that we won't support more-secure connections than we have already
    // allowed for in the explicitly-set parameters below, so if we have some customer
    // who demands that (e.g., who wants only SSL connections to be valid), we will have
    // to come back here and revise this.
    unsetenv("PGHOST");
    unsetenv("PGHOSTADDR");
    unsetenv("PGPORT");
    unsetenv("PGDATABASE");
    unsetenv("PGUSER");
    unsetenv("PGPASSWORD");
    unsetenv("PGPASSFILE");
    unsetenv("PGSERVICE");
    unsetenv("PGSERVICEFILE");
    unsetenv("PGREALM");
    unsetenv("PGOPTIONS");
    unsetenv("PGAPPNAME");
    unsetenv("PGSSLMODE");
    unsetenv("PGREQUIRESSL");
    unsetenv("PGSSLCERT");
    unsetenv("PGSSLKEY");
    unsetenv("PGSSLROOTCERT");
    unsetenv("PGSSLCRL");
    unsetenv("PGREQUIREPEER");
    unsetenv("PGKRBSRVNAME");
    unsetenv("PGGSSLIB");
    unsetenv("PGCONNECT_TIMEOUT");
    unsetenv("PGCLIENTENCODING");
    unsetenv("PGDATESTYLE");
    unsetenv("PGTZ");
    unsetenv("PGGEQO");
    unsetenv("PGSYSCONFDIR");
    unsetenv("PGLOCALEDIR");
}

// ---------------------------------------------------------------- //

// Note that Spine never calls mysql_autocommit(), but then it also never calls mysql_commit()
// either.  From this we conclude that it must be treating the database connection as though
// it defaults to auto-commit mode being enabled (every command that does not follow START
// TRANSACTION or BEGIN will automatically invoke a COMMIT).  Therefore, in theory we must take
// action to do the same with PostgreSQL.  However, PostgreSQL automatically behaves as though
// autocommit is enabled, and there doesn't seem to be any way to disable it, so we should be
// safe in that regard.

static pthread_once_t unset_environment_once_control = PTHREAD_ONCE_INIT;

// Returns a valid POSTGRESQL_DB_HANDLE on success, a null pointer on error.
static POSTGRESQL_DB_HANDLE postgresql_dbconnect (MYSQL *mysql_ptr, const char *host, unsigned int port,
    const char *user, const char *pass, const char *data, const char *connect_timeout) {
    POSTGRESQL_DB_HANDLE postgresql_db_handle;

    pthread_once(&unset_environment_once_control, unset_environment);

    char port_string[12];
    int outcome = snprintf (port_string, sizeof(port_string), "%u", port);
    if (outcome < 0 || outcome >= sizeof (port_string)) {
	mysql_ptr->error_message = "internal error";
	mysql_ptr->error_state   = "XX000";
	return (NULL);
    }

    // FIX LATER:
    // Possible postgres_options we might set:
    // statement_timeout(milliseconds)		time out long-running statements on the server side

    // Note that the database name must be specified as all-lowercase here, given that it actually exists within PostgreSQL
    // that way.  Mixed-case in this specification is not automatically lowercased during a call to PQconnectdbParams().
    // If we might not have lowercase in the incoming database name, we would have to lowercase the name explicitly here.
    const char *postgres_host                      = host;
    // const char *postgres_hostaddr                  = NULL;
    const char *postgres_port                      = port_string;
    const char *postgres_dbname                    = data;
    const char *postgres_user                      = user;
    const char *postgres_password                  = pass;
    const char *postgres_connect_timeout           = connect_timeout ? connect_timeout : "30";
    // const char *postgres_client_encoding           = NULL;
    // const char *postgres_options                   = NULL;	// FIX LATER:  check Chapter 18 in the PostgreSQL manual
    const char *postgres_application_name          = "spine";
    // const char *postgres_failback_application_name = NULL;
    // const char *postgres_keepalives                = NULL;
    // const char *postgres_keepalives_idle           = NULL;
    // const char *postgres_keepalives_interval       = NULL;
    // const char *postgres_keepalives_count          = NULL;
    // const char *postgres_tty                       = NULL;
    // const char *postgres_sslmode                   = NULL;
    // const char *postgres_requiressl                = NULL;
    // const char *postgres_sslcert                   = NULL;
    // const char *postgres_sslkey                    = NULL;
    // const char *postgres_sslrootcert               = NULL;
    // const char *postgres_sslcrl                    = NULL;
    const char *postgres_requirepeer               = "postgres";
    // const char *postgres_krbsrvname                = NULL;
    // const char *postgres_gsslib                    = NULL;
    // const char *postgres_service                   = NULL;

    // We ignore fields that don't seem relevant to our purposes here.
    struct values postgres_connect_values[] = {
	{ "host",                      &postgres_host                      },
	// { "hostaddr",                  &postgres_hostaddr                  },
	{ "port",                      &postgres_port                      },
	{ "dbname",                    &postgres_dbname                    },
	{ "user",                      &postgres_user                      },
	{ "password",                  &postgres_password                  },
	{ "connect_timeout",           &postgres_connect_timeout           },
	// { "client_encoding",           &postgres_client_encoding           },
	// { "options",                   &postgres_options                   },
	{ "application_name",          &postgres_application_name          },
	// { "failback_application_name", &postgres_failback_application_name },
	// { "keepalives",                &postgres_keepalives                },
	// { "keepalives_idle",           &postgres_keepalives_idle           },
	// { "keepalives_interval",       &postgres_keepalives_interval       },
	// { "keepalives_count",          &postgres_keepalives_count          },
	// { "tty",                       &postgres_tty                       },
	// { "sslmode",                   &postgres_sslmode                   },
	// { "requiressl",                &postgres_requiressl                },
	// { "sslcert",                   &postgres_sslcert                   },
	// { "sslkey",                    &postgres_sslkey                    },
	// { "sslrootcert",               &postgres_sslrootcert               },
	// { "sslcrl",                    &postgres_sslcrl                    },
	{ "requirepeer",               &postgres_requirepeer               },
	// { "krbsrvname",                &postgres_krbsrvname                },
	// { "gsslib",                    &postgres_gsslib                    },
	// { "service",                   &postgres_service                   },
    };

    const char **connect_keywords = calloc ((sizeof(postgres_connect_values) / sizeof(struct values)) + 1, sizeof(char *));
    const char **connect_values   = calloc ((sizeof(postgres_connect_values) / sizeof(struct values)) + 1, sizeof(char *));
    if (connect_keywords == NULL || connect_values == NULL) {
	mysql_ptr->error_message = "out of memory";
	mysql_ptr->error_state   = "53200";
	return (NULL);
    }

    int index;
    for (index = 0; index < sizeof(postgres_connect_values) / sizeof(struct values); ++index) {
	connect_keywords[index] =  postgres_connect_values[index].name;
	connect_values  [index] = *postgres_connect_values[index].value;
    }

    int expand_dbname = 0;
    if (! (postgresql_db_handle = PQconnectdbParams (connect_keywords, connect_values, expand_dbname))) {
	mysql_ptr->error_message = "out of memory";
	mysql_ptr->error_state   = "53200";
	return (NULL);
    }

    if (PQstatus (postgresql_db_handle) != CONNECTION_OK) {
	// We make a static buffer for this message rather than dynamically allocating a string here,
	// to preserve the convention that all other assignments to mysql_ptr->error_message use a
	// literal string, so we never have to worry about deallocation.  Having a static buffer
	// permanently ties up a small amount of memory, but we're willing to pay that price.
	static char connect_error[250];
	mysql_ptr->error_message = strncpy(connect_error, PQerrorMessage (postgresql_db_handle), sizeof(connect_error));
	connect_error[sizeof(connect_error) - 1] = '\0';
	// We have no "PGresult *" handle handy on which to run a call to PQresultErrorField() to find
	// the specific SQLSTATE for this failure.  So we have to just use a generic value reflecting
	// some kind of connection failure, without reflecting the actual reason for that failure in
	// the reported state.  Unfortunately, this will never then map into any of the particular
	// MySQL error codes which the Spine code is specifically looking for, which means it will
	// always try repeatedly to make another connection, no matter the cause of the failure.
	mysql_ptr->error_state = "08006";  // "08006" => connection failure
	PQfinish (postgresql_db_handle);
	return (NULL);
    }

    return (postgresql_db_handle);
}

// ---------------------------------------------------------------- //

static int postgresql_dbdisconnect (POSTGRESQL_DB_HANDLE database) {
    if (database) {
	PQfinish (database);
    }
    return (0);
}

// ---------------------------------------------------------------- //

void WINSTANDARDCALL mysql_close(MYSQL *mysql_ptr) {
    if (mysql_ptr->postgresql_db_handle) {
	postgresql_dbdisconnect (mysql_ptr->postgresql_db_handle);
	mysql_ptr->postgresql_db_handle = NULL;
    }
    if (mysql_ptr->connect_timeout) {
	free(mysql_ptr->connect_timeout);
	mysql_ptr->connect_timeout = NULL;
    }

    // We must clean up any outstanding query results that were never handed off
    // to the client application.
    if (mysql_ptr->owns_mysql_res) {
	mysql_free_result(mysql_ptr->mysql_res_ptr);
    }

    // We don't free(mysql_ptr->error_message) because we only ever assign it literal strings
    // or strings whose memory management is otherwise already handled.
    // We don't free(mysql_ptr->error_state) because we only ever assign it literal strings.

    mysql_ptr->error_message = NULL;
    mysql_ptr->error_state   = NULL;

    if (mysql_ptr->is_dynamically_allocated) {
	free(mysql_ptr);
    }
}

// ---------------------------------------------------------------- //

// Spine calls mysql_errno() only after:
// * a call to mysql_query() that returns non-zero, looking for possible lock/deadlock errors
//   (for which it retries the operation up to 30 times)
// * a call to mysql_real_connect() that returns NULL, looking for possible unknown host,
//   unknown database, or access denied errors (for which it stops trying to retry the operation.
unsigned int WINSTANDARDCALL mysql_errno(MYSQL *mysql_ptr) {
    unsigned int error_code = 0;
    char *sqlstate =
	mysql_ptr->error_state ? mysql_ptr->error_state :
	(mysql_ptr->mysql_res_ptr && mysql_ptr->mysql_res_ptr->postgresql_db_result) ?
	    PQresultErrorField(mysql_ptr->mysql_res_ptr->postgresql_db_result, PG_DIAG_SQLSTATE) :
	    NULL;

    if (!sqlstate) {
	// This is not an error or warning result.
	error_code = 0;
    }
    else if (!strcmp(sqlstate, "28000") || !strcmp(sqlstate, "28P01")) {
	// 28000 => invalid authorization specification
	// 28P01 => invalid password
	// MySQL server-side "access denied for this user"
	error_code = 1045;
    }
    else if (!strcmp(sqlstate, "3D000")) {
	// 3D000 => invalid catalog name
	// MySQL server-side "unknown database"
	error_code = 1049;
    }
    else if (!strcmp(sqlstate, "55P03")) {
	// 55P03 => lock not available
	// MySQL server-side "lock wait timeout exceeded; try restarting transaction"
	error_code = 1205;
    }
    else if (!strcmp(sqlstate, "40001") || !strcmp(sqlstate, "40P01")) {
	// 40001 => serialization failure (not really sure if this should be retried, but what the heck)
	// 40P01 => deadlock detected
	// MySQL server-side "deadlock found when trying to get lock; try restarting transaction"
	error_code = 1213;
    }
    else if (!strcmp(sqlstate, "53200")) {
	// MySQL client-side "MySQL client ran out of memory"
	error_code = 2008;
    }
#if 0
    // I don't see anything in the available PostgreSQL error codes (Appendix A) that directly
    // corresponds to this particular MySQL error code.  So we simply will never emit it.
    else if (!strcmp(sqlstate, "xxxxx")) {
	// MySQL client-side "unknown MySQL server host"
	error_code = 2005;
    }
#endif
    else {
	// 1105 is a server-side MySQL "unknown error"
	// 2000 is a client-side MySQL "unknown MySQL error"
	error_code = 2000;
    }

    return (error_code);
}

// ---------------------------------------------------------------- //

const char * WINSTANDARDCALL mysql_error(MYSQL *mysql_ptr) {
    return (
	mysql_ptr->error_message ? mysql_ptr->error_message :
	mysql_ptr->postgresql_db_handle ? PQerrorMessage(mysql_ptr->postgresql_db_handle) :
	""
    );
}

// ---------------------------------------------------------------- //

// We take some care here to avoid walking off the end of the array if this routine
// is called too many times.  (The mysql_res_ptr->head_mysql_row_ptr array, which is
// pointed into by the mysql_res_ptr->next_mysql_row_ptr pointer, is terminated by
// a NULL pointer, for just this reason.)  A well-behaved application program won't
// call this routine too many times, but we protect against it just in case ...
MYSQL_ROW WINSTANDARDCALL mysql_fetch_row(MYSQL_RES *mysql_res_ptr) {
    MYSQL_ROW mysql_row = *mysql_res_ptr->next_mysql_row_ptr;
    if (mysql_row) {
	++mysql_res_ptr->next_mysql_row_ptr;
    }
    return (mysql_row);
}

// ---------------------------------------------------------------- //

void WINSTANDARDCALL mysql_free_result(MYSQL_RES *mysql_res_ptr) {
    if (mysql_res_ptr->postgresql_db_result) {
	PQclear(mysql_res_ptr->postgresql_db_result);
	mysql_res_ptr->postgresql_db_result = NULL;
    }

    if (mysql_res_ptr->head_mysql_row_ptr) {
	// First, walk the list of rows and free all the pointers in that list.
	// We cannot stop at the first *mysql_row_ptr which is a NULL pointer,
	// because the construction of the list of (row) pointers to lists of
	// (cell) pointers might have been aborted partway through because of
	// an out-of-memory condition.  Thus we must walk the entire list of
	// row pointers, no matter what.
	MYSQL_ROW *mysql_row_ptr;
	for ( mysql_row_ptr = &mysql_res_ptr->head_mysql_row_ptr[mysql_res_ptr->result_rows];
	    --mysql_row_ptr >= mysql_res_ptr->head_mysql_row_ptr; ) {
	    // We don't have to free() the cell pointers (pointers to individual
	    // retrieved values), because those belong to the PostgreSQL result
	    // structure and were already freed by the call to PQclear() above.
	    // Also note that free(NULL) is a safe no-op, so we don't bother to
	    // test each pointer before the call.
	    free (*mysql_row_ptr);
	}

	// Then, free the list of rows.
	free (mysql_res_ptr->head_mysql_row_ptr);
	mysql_res_ptr->head_mysql_row_ptr = NULL;
    }

    // We don't free(mysql_res_ptr->next_mysql_row_ptr) because it points into the same array
    // as mysql_res_ptr->head_mysql_row_ptr, and so doesn't need to be independently freed.
    mysql_res_ptr->next_mysql_row_ptr = NULL;

    mysql_res_ptr->result_rows = 0;
    mysql_res_ptr->result_cols = 0;

    free(mysql_res_ptr);
}

// ---------------------------------------------------------------- //

MYSQL * WINSTANDARDCALL mysql_init(MYSQL *mysql_ptr) {
    if (mysql_ptr == NULL) {
	mysql_ptr = calloc(1, sizeof (MYSQL));
	if (mysql_ptr == NULL) {
	    return (NULL);
	}
	mysql_ptr->is_dynamically_allocated = 1;
    }
    else {
	mysql_ptr->postgresql_db_handle     = NULL;
	mysql_ptr->connect_timeout          = NULL;
	mysql_ptr->mysql_res_ptr            = NULL;
	mysql_ptr->error_message            = NULL;
	mysql_ptr->error_state              = NULL;
	mysql_ptr->owns_mysql_res           = 0;
	mysql_ptr->is_dynamically_allocated = 0;
    }
    return (mysql_ptr);
}

// ---------------------------------------------------------------- //

my_ulonglong WINSTANDARDCALL mysql_num_rows(MYSQL_RES *mysql_res_ptr) {
    return (mysql_res_ptr->result_rows);
}

// ---------------------------------------------------------------- //

#define CONNECT_TIMEOUT_STRING_SIZE	12

// In Spine, this is used only to set the MYSQL_OPT_CONNECT_TIMEOUT and MYSQL_OPT_RECONNECT options.
int WINSTANDARDCALL mysql_options(MYSQL *mysql_ptr, enum mysql_option option, const char *option_argument) {
    int outcome = 0;
    mysql_ptr->error_message = NULL;
    mysql_ptr->error_state   = NULL;

    switch (option) {
	unsigned int timeout;

	// Save the desired connect timeout in a place that will be found
	// when we actually attempt to connect to the server.
	case MYSQL_OPT_CONNECT_TIMEOUT:
	    timeout = *(unsigned int *)option_argument;
	    if (! mysql_ptr->connect_timeout) {
		mysql_ptr->connect_timeout = malloc (CONNECT_TIMEOUT_STRING_SIZE);
		if (mysql_ptr->connect_timeout == NULL) {
		    mysql_ptr->error_message = "out of memory";
		    mysql_ptr->error_state   = "53200";
		    outcome = -1;
		    break;
		}
	    }
	    int size = snprintf (mysql_ptr->connect_timeout, CONNECT_TIMEOUT_STRING_SIZE, "%u", timeout);
	    if (size <= 0 || size >= CONNECT_TIMEOUT_STRING_SIZE) {
		mysql_ptr->error_message = "internal error";
		mysql_ptr->error_state   = "XX000";
		outcome = -1;
		break;
	    }
	    outcome = 0;
	    break;

	// Spine purports to call mysql_options() to set the MYSQL_OPT_RECONNECT option,
	// but the Spine code confuses the MYSQL_OPT_RECONNECT enumeration value with a
	// possible preprocessor symbol (#ifdef MYSQL_OPT_RECONNECT) and thus in fact
	// this never gets compiled in and called.  Thus we're okay in pretending that
	// the call succeeded, which is good because the PostgreSQL library does not
	// have an equivalent mechanism for auto-reconnect.
	case MYSQL_OPT_RECONNECT:
	    outcome = 0;
	    break;

	default:
	    // Unimplemented option.  Let the calling program know, so this can be fixed.
	    mysql_ptr->error_message = "feature not supported";
	    mysql_ptr->error_state   = "0A000";
	    outcome = -1;
	    break;
    }

    return (outcome);
}

// ---------------------------------------------------------------- //

// returns zero on success, non-zero on failure
int WINSTANDARDCALL mysql_query(MYSQL *mysql_ptr, const char *query) {
    int outcome = 0;
    mysql_ptr->error_message = NULL;
    mysql_ptr->error_state   = NULL;

    if (mysql_ptr->owns_mysql_res) {
	// We destroy this existing object and create a new one, instead of just re-using
	// the existing object we already own, largely because the copy we own might
	// contain some partial results (additional allocations) that we need to clean up
	// before re-using the structure.  It's easier just to let the entire thing go,
	// using our centralized cleanup routine, and start from scratch.
	mysql_free_result(mysql_ptr->mysql_res_ptr);
	mysql_ptr->mysql_res_ptr  = NULL;
	mysql_ptr->owns_mysql_res = 0;
    }

    mysql_ptr->mysql_res_ptr = calloc(1, sizeof (MYSQL_RES));
    if (mysql_ptr->mysql_res_ptr == NULL) {
	mysql_ptr->error_message = "out of memory";
	mysql_ptr->error_state   = "53200";
	outcome = -1;
    }
    else {
	// All necessary zeroing is already done by calloc():
	//	mysql_ptr->mysql_res.postgresql_db_result = NULL;
	//	mysql_ptr->mysql_res.head_mysql_row_ptr   = NULL;
	//	mysql_ptr->mysql_res.next_mysql_row_ptr   = NULL;
	//	mysql_ptr->mysql_res.result_rows          = 0;
	//	mysql_ptr->mysql_res.result_cols          = 0;
	mysql_ptr->owns_mysql_res = 1;

	mysql_ptr->mysql_res_ptr->postgresql_db_result = PQexec(mysql_ptr->postgresql_db_handle, query);
	if (mysql_ptr->mysql_res_ptr->postgresql_db_result == NULL) {
	    mysql_ptr->error_message = PQerrorMessage(mysql_ptr->postgresql_db_handle);
	    mysql_ptr->error_state   = "53000";  // "53000" => insufficient resources
	    outcome = -1;
	}
	else {
	    // We only delete the mysql_ptr->mysql_res.postgresql_db_result if we
	    // got PGRES_COMMAND_OK and thus know we won't be needing the results.
	    // If we got any error return, we might want to pull an error message
	    // from the result, so we might still need the result set around.
	    switch ( PQresultStatus(mysql_ptr->mysql_res_ptr->postgresql_db_result) ) {
		case PGRES_EMPTY_QUERY:
		    // The string sent to the server was empty.
		    // Logically this isn't really an error at the database level,
		    // but we will treat it as such at the application level because
		    // we shouldn't be sending empty queries to the server.
		    outcome = -1;
		    break;
		case PGRES_COMMAND_OK:
		    // Successful completion of a command returning no data.
		    // We remove the result object because the calling code won't be needing it, and
		    // set that pointer to NULL so mysql_store_result() will return NULL in this case.
		    PQclear(mysql_ptr->mysql_res_ptr->postgresql_db_result);
		    mysql_ptr->mysql_res_ptr->postgresql_db_result = NULL;
		    mysql_ptr->error_message = "";       // mysql_error() returns an empty string if no error occurred
		    mysql_ptr->error_state   = "02000";  // "02000" => no data
		    outcome = 0;
		    break;
		case PGRES_TUPLES_OK:
		    // Successful completion of a command returning data (such as SELECT or SHOW).
		    outcome = 0;
		    break;
		case PGRES_COPY_OUT:
		    // Copy Out (from server) data transfer started.
		    // Since we don't expect to see this in Spine,
		    // we will treat it as an error.
		    mysql_ptr->error_message = "feature not supported";
		    mysql_ptr->error_state   = "0A000";
		    outcome = -1;
		    break;
		case PGRES_COPY_IN:
		    // Copy In (to server) data transfer started.
		    // Since we don't expect to see this in Spine,
		    // we will treat it as an error.
		    mysql_ptr->error_message = "feature not supported";
		    mysql_ptr->error_state   = "0A000";
		    outcome = -1;
		    break;
		case PGRES_BAD_RESPONSE:
		    // The server's response was not understood.
		    outcome = -1;
		    break;
		case PGRES_NONFATAL_ERROR:
		    // A nonfatal error (a notice or warning) occurred.
		    // This might not be serious, but we will treat it as though
		    // it were in order to escalate its importance, hopefully
		    // enough that someone will see the problem, track it down,
		    // and eliminate it.
		    outcome = -1;
		    break;
		case PGRES_FATAL_ERROR:
		    // A fatal error occurred.
		    outcome = -1;
		    break;
		case PGRES_COPY_BOTH:
		    // Copy In/Out (to and from server) data transfer started.
		    // This is currently used only for streaming replication.
		    // Since we don't expect to see this in Spine,
		    // we will treat it as an error.
		    mysql_ptr->error_message = "feature not supported";
		    mysql_ptr->error_state   = "0A000";
		    outcome = -1;
		    break;
		default:
		    // Something unexpected (and undocumented) occurred.
		    // We cannot pretend everything worked as planned.
		    outcome = -1;
		    break;
	    }
	}
    }
    return (outcome);
}

// ---------------------------------------------------------------- //

MYSQL * WINSTANDARDCALL mysql_real_connect(MYSQL *mysql_ptr, const char *dbhost,
    const char *dbuser, const char *dbpass, const char *dbname, unsigned int dbport,
    const char *unix_socket_or_named_pipe, unsigned long client_flags) {

    mysql_ptr->error_message = NULL;
    mysql_ptr->error_state   = NULL;

    mysql_ptr->postgresql_db_handle = postgresql_dbconnect (mysql_ptr,
	dbhost, dbport, dbuser, dbpass, dbname, mysql_ptr->connect_timeout);

    return (mysql_ptr->postgresql_db_handle ? mysql_ptr : NULL);
}

// ---------------------------------------------------------------- //

// Return a null pointer if the query didn't return a result set.
// (Effectively) allocate a MYSQL_RES structure, place the result
//     into that structure, and return a pointer to that structure.
// Return a null pointer if reading the result set failed.
MYSQL_RES * WINSTANDARDCALL mysql_store_result(MYSQL *mysql_ptr) {
    MYSQL_RES * mysql_res_ptr =
	(mysql_ptr->mysql_res_ptr && mysql_ptr->mysql_res_ptr->postgresql_db_result) ?
	mysql_ptr->mysql_res_ptr : NULL;
    if (mysql_res_ptr) {
	mysql_res_ptr->result_rows = PQntuples(mysql_ptr->mysql_res_ptr->postgresql_db_result);
	mysql_res_ptr->result_cols = PQnfields(mysql_ptr->mysql_res_ptr->postgresql_db_result);

	// +1 provides room for a terminating NULL pointer, past the end of the last MYSQL_ROW in the array.
	// That allows us to recognize the end of the array without counting, as we walk it in mysql_fetch_row().
	mysql_res_ptr->head_mysql_row_ptr = calloc(mysql_res_ptr->result_rows + 1, sizeof (MYSQL_ROW));
	mysql_res_ptr->next_mysql_row_ptr = mysql_res_ptr->head_mysql_row_ptr;
	if (mysql_res_ptr->head_mysql_row_ptr == NULL) {
	    // I suppose we could zero out the mysql_res_ptr->result_rows and mysql_res_ptr->result_cols
	    // values here, but that won't matter because without access to mysql_res_ptr itself, the
	    // calling program won't be able to retrieve those counts.
	    mysql_ptr->error_message = "out of memory";
	    mysql_ptr->error_state   = "53200";
	    return (NULL);
	}
	// We count down instead of up when filling in the array of retrieved values,
	// because comparison against zero in a loop generates more efficient machine
	// code than comparison against an arbitrary value.  Once you get used to the
	// idea and the pattern, it becomes idiomatic in your programs.
	MYSQL_ROW *mysql_row_ptr = &mysql_res_ptr->head_mysql_row_ptr[mysql_res_ptr->result_rows];
	int mysql_row_number;
	for (mysql_row_number = mysql_res_ptr->result_rows; --mysql_row_number >= 0; ) {
	    MYSQL_ROW mysql_row = calloc(mysql_res_ptr->result_cols, sizeof (char *));
	    if (mysql_row == NULL) {
		// If we abandon a partially-built data structure here, the application
		// won't be able to get to it, but at some point an internal call will be
		// made to mysql_free_result() from within this emulation layer, and it
		// will still be able crawl what we built so far and clean it all up.
		mysql_ptr->error_message = "out of memory";
		mysql_ptr->error_state   = "53200";
		return (NULL);
	    }
	    *--mysql_row_ptr = mysql_row;
	    MYSQL_ROW mysql_cell_ptr = &mysql_row[mysql_res_ptr->result_cols];
	    int mysql_col_number;
	    for (mysql_col_number = mysql_res_ptr->result_cols; --mysql_col_number >= 0; ) {
		--mysql_cell_ptr;
		// NULL fields retain a NULL pointer in the cell within the row, as demanded
		// by the MySQL client API (see the documentation for mysql_fetch_row()).
		if (! PQgetisnull(mysql_res_ptr->postgresql_db_result, mysql_row_number, mysql_col_number)) {
		    *mysql_cell_ptr = PQgetvalue(mysql_res_ptr->postgresql_db_result, mysql_row_number, mysql_col_number);
		}
	    }
	}
	mysql_ptr->owns_mysql_res = 0;
    }
    else {
	// We don't set the error data to anything special here, because it will already have
	// been set appropriately when mysql_ptr->mysql_res.postgresql_db_result was set to NULL.
	// mysql_ptr->error_message = "xxxxx";
	// mysql_ptr->error_state   = "XXXXX";
    }
    return (mysql_res_ptr);
}

// ---------------------------------------------------------------- //

// We have no thread-specific variables, so there is nothing to do here.
void WINSTANDARDCALL mysql_thread_end(void) {
}

// ---------------------------------------------------------------- //

unsigned int WINSTANDARDCALL mysql_thread_safe(void) {
    return (PQisthreadsafe());
}
