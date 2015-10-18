package technology.mainthread.apps.moment.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.data.api.GooglePlusSignInRequester;
import technology.mainthread.apps.moment.data.api.UserRequester;
import technology.mainthread.apps.moment.data.bus.RxBus;
import technology.mainthread.apps.moment.data.bus.SignInStateEvent;
import technology.mainthread.apps.moment.data.db.MomentTable;
import technology.mainthread.apps.moment.data.db.SyncFriends;
import technology.mainthread.apps.moment.data.db.SyncMoment;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.data.vo.UserDetails;
import timber.log.Timber;

@Singleton
public class UserManager {

    private final String accountType;
    private final String contentAuthority;
    private final int syncFrequencySeconds;
    private final MomentPreferences preferences;
    private final AccountManager accountManager;
    private final GooglePlusSignInRequester googlePlusSignInRequester;
    private final UserRequester userRequester;
    private final Tracker tracker;
    private final FriendsTable friendsTable;
    private final MomentTable momentTable;
    private final RxBus bus;

    private Account currentAccount;
    private boolean isDeletingCredentials;

    @Inject
    public UserManager(Resources resources,
                       MomentPreferences preferences,
                       AccountManager accountManager,
                       GooglePlusSignInRequester googlePlusSignInRequester,
                       UserRequester userRequester,
                       RxBus bus,
                       Tracker tracker,
                       @SyncFriends FriendsTable friendsTable,
                       @SyncMoment MomentTable momentTable) {
        this.bus = bus;
        this.accountType = resources.getString(R.string.account_type);
        this.contentAuthority = resources.getString(R.string.content_authority);
        this.syncFrequencySeconds = resources.getInteger(R.integer.sync_frequency_hours) * 60 * 60;
        this.preferences = preferences;
        this.accountManager = accountManager;
        this.googlePlusSignInRequester = googlePlusSignInRequester;
        this.userRequester = userRequester;
        this.tracker = tracker;
        this.friendsTable = friendsTable;
        this.momentTable = momentTable;
    }

    public boolean isSignedIn() {
        return !isDeletingCredentials && getAccount() != null;
    }

    public Observable<Void> signIn(final UserDetails userDetails) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                Timber.d("Signing in");
                try {
                    long userId = userRequester.register(userDetails);
                    // Add user id to the mix
                    if (userId != 0) {
                        addAccount(userDetails.getAccountName());
                        UserDetails userDetailsWithAccountName = UserDetails.builder(userDetails)
                                .userId(userId).build();
                        preferences.setUserDetails(userDetailsWithAccountName);
                        bus.send(new SignInStateEvent(true));
                        tracker.set("&uid", Long.toString(userId));
                        trackUserAction("sign in");
                        subscriber.onNext(null);
                    } else {
                        subscriber.onError(new Exception("Sign in failed"));
                    }
                } catch (Exception e) {
                    Timber.e(e, "Sign in failed");
                    subscriber.onError(e);
                }

                subscriber.onCompleted();
            }
        });
    }

    public Observable<Void> logOut() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                Timber.d("Logging out");
                trackUserAction("log out");
                removeAccount();

                googlePlusSignInRequester.logOut();
                try {
                    userRequester.unregister();
                    bus.send(new SignInStateEvent(false));
                    subscriber.onNext(null);
                } catch (Exception e) {
                    Timber.e(e, "log out failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Void> deleteAccount() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                Timber.d("Deleting account");
                trackUserAction("delete");
                removeAccount();

                googlePlusSignInRequester.revoke();
                try {
                    userRequester.remove();
                    bus.send(new SignInStateEvent(false));
                    subscriber.onNext(null);
                } catch (Exception e) {
                    Timber.e(e, "Delete account failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public void requestSync() {
        Timber.d("Requesting sync");
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getAccount(), contentAuthority, settingsBundle);
    }

    private void addAccount(String accountName) {
        Timber.d("Adding account");
        Account account = new Account(accountName, accountType);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            Timber.d("Account added");
            ContentResolver.setIsSyncable(account, contentAuthority, 1);
            ContentResolver.setSyncAutomatically(account, contentAuthority, true);
            ContentResolver.addPeriodicSync(account, contentAuthority,
                    new Bundle(), syncFrequencySeconds);
        }
    }

    private Account getAccount() {
        if (currentAccount == null) {
            Account[] accounts = accountManager.getAccountsByType(accountType);
            if (accounts.length > 0) {
                currentAccount = accounts[0];
            }
        }
        return currentAccount;
    }

    /**
     * Removes current account
     */
    private void removeAccount() {
        Timber.d("Removing account");
        Account account = getAccount();
        if (account != null) {
            isDeletingCredentials = true;
            accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    isDeletingCredentials = false;
                    currentAccount = null;
                }
            }, null);
        }
        preferences.removeAllUserDetails();
        friendsTable.deleteAll();
        momentTable.deleteAll();
    }

    private void trackUserAction(String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("user")
                .setAction(action)
                .build());
    }

}
