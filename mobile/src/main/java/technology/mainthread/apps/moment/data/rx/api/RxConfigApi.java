package technology.mainthread.apps.moment.data.rx.api;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import technology.mainthread.service.moment.configApi.ConfigApi;
import technology.mainthread.service.moment.configApi.model.ConfigResponse;
import timber.log.Timber;

public class RxConfigApi {

    private final ConfigApi configApi;

    @Inject
    public RxConfigApi(ConfigApi configApi) {
        this.configApi = configApi;
    }

    public Observable<ConfigResponse> config() {
        return Observable.create(new Observable.OnSubscribe<ConfigResponse>() {
            @Override
            public void call(Subscriber<? super ConfigResponse> subscriber) {
                try {
                    Timber.d("fetching config");
                    ConfigResponse response = configApi.config().execute();
                    if (response != null) {
                        subscriber.onNext(response);
                    } else {
                        subscriber.onError(new Exception("config fetch failed"));
                    }
                } catch (Exception e) {
                    Timber.w(e, "config fetch failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }
}
