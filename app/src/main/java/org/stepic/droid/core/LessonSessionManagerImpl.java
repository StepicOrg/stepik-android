package org.stepic.droid.core;

import android.util.Log;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Submission;
import org.stepic.droid.util.AppConstants;

public class LessonSessionManagerImpl implements ILessonSessionManager {

    private final String TAG = "Lesson Session Manager";

    @Nullable
    @Override
    public Submission restoreSubmissionForStep(long stepId) {
        return null;
    }

    @Nullable
    @Override
    public Attempt restoreAttemptForStep(long stepId) {
        return null;
    }

    @Override
    public void saveSession(long stepId, @Nullable Attempt attempt, @Nullable Submission submission) {
        if (attempt == null || submission == null) {
            Log.d(TAG, AppConstants.SAVE_SESSION_FAIL);
            YandexMetrica.reportEvent(AppConstants.SAVE_SESSION_FAIL);
            return;
        }
    }
}
