package technology.mainthread.apps.moment.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.fragment.MomentDrawingFragment;

public class MomentActivity extends BaseActivity {

    private static final String PARAM_MOMENT_ID = "param_moment_id";

    public static Intent getMomentActivityIntent(Context context, long momentId) {
        Intent intent = new Intent(context, MomentActivity.class);
        intent.putExtra(PARAM_MOMENT_ID, momentId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getFragmentManager().findFragmentById(R.id.container) == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, MomentDrawingFragment.newInstance(getIntent().getLongExtra(PARAM_MOMENT_ID, 0)))
                    .commit();
        }
    }

}
