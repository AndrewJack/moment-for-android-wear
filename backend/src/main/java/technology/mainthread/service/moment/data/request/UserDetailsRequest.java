package technology.mainthread.service.moment.data.request;

public class UserDetailsRequest {

    private String googlePlusId;

    private String displayName;

    private String firstName;

    private String lastName;

    private String profileImageUrl;

    public String getGooglePlusId() {
        return googlePlusId;
    }

    public UserDetailsRequest setGooglePlusId(String googlePlusId) {
        this.googlePlusId = googlePlusId;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserDetailsRequest setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserDetailsRequest setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public UserDetailsRequest setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserDetailsRequest setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }
}
