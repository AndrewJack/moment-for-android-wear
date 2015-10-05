package technology.mainthread.apps.moment;

import technology.mainthread.apps.moment.background.service.ErrorService;
import technology.mainthread.apps.moment.background.service.WearEventsIntentService;
import technology.mainthread.apps.moment.background.service.WearMomentListenerService;
import technology.mainthread.apps.moment.ui.activity.DrawActivity;
import technology.mainthread.apps.moment.ui.activity.SenderActivity;

public interface WearGraph {

    void inject(MomentWearApp momentWearApp);

    // activity
    void inject(SenderActivity senderActivity);

    void inject(DrawActivity drawActivity);

    // service
    void inject(WearMomentListenerService wearMomentListenerService);

    void inject(ErrorService errorService);

    void inject(WearEventsIntentService wearEventsIntentService);

}
