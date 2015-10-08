package org.stepic.droid.web;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Meta;

import java.util.List;

public class LessonStepicResponse implements IStepicResponse {
    Meta meta;
    List<Lesson> lessons;

    public Meta getMeta() {
        return meta;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
