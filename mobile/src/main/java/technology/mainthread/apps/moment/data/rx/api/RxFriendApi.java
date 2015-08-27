package technology.mainthread.apps.moment.data.rx.api;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.GooglePlusApi;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.util.CredentialUtil;
import technology.mainthread.service.moment.friendApi.FriendApi;
import technology.mainthread.service.moment.friendApi.model.CollectionResponseFriendResponse;
import technology.mainthread.service.moment.friendApi.model.FriendResponse;
import timber.log.Timber;

public class RxFriendApi {

    private final GoogleApiClient googleApiClient;
    private final FriendApi.Builder friendApiBuilder;
    private final Tracker tracker;
    private final MomentPreferences preferences;

    private FriendApi friendApi;

    @Inject
    public RxFriendApi(@GooglePlusApi GoogleApiClient googleApiClient,
                       FriendApi.Builder friendApiBuilder,
                       Tracker tracker,
                       MomentPreferences preferences) {
        this.googleApiClient = googleApiClient;
        this.friendApiBuilder = friendApiBuilder;
        this.tracker = tracker;
        this.preferences = preferences;
    }

    // When this class gets created the credential account name may not have been set
    private void checkCredential() {
        friendApi = (FriendApi) CredentialUtil.updateCredential(friendApiBuilder, preferences.getAccountName());
    }

    public Observable<List<FriendResponse>> all() {
        return Observable.create(new Observable.OnSubscribe<List<FriendResponse>>() {
            @Override
            public void call(Subscriber<? super List<FriendResponse>> subscriber) {
                checkCredential();

                try {
                    Timber.d("Getting friends");
                    CollectionResponseFriendResponse response = friendApi.friends().all().execute();
                    if (response != null && response.getItems() != null) {
                        subscriber.onNext(response.getItems());
                    } else {
                        subscriber.onError(null);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Getting friends failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<FriendResponse>> requests() {
        return Observable.create(new Observable.OnSubscribe<List<FriendResponse>>() {
            @Override
            public void call(Subscriber<? super List<FriendResponse>> subscriber) {
                checkCredential();

                try {
                    Timber.d("Getting friend requests");
                    CollectionResponseFriendResponse response = friendApi.friends().requests().execute();
                    if (response != null && response.getItems() != null) {
                        subscriber.onNext(response.getItems());
                    } else {
                        subscriber.onError(null);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Getting friend requests failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<FriendResponse>> search() {
        return Observable.create(new Observable.OnSubscribe<List<FriendResponse>>() {
            @Override
            public void call(Subscriber<? super List<FriendResponse>> subscriber) {
                checkCredential();
                try {
                    Timber.d("Sending search friends request");
                    List<String> googlePlusIds = getCurrentUsersGooglePlusIds();
                    if (!googlePlusIds.isEmpty()) {
                        CollectionResponseFriendResponse response =
                                friendApi.friends().search(googlePlusIds).execute();
                        if (response != null && response.getItems() != null) {
                            subscriber.onNext(response.getItems());
                        } else {
                            subscriber.onError(null);
                        }
                    } else {
                        subscriber.onError(null);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Search friends failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private List<String> getCurrentUsersGooglePlusIds() {
        ArrayList<String> friendGooglePlusIds = new ArrayList<>();
        ConnectionResult connectionResult = googleApiClient
                .blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        if (connectionResult.isSuccess()) {
            People.LoadPeopleResult peopleData = Plus.PeopleApi.loadVisible(googleApiClient, null).await();
            if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                PersonBuffer personBuffer = peopleData.getPersonBuffer();
                try {
                    int count = personBuffer.getCount();
                    for (int i = 0; i < count; i++) {
                        friendGooglePlusIds.add(personBuffer.get(i).getId());
                    }
                } finally {
                    personBuffer.close();
                }
            } else {
                Timber.w("Error requesting visible circles: %s", peopleData.getStatus());
            }
        }
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        return friendGooglePlusIds;
    }

    public Observable<Void> add(final long friendId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                checkCredential();
                trackFriendAction("add");
                try {
                    Timber.d("Adding friend %d", friendId);
                    if (friendId != 0) {
                        friendApi.friends().add(friendId).execute();
                        subscriber.onNext(null);
                    } else {
                        Timber.w("Adding friend %d failed", friendId);
                        subscriber.onError(null);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Adding friends %d failed", friendId);
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Void> remove(final long friendId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                checkCredential();
                trackFriendAction("remove");
                try {
                    Timber.d("removing friend %d", friendId);
                    friendApi.friends().remove(friendId).execute();
                    subscriber.onNext(null);
                } catch (Exception e) {
                    Timber.w(e, "removing friend %d failed", friendId);
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private void trackFriendAction(String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("friend")
                .setAction(action)
                .build());
    }
}
