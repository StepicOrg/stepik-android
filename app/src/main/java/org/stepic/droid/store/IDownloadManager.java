package org.stepic.droid.store;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

public interface IDownloadManager {

    void addSection(Section section);

    void addUnitLesson(Unit unit, Lesson lesson);

    void cancelStep(long stepId);
}
