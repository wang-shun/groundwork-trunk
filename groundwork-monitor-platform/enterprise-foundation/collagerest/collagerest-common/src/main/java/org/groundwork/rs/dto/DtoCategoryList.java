package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="categories")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCategoryList {

    @XmlElement(name="category")
    @JsonProperty("categories")
    private List<DtoCategory> categories = new ArrayList<DtoCategory>();

    public DtoCategoryList() {}
    public DtoCategoryList(List<DtoCategory> categories) {this.categories = categories;}

    public List<DtoCategory> getCategories() {
        return categories;
    }

    public void add(DtoCategory category) {
        categories.add(category);
    }

    public int size() {
        return categories.size();
    }

}
