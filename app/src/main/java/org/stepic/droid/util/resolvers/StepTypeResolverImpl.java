package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.ui.quiz.ChoiceQuizDelegate;
import org.stepic.droid.ui.quiz.NotSupportedQuizDelegate;
import org.stepic.droid.ui.quiz.NumberQuizDelegate;
import org.stepic.droid.ui.quiz.QuizDelegate;
import org.stepic.droid.ui.quiz.StringQuizDelegate;
import org.stepic.droid.util.AppConstants;
import org.stepik.android.model.Step;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import kotlin.Pair;
import timber.log.Timber;

@AppSingleton
public class StepTypeResolverImpl implements StepTypeResolver {

    private final Map<String, Pair<Integer, Integer>> mapFromTypeToDrawableRes;
    private final Pair<Integer, Integer> peerReviewDrawableRes;

    @Inject
    StepTypeResolverImpl() {
        Timber.d("create step type resolver: %s", toString());
        mapFromTypeToDrawableRes = new HashMap<>();

        peerReviewDrawableRes = new Pair<>(R.drawable.ic_review_basic, R.drawable.ic_review_checked);
        Pair<Integer, Integer> simpleQuestionDrawable = new Pair<>(R.drawable.ic_question_basic, R.drawable.ic_question_checked);
        Pair<Integer, Integer> videoDrawable = new Pair<>(R.drawable.ic_video_basic, R.drawable.ic_video_checked);
        Pair<Integer, Integer> hardQuizDrawable = new Pair<>(R.drawable.ic_code_basic, R.drawable.ic_code_checked);
        Pair<Integer, Integer> theoryQuizDrawable = new Pair<>(R.drawable.ic_theory_basic, R.drawable.ic_theory_checked);

        mapFromTypeToDrawableRes.put(AppConstants.TYPE_TEXT, theoryQuizDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_VIDEO, videoDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_MATCHING, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_SORTING, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_MATH, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_FREE_ANSWER, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_STRING, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_CHOICE, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_NUMBER, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_DATASET, hardQuizDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_CHEMICAL, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_PUZZLE, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_PYCHARM, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_CODE, hardQuizDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_ADMIN, hardQuizDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_SQL, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_LINUX_CODE, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_FILL_BLANKS, simpleQuestionDrawable);
        mapFromTypeToDrawableRes.put(AppConstants.TYPE_TABLE, simpleQuestionDrawable);
    }

    @Override
    public Pair<Integer, Integer> getDrawableForType(String type, boolean isPeerReview) {
        if (isPeerReview) {
            return peerReviewDrawableRes;
        }

        Pair<Integer, Integer> drawable = mapFromTypeToDrawableRes.get(type);
        if (drawable == null) {
            drawable = mapFromTypeToDrawableRes.get(AppConstants.TYPE_TEXT);
        }

        return drawable;
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
}
