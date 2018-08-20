package org.groundwork.foundation.bs.logmessage;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

/**
 * TextMessageImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TextMessageImpl implements TextMessage {

    private String text;
    private Map<String, Object> properties;
    private long timestamp;
    private String messageID;

    public TextMessageImpl(String text) {
        this.text = text;
        this.timestamp = System.currentTimeMillis();
        this.messageID = UUID.randomUUID().toString();
    }

    public TextMessageImpl() {
        this(null);
    }

    @Override
    public void clearBody() {
        text = null;
    }

    @Override
    public void clearProperties() {
        properties = null;
    }

    @Override
    public String toString() {
        return "["+messageID+","+timestamp+","+text+"]";
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Enumeration getPropertyNames() {
        return new Vector(properties != null ? properties.keySet() : Collections.EMPTY_LIST).elements();
    }

    @Override
    public boolean propertyExists(String name) {
        return properties != null ? properties.containsKey(name) : false;
    }

    @Override
    public boolean getBooleanProperty(String name) {
        Boolean value = (Boolean) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public byte getByteProperty(String name) {
        Byte value = (Byte) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public double getDoubleProperty(String name) {
        Double value = (Double) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public float getFloatProperty(String name) {
        Float value = (Float) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public int getIntProperty(String name) {
        Integer value = (Integer) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public long getLongProperty(String name) {
        Long value = (Long) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public Object getObjectProperty(String name) {
        Object value = properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public short getShortProperty(String name) {
        Short value = (Short) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public String getStringProperty(String name) {
        String value = (String) properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Property %s does not exist", name));
        }
        return value;
    }

    @Override
    public void setBooleanProperty(String name, boolean value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setByteProperty(String name, byte value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setDoubleProperty(String name, double value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setFloatProperty(String name, float value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setIntProperty(String name, int value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setLongProperty(String name, long value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setObjectProperty(String name, Object value) {
        if ((value instanceof Boolean) || (value instanceof Byte) || (value instanceof Double) || (value instanceof Float) ||
                (value instanceof Long) || (value instanceof Short) || (value instanceof String)) {
            if (properties == null) {
                properties = new HashMap<>();
            }
            properties.put(name, value);
        } else {
            throw new IllegalArgumentException("Only primitive property types supported");
        }
    }

    @Override
    public void setShortProperty(String name, short value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void setStringProperty(String name, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public void acknowledge() throws JMSException {
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return messageID;
    }

    @Override
    public void setJMSMessageID(String s) throws JMSException {
        messageID = s;
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return timestamp;
    }

    @Override
    public void setJMSTimestamp(long l) throws JMSException {
        timestamp = l;
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return new byte[0];
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
    }

    @Override
    public void setJMSCorrelationID(String s) throws JMSException {
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return null;
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return null;
    }

    @Override
    public void setJMSReplyTo(Destination destination) throws JMSException {
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return null;
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryMode(int i) throws JMSException {
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return false;
    }

    @Override
    public void setJMSRedelivered(boolean b) throws JMSException {
    }

    @Override
    public String getJMSType() throws JMSException {
        return null;
    }

    @Override
    public void setJMSType(String s) throws JMSException {
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSExpiration(long l) throws JMSException {
    }

    @Override
    public int getJMSPriority() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSPriority(int i) throws JMSException {
    }
}
