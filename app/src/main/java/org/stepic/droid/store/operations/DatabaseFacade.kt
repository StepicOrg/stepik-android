package org.stepic.droid.store.operations

import android.content.ContentValues
import org.stepic.droid.base.MainApplication
import org.stepic.droid.model.*
import org.stepic.droid.model.Unit
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.store.dao.IDao
import org.stepic.droid.store.structure.*
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.web.ViewAssignment
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFacade {

    enum class Table(val storeName: String) {
        enrolled(DBStructureCourses.ENROLLED_COURSES),
        featured(DBStructureCourses.FEATURED_COURSES)
    }

    @Inject
    lateinit var mSectionDao: IDao<Section>
    @Inject
    lateinit var mUnitDao: IDao<Unit>
    @Inject
    lateinit var mProgressDao: IDao<Progress>
    @Inject
    lateinit var mAssignmentDao: IDao<Assignment>
    @Inject
    lateinit var mLessonDao: IDao<Lesson>
    @Inject
    lateinit var mViewAssignmentDao: IDao<ViewAssignment>
    @Inject
    lateinit var mDownloadEntityDao: IDao<DownloadEntity>
    @Inject
    lateinit var mCachedVideoDao: IDao<CachedVideo>
    @Inject
    lateinit var mStepDao: IDao<Step>

    @Inject
    lateinit var mCoursesEnrolledDao: IDao<Course>
    @Inject
    lateinit var mCoursesFeaturedDao: IDao<Course>

    @Inject
    lateinit var mNotificationDao: IDao<Notification>

    @Inject
    lateinit var calendarSectionDao: IDao<CalendarSection>

    init {
        MainApplication.component().inject(this)
        mCoursesEnrolledDao.setTableName(Table.enrolled.storeName)
        mCoursesFeaturedDao.setTableName(Table.featured.storeName)
    }

    fun dropDatabase() {
        mSectionDao.removeAll()
        mUnitDao.removeAll()
        mProgressDao.removeAll()
        mLessonDao.removeAll()
        mViewAssignmentDao.removeAll()
        mDownloadEntityDao.removeAll()
        mCachedVideoDao.removeAll()
        mStepDao.removeAll()
        mCoursesEnrolledDao.removeAll()
        mCoursesFeaturedDao.removeAll()
        mNotificationDao.removeAll()
    }

    fun getCourseDao(table: Table) =
            if (table == Table.featured) mCoursesFeaturedDao
            else mCoursesEnrolledDao


    fun addAssignment(assignment: Assignment?) = assignment?.let { mAssignmentDao.insertOrUpdate(assignment) }

    /**
     * deprecated because of step has 0..* assignments.
     */
    @Deprecated("")
    fun getAssignmentIdByStepId(stepId: Long): Long {
        val assignment = mAssignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId.toString())
        return assignment?.id ?: -1;
    }

    fun getMapFromStepIdToTheirLesson(stepIds: LongArray?): Map<Long, Lesson> {
        val result = HashMap<Long, Lesson>()
        stepIds?.let {
            val lessonSet = HashSet<Long>()

            DbParseHelper.parseLongArrayToString(stepIds, ",")?.let {
                val steps = mStepDao.getAllInRange(DbStructureStep.Column.STEP_ID, it)
                for (step in steps) {
                    lessonSet.add(step.lesson)
                }

                val lessonIds = lessonSet.toLongArray()
                val lessonIdsCommaSeparated = DbParseHelper.parseLongArrayToString(lessonIds, ",")
                lessonIdsCommaSeparated?.let {
                    val lessonCollection = mLessonDao.getAllInRange(DbStructureLesson.Column.LESSON_ID, lessonIdsCommaSeparated).toHashSet()
                    for (stepItem in steps) {
                        lessonCollection
                                .find { it.id == stepItem.lesson }
                                ?.let { result.put(stepItem.id, it) }
                    }
                }
            }
        }
        return result
    }

    fun getStepById(stepId: Long) = mStepDao.get(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getLessonById(lessonId: Long) = mLessonDao.get(DbStructureLesson.Column.LESSON_ID, lessonId.toString())

    fun getSectionById(sectionId: Long) = mSectionDao.get(DbStructureSections.Column.SECTION_ID, sectionId.toString())

    fun getCourseById(courseId: Long, type: Table) = getCourseDao(type).get(DBStructureCourses.Column.COURSE_ID, courseId.toString())

    fun getProgressById(progressId: String) = mProgressDao.get(DbStructureProgress.Column.ID, progressId)

    @Deprecated("Lesson can have a lot of units", ReplaceWith("try to get unit from section"))
    fun getUnitByLessonId(lessonId: Long) = mUnitDao.get(DbStructureUnit.Column.LESSON, lessonId.toString())

    fun getUnitById(unitId: Long) = mUnitDao.get(DbStructureUnit.Column.UNIT_ID, unitId.toString())

    fun getAllDownloadEntities() = mDownloadEntityDao.getAll()

    fun isUnitCached(unit: Unit?): Boolean {
        val id = unit?.id ?: return false
        val dbUnit = mUnitDao.get(DbStructureUnit.Column.UNIT_ID, id.toString())
        return dbUnit != null && dbUnit.is_cached
    }

    fun isLessonCached(lesson: Lesson?): Boolean {
        val id = lesson?.id ?: return false
        val dbLesson = mLessonDao.get(DbStructureLesson.Column.LESSON_ID, id.toString())
        return dbLesson != null && dbLesson.is_cached
    }

    fun isStepCached(step: Step?): Boolean {
        val id = step?.id ?: return false
        return isStepCached(id)
    }

    fun isStepCached(stepId: Long): Boolean {
        val dbStep = mStepDao.get(DbStructureStep.Column.STEP_ID, stepId.toString())
        return dbStep != null && dbStep.is_cached
    }

    fun updateOnlyCachedLoadingStep(step: Step?) {
        step?.let {
            val cv = ContentValues()
            cv.put(DbStructureStep.Column.IS_LOADING, step.is_loading)
            cv.put(DbStructureStep.Column.IS_CACHED, step.is_cached)
            mStepDao.update(DbStructureStep.Column.STEP_ID, step.id.toString(), cv)
        }
    }

    fun updateOnlyCachedLoadingUnit(unit: Unit?) {
        unit?.let {
            val cv = ContentValues()
            cv.put(DbStructureUnit.Column.IS_LOADING, unit.is_loading)
            cv.put(DbStructureUnit.Column.IS_CACHED, unit.is_cached)
            mUnitDao.update(DbStructureUnit.Column.UNIT_ID, unit.id.toString(), cv)
        }
    }

    fun updateOnlyCachedLoadingLesson(lesson: Lesson?) {
        lesson?.let {
            val cv = ContentValues()
            cv.put(DbStructureLesson.Column.IS_LOADING, lesson.is_loading)
            cv.put(DbStructureLesson.Column.IS_CACHED, lesson.is_cached)
            mLessonDao.update(DbStructureLesson.Column.LESSON_ID, lesson.id.toString(), cv)
        }
    }

    fun updateOnlyCachedLoadingSection(section: Section?) {
        section?.let {
            val cv = ContentValues()
            cv.put(DbStructureSections.Column.IS_LOADING, section.is_loading)
            cv.put(DbStructureSections.Column.IS_CACHED, section.is_cached)
            mSectionDao.update(DbStructureSections.Column.SECTION_ID, section.id.toString(), cv)
        }
    }

    @Deprecated("")
    fun updateOnlyCachedLoadingCourse(course: Course?, type: Table) {
        course?.let {
            val cv = ContentValues()
            cv.put(DBStructureCourses.Column.IS_LOADING, course.is_loading)
            cv.put(DBStructureCourses.Column.IS_CACHED, course.is_cached)
            getCourseDao(type).update(DBStructureCourses.Column.COURSE_ID, course.courseId.toString(), cv)
        }
    }

    fun getAllCourses(type: Table) = getCourseDao(type).getAll()

    fun addCourse(course: Course, type: Table) = getCourseDao(type).insertOrUpdate(course)

    fun deleteCourse(course: Course, type: Table) {
        getCourseDao(type).delete(DBStructureCourses.Column.COURSE_ID, course.courseId.toString())
    }

    fun addSection(section: Section) = mSectionDao.insertOrUpdate(section)

    fun addStep(step: Step) = mStepDao.insertOrUpdate(step)

    fun getAllSectionsOfCourse(course: Course) = mSectionDao.getAll(DbStructureSections.Column.COURSE, course.courseId.toString())

    fun getAllUnitsOfSection(sectionId: Long) = mUnitDao.getAll(DbStructureUnit.Column.SECTION, sectionId.toString())

    fun getStepsOfLesson(lessonId: Long) = mStepDao.getAll(DbStructureStep.Column.LESSON_ID, lessonId.toString())

    fun getLessonOfUnit(unit: Unit?): Lesson? {
        if (unit != null) {
            return mLessonDao.get(DbStructureLesson.Column.LESSON_ID, unit.lesson.toString())
        } else {
            return null
        }
    }


    fun addVideo(cachedVideo: CachedVideo?) = cachedVideo?.let { mCachedVideoDao.insertOrUpdate(cachedVideo) }

    fun deleteDownloadEntityByDownloadId(downloadId: Long) =
            mDownloadEntityDao.delete(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId.toString())

    fun isExistDownloadEntityByVideoId(videoId: Long) =
            mDownloadEntityDao.isInDb(DbStructureSharedDownloads.Column.VIDEO_ID, videoId.toString())

    fun deleteVideo(video: Video) =
            deleteVideo(video.id)

    fun deleteVideo(videoId: Long) =
            mCachedVideoDao.delete(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun deleteVideoByUrl(path: String?) = path?.let { mCachedVideoDao.delete(DbStructureCachedVideo.Column.URL, path) }

    fun deleteStep(step: Step?) {
        val stepId = step?.id ?: return
        deleteStepById(stepId)
    }

    fun deleteStepById(stepId: Long) = mStepDao.delete(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getCachedVideoById(videoId: Long) = mCachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun getAllCachedVideos() = mCachedVideoDao.getAll()

    /**
     * getPath of cached video

     * @param video video which we check for contains in db
     * *
     * @return null if video not existing in db, otherwise path to disk
     */
    fun getPathToVideoIfExist(video: Video): String? {
        val cachedVideo = mCachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, video.id.toString())
        return cachedVideo?.url
    }

    fun getDownloadEntityIfExist(downloadId: Long?): DownloadEntity? {
        downloadId ?: return null
        return mDownloadEntityDao.get(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId.toString())
    }

    fun clearCacheCourses(type: DatabaseFacade.Table) {
        val courses = getAllCourses(type)

        for (courseItem in courses) {
            courseItem?.let {
                deleteCourse(courseItem, type)
            }
        }
    }

    fun addUnit(unit: Unit) = mUnitDao.insertOrUpdate(unit)

    fun addDownloadEntity(downloadEntity: DownloadEntity) = mDownloadEntityDao.insertOrUpdate(downloadEntity)

    fun addLesson(lesson: Lesson) = mLessonDao.insertOrUpdate(lesson)

    fun addToQueueViewedState(viewState: ViewAssignment) = mViewAssignmentDao.insertOrUpdate(viewState)

    val allInQueue: List<ViewAssignment?> = mViewAssignmentDao.getAll()

    fun getAllNotification(): List<Notification?> = mNotificationDao.getAll()

    fun addNotification(notification: Notification) {
        mNotificationDao.insertOrUpdate(notification)
    }

    fun removeNotification(id: Long) {
        mNotificationDao.delete(DbStructureNotification.Column.ID, id.toString())
    }

    fun removeAllNotifications() {
        val notifications = getAllNotification()

        for (notificationItem in notifications) {
            notificationItem?.id?.let {
                removeNotification(it)
            }
        }
    }

    fun removeAllNotificationsByCourseId(courseId: Long) {
        mNotificationDao.delete(DbStructureNotification.Column.COURSE_ID, courseId.toString())
    }

    fun removeFromQueue(viewAssignmentWrapper: ViewAssignment?) {
        val assignmentId = viewAssignmentWrapper?.assignment ?: return
        mViewAssignmentDao.delete(DbStructureViewQueue.Column.ASSIGNMENT_ID, assignmentId.toString())
    }

    fun markProgressAsPassed(assignmentId: Long) {
        val assignment = mAssignmentDao.get(DbStructureAssignment.Column.ASSIGNMENT_ID, assignmentId.toString())
        val progressId = assignment?.progress ?: return
        markProgressAsPassedIfInDb(progressId)
    }

    fun markProgressAsPassedIfInDb(progressId: String) {
        val inDb = mProgressDao.isInDb(DbStructureProgress.Column.ID, progressId)
        if (inDb) {
            val values = ContentValues()
            values.put(DbStructureProgress.Column.IS_PASSED, true)
            mProgressDao.update(DbStructureProgress.Column.ID, progressId, values)
        }
    }

    fun addProgress(progress: Progress) = mProgressDao.insertOrUpdate(progress)

    fun isProgressViewed(progressId: String?): Boolean {
        if (progressId == null) return false
        val progress = mProgressDao.get(DbStructureProgress.Column.ID, progressId)
        return progress?.is_passed ?: false
    }

    fun isStepPassed(stepId: Long): Boolean {
        val assignment = mAssignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId.toString()) ?: return false
        val progressId = assignment.progress
        return isProgressViewed(progressId)
    }

    fun getAllNotificationsOfCourse(courseId: Long): MutableList<Notification?> {
        return mNotificationDao.getAll(DbStructureNotification.Column.COURSE_ID, courseId.toString())
    }

    fun getDownloadEntityByStepId(stepId: Long) = mDownloadEntityDao.get(DbStructureSharedDownloads.Column.STEP_ID, stepId.toString())

    fun getAllDownloadingUnits(): LongArray {
        val units = mUnitDao.getAll(DbStructureUnit.Column.IS_LOADING, 1.toString())
        val unitIds = units.map { it?.id }.filterNotNull()
        return unitIds.toLongArray()
    }

    fun getAllDownloadingSections(): LongArray {
        val sections = mSectionDao.getAll(DbStructureSections.Column.IS_LOADING, 1.toString())
        val sectionIds = sections.map { it?.id }.filterNotNull()
        return sectionIds.toLongArray()
    }

    fun dropOnlyCourseTable() {
        mCoursesEnrolledDao.removeAll()
        mCoursesFeaturedDao.removeAll()
    }

    fun getCalendarSectionsByIds(ids: LongArray): Map<Long, CalendarSection> {
        val stringIds = DbParseHelper.parseLongArrayToString(ids)
        if (stringIds != null) {
            return calendarSectionDao
                    .getAllInRange(DbStructureCalendarSection.Column.SECTION_ID, stringIds)
                    .map { it.id to it }
                    .toMap()
        } else {
            return HashMap<Long, CalendarSection>()
        }
    }
}