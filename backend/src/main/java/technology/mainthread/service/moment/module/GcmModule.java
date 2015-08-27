package technology.mainthread.service.moment.module;

import com.google.android.gcm.server.Sender;

import technology.mainthread.service.moment.gcm.GcmHelper;
import technology.mainthread.service.moment.gcm.MomentGcmHelper;

import static technology.mainthread.service.moment.module.DaoModule.userDAO;

public class GcmModule {

    private static final String API_KEY = System.getProperty("gcm.api.key");

    private static Sender sender() {
        return new Sender(API_KEY);
    }

    public static GcmHelper gcmHelper() {
        return new MomentGcmHelper(sender(), userDAO());
    }

}
