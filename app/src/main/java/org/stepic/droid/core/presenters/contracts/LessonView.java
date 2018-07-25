package org.stepic.droid.core.presenters.contracts;

import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Unit;

public interface LessonView {
    void onLessonCorrupted();

    void onLessonUnitPrepared(Lesson lesson, Unit unit, Section section);

    void onConnectionProblem();

    void showSteps(boolean fromPreviousLesson, long defaultStepPosition);

    void onEmptySteps();

    void onLoading();

    void onUserNotAuth();
}
