package technology.mainthread.apps.moment.data.prefs;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WearMomentPreferences {

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    private static final String DRAWING_COUNT = "DRAWING_COUNT";
    private static final String LAST_SYNCED_FRIENDS = "LAST_SYNCED_FRIENDS";

    private final SharedPreferences preferences;

    @Inject
    public WearMomentPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean shouldShowHelp() {
        return preferences.getInt(DRAWING_COUNT, 0) < 3;
    }

    public void incrementDrawingCount() {
        int newCount = preferences.getInt(DRAWING_COUNT, 0) + 1;
        preferences.edit().putInt(DRAWING_COUNT, newCount).apply();
    }

    public boolean shouldRequestRefresh() {
        long lastSyncedMillis = preferences.getLong(LAST_SYNCED_FRIENDS, 0L);
        return (System.currentTimeMillis() - lastSyncedMillis) > DAY_IN_MILLIS;
    }

    @SuppressLint("CommitPrefEdits")
    public void friendsRefreshedSuccessfully() {
        preferences.edit().putLong(LAST_SYNCED_FRIENDS, System.currentTimeMillis()).commit();
    }
}
