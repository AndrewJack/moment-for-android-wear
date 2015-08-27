package technology.mainthread.service.moment.endpoint;

import com.google.android.gcm.server.Message;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import technology.mainthread.service.moment.Config;
import technology.mainthread.service.moment.Messages;
import technology.mainthread.service.moment.data.dao.FriendDAO;
import technology.mainthread.service.moment.data.dao.UserDAO;
import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.response.FriendResponse;
import technology.mainthread.service.moment.gcm.GcmHelper;
import technology.mainthread.service.moment.module.DaoModule;
import technology.mainthread.service.moment.module.GcmModule;

@Api(
        name = "friendApi",
        description = "Manage current user's friends",
        version = "v1",
        scopes = {Config.EMAIL_SCOPE},
        clientIds = {Config.WEB_CLIENT_ID, Config.ANDROID_CLIENT_ID_DEBUG, Config.ANDROID_CLIENT_ID_RELEASE},
        audiences = {Config.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = Config.OWNER_DOMAIN,
                ownerName = Config.OWNER_NAME
        )
)
public class FriendEndpoint {

    private final UserDAO userDAO;
    private final FriendDAO friendDAO;
    private final GcmHelper gcmHelper;

    public FriendEndpoint() {
        this(DaoModule.userDAO(), DaoModule.friendDAO(), GcmModule.gcmHelper());
    }

    public FriendEndpoint(UserDAO userDAO, FriendDAO friendDAO, GcmHelper gcmHelper) {
        this.userDAO = userDAO;
        this.friendDAO = friendDAO;
        this.gcmHelper = gcmHelper;
    }

    /**
     * Get user's Friends
     *
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "friends.all",
            path = "friends",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public CollectionResponse<FriendResponse> getFriends(User user) throws OAuthRequestException {
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        List<FriendResponse> responseList = new ArrayList<>();

        List<Long> friends = friendDAO.getUsersFriends(currentUser);
        for (Long friendId : friends) {
            UserRecord friendUserRecord = userDAO.getUserRecord(friendId);
            responseList.add(new FriendResponse().setUserRecord(friendUserRecord));
        }

        return CollectionResponse.<FriendResponse>builder().setItems(responseList).build();
    }

    /**
     * Find google plus friends that are signed up to Moment, but are not already added
     *
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "friends.search",
            path = "friends/search",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public CollectionResponse<FriendResponse> findFriends(@Named("googlePlusIds") List<String> googlePlusIds, User user)
            throws OAuthRequestException {

        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        // get list of users with a google id
        List<Long> friends = friendDAO.getUsersFriends(currentUser);
        List<UserRecord> gPlusUsers = userDAO.findUsers(googlePlusIds);

        List<FriendResponse> foundFriends = new ArrayList<>(gPlusUsers.size());
        for (UserRecord userRecord : gPlusUsers) {
            if (!friends.contains(userRecord.getId())) {
                foundFriends.add(new FriendResponse()
                        .setUserRecord(userRecord));
            }
        }

        return CollectionResponse.<FriendResponse>builder().setItems(foundFriends).build();
    }

    /**
     * Add user's friend
     *
     * @param friendId The user id of the friend to add
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "friends.add",
            path = "friends",
            httpMethod = ApiMethod.HttpMethod.PUT
    )
    public void addFriend(@Named("friendId") Long friendId, User user)
            throws OAuthRequestException, BadRequestException, NotFoundException, IOException {

        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        if (friendId == 0) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        FriendRecord friendRecord = friendDAO.getFriendRecord(currentUser);
        if (friendRecord == null) {
            Key<UserRecord> currentUserKey = Key.create(currentUser);
            friendRecord = new FriendRecord().setUser(currentUserKey);
        }

        UserRecord friend = userDAO.getUserRecord(friendId);
        if (friend != null) {
            if (!friendRecord.getFriends().contains(friend.getId())) {
                friendDAO.save(friendRecord.addFriend(friend.getId()));
            }
        } else {
            throw new NotFoundException(Messages.ERROR_FRIEND_NOT_FOUND);
        }

        boolean isFriend = friendDAO.isFriend(currentUser.getId(), friendId);
        Message message = new Message.Builder()
                .addData(Config.GCM_KEY_FRIEND_ID, currentUser.getId().toString())
                .addData(Config.GCM_KEY_FRIEND_NAME, currentUser.getFirstName())
                .addData(Config.GCM_KEY_IS_FRIEND, Boolean.toString(isFriend))
                .build();
        gcmHelper.sendGcmMessage(userDAO.getUserRecord(friend.getUser()), message);
    }

    /**
     * Get User's friend requests
     * users that have added the current user
     *
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "friends.requests",
            path = "friends/requests",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public CollectionResponse<FriendResponse> getFriendRequests(User user) throws OAuthRequestException {
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        List<Long> requestedAdds = friendDAO.getAddRequests(currentUser);
        List<FriendResponse> responseList = new ArrayList<>(requestedAdds.size());
        for (Long requestedAdd : requestedAdds) {
            UserRecord requestingUser = userDAO.getUserRecord(requestedAdd);
            responseList.add(new FriendResponse().setUserRecord(requestingUser));
        }

        return CollectionResponse.<FriendResponse>builder().setItems(responseList).build();
    }

    /**
     * Remove user's friend
     *
     * @param friendId The user id of the friend to remove
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "friends.remove",
            path = "friends/{friendId}",
            httpMethod = ApiMethod.HttpMethod.DELETE
    )
    public void removeFriend(@Named("friendId") long friendId, User user)
            throws OAuthRequestException, BadRequestException, NotFoundException, IOException {

        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        if (friendId == 0) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        FriendRecord friendRecord = friendDAO.getFriendRecord(currentUser);
        if (friendRecord != null && friendRecord.removeFriend(friendId)) {
            // if friend was found and removed then update friend record
            friendDAO.save(friendRecord);
        } else {
            throw new NotFoundException(Messages.ERROR_FRIEND_NOT_FOUND);
        }
    }

}
