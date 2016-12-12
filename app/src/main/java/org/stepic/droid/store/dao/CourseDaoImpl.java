package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureCachedVideo;
import org.stepic.droid.util.DbParseHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseDaoImpl extends DaoBase<Course> {
    private final IDao<CachedVideo> cachedVideoDao;
    private String tableName;

    public CourseDaoImpl(SQLiteDatabase openHelper, IDao<CachedVideo> cachedVideoDao) {
        super(openHelper);
        this.cachedVideoDao = cachedVideoDao;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Course parsePersistentObject(Cursor cursor) {
        Course course = new Course();

        int indexId = cursor.getColumnIndex(DBStructureCourses.Column.COURSE_ID);
        int indexSummary = cursor.getColumnIndex(DBStructureCourses.Column.SUMMARY);
        int indexCover = cursor.getColumnIndex(DBStructureCourses.Column.COVER_LINK);
        int indexIntro = cursor.getColumnIndex(DBStructureCourses.Column.INTRO_LINK_VIMEO);
        int indexTitle = cursor.getColumnIndex(DBStructureCourses.Column.TITLE);
        int indexLanguage = cursor.getColumnIndex(DBStructureCourses.Column.LANGUAGE);
        int indexBeginDateSource = cursor.getColumnIndex(DBStructureCourses.Column.BEGIN_DATE_SOURCE);
        int indexBeginDate = cursor.getColumnIndex(DBStructureCourses.Column.BEGIN_DATE);
        int indexEndDate = cursor.getColumnIndex(DBStructureCourses.Column.END_DATE);
        int indexLastDeadline = cursor.getColumnIndex(DBStructureCourses.Column.LAST_DEADLINE);
        int indexDescription = cursor.getColumnIndex(DBStructureCourses.Column.DESCRIPTION);
        int indexInstructors = cursor.getColumnIndex(DBStructureCourses.Column.INSTRUCTORS);
        int indexRequirements = cursor.getColumnIndex(DBStructureCourses.Column.REQUIREMENTS);
        int indexEnrollment = cursor.getColumnIndex(DBStructureCourses.Column.ENROLLMENT);
        int indexSection = cursor.getColumnIndex(DBStructureCourses.Column.SECTIONS);
        int indexWorkload = cursor.getColumnIndex(DBStructureCourses.Column.WORKLOAD);
        int indexCourseFormat = cursor.getColumnIndex(DBStructureCourses.Column.COURSE_FORMAT);
        int indexTargetAudience = cursor.getColumnIndex(DBStructureCourses.Column.TARGET_AUDIENCE);
        int indexCertificate = cursor.getColumnIndex(DBStructureCourses.Column.CERTIFICATE);
        int indexIntroVideoId = cursor.getColumnIndex(DBStructureCourses.Column.INTRO_VIDEO_ID);
        int indexSlug = cursor.getColumnIndex(DBStructureCourses.Column.SLUG);
        int indexScheduleLink = cursor.getColumnIndex(DBStructureCourses.Column.SCHEDULE_LINK);
        int indexScheduleLongLink = cursor.getColumnIndex(DBStructureCourses.Column.SCHEDULE_LONG_LINK);
        int indexLastStepId = cursor.getColumnIndex(DBStructureCourses.Column.LAST_STEP_ID);
        int indexIsActive = cursor.getColumnIndex(DBStructureCourses.Column.IS_ACTIVE);

        course.setLastStepId(cursor.getString(indexLastStepId));
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
        course.setSections(DbParseHelper.parseStringToLongArray(cursor.getString(indexSection)));
        course.setIntro_video_id(cursor.getLong(indexIntroVideoId));
        course.setSlug(cursor.getString(indexSlug));
        course.setSchedule_link(cursor.getString(indexScheduleLink));
        course.setSchedule_long_link(cursor.getString(indexScheduleLongLink));
        course.setBegin_date(cursor.getString(indexBeginDate));
        course.setEnd_date(cursor.getString(indexEndDate));

        boolean isActive = true;
        try {
            isActive = cursor.getInt(indexIsActive) > 0;
        } catch (Exception exception) {
            //it can be null before migration --> default active
        }
        course.setIs_active(isActive);

        return course;
    }

    @Override
    public ContentValues getContentValues(Course course) {
        ContentValues values = new ContentValues();

        values.put(DBStructureCourses.Column.LAST_STEP_ID, course.getLastStepId());
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
        values.put(DBStructureCourses.Column.SLUG, course.getSlug());

        values.put(DBStructureCourses.Column.SCHEDULE_LINK, course.getSchedule_link());
        values.put(DBStructureCourses.Column.SCHEDULE_LONG_LINK, course.getSchedule_long_link());

        values.put(DBStructureCourses.Column.BEGIN_DATE, course.getBegin_date());
        values.put(DBStructureCourses.Column.END_DATE, course.getEnd_date());
        values.put(DBStructureCourses.Column.IS_ACTIVE, course.is_active());

        Video video = course.getIntro_video();
        if (video != null) {
            values.put(DBStructureCourses.Column.INTRO_VIDEO_ID, video.getId());
        }
        return values;
    }

    @Override
    public String getDbName() {
        return tableName;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DBStructureCourses.Column.COURSE_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Course persistentObject) {
        return persistentObject.getCourseId() + "";
    }

    @Nullable
    @Override
    public Course get(String whereColumn, String whereValue) {
        Course course = super.get(whereColumn, whereValue);
        addInnerObjects(course);
        return course;
    }

    @Override
    protected List<Course> getAllWithQuery(String query, String[] whereArgs) {
        List<Course> courseList = super.getAllWithQuery(query, whereArgs);
        for (Course course : courseList) {
            addInnerObjects(course);
        }
        return courseList;
    }

    private void addInnerObjects(Course course) {
        if (course == null) return;
        CachedVideo video = cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, course.getIntro_video_id() + "");
        if (video != null) {
            course.setIntro_video(transformCachedVideoToRealVideo(video));
        }
    }

    @Override
    public void insertOrUpdate(Course course) {
        super.insertOrUpdate(course);
        if (course != null && course.getIntro_video() != null) {
            Video video = course.getIntro_video();
            CachedVideo storedVideo = new CachedVideo();//it is cached, but not stored video.
            storedVideo.setVideoId(video.getId());
            storedVideo.setStepId(-1);
            storedVideo.setThumbnail(video.getThumbnail());
            if (video.getUrls() != null && !video.getUrls().isEmpty()) {
                VideoUrl videoUrl = video.getUrls().get(0);
                storedVideo.setQuality(videoUrl.getQuality());
                storedVideo.setUrl(videoUrl.getUrl());
            }
            cachedVideoDao.insertOrUpdate(storedVideo);
        }
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
}
