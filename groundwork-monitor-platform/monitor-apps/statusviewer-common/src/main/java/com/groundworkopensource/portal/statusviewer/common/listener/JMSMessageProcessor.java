package com.groundworkopensource.portal.statusviewer.common.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.groundworkopensource.portal.statusviewer.bean.OnDemandServerPush;

/**
 * JMSMessageProcessor.
 */
public class JMSMessageProcessor {

    /** The pool. */
    private static ExecutorService pool;

    /**
     * Processes the command.
     * 
     * @param command
     *            the command
     */
    public static void processMessage(OnDemandServerPush command) {
        if (pool == null) {
            pool = Executors.newCachedThreadPool();
        }
        pool.execute(command);
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected JMSMessageProcessor() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

}
