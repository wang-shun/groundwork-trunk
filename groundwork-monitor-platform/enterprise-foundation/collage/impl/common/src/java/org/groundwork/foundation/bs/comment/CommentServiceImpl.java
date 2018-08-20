package org.groundwork.foundation.bs.comment;

import com.groundwork.collage.model.Comment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.dao.FoundationDAO;

import java.util.Date;

public class CommentServiceImpl extends EntityBusinessServiceImpl implements CommentService {

    protected static Log log = LogFactory.getLog(CommentServiceImpl.class);

    protected CommentServiceImpl(FoundationDAO foundationDAO) {
        super(foundationDAO, Comment.INTERFACE_NAME, Comment.COMPONENT_NAME);
    }

    public Comment createHostComment(Integer hostId, String notes, String author) {
        Comment comment = create(notes, author);
        comment.setHostId(hostId);
        save(comment);
        return comment;
    }

    public Comment createServiceComment(Integer serviceId, String notes, String author) {
        Comment comment = create(notes, author);
        comment.setServiceStatusId(serviceId);
        save(comment);
        return comment;
    }

    private Comment create(String notes, String author) {
        if(log.isDebugEnabled()) log.debug("Creating comment");
        Comment comment = (Comment) this.create();
        comment.setNotes(notes);
        comment.setAuthor(author);
        comment.setCreatedOn(new Date());
        comment.setHostId(null);
        comment.setServiceStatusId(null);
        return comment;
    }

    public Comment get(int id) {
        if(log.isDebugEnabled()) log.debug("Getting comment id=" + id);
        return (Comment) queryById(id);
    }

    public void delete(int id) {
        if(log.isDebugEnabled()) log.debug("Deleting comment id=" + id);
        super.delete(id);
    }
}
