package technology.mainthread.service.moment.endpoint;

import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import technology.mainthread.service.moment.Messages;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.request.UserDetailsRequest;
import technology.mainthread.service.moment.data.response.UserRegisteredResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static technology.mainthread.service.moment.OfyService.ofy;

public class UserEndpointTest {

    private static final User CURRENT_USER = new User("userOne@example.com", "gmail.com", "1");
    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_ID_TWO = "deviceIdTwo";
    private static final String GOOGLE_PLUS_ID = "googlePlusId";
    private static final UserDetailsRequest REGISTER_REQUEST = new UserDetailsRequest()
            .setGooglePlusId(GOOGLE_PLUS_ID)
            .setFirstName("firstName")
            .setLastName("lastName");

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private UserEndpoint sut;
    private Closeable session;

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        session = ObjectifyService.begin();
        sut = new UserEndpoint();
    }

    @After
    public void tearDown() throws Exception {
        ofy().clear();
        session.close();
        helper.tearDown();
    }

    @Test(expected = OAuthRequestException.class)
    public void registerAuthenticationError() throws Exception {
        sut.register(null, null, null);
    }

    @Test(expected = BadRequestException.class)
    public void registerNullDeviceId() throws Exception {
        sut.register(null, REGISTER_REQUEST, CURRENT_USER);
    }

    @Test(expected = BadRequestException.class)
    public void registerNullGooglePlusId() throws Exception {
        sut.register(DEVICE_ID, null, CURRENT_USER);
    }

    @Test
    public void registerNewUser() throws Exception {
        UserRegisteredResponse response = sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);

        assertEquals(Messages.MESSAGE_REGISTERED, response.getStatus());

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getDevices().size());
        assertEquals(GOOGLE_PLUS_ID, list.get(0).getGooglePlusId());
        assertEquals(DEVICE_ID, list.get(0).getDevices().get(0));
    }

    @Test
    public void registerNewDeviceToExistingUser() throws Exception {
        UserRegisteredResponse responseOne = sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        UserRegisteredResponse responseTwo = sut.register(DEVICE_ID_TWO, REGISTER_REQUEST, CURRENT_USER);

        assertEquals(Messages.MESSAGE_REGISTERED, responseOne.getStatus());
        assertEquals(Messages.MESSAGE_REGISTERED, responseTwo.getStatus());

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(GOOGLE_PLUS_ID, list.get(0).getGooglePlusId());
        assertEquals(2, list.get(0).getDevices().size());
        assertEquals(DEVICE_ID, list.get(0).getDevices().get(0));
        assertEquals(DEVICE_ID_TWO, list.get(0).getDevices().get(1));
    }

    @Test
    public void registerSkipRegistrationIfAlreadyRegistered() throws Exception {
        UserRegisteredResponse responseOne = sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        UserRegisteredResponse responseTwo = sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);

        assertEquals(Messages.MESSAGE_REGISTERED, responseOne.getStatus());
        assertEquals(Messages.MESSAGE_ALREADY_REGISTERED, responseTwo.getStatus());

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(GOOGLE_PLUS_ID, list.get(0).getGooglePlusId());
        assertEquals(1, list.get(0).getDevices().size());
        assertEquals(DEVICE_ID, list.get(0).getDevices().get(0));
    }

    @Test(expected = OAuthRequestException.class)
    public void unregisterAuthenticationError() throws Exception {
        sut.unregister(null, null);
    }

    @Test(expected = BadRequestException.class)
    public void unregisterNullDeviceId() throws Exception {
        sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        sut.unregister(null, CURRENT_USER);
    }

    @Test
    public void unregisterRemovesDevice() throws Exception {
        sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        sut.unregister(DEVICE_ID, CURRENT_USER);

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(GOOGLE_PLUS_ID, list.get(0).getGooglePlusId());
        assertTrue(list.get(0).getDevices().isEmpty());
    }

    @Test
    public void unregisterDeviceThatDoesNotExist() throws Exception {
        sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        sut.unregister("anotherDevice", CURRENT_USER);

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(GOOGLE_PLUS_ID, list.get(0).getGooglePlusId());
        assertEquals(1, list.get(0).getDevices().size());
        assertEquals(DEVICE_ID, list.get(0).getDevices().get(0));
    }

    @Test(expected = OAuthRequestException.class)
    public void removeAuthenticationError() throws Exception {
        sut.remove(null);
    }

    @Test(expected = OAuthRequestException.class)
    public void removeUserThatDoesNotExist() throws Exception {
        sut.remove(CURRENT_USER);
    }

    @Test
    public void removeUser() throws Exception {
        sut.register(DEVICE_ID, REGISTER_REQUEST, CURRENT_USER);
        sut.remove(CURRENT_USER);

        List<UserRecord> list = ofy().load().type(UserRecord.class).list();
        assertTrue(list.isEmpty());
    }
}
