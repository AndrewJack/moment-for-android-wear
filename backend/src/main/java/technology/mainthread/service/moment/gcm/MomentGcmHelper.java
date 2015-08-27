package technology.mainthread.service.moment.gcm;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.ListIterator;

import technology.mainthread.service.moment.data.dao.UserDAO;
import technology.mainthread.service.moment.data.record.UserRecord;

public class MomentGcmHelper implements GcmHelper {

    private final Sender sender;
    private final UserDAO userDAO;

    public MomentGcmHelper(Sender sender, UserDAO userDAO) {
        this.sender = sender;
        this.userDAO = userDAO;
    }

    @Override
    public void sendGcmMessage(UserRecord user, Message message) throws IOException {
        boolean updateUser = false;
        // Use iterator instead of foreach to prevent ConcurrentModificationException
        ListIterator<String> iterator = user.getDevices().listIterator();
        while (iterator.hasNext()) {
            String device = iterator.next();
            Result result = sender.send(message, device, 1);
            if (result.getMessageId() != null) {
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update it
                    iterator.remove();
                    iterator.add(canonicalRegId);
                    updateUser = true;
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    // if the device is no longer registered with Gcm, remove it
                    iterator.remove();
                    updateUser = true;
                }
            }
        }
        if (updateUser) {
            userDAO.save(user);
        }
    }
}
