package org.stepic.droid.core.presenters.contracts;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Unit;

public interface RouteStepView {
    void showNextLessonView();

    void openNextLesson(@NotNull Unit nextUnit, @NotNull Lesson nextLesson, @NotNull Section nextSection);

    void showLoading();

    void showCantGoNext();

    void showPreviousLessonView();

    void openPreviousLesson(@NotNull Unit previousUnit, @NotNull Lesson previousLesson, @NotNull Section previousSection);

    void showCantGoPrevious();

}
