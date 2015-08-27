package technology.mainthread.service.moment.data.dao;

import com.google.appengine.api.users.User;

import java.util.List;

import technology.mainthread.service.moment.data.record.UserRecord;

public interface UserDAO {

    /**
     * Checks if user is not registered
     */
    boolean notRegistered(User user);

    /**
     * Retrieves a UserRecord with @param User
     */
    UserRecord getUserRecord(User user);

    /**
     * Retrieves a UserRecord with @param id
     */
    UserRecord getUserRecord(long id);

    /**
     * Find users signed up to moment using a list of google plus ids
     */
    List<UserRecord> findUsers(List<String> googlePlusIds);

    /**
     * Save a UserRecord
     */
    void save(UserRecord userRecord);

    /**
     * Delete a UserRecord
     */
    void delete(UserRecord userRecord);

    /**
     * Given a list of user ids return a list of user names
     */
    List<String> getUserNames(List<Long> recipients);
}
