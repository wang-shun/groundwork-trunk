/* 
 * File:   bronx_neb.h
 * Original author: dfeinsmith
 *
 *	2007-09-17 DEF;	Created.
 *	2012-05-14 GH;	Reference neberrors.h as well.
 */

#ifndef _BRONX_NEB_H
#define	_BRONX_NEB_H

#ifdef	__cplusplus
extern "C" {
#endif

// Define NSCORE so we get access to the extended fields in Nagios objects
#ifndef NSCORE
#define NSCORE
#endif
    
/* include the needed NAGIOS event broker header files */
#include "nebmodules.h"
#include "nebcallbacks.h"
#include "nebstructs.h"
#include "neberrors.h"
#include "broker.h"
    
// NEB FUNCTIONS
//extern int nebmodule_init(int flags, char *args, nebmodule *handle);
//extern int nebmodule_deinit(int flags, int reason);

#ifdef	__cplusplus
}
#endif

#endif	/* _BRONX_NEB_H */
