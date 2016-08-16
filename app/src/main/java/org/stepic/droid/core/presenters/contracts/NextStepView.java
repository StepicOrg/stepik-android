package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Unit;

public interface NextStepView {
    void showNextLessonView();

    void openNextLesson(Unit nextUnit, Lesson nextLesson);

    void showLoadDialog();

    void showCantGoNext();
}
