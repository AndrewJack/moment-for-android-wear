package technology.mainthread.service.moment.endpoint;

import com.google.android.gcm.server.Message;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import technology.mainthread.service.moment.Config;
import technology.mainthread.service.moment.Messages;
import technology.mainthread.service.moment.data.dao.FriendDAO;
import technology.mainthread.service.moment.data.dao.MomentDAO;
import technology.mainthread.service.moment.data.dao.UserDAO;
import technology.mainthread.service.moment.data.record.MomentRecord;
import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.data.response.MomentResponse;
import technology.mainthread.service.moment.data.response.UploadUrlResponse;
import technology.mainthread.service.moment.gcm.GcmHelper;
import technology.mainthread.service.moment.module.DaoModule;
import technology.mainthread.service.moment.module.GcmModule;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

@Api(
        name = "momentApi",
        description = "Used to send and get moments of various types (Drawing, sound, etc)",
        resource = "MomentRecord",
        version = "v1",
        scopes = {Config.EMAIL_SCOPE},
        clientIds = {Config.WEB_CLIENT_ID, Config.ANDROID_CLIENT_ID_DEBUG, Config.ANDROID_CLIENT_ID_RELEASE},
        audiences = {Config.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = Config.OWNER_DOMAIN,
                ownerName = Config.OWNER_NAME
        )
)
public class MomentEndpoint {

    private final UserDAO userDAO;
    private final FriendDAO friendDAO;
    private final MomentDAO momentDAO;
    private final ImagesService imagesService;
    private final GcmHelper gcmHelper;

    public MomentEndpoint() {
        this(
                DaoModule.userDAO(),
                DaoModule.friendDAO(),
                DaoModule.momentDAO(),
                ImagesServiceFactory.getImagesService(),
                GcmModule.gcmHelper()
        );
    }

    public MomentEndpoint(UserDAO userDAO,
                          FriendDAO friendDAO,
                          MomentDAO momentDAO,
                          ImagesService imagesService,
                          GcmHelper gcmHelper) {
        this.userDAO = userDAO;
        this.friendDAO = friendDAO;
        this.momentDAO = momentDAO;
        this.imagesService = imagesService;
        this.gcmHelper = gcmHelper;
    }

    /**
     * Gets the blob upload url
     *
     * @param user The Google Authenticated User
     */
    @ApiMethod(
            name = "moments.uploadUrl",
            path = "moments/uploadUrl",
            httpMethod = HttpMethod.GET
    )
    public UploadUrlResponse getUploadUrl(User user) throws OAuthRequestException {
        if (userDAO.notRegistered(user)) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        return new UploadUrlResponse().setUploadUrl(blobstoreService.createUploadUrl(Config.BLOB_UPLOAD));
    }

    /**
     * Sends a Moment to recipients
     *
     * @param recipients The users for the moment to be sent to
     * @param blobKey    The key of the blob uploaded using getUploadUrl(..)
     * @param user       The Google Authenticated User
     */
    @ApiMethod(
            name = "moments.send",
            path = "moments/send",
            httpMethod = HttpMethod.POST
    )
    public void send(@Named("recipients") List<Long> recipients, @Named("blobKey") String blobKey, User user)
            throws OAuthRequestException, BadRequestException, IOException, NotFoundException {

        // Check user is logged in
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        if (recipients == null || recipients.isEmpty() || blobKey == null) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        // save the moment - ha
        MomentRecord moment = new MomentRecord()
                .setSenderId(currentUser.getId())
                .setSenderName(currentUser.getDisplayName())
                .setRecipients(recipients)
                .setBlobKey(new BlobKey(blobKey))
                .setCreated(new Date());

        momentDAO.save(moment);

        Message message = new Message.Builder().addData(Config.GCM_KEY_MOMENT, moment.getId().toString()).build();
        boolean userFound = false;
        for (long recipient : recipients) {
            UserRecord recipientUser = userDAO.getUserRecord(recipient);
            // check user exists and the current user and recipient are friends
            if (recipientUser != null && friendDAO.isFriend(currentUser.getId(), recipientUser.getId())) {
                userFound = true;
                gcmHelper.sendGcmMessage(recipientUser, message);
            }
        }

        if (!userFound) {
            throw new NotFoundException(Messages.ERROR_RECIPIENTS_NOT_FOUND);
        }
    }

    /**
     * Gets the moment for the related momentId
     *
     * @param id   The id of the moment to be returned.
     * @param user The Google Authenticated User
     * @return A MomentResponse
     */
    @ApiMethod(
            name = "moments.get",
            path = "moments/{id}",
            httpMethod = HttpMethod.GET
    )
    public MomentResponse get(@Named("id") Long id, User user)
            throws OAuthRequestException, BadRequestException, NotFoundException, UnauthorizedException {

        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        // check params
        if (id == null) {
            throw new BadRequestException(Messages.ERROR_INVALID_PARAMETERS);
        }

        // find moment
        MomentRecord moment = momentDAO.get(id);
        if (moment == null) {
            throw new NotFoundException(Messages.ERROR_MOMENT_NOT_FOUND);
        }

        if (moment.getSenderId() != currentUser.getId()) {
            // check recipients if they aren't the sender
            List<Long> recipients = moment.getRecipients();
            if (recipients == null || recipients.isEmpty() || !recipients.contains(currentUser.getId())) {
                throw new UnauthorizedException(Messages.ERROR_NOT_AUTH_TO_VIEW);
            }
        }

        // Use ImageService to serve images according to google it is
        // "more efficient and potentially less-expensive method"
        ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withBlobKey(moment.getBlobKey());
        String servingUrl = imagesService.getServingUrl(servingOptions);
        return new MomentResponse()
                .setMomentId(moment.getId())
                .setSenderId(moment.getSenderId())
                .setSenderName(moment.getSenderName())
                .setMomentType(moment.getType())
                .setServingUrl(servingUrl);
    }

    /**
     * This method gets the all Moments sent by the current user
     *
     * @param user The Google Authenticated User
     * @return A list of Moments sent by the current user
     */
    @ApiMethod(
            name = "moments.allSent",
            path = "moments/allSent",
            httpMethod = HttpMethod.GET
    )
    public CollectionResponse<MomentResponse> allSent(User user)
            throws OAuthRequestException {
        // Check user is logged in
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        List<MomentRecord> allSent = momentDAO.getAllSent(currentUser.getId());
        List<MomentResponse> moments = new ArrayList<>(allSent.size());

        for (MomentRecord record : allSent) {
            ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withBlobKey(record.getBlobKey());
            String servingUrl = imagesService.getServingUrl(servingOptions);
            List<String> recipientNames = userDAO.getUserNames(record.getRecipients());
            moments.add(new MomentResponse()
                    .setMomentId(record.getId())
                    .setRecipientNames(recipientNames)
                    .setMomentType(record.getType())
                    .setServingUrl(servingUrl));
        }

        return CollectionResponse.<MomentResponse>builder().setItems(moments).build();
    }

    /**
     * This method gets the all the Moments received by the current user
     *
     * @param user The Google Authenticated User
     * @return A list of Moments received by the current user
     */
    @ApiMethod(
            name = "moments.allReceived",
            path = "moments/allReceived",
            httpMethod = HttpMethod.GET
    )
    public CollectionResponse<MomentResponse> allReceived(User user)
            throws OAuthRequestException {
        // Check user is logged in
        UserRecord currentUser = userDAO.getUserRecord(user);
        if (currentUser == null) {
            throw new OAuthRequestException(Messages.ERROR_AUTH);
        }

        List<MomentRecord> allReceived = momentDAO.getAllReceived(currentUser.getId());
        List<MomentResponse> moments = new ArrayList<>(allReceived.size());

        for (MomentRecord record : allReceived) {
            ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withBlobKey(record.getBlobKey());
            String servingUrl = imagesService.getServingUrl(servingOptions);
            moments.add(new MomentResponse()
                    .setMomentId(record.getId())
                    .setSenderId(record.getSenderId())
                    .setSenderName(record.getSenderName())
                    .setMomentType(record.getType())
                    .setServingUrl(servingUrl));
        }

        return CollectionResponse.<MomentResponse>builder().setItems(moments).build();
    }
}