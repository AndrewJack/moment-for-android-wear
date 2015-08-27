package technology.mainthread.apps.moment.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.data.rx.RxWearNotifier;
import technology.mainthread.apps.moment.data.rx.api.RxMomentApi;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;

public class MomentDrawingFragment extends BaseFragment {

    private static final String PARAM_MOMENT_ID = "param_moment_id";

    @Inject
    RxMomentApi momentApi;
    @Inject
    RxWearNotifier sendToWear;
    @Inject
    MomentPreferences preferences;
    @Inject
    Picasso picasso;

    @Bind(R.id.content)
    View mContent;
    @Bind(R.id.progress)
    View mProgress;
    @Bind(R.id.txt_moment_info)
    TextView mMomentInfoText;
    @Bind(R.id.img_moment_drawing)
    ImageView mDrawingImageView;
    @Bind(R.id.btn_show_on_watch)
    Button mShowOnWatchButton;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Fragment newInstance(long momentId) {
        Fragment fragment = new MomentDrawingFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_MOMENT_ID, momentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_moment_drawing, container, false);
        ButterKnife.bind(this, rootView);

        getMoment();

        return rootView;
    }

    private void getMoment() {
        long momentId = getArguments().getLong(PARAM_MOMENT_ID, 0);
        if (momentId != 0) {
            Observable<MomentResponse> momentObservable = momentApi.get(momentId)
                    .compose(this.<MomentResponse>bindToLifecycle())
                    .compose(this.<MomentResponse>applySchedulers());
            compositeSubscription.add(momentObservable.subscribe(new Observer<MomentResponse>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Can't get moment", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNext(MomentResponse response) {
                    mProgress.setVisibility(View.GONE);
                    mContent.setVisibility(View.VISIBLE);
                    if (response.getSenderId() != preferences.getUserId()) {
                        mMomentInfoText.setText("Sender: " + response.getSenderName());
                        mShowOnWatchButton.setVisibility(View.VISIBLE);
                    }
                    picasso.load(response.getServingUrl()).into(mDrawingImageView);
                }
            }));
        } else {
            Toast.makeText(getActivity(), "Invalid moment id", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_show_on_watch)
    void sendToWear() {
        long momentId = getArguments().getLong(PARAM_MOMENT_ID, 0);
        if (momentId != 0) {
            Observable<Boolean> sendToWearObservable = sendToWear
                    .sendNotificationToWear(momentId)
                    .compose(this.<Boolean>bindToLifecycle())
                    .compose(this.<Boolean>applySchedulers());

            compositeSubscription.add(sendToWearObservable.subscribe(new Observer<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(getActivity(), "Couldn't send to Wear", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNext(Boolean response) {
                    if (response) {
                        Toast.makeText(getActivity(), "Sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Couldn't send to Wear", Toast.LENGTH_SHORT).show();
                    }
                }
            }));
        }
    }

    @Override
    public void onDestroyView() {
        compositeSubscription.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

}
