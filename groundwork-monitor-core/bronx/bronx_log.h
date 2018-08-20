/*
 * File:   bronx_log.h
 *
 *  Change Log:
 *      2007-10-10 DEF;		Initial creation.
 *      2012-12-05 GH;		Added BRONX_LOGGING_DEVELOP to support extreme insight.
 */

#ifndef _BRONX_LOG_H
#define	_BRONX_LOG_H

#ifdef __GNUC__
#define gnuc_extension(arg)     arg
#else
#define gnuc_extension(arg)
#endif

#ifdef	__cplusplus
extern "C" {
#endif

#define	BRONX_LOGGING_NONE 		0	// Reserved value; not currently supported in bronx.cfg.
#define	BRONX_LOGGING_ERROR 		1
#define	BRONX_LOGGING_WARNING		2
#define	BRONX_LOGGING_COMMANDS		3
#define	BRONX_LOGGING_PASSIVE_CHECKS	4 
#define	BRONX_LOGGING_INFO		5
#define	BRONX_LOGGING_DEBUG		6
#define	BRONX_LOGGING_DEVELOP		7

#define DEFAULT_AUDIT_LOG_FILE          "/usr/local/groundwork/nagios/var/eventbroker_audit.log"

extern apr_status_t log_init();
extern void log_uninit();
extern void bronx_log(char *msg, int level);
extern void bronx_logprintf(int level, char *format_string, ...)
    gnuc_extension (__attribute__ ((format (printf, 2, 3))));
extern int  nagios_level(int);
extern void log_setlevel(int);
extern int  log_getlevel();
extern void bronx_write_to_log(char *buffer);
extern void audit_log(char *msg, char *status, char *user, time_t timestamp);

#ifdef	__cplusplus
}
#endif

#endif	/* _BRONX_LOG_H */
