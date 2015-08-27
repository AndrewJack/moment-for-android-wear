package technology.mainthread.apps.moment.data.vo;

public class UserDetails {

    private final String accountName;
    private final long userId;
    private final String googlePlusId;
    private final String displayName;
    private final String firstName;
    private final String lastName;
    private final String profileImageUrl;

    private UserDetails(Builder builder) {
        accountName = builder.accountName;
        userId = builder.userId;
        googlePlusId = builder.googlePlusId;
        displayName = builder.displayName;
        firstName = builder.firstName;
        lastName = builder.lastName;
        profileImageUrl = builder.profileImageUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(UserDetails copy) {
        Builder builder = new Builder();
        builder.accountName = copy.accountName;
        builder.userId = copy.userId;
        builder.googlePlusId = copy.googlePlusId;
        builder.displayName = copy.displayName;
        builder.firstName = copy.firstName;
        builder.lastName = copy.lastName;
        builder.profileImageUrl = copy.profileImageUrl;
        return builder;
    }

    public String getAccountName() {
        return accountName;
    }

    public long getUserId() {
        return userId;
    }

    public String getGooglePlusId() {
        return googlePlusId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static final class Builder {
        private String accountName;
        private long userId;
        private String googlePlusId;
        private String displayName;
        private String firstName;
        private String lastName;
        private String profileImageUrl;

        private Builder() {
        }

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder googlePlusId(String googlePlusId) {
            this.googlePlusId = googlePlusId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder profileImage(String profileImage) {
            this.profileImageUrl = profileImage;
            return this;
        }

        public UserDetails build() {
            return new UserDetails(this);
        }
    }
}
