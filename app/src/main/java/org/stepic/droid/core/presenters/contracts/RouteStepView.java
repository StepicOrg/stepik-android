package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Unit;

public interface RouteStepView {
    void showNextLessonView();

    void openNextLesson(Unit nextUnit, Lesson nextLesson);

    void showLoading();

    void showCantGoNext();

    void showPreviousLessonView();

    void openPreviousLesson(Unit previousUnit, Lesson previousLesson);

    void showCantGoPrevious();

}
