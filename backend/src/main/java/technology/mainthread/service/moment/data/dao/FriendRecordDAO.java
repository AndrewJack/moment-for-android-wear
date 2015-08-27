package technology.mainthread.service.moment.data.dao;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.UserRecord;

public class FriendRecordDAO implements FriendDAO {

    private final Objectify ofy;

    public FriendRecordDAO(Objectify ofy) {
        this.ofy = ofy;
    }

    private FriendRecord getFriendRecord(Key<UserRecord> user) {
        return ofy.load().type(FriendRecord.class).ancestor(user).first().now();
    }

    private FriendRecord getFriendRecord(Long userId) {
        Key<UserRecord> key = Key.create(UserRecord.class, userId);
        return getFriendRecord(key);
    }

    @Override
    public FriendRecord getFriendRecord(UserRecord user) {
        Key<UserRecord> key = Key.create(user);
        return getFriendRecord(key);
    }

    @Override
    public List<Long> getUsersFriends(UserRecord user) {
        FriendRecord friendRecord = getFriendRecord(user);
        if (friendRecord != null) {
            return friendRecord.getFriends();
        }
        return Collections.emptyList();
    }

    @Override
    public void save(FriendRecord friendRecord) {
        ofy.save().entity(friendRecord).now();
    }

    @Override
    public void delete(UserRecord currentUser) {
        FriendRecord friendRecord = getFriendRecord(currentUser);
        if (friendRecord != null) {
            ofy.delete().entity(friendRecord).now();
        }
    }

    @Override
    public boolean isFriend(Long currentUserId, Long potentialFriendUserId) {
        List<Long> currentUserFriends = getFriendRecord(currentUserId).getFriends();
        List<Long> potentialFriendUserFriends = getFriendRecord(potentialFriendUserId).getFriends();
        return currentUserFriends.contains(potentialFriendUserId) && potentialFriendUserFriends.contains(currentUserId);
    }

    @Override
    public List<Long> getAddRequests(UserRecord user) {
        Query<FriendRecord> query = ofy.load().type(FriendRecord.class).filter("friends", user.getId());

        List<Long> addedUsers = new ArrayList<>();
        if (query.iterable().iterator().hasNext()) {
            FriendRecord userFriendRecord = getFriendRecord(user);
            if (userFriendRecord != null) {
                for (FriendRecord record : query) {
                    long id = record.getUser().getId();
                    if (!userFriendRecord.getFriends().contains(id)) {
                        addedUsers.add(id);
                    }
                }
            }
        }
        return addedUsers;
    }
}