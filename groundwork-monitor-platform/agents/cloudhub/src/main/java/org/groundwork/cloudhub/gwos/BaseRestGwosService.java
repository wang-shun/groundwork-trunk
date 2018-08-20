package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseRestGwosService {

    private static final DateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String buildWsConnectionString(GWOSConfiguration config, String portName) {
        String portNumber = calculatePortNumber(config.getWsPortNumber());
        String protocol = (config.isGwosSSLEnabled()) ? "https://" : "http://";
        String endPoint =
                protocol
                        + config.getGwosServer()
                        + portNumber
                        + config.getWsEndPoint()
                        + "/"
                        + portName;
        return endPoint;
    }

    public static String buildRsConnectionString(GWOSConfiguration config) {
        String portNumber = calculatePortNumber(config.getWsPortNumber());
        String protocol = (config.isGwosSSLEnabled()) ? "https://" : "http://";
        String endPoint =
                protocol
                        + config.getGwosServer()
                        + portNumber
                        + config.getRsEndPoint();
        return endPoint;
    }

    public static String calculatePortNumber(String number) {
        String portNumber = (StringUtils.isEmpty(number)) ? "" : number;
        portNumber = (portNumber.equals("80")) ? "" : portNumber;
        portNumber = (portNumber.equals("443")) ? "" : portNumber;
        portNumber = (portNumber.equals("")) ? "" : ":" + portNumber;
        return portNumber;
    }

    public static Date parseDate(String date) {
        if (date != null) {
            try {
                return DEFAULT_DATETIME_FORMAT.parse(date);
            } catch (Exception e) {
            }
        }
        return new Date();
    }

    public static String nowTime() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(ConnectorConstants.gwosDateFormat);
        return sdf.format(now).toString();
    }



}
