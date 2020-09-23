package org.stepic.droid.storage.operations

import org.stepic.droid.adaptive.model.LocalExpItem
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.model.BlockPersistentWrapper
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.ViewedNotification
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.dao.AdaptiveExpDao
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.storage.dao.SearchQueryDao
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepic.droid.storage.structure.DbStructureLastStep
import org.stepic.droid.storage.structure.DbStructureNotification
import org.stepic.droid.storage.structure.DbStructureProgress
import org.stepic.droid.storage.structure.DbStructureVideoTimestamp
import org.stepic.droid.storage.structure.DbStructureViewQueue
import org.stepic.droid.storage.structure.DbStructureViewedNotificationsQueue
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepik.android.cache.assignment.structure.DbStructureAssignment
import org.stepik.android.cache.course_calendar.structure.DbStructureSectionDateEvent
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDao
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDao
import org.stepik.android.cache.purchase_notification.dao.PurchaseNotificationDao
import org.stepik.android.cache.section.structure.DbStructureSection
import org.stepik.android.cache.step.structure.DbStructureStep
import org.stepik.android.cache.unit.structure.DbStructureUnit
import org.stepik.android.cache.user_courses.dao.UserCourseDao
import org.stepik.android.cache.video_player.model.VideoTimestamp
import org.stepik.android.data.course_list.model.CourseListQueryData
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Assignment
import org.stepik.android.model.Certificate
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.SocialProfile
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import org.stepik.android.model.ViewAssignment
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.DiscussionThread
import javax.inject.Inject

@StorageSingleton
class DatabaseFacade
@Inject
constructor(
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
    private val notificationDao: IDao<Notification>,
    private val videoTimestampDao: IDao<VideoTimestamp>,
    private val lastStepDao: IDao<LastStep>,
    private val blockDao: IDao<BlockPersistentWrapper>,
    private val personalDeadlinesDao: PersonalDeadlinesDao,
    private val deadlinesBannerDao: DeadlinesBannerDao,
    private val viewedStoryTemplatesDao: IDao<ViewedStoryTemplate>,
    private val sectionDateEventDao: IDao<SectionDateEvent>,
    private val submissionDao: IDao<Submission>,
    private val certificateDao: IDao<Certificate>,
    private val discussionThreadDao: IDao<DiscussionThread>,
    private val attemptDao: IDao<Attempt>,
    private val socialProfileDao: IDao<SocialProfile>,
    private val userCourseDao: UserCourseDao,
    private val courseCollectionDao: IDao<CourseCollection>,
    private val courseListQueryDataDao: IDao<CourseListQueryData>,
    private val purchaseNotificationDao: PurchaseNotificationDao,
    private val coursePaymentDao: IDao<CoursePayment>
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
        notificationDao.removeAll()
        lastStepDao.removeAll()
        blockDao.removeAll()
        videoTimestampDao.removeAll()
        assignmentDao.removeAll()
        searchQueryDao.removeAll()
        adaptiveExpDao.removeAll()
        personalDeadlinesDao.removeAll()
        deadlinesBannerDao.removeAll()
        viewedStoryTemplatesDao.removeAll()
        sectionDateEventDao.removeAll()
        submissionDao.removeAll()
        certificateDao.removeAll()
        discussionThreadDao.removeAll()
        attemptDao.removeAll()
        socialProfileDao.removeAll()
        userCourseDao.removeAll()
        courseCollectionDao.removeAll()
        courseListQueryDataDao.removeAll()
        purchaseNotificationDao.removeAll()
        coursePaymentDao.removeAll()
    }

    fun addAssignments(assignments: List<Assignment>) {
        assignmentDao.insertOrReplaceAll(assignments)
    }

    fun getAssignments(assignmentsIds: LongArray): List<Assignment> {
        val stringIds = DbParseHelper.parseLongArrayToString(assignmentsIds, AppConstants.COMMA)
        return if (stringIds != null) {
            assignmentDao.getAllInRange(DbStructureAssignment.Columns.ID, stringIds)
        } else {
            emptyList()
        }
    }

    @Deprecated("because of step has 0..* assignments.")
    fun getAssignmentIdByStepId(stepId: Long): Long {
        val assignment = assignmentDao.get(DbStructureAssignment.Columns.STEP, stepId.toString())
        return assignment?.id ?: -1
    }

    fun getStepsById(stepIds: LongArray): List<Step> {
        val stringIds = DbParseHelper.parseLongArrayToString(stepIds, AppConstants.COMMA)
        return if (stringIds != null) {
            stepDao.getAllInRange(DbStructureStep.Column.ID, stringIds)
        } else {
            emptyList()
        }
    }

    fun getLessonById(lessonId: Long) = lessonDao.get(DbStructureLesson.Columns.ID, lessonId.toString())

    fun getSectionById(sectionId: Long) = sectionDao.get(DbStructureSection.Columns.ID, sectionId.toString())

    fun getCourseById(courseId: Long) = courseDao.get(DbStructureCourse.Columns.ID, courseId.toString())

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

    fun addCourses(courses: List<Course>) {
        courseDao.insertOrReplaceAll(courses)
    }

    fun deleteCourse(courseId: Long) =
        courseDao.remove(DbStructureCourse.Columns.ID, courseId.toString())

    fun deleteCourses() {
        courseDao.removeAll()
    }

    fun addSections(sections: List<Section>) =
        sectionDao.insertOrReplaceAll(sections)

    fun addSteps(steps: List<Step>) {
        stepDao.insertOrReplaceAll(steps)
    }

    fun addUnits(units: List<Unit>) =
        unitDao.insertOrReplaceAll(units)

    fun addLessons(lessons: List<Lesson>) =
        lessonDao.insertOrReplaceAll(lessons)

    fun removeLessons(courseId: Long) {
        lessonDao.removeLike(DbStructureLesson.Columns.COURSES, "%${DbParseHelper.escapeId(courseId.toString())}%")
    }

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

    fun addProgresses(progresses: List<Progress>) =
        progressDao.insertOrReplaceAll(progresses)

    fun getAllNotificationsOfCourse(courseId: Long): List<Notification> =
        notificationDao
            .getAll(DbStructureNotification.Column.COURSE_ID, courseId.toString())

    fun dropOnlyCourseTable() {
        courseDao.removeAll()
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

    fun getSectionDateEvents(vararg sectionIds: Long): List<SectionDateEvent> =
        DbParseHelper.parseLongArrayToString(sectionIds, AppConstants.COMMA)?.let {
            sectionDateEventDao.getAllInRange(DbStructureSectionDateEvent.Columns.SECTION_ID, it)
        } ?: emptyList()

    fun removeSectionDateEvents(vararg sectionIds: Long) =
        DbParseHelper.parseLongArrayToString(sectionIds, AppConstants.COMMA)?.let {
            sectionDateEventDao.removeAllInRange(DbStructureSectionDateEvent.Columns.SECTION_ID, it)
        }
    
    fun addSectionDateEvents(events: List<SectionDateEvent>) {
        sectionDateEventDao.insertOrReplaceAll(events)
    }
}