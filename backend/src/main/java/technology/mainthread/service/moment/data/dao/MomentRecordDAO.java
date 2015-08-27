package technology.mainthread.service.moment.data.dao;

import com.googlecode.objectify.Objectify;

import java.util.List;

import technology.mainthread.service.moment.data.record.MomentRecord;

public class MomentRecordDAO implements MomentDAO {

    private final Objectify ofy;

    public MomentRecordDAO(Objectify ofy) {
        this.ofy = ofy;
    }

    @Override
    public void save(MomentRecord moment) {
        ofy.save().entity(moment).now();
    }

    @Override
    public MomentRecord get(Long id) {
        return ofy.load().type(MomentRecord.class).id(id).now();
    }

    @Override
    public List<MomentRecord> getAllSent(Long senderId) {
        return ofy.load().type(MomentRecord.class).filter("senderId", senderId).limit(10).list();
    }

    @Override
    public List<MomentRecord> getAllReceived(Long recipient) {
        return ofy.load().type(MomentRecord.class).filter("recipients", recipient).limit(10).list();
    }
}
