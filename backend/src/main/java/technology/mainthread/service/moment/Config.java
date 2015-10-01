package technology.mainthread.service.moment;

public class Config {

    public static final String OWNER_DOMAIN = "moment.service.mainthread.technology";
    public static final String OWNER_NAME = "MainThreadTechnology";
    public static final String GCM_KEY_MOMENT = "moment";
    public static final String GCM_KEY_FRIEND_ID = "friend/id";
    public static final String GCM_KEY_FRIEND_NAME = "friend/name";
    public static final String GCM_KEY_IS_FRIEND = "friend/isFriend";
    public static final String BLOB_UPLOAD = "/blob/upload";

    public static final int BACKEND_VERSION = 2;
    public static final int MIN_ANDROID_VERSION_CODE = 1010000;

    public static final String WEB_CLIENT_ID = "<YOUR_ID>.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_DEBUG = "<YOUR_ID>.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_RELEASE = "<YOUR_ID>.apps.googleusercontent.com";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
}
