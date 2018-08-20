/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DtoFileList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="files")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoFileList {

    @XmlElement(name="file")
    @JsonProperty("files")
    private List<String> files = new ArrayList<String>();

    /**
     * Shallow copy constructor.
     *
     * @param files files to copy.
     */
    public DtoFileList(Collection<String> files) {
        this.files.addAll(files);
    }

    /**
     * Default constructor.
     */
    public DtoFileList() {
    }

    /**
     * Add file to files list.
     *
     * @param file file to add
     */
    public void add(String file) {
        files.add(file);
    }

    /**
     * Get files list size.
     *
     * @return size of files list
     */
    public int size()
    {
        return files.size();
    }

    public List<String> getFiles() {
        return files;
    }
}
