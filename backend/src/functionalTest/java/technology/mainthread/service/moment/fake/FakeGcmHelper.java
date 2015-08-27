package technology.mainthread.service.moment.fake;

import com.google.android.gcm.server.Message;

import java.io.IOException;

import technology.mainthread.service.moment.data.record.UserRecord;
import technology.mainthread.service.moment.gcm.GcmHelper;

public class FakeGcmHelper implements GcmHelper {
    @Override
    public void sendGcmMessage(UserRecord user, Message message) throws IOException {

    }
}
