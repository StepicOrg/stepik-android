package org.stepic.droid.core.presenters.contracts;

import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Unit;

public interface RouteStepView {
    void showNextLessonView();

    void openNextLesson(Unit nextUnit, Lesson nextLesson, Section nextSection);

    void showLoading();

    void showCantGoNext();

    void showPreviousLessonView();

    void openPreviousLesson(Unit previousUnit, Lesson previousLesson, Section previousSection);

    void showCantGoPrevious();

}
