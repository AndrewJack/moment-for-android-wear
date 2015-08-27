package technology.mainthread.apps.moment.data;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

/**
 * Extracted into util so we don't have to include stetho in release builds
 */
public class StethoUtil {

    public static void setupStetho(Context context) {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                .build());
    }

    public static void addStethoInterceptor(OkHttpClient client) {
        client.networkInterceptors().add(new StethoInterceptor());
    }

}
