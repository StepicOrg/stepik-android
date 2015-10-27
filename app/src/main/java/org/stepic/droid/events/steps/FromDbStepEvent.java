package org.stepic.droid.events.steps;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;

import java.util.List;

public class FromDbStepEvent {
    final List<Step> stepList;
    final Lesson lesson;

    public FromDbStepEvent(List<Step> stepList, Lesson lesson) {
        this.stepList = stepList;
        this.lesson = lesson;
    }

    public List<Step> getStepList() {
        return stepList;
    }

    public Lesson getLesson() {
        return lesson;
    }
}
