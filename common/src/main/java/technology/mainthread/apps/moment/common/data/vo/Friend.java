package technology.mainthread.apps.moment.common.data.vo;

public class Friend {

    private final int index;
    private final long recordId;
    private final long friendId;
    private final String displayName;
    private final String profileImageUrl;

    private Friend(Builder builder) {
        index = builder.index;
        recordId = builder.recordId;
        friendId = builder.friendId;
        displayName = builder.displayName;
        profileImageUrl = builder.profileImageUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Friend copy) {
        Builder builder = new Builder();
        builder.index = copy.index;
        builder.recordId = copy.recordId;
        builder.friendId = copy.friendId;
        builder.displayName = copy.displayName;
        builder.profileImageUrl = copy.profileImageUrl;
        return builder;
    }

    public int getIndex() {
        return index;
    }

    public long getRecordId() {
        return recordId;
    }

    public long getFriendId() {
        return friendId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static final class Builder {
        private int index;
        private long recordId;
        private long friendId;
        private String displayName;
        private String profileImageUrl;

        private Builder() {
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder recordId(long recordId) {
            this.recordId = recordId;
            return this;
        }

        public Builder friendId(long friendId) {
            this.friendId = friendId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Friend build() {
            return new Friend(this);
        }
    }
}
