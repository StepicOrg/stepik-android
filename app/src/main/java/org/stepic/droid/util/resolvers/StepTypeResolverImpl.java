package org.stepic.droid.util.resolvers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.di.AppSingleton;
import org.stepik.android.model.Step;
import org.stepic.droid.ui.fragments.ChoiceStepFragment;
import org.stepic.droid.ui.fragments.CodeStepFragment;
import org.stepic.droid.ui.fragments.FillBlanksFragment;
import org.stepic.droid.ui.fragments.FreeResponseStepFragment;
import org.stepic.droid.ui.fragments.MatchingStepFragment;
import org.stepic.droid.ui.fragments.MathStepFragment;
import org.stepic.droid.ui.fragments.NotSupportedYetStepFragment;
import org.stepic.droid.ui.fragments.NumberStepFragment;
import org.stepic.droid.ui.fragments.PyCharmStepFragment;
import org.stepic.droid.ui.fragments.SortingStepFragment;
import org.stepic.droid.ui.fragments.SqlStepFragment;
import org.stepic.droid.ui.fragments.StringStepFragment;
import org.stepic.droid.ui.fragments.TableChoiceStepFragment;
import org.stepic.droid.ui.fragments.TextStepFragment;
import org.stepic.droid.ui.fragments.VideoStepFragment;
import org.stepic.droid.ui.quiz.ChoiceQuizDelegate;
import org.stepic.droid.ui.quiz.NotSupportedQuizDelegate;
import org.stepic.droid.ui.quiz.NumberQuizDelegate;
import org.stepic.droid.ui.quiz.QuizDelegate;
import org.stepic.droid.ui.quiz.StringQuizDelegate;
import org.stepic.droid.util.AppConstants;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

@AppSingleton
public class StepTypeResolverImpl implements StepTypeResolver {

    private Map<String, Drawable> mapFromTypeToDrawable;
    private Map<String, Drawable> mapFromTypeToDrawableNotViewed;
    private Context context;
    private Drawable peerReviewDrawable;
    private Drawable peerReviewDrawableNotViewed;

    @Inject
    StepTypeResolverImpl(Context context) {

        Timber.d("create step type resolver: %s", toString());

        this.context = context;
        mapFromTypeToDrawable = new HashMap<>();
        mapFromTypeToDrawableNotViewed = new HashMap<>();

        peerReviewDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_peer_review));
        peerReviewDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_peer_review).mutate());

        Drawable simpleQuestionDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_easy_quiz));
        Drawable simpleQuestionDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_easy_quiz).mutate());

        Drawable videoDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_video_pin));
        Drawable videoDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_video_pin).mutate());

        Drawable animationDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_animation));
        Drawable animationDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_animation).mutate());

        Drawable hardQuizDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_hard_quiz));
        Drawable hardQuizDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_hard_quiz).mutate());

        Drawable theoryDrawableNotViewed = getNotViewedDrawable(getDrawable(context, R.drawable.ic_theory));
        Drawable theoryQuizDrawable = getViewedDrawable(getDrawable(context, R.drawable.ic_theory).mutate());

        mapFromTypeToDrawable.put(AppConstants.TYPE_TEXT, theoryQuizDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_VIDEO, videoDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATCHING, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_SORTING, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_MATH, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_FREE_ANSWER, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_TABLE, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_STRING, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHOICE, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_NUMBER, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_DATASET, hardQuizDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_ANIMATION, animationDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_CHEMICAL, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_FILL_BLANKS, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_PUZZLE, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_PYCHARM, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_CODE, hardQuizDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_ADMIN, hardQuizDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_SQL, simpleQuestionDrawable);
        mapFromTypeToDrawable.put(AppConstants.TYPE_LINUX_CODE, simpleQuestionDrawable);


        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TEXT, theoryDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_VIDEO, videoDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATCHING, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_SORTING, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_MATH, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FREE_ANSWER, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_TABLE, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_STRING, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHOICE, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_NUMBER, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_DATASET, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_ANIMATION, animationDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CHEMICAL, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_FILL_BLANKS, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PUZZLE, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_PYCHARM, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_CODE, hardQuizDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_ADMIN, hardQuizDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_SQL, simpleQuestionDrawableNotViewed);
        mapFromTypeToDrawableNotViewed.put(AppConstants.TYPE_LINUX_CODE, simpleQuestionDrawableNotViewed);

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
            if (drawable == null) {
                drawable = mapFromTypeToDrawable.get(AppConstants.TYPE_TEXT);
            }

            return drawable;
        } else {
            Drawable drawable = mapFromTypeToDrawableNotViewed.get(type);
            if (drawable == null) {
                drawable = mapFromTypeToDrawableNotViewed.get(AppConstants.TYPE_TEXT);
            }
            return drawable;
        }
    }

    private Drawable getNotViewedDrawable(Drawable drawable) {
//        return drawable;//it is default implementation
        return getDrawableWithColor(drawable, R.color.unviewed_step);

    }

    @NonNull
    private Drawable getViewedDrawable(Drawable drawable) {
        return getDrawableWithColor(drawable, R.color.viewed_step);
    }

    private Drawable getDrawableWithColor(Drawable drawable, @ColorRes int colorRes) {
        @ColorInt
        int colorToSet = ContextCompat.getColor(context, colorRes);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        drawable.setColorFilter(colorToSet, mMode);
        return drawable;
    }

    @Override
    @NotNull
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
            case AppConstants.TYPE_FILL_BLANKS:
                return new FillBlanksFragment();
            case AppConstants.TYPE_TABLE:
                return TableChoiceStepFragment.Companion.newInstance();
            case AppConstants.TYPE_CODE:
                return CodeStepFragment.Companion.newInstance();
            case AppConstants.TYPE_SQL:
                return SqlStepFragment.Companion.newInstance();
            default:
                return new NotSupportedYetStepFragment();
        }
    }

    @NotNull
    @Override
    public QuizDelegate getQuizDelegate(Step step) {
        QuizDelegate errorDelegate = new NotSupportedQuizDelegate();
        if (step == null
                || step.getBlock() == null
                || step.getBlock().getName() == null
                || step.getBlock().getName().equals(""))
            return errorDelegate;

        String type = step.getBlock().getName();
        switch (type) {
            case AppConstants.TYPE_CHOICE:
                return new ChoiceQuizDelegate();
            case AppConstants.TYPE_STRING:
                return new StringQuizDelegate();
            case AppConstants.TYPE_NUMBER:
                return new NumberQuizDelegate();
            default:
                return errorDelegate;
        }
    }

    private Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableRes);
        } else {
            return context.getResources().getDrawable(drawableRes);
        }
    }

}
