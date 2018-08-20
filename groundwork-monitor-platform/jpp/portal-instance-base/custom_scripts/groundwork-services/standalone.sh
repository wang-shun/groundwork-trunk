#!/bin/sh

DIRNAME=`dirname "$0"`
PROGNAME=`basename "$0"`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;

    Linux)
        linux=true
        ;;
esac


# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JBOSS_HOME
RESOLVED_JBOSS_HOME=`cd "$DIRNAME/.."; pwd`
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=$RESOLVED_JBOSS_HOME
else
 SANITIZED_JBOSS_HOME=`cd "$JBOSS_HOME"; pwd`
 if [ "$RESOLVED_JBOSS_HOME" != "$SANITIZED_JBOSS_HOME" ]; then
   echo ""
   echo "   WARNING:  JBOSS_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             JBOSS_HOME: $JBOSS_HOME"
   echo ""
   sleep 2s
 fi
fi
export JBOSS_HOME

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/standalone.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

if [ "$PRESERVE_JAVA_OPTS" != "true" ]; then
    # Check for -d32/-d64 in JAVA_OPTS
    JVM_D64_OPTION=`echo $JAVA_OPTS | $GREP "\-d64"`
    JVM_D32_OPTION=`echo $JAVA_OPTS | $GREP "\-d32"`

    # Check If server or client is specified
    SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
    CLIENT_SET=`echo $JAVA_OPTS | $GREP "\-client"`

    if [ "x$JVM_D32_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d32"
    elif [ "x$JVM_D64_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d64"
    elif $darwin && [ "x$SERVER_SET" = "x" ]; then
        # Use 32-bit on Mac, unless server has been specified or the user opts are incompatible
        "$JAVA" -d32 $JAVA_OPTS -version > /dev/null 2>&1 && PREPEND_JAVA_OPTS="-d32" && JVM_OPTVERSION="-d32"
    fi

    CLIENT_VM=false
    if [ "x$CLIENT_SET" != "x" ]; then
        CLIENT_VM=true
    elif [ "x$SERVER_SET" = "x" ]; then
        if $darwin && [ "$JVM_OPTVERSION" = "-d32" ]; then
            # Prefer client for Macs, since they are primarily used for development
            CLIENT_VM=true
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -client"
        else
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -server"
        fi
    fi

    if [ $CLIENT_VM = false ]; then
        NO_COMPRESSED_OOPS=`echo $JAVA_OPTS | $GREP "\-XX:\-UseCompressedOops"`
        if [ "x$NO_COMPRESSED_OOPS" = "x" ]; then
            "$JAVA" $JVM_OPTVERSION -server -XX:+UseCompressedOops -version >/dev/null 2>&1 && PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -XX:+UseCompressedOops"
        fi
    fi

    JAVA_OPTS="$PREPEND_JAVA_OPTS $JAVA_OPTS"
fi

if [ "x$JBOSS_MODULEPATH" = "x" ]; then
    JBOSS_MODULEPATH="$JBOSS_HOME/modules"
fi

if $linux; then
    # consolidate the server and command line opts
    SERVER_OPTS="$JAVA_OPTS $@"
    # process the standalone options
    for var in $SERVER_OPTS
    do
       case $var in
         -Djboss.server.base.dir=*)
              JBOSS_BASE_DIR=`readlink -m ${var#*=}`
              ;;
         -Djboss.server.log.dir=*)
              JBOSS_LOG_DIR=`readlink -m ${var#*=}`
              ;;
         -Djboss.server.config.dir=*)
              JBOSS_CONFIG_DIR=`readlink -m ${var#*=}`
              ;;
       esac
    done
fi
# determine the default base dir, if not set
if [ "x$JBOSS_BASE_DIR" = "x" ]; then
   JBOSS_BASE_DIR="$JBOSS_HOME/standalone"
fi
# determine the default log dir, if not set
if [ "x$JBOSS_LOG_DIR" = "x" ]; then
   JBOSS_LOG_DIR="$JBOSS_BASE_DIR/log"
fi
# determine the default configuration dir, if not set
if [ "x$JBOSS_CONFIG_DIR" = "x" ]; then
   JBOSS_CONFIG_DIR="$JBOSS_BASE_DIR/configuration"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_MODULEPATH=`cygpath --path --windows "$JBOSS_MODULEPATH"`
    JBOSS_BASE_DIR=`cygpath --path --windows "$JBOSS_BASE_DIR"`
    JBOSS_LOG_DIR=`cygpath --path --windows "$JBOSS_LOG_DIR"`
    JBOSS_CONFIG_DIR=`cygpath --path --windows "$JBOSS_CONFIG_DIR"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  JBoss Bootstrap Environment"
echo ""
echo "  JBOSS_HOME: $JBOSS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

while true; do
   if [ "x$LAUNCH_JBOSS_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" -D\"[Standalone]\" $JAVA_OPTS \
         \"-Dorg.jboss.boot.log.file=$JBOSS_LOG_DIR/boot.log\" \
         \"-Dlogging.configuration=file:$JBOSS_CONFIG_DIR/logging.properties\" \
         -jar \"$JBOSS_HOME/jboss-modules.jar\" \
         -mp \"${JBOSS_MODULEPATH}\" \
         -jaxpmodule "javax.xml.jaxp-provider" \
         org.jboss.as.standalone \
         -Djboss.home.dir=\"$JBOSS_HOME\" \
         -Djboss.server.base.dir=\"$JBOSS_BASE_DIR\" \
         "$@"
      JBOSS_STATUS=$?
   else
      # Before we start the Java process, we must clean up any portal application
      # deployment marker files left over from previous runs, so the portal does not
      # immediately try to start those applications.  That's because sequencing their
      # startup order is important for proper functioning.
      /usr/local/groundwork/foundation/feeder/check-listener.pl -r localhost 4913

      # We need to handle termination signals carefully, avoiding any race conditions
      # that might arise right around the time that we are starting the JVM in the
      # background.

      # Trap common signals and save a record that they occurred, so we can later relay
      # them to the jboss process (once we have its PID in hand to do the signaling).
      SIGNAL_HUP=''
      SIGNAL_TERM=''
      SIGNAL_QUIT=''
      SIGNAL_PIPE=''
      trap "SIGNAL_HUP=HUP"   HUP
      trap "SIGNAL_TERM=INT"  INT
      trap "SIGNAL_QUIT=QUIT" QUIT
      trap "SIGNAL_PIPE=PIPE" PIPE
      trap "SIGNAL_TERM=TERM" TERM

      # Execute the JVM in the background, then capture its process ID.
      eval \"$JAVA\" -D\"[Standalone]\" $JAVA_OPTS \
         \"-Dorg.jboss.boot.log.file=$JBOSS_LOG_DIR/boot.log\" \
         \"-Dlogging.configuration=file:$JBOSS_CONFIG_DIR/logging.properties\" \
         -jar \"$JBOSS_HOME/jboss-modules.jar\" \
         -mp \"${JBOSS_MODULEPATH}\" \
         -jaxpmodule "javax.xml.jaxp-provider" \
         org.jboss.as.standalone \
         -Djboss.home.dir=\"$JBOSS_HOME\" \
         -Djboss.server.base.dir=\"$JBOSS_BASE_DIR\" \
         "$@" "&"
      JBOSS_PID=$!

      # Trap common signals and relay them to the jboss process
      trap "kill -HUP  $JBOSS_PID" HUP
      trap "kill -TERM $JBOSS_PID" INT
      trap "kill -QUIT $JBOSS_PID" QUIT
      trap "kill -PIPE $JBOSS_PID" PIPE
      trap "kill -TERM $JBOSS_PID" TERM

      # If we received any type of termination signal before our trap-and-kill handlers were
      # in place, it is now time to relay that signal to the jboss process.  We couldn't do
      # so before because of the race conditions between spawning the background process,
      # capturing its process ID, and setting up those trap-and-kill handlers, vs. when the
      # incoming termination signal might have occurred.
      if [ -n "$SIGNAL_HUP" ]; then
	  kill -HUP $JBOSS_PID
      fi
      if [ -n "$SIGNAL_TERM" ]; then
	  kill -TERM $JBOSS_PID
      fi
      if [ -n "$SIGNAL_QUIT" ]; then
	  kill -QUIT $JBOSS_PID
      fi
      if [ -n "$SIGNAL_PIPE" ]; then
	  kill -PIPE $JBOSS_PID
      fi

      if [ "x$JBOSS_PIDFILE" != "x" ]; then
          echo $JBOSS_PID > $JBOSS_PIDFILE
      fi

      # Wait until all the applications are up and running.
      /usr/local/groundwork/foundation/feeder/check-listener.pl localhost 4913
      if [ $? -eq 0 ]; then
	  echo "All services have started."
      else
	  echo "Service startup has failed."

	  # At this point, what to do?  This script will "wait" (in the loop below) forever until
	  # the java process dies.  Let's compare the situation to what would have happened if the
	  # java process were run in foreground mode in the other branch above (which would have
	  # been invoked if the java process were responsible for all its own deployment marker
	  # file management).  In that case, the java process would presumably never get stuck (and
	  # not die) because of bad deployment-file management, since the deployment files are
	  # supposedly maintained in a manner that can recover from process failure.  But in this
	  # case, where we run in background mode, the java process as a whole might well survive
	  # while only a few portal applications are up, and the check-listener.pl script has timed
	  # out on one of them and then fails to start the rest of them.  So we have introduced a
	  # possible new way of getting the system stuck.  It can be recovered easily enough if
	  # somebody is around to do the work, by restarting gwservices.  But we may as well make
	  # the system self-healing in this case, so we try to stop the java process and allow the
	  # calling context to restart it from scratch.

	  # Currently, we don't bother to make sure that JAVA_HOME is set correctly to run the
	  # jboss-cli.sh script, because if it wasn't, we probably would have executed the wrong
	  # copy of $JAVA above.
	  ## source /usr/local/groundwork/scripts/setenv.sh

	  # Shut down the JBoss Portal Platform instance of JBoss AS.
	  # If we find that this gentle call is not sufficient, then we might also want to follow up
	  # with logic for the same types of waiting and process signalling (TERM and KILL) found in
	  # the gwservices script.  Such an extension awaits a future version of this script.
	  cli_lines=`/usr/local/groundwork/foundation/container/jpp/bin/jboss-cli.sh --commands='connect,/host=:shutdown'`
	  # We should get back the following if the program connected and ran:
	  #     {"outcome" => "success"}
	  # but we don't bother to check, since all we really care about is whether the java process
	  # eventually dies, whether because of this or (in the future) an explicit TERM or KILL signal.
      fi

      # Wait until the background process exits
      # FIX LATER:  This process-waiting strategy is inherited from the original standalone.sh
      # script (presumably from the JBoss folks), and it's obviously bogus.  If you wait for
      # a specific process ID and you return from that wait, the process is already DEAD AND
      # GONE.  There is absolutely no sense in looping to wait for the exact same process ID a
      # second time, and no sense whatsoever in waiting again later on if the wait status was
      # not some special value.
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $JBOSS_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** JBossAS process ($JBOSS_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         JBOSS_STATUS=$WAIT_STATUS
      else
         JBOSS_STATUS=0
      fi
      if [ "$JBOSS_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $JBOSS_PID 2>/dev/null
      fi
      if [ "x$JBOSS_PIDFILE" != "x" ]; then
            grep "$JBOSS_PID" $JBOSS_PIDFILE && rm $JBOSS_PIDFILE
      fi
   fi
   if [ "$JBOSS_STATUS" -eq 10 ]; then
      echo "Restarting JBoss..."
   else
      exit $JBOSS_STATUS
   fi
done
