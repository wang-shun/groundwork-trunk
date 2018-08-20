package org.groundwork.rs.auth;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuthorizationConfiguration {

    private static final String GATEWAY_CONFIG_PATH = "/usr/local/groundwork/config/josso-gateway-config.xml";
    private static final String FOUNDATION_CONFIG_PATH = "/usr/local/groundwork/config/foundation.properties";
    private static final int DEFAULT_SESSIONS_MAX = 500;

    private int maxInactiveIntervalMinutes = 480;
    private int maxSessions = DEFAULT_SESSIONS_MAX;

    public AuthorizationConfiguration() throws IOException {
        readGatewayConfig();
        readFoundationConfig();
    }

    private void readGatewayConfig() throws IOException {
        InputStream fis = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            fis = new FileInputStream(GATEWAY_CONFIG_PATH);
            Document document = builder.parse(fis);
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {

                //We have encountered an <employee> tag.
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.getTagName().equals("def-sessionmgr:session-manager")) {
                        String max = element.getAttribute("maxInactiveInterval");
                        if (max != null) {
                            maxInactiveIntervalMinutes = Integer.parseInt(max);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IOException("Failed to read JOSSO Gateway file", e);
        }
        finally {
            if (fis != null)
                fis.close();
        }
    }

    private void readFoundationConfig() throws IOException {
        InputStream fis = null;
        try {
            Properties properties = new Properties();
            fis = new FileInputStream(FOUNDATION_CONFIG_PATH);
            properties.load(fis);
            String prop = properties.getProperty("collagerest.sessions.max", String.valueOf(DEFAULT_SESSIONS_MAX));
            this.maxSessions = Integer.parseInt(prop);
        } catch (Exception e) {
            throw new IOException("Failed to read Foundation properties", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {}
            }
        }

    }

    public int getMaxInactiveIntervalMinutes() {
        return maxInactiveIntervalMinutes;
    }

    public int getMaxSessions() {
        return maxSessions;
    }
}
