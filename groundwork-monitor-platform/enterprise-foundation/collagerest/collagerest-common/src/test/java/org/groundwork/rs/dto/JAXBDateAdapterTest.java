package org.groundwork.rs.dto;

import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class JAXBDateAdapterTest {

    final static DateTimeZone defaultDateTimeZone = DateTimeZone.getDefault();

    public static class NonParameterizedTests {

        private static JAXBDateAdapter adapter = new JAXBDateAdapter();

        @Test
        public void MarshalNullTest() {
            assertNull(adapter.marshal(null));
        }

        @Test
        public void unmarshalNullTest() {
            assertNull(adapter.unmarshal(null));
            assertNull(adapter.unmarshal(""));
            assertNull(adapter.unmarshal(" "));
            assertNull(adapter.unmarshal("  "));
            assertNull(adapter.unmarshal("0"));
        }
    }


    @RunWith(Parameterized.class)
    public static class ParamterizedTests {

        private String marshalOutput;
        private long milliseconds;
        private String unmarshalInput;
        private String timeZone;
        private JAXBDateAdapter adapter;

        public ParamterizedTests(String unmarshalInput, long milliseconds, String marshalOutput, String timeZone) {
            this.unmarshalInput = unmarshalInput;
            this.milliseconds = milliseconds;
            this.marshalOutput = marshalOutput;
            this.timeZone = timeZone;
            this.adapter = new JAXBDateAdapter();
        }

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{

                    // ISO-8601 + RFC-822 - no milliseconds
                    {"2014-04-03T16:25:25-0600", 1396563925000L, "2014-04-03T17:25:25.000-05:00", "EST"},

                    // ISO-8601 + RFC-822 - with milliseconds
                    {"2014-04-03T16:25:25.011-0600", 1396563925011L, "2014-04-03T17:25:25.011-05:00", "EST"},

                    // ISO-8601 - RFC-822 - with milliseconds
                    {"2014-04-03T16:25:25.021-06:00", 1396563925021L, "2014-04-03T17:25:25.021-05:00", "EST"},

                    // ISO-8601 - RFC-822 - with fractional milliseconds
                    {"2014-04-03T14:45:15.01-06:00", 1396557915010L, "2014-04-03T15:45:15.010-05:00", "EST"},

                    // ISO-8601 - RFC-822 - with fractional milliseconds + european ,
                    {"2014-04-03T14:45:15,02-06:00", 1396557915020L, "2014-04-03T15:45:15.020-05:00", "EST"},

                    // ISO-8601 no milliseconds + short Timezone (hours only)
                    {"2014-04-03T14:45:15+00", 1396536315000L, "2014-04-03T09:45:15.000-05:00", "EST"},

                    // ISO-8601 no milliseconds - short Timezone (hours only)
                    {"2014-04-03T14:45:15-01", 1396539915000L, "2014-04-03T10:45:15.000-05:00", "EST"},

                    // ISO-8601 + short Zulu Timezone (hours minutes seconds)
                    {"2014-04-03T16:22:07Z", 1396542127000L, "2014-04-03T11:22:07.000-05:00", "EST"},

                    // ISO-8601 + short Zulu Timezone (hours minutes seconds ms)
                    {"2014-04-03T16:22:07.234Z", 1396542127234L, "2014-04-03T11:22:07.234-05:00", "EST"},

                    // ISO-8601 Short Zulu Timezone + fractional (glenn's perl case)
                    {"1985-04-12T10:15:30.5Z", 482148930500L, "1985-04-12T05:15:30.500-05:00", "EST"},

                    // ISO-8601 Short form no seconds
                    {"2014-04-03T16:22Z", 1396542120000L, "2014-04-03T11:22:00.000-05:00", "EST"},

                    // Numeric (milliseconds since epoch) format
                    {"1396542120000", 1396542120000L, "2014-04-03T11:22:00.000-05:00", "EST"},

                    // Check for full "-00:00" instead of shorthand "Z" for compatibility
                    {"1", 1L, "1970-01-01T00:00:00.001+00:00", "UTC"},
                    {"1970-01-01T00:00:00.001-00:00", 1L, "1970-01-01T00:00:00.001+00:00", "UTC"},
                    {"1970-01-01T00:00:00.001+00:00", 1L, "1970-01-01T00:00:00.001+00:00", "UTC"},
                    {"1970-01-01T00:00:00.001Z", 1L, "1970-01-01T00:00:00.001+00:00", "UTC"},

                    // Check some non-negative offsets
                    {"1", 1L, "1970-01-01T02:00:00.001+02:00", "EET"},
                    {"1970-01-01T02:00:00.001+02:00", 1L, "1970-01-01T02:00:00.001+02:00", "EET"},

            });
        }

        @Test
        public void marshallTest() {
            DateTimeZone.setDefault(DateTimeZone.forID(timeZone));
            assertEquals(marshalOutput, adapter.marshal(new Date(milliseconds)));
            DateTimeZone.setDefault(defaultDateTimeZone);
        }

        @Test
        public void unmarshallTest() {
            DateTimeZone.setDefault(DateTimeZone.forID(timeZone));
            assertEquals(milliseconds, adapter.unmarshal(unmarshalInput).getTime());
            DateTimeZone.setDefault(defaultDateTimeZone);
        }

    }

}
