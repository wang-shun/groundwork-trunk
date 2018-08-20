package org.itgroundwork.foundation.pagebeans;


/** @author Hibernate CodeGenerator */
public class PluginPlatform  {

    /** identifier field */
    private Integer platformId;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private Integer arch;

    /** nullable persistent field */
    private String description;

    /** full constructor */
    public PluginPlatform(Integer platformId, String name, Integer arch, String description) {
        this.platformId = platformId;
        this.name = name;
        this.arch = arch;
        this.description = description;
        
    }

    /** default constructor */
    public PluginPlatform() {
    }

    /** minimal constructor */
    public PluginPlatform(Integer platformId, String name) {
        this.platformId = platformId;
        this.name = name;
  
    }

    public Integer getPlatformId() {
        return this.platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getArch() {
        return this.arch;
    }

    public void setArch(Integer arch) {
        this.arch = arch;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

   

}
