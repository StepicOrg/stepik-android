package org.stepic.droid.core.presenters.contracts;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Unit;

public interface StepsView {
    void onLessonCorrupted();

    void onLessonUnitPrepared(Lesson lesson, @Nullable Unit unit);

    void onConnectionProblem();
}
