package technology.mainthread.apps.moment.ui.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.PackageInfoHelper;
import technology.mainthread.apps.moment.data.UserManager;
import technology.mainthread.apps.moment.data.bus.RxBus;
import technology.mainthread.apps.moment.data.bus.SignInStateEvent;
import technology.mainthread.apps.moment.data.rx.api.RxConfigApi;
import technology.mainthread.apps.moment.ui.fragment.MainContainerFragment;
import technology.mainthread.apps.moment.ui.fragment.SignInFragment;
import technology.mainthread.service.moment.configApi.model.ConfigResponse;
import timber.log.Timber;

import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static technology.mainthread.apps.moment.ui.activity.FriendDiscoveryActivity.getFriendDiscoveryIntent;
import static technology.mainthread.apps.moment.ui.activity.SettingsActivity.getSettingsIntent;

public class MainActivity extends BaseActivity implements SignInStateUpdater {

    private static final int RC_PLAY_SERVICES_ERROR = 10;

    @Inject
    Tracker tracker;
    @Inject
    RxConfigApi configApi;
    @Inject
    PackageInfoHelper packageInfoHelper;
    @Inject
    UserManager userManager;
    @Inject
    RxBus bus;
    @Inject
    GoogleApiAvailability googleApiAvailability;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private boolean updateButtonPressed;
    private boolean fragmentUpdateNeeded;

    public static Intent getMainActivityIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(this).inject(this);

        setContentView(R.layout.activity_drawer);


        int playServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (playServicesAvailable != SUCCESS) {
            googleApiAvailability.getErrorDialog(this, playServicesAvailable, RC_PLAY_SERVICES_ERROR).show();
        }

        checkForNewVersion();

        if (getFragmentManager().findFragmentById(R.id.container) == null) {
            updateSignInState();
        }

        registerRxBus();
    }

    private void registerRxBus() {
        Observable<Object> busObservable = bus.toObservable()
                .compose(bindToLifecycle())
                .compose(applySchedulers());
        compositeSubscription.add(busObservable.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof SignInStateEvent) {
                    SignInStateEvent signInStateEvent = (SignInStateEvent) event;
                    Timber.d("user state changed %s", signInStateEvent.isSignedIn());
                    fragmentUpdateNeeded = !signInStateEvent.isSignedIn();
                }
            }
        }));
    }

    @Override
    public void updateSignInState() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        getToolbar().setTitle(userManager.isSignedIn() ? getString(R.string.app_name) : "");
        if (userManager.isSignedIn()) {
            ft.replace(R.id.container, MainContainerFragment.newInstance()).commit();
        } else {
            ft.replace(R.id.container, SignInFragment.newInstance()).commit();
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // if signed in show add friends menu item
        MenuItem item = menu.findItem(R.id.item_new_friends);
        item.setVisible(userManager.isSignedIn());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_new_friends:
                startActivity(getFriendDiscoveryIntent(this));
                return true;
            case R.id.item_settings:
                startActivity(getSettingsIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getToolbar().setTitle(userManager.isSignedIn() ? getString(R.string.app_name) : "");
        if (fragmentUpdateNeeded) {
            updateSignInState();
            fragmentUpdateNeeded = false;
        }
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }

    private void checkForNewVersion() {
        final Observable<ConfigResponse> configObservable = configApi.config()
                .compose(this.<ConfigResponse>bindToLifecycle())
                .compose(this.<ConfigResponse>applySchedulers());
        compositeSubscription.add(configObservable.subscribe(new Subscriber<ConfigResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Timber.w("Config check failed");
            }

            @Override
            public void onNext(ConfigResponse configResponse) {
                if (configResponse.getMinAndroidVersionCode() > packageInfoHelper.getVersionCode()) {
                    showForceUpdateDialog();
                }
            }
        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PLAY_SERVICES_ERROR) {
            finish(); // finish if play services is not available
        }

        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showForceUpdateDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.force_update_title)
                .content(R.string.force_update_content)
                .positiveText(R.string.force_update_positive)
                .negativeText(R.string.force_update_negative)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        updateButtonPressed = true;
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("force_update")
                                .setLabel("app")
                                .setAction("update")
                                .build());
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_store_app_uri, appPackageName))));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_store_web_uri, appPackageName))));
                        }
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!updateButtonPressed) {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("force_update")
                                    .setLabel("app")
                                    .setAction("cancel")
                                    .build());
                        }
                        finish();
                    }
                })
                .show();
    }
}
