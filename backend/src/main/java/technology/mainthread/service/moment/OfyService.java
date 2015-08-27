package technology.mainthread.service.moment;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import technology.mainthread.service.moment.data.record.FriendRecord;
import technology.mainthread.service.moment.data.record.MomentRecord;
import technology.mainthread.service.moment.data.record.UserRecord;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public class OfyService {

    static {
        ObjectifyService.register(UserRecord.class);
        ObjectifyService.register(FriendRecord.class);
        ObjectifyService.register(MomentRecord.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
