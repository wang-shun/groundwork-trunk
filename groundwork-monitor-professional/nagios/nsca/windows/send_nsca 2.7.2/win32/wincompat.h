/*
 * Some compabillity routines to help VC compile the unix sources as much as possible..
 * These are only tested with the nrpe sources, and may break on anything else...
 */


#define	LOG_EMERG	1
#define	LOG_ALERT	1
#define	LOG_CRIT	1
#define	LOG_ERR		4
#define	LOG_WARNING	5
#define	LOG_NOTICE	6
#define	LOG_INFO	6
#define	LOG_DEBUG	7


void syslog(int, const char *, ...);
void bzero(char * dest, int length);
void sleep (int seconds);
int InstallService(char * ExePath);
int UninstallService();