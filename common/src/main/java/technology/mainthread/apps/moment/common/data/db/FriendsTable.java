package technology.mainthread.apps.moment.common.data.db;

import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Friend;

public interface FriendsTable {

    void add(Friend friend);

    Friend get(long index);

    List<Friend> getAll();

    void addOrUpdate(Friend friend);

    void update(Friend friend);

    boolean delete(long index);

    boolean deleteAll();

    boolean hasFriends();
}
