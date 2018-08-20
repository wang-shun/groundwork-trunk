package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="tokens")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoTokensList {

    @XmlElement(name="tokens")
    @JsonProperty("tokens")
    private List<DtoToken> tokens = new ArrayList<DtoToken>();

    public DtoTokensList() {}
    public DtoTokensList(List<DtoToken> tokens) { this.tokens = tokens; }

    public List<DtoToken> getTokens() {
        return tokens;
    }

    public void add(DtoToken token) {
        tokens.add(token);
    }

    public int size() {
        return tokens.size();
    }

}
