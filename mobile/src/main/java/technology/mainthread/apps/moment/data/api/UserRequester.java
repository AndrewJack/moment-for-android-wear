package technology.mainthread.apps.moment.data.api;

import android.content.res.Resources;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import javax.inject.Inject;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.data.vo.UserDetails;
import technology.mainthread.apps.moment.util.CredentialUtil;
import technology.mainthread.service.moment.userApi.UserApi;
import technology.mainthread.service.moment.userApi.model.UserDetailsRequest;
import technology.mainthread.service.moment.userApi.model.UserRegisteredResponse;
import timber.log.Timber;

public class UserRequester {

    private final Resources resources;
    private final MomentPreferences preferences;
    private final GoogleCloudMessaging googleCloudMessaging;
    private final UserApi.Builder userApiBuilder;

    private UserApi userApi;

    @Inject
    public UserRequester(Resources resources,
                         MomentPreferences preferences,
                         GoogleCloudMessaging googleCloudMessaging,
                         UserApi.Builder userApiBuilder) {
        this.resources = resources;
        this.preferences = preferences;
        this.googleCloudMessaging = googleCloudMessaging;
        this.userApiBuilder = userApiBuilder;
        this.userApi = userApiBuilder.build();
    }

    // When this class gets created the credential account name has not been set yet
    private void checkCredential(String accountName) {
        Timber.d("Check credential: %s", accountName);
        userApi = (UserApi) CredentialUtil.updateCredential(userApiBuilder, accountName);
    }

    public long register(UserDetails userDetails) throws Exception {
        checkCredential(userDetails.getAccountName());
        // get registration id from gcm
        String registrationId = googleCloudMessaging.register(resources.getString(R.string.gcm_sender_id));
        preferences.setGcmRegId(registrationId);

        // register with api
        UserDetailsRequest request = new UserDetailsRequest()
                .setGooglePlusId(userDetails.getGooglePlusId())
                .setProfileImageUrl(userDetails.getProfileImageUrl())
                .setDisplayName(userDetails.getDisplayName())
                .setFirstName(userDetails.getFirstName())
                .setLastName(userDetails.getFirstName());

        Timber.d("Registering with api");
        UserRegisteredResponse response = userApi.users().register(registrationId, request).execute();
        return response.getId();
    }

    public void unregister() throws Exception {
        Timber.d("un-registering with api");
        String gcmRegId = preferences.getGcmRegId();
        if (gcmRegId == null) {
            gcmRegId = googleCloudMessaging.register(resources.getString(R.string.gcm_sender_id));
        }
        // unregister from api
        userApi.users().unregister(gcmRegId).execute();
    }

    public void remove() throws Exception {
        Timber.d("removing user form api");
        userApi.users().remove().execute();
    }
}
