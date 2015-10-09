package org.stepic.droid.view.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.stepic.droid.view.fragments.VideoStepFragment;

public class StepFragmentAdapter extends FragmentStatePagerAdapter {
    public StepFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        VideoStepFragment fragment = new VideoStepFragment();
        Bundle args = new Bundle();
        args.putString("test", position + 1 + "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 40;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Title " + position;
    }
}
