package org.groundwork.foundation.bs.events;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * EntityPublisherListener
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface EntityPublisherListener {

    void eventTextMessage(TextMessage textMessage) throws JMSException;
}
