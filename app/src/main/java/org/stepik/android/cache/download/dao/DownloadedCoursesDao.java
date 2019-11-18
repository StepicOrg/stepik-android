package org.stepik.android.cache.download.dao;

import org.stepic.droid.storage.dao.IDao;

import java.util.List;

public interface DownloadedCoursesDao extends IDao<Long> {
    List<Long> getCourseIds();
}
