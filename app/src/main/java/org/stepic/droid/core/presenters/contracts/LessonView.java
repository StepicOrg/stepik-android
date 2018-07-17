package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepik.android.model.structure.Unit;

public interface LessonView {
    void onLessonCorrupted();

    void onLessonUnitPrepared(Lesson lesson, Unit unit, Section section);

    void onConnectionProblem();

    void showSteps(boolean fromPreviousLesson, long defaultStepPosition);

    void onEmptySteps();

    void onLoading();

    void onUserNotAuth();
}
