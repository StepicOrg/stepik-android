package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;

import java.util.List;

public interface StepsView {
    void onLessonCorrupted();

    void onLessonUnitPrepared(Lesson lesson, Unit unit);

    void onConnectionProblem();

    void showSteps(List<Step> stepList);

    void onEmptySteps();

    void updateTabState(List<Step> stepList);
}
