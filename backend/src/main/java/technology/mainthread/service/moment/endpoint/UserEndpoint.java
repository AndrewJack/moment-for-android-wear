package technology.mainthread.service.moment.endpoint;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

import java.util.Date;

import javax.inject.Named;

import technology.mainthread.service.moment.Config;
import technology.mainthread.service.moment.Messages;
import technology.mainthread.service.moment.data.dao.FriendDAO;
import technology.mainthread.service.moment.data.dao.UserDAO;
import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.request.UserDetailsRequest;
import technology.mainthread.service.moment.data.response.UserRegisteredResponse;
import technology.mainthread.service.moment.module.DaoModule;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

@Api(
        name = "userApi",
        description = "Managing Users and devices",
        version = "v1",
        scopes = {Config.EMAIL_SCOPE},
        clientIds = {Config.WEB_CLIENT_ID, Config.ANDROID_CLIENT_ID_DEBUG, Config.ANDROID_CLIENT_ID_RELEASE},
        audiences = {Config.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = Config.OWNER_DOMAIN,
                ownerName = Config.OWNER_NAME
        )
)
public class UserEndpoint {

    private final UserDAO userDAO;
    private final FriendDAO friendDAO;

    public UserEndpoint() {
        this(DaoModule.userDAO(), DaoModule.friendDAO());
    }

    public UserEndpoint(UserDAO userDAO, FriendDAO friendDAO) {
        this.userDAO = userDAO;
        this.friendDAO = friendDAO;
    }

    /**
     * Register a user and a device
     * If the user already exists the device is added to that user
     * If the user and device exists the operation is skipped
     *
     * @param deviceId           The Google Cloud Messaging registration Id to add
     * @param userDetailsRequest User details
     * @param user               The Google Authenticated User
     */
    @ApiMethod(
            name = "users.register",
            path = "users/register",
            httpMethod = HttpMethod.POST
    )
    public UserRegisteredResponse register(@Named("deviceId") String deviceId, UserDetailsRequest userDetailsRequest,
                                           User user) throws OAuthRequestException, BadRequestException {
        // check user has passed in authentication but we shouldn't check against our store,
        // because this is a creation method
        if (user == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        if (deviceId == null || userDetailsRequest == null || userDetailsRequest.getGooglePlusId() == null
                || userDetailsRequest.getFirstName() == null || userDetailsRequest.getLastName() == null) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        UserRecord currentUser = userDAO.getUserRecord(user);
        boolean newUser = false;
        if (currentUser == null) {
            // user not found, create new user
            newUser = true;
            currentUser = new UserRecord()
                    .setUser(user)
                    .setDisplayName(userDetailsRequest.getDisplayName())
                    .setFirstName(userDetailsRequest.getFirstName())
                    .setLastName(userDetailsRequest.getLastName())
                    .setGooglePlusId(userDetailsRequest.getGooglePlusId())
                    .setProfileImageUrl(userDetailsRequest.getProfileImageUrl())
                    .addDevice(deviceId)
                    .setCreated(new Date());
        } else {
            // user already created, check if device is registered
            // TODO: update profile image, name(s)
            for (String id : currentUser.getDevices()) {
                if (id.equals(deviceId)) {
                    // device already registered skip add
                    return new UserRegisteredResponse()
                            .setId(currentUser.getId())
                            .setStatus(Messages.MESSAGE_ALREADY_REGISTERED);
                }
            }
            currentUser.addDevice(deviceId);
        }
        // save record
        userDAO.save(currentUser);
        if (newUser) {
            friendDAO.save(new FriendRecord().setUser(Key.create(currentUser)));
        }

        return new UserRegisteredResponse()
                .setId(currentUser.getId())
                .setStatus(Messages.MESSAGE_REGISTERED);
    }

    /**
     * Unregister a device
     * This will not remove a user
     *
     * @param deviceId The Google Cloud Messaging registration Id to remove
     * @param user     The Google Authenticated User
     */
    @ApiMethod(
            name = "users.unregister",
            path = "users/unregister",
            httpMethod = HttpMethod.POST
    )
    public void unregister(@Named("deviceId") String deviceId, User user) throws OAuthRequestException, BadRequestException {
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        if (deviceId == null) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        for (String id : currentUser.getDevices()) {
            if (id.equals(deviceId)) {
                // found device - unregister it
                currentUser.removeDevice(id);
                userDAO.save(currentUser);
                return;
            }
        }
    }

    /**
     * Remove a user
     *
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "users.remove",
            path = "users",
            httpMethod = HttpMethod.DELETE
    )
    public void remove(User user) throws OAuthRequestException {
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        // delete friends record
        friendDAO.delete(currentUser);
        // delete user record
        userDAO.delete(currentUser);

        // TODO: start task to remove user from other people's friends
    }

}
