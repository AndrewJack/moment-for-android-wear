package technology.mainthread.apps.moment.data;

import com.google.android.gms.wearable.DataMap;

import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.common.data.vo.Friend;

public class ConvertUtil {

    public static Friend toFriend(DataMap map) {
        return Friend.builder()
                .recordId(map.getLong(Constants.KEY_FRIEND_ID))
                .friendId(map.getLong(Constants.KEY_FRIEND_USER_ID))
                .displayName(map.getString(Constants.KEY_FRIEND_DISPLAY_NAME))
                .build();
    }

}
