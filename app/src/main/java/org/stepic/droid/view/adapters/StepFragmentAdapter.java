package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.widget.ImageView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.IStepResolver;
import org.stepic.droid.view.fragments.VideoStepFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StepFragmentAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    @Inject
    IStepResolver mResolver;

    List<Step> mStepList;

    public StepFragmentAdapter(FragmentManager fm, Context context, List<Step> stepList) {
        super(fm);
        MainApplication.component().inject(this);
        mContext = context;
        mStepList = stepList;
    }

    @Override
    public Fragment getItem(int position) {
        Step step = mStepList.get(position);

        VideoStepFragment fragment = new VideoStepFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.KEY_STEP_BUNDLE, step);
        args.putString("test", position + 1 + "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mStepList.size();
    }

    public Drawable getTabDrawable(int position) {
        Step step = mStepList.get(position);
        return mResolver.getDrawableForType(step.getBlock().getName(), false);
    }

    public static class TabHolder {
        //now this class is useless for performance, but in future we will tune this.

        @Bind(R.id.icon_for_step)
        ImageView stepIcon;

        public TabHolder(View v) {
            ButterKnife.bind(this, v);
        }

    }
}
