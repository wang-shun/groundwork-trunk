package org.groundwork.rs.conversion;

import org.groundwork.rs.dto.DtoComment;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NagiosSupportTest {

    @Test
    public void TestNullNagiosComment() {
        assertEquals(0, NagiosSupport.parseNagiosComments(null).size());
        assertEquals(0, NagiosSupport.parseNagiosComments("").size());
        assertEquals(0, NagiosSupport.parseNagiosComments("    ").size());
    }

    @Test
    public void TestSingleParseNagiosComments() {
        String comments = "#!#1;::;11-30-2017 11:20:37;::;admin;::;'asdfasdf'";
        List<DtoComment> dtoCommentList = NagiosSupport.parseNagiosComments(comments);
        assertEquals(1, dtoCommentList.size());
        DtoComment dto = dtoCommentList.get(0);
        assertEquals((Integer) 1, dto.getId());
        assertEquals(new DateTime(2017, 11, 30, 11, 20, 37).toDate(), dto.getCreatedOn());
        assertEquals("admin", dto.getAuthor());
        assertEquals("asdfasdf", dto.getNotes());
    }

    @Test
    public void TestMultipleParseNagiosComments() {
        String comments = "#!#5;::;12-11-2017 11:53:07;::;admin3;::;'comment3'#!#4;::;12-11-2017 11:53:02;::;admin2;::;'comment2'#!#3;::;12-11-2017 11:52:55;::;admin1;::;'comment1'";
        List<DtoComment> dtoCommentList = NagiosSupport.parseNagiosComments(comments);
        assertEquals(3, dtoCommentList.size());

        // While there is no guarantee on order returned, returning them in "as-is" order is the implementation
        DtoComment dto1 = dtoCommentList.get(0);
        assertEquals((Integer) 5, dto1.getId());
        assertEquals(new DateTime(2017, 12, 11, 11, 53, 7).toDate(), dto1.getCreatedOn());
        assertEquals("admin3", dto1.getAuthor());
        assertEquals("comment3", dto1.getNotes());

        DtoComment dto2 = dtoCommentList.get(1);
        assertEquals((Integer) 4, dto2.getId());
        assertEquals(new DateTime(2017, 12, 11, 11, 53, 2).toDate(), dto2.getCreatedOn());
        assertEquals("admin2", dto2.getAuthor());
        assertEquals("comment2", dto2.getNotes());

        DtoComment dto3 = dtoCommentList.get(2);
        assertEquals((Integer) 3, dto3.getId());
        assertEquals(new DateTime(2017, 12, 11, 11, 52, 55).toDate(), dto3.getCreatedOn());
        assertEquals("admin1", dto3.getAuthor());
        assertEquals("comment1", dto3.getNotes());
    }
}
