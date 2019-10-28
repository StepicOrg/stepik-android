package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.BlockPersistentWrapper
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.step.structure.DbStructureStep
import org.stepik.android.model.Actions
import org.stepik.android.model.Step
import javax.inject.Inject

class StepDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations,
    private val blockWrapperDao: IDao<BlockPersistentWrapper>
) : DaoBase<Step>(databaseOperations) {

    public override fun parsePersistentObject(cursor: Cursor): Step {
        val review = cursor.getString(DbStructureStep.Column.PEER_REVIEW)

        return Step(
            id = cursor.getLong(DbStructureStep.Column.ID),
            lesson = cursor.getLong(DbStructureStep.Column.LESSON_ID),
            status = Step.Status.byName(cursor.getString(DbStructureStep.Column.STATUS)),
            progress = cursor.getString(DbStructureStep.Column.PROGRESS),

            viewedBy = cursor.getLong(DbStructureStep.Column.VIEWED_BY),
            passedBy = cursor.getLong(DbStructureStep.Column.PASSED_BY),
            worth = cursor.getLong(DbStructureStep.Column.WORTH),

            createDate = cursor.getDate(DbStructureStep.Column.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureStep.Column.UPDATE_DATE),

            subscriptions = DbParseHelper.parseStringToStringList(cursor.getString(DbStructureStep.Column.SUBSCRIPTION)),
            position = cursor.getLong(DbStructureStep.Column.POSITION),
            hasSubmissionRestriction = cursor.getBoolean(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION),
            maxSubmissionCount = cursor.getInt(DbStructureStep.Column.MAX_SUBMISSION_COUNT),

            discussionsCount = cursor.getInt(DbStructureStep.Column.DISCUSSION_COUNT),
            discussionProxy = cursor.getString(DbStructureStep.Column.DISCUSSION_PROXY),
            discussionThreads = DbParseHelper.parseStringToStringList(cursor.getString(DbStructureStep.Column.DISCUSSION_THREADS)),

            actions = Actions(
                vote = false, edit = false, delete = false, pin = false,
                testSection = null,
                doReview = review,
                editInstructions = null
            )
        )
    }

    public override fun getContentValues(step: Step): ContentValues {
        val values = ContentValues()

        values.put(DbStructureStep.Column.ID, step.id)
        values.put(DbStructureStep.Column.LESSON_ID, step.lesson)
        values.put(DbStructureStep.Column.STATUS, step.status?.name)
        values.put(DbStructureStep.Column.PROGRESS, step.progress)
        values.put(DbStructureStep.Column.SUBSCRIPTION, DbParseHelper.parseStringArrayToString(step.subscriptions?.toTypedArray()))
        values.put(DbStructureStep.Column.VIEWED_BY, step.viewedBy)
        values.put(DbStructureStep.Column.PASSED_BY, step.passedBy)
        values.put(DbStructureStep.Column.WORTH, step.worth)
        values.put(DbStructureStep.Column.CREATE_DATE, step.createDate?.time ?: -1)
        values.put(DbStructureStep.Column.UPDATE_DATE, step.updateDate?.time ?: -1)
        values.put(DbStructureStep.Column.POSITION, step.position)
        values.put(DbStructureStep.Column.DISCUSSION_COUNT, step.discussionsCount)
        values.put(DbStructureStep.Column.DISCUSSION_PROXY, step.discussionProxy)
        values.put(DbStructureStep.Column.DISCUSSION_THREADS, DbParseHelper.parseStringArrayToString(step.discussionThreads?.toTypedArray()))
        values.put(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION, step.hasSubmissionRestriction)
        values.put(DbStructureStep.Column.MAX_SUBMISSION_COUNT, step.maxSubmissionCount)
        values.put(DbStructureStep.Column.PEER_REVIEW, step.actions?.doReview)

        return values
    }

    public override fun getDbName(): String =
        DbStructureStep.TABLE_NAME

    public override fun getDefaultPrimaryColumn(): String =
        DbStructureStep.Column.ID

    public override fun getDefaultPrimaryValue(persistentObject: Step): String =
        persistentObject.id.toString()

    override fun get(whereColumnName: String, whereValue: String): Step? =
        super.get(whereColumnName, whereValue)?.let(this::addInnerObjects)

    override fun getAllWithQuery(query: String, whereArgs: Array<String>?): List<Step> =
        super.getAllWithQuery(query, whereArgs).map(this::addInnerObjects)

    private fun addInnerObjects(step: Step): Step {
        step.block = blockWrapperDao.get(DbStructureBlock.Column.STEP_ID, step.id.toString())?.block
        return step
    }

    override fun insertOrReplace(persistentObject: Step) {
        super.insertOrUpdate(persistentObject)
        persistentObject.block?.let { blockWrapperDao.insertOrReplace(BlockPersistentWrapper(it, persistentObject.id)) }
    }

    override fun insertOrReplaceAll(persistentObjects: List<Step>) {
        super.insertOrReplaceAll(persistentObjects)
        blockWrapperDao.insertOrReplaceAll(
            persistentObjects
                .mapNotNull { step -> step.block?.let { BlockPersistentWrapper(it, step.id) }}
        )
    }
}
