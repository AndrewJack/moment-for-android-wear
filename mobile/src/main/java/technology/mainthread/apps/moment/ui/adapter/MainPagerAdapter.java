package technology.mainthread.apps.moment.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v13.app.FragmentPagerAdapter;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.fragment.FriendsFragment;
import technology.mainthread.apps.moment.ui.fragment.MomentsFragment;

import static technology.mainthread.apps.moment.ui.fragment.MomentsFragment.MomentView;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    public MainPagerAdapter(FragmentManager fm, Resources resources) {
        super(fm);
        this.resources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MomentsFragment.newInstance(MomentView.RECEIVED);
            case 1:
                return MomentsFragment.newInstance(MomentView.SENT);
            case 2:
                return FriendsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_title_received);
            case 1:
                return resources.getString(R.string.tab_title_sent);
            case 2:
                return resources.getString(R.string.title_friends);
            default:
                return null;
        }
    }
}
