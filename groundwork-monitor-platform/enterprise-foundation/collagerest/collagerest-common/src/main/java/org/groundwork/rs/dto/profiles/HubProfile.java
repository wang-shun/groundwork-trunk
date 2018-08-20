package org.groundwork.rs.dto.profiles;

public class HubProfile {

    private ProfileType profileType;
    private String agent;

    public HubProfile() {}

    public HubProfile(ProfileType profileType, String agent) {
        this.profileType = profileType;
        this.agent = agent;
    }

    /**
     * Look the profile type (Like application type) for this profile
     * @return ProfileType enum
     */
    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

}

