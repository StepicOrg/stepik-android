package org.stepic.droid.storage.operations

import android.database.Cursor
import org.stepic.droid.model.StepInfo
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.storage.structure.DbStructureStep
import org.stepic.droid.util.*
import javax.inject.Inject

class StepInfoOperationImpl
@Inject constructor(private val crudOperations: CrudOperations) : StepInfoOperation {

    override fun getStepInfo(stepIds: List<Long>): List<StepInfo> {
        val commaSeparatedIds = DbParseHelper.parseLongArrayToString(stepIds.toLongArray(), AppConstants.COMMA)

        val stepTable = DbStructureStep.STEPS
        val blockTable = DbStructureBlock.BLOCKS

        //select steps with blocks, where step id in requested range
        val query =
                "Select $stepTable.${DbStructureStep.Column.STEP_ID}, $stepTable.${DbStructureStep.Column.IS_CACHED}, $blockTable.${DbStructureBlock.Column.NAME} " +
                        "from $stepTable " +
                        "inner join $blockTable on $stepTable.${DbStructureStep.Column.STEP_ID} = $blockTable.${DbStructureBlock.Column.STEP_ID} " +
                        "and $stepTable.${DbStructureStep.Column.STEP_ID} in ($commaSeparatedIds)"

        return crudOperations.executeQuery(query, null, ResultHandler<List<StepInfo>> { cursor ->
            val stepInfoList = mutableListOf<StepInfo>()
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val stepInfo = cursorToStepInfo(cursor)
                stepInfoList.add(stepInfo)
                cursor.moveToNext()
            }

            stepInfoList
        })
    }

    private fun cursorToStepInfo(cursor: Cursor): StepInfo {
        return StepInfo(
                stepId = cursor.getLong(DbStructureStep.Column.STEP_ID),
                name = cursor.getString(DbStructureBlock.Column.NAME),
                isCached = cursor.getBoolean(DbStructureStep.Column.IS_CACHED))
    }
}
