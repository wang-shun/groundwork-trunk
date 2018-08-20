package com.groundwork.collage.model;

import java.util.Date;

public interface Comment {

    /** Spring bean interface id */
    String INTERFACE_NAME = "com.groundwork.collage.model.Comment";

    /** Hibernate component name that this entity service using */
    String COMPONENT_NAME = "com.groundwork.collage.model.impl.Comment";

    /** Hibernate Property Constants **/
    String HP_ID = "commentId";
    String HP_NOTES = "notes";
    String HP_AUTHOR = "author";
    String HP_CREATEDON = "createdon";

    Integer getCommentId();

    //void setCommentId(Integer commentId);

    String getNotes();

    void setNotes(String notes);

    String getAuthor();

    void setAuthor(String author);

    Date getCreatedOn();

    void setCreatedOn(Date createdOn);

    Integer getHostId();

    void setHostId(Integer hostId);

    Integer getServiceStatusId();

    void setServiceStatusId(Integer serviceStatusId);

}
