package com.chin.bbdb;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;

import com.chin.bbdb.R;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment mFragment;
    private final FragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final Bundle bundle;

    /** Constructor used each time a new tab is created.
      * @param activity  The host Activity, used to instantiate the fragment
      * @param tag  The identifier tag for the fragment
      * @param clz  The fragment's Class, used to instantiate the fragment
      */
    public TabListener(FragmentActivity activity, String tag, Class<T> clz, Bundle bundle) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        this.bundle = bundle;
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // use our own v4 version of FragmentTransaction instead of the one passed in
        android.support.v4.app.FragmentTransaction fft = mActivity.getSupportFragmentManager().beginTransaction();

        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName(), bundle);
            fft.add(R.id.tab_viewgroup, mFragment, mTag);
            fft.commit();
        } else {
            // If it exists, simply attach it in order to show it
            fft.attach(mFragment);
            fft.commit();
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // use our own v4 version of FragmentTransaction instead of the one passed in
        android.support.v4.app.FragmentTransaction fft = mActivity.getSupportFragmentManager().beginTransaction();

        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            fft.detach(mFragment);
            fft.commit();
        }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }
}