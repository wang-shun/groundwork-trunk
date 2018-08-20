/* 
 * File:   bronx_utils.h
 * Author: dfeinsmith
 *
 * Created on September 17, 2007, 1:00 PM
 */

#ifndef _BRONX_UTILS_H
#define	_BRONX_UTILS_H

#ifdef	__cplusplus
extern "C" {
#endif

extern void set_bronx_terminating (int termination_state);
extern int bronx_is_terminating ();

// set_bronx_manually_paused() sets both the "paused" and "manually paused"
// flags in one atomic operation, so there is no race condition in determining
// the current state; hence if you call set_bronx_manually_paused() there is
// no need for a separate call to set_bronx_paused()
extern void set_bronx_paused          (int paused_state);
extern void set_bronx_manually_paused (int paused_state);
extern int bronx_is_paused();

extern void set_bronx_start_time ();
extern time_t bronx_start_time ();

extern void normalize_plugin_output(char *plugin_output, char *source);
    
// UTILITY
extern char *pstrreplace(apr_pool_t* mp, const char* str, const char* needle1, const char* needle2);
extern char *strcleanup(apr_pool_t *tempmp, char *str);
extern char *datetostring(apr_pool_t*, time_t time);	// Helper function to convert time_t to string in iso format

// NOTE:  You must never ever call strerror() in a multi-threaded program, as it is
// not thread-safe.  Normally, you would call strerror_r() instead.  But its calling
// prototype unfortunately varies between platforms and compilation environments, so
// you should not call it directly.  Here we provide a replacement for the semi-standard
// strerror_r() function.  Our replacement has a fixed function signature and internally
// auto-adapts to local platform idiosyncracies.  You must use the returned pointer
// rather than the passed-in buffer to subsequently process the error message, as your
// local buffer may or may not have been filled in by the call.  In that sense, the
// calling sequence is more similar to strerror() than to the POSIX strerror_r().  But
// our function will always return a valid string, and never a NULL pointer to indicate
// that the error code was unanalyzable.
extern char *bronx_strerror_r (int errnum, char *strerrbuf, size_t buflen);

// Buffer size, to be used to define the strerrbuf[] array for bronx_strerror_r().
#define ERRNO_STRING_BUF_SIZE   100

#ifdef	__cplusplus
}
#endif

#define stringify(x)		#x
#define expand_and_stringify(x)	stringify(x)

#endif	/* _BRONX_UTILS_H */
