package technology.mainthread.apps.moment.data;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

/**
 * Empty util so we don't have to include stetho in release builds
 */
public class StethoUtil {

    public static void setupStetho(Context context) {}

    public static void addStethoInterceptor(OkHttpClient client) {}

}
