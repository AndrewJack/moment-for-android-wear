package technology.mainthread.apps.moment.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.fragment.FriendDiscoveryFragment;

public class FriendDiscoveryActivity extends BaseActivity {

    public static Intent getFriendDiscoveryIntent(Context context) {
        return new Intent(context, FriendDiscoveryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getFragmentManager().findFragmentById(R.id.container) == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, FriendDiscoveryFragment.newInstance())
                    .commit();
        }
    }
}
