package technology.mainthread.apps.moment.data.db;

import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Moment;

public interface MomentTable {

    void add(Moment moment);

    Moment get(long id);

    Moment getNextInLine();

    List<Moment> getAll();

    void update(Moment moment);

    void delete(int id);

    void deleteAll();

    boolean hasMoments();
}
