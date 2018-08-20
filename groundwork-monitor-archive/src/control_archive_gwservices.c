//
// Program to run a specified, hardcoded setuid control script for starting
// or stopping gwservices on an archive server.
//
// Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
// Use is subject to GroundWork commercial license terms.
//

#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/types.h>
#include <pwd.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>

//  This pathname is set to reference a special copy of the gwservices script
//  that will be available on an Archive Server.  It is NOT the same copy that
//  is used for the general GroundWork Monitor processes, nor the secondary
//  copy that will be used when we have a Dual-JVM setup in play (separating
//  the back-end Foundation JVM from the front-end Portal JVM).
#define ARCHIVE_GWSERVICES_CONTROL_SCRIPT	"/usr/local/groundwork/core/services3/gwservices"

static char *trusted_env[] =
    {
    "PATH=/usr/bin:/usr/sbin:/sbin:/bin",
    0
    };

int main(int argc, char *argv[])
    {
    struct passwd *pwd;
    int i;
    uid_t uid;
    char *action;

    // Step over the program name.
    --argc;
    ++argv;

    if (argc != 1)
	{
	// If you don't know how to run this program, you shouldn't be fiddling with it.
	// So we're not providing any clues like a usage message.
	exit(1);
	}

    action = argv[0];
    if (strcmp(action, "start") && strcmp(action, "stop"))
	{
	// Same error handling as above.
	exit(1);
	}

    // I'm not sure why we bother to ignore almost all signals, except that we really
    // don't want the control script to be interrupted while it's doing its thing.
    for (i = 0; i < NSIG; ++i)
	{
	if (i != SIGKILL && i != SIGCHLD)
	    {
	    (void) signal(i, SIG_IGN);
	    }
	}

    // Who am I, really?
    uid = getuid();
    if ( (pwd = getpwuid(uid)) == (struct passwd *) 0 )
	{
	// Same error handling as above, though this has to do with the system setup
	// rather than (presumably) a failure to run this command properly.  Of course,
	// if we can't find our own UID in the password file, then probably somebody
	// bad is trying to run this program, and once again we don't want to emit
	// any clues that could help bypass our security protections.
	exit(1);
	}

    // We should already be running this program as a setuid-root binary, which means the
    // effective UID is 0, but not necessarily the real UID.  This setuid() call will set
    // the real and saved UID values to be 0 as well.
    setuid((uid_t) 0);

    // If this works, we won't be back here, and the exit code of running the script will
    // automatically become the exit code of this original process.
    execle(ARCHIVE_GWSERVICES_CONTROL_SCRIPT, ARCHIVE_GWSERVICES_CONTROL_SCRIPT, action, (char *) 0, trusted_env);

    // Oops, we made it back here, so running the script failed.
    // This is the one situation in which it might be helpful to explain what went wrong.
    printf("ERROR:  Cannot run %s; errno=%d (%s).\n", ARCHIVE_GWSERVICES_CONTROL_SCRIPT, errno, strerror(errno));

    // We should never get here, since the execle should replace this process image with a
    // shell that runs the control script.  But if that fails for any reason, we want to
    // be totally safe and restore a non-superuser running environment.
    setuid(uid);

    // The execle() above failed, so we need to reflect that in the exit code.
    exit(1);
    }
