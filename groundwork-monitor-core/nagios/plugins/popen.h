/******************************************************************************
 *
 * $Id: popen.h,v 1.1.1.1 2005/02/07 19:33:32 hmann Exp $
 *
 ******************************************************************************/

FILE *spopen (const char *);
int spclose (FILE *);
RETSIGTYPE popen_timeout_alarm_handler (int);

extern unsigned int timeout_interval;
pid_t *childpid;
int *child_stderr_array;
FILE *child_process;
FILE *child_stderr;
