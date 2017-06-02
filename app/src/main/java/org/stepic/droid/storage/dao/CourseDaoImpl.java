package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses;
import org.stepic.droid.storage.structure.DbStructureCachedVideo;
import org.stepic.droid.util.DbParseHelper;
import org.stepic.droid.util.VideoCourseHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CourseDaoImpl extends DaoBase<Course> {
    private final IDao<CachedVideo> cachedVideoDao;
    private String tableName;

    @Inject
    public CourseDaoImpl(SQLiteDatabase openHelper, IDao<CachedVideo> cachedVideoDao, String tableName) {
        super(openHelper);
        this.cachedVideoDao = cachedVideoDao;
        this.tableName = tableName;
    }

    @Override
    public Course parsePersistentObject(Cursor cursor) {
        Course course = new Course();

        int indexId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID);
        int indexSummary = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SUMMARY);
        int indexCover = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COVER_LINK);
        int indexIntro = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_LINK_VIMEO);
        int indexTitle = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.TITLE);
        int indexLanguage = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LANGUAGE);
        int indexBeginDateSource = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE_SOURCE);
        int indexBeginDate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE);
        int indexEndDate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.END_DATE);
        int indexLastDeadline = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LAST_DEADLINE);
        int indexDescription = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.DESCRIPTION);
        int indexInstructors = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INSTRUCTORS);
        int indexRequirements = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.REQUIREMENTS);
        int indexEnrollment = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.ENROLLMENT);
        int indexSection = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SECTIONS);
        int indexWorkload = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.WORKLOAD);
        int indexCourseFormat = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_FORMAT);
        int indexTargetAudience = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.TARGET_AUDIENCE);
        int indexCertificate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE);
        int indexIntroVideoId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID);
        int indexSlug = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SLUG);
        int indexScheduleLink = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK);
        int indexScheduleLongLink = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK);
        int indexLastStepId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID);
        int indexIsActive = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE);

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

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, course.getLastStepId());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID, course.getCourseId());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SUMMARY, course.getSummary());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COVER_LINK, course.getCover());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_LINK_VIMEO, course.getIntro());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.TITLE, course.getTitle());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LANGUAGE, course.getLanguage());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE_SOURCE, course.getBegin_date_source());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LAST_DEADLINE, course.getLast_deadline());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.DESCRIPTION, course.getDescription());

        String instructorsParsed = DbParseHelper.parseLongArrayToString(course.getInstructors());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.INSTRUCTORS, instructorsParsed);

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.REQUIREMENTS, course.getRequirements());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.ENROLLMENT, course.getEnrollment());

        String sectionsParsed = DbParseHelper.parseLongArrayToString(course.getSections());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SECTIONS, sectionsParsed);

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.WORKLOAD, course.getWorkload());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_FORMAT, course.getCourse_format());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.TARGET_AUDIENCE, course.getTarget_audience());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, course.getCertificate());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SLUG, course.getSlug());

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, course.getSchedule_link());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, course.getSchedule_long_link());

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, course.getBegin_date());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, course.getEnd_date());
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, course.is_active());

        Video video = course.getIntro_video();
        if (video != null) {
            values.put(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, video.getId());
        }
        return values;
    }

    @Override
    public String getDbName() {
        return tableName;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID;
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
            CachedVideo cachedVideo = VideoCourseHelper.INSTANCE.transformToCachedVideo(video); //it is cached, but not stored video.
            cachedVideoDao.insertOrUpdate(cachedVideo);
        }
    }

    //// FIXME: 17.02.16 refactor this hack
    @Nullable
    private Video transformCachedVideoToRealVideo(CachedVideo cachedVideo) {
        Video realVideo = null;
        if (cachedVideo != null) {
            realVideo = new Video();
            realVideo.setId(cachedVideo.getVideoId());
            realVideo.setThumbnail(cachedVideo.getThumbnail());
            VideoUrl videoUrl = new VideoUrl();
            videoUrl.setQuality(cachedVideo.getQuality());
            videoUrl.setUrl(cachedVideo.getUrl());

            List<VideoUrl> list = new ArrayList<>();
            list.add(videoUrl);
            realVideo.setUrls(list);
        }
        return realVideo;
    }
}
