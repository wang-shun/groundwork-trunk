package org.groundwork.rs.conversion;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoComment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NagiosSupport {

    protected static Log log = LogFactory.getLog(NagiosSupport.class);

    private static final String COMMENT_LEVEL_DELIMITER = "#!#";
    private static final String FIELD_LEVEL_DELIMITER = ";::;";
    private static final String COMMENT_DATE_FORMAT = "MM-dd-yyyy h:mm:ss";

    static List<DtoComment> parseNagiosComments(String commentsProperty) {
        List<DtoComment> parsedComments = new ArrayList<>();
        String[] comments = StringUtils.splitByWholeSeparator(commentsProperty, COMMENT_LEVEL_DELIMITER);
        if (comments == null) return parsedComments;

        DateFormat dateFormat = new SimpleDateFormat(COMMENT_DATE_FORMAT);
        for (String comment: comments) {
            String[] commentFields = StringUtils.splitByWholeSeparator(comment, FIELD_LEVEL_DELIMITER);
            if (commentFields.length != 4) continue;
            DtoComment dtoComment = new DtoComment();
            dtoComment.setId(Integer.valueOf(commentFields[0]));
            try {
                dtoComment.setCreatedOn(dateFormat.parse(commentFields[1]));
            } catch (ParseException e) {
                log.error("Unable to parse nagios comment: " + comment);
                continue;
            }
            dtoComment.setAuthor(commentFields[2]);
            dtoComment.setNotes(StringUtils.strip(commentFields[3], "'"));
            parsedComments.add(dtoComment);
        }
        return parsedComments;
    }
}
