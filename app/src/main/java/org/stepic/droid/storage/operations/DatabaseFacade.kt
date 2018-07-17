package org.stepic.droid.storage.operations

import android.content.ContentValues
import org.stepic.droid.adaptive.model.LocalExpItem
import org.stepic.droid.di.qualifiers.EnrolledCoursesDaoQualifier
import org.stepic.droid.di.qualifiers.FeaturedCoursesDaoQualifier
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.features.deadlines.storage.dao.DeadlinesBannerDao
import org.stepic.droid.model.*
import org.stepik.android.model.structure.Unit
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.dao.AdaptiveExpDao
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.features.deadlines.storage.dao.PersonalDeadlinesDao
import org.stepic.droid.storage.dao.SearchQueryDao
import org.stepic.droid.storage.structure.*
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.web.ViewAssignment
import org.stepik.android.model.learning.Assignment
import org.stepik.android.model.structure.Course
import org.stepik.android.model.structure.Progress
import org.stepik.android.model.structure.Video
import java.util.*
import javax.inject.Inject

@StorageSingleton
class DatabaseFacade
@Inject constructor(
        private val stepInfoOperation: StepInfoOperation,
        private val codeSubmissionDao: IDao<CodeSubmission>,
        private val searchQueryDao: SearchQueryDao,
        private val adaptiveExpDao: AdaptiveExpDao,
        private val viewedNotificationsQueueDao: IDao<ViewedNotification>,
        private val sectionDao: IDao<Section>,
        private val unitDao: IDao<Unit>,
        private val progressDao: IDao<Progress>,
        private val assignmentDao: IDao<Assignment>,
        private val lessonDao: IDao<Lesson>,
        private val viewAssignmentDao: IDao<ViewAssignment>,
        private val downloadEntityDao: IDao<DownloadEntity>,
        private val cachedVideoDao: IDao<CachedVideo>,
        private val stepDao: IDao<Step>,
        @EnrolledCoursesDaoQualifier
        private val coursesEnrolledDao: IDao<Course>,
        @FeaturedCoursesDaoQualifier
        private val coursesFeaturedDao: IDao<Course>,
        private val notificationDao: IDao<Notification>,
        private val calendarSectionDao: IDao<CalendarSection>,
        private val certificateViewItemDao: IDao<CertificateViewItem>,
        private val videoTimestampDao: IDao<VideoTimestamp>,
        private val lastStepDao: IDao<PersistentLastStep>,
        private val externalVideoUrlDao: IDao<DbVideoUrl>,
        private val blockDao: IDao<BlockPersistentWrapper>,
        private val personalDeadlinesDao: PersonalDeadlinesDao,
        private val deadlinesBannerDao: DeadlinesBannerDao
) {

    fun dropDatabase() {
        sectionDao.removeAll()
        unitDao.removeAll()
        progressDao.removeAll()
        lessonDao.removeAll()
        viewAssignmentDao.removeAll()
        viewedNotificationsQueueDao.removeAll()
        downloadEntityDao.removeAll()
        cachedVideoDao.removeAll()
        stepDao.removeAll()
        coursesEnrolledDao.removeAll()
        coursesFeaturedDao.removeAll()
        notificationDao.removeAll()
        certificateViewItemDao.removeAll()
        lastStepDao.removeAll()
        blockDao.removeAll()
        videoTimestampDao.removeAll()
        externalVideoUrlDao.removeAll()
        assignmentDao.removeAll()
        codeSubmissionDao.removeAll()
        searchQueryDao.removeAll()
        adaptiveExpDao.removeAll()
        personalDeadlinesDao.removeAll()
        deadlinesBannerDao.removeAll()
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

    fun getStepsById(stepIds: List<Long>): List<Step> = getStepsById(stepIds.toLongArray())

    fun getPublishProgressStepInfoByIds(stepIds: List<Long>): List<StepInfo> = stepInfoOperation.getStepInfo(stepIds)

    fun getStepsById(stepIds: LongArray): List<Step> {
        val stringIds = DbParseHelper.parseLongArrayToString(stepIds, AppConstants.COMMA)
        return if (stringIds != null) {
            stepDao
                    .getAllInRange(DbStructureStep.Column.STEP_ID, stringIds)
        } else {
            ArrayList<Step>()
        }
    }

    fun getLessonById(lessonId: Long) = lessonDao.get(DbStructureLesson.Column.LESSON_ID, lessonId.toString())

    fun getSectionById(sectionId: Long) = sectionDao.get(DbStructureSections.Column.SECTION_ID, sectionId.toString())

    fun getCourseById(courseId: Long, type: Table) = getCourseDao(type).get(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID, courseId.toString())

    fun getProgressById(progressId: String) = progressDao.get(DbStructureProgress.Column.ID, progressId)

    fun getProgresses(progressIds: List<String>): List<Progress> {
        //todo change implementation of getAllInRange and escape internally
        val escapedIds = progressIds
                .map {
                    "\"$it\""
                }
                .toTypedArray()
        val range = DbParseHelper.parseStringArrayToString(escapedIds, AppConstants.COMMA)
        return if (range == null) {
            emptyList()
        } else {
            progressDao.getAllInRange(DbStructureProgress.Column.ID, range)
        }
    }

    @Deprecated("Lesson can have a lot of units", ReplaceWith("try to get unit from section"))
    fun getUnitByLessonId(lessonId: Long) = unitDao.get(DbStructureUnit.Column.LESSON, lessonId.toString())

    fun getUnitById(unitId: Long) = unitDao.get(DbStructureUnit.Column.UNIT_ID, unitId.toString())

    fun getAllDownloadEntities() = downloadEntityDao.getAll()

    fun getDownloadEntitiesBy(stepIds: LongArray): List<DownloadEntity> {
        val stringIds = DbParseHelper.parseLongArrayToString(stepIds, AppConstants.COMMA)
        return if (stringIds != null) {
            downloadEntityDao
                    .getAllInRange(DbStructureSharedDownloads.Column.STEP_ID, stringIds)
        } else {
            emptyList()
        }
    }

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
            cv.put(DbStructureSections.Column.IS_LOADING, section.isLoading)
            cv.put(DbStructureSections.Column.IS_CACHED, section.isCached)
            sectionDao.update(DbStructureSections.Column.SECTION_ID, section.id.toString(), cv)
        }
    }

    fun getAllCourses(type: Table) = getCourseDao(type).getAll()

    fun addCourse(course: Course, type: Table) = getCourseDao(type).insertOrUpdate(course)

    fun deleteCourse(course: Course, type: Table) {
        getCourseDao(type).remove(DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID, course.id.toString())
    }

    fun addSection(section: Section) = sectionDao.insertOrUpdate(section)

    fun addStep(step: Step) = stepDao.insertOrUpdate(step)

    fun getAllSectionsOfCourse(course: Course) = sectionDao.getAll(DbStructureSections.Column.COURSE, course.id.toString())

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
            downloadEntityDao.remove(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadId.toString())

    fun isExistDownloadEntityByVideoId(videoId: Long) =
            downloadEntityDao.isInDb(DbStructureSharedDownloads.Column.VIDEO_ID, videoId.toString())

    fun deleteVideo(video: Video) =
            deleteVideo(video.id)

    fun deleteVideo(videoId: Long) =
            cachedVideoDao.remove(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun deleteVideoByUrl(path: String?) = path?.let { cachedVideoDao.remove(DbStructureCachedVideo.Column.URL, path) }

    fun deleteStep(step: Step?) {
        val stepId = step?.id ?: return
        deleteStepById(stepId)
    }

    fun deleteStepById(stepId: Long) = stepDao.remove(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getCachedVideoById(videoId: Long) = cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, videoId.toString())

    fun getAllCachedVideos() = cachedVideoDao.getAll()

    fun getCachedVideoIfExist(video: Video): CachedVideo? =
            cachedVideoDao.get(DbStructureCachedVideo.Column.VIDEO_ID, video.id.toString())

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

    fun addNotification(notification: Notification) {
        notificationDao.insertOrUpdate(notification)
    }

    fun removeAllNotificationsWithCourseId(courseId: Long) {
        notificationDao.remove(DbStructureNotification.Column.COURSE_ID, courseId.toString())
    }

    fun removeFromQueue(viewAssignmentWrapper: ViewAssignment?) {
        val assignmentId = viewAssignmentWrapper?.assignment ?: return
        viewAssignmentDao.remove(DbStructureViewQueue.Column.ASSIGNMENT_ID, assignmentId.toString())
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
        return progress?.isPassed ?: false
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

    fun dropOnlyCourseTable() {
        coursesEnrolledDao.removeAll()
        coursesFeaturedDao.removeAll()
    }

    fun dropEnrolledCourses() {
        coursesEnrolledDao.removeAll()
    }

    fun dropFeaturedCourses() {
        coursesFeaturedDao.removeAll()
    }

    fun getLessonsByIds(lessonIds: LongArray): List<Lesson> {
        val stringIds = DbParseHelper.parseLongArrayToString(lessonIds, AppConstants.COMMA)
        return if (stringIds != null) {
            lessonDao
                    .getAllInRange(DbStructureLesson.Column.LESSON_ID, stringIds)
        } else {
            emptyList()
        }
    }

    fun getCalendarSectionsByIds(ids: LongArray): Map<Long, CalendarSection> {
        val stringIds = DbParseHelper.parseLongArrayToString(ids, AppConstants.COMMA)
        return if (stringIds != null) {
            calendarSectionDao
                    .getAllInRange(DbStructureCalendarSection.Column.SECTION_ID, stringIds)
                    .map { it.id to it }
                    .toMap()
        } else {
            emptyMap()
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
        sectionDao.remove(DbStructureSections.Column.COURSE, courseId.toString());
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

    fun getUnitsByIds(keys: LongArray): List<Unit> {
        DbParseHelper.parseLongArrayToString(keys, AppConstants.COMMA)?.let {
            return unitDao.getAllInRange(DbStructureUnit.Column.UNIT_ID, it)
        }

        return emptyList()
    }

    fun getSectionsByIds(keys: LongArray): List<Section> {
        DbParseHelper.parseLongArrayToString(keys, AppConstants.COMMA)?.let {
            return sectionDao.getAllInRange(DbStructureSections.Column.SECTION_ID, it)
        }

        return ArrayList<Section>()
    }

    fun insertOrUpdateExternalVideoList(videoId: Long, videoUrlList: List<DbVideoUrl>) {
        //remove all related with this video and write new
        externalVideoUrlDao.remove(DbStructureVideoUrl.Column.videoId, videoId.toString())
        videoUrlList.forEach {
            externalVideoUrlDao.insertOrUpdate(it)
        }
    }

    fun getExternalVideoUrls(videoId: Long): List<DbVideoUrl> {
        return externalVideoUrlDao
                .getAll(DbStructureVideoUrl.Column.videoId, videoId.toString())
                .filterNotNull()
    }

    fun getCodeSubmission(attemptId: Long): CodeSubmission? =
            codeSubmissionDao.get(DbStructureCodeSubmission.Column.ATTEMPT_ID, attemptId.toString())

    fun removeCodeSubmissionsOfStep(stepId: Long) {
        codeSubmissionDao.remove(DbStructureCodeSubmission.Column.STEP_ID, stepId.toString())
    }

    fun addCodeSubmission(codeSubmission: CodeSubmission) {
        codeSubmissionDao.insertOrUpdate(codeSubmission)
    }

    fun getSearchQueries(constraint: String, count: Int) =
            searchQueryDao.getSearchQueries(constraint, count)

    fun addSearchQuery(searchQuery: SearchQuery) {
        searchQueryDao.insertOrReplace(searchQuery)
    }

    fun addToViewedNotificationsQueue(viewedNotification: ViewedNotification) {
        viewedNotificationsQueueDao.insertOrReplace(viewedNotification)
    }

    fun getViewedNotificationsQueue() = viewedNotificationsQueueDao.getAll()

    fun removeViewedNotification(viewedNotification: ViewedNotification) {
        viewedNotificationsQueueDao.remove(
                DbStructureViewedNotificationsQueue.Column.NOTIFICATION_ID,
                viewedNotification.notificationId.toString())
    }

    fun syncExp(courseId: Long, apiExp: Long): Long {
        val localExp = getExpForCourse(courseId)
        val diff = apiExp - localExp
        if (diff > 0) {
            val syncRecord = adaptiveExpDao.getExpItem(courseId, 0)?.exp ?: 0
            adaptiveExpDao.insertOrReplace(LocalExpItem(syncRecord + diff, 0, courseId))

            return getExpForCourse(courseId)
        }

        return localExp
    }

    fun getExpForCourse(courseId: Long) = adaptiveExpDao.getExpForCourse(courseId)

    fun getStreakForCourse(courseId: Long) = adaptiveExpDao.getExpItem(courseId)?.exp ?: 0

    fun addLocalExpItem(exp: Long, submissionId: Long, courseId: Long) {
        adaptiveExpDao.insertOrReplace(LocalExpItem(exp, submissionId, courseId))
    }

    fun getExpForLast7Days(courseId: Long) = adaptiveExpDao.getExpForLast7Days(courseId)

    fun getExpForWeeks(courseId: Long) = adaptiveExpDao.getExpForWeeks(courseId)
}