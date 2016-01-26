package org.stepic.droid.store;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;

public interface IDownloadManager {

    void addSection(Section section);

    @Deprecated
    void addCourse(Course course, DatabaseManager.Table type);

    void addUnitLesson(Unit unit, Lesson lesson);
}
