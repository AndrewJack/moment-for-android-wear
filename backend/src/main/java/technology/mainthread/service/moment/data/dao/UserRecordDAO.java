package technology.mainthread.service.moment.data.dao;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Objectify;

import java.util.ArrayList;
import java.util.List;

import technology.mainthread.service.moment.data.record.UserRecord;

public class UserRecordDAO implements UserDAO {

    private final Objectify ofy;

    public UserRecordDAO(Objectify ofy) {
        this.ofy = ofy;
    }

    @Override
    public boolean notRegistered(User user) {
        if (user != null) {
            UserRecord record = ofy.load().type(UserRecord.class).filter("user", user).first().now();
            if (record != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public UserRecord getUserRecord(User user) {
        if (user != null) {
            UserRecord record = ofy.load().type(UserRecord.class).filter("user", user).first().now();
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    @Override
    public UserRecord getUserRecord(long id) {
        return ofy.load().type(UserRecord.class).id(id).now();
    }

    @Override
    public List<UserRecord> findUsers(List<String> googlePlusIds) {
        return ofy.load().type(UserRecord.class).filter("googlePlusId in", googlePlusIds).list();
    }

    @Override
    public void save(UserRecord record) {
        ofy.save().entity(record).now();
    }

    @Override
    public void delete(UserRecord record) {
        ofy.delete().entity(record).now();
    }

    @Override
    public List<String> getUserNames(List<Long> recipients) {
        List<String> userNames = new ArrayList<>();

        for (Long userId : recipients) {
            userNames.add(getUserRecord(userId).getDisplayName());
        }

        return userNames;
    }

}
