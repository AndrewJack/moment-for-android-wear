package technology.mainthread.service.moment.endpoint;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobKey;
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
import java.util.Date;
import java.util.List;

import technology.mainthread.service.moment.data.MomentType;
import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.MomentRecord;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.response.MomentResponse;
import technology.mainthread.service.moment.data.response.UploadUrlResponse;
import technology.mainthread.service.moment.fake.FakeGcmHelper;
import technology.mainthread.service.moment.fake.FakeImagesService;
import technology.mainthread.service.moment.module.DaoModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static technology.mainthread.service.moment.OfyService.ofy;

public class MomentEndpointTest {

    private static final User CURRENT_USER = new User("userOne@example.com", "gmail.com", "1");
    private static final User OTHER_USER = new User("otherUser@example.com", "gmail.com", "2");
    private static final String BLOB_KEY = "blobKey";

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private MomentEndpoint sut;
    private Closeable session;
    private final UserRecord currentUserRecord = new UserRecord().setUser(CURRENT_USER).setDisplayName("Current User").setGooglePlusId("gPlusId1");
    private final UserRecord otherUserRecord = new UserRecord().setUser(OTHER_USER).setDisplayName("Other User").setGooglePlusId("gPlusId2");

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        session = ObjectifyService.begin();
        sut = new MomentEndpoint(DaoModule.userDAO(),
                DaoModule.friendDAO(),
                DaoModule.momentDAO(),
                new FakeImagesService(),
                new FakeGcmHelper());
        ofy().save().entity(currentUserRecord).now();
        ofy().save().entity(otherUserRecord).now();
        ofy().save().entity(new FriendRecord()
                .setUser(Key.create(currentUserRecord))
                .addFriend(otherUserRecord.getId())).now();
        ofy().save().entity(new FriendRecord()
                .setUser(Key.create(otherUserRecord))
                .addFriend(currentUserRecord.getId())).now();
    }

    @After
    public void tearDown() throws Exception {
        ofy().clear();
        session.close();
        helper.tearDown();
    }

    @Test(expected = OAuthRequestException.class)
    public void getUploadUrlAuthenticationError() throws Exception {
        sut.getUploadUrl(null);
    }

    @Test
    public void getUploadUrl() throws Exception {
        UploadUrlResponse uploadUrl = sut.getUploadUrl(CURRENT_USER);
        assertNotNull(uploadUrl.getUploadUrl());
    }

    @Test(expected = OAuthRequestException.class)
    public void sendAuthenticationError() throws Exception {
        sut.send(null, null, null);
    }

    @Test(expected = BadRequestException.class)
    public void sendNullParams() throws Exception {
        sut.send(null, null, CURRENT_USER);
    }

    @Test(expected = BadRequestException.class)
    public void sendEmptyRecipients() throws Exception {
        sut.send(new ArrayList<Long>(), BLOB_KEY, CURRENT_USER);
    }

    @Test(expected = BadRequestException.class)
    public void sendNullBlobKey() throws Exception {
        sut.send(new ArrayList<Long>() {{
            add(123L);
        }}, null, CURRENT_USER);
    }

    @Test
    public void sendSavesMoment() throws Exception {
        sut.send(new ArrayList<Long>() {{
            add(otherUserRecord.getId());
        }}, BLOB_KEY, CURRENT_USER);

        List<MomentRecord> list = ofy().load().type(MomentRecord.class).list();
        assertEquals(1, list.size());
        assertEquals(BLOB_KEY, list.get(0).getBlobKey().getKeyString());
    }

    @Test(expected = NotFoundException.class)
    public void sendRecipientNotFound() throws Exception {
        sut.send(new ArrayList<Long>() {{
            add(123L);
        }}, BLOB_KEY, CURRENT_USER);
    }

    @Test(expected = OAuthRequestException.class)
    public void getMomentAuthenticationError() throws Exception {
        sut.get(null, null);
    }

    @Test(expected = BadRequestException.class)
    public void getMomentIdIsNull() throws Exception {
        sut.get(null, CURRENT_USER);
    }

    @Test(expected = NotFoundException.class)
    public void getMomentNotFound() throws Exception {
        sut.get(123L, CURRENT_USER);
    }

    @Test(expected = UnauthorizedException.class)
    public void getMomentUnauthorizedException() throws Exception {
        MomentRecord moment = new MomentRecord()
                .setBlobKey(new BlobKey(BLOB_KEY))
                .setSenderId(123L)
                .setType(MomentType.DRAWING);
        ofy().save().entity(moment).now();
        sut.get(moment.getId(), CURRENT_USER);
    }

    @Test
    public void getMoment() throws Exception {
        MomentRecord moment = new MomentRecord()
                .setBlobKey(new BlobKey(BLOB_KEY))
                .setSenderId(otherUserRecord.getId())
                .setSenderName(otherUserRecord.getDisplayName())
                .setRecipients(new ArrayList<Long>() {{
                    add(currentUserRecord.getId());
                }})
                .setType(MomentType.DRAWING);
        ofy().save().entity(moment).now();
        MomentResponse response = sut.get(moment.getId(), CURRENT_USER);
        assertNotNull(response);
        assertEquals(moment.getSenderId(), response.getSenderId());
        assertEquals(moment.getSenderName(), response.getSenderName());
        assertEquals(moment.getType(), response.getMomentType());
        assertEquals(FakeImagesService.SERVING_URL, response.getServingUrl());
    }

    @Test
    public void getMomentAsTheSender() throws Exception {
        MomentRecord moment = new MomentRecord()
                .setBlobKey(new BlobKey(BLOB_KEY))
                .setSenderId(currentUserRecord.getId())
                .setSenderName(currentUserRecord.getDisplayName())
                .setRecipients(new ArrayList<Long>() {{
                    add(otherUserRecord.getId());
                }})
                .setType(MomentType.DRAWING);
        ofy().save().entity(moment).now();
        MomentResponse response = sut.get(moment.getId(), CURRENT_USER);
        assertNotNull(response);
        assertEquals(moment.getSenderId(), response.getSenderId());
        assertEquals(moment.getSenderName(), response.getSenderName());
        assertEquals(moment.getType(), response.getMomentType());
        assertEquals(FakeImagesService.SERVING_URL, response.getServingUrl());
    }

    @Test(expected = OAuthRequestException.class)
    public void getAllSentAuthenticationError() throws Exception {
        sut.allSent(null);
    }

    @Test
    public void getAllSentReturnsNothing() throws Exception {
        List<MomentResponse> items = (List<MomentResponse>) sut.allSent(CURRENT_USER).getItems();
        assertTrue(items.isEmpty());
    }

    @Test
    public void getAllSent() throws Exception {
        MomentRecord moment = new MomentRecord()
                .setBlobKey(new BlobKey(BLOB_KEY))
                .setSenderId(currentUserRecord.getId())
                .setRecipients(new ArrayList<Long>() {{
                    add(otherUserRecord.getId());
                }})
                .setType(MomentType.DRAWING)
                .setCreated(new Date());
        ofy().save().entity(moment).now();

        List<MomentResponse> items = (List<MomentResponse>) sut.allSent(CURRENT_USER).getItems();
        assertEquals(1, items.size());
        assertNotNull(items.get(0));
        assertEquals(moment.getType(), items.get(0).getMomentType());
        assertEquals(FakeImagesService.SERVING_URL, items.get(0).getServingUrl());
    }

    @Test(expected = OAuthRequestException.class)
    public void getAllReceivedAuthenticationError() throws Exception {
        sut.allReceived(null);
    }

    @Test
    public void getAllReceivedReturnsNothing() throws Exception {
        List<MomentResponse> items = (List<MomentResponse>) sut.allReceived(CURRENT_USER).getItems();
        assertTrue(items.isEmpty());
    }

    @Test
    public void getAllReceived() throws Exception {
        MomentRecord moment = new MomentRecord()
                .setBlobKey(new BlobKey(BLOB_KEY))
                .setSenderId(otherUserRecord.getId())
                .setSenderName(otherUserRecord.getDisplayName())
                .setRecipients(new ArrayList<Long>() {{
                    add(currentUserRecord.getId());
                }})
                .setType(MomentType.DRAWING)
                .setCreated(new Date());
        ofy().save().entity(moment).now();

        List<MomentResponse> items = (List<MomentResponse>) sut.allReceived(CURRENT_USER).getItems();
        assertEquals(1, items.size());
        assertNotNull(items.get(0));
        assertEquals(moment.getSenderId(), items.get(0).getSenderId());
        assertEquals(moment.getSenderName(), items.get(0).getSenderName());
        assertEquals(moment.getType(), items.get(0).getMomentType());
        assertEquals(FakeImagesService.SERVING_URL, items.get(0).getServingUrl());
    }

}
