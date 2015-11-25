package org.stepic.droid.util.resolvers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentStepBase;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.fragments.NotSupportedYetStepFragment;
import org.stepic.droid.view.fragments.TextStepFragment;
import org.stepic.droid.view.fragments.VideoStepFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class StepTypeResolver implements IStepResolver {

    private Map<String, Drawable> mapFromTypeToDrawable;
    private Map<String, Drawable> mapFromTypeToDrawableNotViewed;
    private Context mContext;


    public StepTypeResolver(Context context) {

        mContext = context;
        mapFromTypeToDrawable = new HashMap<>();
        mapFromTypeToDrawableNotViewed = new HashMap<>();

        mapFromTypeToDrawable.put(AppConstants.TYPE_TEXT, getDrawable(context, R.drawable.ic_theory1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_VIDEO, getDrawable(context, R.drawable.ic_video1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATCHING, getDrawable(context, R.drawable.ic_matching1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_SORTING, getDrawable(context, R.drawable.ic_sorting1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATCH, getDrawable(context, R.drawable.ic_math1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_FREE_ANSWER, getDrawable(context, R.drawable.ic_free_answer1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_TABLE, getDrawable(context, R.drawable.ic_table1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_STRING, getDrawable(context, R.drawable.ic_string1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHOICE, getDrawable(context, R.drawable.ic_choice1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_NUMBER, getDrawable(context, R.drawable.ic_number1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_DATASET, getDrawable(context, R.drawable.ic_dataset1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_ANIMATION, getDrawable(context, R.drawable.ic_animation1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHEMICAL, getDrawable(context, R.drawable.ic_chemical1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_FILL_BLANKS, getDrawable(context, R.drawable.ic_fill_blanks1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_PUZZLE, getDrawable(context, R.drawable.ic_puzzle1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_PYCHARM, getDrawable(context, R.drawable.ic_pycharm1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CODE, getDrawable(context, R.drawable.ic_code1));


        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TEXT, getDrawable(context, R.drawable.ic_theory));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_VIDEO, getDrawable(context, R.drawable.ic_video));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATCHING, getDrawable(context, R.drawable.ic_matching));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_SORTING, getDrawable(context, R.drawable.ic_sorting));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATCH, getDrawable(context, R.drawable.ic_math));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FREE_ANSWER, getDrawable(context, R.drawable.ic_free_answer));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TABLE, getDrawable(context, R.drawable.ic_table));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_STRING, getDrawable(context, R.drawable.ic_string));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHOICE, getDrawable(context, R.drawable.ic_choice));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_NUMBER, getDrawable(context, R.drawable.ic_number));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_DATASET, getDrawable(context, R.drawable.ic_dataset));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_ANIMATION, getDrawable(context, R.drawable.ic_animation));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHEMICAL, getDrawable(context, R.drawable.ic_chemical));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FILL_BLANKS, getDrawable(context, R.drawable.ic_fill_blanks));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PUZZLE, getDrawable(context, R.drawable.ic_puzzle));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PYCHARM, getDrawable(context, R.drawable.ic_pycharm));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CODE, getDrawable(context, R.drawable.ic_code));

    }

    public Drawable getDrawableForType(String type, boolean viewed) {
        //todo:two maps for viewed and not, if viewed 1st map, not viewed the second?
        if (viewed) {
            Drawable drawable = mapFromTypeToDrawable.get(type);
            if (drawable != null)
                return drawable;
            else
                return mapFromTypeToDrawable.get(AppConstants.TYPE_TEXT);
        }
        else {
            Drawable drawable = mapFromTypeToDrawableNotViewed.get(type);
            if (drawable != null)
                return drawable;
            else
                return mapFromTypeToDrawableNotViewed.get(AppConstants.TYPE_TEXT);
        }
    }

    @Override
    public FragmentStepBase getFragment(Step step) {
        FragmentStepBase errorStep = new NotSupportedYetStepFragment();//todo: error and update?
        if (step == null
                || step.getBlock() == null
                || step.getBlock().getName() == null
                || step.getBlock().getName() == "")
            return errorStep;

        String type = step.getBlock().getName();
        switch (type) {
            case AppConstants.TYPE_VIDEO:
                return new VideoStepFragment();
            case AppConstants.TYPE_TEXT:
                return new TextStepFragment();
            default:
                return new NotSupportedYetStepFragment();
        }
    }

    private Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableRes);
        } else {
            return context.getResources().getDrawable(drawableRes);
        }
    }

    public boolean isViewedStatePost(Step step) {
        if (step == null
                || step.getBlock() == null
                || step.getBlock().getName() == null
                || step.getBlock().getName() == "")
            return false;

        String type = step.getBlock().getName();
        switch (type) {
            case AppConstants.TYPE_VIDEO:
                return true;
            case AppConstants.TYPE_TEXT:
                return true;
            default:
                return false;
        }
    }

}
