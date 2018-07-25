package org.stepic.droid.storage;

import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;

public interface IDownloadManager {

    void addSection(Section section);

    void addLesson(Lesson lesson);

    void cancelStep(long stepId);
}
