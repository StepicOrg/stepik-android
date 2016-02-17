package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.store.dao.IDao;
import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureAssignment;
import org.stepic.droid.store.structure.DbStructureCachedVideo;
import org.stepic.droid.store.structure.DbStructureLesson;
import org.stepic.droid.store.structure.DbStructureProgress;
import org.stepic.droid.store.structure.DbStructureSections;
import org.stepic.droid.store.structure.DbStructureSharedDownloads;
import org.stepic.droid.store.structure.DbStructureStep;
import org.stepic.droid.store.structure.DbStructureUnit;
import org.stepic.droid.store.structure.DbStructureViewQueue;
import org.stepic.droid.util.DbParseHelper;
import org.stepic.droid.web.ViewAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

// TODO: 16.01.16 split to DAOs, make more generic
@Singleton
public class DatabaseManager extends DbManagerBase {

    public enum Table {
        enrolled(DBStructureCourses.ENROLLED_COURSES),
        featured(DBStructureCourses.FEATURED_COURSES);

        private String description;

        Table(String description) {
            this.description = description;
        }

        private String getStoreName() {
            return description;
        }
    }

    @Inject
    IDao<Section> mSectionDao;
    @Inject
    IDao<Unit> mUnitDao;
    @Inject
    IDao<Progress> mProgressDao;
    @Inject
    IDao<Assignment> mAssignmentDao;
    @Inject
    IDao<Lesson> mLessonDao;
    @Inject
    IDao<ViewAssignment> mViewAssignmentDao;
    @Inject
    IDao<DownloadEntity> mDownloadEntityDao;
    @Inject
    IDao<CachedVideo> mCachedVideoDao;
    @Inject
    IDao<BlockPersistentWrapper> mBlockPersistentWrapperDao;
    @Inject
    IDao<Step> mStepDao;

    public DatabaseManager(Context context) {
        super(context);
        MainApplication.component().inject(this);
    }

    public void addAssignment(Assignment assignment) {
        mAssignmentDao.insertOrUpdate(assignment);
    }

    /**
     * deprecated because of step has 0..* assignments.
     */
    @Deprecated
    public long getAssignmentIdByStepId(long stepId) {
        Assignment assignment = mAssignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId + "");
        if (assignment == null) return -1;
        return assignment.getId();
    }

    public Map<Long, Lesson> getMapFromStepIdToTheirLesson(long[] stepIds) {
        HashMap<Long, Lesson> result = new HashMap<>();
        Set<Long> lessonSet = new HashSet<>();

        String stepIdsCommaSeparated = DbParseHelper.parseLongArrayToString(stepIds, ",");
        List<Step> steps = mStepDao.getAllInRange(DbStructureStep.Column.STEP_ID, stepIdsCommaSeparated);
        for (Step step : steps) {
            lessonSet.add(step.getLesson());
        }

        Long[] lessonIds = lessonSet.toArray(new Long[lessonSet.size()]);
        String lessonIdsCommaSeparated = DbParseHelper.parseLongArrayToString(lessonIds, ",");
        List<Lesson> lessonArrayList = mLessonDao.getAllInRange(DbStructureLesson.Column.LESSON_ID, lessonIdsCommaSeparated);
        for (Step stepItem : steps) {
            for (Lesson lesson : lessonArrayList) {
                if (lesson.getId() == stepItem.getLesson()) {
                    result.put(stepItem.getId(), lesson);
                    break;
                }
            }
        }

        return result;
    }

    @Nullable
    public Step getStepById(long stepId) {
        return mStepDao.get(DbStructureStep.Column.STEP_ID, stepId+"");
    }

    @Nullable
    public Lesson getLessonById(long lessonId) {
        return mLessonDao.get(DbStructureLesson.LESSONS, lessonId + "");
    }

    @Nullable
    public Section getSectionById(long sectionId) {
        return mSectionDao.get(DbStructureSections.Column.SECTION_ID, sectionId + "");
    }

    @Nullable
    public Course getCourseById(long courseId, Table type) {
        try {
            open();

            String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + courseId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Course course = parseCourse(cursor);
                cursor.close();
                return course;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }

    @Nullable
    public Progress getProgressById(String progressId) {
        return mProgressDao.get(DbStructureProgress.Column.ID, progressId);
    }

    @Deprecated
    @Nullable
    public Unit getUnitByLessonId(long lessonId) {
        return mUnitDao.get(DbStructureUnit.Column.LESSON, lessonId + "");
    }

    @Nullable
    public Unit getUnitById(long unitId) {
        return mUnitDao.get(DbStructureUnit.Column.UNIT_ID, unitId + "");
    }

    @NotNull
    public List<DownloadEntity> getAllDownloadEntities() {
        return mDownloadEntityDao.getAll();
    }

    public boolean isUnitCached(@NotNull Unit unit) {
        Unit dbUnit = mUnitDao.get(DbStructureUnit.Column.UNIT_ID, unit.getId() + "");
        return dbUnit != null && dbUnit.is_cached();
    }

    public boolean isLessonCached(@NotNull Lesson lesson) {
        Lesson dbLesson = mLessonDao.get(DbStructureLesson.Column.LESSON_ID, lesson.getId() + "");
        return dbLesson != null && dbLesson.is_cached();
    }

    public boolean isStepCached(@NotNull Step step) {
        Step dbStep = mStepDao.get(DbStructureStep.Column.STEP_ID, step.getId() + "");
        return dbStep != null && dbStep.is_cached();
    }

    public void updateOnlyCachedLoadingStep(Step step) {
        ContentValues cv = new ContentValues();
        cv.put(DbStructureStep.Column.IS_LOADING, step.is_loading());
        cv.put(DbStructureStep.Column.IS_CACHED, step.is_cached());
        mStepDao.update(DbStructureStep.Column.STEP_ID, step.getId()+"", cv);
    }

    public void updateOnlyCachedLoadingUnit(Unit unit) {
        ContentValues cv = new ContentValues();
        cv.put(DbStructureUnit.Column.IS_LOADING, unit.is_loading());
        cv.put(DbStructureUnit.Column.IS_CACHED, unit.is_cached());
        mUnitDao.update(DbStructureUnit.Column.UNIT_ID, unit.getId() + "", cv);
    }

    public void updateOnlyCachedLoadingLesson(Lesson lesson) {
        ContentValues cv = new ContentValues();
        cv.put(DbStructureLesson.Column.IS_LOADING, lesson.is_loading());
        cv.put(DbStructureLesson.Column.IS_CACHED, lesson.is_cached());
        mUnitDao.update(DbStructureLesson.Column.LESSON_ID, lesson.getId() + "", cv);
    }

    public void updateOnlyCachedLoadingSection(Section section) {
        ContentValues cv = new ContentValues();
        cv.put(DbStructureSections.Column.IS_LOADING, section.is_loading());
        cv.put(DbStructureSections.Column.IS_CACHED, section.is_cached());
        mSectionDao.update(DbStructureSections.Column.SECTION_ID, section.getId() + "", cv);
    }

    @Deprecated
    public void updateOnlyCachedLoadingCourse(Course course, Table type) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DBStructureCourses.Column.IS_LOADING, course.is_loading());
            cv.put(DBStructureCourses.Column.IS_CACHED, course.is_cached());

            database.update(type.getStoreName(), cv, DBStructureCourses.Column.COURSE_ID + "=" + course.getCourseId(), null);
        } finally {
            close();
        }
    }

    public List<Course> getAllCourses(DatabaseManager.Table type) {
        try {
            open();
            List<Course> courses = new ArrayList<>();
            Cursor cursor = getCourseCursor(type);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Course course = parseCourse(cursor);
                courses.add(course);
                cursor.moveToNext();
            }
            cursor.close();
            return courses;

        } finally {
            close();
        }
    }

    public void addCourse(Course course, DatabaseManager.Table type) {

        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DBStructureCourses.Column.COURSE_ID, course.getCourseId());
            values.put(DBStructureCourses.Column.SUMMARY, course.getSummary());
            values.put(DBStructureCourses.Column.COVER_LINK, course.getCover());
            values.put(DBStructureCourses.Column.INTRO_LINK_VIMEO, course.getIntro());
            values.put(DBStructureCourses.Column.TITLE, course.getTitle());
            values.put(DBStructureCourses.Column.LANGUAGE, course.getLanguage());
            values.put(DBStructureCourses.Column.BEGIN_DATE_SOURCE, course.getBegin_date_source());
            values.put(DBStructureCourses.Column.LAST_DEADLINE, course.getLast_deadline());
            values.put(DBStructureCourses.Column.DESCRIPTION, course.getDescription());

            String instructorsParsed = DbParseHelper.parseLongArrayToString(course.getInstructors());
            values.put(DBStructureCourses.Column.INSTRUCTORS, instructorsParsed);

            values.put(DBStructureCourses.Column.REQUIREMENTS, course.getRequirements());
            values.put(DBStructureCourses.Column.ENROLLMENT, course.getEnrollment());

            String sectionsParsed = DbParseHelper.parseLongArrayToString(course.getSections());
            values.put(DBStructureCourses.Column.SECTIONS, sectionsParsed);

            values.put(DBStructureCourses.Column.WORKLOAD, course.getWorkload());
            values.put(DBStructureCourses.Column.COURSE_FORMAT, course.getCourse_format());
            values.put(DBStructureCourses.Column.TARGET_AUDIENCE, course.getTarget_audience());
            values.put(DBStructureCourses.Column.CERTIFICATE, course.getCertificate());

            Video video = course.getIntro_video();
            if (video != null) {
                CachedVideo storedVideo = new CachedVideo();//it is cached, but not stored video.
                storedVideo.setVideoId(video.getId());
                storedVideo.setStepId(-1);
                storedVideo.setThumbnail(video.getThumbnail());
                if (video.getUrls() != null && !video.getUrls().isEmpty()) {
                    VideoUrl videoUrl = video.getUrls().get(0);
                    storedVideo.setQuality(videoUrl.getQuality());
                    storedVideo.setUrl(videoUrl.getUrl());
                }
                addVideo(storedVideo);
                values.put(DBStructureCourses.Column.INTRO_VIDEO_ID, storedVideo.getVideoId());
            }

//            values.put(DBStructureCourses.Column.IS_CACHED, course.is_cached());
//            values.put(DBStructureCourses.Column.IS_LOADING, course.is_loading());


            if (isCourseInDB(course, type)) {
                database.update(type.getStoreName(), values, DBStructureCourses.Column.COURSE_ID + "=" + course.getCourseId(), null);
            } else {
                database.insert(type.getStoreName(), null, values);
            }

        } finally {
            close();
        }


    }

    public void deleteCourse(Course course, DatabaseManager.Table type) {
        try {
            open();
            long courseId = course.getCourseId();
            database.delete(type.getStoreName(),
                    DBStructureCourses.Column.COURSE_ID + " = " + courseId,
                    null);
        } finally {
            close();
        }
    }

    private boolean isCourseInDB(Course course, DatabaseManager.Table type) {
        String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + course.getCourseId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void addSection(Section section) {
        mSectionDao.insertOrUpdate(section);
    }

    public void addStep(Step step) {
        mStepDao.insertOrUpdate(step);
    }

    public List<Section> getAllSectionsOfCourse(Course course) {
        return mSectionDao.getAll(DbStructureSections.Column.COURSE, course.getCourseId() + "");
    }

    public List<Unit> getAllUnitsOfSection(long sectionId) {
        return mUnitDao.getAll(DbStructureUnit.Column.SECTION, sectionId + "");
    }

    public List<Step> getStepsOfLesson(long lessonId) {
        return mStepDao.getAll(DbStructureStep.Column.LESSON_ID, lessonId + "");
    }

    @Nullable
    public Lesson getLessonOfUnit(Unit unit) {
        return mLessonDao.get(DbStructureLesson.Column.LESSON_ID, unit.getLesson() + "");
    }

    public void addVideo(CachedVideo cachedVideo) {
        mCachedVideoDao.insertOrUpdate(cachedVideo);
    }

    public void deleteDownloadEntityByDownloadId(long downloadId) {
        mDownloadEntityDao.delete(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId + "");
    }

    public boolean isExistDownloadEntityByVideoId(long videoId) {
        return mDownloadEntityDao.isInDb(DbStructureSharedDownloads.Column.VIDEO_ID, videoId + "");
    }

    public void deleteVideo(Video video) {
        mCachedVideoDao.delete(DbStructureCachedVideo.Column.VIDEO_ID, video.getId() + "");
    }

    public void deleteVideoByUrl(String path) {
        mCachedVideoDao.delete(DbStructureCachedVideo.Column.URL, path);
    }

    public void deleteStep(Step step) {
        long stepId = step.getId();
        deleteStepById(stepId);
    }

    public void deleteStepById(long stepId) {
        mStepDao.delete(DbStructureStep.Column.STEP_ID, stepId+"");
    }

    @Nullable
    public CachedVideo getCachedVideoById(long videoId) {
        return mCachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, videoId + "");
    }

    public List<CachedVideo> getAllCachedVideo() {
        return mCachedVideoDao.getAll();
    }

    /**
     * getPath of cached video
     *
     * @param video video which we check for contains in db
     * @return null if video not existing in db, otherwise path to disk
     */
    @Nullable
    public String getPathToVideoIfExist(@NotNull Video video) {
        CachedVideo cachedVideo = mCachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, video.getId() + "");

        if (cachedVideo == null) {
            return null;
        } else {
            return cachedVideo.getUrl();
        }
    }

    @Nullable
    public DownloadEntity getDownloadEntityIfExist(Long downloadId) {
        return mDownloadEntityDao.get(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId + "");
    }

    public void clearCacheCourses(DatabaseManager.Table type) {
        List<Course> courses = getAllCourses(type);

        for (Course courseItem : courses) {
            deleteCourse(courseItem, type);
        }
    }

    public void addUnit(Unit unit) {
        mUnitDao.insertOrUpdate(unit);
    }

    public void addDownloadEntity(DownloadEntity downloadEntity) {
        mDownloadEntityDao.insertOrUpdate(downloadEntity);
    }


    public void addLesson(Lesson lesson) {
        mLessonDao.insertOrUpdate(lesson);
    }

    private Course parseCourse(Cursor cursor) {
        Course course = new Course();

        int indexId = cursor.getColumnIndex(DBStructureCourses.Column.COURSE_ID);
        int indexSummary = cursor.getColumnIndex(DBStructureCourses.Column.SUMMARY);
        int indexCover = cursor.getColumnIndex(DBStructureCourses.Column.COVER_LINK);
        int indexIntro = cursor.getColumnIndex(DBStructureCourses.Column.INTRO_LINK_VIMEO);
        int indexTitle = cursor.getColumnIndex(DBStructureCourses.Column.TITLE);
        int indexLanguage = cursor.getColumnIndex(DBStructureCourses.Column.LANGUAGE);
        int indexBeginDateSource = cursor.getColumnIndex(DBStructureCourses.Column.BEGIN_DATE_SOURCE);
        int indexLastDeadline = cursor.getColumnIndex(DBStructureCourses.Column.LAST_DEADLINE);
        int indexDescription = cursor.getColumnIndex(DBStructureCourses.Column.DESCRIPTION);
        int indexInstructors = cursor.getColumnIndex(DBStructureCourses.Column.INSTRUCTORS);
        int indexRequirements = cursor.getColumnIndex(DBStructureCourses.Column.REQUIREMENTS);
        int indexEnrollment = cursor.getColumnIndex(DBStructureCourses.Column.ENROLLMENT);
        int indexSection = cursor.getColumnIndex(DBStructureCourses.Column.SECTIONS);
        int indexIsCached = cursor.getColumnIndex(DBStructureCourses.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DBStructureCourses.Column.IS_LOADING);
        int indexWorkload = cursor.getColumnIndex(DBStructureCourses.Column.WORKLOAD);
        int indexCourseFormat = cursor.getColumnIndex(DBStructureCourses.Column.COURSE_FORMAT);
        int indexTargetAudience = cursor.getColumnIndex(DBStructureCourses.Column.TARGET_AUDIENCE);
        int indexCertificate = cursor.getColumnIndex(DBStructureCourses.Column.CERTIFICATE);
        int indexIntroVideoId = cursor.getColumnIndex(DBStructureCourses.Column.INTRO_VIDEO_ID);

        course.setCertificate(cursor.getString(indexCertificate));
        course.setWorkload(cursor.getString(indexWorkload));
        course.setCourse_format(cursor.getString(indexCourseFormat));
        course.setTarget_audience(cursor.getString(indexTargetAudience));

        course.setId(cursor.getLong(indexId));
        course.setSummary(cursor.getString(indexSummary));
        course.setCover(cursor.getString(indexCover));
        course.setIntro(cursor.getString(indexIntro));
        course.setTitle(cursor.getString(indexTitle));
        course.setLanguage(cursor.getString(indexLanguage));
        course.setBegin_date_source(cursor.getString(indexBeginDateSource));
        course.setLast_deadline(cursor.getString(indexLastDeadline));
        course.setDescription(cursor.getString(indexDescription));
        course.setInstructors(DbParseHelper.parseStringToLongArray(cursor.getString(indexInstructors)));
        course.setRequirements(cursor.getString(indexRequirements));
        course.setEnrollment(cursor.getInt(indexEnrollment));
        course.setIs_cached(cursor.getInt(indexIsCached) > 0);
        course.setIs_loading(cursor.getInt(indexIsLoading) > 0);
        course.setSections(DbParseHelper.parseStringToLongArray(cursor.getString(indexSection)));
        course.setIntro_video_id(cursor.getLong(indexIntroVideoId));

        CachedVideo video = getCachedVideoById(course.getIntro_video_id());
        course.setIntro_video(transformCachedVideoToRealVideo(video));
        return course;
    }

    //// FIXME: 17.02.16 refactor this hack
    @Nullable
    private Video transformCachedVideoToRealVideo(CachedVideo video) {
        Video realVideo = null;
        if (video != null) {
            realVideo = new Video();
            realVideo.setId(video.getVideoId());
            realVideo.setThumbnail(video.getThumbnail());
            VideoUrl videoUrl = new VideoUrl();
            videoUrl.setQuality(video.getQuality());
            videoUrl.setUrl(video.getUrl());

            List<VideoUrl> list = new ArrayList<>();
            list.add(videoUrl);
            realVideo.setUrls(list);
        }
        return realVideo;
    }

    private Cursor getCourseCursor(DatabaseManager.Table type) {
        return database.query(type.getStoreName(), DBStructureCourses.getUsedColumns(),
                null, null, null, null, null);
    }

    public void addToQueueViewedState(ViewAssignment viewState) {
        mViewAssignmentDao.insertOrUpdate(viewState);
    }

    @NotNull
    public List<ViewAssignment> getAllInQueue() {
        return mViewAssignmentDao.getAll();
    }

    public void removeFromQueue(ViewAssignment viewAssignmentWrapper) {
        long assignmentId = viewAssignmentWrapper.getAssignment();
        mViewAssignmentDao.delete(DbStructureViewQueue.Column.ASSIGNMENT_ID, assignmentId + "");
    }

    public void markProgressAsPassed(long assignmentId) {
        Assignment assignment = mAssignmentDao.get(DbStructureAssignment.Column.ASSIGNMENT_ID, assignmentId + "");
        String progressId = assignment.getProgress();
        markProgressAsPassedIfInDb(progressId);
    }

    public void markProgressAsPassedIfInDb(String progressId) {
        boolean inDb = mProgressDao.isInDb(DbStructureProgress.Column.ID, progressId);
        if (inDb) {
            ContentValues values = new ContentValues();
            values.put(DbStructureProgress.Column.IS_PASSED, true);
            mProgressDao.update(DbStructureProgress.Column.ID, progressId, values);
        }
    }

    public void addProgress(Progress progress) {
        mProgressDao.insertOrUpdate(progress);
    }

    public boolean isProgressViewed(String progressId) {
        if (progressId == null) return false;
        Progress progress = mProgressDao.get(DbStructureProgress.Column.ID, progressId);
        return progress.is_passed();
    }

    private boolean isAssignmentByStepViewed(long stepId) {
        Assignment assignment = mAssignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId + "");
        if (assignment == null) return false;
        String progressId = assignment.getProgress();
        return isProgressViewed(progressId);
    }

    public boolean isStepPassed(long stepId) {
        return isAssignmentByStepViewed(stepId);
    }
}