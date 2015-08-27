package technology.mainthread.service.moment.data.record;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FriendRecord {

    @Id
    private Long id;

    @Parent
    private Key<UserRecord> user;

    @Index
    private List<Long> friends = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Key<UserRecord> getUser() {
        return user;
    }

    public FriendRecord setUser(Key<UserRecord> user) {
        this.user = user;
        return this;
    }

    public List<Long> getFriends() {
        return friends;
    }

    public FriendRecord addFriend(Long friendId) {
        this.friends.add(friendId);
        return this;
    }

    public boolean removeFriend(Long friendId) {
        return this.friends.remove(friendId);
    }

}
