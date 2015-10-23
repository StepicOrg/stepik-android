package org.stepic.droid.store;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;

public interface IDownloadManager {


    boolean isDownloadManagerEnabled();

    void addStep(Step step, String title);

    void addSection(Section section);

    void addCourse(Course course);

    void addLesson(Lesson lesson);
}
