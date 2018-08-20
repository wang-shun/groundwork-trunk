package org.groundwork.foundation.bs.comment;

import com.groundwork.collage.model.Comment;
import org.groundwork.foundation.bs.BusinessService;

import java.util.List;

public interface CommentService extends BusinessService {

    Comment createHostComment(Integer hostId, String text, String author);

    Comment createServiceComment(Integer serviceId, String text, String author);

    Comment get(int id);

    void delete(int id);

}
