package technology.mainthread.apps.moment.data.prefs;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import technology.mainthread.apps.moment.data.vo.UserDetails;

@Singleton
public class MomentPreferences {

    public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
    public static final String USER_ID = "USER_ID";
    public static final String GOOGLE_PLUS_ID = "GOOGLE_PLUS_ID";
    public static final String USER_DISPLAY_NAME = "USER_DISPLAY_NAME";
    public static final String USER_FIRST_NAME = "USER_FIRST_NAME";
    public static final String USER_LAST_NAME = "USER_LAST_NAME";
    public static final String USER_PROFILE_IMAGE = "USER_PROFILE_IMAGE";
    public static final String GCM_REG_ID = "GCM_REG_ID";

    private final SharedPreferences preferences;

    @Inject
    public MomentPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @SuppressLint("CommitPrefEdits")
    public void setUserDetails(UserDetails userDetails) {
        preferences.edit()
                .putString(ACCOUNT_NAME, userDetails.getAccountName())
                .putLong(USER_ID, userDetails.getUserId())
                .putString(GOOGLE_PLUS_ID, userDetails.getGooglePlusId())
                .putString(USER_DISPLAY_NAME, userDetails.getDisplayName())
                .putString(USER_FIRST_NAME, userDetails.getFirstName())
                .putString(USER_LAST_NAME, userDetails.getLastName())
                .putString(USER_PROFILE_IMAGE, userDetails.getProfileImageUrl())
                .commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void removeAllUserDetails() {
        preferences.edit()
                .remove(ACCOUNT_NAME)
                .remove(USER_ID)
                .remove(GOOGLE_PLUS_ID)
                .remove(USER_DISPLAY_NAME)
                .remove(USER_FIRST_NAME)
                .remove(USER_LAST_NAME)
                .remove(USER_PROFILE_IMAGE)
                .remove(GCM_REG_ID)
                .commit();
    }

    public String getAccountName() {
        return preferences.getString(ACCOUNT_NAME, null);
    }

    public long getUserId() {
        return preferences.getLong(USER_ID, 0);
    }

    public String getGooglePlusId() {
        return preferences.getString(GOOGLE_PLUS_ID, null);
    }

    public String getUserDisplayName() {
        return preferences.getString(USER_DISPLAY_NAME, null);
    }

    public String getUserFirstName() {
        return preferences.getString(USER_FIRST_NAME, null);
    }

    public String getUserLastName() {
        return preferences.getString(USER_LAST_NAME, null);
    }

    public String getUserProfileImage() {
        return preferences.getString(USER_PROFILE_IMAGE, null);
    }

    public String getGcmRegId() {
        return preferences.getString(GCM_REG_ID, null);
    }

    public void setGcmRegId(String gcmRegId) {
        preferences.edit().putString(GCM_REG_ID, gcmRegId).apply();
    }

}
