package org.stepic.droid.core.presenters.contracts;

import org.stepik.android.model.structure.Lesson;
import org.stepic.droid.model.Section;
import org.stepik.android.model.structure.Unit;

public interface RouteStepView {
    void showNextLessonView();

    void openNextLesson(Unit nextUnit, Lesson nextLesson, Section nextSection);

    void showLoading();

    void showCantGoNext();

    void showPreviousLessonView();

    void openPreviousLesson(Unit previousUnit, Lesson previousLesson, Section previousSection);

    void showCantGoPrevious();

}
