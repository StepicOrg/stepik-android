package org.stepic.droid.store;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Video;

public interface IDownloadManager {

    void addDownload(String url, String fileId);

    boolean isDownloadManagerEnabled();

    void addVideoIfNeed(Video video);

    void addSection(Section section);

    void addLesson(Lesson lesson);
}
