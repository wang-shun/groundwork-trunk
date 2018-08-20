package org.groundwork.cloudhub.connectors.base;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public abstract class BaseConnectorClient {

    public final static String PATH_SEPARATOR = "/";
    public final static String PORT_SEPARATOR = ":";

    protected String concatenatePaths(String base, String path) {
        StringBuilder result = new StringBuilder();
        if (base.endsWith(PATH_SEPARATOR)) {
            if (path.startsWith(PATH_SEPARATOR))
                result.append(path.substring(1));
            else
                result.append(path);
        }
        else {
            if (path.startsWith(PATH_SEPARATOR) || path.startsWith(PORT_SEPARATOR))
                result.append(path);
            else {
                result.append(PATH_SEPARATOR);
                result.append(path);
            }
        }
        return result.toString();
    }

    /**
     * Join a base URL with a relative path to create a full URL
     *
     * @param base the base URL such as http://server-name[:port]/foundation-webapp/api
     * @param path the path such as /devices?query=etc
     * @return the correctly joined path respecting path separators
     */
    protected String joinApiPath(String base, String path) {
        StringBuilder result = new StringBuilder();
        if (base == null) base = "";
        if (path == null) path = "";
        result.append(base);
        if (base.endsWith(PATH_SEPARATOR)) {
            if (path.startsWith(PATH_SEPARATOR))
                result.append(path.substring(1));
            else
                result.append(path);
        }
        else {
            if (path.startsWith(PATH_SEPARATOR) || path.startsWith(PORT_SEPARATOR))
                result.append(path);
            else {
                result.append(PATH_SEPARATOR);
                result.append(path);
            }
        }
        return result.toString();
    }


    protected boolean isEmpty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    private static final int BLOCK_SIZE = 4096;

    public static void drain(Reader r, Writer w) throws IOException {
        char[] bytes = new char[BLOCK_SIZE];
        try {
            int length = r.read(bytes);
            while (length != -1) {
                if (length != 0) {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally {
            bytes = null;
        }

    }

    /**
     * Given a list of names, create a comma-separated list of items to be passed on a URL
     *
     * @param list the list of names to be comma-separated
     * @return the comma-separated string representation of the list
     */
    protected String makeCommaSeparatedParamFromList(List<String> list)  {
        StringBuilder result = new StringBuilder();
        boolean initial = true;
        for (String item : list) {
            if (initial)
                initial = false;
            else
                result.append(",");
            result.append(item);
        }
        return result.toString();
    }

    protected final String formatDateMinusMinutes(int minutes) {
        return formatDateMinusMinutes(minutes, false);
    }

    protected final String now() {
        return now(false);
    }

    protected final String formatDateMinusMinutes(int minutes, boolean utc) {
        DateTime date = new DateTime().minusMinutes(minutes);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        if (utc)
            fmt = fmt.withZoneUTC();
        return fmt.print(date);
    }

    protected final String now(boolean utc) {
        DateTime date = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        if (utc)
            fmt = fmt.withZoneUTC();
        return fmt.print(date);
    }

}
