package org.stepic.droid.storage;

import org.stepik.android.model.structure.Lesson;
import org.stepic.droid.model.Section;

public interface IDownloadManager {

    void addSection(Section section);

    void addLesson(Lesson lesson);

    void cancelStep(long stepId);
}
