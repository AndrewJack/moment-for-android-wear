package technology.mainthread.service.moment.endpoint;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.response.FriendResponse;
import technology.mainthread.service.moment.fake.FakeGcmHelper;
import technology.mainthread.service.moment.module.DaoModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static technology.mainthread.service.moment.OfyService.ofy;

public class FriendEndpointTest {

    private static final User CURRENT_USER = new User("userOne@example.com", "gmail.com", "1");
    private static final User FRIEND_USER_ONE = new User("friendOne@example.com", "gmail.com", "2");
    private static final User FRIEND_USER_TWO = new User("friendTwo@example.com", "gmail.com", "3");
    private static final User FRIEND_USER_THREE = new User("friendThree@example.com", "gmail.com", "4");

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private FriendEndpoint sut;
    private Closeable session;
    private final UserRecord currentUserRecord = new UserRecord().setUser(CURRENT_USER).setDisplayName("currentUser").setGooglePlusId("gPlusId1");
    private final UserRecord friendUserRecordOne = new UserRecord().setUser(FRIEND_USER_ONE).setDisplayName("userOne").setGooglePlusId("gPlusId2");
    private final UserRecord friendUserRecordTwo = new UserRecord().setUser(FRIEND_USER_TWO).setDisplayName("userTwo").setGooglePlusId("gPlusId3");
    private final UserRecord friendUserRecordThree = new UserRecord().setUser(FRIEND_USER_THREE).setDisplayName("userThree").setGooglePlusId("gPlusId4");

    private FriendRecord friendRecordCurrent;

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        session = ObjectifyService.begin();
        sut = new FriendEndpoint(DaoModule.userDAO(), DaoModule.friendDAO(), new FakeGcmHelper());
        ofy().save().entity(currentUserRecord).now();
        ofy().save().entity(friendUserRecordOne).now();
        ofy().save().entity(friendUserRecordTwo).now();
        ofy().save().entity(friendUserRecordThree).now();

        friendRecordCurrent = new FriendRecord().setUser(Key.create(currentUserRecord)).addFriend(friendUserRecordOne.getId());
        FriendRecord friendRecordOne = new FriendRecord().setUser(Key.create(friendUserRecordOne));
        FriendRecord friendRecordTwo = new FriendRecord().setUser(Key.create(friendUserRecordTwo));
        FriendRecord friendRecordThree = new FriendRecord().setUser(Key.create(friendUserRecordThree));

        ofy().save().entity(friendRecordCurrent).now();
        ofy().save().entity(friendRecordOne).now();
        ofy().save().entity(friendRecordTwo).now();
        ofy().save().entity(friendRecordThree).now();
    }

    @After
    public void tearDown() throws Exception {
        ofy().clear();
        session.close();
        helper.tearDown();
    }

    @Test(expected = OAuthRequestException.class)
    public void getFriendsWithNullUser() throws Exception {
        sut.getFriends(null);
    }

    @Test
    public void getFriendsReturnsOneFriend() throws Exception {
        List<FriendResponse> friends = (List<FriendResponse>) sut.getFriends(CURRENT_USER).getItems();
        assertEquals(1, friends.size());
    }

    @Test
    public void getFriendsReturnsTwoFriendsWithCorrectContent() throws Exception {
        ofy().save().entity(friendRecordCurrent.addFriend(friendUserRecordTwo.getId())).now();

        List<FriendResponse> friends = (List<FriendResponse>) sut.getFriends(CURRENT_USER).getItems();
        assertEquals(2, friends.size());

        FriendResponse friendshipOne = friends.get(0);
        assertEquals((long) friendUserRecordOne.getId(), friendshipOne.getFriendId());
        assertEquals(friendUserRecordOne.getDisplayName(), friendshipOne.getDisplayName());

        FriendResponse friendshipTwo = friends.get(1);
        assertEquals((long) friendUserRecordTwo.getId(), friendshipTwo.getFriendId());
        assertEquals(friendUserRecordTwo.getDisplayName(), friendshipTwo.getDisplayName());
    }

    @Test(expected = OAuthRequestException.class)
    public void findFriendsWitNullUser() throws Exception {
        sut.findFriends(new ArrayList<String>(), null);
    }

    @Test
    public void findFriendsDoesNotReturnCurrentFriends() throws Exception {
        ArrayList<String> gPlusIds = new ArrayList<String>() {{
            add("gPlusId2");
            add("gPlusId3");
        }};
        List<FriendResponse> foundFriends =
                (List<FriendResponse>) sut.findFriends(gPlusIds, CURRENT_USER).getItems();
        assertEquals(1, foundFriends.size());
        assertEquals((long) friendUserRecordTwo.getId(), foundFriends.get(0).getFriendId());
    }

    @Test
    public void findFriendsWithSingleGPlusIdReturnsOneResult() throws Exception {
        ArrayList<String> gPlusIds = new ArrayList<String>() {{
            add("gPlusId3");
        }};
        List<FriendResponse> foundFriends =
                (List<FriendResponse>) sut.findFriends(gPlusIds, CURRENT_USER).getItems();
        assertEquals(1, foundFriends.size());
        assertEquals((long) friendUserRecordTwo.getId(), foundFriends.get(0).getFriendId());
    }

    @Test(expected = OAuthRequestException.class)
    public void addFriendWithNullUser() throws Exception {
        sut.addFriend(0L, null);
    }

    @Test(expected = BadRequestException.class)
    public void addFriendWithFriendIdZero() throws Exception {
        sut.addFriend(0L, CURRENT_USER);
    }

    @Test(expected = NotFoundException.class)
    public void addFriendWithWithInvalidId() throws Exception {
        sut.addFriend(123L, CURRENT_USER);
    }

    @Test
    public void addFriendSavesToDataStore() throws Exception {
        sut.addFriend(friendUserRecordTwo.getId(), CURRENT_USER);
        FriendRecord friendRecord = ofy().load().type(FriendRecord.class).ancestor(Key.create(currentUserRecord)).first().now();
        assertNotNull(friendRecord);
        assertEquals(2, friendRecord.getFriends().size());
        assertEquals(friendUserRecordTwo.getId(), friendRecord.getFriends().get(1));
    }

    @Test(expected = OAuthRequestException.class)
    public void getFriendRequestsWithNullUser() throws Exception {
        sut.getFriendRequests(null);
    }

    @Test
    public void getFriendRequestsReturnsEmptyList() throws Exception {
        List<FriendResponse> friendRequests =
                (List<FriendResponse>) sut.getFriendRequests(CURRENT_USER).getItems();

        assertTrue(friendRequests.isEmpty());
    }

    @Test
    public void getFriendRequestsReturnsOneItem() throws Exception {
        List<FriendResponse> friendRequests = (List<FriendResponse>) sut.getFriendRequests(FRIEND_USER_ONE).getItems();
        assertEquals(1, friendRequests.size());
    }

    @Test
    public void getFriendRequestsReturnsCorrectStructure() throws Exception {
        FriendRecord friendRecord = new FriendRecord()
                .setUser(Key.create(friendUserRecordTwo))
                .addFriend(currentUserRecord.getId());
        ofy().save().entity(friendRecord).now();

        List<FriendResponse> friendRequests =
                (List<FriendResponse>) sut.getFriendRequests(CURRENT_USER).getItems();

        FriendResponse friend = friendRequests.get(0);
        assertEquals(friendRecord.getUser().getId(), friend.getFriendId());
    }

    @Test
    public void requestThenUserAddsBack() throws Exception {
        sut.addFriend(friendUserRecordTwo.getId(), CURRENT_USER);
        List<FriendResponse> requests = (List<FriendResponse>) sut.getFriendRequests(FRIEND_USER_TWO).getItems();
        sut.addFriend(requests.get(0).getFriendId(), FRIEND_USER_TWO);
        List<FriendResponse> requestsAfterAdd = (List<FriendResponse>) sut.getFriendRequests(FRIEND_USER_TWO).getItems();
        assertTrue(requestsAfterAdd.isEmpty());
    }

    @Test
    public void requestThenAddBackThenRemove() throws Exception {
        sut.addFriend(friendUserRecordTwo.getId(), CURRENT_USER);
        List<FriendResponse> requests = (List<FriendResponse>) sut.getFriendRequests(FRIEND_USER_TWO).getItems();
        sut.addFriend(requests.get(0).getFriendId(), FRIEND_USER_TWO);
        sut.removeFriend(friendUserRecordTwo.getId(), CURRENT_USER);
        FriendRecord friendRecord = ofy().load().type(FriendRecord.class).ancestor(Key.create(currentUserRecord)).first().now();
        assertEquals(1, friendRecord.getFriends().size());
    }

    @Test(expected = OAuthRequestException.class)
    public void removeFriendWithNullUser() throws Exception {
        sut.removeFriend(0, null);
    }

    @Test(expected = BadRequestException.class)
    public void removeFriendWithZeroUserId() throws Exception {
        sut.removeFriend(0, CURRENT_USER);
    }

    @Test
    public void removeFriendCurrentUser() throws Exception {
        sut.removeFriend(friendUserRecordOne.getId(), CURRENT_USER);
        FriendRecord friendRecord = ofy().load().type(FriendRecord.class).first().now();
        assertTrue(friendRecord.getFriends().isEmpty());
    }

}
