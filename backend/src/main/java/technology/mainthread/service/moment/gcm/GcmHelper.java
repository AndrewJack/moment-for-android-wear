package technology.mainthread.service.moment.gcm;

import com.google.android.gcm.server.Message;

import java.io.IOException;

import technology.mainthread.service.moment.data.record.UserRecord;

public interface GcmHelper {

    void sendGcmMessage(UserRecord user, Message message) throws IOException;
}
