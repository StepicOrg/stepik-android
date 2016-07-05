package org.stepic.droid.core;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Submission;

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
            return;
        }

        mStepIdToAttemptMap.put(stepId, attempt);
        mStepIdToSubmissionMap.put(stepId, submission);
    }

    @Override
    public void reset() {
        mStepIdToAttemptMap.clear();
        mStepIdToSubmissionMap.clear();
    }
}
