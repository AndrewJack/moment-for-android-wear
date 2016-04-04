package technology.mainthread.apps.moment;

import technology.mainthread.apps.moment.background.receiver.AccountChangedReceiver;
import technology.mainthread.apps.moment.background.receiver.ConnectivityBroadcastReceiver;
import technology.mainthread.apps.moment.background.service.GcmListenerService;
import technology.mainthread.apps.moment.background.service.SenderService;
import technology.mainthread.apps.moment.background.service.MobileWearListenerService;
import technology.mainthread.apps.moment.background.service.UpdateFriendsIntentService;
import technology.mainthread.apps.moment.background.sync.MomentSyncAdapter;
import technology.mainthread.apps.moment.ui.activity.MainActivity;
import technology.mainthread.apps.moment.ui.fragment.BaseFragment;
import technology.mainthread.apps.moment.ui.fragment.FriendFinderFragment;
import technology.mainthread.apps.moment.ui.fragment.FriendRequestsFragment;
import technology.mainthread.apps.moment.ui.fragment.FriendsFragment;
import technology.mainthread.apps.moment.ui.fragment.MomentDrawingFragment;
import technology.mainthread.apps.moment.ui.fragment.MomentsFragment;
import technology.mainthread.apps.moment.ui.fragment.SettingsFragment;
import technology.mainthread.apps.moment.ui.fragment.SignInFragment;
import technology.mainthread.apps.moment.ui.fragment.SignInFriendFinderFragment;
import technology.mainthread.apps.moment.ui.fragment.WearablesFragment;

public interface MomentGraph {

    void inject(MomentApp momentApp);

    // Activity
    void inject(MainActivity mainActivity);

    // Fragments
    void inject(BaseFragment baseFragment);

    void inject(SignInFragment signInFragment);

    void inject(SignInFriendFinderFragment signInFriendFinderFragment);

    void inject(WearablesFragment wearablesFragment);

    void inject(MomentsFragment momentsFragment);

    void inject(FriendsFragment friendsFragment);

    void inject(FriendRequestsFragment friendRequestsFragment);

    void inject(FriendFinderFragment friendFinderFragment);

    void inject(MomentDrawingFragment momentDrawingFragment);

    void inject(SettingsFragment settingsFragment);

    // Service
    void inject(GcmListenerService gcmListenerService);

    void inject(MobileWearListenerService mobileWearListenerService);

    void inject(MomentSyncAdapter momentSyncAdapter);

    void inject(SenderService senderService);

    void inject(UpdateFriendsIntentService updateFriendsIntentService);

    // broadcast
    void inject(ConnectivityBroadcastReceiver connectivityBroadcastReceiver);

    void inject(AccountChangedReceiver accountChangedReceiver);

}
