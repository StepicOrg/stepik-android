package org.stepic.droid.util.resolvers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.ui.fragments.ChoiceStepFragment;
import org.stepic.droid.ui.fragments.FreeResponseStepFragment;
import org.stepic.droid.ui.fragments.MatchingStepFragment;
import org.stepic.droid.ui.fragments.MathStepFragment;
import org.stepic.droid.ui.fragments.NotSupportedYetStepFragment;
import org.stepic.droid.ui.fragments.NumberStepFragment;
import org.stepic.droid.ui.fragments.PyCharmStepFragment;
import org.stepic.droid.ui.fragments.SortingStepFragment;
import org.stepic.droid.ui.fragments.StringStepFragment;
import org.stepic.droid.ui.fragments.TextStepFragment;
import org.stepic.droid.ui.fragments.VideoStepFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class StepTypeResolverImpl implements StepTypeResolver {

    private Map<String, Drawable> mapFromTypeToDrawable;
    private Map<String, Drawable> mapFromTypeToDrawableNotViewed;
    private Context context;
    private Drawable peerReviewDrawable;
    private Drawable peerReviewDrawableNotViewed;


    public StepTypeResolverImpl(Context context) {

        this.context = context;
        mapFromTypeToDrawable = new HashMap<>();
        mapFromTypeToDrawableNotViewed = new HashMap<>();

        peerReviewDrawableNotViewed = getDrawable(context, R.drawable.ic_peer_review);
        peerReviewDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_peer_review).mutate());

        mapFromTypeToDrawable.put(AppConstants.TYPE_TEXT, getDrawable(context, R.drawable.ic_theory1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_VIDEO, getDrawable(context, R.drawable.ic_video_pin1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATCHING, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_SORTING, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATH, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_FREE_ANSWER, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_TABLE, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_STRING, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHOICE, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_NUMBER, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_DATASET, getDrawable(context, R.drawable.ic_hard_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_ANIMATION, getDrawable(context, R.drawable.ic_animation1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHEMICAL, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_FILL_BLANKS, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_PUZZLE, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_PYCHARM, getDrawable(context, R.drawable.ic_easy_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_CODE, getDrawable(context, R.drawable.ic_hard_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_ADMIN, getDrawable(context, R.drawable.ic_hard_quiz1));
        mapFromTypeToDrawable.put(AppConstants.TYPE_SQL, getDrawable(context, R.drawable.ic_easy_quiz1));


        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TEXT, getDrawable(context, R.drawable.ic_theory));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_VIDEO, getDrawable(context, R.drawable.ic_video_pin));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATCHING, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_SORTING, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATH, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FREE_ANSWER, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TABLE, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_STRING, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHOICE, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_NUMBER, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_DATASET, getDrawable(context, R.drawable.ic_hard_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_ANIMATION, getDrawable(context, R.drawable.ic_animation));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHEMICAL, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FILL_BLANKS, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PUZZLE, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PYCHARM, getDrawable(context, R.drawable.ic_easy_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CODE, getDrawable(context, R.drawable.ic_hard_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_ADMIN, getDrawable(context, R.drawable.ic_hard_quiz));
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_SQL, getDrawable(context, R.drawable.ic_easy_quiz));

    }

    public Drawable getDrawableForType(String type, boolean viewed, boolean isPeerReview) {
        //todo:two maps for viewed and not, if viewed 1st map, not viewed the second?
        if (isPeerReview) {
            if (viewed) {
                return peerReviewDrawable;
            } else {
                return peerReviewDrawableNotViewed;
            }
        }

        if (viewed) {
            Drawable drawable = mapFromTypeToDrawable.get(type);
            if (drawable == null)
                drawable = mapFromTypeToDrawable.get(AppConstants.TYPE_TEXT);

            return getViewedDrawable(drawable);
        } else {
            Drawable drawable = mapFromTypeToDrawableNotViewed.get(type);
            if (drawable != null)
                return drawable;
            else
                return mapFromTypeToDrawableNotViewed.get(AppConstants.TYPE_TEXT);
        }
    }

    @NonNull
    private Drawable getViewedDrawable(Drawable drawable) {
        int COLOR2 = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            COLOR2 = context.getColor(R.color.stepic_viewed_steps);
        } else {
            COLOR2 = context.getResources().getColor(R.color.stepic_viewed_steps);
        }
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        drawable.setColorFilter(COLOR2, mMode);
        return drawable;
    }

    @Override
    public StepBaseFragment getFragment(Step step) {
        StepBaseFragment errorStep = new NotSupportedYetStepFragment();//todo: error and update?
        if (step == null
                || step.getBlock() == null
                || step.getBlock().getName() == null
                || step.getBlock().getName().equals(""))
            return errorStep;

        String type = step.getBlock().getName();
        switch (type) {
            case AppConstants.TYPE_VIDEO:
                return new VideoStepFragment();
            case AppConstants.TYPE_TEXT:
                return new TextStepFragment();
            case AppConstants.TYPE_CHOICE:
                return new ChoiceStepFragment();
            case AppConstants.TYPE_FREE_ANSWER:
                return new FreeResponseStepFragment();
            case AppConstants.TYPE_STRING:
                return new StringStepFragment();
            case AppConstants.TYPE_MATH:
                return new MathStepFragment();
            case AppConstants.TYPE_NUMBER:
                return new NumberStepFragment();
            case AppConstants.TYPE_PYCHARM:
                return new PyCharmStepFragment();
            case AppConstants.TYPE_SORTING:
                return new SortingStepFragment();
            case AppConstants.TYPE_MATCHING:
                return new MatchingStepFragment();
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
                || step.getBlock().getName().equals(""))
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
