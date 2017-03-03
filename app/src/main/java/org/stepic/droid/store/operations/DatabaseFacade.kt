package org.stepic.droid.store.operations

import android.content.ContentValues
import org.stepic.droid.base.MainApplication
import org.stepic.droid.model.*
import org.stepic.droid.model.Unit
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.store.dao.IDao
import org.stepic.droid.store.structure.*
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.web.ViewAssignment
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFacade {

    @Inject
    lateinit var sectionDao: IDao<Section>

    @Inject
    lateinit var unitDao: IDao<Unit>

    @Inject
    lateinit var progressDao: IDao<Progress>

    @Inject
    lateinit var assignmentDao: IDao<Assignment>

    @Inject
    lateinit var lessonDao: IDao<Lesson>

    @Inject
    lateinit var viewAssignmentDao: IDao<ViewAssignment>

    @Inject
    lateinit var downloadEntityDao: IDao<DownloadEntity>

    @Inject
    lateinit var cachedVideoDao: IDao<CachedVideo>

    @Inject
    lateinit var stepDao: IDao<Step>

    @Inject
    lateinit var coursesEnrolledDao: IDao<Course>

    @Inject
    lateinit var coursesFeaturedDao: IDao<Course>

    @Inject
    lateinit var notificationDao: IDao<Notification>

    @Inject
    lateinit var calendarSectionDao: IDao<CalendarSection>

    @Inject
    lateinit var certificateViewItemDao: IDao<CertificateViewItem>

    @Inject
    lateinit var videoTimestampDao: IDao<VideoTimestamp>

    @Inject
    lateinit var lastStepDao: IDao<PersistentLastStep>

    @Inject
    lateinit var lastInteractions: IDao<CourseLastInteraction>

    init {
        MainApplication.storageComponent().inject(this)
        coursesEnrolledDao.setTableName(Table.enrolled.storeName)
        coursesFeaturedDao.setTableName(Table.featured.storeName)
    }

    fun dropDatabase() {
        sectionDao.removeAll()
        unitDao.removeAll()
        progressDao.removeAll()
        lessonDao.removeAll()
        viewAssignmentDao.removeAll()
        downloadEntityDao.removeAll()
        cachedVideoDao.removeAll()
        stepDao.removeAll()
        coursesEnrolledDao.removeAll()
        coursesFeaturedDao.removeAll()
        notificationDao.removeAll()
        certificateViewItemDao.removeAll()
        lastStepDao.removeAll()
        lastInteractions.removeAll()
    }

    fun getCourseDao(table: Table) =
            if (table == Table.featured) coursesFeaturedDao
            else coursesEnrolledDao


    fun addAssignment(assignment: Assignment?) = assignment?.let { assignmentDao.insertOrUpdate(assignment) }

    @Deprecated("because of step has 0..* assignments.")
    fun getAssignmentIdByStepId(stepId: Long): Long {
        val assignment = assignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId.toString())
        return assignment?.id ?: -1;
    }

    fun getMapFromStepIdToTheirLesson(stepIds: LongArray?): Map<Long, Lesson> {
        val result = HashMap<Long, Lesson>()
        stepIds?.let {
            val lessonSet = HashSet<Long>()

            DbParseHelper.parseLongArrayToString(stepIds, AppConstants.COMMA)?.let {
                val steps = stepDao.getAllInRange(DbStructureStep.Column.STEP_ID, it)
                for (step in steps) {
                    lessonSet.add(step.lesson)
                }

                val lessonIds = lessonSet.toLongArray()
                val lessonIdsCommaSeparated = DbParseHelper.parseLongArrayToString(lessonIds, AppConstants.COMMA)
                lessonIdsCommaSeparated?.let {
                    val lessonCollection = lessonDao.getAllInRange(DbStructureLesson.Column.LESSON_ID, lessonIdsCommaSeparated).toHashSet()
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

    fun getStepById(stepId: Long) = stepDao.get(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getStepsById(stepIds: List<Long>): List<Step> {
        val stringIds = DbParseHelper.parseLongArrayToString(stepIds.toLongArray(), AppConstants.COMMA)
        if (stringIds != null) {
            return stepDao
                    .getAllInRange(DbStructureStep.Column.STEP_ID, stringIds)
        } else {
            return ArrayList<Step>()
        }
    }

    fun getLessonById(lessonId: Long) = lessonDao.get(DbStructureLesson.Column.LESSON_ID, lessonId.toString())

    fun getSectionById(sectionId: Long) = sectionDao.get(DbStructureSections.Column.SECTION_ID, sectionId.toString())

    fun getCourseById(courseId: Long, type: Table) = getCourseDao(type).get(DBStructureCourses.Column.COURSE_ID, courseId.toString())

    fun getProgressById(progressId: String) = progressDao.get(DbStructureProgress.Column.ID, progressId)

    @Deprecated("Lesson can have a lot of units", ReplaceWith("try to get unit from section"))
    fun getUnitByLessonId(lessonId: Long) = unitDao.get(DbStructureUnit.Column.LESSON, lessonId.toString())

    fun getUnitById(unitId: Long) = unitDao.get(DbStructureUnit.Column.UNIT_ID, unitId.toString())

    fun getAllDownloadEntities() = downloadEntityDao.getAll()

    fun isLessonCached(lesson: Lesson?): Boolean {
        val id = lesson?.id ?: return false
        val dbLesson = lessonDao.get(DbStructureLesson.Column.LESSON_ID, id.toString())
        return dbLesson != null && dbLesson.is_cached
    }

    fun isStepCached(step: Step?): Boolean {
        val id = step?.id ?: return false
        return isStepCached(id)
    }

    fun isStepCached(stepId: Long): Boolean {
        val dbStep = stepDao.get(DbStructureStep.Column.STEP_ID, stepId.toString())
        return dbStep != null && dbStep.is_cached
    }

    fun updateOnlyCachedLoadingStep(step: Step?) {
        step?.let {
            val cv = ContentValues()
            cv.put(DbStructureStep.Column.IS_LOADING, step.is_loading)
            cv.put(DbStructureStep.Column.IS_CACHED, step.is_cached)
            stepDao.update(DbStructureStep.Column.STEP_ID, step.id.toString(), cv)
        }
    }

    fun updateOnlyCachedLoadingLesson(lesson: Lesson?) {
        lesson?.let {
            val cv = ContentValues()
            cv.put(DbStructureLesson.Column.IS_LOADING, lesson.is_loading)
            cv.put(DbStructureLesson.Column.IS_CACHED, lesson.is_cached)
            lessonDao.update(DbStructureLesson.Column.LESSON_ID, lesson.id.toString(), cv)
        }
    }

    fun updateOnlyCachedLoadingSection(section: Section?) {
        section?.let {
            val cv = ContentValues()
            cv.put(DbStructureSections.Column.IS_LOADING, section.is_loading)
            cv.put(DbStructureSections.Column.IS_CACHED, section.is_cached)
            sectionDao.update(DbStructureSections.Column.SECTION_ID, section.id.toString(), cv)
        }
    }

    fun getAllCourses(type: Table) = getCourseDao(type).getAll()

    fun addCourse(course: Course, type: Table) = getCourseDao(type).insertOrUpdate(course)

    fun deleteCourse(course: Course, type: Table) {
        getCourseDao(type).delete(DBStructureCourses.Column.COURSE_ID, course.courseId.toString())
    }

    fun addSection(section: Section) = sectionDao.insertOrUpdate(section)

    fun addStep(step: Step) = stepDao.insertOrUpdate(step)

    fun getAllSectionsOfCourse(course: Course) = sectionDao.getAll(DbStructureSections.Column.COURSE, course.courseId.toString())

    fun getAllUnitsOfSection(sectionId: Long) = unitDao.getAll(DbStructureUnit.Column.SECTION, sectionId.toString())

    fun getStepsOfLesson(lessonId: Long) = stepDao.getAll(DbStructureStep.Column.LESSON_ID, lessonId.toString())

    fun getLessonOfUnit(unit: Unit?): Lesson? {
        if (unit != null) {
            return lessonDao.get(DbStructureLesson.Column.LESSON_ID, unit.lesson.toString())
        } else {
            return null
        }
    }


    fun addVideo(cachedVideo: CachedVideo?) = cachedVideo?.let { cachedVideoDao.insertOrUpdate(cachedVideo) }

    fun deleteDownloadEntityByDownloadId(downloadId: Long) =
            downloadEntityDao.delete(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId.toString())

    fun isExistDownloadEntityByVideoId(videoId: Long) =
            downloadEntityDao.isInDb(DbStructureSharedDownloads.Column.VIDEO_ID, videoId.toString())

    fun deleteVideo(video: Video) =
            deleteVideo(video.id)

    fun deleteVideo(videoId: Long) =
            cachedVideoDao.delete(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun deleteVideoByUrl(path: String?) = path?.let { cachedVideoDao.delete(DbStructureCachedVideo.Column.URL, path) }

    fun deleteStep(step: Step?) {
        val stepId = step?.id ?: return
        deleteStepById(stepId)
    }

    fun deleteStepById(stepId: Long) = stepDao.delete(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getCachedVideoById(videoId: Long) = cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun getAllCachedVideos() = cachedVideoDao.getAll()

    /**
     * getPath of cached video

     * @param video video which we check for contains in db
     * *
     * @return null if video not existing in db, otherwise path to disk
     */
    fun getPathToVideoIfExist(video: Video): String? {
        val cachedVideo = cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, video.id.toString())
        return cachedVideo?.url
    }

    fun getDownloadEntityIfExist(downloadId: Long?): DownloadEntity? {
        downloadId ?: return null
        return downloadEntityDao.get(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId.toString())
    }

    fun clearCacheCourses(type: Table) {
        val courses = getAllCourses(type)

        for (courseItem in courses) {
            courseItem?.let {
                deleteCourse(courseItem, type)
            }
        }
    }

    fun addUnit(unit: Unit) = unitDao.insertOrUpdate(unit)

    fun addDownloadEntity(downloadEntity: DownloadEntity) = downloadEntityDao.insertOrUpdate(downloadEntity)

    fun addLesson(lesson: Lesson) = lessonDao.insertOrUpdate(lesson)

    fun addToQueueViewedState(viewState: ViewAssignment) = viewAssignmentDao.insertOrUpdate(viewState)

    fun getAllInQueue() = viewAssignmentDao.getAll()

    fun getAllNotification(): List<Notification?> = notificationDao.getAll()

    fun addNotification(notification: Notification) {
        notificationDao.insertOrUpdate(notification)
    }

    fun removeNotification(id: Long) {
        notificationDao.delete(DbStructureNotification.Column.ID, id.toString())
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
        notificationDao.delete(DbStructureNotification.Column.COURSE_ID, courseId.toString())
    }

    fun removeFromQueue(viewAssignmentWrapper: ViewAssignment?) {
        val assignmentId = viewAssignmentWrapper?.assignment ?: return
        viewAssignmentDao.delete(DbStructureViewQueue.Column.ASSIGNMENT_ID, assignmentId.toString())
    }

    fun markProgressAsPassed(assignmentId: Long) {
        val assignment = assignmentDao.get(DbStructureAssignment.Column.ASSIGNMENT_ID, assignmentId.toString())
        val progressId = assignment?.progress ?: return
        markProgressAsPassedIfInDb(progressId)
    }

    fun markProgressAsPassedIfInDb(progressId: String) {
        val inDb = progressDao.isInDb(DbStructureProgress.Column.ID, progressId)
        if (inDb) {
            val values = ContentValues()
            values.put(DbStructureProgress.Column.IS_PASSED, true)
            progressDao.update(DbStructureProgress.Column.ID, progressId, values)
        }
    }

    fun addProgress(progress: Progress) = progressDao.insertOrUpdate(progress)

    fun isProgressViewed(progressId: String?): Boolean {
        if (progressId == null) return false
        val progress = progressDao.get(DbStructureProgress.Column.ID, progressId)
        return progress?.is_passed ?: false
    }

    fun isStepPassed(step: Step): Boolean {
        val assignment = assignmentDao.get(DbStructureAssignment.Column.STEP_ID, step.id.toString())
        val progressId: String?
        if (assignment != null) {
            progressId = assignment.progress
        } else {
            progressId = step.progress
        }
        return isProgressViewed(progressId)
    }

    fun getAllNotificationsOfCourse(courseId: Long): MutableList<Notification?> {
        return notificationDao.getAll(DbStructureNotification.Column.COURSE_ID, courseId.toString())
    }

    fun getDownloadEntityByStepId(stepId: Long) = downloadEntityDao.get(DbStructureSharedDownloads.Column.STEP_ID, stepId.toString())

    fun getAllDownloadingLessons(): LongArray {
        val lessons = lessonDao.getAll(DbStructureLesson.Column.IS_LOADING, 1.toString())
        val lessonIds = lessons.map { it?.id }.filterNotNull()
        return lessonIds.toLongArray()
    }

    fun getAllDownloadingSections(): LongArray {
        val sections = sectionDao.getAll(DbStructureSections.Column.IS_LOADING, 1.toString())
        val sectionIds = sections.map { it?.id }.filterNotNull()
        return sectionIds.toLongArray()
    }

    fun dropOnlyCourseTable() {
        coursesEnrolledDao.removeAll()
        coursesFeaturedDao.removeAll()
    }

    fun dropFeaturedCourses() {
        coursesFeaturedDao.removeAll()
    }

    fun getLessonsByIds(lessonIds: LongArray): List<Lesson> {
        val stringIds = DbParseHelper.parseLongArrayToString(lessonIds, AppConstants.COMMA)
        if (stringIds != null) {
            return lessonDao
                    .getAllInRange(DbStructureLesson.Column.LESSON_ID, stringIds)
        } else {
            return ArrayList<Lesson>()
        }
    }

    fun getCalendarSectionsByIds(ids: LongArray): Map<Long, CalendarSection> {
        val stringIds = DbParseHelper.parseLongArrayToString(ids, AppConstants.COMMA)
        if (stringIds != null) {
            return calendarSectionDao
                    .getAllInRange(DbStructureCalendarSection.Column.SECTION_ID, stringIds)
                    .map { it.id to it }
                    .toMap()
        } else {
            return HashMap<Long, CalendarSection>()
        }
    }

    fun addCalendarEvent(calendarSection: CalendarSection) {
        calendarSectionDao.insertOrUpdate(calendarSection)
    }

    fun getCalendarEvent(sectionId: Long) = calendarSectionDao.get(DbStructureCalendarSection.Column.SECTION_ID, sectionId.toString())

    fun addCertificateViewItems(certificates: List<CertificateViewItem?>) {
        certificates
                .filterNotNull()
                .forEach { certificateViewItemDao.insertOrUpdate(it) } //todo change to insertAll
    }

    /**
     * null or not empty oldList
     */
    fun getAllCertificates(): List<CertificateViewItem?>? {
        val list = certificateViewItemDao.getAll()
        if (list.isEmpty()) {
            return null
        } else {
            return list
        }
    }

    fun removeSectionsOfCourse(courseId: Long) {
        sectionDao.delete(DbStructureSections.Column.COURSE, courseId.toString());
    }

    fun addTimestamp(videoTimestamp: VideoTimestamp) {
        videoTimestampDao.insertOrUpdate(videoTimestamp)
    }

    fun getVideoTimestamp(videoId: Long): VideoTimestamp? =
            videoTimestampDao.get(DbStructureVideoTimestamp.Column.VIDEO_ID, videoId.toString())

    fun updateLastStep(persistentLastStep: PersistentLastStep) {
        lastStepDao.insertOrUpdate(persistentLastStep)
    }

    fun getLocalLastStepByCourseId(courseId: Long) =
            lastStepDao.get(DbStructureLastStep.Column.COURSE_ID, courseId.toString())

    fun getAllLocalLastCourseInteraction() =
            lastInteractions.getAll()

    fun updateCourseLastInteraction(courseId: Long, timestamp: Long)
            = lastInteractions.insertOrUpdate(CourseLastInteraction(courseId = courseId, timestamp = timestamp))

}