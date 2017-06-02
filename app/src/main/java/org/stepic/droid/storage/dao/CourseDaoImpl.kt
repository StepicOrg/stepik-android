package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.mappers.toDbUrl
import org.stepic.droid.mappers.toVideoUrls
import org.stepic.droid.model.*
import org.stepic.droid.storage.structure.DbStructureCachedVideo
import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses
import org.stepic.droid.storage.structure.DbStructureVideoUrl
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.transformToCachedVideo
import java.util.*
import javax.inject.Inject

class CourseDaoImpl @Inject
constructor(
        openHelper: SQLiteDatabase,
        private val cachedVideoDao: IDao<CachedVideo>,
        private val externalVideoUrlIDao: IDao<DbVideoUrl>,
        private val tableName: String)
    : DaoBase<Course>(openHelper) {

    public override fun parsePersistentObject(cursor: Cursor): Course {
        val course = Course()

        val indexId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID)
        val indexSummary = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SUMMARY)
        val indexCover = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COVER_LINK)
        val indexIntro = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_LINK_VIMEO)
        val indexTitle = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.TITLE)
        val indexLanguage = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LANGUAGE)
        val indexBeginDateSource = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE_SOURCE)
        val indexBeginDate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE)
        val indexEndDate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.END_DATE)
        val indexLastDeadline = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LAST_DEADLINE)
        val indexDescription = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.DESCRIPTION)
        val indexInstructors = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INSTRUCTORS)
        val indexRequirements = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.REQUIREMENTS)
        val indexEnrollment = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.ENROLLMENT)
        val indexSection = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SECTIONS)
        val indexWorkload = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.WORKLOAD)
        val indexCourseFormat = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_FORMAT)
        val indexTargetAudience = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.TARGET_AUDIENCE)
        val indexCertificate = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE)
        val indexIntroVideoId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID)
        val indexSlug = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SLUG)
        val indexScheduleLink = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK)
        val indexScheduleLongLink = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK)
        val indexLastStepId = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID)
        val indexIsActive = cursor.getColumnIndex(DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE)

        course.lastStepId = cursor.getString(indexLastStepId)
        course.certificate = cursor.getString(indexCertificate)
        course.workload = cursor.getString(indexWorkload)
        course.courseFormat = cursor.getString(indexCourseFormat)
        course.targetAudience = cursor.getString(indexTargetAudience)

        course.setId(cursor.getLong(indexId))
        course.summary = cursor.getString(indexSummary)
        course.cover = cursor.getString(indexCover)
        course.intro = cursor.getString(indexIntro)
        course.title = cursor.getString(indexTitle)
        course.language = cursor.getString(indexLanguage)
        course.beginDateSource = cursor.getString(indexBeginDateSource)
        course.lastDeadline = cursor.getString(indexLastDeadline)
        course.description = cursor.getString(indexDescription)
        course.instructors = DbParseHelper.parseStringToLongArray(cursor.getString(indexInstructors))
        course.requirements = cursor.getString(indexRequirements)
        course.enrollment = cursor.getInt(indexEnrollment)
        course.sections = DbParseHelper.parseStringToLongArray(cursor.getString(indexSection))
        course.introVideoId = cursor.getLong(indexIntroVideoId)
        course.slug = cursor.getString(indexSlug)
        course.scheduleLink = cursor.getString(indexScheduleLink)
        course.scheduleLongLink = cursor.getString(indexScheduleLongLink)
        course.beginDate = cursor.getString(indexBeginDate)
        course.endDate = cursor.getString(indexEndDate)

        var isActive = true
        try {
            isActive = cursor.getInt(indexIsActive) > 0
        } catch (exception: Exception) {
            //it can be null before migration --> default active
        }

        course.setActive(isActive)

        return course
    }

    public override fun getContentValues(course: Course): ContentValues {
        val values = ContentValues()

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, course.lastStepId)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID, course.courseId)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SUMMARY, course.summary)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COVER_LINK, course.cover)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_LINK_VIMEO, course.intro)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.TITLE, course.title)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LANGUAGE, course.language)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE_SOURCE, course.beginDateSource)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.LAST_DEADLINE, course.lastDeadline)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.DESCRIPTION, course.description)

        val instructorsParsed = DbParseHelper.parseLongArrayToString(course.instructors)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.INSTRUCTORS, instructorsParsed)

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.REQUIREMENTS, course.requirements)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.ENROLLMENT, course.enrollment)

        val sectionsParsed = DbParseHelper.parseLongArrayToString(course.sections)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SECTIONS, sectionsParsed)

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.WORKLOAD, course.workload)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_FORMAT, course.courseFormat)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.TARGET_AUDIENCE, course.targetAudience)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, course.certificate)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SLUG, course.slug)

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, course.scheduleLink)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, course.scheduleLongLink)

        values.put(DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, course.beginDate)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, course.endDate)
        values.put(DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, course.isActive)

        val video = course.introVideo
        if (video != null) {
            values.put(DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, video.id)
        }
        return values
    }

    public override fun getDbName() = tableName

    public override fun getDefaultPrimaryColumn() = DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID

    public override fun getDefaultPrimaryValue(persistentObject: Course) = persistentObject.courseId.toString()

    override fun get(whereColumnName: String, whereValue: String): Course? {
        val course = super.get(whereColumnName, whereValue)
        addInnerObjects(course)
        return course
    }

    override fun getAllWithQuery(query: String, whereArgs: Array<String>?): List<Course> {
        val courseList = super.getAllWithQuery(query, whereArgs)
        for (course in courseList) {
            addInnerObjects(course)
        }
        return courseList
    }

    private fun addInnerObjects(course: Course?) {
        if (course == null) return
        val video = cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, course.introVideoId.toString())
        if (video != null) {
            val dbVideoUrls = externalVideoUrlIDao
                    .getAll(DbStructureVideoUrl.Column.videoId, course.introVideoId.toString())
            val videoUrls = dbVideoUrls.toVideoUrls()
            course.introVideo = transformCachedVideoToRealVideo(video, videoUrls)
        }
    }

    override fun insertOrUpdate(persistentObject: Course?) {
        super.insertOrUpdate(persistentObject)
        if (persistentObject != null && persistentObject.introVideo != null) {
            val video = persistentObject.introVideo
            val cachedVideo = video.transformToCachedVideo() //it is cached, but not stored video.
            cachedVideoDao.insertOrUpdate(cachedVideo)

            //add all urls for video
            val videoUrlList = video.urls
            if (videoUrlList.isNotEmpty()) {
                externalVideoUrlIDao.remove(DbStructureVideoUrl.Column.videoId, video.id.toString())
                videoUrlList.forEach { videoUrl ->
                    externalVideoUrlIDao.insertOrUpdate(videoUrl.toDbUrl(video.id))
                }
            }
        }
    }

    //// FIXME: 17.02.16 refactor this hack
    private fun transformCachedVideoToRealVideo(cachedVideo: CachedVideo?, videoUrls: List<VideoUrl>?): Video? {
        var realVideo: Video? = null
        if (cachedVideo != null) {
            realVideo = Video()
            realVideo.id = cachedVideo.videoId
            realVideo.thumbnail = cachedVideo.thumbnail

            val resultUrls: List<VideoUrl>
            if (videoUrls != null && !videoUrls.isEmpty()) {
                resultUrls = videoUrls
            } else {
                val videoUrl = VideoUrl()
                videoUrl.url = cachedVideo.url
                videoUrl.quality = cachedVideo.quality
                resultUrls = ArrayList<VideoUrl>()
                resultUrls.add(videoUrl)
            }

            realVideo.urls = resultUrls
        }
        return realVideo
    }
}
