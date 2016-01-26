package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.IStepResolver;

import java.util.List;

import javax.inject.Inject;

public class StepFragmentAdapter extends FragmentStatePagerAdapter {

    private final Lesson mLesson;
    private final Unit mUnit;
    private final int mCount;
    private Context mContext;
    @Inject
    IStepResolver mResolver;

    List<Step> mStepList;

    public StepFragmentAdapter(FragmentManager fm, Context context, List<Step> stepList, Lesson mLesson, Unit mUnit, int mCount) {
        super(fm);
        this.mLesson = mLesson;
        this.mUnit = mUnit;
        this.mCount = mCount;
        MainApplication.component().inject(this);
        mContext = context;
        mStepList = stepList;
    }

    @Override
    public Fragment getItem(int position) {
        Step step = mStepList.get(position);
        StepBaseFragment fragment = mResolver.getFragment(step);
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.KEY_STEP_BUNDLE, step);
        args.putSerializable(AppConstants.KEY_LESSON_BUNDLE, mLesson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mStepList.size();
//        if (mCount > 0) {
//            return mCount;
//        } else {
//            return mStepList.size();
//        }
    }

    public Drawable getTabDrawable(int position) {
        Step step = mStepList.get(position);
        return mResolver.getDrawableForType(step.getBlock().getName(), step.is_custom_passed());
    }
}
