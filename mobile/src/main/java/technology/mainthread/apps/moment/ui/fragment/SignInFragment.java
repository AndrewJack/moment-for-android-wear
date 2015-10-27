package technology.mainthread.apps.moment.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.UserManager;
import technology.mainthread.apps.moment.data.vo.UserDetails;
import timber.log.Timber;

/**
 * SignInFragment based on https://github.com/googleplus/gplus-quickstart-android
 */
public class SignInFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;

    private static final String SAVED_STATE_SIGN_IN_PROGRESS = "SAVED_STATE_SIGN_IN_PROGRESS";
    private static final int RC_SIGN_IN = 0;
    private static final int PERMISSION_GET_ACCOUNTS = 2;

    @Inject
    UserManager userManager;
    @Inject
    GoogleApiAvailability googleApiAvailability;

    @Bind(R.id.btn_sign_in)
    View mSignInButton;
    @Bind(R.id.btn_continue)
    View mContinueButton;
    @Bind(R.id.progress)
    View mProgress;

    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;

    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;

    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;

    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;

    private Subscription mSignInSubscription = Subscriptions.empty();

    public static Fragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState.getInt(SAVED_STATE_SIGN_IN_PROGRESS, 0);
        }
        mGoogleApiClient = buildGoogleApiClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        ButterKnife.bind(this, rootView);
        mContinueButton.setVisibility(View.INVISIBLE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_SIGN_IN_PROGRESS, mSignInProgress);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mSignInSubscription.unsubscribe();
        super.onDestroy();
    }

    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN);

        return builder.build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInButton.setEnabled(false);

        Timber.d("onConnected - Getting user details");
        String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        UserDetails userDetails = UserDetails.builder()
                .accountName(accountName)
                .googlePlusId(currentUser.getId())
                .displayName(currentUser.getDisplayName())
                .firstName(currentUser.getName().getGivenName())
                .lastName(currentUser.getName().getFamilyName())
                .profileImage(currentUser.getImage().getUrl())
                .build();

        signIn(userDetails);

        mSignInProgress = STATE_DEFAULT;
    }

    private void signIn(UserDetails userDetails) {
        mSignInButton.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);

        Observable<Void> signInObservable = userManager.signIn(userDetails)
                .compose(this.<Void>bindToLifecycle())
                .compose(this.<Void>applySchedulers());
        mSignInSubscription = signInObservable.subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                mProgress.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
                mSignInButton.setEnabled(true);
                mContinueButton.setEnabled(false);
            }

            @Override
            public void onNext(Void aVoid) {
                mProgress.setVisibility(View.INVISIBLE);
                mSignInButton.setEnabled(false);
                mContinueButton.setEnabled(true);

                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
                mContinueButton.startAnimation(bottomUp);
                mContinueButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Timber.w("onConnectionFailed - result == %s", result);

        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // An API requested for GoogleApiClient is not available. The device's current
            // configuration might not be supported with the requested API or a required component
            // may not be installed, such as the Android Wear application. You may need to use a
            // second GoogleApiClient to manage the application's optional APIs.
            Timber.w("API Unavailable.");
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                // STATE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == Activity.RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.btn_sign_in)
    void onSignInClicked() {
        if (checkGetAccountsPermission()) {
            doSignIn();
        }
    }

    private void doSignIn() {
        mSignInButton.setEnabled(false);
        mSignInProgress = STATE_SIGN_IN;
        mGoogleApiClient.connect();
    }

    @OnClick(R.id.btn_continue)
    void onContinueClicked() {
        if (userManager.isSignedIn()) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                    .replace(R.id.container, SignInFriendFinderFragment.newInstance())
                    .commit();
        }
    }

    private boolean checkGetAccountsPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_GET_ACCOUNTS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_GET_ACCOUNTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSignIn();
            } else {
                Toast.makeText(getActivity(), R.string.permission_get_accounts, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                getActivity().startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Timber.i(e, "Sign in intent could not be sent");
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            showPlayServicesErrorDialog();
        }
    }

    private void showPlayServicesErrorDialog() {
        Dialog dialog;
        if (googleApiAvailability.isUserResolvableError(mSignInError)) {
            dialog = googleApiAvailability.getErrorDialog(
                    getActivity(),
                    mSignInError,
                    RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Timber.w("Google Play services resolution cancelled");
                            mSignInProgress = STATE_DEFAULT;
                        }
                    });
        } else {
            Timber.w("Google Play services error could not be resolved: %d", mSignInError);
            dialog = new MaterialDialog.Builder(getActivity())
                    .content(R.string.play_services_error)
                    .positiveText(R.string.close)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Timber.w("Google Play services error could not be resolved: %s", mSignInError);
                            mSignInProgress = STATE_DEFAULT;
                            getActivity().finish();
                        }
                    })
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Timber.w("Google Play services error could not be resolved: %s", mSignInError);
                            mSignInProgress = STATE_DEFAULT;
                            getActivity().finish();
                        }
                    })
                    .build();
        }
        dialog.show();
    }
}
