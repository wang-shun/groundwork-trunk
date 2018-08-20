package org.groundwork.rs.examples;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CollageClientUtils {

    protected static Date parseDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return format.parse(date);
        } catch (Exception e) {
        }
        return null;
    }

}
