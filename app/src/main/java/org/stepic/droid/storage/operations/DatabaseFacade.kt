package org.stepic.droid.storage.operations

import android.content.ContentValues
import org.stepic.droid.adaptive.model.LocalExpItem
import org.stepic.droid.di.storage.StorageSingleton
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDao
import org.stepic.droid.model.*
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.dao.AdaptiveExpDao
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDao
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.CourseListDao
import org.stepic.droid.storage.dao.SearchQueryDao
import org.stepic.droid.storage.structure.*
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.web.ViewAssignment
import org.stepik.android.cache.section.structure.DbStructureSection
import org.stepik.android.cache.unit.structure.DbStructureUnit
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.video_player.model.VideoTimestamp
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.*
import org.stepik.android.model.Unit
import javax.inject.Inject

@StorageSingleton
class DatabaseFacade
@Inject
constructor(
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
    private val stepDao: IDao<Step>,
    private val courseDao: IDao<Course>,
    private val courseListDao: CourseListDao,
    private val notificationDao: IDao<Notification>,
    private val calendarSectionDao: IDao<CalendarSection>,
    private val certificateViewItemDao: IDao<CertificateViewItem>,
    private val videoTimestampDao: IDao<VideoTimestamp>,
    private val lastStepDao: IDao<LastStep>,
    private val blockDao: IDao<BlockPersistentWrapper>,
    private val personalDeadlinesDao: PersonalDeadlinesDao,
    private val deadlinesBannerDao: DeadlinesBannerDao,
    private val viewedStoryTemplatesDao: IDao<ViewedStoryTemplate>
) {

    fun dropDatabase() {
        sectionDao.removeAll()
        unitDao.removeAll()
        progressDao.removeAll()
        lessonDao.removeAll()
        viewAssignmentDao.removeAll()
        viewedNotificationsQueueDao.removeAll()
        stepDao.removeAll()
        courseDao.removeAll()
        courseListDao.removeAll()
        notificationDao.removeAll()
        certificateViewItemDao.removeAll()
        lastStepDao.removeAll()
        blockDao.removeAll()
        videoTimestampDao.removeAll()
        assignmentDao.removeAll()
        codeSubmissionDao.removeAll()
        searchQueryDao.removeAll()
        adaptiveExpDao.removeAll()
        personalDeadlinesDao.removeAll()
        deadlinesBannerDao.removeAll()
        viewedStoryTemplatesDao.removeAll()
    }

    fun addAssignment(assignment: Assignment?) = assignment?.let { assignmentDao.insertOrUpdate(assignment) }

    @Deprecated("because of step has 0..* assignments.")
    fun getAssignmentIdByStepId(stepId: Long): Long {
        val assignment = assignmentDao.get(DbStructureAssignment.Column.STEP_ID, stepId.toString())
        return assignment?.id ?: -1
    }

    fun getStepById(stepId: Long) = stepDao.get(DbStructureStep.Column.STEP_ID, stepId.toString())

    fun getStepsById(stepIds: List<Long>): List<Step> = getStepsById(stepIds.toLongArray())

    fun getStepsById(stepIds: LongArray): List<Step> {
        val stringIds = DbParseHelper.parseLongArrayToString(stepIds, AppConstants.COMMA)
        return if (stringIds != null) {
            stepDao.getAllInRange(DbStructureStep.Column.STEP_ID, stringIds)
        } else {
            emptyList()
        }
    }

    fun getLessonById(lessonId: Long) = lessonDao.get(DbStructureLesson.Columns.ID, lessonId.toString())

    fun getSectionById(sectionId: Long) = sectionDao.get(DbStructureSection.Columns.ID, sectionId.toString())

    fun getCourseById(courseId: Long) = courseDao.get(DbStructureCourse.Columns.ID, courseId.toString())

    fun getProgressById(progressId: String) = progressDao.get(DbStructureProgress.Columns.ID, progressId)

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
            progressDao.getAllInRange(DbStructureProgress.Columns.ID, range)
        }
    }

    fun getUnitsByLessonId(lessonId: Long): List<Unit> =
        unitDao.getAll(DbStructureUnit.Columns.LESSON, lessonId.toString())

    fun getUnitById(unitId: Long) =
        unitDao.get(DbStructureUnit.Columns.ID, unitId.toString())

    fun getAllCourses(courseListType: CourseListType) =
        courseListDao.getCourseList(courseListType)

    fun addCourse(course: Course) =
        courseDao.insertOrReplace(course)

    fun deleteCourse(courseId: Long) =
        courseDao.remove(DbStructureCourse.Columns.ID, courseId.toString())

    fun addCourseList(courseListType: CourseListType, courses: List<Course>) =
        courseListDao.addCourseList(courseListType, courses)

    fun addCourseToList(courseListType: CourseListType, courseId: Long) =
        courseListDao.addCourseToList(courseListType, courseId)

    fun deleteCourseFromList(courseListType: CourseListType, courseId: Long) =
        courseListDao.removeCourseFromList(courseListType, courseId)

    fun addSection(section: Section) =
        sectionDao.insertOrUpdate(section)

    fun addSections(sections: List<Section>) =
        sectionDao.insertOrReplaceAll(sections)

    fun addStep(step: Step) = stepDao.insertOrUpdate(step)

    fun getStepsOfLesson(lessonId: Long) = stepDao.getAll(DbStructureStep.Column.LESSON_ID, lessonId.toString())

    fun addUnit(unit: Unit) =
        unitDao.insertOrUpdate(unit)

    fun addUnits(units: List<Unit>) =
        unitDao.insertOrReplaceAll(units)

    fun addLesson(lesson: Lesson) =
        lessonDao.insertOrUpdate(lesson)

    fun addLessons(lessons: List<Lesson>) =
        lessonDao.insertOrReplaceAll(lessons)

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
        val inDb = progressDao.isInDb(DbStructureProgress.Columns.ID, progressId)
        if (inDb) {
            val values = ContentValues()
            values.put(DbStructureProgress.Columns.IS_PASSED, true)
            progressDao.update(DbStructureProgress.Columns.ID, progressId, values)
        }
    }

    fun addProgress(progress: Progress) =
        progressDao.insertOrUpdate(progress)

    fun addProgresses(progresses: List<Progress>) =
        progressDao.insertOrReplaceAll(progresses)

    fun isProgressViewed(progressId: String?): Boolean {
        if (progressId == null) return false
        val progress = progressDao.get(DbStructureProgress.Columns.ID, progressId)
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

    fun getAllNotificationsOfCourse(courseId: Long): List<Notification> =
        notificationDao
            .getAll(DbStructureNotification.Column.COURSE_ID, courseId.toString())

    fun dropOnlyCourseTable() {
        courseDao.removeAll()
    }

    fun dropEnrolledCourses() {
        courseListDao.removeCourseList(CourseListType.ENROLLED)
    }

    fun dropFeaturedCourses() {
        courseListDao.removeCourseList(CourseListType.FEATURED)
    }

    fun getLessonsByIds(lessonIds: LongArray): List<Lesson> {
        val stringIds = DbParseHelper.parseLongArrayToString(lessonIds, AppConstants.COMMA)
        return if (stringIds != null) {
            lessonDao
                    .getAllInRange(DbStructureLesson.Columns.ID, stringIds)
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

    fun addTimestamp(videoTimestamp: VideoTimestamp) {
        videoTimestampDao.insertOrUpdate(videoTimestamp)
    }

    fun getVideoTimestamp(videoId: Long): VideoTimestamp? =
            videoTimestampDao.get(DbStructureVideoTimestamp.Column.VIDEO_ID, videoId.toString())

    fun updateLastStep(lastStep: LastStep) {
        lastStepDao.insertOrUpdate(lastStep)
    }

    fun getLocalLastStepById(lastStepId: String?): LastStep? =
            lastStepId?.let { lastStepDao.get(DbStructureLastStep.Columns.ID, it) }

    fun getUnitsByIds(keys: List<Long>): List<Unit> {
        DbParseHelper.parseLongListToString(keys, AppConstants.COMMA)?.let {
            return unitDao.getAllInRange(DbStructureUnit.Columns.ID, it)
        }

        return emptyList()
    }

    fun getSectionsByIds(keys: LongArray): List<Section> {
        DbParseHelper.parseLongArrayToString(keys, AppConstants.COMMA)?.let {
            return sectionDao.getAllInRange(DbStructureSection.Columns.ID, it)
        }

        return emptyList()
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