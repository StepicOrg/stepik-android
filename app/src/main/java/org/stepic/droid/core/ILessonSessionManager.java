package org.stepic.droid.core;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Submission;

public interface ILessonSessionManager {
    @Nullable
    Submission restoreSubmissionForStep(long stepId);

    @Nullable
    Attempt restoreAttemptForStep(long stepId);

    void saveSession(long stepId, @Nullable Attempt attempt, @Nullable Submission submission);
}
