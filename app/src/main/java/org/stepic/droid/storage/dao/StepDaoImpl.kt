package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepik.android.model.structure.Assignment
import org.stepic.droid.model.BlockPersistentWrapper
import org.stepik.android.model.structure.Progress
import org.stepik.android.model.structure.Step
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureAssignment
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.storage.structure.DbStructureProgress
import org.stepic.droid.storage.structure.DbStructureStep
import org.stepic.droid.util.DbParseHelper
import org.stepik.android.model.structure.Actions

import javax.inject.Inject

class StepDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val blockWrapperDao: IDao<BlockPersistentWrapper>,
        private val assignmentDao: IDao<Assignment>,
        private val progressDao: IDao<Progress>
) : DaoBase<Step>(databaseOperations) {
    private val dateAdapter = UTCDateAdapter()

    public override fun parsePersistentObject(cursor: Cursor): Step {
        val columnIndexCreateDate = cursor.getColumnIndex(DbStructureStep.Column.CREATE_DATE)
        val columnIndexStepId = cursor.getColumnIndex(DbStructureStep.Column.STEP_ID)
        val columnIndexLessonId = cursor.getColumnIndex(DbStructureStep.Column.LESSON_ID)
        val columnIndexStatus = cursor.getColumnIndex(DbStructureStep.Column.STATUS)
        val columnIndexProgress = cursor.getColumnIndex(DbStructureStep.Column.PROGRESS)
        val columnIndexViewedBy = cursor.getColumnIndex(DbStructureStep.Column.VIEWED_BY)
        val columnIndexPassedBy = cursor.getColumnIndex(DbStructureStep.Column.PASSED_BY)
        val columnIndexUpdateDate = cursor.getColumnIndex(DbStructureStep.Column.UPDATE_DATE)
        val columnIndexSubscriptions = cursor.getColumnIndex(DbStructureStep.Column.SUBSCRIPTIONS)
        val columnIndexPosition = cursor.getColumnIndex(DbStructureStep.Column.POSITION)
        val columnIndexIsCached = cursor.getColumnIndex(DbStructureStep.Column.IS_CACHED)
        val columnIndexIsLoading = cursor.getColumnIndex(DbStructureStep.Column.IS_LOADING)
        val columnIndexDiscussionCount = cursor.getColumnIndex(DbStructureStep.Column.DISCUSSION_COUNT)
        val columnIndexDiscussionId = cursor.getColumnIndex(DbStructureStep.Column.DISCUSSION_ID)
        val columnIndexPeerReview = cursor.getColumnIndex(DbStructureStep.Column.PEER_REVIEW)
        val indexHasSubmissionRestriction = cursor.getColumnIndex(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION)
        val indexMaxSubmission = cursor.getColumnIndex(DbStructureStep.Column.MAX_SUBMISSION_COUNT)

        val review = cursor.getString(columnIndexPeerReview)

        //        step.setIs_custom_passed(isAssignmentByStepViewed(step.getId()));
        return Step(
                discussionsCount = cursor.getInt(columnIndexDiscussionCount),
                discussionProxy = cursor.getString(columnIndexDiscussionId),
                id = cursor.getLong(columnIndexStepId),
                lesson = cursor.getLong(columnIndexLessonId),
                createDate = dateAdapter.stringToDate(cursor.getString(columnIndexCreateDate)),
                status = Step.Status.byName(cursor.getString(columnIndexStatus)),
                progress = cursor.getString(columnIndexProgress),
                viewedBy = cursor.getLong(columnIndexViewedBy),
                passedBy = cursor.getLong(columnIndexPassedBy),
                updateDate = dateAdapter.stringToDate(cursor.getString(columnIndexUpdateDate)),
                subscriptions = DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions))?.toList(),
                position = cursor.getLong(columnIndexPosition),
                isCached = cursor.getInt(columnIndexIsCached) > 0,
                isLoading = cursor.getInt(columnIndexIsLoading) > 0,
                hasSubmissionRestriction = cursor.getInt(indexHasSubmissionRestriction) > 0,
                maxSubmissionCount = cursor.getInt(indexMaxSubmission),
                actions = Actions(false, false, null, review, null)
        )
    }

    public override fun getContentValues(step: Step): ContentValues {
        val values = ContentValues()

        values.put(DbStructureStep.Column.STEP_ID, step.id)
        values.put(DbStructureStep.Column.LESSON_ID, step.lesson)
        values.put(DbStructureStep.Column.STATUS, step.status?.name)
        values.put(DbStructureStep.Column.PROGRESS, step.progress)
        values.put(DbStructureStep.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(step.subscriptions?.toTypedArray()))
        values.put(DbStructureStep.Column.VIEWED_BY, step.viewedBy)
        values.put(DbStructureStep.Column.PASSED_BY, step.passedBy)
        values.put(DbStructureStep.Column.CREATE_DATE, dateAdapter.dateToString(step.createDate))
        values.put(DbStructureStep.Column.UPDATE_DATE, dateAdapter.dateToString(step.updateDate))
        values.put(DbStructureStep.Column.POSITION, step.position)
        values.put(DbStructureStep.Column.DISCUSSION_COUNT, step.discussionsCount)
        values.put(DbStructureStep.Column.DISCUSSION_ID, step.discussionProxy)
        values.put(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION, step.hasSubmissionRestriction)
        values.put(DbStructureStep.Column.MAX_SUBMISSION_COUNT, step.maxSubmissionCount)
        values.put(DbStructureStep.Column.PEER_REVIEW, step.actions?.doReview)

        return values
    }

    public override fun getDbName(): String = DbStructureStep.STEPS

    public override fun getDefaultPrimaryColumn(): String = DbStructureStep.Column.STEP_ID

    public override fun getDefaultPrimaryValue(persistentObject: Step): String =
            persistentObject.id.toString()

    override fun get(whereColumnName: String, whereValue: String): Step? =
            super.get(whereColumnName, whereValue)?.let(this::addInnerObjects)

    override fun getAllWithQuery(query: String, whereArgs: Array<String>?): List<Step> =
            super.getAllWithQuery(query, whereArgs).map(this::addInnerObjects)

    private fun addInnerObjects(step: Step): Step {
        val blockPersistentWrapper = blockWrapperDao.get(DbStructureBlock.Column.STEP_ID, step.id.toString())
        if (blockPersistentWrapper != null) {
            step.block = blockPersistentWrapper.block
        }

        val assignment = assignmentDao.get(DbStructureAssignment.Column.STEP_ID, step.id.toString())
        if (assignment?.progress != null) {
            val progress = progressDao.get(DbStructureProgress.Column.ID, assignment.progress!!)
            if (progress != null) {
                step.isCustomPassed = progress.isPassed
            }
        } else {
            if (step.progress != null) {
                val progress = progressDao.get(DbStructureProgress.Column.ID, step.progress!!)
                if (progress != null) {
                    step.isCustomPassed = progress.isPassed
                }
            }
        }

        return step
    }

    override fun insertOrUpdate(persistentObject: Step) {
        super.insertOrUpdate(persistentObject)
        persistentObject.block?.let { blockWrapperDao.insertOrUpdate(BlockPersistentWrapper(it, persistentObject.id)) }
    }
}
