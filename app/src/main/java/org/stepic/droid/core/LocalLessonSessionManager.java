package org.stepic.droid.core;

import android.util.Log;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Submission;
import org.stepic.droid.util.AppConstants;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class LocalLessonSessionManager implements ILessonSessionManager {

    private final String TAG = "Lesson Session Manager";
    private Map<Long, Attempt> mStepIdToAttemptMap;
    private Map<Long, Submission> mStepIdToSubmissionMap;

    @Inject
    public LocalLessonSessionManager() {
        mStepIdToAttemptMap = new HashMap<>();
        mStepIdToSubmissionMap = new HashMap<>();

    }

    @Nullable
    @Override
    public Submission restoreSubmissionForStep(long stepId) {
        return mStepIdToSubmissionMap.get(stepId);
    }

    @Nullable
    @Override
    public Attempt restoreAttemptForStep(long stepId) {
        return mStepIdToAttemptMap.get(stepId);
    }

    @Override
    public void saveSession(long stepId, @Nullable Attempt attempt, @Nullable Submission submission) {
        if (attempt == null || submission == null) {
            Log.d(TAG, AppConstants.SAVE_SESSION_FAIL);
            YandexMetrica.reportEvent(AppConstants.SAVE_SESSION_FAIL);
            return;
        }

        mStepIdToAttemptMap.put(stepId, attempt);
        mStepIdToSubmissionMap.put(stepId, submission);
    }
}
