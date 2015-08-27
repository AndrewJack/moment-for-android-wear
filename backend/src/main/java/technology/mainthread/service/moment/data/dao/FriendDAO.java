package technology.mainthread.service.moment.data.dao;

import java.util.List;

import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.UserRecord;

public interface FriendDAO {

    /**
     * Get a friend record using the parent {@link UserRecord}
     *
     * @param user whom the {@link FriendRecord} belongs
     */
    FriendRecord getFriendRecord(UserRecord user);

    /**
     * Get a list of friend ids using the FriendRecords parent
     *
     * @param user whom the {@link FriendRecord} and friends belong
     */
    List<Long> getUsersFriends(UserRecord user);

    /**
     * Save a friend record
     */
    void save(FriendRecord friendRecord);

    /**
     * Delete friend record with parent record
     *
     * @param currentUser parent of FriendRecord
     */
    void delete(UserRecord currentUser);

    /**
     * Check if two users are friends
     */
    boolean isFriend(Long currentUserId, Long potentialFriendUserId);

    /**
     * Get users that have added user, but they have not added back
     */
    List<Long> getAddRequests(UserRecord user);

}
