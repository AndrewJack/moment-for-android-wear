package technology.mainthread.apps.moment.common;

public class Constants {

    private Constants() {
    }

    // TIMEOUT
    public static final long CONNECTION_TIME_OUT_MS = 30 * 1000;

    // PATHS
    public static final String PATH_NEW_MOMENT = "/moment/new";
    public static final String PATH_MOMENT_NOTIFICATION = "/moment/notification";
    public static final String PATH_FRIENDS_REFRESH_REQUEST = "/moment/friends/refresh/request";
    public static final String PATH_FRIENDS_REFRESH = "/moment/friends/refresh";
    public static final String PATH_FRIEND_ADDED_BACK = "/moment/friends/added_back";
    public static final String PATH_WEAR_ERROR = "/moment/wear/wear_error";

    // KEYS
    public static final String KEY_WEAR_EXCEPTION = "KEY_WEAR_EXCEPTION";
    public static final String KEY_FORCE_UPDATE = "KEY_FORCE_UPDATE";
    public static final String KEY_RECIPIENT = "key_recipient";
    public static final String KEY_MOMENT_ID = "key_moment_id";
    public static final String KEY_SENDER_ID = "key_sender_id";
    public static final String KEY_SENDER_NAME = "key_sender_name";
    public static final String KEY_DRAWING = "key_drawing";
    public static final String KEY_FRIENDS_LIST = "key_friends_list";
    public static final String KEY_FRIEND_ID = "key_friend_id";
    public static final String KEY_FRIEND_USER_ID = "key_friend_user_id";
    public static final String KEY_FRIEND_DISPLAY_NAME = "key_friend_display_name";

    // GCM Keys
    public static final String GCM_KEY_MOMENT = "moment";
    public static final String GCM_KEY_FRIEND_ID = "friend/id";
    public static final String GCM_KEY_FRIEND_NAME = "friend/name";
    public static final String GCM_KEY_IS_FRIEND = "friend/isFriend";

}
