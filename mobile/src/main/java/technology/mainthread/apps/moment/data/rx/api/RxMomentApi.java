package technology.mainthread.apps.moment.data.rx.api;

import android.graphics.Bitmap;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.util.CredentialUtil;
import technology.mainthread.service.moment.momentApi.MomentApi;
import technology.mainthread.service.moment.momentApi.model.CollectionResponseMomentResponse;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;
import technology.mainthread.service.moment.momentApi.model.UploadUrlResponse;
import timber.log.Timber;

public class RxMomentApi {

    private final OkHttpClient client;
    private final MomentApi.Builder momentApiBuilder;
    private final Tracker tracker;
    private final MomentPreferences preferences;

    private MomentApi momentApi;

    @Inject
    public RxMomentApi(OkHttpClient client, MomentApi.Builder momentApi, Tracker tracker, MomentPreferences preferences) {
        this.client = client;
        this.momentApiBuilder = momentApi;
        this.tracker = tracker;
        this.preferences = preferences;
    }

    // When this class gets created the credential account name may not have been set
    private void checkCredential() {
        momentApi = (MomentApi) CredentialUtil.updateCredential(momentApiBuilder, preferences.getAccountName());
    }

    public Observable<Void> send(final List<Long> recipients, final Bitmap drawing) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                checkCredential();
                trackMomentAction("send");
                try {
                    Timber.d("Getting upload url");
                    UploadUrlResponse uploadUrlResponse = momentApi.moments().uploadUrl().execute();
                    if (uploadUrlResponse == null) {
                        subscriber.onError(new Exception());
                        return;
                    }

                    String blobKey = uploadDrawingBitmap(uploadUrlResponse.getUploadUrl(), drawing);

                    Timber.d("Sending moment");
                    momentApi.moments().send(blobKey, recipients).execute();
                    subscriber.onNext(null);
                } catch (Exception e) {
                    Timber.w(e, "send moment failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private String uploadDrawingBitmap(String uploadUrl, Bitmap drawing) throws IOException, JSONException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        drawing.compress(Bitmap.CompressFormat.PNG, 0, bos);

        RequestBody value = RequestBody.create(MediaType.parse("image/png"), bos.toByteArray());
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("moment", "moment.png", value)
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            JSONObject resultJson = new JSONObject(response.body().string());
            return resultJson.getString("blob-key");
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public Observable<MomentResponse> get(final long id) {
        return Observable.create(new Observable.OnSubscribe<MomentResponse>() {
            @Override
            public void call(Subscriber<? super MomentResponse> subscriber) {
                checkCredential();
                trackMomentAction("get");
                try {
                    Timber.d("Getting moment");
                    MomentResponse response = momentApi.moments().get(id).execute();
                    if (response != null) {
                        subscriber.onNext(response);
                    } else {
                        subscriber.onError(new Exception(String.format("fetching moment %s failed", id)));
                    }
                } catch (Exception e) {
                    Timber.w(e, "get moment failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<MomentResponse>> allSent() {
        return Observable.create(new Observable.OnSubscribe<List<MomentResponse>>() {
            @Override
            public void call(Subscriber<? super List<MomentResponse>> subscriber) {
                checkCredential();
                trackMomentAction("allSent");
                try {
                    Timber.d("Getting all sent moments");
                    CollectionResponseMomentResponse response = momentApi.moments().allSent().execute();
                    if (response != null) {
                        subscriber.onNext(response.getItems());
                    } else {
                        subscriber.onError(new Exception("fetching send moments failed"));
                    }
                } catch (Exception e) {
                    Timber.w(e, "get all sent moments failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<MomentResponse>> allReceived() {
        return Observable.create(new Observable.OnSubscribe<List<MomentResponse>>() {
            @Override
            public void call(Subscriber<? super List<MomentResponse>> subscriber) {
                checkCredential();
                trackMomentAction("allReceived");
                try {
                    Timber.d("Getting all received moments");
                    CollectionResponseMomentResponse response = momentApi.moments().allReceived().execute();
                    if (response != null) {
                        subscriber.onNext(response.getItems());
                    } else {
                        subscriber.onError(new Exception("fetching received moments failed"));
                    }
                } catch (Exception e) {
                    Timber.w(e, "get all received moments failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private void trackMomentAction(String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("moment")
                .setAction(action)
                .build());
    }

}
