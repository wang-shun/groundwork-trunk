package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoComment {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String notes;

    @XmlAttribute
    private String author;

    @XmlAttribute
    private Date createdOn;

    public DtoComment() {
    }

    public DtoComment(Integer id, String notes, String author, Date createdOn) {
       this.id = id;
       this.notes = notes;
       this.author = author;
       this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return String.format("Comment [id=%d author=%s createdOn=%s]: %s", id, author, createdOn, notes);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String text) {
        this.notes = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

}
