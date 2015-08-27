package technology.mainthread.service.moment.module;

import technology.mainthread.service.moment.data.dao.FriendDAO;
import technology.mainthread.service.moment.data.dao.FriendRecordDAO;
import technology.mainthread.service.moment.data.dao.MomentDAO;
import technology.mainthread.service.moment.data.dao.MomentRecordDAO;
import technology.mainthread.service.moment.data.dao.UserDAO;
import technology.mainthread.service.moment.data.dao.UserRecordDAO;

import static technology.mainthread.service.moment.OfyService.ofy;

public class DaoModule {

    public static UserDAO userDAO() {
        return new UserRecordDAO(ofy());
    }

    public static FriendDAO friendDAO() {
        return new FriendRecordDAO(ofy());
    }

    public static MomentDAO momentDAO() {
        return new MomentRecordDAO(ofy());
    }

}
