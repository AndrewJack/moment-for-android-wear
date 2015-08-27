package technology.mainthread.apps.moment.data.rx.api;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import technology.mainthread.apps.moment.data.bus.DatabaseChangeEvent;
import technology.mainthread.apps.moment.data.bus.RxBus;
import technology.mainthread.apps.moment.data.api.FriendsSync;
import timber.log.Timber;

@Singleton
public class RxSyncFriends {

    private final FriendsSync friendsSync;
    private final RxBus bus;

    @Inject
    public RxSyncFriends(FriendsSync friendsSync, RxBus bus) {
        this.friendsSync = friendsSync;
        this.bus = bus;
    }

    public Observable<Void> syncFriends() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                Timber.d("Syncing friends");
                try {
                    friendsSync.syncFriends();
                    bus.send(new DatabaseChangeEvent());

                    subscriber.onNext(null);
                } catch (IOException e) {
                    Timber.d(e, "sync friends failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

}
