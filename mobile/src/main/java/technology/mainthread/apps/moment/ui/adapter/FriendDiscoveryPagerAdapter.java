package technology.mainthread.apps.moment.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v13.app.FragmentPagerAdapter;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.fragment.FriendFinderFragment;
import technology.mainthread.apps.moment.ui.fragment.FriendRequestsFragment;

public class FriendDiscoveryPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    public FriendDiscoveryPagerAdapter(FragmentManager fm, Resources resources) {
        super(fm);
        this.resources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FriendRequestsFragment.newInstance();
            case 1:
                return FriendFinderFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_title_requests);
            case 1:
                return resources.getString(R.string.tab_title_google_plus);
            default:
                return null;
        }
    }
}
