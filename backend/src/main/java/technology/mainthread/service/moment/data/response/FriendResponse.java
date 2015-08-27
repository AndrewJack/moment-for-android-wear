package technology.mainthread.service.moment.data.response;

import technology.mainthread.service.moment.data.record.UserRecord;

public class FriendResponse {

    private long friendId;

    private String displayName;

    private String profileImageUrl;

    public FriendResponse() {
    }

    public FriendResponse setUserRecord(UserRecord userRecord) {
        friendId = userRecord.getId();
        displayName = userRecord.getDisplayName();
        profileImageUrl = userRecord.getProfileImageUrl();
        return this;
    }

    public long getFriendId() {
        return friendId;
    }

    public FriendResponse setFriendId(long friendId) {
        this.friendId = friendId;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FriendResponse setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public FriendResponse setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }
}
