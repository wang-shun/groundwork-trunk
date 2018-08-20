package org.groundwork.agents.utils;

import java.util.List;
import java.util.UUID;

public class StringUtils {

	public static boolean isEmpty(String str) {
		return (str == null || str.trim().equals(""));
	}

    public static String concatPaths(String base, String ext) {
        StringBuffer result = new StringBuffer();
        if (base == null) base = "";
        if (ext == null)  ext = "";
        result.append(base);
        if (base.endsWith("/")) {
            if (ext.startsWith("/") && ext.length() > 1)
                result.append(ext.substring(1));
            else
                result.append(ext);
        }
        else {
            if (ext.startsWith("/"))
                result.append(ext);
            else  {
                result.append("/");
                result.append(ext);
            }
        }
        return result.toString();
    }

    public static boolean isUUID(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Java8: String listString = String.join(",", list);
    public static String join(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        for (String s : list)
        {
            sb.append(s);
            sb.append(delim);
        }
        return sb.toString();
    }
}
