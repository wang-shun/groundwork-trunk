package org.groundwork.rs.examples;

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
}
