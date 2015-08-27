package technology.mainthread.apps.moment.ui.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.adapter.FriendDiscoveryPagerAdapter;

public class FriendDiscoveryFragment extends BaseFragment {

    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.sliding_tabs)
    TabLayout mTabLayout;

    public static Fragment newInstance() {
        return new FriendDiscoveryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sliding_tab, container, false);
        ButterKnife.bind(this, rootView);

        mViewPager.setAdapter(new FriendDiscoveryPagerAdapter(getChildFragmentManager(), getResources()));
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
        mTabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

}
