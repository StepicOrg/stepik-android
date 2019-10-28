package org.stepik.android.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class StepSource(
    @SerializedName("id")
    val id: Long,

    @SerializedName("lesson")
    val lesson: Long,

    @SerializedName("position")
    val position: Int,

    @SerializedName("block")
    val block: Block,

    @SerializedName("reason_of_failure")
    val reasonOfFaulure: JsonElement?,

    @SerializedName("error")
    val error: JsonElement?,

    @SerializedName("warnings")
    val warnings: JsonElement?,

    @SerializedName("instruction_id")
    val instructionId: JsonElement?,

    @SerializedName("has_instruction")
    val hasInstruction: JsonElement?,

    @SerializedName("cost")
    val cost: JsonElement?,

    @SerializedName("is_solutions_unlocked")
    val isSolutionsUnlocked: JsonElement?,

    @SerializedName("solutions_unlocked_attempts")
    val solutionsUnlockedAttempts: JsonElement?,

    @SerializedName("max_submissions_count")
    val maxSubmissionsCount: JsonElement?,

    @SerializedName("has_submissions_restrictions")
    val hasSubmissionsRestrictions: JsonElement?,

    @SerializedName("create_date")
    val createDate: JsonElement?,

    @SerializedName("actions")
    val actions: JsonElement?,

    @SerializedName("instruction_type")
    val instructionType: JsonElement?,

    @SerializedName("status")
    val status: JsonElement?,

    @SerializedName("instruction")
    val instruction: JsonElement?
)