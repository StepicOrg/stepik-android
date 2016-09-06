package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Unit;

public interface StepsView {
    void onLessonCorrupted();

    void onLessonUnitPrepared(Lesson lesson, Unit unit);

    void onConnectionProblem();

    void showSteps(boolean fromPreviousLesson, long defaultStepPosition);

    void onEmptySteps();

    void onLoading();
}
