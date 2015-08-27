package technology.mainthread.apps.moment.ui.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import de.psdev.licensesdialog.LicensesDialog;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.PackageInfoHelper;
import technology.mainthread.apps.moment.data.UserManager;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;

public class SettingsFragment extends RxPreferenceFragment implements Preference.OnPreferenceClickListener {

    @Inject
    UserManager userManager;
    @Inject
    MomentPreferences preferences;
    @Inject
    PackageInfoHelper packageInfoHelper;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private Dialog dialog;

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
        addPreferencesFromResource(R.xml.preferences);

        setUserDetails();
        setOnClickEvents();
        setVersionName();
    }

    @Override
    public void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }

    private void setUserDetails() {
        Preference preference = findPreference(getString(R.string.key_current_user));
        boolean signedIn = userManager.isSignedIn();
        findPreference(getString(R.string.key_logout)).setEnabled(signedIn);
        findPreference(getString(R.string.key_delete)).setEnabled(signedIn);
        if (signedIn) {
            preference.setOnPreferenceClickListener(null);
            preference.setTitle(R.string.title_user_signed_in);
            String summaryString = getString(R.string.summary_user_signed_in, preferences.getUserDisplayName(), preferences.getAccountName());
            preference.setSummary(summaryString);
        } else {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().finish();
                    return true;
                }
            });
            preference.setTitle(R.string.title_user_signed_out);
            preference.setSummary(R.string.summary_user_signed_out);
        }
    }

    private void setOnClickEvents() {
        findPreference(getString(R.string.key_os_licences)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_logout)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_delete)).setOnPreferenceClickListener(this);
    }

    private void setVersionName() {
        Preference versionName = findPreference(getString(R.string.key_version));
        String name = packageInfoHelper.getVersionName();
        if (name != null) {
            versionName.setSummary(name);
        } else {
            versionName.setSummary(getString(R.string.version_name_error));
        }
    }

    private void showLicencesDialog() {
        if (!isRemoving()) {
            new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices).setIncludeOwnLicense(true).build().show();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_os_licences))) {
            showLicencesDialog();
        } else if (preference.getKey().equals(getString(R.string.key_logout))) {
            showLogOutDialog();
        } else if (preference.getKey().equals(getString(R.string.key_delete))) {
            showDeleteAccountDialog();
        }
        return true;
    }

    private void showLogOutDialog() {
        if (!isRemoving()) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_title)
                    .content(R.string.dialog_message_logout)
                    .positiveText(R.string.text_yes)
                    .negativeText(R.string.text_no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            showProgressDialog();
                            Observable<Void> logoutObservable = userManager.logOut()
                                    .compose(SettingsFragment.this.<Void>bindToLifecycle())
                                    .compose(SettingsFragment.this.<Void>applySchedulers());
                            compositeSubscription.add(logoutObservable.subscribe(new Subscriber<Void>() {
                                @Override
                                public void onCompleted() {
                                    setUserDetails();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), R.string.error_sign_out, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                    dismissProgressDialog();
                                }
                            }));
                        }
                    })
                    .show();
        }
    }

    private void showDeleteAccountDialog() {
        if (!isRemoving()) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_title)
                    .content(R.string.dialog_message_delete)
                    .positiveText(R.string.text_yes)
                    .negativeText(R.string.text_no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            showProgressDialog();
                            Observable<Void> deleteAccountObservable = userManager.deleteAccount()
                                    .compose(SettingsFragment.this.<Void>bindToLifecycle())
                                    .compose(SettingsFragment.this.<Void>applySchedulers());
                            compositeSubscription.add(deleteAccountObservable.subscribe(new Subscriber<Void>() {
                                @Override
                                public void onCompleted() {
                                    setUserDetails();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    dismissProgressDialog();
                                    Toast.makeText(getActivity(), R.string.error_sign_out, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                    dismissProgressDialog();
                                }
                            }));
                        }
                    })
                    .show();
        }
    }

    private void showProgressDialog() {
        if (!isRemoving()) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .content(R.string.dialog_logging_out)
                    .progress(true, 0)
                    .show();
        }
    }

    private void dismissProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}