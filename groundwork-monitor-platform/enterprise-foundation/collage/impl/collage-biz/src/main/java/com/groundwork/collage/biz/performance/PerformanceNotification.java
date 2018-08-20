package com.groundwork.collage.biz.performance;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import java.util.Map;
import java.util.Properties;

public class PerformanceNotification {

    protected static Log log = LogFactory.getLog(PerformanceNotification.class);

    public static final int MAX_LABEL_LENGTH = 19;

    private static final String DEFAULT_JNDI_FACTORY_CLASS = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String DEFAULT_JNDI_HOST = "localhost";
    private static final String DEFAULT_JNDI_PORT = "4447";
    private static final String DEFAULT_SERVER_CONTEXT = "jms/RemoteConnectionFactory";
    private static final String DEFAULT_QUEUE = "/queue/vema_perf_data";
    private static final String MESSAGE_DELIMITER = "\t";
    private static final String THRESHOLD_DELIMITER = ";";
    private static final String TAG_DELIMITER = ";";
    private JMSDestinationInfo jmsDestinationInfo = null;
    private JMSDestinationWriter writer = null;
    private Object lock = new Object();

    public void writeMessage( String appType,
                              String serverName,
                              String serviceName,
                              Long serverTime,
                              String value,
                              Long warning,
                              Long critical) throws FoundationJMSException {
        writeMessage(appType, serverName, serviceName, serverTime, value, safeLong(warning), safeLong(critical));
    }

    public void writeMessage( String appType,
                              String serverName,
                              String serviceName,
                              Long serverTime,
                              String value,
                              String warning,
                              String critical) throws FoundationJMSException {
        writeMessage(appType, serverName, serviceName, serverTime, null, value, warning, critical);
    }

    public void writeMessage( String appType,
                              String serverName,
                              String serviceName,
                              Long serverTime,
                              String label,
                              String value,
                              String warning,
                              String critical) throws FoundationJMSException {
        writeMessage(appType, serverName, serviceName, serverTime, null, value, warning, critical, null);
    }

    public void writeMessage( String appType,
                              String serverName,
                              String serviceName,
                              Long serverTime,
                              String label,
                              String value,
                              String warning,
                              String critical,
                              Map<String,String> tags) throws FoundationJMSException {
        acquireDestinationWriter();
        if ((label == null) && (serviceName != null) && (serviceName.length() > MAX_LABEL_LENGTH)) {
            label = serviceName.substring(serviceName.length() - MAX_LABEL_LENGTH, serviceName.length());
        } else {
            label = serviceName;
        }
        // message format:
        // serverTime TAB serverName TAB serviceName TAB TAB label = value ; warning ; critical [ TAB [ tagName = tagValue ; ]* ]?
        //
        // format message
        StringBuilder message = new StringBuilder();
        message.append(safeLong(serverTime));
        message.append(MESSAGE_DELIMITER);
        message.append(safe(serverName));
        message.append(MESSAGE_DELIMITER);
        message.append(safe(serviceName));
        message.append(MESSAGE_DELIMITER);
        message.append(MESSAGE_DELIMITER);
        message.append(safe(label));
        message.append("=");
        message.append(safe(value));
        message.append(THRESHOLD_DELIMITER);
        message.append(safe(warning));
        message.append(THRESHOLD_DELIMITER);
        message.append(safe(critical));
        // append extended tags to message, (non-standard message format)
        if ((tags != null) && !tags.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String,String> tag : tags.entrySet()) {
                if (first) {
                    message.append(MESSAGE_DELIMITER);
                    first = false;
                }
                message.append(safe(tag.getKey()));
                message.append("=");
                message.append(safe(tag.getValue()));
                message.append(TAG_DELIMITER);
            }
        }
        // write message
        writer.writeMessageWithProperty(message.toString(), "appType", appType);
    }

    public boolean canWriteToPerformance() throws FoundationJMSException {
        acquireDestinationWriter();
        return writer != null;
    }

    public void commit() throws FoundationJMSException {
        if (writer != null)
            writer.commit();
    }

    protected JMSDestinationWriter acquireDestinationWriter() throws FoundationJMSException {
        if (jmsDestinationInfo == null) {
            synchronized (lock) {
                if (jmsDestinationInfo == null) {
                    jmsDestinationInfo = acquireJMSConnection();
                }
            }
        }
        if (writer == null) {
            synchronized (lock) {
                try {
                    writer = new JMSDestinationWriterImpl();
                    writer.initialize(jmsDestinationInfo);
                }
                catch (Exception e) {
                    log.error("Failed to create JMS writer for PerfData", e);
                    if (writer != null) {
                        writer.unInitialize();
                        writer = null;
                    }
                    throw new FoundationJMSException("Failed initialize queue: " + e.getMessage(), e);
                }
            }
        }
        return writer;
    }

    protected JMSDestinationInfo acquireJMSConnection() {
        CollageFactory service = CollageFactory.getInstance();
        Properties configuration = service.getFoundationProperties();
        jmsDestinationInfo = new JMSDestinationInfoImpl(configuration.getProperty(
                "jndi.factory.initial", DEFAULT_JNDI_FACTORY_CLASS).trim(),
                configuration.getProperty("jndi.factory.host",
                        DEFAULT_JNDI_HOST).trim(), configuration.getProperty(
                "jndi.factory.port", DEFAULT_JNDI_PORT).trim(),
                configuration.getProperty("jms.server.context.id",
                        DEFAULT_SERVER_CONTEXT).trim(), configuration
                .getProperty("perfdata.vema.jms.queue.name", DEFAULT_QUEUE)
                .trim(),configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim(),
                configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim());
        return jmsDestinationInfo;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
    private String safeLong(Long l) {
        return (l == null) ? "" : l.toString();
    }

}
