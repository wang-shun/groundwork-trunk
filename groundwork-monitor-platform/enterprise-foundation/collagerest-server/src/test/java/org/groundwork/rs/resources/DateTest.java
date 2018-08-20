package org.groundwork.rs.resources;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTest {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATE_FORMAT_MS = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String ISO_DATE_FORMAT_NOMS = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final String ISO_BASIC = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_BASIC2 = "yyyy-MM-dd'T'HH:mm:ssZZ";

    /**
     * Joda can handle both 0700 and 07:00 formats interchangebly
     * However there is not a clear way to bridge to Java DateFormat, necessary for Jackson integration
     * We could convert all fields to Joda DateTime...
     *
     * @throws Exception
     */
    @Test
    public void testJoda() throws Exception {
        String[] TEST_DATA = {
                "2014-04-03T16:25:25-0600",        // ISO-8601 + RFC-822 - no milliseconds
                "2014-04-03T16:25:25.011-0600",    // ISO-8601 + RFC-822 - with milliseconds
                "2014-04-03T16:25:25.021-06:00",   // ISO-8601 - RFC-822 - with milliseconds
                "2014-04-03T14:45:15.01-06:00",    // ISO-8601 - RFC-822 - with fractional milliseconds
                "2014-04-03T14:45:15,02-06:00",    // ISO-8601 - RFC-822 - with fractional milliseconds + european ,
                "2014-04-03T14:45:15+00",          // ISO-8601 no milliseconds + short Timezone (hours only)
                "2014-04-03T14:45:15-01",          // ISO-8601 no milliseconds - short Timezone (hours only)
                "2014-04-03T16:22:07Z",            // ISO-8601 + short Zulu Timezone (hours minutes seconds)
                "2014-04-03T16:22:07.234Z",        // ISO-8601 + short Zulu Timezone (hours minutes seconds ms)
                "1985-04-12T10:15:30.5Z",          // ISO-8601 Short Zulu Timezone + fractional (glenn's perl case)
                "2014-04-03T16:22Z"                // ISO-8601 Short form no seconds
        };
        System.out.println("--------------------------");
        System.out.println("Local Time Formatted:");
        System.out.println("--------------------------");
        DateTimeFormatter localParser = ISODateTimeFormat.dateTimeParser();
        DateTimeFormatter fmt = DateTimeFormat.forPattern(ISO_BASIC2);

        for (String test : TEST_DATA) {
            DateTime dt = localParser.parseDateTime(test);
            System.out.println(dt);
            System.out.println(dt.getMillis());
            //String convertedBack = dt.toString(ISO_BASIC2);
            String convertedBack = ISODateTimeFormat.dateTimeParser().parseDateTime(test).toDateTime().toString();
            System.out.println("converted " + convertedBack);
            String convertedBackISO = ISODateTimeFormat.dateTime().print(dt);
            System.out.println("ISO with ::  " + convertedBackISO);
        }
        System.out.println("--------------------------");
        System.out.println("UTC Formatted:");
        System.out.println("--------------------------");
        // Print out in UTC Time
        DateTimeFormatter utcParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();
        for (String test : TEST_DATA) {
            System.out.println(utcParser.parseDateTime(test));
        }

    }

    @Test
    public void testFormatter() throws Exception {
        DateTime date = new DateTime().minusHours(1).minusMinutes(10);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println("format = " + fmt.print(date));
        fmt = fmt.withZoneUTC();
        System.out.println("format = " + fmt.print(date));
    }

    @Test
    public void testDates() throws Exception {
        String d1 = "2014-04-03T16:25:25-0600";
        String d2 = "2014-04-03T16:25:25.001-0600";
        String d4 = "2014-04-03T16:25:25.001-06:00";
        String d3 = "2014-04-03T16:22:07Z";

        DateFormat isoFormatter = new SimpleDateFormat(ISO_DATE_FORMAT_MS);
        Date date = isoFormatter.parse(d2);
        System.out.println(date);
        date = isoFormatter.parse(d4);
        System.out.println(date);

        int count = 0;
        try {
            DateFormat isoFormatterNoMs = new SimpleDateFormat(ISO_DATE_FORMAT_NOMS);
            date = isoFormatterNoMs.parse(d4);
            System.out.println(date);
        } catch (Exception e) {
            count++;
        }
        assert count == 1;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getDefault());
        String value = dateFormat.format(date);
        System.out.println(value);
    }

    /**
     * This fails Z and 'Z' are incompatible
     *
     * @throws Exception
     */
    @Test
    public void testZuluDates() throws Exception {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String d1 = dateFormat.format(now);
        System.out.println(d1);

        Date date = dateFormat.parse(d1);
        String d2 = dateFormat.format(date);
        System.out.println(d2);
        assert d1.equals(d2);

        DateFormat dateFormatOld = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date dateOld = dateFormatOld.parse(d1);
        String d3 = dateFormatOld.format(dateOld);
        System.out.println(d3);

    }

    /**
     * This test fails, 0700 and 07:00 are not intermixable
     * @throws Exception
     */
    @Test
    public void funkyZuluTest() throws Exception {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setLenient(true);
        dateFormat.setTimeZone(TimeZone.getDefault());
        System.out.println(dateFormat.format(now));
        Date later = dateFormat.parse("2017-06-19T13:48:15-0700");
        Date later2 = dateFormat.parse("2017-06-19T13:48:15-07:00");
        assert later.equals(later2);
    }

}

