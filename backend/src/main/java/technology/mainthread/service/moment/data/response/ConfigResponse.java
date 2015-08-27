package technology.mainthread.service.moment.data.response;

import technology.mainthread.service.moment.Config;

public class ConfigResponse {

    private static final int backendVersion = Config.BACKEND_VERSION;

    private static final int minAndroidVersionCode = Config.MIN_ANDROID_VERSION_CODE;

    public int getBackendVersion() {
        return backendVersion;
    }

    public int getMinAndroidVersionCode() {
        return minAndroidVersionCode;
    }
}
