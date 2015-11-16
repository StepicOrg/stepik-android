package org.stepic.droid.events.steps;

import org.stepic.droid.model.Lesson;

public class SuccessToDbStepEvent {

    private Lesson mLesson;

    public SuccessToDbStepEvent(Lesson mLesson) {

        this.mLesson = mLesson;
    }

    public Lesson getmLesson() {
        return mLesson;
    }

    public void setmLesson(Lesson mLesson) {
        this.mLesson = mLesson;
    }
}
