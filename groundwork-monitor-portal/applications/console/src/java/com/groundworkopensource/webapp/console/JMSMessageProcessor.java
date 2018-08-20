package com.groundworkopensource.webapp.console;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class JMSMessageProcessor.
 */
public class JMSMessageProcessor {

    /** Enable Logging *. */
    protected static Log log = LogFactory.getLog(JMSMessageProcessor.class);

    /** The pool. */
    private static ExecutorService pool;

    /**
     * Processes the command.
     * 
     * @param command
     *            the command
     */
    public static void processMessage(ServerPush command) {
        if (pool == null)
            pool = Executors.newCachedThreadPool();
        pool.execute(command);
    }
}
