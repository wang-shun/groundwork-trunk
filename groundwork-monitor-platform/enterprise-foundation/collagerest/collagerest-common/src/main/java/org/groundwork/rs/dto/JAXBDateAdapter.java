package org.groundwork.rs.dto;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

public class JAXBDateAdapter extends XmlAdapter<String, Date> {

    public JAXBDateAdapter() {
    }

    public String marshal(Date date) {
        // Return a standard ISO-formatted date string, with the exception of not using a "Z" shorthand to represent UTC
        // as this has resulted in some complexity in integration with other languages that lack a standard ISO-8601
        // parser.
        return (date == null ? null : ISODateTimeFormat.dateTime().print(date.getTime()).replaceAll("Z$", "+00:00"));
    }

    public Date unmarshal(String s) {
        if (StringUtils.isBlank(s) || s.equals("0")) return null;
        if (NumberUtils.isNumber(s)) return new Date(Long.parseLong(s));
        return ISODateTimeFormat.dateTimeParser().parseDateTime(s).toDate();
    }

}
