// Emulation of (a portion of) the MySQL client API under PostgreSQL.
//
// This emulation only includes as much as is needed by the Cacti Spine
// code, and even then some adjustments will need to be in place within
// the Spine code to only call this emulation with SQL which is valid
// within PostgreSQL.

// Copyright (c) 2011 by GroundWork Open Source, Inc.  All rights reserved.
// Use is subject to GroundWork commercial license terms.

#include <libpq-fe.h>

typedef PGconn   *POSTGRESQL_DB_HANDLE;
typedef PGresult *POSTGRESQL_DB_RESULT;

// FIX LATER:  Some additional adjustments may be needed here for a Cygwin platform,
// which implements a UNIX API and therefore should not be compiled for as though
// it were a native Windows platform.
#if (defined(_WIN32) || defined(_WIN64)) && !defined(__WINDOWS__)
#define __WINDOWS__
#endif

#if !defined(__WINDOWS__)
    #define WINSTANDARDCALL
#else
    #define WINSTANDARDCALL __stdcall
#endif

#if defined(NO_CLIENT_LONG_LONG)
    typedef unsigned long my_ulonglong;
#elif defined (__WINDOWS__)
    typedef unsigned __int64 my_ulonglong;
#else
    typedef unsigned long long my_ulonglong;
#endif

typedef char **MYSQL_ROW;	// query results are returned as an array of strings

typedef struct mysql_result_structure {
    POSTGRESQL_DB_RESULT postgresql_db_result;
    MYSQL_ROW * head_mysql_row_ptr;
    MYSQL_ROW * next_mysql_row_ptr;
    int result_rows;
    int result_cols;
} MYSQL_RES;

typedef struct mysql_structure { 
    POSTGRESQL_DB_HANDLE postgresql_db_handle;
    char * connect_timeout;
    MYSQL_RES * mysql_res_ptr;
    char * error_message;
    char * error_state;
    int owns_mysql_res;
    int is_dynamically_allocated;
} MYSQL;

enum mysql_option {
    MYSQL_OPT_CONNECT_TIMEOUT = 0,
    MYSQL_OPT_RECONNECT       = 20
};

extern void         WINSTANDARDCALL mysql_close(MYSQL *mysql);
extern unsigned int WINSTANDARDCALL mysql_errno(MYSQL *mysql);
extern const char * WINSTANDARDCALL mysql_error(MYSQL *mysql);
extern MYSQL_ROW    WINSTANDARDCALL mysql_fetch_row(MYSQL_RES *mysql_res);
extern void         WINSTANDARDCALL mysql_free_result(MYSQL_RES *mysql_res);
extern MYSQL *      WINSTANDARDCALL mysql_init(MYSQL *mysql);
extern my_ulonglong WINSTANDARDCALL mysql_num_rows(MYSQL_RES *mysql_res);
extern int          WINSTANDARDCALL mysql_options(MYSQL *mysql, enum mysql_option option, const char *option_argument);
extern int          WINSTANDARDCALL mysql_query(MYSQL *mysql, const char *query);
extern MYSQL *      WINSTANDARDCALL mysql_real_connect(MYSQL *mysql, const char *dbhost,
			const char *dbuser, const char *dbpass, const char *dbname, unsigned int dbport,
			const char *unix_socket_or_named_pipe, unsigned long client_flags);
extern MYSQL_RES *  WINSTANDARDCALL mysql_store_result(MYSQL *mysql);
extern void         WINSTANDARDCALL mysql_thread_end(void);
extern unsigned int WINSTANDARDCALL mysql_thread_safe(void);
