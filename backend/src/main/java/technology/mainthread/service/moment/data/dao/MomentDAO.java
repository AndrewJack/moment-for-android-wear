package technology.mainthread.service.moment.data.dao;

import java.util.List;

import technology.mainthread.service.moment.data.record.MomentRecord;

public interface MomentDAO {

    /**
     * Save moment record
     */
    void save(MomentRecord moment);

    /**
     * get friend record
     */
    MomentRecord get(Long id);

    /**
     * Get all the sender's sent moments
     *
     * @param senderId the sender's id
     * @return List of all sent moment records
     */
    List<MomentRecord> getAllSent(Long senderId);

    /**
     * Get all the recipient's received moments
     *
     * @param recipient the recipient's id
     * @return List of all received moment records
     */
    List<MomentRecord> getAllReceived(Long recipient);



}
